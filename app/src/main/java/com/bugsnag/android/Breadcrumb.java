package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class Breadcrumb implements JsonStream.Streamable {

    private final BreadcrumbInternal impl;
    private final Logger logger;

    Breadcrumb(@NonNull String message, @NonNull Logger logger) {
        this.impl = new BreadcrumbInternal(message);
        this.logger = logger;
    }

    Breadcrumb(@NonNull String message,
               @NonNull BreadcrumbType type,
               @Nullable Map<String, Object> metadata,
               @NonNull Date timestamp,
               @NonNull Logger logger) {
        this.impl = new BreadcrumbInternal(message, type, metadata, timestamp);
        this.logger = logger;
    }

    private void logNull(String property) {
        logger.e("Invalid null value supplied to breadcrumb." + property + ", ignoring");
    }

    /**
     * Sets the description of the breadcrumb
     */
    public void setMessage(@NonNull String message) {
        if (message != null) {
            impl.setMessage(message);
        } else {
            logNull("message");
        }
    }

    /**
     * Gets the description of the breadcrumb
     */
    @NonNull
    public String getMessage() {
        return impl.getMessage();
    }

    /**
     * Sets the type of breadcrumb left - one of those enabled in
     * {@link Configuration#getEnabledBreadcrumbTypes()}
     */
    public void setType(@NonNull BreadcrumbType type) {
        if (type != null) {
            impl.setType(type);
        } else {
            logNull("type");
        }
    }

    /**
     * Gets the type of breadcrumb left - one of those enabled in
     * {@link Configuration#getEnabledBreadcrumbTypes()}
     */
    @NonNull
    public BreadcrumbType getType() {
        return impl.getType();
    }

    /**
     * Sets diagnostic data relating to the breadcrumb
     */
    public void setMetadata(@Nullable Map<String, Object> metadata) {
        impl.setMetadata(metadata);
    }

    /**
     * Gets diagnostic data relating to the breadcrumb
     */
    @Nullable
    public Map<String, Object> getMetadata() {
        return impl.getMetadata();
    }

    /**
     * The timestamp that the breadcrumb was left
     */
    @NonNull
    public Date getTimestamp() {
        return impl.getTimestamp();
    }

    @Override
    public void toStream(@NonNull JsonStream stream) throws IOException {
        impl.toStream(stream);
    }
}
