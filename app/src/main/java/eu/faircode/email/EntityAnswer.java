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
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

// https://developer.android.com/training/data-storage/room/defining-data

@Entity(
        tableName = EntityAnswer.TABLE_NAME,
        foreignKeys = {
        },
        indices = {
        }
)
public class EntityAnswer implements Serializable {
    static final String TABLE_NAME = "answer";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public String name;
    public String group;
    @NonNull
    public Boolean standard;
    @NonNull
    public Boolean favorite;
    @NonNull
    public Boolean hide;
    @NonNull
    public String text;

    String getText(Address[] address) {
        return replacePlaceholders(text, address);
    }

    static String replacePlaceholders(String text, Address[] address) {
        String fullName = null;
        String email = null;
        if (address != null && address.length > 0) {
            fullName = ((InternetAddress) address[0]).getPersonal();
            email = ((InternetAddress) address[0]).getAddress();
        }

        if (fullName != null) {
            if (fullName.startsWith("\""))
                fullName = fullName.substring(1);
            if (fullName.endsWith("\""))
                fullName = fullName.substring(0, fullName.length() - 1);
        }

        String first = fullName;
        String last = null;
        if (fullName != null) {
            fullName = fullName.trim();
            int c = fullName.lastIndexOf(",");
            if (c > 0) {
                last = fullName.substring(0, c).trim();
                first = fullName.substring(c + 1).trim();
            } else {
                c = fullName.lastIndexOf(" ");
                if (c > 0) {
                    first = fullName.substring(0, c).trim();
                    last = fullName.substring(c + 1).trim();
                }
            }
        }

        text = text.replace("$name$", fullName == null ? "" : fullName);
        text = text.replace("$firstname$", first == null ? "" : first);
        text = text.replace("$lastname$", last == null ? "" : last);
        text = text.replace("$email$", email == null ? "" : email);

        return text;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("group", group);
        json.put("standard", standard);
        json.put("favorite", favorite);
        json.put("hide", hide);
        json.put("text", text);
        return json;
    }

    public static EntityAnswer fromJSON(JSONObject json) throws JSONException {
        EntityAnswer answer = new EntityAnswer();
        answer.id = json.getLong("id");
        answer.name = json.getString("name");
        answer.group = json.optString("group");
        answer.standard = json.optBoolean("standard");
        answer.favorite = json.optBoolean("favorite");
        answer.hide = json.optBoolean("hide");
        answer.text = json.getString("text");
        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAnswer) {
            EntityAnswer other = (EntityAnswer) obj;
            return (this.name.equals(other.name) &&
                    Objects.equals(this.group, other.group) &&
                    this.standard.equals(other.standard) &&
                    this.favorite.equals(other.favorite) &&
                    this.hide.equals(other.hide) &&
                    this.text.equals(other.text)
            );
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return name + (favorite ? " ★" : "");
    }
}
