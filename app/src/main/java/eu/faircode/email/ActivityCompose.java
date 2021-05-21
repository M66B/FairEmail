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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.core.app.TaskStackBuilder;
import androidx.core.net.MailTo;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActivityCompose extends ActivityBase implements FragmentManager.OnBackStackChangedListener {
    static final int PI_REPLY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            handle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handle(intent);
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (!isShared(getIntent().getAction())) {
                Intent parent = getParentActivityIntent();
                if (parent != null)
                    if (shouldUpRecreateTask(parent))
                        TaskStackBuilder.create(this)
                                .addNextIntentWithParentStack(parent)
                                .startActivities();
                    else {
                        parent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(parent);
                    }
            }

            finishAndRemoveTask();
        }
    }

    private void handle(Intent intent) {
        Bundle args;
        String action = intent.getAction();
        if (isShared(action)) {
            args = new Bundle();
            args.putString("action", "new");
            args.putLong("account", -1);

            Uri uri = intent.getData();
            if (uri != null && "mailto".equalsIgnoreCase(uri.getScheme())) {
                // https://www.ietf.org/rfc/rfc2368.txt
                MailTo mailto = MailTo.parse(uri.toString());

                String to = mailto.getTo();
                if (to != null)
                    args.putString("to", to);

                String cc = mailto.getCc();
                if (cc != null)
                    args.putString("cc", cc);

                String subject = mailto.getSubject();
                if (subject != null)
                    args.putString("subject", subject);

                Map<String, String> headers = mailto.getHeaders();
                if (headers != null)
                    for (String key : headers.keySet())
                        if ("in-reply-to".equalsIgnoreCase(key)) {
                            args.putString("inreplyto", headers.get(key));
                            break;
                        }

                String body = mailto.getBody();
                if (body != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String line : body.split("\\r?\\n"))
                        sb.append("<span>").append(Html.escapeHtml(line)).append("<span><br>");
                    args.putString("body", sb.toString());
                }
            }

            if (intent.hasExtra(Intent.EXTRA_SHORTCUT_ID)) {
                String to = intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID);
                if (to != null)
                    args.putString("to", to);
            }

            if (intent.hasExtra(Intent.EXTRA_EMAIL)) {
                List<String> to = sanitize(intent.getStringArrayExtra(Intent.EXTRA_EMAIL));
                if (to.size() > 0)
                    args.putString("to", TextUtils.join(", ", to));
            }

            if (intent.hasExtra(Intent.EXTRA_CC)) {
                List<String> cc = sanitize(intent.getStringArrayExtra(Intent.EXTRA_CC));
                if (cc.size() > 0)
                    args.putString("cc", TextUtils.join(", ", cc));
            }

            if (intent.hasExtra(Intent.EXTRA_BCC)) {
                List<String> bcc = sanitize(intent.getStringArrayExtra(Intent.EXTRA_BCC));
                if (bcc.size() > 0)
                    args.putString("bcc", TextUtils.join(", ", bcc));
            }

            if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
                String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                if (subject != null)
                    args.putString("subject", subject);
            }

            if (intent.hasExtra(Intent.EXTRA_HTML_TEXT)) {
                String html = intent.getStringExtra(Intent.EXTRA_HTML_TEXT);
                if (!TextUtils.isEmpty(html))
                    args.putString("body", html);
            } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                CharSequence body = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
                if (body != null)
                    if (body instanceof Spanned)
                        args.putString("body", HtmlHelper.toHtml((Spanned) body, this));
                    else {
                        String text = body.toString();
                        if (!TextUtils.isEmpty(text)) {
                            String html = "<span>" + text.replaceAll("\\r?\\n", "<br>") + "</span>";
                            args.putString("body", html);
                        }
                    }
            }

            if (intent.hasExtra(Intent.EXTRA_STREAM))
                if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                    ArrayList<Uri> streams = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (streams != null) {
                        // Some apps send null streams
                        ArrayList<Uri> uris = new ArrayList<>();
                        for (Uri stream : streams)
                            if (stream != null)
                                uris.add(stream);
                        if (uris.size() > 0)
                            args.putParcelableArrayList("attachments", uris);
                    }
                } else {
                    Uri stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (stream != null) {
                        ArrayList<Uri> uris = new ArrayList<>();
                        uris.add(stream);
                        args.putParcelableArrayList("attachments", uris);
                    }
                }
        } else
            args = intent.getExtras();

        FragmentCompose fragment = new FragmentCompose();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("compose");
        fragmentTransaction.commit();
    }

    private static boolean isShared(String action) {
        return (Intent.ACTION_VIEW.equals(action) ||
                Intent.ACTION_SENDTO.equals(action) ||
                Intent.ACTION_SEND.equals(action) ||
                Intent.ACTION_SEND_MULTIPLE.equals(action));
    }

    private List<String> sanitize(String[] addresses) {
        List<String> result = new ArrayList<>();
        if (addresses != null)
            for (String address : addresses) {
                address = address.replaceAll("\\s+", "");
                if (!TextUtils.isEmpty(address))
                    result.add(address);
            }
        return result;
    }

    static void undoSend(final long id, final Context context, final LifecycleOwner owner, final FragmentManager manager) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return undoSend(id, context);
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                if (id == null)
                    return;

                context.startActivity(
                        new Intent(context, ActivityCompose.class)
                                .putExtra("action", "edit")
                                .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(manager, ex, !(ex instanceof IllegalArgumentException));
            }
        }.execute(context, owner, args, "undo:sent");
    }

    static Long undoSend(long id, Context context) {
        DB db = DB.getInstance(context);

        // Cancel send
        EntityOperation operation = db.operation().getOperation(id, EntityOperation.SEND);
        if (operation != null)
            if ("executing".equals(operation.state))
                return null;
            else
                db.operation().deleteOperation(operation.id);

        EntityMessage message;

        try {
            db.beginTransaction();

            message = db.message().getMessage(id);
            if (message == null)
                return null;

            db.folder().setFolderError(message.folder, null);
            if (message.identity != null)
                db.identity().setIdentityError(message.identity, null);

            File source = message.getFile(context);

            // Insert into drafts
            EntityFolder drafts = db.folder().getFolderByType(message.account, EntityFolder.DRAFTS);
            if (drafts == null)
                throw new IllegalArgumentException(context.getString(R.string.title_no_drafts));

            message.id = null;
            message.folder = drafts.id;
            message.fts = false;
            message.ui_snoozed = null;
            message.error = null;
            message.id = db.message().insertMessage(message);

            File target = message.getFile(context);
            source.renameTo(target);

            List<EntityAttachment> attachments = db.attachment().getAttachments(id);
            for (EntityAttachment attachment : attachments)
                db.attachment().setMessage(attachment.id, message.id);

            EntityOperation.queue(context, message, EntityOperation.ADD);

            // Delete from outbox
            db.message().deleteMessage(id); // will delete operation too

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSynchronize.eval(context, "outbox/drafts");

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel("send:" + id, 1);

        return message.id;
    }
}
