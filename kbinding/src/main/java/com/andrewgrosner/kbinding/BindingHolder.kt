package com.andrewgrosner.kbinding

import android.app.Activity
import android.app.Fragment
import android.view.View
import com.andrewgrosner.kbinding.bindings.Binding
import com.andrewgrosner.kbinding.bindings.InputExpressionBindingConverter
import com.andrewgrosner.kbinding.bindings.NullableInputExpressionBindingConverter
import com.andrewgrosner.kbinding.bindings.ObservableBindingConverter
import com.andrewgrosner.kbinding.bindings.OneWayBinding
import com.andrewgrosner.kbinding.bindings.OneWayToSource
import com.andrewgrosner.kbinding.bindings.TwoWayBinding
import com.andrewgrosner.kbinding.bindings.ViewBinder
import com.andrewgrosner.kbinding.bindings.ViewRegister
import com.andrewgrosner.kbinding.bindings.onSelf
import kotlin.reflect.KProperty

/**
 * Internal interface class that's used to consolidate the functionality between a [BindingRegister]
 * and [BindingHolder].
 */
interface BindingRegister<V> {

    /**
     * Starts an [Observable] expression that executes when the attached observable changes.
     */
    fun <Input> bind(function: (V) -> ObservableField<Input>) = ObservableBindingConverter(function, this)

    /**
     * Starts a [KProperty] expression that executes when the [BaseObservable] notifies its value changes.
     */
    fun <Input> bind(kProperty: KProperty<*>? = null, expression: (V) -> Input) = InputExpressionBindingConverter(expression, kProperty, this)


    /**
     * Starts a [KProperty] expression that executes when the [BaseObservable] notifies its value changes.
     * Uses reflection in the getter of the [KProperty] to retrieve the value of the property.
     */
    fun <Input> bind(kProperty: KProperty<Input>) = InputExpressionBindingConverter({ kProperty.getter.call(it) }, kProperty, this)

    /**
     * Starts a [KProperty] expression that executes even when the possibility
     * of the parent [BaseObservable] in this [BindingRegister] is null. Useful for loading state
     * or when expected data is not present yet in the view.
     */
    fun <Input> bindNullable(kProperty: KProperty<*>? = null, expression: (V?) -> Input) = NullableInputExpressionBindingConverter(expression, kProperty, this)

    /**
     * Starts an [Observable] that unwraps the [ObservableField.value] from the function when the value
     * changes.
     */
    fun <Input> bindSelf(function: (V) -> ObservableField<Input>) = bind(function).onSelf()

    /**
     * Starts a [KProperty] expression that passes the result of the expression to the final view expression.
     */
    fun <Input> bindSelf(kProperty: KProperty<*>, expression: (V) -> Input) = bind(kProperty, expression).onSelf()

    /**
     * Starts a [KProperty] expression that passes the result of the expression to the final view expression.
     * Uses reflection to get the value of the property instead of declaring redundant call.
     */
    fun <Input> bindSelf(kProperty: KProperty<Input>) = bind(kProperty).onSelf()

    /**
     * Starts a OneWayToSource [View] expression that will evaluate from the [View] onto the next expression.
     */
    fun <Output, VW : View> bind(v: VW, viewRegister: ViewRegister<VW, Output>) = ViewBinder(v, viewRegister, this)

    /**
     * The data registered on the holder. Use this to pass down variables and content that you expect
     * to change.
     */
    var viewModel: V?

    /**
     * Non-null safe access expression. Will throw a [KotlinNullPointerException] if null. This is
     * assuming the viewmodel exists.
     */
    val viewModelSafe: V
        get() = viewModel!!

    /**
     * Returns true if the [viewModel] is bound.
     */
    var isBound: Boolean

    /**
     * Internal helper method to register OneWay binding.
     */
    fun registerBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>)

    /**
     * Internal helper method to unregister OneWay binding.
     */
    fun unregisterBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>)

    /**
     * Internal helper method to register TwoWay binding.
     */
    fun registerBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>)

    /**
     * Internal helper method to unregister TwoWay binding.
     */
    fun unregisterBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>)

    /**
     * Internal helper method to register OneWayToSource binding.
     */
    fun registerBinding(oneWayToSource: OneWayToSource<V, *, *, *>)

    /**
     * Internal helper method to unregister OneWayToSource binding.
     */
    fun unregisterBinding(oneWayToSource: OneWayToSource<V, *, *, *>)

    /**
     * Called when a [viewModel] is set. Evaluates all binding expressions as well as binds their changes
     * to this [BindingHolder].
     */
    fun bindAll()

    /**
     * Call this to unregister all bindings from this [BindingHolder]. Preferrably in the [Activity.onDestroy]
     * or [Fragment.onDestroyView]
     */
    fun unbindAll()

    /**
     * Forces a reevaluation of all bindings
     */
    fun notifyChanges()
}

/**
 * Represents a set of [Binding]. Provides convenience operations and handles changes from the parent
 * [viewModel] if it is an [Observable].
 */
class BindingHolder<V>(viewModel: V? = null) : BindingRegister<V> {

    private val observableBindings = mutableListOf<OneWayBinding<V, *, *, *, *>>()
    private val genericOneWayBindings = mutableListOf<OneWayBinding<V, *, *, *, *>>()
    private val propertyBindings = mutableMapOf<KProperty<*>, MutableList<OneWayBinding<V, *, *, *, *>>>()

    private val twoWayBindings = mutableListOf<TwoWayBinding<V, *, *, *, *>>()
    private val twoWayPropertyBindings = mutableMapOf<KProperty<*>, TwoWayBinding<V, *, *, *, *>>()

    private val sourceBindings = mutableMapOf<View, MutableList<OneWayToSource<V, *, *, *>>>()

    val onViewModelChanged = { _: Observable, property: KProperty<*>? -> onFieldChanged(property) }

    override var isBound = false

    override var viewModel: V? = viewModel
        set(value) {
            if (field != value) {
                // existing field remove property changes
                if (field is Observable) {
                    (field as Observable).removeOnPropertyChangedCallback(onViewModelChanged)
                }
                field = value

                if (isBound) bindAll()
            }
        }

    override fun registerBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>) {
        if (oneWayBinding.converter is ObservableBindingConverter) {
            observableBindings += oneWayBinding
        } else if (oneWayBinding.converter is InputExpressionBindingConverter) {
            val kProperty = oneWayBinding.converter.property
            if (kProperty != null) {
                var mutableList = propertyBindings[kProperty]
                if (mutableList == null) {
                    mutableList = mutableListOf()
                    propertyBindings[kProperty] = mutableList
                }
                mutableList.add(oneWayBinding)
            } else {
                // generic observableBindings have no property association
                genericOneWayBindings += oneWayBinding
            }
        }
    }

    override fun unregisterBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>) {
        if (oneWayBinding.converter is ObservableBindingConverter) {
            observableBindings -= oneWayBinding
        } else if (oneWayBinding.converter is InputExpressionBindingConverter) {
            val kProperty = oneWayBinding.converter.property
            if (kProperty != null) {
                propertyBindings[kProperty]?.remove(oneWayBinding)
            } else {
                genericOneWayBindings -= oneWayBinding
            }
        }
    }

    override fun registerBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>) {
        val converter = twoWayBinding.oneWayBinding.converter
        if (converter is ObservableBindingConverter) {
            if (twoWayBindings.contains(twoWayBinding)) {
                throw IllegalStateException("Cannot register more than one two way binding on an Observable field. This could result in a view update cycle.")
            }
            twoWayBindings += twoWayBinding
        } else if (converter is InputExpressionBindingConverter) {
            val key = converter.property
            if (twoWayPropertyBindings.containsKey(key)) {
                throw IllegalStateException("Cannot register more than one two way binding to property updates. This could result in a view update cycle.")
            }
            if (key != null) {
                twoWayPropertyBindings[key] = twoWayBinding
            } else {
                throw IllegalStateException("Cannot register generic two way binding. Specify a property.")
            }
        }
    }

    override fun unregisterBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>) {
        val converter = twoWayBinding.oneWayBinding.converter
        if (converter is ObservableBindingConverter) {
            twoWayBindings -= twoWayBinding
        } else if (converter is InputExpressionBindingConverter) {
            val key = converter.property
            if (key != null) {
                twoWayPropertyBindings -= key
            }
        }
    }

    override fun registerBinding(oneWayToSource: OneWayToSource<V, *, *, *>) {
        val view = oneWayToSource.view
        var mutableList = sourceBindings[view]
        if (mutableList == null) {
            mutableList = mutableListOf()
            sourceBindings[view] = mutableList
        }
        mutableList.add(oneWayToSource)
    }

    override fun unregisterBinding(oneWayToSource: OneWayToSource<V, *, *, *>) {
        val view = oneWayToSource.view
        sourceBindings[view]?.remove(oneWayToSource)
    }

    private fun onFieldChanged(property: KProperty<*>?) {
        if (property != null) {
            propertyBindings[property]?.let { it.forEach { it.notifyValueChange() } }
            twoWayPropertyBindings[property]?.notifyValueChange()
        } else {
            // rebind everything if the parent ViewModel changes.
            propertyBindings.forEach { _, bindings -> bindings.forEach { it.notifyValueChange() } }
            twoWayPropertyBindings.forEach { _, binding -> binding.notifyValueChange() }
            genericOneWayBindings.forEach { it.notifyValueChange() }
        }
    }

    override fun notifyChanges() {
        if (!isBound) {
            throw IllegalStateException("Cannot notify changes on an unbound binding holder.")
        }
        observableBindings.forEach { it.notifyValueChange() }
        sourceBindings.values.forEach { bindings -> bindings.forEach { it.notifyValueChange() } }
        propertyBindings.values.forEach { bindings -> bindings.forEach { it.notifyValueChange() } }

        twoWayBindings.forEach { it.notifyValueChange() }
        twoWayPropertyBindings.values.forEach { it.notifyValueChange() }
        genericOneWayBindings.forEach { it.notifyValueChange() }
    }

    override fun bindAll() {
        // new field is observable, register now for changes
        val viewModel = this.viewModel
        if (viewModel is Observable) {
            viewModel.addOnPropertyChangedCallback(onViewModelChanged)
        }

        observableBindings.forEach { it.bind() }
        sourceBindings.values.forEach { bindings -> bindings.forEach { it.bind() } }
        propertyBindings.values.forEach { bindings -> bindings.forEach { it.bind() } }

        twoWayBindings.forEach { it.bind() }
        twoWayPropertyBindings.values.forEach { it.bind() }
        genericOneWayBindings.forEach { it.bind() }

        isBound = true
    }

    override fun unbindAll() {
        val viewModel = viewModel
        if (viewModel is Observable) {
            viewModel.removeOnPropertyChangedCallback(onViewModelChanged)
        }
        observableBindings.forEach { it.unbindInternal() }
        observableBindings.clear()

        sourceBindings.values.forEach { bindings -> bindings.forEach { it.unbindInternal() } }
        sourceBindings.clear()

        propertyBindings.values.forEach { bindings -> bindings.forEach { it.unbindInternal() } }
        propertyBindings.clear()

        genericOneWayBindings.forEach { it.unbindInternal() }
        genericOneWayBindings.clear()

        twoWayBindings.forEach { it.unbindInternal() }
        twoWayBindings.clear()
        twoWayPropertyBindings.values.forEach { it.unbindInternal() }
        twoWayPropertyBindings.clear()

        isBound = false
    }
}