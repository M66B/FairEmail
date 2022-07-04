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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.core.app.TaskStackBuilder;
import androidx.core.net.MailTo;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

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
        getSupportActionBar().setCustomView(R.layout.action_bar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            handle(getIntent(), true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handle(intent, false);
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            String action = getIntent().getAction();
            if (!isShared(action) &&
                    (action == null || !action.startsWith("widget:"))) {
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

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().remove("last_composing").apply();

            finishAndRemoveTask();
        }
    }

    private void handle(Intent intent, boolean create) {
        Bundle args;
        String action = intent.getAction();
        Log.i("Handle action=" + action + " create=" + create + " " + this);

        if (isShared(action)) {
            args = new Bundle();

            Uri uri = intent.getData();
            if (uri != null && "mailto".equalsIgnoreCase(uri.getScheme())) {
                // https://www.ietf.org/rfc/rfc2368.txt
                MailTo mailto = MailTo.parse(uri.toString());

                List<String> to = sanitize(new String[]{mailto.getTo()});
                if (to.size() > 0)
                    args.putString("to", to.get(0));

                List<String> cc = sanitize(new String[]{mailto.getCc()});
                if (cc.size() > 0)
                    args.putString("cc", cc.get(0));

                String subject = mailto.getSubject();
                if (!TextUtils.isEmpty(subject))
                    args.putString("subject", subject);

                Map<String, String> headers = mailto.getHeaders();
                if (headers != null)
                    for (String key : headers.keySet()) {
                        List<String> address = sanitize(new String[]{headers.get(key)});
                        if (address.size() == 0)
                            continue;
                        if ("bcc".equalsIgnoreCase(key))
                            args.putString("bcc", address.get(0));
                        else if ("in-reply-to".equalsIgnoreCase(key))
                            args.putString("inreplyto", address.get(0));
                    }

                String body = mailto.getBody();
                if (!TextUtils.isEmpty(body)) {
                    StringBuilder sb = new StringBuilder();
                    for (String line : body.split("\\r?\\n"))
                        sb.append("<span>").append(Html.escapeHtml(line)).append("<span><br>");
                    args.putString("body", sb.toString());
                }
            }

            if (intent.hasExtra(Intent.EXTRA_SHORTCUT_ID)) {
                List<String> to = sanitize(new String[]{intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID)});
                if (to.size() > 0)
                    args.putString("to", to.get(0));
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
                if (!TextUtils.isEmpty(subject))
                    args.putString("subject", subject);
            }

            String html = null;

            if (intent.hasExtra(Intent.EXTRA_HTML_TEXT))
                html = intent.getStringExtra(Intent.EXTRA_HTML_TEXT);

            if (TextUtils.isEmpty(html) &&
                    intent.hasExtra(Intent.EXTRA_TEXT)) {
                CharSequence body = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
                if (body != null)
                    if (body instanceof Spanned)
                        html = HtmlHelper.toHtml((Spanned) body, this);
                    else {
                        String text = body.toString();
                        if (!TextUtils.isEmpty(text))
                            html = "<span>" + text.replaceAll("\\r?\\n", "<br>") + "</span>";
                    }
            }

            if (!TextUtils.isEmpty(html))
                args.putString("body", html);

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

        FragmentManager fm = getSupportFragmentManager();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean attach_new = prefs.getBoolean("attach_new", true);

        if (!attach_new && !create &&
                args.size() == 1 && args.containsKey("attachments")) {
            List<Fragment> fragments = fm.getFragments();
            if (fragments.size() == 1) {
                ((FragmentCompose) fragments.get(0)).onSharedAttachments(
                        args.getParcelableArrayList("attachments"));
                return;
            }
        }

        if (isShared(action)) {
            args.putString("action", "new");
            args.putLong("account",
                    intent.getLongExtra("fair:account", -1L));
        }

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("compose", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentCompose fragment = new FragmentCompose();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
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
                if (TextUtils.isEmpty(address))
                    continue;
                address = address.replaceAll("\\s+", " ");
                address = address.replaceAll("\u200b", ""); // Discord: zero width space
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean save_drafts = prefs.getBoolean("save_drafts", true);

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

            if (save_drafts &&
                    (message.ui_encrypt == null ||
                            EntityMessage.ENCRYPT_NONE.equals(message.ui_encrypt)))
                EntityOperation.queue(context, message, EntityOperation.ADD);

            // Delete from outbox
            db.message().deleteMessage(id); // will delete operation too

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSynchronize.eval(context, "outbox/drafts");

        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        nm.cancel("send:" + id, NotificationHelper.NOTIFICATION_TAGGED);

        return message.id;
    }
}
