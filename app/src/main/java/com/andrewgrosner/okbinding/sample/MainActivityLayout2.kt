package com.andrewgrosner.okbinding.sample

import android.view.View
import com.andrewgrosner.okbinding.ViewModelComponent
import com.andrewgrosner.okbinding.bindings.bind
import com.andrewgrosner.okbinding.bindings.onSelf
import com.andrewgrosner.okbinding.bindings.toOnCheckedChange
import com.andrewgrosner.okbinding.bindings.toText
import org.jetbrains.anko.*

/**
 * Description:
 */
class MainActivityLayout2(mainActivityViewModel: MainActivityViewModel)
    : ViewModelComponent<MainActivity, MainActivityViewModel>(mainActivityViewModel) {

    override fun createView(ui: AnkoContext<MainActivity>): View {
        return with(ui) {
            verticalLayout {

                textView {
                    id = R.id.firstName

                    oneWay(MainActivityViewModel::firstName,
                            bind { viewModel.firstName }.onSelf().toView(this) { view, text ->
                                view.text = text
                                view.visibility = if (text.isNotBlank()) View.VISIBLE else View.GONE
                            })

                    onClick { viewModel.onFirstNameClick() }
                }

                textView {
                    oneWay(MainActivityViewModel::lastName,
                            bind { viewModel.lastName }.onSelf().toText(this))
                    onClick { viewModel.onLastNameClick() }
                }

                editText {
                    oneWay(MainActivityViewModel::formInput,
                            bind { viewModel.formInput }.onSelf().toText(this))
                }

                switch {
                    oneWay(bind(viewModel.selected).onSelf().toOnCheckedChange(this))
                }

                textView {

                }

                textView {

                }
            }
        }
    }

}