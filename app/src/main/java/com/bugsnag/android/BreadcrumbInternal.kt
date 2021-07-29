package com.bugsnag.android

import java.io.IOException
import java.util.Date

/**
 * In order to understand what happened in your application before each crash, it can be helpful
 * to leave short log statements that we call breadcrumbs. Breadcrumbs are
 * attached to a crash to help diagnose what events lead to the error.
 */
internal class BreadcrumbInternal internal constructor(
    @JvmField var message: String,
    @JvmField var type: BreadcrumbType,
    @JvmField var metadata: MutableMap<String, Any?>?,
    @JvmField val timestamp: Date = Date()
) : JsonStream.Streamable { // JvmField allows direct field access optimizations

    internal constructor(message: String) : this(
        message,
        BreadcrumbType.MANUAL,
        mutableMapOf(),
        Date()
    )

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("timestamp").value(timestamp)
        writer.name("name").value(message)
        writer.name("type").value(type.toString())
        writer.name("metaData")
        writer.value(metadata, true)
        writer.endObject()
    }
}
