# Better Features: Dynamic

A plugin for making dynamic feature modules better.

## Setup

Apply the plugin to a `com.android.application` module that has dynamic features declared in it.
The plugin automatically does the rest.

## Dependency Checking

This plugin adds a task to check that your base module and dynamic feature modules are all pulling
the same versions of each dependency (including transitive ones).

Usage:

```shell
./gradlew :app:checkInternalDebugDependencyVersions
```

This task will either pass or fail with a list of mismatched dependencies.
