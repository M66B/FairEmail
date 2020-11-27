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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface DaoFolder {
    @Query("SELECT * FROM folder" +
            " WHERE account = :account" +
            " AND (NOT :writable OR NOT read_only)" +
            " AND (NOT :selectable OR selectable)")
    List<EntityFolder> getFolders(long account, boolean writable, boolean selectable);

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.pop AS accountProtocol, account.`order` AS accountOrder" +
            ", account.name AS accountName, account.state AS accountState" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT message.id) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN message.ui_flagged THEN message.id ELSE NULL END) AS flagged" +
            ", COUNT(DISTINCT CASE WHEN operation.state = 'executing' THEN operation.id ELSE NULL END) AS executing" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " LEFT JOIN rule ON rule.folder = folder.id" +
            " LEFT JOIN operation ON operation.folder = folder.id" +
            " WHERE folder.account = :account AND account.synchronize" +
            " GROUP BY folder.id")
    List<TupleFolderEx> getFoldersEx(long account);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize" +
            " AND (NOT :synchronizing OR folder.synchronize)" +
            " AND ((:type IS NULL AND folder.unified) OR folder.type = :type)")
    List<EntityFolder> getFoldersUnified(String type, boolean synchronizing);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE folder.id = :folder" +
            " AND (:search OR (account.synchronize AND account.browse AND account.pop = " + EntityAccount.TYPE_IMAP + "))")
    EntityFolder getBrowsableFolder(long folder, boolean search);

    @Query("SELECT folder.*" +
            ", account.`order` AS accountOrder, account.name AS accountName" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.`synchronize`")
    List<TupleFolderSort> getSortedFolders();

    @Transaction
    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.pop AS accountProtocol, account.`order` AS accountOrder" +
            ", account.name AS accountName, account.state AS accountState" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT CASE WHEN message.ui_hide THEN NULL ELSE message.id END) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 AND NOT message.ui_hide THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen AND NOT message.ui_hide THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN message.ui_flagged AND NOT message.ui_hide THEN message.id ELSE NULL END) AS flagged" +
            ", COUNT(DISTINCT CASE WHEN operation.state = 'executing' THEN operation.id ELSE NULL END) AS executing" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id" +
            " LEFT JOIN rule ON rule.folder = folder.id" +
            " LEFT JOIN operation ON operation.folder = folder.id" +
            " WHERE CASE WHEN :primary THEN account.`primary` ELSE" +
            "  CASE WHEN :account IS NULL" +
            "   THEN folder.unified AND account.synchronize" +
            "   ELSE folder.account = :account AND account.synchronize" +
            "  END" +
            " END" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderEx>> liveFolders(Long account, boolean primary);

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.pop AS accountProtocol, account.`order` AS accountOrder" +
            ", account.name AS accountName, account.state AS accountState" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT message.id) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN message.ui_flagged THEN message.id ELSE NULL END) AS flagged" +
            ", COUNT(DISTINCT CASE WHEN operation.state = 'executing' THEN operation.id ELSE NULL END) AS executing" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " LEFT JOIN rule ON rule.folder = folder.id" +
            " LEFT JOIN operation ON operation.folder = folder.id" +
            " WHERE account.`synchronize`" +
            " AND ((:type IS NULL AND folder.unified) OR folder.type = :type)" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderEx>> liveUnified(String type);

    @Query("SELECT folder.*" +
            ", account.`order` AS accountOrder, account.name AS accountName, COALESCE(folder.color, account.color) AS accountColor" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN NOT message.ui_seen THEN 1 ELSE 0 END) AS unseen" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id) AS operations" +
            ", (SELECT COUNT(operation.id) FROM operation WHERE operation.folder = folder.id AND operation.state = 'executing') AS executing" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE account.id IS NULL" +
            " OR (account.`synchronize` AND folder.navigation)" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderNav>> liveNavigation();

    @Query("SELECT COUNT(id) FROM folder" +
            " WHERE sync_state = 'syncing'" +
            " AND folder.type <> '" + EntityFolder.OUTBOX + "'")
    LiveData<Integer> liveSynchronizing();

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.pop AS accountProtocol, account.`order` AS accountOrder" +
            ", account.name AS accountName, account.state AS accountState" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT message.id) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN message.ui_flagged THEN message.id ELSE NULL END) AS flagged" +
            ", COUNT(DISTINCT CASE WHEN operation.state = 'executing' THEN operation.id ELSE NULL END) AS executing" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " LEFT JOIN rule ON rule.folder = folder.id" +
            " LEFT JOIN operation ON operation.folder = folder.id" +
            " WHERE folder.id = :id" +
            " GROUP BY folder.id")
    LiveData<TupleFolderEx> liveFolderEx(long id);

    @Query(TupleFolderView.query)
    LiveData<List<TupleFolderView>> liveFolderView();

    @Query("SELECT * FROM folder ORDER BY account, name COLLATE NOCASE")
    List<EntityFolder> getFolders();

    @Query("SELECT * FROM folder" +
            " WHERE folder.account = :account" +
            " AND type <> '" + EntityFolder.USER + "'")
    List<EntityFolder> getSystemFolders(long account);

    @Query("SELECT * FROM folder" +
            " WHERE folder.account = :account" +
            " AND folder.synchronize")
    List<EntityFolder> getSynchronizingFolders(long account);

    @Query("SELECT * FROM folder" +
            " WHERE folder.account = :account" +
            " AND folder.`synchronize`" +
            " AND folder.notify")
    List<EntityFolder> getNotifyingFolders(long account);

    @Query("SELECT * FROM folder" +
            " WHERE parent = :parent" +
            " ORDER BY name COLLATE NOCASE")
    List<EntityFolder> getChildFolders(long parent);

    @Query("SELECT folder.type" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN NOT message.ui_seen THEN 1 ELSE 0 END) AS unseen" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE account.synchronize" +
            " AND folder.type <> '" + EntityFolder.SYSTEM + "'" +
            " AND folder.type <> '" + EntityFolder.USER + "'" +
            " GROUP BY folder.type")
    LiveData<List<TupleFolderUnified>> liveUnified();

    @Query("SELECT * FROM folder WHERE id = :id")
    EntityFolder getFolder(Long id);

    @Query("SELECT * FROM folder WHERE account = :account AND name = :name")
    EntityFolder getFolderByName(Long account, String name);

    @Query("SELECT folder.* FROM folder" +
            " WHERE account = :account AND type = :type")
    EntityFolder getFolderByType(long account, String type);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize" +
            " AND account.`primary`" +
            " AND type = '" + EntityFolder.DRAFTS + "'")
    EntityFolder getPrimaryDrafts();

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

    @Query("UPDATE folder SET state = :state WHERE account = :account")
    int setFolderStates(long account, String state);

    @Query("UPDATE folder SET sync_state = :state WHERE id = :id")
    int setFolderSyncState(long id, String state);

    @Query("UPDATE folder SET total = :total WHERE id = :id")
    int setFolderTotal(long id, Integer total);

    @Query("UPDATE folder SET error = :error WHERE id = :id")
    int setFolderError(long id, String error);

    @Query("UPDATE folder SET subscribed = :subscribed WHERE id = :id")
    int setFolderSubscribed(long id, Boolean subscribed);

    @Query("UPDATE folder SET selectable = :selectable WHERE id = :id")
    int setFolderSelectable(long id, Boolean selectable);

    @Query("UPDATE folder SET inferiors = :inferiors WHERE id = :id")
    int setFolderInferiors(long id, Boolean inferiors);

    @Query("UPDATE folder SET type = :type WHERE id = :id")
    int setFolderType(long id, String type);

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
            " SET `rename` = :rename" +
            ", display = :display" +
            ", color = :color" +
            ", unified = :unified" +
            ", navigation = :navigation" +
            ", notify = :notify" +
            ", hide = :hide" +
            ", synchronize = :synchronize" +
            ", poll = :poll" +
            ", poll_factor = :poll_factor" +
            ", download = :download" +
            ", `sync_days` = :sync_days" +
            ", `keep_days` = :keep_days" +
            ", auto_delete = :auto_delete" +
            " WHERE id = :id")
    int setFolderProperties(
            long id, String rename,
            String display, Integer color, boolean unified, boolean navigation, boolean notify, boolean hide,
            boolean synchronize, boolean poll, int poll_factor, boolean download,
            int sync_days, int keep_days, boolean auto_delete);

    @Query("UPDATE folder" +
            " SET sync_days = :sync_days, keep_days = :keep_days" +
            " WHERE account = :account" +
            " AND type = '" + EntityFolder.USER + "'")
    int setFolderProperties(long account, int sync_days, int keep_days);

    @Query("UPDATE folder SET keywords = :keywords WHERE id = :id")
    int setFolderKeywords(long id, String keywords);

    @Query("UPDATE folder SET name = :name WHERE account = :account AND name = :old")
    int renameFolder(long account, String old, String name);

    @Query("UPDATE folder SET initialize = :days WHERE id = :id")
    int setFolderInitialize(long id, int days);

    @Query("UPDATE folder SET keep_days = :days WHERE id = :id")
    int setFolderKeep(long id, int days);

    @Query("UPDATE folder SET uidv = :uidv WHERE id = :id")
    int setFolderUidValidity(long id, Long uidv);

    @Query("UPDATE folder SET last_sync = :last_sync WHERE id = :id")
    int setFolderLastSync(long id, long last_sync);

    @Query("UPDATE folder SET read_only = :read_only WHERE id = :id")
    int setFolderReadOnly(long id, boolean read_only);

    @Query("UPDATE folder SET tbc = NULL WHERE id = :id")
    int resetFolderTbc(long id);

    @Query("UPDATE folder SET `rename` = NULL WHERE id = :id")
    int resetFolderRename(long id);

    @Query("UPDATE folder SET tbd = 1 WHERE id = :id")
    int setFolderTbd(long id);

    @Query("UPDATE folder SET poll = :poll, poll_count = 1 WHERE id = :id")
    int setFolderPoll(long id, boolean poll);

    @Query("UPDATE folder SET poll_count = :count WHERE id = :id")
    int setFolderPollCount(long id, int count);

    @Query("DELETE FROM folder WHERE id = :id")
    void deleteFolder(long id);

    @Query("DELETE FROM folder WHERE account = :account AND name = :name")
    void deleteFolder(long account, String name);
}
