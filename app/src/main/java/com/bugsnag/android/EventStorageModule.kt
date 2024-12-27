package com.bugsnag.android

import com.bugsnag.android.internal.BackgroundTaskService
import com.bugsnag.android.internal.dag.BackgroundDependencyModule
import com.bugsnag.android.internal.dag.ConfigModule
import com.bugsnag.android.internal.dag.ContextModule
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
) : BackgroundDependencyModule(bgTaskService) {

    private val cfg = configModule.config

    private val delegate = provider {
        if (cfg.telemetry.contains(Telemetry.INTERNAL_ERRORS))
            InternalReportDelegate(
                contextModule.ctx,
                cfg.logger,
                cfg,
                systemServiceModule.storageManager,
                dataCollectionModule.appDataCollector.get(),
                dataCollectionModule.deviceDataCollector,
                trackerModule.sessionTracker.get(),
                notifier,
                bgTaskService
            ) else null
    }

    val eventStore = provider {
        EventStore(
            cfg,
            cfg.logger,
            notifier,
            bgTaskService,
            delegate.getOrNull(),
            callbackState
        )
    }
}
