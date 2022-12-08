// Copyright Square, Inc.
package app.cash.better.dynamic.features.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class PartialLockfileWriterTask : DefaultTask() {
  /**
   * This is only used to trick Gradle into recognizing that this task is no longer up-to-date
   * For actual processing, [dependencyArtifacts] is used.
   */
  @get:InputFiles
  abstract val dependencyFileCollection: ConfigurableFileCollection

  private lateinit var dependencyArtifacts: Map<String, ArtifactCollection>

  fun setDependencyArtifacts(collection: Map<String, ArtifactCollection>) {
    this.dependencyArtifacts = collection
  }

  @get:Input
  abstract var projectName: String

  @get:Input
  abstract var configurationNames: List<String>

  @get:OutputFile
  abstract var partialLockFile: File

  @TaskAction fun printList() {
    val dependencies = dependencyArtifacts.flatMap { (configuration, value) ->
      value.artifacts
        .mapNotNull {
          val capability = it.variant.capabilities.firstOrNull() ?: return@mapNotNull null

          LockfileEntry(
            "${capability.group}:${capability.name}",
            capability.version!!,
            sortedSetOf("${configuration}RuntimeClasspath"),
          )
        }
    }
      .groupBy { entry -> "${entry.artifact}:${entry.version}" }
      .map { (_, entries) -> entries.reduce { acc, lockfileEntry -> acc.copy(configurations = (acc.configurations + lockfileEntry.configurations).toSortedSet()) } }

    partialLockFile.writeText(dependencies.sorted().joinToString(separator = "\n"))
  }
}
