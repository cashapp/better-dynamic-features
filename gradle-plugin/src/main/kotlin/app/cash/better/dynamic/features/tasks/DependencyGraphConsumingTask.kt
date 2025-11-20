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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles

private val moshi = Moshi.Builder().build()
private val adapter = moshi.adapter(NodeList::class.java)

abstract class DependencyGraphConsumingTask : DefaultTask() {
  /**
   * Dependency graph files for each AGP variant of every feature module.
   */
  @get:InputFiles
  abstract val featureDependencyGraphFiles: ConfigurableFileCollection

  /**
   * Dependency graph files for every AGP variant of the base module.
   */
  @get:InputFiles
  abstract val baseDependencyGraphFiles: ConfigurableFileCollection

  protected fun baseGraph() = baseDependencyGraphFiles.map {
    adapter.fromJson(it.readText())?.nodes ?: error("Could not read graph from $it")
  }.flatten()

  protected fun featureGraphs() = featureDependencyGraphFiles.map {
    adapter.fromJson(it.readText())?.nodes ?: error("Could not read graph from $it")
  }
}
