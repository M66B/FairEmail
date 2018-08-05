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
public interface DaoFolder {
    @Query("SELECT * FROM folder WHERE account = :account")
    List<EntityFolder> getFolders(long account);

    @Query("SELECT * FROM folder WHERE account = :account AND synchronize = :synchronize")
    List<EntityFolder> getFolders(long account, boolean synchronize);

    @Query("SELECT * FROM folder WHERE account = :account AND type = '" + EntityFolder.TYPE_USER + "'")
    List<EntityFolder> getUserFolders(long account);

    @Query("SELECT folder.*, account.name AS accountName" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " GROUP BY folder.id")
    LiveData<List<TupleFolderEx>> liveFolders();

    @Query("SELECT * FROM folder WHERE account = :account")
    LiveData<List<EntityFolder>> liveFolders(long account);

    @Query("SELECT folder.* FROM folder WHERE folder.id = :id")
    LiveData<EntityFolder> liveFolder(long id);

    @Query("SELECT folder.*, account.name AS accountName" +
            ", COUNT(message.id) AS messages" +
            ", SUM(CASE WHEN message.ui_seen = 0 THEN 1 ELSE 0 END) AS unseen" +
            " FROM folder" +
            " LEFT JOIN account ON account.id = folder.account" +
            " LEFT JOIN message ON message.folder = folder.id AND NOT message.ui_hide" +
            " WHERE folder.id = :id")
    LiveData<TupleFolderEx> liveFolderEx(long id);

    @Query("SELECT * FROM folder WHERE id = :id")
    EntityFolder getFolder(Long id);

    @Query("SELECT * FROM folder WHERE account = :account AND name = :name")
    EntityFolder getFolderByName(Long account, String name);

    @Query("SELECT folder.* FROM folder" +
            " WHERE account = :account AND type = :type")
    EntityFolder getFolderByType(long account, String type);

    @Query("SELECT * FROM folder WHERE type = '" + EntityFolder.TYPE_OUTBOX + "'")
    EntityFolder getOutbox();

    @Query("SELECT folder.* FROM folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE account.`primary` AND type = '" + EntityFolder.TYPE_DRAFTS + "' ")
    EntityFolder getPrimaryDraftFolder();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertFolder(EntityFolder folder);

    @Update
    void updateFolder(EntityFolder folder);

    @Query("DELETE FROM folder WHERE account= :account AND name = :name")
    void deleteFolder(Long account, String name);
}
