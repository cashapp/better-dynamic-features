// Copyright Square, Inc.
package app.cash.better.dynamic.features.tasks

import com.squareup.moshi.Moshi
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class DependencyGraphWriterTask : DefaultTask() {
  /**
   * This is only used to trick Gradle into recognizing that this task is no longer up-to-date
   * For actual processing, [graph] is used.
   */
  @get:InputFiles
  abstract val dependencyFileCollection: ConfigurableFileCollection

  private lateinit var graph: Provider<List<Node>>

  fun setResolvedLockfileEntriesProvider(provider: Provider<ResolvedComponentResult>, variant: String) {
    graph = provider.map { resolution ->
      buildDependencyGraph(resolution.getDependenciesForVariant(resolution.variants.first()), "${variant}RuntimeClasspath", mutableSetOf())
    }
  }

  @get:OutputFile
  abstract val partialLockFile: RegularFileProperty

  @TaskAction
  fun printList() {
    val moshi = Moshi.Builder().build()
    partialLockFile.asFile.get().writeText(moshi.adapter(NodeList::class.java).toJson(NodeList(graph.get())))
  }

  private val ResolvedDependencyResult.key: String
    get() = selected.moduleVersion?.let { info -> "${info.group}:${info.name}" } ?: ""

  @Suppress("UnstableApiUsage")
  private fun buildDependencyGraph(topLevel: List<DependencyResult>, configuration: String, visited: MutableSet<String>): List<Node> =
    topLevel
      .asSequence()
      .filterIsInstance<ResolvedDependencyResult>()
      .filter { it.key !in visited }
      .onEach { visited += it.key }
      .map {
        val info = it.selected.moduleVersion!!
        Node(
          "${info.group}:${info.name}",
          info.version,
          mutableSetOf(configuration),
          children = buildDependencyGraph(it.selected.getDependenciesForVariant(it.resolvedVariant), configuration, visited),
          isProjectModule = it.resolvedVariant.owner is ProjectComponentIdentifier,
        )
      }
      .toList()
}
