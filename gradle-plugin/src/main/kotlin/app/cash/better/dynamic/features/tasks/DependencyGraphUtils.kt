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

import app.cash.better.dynamic.features.tasks.DependencyType.Compile
import app.cash.better.dynamic.features.tasks.DependencyType.Runtime

fun List<LockfileEntry>.toText(): String = """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |${sorted().joinToString(separator = "\n")}
      |empty=
""".trimMargin()

private val Node.configurationNames
  get() = variants.mapTo(mutableSetOf()) {
    when (type) {
      Compile -> "${it}CompileClasspath"
      Runtime -> "${it}RuntimeClasspath"
    }
  }

private fun mergeSingleTypeGraphs(
  base: List<Node>,
  others: List<List<Node>>,
): Map<String, LockfileEntry> {
  val graphMap = mutableMapOf<String, Node>()

  fun registerNodes(nodes: List<Node>) {
    nodes.walkAll { node ->
      when (val existing = graphMap[node.artifact]) {
        null -> graphMap[node.artifact] = node
        else -> {
          if (node.version > existing.version) {
            graphMap[node.artifact] = node.copy(variants = node.variants + existing.variants)
          } else {
            graphMap[existing.artifact] = existing.copy(variants = existing.variants + node.variants)
          }
        }
      }
    }
  }

  // Register the initial set of dependencies
  registerNodes(base)

  others.flatten().walkAll { node ->
    val existing = graphMap[node.artifact]
    // We only care about the conflicting dependencies that actually exist in the base
    if (existing != null && node.variants.single() in existing.variants) {
      if (node.version > existing.version) {
        graphMap[node.artifact] = node.copy(variants = node.variants + existing.variants)
      } else {
        graphMap[existing.artifact] = existing.copy(variants = existing.variants + node.variants)
      }

      // Register any new transitive dependencies
      registerNodes(node.children)
    }
  }

  return graphMap
    .filter { (_, entry) -> !entry.isProjectModule }
    .map { (_, entry) ->
      LockfileEntry(
        entry.artifact,
        entry.version.toString(),
        entry.configurationNames,
      )
    }
    .associateBy { it.artifact }
}

fun mergeGraphs(base: List<Node>, others: List<List<Node>>): List<LockfileEntry> {
  val runtimeMap = mergeSingleTypeGraphs(
    base.filter { it.type == Runtime },
    others.map { list -> list.filter { it.type == Runtime } },
  )
  val compileMap = mergeSingleTypeGraphs(
    base.filter { it.type == Compile },
    others.map { list -> list.filter { it.type == Compile } },
  ).toMutableMap()

  // Merge the runtime and compile graphs
  val mergedMap = mutableMapOf<String, LockfileEntry>()
  runtimeMap.forEach { (artifact, entry) ->
    mergedMap[artifact] = entry
    val compileEntry = compileMap[artifact] ?: return@forEach

    mergedMap[artifact] = entry.copy(
      version = maxOf(entry.version, compileEntry.version),
      configurations = entry.configurations + compileEntry.configurations,
    )

    compileMap.remove(artifact)
  }
  compileMap.forEach { (artifact, entry) ->
    mergedMap[artifact] = entry
  }

  return mergedMap.values.toList()
}

private tailrec fun List<Node>.walkAll(callback: (Node) -> Unit) {
  forEach(callback)
  val nextLevel = flatMap { it.children }

  if (nextLevel.isNotEmpty()) {
    nextLevel.walkAll(callback)
  }
}
