package app.cash.boxapp.bigboxfeature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.cash.boxapp.api.ServiceRegistry
import app.cash.boxapp.bigboxfeature.api.BigBoxMainScreen

private object BigBoxScreenWidgets : BigBoxMainScreen {
  val widgets = mutableListOf<@Composable () -> Unit>()

  init {
    ServiceRegistry.of<BigBoxMainScreen>().install(this)
  }

  override fun registerWidget(widget: @Composable () -> Unit) {
    widgets.add(widget)
  }
}

@Composable fun BigBoxScreen() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(255, 222, 133)),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Text(text = "Big Box Main Screen!")
    Spacer(modifier = Modifier.weight(1f))

    BigBoxScreenWidgets.widgets.forEach {
      it()
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}
