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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.google.android.material.chip.ChipDrawable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;
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
        int colorAccent = ColorUtils.setAlphaComponent(
                Helper.resolveColor(context, R.attr.colorAccent), 5 * 255 / 100);
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
                                        cd.setChipBackgroundColor(ColorStateList.valueOf(colorAccent));
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
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                Editable edit = getText();

                int off = Helper.getOffset(this, edit, event);
                ClipImageSpan[] spans = edit.getSpans(off, off, ClipImageSpan.class);
                if (spans.length != 1)
                    return false;

                final Context context = getContext();

                int start = edit.getSpanStart(spans[0]);
                int end = edit.getSpanEnd(spans[0]);
                if (start >= end)
                    return false;

                String email = edit.subSequence(start, end).toString().trim();
                if (email.endsWith(","))
                    email = email.substring(0, email.length() - 1);

                Address[] parsed = MessageHelper.parseAddresses(context, email);
                if (parsed == null && parsed.length != 1)
                    return false;

                String e = ((InternetAddress) parsed[0]).getAddress();
                String p = ((InternetAddress) parsed[0]).getPersonal();

                SpannableString ss = new SpannableString(TextUtils.isEmpty(e) ? p : e);
                ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
                ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);

                PopupMenu popupMenu = new PopupMenu(context, this);
                popupMenu.getMenu().add(Menu.NONE, 0, 1, ss).setEnabled(false);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_contact, 2, R.string.title_edit_contact);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 3, R.string.title_delete);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            int itemId = item.getItemId();
                            if (itemId == R.string.title_edit_contact) {
                                View dview = LayoutInflater.from(context).inflate(R.layout.dialog_edit_email, null);
                                EditText etEmail = dview.findViewById(R.id.etEmail);
                                EditText etName = dview.findViewById(R.id.etName);

                                etEmail.setText(e);
                                etName.setText(p);

                                new AlertDialog.Builder(context)
                                        .setView(dview)
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    String email = etEmail.getText().toString();
                                                    String name = etName.getText().toString();
                                                    InternetAddress a = new InternetAddress(email, name, StandardCharsets.UTF_8.name());
                                                    String formatted = MessageHelper.formatAddressesCompose(new Address[]{a});
                                                    edit.delete(start, end);
                                                    edit.insert(start, formatted);
                                                    setSelection(start + formatted.length());
                                                } catch (Throwable ex) {
                                                    Log.e(ex);
                                                }
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();

                                return true;
                            } else if (itemId == R.string.title_delete) {
                                edit.delete(start, end);
                                setSelection(start);
                                return true;
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                        return false;
                    }
                });

                popupMenu.show();

                return true;
            }

            return super.onTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }
}