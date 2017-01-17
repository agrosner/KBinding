package com.andrewgrosner.okbinding

import kotlin.reflect.KProperty

/**
 * Description:
 */
class BindingHolder {

    val bindings = hashMapOf<KProperty<*>, List<BaseBindingObject<*, *>>>()

    fun discardAll() {
        val uniqueSet = mutableSetOf<BaseBindingObject<*, *>>()
        bindings.values.forEach { lists -> lists.forEach { uniqueSet += it } }
        uniqueSet.forEach { it.discard() }
    }

    fun rebind() {
        val uniqueSet = mutableSetOf<BaseBindingObject<*, *>>()
        bindings.values.forEach { lists -> lists.forEach { uniqueSet += it } }
        uniqueSet.forEach { it.rebind() }
    }

    fun putBinding(baseBindingObject: BaseBindingObject<*, *>) {
        var bindings = bindings[baseBindingObject.field.property]
        if (bindings == null) {
            bindings = arrayListOf()
            this.bindings[baseBindingObject.field.property] = bindings
        }
        bindings += baseBindingObject
    }
}

