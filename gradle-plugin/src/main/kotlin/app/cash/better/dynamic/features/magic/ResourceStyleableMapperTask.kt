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

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

@AaptMagic
abstract class ResourceStyleableMapperTask : DefaultTask() {

  @get:InputFile
  abstract val runtimeSymbolList: RegularFileProperty

  @get:InputFile
  abstract val resourceMappingFile: RegularFileProperty

  @get:OutputFile
  abstract val styleableArraysFile: RegularFileProperty

  private lateinit var resourceMapping: Map<Int, Int>
  private fun readResourceMapping(): Map<Int, Int> =
    resourceMappingFile.asFile.get()
      .readLines()
      .map { it.split(" ") }
      .associate { (_, key, value) -> key.toInt() to value.toInt() }

  @TaskAction
  fun writeStyleables() {
    resourceMapping = readResourceMapping()

    val styleables = parseStyleables(runtimeSymbolList.get().asFile)

    val results = styleables.map { styleable ->
      val newIds = styleable.ids.map { resourceMapping[it] ?: it }
      styleable.copy(ids = newIds)
    }

    styleableArraysFile.asFile.get().writeText(
      results.joinToString(separator = "\n") {
        """${it.name} ${it.ids.joinToString(separator = " ")}"""
      },
    )
  }

  private fun parseStyleables(file: File) = file.readLines().mapNotNull { line ->
    val parts = line.split(" ")
    if (parts[0] != "int[]") return@mapNotNull null

    val match = Regex("""\{(.*)\}""").find(line)!!.value
    val ids = match.removePrefix("{ ").removeSuffix(" }").split(", ")
      .map { it.removePrefix("0x").toInt(16) }

    Styleable(parts[2], ids)
  }

  private data class Styleable(val name: String, val ids: List<Int>)
}
