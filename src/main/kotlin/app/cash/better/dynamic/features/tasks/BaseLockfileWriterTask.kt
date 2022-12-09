package app.cash.better.dynamic.features.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class BaseLockfileWriterTask : DefaultTask() {
  @get:InputFiles
  abstract var partialFeatureLockfiles: List<File>

  @get:InputFile
  abstract var partialBaseLockfile: File

  @get:OutputFile
  abstract var outputLockfile: File

  @TaskAction
  fun generateLockfile() {
    val mergedLockfileEntries = mergeLockfiles()

    outputLockfile.writeText(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |${mergedLockfileEntries.sorted().joinToString(separator = "\n")}
      |empty=
      """.trimMargin(),
    )
  }

  private fun mergeLockfiles(): List<LockfileEntry> {
    val resolvedEntries =
      partialBaseLockfile.readLockfileEntries().associateBy { it.artifact }
        .toMutableMap()

    val allEntries = partialFeatureLockfiles.flatMap { it.readLockfileEntries() }

    allEntries.forEach { entry ->
      val (artifact, version, configurations) = entry

      val existing = resolvedEntries[artifact]
      // We only care about the conflicting dependencies that actually exist in the base
      if (existing != null) {
        val resolvedVersion = maxOf(existing.version, version)
        val resolvedConfigurations = existing.configurations + configurations

        resolvedEntries[artifact] =
          LockfileEntry(artifact, resolvedVersion, resolvedConfigurations.toSortedSet())
      }
    }

    return resolvedEntries.values.toList()
  }

  private fun File.readLockfileEntries(): List<LockfileEntry> = readLines().map { entry ->
    val (dependency, configurationsString) = entry.split("=")
    val (group, artifact, version) = dependency.split(":")
    val configurations = configurationsString.split(", ").toSortedSet()

    LockfileEntry("$group:$artifact", version, configurations)
  }
}
