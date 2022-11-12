package com.bugsnag.android.internal

import java.util.EnumMap
import java.util.Hashtable
import java.util.LinkedList
import java.util.TreeMap
import java.util.Vector
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList

internal object StringUtils {
    private const val trimMessageLength = "***<9> CHARS TRUNCATED***".length

    fun stringTrimmedTo(maxLength: Int, str: String): String {
        val excessCharCount = str.length - maxLength
        return when {
            excessCharCount < trimMessageLength -> str
            else -> "${str.substring(0, maxLength)}***<$excessCharCount> CHARS TRUNCATED***"
        }
    }

    @Suppress("unchecked_cast")
    fun trimStringValuesTo(maxStringLength: Int, list: MutableList<Any?>): TrimMetrics {
        var stringCount = 0
        var charCount = 0

        repeat(list.size) { index ->
            trimValue(maxStringLength, list[index]) { newValue, stringTrimmed, charsTrimmed ->
                list[index] = newValue
                stringCount += stringTrimmed
                charCount += charsTrimmed
            }
        }

        return TrimMetrics(stringCount, charCount)
    }

    @Suppress("unchecked_cast")
    fun trimStringValuesTo(maxStringLength: Int, map: MutableMap<String, Any?>): TrimMetrics {
        var stringCount = 0
        var charCount = 0
        map.entries.forEach { entry ->
            trimValue(maxStringLength, entry.value) { newValue, stringTrimmed, charsTrimmed ->
                entry.setValue(newValue)
                stringCount += stringTrimmed
                charCount += charsTrimmed
            }
        }

        return TrimMetrics(stringCount, charCount)
    }

    @Suppress("unchecked_cast")
    private inline fun trimValue(
        maxStringLength: Int,
        value: Any?,
        update: (newValue: Any, stringTrimmed: Int, charsTrimmed: Int) -> Unit
    ) {
        if (value is String && value.length > maxStringLength) {
            update(stringTrimmedTo(maxStringLength, value), 1, value.length - maxStringLength)
        } else if (value.isDefinitelyMutableMap()) {
            val (innerStringCount, innerCharCount) = trimStringValuesTo(
                maxStringLength,
                value as MutableMap<String, Any?>
            )

            update(value, innerStringCount, innerCharCount)
        } else if (value.isDefinitelyMutableList()) {
            val (innerStringCount, innerCharCount) = trimStringValuesTo(
                maxStringLength,
                value as MutableList<Any?>
            )

            update(value, innerStringCount, innerCharCount)
        } else if (value is Map<*, *>) {
            val newValue = value.toMutableMap() as MutableMap<String, Any?>
            val (innerStringCount, innerCharCount) = trimStringValuesTo(maxStringLength, newValue)
            update(newValue, innerStringCount, innerCharCount)
        } else if (value is Collection<*>) {
            val newValue = value.toMutableList()
            val (innerStringCount, innerCharCount) = trimStringValuesTo(maxStringLength, newValue)
            update(newValue, innerStringCount, innerCharCount)
        }
    }

    /**
     * In order to avoid surprises we have a small list of commonly used Map types that are known
     * to be mutable (avoiding issues around Kotlin trying to determine whether
     * `Collections.singletonMap` (and such) is mutable or not).
     *
     * It is technically possible that a HashMap was extended to be immutable, but it's unlikely.
     */
    private fun Any?.isDefinitelyMutableMap() =
        this is HashMap<*, *> ||
            this is TreeMap<*, *> ||
            this is ConcurrentMap<*, *> || // concurrent automatically implies mutability
            this is EnumMap<*, *> ||
            this is Hashtable<*, *> ||
            this is WeakHashMap<*, *>

    private fun Any?.isDefinitelyMutableList() =
        this is ArrayList<*> ||
            this is LinkedList<*> ||
            this is CopyOnWriteArrayList<*> ||
            this is Vector<*>
}
