package com.bugsnag.android

import android.content.Context
import java.io.File
import java.util.EnumSet

internal class ConfigInternal(
    var apiKey: String
) : CallbackAware, MetadataAware, UserAware, FeatureFlagAware {

    private var user = User()

    @JvmField
    internal val callbackState: CallbackState = CallbackState()

    @JvmField
    internal val metadataState: MetadataState = MetadataState()

    @JvmField
    internal val featureFlagState: FeatureFlagState = FeatureFlagState()

    var appVersion: String? = null
    var versionCode: Int? = 0
    var releaseStage: String? = null
    var sendThreads: ThreadSendPolicy = ThreadSendPolicy.ALWAYS
    var persistUser: Boolean = false

    var launchDurationMillis: Long = DEFAULT_LAUNCH_CRASH_THRESHOLD_MS

    var autoTrackSessions: Boolean = true
    var sendLaunchCrashesSynchronously: Boolean = true
    var enabledErrorTypes: ErrorTypes = ErrorTypes()
    var autoDetectErrors: Boolean = true
    var appType: String? = "android"
    var logger: Logger? = DebugLogger
        set(value) {
            field = value ?: NoopLogger
        }
    var delivery: Delivery? = null
    var endpoints: EndpointConfiguration = EndpointConfiguration()
    var maxBreadcrumbs: Int = DEFAULT_MAX_BREADCRUMBS
    var maxPersistedEvents: Int = DEFAULT_MAX_PERSISTED_EVENTS
    var maxPersistedSessions: Int = DEFAULT_MAX_PERSISTED_SESSIONS
    var maxReportedThreads: Int = DEFAULT_MAX_REPORTED_THREADS
    var context: String? = null

    var redactedKeys: Set<String>
        get() = metadataState.metadata.redactedKeys
        set(value) {
            metadataState.metadata.redactedKeys = value
        }

    var discardClasses: Set<String> = emptySet()
    var enabledReleaseStages: Set<String>? = null
    var enabledBreadcrumbTypes: Set<BreadcrumbType>? = null
    var telemetry: Set<Telemetry> = EnumSet.of(Telemetry.INTERNAL_ERRORS)
    var projectPackages: Set<String> = emptySet()
    var persistenceDirectory: File? = null

    val notifier: Notifier = Notifier()

    protected val plugins = HashSet<Plugin>()

    override fun addOnError(onError: OnErrorCallback) = callbackState.addOnError(onError)
    override fun removeOnError(onError: OnErrorCallback) = callbackState.removeOnError(onError)
    override fun addOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback) =
        callbackState.addOnBreadcrumb(onBreadcrumb)
    override fun removeOnBreadcrumb(onBreadcrumb: OnBreadcrumbCallback) =
        callbackState.removeOnBreadcrumb(onBreadcrumb)
    override fun addOnSession(onSession: OnSessionCallback) = callbackState.addOnSession(onSession)
    override fun removeOnSession(onSession: OnSessionCallback) = callbackState.removeOnSession(onSession)
    fun addOnSend(onSend: OnSendCallback) = callbackState.addOnSend(onSend)
    fun removeOnSend(onSend: OnSendCallback) = callbackState.removeOnSend(onSend)

    override fun addMetadata(section: String, value: Map<String, Any?>) =
        metadataState.addMetadata(section, value)
    override fun addMetadata(section: String, key: String, value: Any?) =
        metadataState.addMetadata(section, key, value)
    override fun clearMetadata(section: String) = metadataState.clearMetadata(section)
    override fun clearMetadata(section: String, key: String) = metadataState.clearMetadata(section, key)
    override fun getMetadata(section: String) = metadataState.getMetadata(section)
    override fun getMetadata(section: String, key: String) = metadataState.getMetadata(section, key)

    override fun addFeatureFlag(name: String) = featureFlagState.addFeatureFlag(name)
    override fun addFeatureFlag(name: String, variant: String?) =
        featureFlagState.addFeatureFlag(name, variant)
    override fun addFeatureFlags(featureFlags: Iterable<FeatureFlag>) =
        featureFlagState.addFeatureFlags(featureFlags)
    override fun clearFeatureFlag(name: String) = featureFlagState.clearFeatureFlag(name)
    override fun clearFeatureFlags() = featureFlagState.clearFeatureFlags()

    override fun getUser(): User = user
    override fun setUser(id: String?, email: String?, name: String?) {
        user = User(id, email, name)
    }

    fun addPlugin(plugin: Plugin) {
        plugins.add(plugin)
    }

    companion object {
        private const val DEFAULT_MAX_BREADCRUMBS = 50
        private const val DEFAULT_MAX_PERSISTED_SESSIONS = 128
        private const val DEFAULT_MAX_PERSISTED_EVENTS = 32
        private const val DEFAULT_MAX_REPORTED_THREADS = 200
        private const val DEFAULT_LAUNCH_CRASH_THRESHOLD_MS: Long = 5000

        @JvmStatic
        fun load(context: Context): Configuration = load(context, null)

        @JvmStatic
        protected fun load(context: Context, apiKey: String?): Configuration {
            return ManifestConfigLoader().load(context, apiKey)
        }
    }
}
