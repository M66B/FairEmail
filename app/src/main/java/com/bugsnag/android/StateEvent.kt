package com.bugsnag.android

sealed class StateEvent {
    class Install(
        val apiKey: String,
        val autoDetectNdkCrashes: Boolean,
        val appVersion: String?,
        val buildUuid: String?,
        val releaseStage: String?,
        val lastRunInfoPath: String,
        val consecutiveLaunchCrashes: Int
    ) : StateEvent()

    object DeliverPending : StateEvent()

    class AddMetadata(val section: String, val key: String?, val value: Any?) : StateEvent()
    class ClearMetadataSection(val section: String) : StateEvent()
    class ClearMetadataValue(val section: String, val key: String?) : StateEvent()

    class AddBreadcrumb(
        val message: String,
        val type: BreadcrumbType,
        val timestamp: String,
        val metadata: MutableMap<String, Any?>
    ) : StateEvent()

    object NotifyHandled : StateEvent()
    object NotifyUnhandled : StateEvent()

    object PauseSession : StateEvent()
    class StartSession(
        val id: String,
        val startedAt: String,
        val handledCount: Int,
        val unhandledCount: Int
    ) : StateEvent()

    class UpdateContext(val context: String?) : StateEvent()
    class UpdateInForeground(val inForeground: Boolean, val contextActivity: String?) : StateEvent()
    class UpdateLastRunInfo(val consecutiveLaunchCrashes: Int) : StateEvent()
    class UpdateIsLaunching(val isLaunching: Boolean) : StateEvent()
    class UpdateOrientation(val orientation: String?) : StateEvent()

    class UpdateUser(val user: User) : StateEvent()

    class UpdateMemoryTrimEvent(val isLowMemory: Boolean) : StateEvent()
}
