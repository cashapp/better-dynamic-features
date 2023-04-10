import com.android.build.gradle.tasks.PackageApplication

plugins {
  alias(libs.plugins.com.android.application)
  alias(libs.plugins.org.jetbrains.kotlin.android)
  alias(libs.plugins.betterDynamicFeatures)
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
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    viewBinding = true
  }
  dynamicFeatures += setOf(":feature")
}

dependencies {

  implementation(libs.core.ktx)
  implementation(libs.appcompat)
  implementation(libs.material)
  implementation(libs.constraintlayout)
  implementation(libs.navigation.fragment.ktx)
  implementation(libs.navigation.ui.ktx)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.espresso.core)
}

tasks.withType(PackageApplication::class.java).configureEach {
  println("Configured: ${this.name}")
//  this.transformationRequest
}
