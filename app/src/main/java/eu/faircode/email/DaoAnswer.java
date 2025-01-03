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
public interface DaoAnswer {
    @Query("SELECT * FROM answer" +
            " WHERE :all OR NOT hide" +
            " ORDER BY -favorite, name COLLATE NOCASE")
    List<EntityAnswer> getAnswers(boolean all);

    @Query("SELECT * FROM answer" +
            " WHERE ai" +
            " AND NOT hide" +
            " ORDER BY name COLLATE NOCASE")
    List<EntityAnswer> getAiPrompts();

    @Query("SELECT * FROM answer" +
            " WHERE favorite = :favorite" +
            " AND NOT hide" +
            " ORDER BY name COLLATE NOCASE")
    List<EntityAnswer> getAnswersByFavorite(boolean favorite);

    @Query("SELECT * FROM answer" +
            " WHERE snippet" +
            " AND NOT hide" +
            " ORDER BY name COLLATE NOCASE")
    List<EntityAnswer> getSnippets();

    @Query("SELECT * FROM answer" +
            " WHERE external" +
            " AND NOT hide" +
            " ORDER BY name COLLATE NOCASE")
    List<EntityAnswer> getAnswersExternal();

    @Query("SELECT * FROM answer WHERE id = :id")
    EntityAnswer getAnswer(long id);

    @Query("SELECT * FROM answer WHERE uuid = :uuid")
    EntityAnswer getAnswerByUUID(String uuid);

    @Query("SELECT * FROM answer WHERE name = :name")
    List<EntityAnswer> getAnswerByName(String name);

    @Query("SELECT * FROM answer" +
            " WHERE standard AND NOT hide")
    EntityAnswer getStandardAnswer();

    @Query("SELECT * FROM answer" +
            " WHERE receipt AND NOT hide")
    EntityAnswer getReceiptAnswer();

    @Query("SELECT * FROM answer")
    LiveData<List<EntityAnswer>> liveAnswers();

    @Query("SELECT COUNT(*) FROM answer" +
            " WHERE NOT hide" +
            " AND (:favorite OR NOT favorite)")
    Integer getAnswerCount(boolean favorite);

    @Query("SELECT DISTINCT `group` FROM answer" +
            " WHERE NOT `group` IS NULL" +
            " ORDER by `group` COLLATE NOCASE")
    List<String> getGroups();

    @Insert
    long insertAnswer(EntityAnswer answer);

    @Update
    int updateAnswer(EntityAnswer answer);

    @Query("UPDATE answer SET favorite = :favorite WHERE id = :id AND NOT (favorite IS :favorite)")
    int setAnswerFavorite(long id, boolean favorite);

    @Query("UPDATE answer SET hide = :hide WHERE id = :id AND NOT (hide IS :hide)")
    int setAnswerHidden(long id, boolean hide);

    @Query("UPDATE answer SET standard = 0 WHERE NOT (standard IS 0)")
    void resetStandard();

    @Query("UPDATE answer SET receipt = 0 WHERE NOT (receipt IS 0)")
    void resetReceipt();

    @Query("UPDATE answer" +
            " SET applied = applied + 1, last_applied = :time" +
            " WHERE id = :id")
    int applyAnswer(long id, long time);

    @Query("UPDATE answer" +
            " SET applied = 0, last_applied = NULL" +
            " WHERE id = :id AND NOT (applied IS 0)")
    int resetAnswer(long id);

    @Query("DELETE FROM answer WHERE id = :id")
    void deleteAnswer(long id);
}
