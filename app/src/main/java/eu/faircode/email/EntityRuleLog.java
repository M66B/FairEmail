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

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityRuleLog.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "rule", entity = EntityRule.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "message", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE),
        },
        indices = {
                @Index(value = {"rule"}),
                @Index(value = {"message"}),
                @Index(value = {"time"})
        }
)
public class EntityRuleLog {
    static final String TABLE_NAME = "rule_log";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long rule;
    @NonNull
    public Long message;
    @NonNull
    public Long time;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityRuleLog) {
            EntityRuleLog other = (EntityRuleLog) obj;
            return this.id.equals(other.id);
        } else
            return false;
    }
}
