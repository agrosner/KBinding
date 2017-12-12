## Getting Started Using Anko

KBinding works best with [Anko](https://github.com/Kotlin/anko), but can be used by other consumers.

First, we need a ViewModel or object to send data to our observableBindings.

By default normal properties, when their value changes, will not propagate those changes
to our observableBindings. So we have a couple of options. First we must extend `BaseObservable`.

```kotlin

class UserViewModel(var name: String = "") : BaseObservable()

```

The base class of `BaseObservable` by default handles propagating changes of the ViewModel
 and fields to the observableBindings when notified. In order to notify changes to the parent `ViewModel`,
 we have three options:

  1. Override a fields `setter` and notify changes to our `BaseObservable`:

```kotlin
var name = ""
    set(value) {
        field = value
        notifyChange(this::name)
    }

```
  2. Delegate the property to our `Observable` implementation:

```kotlin
var name: String by observable("")

```
This auto-registers changes to the field to the parent `ViewModel` to know when value changes.

  3. Make the field `Observable` (preferred).

```kotlin

var name = observable("")

```

Option 3 is the simplest as we can bind directly to the field and notify its changes. Option 2 is the best for outside consumers so it's treated like a real field. Option 1 should be used for efficiency in implementation.

When binding, option (1) requires us to explicitly notify the parent on change of the field.
Option (1) and (2) also requires us to specify the field in the binding:
```kotlin

holder.bindSelf(UserViewModel::name) { it.name }
  .toText(this)

```

Option (3) is preferred since we can then easily bind data changes without explicit reference
to the `KProperty`:

```kotlin

holder.bindSelf { it.name }.toText(this)

```

We use `it` in the binding, not the direct reference to `viewModel` or top-level object in the view because if that `ViewModel` changes, the bindings will reference a stale object!

### Create the UI

Have our components that we use in `Anko` extend `BindingComponent<Activity, ViewModel>` for convenience collection and disposal of the observableBindings:

```kotlin
class MainActivityLayout(mainActivityViewModel: MainActivityViewModel)
    : BindingComponent<MainActivity, MainActivityViewModel>(mainActivityViewModel) {
```

Instead of overridding `createView()`, we override `createViewWithBindings()`. This is
so we internally can bind all created observableBindings after view is created.

Then to bind views:

```kotlin

override fun createViewWithBindings(ui: AnkoContext<MainActivity>): View {
  return with(ui) {
    textView {
      bindSelf(UserViewModel::name) { it.name }
        .toText(this)
        .twoWay()
        .toFieldFromText()
    }
  }
}

```

The `BindingComponent` is backed by the `BindingHolder`, which collects and manages
the observableBindings.

If we do not unbind the `BindingHolder`, it will lead to memory leaks of all of the observableBindings. You need to explicitly call `unbind()` when calling the `BindingHolder` directly, or `destroyView()` if using the `BindingComponent`:

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
