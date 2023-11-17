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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;

import javax.mail.Address;

public class DBMigration0 {
    static void migrate(final Context context, RoomDatabase.Builder<DB> builder) {
        // https://www.sqlite.org/lang_altertable.html
        builder
                .addMigrations(new androidx.room.migration.Migration(1, 2) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` RENAME COLUMN `after` TO `sync_days`");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keep_days` INTEGER NOT NULL DEFAULT 30");
                        db.execSQL("UPDATE `folder` SET keep_days = sync_days");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(2, 3) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `signature` TEXT");
                        db.execSQL("UPDATE `identity` SET signature =" +
                                " (SELECT account.signature FROM account WHERE account.id = identity.account)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(3, 4) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `forwarding` INTEGER" +
                                " REFERENCES `message`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL");
                        db.execSQL("CREATE INDEX `index_message_forwarding` ON `message` (`forwarding`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(4, 5) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `last_connected` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `last_attempt` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(5, 6) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(6, 7) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `answered` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_answered` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(7, 8) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(8, 9) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(9, 10) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_browsed` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("CREATE INDEX `index_message_ui_browsed` ON `message` (`ui_browsed`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(10, 11) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(11, 12) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                .addMigrations(new androidx.room.migration.Migration(12, 13) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_message_ui_flagged` ON `message` (`ui_flagged`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(13, 14) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `level` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(14, 15) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `sync_state` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(15, 16) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(16, 17) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DELETE FROM `message` WHERE ui_found");
                        db.execSQL("DROP INDEX `index_message_folder_uid_ui_found`");
                        db.execSQL("DROP INDEX `index_message_msgid_folder_ui_found`");
                        db.execSQL("CREATE UNIQUE INDEX `index_message_folder_uid` ON `message` (`folder`, `uid`)");
                        db.execSQL("CREATE UNIQUE INDEX `index_message_msgid_folder` ON `message` (`msgid`, `folder`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(17, 18) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbd` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(18, 19) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `delivery_receipt` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `read_receipt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(19, 20) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `folder` SET notify = unified");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(20, 21) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `display` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `bcc` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(21, 22) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `initialize` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("UPDATE `folder` SET sync_days = 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(22, 23) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `download` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(23, 24) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbc` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(24, 25) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `prefix` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(25, 26) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        int browse = (prefs.getBoolean("browse", true) ? 1 : 0);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `browse` INTEGER NOT NULL DEFAULT " + browse);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(26, 27) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `sender` TEXT");
                        db.execSQL("CREATE INDEX `index_message_sender` ON `message` (`sender`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(27, 28) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);

                        try (Cursor cursor = db.query("SELECT `id`, `from` FROM message")) {
                            while (cursor.moveToNext())
                                try {
                                    long id = cursor.getLong(0);
                                    String json = cursor.getString(1);
                                    Address[] from = DB.Converters.decodeAddresses(json);
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
                .addMigrations(new androidx.room.migration.Migration(28, 29) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(29, 30) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `encryption` INTEGER");
                        db.execSQL("UPDATE attachment SET encryption = " + EntityAttachment.PGP_MESSAGE + " where name = 'encrypted.asc'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(30, 31) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `disposition` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(31, 32) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_snoozed` INTEGER");
                        db.execSQL("CREATE INDEX `index_message_ui_snoozed` ON `message` (`ui_snoozed`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(32, 33) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `realm` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `realm` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(33, 34) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `raw` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(34, 35) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(35, 36) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(36, 37) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                .addMigrations(new androidx.room.migration.Migration(37, 38) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `stop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(38, 39) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_left` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_right` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(39, 40) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `last_connected` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(40, 41) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `flags` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(41, 42) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `plain_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(42, 43) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `pop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(43, 44) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE TABLE IF NOT EXISTS `contact`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `type` INTEGER NOT NULL" +
                                ", `email` TEXT NOT NULL" +
                                ", `name` TEXT)");
                        db.execSQL("CREATE UNIQUE INDEX `index_contact_email_type` ON `contact` (`email`, `type`)");
                        db.execSQL("CREATE INDEX `index_contact_name_type` ON `contact` (`name`, `type`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(44, 45) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `ondemand` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(45, 46) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `encrypt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(46, 47) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `use_ip` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(47, 48) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `identity` SET use_ip = 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(48, 49) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_operation_name` ON `operation` (`name`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(49, 50) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP INDEX `index_message_replying`");
                        db.execSQL("DROP INDEX `index_message_forwarding`");
                        db.execSQL("CREATE INDEX `index_message_subject` ON `message` (`subject`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(50, 51) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DELETE FROM operation WHERE name = 'wait'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(51, 52) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `total` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(52, 53) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `account` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(53, 54) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                .addMigrations(new androidx.room.migration.Migration(54, 55) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `avatar` TEXT");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `times_contacted` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `last_contacted` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(55, 56) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `favorite` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(56, 57) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_contact_times_contacted` ON `contact` (`times_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_last_contacted` ON `contact` (`last_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_favorite` ON `contact` (`favorite`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(57, 58) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                .addMigrations(new androidx.room.migration.Migration(58, 59) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE  INDEX `index_contact_avatar` ON `contact` (`avatar`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(59, 60) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `parent` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(60, 61) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `collapsed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(61, 62) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(62, 63) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP INDEX index_message_msgid_folder");
                        db.execSQL("CREATE INDEX `index_message_msgid` ON `message` (`msgid`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(63, 64) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dkim` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `spf` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dmarc` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(64, 65) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(65, 66) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt_request` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(66, 67) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revision` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(67, 68) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revisions` INTEGER");
                        db.execSQL("UPDATE message SET revisions = revision");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(68, 69) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt_to` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(69, 70) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE message SET uid = NULL WHERE uid < 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(70, 71) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `hide` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(71, 72) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `list_post` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(72, 73) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `order` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(73, 74) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `subscribed` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(74, 75) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `navigation` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(75, 76) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `order` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(76, 77) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `read_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(77, 78) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_delete` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(78, 79) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `plain_only` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(79, 80) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP INDEX index_attachment_message_cid");
                        db.execSQL("CREATE INDEX `index_attachment_message_cid` ON `attachment` (`message`, `cid`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(80, 81) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `state` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(81, 82) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_operation_account` ON `operation` (`account`)");
                        db.execSQL("CREATE INDEX `index_operation_state` ON `operation` (`state`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(82, 83) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(83, 84) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE attachment SET disposition = lower(disposition) WHERE NOT disposition IS NULL");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(84, 85) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE attachment SET size = NULL WHERE size = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(85, 86) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE VIEW `folderview` AS SELECT id, account, name, type, display, unified FROM folder");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(86, 87) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP VIEW IF EXISTS `folderview`");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(87, 88) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `partial_fetch` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(88, 89) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `separator` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(89, 90) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `notifying` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(90, 91) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `selectable` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(91, 92) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `account` SET poll_interval = 24");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(92, 93) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `mx` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(93, 94) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `encrypt` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(94, 95) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_key` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(95, 96) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `attachments` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE message SET attachments =" +
                                " (SELECT COUNT(attachment.id) FROM attachment WHERE attachment.message = message.id)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(96, 97) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `uidv` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(97, 98) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `rename` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(98, 99) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `signature` INTEGER NOT NULL DEFAULT 1");
                    }
                });
    }
}
