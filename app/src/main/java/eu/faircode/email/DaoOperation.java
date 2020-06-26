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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface DaoOperation {
    String priority = "CASE" +
            " WHEN operation.name = '" + EntityOperation.BODY + "' THEN -4" +
            " WHEN operation.name = '" + EntityOperation.ATTACHMENT + "' THEN -3" +
            " WHEN operation.name = '" + EntityOperation.HEADERS + "' THEN -2" +
            " WHEN operation.name = '" + EntityOperation.SYNC + "' THEN" +
            "  CASE WHEN folder.account IS NULL THEN -1 ELSE 1 END" + // outbox
            " WHEN operation.name = '" + EntityOperation.FETCH + "' THEN 2" +
            " WHEN operation.name = '" + EntityOperation.EXISTS + "' THEN 3" +
            " ELSE 0" +
            " END";

    @Transaction
    @Query("SELECT operation.*" +
            ", " + priority + " AS priority" +
            ", account.name AS accountName, folder.name AS folderName" +
            " ,((account.synchronize IS NULL OR account.synchronize)" +
            " AND (NOT folder.account IS NULL OR identity.synchronize IS NULL OR identity.synchronize)) AS synchronize" +
            " FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " LEFT JOIN message ON message.id = operation.message" +
            " LEFT JOIN account ON account.id = operation.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " ORDER BY " + priority + ", id")
    LiveData<List<TupleOperationEx>> liveOperations();

    String GET_OPS_FOLDER = "SELECT operation.*" +
            ", " + priority + " AS priority" +
            ", account.name AS accountName, folder.name AS folderName" +
            " ,((account.synchronize IS NULL OR account.synchronize)" +
            " AND (NOT folder.account IS NULL OR identity.synchronize IS NULL OR identity.synchronize)) AS synchronize" +
            " FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " LEFT JOIN message ON message.id = operation.message" +
            " LEFT JOIN account ON account.id = operation.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " WHERE CASE WHEN :folder IS NULL THEN folder.account IS NULL ELSE operation.folder = :folder END" +
            " AND (account.synchronize IS NULL OR account.synchronize)" +
            " AND (NOT folder.account IS NULL OR identity.synchronize IS NULL OR identity.synchronize)" +
            " ORDER BY " + priority + ", id";

    @Query(GET_OPS_FOLDER)
    List<TupleOperationEx> getOperations(Long folder);

    @Transaction
    @Query(GET_OPS_FOLDER)
    LiveData<List<TupleOperationEx>> liveOperations(Long folder);

    @Query("SELECT COUNT(operation.id) AS pending" +
            ", SUM(CASE WHEN operation.error IS NULL THEN 0 ELSE 1 END) AS errors" +
            " FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " LEFT JOIN message ON message.id = operation.message" +
            " LEFT JOIN account ON account.id = operation.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " WHERE (account.synchronize IS NULL OR account.synchronize)" +
            " AND (NOT folder.account IS NULL OR identity.synchronize IS NULL OR identity.synchronize)")
    LiveData<TupleOperationStats> liveStats();

    @Query("SELECT" +
            " COUNT(operation.id) AS count" +
            ", SUM(CASE WHEN operation.state = 'executing' THEN 1 ELSE 0 END) AS busy" +
            " FROM operation" +
            " JOIN message ON message.id = operation.message" +
            " JOIN identity ON identity.id = message.identity" +
            " WHERE operation.name = '" + EntityOperation.SEND + "'" +
            " AND identity.synchronize")
    LiveData<TupleUnsent> liveUnsent();

    @Query("SELECT * FROM operation ORDER BY id")
    List<EntityOperation> getOperations();

    @Query("SELECT * FROM operation WHERE name = :name")
    List<EntityOperation> getOperations(String name);

    @Query("SELECT * FROM operation WHERE account = :account AND name = :name")
    List<EntityOperation> getOperations(long account, String name);

    @Query("SELECT * FROM operation WHERE id = :id")
    EntityOperation getOperation(long id);

    @Query("SELECT * FROM operation WHERE error IS NOT NULL")
    List<EntityOperation> getOperationsError();

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE folder = :folder" +
            " AND (:name IS NULL OR name = :name)")
    int getOperationCount(long folder, String name);

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE folder = :folder" +
            " AND message = :message")
    int getOperationCount(long folder, long message);

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE folder = :folder" +
            " AND message = :message" +
            " AND name = :name")
    int getOperationCount(long folder, long message, String name);

    @Query("UPDATE operation SET tries = :tries WHERE id = :id")
    int setOperationTries(long id, int tries);

    @Query("UPDATE operation SET state = :state WHERE id = :id")
    int setOperationState(long id, String state);

    @Query("UPDATE operation SET state = NULL")
    int resetOperationStates();

    @Query("UPDATE operation SET error = :error WHERE id = :id")
    int setOperationError(long id, String error);

    @Insert
    long insertOperation(EntityOperation operation);

    @Query("DELETE FROM operation WHERE id = :id")
    int deleteOperation(long id);

    @Query("DELETE FROM operation WHERE folder = :folder")
    int deleteOperations(long folder);
}
