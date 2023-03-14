// Copyright Square, Inc.
package app.cash.better.dynamic.features

import app.cash.better.dynamic.features.tasks.BaseLockfileWriterTask
import app.cash.better.dynamic.features.tasks.CheckExternalResourcesTask
import app.cash.better.dynamic.features.tasks.CheckLockfileTask
import app.cash.better.dynamic.features.tasks.DependencyGraphWriterTask
import app.cash.better.dynamic.features.tasks.GenerateExternalResourcesTask
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized

@Suppress("UnstableApiUsage", "Unused")
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

    project.createSharedFeatureConfiguration()
    project.setupFeatureDependencyGraphTasks(androidComponents)
  }

  private fun applyToApplication(project: Project) {
    val pluginExtension = project.extensions.create("betterDynamicFeatures", BetterDynamicFeaturesExtension::class.java)

    val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
    val sharedConfiguration = project.createSharedBaseConfiguration()

    androidComponents.finalizeDsl { extension ->
      check(extension is ApplicationExtension)
      val featureProjects = extension.dynamicFeatures.map { project.project(it) }

      project.setupBaseDependencyGraphTasks(androidComponents, featureProjects, sharedConfiguration)
    }

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

  private fun Project.configureDependencyGraphTask(variant: Variant): TaskProvider<DependencyGraphWriterTask> =
    tasks.register(
      "write${variant.name.capitalized()}DependencyGraph",
      DependencyGraphWriterTask::class.java,
    ) { task ->
      val artifactCollections = project.configurations.getByName("${variant.name}RuntimeClasspath")
        .getConfigurationArtifactCollection()

      task.dependencyFileCollection.setFrom(artifactCollections.artifactFiles)

      task.setResolvedLockfileEntriesProvider(
        project.provider {
          project.configurations.getByName("${variant.name}RuntimeClasspath")
            .incoming
            .resolutionResult
            .root
        },
        variant.name,
      )

      task.partialLockFile.set { buildDir.resolve("betterDynamicFeatures/deps/${variant.name}DependencyGraph.json") }
      task.group = GROUP
    }

  private fun Project.createSharedFeatureConfiguration() {
    configurations.create(CONFIGURATION_BDF).apply {
      isCanBeConsumed = true
      isCanBeResolved = false
      attributes.apply {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, ATTRIBUTE_USAGE_METADATA))
      }
      outgoing.variants.create("feature-dependencies").apply {
        attributes.apply {
          attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements::class.java, ATTRIBUTE_FEATURE_DEPENDENCIES))
        }
      }
    }
  }

  private fun Project.createSharedBaseConfiguration(): Configuration =
    configurations.create(CONFIGURATION_BDF).apply {
      isCanBeConsumed = false
      isCanBeResolved = true
      attributes.apply {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, ATTRIBUTE_USAGE_METADATA))
      }
      outgoing.variants.create("base-dependencies").apply {
        attributes.apply {
          attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements::class.java, ATTRIBUTE_BASE_DEPENDENCIES))
        }
      }
    }

  private fun Project.setupFeatureDependencyGraphTasks(androidComponents: AndroidComponentsExtension<*, *, *>) {
    androidComponents.onVariants { variant ->
      val task = configureDependencyGraphTask(variant)
      artifacts { handler ->
        handler.add(CONFIGURATION_BDF, task.flatMap { it.partialLockFile }) {
          it.builtBy(task)
        }
      }
    }
  }

  private fun Project.setupBaseDependencyGraphTasks(androidComponents: AndroidComponentsExtension<*, *, *>, featureProjects: List<Project>, configuration: Configuration) {
    featureProjects.forEach { dependencies.add(CONFIGURATION_BDF, it) }

    androidComponents.onVariants { variant ->
      val task = configureDependencyGraphTask(variant)
      artifacts { handler ->
        handler.add(CONFIGURATION_BDF, task.flatMap { it.partialLockFile }) {
          it.builtBy(task)
        }
      }
    }

    val featureDependencyArtifacts = configuration.incoming.artifactView { config ->
      config.attributes { container ->
        container.attribute(
          LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
          project.objects.named(LibraryElements::class.java, ATTRIBUTE_FEATURE_DEPENDENCIES),
        )
      }
    }.artifacts.artifactFiles

    val baseDependencyArtifacts = configuration.incoming.artifactView { config ->
      config.attributes { container ->
        container.attribute(
          LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
          project.objects.named(LibraryElements::class.java, ATTRIBUTE_BASE_DEPENDENCIES),
        )
      }
    }.artifacts.artifactFiles

    project.tasks.register("writeLockfile", BaseLockfileWriterTask::class.java) { task ->
      task.outputLockfile.set(dependencyLocking.lockFile)
      task.featureDependencyGraphFiles.setFrom(featureDependencyArtifacts)
      task.baseDependencyGraphFiles.setFrom(baseDependencyArtifacts)

      task.group = GROUP
    }

    val checkLockfileTask = project.tasks.register("checkLockfile", CheckLockfileTask::class.java) { task ->
      task.currentLockfilePath.set(dependencyLocking.lockFile)
      task.outputFile.set { project.buildDir.resolve("betterDynamicFeatures/deps/lockfile_check") }
      task.featureDependencyGraphFiles.setFrom(featureDependencyArtifacts)
      task.baseDependencyGraphFiles.setFrom(baseDependencyArtifacts)

      task.group = GROUP
      task.runningOnCi.set(project.providers.systemProperty("ci").orElse("false").map { it.toBoolean() })
      task.projectPath.set(project.path)
    }
    tasks.named("preBuild").dependsOn(checkLockfileTask)
  }

  internal companion object {
    const val GROUP = "Better Dynamic Features"

    const val ATTRIBUTE_BASE_DEPENDENCIES = "base-dependencies"
    const val ATTRIBUTE_FEATURE_DEPENDENCIES = "feature-dependencies"
    const val ATTRIBUTE_USAGE_METADATA = "better-dynamic-features-metadata"
    const val CONFIGURATION_BDF = "betterDynamicFeatures"
  }
}
