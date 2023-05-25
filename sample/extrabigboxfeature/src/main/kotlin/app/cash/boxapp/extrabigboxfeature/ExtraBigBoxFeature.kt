package app.cash.boxapp.extrabigboxfeature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.cash.better.dynamic.features.DynamicImplementation
import app.cash.boxapp.api.BoxAppFeature

@DynamicImplementation
class ExtraBigBoxFeature : BoxAppFeature {
  @Composable override fun Tile() {
    Box(
      Modifier
        .size(width = 500.dp, height = 100.dp)
        .background(Color(0, 222, 133))
        .padding(20.dp),
    ) {
      Text(
        buildAnnotatedString {
          withStyle(SpanStyle(fontWeight = FontWeight.Black)) {
            append("EXTRA")
          }
          append(" Big Box Feature!")
        },
      )
    }
  }
}
