package app.cash.better.dynamic.features.codegen

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface AndroidVariant : Named {
  companion object {
    val ANDROID_VARIANT_ATTRIBUTE: Attribute<AndroidVariant> = Attribute.of("androidVariant", AndroidVariant::class.java)
  }
}
