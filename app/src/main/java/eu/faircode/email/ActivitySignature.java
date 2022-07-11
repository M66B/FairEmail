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

import android.content.ClipboardManager;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class ActivitySignature extends ActivityBase {
    private ViewGroup view;
    private TextView tvHtmlRemark;
    private EditTextCompose etText;
    private ImageButton ibFull;
    private BottomNavigationView style_bar;
    private BottomNavigationView bottom_navigation;

    private boolean loaded = false;
    private boolean dirty = false;

    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean monospaced = prefs.getBoolean("monospaced", false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.title_edit_signature));

        LayoutInflater inflater = LayoutInflater.from(this);
        view = (ViewGroup) inflater.inflate(R.layout.activity_signature, null, false);
        setContentView(view);

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

        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (loaded)
                    dirty = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

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

        style_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onActionStyle(item.getItemId());
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_insert_image) {
                    insertImage();
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
        FragmentDialogTheme.setBackground(this, view, true);
        tvHtmlRemark.setVisibility(View.GONE);
        style_bar.setVisibility(View.GONE);

        setResult(RESULT_CANCELED, new Intent());

        if (savedInstanceState == null) {
            load(getIntent().getStringExtra("html"));
            dirty = false;
        } else
            dirty = savedInstanceState.getBoolean("fair:dirty");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        load(getIntent().getStringExtra("html"));
        dirty = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:dirty", dirty);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_signature, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_help) {
            onMenuHelp();
            return true;
        } else if (itemId == R.id.menu_edit_html) {
            item.setChecked(!item.isChecked());
            html(item.isChecked());
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

    private void onMenuSelectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("text/*");
        Helper.openAdvanced(intent);
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
        else
            etText.setText(HtmlHelper.fromHtml(html, new HtmlHelper.ImageGetterEx() {
                @Override
                public Drawable getDrawable(Element element) {
                    String source = element.attr("src");
                    if (source.startsWith("cid:"))
                        element.attr("src", "cid:");
                    return ImageHelper.decodeImage(ActivitySignature.this,
                            -1, element, true, 0, 1.0f, etText);
                }
            }, null, this));
        loaded = true;
    }

    private void delete() {
        Intent result = getIntent();
        if (result == null)
            result = new Intent();
        result.putExtra("html", (String) null);
        setResult(RESULT_OK, result);
        finish();
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
        etText.clearComposingText();

        if (etText.isRaw())
            return etText.getText().toString();
        else {
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
        Helper.openAdvanced(intent);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private boolean onActionStyle(int action) {
        Log.i("Style action=" + action);

        if (action == R.id.menu_link) {
            Uri uri = null;
            final int start = etText.getSelectionStart();
            final int end = etText.getSelectionEnd();

            ClipboardManager cbm = Helper.getSystemService(this, ClipboardManager.class);
            if (cbm != null && cbm.hasPrimaryClip()) {
                String link = cbm.getPrimaryClip().getItemAt(0).coerceToText(this).toString();
                uri = Uri.parse(link);
                if (uri.getScheme() == null)
                    uri = null;
            }

            View view = LayoutInflater.from(this).inflate(R.layout.dialog_insert_link, null);
            EditText etLink = view.findViewById(R.id.etLink);
            TextView tvInsecure = view.findViewById(R.id.tvInsecure);

            etLink.setText(uri == null ? "https://" : uri.toString());
            tvInsecure.setVisibility(View.GONE);

            new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String link = etLink.getText().toString();
                            etText.setSelection(start, end);
                            StyleHelper.apply(R.id.menu_link, ActivitySignature.this, null, etText, link);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

            return true;
        } else
            return StyleHelper.apply(action, ActivitySignature.this, findViewById(action), etText);
    }

    private void onImageSelected(Uri uri) {
        try {
            NoStreamException.check(uri, this);

            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            int start = etText.getSelectionStart();
            if (etText.isRaw())
                etText.getText().insert(start, "<img src=\"" + Html.escapeHtml(uri.toString()) + "\" />");
            else {
                SpannableStringBuilder ssb = new SpannableStringBuilderEx(etText.getText());
                ssb.insert(start, " \uFFFC"); // Object replacement character
                String source = uri.toString();
                Drawable d = ImageHelper.decodeImage(this, -1, source, true, 0, 1.0f, etText);
                ImageSpan is = new ImageSpan(d, source);
                ssb.setSpan(is, start + 1, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                etText.setText(ssb);
                etText.setSelection(start + 2);

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
}
