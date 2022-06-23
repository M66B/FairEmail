package com.bugsnag.android

/**
 * Types of telemetry that may be sent to Bugsnag for product improvement purposes.
 */
enum class Telemetry {

    /**
     * Errors within the Bugsnag SDK.
     */
    INTERNAL_ERRORS;

    internal companion object {
        fun fromString(str: String) = values().find { it.name == str } ?: INTERNAL_ERRORS
    }
}
