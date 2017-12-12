package com.andrewgrosner.kbinding.anko

import android.view.View
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.TimePicker
import com.andrewgrosner.kbinding.BindingHolder
import com.andrewgrosner.kbinding.BindingRegister
import com.andrewgrosner.kbinding.bindings.bind
import com.andrewgrosner.kbinding.bindings.onSelf
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import java.util.Calendar

fun <T, Data> BindingComponent<T, Data>.bind(v: TextView) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: CompoundButton) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = register.bind(v, initialValue)
fun <T, Data> BindingComponent<T, Data>.bind(v: TimePicker) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: RatingBar) = register.bind(v)
fun <T, Data> BindingComponent<T, Data>.bind(v: SeekBar) = register.bind(v)

fun <T, Data> BindingComponent<T, Data>.bindSelf(v: TextView) = bind(v).onSelf()
fun <T, Data> BindingComponent<T, Data>.bindSelf(v: CompoundButton) = bind(v).onSelf()
fun <T, Data> BindingComponent<T, Data>.bindSelf(v: DatePicker, initialValue: Calendar = Calendar.getInstance()) = bind(v, initialValue).onSelf()
fun <T, Data> BindingComponent<T, Data>.bindSelf(v: TimePicker) = bind(v).onSelf()
fun <T, Data> BindingComponent<T, Data>.bindSelf(v: RatingBar) = bind(v).onSelf()
fun <T, Data> BindingComponent<T, Data>.bindSelf(v: SeekBar) = bind(v).onSelf()

abstract class BindingComponent<in T, V>(viewModel: V? = null, val register: BindingRegister<V> = BindingHolder(viewModel))
    : AnkoComponent<T>, BindingRegister<V> by register {

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

    override final fun createView(ui: AnkoContext<T>) = createViewWithBindings(ui).apply { register.bindAll() }

    abstract fun createViewWithBindings(ui: AnkoContext<T>): View

    fun destroyView() = register.unbindAll()
}