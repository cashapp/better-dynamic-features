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
package app.cash.boxapp.api

import kotlin.reflect.KType
import kotlin.reflect.typeOf

public interface ServiceRegistry<T> {
  public fun <R : T> install(instance: R)
  public fun whenInstalled(withService: (T) -> Unit)

  public companion object {
    private val services = linkedMapOf<KType, ServiceRegistry<*>>()

    public fun <T> of(type: KType): ServiceRegistry<T> {
      return services.getOrPut(type) {
        object : ServiceRegistry<T> {
          private var value: T? = null
          private val runWhenInstalledQueue = ArrayDeque<(T) -> Unit>()

          override fun <R : T> install(instance: R) {
            value = instance
            exhaustRunWhenInstalledQueue()
          }

          override fun whenInstalled(withService: (T) -> Unit) {
            runWhenInstalledQueue.add(withService)
            exhaustRunWhenInstalledQueue()
          }

          private fun exhaustRunWhenInstalledQueue() {
            val instance = value ?: return
            runWhenInstalledQueue.removeAll {
              it(instance)
              true
            }
          }
        }
      } as ServiceRegistry<T>
    }

    public inline fun <reified T> of(): ServiceRegistry<T> = of(typeOf<T>())
  }
}
