package com.bugsnag.android

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class SessionLifecycleCallback(
    private val sessionTracker: SessionTracker
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityStarted(activity: Activity) =
        sessionTracker.onActivityStarted(activity.javaClass.simpleName)

    override fun onActivityStopped(activity: Activity) =
        sessionTracker.onActivityStopped(activity.javaClass.simpleName)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
