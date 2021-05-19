@file:JvmName("ThrowableUtils")
package com.bugsnag.android

/**
 * Unroll the list of causes for this Throwable, handling any recursion that may appear within
 * the chain. The first element returned will be this Throwable, and the last will be the root
 * cause or last non-recursive Throwable.
 */
internal fun Throwable.safeUnrollCauses(): List<Throwable> {
    val causes = LinkedHashSet<Throwable>()
    var currentEx: Throwable? = this

    // Set.add will return false if we have already "seen" currentEx
    while (currentEx != null && causes.add(currentEx)) {
        currentEx = currentEx.cause
    }

    return causes.toList()
}
