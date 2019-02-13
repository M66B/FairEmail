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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// https://developer.android.com/training/data-storage/room/defining-data

@Entity(
        tableName = EntityContact.TABLE_NAME,
        foreignKeys = {
        },
        indices = {
                @Index(value = {"email", "type"}, unique = true),
                @Index(value = {"name", "type"}),
        }
)
public class EntityContact implements Serializable {
    static final String TABLE_NAME = "contact";

    static final int TYPE_TO = 0;
    static final int TYPE_FROM = 1;

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public int type;
    @NonNull
    public String email;
    public String name;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("type", type);
        json.put("email", email);
        json.put("name", name);
        return json;
    }

    public static EntityContact fromJSON(JSONObject json) throws JSONException {
        EntityContact contact = new EntityContact();
        // id
        contact.type = json.getInt("type");
        contact.email = json.getString("email");
        if (json.has("name"))
            contact.name = json.getString("name");
        return contact;
    }

    @NonNull
    @Override
    public String toString() {
        return (name == null ? email : name + " <" + email + ">");
    }
}
