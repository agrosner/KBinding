package com.andrewgrosner.okbinding

import com.andrewgrosner.okbinding.bindings.BindingOn
import com.andrewgrosner.okbinding.bindings.ObservableBinding
import org.jetbrains.anko.AnkoComponent
import kotlin.reflect.KProperty

/**
 * Description:
 */
abstract class ViewModelComponent<T, V>(viewModel: V) : AnkoComponent<T> {


    private val bindings = mutableListOf<BindingOn<*, *, *>>()
    private val propertyBindings = mutableMapOf<KProperty<*>, MutableList<BindingOn<*, *, *>>>()

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

    fun <Input, Output> oneWay(bindingOn: BindingOn<Input, Output, ObservableBinding<Input>>) {
        bindings += bindingOn
    }

    fun <Input, Output> oneWay(kProperty: KProperty<*>, bindingOn: BindingOn<Input, Output, *>) {
        var mutableList = propertyBindings[kProperty]
        if (mutableList == null) {
            mutableList = mutableListOf()
            propertyBindings[kProperty] = mutableList
        }
        mutableList.add(bindingOn)
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