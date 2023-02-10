// Copyright Square, Inc.
package app.cash.better.dynamic.features

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class BetterDynamicFeaturesPluginTest {
  @Test fun `base and feature apks have different library versions`() {
    val integrationRoot = File("src/test/fixtures/different-versions")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
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
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
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
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:4.9.3=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )
  }

  @Test fun `lockfile updates if task run after dependency version change`() {
    val integrationRoot = File("src/test/fixtures/version-update")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val result = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:writeLockfile")
      .build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:4.9.3=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )

    // Update dependency version
    val featureGradle = integrationRoot.resolve("feature/build.gradle")
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
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
      .withArguments("clean", ":base:writeLockfile")

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
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
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
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3 -> 5.0.0-alpha.2")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
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
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.retrofit2:retrofit:2.9.0=debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.7.20=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )
  }

  @Test fun `lockfiles activated on base runtime classpath configurations`() {
    val integrationRoot = File("src/test/fixtures/lockfile-activation")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:build")

    val result = gradleRunner.buildAndFail()

    // The included lockfile is invalid, so if activated then these errors should be shown
    assertThat(result.output).contains("Resolved 'org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10' which is not part of the dependency lock state")
    assertThat(result.output).contains("Cannot find a version of 'com.squareup.okhttp3:okhttp' that satisfies the version constraints:")
  }

  @Test fun `lockfile handles variant-specific dependencies correctly`() {
    val integrationRoot = File("src/test/fixtures/variant-aware")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.jakewharton.picnic:picnic:0.4.0 -> 0.5.0")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.jakewharton.picnic:picnic:0.5.0=debugRuntimeClasspath
      |com.squareup.okhttp3:okhttp:4.9.3=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.0=debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.0=debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )
  }

  @Test fun `build with outdated lockfile fails`() {
    val integrationRoot = File("src/test/fixtures/out-of-date")
    val lockfile = integrationRoot.resolve("base/gradle.lockfile")
    Files.copy(
      integrationRoot.resolve("base/gradle.lockfile.original").toPath(),
      lockfile.toPath(),
      StandardCopyOption.REPLACE_EXISTING,
    )

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:installDebug")

    val result = gradleRunner.buildAndFail()
    assertThat(result.output).contains("The lockfile was out of date and has been updated. Rerun your build.")

    val lockfileContent = lockfile.readText()
    val expectedContent = integrationRoot.resolve("base/gradle.lockfile.updated").readText()
    assertThat(lockfileContent).isEqualTo(expectedContent)
  }

  @Test fun `checkLockfile task stays up-to-date if dependencies unchanged`() {
    val integrationRoot = File("src/test/fixtures/up-to-date")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:preBuild")

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
      .withDebug(true)
      .withArguments("clean", ":base:writeLockfile")

    gradleRunner.build()

    val lockfile = baseProject.lockfile().readText()
    assertThat(lockfile).contains("com.squareup.sqldelight:runtime-jvm:1.5.4=debugRuntimeClasspath,releaseRuntimeClasspath")
    assertThat(lockfile).contains("com.squareup.sqldelight:runtime:1.5.4=debugRuntimeClasspath,releaseRuntimeClasspath")
  }

  @Test fun `lockfile does not include project dependencies`() {
    val integrationRoot = File("src/test/fixtures/project-dependency")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withDebug(true)
      .withArguments("clean", ":base:writeLockfile")

    gradleRunner.build()

    val lockfile = baseProject.lockfile().readText()
    assertThat(lockfile).doesNotContain("project-dependency:library:unspecified=debugRuntimeClasspath,releaseRuntimeClasspath")
  }

  @Test fun `custom lockfile location is written correctly`() {
    val integrationRoot = File("src/test/fixtures/custom-lockfile")
    val baseProject = integrationRoot.resolve("base")
    val customLockfile = baseProject.resolve("custom.lockfile")
    customLockfile.takeIf { it.exists() }?.delete()

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.squareup.okhttp3:okhttp:4.9.3")
    assertThat(customLockfile.readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:4.9.3=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugRuntimeClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath,releaseRuntimeClasspath
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
      .withArguments("clean", ":base:writeLockfile")

    // Generate lockfile first, and then run dependencies task
    gradleRunner.build()
    val result = gradleRunner.withArguments(":base:dependencies").build()

    assertThat(result.output).contains("com.google.android.material:material:1.2.0 -> 1.7.0")
    // Dynamic animation is a new transitive dependency of material added after 1.2.0
    assertThat(result.output).contains("androidx.dynamicanimation:dynamicanimation:1.0.0")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |androidx.activity:activity:1.5.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.annotation:annotation-experimental:1.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.annotation:annotation:1.3.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.appcompat:appcompat-resources:1.5.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.appcompat:appcompat:1.5.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.arch.core:core-common:2.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.arch.core:core-runtime:2.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.cardview:cardview:1.0.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.collection:collection:1.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.coordinatorlayout:coordinatorlayout:1.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.core:core:1.8.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.cursoradapter:cursoradapter:1.0.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.customview:customview:1.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.drawerlayout:drawerlayout:1.1.1=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.fragment:fragment:1.3.6=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.interpolator:interpolator:1.0.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-common:2.5.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-livedata-core:2.5.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-livedata:2.0.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-runtime:2.5.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.lifecycle:lifecycle-viewmodel:2.5.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.loader:loader:1.0.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.recyclerview:recyclerview:1.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.savedstate:savedstate:1.2.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.transition:transition:1.2.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.vectordrawable:vectordrawable-animated:1.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.vectordrawable:vectordrawable:1.1.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.versionedparcelable:versionedparcelable:1.1.1=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.viewpager2:viewpager2:1.0.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |androidx.viewpager:viewpager:1.0.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |com.google.android.material:material:1.7.0=debugRuntimeClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )
  }

  private fun clearLockfile(root: File) {
    root.lockfile().takeIf { it.exists() }?.delete()
  }

  private fun File.lockfile() = resolve("gradle.lockfile")
}
