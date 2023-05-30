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
package app.cash.better.dynamic.features.codegen

import app.cash.better.dynamic.features.codegen.api.FeatureApi
import app.cash.better.dynamic.features.codegen.api.FeatureImplementation
import com.squareup.kotlinpoet.ClassName

fun generateProguardRules(
  forApi: FeatureApi,
  implementations: List<FeatureImplementation>,
): String = buildString {
  val forApiClass = ClassName(forApi.packageName, forApi.className)
  // Keep feature API interface itself
  appendLine("-if class ${forApiClass.canonicalName}")
  appendLine("-keepnames class ${forApiClass.canonicalName}")
  // Keep generated implementations container looked up by reflection
  appendLine("-if class ${forApiClass.canonicalName}ImplementationsContainer")
  appendLine("-keep class ${forApiClass.canonicalName}ImplementationsContainer { *; }")

  // Keep each implementation class and its empty constructor
  implementations.forEach { implementation ->
    appendLine("-if class ${implementation.qualifiedName}")
    appendLine("-keep class ${implementation.qualifiedName} {")
    appendLine("    public <init>();")
    appendLine("}")
  }
}
