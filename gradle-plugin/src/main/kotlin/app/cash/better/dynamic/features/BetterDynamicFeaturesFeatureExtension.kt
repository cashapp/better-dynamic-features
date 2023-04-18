// Copyright Square, Inc.
package app.cash.better.dynamic.features

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class BetterDynamicFeaturesFeatureExtension {
  abstract val enableResourceRewriting: Property<Boolean>

  abstract val baseProject: Property<Project>
}
