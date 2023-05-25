package app.cash.better.dynamic.features.codegen

import app.cash.better.dynamic.features.codegen.api.FeatureImplementation
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class TypesafeImplementationsGeneratorTask : DefaultTask() {
  @get:InputFiles
  abstract val featureImplementationReports: ConfigurableFileCollection

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
  }
}
