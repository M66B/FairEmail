package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URLConnection;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;

    static void closeQuietly(@Nullable final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (@NonNull final Exception ioe) {
            // ignore
        }
    }

    static int copy(@NonNull final Reader input,
                    @NonNull final Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int read;
        while (EOF != (read = input.read(buffer))) {
            output.write(buffer, 0, read);
            count += read;
        }

        if (count > Integer.MAX_VALUE) {
            return -1;
        }

        return (int) count;
    }

    static void deleteFile(File file, Logger logger) {
        try {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        } catch (Exception ex) {
            logger.w("Failed to delete file", ex);
        }
    }
}
