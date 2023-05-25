package app.cash.better.dynamic.features

@ExperimentalDynamicFeaturesApi
public interface ImplementationsContainer<T : DynamicApi> {
  public fun buildImplementations(): List<T>
}
