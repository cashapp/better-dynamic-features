/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
