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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StyleHelper {
    static boolean apply(int action, LifecycleOwner owner, View anchor, EditText etBody, Object... args) {
        Log.i("Style action=" + action);

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

            SpannableString ss = new SpannableString(etBody.getText());

            switch (action) {
                case R.id.menu_bold:
                case R.id.menu_italic: {
                    int style = (action == R.id.menu_bold ? Typeface.BOLD : Typeface.ITALIC);
                    boolean has = false;
                    for (StyleSpan span : ss.getSpans(start, end, StyleSpan.class))
                        if (span.getStyle() == style) {
                            has = true;
                            ss.removeSpan(span);
                        }

                    if (!has)
                        ss.setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etBody.setText(ss);
                    etBody.setSelection(start, end);

                    return true;
                }

                case R.id.menu_underline: {
                    boolean has = false;
                    for (UnderlineSpan span : ss.getSpans(start, end, UnderlineSpan.class)) {
                        has = true;
                        ss.removeSpan(span);
                    }

                    if (!has)
                        ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etBody.setText(ss);
                    etBody.setSelection(start, end);

                    return true;
                }

                case R.id.menu_style: {
                    final int s = start;
                    final int e = end;
                    final SpannableStringBuilder t = new SpannableStringBuilder(ss);

                    PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(anchor.getContext(), owner, anchor);
                    popupMenu.inflate(R.menu.popup_style);

                    String[] fontNames = anchor.getResources().getStringArray(R.array.fontNameNames);
                    SubMenu smenu = popupMenu.getMenu().findItem(R.id.menu_style_font).getSubMenu();
                    for (int i = 0; i < fontNames.length; i++)
                        smenu.add(R.id.group_style_font, i, 0, fontNames[i]);
                    smenu.add(R.id.group_style_font, fontNames.length, 0, R.string.title_style_font_default);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                switch (item.getGroupId()) {
                                    case R.id.group_style_size:
                                        return setSize(item);
                                    case R.id.group_style_color:
                                        return setColor(item);
                                    case R.id.group_style_align:
                                        return setAlignment(item);
                                    case R.id.group_style_list:
                                        return setList(item);
                                    case R.id.group_style_font:
                                        return setFont(item);
                                    case R.id.group_style_blockquote:
                                        return setBlockQuote(item);
                                    case R.id.group_style_strikethrough:
                                        return setStrikeThrough(item);
                                    case R.id.group_style_clear:
                                        return clear(item);
                                    default:
                                        return false;
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                                return false;
                            }
                        }

                        private boolean setSize(MenuItem item) {
                            RelativeSizeSpan[] spans = t.getSpans(s, e, RelativeSizeSpan.class);
                            for (RelativeSizeSpan span : spans)
                                t.removeSpan(span);

                            Float size;
                            if (item.getItemId() == R.id.menu_style_size_small)
                                size = 0.8f;
                            else if (item.getItemId() == R.id.menu_style_size_large)
                                size = 1.25f;
                            else
                                size = null;

                            if (size != null)
                                t.setSpan(new RelativeSizeSpan(size), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            etBody.setText(t);
                            etBody.setSelection(s, e);

                            return true;
                        }

                        private boolean setColor(MenuItem item) {
                            InputMethodManager imm = (InputMethodManager) etBody.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.hideSoftInputFromWindow(etBody.getWindowToken(), 0);

                            Context context = etBody.getContext();
                            int editTextColor = Helper.resolveColor(context, android.R.attr.editTextColor);

                            ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                                    .with(context)
                                    .setTitle(R.string.title_color)
                                    .showColorEdit(true)
                                    .setColorEditTextColor(editTextColor)
                                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                    .density(6)
                                    .lightnessSliderOnly()
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
                            for (ForegroundColorSpan span : t.getSpans(s, e, ForegroundColorSpan.class))
                                t.removeSpan(span);

                            if (color != null)
                                t.setSpan(new ForegroundColorSpan(color), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            etBody.setText(t);
                            etBody.setSelection(s, e);
                        }

                        private boolean setAlignment(MenuItem item) {
                            Pair<Integer, Integer> paragraph = ensureParagraph(t, s, e);
                            int start = paragraph.first;
                            int end = paragraph.second;

                            AlignmentSpan[] spans = t.getSpans(start, end, AlignmentSpan.class);
                            for (AlignmentSpan span : spans)
                                t.removeSpan(span);

                            Layout.Alignment alignment = null;
                            boolean ltr = (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LTR);
                            switch (item.getItemId()) {
                                case R.id.menu_style_align_start:
                                    alignment = (ltr ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE);
                                    break;
                                case R.id.menu_style_align_center:
                                    alignment = Layout.Alignment.ALIGN_CENTER;
                                    break;
                                case R.id.menu_style_align_end:
                                    alignment = (ltr ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL);
                                    break;
                            }

                            if (alignment != null)
                                t.setSpan(new AlignmentSpan.Standard(alignment),
                                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);

                            etBody.setText(t);
                            etBody.setSelection(start, end);

                            return true;
                        }

                        private boolean setList(MenuItem item) {
                            Context context = etBody.getContext();

                            int colorSecondary = Helper.resolveColor(context, R.attr.colorSecondary);
                            int dp3 = Helper.dp2pixels(context, 3);
                            int dp6 = Helper.dp2pixels(context, 6);

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            int message_zoom = prefs.getInt("message_zoom", 100);
                            float textSize = Helper.getTextSize(context, 0) * message_zoom / 100f;

                            Pair<Integer, Integer> paragraph = ensureParagraph(t, s, e);
                            int start = paragraph.first;
                            int end = paragraph.second;

                            // Remove existing bullets
                            BulletSpan[] spans = t.getSpans(start, end, BulletSpan.class);
                            for (BulletSpan span : spans)
                                t.removeSpan(span);

                            int i = start;
                            int j = start + 1;
                            int index = 1;
                            while (j < end) {
                                if (i > 0 && t.charAt(i - 1) == '\n' && t.charAt(j) == '\n') {
                                    Log.i("Insert " + i + "..." + (j + 1) + " size=" + end);
                                    if (item.getItemId() == R.id.menu_style_list_bullets)
                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                                            t.setSpan(new BulletSpan(dp6, colorSecondary), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);
                                        else
                                            t.setSpan(new BulletSpan(dp6, colorSecondary, dp3), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);
                                    else
                                        t.setSpan(new NumberSpan(dp6, colorSecondary, textSize, index++), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);

                                    i = j + 1;
                                }
                                j++;
                            }

                            etBody.setText(t);
                            etBody.setSelection(start, end);

                            return true;
                        }

                        private boolean setFont(MenuItem item) {
                            TypefaceSpan[] spans = t.getSpans(s, e, TypefaceSpan.class);
                            for (TypefaceSpan span : spans)
                                t.removeSpan(span);

                            int id = item.getItemId();
                            String[] names = anchor.getResources().getStringArray(R.array.fontNameValues);
                            String face = (id < names.length ? names[id] : null);

                            if (face != null)
                                t.setSpan(new TypefaceSpan(face), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            etBody.setText(t);
                            etBody.setSelection(s, e);

                            return true;
                        }

                        private boolean setBlockQuote(MenuItem item) {
                            Context context = etBody.getContext();

                            int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
                            int dp3 = Helper.dp2pixels(context, 3);
                            int dp6 = Helper.dp2pixels(context, 6);

                            Pair<Integer, Integer> paragraph = ensureParagraph(t, s, e);
                            int start = paragraph.first;
                            int end = paragraph.second;

                            QuoteSpan[] spans = t.getSpans(s, e, QuoteSpan.class);
                            for (QuoteSpan span : spans)
                                t.removeSpan(span);

                            QuoteSpan q;
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                                q = new QuoteSpan(colorPrimary);
                            else
                                q = new QuoteSpan(colorPrimary, dp3, dp6);
                            t.setSpan(q, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                            etBody.setText(t);
                            etBody.setSelection(start, end);

                            return true;
                        }

                        private boolean setStrikeThrough(MenuItem item) {
                            boolean has = false;
                            for (StrikethroughSpan span : t.getSpans(s, e, StrikethroughSpan.class)) {
                                has = true;
                                t.removeSpan(span);
                            }

                            if (!has)
                                t.setSpan(new StrikethroughSpan(), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            etBody.setText(t);
                            etBody.setSelection(s, e);

                            return true;
                        }

                        private boolean clear(MenuItem item) {
                            int start = s;
                            int end = e;

                            // Expand to paragraph (block quotes)
                            if (end + 1 < t.length() && t.charAt(end) == '\n')
                                end++;

                            for (Object span : t.getSpans(start, end, Object.class))
                                if (!(span instanceof ImageSpan)) {
                                    int sstart = t.getSpanStart(span);
                                    int send = t.getSpanEnd(span);
                                    int flags = t.getSpanFlags(span);
                                    if (sstart < start && send > start)
                                        setSpan(t, span, sstart, start, flags, etBody.getContext());
                                    if (sstart < end && send > end)
                                        setSpan(t, span, end, send, flags, etBody.getContext());

                                    t.removeSpan(span);
                                }

                            etBody.setText(t);
                            etBody.setSelection(s, e);

                            return true;
                        }
                    });

                    popupMenu.show();

                    return true;
                }

                case R.id.menu_link: {
                    String url = (String) args[0];

                    List<Object> spans = new ArrayList<>();
                    for (Object span : ss.getSpans(start, end, Object.class)) {
                        if (!(span instanceof URLSpan))
                            spans.add(span);
                        ss.removeSpan(span);
                    }

                    if (url != null) {
                        if (start == end) {
                            etBody.getText().insert(start, url);
                            end += url.length();
                            ss = new SpannableString(etBody.getText());
                        }

                        ss.setSpan(new URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    // Restore other spans
                    for (Object span : spans)
                        ss.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etBody.setText(ss);
                    etBody.setSelection(end, end);

                    return true;
                }

                case R.id.menu_clear: {
                    for (Object span : ss.getSpans(0, etBody.length(), Object.class))
                        if (!(span instanceof ImageSpan))
                            ss.removeSpan(span);

                    etBody.setText(ss);
                    etBody.setSelection(start, end);

                    return true;
                }

                default:
                    return false;
            }
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static void setSpan(SpannableStringBuilder ss, Object span, int start, int end, int flags, Context context) {
        if (span instanceof CharacterStyle)
            ss.setSpan(CharacterStyle.wrap((CharacterStyle) span), start, end, flags);
        else if (span instanceof QuoteSpan) {
            ParagraphStyle ps = (ParagraphStyle) span;
            Pair<Integer, Integer> p = ensureParagraph(ss, start, end);
            ss.setSpan(clone(span, ps.getClass(), context), p.first, p.second, flags);
        }
    }

    static private Pair<Integer, Integer> ensureParagraph(SpannableStringBuilder t, int s, int e) {
        int start = s;
        int end = e;

        // Expand selection at start
        while (start > 0 && t.charAt(start - 1) != '\n')
            start--;

        // Expand selection at end
        while (end > 0 && end < t.length() && t.charAt(end - 1) != '\n')
            end++;

        // Nothing to do
        if (start == end)
            return null;

        // Create paragraph at start
        if (start == 0 && t.charAt(start) != '\n') {
            t.insert(0, "\n");
            start++;
            end++;
        }

        // Create paragraph at end
        if (end == t.length() && t.charAt(end - 1) != '\n') {
            t.append("\n");
            end++;
        }

        if (end == t.length())
            t.append("\n"); // workaround Android bug

        return new Pair(start, end);
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
            int dp6 = Helper.dp2pixels(context, 6);
            int colorSecondary = Helper.resolveColor(context, R.attr.colorSecondary);
            return (T) new NumberSpan(dp6, colorSecondary, n.getTextSize(), n.getIndex() + 1);
        } else if (BulletSpan.class.isAssignableFrom(type)) {
            BulletSpan b = (BulletSpan) span;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                int dp6 = Helper.dp2pixels(context, 6);
                int colorSecondary = Helper.resolveColor(context, R.attr.colorSecondary);
                return (T) new BulletSpan(dp6, colorSecondary);
            } else
                return (T) new BulletSpan(b.getGapWidth(), b.getColor(), b.getBulletRadius());

        } else
            throw new IllegalArgumentException(type.getName());
    }

    static void renumber(Editable text, boolean clean, Context context) {
        int dp6 = Helper.dp2pixels(context, 6);
        int colorSecondary = Helper.resolveColor(context, R.attr.colorSecondary);

        Log.i("Renumber clean=" + clean + " text=" + text);

        int next;
        int index = 1;
        int pos = -1;
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

                if (span instanceof NumberSpan) {
                    if (start == pos)
                        index++;
                    else
                        index = 1;

                    NumberSpan ns = (NumberSpan) span;
                    if (index != ns.getIndex()) {
                        NumberSpan clone = new NumberSpan(dp6, colorSecondary, ns.getTextSize(), index);
                        text.removeSpan(span);
                        text.setSpan(clone, start, end, flags);
                    }

                    pos = end;
                }
            }
        }
    }

    //TextUtils.dumpSpans(text, new LogPrinter(android.util.Log.INFO, "FairEmail"), "afterTextChanged ");
}
