/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.boxapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.cash.better.dynamic.features.ExperimentalDynamicFeaturesApi
import app.cash.better.dynamic.features.dynamicImplementations
import app.cash.boxapp.api.BoxAppFeature
import com.google.android.play.core.ktx.requestInstall
import com.google.android.play.core.ktx.status
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalDynamicFeaturesApi::class)
@Composable
internal fun HomeScreen() {
  val context = LocalContext.current
  val splitInstallManager = remember { SplitInstallManagerFactory.create(context) }
  val features by remember { splitInstallManager.dynamicImplementations<BoxAppFeature>() }
    .collectAsState(initial = emptyList())

  val isSplitInstalling by remember {
    callbackFlow {
      send(false)
      val listener = SplitInstallStateUpdatedListener {
        when (it.status) {
          SplitInstallSessionStatus.PENDING, SplitInstallSessionStatus.DOWNLOADING, SplitInstallSessionStatus.DOWNLOADED, SplitInstallSessionStatus.INSTALLING -> trySend(
            true,
          )

          else -> trySend(false)
        }
      }

      splitInstallManager.registerListener(listener)
      awaitClose {
        splitInstallManager.unregisterListener(listener)
      }
    }
  }.collectAsState(initial = false)

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
        features.forEach {
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

    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Button(onClick = {
        scope.launch {
          splitInstallManager.requestInstall(modules = listOf("extrabigboxfeature"))
        }
      }, colors = ButtonDefaults.buttonColors(Color(0, 222, 133)),) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = buildAnnotatedString {
              append("Install ")
              withStyle(SpanStyle(fontWeight = FontWeight.Black)) {
                append("EXTRA")
              }
              append(" Big Box")
            },
          )
          AnimatedVisibility(visible = isSplitInstalling) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = MaterialTheme.colors.onBackground,
              strokeWidth = 2.dp,
            )
          }
        }
      }
    }
  }
}
