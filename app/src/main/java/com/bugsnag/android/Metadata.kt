@file:Suppress("UNCHECKED_CAST")

package com.bugsnag.android

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * A container for additional diagnostic information you'd like to send with
 * every error report.
 *
 * Diagnostic information is presented on your Bugsnag dashboard in tabs.
 */
internal data class Metadata @JvmOverloads constructor(
    internal val store: MutableMap<String, MutableMap<String, Any>> = ConcurrentHashMap()
) : JsonStream.Streamable, MetadataAware {

    val jsonStreamer: ObjectJsonStreamer = ObjectJsonStreamer()

    var redactedKeys: Set<String>
        get() = jsonStreamer.redactedKeys
        set(value) {
            jsonStreamer.redactedKeys = value
        }

    @Throws(IOException::class)
    override fun toStream(writer: JsonStream) {
        jsonStreamer.objectToStream(store, writer, true)
    }

    override fun addMetadata(section: String, value: Map<String, Any?>) {
        value.entries.forEach {
            addMetadata(section, it.key, it.value)
        }
    }

    override fun addMetadata(section: String, key: String, value: Any?) {
        if (value == null) {
            clearMetadata(section, key)
        } else {
            val tab = store[section] ?: ConcurrentHashMap()
            store[section] = tab
            insertValue(tab, key, value)
        }
    }

    private fun insertValue(map: MutableMap<String, Any>, key: String, newValue: Any) {
        var obj = newValue

        // only merge if both the existing and new value are maps
        val existingValue = map[key]
        if (existingValue != null && obj is Map<*, *>) {
            val maps = listOf(existingValue as Map<String, Any>, newValue as Map<String, Any>)
            obj = mergeMaps(maps)
        }
        map[key] = obj
    }

    override fun clearMetadata(section: String) {
        store.remove(section)
    }

    override fun clearMetadata(section: String, key: String) {
        val tab = store[section]
        tab?.remove(key)

        if (tab.isNullOrEmpty()) {
            store.remove(section)
        }
    }

    override fun getMetadata(section: String): Map<String, Any>? {
        return store[section]
    }

    override fun getMetadata(section: String, key: String): Any? {
        return getMetadata(section)?.get(key)
    }

    fun toMap(): MutableMap<String, MutableMap<String, Any>> {
        val copy = ConcurrentHashMap(store)

        // deep copy each section
        store.entries.forEach {
            copy[it.key] = ConcurrentHashMap(it.value)
        }
        return copy
    }

    companion object {
        fun merge(vararg data: Metadata): Metadata {
            val stores = data.map { it.toMap() }
            val redactKeys = data.flatMap { it.jsonStreamer.redactedKeys }
            val newMeta = Metadata(mergeMaps(stores) as MutableMap<String, MutableMap<String, Any>>)
            newMeta.redactedKeys = redactKeys.toSet()
            return newMeta
        }

        internal fun mergeMaps(data: List<Map<String, Any>>): MutableMap<String, Any> {
            val keys = data.flatMap { it.keys }.toSet()
            val result = ConcurrentHashMap<String, Any>()

            for (map in data) {
                for (key in keys) {
                    getMergeValue(result, key, map)
                }
            }
            return result
        }

        private fun getMergeValue(
            result: MutableMap<String, Any>,
            key: String,
            map: Map<String, Any>
        ) {
            val baseValue = result[key]
            val overridesValue = map[key]

            if (overridesValue != null) {
                if (baseValue is Map<*, *> && overridesValue is Map<*, *>) {
                    // Both original and overrides are Maps, go deeper
                    val first = baseValue as Map<String, Any>?
                    val second = overridesValue as Map<String, Any>?
                    result[key] = mergeMaps(listOf(first!!, second!!))
                } else {
                    result[key] = overridesValue
                }
            } else {
                if (baseValue != null) { // No collision, just use base value
                    result[key] = baseValue
                }
            }
        }
    }

    fun copy(): Metadata {
        return this.copy(store = toMap())
            .also { it.redactedKeys = redactedKeys.toSet() }
    }
}
