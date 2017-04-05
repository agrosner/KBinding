package com.andrewgrosner.okbinding.viewextensions

import android.widget.CompoundButton


fun CompoundButton.setCheckedIfNecessary(checked: Boolean) {
    if (isChecked != checked) {
        isChecked = checked
    }
}