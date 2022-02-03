package com.bugsnag.android.internal

import com.bugsnag.android.ObjectJsonStreamer
import com.bugsnag.android.repackaged.dslplatform.json.DslJson
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

internal class FallbackWriter : DslJson.Fallback<MutableMap<String, Any>> {

    private val placeholder = "\"${ObjectJsonStreamer.OBJECT_PLACEHOLDER}\"".toByteArray()

    override fun serialize(instance: Any?, stream: OutputStream) {
        stream.write(placeholder)
    }

    override fun deserialize(
        context: MutableMap<String, Any>?,
        manifest: Type,
        body: ByteArray,
        size: Int
    ): Any = throw UnsupportedOperationException()

    override fun deserialize(
        context: MutableMap<String, Any>?,
        manifest: Type,
        stream: InputStream
    ): Any = throw UnsupportedOperationException()
}
