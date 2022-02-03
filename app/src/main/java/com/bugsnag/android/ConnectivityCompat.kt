package com.bugsnag.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.bugsnag.android.UnknownConnectivity.retrieveNetworkAccessState
import java.util.concurrent.atomic.AtomicBoolean

internal typealias NetworkChangeCallback = (hasConnection: Boolean, networkState: String) -> Unit

internal interface Connectivity {
    fun registerForNetworkChanges()
    fun unregisterForNetworkChanges()
    fun hasNetworkConnection(): Boolean
    fun retrieveNetworkAccessState(): String
}

internal class ConnectivityCompat(
    context: Context,
    callback: NetworkChangeCallback?
) : Connectivity {

    private val cm = context.getConnectivityManager()

    private val connectivity: Connectivity =
        when {
            cm == null -> UnknownConnectivity
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> ConnectivityApi24(cm, callback)
            else -> ConnectivityLegacy(context, cm, callback)
        }

    override fun registerForNetworkChanges() {
        runCatching { connectivity.registerForNetworkChanges() }
    }

    override fun hasNetworkConnection(): Boolean {
        val result = runCatching { connectivity.hasNetworkConnection() }
        return result.getOrElse { true } // allow network requests to be made if state unknown
    }

    override fun unregisterForNetworkChanges() {
        runCatching { connectivity.unregisterForNetworkChanges() }
    }

    override fun retrieveNetworkAccessState(): String {
        val result = runCatching { connectivity.retrieveNetworkAccessState() }
        return result.getOrElse { "unknown" }
    }
}

@Suppress("DEPRECATION")
internal class ConnectivityLegacy(
    private val context: Context,
    private val cm: ConnectivityManager,
    callback: NetworkChangeCallback?
) : Connectivity {

    private val changeReceiver = ConnectivityChangeReceiver(callback)

    private val activeNetworkInfo: android.net.NetworkInfo?
        get() = try {
            cm.activeNetworkInfo
        } catch (e: NullPointerException) {
            // in some rare cases we get a remote NullPointerException via Parcel.readException
            null
        }

    override fun registerForNetworkChanges() {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiverSafe(changeReceiver, intentFilter)
    }

    override fun unregisterForNetworkChanges() = context.unregisterReceiverSafe(changeReceiver)

    override fun hasNetworkConnection(): Boolean {
        return activeNetworkInfo?.isConnectedOrConnecting ?: false
    }

    override fun retrieveNetworkAccessState(): String {
        return when (activeNetworkInfo?.type) {
            null -> "none"
            ConnectivityManager.TYPE_WIFI -> "wifi"
            ConnectivityManager.TYPE_ETHERNET -> "ethernet"
            else -> "cellular" // all other types are cellular in some form
        }
    }

    private inner class ConnectivityChangeReceiver(
        private val cb: NetworkChangeCallback?
    ) : BroadcastReceiver() {

        private val receivedFirstCallback = AtomicBoolean(false)

        override fun onReceive(context: Context, intent: Intent) {
            if (receivedFirstCallback.getAndSet(true)) {
                cb?.invoke(hasNetworkConnection(), retrieveNetworkAccessState())
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
internal class ConnectivityApi24(
    private val cm: ConnectivityManager,
    callback: NetworkChangeCallback?
) : Connectivity {

    private val networkCallback = ConnectivityTrackerCallback(callback)

    override fun registerForNetworkChanges() = cm.registerDefaultNetworkCallback(networkCallback)
    override fun unregisterForNetworkChanges() = cm.unregisterNetworkCallback(networkCallback)
    override fun hasNetworkConnection() = cm.activeNetwork != null

    override fun retrieveNetworkAccessState(): String {
        val network = cm.activeNetwork
        val capabilities = if (network != null) cm.getNetworkCapabilities(network) else null

        return when {
            capabilities == null -> "none"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            else -> "unknown"
        }
    }

    @VisibleForTesting
    internal class ConnectivityTrackerCallback(
        private val cb: NetworkChangeCallback?
    ) : ConnectivityManager.NetworkCallback() {

        private val receivedFirstCallback = AtomicBoolean(false)

        override fun onUnavailable() {
            super.onUnavailable()
            invokeNetworkCallback(false)
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            invokeNetworkCallback(true)
        }

        /**
         * Invokes the network callback, as long as the ConnectivityManager callback has been
         * triggered at least once before (when setting a NetworkCallback Android always
         * invokes the callback with the current network state).
         */
        private fun invokeNetworkCallback(hasConnection: Boolean) {
            if (receivedFirstCallback.getAndSet(true)) {
                cb?.invoke(hasConnection, retrieveNetworkAccessState())
            }
        }
    }
}

/**
 * Connectivity used in cases where we cannot access the system ConnectivityManager.
 * We assume that there is some sort of network and do not attempt to report any network changes.
 */
internal object UnknownConnectivity : Connectivity {
    override fun registerForNetworkChanges() {}

    override fun unregisterForNetworkChanges() {}

    override fun hasNetworkConnection(): Boolean {
        return true
    }

    override fun retrieveNetworkAccessState(): String {
        return "unknown"
    }
}
