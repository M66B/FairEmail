package com.bugsnag.android

/**
 * Provides information about the last launch of the application, if there was one.
 */
class LastRunInfo(

    /**
     * The number times the app has consecutively crashed during its launch period.
     */
    val consecutiveLaunchCrashes: Int,

    /**
     * Whether the last app run ended with a crash, or was abnormally terminated by the system.
     */
    val crashed: Boolean,

    /**
     * True if the previous app run ended with a crash during its launch period.
     */
    val crashedDuringLaunch: Boolean
) {
    override fun toString(): String {
        return "LastRunInfo(consecutiveLaunchCrashes=$consecutiveLaunchCrashes, crashed=$crashed, crashedDuringLaunch=$crashedDuringLaunch)"
    }
}
