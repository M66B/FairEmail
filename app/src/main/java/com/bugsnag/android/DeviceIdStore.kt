package com.bugsnag.android

import android.content.Context
import com.bugsnag.android.internal.ImmutableConfig
import com.bugsnag.android.internal.dag.Provider
import java.io.File
import java.util.UUID

/**
 * This class is responsible for persisting and retrieving the device ID and internal device ID,
 * which uniquely identify this device in various contexts.
 */
internal class DeviceIdStore @JvmOverloads @Suppress("LongParameterList") constructor(
    context: Context,
    private val deviceIdFile: File = File(context.filesDir, "device-id"),
    private val deviceIdGenerator: () -> UUID = { UUID.randomUUID() },
    private val internalDeviceIdFile: File = File(context.filesDir, "internal-device-id"),
    private val internalDeviceIdGenerator: () -> UUID = { UUID.randomUUID() },
    private val sharedPrefMigrator: Provider<SharedPrefMigrator>,
    config: ImmutableConfig,
    private val logger: Logger
) {

    private lateinit var persistence: DeviceIdPersistence
    private lateinit var internalPersistence: DeviceIdPersistence
    private val generateId = config.generateAnonymousId
    private var deviceIds: DeviceIds? = null

    /**
     * Loads the device ID from
     * Loads the device ID from its file system location. Device IDs are UUIDs which are
     * persisted on a per-install basis. This method is thread-safe and multi-process safe.
     *
     * If no device ID exists then the legacy value stored in [SharedPreferences] will
     * be used. If no value is present then a random UUID will be generated and persisted.
     */
    private fun loadDeviceId(): String? {
        // If generateAnonymousId = false, return null
        // so that a previously persisted device ID is not returned,
        // or a new one is not generated and persisted
        if (!generateId) {
            return null
        }
        var result = persistence.loadDeviceId(false)
        if (result != null) {
            return result
        }
        result = sharedPrefMigrator.get().loadDeviceId(false)
        if (result != null) {
            return result
        }
        return persistence.loadDeviceId(true)
    }

    private fun loadInternalDeviceId(): String? {
        // If generateAnonymousId = false, return null
        // so that a previously persisted device ID is not returned,
        // or a new one is not generated and persisted
        if (!generateId) {
            return null
        }
        return internalPersistence.loadDeviceId(true)
    }

    fun load(): DeviceIds? {
        if (deviceIds != null) {
            return deviceIds
        }

        persistence = DeviceIdFilePersistence(deviceIdFile, deviceIdGenerator, logger)
        internalPersistence =
            DeviceIdFilePersistence(internalDeviceIdFile, internalDeviceIdGenerator, logger)

        val deviceId = loadDeviceId()
        val internalDeviceId = loadInternalDeviceId()

        if (deviceId != null || internalDeviceId != null) {
            deviceIds = DeviceIds(deviceId, internalDeviceId)
        }

        return deviceIds
    }

    data class DeviceIds(
        val deviceId: String?,
        val internalDeviceId: String?
    )
}
