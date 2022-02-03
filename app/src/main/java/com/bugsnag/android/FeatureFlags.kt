package com.bugsnag.android

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

internal class FeatureFlags(
    internal val store: MutableMap<String, String?> = ConcurrentHashMap()
) : JsonStream.Streamable, FeatureFlagAware {
    private val emptyVariant = "__EMPTY_VARIANT_SENTINEL__"

    override fun addFeatureFlag(name: String) {
        store[name] = emptyVariant
    }

    override fun addFeatureFlag(name: String, variant: String?) {
        store[name] = variant ?: emptyVariant
    }

    override fun addFeatureFlags(featureFlags: Iterable<FeatureFlag>) {
        featureFlags.forEach { (name, variant) ->
            addFeatureFlag(name, variant)
        }
    }

    override fun clearFeatureFlag(name: String) {
        store.remove(name)
    }

    override fun clearFeatureFlags() {
        store.clear()
    }

    @Throws(IOException::class)
    override fun toStream(stream: JsonStream) {
        stream.beginArray()
        store.forEach { (name, variant) ->
            stream.beginObject()
            stream.name("featureFlag").value(name)
            if (variant != emptyVariant) {
                stream.name("variant").value(variant)
            }
            stream.endObject()
        }
        stream.endArray()
    }

    fun toList(): List<FeatureFlag> = store.entries.map { (name, variant) ->
        FeatureFlag(name, variant.takeUnless { it == emptyVariant })
    }

    fun copy() = FeatureFlags(store.toMutableMap())
}
