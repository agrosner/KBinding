package com.andrewgrosner.okbinding

import android.view.View
import com.andrewgrosner.okbinding.bindings.ViewRegister
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import kotlin.reflect.KProperty

abstract class BindingComponent<T, V>(viewModel: V, val bindingHolder: BindingRegister<V> = BindingHolder(viewModel))
    : AnkoComponent<T> {

    var viewModel: V?
        set(value) {
            bindingHolder.viewModel = value
        }
        get() {
            return bindingHolder.viewModel
        }

    fun <Input> bind(function: (V) -> ObservableField<Input>) = bindingHolder.bind(function)

    fun <Input> bind(kProperty: KProperty<*>, expression: (V) -> Input) = bindingHolder.bind(kProperty, expression)

    fun <Input> bindSelf(function: (V) -> ObservableField<Input>) = bindingHolder.bindSelf(function)

    fun <Input> bindSelf(kProperty: KProperty<*>, expression: (V) -> Input) = bindingHolder.bindSelf(kProperty, expression)

    fun <Output, VW : View> bind(v: VW, viewRegister: ViewRegister<VW, Output>) = bindingHolder.bind(v, viewRegister)

    override final fun createView(ui: AnkoContext<T>) = createViewWithBindings(ui).apply { bindingHolder.bindAll() }

    abstract fun createViewWithBindings(ui: AnkoContext<T>): View

    fun destroyView() = bindingHolder.unbindAll()
}