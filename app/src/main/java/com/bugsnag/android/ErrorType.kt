package com.bugsnag.android

/**
 * Represents the type of error captured
 */
enum class ErrorType(internal val desc: String) {

    /**
     * An error captured from Android's JVM layer
     */
    ANDROID("android"),

    /**
     * An error captured from JavaScript
     */
    REACTNATIVEJS("reactnativejs"),

    /**
     * An error captured from Android's C layer
     */
    C("c"),

    /**
     * An error captured from a Dart / Flutter application
     */
    DART("dart");

    internal companion object {
        @JvmStatic
        @JvmName("fromDescriptor")
        internal fun fromDescriptor(desc: String) = values().find { it.desc == desc }
    }
}
