package com.bugsnag.android

/**
 * Represents the type of thread captured
 */
enum class ThreadType(internal val desc: String) {

    /**
     * A thread captured from Android's JVM layer
     */
    ANDROID("android"),

    /**
     * A thread captured from Android's NDK layer
     */
    C("c"),

    /**
     * A thread captured from JavaScript
     */
    REACTNATIVEJS("reactnativejs")
}
