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
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoSearch {
    @Query("SELECT * FROM search" +
            " ORDER BY `order`, name COLLATE NOCASE")
    LiveData<List<EntitySearch>> liveSearches();

    @Query("SELECT * FROM search")
    List<EntitySearch> getSearches();

    @Insert
    long insertSearch(EntitySearch search);

    @Query("SELECT * FROM search" +
            " WHERE id = :id")
    EntitySearch getSearch(long id);

    @Update
    int updateSearch(EntitySearch search);

    @Query("DELETE FROM search" +
            " WHERE id = :id")
    int deleteSearch(long id);
}
