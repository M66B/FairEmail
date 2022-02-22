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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.preference.PreferenceManager;

import com.google.android.material.chip.ChipDrawable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EditTextMultiAutoComplete extends AppCompatMultiAutoCompleteTextView {
    public EditTextMultiAutoComplete(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EditTextMultiAutoComplete(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditTextMultiAutoComplete(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Helper.setKeyboardIncognitoMode(this, context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dark = Helper.isDarkTheme(context);
        ContextThemeWrapper ctx = new ContextThemeWrapper(context,
                dark ? R.style.ChipDark : R.style.ChipLight);
        ContentResolver resolver = context.getContentResolver();

        Runnable update = new Runnable() {
            @Override
            public void run() {
                try {
                    Editable edit = getText();
                    boolean send_chips = prefs.getBoolean("send_chips", false);

                    boolean added = false;
                    List<ClipImageSpan> spans = new ArrayList<>();
                    spans.addAll(Arrays.asList(edit.getSpans(0, edit.length(), ClipImageSpan.class)));

                    if (send_chips) {
                        boolean quote = false;
                        int start = 0;
                        for (int i = 0; i < edit.length(); i++) {
                            char kar = edit.charAt(i);
                            if (kar == '"')
                                quote = !quote;
                            else if (kar == ',' && !quote) {
                                boolean found = false;
                                for (ClipImageSpan span : new ArrayList<>(spans)) {
                                    int s = edit.getSpanStart(span);
                                    int e = edit.getSpanEnd(span);
                                    if (s == start && e == i + 1) {
                                        found = true;
                                        spans.remove(span);
                                        break;
                                    }
                                }

                                if (!found && start < i + 1) {
                                    String email = edit.subSequence(start, i + 1).toString();
                                    InternetAddress[] parsed;
                                    try {
                                        parsed = MessageHelper.parseAddresses(context, email);
                                        if (parsed != null)
                                            for (InternetAddress a : parsed)
                                                a.validate();
                                    } catch (AddressException ex) {
                                        parsed = null;
                                    }

                                    if (parsed != null && parsed.length == 1) {
                                        Drawable avatar = null;
                                        Uri lookupUri = ContactInfo.getLookupUri(parsed);
                                        if (lookupUri != null) {
                                            InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                                    resolver, lookupUri, false);
                                            avatar = Drawable.createFromStream(is, email);
                                        }

                                        String e = parsed[0].getAddress();
                                        String p = parsed[0].getPersonal();
                                        String text = (TextUtils.isEmpty(p) ? e : p);

                                        // https://github.com/material-components/material-components-android/blob/master/docs/components/Chip.md
                                        ChipDrawable cd = ChipDrawable.createFromResource(ctx, R.xml.chip);
                                        cd.setChipIcon(avatar);
                                        // cd.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
                                        cd.setText(text);
                                        cd.setMaxWidth(getWidth());
                                        cd.setBounds(0, 0, cd.getIntrinsicWidth(), cd.getIntrinsicHeight());

                                        ClipImageSpan is = new ClipImageSpan(cd);
                                        edit.setSpan(is, start, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        added = true;
                                    }
                                }

                                if (i + 1 < edit.length() && edit.charAt(i + 1) == ' ')
                                    start = i + 2;
                                else
                                    start = i + 1;
                            }
                        }
                    }

                    for (ClipImageSpan span : spans)
                        edit.removeSpan(span);

                    if (spans.size() > 0 || added)
                        invalidate();
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        };

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable edit) {
                if (getWidth() == 0)
                    post(update);
                else
                    update.run();
            }
        });
    }

    private static class ClipImageSpan extends ImageSpan {
        public ClipImageSpan(@NonNull Drawable drawable) {
            super(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
        }
    }

    @Override
    public boolean onPreDraw() {
        try {
            return super.onPreDraw();
        } catch (Throwable ex) {
            Log.w(ex);
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Throwable ex) {
            Log.w(ex);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            return super.dispatchTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }
}