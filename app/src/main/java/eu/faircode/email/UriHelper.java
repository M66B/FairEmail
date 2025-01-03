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
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.net.MailTo;
import androidx.core.util.PatternsCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriHelper {
    // https://publicsuffix.org/
    private static final HashSet<String> suffixList = new HashSet<>();

    // https://raw.githubusercontent.com/publicsuffix/list/master/public_suffix_list.dat
    private static final String SUFFIX_LIST_NAME = "public_suffix_list.dat";

    // https://github.com/svenjacobs/leon
    // https://github.com/newhouse/url-tracking-stripper
    // https://maxchadwick.xyz/tracking-query-params-registry/
    private static final List<String> PARANOID_QUERY = Collections.unmodifiableList(Arrays.asList(
            // https://en.wikipedia.org/wiki/UTM_parameters
            "awt_a", // AWeber
            "awt_l", // AWeber
            "awt_m", // AWeber

            "icid", // Adobe
            "ef_id", // https://experienceleague.adobe.com/docs/advertising-cloud/integrations/analytics/mc/mc-ids.html
            "_ga", // Google Analytics
            "gclid", // Google
            "gclsrc", // Google ads
            "dclid", // DoubleClick (Google)
            "fbclid", // Facebook
            "igshid", // Instagram
            "msclkid", // https://help.ads.microsoft.com/apex/index/3/en/60000

            "mc_cid", // MailChimp
            "mc_eid", // MailChimp

            "zanpid", // Zanox (Awin)

            "kclickid", // https://support.freespee.com/hc/en-us/articles/202577831-Kenshoo-integration

            "oly_anon_id", "oly_enc_id", // https://training.omeda.com/knowledge-base/olytics-product-outline/
            "_openstat", // https://yandex.com/support/direct/statistics/url-tags.html
            "vero_conv", "vero_id", // https://help.getvero.com/cloud/articles/what-is-vero_id/
            "wickedid", // https://help.wickedreports.com/how-to-manually-tag-a-facebook-ad-with-wickedid
            "yclid", // https://ads-help.yahoo.co.jp/yahooads/ss/articledetail?lan=en&aid=20442
            "__s", // https://ads-help.yahoo.co.jp/yahooads/ss/articledetail?lan=en&aid=20442
            "guccounter", "guce_referrer", "guce_referrer_sig", // Yahoo
            "rb_clickid", // Russian
            "s_cid", // https://help.goacoustic.com/hc/en-us/articles/360043311613-Track-lead-sources
            "ml_subscriber", "ml_subscriber_hash", // https://www.mailerlite.com/help/how-to-integrate-your-forms-to-a-wix-website
            "twclid", // https://business.twitter.com/en/blog/performance-advertising-on-twitter.html
            "gbraid", "wbraid", // https://support.google.com/google-ads/answer/10417364
            "_hsenc", "__hssc", "__hstc", "__hsfp", "hsCtaTracking" // https://knowledge.hubspot.com/reports/what-cookies-does-hubspot-set-in-a-visitor-s-browser
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
        int dot = host.indexOf('.');
        if (dot < 0)
            return null;
        String parent = host.substring(dot + 1);
        String tld = getTld(context, host);
        if (tld == null || tld.equals(parent) || parent.length() < tld.length())
            return null;
        return parent;
    }

    static String getRootDomain(Context context, String host) {
        if (host == null)
            return null;
        String tld = getTld(context, host);
        if (tld == null)
            return null;
        if (tld.equalsIgnoreCase(host))
            return null;
        int len = host.length() - tld.length() - 1;
        if (len < 0) {
            Log.e("getRootDomain host=" + host + " tld=" + tld);
            return null;
        }
        int dot = host.substring(0, len).lastIndexOf('.');
        if (dot < 0)
            return host;
        return host.substring(dot + 1);
    }

    static boolean isTld(Context context, String host) {
        if (host == null)
            return false;
        String tld = getTld(context, host);
        return (tld != null && tld.equals(host));
    }

    static boolean hasTld(Context context, String host) {
        return (host != null && getTld(context, host) != null);
    }

    static String getTld(Context context, @NonNull String host) {
        ensureSuffixList(context);

        String eval = host.toLowerCase(Locale.ROOT);
        while (true) {
            int d = eval.indexOf('.');
            String w = (d < 0 ? null : '*' + eval.substring(d));

            synchronized (suffixList) {
                if (suffixList.contains(eval))
                    return eval;
                if (suffixList.contains(w))
                    if (suffixList.contains('!' + eval))
                        return eval.substring(d + 1);
                    else
                        return eval;
            }

            int dot = eval.indexOf('.');
            if (dot < 0)
                return null;

            eval = eval.substring(dot + 1);
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

                    try {
                        String ascii = IDN.toASCII(line, IDN.ALLOW_UNASSIGNED);
                        if (!line.equals(ascii))
                            suffixList.add(line);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
                Log.i(SUFFIX_LIST_NAME + "=" + suffixList.size());
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    }

    static Uri sanitize(Context context, Uri uri) {
        if (uri.isOpaque())
            return uri;

        Uri url;
        boolean changed = false;
        if (uri.getHost() != null &&
                uri.getHost().endsWith("safelinks.protection.outlook.com") &&
                !TextUtils.isEmpty(uri.getQueryParameter("url"))) {
            Uri result = Uri.parse(uri.getQueryParameter("url"));
            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        } else if ("https".equals(uri.getScheme()) &&
                "smex-ctp.trendmicro.com".equals(uri.getHost()) &&
                "/wis/clicktime/v1/query".equals(uri.getPath()) &&
                !TextUtils.isEmpty(uri.getQueryParameter("url"))) {
            Uri result = Uri.parse(uri.getQueryParameter("url"));
            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
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

            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        } else if ("https".equals(uri.getScheme()) &&
                uri.getHost() != null &&
                uri.getHost().startsWith("www.google.") &&
                uri.getQueryParameter("url") != null) {
            // Google non-com redirects
            Uri result = Uri.parse(uri.getQueryParameter("url"));
            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        } else if (uri.getPath() != null &&
                uri.getPath().startsWith("/track/click") &&
                uri.getQueryParameter("p") != null) {
            Uri result = null;
            try {
                // Mandrill
                String p = new String(Base64.decode(uri.getQueryParameter("p"), Base64.URL_SAFE));
                JSONObject json = new JSONObject(p);
                json = new JSONObject(json.getString("p"));
                result = Uri.parse(json.getString("url"));
            } catch (Throwable ex) {
                Log.i(ex);
            }
            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        } else if (uri.getHost() != null && uri.getHost().endsWith(".awstrack.me")) {
            // https://docs.aws.amazon.com/ses/latest/dg/configure-custom-open-click-domains.html
            String path = uri.getPath();
            int s = (path == null ? -1 : path.indexOf('/', 1));
            Uri result = (s > 0 ? Uri.parse(path.substring(s + 1)) : null);
            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        } else {
            Uri result = getBraveDebounce(context, uri);

            if (result == null &&
                    uri.getQueryParameter("redirect") != null)
                result = Uri.parse(uri.getQueryParameter("redirect"));

            if (result == null &&
                    uri.getQueryParameter("redirectUrl") != null)
                // https://.../link-tracker?redirectUrl=<base64>&sig=...&iat=...&a=...&account=...&email=...&s=...&i=...
                try {
                    byte[] bytes = Base64.decode(uri.getQueryParameter("redirectUrl"), Base64.URL_SAFE);
                    String u = URLDecoder.decode(new String(bytes), StandardCharsets.UTF_8.name());
                    result = Uri.parse(u);
                } catch (Throwable ex) {
                    Log.i(ex);
                }

            if (result == null && !BuildConfig.PLAY_STORE_RELEASE) {
                for (String key : uri.getQueryParameterNames()) {
                    for (String value : uri.getQueryParameters(key)) {
                        Uri q = Uri.parse(value);
                        if (isHyperLink(q)) {
                            result = q;
                            break;
                        }
                    }
                    if (result != null)
                        break;
                }
            }

            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        }

        if (!changed) {
            // Sophos Email Appliance
            // http://<host>/?<base64>
            Uri result = null;
            try {
                if (uri.getQueryParameterNames().size() == 1) {
                    String key = uri.getQueryParameterNames().iterator().next();
                    if (TextUtils.isEmpty(uri.getQueryParameter(key))) {
                        String data = new String(Base64.decode(key, Base64.URL_SAFE));
                        int v = data.indexOf("ver=");
                        int u = data.indexOf("&&url=");
                        if (v == 0 && u > 0)
                            result = Uri.parse(URLDecoder.decode(data.substring(u + 6), StandardCharsets.UTF_8.name()));
                    }
                }
            } catch (Throwable ex) {
                Log.i(ex);
            }
            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        }

        if (!changed) {
            // go.dhlparcel.nl and others
            // Try base64 last path segment
            Uri result = null;
            String path = uri.getPath();
            try {
                if (path != null) {
                    int s = path.lastIndexOf('/');
                    if (s > 0) {
                        String b = new String(Base64.decode(path.substring(s + 1), Base64.URL_SAFE));
                        result = Uri.parse(b);
                    }
                }
            } catch (Throwable ex) {
                Log.i(ex);
            }
            changed = (result != null && isHyperLink(result));
            url = (changed ? result : uri);
        }

        if (url.isOpaque() || !isHyperLink(url))
            return uri;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean adguard = prefs.getBoolean("adguard", false);
        if (adguard) {
            Uri result = Adguard.filter(context, url);
            return (result == null ? url : result);
        }

        Uri.Builder builder = url.buildUpon();

        builder.clearQuery();
        String host = uri.getHost();
        String path = uri.getPath();
        if (host != null)
            host = host.toLowerCase(Locale.ROOT);
        if (path != null)
            path = path.toLowerCase(Locale.ROOT);

        String q = url.getEncodedQuery();
        if (!TextUtils.isEmpty(q)) {
            boolean first = "www.facebook.com".equals(host);
            StringBuilder sb = new StringBuilder();
            for (String kv : q.split("&")) {
                int eq = kv.indexOf('=');
                String key = (eq < 0 ? kv : kv.substring(0, eq));
                String value = (eq < 0 ? null : kv.substring(eq + 1));

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
                else if (!TextUtils.isEmpty(key)) {
                    Uri suri = Uri.parse(key);
                    if (suri != null && isHyperLink(suri)) {
                        Uri s = sanitize(context, suri);
                        return (s == null ? suri : s);
                    }
                    if (sb.length() > 0)
                        sb.append('&');
                    sb.append(key);
                    if (value != null) {
                        sb.append('=').append(value);
                    }
                }
                first = false;
            }
            if (sb.length() > 0)
                builder.encodedQuery(sb.toString());
        }

        return (changed ? builder.build() : null);
    }

    @Nullable
    private static Uri getBraveDebounce(Context context, Uri uri) {
        // https://github.com/brave/adblock-lists/blob/master/brave-lists/debounce.json
        try (InputStream is = context.getAssets().open("debounce.json")) {
            String json = Helper.readStream(is);
            JSONArray jbounce = new JSONArray(json);
            Log.i("Brave debounces=" + jbounce.length());
            for (int i = 0; i < jbounce.length(); i++) {
                JSONObject jitem = jbounce.getJSONObject(i);
                JSONArray jinclude = jitem.getJSONArray("include");
                JSONArray jexclude = jitem.getJSONArray("exclude");

                boolean include = false;
                for (int j = 0; j < jinclude.length(); j++)
                    if (Pattern.matches(escapeStar(jinclude.getString(j)), uri.toString())) {
                        include = true;
                        break;
                    }

                if (include)
                    for (int j = 0; j < jexclude.length(); j++)
                        if (Pattern.matches(escapeStar(jexclude.getString(j)), uri.toString())) {
                            include = false;
                            break;
                        }

                if (include) {
                    String action = jitem.getString("action");
                    if ("redirect".equals(action) || "base64,redirect".equals(action)) {
                        String name = jitem.getString("param");
                        String param = uri.getQueryParameter(name);
                        if (!TextUtils.isEmpty(param))
                            try {
                                if ("base64,redirect".equals(action))
                                    param = new String(Base64.decode(param, Base64.NO_PADDING));
                                return Uri.parse(param);
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                    } else if ("regex-path".equals(action)) {
                        String regex = jitem.getString("param");
                        String prepend = jitem.optString("prepend_scheme");
                        String path = uri.getPath();
                        if (!TextUtils.isEmpty(path)) {
                            Matcher m = Pattern.compile(regex).matcher(path);
                            if (m.matches()) {
                                String param = m.group(1);
                                if (!TextUtils.isEmpty(prepend))
                                    param = prepend + "://" + param;
                                return Uri.parse(param);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return null;
    }

    private static String escapeStar(String regex) {
        for (char kar : "\\.?![]{}()<>*+-=^$|".toCharArray())
            if (kar != '*')
                regex = regex.replace("" + kar, "\\" + kar);
        return regex.replace("*", ".*");
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
        return (!uri.isOpaque() && "https".equalsIgnoreCase(uri.getScheme()));
    }

    static boolean isHyperLink(Uri uri) {
        return (!uri.isOpaque() &&
                ("http".equalsIgnoreCase(uri.getScheme()) ||
                        "https".equalsIgnoreCase(uri.getScheme())));
    }

    static boolean isMail(Uri uri) {
        return (uri.isOpaque() && "mailto".equalsIgnoreCase(uri.getScheme()));
    }

    static boolean isPhoneNumber(Uri uri) {
        return (uri.isOpaque() && "tel".equalsIgnoreCase(uri.getScheme()));
    }

    static boolean isGeo(Uri uri) {
        return (uri.isOpaque() && "geo".equalsIgnoreCase(uri.getScheme()));
    }

    static Uri fix(Uri uri) {
        if ((!"http".equals(uri.getScheme()) && "http".equalsIgnoreCase(uri.getScheme())) ||
                (!"https".equals(uri.getScheme()) && "https".equalsIgnoreCase(uri.getScheme()))) {
            String u = uri.toString();
            int semi = u.indexOf(':');
            if (semi > 0)
                return Uri.parse(u.substring(0, semi).toLowerCase(Locale.ROOT) + u.substring(semi));
        }

        return uri;
    }

    static String getHost(Uri uri) {
        if ("mailto".equalsIgnoreCase(uri.getScheme())) {
            MailTo email = MailTo.parse(uri.toString());
            return getEmailDomain(email.getTo());
        } else
            return uri.getHost();
    }

    static void test(Context context) {
        String[] hosts = new String[]{
                "child.parent.example.com", "parent.example.com", "example.com", "com",
                "child.parent.co.uk", "parent.co.uk", "co.uk", "uk",
                "child.parent.aaa.ck", "parent.aaa.ck", "aaa.ck", "ck",
                "child.parent.www.ck", "parent.www.ck", "www.ck", "ck"
        };

        for (String host : hosts)
            Log.i("PSL " + host + ":" +
                    " tld=" + getTld(context, host) +
                    " root=" + getRootDomain(context, host) +
                    " parent=" + getParentDomain(context, host));
    }
}
