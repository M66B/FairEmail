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
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.snackbar.Snackbar;
import com.sun.mail.imap.IMAPFolder;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class ActivityEML extends ActivityBase {
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setSubtitle("EML");
        setContentView(R.layout.activity_eml);

        final TextView tvTo = findViewById(R.id.tvTo);
        final TextView tvFrom = findViewById(R.id.tvFrom);
        final TextView tvReplyTo = findViewById(R.id.tvReplyTo);
        final TextView tvCc = findViewById(R.id.tvCc);
        final TextView tvSubject = findViewById(R.id.tvSubject);
        final TextView tvHeaders = findViewById(R.id.tvHeaders);
        final TextView tvParts = findViewById(R.id.tvParts);
        final TextView tvBody = findViewById(R.id.tvBody);
        final TextView tvHtml = findViewById(R.id.tvHtml);
        final ContentLoadingProgressBar pbWait = findViewById(R.id.pbWait);
        final Group grpReady = findViewById(R.id.grpReady);

        grpReady.setVisibility(View.GONE);

        uri = getIntent().getData();
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
                try (InputStream is = descriptor.createInputStream()) {

                    Properties props = MessageHelper.getSessionProperties();
                    Session isession = Session.getInstance(props, null);
                    MimeMessage imessage = new MimeMessage(isession, is);

                    MessageHelper helper = new MessageHelper(imessage);

                    result.from = MessageHelper.formatAddresses(helper.getFrom());
                    result.to = MessageHelper.formatAddresses(helper.getTo());
                    result.replyto = MessageHelper.formatAddresses(helper.getReply());
                    result.cc = MessageHelper.formatAddresses(helper.getCc());
                    result.subject = helper.getSubject();
                    result.headers = HtmlHelper.highlightHeaders(context, helper.getHeaders());

                    MessageHelper.MessageParts parts = helper.getMessageParts();

                    StringBuilder sb = new StringBuilder();
                    for (MessageHelper.AttachmentPart apart : parts.getAttachmentParts()) {
                        if (sb.length() > 0)
                            sb.append("<br>");
                        sb.append(apart.part.getContentType());
                        if (apart.disposition != null)
                            sb.append(' ').append(apart.disposition);
                        if (apart.filename != null)
                            sb.append(' ').append(apart.filename);
                    }
                    result.parts = HtmlHelper.fromHtml(sb.toString());

                    result.html = parts.getHtml(context);
                    if (result.html != null) {
                        result.body = HtmlHelper.fromHtml(HtmlHelper.sanitize(context, result.html, false, false));
                        if (result.html.length() > 100 * 1024)
                            result.html = null;
                    }

                    return result;
                }
            }

            @Override
            protected void onExecuted(Bundle args, Result result) {
                tvFrom.setText(result.from);
                tvTo.setText(result.to);
                tvReplyTo.setText(result.replyto);
                tvCc.setText(result.cc);
                tvSubject.setText(result.subject);
                tvHeaders.setText(result.headers);
                tvParts.setText(result.parts);
                tvBody.setText(result.body);
                tvHtml.setText(result.html);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "eml:decode");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_eml, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                onMenuSave();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuSave() {
        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.account().getSynchronizingAccounts();
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                ArrayAdapter<EntityAccount> adapter =
                        new ArrayAdapter<>(ActivityEML.this, R.layout.spinner_item1, android.R.id.text1);
                adapter.addAll(accounts);

                new AlertDialog.Builder(ActivityEML.this)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EntityAccount account = adapter.getItem(which);

                                Bundle args = new Bundle();
                                args.putParcelable("uri", uri);
                                args.putLong("account", account.id);

                                new SimpleTask<String>() {
                                    @Override
                                    protected void onPreExecute(Bundle args) {
                                        ToastEx.makeText(ActivityEML.this, R.string.title_executing, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    protected String onExecute(Context context, Bundle args) throws Throwable {
                                        Uri uri = args.getParcelable("uri");
                                        long aid = args.getLong("account");

                                        DB db = DB.getInstance(context);
                                        EntityAccount account = db.account().getAccount(aid);
                                        if (account == null)
                                            return null;
                                        EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                                        if (inbox == null)
                                            throw new IllegalArgumentException(context.getString(R.string.title_no_folder));

                                        ContentResolver resolver = context.getContentResolver();
                                        AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                                        try (InputStream is = descriptor.createInputStream()) {

                                            Properties props = MessageHelper.getSessionProperties();
                                            Session isession = Session.getInstance(props, null);
                                            MimeMessage imessage = new MimeMessage(isession, is);

                                            try (MailService iservice = new MailService(context, account.getProtocol(), account.realm, account.insecure, true)) {
                                                iservice.setPartialFetch(account.partial_fetch);
                                                iservice.setSeparateStoreConnection();
                                                iservice.connect(account);

                                                IMAPFolder ifolder = (IMAPFolder) iservice.getStore().getFolder(inbox.name);
                                                ifolder.open(Folder.READ_WRITE);

                                                if (ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
                                                    imessage.setFlag(Flags.Flag.DRAFT, false);

                                                ifolder.appendMessages(new Message[]{imessage});
                                            }
                                        }

                                        return account.name + "/" + inbox.name;
                                    }

                                    @Override
                                    protected void onExecuted(Bundle args, String name) {
                                        ToastEx.makeText(ActivityEML.this, name, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    protected void onException(Bundle args, @NonNull Throwable ex) {
                                        if (ex instanceof IllegalArgumentException)
                                            Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                                        else
                                            Helper.unexpectedError(getSupportFragmentManager(), ex);
                                    }
                                }.execute(ActivityEML.this, args, "eml:store");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                Helper.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "messages:accounts");
    }

    private class Result {
        String from;
        String to;
        String replyto;
        String cc;
        String subject;
        Spanned headers;
        Spanned parts;
        Spanned body;
        String html;
    }
}
