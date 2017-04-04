package com.andrewgrosner.okbinding

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
    fun addOnPropertyChangedCallback(callback: (Observable, KProperty<*>?) -> Unit)

    /**
     * Removes a callback from those listening for changes.
     * @param callback The callback that should stop listening.
     */
    fun removeOnPropertyChangedCallback(callback: (Observable, KProperty<*>?) -> Unit)

}

open class BaseObservable : Observable {
    @Transient private var mCallbacks: PropertyChangeRegistry? = null

    @Synchronized
    override fun addOnPropertyChangedCallback(callback: (Observable, KProperty<*>?) -> Unit) {
        if (mCallbacks == null) {
            mCallbacks = PropertyChangeRegistry()
        }
        mCallbacks?.add(callback)
    }

    @Synchronized
    override fun removeOnPropertyChangedCallback(callback: (Observable, KProperty<*>?) -> Unit) {
        mCallbacks?.remove(callback)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.

     * @param fieldId The generated BR id for the Bindable field.
     */
    @Synchronized fun notifyChange(property: KProperty<*>? = null) = mCallbacks?.notifyCallbacks(this, property, null)
}


class ObservableField<T>(private var _value: T) : BaseObservable(), ReadWriteProperty<Any?, T> {

    var value = _value
        set(value) {
            field = value
            notifyChange()
        }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return _value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (value != this.value) {
            this.value = value
            if (thisRef is BaseObservable) thisRef.notifyChange(property)
        }
    }
}

/**
 * Creates new instance of the [Observable] field.
 */
fun <T> observable(initialValue: T) = ObservableField(initialValue)

