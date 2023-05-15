import app.cash.better.dynamic.features.DynamicImplementation

@DynamicImplementation
class ExampleImplementation : ExampleFeature {
  override fun print() {
    println("Hello World")
  }
}
