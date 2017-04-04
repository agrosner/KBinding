package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView

typealias BindingExpression<Input, Output> = (Input) -> Output

infix fun <Input, Output, TBinding : Binding<Input>> TBinding.on(expression: BindingExpression<Input, Output>)
        = BindingOn(this, expression)

fun <Input, TBinding : Binding<Input>> TBinding.onSelf() = BindingOn(this, { it })

class BindingOn<Input, Output, TBinding : Binding<Input>>(val binding: TBinding,
                                                          val expression: BindingExpression<Input, Output>) {

    var viewExpression: ((View, Output) -> Unit)? = null
    var view: View? = null

    fun convert() = expression(binding.convertValue())

    @Suppress("UNCHECKED_CAST")
    fun <V : View> toView(view: V, viewExpression: ((V, Output) -> Unit)) = apply {
        this.viewExpression = viewExpression as ((View, Output) -> Unit)
        this.view = view
        notifyValueChange()
        binding.bind(this)
    }

    fun unbind() {
        binding.unbind(this)
    }

    /**
     * Reruns binding expressions to views.
     */
    fun notifyValueChange() {
        viewExpression?.let {
            val view = this.view
            if (view != null) {
                it(view, convert())
            }
        }
    }

}


/**
 * Immediately binds the [TextView] to the value of this binding. Subsequent changes are handled by
 * the kind of object it is.
 */
infix fun <Input, TBinding : Binding<Input>>
        BindingOn<Input, String, TBinding>.toText(textView: TextView) = apply {
    toView(textView, { view, output -> view.text = output })
}

infix fun <Input, TBinding : Binding<Input>>
        BindingOn<Input, Boolean, TBinding>.toOnCheckedChange(compoundButton: CompoundButton) = apply {
    toView(compoundButton, { view, output ->
        view.isChecked = output
    })
}

