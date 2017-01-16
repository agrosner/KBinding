package com.andrewgrosner.okbinding.sample

import android.content.Context
import android.view.View
import com.andrewgrosner.okbinding.*
import org.jetbrains.anko.*

/**
 * Description:
 */
class MainActivityLayout(context: Context, mainActivityViewModel: MainActivityViewModel)
    : ViewModelComponent<MainActivity, MainActivityViewModel>(mainActivityViewModel) {

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
                    twoWayChecked(viewModel.selected)
                }
            }
        }
    }
}