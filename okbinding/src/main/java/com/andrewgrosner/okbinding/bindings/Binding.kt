package com.andrewgrosner.okbinding.bindings

import com.andrewgrosner.okbinding.Observable
import com.andrewgrosner.okbinding.ObservableField
import kotlin.reflect.KProperty


interface Binding<Input> {
    fun convertValue(): Input

    fun <Output> bind(bindingOn: BindingOn<Input, Output>)
    fun <Output> unbind(bindingOn: BindingOn<Input, Output>)
}

fun <Input> bind(observableField: ObservableField<Input>) = ObservableBinding(observableField)

fun <Input> bind(expression: () -> Input) = InputExpressionBinding(expression)

class ObservableBinding<Input>(val observableField: ObservableField<Input>)
    : Binding<Input> {

    private var bindingOn: BindingOn<Input, *>? = null

    override fun convertValue() = observableField.value

    override fun <Output> bind(bindingOn: BindingOn<Input, Output>) {
        this.bindingOn = bindingOn
        observableField.addOnPropertyChangedCallback(this::propertyChanged)
    }

    override fun <Output> unbind(bindingOn: BindingOn<Input, Output>) {
        observableField.removeOnPropertyChangedCallback(this::propertyChanged)
        this.bindingOn = null
    }

    fun propertyChanged(observable: Observable, kProperty: KProperty<*>?) {
        bindingOn?.notifyValueChange()
    }
}

class InputExpressionBinding<Input>(val expression: () -> Input) : Binding<Input> {
    override fun convertValue() = expression()


    override fun <Output> bind(bindingOn: BindingOn<Input, Output>) {
    }

    override fun <Output> unbind(bindingOn: BindingOn<Input, Output>) {
    }

}