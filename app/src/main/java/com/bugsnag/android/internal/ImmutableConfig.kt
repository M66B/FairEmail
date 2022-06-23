package com.bugsnag.android.internal

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import com.bugsnag.android.BreadcrumbType
import com.bugsnag.android.Configuration
import com.bugsnag.android.Connectivity
import com.bugsnag.android.DebugLogger
import com.bugsnag.android.DefaultDelivery
import com.bugsnag.android.Delivery
import com.bugsnag.android.DeliveryParams
import com.bugsnag.android.EndpointConfiguration
import com.bugsnag.android.ErrorTypes
import com.bugsnag.android.EventPayload
import com.bugsnag.android.Logger
import com.bugsnag.android.ManifestConfigLoader.Companion.BUILD_UUID
import com.bugsnag.android.NoopLogger
import com.bugsnag.android.Telemetry
import com.bugsnag.android.ThreadSendPolicy
import com.bugsnag.android.errorApiHeaders
import com.bugsnag.android.safeUnrollCauses
import com.bugsnag.android.sessionApiHeaders
import java.io.File

data class ImmutableConfig(
    val apiKey: String,
    val autoDetectErrors: Boolean,
    val enabledErrorTypes: ErrorTypes,
    val autoTrackSessions: Boolean,
    val sendThreads: ThreadSendPolicy,
    val discardClasses: Collection<String>,
    val enabledReleaseStages: Collection<String>?,
    val projectPackages: Collection<String>,
    val enabledBreadcrumbTypes: Set<BreadcrumbType>?,
    val telemetry: Set<Telemetry>,
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
    val maxReportedThreads: Int,
    val persistenceDirectory: Lazy<File>,
    val sendLaunchCrashesSynchronously: Boolean,

    // results cached here to avoid unnecessary lookups in Client.
    val packageInfo: PackageInfo?,
    val appInfo: ApplicationInfo?,
    val redactedKeys: Collection<String>
) {

    @JvmName("getErrorApiDeliveryParams")
    internal fun getErrorApiDeliveryParams(payload: EventPayload) =
        DeliveryParams(endpoints.notify, errorApiHeaders(payload))

    @JvmName("getSessionApiDeliveryParams")
    internal fun getSessionApiDeliveryParams() =
        DeliveryParams(endpoints.sessions, sessionApiHeaders(apiKey))

    /**
     * Returns whether the given throwable should be discarded
     * based on the automatic data capture settings in [Configuration].
     */
    fun shouldDiscardError(exc: Throwable): Boolean {
        return shouldDiscardByReleaseStage() || shouldDiscardByErrorClass(exc)
    }

    /**
     * Returns whether the given error should be discarded
     * based on the automatic data capture settings in [Configuration].
     */
    fun shouldDiscardError(errorClass: String?): Boolean {
        return shouldDiscardByReleaseStage() || shouldDiscardByErrorClass(errorClass)
    }

    /**
     * Returns whether a session should be discarded based on the
     * automatic data capture settings in [Configuration].
     */
    fun shouldDiscardSession(autoCaptured: Boolean): Boolean {
        return shouldDiscardByReleaseStage() || (autoCaptured && !autoTrackSessions)
    }

    /**
     * Returns whether breadcrumbs with the given type should be discarded or not.
     */
    fun shouldDiscardBreadcrumb(type: BreadcrumbType): Boolean {
        return enabledBreadcrumbTypes != null && !enabledBreadcrumbTypes.contains(type)
    }

    /**
     * Returns whether errors/sessions should be discarded or not based on the enabled
     * release stages.
     */
    fun shouldDiscardByReleaseStage(): Boolean {
        return enabledReleaseStages != null && !enabledReleaseStages.contains(releaseStage)
    }

    /**
     * Returns whether errors with the given errorClass should be discarded or not.
     */
    @VisibleForTesting
    internal fun shouldDiscardByErrorClass(errorClass: String?): Boolean {
        return discardClasses.contains(errorClass)
    }

    /**
     * Returns whether errors should be discarded or not based on the errorClass, as deduced
     * by the Throwable's class name.
     */
    @VisibleForTesting
    internal fun shouldDiscardByErrorClass(exc: Throwable): Boolean {
        return exc.safeUnrollCauses().any { throwable ->
            val errorClass = throwable.javaClass.name
            shouldDiscardByErrorClass(errorClass)
        }
    }
}

@JvmOverloads
internal fun convertToImmutableConfig(
    config: Configuration,
    buildUuid: String? = null,
    packageInfo: PackageInfo? = null,
    appInfo: ApplicationInfo? = null,
    persistenceDir: Lazy<File> = lazy { requireNotNull(config.persistenceDirectory) }
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
        maxReportedThreads = config.maxReportedThreads,
        enabledBreadcrumbTypes = config.enabledBreadcrumbTypes?.toSet(),
        telemetry = config.telemetry.toSet(),
        persistenceDirectory = persistenceDir,
        sendLaunchCrashesSynchronously = config.sendLaunchCrashesSynchronously,
        packageInfo = packageInfo,
        appInfo = appInfo,
        redactedKeys = config.redactedKeys.toSet()
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
    val buildUuid = populateBuildUuid(appInfo)

    @Suppress("SENSELESS_COMPARISON")
    if (configuration.delivery == null) {
        configuration.delivery = DefaultDelivery(connectivity, configuration.logger!!)
    }
    return convertToImmutableConfig(
        configuration,
        buildUuid,
        packageInfo,
        appInfo,
        lazy { configuration.persistenceDirectory ?: appContext.cacheDir }
    )
}

private fun populateBuildUuid(appInfo: ApplicationInfo?): String? {
    val bundle = appInfo?.metaData
    return when {
        bundle?.containsKey(BUILD_UUID) == true -> {
            bundle.getString(BUILD_UUID) ?: bundle.getInt(BUILD_UUID).toString()
        }
        else -> null
    }
}

internal const val RELEASE_STAGE_DEVELOPMENT = "development"
internal const val RELEASE_STAGE_PRODUCTION = "production"
