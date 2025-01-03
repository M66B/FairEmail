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
import android.net.ConnectivityManager;
import android.net.DnsResolver;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.minidns.AbstractDnsClient;
import org.minidns.DnsCache;
import org.minidns.DnsClient;
import org.minidns.cache.LruCache;
import org.minidns.dane.DaneVerifier;
import org.minidns.dnsmessage.DnsMessage;
import org.minidns.dnsqueryresult.DnsQueryResult;
import org.minidns.dnsqueryresult.StandardDnsQueryResult;
import org.minidns.dnssec.DnssecClient;
import org.minidns.dnssec.DnssecResultNotAuthenticException;
import org.minidns.dnssec.DnssecUnverifiedReason;
import org.minidns.dnssec.DnssecValidationFailedException;
import org.minidns.dnsserverlookup.AbstractDnsServerLookupMechanism;
import org.minidns.hla.DnssecResolverApi;
import org.minidns.hla.ResolutionUnsuccessfulException;
import org.minidns.hla.ResolverApi;
import org.minidns.hla.ResolverResult;
import org.minidns.record.A;
import org.minidns.record.AAAA;
import org.minidns.record.Data;
import org.minidns.record.MX;
import org.minidns.record.NS;
import org.minidns.record.Record;
import org.minidns.record.SRV;
import org.minidns.record.TXT;
import org.minidns.source.AbstractDnsDataSource;
import org.minidns.source.DnsDataSource;
import org.minidns.util.MultipleIoException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class DnsHelper {
    // https://dns.watch/
    private static final String DEFAULT_DNS4 = "84.200.69.80";
    private static final String DEFAULT_DNS6 = "2001:1608:10:25::1c04:b12f";
    private static final int CHECK_TIMEOUT = 5; // seconds
    private static final int LOOKUP_TIMEOUT = 15; // seconds

    static void init(Context context) {
        DnsClient.addDnsServerLookupMechanism(
                new AbstractDnsServerLookupMechanism("FairEmail", 1) {
                    @Override
                    public boolean isAvailable() {
                        return true;
                    }

                    @Override
                    public List<String> getDnsServerAddresses() {
                        List<String> servers = getDnsServers(context);
                        Log.i("DNS servers=" + TextUtils.join(",", servers));
                        return servers;
                    }
                });
    }

    static void checkMx(Context context, Address[] addresses) throws UnknownHostException {
        if (addresses == null)
            return;

        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            String domain = UriHelper.getEmailDomain(email);
            if (domain == null)
                continue;
            DnsRecord[] records = _lookup(context, domain, "mx", CHECK_TIMEOUT);
            if (records.length == 0)
                throw new UnknownHostException(domain);
        }
    }

    @NonNull
    static DnsRecord[] lookup(Context context, String name, String type) {
        return _lookup(context, name, type, LOOKUP_TIMEOUT);
    }

    @NonNull
    private static DnsRecord[] _lookup(Context context, String name, String type, int timeout) {
        try {
            return _lookup(context, name, type, timeout, false);
        } catch (Throwable ex) {
            if (ex instanceof MultipleIoException ||
                    ex instanceof ResolutionUnsuccessfulException ||
                    ex instanceof DnssecValidationFailedException)
                Log.i(ex);
            else
                Log.e(ex);
            return new DnsRecord[0];
        }
    }

    @NonNull
    private static DnsRecord[] _lookup(Context context, String name, String type, int timeout, boolean dnssec) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dns_custom = prefs.getBoolean("dns_custom", false);

        String filter = null;
        int colon = type.indexOf(':');
        if (colon > 0) {
            filter = type.substring(colon + 1);
            type = type.substring(0, colon);
        }

        Class<? extends Data> clazz;
        switch (type) {
            case "ns":
                clazz = NS.class;
                break;
            case "mx":
                clazz = MX.class;
                break;
            case "srv":
                clazz = SRV.class;
                break;
            case "txt":
                clazz = TXT.class;
                break;
            case "a":
                clazz = A.class;
                break;
            case "aaaa":
                clazz = AAAA.class;
                break;
            default:
                throw new IllegalArgumentException(type);
        }

        ResolverApi resolver = (dnssec ? DnssecResolverApi.INSTANCE : ResolverApi.INSTANCE);
        AbstractDnsClient client = resolver.getClient();

        if (false) {
            String private_dns = ConnectionHelper.getPrivateDnsServerName(context);
            Log.w("DNS private=" + private_dns);
            if (private_dns != null)
                client.setDataSource(new DoTDataSource(private_dns));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !dns_custom)
            client.setDataSource(new AndroidDataSource());

        client.getDataSource().setTimeout(timeout * 1000);

        if (!dnssec)
            client.setDataSource(new AuthoritiveDataSource(client.getDataSource()));

        // https://github.com/MiniDNS/minidns/issues/102
        if (client instanceof DnssecClient)
            ((DnssecClient) client).setUseHardcodedDnsServers(false);

        ResolverResult<? extends Data> data;
        try {
            Log.i("DNS query name=" + type + ":" + name);
            data = resolver.resolve(name, clazz);
            Log.i("DNS resolved name=" + type + ":" + name +
                    " success=" + data.wasSuccessful() +
                    " rcode=" + data.getResponseCode());
            data.throwIfErrorResponse();
        } catch (Throwable ex) {
            Log.w("DNS error message=" + ex.getMessage());
            throw ex;
        }

        Set<DnssecUnverifiedReason> unverifiedReasons = data.getUnverifiedReasons();
        boolean secure = (unverifiedReasons == null || unverifiedReasons.isEmpty());
        Log.i("DNS secure=" + secure + " dnssec=" + dnssec);
        if (!secure && dnssec) {
            DnssecResultNotAuthenticException ex = data.getDnssecResultNotAuthenticException();
            if (ex != null)
                throw ex;
        }

        List<DnsRecord> result = new ArrayList<>();

        DnsMessage raw = data.getRawAnswer();
        List<Record<? extends Data>> answers = (raw == null ? null : raw.answerSection);
        Log.i("DNS answers=" + (answers == null ? "n/a" : answers.size()));
        if (answers != null) {
            Record.TYPE expectedType = data.getQuestion().type;
            for (Record<? extends Data> record : answers) {
                if (record.type != expectedType) {
                    Log.i("DNS skip=" + record);
                    continue;
                }

                Data answer = record.getPayload();
                Log.i("DNS record=" + record + " answer=" + answer);

                if (answer instanceof NS) {
                    NS ns = (NS) answer;
                    result.add(new DnsRecord(ns.getTarget().toString()));
                } else if (answer instanceof MX) {
                    MX mx = (MX) answer;
                    result.add(new DnsRecord(mx.target.toString(), 0, mx.priority, 0));
                } else if (answer instanceof SRV) {
                    SRV srv = (SRV) answer;
                    result.add(new DnsRecord(srv.target.toString(), srv.port, srv.priority, srv.weight));
                } else if (answer instanceof TXT) {
                    StringBuilder sb = new StringBuilder();
                    TXT txt = (TXT) answer;
                    for (String text : txt.getCharacterStrings()) {
                        if (filter != null &&
                                (TextUtils.isEmpty(text) || !text.toLowerCase(Locale.ROOT).startsWith(filter)))
                            continue;
                        int i = 0;
                        int slash = text.indexOf('\\', i);
                        while (slash >= 0 && slash + 4 < text.length()) {
                            String digits = text.substring(slash + 1, slash + 4);
                            if (TextUtils.isDigitsOnly(digits)) {
                                int k = Integer.parseInt(digits);
                                text = text.substring(0, slash) + (char) k + text.substring(slash + 4);
                            } else
                                i += 4;
                            slash = text.indexOf('\\', i);
                        }
                        sb.append(text);
                    }
                    result.add(new DnsRecord(sb.toString(), 0));
                } else if (answer instanceof A) {
                    A a = (A) answer;
                    result.add(new DnsRecord(a.getInetAddress()));
                } else if (answer instanceof AAAA) {
                    AAAA aaaa = (AAAA) answer;
                    result.add(new DnsRecord(aaaa.getInetAddress()));
                } else
                    Log.e("DNS unexpected record=" +
                            (answer == null ? null : answer.getClass().getName()));
            }
        }

        for (DnsRecord record : result) {
            record.query = name;
            record.secure = secure;
            try {
                record.authentic = data.isAuthenticData();
            } catch (Throwable ex) {
                Log.w(ex);
                record.authentic = false;
            }
        }

        if ("mx".equals(type) || "srv".equals(type))
            Collections.sort(result, new Comparator<DnsRecord>() {
                @Override
                public int compare(DnsRecord d1, DnsRecord d2) {
                    int o = Integer.compare(
                            d1.priority == null ? 0 : d1.priority,
                            d2.priority == null ? 0 : d2.priority);
                    if (o == 0)
                        o = Integer.compare(
                                d1.weight == null ? 0 : d1.weight,
                                d2.weight == null ? 0 : d2.weight);
                    return o;
                }
            });

        return result.toArray(new DnsRecord[0]);
    }

    static InetAddress getByName(Context context, String host) throws UnknownHostException {
        return getByName(context, host, false);
    }

    static InetAddress[] getAllByName(Context context, String host) throws UnknownHostException {
        return getAllByName(context, host, false);
    }

    static InetAddress getByName(Context context, String host, boolean dnssec) throws UnknownHostException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dns_custom = prefs.getBoolean("dns_custom", false);

        if (!hasDnsSec())
            dnssec = false;

        if (!dns_custom && !dnssec)
            return InetAddress.getByName(host);

        if (ConnectionHelper.isNumericAddress(host))
            return InetAddress.getByName(host);

        return getAllByName(context, host, dnssec)[0];
    }

    static InetAddress[] getAllByName(Context context, String host, boolean dnssec) throws UnknownHostException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dns_custom = prefs.getBoolean("dns_custom", false);

        if (!hasDnsSec())
            dnssec = false;

        if (!dns_custom && !dnssec)
            return InetAddress.getAllByName(host);

        List<InetAddress> result = new ArrayList<>();

        boolean[] has46 = ConnectionHelper.has46(context);
        try {
            if (has46[0])
                for (DnsRecord a : _lookup(context, host, "a", LOOKUP_TIMEOUT, dnssec))
                    result.add(a.address);

            if (has46[1])
                for (DnsRecord aaaa : _lookup(context, host, "aaaa", LOOKUP_TIMEOUT, dnssec))
                    result.add(aaaa.address);

            if (result.size() == 0)
                throw new UnknownHostException(host);

            return result.toArray(new InetAddress[0]);
        } catch (IOException ex) {
            throw new UnknownHostException(ex.getMessage());
        }
    }

    static void verifyDane(X509Certificate[] chain, String server, int port) throws CertificateException {
        if (!hasDnsSec())
            return;

        List<String> log = new ArrayList<>();

        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                log.add(record.getMessage());
                Log.w("DANE " + record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        String clazz = DaneVerifier.class.getName();
        try {
            Logger.getLogger(clazz).addHandler(handler);
            Log.w("DANE verify " + server + ":" + port);

            DnssecClient client = DnssecResolverApi.INSTANCE.getDnssecClient();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                client.setDataSource(new AndroidDataSource());

            client.getDataSource().setTimeout(LOOKUP_TIMEOUT * 1000);

            client.setUseHardcodedDnsServers(false);

            boolean verified = new DaneVerifier(client).verifyCertificateChain(chain, server, port);
            Log.w("DANE verified=" + verified + " " + server + ":" + port);
            if (!verified)
                throw new CertificateException("DANE missing or invalid",
                        new CertificateException(TextUtils.join("\n", log)));
        } catch (CertificateException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new CertificateException("DANE error", ex);
        } finally {
            Logger.getLogger(clazz).removeHandler(handler);
        }
    }

    static List<String> getDnsServers(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dns_custom = prefs.getBoolean("dns_custom", false);
        String dns_extra = prefs.getString("dns_extra", null);

        List<String> result = new ArrayList<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || dns_custom)
            result.addAll(_getDnsServers(context));

        if (!TextUtils.isEmpty(dns_extra)) {
            String[] extras = dns_extra.replaceAll("\\s+", "").split(",");
            for (String extra : extras)
                if (ConnectionHelper.isNumericAddress(extra))
                    result.add(extra);
                else
                    Log.w("DNS extra invalid=" + extra);
        }

        result.add(DEFAULT_DNS4);
        result.add(DEFAULT_DNS6);

        return result;
    }

    private static List<String> _getDnsServers(Context context) {
        List<String> result = new ArrayList<>();

        try {
            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            if (cm == null)
                return result;

            Network active = ConnectionHelper.getActiveNetwork(context);
            if (active == null)
                return result;

            LinkProperties props = cm.getLinkProperties(active);
            if (props == null)
                return result;

            List<InetAddress> dns = props.getDnsServers();
            for (int i = 0; i < dns.size(); i++)
                result.add(dns.get(i).getHostAddress());
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return result;
    }

    static void clear(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            for (String key : prefs.getAll().keySet())
                if (key != null && key.startsWith("dns."))
                    editor.remove(key);
            editor.apply();

            for (ResolverApi resolver : new ResolverApi[]{DnssecResolverApi.INSTANCE, ResolverApi.INSTANCE}) {
                AbstractDnsClient client = resolver.getClient();
                DnsCache cache = client.getCache();
                if (cache instanceof LruCache)
                    ((LruCache) cache).clear();
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static boolean hasDnsSec() {
        if (BuildConfig.PLAY_STORE_RELEASE)
            return false;
        // DNSSEC causes crashes in libc
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
    }

    static void test(Context context) throws UnknownHostException {
        test(context, "gmail.com", "ns");
        test(context, "web.de", "mx");
        test(context, "_imaps._tcp.gmail.com", "srv");
        test(context, "gmail.com", "txt");
        test(context, "outlook.office365.com", "a");
        test(context, "outlook.office365.com", "aaaa");
        test(context, "posteo.de", "a");
        test(context, "non.existent.tld", "a");
        test(context, "rubbish", "a");
    }

    private static void test(Context context, String name, String type) {
        DnsRecord[] records = lookup(context, name, type);
        Log.w("DNS test " + name + ":" + type);
        if (records.length == 0)
            Log.w("- no records");
        for (DnsRecord record : records)
            Log.w("- " + record);
    }

    private static class DoTDataSource extends AbstractDnsDataSource {
        private String host;

        private DoTDataSource() {
            super();
        }

        DoTDataSource(String host) {
            super();
            this.host = host;
        }

        @Override
        public DnsQueryResult query(DnsMessage query, InetAddress address, int port) throws IOException {
            // https://datatracker.ietf.org/doc/html/rfc7858
            try (Socket socket = SSLSocketFactory.getDefault().createSocket()) {
                socket.connect(new InetSocketAddress(host, 853), timeout);
                socket.setSoTimeout(timeout);

                byte[] out = query.toArray();
                OutputStream os = socket.getOutputStream();
                os.write(new byte[]{(byte) (out.length / 256), (byte) (out.length % 256)});
                os.write(out);

                InputStream is = socket.getInputStream();
                int hi = is.read();
                if (hi < 0)
                    throw new IOException("EOF");
                int lo = is.read();
                if (lo < 0)
                    throw new IOException("EOF");

                int len = hi * 256 + lo;
                byte[] in = new byte[len];
                int i = 0;
                while (i < len) {
                    int r = is.read(in, i, len - i);
                    if (r < 0)
                        throw new IOException("EOF");
                    i += r;
                }

                return new StandardDnsQueryResult(
                        address, port,
                        DnsQueryResult.QueryMethod.tcp,
                        query,
                        new DnsMessage(in));
            }
        }
    }

    private static class DoHDataSource extends AbstractDnsDataSource {
        private String host;

        private DoHDataSource() {
            super();
        }

        DoHDataSource(String host) {
            super();
            this.host = host;
        }

        @Override
        public DnsQueryResult query(DnsMessage query, InetAddress address, int port) throws IOException {
            // https://datatracker.ietf.org/doc/html/rfc8484
            HttpsURLConnection request = null;
            try {
                URL url = new URL("https://" + host + "/dns-query?dns=" +
                        Base64.encodeToString(query.toArray(), Base64.NO_PADDING | Base64.NO_WRAP));
                request = (HttpsURLConnection) url.openConnection();
                request.setRequestMethod("GET");
                request.setRequestProperty("Content-Type", "application/dns-message");
                request.setReadTimeout(timeout);
                request.setConnectTimeout(timeout);
                request.setDoInput(true);
                request.connect();

                int status = request.getResponseCode();
                if (status != HttpsURLConnection.HTTP_OK)
                    throw new IOException("Error " + status + ": " + request.getResponseMessage());

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.writeTo(request.getOutputStream());

                return new StandardDnsQueryResult(
                        address, port,
                        DnsQueryResult.QueryMethod.tcp,
                        query,
                        new DnsMessage(bos.toByteArray()));
            } finally {
                if (request != null)
                    request.disconnect();
            }
        }
    }

    private static class AndroidDataSource extends AbstractDnsDataSource {
        private IOException ex;
        private DnsQueryResult result;

        @Override
        public DnsQueryResult query(DnsMessage query, InetAddress address, int port) throws IOException {
            Semaphore sem = new Semaphore(0);
            DnsResolver resolver = DnsResolver.getInstance();
            Log.i("DNS Android query=" + query);
            resolver.rawQuery(
                    null,
                    query.toArray(),
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
                        public void onAnswer(@NonNull byte[] bytes, int rcode) {
                            try {
                                Log.i("DNS rcode=" + rcode);
                                DnsMessage answer = new DnsMessage(bytes)
                                        .asBuilder()
                                        .setResponseCode(DnsMessage.RESPONSE_CODE.getResponseCode(rcode))
                                        .build();
                                result = new StandardDnsQueryResult(
                                        address, port,
                                        DnsQueryResult.QueryMethod.udp,
                                        query,
                                        answer);
                            } catch (Throwable e) {
                                ex = new IOException(e.getMessage(), e);
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
                if (!sem.tryAcquire(timeout, TimeUnit.MILLISECONDS))
                    ex = new IOException("timeout");
            } catch (InterruptedException e) {
                ex = new IOException("interrupted");
            }

            try {
                if (ex == null) {
                    Log.i("DNS Android answer=" + result);
                    return result;
                } else {
                    Log.i(ex);
                    throw ex;
                }
            } finally {
                ex = null;
                result = null;
            }
        }
    }

    private static class AuthoritiveDataSource extends AbstractDnsDataSource {
        private final DnsDataSource delegate;

        AuthoritiveDataSource(DnsDataSource delegate) {
            this.delegate = delegate;
        }

        @Override
        public DnsQueryResult query(DnsMessage message, InetAddress address, int port) throws IOException {
            DnsQueryResult result = delegate.query(message, address, port);
            if (result == null)
                throw new UnknownHostException();
            DnsMessage answer = new DnsMessage(result.response.toArray())
                    .asBuilder()
                    .setRecursionAvailable(true)
                    .build();
            return new StandardDnsQueryResult(address, port, result.queryMethod, result.query, answer);
        }
    }

    static class DnsRecord {
        String query;
        String response;
        Integer port;
        Integer priority;
        Integer weight;
        Boolean secure;
        Boolean authentic;
        InetAddress address;

        DnsRecord(String response) {
            this.response = response;
        }

        DnsRecord(InetAddress address) {
            this.address = address;
            this.response = address.getHostAddress();
        }

        DnsRecord(String response, int port) {
            this.response = response;
            this.port = port;
        }

        DnsRecord(String response, int port, int priority, int weight) {
            this.response = response;
            this.port = port;
            this.priority = priority;
            this.weight = weight;
        }

        @NonNull
        @Override
        public String toString() {
            return query + "=" + response + ":" + port + " " + priority + "/" + weight +
                    " secure=" + secure + " authentic=" + authentic;
        }
    }
}
