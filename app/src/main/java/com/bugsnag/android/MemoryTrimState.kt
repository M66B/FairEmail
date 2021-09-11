package com.bugsnag.android

import android.content.ComponentCallbacks2

internal class MemoryTrimState : BaseObservable() {
    var isLowMemory: Boolean = false
    var memoryTrimLevel: Int? = null

    val trimLevelDescription: String get() = descriptionFor(memoryTrimLevel)

    fun updateMemoryTrimLevel(newTrimLevel: Int?): Boolean {
        if (memoryTrimLevel == newTrimLevel) {
            return false
        }

        memoryTrimLevel = newTrimLevel
        return true
    }

    fun emitObservableEvent() {
        updateState {
            StateEvent.UpdateMemoryTrimEvent(
                isLowMemory,
                memoryTrimLevel,
                trimLevelDescription
            )
        }
    }

    private fun descriptionFor(memoryTrimLevel: Int?) = when (memoryTrimLevel) {
        null -> "None"
        ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "Complete"
        ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "Moderate"
        ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "Background"
        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI hidden"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "Running critical"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "Running low"
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "Running moderate"
        else -> "Unknown ($memoryTrimLevel)"
    }
}
