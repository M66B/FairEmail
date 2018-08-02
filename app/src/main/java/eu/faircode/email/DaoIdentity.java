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
public interface DaoIdentity {
    @Query("SELECT * FROM identity")
    LiveData<List<EntityIdentity>> liveIdentities();

    @Query("SELECT * FROM identity WHERE synchronize = :synchronize")
    LiveData<List<EntityIdentity>> liveIdentities(boolean synchronize);

    @Query("SELECT * FROM identity WHERE id = :id")
    EntityIdentity getIdentity(long id);

    @Query("SELECT * FROM identity WHERE id = :id")
    LiveData<EntityIdentity> liveIdentity(long id);

    @Query("SELECT * FROM identity ORDER BY id LIMIT 1")
    LiveData<EntityIdentity> liveFirstIdentity();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIdentity(EntityIdentity identity);

    @Update
    void updateIdentity(EntityIdentity identity);

    @Query("UPDATE identity SET `primary` = 0")
    void resetPrimary();
}
