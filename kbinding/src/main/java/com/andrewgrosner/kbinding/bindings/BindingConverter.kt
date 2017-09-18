package com.andrewgrosner.kbinding.bindings

import com.andrewgrosner.kbinding.BindingRegister
import com.andrewgrosner.kbinding.Observable
import com.andrewgrosner.kbinding.ObservableField
import kotlin.reflect.KProperty


interface BindingConverter<Data, out Input> {

    val component: BindingRegister<Data>

    fun convertValue(data: Data?): Input?

    fun bind(binding: Binding) {}
    fun unbind(binding: Binding) {}
}

class ObservableBindingConverter<Data, Input>(val function: (Data) -> ObservableField<Input>,
                                              override val component: BindingRegister<Data>)
    : BindingConverter<Data, Input> {

    private var oneWayBinding: Binding? = null

    val observableField: ObservableField<Input>?
        get() {
            val viewModel = component.viewModel
            return if (viewModel != null) function(viewModel) else null
        }

    override fun convertValue(data: Data?) = observableField?.value

    override fun bind(binding: Binding) {
        this.oneWayBinding = binding
        observableField?.addOnPropertyChangedCallback(this::onPropertyChanged)
    }

    override fun unbind(binding: Binding) {
        observableField?.let { observableField ->
            observableField.removeOnPropertyChangedCallback(this::onPropertyChanged)
            observableField.unregisterFromBinding()
        }
        this.oneWayBinding = null
    }

    fun onPropertyChanged(observable: Observable, kProperty: KProperty<*>?) {
        oneWayBinding?.notifyValueChange()
    }
}

class InputExpressionBindingConverter<Data, out Input>(val expression: (Data) -> Input,
                                                       val property: KProperty<*>? = null,
                                                       override val component: BindingRegister<Data>) : BindingConverter<Data, Input> {
    override fun convertValue(data: Data?): Input? = if (data != null) expression(data) else null
}

class NullableInputExpressionBindingConverter<Data, out Input>(val expression: (Data?) -> Input,
                                                               val property: KProperty<*>? = null,
                                                               override val component: BindingRegister<Data>) : BindingConverter<Data, Input> {
    override fun convertValue(data: Data?) = expression(data)
}
