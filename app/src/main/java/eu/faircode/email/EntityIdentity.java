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
                @Index(value = {"account"}),
                @Index(value = {"account", "email"})
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
    @NonNull
    public Long account;
    public String display;
    public Integer color;
    public String signature;
    @NonNull
    public Integer auth_type;
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
    public String realm;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean primary;
    public String replyto;
    public String bcc;
    @NonNull
    public Boolean delivery_receipt;
    @NonNull
    public Boolean read_receipt;
    @NonNull
    public Boolean store_sent = false; // obsolete
    public Long sent_folder;
    public Boolean tbd;
    public String state;
    public String error;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("email", email);
        json.put("display", display);
        if (color != null)
            json.put("color", color);
        json.put("signature", signature);
        // not account
        json.put("auth_type", auth_type);
        json.put("host", host);
        json.put("starttls", starttls);
        json.put("insecure", insecure);
        json.put("port", port);
        json.put("user", user);
        json.put("password", password);
        json.put("synchronize", synchronize);
        json.put("primary", primary);

        json.put("replyto", replyto);
        json.put("bcc", bcc);
        json.put("delivery_receipt", delivery_receipt);
        json.put("read_receipt", read_receipt);
        json.put("store_sent", store_sent);
        if (sent_folder != null)
            json.put("sent_folder", sent_folder);
        // not state
        // not error
        return json;
    }

    public static EntityIdentity fromJSON(JSONObject json) throws JSONException {
        EntityIdentity identity = new EntityIdentity();
        identity.name = json.getString("name");
        identity.email = json.getString("email");

        if (json.has("display"))
            identity.display = json.getString("display");

        if (json.has("color"))
            identity.color = json.getInt("color");
        if (json.has("signature"))
            identity.signature = json.getString("signature");

        identity.auth_type = json.getInt("auth_type");
        identity.host = json.getString("host");
        identity.starttls = json.getBoolean("starttls");
        identity.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        identity.port = json.getInt("port");
        identity.user = json.getString("user");
        identity.password = json.getString("password");

        identity.synchronize = json.getBoolean("synchronize");
        identity.primary = json.getBoolean("primary");

        if (json.has("replyto"))
            identity.replyto = json.getString("replyto");

        if (json.has("bcc"))
            identity.replyto = json.getString("bcc");

        if (json.has("delivery_receipt"))
            identity.delivery_receipt = json.getBoolean("delivery_receipt");
        else
            identity.delivery_receipt = false;

        if (json.has("read_receipt"))
            identity.read_receipt = json.getBoolean("read_receipt");
        else
            identity.read_receipt = false;

        if (json.has("store_sent"))
            identity.store_sent = json.getBoolean("store_sent");

        if (json.has("sent_folder"))
            identity.sent_folder = json.getLong("sent_folder");

        return identity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityIdentity) {
            EntityIdentity other = (EntityIdentity) obj;
            return (this.name.equals(other.name) &&
                    this.email.equals(other.email) &&
                    this.account.equals(other.account) &&
                    (this.display == null ? other.display == null : this.display.equals(other.display)) &&
                    (this.color == null ? other.color == null : this.color.equals(other.color)) &&
                    (this.signature == null ? other.signature == null : this.signature.equals(other.signature)) &&
                    this.host.equals(other.host) &&
                    this.starttls.equals(other.starttls) &&
                    this.insecure.equals(other.insecure) &&
                    this.port.equals(other.port) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.primary.equals(other.primary) &&
                    (this.replyto == null ? other.replyto == null : this.replyto.equals(other.replyto)) &&
                    this.delivery_receipt.equals(other.delivery_receipt) &&
                    this.read_receipt.equals(other.read_receipt) &&
                    (this.sent_folder == null ? other.sent_folder == null : this.sent_folder.equals(other.sent_folder)) &&
                    (this.tbd == null ? other.tbd == null : this.tbd.equals(other.tbd)) &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }

    String getDisplayName() {
        return (display == null ? name : display);
    }
}
