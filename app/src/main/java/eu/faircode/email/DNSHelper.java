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
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class DNSHelper {
    // https://dns.watch/
    private static final String DEFAULT_DNS = "84.200.69.80";

    static boolean lookupMx(Context context, Address[] addresses) throws UnknownHostException {
        boolean ok = true;

        if (addresses != null)
            for (Address address : addresses)
                try {
                    String email = ((InternetAddress) address).getAddress();
                    if (email == null)
                        continue;

                    int d = email.lastIndexOf("@");
                    if (d < 0)
                        continue;

                    String domain = email.substring(d + 1);
                    Lookup lookup = new Lookup(domain, Type.MX);
                    SimpleResolver resolver = new SimpleResolver(getDnsServer(context));
                    lookup.setResolver(resolver);
                    Log.i("Lookup MX=" + domain + " @" + resolver.getAddress());

                    lookup.run();
                    if (lookup.getResult() == Lookup.HOST_NOT_FOUND ||
                            lookup.getResult() == Lookup.TYPE_NOT_FOUND) {
                        Log.i("Lookup MX=" + domain + " result=" + lookup.getErrorString());
                        throw new UnknownHostException(context.getString(R.string.title_no_server, domain));
                    } else if (lookup.getResult() != Lookup.SUCCESSFUL)
                        ok = false;
                } catch (UnknownHostException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    Log.e(ex);
                    ok = false;
                }

        return ok;
    }

    static InetAddress lookupMx(Context context, String domain) {
        try {
            Lookup lookup = new Lookup(domain, Type.MX);
            SimpleResolver resolver = new SimpleResolver(getDnsServer(context));
            lookup.setResolver(resolver);
            Log.i("Lookup MX=" + domain + " @" + resolver.getAddress());

            lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                Record[] answers = lookup.getAnswers();
                if (answers != null && answers.length > 0 && answers[0] instanceof MXRecord) {
                    MXRecord mx = (MXRecord) answers[0];
                    return InetAddress.getByName(mx.getTarget().toString(true));
                }
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return null;
    }

    @NonNull
    static Record[] lookup(Context context, String name, int type) throws TextParseException, UnknownHostException {
        Lookup lookup = new Lookup(name, type);

        SimpleResolver resolver = new SimpleResolver(getDnsServer(context));
        lookup.setResolver(resolver);
        Log.i("Lookup name=" + name + " @" + resolver.getAddress() + " type=" + type);
        Record[] records = lookup.run();

        if (lookup.getResult() != Lookup.SUCCESSFUL)
            if (lookup.getResult() == Lookup.HOST_NOT_FOUND ||
                    lookup.getResult() == Lookup.TYPE_NOT_FOUND)
                throw new UnknownHostException(name);
            else
                throw new UnknownHostException(lookup.getErrorString());

        if (records.length == 0)
            throw new UnknownHostException(name);

        for (Record record : records)
            Log.i("Found record=" + record);

        return records;
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

    static void test(Context context) {
        try {
            String domain = "gmail.com";
            boolean ok = lookupMx(context, new Address[]{Log.myAddress()});
            InetAddress iaddr = lookupMx(context, domain);
            Record[] records = DNSHelper.lookup(context, "_imaps._tcp." + domain, Type.SRV);
            SRVRecord srv = (SRVRecord) records[0];
            Log.i("DNS ok=" + ok + " iaddr=" + iaddr + " srv=" + srv.getTarget().toString());
        } catch (Throwable ex) {
            Log.e("DNS", ex);
        }
    }
}
