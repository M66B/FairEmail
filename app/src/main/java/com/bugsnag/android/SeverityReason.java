package com.bugsnag.android;

import static com.bugsnag.android.Severity.ERROR;
import static com.bugsnag.android.Severity.WARNING;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final class SeverityReason implements JsonStream.Streamable {

    @StringDef({REASON_UNHANDLED_EXCEPTION, REASON_STRICT_MODE, REASON_HANDLED_EXCEPTION,
            REASON_HANDLED_ERROR, REASON_USER_SPECIFIED, REASON_CALLBACK_SPECIFIED,
            REASON_PROMISE_REJECTION, REASON_LOG, REASON_SIGNAL, REASON_ANR })
    @Retention(RetentionPolicy.SOURCE)
    @interface SeverityReasonType {
    }

    static final String REASON_UNHANDLED_EXCEPTION = "unhandledException";
    static final String REASON_STRICT_MODE = "strictMode";
    static final String REASON_HANDLED_EXCEPTION = "handledException";
    static final String REASON_HANDLED_ERROR = "handledError";
    static final String REASON_USER_SPECIFIED = "userSpecifiedSeverity";
    static final String REASON_CALLBACK_SPECIFIED = "userCallbackSetSeverity";
    static final String REASON_PROMISE_REJECTION = "unhandledPromiseRejection";
    static final String REASON_SIGNAL = "signal";
    static final String REASON_LOG = "log";
    static final String REASON_ANR = "anrError";

    @SeverityReasonType
    private final String severityReasonType;

    @Nullable
    private final String attributeKey;

    @Nullable
    private final String attributeValue;

    private final Severity defaultSeverity;
    private Severity currentSeverity;
    private boolean unhandled;
    final boolean originalUnhandled;

    static SeverityReason newInstance(@SeverityReasonType String severityReasonType) {
        return newInstance(severityReasonType, null, null);
    }

    static SeverityReason newInstance(@SeverityReasonType String reason,
                                      @Nullable Severity severity,
                                      @Nullable String attrVal) {

        if (reason.equals(REASON_STRICT_MODE) && Intrinsics.isEmpty(attrVal)) {
            throw new IllegalArgumentException("No reason supplied for strictmode");
        }
        if (!(reason.equals(REASON_STRICT_MODE)
                || reason.equals(REASON_LOG)) && !Intrinsics.isEmpty(attrVal)) {
            throw new IllegalArgumentException("attributeValue should not be supplied");
        }

        switch (reason) {
            case REASON_UNHANDLED_EXCEPTION:
            case REASON_PROMISE_REJECTION:
            case REASON_ANR:
                return new SeverityReason(reason, ERROR, true, true, null, null);
            case REASON_STRICT_MODE:
                return new SeverityReason(reason, WARNING, true, true, attrVal, "violationType");
            case REASON_HANDLED_ERROR:
            case REASON_HANDLED_EXCEPTION:
                return new SeverityReason(reason, WARNING, false, false, null, null);
            case REASON_USER_SPECIFIED:
            case REASON_CALLBACK_SPECIFIED:
                return new SeverityReason(reason, severity, false, false, null, null);
            case REASON_LOG:
                return new SeverityReason(reason, severity, false, false, attrVal, "level");
            default:
                String msg = "Invalid argument for severityReason: '" + reason + '\'';
                throw new IllegalArgumentException(msg);
        }
    }

    SeverityReason(String severityReasonType,
                   Severity currentSeverity,
                   boolean unhandled,
                   boolean originalUnhandled,
                   @Nullable String attributeValue,
                   @Nullable String attributeKey) {
        this.severityReasonType = severityReasonType;
        this.unhandled = unhandled;
        this.originalUnhandled = originalUnhandled;
        this.defaultSeverity = currentSeverity;
        this.currentSeverity = currentSeverity;
        this.attributeValue = attributeValue;
        this.attributeKey = attributeKey;
    }

    String calculateSeverityReasonType() {
        return defaultSeverity == currentSeverity ? severityReasonType : REASON_CALLBACK_SPECIFIED;
    }

    Severity getCurrentSeverity() {
        return currentSeverity;
    }

    boolean getUnhandled() {
        return unhandled;
    }

    void setUnhandled(boolean unhandled) {
        this.unhandled = unhandled;
    }

    boolean getUnhandledOverridden() {
        return unhandled != originalUnhandled;
    }

    boolean isOriginalUnhandled() {
        return originalUnhandled;
    }

    @Nullable
    String getAttributeValue() {
        return attributeValue;
    }

    String getAttributeKey() {
        return attributeKey;
    }

    void setCurrentSeverity(Severity severity) {
        this.currentSeverity = severity;
    }

    String getSeverityReasonType() {
        return severityReasonType;
    }

    @Override
    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginObject()
                .name("type").value(calculateSeverityReasonType())
                .name("unhandledOverridden").value(getUnhandledOverridden());

        if (attributeKey != null && attributeValue != null) {
            writer.name("attributes").beginObject()
                    .name(attributeKey).value(attributeValue)
                    .endObject();
        }
        writer.endObject();
    }
}