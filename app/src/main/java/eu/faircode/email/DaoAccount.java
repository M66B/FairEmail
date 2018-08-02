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

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

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

    @Query("SELECT * FROM account ORDER BY id LIMIT 1")
    LiveData<EntityAccount> liveFirstAccount();

    @Query("SELECT" +
            " (SELECT COUNT(*) FROM account WHERE synchronize) AS accounts," +
            " (SELECT COUNT(*) FROM operation JOIN message ON message.id = operation.message JOIN account ON account.id = message.account WHERE synchronize) AS operations")
    LiveData<TupleAccountStats> liveStats();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAccount(EntityAccount account);

    @Update
    void updateAccount(EntityAccount account);

    @Query("UPDATE account SET `primary` = 0")
    void resetPrimary();
}

