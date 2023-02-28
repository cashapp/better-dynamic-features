package app.cash.better.dynamic.features.tasks

data class LockfileEntry(
  val artifact: String,
  val version: String,
  val configurations: Set<String>,
) : Comparable<LockfileEntry> {
  override fun compareTo(other: LockfileEntry): Int = this.toString().compareTo(other.toString())

  override fun toString(): String = "$artifact:$version=${configurations.sorted().joinToString(separator = ",")}"
}
