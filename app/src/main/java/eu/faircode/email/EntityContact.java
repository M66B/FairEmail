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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

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
    static final int TYPE_JUNK = 2;
    static final int TYPE_NO_JUNK = 3;

    static final int STATE_DEFAULT = 0;
    static final int STATE_FAVORITE = 1;
    static final int STATE_IGNORE = 2;

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long account;
    public Long identity; // no foreign key, no index
    public Long folder; // last used
    @NonNull
    public int type;
    @NonNull
    public String email;
    public String name;
    public String group;
    public String avatar;

    @NonNull
    public Integer times_contacted;
    @NonNull
    public Long first_contacted;
    @NonNull
    public Long last_contacted;
    @NonNull
    public Integer state = STATE_DEFAULT;

    static void received(
            @NonNull Context context,
            @NonNull EntityAccount account,
            @NonNull EntityFolder folder,
            @NonNull EntityMessage message) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int purge_contact_age = prefs.getInt("purge_contact_age", 1);
        if (purge_contact_age > 0) {
            long now = new Date().getTime();
            long ago = now - purge_contact_age * 30 * 24 * 3600 * 1000L;
            if (message.received < ago)
                return;
        }

        if (EntityFolder.DRAFTS.equals(folder.type) ||
                EntityFolder.ARCHIVE.equals(folder.type) ||
                EntityFolder.TRASH.equals(folder.type) ||
                EntityFolder.JUNK.equals(folder.type))
            return;

        boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
        boolean suggest_received = prefs.getBoolean("suggest_received", false);

        // Shortcut
        if (!suggest_sent && !suggest_received)
            return;

        int type = (folder.isOutgoing() ? TYPE_TO : TYPE_FROM);

        // Check if from self
        if (type == TYPE_FROM) {
            if (message.from != null) {
                List<EntityIdentity> identities = Core.getIdentities(folder.account, context);
                if (identities != null) {
                    for (Address sender : message.from) {
                        for (EntityIdentity identity : identities)
                            if (identity.similarAddress(sender)) {
                                type = TYPE_TO;
                                break;
                            }
                        if (type == TYPE_TO)
                            break;
                    }
                }
            }
        }

        if (type == TYPE_TO && !suggest_sent)
            return;
        if (type == TYPE_FROM && !suggest_received)
            return;

        List<Address> addresses = new ArrayList<>();
        if (type == TYPE_FROM) {
            if (message.reply == null || message.reply.length == 0) {
                if (message.from != null)
                    addresses.addAll(filterAddresses(message.from));
            } else
                addresses.addAll(filterAddresses(message.reply));
        } else if (type == TYPE_TO) {
            if (message.to != null)
                addresses.addAll(filterAddresses(message.to));
            if (message.cc != null)
                addresses.addAll(filterAddresses(message.cc));
            if (message.bcc != null)
                addresses.addAll(filterAddresses(message.bcc));
        }

        update(context, folder.account, message.identity, addresses.toArray(new Address[0]), type, message.received);
    }

    private static List<Address> filterAddresses(Address[] addresses) {
        List<Address> result = new ArrayList<>();

        if (addresses != null)
            for (Address address : addresses)
                if (!MessageHelper.isNoReply(address))
                    result.add(address);

        return result;
    }

    public static void update(Context context, long account, Long identity, Address[] addresses, int type, long time) {
        update(context, account, identity, addresses, null, type, time);
    }

    public static void update(Context context, long account, Long identity, Address[] addresses, String group, int type, long time) {
        if (addresses == null)
            return;

        DB db = DB.getInstance(context);
        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            String name = ((InternetAddress) address).getPersonal();
            Uri avatar = ContactInfo.getLookupUri(new Address[]{address});

            if (TextUtils.isEmpty(email))
                continue;
            if (TextUtils.isEmpty(name))
                name = null;

            try {
                db.beginTransaction();

                EntityContact contact = db.contact().getContact(account, type, email);
                if (contact == null) {
                    contact = new EntityContact();
                    contact.account = account;
                    if (type == TYPE_TO)
                        contact.identity = identity;
                    contact.type = type;
                    contact.email = email;
                    contact.name = name;
                    contact.group = group;
                    contact.avatar = (avatar == null ? null : avatar.toString());
                    contact.times_contacted = 1;
                    contact.first_contacted = time;
                    contact.last_contacted = time;
                    contact.id = db.contact().insertContact(contact);
                    Log.i("Inserted contact=" + contact + " type=" + type);
                } else {
                    if (type == TYPE_TO)
                        contact.identity = identity;
                    if (contact.name == null && name != null)
                        contact.name = name;
                    if (contact.group == null && group != null)
                        contact.group = group;
                    contact.avatar = (avatar == null ? null : avatar.toString());
                    contact.times_contacted++;
                    contact.first_contacted = Math.min(contact.first_contacted, time);
                    contact.last_contacted = time;
                    db.contact().updateContact(contact);
                    Log.i("Updated contact=" + contact + " type=" + type);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    public static void delete(Context context, long account, Address[] addresses, int type) {
        if (addresses == null)
            return;

        DB db = DB.getInstance(context);
        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            if (TextUtils.isEmpty(email))
                continue;
            db.contact().deleteContact(account, type, email);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("identity", identity);
        json.put("type", type);
        json.put("email", email);
        json.put("name", name);
        json.put("group", group);
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
        if (json.has("identity") && !json.isNull("identity"))
            contact.identity = json.getLong("identity");

        contact.type = json.getInt("type");
        contact.email = json.getString("email");

        if (json.has("name") && !json.isNull("name"))
            contact.name = json.getString("name");

        if (json.has("group") && !json.isNull("group"))
            contact.group = json.getString("group");

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
            return (this.account.equals(other.account) &&
                    Objects.equals(this.identity, other.identity) &&
                    this.type == other.type &&
                    this.email.equals(other.email) &&
                    Objects.equals(this.name, other.name) &&
                    Objects.equals(this.group, other.group) &&
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
