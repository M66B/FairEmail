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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ActivityDmarc extends ActivityBase {
    private TextView tvDmarc;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle("DMARC");

        View view = LayoutInflater.from(this).inflate(R.layout.activity_dmarc, null);
        setContentView(view);

        tvDmarc = findViewById(R.id.tvDmarc);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        // Initialize
        FragmentDialogTheme.setBackground(this, view, false);
        grpReady.setVisibility(View.GONE);

        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
    }

    private void load() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        long id = intent.getLongExtra("id", -1L);
        Log.i("DMARC uri=" + uri + " id=" + id);

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putLong("id", id);

        new SimpleTask<Spanned>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Spanned onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                NoStreamException.check(uri, context);

                DateFormat DTF = Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.SHORT);
                int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
                int colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
                float stroke = context.getResources().getDisplayMetrics().density;
                SpannableStringBuilder ssb = new SpannableStringBuilderEx();

                String data;
                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    if (is == null)
                        throw new FileNotFoundException(uri.toString());
                    data = Helper.readStream(is);
                }

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xml = factory.newPullParser();
                xml.setInput(new StringReader(data));

                // https://tools.ietf.org/id/draft-kucherawy-dmarc-base-13.xml#xml_schema
                boolean feedback = false;
                boolean report_metadata = false;
                boolean policy_published = false;
                boolean record = false;
                boolean row = false;
                boolean policy_evaluated = false;
                boolean identifiers = false;
                boolean auth_results = false;
                String lastDomain = null;
                String result = null;
                List<Pair<String, DnsHelper.DnsRecord>> spf = null;
                int eventType = xml.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String name = xml.getName();
                        switch (name) {
                            case "feedback":
                                feedback = true;
                                break;
                            case "report_metadata":
                                report_metadata = true;
                                break;
                            case "policy_published":
                                policy_published = true;
                                lastDomain = null;
                                break;
                            case "record":
                                record = true;
                                break;
                            case "row":
                                row = true;
                                ssb.append("\uFFFC");
                                ssb.setSpan(new LineSpan(colorSeparator, stroke, 0), ssb.length() - 1, ssb.length(), 0);
                                ssb.append("\n");
                                break;
                            case "policy_evaluated":
                                policy_evaluated = true;
                                break;
                            case "identifiers":
                                identifiers = true;
                                break;
                            case "auth_results":
                                auth_results = true;
                                ssb.append("\n");
                                break;

                            case "org_name":
                            case "begin":
                            case "end":
                                if (feedback && report_metadata) {
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String text = xml.getText();
                                        if (text == null)
                                            text = "<null>";
                                        if ("begin".equals(name) || "end".equals(name)) {
                                            text = text.trim();
                                            try {
                                                ssb.append(name).append('=')
                                                        .append(DTF.format(Long.parseLong(text) * 1000));
                                            } catch (Throwable ex) {
                                                Log.w(ex);
                                                ssb.append(name).append('=')
                                                        .append(text);
                                            }
                                        } else
                                            ssb.append(text);
                                        ssb.append(' ');
                                    }
                                }
                                break;
                            case "domain":
                                if (feedback && (policy_published || auth_results)) {
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String text = xml.getText();
                                        lastDomain = text;
                                        if (text == null)
                                            text = "<null>";
                                        ssb.append(text).append(' ');
                                    }
                                }
                                break;
                            case "adkim":
                            case "aspf":
                            case "p":
                            case "sp":
                            case "fo":
                                if (feedback && policy_published) {
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String text = xml.getText();
                                        if (text == null)
                                            text = "<null>";
                                        if ("adkim".equals(name) || "aspf".equals(name))
                                            if ("r".equals(text))
                                                text = "relaxed";
                                            else if ("s".equals(text))
                                                text = "strict";
                                        ssb.append(name).append('=')
                                                .append(text).append(' ');
                                    }
                                }
                                break;
                            case "pct":
                                if (feedback && policy_published) {
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String text = xml.getText();
                                        if (text == null)
                                            text = "<null>";
                                        Integer pct = Helper.parseInt(text);
                                        if (pct == null)
                                            ssb.append(name).append('=')
                                                    .append(text).append(' ');
                                        else
                                            ssb.append(text).append("% ");
                                    }
                                }

                                break;
                            case "source_ip":
                            case "count":
                                if (feedback && record && row) {
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String text = xml.getText();
                                        if (text == null)
                                            text = "<null>";
                                        ssb.append(name).append('=')
                                                .append(text).append(' ');
                                        if ("source_ip".equals(name)) {
                                            try {
                                                boolean valid = false;
                                                if (spf != null)
                                                    for (Pair<String, DnsHelper.DnsRecord> p : spf) {
                                                        for (String ip : p.second.name.split("\\s+")) {
                                                            ip = ip.toLowerCase(Locale.ROOT);
                                                            if (ip.startsWith("ip4:") || ip.startsWith("ip6:")) {
                                                                String[] net = ip.substring(4).split("/");
                                                                if (net.length != 2)
                                                                    continue;
                                                                Integer prefix = Helper.parseInt(net[1]);
                                                                if (prefix == null)
                                                                    continue;
                                                                if (ConnectionHelper.inSubnet(text, net[0], prefix)) {
                                                                    valid = true;
                                                                    break;
                                                                }
                                                            } else if ("mx".equals(ip))
                                                                try {
                                                                    DnsHelper.DnsRecord[] mxs =
                                                                            DnsHelper.lookup(context, p.first, "mx");
                                                                    for (DnsHelper.DnsRecord mx : mxs) {
                                                                        List<DnsHelper.DnsRecord> as = new ArrayList<>();
                                                                        try {
                                                                            as.addAll(Arrays.asList(DnsHelper.lookup(context, mx.name, "a")));
                                                                        } catch (UnknownHostException ignored) {
                                                                        }
                                                                        try {
                                                                            as.addAll(Arrays.asList(DnsHelper.lookup(context, mx.name, "aaaa")));
                                                                        } catch (UnknownHostException ignored) {
                                                                        }
                                                                        for (DnsHelper.DnsRecord a : as)
                                                                            if (text.equals(a.name)) {
                                                                                valid = true;
                                                                                break;
                                                                            }
                                                                        if (valid)
                                                                            break;
                                                                    }
                                                                } catch (UnknownHostException ignored) {
                                                                }
                                                        }
                                                        if (valid)
                                                            break;
                                                    }

                                                int start = ssb.length();
                                                ssb.append(valid ? "valid" : "invalid");
                                                if (!valid) {
                                                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                                                    ssb.setSpan(new ForegroundColorSpan(colorWarning), start, ssb.length(), 0);
                                                }
                                                ssb.append(' ');
                                            } catch (Throwable ex) {
                                                Log.w(ex);
                                                ssb.append(ex.toString()).append('\n');
                                            }

                                            try {
                                                InetAddress addr = InetAddress.getByName(text);
                                                IPInfo info = IPInfo.getOrganization(addr, context);
                                                ssb.append('(').append(info.org).append(") ");
                                            } catch (Throwable ex) {
                                                Log.w(ex);
                                                ssb.append(ex.toString()).append('\n');
                                            }
                                        }
                                    }
                                }
                                break;
                            case "disposition": // none, quarantine, reject
                            case "dkim":
                            case "spf":
                            case "header_from":
                            case "envelope_from":
                            case "envelope_to":
                                if (feedback && record)
                                    if (policy_evaluated || identifiers) {
                                        eventType = xml.next();
                                        if (eventType == XmlPullParser.TEXT) {
                                            ssb.append(name).append('=');
                                            int start = ssb.length();
                                            String text = xml.getText();
                                            if (text == null)
                                                text = "<null>";
                                            ssb.append(text);

                                            if (!"pass".equals(text.toLowerCase(Locale.ROOT)) &&
                                                    ("dkim".equals(name) || "spf".equals(name))) {
                                                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                                                ssb.setSpan(new ForegroundColorSpan(colorWarning), start, ssb.length(), 0);
                                            }

                                            ssb.append(' ');
                                        }
                                    } else if (auth_results)
                                        result = name;
                                break;
                            case "result":
                                if (feedback && auth_results) {
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        ssb.append(result == null ? "?" : result).append('=');
                                        int start = ssb.length();
                                        String text = xml.getText();
                                        if (text == null)
                                            text = "<null>";
                                        ssb.append(text);
                                        if (!"pass".equals(text.toLowerCase(Locale.ROOT))) {
                                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                                            ssb.setSpan(new ForegroundColorSpan(colorWarning), start, ssb.length(), 0);
                                        }
                                        ssb.append(' ');
                                    }
                                }
                                break;
                            case "selector":
                            case "scope":
                                if (feedback && auth_results) {
                                    eventType = xml.next();
                                    if (eventType == XmlPullParser.TEXT) {
                                        String text = xml.getText();
                                        if (text == null)
                                            text = "<null>";
                                        ssb.append(name).append('=')
                                                .append(text).append(' ');
                                    }
                                }
                                break;
                        }

                        if ("report_metadata".equals(name) ||
                                "policy_published".equals(name) ||
                                "row".equals(name) ||
                                "identifiers".equals(name) ||
                                "auth_results".equals(name)) {
                            int start = ssb.length();
                            ssb.append(name);
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                            ssb.append("\n");
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {
                        String name = xml.getName();
                        switch (name) {
                            case "feedback":
                                feedback = false;
                                break;
                            case "report_metadata":
                                report_metadata = false;
                                if (feedback)
                                    ssb.append("\n\n");
                                break;
                            case "policy_published":
                                policy_published = false;
                                if (feedback) {
                                    ssb.append("\n\n");
                                    if (lastDomain == null)
                                        spf = null;
                                    else {
                                        Integer start = null;
                                        SpannableStringBuilder extra = new SpannableStringBuilderEx();
                                        spf = lookupSpf(context, lastDomain, extra);
                                        for (Pair<String, DnsHelper.DnsRecord> p : spf) {
                                            ssb.append(p.first).append(' ')
                                                    .append(p.second.name).append("\n");
                                            if (start == null) {
                                                start = ssb.length();
                                                ssb.append("\n");
                                            }
                                        }
                                        ssb.append('\n').append(extra);
                                        if (start != null) {
                                            ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), start, ssb.length(), 0);
                                            ssb.append("\n");
                                        }

                                        List<DnsHelper.DnsRecord> records = new ArrayList<>();
                                        try {
                                            records.addAll(Arrays.asList(
                                                    DnsHelper.lookup(context, "_dmarc." + lastDomain, "txt")));
                                        } catch (UnknownHostException ignored) {
                                        }
                                        try {
                                            records.addAll(Arrays.asList(
                                                    DnsHelper.lookup(context, "default._bimi." + lastDomain, "txt")));
                                        } catch (UnknownHostException ignored) {
                                        }
                                        for (DnsHelper.DnsRecord r : records)
                                            ssb.append(r.name).append("\n");
                                        ssb.append("\n");
                                    }
                                }
                                break;
                            case "record":
                                record = false;
                                break;
                            case "row":
                                row = false;
                                if (feedback)
                                    ssb.append("\n\n");
                                break;
                            case "policy_evaluated":
                                policy_evaluated = false;
                                break;
                            case "identifiers":
                                identifiers = false;
                                if (feedback)
                                    ssb.append("\n");
                                break;
                            case "auth_results":
                                auth_results = false;
                                if (feedback)
                                    ssb.append("\n");
                                break;
                            case "dkim":
                            case "spf":
                                if (feedback && auth_results) {
                                    result = null;
                                    ssb.append("\n");
                                }
                                break;
                        }
                    }

                    eventType = xml.next();
                }

                ssb.append("\uFFFC");
                ssb.setSpan(new LineSpan(colorSeparator, stroke, 0), ssb.length() - 1, ssb.length(), 0);
                ssb.append("\n");

                int start = ssb.length();
                ssb.append(TextHelper.formatXml(data, 2));
                ssb.setSpan(new TypefaceSpan("monospace"), start, ssb.length(), 0);
                ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), start, ssb.length(), 0);

                return ssb;
            }

            @Override
            protected void onExecuted(Bundle args, Spanned dmarc) {
                tvDmarc.setText(dmarc);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                if (ex instanceof NoStreamException)
                    ((NoStreamException) ex).report(ActivityDmarc.this);
                else
                    tvDmarc.setText(ex + "\n" + android.util.Log.getStackTraceString(ex));
                grpReady.setVisibility(View.VISIBLE);
            }

            private List<Pair<String, DnsHelper.DnsRecord>> lookupSpf(Context context, String domain, SpannableStringBuilder ssb) {
                List<Pair<String, DnsHelper.DnsRecord>> result = new ArrayList<>();
                try {
                    DnsHelper.DnsRecord[] records = DnsHelper.lookup(context, domain, "txt");
                    ssb.append(domain).append('=')
                            .append(Integer.toString(records.length)).append('\n');
                    for (DnsHelper.DnsRecord r : records)
                        if (r.name.contains("spf")) {
                            result.add(new Pair<>(domain, r));
                            for (String part : r.name.split("\\s+"))
                                if (part.toLowerCase(Locale.ROOT).startsWith("include:")) {
                                    String sub = part.substring("include:".length());
                                    result.addAll(lookupSpf(context, sub, ssb));
                                }
                        }
                } catch (Throwable ex) {
                    Log.w(ex);
                    ssb.append(ex.toString()).append('\n');
                }
                return result;
            }
        }.execute(this, args, "dmarc:decode");
    }
}
