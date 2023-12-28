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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Adguard {
    // https://github.com/AdguardTeam/AdguardFilters
    // https://github.com/AdguardTeam/FiltersRegistry/blob/master/filters/filter_17_TrackParam/filter.txt

    private static final List<String> ADGUARD_IGNORE = Collections.unmodifiableList(Arrays.asList(
            "cookie", "font", "image", "media", "script", "subdocument", "stylesheet", "xmlhttprequest"
    ));

    @Nullable
    public static Uri filter(Context context, Uri uri) {
        if (uri.isOpaque())
            return null;

        String host = uri.getHost();
        if (TextUtils.isEmpty(host))
            return null;

        List<String> removes = new ArrayList<>();
        List<String> excepts = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(context.getAssets().open("adguard_filter.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                // https://adguard.com/kb/general/ad-filtering/create-own-filters/#basic-rules-syntax

                if (TextUtils.isEmpty(line) || line.startsWith("!"))
                    continue;

                int dollar = line.indexOf('$');
                while (dollar > 0 && line.charAt(dollar - 1) == '\\')
                    dollar = line.indexOf('$', dollar + 1);
                if (dollar < 0) {
                    if (!line.contains("##"))
                        Log.w("Adguard command missing line=" + line);
                    continue;
                }

                String expr = line.substring(0, dollar)
                        .replace("\\$", "$");
                String rest = line.substring(dollar + 1)
                        .replace("\\$", "$");

                List<String> commands = new ArrayList<>();
                int start = 0;
                while (start < rest.length()) {
                    int comma = rest.indexOf(',', start);
                    while (comma > 0 && rest.charAt(comma - 1) == '\\')
                        comma = rest.indexOf(',', comma + 1);
                    int end = (comma < 0 ? rest.length() : comma);
                    commands.add(rest.substring(start, end)
                            .replace("\\,", ","));
                    start = (comma < 0 ? end : end + 1);
                }

                String remove = null;
                boolean matches = true;
                for (String command : commands) {
                    int equal = command.indexOf('=');
                    String c = (equal < 0 ? command : command.substring(0, equal));
                    String e = (equal < 0 ? "" : command.substring(equal + 1));
                    if ("removeparam".equals(c))
                        remove = e;
                    else if ("domain".equals(c)) {
                        // https://adguard.com/kb/general/ad-filtering/create-own-filters/#domain-modifier
                        matches = false;

                        List<String> domains = new ArrayList<>();
                        start = 0;
                        while (start < e.length()) {
                            int pipe = e.indexOf('|', start);
                            while (pipe > 0 && e.charAt(pipe - 1) == '\\')
                                pipe = e.indexOf('|', pipe + 1);
                            int end = (pipe < 0 ? e.length() : pipe);
                            domains.add(e.substring(start, end)
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
                    } else {
                        if (!c.equals("document") &&
                                !(c.startsWith("~") && !c.equals("~document"))) {
                            if (!ADGUARD_IGNORE.contains(c))
                                Log.w("Adguard ignoring=" + c);
                            remove = null;
                            break;
                        }
                    }
                }

                if (remove == null || !matches)
                    continue;

                boolean except = false;
                matches = TextUtils.isEmpty(expr);
                if (!matches) {
                    // https://adguard.com/kb/general/ad-filtering/create-own-filters/#basic-rules-special-characters

                    if (expr.startsWith("@@")) {
                        except = true;
                        expr = expr.substring(2);
                    }

                    String u = uri.toString();
                    if (expr.startsWith("||")) {
                        int ss = u.indexOf("//");
                        if (ss > 0)
                            u = u.substring(ss + 2);
                        expr = expr.substring(2);
                    }

                    StringBuilder b = new StringBuilder();
                    for (char c : expr.toCharArray())
                        if (c == '*')
                            b.append(".*");
                        else if (c == '^')
                            b.append("[^0-9a-zA-Z\\_\\-\\.\\%]");
                        else if (c == '|') {
                            b.append(b.length() == 0 ? '^' : '$');
                            Log.w("Adguard anchor expr=" + expr);
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

        if ("".equals(remove))
            return true;

        if (remove.startsWith("~")) {
            Log.w("Adguard not supported remove=" + remove);
            return false;
        }

        if (remove.startsWith("/")) {
            int end = remove.lastIndexOf('/');
            if (end < 1) {
                Log.w("Adguard missing slash remove=" + remove + " end=" + end);
                return false;
            }

            String regex = remove.substring(1, end)
                    .replace("\\/", "/");
            String rest = remove.substring(end + 1);
            Log.i("Adguard regex=" + regex + " rest=" + rest);

            if (!TextUtils.isEmpty(rest))
                Log.w("Adguard unexpected remove=" + remove);

            String all = key + "=" + value;
            if (Pattern.compile(regex).matcher(all).find()) {
                Log.i("Adguard omit regex=" + regex);
                return true;
            }
        } else if (remove.equals(key)) {
            Log.i("Adguard omit key=" + key);
            return true;
        }

        return false;
    }
}
