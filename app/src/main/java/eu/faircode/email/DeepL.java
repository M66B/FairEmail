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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DeepL {
    // https://www.deepl.com/docs-api/
    private static JSONArray jlanguages = null;

    private static final int DEEPL_TIMEOUT = 20; // seconds
    private static final String PLAN_URI = "https://www.deepl.com/pro-account/plan";
    private static final String PRIVACY_URI = "https://www.deepl.com/privacy/";

    // curl https://api-free.deepl.com/v2/languages \
    //	-d auth_key=... \
    //	-d type=target

    public static boolean isAvailable(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("deepl_enabled", false);
    }

    public static boolean canTranslate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String deepl_key = prefs.getString("deepl_key", null);
        return !TextUtils.isEmpty(deepl_key);
    }

    public static List<Language> getTargetLanguages(Context context) {
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

                Locale locale = Locale.forLanguageTag(target);
                if (locale != null)
                    name = locale.getDisplayName();

                int frequency = prefs.getInt("translated_" + target, 0);
                if (BuildConfig.DEBUG && frequency > 0)
                    name += " ★";

                String resname = "language_" + target.toLowerCase().replace('-', '_');
                int resid = res.getIdentifier(resname, "drawable", pkg);

                languages.add(new Language(name, target, resid == 0 ? null : resid));
                frequencies.put(target, frequency);
            }

            Collator collator = Collator.getInstance(Locale.getDefault());
            collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
            Collections.sort(languages, new Comparator<Language>() {
                @Override
                public int compare(Language l1, Language l2) {
                    int freq1 = frequencies.get(l1.target);
                    int freq2 = frequencies.get(l2.target);

                    if (freq1 == freq2 || !BuildConfig.DEBUG)
                        return collator.compare(l1.name, l2.name);
                    else
                        return -Integer.compare(freq1, freq2);
                }
            });

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

    public static Pair<Integer, Integer> getParagraph(EditText etBody) {
        int start = etBody.getSelectionStart();
        int end = etBody.getSelectionEnd();
        Editable edit = etBody.getText();

        if (start < 0 || end < 0)
            return null;

        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        // Expand selection at start
        while (start > 0 && edit.charAt(start - 1) != '\n')
            start--;

        if (start == end && end < edit.length())
            end++;

        // Expand selection at end
        while (end > 0 && end < edit.length() && edit.charAt(end - 1) != '\n')
            end++;

        // Trim start
        while (start < edit.length() - 1 && edit.charAt(start) == '\n')
            start++;

        // Trim end
        while (end > 0 && edit.charAt(end - 1) == '\n')
            end--;

        if (start < end)
            return new Pair(start, end);

        return null;
    }

    public static Translation translate(String text, String target, Context context) throws IOException, JSONException {
        // https://www.deepl.com/docs-api/translating-text/request/
        String request =
                "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8.name()) +
                        "&target_lang=" + URLEncoder.encode(target, StandardCharsets.UTF_8.name());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = prefs.getString("deepl_key", null);

        URL url = new URL(getBaseUri(context) + "translate?auth_key=" + key);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setReadTimeout(DEEPL_TIMEOUT * 1000);
        connection.setConnectTimeout(DEEPL_TIMEOUT * 1000);
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
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
                throw new FileNotFoundException(error);
            }

            String response = Helper.readStream(connection.getInputStream());

            JSONObject jroot = new JSONObject(response);
            JSONArray jtranslations = jroot.getJSONArray("translations");
            if (jtranslations.length() == 0)
                throw new FileNotFoundException();
            JSONObject jtranslation = (JSONObject) jtranslations.get(0);

            Translation result = new Translation();
            result.target_language = target;
            result.detected_language = jtranslation.getString("detected_source_language");
            result.translated_text = jtranslation.getString("text");
            return result;
        } finally {
            connection.disconnect();
        }
    }

    public static Integer[] getUsage(Context context) throws IOException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = prefs.getString("deepl_key", null);

        // https://www.deepl.com/docs-api/other-functions/monitoring-usage/
        URL url = new URL(getBaseUri(context) + "usage?auth_key=" + key);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setReadTimeout(DEEPL_TIMEOUT * 1000);
        connection.setConnectTimeout(DEEPL_TIMEOUT * 1000);
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
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
                throw new FileNotFoundException(error);
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

    private static String getBaseUri(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String domain = (prefs.getBoolean("deepl_pro", false)
                ? "api.deepl.com" : "api-free.deepl.com");
        return "https://" + domain + "/v2/";
    }

    public static class Language {
        public String name;
        public String target;
        public Integer icon;

        private Language(String name, String target, Integer icon) {
            this.name = name;
            this.target = target;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Translation {
        public String detected_language;
        public String target_language;
        public String translated_text;
    }

    public static class FragmentDialogDeepL extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Context context = getContext();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String key = prefs.getString("deepl_key", null);
            boolean pro = prefs.getBoolean("deepl_pro", false);
            boolean small = prefs.getBoolean("deepl_small", false);

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_deepl, null);
            final ImageButton ibInfo = view.findViewById(R.id.ibInfo);
            final EditText etKey = view.findViewById(R.id.etKey);
            final CheckBox cbPro = view.findViewById(R.id.cbPro);
            final CheckBox cbSmall = view.findViewById(R.id.cbSmall);
            final TextView tvUsage = view.findViewById(R.id.tvUsage);
            final TextView tvPrivacy = view.findViewById(R.id.tvPrivacy);

            ibInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(v.getContext(), 167, true);
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

            etKey.setText(key);
            cbPro.setChecked(pro);
            cbSmall.setChecked(small);

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
                        tvUsage.setText(getString(R.string.title_translate_usage,
                                Helper.humanReadableByteCount(usage[0]),
                                Helper.humanReadableByteCount(usage[1]),
                                Math.round(100f * usage[0] / usage[1])));
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
                            String key = etKey.getText().toString().trim();
                            SharedPreferences.Editor editor = prefs.edit();
                            if (TextUtils.isEmpty(key))
                                editor.remove("deepl_key");
                            else
                                editor.putString("deepl_key", key);
                            editor.putBoolean("deepl_pro", cbPro.isChecked());
                            editor.putBoolean("deepl_small", cbSmall.isChecked());
                            editor.apply();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
