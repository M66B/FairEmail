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

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Locale;

public class UriHelper {
    // https://publicsuffix.org/
    private static final HashSet<String> suffixList = new HashSet<>();

    // https://raw.githubusercontent.com/publicsuffix/list/master/public_suffix_list.dat
    private static final String SUFFIX_LIST_NAME = "public_suffix_list.dat";

    static String getParentDomain(Context context, String host) {
        if (host == null)
            return null;
        String parent = _getSuffix(context, host);
        return (parent == null ? host : parent);
    }

    static boolean hasParentDomain(Context context, String host) {
        return (host != null && _getSuffix(context, host) != null);
    }

    private static String _getSuffix(Context context, @NonNull String host) {
        ensureSuffixList(context);

        String h = host.toLowerCase(Locale.ROOT);
        while (true) {
            int dot = h.indexOf('.');
            if (dot < 0)
                return null;

            String prefix = h.substring(0, dot);
            h = h.substring(dot + 1);

            int d = h.indexOf('.');
            String w = (d < 0 ? null : '*' + h.substring(d));

            synchronized (suffixList) {
                if (!suffixList.contains('!' + h) &&
                        (suffixList.contains(h) || suffixList.contains(w))) {
                    String parent = prefix + "." + h;
                    Log.d("Host=" + host + " parent=" + parent);
                    return parent;
                }
            }
        }
    }

    static String getEmailUser(String address) {
        if (address == null)
            return null;

        int at = address.indexOf('@');
        if (at > 0)
            return address.substring(0, at);

        return null;
    }

    static String getEmailDomain(String address) {
        if (address == null)
            return null;

        int at = address.indexOf('@');
        if (at > 0)
            return address.substring(at + 1);

        return null;
    }

    static void ensureSuffixList(Context context) {
        synchronized (suffixList) {
            if (suffixList.size() > 0)
                return;

            Log.i("Reading " + SUFFIX_LIST_NAME);
            try (InputStream is = context.getAssets().open(SUFFIX_LIST_NAME)) {
                BufferedReader br = new BufferedReader(new InputStreamReader((is)));
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (TextUtils.isEmpty(line))
                        continue;
                    if (line.startsWith("//"))
                        continue;
                    suffixList.add(line);
                }
                Log.i(SUFFIX_LIST_NAME + "=" + suffixList.size());
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    }
}
