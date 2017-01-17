package com.andrewgrosner.okbinding

import org.jetbrains.anko.AnkoComponent
import kotlin.reflect.KProperty

/**
 * Description:
 */
abstract class ViewModelComponent<T, V>(viewModel: V) : AnkoComponent<T> {

    val holder = BindingHolder()

    val onViewModelChanged = { observable: Observable, property: KProperty<*>? -> onFieldChanged(property) }

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

    fun bind(bindingObject: BaseBindingObject<*, *>) = holder.putBinding(bindingObject)

    private fun onFieldChanged(property: KProperty<*>?) {
        if (property != null) {
            holder.bindings[property]?.let { it.forEach { it.rebind() } }
        } else {
            // rebind everything if the parent ViewModel changes.
            holder.rebind()
        }
    }

    fun destroyView() {
        val viewModel = viewModel
        if (viewModel is Observable) {
            viewModel.removeOnPropertyChangedCallback(onViewModelChanged)
        }
        holder.discardAll()
    }
}