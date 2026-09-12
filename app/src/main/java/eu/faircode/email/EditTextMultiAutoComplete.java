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

    Copyright 2018-2026 by Marcel Bokhorst (M66B)
*/

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.google.android.material.chip.ChipDrawable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EditTextMultiAutoComplete extends AppCompatMultiAutoCompleteTextView {
    private SharedPreferences prefs;
    private boolean dark;
    private int colorAccent;
    private Tokenizer tokenizer;
    private float dragDownX;
    private float dragDownY;
    private Map<String, Integer> encryption = new ConcurrentHashMap<>();

    private static int[] icons = new int[]{
            R.drawable.twotone_vpn_key_24_p,
            R.drawable.twotone_vpn_key_24_s,
            R.drawable.twotone_vpn_key_24_b
    };

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

        tokenizer = new CommaTokenizer();
        setTokenizer(tokenizer);

        setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                try {
                    if (!(event.getLocalState() instanceof DragState))
                        return false;

                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            return (event.getClipDescription() != null &&
                                    event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN));

                        case DragEvent.ACTION_DRAG_ENTERED:
                        case DragEvent.ACTION_DRAG_LOCATION:
                            return true;

                        case DragEvent.ACTION_DROP:
                            return dropClip((DragState) event.getLocalState(), event.getX(), event.getY());

                        case DragEvent.ACTION_DRAG_EXITED:
                        case DragEvent.ACTION_DRAG_ENDED:
                            return true;

                        default:
                            return false;
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                    return false;
                }
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        dark = Helper.isDarkTheme(context);
        colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
        colorAccent = ColorUtils.setAlphaComponent(colorAccent, 5 * 255 / 100);

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
                if (backspace != null)
                    post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (edit == null || backspace == null || !hasFocus())
                                    return;
                                ClipImageSpan[] spans = edit.getSpans(backspace, backspace, ClipImageSpan.class);
                                if (spans.length == 1) {
                                    int start = edit.getSpanStart(spans[0]);
                                    int end = edit.getSpanEnd(spans[0]);
                                    if (backspace > start)
                                        edit.delete(start, end);
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }
                    });

                post(update);
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
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : Helper.LOW_LIGHT);
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
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dragDownX = event.getX();
                    dragDownY = event.getY();
                    break;

                case MotionEvent.ACTION_UP:
                    boolean result = super.onTouchEvent(event);

                    ClipImageSpan span = getClipImageSpan(event.getX(), event.getY());
                    if (span != null) {
                        Editable edit = getText();
                        if (edit != null) {
                            int start = edit.getSpanStart(span);
                            int end = edit.getSpanEnd(span);
                            if (start >= 0 && end > start)
                                setSelection(start);
                        }
                    }

                    dragDownX = 0;
                    dragDownY = 0;
                    return result;

                case MotionEvent.ACTION_CANCEL:
                    dragDownX = 0;
                    dragDownY = 0;
                    break;
            }
            return super.onTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            return true;
        }
    }

    @Nullable
    private ClipImageSpan getClipImageSpan(float x, float y) {
        Editable edit = getText();
        Layout layout = getLayout();
        if (edit == null || layout == null || layout.getLineCount() == 0)
            return null;

        int line = layout.getLineForVertical((int) (y - getTotalPaddingTop() + getScrollY()));
        line = Math.max(0, Math.min(line, layout.getLineCount() - 1));

        float horizontal = x - getTotalPaddingLeft() + getScrollX();
        ClipImageSpan[] spans = edit.getSpans(0, edit.length(), ClipImageSpan.class);

        for (ClipImageSpan span : spans) {
            int start = edit.getSpanStart(span);
            int end = edit.getSpanEnd(span);

            if (start < 0 || end <= start)
                continue;

            if (layout.getLineForOffset(start) != line)
                continue;

            float startX = layout.getPrimaryHorizontal(start);
            Drawable drawable = span.getDrawable();
            if (drawable == null)
                continue;

            float width = drawable.getBounds().width();
            if (width <= 0)
                width = drawable.getIntrinsicWidth();

            float left;
            float right;
            if (layout.getParagraphDirection(line) == Layout.DIR_RIGHT_TO_LEFT) {
                left = startX - width;
                right = startX;
            } else {
                left = startX;
                right = startX + width;
            }

            if (horizontal >= left && horizontal <= right)
                return span;
        }

        return null;
    }

    @Override
    public boolean performLongClick() {
        try {
            Editable edit = getText();
            Layout layout = getLayout();
            if (edit == null || layout == null)
                return super.performLongClick();

            int line = layout.getLineForVertical((int) (dragDownY - getTotalPaddingTop() + getScrollY()));
            float x = dragDownX - getTotalPaddingLeft() + getScrollX();
            int offset = layout.getOffsetForHorizontal(line, x);

            ClipImageSpan[] spans = edit.getSpans(offset, offset, ClipImageSpan.class);
            if (spans.length != 1)
                return super.performLongClick();

            ClipImageSpan span = spans[0];

            int start = edit.getSpanStart(span);
            int end = edit.getSpanEnd(span);

            if (start < 0 || end <= start)
                return super.performLongClick();

            Helper.performHapticFeedback(this, HapticFeedbackConstants.CONFIRM);

            int tokenEnd = getTokenEnd(edit, end);
            String text = edit.subSequence(start, tokenEnd).toString();
            DragState state = new DragState(this, span, start, end, tokenEnd, text);
            ClipData data = ClipData.newPlainText("FairEmail recipient", text);
            View.DragShadowBuilder shadow = new ChipDragShadowBuilder(span);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return startDragAndDrop(data, shadow, state, View.DRAG_FLAG_OPAQUE);
            else {
                startDrag(data, shadow, state, 0);
                return true;
            }
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }


    @Override
    protected void replaceText(CharSequence text) {
        HtmlHelper.clearComposingText(this);

        Editable edit = getText();
        int _end = getSelectionEnd();
        int start = tokenizer.findTokenStart(edit, _end);
        int end = tokenizer.findTokenEnd(edit, _end);
        if (end < edit.length() && edit.charAt(end) == ',') {
            end++;
            while (end < edit.length() && edit.charAt(end) == ' ')
                end++;
        }

        edit.replace(start, end, tokenizer.terminateToken(text));

        setSelection(edit.length());
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            if (id == android.R.id.paste) {
                Context context = getContext();
                ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);
                if (cbm != null && cbm.hasPrimaryClip()) {
                    ClipData data = cbm.getPrimaryClip();
                    ClipDescription description = (data == null ? null : data.getDescription());
                    ClipData.Item item = (data == null ? null : data.getItemAt(0));
                    CharSequence text = (item == null ? null : item.coerceToText(context));
                    if (text != null) {
                        CharSequence label = (description == null ? "coerced_plain_text" : description.getLabel());
                        data = ClipData.newPlainText(label, text.toString());
                        cbm.setPrimaryClip(data);
                    }
                }
            }

            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    private final Runnable update = new Runnable() {
        @Override
        public void run() {
            try {
                final Context context = getContext();
                final Resources res = getResources();
                final Editable edit = getText();
                final int dp3 = Helper.dp2pixels(context, 3);
                final int dp24 = Helper.dp2pixels(context, 24);
                final boolean send_chips = prefs.getBoolean("send_chips", true);
                boolean generated = prefs.getBoolean("generated_icons", true);
                boolean identicons = prefs.getBoolean("identicons", false);
                boolean circular = prefs.getBoolean("circular", true);

                final boolean focus = hasFocus();
                final int selStart = getSelectionStart();
                final int selEnd = getSelectionEnd();

                boolean added = false;
                List<ClipImageSpan> tbd = new ArrayList<>();
                tbd.addAll(Arrays.asList(edit.getSpans(0, edit.length(), ClipImageSpan.class)));

                if (send_chips) {
                    int start = 0;
                    boolean space = true;
                    boolean quote = false;
                    for (int i = 0; i < edit.length(); i++) {
                        char kar = edit.charAt(i);

                        if (space && kar == ' ') {
                            start++;
                            continue;
                        }
                        space = false;

                        if (kar == '"')
                            quote = !quote;
                        else if (!quote && (kar == ',' || (!focus && i + 1 == edit.length()))) {
                            boolean found = false;
                            for (ClipImageSpan span : new ArrayList<>(tbd)) {
                                int s = edit.getSpanStart(span);
                                int e = edit.getSpanEnd(span);
                                if (s == start && e == i + 1) {
                                    found = !span.needsUpdate();
                                    if (found && !(focus && overlap(start, i, selStart, selEnd)))
                                        tbd.remove(span);
                                    break;
                                }
                            }

                            if (!found && start < i + 1 &&
                                    !(focus && overlap(start, i, selStart, selEnd))) {
                                String address = edit.subSequence(start, i + 1).toString();
                                InternetAddress[] parsed;
                                try {
                                    parsed = MessageHelper.parseAddresses(context, address);
                                    if (parsed != null)
                                        for (InternetAddress a : parsed)
                                            a.validate();
                                } catch (AddressException ex) {
                                    parsed = null;
                                }

                                if (parsed != null && parsed.length == 1) {
                                    if (kar == ' ')
                                        edit.insert(i++, ",");
                                    else if (kar != ',')
                                        edit.insert(++i, ",");

                                    String email = parsed[0].getAddress();
                                    String personal = parsed[0].getPersonal();

                                    Bitmap bm = null;
                                    Uri lookupUri = ContactInfo.getLookupUri(parsed);
                                    if (lookupUri != null)
                                        try (InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                                context.getContentResolver(), lookupUri, false)) {
                                            bm = BitmapFactory.decodeStream(is);
                                        } catch (Throwable ex) {
                                            Log.e(ex);
                                        }

                                    if (bm == null && generated && !TextUtils.isEmpty(address))
                                        if (identicons)
                                            bm = ImageHelper.generateIdenticon(email, dp24, 5, context);
                                        else
                                            bm = ImageHelper.generateLetterIcon(email, personal, dp24, context);

                                    if (bm != null && circular && !identicons)
                                        bm = ImageHelper.makeCircular(bm, dp3);

                                    Drawable avatar = (bm == null ? null : new BitmapDrawable(res, bm));

                                    String text = (TextUtils.isEmpty(personal) ? email : personal);

                                    // https://github.com/material-components/material-components-android/blob/master/docs/components/Chip.md
                                    Context ctx = ApplicationEx.getThemedContext(context, dark ? R.style.ChipDark : R.style.ChipLight);
                                    ChipDrawable cd = ChipDrawable.createFromResource(ctx, R.xml.chip);
                                    cd.setChipIcon(avatar);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                        try {
                                            if (TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(text, 0, text.length()))
                                                cd.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                                        } catch (Throwable ex) {
                                            Log.e(ex);
                                        }
                                    cd.setText(text);
                                    cd.setChipBackgroundColor(ColorStateList.valueOf(colorAccent));

                                    ClipImageSpan is = new ClipImageSpan(cd);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                        is.setContentDescription(email);

                                    Integer has = encryption.get(email);
                                    if (has == null) {
                                        final List<Address> recipient = Arrays.asList(new Address[]{parsed[0]});
                                        Helper.getUIExecutor().submit(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    int has = 0;
                                                    if (PgpHelper.hasPgpKey(context, recipient, true))
                                                        has |= 1;
                                                    if (SmimeHelper.hasSmimeKey(context, recipient, true))
                                                        has |= 2;
                                                    encryption.put(email, has);

                                                    if (has != 0) {
                                                        is.invalidate();
                                                        post(update);
                                                    }
                                                } catch (Throwable ex) {
                                                    Log.w(ex);
                                                }
                                            }
                                        });
                                    } else if (has != 0) {
                                        cd.setCloseIcon(ContextCompat.getDrawable(context, icons[has - 1]));
                                        cd.setCloseIconVisible(true);
                                    }

                                    cd.setMaxWidth(getWidth());
                                    cd.setBounds(0, 0, cd.getIntrinsicWidth(), cd.getIntrinsicHeight());

                                    edit.setSpan(is, start, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    if (kar == ',' &&
                                            (i + 1 == edit.length() || edit.charAt(i + 1) != ' '))
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

                if (tbd.size() > 0 || added) {
                    setText(edit);
                    setSelection(selStart, selEnd);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    };

    private static boolean overlap(int start, int end, int selStart, int selEnd) {
        return Math.max(start, selStart) <= Math.min(end, selEnd);
    }

    private boolean dropClip(DragState state, float x, float y) {
        try {
            Editable edit = getText();
            Layout layout = getLayout();
            if (edit == null || layout == null)
                return true;

            int target = getDropOffset(layout, x, y);
            if (target < 0)
                return true;

            target = resolveDropBoundary(edit, target, state.span);

            int sourceStart = state.start;
            int sourceEnd = state.tokenEnd;
            if (this == state.source && target >= sourceStart && target <= sourceEnd)
                return true;

            Editable source = state.source.getText();
            source.removeSpan(state.span);
            source.delete(sourceStart, sourceEnd);
            if (this != state.source) {
                state.source.invalidate();
                state.source.post(state.source.update);
            }
            if (this == state.source && target > sourceEnd)
                target -= sourceEnd - sourceStart;

            target = Math.max(0, Math.min(target, edit.length()));
            target = resolveDropBoundary(edit, target, null);

            String text = state.text;
            int insertLength = text.length();
            int spanLength = state.end - state.start;
            edit.insert(target, text);
            edit.setSpan(state.span, target, target + spanLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            setSelection(target + insertLength);
            invalidate();
            post(update);

            return true;
        } catch (Throwable ex) {
            Log.e(ex);
            return true;
        }
    }

    private static int getTokenEnd(Editable edit, int end) {
        int tokenEnd = end;
        while (tokenEnd < edit.length() && edit.charAt(tokenEnd) == ' ')
            tokenEnd++;
        return tokenEnd;
    }

    private int getDropOffset(Layout layout, float x, float y) {
        int line = layout.getLineForVertical((int) (y - getTotalPaddingTop() + getScrollY()));
        line = Math.max(0, Math.min(line, layout.getLineCount() - 1));
        float horizontal = x - getTotalPaddingLeft() + getScrollX();
        horizontal = Math.max(layout.getLineLeft(line), Math.min(horizontal, layout.getLineRight(line)));
        return layout.getOffsetForHorizontal(line, horizontal);
    }

    private int resolveDropBoundary(Editable edit, int target, @Nullable ClipImageSpan ignore) {
        target = Math.max(0, Math.min(target, edit.length()));
        ClipImageSpan[] spans = edit.getSpans(0, edit.length(), ClipImageSpan.class);
        for (ClipImageSpan span : spans) {
            if (span == ignore)
                continue;

            int start = edit.getSpanStart(span);
            int end = edit.getSpanEnd(span);
            if (start < 0 || end <= start)
                continue;

            int tokenEnd = getTokenEnd(edit, end);
            if (target > start && target < tokenEnd) {
                int middle = start + (tokenEnd - start) / 2;
                return (target < middle ? start : tokenEnd);
            }

            if (target == start)
                return start;

            if (target == end || target == tokenEnd)
                return tokenEnd;
        }

        return target;
    }

    private static class ClipImageSpan extends ImageSpan {
        private boolean update;

        public ClipImageSpan(@NonNull Drawable drawable) {
            super(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
        }

        void invalidate() {
            update = true;
        }

        boolean needsUpdate() {
            return update;
        }
    }

    private static class DragState {
        final EditTextMultiAutoComplete source;
        final ClipImageSpan span;
        final int start;
        final int end;
        final int tokenEnd;
        final String text;

        DragState(EditTextMultiAutoComplete source, ClipImageSpan span, int start, int end, int tokenEnd, String text) {
            this.source = source;
            this.span = span;
            this.start = start;
            this.end = end;
            this.tokenEnd = tokenEnd;
            this.text = text;
        }
    }

    private static class ChipDragShadowBuilder extends View.DragShadowBuilder {
        private final Drawable drawable;

        ChipDragShadowBuilder(ClipImageSpan span) {
            super();
            drawable = span.getDrawable();
            if (drawable != null)
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            int width = Math.max(1, drawable.getBounds().width());
            int height = Math.max(1, drawable.getBounds().height());
            outShadowSize.set(width, height);
            outShadowTouchPoint.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            if (drawable != null)
                drawable.draw(canvas);
        }
    }
}