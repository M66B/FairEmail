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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

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

import static androidx.room.ForeignKey.CASCADE;

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
    static final String HEADERS = "headers";
    static final String RAW = "raw";
    static final String BODY = "body";
    static final String ATTACHMENT = "attachment";
    static final String SYNC = "sync";
    static final String SUBSCRIBE = "subscribe";
    static final String SEND = "send";
    static final String EXISTS = "exists";

    static void queue(Context context, EntityMessage message, String name, Object... values) {
        DB db = DB.getInstance(context);

        try {
            JSONArray jargs = new JSONArray();
            for (Object value : values)
                jargs.put(value);

            if (ADD.equals(name)) {
                if (EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) ||
                        EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt)) {
                    EntityFolder folder = db.folder().getFolder(message.folder);
                    if (folder != null && EntityFolder.DRAFTS.equals(folder.type)) {
                        WorkerFts.init(context, false);
                        return;
                    }
                }
            }

            if (MOVE.equals(name) &&
                    (EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) ||
                            EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt))) {
                EntityFolder folder = db.folder().getFolder(message.folder);
                if (folder != null && EntityFolder.DRAFTS.equals(folder.type))
                    name = DELETE;
            }

            if (SEEN.equals(name)) {
                boolean seen = jargs.getBoolean(0);
                boolean ignore = jargs.optBoolean(1, true);
                for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid)) {
                    db.message().setMessageUiSeen(similar.id, seen);
                    db.message().setMessageUiIgnored(similar.id, ignore);
                    queue(context, similar.account, similar.folder, similar.id, name, jargs);
                }
                return;

            } else if (FLAG.equals(name)) {
                boolean flagged = jargs.getBoolean(0);
                Integer color = (jargs.length() > 1 && !jargs.isNull(1) ? jargs.getInt(1) : null);
                for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid)) {
                    db.message().setMessageUiFlagged(similar.id, flagged, flagged ? color : null);
                    queue(context, similar.account, similar.folder, similar.id, name, jargs);
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
                while (keywords.remove(keyword))
                    ;
                if (set)
                    keywords.add(keyword);
                Collections.sort(keywords);

                db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(keywords.toArray(new String[0])));

            } else if (MOVE.equals(name)) {
                // Parameters:
                // 0: target folder
                // 1: mark seen
                // 2: temporary message
                // 3: remove flag

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean autoread = prefs.getBoolean("autoread", false);
                boolean autounflag = prefs.getBoolean("autounflag", false);
                boolean reset_importance = prefs.getBoolean("reset_importance", false);

                if (jargs.opt(1) != null) // rules
                    autoread = jargs.getBoolean(1);
                jargs.put(1, autoread);
                jargs.put(3, autounflag);

                EntityFolder source = db.folder().getFolder(message.folder);
                EntityFolder target = db.folder().getFolder(jargs.getLong(0));
                if (source.id.equals(target.id))
                    return;

                EntityLog.log(context, "Move message=" + message.id + ":" + message.subject +
                        " source=" + source.id + ":" + source.name + "" +
                        " target=" + target.id + ":" + target.name +
                        " auto read=" + autoread + " flag=" + autounflag);

                if (autoread || autounflag || reset_importance)
                    for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid)) {
                        if (autoread)
                            db.message().setMessageUiSeen(similar.id, true);
                        if (autounflag)
                            db.message().setMessageUiFlagged(similar.id, false, null);
                        if (reset_importance)
                            db.message().setMessageImportance(similar.id, null);
                    }


                if (!EntityFolder.ARCHIVE.equals(source.type) ||
                        EntityFolder.TRASH.equals(target.type) || EntityFolder.JUNK.equals(target.type))
                    db.message().setMessageUiHide(message.id, true);

                if (message.ui_snoozed != null &&
                        (EntityFolder.ARCHIVE.equals(target.type) || EntityFolder.TRASH.equals(target.type))) {
                    message.ui_snoozed = null;
                    EntityMessage.snooze(context, message.id, null);
                }

                // Create copy without uid in target folder
                // Message with same msgid can be in archive
                if (message.uid != null &&
                        !TextUtils.isEmpty(message.msgid) &&
                        db.message().countMessageByMsgId(target.id, message.msgid) == 0) {
                    File msource = message.getFile(context);

                    // Copy message to target folder
                    long id = message.id;
                    Long identity = message.identity;
                    long uid = message.uid;
                    int notifying = message.notifying;
                    boolean fts = message.fts;
                    Integer importance = message.importance;
                    boolean seen = message.seen;
                    boolean flagged = message.flagged;
                    boolean ui_seen = message.ui_seen;
                    boolean ui_flagged = message.ui_flagged;
                    Boolean ui_hide = message.ui_hide;
                    boolean ui_browsed = message.ui_browsed;
                    Integer color = message.color;
                    String error = message.error;

                    message.id = null;
                    message.account = target.account;
                    message.folder = target.id;
                    message.identity = null;
                    message.uid = null;
                    message.notifying = 0;
                    message.fts = false;
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
                    message.ui_browsed = false;
                    message.error = null;
                    message.id = db.message().insertMessage(message);
                    File mtarget = message.getFile(context);
                    long tmpid = message.id;
                    jargs.put(2, tmpid);

                    message.id = id;
                    message.account = source.account;
                    message.folder = source.id;
                    message.identity = identity;
                    message.uid = uid;
                    message.notifying = notifying;
                    message.fts = fts;
                    message.importance = importance;
                    message.seen = seen;
                    message.flagged = flagged;
                    message.ui_seen = ui_seen;
                    message.ui_flagged = ui_flagged;
                    message.ui_hide = ui_hide;
                    message.ui_browsed = ui_browsed;
                    message.color = color;
                    message.error = error;

                    if (message.content)
                        try {
                            Helper.copy(msource, mtarget);
                        } catch (IOException ex) {
                            Log.e(ex);
                            db.message().setMessageContent(tmpid, false, null, null, null);
                            db.message().setMessageSize(message.id, null, null);
                        }

                    EntityAttachment.copy(context, message.id, tmpid);
                }

                // Cross account move
                long folder = source.id;
                if (!source.account.equals(target.account))
                    if (message.raw != null && message.raw) {
                        name = ADD;
                        folder = target.id;
                    } else
                        name = RAW;
                queue(context, message.account, folder, message.id, name, jargs);
                return;

            } else if (DELETE.equals(name))
                db.message().setMessageUiHide(message.id, true);

            else if (ATTACHMENT.equals(name))
                db.attachment().setProgress(jargs.getLong(0), 0);

            queue(context, message.account, message.folder, message.id, name, jargs);

        } catch (JSONException ex) {
            Log.e(ex);
        }
    }

    private static void queue(Context context, Long account, long folder, long message, String name, JSONArray jargs) {
        DB db = DB.getInstance(context);

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
        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
        Log.breadcrumb("queued", crumb);
    }

    static void queue(Context context, EntityFolder folder, String name, Object... values) {
        DB db = DB.getInstance(context);

        JSONArray jargs = new JSONArray();
        for (Object value : values)
            jargs.put(value);

        EntityOperation op = new EntityOperation();
        op.account = folder.account;
        op.folder = folder.id;
        op.message = null;
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
        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
        Log.breadcrumb("queued", crumb);
    }

    static void sync(Context context, long fid, boolean foreground) {
        DB db = DB.getInstance(context);

        EntityFolder folder = db.folder().getFolder(fid);
        if (folder == null)
            return;

        // TODO: replace sync parameters?
        if (db.operation().getOperationCount(fid, EntityOperation.SYNC) == 0) {
            EntityOperation operation = new EntityOperation();
            operation.account = folder.account;
            operation.folder = folder.id;
            operation.message = null;
            operation.name = SYNC;
            operation.args = folder.getSyncArgs().toString();
            operation.created = new Date().getTime();
            operation.id = db.operation().insertOperation(operation);

            Log.i("Queued sync folder=" + folder);
        }

        if (foreground) // Show spinner
            db.folder().setFolderSyncState(fid, "requested");

        if (folder.account == null) // Outbox
            ServiceSend.start(context);
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

    void cleanup(Context context) {
        DB db = DB.getInstance(context);

        if (message != null)
            db.message().setMessageUiHide(message, false);

        if (EntityOperation.MOVE.equals(name) ||
                EntityOperation.ADD.equals(name) ||
                EntityOperation.RAW.equals(name))
            try {
                JSONArray jargs = new JSONArray(args);
                long tmpid = jargs.optLong(2, -1);
                if (tmpid < 0)
                    return;

                db.message().deleteMessage(tmpid);
            } catch (JSONException ex) {
                Log.e(ex);
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
}
