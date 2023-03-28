// Copyright Square, Inc.
package app.cash.better.dynamic.features

import app.cash.better.dynamic.features.tasks.CheckExternalResourcesTask
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File

class ResourceScanningTests {

  @Test fun `style pattern regex matches xml correctly`() {
    val fakeXml = """
      <activity android:name="MyActivity" android:theme="@style/MyActivityTheme" />
      <activity android:theme"@style/MyOtherTheme" android:name="WoahItInRevere" />
    """.trimIndent()

    val matches = CheckExternalResourcesTask.STYLE_PATTERN.findAll(fakeXml).map { it.value }.toList()
    assertThat(matches).contains("@style/MyActivityTheme")
    assertThat(matches).contains("@style/MyOtherTheme")
  }

  @Test fun `theme defined in dynamic feature and declared as external succeeds`() {
    val integrationRoot = File("src/test/fixtures/resources-correct-declaration")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withFreshLockfile()
      .withArguments("clean", ":base:assembleDebug")

    val result = gradleRunner.build()
    assertThat(result.output).contains("BUILD SUCCESSFUL")
    println(result.output)
    assertThat(result.task(":base:generateDebugExternalResources")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(result.task(":base:checkDebugExternalResources")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test fun `theme defined in dynamic feature but no declared fails`() {
    val integrationRoot = File("src/test/fixtures/resources-missing-declaration")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withFreshLockfile()
      .withArguments("clean", ":base:assembleDebug")

    val result = gradleRunner.buildAndFail()
    assertThat(result.output).contains("Some resources are defined externally but not declared as external.")
    assertThat(result.output).contains("Add these to your gradle configuration:")
    assertThat(result.output).contains(
      """
        |  betterDynamicFeatures {
        |    externalResources {
        |      style(MyTheme)
        |    }
        |  }
      """.trimMargin(),
    )
  }

  @Test fun `theme defined in library included in base and declared as external fails`() {
    val integrationRoot = File("src/test/fixtures/resources-overwriting")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withFreshLockfile()
      .withArguments("clean", ":base:assembleDebug")

    val result = gradleRunner.buildAndFail()
    assertThat(result.output).contains("Some external resource declarations are overwriting actual resource definitions.")
    assertThat(result.output).contains("Remove these from your gradle configuration:")
    assertThat(result.output).contains("MyTheme")
  }

  @Test fun `theme defined in library included in base builds normally`() {
    val integrationRoot = File("src/test/fixtures/resources-normal")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withFreshLockfile()
      .withArguments("clean", ":base:assembleDebug")

    val result = gradleRunner.build()
    assertThat(result.output).contains("BUILD SUCCESSFUL")
    assertThat(result.task(":base:generateDebugExternalResources")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(result.task(":base:checkDebugExternalResources")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test fun `theme defined in base doesn't report missing external`() {
    val integrationRoot = File("src/test/fixtures/resources-in-base")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withFreshLockfile()
      .withArguments("clean", ":base:assembleDebug")

    val result = gradleRunner.build()
    println(result.output)
  }
}
