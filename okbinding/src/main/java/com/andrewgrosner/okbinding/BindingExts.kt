package com.andrewgrosner.okbinding

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.widget.TextView

infix fun <T> ObservableField<T>.bindTo(function: (T) -> Unit) = addOnPropertyChangedCallback {
    observable, i ->
    @Suppress("UNCHECKED_CAST")
    function((observable as ObservableField<T>).value)
}

infix fun ObservableBoolean.bindTo(function: (Boolean) -> Unit) = addOnPropertyChangedCallback {
    observable, i ->
    @Suppress("UNCHECKED_CAST")
    function((observable as ObservableBoolean).value)
}

infix fun ObservableByte.bindTo(function: (Byte) -> Unit) = addOnPropertyChangedCallback {
    observable, i ->
    @Suppress("UNCHECKED_CAST")
    function((observable as ObservableByte).value)
}

infix inline fun <reified T : CharSequence> TextView.text(observableField: ObservableField<T>) {
    observableField bindTo { setText(this@text, it) }
}

infix inline fun <reified T : CharSequence> TextView.twoWayText(observableField: ObservableField<T>) {
    observableField bindTo { setText(this@twoWayText, it) }
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (haveContentsChanged(observableField.value, s)) {
                observableField.value = s as T
            }
        }
    })
}

fun setText(view: TextView, text: CharSequence?) {
    val oldText = view.text
    if (text === oldText || text == null && oldText.isEmpty()) {
        return
    }
    if (text is Spanned) {
        if (text == oldText) {
            return  // No change in the spans, so don't set anything.
        }
    } else if (!haveContentsChanged(text, oldText)) {
        return  // No content changes, so don't set anything.
    }
    view.text = text
}

fun haveContentsChanged(str1: CharSequence?, str2: CharSequence?): Boolean {
    if (str1 == null != (str2 == null)) {
        return true
    } else if (str1 == null) {
        return false
    }
    val length = str1.length
    if (length != str2?.length) {
        return true
    }
    return (0..length - 1).any { str2 != null && str1[it] != str2[it] }
}
