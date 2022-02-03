package com.bugsnag.android;

import android.os.StrictMode;

import androidx.annotation.NonNull;

import java.lang.Thread;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Provides automatic notification hooks for unhandled exceptions.
 */
class ExceptionHandler implements UncaughtExceptionHandler {

    private static final String STRICT_MODE_TAB = "StrictMode";
    private static final String STRICT_MODE_KEY = "Violation";

    private final UncaughtExceptionHandler originalHandler;
    private final StrictModeHandler strictModeHandler = new StrictModeHandler();
    private final Client client;
    private final Logger logger;

    ExceptionHandler(Client client, Logger logger) {
        this.client = client;
        this.logger = logger;
        this.originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    void install() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    void uninstall() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler);
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        try {
            if (client.getConfig().shouldDiscardError(throwable)) {
                return;
            }

            boolean strictModeThrowable = strictModeHandler.isStrictModeThrowable(throwable);

            // Notify any subscribed clients of the uncaught exception
            Metadata metadata = new Metadata();
            String violationDesc = null;

            if (strictModeThrowable) { // add strictmode policy violation to metadata
                violationDesc = strictModeHandler.getViolationDescription(throwable.getMessage());
                metadata = new Metadata();
                metadata.addMetadata(STRICT_MODE_TAB, STRICT_MODE_KEY, violationDesc);
            }

            String severityReason = strictModeThrowable
                    ? SeverityReason.REASON_STRICT_MODE : SeverityReason.REASON_UNHANDLED_EXCEPTION;

            if (strictModeThrowable) { // writes to disk on main thread
                StrictMode.ThreadPolicy originalThreadPolicy = StrictMode.getThreadPolicy();
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

                client.notifyUnhandledException(throwable,
                        metadata, severityReason, violationDesc);

                StrictMode.setThreadPolicy(originalThreadPolicy);
            } else {
                client.notifyUnhandledException(throwable,
                        metadata, severityReason, null);
            }
        } catch (Throwable ignored) {
            // the runtime would ignore any exceptions here, we make that absolutely clear
            // to avoid any possible unhandled-exception loops
        } finally {
            forwardToOriginalHandler(thread, throwable);
        }
    }

    private void forwardToOriginalHandler(@NonNull Thread thread, @NonNull Throwable throwable) {
        // Pass exception on to original exception handler
        if (originalHandler != null) {
            originalHandler.uncaughtException(thread, throwable);
        } else {
            System.err.printf("Exception in thread \"%s\" ", thread.getName());
            logger.w("Exception", throwable);
        }
    }
}
