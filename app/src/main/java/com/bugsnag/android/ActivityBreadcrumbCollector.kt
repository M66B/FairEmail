package com.bugsnag.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.WeakHashMap

internal class ActivityBreadcrumbCollector(
    private val cb: (message: String, method: Map<String, Any>) -> Unit
) : Application.ActivityLifecycleCallbacks {

    private val prevState = WeakHashMap<Activity, String>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
        leaveBreadcrumb(activity, "onCreate()", savedInstanceState != null)

    override fun onActivityStarted(activity: Activity) =
        leaveBreadcrumb(activity, "onStart()")

    override fun onActivityResumed(activity: Activity) =
        leaveBreadcrumb(activity, "onResume()")

    override fun onActivityPaused(activity: Activity) =
        leaveBreadcrumb(activity, "onPause()")

    override fun onActivityStopped(activity: Activity) =
        leaveBreadcrumb(activity, "onStop()")

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) =
        leaveBreadcrumb(activity, "onSaveInstanceState()")

    override fun onActivityDestroyed(activity: Activity) {
        leaveBreadcrumb(activity, "onDestroy()")
        prevState.remove(activity)
    }

    private fun getActivityName(activity: Activity) = activity.javaClass.simpleName

    private fun leaveBreadcrumb(
        activity: Activity,
        lifecycleCallback: String,
        hasBundle: Boolean? = null
    ) {
        val metadata = mutableMapOf<String, Any>()
        if (hasBundle != null) {
            metadata["hasBundle"] = hasBundle
        }
        val previousVal = prevState[activity]

        if (previousVal != null) {
            metadata["previous"] = previousVal
        }

        val activityName = getActivityName(activity)
        cb("$activityName#$lifecycleCallback", metadata)
        prevState[activity] = lifecycleCallback
    }
}
