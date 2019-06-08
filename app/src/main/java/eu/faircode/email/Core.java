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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.MailConnectException;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.net.ssl.SSLException;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

class Core {
    private static int lastUnseen = -1;

    private static final int MAX_NOTIFICATION_COUNT = 10; // per group
    private static final int SYNC_CHUNCK_SIZE = 500;
    private static final int SYNC_BATCH_SIZE = 20;
    private static final int DOWNLOAD_BATCH_SIZE = 20;
    private static final long YIELD_DURATION = 200L; // milliseconds

    static void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : prefs.getAll().keySet())
            if (key.startsWith("notifying:"))
                editor.remove(key);
        editor.apply();
    }

    static void processOperations(
            Context context,
            EntityAccount account, EntityFolder folder,
            Session isession, Store istore, Folder ifolder,
            State state)
            throws MessagingException, JSONException, IOException {
        try {
            Log.i(folder.name + " start process");

            DB db = DB.getInstance(context);
            List<EntityOperation> ops = db.operation().getOperations(folder.id);
            Log.i(folder.name + " pending operations=" + ops.size());
            for (int i = 0; i < ops.size() && state.running() && state.recoverable(); i++) {
                EntityOperation op = ops.get(i);
                try {
                    Log.i(folder.name +
                            " start op=" + op.id + "/" + op.name +
                            " folder=" + op.folder +
                            " msg=" + op.message +
                            " args=" + op.args);

                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("name", op.name);
                    crumb.put("args", op.args);
                    crumb.put("folder", folder.type);
                    crumb.put("free", Integer.toString(Log.getFreeMemMb()));
                    crumb.put("UIDPLUS", Boolean.toString(((IMAPStore) istore).hasCapability("UIDPLUS")));
                    Bugsnag.leaveBreadcrumb("operation", BreadcrumbType.LOG, crumb);

                    // Fetch most recent copy of message
                    EntityMessage message = null;
                    if (op.message != null)
                        message = db.message().getMessage(op.message);

                    JSONArray jargs = new JSONArray(op.args);

                    try {
                        if (message == null &&
                                !EntityOperation.SYNC.equals(op.name) &&
                                !EntityOperation.SUBSCRIBE.equals(op.name))
                            throw new MessageRemovedException();

                        db.operation().setOperationError(op.id, null);
                        if (message != null)
                            db.message().setMessageError(message.id, null);

                        if (message != null && message.uid == null &&
                                !(EntityOperation.ADD.equals(op.name) ||
                                        EntityOperation.ANSWERED.equals(op.name) ||
                                        EntityOperation.DELETE.equals(op.name) ||
                                        EntityOperation.SEND.equals(op.name) ||
                                        EntityOperation.SYNC.equals(op.name) ||
                                        EntityOperation.SUBSCRIBE.equals(op.name)))
                            throw new IllegalArgumentException(op.name + " without uid " + op.args);

                        // Operations should use database transaction when needed

                        if (!EntityOperation.SYNC.equals(op.name))
                            db.operation().setOperationState(op.id, "executing");

                        switch (op.name) {
                            case EntityOperation.SEEN:
                                onSeen(context, jargs, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.FLAG:
                                onFlag(context, jargs, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.ANSWERED:
                                onAnswered(context, jargs, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.KEYWORD:
                                onKeyword(context, jargs, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.ADD:
                                boolean squash = false;
                                for (int j = i + 1; j < ops.size(); j++) {
                                    EntityOperation next = ops.get(j);
                                    if (next.message != null && next.message.equals(op.message) &&
                                            (EntityOperation.ADD.equals(next.name) || EntityOperation.DELETE.equals(next.name))) {
                                        squash = true;
                                        break;
                                    }
                                }
                                if (squash)
                                    Log.i(folder.name +
                                            " squashing op=" + op.id + "/" + op.name +
                                            " msg=" + op.message +
                                            " args=" + op.args);
                                else
                                    onAdd(context, jargs, folder, message, isession, (IMAPStore) istore, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.MOVE:
                                onMove(context, jargs, false, folder, message, isession, (IMAPStore) istore, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.COPY:
                                onMove(context, jargs, true, folder, message, isession, (IMAPStore) istore, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.DELETE:
                                onDelete(context, jargs, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.HEADERS:
                                onHeaders(context, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.RAW:
                                onRaw(context, jargs, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.BODY:
                                onBody(context, folder, message, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.ATTACHMENT:
                                onAttachment(context, jargs, folder, message, op, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.SYNC:
                                onSynchronizeMessages(context, jargs, account, folder, (IMAPFolder) ifolder, state);
                                break;

                            case EntityOperation.SUBSCRIBE:
                                onSubscribeFolder(context, jargs, folder, (IMAPFolder) ifolder);
                                break;

                            default:
                                throw new IllegalArgumentException("Unknown operation=" + op.name);
                        }

                        // Operation succeeded
                        db.operation().deleteOperation(op.id);
                    } catch (Throwable ex) {
                        Log.e(folder.name, ex);
                        reportError(context, account, folder, ex);

                        db.operation().setOperationError(op.id, Helper.formatThrowable(ex, true));
                        if (message != null && !(ex instanceof IllegalArgumentException))
                            db.message().setMessageError(message.id, Helper.formatThrowable(ex, true));

                        if (ex instanceof OutOfMemoryError ||
                                ex instanceof MessageRemovedException ||
                                ex instanceof FolderNotFoundException ||
                                ex instanceof IllegalArgumentException ||
                                ex.getCause() instanceof CommandFailedException) {
                            Log.w("Unrecoverable");

                            // There is no use in repeating
                            db.operation().deleteOperation(op.id);

                            // Cleanup
                            if (EntityOperation.SYNC.equals(op.name))
                                db.folder().setFolderSyncState(folder.id, null);

                            // Cleanup
                            if (message != null) {
                                if (ex instanceof MessageRemovedException)
                                    db.message().deleteMessage(message.id);

                                Long newid = null;

                                if (EntityOperation.MOVE.equals(op.name) &&
                                        jargs.length() > 2 && !jargs.isNull(2))
                                    newid = jargs.getLong(2);

                                if ((EntityOperation.ADD.equals(op.name) ||
                                        EntityOperation.RAW.equals(op.name)) &&
                                        jargs.length() > 0 && !jargs.isNull(0))
                                    newid = jargs.getLong(0);

                                // Delete temporary copy in target folder
                                if (newid != null) {
                                    db.message().deleteMessage(newid);
                                    db.message().setMessageUiHide(message.id, false);
                                }
                            }

                            continue;
                        }

                        throw ex;
                    } finally {
                        db.operation().setOperationState(op.id, null);
                    }
                } finally {
                    Log.i(folder.name + " end op=" + op.id + "/" + op.name);
                }
            }
        } finally {
            Log.i(folder.name + " end process state=" + state);
        }
    }

    private static void onSeen(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, JSONException {
        // Mark message (un)seen
        DB db = DB.getInstance(context);

        if (!ifolder.getPermanentFlags().contains(Flags.Flag.SEEN)) {
            db.message().setMessageSeen(message.id, false);
            db.message().setMessageUiSeen(message.id, false);
            return;
        }

        boolean seen = jargs.getBoolean(0);
        if (message.seen.equals(seen))
            return;

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.SEEN, seen);

        db.message().setMessageSeen(message.id, seen);
    }

    private static void onFlag(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, JSONException {
        // Star/unstar message
        DB db = DB.getInstance(context);

        if (!ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED)) {
            db.message().setMessageFlagged(message.id, false);
            db.message().setMessageUiFlagged(message.id, false);
            return;
        }

        boolean flagged = jargs.getBoolean(0);
        if (message.flagged.equals(flagged))
            return;

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.FLAGGED, flagged);

        db.message().setMessageFlagged(message.id, flagged);
    }

    private static void onAnswered(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, JSONException {
        // Mark message (un)answered
        DB db = DB.getInstance(context);

        if (!ifolder.getPermanentFlags().contains(Flags.Flag.ANSWERED)) {
            db.message().setMessageAnswered(message.id, false);
            db.message().setMessageUiAnswered(message.id, false);
            return;
        }

        boolean answered = jargs.getBoolean(0);
        if (message.answered.equals(answered))
            return;

        // This will be fixed when synchronizing the message
        if (message.uid == null)
            return;

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        imessage.setFlag(Flags.Flag.ANSWERED, answered);

        db.message().setMessageAnswered(message.id, answered);
    }

    private static void onKeyword(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, JSONException {
        // Set/reset user flag
        DB db = DB.getInstance(context);

        if (!ifolder.getPermanentFlags().contains(Flags.Flag.USER)) {
            db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(null));
            return;
        }

        // https://tools.ietf.org/html/rfc3501#section-2.3.2
        String keyword = jargs.getString(0);
        boolean set = jargs.getBoolean(1);

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        Flags flags = new Flags(keyword);
        imessage.setFlags(flags, set);

        try {
            db.beginTransaction();

            message = db.message().getMessage(message.id);

            List<String> keywords = new ArrayList<>(Arrays.asList(message.keywords));
            if (set) {
                if (!keywords.contains(keyword))
                    keywords.add(keyword);
            } else
                keywords.remove(keyword);
            db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(keywords.toArray(new String[0])));

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void onAdd(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, Session isession, IMAPStore istore, IMAPFolder ifolder) throws MessagingException, JSONException, IOException {
        // Add message
        DB db = DB.getInstance(context);

        // Get arguments
        Long tmpid = (jargs.length() > 0 && !jargs.isNull(0) ? jargs.getLong(0) : null);
        boolean autoread = (jargs.length() > 1 && jargs.getBoolean(1));
        boolean across = (jargs.length() > 2 && jargs.getBoolean(2));

        try {
            if (EntityFolder.DRAFTS.equals(folder.type) &&
                    !folder.id.equals(message.folder) &&
                    !across) {
                Log.i("Drafts moved folder=" + message.folder);
                return;
            }

            // Delete previous message(s) with same ID
            if (folder.id.equals(message.folder)) {
                // Prevent adding/deleting message
                db.message().setMessageUid(message.id, message.uid == null ? -1L : -message.uid);

                if (TextUtils.isEmpty(message.msgid)) {
                    // Draft might be created somewhere else
                    if (message.uid == null)
                        throw new IllegalArgumentException("Add without ID");
                    else {
                        Message idelete = ifolder.getMessageByUID(message.uid);
                        Log.i(folder.name + " deleting previous uid=" + message.uid + " msgid=" + message.msgid);
                        try {
                            idelete.setFlag(Flags.Flag.DELETED, true);
                        } catch (MessageRemovedException ignored) {
                        }
                    }
                    message.msgid = EntityMessage.generateMessageId();
                } else {
                    Message[] ideletes = ifolder.search(new MessageIDTerm(message.msgid));
                    for (Message idelete : ideletes) {
                        long uid = ifolder.getUID(idelete);
                        Log.i(folder.name + " deleting previous uid=" + uid + " msgid=" + message.msgid);
                        try {
                            idelete.setFlag(Flags.Flag.DELETED, true);
                        } catch (MessageRemovedException ignored) {
                        }
                    }
                }
                ifolder.expunge();
            }

            // Get message
            MimeMessage imessage;
            if (folder.id.equals(message.folder)) {
                // Pre flight checks
                if (!message.content)
                    throw new IllegalArgumentException("Message body missing");

                EntityIdentity identity =
                        (message.identity == null ? null : db.identity().getIdentity(message.identity));

                imessage = MessageHelper.from(context, message, identity, isession);
            } else {
                // Cross account move
                File file = message.getRawFile(context);
                if (!file.exists())
                    throw new IllegalArgumentException("raw message file not found");

                Log.i(folder.name + " reading " + file);
                try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                    imessage = new MimeMessage(isession, is);
                }
            }

            // Handle auto read
            if (ifolder.getPermanentFlags().contains(Flags.Flag.SEEN)) {
                if (autoread && !imessage.isSet(Flags.Flag.SEEN)) {
                    Log.i(folder.name + " autoread");
                    imessage.setFlag(Flags.Flag.SEEN, true);
                }
            }

            // Handle draft
            if (EntityFolder.DRAFTS.equals(folder.type))
                if (ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
                    imessage.setFlag(Flags.Flag.DRAFT, true);

            // Add message
            long uid = -1;
            if (istore.hasCapability("UIDPLUS")) {
                AppendUID[] uids = ifolder.appendUIDMessages(new Message[]{imessage});
                if (uids != null && uids.length > 0) {
                    Log.i("Appended uid=" + uids[0].uid);
                    uid = uids[0].uid;
                }
            } else
                ifolder.appendMessages(new Message[]{imessage});

            // Lookup uid
            if (uid <= 0) {
                Log.i("Searching for appended msgid=" + message.msgid);
                Message[] messages = ifolder.search(new MessageIDTerm(message.msgid));
                if (messages != null)
                    for (Message iappended : messages) {
                        long muid = ifolder.getUID(iappended);
                        Log.i("Found appended uid=" + muid);
                        // RFC3501: Unique identifiers are assigned in a strictly ascending fashion
                        if (muid > uid)
                            uid = muid;
                    }
            }

            if (uid <= 0)
                throw new IllegalArgumentException("uid not found");

            Log.i(folder.name + " appended id=" + message.id + " uid=" + uid);

            if (folder.id.equals(message.folder)) {
                Log.i(folder.name + " Setting id=" + message.id + " uid=" + uid);
                db.message().setMessageUid(message.id, uid);
            } else
                try {
                    db.beginTransaction();

                    // Cross account move
                    if (tmpid != null) {
                        Log.i(folder.name + " Setting id=" + tmpid + " (tmp) appended uid=" + uid);
                        db.message().setMessageUid(tmpid, uid);
                    }

                    // Mark source read
                    if (autoread) {
                        Log.i(folder.name + " queuing SEEN id=" + message.id);
                        EntityOperation.queue(context, message, EntityOperation.SEEN, true);
                    }

                    // Delete source
                    Log.i(folder.name + " queuing DELETE id=" + message.id);
                    EntityOperation.queue(context, message, EntityOperation.DELETE);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
        } catch (Throwable ex) {
            if (folder.id.equals(message.folder))
                db.message().setMessageUid(message.id, message.uid);
            throw ex;
        }
    }

    private static void onMove(Context context, JSONArray jargs, boolean copy, EntityFolder folder, EntityMessage message, Session isession, IMAPStore istore, IMAPFolder ifolder) throws JSONException, MessagingException, IOException {
        // Move message
        DB db = DB.getInstance(context);

        // Get arguments
        long id = jargs.getLong(0);
        boolean autoread = (jargs.length() > 1 && jargs.getBoolean(1));

        // Get source message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Auto read
        if (autoread && ifolder.getPermanentFlags().contains(Flags.Flag.SEEN))
            imessage.setFlag(Flags.Flag.SEEN, true);

        // Get target folder
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();
        IMAPFolder itarget = (IMAPFolder) istore.getFolder(target.name);

        ifolder.copyMessages(new Message[]{imessage}, itarget);

        // Delete source
        if (!copy) {
            try {
                imessage.setFlag(Flags.Flag.DELETED, true);
            } catch (MessageRemovedException ignored) {
            }
            ifolder.expunge();
        }
    }

    private static void onDelete(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException {
        // Delete message
        DB db = DB.getInstance(context);

        Message[] imessages;
        if (TextUtils.isEmpty(message.msgid))
            if (message.uid == null)
                throw new IllegalArgumentException("Delete without ID");
            else {
                Message imessage = ifolder.getMessageByUID(message.uid);
                if (imessage == null)
                    throw new MessageRemovedException();
                imessages = new Message[]{imessage};
            }
        else
            imessages = ifolder.search(new MessageIDTerm(message.msgid));

        for (Message imessage : imessages) {
            Log.i(folder.name + " deleting uid=" + message.uid + " msgid=" + message.msgid);
            try {
                imessage.setFlag(Flags.Flag.DELETED, true);
            } catch (MessageRemovedException ignored) {
            }
        }

        ifolder.expunge();

        db.message().deleteMessage(message.id);
    }

    private static void onHeaders(Context context, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException {
        // Download headers
        DB db = DB.getInstance(context);

        if (message.headers != null)
            return;

        IMAPMessage imessage = (IMAPMessage) ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        MessageHelper helper = new MessageHelper(imessage);
        db.message().setMessageHeaders(message.id, helper.getHeaders());
    }

    private static void onRaw(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, IOException, JSONException {
        // Download raw message
        DB db = DB.getInstance(context);

        if (message.raw == null || !message.raw) {
            IMAPMessage imessage = (IMAPMessage) ifolder.getMessageByUID(message.uid);
            if (imessage == null)
                throw new MessageRemovedException();

            File file = message.getRawFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                imessage.writeTo(os);
                db.message().setMessageRaw(message.id, true);
            }
        }

        if (jargs.length() > 0) {
            // Cross account move
            long target = jargs.getLong(2);
            jargs.remove(2);
            jargs.put(2, true); // cross account
            Log.i(folder.name + " queuing ADD id=" + message.id + ":" + target);

            EntityOperation operation = new EntityOperation();
            operation.folder = target;
            operation.message = message.id;
            operation.name = EntityOperation.ADD;
            operation.args = jargs.toString();
            operation.created = new Date().getTime();
            operation.id = db.operation().insertOperation(operation);
        }
    }

    private static void onBody(Context context, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, IOException {
        // Download message body
        DB db = DB.getInstance(context);

        if (message.content)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        MessageHelper helper = new MessageHelper((MimeMessage) imessage);
        MessageHelper.MessageParts parts = helper.getMessageParts();
        String body = parts.getHtml(context);
        Helper.writeText(message.getFile(context), body);
        db.message().setMessageContent(message.id,
                true,
                parts.isPlainOnly(),
                HtmlHelper.getPreview(body),
                parts.getWarnings(message.warning));
    }

    private static void onAttachment(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws JSONException, MessagingException, IOException {
        // Download attachment
        DB db = DB.getInstance(context);

        long id = jargs.getLong(0);

        // Get attachment
        EntityAttachment local = db.attachment().getAttachment(id);
        if (local == null)
            local = db.attachment().getAttachment(message.id, (int) id); // legacy
        if (local == null)
            throw new IllegalArgumentException("Local attachment not found");
        if (local.available)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Get message parts
        MessageHelper helper = new MessageHelper((MimeMessage) imessage);
        MessageHelper.MessageParts parts = helper.getMessageParts();

        // Match attachment by attributes
        // Some servers order attachments randomly
        boolean found = false;
        List<EntityAttachment> remotes = parts.getAttachments();
        for (int i = 0; i < remotes.size(); i++) {
            EntityAttachment remote = remotes.get(i);
            if (Objects.equals(remote.name, local.name) &&
                    Objects.equals(remote.type, local.type) &&
                    Objects.equals(remote.disposition, local.disposition) &&
                    Objects.equals(remote.cid, local.cid) &&
                    Objects.equals(remote.encryption, local.encryption) &&
                    Objects.equals(remote.size, local.size)) {
                found = true;
                parts.downloadAttachment(context, i, local.id, local.name);
            }
        }

        if (!found) {
            db.attachment().setError(local.id, "Attachment not found");
            if (!EntityFolder.DRAFTS.equals(folder.type)) {
                Log.w("Attachment not found local=" + local);
                for (EntityAttachment remote : remotes)
                    Log.w("Attachment remote=" + remote);
                throw new IllegalArgumentException("Attachment not found");
            }
        }
    }

    static void onSynchronizeFolders(Context context, EntityAccount account, Store istore, State state) throws MessagingException {
        DB db = DB.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean subscribed_only = prefs.getBoolean("subscribed_only", false);
        boolean sync_folders = prefs.getBoolean("sync_folders", true);

        // Get folder names
        List<String> names = new ArrayList<>();
        for (EntityFolder folder : db.folder().getFolders(account.id))
            if (folder.tbc != null) {
                Log.i(folder.name + " creating");
                Folder ifolder = istore.getFolder(folder.name);
                if (!ifolder.exists())
                    ifolder.create(Folder.HOLDS_MESSAGES);
                db.folder().resetFolderTbc(folder.id);
                names.add(folder.name);
                sync_folders = true;
            } else if (folder.tbd != null && folder.tbd) {
                Log.i(folder.name + " deleting");
                Folder ifolder = istore.getFolder(folder.name);
                if (ifolder.exists())
                    ifolder.delete(false);
                db.folder().deleteFolder(folder.id);
                sync_folders = true;
            } else {
                names.add(folder.name);
                if (folder.initialize)
                    sync_folders = true;
            }
        Log.i("Local folder count=" + names.size());

        if (!sync_folders)
            return;

        Log.i("Start sync folders account=" + account.name);

        // Get default folder
        Folder defaultFolder = istore.getDefaultFolder();
        char separator = defaultFolder.getSeparator();
        EntityLog.log(context, account.name + " folder separator=" + separator);

        // Get remote folders
        long start = new Date().getTime();
        Folder[] ifolders = (subscribed_only
                ? defaultFolder.listSubscribed("*")
                : defaultFolder.list("*"));

        // Get subscribed folders
        List<String> subscription = new ArrayList<>();
        try {
            Folder[] isubscribed = (subscribed_only ? ifolders : defaultFolder.listSubscribed("*"));
            for (Folder ifolder : isubscribed)
                subscription.add(ifolder.getFullName());
        } catch (MessagingException ex) {
            Log.e(ex);
        }

        if (subscribed_only && ifolders.length == 0) {
            Log.i("No subscribed folders");
            ifolders = defaultFolder.list("*");
        }
        long duration = new Date().getTime() - start;

        Log.i("Remote folder count=" + ifolders.length +
                " subscribed=" + subscription.size() +
                " separator=" + separator +
                " fetched in " + duration + " ms");

        Map<String, EntityFolder> nameFolder = new HashMap<>();
        Map<String, List<EntityFolder>> parentFolders = new HashMap<>();
        for (Folder ifolder : ifolders) {
            String fullName = ifolder.getFullName();
            String[] name = fullName.split("[" + separator + "]");
            String childName = name[name.length - 1];
            boolean subscribed = subscription.contains(fullName);
            String[] attr = ((IMAPFolder) ifolder).getAttributes();
            String type = EntityFolder.getType(attr, fullName);

            if (EntityFolder.INBOX.equals(type))
                childName = null;

            Log.i(account.name + ":" + fullName + " subscribed=" + subscribed +
                    " type=" + type + " attrs=" + TextUtils.join(" ", attr));

            if (type != null) {
                names.remove(fullName);

                EntityFolder folder;
                try {
                    db.beginTransaction();

                    folder = db.folder().getFolderByName(account.id, fullName);
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = fullName;
                        folder.display = childName;
                        folder.type = (EntityFolder.SYSTEM.equals(type) ? type : EntityFolder.USER);
                        folder.synchronize = false;
                        folder.subscribed = subscribed;
                        folder.poll = ("imap.gmail.com".equals(account.host));
                        folder.sync_days = EntityFolder.DEFAULT_SYNC;
                        folder.keep_days = EntityFolder.DEFAULT_KEEP;
                        folder.id = db.folder().insertFolder(folder);
                        Log.i(folder.name + " added type=" + folder.type);
                    } else {
                        Log.i(folder.name + " exists type=" + folder.type);

                        if (folder.subscribed == null || !folder.subscribed.equals(subscribed))
                            db.folder().setFolderSubscribed(folder.id, subscribed);

                        if (folder.display == null && childName != null)
                            db.folder().setFolderDisplay(folder.id, childName);

                        // Compatibility
                        if (EntityFolder.USER.equals(folder.type) && EntityFolder.SYSTEM.equals(type))
                            db.folder().setFolderType(folder.id, type);
                        else if (EntityFolder.SYSTEM.equals(folder.type) && EntityFolder.USER.equals(type))
                            db.folder().setFolderType(folder.id, type);
                        else if (EntityFolder.INBOX.equals(type) && !EntityFolder.INBOX.equals(folder.type)) {
                            if (db.folder().getFolderByType(folder.account, EntityFolder.INBOX) == null)
                                db.folder().setFolderType(folder.id, type);
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    Log.i("End sync folder");
                }

                nameFolder.put(folder.name, folder);
                String parentName = folder.getParentName(separator);
                if (!parentFolders.containsKey(parentName))
                    parentFolders.put(parentName, new ArrayList<EntityFolder>());
                parentFolders.get(parentName).add(folder);
            }
        }

        Log.i("Updating folder parents=" + parentFolders.size());
        for (String parentName : parentFolders.keySet()) {
            EntityFolder parent = nameFolder.get(parentName);
            for (EntityFolder child : parentFolders.get(parentName))
                db.folder().setFolderParent(child.id, parent == null ? null : parent.id);
        }

        Log.i("Delete local count=" + names.size());
        for (String name : names) {
            Log.i(name + " delete");
            db.folder().deleteFolder(account.id, name);
        }
    }

    private static void onSubscribeFolder(Context context, JSONArray jargs, EntityFolder folder, IMAPFolder ifolder)
            throws JSONException, MessagingException {
        boolean subscribe = jargs.getBoolean(0);
        ifolder.setSubscribed(subscribe);

        DB db = DB.getInstance(context);
        db.folder().setFolderSubscribed(folder.id, subscribe);

        Log.i(folder.name + " subscribed=" + subscribe);
    }

    private static void onSynchronizeMessages(
            Context context, JSONArray jargs,
            EntityAccount account, final EntityFolder folder,
            final IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        final DB db = DB.getInstance(context);
        try {
            // Legacy
            if (jargs.length() == 0)
                jargs = folder.getSyncArgs();

            int sync_days = jargs.getInt(0);
            int keep_days = jargs.getInt(1);
            boolean download = (jargs.length() > 2 && jargs.getBoolean(2));
            boolean auto_delete = (jargs.length() > 3 && jargs.getBoolean(3));

            if (keep_days == sync_days)
                keep_days++;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean sync_unseen = prefs.getBoolean("sync_unseen", false);
            boolean sync_flagged = prefs.getBoolean("sync_flagged", true);
            boolean sync_kept = prefs.getBoolean("sync_kept", false);

            Log.i(folder.name + " start sync after=" + sync_days + "/" + keep_days +
                    "sync unseen=" + sync_unseen + " flagged=" + sync_flagged + " kept=" + sync_kept);

            db.folder().setFolderSyncState(folder.id, "syncing");

            // Get reference times
            Calendar cal_sync = Calendar.getInstance();
            cal_sync.add(Calendar.DAY_OF_MONTH, -sync_days);
            cal_sync.set(Calendar.HOUR_OF_DAY, 0);
            cal_sync.set(Calendar.MINUTE, 0);
            cal_sync.set(Calendar.SECOND, 0);
            cal_sync.set(Calendar.MILLISECOND, 0);

            Calendar cal_keep = Calendar.getInstance();
            cal_keep.add(Calendar.DAY_OF_MONTH, -keep_days);
            cal_keep.set(Calendar.HOUR_OF_DAY, 0);
            cal_keep.set(Calendar.MINUTE, 0);
            cal_keep.set(Calendar.SECOND, 0);
            cal_keep.set(Calendar.MILLISECOND, 0);

            long sync_time = cal_sync.getTimeInMillis();
            if (sync_time < 0)
                sync_time = 0;

            long keep_time = cal_keep.getTimeInMillis();
            if (keep_time < 0)
                keep_time = 0;

            Log.i(folder.name + " sync=" + new Date(sync_time) + " keep=" + new Date(keep_time));

            // Delete old local messages
            if (auto_delete && EntityFolder.TRASH.equals(folder.type)) {
                List<Long> tbds = db.message().getMessagesBefore(folder.id, keep_time, false);
                Log.i(folder.name + " local tbd=" + tbds.size());
                for (Long tbd : tbds) {
                    EntityMessage message = db.message().getMessage(tbd);
                    if (message != null)
                        EntityOperation.queue(context, message, EntityOperation.DELETE);
                }
            } else {
                int old = db.message().deleteMessagesBefore(folder.id, keep_time, false);
                Log.i(folder.name + " local old=" + old);
            }

            // Get list of local uids
            final List<Long> uids = db.message().getUids(folder.id, sync_kept ? null : sync_time);
            Log.i(folder.name + " local count=" + uids.size());

            // Reduce list of local uids
            SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time));
            if (sync_unseen)
                searchTerm = new OrTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            if (sync_flagged && ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED))
                searchTerm = new OrTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

            long search = SystemClock.elapsedRealtime();
            Message[] imessages;
            try {
                imessages = ifolder.search(searchTerm);
            } catch (MessagingException ex) {
                Log.w(ex.getMessage());
                // Fallback to date only search
                imessages = ifolder.search(new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time)));
            }
            Log.i(folder.name + " remote count=" + imessages.length +
                    " search=" + (SystemClock.elapsedRealtime() - search) + " ms");

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID); // To check if message exists
            fp.add(FetchProfile.Item.FLAGS); // To update existing messages
            ifolder.fetch(imessages, fp);

            long fetch = SystemClock.elapsedRealtime();
            Log.i(folder.name + " remote fetched=" + (SystemClock.elapsedRealtime() - fetch) + " ms");

            // Sort for finding references messages
            Arrays.sort(imessages, new Comparator<Message>() {
                @Override
                public int compare(Message m1, Message m2) {
                    try {
                        return Long.compare(ifolder.getUID(m1), ifolder.getUID(m2));
                    } catch (MessagingException ex) {
                        return 0;
                    }
                }
            });

            for (int i = 0; i < imessages.length && state.running() && state.recoverable(); i++)
                try {
                    uids.remove(ifolder.getUID(imessages[i]));
                } catch (MessageRemovedException ex) {
                    Log.w(folder.name, ex);
                } catch (Throwable ex) {
                    Log.e(folder.name, ex);
                    reportError(context, account, folder, ex);
                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                }

            if (uids.size() > 0) {
                MessagingException ex = (MessagingException) ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                    @Override
                    public Object doCommand(IMAPProtocol protocol) {
                        Log.i(folder.name + " executing uid fetch count=" + uids.size());
                        List<List<Long>> chunked = Helper.chunkList(new ArrayList<>(uids), SYNC_CHUNCK_SIZE);
                        for (int c = 0; c < chunked.size(); c++) {
                            Log.i(folder.name + " chunk #" + c + " size=" + chunked.get(c).size());
                            Response[] responses = protocol.command(
                                    "UID FETCH " + TextUtils.join(",", chunked.get(c)) + " (UID)", null);

                            if (responses.length > 0 && responses[responses.length - 1].isOK()) {
                                for (Response response : responses)
                                    if (response instanceof FetchResponse) {
                                        FetchResponse fr = (FetchResponse) response;
                                        UID uid = fr.getItem(UID.class);
                                        if (uid != null)
                                            uids.remove(uid.uid);
                                    }
                            } else {
                                for (Response response : responses)
                                    if (response.isBYE())
                                        return new MessagingException("UID FETCH", new IOException(response.toString()));
                                    else if (response.isNO() || response.isBAD())
                                        return new MessagingException(response.toString());
                                return new MessagingException("UID FETCH failed");
                            }
                        }
                        return null;
                    }
                });
                if (ex != null)
                    throw ex;

                long getuid = SystemClock.elapsedRealtime();
                Log.i(folder.name + " remote uids=" + (SystemClock.elapsedRealtime() - getuid) + " ms");
            }

            // Delete local messages not at remote
            Log.i(folder.name + " delete=" + uids.size());
            for (Long uid : uids) {
                int count = db.message().deleteMessage(folder.id, uid);
                Log.i(folder.name + " delete local uid=" + uid + " count=" + count);
            }

            List<EntityRule> rules = db.rule().getEnabledRules(folder.id);

            fp.add(FetchProfile.Item.ENVELOPE);
            // fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            // fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            // fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);

            // Add/update local messages
            Long[] ids = new Long[imessages.length];
            Log.i(folder.name + " add=" + imessages.length);
            for (int i = imessages.length - 1; i >= 0 && state.running() && state.recoverable(); i -= SYNC_BATCH_SIZE) {
                int from = Math.max(0, i - SYNC_BATCH_SIZE + 1);
                Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);

                // Full fetch new/changed messages only
                List<Message> full = new ArrayList<>();
                for (Message imessage : isub) {
                    long uid = ifolder.getUID(imessage); // already fetched
                    EntityMessage message = db.message().getMessageByUid(folder.id, uid);
                    if (message == null)
                        full.add(imessage);
                }
                if (full.size() > 0) {
                    long headers = SystemClock.elapsedRealtime();
                    ifolder.fetch(full.toArray(new Message[0]), fp);
                    Log.i(folder.name + " fetched headers=" + full.size() +
                            " " + (SystemClock.elapsedRealtime() - headers) + " ms");
                }

                int free = Log.getFreeMemMb();
                Map<String, String> crumb = new HashMap<>();
                crumb.put("start", Integer.toString(from));
                crumb.put("end", Integer.toString(i));
                crumb.put("free", Integer.toString(free));
                Bugsnag.leaveBreadcrumb("sync", BreadcrumbType.LOG, crumb);
                Log.i("Sync " + from + ".." + i + " free=" + free);

                for (int j = isub.length - 1; j >= 0 && state.running() && state.recoverable(); j--)
                    try {
                        EntityMessage message = synchronizeMessage(
                                context,
                                account, folder,
                                ifolder, (IMAPMessage) isub[j],
                                false,
                                rules);
                        ids[from + j] = message.id;
                    } catch (MessageRemovedException ex) {
                        Log.w(folder.name, ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(folder.name, ex);
                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(folder.name, ex);
                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                    } finally {
                        // Free memory
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }
            }

            // Delete not synchronized messages without uid
            db.message().deleteOrphans(folder.id);

            // Add local sent messages to remote sent folder
            if (EntityFolder.SENT.equals(folder.type)) {
                List<EntityMessage> orphans = db.message().getSentOrphans(folder.account);
                Log.i(folder.name + " sent orphans=" + orphans.size() + " account=" + folder.account);
                for (EntityMessage orphan : orphans) {
                    Log.i(folder.name + " adding orphan id=" + orphan.id + " sent=" + new Date(orphan.sent));
                    orphan.folder = folder.id;
                    db.message().updateMessage(orphan);
                    EntityOperation.queue(context, orphan, EntityOperation.ADD);
                }
            }

            int count = ifolder.getMessageCount();
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

            if (download) {
                db.folder().setFolderSyncState(folder.id, "downloading");

                // Download messages/attachments
                Log.i(folder.name + " download=" + imessages.length);
                for (int i = imessages.length - 1; i >= 0 && state.running() && state.recoverable(); i -= DOWNLOAD_BATCH_SIZE) {
                    int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);

                    Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                    // Fetch on demand

                    int free = Log.getFreeMemMb();
                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("start", Integer.toString(from));
                    crumb.put("end", Integer.toString(i));
                    crumb.put("free", Integer.toString(free));
                    Bugsnag.leaveBreadcrumb("download", BreadcrumbType.LOG, crumb);
                    Log.i("Download " + from + ".." + i + " free=" + free);

                    for (int j = isub.length - 1; j >= 0 && state.running() && state.recoverable(); j--)
                        try {
                            if (ids[from + j] != null)
                                downloadMessage(
                                        context,
                                        folder, ifolder,
                                        (IMAPMessage) isub[j], ids[from + j], state);
                        } catch (FolderClosedException ex) {
                            throw ex;
                        } catch (Throwable ex) {
                            Log.e(folder.name, ex);
                        } finally {
                            // Free memory
                            ((IMAPMessage) isub[j]).invalidateHeaders();
                        }
                }
            }

            if (state.running) {
                folder.initialize = false;
                db.folder().setFolderInitialized(folder.id);
            }

            db.folder().setFolderSync(folder.id, new Date().getTime());
            db.folder().setFolderError(folder.id, null);

        } finally {
            Log.i(folder.name + " end sync state=" + state);
            db.folder().setFolderSyncState(folder.id, null);
        }
    }

    static EntityMessage synchronizeMessage(
            Context context,
            EntityAccount account, EntityFolder folder,
            IMAPFolder ifolder, IMAPMessage imessage,
            boolean browsed,
            List<EntityRule> rules) throws MessagingException, IOException {
        synchronized (folder) {
            long uid = ifolder.getUID(imessage);

            if (imessage.isExpunged()) {
                Log.i(folder.name + " expunged uid=" + uid);
                throw new MessageRemovedException();
            }
            if (imessage.isSet(Flags.Flag.DELETED)) {
                Log.i(folder.name + " deleted uid=" + uid);
                throw new MessageRemovedException();
            }

            MessageHelper helper = new MessageHelper(imessage);
            boolean seen = helper.getSeen();
            boolean answered = helper.getAnsered();
            boolean flagged = helper.getFlagged();
            String flags = helper.getFlags();
            String[] keywords = helper.getKeywords();
            boolean update = false;
            boolean filter = false;

            DB db = DB.getInstance(context);

            // Find message by uid (fast, no headers required)
            EntityMessage message = db.message().getMessageByUid(folder.id, uid);

            // Find message by Message-ID (slow, headers required)
            // - messages in inbox have same id as message sent to self
            // - messages in archive have same id as original
            if (message == null) {
                String msgid = helper.getMessageID();
                Log.i(folder.name + " searching for " + msgid);
                for (EntityMessage dup : db.message().getMessageByMsgId(folder.account, msgid)) {
                    EntityFolder dfolder = db.folder().getFolder(dup.folder);
                    Log.i(folder.name + " found as id=" + dup.id + "/" + dup.uid +
                            " folder=" + dfolder.type + ":" + dup.folder + "/" + folder.type + ":" + folder.id +
                            " msgid=" + dup.msgid + " thread=" + dup.thread);

                    if (dup.folder.equals(folder.id) ||
                            (EntityFolder.OUTBOX.equals(dfolder.type) && EntityFolder.SENT.equals(folder.type))) {
                        String thread = helper.getThreadId(context, account.id, uid);
                        Log.i(folder.name + " found as id=" + dup.id +
                                " uid=" + dup.uid + "/" + uid +
                                " msgid=" + msgid + " thread=" + thread);

                        if (dup.uid == null) {
                            Log.i(folder.name + " set uid=" + uid);
                            dup.folder = folder.id; // outbox to sent
                            dup.uid = uid;
                            dup.msgid = msgid;
                            dup.thread = thread;
                            if (dup.size == null)
                                dup.size = helper.getSize();
                            dup.error = null;
                            message = dup;
                            update = true;
                            filter = true;
                        } else if (dup.uid < 0)
                            throw new MessageRemovedException();
                    }
                }

                if (message == null)
                    filter = true;
            }

            if (message == null) {
                Address[] froms = helper.getFrom();
                Address[] tos = helper.getTo();
                Address[] ccs = helper.getCc();

                // Build ordered list of addresses
                List<Address> addresses = new ArrayList<>();
                if (folder.isOutgoing()) {
                    if (froms != null)
                        addresses.addAll(Arrays.asList(froms));
                } else {
                    if (tos != null)
                        addresses.addAll(Arrays.asList(tos));
                    if (ccs != null)
                        addresses.addAll(Arrays.asList(ccs));
                    if (EntityFolder.ARCHIVE.equals(folder.type) || BuildConfig.DEBUG) {
                        if (froms != null)
                            addresses.addAll(Arrays.asList(froms));
                    }
                }

                // Search for matching identity
                EntityIdentity identity = null;
                for (Address address : addresses) {
                    String email = ((InternetAddress) address).getAddress();
                    if (!TextUtils.isEmpty(email)) {
                        identity = db.identity().getIdentity(folder.account, email);
                        if (identity == null) {
                            String canonical = MessageHelper.canonicalAddress(email);
                            if (!canonical.equals(email))
                                identity = db.identity().getIdentity(folder.account, canonical);
                        }
                        if (identity != null)
                            break;
                    }
                }

                String authentication = helper.getAuthentication();
                MessageHelper.MessageParts parts = helper.getMessageParts();

                message = new EntityMessage();
                message.account = folder.account;
                message.folder = folder.id;
                message.identity = (identity == null ? null : identity.id);
                message.uid = uid;

                message.msgid = helper.getMessageID();
                if (TextUtils.isEmpty(message.msgid))
                    Log.w("No Message-ID id=" + message.id + " uid=" + message.uid);

                message.references = TextUtils.join(" ", helper.getReferences());
                message.inreplyto = helper.getInReplyTo();
                // Local address contains control or whitespace in string ``mailing list someone@example.org''
                message.deliveredto = helper.getDeliveredTo();
                message.thread = helper.getThreadId(context, account.id, uid);
                message.receipt_request = helper.getReceiptRequested();
                message.receipt_to = helper.getReceiptTo();
                message.dkim = MessageHelper.getAuthentication("dkim", authentication);
                message.spf = MessageHelper.getAuthentication("spf", authentication);
                message.dmarc = MessageHelper.getAuthentication("dmarc", authentication);
                message.from = froms;
                message.to = tos;
                message.cc = ccs;
                message.bcc = helper.getBcc();
                message.reply = helper.getReply();
                message.list_post = helper.getListPost();
                message.subject = helper.getSubject();
                message.size = helper.getSize();
                message.content = false;
                message.received = helper.getReceived();
                message.sent = helper.getSent();
                message.seen = seen;
                message.answered = answered;
                message.flagged = flagged;
                message.flags = flags;
                message.keywords = keywords;
                message.ui_seen = seen;
                message.ui_answered = answered;
                message.ui_flagged = flagged;
                message.ui_hide = false;
                message.ui_found = false;
                message.ui_ignored = seen;
                message.ui_browsed = browsed;

                message.sender = MessageHelper.getSortKey(message.from);
                Uri lookupUri = ContactInfo.getLookupUri(context, message.from);
                message.avatar = (lookupUri == null ? null : lookupUri.toString());

                /*
                // Authentication is more reliable
                Address sender = helper.getSender(); // header
                if (sender != null) {
                    String[] s = ((InternetAddress) sender).getAddress().split("@");
                    String[] f = (froms == null || froms.length == 0 ? null
                            : (((InternetAddress) froms[0]).getAddress()).split("@"));
                    if (s.length > 1 && (f == null || (f.length > 1 && !s[1].equals(f[1]))))
                        message.warning = context.getString(R.string.title_via, s[1]);
                }
                */

                try {
                    db.beginTransaction();

                    // Check if message was added in the meantime
                    EntityMessage existing = db.message().getMessageByUid(message.folder, message.uid);
                    if (existing != null) {
                        Log.i("Message was already added");
                        return existing;
                    }

                    message.id = db.message().insertMessage(message);
                    Log.i(folder.name + " added id=" + message.id + " uid=" + message.uid);

                    int sequence = 1;
                    for (EntityAttachment attachment : parts.getAttachments()) {
                        Log.i(folder.name + " attachment seq=" + sequence +
                                " name=" + attachment.name + " type=" + attachment.type +
                                " cid=" + attachment.cid + " pgp=" + attachment.encryption);
                        attachment.message = message.id;
                        attachment.sequence = sequence++;
                        attachment.id = db.attachment().insertAttachment(attachment);
                    }

                    runRules(context, imessage, message, rules);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (message.received > account.created &&
                        !EntityFolder.ARCHIVE.equals(folder.type) &&
                        !EntityFolder.TRASH.equals(folder.type) &&
                        !EntityFolder.JUNK.equals(folder.type))
                    updateContactInfo(context, folder, message);
            } else {
                if (!message.seen.equals(seen) || !message.seen.equals(message.ui_seen)) {
                    update = true;
                    message.seen = seen;
                    message.ui_seen = seen;
                    if (seen)
                        message.ui_ignored = true;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " seen=" + seen);
                }

                if (!message.answered.equals(answered) || !message.answered.equals(message.ui_answered)) {
                    if (!answered && message.ui_answered && ifolder.getPermanentFlags().contains(Flags.Flag.ANSWERED)) {
                        // This can happen when the answered operation was skipped because the message was moving
                        answered = true;
                        imessage.setFlag(Flags.Flag.ANSWERED, answered);
                    }
                    update = true;
                    message.answered = answered;
                    message.ui_answered = answered;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " answered=" + answered);
                }

                if (!message.flagged.equals(flagged) || !message.flagged.equals(message.ui_flagged)) {
                    update = true;
                    message.flagged = flagged;
                    message.ui_flagged = flagged;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flagged=" + flagged);
                }

                if (!Objects.equals(flags, message.flags)) {
                    update = true;
                    message.flags = flags;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flags=" + flags);
                }

                if (!Helper.equal(message.keywords, keywords)) {
                    update = true;
                    message.keywords = keywords;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid +
                            " keywords=" + TextUtils.join(" ", keywords));
                }

                if (message.ui_hide && db.operation().getOperationCount(folder.id, message.id) == 0) {
                    update = true;
                    message.ui_hide = false;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " unhide");
                }

                if (message.ui_browsed) {
                    update = true;
                    message.ui_browsed = false;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " unbrowse");
                }

                Uri uri = ContactInfo.getLookupUri(context, message.from);
                String avatar = (uri == null ? null : uri.toString());
                if (!Objects.equals(message.avatar, avatar)) {
                    update = true;
                    message.avatar = avatar;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " avatar=" + avatar);
                }

                if (update)
                    try {
                        db.beginTransaction();

                        db.message().updateMessage(message);

                        if (filter)
                            runRules(context, imessage, message, rules);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                else if (BuildConfig.DEBUG)
                    Log.i(folder.name + " unchanged uid=" + uid);
            }

            List<String> fkeywords = new ArrayList<>(Arrays.asList(folder.keywords));

            for (String keyword : keywords)
                if (!fkeywords.contains(keyword)) {
                    Log.i(folder.name + " adding keyword=" + keyword);
                    fkeywords.add(keyword);
                }

            if (folder.keywords.length != fkeywords.size()) {
                Collections.sort(fkeywords);
                db.folder().setFolderKeywords(folder.id, DB.Converters.fromStringArray(fkeywords.toArray(new String[0])));
            }

            return message;
        }
    }

    private static void runRules(Context context, IMAPMessage imessage, EntityMessage message, List<EntityRule> rules) {
        if (!Helper.isPro(context))
            return;

        DB db = DB.getInstance(context);
        try {
            for (EntityRule rule : rules)
                if (rule.matches(context, message, imessage)) {
                    rule.execute(context, message);
                    if (rule.stop)
                        break;
                }
        } catch (Throwable ex) {
            Log.e(ex);
            db.message().setMessageError(message.id, Helper.formatThrowable(ex));
        }
    }

    private static void updateContactInfo(Context context, final EntityFolder folder, final EntityMessage message) {
        final DB db = DB.getInstance(context);

        final int type = (folder.isOutgoing() ? EntityContact.TYPE_TO : EntityContact.TYPE_FROM);
        Address[] recipients = (type == EntityContact.TYPE_TO
                ? message.to
                : (message.reply != null ? message.reply : message.from));

        // Check if from self
        if (type == EntityContact.TYPE_FROM && recipients != null && recipients.length > 0) {
            boolean me = true;
            for (Address reply : recipients) {
                String email = ((InternetAddress) reply).getAddress();
                String canonical = MessageHelper.canonicalAddress(email);
                if (!TextUtils.isEmpty(email) &&
                        db.identity().getIdentity(folder.account, email) == null &&
                        (canonical.equals(email) ||
                                db.identity().getIdentity(folder.account, canonical) == null)) {
                    me = false;
                    break;
                }
            }
            if (me)
                recipients = message.to;
        }

        if (recipients != null) {
            for (Address recipient : recipients) {
                final String email = ((InternetAddress) recipient).getAddress();
                final String name = ((InternetAddress) recipient).getPersonal();
                final Uri avatar = ContactInfo.getLookupUri(context, new Address[]{recipient});
                db.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        EntityContact contact = db.contact().getContact(folder.account, type, email);
                        if (contact == null) {
                            contact = new EntityContact();
                            contact.account = folder.account;
                            contact.type = type;
                            contact.email = email;
                            contact.name = name;
                            contact.avatar = (avatar == null ? null : avatar.toString());
                            contact.times_contacted = 1;
                            contact.first_contacted = message.received;
                            contact.last_contacted = message.received;
                            contact.id = db.contact().insertContact(contact);
                            Log.i("Inserted contact=" + contact + " type=" + type);
                        } else {
                            contact.name = name;
                            contact.avatar = (avatar == null ? null : avatar.toString());
                            contact.times_contacted++;
                            contact.first_contacted = Math.min(contact.first_contacted, message.received);
                            contact.last_contacted = message.received;
                            db.contact().updateContact(contact);
                            Log.i("Updated contact=" + contact + " type=" + type);
                        }
                    }
                });
            }
        }
    }

    static void downloadMessage(
            Context context,
            EntityFolder folder, IMAPFolder ifolder,
            IMAPMessage imessage, long id, State state) throws MessagingException, IOException {
        if (state.getNetworkState().isRoaming())
            return;

        DB db = DB.getInstance(context);
        EntityMessage message = db.message().getMessage(id);
        if (message == null)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long maxSize = prefs.getInt("download", MessageHelper.DEFAULT_ATTACHMENT_DOWNLOAD_SIZE);
        if (maxSize == 0)
            maxSize = Long.MAX_VALUE;

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
        MessageHelper helper = new MessageHelper(imessage);

        boolean fetch = false;
        if (!message.content)
            if (state.getNetworkState().isUnmetered() || (message.size != null && message.size < maxSize))
                fetch = true;

        if (!fetch)
            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    if (state.getNetworkState().isUnmetered() || (attachment.size != null && attachment.size < maxSize)) {
                        fetch = true;
                        break;
                    }

        if (fetch) {
            Log.i(folder.name + " fetching message id=" + message.id);

            // Fetch on demand to prevent OOM

            //FetchProfile fp = new FetchProfile();
            //fp.add(FetchProfile.Item.ENVELOPE);
            //fp.add(FetchProfile.Item.FLAGS);
            //fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            //fp.add(UIDFolder.FetchProfileItem.UID);
            //fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
            //fp.add(FetchProfile.Item.SIZE);
            //fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            //ifolder.fetch(new Message[]{imessage}, fp);

            MessageHelper.MessageParts parts = helper.getMessageParts();

            if (!message.content) {
                if (state.getNetworkState().isUnmetered() || (message.size != null && message.size < maxSize)) {
                    String body = parts.getHtml(context);
                    Helper.writeText(message.getFile(context), body);
                    db.message().setMessageContent(message.id,
                            true,
                            parts.isPlainOnly(),
                            HtmlHelper.getPreview(body),
                            parts.getWarnings(message.warning));
                    Log.i(folder.name + " downloaded message id=" + message.id + " size=" + message.size);
                }
            }

            List<EntityAttachment> remotes = parts.getAttachments();

            for (EntityAttachment local : attachments)
                if (!local.available)
                    if (state.getNetworkState().isUnmetered() || (local.size != null && local.size < maxSize))
                        try {
                            boolean found = false;
                            for (int i = 0; i < remotes.size(); i++) {
                                EntityAttachment remote = remotes.get(i);
                                if (Objects.equals(remote.name, local.name) &&
                                        Objects.equals(remote.type, local.type) &&
                                        Objects.equals(remote.disposition, local.disposition) &&
                                        Objects.equals(remote.cid, local.cid) &&
                                        Objects.equals(remote.encryption, local.encryption) &&
                                        Objects.equals(remote.size, local.size)) {
                                    found = true;
                                    parts.downloadAttachment(context, i, local.id, local.name);
                                }
                            }

                            if (!found) {
                                Log.w("Attachment not found local=" + local);
                                for (EntityAttachment remote : remotes)
                                    Log.w("Attachment remote=" + remote);
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
        }
    }

    static void notifyReset(Context context) {
        lastUnseen = -1;
        Widget.update(context, -1);
        try {
            ShortcutBadger.removeCount(context);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void notifyMessages(Context context, Map<String, List<Long>> groupNotifying, List<TupleMessageEx> messages) {
        Log.i("Notify messages=" + messages.size());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean badge = prefs.getBoolean("badge", true);
        boolean pro = Helper.isPro(context);

        // Update widget/badge count
        if (lastUnseen < 0 || messages.size() != lastUnseen) {
            lastUnseen = messages.size();
            Widget.update(context, messages.size());
            try {
                ShortcutBadger.applyCount(context, badge ? messages.size() : 0);
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        // Current
        Map<String, List<TupleMessageEx>> groupMessages = new HashMap<>();
        for (TupleMessageEx message : messages) {
            // Check if notification channel enabled
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O &&
                    message.from != null && message.from.length > 0) {
                InternetAddress from = (InternetAddress) message.from[0];
                NotificationChannel channel = nm.getNotificationChannel("notification." + from.getAddress().toLowerCase());
                if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE)
                    continue;
            }

            String group = Long.toString(pro && message.accountNotify ? message.account : 0);
            if (!groupMessages.containsKey(group)) {
                groupMessages.put(group, new ArrayList<TupleMessageEx>());
                if (!groupNotifying.containsKey(group))
                    groupNotifying.put(group, new ArrayList<Long>());
            }

            // This assumes the messages are properly ordered
            if (groupMessages.get(group).size() < MAX_NOTIFICATION_COUNT)
                groupMessages.get(group).add(message);
        }

        // Difference
        for (String group : groupNotifying.keySet()) {
            List<Notification> notifications = getNotificationUnseen(context, group, groupMessages.get(group));

            List<Long> all = new ArrayList<>();
            List<Long> add = new ArrayList<>();
            List<Long> remove = groupNotifying.get(group);
            for (Notification notification : notifications) {
                Long id = notification.extras.getLong("id", 0);
                if (id != 0) {
                    all.add(id);
                    if (remove.contains(id)) {
                        remove.remove(id);
                        Log.i("Notify existing=" + id);
                    } else {
                        remove.remove(-id);
                        add.add(id);
                        Log.i("Notify adding=" + id);
                    }
                }
            }

            int headers = 0;
            for (Long id : add)
                if (id < 0)
                    headers++;

            Log.i("Notify group=" + group + " count=" + notifications.size() +
                    " added=" + add.size() + " removed=" + remove.size() + " headers=" + headers);

            if (notifications.size() == 0 ||
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && headers > 0)) {
                String tag = "unseen." + group + ":0";
                Log.i("Cancelling tag=" + tag);
                nm.cancel(tag, 1);
            }

            for (Long id : remove) {
                String tag = "unseen." + group + ":" + Math.abs(id);
                Log.i("Cancelling tag=" + tag);
                nm.cancel(tag, 1);
            }

            for (Notification notification : notifications) {
                long id = notification.extras.getLong("id", 0);
                if ((id == 0 && add.size() + remove.size() > 0) || add.contains(id)) {
                    String tag = "unseen." + group + ":" + Math.abs(id);
                    Log.i("Notifying tag=" + tag +
                            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? "" : " channel=" + notification.getChannelId()));
                    nm.notify(tag, 1, notification);
                }
            }

            groupNotifying.put(group, all);
        }
    }

    private static List<Notification> getNotificationUnseen(Context context, String group, List<TupleMessageEx> messages) {
        List<Notification> notifications = new ArrayList<>();
        // https://developer.android.com/training/notify-user/group

        if (messages == null || messages.size() == 0)
            return notifications;

        boolean pro = Helper.isPro(context);
        boolean canGroup = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean flags = prefs.getBoolean("flags", true);
        boolean notify_group = (prefs.getBoolean("notify_group", true) && canGroup);
        boolean notify_preview = prefs.getBoolean("notify_preview", true);
        boolean notify_trash = prefs.getBoolean("notify_trash", true);
        boolean notify_archive = prefs.getBoolean("notify_archive", true);
        boolean notify_reply = prefs.getBoolean("notify_reply", false) && pro;
        boolean notify_flag = prefs.getBoolean("notify_flag", false) && pro;
        boolean notify_seen = prefs.getBoolean("notify_seen", true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Get contact info
        Map<TupleMessageEx, ContactInfo> messageContact = new HashMap<>();
        for (TupleMessageEx message : messages)
            messageContact.put(message, ContactInfo.get(context, message.from, false));

        if (notify_group) {
            // Build pending intents
            Intent summary = new Intent(context, ActivityView.class).setAction("unified");
            PendingIntent piSummary = PendingIntent.getActivity(context, ActivityView.REQUEST_UNIFIED, summary, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent clear = new Intent(context, ServiceUI.class).setAction("clear");
            PendingIntent piClear = PendingIntent.getService(context, ServiceUI.PI_CLEAR, clear, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build title
            String title = context.getResources().getQuantityString(
                    R.plurals.title_notification_unseen, messages.size(), messages.size());

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notification");
            builder
                    .setSmallIcon(R.drawable.baseline_email_white_24)
                    .setContentTitle(title)
                    .setContentIntent(piSummary)
                    .setNumber(messages.size())
                    .setShowWhen(false)
                    .setDeleteIntent(piClear)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setGroup(group)
                    .setGroupSummary(true);

            Notification pub = builder.build();
            builder
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setPublicVersion(pub);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setNotificationSoundAndLight(context, builder);
                builder.setOnlyAlertOnce(true);
            } else
                builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
            StringBuilder sb = new StringBuilder();
            for (EntityMessage message : messages) {
                sb.append("<strong>").append(messageContact.get(message).getDisplayName(true)).append("</strong>");
                if (!TextUtils.isEmpty(message.subject))
                    sb.append(": ").append(message.subject);
                sb.append(" ").append(df.format(message.received));
                sb.append("<br>");
            }

            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(HtmlHelper.fromHtml(sb.toString()))
                    .setSummaryText(title));

            notifications.add(builder.build());
        }

        // Message notifications
        for (TupleMessageEx message : messages) {
            ContactInfo info = messageContact.get(message);

            // Build arguments
            Bundle args = new Bundle();
            args.putLong("id", message.content ? message.id : -message.id);

            // Build pending intents
            Intent thread = new Intent(context, ActivityView.class);
            thread.setAction("thread:" + message.thread);
            thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            thread.putExtra("account", message.account);
            thread.putExtra("id", message.id);
            PendingIntent piContent = PendingIntent.getActivity(
                    context, ActivityView.REQUEST_THREAD, thread, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent ignore = new Intent(context, ServiceUI.class).setAction("ignore:" + message.id);
            PendingIntent piIgnore = PendingIntent.getService(context, ServiceUI.PI_IGNORED, ignore, PendingIntent.FLAG_UPDATE_CURRENT);

            // Get channel name
            String channelName = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                NotificationChannel channel = null;

                if (message.from != null && message.from.length > 0) {
                    InternetAddress from = (InternetAddress) message.from[0];
                    channel = nm.getNotificationChannel("notification." + from.getAddress().toLowerCase());
                }

                if (channel == null)
                    channel = nm.getNotificationChannel(EntityFolder.getNotificationChannelId(message.folder));

                if (channel != null)
                    channelName = channel.getId();
            }
            if (channelName == null)
                channelName = EntityAccount.getNotificationChannelId(
                        pro && message.accountNotify ? message.account : 0);

            // Get folder name
            String folderName = message.folderDisplay == null
                    ? Helper.localizeFolderName(context, message.folderName)
                    : message.folderDisplay;

            NotificationCompat.Builder mbuilder;
            mbuilder = new NotificationCompat.Builder(context, channelName);

            mbuilder
                    .addExtras(args)
                    .setSmallIcon(R.drawable.baseline_email_white_24)
                    .setContentTitle(info.getDisplayName(true))
                    .setSubText(message.accountName + " · " + folderName)
                    .setContentIntent(piContent)
                    .setWhen(message.received)
                    .setDeleteIntent(piIgnore)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setOnlyAlertOnce(true);

            if (notify_group)
                mbuilder.setGroup(group).setGroupSummary(false);

            if (notify_trash) {
                Intent trash = new Intent(context, ServiceUI.class)
                        .setAction("trash:" + message.id)
                        .putExtra("group", group);
                PendingIntent piTrash = PendingIntent.getService(context, ServiceUI.PI_TRASH, trash, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionTrash = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_delete_24,
                        context.getString(R.string.title_advanced_notify_action_trash),
                        piTrash);
                mbuilder.addAction(actionTrash.build());
            }

            if (notify_archive) {
                Intent archive = new Intent(context, ServiceUI.class)
                        .setAction("archive:" + message.id)
                        .putExtra("group", group);
                PendingIntent piArchive = PendingIntent.getService(context, ServiceUI.PI_ARCHIVE, archive, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionArchive = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_archive_24,
                        context.getString(R.string.title_advanced_notify_action_archive),
                        piArchive);
                mbuilder.addAction(actionArchive.build());
            }

            if (notify_reply && message.content) {
                Intent reply = new Intent(context, ActivityCompose.class)
                        .putExtra("action", args.getString("action"))
                        .putExtra("reference", message.id);
                reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent piReply = PendingIntent.getActivity(context, ActivityCompose.PI_REPLY, reply, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionReply = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_reply_24,
                        context.getString(R.string.title_advanced_notify_action_reply),
                        piReply);
                mbuilder.addAction(actionReply.build());
            }

            if (notify_flag && flags) {
                Intent flag = new Intent(context, ServiceUI.class)
                        .setAction("flag:" + message.id)
                        .putExtra("group", group);
                PendingIntent piFlag = PendingIntent.getService(context, ServiceUI.PI_FLAG, flag, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionFlag = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_star_24,
                        context.getString(R.string.title_advanced_notify_action_flag),
                        piFlag);
                mbuilder.addAction(actionFlag.build());
            }

            if (notify_seen) {
                Intent seen = new Intent(context, ServiceUI.class)
                        .setAction("seen:" + message.id)
                        .putExtra("group", group);
                PendingIntent piSeen = PendingIntent.getService(context, ServiceUI.PI_SEEN, seen, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionSeen = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_visibility_24,
                        context.getString(R.string.title_advanced_notify_action_seen),
                        piSeen);
                mbuilder.addAction(actionSeen.build());
            }

            if (!TextUtils.isEmpty(message.subject))
                mbuilder.setContentText(message.subject);

            if (message.content && notify_preview)
                try {
                    String body = Helper.readText(message.getFile(context));
                    StringBuilder sbm = new StringBuilder();
                    if (!TextUtils.isEmpty(message.subject))
                        sbm.append(message.subject).append("<br>");
                    String text = Jsoup.parse(body).text();
                    if (!TextUtils.isEmpty(text)) {
                        sbm.append("<em>");
                        if (text.length() > HtmlHelper.PREVIEW_SIZE) {
                            sbm.append(text.substring(0, HtmlHelper.PREVIEW_SIZE));
                            sbm.append("…");
                        } else
                            sbm.append(text);
                        sbm.append("</em>");
                    }
                    mbuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(HtmlHelper.fromHtml(sbm.toString())));
                } catch (IOException ex) {
                    Log.e(ex);
                    mbuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(ex.toString()));
                }

            if (info.hasPhoto())
                mbuilder.setLargeIcon(info.getPhotoBitmap());

            if (info.hasLookupUri())
                mbuilder.addPerson(info.getLookupUri().toString());

            if (pro && message.accountColor != null) {
                mbuilder.setColor(message.accountColor);
                mbuilder.setColorized(true);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                if (notify_group)
                    mbuilder.setSound(null);
                else
                    setNotificationSoundAndLight(context, mbuilder);
            } else {
                if (notify_group)
                    mbuilder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);
            }

            notifications.add(mbuilder.build());
        }

        return notifications;
    }

    private static void setNotificationSoundAndLight(Context context, NotificationCompat.Builder builder) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean light = prefs.getBoolean("light", false);
        String sound = prefs.getString("sound", null);

        if (light)
            builder.setLights(Color.GREEN, 1000, 1000);

        Uri uri = (sound == null ? null : Uri.parse(sound));
        if (uri == null || "file".equals(uri.getScheme()))
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);
    }

    static void reportError(Context context, EntityAccount account, EntityFolder folder, Throwable ex) {
        // FolderClosedException: can happen when no connectivity

        // IllegalStateException:
        // - "This operation is not allowed on a closed folder"
        // - can happen when syncing message

        // ConnectionException
        // - failed to create new store connection (connectivity)

        // MailConnectException
        // - on connectivity problems when connecting to store

        String title;
        if (account == null)
            title = folder.name;
        else if (folder == null)
            title = account.name;
        else
            title = account.name + "/" + folder.name;

        String tag = "error:" + (account == null ? 0 : account.id) + ":" + (folder == null ? 0 : folder.id);

        EntityLog.log(context, title + " " + Helper.formatThrowable(ex));

        if (ex instanceof AuthenticationFailedException || // Also: Too many simultaneous connections
                ex instanceof AlertException ||
                ex instanceof SendFailedException) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(tag, 1, getNotificationError(context, title, ex).build());
        }

        // connection failure: Too many simultaneous connections

        if (BuildConfig.DEBUG &&
                !(ex instanceof SendFailedException) &&
                !(ex instanceof MailConnectException) &&
                !(ex instanceof FolderClosedException) &&
                !(ex instanceof IllegalStateException) &&
                !(ex instanceof StoreClosedException) &&
                !(ex instanceof UnknownHostException) &&
                !(ex instanceof MessageRemovedException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof UnknownHostException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof ConnectionException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof SocketException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof SocketTimeoutException) &&
                !(ex instanceof MessagingException && ex.getCause() instanceof SSLException) &&
                !(ex instanceof MessagingException && "connection failure".equals(ex.getMessage()))) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(tag, 1, getNotificationError(context, title, ex).build());
        }
    }

    static NotificationCompat.Builder getNotificationError(Context context, String title, Throwable ex) {
        return getNotificationError(context, "error", title, ex, true);
    }

    static NotificationCompat.Builder getNotificationError(Context context, String channel, String title, Throwable ex, boolean debug) {
        // Build pending intent
        Intent intent = new Intent(context, ActivityView.class);
        if (debug)
            intent.setAction("error");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                context, ActivityView.REQUEST_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel);

        builder
                .setSmallIcon(R.drawable.baseline_warning_white_24)
                .setContentTitle(context.getString(R.string.title_notification_failed, title))
                .setContentText(Helper.formatThrowable(ex))
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(Helper.formatThrowable(ex, false, "\n")));

        return builder;
    }

    static class AlertException extends Throwable {
        private String alert;

        AlertException(String alert) {
            this.alert = alert;
        }

        @Override
        public String getMessage() {
            return alert;
        }
    }

    static class State {
        private ConnectionHelper.NetworkState networkState;
        private Thread thread;
        private Semaphore semaphore = new Semaphore(0);
        private boolean running = true;
        private boolean recoverable = true;

        State(ConnectionHelper.NetworkState networkState) {
            this.networkState = networkState;
        }

        State(State parent) {
            this(parent.networkState);
        }

        ConnectionHelper.NetworkState getNetworkState() {
            return networkState;
        }

        void runnable(Runnable runnable, String name) {
            thread = new Thread(runnable, name);
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
        }

        void release() {
            semaphore.release();
            yield();
        }

        void acquire() throws InterruptedException {
            semaphore.acquire();
        }

        boolean acquire(long milliseconds) throws InterruptedException {
            return semaphore.tryAcquire(milliseconds, TimeUnit.MILLISECONDS);
        }

        void error(Throwable ex) {
            if (ex instanceof MessagingException &&
                    (ex.getCause() instanceof SocketException ||
                            ex.getCause() instanceof ConnectionException))
                recoverable = false;

            if (ex instanceof FolderClosedException)
                recoverable = false;

            if (ex instanceof IllegalStateException && (
                    "Not connected".equals(ex.getMessage()) ||
                            "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                recoverable = false;

            thread.interrupt();
            yield();
        }

        void reset() {
            recoverable = true;
        }

        private void yield() {
            try {
                // Give interrupted thread some time to acquire wake lock
                Thread.sleep(YIELD_DURATION);
            } catch (InterruptedException ignored) {
            }
        }

        void start() {
            thread.start();
        }

        void stop() {
            running = false;
            semaphore.release();
        }

        void join() {
            join(thread);
        }

        boolean running() {
            return running;
        }

        boolean recoverable() {
            return recoverable;
        }

        void join(Thread thread) {
            boolean joined = false;
            while (!joined)
                try {
                    Log.i("Joining " + thread.getName());
                    thread.join();
                    joined = true;
                    Log.i("Joined " + thread.getName());
                } catch (InterruptedException ex) {
                    Log.w(thread.getName() + " join " + ex.toString());
                }
        }

        @NonNull
        @Override
        public String toString() {
            return "[running=" + running + ",recoverable=" + recoverable + "]";
        }
    }
}
