package com.bugsnag.android

import java.io.IOException

/**
 * Represents a single native stackframe
 */
class NativeStackframe internal constructor(

    /**
     * The name of the method that was being executed
     */
    var method: String?,

    /**
     * The location of the source file
     */
    var file: String?,

    /**
     * The line number within the source file this stackframe refers to
     */
    var lineNumber: Number?,

    /**
     * The address of the instruction where the event occurred.
     */
    var frameAddress: Long?,

    /**
     * The address of the function where the event occurred.
     */
    var symbolAddress: Long?,

    /**
     * The address of the library where the event occurred.
     */
    var loadAddress: Long?
) : JsonStream.Streamable {

    /**
     * The type of the error
     */
    var type: ErrorType? = ErrorType.C

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("method").value(method)
        writer.name("file").value(file)
        writer.name("lineNumber").value(lineNumber)
        writer.name("frameAddress").value(frameAddress)
        writer.name("symbolAddress").value(symbolAddress)
        writer.name("loadAddress").value(loadAddress)

        type?.let {
            writer.name("type").value(it.desc)
        }
        writer.endObject()
    }
}
