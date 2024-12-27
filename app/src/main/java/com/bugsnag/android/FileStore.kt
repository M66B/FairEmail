package com.bugsnag.android

import com.bugsnag.android.JsonStream.Streamable
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

internal abstract class FileStore(
    val storageDir: File,
    private val maxStoreCount: Int,
    protected open val logger: Logger,
    protected val delegate: Delegate?
) {
    internal fun interface Delegate {
        /**
         * Invoked when an error report is not (de)serialized correctly
         *
         * @param exception the error encountered reading/delivering the file
         * @param errorFile file which could not be (de)serialized correctly
         * @param context   the context used to group the exception
         */
        fun onErrorIOFailure(exception: Exception?, errorFile: File?, context: String?)
    }

    private val lock: Lock = ReentrantLock()
    private val queuedFiles: MutableCollection<File> = ConcurrentSkipListSet()

    /**
     * Checks whether the storage directory is a writable directory. If it is not,
     * this method will attempt to create the directory.
     *
     * If the directory could not be created then an error will be logged.
     */
    private fun isStorageDirValid(storageDir: File): Boolean {
        try {
            storageDir.mkdirs()
        } catch (exception: Exception) {
            logger.e("Could not prepare file storage directory", exception)
            return false
        }
        return true
    }

    fun enqueueContentForDelivery(content: String?, filename: String) {
        if (!isStorageDirValid(storageDir)) {
            return
        }
        discardOldestFileIfNeeded()
        lock.lock()
        var out: Writer? = null
        val filePath = File(storageDir, filename).absolutePath
        try {
            val fos = FileOutputStream(filePath)
            out = BufferedWriter(OutputStreamWriter(fos, "UTF-8"))
            out.write(content)
        } catch (exc: Exception) {
            val eventFile = File(filePath)
            delegate?.onErrorIOFailure(exc, eventFile, "NDK Crash report copy")
            IOUtils.deleteFile(eventFile, logger)
        } finally {
            try {
                out?.close()
            } catch (exception: Exception) {
                logger.w("Failed to close unsent payload writer: $filename", exception)
            }
            lock.unlock()
        }
    }

    fun write(streamable: Streamable): String? {
        if (!isStorageDirValid(storageDir)) {
            return null
        }
        if (maxStoreCount == 0) {
            return null
        }
        discardOldestFileIfNeeded()
        val filename = File(storageDir, getFilename(streamable)).absolutePath
        var stream: JsonStream? = null
        lock.lock()
        try {
            val fos = FileOutputStream(filename)
            val out: Writer = BufferedWriter(OutputStreamWriter(fos, "UTF-8"))
            stream = JsonStream(out)
            stream.value(streamable)
            logger.i("Saved unsent payload to disk: '$filename'")
            return filename
        } catch (exc: FileNotFoundException) {
            logger.w("Ignoring FileNotFoundException - unable to create file", exc)
        } catch (exc: Exception) {
            val eventFile = File(filename)
            delegate?.onErrorIOFailure(exc, eventFile, "Crash report serialization")
            IOUtils.deleteFile(eventFile, logger)
        } finally {
            IOUtils.closeQuietly(stream)
            lock.unlock()
        }
        return null
    }

    fun discardOldestFileIfNeeded() {
        // Limit number of saved payloads to prevent disk space issues
        if (isStorageDirValid(storageDir)) {
            val listFiles = storageDir.listFiles() ?: return
            if (listFiles.size < maxStoreCount) return
            val sortedListFiles = listFiles.sortedBy { it.lastModified() }
            // Number of files to discard takes into account that a new file may need to be written
            val numberToDiscard = listFiles.size - maxStoreCount + 1
            var discardedCount = 0
            for (file in sortedListFiles) {
                if (discardedCount == numberToDiscard) {
                    return
                } else if (!queuedFiles.contains(file)) {
                    logger.w(
                        "Discarding oldest error as stored error limit reached: '" +
                            file.path + '\''
                    )
                    deleteStoredFiles(setOf(file))
                    discardedCount++
                }
            }
        }
    }

    abstract fun getFilename(obj: Any?): String

    fun findStoredFiles(): MutableList<File> {
        lock.lock()
        return try {
            val files: MutableList<File> = ArrayList()
            if (isStorageDirValid(storageDir)) {
                val values = storageDir.listFiles()
                if (values != null) {
                    for (value in values) {
                        // delete any tombstoned/empty files, as they contain no useful info
                        if (value.length() == 0L) {
                            if (!value.delete()) {
                                value.deleteOnExit()
                            }
                        } else if (value.isFile && !queuedFiles.contains(value)) {
                            files.add(value)
                        }
                    }
                }
            }
            queuedFiles.addAll(files)
            files
        } finally {
            lock.unlock()
        }
    }

    fun cancelQueuedFiles(files: Collection<File>?) {
        lock.lock()
        try {
            if (files != null) {
                queuedFiles.removeAll(files)
            }
        } finally {
            lock.unlock()
        }
    }

    fun deleteStoredFiles(storedFiles: Collection<File>?) {
        lock.lock()
        try {
            if (storedFiles != null) {
                queuedFiles.removeAll(storedFiles)
                for (storedFile in storedFiles) {
                    if (!storedFile.delete()) {
                        storedFile.deleteOnExit()
                    }
                }
            }
        } finally {
            lock.unlock()
        }
    }
}
