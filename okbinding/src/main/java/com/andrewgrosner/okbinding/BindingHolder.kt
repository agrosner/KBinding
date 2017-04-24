package com.andrewgrosner.okbinding

import android.view.View
import com.andrewgrosner.okbinding.bindings.*
import kotlin.reflect.KProperty


interface BindingRegister<V> {

    fun <Input> bind(function: (V) -> ObservableField<Input>) = ObservableBindingConverter(function, this)

    fun <Input> bind(kProperty: KProperty<*>, expression: (V) -> Input) = InputExpressionBindingConverter(kProperty, expression, this)

    fun <Input> bindSelf(function: (V) -> ObservableField<Input>) = bind(function).onSelf()

    fun <Input> bindSelf(kProperty: KProperty<*>, expression: (V) -> Input) = bind(kProperty, expression).onSelf()

    fun <Output, VW : View> bind(v: VW, viewRegister: ViewRegister<VW, Output>) = ViewBinder(v, viewRegister, this)

    var viewModel: V?

    fun registerBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>)

    fun unregisterBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>)

    fun registerBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>)

    fun unregisterBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>)

    fun registerBinding(oneWayToSource: OneWayToSource<V, *, *, *>)

    fun unregisterBinding(oneWayToSource: OneWayToSource<V, *, *, *>)

    fun bindAll()

    fun unbindAll()
}

/**
 * Represents a set of [Binding]. Provides convenience operations and handles changes from the parent
 * [viewModel] if it is an [Observable].
 */
class BindingHolder<V>(viewModel: V? = null) : BindingRegister<V> {

    private val bindings = mutableListOf<OneWayBinding<V, *, *, *, *>>()
    private val propertyBindings = mutableMapOf<KProperty<*>, MutableList<OneWayBinding<V, *, *, *, *>>>()

    private val twoWayBindings = mutableListOf<TwoWayBinding<V, *, *, *, *>>()
    private val twoWayPropertyBindings = mutableMapOf<KProperty<*>, TwoWayBinding<V, *, *, *, *>>()

    private val sourceBindings = mutableMapOf<View, MutableList<OneWayToSource<V, *, *, *>>>()

    val onViewModelChanged = { _: Observable, property: KProperty<*>? -> onFieldChanged(property) }

    private var isBound = false

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
            bindings += oneWayBinding
        } else if (oneWayBinding.converter is InputExpressionBindingConverter) {
            val kProperty = oneWayBinding.converter.property
            var mutableList = propertyBindings[kProperty]
            if (mutableList == null) {
                mutableList = mutableListOf()
                propertyBindings[kProperty] = mutableList
            }
            mutableList.add(oneWayBinding)
        }
    }

    override fun unregisterBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>) {
        if (oneWayBinding.converter is ObservableBindingConverter) {
            bindings -= oneWayBinding
        } else if (oneWayBinding.converter is InputExpressionBindingConverter) {
            val kProperty = oneWayBinding.converter.property
            propertyBindings[kProperty]?.remove(oneWayBinding)
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
            twoWayPropertyBindings[key] = twoWayBinding
        }
    }

    override fun unregisterBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>) {
        val converter = twoWayBinding.oneWayBinding.converter
        if (converter is ObservableBindingConverter) {
            twoWayBindings -= twoWayBinding
        } else if (converter is InputExpressionBindingConverter) {
            val key = converter.property
            twoWayPropertyBindings -= key
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
            twoWayPropertyBindings[property]?.notifyValueChange()
        }
    }

    override fun bindAll() {
        // new field is observable, register now for changes
        val viewModel = this.viewModel
        if (viewModel is Observable) {
            viewModel.addOnPropertyChangedCallback(onViewModelChanged)
        }

        bindings.forEach { it.bind() }

        sourceBindings.values.forEach { bindings -> bindings.forEach { it.bind() } }

        propertyBindings.values.forEach { bindings -> bindings.forEach { it.bind() } }

        twoWayBindings.forEach { it.bind() }
        twoWayPropertyBindings.values.forEach { it.bind() }

        isBound = true
    }

    override fun unbindAll() {
        val viewModel = viewModel
        if (viewModel is Observable) {
            viewModel.removeOnPropertyChangedCallback(onViewModelChanged)
        }
        bindings.forEach { it.unbindInternal() }
        bindings.clear()

        sourceBindings.values.forEach { bindings -> bindings.forEach { it.unbindInternal() } }
        sourceBindings.clear()

        propertyBindings.values.forEach { bindings -> bindings.forEach { it.unbindInternal() } }
        propertyBindings.clear()

        twoWayBindings.forEach { it.unbindInternal() }
        twoWayBindings.clear()
        twoWayPropertyBindings.values.forEach { it.unbindInternal() }
        twoWayPropertyBindings.clear()

        isBound = false
    }
}