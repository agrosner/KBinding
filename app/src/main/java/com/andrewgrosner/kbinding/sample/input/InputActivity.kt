package com.andrewgrosner.kbinding.sample.input

import com.andrewgrosner.kbinding.sample.base.BaseActivity

class InputActivity : BaseActivity<InputActivityViewModel, InputActivity>() {

    override fun newViewModel() = InputActivityViewModel()

    override fun newComponent(v: InputActivityViewModel) = InputActivityComponent(v)

}
