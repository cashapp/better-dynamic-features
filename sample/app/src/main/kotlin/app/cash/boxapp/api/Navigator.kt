package app.cash.boxapp.api

import androidx.compose.runtime.Composable

public interface Navigator {
  public fun goTo(screen: @Composable () -> Unit)

  public companion object {
    // Singleton for hack week!
    public lateinit var INSTANCE: Navigator
      internal set
  }
}