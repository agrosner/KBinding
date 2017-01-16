package com.andrewgrosner.okbinding

import org.jetbrains.anko.AnkoComponent

/**
 * Description:
 */
abstract class ViewModelComponent<T, V>(viewModel: V) : AnkoComponent<T> {

    private var _viewModel: V = viewModel

    var viewModel: V
        get() = _viewModel
        set(value) {
            if (_viewModel != value) {
                _viewModel = value
            }
        }

}