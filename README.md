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
            notifyChange(UserViewModel::name)
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

Then to bind views:

```kotlin

override fun createView(ui: AnkoContext<MainActivity>): View {
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

## Supported bindings

Currently we support two kinds of bindings:
  1. `oneWay` -> `ViewModel` to `View`
  2. `twoWay` -> `ViewModel` <-> `View`

Next we will support: `oneTime` and `oneWayToSource` (reverse of `oneWay`)
