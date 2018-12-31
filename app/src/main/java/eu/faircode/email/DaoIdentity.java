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
public interface DaoIdentity {
    @Query("SELECT identity.*, account.name AS accountName FROM identity" +
            " JOIN account ON account.id = identity.account")
    LiveData<List<TupleIdentityEx>> liveIdentities();

    @Query("SELECT identity.* FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " WHERE (:account IS NULL OR account.id = :account)" +
            " AND account.synchronize = :synchronize" +
            " AND identity.synchronize = :synchronize")
    LiveData<List<EntityIdentity>> liveIdentities(Long account, boolean synchronize);

    @Query("SELECT * FROM identity")
    List<EntityIdentity> getIdentities();

    @Query("SELECT * FROM identity WHERE account = :account")
    List<EntityIdentity> getIdentities(long account);

    @Query("SELECT * FROM identity WHERE id = :id")
    EntityIdentity getIdentity(long id);

    @Query("SELECT * FROM identity WHERE account = :account AND email = :email")
    EntityIdentity getIdentity(long account, String email);

    @Query("SELECT COUNT(*) FROM identity WHERE synchronize")
    int getSynchronizingIdentityCount();

    @Insert
    long insertIdentity(EntityIdentity identity);

    @Update
    void updateIdentity(EntityIdentity identity);

    @Query("UPDATE identity SET state = :state WHERE id = :id")
    int setIdentityState(long id, String state);

    @Query("UPDATE identity SET password = :password WHERE id = :id")
    int setIdentityPassword(long id, String password);

    @Query("UPDATE identity SET error = :error WHERE id = :id")
    int setIdentityError(long id, String error);

    @Query("UPDATE identity SET `primary` = 0 WHERE account = :account")
    void resetPrimary(long account);

    @Query("UPDATE identity SET tbd = 1 WHERE id = :id")
    int setIdentityTbd(long id);

    @Query("DELETE FROM identity WHERE tbd = 1")
    int deleteIdentitiesTbd();
}
