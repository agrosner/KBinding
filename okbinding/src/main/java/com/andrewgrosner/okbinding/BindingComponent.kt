package com.andrewgrosner.okbinding

import com.andrewgrosner.okbinding.bindings.Binding
import com.andrewgrosner.okbinding.bindings.ObservableBindingConverter
import com.andrewgrosner.okbinding.bindings.OneWayBinding
import com.andrewgrosner.okbinding.bindings.TwoWayBinding
import org.jetbrains.anko.AnkoComponent
import kotlin.reflect.KProperty

abstract class BindingComponent<T, V>(viewModel: V) : AnkoComponent<T> {

    private val bindings = mutableListOf<Binding<*, *, *>>()
    private val propertyBindings = mutableMapOf<KProperty<*>, MutableList<Binding<*, *, *>>>()

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
        bindPropertyBinding(kProperty, oneWayBinding)
    }

    fun <Input, Output> twoWay(twoWayBinding: TwoWayBinding<Input, Output, ObservableBindingConverter<Input>, *>) {
        bindings += twoWayBinding
    }

    fun <Input, Output> twoWay(kProperty: KProperty<*>, oneWayBinding: TwoWayBinding<Input, Output, *, *>) {
        bindPropertyBinding(kProperty, oneWayBinding)
    }

    private fun <Input, Output> bindPropertyBinding(kProperty: KProperty<*>,
                                                    oneWayBinding: Binding<Input, Output, *>) {
        var mutableList = propertyBindings[kProperty]
        if (mutableList == null) {
            mutableList = mutableListOf()
            propertyBindings[kProperty] = mutableList
        }
        mutableList.add(oneWayBinding)
    }

    private fun onFieldChanged(property: KProperty<*>?) {
        if (property != null) {
            propertyBindings[property]?.let { it.forEach { it.notifyValueChange() } }
        } else {
            // rebind everything if the parent ViewModel changes.
            propertyBindings.forEach { _, bindings -> bindings.forEach { it.notifyValueChange() } }
        }
    }

    fun destroyView() {
        val viewModel = viewModel
        if (viewModel is Observable) {
            viewModel.removeOnPropertyChangedCallback(onViewModelChanged)
        }
        bindings.forEach { it.unbind() }
        bindings.clear()

        propertyBindings.forEach { _, bindings -> bindings.forEach { it.unbind() } }
        propertyBindings.clear()
    }
}