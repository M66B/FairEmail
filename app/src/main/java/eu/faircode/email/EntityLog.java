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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Debug;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Objects;

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
    private static final long LOG_KEEP_DURATION = (BuildConfig.DEBUG ? 24 : 12) * 3600 * 1000L; // milliseconds
    private static final int LOG_DELETE_BATCH_SIZE = 50;

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long time;
    @NonNull
    public Type type = Type.General;
    public Long thread;
    public Long account;
    public Long folder;
    public Long message;
    @NonNull
    public String data;

    public enum Type {General, Statistics, Scheduling, Network, Account, Protocol, Classification, Notification, Rules, Cloud, Debug1, Debug2, Debug3, Debug4, Debug5}

    public static void log(final Context context, String data) {
        log(context, Type.General, data);
    }

    static void log(final Context context, @NonNull Type type, EntityAccount account, String data) {
        if (account == null)
            log(context, type, data);
        else {
            if (data == null || !data.contains(account.name))
                log(context, type, account.id, null, null, account.name + " " + data);
            else
                log(context, type, account.id, null, null, data);
        }
    }

    static void log(final Context context, @NonNull Type type, EntityFolder folder, String data) {
        if (folder == null)
            log(context, type, data);
        else {
            if (data == null || !data.contains(folder.name))
                log(context, type, folder.account, folder.id, null, folder.name + " " + data);
            else
                log(context, type, folder.account, folder.id, null, data);
        }
    }

    static void log(final Context context, @NonNull Type type, EntityMessage message, String data) {
        if (message == null)
            log(context, type, data);
        else
            log(context, type, message.account, message.folder, message.id, data);
    }

    static void log(final Context context, @NonNull Type type, String data) {
        log(context, type, null, null, null, data);
    }

    static void log(final Context context, @NonNull Type type, Long account, Long folder, Long message, String data) {
        Log.i(data);

        if (context == null)
            return;
        if ((type == Type.Debug1 || type == Type.Debug2 || type == Type.Debug3 || type == Type.Debug4 || type == Type.Debug5) &&
                !(BuildConfig.DEBUG || Log.isTestRelease()))
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean main_log = prefs.getBoolean("main_log", true);
        if (!main_log)
            return;

        boolean main_log_memory = prefs.getBoolean("main_log_memory", false);
        if (main_log_memory) {
            long j = Log.getAvailableMb() - Log.getFreeMemMb();
            long n = Debug.getNativeHeapSize() / 1024L / 1024L;
            data = j + "/" + n + " " + data;
        }

        final EntityLog entry = new EntityLog();
        entry.time = new Date().getTime();
        entry.type = type;
        entry.thread = Thread.currentThread().getId();
        entry.account = account;
        entry.folder = folder;
        entry.message = message;
        entry.data = data;

        final DB db = DB.getInstance(context);
        final Context acontext = context.getApplicationContext();

        Helper.getSerialExecutor().submit(new Runnable() {
            @Override
            public void run() {
                // Check available storage space
                if (!ok || (++count % LOG_DELETE_BATCH_SIZE) == 0) {
                    long cake = Helper.getAvailableStorageSpace();
                    boolean wasOk = ok;
                    ok = (cake > Helper.MIN_REQUIRED_SPACE);
                    if (!ok)
                        if (wasOk) {
                            entry.type = Type.General;
                            entry.account = null;
                            entry.folder = null;
                            entry.message = null;
                            entry.data = "Insufficient storage space=" +
                                    Helper.humanReadableByteCount(cake) + "/" +
                                    Helper.humanReadableByteCount(Helper.MIN_REQUIRED_SPACE);
                        } else
                            return;
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
                    cleanup(acontext, now - LOG_KEEP_DURATION);
                }
            }
        });
    }

    static void clear(final Context context) {
        final Context acontext = context.getApplicationContext();
        Helper.getParallelExecutor().submit(new Runnable() {
            @Override
            public void run() {
                cleanup(acontext, new Date().getTime());
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

    Integer getColor(Context context) {
        return getColor(context, this.type);
    }

    static Integer getColor(Context context, Type type) {
        // R.color.solarizedRed
        switch (type) {
            case General:
                return Helper.resolveColor(context, android.R.attr.textColorPrimary);
            case Statistics:
                return ContextCompat.getColor(context, R.color.solarizedGreen);
            case Scheduling:
                return ContextCompat.getColor(context, R.color.solarizedYellow);
            case Network:
                return ContextCompat.getColor(context, R.color.solarizedOrange);
            case Account:
                return ContextCompat.getColor(context, R.color.solarizedMagenta);
            case Protocol:
                return Helper.resolveColor(context, android.R.attr.textColorSecondary);
            case Classification:
                return ContextCompat.getColor(context, R.color.solarizedViolet);
            case Notification:
                return ContextCompat.getColor(context, R.color.solarizedBlue);
            case Rules:
                return ContextCompat.getColor(context, R.color.solarizedCyan);
            case Cloud:
                return ContextCompat.getColor(context, R.color.solarizedRed);
            case Debug1:
                return ContextCompat.getColor(context, R.color.solarizedRed);
            case Debug2:
                return ContextCompat.getColor(context, R.color.solarizedGreen);
            case Debug3:
                return ContextCompat.getColor(context, R.color.solarizedBlue);
            case Debug4:
                return ContextCompat.getColor(context, R.color.solarizedOrange);
            case Debug5:
                return ContextCompat.getColor(context, R.color.solarizedYellow);
            default:
                return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityLog) {
            EntityLog other = (EntityLog) obj;
            return (this.id.equals(other.id) &&
                    this.time.equals(other.time) &&
                    this.type.equals(other.type) &&
                    Objects.equals(this.account, other.account) &&
                    Objects.equals(this.folder, other.folder) &&
                    Objects.equals(this.message, other.message) &&
                    this.data.equals(other.data));
        } else
            return false;
    }
}
