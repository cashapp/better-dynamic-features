package app.cash.boxapp.smallboxfeature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable fun SmallBoxWidget() {
  Box(
    modifier = Modifier.size(200.dp, 100.dp)
      .background(Color.White),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = "Small Box Widget")
  }
}
