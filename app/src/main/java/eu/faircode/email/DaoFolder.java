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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface DaoFolder {
    @Query("SELECT * FROM folder" +
            " WHERE account = :account" +
            " ORDER BY CASE WHEN folder.type = '" + EntityFolder.USER + "' THEN 1 ELSE 0 END")
    List<EntityFolder> getFolders(long account);

    @Query("SELECT * FROM folder" +
            " WHERE account = :account" +
            " AND synchronize = :synchronize" +
            " ORDER BY CASE WHEN folder.type = '" + EntityFolder.USER + "' THEN 1 ELSE 0 END")
    List<EntityFolder> getFolders(long account, boolean synchronize);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize AND folder.synchronize")
    List<EntityFolder> getFoldersSynchronizing();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize AND folder.synchronize AND unified")
    List<EntityFolder> getFoldersSynchronizingUnified();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE folder.id = :folder" +
            " AND (:search OR (account.synchronize AND account.browse))")
    EntityFolder getBrowsableFolder(long folder, boolean search);

    @Query("SELECT folder.*, account.name AS accountName, account.color AS accountColor, account.state AS accountState" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.content = 1 THEN 1 ELSE 0 END) AS content" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE CASE WHEN :account IS NULL" +
            "  THEN folder.unified AND account.synchronize" +
            "  ELSE folder.account = :account OR folder.account IS NULL" +
            " END" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderEx>> liveFolders(Long account);

    @Query("SELECT folder.*, account.name AS accountName, account.color AS accountColor, account.state AS accountState" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.content = 1 THEN 1 ELSE 0 END) AS content" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE account.`synchronize`" +
            " AND folder.unified" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderEx>> liveUnified();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE `primary` AND type = '" + EntityFolder.DRAFTS + "'")
    LiveData<EntityFolder> livePrimaryDrafts();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE `primary` AND type = '" + EntityFolder.ARCHIVE + "'")
    LiveData<EntityFolder> livePrimaryArchive();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE folder.type = '" + EntityFolder.DRAFTS + "'" +
            " AND (account.id = :account OR (:account IS NULL AND account.`primary`))")
    LiveData<EntityFolder> liveDrafts(Long account);

    @Query("SELECT folder.*, account.name AS accountName, account.color AS accountColor, account.state AS accountState" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.content = 1 THEN 1 ELSE 0 END) AS content" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE folder.id = :id")
    LiveData<TupleFolderEx> liveFolderEx(long id);

    @Query("SELECT * FROM folder ORDER BY account, name")
    List<EntityFolder> getFolders();

    @Query("SELECT * FROM folder" +
            " WHERE folder.account = :account" +
            " AND type <> '" + EntityFolder.USER + "'")
    List<EntityFolder> getSystemFolders(long account);

    @Query("SELECT * FROM folder WHERE id = :id")
    EntityFolder getFolder(Long id);

    @Query("SELECT * FROM folder WHERE account = :account AND name = :name")
    EntityFolder getFolderByName(Long account, String name);

    @Query("SELECT folder.* FROM folder" +
            " WHERE account = :account AND type = :type")
    EntityFolder getFolderByType(long account, String type);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE `primary` AND type = '" + EntityFolder.DRAFTS + "'")
    EntityFolder getPrimaryDrafts();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE `primary` AND type = '" + EntityFolder.ARCHIVE + "'")
    EntityFolder getPrimaryArchive();

    @Query("SELECT * FROM folder WHERE type = '" + EntityFolder.OUTBOX + "'")
    EntityFolder getOutbox();

    @Query("SELECT download FROM folder WHERE id = :id")
    boolean getFolderDownload(long id);

    @Insert
    long insertFolder(EntityFolder folder);

    @Query("UPDATE folder SET state = :state WHERE id = :id")
    int setFolderState(long id, String state);

    @Query("UPDATE folder SET sync_state = :state WHERE id = :id")
    int setFolderSyncState(long id, String state);

    @Query("UPDATE folder SET error = :error WHERE id = :id")
    int setFolderError(long id, String error);

    @Query("UPDATE folder SET type = :type WHERE id = :id")
    int setFolderType(long id, String type);

    @Query("UPDATE folder SET display = :display WHERE id = :id")
    int setFolderDisplay(long id, String display);

    @Query("UPDATE folder SET level = :level WHERE id = :id")
    int setFolderLevel(long id, int level);

    @Query("UPDATE folder" +
            " SET type = '" + EntityFolder.USER + "'" +
            " WHERE account = :account" +
            " AND type <> '" + EntityFolder.INBOX + "'" +
            " AND type <> '" + EntityFolder.SYSTEM + "'")
    int setFoldersUser(long account);

    @Query("UPDATE folder" +
            " SET display = :display" +
            ", unified = :unified" +
            ", notify = :notify" +
            ", hide = :hide" +
            ", synchronize = :synchronize" +
            ", poll = :poll" +
            ", download = :download" +
            ", `sync_days` = :sync_days" +
            ", `keep_days` = :keep_days" +
            " WHERE id = :id")
    int setFolderProperties(
            long id,
            String display, boolean unified, boolean notify, boolean hide,
            boolean synchronize, boolean poll, boolean download,
            int sync_days, int keep_days);

    @Query("UPDATE folder SET keywords = :keywords WHERE id = :id")
    int setFolderKeywords(long id, String keywords);

    @Query("UPDATE folder SET name = :name WHERE account = :account AND name = :old")
    int renameFolder(long account, String old, String name);

    @Query("UPDATE folder SET initialize = 0 WHERE id = :id")
    int setFolderInitialized(long id);

    @Query("UPDATE folder SET last_sync = :last_sync WHERE id = :id")
    int setFolderSync(long id, long last_sync);

    @Query("UPDATE folder SET tbc = null WHERE id = :id")
    int resetFolderTbc(long id);

    @Query("UPDATE folder SET tbd = 1 WHERE id = :id")
    int setFolderTbd(long id);

    @Query("DELETE FROM folder WHERE id = :id")
    void deleteFolder(long id);

    @Query("DELETE FROM folder WHERE account = :account AND name = :name")
    void deleteFolder(long account, String name);
}
