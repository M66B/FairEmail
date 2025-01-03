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

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class Adguard {
    // https://github.com/AdguardTeam/AdguardFilters
    // https://github.com/AdguardTeam/FiltersRegistry/blob/master/filters/filter_17_TrackParam/filter.txt
    // https://github.com/AdguardTeam/TestCases/tree/master/public/Filters/removeparam-rules

    private final static int FETCH_TIMEOUT = 20 * 1000; // milliseconds
    private final static String LIST = "https://raw.githubusercontent.com/AdguardTeam/FiltersRegistry/master/filters/filter_17_TrackParam/filter.txt";

    private static final List<String> ALL_CONTENT = Collections.unmodifiableList(Arrays.asList(
            "document",
            "subdocument",
            "font",
            "image",
            "media",
            "object",
            "other",
            "ping",
            "script",
            "stylesheet",
            "websocket",
            "xmlhttprequest",
            "popup"
    ));

    @Nullable
    public static Uri filter(Context context, Uri uri) {
        if (uri.isOpaque())
            return null;

        String host = uri.getHost();
        if (TextUtils.isEmpty(host))
            return null;

        File file = getFile(context);
        if (!file.exists())
            return null;

        List<String> removes = new ArrayList<>();
        List<String> importants = new ArrayList<>();
        List<String> excepts = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) {
                // https://adguard.com/kb/general/ad-filtering/create-own-filters/#comments
                if (TextUtils.isEmpty(line) || line.startsWith("!"))
                    continue;

                // rule = ["@@"] pattern [ "$" modifiers ]
                // modifiers = [modifier0, modifier1[, ...[, modifierN]]]

                int dollar = line.indexOf('$');
                while (dollar > 0 && line.charAt(dollar - 1) == '\\')
                    dollar = line.indexOf('$', dollar + 1);
                if (dollar < 0) {
                    if (!line.contains("##"))
                        Log.w("Adguard command missing line=" + line);
                    continue;
                }

                String pattern = line.substring(0, dollar)
                        .replace("\\$", "$");
                String rest = line.substring(dollar + 1)
                        .replace("\\$", "$");

                int start = 0;
                List<String> modifiers = new ArrayList<>();
                while (start < rest.length()) {
                    int comma = rest.indexOf(',', start);
                    while (comma > 0 && rest.charAt(comma - 1) == '\\')
                        comma = rest.indexOf(',', comma + 1);
                    int end = (comma < 0 ? rest.length() : comma);
                    modifiers.add(rest.substring(start, end)
                            .replace("\\,", ","));
                    start = (comma < 0 ? end : end + 1);
                }

                String remove = null;
                boolean important = false;
                boolean matches = true;
                List<String> contents = new ArrayList<>();
                for (String modifier : modifiers) {
                    int equal = modifier.indexOf('=');
                    String name = (equal < 0 ? modifier : modifier.substring(0, equal));
                    String param = (equal < 0 ? "" : modifier.substring(equal + 1));
                    if ("removeparam".equals(name)) {
                        // https://adguard.com/kb/general/ad-filtering/create-own-filters/#removeparam-modifier
                        remove = param;
                    } else if ("domain".equals(name)) {
                        // https://adguard.com/kb/general/ad-filtering/create-own-filters/#domain-modifier
                        // domains = ["~"] entry_0 ["|" ["~"] entry_1 ["|" ["~"]entry_2 ["|" ... ["|" ["~"]entry_N]]]]
                        // entry_i = ( regular_domain / any_tld_domain / regexp )
                        matches = false;

                        List<String> domains = new ArrayList<>();
                        start = 0;
                        while (start < param.length()) {
                            int pipe = param.indexOf('|', start);
                            while (pipe > 0 && param.charAt(pipe - 1) == '\\')
                                pipe = param.indexOf('|', pipe + 1);
                            int end = (pipe < 0 ? param.length() : pipe);
                            domains.add(param.substring(start, end)
                                    .replace("\\|", "|"));
                            start = (pipe < 0 ? end : end + 1);
                        }

                        boolean and = false;
                        for (String domain : domains) {
                            boolean not = domain.startsWith("~");
                            if (not)
                                and = true;

                            String d = (not ? domain.substring(1) : domain);

                            if (d.endsWith("*")) {
                                // any_tld_domain
                                matches = host.startsWith(d.substring(0, d.length() - 1));
                            } else if (d.startsWith("/")) {
                                // regexp
                                int slash = d.lastIndexOf('/');
                                if (slash < 1) {
                                    Log.w("Adguard missing closing slash domain=" + domain);
                                    continue;
                                }
                                // the characters /, $, and | must be escaped with \
                                String regex = d.substring(1, slash)
                                        .replace("\\/", "/");
                                Log.w("Adguard domain regex=" + regex);
                                matches = Pattern.compile(regex).matcher(host).find();
                            } else {
                                // regular_domain
                                matches = host.equals(d);
                            }

                            if (not)
                                matches = !matches;
                            if (matches)
                                Log.i("Adguard domain=" + domain + " host=" + host + " not=" + not);
                            if (and != matches)
                                break;
                        }
                    } else if ("important".equals(name)) {
                        important = true;
                        Log.w("Adguard important=" + param);
                    } else {
                        if (name.startsWith("~")) {
                            name = name.substring(1);
                            for (String content : ALL_CONTENT)
                                if (!name.equals(content))
                                    contents.add(content);
                        } else
                            contents.add(name);
                    }
                }

                if (remove == null || !matches)
                    continue;

                // $removeparam rules that do not have any content type modifiers will match only requests where content type is document.
                if (contents.size() == 0)
                    contents.add("document");

                List<String> recognized = new ArrayList<>();
                String mime = Helper.guessMimeType(uri.getLastPathSegment());
                if (mime.startsWith("audio/") || mime.startsWith("video/"))
                    recognized.add("media");
                else if (mime.startsWith("image/"))
                    recognized.add("image");
                else if ("text/css".equals(mime))
                    recognized.add("stylesheet");
                else if ("application/javascript".equals(mime))
                    recognized.add("script");
                else if (mime.startsWith("font/"))
                    recognized.add("font");
                else
                    recognized.addAll(Arrays.asList("document", "subdocument", "xmlhttprequest"));

                boolean found = false;
                for (String content : recognized)
                    if (contents.contains(content)) {
                        found = true;
                        break;
                    }

                if (!found) {
                    Log.i("Adguard skipping mime=" + mime +
                            " recognized=" + TextUtils.join(", ", recognized) +
                            " contents=" + TextUtils.join(", ", contents) +
                            " line=" + line);
                    continue;
                }

                boolean except = false;
                matches = TextUtils.isEmpty(pattern);
                if (!matches) {
                    // https://adguard.com/kb/general/ad-filtering/create-own-filters/#basic-rules-special-characters

                    if (pattern.startsWith("@@")) {
                        // a marker that is used in rules of exception.
                        // To turn off filtering for a request, start your rule with this marker.
                        except = true;
                        pattern = pattern.substring(2);
                    }

                    String u = uri.toString();
                    if (pattern.startsWith("||")) {
                        // an indication to apply the rule to the specified domain and its subdomains.
                        // With this character, you do not have to specify a particular protocol and subdomain in address mask.
                        // It means that || stands for http://*., https://*., ws://*., wss://*. at once.
                        int ss = u.indexOf("//");
                        if (ss > 0)
                            u = u.substring(ss + 2);
                        pattern = pattern.substring(2);
                    }

                    StringBuilder b = new StringBuilder();
                    for (char c : pattern.toCharArray())
                        if (c == '*') {
                            // a wildcard character. It is used to represent any set of characters.
                            // This can also be an empty string or a string of any length.
                            b.append(".*");
                        } else if (c == '^') {
                            // a separator character mark.
                            // Separator character is any character, but a letter, a digit, or one of the following: _ - . %.
                            b.append("[^0-9a-zA-Z\\_\\-\\.\\%]");
                        } else if (c == '|') {
                            // a pointer to the beginning or the end of address.
                            // The value depends on the character placement in the mask.
                            b.append(b.length() == 0 ? '^' : '$');
                            Log.w("Adguard anchor expr=" + pattern);
                        } else {
                            if ("\\.?![]{}()<>*+-=^$|".indexOf(c) >= 0)
                                b.append('\\');
                            b.append(c);
                        }
                    matches = Pattern.compile(b.toString()).matcher(u).find();
                    if (matches)
                        Log.i("Adguard expr=" + b + " remove=" + remove + " except=" + except);
                }

                if (matches)
                    if (except) {
                        if (!excepts.contains(remove))
                            excepts.add(remove);
                    } else {
                        if (!removes.contains(remove))
                            removes.add(remove);
                        if (important && !importants.contains(remove))
                            importants.add(remove);
                    }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        try {
            Uri.Builder builder = uri.buildUpon();
            builder.clearQuery();

            boolean changed = false;
            for (String key : uri.getQueryParameterNames()) {
                boolean omit = false;
                for (String remove : removes) {
                    String value = uri.getQueryParameter(key);
                    if (omitParam(remove, key, value)) {
                        omit = true;
                        if (!importants.contains(remove))
                            for (String except : excepts)
                                if (omitParam(except, key, value)) {
                                    Log.i("Adguard except=" + except);
                                    omit = false;
                                    break;
                                }
                    }
                }

                if (omit)
                    changed = true;
                else
                    for (String value : uri.getQueryParameters(key))
                        builder.appendQueryParameter(key, value);
            }

            return (changed ? builder.build() : null);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    private static boolean omitParam(String remove, String key, String value) {
        // https://adguard.com/kb/general/ad-filtering/create-own-filters/#removeparam-modifier

        if ("".equals(remove)) {
            // Specify naked $removeparam to remove all query parameters
            return true;
        }

        // $removeparam=~param — removes all query parameters with the name different from param.
        // $removeparam=~/regexp/ — removes all query parameters that do not match the regexp regular expression.
        boolean not = remove.startsWith("~");
        if (not)
            remove = remove.substring(1);

        if (remove.startsWith("/")) {
            // $removeparam=/regexp/[options]
            // the only supported option is i which makes matching case-insensitive.
            int end = remove.lastIndexOf('/');
            if (end < 1) {
                Log.w("Adguard missing slash remove=" + remove + " end=" + end);
                return false;
            }

            String regex = remove.substring(1, end)
                    .replace("\\/", "/");
            String rest = remove.substring(end + 1);
            Log.i("Adguard regex=" + regex + " rest=" + rest);

            int flags = 0;
            if ("i".equals(rest))
                flags = Pattern.CASE_INSENSITIVE;
            else if (!TextUtils.isEmpty(rest))
                Log.w("Adguard unexpected remove=" + remove);

            String all = key + "=" + value;
            if (Pattern.compile(regex, flags).matcher(all).find() ^ not) {
                Log.i("Adguard omit regex=" + regex);
                return true;
            }
        } else if (remove.equals(key) ^ not) {
            // $removeparam=param
            Log.i("Adguard omit key=" + key);
            return true;
        }

        return false;
    }

    static void download(Context context) throws IOException, JSONException {
        File file = getFile(context);

        URL url = new URL(LIST);
        Log.i("GET " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FETCH_TIMEOUT);
        connection.setConnectTimeout(FETCH_TIMEOUT);
        ConnectionHelper.setUserAgent(context, connection);
        connection.connect();

        try {
            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK)
                throw new FileNotFoundException("Error " + status + ": " + connection.getResponseMessage());

            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                Helper.copy(connection.getInputStream(), os);
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putLong("adguard_last", new Date().getTime()).apply();
        } finally {
            connection.disconnect();
        }
    }

    private static File getFile(Context context) {
        return new File(context.getFilesDir(), "adguard.txt");
    }
}
