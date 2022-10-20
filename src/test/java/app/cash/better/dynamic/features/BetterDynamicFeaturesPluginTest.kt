// Copyright Square, Inc.
package app.cash.better.dynamic.features

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import java.io.File

class BetterDynamicFeaturesPluginTest {
  @Test fun `base and feature apks have different library versions`() {
    val integrationRoot = File("src/test/fixtures/different-versions")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", "checkDebugDependencyVersions")

    val result = gradleRunner.buildAndFail()
    assertThat(result.output).contains("com.squareup.okhttp3:okhttp, :base=4.9.3, :feature=5.0.0-alpha.2 -> Use 5.0.0-alpha.2")
  }

  @Test fun `base and feature apks have same library versions`() {
    val integrationRoot = File("src/test/fixtures/same-versions")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", "checkDebugDependencyVersions")

    val result = gradleRunner.build()
    assertThat(result.output).contains("BUILD SUCCESSFUL")
  }
}
