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

import java.util.List;

@Dao
public interface DaoLog {
    @Query("SELECT * FROM log" +
            " WHERE time > :from" +
            " AND (:type IS NULL OR type = :type)" +
            " ORDER BY time DESC" +
            " LIMIT :limit")
    LiveData<List<EntityLog>> liveLogs(long from, int limit, Integer type);

    @Query("SELECT * FROM log" +
            " WHERE time > :from" +
            " AND (:type IS NULL OR type = :type)" +
            " ORDER BY time DESC")
    List<EntityLog> getLogs(long from, Integer type);

    @Insert
    long insertLog(EntityLog log);

    @Query("DELETE FROM log" +
            " WHERE id IN (SELECT id FROM log" +
            " WHERE time < :before ORDER BY time LIMIT :limit)")
    int deleteLogs(long before, int limit);
}
