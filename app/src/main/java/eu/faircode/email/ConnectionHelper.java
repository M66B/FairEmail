package eu.faircode.email;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.preference.PreferenceManager;

import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.sun.mail.imap.IMAPStore;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

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

    static final int AUTH_TYPE_PASSWORD = 1;
    static final int AUTH_TYPE_GMAIL = 2;

    static class NetworkState {
        private Boolean connected = null;
        private Boolean suitable = null;
        private Boolean unmetered = null;
        private Boolean roaming = null;

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

        public void update(NetworkState newState) {
            connected = newState.connected;
            unmetered = newState.unmetered;
            suitable = newState.suitable;
        }
    }

    static NetworkState getNetworkState(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean metered = prefs.getBoolean("metered", true);
        boolean rlah = prefs.getBoolean("rlah", true);
        boolean roaming = prefs.getBoolean("roaming", true);

        NetworkState state = new NetworkState();
        Boolean isMetered = isMetered(context);
        state.connected = (isMetered != null);
        state.unmetered = (isMetered != null && !isMetered);
        state.suitable = (isMetered != null && (metered || !isMetered));

        if (state.connected && !roaming) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                NetworkInfo ani = cm.getActiveNetworkInfo();
                if (ani != null)
                    state.roaming = ani.isRoaming();
            } else {
                Network active = cm.getActiveNetwork();
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
                                RLAH_COUNTRY_CODES.contains(sim) &&
                                RLAH_COUNTRY_CODES.contains(network))
                            state.roaming = false;
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
                }
        }

        return state;
    }

    private static Boolean isMetered(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
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
            boolean unmetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
            Log.i("isMetered: active not VPN unmetered=" + unmetered);
            return !unmetered;
        }

        // VPN: evaluate underlying networks

        boolean underlying = false;
        Network[] networks = cm.getAllNetworks();
        if (networks != null)
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
            Log.i("isMetered: no underlying network");
            return null;
        }

        // Assume metered
        Log.i("isMetered: underlying assume metered");
        return true;
    }

    static void connect(Context context, IMAPStore istore, EntityAccount account)
            throws MessagingException, AuthenticatorException, OperationCanceledException, IOException {
        try {
            istore.connect(account.host, account.port, account.user, account.password);
        } catch (AuthenticationFailedException ex) {
            if (account.auth_type == AUTH_TYPE_GMAIL) {
                account.password = refreshToken(context, "com.google", account.user, account.password);
                DB.getInstance(context).account().setAccountPassword(account.id, account.password);
                istore.connect(account.host, account.port, account.user, account.password);
            } else
                throw ex;
        }

        // https://www.ietf.org/rfc/rfc2971.txt
        if (istore.hasCapability("ID"))
            try {
                Map<String, String> id = new LinkedHashMap<>();
                id.put("name", context.getString(R.string.app_name));
                id.put("version", BuildConfig.VERSION_NAME);
                Map<String, String> sid = istore.id(id);
                if (sid != null) {
                    Map<String, String> crumb = new HashMap<>();
                    for (String key : sid.keySet()) {
                        crumb.put(key, sid.get(key));
                        Log.i("Server " + key + "=" + sid.get(key));
                    }
                    Bugsnag.leaveBreadcrumb("server", BreadcrumbType.LOG, crumb);
                }
            } catch (MessagingException ex) {
                Log.w(ex);
            }
    }

    static String refreshToken(Context context, String type, String name, String current)
            throws AuthenticatorException, OperationCanceledException, IOException {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(type);
        for (Account account : accounts)
            if (name.equals(account.name)) {
                Log.i("Refreshing token");
                am.invalidateAuthToken(type, current);
                String refreshed = am.blockingGetAuthToken(account, getAuthTokenType(type), true);
                if (refreshed == null)
                    throw new OperationCanceledException("no token");
                Log.i("Refreshed token");
                return refreshed;
            }
        return current;
    }

    static String getAuthTokenType(String type) {
        if ("com.google".equals(type))
            return "oauth2:https://mail.google.com/";
        return null;
    }

    static boolean airplaneMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}
