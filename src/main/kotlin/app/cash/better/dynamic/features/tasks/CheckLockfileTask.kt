package app.cash.better.dynamic.features.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class CheckLockfileTask : DefaultTask() {
  @get:InputFile
  abstract var newLockfile: File

  @get:Internal
  abstract var currentLockfilePath: File

  @get:[Optional InputFile]
  val currentLockfile: File?
    get() = currentLockfilePath.takeIf { it.exists() }

  @get:OutputFile
  abstract var outputFile: File

  @get:Input
  abstract var projectPath: String

  @get:Input
  abstract var isCi: Boolean

  @TaskAction
  fun checkLockfiles() {
    val areTheyEqual = newLockfile.readText() == currentLockfile?.readText()
    outputFile.writeText(areTheyEqual.toString())
    if (!areTheyEqual) {
      Files.copy(newLockfile.toPath(), currentLockfilePath.toPath(), StandardCopyOption.REPLACE_EXISTING)
      if (isCi) {
        throw IllegalStateException("The lockfile was out of date. Run the '$projectPath:writeLockfile' task and commit the updated lockfile.")
      } else {
        throw IllegalStateException("The lockfile was out of date and has been updated. Rerun your build.")
      }
    }
  }
}
