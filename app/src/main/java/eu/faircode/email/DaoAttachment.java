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
public interface DaoAttachment {
    @Query("SELECT * FROM attachment WHERE message = :message")
    LiveData<List<EntityAttachment>> liveAttachments(long message);

    @Query("SELECT * FROM attachment WHERE message = :message AND sequence = :sequence")
    EntityAttachment getAttachment(long message, int sequence);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAttachment(EntityAttachment attachment);

    @Update
    void updateAttachment(EntityAttachment attachment);
}
