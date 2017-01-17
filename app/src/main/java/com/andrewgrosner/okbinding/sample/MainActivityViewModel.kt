package com.andrewgrosner.okbinding.sample

import com.andrewgrosner.okbinding.BaseObservable
import com.andrewgrosner.okbinding.observable

/**
 * Description:
 */
class MainActivityViewModel : BaseObservable() {

    val firstName = observable("Andrew")

    val lastName = observable("Grosner")

    val formInput: String by observable("")

    val selected = observable(false)

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