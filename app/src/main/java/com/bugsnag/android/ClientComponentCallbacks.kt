package com.bugsnag.android

import android.content.ComponentCallbacks
import android.content.res.Configuration

internal class ClientComponentCallbacks(
    val callback: (Boolean) -> Unit
) : ComponentCallbacks {
    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onLowMemory() {
        callback(true)
    }
}
