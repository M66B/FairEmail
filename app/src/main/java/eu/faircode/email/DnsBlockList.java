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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import androidx.core.net.MailTo;
import androidx.preference.PreferenceManager;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

public class DnsBlockList {
    private static final List<BlockList> BLOCK_LISTS = Collections.unmodifiableList(Arrays.asList(
            // https://www.spamhaus.org/zen/
            new BlockList(true, "Spamhaus/zen", "zen.spamhaus.org", true, new String[]{
                    // https://www.spamhaus.org/faq/section/DNSBL%20Usage#200
                    "127.0.0.2", // SBL Spamhaus SBL Data
                    "127.0.0.3", // SBL Spamhaus SBL CSS Data
                    "127.0.0.4", // XBL CBL Data
                    "127.0.0.9", // SBL Spamhaus DROP/EDROP Data
                    //127.0.0.10 PBL ISP Maintained
                    //127.0.0.11 PBL Spamhaus Maintained
            }),

            // https://www.spamhaus.org/dbl/
            new BlockList(true, "Spamhaus/DBL", "dbl.spamhaus.org", false, new String[]{
                    // https://www.spamhaus.org/faq/section/Spamhaus%20DBL#291
                    "127.0.1.2", // spam domain
                    "127.0.1.4", // phish domain
                    "127.0.1.5", // malware domain
                    "127.0.1.6", // botnet C&C domain
                    "127.0.1.102", // abused legit spam
                    "127.0.1.103", // abused spammed redirector domain
                    "127.0.1.104", // abused legit phish
                    "127.0.1.105", // abused legit malware
                    "127.0.1.106", // abused legit botnet C&C
            }),

            new BlockList(false, "UCEPROTECT/Level 1", "dnsbl-1.uceprotect.net", true, new String[]{
                    // https://www.uceprotect.net/en/index.php?m=6&s=11
                    "127.0.0.2",
            }),

            new BlockList(false, "UCEPROTECT/Level 2", "dnsbl-2.uceprotect.net", true, new String[]{
                    // https://www.uceprotect.net/en/index.php?m=6&s=11
                    "127.0.0.2",
            }),

            new BlockList(false, "UCEPROTECT/Level 3", "dnsbl-3.uceprotect.net", true, new String[]{
                    // https://www.uceprotect.net/en/index.php?m=6&s=11
                    "127.0.0.2",
            }),

            new BlockList(false, "Spamcop", "bl.spamcop.net", true, new String[]{
                    // https://www.spamcop.net/fom-serve/cache/291.html
                    "127.0.0.2",
            }),

            new BlockList(false, "Barracuda", "b.barracudacentral.org", true, new String[]{
                    // https://www.barracudacentral.org/rbl/how-to-use
                    "127.0.0.2",
            }),

            new BlockList(BuildConfig.DEBUG, "NordSpam", "dbl.nordspam.com", false, new String[]{
                    // https://www.nordspam.com/
                    "127.0.0.2",
            })
    ));

    private static final long CACHE_EXPIRY_AFTER = 3600 * 1000L; // milliseconds
    private static final Map<String, CacheEntry> cache = new Hashtable<>();

    static void clearCache() {
        Log.i("isJunk clear cache");
        synchronized (cache) {
            cache.clear();
        }
    }

    static void setEnabled(Context context, BlockList blocklist, boolean enabled) {
        Log.i("isJunk " + blocklist.name + "=" + enabled);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (blocklist.enabled == null || blocklist.enabled == enabled)
            prefs.edit().remove("blocklist." + blocklist.name).apply();
        else
            prefs.edit().putBoolean("blocklist." + blocklist.name, enabled).apply();
        clearCache();
    }

    static boolean isEnabled(Context context, BlockList blocklist) {
        if (blocklist.enabled == null)
            return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("blocklist." + blocklist.name, blocklist.enabled);
    }

    static void reset(Context context) {
        Log.i("isJunk reset");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (BlockList blocklist : BLOCK_LISTS)
            editor.remove("blocklist." + blocklist.name);
        editor.apply();
        clearCache();
    }

    static List<BlockList> getListsAvailable() {
        List<BlockList> result = new ArrayList<>();
        for (BlockList blockList : BLOCK_LISTS)
            if (blockList.enabled != null)
                result.add(blockList);
        return result;
    }

    static List<String> getNamesEnabled(Context context) {
        List<String> names = new ArrayList<>();
        for (BlockList blocklist : BLOCK_LISTS)
            if (isEnabled(context, blocklist))
                names.add(blocklist.name);
        return names;
    }

    static Boolean isJunk(Context context, String[] received) {
        if (received == null || received.length == 0)
            return null;

        String host = getFromHost(MimeUtility.unfold(received[received.length - 1]));
        if (host == null)
            return null;
        boolean numeric = host.startsWith("[") && host.endsWith("]");
        if (numeric)
            host = host.substring(1, host.length() - 1);

        return isJunk(context, host, true, BLOCK_LISTS);
    }

    static Boolean isJunk(Context context, List<Address> addresses) {
        if (ContactInfo.getLookupUri(addresses) != null)
            return false;

        boolean hasDomain = false;
        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            String domain = UriHelper.getEmailDomain(email);
            if (domain == null)
                continue;
            hasDomain = true;
            if (isJunk(context, domain, false, BLOCK_LISTS))
                return true;
        }

        return (hasDomain ? false : null);
    }

    static Boolean isJunk(Context context, Uri uri) {
        String domain = null;
        if ("mailto".equalsIgnoreCase(uri.getScheme())) {
            MailTo email = MailTo.parse(uri.toString());

            String to = email.getTo();
            if (TextUtils.isEmpty(to))
                return null;

            if (ContactInfo.getLookupUri(to) != null)
                return false;

            domain = UriHelper.getEmailDomain(email.getTo());
        } else
            domain = uri.getHost();

        if (domain == null)
            return null;

        return isJunk(context, domain, false, BLOCK_LISTS);
    }

    private static boolean isJunk(Context context, String host, boolean numeric, List<BlockList> blocklists) {
        synchronized (cache) {
            CacheEntry entry = cache.get(host);
            if (entry != null && !entry.isExpired())
                return entry.isJunk();
        }

        boolean blocked = false;
        for (BlockList blocklist : blocklists)
            if (isEnabled(context, blocklist) &&
                    blocklist.numeric == numeric &&
                    isJunk(context, host, blocklist)) {
                blocked = true;
                break;
            }

        synchronized (cache) {
            cache.put(host, new CacheEntry(blocked));
        }

        return blocked;
    }

    private static boolean isJunk(Context context, String host, BlockList blocklist) {
        try {
            if (blocklist.numeric) {
                long start = new Date().getTime();
                InetAddress[] addresses = DnsHelper.getAllByName(context, host);
                long elapsed = new Date().getTime() - start;
                Log.i("isJunk resolved=" + host + " elapse=" + elapsed + " ms");
                for (InetAddress addr : addresses) {
                    if (addr.isLoopbackAddress() ||
                            addr.isLinkLocalAddress() ||
                            addr.isSiteLocalAddress() ||
                            addr.isMulticastAddress()) {
                        Log.i("isJunk local=" + addr);
                        continue;
                    }
                    try {
                        StringBuilder lookup = new StringBuilder();
                        if (addr instanceof Inet4Address) {
                            byte[] a = addr.getAddress();
                            for (int i = 3; i >= 0; i--)
                                lookup.append(a[i] & 0xff).append('.');
                        } else if (addr instanceof Inet6Address) {
                            byte[] a = addr.getAddress();
                            for (int i = 15; i >= 0; i--) {
                                int b = a[i] & 0xff;
                                lookup.append(String.format("%01x", b & 0xf)).append('.');
                                lookup.append(String.format("%01x", b >> 4)).append('.');
                            }
                        }

                        lookup.append(blocklist.address);

                        if (isJunk(context, lookup.toString(), blocklist.responses))
                            return true;
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }
            } else {
                long start = new Date().getTime();
                String lookup = host + "." + blocklist.address;
                boolean junk = isJunk(context, lookup, blocklist.responses);
                long elapsed = new Date().getTime() - start;
                Log.i("isJunk" + " " + lookup + "=" + junk + " elapsed=" + elapsed);
                return junk;
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return false;
    }

    private static boolean isJunk(Context context, String lookup, InetAddress[] responses) {
        long start = new Date().getTime();
        InetAddress result;
        try {
            // Possibly blocked
            result = DnsHelper.getByName(context, lookup);
        } catch (UnknownHostException ignored) {
            // Not blocked
            result = null;
        }
        long elapsed = new Date().getTime() - start;

        boolean blocked = false;
        if (result != null && responses.length > 0) {
            for (InetAddress response : responses)
                if (response.equals(result)) {
                    blocked = true;
                    break;
                }
            if (!blocked)
                result = null;
        }

        Log.w("isJunk" +
                " lookup=" + lookup +
                " result=" + (result == null ? null : result.getHostAddress()) +
                " blocked=" + blocked +
                " elapsed=" + elapsed);

        return blocked;
    }

    private static String getFromHost(String received) {
        String[] words = received.split("\\s+");
        for (int i = 0; i < words.length - 1; i++)
            if ("from".equalsIgnoreCase(words[i])) {
                String host = words[i + 1].toLowerCase(Locale.ROOT);
                if (!TextUtils.isEmpty(host))
                    return host;
            }
        return null;
    }

    static void show(Context context, String received) {
        String host = DnsBlockList.getFromHost(MimeUtility.unfold(received));
        if (host == null)
            return;

        if (host.startsWith("[") && host.endsWith("]"))
            host = host.substring(1, host.length() - 1);

        Uri uri = Uri.parse(BuildConfig.MXTOOLBOX_URI)
                .buildUpon()
                .appendPath("/SuperTool.aspx")
                .appendQueryParameter("action", "blacklist:" + host)
                .appendQueryParameter("run", "toolpage")
                .build();
        Helper.view(context, uri, true);
    }

    private static class CacheEntry {
        private final long time;
        private final boolean blocked;

        CacheEntry(boolean blocked) {
            this.time = new Date().getTime();
            this.blocked = blocked;
        }

        boolean isExpired() {
            return (new Date().getTime() - this.time) > CACHE_EXPIRY_AFTER;
        }

        boolean isJunk() {
            return blocked;
        }
    }

    static class BlockList {
        int id;
        Boolean enabled;
        String name;
        String address;
        boolean numeric;
        InetAddress[] responses;

        private static int nextid = 1;

        BlockList(Boolean enabled, String name, String address, boolean numeric, String[] responses) {
            this.id = nextid++;
            this.enabled = enabled;
            this.name = name;
            this.address = address;
            this.numeric = numeric;
            List<InetAddress> r = new ArrayList<>();
            for (String response : responses)
                try {
                    r.add(InetAddress.getByName(response));
                } catch (UnknownHostException ex) {
                    Log.e(ex);
                }
            this.responses = r.toArray(new InetAddress[0]);

            if (!numeric && BuildConfig.PLAY_STORE_RELEASE)
                this.enabled = null;
        }
    }
}
