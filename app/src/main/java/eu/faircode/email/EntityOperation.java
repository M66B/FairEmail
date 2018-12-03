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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.util.Log;

import org.json.JSONArray;

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

    public static final String ADD = "add";
    public static final String MOVE = "move";
    public static final String DELETE = "delete";
    public static final String SEND = "send";
    public static final String SEEN = "seen";
    public static final String ANSWERED = "answered";
    public static final String FLAG = "flag";
    public static final String KEYWORD = "keyword";
    public static final String HEADERS = "headers";
    public static final String BODY = "body";
    public static final String ATTACHMENT = "attachment";
    public static final String SYNC = "sync";

    static void queue(DB db, EntityMessage message, String name) {
        JSONArray jargs = new JSONArray();
        queue(db, message.folder, message.id, name, jargs);
    }

    static void queue(DB db, EntityMessage message, String name, Object value) {
        JSONArray jargs = new JSONArray();
        jargs.put(value);
        queue(db, message.folder, message.id, name, jargs);

        if (SEEN.equals(name)) {
            db.message().setMessageUiSeen(message.id, (boolean) value);
            db.message().setMessageUiIgnored(message.id, true);
        } else if (FLAG.equals(name))
            db.message().setMessageUiFlagged(message.id, (boolean) value);
        else if (ANSWERED.equals(name))
            db.message().setMessageUiAnswered(message.id, (boolean) value);
        else if (MOVE.equals(name))
            db.message().setMessageUiHide(message.id, true);
        else if (DELETE.equals(name))
            db.message().setMessageUiHide(message.id, true);
    }

    static void queue(DB db, EntityMessage message, String name, Object value1, Object value2) {
        JSONArray jargs = new JSONArray();
        jargs.put(value1);
        jargs.put(value2);
        queue(db, message.folder, message.id, name, jargs);
    }

    static void sync(DB db, long folder) {
        if (db.operation().getOperationCount(folder, EntityOperation.SYNC) == 0) {
            queue(db, folder, null, EntityOperation.SYNC, new JSONArray());
            db.folder().setFolderSyncState(folder, "requested");
        }
    }

    private static void queue(DB db, long folder, Long message, String name, JSONArray jargs) {
        EntityOperation operation = new EntityOperation();
        operation.folder = folder;
        operation.message = message;
        operation.name = name;
        operation.args = jargs.toString();
        operation.created = new Date().getTime();
        operation.id = db.operation().insertOperation(operation);

        Log.i(Helper.TAG, "Queued op=" + operation.id + "/" + operation.name +
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
