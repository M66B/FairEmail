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
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class EmailProvider {
    public String id;
    public String name;
    public List<String> domain;
    public int order;
    public String type;
    public int keepalive;
    public boolean partial;
    public boolean useip;
    public boolean appPassword;
    public String link;
    public Server imap = new Server();
    public Server smtp = new Server();
    public OAuth oauth;
    public UserType user = UserType.EMAIL;
    public StringBuilder documentation; // html

    enum Discover {ALL, IMAP, SMTP}

    enum UserType {LOCAL, EMAIL}

    private static final int SCAN_TIMEOUT = 5 * 1000; // milliseconds
    private static final int ISPDB_TIMEOUT = 15 * 1000; // milliseconds

    private static final List<String> PROPRIETARY = Collections.unmodifiableList(Arrays.asList(
            "protonmail.ch",
            "protonmail.com",
            "tutanota.com",
            "tutanota.de",
            "tutamail.com", // tutanota
            "tuta.io", // tutanota
            "keemail.me" // tutanota
    ));
    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(0, "provider");

    private EmailProvider() {
    }

    EmailProvider(String name) {
        this.name = name;
    }

    private void validate() throws UnknownHostException {
        if (TextUtils.isEmpty(this.imap.host) || this.imap.port <= 0 ||
                TextUtils.isEmpty(this.smtp.host) || this.smtp.port <= 0)
            throw new UnknownHostException(this.name + " invalid");
    }

    static List<String> getDomainNames(Context context) {
        List<String> result = new ArrayList<>();

        for (String domain : PROPRIETARY)
            result.add(domain.replace(".", "\\."));

        List<EmailProvider> providers = loadProfiles(context);
        for (EmailProvider provider : providers)
            if (provider.domain != null)
                for (String domain : provider.domain)
                    if (!result.contains(domain))
                        result.add(domain);

        return result;
    }

    static List<EmailProvider> loadProfiles(Context context) {
        List<EmailProvider> result = null;
        try {
            EmailProvider provider = null;
            XmlResourceParser xml = context.getResources().getXml(R.xml.providers);
            int eventType = xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = xml.getName();
                    if ("providers".equals(name))
                        result = new ArrayList<>();
                    else if ("provider".equals(name)) {
                        provider = new EmailProvider();
                        provider.id = xml.getAttributeValue(null, "id");
                        provider.name = xml.getAttributeValue(null, "name");
                        String domain = xml.getAttributeValue(null, "domain");
                        if (domain != null)
                            provider.domain = Arrays.asList(domain.split(","));
                        provider.order = xml.getAttributeIntValue(null, "order", Integer.MAX_VALUE);
                        provider.keepalive = xml.getAttributeIntValue(null, "keepalive", 0);
                        provider.partial = xml.getAttributeBooleanValue(null, "partial", true);
                        provider.useip = xml.getAttributeBooleanValue(null, "useip", true);
                        provider.appPassword = xml.getAttributeBooleanValue(null, "appPassword", false);
                        provider.link = xml.getAttributeValue(null, "link");
                        provider.type = xml.getAttributeValue(null, "type");
                        String user = xml.getAttributeValue(null, "user");
                        if ("local".equals(user))
                            provider.user = UserType.LOCAL;
                        else if ("email".equals(user))
                            provider.user = UserType.EMAIL;
                    } else if ("imap".equals(name)) {
                        provider.imap.host = xml.getAttributeValue(null, "host");
                        provider.imap.port = xml.getAttributeIntValue(null, "port", 0);
                        provider.imap.starttls = xml.getAttributeBooleanValue(null, "starttls", false);
                    } else if ("smtp".equals(name)) {
                        provider.smtp.host = xml.getAttributeValue(null, "host");
                        provider.smtp.port = xml.getAttributeIntValue(null, "port", 0);
                        provider.smtp.starttls = xml.getAttributeBooleanValue(null, "starttls", false);
                    } else if ("oauth".equals(name)) {
                        provider.oauth = new OAuth();
                        provider.oauth.enabled = xml.getAttributeBooleanValue(null, "enabled", false);
                        provider.oauth.askAccount = xml.getAttributeBooleanValue(null, "askAccount", false);
                        provider.oauth.clientId = xml.getAttributeValue(null, "clientId");
                        provider.oauth.clientSecret = xml.getAttributeValue(null, "clientSecret");
                        provider.oauth.pcke = xml.getAttributeBooleanValue(null, "pcke", false);
                        provider.oauth.scopes = xml.getAttributeValue(null, "scopes").split(",");
                        provider.oauth.authorizationEndpoint = xml.getAttributeValue(null, "authorizationEndpoint");
                        provider.oauth.tokenEndpoint = xml.getAttributeValue(null, "tokenEndpoint");
                        provider.oauth.redirectUri = xml.getAttributeValue(null, "redirectUri");
                    } else
                        throw new IllegalAccessException(name);
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("provider".equals(xml.getName())) {
                        result.add(provider);
                        provider = null;
                    }
                }

                eventType = xml.next();
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(result, new Comparator<EmailProvider>() {
            @Override
            public int compare(EmailProvider p1, EmailProvider p2) {
                int o = Integer.compare(p1.order, p2.order);
                if (o == 0)
                    return collator.compare(p1.name, p2.name);
                else
                    return o;
            }
        });

        return result;
    }

    static EmailProvider getProvider(Context context, String id) throws FileNotFoundException {
        for (EmailProvider provider : loadProfiles(context))
            if (id.equals(provider.id))
                return provider;

        throw new FileNotFoundException("provider id=" + id);
    }

    @NonNull
    static EmailProvider fromDomain(Context context, String domain, Discover discover) throws IOException {
        return fromEmail(context, domain, discover);
    }

    @NonNull
    static EmailProvider fromEmail(Context context, String email, Discover discover) throws IOException {
        int at = email.indexOf("@");
        String domain = (at < 0 ? email : email.substring(at + 1));
        if (at < 0)
            email = "someone@" + domain;

        if (TextUtils.isEmpty(domain))
            throw new UnknownHostException(context.getString(R.string.title_setup_no_settings, domain));

        if (PROPRIETARY.contains(domain))
            throw new IllegalArgumentException(context.getString(R.string.title_no_standard));

        List<EmailProvider> providers = loadProfiles(context);
        for (EmailProvider provider : providers)
            if (provider.domain != null)
                for (String d : provider.domain)
                    if (domain.toLowerCase(Locale.ROOT).matches(d)) {
                        Log.i("Provider from domain=" + domain + " (" + d + ")");
                        return provider;
                    }

        EmailProvider autoconfig = null;
        try {
            autoconfig = _fromDomain(context, domain.toLowerCase(Locale.ROOT), email, discover);
        } catch (Throwable ex) {
            Log.w(ex);

            try {
                // Retry at MX server addresses
                DnsHelper.DnsRecord[] records = DnsHelper.lookup(context, domain, "mx");
                for (DnsHelper.DnsRecord record : records) {
                    String target = record.name;
                    while (autoconfig == null && target != null && target.indexOf('.') > 0) {
                        try {
                            autoconfig = _fromDomain(context, target.toLowerCase(Locale.ROOT), email, discover);
                        } catch (Throwable ex1) {
                            Log.w(ex1);
                            int dot = target.indexOf('.');
                            target = target.substring(dot + 1);
                        }
                    }
                    if (autoconfig != null)
                        break;
                }
            } catch (Throwable ex1) {
                Log.w(ex1);
            }

            if (autoconfig == null)
                throw ex;
        }

        // Always prefer built-in profiles
        // - ISPDB is not always correct
        // - documentation links
        for (EmailProvider provider : providers)
            if (provider.imap.host.equals(autoconfig.imap.host) ||
                    provider.smtp.host.equals(autoconfig.smtp.host)) {
                Log.i("Replacing auto config by profile=" + provider.name);
                return provider;
            }

        // https://docs.aws.amazon.com/workmail/latest/userguide/using_IMAP_client.html
        if (autoconfig.imap.host != null &&
                autoconfig.imap.host.endsWith(".awsapps.com"))
            autoconfig.partial = false;

        return autoconfig;
    }

    @NonNull
    private static EmailProvider _fromDomain(Context context, String domain, String email, Discover discover) throws IOException {
        try {
            // Assume the provider knows best
            Log.i("Provider from DNS domain=" + domain);
            return fromDNS(context, domain, discover);
        } catch (Throwable ex) {
            Log.w(ex);
            try {
                // Check ISPDB
                Log.i("Provider from ISPDB domain=" + domain);
                return fromISPDB(context, domain, email);
            } catch (Throwable ex1) {
                Log.w(ex1);
                try {
                    // Scan ports
                    Log.i("Provider from template domain=" + domain);
                    return fromTemplate(context, domain, discover);
                } catch (Throwable ex2) {
                    Log.w(ex2);
                    throw new UnknownHostException(context.getString(R.string.title_setup_no_settings, domain));
                }
            }
        }
    }

    @NonNull
    private static EmailProvider fromISPDB(Context context, String domain, String email) throws IOException, XmlPullParserException {
        // https://wiki.mozilla.org/Thunderbird:Autoconfiguration
        try {
            URL url = new URL("https://autoconfig." + domain + "/mail/config-v1.1.xml?emailaddress=" + email);
            return getISPDB(domain, url);
        } catch (Throwable ex) {
            Log.w(ex);
        }

        try {
            URL url = new URL("https://" + domain + "/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=" + email);
            return getISPDB(domain, url);
        } catch (Throwable ex) {
            Log.w(ex);
        }

        URL url = new URL("https://autoconfig.thunderbird.net/v1.1/" + domain);
        return getISPDB(domain, url);
    }

    @NonNull
    private static EmailProvider getISPDB(String domain, URL url) throws IOException, XmlPullParserException {
        EmailProvider provider = new EmailProvider(domain);

        HttpURLConnection request = null;
        try {
            Log.i("Fetching " + url);

            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("GET");
            request.setReadTimeout(ISPDB_TIMEOUT);
            request.setConnectTimeout(ISPDB_TIMEOUT);
            request.setDoInput(true);
            request.connect();

            // https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xml = factory.newPullParser();
            xml.setInput(new InputStreamReader(request.getInputStream()));

            boolean imap = false;
            boolean smtp = false;
            String href = null;
            String title = null;
            int eventType = xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = xml.getName();
                    if ("displayShortName".equals(name)) {
                        // <displayShortName>GMail</displayShortName>
                        eventType = xml.next();
                        if (eventType == XmlPullParser.TEXT) {
                            String display = xml.getText();
                            Log.i("Name=" + display);
                            provider.name = display;
                        }
                        continue;

                    } else if ("incomingServer".equals(name)) {
                        // <incomingServer type="imap">
                        //   <hostname>imap.gmail.com</hostname>
                        //   <port>993</port>
                        //   <socketType>SSL</socketType>
                        //   <username>%EMAILADDRESS%</username>
                        //   <authentication>OAuth2</authentication>
                        //   <authentication>password-cleartext</authentication>
                        // </incomingServer>
                        imap = "imap".equals(xml.getAttributeValue(null, "type"));

                    } else if ("outgoingServer".equals(name)) {
                        // <outgoingServer type="smtp">
                        //   <hostname>smtp.gmail.com</hostname>
                        //   <port>465</port>
                        //   <socketType>SSL</socketType>
                        //   <username>%EMAILADDRESS%</username>
                        //   <authentication>OAuth2</authentication>
                        //   <authentication>password-cleartext</authentication>
                        // </outgoingServer>
                        smtp = "smtp".equals(xml.getAttributeValue(null, "type"));

                    } else if ("hostname".equals(name)) {
                        eventType = xml.next();
                        if (eventType == XmlPullParser.TEXT) {
                            String host = xml.getText();
                            Log.i("Host=" + host);
                            if (imap)
                                provider.imap.host = host;
                            else if (smtp)
                                provider.smtp.host = host;
                        }
                        continue;

                    } else if ("port".equals(name)) {
                        eventType = xml.next();
                        if (eventType == XmlPullParser.TEXT) {
                            String port = xml.getText();
                            Log.i("Port=" + port);
                            if (imap) {
                                provider.imap.port = Integer.parseInt(port);
                                provider.imap.starttls = (provider.imap.port == 143);
                            } else if (smtp) {
                                provider.smtp.port = Integer.parseInt(port);
                                provider.smtp.starttls = (provider.smtp.port == 587);
                            }
                        }
                        continue;

                    } else if ("socketType".equals(name)) {
                        eventType = xml.next();
                        if (eventType == XmlPullParser.TEXT) {
                            String socket = xml.getText();
                            Log.i("Socket=" + socket);
                            if ("SSL".equals(socket)) {
                                if (imap)
                                    provider.imap.starttls = false;
                                else if (smtp)
                                    provider.smtp.starttls = false;
                            } else if ("STARTTLS".equals(socket)) {
                                if (imap)
                                    provider.imap.starttls = true;
                                else if (smtp)
                                    provider.smtp.starttls = true;
                            } else
                                Log.w("Unknown socket type=" + socket);
                        }
                        continue;

                    } else if ("username".equals(name)) {
                        eventType = xml.next();
                        if (eventType == XmlPullParser.TEXT) {
                            String username = xml.getText();
                            Log.i("Username=" + username);
                            if ("%EMAILADDRESS%".equals(username))
                                provider.user = UserType.EMAIL;
                            else if ("%EMAILLOCALPART%".equals(username))
                                provider.user = UserType.LOCAL;
                            else
                                Log.w("Unknown username type=" + username);
                        }
                        continue;

                    } else if ("enable".equals(name)) {
                        // <enable visiturl="https://mail.google.com/mail/?ui=2&shva=1#settings/fwdandpop">
                        //   <instruction>You need to enable IMAP access</instruction>
                        // </enable>
                        href = xml.getAttributeValue(null, "visiturl");
                        title = null;

                    } else if ("documentation".equals(name)) {
                        // <documentation url="http://mail.google.com/support/bin/answer.py?answer=13273">
                        //   <descr>How to enable IMAP/POP3 in GMail</descr>
                        // </documentation>
                        href = xml.getAttributeValue(null, "url");
                        title = null;

                    } else if ("instruction".equals(name) || "descr".equals(name)) {
                        if (href != null) {
                            eventType = xml.next();
                            if (eventType == XmlPullParser.TEXT) {
                                if (title == null)
                                    title = "";
                                else
                                    title += "<br>";
                                title += xml.getText();
                            }
                            continue;
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    String name = xml.getName();
                    if ("incomingServer".equals(name))
                        imap = false;

                    else if ("outgoingServer".equals(name))
                        smtp = false;

                    else if ("enable".equals(name) || "documentation".equals(name)) {
                        if (href != null) {
                            if (title == null)
                                title = href;
                            addDocumentation(provider, href, title);
                            href = null;
                            title = null;
                        }
                    }
                }

                eventType = xml.next();
            }

            Log.i("imap=" + provider.imap.host + ":" + provider.imap.port + ":" + provider.imap.starttls);
            Log.i("smtp=" + provider.smtp.host + ":" + provider.smtp.port + ":" + provider.smtp.starttls);

            provider.validate();

            return provider;
        } finally {
            if (request != null)
                request.disconnect();
        }
    }

    @NonNull
    private static EmailProvider fromDNS(Context context, String domain, Discover discover) throws UnknownHostException {
        // https://tools.ietf.org/html/rfc6186
        EmailProvider provider = new EmailProvider(domain);

        if (discover == Discover.ALL || discover == Discover.IMAP) {
            try {
                // Identifies an IMAP server where TLS is initiated directly upon connection to the IMAP server.
                DnsHelper.DnsRecord[] records = DnsHelper.lookup(context, "_imaps._tcp." + domain, "srv");
                if (records.length == 0)
                    throw new UnknownHostException(domain);
                // ... service is not supported at all at a particular domain by setting the target of an SRV RR to "."
                provider.imap.host = records[0].name;
                provider.imap.port = records[0].port;
                provider.imap.starttls = false;
            } catch (UnknownHostException ex) {
                // Identifies an IMAP server that MAY ... require the MUA to use the "STARTTLS" command
                DnsHelper.DnsRecord[] records = DnsHelper.lookup(context, "_imap._tcp." + domain, "srv");
                if (records.length == 0)
                    throw new UnknownHostException(domain);
                provider.imap.host = records[0].name;
                provider.imap.port = records[0].port;
                provider.imap.starttls = (provider.imap.port == 143);
            }
        }

        if (discover == Discover.ALL || discover == Discover.SMTP) {
            // Note that this covers connections both with and without Transport Layer Security (TLS)
            DnsHelper.DnsRecord[] records = DnsHelper.lookup(context, "_submission._tcp." + domain, "srv");
            if (records.length == 0)
                throw new UnknownHostException(domain);
            provider.smtp.host = records[0].name;
            provider.smtp.port = records[0].port;
            provider.smtp.starttls = (provider.smtp.port == 587);
        }

        provider.validate();

        return provider;
    }

    @NonNull
    private static EmailProvider fromTemplate(Context context, String domain, Discover discover)
            throws ExecutionException, InterruptedException, UnknownHostException {
        // https://tools.ietf.org/html/rfc8314
        Server imap = null;
        Server smtp = null;

        if (discover == Discover.ALL || discover == Discover.IMAP) {
            List<Server> imaps = new ArrayList<>();
            // SSL
            imaps.add(new Server(domain, null, 993));
            imaps.add(new Server(domain, "imap", 993));
            imaps.add(new Server(domain, "mail", 993));
            imaps.add(new Server(domain, "mx", 993));
            // STARTTLS
            imaps.add(new Server(domain, null, 143));
            imaps.add(new Server(domain, "imap", 143));
            imaps.add(new Server(domain, "mail", 143));
            imaps.add(new Server(domain, "mx", 143));

            for (Server server : imaps)
                if (server.reachable.get()) {
                    imap = server;
                    break;
                }

            if (imap == null)
                throw new UnknownHostException(domain + " template");
        }

        if (discover == Discover.ALL || discover == Discover.SMTP) {
            List<Server> smtps = new ArrayList<>();
            // SSL
            smtps.add(new Server(domain, null, 465));
            smtps.add(new Server(domain, "smtp", 465));
            smtps.add(new Server(domain, "mail", 465));
            smtps.add(new Server(domain, "mx", 465));
            // STARTTLS
            smtps.add(new Server(domain, null, 587));
            smtps.add(new Server(domain, "smtp", 587));
            smtps.add(new Server(domain, "mail", 587));
            smtps.add(new Server(domain, "mx", 587));

            for (Server server : smtps)
                if (server.reachable.get()) {
                    smtp = server;
                    break;
                }

            if (smtp == null)
                throw new UnknownHostException(domain + " template");
        }


        EmailProvider provider = new EmailProvider();
        provider.name = domain;

        if (imap != null) {
            provider.imap.host = imap.host;
            provider.imap.port = imap.port;
            provider.imap.starttls = (imap.port == 143);
        }

        if (smtp != null) {
            provider.smtp.host = smtp.host;
            provider.smtp.port = smtp.port;
            provider.smtp.starttls = (smtp.port == 587);
        }

        return provider;
    }

    private static void addDocumentation(EmailProvider provider, String href, String title) {
        if (provider.documentation == null)
            provider.documentation = new StringBuilder();
        else
            provider.documentation.append("<br><br>");

        provider.documentation.append("<a href=\"").append(href).append("\">").append(title).append("</a>");
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static class Server {
        public String host;
        public int port;
        public boolean starttls;

        private Future<Boolean> reachable;

        private Server() {
        }

        private Server(String domain, String prefix, int port) {
            this.host = (prefix == null ? "" : prefix + ".") + domain;
            this.port = port;

            Log.i("Scanning " + host + ":" + port);
            this.reachable = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        for (InetAddress iaddr : InetAddress.getAllByName(host)) {
                            InetSocketAddress address = new InetSocketAddress(iaddr, Server.this.port);
                            try (Socket socket = new Socket()) {
                                Log.i("Connecting to " + address);
                                socket.connect(address, SCAN_TIMEOUT);
                                Log.i("Reachable " + address);
                                return true;
                            } catch (Throwable ex) {
                                Log.i("Unreachable " + address + ": " + Log.formatThrowable(ex));
                            }
                        }
                        return false;
                    } catch (Throwable ex) {
                        Log.w(ex);
                        return false;
                    }
                }
            });
        }

        @NonNull
        @Override
        public String toString() {
            return host + ":" + port;
        }
    }

    public static class OAuth {
        boolean enabled;
        boolean askAccount;
        String clientId;
        String clientSecret;
        boolean pcke;
        String[] scopes;
        String authorizationEndpoint;
        String tokenEndpoint;
        String redirectUri;
    }
}
