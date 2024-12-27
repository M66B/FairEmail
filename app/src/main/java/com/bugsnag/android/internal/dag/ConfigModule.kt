package com.bugsnag.android.internal.dag

import com.bugsnag.android.Configuration
import com.bugsnag.android.Connectivity
import com.bugsnag.android.internal.BackgroundTaskService
import com.bugsnag.android.internal.sanitiseConfiguration

/**
 * A dependency module which constructs the configuration object that is used to alter
 * Bugsnag's default behaviour.
 */
internal class ConfigModule(
    contextModule: ContextModule,
    configuration: Configuration,
    connectivity: Connectivity,
    bgTaskExecutor: BackgroundTaskService
) : BackgroundDependencyModule(bgTaskExecutor) {
    val config = sanitiseConfiguration(contextModule.ctx, configuration, connectivity, bgTaskExecutor)
}
