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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
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
    static final int ATTACHMENT_BUFFER_SIZE = 8192; // bytes

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long message;
    @NonNull
    public Integer sequence;
    public String name;
    @NonNull
    public String type;
    public String cid; // Content-ID
    public Integer size;

    public Integer progress;
    @NonNull
    public Boolean available = false;

    @Ignore
    BodyPart part;

    static File getFile(Context context, Long id) {
        File dir = new File(context.getFilesDir(), "attachments");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, Long.toString(id));
    }

    void write(Context context, String body) throws IOException {
        File file = getFile(context, id);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            out.write(body == null ? "" : body);
        } finally {
            if (out != null)
                out.close();
        }
    }

    void download(Context context, DB db) throws MessagingException, IOException {
        // Build filename
        File file = EntityAttachment.getFile(context, this.id);

        // Download attachment
        InputStream is = null;
        OutputStream os = null;
        try {
            this.progress = null;
            db.attachment().updateAttachment(this);

            is = this.part.getInputStream();
            os = new BufferedOutputStream(new FileOutputStream(file));

            int size = 0;
            byte[] buffer = new byte[ATTACHMENT_BUFFER_SIZE];
            for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                size += len;
                os.write(buffer, 0, len);

                // Update progress
                if (this.size != null)
                    db.attachment().setProgress(this.id, size * 100 / this.size);
            }

            // Store attachment data
            this.size = size;
            this.progress = null;
            this.available = true;
            db.attachment().updateAttachment(this);

            Log.i("Downloaded attachment size=" + this.size);
        } catch (IOException ex) {
            // Reset progress on failure
            this.progress = null;
            db.attachment().updateAttachment(this);
            throw ex;
        } finally {
            try {
                if (is != null)
                    is.close();
            } finally {
                if (os != null)
                    os.close();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAttachment) {
            EntityAttachment other = (EntityAttachment) obj;
            return (this.message.equals(other.message) &&
                    this.sequence.equals(other.sequence) &&
                    (this.name == null ? other.name == null : this.name.equals(other.name)) &&
                    this.type.equals(other.type) &&
                    (this.size == null ? other.size == null : this.size.equals(other.size)) &&
                    (this.progress == null ? other.progress == null : this.progress.equals(other.progress)) &&
                    this.available.equals(other.available));
        } else
            return false;
    }

}
