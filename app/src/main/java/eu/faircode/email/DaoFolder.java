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

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoFolder {
    @Query("SELECT * FROM folder WHERE account = :account")
    List<EntityFolder> getFolders(long account);

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.`order` AS accountOrder, account.name AS accountName, account.color AS accountColor, account.state AS accountState" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.content = 1 THEN 1 ELSE 0 END) AS content" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id AND operation.state = 'executing') AS executing" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE folder.account = :account AND account.synchronize" +
            " GROUP BY folder.id")
    List<TupleFolderEx> getFoldersEx(long account);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize" +
            " AND folder.synchronize AND unified")
    List<EntityFolder> getFoldersSynchronizingUnified();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE folder.id = :folder" +
            " AND (:search OR (account.synchronize AND account.browse))")
    EntityFolder getBrowsableFolder(long folder, boolean search);

    @Query("SELECT folder.*" +
            ", account.`order` AS accountOrder, account.name AS accountName" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.`synchronize`")
    List<TupleFolderSort> getSortedFolders();

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.`order` AS accountOrder, account.name AS accountName, account.color AS accountColor, account.state AS accountState" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.content = 1 THEN 1 ELSE 0 END) AS content" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id AND operation.state = 'executing') AS executing" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE CASE WHEN :account IS NULL" +
            "  THEN folder.unified AND account.synchronize" +
            "  ELSE folder.account = :account AND account.synchronize" +
            " END" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderEx>> liveFolders(Long account);

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.`order` AS accountOrder, account.name AS accountName, account.color AS accountColor, account.state AS accountState" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.content = 1 THEN 1 ELSE 0 END) AS content" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id AND operation.state = 'executing') AS executing" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE account.`synchronize`" +
            " AND folder.unified" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderEx>> liveUnified();

    @Query("SELECT folder.*" +
            ", account.`order` AS accountOrder, account.name AS accountName, account.color AS accountColor" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id) AS operations" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id AND operation.state = 'executing') AS executing" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE account.id IS NULL" +
            " OR (account.`synchronize` AND folder.navigation)" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderNav>> liveNavigation();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize" +
            " AND account.`primary`" +
            " AND folder.type = '" + EntityFolder.DRAFTS + "'")
    LiveData<EntityFolder> livePrimaryDrafts();

    @Query("SELECT COUNT(id) FROM folder" +
            " WHERE sync_state = 'syncing'" +
            " AND folder.type <> '" + EntityFolder.OUTBOX + "'")
    LiveData<Integer> liveSynchronizing();

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.`order` AS accountOrder, account.name AS accountName, account.color AS accountColor, account.state AS accountState" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.content = 1 THEN 1 ELSE 0 END) AS content" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id AND operation.state = 'executing') AS executing" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE folder.id = :id")
    LiveData<TupleFolderEx> liveFolderEx(long id);

    @Query("SELECT * FROM folder ORDER BY account, name COLLATE NOCASE")
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

    @Query("UPDATE folder SET unified = :unified WHERE id = :id")
    int setFolderUnified(long id, boolean unified);

    @Query("UPDATE folder SET navigation = :navigation WHERE id = :id")
    int setFolderNavigation(long id, boolean navigation);

    @Query("UPDATE folder SET notify = :notify WHERE id = :id")
    int setFolderNotify(long id, boolean notify);

    @Query("UPDATE folder SET synchronize = :synchronize WHERE id = :id")
    int setFolderSynchronize(long id, boolean synchronize);

    @Query("UPDATE folder SET state = :state WHERE id = :id")
    int setFolderState(long id, String state);

    @Query("UPDATE folder SET sync_state = :state WHERE id = :id")
    int setFolderSyncState(long id, String state);

    @Query("UPDATE folder SET total = :total WHERE id = :id")
    int setFolderTotal(long id, Integer total);

    @Query("UPDATE folder SET error = :error WHERE id = :id")
    int setFolderError(long id, String error);

    @Query("UPDATE folder SET subscribed = :subscribed WHERE id = :id")
    int setFolderSubscribed(long id, Boolean subscribed);

    @Query("UPDATE folder SET type = :type WHERE id = :id")
    int setFolderType(long id, String type);

    @Query("UPDATE folder SET display = :display WHERE id = :id")
    int setFolderDisplay(long id, String display);

    @Query("UPDATE folder SET `order` = :order WHERE id = :id")
    int setFolderOrder(long id, Integer order);

    @Query("UPDATE folder SET parent = :parent WHERE id = :id")
    int setFolderParent(long id, Long parent);

    @Query("UPDATE folder SET collapsed = :collapsed WHERE id = :id")
    int setFolderCollapsed(long id, boolean collapsed);

    @Query("UPDATE folder" +
            " SET type = '" + EntityFolder.USER + "'" +
            " WHERE account = :account" +
            " AND type <> '" + EntityFolder.INBOX + "'" +
            " AND type <> '" + EntityFolder.SYSTEM + "'")
    int setFoldersUser(long account);

    @Query("UPDATE folder" +
            " SET display = :display" +
            ", unified = :unified" +
            ", navigation = :navigation" +
            ", notify = :notify" +
            ", synchronize = :synchronize" +
            ", poll = :poll" +
            ", download = :download" +
            ", `sync_days` = :sync_days" +
            ", `keep_days` = :keep_days" +
            ", auto_delete = :auto_delete" +
            " WHERE id = :id")
    int setFolderProperties(
            long id,
            String display, boolean unified, boolean navigation, boolean notify,
            boolean synchronize, boolean poll, boolean download,
            int sync_days, int keep_days, boolean auto_delete);

    @Query("UPDATE folder SET keywords = :keywords WHERE id = :id")
    int setFolderKeywords(long id, String keywords);

    @Query("UPDATE folder SET name = :name WHERE account = :account AND name = :old")
    int renameFolder(long account, String old, String name);

    @Query("UPDATE folder SET initialize = 0 WHERE id = :id")
    int setFolderInitialized(long id);

    @Query("UPDATE folder SET last_sync = :last_sync WHERE id = :id")
    int setFolderSync(long id, long last_sync);

    @Query("UPDATE folder SET read_only = :read_only WHERE id = :id")
    int setFolderReadOnly(long id, boolean read_only);

    @Query("UPDATE folder SET tbc = null WHERE id = :id")
    int resetFolderTbc(long id);

    @Query("UPDATE folder SET tbd = 1 WHERE id = :id")
    int setFolderTbd(long id);

    @Query("DELETE FROM folder WHERE id = :id")
    void deleteFolder(long id);

    @Query("DELETE FROM folder WHERE account = :account AND name = :name")
    void deleteFolder(long account, String name);
}
