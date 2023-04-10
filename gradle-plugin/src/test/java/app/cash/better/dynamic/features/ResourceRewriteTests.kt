package app.cash.better.dynamic.features

import com.google.common.truth.Truth.assertThat
import com.reandroid.apk.ApkModule
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ResourceRewriteTests {

  @get:Rule
  val jsonDumpDir = TemporaryFolder()

  @Test
  fun `xml binary rewrite works for attr`() {
    val integrationRoot = File("src/test/fixtures/attr-rewrite-binary")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withFreshLockfile(module = "app")
      .withArguments(":feature:assembleDebug")

    gradleRunner.build()

    val mappingFile =
      integrationRoot.resolve("app/build/betterDynamicFeatures/resource-mapping/debug/mapping.txt")

    // Validate that the affected attr resource was identified in the build process
    assertThat(mappingFile.readLines()).contains("attr/specialText 2113994752 2130904007")

    val apkOutput = integrationRoot.resolve("feature/build/outputs/apk/debug/feature-debug.apk")
    val module = ApkModule.loadApkFile(apkOutput)

    // Validate that the modified resource file in the assembled APK contains the correct attribute
    val jsonRoot = jsonDumpDir.newFolder()
    module.listResFiles()
      .first { it.filePath == "res/layout/fragment_feature.xml" }
      .dumpToJson(jsonRoot)

    assertThat(jsonRoot.resolve("res/layout/fragment_feature.xml.json").readText())
      .contains(
        """|      "name": "specialText",
           |      "id": 2130904007,
        """.trimMargin(),
      )
  }
}
