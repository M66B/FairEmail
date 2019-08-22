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

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

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
                @Index(value = {"folder", "uid"}, unique = true),
                @Index(value = {"msgid"}),
                @Index(value = {"thread"}),
                @Index(value = {"sender"}),
                @Index(value = {"received"}),
                @Index(value = {"subject"}),
                @Index(value = {"ui_seen"}),
                @Index(value = {"ui_flagged"}),
                @Index(value = {"ui_hide"}),
                @Index(value = {"ui_found"}),
                @Index(value = {"ui_ignored"}),
                @Index(value = {"ui_browsed"}),
                @Index(value = {"ui_snoozed"})
        }
)
public class EntityMessage extends DownloadMessage implements Serializable {
    static final String TABLE_NAME = "message";

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
    public Boolean receipt_request;
    public Address[] receipt_to;
    public Boolean dkim;
    public Boolean spf;
    public Boolean dmarc;
    public Boolean mx = null;
    public String avatar; // lookup URI from sender
    public String sender; // sort key
    public Address[] from;
    public Address[] to;
    public Address[] cc;
    public Address[] bcc;
    public Address[] reply;
    public Address[] list_post;
    public String headers;
    public Boolean raw;
    public String subject;
    @NonNull
    public Integer attachments = 0; // performance
    public Boolean plain_only = null;
    public Boolean encrypt = null;
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
    public Integer notifying = 0;
    @NonNull
    public Boolean ui_seen = false;
    @NonNull
    public Boolean ui_answered = false;
    @NonNull
    public Boolean ui_flagged = false;
    @NonNull
    public Long ui_hide = 0L;
    @NonNull
    public Boolean ui_found = false;
    @NonNull
    public Boolean ui_ignored = false;
    @NonNull
    public Boolean ui_browsed = false;
    public Long ui_snoozed;
    public Integer color;
    public Integer revision; // compose
    public Integer revisions; // compose
    public String error; // volatile
    public Long last_attempt; // send

    static String generateMessageId() {
        StringBuilder sb = new StringBuilder();
        sb.append('<')
                .append(Math.abs(new Random().nextInt())).append('.')
                .append(System.currentTimeMillis()).append('.')
                .append(BuildConfig.APPLICATION_ID).append("@localhost")
                .append('>');
        return sb.toString();
    }

    boolean replySelf(String via) {
        Address[] replying = (reply == null || reply.length == 0 ? from : reply);
        if (replying == null || replying.length != 1)
            return false;

        String recipient = MessageHelper.canonicalAddress(((InternetAddress) replying[0]).getAddress());
        return recipient.equals(via);
    }

    Address[] getAllRecipients(String via) {
        List<Address> addresses = new ArrayList<>();

        if (!replySelf(via) && to != null)
            addresses.addAll(Arrays.asList(to));

        if (cc != null)
            addresses.addAll(Arrays.asList(cc));

        // Filter self
        for (Address address : new ArrayList<>(addresses)) {
            String recipient = MessageHelper.canonicalAddress(((InternetAddress) address).getAddress());
            if (recipient.equals(via))
                addresses.remove(address);
        }

        return addresses.toArray(new Address[0]);
    }

    File getFile(Context context, int revision) {
        File dir = new File(context.getFilesDir(), "revision");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, id + "." + revision);
    }

    File getRefFile(Context context) {
        File dir = new File(context.getFilesDir(), "references");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, id.toString());
    }

    File getRawFile(Context context) {
        File dir = new File(context.getFilesDir(), "raw");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, Long.toString(id));
    }

    static void snooze(Context context, long id, Long wakeup) {
        Intent snoozed = new Intent(context, ServiceUI.class);
        snoozed.setAction("snooze:" + id);
        PendingIntent pi = PendingIntent.getService(context, ServiceUI.PI_SNOOZED, snoozed, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (wakeup == null) {
            Log.i("Cancel snooze id=" + id);
            am.cancel(pi);
        } else {
            Log.i("Set snooze id=" + id + " wakeup=" + new Date(wakeup));
            am.set(AlarmManager.RTC_WAKEUP, wakeup, pi);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityMessage) {
            EntityMessage other = (EntityMessage) obj;
            return (Objects.equals(this.account, other.account) &&
                    this.folder.equals(other.folder) &&
                    Objects.equals(this.identity, other.identity) &&
                    Objects.equals(this.uid, other.uid) &&
                    Objects.equals(this.msgid, other.msgid) &&
                    Objects.equals(this.references, other.references) &&
                    Objects.equals(this.deliveredto, other.deliveredto) &&
                    Objects.equals(this.inreplyto, other.inreplyto) &&
                    Objects.equals(this.thread, other.thread) &&
                    Objects.equals(this.receipt_request, other.receipt_request) &&
                    MessageHelper.equal(this.receipt_to, other.receipt_to) &&
                    Objects.equals(this.dkim, other.dkim) &&
                    Objects.equals(this.spf, other.spf) &&
                    Objects.equals(this.dmarc, other.dmarc) &&
                    Objects.equals(this.mx, other.mx) &&
                    Objects.equals(this.avatar, other.avatar) &&
                    Objects.equals(this.sender, other.sender) &&
                    MessageHelper.equal(this.from, other.from) &&
                    MessageHelper.equal(this.to, other.to) &&
                    MessageHelper.equal(this.cc, other.cc) &&
                    MessageHelper.equal(this.bcc, other.bcc) &&
                    MessageHelper.equal(this.reply, other.reply) &&
                    MessageHelper.equal(this.list_post, other.list_post) &&
                    Objects.equals(this.headers, other.headers) &&
                    Objects.equals(this.raw, other.raw) &&
                    Objects.equals(this.subject, other.subject) &&
                    Objects.equals(this.size, other.size) &&
                    Objects.equals(this.attachments, other.attachments) &&
                    this.content == other.content &&
                    Objects.equals(this.plain_only, other.plain_only) &&
                    Objects.equals(this.preview, other.preview) &&
                    Objects.equals(this.sent, other.sent) &&
                    this.received.equals(other.received) &&
                    this.stored.equals(other.stored) &&
                    this.seen.equals(other.seen) &&
                    this.answered.equals(other.answered) &&
                    this.flagged.equals(other.flagged) &&
                    Objects.equals(this.flags, other.flags) &&
                    Helper.equal(this.keywords, other.keywords) &&
                    this.notifying.equals(other.notifying) &&
                    this.ui_seen.equals(other.ui_seen) &&
                    this.ui_answered.equals(other.ui_answered) &&
                    this.ui_flagged.equals(other.ui_flagged) &&
                    this.ui_hide.equals(other.ui_hide) &&
                    this.ui_found.equals(other.ui_found) &&
                    this.ui_ignored.equals(other.ui_ignored) &&
                    this.ui_browsed.equals(other.ui_browsed) &&
                    Objects.equals(this.ui_snoozed, other.ui_snoozed) &&
                    Objects.equals(this.color, other.color) &&
                    Objects.equals(this.revision, other.revision) &&
                    Objects.equals(this.revisions, other.revisions) &&
                    Objects.equals(this.warning, other.warning) &&
                    Objects.equals(this.error, other.error) &&
                    Objects.equals(this.last_attempt, other.last_attempt));
        }
        return false;
    }
}
