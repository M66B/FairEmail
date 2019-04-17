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
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityOperation.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "folder", entity = EntityFolder.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "message", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"folder"}),
                @Index(value = {"message"}),
                @Index(value = {"name"})
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
    public String error;

    static final String ADD = "add";
    static final String MOVE = "move";
    static final String COPY = "copy";
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
    static final String WAIT = "wait";

    static void queue(Context context, DB db, EntityMessage message, String name, Object... values) {
        JSONArray jargs = new JSONArray();
        for (Object value : values)
            jargs.put(value);

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
                // 2: temporary target message id

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
                Long tmpid = null;
                if (message.uid != null &&
                        target.synchronize &&
                        message.received > cal_keep.getTimeInMillis() &&
                        db.message().countMessageByMsgId(target.id, message.msgid) == 0) {
                    File msource = message.getFile(context);

                    // Copy message to target folder
                    long id = message.id;
                    long uid = message.uid;
                    boolean seen = message.seen;
                    boolean ui_seen = message.ui_seen;
                    boolean ui_browsed = message.ui_browsed;
                    message.id = null;
                    message.account = target.account;
                    message.folder = target.id;
                    message.uid = null;
                    if (autoread) {
                        message.seen = true;
                        message.ui_seen = true;
                    }
                    message.ui_browsed = false;
                    message.id = db.message().insertMessage(message);
                    File mtarget = message.getFile(context);
                    tmpid = message.id;

                    message.id = id;
                    message.account = source.account;
                    message.folder = source.id;
                    message.uid = uid;
                    message.seen = seen;
                    message.ui_seen = ui_seen;
                    message.ui_browsed = ui_browsed;

                    if (message.content)
                        try {
                            Helper.copy(msource, mtarget);
                        } catch (IOException ex) {
                            Log.e(ex);
                            db.message().setMessageContent(tmpid, false, null, null);
                            db.message().setMessageSize(message.id, null);
                        }

                    EntityAttachment.copy(context, message.id, tmpid);
                }

                // Cross account move
                if (source.account.equals(target.account))
                    jargs.put(2, tmpid); // Can be null
                else {
                    if (message.raw != null && message.raw) {
                        name = ADD;
                        folder = target.id;
                        jargs = new JSONArray();
                        jargs.put(0, tmpid); // Can be null
                        jargs.put(1, autoread);
                    } else {
                        name = RAW;
                        jargs = new JSONArray();
                        jargs.put(0, tmpid); // Can be null
                        jargs.put(1, autoread);
                        jargs.put(2, target.id);
                    }
                }

            } else if (DELETE.equals(name))
                db.message().setMessageUiHide(message.id, true);

        } catch (JSONException ex) {
            Log.e(ex);
        }

        EntityOperation operation = new EntityOperation();
        operation.account = message.account;
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
        else
            ServiceSynchronize.process(context);
    }

    static void sync(Context context, long fid, boolean foreground) {
        DB db = DB.getInstance(context);

        EntityFolder folder = db.folder().getFolder(fid);
        EntityAccount account = null;
        if (folder.account != null)
            account = db.account().getAccount(folder.account);

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

        if (foreground)
            db.folder().setFolderSyncState(fid, "requested");

        if (account == null) // Outbox
            ServiceSend.start(context);
        else if (foreground)
            ServiceSynchronize.process(context);
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
