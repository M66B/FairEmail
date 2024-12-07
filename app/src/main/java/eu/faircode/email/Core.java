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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static javax.mail.Folder.READ_WRITE;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.OperationCanceledException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
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
import com.sun.mail.imap.protocol.Status;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.imap.protocol.UIDSet;
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
import java.net.SocketException;
import java.net.UnknownHostException;
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
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
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

class Core {
    static final int DEFAULT_RANGE_SIZE = 1000;
    static final int DEFAULT_CHUNK_SIZE = 50;

    private static final int SYNC_BATCH_SIZE = 20;
    private static final int DOWNLOAD_BATCH_SIZE = 20;
    private static final long YIELD_DURATION = 200L; // milliseconds
    private static final long JOIN_WAIT_ALIVE = 5 * 60 * 1000L; // milliseconds
    private static final long JOIN_WAIT_INTERRUPT = 1 * 60 * 1000L; // milliseconds
    private static final long FUTURE_RECEIVED = 30 * 24 * 3600 * 1000L; // milliseconds
    private static final int LOCAL_RETRY_MAX = 2;
    private static final long LOCAL_RETRY_DELAY = 5 * 1000L; // milliseconds
    private static final int TOTAL_RETRY_MAX = LOCAL_RETRY_MAX * 5;
    private static final long EXISTS_RETRY_DELAY = 20 * 1000L; // milliseconds
    private static final int FIND_RETRY_COUNT = 3; // times
    private static final long FIND_RETRY_DELAY = 5 * 1000L; // milliseconds
    private static final int POP3_KEEP_EXTRA = 100; // messages

    private static final Map<Long, List<EntityIdentity>> accountIdentities = new HashMap<>();

    static void clearIdentities() {
        synchronized (accountIdentities) {
            accountIdentities.clear();
        }
    }

    static List<EntityIdentity> getIdentities(long account, Context context) {
        synchronized (accountIdentities) {
            if (!accountIdentities.containsKey(account))
                accountIdentities.put(account,
                        DB.getInstance(context).identity().getSynchronizingIdentities(account));
            return accountIdentities.get(account);
        }
    }

    static void processOperations(
            Context context,
            EntityAccount account, EntityFolder folder, List<TupleOperationEx> ops,
            EmailService iservice, Folder ifolder,
            State state, long serial)
            throws JSONException, FolderClosedException {
        try {
            Log.i(folder.name + " start process");

            Store istore = iservice.getStore();
            DB db = DB.getInstance(context);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int chunk_size = prefs.getInt("chunk_size", DEFAULT_CHUNK_SIZE);

            NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);

            int retry = 0;
            boolean group = true;
            Log.i(folder.name + " executing serial=" + serial + " operations=" + ops.size());
            while (retry < LOCAL_RETRY_MAX && ops.size() > 0 &&
                    state.isRunning() &&
                    state.getSerial() == serial) {
                TupleOperationEx op = ops.get(0);

                try {
                    Log.i(folder.name +
                            " start op=" + op.id + "/" + op.name +
                            " folder=" + op.folder +
                            " msg=" + op.message +
                            " args=" + op.args +
                            " group=" + group +
                            " retry=" + retry);

                    if (EntityOperation.HEADERS.equals(op.name) ||
                            EntityOperation.RAW.equals(op.name))
                        nm.cancel(op.name + ":" + op.message, NotificationHelper.NOTIFICATION_TAGGED);

                    if (!Objects.equals(folder.id, op.folder))
                        throw new IllegalArgumentException("Invalid folder=" + folder.id + "/" + op.folder);

                    if (account.protocol == EntityAccount.TYPE_IMAP && !folder.local && ifolder != null) {
                        try {
                            ((IMAPFolder) ifolder).doCommand(new IMAPFolder.ProtocolCommand() {
                                @Override
                                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                    long ago = System.currentTimeMillis() - protocol.getTimestamp();
                                    if (ago > 20000) {
                                        Log.i("NOOP ago=" + ago + " ms");
                                        protocol.noop();
                                    }
                                    return null;
                                }
                            });
                        } catch (MessagingException ex) {
                            throw new FolderClosedException(ifolder, account.name + "/" + folder.name + " unexpectedly closed", ex);
                        }
                    }

                    if (account.protocol == EntityAccount.TYPE_POP &&
                            EntityFolder.INBOX.equals(folder.type) &&
                            ifolder != null && !ifolder.isOpen())
                        throw new FolderClosedException(ifolder, account.name + "/" + folder.name + " unexpectedly closed");

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
                                !EntityOperation.REPORT.equals(op.name) &&
                                !EntityOperation.SYNC.equals(op.name) &&
                                !EntityOperation.SUBSCRIBE.equals(op.name) &&
                                !EntityOperation.PURGE.equals(op.name) &&
                                !EntityOperation.EXPUNGE.equals(op.name))
                            throw new MessageRemovedException();

                        // Process similar operations
                        boolean skip = false;
                        for (int j = 1; j < ops.size(); j++) {
                            TupleOperationEx next = ops.get(j);

                            switch (op.name) {
                                case EntityOperation.SEEN:
                                case EntityOperation.FLAG:
                                    if (group &&
                                            message.uid != null &&
                                            op.name.equals(next.name) &&
                                            account.protocol == EntityAccount.TYPE_IMAP) {
                                        JSONArray jnext = new JSONArray(next.args);
                                        // Same flag
                                        if (jargs.getBoolean(0) == jnext.getBoolean(0)) {
                                            EntityMessage m = db.message().getMessage(next.message);
                                            if (m != null && m.uid != null)
                                                similar.put(next, m);
                                        }
                                    }
                                    break;

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
                                        // Same uid, invalidate, delete flag
                                        if (jargs.getLong(0) == jnext.getLong(0) &&
                                                jargs.optBoolean(1) == jnext.optBoolean(1) &&
                                                jargs.optBoolean(2) == jnext.optBoolean(2))
                                            skip = true;
                                    }
                                    break;

                                case EntityOperation.DOWNLOAD:
                                    if (EntityOperation.DOWNLOAD.equals(next.name)) {
                                        JSONArray jnext = new JSONArray(next.args);
                                        // Same uid
                                        if (jargs.getLong(0) == jnext.getLong(0))
                                            skip = true;
                                    }
                                    break;

                                case EntityOperation.MOVE:
                                    if (group &&
                                            message.uid != null &&
                                            op.name.equals(next.name) &&
                                            account.protocol == EntityAccount.TYPE_IMAP) {
                                        JSONArray jnext = new JSONArray(next.args);
                                        // Same target
                                        if (Objects.equals(op.account, next.account) &&
                                                jargs.getLong(0) == jnext.getLong(0) &&
                                                jargs.optBoolean(4) == jnext.optBoolean(4)) {
                                            EntityMessage m = db.message().getMessage(next.message);
                                            if (m != null && m.uid != null)
                                                similar.put(next, m);
                                        }
                                    }
                                    if (group &&
                                            op.name.equals(next.name) &&
                                            account.protocol == EntityAccount.TYPE_POP) {
                                        JSONArray jnext = new JSONArray(next.args);
                                        // Same target
                                        if (jargs.getLong(0) == jnext.getLong(0)) {
                                            EntityMessage m = db.message().getMessage(next.message);
                                            if (m != null)
                                                similar.put(next, m);
                                        }
                                    }
                                    break;

                                case EntityOperation.DELETE:
                                    if (group &&
                                            message.uid != null &&
                                            op.name.equals(next.name) &&
                                            account.protocol == EntityAccount.TYPE_IMAP) {
                                        EntityMessage m = db.message().getMessage(next.message);
                                        if (m != null &&
                                                m.uid != null && m.ui_deleted == message.ui_deleted)
                                            similar.put(next, m);
                                    }
                                    if (group &&
                                            op.name.equals(next.name) &&
                                            account.protocol == EntityAccount.TYPE_POP) {
                                        EntityMessage m = db.message().getMessage(next.message);
                                        if (m != null)
                                            similar.put(next, m);
                                    }
                                    break;
                            }

                            if (similar.size() >= chunk_size)
                                break;
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

                        if (istore instanceof POP3Store) {
                            List<EntityMessage> messages = new ArrayList<>();
                            messages.add(message);
                            messages.addAll(similar.values());

                            switch (op.name) {
                                case EntityOperation.DELETE:
                                    onDelete(context, jargs, account, folder, messages, (POP3Folder) ifolder, (POP3Store) istore, state);
                                    break;

                                case EntityOperation.RAW:
                                    onRaw(context, jargs, folder, message, (POP3Store) istore, (POP3Folder) ifolder);
                                    break;

                                case EntityOperation.BODY:
                                    onBody(context, jargs, folder, message, (POP3Folder) ifolder, (POP3Store) istore);
                                    break;

                                case EntityOperation.ATTACHMENT:
                                    onAttachment(context, jargs, folder, message, (POP3Folder) ifolder, (POP3Store) istore);
                                    break;

                                case EntityOperation.SYNC:
                                    Helper.gc("Sync start");
                                    onSynchronizeMessages(context, jargs, account, folder, (POP3Folder) ifolder, (POP3Store) istore, state);
                                    Helper.gc("Sync end");
                                    break;

                                case EntityOperation.PURGE:
                                    onPurgeFolder(context, folder);
                                    break;

                                default:
                                    Log.w(folder.name + " ignored=" + op.name);
                            }
                        } else {
                            List<EntityMessage> messages = new ArrayList<>();
                            messages.add(message);
                            if (similar.size() == 0)
                                ensureUid(context, account, folder, message, op, (IMAPFolder) ifolder);
                            else
                                messages.addAll(similar.values());

                            switch (op.name) {
                                case EntityOperation.SEEN:
                                    onSetFlag(context, jargs, folder, messages, (IMAPFolder) ifolder, Flags.Flag.SEEN);
                                    break;

                                case EntityOperation.FLAG:
                                    onSetFlag(context, jargs, folder, messages, (IMAPFolder) ifolder, Flags.Flag.FLAGGED);
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
                                    onMove(context, jargs, false, account, folder, messages, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.COPY:
                                    onMove(context, jargs, true, account, folder, Arrays.asList(message), (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.FETCH:
                                    onFetch(context, jargs, folder, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.DELETE:
                                    onDelete(context, jargs, account, folder, messages, (IMAPStore) istore, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.HEADERS:
                                    onHeaders(context, jargs, folder, message, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.RAW:
                                    onRaw(context, jargs, account, folder, message, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.BODY:
                                    onBody(context, jargs, folder, message, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.ATTACHMENT:
                                    onAttachment(context, jargs, folder, message, op, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.DETACH:
                                    onDetach(context, jargs, account, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.EXISTS:
                                    onExists(context, jargs, account, folder, message, op, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.REPORT:
                                    onReport(context, jargs, folder, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.SYNC:
                                    Helper.gc("Sync start");
                                    onSynchronizeMessages(context, jargs, account, folder, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    Helper.gc("Sync end");
                                    break;

                                case EntityOperation.SUBSCRIBE:
                                    onSubscribeFolder(context, jargs, folder, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.PURGE:
                                    onPurgeFolder(context, jargs, account, folder, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.EXPUNGE:
                                    onExpungeFolder(context, jargs, folder, (IMAPFolder) ifolder);
                                    break;

                                case EntityOperation.RULE:
                                    onRule(context, jargs, message);
                                    break;

                                case EntityOperation.DOWNLOAD:
                                    onDownload(context, jargs, account, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                case EntityOperation.SUBJECT:
                                    onSubject(context, jargs, account, folder, message, (IMAPStore) istore, (IMAPFolder) ifolder, state);
                                    break;

                                default:
                                    throw new IllegalArgumentException("Unknown operation=" + op.name);
                            }
                        }

                        crumb.put("thread", Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
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
                        iservice.dump(account.name + "/" + folder.name);
                        if (ex instanceof OperationCanceledException ||
                                (ex instanceof IllegalArgumentException &&
                                        ex.getMessage() != null &&
                                        ex.getMessage().startsWith("Message not found for")))
                            Log.i(folder.name, ex);
                        else
                            Log.e(folder.name, ex);

                        EntityLog.log(context, folder.name +
                                " op=" + op.name +
                                " try=" + op.tries +
                                " " + ex + "\n" + android.util.Log.getStackTraceString(ex));

                        try {
                            db.beginTransaction();

                            db.operation().setOperationTries(op.id, op.tries);

                            op.error = Log.formatThrowable(ex, !EntityOperation.BODY.equals(op.name));
                            db.operation().setOperationError(op.id, op.error);

                            if (message != null &&
                                    !EntityOperation.FETCH.equals(op.name) &&
                                    !EntityOperation.ATTACHMENT.equals(op.name) &&
                                    !(ex instanceof IllegalArgumentException))
                                db.message().setMessageError(message.id, op.error);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        if (similar.size() > 0 && op.tries < TOTAL_RETRY_MAX) {
                            // Retry individually
                            group = false;
                            // Finally will reset state
                            continue;
                        }

                        long attachments = (op.message == null ? 0 : db.attachment().countAttachments(op.message));

                        if (op.tries >= TOTAL_RETRY_MAX ||
                                ex instanceof JSONException ||
                                ex instanceof OutOfMemoryError ||
                                ex instanceof FileNotFoundException ||
                                ex instanceof FolderNotFoundException ||
                                ex instanceof IllegalArgumentException ||
                                ex instanceof SQLiteConstraintException ||
                                ex instanceof OperationCanceledException ||
                                (!ConnectionHelper.isIoError(ex) &&
                                        (ex.getCause() instanceof BadCommandException ||
                                                ex.getCause() instanceof CommandFailedException /* NO */) &&
                                        // https://sebastian.marsching.com/wiki/Network/Zimbra#Mailbox_Selected_READ-ONLY_Error_in_Thunderbird
                                        (ex.getMessage() == null ||
                                                !ex.getMessage().contains("mailbox selected READ-ONLY"))) ||
                                MessageHelper.isRemoved(ex) ||
                                EntityOperation.HEADERS.equals(op.name) ||
                                EntityOperation.RAW.equals(op.name) ||
                                (op.tries >= LOCAL_RETRY_MAX &&
                                        EntityOperation.BODY.equals(op.name)) ||
                                EntityOperation.ATTACHMENT.equals(op.name) ||
                                EntityOperation.DETACH.equals(op.name) ||
                                EntityOperation.SUBJECT.equals(op.name) ||
                                ((op.tries >= LOCAL_RETRY_MAX || attachments > 0) &&
                                        EntityOperation.ADD.equals(op.name)) ||
                                (op.tries >= LOCAL_RETRY_MAX &&
                                        EntityOperation.SYNC.equals(op.name) &&
                                        (account.protocol == EntityAccount.TYPE_POP ||
                                                !ConnectionHelper.isIoError(ex)))) {
                            // com.sun.mail.iap.BadCommandException: BAD [TOOBIG] Message too large
                            // com.sun.mail.iap.CommandFailedException: NO [CANNOT] Cannot APPEND to a SPAM folder
                            // com.sun.mail.iap.CommandFailedException: NO [ALERT] Cannot MOVE messages out of the Drafts folder
                            // com.sun.mail.iap.CommandFailedException: NO [OVERQUOTA] quota exceeded
                            // Drafts: javax.mail.FolderClosedException: * BYE Jakarta Mail Exception:
                            //   javax.net.ssl.SSLException: Write error: ssl=0x8286cac0: I/O error during system call, Broken pipe
                            // Drafts: * BYE Jakarta Mail Exception: java.io.IOException: Connection dropped by server?
                            // Sync: BAD Could not parse command
                            // Sync: SEARCH not allowed now
                            // Sync: BAD Command SEARCH invalid in AUTHENTICATED state (MARKER:xxx)
                            // Seen: NO mailbox selected READ-ONLY
                            // Fetch: BAD Error in IMAP command FETCH: Invalid messageset (n.nnn + n.nnn secs).
                            // Fetch: NO all of the requested messages have been expunged
                            // Fetch: BAD parse error: invalid message sequence number:
                            // Fetch: NO The specified message set is invalid.
                            // Fetch: NO [SERVERBUG] SELECT Server error - Please try again later
                            // Fetch: NO [SERVERBUG] UID FETCH Server error - Please try again later
                            // Fetch: NO Invalid message number (took nnn ms)
                            // Fetch: NO Invalid message sequence ID: nnn
                            // Fetch: BAD Internal Server Error
                            // Fetch: BAD Error in IMAP command FETCH: Invalid messageset (n.nnn + n .nnn secs).
                            // Fetch: NO FETCH sequence parse error in: nnn
                            // Fetch: NO [NONEXISTENT] No matching messages
                            // Fetch UID: NO Some messages could not be FETCHed (Failure)
                            // Fetch UID: NO [LIMIT] UID FETCH Rate limit hit.
                            // Fetch UID: NO Server Unavailable. 15
                            // Fetch UID: NO [UNAVAILABLE] Failed to open mailbox
                            // Fetch UID: NO [TEMPFAIL] SELECT completed
                            // Fetch UID: NO Internal error. Try again later... (MARKER:xxx)
                            // Fetch UID: BAD Serious error while processing UID FETCH (NioRecvFail (nn/nn))
                            // Fetch UID: NO SELECT: libmapper: Internal error: No servers available or value handling error!
                            // Fetch UID: BAD Serious error while processing UID FETCH (CassdbDatabaseError (nnn/n))
                            // Move: NO Over quota
                            // Move: NO No matching messages
                            // Move: NO [EXPUNGEISSUED] Some of the requested messages no longer exist (n.nnn + n.nnn + n.nnn secs)
                            // Move: BAD parse error: invalid message sequence number:
                            // Move: NO MOVE failed or partially completed.
                            // Move: NO mailbox selected READ-ONLY
                            // Move: NO read only
                            // Move: NO COPY failed
                            // Move: NO [SERVERBUG] Internal error occurred. Refer to server log for more information.
                            // Move: NO STORE: mtd: internal error: Cannot set message attributes.<404, ebox: no such entity: LiteMessage 29215 does not exist>
                            // Move: NO mailbox selected READ-ONLY
                            // Move: NO System Error (Failure)
                            // Move: NO APPEND processing failed.
                            // Move: NO Server Unavailable. 15
                            // Move: NO [CANNOT] Operation is not supported on mailbox
                            // Move: NO [CANNOT] Can't save messages to this virtual mailbox (0.001 + 0.000 secs).
                            // Move: NO [ALREADYEXISTS] Mailbox already exists
                            // Move: NO Permission denied
                            // Copy: NO Client tried to access nonexistent namespace. (Mailbox name should probably be prefixed with: INBOX.) (n.nnn + n.nnn secs).
                            // Copy: NO Message not found
                            // Add: BAD Data length exceeds limit
                            // Add: NO [LIMIT] APPEND Command exceeds the maximum allowed size
                            // Add: NO APPEND failed: Unknown flag: SEEN
                            // Add: BAD mtd: internal error: APPEND Message too long. 12345678
                            // Add: NO [OVERQUOTA] Not enough disk quota (n.nnn + n.nnn + n.nnn secs).
                            // Add: NO [OVERQUOTA] Quota exceeded (mailbox for user is full) (n.nnn + n.nnn secs).
                            // Add: NO APPEND failed
                            // Add: BAD [TOOBIG] Message too large.
                            // Add: NO Permission denied
                            // Add: NO Message size exceeds fixed maximum message size. Size: xxx KB, Max size: yyy KB
                            // Delete: NO [CANNOT] STORE It's not possible to perform specified operation
                            // Delete: NO [UNAVAILABLE] EXPUNGE Backend error
                            // Delete: NO mailbox selected READ-ONLY
                            // Delete: NO Mails not exist!
                            // Flags: NO mailbox selected READ-ONLY
                            // Flags: BAD Server error: 'NoneType' object has no attribute 'message_id'
                            // Keyword: NO STORE completed
                            // Keyword: NO [CANNOT] Keyword length too long (n.nnn + n.nnn secs).
                            // Search: BAD command syntax error
                            // Search (sync): BAD Could not parse command

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
                                if (MessageHelper.isRemoved(ex)) {
                                    if (message != null &&
                                            !EntityOperation.SEEN.equals(op.name) &&
                                            (!EntityOperation.FLAG.equals(op.name) ||
                                                    EntityFolder.FLAGGED.equals(folder.subtype)))
                                        db.message().deleteMessage(message.id);
                                }

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            ops.remove(op);

                            if (!MessageHelper.isRemoved(ex)) {
                                int resid = context.getResources().getIdentifier(
                                        "title_op_title_" + op.name,
                                        "string",
                                        context.getPackageName());
                                String title = (resid == 0 ? null : context.getString(resid));
                                if (title != null) {
                                    NotificationCompat.Builder builder =
                                            getNotificationError(context, "warning", account, message.id, new Throwable(title, ex));
                                    if (NotificationHelper.areNotificationsEnabled(nm))
                                        nm.notify(op.name + ":" + op.message,
                                                NotificationHelper.NOTIFICATION_TAGGED,
                                                builder.build());
                                }
                            }

                        } else {
                            retry++;
                            if (retry < LOCAL_RETRY_MAX &&
                                    state.isRunning() &&
                                    state.getSerial() == serial)
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

            if (ops.size() != 0 && state.getSerial() == serial) {
                List<String> names = new ArrayList<>();
                for (EntityOperation op : ops)
                    names.add(op.name);
                state.error(new OperationCanceledException("Processing " + TextUtils.join(",", names)));
            }
        } finally {
            Log.i(folder.name + " end process state=" + state + " pending=" + ops.size());
        }
    }

    private static void ensureUid(Context context, EntityAccount account, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws MessagingException, IOException {
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

        DB db = DB.getInstance(context);

        Long uid = findUid(context, account, ifolder, message.msgid, null);
        if (uid == null) {
            if (EntityOperation.MOVE.equals(op.name) &&
                    EntityFolder.DRAFTS.equals(folder.type))
                try {
                    long fid = new JSONArray(op.args).optLong(0, -1L);
                    EntityFolder target = db.folder().getFolder(fid);
                    if (target != null && EntityFolder.TRASH.equals(target.type)) {
                        Log.w(folder.name + " deleting id=" + message.id);
                        db.message().deleteMessage(message.id);
                    }
                } catch (JSONException ex) {
                    Log.e(ex);
                }

            throw new IllegalArgumentException("Message not found for " + op.name + " folder=" + folder.name);
        }

        db.message().setMessageUid(message.id, message.uid);
        message.uid = uid;
    }

    private static Long findUid(Context context, EntityAccount account, IMAPFolder ifolder, String msgid, Long from) throws MessagingException, IOException {
        String name = ifolder.getFullName();
        Log.i(name + " searching for msgid=" + msgid);

        Long uid = null;

        Message[] imessages = findMsgId(context, account, ifolder, msgid, from);
        if (imessages != null)
            for (Message iexisting : imessages)
                try {
                    long muid = ifolder.getUID(iexisting);
                    if (muid < 0)
                        continue;
                    Log.i(name + " found uid=" + muid + " for msgid=" + msgid);
                    // RFC3501: Unique identifiers are assigned in a strictly ascending fashion
                    if (uid == null || muid > uid)
                        uid = muid;
                } catch (MessageRemovedException ex) {
                    Log.w(ex);
                }

        Log.i(name + " got uid=" + uid + " for msgid=" + msgid);
        return uid;
    }

    private static Message[] findMsgId(Context context, EntityAccount account, IMAPFolder ifolder, String msgid, Long from) throws MessagingException, IOException {
        // https://stackoverflow.com/questions/18891509/how-to-get-message-from-messageidterm-for-yahoo-imap-profile
        if (account.isYahooJp() || from != null) {
            if (from == null)
                from = new Date().getTime();
            from -= 24 * 3600 * 1000L;
            long to = from + 3 * 24 * 3600 * 1000L;

            Message[] itemps = ifolder.search(
                    new AndTerm(
                            new ReceivedDateTerm(ComparisonTerm.GE, new Date(from)),
                            new ReceivedDateTerm(ComparisonTerm.LE, new Date(to))));
            List<Message> tmp = new ArrayList<>();
            for (Message itemp : itemps) {
                MessageHelper helper = new MessageHelper((MimeMessage) itemp, context);
                if (msgid.equals(helper.getMessageID()))
                    tmp.add(itemp);
            }
            Log.w("Fallback search by" +
                    " msgid=" + msgid +
                    " host=" + account.host +
                    " from=" + new Date(from) + " to=" + new Date(to) +
                    " found=" + tmp.size());
            return tmp.toArray(new Message[0]);
        } else
            return ifolder.search(new MessageIDTerm(msgid));
    }

    private static Map<EntityMessage, Message> findMessages(Context context, EntityFolder folder, List<EntityMessage> messages, POP3Store istore, POP3Folder ifolder) throws MessagingException, IOException {
        Map<String, String> caps = istore.capabilities();
        boolean hasUidl = caps.containsKey("UIDL");

        Message[] imessages = ifolder.getMessages();

        if (hasUidl) {
            FetchProfile ifetch = new FetchProfile();
            ifetch.add(UIDFolder.FetchProfileItem.UID);
            ifolder.fetch(imessages, ifetch);
        }

        Map<EntityMessage, Message> result = new HashMap<>();

        for (EntityMessage message : messages) {
            result.put(message, null);
            Log.i(folder.name + " POP searching for=" + message.uidl + "/" + message.msgid +
                    " messages=" + imessages.length + " uidl=" + hasUidl);

            for (Message imessage : imessages) {
                MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);

                String uidl = (hasUidl ? ifolder.getUID(imessage) : null);
                String msgid = (TextUtils.isEmpty(uidl) ? helper.getPOP3MessageID() : null);

                if ((uidl != null && uidl.equals(message.uidl)) ||
                        (msgid != null && msgid.equals(message.msgid))) {
                    Log.i(folder.name + " POP found=" + uidl + "/" + msgid);
                    result.put(message, imessage);
                }
            }
        }

        return result;
    }

    private static void onSetFlag(Context context, JSONArray jargs, EntityFolder folder, List<EntityMessage> messages, IMAPFolder ifolder, Flags.Flag flag) throws MessagingException, JSONException {
        // Mark message (un)seen
        DB db = DB.getInstance(context);

        if (flag != Flags.Flag.SEEN &&
                flag != Flags.Flag.ANSWERED &&
                flag != Flags.Flag.FLAGGED &&
                flag != Flags.Flag.DELETED)
            throw new IllegalArgumentException("Invalid flag=" + flag);

        if (folder.read_only)
            return;

        if (!ifolder.getPermanentFlags().contains(flag)) {
            for (EntityMessage message : messages)
                if (flag == Flags.Flag.SEEN) {
                    db.message().setMessageSeen(message.id, false);
                    db.message().setMessageUiSeen(message.id, false);
                } else if (flag == Flags.Flag.ANSWERED) {
                    db.message().setMessageAnswered(message.id, false);
                    db.message().setMessageUiAnswered(message.id, false);
                } else if (flag == Flags.Flag.FLAGGED) {
                    db.message().setMessageFlagged(message.id, false);
                    db.message().setMessageUiFlagged(message.id, false, null);
                } else if (flag == Flags.Flag.DELETED) {
                    db.message().setMessageDeleted(message.id, false);
                    db.message().setMessageUiDeleted(message.id, false);
                }
            return;
        }

        List<Long> uids = new ArrayList<>();
        boolean set = jargs.getBoolean(0);
        for (EntityMessage message : messages) {
            if (message.uid == null)
                if (messages.size() == 1)
                    throw new IllegalArgumentException("Set flag: uid missing");
                else
                    throw new MessagingException("Set flag: uid missing");
            if (flag == Flags.Flag.SEEN && !message.seen.equals(set))
                uids.add(message.uid);
            else if (flag == Flags.Flag.ANSWERED && !message.answered.equals(set))
                uids.add(message.uid);
            else if (flag == Flags.Flag.FLAGGED && !message.flagged.equals(set))
                uids.add(message.uid);
            else if (flag == Flags.Flag.DELETED && !message.deleted.equals(set))
                uids.add(message.uid);
        }

        if (uids.size() == 0)
            return;

        Message[] imessages = ifolder.getMessagesByUID(Helper.toLongArray(uids));
        for (Message imessage : imessages)
            if (imessage == null)
                if (messages.size() == 1)
                    throw new MessageRemovedException();
                else
                    throw new MessagingException("Set flag: message missing");

        ifolder.setFlags(imessages, new Flags(flag), set);

        for (EntityMessage message : messages)
            if (flag == Flags.Flag.SEEN && !message.seen.equals(set))
                db.message().setMessageSeen(message.id, set);
            else if (flag == Flags.Flag.ANSWERED && !message.answered.equals(set))
                db.message().setMessageAnswered(message.id, set);
            else if (flag == Flags.Flag.FLAGGED && !message.flagged.equals(set))
                db.message().setMessageFlagged(message.id, set);
            else if (flag == Flags.Flag.DELETED && !message.deleted.equals(set))
                db.message().setMessageDeleted(message.id, set);
    }

    private static void onAnswered(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, JSONException {
        // Mark message (un)answered
        DB db = DB.getInstance(context);

        if (folder.read_only)
            return;

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

        if (folder.read_only ||
                !ifolder.getPermanentFlags().contains(Flags.Flag.USER))
            return;

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
                    iarchive.close(false);
            }

            if (archived)
                try {
                    Message imessage = ifolder.getMessageByUID(message.uid);
                    if (imessage == null)
                        throw new MessageRemovedException();
                    imessage.setFlag(Flags.Flag.DELETED, true);
                    expunge(context, ifolder, Arrays.asList(imessage));
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
        boolean copy = jargs.optBoolean(2, false); // Cross account

        if (target != folder.id)
            throw new IllegalArgumentException("Invalid folder");

        // External draft might have a uid only
        if (TextUtils.isEmpty(message.msgid)) {
            message.msgid = EntityMessage.generateMessageId();
            db.message().setMessageMsgId(message.id, message.msgid);
        }

        Properties props = MessageHelper.getSessionProperties(account.unicode);
        Session isession = Session.getInstance(props, null);
        Flags flags = ifolder.getPermanentFlags();

        // Get raw message
        MimeMessage imessage;
        File file = message.getRawFile(context);
        if (folder.id.equals(message.folder)) {
            // Pre flight check
            if (!message.content)
                throw new IllegalArgumentException("Message body missing");

            if (!BuildConfig.DEBUG) {
                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                for (EntityAttachment attachment : attachments)
                    if (EntityAttachment.SMIME_SIGNATURE.equals(attachment.encryption))
                        for (EntityAttachment content : attachments)
                            if (EntityAttachment.SMIME_CONTENT.equals(content.encryption)) {
                                boolean afile = attachment.getFile(context).exists();
                                boolean cfile = content.getFile(context).exists();
                                if (!attachment.available || !afile || !content.available || !cfile) {
                                    Log.e("S/MIME vanished" +
                                            " available=" + attachment.available + "/" + content.available +
                                            " file=" + afile + "/" + cfile +
                                            " error=" + attachment.error + "/" + content.error);
                                    db.attachment().setAvailable(attachment.id, false);
                                    db.attachment().setAvailable(content.id, false);
                                    db.attachment().setEncryption(attachment.id, null);
                                    db.attachment().setEncryption(content.id, null);
                                }
                            }
            }

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
                imessage = new MimeMessageEx(isession, is, message.msgid);
            }

            imessage.removeHeader(MessageHelper.HEADER_CORRELATION_ID);
            imessage.addHeader(MessageHelper.HEADER_CORRELATION_ID, message.msgid);

            imessage.saveChanges();
            /*
                javax.mail.internet.ParseException: Unbalanced quoted string
                    at javax.mail.internet.HeaderTokenizer.collectString(SourceFile:15)
                    at javax.mail.internet.HeaderTokenizer.getNext(SourceFile:20)
                    at javax.mail.internet.HeaderTokenizer.next(SourceFile:4)
                    at javax.mail.internet.HeaderTokenizer.next(SourceFile:1)
                    at javax.mail.internet.ParameterList.<init>(SourceFile:23)
                    at javax.mail.internet.ContentType.<init>(SourceFile:17)
                    at javax.mail.internet.MimeBodyPart.updateHeaders(SourceFile:12)
                    at javax.mail.internet.MimeBodyPart.updateHeaders(SourceFile:1)
                    at javax.mail.internet.MimeMultipart.updateHeaders(SourceFile:3)
                    at javax.mail.internet.MimeBodyPart.updateHeaders(SourceFile:24)
                    at javax.mail.internet.MimeMessage.updateHeaders(SourceFile:1)
                    at javax.mail.internet.MimeMessage.saveChanges(SourceFile:3)
             */

            if (flags.contains(Flags.Flag.SEEN))
                imessage.setFlag(Flags.Flag.SEEN, message.ui_seen);
            if (flags.contains(Flags.Flag.ANSWERED))
                imessage.setFlag(Flags.Flag.ANSWERED, message.ui_answered);
            if (flags.contains(Flags.Flag.FLAGGED))
                imessage.setFlag(Flags.Flag.FLAGGED, message.ui_flagged);
            if (flags.contains(Flags.Flag.DELETED))
                imessage.setFlag(Flags.Flag.DELETED, message.ui_deleted);

            if (flags.contains(Flags.Flag.USER)) {
                if (message.isForwarded()) {
                    Flags fwd = new Flags(MessageHelper.FLAG_FORWARDED);
                    imessage.setFlags(new Flags(fwd), true);
                }
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
                Log.w(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // Handle auto read
        if (flags.contains(Flags.Flag.SEEN))
            if (autoread && !imessage.isSet(Flags.Flag.SEEN)) {
                Log.i(folder.name + " autoread");
                imessage.setFlag(Flags.Flag.SEEN, true);
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
            try {
                List<Message> delete = new ArrayList<>();

                if (message.uid != null)
                    try {
                        Message iprev = ifolder.getMessageByUID(message.uid);
                        if (iprev != null) {
                            Log.i(folder.name + " found prev uid=" + message.uid + " msgid=" + message.msgid);
                            iprev.setFlag(Flags.Flag.DELETED, true);
                            delete.add(iprev);
                        }
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }

                Log.i(folder.name + " searching for added msgid=" + message.msgid);
                Message[] imessages = findMsgId(context, account, ifolder, message.msgid, null);
                if (imessages != null) {
                    Long found = newuid;

                    for (Message iexisting : imessages)
                        try {
                            long muid = ifolder.getUID(iexisting);
                            if (muid < 0)
                                continue;
                            Log.i(folder.name + " found added uid=" + muid + " msgid=" + message.msgid);
                            if (found == null || muid > found)
                                found = muid;
                        } catch (MessageRemovedException ex) {
                            Log.w(ex);
                        }

                    if (found != null) {
                        if (newuid == null || found > newuid)
                            newuid = found;

                        for (Message iexisting : imessages)
                            try {
                                long muid = ifolder.getUID(iexisting);
                                if (muid < 0)
                                    continue;
                                if (muid < newuid &&
                                        (message.uid == null || message.uid != muid))
                                    try {
                                        iexisting.setFlag(Flags.Flag.DELETED, true);
                                        delete.add(iexisting);
                                    } catch (MessagingException ex) {
                                        Log.w(ex);
                                    }
                            } catch (MessageRemovedException ex) {
                                Log.w(ex);
                            }
                    }
                }

                expunge(context, ifolder, delete);

            } catch (MessagingException ex) {
                Log.w(ex);
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
            // Lookup added message
            int count = 0;
            Long found = newuid;
            while (found == null && count++ < FIND_RETRY_COUNT) {
                found = findUid(context, account, ifolder, message.msgid, count > 1 ? message.received : null);
                if (found == null)
                    try {
                        Thread.sleep(FIND_RETRY_DELAY);
                    } catch (InterruptedException ex) {
                        Log.e(ex);
                    }
            }

            try {
                db.beginTransaction();

                if (found == null) {
                    db.message().setMessageError(message.id,
                            "Message not found in target folder " + account.name + "/" + folder.name + " msgid=" + message.msgid);
                    db.message().setMessageUiHide(message.id, false);
                } else {
                    // Mark source read
                    if (autoread)
                        EntityOperation.queue(context, message, EntityOperation.SEEN, true);

                    // Delete source
                    if (!copy)
                        EntityOperation.queue(context, message, EntityOperation.DELETE, true /* POP3: permanent */);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            // Fetch target
            if (found != null)
                try {
                    Log.i(folder.name + " Fetching uid=" + found);
                    JSONArray fargs = new JSONArray();
                    fargs.put(found);
                    onFetch(context, fargs, folder, istore, ifolder, state);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
        }
    }

    private static void onMove(Context context, JSONArray jargs, boolean copy, EntityAccount account, EntityFolder folder, List<EntityMessage> messages, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        // Move message
        DB db = DB.getInstance(context);

        // Get arguments
        long id = jargs.getLong(0);
        boolean seen = jargs.optBoolean(1);
        boolean unflag = jargs.optBoolean(3);
        boolean delete = jargs.optBoolean(4);
        boolean create = jargs.optBoolean(5);

        Flags flags = ifolder.getPermanentFlags();

        // Get target folder
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();
        if (folder.id.equals(target.id))
            throw new IllegalArgumentException("self type=" + folder.type + "/" + target.type);
        if (!target.selectable)
            throw new IllegalArgumentException("not selectable type=" + target.type);

        if (create) {
            Folder icreate = istore.getFolder(target.name);
            if (!icreate.exists()) {
                ((IMAPFolder) ifolder).doCommand(new IMAPFolder.ProtocolCommand() {
                    @Override
                    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                        protocol.create(target.name);
                        return null;
                    }
                });
                ifolder.setSubscribed(true);
                db.folder().resetFolderTbc(target.id);
            }
        }

        // De-classify
        if (!copy &&
                !EntityFolder.TRASH.equals(target.type) &&
                !EntityFolder.ARCHIVE.equals(target.type))
            for (EntityMessage message : messages)
                MessageClassifier.classify(message, folder, false, context);

        IMAPFolder itarget = (IMAPFolder) istore.getFolder(target.name);

        // Get source messages
        Map<Message, EntityMessage> map = new HashMap<>();
        Map<EntityMessage, String> msgids = new HashMap<>();
        for (EntityMessage message : messages)
            try {
                if (message.uid == null)
                    throw new IllegalArgumentException("move without uid");
                Message imessage = ifolder.getMessageByUID(message.uid);
                if (imessage == null)
                    throw new MessageRemovedException("move without message");
                if (imessage.isExpunged())
                    throw new MessageRemovedException("move of expunged message");
                map.put(imessage, message);
            } catch (MessageRemovedException ex) {
                Log.e(ex);
                db.message().deleteMessage(message.id);
            }

        // Some servers return different capabilities for different sessions
        // NO [CANNOT] MOVE It's not possible to perform specified operation
        // https://stackoverflow.com/questions/56148668/cannot-delete-emails-of-domain-co-jp-type
        boolean canMove = !account.isYahooJp() &&
                MessageHelper.hasCapability(ifolder, "MOVE");

        // Some providers do not support the COPY operation for drafts
        boolean draft = (EntityFolder.DRAFTS.equals(folder.type) || EntityFolder.DRAFTS.equals(target.type));
        boolean duplicate = (copy && !account.isGmail()) || (draft && account.isGmail());
        if (draft || duplicate) {
            Log.i(folder.name + " " + (duplicate ? "copy" : "move") +
                    " from " + folder.type + " to " + target.type);

            if (!duplicate && account.isSeznam())
                ifolder.copyMessages(map.keySet().toArray(new Message[0]), itarget);
            else {
                List<Message> icopies = new ArrayList<>();
                for (Message imessage : map.keySet()) {
                    EntityMessage message = map.get(imessage);

                    Message icopy;
                    File file = new File(message.getFile(context).getAbsoluteFile() + ".copy");
                    try {
                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                            imessage.writeTo(os);
                        }

                        Properties props = MessageHelper.getSessionProperties(account.unicode);
                        Session isession = Session.getInstance(props, null);

                        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                            if (duplicate) {
                                String msgid = EntityMessage.generateMessageId();
                                msgids.put(message, msgid);
                                icopy = new MimeMessageEx(isession, is, msgid);
                                icopy.removeHeader(MessageHelper.HEADER_CORRELATION_ID);
                                icopy.saveChanges();

                                if (!copy) {
                                    List<EntityMessage> tmps = db.message().getMessagesByMsgId(message.account, message.msgid);
                                    for (EntityMessage tmp : tmps)
                                        if (target.id.equals(tmp.folder)) {
                                            db.message().setMessageMsgId(tmp.id, msgid);
                                            break;
                                        }
                                }
                            } else
                                icopy = new MimeMessage(isession, is);
                        }
                    } finally {
                        file.delete();
                    }

                    for (Flags.Flag flag : imessage.getFlags().getSystemFlags())
                        icopy.setFlag(flag, true);

                    icopies.add(icopy);
                }

                itarget.appendMessages(icopies.toArray(new Message[0]));
            }
        } else {
            for (Message imessage : map.keySet()) {
                Log.i((copy ? "Copy" : "Move") + " seen=" + seen + " unflag=" + unflag + " flags=" + imessage.getFlags() + " can=" + canMove);

                // Mark read
                if (seen && !imessage.isSet(Flags.Flag.SEEN) && flags.contains(Flags.Flag.SEEN))
                    imessage.setFlag(Flags.Flag.SEEN, true);

                // Remove star
                if (unflag && imessage.isSet(Flags.Flag.FLAGGED) && flags.contains(Flags.Flag.FLAGGED))
                    imessage.setFlag(Flags.Flag.FLAGGED, false);

                // Mark not spam
                if (!copy && ifolder.getPermanentFlags().contains(Flags.Flag.USER)) {
                    Flags junk = new Flags(MessageHelper.FLAG_JUNK);
                    Flags notJunk = new Flags(MessageHelper.FLAG_NOT_JUNK);
                    List<String> userFlags = Arrays.asList(imessage.getFlags().getUserFlags());
                    if (EntityFolder.JUNK.equals(target.type)) {
                        // To junk
                        if (userFlags.contains(MessageHelper.FLAG_NOT_JUNK))
                            imessage.setFlags(notJunk, false);
                        imessage.setFlags(junk, true);
                    } else if (EntityFolder.JUNK.equals(folder.type)) {
                        // From junk
                        if (userFlags.contains(MessageHelper.FLAG_JUNK))
                            imessage.setFlags(junk, false);
                        imessage.setFlags(notJunk, true);
                    }
                }
            }

            // https://tools.ietf.org/html/rfc6851
            if (!copy && canMove)
                try {
                    ifolder.moveMessages(map.keySet().toArray(new Message[0]), itarget);
                } catch (MessagingException ex) {
                    if (!(map.size() == 1 &&
                            ex.getCause() instanceof CommandFailedException &&
                            ex.getCause().getMessage() != null &&
                            ex.getCause().getMessage().contains("[EXPUNGEISSUED]")))
                        throw ex;
                }
            else
                ifolder.copyMessages(map.keySet().toArray(new Message[0]), itarget);
        }

        // Delete source
        if (!copy && (draft || !canMove)) {
            List<Message> deleted = new ArrayList<>();
            for (Message imessage : map.keySet())
                try {
                    imessage.setFlag(Flags.Flag.DELETED, true);
                    deleted.add(imessage);
                    if (!folder.synchronize || folder.poll || !MessageHelper.hasCapability(ifolder, "IDLE")) {
                        EntityMessage m = map.get(imessage);
                        if (m != null && m.uid != null)
                            EntityOperation.queue(context, folder, EntityOperation.FETCH, m.uid, false, true);
                    }
                } catch (MessageRemovedException ex) {
                    Log.w(ex);
                }
            expunge(context, ifolder, deleted);
        } else {
            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count, new Date().getTime());
        }

        // Fetch appended/copied when needed
        boolean fetch = (copy || delete ||
                !"connected".equals(target.state) ||
                !MessageHelper.hasCapability(ifolder, "IDLE"));
        if (draft || fetch)
            try {
                Log.i(target.name + " moved message fetch=" + fetch);
                itarget.open(READ_WRITE);

                boolean sync = false;
                List<Message> ideletes = new ArrayList<>();
                for (EntityMessage message : map.values())
                    try {
                        String msgid = msgids.get(message);
                        if (msgid == null)
                            msgid = message.msgid;

                        if (TextUtils.isEmpty(msgid))
                            throw new IllegalArgumentException("move: msgid missing");

                        Long uid = findUid(context, account, itarget, msgid, null);
                        if (uid == null)
                            if (duplicate || !EntityFolder.TRASH.equals(folder.type))
                                throw new IllegalArgumentException("move: uid not found");
                            else {
                                Log.w("move: uid not found");
                                continue;
                            }

                        if (draft || duplicate) {
                            Message icopy = itarget.getMessageByUID(uid);
                            if (icopy == null)
                                throw new IllegalArgumentException("move: gone uid=" + uid);

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

                        if (delete) {
                            Log.i(target.name + " Deleting uid=" + uid);
                            Message idelete = itarget.getMessageByUID(uid);
                            idelete.setFlag(Flags.Flag.DELETED, true);
                            ideletes.add(idelete);
                        } else if (fetch) {
                            Log.i(target.name + " Fetching uid=" + uid);
                            JSONArray fargs = new JSONArray();
                            fargs.put(uid);
                            onFetch(context, fargs, target, istore, itarget, state);
                        }
                    } catch (Throwable ex) {
                        if (ex instanceof IllegalArgumentException)
                            Log.i(ex);
                        else
                            Log.e(ex);
                        if (fetch)
                            sync = true;
                    }

                expunge(context, itarget, ideletes);

                if (sync)
                    EntityOperation.sync(context, target.id, false);
            } catch (Throwable ex) {
                Log.w(ex);
            } finally {
                if (itarget.isOpen())
                    itarget.close(false);
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

    private static void onFetch(Context context, JSONArray jargs, EntityFolder folder, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        long uid = jargs.getLong(0);
        boolean invalidate = jargs.optBoolean(1);
        boolean removed = jargs.optBoolean(2);

        DB db = DB.getInstance(context);
        EntityAccount account = db.account().getAccount(folder.account);
        if (account == null)
            throw new IllegalArgumentException("account missing");

        try {
            if (uid < 0)
                throw new MessageRemovedException(folder.name + " fetch uid=" + uid);
            if (removed)
                throw new MessageRemovedException("removed uid=" + uid);

            MimeMessage imessage = (MimeMessage) ifolder.getMessageByUID(uid);
            if (imessage == null)
                throw new MessageRemovedException(folder.name + " fetch not found uid=" + uid);
            // synchronizeMessage will check expunged/deleted

            if (invalidate && imessage instanceof IMAPMessage)
                ((IMAPMessage) imessage).invalidateHeaders();

            SyncStats stats = new SyncStats();
            boolean download = db.folder().getFolderDownload(folder.id);
            List<EntityRule> rules = db.rule().getEnabledRules(folder.id, false);

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID); // To check if message exists
            fp.add(FetchProfile.Item.FLAGS); // To update existing messages
            if (account.isGmail())
                fp.add(GmailFolder.FetchProfileItem.LABELS);
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

                if (download) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean fast_fetch = prefs.getBoolean("fast_fetch", false);

                    boolean async = false;
                    if (fast_fetch) {
                        long maxSize = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
                        if (maxSize == 0)
                            maxSize = Long.MAX_VALUE;
                        boolean download_limited = prefs.getBoolean("download_limited", false);
                        boolean download_eml = prefs.getBoolean("download_eml", false);

                        if (!message.content)
                            if ((!download_limited && state.getNetworkState().isUnmetered()) ||
                                    (message.size != null && message.size < maxSize))
                                async = true;

                        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                        for (EntityAttachment attachment : attachments)
                            if (!attachment.available)
                                if ((!download_limited && state.getNetworkState().isUnmetered()) ||
                                        (attachment.size != null && attachment.size < maxSize))
                                    async = true;

                        if (download_eml &&
                                (message.raw == null || !message.raw) &&
                                ((!download_limited && state.getNetworkState().isUnmetered()) ||
                                        (message.total != null && message.total < maxSize)))
                            async = true;
                    }

                    if (async && message.uid != null && !message.ui_hide)
                        EntityOperation.queue(context, message, EntityOperation.DOWNLOAD, message.uid);
                    else
                        downloadMessage(context, account, folder, istore, ifolder, imessage, message.id, state, stats);
                }
            }

            if (!stats.isEmpty())
                EntityLog.log(context, EntityLog.Type.Statistics,
                        account.name + "/" + folder.name + " fetch stats " + stats);
        } catch (Throwable ex) {
            if (MessageHelper.isRemoved(ex)) {
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

                int count = db.message().deleteMessage(folder.id, uid);
                Log.i(folder.name + " delete local uid=" + uid + " count=" + count);
            } else
                throw ex;
        } finally {
            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count, new Date().getTime());
        }
    }

    private static void onDelete(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, List<EntityMessage> messages, IMAPStore istore, IMAPFolder ifolder) throws MessagingException, IOException {
        // Delete message
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean perform_expunge = prefs.getBoolean("perform_expunge", true);
        boolean gmail_delete_all = prefs.getBoolean("gmail_delete_all", false);

        if (folder.local) {
            Log.i(folder.name + " local delete");
            for (EntityMessage message : messages)
                db.message().deleteMessage(message.id);
            return;
        }

        try {
            if (account.isGmail() && gmail_delete_all) {
                EntityFolder trash = db.folder().getFolderByType(account.id, EntityFolder.TRASH);
                if (trash != null) {
                    Map<String, Long> folders = new HashMap<>();
                    EntityFolder archive = db.folder().getFolderByType(account.id, EntityFolder.ARCHIVE);
                    if (archive != null)
                        folders.put(archive.name, archive.id);

                    List<Long> uids = new ArrayList<>();
                    for (EntityMessage message : messages)
                        if (message.uid != null)
                            uids.add(message.uid);

                    IMAPFolder itrash = (IMAPFolder) istore.getFolder(trash.name);
                    Message[] imessages = ifolder.getMessagesByUID(Helper.toLongArray(uids));

                    List<Message> imove = new ArrayList<>();
                    if (imessages != null)
                        for (Message imessage : imessages)
                            if (imessage instanceof GmailMessage)
                                try {
                                    imove.add(imessage);
                                    String[] labels = ((GmailMessage) imessage).getLabels();
                                    for (String label : labels)
                                        if (!folders.containsKey(label)) {
                                            EntityFolder f = db.folder().getFolderByName(account.id, label);
                                            if (f != null)
                                                folders.put(f.name, f.id);
                                        }
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }

                    ifolder.moveMessages(imove.toArray(new Message[0]), itrash);

                    itrash.open(READ_WRITE);
                    try {
                        List<Message> trashed = new ArrayList<>();
                        for (EntityMessage message : messages) {
                            Message[] itrashed = itrash.search(new MessageIDTerm(message.msgid));
                            if (itrashed != null && itrashed.length == 1)
                                trashed.add(itrashed[0]);
                        }

                        itrash.setFlags(trashed.toArray(new Message[0]), new Flags(Flags.Flag.DELETED), true);
                        if (perform_expunge)
                            expunge(context, itrash, trashed);
                    } finally {
                        if (itrash.isOpen())
                            itrash.close();
                    }

                    for (long fid : folders.values())
                        EntityOperation.sync(context, fid, false);

                    return;
                }
            }

            if (messages.size() > 1) {
                boolean ui_deleted = messages.get(0).ui_deleted;

                List<Long> uids = new ArrayList<>();
                for (EntityMessage message : messages) {
                    if (message.uid == null)
                        throw new MessagingException("Delete: uid missing");
                    if (message.ui_deleted != ui_deleted)
                        throw new MessagingException("Delete: flag inconsistent");
                    uids.add(message.uid);
                }

                Message[] idelete = ifolder.getMessagesByUID(Helper.toLongArray(uids));
                for (Message imessage : idelete)
                    if (imessage == null)
                        throw new MessagingException("Delete: message missing");

                EntityLog.log(context, folder.name + " deleting messages=" + uids.size());

                if (perform_expunge) {
                    ifolder.setFlags(idelete, new Flags(Flags.Flag.DELETED), true);
                    expunge(context, ifolder, Arrays.asList(idelete));
                    for (EntityMessage message : messages)
                        db.message().deleteMessage(message.id);
                } else {
                    ifolder.setFlags(idelete, new Flags(Flags.Flag.DELETED), ui_deleted);
                    for (EntityMessage message : messages)
                        db.message().setMessageDeleted(message.id, message.ui_deleted);
                }

                EntityLog.log(context, folder.name + " deleted messages=" + uids.size());
            } else if (messages.size() == 1) {
                List<Message> deleted = new ArrayList<>();

                EntityMessage message = messages.get(0);
                if (message.uid != null) {
                    Message iexisting = ifolder.getMessageByUID(message.uid);
                    if (iexisting == null)
                        Log.w(folder.name + " existing not found uid=" + message.uid);
                    else
                        try {
                            Log.i(folder.name + " deleting uid=" + message.uid);
                            if (perform_expunge)
                                iexisting.setFlag(Flags.Flag.DELETED, true);
                            else
                                iexisting.setFlag(Flags.Flag.DELETED, message.ui_deleted);
                            deleted.add(iexisting);
                        } catch (MessageRemovedException ignored) {
                            Log.w(folder.name + " existing gone uid=" + message.uid);
                        }
                }

                boolean found = (deleted.size() > 0);
                if (!TextUtils.isEmpty(message.msgid) &&
                        (!found || EntityFolder.DRAFTS.equals(folder.type)))
                    try {
                        Message[] imessages = findMsgId(context, account, ifolder, message.msgid, null);
                        if (imessages != null)
                            for (Message iexisting : imessages)
                                try {
                                    long muid = ifolder.getUID(iexisting);
                                    if (found && muid == message.uid)
                                        continue;

                                    // Fail safe
                                    MessageHelper helper = new MessageHelper((MimeMessage) iexisting, context);
                                    if (!message.msgid.equals(helper.getMessageID()))
                                        continue;

                                    Log.i(folder.name + " deleting uid=" + muid);
                                    if (perform_expunge)
                                        iexisting.setFlag(Flags.Flag.DELETED, true);
                                    else
                                        iexisting.setFlag(Flags.Flag.DELETED, message.ui_deleted);

                                    deleted.add(iexisting);
                                } catch (MessageRemovedException ex) {
                                    Log.w(ex);
                                }
                    } catch (MessagingException ex) {
                        Log.w(ex);
                    }

                if (perform_expunge) {
                    if (deleted.size() == 0 || expunge(context, ifolder, deleted))
                        db.message().deleteMessage(message.id);
                } else {
                    if (deleted.size() > 0)
                        db.message().setMessageDeleted(message.id, message.ui_deleted);
                }
            }
        } finally {
            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count, new Date().getTime());
        }
    }

    private static void onDelete(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, List<EntityMessage> messages, POP3Folder ifolder, POP3Store istore, State state) throws JSONException, MessagingException, IOException {
        boolean permanent = (jargs.length() > 0 && jargs.getBoolean(0));

        // Delete from server
        if (!EntityFolder.INBOX.equals(folder.type) || (account.leave_deleted && !permanent))
            throw new IllegalArgumentException("POP3: invalid DELETE" +
                    " folder=" + folder.type +
                    " leave=" + account.leave_deleted +
                    " permanent=" + permanent);

        Map<EntityMessage, Message> map = findMessages(context, folder, messages, istore, ifolder);
        for (EntityMessage message : messages) {
            Message imessage = map.get(message);
            if (imessage != null) {
                Log.i(folder.name + " POP delete=" + message.uidl + "/" + message.msgid);
                imessage.setFlag(Flags.Flag.DELETED, true);
            }
        }

        if (map.size() > 0)
            try {
                Log.i(folder.name + " POP expunge");
                ifolder.close(true);
                ifolder.open(Folder.READ_WRITE);
            } catch (Throwable ex) {
                Log.e(ex);
                state.error(new FolderClosedException(ifolder, "POP", new Exception(ex)));
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

    private static void onRaw(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException, IOException, JSONException {
        // Download raw message
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean save_user_flags = prefs.getBoolean("save_user_flags", false);

        if (message.raw == null || !message.raw) {
            IMAPMessage imessage = (IMAPMessage) ifolder.getMessageByUID(message.uid);
            if (imessage == null)
                throw new MessageRemovedException();

            EntityLog.log(context, "Downloading raw id=" + message.id + " subject=" + message.subject);
            File file = message.getRawFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                imessage.writeTo(os);
            }

            Properties props = MessageHelper.getSessionProperties(account.unicode);
            Session isession = Session.getInstance(props, null);

            MimeMessage saved;
            try (InputStream is = new FileInputStream(file)) {
                saved = new MimeMessage(isession, is);
            }

            if (save_user_flags) {
                String userFlags = TextUtils.join(",", imessage.getFlags().getUserFlags());
                saved.addHeader("X-User-Flags", userFlags);
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                    saved.writeTo(os);
                }
            }

            // Yahoo is returning incorrect messages
            MessageHelper helper = new MessageHelper(saved, context);
            if (!Objects.equals(message.msgid, helper.getMessageID()))
                throw new MessagingException("Incorrect msgid=" + message.msgid + "/" + helper.getMessageID());

            db.message().setMessageRaw(message.id, true);
        }

        if (jargs.length() > 0) {
            // Cross account move/copy
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

    private static void onRaw(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, POP3Store istore, POP3Folder ifolder) throws MessagingException, IOException, JSONException {
        // Download raw message
        DB db = DB.getInstance(context);

        if (!EntityFolder.INBOX.equals(folder.type))
            throw new IllegalArgumentException("Not INBOX type=" + folder.type);

        if (message.raw == null || !message.raw) {
            Map<EntityMessage, Message> map = findMessages(context, folder, Arrays.asList(message), istore, ifolder);
            if (map.get(message) == null)
                throw new IllegalArgumentException("Message not found msgid=" + message.msgid);

            File file = message.getRawFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                map.get(message).writeTo(os);
            }

            db.message().setMessageRaw(message.id, true);
        }

        if (jargs.length() > 0) {
            // Cross account move/copy
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean download_plain = prefs.getBoolean("download_plain", false);

        boolean plain_text = jargs.optBoolean(0, download_plain);
        String charset = (jargs.isNull(1) ? null : jargs.optString(1, null));

        if (message.uid == null)
            throw new IllegalArgumentException("uid missing");

        // Download message body
        DB db = DB.getInstance(context);

        if (message.content && message.isPlainOnly() == plain_text && charset == null)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null) {
            File file = message.getFile(context);
            Helper.writeText(file, "");
            db.message().setMessageContent(message.id,
                    true,
                    null,
                    null,
                    null,
                    context.getString(R.string.title_not_existing));
            return;
            //throw new MessageRemovedException();
        }

        MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);
        MessageHelper.MessageParts parts = helper.getMessageParts();
        String body = parts.getHtml(context, plain_text, charset);
        File file = message.getFile(context);
        Helper.writeText(file, body);
        String text = HtmlHelper.getFullText(context, body);
        message.preview = HtmlHelper.getPreview(text);
        message.language = HtmlHelper.getLanguage(context, message.subject, text);
        Integer plain_only = parts.isPlainOnly();
        if (plain_text)
            plain_only = 1 | (plain_only == null ? 0 : plain_only & 0x80);
        db.message().setMessageContent(message.id,
                true,
                message.language,
                plain_only,
                message.preview,
                parts.getWarnings(message.warning));
        MessageClassifier.classify(message, folder, true, context);

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
        if (message.uid == null)
            throw new IllegalArgumentException("Attachment/message uid missing");

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Get message parts
        MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);
        MessageHelper.MessageParts parts = helper.getMessageParts();

        // Download attachment
        parts.downloadAttachment(context, attachment, folder);

        if (attachment.size != null)
            EntityLog.log(context, "Operation attachment size=" + attachment.size);
    }

    private static void onDetach(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, EntityMessage message, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        DB db = DB.getInstance(context);

        JSONArray jids = jargs.getJSONArray(0);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < jids.length(); i++)
            ids.add(jids.getLong(i));

        if (message.uid == null)
            throw new IllegalArgumentException("Delete attachments uid missing");

        EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        String msgid = EntityMessage.generateMessageId();
        String ref = (TextUtils.isEmpty(message.references)
                ? message.msgid
                : message.references + " " + message.msgid);
        MimeMessage icopy = new MimeMessageEx((MimeMessage) imessage, msgid);
        icopy.addHeader("References", MessageHelper.limitReferences(ref));
        icopy.addHeader(MessageHelper.HEADER_MODIFIED_TIME, Long.toString(new Date().getTime()));
        MessageHelper helper = new MessageHelper(icopy, context);
        MessageHelper.MessageParts parts = helper.getMessageParts();
        List<MessageHelper.AttachmentPart> aparts = parts.getAttachmentParts();

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
        for (EntityAttachment attachment : attachments)
            if (ids.contains(attachment.id)) {
                Part apart = aparts.get(attachment.sequence - 1).part;
                if (!deletePart(icopy, apart))
                    throw new IllegalArgumentException("Attachment part not found");
            }

        ifolder.appendMessages(new Message[]{icopy});

        Long uid = findUid(context, account, ifolder, msgid, null);
        if (uid != null) {
            JSONArray fargs = new JSONArray();
            fargs.put(uid);
            onFetch(context, fargs, folder, istore, ifolder, state);
        }

        if (trash == null) {
            imessage.setFlag(Flags.Flag.DELETED, true);
            expunge(context, ifolder, Arrays.asList(imessage));
        } else
            EntityOperation.queue(context, message, EntityOperation.MOVE, trash.id);
    }

    static boolean deletePart(Part part, Part attachment) throws MessagingException, IOException {
        boolean deleted = false;
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                Part child = multipart.getBodyPart(i);
                if (child == attachment) {
                    String fileName = child.getFileName();
                    String contentType = child.getContentType();
                    String disposition = child.getDisposition();

                    if (fileName != null && !fileName.startsWith("deleted"))
                        fileName = "deleted" + (TextUtils.isEmpty(fileName) ? "" : "_" + fileName);
                    if (TextUtils.isEmpty(contentType))
                        contentType = "application/octet-stream";
                    if (TextUtils.isEmpty(disposition))
                        disposition = Part.ATTACHMENT;

                    multipart.removeBodyPart(i);

                    try {
                        // Can't upload empty message/rfc822
                        ContentType ct = new ContentType(contentType);
                        if ("message/rfc822".equalsIgnoreCase(ct.getBaseType()))
                            contentType = "application/octet-stream";
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }

                    BodyPart placeholderPart = new MimeBodyPart();
                    placeholderPart.setContent("", contentType);
                    placeholderPart.setFileName(fileName);
                    placeholderPart.setDisposition(disposition);
                    multipart.addBodyPart(placeholderPart);

                    deleted = true;
                } else {
                    if (deletePart(child, attachment))
                        deleted = true;
                }
            }
        }

        if (part instanceof MimeMessage)
            ((MimeMessage) part).saveChanges();
        else if (part instanceof Multipart)
            part.setDataHandler(part.getDataHandler());

        return deleted;
    }

    private static void onBody(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, POP3Folder ifolder, POP3Store istore) throws MessagingException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean download_plain = prefs.getBoolean("download_plain", false);

        boolean plain_text = jargs.optBoolean(0, download_plain);
        String charset = (jargs.isNull(1) ? null : jargs.optString(1, null));

        if (!EntityFolder.INBOX.equals(folder.type))
            throw new IllegalArgumentException("Not INBOX type=" + folder.type);

        if (message.content && message.isPlainOnly() == plain_text && charset == null)
            return;

        Map<EntityMessage, Message> map = findMessages(context, folder, Arrays.asList(message), istore, ifolder);
        if (map.get(message) == null)
            throw new IllegalArgumentException("Message not found msgid=" + message.msgid);

        MessageHelper helper = new MessageHelper((MimeMessage) map.entrySet().iterator().next().getValue(), context);
        MessageHelper.MessageParts parts = helper.getMessageParts();

        String body = parts.getHtml(context, plain_text, charset);
        File file = message.getFile(context);
        Helper.writeText(file, body);
        String text = HtmlHelper.getFullText(context, body);
        message.preview = HtmlHelper.getPreview(text);
        message.language = HtmlHelper.getLanguage(context, message.subject, text);
        Integer plain_only = parts.isPlainOnly();
        if (plain_text)
            plain_only = 1 | (plain_only == null ? 0 : plain_only & 0x80);

        DB db = DB.getInstance(context);
        db.message().setMessageContent(message.id,
                true,
                message.language,
                plain_only,
                message.preview,
                parts.getWarnings(message.warning));
    }

    private static void onAttachment(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, POP3Folder ifolder, POP3Store istore) throws JSONException, MessagingException, IOException {
        long id = jargs.getLong(0);

        if (!EntityFolder.INBOX.equals(folder.type))
            throw new IllegalArgumentException("Not INBOX type=" + folder.type);

        DB db = DB.getInstance(context);
        EntityAttachment attachment = db.attachment().getAttachment(id);
        if (attachment == null)
            throw new IllegalArgumentException("Local attachment not found");
        if (attachment.subsequence != null)
            throw new IllegalArgumentException("Download of sub attachment");
        if (attachment.available)
            return;

        Map<EntityMessage, Message> map = findMessages(context, folder, Arrays.asList(message), istore, ifolder);
        if (map.get(message) == null)
            throw new IllegalArgumentException("Message not found msgid=" + message.msgid);

        MessageHelper helper = new MessageHelper((MimeMessage) map.entrySet().iterator().next().getValue(), context);
        MessageHelper.MessageParts parts = helper.getMessageParts();

        // Download attachment
        parts.downloadAttachment(context, attachment, folder);

        if (attachment.size != null)
            EntityLog.log(context, "Operation attachment size=" + attachment.size);
    }

    private static void onExists(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws MessagingException, IOException {
        DB db = DB.getInstance(context);

        boolean retry = jargs.optBoolean(0);

        if (message.uid != null)
            return;

        if (message.msgid == null)
            throw new IllegalArgumentException("exists without msgid");

        // Search for message
        // Alternative, inconsistent for Outlook: X-Microsoft-Original-Message-ID
        Message[] imessages = ifolder.search(account.isOutlook()
                ? new HeaderTerm(MessageHelper.HEADER_CORRELATION_ID, message.msgid)
                : new MessageIDTerm(message.msgid));
        EntityLog.log(context, folder.name + " exists" +
                " retry=" + retry +
                " host=" + account.host +
                " outlook=" + account.isOutlook() +
                " messages=" + (imessages == null ? null : imessages.length));

        if (account.isOutlook() && (imessages == null || imessages.length == 0)) {
            imessages = ifolder.search(
                    new HeaderTerm(MessageHelper.HEADER_MICROSOFT_ORIGINAL_MESSAGE_ID, message.msgid));
            EntityLog.log(context, folder.name + " exists alt" +
                    " retry=" + retry +
                    " host=" + account.host +
                    " outlook=" + account.isOutlook() +
                    " messages=" + (imessages == null ? null : imessages.length));
        }

        // Searching for random header:
        //   iCloud: NO [UNAVAILABLE] Unexpected exception
        //   Seznam: Jakarta Mail Exception: java.io.IOException: Connection dropped by server?

        // Some email servers are slow with adding sent messages
        if (retry)
            Log.w(folder.name + " EXISTS retry" +
                    " found=" + (imessages == null ? null : imessages.length) +
                    " host=" + account.host);
        else if (imessages == null || imessages.length == 0) {
            long next = new Date().getTime() + EXISTS_RETRY_DELAY;

            Intent intent = new Intent(context, ServiceSynchronize.class);
            intent.setAction("exists:" + message.id);
            PendingIntent piExists = PendingIntentCompat.getForegroundService(
                    context, ServiceSynchronize.PI_EXISTS, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = Helper.getSystemService(context, AlarmManager.class);
            AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, next, piExists);
            return;
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
                db.folder().setFolderAutoAdd(folder.id, false);
                long uid = ifolder.getUID(imessages[0]);
                EntityOperation.queue(context, folder, EntityOperation.FETCH, uid);
            } else {
                db.folder().setFolderAutoAdd(folder.id, true);
                EntityOperation.queue(context, message, EntityOperation.ADD);
            }
        } else {
            db.folder().setFolderAutoAdd(folder.id, true);
            if (imessages != null && imessages.length > 1)
                Log.e(folder.name + " EXISTS messages=" + imessages.length + " retry=" + retry);
            EntityLog.log(context, folder.name +
                    " EXISTS messages=" + (imessages == null ? null : imessages.length));
            if (account.isYahoo())
                EntityOperation.sync(context, folder.id, false);
            else
                EntityOperation.queue(context, message, EntityOperation.ADD);
        }
    }

    private static void onReport(Context context, JSONArray jargs, EntityFolder folder, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException {
        String msgid = jargs.getString(0);
        String keyword = jargs.getString(1);

        if (TextUtils.isEmpty(msgid))
            throw new IllegalArgumentException("msgid missing");

        if (TextUtils.isEmpty(keyword))
            throw new IllegalArgumentException("keyword missing");

        if (folder.read_only) {
            Log.w(folder.name + " read-only");
            return;
        }

        if (!ifolder.getPermanentFlags().contains(Flags.Flag.USER)) {
            Log.w(folder.name + " has no keywords");
            return;
        }

        Message[] imessages = ifolder.search(new MessageIDTerm(msgid));
        if (imessages == null || imessages.length == 0) {
            Log.w(folder.name + " " + msgid + " not found");
            return;
        }

        for (Message imessage : imessages) {
            long uid = ifolder.getUID(imessage);
            Log.i("Report uid=" + uid + " keyword=" + keyword);

            Flags flags = new Flags(keyword);
            imessage.setFlags(flags, true);

            if (BuildConfig.DEBUG)
                try {
                    JSONArray fargs = new JSONArray();
                    fargs.put(uid);
                    onFetch(context, fargs, folder, istore, ifolder, state);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
        }
    }

    static void onSynchronizeFolders(
            Context context, EntityAccount account, Store istore, State state,
            boolean keep_alive, boolean force) throws MessagingException {
        // Folder names: https://datatracker.ietf.org/doc/html/rfc2060#section-5.1.3
        DB db = DB.getInstance(context);

        if (account.protocol != EntityAccount.TYPE_IMAP)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sync_folders = prefs.getBoolean("sync_folders", true);
        boolean sync_folders_poll = prefs.getBoolean("sync_folders_poll", false);
        boolean sync_shared_folders = prefs.getBoolean("sync_shared_folders", false);
        boolean sync_added_folders = prefs.getBoolean("sync_added_folders", false);
        Log.i(account.name + " sync folders=" + sync_folders +
                " poll=" + sync_folders_poll +
                " shared=" + sync_shared_folders +
                " added=" + sync_added_folders +
                " keep_alive=" + keep_alive +
                " force=" + force);

        // Fix folder poll setting
        boolean fixed = prefs.getBoolean("fixed_poll." + account.id, false);
        if (!fixed &&
                account.created != null &&
                account.created > 1691193600 * 1000L /* 2023-08-05 00:00 */ &&
                account.created < 1692223200 * 1000L /* 2023-05-17 00:00 */)
            try {
                int count = 0;
                EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                List<EntityFolder> children = db.folder().getChildFolders(inbox.id);
                for (EntityFolder child : children)
                    if (!child.poll && EntityFolder.USER.equals(child.type)) {
                        count++;
                        db.folder().setFolderPoll(child.id, true);
                        EntityLog.log(context, "Fixed poll=" + child.name + ":" + child.type);
                    }
                if (count > 0)
                    Log.e("Fixed poll count=" + count);
            } catch (Throwable ex) {
                Log.e(ex);
            } finally {
                prefs.edit().putBoolean("fixed_poll." + account.id, true).apply();
            }

        if (force)
            sync_folders = true;
        if (keep_alive)
            sync_folders = sync_folders_poll;
        if (!sync_folders)
            sync_shared_folders = false;

        // Get folder names
        boolean drafts = false;
        boolean user = false;
        Map<String, EntityFolder> local = new HashMap<>();
        List<EntityFolder> folders = db.folder().getFolders(account.id, false, false);
        for (EntityFolder folder : folders) {
            if (EntityFolder.USER.equals(folder.type))
                user = true;
            if (folder.tbc != null) {
                try {
                    // Prefix folder with namespace
                    try {
                        Folder[] ns = istore.getPersonalNamespaces();
                        Folder[] sh = istore.getSharedNamespaces();
                        if (ns != null && ns.length == 1 &&
                                !(sync_shared_folders && sh != null && sh.length > 0)) {
                            String n = ns[0].getFullName();
                            // Typically "" or "INBOX"
                            if (!TextUtils.isEmpty(n)) {
                                n += ns[0].getSeparator();
                                if (!folder.name.startsWith(n)) {
                                    folder.name = n + folder.name;
                                    db.folder().updateFolder(folder);
                                }
                            }
                        }
                    } catch (MessagingException ex) {
                        Log.w(ex);
                    }

                    EntityLog.log(context, folder.name + " creating");
                    Folder ifolder = istore.getFolder(folder.name);
                    if (ifolder.exists())
                        EntityLog.log(context, folder.name + " already exists on server");
                    else
                        try {
                            ((IMAPFolder) ifolder).doCommand(new IMAPFolder.ProtocolCommand() {
                                @Override
                                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                    protocol.create(folder.name);
                                    return null;
                                }
                            });
                            ifolder.setSubscribed(true);
                        } catch (MessagingException ex) {
                            // com.sun.mail.iap.CommandFailedException:
                            //  K5 NO Client tried to access nonexistent namespace.
                            //  (Mailbox name should probably be prefixed with: INBOX.) (n.nnn + n.nnn secs).
                            // com.sun.mail.iap.CommandFailedException:
                            //  AN5 NO [OVERQUOTA] Quota exceeded (number of mailboxes exceeded) (n.nnn + n.nnn + n.nnn secs).
                            Log.w(ex);
                            EntityLog.log(context, folder.name + " creation " +
                                    ex + "\n" + android.util.Log.getStackTraceString(ex));
                            db.account().setAccountError(account.id, Log.formatThrowable(ex));
                        }
                    local.put(folder.name, folder);
                } finally {
                    db.folder().resetFolderTbc(folder.id);
                    sync_folders = true;
                }

            } else if (folder.rename != null) {
                try {
                    EntityLog.log(context, folder.name + " rename into " + folder.rename);
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
                                itarget.close(false);
                            } catch (MessagingException ex) {
                                Log.w(ex);
                            }

                        db.folder().renameFolder(folder.account, folder.name, folder.rename);
                        folder.name = folder.rename;
                    }
                } finally {
                    db.folder().resetFolderRename(folder.id);
                    sync_folders = true;
                }

            } else if (folder.tbd != null && folder.tbd) {
                try {
                    EntityLog.log(context, folder.name + " deleting server");
                    Folder ifolder = istore.getFolder(folder.name);
                    if (ifolder.exists()) {
                        try {
                            ifolder.setSubscribed(false);
                            ((IMAPFolder) ifolder).doCommand(new IMAPFolder.ProtocolCommand() {
                                @Override
                                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                    protocol.delete(folder.name);
                                    return null;
                                }
                            });
                            EntityLog.log(context, folder.name + " deleting device");
                            db.folder().deleteFolder(folder.id);
                        } catch (MessagingException ex) {
                            Log.w(ex);
                            EntityLog.log(context, folder.name + " deletion " +
                                    ex + "\n" + android.util.Log.getStackTraceString(ex));
                            db.account().setAccountError(account.id, Log.formatThrowable(ex));
                        }
                    } else
                        EntityLog.log(context, folder.name + " does not exist on server anymore");
                } finally {
                    db.folder().resetFolderTbd(folder.id);
                    sync_folders = true;
                }

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
                    if (folder.selectable && folder.synchronize && folder.initialize != 0)
                        sync_folders = true;
                }
            }

            String key = "label.color." + folder.name;
            if (folder.color == null)
                prefs.edit().remove(key).apply();
            else
                prefs.edit().putInt(key, folder.color).apply();
        }
        Log.i("Local folder count=" + local.size() + " drafts=" + drafts);

        if (!drafts) {
            String ldrafts = context.getString(R.string.title_folder_local_drafts);
            EntityFolder d = db.folder().getFolderByName(account.id, ldrafts);
            if (d == null) {
                d = new EntityFolder();
                d.account = account.id;
                d.name = ldrafts;
                d.type = EntityFolder.DRAFTS;
                d.local = true;
                d.setProperties();
                d.synchronize = false;
                d.download = false;
                d.sync_days = Integer.MAX_VALUE;
                d.keep_days = Integer.MAX_VALUE;
                db.folder().insertFolder(d);
            }
        }

        if (!sync_folders)
            return;

        EntityLog.log(context, "Start sync folders account=" + account.name);

        // Get default folder
        Folder defaultFolder = istore.getDefaultFolder();

        // Get remote folders
        long start = new Date().getTime();
        List<Pair<Folder, Folder>> ifolders = new ArrayList<>();
        List<String> subscription = new ArrayList<>();

        boolean root = false;
        List<Folder> personal = new ArrayList<>();
        try {
            Folder[] pnamespaces = istore.getPersonalNamespaces();
            if (pnamespaces != null) {
                personal.addAll(Arrays.asList(pnamespaces));
                for (Folder p : pnamespaces)
                    if (defaultFolder.getFullName().equals(p.getFullName())) {
                        root = true;
                        break;
                    }
            }
        } catch (MessagingException ex) {
            Log.e(ex);
        }

        if (!root)
            personal.add(defaultFolder);

        for (Folder namespace : personal) {
            EntityLog.log(context, "Personal namespace=" + namespace.getFullName());

            String pattern = namespace.getFullName() + "*";
            for (Folder ifolder : defaultFolder.list(pattern))
                ifolders.add(new Pair<>(namespace, ifolder));

            try {
                Folder[] isubscribed = defaultFolder.listSubscribed(pattern);
                for (Folder ifolder : isubscribed) {
                    String fullName = ifolder.getFullName();
                    if (TextUtils.isEmpty(fullName)) {
                        Log.w("Subscribed folder name empty namespace=" + defaultFolder.getFullName());
                        continue;
                    }
                    subscription.add(fullName);
                    Log.i("Subscribed " + fullName);
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
        }

        if (sync_shared_folders) {
            // https://tools.ietf.org/html/rfc2342
            Folder[] shared = istore.getSharedNamespaces();
            EntityLog.log(context, "Shared namespaces=" + shared.length);

            for (Folder namespace : shared) {
                EntityLog.log(context, "Shared namespace=" + namespace.getFullName());

                String pattern = namespace.getFullName() + "*";
                try {
                    for (Folder ifolder : defaultFolder.list(pattern))
                        ifolders.add(new Pair<>(namespace, ifolder));
                } catch (FolderNotFoundException ex) {
                    Log.w(ex);
                }

                try {
                    Folder[] isubscribed = namespace.listSubscribed(pattern);
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
            }
        }

        long duration = new Date().getTime() - start;

        Log.i("Remote folder count=" + ifolders.size() +
                " subscriptions=" + subscription.size() +
                " fetched in " + duration + " ms");

        if (ifolders.size() == 0) {
            List<String> ns = new ArrayList<>();
            for (Folder namespace : personal)
                ns.add("'" + namespace.getFullName() + "'");
            Log.e(account.host + " no folders listed" +
                    " namespaces=" + TextUtils.join(",", ns));
            return;
        }

        // Check if system folders were renamed
        try {
            for (Pair<Folder, Folder> ifolder : ifolders) {
                String fullName = ifolder.second.getFullName();
                if (TextUtils.isEmpty(fullName))
                    continue;

                String[] attrs = ((IMAPFolder) ifolder.second).getAttributes();
                String type = EntityFolder.getType(attrs, fullName, false);
                if (type != null &&
                        !EntityFolder.USER.equals(type) &&
                        !EntityFolder.SYSTEM.equals(type)) {

                    // Rename system folders
                    for (EntityFolder folder : new ArrayList<>(local.values()))
                        if (type.equals(folder.type) &&
                                !fullName.equals(folder.name) &&
                                !local.containsKey(fullName) &&
                                !istore.getFolder(folder.name).exists()) {
                            Log.e(account.host +
                                    " renaming " + type + " folder" +
                                    " from " + folder.name + " to " + fullName);
                            local.remove(folder.name);
                            local.put(fullName, folder);
                            folder.name = fullName;
                            db.folder().setFolderName(folder.id, fullName);
                        }

                    // Reselect system folders once
                    String key = "unset." + account.id + "." + type;
                    boolean unset = prefs.getBoolean(key, false);
                    if (!unset) {
                        EntityFolder folder = db.folder().getFolderByType(account.id, type);
                        if (folder == null) {
                            folder = db.folder().getFolderByName(account.id, fullName);
                            if (folder != null && !folder.local) {
                                Log.e("Reselected " + account.host + " " + type + "=" + fullName);
                                folder.type = type;
                                folder.setProperties();
                                folder.setSpecials(account);
                                db.folder().updateFolder(folder);

                                if (EntityFolder.TRASH.equals(folder.type) &&
                                        account.swipe_left != null && account.swipe_left > 0) {
                                    EntityFolder swipe = db.folder().getFolder(account.swipe_left);
                                    if (swipe == null) {
                                        Log.e("Reselected " + account.host + " swipe left");
                                        account.swipe_left = folder.id;
                                        db.account().setAccountSwipes(account.id,
                                                account.swipe_left, account.swipe_right);
                                    }
                                }

                                if (EntityFolder.ARCHIVE.equals(folder.type) &&
                                        account.swipe_right != null && account.swipe_right > 0) {
                                    EntityFolder swipe = db.folder().getFolder(account.swipe_right);
                                    if (swipe == null) {
                                        Log.e("Reselected " + account.host + " swipe right");
                                        account.swipe_right = folder.id;
                                        db.account().setAccountSwipes(account.id,
                                                account.swipe_left, account.swipe_right);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        Map<String, EntityFolder> nameFolder = new HashMap<>();
        Map<String, List<EntityFolder>> parentFolders = new HashMap<>();
        for (Pair<Folder, Folder> ifolder : ifolders) {
            String fullName = ifolder.second.getFullName();
            if (TextUtils.isEmpty(fullName)) {
                Log.e("Folder name empty");
                continue;
            }

            String[] attrs = ((IMAPFolder) ifolder.second).getAttributes();
            String type = EntityFolder.getType(attrs, fullName, false);
            String subtype = EntityFolder.getSubtype(attrs, fullName);
            boolean subscribed = subscription.contains(fullName);

            boolean selectable = true;
            boolean inferiors = true;
            for (String attr : attrs) {
                if (attr.equalsIgnoreCase("\\NoSelect"))
                    selectable = false;
                if (attr.equalsIgnoreCase("\\NoInferiors"))
                    inferiors = false;
            }
            selectable = selectable && ((ifolder.second.getType() & IMAPFolder.HOLDS_MESSAGES) != 0);
            inferiors = inferiors && ((ifolder.second.getType() & IMAPFolder.HOLDS_FOLDERS) != 0);

            if (EntityFolder.INBOX.equals(type))
                selectable = true;

            Log.i(account.name + ":" + fullName + " type=" + type + ":" + subtype +
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
                        EntityFolder parent = null;
                        char separator = ifolder.first.getSeparator();
                        int sep = fullName.lastIndexOf(separator);
                        if (sep > 0)
                            parent = db.folder().getFolderByName(account.id, fullName.substring(0, sep));

                        if (!EntityFolder.USER.equals(type) && !EntityFolder.SYSTEM.equals(type)) {
                            EntityFolder has = db.folder().getFolderByType(account.id, type);
                            if (has != null)
                                type = EntityFolder.USER;
                        }

                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.namespace = ifolder.first.getFullName();
                        folder.separator = separator;
                        folder.name = fullName;
                        folder.type = type;
                        folder.subtype = type;
                        folder.subscribed = subscribed;
                        folder.selectable = selectable;
                        folder.inferiors = inferiors;
                        folder.setProperties();
                        folder.setSpecials(account);

                        if (selectable)
                            folder.inheritFrom(parent);
                        if (user && sync_added_folders && EntityFolder.USER.equals(type))
                            folder.synchronize = true;

                        folder.id = db.folder().insertFolder(folder);
                        Log.i(folder.name + " added type=" + folder.type + " sync=" + folder.synchronize);
                        if (folder.synchronize)
                            EntityOperation.sync(context, folder.id, false);
                    } else {
                        Log.i(folder.name + " exists type=" + folder.type);

                        folder.namespace = ifolder.first.getFullName();
                        folder.separator = ifolder.first.getSeparator();
                        db.folder().setFolderNamespace(folder.id, folder.namespace, folder.separator);

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
                        db.folder().setFolderSubtype(folder.id, subtype);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                    Log.i("End sync folder");
                }

                nameFolder.put(folder.name, folder);
                String parentName = folder.getParentName();
                if (!parentFolders.containsKey(parentName))
                    parentFolders.put(parentName, new ArrayList<>());
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
        for (String parentName : parentFolders.keySet()) {
            EntityFolder parent = nameFolder.get(parentName);
            for (EntityFolder child : parentFolders.get(parentName)) {
                String rootType = null;
                EntityFolder r = parent;
                while (r != null) {
                    rootType = r.type;
                    if (!EntityFolder.USER.equals(r.type) && !EntityFolder.SYSTEM.equals(r.type))
                        break;
                    r = nameFolder.get(r.getParentName());
                }
                if (EntityFolder.USER.equals(rootType) || EntityFolder.SYSTEM.equals(rootType))
                    rootType = null;
                db.folder().setFolderInheritedType(child.id, rootType);
                db.folder().setFolderParent(child.id, parent == null ? null : parent.id);
            }
        }

        Log.i("Delete local count=" + local.size());
        for (String name : local.keySet()) {
            EntityFolder folder = local.get(name);
            if (EntityFolder.INBOX.equals(folder.type)) {
                Log.w(account.host + " keep inbox");
                continue;
            }
            List<EntityFolder> childs = parentFolders.get(name);
            if (EntityFolder.USER.equals(folder.type) ||
                    childs == null || childs.size() == 0) {
                EntityLog.log(context, name + " delete");
                db.folder().deleteFolder(account.id, name);
                EntityLog.log(context, name + " deleted");
            } else
                Log.w(name + " keep type=" + folder.type);
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

    private static void onPurgeFolder(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, IMAPFolder ifolder) throws MessagingException {
        // Delete all messages from folder
        try {
            DB db = DB.getInstance(context);
            List<Long> busy = db.message().getBusyUids(folder.id, new Date().getTime());

            Message[] imessages = ifolder.getMessages();
            Log.i(folder.name + " purge=" + imessages.length + " busy=" + busy.size());

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            ifolder.fetch(imessages, fp);

            List<Message> idelete = new ArrayList<>();
            for (Message imessage : imessages)
                try {
                    long uid = ifolder.getUID(imessage);
                    if (!busy.contains(uid))
                        idelete.add(imessage);
                } catch (MessageRemovedException ex) {
                    Log.w(ex);
                }

            EntityLog.log(context, folder.name + " purging=" + idelete.size() + "/" + imessages.length);
            if (account.isYahooJp()) {
                for (Message imessage : new ArrayList<>(idelete))
                    try {
                        imessage.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessagingException mex) {
                        Log.w(mex);
                        idelete.remove(imessage);
                    }
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                int chunk_size = prefs.getInt("chunk_size", DEFAULT_CHUNK_SIZE);

                Flags flags = new Flags(Flags.Flag.DELETED);
                List<Message> iremove = new ArrayList<>();
                for (List<Message> list : Helper.chunkList(idelete, chunk_size))
                    try {
                        ifolder.setFlags(list.toArray(new Message[0]), flags, true);
                    } catch (MessagingException ex) {
                        Log.w(ex);
                        for (Message imessage : list)
                            try {
                                imessage.setFlag(Flags.Flag.DELETED, true);
                            } catch (MessagingException mex) {
                                Log.w(mex);
                                iremove.add(imessage);
                            }
                    }

                for (Message imessage : iremove)
                    idelete.remove(imessage);
            }
            Log.i(folder.name + " purge deleted");
            expunge(context, ifolder, idelete);
        } catch (Throwable ex) {
            Log.e(ex);
            throw ex;
        } finally {
            EntityOperation.sync(context, folder.id, false);
        }
    }

    private static void onExpungeFolder(Context context, JSONArray jargs, EntityFolder folder, IMAPFolder ifolder) throws MessagingException {
        Log.i(folder.name + " expunge");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean uid_expunge = prefs.getBoolean("uid_expunge", false);

        if (uid_expunge)
            uid_expunge = MessageHelper.hasCapability(ifolder, "UIDPLUS");

        if (uid_expunge) {
            DB db = DB.getInstance(context);

            List<Long> uids = db.message().getDeletedUids(folder.id);
            if (uids == null || uids.size() == 0)
                return;

            Log.i(ifolder.getName() + " expunging " + TextUtils.join(",", uids));
            uidExpunge(context, ifolder, uids);
            Log.i(ifolder.getName() + " expunged " + TextUtils.join(",", uids));
        } else
            ifolder.expunge();
    }

    private static void onPurgeFolder(Context context, EntityFolder folder) {
        // POP3
        int count = 0;
        int purged = 0;
        do {
            if (count > 0) {
                try {
                    Thread.sleep(YIELD_DURATION);
                } catch (InterruptedException ignored) {
                }
            }

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();
                count = db.message().deleteHiddenMessages(folder.id, 100);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            purged += count;
            Log.i(folder.name + " purge count=" + count + "/" + purged);
        } while (count > 0);
    }

    private static void onRule(Context context, JSONArray jargs, EntityMessage message) throws JSONException, MessagingException, IOException {
        // Deferred rule (download headers, body, etc)
        DB db = DB.getInstance(context);

        try {
            db.beginTransaction();

            long id = jargs.getLong(0);
            if (id < 0) {
                EntityLog.log(context, "Executing deferred daily rules for message=" + message.id);
                List<EntityRule> rules = db.rule().getEnabledRules(message.folder, true);
                EntityRule.run(context, rules, message, null, null);
            } else {
                EntityRule rule = db.rule().getRule(id);
                if (rule == null)
                    throw new IllegalArgumentException("Rule not found id=" + id);

                if (!message.content)
                    throw new IllegalArgumentException("Message without content id=" + rule.id + ":" + rule.name);

                rule.async = true;
                rule.execute(context, message, null);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void onDownload(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, EntityMessage message, IMAPStore istore, IMAPFolder ifolder, State state) throws MessagingException, IOException, JSONException {
        long uid = jargs.getLong(0);
        if (!Objects.equals(uid, message.uid))
            throw new IllegalArgumentException("Different uid=" + uid + "/" + message.uid);

        MimeMessage imessage = (MimeMessage) ifolder.getMessageByUID(uid);
        downloadMessage(context, account, folder, istore, ifolder, imessage, message.id, state, new SyncStats());
    }

    private static void onSubject(Context context, JSONArray jargs, EntityAccount account, EntityFolder folder, EntityMessage message, IMAPStore istore, IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        DB db = DB.getInstance(context);

        String subject = (jargs.isNull(0) ? null : jargs.getString(0));

        EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        String msgid = EntityMessage.generateMessageId();
        String ref = (TextUtils.isEmpty(message.references)
                ? message.msgid
                : message.references + " " + message.msgid);
        MimeMessage icopy = new MimeMessageEx((MimeMessage) imessage, msgid);
        icopy.setSubject(subject); // Update or delete subject
        icopy.addHeader("References", MessageHelper.limitReferences(ref));
        icopy.addHeader(MessageHelper.HEADER_MODIFIED_TIME, Long.toString(new Date().getTime()));

        ifolder.appendMessages(new Message[]{icopy});

        Long uid = findUid(context, account, ifolder, msgid, null);
        if (uid != null) {
            JSONArray fargs = new JSONArray();
            fargs.put(uid);
            onFetch(context, fargs, folder, istore, ifolder, state);
        }

        if (trash == null) {
            imessage.setFlag(Flags.Flag.DELETED, true);
            expunge(context, ifolder, Arrays.asList(imessage));
        } else
            EntityOperation.queue(context, message, EntityOperation.MOVE, trash.id);
    }

    private static void onSynchronizeMessages(
            Context context, JSONArray jargs,
            EntityAccount account, final EntityFolder folder,
            POP3Folder ifolder, POP3Store istore, State state) throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sync_quick_pop = prefs.getBoolean("sync_quick_pop", true);
        boolean notify_known = prefs.getBoolean("notify_known", false);
        boolean native_dkim = prefs.getBoolean("native_dkim", false);
        boolean strict_alignment = prefs.getBoolean("strict_alignment", false);
        boolean download_eml = prefs.getBoolean("download_eml", false);
        boolean download_plain = prefs.getBoolean("download_plain", false);
        boolean check_blocklist = prefs.getBoolean("check_blocklist", false);
        boolean use_blocklist_pop = prefs.getBoolean("use_blocklist_pop", false);
        boolean pro = ActivityBilling.isPro(context);

        boolean force = jargs.optBoolean(5, false);

        EntityLog.log(context, account.name + " POP sync type=" + folder.type +
                " quick=" + sync_quick_pop + " force=" + force +
                " connected=" + (ifolder != null));

        if (!EntityFolder.INBOX.equals(folder.type)) {
            folder.synchronize = false;
            db.folder().setFolderSynchronize(folder.id, folder.synchronize);
            db.folder().setFolderSyncState(folder.id, null);
            return;
        }

        List<EntityRule> rules = db.rule().getEnabledRules(folder.id, false);

        try {
            db.folder().setFolderSyncState(folder.id, "syncing");

            // Get capabilities
            Map<String, String> caps = istore.capabilities();
            boolean hasUidl = caps.containsKey("UIDL");
            String capabilities = TextUtils.join(" ", caps.values());
            EntityLog.log(context, EntityLog.Type.Protocol, account, capabilities);
            db.account().setAccountCapabilities(account.id, capabilities, false, false);

            if (hasUidl) {
                if (Boolean.FALSE.equals(account.capability_uidl)) {
                    hasUidl = false;
                    Log.w(account.host + " did not had UIDL before");
                }
            } else {
                account.capability_uidl = false;
                db.account().setAccountUidl(account.id, account.capability_uidl);
            }

            // Get messages
            Message[] imessages = ifolder.getMessages();

            List<TupleUidl> ids = db.message().getUidls(folder.id);
            int max = (account.max_messages == null
                    ? imessages.length
                    : Math.min(imessages.length, Math.abs(account.max_messages)));
            boolean reversed = (account.max_messages != null && account.max_messages < 0);

            boolean sync = true;
            if (!hasUidl && sync_quick_pop && !force &&
                    imessages.length > 0 && folder.last_sync_count != null &&
                    imessages.length == folder.last_sync_count) {
                // Check if last message known as new messages indicator
                MessageHelper helper = new MessageHelper((MimeMessage) imessages[reversed ? 0 : imessages.length - 1], context);
                String msgid = helper.getPOP3MessageID();
                if (msgid != null) {
                    int count = db.message().countMessageByMsgId(folder.id, msgid, true);
                    if (count == 1) {
                        Log.i(account.name + " POP having last msgid=" + msgid);
                        sync = false;
                    }
                }
            }

            // Index IDs
            int flagged = 0;
            Map<String, TupleUidl> uidlTuple = new HashMap<>();
            Map<String, TupleUidl> msgIdTuple = new HashMap<>();
            for (TupleUidl id : ids) {
                if (id.ui_flagged && !id.ui_hide)
                    flagged++;

                if (id.uidl != null) {
                    if (uidlTuple.containsKey(id.uidl))
                        Log.w(account.name + " POP duplicate uidl/msgid=" + id.uidl + "/" + id.msgid);
                    uidlTuple.put(id.uidl, id);
                }

                if (id.msgid != null) {
                    if (msgIdTuple.containsKey(id.msgid))
                        Log.w(account.name + " POP duplicate msgid/uidl=" + id.msgid + "/" + id.uidl);
                    msgIdTuple.put(id.msgid, id);
                }
            }

            max = Math.min(max + flagged, imessages.length);

            EntityLog.log(context, account.name + " POP" +
                    " device=" + ids.size() +
                    " server=" + imessages.length +
                    " flagged=" + flagged +
                    " max=" + max + "/" + account.max_messages +
                    " reversed=" + reversed +
                    " last=" + folder.last_sync_count +
                    " sync=" + sync +
                    " uidl=" + hasUidl);

            if (sync) {
                // Fetch UIDLs
                if (hasUidl) {
                    FetchProfile ifetch = new FetchProfile();
                    ifetch.add(UIDFolder.FetchProfileItem.UID); // This will fetch all UIDs
                    ifolder.fetch(imessages, ifetch);
                }

                long now = new Date().getTime();

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

                    for (TupleUidl uidl : known.values())
                        if (account.leave_on_device
                                ? uidl.ui_hide && (uidl.ui_busy == null || uidl.ui_busy < now)
                                : !uidl.ui_flagged) {
                            EntityLog.log(context, account.name + " POP purging" +
                                    " uidl=" + uidl.uidl + " hidden=" + uidl.ui_hide);
                            db.message().deleteMessage(uidl.id);
                        }
                } else {
                    Map<String, TupleUidl> known = new HashMap<>();
                    for (TupleUidl id : ids)
                        if (id.msgid != null)
                            known.put(id.msgid, id);

                    for (int i = imessages.length - max; i < imessages.length; i++) {
                        Message imessage = imessages[i];
                        MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);
                        String msgid = helper.getPOP3MessageID(); // expensive!
                        known.remove(msgid);
                    }

                    for (TupleUidl uidl : known.values())
                        if (account.leave_on_device
                                ? uidl.ui_hide && (uidl.ui_busy == null || uidl.ui_busy < now)
                                : !uidl.ui_flagged) {
                            EntityLog.log(context, account.name + " POP purging" +
                                    " msgid=" + uidl.msgid + " hidden=" + uidl.ui_hide);
                            db.message().deleteMessage(uidl.id);
                        }
                }

                boolean _new = true;
                for (int i = reversed ? 0 : imessages.length - 1; reversed ? i < max : i >= imessages.length - max; i += reversed ? 1 : -1) {
                    state.ensureRunning("Sync/POP3");

                    Message imessage = imessages[i];
                    try {
                        MessageHelper helper = new MessageHelper((MimeMessage) imessage, context);

                        String uidl;
                        String msgid;
                        if (hasUidl) {
                            uidl = ifolder.getUID(imessage);
                            if (TextUtils.isEmpty(uidl)) {
                                EntityLog.log(context, account.name + " POP no uidl");
                                continue;
                            }

                            TupleUidl tuple = uidlTuple.get(uidl);
                            msgid = (tuple == null ? null : tuple.msgid);
                            if (msgid == null) {
                                msgid = helper.getMessageID();
                                if (TextUtils.isEmpty(msgid))
                                    msgid = uidl;
                            }
                        } else {
                            uidl = null;
                            msgid = helper.getPOP3MessageID();
                        }

                        if (TextUtils.isEmpty(msgid)) {
                            EntityLog.log(context, account.name + " POP no msgid uidl=" + uidl);
                            continue;
                        }

                        TupleUidl tuple = (hasUidl ? uidlTuple.get(uidl) : msgIdTuple.get(msgid));
                        if (tuple != null) {
                            if (account.max_messages != null)
                                _new = false;

                            Log.i(account.name + " POP having index=" + i + " " +
                                    msgid + "=" + msgIdTuple.containsKey(msgid) + "/" +
                                    uidl + "=" + uidlTuple.containsKey(uidl));

                            // Restore orphan POP3 moves
                            if (tuple.ui_hide &&
                                    tuple.ui_busy != null &&
                                    tuple.ui_busy < new Date().getTime())
                                db.message().setMessageUiHide(tuple.id, false);

                            if (download_eml)
                                try {
                                    File raw = EntityMessage.getRawFile(context, tuple.id);
                                    if (raw.exists())
                                        continue;

                                    Log.i(account.name + " POP raw " + msgid + "/" + uidl);
                                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(raw))) {
                                        imessage.writeTo(os);
                                    }

                                    db.message().setMessageRaw(tuple.id, true);
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }

                            continue;
                        }

                        Long sent = helper.getSent();
                        long received = helper.getPOP3Received();

                        boolean seen = (received <= account.created);
                        EntityLog.log(context, account.name + " POP index=" + i + " sync=" + uidl + "/" + msgid +
                                " new=" + _new + " seen=" + seen);

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
                        message.thread = helper.getThreadId(context, account.id, folder.id, 0, received);
                        message.priority = helper.getPriority();
                        message.sensitivity = helper.getSensitivity();
                        message.auto_submitted = helper.getAutoSubmitted();
                        message.receipt_request = helper.getReceiptRequested();
                        message.receipt_to = helper.getReceiptTo();
                        message.bimi_selector = helper.getBimiSelector();
                        message.tls = helper.getTLS();
                        message.dkim = MessageHelper.getAuthentication("dkim", authentication);
                        message.spf = MessageHelper.getAuthentication("spf", authentication);
                        if (message.spf == null)
                            message.spf = helper.getSPF();
                        message.dmarc = MessageHelper.getAuthentication("dmarc", authentication);
                        message.auth = MessageHelper.getAuthentication("auth", authentication);
                        message.smtp_from = helper.getMailFrom(authentication);
                        message.return_path = helper.getReturnPath();
                        message.submitter = helper.getSubmitter();
                        message.from = helper.getFrom();
                        message.to = helper.getTo();
                        message.cc = helper.getCc();
                        message.bcc = helper.getBcc();
                        message.reply = helper.getReply();
                        message.list_post = helper.getListPost();
                        message.unsubscribe = helper.getListUnsubscribe();
                        message.headers = helper.getHeaders();
                        message.infrastructure = helper.getInfrastructure();
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
                        message.ui_ignored = !_new;
                        message.ui_browsed = false;

                        if (message.deliveredto != null)
                            try {
                                Address deliveredto = new InternetAddress(message.deliveredto);
                                if (MessageHelper.equalEmail(new Address[]{deliveredto}, message.to))
                                    message.deliveredto = null;
                            } catch (AddressException ex) {
                                Log.w(ex);
                            }

                        if (MessageHelper.equalEmail(message.submitter, message.from))
                            message.submitter = null;

                        EntityIdentity identity = matchIdentity(context, folder, message);
                        message.identity = (identity == null ? null : identity.id);
                        boolean fromSelf = message.fromSelf(identity);

                        if (native_dkim &&
                                !fromSelf &&
                                !EntityFolder.isOutgoing(folder.type) &&
                                !BuildConfig.PLAY_STORE_RELEASE) {
                            List<String> signers = helper.verifyDKIM(context);
                            message.dkim = !signers.isEmpty();
                            message.signedby = (signers.isEmpty() ? null : TextUtils.join(",", signers));
                            message.dmarc = helper.isAligned(context, signers,
                                    message.return_path, message.smtp_from, message.from, helper.getSPF());
                        }

                        if (message.size == null && message.total != null)
                            message.size = message.total;

                        message.sender = MessageHelper.getSortKey(message.from);
                        Uri lookupUri = ContactInfo.getLookupUri(message.from);
                        message.avatar = (lookupUri == null ? null : lookupUri.toString());
                        if (message.avatar == null && notify_known && pro)
                            message.ui_ignored = true;

                        message.from_domain = (message.checkFromDomain(context) == null);

                        // No reply_domain
                        // No MX check

                        if (check_blocklist && use_blocklist_pop) {
                            message.blocklist = DnsBlockList.isJunk(context,
                                    imessage.getHeader("Received"));

                            if (message.blocklist == null || !message.blocklist) {
                                List<Address> senders = new ArrayList<>();
                                if (message.reply != null)
                                    senders.addAll(Arrays.asList(message.reply));
                                if (message.from != null)
                                    senders.addAll(Arrays.asList(message.from));
                                message.blocklist = DnsBlockList.isJunk(context, senders);
                            }

                            if (Boolean.TRUE.equals(message.blocklist)) {
                                EntityLog.log(context, account.name + " POP blocklist=" +
                                        MessageHelper.formatAddresses(message.from));
                                message.ui_hide = true;
                            }
                        }

                        if (message.from != null) {
                            EntityContact badboy = null;
                            for (Address from : message.from) {
                                String email = ((InternetAddress) from).getAddress();
                                if (TextUtils.isEmpty(email))
                                    continue;

                                badboy = db.contact().getContact(message.account, EntityContact.TYPE_JUNK, email);
                                if (badboy != null)
                                    break;
                            }

                            if (badboy != null) {
                                badboy.times_contacted++;
                                badboy.last_contacted = new Date().getTime();
                                db.contact().updateContact(badboy);

                                EntityLog.log(context, account.name + " POP blocked=" +
                                        MessageHelper.formatAddresses(message.from));

                                message.ui_hide = true;
                            }
                        }

                        boolean needsHeaders = EntityRule.needsHeaders(message, rules);
                        List<Header> headers = (needsHeaders ? helper.getAllHeaders() : null);
                        String body = parts.getHtml(context, download_plain);

                        try {
                            db.beginTransaction();

                            message.id = db.message().insertMessage(message);
                            EntityLog.log(context, account.name + " POP added id=" + message.id +
                                    " uidl/msgid=" + message.uidl + "/" + message.msgid);

                            int sequence = 1;
                            for (EntityAttachment attachment : parts.getAttachments()) {
                                Log.i(account.name + " POP attachment seq=" + sequence +
                                        " name=" + attachment.name + " type=" + attachment.type +
                                        " cid=" + attachment.cid + " pgp=" + attachment.encryption +
                                        " size=" + attachment.size);
                                attachment.message = message.id;
                                attachment.sequence = sequence++;
                                attachment.id = db.attachment().insertAttachment(attachment);
                            }

                            runRules(context, headers, body, account, folder, message, rules);
                            reportNewMessage(context, account, folder, message);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        File file = message.getFile(context);
                        Helper.writeText(file, body);
                        String text = HtmlHelper.getFullText(context, body);
                        message.preview = HtmlHelper.getPreview(text);
                        message.language = HtmlHelper.getLanguage(context, message.subject, text);
                        db.message().setMessageContent(message.id,
                                true,
                                message.language,
                                parts.isPlainOnly(download_plain),
                                message.preview,
                                parts.getWarnings(message.warning));

                        try {
                            for (EntityAttachment attachment : parts.getAttachments())
                                if (attachment.subsequence == null)
                                    parts.downloadAttachment(context, attachment, folder);
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }

                        if (download_eml)
                            try {
                                Log.i(account.name + " POP raw " + msgid + "/" + uidl);

                                File raw = message.getRawFile(context);
                                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(raw))) {
                                    imessage.writeTo(os);
                                }

                                message.raw = true;
                                db.message().setMessageRaw(message.id, message.raw);
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }

                        if (!account.leave_on_server && account.client_delete)
                            imessage.setFlag(Flags.Flag.DELETED, true);

                        EntityContact.received(context, account, folder, message);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        Log.e(ex);
                        db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                        //if (!(ex instanceof MessagingException))
                        throw ex;

                        /*
                            javax.mail.MessagingException: error loading POP3 headers;
                              nested exception is:
                                java.io.IOException: Unexpected response: ...
                                at com.sun.mail.pop3.POP3Message.loadHeaders(SourceFile:15)
                                at com.sun.mail.pop3.POP3Message.getHeader(SourceFile:5)
                                at eu.faircode.email.MessageHelper.getMessageID(SourceFile:2)
                                at eu.faircode.email.Core.onSynchronizeMessages(SourceFile:78)
                                at eu.faircode.email.Core.processOperations(SourceFile:89)
                                at eu.faircode.email.ServiceSynchronize$19$1$2.run(SourceFile:51)
                                at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:462)
                                at java.util.concurrent.FutureTask.run(FutureTask.java:266)
                                at eu.faircode.email.Helper$PriorityFuture.run(SourceFile:1)
                                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
                                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
                                at java.lang.Thread.run(Thread.java:923)
                            Caused by: java.io.IOException: Unexpected response: Bd04v8G0fQOraFZwxNDLapHDdRM0xj8oW+4nG4FVG05/WuE/sW8i3xxzx3unQBWtyhU3KDqQSDzz
                                at com.sun.mail.pop3.Protocol.readResponse(SourceFile:12)
                                at com.sun.mail.pop3.Protocol.multilineCommand(SourceFile:3)
                                at com.sun.mail.pop3.Protocol.top(SourceFile:1)
                                at com.sun.mail.pop3.POP3Message.loadHeaders(SourceFile:5)
                         */
                    } finally {
                        ((POP3Message) imessage).invalidate(true);
                    }
                }
            }

            if (account.max_messages != null && !account.leave_on_device) {
                int hidden = db.message().setMessagesUiHide(folder.id,
                        Math.abs(account.max_messages) + flagged);
                int deleted = db.message().deleteMessagesKeep(folder.id,
                        Math.abs(account.max_messages) + flagged + POP3_KEEP_EXTRA);
                EntityLog.log(context, account.name + " POP" +
                        " cleanup max=" + account.max_messages +
                        " hidden=" + hidden +
                        " deleted=" + deleted);
            }

            folder.last_sync_count = imessages.length;
            db.folder().setFolderLastSyncCount(folder.id, folder.last_sync_count);
            db.folder().setFolderLastSync(folder.id, new Date().getTime());
            EntityLog.log(context, account.name + " POP done");
        } finally {
            db.folder().setFolderSyncState(folder.id, null);
        }
    }

    private static void onSynchronizeMessages(
            Context context, JSONArray jargs,
            EntityAccount account, final EntityFolder folder,
            IMAPStore istore, final IMAPFolder ifolder, State state)
            throws JSONException, MessagingException, IOException {
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
            boolean sync_quick_imap = prefs.getBoolean("sync_quick_imap", false);
            boolean sync_nodate = prefs.getBoolean("sync_nodate", false);
            boolean sync_unseen = prefs.getBoolean("sync_unseen", false);
            boolean sync_flagged = prefs.getBoolean("sync_flagged", false);
            boolean sync_kept = prefs.getBoolean("sync_kept", true);
            boolean delete_unseen = prefs.getBoolean("delete_unseen", true);
            boolean use_modseq = prefs.getBoolean("use_modseq", true);
            boolean perform_expunge = prefs.getBoolean("perform_expunge", true);
            boolean log = prefs.getBoolean("protocol", false);

            if (account.isYahoo() || account.isAol())
                sync_nodate = false;

            if (account.isZoho()) {
                sync_unseen = false;
                sync_flagged = false;
            }

            Log.i(folder.name + " start sync after=" + sync_days + "/" + keep_days +
                    " quick=" + sync_quick_imap + " force=" + force +
                    " sync unseen=" + sync_unseen + " flagged=" + sync_flagged +
                    " delete unseen=" + delete_unseen + " kept=" + sync_kept);

            if (folder.local) {
                folder.synchronize = false;
                db.folder().setFolderSynchronize(folder.id, folder.synchronize);
                db.folder().setFolderSyncState(folder.id, null);
                return;
            }

            db.folder().setFolderSyncState(folder.id, "syncing");

            Flags flags = ifolder.getPermanentFlags();

            try {
                List<String> f = new ArrayList<>();
                if (flags != null) {
                    if (flags.contains(Flags.Flag.ANSWERED))
                        f.add("\\Answered");
                    if (flags.contains(Flags.Flag.DELETED))
                        f.add("\\Deleted");
                    if (flags.contains(Flags.Flag.DRAFT))
                        f.add("\\Draft");
                    if (flags.contains(Flags.Flag.FLAGGED))
                        f.add("\\Flagged");
                    if (flags.contains(Flags.Flag.RECENT))
                        f.add("\\Recent");
                    if (flags.contains(Flags.Flag.SEEN))
                        f.add("\\Seen");
                    if (flags.contains(Flags.Flag.USER))
                        f.add("\\*");
                }
                db.folder().setFolderFlags(folder.id, DB.Converters.fromStringArray(f.toArray(new String[0])));
            } catch (Throwable ex) {
                Log.e(ex);
            }

            String[] userFlags = flags.getUserFlags();
            if (userFlags != null && userFlags.length > 0) {
                List<String> keywords = new ArrayList<>(Arrays.asList(userFlags));
                Collections.sort(keywords);
                userFlags = keywords.toArray(new String[0]);
                if (!Arrays.equals(folder.keywords, userFlags)) {
                    Log.i(folder.name + " updating flags=" + TextUtils.join(",", userFlags));
                    folder.keywords = userFlags;
                    db.folder().setFolderKeywords(folder.id, DB.Converters.fromStringArray(userFlags));
                }
            }

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

            // https://tools.ietf.org/html/rfc4551
            // https://wiki.mozilla.org/Thunderbird:IMAP_RFC_4551_Implementation
            Long modseq = null;
            boolean modified = true;
            if (use_modseq)
                try {
                    if (MessageHelper.hasCapability(ifolder, "CONDSTORE")) {
                        Status status = (Status) ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                            @Override
                            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                return protocol.status(ifolder.getFullName(), new String[]{"HIGHESTMODSEQ"});
                            }
                        });
                        modseq = status.highestmodseq;
                        if (modseq < 0)
                            modseq = null;
                        modified = (force || initialize != 0 ||
                                folder.modseq == null || !folder.modseq.equals(modseq));
                        EntityLog.log(context,
                                BuildConfig.DEBUG ? EntityLog.Type.Debug4 : EntityLog.Type.General,
                                folder.name + " modseq=" + modseq + "/" + folder.modseq +
                                        " force=" + force + " init=" + (initialize != 0) + " modified=" + modified);
                    }
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

            Calendar cal_keep_unread = Calendar.getInstance();
            cal_keep_unread.add(Calendar.DAY_OF_MONTH,
                    delete_unseen ? -keep_days : -Math.max(keep_days * 6, EntityFolder.DEFAULT_KEEP * 6));
            cal_keep_unread.set(Calendar.HOUR_OF_DAY, 0);
            cal_keep_unread.set(Calendar.MINUTE, 0);
            cal_keep_unread.set(Calendar.SECOND, 0);
            cal_keep_unread.set(Calendar.MILLISECOND, 0);

            long sync_time = cal_sync.getTimeInMillis();
            if (sync_time < 0)
                sync_time = 0;

            long keep_time = cal_keep.getTimeInMillis();
            if (keep_time < 0)
                keep_time = 0;

            long keep_unread_time = cal_keep_unread.getTimeInMillis();
            if (keep_unread_time < 0)
                keep_unread_time = 0;

            Log.i(folder.name + " sync=" + new Date(sync_time) +
                    " keep=" + new Date(keep_time) +
                    " unread=" + new Date(keep_unread_time));

            // Delete old local messages
            long delete_time = new Date().getTime() - 3600 * 1000L;
            if (auto_delete) {
                List<Long> tbds = db.message().getMessagesBefore(folder.id, delete_time, keep_time,
                        !delete_unseen || sync_unseen ? 0 : keep_unread_time);
                Log.i(folder.name + " local tbd=" + tbds.size());
                EntityFolder trash = db.folder().getFolderByType(folder.account, EntityFolder.TRASH);
                for (Long tbd : tbds) {
                    EntityMessage message = db.message().getMessage(tbd);
                    if (message != null && trash != null)
                        if (EntityFolder.TRASH.equals(folder.type) ||
                                EntityFolder.JUNK.equals(folder.type))
                            EntityOperation.queue(context, message, EntityOperation.DELETE);
                        else
                            EntityOperation.queue(context, message, EntityOperation.MOVE, trash.id);
                }
            } else {
                int old = db.message().deleteMessagesBefore(folder.id, delete_time, keep_time,
                        !delete_unseen || sync_unseen ? 0 : keep_unread_time);
                Log.i(folder.name + " local old=" + old);
            }

            Message[] imessages;
            long search;
            Long[] ids;
            if (modified || !sync_quick_imap || force) {
                // Get list of local uids
                final List<Long> uids = db.message().getUids(folder.id, sync_kept || force ? null : sync_time);
                Log.i(folder.name + " local count=" + uids.size());

                if (BuildConfig.DEBUG || log)
                    try {
                        Status status = (Status) ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                            @Override
                            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                return protocol.status(ifolder.getFullName(), null);
                            }
                        });
                        EntityLog.log(context, EntityLog.Type.Protocol, folder.name + " status" +
                                " total=" + status.total + " unseen=" + status.unseen);
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }

                // Reduce list of local uids
                SearchTerm dateTerm = account.use_date
                        ? new SentDateTerm(ComparisonTerm.GE, new Date(sync_time))
                        : new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time));

                SearchTerm searchTerm = dateTerm;
                if (sync_nodate && !account.isOutlook())
                    searchTerm = new OrTerm(searchTerm, new ReceivedDateTerm(ComparisonTerm.LT, new Date(365 * 24 * 3600 * 1000L)));
                if (sync_unseen && flags.contains(Flags.Flag.SEEN))
                    searchTerm = new OrTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                if (sync_flagged && flags.contains(Flags.Flag.FLAGGED))
                    searchTerm = new OrTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

                search = SystemClock.elapsedRealtime();
                if (sync_time == 0)
                    imessages = ifolder.getMessages();
                else
                    try {
                        imessages = ifolder.search(searchTerm);
                    } catch (MessagingException ex) {
                        Log.w(ex);
                        // Fallback to date only search
                        // BAD Could not parse command
                        imessages = ifolder.search(dateTerm);
                    }
                if (imessages == null)
                    imessages = new Message[0];

                for (Message imessage : imessages)
                    if (imessage instanceof IMAPMessage)
                        ((IMAPMessage) imessage).invalidateHeaders();

                stats.search_ms = (SystemClock.elapsedRealtime() - search);
                Log.i(folder.name + " remote count=" + imessages.length + " search=" + stats.search_ms + " ms");

                ids = new Long[imessages.length];

                if (!modified) {
                    Log.i(folder.name + " quick check");
                    long fetch = SystemClock.elapsedRealtime();

                    FetchProfile fp = new FetchProfile();
                    fp.add(UIDFolder.FetchProfileItem.UID);
                    ifolder.fetch(imessages, fp);

                    stats.flags = imessages.length;
                    stats.flags_ms = (SystemClock.elapsedRealtime() - fetch);
                    Log.i(folder.name + " remote fetched=" + stats.flags_ms + " ms");

                    for (int i = 0; i < imessages.length; i++) {
                        state.ensureRunning("Sync/IMAP/check");

                        try {
                            long uid = ifolder.getUID(imessages[i]);
                            EntityMessage message = db.message().getMessageByUid(folder.id, uid);
                            ids[i] = (message == null ? null : message.id);
                            if (message == null || message.ui_hide) {
                                Log.i(folder.name + " missing uid=" + uid);
                                modified = true;
                                break;
                            } else
                                uids.remove(uid);
                        } catch (Throwable ex) {
                            Log.w(ex);
                            modified = true;
                        }
                    }

                    if (uids.size() > 0) {
                        Log.i(folder.name + " remaining=" + uids.size());
                        modified = true;
                    }

                    EntityLog.log(context, folder.name + " modified=" + modified);
                }

                if (modified) {
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

                    List<Message> deleted = new ArrayList<>();
                    for (int i = 0; i < imessages.length; i++) {
                        state.ensureRunning("Sync/IMAP/delete");
                        if (!ifolder.isOpen())
                            throw new FolderClosedException(ifolder);

                        boolean ok = false;
                        try {
                            if (perform_expunge && imessages[i].isSet(Flags.Flag.DELETED))
                                deleted.add(imessages[i]);
                            else
                                uids.remove(ifolder.getUID(imessages[i]));
                            ok = true;
                        } catch (MessageRemovedException ex) {
                            Log.w(folder.name, ex);
                        } catch (FolderClosedException ex) {
                            throw ex;
                        } catch (Throwable ex) {
                            Log.e(folder.name, ex);
                            EntityLog.log(context, folder.name + " expunge " + Log.formatThrowable(ex, false));
                            db.folder().setFolderError(folder.id, Log.formatThrowable(ex));
                        } finally {
                            if (!ok)
                                modseq = null;
                        }
                    }

                    expunge(context, ifolder, deleted);

                    if (uids.size() > 0) {
                        // This is done outside of JavaMail to prevent changed notifications
                        if (!ifolder.isOpen())
                            throw new FolderClosedException(ifolder, "UID FETCH");

                        long getuid = SystemClock.elapsedRealtime();
                        MessagingException ex = (MessagingException) ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                            @Override
                            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                protocol.select(folder.name);

                                // Yahoo range size: 2000
                                // https://help.yahoo.com/kb/download-email-yahoo-mail-third-party-sln28681.html
                                int range_size = prefs.getInt("range_size", DEFAULT_RANGE_SIZE);

                                // Build ranges
                                List<Pair<Long, Long>> ranges = new ArrayList<>();
                                long first = -1;
                                long last = -1;
                                for (long uid : uids)
                                    if (first < 0)
                                        first = uid;
                                    else if ((last < 0 ? first : last) + 1 == uid && (uid - first + 1 <= range_size))
                                        last = uid;
                                    else {
                                        ranges.add(new Pair<>(first, last < 0 ? first : last));
                                        first = uid;
                                        last = -1;
                                    }
                                if (first > 0)
                                    ranges.add(new Pair<>(first, last < 0 ? first : last));

                                // https://datatracker.ietf.org/doc/html/rfc2683#section-3.2.1.5
                                int chunk_size = prefs.getInt("chunk_size", DEFAULT_CHUNK_SIZE);
                                if (chunk_size < 200 &&
                                        (account.isGmail() || account.isOutlook()))
                                    chunk_size = 200;

                                List<List<Pair<Long, Long>>> chunks = new ArrayList<>();

                                int s = 0;
                                List<Pair<Long, Long>> r = new ArrayList<>();
                                for (Pair<Long, Long> range : ranges) {
                                    long n = range.second - range.first + 1;
                                    if (s + n > range_size) {
                                        chunks.addAll(Helper.chunkList(r, chunk_size));
                                        s = 0;
                                        r.clear();
                                    }
                                    s += n;
                                    r.add(range);
                                }
                                chunks.addAll(Helper.chunkList(r, chunk_size));

                                Log.i(folder.name + " executing uid fetch count=" + uids.size() +
                                        " ranges=" + ranges.size() + " chunks=" + chunks.size() +
                                        " range_size=" + range_size + " chunk_size=" + chunk_size);
                                for (int c = 0; c < chunks.size(); c++) {
                                    List<Pair<Long, Long>> chunk = chunks.get(c);

                                    StringBuilder sb = new StringBuilder();
                                    for (Pair<Long, Long> range : chunk) {
                                        if (sb.length() > 0)
                                            sb.append(',');
                                        if (range.first.equals(range.second))
                                            sb.append(range.first);
                                        else
                                            sb.append(range.first).append(':').append(range.second);
                                    }
                                    Log.i(folder.name + " chunk #" + c + " " + sb);

                                    String command = "UID FETCH " + sb + " (UID FLAGS)";
                                    Response[] responses = protocol.command(command, null);

                                    if (responses.length > 0 && responses[responses.length - 1].isOK()) {
                                        for (Response response : responses)
                                            if (response instanceof FetchResponse) {
                                                FetchResponse fr = (FetchResponse) response;
                                                UID uid = fr.getItem(UID.class);
                                                FLAGS flags = fr.getItem(FLAGS.class);
                                                if (uid == null || flags == null)
                                                    continue;
                                                if (perform_expunge && flags.contains(Flags.Flag.DELETED))
                                                    continue;

                                                uids.remove(uid.uid);

                                                if (force) {
                                                    EntityMessage message = db.message().getMessageByUid(folder.id, uid.uid);
                                                    if (message != null) {
                                                        boolean update = false;
                                                        boolean recent = flags.contains(Flags.Flag.RECENT);
                                                        boolean seen = flags.contains(Flags.Flag.SEEN);
                                                        boolean answered = flags.contains(Flags.Flag.ANSWERED);
                                                        boolean flagged = flags.contains(Flags.Flag.FLAGGED);
                                                        boolean deleted = flags.contains(Flags.Flag.DELETED);
                                                        if (message.recent != recent) {
                                                            update = true;
                                                            message.recent = recent;
                                                            Log.i("UID fetch recent=" + recent);
                                                        }
                                                        if (message.seen != seen) {
                                                            update = true;
                                                            message.seen = seen;
                                                            message.ui_seen = seen;
                                                            Log.i("UID fetch seen=" + seen);
                                                        }
                                                        if (message.answered != answered) {
                                                            update = true;
                                                            message.answered = answered;
                                                            message.ui_answered = answered;
                                                            Log.i("UID fetch answered=" + answered);
                                                        }
                                                        if (message.flagged != flagged) {
                                                            update = true;
                                                            message.flagged = flagged;
                                                            message.ui_flagged = flagged;
                                                            Log.i("UID fetch flagged=" + flagged);
                                                        }
                                                        if (message.deleted != deleted) {
                                                            update = true;
                                                            message.deleted = deleted;
                                                            message.ui_deleted = deleted;
                                                            message.ui_ignored = deleted;
                                                            Log.i("UID fetch deleted=" + deleted);
                                                        }

                                                        if (update)
                                                            db.message().updateMessage(message);
                                                    }
                                                }
                                            }
                                    } else {
                                        for (Response response : responses)
                                            if (response.isBYE())
                                                return new MessagingException("UID FETCH", new IOException(response.toString()));
                                            else if (response.isNO()) {
                                                Log.e("UID FETCH " + response);
                                                throw new CommandFailedException(response);
                                            } else if (response.isBAD()) {
                                                Log.e("UID FETCH " + response);
                                                // BAD Error in IMAP command UID FETCH: Too long argument (n.nnn + n.nnn + n.nnn secs).
                                                if (response.toString().contains("Too long argument")) {
                                                    chunk_size = chunk_size / 2;
                                                    if (chunk_size > 0)
                                                        prefs.edit().putInt("chunk_size", chunk_size).apply();
                                                }
                                                throw new BadCommandException(response);
                                            }
                                        throw new ProtocolException("UID FETCH failed");
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

                    List<EntityRule> rules = db.rule().getEnabledRules(folder.id, false);

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
                    DutyCycle dc = new DutyCycle(account.name + " sync");
                    Log.i(folder.name + " add=" + imessages.length);
                    for (int i = imessages.length - 1; i >= 0; i -= SYNC_BATCH_SIZE) {
                        state.ensureRunning("Sync/IMAP/sync/fetch");

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
                        crumb.put("partial", Boolean.toString(account.partial_fetch));
                        Log.breadcrumb("sync", crumb);
                        Log.i("Sync " + from + ".." + i + " free=" + free);

                        for (int j = isub.length - 1; j >= 0; j--) {
                            state.ensureRunning("Sync/IMAP/sync");

                            boolean ok = false;
                            try {
                                dc.start();

                                // Some providers erroneously return old messages
                                if (full.contains(isub[j]))
                                    try {
                                        Date received = isub[j].getReceivedDate();
                                        if (received == null || received.getTime() == 0)
                                            received = isub[j].getSentDate();
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
                                if (message != null)
                                    ok = true;
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
                                isub[j] = null;
                                if (!ok)
                                    modseq = null;
                                dc.stop(state.getForeground(), from == 0 && j == 0);
                            }
                        }
                    }
                }

                // Delete not synchronized messages without uid
                if (!EntityFolder.isOutgoing(folder.type)) {
                    int orphans = db.message().deleteOrphans(folder.id, new Date().getTime());
                    Log.i(folder.name + " deleted orphans=" + orphans);
                }
            } else {
                List<Long> _ids = new ArrayList<>();
                List<Long> _uids = new ArrayList<>();

                if (download && initialize == 0) {
                    List<EntityMessage> messages = db.message().getMessagesWithoutContent(
                            folder.id, sync_kept || force ? null : sync_time);
                    if (messages != null) {
                        Log.i(folder.name + " needs content=" + messages.size());
                        for (EntityMessage message : messages) {
                            _ids.add(message.id);
                            _uids.add(message.uid);
                        }
                    }
                }

                // This will result in message changed events
                imessages = ifolder.getMessagesByUID(Helper.toLongArray(_uids));
                ids = _ids.toArray(new Long[0]);

                search = SystemClock.elapsedRealtime();
            }

            // Update modseq
            folder.modseq = modseq;
            EntityLog.log(context,
                    BuildConfig.DEBUG ? EntityLog.Type.Debug4 : EntityLog.Type.General,
                    folder.name + " set modseq=" + modseq);
            db.folder().setFolderModSeq(folder.id, folder.modseq);

            // Update stats
            int count = MessageHelper.getMessageCount(ifolder);
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);
            account.last_connected = new Date().getTime();
            db.account().setAccountConnected(account.id, account.last_connected);

            if (download && initialize == 0) {
                db.folder().setFolderSyncState(folder.id, "downloading");

                // Download messages/attachments
                DutyCycle dc = new DutyCycle(account.name + " download");
                Log.i(folder.name + " download=" + imessages.length);
                for (int i = imessages.length - 1; i >= 0; i -= DOWNLOAD_BATCH_SIZE) {
                    state.ensureRunning("Sync/IMAP/download/fetch");

                    int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);
                    Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                    Arrays.fill(imessages, from, i + 1, null);
                    // Fetch on demand

                    int free = Log.getFreeMemMb();
                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("account", account.id + ":" + account.protocol);
                    crumb.put("folder", folder.id + ":" + folder.type);
                    crumb.put("start", Integer.toString(from));
                    crumb.put("end", Integer.toString(i));
                    crumb.put("partial", Boolean.toString(account.partial_fetch));
                    Log.breadcrumb("download", crumb);
                    Log.i("Download " + from + ".." + i + " free=" + free);

                    for (int j = isub.length - 1; j >= 0; j--) {
                        state.ensureRunning("Sync/IMAP/download");

                        try {
                            dc.start();
                            if (ids[from + j] != null)
                                downloadMessage(
                                        context,
                                        account, folder,
                                        istore, ifolder,
                                        (MimeMessage) isub[j], ids[from + j],
                                        state, stats);
                        } catch (FolderClosedException ex) {
                            throw ex;
                        } catch (Throwable ex) {
                            Log.e(folder.name, ex);
                        } finally {
                            // Free memory
                            isub[j] = null;
                            dc.stop(state.getForeground(), from == 0 && j == 0);
                        }
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
            //db.folder().setFolderError(folder.id, null);

            stats.total = (SystemClock.elapsedRealtime() - search);

            EntityLog.log(context, EntityLog.Type.Statistics,
                    account.name + "/" + folder.name + " sync stats " + stats);
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
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean download_headers = prefs.getBoolean("download_headers", false);
        boolean download_plain = prefs.getBoolean("download_plain", false);
        boolean notify_known = prefs.getBoolean("notify_known", false);
        boolean native_dkim = prefs.getBoolean("native_dkim", false);
        boolean strict_alignment = prefs.getBoolean("strict_alignment", false);
        boolean experiments = prefs.getBoolean("experiments", false);
        boolean mdn = prefs.getBoolean("mdn", experiments);
        boolean pro = ActivityBilling.isPro(context);

        long uid = ifolder.getUID(imessage);
        if (uid < 0) {
            Log.w(folder.name + " invalid uid=" + uid);
            throw new MessageRemovedException("uid");
        }

        if (imessage.isExpunged()) {
            Log.w(folder.name + " expunged uid=" + uid);
            throw new MessageRemovedException("Expunged");
        }

        if (imessage.isSet(Flags.Flag.DELETED)) {
            Log.w(folder.name + " deleted uid=" + uid);
            if (expunge(context, ifolder, Arrays.asList(imessage)))
                throw new MessageRemovedException("Deleted");
        }

        MessageHelper helper = new MessageHelper(imessage, context);
        boolean recent = helper.getRecent();
        boolean seen = helper.getSeen();
        boolean answered = helper.getAnswered();
        boolean flagged = helper.getFlagged();
        boolean deleted = helper.getDeleted();
        String flags = helper.getFlags();
        String[] keywords = helper.getKeywords();
        String[] labels = helper.getLabels();
        boolean update = false;
        boolean process = false;
        boolean syncSimilar = false;

        // Find message by uid (fast, no headers required)
        EntityMessage message = db.message().getMessageByUid(folder.id, uid);

        // Find message by Message-ID (slow, headers required)
        // - messages in inbox have same id as message sent to self
        // - messages in archive have same id as original
        boolean have = false;
        Integer color = null;
        String notes = null;
        Integer notes_color = null;
        if (message == null) {
            String msgid = helper.getMessageID();
            Log.i(folder.name + " searching for " + msgid);
            List<EntityMessage> dups = db.message().getMessagesByMsgId(folder.account, msgid);
            if (dups.size() == 0 &&
                    account.isOutlook() &&
                    EntityFolder.SENT.equals(folder.type)) {
                String originalId = imessage.getHeader(MessageHelper.HEADER_MICROSOFT_ORIGINAL_MESSAGE_ID, null);
                if (originalId != null) {
                    dups = db.message().getMessagesByMsgId(folder.account, originalId);
                    EntityLog.log(context, folder.name + " found with original ID" +
                            " msgid=" + msgid + " count=" + dups.size());
                }
            }
            for (EntityMessage dup : dups) {
                EntityFolder dfolder = db.folder().getFolder(dup.folder);
                Log.i(folder.name + " found as id=" + dup.id + "/" + dup.uid +
                        " folder=" + dfolder.type + ":" + dup.folder + "/" + folder.type + ":" + folder.id +
                        " msgid=" + dup.msgid + " thread=" + dup.thread);

                if (!EntityFolder.JUNK.equals(dfolder.type))
                    have = true;

                if (dup.folder.equals(folder.id)) {
                    String thread = helper.getThreadId(context, account.id, folder.id, uid, dup.received);
                    Log.i(folder.name + " found as id=" + dup.id +
                            " uid=" + dup.uid + "/" + uid +
                            " msgid=" + msgid + " thread=" + thread);

                    if (dup.uid == null) {
                        Log.i(folder.name + " set uid=" + uid);
                        dup.uid = uid;
                        if (dup.thread == null)
                            dup.thread = thread;

                        if (EntityFolder.SENT.equals(folder.type) &&
                                (folder.auto_add == null || !folder.auto_add)) {
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
                    } else if (msgid != null && EntityFolder.DRAFTS.equals(folder.type)) {
                        try {
                            if (dup.uid < uid) {
                                MimeMessage existing = (MimeMessage) ifolder.getMessageByUID(dup.uid);
                                if (existing != null &&
                                        msgid.equals(existing.getHeader(MessageHelper.HEADER_CORRELATION_ID, null))) {
                                    Log.w(folder.name + " late draft" +
                                            " host=" + account.host + " uid=" + dup.uid + "<" + uid);
                                    existing.setFlag(Flags.Flag.DELETED, true);
                                    expunge(context, ifolder, Arrays.asList(existing));
                                    db.message().setMessageUiHide(dup.id, true);
                                }
                            } else if (dup.uid > uid) {
                                if (msgid.equals(imessage.getHeader(MessageHelper.HEADER_CORRELATION_ID, null))) {
                                    Log.w(folder.name + " late draft" +
                                            " host=" + account.host + " uid=" + dup.uid + ">" + uid);
                                    imessage.setFlag(Flags.Flag.DELETED, true);
                                    expunge(context, ifolder, Arrays.asList(imessage));
                                    return null;
                                }
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                }

                if (dup.recent != recent || dup.seen != seen || dup.answered != answered || dup.flagged != flagged)
                    syncSimilar = true;

                if (dup.flagged && dup.color != null)
                    color = dup.color;
                if (dup.notes != null) {
                    notes = dup.notes;
                    notes_color = dup.notes_color;
                }
            }
        }

        if (message == null) {
            Long sent = helper.getSent();

            Long received;
            long future = new Date().getTime() + FUTURE_RECEIVED;
            if (account.use_date || EntityFolder.SENT.equals(folder.type) || helper.isModified()) {
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
            if (received == null || received == 0)
                received = sent;
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
            message.thread = helper.getThreadId(context, account.id, folder.id, uid, received);
            if (BuildConfig.DEBUG && message.thread.startsWith("outlook:"))
                message.warning = message.thread;
            message.priority = helper.getPriority();
            message.sensitivity = helper.getSensitivity();

            for (String keyword : keywords)
                if (MessageHelper.FLAG_LOW_IMPORTANCE.equals(keyword))
                    message.importance = EntityMessage.PRIORITIY_LOW;
                else if (MessageHelper.FLAG_HIGH_IMPORTANCE.equals(keyword))
                    message.importance = EntityMessage.PRIORITIY_HIGH;

            message.auto_submitted = helper.getAutoSubmitted();
            message.receipt_request = helper.getReceiptRequested();
            message.receipt_to = helper.getReceiptTo();
            message.bimi_selector = helper.getBimiSelector();
            message.tls = helper.getTLS();
            message.dkim = MessageHelper.getAuthentication("dkim", authentication);
            message.spf = MessageHelper.getAuthentication("spf", authentication);
            if (message.spf == null)
                message.spf = helper.getSPF();
            message.dmarc = MessageHelper.getAuthentication("dmarc", authentication);
            message.auth = MessageHelper.getAuthentication("auth", authentication);
            message.smtp_from = helper.getMailFrom(authentication);
            message.return_path = helper.getReturnPath();
            message.submitter = helper.getSubmitter();
            message.from = helper.getFrom();
            message.to = helper.getTo();
            message.cc = helper.getCc();
            message.bcc = helper.getBcc();
            message.reply = helper.getReply();
            message.list_post = helper.getListPost();
            message.unsubscribe = helper.getListUnsubscribe();
            message.autocrypt = helper.getAutocrypt();
            if (download_headers)
                message.headers = helper.getHeaders();
            message.infrastructure = helper.getInfrastructure();
            message.subject = helper.getSubject();
            message.size = parts.getBodySize();
            message.total = helper.getSize();
            message.content = false;
            message.encrypt = parts.getEncryption();
            message.ui_encrypt = message.encrypt;
            message.received = received;
            message.notes = notes;
            message.notes_color = notes_color;
            message.sent = sent;
            message.recent = recent;
            message.seen = seen;
            message.answered = answered;
            message.flagged = flagged;
            message.deleted = deleted;
            message.flags = flags;
            message.keywords = keywords;
            message.labels = labels;
            message.ui_seen = seen;
            message.ui_answered = answered;
            message.ui_flagged = flagged;
            message.ui_deleted = deleted;
            message.ui_hide = false;
            message.ui_found = false;
            message.ui_ignored = (seen || deleted || browsed);
            message.ui_browsed = browsed;

            if (message.flagged)
                message.color = color;

            if (message.deliveredto != null)
                try {
                    Address deliveredto = new InternetAddress(message.deliveredto);
                    if (MessageHelper.equalEmail(new Address[]{deliveredto}, message.to))
                        message.deliveredto = null;
                } catch (AddressException ex) {
                    Log.w(ex);
                }

            if (MessageHelper.equalEmail(message.submitter, message.from))
                message.submitter = null;

            EntityIdentity identity = matchIdentity(context, folder, message);
            message.identity = (identity == null ? null : identity.id);
            boolean fromSelf = message.fromSelf(identity);

            if (native_dkim &&
                    !fromSelf &&
                    !EntityFolder.isOutgoing(folder.type) &&
                    !BuildConfig.PLAY_STORE_RELEASE) {
                List<String> signers = helper.verifyDKIM(context);
                message.dkim = !signers.isEmpty();
                message.signedby = (signers.isEmpty() ? null : TextUtils.join(",", signers));
                message.dmarc = helper.isAligned(context, signers,
                        message.return_path, message.smtp_from, message.from, helper.getSPF());
            }

            // Borrow reply name from sender name
            if (message.from != null && message.from.length == 1 &&
                    message.reply != null && message.reply.length == 1) {
                InternetAddress from = (InternetAddress) message.from[0];
                InternetAddress reply = (InternetAddress) message.reply[0];
                if (TextUtils.isEmpty(reply.getPersonal()) &&
                        Objects.equals(from.getAddress(), reply.getAddress()))
                    reply.setPersonal(from.getPersonal());
            }

            if (helper.isReport() && EntityFolder.DRAFTS.equals(folder.type))
                message.dsn = EntityMessage.DSN_HARD_BOUNCE;

            message.sender = MessageHelper.getSortKey(EntityFolder.isOutgoing(folder.type) ? message.to : message.from);
            Uri lookupUri = ContactInfo.getLookupUri(message.from);
            message.avatar = (lookupUri == null ? null : lookupUri.toString());
            if (message.avatar == null && notify_known && pro)
                message.ui_ignored = true;

            message.from_domain = (message.checkFromDomain(context) == null);

            // For contact forms
            if (!fromSelf) {
                String[] warning = message.checkReplyDomain(context);
                message.reply_domain = (warning == null);
            }

            boolean check_mx = prefs.getBoolean("check_mx", false);
            if (check_mx)
                try {
                    Address[] addresses =
                            (message.reply == null || message.reply.length == 0
                                    ? message.from : message.reply);
                    DnsHelper.checkMx(context, addresses);
                    message.mx = true;
                } catch (UnknownHostException ex) {
                    Log.w(ex);
                    message.mx = false;
                } catch (Throwable ex) {
                    Log.e(folder.name, ex);
                    message.warning = Log.formatThrowable(ex, false);
                }

            boolean check_blocklist = prefs.getBoolean("check_blocklist", false);
            if (check_blocklist) {
                if (!have &&
                        !EntityFolder.isOutgoing(folder.type) &&
                        !EntityFolder.ARCHIVE.equals(folder.type) &&
                        !EntityFolder.TRASH.equals(folder.type) &&
                        !EntityFolder.JUNK.equals(folder.type) &&
                        !message.isNotJunk(context) &&
                        !Arrays.asList(message.keywords).contains(MessageHelper.FLAG_NOT_JUNK))
                    try {
                        message.blocklist = DnsBlockList.isJunk(context,
                                imessage.getHeader("Received"));

                        if (message.blocklist == null || !message.blocklist) {
                            List<Address> senders = new ArrayList<>();
                            if (message.reply != null)
                                senders.addAll(Arrays.asList(message.reply));
                            if (message.from != null)
                                senders.addAll(Arrays.asList(message.from));
                            message.blocklist = DnsBlockList.isJunk(context, senders);
                        }
                    } catch (Throwable ex) {
                        Log.w(folder.name, ex);
                    }
            }

            boolean needsHeaders = EntityRule.needsHeaders(message, rules);
            boolean needsBody = EntityRule.needsBody(message, rules);
            if (needsHeaders || needsBody)
                Log.i(folder.name + " needs headers=" + needsHeaders + " body=" + needsBody);
            List<Header> headers = (needsHeaders ? helper.getAllHeaders() : null);
            String body = (needsBody ? parts.getHtml(context, download_plain) : null);

            if (mdn && helper.isReport())
                try {
                    MessageHelper.Report r = parts.getReport();
                    boolean client_id = prefs.getBoolean("client_id", true);
                    String we = "dns;" + (client_id ? EmailService.getDefaultEhlo() : "example.com");
                    if (r != null && !we.equals(r.reporter)) {
                        String label = null;
                        if (r.isDeliveryStatus())
                            label = (r.isDelivered() ? MessageHelper.FLAG_DELIVERED : MessageHelper.FLAG_NOT_DELIVERED);
                        else if (r.isDispositionNotification())
                            label = (r.isMdnDisplayed() ? MessageHelper.FLAG_DISPLAYED : MessageHelper.FLAG_NOT_DISPLAYED);
                        else if (r.isFeedbackReport())
                            label = MessageHelper.FLAG_COMPLAINT;

                        if (label != null) {
                            Map<Long, EntityFolder> map = new HashMap<>();

                            EntityFolder s = db.folder().getFolderByType(folder.account, EntityFolder.SENT);
                            if (s != null)
                                map.put(s.id, s);

                            List<EntityMessage> all = new ArrayList<>();

                            if (message.inreplyto != null) {
                                List<EntityMessage> replied = db.message().getMessagesByMsgId(folder.account, message.inreplyto);
                                if (replied != null)
                                    all.addAll(replied);
                            }
                            if (r.refid != null && !r.refid.equals(message.inreplyto)) {
                                List<EntityMessage> refs = db.message().getMessagesByMsgId(folder.account, r.refid);
                                if (refs != null)
                                    all.addAll(refs);
                            }

                            for (EntityMessage m : all)
                                if (!map.containsKey(m.folder)) {
                                    EntityFolder f = db.folder().getFolder(m.folder);
                                    if (f != null)
                                        map.put(f.id, f);
                                }

                            for (String msgid : new String[]{message.inreplyto, r.refid})
                                if (msgid != null)
                                    for (EntityFolder f : map.values())
                                        EntityOperation.queue(context, f, EntityOperation.REPORT, msgid, label);
                        }
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
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

                runRules(context, headers, body, account, folder, message, rules);

                if (message.blocklist != null && message.blocklist) {
                    boolean use_blocklist = prefs.getBoolean("use_blocklist", false);
                    if (use_blocklist) {
                        EntityLog.log(context, EntityLog.Type.General, message,
                                "Block list" +
                                        " folder=" + folder.name +
                                        " message=" + message.id +
                                        "@" + new Date(message.received) +
                                        ":" + message.subject);
                        EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                        if (junk != null) {
                            EntityOperation.queue(context, message, EntityOperation.MOVE, junk.id, false);
                            message.ui_hide = true;
                        }
                    }
                }

                if (download && !message.ui_hide &&
                        MessageClassifier.isEnabled(context) && folder.auto_classify_source)
                    db.message().setMessageUiHide(message.id, true); // keep local value

                db.setTransactionSuccessful();
            } catch (SQLiteConstraintException ex) {
                Log.i(ex);

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
                EntityContact.received(context, account, folder, message);

                if (body == null && helper.isReport())
                    body = parts.getHtml(context, download_plain);

                // Download small messages inline
                if (body != null || (download && !message.ui_hide)) {
                    long maxSize;
                    if (state == null || state.networkState.isUnmetered())
                        maxSize = MessageHelper.SMALL_MESSAGE_SIZE;
                    else {
                        maxSize = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
                        if (maxSize == 0 || maxSize > MessageHelper.SMALL_MESSAGE_SIZE)
                            maxSize = MessageHelper.SMALL_MESSAGE_SIZE;
                    }

                    if (body != null ||
                            (message.size != null && message.size < maxSize) ||
                            account.isWpPl() ||
                            (MessageClassifier.isEnabled(context)) && folder.auto_classify_source)
                        try {
                            if (body == null)
                                body = parts.getHtml(context, download_plain);
                            File file = message.getFile(context);
                            Helper.writeText(file, body);
                            String text = HtmlHelper.getFullText(context, body);
                            message.content = true;
                            message.preview = HtmlHelper.getPreview(text);
                            message.language = HtmlHelper.getLanguage(context, message.subject, text);
                            db.message().setMessageContent(message.id,
                                    message.content,
                                    message.language,
                                    parts.isPlainOnly(download_plain),
                                    message.preview,
                                    parts.getWarnings(message.warning));
                            MessageClassifier.classify(message, folder, true, context);

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

            if (!message.recent.equals(recent)) {
                update = true;
                message.recent = recent;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " recent=" + recent);
                syncSimilar = true;
            }

            if ((!message.seen.equals(seen) ||
                    (!folder.read_only && !message.ui_seen.equals(seen))) &&
                    db.operation().getOperationCount(folder.id, message.id, EntityOperation.SEEN) == 0) {
                update = true;
                message.seen = seen;
                message.ui_seen = seen;
                if (seen)
                    message.ui_ignored = true;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " seen=" + seen);
                syncSimilar = true;
            }

            if ((!message.answered.equals(answered) ||
                    (!folder.read_only && !message.ui_answered.equals(message.answered))) &&
                    db.operation().getOperationCount(folder.id, message.id, EntityOperation.ANSWERED) == 0) {
                update = true;
                message.answered = answered;
                message.ui_answered = answered;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " answered=" + answered);
                syncSimilar = true;
            }

            if ((!message.flagged.equals(flagged) ||
                    (!folder.read_only && !message.ui_flagged.equals(flagged))) &&
                    db.operation().getOperationCount(folder.id, message.id, EntityOperation.FLAG) == 0) {
                update = true;
                message.flagged = flagged;
                message.ui_flagged = flagged;
                if (!flagged)
                    message.color = null;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flagged=" + flagged);
                syncSimilar = true;
            }

            if ((!message.deleted.equals(deleted) || !message.ui_deleted.equals(deleted)) &&
                    db.operation().getOperationCount(folder.id, message.id, EntityOperation.DELETE) == 0) {
                update = true;
                message.deleted = deleted;
                message.ui_deleted = deleted;
                message.ui_ignored = deleted;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " deleted=" + deleted);
                syncSimilar = true;
            }

            if (!Objects.equals(flags, message.flags)) {
                update = true;
                message.flags = flags;
                Log.i(folder.name + " updated id=" + message.id + " uid=" + message.uid + " flags=" + flags);
            }

            if (!Helper.equal(message.keywords, keywords) &&
                    !folder.read_only &&
                    (ifolder.getPermanentFlags().contains(Flags.Flag.USER) || keywords.length > 0)) {
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

            if (download_headers && message.headers == null) {
                update = true;
                message.headers = helper.getHeaders();
                Log.i(folder.name + " updated id=" + message.id + " headers");
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

            if (update || process) {
                boolean needsHeaders = (process && EntityRule.needsHeaders(message, rules));
                boolean needsBody = (process && EntityRule.needsBody(message, rules));
                if (needsHeaders || needsBody)
                    Log.i(folder.name + " needs headers=" + needsHeaders + " body=" + needsBody);
                List<Header> headers = (needsHeaders ? helper.getAllHeaders() : null);
                String body = (needsBody ? helper.getMessageParts().getHtml(context, download_plain) : null);

                try {
                    db.beginTransaction();

                    EntityMessage existing = db.message().getMessage(message.id);
                    if (existing != null) {
                        message.revision = existing.revision;
                        message.revisions = existing.revisions;
                    }

                    db.message().updateMessage(message);

                    if (process)
                        runRules(context, headers, body, account, folder, message, rules);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            if (process) {
                EntityContact.received(context, account, folder, message);
                MessageClassifier.classify(message, folder, true, context);
            } else
                Log.d(folder.name + " unchanged uid=" + uid);

            if (process)
                reportNewMessage(context, account, folder, message);
        }

        if (syncSimilar && account.isGmail())
            for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid, message.hash)) {
                if (similar.recent != message.recent) {
                    Log.i(folder.name + " Synchronize similar id=" + similar.id + " recent=" + message.recent);
                    db.message().setMessageRecent(similar.id, message.recent);
                }

                if (similar.seen != message.seen) {
                    Log.i(folder.name + " Synchronize similar id=" + similar.id + " seen=" + message.seen);
                    db.message().setMessageSeen(similar.id, message.seen);
                    db.message().setMessageUiSeen(similar.id, message.seen);
                }

                if (similar.answered != message.answered) {
                    Log.i(folder.name + " Synchronize similar id=" + similar.id + " answered=" + message.answered);
                    db.message().setMessageAnswered(similar.id, message.answered);
                    db.message().setMessageUiAnswered(similar.id, message.answered);
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

    private static boolean expunge(Context context, IMAPFolder ifolder, List<Message> messages) {
        if (messages.size() == 0)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean perform_expunge = prefs.getBoolean("perform_expunge", true);
        boolean uid_expunge = prefs.getBoolean("uid_expunge", false);

        if (!perform_expunge)
            return false;

        try {
            if (uid_expunge)
                uid_expunge = MessageHelper.hasCapability(ifolder, "UIDPLUS");
            if (MessageHelper.hasCapability(ifolder, "X-UIDONLY"))
                uid_expunge = true;

            if (uid_expunge) {
                FetchProfile fp = new FetchProfile();
                fp.add(UIDFolder.FetchProfileItem.UID);
                ifolder.fetch(messages.toArray(new Message[0]), fp);

                List<Long> uids = new ArrayList<>();
                for (Message m : messages)
                    try {
                        long uid = ifolder.getUID(m);
                        if (uid < 0)
                            continue;
                        uids.add(uid);
                    } catch (MessageRemovedException ex) {
                        Log.w(ex);
                    }

                Log.i(ifolder.getName() + " expunging " + TextUtils.join(",", uids));
                uidExpunge(context, ifolder, uids);
                Log.i(ifolder.getName() + " expunged " + TextUtils.join(",", uids));
            } else {
                Log.i(ifolder.getName() + " expunging all=" + messages.size());
                ifolder.expunge();
                Log.i(ifolder.getName() + " expunged all=" + messages.size());
            }

            return true;
        } catch (MessagingException ex) {
            // NO EXPUNGE failed.
            Log.w(ex);
            return false;
        }
    }

    private static void uidExpunge(Context context, IMAPFolder ifolder, List<Long> uids) throws MessagingException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int chunk_size = prefs.getInt("chunk_size", DEFAULT_CHUNK_SIZE);

        ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                // https://datatracker.ietf.org/doc/html/rfc4315#section-2.1
                for (List<Long> list : Helper.chunkList(uids, chunk_size))
                    protocol.uidexpunge(UIDSet.createUIDSets(Helper.toLongArray(list)));
                return null;
            }
        });
    }

    static EntityIdentity matchIdentity(Context context, EntityFolder folder, EntityMessage message) {
        if (EntityFolder.DRAFTS.equals(folder.type))
            return null;

        List<Address> addresses = new ArrayList<>();
        if (folder.isOutgoing()) {
            if (message.from != null)
                addresses.addAll(Arrays.asList(message.from));
        } else {
            Address[] senders = (message.isForwarder() ? message.submitter : message.from);

            if (message.to != null)
                addresses.addAll(Arrays.asList(message.to));
            if (message.cc != null)
                addresses.addAll(Arrays.asList(message.cc));
            if (message.bcc != null)
                addresses.addAll(Arrays.asList(message.bcc));
            if (senders != null)
                addresses.addAll(Arrays.asList(senders));
        }

        InternetAddress deliveredto = null;
        if (message.deliveredto != null)
            try {
                deliveredto = new InternetAddress(message.deliveredto);
            } catch (AddressException ex) {
                Log.w(ex);
            }

        // Search for matching identity
        List<EntityIdentity> identities = getIdentities(folder.account, context);
        if (identities != null) {
            for (Address address : addresses)
                for (EntityIdentity identity : identities)
                    if (identity.sameAddress(address)) {
                        Log.i("Matched same" +
                                " identity=" + identity.email +
                                " address=" + ((InternetAddress) address).getAddress() +
                                " folder=" + folder.name);
                        return identity;
                    }

            for (Address address : addresses)
                for (EntityIdentity identity : identities)
                    if (identity.similarAddress(address)) {
                        Log.i("Matched similar" +
                                " identity=" + identity.email +
                                " regex=" + identity.sender_extra_regex +
                                " address=" + ((InternetAddress) address).getAddress() +
                                " folder=" + folder.name);
                        return identity;
                    }

            if (deliveredto != null)
                for (EntityIdentity identity : identities)
                    if (identity.sameAddress(deliveredto) || identity.similarAddress(deliveredto)) {
                        Log.i("Matched deliveredto" +
                                " identity=" + identity.email +
                                " regex=" + identity.sender_extra_regex +
                                " address=" + ((InternetAddress) deliveredto).getAddress() +
                                " folder=" + folder.name);
                        return identity;
                    }
        }

        Log.i("Matched none" +
                " addresses=" + MessageHelper.formatAddresses(addresses.toArray(new Address[0])) +
                " deliveredto=" + (deliveredto == null ? null : ((InternetAddress) deliveredto).getAddress()) +
                " folder=" + folder.name);

        return null;
    }

    private static void runRules(
            Context context, List<Header> headers, String html,
            EntityAccount account, EntityFolder folder, EntityMessage message,
            List<EntityRule> rules) {

        if (EntityFolder.INBOX.equals(folder.type)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String mnemonic = prefs.getString("wipe_mnemonic", null);
            if (mnemonic != null && message.subject != null &&
                    message.subject.toLowerCase(Locale.ROOT).contains(mnemonic))
                Helper.clearAll(context);
        }

        if (account.protocol == EntityAccount.TYPE_IMAP && folder.read_only)
            return;

        boolean pro = ActivityBilling.isPro(context);

        DB db = DB.getInstance(context);
        try {
            boolean executed = false;
            if (pro) {
                int applied = EntityRule.run(context, rules, message, headers, html);
                executed = (applied > 0);
            }

            if (EntityFolder.INBOX.equals(folder.type))
                if (message.from != null) {
                    EntityContact badboy = null;
                    for (Address from : message.from) {
                        String email = ((InternetAddress) from).getAddress();
                        if (TextUtils.isEmpty(email))
                            continue;

                        badboy = db.contact().getContact(message.account, EntityContact.TYPE_JUNK, email);
                        if (badboy != null)
                            break;
                    }

                    if (badboy != null) {
                        badboy.times_contacted++;
                        badboy.last_contacted = new Date().getTime();
                        db.contact().updateContact(badboy);

                        EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                        if (junk != null) {
                            EntityOperation.queue(context, message, EntityOperation.MOVE, junk.id);
                            message.ui_hide = true;
                            executed = true;
                        }
                    }
                }

            if (executed &&
                    !message.hasKeyword(MessageHelper.FLAG_FILTERED))
                EntityOperation.queue(context, message, EntityOperation.KEYWORD, MessageHelper.FLAG_FILTERED, true);
        } catch (Throwable ex) {
            Log.e(ex);
            db.message().setMessageError(message.id, Log.formatThrowable(ex));
        }
    }

    private static void reportNewMessage(Context context, EntityAccount account, EntityFolder folder, EntityMessage message) {
        // Prepare scroll to top
        if (!message.ui_seen && !message.ui_hide &&
                message.received > account.created) {
            Intent report = new Intent(ActivityView.ACTION_NEW_MESSAGE);
            report.putExtra("folder", folder.id);
            report.putExtra("type", folder.type);
            report.putExtra("unified", folder.unified);
            Log.i("Report new id=" + message.id +
                    " folder=" + folder.type + ":" + folder.name +
                    " unified=" + folder.unified);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(report);
        }
    }

    private static boolean downloadMessage(
            Context context,
            EntityAccount account, EntityFolder folder,
            IMAPStore istore, IMAPFolder ifolder,
            MimeMessage imessage, long id, State state, SyncStats stats) throws MessagingException, IOException {
        if (state.getNetworkState().isRoaming())
            return false;

        if (imessage == null)
            return false;

        DB db = DB.getInstance(context);
        EntityMessage message = db.message().getMessage(id);
        if (message == null || message.ui_hide)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long maxSize = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
        if (maxSize == 0)
            maxSize = Long.MAX_VALUE;
        boolean download_limited = prefs.getBoolean("download_limited", false);
        boolean download_eml = prefs.getBoolean("download_eml", false);

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);

        boolean fetch = false;
        if (!message.content)
            if ((!download_limited && state.getNetworkState().isUnmetered()) ||
                    (message.size != null && message.size < maxSize))
                fetch = true;

        if (!fetch)
            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    if ((!download_limited && state.getNetworkState().isUnmetered()) ||
                            (attachment.size != null && attachment.size < maxSize)) {
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
                if ((!download_limited && state.getNetworkState().isUnmetered()) ||
                        (message.size != null && message.size < maxSize)) {
                    String body = parts.getHtml(context);
                    File file = message.getFile(context);
                    Helper.writeText(file, body);
                    String text = HtmlHelper.getFullText(context, body);
                    message.preview = HtmlHelper.getPreview(text);
                    message.language = HtmlHelper.getLanguage(context, message.subject, text);
                    db.message().setMessageContent(message.id,
                            true,
                            message.language,
                            parts.isPlainOnly(),
                            message.preview,
                            parts.getWarnings(message.warning));
                    MessageClassifier.classify(message, folder, true, context);

                    if (stats != null && body != null)
                        stats.content += body.length();
                    Log.i(folder.name + " downloaded message id=" + message.id +
                            " size=" + message.size + "/" + (body == null ? null : body.length()));

                    if (TextUtils.isEmpty(body) && parts.hasBody())
                        reportEmptyMessage(context, state, account, istore);
                }
            }

            for (EntityAttachment attachment : attachments)
                if (!attachment.available &&
                        attachment.subsequence == null &&
                        TextUtils.isEmpty(attachment.error))
                    if ((!download_limited && state.getNetworkState().isUnmetered()) ||
                            (attachment.size != null && attachment.size < maxSize))
                        try {
                            parts.downloadAttachment(context, attachment, folder);
                            if (stats != null && attachment.size != null)
                                stats.attachments += attachment.size;
                        } catch (Throwable ex) {
                            Log.e(folder.name, ex);
                            db.attachment().setError(attachment.id, Log.formatThrowable(ex, false));
                        }
        }

        if (download_eml &&
                (message.raw == null || !message.raw) &&
                ((!download_limited && state.getNetworkState().isUnmetered()) ||
                        (message.total != null && message.total < maxSize))) {
            File file = message.getRawFile(context);
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                imessage.writeTo(os);
            }

            message.raw = true;
            db.message().setMessageRaw(message.id, message.raw);
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
                    for (String key : sid.keySet())
                        sb.append(" ").append(key).append("=").append(sid.get(key));
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
        if (account.partial_fetch && false) {
            account.partial_fetch = false;
            DB db = DB.getInstance(context);
            db.account().setAccountPartialFetch(account.id, account.partial_fetch);
            state.error(new StoreClosedException(istore));
        }
    }

    // FolderClosedException: can happen when no connectivity

    // IllegalStateException:
    // - "This operation is not allowed on a closed folder"
    // - can happen when syncing message

    // ConnectionException
    // - failed to create new store connection (connectivity)

    // MailConnectException
    // - on connectivity problems when connecting to store

    static NotificationCompat.Builder getNotificationError(Context context, String channel, EntityAccount account, long id, Throwable ex) {
        String title = context.getString(R.string.title_notification_failed, account.name);
        String message = Log.formatThrowable(ex, "\n", false);

        // Build pending intent
        Intent intent = new Intent(context, ActivityError.class);
        intent.setAction(channel + ":" + account.id + ":" + id);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("provider", account.provider);
        intent.putExtra("account", account.id);
        intent.putExtra("protocol", account.protocol);
        intent.putExtra("auth_type", account.auth_type);
        intent.putExtra("host", account.host);
        intent.putExtra("address", account.user);
        intent.putExtra("faq", 22);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntentCompat.getActivity(
                context, ActivityError.PI_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channel)
                        .setSmallIcon(R.drawable.baseline_warning_white_24)
                        .setContentTitle(title)
                        .setContentText(Log.formatThrowable(ex, false))
                        .setContentIntent(pi)
                        .setAutoCancel(false)
                        .setShowWhen(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOnlyAlertOnce(true)
                        .setCategory(NotificationCompat.CATEGORY_ERROR)
                        .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        return builder;
    }

    static class State {
        private int backoff;
        private boolean backingoff = false;
        private ConnectionHelper.NetworkState networkState;
        private Thread thread = new Thread();
        private Semaphore semaphore = new Semaphore(0);
        private boolean started = false;
        private boolean running = true;
        private boolean foreground = false;
        private boolean recoverable = true;
        private Throwable unrecoverable = null;
        private Long lastActivity = null;

        private long serial = 0;

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
            microWait();
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
                microWait();
            }
        }

        void reset() {
            Thread.currentThread().interrupted(); // clear interrupted status
            Log.i("Permits=" + semaphore.drainPermits());
            recoverable = true;
            lastActivity = null;
        }

        void nextSerial() {
            serial++;
        }

        private void microWait() {
            try {
                // Give interrupted thread some time to acquire wake lock
                Thread.sleep(YIELD_DURATION);
            } catch (InterruptedException ignored) {
            }
        }

        void start() {
            thread.start();
            started = true;
        }

        void stop() {
            running = false;
            semaphore.release();
        }

        boolean isAlive() {
            if (!started)
                return true;
            if (!running)
                return false;
            if (thread == null)
                return false;
            return thread.isAlive();
        }

        void join() {
            join(thread);
            CoalMine.watch(thread, getClass().getSimpleName() + "#join()");
        }

        void ensureRunning(String reason) throws OperationCanceledException {
            if (!recoverable && unrecoverable != null)
                throw new OperationCanceledExceptionEx(reason, unrecoverable);
            if (!running)
                throw new OperationCanceledException(reason);
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
                            " state=" + thread.getState() +
                            " interrupted=" + interrupted);

                    thread.join(interrupted ? JOIN_WAIT_INTERRUPT : JOIN_WAIT_ALIVE);

                    // https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.State.html
                    Thread.State state = thread.getState();
                    if (thread.isAlive() &&
                            state != Thread.State.NEW &&
                            state != Thread.State.TERMINATED) {
                        if (interrupted)
                            Log.e("Join " + name + " failed" +
                                    " state=" + state + " interrupted=" + interrupted);
                        if (interrupted)
                            joined = true; // giving up
                        else {
                            thread.interrupt();
                            interrupted = true;
                        }
                    } else {
                        Log.i("Joined " + name + " " + " state=" + state);
                        joined = true;
                    }
                } catch (InterruptedException ex) {
                    Log.i(new Throwable(name, ex));
                }
        }

        synchronized void activity() {
            lastActivity = SystemClock.elapsedRealtime();
        }

        long getIdleTime() {
            Long last = lastActivity;
            return (last == null ? 0 : SystemClock.elapsedRealtime() - last);
        }

        long getSerial() {
            return serial;
        }

        void setForeground(boolean value) {
            this.foreground = value;
        }

        boolean getForeground() {
            return this.foreground;
        }

        @NonNull
        @Override
        public String toString() {
            return "[running=" + running +
                    ",recoverable=" + recoverable +
                    ",idle=" + getIdleTime() + "" +
                    ",serial=" + serial + "]";
        }
    }

    static class OperationCanceledExceptionEx extends OperationCanceledException {
        private Throwable cause;

        OperationCanceledExceptionEx(String message, Throwable cause) {
            super(message);
            this.cause = cause;
        }

        @Nullable
        @Override
        public Throwable getCause() {
            return this.cause;
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
