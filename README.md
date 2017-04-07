# KBinding

KBinding is a library for [Anko](https://github.com/Kotlin/anko) to enable databinding in a fluent, easy to understand syntax.

```kotlin

val holder = BindingHolder(viewModel)

// one way binding on Observable fields
holder.oneWay(bindSelf(viewModel.name).toText(textView))

// one way binding on non observables
holder.oneWay(ViewModel::name,
  bind({ viewModel.name })
    .on { if (it == "") View.GONE : View.VISIBLE } // convert to another type
    .toViewVisibility())

// two way binding on observable that synchronizes text and value changes.
holder.twoWay(bindSelf(viewModel.name)
  .toText(textView)
  .twoWay()
  .toFieldFromText())

// two way binding that synchronizes compoundbutton / checkbox changes
holder.twoWay(bindSelf(viewModel.selected)
        .toOnCheckedChange(checkbox)
        .twoWay()
        .toFieldFromCompound())

// binds input changes from the view to the name property.
holder.oneWayToSource(bind(textView)
    .onSelf()
    .to(viewModel.name))

// binds input changes from the view to the name property (non observable).
holder.oneWayToSource(bind(textView)
    .onSelf()
    .to { input, view -> viewModel.name = input})

holder.viewModel = viewModel // set the ViewModel (no restriction and could be a `Presenter`)
holder.bindAll() // binds all bindings, also will execute all of them once.

holder.unbindAll() // when done, unbind

```

## Including in your project

```gradle

allProjects {
  repositories {
    // required to find the project's artifacts
    maven { url "https://www.jitpack.io" }
  }
}
```

```gradle
compile 'org.jetbrains.anko:anko-sdk15:0.9.1' // current version of anko used
compile 'com.github.agrosner:KBinding:1.0.0-beta1' // version of KBinding
```

## Getting Started

KBinding works best with [Anko](https://github.com/Kotlin/anko), but can be used by other consumers.

First, we need a ViewModel or object to send data to our bindings.

By default normal properties, when their value changes, will not propagate those changes
to our bindings. So we have a couple of options. First we must extend `BaseObservable`.

```kotlin

class UserViewModel(var name: String = "") : BaseObservable()

```

The base class of `BaseObservable` by default handles propagating changes of the ViewModel
 and fields to the bindings when notified. In order to notify changes to the parent `ViewModel`,
 we have three options:

  1. Override a fields `setter` and notify changes to our `BaseObservable`:

```kotlin
var name = ""
        set(value) {
            field = value
            notifyChange(this::name)
        }

```
  2. Delegate the property to our `Observable` implementation and register in the constructor:

```kotlin
var name: String by observable("") { observable, property -> notifyChange(property) }

```
  3. Make the field `Observable` (preferred).

```kotlin

var name = observable("")

```

When binding, option (1) requires us to explicitly notify the parent on change of the field.
Option (1) and (2) also requires us to specify the field in the binding:
```kotlin

oneWay(UserViewModel::name, bindSelf { viewModel.name }.toText(this))

```

Option (3) is preferred since we can then easily bind data changes without explicit reference
to the `KProperty`:

```kotlin

oneWay(bindSelf(viewModel.name).toText(this))

```

### Create the UI

Have our components that we use in `Anko` extend `BindingComponent<Activity, ViewModel>` for convenience collection and disposal of the bindings:

```kotlin
class MainActivityLayout(mainActivityViewModel: MainActivityViewModel)
    : BindingComponent<MainActivity, MainActivityViewModel>(mainActivityViewModel) {
```

Instead of overridding `createView()`, we override `createViewWithBindings()`. This is
so we internally can bind all created bindings after view is created.

Then to bind views:

```kotlin

override fun createViewWithBindings(ui: AnkoContext<MainActivity>): View {
        return with(ui) {
          textView {
            twoWay(UserViewModel::name,
              bindSelf { viewModel.name }
              .toText(this)
              .twoWay()
              .toFieldFromText())
          }

```

The `BindingComponent` is backed by the `BindingHolder`, which collects and manages
the bindings.

If we do not unbind the `BindingHolder`, it will lead to memory leaks of all of the bindings. You need to explicitly call `unbind()` when calling the `BindingHolder` directly, or `destroyView()` if using the `BindingComponent`:

```kotlin
bindingHolder.unbind()

```

In an `Activity.onDestroy()` (or `Fragment.onDestroyView()`)

```kotlin

override fun onDestroy() {
  super.onDestroy()
  component.destroyView()
}

```

# Supported bindings

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
    oneWay(MyViewModelClass::someField,
      bindSelf { viewModel.someField }
      .toText(this))
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
    oneWay(bind(viewModel.count)
      .on { string(R.string.someFormattedString, plural(R.plural.somePlural, it)) } // helper methods for `View.context`
      .toText(this))
}

```

So now whenever we call `viewModel.count.value = newValue`, the expression reruns and the UI updates!

To specify this example on a custom text setter:
```kotlin

textView {
    oneWay(bind(viewModel.count)
      .on { string(R.string.someFormattedString, plural(R.plural.somePlural, it)) } // helper methods for `View.context`
      .toView(this, { value ->
          text = value
        }))
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
    oneWayToSource(bind(this)
      .onSelf() // String updates from `text`
      .to(viewModel.count))
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
    oneWayToSource(bind(this, MyOnTextChangedRegister())
      .onSelf() // String updates from `text`
      .to(viewModel.count))
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
  twoWay(bindSelf(viewModel.address).toText(this)
          .twoWay().toFieldFromText())
}
```

This means that any changes from user input get populated into the `address` property, and any changes (say from API call) are passed along to the `editText` as well.

The `toFieldFromText()` is a convenience method that assigns an `ObservableField` the value from the `editText` in reverse. There are more convenience methods.

To use expressions without conveniences:

```kotlin
editText {
  twoWay(MyViewModelClass::address, bindSelf { viewModel.address }.toText(this)
          .twoWay().toInput(OnTextChangedRegister()) { viewValue ->
            viewModel.address.value = it ?: viewModel.address.defaultValue // if null, set non-null default if we'd like.
            })
}
```

## Pull Requests
I welcome and encourage all pull requests. It usually will take me within 24-48 hours to respond to any issue or request. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using CMD+Option+L (Reformat code) on Mac (not sure for Windows) with Android Studio defaults.
  2. If its a feature, bugfix, or anything please only change code to what you specify.
  3. Please keep PR titles easy to read and descriptive of changes, this will make them easier to merge :)
  4. Pull requests _must_ be made against `develop` branch. Any other branch (unless specified by the maintainers) will get rejected.
  5. Have fun!

## Maintained By
[agrosner](https://github.com/agrosner) ([@agrosner](https://www.twitter.com/agrosner))
