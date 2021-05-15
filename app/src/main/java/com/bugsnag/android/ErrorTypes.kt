package com.bugsnag.android

class ErrorTypes(

    /**
     * Sets whether [ANRs](https://developer.android.com/topic/performance/vitals/anr)
     * should be reported to Bugsnag.
     *
     * If you wish to disable ANR detection, you should set this property to false.
     */
    var anrs: Boolean = true,

    /**
     * Determines whether NDK crashes such as signals and exceptions should be reported by bugsnag.
     *
     * This flag is true by default.
     */
    var ndkCrashes: Boolean = true,

    /**
     * Sets whether Bugsnag should automatically capture and report unhandled errors.
     * By default, this value is true.
     */
    var unhandledExceptions: Boolean = true,

    /**
     * Sets whether Bugsnag should automatically capture and report unhandled promise rejections.
     * This only applies to React Native apps.
     * By default, this value is true.
     */
    var unhandledRejections: Boolean = true
) {
    internal constructor(detectErrors: Boolean) : this(detectErrors, detectErrors, detectErrors, detectErrors)

    internal fun copy() = ErrorTypes(anrs, ndkCrashes, unhandledExceptions, unhandledRejections)
}
