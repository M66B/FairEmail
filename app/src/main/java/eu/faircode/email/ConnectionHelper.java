package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.accounts.AccountsException;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.ConnectionException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConnectionHelper {
    // Roam like at home
    // https://en.wikipedia.org/wiki/European_Union_roaming_regulations
    private static final List<String> RLAH_COUNTRY_CODES = Collections.unmodifiableList(Arrays.asList(
            "AT", // Austria
            "BE", // Belgium
            "BG", // Bulgaria
            "HR", // Croatia
            "CY", // Cyprus
            "CZ", // Czech Republic
            "DK", // Denmark
            "EE", // Estonia
            "FI", // Finland
            "FR", // France
            "DE", // Germany
            "GR", // Greece
            "HU", // Hungary
            "IS", // Iceland
            "IE", // Ireland
            "IT", // Italy
            "LV", // Latvia
            "LI", // Liechtenstein
            "LT", // Lithuania
            "LU", // Luxembourg
            "MT", // Malta
            "NL", // Netherlands
            "NO", // Norway
            "PL", // Poland
            "PT", // Portugal
            "RO", // Romania
            "SK", // Slovakia
            "SI", // Slovenia
            "ES", // Spain
            "SE", // Sweden
            "GB" // United Kingdom
    ));

    static class NetworkState {
        private Boolean connected = null;
        private Boolean suitable = null;
        private Boolean unmetered = null;
        private Boolean roaming = null;
        private Integer type = null;

        boolean isConnected() {
            return (connected != null && connected);
        }

        boolean isSuitable() {
            return (suitable != null && suitable);
        }

        boolean isUnmetered() {
            return (unmetered != null && unmetered);
        }

        boolean isRoaming() {
            return (roaming != null && roaming);
        }

        Integer getType() {
            return type;
        }

        public void update(NetworkState newState) {
            connected = newState.connected;
            unmetered = newState.unmetered;
            suitable = newState.suitable;
            roaming = newState.roaming;
            type = newState.type;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof NetworkState) {
                NetworkState other = (NetworkState) obj;
                return (Objects.equals(this.connected, other.connected) &&
                        Objects.equals(this.suitable, other.suitable) &&
                        Objects.equals(this.unmetered, other.unmetered) &&
                        Objects.equals(this.roaming, other.roaming));
            } else
                return false;
        }
    }

    static NetworkState getNetworkState(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean metered = prefs.getBoolean("metered", true);
        boolean roaming = prefs.getBoolean("roaming", true);
        boolean rlah = prefs.getBoolean("rlah", true);

        NetworkState state = new NetworkState();
        try {
            Boolean isMetered = isMetered(context);
            state.connected = (isMetered != null);
            state.unmetered = (isMetered != null && !isMetered);
            state.suitable = (isMetered != null && (metered || !isMetered));

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ani = (cm == null ? null : cm.getActiveNetworkInfo());
            if (ani != null)
                state.type = ani.getType();

            if (state.connected && !roaming) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    if (ani != null)
                        state.roaming = ani.isRoaming();
                } else {
                    Network active = (cm == null ? null : cm.getActiveNetwork());
                    if (active != null) {
                        NetworkCapabilities caps = cm.getNetworkCapabilities(active);
                        if (caps != null)
                            state.roaming = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
                    }
                }

                if (state.roaming != null && state.roaming && rlah)
                    try {
                        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (tm != null) {
                            String sim = tm.getSimCountryIso();
                            String network = tm.getNetworkCountryIso();
                            Log.i("Country SIM=" + sim + " network=" + network);
                            if (sim != null && network != null &&
                                    RLAH_COUNTRY_CODES.contains(sim.toUpperCase()) &&
                                    RLAH_COUNTRY_CODES.contains(network.toUpperCase()))
                                state.roaming = false;
                        }
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return state;
    }

    private static Boolean isMetered(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return null;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            NetworkInfo ani = cm.getActiveNetworkInfo();
            if (ani == null || !ani.isConnected())
                return null;
            return cm.isActiveNetworkMetered();
        }

        Network active = cm.getActiveNetwork();
        if (active == null) {
            Log.i("isMetered: no active network");
            return null;
        }

        // onLost [... state: DISCONNECTED/DISCONNECTED ... available: true]
        NetworkInfo ani = cm.getNetworkInfo(active);
        if (ani == null || !ani.isConnected())
            return null;

        NetworkCapabilities caps = cm.getNetworkCapabilities(active);
        if (caps == null) {
            Log.i("isMetered: active no caps");
            return null; // network unknown
        }

        Log.i("isMetered: active caps=" + caps);

        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) &&
                !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            Log.i("isMetered: no internet");
            return null;
        }

        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
            Log.i("isMetered: active restricted");
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)) {
            Log.i("isMetered: active background");
            return null;
        }

        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
            // NET_CAPABILITY_NOT_METERED is unreliable on older Android versions
            boolean metered = cm.isActiveNetworkMetered();
            Log.i("isMetered: active not VPN metered=" + metered);
            return metered;
        }

        // VPN: evaluate underlying networks

        boolean underlying = false;
        Network[] networks = cm.getAllNetworks();
        for (Network network : networks) {
            caps = cm.getNetworkCapabilities(network);
            if (caps == null) {
                Log.i("isMetered: no underlying caps");
                continue; // network unknown
            }

            Log.i("isMetered: underlying caps=" + caps);

            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                Log.i("isMetered: underlying no internet");
                continue;
            }

            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                Log.i("isMetered: underlying restricted");
                continue;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                    !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)) {
                Log.i("isMetered: underlying background");
                continue;
            }

            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                underlying = true;
                Log.i("isMetered: underlying is connected");

                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                    Log.i("isMetered: underlying is unmetered");
                    return false;
                }
            }
        }

        if (!underlying) {
            // VPN-only network via USB is possible
            boolean metered = cm.isActiveNetworkMetered();
            Log.i("isMetered: no underlying network metered=" + metered);
            return metered;
        }

        // Assume metered
        Log.i("isMetered: underlying assume metered");
        return true;
    }

    static boolean isIoError(Throwable ex) {
        while (ex != null) {
            if (isMaxConnections(ex.getMessage()) ||
                    ex instanceof IOException ||
                    ex instanceof ConnectionException ||
                    ex instanceof AccountsException ||
                    "failed to connect".equals(ex.getMessage()))
                return true;
            ex = ex.getCause();
        }
        return false;
    }

    static boolean isMaxConnections(String message) {
        return (message != null &&
                (message.contains("Too many simultaneous connections") /* Gmail */ ||
                        message.contains("Maximum number of connections") /* ... from user+IP exceeded */ /* Dovecot */ ||
                        message.contains("Too many concurrent connections") /* ... to this mailbox */ ||
                        message.contains("User is authenticated but not connected") /* Outlook */));
    }

    static Boolean isSyntacticallyInvalid(Throwable ex) {
        if (ex.getMessage() == null)
            return false;
        return ex.getMessage().toLowerCase().contains("syntactically invalid");
    }

    static boolean vpnActive(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;

        try {
            for (Network network : cm.getAllNetworks()) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    return true;
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return false;
    }

    static boolean airplaneMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}
