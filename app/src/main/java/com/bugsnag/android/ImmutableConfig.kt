package com.bugsnag.android

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.io.File

internal data class ImmutableConfig(
    val apiKey: String,
    val autoDetectErrors: Boolean,
    val enabledErrorTypes: ErrorTypes,
    val autoTrackSessions: Boolean,
    val sendThreads: ThreadSendPolicy,
    val discardClasses: Collection<String>,
    val enabledReleaseStages: Collection<String>?,
    val projectPackages: Collection<String>,
    val enabledBreadcrumbTypes: Set<BreadcrumbType>?,
    val releaseStage: String?,
    val buildUuid: String?,
    val appVersion: String?,
    val versionCode: Int?,
    val appType: String?,
    val delivery: Delivery,
    val endpoints: EndpointConfiguration,
    val persistUser: Boolean,
    val launchDurationMillis: Long,
    val logger: Logger,
    val maxBreadcrumbs: Int,
    val maxPersistedEvents: Int,
    val maxPersistedSessions: Int,
    val persistenceDirectory: File,
    val sendLaunchCrashesSynchronously: Boolean
) {

    /**
     * Checks if the given release stage should be notified or not
     *
     * @return true if the release state should be notified else false
     */
    @JvmName("shouldNotifyForReleaseStage")
    internal fun shouldNotifyForReleaseStage() =
        enabledReleaseStages == null || enabledReleaseStages.contains(releaseStage)

    @JvmName("shouldRecordBreadcrumbType")
    internal fun shouldRecordBreadcrumbType(type: BreadcrumbType) =
        enabledBreadcrumbTypes == null || enabledBreadcrumbTypes.contains(type)

    @JvmName("getErrorApiDeliveryParams")
    internal fun getErrorApiDeliveryParams(payload: EventPayload) =
        DeliveryParams(endpoints.notify, errorApiHeaders(payload))

    @JvmName("getSessionApiDeliveryParams")
    internal fun getSessionApiDeliveryParams() =
        DeliveryParams(endpoints.sessions, sessionApiHeaders(apiKey))
}

internal fun convertToImmutableConfig(
    config: Configuration,
    buildUuid: String? = null
): ImmutableConfig {
    val errorTypes = when {
        config.autoDetectErrors -> config.enabledErrorTypes.copy()
        else -> ErrorTypes(false)
    }

    return ImmutableConfig(
        apiKey = config.apiKey,
        autoDetectErrors = config.autoDetectErrors,
        enabledErrorTypes = errorTypes,
        autoTrackSessions = config.autoTrackSessions,
        sendThreads = config.sendThreads,
        discardClasses = config.discardClasses.toSet(),
        enabledReleaseStages = config.enabledReleaseStages?.toSet(),
        projectPackages = config.projectPackages.toSet(),
        releaseStage = config.releaseStage,
        buildUuid = buildUuid,
        appVersion = config.appVersion,
        versionCode = config.versionCode,
        appType = config.appType,
        delivery = config.delivery,
        endpoints = config.endpoints,
        persistUser = config.persistUser,
        launchDurationMillis = config.launchDurationMillis,
        logger = config.logger!!,
        maxBreadcrumbs = config.maxBreadcrumbs,
        maxPersistedEvents = config.maxPersistedEvents,
        maxPersistedSessions = config.maxPersistedSessions,
        enabledBreadcrumbTypes = config.enabledBreadcrumbTypes?.toSet(),
        persistenceDirectory = config.persistenceDirectory!!,
        sendLaunchCrashesSynchronously = config.sendLaunchCrashesSynchronously
    )
}

internal fun sanitiseConfiguration(
    appContext: Context,
    configuration: Configuration,
    connectivity: Connectivity
): ImmutableConfig {
    val packageName = appContext.packageName
    val packageManager = appContext.packageManager
    val packageInfo = runCatching { packageManager.getPackageInfo(packageName, 0) }.getOrNull()
    val appInfo = runCatching {
        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    }.getOrNull()

    // populate releaseStage
    if (configuration.releaseStage == null) {
        configuration.releaseStage = when {
            appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) -> RELEASE_STAGE_DEVELOPMENT
            else -> RELEASE_STAGE_PRODUCTION
        }
    }

    // if the user has set the releaseStage to production manually, disable logging
    if (configuration.logger == null || configuration.logger == DebugLogger) {
        val releaseStage = configuration.releaseStage
        val loggingEnabled = RELEASE_STAGE_PRODUCTION != releaseStage

        if (loggingEnabled) {
            configuration.logger = DebugLogger
        } else {
            configuration.logger = NoopLogger
        }
    }

    if (configuration.versionCode == null || configuration.versionCode == 0) {
        @Suppress("DEPRECATION")
        configuration.versionCode = packageInfo?.versionCode
    }

    // Set sensible defaults if project packages not already set
    if (configuration.projectPackages.isEmpty()) {
        configuration.projectPackages = setOf<String>(packageName)
    }

    // populate buildUUID from manifest
    val buildUuid = appInfo?.metaData?.getString(ManifestConfigLoader.BUILD_UUID)

    @Suppress("SENSELESS_COMPARISON")
    if (configuration.delivery == null) {
        configuration.delivery = DefaultDelivery(connectivity, configuration.logger!!)
    }

    if (configuration.persistenceDirectory == null) {
        configuration.persistenceDirectory = appContext.cacheDir
    }
    return convertToImmutableConfig(configuration, buildUuid)
}

internal const val RELEASE_STAGE_DEVELOPMENT = "development"
internal const val RELEASE_STAGE_PRODUCTION = "production"
