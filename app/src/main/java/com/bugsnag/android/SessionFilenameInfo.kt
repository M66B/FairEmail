package com.bugsnag.android

import java.io.File
import java.util.UUID

/**
 * Represents important information about a session filename.
 * Currently the following information is encoded:
 *
 * uuid - to disambiguate stored error reports
 * timestamp - to sort error reports by time of capture
 */
internal data class SessionFilenameInfo(
    val timestamp: Long,
    val uuid: String,
) {

    fun encode(): String {
        return toFilename(timestamp, uuid)
    }

    internal companion object {

        const val uuidLength = 36

        /**
         * Generates a filename for the session in the format
         * "[UUID][timestamp]_v2.json"
         */
        fun toFilename(timestamp: Long, uuid: String): String {
            return "${uuid}${timestamp}_v2.json"
        }

        @JvmStatic
        fun defaultFilename(): String {
            return toFilename(System.currentTimeMillis(), UUID.randomUUID().toString())
        }

        fun fromFile(file: File): SessionFilenameInfo {
            return SessionFilenameInfo(
                findTimestampInFilename(file),
                findUuidInFilename(file)
            )
        }

        private fun findUuidInFilename(file: File): String {
            return file.name.substring(0, uuidLength - 1)
        }

        @JvmStatic
        fun findTimestampInFilename(file: File): Long {
            return file.name.substring(uuidLength, file.name.indexOf("_")).toLongOrNull() ?: -1
        }
    }
}
