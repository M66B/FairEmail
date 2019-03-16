package eu.faircode.email;

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

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MailConnectException;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

class Core {
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
            for (int i = 0; i < ops.size() && state.running(); i++) {
                EntityOperation op = ops.get(i);
                try {
                    Log.i(folder.name +
                            " start op=" + op.id + "/" + op.name +
                            " msg=" + op.message +
                            " args=" + op.args);

                    // Fetch most recent copy of message
                    EntityMessage message = null;
                    if (op.message != null)
                        message = db.message().getMessage(op.message);

                    JSONArray jargs = new JSONArray(op.args);

                    try {
                        if (message == null && !EntityOperation.SYNC.equals(op.name))
                            throw new MessageRemovedException();

                        db.operation().setOperationError(op.id, null);
                        if (message != null)
                            db.message().setMessageError(message.id, null);

                        if (message != null && message.uid == null &&
                                !(EntityOperation.ADD.equals(op.name) ||
                                        EntityOperation.DELETE.equals(op.name) ||
                                        EntityOperation.SEND.equals(op.name) ||
                                        EntityOperation.SYNC.equals(op.name) ||
                                        EntityOperation.WAIT.equals(op.name)))
                            throw new IllegalArgumentException(op.name + " without uid " + op.args);

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

                            case EntityOperation.WAIT:
                                break;

                            default:
                                throw new IllegalArgumentException("Unknown operation=" + op.name);
                        }

                        // Operation succeeded
                        db.operation().deleteOperation(op.id);
                    } catch (Throwable ex) {
                        Log.e(folder.name, ex);
                        reportError(context, account, folder, ex);

                        db.operation().setOperationError(op.id, Helper.formatThrowable(ex));
                        if (message != null)
                            db.message().setMessageError(message.id, Helper.formatThrowable(ex, true));

                        if (ex instanceof MessageRemovedException ||
                                ex instanceof FolderNotFoundException ||
                                ex instanceof IllegalArgumentException) {
                            Log.w("Unrecoverable");

                            // There is no use in repeating
                            db.operation().deleteOperation(op.id);

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

        if (TextUtils.isEmpty(message.msgid))
            throw new IllegalArgumentException("Message ID missing");

        // Get message
        MimeMessage imessage;
        if (folder.id.equals(message.folder)) {
            // Pre flight checks
            if (!message.content)
                throw new IllegalArgumentException("Message body missing");

            EntityIdentity identity =
                    (message.identity == null ? null : db.identity().getIdentity(message.identity));

            imessage = MessageHelper.from(context, message, isession,
                    identity == null ? false : identity.plain_only);
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
        boolean autoread = false;
        if (jargs.length() > 1) {
            autoread = jargs.getBoolean(1);
            if (ifolder.getPermanentFlags().contains(Flags.Flag.SEEN)) {
                if (autoread && !imessage.isSet(Flags.Flag.SEEN)) {
                    Log.i(folder.name + " autoread");
                    imessage.setFlag(Flags.Flag.SEEN, true);
                }
            }
        }

        // Handle draft
        if (EntityFolder.DRAFTS.equals(folder.type))
            if (ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
                imessage.setFlag(Flags.Flag.DRAFT, true);

        // Add message
        long uid = append(istore, ifolder, imessage);
        Log.i(folder.name + " appended id=" + message.id + " uid=" + uid);
        db.message().setMessageUid(message.id, uid);

        if (folder.id.equals(message.folder)) {
            // Delete previous message
            Message[] ideletes = ifolder.search(new MessageIDTerm(message.msgid));
            for (Message idelete : ideletes) {
                long duid = ifolder.getUID(idelete);
                if (duid == uid)
                    Log.i(folder.name + " append confirmed uid=" + duid);
                else {
                    Log.i(folder.name + " deleting uid=" + duid + " msgid=" + message.msgid);
                    try {
                        idelete.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessageRemovedException ignored) {
                    }
                }
            }
            ifolder.expunge();
        } else
            try {
                db.beginTransaction();

                // Cross account move
                if (autoread) {
                    Log.i(folder.name + " queuing SEEN id=" + message.id);
                    EntityOperation.queue(context, db, message, EntityOperation.SEEN, true);
                }

                Log.i(folder.name + " queuing DELETE id=" + message.id);
                EntityOperation.queue(context, db, message, EntityOperation.DELETE);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
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

        // Get target folder
        EntityFolder target = db.folder().getFolder(id);
        if (target == null)
            throw new FolderNotFoundException();
        IMAPFolder itarget = (IMAPFolder) istore.getFolder(target.name);

        // Get message ID
        String msgid;
        if (copy || message.msgid == null) {
            msgid = EntityMessage.generateMessageId();
            Log.i(target.name + " generated message id=" + msgid);
        } else
            msgid = message.msgid;

        // Serialize source message
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imessage.writeTo(bos);

        // Deserialize target message
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Message icopy = new MimeMessage(isession, bis);

        try {
            // Needed to read flags
            itarget.open(Folder.READ_WRITE);

            // Auto read
            if (itarget.getPermanentFlags().contains(Flags.Flag.SEEN))
                if (autoread && !icopy.isSet(Flags.Flag.SEEN))
                    icopy.setFlag(Flags.Flag.SEEN, true);

            // Move from drafts
            if (EntityFolder.DRAFTS.equals(folder.type))
                if (itarget.getPermanentFlags().contains(Flags.Flag.DRAFT))
                    icopy.setFlag(Flags.Flag.DRAFT, false);

            // Move to drafts
            if (EntityFolder.DRAFTS.equals(target.type))
                if (itarget.getPermanentFlags().contains(Flags.Flag.DRAFT))
                    icopy.setFlag(Flags.Flag.DRAFT, true);

            icopy.setHeader("Message-ID", msgid);

            // Append target
            long uid = append(istore, itarget, (MimeMessage) icopy);

            // This won't work properly when deleting the same message in multiple folders
            // For example Gmail's inbox/archive
            //Long newid = (jargs.length() > 2 && !jargs.isNull(2) ? jargs.getLong(2) : null);
            //if (newid != null) {
            //    Log.i(folder.name + " moved newid=" + newid + " uid=" + uid);
            //    db.message().setMessageUid(newid, uid);
            //}

            // Fixed timing issue of at least Courier based servers
            itarget.close(false);
            itarget.open(Folder.READ_WRITE);

            // Some providers, like Gmail, don't honor the appended seen flag
            if (itarget.getPermanentFlags().contains(Flags.Flag.SEEN)) {
                boolean seen = (autoread || message.ui_seen);
                icopy = itarget.getMessageByUID(uid);
                if (icopy != null && seen != icopy.isSet(Flags.Flag.SEEN)) {
                    Log.i(target.name + " Fixing id=" + message.id + " seen=" + seen);
                    icopy.setFlag(Flags.Flag.SEEN, seen);
                }
            }

            // This is not based on an actual case, so this is just a safeguard
            if (itarget.getPermanentFlags().contains(Flags.Flag.DRAFT)) {
                boolean draft = EntityFolder.DRAFTS.equals(target.type);
                icopy = itarget.getMessageByUID(uid);
                if (icopy != null && draft != icopy.isSet(Flags.Flag.DRAFT)) {
                    Log.i(target.name + " Fixing id=" + message.id + " draft=" + draft);
                    icopy.setFlag(Flags.Flag.DRAFT, draft);
                }
            }

            // Delete source
            if (!copy) {
                try {
                    imessage.setFlag(Flags.Flag.DELETED, true);
                } catch (MessageRemovedException ignored) {
                }
                ifolder.expunge();
            }
        } finally {
            if (itarget.isOpen())
                itarget.close();
        }
    }

    private static void onDelete(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, IMAPFolder ifolder) throws MessagingException {
        // Delete message
        DB db = DB.getInstance(context);

        if (TextUtils.isEmpty(message.msgid))
            throw new IllegalArgumentException("Message ID missing");

        Message[] imessages = ifolder.search(new MessageIDTerm(message.msgid));
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
            long target = jargs.getLong(2);
            jargs.remove(2);
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
        db.message().setMessageContent(message.id, true,
                HtmlHelper.getPreview(body), parts.getWarnings(message.warning));
    }

    private static void onAttachment(Context context, JSONArray jargs, EntityFolder folder, EntityMessage message, EntityOperation op, IMAPFolder ifolder) throws JSONException, MessagingException, IOException {
        // Download attachment
        DB db = DB.getInstance(context);

        int sequence = jargs.getInt(0);

        // Get attachment
        EntityAttachment attachment = db.attachment().getAttachment(op.message, sequence);
        if (attachment.available)
            return;

        // Get message
        Message imessage = ifolder.getMessageByUID(message.uid);
        if (imessage == null)
            throw new MessageRemovedException();

        // Download attachment
        MessageHelper helper = new MessageHelper((MimeMessage) imessage);
        MessageHelper.MessageParts parts = helper.getMessageParts();
        parts.downloadAttachment(context, sequence - 1, attachment.id);
    }

    private static long append(IMAPStore istore, IMAPFolder ifolder, MimeMessage imessage) throws MessagingException {
        String msgid = imessage.getMessageID();
        if (msgid == null)
            throw new IllegalArgumentException("Message ID missing");

        long uid = -1;
        if (istore.hasCapability("UIDPLUS")) {
            AppendUID[] uids = ifolder.appendUIDMessages(new Message[]{imessage});
            if (uids != null && uids.length > 0) {
                Log.i("Appended uid=" + uids[0].uid);
                uid = uids[0].uid;
            }
        } else
            ifolder.appendMessages(new Message[]{imessage});

        if (uid <= 0) {
            Log.i("Searching for appended msgid=" + msgid);
            Message[] messages = ifolder.search(new MessageIDTerm(msgid));
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

        return uid;
    }

    static void onSynchronizeFolders(Context context, EntityAccount account, Store istore, State state) throws MessagingException {
        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            Log.i("Start sync folders account=" + account.name);

            List<String> names = new ArrayList<>();
            for (EntityFolder folder : db.folder().getFolders(account.id))
                if (folder.tbc != null) {
                    Log.i(folder.name + " creating");
                    Folder ifolder = istore.getFolder(folder.name);
                    if (!ifolder.exists())
                        ifolder.create(Folder.HOLDS_MESSAGES);
                    db.folder().resetFolderTbc(folder.id);
                } else if (folder.tbd != null && folder.tbd) {
                    Log.i(folder.name + " deleting");
                    Folder ifolder = istore.getFolder(folder.name);
                    if (ifolder.exists())
                        ifolder.delete(false);
                    db.folder().deleteFolder(folder.id);
                } else
                    names.add(folder.name);
            Log.i("Local folder count=" + names.size());

            Folder defaultFolder = istore.getDefaultFolder();
            char separator = defaultFolder.getSeparator();
            EntityLog.log(context, account.name + " folder separator=" + separator);

            Folder[] ifolders = defaultFolder.list("*");
            Log.i("Remote folder count=" + ifolders.length + " separator=" + separator);

            for (Folder ifolder : ifolders) {
                String fullName = ifolder.getFullName();
                String[] attrs = ((IMAPFolder) ifolder).getAttributes();
                String type = EntityFolder.getType(attrs, fullName);

                EntityLog.log(context, account.name + ":" + fullName +
                        " attrs=" + TextUtils.join(" ", attrs) + " type=" + type);

                if (type != null) {
                    names.remove(fullName);

                    int level = EntityFolder.getLevel(separator, fullName);
                    String display = null;
                    if (account.prefix != null && fullName.startsWith(account.prefix + separator))
                        display = fullName.substring(account.prefix.length() + 1);

                    EntityFolder folder = db.folder().getFolderByName(account.id, fullName);
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.account = account.id;
                        folder.name = fullName;
                        folder.display = display;
                        folder.type = (EntityFolder.SYSTEM.equals(type) ? type : EntityFolder.USER);
                        folder.level = level;
                        folder.synchronize = false;
                        folder.poll = ("imap.gmail.com".equals(account.host));
                        folder.sync_days = EntityFolder.DEFAULT_SYNC;
                        folder.keep_days = EntityFolder.DEFAULT_KEEP;
                        db.folder().insertFolder(folder);
                        Log.i(folder.name + " added type=" + folder.type);
                    } else {
                        Log.i(folder.name + " exists type=" + folder.type);

                        if (folder.display == null) {
                            if (display != null) {
                                db.folder().setFolderDisplay(folder.id, display);
                                EntityLog.log(context, account.name + ":" + folder.name +
                                        " removed prefix display=" + display + " separator=" + separator);
                            }
                        } else {
                            if (account.prefix == null && folder.name.endsWith(separator + folder.display)) {
                                db.folder().setFolderDisplay(folder.id, null);
                                EntityLog.log(context, account.name + ":" + folder.name +
                                        " restored prefix display=" + folder.display + " separator=" + separator);
                            }
                        }

                        db.folder().setFolderLevel(folder.id, level);

                        // Compatibility
                        if ("Inbox_sub".equals(folder.type))
                            db.folder().setFolderType(folder.id, EntityFolder.USER);
                        else if (EntityFolder.USER.equals(folder.type) && EntityFolder.SYSTEM.equals(type))
                            db.folder().setFolderType(folder.id, type);
                        else if (EntityFolder.SYSTEM.equals(folder.type) && EntityFolder.USER.equals(type))
                            db.folder().setFolderType(folder.id, type);
                        else if (EntityFolder.INBOX.equals(type) && !EntityFolder.INBOX.equals(folder.type)) {
                            if (db.folder().getFolderByType(folder.account, EntityFolder.INBOX) == null)
                                db.folder().setFolderType(folder.id, type);
                        }
                    }
                }
            }

            Log.i("Delete local count=" + names.size());
            for (String name : names) {
                Log.i(name + " delete");
                db.folder().deleteFolder(account.id, name);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i("End sync folder");
        }
    }

    private static void onSynchronizeMessages(
            Context context, JSONArray jargs,
            EntityAccount account, final EntityFolder folder,
            IMAPFolder ifolder, State state) throws JSONException, MessagingException, IOException {
        final DB db = DB.getInstance(context);
        try {
            int sync_days = jargs.getInt(0);
            int keep_days = jargs.getInt(1);
            boolean download = jargs.getBoolean(2);

            if (keep_days == sync_days)
                keep_days++;

            Log.i(folder.name + " start sync after=" + sync_days + "/" + keep_days);

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
            int old = db.message().deleteMessagesBefore(folder.id, keep_time);
            Log.i(folder.name + " local old=" + old);

            // Get list of local uids
            final List<Long> uids = db.message().getUids(folder.id, null);
            Log.i(folder.name + " local count=" + uids.size());

            // Reduce list of local uids
            SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.GE, new Date(sync_time));
            if (ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED))
                searchTerm = new OrTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

            long search = SystemClock.elapsedRealtime();
            Message[] imessages = ifolder.search(searchTerm);
            Log.i(folder.name + " remote count=" + imessages.length +
                    " search=" + (SystemClock.elapsedRealtime() - search) + " ms");

            FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(FetchProfile.Item.FLAGS);
            ifolder.fetch(imessages, fp);

            long fetch = SystemClock.elapsedRealtime();
            Log.i(folder.name + " remote fetched=" + (SystemClock.elapsedRealtime() - fetch) + " ms");

            for (int i = 0; i < imessages.length && state.running(); i++)
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
                        Log.i("Executing uid fetch count=" + uids.size());
                        Response[] responses = protocol.command(
                                "UID FETCH " + TextUtils.join(",", uids) + " (UID)", null);

                        if (responses.length > 0 && responses[responses.length - 1].isOK()) {
                            for (Response response : responses)
                                if (response instanceof FetchResponse) {
                                    FetchResponse fr = (FetchResponse) response;
                                    UID uid = fr.getItem(UID.class);
                                    if (uid != null)
                                        uids.remove(uid.uid);
                                }
                            return null;
                        } else {
                            for (Response response : responses)
                                if (response.isNO() || response.isBAD() || response.isBYE())
                                    return new MessagingException(response.toString());
                            return new MessagingException("UID FETCH failed");
                        }
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
            for (int i = imessages.length - 1; i >= 0 && state.running(); i -= SYNC_BATCH_SIZE) {
                int from = Math.max(0, i - SYNC_BATCH_SIZE + 1);
                Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);

                // Full fetch new/changed messages only
                List<Message> full = new ArrayList<>();
                for (Message imessage : isub) {
                    long uid = ifolder.getUID(imessage);
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

                for (int j = isub.length - 1; j >= 0 && state.running(); j--)
                    try {
                        db.beginTransaction();
                        EntityMessage message = synchronizeMessage(
                                context,
                                folder, ifolder, (IMAPMessage) isub[j],
                                false,
                                rules);
                        ids[from + j] = message.id;
                        db.setTransactionSuccessful();
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
                        db.endTransaction();
                        // Reduce memory usage
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
                    EntityOperation.queue(context, db, orphan, EntityOperation.ADD);
                }
            }

            int count = ifolder.getMessageCount();
            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

            if (download) {
                db.folder().setFolderSyncState(folder.id, "downloading");

                //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);

                // Download messages/attachments
                Log.i(folder.name + " download=" + imessages.length);
                for (int i = imessages.length - 1; i >= 0 && state.running(); i -= DOWNLOAD_BATCH_SIZE) {
                    int from = Math.max(0, i - DOWNLOAD_BATCH_SIZE + 1);

                    Message[] isub = Arrays.copyOfRange(imessages, from, i + 1);
                    // Fetch on demand

                    for (int j = isub.length - 1; j >= 0 && state.running(); j--)
                        try {
                            if (ids[from + j] != null)
                                downloadMessage(
                                        context,
                                        folder, ifolder,
                                        (IMAPMessage) isub[j], ids[from + j], state);
                        } catch (FolderClosedException ex) {
                            throw ex;
                        } catch (FolderClosedIOException ex) {
                            throw ex;
                        } catch (Throwable ex) {
                            Log.e(folder.name, ex);
                        } finally {
                            // Free memory
                            ((IMAPMessage) isub[j]).invalidateHeaders();
                        }
                }
            }

            if (state.running)
                db.folder().setFolderInitialized(folder.id);

            db.folder().setFolderSync(folder.id, new Date().getTime());
            db.folder().setFolderError(folder.id, null);

        } finally {
            Log.i(folder.name + " end sync state=" + state);
            db.folder().setFolderSyncState(folder.id, null);
        }
    }

    static EntityMessage synchronizeMessage(
            Context context,
            EntityFolder folder, IMAPFolder ifolder, IMAPMessage imessage,
            boolean browsed,
            List<EntityRule> rules) throws MessagingException, IOException {
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
                    String thread = helper.getThreadId(uid);
                    Log.i(folder.name + " found as id=" + dup.id +
                            " uid=" + dup.uid + "/" + uid +
                            " msgid=" + msgid + " thread=" + thread);
                    dup.folder = folder.id; // outbox to sent

                    if (dup.uid == null) {
                        Log.i(folder.name + " set uid=" + uid);
                        dup.uid = uid;
                        filter = true;
                    } else
                        Log.w(folder.name + " changed uid=" + dup.uid + " -> " + uid);

                    dup.msgid = msgid;
                    dup.thread = thread;
                    dup.error = null;
                    db.message().updateMessage(dup);
                    message = dup;
                }
            }

            if (message == null)
                filter = true;
        }

        if (message == null) {
            Address[] froms = helper.getFrom();
            Address[] tos = helper.getTo();
            Address[] ccs = helper.getCc();
            String delivered = helper.getDeliveredTo();

            // Build ordered list of addresses
            List<Address> addresses = new ArrayList<>();
            if (delivered != null)
                addresses.add(new InternetAddress(delivered));
            if (tos != null)
                addresses.addAll(Arrays.asList(tos));
            if (ccs != null)
                addresses.addAll(Arrays.asList(ccs));
            if (froms != null)
                addresses.addAll(Arrays.asList(froms));

            // Search for matching identity
            EntityIdentity identity = null;
            for (Address address : addresses) {
                String email = ((InternetAddress) address).getAddress();
                if (!TextUtils.isEmpty(email)) {
                    identity = db.identity().getIdentity(folder.account, email);
                    if (identity == null) {
                        String canonical = Helper.canonicalAddress(email);
                        if (!canonical.equals(email))
                            identity = db.identity().getIdentity(folder.account, canonical);
                    }
                    if (identity != null)
                        break;
                }
            }

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
            message.deliveredto = delivered;
            message.thread = helper.getThreadId(uid);
            message.from = froms;
            message.to = tos;
            message.cc = ccs;
            message.bcc = helper.getBcc();
            message.reply = helper.getReply();
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

            Address sender = helper.getSender(); // header
            if (sender != null) {
                String[] s = ((InternetAddress) sender).getAddress().split("@");
                String[] f = (froms == null || froms.length == 0 ? null
                        : (((InternetAddress) froms[0]).getAddress()).split("@"));
                if (s.length > 1 && (f == null || (f.length > 1 && !s[1].equals(f[1]))))
                    message.warning = context.getString(R.string.title_via, s[1]);
            }

            message.id = db.message().insertMessage(message);

            Log.i(folder.name + " added id=" + message.id + " uid=" + message.uid);

            int sequence = 1;
            MessageHelper.MessageParts parts = helper.getMessageParts();
            for (EntityAttachment attachment : parts.getAttachments()) {
                Log.i(folder.name + " attachment seq=" + sequence +
                        " name=" + attachment.name + " type=" + attachment.type +
                        " cid=" + attachment.cid + " pgp=" + attachment.encryption);
                attachment.message = message.id;
                attachment.sequence = sequence++;
                attachment.id = db.attachment().insertAttachment(attachment);
            }

            if (!EntityFolder.ARCHIVE.equals(folder.type) &&
                    !EntityFolder.TRASH.equals(folder.type) &&
                    !EntityFolder.JUNK.equals(folder.type)) {
                int type = (folder.isOutgoing() ? EntityContact.TYPE_TO : EntityContact.TYPE_FROM);
                Address[] recipients = (type == EntityContact.TYPE_TO
                        ? message.to
                        : (message.reply != null ? message.reply : message.from));
                if (recipients != null) {
                    // Check if from self
                    if (type == EntityContact.TYPE_FROM) {
                        boolean me = true;
                        for (Address reply : recipients) {
                            String email = ((InternetAddress) reply).getAddress();
                            String canonical = Helper.canonicalAddress(email);
                            if (!TextUtils.isEmpty(email) &&
                                    db.identity().getIdentity(folder.account, email.toLowerCase()) == null &&
                                    (canonical.equals(email) ||
                                            db.identity().getIdentity(folder.account, canonical) == null)) {
                                me = false;
                                break;
                            }
                        }
                        if (me)
                            recipients = message.to;
                    }

                    for (Address recipient : recipients) {
                        String email = ((InternetAddress) recipient).getAddress();
                        String name = ((InternetAddress) recipient).getPersonal();
                        Uri avatar = ContactInfo.getLookupUri(context, new Address[]{recipient});
                        EntityContact contact = db.contact().getContact(type, email);
                        if (contact == null) {
                            contact = new EntityContact();
                            contact.type = type;
                            contact.email = email;
                            contact.name = name;
                            contact.avatar = (avatar == null ? null : avatar.toString());
                            contact.times_contacted = 1;
                            contact.last_contacted = message.received;
                            contact.id = db.contact().insertContact(contact);
                            Log.i("Inserted contact=" + contact + " type=" + type);
                        } else {
                            contact.name = name;
                            contact.avatar = (avatar == null ? null : avatar.toString());
                            contact.times_contacted++;
                            contact.last_contacted = message.received;
                            db.contact().updateContact(contact);
                            Log.i("Updated contact=" + contact + " type=" + type);
                        }
                    }
                }
            }
        } else {
            boolean update = false;

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

            if (message.avatar == null) {
                Uri lookupUri = ContactInfo.getLookupUri(context, message.from);
                if (lookupUri != null) {
                    update = true;
                    message.avatar = lookupUri.toString();
                    Log.i(folder.name + " updated id=" + message.id + " lookup=" + lookupUri);
                }
            }

            if (update)
                db.message().updateMessage(message);
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

        if (filter && Helper.isPro(context))
            try {
                for (EntityRule rule : rules)
                    if (rule.matches(context, message, imessage)) {
                        rule.execute(context, db, message);
                        if (rule.stop)
                            break;
                    }
            } catch (Throwable ex) {
                Log.e(ex);
                db.message().setMessageError(message.id, Helper.formatThrowable(ex));
            }

        return message;
    }

    static void downloadMessage(
            Context context,
            EntityFolder folder, IMAPFolder ifolder,
            IMAPMessage imessage, long id, State state) throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        EntityMessage message = db.message().getMessage(id);
        if (message == null)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long maxSize = prefs.getInt("download", 32768);
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
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            ifolder.fetch(new Message[]{imessage}, fp);

            MessageHelper.MessageParts parts = helper.getMessageParts();

            if (!message.content) {
                if (state.getNetworkState().isUnmetered() || (message.size != null && message.size < maxSize)) {
                    String body = parts.getHtml(context);
                    Helper.writeText(message.getFile(context), body);
                    db.message().setMessageContent(message.id, true,
                            HtmlHelper.getPreview(body), parts.getWarnings(message.warning));
                    Log.i(folder.name + " downloaded message id=" + message.id + " size=" + message.size);
                }
            }

            for (EntityAttachment attachment : attachments)
                if (!attachment.available)
                    if (state.getNetworkState().isUnmetered() || (attachment.size != null && attachment.size < maxSize))
                        try {
                            parts.downloadAttachment(context, attachment.sequence - 1, attachment.id);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
        }
    }

    static void notifyMessages(Context context, List<TupleMessageEx> messages) {
        Log.i("Notify messages=" + messages.size());

        Widget.update(context, messages.size());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Map<String, List<Long>> groupNotifying = new HashMap<>();
        Map<String, List<TupleMessageEx>> groupMessages = new HashMap<>();

        // Previous
        for (String key : prefs.getAll().keySet())
            if (key.startsWith("notifying:")) {
                String group = key.substring(key.indexOf(":") + 1);
                groupNotifying.put(group, new ArrayList<Long>());

                for (String id : prefs.getString(key, null).split(","))
                    groupNotifying.get(group).add(Long.parseLong(id));

                Log.i("Notifying " + group + "=" + TextUtils.join(",", groupNotifying.get(group)));

                editor.remove(key);
            }

        // Current
        for (TupleMessageEx message : messages) {
            String group = Long.toString(message.accountNotify ? message.account : 0);

            if (!groupMessages.containsKey(group)) {
                groupMessages.put(group, new ArrayList<TupleMessageEx>());
                if (!groupNotifying.containsKey(group))
                    groupNotifying.put(group, new ArrayList<Long>());
            }

            groupMessages.get(group).add(message);
        }

        // Difference
        for (String group : groupNotifying.keySet()) {
            List<Notification> notifications = getNotificationUnseen(context, group, groupMessages.get(group));

            List<String> all = new ArrayList<>();
            List<Long> add = new ArrayList<>();
            List<Long> remove = groupNotifying.get(group);
            for (Notification notification : notifications) {
                Long id = notification.extras.getLong("id", 0);
                if (id != 0) {
                    all.add(Long.toString(id));
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
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && headers > 0))
                nm.cancel("unseen." + group + ":0", 1);

            for (Long id : remove)
                nm.cancel("unseen." + group + ":" + Math.abs(id), 1);

            for (Notification notification : notifications) {
                long id = notification.extras.getLong("id", 0);
                if ((id == 0 && add.size() + remove.size() > 0) || add.contains(id))
                    nm.notify("unseen." + group + ":" + Math.abs(id), 1, notification);
            }

            if (all.size() > 0)
                editor.putString("notifying:" + group, TextUtils.join(",", all));
        }

        editor.apply();
    }

    private static List<Notification> getNotificationUnseen(Context context, String group, List<TupleMessageEx> messages) {
        List<Notification> notifications = new ArrayList<>();
        // https://developer.android.com/training/notify-user/group

        if (messages == null || messages.size() == 0)
            return notifications;

        boolean pro = Helper.isPro(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Get contact info
        Map<TupleMessageEx, ContactInfo> messageContact = new HashMap<>();
        for (TupleMessageEx message : messages)
            messageContact.put(message, ContactInfo.get(context, message.from, false));

        // Build pending intents
        Intent summary = new Intent(context, ActivityView.class);
        summary.setAction("unified");
        PendingIntent piSummary = PendingIntent.getActivity(context, ActivityView.REQUEST_UNIFIED, summary, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clear = new Intent(context, ServiceUI.class);
        clear.setAction("clear");
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
            boolean light = prefs.getBoolean("light", false);
            String sound = prefs.getString("sound", null);

            if (light)
                builder.setLights(Color.GREEN, 1000, 1000);

            if (sound == null) {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(uri);
            } else
                builder.setSound(Uri.parse(sound));

            builder.setOnlyAlertOnce(true);
        } else
            builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

        if (pro) {
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
        }

        notifications.add(builder.build());

        boolean preview = prefs.getBoolean("notify_preview", true);
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

            Intent ignored = new Intent(context, ServiceUI.class);
            ignored.setAction("ignore:" + message.id);
            PendingIntent piDelete = PendingIntent.getService(context, ServiceUI.PI_IGNORED, ignored, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent seen = new Intent(context, ServiceUI.class);
            seen.setAction("seen:" + message.id);
            PendingIntent piSeen = PendingIntent.getService(context, ServiceUI.PI_SEEN, seen, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent archive = new Intent(context, ServiceUI.class);
            archive.setAction("archive:" + message.id);
            PendingIntent piArchive = PendingIntent.getService(context, ServiceUI.PI_ARCHIVE, archive, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent trash = new Intent(context, ServiceUI.class);
            trash.setAction("trash:" + message.id);
            PendingIntent piTrash = PendingIntent.getService(context, ServiceUI.PI_TRASH, trash, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build actions
            NotificationCompat.Action.Builder actionSeen = new NotificationCompat.Action.Builder(
                    R.drawable.baseline_visibility_24,
                    context.getString(R.string.title_action_seen),
                    piSeen);

            NotificationCompat.Action.Builder actionArchive = new NotificationCompat.Action.Builder(
                    R.drawable.baseline_archive_24,
                    context.getString(R.string.title_action_archive),
                    piArchive);

            NotificationCompat.Action.Builder actionTrash = new NotificationCompat.Action.Builder(
                    R.drawable.baseline_delete_24,
                    context.getString(R.string.title_action_trash),
                    piTrash);

            // Get channel name
            String channelName = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O &&
                    message.from != null && message.from.length > 0) {
                InternetAddress from = (InternetAddress) message.from[0];
                NotificationChannel channel = nm.getNotificationChannel("notification." + from.getAddress().toLowerCase());
                if (channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE)
                    channelName = channel.getId();
            }
            if (channelName == null)
                channelName = EntityAccount.getNotificationChannelName(message.accountNotify ? message.account : 0);

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
                    .setDeleteIntent(piDelete)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setGroup(group)
                    .setGroupSummary(false)
                    .setOnlyAlertOnce(true)
                    .addAction(actionSeen.build())
                    .addAction(actionArchive.build())
                    .addAction(actionTrash.build());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                mbuilder.setSound(null);

            if (pro) {
                if (!TextUtils.isEmpty(message.subject))
                    mbuilder.setContentText(message.subject);

                if (message.content && preview)
                    try {
                        String body = Helper.readText(message.getFile(context));
                        StringBuilder sb = new StringBuilder();
                        if (!TextUtils.isEmpty(message.subject))
                            sb.append(message.subject).append("<br>");
                        String text = Jsoup.parse(body).text();
                        if (!TextUtils.isEmpty(text)) {
                            sb.append("<em>");
                            if (text.length() > HtmlHelper.PREVIEW_SIZE) {
                                sb.append(text.substring(0, HtmlHelper.PREVIEW_SIZE));
                                sb.append("…");
                            } else
                                sb.append(text);
                            sb.append("</em>");
                        }
                        mbuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(HtmlHelper.fromHtml(sb.toString())));
                    } catch (IOException ex) {
                        Log.e(ex);
                        mbuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(ex.toString()));
                    }

                if (info.hasPhoto())
                    mbuilder.setLargeIcon(info.getPhotoBitmap());

                if (info.hasLookupUri())
                    mbuilder.addPerson(info.getLookupUri().toString());

                if (message.accountColor != null) {
                    mbuilder.setColor(message.accountColor);
                    mbuilder.setColorized(true);
                }
            }

            mbuilder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            notifications.add(mbuilder.build());
        }

        return notifications;
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

        if ((ex instanceof SendFailedException) || (ex instanceof AlertException)) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(tag, 1, getNotificationError(context, title, ex).build());
        }

        // connection failure: Too many simultaneous connections

        if (BuildConfig.DEBUG &&
                !(ex instanceof SendFailedException) &&
                !(ex instanceof MailConnectException) &&
                !(ex instanceof FolderClosedException) &&
                !(ex instanceof IllegalStateException) &&
                !(ex instanceof AuthenticationFailedException) && // Also: Too many simultaneous connections
                !(ex instanceof StoreClosedException) &&
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
        Intent intent = new Intent(context, ActivitySetup.class);
        if (debug)
            intent.setAction("error");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                context, ActivitySetup.REQUEST_ERROR, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        private Helper.NetworkState networkState;
        private Thread thread;
        private Semaphore semaphore = new Semaphore(0);
        private boolean running = true;

        State(Helper.NetworkState networkState) {
            this.networkState = networkState;
        }

        State(State parent) {
            this(parent.networkState);
        }

        Helper.NetworkState getNetworkState() {
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

        void error() {
            thread.interrupt();
            yield();
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
            return "[running=" + running + "]";
        }
    }
}
