package com.andrewgrosner.kbinding.sample

import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.andrewgrosner.kbinding.anko.BindingComponent
import com.andrewgrosner.kbinding.bindings.toText
import com.andrewgrosner.kbinding.sample.widget.AnkoViewHolder
import com.andrewgrosner.kbinding.sample.widget.AutoBindAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.recyclerview.v7.recyclerView

class HomeActivityViewModel {

    val homeItemsList = arrayListOf(HomeActivityItemViewModel("RecyclerView"),
            HomeActivityItemViewModel("Calendar"),
            HomeActivityItemViewModel("Input Mirroring"))

    var onItemClicked: ((Int, HomeActivityItemViewModel) -> Unit)? = null

}

data class HomeActivityItemViewModel(val name: String)

class HomeActivityItemComponent : BindingComponent<ViewGroup, HomeActivityItemViewModel>() {

    override fun createViewWithBindings(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            textView {
                bindSelf(HomeActivityItemViewModel::name) { it.name }.toText(this)
                padding = dip(12)
                textSize = 16.0f
                textColor = Color.BLACK
            }.lparams {
                width = MATCH_PARENT
                height = WRAP_CONTENT
            }
        }
    }
}

class HomeActivityLayout(viewModel: HomeActivityViewModel)
    : BindingComponent<HomeActivity, HomeActivityViewModel>(viewModel) {
    override fun createViewWithBindings(ui: AnkoContext<HomeActivity>) = with(ui) {
        verticalLayout {
            toolbar {
                title = "KBinding Examples"
            }
            recyclerView {
                val bindAdapter = AutoBindAdapter { viewGroup, _ ->
                    AnkoViewHolder(viewGroup, HomeActivityItemComponent())
                }.apply {
                    onItemClickListener = { i, viewModel, _ ->
                        this@HomeActivityLayout.viewModelSafe.onItemClicked?.invoke(i, viewModel)
                    }
                }

                bindSelf(HomeActivityViewModel::homeItemsList) { it.homeItemsList }
                        .toView(this) { _, value ->
                            bindAdapter.setItemsList(value)
                        }

                layoutManager = LinearLayoutManager(context)
                adapter = bindAdapter

                lparams {
                    width = MATCH_PARENT
                    height = MATCH_PARENT
                }
            }
        }
    }
}