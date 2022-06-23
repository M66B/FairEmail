package com.bugsnag.android

import android.annotation.SuppressLint
import android.content.Context

/**
 * Reads legacy information left in SharedPreferences and migrates it to the new location.
 */
internal class SharedPrefMigrator(context: Context) : DeviceIdPersistence {

    private val prefs = context
        .getSharedPreferences("com.bugsnag.android", Context.MODE_PRIVATE)

    /**
     * This implementation will never create an ID; it will only fetch one if present.
     */
    override fun loadDeviceId(requestCreateIfDoesNotExist: Boolean) = prefs.getString(INSTALL_ID_KEY, null)

    fun loadUser(deviceId: String?) = User(
        prefs.getString(USER_ID_KEY, deviceId),
        prefs.getString(USER_EMAIL_KEY, null),
        prefs.getString(USER_NAME_KEY, null)
    )

    fun hasPrefs() = prefs.contains(INSTALL_ID_KEY)

    @SuppressLint("ApplySharedPref")
    fun deleteLegacyPrefs() {
        if (hasPrefs()) {
            prefs.edit().clear().commit()
        }
    }

    companion object {
        private const val INSTALL_ID_KEY = "install.iud"
        private const val USER_ID_KEY = "user.id"
        private const val USER_NAME_KEY = "user.name"
        private const val USER_EMAIL_KEY = "user.email"
    }
}
