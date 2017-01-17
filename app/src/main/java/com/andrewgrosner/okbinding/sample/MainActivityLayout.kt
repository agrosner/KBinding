package com.andrewgrosner.okbinding.sample

import android.view.View
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
                    bind(field(MainActivityViewModel::firstName)
                            .obs(viewModel.firstName)
                            .toView { view, firstName ->
                                view.text = firstName
                                view.visibility = if (firstName.isNotBlank()) View.VISIBLE else View.GONE
                            })
                    onClick { viewModel.onFirstNameClick() }
                }

                textView {
                    bind(field(MainActivityViewModel::lastName)
                            .obs(viewModel.lastName).toText())
                    onClick { viewModel.onLastNameClick() }
                }

                editText {
                    bind(field(MainActivityViewModel::formInput)
                            .exp { viewModel.formInput }.toText())
                }

                switch {
                    bind(field(MainActivityViewModel::selected)
                            .obs(viewModel.selected).toCheckedChange())
                }

                textView {

                }

                textView {

                }
            }
        }
    }

}