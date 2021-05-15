package com.bugsnag.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.RemoteException

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
