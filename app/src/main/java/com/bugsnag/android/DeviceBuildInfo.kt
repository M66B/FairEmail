package com.bugsnag.android

import android.os.Build

internal class DeviceBuildInfo(
    val manufacturer: String?,
    val model: String?,
    val osVersion: String?,
    val apiLevel: Int?,
    val osBuild: String?,
    val fingerprint: String?,
    val tags: String?,
    val brand: String?,
    val cpuAbis: Array<String>?
) {
    companion object {
        fun defaultInfo(): DeviceBuildInfo {
            @Suppress("DEPRECATION") val cpuABis = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> Build.SUPPORTED_ABIS
                else -> arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            }

            return DeviceBuildInfo(
                Build.MANUFACTURER,
                Build.MODEL,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.DISPLAY,
                Build.FINGERPRINT,
                Build.TAGS,
                Build.BRAND,
                cpuABis
            )
        }
    }
}
