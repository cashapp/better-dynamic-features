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
