// Copyright Square, Inc.
package app.cash.better.dynamic.features.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class PartialLockfileWriterTask : DefaultTask() {
  /**
   * This is only used to trick Gradle into recognizing that this task is no longer up-to-date
   * For actual processing, [resolvedLockfileEntries] is used.
   */
  @get:InputFiles
  abstract val dependencyFileCollection: ConfigurableFileCollection

  private lateinit var resolvedLockfileEntries: Provider<List<LockfileEntry>>

  fun setResolvedLockfileEntriesProvider(provider: Provider<Map<String, ResolvedConfiguration>>) {
    resolvedLockfileEntries = provider.map { configurationMap ->
      configurationMap.flatMap { (configuration, resolved) ->
        buildSet { resolved.firstLevelModuleDependencies.walkDependencyTree { add(it) } }
          .filter { it.moduleVersion != "unspecified" }
          .map {
            LockfileEntry(
              "${it.moduleGroup}:${it.moduleName}",
              it.moduleVersion,
              sortedSetOf("${configuration}RuntimeClasspath"),
            )
          }
      }
    }
  }

  @get:Input
  abstract var projectName: String

  @get:Input
  abstract var configurationNames: List<String>

  @get:OutputFile
  abstract var partialLockFile: File

  @TaskAction fun printList() {
    val dependencies = resolvedLockfileEntries.get()
      .groupBy { entry -> "${entry.artifact}:${entry.version}" }
      .map { (_, entries) -> entries.reduce { acc, lockfileEntry -> acc.copy(configurations = (acc.configurations + lockfileEntry.configurations).toSortedSet()) } }

    partialLockFile.writeText(dependencies.sorted().joinToString(separator = "\n"))
  }

  private tailrec fun Set<ResolvedDependency>.walkDependencyTree(callback: (ResolvedDependency) -> Unit) {
    onEach(callback)
    val nextSet = flatMapTo(mutableSetOf<ResolvedDependency>()) { it.children }

    if (nextSet.isNotEmpty()) {
      nextSet.walkDependencyTree(callback)
    }
  }
}
