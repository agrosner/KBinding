package com.andrewgrosner.okbinding

infix fun <T> ObservableField<T>.bindTo(function: (T) -> Unit) = addOnPropertyChangedCallback {
    observable, i ->
    @Suppress("UNCHECKED_CAST")
    function((observable as ObservableField<T>).value)
}

infix fun ObservableBoolean.bindTo(function: (Boolean) -> Unit) = addOnPropertyChangedCallback {
    observable, i ->
    @Suppress("UNCHECKED_CAST")
    function((observable as ObservableBoolean).value)
}

infix fun ObservableByte.bindTo(function: (Byte) -> Unit) = addOnPropertyChangedCallback {
    observable, i ->
    @Suppress("UNCHECKED_CAST")
    function((observable as ObservableByte).value)
}

