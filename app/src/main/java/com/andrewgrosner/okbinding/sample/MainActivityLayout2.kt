package com.andrewgrosner.okbinding.sample

import android.view.View
import com.andrewgrosner.okbinding.BindingComponent
import com.andrewgrosner.okbinding.bindings.bindSelf
import com.andrewgrosner.okbinding.bindings.toOnCheckedChange
import com.andrewgrosner.okbinding.bindings.toText
import com.andrewgrosner.okbinding.bindings.twoWay
import org.jetbrains.anko.*

/**
 * Description:
 */
class MainActivityLayout2(mainActivityViewModel: MainActivityViewModel)
    : BindingComponent<MainActivity, MainActivityViewModel>(mainActivityViewModel) {

    override fun createView(ui: AnkoContext<MainActivity>): View {
        return with(ui) {
            verticalLayout {

                textView {
                    id = R.id.firstName

                    oneWay(MainActivityViewModel::firstName,
                            bindSelf { viewModel.firstName }.toView(this) { view, text ->
                                view.text = text
                                view.visibility = if (text.isNotBlank()) View.VISIBLE else View.GONE
                            })

                    onClick { viewModel.onFirstNameClick() }
                }

                textView {
                    oneWay(MainActivityViewModel::lastName,
                            bindSelf { viewModel.lastName }.toText(this))
                    onClick { viewModel.onLastNameClick() }
                }

                editText {
                    twoWay(MainActivityViewModel::formInput,
                            bindSelf { viewModel.formInput }.toText(this)
                                    .twoWay().toText(this))
                }

                switch {
                    oneWay(bindSelf(viewModel.selected).toOnCheckedChange(this))
                }

                textView {

                }

                textView {

                }
            }
        }
    }

}