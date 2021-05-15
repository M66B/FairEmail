package com.bugsnag.android

internal interface CallbackAware {
    fun addOnError(onError: OnErrorCallback)
    fun removeOnError(onError: OnErrorCallback)
    fun addOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback)
    fun removeOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback)
    fun addOnSession(onSession: OnSessionCallback)
    fun removeOnSession(onSession: OnSessionCallback)
}
