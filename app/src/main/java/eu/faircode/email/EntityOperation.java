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

    static void sync(DB db, long fid) {
        if (db.operation().getOperationCount(fid, EntityOperation.SYNC) == 0) {

            EntityFolder folder = db.folder().getFolder(fid);

            int sync_days = folder.sync_days;
            if (folder.last_sync != null) {
                int ago_days = (int) ((new Date().getTime() - folder.last_sync) / (24 * 3600 * 1000L)) + 1;
                if (ago_days > sync_days)
                    sync_days = ago_days;
            }

            JSONArray jargs = new JSONArray();
            jargs.put(folder.initialize ? Math.min(EntityFolder.DEFAULT_INIT, folder.keep_days) : sync_days);
            jargs.put(folder.keep_days);
            jargs.put(folder.download);

            EntityOperation operation = new EntityOperation();
            operation.folder = folder.id;
            operation.message = null;
            operation.name = SYNC;
            operation.args = jargs.toString();
            operation.created = new Date().getTime();
            operation.id = db.operation().insertOperation(operation);

            db.folder().setFolderSyncState(fid, "requested");

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
                        message.ui_seen &&
                        target.synchronize &&
                        message.received > cal_keep.getTimeInMillis() &&
                        db.message().countMessageByMsgId(target.id, message.msgid) == 0) {
                    long id = message.id;
                    long uid = message.uid;
                    message.id = null;
                    message.account = target.account;
                    message.folder = target.id;
                    message.uid = null;
                    newid = db.message().insertMessage(message);
                    message.id = id;
                    message.account = source.account;
                    message.folder = source.id;
                    message.uid = uid;

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
                if (!source.account.equals(target.account)) {
                    name = ADD;
                    folder = target.id;
                    jargs = new JSONArray();
                    if (newid != null) {
                        jargs.put(0, newid);
                        jargs.put(1, autoread);
                    }
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
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityOperation) {
            EntityOperation other = (EntityOperation) obj;
            return (this.folder.equals(other.folder) &&
                    (this.message == null ? other.message == null : this.message.equals(other.message)) &&
                    this.name.equals(other.name) &&
                    this.args.equals(other.args) &&
                    this.created.equals(other.created) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }
}
