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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.MailboxInfo;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Message;
import com.sun.mail.pop3.POP3Store;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static androidx.core.app.NotificationCompat.DEFAULT_LIGHTS;
import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static javax.mail.Folder.READ_WRITE;

class Core {
    private static final int MAX_NOTIFICATION_COUNT = 10; // per group
    private static final int SYNC_CHUNCK_SIZE = 200;
    private static final int SYNC_BATCH_SIZE = 20;
    private static final int DOWNLOAD_BATCH_SIZE = 20;
    private static final int SYNC_YIELD_COUNT = 100;
    private static final long SYNC_YIELD_DURATION = 1000; // milliseconds
    private static final int DOWNLOAD_YIELD_COUNT = 25;
    private static final long DOWNLOAD_YIELD_DURATION = 1000; // milliseconds
    private static final long YIELD_DURATION = 200L; // milliseconds
    private static final long JOIN_WAIT = 180 * 1000L; // milliseconds
    private static final long FUTURE_RECEIVED = 30 * 24 * 3600 * 1000L; // milliseconds
    private static final int LOCAL_RETRY_MAX = 2;
    private static final long LOCAL_RETRY_DELAY = 5 * 1000L; // milliseconds
    private static final int TOTAL_RETRY_MAX = LOCAL_RETRY_MAX * 5;
    private static final int MAX_PREVIEW = 5000; // characters

    static void processOperations(
            Context context,
            EntityAccount account, EntityFolder folder, List<TupleOperationEx> ops,
            Store istore, Folder ifolder,
            State state, int priority, long sequence)
            throws JSONException {
        try {
            Log.i(folder.name + " start process");

            DB db = DB.getInstance(context);

            int retry = 0;
            boolean group = true;
            Log.i(folder.name + " executing operations=" + ops.size());
            while (retry < LOCAL_RETRY_MAX && ops.size() > 0 &&
                    state.isRunning() &&
                    state.batchCanRun(folder.id, priority, sequence)) {
                TupleOperationEx op = ops.get(0);

                try {
                    Log.i(folder.name +
                            " start op=" + op.id + "/" + op.name +
                            " folder=" + op.folder +
                            " msg=" + op.message +
                            " args=" + op.args +
                            " group=" + group +
                            " retry=" + retry);

                    if (!Objects.equals(folder.id, op.folder))
                        throw new IllegalArgumentException("Invalid folder=" + folder.id + "/" + op.folder);

                    if (account.protocol == EntityAccount.TYPE_IMAP &&
                            !folder.local && ifolder != null && !ifolder.isOpen()) {
                        Log.w(folder.name + " is closed");
                        break;
                    }

                    // Fetch most recent copy of message
                    EntityMessage message = null;
                    if (op.message != null)
                        message = db.message().getMessage(op.message);

                    JSONArray jargs = new JSONArray(op.args);
                    Map<TupleOperationEx, EntityMessage> similar = new HashMap<>();

                    try {
                        // Operations should use database transaction when needed

                        if (message == null &&
                                !EntityOperation.FETCH.equals(op.name) &&
                                !EntityOperation.SYNC.equals(op.name) &&
                                !EntityOperation.SUBSCRIBE.equals(op.name) &&
                                !EntityOperation.PURGE.equals(op.name))
                            throw new MessageRemovedException();

                        // Process similar operations
                        boolean skip = false;
                        for (int j = 1; j < ops.size(); j++) {
                            TupleOperationEx next = ops.get(j);

                            switch (op.name) {
                                case EntityOperation.ADD:
                                    // Same message
                                    if (Objects.equals(op.message, next.message) &&
                                            (EntityOperation.ADD.equals(next.name) ||
                                                    EntityOperation.DELETE.equals(next.name)))
                                        skip = true;
                                    break;

                                case EntityOperation.FETCH:
                                    if (EntityOperation.FETCH.equals(next.name)) {
                                        JSONArray jnext = new JSONArray(next.args);
                                        // Same uid
                                        if (jargs.getLong(0) == jnext.getLong(0))
                                            skip = true;
                                    }
                                    break;

                                case EntityOperation.MOVE:
                                    if (group &&
                                            message.uid != null &&
                                            EntityOperation.MOVE.equals(next.name) &&
                                            account.protocol == EntityAccount.TYPE_IMAP) {
                                        JSONArray jnext = new JSONArray(next.args);
                                        // Same target
                                        if (jargs.getLong(0) == jnext.getLong(0)) {
                                            EntityMessage m = db.message().getMessage(next.message);
                                            if (m != null && m.uid != null)
                                                similar.put(next, m);
                                        }
                                    }
                                    break;
                            }
                        }

                        if (skip) {
                            Log.i(folder.name +
                                    " skipping op=" + op.id + "/" + op.name +
                                    " msg=" + op.message + " args=" + op.args);
                            db.operation().deleteOperation(op.id);
                            ops.remove(op);
                            continue;
                        }

                        List<Long> sids = new ArrayList<>();
                        for (TupleOperationEx s : similar.keySet())
                            sids.add(s.id);

                        if (similar.size() > 0)
                            Log.i(folder.name + " similar=" + TextUtils.join(",", sids));

                        op.tries++;

                        // Leave crumb
                        Map<String, String> crumb = new HashMap<>();
                        crumb.put("name", op.name);
                        crumb.put("args", op.args);
                        crumb.put("account", op.account + ":" + account.protocol);
                        crumb.put("folder", op.folder + ":" + folder.type);
                        if (op.message != null)
                            crumb.put("message", Long.toString(op.message));
                        crumb.put("tries", Integer.toString(op.tries));
                        crumb.put("similar", TextUtils.join(",", sids));
                        crumb.put("thread", Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
                        Log.breadcrumb("start operation", crumb);

                        try {
                            db.beginTransaction();

                            db.operation().setOperationError(op.id, null);

                            if (message != null)
                                db.message().setMessageError(message.id, null);

                            db.operation().setOperationState(op.id, "executing");
                            for (TupleOperationEx s : similar.keySet())
                                db.operation().setOperationState(s.id, "executing");

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        if (istore instanceof POP3Store)
                            switch (op.name) {
                                case EntityOperation.SEEN:
                                    onSeen(context, jargs, folder, message, (POP3Folder) ifolder);
                                    break;

                                case EntityOperation.FLAG:
                                    onFlag(context, jargs, folder, message, (POP3Folder) ifolder);
                                    break;

                                case EntityOperation.ANSWERED:
                                case EntityOperation.ADD:
                                case EntityOperation.EXISTS:
                                    // Do nothing
                                    break;

                                case EntityOperation.MOVE:
                                    onMove(context, jargs, folder, message);
                                    break;

                                case EntityOperation.DELETE:
                                    onDelete(context, jargs, account, folder, message, (POP3Folder) ifolder, (POP3Store) istore, state);
                                    break;

                                case EntityOperation.SYNC:
                                    onSynchronizeMessages(context, jargs, account, folder, (POP3Folder) ifolder, (POP3Store) istore, state);
                                    break;

                                case EntityOperation.PURGE:
                                    onPurgeFolder(context, folder);
                                    break;

                                default:
                                    Log.w(folder.name + " ignored=" + op.name);
                            }
                        else {
                            ensureUid(context, folder, message, op, (IMAPFolder) ifolder);

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

                                case EntityOperation.LABEL:
                                    onLabel(context, jargs, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.ADD:
                                    onAdd(context, jargs, account, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.MOVE:
                                    List<EntityMessage> messages = new ArrayList<>();
                                    messages.add(message);
                                    messages.addAll(similar.values());
                                    onMove(context, jargs, false, folder, messages, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.COPY:
                                    onMove(context, jargs, true, folder, Arrays.asList(message), (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.FETCH:
                                    onFetch(context, jargs, folder, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.DELETE:
                                    onDelete(context, jargs, folder, message, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.HEADERS:
                                    onHeaders(context, jargs, folder, message, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.RAW:
                                    onRaw(context, jargs, folder, message, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.BODY:
                                    onBody(context, jargs, folder, message, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.ATTACHMENT:
                                    onAttachment(context, jargs, folder, message, op, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.EXISTS:
                                    onExists(context, jargs, folder, message, op, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.SYNC:
                                    onSynchronizeMessages(context, jargs, account, folder, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.SUBSCRIBE:
                                    onSubscribeFolder(context, jargs, folder, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.PURGE:
                                    onPurgeFolder(context, jargs, folder, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.RULE:
                                    onRule(context, jargs, message);
                                    break;

                                default:
                                    throw new IllegalArgumentException("Unknown operation=" + op.name);
                            }
                        }

                        crumb.put("thread", Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
                        Log.breadcrumb("end operation", crumb);

                        // Operation succeeded
                        try {
                            db.beginTransaction();

                            db.operation().deleteOperation(op.id);
                            for (TupleOperationEx s : similar.keySet())
                                db.operation().deleteOperation(s.id);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        ops.remove(op);
                        for (TupleOperationEx s : similar.keySet())
                            ops.remove(s);
                    } catch (Throwable ex) {
                        Log.e(folder.name, ex);
                        EntityLog.log(context, folder.name +
                                " op=" + op.name +
                                " try=" + op.tries +
                                " " + Log.formatThrowable(ex, false));

                        try {
                            db.beginTransaction();

                            db.operation().setOperationTries(op.id, op.tries);

                            op.error = Log.formatThrowable(ex);
                            db.operation().setOperationError(op.id, op.error);

                            if (message != null &&
                                    !EntityOperation.FETCH.equals(op.name) &&
                                    !(ex instanceof IllegalArgumentException))
                                db.message().setMessageError(message.id, op.error);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        if (similar.size() > 0) {
                            // Retry individually
                            group = false;
                            // Finally will reset state
                            continue;
                        }

                        if (op.tries >= TOTAL_RETRY_MAX ||
                                ex instanceof OutOfMemoryError ||
                                ex instanceof FileNotFoundException ||
                                ex instanceof FolderNotFoundException ||
                                ex instanceof IllegalArgumentException ||
                                ex instanceof SQLiteConstraintException ||
                                (!ConnectionHelper.isIoError(ex) &&
                                        (ex.getCause() instanceof BadCommandException ||
                                                ex.getCause() instanceof CommandFailedException /* NO */)) ||
                                MessageHelper.isRemoved(ex) ||
                                EntityOperation.ATTACHMENT.equals(op.name) ||
                                (EntityOperation.ADD.equals(op.name) &&
                                        EntityFolder.DRAFTS.equals(folder.type))) {
                            // com.sun.mail.iap.BadCommandException: BAD [TOOBIG] Message too large
                            // com.sun.mail.iap.CommandFailedException: NO [CANNOT] Cannot APPEND to a SPAM folder
                            // com.sun.mail.iap.CommandFailedException: NO [ALERT] Cannot MOVE messages out of the Drafts folder
                            // com.sun.mail.iap.CommandFailedException: NO [OVERQUOTA] quota exceeded
                            // Drafts: javax.mail.FolderClosedException: * BYE Jakarta Mail Exception:
                            //   javax.net.ssl.SSLException: Write error: ssl=0x8286cac0: I/O error during system call, Broken pipe
                            // Drafts: * BYE Jakarta Mail Exception: java.io.IOException: Connection dropped by server?
                            // Sync: BAD Could not parse command
                            // Seen: NO mailbox selected READ-ONLY
                            // Fetch: BAD Error in IMAP command FETCH: Invalid messageset
                            // Fetch: NO all of the requested messages have been expunged
                            // Fetch: BAD parse error: invalid message sequence number:
                            // Fetch: NO The specified message set is invalid.
                            // Fetch: NO [SERVERBUG] SELECT Server error - Please try again later
                            // Fetch: NO [SERVERBUG] UID FETCH Server error - Please try again later
                            // Fetch: NO Invalid message number (took n ms)
                            // Fetch: BAD Internal Server Error
                            // Move: NO Over quota
                            // Move: NO No matching messages
                            // Move: NO [EXPUNGEISSUED] Some of the requested messages no longer exist
                            // Move: BAD parse error: invalid message sequence number:
                            // Move: NO MOVE failed or partially completed.
                            // Move: NO mailbox selected READ-ONLY
                            // Move: NO read only
                            // Add: BAD Data length exceeds limit
                            // Delete: NO [CANNOT] STORE It's not possible to perform specified operation
                            // Delete: NO [UNAVAILABLE] EXPUNGE Backend error
                            // Delete: NO mailbox selected READ-ONLY

                            String msg = "Unrecoverable operation=" + op.name + " tries=" + op.tries + " created=" + new Date(op.created);

                            EntityLog.log(context, msg +
                                    " folder=" + folder.id + ":" + folder.name +
                                    " message=" + (message == null ? null : message.id + ":" + message.subject) +
                                    " reason=" + Log.formatThrowable(ex, false));

                            if (ifolder != null && ifolder.isOpen() &&
                                    (op.tries > 1 ||
                                            ex.getCause() instanceof BadCommandException ||
                                            ex.getCause() instanceof CommandFailedException))
                                Log.e(new Throwable(msg, ex));

                            try {
                                db.beginTransaction();

                                // Cleanup operation
                                op.cleanup(context, true);

                                // There is no use in repeating
                                db.operation().deleteOperation(op.id);

                                // Cleanup messages
                                if (message != null && MessageHelper.isRemoved(ex))
                                    db.message().deleteMessage(message.id);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            ops.remove(op);
                        } else {
                            retry++;
                            if (retry < LOCAL_RETRY_MAX &&
                                    state.isRunning() &&
                                    state.batchCanRun(folder.id, priority, sequence))
                                try {
                                    Thread.sleep(LOCAL_RETRY_DELAY);
                                } catch (InterruptedException ex1) {
                                    Log.w(ex1);
                                }
                        }
                    } finally {
                        // Reset operation state
                        try {
                            db.beginTransaction();

                            db.operation().setOperationState(op.id, null);
                            for (TupleOperationEx s : similar.keySet())
                                db.operation().setOperationState(s.id, null);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                    }
                } finally {
                    Log.i(folder.name + " end op=" + op.id + "/" + op.name);
                }
            }

            if (ops.size() == 0)
                state.batchCompleted(folder.id, priority, sequence);
            else {
                if (state.batchCanRun(folder.id, priority, sequence))
                    state.error(new OperationCanceledException("Processing"));
            }
        } finally {
            Log.i(folder.name + " end process state=" + state + " pending=" + ops.size());
        }
    }

    private static void ensureUid(Context context, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws MessagingException {
        if (folder.local)
            return;
        if (message == null || message.uid != null)
            return;

        if (EntityOperation.ADD.equals(op.name))
            return;
        if (EntityOperation.FETCH.equals(op.name))
            return;
        if (EntityOperation.EXISTS.equals(op.name))
            return;
        if (EntityOperation.DELETE.equals(op.name) && !TextUtils.isEmpty(message.msgid))
            return;

        Log.i(folder.name + " ensure uid op=" + op.name + " msgid=" + message.msgid);

        if (TextUtils.isEmpty(message.msgid))
            throw new IllegalArgumentException("Message without msgid for " + op.name);

        Long uid = findUid(ifolder, message.msgid, false);
        if (uid == null)
            throw new IllegalArgumentException("Message not found for " + op.name + " folder=" + folder.name);

        DB db = DB.getInstance(context);
        db.message().setMessageUid(message.id, message.uid);
        message.uid = uid;
    }

    private static Long findUid(IMAPFolder ifolder, String msgid, boolean purge) throws MessagingException {
        String name = ifolder.getFullName();
        Log.i(name + " searching for msgid=" + msgid);

        Long uid = null;

        Message[] imessages = ifolder.search(new MessageIDTerm(msgid));
        if (imessages != null) {
            for (Message iexisting : imessages) {
                long muid = ifolder.getUID(iexisting);
                if (muid < 0)
                    continue;
                Log.i(name + " found uid=" + muid + " for msgid=" + msgid);
                // RFC3501: Unique identifiers are assigned in a strictly ascending fashion
                if (uid == null || muid > uid)
                    uid = muid;
            }

            if (uid != null && purge) {
                boolean purged = false;
                for (Message iexisting : imessages) {
                    long muid = ifolder.getUID(iexisting);
                    if (muid < 0)
                        continue;
                    if (muid != uid)
                        try {
                            Log.i(name + " deleting uid=" + muid + " for msgid=" + msgid);
                            iexisting.setFlag(Flags.Flag.DELETED, true);
                            purged = true;
                        } catch (MessageRemovedException ignored) {
                            Log.w(name + " existing gone uid=" + muid + " for msgid=" + msgid);
                        }
                }
                if (purged)
                    ifolder.expunge();
            }
        }

        Log.i(name + " got uid=" + uid + " for msgid=" + msgid);
        return uid;
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

    private static void onSeen(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, POP3Folder ifolder) throws JSONException {
        // Mark message (un)seen
        DB db = DB.getInstance(context);

        boolean seen = jargs.getBoolean(0);
        db.message().setMessageUiSeen(folder.id, seen);
    }

    private static void onFlag(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, JSONException {
        // Star/unstar message
        DB db = DB.getInstance(context);

        if (!ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED)) {
            db.message().setMessageFlagged(message.id, false);
            db.message().setMessageUiFlagged(message.id, false, null);
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

    private static void onFlag(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, POP3Folder ifolder) throws MessagingException, JSONException {
        // Star/unstar message
        DB db = DB.getInstance(context);

        boolean flagged = jargs.getBoolean(0);
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

        // This will be fixed when moving the message
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
        // https://tools.ietf.org/html/rfc3501#section-2.3.2
        String keyword = jargs.getString(0);
        boolean set = jargs.getBoolean(1);

        if (TextUtils.isEmpty(keyword))
            throw new IllegalArgumentException("keyword/empty");

        if (message.uid == null)
            throw new IllegalArgumentException("keyword/uid");

        if (!ifolder.getPermanentFlags().contains(Flags.Flag.USER)) {
            if ("$Forwarded".equals(keyword) && false) {
                JSONArray janswered = new JSONArray();
                janswered.put(true);
                onAnswered(context, janswered, folder, message, ifolder);
            }
            return;
        }

        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        Flags flags = new Flags(keyword);
        imessage.setFlags(flags, set);
    }

    private static void onLabel(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        // Set/clear Gmail label
        // Gmail does not push label changes
        String label = jargs.getString(0);
        boolean set = jargs.getBoolean(1);

        if (TextUtils.isEmpty(label))
            throw new IllegalArgumentException("label/empty");

        if (message.uid == null)
            throw new IllegalArgumentException("label/uid");

        DB db = DB.getInstance(context);

        if (!set && label.equals(folder.name)) {
            if (TextUtils.isEmpty(message.msgid)) {
                Log.w("label/msgid");
                return;
            }

            // Prevent deleting message
            EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
            if (archive == null) {
                Log.w("label/archive");
                return;
            }

            boolean archived;
            Folder iarchive = istore.getFolder(archive.name);
            try {
                iarchive.open(Folder.READ_ONLY);
                Message[] imessages = iarchive.search(new MessageIDTerm(message.msgid));
                archived = (imessages != null && imessages.length > 0);
            } finally {
                if (iarchive.isOpen())
                    iarchive.close();
            }

            if (archived)
                try {
                    Message imessage = ifolder.getMessageByUID(message.uid);
                    if (imessage == null)
                        throw new MessageRemovedException();
                    imessage.setFlag(Flags.Flag.DELETED, true);
                    ifolder.expunge();
                } catch (MessagingException ex) {
                    Log.w(ex);
                }
            else {
                Log.w("label/delete folder=" + folder.name);
                return;
            }
        } else {
            try {
                Message imessage = ifolder.getMessageByUID(message.uid);
                if (imessage instanceof GmailMessage)
                    ((GmailMessage) imessage).setLabels(new String[]{label}, set);
            } catch (MessagingException ex) {
                Log.w(ex);
            }
        }

        try {
            db.beginTransaction();

            List<EntityMessage> messages = db.message().getMessagesByMsgId(message.account, message.msgid);
            if (messages == null)
                return;

            for (EntityMessage m : messages) {
                EntityFolder f = db.folder().getFolder(m.folder);
                if (!label.equals(f.name) && m.setLabel(label, set)) {
                    Log.i("Set " + label + "=" + set + " id=" + m.id + " folder=" + f.name);
                    db.message().setMessageLabels(m.id, DB.Converters.fromStringArray(m.labels));
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void onAdd(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, EntityMessage message, IMAPStore istore, IMAPFolder ifolder, State state) throws MessagingException, IOException {
        // Add message
        DB db = DB.getInstance(context);

        if (folder.local) {
            Log.i(folder.name + " local add");
            return;
        }

        // Drafts can change accounts
        if (jargs.length() == 0 && !folder.id.equals(message.folder))
            throw new IllegalArgumentException("Message folder changed");

        // Get arguments
        long target = jargs.optLong(0, folder.id);
        boolean autoread = jargs.optBoolean(1, false);

        if (target != folder.id)
            throw new IllegalArgumentException("Invalid folder");

        // External draft might have a uid only
        if (TextUtils.isEmpty(message.msgid)) {
            message.msgid = EntityMessage.generateMessageId();
            db.message().setMessageMsgId(message.id, message.msgid);
        }

        Properties props = MessageHelper.getSessionProperties();
        Session isession = Session.getInstance(props, null);
        Flags flags = ifolder.getPermanentFlags();

        // Get raw message
        MimeMessage imessage;
        File file = message.getRawFile(context);
        if (folder.id.equals(message.folder)) {
            // Pre flight check
            if (!message.content)
                throw new IllegalArgumentException("Message body missing");

            imessage = MessageHelper.from(context, message, null, isession, false);

            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                imessage.writeTo(os);
            }
        } else {
            // Cross account move
            if (!file.exists())
                throw new IllegalArgumentException("raw message file not found");

            Log.i(folder.name + " reading " + file);
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                imessage = new MimeMessage(isession, is);
            }
        }

        db.message().setMessageRaw(message.id, true);

        // Check size
        if (account.max_size != null) {
            long size = file.length();
            if (size > account.max_size) {
                String msg = "Too large" +
                        " size=" + Helper.humanReadableByteCount(size) +
                        "/" + Helper.humanReadableByteCount(account.max_size) +
                        " host=" + account.host;
                Log.e(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // Handle auto read
        if (flags.contains(Flags.Flag.SEEN)) {
            if (autoread && !imessage.isSet(Flags.Flag.SEEN)) {
                Log.i(folder.name + " autoread");
                imessage.setFlag(Flags.Flag.SEEN, true);
            }
        }

        // Handle draft
        if (flags.contains(Flags.Flag.DRAFT))
            imessage.setFlag(Flags.Flag.DRAFT, EntityFolder.DRAFTS.equals(folder.type));

        // Add message
        Long newuid = null;
        if (MessageHelper.hasCapability(ifolder, "UIDPLUS")) {
            // https://tools.ietf.org/html/rfc4315
            AppendUID[] uids = ifolder.appendUIDMessages(new Message[]{imessage});
            if (uids != null && uids.length > 0 && uids[0] != null && uids[0].uid > 0) {
                newuid = uids[0].uid;
                Log.i(folder.name + " appended uid=" + newuid);
            }
        } else
            ifolder.appendMessages(new Message[]{imessage});

        if (folder.id.equals(message.folder)) {
            // Prevent deleting message
            db.message().setMessageUid(message.id, null);

            // Some providers do not list the new message yet
            Long found = findUid(ifolder, message.msgid, true);
            if (found != null)
                if (newuid == null)
                    newuid = found;
                else if (!newuid.equals(found)) {
                    Log.w(folder.name + " Added=" + newuid + " found=" + found);
                    newuid = Math.max(newuid, found);
                }

            if (newuid != null && (message.uid == null || newuid > message.uid))
                try {
                    Log.i(folder.name + " Fetching uid=" + newuid);
                    JSONArray fargs = new JSONArray();
                    fargs.put(newuid);
                    onFetch(context, fargs, folder, istore, ifolder, state);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
        } else {
            // Mark source read
            if (autoread)
                EntityOperation.queue(context, message, EntityOperation.SEEN, true);

            // Delete source
            EntityOperation.queue(context, message, EntityOperation.DELETE);
        }
    }

    private static void onMove(Context context, JSONArray jargs, boolean copy, EntityFolder folder, List<EntityMessage> messages, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        // Move message
        DB db = DB.getInstance(context);

        // Get arguments
        long id = jargs.getLong(0);
        boolean seen = jargs.optBoolean(1);
        boolean unflag = jargs.optBoolean(3);

        Flags flags = ifolder.getPermanentFlags();

        // Get target folder
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();
        if (folder.id.equals(target.id))
            throw new IllegalArgumentException("self");

        // De-classify
        for (EntityMessage message : messages)
            MessageClassifier.classify(message, folder, target, context);

        IMAPFolder itarget = (IMAPFolder) istore.getFolder(target.name);

        // Get source messages
        Map<Message, EntityMessage> map = new HashMap<>();
        for (EntityMessage message : messages)
            try {
                if (message.uid == null)
                    throw new IllegalArgumentException("move without uid");
                Message imessage = ifolder.getMessageByUID(message.uid);
                if (imessage == null)
                    throw new MessageRemovedException("move without message");
                map.put(imessage, message);
            } catch (MessageRemovedException ex) {
                Log.e(ex);
                db.message().deleteMessage(message.id);
            }

        // Some servers return different capabilities for different sessions
        boolean canMove = MessageHelper.hasCapability(ifolder, "MOVE");

        // Some providers do not support the COPY operation for drafts
        boolean draft = (EntityFolder.DRAFTS.equals(folder.type) || EntityFolder.DRAFTS.equals(target.type));
        if (draft) {
            Log.i(folder.name + " move from " + folder.type + " to " + target.type);

            List<Message> icopies = new ArrayList<>();
            for (Map.Entry<Message, EntityMessage> entry : map.entrySet()) {
                Message imessage = entry.getKey();
                EntityMessage message = entry.getValue();

                File file = File.createTempFile("draft", "." + message.id, context.getCacheDir());
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                    imessage.writeTo(os);
                }

                Properties props = MessageHelper.getSessionProperties();
                Session isession = Session.getInstance(props, null);

                Message icopy;
                try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                    icopy = new MimeMessage(isession, is);
                }

                file.delete();

                for (Flags.Flag flag : imessage.getFlags().getSystemFlags())
                    icopy.setFlag(flag, true);

                icopies.add(icopy);
            }

            itarget.appendMessages(icopies.toArray(new Message[0]));
        } else {
            for (Message imessage : map.keySet()) {
                Log.i((copy ? "Copy" : "Move") + " seen=" + seen + " unflag=" + unflag + " flags=" + imessage.getFlags() + " can=" + canMove);

                // Mark read
                if (seen && !imessage.isSet(Flags.Flag.SEEN) && flags.contains(Flags.Flag.SEEN))
                    imessage.setFlag(Flags.Flag.SEEN, true);

                // Remove star
                if (unflag && imessage.isSet(Flags.Flag.FLAGGED) && flags.contains(Flags.Flag.FLAGGED))
                    imessage.setFlag(Flags.Flag.FLAGGED, false);
            }

            // https://tools.ietf.org/html/rfc6851
            if (!copy && canMove)
                ifolder.moveMessages(map.keySet().toArray(new Message[0]), itarget);
            else
                ifolder.copyMessages(map.keySet().toArray(new Message[0]), itarget);
        }

        // Delete source
        if (!copy && (draft || !canMove)) {
            try {
                for (Message imessage : map.keySet())
                    imessage.setFlag(Flags.Flag.DELETED, true);
                ifolder.expunge();
            } catch (MessageRemovedException ex) {
                Log.w(ex);
            }
        } else {
            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);
        }

        // Fetch appended/copied when needed
        boolean fetch = (copy ||
                !"connected".equals(target.state) ||
                !MessageHelper.hasCapability(ifolder, "IDLE"));
        if (draft || fetch)
            try {
                Log.i(target.name + " moved message fetch=" + fetch);
                itarget.open(READ_WRITE);

                for (EntityMessage message : map.values())
                    if (!TextUtils.isEmpty(message.msgid))
                        try {
                            Long uid = findUid(itarget, message.msgid, false);
                            if (uid != null) {
                                if (draft) {
                                    Message icopy = itarget.getMessageByUID(uid);
                                    if (icopy == null) {
                                        Log.w(target.name + " Gone uid=" + uid);
                                        continue;
                                    }

                                    // Mark read
                                    if (seen && !icopy.isSet(Flags.Flag.SEEN) && flags.contains(Flags.Flag.SEEN))
                                        icopy.setFlag(Flags.Flag.SEEN, true);

                                    // Remove star
                                    if (unflag && icopy.isSet(Flags.Flag.FLAGGED) && flags.contains(Flags.Flag.FLAGGED))
                                        icopy.setFlag(Flags.Flag.FLAGGED, false);

                                    // Set drafts flag
                                    if (flags.contains(Flags.Flag.DRAFT))
                                        icopy.setFlag(Flags.Flag.DRAFT, EntityFolder.DRAFTS.equals(target.type));
                                }

                                if (fetch)
                                    try {
                                        Log.i(target.name + " Fetching uid=" + uid);
                                        JSONArray fargs = new JSONArray();
                                        fargs.put(uid);
                                        onFetch(context, fargs, target, istore, itarget, state);
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                            }
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
            } catch (Throwable ex) {
                Log.w(ex);
            } finally {
                if (itarget.isOpen())
                    itarget.close();
            }

        // Delete junk contacts
        if (EntityFolder.JUNK.equals(target.type))
            for (EntityMessage message : map.values()) {
                Address[] recipients = (message.reply != null ? message.reply : message.from);
                if (recipients != null)
                    for (Address recipient : recipients) {
                        String email = ((InternetAddress) recipient).getAddress();
                        int count = db.contact().deleteContact(target.account, EntityContact.TYPE_FROM, email);
                        Log.i("Deleted contact email=" + email + " count=" + count);
                    }
            }
    }

    private static void onMove(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message) throws JSONException, FolderNotFoundException {
        // Move message
        DB db = DB.getInstance(context);

        // Get arguments
        long id = jargs.getLong(0);
        boolean seen = jargs.optBoolean(1);
        boolean unflag = jargs.optBoolean(3);

        // Get target folder
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();
        if (folder.id.equals(target.id))
            throw new IllegalArgumentException("self");

        // Move from trash/drafts only
        if (!EntityFolder.TRASH.equals(folder.type) &&
                !EntityFolder.DRAFTS.equals(folder.type))
            throw new IllegalArgumentException("Invalid POP3 folder" +
                    " source=" + folder.type + " target=" + target.type);

        message.folder = target.id;
        if (seen)
            message.ui_seen = seen;
        if (unflag)
            message.ui_flagged = false;
        message.ui_hide = false;

        db.message().updateMessage(message);
    }

    private static void onFetch(Context context, JSONArray jargs, EntityFolder folder, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        long uid = jargs.getLong(0);
        boolean removed = jargs.optBoolean(1);

        if (uid < 0)
            throw new MessageRemovedException(folder.name + " fetch uid=" + uid);

        DB db = DB.getInstance(context);
        EntityAccount account = db.account().getAccount(folder.account);
        if (account == null)
            throw new IllegalArgumentException("account missing");

        try {
            if (removed) {
                db.message().deleteMessage(folder.id, uid);
                throw new MessageRemovedException("removed uid=" + uid);
            }

            MimeMessage imessage = (MimeMessage) ifolder.getMessageByUID(uid);
            if (imessage == null)
                throw new MessageRemovedException(folder.name + " fetch not found uid=" + uid);
            if (imessage.isExpunged())
                throw new MessageRemovedException(folder.name + " fetch expunged uid=" + uid);
            if (imessage.isSet(Flags.Flag.DELETED))
                throw new MessageRemovedException(folder.name + " fetch deleted uid=" + uid);

            SyncStats stats = new SyncStats();
            boolean download = db.folder().getFolderDownload(folder.id);
            List<EntityRule> rules = db.rule().getEnabledRules(folder.id);

            try {
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);
                fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
                //fp.add(UIDFolder.FetchProfileItem.UID);
                fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                fp.add(FetchProfile.Item.SIZE);
                fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                if (account.isGmail()) {
                    fp.add(GmailFolder.FetchProfileItem.THRID);
                    fp.add(GmailFolder.FetchProfileItem.LABELS);
                }
                ifolder.fetch(new Message[]{imessage}, fp);

                EntityMessage message = synchronizeMessage(context, account, folder, istore, ifolder, imessage, false, download, rules, state, stats);
                if (message != null) {
                    if (account.isGmail() && EntityFolder.USER.equals(folder.type))
                        try {
                            JSONArray jlabel = new JSONArray();
                            jlabel.put(0, folder.name);
                            jlabel.put(1, true);
                            onLabel(context, jlabel, folder, message, istore, ifolder, state);
                        } catch (Throwable ex1) {
                            Log.e(ex1);
                        }

                    if (download)
                        downloadMessage(context, account, folder, istore, ifolder, imessage, message.id, state, stats);
                }

                if (!stats.isEmpty())
                    EntityLog.log(context, account.name + "/" + folder.name + " fetch stats " + stats);
            } finally {
                ((IMAPMessage) imessage).invalidateHeaders();
            }
        } catch (MessageRemovedException ex) {
            Log.i(ex);

            if (account.isGmail() && EntityFolder.USER.equals(folder.type)) {
                EntityMessage message = db.message().getMessageByUid(folder.id, uid);
                if (message != null)
                    try {
                        JSONArray jlabel = new JSONArray();
                        jlabel.put(0, folder.name);
                        jlabel.put(1, false);
                        onLabel(context, jlabel, folder, message, istore, ifolder, state);
                    } catch (Throwable ex1) {
                        Log.e(ex1);
                    }
            }

            db.message().deleteMessage(folder.id, uid);
        } finally {
            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);
        }
    }

    private static void onDelete(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException {
        // Delete message
        DB db = DB.getInstance(context);

        if (folder.local) {
            Log.i(folder.name + " local delete");
            db.message().deleteMessage(message.id);
            return;
        }

        try {
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

            if (!TextUtils.isEmpty(message.msgid) && !deleted)
                try {
                    Message[] imessages = ifolder.search(new MessageIDTerm(message.msgid));
                    if (imessages == null)
                        Log.w(folder.name + " search for msgid=" + message.msgid + " returned null");
                    else
                        for (Message iexisting : imessages) {
                            long muid = ifolder.getUID(iexisting);
                            Log.i(folder.name + " deleting uid=" + muid);
                            try {
                                iexisting.setFlag(Flags.Flag.DELETED, true);
                                deleted = true;
                            } catch (MessageRemovedException ignored) {
                                Log.w(folder.name + " existing gone uid=" + muid);
                            }
                        }
                } catch (MessagingException ex) {
                    Log.w(ex);
                }

            if (deleted)
                ifolder.expunge();

            db.message().deleteMessage(message.id);
        } finally {
            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);
        }
    }

    private static void onDelete(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, EntityMessage message, POP3Folder ifolder, POP3Store istore, State state) throws MessagingException, IOException {
        // Delete message
        DB db = DB.getInstance(context);

        if (EntityFolder.INBOX.equals(folder.type)) {
            if (account.leave_deleted) {
                // Remove message/attachments files on cleanup
                db.message().resetMessageContent(message.id);
                db.attachment().resetAvailable(message.id);
            } else {
                Map<String, String> caps = istore.capabilities();

                Message[] imessages = ifolder.getMessages();
                Log.i(folder.name + " POP messages=" + imessages.length);

                boolean hasUidl = caps.containsKey("UIDL");
                if (hasUidl) {
                    FetchProfile ifetch = new FetchProfile();
                    ifetch.add(UIDFolder.FetchProfileItem.UID);
                    ifolder.fetch(imessages, ifetch);
                }

                boolean found = false;
                for (Message imessage : imessages) {
                    MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);

                    String uidl = (hasUidl ? ifolder.getUID(imessage) : null);
                    String msgid = helper.getMessageID();

                    Log.i(folder.name + " POP searching=" + message.uidl + "/" + message.msgid +
                            " iterate=" + uidl + "/" + msgid);
                    if ((uidl != null && uidl.equals(message.uidl)) ||
                            (msgid != null && msgid.equals(message.msgid))) {
                        found = true;
                        Log.i(folder.name + " POP delete=" + uidl + "/" + msgid);
                        imessage.setFlag(Flags.Flag.DELETED, true);
                        break;
                    }
                }

                if (found) {
                    try {
                        Log.i(folder.name + " POP expunge=" + found);
                        ifolder.close(true);
                        ifolder.open(Folder.READ_WRITE);
                    } catch (Throwable ex) {
                        Log.e(ex);
                        state.error(new FolderClosedException(ifolder, "POP", new Exception(ex)));
                    }
                }
            }

            // Synchronize will delete messages when needed
            db.message().setMessageUiHide(message.id, true);
        } else
            db.message().deleteMessage(message.id);

        if (!EntityFolder.DRAFTS.equals(folder.type) &&
                !EntityFolder.TRASH.equals(folder.type)) {
            EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
            if (trash == null) {
                trash = new EntityFolder();
                trash.account = account.id;
                trash.name = context.getString(R.string.title_folder_trash);
                trash.type = EntityFolder.TRASH;
                trash.synchronize = false;
                trash.unified = false;
                trash.notify = false;
                trash.sync_days = Integer.MAX_VALUE;
                trash.keep_days = Integer.MAX_VALUE;
                trash.initialize = 0;
                trash.id = db.folder().insertFolder(trash);
            }

            long id = message.id;

            message.id = null;
            message.folder = trash.id;
            message.msgid = null; // virtual message
            message.ui_hide = false;
            message.ui_seen = true;
            message.id = db.message().insertMessage(message);

            try {
                File source = EntityMessage.getFile(context, id);
                File target = message.getFile(context);
                Helper.copy(source, target);
            } catch (IOException ex) {
                Log.e(ex);
            }

            EntityAttachment.copy(context, id, message.id);
        }
    }

    private static void onHeaders(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, IOException {
        // Download headers
        DB db = DB.getInstance(context);

        if (message.headers != null)
            return;

        IMAPMessage imessage = (IMAPMessage) ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        MessageHelper helper = new MessageHelper(imessage, context);
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
            }

            db.message().setMessageRaw(message.id, true);
        }

        if (jargs.length() > 0) {
            // Cross account move
            long tid = jargs.getLong(0);
            EntityFolder target = db.folder().getFolder(tid);
            if (target == null)
                throw new FolderNotFoundException();

            Log.i(folder.name + " queuing ADD id=" + message.id + ":" + target.id);

            EntityOperation operation = new EntityOperation();
            operation.account = target.account;
            operation.folder = target.id;
            operation.message = message.id;
            operation.name = EntityOperation.ADD;
            operation.args = jargs.toString();
            operation.created = new Date().getTime();
            operation.id = db.operation().insertOperation(operation);
        }
    }

    private static void onBody(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, IOException {
        // Download message body
        DB db = DB.getInstance(context);

        if (message.content)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);
        MessageHelper.MessageParts parts = helper.getMessageParts();
        String body = parts.getHtml(context);
        File file = message.getFile(context);
        Helper.writeText(file, body);
        String text = HtmlHelper.getFullText(body);
        message.preview = HtmlHelper.getPreview(text);
        message.language = HtmlHelper.getLanguage(context, message.subject, text);
        db.message().setMessageContent(message.id,
                true,
                message.language,
                parts.isPlainOnly(),
                message.preview,
                parts.getWarnings(message.warning));
        MessageClassifier.classify(message, folder, null, context);

        if (body != null)
            EntityLog.log(context, "Operation body size=" + body.length());
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
        if (attachment.subsequence != null)
            throw new IllegalArgumentException("Download of sub attachment");
        if (attachment.available)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Get message parts
        MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);
        MessageHelper.MessageParts parts = helper.getMessageParts();

        // Download attachment
        parts.downloadAttachment(context, attachment);

        if (attachment.size != null)
            EntityLog.log(context, "Operation attachment size=" + attachment.size);
    }

    private static void onExists(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws MessagingException, IOException {
        if (message.uid != null)
            return;

        if (message.msgid == null)
            throw new IllegalArgumentException("exists without msgid");

        Message[] imessages = ifolder.search(new MessageIDTerm(message.msgid));
        if (imessages == null || imessages.length == 0)
            try {
                // Needed for Outlook
                imessages = ifolder.search(
                        new AndTerm(
                                new SentDateTerm(ComparisonTerm.GE, new Date()),
                                new HeaderTerm(MessageHelper.HEADER_CORRELATION_ID, message.msgid)));
            } catch (MessagingException ex) {
                Log.e(ex);
            }

        if (imessages != null && imessages.length == 1) {
            String msgid;
            try {
                MessageHelper helper = new MessageHelper((MimeMessage) imessages[0], context);
                msgid = helper.getMessageID();
            } catch (MessagingException ex) {
                Log.e(ex);
                msgid = message.msgid;
            }
            if (Objects.equals(message.msgid, msgid)) {
                long uid = ifolder.getUID(imessages[0]);
                EntityOperation.queue(context, folder, EntityOperation.FETCH, uid);
            } else {
                EntityOperation.queue(context, message, EntityOperation.ADD);
            }
        } else {
            if (imessages != null && imessages.length > 1)
                Log.e(folder.name + " EXISTS messages=" + imessages.length);
            EntityOperation.queue(context, message, EntityOperation.ADD);
        }
    }

    static void onSynchronizeFolders(
            Context context, EntityAccount account, Store istore,
            State state, boolean force) throws MessagingException {
        DB db = DB.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sync_folders = (prefs.getBoolean("sync_folders", true) || force);
        boolean sync_shared_folders = prefs.getBoolean("sync_shared_folders", false);
        boolean subscriptions = prefs.getBoolean("subscriptions", false);
        boolean sync_subscribed = prefs.getBoolean("sync_subscribed", false);

        // Get folder names
        boolean drafts = false;
        Map<String, EntityFolder> local = new HashMap<>();
        List<EntityFolder> folders = db.folder().getFolders(account.id, false, false);
        for (EntityFolder folder : folders)
            if (folder.tbc != null) {
                Log.i(folder.name + " creating");
                Folder ifolder = istore.getFolder(folder.name);
                if (!ifolder.exists()) {
                    ifolder.create(Folder.HOLDS_MESSAGES);
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
                    if (subscribed)
                        ifolder.setSubscribed(false);

                    Folder itarget = istore.getFolder(folder.rename);
                    ifolder.renameTo(itarget);

                    if (subscribed && folder.selectable)
                        try {
                            itarget.open(READ_WRITE);
                            itarget.setSubscribed(subscribed);
                            itarget.close();
                        } catch (MessagingException ex) {
                            Log.w(ex);
                        }

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
                if (EntityFolder.DRAFTS.equals(folder.type))
                    drafts = true;

                if (folder.local) {
                    if (!EntityFolder.DRAFTS.equals(folder.type)) {
                        List<Long> ids = db.message().getMessageByFolder(folder.id);
                        if (ids == null || ids.size() == 0)
                            db.folder().deleteFolder(folder.id);
                    }
                } else {
                    local.put(folder.name, folder);
                    if (folder.synchronize && folder.initialize != 0)
                        sync_folders = true;
                }
            }
        Log.i("Local folder count=" + local.size() + " drafts=" + drafts);

        if (!drafts) {
            EntityFolder d = new EntityFolder();
            d.account = account.id;
            d.name = context.getString(R.string.title_folder_local_drafts);
            d.type = EntityFolder.DRAFTS;
            d.local = true;
            d.setProperties();
            d.synchronize = false;
            d.download = false;
            d.sync_days = Integer.MAX_VALUE;
            d.keep_days = Integer.MAX_VALUE;
            db.folder().insertFolder(d);
        }

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
        List<Folder> ifolders = new ArrayList<>(Arrays.asList(defaultFolder.list("*")));

        List<String> subscription = new ArrayList<>();
        try {
            Folder[] isubscribed = defaultFolder.listSubscribed("*");
            for (Folder ifolder : isubscribed) {
                String fullName = ifolder.getFullName();
                if (TextUtils.isEmpty(fullName)) {
                    Log.w("Subscribed folder name empty namespace=" + defaultFolder.getFullName());
                    continue;
                }
                subscription.add(fullName);
                Log.i("Subscribed " + defaultFolder.getFullName() + ":" + fullName);
            }
        } catch (Throwable ex) {
            /*
                06-21 10:02:38.035  9927 10024 E fairemail: java.lang.NullPointerException: Folder name is null
                06-21 10:02:38.035  9927 10024 E fairemail: 	at com.sun.mail.imap.IMAPFolder.<init>(SourceFile:372)
                06-21 10:02:38.035  9927 10024 E fairemail: 	at com.sun.mail.imap.IMAPFolder.<init>(SourceFile:411)
                06-21 10:02:38.035  9927 10024 E fairemail: 	at com.sun.mail.imap.IMAPStore.newIMAPFolder(SourceFile:1809)
                06-21 10:02:38.035  9927 10024 E fairemail: 	at com.sun.mail.imap.DefaultFolder.listSubscribed(SourceFile:89)
             */
            Log.e(account.name, ex);
        }

        if (sync_shared_folders) {
            // https://tools.ietf.org/html/rfc2342
            Folder[] namespaces = istore.getSharedNamespaces();
            Log.i("Namespaces=" + namespaces.length);
            for (Folder namespace : namespaces) {
                Log.i("Namespace=" + namespace.getFullName());
                if (namespace.getSeparator() == separator) {
                    try {
                        ifolders.addAll(Arrays.asList(namespace.list("*")));
                    } catch (FolderNotFoundException ex) {
                        Log.w(ex);
                    }

                    try {
                        Folder[] isubscribed = namespace.listSubscribed("*");
                        for (Folder ifolder : isubscribed) {
                            String fullName = ifolder.getFullName();
                            if (TextUtils.isEmpty(fullName)) {
                                Log.e("Subscribed folder name empty namespace=" + namespace.getFullName());
                                continue;
                            }
                            subscription.add(fullName);
                            Log.i("Subscribed " + namespace.getFullName() + ":" + fullName);
                        }
                    } catch (Throwable ex) {
                        Log.e(account.name, ex);
                    }
                } else
                    Log.e("Namespace separator=" + namespace.getSeparator() + " default=" + separator);
            }
        }

        long duration = new Date().getTime() - start;

        Log.i("Remote folder count=" + ifolders.size() +
                " subscriptions=" + subscription.size() +
                " separator=" + separator +
                " fetched in " + duration + " ms");

        Map<String, EntityFolder> nameFolder = new HashMap<>();
        Map<String, List<EntityFolder>> parentFolders = new HashMap<>();
        for (Folder ifolder : ifolders) {
            String fullName = ifolder.getFullName();
            if (TextUtils.isEmpty(fullName)) {
                Log.e("Folder name empty");
                continue;
            }

            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
            String type = EntityFolder.getType(attrs, fullName, false);
            boolean subscribed = subscription.contains(fullName);

            boolean selectable = true;
            boolean inferiors = true;
            for (String attr : attrs) {
                if (attr.equalsIgnoreCase("\\NoSelect"))
                    selectable = false;
                if (attr.equalsIgnoreCase("\\NoInferiors"))
                    inferiors = false;
            }
            selectable = selectable && ((ifolder.getType() & IMAPFolder.HOLDS_MESSAGES) != 0);
            inferiors = inferiors && ((ifolder.getType() & IMAPFolder.HOLDS_FOLDERS) != 0);

            Log.i(account.name + ":" + fullName + " type=" + type +
                    " subscribed=" + subscribed +
                    " selectable=" + selectable +
                    " inferiors=" + inferiors +
                    " attrs=" + TextUtils.join(" ", attrs));

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
                        folder.type = (EntityFolder.SYSTEM.equals(type) ? type : EntityFolder.USER);
                        folder.synchronize = (subscribed && subscriptions && sync_subscribed);
                        folder.subscribed = subscribed;
                        folder.poll = true;
                        folder.sync_days = EntityFolder.DEFAULT_SYNC;
                        folder.keep_days = EntityFolder.DEFAULT_KEEP;
                        folder.selectable = selectable;
                        folder.inferiors = inferiors;
                        folder.setSpecials(account);
                        folder.id = db.folder().insertFolder(folder);
                        Log.i(folder.name + " added type=" + folder.type);
                    } else {
                        Log.i(folder.name + " exists type=" + folder.type);

                        if (folder.subscribed == null || !folder.subscribed.equals(subscribed))
                            db.folder().setFolderSubscribed(folder.id, subscribed);

                        if (folder.selectable != selectable)
                            db.folder().setFolderSelectable(folder.id, selectable);

                        if (folder.inferiors != inferiors)
                            db.folder().setFolderInferiors(folder.id, inferiors);

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

        Log.i("Creating folders parents=" + parentFolders.size());
        for (String parentName : parentFolders.keySet()) {
            EntityFolder parent = nameFolder.get(parentName);
            if (parent == null && parentName != null) {
                parent = db.folder().getFolderByName(account.id, parentName);
                if (parent == null) {
                    Log.i("Creating parent name=" + parentName);
                    parent = new EntityFolder();
                    parent.account = account.id;
                    parent.name = parentName;
                    parent.type = EntityFolder.SYSTEM;
                    parent.subscribed = false;
                    parent.selectable = false;
                    parent.inferiors = false;
                    parent.setProperties();
                    parent.display = parentName + "*";
                    parent.id = db.folder().insertFolder(parent);
                }
                nameFolder.put(parentName, parent);
            }
        }

        Log.i("Updating folders parents=" + parentFolders.size());
        for (Map.Entry<String, List<EntityFolder>> entry : parentFolders.entrySet()) {
            EntityFolder parent = nameFolder.get(entry.getKey());
            for (EntityFolder child : entry.getValue())
                db.folder().setFolderParent(child.id, parent == null ? null : parent.id);
        }

        Log.i("Delete local count=" + local.size());
        for (Map.Entry<String, EntityFolder> entry : local.entrySet()) {
            String name = entry.getKey();
            EntityFolder folder = entry.getValue();
            List<EntityFolder> childs = parentFolders.get(name);
            if (EntityFolder.USER.equals(folder.type) ||
                    childs == null || childs.size() == 0) {
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

    private static void onPurgeFolder(Context context, JSONArray jargs, EntityFolder folder, IMAPFolder ifolder) throws MessagingException {
        // Delete all messages from folder
        try {
            Log.i(folder.name + " purge=" + ifolder.getMessageCount());
            ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                @Override
                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                    MailboxInfo info = protocol.select(ifolder.getFullName());
                    if (info.total > 0) {
                        MessageSet[] sets = new MessageSet[]{new MessageSet(1, info.total)};
                        EntityLog.log(context, folder.name + " purging=" + MessageSet.toString(sets));
                        try {
                            protocol.storeFlags(sets, new Flags(Flags.Flag.DELETED), true);
                        } catch (ProtocolException ex) {
                            throw new ProtocolException("Purge=" + MessageSet.toString(sets), ex);
                        }
                    }
                    return null;
                }
            });
            Log.i(folder.name + " purge deleted");

            ifolder.expunge();
            Log.i(folder.name + " purge expunged");
        } catch (Throwable ex) {
            Log.e(ex);
            throw ex;
        } finally {
            EntityOperation.sync(context, folder.id, false);
        }
    }

    private static void onPurgeFolder(Context context, EntityFolder folder) {
        // POP3
        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            int purged = db.message().deleteHiddenMessages(folder.id);
            Log.i(folder.name + " purge count=" + purged);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void onRule(Context context, JSONArray jargs, EntityMessage message) throws JSONException, IOException, AddressException {
        // Download message body
        DB db = DB.getInstance(context);

        long id = jargs.getLong(0);
        EntityRule rule = db.rule().getRule(id);
        if (rule == null)
            throw new IllegalArgumentException("Rule not found id=" + id);

        if (!message.content)
            throw new IllegalArgumentException("Message without content id=" + rule.id + ":" + rule.name);

        rule.execute(context, message);
    }

    private static void onSynchronizeMessages(
            Context context, JSONArray jargs,
            EntityAccount account, final EntityFolder folder,
            POP3Folder ifolder, POP3Store istore, State state) throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify_known = prefs.getBoolean("notify_known", false);
        boolean pro = ActivityBilling.isPro(context);

        EntityLog.log(context, folder.name + " POP sync type=" + folder.type + " connected=" + (ifolder != null));

        if (!EntityFolder.INBOX.equals(folder.type)) {
            db.folder().setFolderSyncState(folder.id, null);
            return;
        }

        List<EntityRule> rules = db.rule().getEnabledRules(folder.id);

        try {
            db.folder().setFolderSyncState(folder.id, "syncing");

            Map<String, String> caps = istore.capabilities();
            EntityLog.log(context, folder.name + " POP capabilities= " + caps.keySet());

            Message[] imessages = ifolder.getMessages();
            EntityLog.log(context, folder.name + " POP messages=" + imessages.length);

            if (account.max_messages != null && imessages.length > account.max_messages)
                imessages = Arrays.copyOfRange(imessages,
                        imessages.length - account.max_messages, imessages.length);

            db.folder().setFolderSyncState(folder.id, "downloading");

            boolean hasUidl = caps.containsKey("UIDL");
            if (hasUidl) {
                FetchProfile ifetch = new FetchProfile();
                ifetch.add(UIDFolder.FetchProfileItem.UID);
                ifolder.fetch(imessages, ifetch);
            }

            List<TupleUidl> ids = db.message().getUidls(folder.id);
            EntityLog.log(context, folder.name + " POP existing=" + ids.size() + " uidl=" + hasUidl);

            // Index UIDLs
            Map<String, String> uidlMsgId = new HashMap<>();
            for (TupleUidl id : ids)
                if (id.uidl != null && id.msgid != null)
                    uidlMsgId.put(id.uidl, id.msgid);

            if (!account.leave_on_device) {
                if (hasUidl) {
                    Map<String, TupleUidl> known = new HashMap<>();
                    for (TupleUidl id : ids)
                        if (id.uidl != null)
                            known.put(id.uidl, id);

                    for (Message imessage : imessages) {
                        String uidl = ifolder.getUID(imessage);
                        if (TextUtils.isEmpty(uidl))
                            known.clear(); // better safe than sorry
                        else
                            known.remove(uidl);
                    }

                    for (TupleUidl uidl : known.values()) {
                        EntityLog.log(context, folder.name + " POP purging uidl=" + uidl.uidl);
                        db.message().deleteMessage(uidl.id);
                    }
                } else {
                    Map<String, TupleUidl> known = new HashMap<>();
                    for (TupleUidl id : ids)
                        if (id.msgid != null)
                            known.put(id.msgid, id);

                    for (Message imessage : imessages) {
                        MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);
                        String msgid = helper.getMessageID(); // expensive!
                        if (!TextUtils.isEmpty(msgid))
                            known.remove(msgid);
                    }

                    for (TupleUidl uidl : known.values()) {
                        EntityLog.log(context, folder.name + " POP purging msgid=" + uidl.msgid);
                        db.message().deleteMessage(uidl.id);
                    }
                }
            }

            for (Message imessage : imessages)
                try {
                    if (!state.isRunning())
                        return;

                    MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);

                    String uidl;
                    String msgid;
                    if (hasUidl) {
                        uidl = ifolder.getUID(imessage);
                        if (TextUtils.isEmpty(uidl)) {
                            EntityLog.log(context, folder.name + " POP no uidl");
                            continue;
                        }

                        msgid = uidlMsgId.get(uidl);
                        if (msgid == null) {
                            msgid = helper.getMessageID();
                            if (TextUtils.isEmpty(msgid))
                                msgid = uidl;
                        } else {
                            Log.i(folder.name + " POP having uidl=" + uidl);
                            continue;
                        }
                    } else {
                        uidl = null;
                        msgid = helper.getMessageID();

                        if (TextUtils.isEmpty(msgid)) {
                            Long time = helper.getReceived();
                            if (time == null)
                                time = helper.getSent();
                            if (time != null)
                                msgid = Long.toString(time);
                        }

                        if (db.message().countMessageByMsgId(folder.id, msgid) > 0) {
                            Log.i(folder.name + " POP having msgid=" + msgid);
                            continue;
                        }
                    }

                    if (TextUtils.isEmpty(msgid)) {
                        EntityLog.log(context, folder.name + " POP no msgid");
                        continue;
                    }

                    try {
                        Log.i(folder.name + " POP sync=" + uidl + "/" + msgid);

                        Long sent = helper.getSent();
                        Long received = helper.getReceivedHeader();
                        if (received == null)
                            received = sent;
                        if (received == null)
                            received = 0L;

                        boolean seen = (received <= account.created);

                        String[] authentication = helper.getAuthentication();
                        MessageHelper.MessageParts parts = helper.getMessageParts();

                        EntityMessage message = new EntityMessage();
                        message.account = folder.account;
                        message.folder = folder.id;
                        message.uid = null;
                        message.uidl = uidl;
                        message.msgid = msgid;
                        message.hash = helper.getHash();
                        message.references = TextUtils.join(" ", helper.getReferences());
                        message.inreplyto = helper.getInReplyTo();
                        message.deliveredto = helper.getDeliveredTo();
                        message.thread = helper.getThreadId(context, account.id, 0);
                        message.priority = helper.getPriority();
                        message.auto_submitted = helper.getAutoSubmitted();
                        message.receipt_request = helper.getReceiptRequested();
                        message.receipt_to = helper.getReceiptTo();
                        message.dkim = MessageHelper.getAuthentication("dkim", authentication);
                        message.spf = MessageHelper.getAuthentication("spf", authentication);
                        message.dmarc = MessageHelper.getAuthentication("dmarc", authentication);
                        message.submitter = helper.getSender();
                        message.from = helper.getFrom();
                        message.to = helper.getTo();
                        message.cc = helper.getCc();
                        message.bcc = helper.getBcc();
                        message.reply = helper.getReply();
                        message.list_post = helper.getListPost();
                        message.unsubscribe = helper.getListUnsubscribe();
                        message.headers = helper.getHeaders();
                        message.subject = helper.getSubject();
                        message.size = parts.getBodySize();
                        message.total = helper.getSize();
                        message.content = false;
                        message.encrypt = parts.getEncryption();
                        message.ui_encrypt = message.encrypt;
                        message.received = received;
                        message.sent = sent;
                        message.seen = seen;
                        message.answered = false;
                        message.flagged = false;
                        message.flags = null;
                        message.keywords = new String[0];
                        message.ui_seen = seen;
                        message.ui_answered = false;
                        message.ui_flagged = false;
                        message.ui_hide = false;
                        message.ui_found = false;
                        message.ui_ignored = false;
                        message.ui_browsed = false;

                        if (MessageHelper.equalEmail(message.submitter, message.from))
                            message.submitter = null;

                        if (message.size == null && message.total != null)
                            message.size = message.total;

                        EntityIdentity identity = matchIdentity(context, folder, message);
                        message.identity = (identity == null ? null : identity.id);

                        message.sender = MessageHelper.getSortKey(message.from);
                        Uri lookupUri = ContactInfo.getLookupUri(message.from);
                        message.avatar = (lookupUri == null ? null : lookupUri.toString());
                        if (message.avatar == null && notify_known && pro)
                            message.ui_ignored = true;

                        // No MX check

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

                            runRules(context, imessage, account, folder, message, rules);
                            reportNewMessage(context, account, folder, message);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        String body = parts.getHtml(context);
                        File file = message.getFile(context);
                        Helper.writeText(file, body);
                        String text = HtmlHelper.getFullText(body);
                        message.preview = HtmlHelper.getPreview(text);
                        message.language = HtmlHelper.getLanguage(context, message.subject, text);
                        db.message().setMessageContent(message.id,
                                true,
                                message.language,
                                parts.isPlainOnly(),
                                message.preview,
                                parts.getWarnings(message.warning));

                        for (EntityAttachment attachment : parts.getAttachments())
                            if (attachment.subsequence == null)
                                parts.downloadAttachment(context, attachment);

                        if (message.received > account.created)
                            updateContactInfo(context, folder, message);
                    } catch (Throwable ex) {
                        db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                    }
                } finally {
                    ((POP3Message) imessage).invalidate(true);
                }

            db.folder().setFolderLastSync(folder.id, new Date().getTime());
            EntityLog.log(context, folder.name + " POP done");
        } finally {
            db.folder().setFolderSyncState(folder.id, null);
        }
    }

    private static void onSynchronizeMessages(
            Context context, JSONArray jargs,
            EntityAccount account, final EntityFolder folder,
            IMAPStore istore, final IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        final DB db = DB.getInstance(context);
        try {
            SyncStats stats = new SyncStats();

            // Legacy
            if (jargs.length() == 0)
                jargs = folder.getSyncArgs(false);

            int sync_days = jargs.getInt(0);
            int keep_days = jargs.getInt(1);
            boolean download = jargs.optBoolean(2, false);
            boolean auto_delete = jargs.optBoolean(3, false);
            int initialize = jargs.optInt(4, folder.initialize);
            boolean force = jargs.optBoolean(5, false);

            if (keep_days == sync_days && keep_days != Integer.MAX_VALUE)
                keep_days++;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean sync_nodate = prefs.getBoolean("sync_nodate", false);
            boolean sync_unseen = prefs.getBoolean("sync_unseen", false);
            boolean sync_flagged = prefs.getBoolean("sync_flagged", false);
            boolean sync_kept = prefs.getBoolean("sync_kept", true);
            boolean delete_unseen = prefs.getBoolean("delete_unseen", false);

            Log.i(folder.name + " start sync after=" + sync_days + "/" + keep_days +
                    " force=" + force +
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
                Log.w(folder.name, ex);
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
            final List<Long> uids = db.message().getUids(folder.id, sync_kept || force ? null : sync_time);
            Log.i(folder.name + " local count=" + uids.size());

            // Reduce list of local uids
            Flags flags = ifolder.getPermanentFlags();
            SearchTerm searchTerm = account.use_date
                    ? new SentDateTerm(ComparisonTerm.GE, new Date(sync_time))
                    : new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time));
            if (sync_nodate)
                searchTerm = new OrTerm(searchTerm, new ReceivedDateTerm(ComparisonTerm.LT, new Date(365 * 24 * 3600 * 1000L)));
            if (sync_unseen && flags.contains(Flags.Flag.SEEN))
                searchTerm = new OrTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            if (sync_flagged && flags.contains(Flags.Flag.FLAGGED))
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
            if (imessages == null)
                imessages = new Message[0];

            stats.search_ms = (SystemClock.elapsedRealtime() - search);
            Log.i(folder.name + " remote count=" + imessages.length + " search=" + stats.search_ms + " ms");

            long fetch = SystemClock.elapsedRealtime();
            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID); // To check if message exists
            fp.add(FetchProfile.Item.FLAGS); // To update existing messages
            if (account.isGmail())
                fp.add(GmailFolder.FetchProfileItem.LABELS);
            ifolder.fetch(imessages, fp);

            stats.flags = imessages.length;
            stats.flags_ms = (SystemClock.elapsedRealtime() - fetch);
            Log.i(folder.name + " remote fetched=" + stats.flags_ms + " ms");

            // Sort for finding referenced/replied-to messages
            // Sorting on date/time would be better, but requires fetching the headers
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

            int expunge = 0;
            for (int i = 0; i < imessages.length && state.isRunning() && state.isRecoverable(); i++)
                try {
                    if (imessages[i].isSet(Flags.Flag.DELETED))
                        expunge++;
                    else
                        uids.remove(ifolder.getUID(imessages[i]));
                } catch (MessageRemovedException ex) {
                    Log.w(folder.name, ex);
                } catch (Throwable ex) {
                    Log.e(folder.name, ex);
                    EntityLog.log(context, folder.name + " " + Log.formatThrowable(ex, false));
                    db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                }

            if (expunge > 0)
                try {
                    Log.i(folder.name + " expunging=" + expunge);
                    ifolder.expunge();
                } catch (Throwable ex) {
                    Log.w(ex);
                }

            if (uids.size() > 0) {
                // This is done outside of JavaMail to prevent changed notifications
                if (!ifolder.isOpen())
                    throw new FolderClosedException(ifolder, "UID FETCH");

                long getuid = SystemClock.elapsedRealtime();
                MessagingException ex = (MessagingException) ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                    @Override
                    public Object doCommand(IMAPProtocol protocol) {
                        try {
                            protocol.select(folder.name);
                        } catch (ProtocolException ex) {
                            return new MessagingException("UID FETCH", ex);
                        }

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

                stats.uids = uids.size();
                stats.uids_ms = (SystemClock.elapsedRealtime() - getuid);
                Log.i(folder.name + " remote uids=" + stats.uids_ms + " ms");
            }

            // Delete local messages not at remote
            Log.i(folder.name + " delete=" + uids.size());
            for (Long uid : uids) {
                int count = db.message().deleteMessage(folder.id, uid);
                Log.i(folder.name + " delete local uid=" + uid + " count=" + count);
            }

            List<EntityRule> rules = db.rule().getEnabledRules(folder.id);

            fp.add(FetchProfile.Item.ENVELOPE);
            //fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            //fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            if (account.isGmail())
                fp.add(GmailFolder.FetchProfileItem.THRID);

            // Add/update local messages
            int synced = 0;
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
                    stats.headers += full.size();
                    stats.headers_ms += (SystemClock.elapsedRealtime() - headers);
                    Log.i(folder.name + " fetched headers=" + full.size() + " " + stats.headers_ms + " ms");
                }

                int free = Log.getFreeMemMb();
                Map<String, String> crumb = new HashMap<>();
                crumb.put("account", account.id + ":" + account.protocol);
                crumb.put("folder", folder.id + ":" + folder.type);
                crumb.put("start", Integer.toString(from));
                crumb.put("end", Integer.toString(i));
                crumb.put("free", Integer.toString(free));
                crumb.put("partial", Boolean.toString(account.partial_fetch));
                Log.breadcrumb("sync", crumb);
                Log.i("Sync " + from + ".." + i + " free=" + free);

                for (int j = isub.length - 1; j >= 0 && state.isRunning() && state.isRecoverable(); j--)
                    try {
                        // Some providers erroneously return old messages
                        if (full.contains(isub[j]))
                            try {
                                Date received = isub[j].getReceivedDate();
                                boolean unseen = (sync_unseen && !isub[j].isSet(Flags.Flag.SEEN));
                                boolean flagged = (sync_flagged && isub[j].isSet(Flags.Flag.FLAGGED));
                                if (received != null && received.getTime() < keep_time && !unseen && !flagged) {
                                    long uid = ifolder.getUID(isub[j]);
                                    Log.i(folder.name + " Skipping old uid=" + uid + " date=" + received);
                                    ids[from + j] = null;
                                    continue;
                                }
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }

                        EntityMessage message = synchronizeMessage(
                                context,
                                account, folder,
                                istore, ifolder, (MimeMessage) isub[j],
                                false, download && initialize == 0,
                                rules, state, stats);
                        ids[from + j] = (message == null || message.ui_hide ? null : message.id);

                        if (message != null && full.contains(isub[j]))
                            if ((++synced % SYNC_YIELD_COUNT) == 0)
                                try {
                                    Log.i(folder.name + " yield synced=" + synced);
                                    Thread.sleep(SYNC_YIELD_DURATION);
                                } catch (InterruptedException ex) {
                                    Log.w(ex);
                                }
                    } catch (MessageRemovedException ex) {
                        Log.w(folder.name, ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(folder.name, ex);
                            db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(folder.name, ex);
                        db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                    } finally {
                        // Free memory
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }
            }

            // Delete not synchronized messages without uid
            if (!EntityFolder.isOutgoing(folder.type)) {
                int orphans = db.message().deleteOrphans(folder.id);
                Log.i(folder.name + " deleted orphans=" + orphans);
            }

            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);
            account.last_connected = new Date().getTime();
            db.account().setAccountConnected(account.id, account.last_connected);

            if (download && initialize == 0) {
                db.folder().setFolderSyncState(folder.id, "downloading");

                // Download messages/attachments
                int downloaded = 0;
                Log.i(folder.name + " download=" + imessages.length);
                for (int i = imessages.length - 1; i >= 0 && state.isRunning() && state.isRecoverable(); i -= DOWNLOAD_BATCH_SIZE) {
                    int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);

                    Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                    // Fetch on demand

                    int free = Log.getFreeMemMb();
                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("account", account.id + ":" + account.protocol);
                    crumb.put("folder", folder.id + ":" + folder.type);
                    crumb.put("start", Integer.toString(from));
                    crumb.put("end", Integer.toString(i));
                    crumb.put("free", Integer.toString(free));
                    crumb.put("partial", Boolean.toString(account.partial_fetch));
                    Log.breadcrumb("download", crumb);
                    Log.i("Download " + from + ".." + i + " free=" + free);

                    for (int j = isub.length - 1; j >= 0 && state.isRunning() && state.isRecoverable(); j--)
                        try {
                            if (ids[from + j] != null) {
                                boolean fetched = downloadMessage(
                                        context,
                                        account, folder,
                                        istore, ifolder,
                                        (MimeMessage) isub[j], ids[from + j],
                                        state, stats);
                                if (fetched)
                                    if ((++downloaded % DOWNLOAD_YIELD_COUNT) == 0)
                                        try {
                                            Log.i(folder.name + " yield downloaded=" + downloaded);
                                            Thread.sleep(DOWNLOAD_YIELD_DURATION);
                                        } catch (InterruptedException ex) {
                                            Log.w(ex);
                                        }
                            }
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

            stats.total = (SystemClock.elapsedRealtime() - search);

            EntityLog.log(context, account.name + "/" + folder.name + " sync stats " + stats);
        } finally {
            Log.i(folder.name + " end sync state=" + state);
            db.folder().setFolderSyncState(folder.id, null);
        }
    }

    static EntityMessage synchronizeMessage(
            Context context,
            EntityAccount account, EntityFolder folder,
            IMAPStore istore, IMAPFolder ifolder, MimeMessage imessage,
            boolean browsed, boolean download,
            List<EntityRule> rules, State state, SyncStats stats) throws MessagingException, IOException {

        long uid = ifolder.getUID(imessage);
        if (uid < 0) {
            Log.i(folder.name + " invalid uid=" + uid);
            throw new MessageRemovedException("uid");
        }

        if (imessage.isExpunged()) {
            Log.i(folder.name + " expunged uid=" + uid);
            throw new MessageRemovedException("Expunged");
        }
        if (imessage.isSet(Flags.Flag.DELETED)) {
            Log.i(folder.name + " deleted uid=" + uid);
            throw new MessageRemovedException("Flagged deleted");
        }

        MessageHelper helper = new MessageHelper(imessage, context);
        boolean seen = helper.getSeen();
        boolean answered = helper.getAnswered();
        boolean flagged = helper.getFlagged();
        String flags = helper.getFlags();
        String[] keywords = helper.getKeywords();
        String[] labels = helper.getLabels();
        boolean update = false;
        boolean process = false;
        boolean syncSimilar = false;

        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify_known = prefs.getBoolean("notify_known", false);
        boolean pro = ActivityBilling.isPro(context);

        // Find message by uid (fast, no headers required)
        EntityMessage message = db.message().getMessageByUid(folder.id, uid);

        // Find message by Message-ID (slow, headers required)
        // - messages in inbox have same id as message sent to self
        // - messages in archive have same id as original
        Integer color = null;
        if (message == null) {
            String msgid = helper.getMessageID();
            Log.i(folder.name + " searching for " + msgid);
            List<EntityMessage> dups = db.message().getMessagesByMsgId(folder.account, msgid);
            for (EntityMessage dup : dups) {
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

                        if (EntityFolder.SENT.equals(folder.type)) {
                            Long sent = helper.getSent();
                            Long received = helper.getReceived();
                            if (sent != null)
                                dup.sent = sent;
                            if (received != null)
                                dup.received = received;
                        }

                        dup.error = null;

                        message = dup;
                        process = true;
                    }
                }

                if (dup.seen != seen || dup.answered != answered || dup.flagged != flagged)
                    syncSimilar = true;

                if (dup.flagged && dup.color != null)
                    color = dup.color;
            }
        }

        if (message == null) {
            Long sent = helper.getSent();

            Long received;
            long future = new Date().getTime() + FUTURE_RECEIVED;
            if (account.use_date) {
                received = sent;
                if (received == null || received == 0 || received > future)
                    received = helper.getReceived();
                if (received == null || received == 0 || received > future)
                    received = helper.getReceivedHeader();
            } else if (account.use_received) {
                received = helper.getReceivedHeader();
                if (received == null || received == 0 || received > future)
                    received = helper.getReceived();
            } else {
                received = helper.getReceived();
                if (received == null || received == 0 || received > future)
                    received = helper.getReceivedHeader();
            }
            if (received == null)
                received = 0L;

            String[] authentication = helper.getAuthentication();
            MessageHelper.MessageParts parts = helper.getMessageParts();

            message = new EntityMessage();
            message.account = folder.account;
            message.folder = folder.id;
            message.uid = uid;

            message.msgid = helper.getMessageID();
            if (TextUtils.isEmpty(message.msgid))
                Log.w("No Message-ID id=" + message.id + " uid=" + message.uid);

            message.hash = helper.getHash();
            message.references = TextUtils.join(" ", helper.getReferences());
            message.inreplyto = helper.getInReplyTo();
            // Local address contains control or whitespace in string ``mailing list someone@example.org''
            message.deliveredto = helper.getDeliveredTo();
            message.thread = helper.getThreadId(context, account.id, uid);
            message.priority = helper.getPriority();
            message.auto_submitted = helper.getAutoSubmitted();
            message.receipt_request = helper.getReceiptRequested();
            message.receipt_to = helper.getReceiptTo();
            message.dkim = MessageHelper.getAuthentication("dkim", authentication);
            message.spf = MessageHelper.getAuthentication("spf", authentication);
            message.dmarc = MessageHelper.getAuthentication("dmarc", authentication);
            message.submitter = helper.getSender();
            message.from = helper.getFrom();
            message.to = helper.getTo();
            message.cc = helper.getCc();
            message.bcc = helper.getBcc();
            message.reply = helper.getReply();
            message.list_post = helper.getListPost();
            message.unsubscribe = helper.getListUnsubscribe();
            message.autocrypt = helper.getAutocrypt();
            message.subject = helper.getSubject();
            message.size = parts.getBodySize();
            message.total = helper.getSize();
            message.content = false;
            message.encrypt = parts.getEncryption();
            message.ui_encrypt = message.encrypt;
            message.received = received;
            message.sent = sent;
            message.seen = seen;
            message.answered = answered;
            message.flagged = flagged;
            message.flags = flags;
            message.keywords = keywords;
            message.labels = labels;
            message.ui_seen = seen;
            message.ui_answered = answered;
            message.ui_flagged = flagged;
            message.ui_hide = false;
            message.ui_found = false;
            message.ui_ignored = seen;
            message.ui_browsed = browsed;

            if (message.flagged)
                message.color = color;

            if (MessageHelper.equalEmail(message.submitter, message.from))
                message.submitter = null;

            // Borrow reply name from sender name
            if (message.from != null && message.from.length == 1 &&
                    message.reply != null && message.reply.length == 1) {
                InternetAddress reply = (InternetAddress) message.reply[0];
                if (TextUtils.isEmpty(reply.getPersonal())) {
                    InternetAddress from = (InternetAddress) message.from[0];
                    reply.setPersonal(from.getPersonal());
                }
            }

            EntityIdentity identity = matchIdentity(context, folder, message);
            message.identity = (identity == null ? null : identity.id);

            message.sender = MessageHelper.getSortKey(EntityFolder.isOutgoing(folder.type) ? message.to : message.from);
            Uri lookupUri = ContactInfo.getLookupUri(message.from);
            message.avatar = (lookupUri == null ? null : lookupUri.toString());
            if (message.avatar == null && notify_known && pro)
                message.ui_ignored = true;

            // For contact forms
            boolean self = false;
            if (identity != null && message.from != null)
                for (Address from : message.from)
                    if (identity.sameAddress(from) || identity.similarAddress(from)) {
                        self = true;
                        break;
                    }
            if (!self) {
                String warning = message.checkReplyDomain(context);
                message.reply_domain = (warning == null);
            }

            boolean check_mx = prefs.getBoolean("check_mx", false);
            if (check_mx)
                try {
                    Address[] addresses = (message.reply == null || message.reply.length == 0
                            ? message.from : message.reply);
                    DnsHelper.checkMx(context, addresses);
                    message.mx = true;
                } catch (UnknownHostException ex) {
                    message.mx = false;
                    message.warning = ex.getMessage();
                } catch (Throwable ex) {
                    Log.e(folder.name, ex);
                    message.warning = Log.formatThrowable(ex, false);
                }

            boolean check_spam = prefs.getBoolean("check_spam", false);
            if (check_spam) {
                String host = helper.getReceivedFromHost();
                if (host != null) {
                    try {
                        InetAddress addr = InetAddress.getByName(host);
                        Log.i("Received from " + host + "=" + addr);

                        StringBuilder lookup = new StringBuilder();
                        if (addr instanceof Inet4Address) {
                            List<String> a = Arrays.asList(addr.getHostAddress().split("\\."));
                            Collections.reverse(a);
                            lookup.append(TextUtils.join(".", a)).append('.');
                        } else if (addr instanceof Inet6Address) {
                            StringBuilder sb = new StringBuilder();
                            byte[] a = addr.getAddress();
                            for (int i = 0; i < 8; i++)
                                sb.append(String.format("%02x",
                                        ((a[i << 1] << 8) & 0xff00) | (a[(i << 1) + 1] & 0xff)));
                            sb.reverse();
                            for (char kar : sb.toString().toCharArray())
                                lookup.append(kar).append('.');
                        }

                        lookup.append("zen.spamhaus.org");

                        try {
                            InetAddress.getByName(lookup.toString());
                            if (message.warning == null)
                                message.warning = lookup.toString();
                            else
                                message.warning += ", " + lookup;
                        } catch (UnknownHostException ignore) {
                            // Not blocked
                        }
                    } catch (UnknownHostException ex) {
                        Log.w(ex);
                    } catch (Throwable ex) {
                        Log.w(folder.name, ex);
                    }
                }
            }

            try {
                db.beginTransaction();

                message.notifying = EntityMessage.NOTIFYING_IGNORE;
                message.id = db.message().insertMessage(message);
                Log.i(folder.name + " added id=" + message.id + " uid=" + message.uid);

                int sequence = 1;
                List<EntityAttachment> attachments = parts.getAttachments();
                for (EntityAttachment attachment : attachments) {
                    Log.i(folder.name + " attachment seq=" + sequence + " " + attachment);
                    attachment.message = message.id;
                    attachment.sequence = sequence++;
                    attachment.id = db.attachment().insertAttachment(attachment);
                }

                runRules(context, imessage, account, folder, message, rules);
                if (download && !message.ui_hide &&
                        MessageClassifier.isEnabled(context) && folder.auto_classify_source)
                    db.message().setMessageUiHide(message.id, true); // keep local value

                db.setTransactionSuccessful();
            } catch (SQLiteConstraintException ex) {
                Log.e(ex);

                Map<String, String> crumb = new HashMap<>();
                crumb.put("folder", message.account + ":" + message.folder + ":" + folder.type);
                crumb.put("message", uid + ":" + message.uid);
                crumb.put("what", ex.getMessage());
                Log.breadcrumb("insert", crumb);

                return null;
            } finally {
                db.endTransaction();
            }

            try {
                if (message.received > account.created)
                    updateContactInfo(context, folder, message);

                // Download small messages inline
                if (download && !message.ui_hide) {
                    long maxSize;
                    if (state == null || state.networkState.isUnmetered())
                        maxSize = MessageHelper.SMALL_MESSAGE_SIZE;
                    else {
                        maxSize = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
                        if (maxSize == 0 || maxSize > MessageHelper.SMALL_MESSAGE_SIZE)
                            maxSize = MessageHelper.SMALL_MESSAGE_SIZE;
                    }

                    if ((message.size != null && message.size < maxSize) ||
                            (MessageClassifier.isEnabled(context)) && folder.auto_classify_source)
                        try {
                            String body = parts.getHtml(context);
                            File file = message.getFile(context);
                            Helper.writeText(file, body);
                            String text = HtmlHelper.getFullText(body);
                            message.preview = HtmlHelper.getPreview(text);
                            message.language = HtmlHelper.getLanguage(context, message.subject, text);
                            db.message().setMessageContent(message.id,
                                    true,
                                    message.language,
                                    parts.isPlainOnly(),
                                    message.preview,
                                    parts.getWarnings(message.warning));
                            MessageClassifier.classify(message, folder, null, context);

                            if (stats != null && body != null)
                                stats.content += body.length();
                            Log.i(folder.name + " inline downloaded message id=" + message.id +
                                    " size=" + message.size + "/" + (body == null ? null : body.length()));

                            if (TextUtils.isEmpty(body) && parts.hasBody())
                                reportEmptyMessage(context, state, account, istore);
                        } finally {
                            if (!message.ui_hide)
                                db.message().setMessageUiHide(message.id, false);
                        }
                }
            } finally {
                db.message().setMessageNotifying(message.id, 0);
            }

            reportNewMessage(context, account, folder, message);
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
                syncSimilar = true;
            }

            if ((!message.answered.equals(answered) || !message.ui_answered.equals(message.answered)) &&
                    db.operation().getOperationCount(folder.id, message.id, EntityOperation.ANSWERED) == 0) {
                update = true;
                message.answered = answered;
                message.ui_answered = answered;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " answered=" + answered);
                syncSimilar = true;
            }

            if ((!message.flagged.equals(flagged) || !message.ui_flagged.equals(flagged)) &&
                    db.operation().getOperationCount(folder.id, message.id, EntityOperation.FLAG) == 0) {
                update = true;
                message.flagged = flagged;
                message.ui_flagged = flagged;
                if (!flagged)
                    message.color = null;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flagged=" + flagged);
                syncSimilar = true;
            }

            if (!Objects.equals(flags, message.flags)) {
                update = true;
                message.flags = flags;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flags=" + flags);
            }

            if (!Helper.equal(message.keywords, keywords) &&
                    ifolder.getPermanentFlags().contains(Flags.Flag.USER)) {
                update = true;
                message.keywords = keywords;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid +
                        " keywords=" + TextUtils.join(" ", keywords));
            }

            if (!Helper.equal(message.labels, labels)) {
                update = true;
                message.labels = labels;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid +
                        " labels=" + (labels == null ? null : TextUtils.join(" ", labels)));
            }

            if (message.hash == null || process) {
                update = true;
                message.hash = helper.getHash();
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " hash=" + message.hash);

                // Update archive to prevent visible > 1
                if (EntityFolder.DRAFTS.equals(folder.type))
                    for (EntityMessage dup : db.message().getMessagesByMsgId(message.account, message.msgid))
                        db.message().setMessageHash(dup.id, message.hash);
            }

            if (message.ui_hide &&
                    message.ui_snoozed == null &&
                    (message.ui_busy == null || message.ui_busy < new Date().getTime()) &&
                    db.operation().getOperationCount(folder.id, message.id) == 0 &&
                    db.operation().getOperationCount(folder.id, EntityOperation.PURGE) == 0) {
                update = true;
                message.ui_hide = false;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " unhide");
            }

            if (message.ui_browsed != browsed) {
                update = true;
                message.ui_browsed = browsed;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " browsed=" + browsed);
            }

            Uri uri = ContactInfo.getLookupUri(message.from);
            if (uri != null) {
                String avatar = uri.toString();
                if (!Objects.equals(message.avatar, avatar)) {
                    update = true;
                    message.avatar = avatar;
                    Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " avatar=" + avatar);
                }
            }

            if (update || process)
                try {
                    db.beginTransaction();

                    db.message().updateMessage(message);

                    if (process)
                        runRules(context, imessage, account, folder, message, rules);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

            if (process) {
                updateContactInfo(context, folder, message);
                MessageClassifier.classify(message, folder, null, context);
            } else
                Log.d(folder.name + " unchanged uid=" + uid);

            if (process)
                reportNewMessage(context, account, folder, message);
        }

        if (syncSimilar && account.isGmail())
            for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid)) {
                if (similar.seen != message.seen) {
                    Log.i(folder.name + " Synchronize similar id=" + similar.id + " seen=" + message.seen);
                    db.message().setMessageSeen(similar.id, message.seen);
                    db.message().setMessageUiSeen(similar.id, message.seen);
                }
                if (similar.answered != message.answered) {
                    Log.i(folder.name + " Synchronize similar id=" + similar.id + " answered=" + message.answered);
                    db.message().setMessageAnswered(similar.id, message.answered);
                }
                if (similar.flagged != flagged) {
                    Log.i(folder.name + " Synchronize similar id=" + similar.id + " flagged=" + message.flagged);
                    db.message().setMessageFlagged(similar.id, message.flagged);
                    db.message().setMessageUiFlagged(similar.id, message.flagged, flagged ? similar.color : null);
                }
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

    private static EntityIdentity matchIdentity(Context context, EntityFolder folder, EntityMessage message) {
        DB db = DB.getInstance(context);

        if (EntityFolder.DRAFTS.equals(folder.type))
            return null;

        List<Address> addresses = new ArrayList<>();
        if (folder.isOutgoing()) {
            if (message.from != null)
                addresses.addAll(Arrays.asList(message.from));
        } else {
            if (message.to != null)
                addresses.addAll(Arrays.asList(message.to));
            if (message.cc != null)
                addresses.addAll(Arrays.asList(message.cc));
            if (message.bcc != null)
                addresses.addAll(Arrays.asList(message.bcc));
            if (message.from != null)
                addresses.addAll(Arrays.asList(message.from));
        }

        InternetAddress deliveredto = null;
        if (message.deliveredto != null)
            try {
                deliveredto = new InternetAddress(message.deliveredto);
            } catch (AddressException ex) {
                Log.w(ex);
            }

        // Search for matching identity
        List<EntityIdentity> identities = db.identity().getSynchronizingIdentities(folder.account);
        if (identities != null) {
            for (Address address : addresses)
                for (EntityIdentity identity : identities)
                    if (identity.sameAddress(address))
                        return identity;

            for (Address address : addresses)
                for (EntityIdentity identity : identities)
                    if (identity.similarAddress(address))
                        return identity;

            if (deliveredto != null)
                for (EntityIdentity identity : identities)
                    if (identity.sameAddress(deliveredto) || identity.similarAddress(deliveredto))
                        return identity;
        }

        return null;
    }

    private static void runRules(
            Context context, Message imessage,
            EntityAccount account, EntityFolder folder, EntityMessage message,
            List<EntityRule> rules) {

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
            db.message().setMessageError(message.id, Log.formatThrowable(ex));
        }

        if (BuildConfig.DEBUG &&
                message.sender != null && EntityFolder.INBOX.equals(folder.type)) {
            EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
            if (junk != null) {
                int senders = db.message().countSender(junk.id, message.sender);
                if (senders > 0) {
                    EntityLog.log(context, "JUNK sender=" + message.sender + " count=" + senders);
                    EntityOperation.queue(context, message, EntityOperation.KEYWORD, "$MoreJunk", true);
                }
            }
        }
    }

    private static void reportNewMessage(Context context, EntityAccount account, EntityFolder folder, EntityMessage message) {
        // Prepare scroll to top
        if (!message.ui_seen && !message.ui_hide &&
                message.received > account.created) {
            Intent report = new Intent(ActivityView.ACTION_NEW_MESSAGE);
            report.putExtra("folder", folder.id);
            report.putExtra("unified", folder.unified);
            Log.i("Report new id=" + message.id + " folder=" + folder.name + " unified=" + folder.unified);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(report);
        }
    }

    private static void updateContactInfo(Context context, final EntityFolder folder, final EntityMessage message) {
        DB db = DB.getInstance(context);

        if (EntityFolder.DRAFTS.equals(folder.type) ||
                EntityFolder.ARCHIVE.equals(folder.type) ||
                EntityFolder.TRASH.equals(folder.type) ||
                EntityFolder.JUNK.equals(folder.type))
            return;

        int type = (folder.isOutgoing() ? EntityContact.TYPE_TO : EntityContact.TYPE_FROM);

        // Check if from self
        if (type == EntityContact.TYPE_FROM) {
            if (message.from != null) {
                List<EntityIdentity> identities = db.identity().getSynchronizingIdentities(folder.account);
                if (identities != null) {
                    for (Address sender : message.from) {
                        for (EntityIdentity identity : identities)
                            if (identity.similarAddress(sender)) {
                                type = EntityContact.TYPE_TO;
                                break;
                            }
                        if (type == EntityContact.TYPE_TO)
                            break;
                    }
                }
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
        boolean suggest_received = prefs.getBoolean("suggest_received", false);

        if (type == EntityContact.TYPE_TO && !suggest_sent)
            return;
        if (type == EntityContact.TYPE_FROM && !suggest_received)
            return;

        List<Address> addresses = new ArrayList<>();
        if (type == EntityContact.TYPE_FROM) {
            if (message.reply == null || message.reply.length == 0) {
                if (message.from != null)
                    addresses.addAll(Arrays.asList(message.from));
            } else
                addresses.addAll(Arrays.asList(message.reply));
        } else if (type == EntityContact.TYPE_TO) {
            if (message.to != null)
                addresses.addAll(Arrays.asList(message.to));
            if (message.cc != null)
                addresses.addAll(Arrays.asList(message.cc));
        }

        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            String name = ((InternetAddress) address).getPersonal();
            Uri avatar = ContactInfo.getLookupUri(new Address[]{address});

            if (TextUtils.isEmpty(email))
                continue;
            if (TextUtils.isEmpty(name))
                name = null;

            try {
                db.beginTransaction();

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
                    if (contact.name == null && name != null)
                        contact.name = name;
                    contact.avatar = (avatar == null ? null : avatar.toString());
                    contact.times_contacted++;
                    contact.first_contacted = Math.min(contact.first_contacted, message.received);
                    contact.last_contacted = message.received;
                    db.contact().updateContact(contact);
                    Log.i("Updated contact=" + contact + " type=" + type);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private static boolean downloadMessage(
            Context context,
            EntityAccount account, EntityFolder folder,
            IMAPStore istore, IMAPFolder ifolder,
            MimeMessage imessage, long id, State state, SyncStats stats) throws MessagingException, IOException {
        if (state.getNetworkState().isRoaming())
            return false;

        DB db = DB.getInstance(context);
        EntityMessage message = db.message().getMessage(id);
        if (message == null || message.ui_hide)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long maxSize = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
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
            //if (account.isGmail()) {
            //    fp.add(GmailFolder.FetchProfileItem.THRID);
            //    fp.add(GmailFolder.FetchProfileItem.LABELS);
            //}
            //ifolder.fetch(new Message[]{imessage}, fp);

            MessageHelper helper = new MessageHelper(imessage, context);
            MessageHelper.MessageParts parts = helper.getMessageParts();

            if (!message.content) {
                if (state.getNetworkState().isUnmetered() ||
                        (message.size != null && message.size < maxSize)) {
                    String body = parts.getHtml(context);
                    File file = message.getFile(context);
                    Helper.writeText(file, body);
                    String text = HtmlHelper.getFullText(body);
                    message.preview = HtmlHelper.getPreview(text);
                    message.language = HtmlHelper.getLanguage(context, message.subject, text);
                    db.message().setMessageContent(message.id,
                            true,
                            message.language,
                            parts.isPlainOnly(),
                            message.preview,
                            parts.getWarnings(message.warning));
                    MessageClassifier.classify(message, folder, null, context);

                    if (stats != null && body != null)
                        stats.content += body.length();
                    Log.i(folder.name + " downloaded message id=" + message.id +
                            " size=" + message.size + "/" + (body == null ? null : body.length()));

                    if (TextUtils.isEmpty(body) && parts.hasBody())
                        reportEmptyMessage(context, state, account, istore);
                }
            }

            for (EntityAttachment attachment : attachments)
                if (!attachment.available && TextUtils.isEmpty(attachment.error))
                    if (state.getNetworkState().isUnmetered() ||
                            (attachment.size != null && attachment.size < maxSize))
                        try {
                            parts.downloadAttachment(context, attachment);
                            if (stats != null && attachment.size != null)
                                stats.attachments += attachment.size;
                        } catch (Throwable ex) {
                            Log.e(folder.name, ex);
                            db.attachment().setError(attachment.id, Log.formatThrowable(ex, false));
                        }
        }

        return fetch;
    }

    private static void reportEmptyMessage(Context context, State state, EntityAccount account, IMAPStore istore) {
        try {
            if (istore.hasCapability("ID")) {
                Map<String, String> id = new LinkedHashMap<>();
                id.put("name", context.getString(R.string.app_name));
                id.put("version", BuildConfig.VERSION_NAME);
                Map<String, String> sid = istore.id(id);
                if (sid != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : sid.entrySet())
                        sb.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
                    if (!account.partial_fetch)
                        Log.w("Empty message" + sb.toString());
                }
            } else {
                if (!account.partial_fetch)
                    Log.w("Empty message " + account.host);
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        // Auto disable partial fetch
        if (account.partial_fetch) {
            account.partial_fetch = false;
            DB db = DB.getInstance(context);
            db.account().setAccountPartialFetch(account.id, account.partial_fetch);
            state.error(new StoreClosedException(istore));
        }
    }

    static void notifyMessages(Context context, List<TupleMessageEx> messages, Map<Long, List<Long>> groupNotifying, boolean foreground) {
        if (messages == null)
            messages = new ArrayList<>();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null)
            return;

        DB db = DB.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify_background_only = prefs.getBoolean("notify_background_only", false);
        boolean notify_summary = prefs.getBoolean("notify_summary", false);
        boolean notify_preview = prefs.getBoolean("notify_preview", true);
        boolean notify_preview_only = prefs.getBoolean("notify_preview_only", false);
        boolean wearable_preview = prefs.getBoolean("wearable_preview", false);
        boolean biometrics = prefs.getBoolean("biometrics", false);
        String pin = prefs.getString("pin", null);
        boolean biometric_notify = prefs.getBoolean("biometrics_notify", false);
        boolean pro = ActivityBilling.isPro(context);

        boolean redacted = ((biometrics || !TextUtils.isEmpty(pin)) && !biometric_notify);
        if (redacted)
            notify_summary = true;

        Log.i("Notify messages=" + messages.size() +
                " biometrics=" + biometrics + "/" + biometric_notify +
                " summary=" + notify_summary);

        Map<Long, List<TupleMessageEx>> groupMessages = new HashMap<>();
        for (long group : groupNotifying.keySet())
            groupMessages.put(group, new ArrayList<>());

        // Current
        for (TupleMessageEx message : messages) {
            if (message.notifying == EntityMessage.NOTIFYING_IGNORE) {
                Log.e("Notify ignore");
                continue;
            }

            // Check if notification channel enabled
            if (message.notifying == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pro) {
                String channelId = message.getNotificationChannelId();
                if (channelId != null) {
                    NotificationChannel channel = nm.getNotificationChannel(channelId);
                    if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                        Log.i("Notify disabled=" + message.id + " channel=" + channelId);
                        continue;
                    }
                }
            }

            if (notify_preview && notify_preview_only && !message.content)
                continue;

            if (foreground && notify_background_only && message.notifying == 0) {
                Log.i("Notify foreground=" + message.id);
                if (!message.ui_ignored)
                    db.message().setMessageUiIgnored(message.id, true);
                continue;
            }

            long group = (pro && message.accountNotify ? message.account : 0);
            if (!message.folderUnified)
                group = -message.folder;
            if (!groupNotifying.containsKey(group))
                groupNotifying.put(group, new ArrayList<Long>());
            if (!groupMessages.containsKey(group))
                groupMessages.put(group, new ArrayList<TupleMessageEx>());

            if (message.notifying != 0) {
                long id = message.id * message.notifying;
                if (!groupNotifying.get(group).contains(id) &&
                        !groupNotifying.get(group).contains(-id)) {
                    Log.i("Notify database=" + id);
                    groupNotifying.get(group).add(id);
                }
            }

            if (!(message.ui_seen || message.ui_ignored || message.ui_hide)) {
                // This assumes the messages are properly ordered
                if (groupMessages.get(group).size() < MAX_NOTIFICATION_COUNT)
                    groupMessages.get(group).add(message);
                else {
                    if (!message.ui_ignored)
                        db.message().setMessageUiIgnored(message.id, true);
                }
            }
        }

        // Difference
        for (Map.Entry<Long, List<TupleMessageEx>> entry : groupMessages.entrySet()) {
            long group = entry.getKey();
            List<Long> add = new ArrayList<>();
            List<Long> update = new ArrayList<>();
            List<Long> remove = new ArrayList<>(groupNotifying.get(group));
            for (TupleMessageEx message : entry.getValue()) {
                long id = (message.content ? message.id : -message.id);
                if (remove.contains(id)) {
                    remove.remove(id);
                    Log.i("Notify existing=" + id);
                } else {
                    boolean existing = remove.contains(-id);
                    if (existing) {
                        if (message.content && notify_preview) {
                            Log.i("Notify preview=" + id);
                            add.add(id);
                            update.add(id);
                        }
                        remove.remove(-id);
                    } else
                        add.add(id);
                    Log.i("Notify adding=" + id + " existing=" + existing);
                }
            }

            int new_messages = add.size() - update.size();

            if (notify_summary
                    ? remove.size() + new_messages == 0
                    : remove.size() + add.size() == 0) {
                Log.i("Notify unchanged");
                continue;
            }

            // Build notifications
            List<NotificationCompat.Builder> notifications = getNotificationUnseen(context,
                    group, entry.getValue(),
                    notify_summary, new_messages,
                    redacted);

            Log.i("Notify group=" + group + " count=" + notifications.size() +
                    " added=" + add.size() + " removed=" + remove.size());

            if (notifications.size() == 0) {
                String tag = "unseen." + group + "." + 0;
                Log.i("Notify cancel tag=" + tag);
                nm.cancel(tag, 1);
            }

            for (Long id : remove) {
                String tag = "unseen." + group + "." + Math.abs(id);
                Log.i("Notify cancel tag=" + tag + " id=" + id);
                nm.cancel(tag, 1);

                groupNotifying.get(group).remove(id);
                db.message().setMessageNotifying(Math.abs(id), 0);
            }

            for (Long id : add) {
                groupNotifying.get(group).add(id);
                groupNotifying.get(group).remove(-id);
                db.message().setMessageNotifying(Math.abs(id), (int) Math.signum(id));
            }

            for (NotificationCompat.Builder builder : notifications) {
                long id = builder.getExtras().getLong("id", 0);
                if ((id == 0 && add.size() + remove.size() > 0) || add.contains(id)) {
                    // https://developer.android.com/training/wearables/notifications/creating
                    if (id == 0) {
                        if (!notify_summary)
                            builder.setLocalOnly(true);
                    } else {
                        if (wearable_preview ? id < 0 : update.contains(id))
                            builder.setLocalOnly(true);
                    }

                    String tag = "unseen." + group + "." + Math.abs(id);
                    Notification notification = builder.build();
                    EntityLog.log(context, "Notifying tag=" + tag +
                            " id=" + id + " group=" + notification.getGroup() +
                            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                                    ? " sdk=" + Build.VERSION.SDK_INT
                                    : " channel=" + notification.getChannelId()) +
                            " sort=" + notification.getSortKey());
                    try {
                        nm.notify(tag, 1, notification);
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }
            }
        }
    }

    private static List<NotificationCompat.Builder> getNotificationUnseen(
            Context context,
            long group, List<TupleMessageEx> messages,
            boolean notify_summary, int new_messages, boolean redacted) {
        List<NotificationCompat.Builder> notifications = new ArrayList<>();

        // Android 7+ N https://developer.android.com/training/notify-user/group
        // Android 8+ O https://developer.android.com/training/notify-user/channels
        // Android 7+ N https://android-developers.googleblog.com/2016/06/notifications-in-android-n.html

        // Group
        // < 0: folder
        // = 0: unified
        // > 0: account

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (messages == null || messages.size() == 0 || nm == null)
            return notifications;

        boolean pro = ActivityBilling.isPro(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notify_newest_first = prefs.getBoolean("notify_newest_first", false);
        boolean name_email = prefs.getBoolean("name_email", false);
        boolean prefer_contact = prefs.getBoolean("prefer_contact", false);
        boolean flags = prefs.getBoolean("flags", true);
        boolean notify_messaging = prefs.getBoolean("notify_messaging", false);
        boolean notify_preview = prefs.getBoolean("notify_preview", true);
        boolean notify_preview_all = prefs.getBoolean("notify_preview_all", false);
        boolean wearable_preview = prefs.getBoolean("wearable_preview", false);
        boolean notify_trash = (prefs.getBoolean("notify_trash", true) || !pro);
        boolean notify_junk = (prefs.getBoolean("notify_junk", false) && pro);
        boolean notify_archive = (prefs.getBoolean("notify_archive", true) || !pro);
        boolean notify_move = (prefs.getBoolean("notify_move", false) && pro);
        boolean notify_reply = (prefs.getBoolean("notify_reply", false) && pro);
        boolean notify_reply_direct = (prefs.getBoolean("notify_reply_direct", false) && pro);
        boolean notify_flag = (prefs.getBoolean("notify_flag", false) && flags && pro);
        boolean notify_seen = (prefs.getBoolean("notify_seen", true) || !pro);
        boolean notify_snooze = (prefs.getBoolean("notify_snooze", false) && pro);
        boolean notify_remove = prefs.getBoolean("notify_remove", true);
        boolean light = prefs.getBoolean("light", false);
        String sound = prefs.getString("sound", null);
        boolean alert_once = prefs.getBoolean("alert_once", true);

        // Get contact info
        Map<Long, Address[]> messageFrom = new HashMap<>();
        Map<Long, ContactInfo[]> messageInfo = new HashMap<>();
        for (TupleMessageEx message : messages) {
            ContactInfo[] info = ContactInfo.get(context, message.account, message.folderType, message.from);

            Address[] modified = (message.from == null
                    ? new InternetAddress[0]
                    : Arrays.copyOf(message.from, message.from.length));
            for (int i = 0; i < modified.length; i++) {
                String displayName = info[i].getDisplayName();
                if (!TextUtils.isEmpty(displayName)) {
                    String email = ((InternetAddress) modified[i]).getAddress();
                    String personal = ((InternetAddress) modified[i]).getPersonal();
                    if (TextUtils.isEmpty(personal) || prefer_contact)
                        try {
                            modified[i] = new InternetAddress(email, displayName, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException ex) {
                            Log.w(ex);
                        }
                }
            }

            messageInfo.put(message.id, info);
            messageFrom.put(message.id, modified);
        }

        // Summary notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || notify_summary) {
            // Build pending intents
            Intent content;
            if (group < 0) {
                content = new Intent(context, ActivityView.class)
                        .setAction("folder:" + (-group) + (notify_remove ? ":" + group : ""));
                if (messages.size() > 0)
                    content.putExtra("type", messages.get(0).folderType);
            } else
                content = new Intent(context, ActivityView.class)
                        .setAction("unified" + (notify_remove ? ":" + group : ""));
            content.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent piContent = PendingIntent.getActivity(context, ActivityView.REQUEST_UNIFIED, content, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent clear = new Intent(context, ServiceUI.class).setAction("clear:" + group);
            PendingIntent piClear = PendingIntent.getService(context, ServiceUI.PI_CLEAR, clear, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build title
            String title = context.getResources().getQuantityString(
                    R.plurals.title_notification_unseen, messages.size(), messages.size());

            long cgroup = (group >= 0
                    ? group
                    : (pro && messages.size() > 0 && messages.get(0).accountNotify ? messages.get(0).account : 0));

            // Build notification
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, EntityAccount.getNotificationChannelId(cgroup))
                            .setSmallIcon(messages.size() > 1
                                    ? R.drawable.baseline_mail_more_white_24
                                    : R.drawable.baseline_mail_white_24)
                            .setContentTitle(title)
                            .setContentIntent(piContent)
                            .setNumber(messages.size())
                            .setDeleteIntent(piClear)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(notify_summary
                                    ? NotificationCompat.CATEGORY_EMAIL : NotificationCompat.CATEGORY_STATUS)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setAllowSystemGeneratedContextualActions(false);

            if (notify_summary) {
                builder.setOnlyAlertOnce(new_messages == 0);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    if (new_messages > 0)
                        setLightAndSound(builder, light, sound);
                    else
                        builder.setSound(null);
            } else {
                builder
                        .setGroup(Long.toString(group))
                        .setGroupSummary(true)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                    builder.setSound(null);
            }

            if (pro && group != 0 && messages.size() > 0) {
                TupleMessageEx amessage = messages.get(0);
                Integer color = getColor(amessage);
                if (color != null) {
                    builder.setColor(color);
                    builder.setColorized(true);
                }
                if (amessage.folderUnified)
                    builder.setSubText(amessage.accountName);
                else
                    builder.setSubText(amessage.accountName + " · " + amessage.getFolderName(context));
            }

            Notification pub = builder.build();
            builder
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setPublicVersion(pub);

            if (notify_preview)
                if (redacted)
                    builder.setContentText(context.getString(R.string.title_notification_redacted));
                else {
                    DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                    StringBuilder sb = new StringBuilder();
                    for (EntityMessage message : messages) {
                        Address[] afrom = messageFrom.get(message.id);
                        String from = MessageHelper.formatAddresses(afrom, name_email, false);
                        sb.append("<strong>").append(Html.escapeHtml(from)).append("</strong>");
                        if (!TextUtils.isEmpty(message.subject))
                            sb.append(": ").append(Html.escapeHtml(message.subject));
                        sb.append(" ").append(Html.escapeHtml(DTF.format(message.received)));
                        sb.append("<br>");
                    }

                    // Wearables
                    builder.setContentText(title);

                    // Device
                    builder.setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(HtmlHelper.fromHtml(sb.toString(), context))
                            .setSummaryText(title));
                }

            notifications.add(builder);
        }

        if (notify_summary)
            return notifications;

        // Message notifications
        for (TupleMessageEx message : messages) {
            ContactInfo[] info = messageInfo.get(message.id);

            // Build arguments
            long id = (message.content ? message.id : -message.id);
            Bundle args = new Bundle();
            args.putLong("id", id);

            // Build pending intents
            PendingIntent piContent;
            if (notify_remove) {
                Intent thread = new Intent(context, ServiceUI.class);
                thread.setAction("ignore:" + message.id);
                thread.putExtra("view", true);
                piContent = PendingIntent.getService(context, ServiceUI.PI_THREAD, thread, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                Intent thread = new Intent(context, ActivityView.class);
                thread.setAction("thread:" + message.id);
                thread.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                thread.putExtra("account", message.account);
                thread.putExtra("folder", message.folder);
                thread.putExtra("thread", message.thread);
                thread.putExtra("filter_archive", !EntityFolder.ARCHIVE.equals(message.folderType));
                piContent = PendingIntent.getActivity(context, ActivityView.REQUEST_THREAD, thread, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            Intent ignore = new Intent(context, ServiceUI.class).setAction("ignore:" + message.id);
            PendingIntent piIgnore = PendingIntent.getService(context, ServiceUI.PI_IGNORED, ignore, PendingIntent.FLAG_UPDATE_CURRENT);

            // Get channel name
            String channelName = EntityAccount.getNotificationChannelId(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pro) {
                NotificationChannel channel = null;

                String channelId = message.getNotificationChannelId();
                if (channelId != null)
                    channel = nm.getNotificationChannel(channelId);

                if (channel == null)
                    channel = nm.getNotificationChannel(EntityFolder.getNotificationChannelId(message.folder));

                if (channel == null) {
                    if (message.accountNotify)
                        channelName = EntityAccount.getNotificationChannelId(message.account);
                } else
                    channelName = channel.getId();
            }

            String sortKey = String.format(Locale.ROOT, "%13d",
                    notify_newest_first ? (10000000000000L - message.received) : message.received);

            NotificationCompat.Builder mbuilder =
                    new NotificationCompat.Builder(context, channelName)
                            .addExtras(args)
                            .setSmallIcon(R.drawable.baseline_mail_white_24)
                            .setContentIntent(piContent)
                            .setWhen(message.received)
                            .setShowWhen(true)
                            .setSortKey(sortKey)
                            .setDeleteIntent(piIgnore)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setCategory(NotificationCompat.CATEGORY_EMAIL)
                            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setOnlyAlertOnce(alert_once)
                            .setAllowSystemGeneratedContextualActions(false);

            if (notify_messaging) {
                // https://developer.android.com/training/cars/messaging
                String meName = MessageHelper.formatAddresses(message.to, name_email, false);
                String youName = MessageHelper.formatAddresses(message.from, name_email, false);

                // Names cannot be empty
                if (TextUtils.isEmpty(meName))
                    meName = "-";
                if (TextUtils.isEmpty(youName))
                    youName = "-";

                Person.Builder me = new Person.Builder().setName(meName);
                Person.Builder you = new Person.Builder().setName(youName);

                if (info[0].hasPhoto())
                    you.setIcon(IconCompat.createWithBitmap(info[0].getPhotoBitmap()));

                if (info[0].hasLookupUri())
                    you.setUri(info[0].getLookupUri().toString());

                NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(me.build());

                if (!TextUtils.isEmpty(message.subject))
                    messagingStyle.setConversationTitle(message.subject);

                messagingStyle.addMessage(
                        notify_preview && message.preview != null ? message.preview : "",
                        message.received,
                        you.build());

                mbuilder.setStyle(messagingStyle);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                mbuilder
                        .setGroup(Long.toString(group))
                        .setGroupSummary(false)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                setLightAndSound(mbuilder, light, sound);

            Address[] afrom = messageFrom.get(message.id);
            String from = MessageHelper.formatAddresses(afrom, name_email, false);
            mbuilder.setContentTitle(from);
            if (message.folderUnified && !EntityFolder.INBOX.equals(message.folderType))
                mbuilder.setSubText(message.accountName + " · " + message.getFolderName(context));
            else
                mbuilder.setSubText(message.accountName);

            DB db = DB.getInstance(context);

            List<NotificationCompat.Action> wactions = new ArrayList<>();

            if (notify_trash &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP &&
                    db.folder().getFolderByType(message.account, EntityFolder.TRASH) != null) {
                Intent trash = new Intent(context, ServiceUI.class)
                        .setAction("trash:" + message.id)
                        .putExtra("group", group);
                PendingIntent piTrash = PendingIntent.getService(context, ServiceUI.PI_TRASH, trash, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionTrash = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_delete_24,
                        context.getString(R.string.title_advanced_notify_action_trash),
                        piTrash)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionTrash.build());

                wactions.add(actionTrash.build());
            }

            if (notify_junk &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP &&
                    db.folder().getFolderByType(message.account, EntityFolder.JUNK) != null) {
                Intent junk = new Intent(context, ServiceUI.class)
                        .setAction("junk:" + message.id)
                        .putExtra("group", group);
                PendingIntent piJunk = PendingIntent.getService(context, ServiceUI.PI_JUNK, junk, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionJunk = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_report_problem_24,
                        context.getString(R.string.title_advanced_notify_action_junk),
                        piJunk)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionJunk.build());

                wactions.add(actionJunk.build());
            }

            if (notify_archive &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP &&
                    db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE) != null) {
                Intent archive = new Intent(context, ServiceUI.class)
                        .setAction("archive:" + message.id)
                        .putExtra("group", group);
                PendingIntent piArchive = PendingIntent.getService(context, ServiceUI.PI_ARCHIVE, archive, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionArchive = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_archive_24,
                        context.getString(R.string.title_advanced_notify_action_archive),
                        piArchive)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_ARCHIVE)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionArchive.build());

                wactions.add(actionArchive.build());
            }

            if (notify_move &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP) {
                EntityAccount account = db.account().getAccount(message.account);
                if (account != null && account.move_to != null) {
                    EntityFolder folder = db.folder().getFolder(account.move_to);
                    if (folder != null) {
                        Intent move = new Intent(context, ServiceUI.class)
                                .setAction("move:" + message.id)
                                .putExtra("group", group);
                        PendingIntent piMove = PendingIntent.getService(context, ServiceUI.PI_MOVE, move, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Action.Builder actionMove = new NotificationCompat.Action.Builder(
                                R.drawable.twotone_folder_24,
                                folder.getDisplayName(context),
                                piMove)
                                .setShowsUserInterface(false)
                                .setAllowGeneratedReplies(false);
                        mbuilder.addAction(actionMove.build());

                        wactions.add(actionMove.build());
                    }
                }
            }

            if (notify_reply && message.content &&
                    db.identity().getComposableIdentities(message.account).size() > 0) {
                Intent reply = new Intent(context, ActivityCompose.class)
                        .putExtra("action", "reply")
                        .putExtra("reference", message.id)
                        .putExtra("group", group);
                reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent piReply = PendingIntent.getActivity(context, ActivityCompose.PI_REPLY, reply, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionReply = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_reply_24,
                        context.getString(R.string.title_advanced_notify_action_reply),
                        piReply)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .setShowsUserInterface(true)
                        .setAllowGeneratedReplies(false);
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
                        R.drawable.twotone_reply_24,
                        context.getString(R.string.title_advanced_notify_action_reply_direct),
                        piReply)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                RemoteInput.Builder input = new RemoteInput.Builder("text")
                        .setLabel(context.getString(R.string.title_advanced_notify_action_reply));
                actionReply.addRemoteInput(input.build())
                        .setAllowGeneratedReplies(false);
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
                        piFlag)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_THUMBS_UP)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionFlag.build());

                wactions.add(actionFlag.build());
            }

            if (notify_seen) {
                Intent seen = new Intent(context, ServiceUI.class)
                        .setAction("seen:" + message.id)
                        .putExtra("group", group);
                PendingIntent piSeen = PendingIntent.getService(context, ServiceUI.PI_SEEN, seen, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionSeen = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_visibility_24,
                        context.getString(R.string.title_advanced_notify_action_seen),
                        piSeen)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionSeen.build());

                wactions.add(actionSeen.build());
            }

            if (notify_snooze) {
                Intent snooze = new Intent(context, ServiceUI.class)
                        .setAction("snooze:" + message.id)
                        .putExtra("group", group);
                PendingIntent piSnooze = PendingIntent.getService(context, ServiceUI.PI_SNOOZE, snooze, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action.Builder actionSnooze = new NotificationCompat.Action.Builder(
                        R.drawable.twotone_timelapse_24,
                        context.getString(R.string.title_advanced_notify_action_snooze),
                        piSnooze)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MUTE)
                        .setShowsUserInterface(false)
                        .setAllowGeneratedReplies(false);
                mbuilder.addAction(actionSnooze.build());

                wactions.add(actionSnooze.build());
            }

            if (message.content && notify_preview) {
                // Android will truncate the text
                String preview = message.preview;
                if (notify_preview_all)
                    try {
                        File file = message.getFile(context);
                        preview = HtmlHelper.getFullText(file);
                        if (preview != null && preview.length() > MAX_PREVIEW)
                            preview = preview.substring(0, MAX_PREVIEW);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                // Wearables
                StringBuilder sb = new StringBuilder();
                if (!TextUtils.isEmpty(message.subject))
                    sb.append(message.subject);
                if (wearable_preview) {
                    if (sb.length() != 0)
                        sb.append(" - ");
                    if (!TextUtils.isEmpty(preview))
                        sb.append(preview);
                }
                if (sb.length() > 0)
                    mbuilder.setContentText(sb.toString());

                // Device
                if (!notify_messaging) {
                    StringBuilder sbm = new StringBuilder();
                    if (!TextUtils.isEmpty(message.subject))
                        sbm.append("<em>").append(Html.escapeHtml(message.subject)).append("</em>").append("<br>");

                    if (!TextUtils.isEmpty(preview))
                        sbm.append(Html.escapeHtml(preview));

                    if (sbm.length() > 0) {
                        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle()
                                .bigText(HtmlHelper.fromHtml(sbm.toString(), context));
                        if (!TextUtils.isEmpty(message.subject))
                            bigText.setSummaryText(message.subject);

                        mbuilder.setStyle(bigText);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(message.subject))
                    mbuilder.setContentText(message.subject);
            }

            if (info[0].hasPhoto())
                mbuilder.setLargeIcon(info[0].getPhotoBitmap());

            if (info[0].hasLookupUri()) {
                Person.Builder you = new Person.Builder()
                        .setUri(info[0].getLookupUri().toString());
                mbuilder.addPerson(you.build());
            }

            Integer color = getColor(message);
            if (pro && color != null) {
                mbuilder.setColor(color);
                mbuilder.setColorized(true);
            }

            // https://developer.android.com/training/wearables/notifications
            // https://developer.android.com/reference/android/app/Notification.WearableExtender
            mbuilder.extend(new NotificationCompat.WearableExtender()
                            .addActions(wactions)
                            .setDismissalId(BuildConfig.APPLICATION_ID + ":" + id)
                    /* .setBridgeTag(id < 0 ? "header" : "body") */);

            notifications.add(mbuilder);
        }

        return notifications;
    }

    private static Integer getColor(TupleMessageEx message) {
        if (!message.folderUnified && message.folderColor != null)
            return message.folderColor;
        return message.accountColor;
    }

    private static void setLightAndSound(NotificationCompat.Builder builder, boolean light, String sound) {
        int def = 0;

        if (light) {
            def |= DEFAULT_LIGHTS;
            Log.i("Notify light enabled");
        }

        if (!"".equals(sound)) {
            // Not silent sound
            Uri uri = (sound == null ? null : Uri.parse(sound));
            if (uri != null && !"content".equals(uri.getScheme()))
                uri = null;
            Log.i("Notify sound=" + uri);

            if (uri == null)
                def |= DEFAULT_SOUND;
            else
                builder.setSound(uri);
        }

        builder.setDefaults(def);
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
        intent.setAction("error");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                context, ActivityView.REQUEST_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channel)
                        .setSmallIcon(R.drawable.baseline_warning_white_24)
                        .setContentTitle(context.getString(R.string.title_notification_failed, title))
                        .setContentText(Log.formatThrowable(ex, false))
                        .setContentIntent(pi)
                        .setAutoCancel(false)
                        .setShowWhen(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOnlyAlertOnce(true)
                        .setCategory(NotificationCompat.CATEGORY_ERROR)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(Log.formatThrowable(ex, "\n", false)));

        return builder;
    }

    static class State {
        private int backoff;
        private boolean backingoff = false;
        private ConnectionHelper.NetworkState networkState;
        private Thread thread = new Thread();
        private Semaphore semaphore = new Semaphore(0);
        private boolean running = true;
        private boolean recoverable = true;
        private Throwable unrecoverable = null;
        private Long lastActivity = null;

        private boolean process = false;
        private Map<FolderPriority, Long> sequence = new HashMap<>();
        private Map<FolderPriority, Long> batch = new HashMap<>();

        State(ConnectionHelper.NetworkState networkState) {
            this.networkState = networkState;
        }

        void setNetworkState(ConnectionHelper.NetworkState networkState) {
            this.networkState = networkState;
        }

        ConnectionHelper.NetworkState getNetworkState() {
            return networkState;
        }

        void setBackoff(int value) {
            this.backoff = value;
        }

        int getBackoff() {
            return backoff;
        }

        void runnable(Runnable runnable, String name) {
            thread = new Thread(runnable, name);
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
        }

        boolean release() {
            if (!thread.isAlive())
                return false;

            semaphore.release();
            yield();
            return true;
        }

        boolean acquire(long milliseconds, boolean backingoff) throws InterruptedException {
            try {
                this.backingoff = backingoff;
                return semaphore.tryAcquire(milliseconds, TimeUnit.MILLISECONDS);
            } finally {
                this.backingoff = false;
            }
        }

        void error(Throwable ex) {
            if (ex instanceof MessagingException &&
                    ("connection failure".equals(ex.getMessage()) ||
                            "Not connected".equals(ex.getMessage()) || // POP3
                            ex.getCause() instanceof SocketException ||
                            ex.getCause() instanceof ConnectionException))
                recoverable = false;

            if (ex instanceof ConnectionException)
                // failed to create new store connection
                // BYE, Socket is closed
                recoverable = false;

            if (ex instanceof StoreClosedException ||
                    ex instanceof FolderClosedException ||
                    ex instanceof FolderNotFoundException)
                // Lost folder connection to server
                recoverable = false;

            if (ex instanceof IllegalStateException && (
                    "Not connected".equals(ex.getMessage()) ||
                            "This operation is not allowed on a closed folder".equals(ex.getMessage())))
                recoverable = false;

            if (ex instanceof OperationCanceledException)
                recoverable = false;

            if (!recoverable)
                unrecoverable = ex;

            if (!backingoff) {
                thread.interrupt();
                yield();
            }
        }

        void reset() {
            recoverable = true;
            lastActivity = null;
            resetBatches();
            process = true;
        }

        void resetBatches() {
            process = false;
            synchronized (this) {
                for (Map.Entry<FolderPriority, Long> entry : sequence.entrySet()) {
                    FolderPriority key = entry.getKey();
                    batch.put(key, entry.getValue());
                    if (BuildConfig.DEBUG)
                        Log.i("=== Reset " + key.folder + ":" + key.priority + " batch=" + batch.get(key));
                }
            }
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

        Throwable getUnrecoverable() {
            return unrecoverable;
        }

        void join(Thread thread) {
            boolean joined = false;
            boolean interrupted = false;
            String name = thread.getName();
            while (!joined)
                try {
                    Log.i("Joining " + name +
                            " alive=" + thread.isAlive() +
                            " state=" + thread.getState());

                    thread.join(JOIN_WAIT);

                    // https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.State.html
                    Thread.State state = thread.getState();
                    if (thread.isAlive()) {
                        if (interrupted)
                            Log.e("Join " + name + " failed state=" + state + " interrupted=" + interrupted);
                        if (interrupted)
                            joined = true; // give up
                        else {
                            thread.interrupt();
                            interrupted = true;
                        }
                    } else {
                        Log.i("Joined " + name + " " + " state=" + state);
                        joined = true;
                    }
                } catch (InterruptedException ex) {
                    Log.w(thread.getName() + " join " + ex.toString());
                }
        }

        synchronized void activity() {
            lastActivity = SystemClock.elapsedRealtime();
        }

        long getIdleTime() {
            Long last = lastActivity;
            return (last == null ? 0 : SystemClock.elapsedRealtime() - last);
        }

        long getSequence(long folder, int priority) {
            synchronized (this) {
                FolderPriority key = new FolderPriority(folder, priority);
                if (!sequence.containsKey(key)) {
                    sequence.put(key, 0L);
                    batch.put(key, 0L);
                }
                long result = sequence.get(key);
                sequence.put(key, result + 1);
                if (BuildConfig.DEBUG)
                    Log.i("=== Get " + folder + ":" + priority + " sequence=" + result);
                return result;
            }
        }

        boolean batchCanRun(long folder, int priority, long current) {
            if (!process) {
                Log.i("=== Can " + folder + ":" + priority + " process=" + process);
                return false;
            }

            synchronized (this) {
                FolderPriority key = new FolderPriority(folder, priority);
                boolean can = batch.get(key).equals(current);
                if (BuildConfig.DEBUG)
                    Log.i("=== Can " + folder + ":" + priority + " can=" + can);
                return can;
            }
        }

        void batchCompleted(long folder, int priority, long current) {
            synchronized (this) {
                FolderPriority key = new FolderPriority(folder, priority);
                if (batch.get(key).equals(current))
                    batch.put(key, batch.get(key) + 1);
                if (BuildConfig.DEBUG)
                    Log.i("=== Completed " + folder + ":" + priority + " next=" + batch.get(key));
            }
        }

        @NonNull
        @Override
        public String toString() {
            return "[running=" + running +
                    ",recoverable=" + recoverable +
                    ",idle=" + getIdleTime() + "]";
        }

        private static class FolderPriority {
            private long folder;
            private int priority;

            FolderPriority(long folder, int priority) {
                this.folder = folder;
                this.priority = priority;
            }

            @Override
            public int hashCode() {
                return (int) (this.folder * 37 + priority);
            }

            @Override
            public boolean equals(@Nullable Object obj) {
                if (obj instanceof FolderPriority) {
                    FolderPriority other = (FolderPriority) obj;
                    return (this.folder == other.folder && this.priority == other.priority);
                } else
                    return false;
            }
        }
    }

    private static class SyncStats {
        long search_ms;
        int flags;
        long flags_ms;
        int uids;
        long uids_ms;
        int headers;
        long headers_ms;
        long content;
        long attachments;
        long total;

        boolean isEmpty() {
            return (search_ms == 0 &&
                    flags == 0 &&
                    flags_ms == 0 &&
                    uids == 0 &&
                    uids_ms == 0 &&
                    headers == 0 &&
                    headers_ms == 0 &&
                    content == 0 &&
                    attachments == 0 &&
                    total == 0);
        }

        @Override
        public String toString() {
            return "search=" + search_ms + " ms" +
                    " flags=" + flags + "/" + flags_ms + " ms" +
                    " uids=" + uids + "/" + uids_ms + " ms" +
                    " headers=" + headers + "/" + headers_ms + " ms" +
                    " content=" + Helper.humanReadableByteCount(content) +
                    " attachments=" + Helper.humanReadableByteCount(attachments) +
                    " total=" + total + " ms";
        }
    }
}
