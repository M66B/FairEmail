package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
        tableName = EntityAccount.TABLE_NAME,
        indices = {
        }
)
public class EntityAccount {
    static final String TABLE_NAME = "account";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public String name;
    public String signature;
    @NonNull
    public String host; // IMAP
    @NonNull
    public Integer port;
    @NonNull
    public String user;
    @NonNull
    public String password;
    @NonNull
    public Integer auth_type;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean primary;
    public Integer color;
    @NonNull
    public Boolean store_sent; // obsolete
    @NonNull
    public Integer poll_interval; // keep-alive interval
    public Long seen_until; // obsolete
    public String state;
    public String error;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAccount) {
            EntityAccount other = (EntityAccount) obj;
            return ((this.name == null ? other.name == null : this.name.equals(other.name)) &&
                    (this.signature == null ? other.signature == null : this.signature.equals(other.signature)) &&
                    this.host.equals(other.host) &&
                    this.port.equals(other.port) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    this.auth_type.equals(other.auth_type) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.primary.equals(other.primary) &&
                    (this.color == null ? other.color == null : this.color.equals(other.color)) &&
                    this.poll_interval.equals(other.poll_interval) &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("signature", signature);
        json.put("host", host);
        json.put("port", port);
        json.put("user", user);
        json.put("password", "");
        json.put("auth_type", auth_type);
        json.put("synchronize", false);
        json.put("primary", primary);
        if (color != null)
            json.put("color", color);
        json.put("poll_interval", poll_interval);
        return json;
    }

    public static EntityAccount fromJSON(JSONObject json) throws JSONException {
        EntityAccount account = new EntityAccount();
        if (json.has("name"))
            account.name = json.getString("name");
        if (json.has("signature"))
            account.signature = json.getString("signature");
        account.host = json.getString("host");
        account.port = json.getInt("port");
        account.user = json.getString("user");
        account.password = json.getString("password");
        account.auth_type = json.getInt("auth_type");
        account.synchronize = json.getBoolean("synchronize");
        account.primary = json.getBoolean("primary");
        if (json.has("color"))
            account.color = json.getInt("color");
        account.poll_interval = json.getInt("poll_interval");
        return account;
    }

    @Override
    public String toString() {
        return name + (primary ? " ★" : "");
    }
}
