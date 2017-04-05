package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.RatingBar
import android.widget.TextView
import java.util.*

fun <Input, Output, Converter : BindingConverter<Input>, V : View>
        OneWayBinding<Input, Output, Converter, V>.twoWay() = TwoWayBindingExpression(this)

class TwoWayBindingExpression<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val oneWayBinding: OneWayBinding<Input, Output, Converter, V>) {
    fun toInput(
            viewRegister: ViewRegister<V, Output>,
            inverseSetter: (InverseSetter<Output>)) = TwoWayBinding(this, viewRegister, inverseSetter)
}


private typealias InverseSetter<T> = (T?) -> Unit

/**
 * Reverses the binding on a field to [View] and provides also [View] to Field support.
 */
class TwoWayBinding<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val twoWayBindingExpression: TwoWayBindingExpression<Input, Output, Converter, V>,
        val viewRegister: ViewRegister<V, Output>,
        inverseSetter: InverseSetter<Output>,
        val oneWayBinding: OneWayBinding<Input, Output, Converter, V> = twoWayBindingExpression.oneWayBinding)
    : Binding {

    private val inverseSetters = mutableSetOf<InverseSetter<Output>>()

    init {
        viewRegister.register(oneWayBinding.view!!, { notifyViewChanged(it) })
        inverseSetters += inverseSetter
    }

    /**
     * Appends another expression that gets called with the value of the view whenever the view itself changes.
     */
    fun onExpression(inverseSetter: InverseSetter<Output>) = apply {
        inverseSetters += inverseSetter
    }

    override fun unbind() {
        oneWayBinding.unbind()
        viewRegister.deregister(oneWayBinding.view!!)
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
    fun notifyViewChanged(value: Output?) {
        inverseSetters.forEach { it.invoke(value) }
    }
}

/**
 * Immediately binds changes from this [TextView] to the specified observable field in a two way binding.
 * Changes from either the view or the field are synchronized between each instance.
 */
fun TwoWayBindingExpression<String, String,
        ObservableBindingConverter<String>, TextView>.toFieldFromText(
        inverseSetter: InverseSetter<String?> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnTextChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [CompoundButton] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 * The [inverseSetter] returns values from the bound view and allows you to mutate values.
 */
fun TwoWayBindingExpression<Boolean, Boolean, ObservableBindingConverter<Boolean>, CompoundButton>.toFieldFromCompound(
        inverseSetter: InverseSetter<Boolean> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnCheckedChangeRegister(), inverseSetter)


/**
 * Immediately binds changes from this [DatePicker] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 * The [inverseSetter] returns values from the bound view and allows you to mutate values.
 */
fun TwoWayBindingExpression<Calendar, Calendar, ObservableBindingConverter<Calendar>, DatePicker>.toFieldFromDate(
        inverseSetter: InverseSetter<Calendar> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnDateChangedRegister(oneWayBinding.convert()), inverseSetter)

/**
 * Immediately binds changes from this [RatingBar] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 * The [inverseSetter] returns values from the bound view and allows you to mutate values.
 */
fun TwoWayBindingExpression<Float, Float, ObservableBindingConverter<Float>, RatingBar>.toFieldFromRating(
        inverseSetter: InverseSetter<Float> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnRatingBarChangedRegister(), inverseSetter)