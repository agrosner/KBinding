package com.andrewgrosner.kbinding.sample.input

import com.andrewgrosner.kbinding.observable
import java.util.*

/**
 * Description:
 */
class InputActivityViewModel : com.andrewgrosner.kbinding.BaseObservable() {

    val firstName = "Andrew"

    val lastName: String by observable("Grosner") { _, property -> notifyChange(property) }

    val formInput = observable("")

    val oneWaySourceInput = observable("")

    val selected = observable(true)

    var normalField = ""
        set(value) {
            field = value
            notifyChange(this::normalField)
        }

    fun onFirstNameClick() {

    }

    fun onLastNameClick() {

    }
}