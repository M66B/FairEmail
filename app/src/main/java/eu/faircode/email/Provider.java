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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Provider {
    public String name;
    public int order;
    public String link;
    public String type;
    public String prefix;
    public String imap_host;
    public boolean imap_starttls;
    public int imap_port;
    public String smtp_host;
    public int smtp_port;
    public boolean smtp_starttls;

    private Provider() {
    }

    Provider(String name) {
        this.name = name;
    }

    static List<Provider> loadProfiles(Context context) {
        List<Provider> result = null;
        try {
            Provider provider = null;
            XmlResourceParser xml = context.getResources().getXml(R.xml.providers);
            int eventType = xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("providers".equals(xml.getName()))
                        result = new ArrayList<>();
                    else if ("provider".equals(xml.getName())) {
                        provider = new Provider();
                        provider.name = xml.getAttributeValue(null, "name");
                        provider.order = xml.getAttributeIntValue(null, "order", Integer.MAX_VALUE);
                        provider.link = xml.getAttributeValue(null, "link");
                        provider.type = xml.getAttributeValue(null, "type");
                        provider.prefix = xml.getAttributeValue(null, "prefix");
                    } else if ("imap".equals(xml.getName())) {
                        provider.imap_host = xml.getAttributeValue(null, "host");
                        provider.imap_port = xml.getAttributeIntValue(null, "port", 0);
                        provider.imap_starttls = xml.getAttributeBooleanValue(null, "starttls", false);
                    } else if ("smtp".equals(xml.getName())) {
                        provider.smtp_host = xml.getAttributeValue(null, "host");
                        provider.smtp_port = xml.getAttributeIntValue(null, "port", 0);
                        provider.smtp_starttls = xml.getAttributeBooleanValue(null, "starttls", false);
                    } else
                        throw new IllegalAccessException(xml.getName());
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

        Collections.sort(result, new Comparator<Provider>() {
            @Override
            public int compare(Provider p1, Provider p2) {
                int o = Integer.compare(p1.order, p2.order);
                if (o == 0)
                    return collator.compare(p1.name, p2.name);
                else
                    return o;
            }
        });

        return result;
    }

    static Provider fromDomain(Context context, String domain) throws IOException, XmlPullParserException {
        try {
            return Provider.fromDNS(context, domain);
        } catch (TextParseException ex) {
            Log.w(ex);
            throw new UnknownHostException(domain);
        } catch (UnknownHostException ex) {
            Log.w(ex);
            return Provider.fromConfig(context, domain);
        }
    }

    private static Provider fromConfig(Context context, String domain) throws IOException, XmlPullParserException {
        // https://wiki.mozilla.org/Thunderbird:Autoconfiguration:ConfigFileFormat
        URL url = new URL("https://autoconfig.thunderbird.net/v1.1/" + domain);
        Log.i("Fetching " + url);

        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setReadTimeout(20 * 1000);
        request.setConnectTimeout(20 * 1000);
        request.setRequestMethod("GET");
        request.setDoInput(true);
        request.connect();

        // https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xml = factory.newPullParser();
        xml.setInput(new InputStreamReader(request.getInputStream()));

        boolean imap = false;
        boolean smtp = false;
        Provider provider = new Provider(domain);
        int eventType = xml.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if ("incomingServer".equals(xml.getName()))
                    imap = "imap".equals(xml.getAttributeValue(null, "type"));

                else if ("outgoingServer".equals(xml.getName()))
                    smtp = "smtp".equals(xml.getAttributeValue(null, "type"));

                else if ("hostname".equals(xml.getName())) {
                    eventType = xml.next();
                    if (eventType == XmlPullParser.TEXT) {
                        String host = xml.getText();
                        Log.i("Host=" + host);
                        if (imap)
                            provider.imap_host = host;
                        else if (smtp)
                            provider.smtp_host = host;
                    }
                    continue;

                } else if ("port".equals(xml.getName())) {
                    eventType = xml.next();
                    if (eventType == XmlPullParser.TEXT) {
                        String port = xml.getText();
                        Log.i("Port=" + port);
                        if (imap) {
                            provider.imap_port = Integer.parseInt(port);
                            provider.imap_starttls = (provider.imap_port == 143);
                        } else if (smtp) {
                            provider.smtp_port = Integer.parseInt(port);
                            provider.smtp_starttls = (provider.smtp_port == 587);
                        }
                    }
                    continue;

                } else if ("socketType".equals(xml.getName())) {
                    eventType = xml.next();
                    if (eventType == XmlPullParser.TEXT) {
                        String socket = xml.getText();
                        Log.i("Socket=" + socket);
                        if ("SSL".equals(socket)) {
                            if (imap)
                                provider.imap_starttls = false;
                            else if (smtp)
                                provider.smtp_starttls = false;
                        } else if ("STARTTLS".equals(socket)) {
                            if (imap)
                                provider.imap_starttls = true;
                            else if (smtp)
                                provider.smtp_starttls = true;
                        }
                    }

                    continue;
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if ("incomingServer".equals(xml.getName()))
                    imap = false;
                else if ("outgoingServer".equals(xml.getName()))
                    smtp = false;
            }

            eventType = xml.next();
        }

        request.disconnect();

        Log.i("imap=" + provider.imap_host + ":" + provider.imap_port + ":" + provider.imap_starttls);
        Log.i("smtp=" + provider.smtp_host + ":" + provider.smtp_port + ":" + provider.smtp_starttls);
        return provider;
    }

    private static Provider fromDNS(Context context, String domain) throws TextParseException, UnknownHostException {
        // https://tools.ietf.org/html/rfc6186
        SRVRecord imap = lookup("_imaps._tcp." + domain);
        SRVRecord smtp = lookup("_submission._tcp." + domain);

        Provider provider = new Provider(domain);
        provider.imap_host = imap.getTarget().toString(true);
        provider.imap_port = imap.getPort();
        provider.imap_starttls = (provider.imap_port == 143);

        provider.smtp_host = smtp.getTarget().toString(true);
        provider.smtp_port = smtp.getPort();
        provider.smtp_starttls = (provider.smtp_port == 587);

        return provider;
    }

    private static SRVRecord lookup(String dns) throws TextParseException, UnknownHostException {
        Lookup lookup = new Lookup(dns, Type.SRV);

        // https://dns.watch/
        SimpleResolver resolver = new SimpleResolver("84.200.69.80");
        lookup.setResolver(resolver);
        Log.i("Lookup dns=" + dns + " @" + resolver.getAddress());
        Record[] records = lookup.run();
        if (lookup.getResult() != Lookup.SUCCESSFUL)
            if (lookup.getResult() == Lookup.HOST_NOT_FOUND)
                throw new UnknownHostException(dns);
            else
                throw new IllegalArgumentException(lookup.getErrorString());
        Log.i("Found dns=" + (records == null ? -1 : records.length));
        return (records == null || records.length == 0 ? null : (SRVRecord) records[0]);
    }

    public int getAuthType() {
        if ("com.google".equals(type))
            return Helper.AUTH_TYPE_GMAIL;
        return Helper.AUTH_TYPE_PASSWORD;
    }

    @Override
    public String toString() {
        return name;
    }
}
