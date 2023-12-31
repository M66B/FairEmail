package com.bugsnag.android.internal

import com.bugsnag.android.StateEvent

fun interface StateObserver {
    /**
     * This is called whenever the notifier's state is altered, so that observers can react
     * appropriately. This is intended for internal use only.
     */
    fun onStateChange(event: StateEvent)
}
