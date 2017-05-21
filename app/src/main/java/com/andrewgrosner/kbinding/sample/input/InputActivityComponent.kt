package com.andrewgrosner.kbinding.sample.input

import android.graphics.Color
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.andrewgrosner.kbinding.anko.BindingComponent
import com.andrewgrosner.kbinding.anko.bind
import com.andrewgrosner.kbinding.bindings.*
import com.andrewgrosner.kbinding.sample.R
import org.jetbrains.anko.*

/**
 * Description:
 */
class InputActivityComponent(viewModel: InputActivityViewModel)
    : BindingComponent<InputActivity, InputActivityViewModel>(viewModel) {

    val nonNonNullViewModel = viewModel

    override fun createViewWithBindings(ui: AnkoContext<InputActivity>) = with(ui) {
        scrollView {
            verticalLayout {
                linearLayout {
                    textView {
                        id = R.id.firstName
                        textSize = 16f
                        bindSelf(InputActivityViewModel::firstName) { it.firstName }.toText(this)
                        bind(InputActivityViewModel::firstName) { it.firstName }
                                .onIsNotNullOrEmpty()
                                .toViewVisibilityB(this)

                        setOnClickListener { viewModel?.onFirstNameClick() }
                    }.lparams {
                        rightMargin = dip(4)
                    }

                    textView {
                        textSize = 16f
                        bindSelf(InputActivityViewModel::lastName) { it.lastName }.toText(this)
                        setOnClickListener { viewModel?.onLastNameClick() }
                    }.lparams {
                        leftMargin = dip(4)
                    }
                }.lparams {
                    width = MATCH_PARENT
                }

                editText {
                    hint = "Text mirrors below (Two way)"
                    bindSelf { it.formInput }.toText(this)
                            .twoWay().toFieldFromText()
                }.lparams {
                    width = MATCH_PARENT
                }

                textView {
                    bindSelf { it.formInput }.toText(this)
                }

                editText {
                    hint = "Text mirrors below (One way to source)"
                    bind(this).onSelf()
                            .toObservable { it.oneWaySourceInput }
                }

                textView {
                    bindSelf { it.oneWaySourceInput }.toText(this)
                }

                switch {
                    bindSelf { it.selected }.toOnCheckedChange(this)
                            .twoWay().toFieldFromCompound().onExpression { _, input ->
                        text = if (input ?: false) "On" else "Off"
                    }
                }

                button {
                    bindNullable { it }.onNullable { if (it == null) "Set Not Null" else "Set Null" }
                            .toText(this)
                    textColor = Color.BLACK
                    setOnClickListener {
                        if (viewModel != null) {
                            viewModel = null
                        } else {
                            viewModel = nonNonNullViewModel
                        }
                    }
                }
            }.lparams {
                horizontalMargin = dip(8)
            }
        }
    }

}

