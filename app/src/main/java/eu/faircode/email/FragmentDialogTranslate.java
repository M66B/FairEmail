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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;

public class FragmentDialogTranslate extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_translate, null);
        final ImageButton ibAll = view.findViewById(R.id.ibAll);
        final Spinner spLanguage = view.findViewById(R.id.spLanguage);
        final TextView tvText = view.findViewById(R.id.tvText);
        final ContentLoadingProgressBar pbWait = view.findViewById(R.id.pbWait);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        int message_zoom = prefs.getInt("message_zoom", 100);

        float textSize = Helper.getTextSize(context, zoom) * message_zoom / 100f;
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        ibAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeepL.Language language = (DeepL.Language) spLanguage.getSelectedItem();
                if (language == null)
                    return;

                Bundle args = new Bundle();
                args.putLong("id", getArguments().getLong("id"));
                args.putString("target", language.target);

                new SimpleTask<DeepL.Translation>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        ibAll.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        ibAll.setEnabled(true);
                    }

                    @Override
                    protected DeepL.Translation onExecute(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");
                        String target = args.getString("target");
                        String text = processMessage(id, context);
                        return DeepL.translate(text, false, target, context);
                    }

                    @Override
                    protected void onExecuted(Bundle args, DeepL.Translation translation) {
                        int textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
                        SpannableStringBuilder ssb = new SpannableStringBuilderEx(translation.translated_text);

                        ssb.setSpan(new StyleSpan(Typeface.ITALIC), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.setSpan(new ForegroundColorSpan(textColorPrimary), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        Locale source = Locale.forLanguageTag(translation.detected_language);
                        Locale target = Locale.forLanguageTag(translation.target_language);

                        String lang = "[" + source.getDisplayLanguage(target) + "]\n\n";
                        ssb.insert(0, lang);

                        ssb.setSpan(new StyleSpan(Typeface.ITALIC), 0, lang.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, lang.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        tvText.setText(ssb);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex, !(ex instanceof IOException));
                    }
                }.execute(FragmentDialogTranslate.this, args, "translate:all");
            }
        });

        List<DeepL.Language> languages = DeepL.getTargetLanguages(context, false);
        ArrayAdapter<DeepL.Language> adapter = new ArrayAdapter<DeepL.Language>(context, android.R.layout.simple_spinner_item, android.R.id.text1, languages) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return _getView(position, super.getView(position, convertView, parent));
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return _getView(position, super.getDropDownView(position, convertView, parent));
            }

            private View _getView(int position, View view) {
                DeepL.Language language = getItem(position);
                if (language != null && language.icon != null && language.name != null) {
                    TextView tv = view.findViewById(android.R.id.text1);

                    Drawable icon = ContextCompat.getDrawable(context, language.icon);
                    int iconSize = context.getResources()
                            .getDimensionPixelSize(R.dimen.menu_item_icon_size);
                    icon.setBounds(0, 0, iconSize, iconSize);
                    ImageSpan imageSpan = new CenteredImageSpan(icon);

                    SpannableStringBuilder ssb = new SpannableStringBuilderEx(language.name);
                    ssb.insert(0, "\uFFFC\u2002"); // object replacement character, en space
                    ssb.setSpan(imageSpan, 0, 1, 0);

                    tv.setText(ssb);
                }

                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(adapter);

        String current = prefs.getString("deepl_target", null);

        for (int pos = 0; pos < languages.size(); pos++)
            if (languages.get(pos).target.equals(current)) {
                spLanguage.setSelection(pos);
                break;
            }

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putString("deepl_target", languages.get(position).target).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("deepl_target").apply();
            }
        });

        tvText.setText(null);

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
                return processMessage(id, context);
            }

            @Override
            protected void onExecuted(Bundle args, String text) {
                tvText.setText(text);

                tvText.setMovementMethod(new ArrowKeyMovementMethod() {
                    @Override
                    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP)
                            translate(widget, buffer, event);
                        return super.onTouchEvent(widget, buffer, event);
                    }

                    private void translate(TextView widget, Spannable buffer, MotionEvent event) {
                        int off = Helper.getOffset(widget, buffer, event);
                        if (off < 0)
                            return;

                        int start = off;
                        while (start > 0 && buffer.charAt(start - 1) != '\n')
                            start--;

                        int end = off;
                        while (end < buffer.length() && buffer.charAt(end - 1) != '\n')
                            end++;

                        if (end <= start)
                            return;

                        StyleSpan[] spans = buffer.getSpans(start, end, StyleSpan.class);
                        if (spans != null && spans.length > 0)
                            return;

                        final StyleSpan mark = new StyleSpan(Typeface.ITALIC);
                        buffer.setSpan(mark, start, end, 0);

                        DeepL.Language language = (DeepL.Language) spLanguage.getSelectedItem();
                        if (language == null)
                            return;

                        Bundle args = new Bundle();
                        args.putString("target", language.target);
                        args.putString("text", buffer.subSequence(start, end).toString());

                        new SimpleTask<DeepL.Translation>() {
                            @Override
                            protected void onPreExecute(Bundle args) {
                                pbWait.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected void onPostExecute(Bundle args) {
                                pbWait.setVisibility(View.GONE);
                            }

                            @Override
                            protected DeepL.Translation onExecute(Context context, Bundle args) throws Throwable {
                                String text = args.getString("text");
                                String target = args.getString("target");
                                return DeepL.translate(text, false, target, context);
                            }

                            @Override
                            protected void onExecuted(Bundle args, DeepL.Translation translation) {
                                SpannableStringBuilder ssb = new SpannableStringBuilderEx(tvText.getText());
                                int start = ssb.getSpanStart(mark);
                                int end = ssb.getSpanEnd(mark);
                                int textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);

                                ssb.removeSpan(mark);

                                ssb = ssb.replace(start, end, translation.translated_text);
                                end = start + translation.translated_text.length();

                                ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                ssb.setSpan(new ForegroundColorSpan(textColorPrimary), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                Locale source = Locale.forLanguageTag(translation.detected_language);
                                Locale target = Locale.forLanguageTag(translation.target_language);

                                String lang = "[" + source.getDisplayLanguage(target) + "] ";
                                ssb.insert(start, lang);

                                ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, start + lang.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), start, start + lang.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                tvText.setText(ssb);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                SpannableStringBuilder ssb = new SpannableStringBuilderEx(tvText.getText());
                                ssb.removeSpan(mark);
                                tvText.setText(ssb);

                                Log.unexpectedError(getParentFragmentManager(), ex, !(ex instanceof IOException));
                            }
                        }.execute(FragmentDialogTranslate.this, args, "paragraph:translate");
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                tvText.setText(new ThrowableWrapper(ex).toSafeString());
            }
        }.execute(this, getArguments(), "message:translate");

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.title_copy_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String html = HtmlHelper.toHtml((Spanned) tvText.getText(), context);
                        String text = HtmlHelper.getText(context, html);
                        ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);
                        cbm.setPrimaryClip(ClipData.newHtmlText(getString(R.string.app_name), text, html));

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                            ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
                    }
                });

        return builder.create();
    }

    private static String processMessage(long id, Context context) throws IOException {
        DB db = DB.getInstance(context);
        EntityMessage message = db.message().getMessage(id);

        File file = EntityMessage.getFile(context, id);
        if (!file.exists())
            return null;

        Document d = JsoupEx.parse(file);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean remove_signatures = prefs.getBoolean("remove_signatures", false);
        if (remove_signatures)
            HtmlHelper.removeSignatures(d);

        HtmlHelper.removeQuotes(d, true);

        d = HtmlHelper.sanitizeView(context, d, false);

        HtmlHelper.truncate(d, HtmlHelper.MAX_TRANSLATABLE_TEXT_SIZE);

        SpannableStringBuilder ssb = HtmlHelper.fromDocument(context, d, null, null);

        if (message != null) {
            if (!TextUtils.isEmpty(message.subject)) {
                ssb.insert(0, "\n\n");
                ssb.insert(0, message.subject);
            }

            List<TupleIdentityEx> identities = db.identity().getComposableIdentities(message.account);
            Address[] from = (message.fromSelf(identities) ? message.to : message.from);
            if (from != null && from.length > 0) {
                ssb.insert(0, "\n\n");
                ssb.insert(0, MessageHelper.formatAddresses(from));
            }
        }

        return ssb.toString()
                .replace("\uFFFC", "") // Object replacement character
                .replaceAll("\n\\s+\n", "\n")
                .replaceAll("\n+", "\n\n");
    }
}
