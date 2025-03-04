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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
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
            " WHEN operation.name = '" + EntityOperation.BODY + "' THEN -6" +
            " WHEN operation.name = '" + EntityOperation.ATTACHMENT + "' THEN -5" +
            " WHEN operation.name = '" + EntityOperation.HEADERS + "' THEN -4" +
            " WHEN operation.name = '" + EntityOperation.RAW + "' THEN -4" +
            " WHEN operation.name = '" + EntityOperation.SYNC + "' AND folder.account IS NULL THEN -3" + // Outbox
            " WHEN operation.name = '" + EntityOperation.ADD + "' THEN -2" +
            " WHEN operation.name = '" + EntityOperation.DELETE + "' THEN -2" +
            " WHEN operation.name = '" + EntityOperation.COPY + "' THEN -1" +
            // Other operations: seen, answered, flag, keyword, label, subscribe, send, rule
            " WHEN operation.name = '" + EntityOperation.SYNC + "' AND folder.account IS NOT NULL THEN 1" +
            " WHEN operation.name = '" + EntityOperation.FETCH + "' THEN 2" +
            " WHEN operation.name = '" + EntityOperation.DOWNLOAD + "' THEN 3" +
            " WHEN operation.name = '" + EntityOperation.EXISTS + "' THEN 3" +
            " WHEN operation.name = '" + EntityOperation.REPORT + "' THEN 3" +
            " WHEN operation.name = '" + EntityOperation.SUBJECT + "' THEN 3" +
            " WHEN operation.name = '" + EntityOperation.MOVE + "' THEN 5" +
            " WHEN operation.name = '" + EntityOperation.PURGE + "' THEN 6" +
            " WHEN operation.name = '" + EntityOperation.DELETE + "' THEN 7" +
            " WHEN operation.name = '" + EntityOperation.EXPUNGE + "' THEN 8" +
            " ELSE 0" +
            " END";

    @Transaction
    @Query("SELECT operation.*" +
            ", " + priority + " AS priority" +
            ", account.name AS accountName" +
            ", folder.name AS folderName, folder.type AS folderType" +
            ", (account.synchronize IS NULL OR account.synchronize) AS synchronize" +
            " FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " LEFT JOIN account ON account.id = operation.account" +
            " ORDER BY " + priority + ", id")
    LiveData<List<TupleOperationEx>> liveOperations();

    @Transaction
    @Query("SELECT operation.*" +
            ", " + priority + " AS priority" +
            ", account.name AS accountName" +
            ", folder.name AS folderName, folder.type AS folderType" +
            ", account.synchronize" +
            " FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " JOIN account ON account.id = operation.account" +
            " WHERE operation.account = :account" +
            " AND account.synchronize" +
            " AND folder.account IS NOT NULL" + // not outbox
            " ORDER BY " + priority + ", id")
    LiveData<List<TupleOperationEx>> liveOperations(long account);

    @Query("SELECT operation.id" +
            ", message.uid, message.content" +
            " FROM message" +
            " LEFT JOIN operation ON operation.message = message.id AND operation.name = :name" +
            " WHERE message.id = :message")
    LiveData<TupleMessageOperation> liveOperations(long message, String name);

    @Transaction
    @Query("SELECT operation.*" +
            " FROM operation" +
            " JOIN folder ON folder.id = operation.folder" +
            " WHERE folder.account IS NULL" + // outbox
            " ORDER BY id")
    LiveData<List<EntityOperation>> liveSend();

    @Query("SELECT COUNT(operation.id) AS pending" +
            ", SUM(CASE WHEN operation.error IS NULL THEN 0 ELSE 1 END) AS errors" +
            " FROM operation" +
            " LEFT JOIN account ON account.id = operation.account" +
            " WHERE (account.synchronize IS NULL OR account.synchronize)")
    LiveData<TupleOperationStats> liveStats();

    @Query("SELECT" +
            " COUNT(operation.id) AS count" +
            ", SUM(CASE WHEN operation.state = 'executing' THEN 1 ELSE 0 END) AS busy" +
            " FROM operation" +
            " WHERE operation.name = '" + EntityOperation.SEND + "'")
    LiveData<TupleUnsent> liveUnsent();

    @Query("SELECT * FROM operation ORDER BY id")
    List<EntityOperation> getOperations();

    @Query("SELECT * FROM operation WHERE name = :name")
    List<EntityOperation> getOperations(String name);

    @Query("SELECT * FROM operation WHERE account = :account AND name = :name")
    List<EntityOperation> getOperations(long account, String name);

    @Query("SELECT * FROM operation WHERE folder = :folder AND name = :name")
    List<EntityOperation> getOperationsByFolder(long folder, String name);

    @Query("SELECT * FROM operation WHERE id = :id")
    EntityOperation getOperation(long id);

    @Query("SELECT * FROM operation WHERE message = :message AND name = :name")
    EntityOperation getOperation(long message, String name);

    @Query("SELECT * FROM operation WHERE error IS NOT NULL")
    List<EntityOperation> getOperationsError();

    @Query("SELECT COUNT(id) FROM operation")
    int getOperationCount();

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE name = :name")
    int getOperationCount(String name);

    @Query("SELECT COUNT(id) FROM operation" +
            " WHERE account = :account")
    int getOperationCount(long account);

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

    @Query("UPDATE operation SET tries = :tries WHERE id = :id AND NOT (tries IS :tries)")
    int setOperationTries(long id, int tries);

    @Query("UPDATE operation SET state = :state WHERE id = :id AND NOT (state IS :state)")
    int setOperationState(long id, String state);

    @Query("UPDATE operation SET state = NULL" +
            " WHERE account = :account" +
            " AND state IS NOT NULL" +
            " AND name <> '" + EntityOperation.SEND + "'")
    int resetOperationStates(long account);

    @Query("UPDATE operation SET error = :error WHERE id = :id AND NOT (error IS :error)")
    int setOperationError(long id, String error);

    @Insert
    long insertOperation(EntityOperation operation);

    @Query("DELETE FROM operation WHERE id = :id")
    int deleteOperation(long id);

    @Query("DELETE FROM operation WHERE folder = :folder AND name = :name")
    int deleteOperations(long folder, String name);
}
