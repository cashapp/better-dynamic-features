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
      "${entry.artifact}:${entry.version}" to LockfileEntry(
        entry.artifact,
        entry.version,
        entry.configurationNames,
      )
    }
    .toMap()
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

    if (entry.version == compileEntry.version) {
      mergedMap[artifact] =
        entry.copy(configurations = entry.configurations + compileEntry.configurations)
    } else {
      mergedMap[artifact] = compileEntry
    }
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
