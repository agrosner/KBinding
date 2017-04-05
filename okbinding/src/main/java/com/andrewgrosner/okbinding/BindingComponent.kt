package com.andrewgrosner.okbinding

import android.view.View
import com.andrewgrosner.okbinding.bindings.ObservableBindingConverter
import com.andrewgrosner.okbinding.bindings.OneWayBinding
import com.andrewgrosner.okbinding.bindings.OneWayToSource
import com.andrewgrosner.okbinding.bindings.TwoWayBinding
import org.jetbrains.anko.AnkoComponent
import kotlin.reflect.KProperty

abstract class BindingComponent<T, V>(viewModel: V) : AnkoComponent<T> {

    private val bindingHolder: BindingHolder<V> = BindingHolder(viewModel)

    var viewModel: V
        set(value) {
            bindingHolder.viewModel = value
        }
        get() {
            return bindingHolder.viewModel
        }

    fun <Input, Output> oneWay(oneWayBinding: OneWayBinding<Input, Output, ObservableBindingConverter<Input>, *>) {
        bindingHolder.oneWay(oneWayBinding)
    }

    fun <Input, Output> oneWay(kProperty: KProperty<*>, oneWayBinding: OneWayBinding<Input, Output, *, *>) {
        bindingHolder.oneWay(kProperty, oneWayBinding)
    }

    fun <Input, Output, V : View> oneWayToSource(oneWayBinding: OneWayToSource<Input, Output, V>) {
        bindingHolder.oneWayToSource(oneWayBinding)
    }

    fun <Input, Output> twoWay(twoWayBinding: TwoWayBinding<Input, Output, ObservableBindingConverter<Input>, *>) {
        bindingHolder.twoWay(twoWayBinding)
    }

    fun <Input, Output> twoWay(kProperty: KProperty<*>, oneWayBinding: TwoWayBinding<Input, Output, *, *>) {
        bindingHolder.twoWay(kProperty, oneWayBinding)
    }

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(kProperty: KProperty<*>) = bindingHolder.twoWayBindingFor<Output>(kProperty)

    @Suppress("UNCHECKED_CAST")
    fun <Output> twoWayBindingFor(observableField: ObservableField<Output>) = bindingHolder.twoWayBindingFor(observableField)

    fun destroyView() = bindingHolder.unbind()
}