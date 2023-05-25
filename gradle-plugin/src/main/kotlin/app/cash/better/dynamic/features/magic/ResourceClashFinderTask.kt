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
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

@AaptMagic
abstract class ResourceClashFinderTask : DefaultTask() {
  @get:InputFile
  abstract val baseSymbolList: RegularFileProperty

  @get:InputFiles
  abstract val featureSymbolLists: ConfigurableFileCollection

  @get:OutputFile
  abstract val resourceMappingFile: RegularFileProperty

  @TaskAction
  fun findClashes() {
    // Maps the feature module value to the base module value
    val clashMappings = mutableMapOf<Int, Clash>()

    val baseResourcesByName =
      parseSymbols(baseSymbolList.get().asFile)
        .filter { it.type in SUPPORTED_TYPES }
        .associateBy { it.key }

    featureSymbolLists.files.forEach { file ->
      parseSymbols(file).filter { it.type in SUPPORTED_TYPES }.forEach { symbol ->
        when (val base = baseResourcesByName[symbol.key]) {
          null -> {}
          else -> {
            clashMappings[symbol.id] = Clash(symbol.key, symbol.id, base.id)
          }
        }
      }
    }

    resourceMappingFile.asFile.get().writeText(
      clashMappings.entries.joinToString(separator = "\n") { (_, clash) ->
        "${clash.resource} ${clash.feature} ${clash.base}"
      },
    )
  }

  private fun parseSymbols(file: File) = file.readLines().mapNotNull { line ->
    val parts = line.split(" ")
    if (parts[0] == "int[]") return@mapNotNull null

    val id = parts[3].removePrefix("0x").toInt(16)
    Symbol(parts[1], parts[2], id)
  }

  /**
   * int attr specialText 0x7e010000
   * int dimen fab_margin 0x7e020000
   * int id FirstFragment 0x7e030000
   * int id button_first 0x7e030001
   * int id custom_text_view 0x7e030002
   * int id fab 0x7e030003
   * int id nav_graph 0x7e030004
   */
  private data class Symbol(val type: String, val name: String, val id: Int)

  private data class Clash(val resource: String, val feature: Int, val base: Int)

  private val Symbol.key get() = "$type/$name"

  private companion object {
    val SUPPORTED_TYPES = setOf("attr", "id")
  }
}
