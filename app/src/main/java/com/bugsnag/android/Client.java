package com.bugsnag.android;

import static com.bugsnag.android.SeverityReason.REASON_HANDLED_EXCEPTION;

import com.bugsnag.android.internal.BackgroundTaskService;
import com.bugsnag.android.internal.ForegroundDetector;
import com.bugsnag.android.internal.ImmutableConfig;
import com.bugsnag.android.internal.InternalMetrics;
import com.bugsnag.android.internal.InternalMetricsImpl;
import com.bugsnag.android.internal.InternalMetricsNoop;
import com.bugsnag.android.internal.StateObserver;
import com.bugsnag.android.internal.TaskType;
import com.bugsnag.android.internal.dag.ConfigModule;
import com.bugsnag.android.internal.dag.ContextModule;
import com.bugsnag.android.internal.dag.Provider;
import com.bugsnag.android.internal.dag.SystemServiceModule;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

/**
 * A Bugsnag Client instance allows you to use Bugsnag in your Android app.
 * Typically you'd instead use the static access provided in the Bugsnag class.
 * <p/>
 * Example usage:
 * <p/>
 * Client client = new Client(this, "your-api-key");
 * client.notify(new RuntimeException("something broke!"));
 *
 * @see Bugsnag
 */
@SuppressWarnings({"checkstyle:JavadocTagContinuationIndentation", "ConstantConditions"})
public class Client implements MetadataAware, CallbackAware, UserAware, FeatureFlagAware {

    final ImmutableConfig immutableConfig;

    final MetadataState metadataState;
    final FeatureFlagState featureFlagState;

    private final InternalMetrics internalMetrics;
    private final ContextState contextState;
    private final CallbackState callbackState;
    private final Provider<UserState> userState;
    private final Map<String, Object> configDifferences;

    final Context appContext;

    @NonNull
    final DeviceDataCollector deviceDataCollector;

    @NonNull
    final AppDataCollector appDataCollector;

    @NonNull
    final BreadcrumbState breadcrumbState;

    @NonNull
    final MemoryTrimState memoryTrimState = new MemoryTrimState();

    @NonNull
    protected final EventStore eventStore;

    final SessionTracker sessionTracker;

    final SystemBroadcastReceiver systemBroadcastReceiver;

    final Logger logger;
    final Connectivity connectivity;
    final DeliveryDelegate deliveryDelegate;

    final ClientObservable clientObservable;
    PluginClient pluginClient;

    final Notifier notifier;

    @Nullable
    final LastRunInfo lastRunInfo;
    final LastRunInfoStore lastRunInfoStore;
    final LaunchCrashTracker launchCrashTracker;
    final BackgroundTaskService bgTaskService = new BackgroundTaskService();
    private final ExceptionHandler exceptionHandler;

    /**
     * Initialize a Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     */
    public Client(@NonNull Context androidContext) {
        this(androidContext, Configuration.load(androidContext));
    }

    /**
     * Initialize a Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     * @param apiKey         your Bugsnag API key from your Bugsnag dashboard
     */
    public Client(@NonNull Context androidContext, @NonNull String apiKey) {
        this(androidContext, Configuration.load(androidContext, apiKey));
    }

    /**
     * Initialize a Bugsnag client
     *
     * @param androidContext an Android context, usually <code>this</code>
     * @param configuration  a configuration for the Client
     */
    public Client(@NonNull Context androidContext, @NonNull final Configuration configuration) {
        ContextModule contextModule = new ContextModule(androidContext, bgTaskService);
        appContext = contextModule.getCtx();

        notifier = configuration.getNotifier();

        connectivity = new ConnectivityCompat(appContext, new Function2<Boolean, String, Unit>() {
            @Override
            public Unit invoke(Boolean hasConnection, String networkState) {
                Map<String, Object> data = new HashMap<>();
                data.put("hasConnection", hasConnection);
                data.put("networkState", networkState);
                leaveAutoBreadcrumb("Connectivity changed", BreadcrumbType.STATE, data);
                if (hasConnection) {
                    eventStore.flushAsync();
                    sessionTracker.flushAsync();
                }
                return null;
            }
        });

        // set sensible defaults for delivery/project packages etc if not set
        ConfigModule configModule = new ConfigModule(
                contextModule,
                configuration,
                connectivity,
                bgTaskService
        );

        immutableConfig = configModule.getConfig();
        logger = immutableConfig.getLogger();

        if (!(androidContext instanceof Application)) {
            logger.w("You should initialize Bugsnag from the onCreate() callback of your "
                    + "Application subclass, as this guarantees errors are captured as early "
                    + "as possible. "
                    + "If a custom Application subclass is not possible in your app then you "
                    + "should suppress this warning by passing the Application context instead: "
                    + "Bugsnag.start(context.getApplicationContext()). "
                    + "For further info see: "
                    + "https://docs.bugsnag.com/platforms/android/#basic-configuration");
        }

        // setup storage as soon as possible
        final StorageModule storageModule = new StorageModule(appContext,
                immutableConfig, bgTaskService);

        // setup state trackers for bugsnag
        BugsnagStateModule bugsnagStateModule =
                new BugsnagStateModule(immutableConfig, configuration);
        clientObservable = bugsnagStateModule.getClientObservable();
        callbackState = bugsnagStateModule.getCallbackState();
        breadcrumbState = bugsnagStateModule.getBreadcrumbState();
        contextState = bugsnagStateModule.getContextState();
        metadataState = bugsnagStateModule.getMetadataState();
        featureFlagState = bugsnagStateModule.getFeatureFlagState();

        // lookup system services
        final SystemServiceModule systemServiceModule =
                new SystemServiceModule(contextModule, bgTaskService);

        // setup further state trackers and data collection
        TrackerModule trackerModule = new TrackerModule(configModule,
                storageModule, this, bgTaskService, callbackState);

        DataCollectionModule dataCollectionModule = new DataCollectionModule(contextModule,
                configModule, systemServiceModule, trackerModule,
                bgTaskService, connectivity, storageModule.getDeviceIdStore(),
                memoryTrimState);

        // load the device + user information
        userState = storageModule.loadUser(configuration.getUser());

        EventStorageModule eventStorageModule = new EventStorageModule(contextModule, configModule,
                dataCollectionModule, bgTaskService, trackerModule, systemServiceModule, notifier,
                callbackState);

        eventStore = eventStorageModule.getEventStore().get();

        deliveryDelegate = new DeliveryDelegate(logger, eventStore,
                immutableConfig, callbackState, notifier, bgTaskService);

        exceptionHandler = new ExceptionHandler(this, logger);

        // load last run info
        lastRunInfoStore = storageModule.getLastRunInfoStore().getOrNull();
        lastRunInfo = storageModule.getLastRunInfo().getOrNull();

        launchCrashTracker = trackerModule.getLaunchCrashTracker();
        sessionTracker = trackerModule.getSessionTracker().get();
        appDataCollector = dataCollectionModule.getAppDataCollector().get();
        deviceDataCollector = dataCollectionModule.getDeviceDataCollector().get();

        Set<Plugin> userPlugins = configuration.getPlugins();
        pluginClient = new PluginClient(userPlugins, immutableConfig, logger);

        if (configuration.getTelemetry().contains(Telemetry.USAGE)) {
            internalMetrics = new InternalMetricsImpl();
        } else {
            internalMetrics = new InternalMetricsNoop();
        }

        configDifferences = configuration.impl.getConfigDifferences();
        systemBroadcastReceiver = new SystemBroadcastReceiver(this, logger);

        start();
    }

    @VisibleForTesting
    Client(
            ImmutableConfig immutableConfig,
            MetadataState metadataState,
            ContextState contextState,
            CallbackState callbackState,
            Provider<UserState> userState,
            FeatureFlagState featureFlagState,
            ClientObservable clientObservable,
            Context appContext,
            @NonNull DeviceDataCollector deviceDataCollector,
            @NonNull AppDataCollector appDataCollector,
            @NonNull BreadcrumbState breadcrumbState,
            @NonNull EventStore eventStore,
            SystemBroadcastReceiver systemBroadcastReceiver,
            SessionTracker sessionTracker,
            Connectivity connectivity,
            Logger logger,
            DeliveryDelegate deliveryDelegate,
            LastRunInfoStore lastRunInfoStore,
            LaunchCrashTracker launchCrashTracker,
            ExceptionHandler exceptionHandler,
            Notifier notifier
    ) {
        this.immutableConfig = immutableConfig;
        this.metadataState = metadataState;
        this.contextState = contextState;
        this.callbackState = callbackState;
        this.userState = userState;
        this.featureFlagState = featureFlagState;
        this.clientObservable = clientObservable;
        this.appContext = appContext;
        this.deviceDataCollector = deviceDataCollector;
        this.appDataCollector = appDataCollector;
        this.breadcrumbState = breadcrumbState;
        this.eventStore = eventStore;
        this.systemBroadcastReceiver = systemBroadcastReceiver;
        this.sessionTracker = sessionTracker;
        this.connectivity = connectivity;
        this.logger = logger;
        this.deliveryDelegate = deliveryDelegate;
        this.lastRunInfoStore = lastRunInfoStore;
        this.launchCrashTracker = launchCrashTracker;
        this.lastRunInfo = null;
        this.exceptionHandler = exceptionHandler;
        this.notifier = notifier;
        internalMetrics = new InternalMetricsNoop();
        configDifferences = new HashMap<>();
    }

    private void start() {
        if (immutableConfig.getEnabledErrorTypes().getUnhandledExceptions()) {
            exceptionHandler.install();
        }

        // Initialise plugins before attempting anything else
        NativeInterface.setClient(Client.this);
        pluginClient.loadPlugins(Client.this);
        NdkPluginCaller.INSTANCE.setNdkPlugin(pluginClient.getNdkPlugin());
        if (immutableConfig.getTelemetry().contains(Telemetry.USAGE)) {
            NdkPluginCaller.INSTANCE.setInternalMetricsEnabled(true);
        }

        // Flush any on-disk errors and sessions
        eventStore.flushOnLaunch();
        eventStore.flushAsync();
        sessionTracker.flushAsync();

        // These call into NdkPluginCaller to sync with the native side, so they must happen later
        internalMetrics.setConfigDifferences(configDifferences);
        callbackState.setInternalMetrics(internalMetrics);

        // Register listeners for system events in the background
        registerLifecycleCallbacks();
        registerComponentCallbacks();
        registerListenersInBackground();

        // Leave auto breadcrumb
        Map<String, Object> data = new HashMap<>();
        leaveAutoBreadcrumb("Bugsnag loaded", BreadcrumbType.STATE, data);

        logger.d("Bugsnag loaded");
    }

    void registerLifecycleCallbacks() {
        if (appContext instanceof Application) {
            Application application = (Application) appContext;
            ForegroundDetector.registerOn(application);
            ForegroundDetector.registerActivityCallbacks(sessionTracker);

            if (!immutableConfig.shouldDiscardBreadcrumb(BreadcrumbType.STATE)) {
                ActivityBreadcrumbCollector activityCb = new ActivityBreadcrumbCollector(
                        new Function2<String, Map<String, ? extends Object>, Unit>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public Unit invoke(String activity, Map<String, ?> metadata) {
                                leaveBreadcrumb(activity, (Map<String, Object>) metadata,
                                        BreadcrumbType.STATE);
                                return null;
                            }
                        }
                );
                application.registerActivityLifecycleCallbacks(activityCb);
            }
        }
    }

    /**
     * Registers listeners for system events in the background. This offloads work from the main
     * thread that collects useful information from callbacks, but that don't need to be done
     * immediately on client construction.
     */
    void registerListenersInBackground() {
        try {
            bgTaskService.submitTask(TaskType.DEFAULT, new Runnable() {
                @Override
                public void run() {
                    connectivity.registerForNetworkChanges();
                    SystemBroadcastReceiver.register(appContext, systemBroadcastReceiver, logger);
                }
            });
        } catch (RejectedExecutionException ex) {
            logger.w("Failed to register for system events", ex);
        }
    }


    /**
     * Load information about the last run, and reset the persisted information to the defaults.
     */
    private void persistRunInfo(final LastRunInfo runInfo) {
        try {
            bgTaskService.submitTask(TaskType.IO, new Runnable() {
                @Override
                public void run() {
                    lastRunInfoStore.persist(runInfo);
                }
            });
        } catch (RejectedExecutionException exc) {
            logger.w("Failed to persist last run info", exc);
        }
    }

    private void logNull(String property) {
        logger.e("Invalid null value supplied to client." + property + ", ignoring");
    }

    private void registerComponentCallbacks() {
        appContext.registerComponentCallbacks(new ClientComponentCallbacks(
                deviceDataCollector,
                new Function2<String, String, Unit>() {
                    @Override
                    public Unit invoke(String oldOrientation, String newOrientation) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("from", oldOrientation);
                        data.put("to", newOrientation);
                        leaveAutoBreadcrumb("Orientation changed", BreadcrumbType.STATE, data);
                        clientObservable.postOrientationChange(newOrientation);
                        return null;
                    }
                }, new Function2<Boolean, Integer, Unit>() {
                    @Override
                    public Unit invoke(Boolean isLowMemory, Integer memoryTrimLevel) {
                        memoryTrimState.setLowMemory(Boolean.TRUE.equals(isLowMemory));
                        if (memoryTrimState.updateMemoryTrimLevel(memoryTrimLevel)) {
                            leaveAutoBreadcrumb(
                                    "Trim Memory",
                                    BreadcrumbType.STATE,
                                    Collections.<String, Object>singletonMap(
                                            "trimLevel", memoryTrimState.getTrimLevelDescription()
                                    )
                            );
                        }

                        memoryTrimState.emitObservableEvent();
                        return null;
                    }
                }
        ));
    }

    void setupNdkPlugin() {
        if (!setupNdkDirectory()) {
            logger.w("Failed to setup NDK directory.");
            return;
        }

        String lastRunInfoPath = lastRunInfoStore.getFile().getAbsolutePath();
        int crashes = (lastRunInfo != null) ? lastRunInfo.getConsecutiveLaunchCrashes() : 0;
        clientObservable.postNdkInstall(immutableConfig, lastRunInfoPath, crashes);
        syncInitialState();
        clientObservable.postNdkDeliverPending();
    }

    private boolean setupNdkDirectory() {
        try {
            return bgTaskService.submitTask(TaskType.IO, new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    File outFile = NativeInterface.getNativeReportPath();
                    return outFile.exists() || outFile.mkdirs();
                }
            }).get();
        } catch (Throwable exc) {
            return false;
        }
    }

    void addObserver(StateObserver observer) {
        metadataState.addObserver(observer);
        breadcrumbState.addObserver(observer);
        sessionTracker.addObserver(observer);
        clientObservable.addObserver(observer);
        userState.get().addObserver(observer);
        contextState.addObserver(observer);
        deliveryDelegate.addObserver(observer);
        launchCrashTracker.addObserver(observer);
        memoryTrimState.addObserver(observer);
        featureFlagState.addObserver(observer);
    }

    void removeObserver(StateObserver observer) {
        metadataState.removeObserver(observer);
        breadcrumbState.removeObserver(observer);
        sessionTracker.removeObserver(observer);
        clientObservable.removeObserver(observer);
        userState.get().removeObserver(observer);
        contextState.removeObserver(observer);
        deliveryDelegate.removeObserver(observer);
        launchCrashTracker.removeObserver(observer);
        memoryTrimState.removeObserver(observer);
        featureFlagState.removeObserver(observer);
    }

    /**
     * Sends initial state values for Metadata/User/Context to any registered observers.
     */
    void syncInitialState() {
        metadataState.emitObservableEvent();
        contextState.emitObservableEvent();
        userState.get().emitObservableEvent();
        memoryTrimState.emitObservableEvent();
        featureFlagState.emitObservableEvent();
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
    public void startSession() {
        sessionTracker.startSession(false);
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
    public void pauseSession() {
        sessionTracker.pauseSession();
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
    public boolean resumeSession() {
        return sessionTracker.resumeSession();
    }

    /**
     * Bugsnag uses the concept of "contexts" to help display and group your errors. Contexts
     * represent what was happening in your application at the time an error occurs.
     *
     * In an android app the "context" is automatically set as the foreground Activity.
     * If you would like to set this value manually, you should alter this property.
     */
    @Nullable
    public String getContext() {
        return contextState.getContext();
    }

    /**
     * Bugsnag uses the concept of "contexts" to help display and group your errors. Contexts
     * represent what was happening in your application at the time an error occurs.
     *
     * In an android app the "context" is automatically set as the foreground Activity.
     * If you would like to set this value manually, you should alter this property.
     */
    public void setContext(@Nullable String context) {
        contextState.setManualContext(context);
    }

    /**
     * Sets the user associated with the event.
     */
    @Override
    public void setUser(@Nullable String id, @Nullable String email, @Nullable String name) {
        userState.get().setUser(new User(id, email, name));
    }

    /**
     * Returns the currently set User information.
     */
    @NonNull
    @Override
    public User getUser() {
        return userState.get().getUser();
    }

    /**
     * Add a "on error" callback, to execute code at the point where an error report is
     * captured in Bugsnag.
     *
     * You can use this to add or modify information attached to an Event
     * before it is sent to your dashboard. You can also return
     * <code>false</code> from any callback to prevent delivery. "on error"
     * callbacks do not run before reports generated in the event
     * of immediate app termination from crashes in C/C++ code.
     *
     * For example:
     *
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
    @Override
    public void addOnError(@NonNull OnErrorCallback onError) {
        if (onError != null) {
            callbackState.addOnError(onError);
        } else {
            logNull("addOnError");
        }
    }

    /**
     * Removes a previously added "on error" callback
     *
     * @param onError the callback to remove
     */
    @Override
    public void removeOnError(@NonNull OnErrorCallback onError) {
        if (onError != null) {
            callbackState.removeOnError(onError);
        } else {
            logNull("removeOnError");
        }
    }

    /**
     * Add an "on breadcrumb" callback, to execute code before every
     * breadcrumb captured by Bugsnag.
     *
     * You can use this to modify breadcrumbs before they are stored by Bugsnag.
     * You can also return <code>false</code> from any callback to ignore a breadcrumb.
     *
     * For example:
     *
     * Bugsnag.onBreadcrumb(new OnBreadcrumbCallback() {
     * public boolean run(Breadcrumb breadcrumb) {
     * return false; // ignore the breadcrumb
     * }
     * })
     *
     * @param onBreadcrumb a callback to run before a breadcrumb is captured
     * @see OnBreadcrumbCallback
     */
    @Override
    public void addOnBreadcrumb(@NonNull OnBreadcrumbCallback onBreadcrumb) {
        if (onBreadcrumb != null) {
            callbackState.addOnBreadcrumb(onBreadcrumb);
        } else {
            logNull("addOnBreadcrumb");
        }
    }

    /**
     * Removes a previously added "on breadcrumb" callback
     *
     * @param onBreadcrumb the callback to remove
     */
    @Override
    public void removeOnBreadcrumb(@NonNull OnBreadcrumbCallback onBreadcrumb) {
        if (onBreadcrumb != null) {
            callbackState.removeOnBreadcrumb(onBreadcrumb);
        } else {
            logNull("removeOnBreadcrumb");
        }
    }

    /**
     * Add an "on session" callback, to execute code before every
     * session captured by Bugsnag.
     *
     * You can use this to modify sessions before they are stored by Bugsnag.
     * You can also return <code>false</code> from any callback to ignore a session.
     *
     * For example:
     *
     * Bugsnag.onSession(new OnSessionCallback() {
     * public boolean run(Session session) {
     * return false; // ignore the session
     * }
     * })
     *
     * @param onSession a callback to run before a session is captured
     * @see OnSessionCallback
     */
    @Override
    public void addOnSession(@NonNull OnSessionCallback onSession) {
        if (onSession != null) {
            callbackState.addOnSession(onSession);
        } else {
            logNull("addOnSession");
        }
    }

    /**
     * Removes a previously added "on session" callback
     *
     * @param onSession the callback to remove
     */
    @Override
    public void removeOnSession(@NonNull OnSessionCallback onSession) {
        if (onSession != null) {
            callbackState.removeOnSession(onSession);
        } else {
            logNull("removeOnSession");
        }
    }

    /**
     * Notify Bugsnag of a handled exception
     *
     * @param exception the exception to send to Bugsnag
     */
    public void notify(@NonNull Throwable exception) {
        notify(exception, null);
    }

    /**
     * Notify Bugsnag of a handled exception
     *
     * @param exc     the exception to send to Bugsnag
     * @param onError callback invoked on the generated error report for
     *                additional modification
     */
    public void notify(@NonNull Throwable exc, @Nullable OnErrorCallback onError) {
        if (exc != null) {
            if (immutableConfig.shouldDiscardError(exc)) {
                return;
            }
            SeverityReason severityReason = SeverityReason.newInstance(REASON_HANDLED_EXCEPTION);
            Metadata metadata = metadataState.getMetadata();
            FeatureFlags featureFlags = featureFlagState.getFeatureFlags();
            Event event = new Event(exc, immutableConfig, severityReason, metadata, featureFlags,
                    logger);
            populateAndNotifyAndroidEvent(event, onError);
        } else {
            logNull("notify");
        }
    }

    /**
     * Caches an error then attempts to notify.
     *
     * Should only ever be called from the {@link ExceptionHandler}.
     */
    void notifyUnhandledException(@NonNull Throwable exc, Metadata metadata,
                                  @SeverityReason.SeverityReasonType String severityReason,
                                  @Nullable String attributeValue) {
        SeverityReason handledState
                = SeverityReason.newInstance(severityReason, Severity.ERROR, attributeValue);
        Metadata data = Metadata.Companion.merge(metadataState.getMetadata(), metadata);
        Event event = new Event(exc, immutableConfig, handledState,
                data, featureFlagState.getFeatureFlags(), logger);
        populateAndNotifyAndroidEvent(event, null);

        // persist LastRunInfo so that on relaunch users can check the app crashed
        int consecutiveLaunchCrashes = lastRunInfo == null ? 0
                : lastRunInfo.getConsecutiveLaunchCrashes();
        boolean launching = launchCrashTracker.isLaunching();
        if (launching) {
            consecutiveLaunchCrashes += 1;
        }
        LastRunInfo runInfo = new LastRunInfo(consecutiveLaunchCrashes, true, launching);
        persistRunInfo(runInfo);

        // suspend execution of any further background tasks, waiting for previously
        // submitted ones to complete.
        bgTaskService.shutdown();
    }

    void populateAndNotifyAndroidEvent(@NonNull Event event,
                                       @Nullable OnErrorCallback onError) {
        // Capture the state of the app and device and attach diagnostics to the event
        event.setDevice(deviceDataCollector.generateDeviceWithState(new Date().getTime()));
        event.addMetadata("device", deviceDataCollector.getDeviceMetadata());

        // add additional info that belongs in metadata
        // generate new object each time, as this can be mutated by end-users
        event.setApp(appDataCollector.generateAppWithState());
        event.addMetadata("app", appDataCollector.getAppDataMetadata());

        // Attach breadcrumbState to the event
        event.setBreadcrumbs(breadcrumbState.copy());

        // Attach user info to the event
        User user = userState.get().getUser();
        event.setUser(user.getId(), user.getEmail(), user.getName());

        // Attach context to the event
        event.setContext(contextState.getContext());

        event.setInternalMetrics(internalMetrics);

        notifyInternal(event, onError);
    }

    void notifyInternal(@NonNull Event event,
                        @Nullable OnErrorCallback onError) {
        // set the redacted keys on the event as this
        // will not have been set for RN/Unity events
        Collection<Pattern> redactedKeys = metadataState.getMetadata().getRedactedKeys();
        event.setRedactedKeys(redactedKeys);

        // get session for event
        Session currentSession = sessionTracker.getCurrentSession();

        if (currentSession != null
                && (immutableConfig.getAutoTrackSessions() || !currentSession.isAutoCaptured())) {
            event.setSession(currentSession);
        }

        // Run on error tasks, don't notify if any return false
        if (!callbackState.runOnErrorTasks(event, logger)
                || (onError != null
                && !onError.onError(event))) {
            logger.d("Skipping notification - onError task returned false");
            return;
        }

        // leave an error breadcrumb of this event - for the next event
        leaveErrorBreadcrumb(event);

        deliveryDelegate.deliver(event);
    }

    /**
     * Returns the current buffer of breadcrumbs that will be sent with captured events. This
     * ordered list represents the most recent breadcrumbs to be captured up to the limit
     * set in {@link Configuration#getMaxBreadcrumbs()}.
     *
     * The returned collection is readonly and mutating the list will cause no effect on the
     * Client's state. If you wish to alter the breadcrumbs collected by the Client then you should
     * use {@link Configuration#setEnabledBreadcrumbTypes(Set)} and
     * {@link Configuration#addOnBreadcrumb(OnBreadcrumbCallback)} instead.
     *
     * @return a list of collected breadcrumbs
     */
    @NonNull
    public List<Breadcrumb> getBreadcrumbs() {
        return breadcrumbState.copy();
    }

    @NonNull
    AppDataCollector getAppDataCollector() {
        return appDataCollector;
    }

    @NonNull
    DeviceDataCollector getDeviceDataCollector() {
        return deviceDataCollector;
    }

    /**
     * Adds a map of multiple metadata key-value pairs to the specified section.
     */
    @Override
    public void addMetadata(@NonNull String section, @NonNull Map<String, ?> value) {
        if (section != null && value != null) {
            metadataState.addMetadata(section, value);
        } else {
            logNull("addMetadata");
        }
    }

    /**
     * Adds the specified key and value in the specified section. The value can be of
     * any primitive type or a collection such as a map, set or array.
     */
    @Override
    public void addMetadata(@NonNull String section, @NonNull String key, @Nullable Object value) {
        if (section != null && key != null) {
            metadataState.addMetadata(section, key, value);

        } else {
            logNull("addMetadata");
        }
    }

    /**
     * Removes all the data from the specified section.
     */
    @Override
    public void clearMetadata(@NonNull String section) {
        if (section != null) {
            metadataState.clearMetadata(section);
        } else {
            logNull("clearMetadata");
        }
    }

    /**
     * Removes data with the specified key from the specified section.
     */
    @Override
    public void clearMetadata(@NonNull String section, @NonNull String key) {
        if (section != null && key != null) {
            metadataState.clearMetadata(section, key);
        } else {
            logNull("clearMetadata");
        }
    }

    /**
     * Returns a map of data in the specified section.
     */
    @Nullable
    @Override
    public Map<String, Object> getMetadata(@NonNull String section) {
        if (section != null) {
            return metadataState.getMetadata(section);
        } else {
            logNull("getMetadata");
            return null;
        }
    }

    /**
     * Returns the value of the specified key in the specified section.
     */
    @Override
    @Nullable
    public Object getMetadata(@NonNull String section, @NonNull String key) {
        if (section != null && key != null) {
            return metadataState.getMetadata(section, key);
        } else {
            logNull("getMetadata");
            return null;
        }
    }

    // cast map to retain original signature until next major version bump, as this
    // method signature is used by Unity/React native
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    Map<String, Object> getMetadata() {
        return (Map) metadataState.getMetadata().toMap();
    }

    /**
     * Leave a "breadcrumb" log message, representing an action that occurred
     * in your app, to aid with debugging.
     *
     * @param message the log message to leave
     */
    public void leaveBreadcrumb(@NonNull String message) {
        if (message != null) {
            breadcrumbState.add(new Breadcrumb(message, logger));
        } else {
            logNull("leaveBreadcrumb");
        }
    }

    /**
     * Leave a "breadcrumb" log message representing an action or event which
     * occurred in your app, to aid with debugging
     *
     * @param message  A short label
     * @param metadata Additional diagnostic information about the app environment
     * @param type     A category for the breadcrumb
     */
    public void leaveBreadcrumb(@NonNull String message,
                                @NonNull Map<String, Object> metadata,
                                @NonNull BreadcrumbType type) {
        if (message != null && type != null && metadata != null) {
            breadcrumbState.add(new Breadcrumb(message, type, metadata, new Date(), logger));
        } else {
            logNull("leaveBreadcrumb");
        }
    }

    /**
     * Intended for internal use only - leaves a breadcrumb if the type is enabled for automatic
     * breadcrumbs.
     *
     * @param message  A short label
     * @param type     A category for the breadcrumb
     * @param metadata Additional diagnostic information about the app environment
     */
    void leaveAutoBreadcrumb(@NonNull String message,
                             @NonNull BreadcrumbType type,
                             @NonNull Map<String, Object> metadata) {
        if (!immutableConfig.shouldDiscardBreadcrumb(type)) {
            breadcrumbState.add(new Breadcrumb(message, type, metadata, new Date(), logger));
        }
    }

    private void leaveErrorBreadcrumb(@NonNull Event event) {
        // Add a breadcrumb for this event occurring
        List<Error> errors = event.getErrors();

        if (errors.size() > 0) {
            String errorClass = errors.get(0).getErrorClass();
            String message = errors.get(0).getErrorMessage();

            Map<String, Object> data = new HashMap<>();
            data.put("errorClass", errorClass);
            data.put("message", message);
            data.put("unhandled", String.valueOf(event.isUnhandled()));
            data.put("severity", event.getSeverity().toString());
            breadcrumbState.add(new Breadcrumb(errorClass,
                    BreadcrumbType.ERROR, data, new Date(), logger));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFeatureFlag(@NonNull String name) {
        if (name != null) {
            featureFlagState.addFeatureFlag(name);
        } else {
            logNull("addFeatureFlag");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFeatureFlag(@NonNull String name, @Nullable String variant) {
        if (name != null) {
            featureFlagState.addFeatureFlag(name, variant);
        } else {
            logNull("addFeatureFlag");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFeatureFlags(@NonNull Iterable<FeatureFlag> featureFlags) {
        if (featureFlags != null) {
            featureFlagState.addFeatureFlags(featureFlags);
        } else {
            logNull("addFeatureFlags");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearFeatureFlag(@NonNull String name) {
        if (name != null) {
            featureFlagState.clearFeatureFlag(name);
        } else {
            logNull("clearFeatureFlag");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearFeatureFlags() {
        featureFlagState.clearFeatureFlags();
    }

    /**
     * Retrieves information about the last launch of the application, if it has been run before.
     *
     * For example, this allows checking whether the app crashed on its last launch, which could
     * be used to perform conditional behaviour to recover from crashes, such as clearing the
     * app data cache.
     */
    @Nullable
    public LastRunInfo getLastRunInfo() {
        return lastRunInfo;
    }

    /**
     * Informs Bugsnag that the application has finished launching. Once this has been called
     * {@link AppWithState#isLaunching()} will always be false in any new error reports,
     * and synchronous delivery will not be attempted on the next launch for any fatal crashes.
     *
     * By default this method will be called after Bugsnag is initialized when
     * {@link Configuration#getLaunchDurationMillis()} has elapsed. Invoking this method manually
     * has precedence over the value supplied via the launchDurationMillis configuration option.
     */
    public void markLaunchCompleted() {
        launchCrashTracker.markLaunchCompleted();
    }

    SessionTracker getSessionTracker() {
        return sessionTracker;
    }

    @NonNull
    EventStore getEventStore() {
        return eventStore;
    }

    /**
     * Finalize by removing the receiver
     *
     * @throws Throwable if something goes wrong
     */
    @SuppressWarnings("checkstyle:NoFinalizer")
    protected void finalize() throws Throwable {
        if (systemBroadcastReceiver != null) {
            try {
                ContextExtensionsKt.unregisterReceiverSafe(appContext,
                        systemBroadcastReceiver, logger);
            } catch (IllegalArgumentException exception) {
                logger.w("Receiver not registered");
            }
        }
        super.finalize();
    }

    ImmutableConfig getConfig() {
        return immutableConfig;
    }

    void setBinaryArch(String binaryArch) {
        getAppDataCollector().setBinaryArch(binaryArch);
    }

    Context getAppContext() {
        return appContext;
    }

    /**
     * Intended for internal use only - sets the code bundle id for React Native
     */
    @Nullable
    String getCodeBundleId() {
        return appDataCollector.getCodeBundleId();
    }

    /**
     * Intended for internal use only - sets the code bundle id for React Native
     */
    void setCodeBundleId(@Nullable String codeBundleId) {
        appDataCollector.setCodeBundleId(codeBundleId);
    }

    void addRuntimeVersionInfo(@NonNull String key, @NonNull String value) {
        deviceDataCollector.addRuntimeVersionInfo(key, value);
    }

    @VisibleForTesting
    void close() {
        connectivity.unregisterForNetworkChanges();
        bgTaskService.shutdown();
    }

    Logger getLogger() {
        return logger;
    }

    /**
     * Retrieves an instantiated plugin of the given type, or null if none has been created
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    Plugin getPlugin(@NonNull Class clz) {
        return pluginClient.findPlugin(clz);
    }

    Notifier getNotifier() {
        return notifier;
    }

    MetadataState getMetadataState() {
        return metadataState;
    }

    FeatureFlagState getFeatureFlagState() {
        return featureFlagState;
    }

    ContextState getContextState() {
        return contextState;
    }

    void setAutoNotify(boolean autoNotify) {
        pluginClient.setAutoNotify(this, autoNotify);

        if (autoNotify) {
            exceptionHandler.install();
        } else {
            exceptionHandler.uninstall();
        }
    }

    void setAutoDetectAnrs(boolean autoDetectAnrs) {
        pluginClient.setAutoDetectAnrs(this, autoDetectAnrs);
    }

    void addOnSend(OnSendCallback callback) {
        callbackState.addPreOnSend(callback);
    }
}
