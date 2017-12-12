# KBinding

KBinding is a Kotlin databinding library best used with [Anko](https://github.com/Kotlin/anko) to enable databinding in a fluent, easy to understand syntax.

We can represent our UI in ways such as:
```kotlin
verticalLayout {

  textView {
    bindSelf { it.name }.toText(this)
  }

  editText {
    hint = "Email"
    bindSelf { it.emailInput }
      .toText(this)
      .twoWay(
      .toFieldFromText()
  }.lparams {
    width = MATCH_PARENT
  }

  textView {
    bindSelf { it.emailInput } // mirrors input for example
      .toText(this)
  }
}
```

## Including in your project

```gradle

buildscript {
  ext {
    kbinding_version = "1.0.0"
  }
}

allProjects {
  repositories {
    // required to find the project's artifacts
    maven { url "https://www.jitpack.io" }
  }
}
```

```gradle

compile "com.github.agrosner.kbinding:kbinding:$kbinding_version"

// to use with Anko, separate artifact.
compile 'org.jetbrains.anko:anko-sdk15:0.10.3' // current version of anko used
compile "com.github.agrosner.KBinding:kbinding-anko:$kbinding_version"
```

## Documentation

We support three kind of bindings:
1. One Way (ViewModel to View)
2. Two Way (ViewModel <-> View)
3. One Way to Source (View to ViewModel)

Read more in the docs below:

[Supported Bindings](/Bindings.md)

[Getting Started](/GettingStartedAnko.md)

[KBinding By Example](/Examples.md)

## Pull Requests
I welcome and encourage all pull requests. Here are some basic rules to follow to ensure timely addition of your request:
  1. Match coding style (braces, spacing, etc.) This is best achieved using CMD+Option+L (Reformat code) on Mac (not sure for Windows) with Android Studio defaults.
  2. If its a feature, bugfix, or anything please only change code to what you specify.
  3. Please keep PR titles easy to read and descriptive of changes, this will make them easier to merge :)
  4. Pull requests _must_ be made against `develop` branch. Any other branch (unless specified by the maintainers) will get rejected.
  5. Have fun!

## Maintained By
[agrosner](https://github.com/agrosner) ([@agrosner](https://www.twitter.com/agrosner))
