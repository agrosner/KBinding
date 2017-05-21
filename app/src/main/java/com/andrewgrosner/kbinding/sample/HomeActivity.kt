package com.andrewgrosner.kbinding.sample

import android.os.Bundle
import com.andrewgrosner.kbinding.sample.base.BaseActivity
import com.andrewgrosner.kbinding.sample.calendar.CalendarActivity
import com.andrewgrosner.kbinding.sample.input.InputActivity
import org.jetbrains.anko.startActivity

/**
 * Description:
 */
class HomeActivity : BaseActivity<HomeActivityViewModel, HomeActivity>() {

    override fun newViewModel() = HomeActivityViewModel()

    override fun newComponent(v: HomeActivityViewModel) = HomeActivityLayout(v)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel?.onItemClicked = { _, (name) ->
            when (name) {
                "Input Mirroring" -> startActivity<InputActivity>()
                "Calendar" -> startActivity<CalendarActivity>()
            }
        }
    }

}