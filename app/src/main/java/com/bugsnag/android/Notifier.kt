package com.bugsnag.android

import java.io.IOException

/**
 * Information about this library, including name and version.
 */
class Notifier @JvmOverloads constructor(
    var name: String = "Android Bugsnag Notifier",
    var version: String = "6.10.0",
    var url: String = "https://bugsnag.com"
) : JsonStream.Streamable {

    var dependencies = listOf<Notifier>()

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        writer.beginObject()
        writer.name("name").value(name)
        writer.name("version").value(version)
        writer.name("url").value(url)

        if (dependencies.isNotEmpty()) {
            writer.name("dependencies")
            writer.beginArray()
            dependencies.forEach { writer.value(it) }
            writer.endArray()
        }
        writer.endObject()
    }
}
