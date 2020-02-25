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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;

public class ActivitySignature extends ActivityBase {
    private EditTextCompose etText;
    private BottomNavigationView style_bar;
    private BottomNavigationView bottom_navigation;

    private boolean raw = false;
    private boolean dirty = false;

    private static final int REQUEST_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            raw = savedInstanceState.getBoolean("fair:raw");

        getSupportActionBar().setSubtitle(getString(R.string.title_edit_signature));
        setContentView(R.layout.activity_signature);

        etText = findViewById(R.id.etText);
        style_bar = findViewById(R.id.style_bar);
        bottom_navigation = findViewById(R.id.bottom_navigation);

        etText.setSelectionListener(new EditTextCompose.ISelection() {
            @Override
            public void onSelected(boolean selection) {
                style_bar.setVisibility(selection ? View.VISIBLE : View.GONE);
            }
        });

        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dirty = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
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
                switch (item.getItemId()) {
                    case R.id.action_insert_image:
                        insertImage();
                        return true;
                    case R.id.action_delete:
                        delete();
                        return true;
                    case R.id.action_save:
                        save();
                        return true;
                    default:
                        return false;
                }
            }
        });

        style_bar.setVisibility(View.GONE);

        setResult(RESULT_CANCELED, new Intent());

        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:raw", raw);
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
        switch (item.getItemId()) {
            case R.id.menu_edit_html:
                item.setChecked(!item.isChecked());
                html(item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void load() {
        String html = getIntent().getStringExtra("html");
        if (html == null)
            etText.setText(null);
        else if (raw)
            etText.setText(html);
        else
            etText.setText(HtmlHelper.fromHtml(html, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    return getDrawableByUri(ActivitySignature.this, Uri.parse(source));
                }
            }, null));
        dirty = false;
    }

    private void delete() {
        Intent result = new Intent();
        result.putExtra("html", (String) null);
        setResult(RESULT_OK, result);
        finish();
    }

    private void save() {
        etText.clearComposingText();
        String html = (raw ? etText.getText().toString() : HtmlHelper.toHtml(etText.getText()));
        Intent result = new Intent();
        result.putExtra("html", html);
        setResult(RESULT_OK, result);
        finish();
    }

    private void html(boolean raw) {
        this.raw = raw;

        if (!raw || dirty) {
            String html = (raw ? HtmlHelper.toHtml(etText.getText()) : etText.getText().toString());
            getIntent().putExtra("html", html);
        }

        load();
    }

    private void insertImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        Helper.openAdvanced(intent);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private boolean onActionStyle(int action) {
        Log.i("Style action=" + action);

        if (action == R.id.menu_link) {
            Uri uri = null;

            ClipboardManager cbm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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
                            StyleHelper.apply(R.id.menu_link, etText, link);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

            return true;
        } else
            return StyleHelper.apply(action, etText);
    }

    private void onImageSelected(Uri uri) {
        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        int start = etText.getSelectionStart();
        SpannableStringBuilder ssb = new SpannableStringBuilder(etText.getText());
        ssb.insert(start, " ");
        ImageSpan is = new ImageSpan(getDrawableByUri(this, uri), uri.toString(), ImageSpan.ALIGN_BASELINE);
        ssb.setSpan(is, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        etText.setText(ssb);
        etText.setSelection(start + 1);
    }

    static Drawable getDrawableByUri(Context context, Uri uri) {
        if ("content".equals(uri.getScheme())) {
            Drawable d;
            try {
                Log.i("Loading image source=" + uri);
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                d = Drawable.createFromStream(inputStream, uri.toString());
            } catch (Throwable ex) {
                // FileNotFound, Security
                Log.w(ex);
                d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24);
            }

            int w = Helper.dp2pixels(context, d.getIntrinsicWidth());
            int h = Helper.dp2pixels(context, d.getIntrinsicHeight());

            d.setBounds(0, 0, w, h);
            return d;
        } else {
            Drawable d = context.getResources().getDrawable(R.drawable.baseline_image_24);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
    }
}
