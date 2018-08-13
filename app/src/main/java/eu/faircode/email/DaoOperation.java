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
import androidx.room.Query;

@Dao
public interface DaoOperation {
    @Query("SELECT * FROM operation WHERE message = :message ORDER BY id")
    LiveData<List<EntityOperation>> getOperationsByMessage(long message);

    @Query("SELECT * FROM operation WHERE folder = :folder ORDER BY id")
    List<EntityOperation> getOperationsByFolder(long folder);

    @Query("SELECT * FROM operation ORDER BY id")
    LiveData<List<EntityOperation>> liveOperations();

    @Query("SELECT COUNT(id) FROM operation WHERE folder = :folder")
    int getOperationCount(long folder);

    @Insert
    long insertOperation(EntityOperation operation);

    @Query("DELETE FROM operation WHERE id = :id")
    void deleteOperation(long id);
}
