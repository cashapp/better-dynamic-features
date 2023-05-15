package app.cash.better.dynamic.features

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.Path

internal fun GradleRunner.withCommonConfiguration(projectRoot: File): GradleRunner {
  File(projectRoot, "gradle.properties").writeText(
    """
      |org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
      |android.useAndroidX=true
      |
    """.trimMargin(),
  )
  File(projectRoot, "local.properties").apply {
    if (!exists()) writeText("sdk.dir=${androidHome()}\n")
  }
  return withProjectDir(projectRoot)
}

internal fun androidHome(): String {
  val env = System.getenv("ANDROID_HOME")
  if (env != null) {
    return env.withInvariantPathSeparators()
  }
  val localProp = File(File(System.getProperty("user.dir")).parentFile, "local.properties")
  if (localProp.exists()) {
    val prop = Properties()
    localProp.inputStream().use {
      prop.load(it)
    }
    val sdkHome = prop.getProperty("sdk.dir")
    if (sdkHome != null) {
      return sdkHome.withInvariantPathSeparators()
    }
  }
  throw IllegalStateException(
    "Missing 'ANDROID_HOME' environment variable or local.properties with 'sdk.dir'",
  )
}

internal fun String.withInvariantPathSeparators() = replace("\\", "/")

fun GradleRunner.withFreshLockfile(module: String = "base"): GradleRunner {
  withArguments("$module:writeLockfile").build()
  return this
}

fun GradleRunner.cleaned(module: String? = null): GradleRunner {
  withArguments(if (module == null) "clean" else "$module:clean").build()
  return this
}

fun dexdump(file: File): Path {
  val outputPath = Files.createTempFile("dexdump", "txt")

  val buildToolsVersion = Class.forName("com.android.SdkConstants").getDeclaredField("CURRENT_BUILD_TOOLS_VERSION").get(null)
  val dexdumpPath = "${System.getenv("ANDROID_SDK_ROOT")}/build-tools/$buildToolsVersion/dexdump"
  check(Files.exists(Path(dexdumpPath))) { "Could not find 'dexdump' binary. Checked $dexdumpPath" }

  val process = ProcessBuilder()
    .apply {
      command(dexdumpPath, file.absolutePath)
      redirectOutput(outputPath.toFile())
    }
    .start()

  check(process.waitFor() == 0) { "dexdump failed" }
  return outputPath
}
