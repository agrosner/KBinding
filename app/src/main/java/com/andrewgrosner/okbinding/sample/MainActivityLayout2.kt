package com.andrewgrosner.okbinding.sample

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.andrewgrosner.okbinding.BindingComponent
import com.andrewgrosner.okbinding.bindings.*
import org.jetbrains.anko.*
import java.util.Calendar.*

/**
 * Description:
 */
class MainActivityLayout2(mainActivityViewModel: MainActivityViewModel)
    : BindingComponent<MainActivity, MainActivityViewModel>(mainActivityViewModel) {

    override fun createView(ui: AnkoContext<MainActivity>): View {
        return with(ui) {
            scrollView {
                verticalLayout {

                    linearLayout {
                        textView {
                            id = R.id.firstName
                            textSize = 16f
                            oneWay(MainActivityViewModel::firstName,
                                    bindSelf { viewModel.firstName }.toView(this) { view, text ->
                                        view.text = text
                                        view.visibility = if (text.isNotBlank()) View.VISIBLE else View.GONE
                                    })

                            onClick { viewModel.onFirstNameClick() }
                        }.lparams {
                            leftMargin = dip(8)
                            rightMargin = dip(4)
                        }

                        textView {
                            textSize = 16f
                            oneWay(MainActivityViewModel::lastName,
                                    bindSelf { viewModel.lastName }.toText(this))
                            onClick { viewModel.onLastNameClick() }
                        }.lparams {
                            leftMargin = dip(4)
                            rightMargin = dip(8)
                        }
                    }.lparams {
                        width = MATCH_PARENT
                    }

                    editText {
                        hint = "This should change"
                        twoWay(MainActivityViewModel::formInput,
                                bindSelf(viewModel.formInput).toText(this)
                                        .twoWay().toFieldFromText())
                    }.lparams {
                        width = MATCH_PARENT
                        horizontalMargin = dip(8)
                    }


                    textView {
                        id = R.id.mirrorText
                        twoWayBindingFor<String>(MainActivityViewModel::formInput).onExpression { text = it }
                    }

                    switch {
                        twoWay(bindSelf(viewModel.selected).toOnCheckedChange(this)
                                .twoWay().toFieldFromCompound())
                    }

                    textView {
                        id = R.id.onOff
                        twoWayBindingFor(viewModel.selected).onExpression {
                            val selected = it ?: false
                            text = if (selected) "On" else "Off"
                        }
                    }

                    textView {
                        oneWay(bindSelf(viewModel.currentTime).toView(this) { view, value ->
                            text = "Current Date is ${value.get(MONTH)}/${value.get(DAY_OF_MONTH)}/${value.get(YEAR)}"
                        })
                    }

                    datePicker {
                        twoWay(bind(viewModel.currentTime).onSelf().toDatePicker(this)
                                .twoWay().toFieldFromDate())
                    }
                }
            }
        }
    }

}