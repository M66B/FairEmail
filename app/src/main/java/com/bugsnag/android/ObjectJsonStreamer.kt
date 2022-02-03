package com.bugsnag.android

import com.bugsnag.android.internal.DateUtils
import java.io.IOException
import java.lang.reflect.Array
import java.util.Date

internal class ObjectJsonStreamer {

    companion object {
        internal const val REDACTED_PLACEHOLDER = "[REDACTED]"
        internal const val OBJECT_PLACEHOLDER = "[OBJECT]"
    }

    var redactedKeys = setOf("password")

    // Write complex/nested values to a JsonStreamer
    @Throws(IOException::class)
    fun objectToStream(obj: Any?, writer: JsonStream, shouldRedactKeys: Boolean = false) {
        when {
            obj == null -> writer.nullValue()
            obj is String -> writer.value(obj)
            obj is Number -> writer.value(obj)
            obj is Boolean -> writer.value(obj)
            obj is JsonStream.Streamable -> obj.toStream(writer)
            obj is Date -> writer.value(DateUtils.toIso8601(obj))
            obj is Map<*, *> -> mapToStream(writer, obj, shouldRedactKeys)
            obj is Collection<*> -> collectionToStream(writer, obj)
            obj.javaClass.isArray -> arrayToStream(writer, obj)
            else -> writer.value(OBJECT_PLACEHOLDER)
        }
    }

    private fun mapToStream(writer: JsonStream, obj: Map<*, *>, shouldRedactKeys: Boolean) {
        writer.beginObject()
        obj.entries.forEach {
            val keyObj = it.key
            if (keyObj is String) {
                writer.name(keyObj)
                if (shouldRedactKeys && isRedactedKey(keyObj)) {
                    writer.value(REDACTED_PLACEHOLDER)
                } else {
                    objectToStream(it.value, writer, shouldRedactKeys)
                }
            }
        }
        writer.endObject()
    }

    private fun collectionToStream(writer: JsonStream, obj: Collection<*>) {
        writer.beginArray()
        obj.forEach { objectToStream(it, writer) }
        writer.endArray()
    }

    private fun arrayToStream(writer: JsonStream, obj: Any) {
        // Primitive array objects
        writer.beginArray()
        val length = Array.getLength(obj)
        var i = 0
        while (i < length) {
            objectToStream(Array.get(obj, i), writer)
            i += 1
        }
        writer.endArray()
    }

    // Should this key be redacted
    private fun isRedactedKey(key: String) = redactedKeys.any { key.contains(it) }
}
