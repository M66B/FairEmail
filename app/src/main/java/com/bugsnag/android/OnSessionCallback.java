package com.bugsnag.android;

import androidx.annotation.NonNull;

/**
 * A callback to be run before sessions are sent to Bugsnag.
 * <p>
 * <p>You can use this to add or modify information attached to a session
 * before it is sent to your dashboard. You can also return
 * <code>false</code> from any callback to halt execution.
 */
public interface OnSessionCallback {

    /**
     * Runs the "on session" callback. If the callback returns
     * <code>false</code> any further OnSessionCallback callbacks will not be called
     * and the session will not be sent to Bugsnag.
     *
     * @param session the session to be sent to Bugsnag
     * @see Session
     */
    boolean onSession(@NonNull Session session);
}
