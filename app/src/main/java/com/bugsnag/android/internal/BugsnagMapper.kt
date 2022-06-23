package com.bugsnag.android.internal

import com.bugsnag.android.BugsnagEventMapper
import com.bugsnag.android.Event
import com.bugsnag.android.JsonStream
import com.bugsnag.android.Logger
import java.io.ByteArrayOutputStream
import com.bugsnag.android.Error as BugsnagError

class BugsnagMapper(logger: Logger) {
    private val eventMapper = BugsnagEventMapper(logger)

    /**
     * Convert the given `Map` of data to an `Event` object
     */
    fun convertToEvent(data: Map<in String, Any?>, fallbackApiKey: String): Event {
        return eventMapper.convertToEvent(data, fallbackApiKey)
    }

    /**
     * Convert the given `Map` of data to an `Error` object
     */
    fun convertToError(data: Map<in String, Any?>): BugsnagError {
        return eventMapper.convertError(data)
    }

    /**
     * Convert a given `Event` object to a `Map<String, Any?>`
     */
    fun convertToMap(event: Event): Map<in String, Any?> {
        val byteStream = ByteArrayOutputStream()
        byteStream.writer().use { writer -> JsonStream(writer).value(event) }
        return JsonHelper.deserialize(byteStream.toByteArray())
    }

    /**
     * Convert a given `Error` object to a `Map<String, Any?>`
     */
    fun convertToMap(error: BugsnagError): Map<in String, Any?> {
        val byteStream = ByteArrayOutputStream()
        byteStream.writer().use { writer -> JsonStream(writer).value(error) }
        return JsonHelper.deserialize(byteStream.toByteArray())
    }
}
