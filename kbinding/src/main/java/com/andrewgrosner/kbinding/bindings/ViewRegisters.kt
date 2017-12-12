package com.andrewgrosner.kbinding.bindings

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Calendar.getInstance

private typealias Callback<T> = (T) -> Unit

abstract class ViewRegister<in V : View, Output> {

    private var callback: (((Output?) -> Unit))? = null

    fun register(view: V, callback: Callback<Output?>) {
        this.callback = callback
        registerView(view)
    }


    fun deregister(view: V) {
        deregisterFromView(view)
        this.callback = null
    }

    abstract fun registerView(view: V)

    open fun deregisterFromView(view: V) {}

    abstract fun getValue(view: V): Output

    protected fun notifyChange(output: Output?) {
        callback?.invoke(output)
    }
}

class OnTextChangedRegister : ViewRegister<TextView, String>(), TextWatcher {

    override fun registerView(view: TextView) {
        view.addTextChangedListener(this)
    }

    override fun deregisterFromView(view: TextView) {
        view.removeTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = notifyChange(s?.toString())

    override fun getValue(view: TextView) = view.text.toString()
}

class OnCheckedChangeRegister : ViewRegister<CompoundButton, Boolean>(), CompoundButton.OnCheckedChangeListener {

    override fun registerView(view: CompoundButton) {
        view.setOnCheckedChangeListener(this)
    }

    override fun deregisterFromView(view: CompoundButton) {
        view.setOnCheckedChangeListener(null)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = notifyChange(isChecked)

    override fun getValue(view: CompoundButton) = view.isChecked
}

class OnDateChangedRegister(initialValue: Calendar? = null) : ViewRegister<DatePicker, Calendar>(),
        OnDateChangedListener {

    private val initialValue = initialValue ?: getInstance().apply { timeInMillis = 0 }

    private class WeakOnDateChangedListener(self: OnDateChangedListener) : OnDateChangedListener {

        private val listener = WeakReference(self)

        override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            listener.get()?.onDateChanged(view, year, monthOfYear, dayOfMonth)
        }
    }

    private val listener = WeakOnDateChangedListener(this)

    override fun registerView(view: DatePicker) {
        view.init(initialValue[YEAR], initialValue[MONTH], initialValue[DAY_OF_MONTH], listener)
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
        notifyChange(calendar)
    }

}

class OnTimeChangedRegister : ViewRegister<TimePicker, Calendar>(), TimePicker.OnTimeChangedListener {

    override fun registerView(view: TimePicker) {
        view.setOnTimeChangedListener(this)
    }

    override fun deregisterFromView(view: TimePicker) {
        view.setOnTimeChangedListener(null)
    }

    @Suppress("DEPRECATION")
    override fun getValue(view: TimePicker) = getInstance().apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            set(HOUR_OF_DAY, view.currentHour)
            set(MINUTE, view.currentMinute)
        } else {
            set(HOUR_OF_DAY, view.hour)
            set(MINUTE, view.minute)
        }
    }!!

    override fun onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) = notifyChange(getInstance().apply {
        set(HOUR_OF_DAY, hourOfDay)
        set(MINUTE, minute)
    })

}

class OnRatingBarChangedRegister : ViewRegister<RatingBar, Float>(), RatingBar.OnRatingBarChangeListener {

    override fun registerView(view: RatingBar) {
        view.onRatingBarChangeListener = this
    }

    override fun deregisterFromView(view: RatingBar) {
        view.onRatingBarChangeListener = null
    }

    override fun getValue(view: RatingBar) = view.rating

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) = notifyChange(rating)

}

class OnSeekBarChangedRegister : ViewRegister<SeekBar, Int>(), SeekBar.OnSeekBarChangeListener {

    override fun registerView(view: SeekBar) {
        view.setOnSeekBarChangeListener(this)
    }

    override fun deregisterFromView(view: SeekBar) {
        view.setOnSeekBarChangeListener(null)
    }

    override fun getValue(view: SeekBar) = view.progress

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = notifyChange(progress)

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

}