// Copyright Square, Inc.
package app.cash.better.dynamic.features

import app.cash.better.dynamic.features.tasks.BaseLockfileWriterTask
import app.cash.better.dynamic.features.tasks.CheckExternalResourcesTask
import app.cash.better.dynamic.features.tasks.CheckLockfileTask
import app.cash.better.dynamic.features.tasks.GenerateExternalResourcesTask
import app.cash.better.dynamic.features.tasks.PartialLockfileWriterTask
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.configurationcache.extensions.capitalized
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
    val pluginExtension = project.extensions.create("betterDynamicFeatures", BetterDynamicFeaturesExtension::class.java)

    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

    androidComponents.finalizeDsl { extension ->
      check(extension is ApplicationExtension)
      val featureProjects = extension.dynamicFeatures.map { project.project(it) }

      val realLockfile = project.projectDir.resolve("gradle.lockfile")
      val tempLockfile = project.buildDir.resolve("tmp/gradle.lockfile")

      project.tasks.register("writeLockfile", BaseLockfileWriterTask::class.java) { task ->
        task.configure(project, featureProjects, realLockfile)
      }

      val tempLockfileTask =
        project.tasks.register("writeTempLockfile", BaseLockfileWriterTask::class.java) { task ->
          task.configure(project, featureProjects, tempLockfile)
        }

      val checkLockfileTask =
        project.tasks.register("checkLockfile", CheckLockfileTask::class.java) { task ->
          task.currentLockfilePath = realLockfile
          task.newLockfile = tempLockfile
          task.outputFile = project.buildDir.resolve("tmp/lockfile_check")

          task.dependsOn(tempLockfileTask)
          task.group = GROUP
          task.isCi = project.providers.systemProperty("ci").orElse("false").get().toBoolean()
          task.projectPath = project.path
        }

      project.tasks.named("preBuild").dependsOn(checkLockfileTask)
    }

    project.setupListingTask(androidComponents)
    androidComponents.onVariants { variant ->
      // We only want to enforce the lockfile if we aren't explicitly trying to update it
      if (project.gradle.startParameter.taskNames.none {
        it.contains("writeLockfile", ignoreCase = true) ||
          it.contains("writePartialLockfile", ignoreCase = true) ||
          it.contains("checkLockfile", ignoreCase = true)
      }
      ) {
        project.configurations.named("${variant.name}RuntimeClasspath").configure {
          it.resolutionStrategy.activateDependencyLocking()
        }
      }

      val generateTask = project.tasks.register(
        "generate${variant.name.capitalized()}ExternalResources",
        GenerateExternalResourcesTask::class.java,
      ) { task ->
        task.outputDirectory.set(project.layout.buildDirectory.dir("gen"))
        task.declarations = pluginExtension.externalStyles
        task.group = GROUP
      }
      variant.sources.res?.addGeneratedSourceDirectory(generateTask, GenerateExternalResourcesTask::outputDirectory)

      project.afterEvaluate {
        val externalsTask = project.tasks.register(
          "check${variant.name.capitalized()}ExternalResources",
          CheckExternalResourcesTask::class.java,
        ) { task ->
          val configuration = project.configurations.getByName("${variant.name}RuntimeClasspath")
          val artifacts = configuration.incoming.artifactView { config ->
            config.attributes { container ->
              container.attribute(
                AndroidArtifacts.ARTIFACT_TYPE,
                AndroidArtifacts.ArtifactType.SYMBOL_LIST_WITH_PACKAGE_NAME.type,
              )
            }
          }.artifacts

          task.setIncomingResources(artifacts)
          task.incomingResourcesCollection.setFrom(artifacts.artifactFiles)
          task.manifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
          task.externalDeclarations = pluginExtension.externalStyles
          task.localResources = variant.sources.res?.all
          task.group = GROUP
        }

        project.tasks.named("process${variant.name.capitalized()}Resources").dependsOn(externalsTask)

        // https://issuetracker.google.com/issues/223240936#comment40
        project.tasks.named("map${variant.name.capitalized()}SourceSetPaths").dependsOn(generateTask)
      }
    }
  }

  private fun Project.setupListingTask(androidComponents: AndroidComponentsExtension<*, *, *>) {
    val variantNames = mutableSetOf<String>()

    androidComponents.onVariants { variant ->
      variantNames += variant.name
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

        task.dependencyFileCollection.setFrom(
          artifactCollections.map { (_, value) -> value.artifactFiles }
            .reduce { acc, fileCollection -> acc + fileCollection },
        )

        task.setResolvedLockfileEntriesProvider(
          project.provider {
            variantNames.associateWith {
              project.configurations.getByName(
                "${it}RuntimeClasspath",
              ).resolvedConfiguration
            }
          },
        )

        task.projectName = this.name
        task.rootProjectName = this.rootProject.name
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
    featureProjects: List<Project>,
    output: File,
  ) {
    dependsOn(project.tasks.named("writePartialLockfile"))

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
