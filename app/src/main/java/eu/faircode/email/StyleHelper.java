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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
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
import android.util.Pair;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StyleHelper {
    static boolean apply(int action, LifecycleOwner owner, View anchor, EditText etBody, Object... args) {
        Log.i("Style action=" + action);

        try {
            int _start = etBody.getSelectionStart();
            int _end = etBody.getSelectionEnd();

            if (_start < 0)
                _start = 0;
            if (_end < 0)
                _end = 0;

            if (_start > _end) {
                int tmp = _start;
                _start = _end;
                _end = tmp;
            }

            final Editable edit = etBody.getText();
            final int start = _start;
            final int end = _end;

            if (action == R.id.menu_bold || action == R.id.menu_italic) {
                String name = (action == R.id.menu_bold ? "bold" : "italic");
                Log.breadcrumb("style", "action", name);

                boolean has = false;
                int style = (action == R.id.menu_bold ? Typeface.BOLD : Typeface.ITALIC);
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
                etBody.setSelection(start, end);

                return true;
            } else if (action == R.id.menu_underline) {
                Log.breadcrumb("style", "action", "underline");

                boolean has = false;
                UnderlineSpan[] spans = edit.getSpans(start, end, UnderlineSpan.class);
                for (UnderlineSpan span : spans) {
                    int s = edit.getSpanStart(span);
                    int e = edit.getSpanEnd(span);
                    int f = edit.getSpanFlags(span);
                    edit.removeSpan(span);
                    if (splitSpan(edit, start, end, s, e, f, true,
                            new UnderlineSpan(), new UnderlineSpan()))
                        has = true;
                }

                if (!has)
                    edit.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                etBody.setText(edit);
                etBody.setSelection(start, end);

                return true;
            } else if (action == R.id.menu_style) {
                final Context context = anchor.getContext();
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);
                popupMenu.inflate(R.menu.popup_style);

                {
                    SubMenu smenu = popupMenu.getMenu().findItem(R.id.menu_style_size).getSubMenu();
                    smenu.clear();
                    int[] ids = new int[]{R.id.menu_style_size_small, R.id.menu_style_size_medium, R.id.menu_style_size_large};
                    int[] titles = new int[]{R.string.title_style_size_small, R.string.title_style_size_medium, R.string.title_style_size_large};
                    float[] sizes = new float[]{HtmlHelper.FONT_SMALL, 1.0f, HtmlHelper.FONT_LARGE};
                    for (int i = 0; i < ids.length; i++) {
                        SpannableStringBuilder ssb = new SpannableStringBuilder(context.getString(titles[i]));
                        ssb.setSpan(new RelativeSizeSpan(sizes[i]), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        smenu.add(R.id.group_style_size, ids[i], i, ssb);
                    }
                }

                String[] fontNameNames = anchor.getResources().getStringArray(R.array.fontNameNames);
                String[] fontNameValues = anchor.getResources().getStringArray(R.array.fontNameValues);
                SubMenu smenu = popupMenu.getMenu().findItem(R.id.menu_style_font).getSubMenu();
                for (int i = 0; i < fontNameNames.length; i++) {
                    SpannableStringBuilder ssb = new SpannableStringBuilder(fontNameNames[i]);
                    ssb.setSpan(getTypefaceSpan(fontNameValues[i], context), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    smenu.add(R.id.group_style_font, i, 0, ssb);
                }
                smenu.add(R.id.group_style_font, fontNameNames.length, 0, R.string.title_style_font_default);

                int level = -1;
                BulletSpan[] spans = edit.getSpans(start, end, BulletSpan.class);
                for (BulletSpan span : spans)
                    if (span instanceof NumberSpan)
                        level = ((NumberSpan) span).getLevel();
                    else if (span instanceof BulletSpanEx)
                        level = ((BulletSpanEx) span).getLevel();

                popupMenu.getMenu().findItem(R.id.menu_style_list_increase).setVisible(level >= 0);
                popupMenu.getMenu().findItem(R.id.menu_style_list_decrease).setVisible(level > 0);

                boolean decrease = false;
                IndentSpan[] indents = edit.getSpans(start, end, IndentSpan.class);
                for (IndentSpan indent : indents) {
                    int s = edit.getSpanStart(indent);
                    int e = edit.getSpanEnd(indent);
                    if (s >= start && e <= end) {
                        decrease = true;
                        break;
                    }
                }

                popupMenu.getMenu().findItem(R.id.menu_style_indentation_decrease).setEnabled(decrease);

                popupMenu.insertIcons(context);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            int groupId = item.getGroupId();
                            int itemId = item.getItemId();
                            if (groupId == R.id.group_style_size) {
                                return setSize(item);
                            } else if (itemId == R.id.menu_style_background) {
                                return setBackground(item);
                            } else if (itemId == R.id.menu_style_color) {
                                return setColor(item);
                            } else if (groupId == R.id.group_style_font) {
                                return setFont(item);
                            } else if (groupId == R.id.group_style_align) {
                                return setAlignment(item);
                            } else if (groupId == R.id.group_style_list) {
                                if (item.getItemId() == R.id.menu_style_list_increase ||
                                        item.getItemId() == R.id.menu_style_list_decrease)
                                    return setListLevel(item);
                                else
                                    return setList(item);
                            } else if (groupId == R.id.group_style_blockquote) {
                                return setBlockQuote(item);
                            } else if (groupId == R.id.group_style_indentation) {
                                return setIndentation(item);
                            } else if (groupId == R.id.group_style_strikethrough) {
                                return setStrikeThrough(item);
                            } else if (groupId == R.id.group_style_clear) {
                                return clear(item);
                            }
                            return false;
                        } catch (Throwable ex) {
                            Log.e(ex);
                            return false;
                        }
                    }

                    private boolean setSize(MenuItem item) {
                        Log.breadcrumb("style", "action", "size");

                        Float size;
                        if (item.getItemId() == R.id.menu_style_size_small)
                            size = 0.8f;
                        else if (item.getItemId() == R.id.menu_style_size_large)
                            size = 1.25f;
                        else
                            size = null;

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
                        etBody.setSelection(start, end);

                        return true;
                    }

                    private boolean setBackground(MenuItem item) {
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
                                        _setBackground(selectedColor);
                                    }
                                })
                                .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        _setBackground(null);
                                    }
                                });

                        builder.build().show();

                        return true;
                    }

                    private void _setBackground(Integer color) {
                        Log.breadcrumb("style", "action", "background");

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
                        etBody.setSelection(start, end);
                    }

                    private boolean setColor(MenuItem item) {
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
                                        _setColor(selectedColor);
                                    }
                                })
                                .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        _setColor(null);
                                    }
                                });

                        builder.build().show();

                        return true;
                    }

                    private void _setColor(Integer color) {
                        Log.breadcrumb("style", "action", "color");

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
                        etBody.setSelection(start, end);
                    }

                    private boolean setAlignment(MenuItem item) {
                        Log.breadcrumb("style", "action", "alignment");

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
                        int itemId = item.getItemId();
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
                        etBody.setSelection(s, e);

                        return true;
                    }

                    private boolean setListLevel(MenuItem item) {
                        Log.breadcrumb("style", "action", "level");

                        Context context = etBody.getContext();
                        int add = (item.getItemId() == R.id.menu_style_list_increase ? 1 : -1);

                        boolean renum = false;
                        BulletSpan[] spans = edit.getSpans(start, end, BulletSpan.class);
                        for (BulletSpan span : spans)
                            if (span instanceof BulletSpanEx) {
                                BulletSpanEx bs = (BulletSpanEx) span;
                                bs.setLevel(bs.getLevel() + add);
                            } else if (span instanceof NumberSpan) {
                                renum = true;
                                NumberSpan ns = (NumberSpan) span;
                                ns.setLevel(ns.getLevel() + add);
                            }

                        if (renum)
                            renumber(edit, false, context);

                        etBody.setText(edit);
                        etBody.setSelection(start, end);

                        return true;
                    }

                    private boolean setList(MenuItem item) {
                        Log.breadcrumb("style", "action", "list");

                        Context context = etBody.getContext();

                        int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
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
                                if (item.getItemId() == R.id.menu_style_list_bullets)
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
                        etBody.setSelection(s, e);

                        return true;
                    }

                    private boolean setFont(MenuItem item) {
                        Log.breadcrumb("style", "action", "font");

                        int id = item.getItemId();
                        String[] names = anchor.getResources().getStringArray(R.array.fontNameValues);
                        String face = (id < names.length ? names[id] : null);

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
                        etBody.setSelection(start, end);

                        return true;
                    }

                    private boolean setBlockQuote(MenuItem item) {
                        Log.breadcrumb("style", "action", "quote");

                        Context context = etBody.getContext();

                        int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
                        final int colorBlockquote = Helper.resolveColor(context, R.attr.colorBlockquote, colorPrimary);
                        int quoteGap = context.getResources().getDimensionPixelSize(R.dimen.quote_gap_size);
                        int quoteStripe = context.getResources().getDimensionPixelSize(R.dimen.quote_stripe_width);

                        Pair<Integer, Integer> paragraph = ensureParagraph(edit, start, end);
                        if (paragraph == null)
                            return false;

                        QuoteSpan[] quotes = edit.getSpans(paragraph.first, paragraph.second, QuoteSpan.class);
                        for (QuoteSpan quote : quotes)
                            edit.removeSpan(quote);

                        if (quotes.length == 1)
                            return true;

                        IndentSpan[] indents = edit.getSpans(start, end, IndentSpan.class);
                        for (IndentSpan indent : indents)
                            edit.removeSpan(indent);

                        QuoteSpan q;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                            q = new QuoteSpan(colorBlockquote);
                        else
                            q = new QuoteSpan(colorBlockquote, quoteStripe, quoteGap);
                        edit.setSpan(q, paragraph.first, paragraph.second, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                        etBody.setText(edit);
                        etBody.setSelection(paragraph.first, paragraph.second);

                        return true;
                    }

                    private boolean setIndentation(MenuItem item) {
                        Log.breadcrumb("style", "action", "indent");

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

                            if (item.getItemId() == R.id.menu_style_indentation_decrease) {
                                IndentSpan[] indents = edit.getSpans(prev, prev, IndentSpan.class);
                                if (indents.length > 0)
                                    edit.removeSpan(indents[0]);
                            } else {
                                IndentSpan is = new IndentSpan(intentSize);
                                edit.setSpan(is, prev, next + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            }

                            next++;
                            prev = next;
                        }

                        etBody.setText(edit);
                        etBody.setSelection(paragraph.first, paragraph.second);

                        return true;
                    }

                    private boolean setStrikeThrough(MenuItem item) {
                        Log.breadcrumb("style", "action", "strike");

                        boolean has = false;
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
                        etBody.setSelection(start, end);

                        return true;
                    }

                    private boolean clear(MenuItem item) {
                        Log.breadcrumb("style", "action", "clear");

                        int e = end;

                        // Expand to paragraph (block quotes)
                        if (e + 1 < edit.length() && edit.charAt(e) == '\n')
                            e++;

                        for (Object span : edit.getSpans(start, e, Object.class))
                            if (!(span instanceof ImageSpan)) {
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
                        etBody.setSelection(start, e);

                        return true;
                    }
                });

                popupMenu.show();

                return true;
            } else if (action == R.id.menu_link) {
                Log.breadcrumb("style", "action", "link");

                String url = (String) args[0];

                List<CharacterStyle> spans = new ArrayList<>();
                Map<CharacterStyle, Pair<Integer, Integer>> ranges = new HashMap<>();
                Map<CharacterStyle, Integer> flags = new HashMap<>();
                for (CharacterStyle span : edit.getSpans(start, end, CharacterStyle.class)) {
                    if (!(span instanceof URLSpan)) {
                        spans.add(span);
                        ranges.put(span, new Pair<>(edit.getSpanStart(span), edit.getSpanEnd(span)));
                        flags.put(span, edit.getSpanFlags(span));
                    }
                    edit.removeSpan(span);
                }

                if (url != null) {
                    int e = end;
                    if (start == end) {
                        etBody.getText().insert(start, url);
                        e += url.length();
                    }

                    edit.setSpan(new URLSpan(url), start, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // Restore other spans
                for (CharacterStyle span : spans)
                    edit.setSpan(span,
                            ranges.get(span).first,
                            ranges.get(span).second,
                            flags.get(span));

                etBody.setText(edit);
                etBody.setSelection(end, end);

                return true;
            } else if (action == R.id.menu_clear) {
                Log.breadcrumb("style", "action", "clear/all");

                for (Object span : edit.getSpans(0, etBody.length(), Object.class))
                    if (!(span instanceof ImageSpan))
                        edit.removeSpan(span);

                etBody.setText(edit);
                etBody.setSelection(start, end);

                return true;
            }
            return false;
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
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
            int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
            return (T) new NumberSpan(bulletIndent, bulletGap, colorAccent, n.getTextSize(), n.getLevel(), n.getIndex() + 1);
        } else if (BulletSpanEx.class.isAssignableFrom(type)) {
            BulletSpanEx b = (BulletSpanEx) span;
            int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.bullet_indent_size);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
                int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.bullet_gap_size);
                return (T) new BulletSpanEx(bulletIndent, bulletGap, colorAccent, b.getLevel());
            } else
                return (T) new BulletSpanEx(bulletIndent, b.getGapWidth(), b.getColor(), b.getBulletRadius(), b.getLevel());

        } else
            throw new IllegalArgumentException(type.getName());
    }

    static void renumber(Editable text, boolean clean, Context context) {
        int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.bullet_gap_size);
        int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.bullet_indent_size);
        int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);

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

    static TypefaceSpan getTypefaceSpan(String family, Context context) {
        String faces = family.toLowerCase(Locale.ROOT);
        if (faces.contains("comic sans"))
            family = "comic sans ms, sans-serif";
        return new CustomTypefaceSpan(family, getTypeface(family, context));
    }

    static Typeface getTypeface(String family, Context context) {
        String faces = family.toLowerCase(Locale.ROOT);

        if (faces.equals("fairemail"))
            return ResourcesCompat.getFont(context, R.font.fantasy);

        if (faces.contains("comic sans"))
            return ResourcesCompat.getFont(context, R.font.opendyslexic);

        for (String face : faces.split(",")) {
            face = face.trim().replace("\"", "");
            Typeface tf = Typeface.create(face, Typeface.NORMAL);
            if (!tf.equals(Typeface.DEFAULT))
                return tf;
        }
        return Typeface.DEFAULT;
    }

    //TextUtils.dumpSpans(text, new LogPrinter(android.util.Log.INFO, "FairEmail"), "afterTextChanged ");
}
