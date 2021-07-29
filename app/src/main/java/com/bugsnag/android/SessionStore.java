package com.bugsnag.android;

import com.bugsnag.android.internal.ImmutableConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

/**
 * Store and flush Sessions which couldn't be sent immediately due to
 * lack of network connectivity.
 */
class SessionStore extends FileStore {

    static final Comparator<File> SESSION_COMPARATOR = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs == null && rhs == null) {
                return 0;
            }
            if (lhs == null) {
                return 1;
            }
            if (rhs == null) {
                return -1;
            }
            String lhsName = lhs.getName();
            String rhsName = rhs.getName();
            return lhsName.compareTo(rhsName);
        }
    };

    SessionStore(@NonNull ImmutableConfig config,
                 @NonNull Logger logger,
                 @Nullable Delegate delegate) {
        super(new File(config.getPersistenceDirectory(), "bugsnag-sessions"),
                config.getMaxPersistedSessions(),
                SESSION_COMPARATOR,
                logger,
                delegate);
    }

    @NonNull
    @Override
    String getFilename(Object object) {
        return UUID.randomUUID().toString() + System.currentTimeMillis() + "_v2.json";
    }

}
