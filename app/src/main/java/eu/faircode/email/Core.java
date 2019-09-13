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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.MessageRemovedIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
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
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static androidx.core.app.NotificationCompat.DEFAULT_LIGHTS;
import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;

class Core {
    private static int lastUnseen = -1;
    private static ConcurrentMap<Long, Long> lockFolders = new ConcurrentHashMap<>();

    private static final int MAX_NOTIFICATION_COUNT = 100; // per group
    private static final int SYNC_CHUNCK_SIZE = 200;
    private static final int SYNC_BATCH_SIZE = 20;
    private static final int DOWNLOAD_BATCH_SIZE = 20;
    private static final long YIELD_DURATION = 200L; // milliseconds
    private static final long MIN_HIDE = 60 * 1000L; // milliseconds

    static void processOperations(
            Context context,
            EntityAccount account, EntityFolder folder,
            Store istore, Folder ifolder,
            State state)
            throws MessagingException, JSONException, IOException {
        try {
            Log.i(folder.name + " start process");

            DB db = DB.getInstance(context);
            List<EntityOperation> ops = db.operation().getOperations(folder.id);
            Log.i(folder.name + " pending operations=" + ops.size());
            for (int i = 0; i < ops.size() && state.isRunning() && state.isRecoverable(); i++) {
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
                    crumb.put("folder", op.account + ":" + op.folder + ":" + folder.type);
                    if (op.message != null)
                        crumb.put("message", Long.toString(op.message));
                    crumb.put("free", Integer.toString(Log.getFreeMemMb()));
                    Log.breadcrumb("operation", crumb);

                    // Fetch most recent copy of message
                    EntityMessage message = null;
                    if (op.message != null)
                        message = db.message().getMessage(op.message);

                    JSONArray jargs = new JSONArray(op.args);

                    try {
                        db.operation().setOperationError(op.id, null);
                        if (!EntityOperation.SYNC.equals(op.name))
                            db.operation().setOperationState(op.id, "executing");

                        if (message == null) {
                            if (!EntityOperation.SYNC.equals(op.name) &&
                                    !EntityOperation.SUBSCRIBE.equals(op.name))
                                throw new MessageRemovedException();
                        } else {
                            db.message().setMessageError(message.id, null);
                            ensureUid(context, folder, message, op, (IMAPFolder) ifolder);
                        }

                        // Operations should use database transaction when needed

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
                                    if (next.message != null &&
                                            next.message.equals(op.message) &&
                                            (EntityOperation.ADD.equals(next.name) ||
                                                    EntityOperation.DELETE.equals(next.name))) {
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
                                    onAdd(context, jargs, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.MOVE:
                                onMove(context, jargs, false, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder);
                                break;

                            case EntityOperation.COPY:
                                onMove(context, jargs, true, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder);
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
                        EntityLog.log(context, folder.name + " " + Helper.formatThrowable(ex, false));

                        db.operation().setOperationError(op.id, Helper.formatThrowable(ex));
                        if (message != null && !(ex instanceof IllegalArgumentException))
                            db.message().setMessageError(message.id, Helper.formatThrowable(ex));

                        if (ex instanceof OutOfMemoryError ||
                                ex instanceof MessageRemovedException ||
                                ex instanceof MessageRemovedIOException ||
                                ex instanceof FileNotFoundException ||
                                ex instanceof FolderNotFoundException ||
                                ex instanceof IllegalArgumentException ||
                                ex.getCause() instanceof BadCommandException ||
                                ex.getCause() instanceof CommandFailedException) {
                            // com.sun.mail.iap.BadCommandException: B13 BAD [TOOBIG] Message too large
                            // com.sun.mail.iap.CommandFailedException: AY3 NO [CANNOT] Cannot APPEND to a SPAM folder
                            // com.sun.mail.iap.CommandFailedException: B16 NO [ALERT] Cannot MOVE messages out of the Drafts folder
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
                                    db.message().setMessageUiHide(message.id, 0L);
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

    private static void ensureUid(Context context, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws MessagingException {
        if (message.uid != null)
            return;
        if (EntityOperation.ADD.equals(op.name))
            return;
        if (EntityOperation.DELETE.equals(op.name) && !TextUtils.isEmpty(message.msgid))
            return;

        Log.i(folder.name + " ensure uid op=" + op.name + " msgid=" + message.msgid);

        if (TextUtils.isEmpty(message.msgid))
            throw new IllegalArgumentException("Message without ID for " + op.name);

        Message[] imessages = ifolder.search(new MessageIDTerm(message.msgid));
        if (imessages == null || imessages.length == 0)
            throw new IllegalArgumentException("Message not found for " + op.name);

        long uid = -1;
        for (Message iexisting : imessages) {
            long muid = ifolder.getUID(iexisting);
            Log.i(folder.name + " found uid=" + muid);
            // RFC3501: Unique identifiers are assigned in a strictly ascending fashion
            if (muid > uid)
                uid = muid;
        }

        message.uid = uid;

        DB db = DB.getInstance(context);
        db.message().setMessageUid(message.id, uid);
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

    private static void onAdd(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPStore istore, IMAPFolder ifolder) throws MessagingException, JSONException, IOException {
        // Add message
        DB db = DB.getInstance(context);

        // Drafts can change accounts
        if (jargs.length() == 0 && !folder.id.equals(message.folder))
            throw new IllegalArgumentException("Message folder changed");

        // Get arguments
        long target = jargs.optLong(0, folder.id);
        boolean autoread = jargs.optBoolean(1, false);

        if (target != folder.id)
            throw new IllegalArgumentException("Invalid folder");

        // Prevent async deletion
        if (folder.id.equals(message.folder)) {
            if (message.uid != null)
                db.message().setMessageUid(message.id, null);
        }

        // External draft might have a uid only
        if (TextUtils.isEmpty(message.msgid)) {
            message.msgid = EntityMessage.generateMessageId();
            db.message().setMessageMsgId(message.id, message.msgid);
        }

        Properties props = MessageHelper.getSessionProperties();
        Session isession = Session.getInstance(props, null);

        // Get raw message
        MimeMessage imessage;
        if (folder.id.equals(message.folder)) {
            // Pre flight check
            if (!message.content)
                throw new IllegalArgumentException("Message body missing");

            imessage = MessageHelper.from(context, message, null, isession);
        } else {
            // Cross account move
            File file = message.getRawFile(context);
            if (!file.exists())
                throw new IllegalArgumentException("raw message file not found");

            Log.i(folder.name + " reading " + file);
            try (InputStream is = new FileInputStream(file)) {
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
        if (ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
            imessage.setFlag(Flags.Flag.DRAFT, EntityFolder.DRAFTS.equals(folder.type));

        // Add message
        ifolder.appendMessages(new Message[]{imessage});

        if (folder.id.equals(message.folder)) {
            // External draft might have a uid only
            if (message.uid != null) {
                Message iexisting = ifolder.getMessageByUID(message.uid);
                if (iexisting == null)
                    Log.w(folder.name + " existing not found uid=" + message.uid);
                else
                    try {
                        Log.i(folder.name + " deleting uid=" + message.uid);
                        iexisting.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessageRemovedException ignored) {
                        Log.w(folder.name + " existing gone uid=" + message.uid);
                    }
            }

            Log.i(folder.name + " searching for msgid=" + message.msgid);
            Message[] imessages = ifolder.search(new MessageIDTerm(message.msgid));
            if (imessages == null)
                Log.w(folder.name + " search for msgid=" + message.msgid + " returned null");
            else {
                long uid = -1;

                for (Message iexisting : imessages) {
                    long muid = ifolder.getUID(iexisting);
                    Log.i(folder.name + " found uid=" + muid);
                    // RFC3501: Unique identifiers are assigned in a strictly ascending fashion
                    if (muid > uid)
                        uid = muid;
                }

                if (uid < 0)
                    Log.w(folder.name + " appended msgid=" + message.msgid + " not found");
                else {
                    Log.i(folder.name + " appended uid=" + uid);

                    for (Message iexisting : imessages) {
                        long muid = ifolder.getUID(iexisting);
                        if (muid != uid)
                            try {
                                Log.i(folder.name + " deleting uid=" + muid);
                                iexisting.setFlag(Flags.Flag.DELETED, true);
                            } catch (MessageRemovedException ignored) {
                                Log.w(folder.name + " existing gone uid=" + muid);
                            }
                    }
                }
            }

            ifolder.expunge();
        } else {
            // Mark source read
            if (autoread)
                EntityOperation.queue(context, message, EntityOperation.SEEN, true);

            // Delete source
            EntityOperation.queue(context, message, EntityOperation.DELETE);
        }
    }

    private static void onMove(Context context, JSONArray jargs, boolean copy, EntityFolder folder, EntityMessage message, IMAPStore istore, IMAPFolder ifolder) throws JSONException, MessagingException, IOException {
        // Move message
        DB db = DB.getInstance(context);

        // Get arguments
        long id = jargs.getLong(0);
        boolean autoread = jargs.optBoolean(1, false);

        // Get source message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Get target folder
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();
        IMAPFolder itarget = (IMAPFolder) istore.getFolder(target.name);

        if (EntityFolder.DRAFTS.equals(folder.type) || EntityFolder.DRAFTS.equals(target.type)) {
            Log.i(folder.name + " move from " + folder.type + " to " + target.type);

            File file = message.getRawFile(context);
            try (OutputStream os = new FileOutputStream(file)) {
                imessage.writeTo(os);
            }

            Properties props = MessageHelper.getSessionProperties();
            Session isession = Session.getInstance(props, null);

            Message icopy;
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                icopy = new MimeMessage(isession, is);
            }

            file.delete();

            // Auto read
            if (autoread)
                icopy.setFlag(Flags.Flag.SEEN, true);

            // Set drafts flag
            icopy.setFlag(Flags.Flag.DRAFT, EntityFolder.DRAFTS.equals(target.type));

            itarget.appendMessages(new Message[]{icopy});
        } else {
            // Auto read
            if (autoread && ifolder.getPermanentFlags().contains(Flags.Flag.SEEN))
                imessage.setFlag(Flags.Flag.SEEN, true);

            ifolder.copyMessages(new Message[]{imessage}, itarget);
        }

        // Delete source
        if (!copy) {
            try {
                imessage.setFlag(Flags.Flag.DELETED, true);
            } catch (MessageRemovedException ignored) {
            }
            ifolder.expunge();
        }

        // Delete junk contacts
        if (EntityFolder.JUNK.equals(target.type)) {
            Address[] recipients = (message.reply != null ? message.reply : message.from);
            if (recipients != null)
                for (Address recipient : recipients) {
                    String email = ((InternetAddress) recipient).getAddress();
                    int count = db.contact().deleteContact(target.account, EntityContact.TYPE_FROM, email);
                    Log.i("Deleted contact email=" + email + " count=" + count);
                }
        }
    }

    private static void onDelete(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException {
        // Delete message
        DB db = DB.getInstance(context);

        boolean deleted = false;

        if (message.uid != null) {
            Message iexisting = ifolder.getMessageByUID(message.uid);
            if (iexisting == null)
                Log.w(folder.name + " existing not found uid=" + message.uid);
            else
                try {
                    Log.i(folder.name + " deleting uid=" + message.uid);
                    iexisting.setFlag(Flags.Flag.DELETED, true);
                    deleted = true;
                } catch (MessageRemovedException ignored) {
                    Log.w(folder.name + " existing gone uid=" + message.uid);
                }
        }

        if (!TextUtils.isEmpty(message.msgid) && !deleted) {
            Message[] imessages = ifolder.search(new MessageIDTerm(message.msgid));
            if (imessages == null)
                Log.w(folder.name + " search for msgid=" + message.msgid + " returned null");
            else
                for (Message iexisting : imessages) {
                    long muid = ifolder.getUID(iexisting);
                    Log.i(folder.name + " deleting uid=" + muid);
                    try {
                        iexisting.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessageRemovedException ignored) {
                        Log.w(folder.name + " existing gone uid=" + muid);
                    }
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
            try (OutputStream os = new FileOutputStream(file)) {
                imessage.writeTo(os);
                db.message().setMessageRaw(message.id, true);
            }
        }

        if (jargs.length() > 0) {
            // Cross account move
            long target = jargs.getLong(0);
            Log.i(folder.name + " queuing ADD id=" + message.id + ":" + target);

            EntityOperation operation = new EntityOperation();
            operation.account = message.account;
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

        if (!TextUtils.isEmpty(body))
            fixAttachments(context, message.id, body);
    }

    private static void onAttachment(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws JSONException, MessagingException, IOException {
        // Download attachment
        DB db = DB.getInstance(context);

        long id = jargs.getLong(0);

        // Get attachment
        EntityAttachment attachment = db.attachment().getAttachment(id);
        if (attachment == null)
            attachment = db.attachment().getAttachment(message.id, (int) id); // legacy
        if (attachment == null)
            throw new IllegalArgumentException("Local attachment not found");
        if (attachment.available)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Get message parts
        MessageHelper helper = new MessageHelper((MimeMessage) imessage);
        MessageHelper.MessageParts parts = helper.getMessageParts();

        // Download attachment
        parts.downloadAttachment(context, attachment);
    }

    static void onSynchronizeFolders(Context context, EntityAccount account, Store istore, State state) throws MessagingException {
        DB db = DB.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean subscribed_only = prefs.getBoolean("subscribed_only", false);
        boolean sync_folders = prefs.getBoolean("sync_folders", true);

        // Get folder names
        Map<String, EntityFolder> local = new HashMap<>();
        for (EntityFolder folder : db.folder().getFolders(account.id, false, false))
            if (folder.tbc != null) {
                Log.i(folder.name + " creating");
                Folder ifolder = istore.getFolder(folder.name);
                if (!ifolder.exists()) {
                    ifolder.create(Folder.HOLDS_MESSAGES);
                    if (subscribed_only)
                        ifolder.setSubscribed(true);
                }
                db.folder().resetFolderTbc(folder.id);
                local.put(folder.name, folder);
                sync_folders = true;

            } else if (folder.rename != null) {
                Log.i(folder.name + " rename into " + folder.rename);
                Folder ifolder = istore.getFolder(folder.name);
                if (ifolder.exists()) {
                    // https://tools.ietf.org/html/rfc3501#section-6.3.9
                    boolean subscribed = ifolder.isSubscribed();
                    ifolder.setSubscribed(false);
                    ifolder.renameTo(istore.getFolder(folder.rename));
                    ifolder.setSubscribed(subscribed);
                    db.folder().renameFolder(folder.account, folder.name, folder.rename);
                    folder.name = folder.rename;
                }
                db.folder().resetFolderRename(folder.id);
                sync_folders = true;

            } else if (folder.tbd != null && folder.tbd) {
                Log.i(folder.name + " deleting");
                Folder ifolder = istore.getFolder(folder.name);
                if (ifolder.exists()) {
                    ifolder.setSubscribed(false);
                    ifolder.delete(false);
                }
                db.folder().deleteFolder(folder.id);
                sync_folders = true;

            } else {
                local.put(folder.name, folder);
                if (folder.initialize != 0)
                    sync_folders = true;
            }
        Log.i("Local folder count=" + local.size());

        if (!sync_folders)
            return;

        Log.i("Start sync folders account=" + account.name);

        // Get default folder
        Folder defaultFolder = istore.getDefaultFolder();
        char separator = defaultFolder.getSeparator();
        EntityLog.log(context, account.name + " folder separator=" + separator);
        db.account().setFolderSeparator(account.id, separator);

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
            String[] name = fullName.split(Pattern.quote(Character.toString(separator)));
            String childName = name[name.length - 1];
            boolean subscribed = subscription.contains(fullName);
            String[] attr = ((IMAPFolder) ifolder).getAttributes();
            String type = EntityFolder.getType(attr, fullName, false);
            boolean selectable = !Arrays.asList(attr).contains("\\Noselect") &&
                    ((ifolder.getType() & IMAPFolder.HOLDS_MESSAGES) != 0);

            if (EntityFolder.INBOX.equals(type) || fullName.equals(childName))
                childName = null;

            Log.i(account.name + ":" + fullName + " subscribed=" + subscribed +
                    " type=" + type + " attrs=" + TextUtils.join(" ", attr));

            if (type != null) {
                local.remove(fullName);

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
                        folder.selectable = selectable;
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

        Log.i("Delete local count=" + local.size());
        for (String name : local.keySet()) {
            EntityFolder folder = local.get(name);
            if (EntityFolder.USER.equals(folder.type)) {
                Log.i(name + " delete");
                db.folder().deleteFolder(account.id, name);
            } else
                Log.i(name + " keep type=" + folder.type);
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
            boolean download = jargs.optBoolean(2, false);
            boolean auto_delete = jargs.optBoolean(3, false);
            int initialize = jargs.optInt(4, folder.initialize);

            if (keep_days == sync_days && keep_days != Integer.MAX_VALUE)
                keep_days++;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean sync_unseen = prefs.getBoolean("sync_unseen", false);
            boolean sync_flagged = prefs.getBoolean("sync_flagged", false);
            boolean sync_kept = prefs.getBoolean("sync_kept", true);
            boolean delete_unseen = prefs.getBoolean("delete_unseen", false);

            if (account.host.toLowerCase().contains("imap.zoho")) {
                sync_unseen = false;
                sync_flagged = false;
            }

            Log.i(folder.name + " start sync after=" + sync_days + "/" + keep_days +
                    " sync unseen=" + sync_unseen + " flagged=" + sync_flagged +
                    " delete unseen=" + delete_unseen + " kept=" + sync_kept);

            db.folder().setFolderSyncState(folder.id, "syncing");

            // Check uid validity
            try {
                long uidv = ifolder.getUIDValidity();
                if (folder.uidv != null && !folder.uidv.equals(uidv)) {
                    Log.w(folder.name + " uid validity changed from " + folder.uidv + " to " + uidv);
                    db.message().deleteLocalMessages(folder.id);
                }
                folder.uidv = uidv;
                db.folder().setFolderUidValidity(folder.id, uidv);
            } catch (MessagingException ex) {
                Log.w(ex);
            }

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
            if (auto_delete) {
                List<Long> tbds = db.message().getMessagesBefore(folder.id, keep_time, delete_unseen);
                Log.i(folder.name + " local tbd=" + tbds.size());
                EntityFolder trash = db.folder().getFolderByType(folder.account, EntityFolder.TRASH);
                for (Long tbd : tbds) {
                    EntityMessage message = db.message().getMessage(tbd);
                    if (message != null && trash != null)
                        if (EntityFolder.TRASH.equals(folder.type))
                            EntityOperation.queue(context, message, EntityOperation.DELETE);
                        else
                            EntityOperation.queue(context, message, EntityOperation.MOVE, trash.id);
                }
            } else {
                int old = db.message().deleteMessagesBefore(folder.id, keep_time, delete_unseen);
                Log.i(folder.name + " local old=" + old);
            }

            // Get list of local uids
            final List<Long> uids = db.message().getUids(folder.id, sync_kept ? null : sync_time);
            Log.i(folder.name + " local count=" + uids.size());

            // Reduce list of local uids
            SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time));
            if (sync_unseen && ifolder.getPermanentFlags().contains(Flags.Flag.SEEN))
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

            for (int i = 0; i < imessages.length && state.isRunning() && state.isRecoverable(); i++)
                try {
                    if (!imessages[i].isSet(Flags.Flag.DELETED))
                        uids.remove(ifolder.getUID(imessages[i]));
                } catch (MessageRemovedException ex) {
                    Log.w(folder.name, ex);
                } catch (Throwable ex) {
                    Log.e(folder.name, ex);
                    EntityLog.log(context, folder.name + " " + Helper.formatThrowable(ex, false));
                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                }

            if (uids.size() > 0) {
                // This is done outside of JavaMail to prevent changed notifications
                if (!ifolder.isOpen())
                    throw new FolderClosedException(ifolder, "UID FETCH");
                MessagingException ex = (MessagingException) ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                    @Override
                    public Object doCommand(IMAPProtocol protocol) {
                        // Build ranges
                        List<Pair<Long, Long>> ranges = new ArrayList<>();
                        long first = -1;
                        long last = -1;
                        for (long uid : uids)
                            if (first < 0)
                                first = uid;
                            else if ((last < 0 ? first : last) + 1 == uid)
                                last = uid;
                            else {
                                ranges.add(new Pair<>(first, last < 0 ? first : last));
                                first = uid;
                                last = -1;
                            }
                        if (first > 0)
                            ranges.add(new Pair<>(first, last < 0 ? first : last));

                        List<List<Pair<Long, Long>>> chunks = Helper.chunkList(ranges, SYNC_CHUNCK_SIZE);

                        Log.i(folder.name + " executing uid fetch count=" + uids.size() +
                                " ranges=" + ranges.size() + " chunks=" + chunks.size());
                        for (int c = 0; c < chunks.size(); c++) {
                            List<Pair<Long, Long>> chunk = chunks.get(c);
                            Log.i(folder.name + " chunk #" + c + " size=" + chunk.size());

                            StringBuilder sb = new StringBuilder();
                            for (Pair<Long, Long> range : chunk) {
                                if (sb.length() > 0)
                                    sb.append(',');
                                if (range.first.equals(range.second))
                                    sb.append(range.first);
                                else
                                    sb.append(range.first).append(':').append(range.second);
                            }
                            String command = "UID FETCH " + sb + " (UID FLAGS)";
                            Response[] responses = protocol.command(command, null);

                            if (responses.length > 0 && responses[responses.length - 1].isOK()) {
                                for (Response response : responses)
                                    if (response instanceof FetchResponse) {
                                        FetchResponse fr = (FetchResponse) response;
                                        UID uid = fr.getItem(UID.class);
                                        FLAGS flags = fr.getItem(FLAGS.class);
                                        if (uid != null && (flags == null || !flags.contains(Flags.Flag.DELETED)))
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
            for (int i = imessages.length - 1; i >= 0 && state.isRunning() && state.isRecoverable(); i -= SYNC_BATCH_SIZE) {
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
                Log.breadcrumb("sync", crumb);
                Log.i("Sync " + from + ".." + i + " free=" + free);

                for (int j = isub.length - 1; j >= 0 && state.isRunning() && state.isRecoverable(); j--)
                    try {
                        // Some providers, like Zoho, erroneously return old messages
                        if (full.contains(isub[j])) {
                            Date received = isub[j].getReceivedDate();
                            boolean unseen = (sync_unseen && !isub[j].isSet(Flags.Flag.SEEN));
                            boolean flagged = (sync_flagged && isub[j].isSet(Flags.Flag.FLAGGED));
                            if (received != null && received.getTime() < keep_time && !unseen && !flagged) {
                                long uid = ifolder.getUID(isub[j]);
                                Log.i(folder.name + " Skipping old uid=" + uid + " date=" + received);
                                ids[from + j] = null;
                                continue;
                            }
                        }

                        EntityMessage message = synchronizeMessage(
                                context,
                                account, folder,
                                ifolder, (IMAPMessage) isub[j],
                                false, download,
                                rules, state);
                        ids[from + j] = message.id;
                    } catch (MessageRemovedException ex) {
                        Log.w(folder.name, ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(folder.name, ex);
                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(folder.name, ex);
                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                    } finally {
                        // Free memory
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }
            }

            // Add local sent messages to remote sent folder
            if (EntityFolder.SENT.equals(folder.type)) {
                List<EntityMessage> orphans = db.message().getOrphans(folder.id);
                Log.i(folder.name + " sent orphans=" + orphans.size());
                for (EntityMessage orphan : orphans) {
                    Log.i(folder.name + " adding orphan id=" + orphan.id);
                    if (orphan.content && orphan.ui_hide == 0L)
                        EntityOperation.queue(context, orphan, EntityOperation.ADD);
                }
            } else {
                // Delete not synchronized messages without uid
                db.message().deleteOrphans(folder.id);
            }

            int count = ifolder.getMessageCount();
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

            if (download && initialize == 0) {
                db.folder().setFolderSyncState(folder.id, "downloading");

                // Download messages/attachments
                Log.i(folder.name + " download=" + imessages.length);
                for (int i = imessages.length - 1; i >= 0 && state.isRunning() && state.isRecoverable(); i -= DOWNLOAD_BATCH_SIZE) {
                    int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);

                    Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                    // Fetch on demand

                    int free = Log.getFreeMemMb();
                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("start", Integer.toString(from));
                    crumb.put("end", Integer.toString(i));
                    crumb.put("free", Integer.toString(free));
                    Log.breadcrumb("download", crumb);
                    Log.i("Download " + from + ".." + i + " free=" + free);

                    for (int j = isub.length - 1; j >= 0 && state.isRunning() && state.isRecoverable(); j--)
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

            if (state.running && initialize != 0) {
                jargs.put(4, 0);
                folder.initialize = 0;
                db.folder().setFolderInitialize(folder.id, 0);

                // Schedule download
                if (download) {
                    EntityOperation operation = new EntityOperation();
                    operation.account = folder.account;
                    operation.folder = folder.id;
                    operation.message = null;
                    operation.name = EntityOperation.SYNC;
                    operation.args = jargs.toString();
                    operation.created = new Date().getTime();
                    operation.id = db.operation().insertOperation(operation);
                }
            }

            db.folder().setFolderLastSync(folder.id, new Date().getTime());
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
            boolean browsed, boolean download,
            List<EntityRule> rules, State state) throws MessagingException, IOException {
        // Instead of locking the database while performing message I/O
        lockFolders.putIfAbsent(folder.id, folder.id);
        synchronized (lockFolders.get(folder.id)) {
            long uid = ifolder.getUID(imessage);

            if (imessage.isExpunged()) {
                Log.i(folder.name + " expunged uid=" + uid);
                throw new MessageRemovedException("Expunged");
            }
            if (imessage.isSet(Flags.Flag.DELETED)) {
                Log.i(folder.name + " deleted uid=" + uid);
                throw new MessageRemovedException("Flagged deleted");
            }

            MessageHelper helper = new MessageHelper(imessage);
            boolean seen = helper.getSeen();
            boolean answered = helper.getAnsered();
            boolean flagged = helper.getFlagged();
            String flags = helper.getFlags();
            String[] keywords = helper.getKeywords();
            boolean update = false;
            boolean process = false;

            DB db = DB.getInstance(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

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

                    if (dup.folder.equals(folder.id)) {
                        String thread = helper.getThreadId(context, account.id, uid);
                        Log.i(folder.name + " found as id=" + dup.id +
                                " uid=" + dup.uid + "/" + uid +
                                " msgid=" + msgid + " thread=" + thread);

                        if (dup.uid == null) {
                            Log.i(folder.name + " set uid=" + uid);
                            dup.uid = uid;
                            dup.thread = thread;

                            if (dup.size == null)
                                dup.size = helper.getSize();

                            if (EntityFolder.SENT.equals(folder.type)) {
                                dup.received = helper.getReceived();
                                dup.sent = helper.getSent();
                            }

                            dup.error = null;

                            message = dup;
                            process = true;
                        }
                    }
                }
            }

            if (message == null) {
                String authentication = helper.getAuthentication();
                MessageHelper.MessageParts parts = helper.getMessageParts();

                message = new EntityMessage();
                message.account = folder.account;
                message.folder = folder.id;
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
                message.from = helper.getFrom();
                message.to = helper.getTo();
                message.cc = helper.getCc();
                message.bcc = helper.getBcc();
                message.reply = helper.getReply();
                message.list_post = helper.getListPost();
                message.unsubscribe = helper.getListUnsubscribe();
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
                message.ui_hide = 0L;
                message.ui_found = false;
                message.ui_ignored = seen;
                message.ui_browsed = browsed;

                EntityIdentity identity = matchIdentity(context, folder, message);
                message.identity = (identity == null ? null : identity.id);

                message.sender = MessageHelper.getSortKey(message.from);
                Uri lookupUri = ContactInfo.getLookupUri(context, message.from);
                message.avatar = (lookupUri == null ? null : lookupUri.toString());

                boolean check_mx = prefs.getBoolean("check_mx", false);
                if (check_mx)
                    try {
                        if (ConnectionHelper.lookupMx(
                                message.reply == null || message.reply.length == 0
                                        ? message.from : message.reply, context))
                            message.mx = true;
                    } catch (UnknownHostException ex) {
                        message.mx = false;
                        message.warning = ex.getMessage();
                    } catch (Throwable ex) {
                        Log.e(ex);
                        message.warning = Helper.formatThrowable(ex, false);
                    }

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

                    message.id = db.message().insertMessage(message);
                    Log.i(folder.name + " added id=" + message.id + " uid=" + message.uid);

                    int sequence = 1;
                    for (EntityAttachment attachment : parts.getAttachments()) {
                        Log.i(folder.name + " attachment seq=" + sequence +
                                " name=" + attachment.name + " type=" + attachment.type +
                                " cid=" + attachment.cid + " pgp=" + attachment.encryption +
                                " size=" + attachment.size);
                        attachment.message = message.id;
                        attachment.sequence = sequence++;
                        attachment.id = db.attachment().insertAttachment(attachment);
                    }

                    runRules(context, imessage, message, rules);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (message.received > account.created)
                    updateContactInfo(context, folder, message);

                // Download small messages inline
                if (download && message.size != null) {
                    long maxSize;
                    if (state == null || state.networkState.isUnmetered())
                        maxSize = MessageHelper.SMALL_MESSAGE_SIZE;
                    else {
                        int downloadSize = prefs.getInt("download", 0);
                        maxSize = (downloadSize == 0
                                ? MessageHelper.SMALL_MESSAGE_SIZE
                                : Math.min(downloadSize, MessageHelper.SMALL_MESSAGE_SIZE));
                    }

                    if (message.size < maxSize) {
                        String body = parts.getHtml(context);
                        Helper.writeText(message.getFile(context), body);
                        db.message().setMessageContent(message.id,
                                true,
                                parts.isPlainOnly(),
                                HtmlHelper.getPreview(body),
                                parts.getWarnings(message.warning));
                        Log.i(folder.name + " inline downloaded message id=" + message.id +
                                " size=" + message.size + "/" + (body == null ? null : body.length()));

                        if (!TextUtils.isEmpty(body))
                            fixAttachments(context, message.id, body);
                    }
                }

            } else {
                if (process) {
                    EntityIdentity identity = matchIdentity(context, folder, message);
                    if (identity != null &&
                            (message.identity == null || !message.identity.equals(identity.id))) {
                        message.identity = identity.id;
                        Log.i(folder.name + " updated id=" + message.id + " identity=" + identity.id);
                    }
                }

                if ((!message.seen.equals(seen) || !message.ui_seen.equals(seen)) &&
                        db.operation().getOperationCount(folder.id, message.id, EntityOperation.SEEN) == 0) {
                    update = true;
                    message.seen = seen;
                    message.ui_seen = seen;
                    if (seen)
                        message.ui_ignored = true;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " seen=" + seen);
                }

                if ((!message.answered.equals(answered) || !message.ui_answered.equals(message.answered)) &&
                        db.operation().getOperationCount(folder.id, message.id, EntityOperation.ANSWERED) == 0) {
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

                if ((!message.flagged.equals(flagged) || !message.ui_flagged.equals(flagged)) &&
                        db.operation().getOperationCount(folder.id, message.id, EntityOperation.FLAG) == 0) {
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

                if (message.ui_hide != 0 && message.ui_hide + MIN_HIDE < new Date().getTime() &&
                        db.operation().getOperationCount(folder.id, message.id) == 0) {
                    update = true;
                    message.ui_hide = 0L;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " unhide");
                }

                if (message.ui_browsed != browsed) {
                    update = true;
                    message.ui_browsed = browsed;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " browsed=" + browsed);
                }

                Uri uri = ContactInfo.getLookupUri(context, message.from);
                String avatar = (uri == null ? null : uri.toString());
                if (!Objects.equals(message.avatar, avatar)) {
                    update = true;
                    message.avatar = avatar;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " avatar=" + avatar);
                }

                if (update || process)
                    try {
                        db.beginTransaction();

                        db.message().updateMessage(message);

                        if (process)
                            runRules(context, imessage, message, rules);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                if (process)
                    updateContactInfo(context, folder, message);

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

    private static EntityIdentity matchIdentity(Context context, EntityFolder folder, EntityMessage message) {
        DB db = DB.getInstance(context);

        List<Address> addresses = new ArrayList<>();
        if (folder.isOutgoing()) {
            if (message.from != null)
                addresses.addAll(Arrays.asList(message.from));
        } else {
            if (message.to != null)
                addresses.addAll(Arrays.asList(message.to));
            if (message.cc != null)
                addresses.addAll(Arrays.asList(message.cc));
            if (EntityFolder.ARCHIVE.equals(folder.type) || BuildConfig.DEBUG) {
                if (message.from != null)
                    addresses.addAll(Arrays.asList(message.from));
            }
        }

        // Search for matching identity
        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            if (!TextUtils.isEmpty(email)) {
                EntityIdentity ident = db.identity().getIdentity(folder.account, email);
                if (ident != null)
                    return ident;

                String canonical = MessageHelper.canonicalAddress(email);
                if (canonical.equals(email))
                    continue;

                ident = db.identity().getIdentity(folder.account, canonical);
                if (ident != null)
                    return ident;
            }
        }

        return null;
    }

    private static void runRules(Context context, Message imessage, EntityMessage message, List<EntityRule> rules) {
        if (!ActivityBilling.isPro(context))
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

        if (EntityFolder.DRAFTS.equals(folder.type) ||
                EntityFolder.ARCHIVE.equals(folder.type) ||
                EntityFolder.TRASH.equals(folder.type) ||
                EntityFolder.JUNK.equals(folder.type))
            return;

        final int type = (folder.isOutgoing() ? EntityContact.TYPE_TO : EntityContact.TYPE_FROM);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean suggest_sent = prefs.getBoolean("suggest_sent", false);
        boolean suggest_received = prefs.getBoolean("suggest_received", false);

        if (type == EntityContact.TYPE_TO && !suggest_sent)
            return;
        if (type == EntityContact.TYPE_FROM && !suggest_received)
            return;

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
                            if (!TextUtils.isEmpty(name))
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

            MessageHelper helper = new MessageHelper(imessage);
            MessageHelper.MessageParts parts = helper.getMessageParts();

            if (!message.content) {
                if (state.getNetworkState().isUnmetered() ||
                        (message.size != null && message.size < maxSize)) {
                    String body = parts.getHtml(context);
                    Helper.writeText(message.getFile(context), body);
                    db.message().setMessageContent(message.id,
                            true,
                            parts.isPlainOnly(),
                            HtmlHelper.getPreview(body),
                            parts.getWarnings(message.warning));
                    Log.i(folder.name + " downloaded message id=" + message.id +
                            " size=" + message.size + "/" + (body == null ? null : body.length()));

                    if (!TextUtils.isEmpty(body))
                        fixAttachments(context, message.id, body);
                }
            }

            for (EntityAttachment attachment : attachments)
                if (!attachment.available && TextUtils.isEmpty(attachment.error))
                    if (state.getNetworkState().isUnmetered() ||
                            (attachment.size != null && attachment.size < maxSize))
                        try {
                            parts.downloadAttachment(context, attachment);
                        } catch (Throwable ex) {
                            Log.e(ex);
                            db.attachment().setError(attachment.id, Helper.formatThrowable(ex, false));
                        }
        }
    }

    private static void fixAttachments(Context context, long id, String body) {
        DB db = DB.getInstance(context);
        for (Element element : Jsoup.parse(body).select("img")) {
            String src = element.attr("src");
            if (src.startsWith("cid:")) {
                EntityAttachment attachment = db.attachment().getAttachment(id, "<" + src.substring(4) + ">");
                if (attachment != null && !attachment.isInline()) {
                    Log.i("Setting attachment type to inline id=" + attachment.id);
                    db.attachment().setDisposition(attachment.id, Part.INLINE);
                }
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

    static void notifyMessages(Context context, List<TupleMessageEx> messages, Map<Long, List<Long>> groupNotifying) {
        if (messages == null)
            messages = new ArrayList<>();
        Log.i("Notify messages=" + messages.size());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean badge = prefs.getBoolean("badge", true);
        boolean unseen_ignored = prefs.getBoolean("unseen_ignored", false);
        boolean pro = ActivityBilling.isPro(context);

        int unseen = 0;
        Map<Long, List<TupleMessageEx>> groupMessages = new HashMap<>();
        for (long group : groupNotifying.keySet())
            groupMessages.put(group, new ArrayList<>());

        // Current
        for (TupleMessageEx message : messages) {
            if (!message.ui_seen && (!unseen_ignored || !message.ui_ignored) && message.ui_hide == 0)
                unseen++;

            // Check if notification channel enabled
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O &&
                    message.notifying == 0 && message.from != null && message.from.length > 0) {
                InternetAddress from = (InternetAddress) message.from[0];
                NotificationChannel channel = nm.getNotificationChannel("notification." + from.getAddress().toLowerCase());
                if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE)
                    continue;
            }

            long group = (pro && message.accountNotify ? message.account : 0);
            if (!groupMessages.containsKey(group)) {
                groupNotifying.put(group, new ArrayList<Long>());
                groupMessages.put(group, new ArrayList<TupleMessageEx>());
            }

            if (message.notifying != 0) {
                long id = message.id * message.notifying;
                if (!groupNotifying.get(group).contains(id))
                    groupNotifying.get(group).add(id);
            }

            if (!(message.ui_seen || message.ui_ignored || message.ui_hide != 0)) {
                // This assumes the messages are properly ordered
                if (groupMessages.get(group).size() < MAX_NOTIFICATION_COUNT)
                    groupMessages.get(group).add(message);
            }
        }

        // Update widget/badge count
        if (lastUnseen < 0 || unseen != lastUnseen) {
            lastUnseen = unseen;
            Widget.update(context, unseen);
            try {
                ShortcutBadger.applyCount(context, badge ? unseen : 0);
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        // Difference
        for (long group : groupMessages.keySet()) {
            // Difference
            final List<Long> add = new ArrayList<>();
            final List<Long> remove = new ArrayList<>(groupNotifying.get(group));
            for (TupleMessageEx message : groupMessages.get(group)) {
                long id = (message.content ? message.id : -message.id);
                if (remove.contains(id)) {
                    remove.remove(id);
                    Log.i("Notify existing=" + id);
                } else {
                    remove.remove(-id);
                    add.add(id);
                    Log.i("Notify adding=" + id);
                }
            }

            if (remove.size() + add.size() == 0) {
                Log.i("Notify unchanged");
                continue;
            }

            // Build notifications
            List<Notification> notifications = getNotificationUnseen(context, group, groupMessages.get(group));

            Log.i("Notify group=" + group + " count=" + notifications.size() +
                    " added=" + add.size() + " removed=" + remove.size());

            if (notifications.size() == 0) {
                String tag = "unseen." + group + "." + 0;
                Log.i("Notify cancel tag=" + tag);
                nm.cancel(tag, 1);
            }

            for (Long id : remove) {
                String tag = "unseen." + group + "." + Math.abs(id);
                Log.i("Notify cancel tag=" + tag);
                nm.cancel(tag, 1);
            }

            for (Notification notification : notifications) {
                long id = notification.extras.getLong("id", 0);
                if ((id == 0 && add.size() + remove.size() > 0) || add.contains(id)) {
                    String tag = "unseen." + group + "." + Math.abs(id);
                    Log.i("Notifying tag=" + tag +
                            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? "" : " channel=" + notification.getChannelId()));
                    nm.notify(tag, 1, notification);
                }
            }

            if (remove.size() + add.size() > 0) {
                DB db = DB.getInstance(context);
                for (long id : remove) {
                    groupNotifying.get(group).remove(id);
                    db.message().setMessageNotifying(Math.abs(id), 0);
                }
                for (long id : add) {
                    groupNotifying.get(group).add(id);
                    db.message().setMessageNotifying(Math.abs(id), (int) Math.signum(id));
                }
            }
        }

        groupNotifying.clear();
    }

    private static List<Notification> getNotificationUnseen(Context context, long group, List<TupleMessageEx> messages) {
        List<Notification> notifications = new ArrayList<>();

        // Android 7+ N https://developer.android.com/training/notify-user/group
        // Android 8+ O https://developer.android.com/training/notify-user/channels

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (messages == null || messages.size() == 0 || nm == null)
            return notifications;

        boolean pro = ActivityBilling.isPro(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean biometrics = prefs.getBoolean("biometrics", false);
        boolean biometric_notify = prefs.getBoolean("biometrics_notify", false);
        boolean name_email = prefs.getBoolean("name_email", false);
        boolean flags = prefs.getBoolean("flags", true);
        boolean notify_preview = prefs.getBoolean("notify_preview", true);
        boolean notify_trash = (prefs.getBoolean("notify_trash", true) || !pro);
        boolean notify_archive = (prefs.getBoolean("notify_archive", true) || !pro);
        boolean notify_reply = (prefs.getBoolean("notify_reply", false) && pro);
        boolean notify_reply_direct = (prefs.getBoolean("notify_reply_direct", false) && pro);
        boolean notify_flag = (prefs.getBoolean("notify_flag", false) && flags && pro);
        boolean notify_seen = (prefs.getBoolean("notify_seen", true) || !pro);
        boolean light = prefs.getBoolean("light", false);
        String sound = prefs.getString("sound", null);

        // Get contact info
        Map<TupleMessageEx, ContactInfo> messageContact = new HashMap<>();
        for (TupleMessageEx message : messages)
            messageContact.put(message, ContactInfo.get(context, message.from, false));

        // Summary notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Build pending intents
            Intent summary = new Intent(context, ActivityView.class).setAction("unified");
            PendingIntent piSummary = PendingIntent.getActivity(context, ActivityView.REQUEST_UNIFIED, summary, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent clear = new Intent(context, ServiceUI.class).setAction("clear:" + group);
            PendingIntent piClear = PendingIntent.getService(context, ServiceUI.PI_CLEAR, clear, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build title
            String title = context.getResources().getQuantityString(
                    R.plurals.title_notification_unseen, messages.size(), messages.size());

            // Build notification
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, "notification")
                            .setSmallIcon(R.drawable.baseline_email_white_24)
                            .setContentTitle(title)
                            .setContentIntent(piSummary)
                            .setNumber(messages.size())
                            .setShowWhen(false)
                            .setDeleteIntent(piClear)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(NotificationCompat.CATEGORY_STATUS)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setGroup(Long.toString(group))
                            .setGroupSummary(true)
                            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            if (pro && group != 0 && messages.size() > 0) {
                TupleMessageEx amessage = messages.get(0);
                if (amessage.accountColor != null) {
                    builder.setColor(amessage.accountColor);
                    builder.setColorized(true);
                }
                builder.setSubText(amessage.accountName);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                builder.setSound(null);

            Notification pub = builder.build();
            builder
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setPublicVersion(pub);

            if (!biometrics || biometric_notify) {
                DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                StringBuilder sb = new StringBuilder();
                for (EntityMessage message : messages) {
                    sb.append("<strong>").append(messageContact.get(message).getDisplayName(name_email)).append("</strong>");
                    if (!TextUtils.isEmpty(message.subject))
                        sb.append(": ").append(message.subject);
                    sb.append(" ").append(DTF.format(message.received));
                    sb.append("<br>");
                }

                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(HtmlHelper.fromHtml(sb.toString()))
                        .setSummaryText(title));
            }

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

            NotificationCompat.Builder mbuilder =
                    new NotificationCompat.Builder(context, channelName)
                            .addExtras(args)
                            .setSmallIcon(R.drawable.baseline_email_white_24)
                            .setContentIntent(piContent)
                            .setWhen(message.received)
                            .setDeleteIntent(piIgnore)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(NotificationCompat.CATEGORY_EMAIL)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setOnlyAlertOnce(true);

            // TODO: setAllowSystemGeneratedContextualActions

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                mbuilder
                        .setGroup(Long.toString(group))
                        .setGroupSummary(false)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                int def = 0;

                if (light) {
                    def |= DEFAULT_LIGHTS;
                    Log.i("Notify light enabled");
                }

                Uri uri = (sound == null ? null : Uri.parse(sound));
                if (uri == null || "file".equals(uri.getScheme()))
                    uri = null;
                Log.i("Notify sound=" + uri);

                if (uri == null)
                    def |= DEFAULT_SOUND;
                else
                    mbuilder.setSound(uri);

                mbuilder.setDefaults(def);
            }

            if (biometrics && !biometric_notify)
                mbuilder.setContentTitle(context.getResources().getQuantityString(
                        R.plurals.title_notification_unseen, 1, 1));
            else {
                String folderName = message.folderDisplay == null
                        ? Helper.localizeFolderName(context, message.folderName)
                        : message.folderDisplay;

                mbuilder.setContentTitle(info.getDisplayName(name_email))
                        .setSubText(message.accountName + " · " + folderName);
            }

            DB db = DB.getInstance(context);

            if (notify_trash &&
                    db.folder().getFolderByType(message.account, EntityFolder.TRASH) != null) {
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

            if (notify_archive &&
                    db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE) != null) {
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

            if (notify_reply && message.content &&
                    db.identity().getComposableIdentities(message.account).size() > 0) {
                Intent reply = new Intent(context, ActivityCompose.class)
                        .putExtra("action", "reply")
                        .putExtra("reference", message.id);
                reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent piReply = PendingIntent.getActivity(context, ActivityCompose.PI_REPLY, reply, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionReply = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_reply_24,
                        context.getString(R.string.title_advanced_notify_action_reply),
                        piReply);
                mbuilder.addAction(actionReply.build());
            }

            if (notify_reply_direct &&
                    message.content &&
                    message.identity != null &&
                    message.from != null && message.from.length > 0 &&
                    db.folder().getOutbox() != null) {
                Intent reply = new Intent(context, ServiceUI.class)
                        .setAction("reply:" + message.id)
                        .putExtra("group", group);
                PendingIntent piReply = PendingIntent.getService(context, ServiceUI.PI_REPLY_DIRECT, reply, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionReply = new NotificationCompat.Action.Builder(
                        R.drawable.baseline_reply_24,
                        context.getString(R.string.title_advanced_notify_action_reply_direct),
                        piReply);
                RemoteInput.Builder input = new RemoteInput.Builder("text")
                        .setLabel(context.getString(R.string.title_advanced_notify_action_reply));
                actionReply.addRemoteInput(input.build()).setAllowGeneratedReplies(false);
                mbuilder.addAction(actionReply.build());
            }

            if (notify_flag) {
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

            if (!biometrics || biometric_notify) {
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
                        db.message().setMessageContent(message.id, false, null, null, null);
                    }

                if (info.hasPhoto())
                    mbuilder.setLargeIcon(info.getPhotoBitmap());

                if (info.hasLookupUri())
                    mbuilder.addPerson(info.getLookupUri().toString());

                if (pro && message.accountColor != null) {
                    mbuilder.setColor(message.accountColor);
                    mbuilder.setColorized(true);
                }
            }

            notifications.add(mbuilder.build());
        }

        return notifications;
    }

    // FolderClosedException: can happen when no connectivity

    // IllegalStateException:
    // - "This operation is not allowed on a closed folder"
    // - can happen when syncing message

    // ConnectionException
    // - failed to create new store connection (connectivity)

    // MailConnectException
    // - on connectivity problems when connecting to store

    static NotificationCompat.Builder getNotificationError(Context context, String channel, String title, Throwable ex) {
        // Build pending intent
        Intent intent = new Intent(context, ActivityView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                context, ActivityView.REQUEST_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channel)
                        .setSmallIcon(R.drawable.baseline_warning_white_24)
                        .setContentTitle(context.getString(R.string.title_notification_failed, title))
                        .setContentText(Helper.formatThrowable(ex, false))
                        .setContentIntent(pi)
                        .setAutoCancel(false)
                        .setShowWhen(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOnlyAlertOnce(true)
                        .setCategory(NotificationCompat.CATEGORY_ERROR)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(Helper.formatThrowable(ex, "\n", false)));

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
        List<State> childs = Collections.synchronizedList(new ArrayList<>());

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
                    ("connection failure".equals(ex.getMessage()) ||
                            ex.getCause() instanceof SocketException ||
                            ex.getCause() instanceof ConnectionException))
                recoverable = false;

            if (ex instanceof ConnectionException)
                // failed to create new store connection
                // BYE, Socket is closed
                recoverable = false;

            if (ex instanceof FolderClosedException ||
                    ex instanceof FolderNotFoundException)
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

        boolean isRunning() {
            return running;
        }

        boolean isRecoverable() {
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
