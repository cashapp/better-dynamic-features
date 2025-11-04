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
package app.cash.better.dynamic.features.utils

import com.google.common.truth.Fact.simpleFact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout

class SequenceSubject<T>(metadata: FailureMetadata, private val actual: Sequence<T>) : Subject(metadata, actual) {
  fun containsInConsecutiveOrder(vararg values: T) {
    var matchCounter = 0
    actual.forEach {
      if (matchCounter == values.size) return@forEach
      if (it == values[matchCounter]) {
        matchCounter++
      } else {
        matchCounter = 0
      }
    }

    if (matchCounter != values.size) {
      failWithoutActual(
        simpleFact(
          "Expected to find ${
            values.joinToString(
              prefix = "[",
              postfix = "]",
            )
          } in consecutive order, but they were not found.",
        ),
      )
    }
  }

  companion object {
    @JvmStatic
    fun <T> sequences(): Factory<SequenceSubject<T>, Sequence<T>> {
      return Factory { metadata, actual ->
        SequenceSubject(metadata, actual as Sequence<T>)
      }
    }
  }
}

fun <T> assertThat(actual: Sequence<T>): SequenceSubject<T> = assertAbout(SequenceSubject.sequences<T>()).that(actual)
