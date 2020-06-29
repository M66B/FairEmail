package eu.faircode.email;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.widget.PopupMenu;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.lang.reflect.Field;
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

                case R.id.menu_style: {
                    final int s = start;
                    final int e = end;
                    final SpannableString t = ss;

                    PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
                    popupMenu.inflate(R.menu.popup_style);

                    String[] fontNames = anchor.getResources().getStringArray(R.array.fontNameNames);
                    for (int i = 0; i < fontNames.length; i++)
                        popupMenu.getMenu().add(R.id.group_style_font, i, Menu.NONE, fontNames[i]);
                    popupMenu.getMenu().add(R.id.group_style_font, fontNames.length, Menu.NONE, R.string.title_style_font_default);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getGroupId()) {
                                case R.id.group_style_size:
                                    return setSize(item);
                                case R.id.group_style_color:
                                    return setColor(item);
                                case R.id.group_style_font:
                                    return setFont(item);
                                case R.id.group_style_clear:
                                    return clear(item);
                                default:
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

                            ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                                    .with(etBody.getContext())
                                    .setTitle(R.string.title_color)
                                    .showColorEdit(true)
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

                            Dialog dialog = builder.build();

                            try {
                                Field fColorEdit = builder.getClass().getDeclaredField("colorEdit");
                                fColorEdit.setAccessible(true);
                                EditText colorEdit = (EditText) fColorEdit.get(builder);
                                colorEdit.setTextColor(Helper.resolveColor(etBody.getContext(), android.R.attr.textColorPrimary));
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }

                            dialog.show();

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

                        private boolean clear(MenuItem item) {
                            for (Object span : t.getSpans(s, e, Object.class))
                                if (!(span instanceof ImageSpan))
                                    t.removeSpan(span);

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
