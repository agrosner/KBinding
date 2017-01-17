package com.andrewgrosner.okbinding

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import kotlin.reflect.KProperty


class BindingField<V : View>(val property: KProperty<*>,
                             val view: V) {

    fun <T> exp(function: () -> T) = BindingObject(this, function)

    fun <T> obs(observable: ObservableField<T>) = ObservableBindingObject(this, observable)

    fun obs(observable: ObservableBoolean) = BooleanObservableBindingObject(this, observable)
}

abstract class BaseBindingObject<T, V : View>(val field: BindingField<V>) {

    protected var viewOperation: ((V, T) -> Unit)? = null

    infix fun toView(viewOperation: (V, T) -> Unit): BaseBindingObject<T, V> {
        this.viewOperation = viewOperation
        return this
    }

    abstract fun rebind()

    open fun discard() = Unit
}


/**
 * Description: Holds the object operated on
 */
class BindingObject<T, V : View>(field: BindingField<V>,
                                 val expression: () -> T) : BaseBindingObject<T, V>(field) {

    private var ifNull: (() -> T)? = null

    private var defaultValue: T? = null

    fun ifNull(lazyNullExpression: () -> T): BindingObject<T, V> {
        ifNull = lazyNullExpression
        return this
    }

    fun defaultValue(default: T): BindingObject<T, V> {
        defaultValue = default
        return this
    }

    override fun rebind() {
        viewOperation?.invoke(field.view, expression())
    }
}

abstract class BaseObservableBindingObject<T, V : View>(field: BindingField<V>,
                                                        val observable: Observable)
    : BaseBindingObject<T, V>(field) {

    val callback: ((Observable, KProperty<*>?) -> Unit) = {
        observable, i ->
        // field change on this observable calls for rebind.
        rebind()
    }

    init {
        observable.addOnPropertyChangedCallback(callback)
    }

    override fun discard() {
        observable.removeOnPropertyChangedCallback(callback)
    }
}

@Suppress("UNCHECKED_CAST")
class ObservableBindingObject<T, V : View>(field: BindingField<V>,
                                           observable: ObservableField<T>)
    : BaseObservableBindingObject<T, V>(field, observable) {

    override fun rebind() {
        viewOperation?.invoke(field.view, (this.observable as ObservableField<T>).value)
    }

}

@Suppress("UNCHECKED_CAST")
class BooleanObservableBindingObject<V : View>(field: BindingField<V>,
                                               observable: ObservableBoolean)
    : BaseObservableBindingObject<Boolean, V>(field, observable) {

    override fun rebind() {
        viewOperation?.invoke(field.view, (this.observable as ObservableBoolean).value)
    }

}

fun <V : View> V.field(kProperty: KProperty<*>) = BindingField(kProperty, this)

fun <T : CharSequence?, TV : TextView> BaseBindingObject<T, TV>.toText()
        : BaseBindingObject<T, TV> {
    toView { view, it -> view.text = it }
    return this
}

fun <V : CompoundButton> BaseBindingObject<Boolean, V>.toCheckedChange()
        : BaseBindingObject<Boolean, V> {
    toView { v, t -> v.isChecked = t }
    return this
}

