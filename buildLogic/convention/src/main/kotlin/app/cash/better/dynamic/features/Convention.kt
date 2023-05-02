package app.cash.better.dynamic.features

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class Convention : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply("org.jetbrains.kotlin.jvm")

    target.kotlinExtension.jvmToolchain { spec ->
      spec.languageVersion.set(JavaLanguageVersion.of(TOOLCHAIN_JDK))
      spec.vendor.set(JvmVendorSpec.AZUL)
    }

    target.tasks.withType(KotlinCompile::class.java).configureEach { task ->
      task.compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(TARGET_JDK.toString()))
      }
    }

    target.extensions.findByType(JavaPluginExtension::class.java)?.apply {
      sourceCompatibility = JavaVersion.toVersion(TARGET_JDK)
      targetCompatibility = JavaVersion.toVersion(TARGET_JDK)
    }
  }

  private companion object {
    const val TOOLCHAIN_JDK = 17
    const val TARGET_JDK = 11
  }
}
