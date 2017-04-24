package com.andrewgrosner.kbinding.sample

import com.andrewgrosner.kbinding.BaseObservable
import com.andrewgrosner.kbinding.observable
import java.util.*

/**
 * Description:
 */
class MainActivityViewModel : BaseObservable() {

    val firstName = "Andrew"

    val lastName: String by observable("Grosner") { _, property -> notifyChange(property) }

    val formInput = observable("")

    val oneWaySourceInput = observable("")

    val selected = observable(true)

    val currentTime = observable(Calendar.getInstance())

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