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
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GenerateImplementationsContainerTest {
  @Test
  fun `codegen for an api and implementations`() {
    val api = FeatureApi("test", "TestApi")
    val implementations = listOf(
      FeatureImplementation("test.TestImplementation1", api),
      FeatureImplementation("test.TestImplementation2", api),
    )

    val spec = generateImplementationsContainer(forApi = api, implementations)
    assertThat(spec.toString())
      .isEqualTo(
        """
        package test

        import app.cash.better.`dynamic`.features.ExperimentalDynamicFeaturesApi
        import app.cash.better.`dynamic`.features.ImplementationsContainer
        import java.lang.ClassNotFoundException
        import kotlin.OptIn
        import kotlin.collections.List
        import kotlin.collections.buildList

        @OptIn(ExperimentalDynamicFeaturesApi::class)
        public object TestApiImplementationsContainer : ImplementationsContainer<TestApi> {
          public override fun buildImplementations(): List<TestApi> = buildList {
            try {
              add(Class.forName("test.TestImplementation1").getDeclaredConstructor().newInstance() as
                  TestApi)
            } catch(e: ClassNotFoundException) {
            }
            try {
              add(Class.forName("test.TestImplementation2").getDeclaredConstructor().newInstance() as
                  TestApi)
            } catch(e: ClassNotFoundException) {
            }
          }
        }

        """.trimIndent(),
      )
  }
}
