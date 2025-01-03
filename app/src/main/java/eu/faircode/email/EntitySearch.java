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

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

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
    public String account_uuid;
    public String folder_name;
    @NonNull
    public String name;
    public Integer order;
    public Integer color;
    @NonNull
    public String data;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("account", account_uuid);
        json.put("folder", folder_name);
        json.put("name", name);
        json.put("order", order);
        json.put("color", color);
        json.put("data", data);
        return json;
    }

    public static EntitySearch fromJSON(JSONObject json) throws JSONException {
        EntitySearch search = new EntitySearch();
        // id
        if (json.has("account") && !json.isNull("account"))
            search.account_uuid = json.getString("account");
        if (json.has("folder") && !json.isNull("folder"))
            search.folder_name = json.getString("folder");
        search.name = json.getString("name");
        if (json.has("order") && !json.isNull("order"))
            search.order = json.getInt("order");
        if (json.has("color") && !json.isNull("color"))
            search.color = json.getInt("color");
        search.data = json.getString("data");

        return search;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntitySearch) {
            EntitySearch other = (EntitySearch) obj;
            return (Objects.equals(this.account_uuid, other.account_uuid) &&
                    Objects.equals(this.folder_name, other.folder_name) &&
                    this.name.equals(other.name) &&
                    Objects.equals(this.order, other.order) &&
                    Objects.equals(this.color, other.color) &&
                    this.data.equals(other.data));
        } else
            return false;
    }
}
