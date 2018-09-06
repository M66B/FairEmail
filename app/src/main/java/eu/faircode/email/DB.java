package eu.faircode.email;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
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

// https://developer.android.com/topic/libraries/architecture/room.html

@Database(
        version = 10,
        entities = {
                EntityIdentity.class,
                EntityAccount.class,
                EntityFolder.class,
                EntityMessage.class,
                EntityAttachment.class,
                EntityOperation.class,
                EntityAnswer.class,
                EntityLog.class
        }
)

@TypeConverters({DB.Converters.class})
public abstract class DB extends RoomDatabase {
    public abstract DaoIdentity identity();

    public abstract DaoAccount account();

    public abstract DaoFolder folder();

    public abstract DaoMessage message();

    public abstract DaoAttachment attachment();

    public abstract DaoOperation operation();

    public abstract DaoAnswer answer();

    public abstract DaoLog log();

    private static DB sInstance;

    private static final String DB_NAME = "email";

    public static synchronized DB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = migrate(Room
                    .databaseBuilder(context.getApplicationContext(), DB.class, DB_NAME)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING));

            Log.i(Helper.TAG, "sqlite version=" + exec(sInstance, "SELECT sqlite_version() AS sqlite_version"));
            Log.i(Helper.TAG, "sqlite sync=" + exec(sInstance, "PRAGMA synchronous"));
            Log.i(Helper.TAG, "sqlite journal=" + exec(sInstance, "PRAGMA journal_mode"));
        }

        return sInstance;
    }

    static String exec(DB db, String command) {
        Cursor cursor = null;
        try {
            cursor = db.query(command, new Object[0]);
            if (cursor.moveToNext())
                return cursor.getString(0);
            else
                return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private static DB migrate(RoomDatabase.Builder<DB> builder) {
        return builder
                .addCallback(new Callback() {
                    @Override
                    public void onOpen(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "Database version=" + db.getVersion());
                        super.onOpen(db);
                    }
                })
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `poll_interval` INTEGER NOT NULL DEFAULT 9");
                    }
                })
                .addMigrations(new Migration(2, 3) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `store_sent` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(3, 4) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE TABLE IF NOT EXISTS `answer` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL, `text` TEXT NOT NULL)");
                    }
                })
                .addMigrations(new Migration(4, 5) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `auth_type` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `auth_type` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(5, 6) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_found` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(6, 7) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE TABLE IF NOT EXISTS `log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `time` INTEGER NOT NULL, `data` TEXT NOT NULL)");
                        db.execSQL("CREATE  INDEX `index_log_time` ON `log` (`time`)");
                    }
                })
                .addMigrations(new Migration(7, 8) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE  INDEX `index_message_ui_found` ON `message` (`ui_found`)");
                    }
                })
                .addMigrations(new Migration(8, 9) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `headers` TEXT");
                    }
                })
                .addMigrations(new Migration(9, 10) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `unified` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("CREATE  INDEX `index_folder_unified` ON `folder` (`unified`)");
                        db.execSQL("UPDATE `folder` SET unified = 1 WHERE type = '" + EntityFolder.INBOX + "'");
                    }
                })
                .build();
    }

    public static class Converters {
        @TypeConverter
        public static String[] fromStringArray(String value) {
            return value.split(",");
        }

        @TypeConverter
        public static String toStringArray(String[] value) {
            return TextUtils.join(",", value);
        }

        @TypeConverter
        public static String encodeAddresses(Address[] addresses) {
            if (addresses == null)
                return null;
            JSONArray jaddresses = new JSONArray();
            if (addresses != null)
                for (Address address : addresses)
                    try {
                        if (address instanceof InternetAddress) {
                            String a = ((InternetAddress) address).getAddress();
                            String p = ((InternetAddress) address).getPersonal();
                            JSONObject jaddress = new JSONObject();
                            if (a != null)
                                jaddress.put("address", a);
                            if (p != null)
                                jaddress.put("personal", p);
                            jaddresses.put(jaddress);
                        } else {
                            JSONObject jaddress = new JSONObject();
                            jaddress.put("address", address.toString());
                            jaddresses.put(jaddress);
                        }
                    } catch (JSONException ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }
            return jaddresses.toString();
        }

        @TypeConverter
        public static Address[] decodeAddresses(String json) {
            if (json == null)
                return null;
            List<Address> result = new ArrayList<>();
            try {
                JSONArray jaddresses = new JSONArray(json);
                for (int i = 0; i < jaddresses.length(); i++) {
                    JSONObject jaddress = (JSONObject) jaddresses.get(i);
                    if (jaddress.has("personal"))
                        result.add(new InternetAddress(
                                jaddress.getString("address"),
                                jaddress.getString("personal")));
                    else
                        result.add(new InternetAddress(
                                jaddress.getString("address")));
                }
            } catch (Throwable ex) {
                // Compose can store invalid addresses
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
            return result.toArray(new Address[0]);
        }
    }
}

