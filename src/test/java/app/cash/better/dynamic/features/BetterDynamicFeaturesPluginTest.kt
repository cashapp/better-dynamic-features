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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath, releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath, releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:4.9.3=debugRuntimeClasspath, releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath, releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:4.9.3=debugRuntimeClasspath, releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath, releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath, releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath, releaseRuntimeClasspath
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

  @Test fun `conflict check looks at transitive dependencies`() {
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugRuntimeClasspath, releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.7.20=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.20=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.7.20=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath, releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:4.9.3=debugRuntimeClasspath, releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.0=debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.0=debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugRuntimeClasspath, releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugRuntimeClasspath, releaseRuntimeClasspath
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

  @Test fun `configuration fails when feature project does not depend on base module`() {
    val integrationRoot = File("src/test/fixtures/no-base-dependency")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":feature:checkDependencyOnBase")
    val result = gradleRunner.buildAndFail()

    assertThat(result.output).contains("project ':feature' should have a dependency on the base project ':base'")
  }

  @Test fun `configuration fails when feature project does not depend on base module with reversed evaluation order`() {
    val integrationRoot = File("src/test/fixtures/no-base-dependency-reverse")
    val baseProject = integrationRoot.resolve("base")
    clearLockfile(baseProject)

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", ":feature:checkDependencyOnBase")
    val result = gradleRunner.buildAndFail()

    assertThat(result.output).contains("project ':feature' should have a dependency on the base project ':base'")
  }

  private fun clearLockfile(root: File) {
    root.lockfile().takeIf { it.exists() }?.delete()
  }

  private fun File.lockfile() = resolve("gradle.lockfile")
}
