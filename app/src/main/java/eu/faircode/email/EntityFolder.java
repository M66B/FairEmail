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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityFolder.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account", "name"}, unique = true),
                @Index(value = {"account"}),
                @Index(value = {"name"}),
                @Index(value = {"type"}),
                @Index(value = {"unified"})
        }
)

public class EntityFolder implements Serializable {
    static final String TABLE_NAME = "folder";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long account; // Outbox = null
    @NonNull
    public String name;
    @NonNull
    public String type;
    @NonNull
    public Boolean synchronize;
    public Integer poll_interval; // obsolete
    @NonNull
    public Integer after; // days
    public String display;
    @NonNull
    public Boolean hide = false;
    @NonNull
    public Boolean unified = false;
    public String state;
    public String error;

    static final String INBOX = "Inbox";
    static final String OUTBOX = "Outbox";
    static final String ARCHIVE = "All";
    static final String DRAFTS = "Drafts";
    static final String TRASH = "Trash";
    static final String JUNK = "Junk";
    static final String SENT = "Sent";
    static final String SYSTEM = "System";
    static final String USER = "User";

    static final List<String> SYSTEM_FOLDER_ATTR = Arrays.asList(
            "All",
            "Drafts",
            "Trash",
            "Junk",
            "Sent",
            "Important",
            "Flagged"
    );
    static final List<String> SYSTEM_FOLDER_TYPE = Arrays.asList(
            ARCHIVE,
            DRAFTS,
            TRASH,
            JUNK,
            SENT,
            SYSTEM,
            SYSTEM
    ); // MUST match SYSTEM_FOLDER_ATTR

    static final List<String> FOLDER_SORT_ORDER = Arrays.asList(
            INBOX,
            OUTBOX,
            DRAFTS,
            SENT,
            ARCHIVE,
            TRASH,
            JUNK,
            SYSTEM,
            USER
    );

    static final int DEFAULT_INBOX_SYNC = 30; // days
    static final int DEFAULT_SYSTEM_SYNC = 7; // days
    static final int DEFAULT_USER_SYNC = 7; // days

    static final List<String> SYSTEM_FOLDER_SYNC = Arrays.asList(
            ARCHIVE,
            DRAFTS,
            TRASH,
            SENT
    );

    public EntityFolder() {
    }

    boolean isOutgoing() {
        return isOutgoing(this.type);
    }

    static boolean isOutgoing(String type) {
        return DRAFTS.equals(type) || OUTBOX.equals(type) || SENT.equals(type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityFolder) {
            EntityFolder other = (EntityFolder) obj;
            return (this.id.equals(other.id) &&
                    (this.account == null ? other.account == null : this.account.equals(other.account)) &&
                    this.name.equals(other.name) &&
                    this.type.equals(other.type) &&
                    this.synchronize.equals(other.synchronize) &&
                    (this.poll_interval == null ? other.poll_interval == null : this.poll_interval.equals(other.poll_interval)) && this.after.equals(other.after) &&
                    (this.display == null ? other.display == null : this.display.equals(other.display)) &&
                    this.hide == other.hide &&
                    this.unified == other.unified &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("type", type);
        json.put("synchronize", synchronize);
        json.put("poll_interval", poll_interval);
        json.put("after", after);
        json.put("display", display);
        json.put("hide", hide);
        json.put("unified", unified);
        return json;
    }

    public static EntityFolder fromJSON(JSONObject json) throws JSONException {
        EntityFolder folder = new EntityFolder();
        folder.name = json.getString("name");
        folder.type = json.getString("type");
        folder.synchronize = json.getBoolean("synchronize");
        if (json.has("poll_interval"))
            folder.poll_interval = json.getInt("poll_interval");
        folder.after = json.getInt("after");
        if (json.has("display"))
            folder.display = json.getString("display");
        if (json.has("hide"))
            folder.hide = json.getBoolean("hide");
        folder.unified = json.getBoolean("unified");
        return folder;
    }
}
