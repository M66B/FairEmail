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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FragmentDialogSummarize extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_summarize, null);
        final TextView tvSummary = view.findViewById(R.id.tvSummary);
        final ContentLoadingProgressBar pbWait = view.findViewById(R.id.pbWait);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        int message_zoom = prefs.getInt("message_zoom", 100);

        float textSize = Helper.getTextSize(context, zoom) * message_zoom / 100f;
        tvSummary.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        tvSummary.setText(null);

        new SimpleTask<String>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                File file = EntityMessage.getFile(context, id);
                if (!file.exists())
                    return null;

                Document d = JsoupEx.parse(file);
                d = HtmlHelper.sanitizeView(context, d, false);
                HtmlHelper.removeSignatures(d);
                d.select("blockquote").remove();
                HtmlHelper.truncate(d, HtmlHelper.MAX_TRANSLATABLE_TEXT_SIZE);
                String text = d.text();
                if (TextUtils.isEmpty(text))
                    return null;

                if (OpenAI.isAvailable(context)) {
                    String model = prefs.getString("openai_model", "gpt-3.5-turbo");
                    float temperature = prefs.getFloat("openai_temperature", 0.5f);

                    List<OpenAI.Message> result = new ArrayList<>();
                    result.add(new OpenAI.Message(OpenAI.ASSISTANT, OpenAI.SUMMARY_PROMPT));
                    result.add(new OpenAI.Message(OpenAI.USER, text));
                    OpenAI.Message[] completions =
                            OpenAI.completeChat(context, model, result.toArray(new OpenAI.Message[0]), temperature, 1);
                    StringBuilder sb = new StringBuilder();
                    for (OpenAI.Message completion : completions) {
                        if (sb.length() != 0)
                            sb.append('\n');
                        sb.append(completion.getContent());
                    }
                    return sb.toString();
                } else if (Gemini.isAvailable(context)) {
                    String model = prefs.getString("gemini_model", "gemini-pro");

                    String[] result = Gemini.generate(context, model, new String[]{Gemini.SUMMARY_PROMPT, text});
                    return TextUtils.join("\n", result);
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, String text) {
                tvSummary.setText(text);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                tvSummary.setText(new ThrowableWrapper(ex).toSafeString());
            }
        }.execute(this, getArguments(), "message:summarize");

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.cancel, null);

        return builder.create();
    }
}
