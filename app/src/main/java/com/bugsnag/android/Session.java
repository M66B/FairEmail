package com.bugsnag.android;

import com.bugsnag.android.internal.DateUtils;
import com.bugsnag.android.internal.JsonHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a contiguous session in an application.
 */
@SuppressWarnings("ConstantConditions")
public final class Session implements JsonStream.Streamable, Deliverable, UserAware {

    private final File file;
    private final Notifier notifier;
    private String id;
    private Date startedAt;
    private User user;
    private final Logger logger;
    private App app;
    private Device device;

    private volatile boolean autoCaptured = false;
    private final AtomicInteger unhandledCount = new AtomicInteger();
    private final AtomicInteger handledCount = new AtomicInteger();
    private final AtomicBoolean tracked = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    private String apiKey;

    static Session copySession(Session session) {
        Session copy = new Session(session.id, session.startedAt, session.user,
                session.unhandledCount.get(), session.handledCount.get(), session.notifier,
                session.logger, session.getApiKey());
        copy.tracked.set(session.tracked.get());
        copy.autoCaptured = session.isAutoCaptured();
        return copy;
    }

    Session(Map<String, Object> map, Logger logger, String apiKey) {
        this(null, null, logger, apiKey);
        setId((String) map.get("id"));

        String timestamp = (String) map.get("startedAt");
        setStartedAt(DateUtils.fromIso8601(timestamp));

        @SuppressWarnings("unchecked")
        Map<String, Object> events = (Map<String, Object>) map.get("events");

        Number handled = (Number) events.get("handled");
        handledCount.set(handled.intValue());

        Number unhandled = (Number) events.get("unhandled");
        unhandledCount.set(unhandled.intValue());
    }

    Session(String id, Date startedAt, User user, boolean autoCaptured,
            Notifier notifier, Logger logger, String apiKey) {
        this(null, notifier, logger, apiKey);
        this.id = id;
        this.startedAt = new Date(startedAt.getTime());
        this.user = user;
        this.autoCaptured = autoCaptured;
        this.apiKey = apiKey;
    }

    Session(String id, Date startedAt, User user, int unhandledCount, int handledCount,
            Notifier notifier, Logger logger, String apiKey) {
        this(id, startedAt, user, false, notifier, logger, apiKey);
        this.unhandledCount.set(unhandledCount);
        this.handledCount.set(handledCount);
        this.tracked.set(true);
        this.apiKey = apiKey;
    }

    Session(File file, Notifier notifier, Logger logger, String apiKey) {
        this.file = file;
        this.logger = logger;
        this.apiKey = SessionFilenameInfo.findApiKeyInFilename(file, apiKey);
        if (notifier != null) {
            Notifier copy = new Notifier(notifier.getName(),
                    notifier.getVersion(), notifier.getUrl());
            copy.setDependencies(new ArrayList<>(notifier.getDependencies()));
            this.notifier = copy;
        } else {
            this.notifier = null;
        }
    }

    private void logNull(String property) {
        logger.e("Invalid null value supplied to session." + property + ", ignoring");
    }

    /**
     * Retrieves the session ID. This must be a unique value across all of your sessions.
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Sets the session ID. This must be a unique value across all of your sessions.
     */
    public void setId(@NonNull String id) {
        if (id != null) {
            this.id = id;
        } else {
            logNull("id");
        }
    }

    /**
     * Gets the session start time.
     */
    @NonNull
    public Date getStartedAt() {
        return startedAt;
    }

    /**
     * Sets the session start time.
     */
    public void setStartedAt(@NonNull Date startedAt) {
        if (startedAt != null) {
            this.startedAt = startedAt;
        } else {
            logNull("startedAt");
        }
    }

    /**
     * Returns the currently set User information.
     */
    @NonNull
    @Override
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with the session.
     */
    @Override
    public void setUser(@Nullable String id, @Nullable String email, @Nullable String name) {
        user = new User(id, email, name);
    }

    /**
     * Information set by the notifier about your app can be found in this field. These values
     * can be accessed and amended if necessary.
     */
    @NonNull
    public App getApp() {
        return app;
    }

    /**
     * Information set by the notifier about your device can be found in this field. These values
     * can be accessed and amended if necessary.
     */
    @NonNull
    public Device getDevice() {
        return device;
    }

    void setApp(App app) {
        this.app = app;
    }

    void setDevice(Device device) {
        this.device = device;
    }

    int getUnhandledCount() {
        return unhandledCount.intValue();
    }

    int getHandledCount() {
        return handledCount.intValue();
    }

    Session incrementHandledAndCopy() {
        handledCount.incrementAndGet();
        return copySession(this);
    }

    Session incrementUnhandledAndCopy() {
        unhandledCount.incrementAndGet();
        return copySession(this);
    }

    boolean markTracked() {
        return tracked.compareAndSet(false, true);
    }

    boolean markResumed() {
        return isPaused.compareAndSet(true, false);
    }

    void markPaused() {
        isPaused.set(true);
    }

    boolean isPaused() {
        return isPaused.get();
    }

    boolean isAutoCaptured() {
        return autoCaptured;
    }

    void setAutoCaptured(boolean autoCaptured) {
        this.autoCaptured = autoCaptured;
    }

    /**
     * Determines whether a cached session payload is v1 (where only the session is stored)
     * or v2 (where the whole payload including app/device is stored).
     *
     * @return whether the payload is v2
     */

    boolean isLegacyPayload() {
        return !(file != null
                && (file.getName().endsWith("_v2.json") || file.getName().endsWith("_v3.json")));
    }

    Notifier getNotifier() {
        return notifier;
    }

    @Override
    public void toStream(@NonNull JsonStream writer) throws IOException {
        if (file != null) {
            if (!isLegacyPayload()) {
                serializePayload(writer);
            } else {
                serializeLegacyPayload(writer);
            }
        } else {
            writer.beginObject();
            writer.name("notifier").value(notifier);
            writer.name("app").value(app);
            writer.name("device").value(device);
            writer.name("sessions").beginArray();
            serializeSessionInfo(writer);
            writer.endArray();
            writer.endObject();
        }
    }

    @NonNull
    public byte[] toByteArray() throws IOException {
        return JsonHelper.INSTANCE.serialize(this);
    }

    @Nullable
    @Override
    public String getIntegrityToken() {
        return Deliverable.DefaultImpls.getIntegrityToken(this);
    }

    private void serializePayload(@NonNull JsonStream writer) throws IOException {
        writer.value(file);
    }

    private void serializeLegacyPayload(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name("notifier").value(notifier);
        writer.name("app").value(app);
        writer.name("device").value(device);
        writer.name("sessions").beginArray();
        writer.value(file);
        writer.endArray();
        writer.endObject();
    }

    void serializeSessionInfo(@NonNull JsonStream writer) throws IOException {
        writer.beginObject();
        writer.name("id").value(id);
        writer.name("startedAt").value(startedAt);
        writer.name("user").value(user);
        writer.endObject();
    }

    /**
     * The API key used for session sent to Bugsnag. Even though the API key is set when Bugsnag
     * is initialized, you may choose to send certain sessions to a different Bugsnag project.
     */
    public void setApiKey(@NonNull String apiKey) {
        if (apiKey != null) {
            this.apiKey = apiKey;
        } else {
            logNull("apiKey");
        }
    }

    /**
     * The API key used for session sent to Bugsnag. Even though the API key is set when Bugsnag
     * is initialized, you may choose to send certain sessions to a different Bugsnag project.
     */
    @NonNull
    public String getApiKey() {
        return apiKey;
    }
}
