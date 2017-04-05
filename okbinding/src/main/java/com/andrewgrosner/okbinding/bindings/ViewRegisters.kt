package com.andrewgrosner.okbinding.bindings

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView

private typealias Callback<T> = (T) -> Unit

interface ViewRegister<in V : View, out Output> {

    fun register(view: V, callback: Callback<Output?>)

    fun deregister(view: V)
}

class TextViewRegister : ViewRegister<TextView, String>, TextWatcher {

    private var callback: ((String?) -> Unit)? = null

    override fun register(view: TextView, callback: Callback<String?>) {
        this.callback = callback
        view.addTextChangedListener(this)
    }

    override fun deregister(view: TextView) {
        this.callback = null
        view.removeTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        @Suppress("UNCHECKED_CAST")
        callback?.invoke(s?.toString())
    }
}

class OnCheckedChangeRegister : ViewRegister<CompoundButton, Boolean>, CompoundButton.OnCheckedChangeListener {

    private var callback: ((Boolean?) -> Unit)? = null

    override fun register(view: CompoundButton, callback: Callback<Boolean?>) {
        view.setOnCheckedChangeListener(this)
        this.callback = callback
    }

    override fun deregister(view: CompoundButton) {
        view.setOnCheckedChangeListener(null)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        callback?.invoke(isChecked)
    }

}