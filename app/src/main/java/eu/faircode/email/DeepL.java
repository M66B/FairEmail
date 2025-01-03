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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DeepL {
    // https://www.deepl.com/docs-api/
    // https://github.com/DeepLcom/deepl-java
    private static JSONArray jlanguages = null;

    private static final int DEEPL_TIMEOUT = 20; // seconds
    private static final String PLAN_URI = "https://www.deepl.com/pro-account/plan";

    static final String PRIVACY_URI = "https://www.deepl.com/privacy/";

    // curl https://api-free.deepl.com/v2/languages -d auth_key=... -d type=target

    public static boolean isAvailable(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("deepl_enabled", false);
    }

    public static boolean canTranslate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String deepl_key = prefs.getString("deepl_key", null);
        return !TextUtils.isEmpty(deepl_key);
    }

    public static List<Language> getTargetLanguages(Context context, boolean favorites) {
        try {
            ensureLanguages(context);

            String pkg = context.getPackageName();
            Resources res = context.getResources();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            List<Language> languages = new ArrayList<>();
            Map<String, Integer> frequencies = new HashMap<>();
            for (int i = 0; i < jlanguages.length(); i++) {
                JSONObject jlanguage = jlanguages.getJSONObject(i);
                String name = jlanguage.getString("name");
                String target = jlanguage.getString("language");
                boolean formality = jlanguage.optBoolean("supports_formality");

                Locale locale = Locale.forLanguageTag(target);
                if (locale != null)
                    name = locale.getDisplayName();

                int frequency = prefs.getInt("translated_" + target, 0);

                String flag;
                if ("AR".equals(target))
                    flag = "SA";
                else if ("CS".equals(target))
                    flag = "CZ";
                else if ("DA".equals(target))
                    flag = "DK";
                else if ("EL".equals(target))
                    flag = "GR";
                else if ("ET".equals(target))
                    flag = "EE";
                else if ("JA".equals(target))
                    flag = "JP";
                else if ("KO".equals(target))
                    flag = "KR";
                else if ("NB".equals(target))
                    flag = "NO";
                else if ("SL".equals(target))
                    flag = "SI";
                else if ("SV".equals(target))
                    flag = "SE";
                else if ("UK".equals(target))
                    flag = "UA";
                else if ("ZH".equals(target))
                    flag = "CN";
                else {
                    String[] t = target.split("-");
                    flag = t[t.length - 1];
                }

                String resname = "flag_" + flag.toLowerCase();
                int resid = res.getIdentifier(resname, "drawable", pkg);

                languages.add(new Language(name, target, formality,
                        resid == 0 ? null : resid,
                        favorites && frequency > 0, frequency));
                frequencies.put(target, frequency);
            }

            Collator collator = Collator.getInstance(Locale.getDefault());
            collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
            Collections.sort(languages, new Comparator<Language>() {
                @Override
                public int compare(Language l1, Language l2) {
                    int freq1 = frequencies.get(l1.target);
                    int freq2 = frequencies.get(l2.target);

                    if (freq1 == freq2 || !favorites)
                        return collator.compare(l1.name, l2.name);
                    else
                        return -Integer.compare(freq1, freq2);
                }
            });

            if (BuildConfig.DEBUG && TextHelper.canTransliterate())
                languages.add(0, new Language(context.getString(R.string.title_advanced_notify_transliterate),
                        "transliterate", false, null, true, 0));

            return languages;
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    private static void ensureLanguages(Context context) throws IOException, JSONException {
        if (jlanguages != null)
            return;

        try (InputStream is = context.getAssets().open("deepl.json")) {
            String json = Helper.readStream(is);
            jlanguages = new JSONArray(json);
        }
    }

    public static Translation translate(CharSequence text, boolean html, String target, Context context) throws IOException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean deepl_formal = prefs.getBoolean("deepl_formal", true);
        boolean deepl_html = prefs.getBoolean("deepl_html", false);
        return translate(text, html && deepl_html, target, deepl_formal, context);
    }

    public static Translation translate(CharSequence text, boolean html, String target, boolean formality, Context context) throws IOException, JSONException {
        if ("transliterate".equals(target)) {
            Locale detected = TextHelper.detectLanguage(context, text.toString());
            String transliterated = TextHelper.transliterate(context, text.toString());
            String language = Locale.getDefault().toLanguageTag();
            return new Translation(detected == null ? language : detected.toLanguageTag(), language, transliterated);
        }

        if (!ConnectionHelper.getNetworkState(context).isConnected())
            throw new IllegalArgumentException(context.getString(R.string.title_no_internet));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean small = prefs.getBoolean("deepl_small", false);
        String key = prefs.getString("deepl_key", null);

        String input;
        if (html) {
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(text);
            if (small)
                for (RelativeSizeSpan span : ssb.getSpans(0, ssb.length(), RelativeSizeSpan.class))
                    if (span.getSizeChange() == HtmlHelper.FONT_SMALL)
                        ssb.removeSpan(span);
            String h = HtmlHelper.toHtml(ssb, context);
            Elements content = JsoupEx.parse(h).body().children();
            Element last = (content.size() == 0 ? null : content.get(content.size() - 1));
            if (last != null && "br".equals(last.tagName()))
                content.remove(last);
            input = content.outerHtml();
        } else
            input = text.toString();

        Log.i("DeepL input=" + input.replaceAll("\\r?\\n", "|"));

        // https://www.deepl.com/docs-api/translating-text/request/
        String request =
                "text=" + URLEncoder.encode(input, StandardCharsets.UTF_8.name()) +
                        "&target_lang=" + URLEncoder.encode(target, StandardCharsets.UTF_8.name());

        // https://www.deepl.com/docs-api/handling-html-(beta)/
        if (html)
            request += "&tag_handling=html";

        request += "&formality=" + (formality ? "prefer_more" : "prefer_less");

        URL url = new URL(getBaseUri(key) + "translate");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setReadTimeout(DEEPL_TIMEOUT * 1000);
        connection.setConnectTimeout(DEEPL_TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Authorization", "DeepL-Auth-Key " + key);
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Content-Length", Integer.toString(request.length()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.connect();

        try {
            connection.getOutputStream().write(request.getBytes());

            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK) {
                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    InputStream is = connection.getErrorStream();
                    if (is != null)
                        error += "\n" + Helper.readStream(is);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                throw new IOException(error);
            }

            String response = Helper.readStream(connection.getInputStream());

            JSONObject jroot = new JSONObject(response);
            JSONArray jtranslations = jroot.getJSONArray("translations");
            if (jtranslations.length() == 0)
                throw new IOException();
            JSONObject jtranslation = (JSONObject) jtranslations.get(0);

            Translation result = new Translation();
            result.target_language = target;
            result.detected_language = jtranslation.getString("detected_source_language");

            String output = jtranslation.getString("text");

            Log.i("DeepL output=" + output.replaceAll("\\r?\\n", "|"));

            if (html) {
                Document document = JsoupEx.parse(output);
                result.translated_text = HtmlHelper.fromDocument(context, document, null, null);
            } else
                result.translated_text = output;

            Log.i("DeepL result=" + result.translated_text.toString().replaceAll("\\r?\\n", "|"));

            return result;
        } finally {
            connection.disconnect();
        }
    }

    public static Integer[] getUsage(Context context) throws IOException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = prefs.getString("deepl_key", null);

        // https://www.deepl.com/docs-api/other-functions/monitoring-usage/
        URL url = new URL(getBaseUri(key) + "usage");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setReadTimeout(DEEPL_TIMEOUT * 1000);
        connection.setConnectTimeout(DEEPL_TIMEOUT * 1000);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Authorization", "DeepL-Auth-Key " + key);
        connection.connect();

        try {
            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK) {
                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    InputStream is = connection.getErrorStream();
                    if (is != null)
                        error += "\n" + Helper.readStream(is);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                throw new IOException(error);
            }

            String response = Helper.readStream(connection.getInputStream());

            JSONObject jroot = new JSONObject(response);
            int count = jroot.getInt("character_count");
            int limit = jroot.getInt("character_limit");
            return new Integer[]{count, limit};
        } finally {
            connection.disconnect();
        }
    }

    private static String getBaseUri(String key) {
        String domain = (key != null && key.endsWith(":fx") ? "api-free.deepl.com" : "api.deepl.com");
        return "https://" + domain + "/v2/";
    }

    public static class Language {
        public String name;
        public String target;
        public boolean formality;
        public Integer icon;
        public boolean favorite;
        public int frequency;

        private Language(String name, String target, boolean formality, Integer icon, boolean favorite, int frequency) {
            this.name = name;
            this.target = target;
            this.formality = formality;
            this.icon = icon;
            this.favorite = favorite;
            this.frequency = frequency;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Translation {
        public String detected_language;
        public String target_language;
        public CharSequence translated_text;

        Translation() {
        }

        Translation(String detected, String target, CharSequence text) {
            this.detected_language = detected;
            this.target_language = target;
            this.translated_text = text;
        }
    }

    public static class FragmentDialogDeepL extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Context context = getContext();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String key = prefs.getString("deepl_key", null);
            boolean formal = prefs.getBoolean("deepl_formal", true);
            boolean small = prefs.getBoolean("deepl_small", false);
            boolean replace = prefs.getBoolean("deepl_replace", false);
            boolean highlight = prefs.getBoolean("deepl_highlight", true);
            boolean html = prefs.getBoolean("deepl_html", false);
            int subscription = prefs.getInt("deepl_subscription", BuildConfig.DEBUG ? 17 : 0);

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_deepl, null);
            final ImageButton ibInfo = view.findViewById(R.id.ibInfo);
            final TextInputLayout tilKey = view.findViewById(R.id.tilKey);
            final CheckBox cbFormal = view.findViewById(R.id.cbFormal);
            final TextView tvFormal = view.findViewById(R.id.tvFormal);
            final CheckBox cbSmall = view.findViewById(R.id.cbSmall);
            final CheckBox cbReplace = view.findViewById(R.id.cbReplace);
            final CheckBox cbHighlight = view.findViewById(R.id.cbHighlight);
            final CheckBox cbHtml = view.findViewById(R.id.cbHtml);
            final TextView tvUsage = view.findViewById(R.id.tvUsage);
            final TextView tvPrivacy = view.findViewById(R.id.tvPrivacy);

            ibInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(v.getContext(), 167);
                }
            });

            cbSmall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cbReplace.setEnabled(!isChecked);
                }
            });

            tvUsage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Helper.view(view.getContext(), Uri.parse(PLAN_URI), true);
                }
            });

            tvPrivacy.setPaintFlags(tvPrivacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.view(v.getContext(), Uri.parse(PRIVACY_URI), false);
                }
            });

            tilKey.getEditText().setText(key);
            cbFormal.setChecked(formal);

            try {
                List<String> formals = new ArrayList<>();
                for (Language lang : getTargetLanguages(context, false))
                    if (lang.formality)
                        formals.add(lang.name);

                tvFormal.setText(TextUtils.join(", ", formals));
            } catch (Throwable ex) {
                tvFormal.setText(Log.formatThrowable(ex, false));
            }

            cbSmall.setChecked(small);
            cbReplace.setChecked(replace);
            cbReplace.setEnabled(!small);
            cbHighlight.setChecked(highlight);
            cbHtml.setChecked(html);

            tvUsage.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(key)) {
                Bundle args = new Bundle();
                args.putString("key", key);

                new SimpleTask<Integer[]>() {
                    @Override
                    protected Integer[] onExecute(Context context, Bundle args) throws Throwable {
                        return DeepL.getUsage(context);
                    }

                    @Override
                    protected void onExecuted(Bundle args, Integer[] usage) {
                        String used = getString(R.string.title_translate_usage,
                                Helper.humanReadableByteCount(usage[0]),
                                Helper.humanReadableByteCount(usage[1]),
                                Math.round(100f * usage[0] / usage[1]));

                        if (subscription > 0) {
                            Calendar next = Calendar.getInstance();
                            next.set(Calendar.MILLISECOND, 0);
                            next.set(Calendar.SECOND, 0);
                            next.set(Calendar.MINUTE, 0);
                            next.set(Calendar.HOUR_OF_DAY, 0);
                            long today = next.getTimeInMillis();
                            if (next.get(Calendar.DATE) > subscription)
                                next.add(Calendar.MONTH, 1);
                            next.set(Calendar.DATE, subscription);
                            int remaining = (int) ((next.getTimeInMillis() - today) / (24 * 3600 * 1000L));

                            if (remaining > 0)
                                used += " +" + remaining;
                        }

                        tvUsage.setText(used);
                        tvUsage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        tvUsage.setText(Log.formatThrowable(ex, false));
                        tvUsage.setVisibility(View.VISIBLE);
                    }
                }.execute(this, new Bundle(), "deepl:usage");
            }

            return new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String key = tilKey.getEditText().getText().toString().trim();
                            SharedPreferences.Editor editor = prefs.edit();
                            if (TextUtils.isEmpty(key))
                                editor.remove("deepl_key");
                            else
                                editor.putString("deepl_key", key);
                            editor.putBoolean("deepl_formal", cbFormal.isChecked());
                            editor.putBoolean("deepl_small", cbSmall.isChecked());
                            editor.putBoolean("deepl_replace", cbReplace.isChecked());
                            editor.putBoolean("deepl_highlight", cbHighlight.isChecked());
                            editor.putBoolean("deepl_html", cbHtml.isChecked());
                            editor.apply();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
