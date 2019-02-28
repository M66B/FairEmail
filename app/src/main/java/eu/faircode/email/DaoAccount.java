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
import androidx.room.Update;

@Dao
public interface DaoAccount {
    @Query("SELECT * FROM account")
    List<EntityAccount> getAccounts();

    @Query("SELECT * FROM account" +
            " WHERE synchronize" +
            " AND (:all OR NOT ondemand)")
    List<EntityAccount> getSynchronizingAccounts(boolean all);

    @Query("SELECT * FROM account WHERE tbd = 1")
    List<EntityAccount> getAccountsTbd();

    @Query("SELECT * FROM account")
    LiveData<List<EntityAccount>> liveAccounts();

    @Query("SELECT * FROM account" +
            " WHERE synchronize")
        // including on demand
    LiveData<List<EntityAccount>> liveSynchronizingAccounts();

    @Query("SELECT *" +
            ", (SELECT COUNT(message.id)" +
            "    FROM message" +
            "    JOIN folder ON folder.id = message.folder" +
            "    WHERE message.account = account.id" +
            "    AND folder.type <> '" + EntityFolder.ARCHIVE + "'" +
            "    AND folder.type <> '" + EntityFolder.TRASH + "'" +
            "    AND folder.type <> '" + EntityFolder.DRAFTS + "'" +
            "    AND folder.type <> '" + EntityFolder.OUTBOX + "'" +
            "    AND NOT ui_seen" +
            "    AND NOT ui_hide) AS unseen" +
            " FROM account" +
            " WHERE synchronize")
    LiveData<List<TupleAccountEx>> liveAccountsEx();

    @Query("SELECT * FROM account WHERE id = :id")
    EntityAccount getAccount(long id);

    @Query("SELECT * FROM account WHERE `primary`")
    EntityAccount getPrimaryAccount();

    @Query("SELECT * FROM account WHERE `primary`")
    LiveData<EntityAccount> livePrimaryAccount();

    @Query("SELECT * FROM account WHERE id = :id")
    LiveData<EntityAccount> liveAccount(long id);

    @Query("SELECT" +
            " (SELECT COUNT(account.id) FROM account" +
            "    WHERE synchronize" +
            "    AND NOT ondemand" +
            "    AND state = 'connected') AS accounts" +
            ", (SELECT COUNT(operation.id) FROM operation" +
            "    JOIN folder ON folder.id = operation.folder" +
            "    JOIN account ON account.id = folder.account" + // not outbox
            "    WHERE account.synchronize) AS operations")
    LiveData<TupleAccountStats> liveStats();

    @Query("SELECT account.id, swipe_left, l.type AS left_type, swipe_right, r.type AS right_type" +
            " FROM account" +
            " LEFT JOIN folder l ON l.id = account.swipe_left" +
            " LEFT JOIN folder r ON r.id = account.swipe_right")
    LiveData<List<TupleAccountSwipes>> liveAccountSwipes();

    @Insert
    long insertAccount(EntityAccount account);

    @Update
    void updateAccount(EntityAccount account);

    @Query("UPDATE account SET state = :state WHERE id = :id")
    int setAccountState(long id, String state);

    @Query("UPDATE account SET last_connected = :last_connected WHERE id = :id")
    int setAccountConnected(long id, long last_connected);

    @Query("UPDATE account SET password = :password WHERE id = :id")
    int setAccountPassword(long id, String password);

    @Query("UPDATE account SET error = :error WHERE id = :id")
    int setAccountError(long id, String error);

    @Query("UPDATE account SET `primary` = 0")
    void resetPrimary();

    @Query("UPDATE account SET tbd = 1 WHERE id = :id")
    int setAccountTbd(long id);

    @Query("DELETE FROM account WHERE tbd = 1")
    int deleteAccountsTbd();
}

