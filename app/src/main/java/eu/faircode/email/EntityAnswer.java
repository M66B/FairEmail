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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

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
    @NonNull
    public Boolean hide;
    @NonNull
    public String text;

    static String getAnswerText(Context context, long id, Address[] from) {
        DB db = DB.getInstance(context);
        EntityAnswer answer = db.answer().getAnswer(id);
        if (answer == null)
            return null;

        return getAnswerText(answer, from);
    }

    static String getAnswerText(EntityAnswer answer, Address[] from) {
        String name = null;
        String email = null;
        String first = null;
        String last = null;
        if (from != null && from.length > 0) {
            name = ((InternetAddress) from[0]).getPersonal();
            email = ((InternetAddress) from[0]).getAddress();
        }
        if (name != null) {
            name = name.trim();
            int c = name.lastIndexOf(",");
            if (c < 0) {
                c = name.lastIndexOf(" ");
                if (c < 0) {
                    first = name;
                    last = name;
                } else {
                    first = name.substring(0, c).trim();
                    last = name.substring(c + 1).trim();
                }
            } else {
            }
        }

        return replacePlaceholders(answer.text, name, first, last, email);
    }

    static String replacePlaceholders(
            String text, String fullName, String firstName, String lastName, String email) {
        text = text.replace("$name$", fullName == null ? "" : fullName);
        text = text.replace("$firstname$", firstName == null ? "" : firstName);
        text = text.replace("$lastname$", lastName == null ? "" : lastName);
        text = text.replace("$email$", email == null ? "" : email);

        return text;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("hide", hide);
        json.put("text", text);
        return json;
    }

    public static EntityAnswer fromJSON(JSONObject json) throws JSONException {
        EntityAnswer answer = new EntityAnswer();
        answer.id = json.getLong("id");
        answer.name = json.getString("name");
        answer.hide = (json.has("hide") && json.getBoolean("hide"));
        answer.text = json.getString("text");
        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAnswer) {
            EntityAnswer other = (EntityAnswer) obj;
            return (this.name.equals(other.name) &&
                    this.hide.equals(other.hide) &&
                    this.text.equals(other.text)
            );
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
