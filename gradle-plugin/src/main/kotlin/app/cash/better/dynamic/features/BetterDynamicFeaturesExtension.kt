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
