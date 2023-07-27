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
package app.cash.boxapp.install

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.ktx.requestInstall
import com.google.android.play.core.ktx.status
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Composable
internal fun rememberSplitInstallHelper(): SplitInstallHelper {
  val context = LocalContext.current
  return remember { SplitInstallHelper(context) }
}

internal class SplitInstallHelper(
  context: Context,
  val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(context),
) {
  val isInstalling: Flow<Boolean> = callbackFlow {
    send(false)
    val listener = SplitInstallStateUpdatedListener {
      when (it.status) {
        SplitInstallSessionStatus.PENDING,
        SplitInstallSessionStatus.DOWNLOADING,
        SplitInstallSessionStatus.DOWNLOADED,
        SplitInstallSessionStatus.INSTALLING,
        -> trySend(true)

        else -> trySend(false)
      }
    }

    splitInstallManager.registerListener(listener)
    awaitClose {
      splitInstallManager.unregisterListener(listener)
    }
  }

  suspend fun requestInstall(module: Module) {
    splitInstallManager.requestInstall(modules = listOf(module.id))
  }
}
