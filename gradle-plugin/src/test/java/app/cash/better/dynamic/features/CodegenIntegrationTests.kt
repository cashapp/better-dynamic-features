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

import app.cash.better.dynamic.features.utils.assertThat
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import java.io.File
import kotlin.io.path.bufferedReader

class CodegenIntegrationTests {
  @Test
  fun `codegen generates code correctly`() {
    val integrationRoot = File("src/test/fixtures/codegen-fixture")
    writeCommonLockfile(integrationRoot)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withDebug(true)
      .withArguments("clean", ":base:bundleDebug")

    gradleRunner.build()

    val generatedCode = integrationRoot.resolve("base/build/betterDynamicFeatures/generatedImplementations/debug/ExampleFeatureImplementationsContainer.kt")
    assertThat(generatedCode.readText()).isEqualTo(
      """
      |import app.cash.better.`dynamic`.features.ExperimentalDynamicFeaturesApi
      |import app.cash.better.`dynamic`.features.ImplementationsContainer
      |import java.lang.ClassNotFoundException
      |import kotlin.OptIn
      |import kotlin.collections.List
      |import kotlin.collections.buildList
      |
      |@OptIn(ExperimentalDynamicFeaturesApi::class)
      |public object ExampleFeatureImplementationsContainer : ImplementationsContainer<ExampleFeature> {
      |  override fun buildImplementations(): List<ExampleFeature> = buildList {
      |    try {
      |      add(Class.forName("ExampleImplementation").getDeclaredConstructor().newInstance() as ExampleFeature)
      |    } catch(e: ClassNotFoundException) {
      |    }
      |  }
      |}
      |
      """.trimMargin(),
    )
  }

  @Test
  fun `generated classes are in dexfiles`() {
    val integrationRoot = File("src/test/fixtures/codegen-fixture")
    writeCommonLockfile(integrationRoot)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withDebug(true)
      .withArguments("clean", ":base:packageDebugUniversalApk")

    gradleRunner.build()

    val dexContent = dexdump(integrationRoot.resolve("base/build/outputs/apk_from_bundle/debug/base-debug-universal.apk"))
    assertThat(dexContent.bufferedReader().lineSequence()).containsInConsecutiveOrder(
      """  Class descriptor  : 'LExampleFeatureImplementationsContainer;'""",
      """  Access flags      : 0x0011 (PUBLIC FINAL)""",
      """  Superclass        : 'Ljava/lang/Object;'""",
      """  Interfaces        -""",
      """    #0              : 'Lapp/cash/better/dynamic/features/ImplementationsContainer;'""",
    )
  }

  @Test
  fun `proguard rules for generated code are generated`() {
    val integrationRoot = File("src/test/fixtures/codegen-fixture")
    writeCommonLockfile(integrationRoot)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withDebug(true)
      .withArguments("clean", ":base:bundleRelease")

    gradleRunner.build()

    val proguardFile = integrationRoot.resolve("base/build/betterDynamicFeatures/generatedProguard/betterDynamicFeatures-release.pro")
    assertThat(proguardFile.exists()).isTrue()
    assertThat(proguardFile.readText()).isEqualTo(
      """
        |-keepnames class ExampleFeature
        |-keep class ExampleFeatureImplementationsContainer { *; }
        |-keep class ExampleImplementation {
        |    public <init>();
        |}
        |
      """.trimMargin(),
    )
  }

  private fun writeCommonLockfile(projectRoot: File) {
    projectRoot.resolve("base/gradle.lockfile").writeText(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |androidx.activity:activity:1.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.annotation:annotation:1.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.arch.core:core-common:2.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.arch.core:core-runtime:2.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.collection:collection:1.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.core:core:1.2.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.customview:customview:1.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.fragment:fragment:1.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-common:2.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-livedata-core:2.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-livedata:2.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-runtime:2.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-viewmodel:2.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.loader:loader:1.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.savedstate:savedstate:1.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.versionedparcelable:versionedparcelable:1.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |androidx.viewpager:viewpager:1.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.google.android.gms:play-services-basement:18.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.google.android.gms:play-services-tasks:18.0.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.google.android.play:core-common:2.0.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.google.android.play:feature-delivery-ktx:2.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.google.android.play:feature-delivery:2.1.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okhttp3:okhttp:4.11.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio-jvm:3.2.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:3.2.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )
  }
}
