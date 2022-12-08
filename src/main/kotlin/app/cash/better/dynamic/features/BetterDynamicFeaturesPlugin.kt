// Copyright Square, Inc.
package app.cash.better.dynamic.features

import app.cash.better.dynamic.features.tasks.BaseLockfileWriterTask
import app.cash.better.dynamic.features.tasks.CheckLockfileTask
import app.cash.better.dynamic.features.tasks.PartialLockfileWriterTask
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
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

    project.afterEvaluate {
      val base = project.configurations.getByName("implementation").dependencies
        .filterIsInstance<DefaultProjectDependency>()
        .firstOrNull {
          it.dependencyProject.plugins.hasPlugin("com.android.application") &&
            it.dependencyProject.plugins.hasPlugin("app.cash.better.dynamic.features")
        }
      checkNotNull(base) {
        "Your base application module should be added as a dependency of this project"
      }
    }
  }

  private fun applyToApplication(project: Project) {
    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    // Callback for finalizeDsl is called before onVariants, so this is "safe"
    val dynamicModules = mutableSetOf<String>()
    androidComponents.finalizeDsl { extension ->
      check(extension is ApplicationExtension)
      dynamicModules.addAll(extension.dynamicFeatures)

      val realLockfile = project.projectDir.resolve("gradle.lockfile")
      val tempLockfile = project.buildDir.resolve("tmp/gradle.lockfile")

      project.tasks.register("writeLockfile", BaseLockfileWriterTask::class.java) { task ->
        task.configure(project, dynamicModules, realLockfile)
      }

      val tempLockfileTask =
        project.tasks.register("writeTempLockfile", BaseLockfileWriterTask::class.java) { task ->
          task.configure(project, dynamicModules, tempLockfile)
        }

      val checkLockfileTask =
        project.tasks.register("checkLockfile", CheckLockfileTask::class.java) { task ->
          task.currentLockfile = realLockfile
          task.newLockfile = tempLockfile
          task.outputFile = project.buildDir.resolve("tmp/lockfile_check")

          task.dependsOn(tempLockfileTask)
          task.group = GROUP
        }

      project.tasks.named("preBuild").dependsOn(checkLockfileTask)
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
        PartialLockfileWriterTask::class.java,
      ) { task ->
        val artifactCollections = variantNames.associateWith {
          project.configurations.getByName("${it}RuntimeClasspath")
            .getConfigurationArtifactCollection()
        }

        task.setDependencyArtifacts(artifactCollections)
        task.dependencyFileCollection.setFrom(
          artifactCollections.map { (_, value) -> value.artifactFiles }
            .reduce { acc, fileCollection -> acc + fileCollection },
        )

        task.projectName = this.name
        task.partialLockFile = this.partialLockfilePath()
        task.configurationNames = variantNames.map { "${it}RuntimeClasspath" }.sorted()

        task.group = GROUP
      }
    }
  }

  private fun Configuration.getConfigurationArtifactCollection(): ArtifactCollection =
    incoming.artifactView { config ->
      config.attributes { container ->
        container.attribute(
          AndroidArtifacts.ARTIFACT_TYPE,
          AndroidArtifacts.ArtifactType.AAR_OR_JAR.type,
        )
      }

      // Look only for external dependencies
      config.componentFilter { it !is ProjectComponentIdentifier }
    }.artifacts

  private fun BaseLockfileWriterTask.configure(
    project: Project,
    dynamicModules: Set<String>,
    output: File,
  ) {
    dependsOn(project.tasks.named("writePartialLockfile"))
    val featureProjects = dynamicModules.map { project.project(it) }

    featureProjects.forEach { featureProject ->
      check(featureProject.plugins.hasPlugin("app.cash.better.dynamic.features")) {
        "Plugin 'app.cash.better.dynamic.features' needs to be applied to $featureProject"
      }
      dependsOn(featureProject.tasks.named("writePartialLockfile"))
    }

    outputLockfile = output
    partialFeatureLockfiles = featureProjects.map { it.partialLockfilePath() }
    partialBaseLockfile = project.partialLockfilePath()

    group = GROUP
  }

  private fun Project.partialLockfilePath(): File = buildDir.resolve("tmp/gradle.lockfile.partial")

  internal companion object {
    const val GROUP = "Better Dynamic Features"
  }
}
