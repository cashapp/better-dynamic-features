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
import com.reandroid.apk.ApkModule
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
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

    assertThat(dexContent.bufferedReader().lineSequence())
      .containsInConsecutiveOrder(
        """      name          : 'specialText'""",
        """      type          : 'I'""",
        """      access        : 0x0009 (PUBLIC STATIC)""",
        """      value         : $mappedAttr""",
      )

    assertThat(dexContent.bufferedReader().lineSequence())
      .containsInConsecutiveOrder(
        """      name          : 'button_first'""",
        """      type          : 'I'""",
        """      access        : 0x0009 (PUBLIC STATIC)""",
        """      value         : $mappedId""",
      )
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
}
