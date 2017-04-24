package com.andrewgrosner.kbinding.anko

import android.view.View
import android.widget.*
import com.andrewgrosner.kbinding.BindingHolder
import com.andrewgrosner.kbinding.BindingRegister
import com.andrewgrosner.kbinding.ObservableField
import com.andrewgrosner.kbinding.bindings.*
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import java.util.*
import kotlin.reflect.KProperty

fun <T, Data> BindingComponent<T, Data>.bind(v: TextView) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: CompoundButton) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = register.bind(v, initialValue)
fun <T, Data> BindingComponent<T, Data>.bind(v: TimePicker) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: RatingBar) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: SeekBar) = register.bind(v)

abstract class BindingComponent<T, V>(viewModel: V? = null, val register: BindingRegister<V> = BindingHolder(viewModel))
    : AnkoComponent<T>, BindingRegister<V> {

    override var viewModel: V?
        set(value) {
            register.viewModel = value
        }
        get() {
            return register.viewModel
        }

    override var isBound: Boolean
        get() = register.isBound
        set(value) {
            register.isBound = value
        }

    override fun <Input> bind(function: (V) -> ObservableField<Input>) = register.bind(function)

    override fun <Input> bind(kProperty: KProperty<*>?, expression: (V) -> Input) = register.bind(kProperty, expression)

    override fun <Input> bindNullable(kProperty: KProperty<*>?, expression: (V?) -> Input) = register.bindNullable(kProperty, expression)

    override fun <Input> bindSelf(function: (V) -> ObservableField<Input>) = register.bindSelf(function)

    override fun <Input> bindSelf(kProperty: KProperty<*>, expression: (V) -> Input) = register.bindSelf(kProperty, expression)

    override fun <Output, VW : View> bind(v: VW, viewRegister: ViewRegister<VW, Output>) = register.bind(v, viewRegister)

    override fun bindAll() = register.bindAll()

    override fun unbindAll() = register.unbindAll()

    override fun registerBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>) = register.registerBinding(oneWayBinding)

    override fun unregisterBinding(oneWayBinding: OneWayBinding<V, *, *, *, *>) = register.unregisterBinding(oneWayBinding)

    override fun registerBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>) = register.registerBinding(twoWayBinding)

    override fun unregisterBinding(twoWayBinding: TwoWayBinding<V, *, *, *, *>) = register.unregisterBinding(twoWayBinding)

    override fun registerBinding(oneWayToSource: OneWayToSource<V, *, *, *>) = register.registerBinding(oneWayToSource)

    override fun unregisterBinding(oneWayToSource: OneWayToSource<V, *, *, *>) = register.unregisterBinding(oneWayToSource)

    override final fun createView(ui: AnkoContext<T>) = createViewWithBindings(ui).apply { register.bindAll() }

    abstract fun createViewWithBindings(ui: AnkoContext<T>): View

    fun destroyView() = register.unbindAll()
}