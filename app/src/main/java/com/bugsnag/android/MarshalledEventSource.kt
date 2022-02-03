package com.bugsnag.android

import com.bugsnag.android.internal.JsonHelper
import java.io.File

internal class MarshalledEventSource(
    private val eventFile: File,
    private val apiKey: String,
    private val logger: Logger
) : () -> Event {

    /**
     * The parsed and possibly processed event. This field remains `null` if the `EventSource`
     * is not used, and may not reflect the same data as is stored in `eventFile` (as the `Event`
     * is mutable, and may have been modified after loading).
     */
    var event: Event? = null
        private set

    override fun invoke(): Event {
        var unmarshalledEvent = event
        if (unmarshalledEvent == null) {
            unmarshalledEvent = unmarshall()
            event = unmarshalledEvent
        }

        return unmarshalledEvent
    }

    fun clear() {
        event = null
    }

    private fun unmarshall(): Event {
        val eventMapper = BugsnagEventMapper(logger)
        val jsonMap = JsonHelper.deserialize(eventFile)
        return Event(
            eventMapper.convertToEventImpl(jsonMap, apiKey),
            logger
        )
    }
}
