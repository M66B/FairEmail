package com.bugsnag.android.internal.dag

import com.bugsnag.android.getActivityManager
import com.bugsnag.android.getStorageManager

/**
 * A dependency module which provides a reference to Android system services.
 */
internal class SystemServiceModule(
    contextModule: ContextModule
) : DependencyModule() {

    val storageManager = contextModule.ctx.getStorageManager()
    val activityManager = contextModule.ctx.getActivityManager()
}
