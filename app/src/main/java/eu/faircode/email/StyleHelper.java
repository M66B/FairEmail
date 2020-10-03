package eu.faircode.email;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
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
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StyleHelper {
    static boolean apply(int action, View anchor, EditText etBody, Object... args) {
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

                    PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
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
                                        return setBlockquote(item);
                                    case R.id.group_style_strikethrough:
                                        return setStrikethrough(item);
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

                            int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
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
                                            t.setSpan(new BulletSpan(dp6, colorAccent), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);
                                        else
                                            t.setSpan(new BulletSpan(dp6, colorAccent, dp3), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);
                                    else
                                        t.setSpan(new NumberSpan(dp6, colorAccent, textSize, index++), i, j + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | Spanned.SPAN_PARAGRAPH);

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

                        private boolean setBlockquote(MenuItem item) {
                            Context context = etBody.getContext();

                            int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
                            int dp3 = Helper.dp2pixels(context, 3);
                            int dp6 = Helper.dp2pixels(context, 6);

                            QuoteSpan[] spans = t.getSpans(s, e, QuoteSpan.class);
                            for (QuoteSpan span : spans)
                                t.removeSpan(span);

                            QuoteSpan q;
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                                q = new QuoteSpan(colorPrimary);
                            else
                                q = new QuoteSpan(colorPrimary, dp3, dp6);
                            t.setSpan(q, s, e, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                            etBody.setText(t);
                            etBody.setSelection(s, e);

                            return true;
                        }

                        private boolean setStrikethrough(MenuItem item) {
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
                            for (Object span : t.getSpans(s, e, Object.class))
                                if (!(span instanceof ImageSpan))
                                    t.removeSpan(span);

                            etBody.setText(t);
                            etBody.setSelection(s, e);

                            return true;
                        }

                        private Pair<Integer, Integer> ensureParagraph(SpannableStringBuilder t, int s, int e) {
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
                    boolean selected = (start != end);
                    if (start == end) {
                        start = 0;
                        end = etBody.length();
                    }

                    for (Object span : ss.getSpans(start, end, Object.class))
                        if (!(span instanceof ImageSpan))
                            ss.removeSpan(span);

                    etBody.setText(ss);
                    if (selected)
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
}
