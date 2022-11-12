package com.bugsnag.android

import java.lang.reflect.Method

/**
 * Calls the NDK plugin if it is loaded, otherwise does nothing / returns the default.
 */
internal object NdkPluginCaller {
    private var ndkPlugin: Plugin? = null
    private var setInternalMetricsEnabled: Method? = null
    private var setStaticData: Method? = null
    private var getSignalUnwindStackFunction: Method? = null
    private var getCurrentCallbackSetCounts: Method? = null
    private var getCurrentNativeApiCallUsage: Method? = null
    private var initCallbackCounts: Method? = null
    private var notifyAddCallback: Method? = null
    private var notifyRemoveCallback: Method? = null

    private fun getMethod(name: String, vararg parameterTypes: Class<*>): Method? {
        val plugin = ndkPlugin
        if (plugin == null) {
            return null
        }
        return plugin.javaClass.getMethod(name, *parameterTypes)
    }

    fun setNdkPlugin(plugin: Plugin?) {
        if (plugin != null) {
            ndkPlugin = plugin
            setInternalMetricsEnabled = getMethod("setInternalMetricsEnabled", Boolean::class.java)
            setStaticData = getMethod("setStaticData", Map::class.java)
            getSignalUnwindStackFunction = getMethod("getSignalUnwindStackFunction")
            getCurrentCallbackSetCounts = getMethod("getCurrentCallbackSetCounts")
            getCurrentNativeApiCallUsage = getMethod("getCurrentNativeApiCallUsage")
            initCallbackCounts = getMethod("initCallbackCounts", Map::class.java)
            notifyAddCallback = getMethod("notifyAddCallback", String::class.java)
            notifyRemoveCallback = getMethod("notifyRemoveCallback", String::class.java)
        }
    }

    fun getSignalUnwindStackFunction(): Long {
        val method = getSignalUnwindStackFunction
        if (method != null) {
            return method.invoke(ndkPlugin) as Long
        }
        return 0
    }

    fun setInternalMetricsEnabled(enabled: Boolean) {
        val method = setInternalMetricsEnabled
        if (method != null) {
            method.invoke(ndkPlugin, enabled)
        }
    }

    fun getCurrentCallbackSetCounts(): Map<String, Int>? {
        val method = getCurrentCallbackSetCounts
        if (method != null) {
            @Suppress("UNCHECKED_CAST")
            return method.invoke(ndkPlugin) as Map<String, Int>
        }
        return null
    }

    fun getCurrentNativeApiCallUsage(): Map<String, Boolean>? {
        val method = getCurrentNativeApiCallUsage
        if (method != null) {
            @Suppress("UNCHECKED_CAST")
            return method.invoke(ndkPlugin) as Map<String, Boolean>
        }
        return null
    }

    fun initCallbackCounts(counts: Map<String, Int>) {
        val method = initCallbackCounts
        if (method != null) {
            method.invoke(ndkPlugin, counts)
        }
    }

    fun notifyAddCallback(callback: String) {
        val method = notifyAddCallback
        if (method != null) {
            method.invoke(ndkPlugin, callback)
        }
    }

    fun notifyRemoveCallback(callback: String) {
        val method = notifyRemoveCallback
        if (method != null) {
            method.invoke(ndkPlugin, callback)
        }
    }

    fun setStaticData(data: Map<String, Any>) {
        val method = setStaticData
        if (method != null) {
            method.invoke(ndkPlugin, data)
        }
    }
}
