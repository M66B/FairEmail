package com.bugsnag.android;

import android.os.Build;
import android.os.StrictMode.OnThreadViolationListener;
import android.os.strictmode.Violation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Sends an error report to Bugsnag for each StrictMode thread policy violation that occurs in
 * your app.
 * <p></p>
 * You should use this class by instantiating Bugsnag in the normal way and then set the
 * StrictMode policy with
 * {@link android.os.StrictMode.ThreadPolicy.Builder#penaltyListener
 * (Executor, OnThreadViolationListener)}.
 * This functionality is only supported on API 28+.
 */
@RequiresApi(api = Build.VERSION_CODES.P)
public class BugsnagThreadViolationListener implements OnThreadViolationListener {

    private final Client client;
    private final OnThreadViolationListener listener;

    public BugsnagThreadViolationListener() {
        this(Bugsnag.getClient(), null);
    }

    public BugsnagThreadViolationListener(@NonNull Client client) {
        this(client, null);
    }

    public BugsnagThreadViolationListener(@NonNull Client client,
                                          @Nullable OnThreadViolationListener listener) {
        this.client = client;
        this.listener = listener;
    }

    @Override
    public void onThreadViolation(@NonNull Violation violation) {
        if (client != null) {
            client.notify(violation, new StrictModeOnErrorCallback(
                    "StrictMode policy violation detected: ThreadPolicy"
            ));
        }
        if (listener != null) {
            listener.onThreadViolation(violation);
        }
    }
}
