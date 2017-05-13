package com.andrewgrosner.kbinding.sample.calendar

import com.andrewgrosner.kbinding.observable
import java.util.*

class CalendarActivityViewModel  {

    val currentTime = observable(Calendar.getInstance())

}