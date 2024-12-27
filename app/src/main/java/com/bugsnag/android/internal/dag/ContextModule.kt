package com.bugsnag.android.internal.dag

import android.content.Context
import com.bugsnag.android.internal.BackgroundTaskService

/**
 * A dependency module which accesses the application context object, falling back to the supplied
 * context if it is the base context.
 */
internal class ContextModule(
    appContext: Context,
    bgTaskService: BackgroundTaskService
) : BackgroundDependencyModule(bgTaskService) {

    val ctx: Context = when (appContext.applicationContext) {
        null -> appContext
        else -> appContext.applicationContext
    }
}
