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

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

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
    public Boolean use_ip = true; // instead of domain name
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean primary;
    @NonNull
    public boolean sender_extra = false;
    public String replyto;
    public String bcc;
    @NonNull
    public Boolean plain_only = false;
    @NonNull
    public Boolean encrypt = false;
    @NonNull
    public Boolean delivery_receipt = false;
    @NonNull
    public Boolean read_receipt = false;
    @NonNull
    public Boolean store_sent = false;
    public Long sent_folder = null; // obsolete
    public Boolean tbd;
    public String state;
    public String error;
    public Long last_connected;

    String getProtocol() {
        return (starttls ? "smtp" : "smtps");
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
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
        json.put("realm", realm);
        json.put("use_ip", use_ip);

        json.put("synchronize", synchronize);
        json.put("primary", primary);

        json.put("replyto", replyto);
        json.put("bcc", bcc);

        json.put("plain_only", plain_only);
        json.put("encrypt", encrypt);
        json.put("delivery_receipt", delivery_receipt);
        json.put("read_receipt", read_receipt);
        json.put("store_sent", store_sent);
        // not state
        // not error
        return json;
    }

    public static EntityIdentity fromJSON(JSONObject json) throws JSONException {
        EntityIdentity identity = new EntityIdentity();
        // id
        identity.name = json.getString("name");
        identity.email = json.getString("email");
        if (json.has("display") && !json.isNull("display"))
            identity.display = json.getString("display");
        if (json.has("color"))
            identity.color = json.getInt("color");
        if (json.has("signature") && !json.isNull("signature"))
            identity.signature = json.getString("signature");

        identity.auth_type = json.getInt("auth_type");
        identity.host = json.getString("host");
        identity.starttls = json.getBoolean("starttls");
        identity.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        identity.port = json.getInt("port");
        identity.user = json.getString("user");
        identity.password = json.getString("password");
        if (json.has("realm") && !json.isNull("realm"))
            identity.realm = json.getString("realm");
        if (json.has("use_ip"))
            identity.use_ip = json.getBoolean("use_ip");

        identity.synchronize = json.getBoolean("synchronize");
        identity.primary = json.getBoolean("primary");

        if (json.has("replyto") && !json.isNull("replyto"))
            identity.replyto = json.getString("replyto");
        if (json.has("bcc") && !json.isNull("bcc"))
            identity.bcc = json.getString("bcc");

        if (json.has("plain_only"))
            identity.plain_only = json.getBoolean("plain_only");
        if (json.has("encrypt"))
            identity.encrypt = json.getBoolean("encrypt");
        if (json.has("delivery_receipt"))
            identity.delivery_receipt = json.getBoolean("delivery_receipt");
        if (json.has("read_receipt"))
            identity.read_receipt = json.getBoolean("read_receipt");

        if (json.has("store_sent"))
            identity.store_sent = json.getBoolean("store_sent");

        return identity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityIdentity) {
            EntityIdentity other = (EntityIdentity) obj;
            return (this.name.equals(other.name) &&
                    this.email.equals(other.email) &&
                    this.account.equals(other.account) &&
                    Objects.equals(this.display, other.display) &&
                    Objects.equals(this.color, other.color) &&
                    Objects.equals(this.signature, other.signature) &&
                    this.auth_type.equals(other.auth_type) &&
                    this.host.equals(other.host) &&
                    this.starttls.equals(other.starttls) &&
                    this.insecure.equals(other.insecure) &&
                    this.port.equals(other.port) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    Objects.equals(this.realm, other.realm) &&
                    this.use_ip == other.use_ip &&
                    this.synchronize.equals(other.synchronize) &&
                    this.primary.equals(other.primary) &&
                    Objects.equals(this.replyto, other.replyto) &&
                    Objects.equals(this.bcc, other.bcc) &&
                    this.plain_only.equals(other.plain_only) &&
                    this.encrypt.equals(other.encrypt) &&
                    this.delivery_receipt.equals(other.delivery_receipt) &&
                    this.read_receipt.equals(other.read_receipt) &&
                    this.store_sent.equals(other.store_sent) &&
                    Objects.equals(this.tbd, other.tbd) &&
                    Objects.equals(this.state, other.state) &&
                    Objects.equals(this.error, other.error) &&
                    Objects.equals(this.last_connected, other.last_connected));
        } else
            return false;
    }

    String getDisplayName() {
        return (display == null ? name : display);
    }

    @NonNull
    @Override
    public String toString() {
        return getDisplayName() + " <" + email + ">" + (primary ? " ★" : "");
    }
}
