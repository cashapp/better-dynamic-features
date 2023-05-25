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

import com.google.android.play.core.ktx.status
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Obtain a list of instances of [T] that were implemented in feature modules.
 *
 * TODO: This API is not finalized and may change drastically!
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalDynamicFeaturesApi
@Suppress("UNCHECKED_CAST")
public inline fun <reified T : DynamicApi> SplitInstallManager.dynamicImplementations(): Flow<List<T>> {
  val apiClassName = T::class.java.name

  val container = Class.forName("${apiClassName}ImplementationsContainer")
    .getDeclaredField("INSTANCE")
    .get(null) as ImplementationsContainer<T>

  return callbackFlow {
    send(container.buildImplementations())
    val listener = SplitInstallStateUpdatedListener {
      if (it.status == SplitInstallSessionStatus.INSTALLED) {
        trySend(container.buildImplementations())
      }
    }

    this@dynamicImplementations.registerListener(listener)
    awaitClose {
      this@dynamicImplementations.unregisterListener(listener)
    }
  }
}
