pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}
rootProject.name = "Attr"
include(":app")
include(":feature")

includeBuild("/Users/derekellis/projects/better-dynamic-features") {
  dependencySubstitution {
    substitute(module("app.cash.better.dynamic.features:app.cash.better.dynamic.features.gradle.plugin")).using(project(":gradle-plugin"))
  }
}
