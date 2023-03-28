package app.cash.boxapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.boxapp.api.Navigator
import java.util.Stack

internal class MainActivity : ComponentActivity(), Navigator {
  init {
    Navigator.INSTANCE = this
  }

  private var goToListener: (@Composable () -> Unit) -> Unit = { }
  private var backStack = Stack<@Composable () -> Unit>().apply {
    push { HomeScreen() }
  }

  override fun goTo(screen: @Composable () -> Unit) {
    backStack.push(screen)
    goToListener(screen)
  }

  override fun onBackPressed() {
    if (backStack.size == 1) {
      super.onBackPressed()
    } else {
      backStack.pop() // Current screen.
      goToListener(backStack.peek())
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      var screen: @Composable () -> Unit by remember { mutableStateOf({ HomeScreen() }) }

      goToListener = {
        screen = it
      }

      screen()
    }
  }
}
