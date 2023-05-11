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
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      .cleaned()
      .withArguments(":base:writeLockfile")
      .build()

    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.21=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.21=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      |com.squareup.okhttp3:okhttp:5.0.0-alpha.2=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.9.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.retrofit2:retrofit:2.9.0=debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.8.20=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
    assertThat(result.output).contains("Resolved 'org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10' which is not part of the dependency lock state")
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
    assertThat(baseProject.lockfile().readText()).isEqualTo(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.jakewharton.picnic:picnic:0.5.0=debugCompileClasspath,debugRuntimeClasspath
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.0=debugCompileClasspath,debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.0=debugCompileClasspath,debugRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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
      .cleaned()
      .withArguments(":base:installDebug")

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
      |com.squareup.okhttp3:okhttp:4.9.3=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:2.8.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:1.4.10=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
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

  private fun clearLockfile(root: File) {
    root.lockfile().takeIf { it.exists() }?.delete()
  }

  private fun File.lockfile() = resolve("gradle.lockfile")
}
