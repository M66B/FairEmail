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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.File;
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
import static androidx.room.ForeignKey.SET_NULL;

// https://developer.android.com/training/data-storage/room/defining-data

@Entity(
        tableName = EntityMessage.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "folder", entity = EntityFolder.class, parentColumns = "id", onDelete = CASCADE),
                @ForeignKey(childColumns = "identity", entity = EntityIdentity.class, parentColumns = "id", onDelete = SET_NULL),
                @ForeignKey(childColumns = "replying", entity = EntityMessage.class, parentColumns = "id", onDelete = SET_NULL),
                @ForeignKey(childColumns = "forwarding", entity = EntityMessage.class, parentColumns = "id", onDelete = SET_NULL)
        },
        indices = {
                @Index(value = {"account"}),
                @Index(value = {"folder"}),
                @Index(value = {"identity"}),
                @Index(value = {"replying"}),
                @Index(value = {"forwarding"}),
                @Index(value = {"folder", "uid"}, unique = true),
                @Index(value = {"msgid", "folder"}, unique = true),
                @Index(value = {"thread"}),
                @Index(value = {"sender"}),
                @Index(value = {"received"}),
                @Index(value = {"ui_seen"}),
                @Index(value = {"ui_flagged"}),
                @Index(value = {"ui_hide"}),
                @Index(value = {"ui_found"}),
                @Index(value = {"ui_ignored"}),
                @Index(value = {"ui_browsed"}),
                @Index(value = {"ui_snoozed"})
        }
)
public class EntityMessage implements Serializable {
    static final String TABLE_NAME = "message";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long account; // performance
    @NonNull
    public Long folder;
    public Long identity;
    public String extra; // plus
    public Long replying; // obsolete
    public Long forwarding; // obsolete
    public Long uid; // compose/moved = null
    public String msgid;
    public String references;
    public String deliveredto;
    public String inreplyto;
    public String thread; // compose = null
    public String avatar; // obsolete
    public String sender; // sort key
    public Address[] from;
    public Address[] to;
    public Address[] cc;
    public Address[] bcc;
    public Address[] reply;
    public String headers;
    public Boolean raw;
    public String subject;
    public Integer size;
    @NonNull
    public Boolean content = false;
    public String preview;
    public Long sent; // compose = null
    @NonNull
    public Long received; // compose = stored
    @NonNull
    public Long stored = new Date().getTime();
    @NonNull
    public Boolean seen = false;
    @NonNull
    public Boolean answered = false;
    @NonNull
    public Boolean flagged = false;
    public String flags; // system flags
    public String[] keywords; // user flags
    @NonNull
    public Boolean ui_seen = false;
    @NonNull
    public Boolean ui_answered = false;
    @NonNull
    public Boolean ui_flagged = false;
    @NonNull
    public Boolean ui_hide = false;
    @NonNull
    public Boolean ui_found = false;
    @NonNull
    public Boolean ui_ignored = false;
    @NonNull
    public Boolean ui_browsed = false;
    public Long ui_snoozed;
    public String warning; // persistent
    public String error; // volatile
    public Long last_attempt; // send

    @Ignore
    boolean day = false;

    static String generateMessageId() {
        StringBuilder sb = new StringBuilder();
        sb.append('<')
                .append(Math.abs(new Random().nextInt())).append('.')
                .append(System.currentTimeMillis()).append('.')
                .append(BuildConfig.APPLICATION_ID).append("@localhost")
                .append('>');
        return sb.toString();
    }

    static File getFile(Context context, Long id) {
        File dir = new File(context.getFilesDir(), "messages");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, id.toString());
    }

    static File getRefFile(Context context, Long id) {
        File dir = new File(context.getFilesDir(), "references");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, id.toString());
    }

    static File getRawFile(Context context, Long id) {
        File dir = new File(context.getFilesDir(), "raw");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, Long.toString(id));
    }

    static void snooze(Context context, long id, Long wakeup) {
        Intent snoozed = new Intent(context, ServiceSynchronize.class);
        snoozed.setAction("snooze:" + id);
        PendingIntent pi = PendingIntent.getService(context, ServiceSynchronize.PI_SNOOZED, snoozed, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (wakeup == null) {
            Log.i("Cancel snooze id=" + id);
            am.cancel(pi);
        } else {
            Log.i("Set snooze id=" + id + " wakeup=" + new Date(wakeup));
            am.set(AlarmManager.RTC_WAKEUP, wakeup, pi);
        }
    }

    public boolean uiEquals(Object obj) {
        if (obj instanceof EntityMessage) {
            EntityMessage other = (EntityMessage) obj;
            return (true &&
                    //(this.account == null ? other.account == null : this.account.equals(other.account)) &&
                    //this.folder.equals(other.folder) &&
                    //(this.identity == null ? other.identity == null : this.identity.equals(other.identity)) &&
                    //(this.replying == null ? other.replying == null : this.replying.equals(other.replying)) &&
                    //(this.forwarding == null ? other.forwarding == null : this.forwarding.equals(other.forwarding)) &&
                    (this.uid == null ? other.uid == null : this.uid.equals(other.uid)) &&
                    (this.msgid == null ? other.msgid == null : this.msgid.equals(other.msgid)) && // debug info
                    //(this.references == null ? other.references == null : this.references.equals(other.references)) &&
                    //(this.deliveredto == null ? other.deliveredto == null : this.deliveredto.equals(other.deliveredto)) &&
                    //(this.inreplyto == null ? other.inreplyto == null : this.inreplyto.equals(other.inreplyto)) &&
                    (this.thread == null ? other.thread == null : this.thread.equals(other.thread)) &&
                    MessageHelper.equal(this.from, other.from) &&
                    MessageHelper.equal(this.to, other.to) &&
                    MessageHelper.equal(this.cc, other.cc) &&
                    MessageHelper.equal(this.bcc, other.bcc) &&
                    MessageHelper.equal(this.reply, other.reply) &&
                    (this.headers == null ? other.headers == null : this.headers.equals(other.headers)) &&
                    (this.raw == null ? other.raw == null : this.raw.equals(other.raw)) &&
                    (this.subject == null ? other.subject == null : this.subject.equals(other.subject)) &&
                    (this.size == null ? other.size == null : this.size.equals(other.size)) &&
                    this.content == other.content &&
                    (this.preview == null ? other.preview == null : this.preview.equals(other.preview)) &&
                    //(this.sent == null ? other.sent == null : this.sent.equals(other.sent)) &&
                    this.received.equals(other.received) &&
                    this.stored.equals(other.stored) && // updated after decryption
                    //this.seen.equals(other.seen) &&
                    //this.answered.equals(other.answered) &&
                    //this.flagged.equals(other.flagged) &&
                    (!BuildConfig.DEBUG || (this.flags == null ? other.flags == null : this.flags.equals(other.flags))) &&
                    Helper.equal(this.keywords, other.keywords) &&
                    this.ui_seen.equals(other.ui_seen) &&
                    this.ui_answered.equals(other.ui_answered) &&
                    this.ui_flagged.equals(other.ui_flagged) &&
                    this.ui_hide.equals(other.ui_hide) &&
                    this.ui_found.equals(other.ui_found) &&
                    this.ui_ignored.equals(other.ui_ignored) &&
                    this.ui_browsed.equals(other.ui_browsed) &&
                    (this.ui_snoozed == null ? other.ui_snoozed == null : this.ui_snoozed.equals(other.ui_snoozed)) &&
                    (this.warning == null ? other.warning == null : this.warning.equals(other.warning)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)) &&
                    this.day == other.day);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityMessage) {
            EntityMessage other = (EntityMessage) obj;
            return ((this.account == null ? other.account == null : this.account.equals(other.account)) &&
                    this.folder.equals(other.folder) &&
                    (this.identity == null ? other.identity == null : this.identity.equals(other.identity)) &&
                    (this.uid == null ? other.uid == null : this.uid.equals(other.uid)) &&
                    (this.msgid == null ? other.msgid == null : this.msgid.equals(other.msgid)) &&
                    (this.references == null ? other.references == null : this.references.equals(other.references)) &&
                    (this.deliveredto == null ? other.deliveredto == null : this.deliveredto.equals(other.deliveredto)) &&
                    (this.inreplyto == null ? other.inreplyto == null : this.inreplyto.equals(other.inreplyto)) &&
                    (this.thread == null ? other.thread == null : this.thread.equals(other.thread)) &&
                    MessageHelper.equal(this.from, other.from) &&
                    MessageHelper.equal(this.to, other.to) &&
                    MessageHelper.equal(this.cc, other.cc) &&
                    MessageHelper.equal(this.bcc, other.bcc) &&
                    MessageHelper.equal(this.reply, other.reply) &&
                    (this.headers == null ? other.headers == null : this.headers.equals(other.headers)) &&
                    (this.raw == null ? other.raw == null : this.raw.equals(other.raw)) &&
                    (this.subject == null ? other.subject == null : this.subject.equals(other.subject)) &&
                    (this.size == null ? other.size == null : this.size.equals(other.size)) &&
                    this.content == other.content &&
                    (this.preview == null ? other.preview == null : this.preview.equals(other.preview)) &&
                    (this.sent == null ? other.sent == null : this.sent.equals(other.sent)) &&
                    this.received.equals(other.received) &&
                    this.stored.equals(other.stored) &&
                    this.seen.equals(other.seen) &&
                    this.answered.equals(other.answered) &&
                    this.flagged.equals(other.flagged) &&
                    (this.flags == null ? other.flags == null : this.flags.equals(other.flags)) &&
                    Helper.equal(this.keywords, other.keywords) &&
                    this.ui_seen.equals(other.ui_seen) &&
                    this.ui_answered.equals(other.ui_answered) &&
                    this.ui_flagged.equals(other.ui_flagged) &&
                    this.ui_hide.equals(other.ui_hide) &&
                    this.ui_found.equals(other.ui_found) &&
                    this.ui_ignored.equals(other.ui_ignored) &&
                    this.ui_browsed.equals(other.ui_browsed) &&
                    (this.ui_snoozed == null ? other.ui_snoozed == null : this.ui_snoozed.equals(other.ui_snoozed)) &&
                    (this.warning == null ? other.warning == null : this.warning.equals(other.warning)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        }
        return false;
    }
}
