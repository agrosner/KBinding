package com.andrewgrosner.okbinding.sample

import com.andrewgrosner.okbinding.BaseObservable
import com.andrewgrosner.okbinding.observable

/**
 * Description:
 */
class MainActivityViewModel : BaseObservable() {

    val firstName = "Andrew"

    val lastName = "Grosner"

    val formInput: String by observable("This should change")

    val selected = observable(true)

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