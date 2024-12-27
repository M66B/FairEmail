package com.bugsnag.android

import com.bugsnag.android.internal.DateUtils
import java.io.OutputStream
import java.util.Date

private const val HEADER_API_PAYLOAD_VERSION = "Bugsnag-Payload-Version"
private const val HEADER_BUGSNAG_SENT_AT = "Bugsnag-Sent-At"
private const val HEADER_BUGSNAG_STACKTRACE_TYPES = "Bugsnag-Stacktrace-Types"
private const val HEADER_CONTENT_TYPE = "Content-Type"
internal const val HEADER_BUGSNAG_INTEGRITY = "Bugsnag-Integrity"
internal const val HEADER_API_KEY = "Bugsnag-Api-Key"
internal const val HEADER_INTERNAL_ERROR = "Bugsnag-Internal-Error"

/**
 * Supplies the headers which must be used in any request sent to the Error Reporting API.
 *
 * @return the HTTP headers
 */
internal fun errorApiHeaders(payload: EventPayload): Map<String, String?> {
    val mutableHeaders = mutableMapOf(
        HEADER_API_PAYLOAD_VERSION to "4.0",
        HEADER_API_KEY to (payload.apiKey ?: ""),
        HEADER_BUGSNAG_SENT_AT to DateUtils.toIso8601(Date()),
        HEADER_CONTENT_TYPE to "application/json"
    )
    val errorTypes = payload.getErrorTypes()
    if (errorTypes.isNotEmpty()) {
        mutableHeaders[HEADER_BUGSNAG_STACKTRACE_TYPES] = serializeErrorTypeHeader(errorTypes)
    }
    return mutableHeaders.toMap()
}

/**
 * Serializes the error types to a comma delimited string
 */
internal fun serializeErrorTypeHeader(errorTypes: Set<ErrorType>): String {
    return when {
        errorTypes.isEmpty() -> ""
        else ->
            errorTypes
                .map(ErrorType::desc)
                .reduce { accumulator, str ->
                    "$accumulator,$str"
                }
    }
}

/**
 * Supplies the headers which must be used in any request sent to the Session Tracking API.
 *
 * @return the HTTP headers
 */
internal fun sessionApiHeaders(apiKey: String): Map<String, String?> = mapOf(
    HEADER_API_PAYLOAD_VERSION to "1.0",
    HEADER_API_KEY to apiKey,
    HEADER_CONTENT_TYPE to "application/json",
    HEADER_BUGSNAG_SENT_AT to DateUtils.toIso8601(Date())
)

internal class NullOutputStream : OutputStream() {
    override fun write(b: Int) = Unit
}
