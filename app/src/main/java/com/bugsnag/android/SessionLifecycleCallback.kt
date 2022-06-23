package com.bugsnag.android

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle

internal class SessionLifecycleCallback(
    private val sessionTracker: SessionTracker
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityStarted(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            sessionTracker.onActivityStarted(activity.javaClass.simpleName)
        }
    }

    override fun onActivityPostStarted(activity: Activity) {
        sessionTracker.onActivityStarted(activity.javaClass.simpleName)
    }

    override fun onActivityStopped(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            sessionTracker.onActivityStopped(activity.javaClass.simpleName)
        }
    }

    override fun onActivityPostStopped(activity: Activity) {
        sessionTracker.onActivityStopped(activity.javaClass.simpleName)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
