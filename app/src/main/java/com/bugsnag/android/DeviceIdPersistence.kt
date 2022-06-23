package com.bugsnag.android

interface DeviceIdPersistence {
    /**
     * Loads the device ID from storage.
     *
     * Device IDs are UUIDs which are persisted on a per-install basis.
     *
     * This method must be thread-safe and multi-process safe.
     *
     * Note: requestCreateIfDoesNotExist is only a request; an implementation may still refuse to create a new ID.
     */
    fun loadDeviceId(requestCreateIfDoesNotExist: Boolean): String?
}
