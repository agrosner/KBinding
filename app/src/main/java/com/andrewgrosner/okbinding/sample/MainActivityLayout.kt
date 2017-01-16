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

    val mutableField = ObservableField("")

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
                    text(viewModel.lastName,
                            defaultValue = "Defaulted",
                            ifNull = { "Not Null" })
                    onClick { viewModel.onLastNameClick() }
                }

                editText {
                    twoWayText(viewModel.formInput)
                }

                switch {
                    twoWayChecked(viewModel.selected)
                }

                textView {
                    text(mutableField)
                }
            }
        }
    }
}