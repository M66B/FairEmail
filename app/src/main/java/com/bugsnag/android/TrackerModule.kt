package com.bugsnag.android

import com.bugsnag.android.internal.BackgroundTaskService
import com.bugsnag.android.internal.dag.BackgroundDependencyModule
import com.bugsnag.android.internal.dag.ConfigModule

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
) : BackgroundDependencyModule(bgTaskService) {

    private val config = configModule.config

    val launchCrashTracker = LaunchCrashTracker(config)

    val sessionTracker = provider {
        client.config
        SessionTracker(
            config,
            callbackState,
            client,
            storageModule.sessionStore.get(),
            config.logger,
            bgTaskService
        )
    }
}
