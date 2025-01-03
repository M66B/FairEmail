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
public interface DaoRule {
    @Query("SELECT * FROM rule" +
            " WHERE folder = :folder" +
            " ORDER BY `order`, name COLLATE NOCASE")
    List<EntityRule> getRules(long folder);

    @Query("SELECT * FROM rule" +
            " WHERE folder = :folder" +
            " AND enabled" +
            " AND (:daily IS NULL OR daily = :daily)" +
            " ORDER BY `order`, name COLLATE NOCASE")
    List<EntityRule> getEnabledRules(long folder, Boolean daily);

    @Query("SELECT rule.*, folder.account, folder.name AS folderName, account.name AS accountName FROM rule" +
            " JOIN folder ON folder.id = rule.folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE rule.id = :id")
    TupleRuleEx getRule(long id);

    @Query("SELECT rule.* FROM rule" +
            " JOIN folder ON folder.id = rule.folder" +
            " WHERE folder.account = :account" +
            " AND rule.name = :name")
    List<EntityRule> getRuleByName(long account, String name);

    @Query("SELECT * FROM rule WHERE uuid = :uuid")
    EntityRule getRuleByUUID(String uuid);

    @Query("SELECT rule.*, folder.account, folder.name AS folderName, account.name AS accountName FROM rule" +
            " JOIN folder ON folder.id = rule.folder" +
            " JOIN account ON account.id = folder.account" +
            " WHERE rule.folder = :folder")
    LiveData<List<TupleRuleEx>> liveRules(long folder);

    @Query("SELECT DISTINCT `group` FROM rule" +
            " WHERE NOT `group` IS NULL" +
            " ORDER by `group` COLLATE NOCASE")
    List<String> getGroups();

    @Query("SELECT COUNT(*) FROM rule" +
            " JOIN folder ON folder.id = rule.folder" +
            " WHERE (:account IS NULL OR folder.account = :account)" +
            " AND (:folder IS NULL OR folder.id = :folder)" +
            " AND rule.enabled")
    int countTotal(Long account, Long folder);

    @Insert
    long insertRule(EntityRule rule);

    @Update
    int updateRule(EntityRule rule);

    @Query("UPDATE rule" +
            " SET folder = :folder" +
            " WHERE id = :id AND NOT (folder IS :folder)")
    int setRuleFolder(long id, long folder);

    @Query("UPDATE rule" +
            " SET enabled = :enabled" +
            " WHERE id = :id AND NOT (enabled IS :enabled)")
    int setRuleEnabled(long id, boolean enabled);

    @Query("UPDATE rule" +
            " SET `group` = :group" +
            " WHERE id = :id AND NOT (`group` IS :group)")
    int setRuleGroup(long id, String group);

    @Query("UPDATE rule" +
            " SET applied = applied + 1, last_applied = :time" +
            " WHERE id = :id")
    int applyRule(long id, long time);

    @Query("UPDATE rule" +
            " SET applied = 0, last_applied = NULL" +
            " WHERE id = :id AND NOT (applied IS 0)")
    int resetRule(long id);

    @Query("DELETE FROM rule WHERE id = :id")
    void deleteRule(long id);

    @Query("DELETE FROM rule WHERE folder = :folder")
    void deleteRules(long folder);
}
