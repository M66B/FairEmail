package com.bugsnag.android

/**
 * Controls whether we should capture and serialize the state of all threads at the time
 * of an error.
 */
enum class ThreadSendPolicy {

    /**
     * Threads should be captured for all events.
     */
    ALWAYS,

    /**
     * Threads should be captured for unhandled events only.
     */
    UNHANDLED_ONLY,

    /**
     * Threads should never be captured.
     */
    NEVER;

    internal companion object {
        fun fromString(str: String) = values().find { it.name == str } ?: ALWAYS
    }
}
