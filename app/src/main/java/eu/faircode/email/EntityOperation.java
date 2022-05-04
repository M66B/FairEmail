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

import static androidx.room.ForeignKey.CASCADE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(
        tableName = EntityOperation.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "folder", entity = EntityFolder.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "message", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account"}),
                @Index(value = {"folder"}),
                @Index(value = {"message"}),
                @Index(value = {"name"}),
                @Index(value = {"state"})
        }
)
public class EntityOperation {
    static final String TABLE_NAME = "operation";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long account; // performance
    @NonNull
    public Long folder;
    public Long message;
    @NonNull
    public String name;
    @NonNull
    public String args;
    @NonNull
    public Long created;
    @NonNull
    public int tries = 0;
    public String state;
    public String error;

    static final String ADD = "add";
    static final String MOVE = "move";
    static final String COPY = "copy";
    static final String FETCH = "fetch";
    static final String DELETE = "delete";
    static final String SEEN = "seen";
    static final String ANSWERED = "answered";
    static final String FLAG = "flag";
    static final String KEYWORD = "keyword";
    static final String LABEL = "label"; // Gmail
    static final String HEADERS = "headers";
    static final String RAW = "raw";
    static final String BODY = "body";
    static final String ATTACHMENT = "attachment";
    static final String SYNC = "sync";
    static final String SUBSCRIBE = "subscribe";
    static final String SEND = "send";
    static final String EXISTS = "exists";
    static final String RULE = "rule";
    static final String PURGE = "purge";
    static final String EXPUNGE = "expunge";
    static final String REPORT = "report";

    private static final int MAX_FETCH = 100; // operations
    private static final long FORCE_WITHIN = 30 * 1000; // milliseconds

    static void queue(Context context, EntityMessage message, String name, Object... values) {
        DB db = DB.getInstance(context);

        try {
            JSONArray jargs = new JSONArray();
            for (Object value : values)
                jargs.put(value);

            if (SEEN.equals(name)) {
                boolean seen = jargs.getBoolean(0);
                boolean ignore = jargs.optBoolean(1, true);
                for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid))
                    if (similar.ui_seen != seen || similar.ui_ignored != ignore) {
                        db.message().setMessageUiSeen(similar.id, seen);
                        db.message().setMessageUiIgnored(similar.id, ignore);
                        queue(context, similar.account, similar.folder, similar.id, name, jargs);
                    }
                return;

            } else if (FLAG.equals(name)) {
                boolean flagged = jargs.getBoolean(0);
                Integer color = (jargs.length() > 1 && !jargs.isNull(1) ? jargs.getInt(1) : null);
                for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid))
                    if (similar.ui_flagged != flagged || !Objects.equals(similar.color, color)) {
                        db.message().setMessageUiFlagged(similar.id, flagged, flagged ? color : null);
                        queue(context, similar.account, similar.folder, similar.id, name, jargs);
                    }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean auto_important = prefs.getBoolean("auto_important", false);
                if (auto_important) {
                    db.message().setMessageImportance(message.id, flagged ? EntityMessage.PRIORITIY_HIGH : null);
                    queue(context, message, KEYWORD, MessageHelper.FLAG_LOW_IMPORTANCE, false);
                    queue(context, message, KEYWORD, MessageHelper.FLAG_HIGH_IMPORTANCE, true);
                }

                return;

            } else if (ANSWERED.equals(name)) {
                for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid)) {
                    db.message().setMessageUiAnswered(similar.id, jargs.getBoolean(0));
                    queue(context, similar.account, similar.folder, similar.id, name, jargs);
                }
                return;

            } else if (KEYWORD.equals(name)) {
                String keyword = jargs.getString(0);
                boolean set = jargs.getBoolean(1);

                List<String> keywords = new ArrayList<>(Arrays.asList(message.keywords));
                if (set == keywords.contains(keyword))
                    return;

                while (keywords.remove(keyword))
                    ;
                if (set)
                    keywords.add(keyword);

                Collections.sort(keywords);

                message.keywords = keywords.toArray(new String[0]);
                db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(message.keywords));

            } else if (LABEL.equals(name)) {
                String label = jargs.getString(0);
                boolean set = jargs.getBoolean(1);

                if (message.setLabel(label, set))
                    db.message().setMessageLabels(message.id, DB.Converters.fromStringArray(message.labels));

            } else if (MOVE.equals(name)) {
                // Parameters in:
                // 0: target folder
                // 1: mark seen
                // 2: auto classified

                // Parameters out:
                // 0: target folder
                // 1: mark seen
                // 2: temporary message
                // 3: remove flag
                // 4: permanently delete

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean autoread = prefs.getBoolean("autoread", false);
                boolean autounflag = prefs.getBoolean("autounflag", false);
                boolean reset_importance = prefs.getBoolean("reset_importance", false);

                if (jargs.opt(1) != null) {
                    // rules, classify
                    autoread = jargs.getBoolean(1);
                    autounflag = false;
                }

                boolean auto_classified = false;
                if (jargs.opt(2) != null) {
                    auto_classified = jargs.getBoolean(2);
                    jargs.remove(2);
                }

                EntityFolder source = db.folder().getFolder(message.folder);
                EntityFolder target = db.folder().getFolder(jargs.getLong(0));
                if (source == null || target == null || source.id.equals(target.id))
                    return;

                if (EntityFolder.DRAFTS.equals(source.type) &&
                        EntityFolder.TRASH.equals(target.type))
                    autoread = true;

                if (EntityFolder.JUNK.equals(source.type) &&
                        EntityFolder.INBOX.equals(target.type))
                    autoread = false;

                jargs.put(1, autoread);
                jargs.put(3, autounflag);

                EntityLog.log(context, EntityLog.Type.General, message,
                        "Move message=" + message.id +
                                "@" + new Date(message.received) +
                                ":" + message.subject +
                                " source=" + source.id + ":" + source.type + ":" + source.name + "" +
                                " target=" + target.id + ":" + target.type + ":" + target.name +
                                " auto read=" + autoread + " flag=" + autounflag + " importance=" + reset_importance);

                if (autoread || autounflag || reset_importance)
                    for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid)) {
                        if (autoread)
                            db.message().setMessageUiSeen(similar.id, true);
                        if (autounflag)
                            db.message().setMessageUiFlagged(similar.id, false, null);
                        if (reset_importance) {
                            db.message().setMessageImportance(similar.id, null);
                            queue(context, similar, KEYWORD, MessageHelper.FLAG_LOW_IMPORTANCE, false);
                            queue(context, similar, KEYWORD, MessageHelper.FLAG_HIGH_IMPORTANCE, false);
                        }
                    }

                if (message.ui_found)
                    db.message().setMessageFound(message.id, false);

                if (source.account.equals(target.account)) {
                    EntityAccount account = db.account().getAccount(message.account);
                    if ((account != null && !account.isGmail()) ||
                            !EntityFolder.ARCHIVE.equals(source.type) ||
                            EntityFolder.TRASH.equals(target.type) || EntityFolder.JUNK.equals(target.type))
                        if (!message.ui_deleted)
                            db.message().setMessageUiHide(message.id, true);

                    if (account != null && account.isGmail() &&
                            EntityFolder.ARCHIVE.equals(source.type) &&
                            !(EntityFolder.TRASH.equals(target.type) || EntityFolder.JUNK.equals(target.type)))
                        name = COPY;
                }

                if (message.ui_snoozed != null &&
                        (EntityFolder.ARCHIVE.equals(target.type) ||
                                EntityFolder.TRASH.equals(target.type) ||
                                EntityFolder.JUNK.equals(target.type))) {
                    message.ui_snoozed = null;
                    EntityMessage.snooze(context, message.id, null);
                }

                if (EntityFolder.JUNK.equals(source.type)) {
                    List<EntityRule> rules = db.rule().getRules(target.id);
                    for (EntityRule rule : rules)
                        if (rule.isBlockingSender(message, source))
                            db.rule().deleteRule(rule.id);

                    EntityContact.delete(context, message.account, message.from,
                            EntityContact.TYPE_JUNK);
                    EntityContact.update(context, message.account, message.identity, message.from,
                            EntityContact.TYPE_NO_JUNK, message.received);
                }

                if (EntityFolder.JUNK.equals(target.type))
                    EntityContact.delete(context, message.account, message.from, EntityContact.TYPE_NO_JUNK);

                // Create copy without uid in target folder
                // Message with same msgid can be in archive
                if (message.uid != null &&
                        !TextUtils.isEmpty(message.msgid) &&
                        db.message().countMessageByMsgId(target.id, message.msgid) == 0) {
                    File msource = message.getFile(context);

                    // Copy message to target folder
                    long _id = message.id;
                    Long _identity = message.identity;
                    long _uid = message.uid;
                    Boolean _raw = message.raw;
                    Long _stored = message.stored;
                    int _notifying = message.notifying;
                    boolean _fts = message.fts;
                    boolean _auto_classified = message.auto_classified;
                    Integer _importance = message.importance;
                    boolean _seen = message.seen;
                    boolean _flagged = message.flagged;
                    boolean _ui_seen = message.ui_seen;
                    boolean _ui_flagged = message.ui_flagged;
                    boolean _ui_hide = message.ui_hide;
                    boolean _ui_found = message.ui_found;
                    boolean _ui_browsed = message.ui_browsed;
                    Long ui_busy = message.ui_busy;
                    Integer _color = message.color;
                    String _error = message.error;

                    message.id = null;
                    message.account = target.account;
                    message.folder = target.id;
                    message.identity = null;
                    message.uid = null;
                    message.raw = null;
                    message.stored = new Date().getTime();
                    message.notifying = 0;
                    message.fts = false;
                    message.auto_classified = auto_classified;
                    if (reset_importance)
                        message.importance = null;
                    if (autoread) {
                        message.seen = true;
                        message.ui_seen = true;
                    }
                    if (autounflag) {
                        message.flagged = false;
                        message.ui_flagged = false;
                        message.color = null;
                    }
                    message.ui_hide = false;
                    message.ui_found = false;
                    message.ui_browsed = false;
                    message.ui_busy = null;
                    message.error = null;
                    message.id = db.message().insertMessage(message);

                    File mtarget = message.getFile(context);
                    long tmpid = message.id;
                    jargs.put(2, tmpid);

                    message.id = _id;
                    message.account = source.account;
                    message.folder = source.id;
                    message.identity = _identity;
                    message.uid = _uid;
                    message.raw = _raw;
                    message.stored = _stored;
                    message.notifying = _notifying;
                    message.fts = _fts;
                    message.auto_classified = _auto_classified;
                    message.importance = _importance;
                    message.seen = _seen;
                    message.flagged = _flagged;
                    message.ui_seen = _ui_seen;
                    message.ui_flagged = _ui_flagged;
                    message.ui_hide = _ui_hide;
                    message.ui_found = _ui_found;
                    message.ui_browsed = _ui_browsed;
                    message.ui_busy = ui_busy;
                    message.color = _color;
                    message.error = _error;

                    if (message.content)
                        try {
                            Helper.copy(msource, mtarget);
                        } catch (IOException ex) {
                            Log.e(ex);
                            db.message().resetMessageContent(tmpid);
                        }

                    EntityAttachment.copy(context, message.id, tmpid);

                    if (message.ui_snoozed != null)
                        EntityMessage.snooze(context, tmpid, message.ui_snoozed);
                }

                // Cross account move
                if (source.account.equals(target.account))
                    queue(context, message.account, source.id, message.id, name, jargs);
                else {
                    if (message.raw != null && message.raw)
                        queue(context, target.account, target.id, message.id, ADD, jargs);
                    else
                        queue(context, source.account, source.id, message.id, RAW, jargs);
                }

                return;
            } else if (COPY.equals(name)) {
                // Parameters in:
                // 0: target folder
                // 1: mark seen

                EntityFolder source = db.folder().getFolder(message.folder);
                EntityFolder target = db.folder().getFolder(jargs.getLong(0));
                if (source == null || target == null)
                    return;

                // Cross account copy
                if (!source.account.equals(target.account)) {
                    jargs.put(2, true); // copy
                    if (message.raw != null && message.raw)
                        queue(context, target.account, target.id, message.id, ADD, jargs);
                    else
                        queue(context, source.account, source.id, message.id, RAW, jargs);
                    return;
                }

            } else if (DELETE.equals(name)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean perform_expunge = prefs.getBoolean("perform_expunge", true);

                EntityAccount account = db.account().getAccount(message.account);

                if (perform_expunge ||
                        account == null ||
                        account.protocol != EntityAccount.TYPE_IMAP) {
                    message.ui_hide = true;
                    db.message().setMessageUiHide(message.id, message.ui_hide);

                    if (perform_expunge && account != null && account.isGmail()) {
                        EntityFolder source = db.folder().getFolder(message.folder);
                        if (source != null && EntityFolder.ARCHIVE.equals(source.type)) {
                            EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                            if (trash != null && !trash.id.equals(message.folder)) {
                                jargs.put(0, trash.id); // target
                                jargs.put(4, true); // delete
                                queue(context, message.account, message.folder, message.id, EntityOperation.MOVE, jargs);
                                return;
                            }
                        }
                    }
                } else {
                    message.ui_deleted = !message.ui_deleted;
                    db.message().setMessageUiDeleted(message.id, message.ui_deleted);
                    if (message.ui_deleted) {
                        message.ui_ignored = true;
                        db.message().setMessageUiIgnored(message.id, message.ui_ignored);
                    }
                }
/*
                if (message.hash != null) {
                    List<EntityMessage> sames = db.message().getMessagesByHash(message.account, message.hash);
                    for (EntityMessage same : sames) {
                        db.message().setMessageUiHide(same.id, true);
                        queue(context, same.account, same.folder, same.id, name, jargs);
                    }
                }
*/
            } else if (ATTACHMENT.equals(name))
                db.attachment().setProgress(jargs.getLong(0), 0);

            queue(context, message.account, message.folder, message.id, name, jargs);

        } catch (JSONException ex) {
            Log.e(ex);
        }
    }

    static void queue(Context context, EntityFolder folder, String name, Object... values) {
        JSONArray jargs = new JSONArray();
        for (Object value : values)
            jargs.put(value);

        queue(context, folder.account, folder.id, null, name, jargs);
    }

    private static void queue(Context context, Long account, long folder, Long message, String name, JSONArray jargs) {
        DB db = DB.getInstance(context);

        if (FETCH.equals(name)) {
            int count = db.operation().getOperationCount(folder, name);
            if (count >= MAX_FETCH) {
                Log.i("Replacing fetch by sync folder=" + folder + " args=" + jargs + " count=" + count);
                sync(context, folder, false, false);
                return;
            }
        }

        EntityOperation op = new EntityOperation();
        op.account = account;
        op.folder = folder;
        op.message = message;
        op.name = name;
        op.args = jargs.toString();
        op.created = new Date().getTime();
        op.id = db.operation().insertOperation(op);

        Log.i("Queued op=" + op.id + "/" + op.name +
                " folder=" + op.folder + " msg=" + op.message +
                " args=" + op.args);

        Map<String, String> crumb = new HashMap<>();
        crumb.put("name", op.name);
        crumb.put("args", op.args);
        crumb.put("folder", op.account + ":" + op.folder);
        if (op.message != null)
            crumb.put("message", Long.toString(op.message));
        Log.breadcrumb("queued", crumb);
    }

    static void poll(Context context, long fid) throws JSONException {
        DB db = DB.getInstance(context);

        boolean force = false;
        List<EntityOperation> ops = db.operation().getOperationsByFolder(fid, SYNC);
        if (ops != null)
            for (EntityOperation op : ops)
                if (EntityFolder.isSyncForced(op.args)) {
                    force = true;
                    break;
                }

        int count = db.operation().deleteOperation(fid, SYNC);

        Map<String, String> crumb = new HashMap<>();
        crumb.put("folder", Long.toString(fid));
        crumb.put("stale", Integer.toString(count));
        crumb.put("force", Boolean.toString(force));
        Log.breadcrumb("Poll", crumb);

        sync(context, fid, false, force);
    }

    static void sync(Context context, long fid, boolean foreground) {
        sync(context, fid, foreground, false);
    }

    static void sync(Context context, long fid, boolean foreground, boolean force) {
        sync(context, fid, foreground, force, false);
    }

    static void sync(Context context, long fid, boolean foreground, boolean force, boolean outbox) {
        DB db = DB.getInstance(context);

        EntityFolder folder = db.folder().getFolder(fid);
        if (folder == null)
            return;

        if (foreground) {
            long now = new Date().getTime();
            if (folder.last_sync_foreground != null &&
                    now - folder.last_sync_foreground < FORCE_WITHIN) {
                Log.i(folder.name + " Auto force");
                force = true;
            }
            db.folder().setFolderLastSyncForeground(folder.id, now);
        }

        if (force)
            db.operation().deleteOperation(fid, SYNC);

        // TODO: replace sync parameters?
        if (db.operation().getOperationCount(fid, SYNC) == 0) {
            EntityOperation operation = new EntityOperation();
            operation.account = folder.account;
            operation.folder = folder.id;
            operation.message = null;
            operation.name = SYNC;
            operation.args = folder.getSyncArgs(force).toString();
            operation.created = new Date().getTime();
            operation.id = db.operation().insertOperation(operation);

            Log.i("Queued sync folder=" + folder + " force=" + force);
        }

        if (foreground && folder.sync_state == null) // Show spinner
            db.folder().setFolderSyncState(fid, "requested");

        if (foreground && EntityFolder.SENT.equals(folder.type)) {
            EntityAccount account = db.account().getAccount(folder.account);
            if (account.protocol == EntityAccount.TYPE_IMAP) {
                List<EntityMessage> orphans = db.message().getSentOrphans(folder.id);
                if (orphans != null) {
                    EntityLog.log(context, "Sent orphans=" + orphans.size());
                    for (EntityMessage orphan : orphans)
                        EntityOperation.queue(context, orphan, EntityOperation.EXISTS);
                }
            }
        }

        if (folder.account == null) // Outbox
            if (!outbox) {
                Log.e("outbox");
                ServiceSend.start(context);
            }
    }

    static void subscribe(Context context, long fid, boolean subscribe) {
        DB db = DB.getInstance(context);

        EntityFolder folder = db.folder().getFolder(fid);

        JSONArray jargs = new JSONArray();
        jargs.put(subscribe);

        EntityOperation operation = new EntityOperation();
        operation.account = folder.account;
        operation.folder = folder.id;
        operation.message = null;
        operation.name = SUBSCRIBE;
        operation.args = jargs.toString();
        operation.created = new Date().getTime();
        operation.id = db.operation().insertOperation(operation);

        Log.i("Queued subscribe=" + subscribe + " folder=" + folder);
    }

    void cleanup(Context context, boolean fetch) {
        DB db = DB.getInstance(context);

        EntityLog.log(context, "Cleanup op=" + id + "/" + name + " folder=" + folder + " message=" + message);

        if (message != null) {
            db.message().setMessageUiHide(message, false);

            if (SEEN.equals(name)) {
                EntityMessage m = db.message().getMessage(message);
                if (m != null)
                    db.message().setMessageUiSeen(m.id, m.seen);
            }

            if (FLAG.equals(name)) {
                EntityMessage m = db.message().getMessage(message);
                if (m != null)
                    db.message().setMessageUiFlagged(m.id, m.flagged, m.color);
            }
        }

        if (MOVE.equals(name)) {
            int count = db.operation().deleteOperation(folder, PURGE);
            if (count > 0)
                sync(context, folder, false);
        }

        if (MOVE.equals(name) ||
                ADD.equals(name) ||
                RAW.equals(name))
            try {
                JSONArray jargs = new JSONArray(args);
                long tmpid = jargs.optLong(2, -1);
                if (tmpid < 0)
                    return;

                db.message().deleteMessage(tmpid);
            } catch (JSONException ex) {
                Log.e(ex);
            }

        if (EXISTS.equals(name)) {
            EntityMessage m = db.message().getMessage(message);
            if (m != null)
                queue(context, m, ADD);
        }

        if (ATTACHMENT.equals(name))
            try {
                JSONArray jargs = new JSONArray(args);
                long id = jargs.getLong(0);
                db.attachment().setProgress(id, null);
                db.attachment().setError(id, error);
                return;
            } catch (JSONException ex) {
                Log.e(ex);
            }

        if (SYNC.equals(name))
            db.folder().setFolderSyncState(folder, null);

        if (fetch && message != null) {
            EntityMessage m = db.message().getMessage(message);
            if (m == null || m.uid == null)
                return;

            EntityFolder f = db.folder().getFolder(folder);
            if (f == null)
                return;

            if (FETCH.equals(name))
                sync(context, f.id, false);
            else
                queue(context, f, FETCH, m.uid);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityOperation) {
            EntityOperation other = (EntityOperation) obj;
            return (this.folder.equals(other.folder) &&
                    Objects.equals(this.message, other.message) &&
                    this.name.equals(other.name) &&
                    this.args.equals(other.args) &&
                    this.created.equals(other.created) &&
                    Objects.equals(this.state, other.state) &&
                    Objects.equals(this.error, other.error));
        } else
            return false;
    }

    @Override
    public String toString() {
        return Long.toString(id);
    }
}
