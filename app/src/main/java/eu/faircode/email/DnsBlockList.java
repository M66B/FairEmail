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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class DnsBlockList {
    static final List<BlockList> BLOCKLISTS = Collections.unmodifiableList(Arrays.asList(
            new BlockList("zen.spamhaus.org", new String[]{
                    //https://www.spamhaus.org/faq/section/DNSBL%20Usage#200
                    "127.0.0.2", // SBL Spamhaus SBL Data
                    "127.0.0.3", // SBL Spamhaus SBL CSS Data
                    "127.0.0.4", // XBL CBL Data
                    "127.0.0.9" // SBL Spamhaus DROP/EDROP Data
                    //127.0.0.10 PBL ISP Maintained
                    //127.0.0.11 PBL Spamhaus Maintained
            }),
            new BlockList("bl.spamcop.net", new String[]{
                    // https://www.spamcop.net/fom-serve/cache/291.html
                    "127.0.0.2"
            })
            //new BlockList("b.barracudacentral.org", new String[]{
            //        // https://www.barracudacentral.org/rbl/how-to-use
            //})
    ));

    private static final long CACHE_EXPIRY_AFTER = 3600 * 1000L; // milliseconds
    private static final Map<String, CacheEntry> cache = new Hashtable<>();

    static boolean isJunk(String email) {
        if (TextUtils.isEmpty(email))
            return false;

        int at = email.indexOf('@');
        if (at < 0)
            return false;

        return isJunk(email.substring(at + 1), BLOCKLISTS);
    }

    private static boolean isJunk(String domain, List<BlockList> blocklists) {
        synchronized (cache) {
            CacheEntry entry = cache.get(domain);
            if (entry != null && !entry.isExpired())
                return entry.isJunk();
        }

        boolean blocked = false;
        for (BlockList blocklist : blocklists)
            if (isJunk(domain, blocklist)) {
                blocked = true;
                break;
            }

        synchronized (cache) {
            cache.put(domain, new CacheEntry(blocked));
        }

        return blocked;
    }

    private static boolean isJunk(String domain, BlockList blocklist) {
        try {
            long start = new Date().getTime();
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            long elapsed = new Date().getTime() - start;
            Log.i("isJunk resolved=" + domain + " elapse=" + elapsed + " ms");
            for (InetAddress addr : addresses)
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

                    start = new Date().getTime();
                    InetAddress result;
                    try {
                        // Possibly blocked
                        result = InetAddress.getByName(lookup.toString());
                    } catch (UnknownHostException ignored) {
                        // Not blocked
                        result = null;
                    }
                    elapsed = new Date().getTime() - start;

                    if (result != null && blocklist.responses.length > 0) {
                        boolean blocked = false;
                        for (InetAddress response : blocklist.responses)
                            if (response.equals(result)) {
                                blocked = true;
                                break;
                            }
                        if (!blocked) {
                            Log.w("isJunk" +
                                    " addr=" + addr +
                                    " lookup=" + lookup +
                                    " result=" + result +
                                    " elapsed=" + elapsed);
                            result = null;
                        }
                    }

                    Log.i("isJunk " + addr +
                            " " + lookup + "=" + (result == null ? "false" : result) +
                            " elapsed=" + elapsed);

                    if (result != null)
                        return true;
                } catch (Throwable ex) {
                    Log.w(ex);
                }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return false;
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
        String address;
        InetAddress[] responses;

        BlockList(String address, String[] responses) {
            this.address = address;
            List<InetAddress> r = new ArrayList<>();
            for (String response : responses)
                try {
                    r.add(InetAddress.getByName(response));
                } catch (UnknownHostException ex) {
                    Log.e(ex);
                }
            this.responses = r.toArray(new InetAddress[0]);
        }
    }
}
