package com.bugsnag.android

import com.bugsnag.android.internal.dag.ConfigModule
import com.bugsnag.android.internal.dag.ContextModule
import com.bugsnag.android.internal.dag.DependencyModule
import com.bugsnag.android.internal.dag.SystemServiceModule

/**
 * A dependency module which constructs the objects that persist events to disk in Bugsnag.
 */
internal class EventStorageModule(
    contextModule: ContextModule,
    configModule: ConfigModule,
    dataCollectionModule: DataCollectionModule,
    bgTaskService: BackgroundTaskService,
    trackerModule: TrackerModule,
    systemServiceModule: SystemServiceModule,
    notifier: Notifier,
    callbackState: CallbackState
) : DependencyModule() {

    private val cfg = configModule.config

    private val delegate by future {
        if (cfg.telemetry.contains(Telemetry.INTERNAL_ERRORS) == true)
            InternalReportDelegate(
                contextModule.ctx,
                cfg.logger,
                cfg,
                systemServiceModule.storageManager,
                dataCollectionModule.appDataCollector,
                dataCollectionModule.deviceDataCollector,
                trackerModule.sessionTracker,
                notifier,
                bgTaskService
            ) else null
    }

    val eventStore by future { EventStore(cfg, cfg.logger, notifier, bgTaskService, delegate, callbackState) }
}
