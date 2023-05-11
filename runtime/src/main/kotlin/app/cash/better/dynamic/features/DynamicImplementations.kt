package app.cash.better.dynamic.features

/**
 * Obtain a list of instances of [T] that were implemented in feature modules.
 *
 * TODO: This API is not finalized and may change drastically!
 */
@ExperimentalDynamicFeaturesApi
@Suppress("UNCHECKED_CAST")
public inline fun <reified T : DynamicApi> dynamicImplementations(): List<T> {
  val apiClassName = T::class.java.name

  val container = Class.forName("${apiClassName}ImplementationsContainer")
    .getDeclaredField("INSTANCE")
    .get(null) as ImplementationsContainer<T>

  return container.implementations
}
