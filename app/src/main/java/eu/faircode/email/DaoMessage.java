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
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DaoMessage {
    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread AND NOT m.ui_hide) AS count" +
            ", SUM(CASE WHEN message.ui_seen THEN 0 ELSE 1 END) AS unseen" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            " FROM folder" +
            " JOIN message ON folder = folder.id" +
            " WHERE folder.type = '" + EntityFolder.TYPE_INBOX + "'" +
            " AND (NOT ui_hide OR :debug)" +
            " GROUP BY thread" +
            " ORDER BY message.received DESC")
        // in theory the message id and thread could be the same
    DataSource.Factory<Integer, TupleMessageEx> pagedUnifiedInbox(boolean debug);

    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread AND NOT m.ui_hide) AS count" +
            ", SUM(CASE WHEN message.ui_seen THEN 0 ELSE 1 END) AS unseen" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            " FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE folder.id = :folder" +
            " AND (NOT ui_hide OR :debug)" +
            " GROUP BY thread" +
            " ORDER BY received DESC, sent DESC")
    DataSource.Factory<Integer, TupleMessageEx> pagedFolder(long folder, boolean debug);

    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", 1 AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            " FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.account = (SELECT m1.account FROM message m1 WHERE m1.id = :msgid)" +
            " AND message.thread = (SELECT m2.thread FROM message m2 WHERE m2.id = :msgid)" +
            " AND (NOT ui_hide OR :debug)" +
            " ORDER BY received DESC")
    DataSource.Factory<Integer, TupleMessageEx> pagedThread(long msgid, boolean debug);

    @Query("SELECT * FROM message WHERE id = :id")
    EntityMessage getMessage(long id);

    @Query("SELECT * FROM message WHERE folder = :folder AND uid = :uid")
    EntityMessage getMessage(long folder, long uid);

    @Query("SELECT message.*, folder.name as folderName, folder.type as folderType" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread) AS count" +
            ", (SELECT COUNT(m.id) FROM message m WHERE m.account = message.account AND m.thread = message.thread AND NOT m.ui_seen) AS unseen" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
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
    int deleteMessage(long folder, long uid);

    @Query("DELETE FROM message WHERE folder = :folder")
    void deleteMessages(long folder);

    @Query("DELETE FROM message WHERE folder = :folder AND received < :received AND NOT uid IS NULL")
    int deleteMessagesBefore(long folder, long received);
}