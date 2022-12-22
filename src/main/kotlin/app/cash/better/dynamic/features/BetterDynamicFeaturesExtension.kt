// Copyright Square, Inc.
package app.cash.better.dynamic.features

import groovy.lang.Closure
import org.gradle.util.internal.ConfigureUtil

open class BetterDynamicFeaturesExtension {
  internal val externalStyles = mutableListOf<String>()

  fun externalResources(block: Closure<ExternalResourcesDsl>) {
    val dsl = ExternalResourcesDsl()
    ConfigureUtil.configure(block, dsl)

    externalStyles += dsl.styles
  }

  fun externalResources(block: ExternalResourcesDsl.() -> Unit) {
    externalStyles += ExternalResourcesDsl().apply(block).styles
  }

  class ExternalResourcesDsl {
    internal val styles = mutableListOf<String>()

    fun style(name: String) {
      styles += name
    }
  }
}
