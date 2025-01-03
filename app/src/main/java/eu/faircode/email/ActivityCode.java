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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.parser.Parser;
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

    private boolean force_light = false;
    private boolean sanitize = false;
    private boolean lines = false;
    private boolean links = false;
    private boolean pretty = true;
    private String searching = null;

    private static final int REQUEST_SAVE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            force_light = savedInstanceState.getBoolean("fair:force_light");
            sanitize = savedInstanceState.getBoolean("fair:sanitize");
            lines = savedInstanceState.getBoolean("fair:lines");
            links = savedInstanceState.getBoolean("fair:links");
            pretty = savedInstanceState.getBoolean("fair:pretty");
            searching = savedInstanceState.getString("fair:searching");
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAndRemoveTask();
            }
        });

        View view = LayoutInflater.from(this).inflate(R.layout.activity_code, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        wvCode = findViewById(R.id.wvCode);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        wvCode.clearCache(true);

        WebSettings settings = wvCode.getSettings();

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        if (WebViewEx.isFeatureSupported(this, WebViewFeature.ATTRIBUTION_REGISTRATION_BEHAVIOR))
            WebSettingsCompat.setAttributionRegistrationBehavior(settings, WebSettingsCompat.ATTRIBUTION_BEHAVIOR_DISABLED);

        settings.setLoadsImagesAutomatically(false);
        settings.setBlockNetworkLoads(true);
        settings.setBlockNetworkImage(true);
        settings.setAllowFileAccess(false);
        settings.setJavaScriptEnabled(true);

        setDarkMode();

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
        outState.putBoolean("fair:force_light", force_light);
        outState.putBoolean("fair:sanitize", sanitize);
        outState.putBoolean("fair:lines", lines);
        outState.putBoolean("fair:links", links);
        outState.putBoolean("fair:pretty", pretty);
        outState.putString("fair:searching", searching);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_code, menu);

        final String saved = searching;
        final MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();

        if (searchView != null)
            searchView.setQueryHint(getString(R.string.title_search));

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    search(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText);
                    return false;
                }

                private void search(String query) {
                    searching = query;
                    if (wvCode != null)
                        wvCode.findAllAsync(query);
                }
            });

            if (!TextUtils.isEmpty(saved)) {
                menuSearch.expandActionView();
                searchView.setQuery(saved, false);
            }
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean debug = prefs.getBoolean("debug", false);

        boolean dark = Helper.isDarkTheme(this);
        boolean canDarken = WebViewEx.isFeatureSupported(this, WebViewFeature.ALGORITHMIC_DARKENING);
        menu.findItem(R.id.menu_force_light)
                .setVisible(dark && canDarken)
                .getIcon().setLevel(force_light ? 1 : 0);

        menu.findItem(R.id.menu_sanitize)
                .setVisible(BuildConfig.DEBUG || debug)
                .setIcon(sanitize
                        ? R.drawable.twotone_fullscreen_24
                        : R.drawable.twotone_fullscreen_exit_24)
                .setTitle(getString(sanitize
                        ? R.string.title_legend_show_full
                        : R.string.title_legend_show_reformatted));

        menu.findItem(R.id.menu_lines).setChecked(lines);
        menu.findItem(R.id.menu_links).setChecked(links);
        menu.findItem(R.id.menu_pretty).setChecked(pretty);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_force_light) {
            force_light = !force_light;
            item.getIcon().setLevel(force_light ? 1 : 0);
            setDarkMode();
            return true;
        } else if (itemId == R.id.menu_sanitize) {
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
        } else if (itemId == R.id.menu_pretty) {
            pretty = !pretty;
            item.setChecked(pretty);
            load();
            return true;
        } else if (itemId == R.id.menu_check_html) {
            checkHtml();
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

    private void setDarkMode() {
        WebSettings settings = wvCode.getSettings();
        boolean dark = (Helper.isDarkTheme(this) && !force_light);
        boolean canDarken = WebViewEx.isFeatureSupported(this, WebViewFeature.ALGORITHMIC_DARKENING);
        if (canDarken)
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, dark);
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
        args.putBoolean("pretty", pretty);

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
                boolean pretty = args.getBoolean("pretty");

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
                        .prettyPrint(pretty)
                        .outline(pretty)
                        .indentAmount(1);

                if (selected == null)
                    return d.html();
                else
                    return d.body().html();
            }

            @Override
            protected void onExecuted(Bundle args, String code) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.setSubtitle(args.getString("subject"));

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

    private void checkHtml() {
        Intent intent = getIntent();
        long id = intent.getLongExtra("id", -1L);
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<ParseErrorList>() {
            @Override
            protected ParseErrorList onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                File file = EntityMessage.getFile(context, id);
                Parser parser = Parser.htmlParser().setTrackErrors(20);
                Jsoup.parse(file, StandardCharsets.UTF_8.name(), "", parser);
                return parser.getErrors();
            }

            @Override
            protected void onExecuted(Bundle args, ParseErrorList errors) {
                lines = true;
                pretty = false;
                load();

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

                new AlertDialog.Builder(ActivityCode.this)
                        .setIcon(R.drawable.twotone_bug_report_24)
                        .setTitle(R.string.title_check_html)
                        .setMessage(ssb)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "code:check");
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
