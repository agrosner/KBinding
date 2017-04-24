package com.andrewgrosner.okbinding.bindings

import com.andrewgrosner.okbinding.BindingRegister
import com.andrewgrosner.okbinding.Observable
import com.andrewgrosner.okbinding.ObservableField
import kotlin.reflect.KProperty


interface BindingConverter<Data, out Input> {

    val component: BindingRegister<Data>

    fun convertValue(data: Data): Input

    fun bind(binding: Binding<Data>) {}
    fun unbind(binding: Binding<Data>) {}
}

class ObservableBindingConverter<Data, Input>(val function: (Data) -> ObservableField<Input>,
                                              override val component: BindingRegister<Data>)
    : BindingConverter<Data, Input> {

    private var oneWayBinding: Binding<Data>? = null

    val observableField: ObservableField<Input>?
        get() {
            val viewModel = component.viewModel
            return if (viewModel != null) function(viewModel) else null
        }

    override fun convertValue(data: Data) = observableField!!.value

    override fun bind(binding: Binding<Data>) {
        this.oneWayBinding = binding
        observableField?.addOnPropertyChangedCallback(this::propertyChanged)
    }

    override fun unbind(binding: Binding<Data>) {
        observableField?.removeOnPropertyChangedCallback(this::propertyChanged)
        this.oneWayBinding = null
    }

    fun propertyChanged(observable: Observable, kProperty: KProperty<*>?) {
        oneWayBinding?.notifyValueChange()
    }
}

class InputExpressionBindingConverter<Data, Input>(val property: KProperty<*>,
                                                   val expression: (Data) -> Input,
                                                   override val component: BindingRegister<Data>) : BindingConverter<Data, Input> {
    override fun convertValue(data: Data) = expression(data)
}
