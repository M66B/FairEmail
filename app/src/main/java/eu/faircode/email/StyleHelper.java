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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.LogPrinter;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StyleHelper {
    static final List<Class<?>> CLEAR_STYLES = Collections.unmodifiableList(Arrays.asList(
            StyleSpan.class,
            UnderlineSpan.class,
            RelativeSizeSpan.class,
            BackgroundColorSpan.class,
            ForegroundColorSpan.class,
            AlignmentSpan.class, AlignmentSpan.Standard.class,
            BulletSpanEx.class, NumberSpan.class,
            QuoteSpan.class, IndentSpan.class,
            SubscriptSpanEx.class, SuperscriptSpanEx.class,
            StrikethroughSpan.class,
            URLSpan.class,
            TypefaceSpan.class, CustomTypefaceSpan.class,
            MarkSpan.class,
            InsertedSpan.class
    ));

    private static Integer[] ids = new Integer[]{
            R.id.menu_bold,
            R.id.menu_italic,
            R.id.menu_underline,
            R.id.menu_style_size,
            R.id.menu_style_background,
            R.id.menu_style_color,
            R.id.menu_style_font,
            R.id.menu_style_align,
            R.id.menu_style_list,
            R.id.menu_style_indentation,
            R.id.menu_style_blockquote,
            R.id.menu_style_mark,
            R.id.menu_style_subscript,
            R.id.menu_style_superscript,
            R.id.menu_style_strikethrough,
            R.id.menu_style_insert_line,
            R.id.menu_style_insert_answer,
            R.id.menu_style_spell_check,
            R.id.menu_style_password,
            R.id.menu_style_code,
            R.id.menu_style_reverse,
            R.id.menu_style_clear,
            R.id.menu_style_settings
    };

    private static final int group_style_size = 1;
    private static final int group_style_font_standard = 2;
    private static final int group_style_font_custom = 3;
    private static final int group_style_align = 4;
    private static final int group_style_list = 5;
    private static final int group_style_indentation = 6;

    static void wire(LifecycleOwner owner, View view, EditText etBody) {
        View.OnClickListener styleListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyleHelper.apply(v.getId(), owner, v, etBody);
            }
        };

        for (int id : ids) {
            View v = view.findViewById(id);

            v.setOnClickListener(styleListener);

            if (id == R.id.menu_style_insert_answer)
                v.setVisibility(View.GONE);
            else if (id == R.id.menu_style_spell_check)
                v.setVisibility(
                        BuildConfig.DEBUG && LanguageTool.isEnabled(v.getContext())
                                ? View.VISIBLE : View.GONE);
            else if (id == R.id.menu_style_password)
                v.setVisibility(
                        !BuildConfig.PLAY_STORE_RELEASE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                ? View.VISIBLE : View.GONE);
            else if (id == R.id.menu_style_code || id == R.id.menu_style_reverse)
                v.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        }

        view.findViewById(R.id.menu_link).setVisibility(View.GONE);
    }

    static TextWatcher getTextWatcher(EditText etBody) {
        // https://developer.android.com/reference/android/text/TextWatcher
        return new TextWatcher() {
            private Integer added = null;
            private Integer removed = null;
            private Integer inserted = null;

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                if (count == 1 && after == 0 && (start == 0 || text.charAt(start) == '\n')) {
                    Log.i("Removed=" + start);
                    removed = start;
                }

                if (BuildConfig.DEBUG && count - after == 1 && start + after > 0) {
                    int replaced = start + after;
                    Spanned spanned = ((Spanned) text);
                    StyleHelper.InsertedSpan[] spans =
                            spanned.getSpans(replaced, replaced, StyleHelper.InsertedSpan.class);
                    for (StyleHelper.InsertedSpan span : spans) {
                        int end = spanned.getSpanEnd(span);
                        Log.i("Replaced=" + replaced);
                        if (end - 1 == replaced) {
                            inserted = end - 1;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                int index = start + before;

                if (count - before == 1 && index > 0) {
                    char c = text.charAt(index);
                    if (c == '\n') {
                        Log.i("Added=" + index);
                        added = index;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (etBody == null)
                    return;

                LogPrinter lp = null;
                if (BuildConfig.DEBUG &&
                        (added != null || removed != null))
                    lp = new LogPrinter(android.util.Log.INFO, "FairEmail");

                if (lp != null)
                    TextUtils.dumpSpans(text, new LogPrinter(android.util.Log.INFO, "FairEmail"), "---before>");

                if (added != null)
                    try {
                        // break block quotes
                        boolean broken = false;
                        QuoteSpan[] spans = text.getSpans(added + 1, added + 1, QuoteSpan.class);
                        for (QuoteSpan span : spans) {
                            int s = text.getSpanStart(span);
                            int e = text.getSpanEnd(span);
                            int f = text.getSpanFlags(span);
                            Log.i(span + " " + s + "..." + e + " added=" + added);

                            if (s > 0 && added - s > 0 && e - (added + 1) > 0 &&
                                    text.charAt(s - 1) == '\n' && text.charAt(added - 1) == '\n' &&
                                    text.charAt(added) == '\n' && text.charAt(e - 1) == '\n') {
                                broken = true;

                                QuoteSpan q1 = StyleHelper.clone(span, QuoteSpan.class, etBody.getContext());
                                text.setSpan(q1, s, added, f);
                                Log.i(span + " " + s + "..." + added);

                                QuoteSpan q2 = StyleHelper.clone(span, QuoteSpan.class, etBody.getContext());
                                text.setSpan(q2, added + 1, e, f);
                                Log.i(span + " " + (added + 1) + "..." + e);

                                text.removeSpan(span);
                            }
                        }

                        if (broken) {
                            CharacterStyle[] sspan = text.getSpans(added + 1, added + 1, CharacterStyle.class);
                            for (CharacterStyle span : sspan) {
                                int s = text.getSpanStart(span);
                                int e = text.getSpanEnd(span);
                                int f = text.getSpanFlags(span);
                                Log.i(span + " " + s + "..." + e + " start=" + added);

                                if (s <= added && added + 1 <= e) {
                                    CharacterStyle s1 = CharacterStyle.wrap(span);
                                    text.setSpan(s1, s, added, f);
                                    Log.i(span + " " + s + "..." + added);

                                    CharacterStyle s2 = CharacterStyle.wrap(span);
                                    text.setSpan(s2, added + 1, e, f);
                                    Log.i(span + " " + (added + 1) + "..." + e);

                                    text.removeSpan(span);
                                }
                            }

                            etBody.setSelection(added);
                        }

                        // Escape indent at end
                        IndentSpan[] indents = text.getSpans(added + 1, added + 1, IndentSpan.class);
                        for (IndentSpan indent : indents) {
                            int s = text.getSpanStart(indent);
                            int e = text.getSpanEnd(indent);
                            int f = text.getSpanFlags(indent);
                            if (e - 1 > s && added + 1 == e) {
                                text.removeSpan(indent);
                                text.setSpan(new IndentSpan(indent.getLeadingMargin(true)), s, e - 1, f);
                            }
                        }

                        boolean renum = false;
                        BulletSpan[] bullets = text.getSpans(added + 1, added + 1, BulletSpan.class);

                        int len = 0;
                        BulletSpan shortest = null;
                        for (BulletSpan span : bullets) {
                            int s = text.getSpanStart(span);
                            int e = text.getSpanEnd(span);
                            if (shortest == null || e - s < len) {
                                shortest = span;
                                len = e - s;
                            }
                        }

                        if (shortest != null) {
                            int s = text.getSpanStart(shortest);
                            int e = text.getSpanEnd(shortest);
                            int f = text.getSpanFlags(shortest) | Spanned.SPAN_PARAGRAPH;
                            Log.i(shortest + " " + s + "..." + e + " added=" + added);

                            if (s > 0 &&
                                    added + 1 > s && e > added + 1 &&
                                    text.charAt(s - 1) == '\n' && text.charAt(e - 1) == '\n') {
                                if (e - s > 2) {
                                    BulletSpan b1 = StyleHelper.clone(shortest, shortest.getClass(), etBody.getContext());
                                    text.setSpan(b1, s, added + 1, f);
                                    Log.i(shortest + " " + s + "..." + (added + 1));

                                    BulletSpan b2 = StyleHelper.clone(b1, shortest.getClass(), etBody.getContext());
                                    text.setSpan(b2, added + 1, e, f);
                                    Log.i(shortest + " " + (added + 1) + "..." + e);
                                }

                                renum = true;
                                text.removeSpan(shortest);
                            }
                        }

                        if (renum)
                            StyleHelper.renumber(text, false, etBody.getContext());

                        StyleHelper.markAsInserted(text, -1, -1);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        added = null;
                    }

                if (removed != null)
                    try {
                        ParagraphStyle[] ps = text.getSpans(removed, removed + 1, ParagraphStyle.class);
                        if (ps != null)
                            for (ParagraphStyle p : ps) {
                                int start = text.getSpanStart(p);
                                int end = text.getSpanEnd(p);
                                if (start >= removed && end <= removed + 1)
                                    text.removeSpan(p);
                            }

                        StyleHelper.renumber(text, true, etBody.getContext());
                    } finally {
                        removed = null;
                    }

                if (inserted != null)
                    try {
                        StyleHelper.InsertedSpan[] spans =
                                text.getSpans(inserted, inserted, StyleHelper.InsertedSpan.class);
                        for (StyleHelper.InsertedSpan span : spans) {
                            int start = text.getSpanStart(span);
                            int end = text.getSpanEnd(span);
                            if (end == inserted) {
                                for (Object o : text.getSpans(start, end, Object.class)) {
                                    int s = text.getSpanStart(o);
                                    int e = text.getSpanEnd(o);
                                    if (s <= e && s >= start && e <= end)
                                        text.removeSpan(o);
                                }
                                text.delete(start, end);
                                text.removeSpan(span);
                            }
                        }
                    } finally {
                        inserted = null;
                    }

                if (lp != null)
                    TextUtils.dumpSpans(text, lp, "---after>");
            }
        };
    }

    static boolean apply(int itemId, LifecycleOwner owner, View anchor, EditText etBody, Object... args) {
        return apply(-1, itemId, owner, anchor, etBody, args);
    }

    static boolean apply(int groupId, int itemId, LifecycleOwner owner, View anchor, EditText etBody, Object... args) {
        Log.i("Style action=" + groupId + ":" + itemId);

        try {
            int start = etBody.getSelectionStart();
            int end = etBody.getSelectionEnd();

            if (start < 0)
                start = 0;
            if (end < 0)
                end = 0;

            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            }

            if (start == end &&
                    itemId == R.id.menu_style_spell_check) {
                Pair<Integer, Integer> paragraph = getParagraph(etBody);
                if (paragraph == null)
                    return false;
                start = paragraph.first;
                end = paragraph.second;
            }

            if (start == end &&
                    itemId != R.id.menu_link &&
                    itemId != R.id.menu_clear &&
                    itemId != R.id.menu_style_align && groupId != group_style_align &&
                    itemId != R.id.menu_style_list && groupId != group_style_list &&
                    itemId != R.id.menu_style_indentation && groupId != group_style_indentation &&
                    itemId != R.id.menu_style_blockquote &&
                    itemId != R.id.menu_style_insert_line &&
                    itemId != R.id.menu_style_insert_answer &&
                    itemId != R.id.menu_style_spell_check &&
                    itemId != R.id.menu_style_settings) {
                Pair<Integer, Integer> word = getWord(etBody);
                if (word == null)
                    return false;
                start = word.first;
                end = word.second;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(etBody.getContext());
            boolean keep_selection = prefs.getBoolean("style_setting_keep_selection", false);

            if (groupId < 0) {
                if (itemId == R.id.menu_bold || itemId == R.id.menu_italic)
                    return setBoldItalic(itemId, etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_underline)
                    return setUnderline(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_size)
                    return selectSize(owner, anchor, etBody);
                else if (itemId == R.id.menu_style_background)
                    return selectBackground(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_color)
                    return selectColor(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_font)
                    return selectFont(owner, anchor, etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_align)
                    return selectAlignment(owner, anchor, etBody, start, end);
                else if (itemId == R.id.menu_style_list)
                    return selectList(owner, anchor, etBody, start, end);
                else if (itemId == R.id.menu_style_indentation)
                    return selectIndentation(owner, anchor, etBody, start, end);
                else if (itemId == R.id.menu_style_blockquote) {
                    if (start == end) {
                        Pair<Integer, Integer> block = StyleHelper.getParagraph(etBody, true);
                        if (block == null)
                            return false;
                        return StyleHelper.setBlockQuote(etBody, block.first, block.second, keep_selection);
                    } else
                        return setBlockQuote(etBody, start, end, keep_selection);
                } else if (itemId == R.id.menu_style_mark)
                    return setMark(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_subscript)
                    return setSubscript(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_superscript)
                    return setSuperscript(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_strikethrough)
                    return setStrikeThrough(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_insert_line)
                    return setLine(etBody, end);
                else if (itemId == R.id.menu_style_spell_check)
                    return spellCheck(owner, etBody, start, end);
                else if (itemId == R.id.menu_style_password)
                    return setPassword(owner, etBody, start, end);
                else if (itemId == R.id.menu_style_code) {
                    Log.breadcrumb("style", "action", "code");
                    setSize(etBody, start, end, HtmlHelper.FONT_SMALL, keep_selection);
                    setFont(etBody, start, end, "monospace", keep_selection);
                    return true;
                } else if (itemId == R.id.menu_style_reverse) {
                    return reverse(etBody, start, end);
                } else if (itemId == R.id.menu_link)
                    return setLink(etBody, start, end, args);
                else if (itemId == R.id.menu_style_clear)
                    return clear(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_clear)
                    return clearAll(etBody, start, end, keep_selection);
                else if (itemId == R.id.menu_style_settings)
                    return updateSettings(owner, etBody, anchor, start, end, keep_selection);
            } else {
                switch (groupId) {
                    case group_style_size: {
                        Float size;
                        if (itemId == 1)
                            size = HtmlHelper.FONT_XSMALL;
                        else if (itemId == 2)
                            size = HtmlHelper.FONT_SMALL;
                        else if (itemId == 4)
                            size = HtmlHelper.FONT_LARGE;
                        else if (itemId == 5)
                            size = HtmlHelper.FONT_XLARGE;
                        else
                            size = null;

                        return setSize(etBody, start, end, size, keep_selection);
                    }

                    case group_style_font_standard:
                    case group_style_font_custom:
                        return setFont(etBody, start, end, (String) args[0], keep_selection);

                    case group_style_align: {
                        if (start == end) {
                            Pair<Integer, Integer> block = StyleHelper.getParagraph(etBody, true);
                            if (block == null)
                                return false;
                            return setAlignment(itemId, etBody, block.first, block.second, keep_selection);
                        } else
                            return setAlignment(itemId, etBody, start, end, keep_selection);
                    }

                    case group_style_list: {
                        boolean level = (itemId == R.id.menu_style_list_decrease || itemId == R.id.menu_style_list_increase);
                        if (start == end) {
                            Pair<Integer, Integer> p = StyleHelper.getParagraph(etBody, false);
                            if (p == null)
                                return false;
                            if (level)
                                return StyleHelper.setListLevel(itemId, etBody, p.first, p.second, keep_selection);
                            else
                                return StyleHelper.setList(itemId, etBody, p.first, p.second, keep_selection);
                        } else {
                            if (level)
                                return setListLevel(itemId, etBody, start, end, keep_selection);
                            else
                                return setList(itemId, etBody, start, end, keep_selection);
                        }
                    }

                    case group_style_indentation: {
                        if (start == end) {
                            Pair<Integer, Integer> block = StyleHelper.getParagraph(etBody, true);
                            if (block == null)
                                return false;
                            return StyleHelper.setIndentation(itemId, etBody, block.first, block.second, keep_selection);
                        } else
                            return setIndentation(itemId, etBody, start, end, keep_selection);
                    }
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return false;
    }

    static boolean setBoldItalic(int itemId, EditText etBody, int start, int end, boolean select) {
        String name = (itemId == R.id.menu_bold ? "bold" : "italic");
        Log.breadcrumb("style", "action", name);

        boolean has = false;
        Editable edit = etBody.getText();
        int style = (itemId == R.id.menu_bold ? Typeface.BOLD : Typeface.ITALIC);
        StyleSpan[] spans = edit.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : spans)
            if (span.getStyle() == style) {
                int s = edit.getSpanStart(span);
                int e = edit.getSpanEnd(span);
                int f = edit.getSpanFlags(span);
                edit.removeSpan(span);
                if (splitSpan(edit, start, end, s, e, f, true,
                        new StyleSpan(style), new StyleSpan(style)))
                    has = true;
            }

        if (!has)
            edit.setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean setUnderline(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "underline");

        boolean has = false;
        Editable edit = etBody.getText();
        UnderlineSpan[] spans = edit.getSpans(start, end, UnderlineSpan.class);
        for (UnderlineSpan span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            if ((f & Spanned.SPAN_COMPOSING) != 0)
                continue;
            edit.removeSpan(span);
            if (splitSpan(edit, start, end, s, e, f, true,
                    new UnderlineSpan(), new UnderlineSpan()))
                has = true;
        }

        if (!has)
            edit.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean selectSize(LifecycleOwner owner, View anchor, EditText etBody) {
        Log.breadcrumb("style", "action", "selectSize");

        Context context = anchor.getContext();
        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);

        int[] titles = new int[]{
                R.string.title_style_size_xsmall,
                R.string.title_style_size_small,
                R.string.title_style_size_medium,
                R.string.title_style_size_large,
                R.string.title_style_size_xlarge};

        float[] sizes = new float[]{
                HtmlHelper.FONT_XSMALL,
                HtmlHelper.FONT_SMALL,
                1.0f,
                HtmlHelper.FONT_LARGE,
                HtmlHelper.FONT_XLARGE};

        for (int i = 0; i < titles.length; i++) {
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(context.getString(titles[i]));
            ssb.setSpan(new RelativeSizeSpan(sizes[i]), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            popupMenu.getMenu().add(group_style_size, i + 1, i, ssb);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return StyleHelper.apply(group_style_size, item.getItemId(), owner, anchor, etBody);
            }
        });

        popupMenu.show();

        return true;
    }

    static boolean setSize(EditText etBody, int start, int end, Float size, boolean select) {
        Log.breadcrumb("style", "action", "size");

        Editable edit = etBody.getText();
        RelativeSizeSpan[] spans = edit.getSpans(start, end, RelativeSizeSpan.class);
        for (RelativeSizeSpan span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            splitSpan(edit, start, end, s, e, f, false,
                    new RelativeSizeSpan(span.getSizeChange()),
                    new RelativeSizeSpan(span.getSizeChange()));
        }

        if (size != null)
            edit.setSpan(new RelativeSizeSpan(size), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean selectBackground(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "selectBackground");

        Helper.hideKeyboard(etBody);

        Context context = etBody.getContext();
        int editTextColor = Helper.resolveColor(context, android.R.attr.editTextColor);

        ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                .with(context)
                .setTitle(R.string.title_background)
                .showColorEdit(true)
                .setColorEditTextColor(editTextColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                //.lightnessSliderOnly()
                .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        setBackground(etBody, start, end, selectedColor, select);
                    }
                })
                .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setBackground(etBody, start, end, null, select);
                    }
                });

        BackgroundColorSpan[] spans = etBody.getText().getSpans(start, end, BackgroundColorSpan.class);
        if (spans != null && spans.length == 1)
            builder.initialColor(spans[0].getBackgroundColor());

        builder.build().show();

        return true;
    }

    static void setBackground(EditText etBody, int start, int end, Integer color, boolean select) {
        Log.breadcrumb("style", "action", "background");

        Editable edit = etBody.getText();
        BackgroundColorSpan spans[] = edit.getSpans(start, end, BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            splitSpan(edit, start, end, s, e, f, false,
                    new BackgroundColorSpan(span.getBackgroundColor()),
                    new BackgroundColorSpan(span.getBackgroundColor()));
        }

        if (color != null)
            edit.setSpan(new BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);
    }

    static boolean selectColor(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "selectColor");

        Helper.hideKeyboard(etBody);

        Context context = etBody.getContext();
        int editTextColor = Helper.resolveColor(context, android.R.attr.editTextColor);

        ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                .with(context)
                .setTitle(R.string.title_color)
                .showColorEdit(true)
                .setColorEditTextColor(editTextColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                //.lightnessSliderOnly()
                .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        setColor(etBody, start, end, selectedColor, select);
                    }
                })
                .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setColor(etBody, start, end, null, select);
                    }
                });

        ForegroundColorSpan[] spans = etBody.getText().getSpans(start, end, ForegroundColorSpan.class);
        if (spans != null && spans.length == 1)
            builder.initialColor(spans[0].getForegroundColor());

        builder.build().show();

        return true;
    }

    static void setColor(EditText etBody, int start, int end, Integer color, boolean select) {
        Log.breadcrumb("style", "action", "color");

        Editable edit = etBody.getText();
        ForegroundColorSpan spans[] = edit.getSpans(start, end, ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            splitSpan(edit, start, end, s, e, f, false,
                    new ForegroundColorSpan(span.getForegroundColor()),
                    new ForegroundColorSpan(span.getForegroundColor()));
        }

        if (color != null)
            edit.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);
    }

    static boolean selectFont(LifecycleOwner owner, View anchor, EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "selectFont");

        Context context = anchor.getContext();
        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);

        List<FontDescriptor> fonts = getFonts(context, false);
        for (int i = 0; i < fonts.size(); i++) {
            FontDescriptor font = fonts.get(i);
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(font.toString());
            ssb.setSpan(getTypefaceSpan(font.type, context), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            popupMenu.getMenu().add(font.custom ? group_style_font_custom : group_style_font_standard, i, 0, ssb)
                    .setIntent(new Intent().putExtra("face", font.type));
        }
        popupMenu.getMenu().add(group_style_font_standard, fonts.size(), 0, R.string.title_style_font_default)
                .setIntent(new Intent());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return setFont(etBody, start, end, item.getIntent().getStringExtra("face"), select);
            }
        });

        popupMenu.show();

        return true;
    }

    static boolean setFont(EditText etBody, int start, int end, String face, boolean select) {
        Log.breadcrumb("style", "action", "font");

        Context context = etBody.getContext();

        Editable edit = etBody.getText();
        TypefaceSpan[] spans = edit.getSpans(start, end, TypefaceSpan.class);
        for (TypefaceSpan span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            splitSpan(edit, start, end, s, e, f, false,
                    getTypefaceSpan(span.getFamily(), context),
                    getTypefaceSpan(span.getFamily(), context));
        }

        if (face != null)
            edit.setSpan(getTypefaceSpan(face, context), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean selectAlignment(LifecycleOwner owner, View anchor, EditText etBody, int start, int end) {
        Log.breadcrumb("style", "action", "selectAlignment");

        Context context = anchor.getContext();
        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);
        popupMenu.inflate(R.menu.popup_style_alignment);

        if (start == end) {
            Pair<Integer, Integer> block = StyleHelper.getParagraph(etBody, true);
            if (block == null)
                return false;
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return StyleHelper.apply(group_style_align, item.getItemId(), owner, anchor, etBody);
            }
        });

        popupMenu.insertIcons(context);

        popupMenu.show();

        return true;
    }

    static boolean setAlignment(int itemId, EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "alignment");

        Editable edit = etBody.getText();
        Pair<Integer, Integer> paragraph = ensureParagraph(edit, start, end);
        if (paragraph == null)
            return false;

        int s = paragraph.first;
        int e = paragraph.second;

        AlignmentSpan[] spans = edit.getSpans(s, e, AlignmentSpan.class);
        for (AlignmentSpan span : spans)
            edit.removeSpan(span);

        Layout.Alignment alignment = null;
        boolean ltr = (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LTR);
        if (itemId == R.id.menu_style_align_start) {
            alignment = (ltr ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE);
        } else if (itemId == R.id.menu_style_align_center) {
            alignment = Layout.Alignment.ALIGN_CENTER;
        } else if (itemId == R.id.menu_style_align_end) {
            alignment = (ltr ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL);
        }

        if (alignment != null)
            edit.setSpan(new AlignmentSpan.Standard(alignment),
                    s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);

        etBody.setText(edit);
        etBody.setSelection(select ? s : e, e);

        return true;
    }

    static boolean selectList(LifecycleOwner owner, View anchor, EditText etBody, int start, int end) {
        Log.breadcrumb("style", "action", "selectList");

        Context context = anchor.getContext();
        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);
        popupMenu.inflate(R.menu.popup_style_list);

        int s = start;
        int e = end;
        if (s == e) {
            Pair<Integer, Integer> p = StyleHelper.getParagraph(etBody, false);
            if (p == null)
                return false;
            s = p.first;
            e = p.second;
        }

        Editable edit = etBody.getText();
        Integer maxLevel = getMaxListLevel(edit, s, e);
        IndentSpan[] indents = edit.getSpans(s, e, IndentSpan.class);

        popupMenu.getMenu().findItem(R.id.menu_style_list_bullets).setEnabled(indents.length == 0);
        popupMenu.getMenu().findItem(R.id.menu_style_list_numbered).setEnabled(indents.length == 0);
        popupMenu.getMenu().findItem(R.id.menu_style_list_increase).setEnabled(indents.length == 0 && maxLevel != null);
        popupMenu.getMenu().findItem(R.id.menu_style_list_decrease).setEnabled(indents.length == 0 && maxLevel != null && maxLevel > 0);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return StyleHelper.apply(group_style_list, item.getItemId(), owner, anchor, etBody);
            }
        });

        popupMenu.insertIcons(context);

        popupMenu.show();

        return true;
    }

    static boolean setListLevel(int itemId, EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "level");

        Context context = etBody.getContext();
        int add = (itemId == R.id.menu_style_list_increase ? 1 : -1);

        boolean renum = false;
        Editable edit = etBody.getText();
        BulletSpan[] spans = edit.getSpans(start, end, BulletSpan.class);
        for (BulletSpan span : spans)
            if (span instanceof BulletSpanEx) {
                BulletSpanEx bs = (BulletSpanEx) span;
                bs.setLevel(Math.max(0, bs.getLevel() + add));
            } else if (span instanceof NumberSpan) {
                renum = true;
                NumberSpan ns = (NumberSpan) span;
                ns.setLevel(Math.max(0, ns.getLevel() + add));
            }

        if (renum)
            renumber(edit, false, context);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean setList(int itemId, EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "list");

        Context context = etBody.getContext();
        Editable edit = etBody.getText();

        int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
        int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.bullet_gap_size);
        int bulletRadius = context.getResources().getDimensionPixelSize(R.dimen.bullet_radius_size);
        int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.bullet_indent_size);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int message_zoom = prefs.getInt("message_zoom", 100);
        float textSize = Helper.getTextSize(context, 0) * message_zoom / 100f;

        Pair<Integer, Integer> paragraph = ensureParagraph(edit, start, end);
        if (paragraph == null)
            return false;

        int s = paragraph.first;
        int e = paragraph.second;

        // Remove existing bullets
        BulletSpan[] spans = edit.getSpans(s, e, BulletSpan.class);
        for (BulletSpan span : spans)
            edit.removeSpan(span);

        int i = s;
        int j = s + 1;
        int index = 1;
        while (j < e) {
            if (i > 0 && edit.charAt(i - 1) == '\n' && edit.charAt(j) == '\n') {
                Log.i("Insert " + i + "..." + (j + 1) + " size=" + e);
                if (itemId == R.id.menu_style_list_bullets)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                        edit.setSpan(new BulletSpanEx(bulletIndent, bulletGap, colorAccent, 0), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);
                    else
                        edit.setSpan(new BulletSpanEx(bulletIndent, bulletGap, colorAccent, bulletRadius, 0), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);
                else
                    edit.setSpan(new NumberSpan(bulletIndent, bulletGap, colorAccent, textSize, 0, index++), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);

                i = j + 1;
            }
            j++;
        }

        renumber(edit, false, context);

        etBody.setText(edit);
        etBody.setSelection(select ? s : e - 1, e - 1);

        return true;
    }

    static boolean selectIndentation(LifecycleOwner owner, View anchor, EditText etBody, int start, int end) {
        Log.breadcrumb("style", "action", "selectIndentation");

        Context context = anchor.getContext();
        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);
        popupMenu.inflate(R.menu.popup_style_indentation);

        int s = start;
        int e = end;
        if (s == e) {
            Pair<Integer, Integer> block = StyleHelper.getParagraph(etBody, true);
            if (block == null)
                return false;
            s = block.first;
            e = block.second;
        }

        Editable edit = etBody.getText();
        Integer maxLevel = getMaxListLevel(edit, s, e);
        IndentSpan[] indents = edit.getSpans(s, e, IndentSpan.class);

        popupMenu.getMenu().findItem(R.id.menu_style_indentation_increase).setEnabled(maxLevel == null);
        popupMenu.getMenu().findItem(R.id.menu_style_indentation_decrease).setEnabled(indents.length > 0);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return StyleHelper.apply(group_style_indentation, item.getItemId(), owner, anchor, etBody);
            }
        });

        popupMenu.insertIcons(context);

        popupMenu.show();

        return true;
    }

    static boolean setIndentation(int itemId, EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "indent");

        Editable edit = etBody.getText();
        Pair<Integer, Integer> paragraph = ensureParagraph(edit, start, end);
        if (paragraph == null)
            return false;

        Context context = etBody.getContext();
        int intentSize = context.getResources().getDimensionPixelSize(R.dimen.indent_size);

        QuoteSpan[] quotes = edit.getSpans(start, end, QuoteSpan.class);
        for (QuoteSpan quote : quotes)
            edit.removeSpan(quote);

        int prev = paragraph.first;
        int next = paragraph.first;
        while (next < paragraph.second) {
            while (next < paragraph.second && edit.charAt(next) != '\n')
                next++;

            if (itemId == R.id.menu_style_indentation_decrease) {
                IndentSpan[] indents = edit.getSpans(prev, prev, IndentSpan.class);
                if (indents.length > 0)
                    edit.removeSpan(indents[0]);
            } else {
                IndentSpan is = new IndentSpan(intentSize);
                edit.setSpan(is, prev, next + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            next++;
            prev = next;
        }

        etBody.setText(edit);
        etBody.setSelection(select ? paragraph.first : paragraph.second, paragraph.second);

        return true;
    }

    static boolean setBlockQuote(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "quote");

        Context context = etBody.getContext();
        Editable edit = etBody.getText();

        int colorPrimary = Helper.resolveColor(context, androidx.appcompat.R.attr.colorPrimary);
        final int colorBlockquote = Helper.resolveColor(context, R.attr.colorBlockquote, colorPrimary);
        int quoteGap = context.getResources().getDimensionPixelSize(R.dimen.quote_gap_size);
        int quoteStripe = context.getResources().getDimensionPixelSize(R.dimen.quote_stripe_width);

        Pair<Integer, Integer> paragraph = ensureParagraph(edit, start, end);
        if (paragraph == null)
            return false;

        QuoteSpan[] quotes = edit.getSpans(paragraph.first, paragraph.second, QuoteSpan.class);
        for (QuoteSpan quote : quotes)
            edit.removeSpan(quote);

        if (quotes.length == 0) {
            IndentSpan[] indents = edit.getSpans(start, end, IndentSpan.class);
            for (IndentSpan indent : indents)
                edit.removeSpan(indent);

            QuoteSpan q;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                q = new QuoteSpan(colorBlockquote);
            else
                q = new QuoteSpan(colorBlockquote, quoteStripe, quoteGap);
            edit.setSpan(q, paragraph.first, paragraph.second, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        etBody.setText(edit);
        etBody.setSelection(select ? paragraph.first : paragraph.second, paragraph.second);

        return true;
    }

    static boolean setMark(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "mark");

        boolean has = false;
        Editable edit = etBody.getText();
        MarkSpan[] spans = edit.getSpans(start, end, MarkSpan.class);
        for (MarkSpan span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            if (splitSpan(edit, start, end, s, e, f, true,
                    new MarkSpan(), new MarkSpan()))
                has = true;
        }

        if (!has)
            edit.setSpan(new MarkSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean setSubscript(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "subscript");

        boolean has = false;
        Editable edit = etBody.getText();
        SubscriptSpanEx[] spans = edit.getSpans(start, end, SubscriptSpanEx.class);
        for (SubscriptSpanEx span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            if (splitSpan(edit, start, end, s, e, f, true,
                    new SubscriptSpanEx(), new SubscriptSpanEx()))
                has = true;
        }

        if (!has)
            edit.setSpan(new SubscriptSpanEx(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean setSuperscript(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "superscript");

        boolean has = false;
        Editable edit = etBody.getText();
        SuperscriptSpanEx[] spans = edit.getSpans(start, end, SuperscriptSpanEx.class);
        for (SuperscriptSpanEx span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            if (splitSpan(edit, start, end, s, e, f, true,
                    new SuperscriptSpanEx(), new SuperscriptSpanEx()))
                has = true;
        }

        if (!has)
            edit.setSpan(new SuperscriptSpanEx(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean setStrikeThrough(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "strike");

        boolean has = false;
        Editable edit = etBody.getText();
        StrikethroughSpan[] spans = edit.getSpans(start, end, StrikethroughSpan.class);
        for (StrikethroughSpan span : spans) {
            int s = edit.getSpanStart(span);
            int e = edit.getSpanEnd(span);
            int f = edit.getSpanFlags(span);
            edit.removeSpan(span);
            if (splitSpan(edit, start, end, s, e, f, true,
                    new StrikethroughSpan(), new StrikethroughSpan()))
                has = true;
        }

        if (!has)
            edit.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean setLine(EditText etBody, int end) {
        Log.breadcrumb("style", "action", "line");

        Context context = etBody.getContext();
        Editable edit = etBody.getText();

        if (end == 0 || edit.charAt(end - 1) != '\n')
            edit.insert(end++, "\n");
        if (end == edit.length() || edit.charAt(end) != '\n')
            edit.insert(end, "\n");

        edit.insert(end, "\uFFFC"); // Object replacement character

        int colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
        float stroke = context.getResources().getDisplayMetrics().density;
        edit.setSpan(
                new LineSpan(colorSeparator, stroke, 0f),
                end, end + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        etBody.setSelection(end + 2);

        return true;
    }

    static boolean spellCheck(LifecycleOwner owner, EditText etBody, int start, int end) {
        Log.breadcrumb("style", "action", "spell");

        etBody.setSelection(end);

        final Context context = etBody.getContext();

        Bundle args = new Bundle();
        args.putCharSequence("text", etBody.getText().subSequence(start, end));

        new SimpleTask<List<LanguageTool.Suggestion>>() {
            private HighlightSpan highlightSpan = null;

            @Override
            protected void onPreExecute(Bundle args) {
                int textColorHighlight = Helper.resolveColor(context, android.R.attr.textColorHighlight);
                highlightSpan = new HighlightSpan(textColorHighlight);
                etBody.getText().setSpan(highlightSpan, start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_COMPOSING);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (highlightSpan != null)
                    etBody.getText().removeSpan(highlightSpan);
            }

            @Override
            protected List<LanguageTool.Suggestion> onExecute(Context context, Bundle args) throws Throwable {
                CharSequence text = args.getCharSequence("text");
                return LanguageTool.getSuggestions(context, text);
            }

            @Override
            protected void onExecuted(Bundle args, List<LanguageTool.Suggestion> suggestions) {
                LanguageTool.applySuggestions(etBody, start, end, suggestions);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                ToastEx.makeText(context, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
            }
        }.execute(context, owner, args, "spell");

        return true;
    }

    static boolean setPassword(LifecycleOwner owner, EditText etBody, int start, int end) {
        Log.breadcrumb("style", "action", "password");

        Context context = etBody.getContext();
        if (!ActivityBilling.isPro(context)) {
            context.startActivity(new Intent(context, ActivityBilling.class));
            return true;
        }

        boolean toolong = false;
        if (end - start > ProtectedContent.MAX_PROTECTED_TEXT) {
            toolong = true;
        } else {
            Editable edit = etBody.getText();
            Spanned text = (Spanned) edit.subSequence(start, end);
            String html = ProtectedContent.getContent(context, text);
            if (html.length() > ProtectedContent.MAX_PROTECTED_TEXT)
                toolong = true;
        }
        if (toolong) {
            ToastEx.makeText(context, R.string.title_style_protect_size, Toast.LENGTH_LONG).show();
            return true;
        }

        ProtectedContent.showDialogEncrypt(context, owner, etBody);

        return true;
    }

    static boolean reverse(EditText etBody, int start, int end) {
        Log.breadcrumb("style", "action", "reverse");

        Editable edit = etBody.getText();
        List<String> lines = new ArrayList<>(Arrays.asList(edit.subSequence(start, end).toString().split("\n")));
        Collections.reverse(lines);
        edit.replace(start, end, TextUtils.join("\n", lines));
        etBody.setSelection(start, end);

        return true;
    }

    static boolean setLink(EditText etBody, int start, int end, Object... args) {
        Log.breadcrumb("style", "action", "link");

        long working = (Long) args[0];
        int zoom = (Integer) args[1];
        String url = (String) args[2];
        boolean image = (Boolean) args[3];
        String title = (String) args[4];

        Editable edit = etBody.getText();
        if (image) {
            Uri uri = Uri.parse(url);
            if (!UriHelper.isHyperLink(uri))
                return false;

            SpannableStringBuilder ssb = new SpannableStringBuilderEx(edit);

            ssb.insert(start, "\n\uFFFC\n"); // Object replacement character

            Drawable img = ImageHelper.decodeImage(etBody.getContext(),
                    working, url, true, zoom, 1.0f, etBody);

            ImageSpan is = new ImageSpan(img, url);
            ssb.setSpan(is, start + 1, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            etBody.setText(ssb);
            etBody.setSelection(start + 3);
        } else {
            URLSpan[] spans = edit.getSpans(start, end, URLSpan.class);
            for (URLSpan span : spans)
                edit.removeSpan(span);

            if (!TextUtils.isEmpty(url)) {
                if (TextUtils.isEmpty(title))
                    title = url;

                if (start == end)
                    edit.insert(start, title);
                else if (!title.equals(edit.subSequence(start, end).toString()))
                    edit.replace(start, end, title);

                edit.setSpan(new URLSpan(url), start, start + title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            etBody.setText(edit);
            etBody.setSelection(start + title.length());
        }

        return true;

    }

    static boolean clear(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "clear");

        int e = end;

        // Expand to paragraph (block quotes)
        Editable edit = etBody.getText();
        if (e + 1 < edit.length() && edit.charAt(e) == '\n')
            e++;

        for (Object span : edit.getSpans(start, e, Object.class)) {
            boolean has = false;
            for (Class<?> cls : CLEAR_STYLES)
                if (cls.isAssignableFrom(span.getClass())) {
                    has = true;
                    break;
                }
            if (!has)
                continue;

            int sstart = edit.getSpanStart(span);
            int send = edit.getSpanEnd(span);
            int flags = edit.getSpanFlags(span);

            if (sstart < start && send > start)
                setSpan(edit, span, sstart, start, flags, etBody.getContext());
            if (sstart < end && send > end)
                setSpan(edit, span, e, send, flags, etBody.getContext());

            edit.removeSpan(span);
        }

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean clearAll(EditText etBody, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "clear/all");

        Editable edit = etBody.getText();
        for (Object span : edit.getSpans(0, etBody.length(), Object.class)) {
            if (!CLEAR_STYLES.contains(span.getClass()))
                continue;
            edit.removeSpan(span);
        }

        etBody.setText(edit);
        etBody.setSelection(select ? start : end, end);

        return true;
    }

    static boolean updateSettings(LifecycleOwner owner, EditText etBody, View anchor, int start, int end, boolean select) {
        Log.breadcrumb("style", "action", "updateSettings");

        Context context = anchor.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);
        popupMenu.inflate(R.menu.popup_style_settings);

        popupMenu.getMenu().findItem(R.id.menu_style_setting_keep_selection).setChecked(select);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_style_setting_keep_selection) {
                    boolean keep = !item.isChecked();
                    prefs.edit().putBoolean("style_setting_keep_selection", keep).apply();
                    etBody.setSelection(keep ? start : end, end);
                    return true;
                } else
                    return false;
            }
        });

        popupMenu.show();

        return true;
    }

    static boolean splitSpan(Editable edit, int start, int end, int s, int e, int f, boolean extend, Object span1, Object span2) {
        if (start < 0 || end < 0) {
            Log.e(span1 + " invalid selection=" + start + "..." + end);
            return false;
        }

        if (s < 0 || e < 0) {
            Log.e(span1 + " not attached=" + s + "..." + e);
            return false;
        }

        if (s > e) {
            int tmp = s;
            s = e;
            e = tmp;
        }

        if (start < s && end > s && end < e) {
            // overlap before
            if (extend)
                edit.setSpan(span1, start, e, f);
            else
                edit.setSpan(span1, end, e, f);
            return true;
        } else if (start < e && end > e && start > s) {
            // overlap after
            if (extend)
                edit.setSpan(span1, s, end, f);
            else
                edit.setSpan(span1, s, start, f);
            return true;
        } else if (start < s && end > e) {
            // overlap all
            if (extend) {
                edit.setSpan(span1, start, end, f);
                return true;
            }
        } else if (start >= s && end <= e) {
            if (start == s && end == e)
                return true;

            // overlap inner
            if (s < start)
                edit.setSpan(span1, s, start, f);
            if (end < e)
                edit.setSpan(span2, end, e, f);
            if (s < start || end < e)
                return true;
        }

        return false;
    }

    static void setSpan(Editable edit, Object span, int start, int end, int flags, Context context) {
        if (span instanceof CharacterStyle)
            edit.setSpan(CharacterStyle.wrap((CharacterStyle) span), start, end, flags);
        else if (span instanceof QuoteSpan) {
            ParagraphStyle ps = (ParagraphStyle) span;
            Pair<Integer, Integer> p = ensureParagraph(edit, start, end);
            if (p == null)
                return;
            edit.setSpan(clone(span, ps.getClass(), context), p.first, p.second, flags);
        }
    }

    static private Pair<Integer, Integer> ensureParagraph(Editable edit, int s, int e) {
        int start = s;
        int end = e;

        // Expand selection at start
        while (start > 0 && edit.charAt(start - 1) != '\n')
            start--;

        // Expand selection at end
        while (end > 0 && end < edit.length() && edit.charAt(end - 1) != '\n')
            end++;

        // Nothing to do
        if (start == end)
            return null;

        // Create paragraph at start
        if (start == 0 && edit.charAt(start) != '\n') {
            edit.insert(0, "\n");
            start++;
            end++;
        }

        // Create paragraph at end
        if (end == edit.length() && edit.charAt(end - 1) != '\n') {
            edit.append("\n");
            end++;
        }

        if (end == edit.length())
            edit.append("\n"); // workaround Android bug

        return new Pair<>(start, end);
    }

    static private Pair<Integer, Integer> getWord(TextView tvBody) {
        int start = tvBody.getSelectionStart();
        int end = tvBody.getSelectionEnd();
        Spannable edit = (Spannable) tvBody.getText();

        if (start < 0 || end < 0)
            return null;

        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        // Expand selection at start
        while (start > 0 && edit.charAt(start - 1) != ' ' && edit.charAt(start - 1) != '\n')
            start--;

        // Expand selection at end
        while (end < edit.length() && edit.charAt(end) != ' ' && edit.charAt(end) != '\n')
            end++;

        if (start == end)
            return null;

        return new Pair<>(start, end);
    }

    public static Pair<Integer, Integer> getParagraph(TextView tvBody) {
        return getParagraph(tvBody, false);
    }

    public static Pair<Integer, Integer> getParagraph(TextView tvBody, boolean block) {
        int start = tvBody.getSelectionStart();
        int end = tvBody.getSelectionEnd();
        Spannable edit = (Spannable) tvBody.getText();

        if (start < 0 || end < 0)
            return null;

        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        // Expand selection at start
        while (start > 0 &&
                (edit.charAt(start - 1) != '\n' ||
                        (block && start - 1 > 0 && edit.charAt(start - 2) != '\n')))
            start--;

        if (start == end && end < edit.length())
            end++;

        // Expand selection at end
        while (end > 0 && end < edit.length() &&
                (edit.charAt(end - 1) != '\n' ||
                        (block && end - 1 > 0 && edit.charAt(end - 2) != '\n')))
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

    static <T extends ParagraphStyle> T clone(Object span, Class<T> type, Context context) {
        if (QuoteSpan.class.isAssignableFrom(type)) {
            QuoteSpan q = (QuoteSpan) span;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                return (T) new QuoteSpan(q.getColor());
            else
                return (T) new QuoteSpan(q.getColor(), q.getStripeWidth(), q.getGapWidth());
        } else if (NumberSpan.class.isAssignableFrom(type)) {
            NumberSpan n = (NumberSpan) span;
            int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.bullet_gap_size);
            int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.bullet_indent_size);
            int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
            return (T) new NumberSpan(bulletIndent, bulletGap, colorAccent, n.getTextSize(), n.getLevel(), n.getIndex() + 1);
        } else if (BulletSpanEx.class.isAssignableFrom(type)) {
            BulletSpanEx b = (BulletSpanEx) span;
            int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.bullet_indent_size);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
                int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.bullet_gap_size);
                return (T) new BulletSpanEx(bulletIndent, bulletGap, colorAccent, b.getLevel());
            } else
                return (T) new BulletSpanEx(bulletIndent, b.getGapWidth(), b.getColor(), b.getBulletRadius(), b.getLevel());

        } else
            throw new IllegalArgumentException(type.getName());
    }

    static Integer getMaxListLevel(Editable edit, int start, int end) {
        Integer maxLevel = null;
        BulletSpan[] bullets = edit.getSpans(start, end, BulletSpan.class);
        for (BulletSpan span : bullets) {
            Integer level = null;
            if (span instanceof NumberSpan)
                level = ((NumberSpan) span).getLevel();
            else if (span instanceof BulletSpanEx)
                level = ((BulletSpanEx) span).getLevel();
            if (level != null && (maxLevel == null || level > maxLevel))
                maxLevel = level;
        }
        return maxLevel;
    }

    static void renumber(Editable text, boolean clean, Context context) {
        int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.bullet_gap_size);
        int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.bullet_indent_size);
        int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);

        Log.i("Renumber clean=" + clean + " text=" + text);

        int next;
        int pos = -1;
        List<Integer> levels = new ArrayList<>();
        for (int i = 0; i < text.length(); i = next) {
            next = text.nextSpanTransition(i, text.length(), NumberSpan.class);
            Log.i("Bullet span next=" + next);

            BulletSpan[] spans = text.getSpans(i, next, BulletSpan.class);
            for (BulletSpan span : spans) {
                int start = text.getSpanStart(span);
                int end = text.getSpanEnd(span);
                int flags = text.getSpanFlags(span);
                Log.i("Bullet span " + start + "..." + end);

                if (clean && start == end) {
                    text.removeSpan(span);
                    continue;
                }

                int level;
                if (span instanceof NumberSpan)
                    level = ((NumberSpan) span).getLevel();
                else if (span instanceof BulletSpanEx)
                    level = ((BulletSpanEx) span).getLevel();
                else
                    level = 0;

                if (start != pos)
                    levels.clear();
                while (levels.size() > level + 1)
                    levels.remove(levels.size() - 1);
                if (levels.size() == level + 1 && !(span instanceof NumberSpan))
                    levels.remove(level);
                while (levels.size() < level + 1)
                    levels.add(0);

                int index = levels.get(level) + 1;
                levels.remove(level);
                levels.add(level, index);

                if (span instanceof NumberSpan) {
                    NumberSpan ns = (NumberSpan) span;
                    if (index != ns.getIndex()) {
                        text.removeSpan(span);
                        // Text size needs measuring
                        NumberSpan clone = new NumberSpan(bulletIndent, bulletGap, colorAccent, ns.getTextSize(), level, index);
                        text.setSpan(clone, start, end, flags);
                    }

                    pos = end;
                }
            }
        }
    }

    static void markAsInserted(Editable text, int start, int end) {
        if (!BuildConfig.DEBUG)
            return;
        for (InsertedSpan span : text.getSpans(0, text.length(), InsertedSpan.class))
            text.removeSpan(span);
        if (start >= 0 && start < end && end <= text.length()) {
            if (start == 0) {
                /*
                    java.lang.IndexOutOfBoundsException: Invalid Context Range: 0, 1 must be in 0, 0
                        at android.graphics.Paint.getRunCharacterAdvance(Paint.java:3541)
                        at android.text.TextLine.getRunAdvance(TextLine.java:1274)
                        at android.text.TextLine.handleText(TextLine.java:1361)
                        at android.text.TextLine.handleRun(TextLine.java:1640)
                        at android.text.TextLine.measureRun(TextLine.java:882)
                        at android.text.TextLine.measure(TextLine.java:604)
                        at android.text.TextLine.metrics(TextLine.java:494)
                        at android.text.Layout.getLineExtent(Layout.java:1896)
                        at android.text.Layout.getLineMax(Layout.java:1843)
                        at android.text.Layout.getLineRight(Layout.java:1833)
                        at android.widget.TextView.getCursorAnchorInfo(TextView.java:14517)
                        at android.widget.Editor$CursorAnchorInfoNotifier.updatePosition(Editor.java:4883)
                        at android.widget.Editor$PositionListener.onPreDraw(Editor.java:3750)
                        at android.view.ViewTreeObserver.dispatchOnPreDraw(ViewTreeObserver.java:1176)
                        at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:4158)
                        at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:2836)
                        at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:10145)
                        at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1406)
                        at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1415)
                        at android.view.Choreographer.doCallbacks(Choreographer.java:1015)
                        at android.view.Choreographer.doFrame(Choreographer.java:945)
                        at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1389)
                        at android.os.Handler.handleCallback(Handler.java:959)
                */
                return;
            }
            text.setSpan(new InsertedSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    static class InsertedSpan implements NoCopySpan {
    }

    static class MarkSpan extends BackgroundColorSpan {
        public MarkSpan() {
            super(Color.YELLOW);
        }

        @Override
        public void updateDrawState(@NonNull TextPaint textPaint) {
            super.updateDrawState(textPaint);
            textPaint.setColor(Color.BLACK);
        }
    }

    static String getFamily(String family) {
        // https://web.mit.edu/jmorzins/www/fonts.html
        // https://en.wikipedia.org/wiki/Croscore_fonts
        // https://developer.mozilla.org/en-US/docs/Web/CSS/font-family
        // TODO: Microsoft: Georgia (Serif), Tahoma (Sans-serif), Trebuchet MS (Sans-serif)
        String faces = family.toLowerCase(Locale.ROOT);
        if (faces.contains("montserrat"))
            return "Montserrat, Gotham, \"Proxima Nova\", sans-serif";
        if (faces.contains("arimo"))
            return "Arimo, Arial, Verdana, Helvetica, \"Helvetica Neue\", sans-serif";
        if (faces.contains("tinos"))
            return "Tinos, \"Times New Roman\", Times, serif";
        if (faces.contains("cousine"))
            return "Cousine, \"Courier New\", Courier, monospace";
        if (faces.contains("lato"))
            return "Lato, Carlito, Calibri, Aptos, sans-serif";
        if (faces.contains("caladea"))
            return "Caladea, Cambo, Cambria, serif";
        if (faces.contains("comic sans"))
            return "OpenDyslexic, \"Comic Sans\", \"Comic Sans MS\", sans-serif";
        if (faces.contains("sans narrow"))
            return "\"Liberation Sans Narrow\", \"Arial Narrow\"";
        return family;
    }

    static TypefaceSpan getTypefaceSpan(String family, Context context) {
        family = getFamily(family);
        return new CustomTypefaceSpan(family, getTypeface(family, context));
    }

    static Typeface getTypeface(String family, Context context) {
        if (TextUtils.isEmpty(family))
            return Typeface.DEFAULT;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean bundled_fonts = prefs.getBoolean("bundled_fonts", true);
        boolean narrow_fonts = prefs.getBoolean("narrow_fonts", false);

        List<String> faces = new ArrayList<>();
        for (String face : family.split(","))
            faces.add(face
                    .trim()
                    .toLowerCase(Locale.ROOT)
                    .replace("'", "")
                    .replace("\"", ""));

        try {

            if (faces.contains("fairemail"))
                return ResourcesCompat.getFont(context.getApplicationContext(), R.font.fantasy);

            if (bundled_fonts) {
                if (BuildConfig.DEBUG)
                    if (faces.contains("montserrat") ||
                            faces.contains("gotham") ||
                            faces.contains("proxima nova"))
                        return ResourcesCompat.getFont(context.getApplicationContext(), R.font.montserrat);

                if (faces.contains("arimo") ||
                        faces.contains("arial") ||
                        faces.contains("verdana") ||
                        faces.contains("helvetica") ||
                        faces.contains("helvetica neue"))
                    return ResourcesCompat.getFont(context.getApplicationContext(), R.font.arimo);

                if (faces.contains("tinos") ||
                        faces.contains("times") ||
                        faces.contains("times new roman"))
                    return ResourcesCompat.getFont(context.getApplicationContext(), R.font.tinos);

                if (faces.contains("cousine") ||
                        faces.contains("courier") ||
                        faces.contains("courier new"))
                    return ResourcesCompat.getFont(context.getApplicationContext(), R.font.cousine);

                if (faces.contains("lato") ||
                        faces.contains("carlito") ||
                        faces.contains("calibri") ||
                        faces.contains("aptos"))
                    return ResourcesCompat.getFont(context.getApplicationContext(), R.font.lato);

                if (faces.contains("caladea") ||
                        faces.contains("cambo") ||
                        faces.contains("cambria"))
                    return ResourcesCompat.getFont(context.getApplicationContext(), R.font.caladea);

                if (faces.contains("opendyslexic") ||
                        faces.contains("comic sans") ||
                        faces.contains("comic sans ms"))
                    return ResourcesCompat.getFont(context.getApplicationContext(), R.font.opendyslexic);

                if (narrow_fonts &&
                        (faces.contains("sans narrow") ||
                                faces.contains("arial narrow"))) // condensed, compressed
                    return ResourcesCompat.getFont(context.getApplicationContext(), R.font.liberation_sans_narrow_regular);
            }

            for (String face : faces) {
                Typeface tf = Typeface.create(face, Typeface.NORMAL);
                if (!tf.equals(Typeface.DEFAULT))
                    return tf;
            }
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.Object java.lang.reflect.Constructor.newInstance(java.lang.Object[])' on a null object reference
                    at androidx.core.graphics.TypefaceCompatApi21Impl.newFamily(SourceFile:9)
                    at androidx.core.graphics.TypefaceCompatApi21Impl.createFromFontFamilyFilesResourceEntry(SourceFile:1)
                    at androidx.core.graphics.TypefaceCompat.createFromResourcesFamilyXml(SourceFile:86)
                    at androidx.core.content.res.ResourcesCompat.loadFont(SourceFile:17)
                    at androidx.core.content.res.ResourcesCompat.loadFont(SourceFile:3)
                    at androidx.core.content.res.ResourcesCompat.getFont(SourceFile:2)
                    at eu.faircode.email.StyleHelper.getTypeface(SourceFile:316)
                    at eu.faircode.email.StyleHelper.getTypefaceSpan(SourceFile:7)
             */
        }

        return Typeface.DEFAULT;
    }

    public static List<FontDescriptor> getFonts(Context context) {
        return getFonts(context, true);
    }

    public static List<FontDescriptor> getFonts(Context context, boolean all) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean bundled_fonts = prefs.getBoolean("bundled_fonts", true);
        boolean narrow_fonts = prefs.getBoolean("narrow_fonts", false);

        List<FontDescriptor> result = new ArrayList<>();
        String[] fontNameNames = context.getResources().getStringArray(R.array.fontNameNames);
        String[] fontNameValues = context.getResources().getStringArray(R.array.fontNameValues);
        for (int i = 0; i < fontNameNames.length; i++)
            result.add(new FontDescriptor(fontNameValues[i], fontNameNames[i]));

        // https://en.wikipedia.org/wiki/Croscore_fonts
        if (all || bundled_fonts) {
            result.add(new FontDescriptor("arimo", "Arimo (Arial, Verdana)", true));
            result.add(new FontDescriptor("tinos", "Tinos (Times New Roman)", true));
            result.add(new FontDescriptor("cousine", "Cousine (Courier New)", true));
            result.add(new FontDescriptor("lato", "Lato (Calibri)", true));
            result.add(new FontDescriptor("caladea", "Caladea (Cambria)", true));

            if (all || narrow_fonts)
                result.add(new FontDescriptor("sans narrow", "Liberation Sans Narrow (Arial Narrow)", true));

            if (BuildConfig.DEBUG)
                result.add(new FontDescriptor("montserrat", "Montserrat", true));

            result.add(new FontDescriptor("comic sans", "OpenDyslexic", true));
        }

        return result;
    }

    public static class FontDescriptor {
        @NonNull
        public String type;
        @NonNull
        public String name;
        public boolean custom;

        FontDescriptor(String type, String name) {
            this(type, name, false);
        }

        FontDescriptor(String type, String name, boolean custom) {
            this.type = type;
            this.name = name;
            this.custom = custom;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    //TextUtils.dumpSpans(text, new LogPrinter(android.util.Log.INFO, "FairEmail"), "afterTextChanged ");
}
