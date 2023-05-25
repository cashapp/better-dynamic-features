/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.better.dynamic.features.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CheckLockfileTask : DependencyGraphConsumingTask() {
  @get:Internal
  abstract val currentLockfilePath: RegularFileProperty

  @get:[Optional InputFile]
  val currentLockfile: File?
    get() = currentLockfilePath.get().asFile.takeIf { it.exists() }

  // Used for caching
  @get:OutputFile
  abstract val outputFile: RegularFileProperty

  @get:Input
  abstract val projectPath: Property<String>

  @get:Input
  abstract val runningOnCi: Property<Boolean>

  @TaskAction
  fun checkLockfiles() {
    val currentEntries = mergeGraphs(baseGraph(), featureGraphs())
    val areTheyEqual = currentEntries.toText() == currentLockfile?.readText()

    outputFile.asFile.get().writeText(areTheyEqual.toString())
    if (!areTheyEqual) {
      currentLockfilePath.asFile.get().writeText(currentEntries.toText())
      if (runningOnCi.get()) {
        // TODO: Revert back to a more generic message when this flag is no longer needed
        throw IllegalStateException("The lockfile was out of date. Run './gradlew -Dcash.os.dynamicFeatures=true ${projectPath.get()}:writeLockfile' and commit the updated lockfile.")
      } else {
        throw IllegalStateException("The lockfile was out of date and has been updated. Rerun your build.")
      }
    }
  }
}
