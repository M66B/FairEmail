package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
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

    @Query("SELECT * FROM account WHERE synchronize = :synchronize")
    List<EntityAccount> getAccounts(boolean synchronize);

    @Query("SELECT * FROM account")
    LiveData<List<EntityAccount>> liveAccounts();

    @Query("SELECT * FROM account WHERE synchronize = :synchronize")
    LiveData<List<EntityAccount>> liveAccounts(boolean synchronize);

    @Query("SELECT * FROM account WHERE id = :id")
    EntityAccount getAccount(long id);

    @Query("SELECT * FROM account WHERE `primary`")
    EntityAccount getPrimaryAccount();

    @Query("SELECT COUNT(*) FROM account WHERE synchronize")
    int getSynchronizingAccountCount();

    @Query("SELECT * FROM account WHERE `primary`")
    LiveData<EntityAccount> livePrimaryAccount();

    @Query("SELECT * FROM account WHERE id = :id")
    LiveData<EntityAccount> liveAccount(long id);

    @Query("SELECT" +
            " (SELECT COUNT(account.id) FROM account WHERE synchronize) AS accounts" +
            ", (SELECT COUNT(operation.id) FROM operation" +
            "     JOIN message ON message.id = operation.message" +
            "     JOIN account ON account.id = message.account" +
            "     WHERE synchronize) AS operations" +
            ", (SELECT COUNT(message.id) FROM message" +
            "     JOIN folder ON folder.id = message.folder" +
            "     JOIN operation ON operation.message = message.id AND operation.name = '" + EntityOperation.SEND + "'" +
            "     WHERE NOT message.ui_seen" +
            "     AND folder.type = '" + EntityFolder.OUTBOX + "') AS unsent")
    LiveData<TupleAccountStats> liveStats();

    @Insert
    long insertAccount(EntityAccount account);

    @Update
    void updateAccount(EntityAccount account);

    @Query("UPDATE account SET state = :state WHERE id = :id")
    int setAccountState(long id, String state);

    @Query("UPDATE account SET password = :password WHERE id = :id")
    int setAccountPassword(long id, String password);

    @Query("UPDATE account SET error = :error WHERE id = :id")
    int setAccountError(long id, String error);

    @Query("UPDATE account SET `primary` = 0")
    void resetPrimary();

    @Query("DELETE FROM account WHERE id = :id")
    void deleteAccount(long id);
}

