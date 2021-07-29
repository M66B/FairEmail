package com.bugsnag.android

import com.bugsnag.android.internal.ImmutableConfig
import java.io.File
import java.io.IOException

/**
 * An error report payload.
 *
 * This payload contains an error report and identifies the source application
 * using your API key.
 */
class EventPayload @JvmOverloads internal constructor(
    var apiKey: String?,
    val event: Event? = null,
    internal val eventFile: File? = null,
    notifier: Notifier,
    private val config: ImmutableConfig
) : JsonStream.Streamable {

    internal val notifier = Notifier(notifier.name, notifier.version, notifier.url).apply {
        dependencies = notifier.dependencies.toMutableList()
    }

    internal fun getErrorTypes(): Set<ErrorType> {
        return when {
            event != null -> event.impl.getErrorTypesFromStackframes()
            eventFile != null -> EventFilenameInfo.fromFile(eventFile, config).errorTypes
            else -> emptySet()
        }
    }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("apiKey").value(apiKey)
        writer.name("payloadVersion").value("4.0")
        writer.name("notifier").value(notifier)
        writer.name("events").beginArray()

        when {
            event != null -> writer.value(event)
            eventFile != null -> writer.value(eventFile)
            else -> Unit
        }

        writer.endArray()
        writer.endObject()
    }
}
