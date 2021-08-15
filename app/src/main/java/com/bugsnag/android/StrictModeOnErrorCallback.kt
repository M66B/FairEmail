package com.bugsnag.android

internal class StrictModeOnErrorCallback(private val errMsg: String) : OnErrorCallback {
    override fun onError(event: Event): Boolean {
        event.updateSeverityInternal(Severity.INFO)
        event.updateSeverityReason(SeverityReason.REASON_STRICT_MODE)
        val error = event.errors.firstOrNull()
        error?.errorMessage = errMsg
        return true
    }
}
