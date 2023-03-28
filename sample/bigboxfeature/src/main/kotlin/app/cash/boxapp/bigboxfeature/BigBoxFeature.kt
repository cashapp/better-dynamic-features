package app.cash.boxapp.bigboxfeature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.boxapp.api.BoxAppFeature
import app.cash.boxapp.api.Navigator

class BigBoxFeature : BoxAppFeature {
  private val navigator get() = Navigator.INSTANCE

  @Composable override fun Tile() {
    Box(
      Modifier
        .size(width = 200.dp, height = 60.dp)
        .background(Color(255, 222, 133))
        .padding(20.dp)
        .clickable { navigator.goTo { BigBoxScreen() } }
    ) {
      Text("Big Box Feature!")
    }
  }
}