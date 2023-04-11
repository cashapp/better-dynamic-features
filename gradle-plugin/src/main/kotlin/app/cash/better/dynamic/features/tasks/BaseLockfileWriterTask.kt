package app.cash.better.dynamic.features.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class BaseLockfileWriterTask : DependencyGraphConsumingTask() {
  @get:OutputFile
  abstract val outputLockfile: RegularFileProperty

  @TaskAction
  fun generateLockfile() {
    val entries = mergeGraphs(baseGraph(), featureGraphs())
    outputLockfile.get().asFile.writeText(entries.toText())
  }
}
