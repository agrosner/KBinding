package com.andrewgrosner.kbinding.viewextensions

import android.widget.RatingBar

fun RatingBar.setRatingIfNecessary(rating: Float?) {
    if (this.rating != rating) {
        this.rating = rating ?: 0.0f
    }
}