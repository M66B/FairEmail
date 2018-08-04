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

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DaoOperation {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOperation(EntityOperation operation);

    @Query("SELECT operation.*, message.uid FROM operation" +
            " JOIN message ON message.id = operation.message" +
            " WHERE folder = :folder" +
            " ORDER BY operation.id")
    List<TupleOperationEx> getOperations(long folder);

    @Query("SELECT COUNT(operation.id) FROM operation" +
            " JOIN message ON message.id = operation.message" +
            " WHERE folder = :folder" +
            " ORDER BY operation.id")
    int getOperationCount(long folder);

    @Query("DELETE FROM operation WHERE id = :id")
    void deleteOperation(long id);

    @Query("DELETE FROM operation WHERE message = :id AND name = :name")
    int deleteOperations(long id, String name);
}
