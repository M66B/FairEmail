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
import static androidx.room.ForeignKey.SET_NULL;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

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
                @Index(value = {"inreplyto"}),
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
public class EntityMessage implements Serializable {
    static final String TABLE_NAME = "message";

    static final int NOTIFYING_IGNORE = -2;

    static final Integer ENCRYPT_NONE = 0;
    static final Integer PGP_SIGNENCRYPT = 1;
    static final Integer PGP_SIGNONLY = 2;
    static final Integer SMIME_SIGNENCRYPT = 3;
    static final Integer SMIME_SIGNONLY = 4;
    static final Integer PGP_ENCRYPTONLY = 5;
    static final Integer SMIME_ENCRYPTONLY = 6;

    static final Integer PRIORITIY_LOW = 0;
    static final Integer PRIORITIY_NORMAL = 1;
    static final Integer PRIORITIY_HIGH = 2;

    static final Integer SENSITIVITY_PERSONAL = 1;
    static final Integer SENSITIVITY_PRIVATE = 2;
    static final Integer SENSITIVITY_CONFIDENTIAL = 3;

    static final Integer DSN_NONE = 0;
    static final Integer DSN_RECEIPT = 1;
    static final Integer DSN_HARD_BOUNCE = 2;

    static final Long SWIPE_ACTION_ASK = -1L;
    static final Long SWIPE_ACTION_SEEN = -2L;
    static final Long SWIPE_ACTION_SNOOZE = -3L;
    static final Long SWIPE_ACTION_HIDE = -4L;
    static final Long SWIPE_ACTION_MOVE = -5L;
    static final Long SWIPE_ACTION_FLAG = -6L;
    static final Long SWIPE_ACTION_DELETE = -7L;
    static final Long SWIPE_ACTION_JUNK = -8L;
    static final Long SWIPE_ACTION_REPLY = -9L;
    static final Long SWIPE_ACTION_IMPORTANCE = -10L;
    static final Long SWIPE_ACTION_SUMMARIZE = -11L;
    static final Long SWIPE_ACTION_TTS = -12L;

    private static final int MAX_SNOOZED = 300;

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
    public String uidl; // POP3
    public String msgid;
    public String hash; // headers hash
    public String references;
    public String deliveredto;
    public String inreplyto;
    public String wasforwardedfrom;
    public String thread; // compose = null
    public Integer priority;
    public Integer importance;
    public Integer sensitivity;
    public Boolean auto_submitted;
    @ColumnInfo(name = "receipt")
    public Integer dsn;
    public Boolean receipt_request;
    public Address[] receipt_to;
    public String bimi_selector;
    public String signedby;
    public Boolean tls;
    public Boolean dkim;
    public Boolean spf;
    public Boolean dmarc;
    public Boolean auth; // SMTP
    public Boolean mx;
    public Boolean blocklist;
    public Boolean from_domain; // spf/smtp.mailfrom <> from
    public Boolean reply_domain; // reply-to <> from
    public String avatar; // lookup URI from sender
    public String sender; // sort key: from email address
    public Address[] return_path;
    public Address[] smtp_from;
    public Address[] submitter; // sent on behalf of
    public Address[] from;
    public Address[] to;
    public Address[] cc;
    public Address[] bcc;
    public Address[] reply;
    public Address[] list_post;
    public String unsubscribe;
    public String autocrypt;
    public String headers;
    public String infrastructure;
    public Boolean raw;
    public String subject;
    public Long size;
    public Long total;
    @NonNull
    public Integer attachments = 0; // performance
    @NonNull
    public Boolean content = false;
    public String language = null; // classified
    public Integer plain_only = null; // 1=true; 0x80=alt
    public Boolean write_below;
    public Boolean resend = null;
    public Integer encrypt = null;
    public Integer ui_encrypt = null;
    @NonNull
    public Boolean verified = false;
    public String preview;
    public String notes;
    public Integer notes_color;
    @NonNull
    public Boolean signature = true;
    public Long sent; // compose = null
    @NonNull
    public Long received; // compose = stored
    @NonNull
    public Long stored = new Date().getTime();
    @NonNull
    public Boolean recent = false;
    @NonNull
    public Boolean seen = false;
    @NonNull
    public Boolean answered = false;
    @NonNull
    public Boolean flagged = false;
    @NonNull
    public Boolean deleted = false;
    public String flags; // system flags
    public String[] keywords; // user flags
    public String[] labels; // Gmail
    @NonNull
    public Boolean fts = false;
    @NonNull
    public Boolean auto_classified = false;
    @NonNull
    public Integer notifying = 0;
    @NonNull
    public Boolean ui_seen = false;
    @NonNull
    public Boolean ui_answered = false;
    @NonNull
    public Boolean ui_flagged = false;
    @NonNull
    public Boolean ui_deleted = false;
    @NonNull
    public Boolean ui_hide = false;
    @NonNull
    public Boolean ui_found = false;
    @NonNull
    public Boolean ui_ignored = false;
    @NonNull
    public Boolean ui_silent = false;
    @NonNull
    public Boolean ui_local_only = false;
    @NonNull
    public Boolean ui_browsed = false;
    public Long ui_busy;
    public Long ui_snoozed;
    @NonNull
    public Boolean ui_unsnoozed = false;
    @NonNull
    public Boolean show_images = false;
    @NonNull
    public Boolean show_full = false;
    public Integer color;
    public Integer revision; // compose
    public Integer revisions; // compose
    public String warning; // persistent
    public String error; // volatile
    public Long last_attempt; // send
    public Long last_touched;

    static String generateMessageId() {
        return generateMessageId("localhost");
    }

    static String generateMessageId(String domain) {
        // https://www.jwz.org/doc/mid.html
        // https://tools.ietf.org/html/rfc2822.html#section-3.6.4
        return "<" + UUID.randomUUID() + "@" + domain + '>';
    }

    String getLink() {
        // adb shell pm set-app-links --package eu.faircode.email 0 all
        // adb shell pm verify-app-links --re-verify eu.faircode.email
        // adb shell pm get-app-links eu.faircode.email
        // https://link.fairemail.net/.well-known/assetlinks.json
        return "https://link.fairemail.net/#" + id;
    }

    boolean isPlainOnly() {
        return (this.plain_only != null && (this.plain_only & 1) != 0);
    }

    boolean hasAlt() {
        return (this.plain_only != null && (this.plain_only & 0x80) != 0);
    }

    boolean fromSelf(EntityIdentity identity) {
        if (from != null && identity != null)
            for (Address sender : from)
                if (identity.self &&
                        (identity.sameAddress(sender) || identity.similarAddress(sender)))
                    return true;
        return false;
    }

    boolean fromSelf(List<TupleIdentityEx> identities) {
        List<Address> senders = new ArrayList<>();
        if (from != null)
            senders.addAll(Arrays.asList(from));
        //if (reply != null)
        //    senders.addAll(Arrays.asList(reply));

        if (identities != null)
            for (TupleIdentityEx identity : identities)
                for (Address sender : senders)
                    if (identity.self && identity.similarAddress(sender))
                        return true;

        return false;
    }

    boolean replySelf(List<TupleIdentityEx> identities, long account) {
        Address[] senders = (reply == null || reply.length == 0 ? from : reply);
        if (identities != null && senders != null)
            for (Address sender : senders)
                for (TupleIdentityEx identity : identities)
                    if (identity.account == account &&
                            identity.self &&
                            identity.similarAddress(sender))
                        return true;

        return false;
    }

    Address[] getAllRecipients(List<TupleIdentityEx> identities, long account) {
        List<Address> addresses = new ArrayList<>();

        if (!replySelf(identities, account)) {
            if (to != null)
                addresses.addAll(Arrays.asList(to));
        }

        if (cc != null)
            addresses.addAll(Arrays.asList(cc));

        // Filter from
        if (from != null)
            for (Address address : new ArrayList<>(addresses))
                for (Address f : from)
                    if (MessageHelper.equalEmail(address, f)) {
                        addresses.remove(address);
                        break;
                    }

        // Filter self
        if (identities != null)
            for (Address address : new ArrayList<>(addresses))
                for (TupleIdentityEx identity : identities)
                    if (identity.account == account &&
                            identity.self &&
                            identity.similarAddress(address))
                        addresses.remove(address);

        return addresses.toArray(new Address[0]);
    }

    List<Address> getAllRecipients() {
        List<Address> recipients = new ArrayList<>();
        if (to != null)
            recipients.addAll(Arrays.asList(to));
        if (cc != null)
            recipients.addAll(Arrays.asList(cc));
        if (bcc != null)
            recipients.addAll(Arrays.asList(bcc));
        return recipients;
    }

    String getRemark() {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageHelper.formatAddresses(from));
        if (!TextUtils.isEmpty(subject)) {
            if (sb.length() > 0)
                sb.append('\n');
            sb.append(subject);
        }
        return sb.toString();
    }

    boolean hasKeyword(@NonNull String value) {
        // https://tools.ietf.org/html/rfc5788
        if (keywords == null)
            return false;

        for (String keyword : keywords)
            if (value.equalsIgnoreCase(keyword))
                return true;

        return false;
    }

    boolean isForwarded() {
        return hasKeyword(MessageHelper.FLAG_FORWARDED);
    }

    boolean isFiltered() {
        return hasKeyword(MessageHelper.FLAG_FILTERED);
    }

    boolean isSigned() {
        return (EntityMessage.PGP_SIGNONLY.equals(ui_encrypt) ||
                EntityMessage.SMIME_SIGNONLY.equals(ui_encrypt));
    }

    boolean isEncrypted() {
        return (EntityMessage.PGP_SIGNENCRYPT.equals(ui_encrypt) ||
                EntityMessage.SMIME_SIGNENCRYPT.equals(ui_encrypt));
    }

    boolean isVerifiable() {
        return (EntityMessage.PGP_SIGNONLY.equals(encrypt) ||
                EntityMessage.SMIME_SIGNONLY.equals(encrypt));
    }

    boolean isUnlocked() {
        return (EntityMessage.PGP_SIGNENCRYPT.equals(ui_encrypt) &&
                !EntityMessage.PGP_SIGNENCRYPT.equals(encrypt)) ||
                (EntityMessage.SMIME_SIGNENCRYPT.equals(ui_encrypt) &&
                        !EntityMessage.SMIME_SIGNENCRYPT.equals(encrypt));
    }

    boolean isNotJunk(Context context) {
        DB db = DB.getInstance(context);

        boolean notJunk = false;
        if (from != null)
            for (Address sender : from) {
                String email = ((InternetAddress) sender).getAddress();
                if (TextUtils.isEmpty(email))
                    continue;

                EntityContact contact = db.contact().getContact(account, EntityContact.TYPE_NO_JUNK, email);
                if (contact != null) {
                    contact.times_contacted++;
                    contact.last_contacted = new Date().getTime();
                    db.contact().updateContact(contact);
                    notJunk = true;
                }
            }
        return notJunk;
    }

    boolean isForwarder() {
        if (from == null || from.length != 1)
            return false;
        if (submitter == null || submitter.length != 1)
            return false;
        String email = ((InternetAddress) from[0]).getAddress();
        String domain = UriHelper.getEmailDomain(email);
        if (TextUtils.isEmpty(domain))
            return false;
        return "duck.com".equals(domain) ||
                "simplelogin.co".equals(domain) ||
                "anonaddy.me".equals(domain) ||
                domain.endsWith(".mozmail.com");
    }

    String[] checkFromDomain(Context context) {
        return MessageHelper.equalRootDomain(context, from, smtp_from);
    }

    String[] checkReplyDomain(Context context) {
        return MessageHelper.equalRootDomain(context, reply, from);
    }

    static String getSubject(Context context, String language, String subject, boolean forward) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean prefix_once = prefs.getBoolean("prefix_once", true);
        boolean prefix_count = prefs.getBoolean("prefix_count", false);
        boolean alt = prefs.getBoolean(forward ? "alt_fwd" : "alt_re", false);

        if (subject == null)
            subject = "";

        int resid = forward
                ? (alt ? R.string.title_subject_forward_alt : R.string.title_subject_forward)
                : (alt ? R.string.title_subject_reply_alt : R.string.title_subject_reply);

        if (!prefix_once)
            return Helper.getString(context, language, resid, subject);

        List<Pair<String, Boolean>> prefixes = new ArrayList<>();
        for (String re : Helper.getStrings(context, language, R.string.title_subject_reply, ""))
            prefixes.add(new Pair<>(re, false));
        for (String re : Helper.getStrings(context, language, R.string.title_subject_reply_alt, ""))
            prefixes.add(new Pair<>(re, false));
        for (String fwd : Helper.getStrings(context, language, R.string.title_subject_forward, ""))
            prefixes.add(new Pair<>(fwd, true));
        for (String fwd : Helper.getStrings(context, language, R.string.title_subject_forward_alt, ""))
            prefixes.add(new Pair<>(fwd, true));

        int replies = 0;
        boolean re = !forward;
        List<Boolean> scanned = new ArrayList<>();
        String subj = subject.trim();
        while (true) {
            boolean found = false;
            for (Pair<String, Boolean> prefix : prefixes) {
                Matcher m = getPattern(prefix.first.trim()).matcher(subj);
                if (m.matches()) {
                    found = true;
                    subj = m.group(m.groupCount()).trim();

                    re = (re && !prefix.second);
                    if (re)
                        if (prefix.first.trim().endsWith(":"))
                            try {
                                String n = m.group(2);
                                if (n == null)
                                    replies++;
                                else
                                    replies += Integer.parseInt(n.substring(1, n.length() - 1));
                            } catch (NumberFormatException ex) {
                                Log.e(ex);
                                replies++;
                            }
                        else
                            replies++;

                    int count = scanned.size();
                    if (!prefix.second.equals(count == 0 ? forward : scanned.get(count - 1)))
                        scanned.add(prefix.second);

                    break;
                }
            }
            if (!found)
                break;
        }

        String pre = Helper.getString(context, language, resid, "");
        int semi = pre.lastIndexOf(':');
        if (prefix_count && replies > 0 && semi > 0)
            pre = pre.substring(0, semi) + "[" + (replies + 1) + "]" + pre.substring(semi);

        StringBuilder result = new StringBuilder(pre);
        for (int i = 0; i < scanned.size(); i++)
            result.append(context.getString(scanned.get(i) ? R.string.title_subject_forward : R.string.title_subject_reply, ""));
        result.append(subj);

        return result.toString();
    }

    private static Pattern getPattern(String prefix) {
        String pat = prefix.endsWith(":")
                ? "(^" + Pattern.quote(prefix.substring(0, prefix.length() - 1)) + ")" + "((\\[\\d+\\])|(\\(\\d+\\)))?" + ":"
                : "(^" + Pattern.quote(prefix) + ")";
        return Pattern.compile(pat + "(\\s*)(.*)", Pattern.CASE_INSENSITIVE);
    }

    Element getReplyHeader(Context context, Document document, boolean separate, boolean extended) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hide_timezone = prefs.getBoolean("hide_timezone", false);
        String template_reply = prefs.getString("template_reply", null);
        boolean language_detection = prefs.getBoolean("language_detection", false);
        String compose_font = prefs.getString("compose_font", "");
        String l = (language_detection ? language : null);

        DateFormat DTF = (hide_timezone
                ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                : Helper.getDateTimeInstance(context));
        DTF.setTimeZone(hide_timezone ? TimeZone.getTimeZone("UTC") : TimeZone.getDefault());
        String date = (received instanceof Number ? DTF.format(received) : "-");

        Element p = document.createElement("p");
        if (extended) {
            if (from != null && from.length > 0) {
                Element strong = document.createElement("strong");
                strong.text(Helper.getString(context, l, R.string.title_from) + " ");
                p.appendChild(strong);
                p.appendText(MessageHelper.formatAddresses(from));
                p.appendElement("br");
            }
            if (to != null && to.length > 0) {
                Element strong = document.createElement("strong");
                strong.text(Helper.getString(context, l, R.string.title_to) + " ");
                p.appendChild(strong);
                p.appendText(MessageHelper.formatAddresses(to));
                p.appendElement("br");
            }
            if (cc != null && cc.length > 0) {
                Element strong = document.createElement("strong");
                strong.text(Helper.getString(context, l, R.string.title_cc) + " ");
                p.appendChild(strong);
                p.appendText(MessageHelper.formatAddresses(cc));
                p.appendElement("br");
            }
            if (received != null) { // embedded messages
                Element strong = document.createElement("strong");
                strong.text(Helper.getString(context, l, R.string.title_date) + " ");
                p.appendChild(strong);
                p.appendText(date);
                p.appendElement("br");
            }
            if (!TextUtils.isEmpty(subject)) {
                Element strong = document.createElement("strong");
                strong.text(Helper.getString(context, l, R.string.title_subject) + " ");
                p.appendChild(strong);
                p.appendText(subject);
                p.appendElement("br");
            }
        } else if (TextUtils.isEmpty(template_reply))
            p.text(date + " " + MessageHelper.formatAddresses(from) + ":");
        else {
            template_reply = template_reply.replace("$from$", MessageHelper.formatAddresses(from));
            template_reply = template_reply.replace("$to$", MessageHelper.formatAddresses(to));
            template_reply = template_reply.replace("$cc$", MessageHelper.formatAddresses(cc));
            template_reply = template_reply.replace("$time$", date);
            template_reply = template_reply.replace("$subject$", subject);
            p.html(template_reply);
        }


        Element div = document.createElement("div")
                .attr("fairemail", "reply");
        try {
            String text = p.text();
            boolean rtl = TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(text, 0, text.length());
            div.attr("dir", rtl ? "rtl" : "ltr");
        } catch (Throwable ex) {
            Log.e(ex);
        }
        if (!TextUtils.isEmpty(compose_font))
            div.attr("style", "font-family: " + StyleHelper.getFamily(compose_font));
        if (separate)
            div.appendElement("hr");
        div.appendChild(p);
        return div;
    }

    String getNotificationChannelId() {
        if (from == null || from.length == 0)
            return null;
        InternetAddress sender = (InternetAddress) from[0];
        return "notification." + sender.getAddress().toLowerCase(Locale.ROOT);
    }

    boolean setLabel(String label, boolean set) {
        List<String> list = new ArrayList<>();
        if (labels != null)
            list.addAll(Arrays.asList(labels));

        boolean changed = false;
        if (set) {
            if (!list.contains(label)) {
                changed = true;
                list.add(label);
            }
        } else {
            if (list.contains(label)) {
                changed = true;
                list.remove(label);
            }
        }

        if (changed)
            labels = list.toArray(new String[0]);

        return changed;
    }

    static File getFile(Context context, Long id) {
        File root = Helper.ensureExists(context, "messages");
        File dir = new File(root, "D" + (id / 1000));
        dir.mkdir(); // TODO CASA composed directory name
        return new File(dir, id.toString());
    }

    static void convert(Context context) {
        File root = Helper.ensureExists(context, "messages");
        List<File> files = Helper.listFiles(root);
        for (File file : files)
            if (file.isFile())
                try {
                    long id = Long.parseLong(file.getName());
                    File target = getFile(context, id);
                    if (!file.renameTo(target))
                        Log.e("Move failed: " + file);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
    }

    File getFile(Context context) {
        return getFile(context, id);
    }

    File getFile(Context context, int revision) {
        File dir = Helper.ensureExists(context, "revision");
        return new File(dir, id + "." + revision);
    }

    File getRefFile(Context context) {
        File dir = Helper.ensureExists(context, "references");
        return new File(dir, id.toString());
    }

    File getRawFile(Context context) {
        return getRawFile(context, id);
    }

    static File getRawFile(Context context, Long id) {
        File dir = Helper.ensureExists(context, "raw");
        return new File(dir, id + ".eml");
    }

    static void snooze(Context context, long id, Long wakeup) {
        if (wakeup != null && wakeup != Long.MAX_VALUE) {
            /*
                java.lang.IllegalStateException: Maximum limit of concurrent alarms 500 reached for uid: u0a601, callingPackage: eu.faircode.email
                    at android.os.Parcel.createExceptionOrNull(Parcel.java:2433)
                    at android.os.Parcel.createException(Parcel.java:2409)
                    at android.os.Parcel.readException(Parcel.java:2392)
                    at android.os.Parcel.readException(Parcel.java:2334)
                    at android.app.IAlarmManager$Stub$Proxy.set(IAlarmManager.java:359)
                    at android.app.AlarmManager.setImpl(AlarmManager.java:947)
                    at android.app.AlarmManager.setImpl(AlarmManager.java:907)
                    at android.app.AlarmManager.setExactAndAllowWhileIdle(AlarmManager.java:1175)
                    at androidx.core.app.AlarmManagerCompat$Api23Impl.setExactAndAllowWhileIdle(Unknown Source:0)
                    at androidx.core.app.AlarmManagerCompat.setExactAndAllowWhileIdle(SourceFile:2)
                    at eu.faircode.email.AlarmManagerCompatEx.setAndAllowWhileIdle(SourceFile:2)
                    at eu.faircode.email.EntityMessage.snooze(SourceFile:7)
             */
            DB db = DB.getInstance(context);
            int count = db.message().getSnoozedCount();
            Log.i("Snoozed=" + count + "/" + MAX_SNOOZED);
            if (count > MAX_SNOOZED)
                throw new IllegalArgumentException(
                        String.format("Due to Android limitations, no more than %d messages can be snoozed or delayed", MAX_SNOOZED));
        }

        Intent snoozed = new Intent(context, ServiceSynchronize.class);
        snoozed.setAction("unsnooze:" + id);
        PendingIntent pi = PendingIntentCompat.getForegroundService(
                context, ServiceSynchronize.PI_UNSNOOZE, snoozed, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = Helper.getSystemService(context, AlarmManager.class);
        if (wakeup == null || wakeup == Long.MAX_VALUE) {
            Log.i("Cancel snooze id=" + id);
            am.cancel(pi);
        } else {
            Log.i("Set snooze id=" + id + " wakeup=" + new Date(wakeup));
            AlarmManagerCompatEx.setAndAllowWhileIdle(context, am, AlarmManager.RTC_WAKEUP, wakeup, pi);
        }
    }

    static String getSwipeType(Long type) {
        if (type == null)
            return "none";
        if (type > 0)
            return "folder";
        if (SWIPE_ACTION_ASK.equals(type))
            return "ask";
        if (SWIPE_ACTION_SEEN.equals(type))
            return "seen";
        if (SWIPE_ACTION_SNOOZE.equals(type))
            return "snooze";
        if (SWIPE_ACTION_HIDE.equals(type))
            return "hide";
        if (SWIPE_ACTION_MOVE.equals(type))
            return "move";
        if (SWIPE_ACTION_FLAG.equals(type))
            return "flag";
        if (SWIPE_ACTION_IMPORTANCE.equals(type))
            return "importance";
        if (SWIPE_ACTION_DELETE.equals(type))
            return "delete";
        if (SWIPE_ACTION_JUNK.equals(type))
            return "junk";
        if (SWIPE_ACTION_REPLY.equals(type))
            return "reply";
        if (SWIPE_ACTION_SUMMARIZE.equals(type))
            return "summarize";
        if (SWIPE_ACTION_TTS.equals(type))
            return "TTS";
        return "???";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityMessage) {
            EntityMessage other = (EntityMessage) obj;
            return (Objects.equals(this.account, other.account) &&
                    this.folder.equals(other.folder) &&
                    Objects.equals(this.identity, other.identity) &&
                    // extra
                    Objects.equals(this.uid, other.uid) &&
                    Objects.equals(this.msgid, other.msgid) &&
                    Objects.equals(this.references, other.references) &&
                    Objects.equals(this.deliveredto, other.deliveredto) &&
                    Objects.equals(this.inreplyto, other.inreplyto) &&
                    Objects.equals(this.wasforwardedfrom, other.wasforwardedfrom) &&
                    Objects.equals(this.thread, other.thread) &&
                    Objects.equals(this.priority, other.priority) &&
                    Objects.equals(this.importance, other.importance) &&
                    Objects.equals(this.sensitivity, other.sensitivity) &&
                    Objects.equals(this.dsn, other.dsn) &&
                    Objects.equals(this.receipt_request, other.receipt_request) &&
                    MessageHelper.equal(this.receipt_to, other.receipt_to) &&
                    Objects.equals(this.bimi_selector, other.bimi_selector) &&
                    Objects.equals(this.tls, other.tls) &&
                    Objects.equals(this.dkim, other.dkim) &&
                    Objects.equals(this.spf, other.spf) &&
                    Objects.equals(this.dmarc, other.dmarc) &&
                    Objects.equals(this.auth, other.auth) &&
                    Objects.equals(this.mx, other.mx) &&
                    Objects.equals(this.blocklist, other.blocklist) &&
                    Objects.equals(this.from_domain, other.from_domain) &&
                    Objects.equals(this.reply_domain, other.reply_domain) &&
                    Objects.equals(this.avatar, other.avatar) &&
                    Objects.equals(this.sender, other.sender) &&
                    MessageHelper.equal(this.return_path, other.return_path) &&
                    MessageHelper.equal(this.smtp_from, other.smtp_from) &&
                    MessageHelper.equal(this.submitter, other.submitter) &&
                    MessageHelper.equal(this.from, other.from) &&
                    MessageHelper.equal(this.to, other.to) &&
                    MessageHelper.equal(this.cc, other.cc) &&
                    MessageHelper.equal(this.bcc, other.bcc) &&
                    MessageHelper.equal(this.reply, other.reply) &&
                    MessageHelper.equal(this.list_post, other.list_post) &&
                    Objects.equals(this.unsubscribe, other.unsubscribe) &&
                    Objects.equals(this.autocrypt, other.autocrypt) &&
                    Objects.equals(this.headers, other.headers) &&
                    Objects.equals(this.infrastructure, other.infrastructure) &&
                    Objects.equals(this.raw, other.raw) &&
                    Objects.equals(this.subject, other.subject) &&
                    Objects.equals(this.size, other.size) &&
                    Objects.equals(this.total, other.total) &&
                    Objects.equals(this.attachments, other.attachments) &&
                    this.content == other.content &&
                    Objects.equals(this.language, other.language) &&
                    Objects.equals(this.plain_only, other.plain_only) &&
                    Objects.equals(this.encrypt, other.encrypt) &&
                    Objects.equals(this.ui_encrypt, other.ui_encrypt) &&
                    this.verified == other.verified &&
                    Objects.equals(this.preview, other.preview) &&
                    Objects.equals(this.notes, other.notes) &&
                    Objects.equals(this.notes_color, other.notes_color) &&
                    this.signature.equals(other.signature) &&
                    Objects.equals(this.sent, other.sent) &&
                    this.received.equals(other.received) &&
                    this.stored.equals(other.stored) &&
                    this.recent.equals(other.recent) &&
                    this.seen.equals(other.seen) &&
                    this.answered.equals(other.answered) &&
                    this.flagged.equals(other.flagged) &&
                    this.deleted.equals(other.deleted) &&
                    Objects.equals(this.flags, other.flags) &&
                    Helper.equal(this.keywords, other.keywords) &&
                    this.auto_classified.equals(other.auto_classified) &&
                    this.notifying.equals(other.notifying) &&
                    this.ui_seen.equals(other.ui_seen) &&
                    this.ui_answered.equals(other.ui_answered) &&
                    this.ui_flagged.equals(other.ui_flagged) &&
                    this.ui_deleted.equals(other.ui_deleted) &&
                    this.ui_hide.equals(other.ui_hide) &&
                    this.ui_found.equals(other.ui_found) &&
                    this.ui_ignored.equals(other.ui_ignored) &&
                    this.ui_silent.equals(other.ui_silent) &&
                    this.ui_local_only.equals(other.ui_local_only) &&
                    this.ui_browsed.equals(other.ui_browsed) &&
                    Objects.equals(this.ui_busy, other.ui_busy) &&
                    Objects.equals(this.ui_snoozed, other.ui_snoozed) &&
                    this.ui_unsnoozed.equals(other.ui_unsnoozed) &&
                    this.show_images.equals(other.show_images) &&
                    this.show_full.equals(other.show_full) &&
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
