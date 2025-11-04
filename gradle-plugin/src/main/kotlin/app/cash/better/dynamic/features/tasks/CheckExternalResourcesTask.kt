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
package app.cash.better.dynamic.features.tasks

import app.cash.better.dynamic.features.BetterDynamicFeaturesExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * This task reports errors with externally-defined resources that are being indirectly referenced
 * by the base module in a dynamic feature project setup.
 *
 * If a style is defined and included in the manifest of a dynamic feature module, it will be merged
 * into the base module's manifest and cause a linker error. This task reports this issue and recommends
 * adding it to the externally defined resource declarations in the gradle config. (See [BetterDynamicFeaturesExtension])
 *
 * If a resource has been declared in the gradle config, but that resource is actually being
 * included in the base module, this could cause the actual resource to be overwritten and this task
 * will report this error and recommend removing the external resource declaration.
 */
abstract class CheckExternalResourcesTask : DefaultTask() {

  @get:InputFiles
  abstract val incomingResourcesCollection: ConfigurableFileCollection

  private lateinit var incomingResources: Provider<ArtifactCollection>
  fun setIncomingResources(collection: Provider<ArtifactCollection>) {
    this.incomingResources = collection
  }

  @get:InputFiles
  abstract val manifestFile: RegularFileProperty

  @get:Input
  abstract val externalDeclarations: ListProperty<String>

  @get:InputFiles
  @get:Optional
  abstract val localResources: ConfigurableFileCollection

  @get:OutputFile
  abstract val result: RegularFileProperty

  @TaskAction
  fun generateExternalDeclarations() {
    val localResourceModule = computeLocalResources()

    val manifestContents = manifestFile.get().asFile.readText()

    // Parsing XML? Haha. No.
    val styleMatches = STYLE_PATTERN.findAll(manifestContents)

    val resourceModules = incomingResources.get().artifactFiles.files
      .map { file ->
        val allLines = file.readLines()
        val namespace = allLines.first()

        val resources = allLines.drop(1)
          .map {
            val (type, name) = it.split(" ")
            type to name
          }
          .groupBy { (type) -> type }
          .mapValues { (_, values) -> values.mapTo(mutableSetOf()) { it.second } }

        ResourceModule(namespace, resources)
      }
      .let { if (localResourceModule == null) it else it + localResourceModule }

    val manifestStyles = styleMatches.map { match -> transformStyleReference(match.value) }.toSet()

    try {
      checkMissingExternals(resourceModules, manifestStyles)
      checkOverwrittenStyles(resourceModules)
      result.get().asFile.writeText("success")
    } catch (e: IllegalStateException) {
      result.get().asFile.writeText("failed")
      throw e
    }
  }

  private fun checkMissingExternals(resourceModules: List<ResourceModule>, styles: Set<String>) {
    // Incoming style resources that are referenced by the manifest
    val includedStyles = resourceModules.flatMap { (_, resources) ->
      val moduleStyles = resources["style"] ?: emptySet()
      styles.intersect(moduleStyles)
    }.toSet()

    // Styles referenced in the manifest minus the ones that are present in the incoming resources
    val externallyDefined = styles - includedStyles

    val missing = externallyDefined - externalDeclarations.get().map(::transformStyleReference).toSet()
    check(missing.isEmpty()) {
      """
        |Some resources are defined externally but not declared as external.
        |Add these to your gradle configuration:
        |
        |betterDynamicFeatures {
        |  externalResources {
        |${missing.joinToString(separator = "\n") { "    style(\"$it\")" }}
        |  }
        |}
      """.trimMargin()
    }
  }

  private fun checkOverwrittenStyles(resourceModules: List<ResourceModule>) {
    val externalsSet = externalDeclarations.get().toSet()

    val overwritten = resourceModules.flatMap { (_, resources) ->
      val moduleStyles = resources["style"] ?: emptySet()
      externalsSet.intersect(moduleStyles)
    }

    check(overwritten.isEmpty()) {
      """
        |Some external resource declarations are overwriting actual resource definitions.
        |Remove these from your gradle configuration:
        |
        |${overwritten.joinToString(separator = "\n")}
      """.trimMargin()
    }
  }

  private fun computeLocalResources(): ResourceModule? {
    val localStyles = localResources.files
      .filter {
        // Exclude the resources we generate
        "betterDynamicFeatures" !in it.path && it.exists()
      }
      .flatMap { directory -> directory.walk() } // Grab every .xml file from each res/ directory
      .filter { it.extension == "xml" }
      .flatMap { file ->
        // We only care about searching for <style> entries right now
        val stylePattern = Regex("""<style(.*?)>""")
        stylePattern.findAll(file.readText()).map { it.value }
      }
      .mapNotNull { style ->
        val namePattern = Regex("""name="(.*?)"""")
        namePattern.find(style)?.value?.removePrefix("name=\"")?.removeSuffix("\"")
      }
      .map(::transformStyleReference)

    return ResourceModule("_local", mapOf("style" to localStyles.toSet()))
  }

  /**
   * Transforms a style reference, e.g. `@style/My.Theme.Identifier`, into a compiled resource name e.g. `My_Theme_Identifier`.
   */
  private fun transformStyleReference(ref: String): String = ref.replace(Regex("""@(android:)?style\/"""), "").replace(".", "_")

  private data class ResourceModule(val namespace: String, val resources: Map<String, Set<String>>)

  companion object {
    val STYLE_PATTERN = Regex("""@(android:)?style\/.+?(?=")""")
  }
}
