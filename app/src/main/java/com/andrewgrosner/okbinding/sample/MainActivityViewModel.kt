package com.andrewgrosner.okbinding.sample

import com.andrewgrosner.okbinding.BaseObservable
import com.andrewgrosner.okbinding.observable
import java.util.*

/**
 * Description:
 */
class MainActivityViewModel : BaseObservable() {

    val firstName = "Andrew"

    val lastName = "Grosner"

    val formInput = observable("")

    val selected = observable(true)

    val currentTime = observable(Calendar.getInstance())

    var normalField = ""
        set(value) {
            field = value
            notifyChange(MainActivityViewModel::normalField)
        }

    fun onFirstNameClick() {

    }

    fun onLastNameClick() {

    }
}