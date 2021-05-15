package com.bugsnag.android

internal class ClientObservable : BaseObservable() {

    fun postOrientationChange(orientation: String?) {
        notifyObservers(StateEvent.UpdateOrientation(orientation))
    }

    fun postNdkInstall(conf: ImmutableConfig, lastRunInfoPath: String, consecutiveLaunchCrashes: Int) {
        notifyObservers(
            StateEvent.Install(
                conf.apiKey,
                conf.enabledErrorTypes.ndkCrashes,
                conf.appVersion,
                conf.buildUuid,
                conf.releaseStage,
                lastRunInfoPath,
                consecutiveLaunchCrashes
            )
        )
    }

    fun postNdkDeliverPending() {
        notifyObservers(StateEvent.DeliverPending)
    }
}
