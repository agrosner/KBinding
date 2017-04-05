package com.andrewgrosner.okbinding

import com.andrewgrosner.okbinding.bindings.Binding
import com.andrewgrosner.okbinding.bindings.ObservableBindingConverter
import com.andrewgrosner.okbinding.bindings.OneWayBinding
import com.andrewgrosner.okbinding.bindings.TwoWayBinding
import kotlin.reflect.KProperty

/**
 * Represents a set of [Binding]. Provides convenience operations and handles changes from the parent
 * [viewModel] if it is an [Observable].
 */
class BindingHolder<V>(viewModel: V) {

    private val bindings = mutableListOf<OneWayBinding<*, *, *, *>>()
    private val propertyBindings = mutableMapOf<KProperty<*>, MutableList<OneWayBinding<*, *, *, *>>>()

    private val twoWayBindings = mutableMapOf<ObservableField<*>, TwoWayBinding<*, *, *, *>>()
    private val twoWayPropertyBindings = mutableMapOf<KProperty<*>, TwoWayBinding<*, *, *, *>>()

    val onViewModelChanged = { _: Observable, property: KProperty<*>? -> onFieldChanged(property) }

    var viewModel: V = viewModel
        set(value) {
            if (field != value) {
                // existing field remove property changes
                if (field is Observable) {
                    (field as Observable).removeOnPropertyChangedCallback(onViewModelChanged)
                }
                field = value

                // new field is observable, register now for changes
                if (value is Observable) {
                    value.addOnPropertyChangedCallback(onViewModelChanged)
                }
            }
        }

    fun <Input, Output> oneWay(oneWayBinding: OneWayBinding<Input, Output, ObservableBindingConverter<Input>, *>) {
        bindings += oneWayBinding
    }

    fun <Input, Output> oneWay(kProperty: KProperty<*>, oneWayBinding: OneWayBinding<Input, Output, *, *>) {
        var mutableList = propertyBindings[kProperty]
        if (mutableList == null) {
            mutableList = mutableListOf()
            propertyBindings[kProperty] = mutableList
        }
        mutableList.add(oneWayBinding)
    }

    fun <Input, Output> twoWay(twoWayBinding: TwoWayBinding<Input, Output, ObservableBindingConverter<Input>, *>) {
        val observableField = twoWayBinding.oneWayBinding.binding.observableField
        if (twoWayBindings.containsKey(observableField)) {
            throw IllegalStateException("Cannot register more than one two way binding on an Observable field. This could result in a view update cycle.")
        }
        twoWayBindings[observableField] = twoWayBinding
    }

    fun <Input, Output> twoWay(kProperty: KProperty<*>, oneWayBinding: TwoWayBinding<Input, Output, *, *>) {
        val key = kProperty
        if (twoWayPropertyBindings.containsKey(key)) {
            throw IllegalStateException("Cannot register more than one two way binding to property updates. This could result in a view update cycle.")
        }
        twoWayPropertyBindings[key] = oneWayBinding
    }

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(kProperty: KProperty<*>)
            = twoWayPropertyBindings.getOrElse(kProperty) { throw IllegalArgumentException("Could not find two way binding for $kProperty.") } as TwoWayBinding<*, Output, *, *>

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(observableField: ObservableField<Output>)
            = twoWayBindings.getOrElse(observableField, { throw IllegalArgumentException("Could not find two way binding for observable field.") }) as TwoWayBinding<ObservableField<Output>, Output, *, *>

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

    fun unbind() {
        val viewModel = viewModel
        if (viewModel is Observable) {
            viewModel.removeOnPropertyChangedCallback(onViewModelChanged)
        }
        bindings.forEach { it.unbind() }
        bindings.clear()

        propertyBindings.forEach { _, bindings -> bindings.forEach { it.unbind() } }
        propertyBindings.clear()

        twoWayBindings.values.forEach { it.unbind() }
        twoWayPropertyBindings.values.forEach { it.unbind() }
    }
}