package com.bugsnag.android;

import com.bugsnag.android.internal.ImmutableConfig;
import com.bugsnag.android.internal.JsonHelper;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Used as the entry point for native code to allow proguard to obfuscate other areas if needed
 */
public class NativeInterface {

    // The default charset on Android is always UTF-8
    private static Charset UTF8Charset = Charset.defaultCharset();

    /**
     * Static reference used if not using Bugsnag.start()
     */
    @SuppressLint("StaticFieldLeak")
    private static Client client;

    @NonNull
    private static Client getClient() {
        if (client != null) {
            return client;
        } else {
            return Bugsnag.getClient();
        }
    }

    /**
     * Create an empty Event for a "handled exception" report. The returned Event will have
     * no Error objects, metadata, breadcrumbs, or feature flags. It's indented that the caller
     * will populate the Error and then pass the Event object to
     * {@link Client#populateAndNotifyAndroidEvent(Event, OnErrorCallback)}.
     */
    private static Event createEmptyEvent() {
        Client client = getClient();

        return new Event(
                new EventInternal(
                        (Throwable) null,
                        client.getConfig(),
                        SeverityReason.newInstance(SeverityReason.REASON_HANDLED_EXCEPTION),
                        client.getMetadataState().getMetadata().copy()
                ),
                client.getLogger()
        );
    }

    /**
     * Caches a client instance for responding to future events
     */
    public static void setClient(@NonNull Client client) {
        NativeInterface.client = client;
    }

    @Nullable
    public static String getContext() {
        return getClient().getContext();
    }

    /**
     * Retrieves the directory used to store native crash reports
     */
    @NonNull
    public static File getNativeReportPath() {
        return getNativeReportPath(getPersistenceDirectory());
    }

    private static @NonNull File getNativeReportPath(@NonNull File persistenceDirectory) {
        return new File(persistenceDirectory, "bugsnag-native");
    }

    private static @NonNull File getPersistenceDirectory() {
        return getClient().getConfig().getPersistenceDirectory().getValue();
    }

    /**
     * Retrieve user data from the static Client instance as a Map
     */
    @NonNull
    @SuppressWarnings("unused")
    public static Map<String, String> getUser() {
        HashMap<String, String> userData = new HashMap<>();
        User user = getClient().getUser();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        return userData;
    }

    /**
     * Retrieve app data from the static Client instance as a Map
     */
    @NonNull
    @SuppressWarnings("unused")
    public static Map<String, Object> getApp() {
        HashMap<String, Object> data = new HashMap<>();
        AppDataCollector source = getClient().getAppDataCollector();
        AppWithState app = source.generateAppWithState();
        data.put("version", app.getVersion());
        data.put("releaseStage", app.getReleaseStage());
        data.put("id", app.getId());
        data.put("type", app.getType());
        data.put("buildUUID", app.getBuildUuid());
        data.put("duration", app.getDuration());
        data.put("durationInForeground", app.getDurationInForeground());
        data.put("versionCode", app.getVersionCode());
        data.put("inForeground", app.getInForeground());
        data.put("isLaunching", app.isLaunching());
        data.put("binaryArch", app.getBinaryArch());
        data.putAll(source.getAppDataMetadata());
        return data;
    }

    /**
     * Retrieve device data from the static Client instance as a Map
     */
    @NonNull
    @SuppressWarnings("unused")
    public static Map<String, Object> getDevice() {
        DeviceDataCollector source = getClient().getDeviceDataCollector();
        HashMap<String, Object> deviceData = new HashMap<>(source.getDeviceMetadata());

        DeviceWithState src = source.generateDeviceWithState(new Date().getTime());
        deviceData.put("freeDisk", src.getFreeDisk());
        deviceData.put("freeMemory", src.getFreeMemory());
        deviceData.put("orientation", src.getOrientation());
        deviceData.put("time", src.getTime());
        deviceData.put("cpuAbi", src.getCpuAbi());
        deviceData.put("jailbroken", src.getJailbroken());
        deviceData.put("id", src.getId());
        deviceData.put("locale", src.getLocale());
        deviceData.put("manufacturer", src.getManufacturer());
        deviceData.put("model", src.getModel());
        deviceData.put("osName", src.getOsName());
        deviceData.put("osVersion", src.getOsVersion());
        deviceData.put("runtimeVersions", src.getRuntimeVersions());
        deviceData.put("totalMemory", src.getTotalMemory());
        return deviceData;
    }

    /**
     * Retrieve the CPU ABI(s) for the current device
     */
    @NonNull
    public static String[] getCpuAbi() {
        return getClient().getDeviceDataCollector().getCpuAbi();
    }

    /**
     * Retrieves global metadata from the static Client instance as a Map
     */
    @NonNull
    public static Map<String, Object> getMetadata() {
        return getClient().getMetadata();
    }

    /**
     * Retrieves a list of stored breadcrumbs from the static Client instance
     */
    @NonNull
    public static List<Breadcrumb> getBreadcrumbs() {
        return getClient().getBreadcrumbs();
    }

    /**
     * Sets the user
     *
     * @param id    id
     * @param email email
     * @param name  name
     */
    @SuppressWarnings("unused")
    public static void setUser(@Nullable final String id,
                               @Nullable final String email,
                               @Nullable final String name) {
        Client client = getClient();
        client.setUser(id, email, name);
    }

    /**
     * Sets the user
     *
     * @param idBytes    id
     * @param emailBytes email
     * @param nameBytes  name
     */
    @SuppressWarnings("unused")
    public static void setUser(@Nullable final byte[] idBytes,
                               @Nullable final byte[] emailBytes,
                               @Nullable final byte[] nameBytes) {
        String id = idBytes == null ? null : new String(idBytes, UTF8Charset);
        String email = emailBytes == null ? null : new String(emailBytes, UTF8Charset);
        String name = nameBytes == null ? null : new String(nameBytes, UTF8Charset);
        setUser(id, email, name);
    }

    /**
     * Leave a "breadcrumb" log message
     */
    public static void leaveBreadcrumb(@NonNull final String name,
                                       @NonNull final BreadcrumbType type) {
        if (name == null) {
            return;
        }
        getClient().leaveBreadcrumb(name, new HashMap<String, Object>(), type);
    }

    /**
     * Leave a "breadcrumb" log message
     */
    public static void leaveBreadcrumb(@NonNull final byte[] nameBytes,
                                       @NonNull final BreadcrumbType type) {
        if (nameBytes == null) {
            return;
        }
        String name = new String(nameBytes, UTF8Charset);
        getClient().leaveBreadcrumb(name, new HashMap<String, Object>(), type);
    }

    /**
     * Leaves a breadcrumb on the static client instance
     */
    public static void leaveBreadcrumb(@NonNull String message,
                                       @NonNull String type,
                                       @NonNull Map<String, Object> metadata) {
        String typeName = type.toUpperCase(Locale.US);
        getClient().leaveBreadcrumb(message, metadata, BreadcrumbType.valueOf(typeName));
    }

    /**
     * Remove metadata from subsequent exception reports
     */
    public static void clearMetadata(@NonNull String section, @Nullable String key) {
        if (key == null) {
            getClient().clearMetadata(section);
        } else {
            getClient().clearMetadata(section, key);
        }
    }

    /**
     * Add metadata to subsequent exception reports
     */
    public static void addMetadata(@NonNull final String tab,
                                   @Nullable final String key,
                                   @Nullable final Object value) {
        getClient().addMetadata(tab, key, value);
    }

    /**
     * Return the client report release stage
     */
    @Nullable
    public static String getReleaseStage() {
        return getClient().getConfig().getReleaseStage();
    }

    /**
     * Return the client session endpoint
     */
    @NonNull
    public static String getSessionEndpoint() {
        return getClient().getConfig().getEndpoints().getSessions();
    }

    /**
     * Return the client report endpoint
     */
    @NonNull
    public static String getEndpoint() {
        return getClient().getConfig().getEndpoints().getNotify();
    }

    /**
     * Set the client report context
     */
    public static void setContext(@Nullable final String context) {
        getClient().setContext(context);
    }

    /**
     * Set the binary arch used in the application
     */
    public static void setBinaryArch(@NonNull final String binaryArch) {
        getClient().setBinaryArch(binaryArch);
    }

    /**
     * Return the client report app version
     */
    @Nullable
    public static String getAppVersion() {
        return getClient().getConfig().getAppVersion();
    }

    /**
     * Return which release stages notify
     */
    @Nullable
    public static Collection<String> getEnabledReleaseStages() {
        return getClient().getConfig().getEnabledReleaseStages();
    }

    /**
     * Update the current session with a given start time, ID, and event counts
     */
    public static void registerSession(long startedAt, @Nullable String sessionId,
                                       int unhandledCount, int handledCount) {
        Client client = getClient();
        User user = client.getUser();
        Date startDate = startedAt > 0 ? new Date(startedAt) : null;
        client.getSessionTracker().registerExistingSession(startDate, sessionId, user,
                unhandledCount, handledCount);
    }

    /**
     * Ask if an error class is on the configurable discard list.
     * This is used by the native layer to decide whether to pass an event to
     * deliverReport() or not.
     *
     * @param name The error class to ask about.
     */
    @SuppressWarnings("unused")
    public static boolean isDiscardErrorClass(@NonNull String name) {
        return getClient().getConfig().getDiscardClasses().contains(name);
    }

    @SuppressWarnings("unchecked")
    private static void deepMerge(Map<String, Object> src, Map<String, Object> dst) {
        for (Map.Entry<String, Object> entry: src.entrySet()) {
            String key = entry.getKey();
            Object srcValue = entry.getValue();
            Object dstValue = dst.get(key);
            if (srcValue instanceof Map && (dstValue instanceof Map)) {
                deepMerge((Map<String, Object>)srcValue, (Map<String, Object>)dstValue);
            } else if (srcValue instanceof Collection && dstValue instanceof Collection) {
                // Just append everything because we don't know enough about the context or
                // provenance of the data to make an intelligent decision about this.
                ((Collection<Object>)dstValue).addAll((Collection<Object>)srcValue);
            } else {
                dst.put(key, srcValue);
            }
        }
    }

    /**
     * Deliver a report, serialized as an event JSON payload.
     *
     * @param releaseStageBytes The release stage in which the event was
     *                          captured. Used to determine whether the report
     *                          should be discarded, based on configured release
     *                          stages
     * @param payloadBytes      The raw JSON payload of the event
     * @param apiKey            The apiKey for the event
     * @param isLaunching       whether the crash occurred when the app was launching
     */
    @SuppressWarnings("unused")
    public static void deliverReport(@Nullable byte[] releaseStageBytes,
                                     @NonNull byte[] payloadBytes,
                                     @Nullable byte[] staticDataBytes,
                                     @NonNull String apiKey,
                                     boolean isLaunching) {
        // If there's saved static data, merge it directly into the payload map.
        if (staticDataBytes != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadMap = (Map<String, Object>) JsonHelper.INSTANCE.deserialize(
                    new ByteArrayInputStream(payloadBytes));
            @SuppressWarnings("unchecked")
            Map<String, Object> staticDataMap =
                    (Map<String, Object>) JsonHelper.INSTANCE.deserialize(
                    new ByteArrayInputStream(staticDataBytes));
            deepMerge(staticDataMap, payloadMap);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JsonHelper.INSTANCE.serialize(payloadMap, os);
            payloadBytes = os.toByteArray();
        }

        String payload = new String(payloadBytes, UTF8Charset);
        String releaseStage = releaseStageBytes == null
                ? null
                : new String(releaseStageBytes, UTF8Charset);
        Client client = getClient();
        ImmutableConfig config = client.getConfig();
        if (releaseStage == null
                || releaseStage.length() == 0
                || !config.shouldDiscardByReleaseStage()) {
            EventStore eventStore = client.getEventStore();

            String filename = eventStore.getNdkFilename(payload, apiKey);
            if (isLaunching) {
                filename = filename.replace(".json", "startupcrash.json");
            }
            eventStore.enqueueContentForDelivery(payload, filename);
        }
    }

    /**
     * Notifies using the Android SDK
     *
     * @param nameBytes    the error name
     * @param messageBytes the error message
     * @param severity     the error severity
     * @param stacktrace   a stacktrace
     */
    public static void notify(@NonNull final byte[] nameBytes,
                              @NonNull final byte[] messageBytes,
                              @NonNull final Severity severity,
                              @NonNull final StackTraceElement[] stacktrace) {
        if (nameBytes == null || messageBytes == null || stacktrace == null) {
            return;
        }
        String name = new String(nameBytes, UTF8Charset);
        String message = new String(messageBytes, UTF8Charset);
        notify(name, message, severity, stacktrace);
    }

    /**
     * Notifies using the Android SDK
     *
     * @param name       the error name
     * @param message    the error message
     * @param severity   the error severity
     * @param stacktrace a stacktrace
     */
    public static void notify(@NonNull final String name,
                              @NonNull final String message,
                              @NonNull final Severity severity,
                              @NonNull final StackTraceElement[] stacktrace) {
        if (getClient().getConfig().shouldDiscardError(name)) {
            return;
        }
        Throwable exc = new RuntimeException();
        exc.setStackTrace(stacktrace);

        getClient().notify(exc, new OnErrorCallback() {
            @Override
            public boolean onError(@NonNull Event event) {
                event.updateSeverityInternal(severity);
                List<Error> errors = event.getErrors();
                Error error = event.getErrors().get(0);

                // update the error's type to C
                if (!errors.isEmpty()) {
                    error.setErrorClass(name);
                    error.setErrorMessage(message);

                    for (Error err : errors) {
                        err.setType(ErrorType.C);
                    }
                }
                return true;
            }
        });
    }

    /**
     * Notifies using the Android SDK
     *
     * @param nameBytes    the error name
     * @param messageBytes the error message
     * @param severity     the error severity
     * @param stacktrace   a stacktrace
     */
    public static void notify(@NonNull final byte[] nameBytes,
                              @NonNull final byte[] messageBytes,
                              @NonNull final Severity severity,
                              @NonNull final NativeStackframe[] stacktrace) {

        if (nameBytes == null || messageBytes == null || stacktrace == null) {
            return;
        }
        String name = new String(nameBytes, UTF8Charset);
        String message = new String(messageBytes, UTF8Charset);
        notify(name, message, severity, stacktrace);
    }

    /**
     * Notifies using the Android SDK
     *
     * @param name       the error name
     * @param message    the error message
     * @param severity   the error severity
     * @param stacktrace a stacktrace
     */
    public static void notify(@NonNull final String name,
                              @NonNull final String message,
                              @NonNull final Severity severity,
                              @NonNull final NativeStackframe[] stacktrace) {
        Client client = getClient();

        if (client.getConfig().shouldDiscardError(name)) {
            return;
        }

        Event event = createEmptyEvent();
        event.updateSeverityInternal(severity);

        List<Stackframe> stackframes = new ArrayList<>(stacktrace.length);
        for (NativeStackframe nativeStackframe : stacktrace) {
            stackframes.add(new Stackframe(nativeStackframe));
        }
        event.getErrors().add(new Error(
                new ErrorInternal(name, message, new Stacktrace(stackframes), ErrorType.C),
                client.getLogger()
        ));

        getClient().populateAndNotifyAndroidEvent(event, null);
    }

    /**
     * Create an {@code Event} object
     *
     * @param exc            the Throwable object that caused the event
     * @param client         the Client object that the event is associated with
     * @param severityReason the severity of the Event
     * @return a new {@code Event} object
     */
    @NonNull
    public static Event createEvent(@Nullable Throwable exc,
                                    @NonNull Client client,
                                    @NonNull SeverityReason severityReason) {
        Metadata metadata = client.getMetadataState().getMetadata();
        FeatureFlags featureFlags = client.getFeatureFlagState().getFeatureFlags();
        return new Event(exc, client.getConfig(), severityReason, metadata, featureFlags,
                client.logger);
    }

    @NonNull
    public static Logger getLogger() {
        return getClient().getConfig().getLogger();
    }

    /**
     * Switches automatic error detection on/off after Bugsnag has initialized.
     * This is required to support legacy functionality in Unity.
     *
     * @param autoNotify whether errors should be automatically detected.
     */
    public static void setAutoNotify(boolean autoNotify) {
        getClient().setAutoNotify(autoNotify);
    }

    /**
     * Switches automatic ANR detection on/off after Bugsnag has initialized.
     * This is required to support legacy functionality in Unity.
     *
     * @param autoDetectAnrs whether ANRs should be automatically detected.
     */
    public static void setAutoDetectAnrs(boolean autoDetectAnrs) {
        getClient().setAutoDetectAnrs(autoDetectAnrs);
    }

    public static void startSession() {
        getClient().startSession();
    }

    public static void pauseSession() {
        getClient().pauseSession();
    }

    public static boolean resumeSession() {
        return getClient().resumeSession();
    }

    @Nullable
    public static Session getCurrentSession() {
        return getClient().sessionTracker.getCurrentSession();
    }

    /**
     * Marks the launch period as complete
     */
    public static void markLaunchCompleted() {
        getClient().markLaunchCompleted();
    }

    /**
     * Get the last run info object
     */
    @Nullable
    public static LastRunInfo getLastRunInfo() {
        return getClient().getLastRunInfo();
    }
}
