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
