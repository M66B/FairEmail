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

import android.database.Cursor;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DaoContact {
    @Query("SELECT * FROM contact")
    List<EntityContact> getContacts();

    @Query("SELECT *" +
            " FROM contact" +
            " WHERE email = :email" +
            " AND (:type IS NULL OR type = :type)")
    List<EntityContact> getContacts(Integer type, String email);

    @Query("SELECT id AS _id, name, email" +
            ", CASE type" +
            "  WHEN " + EntityContact.TYPE_TO + " THEN '>'" +
            "  WHEN " + EntityContact.TYPE_FROM + " THEN '<'" +
            "  ELSE '?'" +
            " END AS type" +
            " FROM contact" +
            " WHERE name LIKE :name" +
            " AND (:type IS NULL OR type = :type)")
    Cursor searchContacts(Integer type, String name);

    @Insert
    long insertContact(EntityContact contact);

    @Update
    int updateContact(EntityContact contact);

    @Query("DELETE from contact")
    int clearContacts();
}
