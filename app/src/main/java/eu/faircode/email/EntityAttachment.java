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

import android.content.Context;

import java.io.File;

import javax.mail.Part;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityAttachment.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "message", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"message"}),
                @Index(value = {"message", "sequence"}, unique = true),
                @Index(value = {"message", "cid"}, unique = true)
        }
)
public class EntityAttachment {
    static final String TABLE_NAME = "attachment";

    static final Integer PGP_MESSAGE = 1;
    static final Integer PGP_SIGNATURE = 2;

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long message;
    @NonNull
    public Integer sequence;
    public String name;
    @NonNull
    public String type;
    public String disposition;
    public String cid; // Content-ID
    public Integer encryption;
    public Long size;
    public Integer progress;
    @NonNull
    public Boolean available = false;
    public String error;

    boolean isInline() {
        return (disposition != null && disposition.equalsIgnoreCase(Part.INLINE));
    }

    static File getFile(Context context, Long id) {
        File dir = new File(context.getFilesDir(), "attachments");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, Long.toString(id));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAttachment) {
            EntityAttachment other = (EntityAttachment) obj;
            return (this.message.equals(other.message) &&
                    this.sequence.equals(other.sequence) &&
                    (this.name == null ? other.name == null : this.name.equals(other.name)) &&
                    this.type.equals(other.type) &&
                    (this.disposition == null ? other.disposition == null : this.disposition.equals(other.disposition)) &&
                    (this.cid == null ? other.cid == null : this.cid.equals(other.cid)) &&
                    (this.encryption == null ? other.encryption == null : this.encryption.equals(other.encryption)) &&
                    (this.size == null ? other.size == null : this.size.equals(other.size)) &&
                    (this.progress == null ? other.progress == null : this.progress.equals(other.progress)) &&
                    this.available.equals(other.available));
        } else
            return false;
    }
}
