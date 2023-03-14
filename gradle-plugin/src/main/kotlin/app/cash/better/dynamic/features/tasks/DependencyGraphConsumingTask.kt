package app.cash.better.dynamic.features.tasks

import com.squareup.moshi.Moshi
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

abstract class DependencyGraphConsumingTask : DefaultTask() {
  private val moshi = Moshi.Builder().build()
  private val adapter = moshi.adapter(NodeList::class.java)

  /**
   * Dependency graph files for each AGP variant of every feature module.
   */
  @get:InputFiles
  abstract val featureDependencyGraphFiles: ConfigurableFileCollection

  /**
   * Dependency graph files for every AGP variant of the base module.
   */
  @get:InputFiles
  abstract val baseDependencyGraphFiles: ConfigurableFileCollection

  @get:Internal
  protected val baseGraph by lazy {
    baseDependencyGraphFiles.map {
      adapter.fromJson(it.readText())?.nodes ?: error("Could not read graph from $it")
    }.flatten()
  }

  @get:Internal
  protected val featureGraphs by lazy {
    featureDependencyGraphFiles.map {
      adapter.fromJson(it.readText())?.nodes ?: error("Could not read graph from $it")
    }
  }
}
