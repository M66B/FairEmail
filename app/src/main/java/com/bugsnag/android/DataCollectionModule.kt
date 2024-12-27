package com.bugsnag.android

import android.os.Environment
import com.bugsnag.android.internal.BackgroundTaskService
import com.bugsnag.android.internal.dag.BackgroundDependencyModule
import com.bugsnag.android.internal.dag.ConfigModule
import com.bugsnag.android.internal.dag.ContextModule
import com.bugsnag.android.internal.dag.Provider
import com.bugsnag.android.internal.dag.SystemServiceModule

/**
 * A dependency module which constructs the objects that collect data in Bugsnag. For example, this
 * class is responsible for creating classes which capture device-specific information.
 */
internal class DataCollectionModule(
    contextModule: ContextModule,
    configModule: ConfigModule,
    systemServiceModule: SystemServiceModule,
    trackerModule: TrackerModule,
    bgTaskService: BackgroundTaskService,
    connectivity: Connectivity,
    deviceIdStore: Provider<DeviceIdStore>,
    memoryTrimState: MemoryTrimState
) : BackgroundDependencyModule(bgTaskService) {

    private val ctx = contextModule.ctx
    private val cfg = configModule.config
    private val logger = cfg.logger
    private val deviceBuildInfo: DeviceBuildInfo = DeviceBuildInfo.defaultInfo()
    private val dataDir = Environment.getDataDirectory()

    val appDataCollector = provider {
        AppDataCollector(
            ctx,
            ctx.packageManager,
            cfg,
            trackerModule.sessionTracker.get(),
            systemServiceModule.activityManager,
            trackerModule.launchCrashTracker,
            memoryTrimState
        )
    }

    private val rootDetection = provider {
        val rootDetector = RootDetector(logger = logger, deviceBuildInfo = deviceBuildInfo)
        rootDetector.isRooted()
    }

    val deviceDataCollector = provider {
        DeviceDataCollector(
            connectivity,
            ctx,
            ctx.resources,
            deviceIdStore.map { it.load() },
            deviceBuildInfo,
            dataDir,
            rootDetection,
            bgTaskService,
            logger
        )
    }
}
