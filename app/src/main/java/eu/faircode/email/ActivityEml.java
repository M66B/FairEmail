package eu.faircode.email;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import androidx.constraintlayout.widget.Group;

public class ActivityEml extends ActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setSubtitle("EML");
        setContentView(R.layout.activity_eml);

        final TextView tvEml = findViewById(R.id.tvEml);
        final TextView tvBody = findViewById(R.id.tvBody);
        final ContentLoadingProgressBar pbWait = findViewById(R.id.pbWait);
        final Group grpEml = findViewById(R.id.grpEml);
        tvEml.setMovementMethod(new ScrollingMovementMethod());
        tvBody.setMovementMethod(new ScrollingMovementMethod());
        grpEml.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        Log.logExtras(getIntent());
        Log.i("EML uri=" + getIntent().getData());

        Bundle args = new Bundle();
        args.putParcelable("uri", getIntent().getData());

        new SimpleTask<String[]>() {
            @Override
            protected String[] onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                InputStream is = null;
                try {
                    ContentResolver resolver = context.getContentResolver();
                    AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                    is = new BufferedInputStream(descriptor.createInputStream());

                    Properties props = MessageHelper.getSessionProperties(Helper.AUTH_TYPE_PASSWORD, null, false);
                    Session isession = Session.getInstance(props, null);
                    MimeMessage mmessage = new MimeMessage(isession, is);

                    String body = null;
                    try {
                        MessageHelper helper = new MessageHelper(mmessage);
                        MessageHelper.MessageParts parts = helper.getMessageParts();
                        body = parts.getHtml(context);
                    } catch (Throwable ex) {
                        body = ex.toString();
                    }

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mmessage.writeTo(bos);
                    String eml = new String(bos.toByteArray());

                    return new String[]{eml, body};
                } finally {
                    if (is != null)
                        is.close();
                }
            }

            @Override
            protected void onExecuted(Bundle args, String[] data) {
                tvEml.setText(data[0]);
                tvBody.setText(Html.fromHtml(data[1]));
                grpEml.setVisibility(View.VISIBLE);
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(ActivityEml.this, ActivityEml.this, ex);
            }
        }.execute(this, args, "eml");
    }
}
