package com.bugsnag.android

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException

internal class ManifestConfigLoader {

    companion object {
        // mandatory
        private const val BUGSNAG_NS = "com.bugsnag.android"
        private const val API_KEY = "$BUGSNAG_NS.API_KEY"
        internal const val BUILD_UUID = "$BUGSNAG_NS.BUILD_UUID"

        // detection
        private const val AUTO_TRACK_SESSIONS = "$BUGSNAG_NS.AUTO_TRACK_SESSIONS"
        private const val AUTO_DETECT_ERRORS = "$BUGSNAG_NS.AUTO_DETECT_ERRORS"
        private const val PERSIST_USER = "$BUGSNAG_NS.PERSIST_USER"
        private const val SEND_THREADS = "$BUGSNAG_NS.SEND_THREADS"

        // endpoints
        private const val ENDPOINT_NOTIFY = "$BUGSNAG_NS.ENDPOINT_NOTIFY"
        private const val ENDPOINT_SESSIONS = "$BUGSNAG_NS.ENDPOINT_SESSIONS"

        // app/project packages
        private const val APP_VERSION = "$BUGSNAG_NS.APP_VERSION"
        private const val VERSION_CODE = "$BUGSNAG_NS.VERSION_CODE"
        private const val RELEASE_STAGE = "$BUGSNAG_NS.RELEASE_STAGE"
        private const val ENABLED_RELEASE_STAGES = "$BUGSNAG_NS.ENABLED_RELEASE_STAGES"
        private const val DISCARD_CLASSES = "$BUGSNAG_NS.DISCARD_CLASSES"
        private const val PROJECT_PACKAGES = "$BUGSNAG_NS.PROJECT_PACKAGES"
        private const val REDACTED_KEYS = "$BUGSNAG_NS.REDACTED_KEYS"

        // misc
        private const val MAX_BREADCRUMBS = "$BUGSNAG_NS.MAX_BREADCRUMBS"
        private const val MAX_PERSISTED_EVENTS = "$BUGSNAG_NS.MAX_PERSISTED_EVENTS"
        private const val MAX_PERSISTED_SESSIONS = "$BUGSNAG_NS.MAX_PERSISTED_SESSIONS"
        private const val MAX_REPORTED_THREADS = "$BUGSNAG_NS.MAX_REPORTED_THREADS"
        private const val LAUNCH_CRASH_THRESHOLD_MS = "$BUGSNAG_NS.LAUNCH_CRASH_THRESHOLD_MS"
        private const val LAUNCH_DURATION_MILLIS = "$BUGSNAG_NS.LAUNCH_DURATION_MILLIS"
        private const val SEND_LAUNCH_CRASHES_SYNCHRONOUSLY = "$BUGSNAG_NS.SEND_LAUNCH_CRASHES_SYNCHRONOUSLY"
        private const val APP_TYPE = "$BUGSNAG_NS.APP_TYPE"
    }

    fun load(ctx: Context, userSuppliedApiKey: String?): Configuration {
        try {
            val packageManager = ctx.packageManager
            val packageName = ctx.packageName
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val data = ai.metaData
            return load(data, userSuppliedApiKey)
        } catch (exc: Exception) {
            throw IllegalStateException("Bugsnag is unable to read config from manifest.", exc)
        }
    }

    /**
     * Populates the config with meta-data values supplied from the manifest as a Bundle.
     *
     * @param data   the manifest bundle
     */
    @VisibleForTesting
    internal fun load(data: Bundle?, userSuppliedApiKey: String?): Configuration {
        // get the api key from the JVM call, or lookup in the manifest if null
        val apiKey = (userSuppliedApiKey ?: data?.getString(API_KEY))
            ?: throw IllegalArgumentException("No Bugsnag API key set")
        val config = Configuration(apiKey)

        if (data != null) {
            loadDetectionConfig(config, data)
            loadEndpointsConfig(config, data)
            loadAppConfig(config, data)

            // misc config
            with(config) {
                maxBreadcrumbs = data.getInt(MAX_BREADCRUMBS, maxBreadcrumbs)
                maxPersistedEvents = data.getInt(MAX_PERSISTED_EVENTS, maxPersistedEvents)
                maxPersistedSessions = data.getInt(MAX_PERSISTED_SESSIONS, maxPersistedSessions)
                maxReportedThreads = data.getInt(MAX_REPORTED_THREADS, maxReportedThreads)
                launchDurationMillis = data.getInt(
                    LAUNCH_CRASH_THRESHOLD_MS,
                    launchDurationMillis.toInt()
                ).toLong()
                launchDurationMillis = data.getInt(
                    LAUNCH_DURATION_MILLIS,
                    launchDurationMillis.toInt()
                ).toLong()
                sendLaunchCrashesSynchronously = data.getBoolean(
                    SEND_LAUNCH_CRASHES_SYNCHRONOUSLY,
                    sendLaunchCrashesSynchronously
                )
            }
        }
        return config
    }

    private fun loadDetectionConfig(config: Configuration, data: Bundle) {
        with(config) {
            autoTrackSessions = data.getBoolean(AUTO_TRACK_SESSIONS, autoTrackSessions)
            autoDetectErrors = data.getBoolean(AUTO_DETECT_ERRORS, autoDetectErrors)
            persistUser = data.getBoolean(PERSIST_USER, persistUser)

            val str = data.getString(SEND_THREADS)

            if (str != null) {
                sendThreads = ThreadSendPolicy.fromString(str)
            }
        }
    }

    private fun loadEndpointsConfig(config: Configuration, data: Bundle) {
        if (data.containsKey(ENDPOINT_NOTIFY)) {
            val endpoint = data.getString(ENDPOINT_NOTIFY, config.endpoints.notify)
            val sessionEndpoint = data.getString(ENDPOINT_SESSIONS, config.endpoints.sessions)
            config.endpoints = EndpointConfiguration(endpoint, sessionEndpoint)
        }
    }

    private fun loadAppConfig(config: Configuration, data: Bundle) {
        with(config) {
            releaseStage = data.getString(RELEASE_STAGE, config.releaseStage)
            appVersion = data.getString(APP_VERSION, config.appVersion)
            appType = data.getString(APP_TYPE, config.appType)

            if (data.containsKey(VERSION_CODE)) {
                versionCode = data.getInt(VERSION_CODE)
            }
            if (data.containsKey(ENABLED_RELEASE_STAGES)) {
                enabledReleaseStages = getStrArray(data, ENABLED_RELEASE_STAGES, enabledReleaseStages)
            }
            discardClasses = getStrArray(data, DISCARD_CLASSES, discardClasses) ?: emptySet()
            projectPackages = getStrArray(data, PROJECT_PACKAGES, emptySet()) ?: emptySet()
            redactedKeys = getStrArray(data, REDACTED_KEYS, redactedKeys) ?: emptySet()
        }
    }

    private fun getStrArray(
        data: Bundle,
        key: String,
        default: Set<String>?
    ): Set<String>? {
        val delimitedStr = data.getString(key)

        return when (val ary = delimitedStr?.split(",")) {
            null -> default
            else -> ary.toSet()
        }
    }
}
