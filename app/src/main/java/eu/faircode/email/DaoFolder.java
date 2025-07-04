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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

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
            ", account.name AS accountName, account.category AS accountCategory, account.color AS accountColor" +
            ", account.state AS accountState, account.error AS accountError" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT message.id) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen AND message.received > folder.last_view THEN message.id ELSE NULL END) AS unexposed" +
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
            ", account.name AS accountName, account.category AS accountCategory, account.color AS accountColor" +
            ", account.state AS accountState, account.error AS accountError" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT CASE WHEN message.ui_hide THEN NULL ELSE message.id END) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 AND NOT message.ui_hide THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen AND NOT message.ui_hide THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen AND NOT message.ui_hide AND message.received > folder.last_view THEN message.id ELSE NULL END) AS unexposed" +
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

    final String queryUnified = "SELECT folder.*" +
            ", account.id AS accountId, account.pop AS accountProtocol, account.`order` AS accountOrder" +
            ", account.name AS accountName, account.category AS accountCategory, account.color AS accountColor" +
            ", account.state AS accountState, account.error AS accountError" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT message.id) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen AND message.received > folder.last_view THEN message.id ELSE NULL END) AS unexposed" +
            ", COUNT(DISTINCT CASE WHEN message.ui_flagged THEN message.id ELSE NULL END) AS flagged" +
            ", COUNT(DISTINCT CASE WHEN operation.state = 'executing' THEN operation.id ELSE NULL END) AS executing" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " LEFT JOIN rule ON rule.folder = folder.id" +
            " LEFT JOIN operation ON operation.folder = folder.id" +
            " WHERE account.`synchronize`" +
            " AND ((:type IS NULL AND folder.unified) OR folder.type = :type)" +
            " AND (:category IS NULL OR account.category = :category)" +
            " GROUP BY folder.id";

    @Query(queryUnified)
    LiveData<List<TupleFolderEx>> liveUnified(String type, String category);

    @Query(queryUnified)
    List<TupleFolderEx> getUnified(String type, String category);

    @Query("SELECT folder.account, folder.id AS folder, unified, sync_state" +
            " FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.`synchronize`" +
            " AND sync_state IS NOT NULL" +
            " AND folder.type <> '" + EntityFolder.OUTBOX + "'")
    LiveData<List<TupleFolderSync>> liveSynchronizing();

    @Query("SELECT folder.*" +
            ", account.id AS accountId, account.pop AS accountProtocol, account.`order` AS accountOrder" +
            ", account.name AS accountName, account.category AS accountCategory, account.color AS accountColor" +
            ", account.state AS accountState, account.error AS accountError" +
            ", COUNT(DISTINCT CASE WHEN rule.enabled THEN rule.id ELSE NULL END) rules" +
            ", COUNT(DISTINCT message.id) AS messages" +
            ", COUNT(DISTINCT CASE WHEN message.content = 1 THEN message.id ELSE NULL END) AS content" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen THEN message.id ELSE NULL END) AS unseen" +
            ", COUNT(DISTINCT CASE WHEN NOT message.ui_seen AND message.received > folder.last_view THEN message.id ELSE NULL END) AS unexposed" +
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
            " AND type <> '" + EntityFolder.USER + "'")
    LiveData<List<EntityFolder>> liveSystemFolders(long account);

    @Query("SELECT * FROM folder" +
            " WHERE folder.account = :account" +
            " AND folder.selectable" +
            " AND folder.synchronize")
    List<EntityFolder> getSynchronizingFolders(long account);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account_view AS account ON account.id = folder.account" +
            " WHERE folder.account = :account" +
            " AND folder.notify" +
            " AND account.`synchronize`")
    List<EntityFolder> getNotifyingFolders(long account);

    @Query("SELECT * FROM folder" +
            " WHERE parent = :parent" +
            " ORDER BY name COLLATE NOCASE")
    List<EntityFolder> getChildFolders(long parent);

    @Query("SELECT folder.type, account.category, folder.unified" +
            ", COUNT(DISTINCT folder.id) AS folders" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN NOT message.ui_seen THEN 1 ELSE 0 END) AS unseen" +
            ", SUM(CASE WHEN NOT message.ui_seen AND message.received > folder.last_view THEN 1 ELSE 0 END) AS unexposed" +
            ", CASE WHEN folder.account IS NULL THEN folder.sync_state ELSE NULL END AS sync_state" +
            ", folder.color, COUNT (DISTINCT folder.color) AS colorCount" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE (account.id IS NULL OR account.synchronize)" +
            " AND ((folder.type <> '" + EntityFolder.SYSTEM + "'" +
            " AND folder.type <> '" + EntityFolder.USER + "')" +
            " OR folder.unified)" +
            " GROUP BY folder.type, account.category, folder.unified")
    LiveData<List<TupleFolderUnified>> liveUnified();

    @Query("SELECT * FROM folder" +
            " WHERE account = :account" +
            " AND selected_count > 0" +
            " AND NOT folder.id IN (:disabled)" +
            " ORDER BY selected_count DESC, selected_last DESC" +
            " LIMIT :count")
    List<EntityFolder> getFavoriteFolders(long account, int count, long[] disabled);

    @Query("UPDATE folder" +
            " SET selected_last = :last, selected_count = selected_count + 1" +
            " WHERE id = :id")
    int increaseSelectedCount(long id, long last);

    @Query("UPDATE folder" +
            " SET selected_last = 0, selected_count = 0" +
            " WHERE account = :account")
    int resetSelectedCount(long account);

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
            " AND type = :type")
    List<EntityFolder> getFoldersByType(String type);

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize" +
            " AND account.`primary`" +
            " AND type = :type")
    EntityFolder getFolderPrimary(String type);

    @Query("SELECT * FROM folder WHERE type = '" + EntityFolder.OUTBOX + "'")
    EntityFolder getOutbox();

    @Query("SELECT download FROM folder WHERE id = :id")
    boolean getFolderDownload(long id);

    @Query("SELECT COUNT(*) FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize")
    int countTotal();

    @Query("SELECT COUNT(*) FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.synchronize AND folder.synchronize")
    int countSync();

    @Insert
    long insertFolder(EntityFolder folder);

    @Query("UPDATE folder" +
            " SET namespace = :namespace, separator = :separator" +
            " WHERE id = :id AND NOT (namespace IS :namespace AND separator IS :separator)")
    int setFolderNamespace(long id, String namespace, Character separator);

    @Query("UPDATE folder SET unified = :unified WHERE id = :id AND NOT (unified IS :unified)")
    int setFolderUnified(long id, boolean unified);

    @Query("UPDATE folder SET navigation = :navigation WHERE id = :id AND NOT (navigation IS :navigation)")
    int setFolderNavigation(long id, boolean navigation);

    @Query("UPDATE folder SET notify = :notify WHERE id = :id AND NOT (notify IS :notify)")
    int setFolderNotify(long id, boolean notify);

    @Query("UPDATE folder SET synchronize = :synchronize WHERE id = :id AND NOT (synchronize IS :synchronize)")
    int setFolderSynchronize(long id, boolean synchronize);

    @Query("UPDATE folder SET state = :state WHERE id = :id AND NOT (state IS :state)")
    int setFolderState(long id, String state);

    @Query("UPDATE folder SET state = :state WHERE account = :account AND NOT (state IS :state)")
    int setFolderStates(long account, String state);

    @Query("UPDATE folder SET sync_state = :state WHERE id = :id AND NOT (sync_state IS :state)")
    int setFolderSyncState(long id, String state);

    @Query("UPDATE folder SET total = :total WHERE id = :id AND NOT (total IS :total)")
    int setFolderTotal(long id, Integer total);

    @Query("UPDATE folder SET total = :total, last_sync = :last_sync" +
            " WHERE id = :id" +
            " AND NOT (total IS :total AND last_sync IS :last_sync)")
    int setFolderTotal(long id, Integer total, Long last_sync);

    @Query("UPDATE folder SET error = :error WHERE id = :id AND NOT (error IS :error)")
    int setFolderError(long id, String error);

    @Query("UPDATE folder SET subscribed = :subscribed WHERE id = :id AND NOT (subscribed IS :subscribed)")
    int setFolderSubscribed(long id, Boolean subscribed);

    @Query("UPDATE folder SET selectable = :selectable WHERE id = :id AND NOT (selectable IS :selectable)")
    int setFolderSelectable(long id, Boolean selectable);

    @Query("UPDATE folder SET inferiors = :inferiors WHERE id = :id AND NOT (inferiors IS :inferiors)")
    int setFolderInferiors(long id, Boolean inferiors);

    @Query("UPDATE folder SET name = :name WHERE id = :id AND NOT (name IS :name)")
    int setFolderName(long id, String name);

    @Query("UPDATE folder SET type = :type WHERE id = :id AND NOT (type IS :type)")
    int setFolderType(long id, String type);

    @Query("UPDATE folder SET color = :color WHERE id = :id AND NOT (color IS :color)")
    int setFolderColor(long id, Integer color);

    @Query("UPDATE folder SET inherited_type = :type WHERE id = :id AND NOT (inherited_type IS :type)")
    int setFolderInheritedType(long id, String type);

    @Query("UPDATE folder SET subtype = :subtype WHERE id = :id AND NOT (subtype IS :subtype)")
    int setFolderSubtype(long id, String subtype);

    @Query("UPDATE folder SET `order` = :order WHERE id = :id AND NOT (`order` IS :order)")
    int setFolderOrder(long id, Integer order);

    @Query("UPDATE folder SET parent = :parent WHERE id = :id AND NOT (parent IS :parent)")
    int setFolderParent(long id, Long parent);

    @Query("UPDATE folder SET collapsed = :collapsed WHERE id = :id AND NOT (collapsed IS :collapsed)")
    int setFolderCollapsed(long id, boolean collapsed);

    @Query("UPDATE folder" +
            " SET type = '" + EntityFolder.USER + "'" +
            " WHERE account = :account" +
            " AND type <> '" + EntityFolder.INBOX + "'" +
            " AND type <> '" + EntityFolder.SYSTEM + "'" +
            " AND type <> '" + EntityFolder.USER + "'")
    int setFoldersUser(long account);

    @Query("UPDATE folder" +
            " SET `rename` = :rename" +
            ", display = :display" +
            ", color = :color" +
            ", unified = :unified" +
            ", navigation = :navigation" +
            ", count_unread = :count_unread" +
            ", notify = :notify" +
            ", hide = :hide" +
            ", hide_seen = :hide_seen" +
            ", synchronize = :synchronize" +
            ", poll = :poll" +
            ", poll_factor = :poll_factor" +
            ", download = :download" +
            ", auto_classify_source = :auto_classify_source" +
            ", auto_classify_target = :auto_classify_target" +
            ", `sync_days` = :sync_days" +
            ", `keep_days` = :keep_days" +
            ", auto_delete = :auto_delete" +
            " WHERE id = :id")
    int setFolderProperties(
            long id, String rename,
            String display, Integer color, boolean unified,
            boolean navigation, boolean count_unread, boolean notify,
            boolean hide, boolean hide_seen,
            boolean synchronize, boolean poll, int poll_factor, boolean download,
            boolean auto_classify_source, boolean auto_classify_target,
            int sync_days, int keep_days, boolean auto_delete);

    @Query("UPDATE folder" +
            " SET sync_days = :sync_days, keep_days = :keep_days" +
            " WHERE id = :id" +
            " AND NOT (sync_days IS :sync_days AND keep_days IS :keep_days)")
    int setFolderProperties(long id, int sync_days, int keep_days);

    @Query("UPDATE folder SET flags = :flags WHERE id = :id AND NOT (flags IS :flags)")
    int setFolderFlags(long id, String flags);

    @Query("UPDATE folder SET keywords = :keywords WHERE id = :id AND NOT (keywords IS :keywords)")
    int setFolderKeywords(long id, String keywords);

    @Query("UPDATE folder SET name = :name WHERE account = :account AND name = :old AND NOT (:old IS :name)")
    int renameFolder(long account, String old, String name);

    @Query("UPDATE folder SET initialize = :days WHERE id = :id AND NOT (initialize IS :days)")
    int setFolderInitialize(long id, int days);

    @Query("UPDATE folder SET keep_days = :days WHERE id = :id AND NOT (keep_days IS :days)")
    int setFolderKeep(long id, int days);

    @Query("UPDATE folder SET uidv = :uidv WHERE id = :id AND NOT (uidv IS :uidv)")
    int setFolderUidValidity(long id, Long uidv);

    @Query("UPDATE folder SET modseq = :modseq WHERE id = :id AND NOT (modseq IS :modseq)")
    int setFolderModSeq(long id, Long modseq);

    @Query("UPDATE folder SET last_sync = :last_sync WHERE id = :id AND NOT (last_sync IS :last_sync)")
    int setFolderLastSync(long id, long last_sync);

    @Query("UPDATE folder SET last_sync_foreground = :last_sync_foreground WHERE id = :id AND NOT (last_sync_foreground IS :last_sync_foreground)")
    int setFolderLastSyncForeground(long id, long last_sync_foreground);

    @Query("UPDATE folder SET last_sync_count = :last_sync_count WHERE id = :id AND NOT (last_sync_count IS :last_sync_count)")
    int setFolderLastSyncCount(long id, Integer last_sync_count);

    @Query("UPDATE folder SET last_view = :last_view" +
            " WHERE (id = :folder" +
            "   OR (type = :type AND type <> '" + EntityFolder.OUTBOX + "')" +
            "   OR (account = :account AND :folder IS NULL AND :type IS NULL AND unified))")
    int setFolderLastView(Long account, Long folder, String type, long last_view);

    @Query("UPDATE folder SET read_only = :read_only WHERE id = :id AND NOT (read_only IS :read_only)")
    int setFolderReadOnly(long id, boolean read_only);

    @Query("UPDATE folder SET auto_add = :auto_add WHERE id = :id AND NOT (auto_add IS :auto_add)")
    int setFolderAutoAdd(long id, Boolean auto_add);

    @Query("UPDATE folder SET tbc = NULL WHERE id = :id AND tbc IS NOT NULL")
    int resetFolderTbc(long id);

    @Query("UPDATE folder SET tbd = NULL WHERE id = :id AND tbd IS NOT NULL")
    int resetFolderTbd(long id);

    @Query("UPDATE folder SET `rename` = NULL WHERE id = :id AND `rename` IS NOT NULL")
    int resetFolderRename(long id);

    @Query("UPDATE folder SET tbd = 1 WHERE id = :id AND NOT (tbd IS 1)")
    int setFolderTbd(long id);

    @Query("UPDATE folder" +
            " SET poll = :poll, poll_count = 1" +
            " WHERE id = :id" +
            " AND (NOT (poll IS :poll) OR NOT (poll_count IS 1))")
    int setFolderPoll(long id, boolean poll);

    @Query("UPDATE folder SET poll_count = :count WHERE id = :id AND NOT (poll_count IS :count)")
    int setFolderPollCount(long id, int count);

    @Query("UPDATE folder SET download = :download WHERE id = :id AND NOT (download IS :download)")
    int setFolderDownload(long id, boolean download);

    @Query("UPDATE folder SET hide = :hide WHERE id = :id AND NOT (hide IS :hide)")
    int setFolderHide(long id, boolean hide);

    @Query("UPDATE folder" +
            " SET auto_classify_source = :source, auto_classify_target = :target" +
            " WHERE id = :id" +
            " AND NOT (auto_classify_source IS :source AND auto_classify_target IS :target)")
    int setFolderAutoClassify(long id, boolean source, boolean target);

    @Update
    int updateFolder(EntityFolder folder);

    @Query("DELETE FROM folder WHERE id = :id")
    void deleteFolder(long id);

    @Query("DELETE FROM folder WHERE account = :account AND name = :name")
    void deleteFolder(long account, String name);
}
