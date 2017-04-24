package com.andrewgrosner.kbinding.viewextensions

import android.widget.CompoundButton


fun CompoundButton.setCheckedIfNecessary(checked: Boolean?) {
    if (checked != null && isChecked != checked) {
        isChecked = checked
    }
}