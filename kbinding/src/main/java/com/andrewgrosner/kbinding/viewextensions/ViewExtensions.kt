package com.andrewgrosner.kbinding.viewextensions

import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View

fun View.setVisibilityIfNeeded(visibility: Int?) {
    if (visibility != null && this.visibility != visibility) {
        this.visibility = visibility
    }
}

fun View.string(resId: Int, vararg args: Any?) = context.getString(resId, args)!!

fun View.plural(resId: Int, quantity: Int, vararg args: Any?) = context.resources.getQuantityString(resId, quantity, args)!!

fun View.int(resId: Int) = context.resources.getInteger(resId)

fun View.boolean(resId: Int) = context.resources.getBoolean(resId)

fun View.text(resId: Int) = context.getText(resId)!!

fun View.color(colorRes: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getColor(colorRes)
    } else {
        resources.getColor(colorRes)
    }
}

fun View.drawable(drawableRes: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        context.getDrawable(drawableRes)!!
    } else {
        resources.getDrawable(drawableRes)
    }
}