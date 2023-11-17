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

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;

public class DBMigration1 {
    static void migrate(final Context context, RoomDatabase.Builder<DB> builder) {
        // https://www.sqlite.org/lang_altertable.html
        builder
                .addMigrations(new androidx.room.migration.Migration(99, 100) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `unsubscribe` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(100, 101) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra_regex` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(101, 102) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `auto_seen` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(102, 103) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `account` SET browse = 1 WHERE pop = 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(103, 104) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET ui_hide = 1 WHERE ui_hide <> 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(104, 105) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `priority` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(105, 106) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `total` INTEGER");
                        db.execSQL("UPDATE `message` SET total = size");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(106, 107) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `receipt` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(107, 108) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(108, 109) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `ignore_size` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(109, 110) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_busy` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(110, 111) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `applied` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(111, 112) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `move_to` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(112, 113) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE message SET encrypt = 1 WHERE id IN " +
                                "(SELECT DISTINCT message FROM attachment" +
                                " WHERE encryption = " + EntityAttachment.PGP_MESSAGE + ")");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(114, 115) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP TABLE revision");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(115, 116) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `use_date` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(116, 117) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_key_alias` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(119, 120) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `after` INTEGER");
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `before` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(120, 121) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `account` SET ondemand = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(121, 122) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `fingerprint` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `fingerprint` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(123, 124) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `provider` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `provider` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(124, 125) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `autocrypt` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(126, 127) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_ok` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_failed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(127, 128) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `quota_usage` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `quota_limit` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(128, 129) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `poll_exempted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(129, 130) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `fts` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(130, 131) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `favorite` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(131, 132) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_encrypt` INTEGER");
                        db.execSQL("UPDATE `message` SET `ui_encrypt` = `encrypt`");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(132, 133) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_on_server` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_on_device` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE `account` SET `leave_on_server` = `browse` WHERE `pop` = " + EntityAccount.TYPE_POP);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(133, 134) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(134, 135) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `identity_view` AS " + TupleIdentityView.query);
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `folder_view` AS " + TupleFolderView.query);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(135, 136) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `certificate` ADD COLUMN `intermediate` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(136, 137) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `submitter` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(137, 138) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `importance` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(138, 139) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(139, 140) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(140, 141) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `certificate` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `certificate` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(141, 142) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `certificate_alias` TEXT");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `certificate_alias` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(142, 143) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `operation` ADD COLUMN `tries` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(143, 144) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `inferiors` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(144, 145) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll_factor` INTEGER NOT NULL DEFAULT 1");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `poll_count` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(145, 146) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `max_messages` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(146, 147) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `thread` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(147, 148) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `cc` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(148, 149) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `leave_deleted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(149, 150) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `language` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(150, 151) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_succeeded` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE account SET keep_alive_failed = 0, keep_alive_ok = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(151, 152) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `hash` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(152, 153) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `unicode` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(153, 154) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `ehlo` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(154, 155) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `folder` SET `poll` = 1 WHERE `synchronize` = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(155, 156) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_inreplyto` ON `message` (`inreplyto`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(156, 157) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `wasforwardedfrom` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(157, 158) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `uidl` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(158, 159) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_unsnoozed` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(159, 160) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(160, 161) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        String encrypt_method = prefs.getString("default_encrypt_method", "pgp");
                        db.execSQL("UPDATE identity SET encrypt = " + ("pgp".equals(encrypt_method) ? 0 : 1));
                        prefs.edit().remove("default_encrypt_method").apply();
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(161, 162) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `verified` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(162, 163) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(163, 164) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(164, 165) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `labels` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(167, 168) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `self` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(168, 169) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `max_size` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(169, 170) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `max_size` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(170, 171) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(171, 172) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `use_received` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(172, 173) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `subsequence` INTEGER");
                        db.execSQL("DROP INDEX `index_attachment_message_sequence`");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attachment_message_sequence_subsequence` ON `attachment` (`message`, `sequence`, `subsequence`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(173, 174) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `group` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(174, 175) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `standard` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(175, 176) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `auto_submitted` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(176, 177) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `backoff_until` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(177, 178) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `local` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(179, 180) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `reply_domain` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(180, 181) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `last_applied` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(181, 182) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_classify` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(182, 183) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `auto_classified` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(183, 184) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `notes` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(185, 186) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `return_path` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(186, 187) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `deleted` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_deleted` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(187, 188) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_silent` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(188, 189) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sign_default` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `encrypt_default` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(189, 190) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `selected_count` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(190, 191) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `selected_last` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(191, 192) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `modseq` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(192, 193) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `notes_color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(193, 194) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sender_extra_name` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(194, 195) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `receipt` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(195, 196) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `internal` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(196, 197) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `applied` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `last_applied` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(197, 198) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `show_images` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `show_full` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(198, 199) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capability_idle` INTEGER");
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capability_utf8` INTEGER");
                    }
                });
    }
}
