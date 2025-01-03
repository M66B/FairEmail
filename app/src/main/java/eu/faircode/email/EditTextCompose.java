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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.QuoteSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.preference.PreferenceManager;

import org.github.DetectHtml;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EditTextCompose extends FixedEditText {
    private boolean raw = false;
    private ISelection selectionListener = null;
    private IInputContentListener inputContentListener = null;

    private Boolean canUndo = null;
    private Boolean canRedo = null;
    private List<EntityAnswer> snippets;

    private int colorPrimary;
    private int colorBlockquote;
    private int quoteGap;
    private int quoteStripe;
    private boolean lt_description;
    private boolean undo_manager;
    private boolean paste_plain;
    private boolean paste_quote;

    public EditTextCompose(Context context) {
        super(context);
        init(context);
    }

    public EditTextCompose(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditTextCompose(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        Helper.setKeyboardIncognitoMode(this, context);

        this.colorPrimary = Helper.resolveColor(context, androidx.appcompat.R.attr.colorPrimary);
        this.colorBlockquote = Helper.resolveColor(context, R.attr.colorBlockquote, colorPrimary);
        this.quoteGap = context.getResources().getDimensionPixelSize(R.dimen.quote_gap_size);
        this.quoteStripe = context.getResources().getDimensionPixelSize(R.dimen.quote_stripe_width);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.lt_description = prefs.getBoolean("lt_description", false);
        this.undo_manager = prefs.getBoolean("undo_manager", false);
        this.paste_plain = prefs.getBoolean("paste_plain", false);
        this.paste_quote = prefs.getBoolean("paste_quote", false);

        addTextChangedListener(new TextWatcher() {
            private Integer replace;
            private Integer length;
            private String what;
            private boolean replacing = false;

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                try {
                    int index = start + before;
                    if (count - before == 1 && index > 1) {
                        char c = text.charAt(index);
                        if (c == '>' &&
                                text.charAt(index - 1) == '-' &&
                                text.charAt(index - 2) == '-') {
                            replace = index - 2;
                            length = 3;
                            what = "→";
                        } else if (c == '-' &&
                                text.charAt(index - 1) == '-' &&
                                text.charAt(index - 2) == '<') {
                            replace = index - 2;
                            length = 3;
                            what = "←";
                        }
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (!replacing && replace != null)
                    try {
                        replacing = true;
                        text.replace(replace, replace + length, what);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        replace = null;
                        replacing = false;
                    }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    try {
                        int order = 1000;
                        menu.add(Menu.CATEGORY_SECONDARY, R.string.title_insert_brackets,
                                order++, "(" + context.getString(R.string.title_insert_brackets) + ")");
                        menu.add(Menu.CATEGORY_SECONDARY, R.string.title_insert_quotes,
                                order++, "\"" + context.getString(R.string.title_insert_quotes) + "\"");
                        menu.add(Menu.CATEGORY_SECONDARY, R.string.title_lt_add,
                                order++, context.getString(R.string.title_lt_add));
                        menu.add(Menu.CATEGORY_SECONDARY, R.string.title_lt_delete,
                                order++, context.getString(R.string.title_lt_delete));
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    int start = getSelectionStart();
                    int end = getSelectionEnd();
                    boolean selection = (start >= 0 && start < end);
                    Context context = getContext();
                    Editable edit = getText();
                    boolean dictionary = (selection &&
                            context instanceof AppCompatActivity &&
                            LanguageTool.isPremium(context) &&
                            edit != null &&
                            edit.subSequence(start, end).toString().indexOf(' ') < 0);
                    menu.findItem(R.string.title_insert_brackets).setVisible(selection);
                    menu.findItem(R.string.title_insert_quotes).setVisible(selection);
                    menu.findItem(R.string.title_lt_add).setVisible(dictionary);
                    menu.findItem(R.string.title_lt_delete).setVisible(dictionary);
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getGroupId() == Menu.CATEGORY_SECONDARY) {
                        int id = item.getItemId();
                        if (id == R.string.title_insert_brackets)
                            return surround("(", ")");
                        else if (id == R.string.title_insert_quotes)
                            return surround("\"", "\"");
                        else if (id == R.string.title_lt_add)
                            return modifyDictionary(true);
                        else if (id == R.string.title_lt_delete)
                            return modifyDictionary(false);
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    // Do nothing
                }

                private boolean surround(String before, String after) {
                    Editable edit = getText();
                    int start = getSelectionStart();
                    int end = getSelectionEnd();
                    boolean selection = (edit != null && start >= 0 && start < end);
                    if (selection) {
                        int s = start - before.length();
                        int e = end + after.length();
                        if (s >= 0 && e < length() &&
                                edit.subSequence(s, start).toString().equals(before) &&
                                edit.subSequence(end, e).toString().equals(after)) {
                            edit.delete(end, e);
                            edit.delete(s, start);
                        } else {
                            edit.insert(end, after);
                            edit.insert(start, before);
                        }
                    }
                    return selection;
                }

                private boolean modifyDictionary(boolean add) {
                    int start = getSelectionStart();
                    int end = getSelectionEnd();
                    if (start < 0 || start >= end)
                        return false;

                    final Context context = getContext();
                    if (!(context instanceof AppCompatActivity))
                        return false;
                    AppCompatActivity activity = (AppCompatActivity) getContext();

                    Editable edit = getText();
                    if (edit == null)
                        return false;

                    String word = edit.subSequence(start, end).toString();

                    Bundle args = new Bundle();
                    args.putString("word", word);
                    args.putBoolean("add", add);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                            String word = args.getString("word");
                            boolean add = args.getBoolean("add");
                            LanguageTool.modifyDictionary(context, word, null, add);
                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            setSelection(end);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            // Ignored
                        }
                    }.execute(activity, args, "dictionary:modify");

                    return true;
                }
            });

            setCustomInsertionActionModeCallback(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    try {
                        int order = 1000;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || paste_plain)
                            menu.add(Menu.CATEGORY_SECONDARY, android.R.id.pasteAsPlainText, order++, getTitle(R.string.title_paste_plain));
                        if (paste_quote)
                            menu.add(Menu.CATEGORY_SECONDARY, R.string.title_paste_as_quote, order++, getTitle(R.string.title_paste_as_quote));
                        if (undo_manager && can(android.R.id.undo))
                            menu.add(Menu.CATEGORY_SECONDARY, R.string.title_undo, order++, getTitle(R.string.title_undo));
                        if (undo_manager && can(android.R.id.redo))
                            menu.add(Menu.CATEGORY_SECONDARY, R.string.title_redo, order++, getTitle(R.string.title_redo));
                        menu.add(Menu.CATEGORY_SECONDARY, R.string.title_insert_line, order++, context.getString(R.string.title_insert_line));
                        if (snippets != null)
                            for (EntityAnswer snippet : snippets) {
                                menu.add(Menu.CATEGORY_SECONDARY, order, order, snippet.name).
                                        setIntent(new Intent().putExtra("id", snippet.id));
                                order++;
                            }
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                    return true;
                }

                private CharSequence getTitle(int resid) {
                    SpannableStringBuilder ssb = new SpannableStringBuilderEx(context.getString(resid));
                    ssb.setSpan(new StyleSpan(Typeface.ITALIC), 0, ssb.length(), 0);
                    return ssb;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getGroupId() == Menu.CATEGORY_SECONDARY) {
                        int id = item.getItemId();
                        if (id == android.R.id.pasteAsPlainText)
                            return insertPlain();
                        else if (id == R.string.title_paste_as_quote && paste_quote)
                            return pasteAsQuote();
                        else if (id == R.string.title_undo && undo_manager)
                            return EditTextCompose.super.onTextContextMenuItem(android.R.id.undo);
                        else if (id == R.string.title_redo && undo_manager)
                            return EditTextCompose.super.onTextContextMenuItem(android.R.id.redo);
                        else if (id == R.string.title_insert_line)
                            return insertLine();
                        else {
                            Intent intent = item.getIntent();
                            if (intent == null)
                                return false;
                            return insertSnippet(intent.getLongExtra("id", -1L));
                        }
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    // Do nothing
                }

                private boolean insertPlain() {
                    ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);
                    if (!cbm.hasPrimaryClip())
                        return true;

                    ClipData clip = cbm.getPrimaryClip();
                    if (clip == null || clip.getItemCount() < 1)
                        return true;

                    ClipData.Item item = clip.getItemAt(0);
                    if (item == null)
                        return true;

                    CharSequence text = item.getText();
                    if (TextUtils.isEmpty(text))
                        return true;

                    int start = getSelectionStart();
                    if (start < 0)
                        start = 0;
                    String plain = text.toString();
                    getText().insert(start, plain);

                    StyleHelper.markAsInserted(getText(), start, start + plain.length());

                    return true;
                }

                private boolean pasteAsQuote() {
                    ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);
                    if (!cbm.hasPrimaryClip())
                        return true;

                    ClipData clip = cbm.getPrimaryClip();
                    if (clip == null || clip.getItemCount() < 1)
                        return true;

                    ClipData.Item item = clip.getItemAt(0);
                    if (item == null)
                        return true;

                    String h = item.getHtmlText();
                    if (TextUtils.isEmpty(h)) {
                        CharSequence t = item.getText();
                        if (TextUtils.isEmpty(t))
                            return true;
                        h = "<div>" + HtmlHelper.formatPlainText(t.toString(), false) + "</div>";
                    }
                    String style = HtmlHelper.getQuoteStyle("", 0, 0);
                    String html = "<blockquote style=\"" + style + "\">" + h + "</blockquote>";

                    Helper.getUIExecutor().submit(new RunnableEx("pasteq") {
                        @Override
                        public void delegate() {
                            SpannableStringBuilder ssb = getSpanned(context, html);

                            EditTextCompose.this.post(new RunnableEx("pasteq") {
                                @Override
                                public void delegate() {
                                    int start = getSelectionStart();
                                    if (start < 0)
                                        start = 0;
                                    getText().insert(start, ssb);

                                    StyleHelper.markAsInserted(getText(), start, start + ssb.length());
                                }
                            });
                        }
                    });

                    return true;
                }

                private boolean insertLine() {
                    return StyleHelper.apply(R.id.menu_style_insert_line, null, null, EditTextCompose.this);
                }

                private boolean insertSnippet(long id) {
                    if (snippets == null)
                        return false;

                    InternetAddress[] to = null;
                    try {
                        View root = getRootView();
                        EditText etTo = (root == null ? null : root.findViewById(R.id.etTo));
                        if (etTo != null)
                            to = MessageHelper.parseAddresses(getContext(), etTo.getText().toString());
                    } catch (AddressException ignored) {
                    }

                    for (EntityAnswer snippet : snippets)
                        if (snippet.id.equals(id)) {
                            String html = snippet.getData(context, to).getHtml();

                            Helper.getUIExecutor().submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        SpannableStringBuilder ssb = getSpanned(context, html);
                                        int len = ssb.length();
                                        if (len > 0 && ssb.charAt(len - 1) == '\n')
                                            ssb.replace(len - 1, len, " ");

                                        EditTextCompose.this.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    int start = getSelectionStart();
                                                    if (start < 0)
                                                        start = 0;
                                                    int at = start;

                                                    Editable edit = getText();

                                                    if (start > 0) {
                                                        char kar = edit.charAt(start - 1);
                                                        if (!(kar == '\n' || kar == ' '))
                                                            edit.insert(start++, " ");
                                                    }

                                                    edit.insert(start, ssb);

                                                    setSelection(start + ssb.length());

                                                    StyleHelper.markAsInserted(getText(), at, at + (start - at) + ssb.length());
                                                } catch (Throwable ex) {
                                                    Log.e(ex);
                                                }
                                            }
                                        });
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }
                            });

                            return true;
                        }

                    return false;
                }
            });

            DB db = DB.getInstance(context);
            Helper.getUIExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        snippets = db.answer().getSnippets();
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });
        }
    }

    private boolean can(int what) {
        canUndo = null;
        canRedo = null;

        try {
            int meta = KeyEvent.META_CTRL_ON;
            if (what == android.R.id.redo)
                meta = meta | KeyEvent.META_SHIFT_ON;
            KeyEvent ke = new KeyEvent(0, 0, 0, 0, 0, meta);
            onKeyShortcut(KeyEvent.KEYCODE_Z, ke);
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return Boolean.TRUE.equals(what == android.R.id.redo ? canRedo : canUndo);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, this.raw);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setRaw(savedState.getRaw());
    }

    @Override
    protected void onAttachedToWindow() {
        // Spellchecker workaround
        boolean enabled = isEnabled();
        super.setEnabled(true);
        super.onAttachedToWindow();
        super.setEnabled(enabled);
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public boolean isRaw() {
        return raw;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selectionListener != null)
            selectionListener.onSelected(hasSelection());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (lt_description && event.getAction() == MotionEvent.ACTION_DOWN) {
            Editable edit = getText();
            if (edit != null) {
                int off = Helper.getOffset(this, edit, event);
                SuggestionSpanEx[] suggestions = edit.getSpans(off, off, SuggestionSpanEx.class);
                if (suggestions != null && suggestions.length > 0) {
                    String description = suggestions[0].getDescription();
                    if (!TextUtils.isEmpty(description))
                        ToastEx.makeText(getContext(), description, Toast.LENGTH_LONG).show();
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        try {
            if (id == android.R.id.copy) {
                int start = getSelectionStart();
                int end = getSelectionEnd();
                if (start > end) {
                    int s = start;
                    start = end;
                    end = s;
                }

                Context context = getContext();
                ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);
                if (start != end && cbm != null) {
                    CharSequence selected = getEditableText().subSequence(start, end);
                    if (selected instanceof Spanned) {
                        String html = HtmlHelper.toHtml((Spanned) selected, context);
                        cbm.setPrimaryClip(ClipData.newHtmlText(context.getString(R.string.app_name), selected, html));
                        setSelection(end);
                        return true;
                    }
                }
            } else if (id == android.R.id.paste) {
                final Context context = getContext();

                ClipboardManager cbm = Helper.getSystemService(context, ClipboardManager.class);
                if (cbm == null || !cbm.hasPrimaryClip())
                    return false;

                ClipData.Item item = cbm.getPrimaryClip().getItemAt(0);

                final String html;
                String h = null;
                if (raw || (!BuildConfig.PLAY_STORE_RELEASE && length() == 0)) {
                    CharSequence text = item.getText();
                    if (text != null && DetectHtml.isHtml(text.toString())) {
                        Log.i("Paste: raw HTML");
                        h = text.toString();
                    }
                }
                if (h == null)
                    h = item.getHtmlText();
                if (h == null) {
                    CharSequence text = item.getText();
                    if (text == null)
                        return false;
                    Log.i("Paste: using plain text");
                    html = "<div>" + HtmlHelper.formatPlainText(text.toString(), false) + "</div>";
                } else {
                    Log.i("Paste: using HTML");
                    html = h;
                }

                Helper.getUIExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SpannableStringBuilder ssb = (raw
                                    ? new SpannableStringBuilderEx(html)
                                    : getSpanned(context, html));

                            EditTextCompose.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int start = getSelectionStart();
                                        int end = getSelectionEnd();

                                        if (start < 0)
                                            start = 0;
                                        if (end < 0)
                                            end = 0;

                                        if (start > end) {
                                            int tmp = start;
                                            start = end;
                                            end = tmp;
                                        }

                                        if (start == end)
                                            getText().insert(start, ssb);
                                        else
                                            getText().replace(start, end, ssb);

                                        StyleHelper.markAsInserted(getText(), start, start + ssb.length());
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                        /*
                                            java.lang.RuntimeException: PARAGRAPH span must start at paragraph boundary
                                                    at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:619)
                                                    at android.text.SpannableStringBuilder.change(SpannableStringBuilder.java:391)
                                                    at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:496)
                                                    at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:454)
                                                    at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:33)
                                                    at android.widget.TextView.paste(TextView.java:8891)
                                                    at android.widget.TextView.onTextContextMenuItem(TextView.java:8706)
                                         */
                                    }
                                }
                            });
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });

                return true;
            } else if (id == android.R.id.pasteAsPlainText) {
                int start = getSelectionStart();
                int length = length();
                boolean pasted = super.onTextContextMenuItem(id);
                int end = start + length() - length;
                if (pasted && start >= 0 && end > start)
                    StyleHelper.markAsInserted(getText(), start, end);
                return pasted;
            } else if (id == android.R.id.undo && undo_manager) {
                canUndo = true;
                return true;
            } else if (id == android.R.id.redo && undo_manager) {
                canRedo = true;
                return true;
            }

            return super.onTextContextMenuItem(id);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    private SpannableStringBuilder getSpanned(Context context, String html) {
        Document document = HtmlHelper.sanitizeCompose(context, html, false);
        Spanned paste = HtmlHelper.fromDocument(context, document, new HtmlHelper.ImageGetterEx() {
            @Override
            public Drawable getDrawable(Element element) {
                return ImageHelper.decodeImage(context,
                        -1, element, true, 0, 1.0f, EditTextCompose.this);
            }
        }, null);

        SpannableStringBuilder ssb = new SpannableStringBuilderEx(paste);
        QuoteSpan[] spans = ssb.getSpans(0, ssb.length(), QuoteSpan.class);
        for (QuoteSpan span : spans) {
            QuoteSpan q;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                q = new QuoteSpan(colorBlockquote);
            else
                q = new QuoteSpan(colorBlockquote, quoteStripe, quoteGap);
            ssb.setSpan(q,
                    ssb.getSpanStart(span),
                    ssb.getSpanEnd(span),
                    ssb.getSpanFlags(span));
            ssb.removeSpan(span);
        }

        return ssb;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        //https://developer.android.com/guide/topics/text/image-keyboard
        InputConnection ic = super.onCreateInputConnection(editorInfo);
        if (ic == null)
            return null;

        ic = new InputConnectionWrapper(ic, false) {
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                try {
                    return super.deleteSurroundingText(beforeLength, afterLength);
                } catch (Throwable ex) {
                    Log.w(ex);
                    return true;
                    /*
                        java.lang.IndexOutOfBoundsException: replace (107 ... -2147483542) has end before start
                                at android.text.SpannableStringBuilder.checkRange(SpannableStringBuilder.java:1318)
                                at android.text.SpannableStringBuilder.replace(SpannableStringBuilder.java:513)
                                at androidx.emoji2.text.SpannableBuilder.replace(SourceFile:7)
                                at android.text.SpannableStringBuilder.delete(SpannableStringBuilder.java:230)
                                at androidx.emoji2.text.SpannableBuilder.delete(SourceFile:2)
                                at androidx.emoji2.text.SpannableBuilder.delete(SourceFile:1)
                                at android.view.inputmethod.BaseInputConnection.deleteSurroundingText(BaseInputConnection.java:276)
                                at android.view.inputmethod.InputConnectionWrapper.deleteSurroundingText(InputConnectionWrapper.java:133)
                                at androidx.emoji2.viewsintegration.EmojiInputConnection.deleteSurroundingText(SourceFile:17)
                                at android.view.inputmethod.InputConnectionWrapper.deleteSurroundingText(InputConnectionWrapper.java:133)
                     */
                }
            }

            @Override
            public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
                try {
                    return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
                } catch (Throwable ex) {
                    Log.w(ex);
                    return true;
                }
            }
        };

        EditorInfoCompat.setContentMimeTypes(editorInfo, new String[]{"image/*"});

        return InputConnectionCompat.createWrapper(ic, editorInfo, new InputConnectionCompat.OnCommitContentListener() {
            @Override
            public boolean onCommitContent(InputContentInfoCompat info, int flags, Bundle opts) {
                Log.i("Uri=" + info.getContentUri());
                try {
                    if (inputContentListener == null)
                        throw new IllegalArgumentException("InputContent listener not set");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 &&
                            (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0)
                        info.requestPermission();

                    String type = null;
                    if (info.getDescription().getMimeTypeCount() > 0)
                        type = info.getDescription().getMimeType(0);

                    inputContentListener.onInputContent(info.getContentUri(), type);
                    return true;
                } catch (Throwable ex) {
                    Log.w(ex);
                    return false;
                }
            }
        });
    }

    void setInputContentListener(IInputContentListener listener) {
        this.inputContentListener = listener;
    }

    interface IInputContentListener {
        void onInputContent(Uri uri, String type);
    }

    void setSelectionListener(ISelection listener) {
        this.selectionListener = listener;
    }

    interface ISelection {
        void onSelected(boolean selection);
    }

    static class SavedState extends View.BaseSavedState {
        private boolean raw;

        private SavedState(Parcelable superState, boolean raw) {
            super(superState);
            this.raw = raw;
        }

        private SavedState(Parcel in) {
            super(in);
            raw = (in.readInt() != 0);
        }

        public boolean getRaw() {
            return this.raw;
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(raw ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
