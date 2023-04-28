package app.cash.better.dynamic.features

@ExperimentalDynamicFeaturesApi
public interface ImplementationsContainer<T : DynamicApi> {
  public val implementations: List<T>
}
