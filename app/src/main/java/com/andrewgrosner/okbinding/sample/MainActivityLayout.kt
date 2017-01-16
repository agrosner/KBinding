package com.andrewgrosner.okbinding.sample

import android.view.View
import android.widget.TextView
import com.andrewgrosner.okbinding.*
import org.jetbrains.anko.*

/**
 * Description:
 */
class MainActivityLayout(mainActivityViewModel: MainActivityViewModel)
    : ViewModelComponent<MainActivity, MainActivityViewModel>(mainActivityViewModel) {

    val mutableField = ObservableField("")


    override fun createView(ui: AnkoContext<MainActivity>): View {
        return with(ui) {
            verticalLayout {

                textView {
                    id = R.id.firstName
                    bind(viewModel.firstName toUpdates { firstName ->
                        text = firstName
                        visibility = if (firstName.isNotBlank()) View.VISIBLE else View.GONE
                    })
                    onClick { viewModel.onFirstNameClick() }
                }

                textView {
                    bind(text(viewModel.lastName,
                            defaultValue = "Defaulted",
                            ifNull = { "Not Null" }))
                    onClick { viewModel.onLastNameClick() }
                }

                editText {
                    bind(twoWayText(viewModel.formInput))
                }

                switch {
                    bind(twoWayChecked(viewModel.selected))
                }

                textView {
                    bind(text(mutableField))
                }

                textView {
                    bind(text(MainActivityViewModel::normalField))
                }
            }
        }
    }

}