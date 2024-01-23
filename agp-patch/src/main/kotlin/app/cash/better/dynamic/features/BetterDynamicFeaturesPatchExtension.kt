package app.cash.better.dynamic.features

open class BetterDynamicFeaturesPatchExtension {
  internal val ignoredVersions = mutableSetOf<String>()

  fun suppressVersionCheck(version: String) {
    ignoredVersions += version
  }
}
