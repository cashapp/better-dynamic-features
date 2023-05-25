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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class Convention : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply("org.jetbrains.kotlin.jvm")

    target.kotlinExtension.jvmToolchain { spec ->
      spec.languageVersion.set(JavaLanguageVersion.of(TOOLCHAIN_JDK))
      spec.vendor.set(JvmVendorSpec.AZUL)
    }

    target.tasks.withType(KotlinCompile::class.java).configureEach { task ->
      task.compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(TARGET_JDK.toString()))
      }
    }

    target.tasks.withType(JavaCompile::class.java).configureEach { task ->
      task.sourceCompatibility = TARGET_JDK.toString()
      task.targetCompatibility = TARGET_JDK.toString()
    }
  }

  private companion object {
    const val TOOLCHAIN_JDK = 17
    const val TARGET_JDK = 11
  }
}
