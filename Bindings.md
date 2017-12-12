# Supported Bindings

Currently we support three kinds of bindings:
  1. `oneWay` -> handle changes from `ViewModel` to `View`
  2. `twoWay` -> handles changes in both directions between `ViewModel` <-> `View`
  3. `oneWayToSource` -> handles changes from `View` to `ViewModel`

## One Way Bindings

`oneWay` bindings handle changes from a `Observable` or functional expression on a specific `View`.

The changes from an `ObservableField` come directly from the instance, while changes
from an expression need explicit wiring to determine for which property it came from.

### Flow
expression or `ObservableField.value` -> `Output` -> `View` -> `View` property set from `Output`

The expression or `ObservableField.value` is considered the `Input` which can get transformed in the `Output` phase,
which then applies to the `View` via Setter methods.  This library provides a few default out-of-the-box methods as conveniences. These are fully extensible and customizable.

The expression syntax is required to register to changes on a specific `KProperty` or `MyViewModelClass::someField`. Then whenever we want that expression to get reevaluated, we need to `notifyChange(MyViewModelClass::someField)` on the registered parent object, or `ViewModel`.

```kotlin

textView {
    bindSelf(MyViewModelClass::someField)
    { viewModel.someField }
      .toText(this)
}

```

Just by specifying this alone is not enough to get changes from that expression. We also need to, whenever that property is `set`, notify to our parent `viewModel` that a change occurred:

```kotlin

class MyViewModelClass : BaseObservable() {

  var someField = ""
  set(value) {
    field = value
    notifyChange(this::someField)
  }
}

```

When we call `notifyChange` that expression `{ viewModel.someField }` runs again
and the `textView` (in this example) updates its `text` with the result of that expression.

The expression syntax is very useful when we want to update UI based on our field's value.
Take, in an e-commerce app we want to display the number of items in the cart. The value is an `Int` but we want to update the UI whenever that count of items changes.  We define an `ObservableField<Int>` and bind it to the view:

```kotlin

textView {
    bind { viewModel.count }
      .on { string(R.string.someFormattedString, plural(R.plural.somePlural, it)) } // helper methods for `View.context`
      .toText(this)
}

```

So now whenever we call `viewModel.count.value = newValue`, the expression reruns and the UI updates!

To specify this example on a custom text setter:
```kotlin

textView {
    bind { viewModel.count }
      .on { string(R.string.someFormattedString, plural(R.plural.somePlural, it)) } // helper methods for `View.context`
      .toView(this, { view, value ->
          text = value
        })
}

```


## One Way To Source Bindings

`oneWayToSource` is the reverse of `oneWay`. It specifies that we want changes from the UI to send back data to our `ViewModel` via an `ObservableField` or expression.

Since Views will send back results to the expression or `ObservableField`, registering is a little different. We must first bind a `View` via a `ViewRegister`. `ViewRegister` are an abstract class that handle registering and unregistering specific listeners on `View`. For example, a `OnTextChangedRegister()` adds a `TextWatcher` on a `TextView` and receives callbacks when text changes. That result is then passed along to the expression or `ObservableField`.


### Flow
`View` -> `Output` -> `Input` -> expression or `ObservableField.value`

The `ViewRegister` knows how to convert the view's data to an `Output`, then the `on` clause specifies a potential conversion into another type `Input`.
Then the `Input` gets sent to the expression or `ObservableField.value` for updating.
For example:

```kotlin

textView {
    bind(this)
      .onSelf() // String updates from `text`
      .toObservable  { it.name }

    // or custom update method
    bindSelf(this).to { vm, input, view ->
      // viewmodel might be null
      vm?.let { vm ->
        // assign data to the viewmodel
        vm.name = input
      }
    }
}

```

For convenience, we provide a set of default `bind()` and `ViewRegister` that you can use out of the box. These are completely extensible and customizable.

Without convenience methods, we must create a `ViewRegister`:

```kotlin
class MyOnTextChangedRegister : ViewRegister<TextView, String>(), TextWatcher {

    override fun registerView(view: TextView) = view.addTextChangedListener(this)

    override fun deregisterFromView(view: TextView) = view.removeTextChangedListener(this)

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        notifyChange(s?.toString()) // pass changes to to listeners so data can update.
    }

    override fun getValue(view: TextView) = view.text.toString() // specifies how to convert view data out
}

```

Then pass it into the call to `bind`:

```kotlin
textView {
    bind(this, MyOnTextChangedRegister())
      .onSelf() // String updates from `text`
      .to { viewModel.count }
}
```

## Two Way Bindings

`twoWay` bindings are slightly more complex and complicated. It specifies that an expression or `ObservableField` and `View`'s data are synchronized. We only allow one such binding per `KProperty` or `ObservableField` to prevent a cycle of updates occurring in the UI.

We start off the binding the same way as a `oneWay` (`twoWay` extends off of `oneWay`) and then specify we want it `twoWay` and complete the reverse assignment. Any default, out-of-the-box `oneWay` binding on a `View` will only update `View` when the value is different than the current value. This prevents update cycles that could occur in a `twoWay`.

### Flow
expression or `ObservableField.value` -> `Output` -> `View` -> `View` property set -> `Output` -> expression or `ObservableField.value` is set if changed.

When a `View` changes, it notifies the expression or `ObservableField`. When the expression or `ObservableField` changes, they notify the `View`. Both the `View` and `ObservableField` have mechanisms in place to only change and notify when their value changes so that a cycle in this flow doesn't happen.

To register a `twoWay` binding on an `ObservableField` that relates to a user inputting data for an address:

```kotlin
editText {
  bindSelf { viewModel.address }.toText(this)
          .twoWay().toFieldFromText()
}
```

This means that any changes from user input get populated into the `address` property, and any changes (say from API call) are passed along to the `editText` as well.

The `toFieldFromText()` is a convenience method that assigns an `ObservableField` the value from the `editText` in reverse. There are more convenience methods.

To use expressions without conveniences:

```kotlin
editText {
  bindSelf(MyViewModelClass::address) { viewModel.address }.toText(this)
          .twoWay().toInput(OnTextChangedRegister()) { viewValue ->
            viewModel.address.value = it ?: viewModel.address.defaultValue // if null, set non-null default if we'd like.
            }
}
```
