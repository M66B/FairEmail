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
    internal val store: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
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
            var tab = store[section]
            if (tab !is MutableMap<*, *>) {
                tab = ConcurrentHashMap<Any, Any>()
                store[section] = tab
            }
            insertValue(tab as MutableMap<String, Any>, key, value)
        }
    }

    private fun insertValue(map: MutableMap<String, Any>, key: String, newValue: Any) {
        var obj = newValue

        // only merge if both the existing and new value are maps
        val existingValue = map[key]
        if (obj is MutableMap<*, *> && existingValue is MutableMap<*, *>) {
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

        if (tab is MutableMap<*, *>) {
            tab.remove(key)

            if (tab.isEmpty()) {
                store.remove(section)
            }
        }
    }

    override fun getMetadata(section: String): Map<String, Any>? {
        return store[section] as (Map<String, Any>?)
    }

    override fun getMetadata(section: String, key: String): Any? {
        return when (val tab = store[section]) {
            is Map<*, *> -> (tab as Map<String, Any>?)!![key]
            else -> tab
        }
    }

    fun toMap(): ConcurrentHashMap<String, Any> {
        val hashMap = ConcurrentHashMap(store)

        // deep copy each section
        store.entries.forEach {
            if (it.value is ConcurrentHashMap<*, *>) {
                hashMap[it.key] = ConcurrentHashMap(it.value as ConcurrentHashMap<*, *>)
            }
        }
        return hashMap
    }

    companion object {
        fun merge(vararg data: Metadata): Metadata {
            val stores = data.map { it.toMap() }
            val redactKeys = data.flatMap { it.jsonStreamer.redactedKeys }
            val newMeta = Metadata(mergeMaps(stores))
            newMeta.redactedKeys = redactKeys.toSet()
            return newMeta
        }

        internal fun mergeMaps(data: List<Map<String, Any>>): ConcurrentHashMap<String, Any> {
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
            result: ConcurrentHashMap<String, Any>,
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
