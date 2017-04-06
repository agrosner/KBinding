package com.andrewgrosner.okbinding.bindings

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import android.widget.DatePicker.OnDateChangedListener
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

    abstract fun getValue(view: V): Output
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

    override fun getValue(view: TextView) = view.text.toString()
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

    override fun getValue(view: CompoundButton) = view.isChecked
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

    override fun getValue(view: DatePicker) = getInstance().apply {
        set(MONTH, view.month)
        set(DAY_OF_MONTH, view.dayOfMonth)
        set(YEAR, view.year)
    }!!

    override fun onDateChanged(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar[MONTH] = monthOfYear
        calendar[DAY_OF_MONTH] = dayOfMonth
        calendar[YEAR] = year
        callback?.invoke(calendar)
    }

}

class OnTimeChangedRegister : ViewRegister<TimePicker, Calendar>(), TimePicker.OnTimeChangedListener {

    override fun register(view: TimePicker) {
        view.setOnTimeChangedListener(this)
    }

    override fun deregister(view: TimePicker) {
        view.setOnTimeChangedListener(null)
    }

    override fun getValue(view: TimePicker) = getInstance().apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            set(HOUR_OF_DAY, view.currentHour)
            set(MINUTE, view.currentMinute)
        } else {
            set(HOUR_OF_DAY, view.hour)
            set(MINUTE, view.minute)
        }
    }!!

    override fun onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) {
        callback?.invoke(getInstance().apply {
            set(HOUR_OF_DAY, hourOfDay)
            set(MINUTE, minute)
        })
    }

}

class OnRatingBarChangedRegister : ViewRegister<RatingBar, Float>(), RatingBar.OnRatingBarChangeListener {

    override fun register(view: RatingBar) {
        view.onRatingBarChangeListener = this
    }

    override fun deregister(view: RatingBar) {
        view.onRatingBarChangeListener = null
    }

    override fun getValue(view: RatingBar) = view.rating

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        callback?.invoke(rating)
    }

}

class OnSeekBarChangedRegister : ViewRegister<SeekBar, Int>(), SeekBar.OnSeekBarChangeListener {

    override fun register(view: SeekBar) {
        view.setOnSeekBarChangeListener(this)
    }

    override fun deregister(view: SeekBar) {
        view.setOnSeekBarChangeListener(null)
    }

    override fun getValue(view: SeekBar) = view.progress

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        callback?.invoke(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

}