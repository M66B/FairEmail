package com.bugsnag.android;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class DateUtils {
    // SimpleDateFormat isn't thread safe, cache one instance per thread as needed.
    private static final ThreadLocal<DateFormat> iso8601Holder = new ThreadLocal<DateFormat>() {
        @NonNull
        @Override
        protected DateFormat initialValue() {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            iso8601.setTimeZone(tz);
            return iso8601;
        }
    };

    @NonNull
    static String toIso8601(@NonNull Date date) {
        DateFormat dateFormat = iso8601Holder.get();
        if (dateFormat == null) {
            throw new IllegalStateException("Unable to find valid dateformatter");
        }
        return dateFormat.format(date);
    }

    @NonNull
    static Date fromIso8601(@NonNull String date) {
        try {
            return iso8601Holder.get().parse(date);
        } catch (ParseException exc) {
            throw new IllegalArgumentException("Failed to parse timestamp", exc);
        }
    }
}
