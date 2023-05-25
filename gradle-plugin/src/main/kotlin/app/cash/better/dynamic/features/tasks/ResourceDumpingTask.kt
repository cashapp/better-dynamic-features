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

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.PublishArtifactSet
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

abstract class ResourceDumpingTask : DefaultTask() {

  private lateinit var dependencyArtifacts: ArtifactCollection

  fun setDependencyArtifacts(collection: ArtifactCollection) {
    this.dependencyArtifacts = collection
  }

  private lateinit var publishedArtifacts: PublishArtifactSet
  fun setPublishArtifacts(artifacts: PublishArtifactSet) {
    this.publishedArtifacts = artifacts
  }

  @get:[Optional InputFile]
  abstract val localArtifacts: RegularFileProperty

  @TaskAction
  fun dump() {
    println("Dump: ${dependencyArtifacts.artifacts.size}")
    dependencyArtifacts.artifactFiles.forEach {
      println(it)
    }
    println("Published: ${publishedArtifacts.size}")
    publishedArtifacts.files.forEach {
      println(it)
    }
    println(localArtifacts.orNull?.asFile)
  }
}
