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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class DnsHelper {
    // https://dns.watch/
    private static final String DEFAULT_DNS = "84.200.69.80";
    private static final int LOOKUP_TIMEOUT = 15; // seconds

    static void checkMx(Context context, Address[] addresses) throws UnknownHostException {
        if (addresses == null)
            return;

        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            if (email == null)
                continue;

            int d = email.lastIndexOf("@");
            if (d < 0)
                continue;

            String domain = email.substring(d + 1);

            boolean found = true;
            try {
                SimpleResolver resolver = new SimpleResolver(getDnsServer(context));
                resolver.setTimeout(LOOKUP_TIMEOUT);
                Lookup lookup = new Lookup(domain, Type.MX);
                lookup.setResolver(resolver);
                lookup.run();
                Log.i("Check name=" + domain + " @" + resolver.getAddress() + " result=" + lookup.getResult());

                if (lookup.getResult() == Lookup.HOST_NOT_FOUND ||
                        lookup.getResult() == Lookup.TYPE_NOT_FOUND)
                    found = false;
                else if (lookup.getResult() != Lookup.SUCCESSFUL)
                    throw new UnknownHostException("DNS error=" + lookup.getErrorString());
            } catch (Throwable ex) {
                Log.e(ex);
            }

            if (!found)
                throw new UnknownHostException(context.getString(R.string.title_no_server, domain));
        }
    }

    static InetAddress lookupMx(Context context, String domain) {
        try {
            DnsRecord[] records = lookup(context, domain, "mx");
            if (records.length > 0)
                return InetAddress.getByName(records[0].name);
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return null;
    }

    @NonNull
    static DnsRecord[] lookup(Context context, String name, String type) throws UnknownHostException {
        int rtype;
        switch (type) {
            case "mx":
                rtype = Type.MX;
                break;
            case "srv":
                rtype = Type.SRV;
                break;
            default:
                throw new IllegalArgumentException(type);
        }

        try {
            SimpleResolver resolver = new SimpleResolver(getDnsServer(context));
            resolver.setTimeout(LOOKUP_TIMEOUT);
            Lookup lookup = new Lookup(name, rtype);
            lookup.setResolver(resolver);
            Log.i("Lookup name=" + name + " @" + resolver.getAddress() + " type=" + rtype);
            Record[] records = lookup.run();

            if (lookup.getResult() == Lookup.HOST_NOT_FOUND ||
                    lookup.getResult() == Lookup.TYPE_NOT_FOUND)
                throw new UnknownHostException(name);
            else if (lookup.getResult() != Lookup.SUCCESSFUL)
                Log.e("DNS error=" + lookup.getErrorString());

            List<DnsRecord> result = new ArrayList<>();

            if (records != null)
                for (Record record : records) {
                    Log.i("Found record=" + record);
                    if (record instanceof MXRecord) {
                        MXRecord mx = (MXRecord) record;
                        result.add(new DnsRecord(mx.getTarget().toString(true)));
                    } else if (record instanceof SRVRecord) {
                        SRVRecord srv = (SRVRecord) record;
                        result.add(new DnsRecord(srv.getTarget().toString(true), srv.getPort()));
                    } else
                        throw new IllegalArgumentException(record.getClass().getName());
                }

            return result.toArray(new DnsRecord[0]);
        } catch (TextParseException ex) {
            throw new UnknownHostException(ex.getMessage());
        }
    }

    static String getParentDomain(String host) {
        if (host != null) {
            String[] h = host.split("\\.");
            if (h.length >= 2)
                return h[h.length - 2] + "." + h[h.length - 1];
        }
        return host;
    }

    private static String getDnsServer(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return DEFAULT_DNS;

        LinkProperties props = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            for (Network network : cm.getAllNetworks()) {
                NetworkInfo ni = cm.getNetworkInfo(network);
                if (ni != null && ni.isConnected()) {
                    props = cm.getLinkProperties(network);
                    Log.i("Old props=" + props);
                    break;
                }
            }
        else {
            Network active = cm.getActiveNetwork();
            if (active == null)
                return DEFAULT_DNS;
            props = cm.getLinkProperties(active);
            Log.i("New props=" + props);
        }

        if (props == null)
            return DEFAULT_DNS;

        List<InetAddress> dns = props.getDnsServers();
        if (dns.size() == 0)
            return DEFAULT_DNS;
        else
            return dns.get(0).getHostAddress();
    }

    static class DnsRecord {
        String name;
        Integer port;

        DnsRecord(String name) {
            this.name = name;
        }

        DnsRecord(String name, int port) {
            this.name = name;
            this.port = port;
        }
    }

    static void test(Context context) {
        try {
            String domain = "gmail.com";
            checkMx(context, new Address[]{Log.myAddress()});
            InetAddress iaddr = lookupMx(context, domain);
            DnsRecord[] records = DnsHelper.lookup(context, "_imaps._tcp." + domain, "srv");
            if (records.length == 0)
                throw new UnknownHostException(domain);
            Log.i("DNS iaddr=" + iaddr + " srv=" + records[0].name + ":" + records[0].port);
        } catch (Throwable ex) {
            Log.e("DNS", ex);
        }
    }
}
