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
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.core.util.PatternsCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class UriHelper {
    // https://publicsuffix.org/
    private static final HashSet<String> suffixList = new HashSet<>();

    // https://raw.githubusercontent.com/publicsuffix/list/master/public_suffix_list.dat
    private static final String SUFFIX_LIST_NAME = "public_suffix_list.dat";

    // https://github.com/newhouse/url-tracking-stripper
    private static final List<String> PARANOID_QUERY = Collections.unmodifiableList(Arrays.asList(
            // https://en.wikipedia.org/wiki/UTM_parameters
            "awt_a", // AWeber
            "awt_l", // AWeber
            "awt_m", // AWeber

            "icid", // Adobe
            "gclid", // Google
            "gclsrc", // Google ads
            "dclid", // DoubleClick (Google)
            "fbclid", // Facebook
            "igshid", // Instagram

            "mc_cid", // MailChimp
            "mc_eid", // MailChimp

            "zanpid", // Zanox (Awin)

            "kclickid" // https://support.freespee.com/hc/en-us/articles/202577831-Kenshoo-integration
    ));

    // https://github.com/snarfed/granary/blob/master/granary/facebook.py#L1789

    private static final List<String> FACEBOOK_WHITELIST_PATH = Collections.unmodifiableList(Arrays.asList(
            "/nd/", "/n/", "/story.php"
    ));

    private static final List<String> FACEBOOK_WHITELIST_QUERY = Collections.unmodifiableList(Arrays.asList(
            "story_fbid", "fbid", "id", "comment_id"
    ));

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

    static @NonNull
    Uri guessScheme(@NonNull Uri uri) {
        if (uri.getScheme() != null)
            return uri;

        String url = uri.toString();
        if (Helper.EMAIL_ADDRESS.matcher(url).matches())
            return Uri.parse("mailto:" + url);
        else if (PatternsCompat.IP_ADDRESS.matcher(url).matches())
            return Uri.parse("https://" + url);
        else if (android.util.Patterns.PHONE.matcher(url).matches())
            // Patterns.PHONE (\+[0-9]+[\- \.]*)?(\([0-9]+\)[\- \.]*)?([0-9][0-9\- \.]+[0-9])
            // PhoneNumberUtils.isGlobalPhoneNumber() [\+]?[0-9.-]+
            return Uri.parse("tel:" + url);
        else {
            Uri g = Uri.parse(URLUtil.guessUrl(url));
            String scheme = g.getScheme();
            if (scheme == null)
                return uri;
            else if ("http".equals(scheme))
                scheme = "https";
            return Uri.parse(scheme + "://" + url);
        }
    }

    static int getSuffixCount(Context context) {
        ensureSuffixList(context);
        synchronized (suffixList) {
            return suffixList.size();
        }
    }

    private static void ensureSuffixList(Context context) {
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

    static Uri sanitize(Uri uri) {
        if (uri.isOpaque())
            return uri;

        Uri url;
        boolean changed = false;
        if (uri.getHost() != null &&
                uri.getHost().endsWith("safelinks.protection.outlook.com") &&
                !TextUtils.isEmpty(uri.getQueryParameter("url"))) {
            changed = true;
            url = Uri.parse(uri.getQueryParameter("url"));
        } else if ("https".equals(uri.getScheme()) &&
                "smex-ctp.trendmicro.com".equals(uri.getHost()) &&
                "/wis/clicktime/v1/query".equals(uri.getPath()) &&
                !TextUtils.isEmpty(uri.getQueryParameter("url"))) {
            changed = true;
            url = Uri.parse(uri.getQueryParameter("url"));
        } else if ("https".equals(uri.getScheme()) &&
                "www.google.com".equals(uri.getHost()) &&
                uri.getPath() != null &&
                uri.getPath().startsWith("/amp/")) {
            // https://blog.amp.dev/2017/02/06/whats-in-an-amp-url/
            Uri result = null;

            String u = uri.toString();
            u = u.replace("https://www.google.com/amp/", "");

            int p = u.indexOf("/");
            while (p > 0) {
                String segment = u.substring(0, p);
                if (segment.contains(".")) {
                    result = Uri.parse("https://" + u);
                    break;
                }

                u = u.substring(p + 1);
                p = u.indexOf("/");
            }

            changed = (result != null);
            url = (result == null ? uri : result);
        } else if (uri.getQueryParameterNames().size() == 1) {
            // Sophos Email Appliance
            Uri result = null;

            String key = uri.getQueryParameterNames().iterator().next();
            if (TextUtils.isEmpty(uri.getQueryParameter(key)))
                try {
                    String data = new String(Base64.decode(key, Base64.DEFAULT));
                    int v = data.indexOf("ver=");
                    int u = data.indexOf("&&url=");
                    if (v == 0 && u > 0)
                        result = Uri.parse(URLDecoder.decode(data.substring(u + 6), StandardCharsets.UTF_8.name()));
                } catch (Throwable ex) {
                    Log.w(ex);
                }

            changed = (result != null);
            url = (result == null ? uri : result);
        } else
            url = uri;

        if (url.isOpaque())
            return uri;

        Uri.Builder builder = url.buildUpon();

        builder.clearQuery();
        String host = uri.getHost();
        String path = uri.getPath();
        if (host != null)
            host = host.toLowerCase(Locale.ROOT);
        if (path != null)
            path = path.toLowerCase(Locale.ROOT);
        boolean first = "www.facebook.com".equals(host);
        for (String key : url.getQueryParameterNames()) {
            // https://en.wikipedia.org/wiki/UTM_parameters
            // https://docs.oracle.com/en/cloud/saas/marketing/eloqua-user/Help/EloquaAsynchronousTrackingScripts/EloquaTrackingParameters.htm
            String lkey = key.toLowerCase(Locale.ROOT);
            if (PARANOID_QUERY.contains(lkey) ||
                    lkey.startsWith("utm_") ||
                    lkey.startsWith("elq") ||
                    ((host != null && host.endsWith("facebook.com")) &&
                            !first &&
                            FACEBOOK_WHITELIST_PATH.contains(path) &&
                            !FACEBOOK_WHITELIST_QUERY.contains(lkey)) ||
                    ("store.steampowered.com".equals(host) &&
                            "snr".equals(lkey)))
                changed = true;
            else if (!TextUtils.isEmpty(key))
                for (String value : url.getQueryParameters(key)) {
                    Log.i("Query " + key + "=" + value);
                    Uri suri = Uri.parse(value);
                    if ("http".equals(suri.getScheme()) || "https".equals(suri.getScheme())) {
                        Uri s = sanitize(suri);
                        if (s != null) {
                            changed = true;
                            value = s.toString();
                        }
                    }
                    builder.appendQueryParameter(key, value);
                }
            first = false;
        }

        return (changed ? builder.build() : null);
    }

    static Uri secure(Uri uri, boolean https) {
        String scheme = uri.getScheme();
        if (https ? "http".equals(scheme) : "https".equals(scheme)) {
            Uri.Builder builder = uri.buildUpon();
            builder.scheme(https ? "https" : "http");

            String authority = uri.getEncodedAuthority();
            if (authority != null) {
                authority = authority.replace(https ? ":80" : ":443", https ? ":443" : ":80");
                builder.encodedAuthority(authority);
            }

            return builder.build();
        } else
            return uri;
    }

    static boolean isSecure(Uri uri) {
        return (!uri.isOpaque() && "https".equals(uri.getScheme()));
    }

    static boolean isHyperLink(Uri uri) {
        return (!uri.isOpaque() &&
                ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())));
    }
}
