package eu.faircode.email;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import androidx.constraintlayout.widget.Group;

public class ActivityDSN extends ActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setSubtitle("DSN");
        setContentView(R.layout.activity_dsn);

        final TextView tvHeaders = findViewById(R.id.tvHeaders);
        final ContentLoadingProgressBar pbWait = findViewById(R.id.pbWait);
        final Group grpReady = findViewById(R.id.grpReady);

        grpReady.setVisibility(View.GONE);

        Uri uri = getIntent().getData();
        if (uri == null) {
            pbWait.setVisibility(View.GONE);
            return;
        } else
            pbWait.setVisibility(View.VISIBLE);

        Log.i("Disposition uri=" + uri);

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        new SimpleTask<Result>() {
            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Result onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                if ("file".equals(uri.getScheme()))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));

                Result result = new Result();

                ContentResolver resolver = context.getContentResolver();
                AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                try (InputStream is = new BufferedInputStream(descriptor.createInputStream())) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) != -1)
                        bos.write(buffer, 0, length);
                    result.headers = bos.toString("UTF-8");
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
                    Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(ActivityDSN.this, ActivityDSN.this, ex);
            }
        }.execute(this, args, "disposition:decode");
    }

    private class Result {
        String headers;
    }
}
