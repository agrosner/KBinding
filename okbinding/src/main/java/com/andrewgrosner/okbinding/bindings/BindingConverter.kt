package com.andrewgrosner.okbinding.bindings

import com.andrewgrosner.okbinding.Observable
import com.andrewgrosner.okbinding.ObservableField
import kotlin.reflect.KProperty


interface BindingConverter<out Input> {
    fun convertValue(): Input

    fun bind(binding: Binding) {}
    fun unbind(binding: Binding) {}
}

fun <Input> bind(observableField: ObservableField<Input>) = ObservableBindingConverter(observableField)

fun <Input> bind(expression: () -> Input) = InputExpressionBindingConverter(expression)

fun <Input> bindSelf(observableField: ObservableField<Input>) = bind(observableField).onSelf()

fun <Input> bindSelf(expression: () -> Input) = bind(expression).onSelf()

class ObservableBindingConverter<Input>(val observableField: ObservableField<Input>)
    : BindingConverter<Input> {

    private var oneWayBinding: Binding? = null

    override fun convertValue() = observableField.value

    override fun bind(binding: Binding) {
        this.oneWayBinding = binding
        observableField.addOnPropertyChangedCallback(this::propertyChanged)
    }

    override fun unbind(binding: Binding) {
        observableField.removeOnPropertyChangedCallback(this::propertyChanged)
        this.oneWayBinding = null
    }

    fun propertyChanged(observable: Observable, kProperty: KProperty<*>?) {
        oneWayBinding?.notifyValueChange()
    }
}

class InputExpressionBindingConverter<Input>(val expression: () -> Input) : BindingConverter<Input> {
    override fun convertValue() = expression()
}
