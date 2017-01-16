package com.andrewgrosner.okbinding

import android.view.View
import kotlin.reflect.KProperty

/**
 * Description:
 */
class BindingHolder {¬

    val boundObservableFields = hashMapOf<Int, BoundObservableField>()

    val boundFields = hashMapOf<Int, BoundField<*, View>>()

    fun unregisterAll() {
        boundObservableFields.values.forEach { it.observable.removeOnPropertyChangedCallback(it.function) }
        boundFields.values.clear()
    }

}

class BoundObservableField(val observable: Observable, val function: (Observable, Int) -> Unit)

class BoundField<T, V : View>(val view: V, val property: KProperty<T>, val setterFunction: (V, T) -> Unit)
