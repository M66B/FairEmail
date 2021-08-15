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
    notifier: Notifier
) : DependencyModule() {

    private val cfg = configModule.config

    private val delegate by future {
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
        )
    }

    val eventStore by future { EventStore(cfg, cfg.logger, notifier, bgTaskService, delegate) }
}
