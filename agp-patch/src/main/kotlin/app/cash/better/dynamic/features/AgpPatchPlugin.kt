/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.better.dynamic.features

import com.android.Version
import com.android.build.gradle.internal.crash.afterEvaluate
import org.gradle.api.Plugin
import org.gradle.api.Project

class AgpPatchPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create("betterDynamicFeaturesPatch", BetterDynamicFeaturesPatchExtension::class.java)

    target.plugins.withId("com.android.application") {
      target.afterEvaluate {

        val agpVersion = Version.ANDROID_GRADLE_PLUGIN_VERSION
        // Skip the version check if we ignored the current AGP version!
        if (agpVersion in extension.ignoredVersions) {
          target.logger.info("Version compatibility check for Android Gradle Plugin $agpVersion skipped.")
          return@afterEvaluate
        }

        // Do a strict version check to ensure that our monkey-patching will work correctly.
        check(agpVersion == TARGET_AGP_VERSION) {
          "This version of the Android Gradle Plugin ($agpVersion) is not supported by the better-dynamic-features plugin. Only version $TARGET_AGP_VERSION is supported."
        }
      }
    }
  }
}
