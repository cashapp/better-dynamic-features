package app.cash.better.dynamic.features.tasks

import com.squareup.moshi.Moshi
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
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
  abstract val outputLockfile: RegularFileProperty

  @TaskAction
  fun generateLockfile() {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(NodeList::class.java)

    val baseGraph = adapter.fromJson(partialBaseLockfile.readText()) ?: error("Could not read base graph")
    val otherGraphs = partialFeatureLockfiles.map { adapter.fromJson(it.readText())?.nodes ?: error("Could not read graph from $it") }

    val entries = mergeGraphs(baseGraph.nodes, otherGraphs)

    outputLockfile.get().asFile.writeText(
      """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |${entries.sorted().joinToString(separator = "\n")}
      |empty=
      """.trimMargin(),
    )
  }

  private fun mergeGraphs(base: List<Node>, others: List<List<Node>>): List<LockfileEntry> {
    val graphMap = mutableMapOf<String, Node>()
    base.walkAll { node ->
      when (val existing = graphMap[node.artifact]) {
        null -> graphMap[node.artifact] = node
        else -> {
          if (node.version > existing.version) {
            node.walk { it.configurations += existing.configurations }
            graphMap[node.artifact] = node
          } else {
            existing.walk { it.configurations += node.configurations }
          }
        }
      }
    }

    others.flatten().walkAll { node ->
      val existing = graphMap[node.artifact]
      // We only care about the conflicting dependencies that actually exist in the base
      if (existing != null && node.version > existing.version) {
        node.walk { it.configurations += existing.configurations }
        graphMap[node.artifact] = node
      }
    }

    return graphMap.map { (_, entry) -> LockfileEntry(entry.artifact, entry.version, entry.configurations) }
  }

  private fun Node.walk(callback: (Node) -> Unit) {
    listOf(this).walkAll(callback)
  }

  private tailrec fun List<Node>.walkAll(callback: (Node) -> Unit) {
    forEach(callback)
    val nextLevel = flatMap { it.children }

    if (nextLevel.isNotEmpty()) {
      nextLevel.walkAll(callback)
    }
  }
}
