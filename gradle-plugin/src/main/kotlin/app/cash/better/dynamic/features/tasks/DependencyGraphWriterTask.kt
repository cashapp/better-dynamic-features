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

  fun setResolvedLockfileEntriesProvider(provider: Provider<ResolvedComponentResultPair>, variant: String) {
    graph = provider.map { (runtime, compile) ->
      val runtimeNodes = if (runtime.variants.isEmpty()) {
        emptyList()
      } else {
        buildDependencyGraph(
          runtime.getDependenciesForVariant(runtime.variants.first()),
          variant,
          mutableSetOf(),
          type = DependencyType.Runtime,
        )
      }
      val compileNodes = if (compile.variants.isEmpty()) {
        emptyList()
      } else {
        buildDependencyGraph(
          compile.getDependenciesForVariant(compile.variants.first()),
          variant,
          mutableSetOf(),
          type = DependencyType.Compile,
        )
      }

      runtimeNodes + compileNodes
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

  private fun buildDependencyGraph(topLevel: List<DependencyResult>, variant: String, visited: MutableSet<String>, type: DependencyType): List<Node> = topLevel
    .asSequence()
    .filterIsInstance<ResolvedDependencyResult>()
    .filter { !it.isConstraint && it.key !in visited }
    .onEach { visited += it.key }
    .map {
      val info = it.selected.moduleVersion!!
      Node(
        "${info.group}:${info.name}",
        Version(info.version),
        mutableSetOf(variant),
        children = buildDependencyGraph(it.selected.getDependenciesForVariant(it.resolvedVariant), variant, visited, type),
        isProjectModule = it.resolvedVariant.owner is ProjectComponentIdentifier,
        type = type,
      )
    }
    .toList()

  data class ResolvedComponentResultPair(val runtime: ResolvedComponentResult, val compile: ResolvedComponentResult)
}
