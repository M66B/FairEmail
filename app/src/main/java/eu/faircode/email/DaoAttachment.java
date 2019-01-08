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
public interface DaoAttachment {
    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " ORDER BY sequence")
    LiveData<List<EntityAttachment>> liveAttachments(long message);

    @Query("SELECT ifnull(MAX(sequence), 0)" +
            " FROM attachment" +
            " WHERE message = :message")
    int getAttachmentSequence(long message);

    @Query("SELECT COUNT(id)" +
            " FROM attachment" +
            " WHERE id = :id")
    int countAttachment(long id);

    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " ORDER BY sequence")
    List<EntityAttachment> getAttachments(long message);

    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " AND sequence = :sequence")
    EntityAttachment getAttachment(long message, int sequence);

    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " AND cid = :cid")
    EntityAttachment getAttachment(long message, String cid);

    @Query("UPDATE attachment" +
            " SET message = :message" +
            " WHERE id = :id")
    void setMessage(long id, long message);

    @Query("UPDATE attachment" +
            " SET progress = :progress, available = 0" +
            " WHERE id = :id")
    void setProgress(long id, Integer progress);

    @Query("UPDATE attachment" +
            " SET size = :size, progress = NULL, available = 1" +
            " WHERE id = :id")
    void setDownloaded(long id, Long size);

    @Query("UPDATE attachment" +
            " SET cid = :cid" +
            " WHERE id = :id")
    void setCid(long id, String cid);

    @Insert
    long insertAttachment(EntityAttachment attachment);

    @Query("DELETE FROM attachment" +
            " WHERE id = :id")
    int deleteAttachment(long id);
}
