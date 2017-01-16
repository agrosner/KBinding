package com.andrewgrosner.okbinding

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Description:
 */


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


class ObservableField<T>(var value: T) : BaseObservable(), ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (value != this.value) {
            this.value = value
            notifyChange()
        }
    }
}

/**
 * An observable class that holds a primitive boolean.
 *
 *
 * Observable field classes may be used instead of creating an Observable object:
 * <pre>`public class MyDataObject {
 * public final ObservableBoolean isAdult = new ObservableBoolean();
 * }`</pre>
 * Fields of this type should be declared final because bindings only detect changes in the
 * field's value, not of the field itself.
 *
 *
 * This class is parcelable and serializable but callbacks are ignored when the object is
 * parcelled / serialized. Unless you add custom callbacks, this will not be an issue because
 * data binding framework always re-registers callbacks when the view is bound.
 */
class ObservableBoolean(var value: Boolean = false) : BaseObservable(),
        Parcelable, Serializable, ReadWriteProperty<Any?, Boolean> {

    override fun getValue(thisRef: Any?, property: KProperty<*>) = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        if (value != this.value) {
            this.value = value
            notifyChange()
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeInt(if (value) 1 else 0)

    companion object {
        internal const val serialVersionUID = 1L

        @JvmStatic
        val CREATOR: Parcelable.Creator<ObservableBoolean> = object : Parcelable.Creator<ObservableBoolean> {

            override fun createFromParcel(source: Parcel): ObservableBoolean {
                return ObservableBoolean(source.readInt() == 1)
            }

            override fun newArray(size: Int): Array<ObservableBoolean?> {
                return arrayOfNulls(size)
            }
        }
    }
}


/**
 * An observable class that holds a primitive byte.
 *
 *
 * Observable field classes may be used instead of creating an Observable object:
 * <pre>`public class MyDataObject {
 * public final ObservableByte flags = new ObservableByte();
 * }`</pre>
 * Fields of this type should be declared final because bindings only detect changes in the
 * field's value, not of the field itself.
 *
 *
 * This class is parcelable and serializable but callbacks are ignored when the object is
 * parcelled / serialized. Unless you add custom callbacks, this will not be an issue because
 * data binding framework always re-registers callbacks when the view is bound.
 */
class ObservableByte(var value: Byte) : BaseObservable(), Parcelable, Serializable,
        ReadWriteProperty<Any?, Byte> {

    override fun getValue(thisRef: Any?, property: KProperty<*>) = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Byte) {
        if (value != this.value) {
            this.value = value
            notifyChange()
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeByte(value)

    companion object {
        internal const val serialVersionUID = 1L

        @JvmStatic
        val CREATOR: Parcelable.Creator<ObservableByte> = object : Parcelable.Creator<ObservableByte> {

            override fun createFromParcel(source: Parcel) = ObservableByte(source.readByte())

            override fun newArray(size: Int): Array<ObservableByte?> = arrayOfNulls(size)
        }
    }
}

/**
 * Creates new instance of the [Observable] field.
 */
fun <T> observable(initialValue: T) = ObservableField(initialValue)

fun observable(initialValue: Boolean) = ObservableBoolean(initialValue)

fun observable(initialValue: Byte) = ObservableByte(initialValue)