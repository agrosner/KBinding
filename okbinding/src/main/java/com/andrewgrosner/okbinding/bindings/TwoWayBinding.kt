package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.*
import com.andrewgrosner.okbinding.BaseObservable
import com.andrewgrosner.okbinding.BindingHolder
import java.util.*

fun <Data, Input, Output, Converter : BindingConverter<Data, Input>, V : View>
        OneWayBinding<Data, Input, Output, Converter, V>.twoWay() = TwoWayBindingExpression(this)

class TwoWayBindingExpression<Data, Input, Output, Converter : BindingConverter<Data, Input>, V : View>(
        val oneWayBinding: OneWayBinding<Data, Input, Output, Converter, V>) {
    fun toInput(viewRegister: ViewRegister<V, Output>, inverseSetter: (InverseSetter<Output>))
            = TwoWayBinding(this, viewRegister, inverseSetter)
}


private typealias InverseSetter<T> = (T?) -> Unit

/**
 * Reverses the binding on a field to [View] and provides also [View] to Field support.
 */
class TwoWayBinding<Data, Input, Output, Converter : BindingConverter<Data, Input>, V : View>(
        val twoWayBindingExpression: TwoWayBindingExpression<Data, Input, Output, Converter, V>,
        val viewRegister: ViewRegister<V, Output>,
        val inverseSetter: InverseSetter<Output>,
        val oneWayBinding: OneWayBinding<Data, Input, Output, Converter, V> = twoWayBindingExpression.oneWayBinding)
    : Binding<Data> {

    private val inverseSetters = mutableSetOf(inverseSetter)

    init {
        val component = oneWayBinding.converter.component

        // unregister previously one way binding to ensure we don't duplicate
        component.unregisterBinding(oneWayBinding)
        component.registerBinding(this)
    }

    /**
     * Appends another expression that gets called with the value of the view whenever the view itself changes.
     */
    fun onExpression(inverseSetter: InverseSetter<Output>) = apply {
        inverseSetters += inverseSetter
    }

    override fun bind(data: Data) {
        oneWayBinding.bind(data)
        viewRegister.register(oneWayBinding.view!!, { notifyViewChanged(it) })
        oneWayBinding.notifyValueChange() // trigger value change on bind to respect value of ViewModel over view.
    }

    override fun unbind() {
        oneWayBinding.unbind()
        viewRegister.deregister(oneWayBinding.view!!)
        oneWayBinding.converter.component.unregisterBinding(this)
    }

    internal fun unbindInternal() {
        oneWayBinding.unbindInternal()
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

    /**
     * Notifies change manually from the current value of the field bound to it.
     */
    fun notifyViewChanged() {
        notifyViewChanged(oneWayBinding.convert())
    }
}

/**
 * Immediately binds changes from this [TextView] to the specified observable field in a two way binding.
 * Changes from either the view or the field are synchronized between each instance.
 *  The [inverseSetter] (optional) receives values from the view. Here you should update the observable property tied to the beginning of the binding.
 */
fun <Data> TwoWayBindingExpression<Data, String, String,
        ObservableBindingConverter<Data, String>, TextView>.toFieldFromText(
        inverseSetter: InverseSetter<String?> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnTextChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [TextView] to the specified expression in a two way binding.
 * The expression should mutate a field that is observed and pass changes back up to the parent [BaseObservable].
 * Changes from either the view or the field are synchronized between each instance.
 *  The [inverseSetter] should mutate a property that is observed by the parent ViewModel registered in the [BindingHolder].
 *  Otherwise the view in this binding will not receive updates and two way binding will not work.
 */
fun <Data> TwoWayBindingExpression<Data, String, String,
        BindingConverter<Data, String>, TextView>.toExprFromText(
        inverseSetter: InverseSetter<String?>)
        = toInput(OnTextChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [CompoundButton] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 *  The [inverseSetter] (optional) receives values from the view. Here you should update the observable property tied to the beginning of the binding.
 */
fun <Data> TwoWayBindingExpression<Data, Boolean, Boolean, ObservableBindingConverter<Data, Boolean>, CompoundButton>.toFieldFromCompound(
        inverseSetter: InverseSetter<Boolean> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnCheckedChangeRegister(), inverseSetter)

/**
 * Immediately binds changes from this [CompoundButton] to the specified expression in a two way binding.
 * The expression should mutate a field that is observed and pass changes back up to the parent [BaseObservable].
 * Changes from either the view or the field are synchronized between each instance.
 *  The [inverseSetter] should mutate a property that is observed by the parent ViewModel registered in the [BindingHolder].
 *  Otherwise the view in this binding will not receive updates and two way binding will not work.
 */
fun <Data> TwoWayBindingExpression<Data, Boolean, Boolean,
        BindingConverter<Data, Boolean>, CompoundButton>.toExprFromCompound(
        inverseSetter: InverseSetter<Boolean>)
        = toInput(OnCheckedChangeRegister(), inverseSetter)


/**
 * Immediately binds changes from this [DatePicker] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 *  The [inverseSetter] (optional) receives values from the view. Here you should update the observable property tied to the beginning of the binding.
 */
fun <Data> TwoWayBindingExpression<Data, Calendar, Calendar, ObservableBindingConverter<Data, Calendar>, DatePicker>.toFieldFromDate(
        inverseSetter: InverseSetter<Calendar> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnDateChangedRegister(oneWayBinding.convert()), inverseSetter)

/**
 * Immediately binds changes from this [DatePicker] to the specified expression in a two way binding.
 * The expression should mutate a field that is observed and pass changes back up to the parent [BaseObservable].
 * Changes from either the view or the field are synchronized between each instance.
 *  The [inverseSetter] should mutate a property that is observed by the parent ViewModel registered in the [BindingHolder].
 *  Otherwise the view in this binding will not receive updates and two way binding will not work.
 */
fun <Data> TwoWayBindingExpression<Data, Calendar, Calendar,
        BindingConverter<Data, Calendar>, DatePicker>.toExprFromDate(
        inverseSetter: InverseSetter<Calendar>)
        = toInput(OnDateChangedRegister(oneWayBinding.convert()), inverseSetter)

/**
 * Immediately binds changes from this [TimePicker] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 *  The [inverseSetter] (optional) receives values from the view. Here you should update the observable property tied to the beginning of the binding.
 */
fun <Data> TwoWayBindingExpression<Data, Calendar, Calendar, ObservableBindingConverter<Data, Calendar>, TimePicker>.toFieldFromTime(
        inverseSetter: InverseSetter<Calendar> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnTimeChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [TimePicker] to the specified expression in a two way binding.
 * The expression should mutate a field that is observed and pass changes back up to the parent [BaseObservable].
 * Changes from either the view or the field are synchronized between each instance.
 *  The [inverseSetter] should mutate a property that is observed by the parent ViewModel registered in the [BindingHolder].
 *  Otherwise the view in this binding will not receive updates and two way binding will not work.
 */
fun <Data> TwoWayBindingExpression<Data, Calendar, Calendar,
        BindingConverter<Data, Calendar>, TimePicker>.toExprFromTime(
        inverseSetter: InverseSetter<Calendar>)
        = toInput(OnTimeChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [RatingBar] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 *  The [inverseSetter] (optional) receives values from the view. Here you should update the observable property tied to the beginning of the binding.
 */
fun <Data> TwoWayBindingExpression<Data, Float, Float, ObservableBindingConverter<Data, Float>, RatingBar>.toFieldFromRating(
        inverseSetter: InverseSetter<Float> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnRatingBarChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [RatingBar] to the specified expression in a two way binding.
 * The expression should mutate a field that is observed and pass changes back up to the parent [BaseObservable].
 * Changes from either the view or the field are synchronized between each instance.
 *  The [inverseSetter] should mutate a property that is observed by the parent ViewModel registered in the [BindingHolder].
 *  Otherwise the view in this binding will not receive updates and two way binding will not work.
 */
fun <Data> TwoWayBindingExpression<Data, Float, Float,
        BindingConverter<Data, Float>, RatingBar>.toExprFromRating(
        inverseSetter: InverseSetter<Float>)
        = toInput(OnRatingBarChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [SeekBar] to the specified observable field in a two way binding.
 * Changes from either the view or the field expression are synchronized between each instance.
 *  The [inverseSetter] (optional) receives values from the view. Here you should update the observable property tied to the beginning of the binding.
 */
fun <Data> TwoWayBindingExpression<Data, Int, Int, ObservableBindingConverter<Data, Int>, SeekBar>.toFieldFromSeekBar(
        inverseSetter: InverseSetter<Int> = {
            val observableField = oneWayBinding.oneWayExpression.converter.observableField
            observableField.value = it ?: observableField.defaultValue
        })
        = toInput(OnSeekBarChangedRegister(), inverseSetter)

/**
 * Immediately binds changes from this [SeekBar] to the specified expression in a two way binding.
 * The expression should mutate a field that is observed and pass changes back up to the parent [BaseObservable].
 * Changes from either the view or the field are synchronized between each instance.
 *  The [inverseSetter] should mutate a property that is observed by the parent ViewModel registered in the [BindingHolder].
 *  Otherwise the view in this binding will not receive updates and two way binding will not work.
 */
fun <Data> TwoWayBindingExpression<Data, Int, Int,
        BindingConverter<Data, Int>, SeekBar>.toExprFromSeekBar(
        inverseSetter: InverseSetter<Int>)
        = toInput(OnSeekBarChangedRegister(), inverseSetter)