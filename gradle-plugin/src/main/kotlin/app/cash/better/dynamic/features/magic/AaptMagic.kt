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
package app.cash.better.dynamic.features.magic

/**
 * Right now we do a bunch of magical nonsense to fix an AAPT bug.
 * We want to remove this magic ASAP, and this will help make it easier to find and delete that
 * magic in the future.
 *
 * [https://issuetracker.google.com/issues/275748380](https://issuetracker.google.com/issues/275748380)
 */
@RequiresOptIn
annotation class AaptMagic
