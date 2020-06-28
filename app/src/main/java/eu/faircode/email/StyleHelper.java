package eu.faircode.email;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

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

                case R.id.menu_size: {
                    final int s = start;
                    final int e = end;
                    final SpannableString t = ss;

                    int order = 1;
                    PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_style_size_small, order++, R.string.title_style_size_small);
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_style_size_medium, order++, R.string.title_style_size_medium);
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_style_size_large, order++, R.string.title_style_size_large);

                    popupMenu.getMenu().add(1, R.string.title_style_font_cursive, order++, R.string.title_style_font_cursive);
                    popupMenu.getMenu().add(1, R.string.title_style_font_serif, order++, R.string.title_style_font_serif);
                    popupMenu.getMenu().add(1, R.string.title_style_font_sans_serif, order++, R.string.title_style_font_sans_serif);
                    popupMenu.getMenu().add(1, R.string.title_style_font_monospace, order++, R.string.title_style_font_monospace);
                    popupMenu.getMenu().add(1, R.string.title_style_font_default, order++, R.string.title_style_font_default);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getGroupId() == Menu.NONE) {
                                RelativeSizeSpan[] spans = t.getSpans(s, e, RelativeSizeSpan.class);
                                for (RelativeSizeSpan span : spans)
                                    t.removeSpan(span);

                                Float size;
                                if (item.getItemId() == R.string.title_style_size_small)
                                    size = 0.8f;
                                else if (item.getItemId() == R.string.title_style_size_large)
                                    size = 1.25f;
                                else
                                    size = null;

                                if (size != null)
                                    t.setSpan(new RelativeSizeSpan(size), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                etBody.setText(t);
                                etBody.setSelection(s, e);
                            } else {
                                TypefaceSpan[] spans = t.getSpans(s, e, TypefaceSpan.class);
                                for (TypefaceSpan span : spans)
                                    t.removeSpan(span);

                                String face;
                                switch (item.getItemId()) {
                                    case R.string.title_style_font_cursive:
                                        face = "cursive";
                                        break;
                                    case R.string.title_style_font_serif:
                                        face = "serif";
                                        break;
                                    case R.string.title_style_font_sans_serif:
                                        face = "sans-serif";
                                        break;
                                    case R.string.title_style_font_monospace:
                                        face = "monospace";
                                        break;
                                    default:
                                        face = null;
                                }

                                if (face != null)
                                    t.setSpan(new TypefaceSpan(face), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                etBody.setText(t);
                                etBody.setSelection(s, e);
                            }

                            return false;
                        }
                    });

                    popupMenu.show();

                    return true;
                }

                case R.id.menu_color: {
                    for (ForegroundColorSpan span : ss.getSpans(start, end, ForegroundColorSpan.class))
                        ss.removeSpan(span);

                    ss.setSpan(new ForegroundColorSpan((int) args[0]), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etBody.setText(ss);
                    etBody.setSelection(start, end);

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
