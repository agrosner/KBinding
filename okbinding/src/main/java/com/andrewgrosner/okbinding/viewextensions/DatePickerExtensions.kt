package com.andrewgrosner.okbinding.viewextensions

import android.widget.DatePicker
import java.util.*
import java.util.Calendar.*


fun DatePicker.setTimeIfNecessary(date: Calendar) {
    val year = date.get(YEAR)
    val month = date.get(MONTH)
    val day = date.get(DAY_OF_MONTH)

    if (this.dayOfMonth != day || this.month != month || this.year != year) {
        updateDate(year, month, day)
    }
}