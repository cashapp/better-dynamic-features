@file:Suppress("PrivatePropertyName")

package app.cash.better.dynamic.features.codegen

import app.cash.better.dynamic.features.codegen.api.FeatureApi
import app.cash.better.dynamic.features.codegen.api.FeatureImplementation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

private val TYPE_IMPLEMENTATIONS_CONTAINER =
  ClassName("app.cash.better.dynamic.features", "ImplementationsContainer")

fun generateImplementationsContainer(
  forApi: FeatureApi,
  implementations: List<FeatureImplementation>,
): FileSpec {
  val apiTypeName = ClassName(forApi.packageName, forApi.className)
  val fileSpec = FileSpec.builder(forApi.packageName, "${forApi.className}ImplementationsContainer")

  val implementationsFunction = PropertySpec.builder("implementations", List::class.asTypeName().parameterizedBy(apiTypeName))
    .addModifiers(OVERRIDE)
    .initializer(
      CodeBlock.builder()
        .addStatement("buildList {")
        .apply {
          implementations.forEach { implementation ->
            addStatement(
              "add(Class.forName(%S).getDeclaredConstructor().newInstance() as %T)",
              implementation.qualifiedName,
              apiTypeName,
            )
          }
        }
        .addStatement("}").build(),
    )
    .build()

  val objectSpec = TypeSpec.objectBuilder("${forApi.className}ImplementationsContainer")
    .addSuperinterface(TYPE_IMPLEMENTATIONS_CONTAINER.parameterizedBy(apiTypeName))
    .addAnnotation(
      AnnotationSpec.builder(ClassName.bestGuess("kotlin.OptIn"))
        .addMember(
          "%T::class",
          ClassName("app.cash.better.dynamic.features", "ExperimentalDynamicFeaturesApi"),
        ).build(),
    )
    .addProperty(implementationsFunction)
    .build()

  return fileSpec.addType(objectSpec).build()
}
