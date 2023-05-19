# Better Features: Dynamic

A plugin for making dynamic feature modules better.

Required version of AGP: **8.0.0**

## Setup

Apply the plugin to your `com.android.application` module and each of your `com.android.dynamic-feature` modules.

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

## Gradle Lockfiles

This plugin uses [gradle lockfiles](https://docs.gradle.org/current/userguide/dependency_locking.html) to ensure that the dependency versions of your base and feature modules stay in sync.
The plugin will activate dependency locking on each of your `runtimeClasspath` configurations.

To generate the lockfile, run the `writeLockfile` task on your application module.
This will generate the `gradle.lockfile` file in the application module directory.
This file should be checked in to version control.
