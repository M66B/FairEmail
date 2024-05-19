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

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AI {
    static boolean isAvailable(Context context) {
        return (OpenAI.isAvailable(context) || Gemini.isAvailable(context));
    }

    static String completeChat(Context context, long id, CharSequence body) throws JSONException, IOException {
        if (body == null || body.length() == 0)
            return null;

        if (OpenAI.isAvailable(context)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String model = prefs.getString("openai_model", OpenAI.DEFAULT_MODEL);
            float temperature = prefs.getFloat("openai_temperature", OpenAI.DEFAULT_TEMPERATURE);

            OpenAI.Message message;
            if (body instanceof Spannable)
                message = new OpenAI.Message(OpenAI.USER, OpenAI.Content.get((Spannable) body, id, context));
            else
                message = new OpenAI.Message(OpenAI.USER, new OpenAI.Content[]{
                        new OpenAI.Content(OpenAI.CONTENT_TEXT, body.toString())});

            OpenAI.Message[] completions =
                    OpenAI.completeChat(context, model, new OpenAI.Message[]{message}, temperature, 1);

            StringBuilder sb = new StringBuilder();
            for (OpenAI.Message completion : completions)
                for (OpenAI.Content content : completion.getContent())
                    if (OpenAI.CONTENT_TEXT.equals(content.getType())) {
                        if (sb.length() > 0)
                            sb.append('\n');
                        sb.append(content.getContent()
                                .replaceAll("^\\n+", "").replaceAll("\\n+$", ""));
                    }

            return sb.toString();
        } else if (Gemini.isAvailable(context)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String model = prefs.getString("gemini_model", Gemini.DEFAULT_MODEL);
            float temperature = prefs.getFloat("gemini_temperature", Gemini.DEFAULT_TEMPERATURE);

            Gemini.Message message = new Gemini.Message(Gemini.USER,
                    new String[]{Gemini.truncateParagraphs(body.toString())});
            Gemini.Message[] completions = Gemini.generate(context, model, new Gemini.Message[]{message}, temperature, 1);
            if (completions.length == 0)
                return null;

            return TextUtils.join("\n", completions[0].getContent())
                    .replaceAll("^\\n+", "").replaceAll("\\n+$", "");
        } else
            return null;
    }

    static String getSummarizePrompt(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (OpenAI.isAvailable(context))
            return prefs.getString("openai_summarize", OpenAI.DEFAULT_SUMMARY_PROMPT);
        else if (Gemini.isAvailable(context))
            return prefs.getString("gemini_summarize", Gemini.DEFAULT_SUMMARY_PROMPT);
        else
            return context.getString(R.string.title_summarize);
    }

    static String summarize(Context context, long id, String subject, Document d) throws JSONException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (OpenAI.isAvailable(context)) {
            String model = prefs.getString("openai_model", OpenAI.DEFAULT_MODEL);
            float temperature = prefs.getFloat("openai_temperature", OpenAI.DEFAULT_TEMPERATURE);
            String prompt = prefs.getString("openai_summarize", OpenAI.DEFAULT_SUMMARY_PROMPT);

            List<OpenAI.Message> input = new ArrayList<>();
            input.add(new OpenAI.Message(OpenAI.USER,
                    new OpenAI.Content[]{new OpenAI.Content(OpenAI.CONTENT_TEXT, prompt)}));

            if (!TextUtils.isEmpty(subject))
                input.add(new OpenAI.Message(OpenAI.USER,
                        new OpenAI.Content[]{new OpenAI.Content(OpenAI.CONTENT_TEXT, subject)}));

            SpannableStringBuilder ssb = HtmlHelper.fromDocument(context, d, null, null);
            input.add(new OpenAI.Message(OpenAI.USER,
                    OpenAI.Content.get(ssb, id, context)));

            OpenAI.Message[] result =
                    OpenAI.completeChat(context, model, input.toArray(new OpenAI.Message[0]), temperature, 1);

            if (result.length == 0)
                return null;

            StringBuilder sb = new StringBuilder();
            for (OpenAI.Message completion : result)
                for (OpenAI.Content content : completion.getContent())
                    if (OpenAI.CONTENT_TEXT.equals(content.getType())) {
                        if (sb.length() != 0)
                            sb.append('\n');
                        sb.append(content.getContent());
                    }
            return sb.toString();
        } else if (Gemini.isAvailable(context)) {
            String model = prefs.getString("gemini_model", Gemini.DEFAULT_MODEL);
            float temperature = prefs.getFloat("gemini_temperature", Gemini.DEFAULT_TEMPERATURE);
            String prompt = prefs.getString("gemini_summarize", Gemini.DEFAULT_SUMMARY_PROMPT);

            String text = d.text();
            if (TextUtils.isEmpty(text))
                return null;
            Gemini.Message content = new Gemini.Message(Gemini.USER, new String[]{prompt, text});

            Gemini.Message[] result =
                    Gemini.generate(context, model, new Gemini.Message[]{content}, temperature, 1);

            if (result.length == 0)
                return null;

            return TextUtils.join("\n", result[0].getContent());
        } else
            return null;
    }
}
