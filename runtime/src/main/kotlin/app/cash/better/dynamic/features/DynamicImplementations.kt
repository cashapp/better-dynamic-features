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
