package com.bugsnag.android

import java.io.IOException

internal class FeatureFlags(
    internal val store: MutableMap<String, String?> = mutableMapOf()
) : JsonStream.Streamable, FeatureFlagAware {
    private val emptyVariant = "__EMPTY_VARIANT_SENTINEL__"

    @Synchronized override fun addFeatureFlag(name: String) {
        addFeatureFlag(name, null)
    }

    @Synchronized override fun addFeatureFlag(name: String, variant: String?) {
        store.remove(name)
        store[name] = variant ?: emptyVariant
    }

    @Synchronized override fun addFeatureFlags(featureFlags: Iterable<FeatureFlag>) {
        featureFlags.forEach { (name, variant) ->
            addFeatureFlag(name, variant)
        }
    }

    @Synchronized override fun clearFeatureFlag(name: String) {
        store.remove(name)
    }

    @Synchronized override fun clearFeatureFlags() {
        store.clear()
    }

    @Throws(IOException::class)
    override fun toStream(stream: JsonStream) {
        val storeCopy = synchronized(this) { store.toMap() }
        stream.beginArray()
        storeCopy.forEach { (name, variant) ->
            stream.beginObject()
            stream.name("featureFlag").value(name)
            if (variant != emptyVariant) {
                stream.name("variant").value(variant)
            }
            stream.endObject()
        }
        stream.endArray()
    }

    @Synchronized fun toList(): List<FeatureFlag> = store.entries.map { (name, variant) ->
        FeatureFlag(name, variant.takeUnless { it == emptyVariant })
    }

    @Synchronized fun copy() = FeatureFlags(store.toMutableMap())
}
