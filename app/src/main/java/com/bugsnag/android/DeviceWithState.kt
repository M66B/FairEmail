package com.bugsnag.android

import java.util.Date

/**
 * Stateful information set by the notifier about the device on which the event occurred can be
 * found on this class. These values can be accessed and amended if necessary.
 */
class DeviceWithState internal constructor(
    buildInfo: DeviceBuildInfo,
    jailbroken: Boolean?,
    id: String?,
    locale: String?,
    totalMemory: Long?,
    runtimeVersions: MutableMap<String, Any>,

    /**
     * The number of free bytes of storage available on the device
     */
    var freeDisk: Long?,

    /**
     * The number of free bytes of memory available on the device
     */
    var freeMemory: Long?,

    /**
     * The orientation of the device when the event occurred: either portrait or landscape
     */
    var orientation: String?,

    /**
     * The timestamp on the device when the event occurred
     */
    var time: Date?
) : Device(buildInfo, buildInfo.cpuAbis, jailbroken, id, locale, totalMemory, runtimeVersions) {

    override fun serializeFields(writer: JsonStream) {
        super.serializeFields(writer)
        writer.name("freeDisk").value(freeDisk)
        writer.name("freeMemory").value(freeMemory)
        writer.name("orientation").value(orientation)

        if (time != null) {
            writer.name("time").value(time)
        }
    }
}
