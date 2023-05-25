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
package app.cash.better.dynamic.features.magic

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readBytes

/**
 * This task manipulates the bytecode of the generated classes in the generated `R.jar` archive so
 * that the static fields contain the correct resource ID values taken from the base module.
 */
@AaptMagic
abstract class MagicRClassFixingTask : DefaultTask() {
  @get:InputFile
  abstract val resourceMappingFile: RegularFileProperty

  @get:InputFile
  abstract val jar: RegularFileProperty

  @get:InputFile
  abstract val styleableMappingFile: RegularFileProperty

  @get:OutputFile
  abstract val output: RegularFileProperty

  private lateinit var resourceMapping: Map<String, Map<Int, ResourceEntry>>
  private lateinit var styleableMapping: Map<String, List<Int>>

  @TaskAction
  fun rewriteClasses() {
    resourceMapping = readResourceMapping()
    styleableMapping = readStyleableMapping()

    val tempJarFile = temporaryDir.resolve("R.jar")
    val jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(tempJarFile)))

    val jarFs = FileSystems.newFileSystem(jar.asFile.get().toPath(), javaClass.classLoader)
    val pool = ClassPool(null)

    Files.walk(jarFs.getPath("")).filter(Path::isRegularFile).forEach { path ->
      val bytes = when (path.name) {
        "R\$attr.class" -> processClass(pool, path, "attr").toBytecode()
        "R\$id.class" -> processClass(pool, path, "id").toBytecode()
        "R\$styleable.class" -> processStyleableClass(pool, path).toBytecode()
        else -> path.readBytes()
      }

      jarOutput.run {
        putNextEntry(JarEntry(path.toString()))
        write(bytes)
        closeEntry()
      }
    }

    jarOutput.close()
    tempJarFile.copyTo(output.asFile.get(), overwrite = true)
  }

  private fun processClass(pool: ClassPool, file: Path, type: String): CtClass {
    val clazz = pool.makeClass(file.inputStream())
    val attrs = resourceMapping.getValue(type)

    // Prevent concurrent modification
    val fieldsCopy = clazz.declaredFields.toList()

    fieldsCopy.forEach { field ->
      val originalId = field.constantValue as Int
      if (attrs.containsKey(originalId)) {
        clazz.removeField(field)
        clazz.addField(field, CtField.Initializer.constant(attrs.getValue(originalId).base))
      }
    }

    return clazz
  }

  private fun processStyleableClass(pool: ClassPool, file: Path): CtClass {
    val clazz = pool.makeClass(file.inputStream())
    val fieldsCopy = clazz.declaredFields.toList()

    fieldsCopy.forEach { field ->
      if (styleableMapping.containsKey(field.name)) {
        val realIds = styleableMapping.getValue(field.name)
        clazz.removeField(field)
        clazz.addField(field, CtField.Initializer.byExpr("new int[] { ${realIds.joinToString()} }"))
      }
    }

    // If we don't clear the static initializer, it'll try to overwrite the values in fields that we added
    clazz.classInitializer?.setBody("{}")

    return clazz
  }

  private fun readResourceMapping(): Map<String, Map<Int, ResourceEntry>> =
    resourceMappingFile.asFile.get()
      .readLines()
      .map { line ->
        val (key, feature, base) = line.split(" ")
        val (type, name) = key.split("/")

        ResourceEntry(type, name, feature.toInt(), base.toInt())
      }
      .groupBy { it.type }
      .mapValues { (_, value) -> value.associateBy { it.feature } }

  private fun readStyleableMapping(): Map<String, List<Int>> = styleableMappingFile.asFile.get()
    .readLines()
    .associate { line ->
      val parts = line.split(" ")

      parts[0] to parts.drop(1).map { it.toInt() }
    }

  private data class ResourceEntry(
    val type: String,
    val name: String,
    val feature: Int,
    val base: Int,
  )
}
