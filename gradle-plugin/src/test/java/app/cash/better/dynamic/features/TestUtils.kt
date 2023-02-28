package app.cash.better.dynamic.features

import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.Properties

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
