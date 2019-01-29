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

@Dao
public interface DaoOperation {
    @Query("SELECT operation.*, account.name AS accountName, folder.name AS folderName" +
            " ,((account.synchronize IS NULL OR account.synchronize)" +
            " AND (NOT folder.account IS NULL OR identity.synchronize)) AS synchronize" +
            " FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " LEFT JOIN message ON message.id = operation.message" +
            " LEFT JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " ORDER BY" +
            "  CASE WHEN operation.name = '" + EntityOperation.SYNC + "' THEN" +
            "    CASE WHEN folder.account IS NULL THEN -1 ELSE 1 END" + // outbox
            "    ELSE 0" +
            "  END" +
            ", id")
    LiveData<List<TupleOperationEx>> liveOperations();

    String GET_OPS_FOLDER = "SELECT operation.* FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " LEFT JOIN message ON message.id = operation.message" +
            " LEFT JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " WHERE operation.folder = :folder" +
            " AND (account.synchronize IS NULL OR account.synchronize)" +
            " AND (NOT folder.account IS NULL OR identity.synchronize)" +
            " ORDER BY" +
            "  CASE WHEN operation.name = '" + EntityOperation.SYNC + "' THEN" +
            "    CASE WHEN folder.account IS NULL THEN -1 ELSE 1 END" + // outbox
            "    ELSE 0" +
            "  END" +
            ", id";

    @Query(GET_OPS_FOLDER)
    List<EntityOperation> getOperations(long folder);

    @Query(GET_OPS_FOLDER)
    LiveData<List<EntityOperation>> liveOperations(long folder);

    @Query("SELECT * FROM operation ORDER BY id")
    List<EntityOperation> getOperations();

    @Query("SELECT * FROM operation WHERE error IS NOT NULL")
    List<EntityOperation> getOperationsError();

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE folder = :folder" +
            " AND (:name IS NULL OR operation.name = :name)")
    int getOperationCount(long folder, String name);

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE folder = :folder" +
            " AND  message = :message")
    int getOperationCount(long folder, long message);

    @Query("UPDATE operation SET error = :error WHERE id = :id")
    int setOperationError(long id, String error);

    @Insert
    long insertOperation(EntityOperation operation);

    @Query("DELETE FROM operation WHERE id = :id")
    void deleteOperation(long id);
}
