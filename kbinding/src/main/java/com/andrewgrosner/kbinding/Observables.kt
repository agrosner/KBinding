package com.andrewgrosner.kbinding

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


typealias PropertyChangedCallback = (Observable, KProperty<*>?) -> Unit

/**
 * Observable classes provide a way in which data bound UI can be notified of changes.
 * [ObservableList] and [ObservableMap] also provide the ability to notify when
 * changes occur. ObservableField, ObservableParcelable, ObservableBoolean, ObservableByte,
 * ObservableShort, ObservableInt, ObservableLong, ObservableFloat, and ObservableDouble provide
 * a means by which properties may be notified without implementing Observable.
 *
 *
 * An Observable object should notify the [OnPropertyChangedCallback] whenever
 * an observed property of the class changes.
 *
 *
 * The getter for an observable property should be annotated with [Bindable].
 *
 *
 * Convenience class BaseObservable implements this interface and PropertyChangeRegistry
 * can help classes that don't extend BaseObservable to implement the listener registry.
 */
interface Observable {

    /**
     * Adds a callback to listen for changes to the Observable.
     * @param callback The callback to start listening.
     */
    fun addOnPropertyChangedCallback(callback: PropertyChangedCallback)

    /**
     * Removes a callback from those listening for changes.
     * @param callback The callback that should stop listening.
     */
    fun removeOnPropertyChangedCallback(callback: PropertyChangedCallback)
}

open class BaseObservable : Observable {
    @Transient private var mCallbacks: PropertyChangeRegistry? = null

    @Synchronized
    override fun addOnPropertyChangedCallback(callback: PropertyChangedCallback) {
        if (mCallbacks == null) {
            mCallbacks = PropertyChangeRegistry()
        }
        mCallbacks?.add(callback)
    }

    @Synchronized
    override fun removeOnPropertyChangedCallback(callback: PropertyChangedCallback) {
        mCallbacks?.remove(callback)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.

     * @param fieldId The generated BR id for the Bindable field.
     */
    @Synchronized
    fun notifyChange(property: KProperty<*>? = null) = mCallbacks?.notifyChange(this, property)
}

interface ObservableField<T> : Observable {

    var value: T

    val defaultValue: T

    /**
     * Called when unbinding from its own reference. Useful for cleanup.
     */
    fun unregisterFromBinding() {

    }
}


class ObservableFieldImpl<T>(_value: T, private val configureClosure: PropertyChangedCallback? = null)
    : BaseObservable(), ReadWriteProperty<Any?, T>, ObservableField<T> {

    private var configured = false

    override val defaultValue = _value

    override var value = _value
        set(value) {
            checkConfigured()
            field = value
            notifyChange()
        }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        checkConfigured()
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        checkConfigured()
        if (value != this.value) {
            this.value = value
            (thisRef as? BaseObservable)?.notifyChange(property)
        }
    }

    private fun checkConfigured() {
        if (!configured && configureClosure != null) {
            configured = true
            addOnPropertyChangedCallback(configureClosure)
        }
    }
}

/**
 * Creates new instance of the [Observable] field.
 */
fun <T> observable(initialValue: T, change: PropertyChangedCallback? = null)
        = ObservableFieldImpl(initialValue, change)

