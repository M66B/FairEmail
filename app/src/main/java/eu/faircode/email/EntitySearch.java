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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = EntitySearch.TABLE_NAME,
        foreignKeys = {
        },
        indices = {
        }
)
public class EntitySearch {
    static final String TABLE_NAME = "search";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public String name;
    public Integer order;
    public Integer color;
    @NonNull
    public String data;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntitySearch) {
            EntitySearch other = (EntitySearch) obj;
            return (this.id.equals(other.id) &&
                    this.name.equals(other.name) &&
                    Objects.equals(this.order, other.order) &&
                    Objects.equals(this.color, other.color) &&
                    this.data.equals(other.data));
        } else
            return false;
    }
}
