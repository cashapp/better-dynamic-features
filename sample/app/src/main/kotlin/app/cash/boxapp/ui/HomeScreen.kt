package app.cash.boxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.boxapp.api.BoxAppFeature
import java.util.ServiceLoader

@Composable internal fun HomeScreen() {
  Column(
    modifier = Modifier
      .padding(10.dp)
      .background(color = Color(196, 255, 233)),
  ) {
    // The "My Boxes" tab.
    Row(modifier = Modifier.weight(1f)) {
      Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        ServiceLoader.load(BoxAppFeature::class.java).forEach {
          it.Tile()
        }
      }
    }

    // The tabs
    Row(
      modifier = Modifier
        .padding(10.dp)
        .background(color = Color(128, 255, 206))
        .padding(10.dp),
    ) {
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(Color(83, 163, 133)),
      ) {
        Text(
          text = "A",
          modifier = Modifier.align(Alignment.Center),
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(Color(83, 163, 133)),
      ) {
        Text(
          text = "B",
          modifier = Modifier.align(Alignment.Center),
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(Color(83, 163, 133)),
      ) {
        Text(
          text = "C",
          modifier = Modifier.align(Alignment.Center),
        )
      }
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}
