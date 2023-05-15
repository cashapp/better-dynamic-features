// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("com.android.application") version "7.4.2" apply false
  id("org.jetbrains.kotlin.android") version "1.8.0" apply false
  id("com.android.dynamic-feature") version "7.4.2" apply false
  id("app.cash.better.dynamic.features") version "+" apply false
}

apply(from = "../buildscript.gradle")