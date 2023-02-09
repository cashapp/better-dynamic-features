// Copyright Square, Inc.
package app.cash.better.dynamic.features.tasks

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Node(val artifact: String, val version: String, val configurations: MutableSet<String>, val children: List<Node>, val isProjectModule: Boolean)

@JsonClass(generateAdapter = true)
data class NodeList(val nodes: List<Node>)
