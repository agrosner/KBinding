package com.andrewgrosner.okbinding.bindings

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.RatingBar
import android.widget.TextView
import java.lang.ref.WeakReference
import java.util.*
import java.util.Calendar.*

private typealias Callback<T> = (T) -> Unit

abstract class ViewRegister<in V : View, Output> {

    var callback: (((Output?) -> Unit))? = null

    fun register(view: V, callback: Callback<Output?>) {
        this.callback = callback
        register(view)
    }

    abstract fun register(view: V)

    abstract fun deregister(view: V)
}

class OnTextChangedRegister : ViewRegister<TextView, String>(), TextWatcher {

    override fun register(view: TextView) {
        view.addTextChangedListener(this)
    }

    override fun deregister(view: TextView) {
        this.callback = null
        view.removeTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        @Suppress("UNCHECKED_CAST")
        callback?.invoke(s?.toString())
    }
}

class OnCheckedChangeRegister : ViewRegister<CompoundButton, Boolean>(), CompoundButton.OnCheckedChangeListener {

    override fun register(view: CompoundButton) {
        view.setOnCheckedChangeListener(this)
    }

    override fun deregister(view: CompoundButton) {
        view.setOnCheckedChangeListener(null)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        callback?.invoke(isChecked)
    }

}

class OnDateChangedRegister(private val initialValue: Calendar) : ViewRegister<DatePicker, Calendar>(),
        OnDateChangedListener {

    private class WeakOnDateChangedListener(self: OnDateChangedListener) : OnDateChangedListener {

        private val listener = WeakReference(self)

        override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            listener.get()?.onDateChanged(view, year, monthOfYear, dayOfMonth)
        }
    }

    private val listener = WeakOnDateChangedListener(this)

    override fun register(view: DatePicker) {
        view.init(initialValue[YEAR], initialValue[MONTH], initialValue[DAY_OF_MONTH], listener)
    }

    override fun deregister(view: DatePicker) {
        this.callback = null
    }

    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar[MONTH] = monthOfYear
        calendar[DAY_OF_MONTH] = dayOfMonth
        calendar[YEAR] = year
        callback?.invoke(calendar)
    }

}

class OnRatingBarChangedRegister : ViewRegister<RatingBar, Float>(), RatingBar.OnRatingBarChangeListener {

    override fun register(view: RatingBar) {
        view.onRatingBarChangeListener = this
    }

    override fun deregister(view: RatingBar) {
        view.onRatingBarChangeListener = null
    }

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        callback?.invoke(rating)
    }

}