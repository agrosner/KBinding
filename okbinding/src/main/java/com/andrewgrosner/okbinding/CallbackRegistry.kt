package com.andrewgrosner.okbinding

import java.util.*
import kotlin.reflect.KProperty

/**
 * A utility for storing and notifying callbacks. This class supports reentrant modification
 * of the callbacks during notification without adversely disrupting notifications.
 * A common pattern for callbacks is to receive a notification and then remove
 * themselves. This class handles this behavior with constant memory under
 * most circumstances.
 *
 * <p>A subclass of {@link CallbackRegistry.NotifierCallback} must be passed to
 * the constructor to define how notifications should be called. That implementation
 * does the actual notification on the listener. It is typically a static instance
 * that can be reused for all similar CallbackRegistries.</p>
 *
 * <p>This class supports only callbacks with at most three parameters.
 * Typically, these are the notification originator and a parameter, with another to
 * indicate which method to call, but these may be used as required. If more than
 * three parameters are required or primitive types other than the single int provided
 * must be used, <code>A</code> should be some kind of containing structure that
 * the subclass may reuse between notifications.</p>
 *
 * @param <C> The callback type.
 * @param <T> The notification sender type. Typically this is the containing class.
 * @param <A> Opaque argument used to pass additional data beyond an int.
 */
open class CallbackRegistry<C, T, A>
/**
 * Creates an EventRegistry that notifies the event with notifier.
 * @param notifier The class to use to notify events.
 */(val notifier: (C, T, KProperty<*>?, A) -> Unit) : Cloneable {

    companion object {

        val TAG = "CallbackRegistry"
    }

    /** An ordered collection of listeners waiting to be notified. */
    private var mCallbacks = arrayListOf<C>()

    /**
     * A bit flag for the first 64 listeners that are removed during notification.
     * The lowest significant bit corresponds to the 0th index into mCallbacks.
     * For a small number of callbacks, no additional array of objects needs to
     * be allocated.
     */
    private var mFirst64Removed: Long = 0x0

    /**
     * Bit flags for the remaining callbacks that are removed during notification.
     * When there are more than 64 callbacks and one is marked for removal, a dynamic
     * array of bits are allocated for the callbacks.
     */
    private var mRemainderRemoved: LongArray? = null

    /** The recursion level of the notification */
    private var mNotificationLevel: Int = 0

    /**
     * Notify all callbacks.

     * @param sender The originator. This is an opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg2 An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     */
    @Synchronized fun notifyCallbacks(sender: T, property: KProperty<*>?, arg2: A) {
        mNotificationLevel++
        notifyRecurse(sender, property, arg2)
        mNotificationLevel--
        if (mNotificationLevel == 0) {
            val remainderRemoved = mRemainderRemoved
            if (remainderRemoved != null) {
                for (i in remainderRemoved.indices.reversed()) {
                    val removedBits = remainderRemoved[i]
                    if (removedBits != 0L) {
                        removeRemovedCallbacks((i + 1) * java.lang.Long.SIZE, removedBits)
                        remainderRemoved[i] = 0
                    }
                }
            }
            if (mFirst64Removed != 0L) {
                removeRemovedCallbacks(0, mFirst64Removed)
                mFirst64Removed = 0
            }
        }
    }

    /**
     * Notify up to the first Long.SIZE callbacks that don't have a bit set in `removed`.

     * @param sender The originator. This is an opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg2 An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     */
    private fun notifyFirst64(sender: T, property: KProperty<*>?, arg2: A) {
        val maxNotified = Math.min(java.lang.Long.SIZE, mCallbacks.size)
        notifyCallbacks(sender, property, arg2, 0, maxNotified, mFirst64Removed)
    }

    /**
     * Notify all callbacks using a recursive algorithm to avoid allocating on the heap.
     * This part captures the callbacks beyond Long.SIZE that have no bits allocated for
     * removal before it recurses into [.notifyRemainder].

     *
     * Recursion is used to avoid allocating temporary state on the heap.

     * @param sender The originator. This is an opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg2 An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     */
    private fun notifyRecurse(sender: T, property: KProperty<*>?, arg2: A) {
        val callbackCount = mCallbacks.size
        val remainderRemoved = mRemainderRemoved
        val remainderIndex = if (remainderRemoved == null) -1 else remainderRemoved.size - 1

        // Now we've got all callbakcs that have no mRemainderRemoved value, so notify the
        // others.
        notifyRemainder(sender, property, arg2, remainderIndex)

        // notifyRemainder notifies all at maxIndex, so we'd normally start at maxIndex + 1
        // However, we must also keep track of those in mFirst64Removed, so we add 2 instead:
        val startCallbackIndex = (remainderIndex + 2) * java.lang.Long.SIZE

        // The remaining have no bit set
        notifyCallbacks(sender, property, arg2, startCallbackIndex, callbackCount, 0)
    }

    /**
     * Notify callbacks that have mRemainderRemoved bits set for remainderIndex. If
     * remainderIndex is -1, the first 64 will be notified instead.

     * @param sender The originator. This is an opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg2 An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param remainderIndex The index into mRemainderRemoved that should be notified.
     */
    private fun notifyRemainder(sender: T, property: KProperty<*>?, arg2: A, remainderIndex: Int) {
        if (remainderIndex < 0) {
            notifyFirst64(sender, property, arg2)
        } else {
            val remainderRemoved = mRemainderRemoved
            if (remainderRemoved != null) {
                val bits = remainderRemoved[remainderIndex]
                val startIndex = (remainderIndex + 1) * java.lang.Long.SIZE
                val endIndex = Math.min(mCallbacks.size, startIndex + java.lang.Long.SIZE)
                notifyRemainder(sender, property, arg2, remainderIndex - 1)
                notifyCallbacks(sender, property, arg2, startIndex, endIndex, bits)
            }
        }
    }

    /**
     * Notify callbacks from startIndex to endIndex, using bits as the bit status
     * for whether they have been removed or not. bits should be from mRemainderRemoved or
     * mFirst64Removed. bits set to 0 indicates that all callbacks from startIndex to
     * endIndex should be notified.

     * @param sender The originator. This is an opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param arg2 An opaque parameter passed to
     * * [CallbackRegistry.NotifierCallback.onNotifyCallback]
     * *
     * @param startIndex The index into the mCallbacks to start notifying.
     * *
     * @param endIndex One past the last index into mCallbacks to notify.
     * *
     * @param bits A bit field indicating which callbacks have been removed and shouldn't
     * *             be notified.
     */
    private fun notifyCallbacks(sender: T, property: KProperty<*>?, arg2: A, startIndex: Int,
                                endIndex: Int, bits: Long) {
        var bitMask: Long = 1
        for (i in startIndex..endIndex - 1) {
            if (bits and bitMask == 0L) {
                notifier(mCallbacks[i], sender, property, arg2)
            }
            bitMask = bitMask shl 1
        }
    }

    /**
     * Add a callback to be notified. If the callback is already in the list, another won't
     * be added. This does not affect current notifications.
     * @param callback The callback to add.
     */
    @Synchronized fun add(callback: C?) {
        if (callback == null) {
            throw IllegalArgumentException("callback cannot be null")
        }
        val index = mCallbacks.lastIndexOf(callback)
        if (index < 0 || isRemoved(index)) {
            mCallbacks.add(callback)
        }
    }

    /**
     * Returns true if the callback at index has been marked for removal.

     * @param index The index into mCallbacks to check.
     * *
     * @return true if the callback at index has been marked for removal.
     */
    private fun isRemoved(index: Int): Boolean {
        if (index < java.lang.Long.SIZE) {
            // It is in the first 64 callbacks, just check the bit.
            val bitMask = 1L shl index
            return mFirst64Removed and bitMask != 0L
        } else {
            val remainderRemoved = mRemainderRemoved
            if (remainderRemoved == null) {
                // It is after the first 64 callbacks, but nothing else was marked for removal.
                return false
            } else {
                val maskIndex = index / java.lang.Long.SIZE - 1
                if (maskIndex >= remainderRemoved.size) {
                    // There are some items in mRemainderRemoved, but nothing at the given index.
                    return false
                } else {
                    // There is something marked for removal, so we have to check the bit.
                    val bits = remainderRemoved[maskIndex]
                    val bitMask = 1L shl index % java.lang.Long.SIZE
                    return bits and bitMask != 0L
                }
            }
        }
    }

    /**
     * Removes callbacks from startIndex to startIndex + Long.SIZE, based
     * on the bits set in removed.

     * @param startIndex The index into the mCallbacks to start removing callbacks.
     * *
     * @param removed The bits indicating removal, where each bit is set for one callback
     * *                to be removed.
     */
    private fun removeRemovedCallbacks(startIndex: Int, removed: Long) {
        // The naive approach should be fine. There may be a better bit-twiddling approach.
        val endIndex = startIndex + java.lang.Long.SIZE

        var bitMask = 1L shl java.lang.Long.SIZE - 1
        for (i in endIndex - 1 downTo startIndex) {
            if (removed and bitMask != 0L) {
                mCallbacks.removeAt(i)
            }
            bitMask = bitMask ushr 1
        }
    }

    /**
     * Remove a callback. This callback won't be notified after this call completes.

     * @param callback The callback to remove.
     */
    @Synchronized fun remove(callback: C) {
        if (mNotificationLevel == 0) {
            mCallbacks.remove(callback)
        } else {
            val index = mCallbacks.lastIndexOf(callback)
            if (index >= 0) {
                setRemovalBit(index)
            }
        }
    }

    private fun setRemovalBit(index: Int) {
        if (index < java.lang.Long.SIZE) {
            // It is in the first 64 callbacks, just check the bit.
            val bitMask = 1L shl index
            mFirst64Removed = mFirst64Removed or bitMask
        } else {
            val remainderIndex = index / java.lang.Long.SIZE - 1
            val remainderRemoved = mRemainderRemoved
            if (remainderRemoved == null) {
                mRemainderRemoved = LongArray(mCallbacks.size / java.lang.Long.SIZE)
            } else if (remainderRemoved.size < remainderIndex) {
                // need to make it bigger
                val newRemainders = LongArray(mCallbacks.size / java.lang.Long.SIZE)
                System.arraycopy(mRemainderRemoved, 0, newRemainders, 0, remainderRemoved.size)
                mRemainderRemoved = newRemainders
            }
            val bitMask = 1L shl index % java.lang.Long.SIZE
            if (remainderRemoved != null) {
                remainderRemoved[remainderIndex] = remainderRemoved[remainderIndex] or bitMask
            }
        }
    }

    /**
     * Makes a copy of the registered callbacks and returns it.

     * @return a copy of the registered callbacks.
     */
    @Synchronized fun copyCallbacks(): ArrayList<C> {
        val callbacks = ArrayList<C>(mCallbacks.size)
        val numListeners = mCallbacks.size
        for (i in 0..numListeners - 1) {
            if (!isRemoved(i)) {
                callbacks.add(mCallbacks[i])
            }
        }
        return callbacks
    }

    /**
     * Modifies `callbacks` to contain all callbacks in the CallbackRegistry.

     * @param callbacks modified to contain all callbacks registered to receive events.
     */
    @Synchronized fun copyCallbacks(callbacks: MutableList<C>) {
        callbacks.clear()
        val numListeners = mCallbacks.size
        for (i in 0..numListeners - 1) {
            if (!isRemoved(i)) {
                callbacks.add(mCallbacks[i])
            }
        }
    }

    /**
     * Returns true if there are no registered callbacks or false otherwise.

     * @return true if there are no registered callbacks or false otherwise.
     */
    @Synchronized fun isEmpty(): Boolean {
        if (mCallbacks.isEmpty()) {
            return true
        } else if (mNotificationLevel == 0) {
            return false
        } else {
            val numListeners = mCallbacks.size
            for (i in 0..numListeners - 1) {
                if (!isRemoved(i)) {
                    return false
                }
            }
            return true
        }
    }

    /**
     * Removes all callbacks from the list.
     */
    @Synchronized fun clear() {
        if (mNotificationLevel == 0) {
            mCallbacks.clear()
        } else if (!mCallbacks.isEmpty()) {
            for (i in mCallbacks.indices.reversed()) {
                setRemovalBit(i)
            }
        }
    }

    /**
     * @return A copy of the CallbackRegistry with all callbacks listening to both instances.
     */
    @Synchronized public override fun clone(): CallbackRegistry<C, T, A> {
        var clone: CallbackRegistry<C, T, A>? = null
        try {
            @Suppress("UNCHECKED_CAST")
            clone = super.clone() as CallbackRegistry<C, T, A>
            clone.mFirst64Removed = 0
            clone.mRemainderRemoved = null
            clone.mNotificationLevel = 0
            clone.mCallbacks = ArrayList<C>()
            val numListeners = mCallbacks.size
            for (i in 0..numListeners - 1) {
                if (!isRemoved(i)) {
                    clone.mCallbacks.add(mCallbacks[i])
                }
            }
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }

        return clone!!
    }
}


/**
 * Utility class for managing Observable callbacks.
 */
class PropertyChangeRegistry : CallbackRegistry<(Observable, KProperty<*>?) -> Unit, Observable, Unit?>
(notifierCallback) {

    /**
     * Notifies registered callbacks that a specific property has changed.

     * @param observable The Observable that has changed.
     * *
     * @param propertyId The BR id of the property that has changed or BR._all if the entire
     * *                   Observable has changed.
     */
    fun notifyChange(observable: Observable, property: KProperty<*>? = null) {
        notifyCallbacks(observable, property, null)
    }

    companion object {
        val notifierCallback = { callback: (Observable, KProperty<*>?) -> Unit,
                                 sender: Observable, property: KProperty<*>?, notUsed: Unit? ->
            callback(sender, property)
        }
    }

}