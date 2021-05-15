package com.bugsnag.android;

import androidx.annotation.NonNull;

/**
 * A callback to be run before error reports are sent to Bugsnag.
 * <p>
 * <p>You can use this to add or modify information attached to an error
 * before it is sent to your dashboard. You can also return
 * <code>false</code> from any callback to halt execution.
 * <p>"on error" callbacks added via the JVM API do not run when a fatal C/C++ crash occurs.
 */
public interface OnErrorCallback {

    /**
     * Runs the "on error" callback. If the callback returns
     * <code>false</code> any further OnErrorCallback callbacks will not be called
     * and the event will not be sent to Bugsnag.
     *
     * @param event the event to be sent to Bugsnag
     * @see Event
     */
    boolean onError(@NonNull Event event);
}
