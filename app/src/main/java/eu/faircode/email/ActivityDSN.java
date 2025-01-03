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
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ActivityDSN extends ActivityBase {
    private TextView tvHeaders;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_dsn, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle("DSN");

        tvHeaders = findViewById(R.id.tvHeaders);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void load() {
        Uri uri = getIntent().getData();
        Log.i("DSN uri=" + uri);

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        new SimpleTask<Spanned>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Spanned onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                NoStreamException.check(uri, context);

                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    if (is == null)
                        throw new FileNotFoundException(uri.toString());

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    Helper.copy(is, bos);

                    String headers = bos.toString(StandardCharsets.UTF_8.name());
                    headers = MessageHelper.decodeMime(headers);
                    return HtmlHelper.highlightHeaders(context,
                            null, null, null, headers, false, false);
                }
            }

            @Override
            protected void onExecuted(Bundle args, Spanned result) {
                tvHeaders.setText(result);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof NoStreamException)
                    ((NoStreamException) ex).report(ActivityDSN.this);
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex, false);
            }
        }.execute(this, args, "disposition:decode");
    }
}
