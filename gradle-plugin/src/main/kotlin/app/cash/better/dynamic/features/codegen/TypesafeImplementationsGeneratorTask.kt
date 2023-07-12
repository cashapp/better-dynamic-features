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

import app.cash.better.dynamic.features.codegen.api.FeatureImplementation
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class TypesafeImplementationsGeneratorTask : DefaultTask() {
  @get:InputFiles
  abstract val featureImplementationReports: ConfigurableFileCollection

  @get:OutputFile
  abstract val generatedProguardFile: RegularFileProperty

  @get:OutputDirectory
  abstract val generatedFilesDirectory: DirectoryProperty

  private val moshi = Moshi.Builder().build()
  private val featureAdapter = moshi.adapter(FeatureImplementation::class.java)

  private val generatedSourcesDirectory get() = generatedFilesDirectory.asFile.get()

  @TaskAction
  fun generate() {
    generatedSourcesDirectory.mkdirs()

    val collectedFeatures = featureImplementationReports.files
      .flatMap { dir -> buildList { dir.walk().forEach { if (it.isFile) add(it) } } }
      .mapNotNull { it.source().buffer().use { buffer -> featureAdapter.fromJson(buffer) } }
      .groupBy { it.parentClass }

    collectedFeatures.forEach { (api, implementations) ->
      val fileSpec =
        generateImplementationsContainer(forApi = api, implementations = implementations)
      fileSpec.writeTo(directory = generatedSourcesDirectory)
    }

    val proguardFile = generatedProguardFile.asFile.get()
    if (proguardFile.exists()) {
      proguardFile.delete()
      proguardFile.createNewFile()
    }
    proguardFile.bufferedWriter().use { writer ->
      collectedFeatures.forEach { (api, implementations) ->
        writer.append(generateProguardRules(forApi = api, implementations))
      }
    }
  }
}
