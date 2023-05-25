package app.cash.boxapp.api

import androidx.compose.runtime.Composable
import app.cash.better.dynamic.features.DynamicApi

public interface BoxAppFeature : DynamicApi {
  @Composable public fun Tile()
}
