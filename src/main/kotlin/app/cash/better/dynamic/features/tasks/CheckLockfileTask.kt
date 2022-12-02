package app.cash.better.dynamic.features.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CheckLockfileTask : DefaultTask() {
  @get:InputFile
  abstract var newLockfile: File

  @get:InputFile
  abstract var currentLockfile: File

  @get:OutputFile
  abstract var outputFile: File

  @TaskAction
  fun checkLockfiles() {
    val areTheyEqual = newLockfile.readText() == currentLockfile.readText()
    outputFile.writeText(areTheyEqual.toString())
    check(areTheyEqual) {
      "The lockfile is out of date. Run ${project.path}:writeLockfile to update it."
    }
  }
}
