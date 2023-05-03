package app.cash.better.dynamic.features.utils

import com.google.common.truth.Fact.simpleFact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth.assertAbout

class SequenceSubject<T>(metadata: FailureMetadata, private val actual: Sequence<T>) :
  Subject(metadata, actual) {
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
    fun <T> sequences(): Factory<SequenceSubject<T>, Sequence<T>> = Factory(::SequenceSubject)
  }
}

fun <T> assertThat(actual: Sequence<T>): SequenceSubject<T> =
  assertAbout(SequenceSubject.sequences<T>()).that(actual)
