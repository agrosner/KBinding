package com.andrewgrosner.okbinding

import android.view.View
import org.jetbrains.anko.AnkoComponent

/**
 * Description:
 */
abstract class ViewModelComponent<T, V>(viewModel: V) : AnkoComponent<T> {

    val holder = BindingHolder()

    val onViewModelChanged = { observable: Observable, id: Int -> onFieldChanged(id) }

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

    fun View.bind(boundObservableField: BoundObservableField) {
        holder.boundObservableFields += id to boundObservableField
    }

    fun View.bind(boundField: BoundField<*, View>) {
        holder.boundFields += id to boundField
    }

    private fun onFieldChanged(changedFieldId: Int) {
        val boundField = holder.boundFields[changedFieldId]
        // reinvoke method with referenced field value
        boundField?.setterFunction?.invoke(boundField.view, boundField.property.call())
    }

    fun destroyView() {
        holder.unregisterAll()
    }
}