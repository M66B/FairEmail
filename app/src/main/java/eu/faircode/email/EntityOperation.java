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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityOperation.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "folder", entity = EntityFolder.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "message", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"folder"}),
                @Index(value = {"message"})
        }
)
public class EntityOperation {
    static final String TABLE_NAME = "operation";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long folder;
    public Long message;
    @NonNull
    public String name;
    @NonNull
    public String args;
    @NonNull
    public Long created;
    public String error;

    static final String ADD = "add";
    static final String MOVE = "move";
    static final String DELETE = "delete";
    static final String SEND = "send";
    static final String SEEN = "seen";
    static final String ANSWERED = "answered";
    static final String FLAG = "flag";
    static final String KEYWORD = "keyword";
    static final String HEADERS = "headers";
    static final String RAW = "raw";
    static final String BODY = "body";
    static final String ATTACHMENT = "attachment";
    static final String SYNC = "sync";

    static void queue(Context context, DB db, EntityMessage message, String name) {
        JSONArray jargs = new JSONArray();
        queue(context, db, message, name, jargs);
    }

    static void queue(Context context, DB db, EntityMessage message, String name, Object value) {
        JSONArray jargs = new JSONArray();
        jargs.put(value);
        queue(context, db, message, name, jargs);
    }

    static void queue(Context context, DB db, EntityMessage message, String name, Object value1, Object value2) {
        JSONArray jargs = new JSONArray();
        jargs.put(value1);
        jargs.put(value2);
        queue(context, db, message, name, jargs);
    }

    static void sync(Context context, long fid) {
        DB db = DB.getInstance(context);
        if (db.operation().getOperationCount(fid, EntityOperation.SYNC) == 0) {

            EntityFolder folder = db.folder().getFolder(fid);
            EntityAccount account = null;
            if (folder.account != null)
                account = db.account().getAccount(folder.account);

            EntityOperation operation = new EntityOperation();
            operation.folder = folder.id;
            operation.message = null;
            operation.name = SYNC;
            operation.args = folder.getSyncArgs().toString();
            operation.created = new Date().getTime();
            operation.id = db.operation().insertOperation(operation);

            if (account != null && (account.ondemand || !"connected".equals(account.state))) {
                db.folder().setFolderState(fid, "waiting");
                db.folder().setFolderSyncState(fid, "manual");
            } else
                db.folder().setFolderSyncState(fid, "requested");

            if (account == null) // Outbox
                ServiceSend.start(context);
            else if (account.ondemand || !"connected".equals(account.state))
                ServiceUI.process(context, fid);

            Log.i("Queued sync folder=" + folder);
        }
    }

    private static void queue(Context context, DB db, EntityMessage message, String name, JSONArray jargs) {
        long folder = message.folder;
        try {
            if (SEEN.equals(name)) {
                for (EntityMessage similar : db.message().getMessageByMsgId(message.account, message.msgid)) {
                    db.message().setMessageUiSeen(similar.id, jargs.getBoolean(0));
                    db.message().setMessageUiIgnored(similar.id, true);
                }

            } else if (FLAG.equals(name))
                for (EntityMessage similar : db.message().getMessageByMsgId(message.account, message.msgid))
                    db.message().setMessageUiFlagged(similar.id, jargs.getBoolean(0));

            else if (ANSWERED.equals(name))
                for (EntityMessage similar : db.message().getMessageByMsgId(message.account, message.msgid))
                    db.message().setMessageUiAnswered(similar.id, jargs.getBoolean(0));

            else if (MOVE.equals(name)) {
                // Parameters:
                // 0: target folder id
                // 1: allow auto read

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean autoread = prefs.getBoolean("autoread", false);
                if (jargs.length() > 1)
                    autoread = (autoread && jargs.getBoolean(1));
                jargs.put(1, autoread);

                EntityFolder source = db.folder().getFolder(message.folder);
                EntityFolder target = db.folder().getFolder(jargs.getLong(0));
                if (source.id.equals(target.id))
                    return;

                if (!EntityFolder.ARCHIVE.equals(source.type) || EntityFolder.TRASH.equals(target.type))
                    db.message().setMessageUiHide(message.id, true);

                Calendar cal_keep = Calendar.getInstance();
                cal_keep.add(Calendar.DAY_OF_MONTH, -target.keep_days);
                cal_keep.set(Calendar.HOUR_OF_DAY, 0);
                cal_keep.set(Calendar.MINUTE, 0);
                cal_keep.set(Calendar.SECOND, 0);
                cal_keep.set(Calendar.MILLISECOND, 0);

                // Create copy without uid in target folder
                // Message with same msgid can be in archive
                Long newid = null;
                if (message.uid != null &&
                        target.synchronize &&
                        message.received > cal_keep.getTimeInMillis() &&
                        db.message().countMessageByMsgId(target.id, message.msgid) == 0) {
                    long id = message.id;
                    long uid = message.uid;
                    boolean seen = message.seen;
                    boolean ui_seen = message.ui_seen;
                    message.id = null;
                    message.account = target.account;
                    message.folder = target.id;
                    message.uid = null;
                    if (autoread) {
                        message.seen = true;
                        message.ui_seen = true;
                    }
                    newid = db.message().insertMessage(message);
                    message.id = id;
                    message.account = source.account;
                    message.folder = source.id;
                    message.uid = uid;
                    message.seen = seen;
                    message.ui_seen = ui_seen;

                    if (message.content)
                        try {
                            Helper.copy(
                                    EntityMessage.getFile(context, id),
                                    EntityMessage.getFile(context, newid));
                        } catch (IOException ex) {
                            Log.e(ex);
                            db.message().setMessageContent(newid, false, null);
                        }

                    EntityAttachment.copy(context, db, message.id, newid);

                    // Store new id for when source message was deleted
                    jargs.put(2, newid);
                }

                // Cross account move
                if (!source.account.equals(target.account))
                    if (message.raw != null && message.raw) {
                        name = ADD;
                        folder = target.id;
                        jargs = new JSONArray();
                        jargs.put(0, newid); // Can be null
                        jargs.put(1, autoread);
                    } else {
                        name = RAW;
                        jargs = new JSONArray();
                        jargs.put(0, newid); // Can be null
                        jargs.put(1, autoread);
                        jargs.put(2, target.id);
                    }

            } else if (DELETE.equals(name))
                db.message().setMessageUiHide(message.id, true);

        } catch (JSONException ex) {
            Log.e(ex);
        }

        EntityOperation operation = new EntityOperation();
        operation.folder = folder;
        operation.message = message.id;
        operation.name = name;
        operation.args = jargs.toString();
        operation.created = new Date().getTime();
        operation.id = db.operation().insertOperation(operation);

        Log.i("Queued op=" + operation.id + "/" + operation.name +
                " msg=" + operation.folder + "/" + operation.message +
                " args=" + operation.args);

        if (SEND.equals(name))
            ServiceSend.start(context);
        else {
            EntityAccount account = db.account().getAccount(message.account);
            if (account.ondemand)
                ServiceUI.process(context, operation.folder);
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
                    Objects.equals(this.error, other.error));
        } else
            return false;
    }
}
