package com.bugsnag.android;

import com.bugsnag.android.internal.DateUtils;
import com.bugsnag.android.internal.ImmutableConfig;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

class SessionTracker extends BaseObservable {

    private static final int DEFAULT_TIMEOUT_MS = 30000;

    private final Collection<String>
            foregroundActivities = new ConcurrentLinkedQueue<>();
    private final long timeoutMs;

    private final ImmutableConfig configuration;
    private final CallbackState callbackState;
    private final Client client;
    final SessionStore sessionStore;

    // This most recent time an Activity was stopped.
    private final AtomicLong lastExitedForegroundMs = new AtomicLong(0);

    // The first Activity in this 'session' was started at this time.
    private final AtomicLong lastEnteredForegroundMs = new AtomicLong(0);
    private volatile Session currentSession = null;
    private final ForegroundDetector foregroundDetector;
    final BackgroundTaskService backgroundTaskService;
    final Logger logger;

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
        this.foregroundDetector = new ForegroundDetector(client.getAppContext());
        this.backgroundTaskService = backgroundTaskService;
        this.logger = logger;
        notifyNdkInForeground();
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
        if (client.getConfig().shouldDiscardSession(autoCaptured)) {
            return null;
        }
        String id = UUID.randomUUID().toString();
        Session session = new Session(id, date, user, autoCaptured, client.getNotifier(), logger);
        if (trackSessionIfNeeded(session)) {
            return session;
        } else {
            return null;
        }
    }

    Session startSession(boolean autoCaptured) {
        if (client.getConfig().shouldDiscardSession(autoCaptured)) {
            return null;
        }
        return startNewSession(new Date(), client.getUser(), autoCaptured);
    }

    void pauseSession() {
        Session session = currentSession;

        if (session != null) {
            session.isPaused.set(true);
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
            resumed = session.isPaused.compareAndSet(true, false);
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
                    client.getNotifier(), logger);
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

        if (deliverSession && session.isTracked().compareAndSet(false, true)) {
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

        if (session != null && !session.isPaused.get()) {
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
        Session payload = new Session(storedFile, client.getNotifier(), logger);

        if (!payload.isV2Payload()) { // collect data here
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
        DeliveryParams params = configuration.getSessionApiDeliveryParams();
        Delivery delivery = configuration.getDelivery();
        return delivery.deliver(payload, params);
    }

    void onActivityStarted(String activityName) {
        updateForegroundTracker(activityName, true, SystemClock.elapsedRealtime());
    }

    void onActivityStopped(String activityName) {
        updateForegroundTracker(activityName, false, SystemClock.elapsedRealtime());
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
     * @param nowMs            The current time in ms
     */
    void updateForegroundTracker(String activityName, boolean activityStarting, long nowMs) {
        if (activityStarting) {
            long noActivityRunningForMs = nowMs - lastExitedForegroundMs.get();

            //FUTURE:SM Race condition between isEmpty and put
            if (foregroundActivities.isEmpty()) {
                lastEnteredForegroundMs.set(nowMs);

                if (noActivityRunningForMs >= timeoutMs
                        && configuration.getAutoTrackSessions()) {
                    startNewSession(new Date(), client.getUser(), true);
                }
            }
            foregroundActivities.add(activityName);
        } else {
            foregroundActivities.remove(activityName);

            if (foregroundActivities.isEmpty()) {
                lastExitedForegroundMs.set(nowMs);
            }
        }
        client.getContextState().setAutomaticContext(getContextActivity());
        notifyNdkInForeground();
    }

    private void notifyNdkInForeground() {
        Boolean inForeground = isInForeground();
        final boolean foreground = inForeground != null ? inForeground : false;
        updateState(new StateEvent.UpdateInForeground(foreground, getContextActivity()));
    }

    @Nullable
    Boolean isInForeground() {
        return foregroundDetector.isInForeground();
    }

    long getLastEnteredForegroundMs() {
        return lastEnteredForegroundMs.get();
    }

    @Nullable
    String getContextActivity() {
        if (foregroundActivities.isEmpty()) {
            return null;
        } else {
            // linked hash set retains order of added activity and ensures uniqueness
            // therefore obtain the most recently added
            int size = foregroundActivities.size();
            String[] activities = foregroundActivities.toArray(new String[size]);
            return activities[size - 1];
        }
    }
}
