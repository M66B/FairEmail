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

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.UUID;

public class DBMigration2 {
    static void migrate(final Context context, RoomDatabase.Builder<DB> builder) {
        // https://www.sqlite.org/lang_altertable.html
        builder
                .addMigrations(new androidx.room.migration.Migration(199, 200) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `blocklist` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(200, 201) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `bimi_selector` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(201, 202) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE identity SET use_ip = 0 WHERE host = 'smtp.office365.com'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(202, 203) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync_count` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(203, 204) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                })
                .addMigrations(new androidx.room.migration.Migration(204, 205) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `external` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(205, 206) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capabilities` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(206, 207) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        //db.execSQL("DROP VIEW IF EXISTS `account_view`");
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(207, 208) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `type` INTEGER NOT NULL DEFAULT " + EntityLog.Type.General.ordinal());
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(208, 209) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `account` INTEGER");
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `folder` INTEGER");
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `message` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(209, 210) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `namespace` TEXT");
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `separator` INTEGER");
                        db.execSQL("UPDATE folder SET separator =" +
                                " (SELECT separator FROM account WHERE account.id = folder.account)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(210, 211) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE TABLE `search`" +
                                " (`id` INTEGER PRIMARY KEY AUTOINCREMENT" +
                                ", name TEXT NOT NULL" +
                                ", `data` TEXT NOT NULL)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(211, 212) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(212, 213) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `category` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(213, 214) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_account_synchronize` ON `account` (`synchronize`)");
                        db.execSQL("CREATE INDEX `index_account_category` ON `account` (`category`)");
                        //db.execSQL("DROP VIEW IF EXISTS `account_view`");
                        //db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(214, 215) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `auto_add` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(215, 216) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `infrastructure` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(216, 217) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `last_sync_foreground` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(217, 218) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `smtp_from` TEXT");
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `from_domain` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(218, 219) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `resend` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(219, 220) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `tls` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(220, 221) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `sensitivity` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(221, 222) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `color` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(222, 223) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `related` INTEGER");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(223, 224) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `media_uri` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(224, 225) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE folder" +
                                " SET auto_delete = 0" +
                                " WHERE type ='" + EntityFolder.JUNK + "'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(225, 226) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `snippet` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(226, 227) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
                        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(227, 228) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `group` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(228, 229) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("DROP VIEW IF EXISTS `identity_view`");
                        db.execSQL("CREATE VIEW IF NOT EXISTS `identity_view` AS " + TupleIdentityView.query);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(229, 230) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `hide_seen` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(230, 231) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE `message` SET thread = account || ':' || thread");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(231, 232) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN 'identity' INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(232, 233) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN 'conditions' TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(233, 234) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                })
                .addMigrations(new androidx.room.migration.Migration(234, 233) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(234, 235) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `recent` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(235, 236) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `octetmime` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(236, 237) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                })
                .addMigrations(new androidx.room.migration.Migration(237, 238) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                })
                .addMigrations(new androidx.room.migration.Migration(238, 239) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
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
                })
                .addMigrations(new androidx.room.migration.Migration(239, 240) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `order` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(240, 241) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `inherited_type` TEXT");
                        db.execSQL("DROP VIEW IF EXISTS `folder_view`");
                        db.execSQL("CREATE VIEW IF NOT EXISTS `folder_view` AS " + TupleFolderView.query);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(241, 242) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `unicode` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(242, 243) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `keep_alive_noop` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("UPDATE account SET keep_alive_noop = 1" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(243, 244) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 0" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(244, 245) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 1" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(245, 246) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(246, 247) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(247, 248) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        EntityMessage.convert(context);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(248, 249) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        Fts4DbHelper.delete(context);
                        Fts5DbHelper.delete(context);
                        db.execSQL("UPDATE `message` SET fts = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(249, 250) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        db.execSQL("UPDATE `account` SET partial_fetch = 0 WHERE host = 'imap.mail.yahoo.com'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(250, 251) {
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
                })
                .addMigrations(new androidx.room.migration.Migration(251, 252) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `calendar` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(252, 253) {
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
                })
                .addMigrations(new androidx.room.migration.Migration(253, 254) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 1" +
                                " WHERE (host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com')" +
                                " AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(254, 255) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `ui_local_only` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(255, 256) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `capability_uidl` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(256, 257) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `account_uuid` TEXT");
                        db.execSQL("ALTER TABLE `search` ADD COLUMN `folder_name` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(257, 258) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        DB.defaultSearches(db, context);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(258, 259) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET keep_alive_noop = 0" +
                                " WHERE host = 'outlook.office365.com' AND pop = " + EntityAccount.TYPE_IMAP);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(259, 260) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `daily` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(260, 261) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        String startup = prefs.getString("startup", "unified");
                        if ("folders".equals(startup))
                            db.execSQL("UPDATE `folder` SET `hide_seen` = 0 WHERE `unified` = 0");
                        else
                            db.execSQL("UPDATE `folder` SET `hide_seen` = 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(262, 261) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(261, 262) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `subtype` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(262, 263) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `last_modified` INTEGER");
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `last_modified` INTEGER");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(263, 264) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `raw_fetch` INTEGER NOT NULL DEFAULT 0");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(264, 265) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `reply_extra_name` INTEGER NOT NULL DEFAULT 0");
                        //createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(265, 266) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("CREATE INDEX `index_message_replying` ON `message` (`replying`)");
                        db.execSQL("CREATE INDEX `index_message_forwarding` ON `message` (`forwarding`)");
                        DB.createTriggers(db);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(266, 267) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        // Do nothing
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(267, 268) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `count_unread` INTEGER NOT NULL DEFAULT 1");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(268, 269) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `signedby` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(269, 270) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET partial_fetch = 1" +
                                " WHERE host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(270, 271) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET partial_fetch = 0, raw_fetch = 1" +
                                " WHERE host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(271, 272) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account SET partial_fetch = 1, raw_fetch = 0" +
                                " WHERE host = 'imap.mail.yahoo.com' OR host = 'imap.aol.com'");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(272, 273) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `client_delete` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(273, 274) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `answer` ADD COLUMN `label` TEXT");
                        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_answer_label` ON `answer` (`label`)");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(274, 275) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `contact` ADD COLUMN `folder` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(275, 276) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `log` ADD COLUMN `thread` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(276, 277) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `uri` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(277, 278) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `rule` ADD COLUMN `group` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(278, 279) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `account` ADD COLUMN `summary` INTEGER NOT NULL DEFAULT 0");
                        db.execSQL("DROP VIEW IF EXISTS `account_view`");
                        db.execSQL("CREATE VIEW IF NOT EXISTS `account_view` AS " + TupleAccountView.query);
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(279, 280) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `folder` ADD COLUMN `flags` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(280, 281) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `attachment` ADD COLUMN `section` TEXT");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(281, 282) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `receipt_type` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(282, 283) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `identity` ADD COLUMN `sensitivity` INTEGER NOT NULL DEFAULT 0");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(283, 284) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        if (BuildConfig.PLAY_STORE_RELEASE) {
                            db.execSQL("UPDATE `account` SET insecure = 1 WHERE auth_type = " + AUTH_TYPE_PASSWORD);
                            db.execSQL("UPDATE `identity` SET insecure = 1 WHERE auth_type = " + AUTH_TYPE_PASSWORD);
                        }
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(284, 285) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("ALTER TABLE `message` ADD COLUMN `write_below` INTEGER");
                    }
                })
                .addMigrations(new androidx.room.migration.Migration(998, 999) {
                    @Override
                    public void migrate(@NonNull SupportSQLiteDatabase db) {
                        DB.logMigration(startVersion, endVersion);
                        db.execSQL("UPDATE account" +
                                " SET partial_fetch = 0" +
                                " WHERE host = 'imap.vodafonemail.de'" +
                                " OR host = 'imap.arcor.de'" +
                                " OR host = 'imap.nexgo.de'");
                    }
                });
    }
}
