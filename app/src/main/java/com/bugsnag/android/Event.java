package com.bugsnag.android;

import com.bugsnag.android.internal.ImmutableConfig;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An Event object represents a Throwable captured by Bugsnag and is available as a parameter on
 * an {@link OnErrorCallback}, where individual properties can be mutated before an error report is
 * sent to Bugsnag's API.
 */
@SuppressWarnings("ConstantConditions")
public class Event implements JsonStream.Streamable, MetadataAware, UserAware, FeatureFlagAware {

    private final EventInternal impl;
    private final Logger logger;

    Event(@Nullable Throwable originalError,
          @NonNull ImmutableConfig config,
          @NonNull SeverityReason severityReason,
          @NonNull Logger logger) {
        this(originalError, config, severityReason, new Metadata(), new FeatureFlags(), logger);
    }

    Event(@Nullable Throwable originalError,
          @NonNull ImmutableConfig config,
          @NonNull SeverityReason severityReason,
          @NonNull Metadata metadata,
          @NonNull FeatureFlags featureFlags,
          @NonNull Logger logger) {
        this(new EventInternal(originalError, config, severityReason, metadata, featureFlags),
                logger);
    }

    Event(@NonNull EventInternal impl, @NonNull Logger logger) {
        this.impl = impl;
        this.logger = logger;
    }

    private void logNull(String property) {
        logger.e("Invalid null value supplied to config." + property + ", ignoring");
    }

    /**
     * The Throwable object that caused the event in your application.
     *
     * Manipulating this field does not affect the error information reported to the
     * Bugsnag dashboard. Use {@link Event#getErrors()} to access and amend the representation of
     * the error that will be sent.
     */
    @Nullable
    public Throwable getOriginalError() {
        return impl.getOriginalError();
    }

    /**
     * Information extracted from the {@link Throwable} that caused the event can be found in this
     * field. The list contains at least one {@link Error} that represents the thrown object
     * with subsequent elements in the list populated from {@link Throwable#getCause()}.
     *
     * A reference to the actual {@link Throwable} object that caused the event is available
     * through {@link Event#getOriginalError()} ()}.
     */
    @NonNull
    public List<Error> getErrors() {
        return impl.getErrors();
    }

    /**
     * If thread state is being captured along with the event, this field will contain a
     * list of {@link Thread} objects.
     */
    @NonNull
    public List<Thread> getThreads() {
        return impl.getThreads();
    }

    /**
     * A list of breadcrumbs leading up to the event. These values can be accessed and amended
     * if necessary. See {@link Breadcrumb} for details of the data available.
     */
    @NonNull
    public List<Breadcrumb> getBreadcrumbs() {
        return impl.getBreadcrumbs();
    }

    /**
     * Information set by the notifier about your app can be found in this field. These values
     * can be accessed and amended if necessary.
     */
    @NonNull
    public AppWithState getApp() {
        return impl.getApp();
    }

    /**
     * Information set by the notifier about your device can be found in this field. These values
     * can be accessed and amended if necessary.
     */
    @NonNull
    public DeviceWithState getDevice() {
        return impl.getDevice();
    }

    /**
     * The API key used for events sent to Bugsnag. Even though the API key is set when Bugsnag
     * is initialized, you may choose to send certain events to a different Bugsnag project.
     */
    public void setApiKey(@NonNull String apiKey) {
        if (apiKey != null) {
            impl.setApiKey(apiKey);
        } else {
            logNull("apiKey");
        }
    }

    /**
     * The API key used for events sent to Bugsnag. Even though the API key is set when Bugsnag
     * is initialized, you may choose to send certain events to a different Bugsnag project.
     */
    @NonNull
    public String getApiKey() {
        return impl.getApiKey();
    }

    /**
     * The severity of the event. By default, unhandled exceptions will be {@link Severity#ERROR}
     * and handled exceptions sent with {@link Bugsnag#notify} {@link Severity#WARNING}.
     */
    public void setSeverity(@NonNull Severity severity) {
        if (severity != null) {
            impl.setSeverity(severity);
        } else {
            logNull("severity");
        }
    }

    /**
     * The severity of the event. By default, unhandled exceptions will be {@link Severity#ERROR}
     * and handled exceptions sent with {@link Bugsnag#notify} {@link Severity#WARNING}.
     */
    @NonNull
    public Severity getSeverity() {
        return impl.getSeverity();
    }

    /**
     * Set the grouping hash of the event to override the default grouping on the dashboard.
     * All events with the same grouping hash will be grouped together into one error. This is an
     * advanced usage of the library and mis-using it will cause your events not to group properly
     * in your dashboard.
     *
     * As the name implies, this option accepts a hash of sorts.
     */
    public void setGroupingHash(@Nullable String groupingHash) {
        impl.setGroupingHash(groupingHash);
    }

    /**
     * Set the grouping hash of the event to override the default grouping on the dashboard.
     * All events with the same grouping hash will be grouped together into one error. This is an
     * advanced usage of the library and mis-using it will cause your events not to group properly
     * in your dashboard.
     *
     * As the name implies, this option accepts a hash of sorts.
     */
    @Nullable
    public String getGroupingHash() {
        return impl.getGroupingHash();
    }

    /**
     * Sets the context of the error. The context is a summary what what was occurring in the
     * application at the time of the crash, if available, such as the visible activity.
     */
    public void setContext(@Nullable String context) {
        impl.setContext(context);
    }

    /**
     * Returns the context of the error. The context is a summary what what was occurring in the
     * application at the time of the crash, if available, such as the visible activity.
     */
    @Nullable
    public String getContext() {
        return impl.getContext();
    }

    /**
     * Sets the user associated with the event.
     */
    @Override
    public void setUser(@Nullable String id, @Nullable String email, @Nullable String name) {
        impl.setUser(id, email, name);
    }

    /**
     * Returns the currently set User information.
     */
    @Override
    @NonNull
    public User getUser() {
        return impl.getUser();
    }

    /**
     * Adds a map of multiple metadata key-value pairs to the specified section.
     */
    @Override
    public void addMetadata(@NonNull String section, @NonNull Map<String, ?> value) {
        if (section != null && value != null) {
            impl.addMetadata(section, value);
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
            impl.addMetadata(section, key, value);
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
            impl.clearMetadata(section);
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
            impl.clearMetadata(section, key);
        } else {
            logNull("clearMetadata");
        }
    }

    /**
     * Returns a map of data in the specified section.
     */
    @Override
    @Nullable
    public Map<String, Object> getMetadata(@NonNull String section) {
        if (section != null) {
            return impl.getMetadata(section);
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
            return impl.getMetadata(section, key);
        } else {
            logNull("getMetadata");
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFeatureFlag(@NonNull String name) {
        if (name != null) {
            impl.addFeatureFlag(name);
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
            impl.addFeatureFlag(name, variant);
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
            impl.addFeatureFlags(featureFlags);
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
            impl.clearFeatureFlag(name);
        } else {
            logNull("clearFeatureFlag");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearFeatureFlags() {
        impl.clearFeatureFlags();
    }

    @Override
    public void toStream(@NonNull JsonStream stream) throws IOException {
        impl.toStream(stream);
    }

    /**
     * Whether the event was a crash (i.e. unhandled) or handled error in which the system
     * continued running.
     *
     * Unhandled errors count towards your stability score. If you don't want certain errors
     * to count towards your stability score, you can alter this property through an
     * {@link OnErrorCallback}.
     */
    public boolean isUnhandled() {
        return impl.getUnhandled();
    }

    /**
     * Whether the event was a crash (i.e. unhandled) or handled error in which the system
     * continued running.
     *
     * Unhandled errors count towards your stability score. If you don't want certain errors
     * to count towards your stability score, you can alter this property through an
     * {@link OnErrorCallback}.
     */
    public void setUnhandled(boolean unhandled) {
        impl.setUnhandled(unhandled);
    }

    protected boolean shouldDiscardClass() {
        return impl.shouldDiscardClass();
    }

    protected void updateSeverityInternal(@NonNull Severity severity) {
        impl.updateSeverityInternal(severity);
    }

    protected void updateSeverityReason(@NonNull @SeverityReason.SeverityReasonType String reason) {
        impl.updateSeverityReason(reason);
    }

    void setApp(@NonNull AppWithState app) {
        impl.setApp(app);
    }

    void setDevice(@NonNull DeviceWithState device) {
        impl.setDevice(device);
    }

    void setBreadcrumbs(@NonNull List<Breadcrumb> breadcrumbs) {
        impl.setBreadcrumbs(breadcrumbs);
    }

    @Nullable
    Session getSession() {
        return impl.session;
    }

    void setSession(@Nullable Session session) {
        impl.session = session;
    }

    EventInternal getImpl() {
        return impl;
    }

    void setRedactedKeys(Collection<String> redactedKeys) {
        impl.setRedactedKeys(redactedKeys);
    }
}
