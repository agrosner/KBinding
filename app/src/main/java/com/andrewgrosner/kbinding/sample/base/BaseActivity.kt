package com.andrewgrosner.kbinding.sample.base

import android.support.v7.app.AppCompatActivity
import com.andrewgrosner.kbinding.anko.BindingComponent
import org.jetbrains.anko.AnkoContextImpl

abstract class BaseActivity<V, A : BaseActivity<V, A>> : AppCompatActivity() {

    private var layout: BindingComponent<A, V>? = null

    var viewModel: V? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = newViewModel().apply {
            layout = newComponent(this).apply {
                createView(AnkoContextImpl(this@BaseActivity, this@BaseActivity as A, true))
                notifyChanges()
            }
        }
    }

    abstract fun newViewModel(): V

    abstract fun newComponent(v: V): BindingComponent<A, V>

    override fun onDestroy() {
        super.onDestroy()
        layout?.destroyView()
        layout = null
    }
}