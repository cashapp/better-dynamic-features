package app.cash.better.dynamic.features

import com.google.common.truth.Truth.assertThat
import com.reandroid.apk.ApkModule
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.bufferedReader

class ResourceRewriteTests {

  @get:Rule
  val jsonDumpDir = TemporaryFolder()

  @get:Rule
  val dexDir = TemporaryFolder()

  @Test
  fun `xml binary rewrite works`() {
    val integrationRoot = File("src/test/fixtures/attr-rewrite-binary")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withFreshLockfile(module = "app")
      .withArguments(":feature:assembleDebug")

    gradleRunner.build()

    val mappingFile =
      integrationRoot.resolve("app/build/betterDynamicFeatures/resource-mapping/debug/mapping.txt")
    val mappingContent = mappingFile.readText()

    // Validate that the affected attr resource was identified in the build process
    assertThat(mappingContent).containsMatch(ATTR_REGEX)
    assertThat(mappingContent).containsMatch(ID_REGEX)

    // The generated IDs aren't guaranteed to be stable between different machines, so we have to read the real value like this
    val mappedAttr = Regex(ATTR_REGEX).find(mappingContent)!!.value.split(" ").last()
    val mappedId = Regex(ID_REGEX).find(mappingContent)!!.value.split(" ").last()

    val apkOutput = integrationRoot.resolve("feature/build/outputs/apk/debug/feature-debug.apk")
    val module = ApkModule.loadApkFile(apkOutput)

    // Validate that the modified resource file in the assembled APK contains the correct attribute
    val jsonRoot = jsonDumpDir.newFolder()
    module.listResFiles()
      .first { it.filePath == "res/layout/fragment_feature.xml" }
      .dumpToJson(jsonRoot)

    val jsonContent = jsonRoot.resolve("res/layout/fragment_feature.xml.json").readText()
    assertThat(jsonContent)
      .contains(
        """|      "name": "specialText",
           |      "id": $mappedAttr,
        """.trimMargin(),
      )

    assertThat(jsonContent)
      .contains(
        """|      "value_type": "REFERENCE",
           |      "data": $mappedId
        """.trimMargin(),
      )
  }

  @Test
  fun `xml proto rewrite works`() {
    val integrationRoot = File("src/test/fixtures/attr-rewrite-binary")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withFreshLockfile(module = "app")
      .withArguments(":app:packageDebugUniversalApk")

    gradleRunner.build()

    val mappingFile =
      integrationRoot.resolve("app/build/betterDynamicFeatures/resource-mapping/debug/mapping.txt")
    val mappingContent = mappingFile.readText()

    // Validate that the affected attr resource was identified in the build process
    assertThat(mappingContent).containsMatch(ATTR_REGEX)
    assertThat(mappingContent).containsMatch(ID_REGEX)

    // The generated IDs aren't guaranteed to be stable between different machines, so we have to read the real value like this
    val mappedAttr = Regex(ATTR_REGEX).find(mappingContent)!!.value.split(" ").last()
    val mappedId = Regex(ID_REGEX).find(mappingContent)!!.value.split(" ").last()

    val apkOutput = integrationRoot.resolve("app/build/outputs/apk_from_bundle/debug/app-debug-universal.apk")
    val module = ApkModule.loadApkFile(apkOutput)

    // Validate that the modified resource file in the assembled APK contains the correct attribute
    val jsonRoot = jsonDumpDir.newFolder()
    module.listResFiles()
      .first { it.filePath == "res/layout/fragment_feature.xml" }
      .dumpToJson(jsonRoot)

    val jsonContent = jsonRoot.resolve("res/layout/fragment_feature.xml.json").readText()
    assertThat(jsonContent)
      .contains(
        """|      "name": "specialText",
           |      "id": $mappedAttr,
        """.trimMargin(),
      )

    assertThat(jsonContent)
      .contains(
        """|      "value_type": "REFERENCE",
           |      "data": $mappedId
        """.trimMargin(),
      )
  }

  @Test
  fun `R class rewrite works`() {
    val integrationRoot = File("src/test/fixtures/attr-rewrite-binary")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withFreshLockfile(module = "app")
      .withArguments(":app:packageDebugUniversalApk")

    gradleRunner.build()

    val mappingFile =
      integrationRoot.resolve("app/build/betterDynamicFeatures/resource-mapping/debug/mapping.txt")
    val mappingContent = mappingFile.readText()

    // Validate that the affected attr resource was identified in the build process
    assertThat(mappingContent).containsMatch(ATTR_REGEX)
    assertThat(mappingContent).containsMatch(ID_REGEX)

    // The generated IDs aren't guaranteed to be stable between different machines, so we have to read the real value like this
    val mappedAttr = Regex(ATTR_REGEX).find(mappingContent)!!.value.split(" ").last()
    val mappedId = Regex(ID_REGEX).find(mappingContent)!!.value.split(" ").last()

    val apkOutput = integrationRoot.resolve("app/build/outputs/apk_from_bundle/debug/app-debug-universal.apk")
    val dexContent = dexdump(apkOutput)

    assertThat(dexContent.bufferedReader().lineSequence().asIterable())
      .containsAtLeast(
        """      name          : 'specialText'""",
        """      type          : 'I'""",
        """      access        : 0x0019 (PUBLIC STATIC FINAL)""",
        """      value         : $mappedAttr""",
      ).inOrder()

    assertThat(dexContent.bufferedReader().lineSequence().asIterable()).containsAtLeast(
      """      name          : 'button_first'""",
      """      type          : 'I'""",
      """      access        : 0x0019 (PUBLIC STATIC FINAL)""",
      """      value         : $mappedId""",
    ).inOrder()
  }

  @Test
  fun `styleable attribute mapping works`() {
    val integrationRoot = File("src/test/fixtures/attr-rewrite-binary")

    val gradleRunner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .cleaned()
      .withFreshLockfile(module = "app")
      .withArguments(":app:packageDebugUniversalApk")

    gradleRunner.build()

    val mappingFile =
      integrationRoot.resolve("app/build/betterDynamicFeatures/resource-mapping/debug/mapping.txt")
    val mappingContent = mappingFile.readText()

    // Validate that the affected attr resource was identified in the build process
    assertThat(mappingContent).containsMatch(ATTR_REGEX)

    // The generated IDs aren't guaranteed to be stable between different machines, so we have to read the real value like this
    val mappedAttr = Regex(ATTR_REGEX).find(mappingContent)!!.value.split(" ").last()

    val styleableFile = integrationRoot.resolve("feature/build/betterDynamicFeatures/resource-mapping/debug/styleables.txt")
    val styleablesContent = styleableFile.readText()

    assertThat(styleablesContent).contains("AnotherView $mappedAttr")
  }

  private companion object {
    const val ATTR_REGEX = """attr\/specialText \d+ \d+"""
    const val ID_REGEX = """id\/button_first \d+ \d+"""
  }

  private fun dexdump(file: File): Path {
    val outputPath = Files.createTempFile("dexdump", "txt")

    val buildToolsVersion = Class.forName("com.android.SdkConstants").getDeclaredField("CURRENT_BUILD_TOOLS_VERSION").get(null)
    val dexdumpPath = "${System.getenv("ANDROID_SDK_ROOT")}/build-tools/$buildToolsVersion/dexdump"
    check(Files.exists(Path(dexdumpPath))) { "Could not find 'dexdump' binary. Checked $dexdumpPath" }

    val process = ProcessBuilder()
      .apply {
        command(dexdumpPath, file.absolutePath)
        redirectOutput(outputPath.toFile())
      }
      .start()

    check(process.waitFor() == 0) { "dexdump failed" }
    return outputPath
  }
}
