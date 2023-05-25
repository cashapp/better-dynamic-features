# Better Features Dynamic

:construction: Under construction!

Making dynamic feature modules better.

Version Compatibility:

| Better Dynamic Features | AGP   |
|-------------------------|-------|
| 0.1.0                   | 8.0.1 |

## Setup

Apply this plugin and KSP to your `com.android.application` module and each of
your `com.android.dynamic-feature` modules.

```groovy
plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.ksp")
  id("app.cash.better.dynamic.features")
}

// Or...

plugins {
  id("com.android.dynamic-feature")
  id("org.jetbrains.kotlin.ksp")
  id("app.cash.better.dynamic.features")
}
```

# Feature Module Patches

The better dynamic features plugin includes a few patches to fix some bugs and improve the
development
experience of using feature modules in your project.

## Gradle Lockfiles

Because of the inverted dependency structure of a base module and its feature modules,
a [known issue](https://issuetracker.google.com/issues/185161615)
is that older versions of a dependency may be bundled into your app even if a feature module depends
on a newer version of that dependency.

The better dynamic features plugin includes a custom task that generates a [Gradle lockfile] which
resolves the dependency versions
between the base module and feature modules correctly. This lockfile is then used to pin the
versions of the base module's dependencies
to the correct versions.

### Usage

1. After applying the plugin, generate a lockfile for your base module. `gradlew :app:writeLockfile`
2. Check the `gradle.lockfile` file that is generated into your source control.
3. When adding or modifying dependencies, be sure to run this command again to update your lockfile.
4. You can validate the lockfile by running the `:app:checkLockfile` task. This will automatically
   update your lockfile if it was out of date.

## Feature Module Styles

A limitation of feature modules is that all manifest entries from a feature module are merged into
the base module's manifest, meaning that any resources coming from a feature module that are
referenced in the feature module's manifest will result in a resource linking error at build time.

For style resources referenced in the
manifest, [a common recommendation](https://medium.com/androiddevelopers/a-patchwork-plaid-monolith-to-modularized-app-60235d9f212e#:~:text=We%20worked%20around%20this%20by%20creating%20an%20empty%20declaration%20for%20each%20style%20within%20%3Acore%E2%80%99s%20styles.xml%20like%20this%3A)
has been to simply declare empty versions of those styles in the base module to satisfy the resource
linker. This works, but can cause unintended _runtime_ errors if, for example, some of these styles
are
later moved into the base module and your empty declaration overwrites the actual style definition.

To address this, the plugin adds a centralized way to declare these "external styles" in addition to
verifying that these declarations are not overwriting any styles in your base module.

### Usage

In your base module's gradle file, add the `betterDynamicFeatures` block as follows:

```groovy
betterDynamicFeatures {
  externalResources {
    // Add a declaration for each "external" style
    style("Your.Style.Name.Here")
    style("MyTheme")
  }
}
```

The plugin will automatically validate these resources during a regular build, and will report which
styles are conflicting.

```
# :app:assembleDebug, or :app:bundleDebug

Execution failed for task ':app:checkDebugExternalResources'.
> Some external resource declarations are overwriting actual resource definitions.
  Remove these from your gradle configuration:
  
  MyTheme
```

# Using Feature Module Code

Using feature module code from your base module isn't directly possible because your feature module
depends on the base module, not the other way around. The only way to reference code in your feature
module is to use reflection, which is inconvenient and loses some type safety (e.g. if your class
name changes).

Better dynamic features supports generating code to access these kinds of classes in a convenient
and type-safe way.

### Usage

In your **base** module, define an interface (or abstract class) that implements
the [`DynamicApi`](runtime/jvm/src/main/kotlin/app/cash/better/dynamic/features/DynamicApi.kt)
interface.

```kotlin
// app/src/main/...

interface MyCoolFeature : DynamicApi {
  fun doCoolThing()
}
```

In your **feature** module(s), implement your feature and annotate the class with
the [`DynamicImplementation`](runtime/jvm/src/main/kotlin/app/cash/better/dynamic/features/DynamicImplementation.kt)
annotation.

```kotlin
// feature/src/main/...

@DynamicImplementation
class SuperCoolFeatureImplementation : MyCoolFeature {
  override fun doCoolThing() {
    Log.wtf("SuperCool", "Wowee!")
  }
}
```

Back in your **base** module, you can use
the [`dynamicImplementations()`](runtime/src/main/kotlin/app/cash/better/dynamic/features/DynamicImplementations.kt)
method to get a list containing an instance of each class that is implemented and annotated in your feature modules.

This method returns a `Flow` which will automatically emit an updated list of instances when an on-demand
feature module is installed.

```kotlin
// app/src/main/...

suspend fun doTheCoolThings() {
  dynamicImplementations<MyCoolFeature>().collect { impls ->
    impls.forEach {
      it.doCoolThing()
    }
  }
}
```

[Gradle lockfile]: https://docs.gradle.org/current/userguide/dependency_locking.html

# License

```
Copyright 2023 Square, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
