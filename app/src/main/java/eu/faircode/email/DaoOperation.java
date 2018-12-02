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

    @Query("SELECT * FROM operation WHERE folder = :folder ORDER BY id")
    LiveData<List<EntityOperation>> liveOperations(long folder);

    @Query("SELECT * FROM operation ORDER BY id")
    List<EntityOperation> getOperations();

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE folder = :folder" +
            " AND (:name IS NULL OR operation.name = :name)")
    int getOperationCount(long folder, String name);

    @Query("UPDATE operation SET error = :error WHERE id = :id")
    int setOperationError(long id, String error);

    @Insert
    long insertOperation(EntityOperation operation);

    @Query("DELETE FROM operation WHERE id = :id")
    void deleteOperation(long id);
}
