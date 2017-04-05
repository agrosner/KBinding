package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import com.andrewgrosner.okbinding.viewextensions.setCheckedIfNecessary
import com.andrewgrosner.okbinding.viewextensions.setTextIfNecessary

typealias BindingExpression<Input, Output> = (Input) -> Output

interface Binding<Input, Output, Converter : BindingConverter<Input>> {

    fun notifyValueChange()

    fun unbind()
}

infix fun <Input, Output, TBinding : BindingConverter<Input>> TBinding.on(expression: BindingExpression<Input, Output>)
        = OneWayExpression(this, expression)

fun <Input, TBinding : BindingConverter<Input>> TBinding.onSelf() = OneWayExpression(this, { it })

fun <Input, Output, Converter : BindingConverter<Input>, V : View>
        OneWayExpression<Input, Output, Converter>.toView(view: V, viewExpression: (V, Output) -> Unit)
        = OneWayBinding<Input, Output, Converter, V>(this).toView(view, viewExpression)

class OneWayExpression<Input, out Output, out Converter : BindingConverter<Input>>(
        val binding: Converter,
        val expression: BindingExpression<Input, Output>)

class OneWayBinding<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val oneWayExpression: OneWayExpression<Input, Output, Converter>,
        val binding: Converter = oneWayExpression.binding) : Binding<Input, Output, Converter> {

    var viewExpression: ((V, Output) -> Unit)? = null
    var view: V? = null

    fun convert() = oneWayExpression.expression(binding.convertValue())

    @Suppress("UNCHECKED_CAST")
    fun toView(view: V, viewExpression: ((V, Output) -> Unit)) = apply {
        this.viewExpression = viewExpression
        this.view = view
        notifyValueChange()
        binding.bind(this)
    }

    override fun unbind() {
        binding.unbind(this)
    }

    /**
     * Reruns binding expressions to views.
     */
    override fun notifyValueChange() {
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
infix fun <Input, TBinding : BindingConverter<Input>, TChar : CharSequence, TV : TextView>
        OneWayExpression<Input, TChar, TBinding>.toText(textView: TextView) = apply {
    toView(textView, TextView::setTextIfNecessary)
}

infix fun <Input, TBinding : BindingConverter<Input>>
        OneWayExpression<Input, Boolean, TBinding>.toOnCheckedChange(compoundButton: CompoundButton) = apply {
    toView(compoundButton, CompoundButton::setCheckedIfNecessary)
}

