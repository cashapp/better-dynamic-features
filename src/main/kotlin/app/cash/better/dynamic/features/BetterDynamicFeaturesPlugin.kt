// Copyright Square, Inc.
package app.cash.better.dynamic.features

import app.cash.better.dynamic.features.tasks.DependenciesVersionCheckTask
import app.cash.better.dynamic.features.tasks.ListDependenciesTask
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import java.io.File

@Suppress("UnstableApiUsage")
class BetterDynamicFeaturesPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    check(project.plugins.hasPlugin("com.android.application")) {
      "Plugin 'com.android.application' must also be applied before this plugin"
    }
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    // Callback for finalizeDsl is called before onVariants, so this is "safe"
    val dynamicModules = mutableSetOf<String>()
    androidComponents.finalizeDsl { extension ->
      check(extension is ApplicationExtension)
      dynamicModules.addAll(extension.dynamicFeatures)
    }

    androidComponents.onVariants { variant ->
      val baseTask = project.registerListingTask(variant)
      val featureTasks = project.setupBetterDynamicFeatures(dynamicModules, variant)

      project.tasks.register(
        "check${variant.name.capitalized()}DependencyVersions",
        DependenciesVersionCheckTask::class.java
      ) { task ->
        task.baseModuleDependencies = project.depsListFile()
        task.dynamicModuleDependencies = dynamicModules.map { project.project(it).depsListFile() }

        task.dependsOn(baseTask)
        task.dependsOn(featureTasks)
      }
    }
  }

  private fun Project.setupBetterDynamicFeatures(
    featureModules: Set<String>,
    variant: Variant
  ): List<TaskProvider<ListDependenciesTask>> =
    featureModules.map { project(it) }.map { project ->
      // Register tasks
      project.registerListingTask(variant)
    }

  private fun Project.registerListingTask(variant: Variant): TaskProvider<ListDependenciesTask> = tasks.register(
    "list${variant.name.capitalized()}Dependencies",
    ListDependenciesTask::class.java
  ) {
    val collection = configurations.getByName("${variant.name}RuntimeClasspath").incoming
      .artifactView { config ->
        config.attributes { container ->
          container.attribute(
            AndroidArtifacts.ARTIFACT_TYPE,
            AndroidArtifacts.ArtifactType.AAR_OR_JAR.type
          )
        }
      }
      .artifacts
    it.setDependencyArtifacts(collection)
    it.projectName = this.name
    it.listFile = this.depsListFile()
  }

  private fun Project.depsListFile(): File = buildDir.resolve("tmp/bfd/deps.txt")
}
