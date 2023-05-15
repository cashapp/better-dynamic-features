import com.android.build.gradle.tasks.PackageApplication

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
  id("app.cash.better.dynamic.features")
}

android {
  namespace = "com.example.attr"
  compileSdk = 33

  defaultConfig {
    applicationId = "com.example.attr"
    minSdk = 24
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  buildFeatures {
    viewBinding = true
  }
  dynamicFeatures += setOf(":feature")
}

dependencies {
  implementation("androidx.core:core-ktx:1.10.0")
  implementation("com.google.android.material:material:1.8.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
  implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
}
