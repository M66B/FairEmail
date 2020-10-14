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

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import com.google.android.material.snackbar.Snackbar;

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

        getSupportActionBar().setSubtitle("DSN");
        setContentView(R.layout.activity_dsn);

        tvHeaders = findViewById(R.id.tvHeaders);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        grpReady.setVisibility(View.GONE);

        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
    }

    private void load() {
        Uri uri = getIntent().getData();
        Log.i("DSN uri=" + uri);

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        new SimpleTask<Result>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Result onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme()) &&
                        !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.w("DSN uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                Result result = new Result();

                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[Helper.BUFFER_SIZE];
                    int length;
                    while ((length = is.read(buffer)) != -1)
                        bos.write(buffer, 0, length);

                    String headers = MessageHelper.decodeMime(bos.toString(StandardCharsets.UTF_8.name()));
                    result.headers = HtmlHelper.highlightHeaders(context, headers);
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, Result result) {
                tvHeaders.setText(result.headers);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex, false);
            }
        }.execute(this, args, "disposition:decode");
    }

    private class Result {
        Spanned headers;
    }
}
