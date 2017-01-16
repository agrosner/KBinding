package com.andrewgrosner.okbinding.sample

import android.content.Context
import android.view.View
import com.andrewgrosner.okbinding.bindTo
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
                    viewModel.firstName bindTo { text = it }
                    onClick { viewModel.onFirstNameClick() }
                }

                textView {
                    viewModel.lastName bindTo { text = it }
                    onClick { viewModel.onLastNameClick() }¬
                }

                switch {
                    viewModel.selected bindTo { isChecked = it }
                }
            }
        }
    }
}