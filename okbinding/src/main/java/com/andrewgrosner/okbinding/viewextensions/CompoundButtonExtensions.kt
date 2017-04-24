package com.andrewgrosner.okbinding.viewextensions

import android.widget.CompoundButton


fun CompoundButton.setCheckedIfNecessary(checked: Boolean?) {
    if (checked != null && isChecked != checked) {
        isChecked = checked
    }
}