package com.andrewgrosner.kbinding.sample.calendar

import com.andrewgrosner.kbinding.sample.base.BaseActivity

class CalendarActivity : BaseActivity<CalendarActivityViewModel, CalendarActivity>() {

    override fun newViewModel() = CalendarActivityViewModel()

    override fun newComponent(v: CalendarActivityViewModel) = CalendarActivityComponent(v)
}