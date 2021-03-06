package com.andrewgrosner.kbinding.sample.input

import com.andrewgrosner.kbinding.BaseObservable
import com.andrewgrosner.kbinding.observable

/**
 * Description:
 */
class InputActivityViewModel : BaseObservable() {

    val firstName = "Andrew"

    val lastName: String by observable("Grosner")

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