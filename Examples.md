# KBinding By Example:

Create a `BindingHolder` to contain our bindings:

```kotlin
val holder = BindingHolder(viewModel)
```

Bind the value of `ObservableField<String>` to the UI `TextView`:

```kotlin
// one way binding on Observable fields
holder.bindSelf { viewModel.name }.toText(textView)
```

Bind the field value of a normal `String` to the visibility `Int`:
```kotlin
// one way binding on non observables
holder.bind(ViewModel::name) { it.name }
    .onIsNotNullOrEmpty() // if null or empty return false
    .toShowHideView(someView) // if true show, if false hide.
```

Bind both changes of `ObservableField<String>` and `EditText` value changes:
```kotlin
// two way binding on observable that synchronizes text and value changes.
holder.bindSelf { it.name }
  .toText(input)
  .twoWay()
  .toFieldFromText()
```

Bind changes of `ObservableField<Boolean>` and a `CheckBox` change together:
```kotlin
// two way binding that synchronizes compoundbutton / checkbox changes
holder.bindSelf { it.selected }
        .toOnCheckedChange(checkbox)
        .twoWay()
        .toFieldFromCompound()
```

Back changes in UI from a `TextView`/`EditText` with an `ObservableField<String>`:
```kotlin
// binds input changes from the view to the name property.
holder.bind(textView)
    .onSelf()
    .to { it.name }
```

Back changes in UI from a `TextView`/`EditText` to a non-observable property:
```kotlin
// binds input changes from the view to the name property (non observable).
holder.bind(textView)
    .onSelf()
    .to { input, view -> it.name = input}
```

We support swapping top-level `ViewModel` in the `BindingHolder`:
```kotlin
holder.viewModel = viewModel // set the ViewModel (no restriction and could be a `Presenter`)
holder.viewModel = null // support null `ViewModel` too!
// if bound will reevaluate the bindings.
```

By default bindings are __not__ executed when the `ViewModel` is `null`. If you wish to supply default values or execute when it is `null`, use:
```kotlin
// if normal binding, a default value for field is used and the expression is not evaluated.
// in this case it's executed always
holder.bindNullable(ViewModel::name) { it?.name }
  .onSelf()
  .toText(textView)
```

When done, cleanup bindings to prevent memory leaks! (`BindingComponent` for Anko does the cleanup for you when calling `destroyView()`)
```kotlin
holder.unbindAll() // when done, unbind
```

We can also _easily_ turn off individual bindings as needed, just hold a reference to it:
```kotlin
val binding = holder.bindSelf(textView).toObservable { it.name }
binding.unbind() // can turn off binding as needed

```
