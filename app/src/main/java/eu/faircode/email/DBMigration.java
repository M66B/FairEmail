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

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.mail.Address;

public class DBMigration {
    static RoomDatabase.Builder<DB> migrate(final Context context, RoomDatabase.Builder<DB> builder) {
        // https://www.sqlite.org/lang_altertable.html
        return builder
                .addMigrations(new androidx.room.migration.Migration(1, 2) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` RENAME COLUMN `after` TO `sync_days`");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keep_days` INTEGER NOT NULL DEFAULT 30");
                        db.execSQL("UPDATE `folder` SET keep_days = sync_days");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(2, 3) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `signature` TEXT");
                        db.execSQL("UPDATE `identity` SET signature =" +
                                " (SELECT account.signature FROM account WHERE account.id = identity.account)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(3, 4) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `forwarding` INTEGER" +
                                " REFERENCES `message`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL");
                        db.execSQL("CREATE INDEX `index_message_forwarding` ON `message` (`forwarding`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(4, 5) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `last_connected` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `last_attempt` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(5, 6) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(6, 7) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `answered` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_answered` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(7, 8) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(8, 9) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `keywords` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(9, 10) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_browsed` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("CREATE INDEX `index_message_ui_browsed` ON `message` (`ui_browsed`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(10, 11) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(11, 12) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_message_ui_flagged` ON `message` (`ui_flagged`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(13, 14) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `level` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(14, 15) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `sync_state` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(15, 16) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(16, 17) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `tbd` INTEGER");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbd` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(18, 19) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `delivery_receipt` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `read_receipt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(19, 20) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `notify` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `folder` SET notify = unified");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(20, 21) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `display` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `bcc` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(21, 22) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `initialize` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("UPDATE `folder` SET sync_days = 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(22, 23) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `download` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(23, 24) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `tbc` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(24, 25) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `prefix` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(25, 26) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        int browse = (prefs.getBoolean("browse", true) ? 1 : 0);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `browse` INTEGER NOT NULL DEFAULT " + browse);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(26, 27) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `sender` TEXT");
                        db.execSQL("CREATE INDEX `index_message_sender` ON `message` (`sender`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(27, 28) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);

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
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(29, 30) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `encryption` INTEGER");
                        db.execSQL("UPDATE attachment SET encryption = " + EntityAttachment.PGP_MESSAGE + " where name = 'encrypted.asc'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(30, 31) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `disposition` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(31, 32) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_snoozed` INTEGER");
                        db.execSQL("CREATE INDEX `index_message_ui_snoozed` ON `message` (`ui_snoozed`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(32, 33) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `realm` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `realm` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(33, 34) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `raw` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(34, 35) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `error` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(35, 36) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(36, 37) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `stop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(38, 39) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_left` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `swipe_right` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(39, 40) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `last_connected` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(40, 41) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `flags` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(41, 42) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `plain_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(42, 43) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `pop` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(43, 44) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `ondemand` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(45, 46) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `encrypt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(46, 47) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `use_ip` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(47, 48) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `identity` SET use_ip = 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(48, 49) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_operation_name` ON `operation` (`name`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(49, 50) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP INDEX `index_message_replying`");
                        db.execSQL("DROP INDEX `index_message_forwarding`");
                        db.execSQL("CREATE INDEX `index_message_subject` ON `message` (`subject`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(50, 51) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DELETE FROM operation WHERE name = 'wait'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(51, 52) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `total` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(52, 53) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `account` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(53, 54) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `avatar` TEXT");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `times_contacted` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `last_contacted` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(55, 56) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `favorite` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(56, 57) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_contact_times_contacted` ON `contact` (`times_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_last_contacted` ON `contact` (`last_contacted`)");
                        db.execSQL("CREATE INDEX `index_contact_favorite` ON `contact` (`favorite`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(57, 58) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE  INDEX `index_contact_avatar` ON `contact` (`avatar`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(59, 60) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `parent` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(60, 61) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `collapsed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(61, 62) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `warning` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(62, 63) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP INDEX index_message_msgid_folder");
                        db.execSQL("CREATE INDEX `index_message_msgid` ON `message` (`msgid`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(63, 64) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dkim` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `spf` INTEGER");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `dmarc` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(64, 65) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(65, 66) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt_request` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(66, 67) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revision` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(67, 68) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `revisions` INTEGER");
                        db.execSQL("UPDATE message SET revisions = revision");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(68, 69) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt_to` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(69, 70) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE message SET uid = NULL WHERE uid < 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(70, 71) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `hide` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(71, 72) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `list_post` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(72, 73) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `order` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(73, 74) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `subscribed` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(74, 75) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `navigation` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(75, 76) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `order` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(76, 77) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `read_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(77, 78) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_delete` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(78, 79) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `plain_only` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(79, 80) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP INDEX index_attachment_message_cid");
                        db.execSQL("CREATE INDEX `index_attachment_message_cid` ON `attachment` (`message`, `cid`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(80, 81) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `state` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(81, 82) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_operation_account` ON `operation` (`account`)");
                        db.execSQL("CREATE INDEX `index_operation_state` ON `operation` (`state`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(82, 83) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(83, 84) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE attachment SET disposition = lower(disposition) WHERE NOT disposition IS NULL");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(84, 85) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE attachment SET size = NULL WHERE size = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(85, 86) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE VIEW `folderview` AS SELECT id, account, name, type, display, unified FROM folder");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(86, 87) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP VIEW IF EXISTS `folderview`");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(87, 88) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `partial_fetch` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(88, 89) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `separator` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(89, 90) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `notifying` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(90, 91) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `selectable` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(91, 92) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `account` SET poll_interval = 24");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(92, 93) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `mx` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(93, 94) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `encrypt` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(94, 95) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_key` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(95, 96) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `attachments` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE message SET attachments =" +
                                " (SELECT COUNT(attachment.id) FROM attachment WHERE attachment.message = message.id)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(96, 97) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `uidv` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(97, 98) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `rename` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(98, 99) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `signature` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(99, 100) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `unsubscribe` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(100, 101) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra_regex` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(101, 102) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `auto_seen` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(102, 103) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `account` SET browse = 1 WHERE pop = 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(103, 104) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET ui_hide = 1 WHERE ui_hide <> 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(104, 105) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `priority` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(105, 106) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `total` INTEGER");
                        db.execSQL("UPDATE `message` SET total = size");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(106, 107) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(107, 108) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(108, 109) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `ignore_size` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(109, 110) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_busy` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(110, 111) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `applied` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(111, 112) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `move_to` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(112, 113) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                .addMigrations(new androidx.room.migration.Migration(113, 114) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE message SET encrypt = 1 WHERE id IN " +
                                "(SELECT DISTINCT message FROM attachment" +
                                " WHERE encryption = " + EntityAttachment.PGP_MESSAGE + ")");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(114, 115) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP TABLE revision");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(115, 116) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `use_date` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(116, 117) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE TABLE IF NOT EXISTS `certificate`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", `subject` TEXT NOT NULL" +
                                ", `email` TEXT" +
                                ", `data` TEXT NOT NULL)");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_certificate_subject` ON `certificate` (`subject`)");
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_certificate_email` ON `certificate` (`email`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(117, 118) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
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
                .addMigrations(new androidx.room.migration.Migration(118, 119) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_key_alias` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(119, 120) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `after` INTEGER");
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `before` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(120, 121) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `account` SET ondemand = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(121, 122) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET raw = NULL");

                        File[] raws = new File(context.getFilesDir(), "raw").listFiles();
                        if (raws != null)
                            for (File file : raws)
                                Helper.secureDelete(file);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(122, 123) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `fingerprint` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `fingerprint` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(123, 124) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `provider` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `provider` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(124, 125) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        int previous_version = prefs.getInt("previous_version", -1);
                        if (previous_version <= 848 && Helper.isPlayStoreInstall()) {
                            // JavaMail didn't check server certificates
                            db.execSQL("UPDATE account SET insecure = 1 WHERE auth_type = " + AUTH_TYPE_PASSWORD);
                            db.execSQL("UPDATE identity SET insecure = 1 WHERE auth_type = " + AUTH_TYPE_PASSWORD);
                        }
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(125, 126) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `autocrypt` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(126, 127) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_ok` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_failed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(127, 128) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `quota_usage` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `quota_limit` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(128, 129) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `poll_exempted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(129, 130) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `fts` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(130, 131) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `favorite` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(131, 132) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_encrypt` INTEGER");
                        db.execSQL("UPDATE `message` SET `ui_encrypt` = `encrypt`");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(132, 133) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_on_server` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_on_device` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `account` SET `leave_on_server` = `browse` WHERE `pop` = " + EntityAccount.TYPE_POP);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(133, 134) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(134, 135) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `identity_view` AS " + TupleIdentityView.query);
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `folder_view` AS " + TupleFolderView.query);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(135, 136) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `intermediate` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(136, 137) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `submitter` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(137, 138) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `importance` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(138, 139) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(139, 140) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(140, 141) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `certificate` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `certificate` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(141, 142) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `certificate_alias` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `certificate_alias` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(142, 143) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `tries` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(143, 144) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `inferiors` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(144, 145) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll_factor` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll_count` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(145, 146) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `max_messages` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(146, 147) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `thread` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(147, 148) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `cc` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(148, 149) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_deleted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(149, 150) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `language` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(150, 151) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_succeeded` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE account SET keep_alive_failed = 0, keep_alive_ok = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(151, 152) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `hash` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(152, 153) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `unicode` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(153, 154) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `ehlo` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(154, 155) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `folder` SET `poll` = 1 WHERE `synchronize` = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(155, 156) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_inreplyto` ON `message` (`inreplyto`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(156, 157) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `wasforwardedfrom` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(157, 158) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `uidl` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(158, 159) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_unsnoozed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(159, 160) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(160, 161) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        String encrypt_method = prefs.getString("default_encrypt_method", "pgp");
                        db.execSQL("UPDATE identity SET encrypt = " + ("pgp".equals(encrypt_method) ? 0 : 1));
                        prefs.edit().remove("default_encrypt_method").apply();
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(161, 162) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `verified` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(162, 163) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(163, 164) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(164, 165) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attachment_message_type` ON `attachment` (`message`, `type`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(165, 166) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        db.execSQL("DROP INDEX `index_attachment_message_type`");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(166, 167) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `labels` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(167, 168) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `self` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(168, 169) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `max_size` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(169, 170) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `max_size` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(170, 171) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(171, 172) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `use_received` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(172, 173) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `subsequence` INTEGER");
                        db.execSQL("DROP INDEX `index_attachment_message_sequence`");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attachment_message_sequence_subsequence` ON `attachment` (`message`, `sequence`, `subsequence`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(173, 174) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `group` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(174, 175) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `standard` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(175, 176) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `auto_submitted` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(176, 177) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `backoff_until` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(177, 178) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE folder" +
                                " SET poll = 1" +
                                " WHERE type <> '" + EntityFolder.INBOX + "'" +
                                " AND account IN" +
                                "  (SELECT id FROM account" +
                                "   WHERE host IN ('imap.arcor.de'))");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(178, 179) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `local` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(179, 180) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `reply_domain` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(180, 181) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `last_applied` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(181, 182) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_classify` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(182, 183) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `auto_classified` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(183, 184) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_classify_source` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_classify_target` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `folder`" +
                                " SET auto_classify_source = 1" +
                                " WHERE (SELECT pop FROM account WHERE id = folder.account) = " + EntityAccount.TYPE_IMAP +
                                " AND (auto_classify" +
                                " OR type = '" + EntityFolder.INBOX + "'" +
                                " OR type = '" + EntityFolder.JUNK + "')");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(184, 185) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `notes` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(185, 186) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `return_path` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(186, 187) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `deleted` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_deleted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(187, 188) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_silent` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(188, 189) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_default` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `encrypt_default` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(189, 190) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `selected_count` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(190, 191) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `selected_last` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(191, 192) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `modseq` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(192, 193) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `notes_color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(193, 194) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra_name` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(194, 195) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `receipt` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(195, 196) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `internal` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(196, 197) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `applied` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `last_applied` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(197, 198) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `show_images` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `show_full` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(198, 199) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capability_idle` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capability_utf8` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(199, 200) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `blocklist` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(200, 201) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `bimi_selector` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(201, 202) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE identity SET use_ip = 0 WHERE host = 'smtp.office365.com'");
                    }
                }).addMigrations(new androidx.room.migration.Migration(202, 203) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync_count` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(203, 204) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''");
                        try (Cursor cursor = db.query("SELECT id FROM account")) {
                            while (cursor != null && cursor.moveToNext()) {
                                long id = cursor.getLong(0);
                                String uuid = UUID.randomUUID().toString();
                                db.execSQL("UPDATE account SET uuid = ? WHERE id = ?", new Object[]{uuid, id});
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                }).addMigrations(new androidx.room.migration.Migration(204, 205) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `external` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(205, 206) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capabilities` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(206, 207) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        //db.execSQL("DROP VIEW IF EXISTS `account_view`");
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                    }
                }).addMigrations(new androidx.room.migration.Migration(207, 208) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `type` INTEGER NOT NULL DEFAULT " + EntityLog.Type.General.ordinal());
                    }
                }).addMigrations(new androidx.room.migration.Migration(208, 209) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `account` INTEGER");
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `folder` INTEGER");
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `message` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(209, 210) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `namespace` TEXT");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `separator` INTEGER");
                        db.execSQL("UPDATE folder SET separator =" +
                                " (SELECT separator FROM account WHERE account.id = folder.account)");
                    }
                }).addMigrations(new androidx.room.migration.Migration(210, 211) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE TABLE `search`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", name TEXT NOT NULL" +
                                ", `data` TEXT NOT NULL)");
                    }
                }).addMigrations(new androidx.room.migration.Migration(211, 212) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `color` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(212, 213) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `category` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(213, 214) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_account_synchronize` ON `account` (`synchronize`)");
                        db.execSQL("CREATE INDEX `index_account_category` ON `account` (`category`)");
                        //db.execSQL("DROP VIEW IF EXISTS `account_view`");
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                    }
                }).addMigrations(new androidx.room.migration.Migration(214, 215) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_add` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(215, 216) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `infrastructure` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(216, 217) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync_foreground` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(217, 218) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `smtp_from` TEXT");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `from_domain` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(218, 219) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `resend` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(219, 220) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `tls` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(220, 221) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `sensitivity` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(221, 222) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `color` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(222, 223) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `related` INTEGER");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                }).addMigrations(new androidx.room.migration.Migration(223, 224) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `media_uri` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(224, 225) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE folder" +
                                " SET auto_delete = 0" +
                                " WHERE type ='" + EntityFolder.JUNK + "'");
                    }
                }).addMigrations(new androidx.room.migration.Migration(225, 226) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `snippet` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(226, 227) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                }).addMigrations(new androidx.room.migration.Migration(227, 228) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `group` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(228, 229) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("DROP VIEW IF EXISTS `identity_view`");
                        db.execSQL("CREATE VIEW IF NOT EXISTS `identity_view` AS " + TupleIdentityView.query);
                    }
                }).addMigrations(new androidx.room.migration.Migration(229, 230) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `hide_seen` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(230, 231) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET thread = account || ':' || thread");
                    }
                }).addMigrations(new androidx.room.migration.Migration(231, 232) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN 'identity' INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(232, 233) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN 'conditions' TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(233, 234) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        DB.dropTriggers(db);
                        db.execSQL("UPDATE account" +
                                " SET max_messages = MAX(max_messages, MIN(max_messages * 4," +
                                "   (SELECT COUNT(*) FROM folder" +
                                "    JOIN message ON message.folder = folder.id" +
                                "    WHERE folder.account = account.id" +
                                "    AND folder.type = '" + EntityFolder.INBOX + "'" +
                                "    AND NOT message.ui_hide)))" +
                                " WHERE pop = " + EntityAccount.TYPE_POP +
                                " AND NOT max_messages IS NULL" +
                                " AND NOT leave_on_device");
                    }
                }).addMigrations(new androidx.room.migration.Migration(234, 233) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                    }
                }).addMigrations(new androidx.room.migration.Migration(234, 235) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `recent` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(235, 236) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `octetmime` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(236, 237) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''");
                        try (Cursor cursor = db.query("SELECT id FROM rule")) {
                            while (cursor != null && cursor.moveToNext()) {
                                long id = cursor.getLong(0);
                                String uuid = UUID.randomUUID().toString();
                                db.execSQL("UPDATE rule SET uuid = ? WHERE id = ?", new Object[]{uuid, id});
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                }).addMigrations(new androidx.room.migration.Migration(237, 238) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''");
                        try (Cursor cursor = db.query("SELECT id FROM answer")) {
                            while (cursor != null && cursor.moveToNext()) {
                                long id = cursor.getLong(0);
                                String uuid = UUID.randomUUID().toString();
                                db.execSQL("UPDATE answer SET uuid = ? WHERE id = ?", new Object[]{uuid, id});
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                }).addMigrations(new androidx.room.migration.Migration(238, 239) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''");
                        try (Cursor cursor = db.query("SELECT id FROM identity")) {
                            while (cursor != null && cursor.moveToNext()) {
                                long id = cursor.getLong(0);
                                String uuid = UUID.randomUUID().toString();
                                db.execSQL("UPDATE identity SET uuid = ? WHERE id = ?", new Object[]{uuid, id});
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                }).addMigrations(new androidx.room.migration.Migration(239, 240) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `order` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(240, 241) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `inherited_type` TEXT");
                        db.execSQL("DROP VIEW IF EXISTS `folder_view`");
                        db.execSQL("CREATE VIEW IF NOT EXISTS `folder_view` AS " + TupleFolderView.query);
                    }
                }).addMigrations(new androidx.room.migration.Migration(241, 242) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `unicode` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(242, 243) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_noop` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE account SET keep_alive_noop = 1" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                }).addMigrations(new androidx.room.migration.Migration(243, 244) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 0" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                }).addMigrations(new androidx.room.migration.Migration(244, 245) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 1" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                }).addMigrations(new androidx.room.migration.Migration(245, 246) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                    }
                }).addMigrations(new androidx.room.migration.Migration(246, 247) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                    }
                }).addMigrations(new androidx.room.migration.Migration(247, 248) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        EntityMessage.convert(context);
                    }
                }).addMigrations(new androidx.room.migration.Migration(248, 249) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Fts4DbHelper.delete(context);
                        Fts5DbHelper.delete(context);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(249, 250) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        db.execSQL("UPDATE `account` SET partial_fetch = 0 WHERE host = 'imap.mail.yahoo.com'");
                    }
                }).addMigrations(new androidx.room.migration.Migration(250, 251) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        // RENAME COLUMN workaround
                        boolean auto_classify;
                        boolean auto_classify_target;
                        try (Cursor cursor = db.query("SELECT * FROM `folder` LIMIT 0")) {
                            auto_classify = (cursor.getColumnIndex("auto_classify") >= 0);
                            auto_classify_target = (cursor.getColumnIndex("auto_classify_target") >= 0);
                        }
                        if (!auto_classify)
                            db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_classify` INTEGER NOT NULL DEFAULT 0");
                        if (!auto_classify_target)
                            db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_classify_target` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `folder` SET auto_classify_target = auto_classify WHERE auto_classify <> 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(251, 252) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `calendar` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(252, 253) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        for (String key : prefs.getAll().keySet())
                            if (key.startsWith("updated.") || key.startsWith("unset."))
                                editor.remove(key);
                        try (Cursor cursor = db.query("SELECT account.id" +
                                ", archive.type AS archive" +
                                ", drafts.type AS drafts" +
                                ", trash.type AS trash" +
                                ", junk.type AS junk" +
                                ", sent.type AS sent" +
                                " FROM `account`" +
                                " LEFT JOIN folder AS archive ON archive.account = account.id AND archive.type = 'All'" +
                                " LEFT JOIN folder AS drafts ON drafts.account = account.id AND drafts.type = 'Drafts'" +
                                " LEFT JOIN folder AS trash ON trash.account = account.id AND trash.type = 'Trash'" +
                                " LEFT JOIN folder AS junk ON junk.account = account.id AND junk.type = 'Junk'" +
                                " LEFT JOIN folder AS sent ON sent.account = account.id AND sent.type = 'Sent'" +
                                " WHERE account.pop = 0")) {
                            while (cursor.moveToNext()) {
                                long id = cursor.getLong(0);
                                if (cursor.getString(1) == null)
                                    editor.putBoolean("unset." + id + ".All", true);
                                if (cursor.getString(2) == null)
                                    editor.putBoolean("unset." + id + ".Drafts", true);
                                if (cursor.getString(3) == null)
                                    editor.putBoolean("unset." + id + ".Trash", true);
                                if (cursor.getString(4) == null)
                                    editor.putBoolean("unset." + id + ".Junk", true);
                                if (cursor.getString(5) == null)
                                    editor.putBoolean("unset." + id + ".Sent", true);
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                        editor.apply();
                    }
                }).addMigrations(new androidx.room.migration.Migration(253, 254) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 1" +
                                " WHERE (host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com')" +
                                " AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                }).addMigrations(new androidx.room.migration.Migration(254, 255) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_local_only` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(255, 256) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capability_uidl` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(256, 257) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `account_uuid` TEXT");
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `folder_name` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(257, 258) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        DB.defaultSearches(db, context);
                    }
                }).addMigrations(new androidx.room.migration.Migration(258, 259) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 0" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                }).addMigrations(new androidx.room.migration.Migration(259, 260) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `daily` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(260, 261) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        String startup = prefs.getString("startup", "unified");
                        if ("folders".equals(startup))
                            db.execSQL("UPDATE `folder` SET `hide_seen` = 0 WHERE `unified` = 0");
                        else
                            db.execSQL("UPDATE `folder` SET `hide_seen` = 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(262, 261) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                    }
                }).addMigrations(new androidx.room.migration.Migration(261, 262) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `subtype` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(262, 263) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `last_modified` INTEGER");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `last_modified` INTEGER");
                        //createTriggers(db);
                    }
                }).addMigrations(new androidx.room.migration.Migration(263, 264) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `raw_fetch` INTEGER NOT NULL DEFAULT 0");
                        //createTriggers(db);
                    }
                }).addMigrations(new androidx.room.migration.Migration(264, 265) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `reply_extra_name` INTEGER NOT NULL DEFAULT 0");
                        //createTriggers(db);
                    }
                }).addMigrations(new androidx.room.migration.Migration(265, 266) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_message_replying` ON `message` (`replying`)");
                        db.execSQL("CREATE INDEX `index_message_forwarding` ON `message` (`forwarding`)");
                        DB.createTriggers(db);
                    }
                }).addMigrations(new androidx.room.migration.Migration(266, 267) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        // Do nothing
                    }
                }).addMigrations(new androidx.room.migration.Migration(267, 268) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `count_unread` INTEGER NOT NULL DEFAULT 1");
                    }
                }).addMigrations(new androidx.room.migration.Migration(268, 269) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `signedby` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(269, 270) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET partial_fetch = 1" +
                                " WHERE host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com'");
                    }
                }).addMigrations(new androidx.room.migration.Migration(270, 271) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET partial_fetch = 0, raw_fetch = 1" +
                                " WHERE host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com'");
                    }
                }).addMigrations(new androidx.room.migration.Migration(271, 272) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET partial_fetch = 1, raw_fetch = 0" +
                                " WHERE host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com'");
                    }
                }).addMigrations(new androidx.room.migration.Migration(272, 273) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `client_delete` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(273, 274) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `label` TEXT");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_answer_label` ON `answer` (`label`)");
                    }
                }).addMigrations(new androidx.room.migration.Migration(274, 275) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `folder` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(275, 276) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `thread` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(276, 277) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `uri` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(277, 278) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `group` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(278, 279) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `summary` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("DROP VIEW IF EXISTS `account_view`");
                        db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                    }
                }).addMigrations(new androidx.room.migration.Migration(279, 280) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `flags` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(280, 281) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `section` TEXT");
                    }
                }).addMigrations(new androidx.room.migration.Migration(281, 282) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `receipt_type` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(282, 283) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sensitivity` INTEGER NOT NULL DEFAULT 0");
                    }
                }).addMigrations(new androidx.room.migration.Migration(283, 284) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        if (BuildConfig.PLAY_STORE_RELEASE) {
                            db.execSQL("UPDATE `account` SET insecure = 1 WHERE auth_type = " + AUTH_TYPE_PASSWORD);
                            db.execSQL("UPDATE `identity` SET insecure = 1 WHERE auth_type = " + AUTH_TYPE_PASSWORD);
                        }
                    }
                }).addMigrations(new androidx.room.migration.Migration(284, 285) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `write_below` INTEGER");
                    }
                }).addMigrations(new androidx.room.migration.Migration(998, 999) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account" +
                                " SET partial_fetch = 0" +
                                " WHERE host = 'imap.vodafonemail.de'" +
                                " OR host = 'imap.arcor.de'" +
                                " OR host = 'imap.nexgo.de'");
                    }
                });
    }

    private static void logMigration(int startVersion, int endVersion) {
        Map<String, String> crumb = new HashMap<>();
        crumb.put("startVersion", Integer.toString(startVersion));
        crumb.put("endVersion", Integer.toString(endVersion));
        Log.breadcrumb("Migration", crumb);
    }
}
