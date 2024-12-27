package com.bugsnag.android

import java.io.IOException
import java.security.DigestOutputStream
import java.security.MessageDigest

/**
 * Denotes objects that are expected to be delivered over a network.
 */
interface Deliverable {
    /**
     * Return the byte representation of this `Deliverable`.
     */
    @Throws(IOException::class)
    fun toByteArray(): ByteArray

    /**
     * The value of the "Bugsnag-Integrity" HTTP header returned as a String. This value is used
     * to validate the payload and is expected by the standard BugSnag servers.
     */
    val integrityToken: String?
        get() {
            runCatching {
                val shaDigest = MessageDigest.getInstance("SHA-1")
                val builder = StringBuilder("sha1 ")

                // Pipe the object through a no-op output stream
                DigestOutputStream(NullOutputStream(), shaDigest).use { stream ->
                    stream.buffered().use { writer ->
                        writer.write(toByteArray())
                    }
                    shaDigest.digest().forEach { byte ->
                        builder.append(String.format("%02x", byte))
                    }
                }
                return builder.toString()
            }.getOrElse { return null }
        }
}
