package com.bugsnag.android

import java.io.IOException
import kotlin.math.max

internal class FeatureFlags private constructor(
    @Volatile
    private var flags: Array<FeatureFlag>
) : JsonStream.Streamable, FeatureFlagAware {

    /*
     * Implemented as *effectively* a CopyOnWriteArrayList - but since FeatureFlags are
     * key/value pairs, CopyOnWriteArrayList would require external locking (in addition to it's
     * internal locking) for us to be sure we are not adding duplicates.
     *
     * This class aims to have similar performance while also ensuring that the FeatureFlag object
     * themselves don't leak, as they are mutable and we want 'copy' to be an O(1) snapshot
     * operation for when an Event is created.
     *
     * It's assumed that *most* FeatureFlags will be added up-front, or during the normal app
     * lifecycle (not during an Event).
     *
     * As such a copy-on-write structure allows an Event to simply capture a reference to the
     * "snapshot" of FeatureFlags that were active when the Event was created.
     */

    constructor() : this(emptyArray<FeatureFlag>())

    override fun addFeatureFlag(name: String) {
        addFeatureFlag(name, null)
    }

    override fun addFeatureFlag(name: String, variant: String?) {
        synchronized(this) {
            val flagArray = flags
            val index = flagArray.indexOfFirst { it.name == name }

            flags = when {
                // this is a new FeatureFlag
                index == -1 -> flagArray + FeatureFlag(name, variant)

                // this is a change to an existing FeatureFlag
                flagArray[index].variant != variant -> flagArray.copyOf().also {
                    // replace the existing FeatureFlag in-place
                    it[index] = FeatureFlag(name, variant)
                }

                // no actual change, so we return
                else -> return
            }
        }
    }

    override fun addFeatureFlags(featureFlags: Iterable<FeatureFlag>) {
        synchronized(this) {
            val flagArray = flags

            val newFlags = ArrayList<FeatureFlag>(
                // try to guess a reasonable upper-bound for the output array
                if (featureFlags is Collection<*>) flagArray.size + featureFlags.size
                else max(flagArray.size * 2, flagArray.size)
            )

            newFlags.addAll(flagArray)

            featureFlags.forEach { (name, variant) ->
                val existingIndex = newFlags.indexOfFirst { it.name == name }
                when (existingIndex) {
                    // add a new flag to the end of the list
                    -1 -> newFlags.add(FeatureFlag(name, variant))
                    // replace the existing flag
                    else -> newFlags[existingIndex] = FeatureFlag(name, variant)
                }
            }

            flags = newFlags.toTypedArray()
        }
    }

    override fun clearFeatureFlag(name: String) {
        synchronized(this) {
            val flagArray = flags
            val index = flagArray.indexOfFirst { it.name == name }
            if (index == -1) {
                return
            }

            val out = arrayOfNulls<FeatureFlag>(flagArray.size - 1)
            flagArray.copyInto(out, 0, 0, index)
            flagArray.copyInto(out, index, index + 1)

            @Suppress("UNCHECKED_CAST")
            flags = out as Array<FeatureFlag>
        }
    }

    override fun clearFeatureFlags() {
        synchronized(this) {
            flags = emptyArray()
        }
    }

    @Throws(IOException::class)
    override fun toStream(stream: JsonStream) {
        val storeCopy = flags
        stream.beginArray()
        storeCopy.forEach { (name, variant) ->
            stream.beginObject()
            stream.name("featureFlag").value(name)
            if (variant != null) {
                stream.name("variant").value(variant)
            }
            stream.endObject()
        }
        stream.endArray()
    }

    fun toList(): List<FeatureFlag> = flags.map { (name, variant) -> FeatureFlag(name, variant) }

    fun copy() = FeatureFlags(flags)
}
