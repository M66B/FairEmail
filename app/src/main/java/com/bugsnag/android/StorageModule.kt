package com.bugsnag.android

import android.content.Context
import com.bugsnag.android.internal.ImmutableConfig
import com.bugsnag.android.internal.dag.DependencyModule

/**
 * A dependency module which constructs the objects that store information to disk in Bugsnag.
 */
internal class StorageModule(
    appContext: Context,
    immutableConfig: ImmutableConfig,
    logger: Logger
) : DependencyModule() {

    val sharedPrefMigrator by future { SharedPrefMigrator(appContext) }

    private val deviceIdStore by future {
        DeviceIdStore(
            appContext,
            sharedPrefMigrator = sharedPrefMigrator,
            logger = logger
        )
    }

    val deviceId by future { deviceIdStore.loadDeviceId() }

    val internalDeviceId by future { deviceIdStore.loadInternalDeviceId() }

    val userStore by future {
        UserStore(
            immutableConfig,
            deviceId,
            sharedPrefMigrator = sharedPrefMigrator,
            logger = logger
        )
    }

    val lastRunInfoStore by future { LastRunInfoStore(immutableConfig) }

    val sessionStore by future { SessionStore(immutableConfig, logger, null) }

    val lastRunInfo by future {
        val info = lastRunInfoStore.load()
        val currentRunInfo = LastRunInfo(0, crashed = false, crashedDuringLaunch = false)
        lastRunInfoStore.persist(currentRunInfo)
        info
    }
}
