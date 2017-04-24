package com.andrewgrosner.okbinding.bindings

import android.view.View
import android.widget.*
import com.andrewgrosner.okbinding.viewextensions.*
import java.util.*

typealias BindingExpression<Input, Output> = (Input) -> Output

interface Binding<Data> {

    fun notifyValueChange()

    fun bind()

    fun unbind()
}

infix fun <Data, Input, Output, TBinding : BindingConverter<Data, Input>>
        TBinding.on(expression: BindingExpression<Input, Output>) = OneWayExpression(this, expression)

/**
 * Builds an expression that returns itself as the Output of the [Input].
 */
fun <Data, Input, TBinding : BindingConverter<Data, Input>> TBinding.onSelf() = OneWayExpression(this, { it })

/**
 * Builds an expression that returns true if the object is null.
 */
fun <Data, Input, TBinding : BindingConverter<Data, Input>> TBinding.onIsNull() = OneWayExpression(this, { it == null })

/**
 * Builds an expression that returns true if the object is not null.
 */
fun <Data, Input, TBinding : BindingConverter<Data, Input>> TBinding.onIsNotNull() = OneWayExpression(this, { it != null })

/**
 * Builds an expression on a [CharSequence] that returns true if [isNullOrEmpty].
 */
fun <Data, TChar : CharSequence?, TBinding : BindingConverter<Data, TChar>> TBinding.onIsNullOrEmpty() = OneWayExpression(this, { it.isNullOrEmpty() })

/**
 * Builds an expression on a [CharSequence] that returns true if not [isNullOrEmpty].
 */
fun <Data, TChar : CharSequence?, TBinding : BindingConverter<Data, TChar>> TBinding.onIsNotNullOrEmpty() = OneWayExpression(this, { !it.isNullOrEmpty() })

class OneWayExpression<Data, Input, Output, Converter : BindingConverter<Data, Input>>
internal constructor(val converter: Converter,
                     val expression: BindingExpression<Input, Output?>) {
    fun <V : View> toView(view: V, viewExpression: (V, Output?) -> Unit)
            = OneWayBinding<Data, Input, Output, Converter, V>(this).toView(view, viewExpression)

}

class OneWayBinding<Data, Input, Output, Converter : BindingConverter<Data, Input>, V : View>
internal constructor(val oneWayExpression: OneWayExpression<Data, Input, Output, Converter>,
                     val converter: Converter = oneWayExpression.converter) : Binding<Data> {

    var viewExpression: ((V, Output?) -> Unit)? = null
    var view: V? = null

    fun convert(): Output? {
        val data = converter.component.viewModel
        return if (data != null) oneWayExpression.expression(converter.convertValue(data)) else null
    }

    @Suppress("UNCHECKED_CAST")
    fun toView(view: V, viewExpression: ((V, Output?) -> Unit)) = apply {
        this.viewExpression = viewExpression
        this.view = view
        converter.component.registerBinding(this)
    }

    override fun bind() {
        notifyValueChange()
        converter.bind(this)
    }

    override fun unbind() {
        unbindInternal()
        converter.component.unregisterBinding(this)
    }

    internal fun unbindInternal() {
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
infix fun <Input, TBinding : BindingConverter<*, Input>>
        OneWayExpression<*, Input, Int, TBinding>.toViewVisibility(textView: View)
        = toView(textView, View::setVisibilityIfNeeded)

/**
 * Immediately binds the [View] to the value of this binding. Toggles visibility based on [Boolean] returned
 * in previous expressions. If true, [View.VISIBLE] is used, otherwise it's set to [View.GONE]
 */
infix fun <Input, TBinding : BindingConverter<*, Input>>
        OneWayExpression<*, Input, Boolean, TBinding>.toViewVisibilityB(textView: View)
        = toView(textView, { view, value -> view.setVisibilityIfNeeded(if (value != null && value) View.VISIBLE else View.GONE) })

/**
 * Immediately binds the [TextView] to the value of this binding. Subsequent changes are handled by
 * the kind of object it is.
 */
infix fun <Data, Input, TBinding : BindingConverter<Data, Input>, TChar : CharSequence?>
        OneWayExpression<Data, Input, TChar, TBinding>.toText(textView: TextView)
        = toView(textView, TextView::setTextIfNecessary)

/**
 * Binds the output of the initial expression to [CompoundButton.setChecked] method.
 */
infix fun <Data, Input, TBinding : BindingConverter<Data, Input>>
        OneWayExpression<Data, Input, Boolean, TBinding>.toOnCheckedChange(compoundButton: CompoundButton)
        = toView(compoundButton, CompoundButton::setCheckedIfNecessary)

/**
 * Binds the output of the initial expression to [DatePicker.updateDate] method.
 */
infix fun <Data, Input, TBinding : ObservableBindingConverter<Data, Input>>
        OneWayExpression<Data, Input, Calendar, TBinding>.toDatePicker(datePicker: DatePicker)
        = toView(datePicker, DatePicker::setTimeIfNecessary)

/**
 * Binds the output of the initial expression to [RatingBar.setRating] method.
 */
infix fun <Data, Input, TBinding : BindingConverter<Data, Input>>
        OneWayExpression<Data, Input, Float, TBinding>.toRating(ratingBar: RatingBar)
        = toView(ratingBar, RatingBar::setRatingIfNecessary)

/**
 * Binds the output of the initial expression to [ProgressBar.setProgress] method.
 */
infix fun <Data, Input, TBinding : BindingConverter<Data, Input>>
        OneWayExpression<Data, Input, Int, TBinding>.toProgressBar(progressBar: ProgressBar)
        = toView(progressBar, ProgressBar::setProgressIfNecessary)