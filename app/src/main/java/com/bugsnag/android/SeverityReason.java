package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final class SeverityReason implements JsonStream.Streamable {

    @StringDef({REASON_UNHANDLED_EXCEPTION, REASON_STRICT_MODE, REASON_HANDLED_EXCEPTION,
        REASON_USER_SPECIFIED, REASON_CALLBACK_SPECIFIED, REASON_PROMISE_REJECTION,
        REASON_LOG, REASON_SIGNAL, REASON_ANR})
    @Retention(RetentionPolicy.SOURCE)
    @interface SeverityReasonType {
    }

    static final String REASON_UNHANDLED_EXCEPTION = "unhandledException";
    static final String REASON_STRICT_MODE = "strictMode";
    static final String REASON_HANDLED_EXCEPTION = "handledException";
    static final String REASON_USER_SPECIFIED = "userSpecifiedSeverity";
    static final String REASON_CALLBACK_SPECIFIED = "userCallbackSetSeverity";
    static final String REASON_PROMISE_REJECTION = "unhandledPromiseRejection";
    static final String REASON_SIGNAL = "signal";
    static final String REASON_LOG = "log";
    static final String REASON_ANR = "anrError";

    @SeverityReasonType
    private final String severityReasonType;

    @Nullable
    private final String attributeValue;

    private final Severity defaultSeverity;
    private Severity currentSeverity;
    private boolean unhandled;
    final boolean originalUnhandled;

    static SeverityReason newInstance(@SeverityReasonType String severityReasonType) {
        return newInstance(severityReasonType, null, null);
    }

    static SeverityReason newInstance(@SeverityReasonType String severityReasonType,
                                      @Nullable Severity severity,
                                      @Nullable String attrVal) {

        if (severityReasonType.equals(REASON_STRICT_MODE) && Intrinsics.isEmpty(attrVal)) {
            throw new IllegalArgumentException("No reason supplied for strictmode");
        }
        if (!(severityReasonType.equals(REASON_STRICT_MODE)
            || severityReasonType.equals(REASON_LOG)) && !Intrinsics.isEmpty(attrVal)) {
            throw new IllegalArgumentException("attributeValue should not be supplied");
        }

        switch (severityReasonType) {
            case REASON_UNHANDLED_EXCEPTION:
            case REASON_PROMISE_REJECTION:
            case REASON_ANR:
                return new SeverityReason(severityReasonType, Severity.ERROR, true, null);
            case REASON_STRICT_MODE:
                return new SeverityReason(severityReasonType, Severity.WARNING, true, attrVal);
            case REASON_HANDLED_EXCEPTION:
                return new SeverityReason(severityReasonType, Severity.WARNING, false, null);
            case REASON_USER_SPECIFIED:
            case REASON_CALLBACK_SPECIFIED:
                return new SeverityReason(severityReasonType, severity, false, null);
            case REASON_LOG:
                return new SeverityReason(severityReasonType, severity, false, attrVal);
            default:
                String msg = String.format("Invalid argument '%s' for severityReason",
                    severityReasonType);
                throw new IllegalArgumentException(msg);
        }
    }

    SeverityReason(String severityReasonType, Severity currentSeverity, boolean unhandled,
                   @Nullable String attributeValue) {
        this(severityReasonType, currentSeverity, unhandled, unhandled, attributeValue);
    }

    SeverityReason(String severityReasonType, Severity currentSeverity, boolean unhandled,
                   boolean originalUnhandled, @Nullable String attributeValue) {
        this.severityReasonType = severityReasonType;
        this.unhandled = unhandled;
        this.originalUnhandled = originalUnhandled;
        this.defaultSeverity = currentSeverity;
        this.currentSeverity = currentSeverity;
        this.attributeValue = attributeValue;
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

        if (attributeValue != null) {
            String attributeKey = null;
            switch (severityReasonType) {
                case REASON_LOG:
                    attributeKey = "level";
                    break;
                case REASON_STRICT_MODE:
                    attributeKey = "violationType";
                    break;
                default:
                    break;
            }
            if (attributeKey != null) {
                writer.name("attributes").beginObject()
                    .name(attributeKey).value(attributeValue)
                    .endObject();
            }
        }
        writer.endObject();
    }

}
