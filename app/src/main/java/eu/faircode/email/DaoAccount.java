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
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoAccount {
    @Query("SELECT * FROM account" +
            " ORDER BY `order`, `primary` DESC, name COLLATE NOCASE")
    List<EntityAccount> getAccounts();

    @Query("SELECT * FROM account" +
            " WHERE synchronize" +
            " AND (:type IS NULL OR pop = :type)" +
            " ORDER BY `order`, `primary` DESC, name COLLATE NOCASE")
    List<EntityAccount> getSynchronizingAccounts(Integer type);

    @Query("SELECT * FROM account" +
            " WHERE (:id IS NULL OR id = :id)" +
            " AND synchronize" +
            " AND NOT ondemand" +
            " ORDER BY `order`, `primary` DESC, name COLLATE NOCASE")
    List<EntityAccount> getPollAccounts(Long id);

    @Query("SELECT * FROM account WHERE synchronize")
    LiveData<List<EntityAccount>> liveSynchronizingAccounts();

    @Query("SELECT account.*" +
            ", (SELECT COUNT(identity.id)" +
            "    FROM identity" +
            "    WHERE identity.account = account.id" +
            "    AND identity.synchronize) AS identities" +
            ", drafts.id AS drafts, sent.id AS sent" +
            ", NULL AS folderId, NULL AS folderSeparator" +
            ", NULL AS folderType, -1 AS folderOrder" +
            ", NULL AS folderName, NULL AS folderDisplay, NULL AS folderColor" +
            ", 0 AS folderSync, NULL AS folderState, NULL AS folderSyncState" +
            ", 0 AS executing" +
            ", 0 AS messages" +
            ", (SELECT COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END)" +
            "    FROM message" +
            "    JOIN folder ON folder.id = message.folder" +
            "    WHERE message.account = account.id" +
            "    AND folder.type <> '" + EntityFolder.OUTBOX + "'" +
            "    AND folder.count_unread" +
            "    AND NOT ui_seen" +
            "    AND NOT ui_hide) AS unseen" +
            ", (SELECT COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END)" +
            "    FROM message" +
            "    JOIN folder ON folder.id = message.folder" +
            "    WHERE message.account = account.id" +
            "    AND folder.type <> '" + EntityFolder.OUTBOX + "'" +
            "    AND folder.count_unread" +
            "    AND NOT ui_seen AND message.received > folder.last_view" +
            "    AND NOT ui_hide) AS unexposed" +
            " FROM account" +
            " LEFT JOIN folder AS drafts ON drafts.account = account.id AND drafts.type = '" + EntityFolder.DRAFTS + "'" +
            " LEFT JOIN folder AS sent ON sent.account = account.id AND sent.type = '" + EntityFolder.SENT + "'" +
            " WHERE (:settings OR account.synchronize)" +
            " GROUP BY account.id" +

            " UNION " +

            " SELECT account.*" +
            ", 0 AS identities, 0 AS drafts, 0 AS sent" +
            ", folder.id AS folderId, folder.separator AS folderSeparator" +
            ", folder.type AS folderType, folder.`order` AS folderOrder" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.color AS folderColor" +
            ", folder.synchronize AS folderSync, folder.state AS foldeState, folder.sync_state AS folderSyncState" +
            ", COUNT(operation.id) AS executing" +
            ", (SELECT COUNT(message.id) FROM message" +
            "   WHERE message.folder = folder.id" +
            "   AND NOT ui_hide) AS messages" +
            ", (SELECT COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END)" +
            "    FROM message" +
            "    WHERE message.folder = folder.id" +
            "    AND NOT ui_seen" +
            "    AND NOT ui_hide) AS unseen" +
            ", (SELECT COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END)" +
            "    FROM message" +
            "    WHERE message.folder = folder.id" +
            "    AND NOT message.ui_seen AND message.received > folder.last_view" +
            "    AND NOT ui_hide) AS unexposed" +
            " FROM account" +
            " JOIN folder ON folder.account = account.id" +
            " LEFT JOIN operation ON operation.folder = folder.id AND operation.state = 'executing'" +
            " WHERE (:settings OR account.synchronize)" +
            " AND NOT :settings AND folder.navigation" +
            " GROUP BY folder.id")
    LiveData<List<TupleAccountFolder>> liveAccountFolder(boolean settings);

    @Query("SELECT account.*" +
            ", SUM(folder.synchronize) AS folders" +
            ", (SELECT COUNT(id) FROM operation" +
            "  WHERE operation.account = account.id AND operation.name <> '" + EntityOperation.SEND + "') AS operations" +
            " FROM account" +
            " LEFT JOIN folder ON folder.account = account.id" +
            " GROUP BY account.id" +
            " ORDER BY account.id")
    LiveData<List<TupleAccountState>> liveAccountState();

    @Query("SELECT * FROM account WHERE id = :id")
    EntityAccount getAccount(long id);

    @Query("SELECT * FROM account WHERE uuid = :uuid")
    EntityAccount getAccountByUUID(String uuid);

    @Query("SELECT * FROM account WHERE auth_type = :auth_type AND user = :user")
    EntityAccount getAccount(int auth_type, String user);

    @Query("SELECT * FROM account WHERE name = :name")
    EntityAccount getAccount(String name);

    @Query("SELECT * FROM account" +
            " WHERE user = :user COLLATE NOCASE" +
            " AND pop = :protocol" +
            " AND tbd IS NULL")
    List<EntityAccount> getAccounts(String user, int protocol);

    @Query("SELECT DISTINCT category" +
            " FROM account" +
            " WHERE NOT (category IS NULL OR category = '')" +
            " ORDER BY category COLLATE NOCASE")
    List<String> getAccountCategories();

    @Query("SELECT * FROM account WHERE `primary`")
    EntityAccount getPrimaryAccount();

    @Query("SELECT * FROM account WHERE `primary`")
    LiveData<EntityAccount> livePrimaryAccount();

    @Query("SELECT * FROM account WHERE id = :id")
    LiveData<EntityAccount> liveAccount(long id);

    @Query(TupleAccountView.query)
    LiveData<List<TupleAccountView>> liveAccountView();

    String swipes = "SELECT account.id" +
            ", account.swipe_left, l.type AS left_type, l.name AS left_name, l.color AS left_color" +
            ", account.swipe_right, r.type AS right_type, r.name AS right_name, r.color AS right_color" +
            " FROM account" +
            " LEFT JOIN folder_view l ON l.id = account.swipe_left" +
            " LEFT JOIN folder_view r ON r.id = account.swipe_right" +
            " WHERE :account IS NULL OR account.id = :account";

    @Query(swipes)
    LiveData<List<TupleAccountSwipes>> liveAccountSwipes(Long account);

    @Query(swipes)
    List<TupleAccountSwipes> getAccountSwipes(Long account);

    @Insert
    long insertAccount(EntityAccount account);

    @Update
    void updateAccount(EntityAccount account);

    @Query("UPDATE account SET uuid = :uuid WHERE id = :id AND NOT (uuid IS :uuid)")
    int setAccountUuid(long id, String uuid);

    @Query("UPDATE account SET synchronize = :synchronize WHERE id = :id AND NOT (synchronize IS :synchronize)")
    int setAccountSynchronize(long id, boolean synchronize);

    @Query("UPDATE account SET ondemand = :ondemand WHERE id = :id AND NOT (ondemand IS :ondemand)")
    int setAccountOnDemand(long id, boolean ondemand);

    @Query("UPDATE account SET `primary` = :primary WHERE id = :id AND NOT (`primary` IS :primary)")
    int setAccountPrimary(long id, boolean primary);

    @Query("UPDATE account SET notify = :notify WHERE id = :id AND NOT (notify IS :notify)")
    int setAccountNotify(long id, boolean notify);

    @Query("UPDATE account SET thread = :thread WHERE id = :id AND NOT (thread IS :thread)")
    int setAccountThread(long id, Long thread);

    @Query("SELECT thread FROM account WHERE id = :id")
    Long getAccountThread(long id);

    @Query("UPDATE account SET state = :state WHERE id = :id AND NOT (state IS :state)")
    int setAccountState(long id, String state);

    @Query("UPDATE account SET name = :name WHERE id = :id AND NOT (name IS :name)")
    int setAccountName(long id, String name);

    @Query("UPDATE account SET color = :color WHERE id = :id AND NOT (color IS :color)")
    int setAccountColor(long id, Integer color);

    @Query("UPDATE account" +
            " SET password = :password, auth_type = :auth_type, provider = :provider" +
            " WHERE id = :id" +
            " AND NOT (password IS :password AND auth_type = :auth_type AND provider = :provider)")
    int setAccountPassword(long id, String password, int auth_type, String provider);

    @Query("UPDATE account" +
            " SET fingerprint = :fingerprint, insecure = :insecure" +
            " WHERE id = :id" +
            " AND NOT (fingerprint IS :fingerprint)")
    int setAccountFingerprint(long id, String fingerprint, boolean insecure);

    @Query("UPDATE account SET last_connected = :last_connected WHERE id = :id AND NOT (last_connected IS :last_connected)")
    int setAccountConnected(long id, Long last_connected);

    @Query("UPDATE account SET backoff_until = :backoff_until WHERE id = :id AND NOT (backoff_until IS :backoff_until)")
    int setAccountBackoff(long id, Long backoff_until);

    @Query("UPDATE account" +
            " SET quota_usage = :used, quota_limit = :limit" +
            " WHERE id = :id" +
            " AND (NOT (quota_usage IS :used) OR NOT (quota_limit IS :limit))")
    int setAccountQuota(long id, Long used, Long limit);

    @Query("UPDATE account SET poll_interval = :value WHERE id = :id AND NOT (poll_interval IS :value)")
    int setAccountKeepAliveInterval(long id, int value);

    @Query("UPDATE account SET keep_alive_ok = :ok WHERE id = :id AND NOT (keep_alive_ok IS :ok)")
    int setAccountKeepAliveOk(long id, boolean ok);

    @Query("UPDATE account" +
            " SET keep_alive_failed = :failed, keep_alive_succeeded = :succeeded" +
            " WHERE id = :id" +
            " AND (NOT (keep_alive_failed IS :failed) OR NOT (keep_alive_succeeded IS :succeeded))")
    int setAccountKeepAliveValues(long id, int failed, int succeeded);

    @Query("UPDATE account SET poll_exempted = :value WHERE id = :id AND NOT (poll_exempted IS :value)")
    int setAccountPollExempted(long id, boolean value);

    @Query("UPDATE account SET `order` = :order WHERE id = :id AND NOT (`order` IS :order)")
    int setAccountOrder(long id, Integer order);

    @Query("UPDATE account SET partial_fetch = :partial_fetch WHERE id = :id AND NOT (partial_fetch IS :partial_fetch)")
    int setAccountPartialFetch(long id, boolean partial_fetch);

    @Query("UPDATE account SET max_size = :max_size WHERE id = :id AND NOT (max_size IS :max_size)")
    int setAccountMaxSize(long id, Long max_size);

    @Query("UPDATE account" +
            " SET capabilities = :capabilities" +
            ", capability_idle = :idle" +
            ", capability_utf8 = :utf8" +
            " WHERE id = :id" +
            " AND NOT (capabilities IS :capabilities" +
            "  AND capability_idle IS :idle" +
            "  AND capability_utf8 IS :utf8)")
    int setAccountCapabilities(long id, String capabilities, Boolean idle, Boolean utf8);

    @Query("UPDATE account SET warning = :warning WHERE id = :id AND NOT (warning IS :warning)")
    int setAccountWarning(long id, String warning);

    @Query("UPDATE account SET error = :error WHERE id = :id AND NOT (error IS :error)")
    int setAccountError(long id, String error);

    @Query("UPDATE account" +
            " SET swipe_left = :left, swipe_right = :right" +
            " WHERE id = :id" +
            " AND (NOT (swipe_left IS :left) OR NOT (swipe_right IS :right))")
    int setAccountSwipes(long id, Long left, Long right);

    @Query("UPDATE account SET `primary` = 0 WHERE NOT (`primary` IS 0)")
    void resetPrimary();

    @Query("UPDATE account SET `created` = 0 WHERE id = :id")
    void resetCreated(long id);

    @Query("UPDATE account SET tbd = 1 WHERE id = :id AND NOT (tbd IS 1)")
    int setAccountTbd(long id);

    @Query("UPDATE account SET capability_uidl = :uidl WHERE id = :id AND NOT (capability_uidl IS :uidl)")
    int setAccountUidl(long id, Boolean uidl);

    @Query("UPDATE account" +
            " SET last_modified = :last_modified" +
            " WHERE id = :id")
    int setAccountLastModified(long id, Long last_modified);

    @Query("DELETE FROM account WHERE id = :id")
    int deleteAccount(long id);
}

