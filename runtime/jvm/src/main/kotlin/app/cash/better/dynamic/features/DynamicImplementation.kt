package app.cash.better.dynamic.features

import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Classes in a dynamic-feature module marked with this annotation that also implement the
 * [DynamicApi] interface will have code generated to access an instance of the class from an
 * application's base module.
 *
 * These instances can be accessed using the [dynamicImplementations] function.
 */
@Target(CLASS)
public annotation class DynamicImplementation
