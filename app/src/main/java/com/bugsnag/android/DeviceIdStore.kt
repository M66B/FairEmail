package com.bugsnag.android

import android.content.Context
import com.bugsnag.android.internal.ImmutableConfig
import java.io.File
import java.util.UUID

/**
 * This class is responsible for persisting and retrieving the device ID and internal device ID,
 * which uniquely identify this device in various contexts.
 */
internal class DeviceIdStore @JvmOverloads @Suppress("LongParameterList") constructor(
    context: Context,
    deviceIdfile: File = File(context.filesDir, "device-id"),
    deviceIdGenerator: () -> UUID = { UUID.randomUUID() },
    internalDeviceIdfile: File = File(context.filesDir, "internal-device-id"),
    internalDeviceIdGenerator: () -> UUID = { UUID.randomUUID() },
    private val sharedPrefMigrator: SharedPrefMigrator,
    config: ImmutableConfig,
    logger: Logger
) {

    private val persistence: DeviceIdPersistence
    private val internalPersistence: DeviceIdPersistence
    private val generateId = config.generateAnonymousId

    init {
        persistence = DeviceIdFilePersistence(deviceIdfile, deviceIdGenerator, logger)
        internalPersistence = DeviceIdFilePersistence(internalDeviceIdfile, internalDeviceIdGenerator, logger)
    }

    /**
     * Loads the device ID from
     * Loads the device ID from its file system location. Device IDs are UUIDs which are
     * persisted on a per-install basis. This method is thread-safe and multi-process safe.
     *
     * If no device ID exists then the legacy value stored in [SharedPreferences] will
     * be used. If no value is present then a random UUID will be generated and persisted.
     */
    fun loadDeviceId(): String? {
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
        result = sharedPrefMigrator.loadDeviceId(false)
        if (result != null) {
            return result
        }
        return persistence.loadDeviceId(true)
    }

    fun loadInternalDeviceId(): String? {
        // If generateAnonymousId = false, return null
        // so that a previously persisted device ID is not returned,
        // or a new one is not generated and persisted
        if (!generateId) {
            return null
        }
        return internalPersistence.loadDeviceId(true)
    }
}
