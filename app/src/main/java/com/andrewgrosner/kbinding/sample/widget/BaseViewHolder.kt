package com.andrewgrosner.kbinding.sample.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.andrewgrosner.kbinding.BindingHolder
import com.andrewgrosner.kbinding.BindingRegister
import com.andrewgrosner.kbinding.anko.BindingComponent
import org.jetbrains.anko.AnkoContext

/**
 * Description: ViewHolder that holds a required binding.
 */
abstract class BaseViewHolder<Data> : RecyclerView.ViewHolder {

    val component: BindingRegister<Data>

    constructor(view: View) : super(view) {
        component = BindingHolder<Data>()
        component.bindAll()
    }

    constructor(parent: ViewGroup, component: BindingComponent<ViewGroup, Data>)
            : super(component.createView(AnkoContext.create(parent.context, parent))) {
        this.component = component
        component.bindAll()
    }

    val context
        get() = itemView.context

    fun bind(data: Data) {
        applyBindings(data, component)
        if (!component.isBound) {
            component.bindAll()
        }
    }

    abstract fun applyBindings(data: Data, holder: BindingRegister<Data>)
}

class AnkoViewHolder<Data>(parent: ViewGroup, component: BindingComponent<ViewGroup, Data>)
    : BaseViewHolder<Data>(parent, component) {

    override fun applyBindings(data: Data, holder: BindingRegister<Data>) {
        holder.viewModel = data
    }
}

class NoBindingViewHolder<Any>(view: View)
    : BaseViewHolder<Any>(view) {
    override fun applyBindings(data: Any, holder: BindingRegister<Any>) = Unit
}
