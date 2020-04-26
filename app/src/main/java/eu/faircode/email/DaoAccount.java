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
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoAccount {
    @Query("SELECT * FROM account" +
            " ORDER BY `order`, `primary` DESC, name COLLATE NOCASE")
    List<EntityAccount> getAccounts();

    @Query("SELECT * FROM account WHERE synchronize" +
            " ORDER BY `order`, `primary` DESC, name COLLATE NOCASE")
    List<EntityAccount> getSynchronizingAccounts();

    @Query("SELECT * FROM account" +
            " WHERE (:id IS NULL OR id = :id)" +
            " AND synchronize" +
            " AND NOT ondemand" +
            " ORDER BY `order`, `primary` DESC, name COLLATE NOCASE")
    List<EntityAccount> getPollAccounts(Long id);

    @Query("SELECT * FROM account WHERE synchronize")
    LiveData<List<EntityAccount>> liveSynchronizingAccounts();

    @Query("SELECT account.*" +
            ", (SELECT COUNT(DISTINCT CASE WHEN message.msgid IS NULL THEN message.id ELSE message.msgid END)" +
            "    FROM message" +
            "    JOIN folder ON folder.id = message.folder" +
            "    WHERE message.account = account.id" +
            "    AND folder.type <> '" + EntityFolder.ARCHIVE + "'" +
            "    AND folder.type <> '" + EntityFolder.TRASH + "'" +
            "    AND folder.type <> '" + EntityFolder.JUNK + "'" +
            "    AND folder.type <> '" + EntityFolder.DRAFTS + "'" +
            "    AND folder.type <> '" + EntityFolder.OUTBOX + "'" +
            "    AND NOT ui_seen" +
            "    AND NOT ui_hide) AS unseen" +
            ", (SELECT COUNT(identity.id)" +
            "    FROM identity" +
            "    WHERE identity.account = account.id" +
            "    AND identity.synchronize) AS identities" +
            ", CASE WHEN drafts.id IS NULL THEN 0 ELSE 1 END AS drafts" +
            " FROM account" +
            " LEFT JOIN folder AS drafts ON drafts.account = account.id AND drafts.type = '" + EntityFolder.DRAFTS + "'" +
            " WHERE :all OR account.synchronize" +
            " GROUP BY account.id" +
            " ORDER BY CASE WHEN :all THEN 0 ELSE account.`order` END" +
            ", CASE WHEN :all THEN 0 ELSE account.`primary` END DESC" +
            ", account.name COLLATE NOCASE")
    LiveData<List<TupleAccountEx>> liveAccountsEx(boolean all);

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

    @Query("SELECT * FROM account WHERE name = :name")
    EntityAccount getAccount(String name);

    @Query("SELECT * FROM account WHERE `primary`")
    EntityAccount getPrimaryAccount();

    @Query("SELECT * FROM account WHERE `primary`")
    LiveData<EntityAccount> livePrimaryAccount();

    @Query("SELECT * FROM account WHERE id = :id")
    LiveData<EntityAccount> liveAccount(long id);

    @Query(TupleAccountView.query)
    LiveData<List<TupleAccountView>> liveAccountView();

    @Query("SELECT account.id" +
            ", account.swipe_left, l.type AS left_type, l.name AS left_name, l.color AS left_color" +
            ", account.swipe_right, r.type AS right_type, r.name AS right_name, r.color AS right_color" +
            " FROM account" +
            " LEFT JOIN folder_view l ON l.id = account.swipe_left" +
            " LEFT JOIN folder_view r ON r.id = account.swipe_right" +
            " WHERE :account IS NULL OR account.id = :account")
    LiveData<List<TupleAccountSwipes>> liveAccountSwipes(Long account);

    @Insert
    long insertAccount(EntityAccount account);

    @Update
    void updateAccount(EntityAccount account);

    @Query("UPDATE account SET separator = :separator WHERE id = :id")
    int setFolderSeparator(long id, Character separator);

    @Query("UPDATE account SET synchronize = :synchronize WHERE id = :id")
    int setAccountSynchronize(long id, boolean synchronize);

    @Query("UPDATE account SET `primary` = :primary WHERE id = :id")
    int setAccountPrimary(long id, boolean primary);

    @Query("UPDATE account SET thread = :thread WHERE id = :id")
    int setAccountThread(long id, Long thread);

    @Query("SELECT thread FROM account WHERE id = :id")
    Long getAccountThread(long id);

    @Query("UPDATE account SET state = :state WHERE id = :id")
    int setAccountState(long id, String state);

    @Query("UPDATE account SET password = :password WHERE id = :id")
    int setAccountPassword(long id, String password);

    @Query("UPDATE account SET last_connected = :last_connected WHERE id = :id")
    int setAccountConnected(long id, long last_connected);

    @Query("UPDATE account SET quota_usage = :used, quota_limit = :limit WHERE id = :id")
    int setAccountQuota(long id, Long used, Long limit);

    @Query("UPDATE account SET poll_interval = :value WHERE id = :id")
    int setAccountKeepAliveInterval(long id, int value);

    @Query("UPDATE account SET keep_alive_ok = :ok WHERE id = :id")
    int setAccountKeepAliveOk(long id, boolean ok);

    @Query("UPDATE account" +
            " SET keep_alive_failed = :failed, keep_alive_succeeded = :succeeded" +
            " WHERE id = :id")
    int setAccountKeepAliveValues(long id, int failed, int succeeded);

    @Query("UPDATE account SET poll_exempted = :value WHERE id = :id")
    int setAccountPollExempted(long id, boolean value);

    @Query("UPDATE account SET `order` = :order WHERE id = :id")
    int setAccountOrder(long id, Integer order);

    @Query("UPDATE account SET partial_fetch = :partial_fetch WHERE id = :id")
    int setAccountPartialFetch(long id, boolean partial_fetch);

    @Query("UPDATE account SET warning = :warning WHERE id = :id")
    int setAccountWarning(long id, String warning);

    @Query("UPDATE account SET error = :error WHERE id = :id")
    int setAccountError(long id, String error);

    @Query("UPDATE account SET swipe_left = :left, swipe_right = :right  WHERE id = :id")
    int setAccountSwipes(long id, Long left, Long right);

    @Query("UPDATE account SET `primary` = 0")
    void resetPrimary();

    @Query("UPDATE account SET tbd = 1 WHERE id = :id")
    int setAccountTbd(long id);

    @Query("DELETE FROM account WHERE id = :id")
    int deleteAccount(long id);
}

