package com.andrewgrosner.kbinding.sample.widget

import android.view.ViewGroup

class AutoBindAdapter<Data>(private val createExp: (ViewGroup, Int) -> BaseViewHolder<Data>)
    : BaseRecyclerViewAdapter<Data, BaseViewHolder<Data>>() {
    override fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int) = createExp(parent, viewType)


    override fun onBindViewHolder(holder: BaseViewHolder<Data>,
                                  item: Data, position: Int) {
        holder.bind(item)
    }
}