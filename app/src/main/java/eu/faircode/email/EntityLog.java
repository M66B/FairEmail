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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.concurrent.ExecutorService;

@Entity(
        tableName = EntityLog.TABLE_NAME,
        foreignKeys = {
        },
        indices = {
                @Index(value = {"time"})
        }
)
public class EntityLog {
    static final String TABLE_NAME = "log";

    private static boolean ok = true;
    private static long count = 0;
    private static Long last_cleanup = null;

    private static final long LOG_CLEANUP_INTERVAL = 3600 * 1000L; // milliseconds
    private static final long LOG_KEEP_DURATION = 24 * 3600 * 1000L; // milliseconds
    private static final int LOG_DELETE_BATCH_SIZE = 50;

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long time;
    @NonNull
    public Type type = Type.General;
    public Long account;
    public Long folder;
    public Long message;
    @NonNull
    public String data;

    enum Type {General, Statistics, Scheduling, Network, Account, Protocol, Classification, Notification, Rules}

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "log");

    static void log(final Context context, String data) {
        log(context, Type.General, data);
    }

    static void log(final Context context, Type type, EntityAccount account, String data) {
        log(context, type, account.id, null, null, account.name + " " + data);
    }

    static void log(final Context context, Type type, EntityAccount account, EntityFolder folder, String data) {
        log(context, type, account.id, folder.id, null, account.name + "/" + folder.name + " " + data);
    }

    static void log(final Context context, Type type, EntityFolder folder, String data) {
        log(context, type, folder.account, folder.id, null, folder.name + " " + data);
    }

    static void log(final Context context, Type type, EntityMessage message, String data) {
        log(context, type, message.account, message.folder, message.id, data);
    }

    static void log(final Context context, Type type, String data) {
        log(context, type, null, null, null, data);
    }

    static void log(final Context context, Type type, Long account, Long folder, Long message, String data) {
        Log.i(data);

        if (context == null)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean main_log = prefs.getBoolean("main_log", true);
        if (!main_log)
            return;

        final EntityLog entry = new EntityLog();
        entry.time = new Date().getTime();
        entry.type = type;
        entry.account = account;
        entry.folder = folder;
        entry.message = message;
        entry.data = data;

        final DB db = DB.getInstance(context);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (!ok || (++count % LOG_DELETE_BATCH_SIZE) == 0) {
                    long cake = Helper.getAvailableStorageSpace();
                    ok = (cake < Helper.MIN_REQUIRED_SPACE);
                    if (!ok)
                        ok = false;
                }

                try {
                    db.beginTransaction();
                    db.log().insertLog(entry);
                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(ex);
                } finally {
                    db.endTransaction();
                }

                long now = new Date().getTime();
                if (last_cleanup == null || last_cleanup + LOG_CLEANUP_INTERVAL < now) {
                    last_cleanup = now;
                    cleanup(context, now - LOG_KEEP_DURATION);
                }
            }
        });
    }

    static void clear(final Context context) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                cleanup(context, new Date().getTime());
            }
        });
    }

    private static void cleanup(final Context context, final long before) {
        Log.i("Log cleanup interval=" + LOG_CLEANUP_INTERVAL);
        DB db = DB.getInstance(context);
        while (true)
            try {
                db.beginTransaction();
                int logs = db.log().deleteLogs(before, LOG_DELETE_BATCH_SIZE);
                db.setTransactionSuccessful();
                Log.i("Cleanup logs=" + logs + " before=" + new Date(before));
                if (logs < LOG_DELETE_BATCH_SIZE)
                    break;
            } catch (Throwable ex) {
                Log.e(ex);
                break;
            } finally {
                db.endTransaction();
            }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityLog) {
            EntityLog other = (EntityLog) obj;
            return (this.time.equals(other.time) && this.data.equals(other.data));
        } else
            return false;
    }
}
