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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Parser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class ActivitySignature extends ActivityBase {
    private ViewGroup view;
    private TextView tvHtmlRemark;
    private EditTextCompose etText;
    private ImageButton ibFull;
    private HorizontalScrollView style_bar;
    private BottomNavigationView bottom_navigation;

    private boolean loaded = false;
    private boolean dirty = false;
    private String saved = null;

    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_FILE = 2;
    private static final int REQUEST_LINK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean monospaced = prefs.getBoolean("monospaced", false);

        LayoutInflater inflater = LayoutInflater.from(this);
        view = (ViewGroup) inflater.inflate(R.layout.activity_signature, null, false);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.title_edit_signature));

        tvHtmlRemark = findViewById(R.id.tvHtmlRemark);
        etText = findViewById(R.id.etText);
        ibFull = findViewById(R.id.ibFull);
        style_bar = findViewById(R.id.style_bar);
        bottom_navigation = findViewById(R.id.bottom_navigation);

        etText.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);

        etText.setSelectionListener(new EditTextCompose.ISelection() {
            @Override
            public void onSelected(boolean selection) {
                style_bar.setVisibility(selection && !etText.isRaw() ? View.VISIBLE : View.GONE);
            }
        });

        etText.addTextChangedListener(StyleHelper.getTextWatcher(etText));

        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (loaded &&
                        !(start == 0 && before == s.length() && count == s.length())) {
                    dirty = true;
                    saved = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        StyleHelper.wire(this, view, etText);

        ibFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("html", getHtml());
                args.putBoolean("overview_mode", false);
                args.putBoolean("safe_browsing", false);
                args.putBoolean("force_light", true);

                FragmentDialogOpenFull dialog = new FragmentDialogOpenFull();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "signature");
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_insert_image) {
                    insertImage();
                    return true;
                } else if (itemId == R.id.action_insert_link) {
                    insertLink();
                    return true;
                } else if (itemId == R.id.action_delete) {
                    delete();
                    return true;
                } else if (itemId == R.id.action_save) {
                    save();
                    return true;
                }
                return false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Helper.isKeyboardVisible(view)) {
                    Helper.hideKeyboard(view);
                    return;
                }

                String prev = getIntent().getStringExtra("html");
                String current = getHtml();
                boolean dirty = !Objects.equals(prev, current) &&
                        !(TextUtils.isEmpty(prev) && TextUtils.isEmpty(current));

                if (dirty)
                    new AlertDialog.Builder(ActivitySignature.this)
                            .setIcon(R.drawable.twotone_save_alt_24)
                            .setTitle(R.string.title_ask_save)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    save();
                                    performBack();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                else
                    performBack();
            }
        });

        // Initialize
        tvHtmlRemark.setVisibility(View.GONE);
        style_bar.setVisibility(View.GONE);

        setResult(RESULT_CANCELED, new Intent());

        if (savedInstanceState == null) {
            load(getIntent().getStringExtra("html"));
            dirty = false;
        } else {
            dirty = savedInstanceState.getBoolean("fair:dirty");
            saved = savedInstanceState.getString("fair:saved");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        load(getIntent().getStringExtra("html"));
        dirty = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        etText.setTypeface(etText.isRaw() ? Typeface.MONOSPACE : Typeface.DEFAULT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:dirty", dirty);
        outState.putString("fair:saved", saved);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_signature, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_edit_html).setChecked(etText.isRaw());
        menu.findItem(R.id.menu_check_html).setVisible(etText.isRaw());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_help) {
            onMenuHelp();
            return true;
        } else if (itemId == R.id.menu_edit_html) {
            item.setChecked(!item.isChecked());
            html(item.isChecked());
            return true;
        } else if (itemId == R.id.menu_check_html) {
            onMenuCheckHtml();
            return true;
        } else if (itemId == R.id.menu_import_file) {
            onMenuSelectFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuHelp() {
        Helper.viewFAQ(this, 57);
    }

    private void onMenuCheckHtml() {
        Parser parser = Parser.htmlParser().setTrackErrors(20);
        Jsoup.parse(etText.getText().toString(), "", parser);
        ParseErrorList errors = parser.getErrors();
        SpannableStringBuilderEx ssb = new SpannableStringBuilderEx();
        ssb.append("Errors: ")
                .append(Integer.toString(errors.size()))
                .append("\n\n");
        for (ParseError error : errors)
            ssb.append("At ")
                    .append(error.getCursorPos())
                    .append(' ')
                    .append(error.getErrorMessage())
                    .append("\n\n");

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.twotone_bug_report_24)
                .setTitle(R.string.title_check_html)
                .setMessage(ssb)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void onMenuSelectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("text/*");
        Helper.openAdvanced(ActivitySignature.this, intent);
        startActivityForResult(intent, REQUEST_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_IMAGE:
                    if (resultCode == RESULT_OK && data != null)
                        onImageSelected(data.getData());
                    break;
                case REQUEST_FILE:
                    if (resultCode == RESULT_OK && data != null)
                        onFileSelected(data.getData());
                    break;
                case REQUEST_LINK:
                    if (resultCode == RESULT_OK && data != null)
                        onLinkSelected(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void load(String html) {
        loaded = false;
        if (html == null)
            etText.setText(null);
        else if (etText.isRaw())
            etText.setText(html);
        else {
            Document d = HtmlHelper.sanitizeCompose(this, html, true);
            Spanned signature = HtmlHelper.fromDocument(this, d, new HtmlHelper.ImageGetterEx() {
                @Override
                public Drawable getDrawable(Element element) {
                    String source = element.attr("src");
                    if (source.startsWith("cid:"))
                        element.attr("src", "cid:");
                    return ImageHelper.decodeImage(ActivitySignature.this,
                            -1, element, true, 0, 1.0f, etText);
                }
            }, null);
            etText.setText(signature);
        }

        etText.getText().setSpan(new SpanWatcher() {
            @Override
            public void onSpanAdded(Spannable text, Object what, int start, int end) {
                checkChanged(what);
            }

            @Override
            public void onSpanRemoved(Spannable text, Object what, int start, int end) {
                checkChanged(what);
            }

            @Override
            public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
                checkChanged(what);
            }

            private void checkChanged(Object what) {
                for (Class<?> cls : StyleHelper.CLEAR_STYLES)
                    if (cls.isAssignableFrom(what.getClass())) {
                        dirty = true;
                        saved = null;
                    }
            }
        }, 0, etText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        saved = html;
        loaded = true;
    }

    private void delete() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.twotone_gesture_24)
                .setTitle(R.string.title_edit_signature_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent result = getIntent();
                        if (result == null)
                            result = new Intent();
                        result.putExtra("html", (String) null);
                        setResult(RESULT_OK, result);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .show();
    }

    private void save() {
        Intent result = getIntent();
        if (result == null)
            result = new Intent();
        result.putExtra("html", getHtml());
        setResult(RESULT_OK, result);
        finish();
    }

    private void html(boolean raw) {
        String html = (dirty
                ? getHtml()
                : getIntent().getStringExtra("html"));

        tvHtmlRemark.setVisibility(raw ? View.VISIBLE : View.GONE);
        etText.setRaw(raw);
        etText.setTypeface(raw ? Typeface.MONOSPACE : Typeface.DEFAULT);
        load(html);

        if (raw)
            style_bar.setVisibility(View.GONE);
    }

    private String getHtml() {
        HtmlHelper.clearComposingText(etText);

        if (etText.isRaw()) {
            saved = etText.getText().toString();
            return saved;
        } else {
            if (saved != null)
                return saved;
            String html = HtmlHelper.toHtml(etText.getText(), this);
            Document d = JsoupEx.parse(html);
            return d.body().html();
        }
    }

    private void insertImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        Helper.openAdvanced(ActivitySignature.this, intent);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void insertLink() {
        FragmentDialogInsertLink fragment = new FragmentDialogInsertLink();
        fragment.setArguments(FragmentDialogInsertLink.getArguments(etText));
        fragment.setTargetActivity(this, REQUEST_LINK);
        fragment.show(getSupportFragmentManager(), "signature:link");
    }

    private void onImageSelected(Uri uri) {
        try {
            NoStreamException.check(uri, this);

            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (!Helper.isPersisted(this, uri, true, false))
                throw new IllegalStateException("No permission granted to access selected image " + uri);

            int start = etText.getSelectionStart();
            if (etText.isRaw())
                etText.getText().insert(start, "<img src=\"" + Html.escapeHtml(uri.toString()) + "\" />");
            else {
                SpannableStringBuilder ssb = new SpannableStringBuilderEx(etText.getText());
                ssb.insert(start, "\n\uFFFC\n"); // Object replacement character
                String source = uri.toString();
                Drawable d = ImageHelper.decodeImage(this, -1, source, true, 0, 1.0f, etText);
                ImageSpan is = new ImageSpan(d, source);
                ssb.setSpan(is, start + 1, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                etText.setText(ssb);
                etText.setSelection(start + 3);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean signature_images_hint = prefs.getBoolean("signature_images_hint", false);

                if (!signature_images_hint)
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.title_hint_important)
                            .setMessage(R.string.title_edit_signature_image_hint)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().putBoolean("signature_images_hint", true).apply();
                                }
                            })
                            .show();
            }
        } catch (NoStreamException ex) {
            ex.report(this);
        } catch (Throwable ex) {
            Log.unexpectedError(getSupportFragmentManager(), ex);
        }
    }

    private void onFileSelected(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        new SimpleTask<String>() {
            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    if (is == null)
                        throw new FileNotFoundException(uri.toString());
                    return Helper.readStream(is);
                }
            }

            @Override
            protected void onExecuted(Bundle args, String text) {
                int start = etText.getSelectionStart();
                if (start < 0)
                    start = 0;
                etText.getText().insert(start, text);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof NoStreamException)
                    ((NoStreamException) ex).report(ActivitySignature.this);
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "signature:file");
    }

    private void onLinkSelected(Bundle args) {
        String link = args.getString("link");
        boolean image = args.getBoolean("image");
        int start = args.getInt("start");
        int end = args.getInt("end");
        String title = args.getString("title");
        etText.setSelection(start, end);
        StyleHelper.apply(R.id.menu_link, this, null, etText, -1L, 0, link, image, title);
    }
}
