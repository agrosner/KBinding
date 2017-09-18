package com.grosner.kbinding.rx

import com.andrewgrosner.kbinding.BaseObservable
import com.andrewgrosner.kbinding.ObservableField
import io.reactivex.Observable

class ObservableFieldWrapper<T>(observable: Observable<T>)
    : ObservableField<T?>, BaseObservable() {

    override var value: T? = null
        get() = field
        set(value) {
            field = value
            notifyChange()
        }

    override val defaultValue: T? = null

    private val disposable = observable.subscribe {
        value = it
    }

    override fun unregisterFromBinding() {
        disposable.dispose()
    }
}

/**
 * Description:
 */
fun <T> Observable<T>.toObservableField() = ObservableFieldWrapper(this)