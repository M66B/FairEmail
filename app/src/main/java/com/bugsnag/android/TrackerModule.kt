package com.bugsnag.android

import com.bugsnag.android.internal.dag.ConfigModule
import com.bugsnag.android.internal.dag.DependencyModule

/**
 * A dependency module which constructs objects that track launch/session related information
 * in Bugsnag.
 */
internal class TrackerModule(
    configModule: ConfigModule,
    storageModule: StorageModule,
    client: Client,
    bgTaskService: BackgroundTaskService,
    callbackState: CallbackState
) : DependencyModule() {

    private val config = configModule.config

    val launchCrashTracker = LaunchCrashTracker(config)

    val sessionTracker = SessionTracker(
        config,
        callbackState,
        client,
        storageModule.sessionStore,
        config.logger,
        bgTaskService
    )
}
