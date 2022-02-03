package com.bugsnag.android

/**
 * Recognized types of breadcrumbs
 */
enum class BreadcrumbType(private val type: String) {
    /**
     * An error was sent to Bugsnag (internal use only)
     */
    ERROR("error"),
    /**
     * A log message
     */
    LOG("log"),
    /**
     * A manual invocation of `leaveBreadcrumb` (default)
     */
    MANUAL("manual"),
    /**
     * A navigation event, such as a window opening or closing
     */
    NAVIGATION("navigation"),
    /**
     * A background process such as a database query
     */
    PROCESS("process"),
    /**
     * A network request
     */
    REQUEST("request"),
    /**
     * A change in application state, such as launch or memory warning
     */
    STATE("state"),
    /**
     * A user action, such as tapping a button
     */
    USER("user");

    override fun toString() = type

    internal companion object {
        internal fun fromDescriptor(type: String) = values().singleOrNull { it.type == type }
    }
}
