package com.bugsnag.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class ConfigChangeReceiver(
    private val deviceDataCollector: DeviceDataCollector,
    private val cb: (oldOrientation: String?, newOrientation: String?) -> Unit
) : BroadcastReceiver() {

    var orientation = deviceDataCollector.calculateOrientation()

    override fun onReceive(context: Context?, intent: Intent?) {
        val newOrientation = deviceDataCollector.calculateOrientation()

        if (!newOrientation.equals(orientation)) {
            cb(orientation, newOrientation)
            orientation = newOrientation
        }
    }
}
