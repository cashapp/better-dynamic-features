// Copyright Square, Inc.
package app.cash.better.dynamic.features.tasks

import com.squareup.moshi.JsonClass
import org.gradle.util.internal.VersionNumber

@JsonClass(generateAdapter = true)
data class Node(
  val artifact: String,
  val version: Version,
  val variants: Set<String>,
  val children: List<Node>,
  val isProjectModule: Boolean,
  val type: DependencyType,
)

enum class DependencyType {
  Runtime,
  Compile;
}

@JsonClass(generateAdapter = true)
data class NodeList(val nodes: List<Node>)

@JsonClass(generateAdapter = true)
@JvmInline
value class Version(val string: String) : Comparable<Version> {
  override fun compareTo(other: Version): Int =
    VersionNumber.parse(string).compareTo(VersionNumber.parse(other.string))

  override fun toString(): String = string
}
