package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.*
import com.andrewgrosner.okbinding.ObservableField
import java.util.*

fun <Output, V : View> bind(v: V, viewRegister: ViewRegister<V, Output>) = ViewBinder(v, viewRegister)

fun bind(v: TextView) = ViewBinder(v, OnTextChangedRegister())

fun bind(v: CompoundButton) = ViewBinder(v, OnCheckedChangeRegister())

fun bind(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = ViewBinder(v, OnDateChangedRegister(initialValue))

fun bind(v: RatingBar) = ViewBinder(v, OnRatingBarChangedRegister())

fun bind(v: SeekBar) = ViewBinder(v, OnSeekBarChangedRegister())

class ViewBinder<V : View, Output>(val view: V,
                                   val viewRegister: ViewRegister<V, Output>)

fun <V : View, Output, Input> ViewBinder<V, Output>.on(bindingExpression: BindingExpression<Output?, Input?>)
        = OneWayToSourceExpression(this, bindingExpression)

fun <V : View, Output> ViewBinder<V, Output>.onSelf() = on { it }

class OneWayToSourceExpression<Input, Output, V : View>(
        val viewBinder: ViewBinder<V, Output>,
        val bindingExpression: BindingExpression<Output?, Input?>) {

    fun to(propertySetter: (Input?, V) -> Unit) = OneWayToSource(this, propertySetter)
}

fun <Input, Output, V : View> OneWayToSourceExpression<Input, Output, V>.to(observableField: ObservableField<Input>)
        = to { input, _ -> observableField.value = input ?: observableField.defaultValue }

class OneWayToSource<Input, Output, V : View>(
        val expression: OneWayToSourceExpression<Input, Output, V>,
        val propertySetter: (Input?, V) -> Unit,
        val bindingExpression: BindingExpression<Output?, Input?> = expression.bindingExpression,
        val view: V = expression.viewBinder.view,
        val viewRegister: ViewRegister<V, Output> = expression.viewBinder.viewRegister) : Binding {

    override fun bind() {
        viewRegister.register(view, { propertySetter(bindingExpression(it), view) })
        notifyValueChange()
    }

    override fun unbind() {
        viewRegister.deregister(view)
    }

    override fun notifyValueChange() {
        propertySetter(bindingExpression(viewRegister.getValue(view)), view)
    }
}

