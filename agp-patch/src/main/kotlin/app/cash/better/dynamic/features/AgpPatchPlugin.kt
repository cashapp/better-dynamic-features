package app.cash.better.dynamic.features

import com.android.Version
import org.gradle.api.Plugin
import org.gradle.api.Project

class AgpPatchPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.withId("com.android.application") {
      // Do a strict version check to ensure that our monkey-patching will work correctly.
      check(Version.ANDROID_GRADLE_PLUGIN_VERSION == TARGET_AGP_VERSION) {
        "This version of the Android Gradle Plugin (${Version.ANDROID_GRADLE_PLUGIN_VERSION}) is not supported by the better-dynamic-features plugin. Only version $TARGET_AGP_VERSION is supported."
      }
    }
  }
}
