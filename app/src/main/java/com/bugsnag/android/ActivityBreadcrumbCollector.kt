package com.bugsnag.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.util.WeakHashMap

internal class ActivityBreadcrumbCollector(
    private val cb: (message: String, method: Map<String, Any>) -> Unit
) : Application.ActivityLifecycleCallbacks {

    private val prevState = WeakHashMap<Activity, String>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        leaveBreadcrumb(
            activity,
            "onCreate()",
            mutableMapOf<String, Any>().apply {
                set("hasBundle", savedInstanceState != null)
                setActivityIntentMetadata(activity.intent)
            }
        )
    }

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
        metadata: MutableMap<String, Any> = mutableMapOf()
    ) {
        val previousVal = prevState[activity]

        if (previousVal != null) {
            metadata["previous"] = previousVal
        }

        val activityName = getActivityName(activity)
        cb("$activityName#$lifecycleCallback", metadata)
        prevState[activity] = lifecycleCallback
    }

    private fun MutableMap<String, Any>.setActivityIntentMetadata(intent: Intent?) {
        if (intent == null) return

        intent.action?.let { set("action", it) }
        intent.categories?.let { set("categories", it.joinToString(", ")) }
        intent.type?.let { set("type", it) }

        if (intent.flags != 0) {
            @Suppress("MagicNumber") // hex radix
            set("flags", "0x${intent.flags.toString(16)}")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.identifier?.let { set("id", it) }
        }

        set("hasData", intent.data != null)

        try {
            set("hasExtras", intent.extras?.keySet()?.joinToString(", ") ?: false)
        } catch (re: Exception) {
            // deliberately ignore
        }
    }
}
