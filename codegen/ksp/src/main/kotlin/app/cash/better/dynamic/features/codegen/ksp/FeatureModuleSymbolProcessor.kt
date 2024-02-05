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
package app.cash.better.dynamic.features.codegen.ksp

import app.cash.better.dynamic.features.codegen.api.FeatureApi
import app.cash.better.dynamic.features.codegen.api.FeatureImplementation
import app.cash.better.dynamic.features.codegen.api.KSP_REPORT_DIRECTORY_PREFIX
import app.cash.better.dynamic.features.codegen.api.RUNTIME_API_INTERFACE
import app.cash.better.dynamic.features.codegen.api.RUNTIME_IMPLEMENTATION_ANNOTATION
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.moshi.Moshi
import okio.buffer
import okio.sink
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.outputStream

class FeatureModuleSymbolProcessor(private val environment: SymbolProcessorEnvironment) :
  SymbolProcessor {
  private val logger = environment.logger
  private val reportDirectoryPaths = environment.options.filterKeys { it.startsWith(KSP_REPORT_DIRECTORY_PREFIX) }.map { (_, path) -> Path(path) }

  private val moshi = Moshi.Builder().build()
  private val featureAdapter = moshi.adapter(FeatureImplementation::class.java)

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val implementers =
      resolver.getSymbolsWithAnnotation(RUNTIME_IMPLEMENTATION_ANNOTATION, inDepth = true)
        .filterIsInstance<KSClassDeclaration>()
        .onEach { implementation ->
          if (implementation.isAbstract()) logger.error(
            "'@DynamicImplementation' cannot be applied to abstract classes.",
            symbol = implementation,
          )
        }
        .map { implementation -> implementation to findApiSuperType(listOf(implementation)) }
        .filter { (implementation, superType) ->
          if (superType == null) logger.error(
            "Class does not inherit from a 'DynamicApi' super type.",
            symbol = implementation,
          )

          superType != null
        }
        .toList()

    val details = implementers.map { (implementation, superType) ->
      val superTypeDeclaration = superType!!
      FeatureImplementation(
        qualifiedName = implementation.qualifiedName!!.asString(),
        parentClass = FeatureApi(superTypeDeclaration.packageName.asString(), superTypeDeclaration.simpleName.asString()),
      )
    }

    // Write the outputs to each variant directory
    reportDirectoryPaths.forEach { path ->
      Files.createDirectories(path)

      details.forEach {
        val reportFile = path / "${it.qualifiedName}.json"
        reportFile.outputStream().sink().buffer().use { buffer ->
          featureAdapter.toJson(buffer, it)
        }
      }
    }

    return emptyList()
  }

  /**
   * Finds the parent interface/class which implements the "DynamicApi" interface.
   *
   * TODO: Handle multiple DynamicApi supertypes
   */
  private tailrec fun findApiSuperType(declarations: List<KSClassDeclaration>): KSClassDeclaration? {
    val declarationSuperTypes = declarations.map { it to it.superTypes.map { type -> type.resolve() }.toList() }
    val nextTypes = mutableListOf<KSClassDeclaration>()

    for ((declaration, superTypes) in declarationSuperTypes) {
      for (type in superTypes) {
        if (type.declaration.qualifiedName?.asString() == RUNTIME_API_INTERFACE) {
          return declaration
        }

        when (val superType = type.declaration) {
          is KSClassDeclaration -> nextTypes.add(superType)
        }
      }
    }

    if (nextTypes.isEmpty()) return null
    return findApiSuperType(nextTypes)
  }
}
