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
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

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

  @TaskAction
  fun processImplementations() {
    val tempClassDirectory = temporaryDir.resolve("classes").also { it.mkdirs() }

    compile(tempClassDirectory)
    mergeClasses(tempClassDirectory)
  }

  private fun compile(classOutputDirectory: File) {
    val combinedClasspath = (
      projectClasses.get().map { it.asFile } +
        kotlinCompileClasspath.files.flatMap { it.walk().filter(File::isFile) }
      )

    val generatedSourceFiles = generatedSources.asFile.get()
      .walk()
      .filter(File::isFile)
      .map { it.absolutePath }
      .toList()

    val arguments = K2JVMCompilerArguments().apply {
      classpath = combinedClasspath.joinToString(separator = File.pathSeparator) { it.absolutePath }
      destination = classOutputDirectory.absolutePath
      freeArgs = generatedSourceFiles

      // We supply the stdlib JAR(s) via the classpath, this just suppresses a warning message
      noStdlib = true
    }

    // We can't use @SkipWhenEmpty on the sources directory because AGP always needs an output from
    // this task, but if we try to pass zero source files to the compiler it crashes
    if (generatedSourceFiles.isNotEmpty()) {
      val result = K2JVMCompiler().exec(
        messageCollector = PrintingMessageCollector(
          System.err,
          MessageRenderer.GRADLE_STYLE,
          false,
        ),
        services = Services.EMPTY,
        arguments,
      )

      require(result == ExitCode.OK) { "Kotlin Compiler Error while compiling dynamic feature implementations." }
    } else {
      logger.debug("No generated sources to compile. Skipping.")
    }
  }

  private fun mergeClasses(compiledGeneratedClasses: File) {
    val target = output.asFile.get()

    JarOutputStream(target.outputStream()).use { jar ->
      // Copy existing jar entries over
      projectJars.get().map { JarFile(it.asFile) }.forEach { inputJar ->
        inputJar.entries().iterator().forEach { jarEntry ->
          jar.putNextEntry(JarEntry(jarEntry.name))
          inputJar.getInputStream(jarEntry).use { it.copyTo(jar) }
          jar.closeEntry()
        }
      }

      // Copy existing classes over
      projectClasses.get().map { it.asFile }.forEach { root ->
        root.walk().filter(File::isFile).forEach { file ->
          val relative = file.relativeTo(root)
          jar.putEntry(relative.path, file)
        }
      }

      // Copy compiled generated classes over
      compiledGeneratedClasses.walk().filter(File::isFile).forEach { file ->
        val relative = file.relativeTo(compiledGeneratedClasses)
        jar.putEntry(relative.path, file)
      }
    }
  }

  private fun JarOutputStream.putEntry(name: String, source: File) {
    putNextEntry(JarEntry(name))
    source.inputStream().copyTo(this)
    closeEntry()
  }
}
