package com.bugsnag.android

internal class PluginClient(
    userPlugins: Set<Plugin>,
    immutableConfig: ImmutableConfig,
    private val logger: Logger
) {

    protected val plugins: Set<Plugin>

    init {
        val set = mutableSetOf<Plugin>()
        set.addAll(userPlugins)

        // instantiate ANR + NDK plugins by reflection as bugsnag-android-core has no
        // direct dependency on the artefacts
        if (immutableConfig.enabledErrorTypes.ndkCrashes) {
            instantiatePlugin("com.bugsnag.android.NdkPlugin")?.let { set.add(it) }
        }
        if (immutableConfig.enabledErrorTypes.anrs) {
            instantiatePlugin("com.bugsnag.android.AnrPlugin")?.let { set.add(it) }
        }
        instantiatePlugin("com.bugsnag.android.BugsnagReactNativePlugin")?.let { set.add(it) }
        plugins = set.toSet()
    }

    private fun instantiatePlugin(clz: String): Plugin? {
        return try {
            val pluginClz = Class.forName(clz)
            pluginClz.newInstance() as Plugin
        } catch (exc: ClassNotFoundException) {
            logger.d("Plugin '$clz' is not on the classpath - functionality will not be enabled.")
            null
        } catch (exc: Throwable) {
            logger.e("Failed to load plugin '$clz'", exc)
            null
        }
    }

    fun loadPlugins(client: Client) = plugins.forEach {
        try {
            it.load(client)
        } catch (exc: Throwable) {
            logger.e("Failed to load plugin $it, continuing with initialisation.", exc)
        }
    }
}
