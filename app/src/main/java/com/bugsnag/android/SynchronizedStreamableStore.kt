package com.bugsnag.android

import android.util.JsonReader
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * Persists and loads a [Streamable] object to the file system. This is intended for use
 * primarily as a replacement for primitive value stores such as [SharedPreferences].
 *
 * This class is made thread safe through the use of a [ReadWriteLock].
 */
internal class SynchronizedStreamableStore<T : JsonStream.Streamable>(
    private val file: File
) {

    private val lock = ReentrantReadWriteLock()

    @Throws(IOException::class)
    fun persist(streamable: T) {
        lock.writeLock().withLock {
            file.writer().buffered().use {
                streamable.toStream(JsonStream(it))
                true
            }
        }
    }

    @Throws(IOException::class)
    fun load(loadCallback: (JsonReader) -> T): T {
        lock.readLock().withLock {
            return file.reader().buffered().use {
                loadCallback(JsonReader(it))
            }
        }
    }
}
