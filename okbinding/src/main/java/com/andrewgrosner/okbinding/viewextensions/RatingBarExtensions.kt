package com.andrewgrosner.okbinding.viewextensions

import android.widget.RatingBar

fun RatingBar.setRatingIfNecessary(rating: Float?) {
    if (this.rating != rating) {
        this.rating = rating ?: 0.0f
    }
}