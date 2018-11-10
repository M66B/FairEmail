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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityIdentity.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account"})
        }
)
public class EntityIdentity {
    static final String TABLE_NAME = "identity";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public String name;
    @NonNull
    public String email;
    public String replyto;
    @NonNull
    public Long account;
    @NonNull
    public String host; // SMTP
    @NonNull
    public Boolean starttls;
    @NonNull
    public Boolean insecure;
    @NonNull
    public Integer port;
    @NonNull
    public String user;
    @NonNull
    public String password;
    @NonNull
    public Integer auth_type;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean store_sent;
    public String state;
    public String error;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("email", email);
        json.put("replyto", replyto);
        // not account
        json.put("host", host);
        json.put("starttls", starttls);
        json.put("insecure", insecure);
        json.put("port", port);
        json.put("user", user);
        json.put("password", "");
        json.put("auth_type", auth_type);
        json.put("primary", primary);
        json.put("synchronize", false);
        json.put("store_sent", store_sent);
        // not state
        // not error
        return json;
    }

    public static EntityIdentity fromJSON(JSONObject json) throws JSONException {
        EntityIdentity identity = new EntityIdentity();
        identity.name = json.getString("name");
        identity.email = json.getString("email");
        if (json.has("replyto"))
            identity.replyto = json.getString("replyto");
        identity.host = json.getString("host");
        identity.starttls = json.getBoolean("starttls");
        identity.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        identity.port = json.getInt("port");
        identity.user = json.getString("user");
        identity.password = json.getString("password");
        identity.auth_type = json.getInt("auth_type");
        identity.primary = json.getBoolean("primary");
        identity.synchronize = json.getBoolean("synchronize");
        identity.store_sent = json.getBoolean("store_sent");
        return identity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityIdentity) {
            EntityIdentity other = (EntityIdentity) obj;
            return (this.name.equals(other.name) &&
                    this.email.equals(other.email) &&
                    (this.replyto == null ? other.replyto == null : this.replyto.equals(other.replyto)) &&
                    this.account.equals(other.account) &&
                    this.host.equals(other.host) &&
                    this.starttls.equals(other.starttls) &&
                    this.insecure.equals(other.insecure) &&
                    this.port.equals(other.port) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    this.primary.equals(other.primary) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.store_sent.equals(other.store_sent) &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name + (primary ? " ★" : "");
    }
}
