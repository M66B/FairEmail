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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.File;
import java.util.List;

public class ActivityHTML extends ActivityBase {
    private TextView tvText;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private boolean sanitize = BuildConfig.DEBUG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            sanitize = savedInstanceState.getBoolean("fair:sanitize");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_text, null);
        setContentView(view);

        tvText = findViewById(R.id.tvText);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

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
        outState.putBoolean("fair:sanitize", sanitize);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_html, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean debug = prefs.getBoolean("debug", false);

        menu.findItem(R.id.menu_sanitize)
                .setVisible(BuildConfig.DEBUG || debug)
                .setChecked(sanitize);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_sanitize) {
            sanitize = !sanitize;
            item.setChecked(sanitize);
            load();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void load() {
        Intent intent = getIntent();
        long id = intent.getLongExtra("id", -1L);
        Log.i("Text id=" + id + " sanitize=" + sanitize);

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
                if (sanitize) {
                    Document d = JsoupEx.parse(file);

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

                    return d.html();
                } else
                    return Helper.readText(file);
            }

            @Override
            protected void onExecuted(Bundle args, String text) {
                getSupportActionBar().setSubtitle(args.getString("subject"));

                tvText.setText(text);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex, false);
            }
        }.execute(this, args, "view:text");
    }
}
