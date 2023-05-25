@file:Suppress("PrivatePropertyName")

package app.cash.better.dynamic.features.codegen

import app.cash.better.dynamic.features.codegen.api.FeatureApi
import app.cash.better.dynamic.features.codegen.api.FeatureImplementation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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

  val implementationsFunction = FunSpec.builder("buildImplementations")
    .returns(List::class.asTypeName().parameterizedBy(apiTypeName))
    .addModifiers(OVERRIDE)
    .addCode(
      CodeBlock.builder()
        .beginControlFlow("return %M", MemberName("kotlin.collections", "buildList"))
        .apply {
          implementations.forEach { implementation ->
            beginControlFlow("try")
            addStatement(
              "add(Class.forName(%S).getDeclaredConstructor().newInstance() as %T)",
              implementation.qualifiedName,
              apiTypeName,
            )
            nextControlFlow("catch(e: %T)", ClassName("java.lang", "ClassNotFoundException"))
            endControlFlow()
          }
        }
        .endControlFlow().build(),
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
    .addFunction(implementationsFunction)
    .build()

  return fileSpec.addType(objectSpec).build()
}
