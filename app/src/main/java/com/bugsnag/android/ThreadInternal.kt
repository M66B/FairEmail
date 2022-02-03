package com.bugsnag.android

import java.io.IOException

class ThreadInternal internal constructor(
    var id: Long,
    var name: String,
    var type: ThreadType,
    val isErrorReportingThread: Boolean,
    var state: String,
    stacktrace: Stacktrace
) : JsonStream.Streamable {

    var stacktrace: MutableList<Stackframe> = stacktrace.trace.toMutableList()

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("id").value(id)
        writer.name("name").value(name)
        writer.name("type").value(type.desc)
        writer.name("state").value(state)

        writer.name("stacktrace")
        writer.beginArray()
        stacktrace.forEach { writer.value(it) }
        writer.endArray()

        if (isErrorReportingThread) {
            writer.name("errorReportingThread").value(true)
        }
        writer.endObject()
    }
}
