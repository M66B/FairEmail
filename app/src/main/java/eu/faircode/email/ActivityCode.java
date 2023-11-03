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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ActivityCode extends ActivityBase {
    private WebView wvCode;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private boolean lines = BuildConfig.DEBUG;
    private boolean links = false;
    private boolean sanitize = BuildConfig.DEBUG;

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

        menu.findItem(R.id.menu_lines)
                .setChecked(lines)
                .setIcon(lines
                        ? R.drawable.twotone_speaker_notes_off_24
                        : R.drawable.twotone_speaker_notes_24);

        menu.findItem(R.id.menu_links)
                .setChecked(links)
                .setIcon(links
                        ? R.drawable.twotone_link_off_24
                        : R.drawable.twotone_link_24);

        menu.findItem(R.id.menu_sanitize)
                .setVisible(BuildConfig.DEBUG || debug)
                .setChecked(sanitize)
                .setIcon(sanitize
                        ? R.drawable.twotone_fullscreen_24
                        : R.drawable.twotone_fullscreen_exit_24)
                .setTitle(getString(sanitize
                        ? R.string.title_legend_show_full
                        : R.string.title_legend_show_reformatted));


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finishAndRemoveTask();
            return true;
        } else if (itemId == R.id.menu_lines) {
            lines = !lines;
            invalidateOptionsMenu();
            load();
            return true;
        } else if (itemId == R.id.menu_links) {
            links = !links;
            invalidateOptionsMenu();
            load();
            return true;
        } else if (itemId == R.id.menu_sanitize) {
            sanitize = !sanitize;
            invalidateOptionsMenu();
            load();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void load() {
        Intent intent = getIntent();
        long id = intent.getLongExtra("id", -1L);
        Log.i("Show code message=" + id + " lines=" + lines + " links=" + links + " sanitize=" + sanitize);

        Bundle args = new Bundle();
        args.putLong("id", id);
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
                boolean sanitize = args.getBoolean("sanitize");

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                args.putString("subject", message.subject);

                File file = message.getFile(context);
                Document d = JsoupEx.parse(file);

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
                    d.outputSettings().prettyPrint(true).outline(true).indentAmount(1);
                }

                return d.html();
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
                        "  <link href=\"file:///android_asset/prism.css\" rel=\"stylesheet\" />" +
                        (links ? "  <link href=\"file:///android_asset/prism-autolinker.min.css\" rel=\"stylesheet\" />" : "") +
                        "  <style>" +
                        "    body { font-size: smaller !important; }" +
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
        }.execute(this, args, "view:text");
    }
}
