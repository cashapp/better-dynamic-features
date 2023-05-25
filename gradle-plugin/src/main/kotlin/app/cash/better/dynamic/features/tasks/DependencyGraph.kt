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
