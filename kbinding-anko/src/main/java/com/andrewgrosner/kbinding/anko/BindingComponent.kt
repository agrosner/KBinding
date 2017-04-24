package com.andrewgrosner.kbinding.anko

import android.view.View
import android.widget.*
import com.andrewgrosner.kbinding.BindingHolder
import com.andrewgrosner.kbinding.BindingRegister
import com.andrewgrosner.kbinding.ObservableField
import com.andrewgrosner.kbinding.bindings.ViewRegister
import com.andrewgrosner.kbinding.bindings.bind
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

abstract class BindingComponent<T, V>(viewModel: V, val register: BindingRegister<V> = BindingHolder(viewModel))
    : AnkoComponent<T> {

    var viewModel: V?
        set(value) {
            register.viewModel = value
        }
        get() {
            return register.viewModel
        }

    fun <Input> bind(function: (V) -> ObservableField<Input>) = register.bind(function)

    fun <Input> bind(kProperty: KProperty<*>, expression: (V) -> Input) = register.bind(kProperty, expression)

    fun <Input> bindNullable(kProperty: KProperty<*>? = null, expression: (V?) -> Input) = register.bind(kProperty, expression)

    fun <Input> bindSelf(function: (V) -> ObservableField<Input>) = register.bindSelf(function)

    fun <Input> bindSelf(kProperty: KProperty<*>, expression: (V) -> Input) = register.bindSelf(kProperty, expression)

    fun <Output, VW : View> bind(v: VW, viewRegister: ViewRegister<VW, Output>) = register.bind(v, viewRegister)

    override final fun createView(ui: AnkoContext<T>) = createViewWithBindings(ui).apply { register.bindAll() }

    abstract fun createViewWithBindings(ui: AnkoContext<T>): View

    fun destroyView() = register.unbindAll()
}