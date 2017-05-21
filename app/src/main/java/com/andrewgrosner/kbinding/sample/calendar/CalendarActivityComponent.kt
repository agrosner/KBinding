package com.andrewgrosner.kbinding.sample.calendar

import com.andrewgrosner.kbinding.anko.BindingComponent
import com.andrewgrosner.kbinding.bindings.toDatePicker
import com.andrewgrosner.kbinding.bindings.toFieldFromDate
import com.andrewgrosner.kbinding.bindings.twoWay
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.datePicker
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.util.*

class CalendarActivityComponent(viewModel: CalendarActivityViewModel)
    : BindingComponent<CalendarActivity, CalendarActivityViewModel>(viewModel) {

    override fun createViewWithBindings(ui: AnkoContext<CalendarActivity>) = with(ui) {
        verticalLayout {
            textView {
                bindSelf { it.currentTime }.toView(this) { _, value ->
                    text = "Current Date is ${value?.get(Calendar.MONTH)}/${value?.get(Calendar.DAY_OF_MONTH)}/${value?.get(Calendar.YEAR)}"
                }
            }

            datePicker {
                bindSelf { it.currentTime }.toDatePicker(this)
                        .twoWay().toFieldFromDate()
            }
        }
    }
}