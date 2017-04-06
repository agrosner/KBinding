package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.*
import com.andrewgrosner.okbinding.viewextensions.*
import java.util.*

typealias BindingExpression<Input, Output> = (Input) -> Output

interface Binding {

    fun notifyValueChange()

    fun bind()

    fun unbind()
}

infix fun <Input, Output, TBinding : BindingConverter<Input>>
        TBinding.on(expression: BindingExpression<Input, Output>) = OneWayExpression(this, expression)

/**
 * Builds an expression that returns itself as the Output of the [Input].
 */
fun <Input, TBinding : BindingConverter<Input>> TBinding.onSelf() = OneWayExpression(this, { it })

/**
 * Builds an expression that returns true if the object is null.
 */
fun <Input, TBinding : BindingConverter<Input>> TBinding.onIsNull() = OneWayExpression(this, { it == null })

/**
 * Builds an expression that returns true if the object is not null.
 */
fun <Input, TBinding : BindingConverter<Input>> TBinding.onIsNotNull() = OneWayExpression(this, { it != null })

/**
 * Builds an expression on a [CharSequence] that returns true if [isNullOrEmpty].
 */
fun <TChar : CharSequence?, TBinding : BindingConverter<TChar>> TBinding.onIsNullOrEmpty() = OneWayExpression(this, { it.isNullOrEmpty() })

/**
 * Builds an expression on a [CharSequence] that returns true if not [isNullOrEmpty].
 */
fun <TChar : CharSequence?, TBinding : BindingConverter<TChar>> TBinding.onIsNotNullOrEmpty() = OneWayExpression(this, { !it.isNullOrEmpty() })

class OneWayExpression<Input, Output, Converter : BindingConverter<Input>>(
        val converter: Converter,
        val expression: BindingExpression<Input, Output>) {
    fun <V : View> toView(view: V, viewExpression: (V, Output) -> Unit)
            = OneWayBinding<Input, Output, Converter, V>(this).toView(view, viewExpression)

}

class OneWayBinding<Input, Output, Converter : BindingConverter<Input>, V : View>(
        val oneWayExpression: OneWayExpression<Input, Output, Converter>,
        val converter: Converter = oneWayExpression.converter) : Binding {

    var viewExpression: ((V, Output) -> Unit)? = null
    var view: V? = null

    fun convert() = oneWayExpression.expression(converter.convertValue())

    @Suppress("UNCHECKED_CAST")
    fun toView(view: V, viewExpression: ((V, Output) -> Unit)) = apply {
        this.viewExpression = viewExpression
        this.view = view
    }

    override fun bind() {
        notifyValueChange()
        converter.bind(this)
    }

    override fun unbind() {
        converter.unbind(this)
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
 * Immediately binds the [View] to the value of this binding. Toggles visibility based on [Int] returned
 * in previous expressions. The [Int] aligns with [View] visibility ints.
 */
infix fun <Input, TBinding : BindingConverter<Input>>
        OneWayExpression<Input, Int, TBinding>.toViewVisibility(textView: View)
        = toView(textView, View::setVisibilityIfNeeded)

/**
 * Immediately binds the [View] to the value of this binding. Toggles visibility based on [Boolean] returned
 * in previous expressions. If true, [View.VISIBLE] is used, otherwise it's set to [View.GONE]
 */
infix fun <Input, TBinding : BindingConverter<Input>>
        OneWayExpression<Input, Boolean, TBinding>.toViewVisibilityB(textView: View)
        = toView(textView, { view, value -> view.setVisibilityIfNeeded(if (value) View.VISIBLE else View.GONE) })

/**
 * Immediately binds the [TextView] to the value of this binding. Subsequent changes are handled by
 * the kind of object it is.
 */
infix fun <Input, TBinding : BindingConverter<Input>, TChar : CharSequence?>
        OneWayExpression<Input, TChar, TBinding>.toText(textView: TextView)
        = toView(textView, TextView::setTextIfNecessary)

/**
 * Binds the output of the initial expression to [CompoundButton.setChecked] method.
 */
infix fun <Input, TBinding : BindingConverter<Input>>
        OneWayExpression<Input, Boolean, TBinding>.toOnCheckedChange(compoundButton: CompoundButton)
        = toView(compoundButton, CompoundButton::setCheckedIfNecessary)

/**
 * Binds the output of the initial expression to [DatePicker.updateDate] method.
 */
infix fun <Input, TBinding : ObservableBindingConverter<Input>>
        OneWayExpression<Input, Calendar, TBinding>.toDatePicker(datePicker: DatePicker)
        = toView(datePicker, DatePicker::setTimeIfNecessary)

/**
 * Binds the output of the initial expression to [RatingBar.setRating] method.
 */
infix fun <Input, TBinding : BindingConverter<Input>>
        OneWayExpression<Input, Float, TBinding>.toRating(ratingBar: RatingBar)
        = toView(ratingBar, RatingBar::setRatingIfNecessary)

/**
 * Binds the output of the initial expression to [ProgressBar.setProgress] method.
 */
infix fun <Input, TBinding : BindingConverter<Input>>
        OneWayExpression<Input, Int, TBinding>.toProgressBar(progressBar: ProgressBar)
        = toView(progressBar, ProgressBar::setProgressIfNecessary)