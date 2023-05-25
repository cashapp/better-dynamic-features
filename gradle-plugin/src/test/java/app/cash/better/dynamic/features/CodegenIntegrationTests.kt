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
    integrationRoot.resolve("base/gradle.lockfile").writeText(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:4.11.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio-jvm:3.2.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:3.2.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )

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
      |import kotlin.OptIn
      |import kotlin.collections.List
      |
      |@OptIn(ExperimentalDynamicFeaturesApi::class)
      |public object ExampleFeatureImplementationsContainer : ImplementationsContainer<ExampleFeature> {
      |  public override val implementations: List<ExampleFeature> = buildList {
      |  add(Class.forName("ExampleImplementation").getDeclaredConstructor().newInstance() as
      |      ExampleFeature)
      |  }
      |
      |}
      |
      """.trimMargin(),
    )
  }

  @Test
  fun `generated classes are in dexfiles`() {
    val integrationRoot = File("src/test/fixtures/codegen-fixture")
    integrationRoot.resolve("base/gradle.lockfile").writeText(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |com.squareup.okhttp3:okhttp:4.11.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio-jvm:3.2.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |com.squareup.okio:okio:3.2.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-common:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains.kotlin:kotlin-stdlib:$KOTLIN_VERSION=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |org.jetbrains:annotations:13.0=debugCompileClasspath,debugRuntimeClasspath,releaseCompileClasspath,releaseRuntimeClasspath
      |empty=
      """.trimMargin(),
    )

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
}
