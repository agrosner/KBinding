package com.andrewgrosner.okbinding

import android.view.View
import com.andrewgrosner.okbinding.bindings.*
import kotlin.reflect.KProperty

/**
 * Represents a set of [Binding]. Provides convenience operations and handles changes from the parent
 * [viewModel] if it is an [Observable].
 */
class BindingHolder<V>(viewModel: V) {

    private val bindings = mutableListOf<OneWayBinding<V, *, *, *, *>>()
    private val propertyBindings = mutableMapOf<KProperty<*>, MutableList<OneWayBinding<V, *, *, *, *>>>()

    private val twoWayBindings = mutableMapOf<ObservableField<*>, TwoWayBinding<V, *, *, *, *>>()
    private val twoWayPropertyBindings = mutableMapOf<KProperty<*>, TwoWayBinding<V, *, *, *, *>>()

    private val sourceBindings = mutableMapOf<View, MutableList<OneWayToSource<V, *, *, *>>>()

    val onViewModelChanged = { _: Observable, property: KProperty<*>? -> onFieldChanged(property) }

    var viewModel: V = viewModel
        set(value) {
            if (field != value) {
                // existing field remove property changes
                if (field is Observable) {
                    (field as Observable).removeOnPropertyChangedCallback(onViewModelChanged)
                }
                field = value
            }
        }

    fun <Input> bind(observableField: ObservableField<Input>) = ObservableBindingConverter(observableField, this)

    fun <Input> bind(kProperty: KProperty<*>, expression: (V) -> Input) = InputExpressionBindingConverter(kProperty, expression, this)

    fun <Input> bindSelf(observableField: ObservableField<Input>) = bind(observableField).onSelf()

    fun <Input> bindSelf(kProperty: KProperty<*>, expression: (V) -> Input) = bind(kProperty, expression).onSelf()

    fun <Output, VW : View> bind(v: VW, viewRegister: ViewRegister<VW, Output>) = ViewBinder(v, viewRegister, this)

    internal fun registerBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>) {
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

    internal fun unregisterBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>) {
        if (oneWayBinding.converter is ObservableBindingConverter) {
            bindings -= oneWayBinding
        } else if (oneWayBinding.converter is InputExpressionBindingConverter) {
            val kProperty = oneWayBinding.converter.property
            propertyBindings[kProperty]?.remove(oneWayBinding)
        }
    }

    internal fun registerBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>) {
        val converter = twoWayBinding.oneWayBinding.converter
        if (converter is ObservableBindingConverter) {
            val observableField = converter.observableField
            if (twoWayBindings.containsKey(observableField)) {
                throw IllegalStateException("Cannot register more than one two way binding on an Observable field. This could result in a view update cycle.")
            }
            twoWayBindings[observableField] = twoWayBinding
        } else if (converter is InputExpressionBindingConverter) {
            val key = converter.property
            if (twoWayPropertyBindings.containsKey(key)) {
                throw IllegalStateException("Cannot register more than one two way binding to property updates. This could result in a view update cycle.")
            }
            twoWayPropertyBindings[key] = twoWayBinding
        }
    }

    internal fun unregisterBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>) {
        val converter = twoWayBinding.oneWayBinding.converter
        if (converter is ObservableBindingConverter) {
            val observableField = converter.observableField
            twoWayBindings -= observableField
        } else if (converter is InputExpressionBindingConverter) {
            val key = converter.property
            twoWayPropertyBindings -= key
        }
    }

    internal fun registerBinding(oneWayToSource: OneWayToSource<V, *, *, *>) {
        val view = oneWayToSource.view
        var mutableList = sourceBindings[view]
        if (mutableList == null) {
            mutableList = mutableListOf()
            sourceBindings[view] = mutableList
        }
        mutableList.add(oneWayToSource)
    }

    internal fun unregisterBinding(oneWayToSource: OneWayToSource<V, *, *, *>) {
        val view = oneWayToSource.view
        sourceBindings[view]?.remove(oneWayToSource)
    }

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(kProperty: KProperty<*>)
            = twoWayPropertyBindings.getOrElse(kProperty) { throw IllegalArgumentException("Could not find two way binding for $kProperty.") } as TwoWayBinding<V, *, Output, *, *>

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(observableField: ObservableField<Output>)
            = twoWayBindings.getOrElse(observableField, { throw IllegalArgumentException("Could not find two way binding for observable field.") }) as TwoWayBinding<V, ObservableField<Output>, Output, *, *>

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

    fun bindAll() {
        // new field is observable, register now for changes
        val viewModel = this.viewModel
        if (viewModel is Observable) {
            viewModel.addOnPropertyChangedCallback(onViewModelChanged)
        }

        bindings.forEach { it.bind(viewModel) }

        sourceBindings.values.forEach { bindings -> bindings.forEach { it.bind(Unit) } }

        propertyBindings.values.forEach { bindings -> bindings.forEach { it.bind(viewModel) } }

        twoWayBindings.values.forEach { it.bind(viewModel) }
        twoWayPropertyBindings.values.forEach { it.bind(viewModel) }
    }


    fun unbindAll() {
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

        twoWayBindings.values.forEach { it.unbindInternal() }
        twoWayBindings.clear()
        twoWayPropertyBindings.values.forEach { it.unbindInternal() }
        twoWayPropertyBindings.clear()
    }
}