package com.bugsnag.android

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE_PRE_26
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING_PRE_28
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.app.ActivityManager.RunningAppProcessInfo.REASON_PROVIDER_IN_USE
import android.app.ActivityManager.RunningAppProcessInfo.REASON_SERVICE_IN_USE
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Process
import android.os.SystemClock
import com.bugsnag.android.internal.ImmutableConfig

/**
 * Collects various data on the application state
 */
internal class AppDataCollector(
    appContext: Context,
    private val packageManager: PackageManager?,
    private val config: ImmutableConfig,
    private val sessionTracker: SessionTracker,
    private val activityManager: ActivityManager?,
    private val launchCrashTracker: LaunchCrashTracker,
    private val memoryTrimState: MemoryTrimState
) {

    var codeBundleId: String? = null

    private val packageName: String = appContext.packageName
    private val bgWorkRestricted = isBackgroundWorkRestricted()

    private var binaryArch: String? = null
    private val appName = getAppName()
    private val processName = findProcessName()
    private val releaseStage = config.releaseStage
    private val versionName = config.appVersion ?: config.packageInfo?.versionName
    private val installerPackage = getInstallerPackageName()

    fun generateApp(): App =
        App(config, binaryArch, packageName, releaseStage, versionName, codeBundleId)

    fun generateAppWithState(): AppWithState {
        val inForeground = sessionTracker.isInForeground
        val durationInForeground = calculateDurationInForeground(inForeground)

        return AppWithState(
            config, binaryArch, packageName, releaseStage, versionName, codeBundleId,
            getDurationMs(), durationInForeground, inForeground,
            launchCrashTracker.isLaunching()
        )
    }

    @SuppressLint("SwitchIntDef")
    @Suppress("DEPRECATION")
    private fun getProcessImportance(): String? {
        try {
            val appInfo = ActivityManager.RunningAppProcessInfo()
            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                ActivityManager.getMyMemoryState(appInfo)
            } else {
                val expectedPid = Process.myPid()
                activityManager?.runningAppProcesses
                    ?.find { it.pid == expectedPid }
                    ?.let {
                        appInfo.importance = it.importance
                        appInfo.pid = expectedPid
                    }
            }

            if (appInfo.pid == 0) {
                return null
            }

            return when (appInfo.importance) {
                IMPORTANCE_FOREGROUND -> "foreground"
                IMPORTANCE_FOREGROUND_SERVICE -> "foreground service"
                IMPORTANCE_TOP_SLEEPING -> "top sleeping"
                IMPORTANCE_TOP_SLEEPING_PRE_28 -> "top sleeping"
                IMPORTANCE_VISIBLE -> "visible"
                IMPORTANCE_PERCEPTIBLE -> "perceptible"
                IMPORTANCE_PERCEPTIBLE_PRE_26 -> "perceptible"
                IMPORTANCE_CANT_SAVE_STATE -> "can't save state"
                IMPORTANCE_CANT_SAVE_STATE_PRE_26 -> "can't save state"
                IMPORTANCE_SERVICE -> "service"
                IMPORTANCE_CACHED -> "cached/background"
                IMPORTANCE_GONE -> "gone"
                IMPORTANCE_EMPTY -> "empty"
                REASON_PROVIDER_IN_USE -> "provider in use"
                REASON_SERVICE_IN_USE -> "service in use"
                else -> "unknown importance (${appInfo.importance})"
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun getAppDataMetadata(): MutableMap<String, Any?> {
        val map = HashMap<String, Any?>()
        map["name"] = appName
        map["activeScreen"] = sessionTracker.contextActivity
        map["lowMemory"] = memoryTrimState.isLowMemory
        map["memoryTrimLevel"] = memoryTrimState.trimLevelDescription
        map["processImportance"] = getProcessImportance()

        populateRuntimeMemoryMetadata(map)

        bgWorkRestricted?.let {
            map["backgroundWorkRestricted"] = bgWorkRestricted
        }
        processName?.let {
            map["processName"] = it
        }
        return map
    }

    private fun populateRuntimeMemoryMetadata(map: MutableMap<String, Any?>) {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        map["memoryUsage"] = totalMemory - freeMemory
        map["totalMemory"] = totalMemory
        map["freeMemory"] = freeMemory
        map["memoryLimit"] = runtime.maxMemory()
        map["installerPackage"] = installerPackage
    }

    /**
     * Checks whether the user has restricted the amount of work this app can do in the background.
     * https://developer.android.com/reference/android/app/ActivityManager#isBackgroundRestricted()
     */
    private fun isBackgroundWorkRestricted(): Boolean? {
        return if (activityManager == null || VERSION.SDK_INT < VERSION_CODES.P) {
            null
        } else if (activityManager.isBackgroundRestricted) {
            true // only return non-null value if true to avoid noise in error reports
        } else {
            null
        }
    }

    fun setBinaryArch(binaryArch: String) {
        this.binaryArch = binaryArch
    }

    /**
     * Calculates the duration the app has been in the foreground
     *
     * @return the duration in ms
     */
    internal fun calculateDurationInForeground(inForeground: Boolean? = sessionTracker.isInForeground): Long? {
        if (inForeground == null) {
            return null
        }

        val nowMs = SystemClock.elapsedRealtime()
        var durationMs: Long = 0

        val sessionStartTimeMs: Long = sessionTracker.lastEnteredForegroundMs

        if (inForeground && sessionStartTimeMs != 0L) {
            durationMs = nowMs - sessionStartTimeMs
        }

        return if (durationMs > 0) durationMs else 0
    }

    /**
     * The name of the running Android app, from android:label in
     * AndroidManifest.xml
     */
    private fun getAppName(): String? {
        val copy = config.appInfo
        return when {
            packageManager != null && copy != null -> {
                packageManager.getApplicationLabel(copy).toString()
            }

            else -> null
        }
    }

    /**
     * The name of installer / vendor package of the app
     */
    fun getInstallerPackageName(): String? {
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.R)
                return packageManager?.getInstallSourceInfo(packageName)?.installingPackageName
            @Suppress("DEPRECATION")
            return packageManager?.getInstallerPackageName(packageName)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Finds the name of the current process, or null if this cannot be found.
     */
    @SuppressLint("PrivateApi")
    private fun findProcessName(): String? {
        return runCatching {
            when {
                VERSION.SDK_INT >= VERSION_CODES.P -> {
                    Application.getProcessName()
                }

                else -> {
                    // see https://stackoverflow.com/questions/19631894
                    val clz = Class.forName("android.app.ActivityThread")
                    val methodName = when {
                        VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2 -> "currentProcessName"
                        else -> "currentPackageName"
                    }

                    val getProcessName = clz.getDeclaredMethod(methodName)
                    getProcessName.invoke(null) as String
                }
            }
        }.getOrNull()
    }

    companion object {
        internal val startTimeMs = SystemClock.elapsedRealtime()

        /**
         * Get the time in milliseconds since Bugsnag was initialized, which is a
         * good approximation for how long the app has been running.
         */
        fun getDurationMs(): Long = SystemClock.elapsedRealtime() - startTimeMs

        private const val IMPORTANCE_CANT_SAVE_STATE_PRE_26 = 170
    }
}
