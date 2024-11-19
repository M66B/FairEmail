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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import static android.system.OsConstants.ECONNREFUSED;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class EmailProvider implements Parcelable {
    public String id;
    public String name;
    public String description;
    public boolean debug;
    public boolean enabled;
    public boolean alt;
    public List<String> domain;
    public List<String> mx;
    public int order;
    public String type;
    public int keepalive;
    public boolean noop;
    public boolean partial;
    public boolean raw;
    public boolean useip;
    public boolean appPassword;
    public String maxtls;
    public String link;
    public Server imap = new Server();
    public Server smtp = new Server();
    public Server pop;
    public OAuth oauth;
    public OAuth graph;
    public UserType user = UserType.EMAIL;
    public String username;
    public StringBuilder documentation; // html

    enum Discover {ALL, IMAP, SMTP}

    enum UserType {LOCAL, EMAIL, VALUE}

    private static List<EmailProvider> imported;

    private static final int SCAN_TIMEOUT = 10 * 1000; // milliseconds
    private static final int ISPDB_TIMEOUT = 10 * 1000; // milliseconds
    private static final int MS_TIMEOUT = 10 * 1000; // milliseconds

    private static final List<String> PROPRIETARY = Collections.unmodifiableList(Arrays.asList(
            "protonmail.ch",
            "protonmail.com",
            "proton.me",
            "tutanota.com",
            "tutanota.de",
            "tutamail.com", // tutanota
            "tuta.io", // tutanota
            "keemail.me", // tutanota
            "ctemplar.com",
            "cyberfear.com",
            "skiff.com",
            "tildamail.com",
            "criptext.com",
            "onmail.com"
    ));

    private EmailProvider() {
    }

    EmailProvider(String name) {
        this.name = name;
    }

    static void init(Context context) {
        Helper.getSerialExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(context.getFilesDir(), "providers.xml");
                    if (file.exists()) {
                        try (FileInputStream is = new FileInputStream(file)) {
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(is, null);
                            imported = parseProfiles(parser);
                        }
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    static void importProfiles(InputStream is, Context context) throws IOException {
        File file = new File(context.getFilesDir(), "providers.xml");
        try (OutputStream os = new FileOutputStream(file)) {
            Helper.copy(is, os);
        }

        init(context);
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
            if (provider.enabled && provider.domain != null)
                for (String domain : provider.domain)
                    if (!result.contains(domain))
                        result.add(domain);

        return result;
    }

    private static List<EmailProvider> loadProfiles(Context context) {
        List<EmailProvider> result = null;

        try {
            result = parseProfiles(context.getResources().getXml(R.xml.providers));
        } catch (Throwable ex) {
            Log.e(ex);
        }

        if (imported != null)
            result.addAll(imported);

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

    private static List<EmailProvider> parseProfiles(XmlPullParser xml) {
        List<EmailProvider> result = null;

        String lang = Locale.getDefault().getLanguage();

        try {
            EmailProvider provider = null;
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

                        provider.description = xml.getAttributeValue(null, "description");
                        if (provider.description == null)
                            provider.description = provider.name;

                        provider.debug = getAttributeBooleanValue(xml, "debug", false);
                        provider.enabled = getAttributeBooleanValue(xml, "enabled", true);
                        provider.alt = getAttributeBooleanValue(xml, "alt", false);

                        String domain = xml.getAttributeValue(null, "domain");
                        if (domain != null)
                            provider.domain = Arrays.asList(domain.split(","));

                        String mx = xml.getAttributeValue(null, "mx");
                        if (mx != null)
                            provider.mx = Arrays.asList(mx.split(","));

                        provider.order = getAttributeIntValue(xml, "order", Integer.MAX_VALUE);
                        provider.keepalive = getAttributeIntValue(xml, "keepalive", 0);
                        provider.noop = getAttributeBooleanValue(xml, "noop", false);
                        provider.partial = getAttributeBooleanValue(xml, "partial", true);
                        provider.raw = getAttributeBooleanValue(xml, "raw", false);
                        provider.useip = getAttributeBooleanValue(xml, "useip", true);
                        provider.appPassword = getAttributeBooleanValue(xml, "appPassword", false);
                        provider.maxtls = xml.getAttributeValue(null, "maxtls");
                        provider.link = xml.getAttributeValue(null, "link");

                        String documentation = xml.getAttributeValue(null, "documentation." + lang);
                        if (documentation == null)
                            documentation = xml.getAttributeValue(null, "documentation");
                        if (documentation != null)
                            provider.documentation = new StringBuilder(documentation);

                        provider.type = xml.getAttributeValue(null, "type");

                        String user = xml.getAttributeValue(null, "user");
                        if ("local".equals(user))
                            provider.user = UserType.LOCAL;
                        else if ("email".equals(user))
                            provider.user = UserType.EMAIL;
                        else {
                            if (!TextUtils.isEmpty(user)) {
                                provider.user = UserType.VALUE;
                                provider.username = user;
                            }
                        }
                    } else if ("imap".equals(name)) {
                        provider.imap.score = 100;
                        provider.imap.host = xml.getAttributeValue(null, "host");
                        provider.imap.port = getAttributeIntValue(xml, "port", 0);
                        provider.imap.starttls = getAttributeBooleanValue(xml, "starttls", false);
                    } else if ("smtp".equals(name)) {
                        provider.smtp.score = 100;
                        provider.smtp.host = xml.getAttributeValue(null, "host");
                        provider.smtp.port = getAttributeIntValue(xml, "port", 0);
                        provider.smtp.starttls = getAttributeBooleanValue(xml, "starttls", false);
                    } else if ("pop".equals(name)) {
                        provider.pop = new Server();
                        provider.pop.host = xml.getAttributeValue(null, "host");
                        provider.pop.port = getAttributeIntValue(xml, "port", 0);
                        provider.pop.starttls = getAttributeBooleanValue(xml, "starttls", false);
                    } else if ("oauth".equals(name)) {
                        String val = xml.getAttributeValue(null, "enabled");
                        boolean enabled = ("debug".equals(val) ? BuildConfig.DEBUG : Boolean.parseBoolean(val));

                        provider.oauth = new OAuth();
                        provider.oauth.enabled = enabled;
                        provider.oauth.askAccount = getAttributeBooleanValue(xml, "askAccount", false);
                        provider.oauth.clientId = xml.getAttributeValue(null, "clientId");
                        provider.oauth.clientSecret = xml.getAttributeValue(null, "clientSecret");
                        provider.oauth.scopes = xml.getAttributeValue(null, "scopes").split(",");
                        provider.oauth.authorizationEndpoint = xml.getAttributeValue(null, "authorizationEndpoint");
                        provider.oauth.tokenEndpoint = xml.getAttributeValue(null, "tokenEndpoint");
                        provider.oauth.tokenScopes = getAttributeBooleanValue(xml, "tokenScopes", false);
                        provider.oauth.redirectUri = xml.getAttributeValue(null, "redirectUri");
                        provider.oauth.privacy = xml.getAttributeValue(null, "privacy");
                        provider.oauth.prompt = xml.getAttributeValue(null, "prompt");
                    } else if ("graph".equals(name)) {
                        provider.graph = new OAuth();
                        provider.graph.enabled = getAttributeBooleanValue(xml, "enabled", false);
                        provider.graph.askAccount = getAttributeBooleanValue(xml, "askAccount", false);
                        provider.graph.clientId = xml.getAttributeValue(null, "clientId");
                        provider.graph.clientSecret = xml.getAttributeValue(null, "clientSecret");
                        provider.graph.scopes = xml.getAttributeValue(null, "scopes").split(",");
                        provider.graph.authorizationEndpoint = xml.getAttributeValue(null, "authorizationEndpoint");
                        provider.graph.tokenEndpoint = xml.getAttributeValue(null, "tokenEndpoint");
                        provider.graph.tokenScopes = getAttributeBooleanValue(xml, "tokenScopes", false);
                        provider.graph.redirectUri = xml.getAttributeValue(null, "redirectUri");
                        provider.graph.privacy = xml.getAttributeValue(null, "privacy");
                        provider.graph.prompt = xml.getAttributeValue(null, "prompt");
                    } else if ("parameter".equals(name)) {
                        if (provider.oauth.parameters == null)
                            provider.oauth.parameters = new LinkedHashMap<>();
                        provider.oauth.parameters.put(
                                xml.getAttributeValue(null, "key"),
                                xml.getAttributeValue(null, "value"));
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

        return result;
    }

    private static boolean getAttributeBooleanValue(XmlPullParser parser, String name, boolean defaultValue) {
        String value = parser.getAttributeValue(null, name);
        return (value == null ? defaultValue : Boolean.parseBoolean(value));
    }

    private static int getAttributeIntValue(XmlPullParser parser, String name, int defaultValue) {
        String value = parser.getAttributeValue(null, name);
        return (value == null ? defaultValue : Integer.parseInt(value));
    }

    static EmailProvider getProvider(Context context, String id) throws FileNotFoundException {
        if (id != null)
            for (EmailProvider provider : loadProfiles(context))
                if (id.equals(provider.id))
                    return provider;

        throw new FileNotFoundException("provider id=" + id);
    }

    static EmailProvider getProviderByHost(Context context, @NonNull String host) {
        for (EmailProvider provider : loadProfiles(context)) {
            if (provider.imap != null && host.equals(provider.imap.host))
                return provider;
            if (provider.pop != null && host.equals(provider.pop.host))
                return provider;
            if (provider.smtp != null && host.equals(provider.smtp.host))
                return provider;
        }

        return null;
    }

    // For user interface
    static List<EmailProvider> getProviders(Context context) {
        return getProviders(context, false);
    }

    static List<EmailProvider> getProviders(Context context, boolean debug) {
        List<EmailProvider> result = new ArrayList<>();
        for (EmailProvider provider : loadProfiles(context))
            if (provider.enabled || (provider.debug && debug))
                result.add(provider);
        return result;
    }

    @NonNull
    static List<EmailProvider> fromDomain(Context context, String domain, Discover discover) throws IOException {
        return fromEmail(context, domain, discover,
                new IDiscovery() {
                    @Override
                    public void onStatus(String status) {
                        // Do nothing
                    }
                });
    }

    @NonNull
    static List<EmailProvider> fromEmail(Context context, String _email, Discover discover, IDiscovery intf) throws IOException {
        int at = _email.indexOf('@');
        String domain = (at < 0 ? _email : _email.substring(at + 1));
        String email = (at < 0 ? "someone@" + domain : _email);

        if (TextUtils.isEmpty(domain))
            throw new UnknownHostException(context.getString(R.string.title_setup_no_settings, domain));

        if (PROPRIETARY.contains(domain))
            throw new IllegalArgumentException(context.getString(R.string.title_no_standard));

        List<EmailProvider> providers = loadProfiles(context);
        for (EmailProvider provider : providers)
            if (provider.enabled && provider.domain != null)
                for (String d : provider.domain)
                    if (domain.toLowerCase(Locale.ROOT).matches(d)) {
                        EntityLog.log(context, "Provider from domain=" + domain + " (" + d + ")");
                        return Arrays.asList(provider);
                    }

        try {
            intf.onStatus("NS " + domain);
            DnsHelper.DnsRecord[] ns = DnsHelper.lookup(context, domain, "ns");
            for (DnsHelper.DnsRecord record : ns)
                for (EmailProvider provider : providers)
                    if (provider.mx != null)
                        for (String mx : provider.mx)
                            if (record.response.matches(mx))
                                return Arrays.asList(provider);
        } catch (Throwable ex) {
            Log.w(ex);
        }

        List<EmailProvider> candidates =
                new ArrayList<>(_fromDomain(context, domain.toLowerCase(Locale.ROOT), email, discover, intf));

        if (false) // Unsafe: the password could be sent to an unrelated email server
            try {
                InetAddress iaddr = DnsHelper.getByName(context, domain.toLowerCase(Locale.ROOT));
                List<String> commonNames =
                        ConnectionHelper.getCommonNames(context, domain.toLowerCase(Locale.ROOT), 443, SCAN_TIMEOUT);
                EntityLog.log(context, "Website common names=" + TextUtils.join(",", commonNames));
                for (String commonName : commonNames) {
                    String altName = commonName;
                    if (altName.startsWith("*.")) {
                        altName = altName.substring(2);
                        if (commonNames.contains(altName))
                            continue;
                    }

                    if (!altName.equalsIgnoreCase(domain))
                        try {
                            InetAddress ialt = DnsHelper.getByName(context, altName);
                            if (!ialt.equals(iaddr)) {
                                EntityLog.log(context, "Using website common name=" + altName);
                                candidates.addAll(_fromDomain(context, altName.toLowerCase(Locale.ROOT), email, discover, intf));
                            }
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                }
            } catch (Throwable ex) {
                Log.w(ex);
            }

        try {
            List<DnsHelper.DnsRecord> records = new ArrayList<>();
            try {
                intf.onStatus("MX " + domain);
                DnsHelper.DnsRecord[] mx = DnsHelper.lookup(context, domain, "mx");
                if (mx.length > 0)
                    records.add(mx[0]);
            } catch (Throwable ex) {
                Log.w(ex);
            }

            for (DnsHelper.DnsRecord record : records)
                if (!TextUtils.isEmpty(record.response)) {
                    String target = record.response.toLowerCase(Locale.ROOT);
                    EntityLog.log(context, "MX target=" + target);

                    for (EmailProvider provider : providers) {
                        if (provider.enabled && provider.mx != null)
                            for (String mx : provider.mx)
                                if (target.matches(mx)) {
                                    EntityLog.log(context, "From MX domain=" + domain);
                                    candidates.add(provider);
                                    break;
                                }

                        String mxroot = UriHelper.getRootDomain(context, target);
                        String proot = UriHelper.getRootDomain(context, provider.imap.host);
                        if (mxroot != null && mxroot.equalsIgnoreCase(proot)) {
                            EntityLog.log(context, "From MX host=" + provider.imap.host);
                            candidates.add(provider);
                            break;
                        }
                    }

                    while (candidates.size() == 0 && target.indexOf('.') > 0) {
                        candidates.addAll(_fromDomain(context, target, email, discover, intf));
                        int dot = target.indexOf('.');
                        target = target.substring(dot + 1);
                        if (UriHelper.isTld(context, target))
                            break;
                    }
                }

            for (DnsHelper.DnsRecord record : records)
                try {
                    String target = record.response.toLowerCase(Locale.ROOT);
                    DnsHelper.getByName(context, target);

                    EmailProvider mx1 = new EmailProvider(domain);
                    mx1.imap.score = 0;
                    mx1.imap.host = target;
                    mx1.imap.port = 993;
                    mx1.imap.starttls = false;
                    mx1.smtp.score = 0;
                    mx1.smtp.host = target;
                    mx1.smtp.port = 587;
                    mx1.smtp.starttls = true;
                    candidates.add(mx1);

                    EmailProvider mx2 = new EmailProvider(domain);
                    mx2.imap.score = 0;
                    mx2.imap.host = target;
                    mx2.imap.port = 993;
                    mx2.imap.starttls = false;
                    mx2.smtp.score = 0;
                    mx2.smtp.host = target;
                    mx2.smtp.port = 465;
                    mx2.smtp.starttls = false;
                    candidates.add(mx2);

                    break;
                } catch (UnknownHostException ex) {
                    Log.w(ex);
                }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        if (candidates.size() == 0)
            throw new UnknownHostException(context.getString(R.string.title_setup_no_settings, domain));

        for (EmailProvider candidate : candidates) {
            // Always prefer built-in profiles
            // - ISPDB is not always correct
            // - documentation links
            for (EmailProvider provider : providers)
                if (provider.enabled &&
                        provider.imap.host.equals(candidate.imap.host) ||
                        provider.smtp.host.equals(candidate.smtp.host)) {
                    EntityLog.log(context, "Replacing auto config host by profile=" + provider.name);
                    return Arrays.asList(provider);
                } else if (provider.enabled && provider.mx != null)
                    for (String mx : provider.mx)
                        if ((candidate.imap.host != null && candidate.imap.host.matches(mx)) ||
                                (candidate.smtp.host != null && candidate.smtp.host.matches(mx))) {
                            EntityLog.log(context, "Replacing auto config MC by profile=" + provider.name);
                            return Arrays.asList(provider);
                        }

            // https://help.dreamhost.com/hc/en-us/articles/214918038-Email-client-configuration-overview
            if (candidate.imap.host != null &&
                    candidate.imap.host.endsWith(".dreamhost.com"))
                candidate.imap.host = "imap.dreamhost.com";

            if (candidate.smtp.host != null &&
                    candidate.smtp.host.endsWith(".dreamhost.com"))
                candidate.smtp.host = "smtp.dreamhost.com";

            // https://docs.aws.amazon.com/workmail/latest/userguide/using_IMAP_client.html
            if (candidate.imap.host != null &&
                    candidate.imap.host.endsWith(".awsapps.com"))
                candidate.partial = false;
        }

        // Sort by score
        Collections.sort(candidates, new Comparator<EmailProvider>() {
            @Override
            public int compare(EmailProvider p1, EmailProvider p2) {
                return -Integer.compare(p1.getScore(email), p2.getScore(email));
            }
        });

        // Log candidates
        for (EmailProvider candidate : candidates)
            EntityLog.log(context, "Candidate" +
                    " score=" + candidate.getScore(email) +
                    " imap=" + candidate.imap +
                    " smtp=" + candidate.smtp +
                    " user=" + candidate.username);

        // Remove duplicates
        List<EmailProvider> result = new ArrayList<>();
        for (EmailProvider candidate : candidates)
            if (!result.contains(candidate))
                result.add(candidate);

        Log.i("Candidates=" + result.size());

        return result;
    }

    @NonNull
    private static List<EmailProvider> _fromDomain(Context context, String domain, String email, Discover discover, IDiscovery intf) {
        List<EmailProvider> result = new ArrayList<>();

        try {
            // Assume the provider knows best
            Log.i("Provider from DNS domain=" + domain);
            result.add(fromDNS(context, domain, discover, intf));
        } catch (Throwable ex) {
            Log.w(ex);
        }

        try {
            // Check ISPDB
            Log.i("Provider from ISPDB domain=" + domain);
            result.add(fromISPDB(context, domain, email, intf));
        } catch (Throwable ex) {
            Log.w(ex);
        }

        try {
            // Check Microsoft auto discovery
            Log.i("Provider from MS domain=" + domain);
            result.add(fromMSAutodiscovery(context, domain, email, intf));
        } catch (Throwable ex) {
            Log.w(ex);
        }

        try {
            // Scan ports
            Log.i("Provider from scan domain=" + domain);
            result.add(fromScan(context, domain, discover, intf));
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return result;
    }

    @NonNull
    private static EmailProvider fromISPDB(Context context, String domain, String email, IDiscovery intf) throws Throwable {
        // https://wiki.mozilla.org/Thunderbird:Autoconfiguration
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean open_safe = prefs.getBoolean("open_safe", false);
        for (String link : Misc.getISPDBUrls(context, domain, email))
            try {
                URL url = new URL(link);
                return getISPDB(context, domain, url, open_safe, intf);
            } catch (Throwable ex) {
                Log.i(ex);
            }

        URL url = new URL("https://autoconfig.thunderbird.net/v1.1/" + domain);
        return getISPDB(context, domain, url, true, intf);
    }

    @NonNull
    private static EmailProvider getISPDB(Context context, String domain, URL url, boolean open_safe, IDiscovery intf) throws IOException, XmlPullParserException {
        EmailProvider provider = new EmailProvider(domain);

        HttpURLConnection request = null;
        try {
            Log.i("Fetching " + url);
            intf.onStatus("ISPDB " + url);

            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("GET");
            request.setReadTimeout(ISPDB_TIMEOUT);
            request.setConnectTimeout(ISPDB_TIMEOUT);
            request.setDoInput(true);
            ConnectionHelper.setUserAgent(context, request);

            if (request instanceof HttpsURLConnection) {
                if (!open_safe)
                    ((HttpsURLConnection) request).setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
            } else {
                if (open_safe)
                    throw new IOException("https required url=" + url);
            }

            request.connect();

            int status = request.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
                throw new FileNotFoundException("Error " + status + ": " + request.getResponseMessage());

            // https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xml = factory.newPullParser();
            xml.setInput(new InputStreamReader(request.getInputStream()));

            EntityLog.log(context, "Parsing " + url);

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
                            if (imap) {
                                provider.imap.score = 20;
                                provider.imap.host = host;
                            } else if (smtp) {
                                provider.smtp.score = 20;
                                provider.smtp.host = host;
                            }
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
                            else {
                                if (!TextUtils.isEmpty(username)) {
                                    provider.user = UserType.VALUE;
                                    provider.username = username;
                                }
                            }
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

            provider.validate();

            return provider;
        } finally {
            if (request != null)
                request.disconnect();
        }
    }

    @NonNull
    private static EmailProvider fromMSAutodiscovery(Context context, String domain, String email, IDiscovery intf) throws UnknownHostException {
        // https://learn.microsoft.com/en-us/Exchange/architecture/client-access/autodiscover
        // https://github.com/gronke/email-autodiscover/blob/master/mail/autodiscover.xml
        // Example: https://mail.de/autodiscover/autodiscover.xml
        for (String link : Misc.getMSUrls(context, domain, email))
            try {
                URL url = new URL(link);
                return getMSAutodiscovery(context, domain, email, url, true, intf);
            } catch (Throwable ex) {
                Log.i(ex);
            }

        throw new UnknownHostException(domain);
    }

    private static EmailProvider getMSAutodiscovery(Context context, String domain, String email, URL url, boolean unsafe, IDiscovery intf)
            throws IOException, XmlPullParserException, ParserConfigurationException, TransformerException {
        EmailProvider provider = new EmailProvider(domain);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element xAutodiscover = doc.createElement("Autodiscover");
        xAutodiscover.setAttribute("xmlns", "http://schemas.microsoft.com/exchange/autodiscover/outlook/requestschema/2006");
        doc.appendChild(xAutodiscover);

        Element xRequest = doc.createElement("Request");
        xAutodiscover.appendChild(xRequest);

        Element xEMailAddress = doc.createElement("EMailAddress");
        xEMailAddress.setTextContent(email);
        xRequest.appendChild(xEMailAddress);

        Element xAcceptableResponseSchema = doc.createElement("AcceptableResponseSchema");
        xAcceptableResponseSchema.setTextContent("http://schemas.microsoft.com/exchange/autodiscover/outlook/responseschema/2006a");
        xRequest.appendChild(xAcceptableResponseSchema);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        transformer.transform(source, result);

        HttpURLConnection request = null;
        try {
            Log.i("Fetching " + url);
            intf.onStatus("MS " + url);

            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("POST");
            request.setReadTimeout(MS_TIMEOUT);
            request.setConnectTimeout(MS_TIMEOUT);
            request.setDoOutput(true);
            request.setDoInput(true);
            ConnectionHelper.setUserAgent(context, request);

            if (unsafe && request instanceof HttpsURLConnection) {
                ((HttpsURLConnection) request).setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            request.connect();

            request.getOutputStream().write(bos.toByteArray());

            int status = request.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
                throw new FileNotFoundException("Error " + status + ": " + request.getResponseMessage());

            // https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xml = factory.newPullParser();
            xml.setInput(new InputStreamReader(request.getInputStream()));

            EntityLog.log(context, "Parsing " + url);

            boolean isAccount = false;
            boolean isEmail = false;
            boolean isSettings = false;
            boolean isProtocol = false;
            boolean isImap = false;
            boolean isSmtp = false;
            String host = null;
            Integer port = null;
            int eventType = xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = xml.getName();
                    if ("Account".equals(name)) {
                        // <AccountType>email</AccountType>
                        isAccount = true;
                    } else if ("AccountType".equals(name) && isAccount) {
                        // <AccountType>email</AccountType>
                        eventType = xml.next();
                        if (eventType == XmlPullParser.TEXT && "email".equals(xml.getText()))
                            isEmail = true;
                        continue;
                    } else if ("Action".equals(name) && isAccount) {
                        // <Action>settings</Action>
                        eventType = xml.next();
                        if (eventType == XmlPullParser.TEXT && "settings".equals(xml.getText()))
                            isSettings = true;
                        continue;
                    } else if ("Protocol".equals(name) && isAccount) {
                        // <AccountType>email</AccountType>
                        isProtocol = true;
                    } else if (isAccount && isEmail && isSettings && isProtocol) {
                        if ("Type".equals(name)) {
                            // <AccountType>email</AccountType>
                            eventType = xml.next();
                            if (eventType == XmlPullParser.TEXT) {
                                String type = xml.getText();
                                if ("IMAP".equals(type))
                                    isImap = true;
                                else if ("SMTP".equals(type))
                                    isSmtp = true;
                            }
                            continue;
                        } else if ("Server".equals(name)) {
                            eventType = xml.next();
                            if (eventType == XmlPullParser.TEXT)
                                host = xml.getText();
                            continue;
                        } else if ("Port".equals(name)) {
                            eventType = xml.next();
                            if (eventType == XmlPullParser.TEXT) {
                                String p = xml.getText();
                                if (TextUtils.isDigitsOnly(p))
                                    port = Integer.parseInt(p);
                            }
                            continue;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String name = xml.getName();
                    if ("Account".equals(name)) {
                        isAccount = false;
                        isEmail = false;
                        isSettings = false;
                    } else if ("Protocol".equals(name)) {
                        if (isProtocol && host != null && port != null) {
                            if (isImap) {
                                provider.imap.score = 20;
                                provider.imap.host = host;
                                provider.imap.port = port;
                                provider.imap.starttls = (provider.imap.port == 143);
                            } else if (isSmtp) {
                                provider.smtp.score = 20;
                                provider.smtp.host = host;
                                provider.smtp.port = port;
                                provider.smtp.starttls = (provider.smtp.port == 587);
                            }
                        }
                        isProtocol = false;
                        isImap = false;
                        isSmtp = false;
                        host = null;
                        port = null;
                    }
                }

                eventType = xml.next();
            }

            provider.validate();
            Log.e("MS=" + url);
            Log.i("MS imap=" + provider.imap);
            Log.i("MS smtp=" + provider.smtp);

            return provider;
        } finally {
            if (request != null)
                request.disconnect();
        }
    }

    @NonNull
    private static EmailProvider fromDNS(Context context, String domain, Discover discover, IDiscovery intf) throws UnknownHostException {
        // https://tools.ietf.org/html/rfc6186
        EmailProvider provider = new EmailProvider(domain);

        if (discover == Discover.ALL || discover == Discover.IMAP) {
            intf.onStatus("SRV imap " + domain);

            // Identifies an IMAP server where TLS is initiated directly upon connection to the IMAP server.
            List<DnsHelper.DnsRecord> list = new ArrayList<>();
            list.addAll(Arrays.asList(DnsHelper.lookup(context, "_imap._tcp." + domain, "srv")));
            list.addAll(Arrays.asList(DnsHelper.lookup(context, "_imaps._tcp." + domain, "srv")));

            // ... service is not supported at all at a particular domain by setting the target of an SRV RR to "."
            for (DnsHelper.DnsRecord record : new ArrayList<>(list))
                if (TextUtils.isEmpty(record.response) || ".".equals(record.response))
                    list.remove(record);

            if (list.size() == 0)
                throw new UnknownHostException(domain);

            Collections.sort(list, new Comparator<DnsHelper.DnsRecord>() {
                @Override
                public int compare(DnsHelper.DnsRecord d1, DnsHelper.DnsRecord d2) {
                    int p = Integer.compare(d1.priority, d2.priority);
                    if (p != 0)
                        return p;
                    int w = -Integer.compare(d1.weight, d2.weight);
                    if (w != 0)
                        return w;
                    return -Boolean.compare(d1.query.startsWith("_imaps._tcp."), d2.query.startsWith("_imaps._tcp."));
                }
            });

            DnsHelper.DnsRecord pref = list.get(0);

            provider.imap.score = 50;
            provider.imap.host = pref.response;
            provider.imap.port = pref.port;
            provider.imap.starttls = (!pref.query.startsWith("_imaps._tcp.") && pref.port == 143);
            EntityLog.log(context, pref.query + "=" + provider.imap);
        }

        if (discover == Discover.ALL || discover == Discover.SMTP) {
            intf.onStatus("SRV smtp " + domain);
            // https://tools.ietf.org/html/rfc8314

            List<DnsHelper.DnsRecord> list = new ArrayList<>();
            // Note that this covers connections both with and without Transport Layer Security (TLS)
            list.addAll(Arrays.asList(DnsHelper.lookup(context, "_submission._tcp." + domain, "srv")));
            list.addAll(Arrays.asList(DnsHelper.lookup(context, "_submissions._tcp." + domain, "srv")));

            for (DnsHelper.DnsRecord record : new ArrayList<>(list))
                if (TextUtils.isEmpty(record.response) || ".".equals(record.response))
                    list.remove(record);

            if (list.size() == 0)
                throw new UnknownHostException(domain);

            Collections.sort(list, new Comparator<DnsHelper.DnsRecord>() {
                @Override
                public int compare(DnsHelper.DnsRecord d1, DnsHelper.DnsRecord d2) {
                    int p = Integer.compare(d1.priority, d2.priority);
                    if (p != 0)
                        return p;
                    int w = -Integer.compare(d1.weight, d2.weight);
                    if (w != 0)
                        return w;
                    // submission is being preferred
                    return -Boolean.compare(d1.query.startsWith("_submission._tcp."), d2.query.startsWith("_submission._tcp."));
                }
            });

            DnsHelper.DnsRecord pref = list.get(0);

            provider.smtp.score = 50;
            provider.smtp.host = pref.response;
            provider.smtp.port = pref.port;
            provider.smtp.starttls = (!pref.query.startsWith("_submissions._tcp.") && pref.port == 587);
            EntityLog.log(context, pref.query + "=" + provider.smtp);
        }

        provider.validate();

        return provider;
    }

    @NonNull
    private static EmailProvider fromScan(Context context, String domain, Discover discover, IDiscovery intf)
            throws ExecutionException, InterruptedException, UnknownHostException {
        // https://tools.ietf.org/html/rfc8314
        Server imap = null;
        Server smtp = null;

        if (discover == Discover.ALL || discover == Discover.IMAP) {
            List<Server> imaps = new ArrayList<>();
            // SSL
            imaps.add(new Server(context, domain, "imap", 993, false));
            imaps.add(new Server(context, domain, "imaps", 993, false));
            imaps.add(new Server(context, domain, "mail", 993, false));
            imaps.add(new Server(context, domain, "mx", 993, false));
            imaps.add(new Server(context, domain, null, 993, false));
            // STARTTLS
            imaps.add(new Server(context, domain, "imap", 143, true));
            imaps.add(new Server(context, domain, "mail", 143, true));
            imaps.add(new Server(context, domain, "mx", 143, true));
            imaps.add(new Server(context, domain, null, 143, true));

            Server untrusted = null;
            for (Server server : imaps) {
                intf.onStatus("HOST " + server);
                Boolean result = server.isReachable.get();
                if (result == null) {
                    if (untrusted == null)
                        untrusted = server;
                } else if (result) {
                    imap = server;
                    break;
                }
            }

            if (imap == null)
                imap = untrusted;

            if (imap == null)
                throw new UnknownHostException(domain + " template");
        }

        if (discover == Discover.ALL || discover == Discover.SMTP) {
            List<Server> smtps = new ArrayList<>();
            // STARTTLS
            smtps.add(new Server(context, domain, "smtp", 587, true));
            smtps.add(new Server(context, domain, "mail", 587, true));
            smtps.add(new Server(context, domain, "mx", 587, true));
            smtps.add(new Server(context, domain, null, 587, true));
            // SSL
            smtps.add(new Server(context, domain, "smtp", 465, false));
            smtps.add(new Server(context, domain, "smtps", 465, false));
            smtps.add(new Server(context, domain, "mail", 465, false));
            smtps.add(new Server(context, domain, "mx", 465, false));
            smtps.add(new Server(context, domain, null, 465, false));

            Server untrusted = null;
            for (Server server : smtps) {
                intf.onStatus("HOST " + server);
                Boolean result = server.isReachable.get();
                if (result == null) {
                    if (untrusted == null)
                        untrusted = server;
                } else if (result) {
                    smtp = server;
                    break;
                }
            }

            if (smtp == null)
                smtp = untrusted;

            if (smtp == null)
                throw new UnknownHostException(domain + " template");
        }


        EmailProvider provider = new EmailProvider();
        provider.name = domain;

        if (imap != null)
            provider.imap = imap;

        if (smtp != null)
            provider.smtp = smtp;

        return provider;
    }

    private static void addDocumentation(EmailProvider provider, String href, String title) {
        if (provider.documentation == null)
            provider.documentation = new StringBuilder();
        else
            provider.documentation.append("<br><br>");

        provider.documentation.append("<a href=\"").append(href).append("\">").append(title).append("</a>");
    }

    protected EmailProvider(Parcel in) {
        if (in.readInt() == 0)
            imap = null;
        else {
            imap = new Server();
            imap.host = in.readString();
            imap.port = in.readInt();
            imap.starttls = (in.readInt() != 0);
            imap.score = in.readInt();
        }

        if (in.readInt() == 0)
            smtp = null;
        else {
            smtp = new Server();
            smtp.host = in.readString();
            smtp.port = in.readInt();
            smtp.starttls = (in.readInt() != 0);
            smtp.score = in.readInt();
        }

        appPassword = (in.readInt() != 0);
        link = in.readString();
        String doc = in.readString();
        documentation = (doc == null ? null : new StringBuilder(doc));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imap == null ? 0 : 1);
        if (imap != null) {
            dest.writeString(imap.host);
            dest.writeInt(imap.port);
            dest.writeInt(imap.starttls ? 1 : 0);
            dest.writeInt(imap.score);
        }

        dest.writeInt(smtp == null ? 0 : 1);
        if (smtp != null) {
            dest.writeString(smtp.host);
            dest.writeInt(smtp.port);
            dest.writeInt(smtp.starttls ? 1 : 0);
            dest.writeInt(smtp.score);
        }

        dest.writeInt(appPassword ? 1 : 0);
        dest.writeString(link);
        dest.writeString(documentation == null ? null : documentation.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EmailProvider> CREATOR = new Creator<EmailProvider>() {
        @Override
        public EmailProvider createFromParcel(Parcel in) {
            return new EmailProvider(in);
        }

        @Override
        public EmailProvider[] newArray(int size) {
            return new EmailProvider[size];
        }
    };

    private int getScore(String email) {
        if (imap == null || smtp == null)
            return -1;
        return imap.score + smtp.score +
                (TextUtils.isEmpty(username) || username.equalsIgnoreCase(email) ? 0 : 100);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EmailProvider) {
            EmailProvider other = (EmailProvider) obj;
            return (Objects.equals(this.imap, other.imap) &&
                    Objects.equals(this.smtp, other.smtp));
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imap, smtp);
    }

    @NonNull
    @Override
    public String toString() {
        return TextUtils.isEmpty(description) ? name : description;
    }

    public static class Server {
        public String host;
        public int port;
        public boolean starttls;

        // Scores:
        //   0 from MX record
        //  10 from port scan
        //     +2 trusted host
        //     +1 trusted DNS name
        //  20 from autoconfig
        //     +100 with username
        //  50 from DNS
        // 100 from profile

        private int score = 0;
        private Future<Boolean> isReachable;

        private Server() {
        }

        private Server(Context context, String domain, String prefix, int port, boolean starttls) {
            this.score = 10;
            this.host = (prefix == null ? "" : prefix + ".") + domain;
            this.port = port;
            this.starttls = starttls;
            this.isReachable = getReachable(context);
        }

        private void checkCertificate(Context context) {
            try {
                getReachable(context).get();
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        private Future<Boolean> getReachable(Context context) {
            Log.i("Scanning " + this);
            return Helper.getDownloadTaskExecutor().submit(new Callable<Boolean>() {
                // Returns:
                //   false: closed
                //   true: listening
                //   null: untrusted
                @Override
                public Boolean call() {
                    try {
                        for (InetAddress iaddr : DnsHelper.getAllByName(context, host)) {
                            InetSocketAddress address = new InetSocketAddress(iaddr, Server.this.port);

                            SocketFactory factory = (starttls
                                    ? SocketFactory.getDefault()
                                    : SSLSocketFactory.getDefault());
                            try (Socket socket = factory.createSocket()) {
                                EntityLog.log(context, "Connecting to " + address);
                                socket.connect(address, SCAN_TIMEOUT);
                                EntityLog.log(context, "Connected " + address);

                                socket.setSoTimeout(SCAN_TIMEOUT);

                                SSLSocket sslSocket = null;
                                try {
                                    if (starttls)
                                        sslSocket = ConnectionHelper.starttls(socket, host, port, context);
                                    else
                                        sslSocket = (SSLSocket) socket;

                                    sslSocket.startHandshake();

                                    Certificate[] certs = sslSocket.getSession().getPeerCertificates();
                                    for (Certificate cert : certs)
                                        if (cert instanceof X509Certificate) {
                                            List<String> names = EntityCertificate.getDnsNames((X509Certificate) cert);
                                            EntityLog.log(context, "Certificate " + address +
                                                    " " + TextUtils.join(",", names));

                                            if (EntityCertificate.matches(host, names)) {
                                                score += 2;
                                                EntityLog.log(context, "Trusted " + address);
                                                return true;
                                            }

                                            for (String name : names)
                                                try {
                                                    String similar = name;
                                                    if (similar.startsWith("*."))
                                                        similar = similar.substring(2);
                                                    InetAddress isimilar = DnsHelper.getByName(context, similar);
                                                    if (iaddr.equals(isimilar)) {
                                                        score += 1;
                                                        EntityLog.log(context, "Similar " + similar + " host=" + host);
                                                        host = similar;
                                                        return true;
                                                    }
                                                } catch (Throwable ex) {
                                                    Log.w(ex);
                                                }
                                        }

                                    EntityLog.log(context, "Untrusted " + address);
                                    return null;
                                } catch (Throwable ex) {
                                    // Typical:
                                    //   javax.net.ssl.SSLException: Unable to parse TLS packet header
                                    EntityLog.log(context, "Handshake " + address + ": " + Log.formatThrowable(ex));
                                } finally {
                                    try {
                                        if (sslSocket != null) {
                                            ConnectionHelper.signOff(sslSocket, port, context);
                                            sslSocket.close();
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }

                                EntityLog.log(context, "Reachable " + address);
                                return true;
                            } catch (Throwable ex) {
                                // Typical:
                                //   java.net.ConnectException: failed to connect to ...
                                //     android.system.ErrnoException: isConnected failed: ECONNREFUSED (Connection refused)
                                EntityLog.log(context, "Unreachable " + address + ": " + Log.formatThrowable(ex));

                                // Skip other addresses
                                if (ex instanceof ConnectException &&
                                        ex.getCause() instanceof ErrnoException &&
                                        ((ErrnoException) ex.getCause()).errno == ECONNREFUSED)
                                    return false;
                            }
                        }
                        return false;
                    } catch (Throwable ex) {
                        // Typical:
                        //   java.net.UnknownHostException: Unable to resolve host
                        //     android.system.GaiException: android_getaddrinfo failed: EAI_NODATA (No address associated with hostname)
                        EntityLog.log(context, "Error " + this + ": " + Log.formatThrowable(ex));
                        return false;
                    }
                }
            });
        }

        public boolean isSecure() {
            return (score >= 20); // Autoconfig or better
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Server) {
                Server other = (Server) obj;
                return (Objects.equals(this.host, other.host) &&
                        this.port == other.port &&
                        this.starttls == other.starttls);
            } else
                return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port, starttls);
        }

        @NonNull
        @Override
        public String toString() {
            return host + ":" + port + (starttls ? " starttls" : " ssl/tls") + " " + score;
        }
    }

    interface IDiscovery {
        void onStatus(String status);
    }

    public static class OAuth {
        boolean enabled;
        boolean askAccount;
        String clientId;
        String clientSecret;
        String[] scopes;
        String authorizationEndpoint;
        String tokenEndpoint;
        boolean tokenScopes;
        String redirectUri;
        String privacy;
        String prompt;
        Map<String, String> parameters;

        boolean askTenant() {
            return (authorizationEndpoint.contains("{tenant}") ||
                    tokenEndpoint.contains("{tenant}"));
        }
    }
}
