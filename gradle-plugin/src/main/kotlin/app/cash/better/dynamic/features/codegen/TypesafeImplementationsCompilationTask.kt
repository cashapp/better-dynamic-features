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
package app.cash.better.dynamic.features.codegen

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class TypesafeImplementationsCompilationTask : DefaultTask() {
  @get:InputFiles
  abstract val projectJars: ListProperty<RegularFile>

  @get:InputFiles
  abstract val projectClasses: ListProperty<Directory>

  @get:OutputFile
  abstract val output: RegularFileProperty

  @get:InputDirectory
  abstract val generatedSources: DirectoryProperty

  @get:Classpath
  abstract val kotlinCompileClasspath: ConfigurableFileCollection

  @get:Inject
  abstract val workerExecutor: WorkerExecutor

  interface TypesafeImplementationsCompilationWorkParameters : WorkParameters {
    val projectJars: ListProperty<RegularFile>
    val projectClasses: ListProperty<Directory>
    val output: RegularFileProperty
    val generatedSources: DirectoryProperty
    val kotlinCompileClasspath: ConfigurableFileCollection
    val temporaryDir: DirectoryProperty
  }

  @TaskAction
  fun processImplementations() {
    val tempClassDirectory = temporaryDir.resolve("classes").also { it.mkdirs() }

    val workQueue = workerExecutor.classLoaderIsolation()

    workQueue.submit(CompileTypesafeImplementations::class.java) { parameters ->
      parameters.projectJars.set(projectJars)
      parameters.projectClasses.set(projectClasses)
      parameters.output.set(output)
      parameters.generatedSources.set(generatedSources)
      parameters.kotlinCompileClasspath.setFrom(kotlinCompileClasspath)
      parameters.temporaryDir.set(tempClassDirectory)
    }
  }
}
