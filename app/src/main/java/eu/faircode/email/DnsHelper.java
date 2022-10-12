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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DnsResolver;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class DnsHelper {
    // https://dns.watch/
    private static final String DEFAULT_DNS = "84.200.69.80";
    private static final int CHECK_TIMEOUT = 5; // seconds
    private static final int LOOKUP_TIMEOUT = 15; // seconds

    static void checkMx(Context context, Address[] addresses) throws UnknownHostException {
        if (addresses == null)
            return;

        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            String domain = UriHelper.getEmailDomain(email);
            if (domain == null)
                continue;
            lookup(context, domain, "mx", CHECK_TIMEOUT);
        }
    }

    @NonNull
    static DnsRecord[] lookup(Context context, String name, String type) throws UnknownHostException {
        return lookup(context, name, type, LOOKUP_TIMEOUT);
    }

    @NonNull
    static DnsRecord[] lookup(Context context, String name, String type, int timeout) throws UnknownHostException {
        int rtype;
        switch (type) {
            case "ns":
                rtype = Type.NS;
                break;
            case "mx":
                rtype = Type.MX;
                break;
            case "soa":
                rtype = Type.SOA;
                break;
            case "srv":
                rtype = Type.SRV;
                break;
            case "txt":
                rtype = Type.TXT;
                break;
            case "a":
                rtype = Type.A;
                break;
            case "aaaa":
                rtype = Type.AAAA;
                break;
            default:
                throw new IllegalArgumentException(type);
        }

        try {
            SimpleResolver resolver = new SimpleResolver(getDnsServer(context)) {
                private IOException ex;
                private Message result;

                @Override
                public Message send(Message query) throws IOException {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                        return super.send(query);
                    else {
                        Log.i("Using Android DNS resolver");
                        Semaphore sem = new Semaphore(0);
                        DnsResolver resolver = DnsResolver.getInstance();
                        //OPTRecord optRecord = new OPTRecord(4096, 0, 0, Flags.DO, null);
                        //query.addRecord(optRecord, Section.ADDITIONAL);
                        //query.getHeader().setFlag(Flags.AD);
                        Log.i("DNS query=" + query.toString());
                        resolver.rawQuery(
                                null,
                                query.toWire(),
                                DnsResolver.FLAG_EMPTY,
                                new Executor() {
                                    @Override
                                    public void execute(Runnable command) {
                                        command.run();
                                    }
                                },
                                null,
                                new DnsResolver.Callback<byte[]>() {
                                    @Override
                                    public void onAnswer(@NonNull byte[] answer, int rcode) {
                                        try {
                                            if (rcode == 0)
                                                result = new Message(answer);
                                            else
                                                ex = new IOException("rcode=" + rcode);
                                        } catch (Throwable e) {
                                            ex = new IOException(e.getMessage());
                                        } finally {
                                            sem.release();
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull DnsResolver.DnsException e) {
                                        try {
                                            ex = new IOException(e.getMessage(), e);
                                        } finally {
                                            sem.release();
                                        }
                                    }
                                });
                        try {
                            if (!sem.tryAcquire(timeout, TimeUnit.SECONDS))
                                ex = new IOException("timeout");
                        } catch (InterruptedException e) {
                            ex = new IOException("interrupted");
                        }

                        if (ex == null) {
                            //ConnectivityManager cm = getSystemService(context, ConnectivityManager.class);
                            //Network active = (cm == null ? null : cm.getActiveNetwork());
                            //LinkProperties props = (active == null ? null : cm.getLinkProperties(active));
                            //Log.i("DNS private=" + (props == null ? null : props.isPrivateDnsActive()));
                            Log.i("DNS answer=" + result.toString() + " flags=" + result.getHeader().printFlags());
                            return result;
                        } else {
                            Log.i(ex);
                            throw ex;
                        }
                    }
                }
            };
            resolver.setTimeout(timeout);
            Lookup lookup = new Lookup(name, rtype);
            lookup.setResolver(resolver);
            Log.i("Lookup name=" + name + " @" + resolver.getAddress() + " type=" + rtype);
            Record[] records = lookup.run();

            if (lookup.getResult() == Lookup.HOST_NOT_FOUND ||
                    lookup.getResult() == Lookup.TYPE_NOT_FOUND)
                throw new UnknownHostException(name);
            else if (lookup.getResult() != Lookup.SUCCESSFUL)
                Log.i("DNS error=" + lookup.getErrorString());

            List<DnsRecord> result = new ArrayList<>();

            if (records != null)
                for (Record record : records) {
                    Log.i("Found record=" + record);
                    if (record instanceof NSRecord) {
                        NSRecord ns = (NSRecord) record;
                        result.add(new DnsRecord(ns.getTarget().toString(true)));
                    } else if (record instanceof MXRecord) {
                        MXRecord mx = (MXRecord) record;
                        result.add(new DnsRecord(mx.getTarget().toString(true)));
                    } else if (record instanceof SOARecord) {
                        SOARecord soa = (SOARecord) record;
                        result.add(new DnsRecord(soa.getHost().toString(true)));
                    } else if (record instanceof SRVRecord) {
                        SRVRecord srv = (SRVRecord) record;
                        result.add(new DnsRecord(srv.getTarget().toString(true), srv.getPort()));
                    } else if (record instanceof TXTRecord) {
                        TXTRecord txt = (TXTRecord) record;
                        for (Object content : txt.getStrings())
                            if (result.size() > 0)
                                result.get(0).name += content.toString();
                            else
                                result.add(new DnsRecord(content.toString(), 0));
                    } else if (record instanceof ARecord) {
                        ARecord a = (ARecord) record;
                        result.add(new DnsRecord(a.getAddress().getHostAddress()));
                    } else if (record instanceof AAAARecord) {
                        AAAARecord aaaa = (AAAARecord) record;
                        result.add(new DnsRecord(aaaa.getAddress().getHostAddress()));
                    } else
                        throw new IllegalArgumentException(record.getClass().getName());
                }

            return result.toArray(new DnsRecord[0]);
        } catch (TextParseException ex) {
            Log.e(ex);
            return new DnsRecord[0];
        }
    }

    private static String getDnsServer(Context context) {
        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
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
}
