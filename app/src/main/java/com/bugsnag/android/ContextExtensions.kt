package com.bugsnag.android

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.RemoteException
import android.os.storage.StorageManager
import java.lang.RuntimeException

/**
 * Calls [Context.registerReceiver] but swallows [SecurityException] and [RemoteException]
 * to avoid terminating the process in rare cases where the registration is unsuccessful.
 */
internal fun Context.registerReceiverSafe(
    receiver: BroadcastReceiver?,
    filter: IntentFilter?,
    logger: Logger? = null
): Intent? {
    try {
        return registerReceiver(receiver, filter)
    } catch (exc: SecurityException) {
        logger?.w("Failed to register receiver", exc)
    } catch (exc: RemoteException) {
        logger?.w("Failed to register receiver", exc)
    } catch (exc: IllegalArgumentException) {
        logger?.w("Failed to register receiver", exc)
    }
    return null
}

/**
 * Calls [Context.unregisterReceiver] but swallows [SecurityException] and [RemoteException]
 * to avoid terminating the process in rare cases where the registration is unsuccessful.
 */
internal fun Context.unregisterReceiverSafe(
    receiver: BroadcastReceiver?,
    logger: Logger? = null
) {
    try {
        unregisterReceiver(receiver)
    } catch (exc: SecurityException) {
        logger?.w("Failed to register receiver", exc)
    } catch (exc: RemoteException) {
        logger?.w("Failed to register receiver", exc)
    } catch (exc: IllegalArgumentException) {
        logger?.w("Failed to register receiver", exc)
    }
}

private inline fun <reified T> Context.safeGetSystemService(name: String): T? {
    return try {
        getSystemService(name) as? T
    } catch (exc: RuntimeException) {
        null
    }
}

@JvmName("getActivityManagerFrom")
internal fun Context.getActivityManager(): ActivityManager? =
    safeGetSystemService(Context.ACTIVITY_SERVICE)

@JvmName("getConnectivityManagerFrom")
internal fun Context.getConnectivityManager(): ConnectivityManager? =
    safeGetSystemService(Context.CONNECTIVITY_SERVICE)

@JvmName("getStorageManagerFrom")
internal fun Context.getStorageManager(): StorageManager? =
    safeGetSystemService(Context.STORAGE_SERVICE)

@JvmName("getLocationManager")
internal fun Context.getLocationManager(): LocationManager? =
    safeGetSystemService(Context.LOCATION_SERVICE)
