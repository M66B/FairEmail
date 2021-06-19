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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.text.TextUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class DnsBlockList {
    // https://www.spamhaus.org/zen/
    static String[] DEFAULT_BLOCKLISTS = new String[]{"zen.spamhaus.org"};
    private static final long CACHE_EXPIRY_AFTER = 3600 * 1000L; // milliseconds
    private static final Map<InetAddress, CacheEntry> cache = new Hashtable<>();

    static boolean isJunk(String email) {
        return isJunk(email, DEFAULT_BLOCKLISTS);
    }

    static boolean isJunk(String email, String[] blocklists) {
        if (TextUtils.isEmpty(email))
            return false;
        int at = email.indexOf('@');
        if (at < 0)
            return false;
        String domain = email.substring(at + 1);
        for (String blocklist : blocklists)
            if (isJunk(domain, blocklist))
                return true;
        return false;
    }

    private static boolean isJunk(String domain, String blocklist) {
        boolean blocked = false;
        try {
            for (InetAddress addr : InetAddress.getAllByName(domain))
                try {
                    synchronized (cache) {
                        CacheEntry cached = cache.get(addr);
                        if (cached != null && !cached.isExpired())
                            return cached.isJunk();
                    }

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

                    lookup.append(blocklist);

                    InetAddress result;
                    try {
                        result = InetAddress.getByName(lookup.toString());
                        if (result instanceof Inet4Address) {
                            /*
                                https://www.spamhaus.org/faq/section/DNSBL%20Usage#200

                                127.0.0.2	SBL	Spamhaus SBL Data
                                127.0.0.3	SBL	Spamhaus SBL CSS Data
                                127.0.0.4	XBL	CBL Data
                                127.0.0.9	SBL	Spamhaus DROP/EDROP Data (in addition to 127.0.0.2, since 01-Jun-2016)
                                127.0.0.10	PBL	ISP Maintained
                                127.0.0.11	PBL	Spamhaus Maintained
                             */

                            byte[] a = result.getAddress();
                            int statusClass = a[1] & 0xFF;
                            int statusCode = a[3] & 0xFF;
                            if (statusClass != 0 ||
                                    (statusCode != 2 &&
                                            statusCode != 3 &&
                                            statusCode != 4 &&
                                            statusCode != 9)) {
                                Log.w("isJunk" +
                                        " addr=" + addr +
                                        " lookup=" + lookup +
                                        " result=" + result +
                                        " status=" + statusClass + "/" + statusCode);
                                result = null;
                            }
                        } else {
                            Log.w("isJunk result=" + result);
                            result = null;
                        }
                    } catch (UnknownHostException ignored) {
                        // Not blocked
                        result = null;
                    }

                    Log.i("isJunk " + addr + " " + lookup + "=" + (result == null ? "false" : result));

                    synchronized (cache) {
                        cache.put(addr, new CacheEntry(result));
                    }

                    if (result != null)
                        blocked = true;
                } catch (Throwable ex) {
                    Log.w(ex);
                }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return blocked;
    }

    private static class CacheEntry {
        private final long time;
        private final InetAddress result;

        CacheEntry(InetAddress result) {
            this.time = new Date().getTime();
            this.result = result;
        }

        boolean isExpired() {
            return (new Date().getTime() - this.time) > CACHE_EXPIRY_AFTER;
        }

        boolean isJunk() {
            return (this.result != null);
        }
    }
}
