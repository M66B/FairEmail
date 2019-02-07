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

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
    public String text;

    static String getAnswerText(DB db, long id, Address[] from) {
        EntityAnswer answer = db.answer().getAnswer(id);
        if (answer == null)
            return null;

        String name = null;
        String email = null;
        if (from != null && from.length > 0) {
            name = ((InternetAddress) from[0]).getPersonal();
            email = ((InternetAddress) from[0]).getAddress();
        }

        String text = answer.text;
        text = text.replace("$name$", name == null ? "" : name);
        text = text.replace("$email$", email == null ? "" : email);

        return text;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("text", text);
        return json;
    }

    public static EntityAnswer fromJSON(JSONObject json) throws JSONException {
        EntityAnswer answer = new EntityAnswer();
        // id
        answer.name = json.getString("name");
        answer.text = json.getString("text");
        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAnswer) {
            EntityAnswer other = (EntityAnswer) obj;
            return (this.name.equals(other.name) &&
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
