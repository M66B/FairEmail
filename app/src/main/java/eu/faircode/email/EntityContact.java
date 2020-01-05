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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

import static androidx.room.ForeignKey.CASCADE;

// https://developer.android.com/training/data-storage/room/defining-data

@Entity(
        tableName = EntityContact.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account", "type", "email"}, unique = true),
                @Index(value = {"email"}),
                @Index(value = {"name"}),
                @Index(value = {"avatar"}),
                @Index(value = {"times_contacted"}),
                @Index(value = {"last_contacted"}),
                @Index(value = {"state"})
        }
)
public class EntityContact implements Serializable {
    static final String TABLE_NAME = "contact";

    static final int TYPE_TO = 0;
    static final int TYPE_FROM = 1;

    static final int STATE_DEFAULT = 0;
    static final int STATE_FAVORITE = 1;
    static final int STATE_IGNORE = 2;

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long account;
    @NonNull
    public int type;
    @NonNull
    public String email;
    public String name;
    public String avatar;

    @NonNull
    public Integer times_contacted;
    @NonNull
    public Long first_contacted;
    @NonNull
    public Long last_contacted;
    @NonNull
    public Integer state = STATE_DEFAULT;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("type", type);
        json.put("email", email);
        json.put("name", name);
        json.put("avatar", avatar);
        json.put("times_contacted", times_contacted);
        json.put("first_contacted", first_contacted);
        json.put("last_contacted", last_contacted);
        json.put("state", state);
        return json;
    }

    public static EntityContact fromJSON(JSONObject json) throws JSONException {
        EntityContact contact = new EntityContact();
        // id
        contact.type = json.getInt("type");
        contact.email = json.getString("email");

        if (json.has("name") && !json.isNull("name"))
            contact.name = json.getString("name");

        if (json.has("avatar") && !json.isNull("avatar"))
            contact.avatar = json.getString("avatar");

        contact.times_contacted = json.getInt("times_contacted");
        contact.first_contacted = json.getLong("first_contacted");
        contact.last_contacted = json.getLong("last_contacted");
        contact.state = json.getInt("state");

        return contact;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof EntityContact) {
            EntityContact other = (EntityContact) obj;
            return (this.account == other.account &&
                    this.type == other.type &&
                    this.email.equals(other.email) &&
                    Objects.equals(this.name, other.name) &&
                    Objects.equals(this.avatar, other.avatar) &&
                    this.times_contacted.equals(other.times_contacted) &&
                    this.first_contacted.equals(first_contacted) &&
                    this.last_contacted.equals(last_contacted) &&
                    this.state.equals(other.state));
        } else
            return false;
    }

    @NonNull
    @Override
    public String toString() {
        return (name == null ? email : name + " <" + email + ">");
    }
}
