package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DaoAccount {
    @Query("SELECT * FROM account WHERE synchronize = :synchronize")
    List<EntityAccount> getAccounts(boolean synchronize);

    @Query("SELECT * FROM account")
    LiveData<List<EntityAccount>> liveAccounts();

    @Query("SELECT * FROM account WHERE synchronize = :synchronize")
    LiveData<List<EntityAccount>> liveAccounts(boolean synchronize);

    @Query("SELECT * FROM account WHERE id = :id")
    EntityAccount getAccount(long id);

    @Query("SELECT * FROM account WHERE id = :id")
    LiveData<EntityAccount> liveAccount(long id);

    @Query("SELECT" +
            " (SELECT COUNT(*) FROM account WHERE synchronize) AS accounts" +
            ", (SELECT COUNT(*) FROM operation" +
            "     JOIN message ON message.id = operation.message" +
            "     JOIN account ON account.id = message.account" +
            "     WHERE synchronize) AS operations" +
            ", (SELECT COUNT(*) FROM message" +
            "     JOIN account ON account.id = message.account" +
            "     JOIN folder ON folder.id = message.folder" +
            "     WHERE NOT message.ui_seen AND NOT message.ui_hide" +
            "     AND (account.seen_until IS NULL OR message.received > account.seen_until)" +
            "     AND folder.type = '" + EntityFolder.TYPE_INBOX + "') AS unseen")
    LiveData<TupleAccountStats> liveStats();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAccount(EntityAccount account);

    @Update
    void updateAccount(EntityAccount account);

    @Query("UPDATE account SET `primary` = 0")
    void resetPrimary();

    @Query("DELETE FROM account WHERE id = :id")
    void deleteAccount(long id);
}

