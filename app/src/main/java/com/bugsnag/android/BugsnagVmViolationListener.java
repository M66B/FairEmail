package com.bugsnag.android;

import android.os.Build;
import android.os.StrictMode.OnVmViolationListener;
import android.os.strictmode.Violation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Sends an error report to Bugsnag for each StrictMode VM policy violation that occurs in
 * your app.
 * <p></p>
 * You should use this class by instantiating Bugsnag in the normal way and then set the
 * StrictMode policy with
 * {@link android.os.StrictMode.VmPolicy.Builder#penaltyListener
 * (Executor, OnVmViolationListener)}.
 * This functionality is only supported on API 28+.
 */
@RequiresApi(api = Build.VERSION_CODES.P)
public class BugsnagVmViolationListener implements OnVmViolationListener {

    private final Client client;
    private final OnVmViolationListener listener;

    public BugsnagVmViolationListener() {
        this(Bugsnag.getClient(), null);
    }

    public BugsnagVmViolationListener(@NonNull Client client) {
        this(client, null);
    }

    public BugsnagVmViolationListener(@NonNull Client client,
                                      @Nullable OnVmViolationListener listener) {
        this.client = client;
        this.listener = listener;
    }

    @Override
    public void onVmViolation(@NonNull Violation violation) {
        if (client != null) {
            client.notify(violation, new StrictModeOnErrorCallback(
                    "StrictMode policy violation detected: VmPolicy"
            ));
        }
        if (listener != null) {
            listener.onVmViolation(violation);
        }
    }
}
