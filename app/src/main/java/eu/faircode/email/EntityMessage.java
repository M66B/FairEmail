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

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;

import javax.mail.Address;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

// https://developer.android.com/training/data-storage/room/defining-data

@Entity(
        tableName = EntityMessage.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "folder", entity = EntityFolder.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "identity", entity = EntityIdentity.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "replying", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account"}),
                @Index(value = {"folder"}),
                @Index(value = {"identity"}),
                @Index(value = {"replying"}),
                @Index(value = {"folder", "uid"}, unique = true),
                @Index(value = {"msgid", "folder"}, unique = true),
                @Index(value = {"thread"}),
                @Index(value = {"received"}),
                @Index(value = {"ui_seen"}),
                @Index(value = {"ui_hide"}),
                @Index(value = {"ui_found"})
        }
)
public class EntityMessage implements Serializable {
    static final String TABLE_NAME = "message";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long account; // performance
    @NonNull
    public Long folder;
    public Long identity;
    public Long replying;
    public Long uid; // compose = null
    public String msgid;
    public String references;
    public String deliveredto;
    public String inreplyto;
    public String thread; // compose = null
    public String avatar; // URI
    public Address[] from;
    public Address[] to;
    public Address[] cc;
    public Address[] bcc;
    public Address[] reply;
    public String headers;
    public String subject;
    public Integer size;
    @NonNull
    public Boolean content = false;
    public Long sent; // compose = null
    @NonNull
    public Long received; // compose = stored
    @NonNull
    public Long stored = new Date().getTime();
    @NonNull
    public Boolean seen;
    @NonNull
    public Boolean flagged;
    @NonNull
    public Boolean ui_seen;
    @NonNull
    public Boolean ui_flagged;
    @NonNull
    public Boolean ui_hide;
    @NonNull
    public Boolean ui_found;
    public String error;

    @Ignore
    String body = null;

    static String generateMessageId() {
        StringBuffer sb = new StringBuffer();
        sb.append('<')
                .append(Math.abs(new Random().nextInt())).append('.')
                .append(System.currentTimeMillis()).append('.')
                .append(BuildConfig.APPLICATION_ID).append("@localhost")
                .append('>');
        return sb.toString();
    }

    static File getFile(Context context, Long id) {
        File dir = new File(context.getFilesDir(), "messages");
        dir.mkdir();
        return new File(dir, id.toString());
    }

    void write(Context context, String body) throws IOException {
        File file = getFile(context, id);
        BufferedWriter out = null;
        try {
            this.body = (body == null ? "" : body);
            out = new BufferedWriter(new FileWriter(file));
            out.write(this.body);
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(Helper.TAG, e + "\n" + Log.getStackTraceString(e));
                }
        }
    }

    String read(Context context) throws IOException {
        if (body == null)
            body = read(context, this.id);
        return body;
    }

    static String read(Context context, Long id) throws IOException {
        File file = getFile(context, id);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                body.append(line);
                body.append('\n');
            }
            return body.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityMessage) {
            EntityMessage other = (EntityMessage) obj;
            return ((this.account == null ? other.account == null : this.account.equals(other.account)) &&
                    this.folder.equals(other.folder) &&
                    (this.identity == null ? other.identity == null : this.identity.equals(other.identity)) &&
                    (this.replying == null ? other.replying == null : this.replying.equals(other.replying)) &&
                    (this.uid == null ? other.uid == null : this.uid.equals(other.uid)) &&
                    (this.msgid == null ? other.msgid == null : this.msgid.equals(other.msgid)) &&
                    (this.references == null ? other.references == null : this.references.equals(other.references)) &&
                    (this.deliveredto == null ? other.deliveredto == null : this.deliveredto.equals(other.deliveredto)) &&
                    (this.inreplyto == null ? other.inreplyto == null : this.inreplyto.equals(other.inreplyto)) &&
                    (this.thread == null ? other.thread == null : this.thread.equals(other.thread)) &&
                    (this.avatar == null ? other.avatar == null : this.avatar.equals(other.avatar)) &&
                    equal(this.from, other.from) &&
                    equal(this.to, other.to) &&
                    equal(this.cc, other.cc) &&
                    equal(this.bcc, other.bcc) &&
                    equal(this.reply, other.reply) &&
                    (this.headers == null ? other.headers == null : this.headers.equals(other.headers)) &&
                    (this.subject == null ? other.subject == null : this.subject.equals(other.subject)) &&
                    (this.size == null ? other.size == null : this.size.equals(other.size)) &&
                    this.content == other.content &&
                    (this.sent == null ? other.sent == null : this.sent.equals(other.sent)) &&
                    this.received.equals(other.received) &&
                    this.stored.equals(other.stored) &&
                    this.seen.equals(other.seen) &&
                    this.flagged.equals(other.flagged) &&
                    this.ui_seen.equals(other.ui_seen) &&
                    this.ui_flagged.equals(other.ui_flagged) &&
                    this.ui_hide.equals(other.ui_hide) &&
                    this.ui_found.equals(other.ui_found) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        }
        return false;
    }

    private static boolean equal(Address[] a1, Address[] a2) {
        if (a1 == null && a2 == null)
            return true;

        if (a1 == null || a2 == null)
            return false;

        if (a1.length != a2.length)
            return false;

        for (int i = 0; i < a1.length; i++)
            if (!a1[i].toString().equals(a2[i].toString()))
                return false;

        return true;
    }
}
