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
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DaoAttachment {
    @Query("SELECT id,message,sequence,name,type,size,progress" +
            ", (NOT content IS NULL) as content" +
            " FROM attachment WHERE message = :message")
    LiveData<List<TupleAttachment>> liveAttachments(long message);

    @Query("SELECT * FROM attachment WHERE message = :message AND sequence = :sequence")
    EntityAttachment getAttachment(long message, int sequence);

    @Query("SELECT COUNT(attachment.id) FROM attachment WHERE message = :message")
    int getAttachmentCount(long message);

    @Query("UPDATE attachment SET progress = :progress WHERE id = :id")
    void setProgress(long id, int progress);

    @Query("SELECT content FROM attachment WHERE id = :id")
    byte[] getContent(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAttachment(EntityAttachment attachment);

    @Update
    void updateAttachment(EntityAttachment attachment);
}
