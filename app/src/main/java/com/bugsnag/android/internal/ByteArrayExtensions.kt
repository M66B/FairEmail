package com.bugsnag.android.internal

private const val HEX_RADIX = 16

/**
 * Encode this `ByteArray` as a string of lowercase hex-pairs.
 */
internal fun ByteArray.toHexString(): String = buildString(size * 2) {
    for (byte in this@toHexString) {
        @Suppress("MagicNumber")
        val value = byte.toInt() and 0xff
        if (value < HEX_RADIX) append('0')
        append(value.toString(HEX_RADIX))
    }
}
