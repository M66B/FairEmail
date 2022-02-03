package com.bugsnag.android.internal

import com.bugsnag.android.repackaged.dslplatform.json.DslJson
import com.bugsnag.android.repackaged.dslplatform.json.JsonWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Date

internal object JsonHelper {

    // ignore deprecation warnings as there is no other API that allows
    // serializing a placeholder for a type and all its subtypes
    @Suppress("deprecation")
    private val settings = DslJson.Settings<MutableMap<String, Any>>().fallbackTo(FallbackWriter())

    // Only one global DslJson is needed, and is thread-safe
    // Note: dsl-json adds about 150k to the final binary size.
    private val dslJson = DslJson(settings)

    init {
        dslJson.registerWriter(Date::class.java) { writer: JsonWriter, value: Date? ->
            value?.let {
                val timestamp = DateUtils.toIso8601(it)
                writer.writeString(timestamp)
            }
        }
    }

    fun serialize(value: Any, stream: OutputStream) {
        dslJson.serialize(value, stream)
    }

    fun serialize(value: Any, file: File) {
        val parentFile = file.parentFile
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw FileSystemException(file, null, "Could not create parent dirs of file")
            }
        }
        try {
            FileOutputStream(file).use { stream -> dslJson.serialize(value, stream) }
        } catch (ex: IOException) {
            throw IOException("Could not serialize JSON document to $file", ex)
        }
    }

    fun deserialize(bytes: ByteArray): MutableMap<String, Any> {
        val document = dslJson.deserialize(
            MutableMap::class.java,
            bytes,
            bytes.size
        )
        requireNotNull(document) { "JSON document is invalid" }
        @Suppress("UNCHECKED_CAST")
        return document as MutableMap<String, Any>
    }

    fun deserialize(stream: InputStream): MutableMap<in String, out Any> {
        val document = dslJson.deserialize(MutableMap::class.java, stream)
        requireNotNull(document) { "JSON document is invalid" }
        @Suppress("UNCHECKED_CAST")
        return document as MutableMap<String, Any>
    }

    fun deserialize(file: File): MutableMap<in String, out Any> {
        try {
            FileInputStream(file).use { stream -> return deserialize(stream) }
        } catch (ex: FileNotFoundException) {
            throw ex
        } catch (ex: IOException) {
            throw IOException("Could not deserialize from $file", ex)
        }
    }
}
