# Better Features Dynamic

Making dynamic feature modules better.

**This project is no longer under active development.**

## Setup

Apply this plugin to your `com.android.application` module and each of
your `com.android.dynamic-feature` modules.

```groovy
plugins {
  id("com.android.application")
  id("app.cash.better.dynamic.features")
}

// Or...

plugins {
  id("com.android.dynamic-feature")
  id("app.cash.better.dynamic.features")
}
```

# Sample Project

See the [sample README](sample/README.md) for details.

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

## Android Gradle Plugin Patch

A [known bug](https://issuetracker.google.com/issues/246326007) can cause classes to be missing in
your app bundle when building with dynamic feature modules, resulting in unexpected runtime crashes.
This project includes a patch for the Android Gradle Plugin that works around this issue, and it can
be applied to your project as a Gradle plugin.

```groovy
// root build.gradle file
buildscript {
  dependencies {
    classpath "app.cash.better.dynamic.features:agp-patch:0.5.0-agp8.5.0" // Must be loaded before AGP!
    classpath "com.android.tools.build:gradle:8.5.0"
  }
}
```

Loading the plugin onto the classpath is sufficient to apply the patch, however because this
directly patches one of AGP's classes it is tied directly to a specific version of AGP. To validate
that the correct version of the plugin that matches your AGP version is being applied, you can add
the plugin to your application module.

```groovy
// app/build.gradle
plugins {
  id("app.cash.better.dynamic.features.agp-patch") version "0.2.1"
  id("com.android.application") version "7.4.2" // Wrong version!
}

// Reports an error:
// "This version of the Android Gradle Plugin (7.4.2) is not supported by the better-dynamic-features plugin. Only version 8.2.0 is supported."
```

Note that this bug has been marked as fixed in AGP 8.1, however this fix was made by disabling
incremental dexing transforms for feature modules. When the underlying Gradle bug has been fixed,
[this will be reverted](https://issuetracker.google.com/issues/268603989) but in the meantime this
project will continue to patch this issue without disabling the incremental transforms.

### Patch Plugin Version Compatibility

Version Compatibility:

| AGP   | Better Dynamic Features |
|-------|-------------------------|
| 8.1.0 | 0.1.0                   |
| 8.2.0 | 0.2.0                   |
| 8.2.1 | 0.2.1                   |
| 8.3.0 | 0.3.0                   |
| 8.3.2 | 0.3.1                   |
| 8.4.1 | 0.4.0                   |
| 8.5.0 | **0.5.0-agp8.5.0**      |
| X.Y.Z | 0.5.0-agpX.Y.Z          |

From version `0.5.0` and up, the targeted version of AGP is appended to the version in the format of `-agpX.Y.Z`.
The main better-dynamic-features version indicates the runtime API version of the library.

# Using Feature Module Code

Using feature module code from your base module isn't directly possible because your feature module
depends on the base module, not the other way around. The only way to reference code in your feature
module is to use reflection, which is inconvenient and loses some type safety (e.g. if your class
name changes).

Better dynamic features supports generating code to access these kinds of classes in a convenient
and type-safe way.

### Usage

Add the KSP plugin to each of your application and feature modules. The better dynamic features
plugin will automatically configure KSP to do the code generation described below.

```groovy
plugins {
  id("com.google.devtools.ksp")
  id("app.cash.better.dynamic.features")
}
```

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
method to get a list containing an instance of each class that is implemented and annotated in your
feature modules.

This method returns a `Flow` which will automatically emit an updated list of instances when an
on-demand feature module is installed.

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
