package app.cash.better.dynamic.features.tasks

import java.util.SortedSet

data class LockfileEntry(
  val artifact: String,
  val version: String,
  val configurations: SortedSet<String>
) : Comparable<LockfileEntry> {
  override fun compareTo(other: LockfileEntry): Int = this.toString().compareTo(other.toString())

  override fun toString(): String = "$artifact:$version=${configurations.joinToString()}"
}
