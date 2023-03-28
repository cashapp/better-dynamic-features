package app.cash.boxapp.bigboxfeature.api

import androidx.compose.runtime.Composable

interface BigBoxMainScreen {
  fun registerWidget(widget: @Composable () -> Unit)
}