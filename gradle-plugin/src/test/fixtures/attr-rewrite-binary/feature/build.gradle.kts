import com.android.build.api.artifact.ScopedArtifact
import com.android.build.gradle.internal.res.namespaced.ProcessAndroidAppResourcesTask
import com.android.build.gradle.tasks.PackageApplication
import com.android.build.gradle.tasks.ProcessAndroidResources
import org.gradle.configurationcache.extensions.capitalized

plugins {
  id("com.android.dynamic-feature")
  id("org.jetbrains.kotlin.android")
  id("app.cash.better.dynamic.features")
}
android {
  namespace = "com.example.attr.feature"
  compileSdk = 33

  defaultConfig {
    minSdk = 24
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
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
}

betterDynamicFeatures {
  enableResourceRewriting.set(true)
  baseProject.set(project(":app"))
}

dependencies {
  implementation(project(":app"))

  implementation("androidx.core:core-ktx:1.10.0")
  implementation("com.google.android.material:material:1.8.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
  implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
}
