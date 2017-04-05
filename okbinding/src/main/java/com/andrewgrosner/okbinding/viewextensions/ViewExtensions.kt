package com.andrewgrosner.okbinding.viewextensions

import android.view.View

fun View.setVisibilityIfNeeded(visibility: Int) {
    if (this.visibility != visibility) {
        this.visibility = visibility
    }
}