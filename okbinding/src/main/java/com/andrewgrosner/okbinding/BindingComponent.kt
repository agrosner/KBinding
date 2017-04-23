package com.andrewgrosner.okbinding

import android.view.View
import com.andrewgrosner.okbinding.bindings.ViewRegister
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import kotlin.reflect.KProperty

abstract class BindingComponent<T, V>(viewModel: V) : AnkoComponent<T> {

    val bindingHolder: BindingHolder<V> = BindingHolder(viewModel)

    var viewModel: V
        set(value) {
            bindingHolder.viewModel = value
        }
        get() {
            return bindingHolder.viewModel
        }

    fun <Input> bind(observableField: ObservableField<Input>) = bindingHolder.bind(observableField)

    fun <Input> bind(kProperty: KProperty<*>, expression: (V) -> Input) = bindingHolder.bind(kProperty, expression)

    fun <Input> bindSelf(observableField: ObservableField<Input>) = bindingHolder.bindSelf(observableField)

    fun <Input> bindSelf(kProperty: KProperty<*>, expression: (V) -> Input) = bindingHolder.bindSelf(kProperty, expression)

    fun <Output, VW : View> bind(v: VW, viewRegister: ViewRegister<VW, Output>) = bindingHolder.bind(v, viewRegister)

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(kProperty: KProperty<*>) = bindingHolder.twoWayBindingFor<Output>(kProperty)

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(observableField: ObservableField<Output>) = bindingHolder.twoWayBindingFor(observableField)

    override final fun createView(ui: AnkoContext<T>) = createViewWithBindings(ui).apply { bindingHolder.bindAll() }

    abstract fun createViewWithBindings(ui: AnkoContext<T>): View

    fun destroyView() = bindingHolder.unbindAll()
}