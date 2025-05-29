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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import static androidx.room.ForeignKey.CASCADE;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

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
    public String uuid = UUID.randomUUID().toString();
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
    public Boolean dnssec = false;
    @NonNull
    public String host; // SMTP
    @NonNull
    @ColumnInfo(name = "starttls")
    public Integer encryption;
    @NonNull
    public Boolean insecure = false;
    @NonNull
    public Boolean dane = false;
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
    public Boolean login = false;
    @NonNull
    public boolean certificate = false; // obsolete
    public String certificate_alias;
    public String realm;
    public String fingerprint;
    @NonNull
    public Boolean use_ip = true; // instead of domain name
    public String ehlo;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean self = true;
    @NonNull
    public Boolean sender_extra = false;
    @NonNull
    public Boolean sender_extra_name = false;
    @NonNull
    public Boolean reply_extra_name = false;
    public String sender_extra_regex;
    public String replyto;
    public String cc;
    public String bcc;
    public String envelopeFrom;
    public String internal;
    public String uri; // linked contact
    @NonNull
    public Boolean unicode = false;
    @NonNull
    public Boolean octetmime = false;
    @NonNull
    public Boolean plain_only = false; // obsolete
    @NonNull
    public Boolean sign_default = false;
    @NonNull
    public Boolean encrypt_default = false;
    @NonNull
    public Integer encrypt = 0; // Default method 0=PGP 1=S/MIME
    public Integer receipt_type;
    @NonNull
    public Boolean delivery_receipt = false; // obsolete
    @NonNull
    public Boolean read_receipt = false; // obsolete
    @NonNull
    public Integer sensitivity = 0; // Normal
    @NonNull
    public Boolean store_sent = false; // obsolete
    public Long sent_folder = null; // obsolete
    public Long sign_key = null; // OpenPGP
    public String sign_key_alias = null; // S/MIME
    public Boolean tbd; // obsolete
    public String state;
    public String error;
    public Long last_connected;
    public Long max_size;
    public Long last_modified; // sync

    String getProtocol() {
        return (encryption == EmailService.ENCRYPTION_SSL ? "smtps" : "smtp");
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

        if (TextUtils.isEmpty(sender_extra_regex)) {
            // Domain
            if ("secure.mailbox.org".equalsIgnoreCase(cother[1]))
                cother[1] = "mailbox.org";
            if ("secure.mailbox.org".equalsIgnoreCase(cemail[1]))
                cemail[1] = "mailbox.org";
            if (!cother[1].equalsIgnoreCase(cemail[1]))
                return false;

            // User
            int plus = cother[0].indexOf('+');
            String user = (plus < 0 ? cother[0] : cother[0].substring(0, plus));
            if (user.equalsIgnoreCase(cemail[0]))
                return true;
        } else {
            // Domain
            boolean at = sender_extra_regex.contains("@");
            if (!at && !cother[1].equalsIgnoreCase(cemail[1]))
                return false;

            // User
            String input = (at ? other.toLowerCase(Locale.ROOT) : cother[0]);
            if (Pattern.matches(sender_extra_regex, input))
                return true;
        }

        return false;
    }

    void setAlias(String alias, int type) throws JSONException {
        JSONObject jaliases;
        if (sign_key_alias == null)
            jaliases = new JSONObject();
        else
            try {
                jaliases = new JSONObject(sign_key_alias);
            } catch (JSONException ex) {
                Log.w(ex);
                jaliases = new JSONObject();
                jaliases.put("s", sign_key_alias);
                jaliases.put("e", sign_key_alias);
            }

        jaliases.put(type == EntityMessage.SMIME_SIGNONLY ? "s" : "e", alias);
        sign_key_alias = jaliases.toString();
    }

    String getAlias(int type) {
        if (sign_key_alias == null)
            return null;

        try {
            JSONObject jaliases = new JSONObject(sign_key_alias);
            String key = (type == EntityMessage.SMIME_SIGNONLY ? "s" : "e");
            return (jaliases.has(key) ? jaliases.getString(key) : null);
        } catch (JSONException ex) {
            Log.w(ex);
            return sign_key_alias;
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("uuid", uuid);
        json.put("name", name);
        json.put("email", email);
        // not account
        json.put("display", display);
        if (color != null)
            json.put("color", color);
        json.put("signature", signature);

        json.put("host", host);
        json.put("encryption", encryption);
        json.put("insecure", insecure);
        json.put("port", port);
        json.put("auth_type", auth_type);
        json.put("provider", provider);
        json.put("user", user);
        json.put("password", password);
        json.put("login", login);
        json.put("certificate_alias", certificate_alias);
        json.put("realm", realm);
        json.put("fingerprint", fingerprint);
        json.put("use_ip", use_ip);
        json.put("ehlo", ehlo);

        json.put("synchronize", synchronize);
        json.put("primary", primary);
        json.put("self", self);
        json.put("sender_extra", sender_extra);
        json.put("sender_extra_name", sender_extra_name);
        json.put("reply_extra_name", reply_extra_name);
        json.put("sender_extra_regex", sender_extra_regex);

        json.put("replyto", replyto);
        json.put("cc", cc);
        json.put("bcc", bcc);
        json.put("internal", internal);
        json.put("uri", uri);

        json.put("unicode", unicode);
        json.put("octetmime", octetmime);
        // not plain_only
        json.put("sign_default", sign_default);
        json.put("encrypt_default", encrypt_default);
        // not encrypt
        if (receipt_type != null)
            json.put("receipt_type", receipt_type);
        // delivery_receipt
        // read_receipt
        json.put("sensitivity", sensitivity);
        // not store_sent
        // not sent_folder
        // not sign_key
        // sign_key_alias
        // not tbd
        // not state
        // not error
        // not last_connected
        // not max_size
        return json;
    }

    public static EntityIdentity fromJSON(JSONObject json) throws JSONException {
        EntityIdentity identity = new EntityIdentity();
        identity.id = json.getLong("id");

        if (json.has("uuid") && !json.isNull("uuid"))
            identity.uuid = json.getString("uuid");

        identity.name = json.getString("name");
        identity.email = json.getString("email");
        if (json.has("display") && !json.isNull("display"))
            identity.display = json.getString("display");
        if (json.has("color"))
            identity.color = json.getInt("color");
        if (json.has("signature") && !json.isNull("signature"))
            identity.signature = json.getString("signature");

        identity.host = json.getString("host");
        if (json.has("starttls"))
            identity.encryption = (json.getBoolean("starttls")
                    ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);
        else
            identity.encryption = json.getInt("encryption");
        identity.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        identity.port = json.getInt("port");
        identity.auth_type = json.getInt("auth_type");
        if (json.has("provider") && !json.isNull("provider"))
            identity.provider = json.getString("provider");
        identity.user = json.getString("user");
        identity.password = json.getString("password");
        identity.login = json.optBoolean("login");
        if (json.has("certificate_alias") && !json.isNull("certificate_alias"))
            identity.certificate_alias = json.getString("certificate_alias");
        if (json.has("realm") && !json.isNull("realm"))
            identity.realm = json.getString("realm");
        if (json.has("fingerprint") && !json.isNull("fingerprint"))
            identity.fingerprint = json.getString("fingerprint");
        if (json.has("use_ip"))
            identity.use_ip = json.getBoolean("use_ip");
        if (json.has("ehlo") && !json.isNull("ehlo"))
            identity.ehlo = json.getString("ehlo");

        identity.synchronize = json.getBoolean("synchronize");
        identity.primary = json.getBoolean("primary");
        identity.self = json.optBoolean("self", true);

        if (json.has("sender_extra"))
            identity.sender_extra = json.getBoolean("sender_extra");
        if (json.has("sender_extra_name"))
            identity.sender_extra_name = json.getBoolean("sender_extra_name");
        if (json.has("reply_extra_name"))
            identity.reply_extra_name = json.getBoolean("reply_extra_name");
        if (json.has("sender_extra_regex") && !json.isNull("sender_extra_regex"))
            identity.sender_extra_regex = json.getString("sender_extra_regex");

        if (json.has("replyto") && !json.isNull("replyto"))
            identity.replyto = json.getString("replyto");
        if (json.has("cc") && !json.isNull("cc"))
            identity.cc = json.getString("cc");
        if (json.has("bcc") && !json.isNull("bcc"))
            identity.bcc = json.getString("bcc");
        if (json.has("internal") && !json.isNull("internal"))
            identity.internal = json.getString("internal");
        if (json.has("uri") && !json.isNull("uri"))
            identity.uri = json.getString("uri");

        if (json.has("unicode"))
            identity.unicode = json.getBoolean("unicode");

        if (json.has("octetmime"))
            identity.octetmime = json.getBoolean("octetmime");

        if (json.has("sign_default"))
            identity.sign_default = json.getBoolean("sign_default");
        if (json.has("encrypt_default"))
            identity.encrypt_default = json.getBoolean("encrypt_default");

        if (json.has("receipt_type"))
            identity.receipt_type = json.getInt("receipt_type");

        if (json.has("sensitivity"))
            identity.sensitivity = json.getInt("sensitivity");

        return identity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityIdentity) {
            EntityIdentity other = (EntityIdentity) obj;
            return areEqual(this, other, true, true);
        } else
            return false;
    }

    public static boolean areEqual(EntityIdentity i1, EntityIdentity other, boolean auth, boolean state) {
        return (Objects.equals(i1.uuid, other.uuid) &&
                i1.name.equals(other.name) &&
                i1.email.equals(other.email) &&
                (!state || Objects.equals(i1.account, other.account)) &&
                Objects.equals(i1.display, other.display) &&
                Objects.equals(i1.color, other.color) &&
                Objects.equals(i1.signature, other.signature) &&
                i1.host.equals(other.host) &&
                i1.encryption.equals(other.encryption) &&
                i1.insecure.equals(other.insecure) &&
                i1.port.equals(other.port) &&
                i1.auth_type.equals(other.auth_type) &&
                Objects.equals(i1.provider, other.provider) &&
                i1.user.equals(other.user) &&
                (!auth || i1.password.equals(other.password)) &&
                // login
                // certificate
                Objects.equals(i1.certificate_alias, other.certificate_alias) &&
                Objects.equals(i1.realm, other.realm) &&
                Objects.equals(i1.fingerprint, other.fingerprint) &&
                i1.use_ip == other.use_ip &&
                Objects.equals(i1.ehlo, other.ehlo) &&
                i1.synchronize.equals(other.synchronize) &&
                i1.primary.equals(other.primary) &&
                i1.self.equals(other.self) &&
                i1.sender_extra.equals(other.sender_extra) &&
                i1.sender_extra_name.equals(other.sender_extra_name) &&
                Objects.equals(i1.sender_extra_regex, other.sender_extra_regex) &&
                Objects.equals(i1.replyto, other.replyto) &&
                Objects.equals(i1.cc, other.cc) &&
                Objects.equals(i1.bcc, other.bcc) &&
                Objects.equals(i1.internal, other.internal) &&
                Objects.equals(i1.uri, other.uri) &&
                Objects.equals(i1.unicode, other.unicode) &&
                Objects.equals(i1.octetmime, other.octetmime) &&
                // plain_only
                Objects.equals(i1.sign_default, other.sign_default) &&
                Objects.equals(i1.encrypt_default, other.encrypt_default) &&
                Objects.equals(i1.encrypt, other.encrypt) &&
                Objects.equals(i1.receipt_type, other.receipt_type) &&
                // delivery_receipt
                // read_receipt
                Objects.equals(i1.sensitivity, other.sensitivity) &&
                // store_sent
                // sent_folder
                Objects.equals(i1.sign_key, other.sign_key) &&
                Objects.equals(i1.sign_key_alias, other.sign_key_alias) &&
                Objects.equals(i1.tbd, other.tbd) &&
                (!state || Objects.equals(i1.state, other.state)) &&
                (!state || Objects.equals(i1.error, other.error)) &&
                (!state || Objects.equals(i1.last_connected, other.last_connected)) &&
                (!state || Objects.equals(i1.max_size, other.max_size)) &&
                (!state || Objects.equals(i1.last_modified, other.last_modified)));
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
