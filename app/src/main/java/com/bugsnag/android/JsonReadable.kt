package com.bugsnag.android

import android.util.JsonReader

/**
 * Classes which implement this interface are capable of deserializing a JSON input.
 */
internal interface JsonReadable<T : JsonStream.Streamable> {

    /**
     * Constructs an object from a JSON input.
     */
    fun fromReader(reader: JsonReader): T
}
