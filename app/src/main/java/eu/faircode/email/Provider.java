package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Provider {
    public String name;
    public String imap_host;
    public int imap_port;
    public String smtp_host;
    public int smtp_port;
    public boolean starttls;

    private Provider() {
    }

    Provider(String name) {
        this.name = name;
    }

    static List<Provider> loadProfiles(Context context) {
        List<Provider> result = null;
        try {
            XmlResourceParser xml = context.getResources().getXml(R.xml.providers);
            int eventType = xml.getEventType();
            Provider provider = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("providers".equals(xml.getName()))
                        result = new ArrayList<>();
                    else if ("provider".equals(xml.getName())) {
                        provider = new Provider();
                        provider.name = xml.getAttributeValue(null, "name");
                    } else if ("imap".equals(xml.getName())) {
                        provider.imap_host = xml.getAttributeValue(null, "host");
                        provider.imap_port = xml.getAttributeIntValue(null, "port", 0);
                    } else if ("smtp".equals(xml.getName())) {
                        provider.smtp_host = xml.getAttributeValue(null, "host");
                        provider.smtp_port = xml.getAttributeIntValue(null, "port", 0);
                        provider.starttls = xml.getAttributeBooleanValue(null, "starttls", false);
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
            Log.e(Helper.TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(result, new Comparator<Provider>() {
            @Override
            public int compare(Provider p1, Provider p2) {
                return collator.compare(p1.name, p2.name);
            }
        });

        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
