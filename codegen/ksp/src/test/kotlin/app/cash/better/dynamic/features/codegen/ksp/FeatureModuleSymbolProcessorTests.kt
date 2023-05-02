package app.cash.better.dynamic.features.codegen.ksp

import app.cash.better.dynamic.features.codegen.api.KSP_REPORT_DIRECTORY_PREFIX
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessorProviders
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

  private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation =
    KotlinCompilation().apply {
      workingDir = workingDirectory.root
      inheritClassPath = true
      sources = sourceFiles.asList()
      verbose = true
      kspIncremental = true

      symbolProcessorProviders = listOf(DynamicFeaturesSymbolProcessorProvider())
      kspArgs[KSP_REPORT_DIRECTORY_PREFIX] = resultsDirectory.root.absolutePath
    }

  private fun compile(vararg sourceFile: SourceFile): KotlinCompilation.Result =
    prepareCompilation(*sourceFile).compile()

  private fun assertThatFileContent(file: File) = assertThat(file.readText())
}
