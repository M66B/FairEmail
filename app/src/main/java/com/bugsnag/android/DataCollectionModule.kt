package com.bugsnag.android

import android.os.Environment
import com.bugsnag.android.internal.dag.ConfigModule
import com.bugsnag.android.internal.dag.ContextModule
import com.bugsnag.android.internal.dag.DependencyModule
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
    deviceId: String?,
    internalDeviceId: String?,
    memoryTrimState: MemoryTrimState
) : DependencyModule() {

    private val ctx = contextModule.ctx
    private val cfg = configModule.config
    private val logger = cfg.logger
    private val deviceBuildInfo: DeviceBuildInfo = DeviceBuildInfo.defaultInfo()
    private val dataDir = Environment.getDataDirectory()

    val appDataCollector by future {
        AppDataCollector(
            ctx,
            ctx.packageManager,
            cfg,
            trackerModule.sessionTracker,
            systemServiceModule.activityManager,
            trackerModule.launchCrashTracker,
            memoryTrimState
        )
    }

    private val rootDetector by future {
        RootDetector(logger = logger, deviceBuildInfo = deviceBuildInfo)
    }

    val deviceDataCollector by future {
        DeviceDataCollector(
            connectivity,
            ctx,
            ctx.resources,
            deviceId,
            internalDeviceId,
            deviceBuildInfo,
            dataDir,
            rootDetector,
            bgTaskService,
            logger
        )
    }
}
