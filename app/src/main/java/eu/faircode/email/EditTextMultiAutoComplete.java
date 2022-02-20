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
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;

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

        if (BuildConfig.DEBUG) {
            boolean dark = Helper.isDarkTheme(context);
            ContextThemeWrapper ctx = new ContextThemeWrapper(context,
                    dark ? R.style.Base_Theme_Material3_Dark : R.style.Base_Theme_Material3_Light);
            ContentResolver resolver = context.getContentResolver();
            int dp3 = Helper.dp2pixels(context, 3);

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
                    boolean added = false;
                    List<ImageSpan> spans = new ArrayList<>();
                    spans.addAll(Arrays.asList(edit.getSpans(0, edit.length(), ImageSpan.class)));

                    boolean quote = false;
                    int start = 0;
                    for (int i = 0; i < edit.length(); i++) {
                        char kar = edit.charAt(i);
                        if (kar == '"')
                            quote = !quote;
                        else if (kar == ',' && !quote) {
                            boolean found = false;
                            for (ImageSpan span : new ArrayList<>(spans)) {
                                int s = edit.getSpanStart(span);
                                int e = edit.getSpanEnd(span);
                                if (s == start && e == i + 1) {
                                    found = true;
                                    spans.remove(span);
                                    break;
                                }
                            }

                            if (!found) {
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

                                    ChipDrawable cd = ChipDrawable.createFromResource(ctx, R.xml.chip);
                                    cd.setChipIcon(avatar);
                                    cd.setCloseIcon(null);
                                    cd.setTextStartPadding(dp3);
                                    cd.setTextEndPadding(dp3);

                                    String e = parsed[0].getAddress();
                                    String p = parsed[0].getPersonal();
                                    if (TextUtils.isEmpty(p))
                                        if (TextUtils.isEmpty(e))
                                            cd.setText(email);
                                        else
                                            cd.setText(e);
                                    else
                                        cd.setText(p);

                                    cd.setBounds(0, 0, cd.getIntrinsicWidth(), cd.getIntrinsicHeight());

                                    ImageSpan is = new ImageSpan(cd, DynamicDrawableSpan.ALIGN_BOTTOM);
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

                    for (ImageSpan span : spans)
                        edit.removeSpan(span);

                    if (spans.size() > 0 || added)
                        invalidate();
                }
            });
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