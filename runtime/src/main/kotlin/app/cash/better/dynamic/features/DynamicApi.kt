// Copyright Square, Inc.
package app.cash.better.dynamic.features

/**
 * A base interface for classes defined in base modules with implementations that live in feature
 * modules.
 *
 * Your class should implement this interface, and each implementation should be annotated with
 * @[DynamicImplementation] to be made accessible to the base module at runtime.
 */
public interface DynamicApi
