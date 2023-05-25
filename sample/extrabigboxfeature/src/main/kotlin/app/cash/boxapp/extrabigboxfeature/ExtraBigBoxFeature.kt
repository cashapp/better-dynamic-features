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
