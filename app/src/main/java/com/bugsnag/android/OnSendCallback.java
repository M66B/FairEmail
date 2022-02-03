package com.bugsnag.android;

import androidx.annotation.NonNull;

/**
 * A callback to be invoked before an {@link Event} is uploaded to a server. Similar to
 * {@link OnErrorCallback}, an {@code OnSendCallback} may modify the {@code Event}
 * contents or even reject the entire payload by returning {@code false}.
 */
public interface OnSendCallback {
    boolean onSend(@NonNull Event event);
}
