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

import android.os.Parcel;
import android.os.Parcelable;

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

public class EntityFolder implements Parcelable {
    static final String TABLE_NAME = "folder";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long account; // Outbox = null
    @NonNull
    public String name;
    @NonNull
    public String type;
    @NonNull
    public Boolean unified = false;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Integer after; // days
    public Long last_sync;
    public String state;
    public String error;

    static final String INBOX = "Inbox";
    static final String OUTBOX = "Outbox";
    static final String ARCHIVE = "All";
    static final String DRAFTS = "Drafts";
    static final String TRASH = "Trash";
    static final String JUNK = "Junk";
    static final String SENT = "Sent";
    static final String USER = "User";

    static final List<String> SYSTEM_FOLDER_ATTR = Arrays.asList(
            "All",
            "Drafts",
            "Trash",
            "Junk",
            "Sent"
    );
    static final List<String> SYSTEM_FOLDER_TYPE = Arrays.asList(
            ARCHIVE,
            DRAFTS,
            TRASH,
            JUNK,
            SENT
    ); // MUST match SYSTEM_FOLDER_ATTR

    static final List<String> FOLDER_SORT_ORDER = Arrays.asList(
            INBOX,
            OUTBOX,
            DRAFTS,
            SENT,
            ARCHIVE,
            TRASH,
            JUNK,
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityFolder) {
            EntityFolder other = (EntityFolder) obj;
            return (this.id.equals(other.id) &&
                    (this.account == null ? other.account == null : this.account.equals(other.account)) &&
                    this.name.equals(other.name) &&
                    this.type.equals(other.type) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.after.equals(other.after) &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        if (account == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(account);
        }
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeByte((byte) (synchronize == null ? 0 : synchronize ? 1 : 2));
        if (after == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(after);
        }
        parcel.writeString(state);
        parcel.writeString(error);
    }

    protected EntityFolder(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        if (in.readByte() == 0) {
            account = null;
        } else {
            account = in.readLong();
        }
        name = in.readString();
        type = in.readString();
        byte tmpSynchronize = in.readByte();
        synchronize = tmpSynchronize == 0 ? null : tmpSynchronize == 1;
        if (in.readByte() == 0) {
            after = null;
        } else {
            after = in.readInt();
        }
        state = in.readString();
        error = in.readString();
    }

    public static final Creator<EntityFolder> CREATOR = new Creator<EntityFolder>() {
        @Override
        public EntityFolder createFromParcel(Parcel in) {
            return new EntityFolder(in);
        }

        @Override
        public EntityFolder[] newArray(int size) {
            return new EntityFolder[size];
        }
    };
}
