package eu.faircode.email;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory;

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

// https://developer.android.com/topic/libraries/architecture/room.html

@Database(
        version = 68,
        entities = {
                EntityIdentity.class,
                EntityAccount.class,
                EntityFolder.class,
                EntityMessage.class,
                EntityAttachment.class,
                EntityOperation.class,
                EntityContact.class,
                EntityAnswer.class,
                EntityRule.class,
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

    public abstract DaoContact contact();

    public abstract DaoAnswer answer();

    public abstract DaoRule rule();

    public abstract DaoLog log();

    private static DB sInstance;
    private static ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static final String DB_NAME = "fairemail";

    public static synchronized DB getInstance(Context ctx) {
        if (sInstance == null) {
            Context context = ctx.getApplicationContext();
            sInstance = migrate(context, getBuilder(context));

            Log.i("SQLite version=" + exec(sInstance, "SELECT sqlite_version() AS sqlite_version"));
            Log.i("SQLite sync=" + exec(sInstance, "PRAGMA synchronous"));
            Log.i("SQLite journal=" + exec(sInstance, "PRAGMA journal_mode"));
        }

        return sInstance;
    }

    private static RoomDatabase.Builder getBuilder(Context context) {
        return Room
                .databaseBuilder(context, DB.class, DB_NAME)
                .openHelperFactory(new RequerySQLiteOpenHelperFactory())
                .setQueryExecutor(executor)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING);
    }

    private static String exec(DB db, String command) {
        try (Cursor cursor = db.query(command, new Object[0])) {
            if (cursor != null && cursor.moveToNext())
                return cursor.getString(0);
            else
                return null;
        }
    }

    private static DB migrate(final Context context, RoomDatabase.Builder<DB> builder) {
        // https://www.sqlite.org/lang_altertable.html
        return builder
                .addCallback(new Callback() {
                    @Override
                    public void onOpen(SupportSQLiteDatabase db) {
                        Log.i("Database version=" + db.getVersion());
                        super.onOpen(db);
                    }
                })
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` RENAME COLUMN `after` TO `sync_days`");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keep_days` INTEGER NOT NULL DEFAULT 30");
                        db.execSQL("UPDATE `folder` SET keep_days = sync_days");
                    }
                })
                .addMigrations(new Migration(2, 3) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `signature` TEXT");
                        db.execSQL("UPDATE `identity` SET signature =" +
                                " (SELECT account.signature FROM account WHERE account.id = identity.account)");
                    }
                })
                .addMigrations(new Migration(3, 4) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `forwarding` INTEGER" +
                                " REFERENCES `message`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL");
                        db.execSQL("CREATE INDEX `index_message_forwarding` ON `message` (`forwarding`)");
                    }
                })
                .addMigrations(new Migration(4, 5) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `last_connected` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `last_attempt` INTEGER");
                    }
                })
                .addMigrations(new Migration(5, 6) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(6, 7) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `answered` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_answered` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(7, 8) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new Migration(8, 9) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new Migration(9, 10) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_browsed` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("CREATE INDEX `index_message_ui_browsed` ON `message` (`ui_browsed`)");
                    }
                })
                .addMigrations(new Migration(10, 11) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new Migration(11, 12) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP INDEX `index_operation_folder`");
                        db.execSQL("DROP INDEX `index_operation_message`");
                        db.execSQL("DROP TABLE `operation`");
                        db.execSQL("CREATE TABLE `operation`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `folder` INTEGER NOT NULL" +
                                ", `message` INTEGER" +
                                ", `name` TEXT NOT NULL" +
                                ", `args` TEXT NOT NULL" +
                                ", `created` INTEGER NOT NULL" +
                                ", `error` TEXT" +
                                ", FOREIGN KEY(`folder`) REFERENCES `folder`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE" +
                                ", FOREIGN KEY(`message`) REFERENCES `message`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
                        db.execSQL("CREATE INDEX `index_operation_folder` ON `operation` (`folder`)");
                        db.execSQL("CREATE INDEX `index_operation_message` ON `operation` (`message`)");
                    }
                })
                .addMigrations(new Migration(12, 13) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE INDEX `index_message_ui_flagged` ON `message` (`ui_flagged`)");
                    }
                })
                .addMigrations(new Migration(13, 14) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `level` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(14, 15) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `sync_state` TEXT");
                    }
                })
                .addMigrations(new Migration(15, 16) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(16, 17) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DELETE FROM `message` WHERE ui_found");
                        db.execSQL("DROP INDEX `index_message_folder_uid_ui_found`");
                        db.execSQL("DROP INDEX `index_message_msgid_folder_ui_found`");
                        db.execSQL("CREATE UNIQUE INDEX `index_message_folder_uid` ON `message` (`folder`, `uid`)");
                        db.execSQL("CREATE UNIQUE INDEX `index_message_msgid_folder` ON `message` (`msgid`, `folder`)");
                    }
                })
                .addMigrations(new Migration(17, 18) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbd` INTEGER");
                    }
                })
                .addMigrations(new Migration(18, 19) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `delivery_receipt` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `read_receipt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(19, 20) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `folder` SET notify = unified");
                    }
                })
                .addMigrations(new Migration(20, 21) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `display` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `bcc` TEXT");
                    }
                })
                .addMigrations(new Migration(21, 22) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `initialize` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("UPDATE `folder` SET sync_days = 1");
                    }
                })
                .addMigrations(new Migration(22, 23) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `download` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(23, 24) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbc` INTEGER");
                    }
                })
                .addMigrations(new Migration(24, 25) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `prefix` TEXT");
                    }
                })
                .addMigrations(new Migration(25, 26) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        int browse = (prefs.getBoolean("browse", true) ? 1 : 0);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `browse` INTEGER NOT NULL DEFAULT " + browse);
                    }
                })
                .addMigrations(new Migration(26, 27) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `sender` TEXT");
                        db.execSQL("CREATE INDEX `index_message_sender` ON `message` (`sender`)");
                    }
                })
                .addMigrations(new Migration(27, 28) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);

                        try (Cursor cursor = db.query("SELECT `id`, `from` FROM message")) {
                            while (cursor.moveToNext())
                                try {
                                    long id = cursor.getLong(0);
                                    String json = cursor.getString(1);
                                    Address[] from = Converters.decodeAddresses(json);
                                    String sender = MessageHelper.getSortKey(from);
                                    db.execSQL(
                                            "UPDATE message SET sender = ? WHERE id = ?",
                                            new Object[]{sender, id});
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }

                        }
                    }
                })
                .addMigrations(new Migration(28, 29) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync` INTEGER");
                    }
                })
                .addMigrations(new Migration(29, 30) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `encryption` INTEGER");
                        db.execSQL("UPDATE attachment SET encryption = " + EntityAttachment.PGP_MESSAGE + " where name = 'encrypted.asc'");
                    }
                })
                .addMigrations(new Migration(30, 31) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `disposition` TEXT");
                    }
                })
                .addMigrations(new Migration(31, 32) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_snoozed` INTEGER");
                        db.execSQL("CREATE INDEX `index_message_ui_snoozed` ON `message` (`ui_snoozed`)");
                    }
                })
                .addMigrations(new Migration(32, 33) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `realm` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `realm` TEXT");
                    }
                })
                .addMigrations(new Migration(33, 34) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `raw` INTEGER");
                    }
                })
                .addMigrations(new Migration(34, 35) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new Migration(35, 36) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new Migration(36, 37) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE TABLE `rule`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                                " `folder` INTEGER NOT NULL," +
                                " `name` TEXT NOT NULL," +
                                " `order` INTEGER NOT NULL," +
                                " `enabled` INTEGER NOT NULL," +
                                " `condition` TEXT NOT NULL," +
                                " `action` TEXT NOT NULL," +
                                " FOREIGN KEY(`folder`) REFERENCES `folder`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
                        db.execSQL("CREATE INDEX `index_rule_folder` ON `rule` (`folder`)");
                        db.execSQL("CREATE INDEX `index_rule_order` ON `rule` (`order`)");
                    }
                })
                .addMigrations(new Migration(37, 38) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `stop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(38, 39) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_left` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_right` INTEGER");
                    }
                })
                .addMigrations(new Migration(39, 40) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `last_connected` INTEGER");
                    }
                })
                .addMigrations(new Migration(40, 41) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `flags` TEXT");
                    }
                })
                .addMigrations(new Migration(41, 42) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `plain_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(42, 43) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `pop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(43, 44) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE TABLE IF NOT EXISTS `contact`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `type` INTEGER NOT NULL" +
                                ", `email` TEXT NOT NULL" +
                                ", `name` TEXT)");
                        db.execSQL("CREATE UNIQUE INDEX `index_contact_email_type` ON `contact` (`email`, `type`)");
                        db.execSQL("CREATE INDEX `index_contact_name_type` ON `contact` (`name`, `type`)");
                    }
                })
                .addMigrations(new Migration(44, 45) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `ondemand` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(45, 46) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `encrypt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(46, 47) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `use_ip` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(47, 48) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `identity` SET use_ip = 1");
                    }
                })
                .addMigrations(new Migration(48, 49) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE INDEX `index_operation_name` ON `operation` (`name`)");
                    }
                })
                .addMigrations(new Migration(49, 50) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP INDEX `index_message_replying`");
                        db.execSQL("DROP INDEX `index_message_forwarding`");
                        db.execSQL("CREATE INDEX `index_message_subject` ON `message` (`subject`)");
                    }
                })
                .addMigrations(new Migration(50, 51) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DELETE FROM operation WHERE name = '" + EntityOperation.WAIT + "'");
                    }
                })
                .addMigrations(new Migration(51, 52) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `total` INTEGER");
                    }
                })
                .addMigrations(new Migration(52, 53) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `account` INTEGER");
                    }
                })
                .addMigrations(new Migration(53, 54) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        File folder = new File(context.getFilesDir(), "attachments");
                        File[] attachments = folder.listFiles();
                        if (attachments != null)
                            for (File source : attachments) {
                                long id = Long.parseLong(source.getName().split("\\.")[0]);
                                Cursor cursor = null;
                                try {
                                    cursor = db.query("SELECT name FROM attachment WHERE id = ?", new Object[]{id});
                                    if (cursor != null && cursor.moveToNext()) {
                                        String name = cursor.getString(0);
                                        if (!TextUtils.isEmpty(name)) {
                                            File target = new File(folder, id + "." + Helper.sanitizeFilename(name));
                                            if (source.renameTo(target))
                                                Log.i("Renamed attachment=" + target.getName());
                                            else {
                                                Log.i("Unavailable attachment=" + source.getName());
                                                db.execSQL("UPDATE attachment SET available = 0 WHERE id = ?", new Object[]{id});
                                            }
                                        }
                                    }
                                } catch (Throwable ex) {
                                    if (cursor != null)
                                        cursor.close();
                                }
                            }
                    }
                })
                .addMigrations(new Migration(54, 55) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `avatar` TEXT");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `times_contacted` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `last_contacted` INTEGER");
                    }
                })
                .addMigrations(new Migration(55, 56) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `favorite` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(56, 57) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE INDEX `index_contact_times_contacted` ON `contact` (`times_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_last_contacted` ON `contact` (`last_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_favorite` ON `contact` (`favorite`)");
                    }
                })
                .addMigrations(new Migration(57, 58) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP TABLE `contact`");
                        db.execSQL("CREATE TABLE IF NOT EXISTS `contact`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `account` INTEGER NOT NULL" +
                                ", `type` INTEGER NOT NULL" +
                                ", `email` TEXT NOT NULL" +
                                ", `name` TEXT, `avatar` TEXT" +
                                ", `times_contacted` INTEGER NOT NULL" +
                                ", `first_contacted` INTEGER NOT NULL" +
                                ", `last_contacted` INTEGER NOT NULL" +
                                ", `state` INTEGER NOT NULL" +
                                ", FOREIGN KEY(`account`) REFERENCES `account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
                        db.execSQL("CREATE UNIQUE INDEX `index_contact_account_type_email` ON `contact` (`account`, `type`, `email`)");
                        db.execSQL("CREATE  INDEX `index_contact_email` ON `contact` (`email`)");
                        db.execSQL("CREATE  INDEX `index_contact_name` ON `contact` (`name`)");
                        db.execSQL("CREATE  INDEX `index_contact_times_contacted` ON `contact` (`times_contacted`)");
                        db.execSQL("CREATE  INDEX `index_contact_last_contacted` ON `contact` (`last_contacted`)");
                        db.execSQL("CREATE  INDEX `index_contact_state` ON `contact` (`state`)");
                    }
                })
                .addMigrations(new Migration(58, 59) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE  INDEX `index_contact_avatar` ON `contact` (`avatar`)");
                    }
                })
                .addMigrations(new Migration(59, 60) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `parent` INTEGER");
                    }
                })
                .addMigrations(new Migration(60, 61) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `collapsed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(61, 62) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new Migration(62, 63) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP INDEX index_message_msgid_folder");
                        db.execSQL("CREATE INDEX `index_message_msgid` ON `message` (`msgid`)");
                    }
                })
                .addMigrations(new Migration(63, 64) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dkim` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `spf` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dmarc` INTEGER");
                    }
                })
                .addMigrations(new Migration(64, 65) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(65, 66) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt_request` INTEGER");
                    }
                })
                .addMigrations(new Migration(66, 67) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revision` INTEGER");
                    }
                })
                .addMigrations(new Migration(67, 68) {
                    @Override
                    public void migrate(SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revisions` INTEGER");
                        db.execSQL("UPDATE message SET revisions = revision");
                    }
                })
                .build();
    }

    public static class Converters {
        @TypeConverter
        public static String[] toStringArray(String value) {
            if (value == null)
                return new String[0];
            else
                return TextUtils.split(value, " ");
        }

        @TypeConverter
        public static String fromStringArray(String[] value) {
            if (value == null || value.length == 0)
                return null;
            else
                return TextUtils.join(" ", value);
        }

        @TypeConverter
        public static String encodeAddresses(Address[] addresses) {
            if (addresses == null)
                return null;
            JSONArray jaddresses = new JSONArray();
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
                    Log.e(ex);
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
                Log.w(ex);
            }
            return result.toArray(new Address[0]);
        }
    }
}

