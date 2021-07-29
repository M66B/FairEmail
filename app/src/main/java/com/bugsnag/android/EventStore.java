package com.bugsnag.android;

import com.bugsnag.android.internal.ImmutableConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Store and flush Event reports which couldn't be sent immediately due to
 * lack of network connectivity.
 */
class EventStore extends FileStore {

    private static final long LAUNCH_CRASH_TIMEOUT_MS = 2000;

    private final ImmutableConfig config;
    private final Delegate delegate;
    private final Notifier notifier;
    private final BackgroundTaskService bgTaskSevice;
    final Logger logger;

    static final Comparator<File> EVENT_COMPARATOR = new Comparator<File>() {
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
            return lhs.compareTo(rhs);
        }
    };

    EventStore(@NonNull ImmutableConfig config,
               @NonNull Logger logger,
               Notifier notifier,
               BackgroundTaskService bgTaskSevice,
               Delegate delegate) {
        super(new File(config.getPersistenceDirectory(), "bugsnag-errors"),
                config.getMaxPersistedEvents(),
                EVENT_COMPARATOR,
                logger,
                delegate);
        this.config = config;
        this.logger = logger;
        this.delegate = delegate;
        this.notifier = notifier;
        this.bgTaskSevice = bgTaskSevice;
    }

    /**
     * Flush startup crashes synchronously on the main thread
     */
    void flushOnLaunch() {
        if (!config.getSendLaunchCrashesSynchronously()) {
            return;
        }
        Future<?> future = null;
        try {
            future = bgTaskSevice.submitTask(TaskType.ERROR_REQUEST, new Runnable() {
                @Override
                public void run() {
                    flushLaunchCrashReport();
                }
            });
        } catch (RejectedExecutionException exc) {
            logger.d("Failed to flush launch crash reports, continuing.", exc);
        }

        try {
            if (future != null) {
                future.get(LAUNCH_CRASH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException exc) {
            logger.d("Failed to send launch crash reports within 2s timeout, continuing.", exc);
        }
    }

    void flushLaunchCrashReport() {
        List<File> storedFiles = findStoredFiles();
        File launchCrashReport = findLaunchCrashReport(storedFiles);

        // cancel non-launch crash reports
        if (launchCrashReport != null) {
            storedFiles.remove(launchCrashReport);
        }
        cancelQueuedFiles(storedFiles);

        if (launchCrashReport != null) {
            logger.i("Attempting to send the most recent launch crash report");
            flushReports(Collections.singletonList(launchCrashReport));
            logger.i("Continuing with Bugsnag initialisation");
        } else {
            logger.d("No startupcrash events to flush to Bugsnag.");
        }
    }

    @Nullable
    File findLaunchCrashReport(Collection<File> storedFiles) {
        List<File> launchCrashes = new ArrayList<>();

        for (File file : storedFiles) {
            EventFilenameInfo filenameInfo = EventFilenameInfo.Companion.fromFile(file, config);
            if (filenameInfo.isLaunchCrashReport()) {
                launchCrashes.add(file);
            }
        }

        // sort to get most recent timestamp
        Collections.sort(launchCrashes, EVENT_COMPARATOR);
        return launchCrashes.isEmpty() ? null : launchCrashes.get(launchCrashes.size() - 1);
    }

    /**
     * Flush any on-disk errors to Bugsnag
     */
    void flushAsync() {
        try {
            bgTaskSevice.submitTask(TaskType.ERROR_REQUEST, new Runnable() {
                @Override
                public void run() {
                    List<File> storedFiles = findStoredFiles();
                    if (storedFiles.isEmpty()) {
                        logger.d("No regular events to flush to Bugsnag.");
                    }
                    flushReports(storedFiles);
                }
            });
        } catch (RejectedExecutionException exception) {
            logger.w("Failed to flush all on-disk errors, retaining unsent errors for later.");
        }
    }

    void flushReports(Collection<File> storedReports) {
        if (!storedReports.isEmpty()) {
            int size = storedReports.size();
            logger.i("Sending " + size + " saved error(s) to Bugsnag");

            for (File eventFile : storedReports) {
                flushEventFile(eventFile);
            }
        }
    }

    private void flushEventFile(File eventFile) {
        try {
            EventFilenameInfo eventInfo = EventFilenameInfo.Companion.fromFile(eventFile, config);
            String apiKey = eventInfo.getApiKey();
            EventPayload payload = new EventPayload(apiKey, null, eventFile, notifier, config);
            DeliveryParams deliveryParams = config.getErrorApiDeliveryParams(payload);
            Delivery delivery = config.getDelivery();
            DeliveryStatus deliveryStatus = delivery.deliver(payload, deliveryParams);

            switch (deliveryStatus) {
                case DELIVERED:
                    deleteStoredFiles(Collections.singleton(eventFile));
                    logger.i("Deleting sent error file " + eventFile.getName());
                    break;
                case UNDELIVERED:
                    cancelQueuedFiles(Collections.singleton(eventFile));
                    logger.w("Could not send previously saved error(s)"
                            + " to Bugsnag, will try again later");
                    break;
                case FAILURE:
                    Exception exc = new RuntimeException("Failed to deliver event payload");
                    handleEventFlushFailure(exc, eventFile);
                    break;
                default:
                    break;
            }
        } catch (Exception exception) {
            handleEventFlushFailure(exception, eventFile);
        }
    }

    private void handleEventFlushFailure(Exception exc, File eventFile) {
        if (delegate != null) {
            delegate.onErrorIOFailure(exc, eventFile, "Crash Report Deserialization");
        }
        deleteStoredFiles(Collections.singleton(eventFile));
    }

    @NonNull
    @Override
    String getFilename(Object object) {
        EventFilenameInfo eventInfo
                = EventFilenameInfo.Companion.fromEvent(object, null, config);
        return eventInfo.encode();
    }

    String getNdkFilename(Object object, String apiKey) {
        EventFilenameInfo eventInfo
                = EventFilenameInfo.Companion.fromEvent(object, apiKey, config);
        return eventInfo.encode();
    }
}
