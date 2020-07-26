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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class ActivitySignature extends ActivityBase {
    private ViewGroup view;
    private EditTextCompose etText;
    private BottomNavigationView style_bar;
    private BottomNavigationView bottom_navigation;

    private boolean dirty = false;

    private static final int REQUEST_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            etText.setRaw(savedInstanceState.getBoolean("fair:raw"));

        getSupportActionBar().setSubtitle(getString(R.string.title_edit_signature));

        LayoutInflater inflater = LayoutInflater.from(this);
        view = (ViewGroup) inflater.inflate(R.layout.activity_signature, null, false);
        setContentView(view);

        etText = findViewById(R.id.etText);
        style_bar = findViewById(R.id.style_bar);
        bottom_navigation = findViewById(R.id.bottom_navigation);

        etText.setSelectionListener(new EditTextCompose.ISelection() {
            @Override
            public void onSelected(boolean selection) {
                style_bar.setVisibility(selection && !etText.getRaw() ? View.VISIBLE : View.GONE);
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
        outState.putBoolean("fair:raw", etText.getRaw());
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
        else if (etText.getRaw())
            etText.setText(html);
        else
            etText.setText(HtmlHelper.fromHtml(html, false, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    return ImageHelper.decodeImage(ActivitySignature.this, -1, source, true, 0, etText);
                }
            }, null, this));
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
        String html = (etText.getRaw()
                ? etText.getText().toString()
                : HtmlHelper.toHtml(etText.getText(), this));
        Intent result = new Intent();
        result.putExtra("html", html);
        setResult(RESULT_OK, result);
        finish();
    }

    private void html(boolean raw) {
        etText.setRaw(raw);

        if (!raw || dirty) {
            String html = (raw
                    ? HtmlHelper.toHtml(etText.getText(), this)
                    : etText.getText().toString());
            getIntent().putExtra("html", html);
        }

        if (raw)
            style_bar.setVisibility(View.GONE);

        load();
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
                            etText.setSelection(start, end);
                            StyleHelper.apply(R.id.menu_link, null, etText, link);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

            return true;
        } else
            return StyleHelper.apply(action, findViewById(action), etText);
    }

    private void onImageSelected(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            int start = etText.getSelectionStart();
            if (etText.getRaw())
                etText.getText().insert(start, "<img src=\"" + Html.escapeHtml(uri.toString()) + "\" />");
            else {
                SpannableStringBuilder ssb = new SpannableStringBuilder(etText.getText());
                ssb.insert(start, " \uFFFC"); // Object replacement character
                String source = uri.toString();
                Drawable d = ImageHelper.decodeImage(this, -1, source, true, 0, etText);
                ImageSpan is = new ImageSpan(d, source);
                ssb.setSpan(is, start + 1, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                etText.setText(ssb);
                etText.setSelection(start + 2);
            }
        } catch (SecurityException ex) {
            Snackbar sb = Snackbar.make(view, R.string.title_no_stream, Snackbar.LENGTH_INDEFINITE)
                    .setGestureInsetBottomIgnored(true);
            sb.setAction(R.string.title_info, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(ActivitySignature.this, 49);
                }
            });
            sb.show();
        } catch (Throwable ex) {
            Log.unexpectedError(getSupportFragmentManager(), ex);
        }
    }
}
