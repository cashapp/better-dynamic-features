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
import com.google.devtools.ksp.symbol.KSTypeReference
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
        .map { implementation -> implementation to findApiSuperType(implementation) }
        .filter { (implementation, superType) ->
          if (superType == null) logger.error(
            "Class does not inherit from a 'DynamicApi' super type.",
            symbol = implementation,
          )

          superType != null
        }
        .toList()

    val details = implementers.map { (implementation, superType) ->
      val superTypeDeclaration = superType!!.resolve().declaration
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
   * TODO: Recursively search superType hierarchy, we're only checking direct super types right now
   */
  private fun findApiSuperType(implementation: KSClassDeclaration): KSTypeReference? {
    return implementation.superTypes
      .filter { superType ->
        (superType.resolve().declaration as KSClassDeclaration).superTypes
          .any { it.resolve().declaration.qualifiedName?.asString() == RUNTIME_API_INTERFACE }
      }
      .firstOrNull()
  }
}
