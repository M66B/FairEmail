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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ActivityCode extends ActivityBase {
    private WebView wvCode;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private boolean lines = false;
    private boolean links = false;
    private boolean sanitize = false;

    private static final int REQUEST_SAVE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lines = savedInstanceState.getBoolean("fair:lines");
            links = savedInstanceState.getBoolean("fair:links");
            sanitize = savedInstanceState.getBoolean("fair:sanitize");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAndRemoveTask();
            }
        });

        View view = LayoutInflater.from(this).inflate(R.layout.activity_code, null);
        setContentView(view);

        wvCode = findViewById(R.id.wvCode);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        WebSettings settings = wvCode.getSettings();

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        settings.setAllowFileAccess(false);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        settings.setLoadsImagesAutomatically(false);
        settings.setBlockNetworkLoads(true);
        settings.setBlockNetworkImage(true);
        settings.setJavaScriptEnabled(true);

        wvCode.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Bundle args = new Bundle();
                args.putParcelable("uri", Uri.parse(url));
                args.putString("title", null);
                args.putBoolean("always_confirm", true);

                FragmentDialogOpenLink fragment = new FragmentDialogOpenLink();
                fragment.setArguments(args);
                fragment.show(getSupportFragmentManager(), "open:link");

                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.w("View code error " + errorCode + ":" + description);
            }
        });

        // Initialize
        grpReady.setVisibility(View.GONE);

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
        outState.putBoolean("fair:lines", lines);
        outState.putBoolean("fair:links", links);
        outState.putBoolean("fair:sanitize", sanitize);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_code, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean debug = prefs.getBoolean("debug", false);

        menu.findItem(R.id.menu_sanitize)
                .setVisible(BuildConfig.DEBUG || debug)
                .setChecked(sanitize)
                .setIcon(sanitize
                        ? R.drawable.twotone_fullscreen_24
                        : R.drawable.twotone_fullscreen_exit_24)
                .setTitle(getString(sanitize
                        ? R.string.title_legend_show_full
                        : R.string.title_legend_show_reformatted));

        menu.findItem(R.id.menu_lines).setChecked(lines);
        menu.findItem(R.id.menu_links).setChecked(links);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_sanitize) {
            sanitize = !sanitize;
            invalidateOptionsMenu();
            load();
            return true;
        } else if (itemId == android.R.id.home) {
            finishAndRemoveTask();
            return true;
        } else if (itemId == R.id.menu_lines) {
            lines = !lines;
            item.setChecked(lines);
            load();
            return true;
        } else if (itemId == R.id.menu_links) {
            links = !links;
            item.setChecked(links);
            load();
            return true;
        } else if (itemId == R.id.menu_save) {
            selectFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_SAVE:
                    if (resultCode == RESULT_OK)
                        save(data);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void load() {
        Intent intent = getIntent();
        long id = intent.getLongExtra("id", -1L);
        CharSequence selected = intent.getCharSequenceExtra("selected");
        Log.i("Show code message=" + id + " selected=" + (selected != null) +
                " lines=" + lines + " links=" + links + " sanitize=" + sanitize);

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putCharSequence("selected", selected);
        args.putBoolean("sanitize", sanitize);

        new SimpleTask<String>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                CharSequence selected = args.getCharSequence("selected");
                boolean sanitize = args.getBoolean("sanitize");

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                args.putString("subject", message.subject);

                Document d;
                if (selected == null) {
                    File file = message.getFile(context);
                    d = JsoupEx.parse(file);
                } else {
                    String html = HtmlHelper.toHtml((Spanned) selected, context);
                    d = JsoupEx.parse(html);
                }

                if (sanitize) {
                    List<CSSStyleSheet> sheets =
                            HtmlHelper.parseStyles(d.head().select("style"));
                    for (Element element : d.select("*")) {
                        String computed = HtmlHelper.processStyles(context,
                                element.tagName(),
                                element.className(),
                                element.attr("style"),
                                sheets);
                        if (!TextUtils.isEmpty(computed))
                            element.attr("x-computed", computed);
                    }

                    d = HtmlHelper.sanitizeView(context, d, false);
                }

                d.outputSettings()
                        .prettyPrint(true)
                        .outline(true)
                        .indentAmount(1);

                if (selected == null)
                    return d.html();
                else
                    return d.body().html();
            }

            @Override
            protected void onExecuted(Bundle args, String code) {
                getSupportActionBar().setSubtitle(args.getString("subject"));

                String clazz = "language-html";
                if (lines)
                    clazz += " line-numbers";

                String html = "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "  <meta charset=\"utf-8\" />" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=0.5\" />" +
                        "  <link href=\"file:///android_asset/prism.css\" rel=\"stylesheet\" />" +
                        (links ? "  <link href=\"file:///android_asset/prism-autolinker.min.css\" rel=\"stylesheet\" />" : "") +
                        "  <style>" +
                        "    body { margin: 0 !important; font-size: smaller !important; }" +
                        "    pre { margin-top: 0 !important; margin-bottom: 0 !important }" +
                        "  </style>" +
                        "</head>" +
                        "<body>" +
                        "  <script src=\"file:///android_asset/prism.js\"></script>" +
                        (links ? "  <script src=\"file:///android_asset/prism-autolinker.min.js\"></script>" : "") +
                        "  <pre><code class=\"" + clazz + "\">" + Html.escapeHtml(code) + "</code></pre>" +
                        "</body>" +
                        "</html>";

                wvCode.loadDataWithBaseURL("file:///android_asset/", html, "text/html", StandardCharsets.UTF_8.name(), null);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex, false);
            }
        }.execute(this, args, "code:view");
    }

    private void selectFile() {
        long id = getIntent().getLongExtra("id", -1L);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, Long.toString(id) + ".html");
        Helper.openAdvanced(this, intent);
        startActivityForResult(intent, REQUEST_SAVE);
    }

    private void save(Intent data) {
        long id = getIntent().getLongExtra("id", -1L);

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(ActivityCode.this, R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Export uri=" + uri);
                    throw new IllegalArgumentException(uri.getScheme());
                }

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                File file = message.getFile(context);

                ContentResolver resolver = context.getContentResolver();
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    try (InputStream is = new FileInputStream(file)) {
                        Helper.copy(is, os);
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(ActivityCode.this, R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex, false);
            }
        }.execute(this, args, "code:save");
    }
}
