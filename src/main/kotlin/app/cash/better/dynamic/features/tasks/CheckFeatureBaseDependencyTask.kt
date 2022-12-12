package app.cash.better.dynamic.features.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class CheckFeatureBaseDependencyTask : DefaultTask() {
  @get:Input
  abstract var baseProject: Project

  @TaskAction
  fun doCheck() {
    project.checkHasDependencyOn(baseProject)
  }

  private fun Project.checkHasDependencyOn(other: Project) {
    val match = configurations.getByName("implementation").dependencies
      .filterIsInstance<ProjectDependency>()
      .firstOrNull { it.dependencyProject == other }

    checkNotNull(match) {
      "$this should have a dependency on the base $other"
    }
  }
}
