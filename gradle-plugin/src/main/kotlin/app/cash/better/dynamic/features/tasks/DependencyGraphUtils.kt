package app.cash.better.dynamic.features.tasks

fun List<LockfileEntry>.toText(): String = """
      |# This is a Gradle generated file for dependency locking.
      |# Manual edits can break the build and are not advised.
      |# This file is expected to be part of source control.
      |${sorted().joinToString(separator = "\n")}
      |empty=
""".trimMargin()

fun mergeGraphs(base: List<Node>, others: List<List<Node>>): List<LockfileEntry> {
  val graphMap = mutableMapOf<String, Node>()
  base.walkAll { node ->
    when (val existing = graphMap[node.artifact]) {
      null -> graphMap[node.artifact] = node
      else -> {
        if (node.version > existing.version) {
          node.configurations += existing.configurations
          graphMap[node.artifact] = node
        } else {
          existing.configurations += node.configurations
        }
      }
    }
  }

  others.flatten().walkAll { node ->
    val existing = graphMap[node.artifact]
    // We only care about the conflicting dependencies that actually exist in the base
    if (existing != null && node.version > existing.version) {
      node.configurations += existing.configurations
      graphMap[node.artifact] = node
    }
  }

  return graphMap
    .filter { (_, entry) -> !entry.isProjectModule }
    .map { (_, entry) -> LockfileEntry(entry.artifact, entry.version, entry.configurations) }
}

private tailrec fun List<Node>.walkAll(callback: (Node) -> Unit) {
  forEach(callback)
  val nextLevel = flatMap { it.children }

  if (nextLevel.isNotEmpty()) {
    nextLevel.walkAll(callback)
  }
}
