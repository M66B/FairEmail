package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityOperation.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "message", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"message"})
        }
)
public class EntityOperation {
    static final String TABLE_NAME = "operation";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long message;
    @NonNull
    public String name;
    public String args;

    public static final String SEEN = "seen";
    public static final String ADD = "add";
    public static final String MOVE = "move";
    public static final String DELETE = "delete";
    public static final String SEND = "send";
    public static final String ATTACHMENT = "attachment";

    private static List<Intent> queue = new ArrayList<>();

    static void queue(Context context, EntityMessage message, String name) {
        JSONArray jsonArray = new JSONArray();
        queue(context, message, name, jsonArray);
    }

    static void queue(Context context, EntityMessage message, String name, Object value) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(value);
        queue(context, message, name, jsonArray);
    }

    private static void queue(Context context, EntityMessage message, String name, JSONArray jsonArray) {
        DaoOperation dao = DB.getInstance(context).operation();

        int purged = 0;
        if (SEEN.equals(name)) {
            if (message.uid == null) {
                // local message
                return;
            }
            purged = dao.deleteOperations(message.id, name);
        }

        EntityOperation operation = new EntityOperation();
        operation.message = message.id;
        operation.name = name;
        operation.args = jsonArray.toString();
        operation.id = dao.insertOperation(operation);

        synchronized (queue) {
            queue.add(new Intent(SEND.equals(name)
                    ? ServiceSynchronize.ACTION_PROCESS_OUTBOX
                    : ServiceSynchronize.ACTION_PROCESS_FOLDER)
                    .putExtra("folder", message.folder));
        }

        Log.i(Helper.TAG, "Queued op=" + operation.id + "/" + name +
                " args=" + operation.args +
                " msg=" + message.folder + "/" + message.id + " uid=" + message.uid +
                " purged=" + purged);
    }

    public static void process(Context context) {
        // Processing needs to be done after committing to the database
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        synchronized (queue) {
            for (Intent intent : queue)
                lbm.sendBroadcast(intent);
            queue.clear();
        }
    }
}
