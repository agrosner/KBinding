package com.andrewgrosner.okbinding.sample

import android.view.View
import com.andrewgrosner.okbinding.BindingComponent
import com.andrewgrosner.okbinding.bindings.*
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
                            bindSelf(viewModel.formInput).toTextObs(this)
                                    .twoWay().toFieldFromText())
                }

                switch {
                    twoWay(bindSelf(viewModel.selected).toOnCheckedChange(this)
                            .twoWay().toFieldFromCompound())
                }

                textView {
                    id = R.id.mirrorText
                    twoWayBindingFor<String>(MainActivityViewModel::formInput).onExpression { text = it }
                }

                textView {
                    id = R.id.onOff
                    twoWayBindingFor(viewModel.selected).onExpression {
                        val selected = it ?: false
                        text = if (selected) "On" else "Off"
                    }
                }
            }
        }
    }

}