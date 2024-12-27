package com.bugsnag.android;

import static com.bugsnag.android.DeliveryHeadersKt.HEADER_INTERNAL_ERROR;
import static com.bugsnag.android.SeverityReason.REASON_UNHANDLED_EXCEPTION;

import com.bugsnag.android.internal.BackgroundTaskService;
import com.bugsnag.android.internal.ImmutableConfig;
import com.bugsnag.android.internal.TaskType;
import com.bugsnag.android.internal.dag.Provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

class InternalReportDelegate implements EventStore.Delegate {

    static final String INTERNAL_DIAGNOSTICS_TAB = "BugsnagDiagnostics";

    final Logger logger;
    final ImmutableConfig config;

    @Nullable
    final StorageManager storageManager;

    final AppDataCollector appDataCollector;
    final Provider<DeviceDataCollector> deviceDataCollector;
    final Context appContext;
    final SessionTracker sessionTracker;
    final Notifier notifier;
    final BackgroundTaskService backgroundTaskService;

    InternalReportDelegate(Context context,
                           Logger logger,
                           ImmutableConfig immutableConfig,
                           @Nullable StorageManager storageManager,
                           AppDataCollector appDataCollector,
                           Provider<DeviceDataCollector> deviceDataCollector,
                           SessionTracker sessionTracker,
                           Notifier notifier,
                           BackgroundTaskService backgroundTaskService) {
        this.logger = logger;
        this.config = immutableConfig;
        this.storageManager = storageManager;
        this.appDataCollector = appDataCollector;
        this.deviceDataCollector = deviceDataCollector;
        this.appContext = context;
        this.sessionTracker = sessionTracker;
        this.notifier = notifier;
        this.backgroundTaskService = backgroundTaskService;
    }

    @Override
    public void onErrorIOFailure(Exception exc, File errorFile, String context) {
        // send an internal error to bugsnag with no cache
        SeverityReason severityReason = SeverityReason.newInstance(REASON_UNHANDLED_EXCEPTION);
        Event err = new Event(exc, config, severityReason, logger);
        err.setContext(context);

        err.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "canRead", errorFile.canRead());
        err.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "canWrite", errorFile.canWrite());
        err.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "exists", errorFile.exists());

        @SuppressLint("UsableSpace") // storagemanager alternative API requires API 26
        long usableSpace = appContext.getCacheDir().getUsableSpace();
        err.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "usableSpace", usableSpace);
        err.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "filename", errorFile.getName());
        err.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "fileLength", errorFile.length());
        recordStorageCacheBehavior(err);
        reportInternalBugsnagError(err);
    }

    void recordStorageCacheBehavior(Event event) {
        if (storageManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            File cacheDir = appContext.getCacheDir();
            File errDir = new File(cacheDir, "bugsnag/errors");

            try {
                boolean tombstone = storageManager.isCacheBehaviorTombstone(errDir);
                boolean group = storageManager.isCacheBehaviorGroup(errDir);
                event.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "cacheTombstone", tombstone);
                event.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "cacheGroup", group);
            } catch (IOException exc) {
                logger.w("Failed to record cache behaviour, skipping diagnostics", exc);
            }
        }
    }

    /**
     * Reports an event that occurred within the notifier to bugsnag. A lean event report will be
     * generated and sent asynchronously with no callbacks, retry attempts, or writing to disk.
     * This is intended for internal use only, and reports will not be visible to end-users.
     */
    void reportInternalBugsnagError(@NonNull Event event) {
        event.setApp(appDataCollector.generateAppWithState());
        event.setDevice(deviceDataCollector.get().generateDeviceWithState(new Date().getTime()));

        event.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "notifierName", notifier.getName());
        event.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "notifierVersion", notifier.getVersion());
        event.addMetadata(INTERNAL_DIAGNOSTICS_TAB, "apiKey", config.getApiKey());

        final EventPayload payload = new EventPayload(null, event, notifier, config);
        try {
            backgroundTaskService.submitTask(TaskType.INTERNAL_REPORT, new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.d("InternalReportDelegate - sending internal event");
                        Delivery delivery = config.getDelivery();
                        DeliveryParams params = config.getErrorApiDeliveryParams(payload);

                        // can only modify headers if DefaultDelivery is in use
                        if (delivery instanceof DefaultDelivery) {
                            Map<String, String> headers = params.getHeaders();
                            headers.put(HEADER_INTERNAL_ERROR, "bugsnag-android");
                            headers.remove(DeliveryHeadersKt.HEADER_API_KEY);
                            DefaultDelivery defaultDelivery = (DefaultDelivery) delivery;
                            defaultDelivery.deliver(
                                    params.getEndpoint(),
                                    payload.toByteArray(),
                                    payload.getIntegrityToken(),
                                    headers
                            );
                        }

                    } catch (Exception exception) {
                        logger.w("Failed to report internal event to Bugsnag", exception);
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            // drop internal report
        }
    }
}
