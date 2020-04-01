package eu.faircode.email;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.getkeepsafe.relinker.ReLinker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory;
import io.requery.android.database.sqlite.SQLiteDatabase;

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

// https://developer.android.com/topic/libraries/architecture/room.html

@Database(
        version = 152,
        entities = {
                EntityIdentity.class,
                EntityAccount.class,
                EntityFolder.class,
                EntityMessage.class,
                EntityAttachment.class,
                EntityOperation.class,
                EntityContact.class,
                EntityCertificate.class,
                EntityAnswer.class,
                EntityRule.class,
                EntityLog.class
        },
        views = {
                TupleAccountView.class,
                TupleIdentityView.class,
                TupleFolderView.class
        }
)

@TypeConverters({DB.Converters.class})
public abstract class DB extends RoomDatabase {
    public abstract DaoAccount account();

    public abstract DaoIdentity identity();

    public abstract DaoFolder folder();

    public abstract DaoMessage message();

    public abstract DaoAttachment attachment();

    public abstract DaoOperation operation();

    public abstract DaoContact contact();

    public abstract DaoCertificate certificate();

    public abstract DaoAnswer answer();

    public abstract DaoRule rule();

    public abstract DaoLog log();

    private static DB sInstance;
    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "query");

    private static final String DB_NAME = "fairemail";
    private static final int DB_CHECKPOINT = 1000; // requery/sqlite-android default

    private static final String[] DB_TABLES = new String[]{
            "identity", "account", "folder", "message", "attachment", "operation", "contact", "certificate", "answer", "rule", "log"};

    @Override
    public void init(@NonNull DatabaseConfiguration configuration) {
        // https://www.sqlite.org/pragma.html#pragma_wal_autocheckpoint
        if (BuildConfig.DEBUG) {
            File dbfile = configuration.context.getDatabasePath(DB_NAME);
            if (dbfile.exists()) {
                try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbfile.getPath(), null, SQLiteDatabase.OPEN_READWRITE)) {
                    Log.i("DB checkpoint=" + DB_CHECKPOINT);
                    try (Cursor cursor = db.rawQuery("PRAGMA wal_autocheckpoint=" + DB_CHECKPOINT + ";", null)) {
                        cursor.moveToNext(); // required
                    }
                }
            }
        }

        super.init(configuration);
    }

    public static synchronized DB getInstance(Context context) {
        if (sInstance == null) {
            Context acontext = context.getApplicationContext();

            sInstance = migrate(acontext, getBuilder(acontext)).build();

            try {
                Log.i("Disabling view invalidation");
                Field fmViewTables = InvalidationTracker.class.getDeclaredField("mViewTables");
                fmViewTables.setAccessible(true);
                Map<String, Set<String>> mViewTables = (Map) fmViewTables.get(sInstance.getInvalidationTracker());
                mViewTables.get("account_view").clear();
                mViewTables.get("identity_view").clear();
                mViewTables.get("folder_view").clear();
                Log.i("Disabled view invalidation");
            } catch (ReflectiveOperationException ex) {
                Log.e(ex);
            }

            sInstance.getInvalidationTracker().addObserver(new InvalidationTracker.Observer(DB.DB_TABLES) {
                @Override
                public void onInvalidated(@NonNull Set<String> tables) {
                    Log.d("ROOM invalidated=" + TextUtils.join(",", tables));
                }
            });
        }

        return sInstance;
    }

    private static RoomDatabase.Builder<DB> getBuilder(Context context) {
        try {
            ReLinker.log(new ReLinker.Logger() {
                @Override
                public void log(String message) {
                    Log.i("Relinker: " + message);
                }
            }).loadLibrary(context, "sqlite3x");
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return Room
                .databaseBuilder(context, DB.class, DB_NAME)
                .openHelperFactory(new RequerySQLiteOpenHelperFactory())
                .setQueryExecutor(executor)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // using the latest sqlite
                .addCallback(new Callback() {
                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        Log.i("Database version=" + db.getVersion());

                        createTriggers(db);
                    }
                });
    }

    private static void createTriggers(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER IF NOT EXISTS attachment_insert" +
                " AFTER INSERT ON attachment" +
                " BEGIN" +
                "  UPDATE message SET attachments = attachments + 1" +
                "  WHERE message.id = NEW.message" +
                "  AND (NEW.encryption IS NULL OR NEW.encryption = 0);" +
                " END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS attachment_delete" +
                " AFTER DELETE ON attachment" +
                " BEGIN" +
                "  UPDATE message SET attachments = attachments - 1" +
                "  WHERE message.id = OLD.message" +
                "  AND (OLD.encryption IS NULL OR OLD.encryption = 0);" +
                " END");
    }

    private static RoomDatabase.Builder<DB> migrate(final Context context, RoomDatabase.Builder<DB> builder) {
        // https://www.sqlite.org/lang_altertable.html
        return builder
                .addMigrations(new Migration(1, 2) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` RENAME COLUMN `after` TO `sync_days`");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keep_days` INTEGER NOT NULL DEFAULT 30");
                        db.execSQL("UPDATE `folder` SET keep_days = sync_days");
                    }
                })
                .addMigrations(new Migration(2, 3) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `signature` TEXT");
                        db.execSQL("UPDATE `identity` SET signature =" +
                                " (SELECT account.signature FROM account WHERE account.id = identity.account)");
                    }
                })
                .addMigrations(new Migration(3, 4) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `forwarding` INTEGER" +
                                " REFERENCES `message`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL");
                        db.execSQL("CREATE INDEX `index_message_forwarding` ON `message` (`forwarding`)");
                    }
                })
                .addMigrations(new Migration(4, 5) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `last_connected` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `last_attempt` INTEGER");
                    }
                })
                .addMigrations(new Migration(5, 6) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(6, 7) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `answered` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_answered` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(7, 8) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new Migration(8, 9) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new Migration(9, 10) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_browsed` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("CREATE INDEX `index_message_ui_browsed` ON `message` (`ui_browsed`)");
                    }
                })
                .addMigrations(new Migration(10, 11) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new Migration(11, 12) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE INDEX `index_message_ui_flagged` ON `message` (`ui_flagged`)");
                    }
                })
                .addMigrations(new Migration(13, 14) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `level` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(14, 15) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `sync_state` TEXT");
                    }
                })
                .addMigrations(new Migration(15, 16) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(16, 17) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbd` INTEGER");
                    }
                })
                .addMigrations(new Migration(18, 19) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `delivery_receipt` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `read_receipt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(19, 20) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `folder` SET notify = unified");
                    }
                })
                .addMigrations(new Migration(20, 21) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `display` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `bcc` TEXT");
                    }
                })
                .addMigrations(new Migration(21, 22) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `initialize` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("UPDATE `folder` SET sync_days = 1");
                    }
                })
                .addMigrations(new Migration(22, 23) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `download` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(23, 24) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbc` INTEGER");
                    }
                })
                .addMigrations(new Migration(24, 25) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `prefix` TEXT");
                    }
                })
                .addMigrations(new Migration(25, 26) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        int browse = (prefs.getBoolean("browse", true) ? 1 : 0);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `browse` INTEGER NOT NULL DEFAULT " + browse);
                    }
                })
                .addMigrations(new Migration(26, 27) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `sender` TEXT");
                        db.execSQL("CREATE INDEX `index_message_sender` ON `message` (`sender`)");
                    }
                })
                .addMigrations(new Migration(27, 28) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync` INTEGER");
                    }
                })
                .addMigrations(new Migration(29, 30) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `encryption` INTEGER");
                        db.execSQL("UPDATE attachment SET encryption = " + EntityAttachment.PGP_MESSAGE + " where name = 'encrypted.asc'");
                    }
                })
                .addMigrations(new Migration(30, 31) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `disposition` TEXT");
                    }
                })
                .addMigrations(new Migration(31, 32) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_snoozed` INTEGER");
                        db.execSQL("CREATE INDEX `index_message_ui_snoozed` ON `message` (`ui_snoozed`)");
                    }
                })
                .addMigrations(new Migration(32, 33) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `realm` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `realm` TEXT");
                    }
                })
                .addMigrations(new Migration(33, 34) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `raw` INTEGER");
                    }
                })
                .addMigrations(new Migration(34, 35) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new Migration(35, 36) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new Migration(36, 37) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `stop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(38, 39) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_left` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_right` INTEGER");
                    }
                })
                .addMigrations(new Migration(39, 40) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `last_connected` INTEGER");
                    }
                })
                .addMigrations(new Migration(40, 41) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `flags` TEXT");
                    }
                })
                .addMigrations(new Migration(41, 42) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `plain_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(42, 43) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `pop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(43, 44) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `ondemand` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(45, 46) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `encrypt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(46, 47) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `use_ip` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(47, 48) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `identity` SET use_ip = 1");
                    }
                })
                .addMigrations(new Migration(48, 49) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE INDEX `index_operation_name` ON `operation` (`name`)");
                    }
                })
                .addMigrations(new Migration(49, 50) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP INDEX `index_message_replying`");
                        db.execSQL("DROP INDEX `index_message_forwarding`");
                        db.execSQL("CREATE INDEX `index_message_subject` ON `message` (`subject`)");
                    }
                })
                .addMigrations(new Migration(50, 51) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DELETE FROM operation WHERE name = 'wait'");
                    }
                })
                .addMigrations(new Migration(51, 52) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `total` INTEGER");
                    }
                })
                .addMigrations(new Migration(52, 53) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `account` INTEGER");
                    }
                })
                .addMigrations(new Migration(53, 54) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `avatar` TEXT");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `times_contacted` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `last_contacted` INTEGER");
                    }
                })
                .addMigrations(new Migration(55, 56) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `favorite` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(56, 57) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE INDEX `index_contact_times_contacted` ON `contact` (`times_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_last_contacted` ON `contact` (`last_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_favorite` ON `contact` (`favorite`)");
                    }
                })
                .addMigrations(new Migration(57, 58) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
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
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE  INDEX `index_contact_avatar` ON `contact` (`avatar`)");
                    }
                })
                .addMigrations(new Migration(59, 60) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `parent` INTEGER");
                    }
                })
                .addMigrations(new Migration(60, 61) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `collapsed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(61, 62) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new Migration(62, 63) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP INDEX index_message_msgid_folder");
                        db.execSQL("CREATE INDEX `index_message_msgid` ON `message` (`msgid`)");
                    }
                })
                .addMigrations(new Migration(63, 64) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dkim` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `spf` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dmarc` INTEGER");
                    }
                })
                .addMigrations(new Migration(64, 65) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(65, 66) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt_request` INTEGER");
                    }
                })
                .addMigrations(new Migration(66, 67) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revision` INTEGER");
                    }
                })
                .addMigrations(new Migration(67, 68) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revisions` INTEGER");
                        db.execSQL("UPDATE message SET revisions = revision");
                    }
                })
                .addMigrations(new Migration(68, 69) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt_to` TEXT");
                    }
                })
                .addMigrations(new Migration(69, 70) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE message SET uid = NULL WHERE uid < 0");
                    }
                })
                .addMigrations(new Migration(70, 71) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `hide` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(71, 72) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `list_post` TEXT");
                    }
                })
                .addMigrations(new Migration(72, 73) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `order` INTEGER");
                    }
                })
                .addMigrations(new Migration(73, 74) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `subscribed` INTEGER");
                    }
                })
                .addMigrations(new Migration(74, 75) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `navigation` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(75, 76) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `order` INTEGER");
                    }
                })
                .addMigrations(new Migration(76, 77) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `read_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(77, 78) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_delete` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(78, 79) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `plain_only` INTEGER");
                    }
                })
                .addMigrations(new Migration(79, 80) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP INDEX index_attachment_message_cid");
                        db.execSQL("CREATE INDEX `index_attachment_message_cid` ON `attachment` (`message`, `cid`)");
                    }
                })
                .addMigrations(new Migration(80, 81) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `state` TEXT");
                    }
                })
                .addMigrations(new Migration(81, 82) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE INDEX `index_operation_account` ON `operation` (`account`)");
                        db.execSQL("CREATE INDEX `index_operation_state` ON `operation` (`state`)");
                    }
                })
                .addMigrations(new Migration(82, 83) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new Migration(83, 84) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE attachment SET disposition = lower(disposition) WHERE NOT disposition IS NULL");
                    }
                })
                .addMigrations(new Migration(84, 85) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE attachment SET size = NULL WHERE size = 0");
                    }
                })
                .addMigrations(new Migration(85, 86) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE VIEW `folderview` AS SELECT id, account, name, type, display, unified FROM folder");
                    }
                })
                .addMigrations(new Migration(86, 87) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP VIEW `folderview`");
                    }
                })
                .addMigrations(new Migration(87, 88) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `partial_fetch` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(88, 89) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `separator` INTEGER");
                    }
                })
                .addMigrations(new Migration(89, 90) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `notifying` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(90, 91) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `selectable` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(91, 92) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `account` SET poll_interval = 24");
                    }
                })
                .addMigrations(new Migration(92, 93) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `mx` INTEGER");
                    }
                })
                .addMigrations(new Migration(93, 94) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `encrypt` INTEGER");
                    }
                })
                .addMigrations(new Migration(94, 95) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_key` INTEGER");
                    }
                })
                .addMigrations(new Migration(95, 96) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `attachments` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE message SET attachments =" +
                                " (SELECT COUNT(attachment.id) FROM attachment WHERE attachment.message = message.id)");
                    }
                })
                .addMigrations(new Migration(96, 97) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `uidv` INTEGER");
                    }
                })
                .addMigrations(new Migration(97, 98) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `rename` TEXT");
                    }
                })
                .addMigrations(new Migration(98, 99) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `signature` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(99, 100) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `unsubscribe` TEXT");
                    }
                })
                .addMigrations(new Migration(100, 101) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra_regex` TEXT");
                    }
                })
                .addMigrations(new Migration(101, 102) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `auto_seen` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(102, 103) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `account` SET browse = 1 WHERE pop = 1");
                    }
                })
                .addMigrations(new Migration(103, 104) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `message` SET ui_hide = 1 WHERE ui_hide <> 0");
                    }
                })
                .addMigrations(new Migration(104, 105) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `priority` INTEGER");
                    }
                })
                .addMigrations(new Migration(105, 106) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `total` INTEGER");
                        db.execSQL("UPDATE `message` SET total = size");
                    }
                })
                .addMigrations(new Migration(106, 107) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt` INTEGER");
                    }
                })
                .addMigrations(new Migration(107, 108) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new Migration(108, 109) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `ignore_size` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(109, 110) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_busy` INTEGER");
                    }
                })
                .addMigrations(new Migration(110, 111) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `applied` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(111, 112) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `move_to` INTEGER");
                    }
                })
                .addMigrations(new Migration(112, 113) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE TABLE IF NOT EXISTS `revision`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `message` INTEGER NOT NULL" +
                                ", `sequence` INTEGER NOT NULL" +
                                ", `reference` INTEGER NOT NULL" +
                                ", FOREIGN KEY(`message`) REFERENCES `message`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_revision_message` ON `revision` (`message`)");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_revision_message_sequence` ON `revision` (`message`, `sequence`)");
                    }

                })
                .addMigrations(new Migration(113, 114) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE message SET encrypt = 1 WHERE id IN " +
                                "(SELECT DISTINCT message FROM attachment" +
                                " WHERE encryption = " + EntityAttachment.PGP_MESSAGE + ")");
                    }
                })
                .addMigrations(new Migration(114, 115) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP TABLE revision");
                    }
                })
                .addMigrations(new Migration(115, 116) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `use_date` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(116, 117) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE TABLE IF NOT EXISTS `certificate`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `subject` TEXT NOT NULL" +
                                ", `email` TEXT" +
                                ", `data` TEXT NOT NULL)");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_certificate_subject` ON `certificate` (`subject`)");
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_certificate_email` ON `certificate` (`email`)");
                    }
                })
                .addMigrations(new Migration(117, 118) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP TABLE IF EXISTS `certificate`");
                        db.execSQL("CREATE TABLE IF NOT EXISTS `certificate`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `fingerprint` TEXT NOT NULL" +
                                ", `email` TEXT NOT NULL" +
                                ", `subject` TEXT" +
                                ", `data` TEXT NOT NULL)");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_certificate_fingerprint_email` ON `certificate` (`fingerprint`, `email`)");
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_certificate_email` ON `certificate` (`email`)");
                    }
                })
                .addMigrations(new Migration(118, 119) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_key_alias` TEXT");
                    }
                })
                .addMigrations(new Migration(119, 120) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `after` INTEGER");
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `before` INTEGER");
                    }
                })
                .addMigrations(new Migration(120, 121) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `account` SET ondemand = 0");
                    }
                })
                .addMigrations(new Migration(121, 122) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `message` SET raw = NULL");

                        File[] raws = new File(context.getFilesDir(), "raw").listFiles();
                        if (raws != null)
                            for (File file : raws)
                                file.delete();
                    }
                })
                .addMigrations(new Migration(122, 123) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `fingerprint` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `fingerprint` TEXT");
                    }
                })
                .addMigrations(new Migration(123, 124) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `provider` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `provider` TEXT");
                    }
                })
                .addMigrations(new Migration(124, 125) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        int previous_version = prefs.getInt("previous_version", -1);
                        if (previous_version <= 848 && Helper.isPlayStoreInstall()) {
                            // JavaMail didn't check server certificates
                            db.execSQL("UPDATE account SET insecure = 1 WHERE auth_type = " + EmailService.AUTH_TYPE_PASSWORD);
                            db.execSQL("UPDATE identity SET insecure = 1 WHERE auth_type = " + EmailService.AUTH_TYPE_PASSWORD);
                        }
                    }
                })
                .addMigrations(new Migration(125, 126) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `autocrypt` TEXT");
                    }
                })
                .addMigrations(new Migration(126, 127) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_ok` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_failed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(127, 128) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `quota_usage` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `quota_limit` INTEGER");
                    }
                })
                .addMigrations(new Migration(128, 129) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `poll_exempted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(129, 130) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `fts` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(130, 131) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `favorite` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(131, 132) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_encrypt` INTEGER");
                        db.execSQL("UPDATE `message` SET `ui_encrypt` = `encrypt`");
                    }
                })
                .addMigrations(new Migration(132, 133) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_on_server` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_on_device` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `account` SET `leave_on_server` = `browse` WHERE `pop` = " + EntityAccount.TYPE_POP);
                    }
                })
                .addMigrations(new Migration(133, 134) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        createTriggers(db);
                    }
                })
                .addMigrations(new Migration(134, 135) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                        db.execSQL("CREATE VIEW IF NOT EXISTS `identity_view` AS " + TupleIdentityView.query);
                        db.execSQL("CREATE VIEW IF NOT EXISTS `folder_view` AS " + TupleFolderView.query);
                    }
                })
                .addMigrations(new Migration(135, 136) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `intermediate` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(136, 137) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `submitter` TEXT");
                    }
                })
                .addMigrations(new Migration(137, 138) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `importance` INTEGER");
                    }
                })
                .addMigrations(new Migration(138, 139) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                })
                .addMigrations(new Migration(139, 140) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                })
                .addMigrations(new Migration(140, 141) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `certificate` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `certificate` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(141, 142) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `certificate_alias` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `certificate_alias` TEXT");
                    }
                })
                .addMigrations(new Migration(142, 143) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `tries` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(143, 144) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `inferiors` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new Migration(144, 145) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll_factor` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll_count` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(145, 146) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `max_messages` INTEGER");
                    }
                })
                .addMigrations(new Migration(146, 147) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `thread` INTEGER");
                    }
                })
                .addMigrations(new Migration(147, 148) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `cc` TEXT");
                    }
                })
                .addMigrations(new Migration(148, 149) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_deleted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new Migration(149, 150) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `language` TEXT");
                    }
                })
                .addMigrations(new Migration(150, 151) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_succeeded` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE account SET keep_alive_failed = 0, keep_alive_ok = 0");
                    }
                })
                .addMigrations(new Migration(151, 152) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Log.i("DB migration from version " + startVersion + " to " + endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `hash` TEXT");
                    }
                });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void beginTransaction() {
        super.beginTransaction();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setTransactionSuccessful() {
        super.setTransactionSuccessful();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void endTransaction() {
        super.endTransaction();
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
                JSONArray jroot = new JSONArray(json);
                JSONArray jaddresses = new JSONArray();
                for (int i = 0; i < jroot.length(); i++) {
                    Object item = jroot.get(i);
                    if (jroot.get(i) instanceof JSONArray)
                        for (int j = 0; j < ((JSONArray) item).length(); j++)
                            jaddresses.put(((JSONArray) item).get(j));
                    else
                        jaddresses.put(item);
                }
                for (int i = 0; i < jaddresses.length(); i++) {
                    JSONObject jaddress = (JSONObject) jaddresses.get(i);
                    String email = jaddress.getString("address");
                    String personal = jaddress.optString("personal");
                    if (TextUtils.isEmpty(personal))
                        personal = null;
                    result.add(new InternetAddress(email, personal, StandardCharsets.UTF_8.name()));
                }
            } catch (Throwable ex) {
                // Compose can store invalid addresses
                Log.w(ex);
            }
            return result.toArray(new Address[0]);
        }
    }
}

