package com.andrewgrosner.okbinding

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.widget.CompoundButton
import android.widget.TextView
import org.jetbrains.anko.onCheckedChange
import kotlin.reflect.KProperty

infix fun <T> ObservableField<T>.toUpdates(function: (T) -> Unit): BoundObservableField {
    val callback: (Observable, Int) -> Unit = {
        observable, i ->
        @Suppress("UNCHECKED_CAST")
        function((observable as ObservableField<T>).value)
    }
    addOnPropertyChangedCallback(callback)
    return BoundObservableField(this, callback)
}

infix fun ObservableBoolean.toUpdates(function: (Boolean) -> Unit): BoundObservableField {
    val callback: (Observable, Int) -> Unit = {
        observable, i ->
        @Suppress("UNCHECKED_CAST")
        function((observable as ObservableBoolean).value)
    }
    addOnPropertyChangedCallback(callback)
    return BoundObservableField(this, callback)
}

infix fun ObservableByte.toUpdates(function: (Byte) -> Unit) {
    addOnPropertyChangedCallback {
        observable, i ->
        @Suppress("UNCHECKED_CAST")
        function((observable as ObservableByte).value)
    }
}

/**
 * Set text on a [TextView]. Executes a ifNull if specified if the value is found to be null.
 * Set the initial value to be [defaultValue] if specified (not null).
 */
inline fun <reified T : CharSequence?> TextView.text(observableField: ObservableField<T>,
                                                     defaultValue: T? = null,
                                                     noinline ifNull: (() -> T)? = null): BoundObservableField {
    if (defaultValue != null) {
        setText(this@text, defaultValue)
    }
    return observableField toUpdates {
        setText(this@text, if (ifNull != null && it == null) ifNull() else it)
    }
}


/**
 * Set text on a [TextView]. Executes a ifNull if specified if the value is found to be null.
 * Set the initial value to be [defaultValue] if specified (not null).
 */
inline fun <reified T : CharSequence?> TextView.text(field: KProperty<T>,
                                                     noinline ifNull: (() -> T)? = null): BoundField<T, TextView> {
    val value = field.call()
    if (value != null) {
        setText(this@text, value)
    }
    return BoundField(this, field, { view, t -> view.text = t })
}

/**
 * Set text on a [TextView] via two-way binding. When the text changes on screen, the [observableField] will also update its value.
 * Executes a ifNull if specified if the value is found to be null.
 * Set the initial value to be [defaultValue] if specified (not null).
 */
inline fun <reified T : CharSequence> TextView.twoWayText(observableField: ObservableField<T>,
                                                          defaultValue: T? = null,
                                                          noinline ifNull: (() -> T)? = null): BoundObservableField {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (haveContentsChanged(observableField.value, s)) {
                observableField.value = s as T
            }
        }
    })
    return text(observableField, defaultValue, ifNull)
}

fun CompoundButton.checked(boolean: ObservableBoolean,
                           defaultValue: Boolean? = null): BoundObservableField {
    if (defaultValue != null) {
        isChecked = defaultValue
    }
    return boolean toUpdates { if (isChecked != it) isChecked = it }
}

fun CompoundButton.twoWayChecked(boolean: ObservableBoolean,
                                 defaultValue: Boolean? = null): BoundObservableField {
    onCheckedChange { view, checked ->
        if (boolean.value != checked) boolean.value = checked
    }
    return checked(boolean, defaultValue)
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
