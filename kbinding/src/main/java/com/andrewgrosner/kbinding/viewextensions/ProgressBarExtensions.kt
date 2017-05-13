package com.andrewgrosner.kbinding.viewextensions

import android.widget.ProgressBar

fun ProgressBar.setProgressIfNecessary(progress: Int?) {
    if (this.progress != progress) {
        this.progress = progress ?: 0
    }
}