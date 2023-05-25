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
