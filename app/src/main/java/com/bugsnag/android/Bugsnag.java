package com.bugsnag.android;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static access to a Bugsnag Client, the easiest way to use Bugsnag in your Android app.
 * For example:
 * <p>
 * Bugsnag.start(this, "your-api-key");
 * Bugsnag.notify(new RuntimeException("something broke!"));
 *
 * @see Client
 */
@SuppressWarnings("checkstyle:JavadocTagContinuationIndentation")
public final class Bugsnag {

    private static final Object lock = new Object();

    @SuppressLint("StaticFieldLeak")
    static Client client;

    private Bugsnag() {
    }

    /**
     * Initialize the static Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     */
    @NonNull
    public static Client start(@NonNull Context androidContext) {
        return start(androidContext, Configuration.load(androidContext));
    }

    /**
     * Initialize the static Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     * @param apiKey         your Bugsnag API key from your Bugsnag dashboard
     */
    @NonNull
    public static Client start(@NonNull Context androidContext, @NonNull String apiKey) {
        return start(androidContext, Configuration.load(androidContext, apiKey));
    }

    /**
     * Initialize the static Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     * @param config         a configuration for the Client
     */
    @NonNull
    public static Client start(@NonNull Context androidContext, @NonNull Configuration config) {
        synchronized (lock) {
            if (client == null) {
                client = new Client(androidContext, config);
            } else {
                logClientInitWarning();
            }
        }
        return client;
    }

    /**
     * Returns true if one of the <code>start</code> methods have been has been called and
     * so Bugsnag is initialized; false if <code>start</code> has not been called and the
     * other methods will throw IllegalStateException.
     */
    public static boolean isStarted() {
        return client != null;
    }

    private static void logClientInitWarning() {
        getClient().logger.w("Multiple Bugsnag.start calls detected. Ignoring.");
    }

    /**
     * Bugsnag uses the concept of "contexts" to help display and group your errors. Contexts
     * represent what was happening in your application at the time an error occurs.
     * <p>
     * In an android app the "context" is automatically set as the foreground Activity.
     * If you would like to set this value manually, you should alter this property.
     */
    @Nullable
    public static String getContext() {
        return getClient().getContext();
    }

    /**
     * Bugsnag uses the concept of "contexts" to help display and group your errors. Contexts
     * represent what was happening in your application at the time an error occurs.
     * <p>
     * In an android app the "context" is automatically set as the foreground Activity.
     * If you would like to set this value manually, you should alter this property.
     */
    public static void setContext(@Nullable final String context) {
        getClient().setContext(context);
    }

    /**
     * Sets the user associated with the event.
     */
    public static void setUser(@Nullable final String id,
                               @Nullable final String email,
                               @Nullable final String name) {
        getClient().setUser(id, email, name);
    }

    /**
     * Returns the currently set User information.
     */
    @NonNull
    public static User getUser() {
        return getClient().getUser();
    }

    /**
     * Add a "on error" callback, to execute code at the point where an error report is
     * captured in Bugsnag.
     * <p>
     * You can use this to add or modify information attached to an Event
     * before it is sent to your dashboard. You can also return
     * <code>false</code> from any callback to prevent delivery. "on error"
     * callbacks do not run before reports generated in the event
     * of immediate app termination from crashes in C/C++ code.
     * <p>
     * For example:
     * <p>
     * Bugsnag.addOnError(new OnErrorCallback() {
     * public boolean run(Event event) {
     * event.setSeverity(Severity.INFO);
     * return true;
     * }
     * })
     *
     * @param onError a callback to run before sending errors to Bugsnag
     * @see OnErrorCallback
     */
    public static void addOnError(@NonNull OnErrorCallback onError) {
        getClient().addOnError(onError);
    }

    /**
     * Removes a previously added "on error" callback
     *
     * @param onError the callback to remove
     */
    public static void removeOnError(@NonNull OnErrorCallback onError) {
        getClient().removeOnError(onError);
    }

    /**
     * Add an "on breadcrumb" callback, to execute code before every
     * breadcrumb captured by Bugsnag.
     * <p>
     * You can use this to modify breadcrumbs before they are stored by Bugsnag.
     * You can also return <code>false</code> from any callback to ignore a breadcrumb.
     * <p>
     * For example:
     * <p>
     * Bugsnag.onBreadcrumb(new OnBreadcrumbCallback() {
     * public boolean run(Breadcrumb breadcrumb) {
     * return false; // ignore the breadcrumb
     * }
     * })
     *
     * @param onBreadcrumb a callback to run before a breadcrumb is captured
     * @see OnBreadcrumbCallback
     */
    public static void addOnBreadcrumb(@NonNull final OnBreadcrumbCallback onBreadcrumb) {
        getClient().addOnBreadcrumb(onBreadcrumb);
    }

    /**
     * Removes a previously added "on breadcrumb" callback
     *
     * @param onBreadcrumb the callback to remove
     */
    public static void removeOnBreadcrumb(@NonNull OnBreadcrumbCallback onBreadcrumb) {
        getClient().removeOnBreadcrumb(onBreadcrumb);
    }

    /**
     * Add an "on session" callback, to execute code before every
     * session captured by Bugsnag.
     * <p>
     * You can use this to modify sessions before they are stored by Bugsnag.
     * You can also return <code>false</code> from any callback to ignore a session.
     * <p>
     * For example:
     * <p>
     * Bugsnag.onSession(new OnSessionCallback() {
     * public boolean run(Session session) {
     * return false; // ignore the session
     * }
     * })
     *
     * @param onSession a callback to run before a session is captured
     * @see OnSessionCallback
     */
    public static void addOnSession(@NonNull OnSessionCallback onSession) {
        getClient().addOnSession(onSession);
    }

    /**
     * Removes a previously added "on session" callback
     *
     * @param onSession the callback to remove
     */
    public static void removeOnSession(@NonNull OnSessionCallback onSession) {
        getClient().removeOnSession(onSession);
    }

    /**
     * Notify Bugsnag of a handled exception
     *
     * @param exception the exception to send to Bugsnag
     */
    public static void notify(@NonNull final Throwable exception) {
        getClient().notify(exception);
    }

    /**
     * Notify Bugsnag of a handled exception
     *
     * @param exception the exception to send to Bugsnag
     * @param onError   callback invoked on the generated error report for
     *                  additional modification
     */
    public static void notify(@NonNull final Throwable exception,
                              @Nullable final OnErrorCallback onError) {
        getClient().notify(exception, onError);
    }

    /**
     * Adds a map of multiple metadata key-value pairs to the specified section.
     */
    public static void addMetadata(@NonNull String section, @NonNull Map<String, ?> value) {
        getClient().addMetadata(section, value);
    }

    /**
     * Adds the specified key and value in the specified section. The value can be of
     * any primitive type or a collection such as a map, set or array.
     */
    public static void addMetadata(@NonNull String section, @NonNull String key,
                                   @Nullable Object value) {
        getClient().addMetadata(section, key, value);
    }

    /**
     * Removes all the data from the specified section.
     */
    public static void clearMetadata(@NonNull String section) {
        getClient().clearMetadata(section);
    }

    /**
     * Removes data with the specified key from the specified section.
     */
    public static void clearMetadata(@NonNull String section, @NonNull String key) {
        getClient().clearMetadata(section, key);
    }

    /**
     * Returns a map of data in the specified section.
     */
    @Nullable
    public static Map<String, Object> getMetadata(@NonNull String section) {
        return getClient().getMetadata(section);
    }

    /**
     * Returns the value of the specified key in the specified section.
     */
    @Nullable
    public static Object getMetadata(@NonNull String section, @NonNull String key) {
        return getClient().getMetadata(section, key);
    }

    /**
     * Leave a "breadcrumb" log message, representing an action that occurred
     * in your app, to aid with debugging.
     *
     * @param message the log message to leave
     */
    public static void leaveBreadcrumb(@NonNull String message) {
        getClient().leaveBreadcrumb(message);
    }

    /**
     * Leave a "breadcrumb" log message representing an action or event which
     * occurred in your app, to aid with debugging
     *
     * @param message  A short label
     * @param metadata Additional diagnostic information about the app environment
     * @param type     A category for the breadcrumb
     */
    public static void leaveBreadcrumb(@NonNull String message,
                                       @NonNull Map<String, Object> metadata,
                                       @NonNull BreadcrumbType type) {
        getClient().leaveBreadcrumb(message, metadata, type);
    }

    /**
     * Starts tracking a new session. You should disable automatic session tracking via
     * {@link Configuration#setAutoTrackSessions(boolean)} if you call this method.
     * <p/>
     * You should call this at the appropriate time in your application when you wish to start a
     * session. Any subsequent errors which occur in your application will still be reported to
     * Bugsnag but will not count towards your application's
     * <a href="https://docs.bugsnag.com/product/releases/releases-dashboard/#stability-score">
     * stability score</a>. This will start a new session even if there is already an existing
     * session; you should call {@link #resumeSession()} if you only want to start a session
     * when one doesn't already exist.
     *
     * @see #resumeSession()
     * @see #pauseSession()
     * @see Configuration#setAutoTrackSessions(boolean)
     */
    public static void startSession() {
        getClient().startSession();
    }

    /**
     * Resumes a session which has previously been paused, or starts a new session if none exists.
     * If a session has already been resumed or started and has not been paused, calling this
     * method will have no effect. You should disable automatic session tracking via
     * {@link Configuration#setAutoTrackSessions(boolean)} if you call this method.
     * <p/>
     * It's important to note that sessions are stored in memory for the lifetime of the
     * application process and are not persisted on disk. Therefore calling this method on app
     * startup would start a new session, rather than continuing any previous session.
     * <p/>
     * You should call this at the appropriate time in your application when you wish to resume
     * a previously started session. Any subsequent errors which occur in your application will
     * still be reported to Bugsnag but will not count towards your application's
     * <a href="https://docs.bugsnag.com/product/releases/releases-dashboard/#stability-score">
     * stability score</a>.
     *
     * @return true if a previous session was resumed, false if a new session was started.
     * @see #startSession()
     * @see #pauseSession()
     * @see Configuration#setAutoTrackSessions(boolean)
     */
    public static boolean resumeSession() {
        return getClient().resumeSession();
    }

    /**
     * Pauses tracking of a session. You should disable automatic session tracking via
     * {@link Configuration#setAutoTrackSessions(boolean)} if you call this method.
     * <p/>
     * You should call this at the appropriate time in your application when you wish to pause a
     * session. Any subsequent errors which occur in your application will still be reported to
     * Bugsnag but will not count towards your application's
     * <a href="https://docs.bugsnag.com/product/releases/releases-dashboard/#stability-score">
     * stability score</a>. This can be advantageous if, for example, you do not wish the
     * stability score to include crashes in a background service.
     *
     * @see #startSession()
     * @see #resumeSession()
     * @see Configuration#setAutoTrackSessions(boolean)
     */
    public static void pauseSession() {
        getClient().pauseSession();
    }

    /**
     * Returns the current buffer of breadcrumbs that will be sent with captured events. This
     * ordered list represents the most recent breadcrumbs to be captured up to the limit
     * set in {@link Configuration#getMaxBreadcrumbs()}.
     * <p>
     * The returned collection is readonly and mutating the list will cause no effect on the
     * Client's state. If you wish to alter the breadcrumbs collected by the Client then you should
     * use {@link Configuration#setEnabledBreadcrumbTypes(Set)} and
     * {@link Configuration#addOnBreadcrumb(OnBreadcrumbCallback)} instead.
     *
     * @return a list of collected breadcrumbs
     */
    @NonNull
    public static List<Breadcrumb> getBreadcrumbs() {
        return getClient().getBreadcrumbs();
    }

    /**
     * Retrieves information about the last launch of the application, if it has been run before.
     * <p>
     * For example, this allows checking whether the app crashed on its last launch, which could
     * be used to perform conditional behaviour to recover from crashes, such as clearing the
     * app data cache.
     */
    @Nullable
    public static LastRunInfo getLastRunInfo() {
        return getClient().getLastRunInfo();
    }

    /**
     * Informs Bugsnag that the application has finished launching. Once this has been called
     * {@link AppWithState#isLaunching()} will always be false in any new error reports,
     * and synchronous delivery will not be attempted on the next launch for any fatal crashes.
     * <p>
     * By default this method will be called after Bugsnag is initialized when
     * {@link Configuration#getLaunchDurationMillis()} has elapsed. Invoking this method manually
     * has precedence over the value supplied via the launchDurationMillis configuration option.
     */
    public static void markLaunchCompleted() {
        getClient().markLaunchCompleted();
    }

    /**
     * Add a single feature flag with no variant. If there is an existing feature flag with the
     * same name, it will be overwritten to have no variant.
     *
     * @param name the name of the feature flag to add
     * @see #addFeatureFlag(String, String)
     */
    public static void addFeatureFlag(@NonNull String name) {
        getClient().addFeatureFlag(name);
    }

    /**
     * Add a single feature flag with an optional variant. If there is an existing feature
     * flag with the same name, it will be overwritten with the new variant. If the variant is
     * {@code null} this method has the same behaviour as {@link #addFeatureFlag(String)}.
     *
     * @param name    the name of the feature flag to add
     * @param variant the variant to set the feature flag to, or {@code null} to specify a feature
     *                flag with no variant
     */
    public static void addFeatureFlag(@NonNull String name, @Nullable String variant) {
        getClient().addFeatureFlag(name, variant);
    }

    /**
     * Add a collection of feature flags. This method behaves exactly the same as calling
     * {@link #addFeatureFlag(String, String)} for each of the {@code FeatureFlag} objects.
     *
     * @param featureFlags the feature flags to add
     * @see #addFeatureFlag(String, String)
     */
    public static void addFeatureFlags(@NonNull Iterable<FeatureFlag> featureFlags) {
        getClient().addFeatureFlags(featureFlags);
    }

    /**
     * Remove a single feature flag regardless of its current status. This will stop the specified
     * feature flag from being reported. If the named feature flag does not exist this will
     * have no effect.
     *
     * @param name the name of the feature flag to remove
     */
    public static void clearFeatureFlag(@NonNull String name) {
        getClient().clearFeatureFlag(name);
    }

    /**
     * Clear all of the feature flags. This will stop all feature flags from being reported.
     */
    public static void clearFeatureFlags() {
        getClient().clearFeatureFlags();
    }

    /**
     * Get the current Bugsnag Client instance.
     */
    @NonNull
    public static Client getClient() {
        if (client == null) {
            synchronized (lock) {
                if (client == null) {
                    throw new IllegalStateException("You must call Bugsnag.start before any"
                            + " other Bugsnag methods");
                }
            }
        }

        return client;
    }
}
