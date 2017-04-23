package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.*
import com.andrewgrosner.okbinding.BindingComponent
import com.andrewgrosner.okbinding.BindingHolder
import com.andrewgrosner.okbinding.ObservableField
import java.util.*


fun <Data> BindingHolder<Data>.bind(v: TextView) = bind(v, OnTextChangedRegister())
fun <Data> BindingHolder<Data>.bind(v: CompoundButton) = bind(v, OnCheckedChangeRegister())
fun <Data> BindingHolder<Data>.bind(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = bind(v, OnDateChangedRegister(initialValue))
fun <Data> BindingHolder<Data>.bind(v: TimePicker) = bind(v, OnTimeChangedRegister())
fun <Data> BindingHolder<Data>.bind(v: RatingBar) = bind(v, OnRatingBarChangedRegister())
fun <Data> BindingHolder<Data>.bind(v: SeekBar) = bind(v, OnSeekBarChangedRegister())

fun <T, Data> BindingComponent<T, Data>.bind(v: TextView) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: CompoundButton) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = bindingHolder.bind(v, initialValue)
fun <T, Data> BindingComponent<T, Data>.bind(v: TimePicker) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: RatingBar) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: SeekBar) = bindingHolder.bind(v)

class ViewBinder<Data, V : View, Output>(val view: V,
                                         val viewRegister: ViewRegister<V, Output>,
                                         val component: BindingHolder<Data>)

fun <Data, V : View, Output, Input> ViewBinder<Data, V, Output>.on(bindingExpression: BindingExpression<Output?, Input?>)
        = OneWayToSourceExpression(this, bindingExpression)

fun <Data, V : View, Output> ViewBinder<Data, V, Output>.onSelf() = on { it }

class OneWayToSourceExpression<Data, Input, Output, V : View>
internal constructor(val viewBinder: ViewBinder<Data, V, Output>,
                     val bindingExpression: BindingExpression<Output?, Input?>) {

    fun to(propertySetter: (Input?, V) -> Unit) = OneWayToSource(this, propertySetter)
}

fun <Data, Input, Output, V : View> OneWayToSourceExpression<Data, Input, Output, V>.to(observableField: ObservableField<Input>)
        = to { input, _ -> observableField.value = input ?: observableField.defaultValue }

class OneWayToSource<Data, Input, Output, V : View>
internal constructor(
        val expression: OneWayToSourceExpression<Data, Input, Output, V>,
        val propertySetter: (Input?, V) -> Unit,
        val bindingExpression: BindingExpression<Output?, Input?> = expression.bindingExpression,
        val view: V = expression.viewBinder.view,
        val viewRegister: ViewRegister<V, Output> = expression.viewBinder.viewRegister) : Binding<Unit> {

    init {
        expression.viewBinder.component.registerBinding(this)
    }

    override fun bind(data: Unit) {
        viewRegister.register(view, { propertySetter(bindingExpression(it), view) })
        notifyValueChange()
    }

    override fun unbind() {
        unbindInternal()
        expression.viewBinder.component.unregisterBinding(this)
    }

    internal fun unbindInternal() {
        viewRegister.deregister(view)
    }

    override fun notifyValueChange() {
        propertySetter(bindingExpression(viewRegister.getValue(view)), view)
    }
}

