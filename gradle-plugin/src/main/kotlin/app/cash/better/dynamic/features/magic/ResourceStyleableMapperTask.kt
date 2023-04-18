// Copyright Square, Inc.
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
