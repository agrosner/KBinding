package com.andrewgrosner.kbinding.viewextensions

import android.widget.DatePicker
import java.util.*
import java.util.Calendar.*


fun DatePicker.setTimeIfNecessary(date: Calendar?) {
    val newDate = date ?: Calendar.getInstance().apply { timeInMillis = 0 } // epoch
    val year = newDate.get(YEAR)
    val month = newDate.get(MONTH)
    val day = newDate.get(DAY_OF_MONTH)

    if (this.dayOfMonth != day || this.month != month || this.year != year) {
        updateDate(year, month, day)
    }
}