package com.bugsnag.android;

import com.bugsnag.android.internal.BackgroundTaskService;
import com.bugsnag.android.internal.DateUtils;
import com.bugsnag.android.internal.ForegroundDetector;
import com.bugsnag.android.internal.ImmutableConfig;
import com.bugsnag.android.internal.TaskType;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

class SessionTracker extends BaseObservable implements ForegroundDetector.OnActivityCallback {

    private static final int DEFAULT_TIMEOUT_MS = 30000;

    private final Deque<String>
            foregroundActivities = new ArrayDeque<>();
    private final long timeoutMs;

    private final ImmutableConfig configuration;
    private final CallbackState callbackState;
    private final Client client;
    final SessionStore sessionStore;
    private volatile Session currentSession = null;
    final BackgroundTaskService backgroundTaskService;
    final Logger logger;
    private boolean shouldSuppressFirstAutoSession = true;

    SessionTracker(ImmutableConfig configuration,
                   CallbackState callbackState,
                   Client client,
                   SessionStore sessionStore,
                   Logger logger,
                   BackgroundTaskService backgroundTaskService) {
        this(configuration, callbackState, client, DEFAULT_TIMEOUT_MS,
                sessionStore, logger, backgroundTaskService);
    }

    SessionTracker(ImmutableConfig configuration,
                   CallbackState callbackState,
                   Client client,
                   long timeoutMs,
                   SessionStore sessionStore,
                   Logger logger,
                   BackgroundTaskService backgroundTaskService) {
        this.configuration = configuration;
        this.callbackState = callbackState;
        this.client = client;
        this.timeoutMs = timeoutMs;
        this.sessionStore = sessionStore;
        this.backgroundTaskService = backgroundTaskService;
        this.logger = logger;
    }

    /**
     * Starts a new session with the given date and user.
     * <p>
     * A session will only be created if {@link Configuration#getAutoTrackSessions()} returns
     * true.
     *
     * @param date the session start date
     * @param user the session user (if any)
     */
    @Nullable
    @VisibleForTesting
    Session startNewSession(@NonNull Date date, @Nullable User user,
                            boolean autoCaptured) {
        if (shouldDiscardSession(autoCaptured)) {
            return null;
        }
        String id = UUID.randomUUID().toString();
        Session session = new Session(
                id, date, user, autoCaptured,
                client.getNotifier(), logger, configuration.getApiKey()
        );
        if (trackSessionIfNeeded(session)) {
            return session;
        } else {
            return null;
        }
    }

    Session startSession(boolean autoCaptured) {
        if (shouldDiscardSession(autoCaptured)) {
            return null;
        }
        return startNewSession(new Date(), client.getUser(), autoCaptured);
    }

    private boolean shouldDiscardSession(boolean autoCaptured) {
        if (client.getConfig().shouldDiscardSession(autoCaptured)) {
            return true;
        } else {
            Session existingSession = currentSession;
            if (autoCaptured
                    && existingSession != null
                    && !existingSession.isAutoCaptured()
                    && shouldSuppressFirstAutoSession) {
                shouldSuppressFirstAutoSession = false;
                return true;
            }

            if (autoCaptured) {
                shouldSuppressFirstAutoSession = false;
            }
        }
        return false;
    }


    void pauseSession() {
        Session session = currentSession;

        if (session != null) {
            session.markPaused();
            updateState(StateEvent.PauseSession.INSTANCE);
        }
    }

    boolean resumeSession() {
        Session session = currentSession;
        boolean resumed;

        if (session == null) {
            session = startSession(false);
            resumed = false;
        } else {
            resumed = session.markResumed();
        }

        if (session != null) {
            notifySessionStartObserver(session);
        }
        return resumed;
    }

    private void notifySessionStartObserver(final Session session) {
        final String startedAt = DateUtils.toIso8601(session.getStartedAt());
        updateState(new StateEvent.StartSession(session.getId(), startedAt,
                session.getHandledCount(), session.getUnhandledCount()));
    }

    /**
     * Cache details of a previously captured session.
     * Append session details to all subsequent reports.
     *
     * @param date           the session start date
     * @param sessionId      the unique session identifier
     * @param user           the session user (if any)
     * @param unhandledCount the number of unhandled events which have occurred during the session
     * @param handledCount   the number of handled events which have occurred during the session
     * @return the session
     */
    @Nullable
    Session registerExistingSession(@Nullable Date date, @Nullable String sessionId,
                                    @Nullable User user, int unhandledCount,
                                    int handledCount) {
        if (client.getConfig().shouldDiscardSession(false)) {
            return null;
        }
        Session session = null;
        if (date != null && sessionId != null) {
            session = new Session(sessionId, date, user, unhandledCount, handledCount,
                    client.getNotifier(), logger, configuration.getApiKey());
            notifySessionStartObserver(session);
        } else {
            updateState(StateEvent.PauseSession.INSTANCE);
        }
        currentSession = session;
        return session;
    }

    /**
     * Determines whether or not a session should be tracked. If this is true, the session will be
     * stored and sent to the Bugsnag API, otherwise no action will occur in this method.
     *
     * @param session the session
     * @return true if the Session should be tracked
     */
    private boolean trackSessionIfNeeded(final Session session) {
        logger.d("SessionTracker#trackSessionIfNeeded() - session captured by Client");
        session.setApp(client.getAppDataCollector().generateApp());
        session.setDevice(client.getDeviceDataCollector().generateDevice());
        boolean deliverSession = callbackState.runOnSessionTasks(session, logger);

        if (deliverSession && session.markTracked()) {
            currentSession = session;
            notifySessionStartObserver(session);
            flushInMemorySession(session);
            flushAsync();
            return true;
        }
        return false;
    }

    @Nullable
    Session getCurrentSession() {
        Session session = currentSession;

        if (session != null && !session.isPaused()) {
            return session;
        }
        return null;
    }

    /**
     * Increments the unhandled error count on the current session, then returns a deep-copy
     * of the current session.
     *
     * @return a copy of the current session, or null if no session has been started.
     */
    Session incrementUnhandledAndCopy() {
        Session session = getCurrentSession();
        if (session != null) {
            return session.incrementUnhandledAndCopy();
        }
        return null;
    }

    /**
     * Increments the handled error count on the current session, then returns a deep-copy
     * of the current session.
     *
     * @return a copy of the current session, or null if no session has been started.
     */
    Session incrementHandledAndCopy() {
        Session session = getCurrentSession();
        if (session != null) {
            return session.incrementHandledAndCopy();
        }
        return null;
    }

    /**
     * Asynchronously flushes any session payloads stored on disk
     */
    void flushAsync() {
        try {
            backgroundTaskService.submitTask(TaskType.SESSION_REQUEST, new Runnable() {
                @Override
                public void run() {
                    flushStoredSessions();
                }
            });
        } catch (RejectedExecutionException ex) {
            logger.w("Failed to flush session reports", ex);
        }
    }

    /**
     * Attempts to flush session payloads stored on disk
     */
    void flushStoredSessions() {
        List<File> storedFiles = sessionStore.findStoredFiles();

        for (File storedFile : storedFiles) {
            flushStoredSession(storedFile);
        }
    }

    void flushStoredSession(File storedFile) {
        logger.d("SessionTracker#flushStoredSession() - attempting delivery");
        Session payload = new Session(
                storedFile, client.getNotifier(), logger, configuration.getApiKey()
        );

        if (payload.isLegacyPayload()) { // collect data here
            payload.setApp(client.getAppDataCollector().generateApp());
            payload.setDevice(client.getDeviceDataCollector().generateDevice());
        }

        DeliveryStatus deliveryStatus = deliverSessionPayload(payload);

        switch (deliveryStatus) {
            case DELIVERED:
                sessionStore.deleteStoredFiles(Collections.singletonList(storedFile));
                logger.d("Sent 1 new session to Bugsnag");
                break;
            case UNDELIVERED:
                if (sessionStore.isTooOld(storedFile)) {
                    logger.w("Discarding historical session (from {"
                            + sessionStore.getCreationDate(storedFile)
                            + "}) after failed delivery");
                    sessionStore.deleteStoredFiles(Collections.singletonList(storedFile));
                } else {
                    sessionStore.cancelQueuedFiles(Collections.singletonList(storedFile));
                    logger.w("Leaving session payload for future delivery");
                }
                break;
            case FAILURE:
                // drop bad data
                logger.w("Deleting invalid session tracking payload");
                sessionStore.deleteStoredFiles(Collections.singletonList(storedFile));
                break;
            default:
                break;
        }
    }

    private void flushInMemorySession(final Session session) {
        try {
            backgroundTaskService.submitTask(TaskType.SESSION_REQUEST, new Runnable() {
                @Override
                public void run() {
                    deliverInMemorySession(session);
                }
            });
        } catch (RejectedExecutionException exception) {
            // This is on the current thread but there isn't much else we can do
            sessionStore.write(session);
        }
    }

    void deliverInMemorySession(Session session) {
        try {
            logger.d("SessionTracker#trackSessionIfNeeded() - attempting initial delivery");
            DeliveryStatus deliveryStatus = deliverSessionPayload(session);

            switch (deliveryStatus) {
                case UNDELIVERED:
                    logger.w("Storing session payload for future delivery");
                    sessionStore.write(session);
                    break;
                case FAILURE:
                    logger.w("Dropping invalid session tracking payload");
                    break;
                case DELIVERED:
                    logger.d("Sent 1 new session to Bugsnag");
                    break;
                default:
                    break;
            }
        } catch (Exception exception) {
            logger.w("Session tracking payload failed", exception);
        }
    }

    DeliveryStatus deliverSessionPayload(Session payload) {
        DeliveryParams params = configuration.getSessionApiDeliveryParams(payload);
        Delivery delivery = configuration.getDelivery();
        return delivery.deliver(payload, params);
    }

    public void onActivityStarted(Activity activity) {
        updateContext(
                activity.getClass().getSimpleName(),
                true
        );
    }

    public void onActivityStopped(Activity activity) {
        updateContext(
                activity.getClass().getSimpleName(),
                false
        );
    }

    /**
     * Tracks whether an activity is in the foreground or not.
     * <p>
     * If an activity leaves the foreground, a timeout should be recorded (e.g. 30s), during which
     * no new sessions should be automatically started.
     * <p>
     * If an activity comes to the foreground and is the only foreground activity, a new session
     * should be started, unless the app is within a timeout period.
     *
     * @param activityName     the activity name
     * @param activityStarting whether the activity is being started or not
     */
    void updateContext(String activityName, boolean activityStarting) {
        if (activityStarting) {
            synchronized (foregroundActivities) {
                foregroundActivities.add(activityName);
            }
        } else {
            synchronized (foregroundActivities) {
                foregroundActivities.removeLastOccurrence(activityName);
            }
        }
        client.getContextState().setAutomaticContext(getContextActivity());
    }

    boolean isInForeground() {
        return ForegroundDetector.isInForeground();
    }

    long getLastEnteredForegroundMs() {
        return ForegroundDetector.getLastEnteredForegroundMs();
    }

    @Nullable
    String getContextActivity() {
        synchronized (foregroundActivities) {
            return foregroundActivities.peekLast();
        }
    }

    @Override
    public void onForegroundStatus(boolean foreground, long timestamp) {
        if (foreground) {
            long noActivityRunningForMs =
                    timestamp - ForegroundDetector.getLastExitedForegroundMs();
            if (noActivityRunningForMs >= timeoutMs && configuration.getAutoTrackSessions()) {
                startNewSession(new Date(), client.getUser(), true);
            }
        }

        // update any downstream notifiers (NDK, ReactNative, Flutter, etc.)
        updateState(new StateEvent.UpdateInForeground(foreground, getContextActivity()));
    }
}
