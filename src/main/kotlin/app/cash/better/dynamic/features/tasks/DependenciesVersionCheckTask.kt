// Copyright Square, Inc.
package app.cash.better.dynamic.features.tasks

import app.cash.better.dynamic.features.tasks.ListDependenciesTask.Companion.SEPARATOR
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class DependenciesVersionCheckTask : DefaultTask() {
  @get:InputFiles abstract var dynamicModuleDependencies: List<File>

  @get:InputFile abstract var baseModuleDependencies: File

  @TaskAction fun checkVersions() {
    val dynamicVersionMaps = dynamicModuleDependencies.map { readVersionMapping(it) }
    val baseVersionMap = baseModuleDependencies.let(::readVersionMapping)

    val mismatchList = mutableListOf<String>()
    dynamicVersionMaps.forEach { (module, dependencies) ->
      dependencies.forEach { (artifact, version) ->
        val baseVersion = baseVersionMap.dependencies[artifact]
        if (baseVersion != null && baseVersion != version) {
          mismatchList.add("$artifact, ${baseVersionMap.module}=${baseVersion}, ${module}=$version -> Use ${maxOf(baseVersion, version)}")
        }
      }
    }

    if (mismatchList.isNotEmpty()) {
      throw IllegalStateException("There are version mismatches in your base and dynamic feature module dependencies:\n${
        mismatchList.joinToString(separator = "\n") { "\t$it" }
      }")
    }
  }

  private fun readVersionMapping(file: File): ModuleDependencies {
    val lines = file.readLines()

    val module = lines.first()
    val deps = lines.drop(1).map { it.split(SEPARATOR) }
      .associate { (artifact, version) -> artifact to version }
    return ModuleDependencies(module, deps)
  }

  private data class ModuleDependencies(val module: String, val dependencies: Map<String, String>)
}
