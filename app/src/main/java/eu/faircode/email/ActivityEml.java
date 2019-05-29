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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

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
        final Group grpReady = findViewById(R.id.grpReady);

        grpReady.setVisibility(View.GONE);

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

                if ("file".equals(uri.getScheme()) &&
                        !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.w("EML uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                Result result = new Result();

                ContentResolver resolver = context.getContentResolver();
                AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                try (InputStream is = new BufferedInputStream(descriptor.createInputStream())) {

                    Properties props = MessageHelper.getSessionProperties(
                            ConnectionHelper.AUTH_TYPE_PASSWORD, null, false);
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
                        sb.append(apart.part.getContentType());
                        if (apart.disposition != null)
                            sb.append(' ').append(apart.disposition);
                        if (apart.filename != null)
                            sb.append(' ').append(apart.filename);
                    }
                    result.parts = HtmlHelper.fromHtml(sb.toString());

                    result.html = parts.getHtml(context);
                    if (result.html != null)
                        result.body = HtmlHelper.fromHtml(HtmlHelper.sanitize(context, result.html));

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mmessage.writeTo(bos);
                    result.eml = new String(bos.toByteArray());

                    return result;
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
                tvEml.setText(result.eml.substring(0, Math.min(10 * 1024, result.eml.length()))); // prevent ANR
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(ActivityEml.this, ActivityEml.this, ex);
            }
        }.execute(this, args, "eml:decode");
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
