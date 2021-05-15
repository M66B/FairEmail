package com.bugsnag.android

/**
 * Logs internal messages from within the bugsnag notifier.
 */
interface Logger {

    /**
     * Logs a message at the error level.
     */
    fun e(msg: String): Unit = Unit

    /**
     * Logs a message at the error level.
     */
    fun e(msg: String, throwable: Throwable): Unit = Unit

    /**
     * Logs a message at the warning level.
     */
    fun w(msg: String): Unit = Unit

    /**
     * Logs a message at the warning level.
     */
    fun w(msg: String, throwable: Throwable): Unit = Unit

    /**
     * Logs a message at the info level.
     */
    fun i(msg: String): Unit = Unit

    /**
     * Logs a message at the info level.
     */
    fun i(msg: String, throwable: Throwable): Unit = Unit

    /**
     * Logs a message at the debug level.
     */
    fun d(msg: String): Unit = Unit

    /**
     * Logs a message at the debug level.
     */
    fun d(msg: String, throwable: Throwable): Unit = Unit
}
