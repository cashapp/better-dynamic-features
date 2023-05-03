package app.cash.better.dynamic.features.codegen.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeatureImplementation(
  val qualifiedName: String,
  val parentClass: FeatureApi,
)
