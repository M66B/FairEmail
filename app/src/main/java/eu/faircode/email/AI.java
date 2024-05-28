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
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AI {
    private static final int MAX_SUMMARIZE_TEXT_SIZE = 4 * 1024;

    static boolean isAvailable(Context context) {
        return (OpenAI.isAvailable(context) || Gemini.isAvailable(context));
    }

    static String completeChat(Context context, long id, CharSequence body, long template) throws JSONException, IOException {
        String reply = null;
        if (body == null || TextUtils.isEmpty(body.toString().trim())) {
            body = "?";

            File file = EntityMessage.getFile(context, id);
            if (file.exists()) {
                Document d = JsoupEx.parse(file);
                Elements ref = d.select("div[fairemail=reference]");
                if (!ref.isEmpty()) {
                    d = Document.createShell("");
                    d.appendChildren(ref);

                    HtmlHelper.removeSignatures(d);
                    HtmlHelper.truncate(d, MAX_SUMMARIZE_TEXT_SIZE);

                    reply = d.text();
                    if (TextUtils.isEmpty(reply.trim()))
                        reply = null;
                }
            }
        }

        String prompt = null;
        DB db = DB.getInstance(context);
        EntityAnswer t = db.answer().getAnswer(template);
        if (t != null) {
            String html = t.getHtml(context, null);
            prompt = JsoupEx.parse(html).body().text();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (OpenAI.isAvailable(context)) {
            String model = prefs.getString("openai_model", OpenAI.DEFAULT_MODEL);
            float temperature = prefs.getFloat("openai_temperature", OpenAI.DEFAULT_TEMPERATURE);
            boolean multimodal = prefs.getBoolean("openai_multimodal", false);
            String answer = prefs.getString("openai_answer", OpenAI.DEFAULT_ANSWER_PROMPT);

            List<OpenAI.Message> messages = new ArrayList<>();

            if (reply == null) {
                if (body instanceof Spannable && multimodal)
                    messages.add(new OpenAI.Message(OpenAI.USER,
                            OpenAI.Content.get((Spannable) body, id, context)));
                else
                    messages.add(new OpenAI.Message(OpenAI.USER, new OpenAI.Content[]{
                            new OpenAI.Content(OpenAI.CONTENT_TEXT, body.toString())}));
            } else {
                messages.add(new OpenAI.Message(OpenAI.USER, new OpenAI.Content[]{
                        new OpenAI.Content(OpenAI.CONTENT_TEXT, prompt == null ? answer : prompt)}));
                messages.add(new OpenAI.Message(OpenAI.USER, new OpenAI.Content[]{
                        new OpenAI.Content(OpenAI.CONTENT_TEXT, reply)}));
            }

            OpenAI.Message[] completions = OpenAI.completeChat(context,
                    model, messages.toArray(new OpenAI.Message[0]), temperature, 1);

            StringBuilder sb = new StringBuilder();
            for (OpenAI.Message completion : completions)
                for (OpenAI.Content content : completion.getContent())
                    if (OpenAI.CONTENT_TEXT.equals(content.getType())) {
                        if (sb.length() > 0)
                            sb.append('\n');
                        sb.append(content.getContent()
                                .replaceAll("^\\n+", "")
                                .replaceAll("\\n+$", ""));
                    }
            return sb.toString();
        } else if (Gemini.isAvailable(context)) {
            String model = prefs.getString("gemini_model", Gemini.DEFAULT_MODEL);
            float temperature = prefs.getFloat("gemini_temperature", Gemini.DEFAULT_TEMPERATURE);
            String answer = prefs.getString("gemini_answer", Gemini.DEFAULT_ANSWER_PROMPT);

            List<Gemini.Message> messages = new ArrayList<>();

            if (reply == null)
                messages.add(new Gemini.Message(Gemini.USER,
                        new String[]{Gemini.truncateParagraphs(body.toString())}));
            else {
                messages.add(new Gemini.Message(Gemini.USER, new String[]{prompt == null ? answer : prompt}));
                messages.add(new Gemini.Message(Gemini.USER, new String[]{reply}));
            }

            Gemini.Message[] completions = Gemini.generate(context,
                    model, messages.toArray(new Gemini.Message[0]), temperature, 1);

            StringBuilder sb = new StringBuilder();
            for (Gemini.Message completion : completions)
                for (String result : completion.getContent()) {
                    if (sb.length() > 0)
                        sb.append('\n');
                    sb.append(result
                            .replaceAll("^\\n+", "")
                            .replaceAll("\\n+$", ""));
                }
            return sb.toString();
        } else
            throw new IllegalArgumentException("No AI available");
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

    static String getSummaryText(Context context, EntityMessage message) throws JSONException, IOException {
        File file = message.getFile(context);
        if (!file.exists())
            return null;

        Document d = JsoupEx.parse(file);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean remove_signatures = prefs.getBoolean("remove_signatures", false);
        if (remove_signatures)
            HtmlHelper.removeSignatures(d);

        HtmlHelper.removeQuotes(d);

        d = HtmlHelper.sanitizeView(context, d, false);

        HtmlHelper.truncate(d, MAX_SUMMARIZE_TEXT_SIZE);

        if (OpenAI.isAvailable(context)) {
            String model = prefs.getString("openai_model", OpenAI.DEFAULT_MODEL);
            float temperature = prefs.getFloat("openai_temperature", OpenAI.DEFAULT_TEMPERATURE);
            String prompt = prefs.getString("openai_summarize", OpenAI.DEFAULT_SUMMARY_PROMPT);
            boolean multimodal = prefs.getBoolean("openai_multimodal", false);

            List<OpenAI.Message> input = new ArrayList<>();
            input.add(new OpenAI.Message(OpenAI.USER,
                    new OpenAI.Content[]{new OpenAI.Content(OpenAI.CONTENT_TEXT, prompt)}));

            if (!TextUtils.isEmpty(message.subject))
                input.add(new OpenAI.Message(OpenAI.USER,
                        new OpenAI.Content[]{new OpenAI.Content(OpenAI.CONTENT_TEXT, message.subject)}));

            if (multimodal) {
                SpannableStringBuilder ssb = HtmlHelper.fromDocument(context, d, null, null);
                input.add(new OpenAI.Message(OpenAI.USER,
                        OpenAI.Content.get(ssb, message.id, context)));
            } else
                input.add(new OpenAI.Message(OpenAI.USER, new OpenAI.Content[]{
                        new OpenAI.Content(OpenAI.CONTENT_TEXT, d.text())}));

            OpenAI.Message[] completions =
                    OpenAI.completeChat(context, model, input.toArray(new OpenAI.Message[0]), temperature, 1);

            StringBuilder sb = new StringBuilder();
            for (OpenAI.Message completion : completions)
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

            String body = d.text();

            List<String> texts = new ArrayList<>();
            texts.add(prompt);
            if (!TextUtils.isEmpty(message.subject))
                texts.add(message.subject);
            if (!TextUtils.isEmpty(body))
                texts.add(body);
            Gemini.Message content = new Gemini.Message(Gemini.USER, texts.toArray(new String[0]));

            Gemini.Message[] completions =
                    Gemini.generate(context, model, new Gemini.Message[]{content}, temperature, 1);

            StringBuilder sb = new StringBuilder();
            for (Gemini.Message completion : completions)
                for (String result : completion.getContent()) {
                    if (sb.length() != 0)
                        sb.append('\n');
                    sb.append(result);
                }
            return sb.toString();
        } else
            throw new IllegalArgumentException("No AI available");
    }
}
