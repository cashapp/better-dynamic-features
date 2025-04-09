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

import app.cash.better.dynamic.features.codegen.api.KSP_REPORT_DIRECTORY_PREFIX
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class FeatureModuleSymbolProcessorTests {
  // Working directory for the embedded Kotlin compiler
  @get:Rule
  val workingDirectory = TemporaryFolder()

  // Directory that KSP dumps the .json files into
  @get:Rule
  val resultsDirectory = TemporaryFolder()

  @Test
  fun `basic dynamic api setup works`() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        package test

        import app.cash.better.dynamic.features.DynamicApi
        import app.cash.better.dynamic.features.DynamicImplementation

        interface MyApi : DynamicApi

        @DynamicImplementation
        class MyImplementation : MyApi
        """.trimIndent(),
      ),
    )

    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    assertThatFileContent(resultsDirectory.root.resolve("test.MyImplementation.json"))
      .contains("""{"qualifiedName":"test.MyImplementation","parentClass":{"packageName":"test","className":"MyApi"}}""")
  }

  @Test
  fun `abstract dynamic implementation is not allowed`() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        package test

        import app.cash.better.dynamic.features.DynamicApi
        import app.cash.better.dynamic.features.DynamicImplementation

        interface MyApi : DynamicApi

        @DynamicImplementation
        abstract class MyAbstractImplementation : MyApi
        """.trimIndent(),
      ),
    )

    assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
    assertThat(result.messages).contains("'@DynamicImplementation' cannot be applied to abstract classes.")
  }

  @Test
  fun `dynamic implementation not inheriting a dynamic api is not allowed`() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        package test

        import app.cash.better.dynamic.features.DynamicApi
        import app.cash.better.dynamic.features.DynamicImplementation

        interface MyApi

        @DynamicImplementation
        class MyImplementation : MyApi
        """.trimIndent(),
      ),
    )

    assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
    assertThat(result.messages).contains("Class does not inherit from a 'DynamicApi' super type.")
  }

  @Test
  fun `processor recursively searches supertype hierarchy for dynamic api`() {
    val result = compile(
      kotlin(
        "source.kt",
        """
        package test

        import app.cash.better.dynamic.features.DynamicApi
        import app.cash.better.dynamic.features.DynamicImplementation

        interface MyApi : DynamicApi

        abstract class CustomApi : MyApi

        @DynamicImplementation
        class MyImplementation : CustomApi()
        """.trimIndent(),
      ),
    )

    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    assertThatFileContent(resultsDirectory.root.resolve("test.MyImplementation.json"))
      .contains("""{"qualifiedName":"test.MyImplementation","parentClass":{"packageName":"test","className":"MyApi"}}""")
  }

  private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation =
    KotlinCompilation().apply {
      workingDir = workingDirectory.root
      inheritClassPath = true
      sources = sourceFiles.asList()
      verbose = true
      kspIncremental = true
      useKsp2()

      symbolProcessorProviders = mutableListOf(DynamicFeaturesSymbolProcessorProvider())
      kspProcessorOptions[KSP_REPORT_DIRECTORY_PREFIX] = resultsDirectory.root.absolutePath
    }

  private fun compile(vararg sourceFile: SourceFile): JvmCompilationResult =
    prepareCompilation(*sourceFile).compile()

  private fun assertThatFileContent(file: File) = assertThat(file.readText())
}
