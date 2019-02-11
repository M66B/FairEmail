package eu.faircode.email;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

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

        final TextView tvTo = findViewById(R.id.tvTo);
        final TextView tvFrom = findViewById(R.id.tvFrom);
        final TextView tvReplyTo = findViewById(R.id.tvReplyTo);
        final TextView tvCc = findViewById(R.id.tvCc);
        final TextView tvBcc = findViewById(R.id.tvBcc);
        final TextView tvSubject = findViewById(R.id.tvSubject);
        final TextView tvParts = findViewById(R.id.tvParts);
        final TextView tvBody = findViewById(R.id.tvBody);
        final TextView tvHtml = findViewById(R.id.tvHtml);
        final TextView tvEml = findViewById(R.id.tvEml);
        final ContentLoadingProgressBar pbWait = findViewById(R.id.pbWait);
        final Group grpEml = findViewById(R.id.grpEml);

        grpEml.setVisibility(View.GONE);

        Uri uri = getIntent().getData();
        if (uri == null) {
            pbWait.setVisibility(View.GONE);
            return;
        } else
            pbWait.setVisibility(View.VISIBLE);

        Log.i("EML uri=" + uri);

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

                InputStream is = null;
                try {
                    ContentResolver resolver = context.getContentResolver();
                    AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                    is = new BufferedInputStream(descriptor.createInputStream());

                    Properties props = MessageHelper.getSessionProperties(Helper.AUTH_TYPE_PASSWORD, null, false);
                    Session isession = Session.getInstance(props, null);
                    MimeMessage mmessage = new MimeMessage(isession, is);

                    MessageHelper helper = new MessageHelper(mmessage);

                    result.from = MessageHelper.formatAddresses(helper.getFrom());
                    result.to = MessageHelper.formatAddresses(helper.getTo());
                    result.replyto = MessageHelper.formatAddresses(helper.getReply());
                    result.cc = MessageHelper.formatAddresses(helper.getCc());
                    result.bcc = MessageHelper.formatAddresses(helper.getBcc());
                    result.subject = helper.getSubject();

                    MessageHelper.MessageParts parts = helper.getMessageParts();

                    StringBuilder sb = new StringBuilder();
                    for (MessageHelper.AttachmentPart apart : parts.getAttachmentParts()) {
                        if (sb.length() > 0)
                            sb.append("<br />");
                        sb.append(
                                apart.part.getContentType()).append(' ')
                                .append(apart.disposition).append(' ')
                                .append(apart.filename);
                    }
                    result.parts = HtmlHelper.fromHtml(sb.toString());

                    result.html = HtmlHelper.sanitize(parts.getHtml(context), true);
                    result.body = HtmlHelper.fromHtml(result.html);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mmessage.writeTo(bos);
                    result.eml = new String(bos.toByteArray());

                    return result;
                } finally {
                    if (is != null)
                        is.close();
                }
            }

            @Override
            protected void onExecuted(Bundle args, Result result) {
                tvFrom.setText(result.from);
                tvTo.setText(result.to);
                tvReplyTo.setText(result.replyto);
                tvCc.setText(result.cc);
                tvBcc.setText(result.bcc);
                tvSubject.setText(result.subject);
                tvParts.setText(result.parts);
                tvBody.setText(result.body);
                tvHtml.setText(result.html);
                tvEml.setText(result.eml);
                grpEml.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(ActivityEml.this, ActivityEml.this, ex);
            }
        }.execute(this, args, "eml");
    }

    private class Result {
        String from;
        String to;
        String replyto;
        String cc;
        String bcc;
        String subject;
        Spanned parts;
        Spanned body;
        String html;
        String eml;
    }
}
