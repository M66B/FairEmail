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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Rect;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private SharedPreferences prefs;
    private boolean dark;
    private int colorAccent;
    private ContextThemeWrapper ctx;

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

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        dark = Helper.isDarkTheme(context);
        colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        colorAccent = ColorUtils.setAlphaComponent(colorAccent, 5 * 255 / 100);
        ctx = new ContextThemeWrapper(context, dark ? R.style.ChipDark : R.style.ChipLight);

        addTextChangedListener(new TextWatcher() {
            private Integer backspace = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                backspace = (count - after == 1 ? start : null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable edit) {
                if (backspace != null) {
                    ClipImageSpan[] spans = edit.getSpans(backspace, backspace, ClipImageSpan.class);
                    if (spans.length == 1) {
                        int start = edit.getSpanStart(spans[0]);
                        int end = edit.getSpanEnd(spans[0]);
                        edit.delete(start, end);
                    }
                }

                if (getWidth() == 0)
                    post(update);
                else
                    update.run();
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        post(update);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        post(update);
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
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                Editable edit = getText();

                int off = Helper.getOffset(this, edit, event);
                ClipImageSpan[] spans = edit.getSpans(off, off, ClipImageSpan.class);
                if (spans.length != 1)
                    return super.onTouchEvent(event);

                final Context context = getContext();

                int start = edit.getSpanStart(spans[0]);
                int end = edit.getSpanEnd(spans[0]);
                if (start >= end)
                    return super.onTouchEvent(event);

                String email = edit.subSequence(start, end).toString().trim();
                if (email.endsWith(","))
                    email = email.substring(0, email.length() - 1);

                Address[] parsed = MessageHelper.parseAddresses(context, email);
                if (parsed == null && parsed.length != 1)
                    return super.onTouchEvent(event);

                String e = ((InternetAddress) parsed[0]).getAddress();
                String p = ((InternetAddress) parsed[0]).getPersonal();

                SpannableString ss = new SpannableString(TextUtils.isEmpty(e) ? p : e);
                ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
                ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);

                TwoStateOwner owner = new TwoStateOwner("Chip");
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, this);
                popupMenu.getMenu().add(Menu.NONE, 0, 1, ss)
                        .setEnabled(false)
                        .setIcon(R.drawable.twotone_alternate_email_24);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_contact, 2, R.string.title_edit_contact)
                        .setIcon(R.drawable.twotone_edit_24);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_clipboard_copy, 3, R.string.title_clipboard_copy)
                        .setIcon(R.drawable.twotone_file_copy_24);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 4, R.string.title_delete)
                        .setIcon(R.drawable.twotone_delete_24);

                popupMenu.insertIcons(context);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            int itemId = item.getItemId();
                            if (itemId == R.string.title_edit_contact) {
                                return onEdit();
                            } else if (itemId == R.string.title_clipboard_copy) {
                                return onCopy();
                            } else if (itemId == R.string.title_delete) {
                                return onDelete();
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                        return false;
                    }

                    private boolean onEdit() {
                        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_edit_email, null);
                        EditText etEmail = dview.findViewById(R.id.etEmail);
                        EditText etName = dview.findViewById(R.id.etName);

                        etEmail.setText(e);
                        etName.setText(p);

                        AlertDialog dialog = new AlertDialog.Builder(context)
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
                                .create();

                        TextView.OnEditorActionListener done = new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                                    return true;
                                } else
                                    return false;
                            }
                        };

                        etEmail.setOnEditorActionListener(done);
                        etName.setOnEditorActionListener(done);

                        dialog.show();

                        return true;
                    }

                    private boolean onCopy() {
                        ClipboardManager clipboard =
                                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard == null)
                            return false;

                        String formatted = MessageHelper.formatAddressesCompose(parsed);
                        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), formatted);
                        clipboard.setPrimaryClip(clip);
                        ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();

                        return true;
                    }

                    private boolean onDelete() {
                        edit.delete(start, end);
                        setSelection(start);

                        return true;
                    }
                });

                popupMenu.show();

                return true;
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return super.onTouchEvent(event);
    }

    private final Runnable update = new Runnable() {
        @Override
        public void run() {
            try {
                final Context context = getContext();
                final Editable edit = getText();
                final int len = edit.length();
                final boolean send_chips = prefs.getBoolean("send_chips", !BuildConfig.PLAY_STORE_RELEASE);

                final boolean focus = hasFocus();
                final int selStart = getSelectionStart();
                final int selEnd = getSelectionEnd();

                boolean added = false;
                List<ClipImageSpan> tbd = new ArrayList<>();
                tbd.addAll(Arrays.asList(edit.getSpans(0, len, ClipImageSpan.class)));

                if (send_chips) {
                    int start = 0;
                    boolean space = true;
                    boolean quote = false;
                    for (int i = 0; i < len; i++) {
                        char kar = edit.charAt(i);

                        if (space && kar == ' ') {
                            start++;
                            continue;
                        }
                        space = false;

                        if (kar == '"')
                            quote = !quote;
                        else if (!quote && (kar == ',' || (!focus && i + 1 == len))) {
                            boolean found = false;
                            for (ClipImageSpan span : new ArrayList<>(tbd)) {
                                int s = edit.getSpanStart(span);
                                int e = edit.getSpanEnd(span);
                                if (s == start && e == i + 1) {
                                    found = true;
                                    if (!(focus && overlap(start, i, selStart, selEnd)))
                                        tbd.remove(span);
                                    break;
                                }
                            }

                            if (!found && start < i + 1 &&
                                    !(focus && overlap(start, i, selStart, selEnd))) {
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
                                    if (kar != ',')
                                        edit.insert(++i, ",");

                                    Drawable avatar = null;
                                    Uri lookupUri = ContactInfo.getLookupUri(parsed);
                                    if (lookupUri != null) {
                                        InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                                context.getContentResolver(), lookupUri, false);
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

                                    if (i + 1 == len || edit.charAt(i + 1) != ' ')
                                        edit.insert(++i, " ");
                                    added = true;
                                }
                            }

                            start = i + 1;
                            space = true;
                        }
                    }
                }

                for (ClipImageSpan span : tbd)
                    edit.removeSpan(span);

                if (tbd.size() > 0 || added)
                    invalidate();
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    };

    private static boolean overlap(int start, int end, int selStart, int selEnd) {
        return Math.max(start, selStart) <= Math.min(end, selEnd);
    }

    private static class ClipImageSpan extends ImageSpan {
        public ClipImageSpan(@NonNull Drawable drawable) {
            super(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
        }
    }
}