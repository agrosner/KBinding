package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import com.andrewgrosner.okbinding.ObservableField

fun <Input, Output, Converter : BindingConverter<Input>, V : View>
        OneWayBinding<Input, Output, Converter, V>.twoWay() = TwoWayBinding(this)

class TwoWayBinding<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val oneWayBinding: OneWayBinding<Input, Output, Converter, V>) : Binding<Input, Output, Converter> {

    var inverseExpression: ((V) -> Output)? = null
    var inverseSetter: ((Output, Input) -> Unit)? = null

    fun toInput(inverseExpression: (V) -> Output,
                inverseSetter: ((Output, Input) -> Unit)) {
        this.inverseExpression = inverseExpression
        this.inverseSetter = inverseSetter
    }

    override fun unbind() {
        oneWayBinding.unbind()
    }

    /**
     * Reruns binding expressions to views.
     */
    override fun notifyValueChange() {
        oneWayBinding.notifyValueChange()
    }

    /**
     * When view changes, call our binding expression again.
     */
    fun notifyViewChanged() {
        inverseExpression?.let {
            val view = this.oneWayBinding.view
            if (view != null) {
                it(view)
            }
        }
    }
}


/**
 * Immediately binds the [TextView] to the value of this binding. Subsequent changes are handled by
 * the kind of object it is.
 */
fun <TBinding : BindingConverter<ObservableField<CharSequence>>>
        TwoWayBinding<ObservableField<CharSequence>, CharSequence, TBinding>.toField() = apply {
    val view = oneWayBinding.view
    if (view is TextView) {
        view.textChangedListener { notifyViewChanged() }
    }
    toInput<TextView>({ view.text }, { output, input -> input.value = input })
}

infix fun <Input, TBinding : BindingConverter<Input>>
        TwoWayBinding<Input, Boolean, TBinding>.toOnCheckedChange(compoundButton: CompoundButton) = apply {

}

