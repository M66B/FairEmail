package com.bugsnag.android.internal.dag

import com.bugsnag.android.getActivityManager
import com.bugsnag.android.getStorageManager
import com.bugsnag.android.internal.BackgroundTaskService

/**
 * A dependency module which provides a reference to Android system services.
 */
internal class SystemServiceModule(
    contextModule: ContextModule,
    bgTaskService: BackgroundTaskService
) : BackgroundDependencyModule(bgTaskService) {

    val storageManager = contextModule.ctx.getStorageManager()
    val activityManager = contextModule.ctx.getActivityManager()
}
