package com.andrewgrosner.okbinding.viewextensions

import android.support.v4.content.ContextCompat
import android.view.View

fun View.setVisibilityIfNeeded(visibility: Int) {
    if (this.visibility != visibility) {
        this.visibility = visibility
    }
}

fun View.string(resId: Int, vararg args: Any?) = context.getString(resId, args)!!

fun View.plural(resId: Int, quantity: Int, vararg args: Any?) = context.resources.getQuantityString(resId, quantity, args)!!

fun View.int(resId: Int) = context.resources.getInteger(resId)

fun View.boolean(resId: Int) = context.resources.getBoolean(resId)

fun View.text(resId: Int) = context.getText(resId)!!

fun View.color(colorRes: Int) = ContextCompat.getColor(context, colorRes)

fun View.drawable(drawableRes: Int) = ContextCompat.getDrawable(context, drawableRes)!!