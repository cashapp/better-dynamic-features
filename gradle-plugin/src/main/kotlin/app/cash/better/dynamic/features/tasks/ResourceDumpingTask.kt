// Copyright Square, Inc.
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
