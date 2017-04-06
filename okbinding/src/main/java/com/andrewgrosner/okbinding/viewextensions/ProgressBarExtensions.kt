package com.andrewgrosner.okbinding.viewextensions

import android.widget.ProgressBar

fun ProgressBar.setProgressIfNecessary(progress: Int) {
    if (this.progress != progress) {
        this.progress = progress
    }
}