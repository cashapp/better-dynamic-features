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
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
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
        task.baseModuleDependencies = project.depsListFile(variant.name)
        task.dynamicModuleDependencies = dynamicModules.map { project.project(it).depsListFile(variant.name) }

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
  ) { task ->
    val collection = configurations.getByName("${variant.name}RuntimeClasspath").incoming
      .artifactView { config ->
        config.attributes { container ->
          container.attribute(
            AndroidArtifacts.ARTIFACT_TYPE,
            AndroidArtifacts.ArtifactType.AAR_OR_JAR.type
          )
        }

        // Look only for external dependencies
        config.componentFilter { it !is ProjectComponentIdentifier }
      }
    task.dependencyFileCollection.setFrom(collection.artifacts.artifactFiles)
    task.setDependencyArtifacts(collection.artifacts)
    task.projectName = this.name
    task.listFile = this.depsListFile(variant.name)
  }

  private fun Project.depsListFile(variant: String): File = buildDir.resolve("tmp/bfd/$variant/deps.txt")
}
