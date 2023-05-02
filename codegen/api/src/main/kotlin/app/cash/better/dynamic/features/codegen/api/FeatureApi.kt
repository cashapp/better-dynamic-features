package app.cash.better.dynamic.features.codegen.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeatureApi(
  val packageName: String,
  val className: String,
)
