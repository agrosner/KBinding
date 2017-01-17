package com.andrewgrosner.okbinding

import android.text.Spanned
import android.widget.TextView

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
