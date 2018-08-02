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
public interface DaoMessage {
    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread) AS count" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread AND NOT m.ui_seen) AS unseen" +
            " FROM folder" +
            " JOIN message ON folder = folder.id" +
            " WHERE folder.type = '" + EntityFolder.TYPE_INBOX + "'" +
            " AND NOT ui_hide" +
            " AND received IN (SELECT MAX(m.received) FROM message m WHERE m.folder = message.folder GROUP BY m.thread)")
    LiveData<List<TupleMessageEx>> liveUnifiedInbox();

    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread) AS count" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread AND NOT m.ui_seen) AS unseen" +
            " FROM folder" +
            " JOIN message ON folder = folder.id" +
            " WHERE folder.id = :folder" +
            " AND NOT ui_hide" +
            " AND received IN (SELECT MAX(m.received) FROM message m WHERE m.folder = message.folder GROUP BY m.thread)")
    LiveData<List<TupleMessageEx>> liveMessages(long folder);

    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread) AS count" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread AND NOT m.ui_seen) AS unseen" +
            " FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " JOIN message m1 ON m1.id = :msgid AND m1.account = message.account AND m1.thread = message.thread" +
            " WHERE NOT message.ui_hide")
    LiveData<List<TupleMessageEx>> liveThread(long msgid);

    @Query("SELECT * FROM message WHERE id = :id")
    EntityMessage getMessage(long id);

    @Query("SELECT * FROM message WHERE folder = :folder AND uid = :uid")
    EntityMessage getMessage(long folder, long uid);

    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread) AS count" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread AND NOT m.ui_seen) AS unseen" +
            " FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.id = :id")
    LiveData<TupleMessageEx> liveMessage(long id);

    @Query("SELECT uid FROM message WHERE folder = :folder AND received >= :received AND NOT uid IS NULL")
    List<Long> getUids(long folder, long received);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMessage(EntityMessage message);

    @Update
    void updateMessage(EntityMessage message);

    @Query("DELETE FROM message WHERE id = :id")
    void deleteMessage(long id);

    @Query("DELETE FROM message WHERE folder = :folder AND uid = :uid")
    void deleteMessage(long folder, long uid);

    @Query("DELETE FROM message WHERE folder = :folder")
    int deleteMessages(long folder);

    @Query("DELETE FROM message WHERE folder = :folder AND received < :received AND NOT uid IS NULL")
    int deleteMessagesBefore(long folder, long received);
}