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

import app.cash.better.dynamic.features.tasks.Version
import app.cash.better.dynamic.features.utils.assertThat
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File
import kotlin.io.path.bufferedReader

class BetterDynamicFeaturesPluginTest {
  @Test fun `base and feature apks have different library versions`() {
    val integrationRoot = File("src/test/fixtures/different-versions")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )
  }

  @Test fun `base configurations have different dependency versions`() {
    val integrationRoot = File("src/test/fixtures/different-versions-in-base")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

  @Test fun `base and feature apks have same library versions`() {
    val integrationRoot = File("src/test/fixtures/same-versions")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )
  }

  @Test fun `lockfile updates if task run after dependency version change`() {
    val integrationRoot = File("src/test/fixtures/version-update")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val featureGradle = integrationRoot.resolve("feature/build.gradle")
    featureGradle.writeText(featureGradle.readText().replace("5.0.0-alpha.2", "4.9.3"))

    val result = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")
      .build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

    // Update dependency version
    featureGradle.writeText(featureGradle.readText().replace("4.9.3", "5.0.0-alpha.2"))

    val newGradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments(":base:writeLockfile")

    val newResult = newGradleRunner.build()
    assertThat(newResult.output).contains("BUILD SUCCESSFUL")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

    // Undo that
    featureGradle.writeText(featureGradle.readText().replace("5.0.0-alpha.2", "4.9.3"))
  }

  @Test fun `plugin tasks do not build project module dependencies`() {
    val integrationRoot = File("src/test/fixtures/project-dependency")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    val result = gradleRunner.build()
    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(result.task(":library:jar")?.outcome).isNull()
  }

  @Test fun `lockfile includes transitive dependencies from feature dependencies`() {
    val integrationRoot = File("src/test/fixtures/transitive-dependency")

    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

  @Test fun `lockfile includes transitive dependencies from base module project dependencies`() {
    val integrationRoot = File("src/test/fixtures/transitive-dependency-on-base")

    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

  @Test fun `variant-specific transitive dependencies from base module project dependencies are handled`() {
    val integrationRoot = File("src/test/fixtures/transitive-dependency-on-base-variant-aware")

    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    assertThat(baseProject.lockfile().readText()).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.retrofit2:retrofit:2.9.0=debugRuntimeClasspath
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

  @Test fun `lockfiles activated on base runtime classpath configurations`() {
    val integrationRoot = File("src/test/fixtures/lockfile-activation")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:build")

    val result = gradleRunner.buildAndFail()

    // The included lockfile is invalid, so if activated then these errors should be shown
    assertThat(result.output).contains("Resolved 'org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION' which is not part of the dependency lock state")
    assertThat(result.output).contains("Cannot find a version of 'com.squareup.okhttp3:okhttp' that satisfies the version constraints:")
  }

  @Test fun `lockfile handles variant-specific dependencies correctly`() {
    val integrationRoot = File("src/test/fixtures/variant-aware")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.jakewharton.picnic:picnic:0.4.0 -> 0.5.0")
    assertThat(baseProject.lockfile().readText()).contains(
      """
      |com.jakewharton.picnic:picnic:0.5.0=debugCompileClasspath,debugRuntimeClasspath
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      """.trimMargin(),
    )
    assertThat(baseProject.lockfile().readText()).contains(
      """
      |org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      """.trimMargin(),
    )
  }

  @Test fun `build with outdated lockfile fails`() {
    val integrationRoot = File("src/test/fixtures/out-of-date")
    val lockfile = integrationRoot.resolve("base/gradle.lockfile")
    integrationRoot.lockfile("base").writeText(
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
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:installDebug")

    val result = gradleRunner.buildAndFail()
    assertThat(result.output).contains("The lockfile was out of date and has been updated. Rerun your build.")

    val lockfileContent = lockfile.readText()
    assertThat(lockfileContent).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

  @Test fun `checkLockfile task stays up-to-date if dependencies unchanged`() {
    val integrationRoot = File("src/test/fixtures/up-to-date")

    integrationRoot.lockfile("base").writeText(
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:preBuild")

    gradleRunner.build()

    val result = gradleRunner.withArguments(":base:preBuild").build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(result.task(":base:checkLockfile")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
  }

  @Test fun `lockfile references multiplatform dependencies correctly`() {
    val integrationRoot = File("src/test/fixtures/multiplatform-dependency")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    gradleRunner.build()

    val lockfile = baseProject.lockfile().readText()
    assertThat(lockfile).contains("com.squareup.sqldelight:runtime-jvm:1.5.4=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
    assertThat(lockfile).contains("com.squareup.sqldelight:runtime:1.5.4=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
  }

  @Test fun `lockfile does not include project dependencies`() {
    val integrationRoot = File("src/test/fixtures/project-dependency")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    gradleRunner.build()

    val lockfile = baseProject.lockfile().readText()
    assertThat(lockfile).doesNotContain("project-dependency:library:unspecified=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
  }

  @Test fun `custom lockfile location is written correctly`() {
    val integrationRoot = File("src/test/fixtures/custom-lockfile")
    val baseProject = integrationRoot.resolve("base")
    val customLockfile = baseProject.resolve("custom.lockfile")
    customLockfile.takeIf { it.exists() }?.delete()

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3")
    assertThat(customLockfile.readText()).isEqualTo(
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
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

  @Test fun `new transitive dependency is recorded in lockfile`() {
    val integrationRoot = File("src/test/fixtures/new-transitive-dependency")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.google.android.material:material:1.2.0 -> 1.7.0")
    // Dynamic animation is a new transitive dependency of material added after 1.2.0
    assertThat(result.output).contains("androidx.dynamicanimation:dynamicanimation:1.0.0")

    val lockfileContent = baseProject.lockfile().readText()
    assertThat(lockfileContent).contains("com.google.android.material:material:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
    assertThat(lockfileContent).contains("androidx.dynamicanimation:dynamicanimation:1.0.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
  }

  @Test fun `constraints from BOMs are not pulled into lockfile`() {
    val integrationRoot = File("src/test/fixtures/has-constraints")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    gradleRunner.build()

    val lockfileContent = baseProject.lockfile().readText()
    assertThat(lockfileContent).contains("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
    assertThat(lockfileContent).contains("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
    // These are constraints that exists on the BOM which shouldn't be pulled in
    assertThat(lockfileContent).doesNotContain("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
    assertThat(lockfileContent).doesNotContain("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath")
  }

  @Test fun `writeLockfile task passes with configureondemand enabled`() {
    val integrationRoot = File("src/test/fixtures/same-versions")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    // We run the clean task only on :base to avoid direct configuration of the :feature module
    // This tests that the :feature module is configured indirectly through being depended on by :base
    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned(module = "base")
      .withArguments("-Dorg.gradle.configureondemand=true", ":base:writeLockfile")

    gradleRunner.build()
  }

  @Test fun `updating lockfile while with other tasks fails`() {
    val integrationRoot = File("src/test/fixtures/same-versions")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:writeLockfile")

    val result = gradleRunner.buildAndFail()
    assertThat(result.output).contains("Updating the lockfile and running other tasks together is an error. Update the lockfile first, and then run your other tasks separately.")
  }

  @Test fun `feature module compile classpath does not influence base module runtime classpath`() {
    val integrationRoot = File("src/test/fixtures/feature-compile-classpath")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile", "--stacktrace")

    gradleRunner.build()

    assertThat(baseProject.lockfile().readText()).contains(
      """
      |org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      """.trimMargin(),
    )
  }

  @Test fun `semver comparison is correct`() {
    val low = Version("2.8.7")
    val high = Version("2.10.1")

    assertThat(high).isGreaterThan(low)
  }

  @Test fun `missing dex patch works correctly`() {
    val integrationRoot = File("src/test/fixtures/missing-dex")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:packageDebugUniversalApk", "--stacktrace")

    gradleRunner.build()

    val dexContent = dexdump(integrationRoot.resolve("base/build/outputs/apk_from_bundle/debug/base-debug-universal.apk"))
    assertThat(dexContent.bufferedReader().lineSequence()).containsInConsecutiveOrder(
      """  Class descriptor  : 'Lcom/example/JavaClass;'""",
      """  Access flags      : 0x0001 (PUBLIC)""",
      """  Superclass        : 'Ljava/lang/Object;'""",
    )
    assertThat(dexContent.bufferedReader().lineSequence()).containsInConsecutiveOrder(
      """  Class descriptor  : 'Lcom/example/KotlinClass;'""",
      """  Access flags      : 0x0011 (PUBLIC FINAL)""",
      """  Superclass        : 'Ljava/lang/Object;'""",
    )
  }

  @Test fun `custom build type is acknowledged`() {
    val integrationRoot = File("src/test/fixtures/custom-build-type")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withArguments(":base:writeLockfile")

    // Generate lockfile first, and then run assemble task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:assembleStaging", "--stacktrace").build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  private fun clearLockfile(root: File) {
    root.lockfile().takeIf { it.exists() }?.delete()
  }

  private fun File.lockfile(module: String? = null) =
    resolve(if (module != null) "$module/gradle.lockfile" else "gradle.lockfile")
}
