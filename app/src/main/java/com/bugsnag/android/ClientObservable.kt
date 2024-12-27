package com.bugsnag.android

import com.bugsnag.android.internal.ImmutableConfig

internal class ClientObservable : BaseObservable() {

    fun postOrientationChange(orientation: String?) {
        updateState { StateEvent.UpdateOrientation(orientation) }
    }

    fun postNdkInstall(
        conf: ImmutableConfig,
        lastRunInfoPath: String,
        consecutiveLaunchCrashes: Int
    ) {
        updateState {
            StateEvent.Install(
                conf.apiKey,
                conf.enabledErrorTypes.ndkCrashes,
                conf.appVersion,
                conf.buildUuid?.getOrNull(),
                conf.releaseStage,
                lastRunInfoPath,
                consecutiveLaunchCrashes,
                conf.sendThreads,
                conf.maxBreadcrumbs
            )
        }
    }

    fun postNdkDeliverPending() {
        updateState { StateEvent.DeliverPending }
    }
}
