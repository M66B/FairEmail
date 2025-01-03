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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.accounts.AccountsException;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.LineInputStream;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import inet.ipaddr.IPAddressString;

public class ConnectionHelper {
    static final int MAX_REDIRECTS = 5; // https://www.freesoft.org/CIE/RFC/1945/46.htm

    static final List<String> PREF_NETWORK = Collections.unmodifiableList(Arrays.asList(
            "metered", "roaming", "rlah", "require_validated", "require_validated_captive", "vpn_only" // update network state
    ));

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
            "SE" // Sweden
    ));

    static {
        System.loadLibrary("fairemail");
    }

    public static native int jni_socket_keep_alive(int fd, int seconds);

    public static native int jni_socket_get_send_buffer(int fd);

    public static native boolean jni_is_numeric_address(String _ip);

    static class NetworkState {
        private Boolean connected = null;
        private Boolean suitable = null;
        private Boolean unmetered = null;
        private Boolean roaming = null;
        private Network active = null;

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

        Network getActive() {
            return active;
        }

        public void update(NetworkState newState) {
            connected = newState.connected;
            suitable = newState.suitable;
            unmetered = newState.unmetered;
            roaming = newState.roaming;
            active = newState.active;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof NetworkState) {
                NetworkState other = (NetworkState) obj;
                return (Objects.equals(this.connected, other.connected) &&
                        Objects.equals(this.suitable, other.suitable) &&
                        Objects.equals(this.unmetered, other.unmetered) &&
                        Objects.equals(this.roaming, other.roaming) &&
                        Objects.equals(this.active, other.active));
            } else
                return false;
        }

        @Override
        public String toString() {
            return "connected=" + connected +
                    " suitable=" + suitable +
                    " unmetered=" + unmetered +
                    " roaming=" + roaming +
                    " active=" + active;
        }
    }

    static boolean isConnected(Context context, Network network) {
        NetworkInfo ni = getNetworkInfo(context, network);
        return (ni != null && ni.isConnected());
    }

    static NetworkInfo getNetworkInfo(Context context, Network network) {
        try {
            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            return (cm == null ? null : cm.getNetworkInfo(network));
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
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
            state.active = getActiveNetwork(context);

            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);

            if (state.connected && !roaming) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    NetworkInfo ani = (cm == null ? null : cm.getActiveNetworkInfo());
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
                        TelephonyManager tm = Helper.getSystemService(context, TelephonyManager.class);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean standalone_vpn = prefs.getBoolean("standalone_vpn", false);
        boolean require_validated = prefs.getBoolean("require_validated", false);
        boolean require_validated_captive = prefs.getBoolean("require_validated_captive", true);
        boolean vpn_only = prefs.getBoolean("vpn_only", false);

        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
        if (cm == null) {
            Log.i("isMetered: no connectivity manager");
            return null;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            NetworkInfo ani = cm.getActiveNetworkInfo();
            if (ani == null || !ani.isConnected())
                return null;
            if (vpn_only && !vpnActive(context))
                return null;
            return cm.isActiveNetworkMetered();
        }

        Network active = cm.getActiveNetwork();
        if (active == null) {
            Log.i("isMetered: no active network");
            return null;
        }

        // onLost [... state: SUSPENDED/SUSPENDED ... available: true]
        // onLost [... state: DISCONNECTED/DISCONNECTED ... available: true]
        NetworkInfo ani = cm.getNetworkInfo(active);
        if (ani == null || ani.getState() != NetworkInfo.State.CONNECTED) {
            Log.i("isMetered: no/connected active info ani=" + ani);
            if (ani == null ||
                    ani.getState() != NetworkInfo.State.SUSPENDED ||
                    ani.getType() != ConnectivityManager.TYPE_VPN)
                return null;
        }

        NetworkCapabilities caps = cm.getNetworkCapabilities(active);
        if (caps == null) {
            Log.i("isMetered: active no caps");
            return null; // network unknown
        }

        Log.i("isMetered: active caps=" + caps);

        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
            // Active network is not a VPN

            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                Log.i("isMetered: no internet");
                return null;
            }

            boolean captive = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL);
            if ((require_validated || (require_validated_captive && captive)) &&
                    !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                Log.i("isMetered: not validated captive=" + captive);
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
        }

        if (vpn_only) {
            boolean vpn = vpnActive(context);
            Log.i("isMetered: VPN only vpn=" + vpn);
            if (!vpn)
                return null;
        }

        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
            // NET_CAPABILITY_NOT_METERED is unreliable on older Android versions
            boolean metered = cm.isActiveNetworkMetered();
            Log.i("isMetered: active not VPN metered=" + metered);
            return metered;
        }

        // Active network is a VPN

        Network[] networks = cm.getAllNetworks();
        if (standalone_vpn && networks != null && networks.length == 1) {
            // Internet not checked/validated
            // Used for USB/Ethernet internet connection
            boolean metered = cm.isActiveNetworkMetered();
            Log.i("isMetered: active VPN metered=" + metered);
            return metered;
        }

        // VPN: evaluate underlying networks

        boolean underlying = false;
        for (Network network : networks) {
            caps = cm.getNetworkCapabilities(network);
            if (caps == null) {
                Log.i("isMetered: no underlying caps");
                continue; // network unknown
            }

            Log.i("isMetered: underlying caps=" + caps);

            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                Log.i("isMetered: underlying VPN");
                continue;
            }

            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                Log.i("isMetered: underlying no internet");
                continue;
            }

            boolean captive = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL);
            if ((require_validated || (require_validated_captive && captive)) &&
                    !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                Log.i("isMetered: underlying not validated captive=" + captive);
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

            underlying = true;
            Log.i("isMetered: underlying is connected");

            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                Log.i("isMetered: underlying is unmetered");
                return false;
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

    static Network getActiveNetwork(Context context) {
        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
        if (cm == null)
            return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return cm.getActiveNetwork();

        NetworkInfo ani = cm.getActiveNetworkInfo();
        if (ani == null)
            return null;

        Network[] networks = cm.getAllNetworks();
        for (Network network : networks) {
            NetworkInfo ni = cm.getNetworkInfo(network);
            if (ni == null)
                continue;
            if (ni.getType() == ani.getType() &&
                    ni.getSubtype() == ani.getSubtype())
                return network;
        }

        return null;
    }

    static Boolean isPrivateDnsActive(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                return null;
            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            if (cm == null)
                return null;
            Network active = cm.getActiveNetwork();
            if (active == null)
                return null;
            LinkProperties props = cm.getLinkProperties(active);
            if (props == null)
                return null;
            return props.isPrivateDnsActive();
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static String getPrivateDnsServerName(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                return null;
            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            if (cm == null)
                return null;
            Network active = cm.getActiveNetwork();
            if (active == null)
                return null;
            LinkProperties props = cm.getLinkProperties(active);
            if (props == null)
                return null;
            return props.getPrivateDnsServerName();
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static boolean isIoError(Throwable ex) {
        if (ex instanceof MessagingException &&
                ex.getMessage() != null &&
                ex.getMessage().contains("Got bad greeting") &&
                ex.getMessage().contains("[EOF]"))
            return true;

        while (ex != null) {
            if (isMaxConnections(ex.getMessage()) ||
                    ex instanceof IOException ||
                    ex instanceof FolderClosedIOException ||
                    ex instanceof ConnectionException ||
                    ex instanceof AccountsException ||
                    ex instanceof InterruptedException ||
                    "EOF on socket".equals(ex.getMessage()) ||
                    "Read timed out".equals(ex.getMessage()) || // POP3
                    "failed to connect".equals(ex.getMessage()))
                return true;
            ex = ex.getCause();
        }

        return false;
    }

    static boolean isAborted(Throwable ex) {
        while (ex != null) {
            String msg = ex.getMessage();
            if (msg != null &&
                    (msg.contains("Connection reset by peer") ||
                            msg.contains("Connection closed by peer")))
                return true;
            ex = ex.getCause();
        }

        return false;
    }

    static boolean isMaxConnections(Throwable ex) {
        while (ex != null) {
            if (isMaxConnections(ex.getMessage()))
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
                        message.contains("User is authenticated but not connected") /* Outlook */ ||
                        message.contains("Account is temporarily unavailable") /* Arcor.de / TalkTalk.net */ ||
                        message.contains("Connection dropped by server?")));
    }

    static Boolean isSyntacticallyInvalid(Throwable ex) {
        if (ex.getMessage() == null)
            return false;
        // 501 HELO requires valid address
        // 501 Syntactically invalid HELO argument(s)
        String message = ex.getMessage().toLowerCase(Locale.ROOT);
        return message.contains("syntactically invalid") ||
                message.contains("requires valid address");
    }

    static boolean isDataSaving(Context context) {
        // https://developer.android.com/training/basics/network-ops/data-saver.html
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return false;

        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
        if (cm == null)
            return false;

        // RESTRICT_BACKGROUND_STATUS_DISABLED: Data Saver is disabled.
        // RESTRICT_BACKGROUND_STATUS_ENABLED: The user has enabled Data Saver for this app. (Globally)
        // RESTRICT_BACKGROUND_STATUS_WHITELISTED: The user has enabled Data Saver but the app is allowed to bypass it.
        int status = cm.getRestrictBackgroundStatus();
        return (status == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED);
    }

    static String getDataSaving(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            return null;

        try {
            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            if (cm == null)
                return null;

            int status = cm.getRestrictBackgroundStatus();
            switch (status) {
                case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED:
                    return "disabled";
                case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED:
                    return "enabled";
                case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED:
                    return "whitelisted";
                default:
                    return Integer.toString(status);
            }
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static boolean vpnActive(Context context) {
        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
        if (cm == null)
            return false;

        // The active network doesn't necessarily have VPN transport
        //   active=... caps=[ Transports: WIFI Capabilities: ...&NOT_VPN&...
        //   network=... caps=[ Transports: WIFI|VPN Capabilities: ...

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
        try {
            return (Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static InetAddress from6to4(InetAddress addr) {
        // https://en.wikipedia.org/wiki/6to4
        if (addr instanceof Inet6Address) {
            byte[] octets = ((Inet6Address) addr).getAddress();
            if (octets[0] == 0x20 && octets[1] == 0x02)
                try {
                    return Inet4Address.getByAddress(Arrays.copyOfRange(octets, 2, 6));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
        }
        return addr;
    }

    static boolean isNumericAddress(String host) {
        // IPv4-mapped IPv6 can be 45 characters
        if (host == null || host.length() > 64)
            return false;
        return ConnectionHelper.jni_is_numeric_address(host);
    }

    static boolean isLocalAddress(String host) {
        try {
            InetAddress addr = ConnectionHelper.from6to4(InetAddress.getByName(host));
            return (addr.isLoopbackAddress() ||
                    addr.isSiteLocalAddress() ||
                    addr.isLinkLocalAddress());
        } catch (UnknownHostException ex) {
            Log.e(ex);
            return false;
        }
    }

    static boolean inSubnet(final String ip, final String net, final int prefix) {
        try {
            return new IPAddressString(net + "/" + prefix).getAddress()
                    .contains(new IPAddressString(ip).getAddress());
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    static boolean[] has46(Context context) {
        boolean has4 = false;
        boolean has6 = false;

        // props={
        //   InterfaceName: rmnet16
        //   LinkAddresses: [ 2a01:599:a1b:a486:9aa1:495d:81d9:5386/64 ]
        //   DnsAddresses: [ /2a01:598:7ff:0:10:74:210:221,/2a01:598:7ff:0:10:74:210:222 ]
        //   Domains: null
        //   MTU: 1500
        //   TcpBufferSizes: 2097152,6291456,16777216,512000,2097152,8388608
        //   Routes: [ ::/0 -> :: rmnet16 mtu 1500,2a01:599:a1b:a486::/64 -> :: rmnet16 mtu 0 ]
        //   Nat64Prefix: 64:ff9b::/96
        //   Stacked: [[ {
        //       InterfaceName: v4-rmnet16
        //       LinkAddresses: [ 192.0.0.4/32 ]
        //       DnsAddresses: [ ]
        //       Domains: null
        //       MTU: 0
        //       Routes: [ 0.0.0.0/0 -> 192.0.0.4 v4-rmnet16 mtu 0 ]
        //   } ]]
        // }

/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
                Network active = (cm == null ? null : cm.getActiveNetwork());
                LinkProperties props = (active == null ? null : cm.getLinkProperties(active));
                String ifacename = (props == null ? null : props.getInterfaceName());
                List<LinkAddress> las = (props == null ? null : props.getLinkAddresses());
                if (las != null)
                    for (LinkAddress la : las) {
                        InetAddress addr = la.getAddress();
                        boolean local = (addr.isLoopbackAddress() || addr.isLinkLocalAddress());
                        EntityLog.log(context, EntityLog.Type.Network,
                                "Link addr=" + addr + " local=" + local + " interface=" + ifacename);
                        if (local)
                            continue;
                        if (addr instanceof Inet4Address)
                            has4 = true;
                        else if (addr instanceof Inet6Address)
                            has6 = true;
                    }
            } catch (Throwable ex) {
                Log.e(ex);
            }
            return new boolean[]{has4, has6};
        }
*/
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni != null && ni.isUp())
                    for (InterfaceAddress iaddr : ni.getInterfaceAddresses()) {
                        InetAddress addr = iaddr.getAddress();
                        boolean local = (addr.isLoopbackAddress() || addr.isLinkLocalAddress());
                        EntityLog.log(context, EntityLog.Type.Network,
                                "Interface=" + ni + " addr=" + addr + " local=" + local);
                        if (!local)
                            if (addr instanceof Inet4Address)
                                has4 = true;
                            else if (addr instanceof Inet6Address)
                                has6 = true;
                    }
            }
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.NullPointerException: Attempt to read from field 'java.util.List java.net.NetworkInterface.childs' on a null object reference
                    at java.net.NetworkInterface.getAll(NetworkInterface.java:498)
                    at java.net.NetworkInterface.getNetworkInterfaces(NetworkInterface.java:398)
             */
        }

        return new boolean[]{has4, has6};
    }

    static List<String> getCommonNames(Context context, String domain, int port, int timeout) throws IOException {
        List<String> result = new ArrayList<>();
        InetSocketAddress address = new InetSocketAddress(domain, port);
        SocketFactory factory = SSLSocketFactory.getDefault();
        try (SSLSocket sslSocket = (SSLSocket) factory.createSocket()) {
            EntityLog.log(context, EntityLog.Type.Network, "Connecting to " + address);
            sslSocket.connect(address, timeout);
            EntityLog.log(context, EntityLog.Type.Network, "Connected " + address);

            sslSocket.setSoTimeout(timeout);
            sslSocket.startHandshake();

            Certificate[] certs = sslSocket.getSession().getPeerCertificates();
            for (Certificate cert : certs)
                if (cert instanceof X509Certificate) {
                    try {
                        result.addAll(EntityCertificate.getDnsNames((X509Certificate) cert));
                    } catch (CertificateParsingException ex) {
                        Log.w(ex);
                    }
                }
        }
        return result;
    }

    static void setUserAgent(Context context, HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));

        if (BuildConfig.DEBUG) {
            // https://web.dev/migrate-to-ua-ch/
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean generic_ua = prefs.getBoolean("generic_ua", false);

            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA
            connection.setRequestProperty("Sec-CH-UA", "\"Chromium\""); // No WebView API yet
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA-Mobile
            connection.setRequestProperty("Sec-CH-UA-Mobile", "?1");
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA-Platform
            connection.setRequestProperty("Sec-CH-UA-Platform", "\"Android\"");

            if (!generic_ua) {
                String release = Build.VERSION.RELEASE;
                if (release == null)
                    release = "";
                release = release.replace("\"", "'");

                String model = Build.MODEL;
                if (model == null)
                    model = "";
                model = model.replace("\"", "'");

                // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA-Platform-Version
                connection.setRequestProperty("Sec-CH-UA-Platform-Version", "\"" + release + "\"");
                // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA-Model
                connection.setRequestProperty("Sec-CH-UA-Model", "\"" + model + "\"");
            }
        }
    }

    static HttpURLConnection openConnectionUnsafe(Context context, String source, int ctimeout, int rtimeout) throws IOException {
        return openConnectionUnsafe(context, new URL(source), ctimeout, rtimeout);
    }

    static HttpURLConnection openConnectionUnsafe(Context context, URL url, int ctimeout, int rtimeout) throws IOException {
        // https://support.google.com/faqs/answer/7188426
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean open_safe = prefs.getBoolean("open_safe", false);
        boolean http_redirect = prefs.getBoolean("http_redirect", true);

        int redirects = 0;
        while (true) {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setReadTimeout(rtimeout);
            urlConnection.setConnectTimeout(ctimeout);
            urlConnection.setInstanceFollowRedirects(true);

            if (urlConnection instanceof HttpsURLConnection) {
                if (!open_safe)
                    ((HttpsURLConnection) urlConnection).setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
            } else {
                if (open_safe)
                    throw new IOException("https required url=" + url);
            }

            ConnectionHelper.setUserAgent(context, urlConnection);
            urlConnection.connect();

            try {
                int status = urlConnection.getResponseCode();

                if (http_redirect &&
                        (status == HttpURLConnection.HTTP_MOVED_PERM ||
                                status == HttpURLConnection.HTTP_MOVED_TEMP ||
                                status == HttpURLConnection.HTTP_SEE_OTHER ||
                                status == 307 /* Temporary redirect */ ||
                                status == 308 /* Permanent redirect */)) {
                    if (++redirects > MAX_REDIRECTS)
                        throw new IOException("Too many redirects");

                    String header = urlConnection.getHeaderField("Location");
                    if (header == null)
                        throw new IOException("Location header missing");

                    String location = URLDecoder.decode(header, StandardCharsets.UTF_8.name());
                    url = new URL(url, location);
                    Log.i("Redirect #" + redirects + " to " + url);

                    urlConnection.disconnect();
                    continue;
                }

                if (status == HttpURLConnection.HTTP_NOT_FOUND)
                    throw new FileNotFoundException("Error " + status + ": " + urlConnection.getResponseMessage());
                if (status != HttpURLConnection.HTTP_OK)
                    throw new IOException("Error " + status + ": " + urlConnection.getResponseMessage());

                return urlConnection;
            } catch (IOException ex) {
                urlConnection.disconnect();
                throw ex;
            }
        }
    }

    static Integer getLinkDownstreamBandwidthKbps(Context context) {
        // 2G GSM ~14.4 Kbps
        // G GPRS ~26.8 Kbps
        // E EDGE ~108.8 Kbps
        // 3G UMTS ~128 Kbps
        // H HSPA ~3.6 Mbps
        // H+ HSPA+ ~14.4 Mbps-23.0 Mbps
        // 4G LTE ~50 Mbps
        // 4G LTE-A ~500 Mbps
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return null;
        try {
            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            if (cm == null)
                return null;
            Network active = cm.getActiveNetwork();
            if (active == null)
                return null;
            NetworkCapabilities caps = cm.getNetworkCapabilities(active);
            if (caps == null)
                return null;
            return caps.getLinkDownstreamBandwidthKbps();
        } catch (Throwable ex) {
            Log.w(ex);
            return null;
        }
    }

    static SSLSocket starttls(Socket socket, String host, int port, Context context) throws IOException {
        String response;
        String command;
        boolean has = false;

        LineInputStream lis =
                new LineInputStream(
                        new BufferedInputStream(
                                socket.getInputStream()));

        if (port == 587) {
            do {
                response = lis.readLine();
                if (response != null)
                    EntityLog.log(context, EntityLog.Type.Protocol,
                            socket.getRemoteSocketAddress() + " <" + response);
            } while (response != null && !response.startsWith("220 "));

            command = "EHLO " + EmailService.getDefaultEhlo() + "\n";
            EntityLog.log(context, socket.getRemoteSocketAddress() + " >" + command);
            socket.getOutputStream().write(command.getBytes());

            do {
                response = lis.readLine();
                if (response != null) {
                    EntityLog.log(context, EntityLog.Type.Protocol,
                            socket.getRemoteSocketAddress() + " <" + response);
                    if (response.contains("STARTTLS"))
                        has = true;
                }
            } while (response != null &&
                    response.length() >= 4 && response.charAt(3) == '-');

            if (has) {
                command = "STARTTLS\n";
                EntityLog.log(context, EntityLog.Type.Protocol,
                        socket.getRemoteSocketAddress() + " >" + command);
                socket.getOutputStream().write(command.getBytes());
            }
        } else if (port == 143) {
            do {
                response = lis.readLine();
                if (response != null) {
                    EntityLog.log(context, EntityLog.Type.Protocol,
                            socket.getRemoteSocketAddress() + " <" + response);
                    if (response.contains("STARTTLS"))
                        has = true;
                }
            } while (response != null &&
                    !response.startsWith("* OK"));

            if (has) {
                command = "A001 STARTTLS\n";
                EntityLog.log(context, EntityLog.Type.Protocol,
                        socket.getRemoteSocketAddress() + " >" + command);
                socket.getOutputStream().write(command.getBytes());
            }
        }

        if (has) {
            do {
                response = lis.readLine();
                if (response != null)
                    EntityLog.log(context, EntityLog.Type.Protocol,
                            socket.getRemoteSocketAddress() + " <" + response);
            } while (response != null &&
                    !(response.startsWith("A001 OK") || response.startsWith("220 ")));

            SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            return (SSLSocket) sslFactory.createSocket(socket, host, port, false);
        } else
            throw new SocketException("No STARTTLS");
    }

    static void signOff(Socket socket, int port, Context context) {
        try {
            String command = (port == 465 || port == 587 ? "QUIT" : "A002 LOGOUT");

            EntityLog.log(context, EntityLog.Type.Protocol,
                    socket.getRemoteSocketAddress() + " >" + command);
            socket.getOutputStream().write((command + "\n").getBytes());

            LineInputStream lis =
                    new LineInputStream(
                            new BufferedInputStream(
                                    socket.getInputStream()));
            String response;
            do {
                response = lis.readLine();
                if (response != null)
                    EntityLog.log(context, EntityLog.Type.Protocol,
                            socket.getRemoteSocketAddress() + " <" + response);
            } while (response != null);
        } catch (IOException ex) {
            Log.w(ex);
        }
    }

    static void setupProxy(Context context) {
        if (!BuildConfig.DEBUG)
            return;

        // https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                // new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("", 0));
                Log.i("PROXY uri=" + uri);
                return Arrays.asList(Proxy.NO_PROXY);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ex) {
                Log.e("PROXY uri=" + uri + " sa=" + sa, ex);
            }
        });
    }

    public static Socket getSocket(String host, int port) {
        if (BuildConfig.DEBUG) {
            Proxy proxy = ProxySelector.getDefault().select(URI.create("socket://" + host + ":" + port)).get(0);
            return new Socket(proxy);
        } else
            return new Socket();
    }
}
