package com.bugsnag.android;

import java.util.concurrent.atomic.AtomicBoolean;

class LibraryLoader {

    private final AtomicBoolean attemptedLoad = new AtomicBoolean();
    private boolean loaded = false;

    /**
     * Attempts to load a native library, returning false if the load was unsuccessful.
     * <p>
     * If a load was attempted and failed, an error report will be sent using the supplied client
     * and OnErrorCallback.
     *
     * @param name     the library name
     * @param client   the bugsnag client
     * @param callback an OnErrorCallback
     * @return true if the library was loaded, false if not
     */
    boolean loadLibrary(String name, Client client, OnErrorCallback callback) {
        if (!attemptedLoad.getAndSet(true)) {
            try {
                System.loadLibrary(name);
                loaded = true;
                return true;
            } catch (UnsatisfiedLinkError error) {
                client.notify(error, callback);
            }
        }
        return false;
    }

    boolean isLoaded() {
        return loaded;
    }
}
