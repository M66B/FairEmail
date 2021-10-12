package com.bugsnag.android.internal

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    // SimpleDateFormat isn't thread safe, cache one instance per thread as needed.
    private val iso8601Holder = object : ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        }
    }

    private val iso8601Format: DateFormat
        get() = requireNotNull(iso8601Holder.get()) { "Unable to find valid dateformatter" }

    @JvmStatic
    fun toIso8601(date: Date): String {
        return iso8601Format.format(date)
    }

    @JvmStatic
    fun fromIso8601(date: String): Date {
        return try {
            iso8601Format.parse(date) ?: throw ParseException("DateFormat.parse returned null", 0)
        } catch (exc: ParseException) {
            throw IllegalArgumentException("Failed to parse timestamp", exc)
        }
    }
}
