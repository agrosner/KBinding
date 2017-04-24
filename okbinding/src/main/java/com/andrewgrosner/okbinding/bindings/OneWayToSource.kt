package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.*
import com.andrewgrosner.okbinding.BindingComponent
import com.andrewgrosner.okbinding.BindingRegister
import com.andrewgrosner.okbinding.ObservableField
import java.util.*


fun <Data> BindingRegister<Data>.bind(v: TextView) = bind(v, OnTextChangedRegister())
fun <Data> BindingRegister<Data>.bind(v: CompoundButton) = bind(v, OnCheckedChangeRegister())
fun <Data> BindingRegister<Data>.bind(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = bind(v, OnDateChangedRegister(initialValue))
fun <Data> BindingRegister<Data>.bind(v: TimePicker) = bind(v, OnTimeChangedRegister())
fun <Data> BindingRegister<Data>.bind(v: RatingBar) = bind(v, OnRatingBarChangedRegister())
fun <Data> BindingRegister<Data>.bind(v: SeekBar) = bind(v, OnSeekBarChangedRegister())

fun <T, Data> BindingComponent<T, Data>.bind(v: TextView) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: CompoundButton) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = bindingHolder.bind(v, initialValue)
fun <T, Data> BindingComponent<T, Data>.bind(v: TimePicker) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: RatingBar) = bindingHolder.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: SeekBar) = bindingHolder.bind(v)

class ViewBinder<Data, V : View, Output>(val view: V,
                                         val viewRegister: ViewRegister<V, Output>,
                                         val component: BindingRegister<Data>)

fun <Data, V : View, Output, Input> ViewBinder<Data, V, Output>.on(bindingExpression: BindingExpression<Output?, Input?>)
        = OneWayToSourceExpression(this, bindingExpression)

fun <Data, V : View, Output> ViewBinder<Data, V, Output>.onSelf() = on { it }

class OneWayToSourceExpression<Data, Input, Output, V : View>
internal constructor(val viewBinder: ViewBinder<Data, V, Output>,
                     val bindingExpression: BindingExpression<Output?, Input?>) {

    fun to(propertySetter: (Input?, V) -> Unit) = OneWayToSource(this, propertySetter)
}

inline fun <Data, Input, Output, V : View>
        OneWayToSourceExpression<Data, Input, Output, V>.toObservable(crossinline function: (Data) -> ObservableField<Input>)
        = to { input, _ -> viewBinder.component.viewModel?.let { viewModel -> function(viewModel).let { it.value = input ?: it.defaultValue } } }

class OneWayToSource<Data, Input, Output, V : View>
internal constructor(
        val expression: OneWayToSourceExpression<Data, Input, Output, V>,
        val propertySetter: (Input?, V) -> Unit,
        val bindingExpression: BindingExpression<Output?, Input?> = expression.bindingExpression,
        val view: V = expression.viewBinder.view,
        val viewRegister: ViewRegister<V, Output> = expression.viewBinder.viewRegister) : Binding<Data> {

    init {
        expression.viewBinder.component.registerBinding(this)
    }

    override fun bind() {
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

