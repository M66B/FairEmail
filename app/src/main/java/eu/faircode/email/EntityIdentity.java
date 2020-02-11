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

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

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
    public String host; // SMTP
    @NonNull
    public Boolean starttls;
    @NonNull
    public Boolean insecure = false;
    @NonNull
    public Integer port;
    @NonNull
    public Integer auth_type;
    public String provider;
    @NonNull
    public String user;
    @NonNull
    public String password;
    @NonNull
    public boolean certificate = false; // obsolete
    public String certificate_alias;
    public String realm;
    public String fingerprint;
    @NonNull
    public Boolean use_ip = true; // instead of domain name
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean sender_extra = false;
    public String sender_extra_regex;
    public String replyto;
    public String bcc;
    @NonNull
    public Boolean plain_only = false; // obsolete
    @NonNull
    public Boolean encrypt = false; // obsolete
    @NonNull
    public Boolean delivery_receipt = false; // obsolete
    @NonNull
    public Boolean read_receipt = false; // obsolete
    @NonNull
    public Boolean store_sent = false; // obsolete
    public Long sent_folder = null; // obsolete
    public Long sign_key = null; // OpenPGP
    public String sign_key_alias = null; // S/MIME
    public Boolean tbd; // obsolete
    public String state;
    public String error;
    public Long last_connected;

    String getProtocol() {
        return (starttls ? "smtp" : "smtps");
    }

    boolean sameAddress(Address address) {
        String other = ((InternetAddress) address).getAddress();
        if (other == null)
            return false;

        if (!other.contains("@") || !email.contains("@"))
            return false;

        return other.equalsIgnoreCase(email);
    }

    boolean similarAddress(Address address) {
        String other = ((InternetAddress) address).getAddress();
        if (other == null)
            return false;

        if (!other.contains("@") || !email.contains("@"))
            return false;

        String[] cother = other.split("@");
        String[] cemail = email.split("@");

        if (cother.length != 2 || cemail.length != 2)
            return false;

        // Domain
        if (!cother[1].equalsIgnoreCase(cemail[1]))
            return false;

        // User
        if (TextUtils.isEmpty(sender_extra_regex)) {
            String user = (cother[0].contains("+") ? cother[0].split("\\+")[0] : cother[0]);
            if (user.equalsIgnoreCase(cemail[0]))
                return true;
        } else {
            if (Pattern.matches(sender_extra_regex, cother[0]))
                return true;
        }

        return false;
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

        json.put("host", host);
        json.put("starttls", starttls);
        json.put("insecure", insecure);
        json.put("port", port);
        json.put("auth_type", auth_type);
        json.put("provider", provider);
        json.put("user", user);
        json.put("password", password);
        json.put("certificate_alias", certificate_alias);
        json.put("realm", realm);
        json.put("fingerprint", fingerprint);
        json.put("use_ip", use_ip);

        json.put("synchronize", synchronize);
        json.put("primary", primary);
        json.put("sender_extra", sender_extra);
        json.put("sender_extra_regex", sender_extra_regex);

        json.put("replyto", replyto);
        json.put("bcc", bcc);

        // not plain_only
        // not encrypt
        // not store_sent
        // not sent_folder
        // not sign_key
        // not tbd
        // not state
        // not error
        // not last_connected
        return json;
    }

    public static EntityIdentity fromJSON(JSONObject json) throws JSONException {
        EntityIdentity identity = new EntityIdentity();
        identity.id = json.getLong("id");
        identity.name = json.getString("name");
        identity.email = json.getString("email");
        if (json.has("display") && !json.isNull("display"))
            identity.display = json.getString("display");
        if (json.has("color"))
            identity.color = json.getInt("color");
        if (json.has("signature") && !json.isNull("signature"))
            identity.signature = json.getString("signature");

        identity.host = json.getString("host");
        identity.starttls = json.getBoolean("starttls");
        identity.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        identity.port = json.getInt("port");
        identity.auth_type = json.getInt("auth_type");
        if (json.has("provider"))
            identity.provider = json.getString("provider");
        identity.user = json.getString("user");
        identity.password = json.getString("password");
        if (json.has("certificate_alias"))
            identity.certificate_alias = json.getString("certificate_alias");
        if (json.has("realm") && !json.isNull("realm"))
            identity.realm = json.getString("realm");
        if (json.has("fingerprint"))
            identity.fingerprint = json.getString("fingerprint");
        if (json.has("use_ip"))
            identity.use_ip = json.getBoolean("use_ip");

        identity.synchronize = json.getBoolean("synchronize");
        identity.primary = json.getBoolean("primary");
        if (json.has("sender_extra"))
            identity.sender_extra = json.getBoolean("sender_extra");
        if (json.has("sender_extra_regex"))
            identity.sender_extra_regex = json.getString("sender_extra_regex");

        if (json.has("replyto") && !json.isNull("replyto"))
            identity.replyto = json.getString("replyto");
        if (json.has("bcc") && !json.isNull("bcc"))
            identity.bcc = json.getString("bcc");

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
                    this.host.equals(other.host) &&
                    this.starttls.equals(other.starttls) &&
                    this.insecure.equals(other.insecure) &&
                    this.port.equals(other.port) &&
                    this.auth_type.equals(other.auth_type) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    Objects.equals(this.realm, other.realm) &&
                    this.use_ip == other.use_ip &&
                    this.synchronize.equals(other.synchronize) &&
                    this.primary.equals(other.primary) &&
                    this.sender_extra.equals(sender_extra) &&
                    Objects.equals(this.sender_extra_regex, other.sender_extra_regex) &&
                    Objects.equals(this.replyto, other.replyto) &&
                    Objects.equals(this.bcc, other.bcc) &&
                    Objects.equals(this.sign_key, other.sign_key) &&
                    Objects.equals(this.sign_key_alias, other.sign_key_alias) &&
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
