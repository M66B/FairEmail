package com.bugsnag.android

import android.content.ComponentCallbacks
import android.content.res.Configuration

internal class ClientComponentCallbacks(
    private val deviceDataCollector: DeviceDataCollector,
    private val cb: (oldOrientation: String?, newOrientation: String?) -> Unit,
    val callback: (Boolean) -> Unit
) : ComponentCallbacks {

    override fun onConfigurationChanged(newConfig: Configuration) {
        val oldOrientation = deviceDataCollector.getOrientationAsString()

        if (deviceDataCollector.updateOrientation(newConfig.orientation)) {
            val newOrientation = deviceDataCollector.getOrientationAsString()
            cb(oldOrientation, newOrientation)
        }
    }

    override fun onLowMemory() {
        callback(true)
    }
}
