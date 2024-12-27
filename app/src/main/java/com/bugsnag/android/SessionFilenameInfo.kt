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
    var apiKey: String,
    val timestamp: Long,
    val uuid: String
) {

    fun encode(): String {
        return toFilename(apiKey, timestamp, uuid)
    }

    internal companion object {

        const val uuidLength = 36

        /**
         * Generates a filename for the session in the format
         * "[UUID][timestamp]_v2.json"
         */
        fun toFilename(apiKey: String, timestamp: Long, uuid: String): String {
            return "${apiKey}_${uuid}${timestamp}_v3.json"
        }

        @JvmStatic
        fun defaultFilename(obj: Any?, apiKey: String): SessionFilenameInfo {
            val sanitizedApiKey = when (obj) {
                is Session -> obj.apiKey
                else -> apiKey
            }

            return SessionFilenameInfo(
                sanitizedApiKey,
                System.currentTimeMillis(),
                UUID.randomUUID().toString()
            )
        }

        fun fromFile(file: File, defaultApiKey: String): SessionFilenameInfo {
            return SessionFilenameInfo(
                findApiKeyInFilename(file, defaultApiKey),
                findTimestampInFilename(file),
                findUuidInFilename(file)
            )
        }

        @JvmStatic
        fun findUuidInFilename(file: File): String {
            var fileName = file.name
            if (isFileV3(file)) {
                fileName = file.name.substringAfter('_')
            }
            return fileName.takeIf { it.length >= uuidLength }?.take(uuidLength) ?: ""
        }

        @JvmStatic
        fun findTimestampInFilename(file: File): Long {
            var fileName = file.name
            if (isFileV3(file)) {
                fileName = file.name.substringAfter('_')
            }
            return fileName.drop(findUuidInFilename(file).length)
                .substringBefore('_')
                .toLongOrNull() ?: -1
        }

        @JvmStatic
        fun findApiKeyInFilename(file: File?, defaultApiKey: String): String {
            if (file == null || !isFileV3(file)) {
                return defaultApiKey
            }
            return file.name.substringBefore('_').takeUnless { it.isEmpty() } ?: defaultApiKey
        }

        internal fun isFileV3(file: File): Boolean = file.name.endsWith("_v3.json")
    }
}
