package com.andrewgrosner.okbinding.sample

import android.content.Context
import android.view.View
import com.andrewgrosner.okbinding.bindTo
import com.andrewgrosner.okbinding.text
import com.andrewgrosner.okbinding.twoWayText
import org.jetbrains.anko.*

/**
 * Description:
 */
class MainActivityLayout(context: Context, val viewModel: MainActivityViewModel)
    : AnkoComponent<MainActivity> {


    override fun createView(ui: AnkoContext<MainActivity>): View {
        return with(ui) {
            verticalLayout {

                textView {
                    viewModel.firstName bindTo { firstName ->
                        text = firstName
                        visibility = if (firstName.isNotBlank()) View.VISIBLE else View.GONE
                    }
                    onClick { viewModel.onFirstNameClick() }
                }

                textView {
                    text(viewModel.lastName)
                    onClick { viewModel.onLastNameClick() }
                }

                editText {
                    twoWayText(viewModel.formInput)
                }

                switch {
                    viewModel.selected bindTo { isChecked = it }
                }
            }
        }
    }
}