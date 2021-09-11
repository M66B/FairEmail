package com.bugsnag.android

import android.content.ComponentCallbacks2
import android.content.res.Configuration

internal class ClientComponentCallbacks(
    private val deviceDataCollector: DeviceDataCollector,
    private val cb: (oldOrientation: String?, newOrientation: String?) -> Unit,
    val memoryCallback: (Boolean, Int?) -> Unit
) : ComponentCallbacks2 {

    override fun onConfigurationChanged(newConfig: Configuration) {
        val oldOrientation = deviceDataCollector.getOrientationAsString()

        if (deviceDataCollector.updateOrientation(newConfig.orientation)) {
            val newOrientation = deviceDataCollector.getOrientationAsString()
            cb(oldOrientation, newOrientation)
        }
    }

    override fun onTrimMemory(level: Int) {
        memoryCallback(level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE, level)
    }

    override fun onLowMemory() {
        memoryCallback(true, null)
    }
}
