package com.andrewgrosner.okbinding.sample

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

    override fun createViewWithBindings(ui: AnkoContext<MainActivity>) = with(ui) {
        scrollView {
            verticalLayout {
                linearLayout {
                    textView {
                        id = R.id.firstName
                        textSize = 16f
                        bindSelf(MainActivityViewModel::firstName, { it.firstName }).toText(this)
                        bind(MainActivityViewModel::firstName, { it.firstName }).onIsNotNullOrEmpty()
                                .toViewVisibilityB(this)

                        onClick { viewModel.onFirstNameClick() }
                    }.lparams {
                        rightMargin = dip(4)
                    }

                    textView {
                        textSize = 16f
                        bindSelf(MainActivityViewModel::lastName, { it.lastName }).toText(this)
                        onClick { viewModel.onLastNameClick() }
                    }.lparams {
                        leftMargin = dip(4)
                    }
                }.lparams {
                    width = MATCH_PARENT
                }

                editText {
                    hint = "Text mirrors below (Two way)"
                    bindSelf(viewModel.formInput).toText(this)
                            .twoWay().toFieldFromText()
                }.lparams {
                    width = MATCH_PARENT
                }

                textView {
                    bindSelf(viewModel.formInput).toText(this)
                }

                editText {
                    hint = "Text mirrors below (One way to source)"
                    bind(this).onSelf()
                            .to(viewModel.oneWaySourceInput)
                }

                textView {
                    bindSelf(viewModel.oneWaySourceInput).toText(this)
                }

                switch {
                    bindSelf(viewModel.selected).toOnCheckedChange(this)
                            .twoWay().toFieldFromCompound().onExpression {
                        text = if (it ?: false) "On" else "Off"
                    }
                }

                textView {
                    bindSelf(viewModel.currentTime).toView(this) { _, value ->
                        text = "Current Date is ${value.get(MONTH)}/${value.get(DAY_OF_MONTH)}/${value.get(YEAR)}"
                    }
                }

                datePicker {
                    bindSelf(viewModel.currentTime).toDatePicker(this)
                            .twoWay().toFieldFromDate()
                }
            }.lparams {
                horizontalMargin = dip(8)
            }
        }
    }

}

