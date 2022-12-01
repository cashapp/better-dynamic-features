// Copyright Square, Inc.
package app.cash.better.dynamic.features

import app.cash.better.dynamic.features.tasks.BaseLockfileWriterTask
import app.cash.better.dynamic.features.tasks.PartialLockfileWriterTask
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import java.io.File

@Suppress("UnstableApiUsage")
class BetterDynamicFeaturesPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    check(project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.dynamic-feature")) {
      "Plugin 'com.android.application' or 'com.android.dynamic-feature' must also be applied before this plugin"
    }

    if (project.plugins.hasPlugin("com.android.application")) {
      applyToApplication(project)
    } else {
      applyToFeature(project)
    }
  }

  private fun applyToFeature(project: Project) {
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    project.setupListingTask(androidComponents)
  }

  private fun applyToApplication(project: Project) {
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    // Callback for finalizeDsl is called before onVariants, so this is "safe"
    val dynamicModules = mutableSetOf<String>()
    androidComponents.finalizeDsl { extension ->
      check(extension is ApplicationExtension)
      dynamicModules.addAll(extension.dynamicFeatures)

      project.tasks.register("writeLockfile", BaseLockfileWriterTask::class.java) { task ->
        task.dependsOn(project.tasks.named("writePartialLockfile"))
        val featureProjects = dynamicModules.map { project.project(it) }

        featureProjects.forEach { featureProject ->
          task.dependsOn(featureProject.tasks.named("writePartialLockfile"))
        }

        task.outputLockfile = project.projectDir.resolve("gradle.lockfile")
        task.partialFeatureLockfiles = featureProjects.map { it.partialLockfilePath() }
        task.partialBaseLockfile = project.partialLockfilePath()
      }
    }

    project.setupListingTask(androidComponents)
  }

  private fun Project.setupListingTask(androidComponents: AndroidComponentsExtension<*, *, *>) {
    val variantNames = mutableSetOf<String>()

    androidComponents.onVariants { variant ->
      variantNames += variant.name

      configurations.named("${variant.name}RuntimeClasspath").configure {
        it.resolutionStrategy.activateDependencyLocking()
      }
    }

    afterEvaluate {
      project.tasks.register(
        "writePartialLockfile",
        PartialLockfileWriterTask::class.java
      ) { task ->
        val artifactCollections = variantNames.associateWith {
          project.configurations.getByName("${it}RuntimeClasspath")
            .getConfigurationArtifactCollection()
        }

        task.setDependencyArtifacts(artifactCollections)
        task.dependencyFileCollection.setFrom(artifactCollections.map { (_, value) -> value.artifactFiles }
          .reduce { acc, fileCollection -> acc + fileCollection })

        task.projectName = this.name
        task.partialLockFile = this.partialLockfilePath()
        task.configurationNames = variantNames.map { "${it}RuntimeClasspath" }.sorted()
      }
    }
  }

  private fun Configuration.getConfigurationArtifactCollection(): ArtifactCollection =
    incoming.artifactView { config ->
      config.attributes { container ->
        container.attribute(
          AndroidArtifacts.ARTIFACT_TYPE,
          AndroidArtifacts.ArtifactType.AAR_OR_JAR.type
        )
      }

      // Look only for external dependencies
      config.componentFilter { it !is ProjectComponentIdentifier }
    }.artifacts

  private fun Project.partialLockfilePath(): File = buildDir.resolve("tmp/gradle.lockfile.partial")
}
