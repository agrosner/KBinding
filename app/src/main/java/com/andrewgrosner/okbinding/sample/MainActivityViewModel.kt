package com.andrewgrosner.okbinding.sample

import com.andrewgrosner.okbinding.ObservableBoolean
import com.andrewgrosner.okbinding.ObservableField

/**
 * Description:
 */
class MainActivityViewModel {

    val firstName = ObservableField("Andrew")

    val lastName = ObservableField("Grosner")

    val selected = ObservableBoolean()

    fun onFirstNameClick() {

    }

    fun onLastNameClick() {

    }
}