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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityRule.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "folder", entity = EntityFolder.class, parentColumns = "id", onDelete = CASCADE),
        },
        indices = {
                @Index(value = {"folder"}),
                @Index(value = {"order"})
        }
)
public class EntityRule {
    static final String TABLE_NAME = "rule";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long folder;
    @NonNull
    public String name;
    @NonNull
    public int order;
    @NonNull
    public boolean enabled;
    @NonNull
    public boolean stop;
    @NonNull
    public String condition;
    @NonNull
    public String action;
    @NonNull
    public Integer applied = 0;

    static final int TYPE_SEEN = 1;
    static final int TYPE_UNSEEN = 2;
    static final int TYPE_MOVE = 3;
    static final int TYPE_ANSWER = 4;
    static final int TYPE_AUTOMATION = 5;
    static final int TYPE_FLAG = 6;
    static final int TYPE_COPY = 7;
    static final int TYPE_SNOOZE = 8;
    static final int TYPE_IGNORE = 9;
    static final int TYPE_NOOP = 10;
    static final int TYPE_KEYWORD = 11;
    static final int TYPE_HIDE = 12;
    static final int TYPE_IMPORTANCE = 13;

    static final String ACTION_AUTOMATION = BuildConfig.APPLICATION_ID + ".AUTOMATION";
    static final String EXTRA_RULE = "rule";
    static final String EXTRA_SENDER = "sender";
    static final String EXTRA_SUBJECT = "subject";

    private static final long SEND_DELAY = 5000L; // milliseconds

    boolean matches(Context context, EntityMessage message, Message imessage) throws MessagingException {
        try {
            JSONObject jcondition = new JSONObject(condition);

            // Sender
            JSONObject jsender = jcondition.optJSONObject("sender");
            if (jsender != null) {
                String value = jsender.getString("value");
                boolean regex = jsender.getBoolean("regex");
                boolean known = jsender.optBoolean("known");

                boolean matches = false;
                if (message.from != null) {
                    for (Address from : message.from) {
                        InternetAddress ia = (InternetAddress) from;
                        String email = ia.getAddress();
                        String personal = ia.getPersonal();

                        if (known) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
                            if (suggest_sent) {
                                DB db = DB.getInstance(context);
                                EntityContact contact =
                                        db.contact().getContact(message.account, EntityContact.TYPE_TO, email);
                                if (contact != null) {
                                    Log.i(email + " is local contact");
                                    matches = true;
                                    break;
                                }
                            }

                            if (!TextUtils.isEmpty(message.avatar)) {
                                Log.i(email + " is Android contact");
                                matches = true;
                                break;
                            }
                        } else {
                            String formatted = ((personal == null ? "" : personal + " ") + "<" + email + ">");
                            if (matches(context, value, formatted, regex)) {
                                matches = true;
                                break;
                            }
                        }
                    }
                }
                if (!matches)
                    return false;
            }

            // Recipient
            JSONObject jrecipient = jcondition.optJSONObject("recipient");
            if (jrecipient != null) {
                String value = jrecipient.getString("value");
                boolean regex = jrecipient.getBoolean("regex");

                boolean matches = false;
                List<Address> recipients = new ArrayList<>();
                if (message.to != null)
                    recipients.addAll(Arrays.asList(message.to));
                if (message.cc != null)
                    recipients.addAll(Arrays.asList(message.cc));
                for (Address recipient : recipients) {
                    InternetAddress ia = (InternetAddress) recipient;
                    String personal = ia.getPersonal();
                    String formatted = ((personal == null ? "" : personal + " ") + "<" + ia.getAddress() + ">");
                    if (matches(context, value, formatted, regex)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches)
                    return false;
            }

            // Subject
            JSONObject jsubject = jcondition.optJSONObject("subject");
            if (jsubject != null) {
                String value = jsubject.getString("value");
                boolean regex = jsubject.getBoolean("regex");

                if (!matches(context, value, message.subject, regex))
                    return false;
            }

            // Attachments
            if (jcondition.optBoolean("attachments")) {
                DB db = DB.getInstance(context);
                if (db.attachment().getAttachments(message.id).size() == 0)
                    return false;
            }

            // Header
            JSONObject jheader = jcondition.optJSONObject("header");
            if (jheader != null && imessage != null) {
                String value = jheader.getString("value");
                boolean regex = jheader.getBoolean("regex");

                boolean matches = false;
                Enumeration<Header> headers = imessage.getAllHeaders();
                while (headers.hasMoreElements()) {
                    Header header = headers.nextElement();
                    String formatted = header.getName() + ": " + header.getValue();
                    if (matches(context, value, formatted, regex)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches)
                    return false;
            }

            // Schedule
            JSONObject jschedule = jcondition.optJSONObject("schedule");
            if (jschedule != null) {
                int start = jschedule.optInt("start", 0);
                int end = jschedule.optInt("end", 0);

                Calendar cal_start = getRelativeCalendar(start, message.received);
                Calendar cal_end = getRelativeCalendar(end, message.received);

                if (cal_start.getTimeInMillis() > cal_end.getTimeInMillis())
                    cal_start.add(Calendar.HOUR_OF_DAY, -7 * 24);

                if (message.received < cal_start.getTimeInMillis() ||
                        message.received > cal_end.getTimeInMillis())
                    return false;
            }

            // Safeguard
            if (jsender == null &&
                    jrecipient == null &&
                    jsubject == null &&
                    !jcondition.optBoolean("attachments") &&
                    jheader == null &&
                    jschedule == null)
                return false;
        } catch (JSONException ex) {
            Log.e(ex);
            return false;
        }

        return true;
    }

    private boolean matches(Context context, String needle, String haystack, boolean regex) {
        boolean matched = false;
        if (needle != null && haystack != null)
            if (regex) {
                Pattern pattern = Pattern.compile(needle, Pattern.DOTALL);
                matched = pattern.matcher(haystack).matches();
            } else
                matched = haystack.toLowerCase().contains(needle.trim().toLowerCase());

        if (matched)
            EntityLog.log(context, "Rule=" + name + ":" + order + " matched " +
                    " needle=" + needle + " haystack=" + haystack + " regex=" + regex);
        else
            Log.i("Rule=" + name + ":" + order + " matched=" + matched +
                    " needle=" + needle + " haystack=" + haystack + " regex=" + regex);
        return matched;
    }

    boolean execute(Context context, EntityMessage message) throws JSONException, IOException {
        boolean executed = _execute(context, message);
        if (id != null && executed) {
            DB db = DB.getInstance(context);
            db.rule().applyRule(id);
        }
        return executed;
    }

    private boolean _execute(Context context, EntityMessage message) throws JSONException, IOException {
        JSONObject jaction = new JSONObject(action);
        int type = jaction.getInt("type");
        Log.i("Executing rule=" + type + ":" + name + " message=" + message.id);

        switch (type) {
            case TYPE_NOOP:
                return true;
            case TYPE_SEEN:
                return onActionSeen(context, message, true);
            case TYPE_UNSEEN:
                return onActionSeen(context, message, false);
            case TYPE_HIDE:
                return onActionHide(context, message);
            case TYPE_IGNORE:
                return onActionIgnore(context, message, jaction);
            case TYPE_SNOOZE:
                return onActionSnooze(context, message, jaction);
            case TYPE_FLAG:
                return onActionFlag(context, message, jaction);
            case TYPE_IMPORTANCE:
                return onActionImportance(context, message, jaction);
            case TYPE_KEYWORD:
                return onActionKeyword(context, message, jaction);
            case TYPE_MOVE:
                return onActionMove(context, message, jaction);
            case TYPE_COPY:
                return onActionCopy(context, message, jaction);
            case TYPE_ANSWER:
                return onActionAnswer(context, message, jaction);
            case TYPE_AUTOMATION:
                return onActionAutomation(context, message, jaction);
            default:
                throw new IllegalArgumentException("Unknown rule type=" + type);
        }
    }

    private boolean onActionSeen(Context context, EntityMessage message, boolean seen) {
        EntityOperation.queue(context, message, EntityOperation.SEEN, seen);

        message.ui_seen = seen;
        message.ui_ignored = true;

        return true;
    }

    private boolean onActionHide(Context context, EntityMessage message) {
        DB db = DB.getInstance(context);
        db.message().setMessageSnoozed(message.id, Long.MAX_VALUE);
        db.message().setMessageUiIgnored(message.id, true);
        EntityMessage.snooze(context, message.id, Long.MAX_VALUE);

        message.ui_snoozed = Long.MAX_VALUE;
        message.ui_ignored = true;
        return true;
    }

    private boolean onActionIgnore(Context context, EntityMessage message, JSONObject jargs) {
        DB db = DB.getInstance(context);
        db.message().setMessageUiIgnored(message.id, true);

        message.ui_ignored = true;
        return true;
    }

    private boolean onActionMove(Context context, EntityMessage message, JSONObject jargs) {
        long target = jargs.optLong("target", -1);
        boolean seen = jargs.optBoolean("seen");
        boolean thread = jargs.optBoolean("thread");

        DB db = DB.getInstance(context);
        EntityFolder folder = db.folder().getFolder(target);
        if (folder == null)
            throw new IllegalArgumentException("Rule move to folder not found");

        List<EntityMessage> messages = db.message().getMessagesByThread(
                message.account, message.thread, thread ? null : message.id, message.folder);
        for (EntityMessage threaded : messages)
            EntityOperation.queue(context, threaded, EntityOperation.MOVE, target, seen);

        if (seen) {
            message.ui_seen = true;
            message.ui_ignored = true;
        }

        return true;
    }

    private boolean onActionCopy(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        long target = jargs.getLong("target");

        DB db = DB.getInstance(context);
        EntityFolder folder = db.folder().getFolder(target);
        if (folder == null)
            throw new IllegalArgumentException("Rule copy to folder not found");

        EntityOperation.queue(context, message, EntityOperation.COPY, target, false);

        return true;
    }

    private boolean onActionAnswer(Context context, EntityMessage message, JSONObject jargs) throws JSONException, IOException {
        long iid = jargs.getLong("identity");
        long aid = jargs.getLong("answer");
        boolean cc = (jargs.has("cc") && jargs.getBoolean("cc"));

        DB db = DB.getInstance(context);

        EntityIdentity identity = db.identity().getIdentity(iid);
        if (identity == null)
            throw new IllegalArgumentException("Rule identity not found");

        EntityAnswer answer = db.answer().getAnswer(aid);
        if (answer == null)
            throw new IllegalArgumentException("Rule answer not found");


        EntityMessage reply = new EntityMessage();
        reply.account = message.account;
        reply.folder = db.folder().getOutbox().id;
        reply.identity = identity.id;
        reply.msgid = EntityMessage.generateMessageId();
        reply.references = (message.references == null ? "" : message.references + " ") + message.msgid;
        reply.inreplyto = message.msgid;
        reply.thread = message.thread;
        reply.to = (message.reply == null || message.reply.length == 0 ? message.from : message.reply);
        reply.from = new InternetAddress[]{new InternetAddress(identity.email, identity.name)};
        if (cc)
            reply.cc = message.cc;
        reply.unsubscribe = "mailto:" + identity.email;
        reply.subject = context.getString(R.string.title_subject_reply, message.subject == null ? "" : message.subject);
        reply.received = new Date().getTime();

        reply.sender = MessageHelper.getSortKey(reply.from);
        Uri lookupUri = ContactInfo.getLookupUri(context, reply.from);
        reply.avatar = (lookupUri == null ? null : lookupUri.toString());

        reply.id = db.message().insertMessage(reply);

        String body = answer.getText(message.from);
        File file = reply.getFile(context);
        Helper.writeText(file, body);
        db.message().setMessageContent(reply.id,
                true,
                HtmlHelper.getLanguage(context, body),
                false,
                HtmlHelper.getPreview(body),
                null);

        EntityOperation.queue(context, reply, EntityOperation.SEND);

        // Batch send operations, wait until after commit
        ServiceSend.schedule(context, SEND_DELAY);

        return true;
    }

    private boolean onActionAutomation(Context context, EntityMessage message, JSONObject jargs) {
        String sender = (message.from == null || message.from.length == 0
                ? null : ((InternetAddress) message.from[0]).getAddress());

        Intent automation = new Intent(ACTION_AUTOMATION);
        automation.putExtra(EXTRA_RULE, name);
        automation.putExtra(EXTRA_SENDER, sender);
        automation.putExtra(EXTRA_SUBJECT, message.subject);

        Log.i("Sending " + automation);
        context.sendBroadcast(automation);

        return true;
    }

    private boolean onActionSnooze(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        int duration = jargs.getInt("duration");
        boolean schedule_end = jargs.optBoolean("schedule_end", false);
        boolean seen = jargs.optBoolean("seen", false);

        long wakeup;
        if (schedule_end) {
            JSONObject jcondition = new JSONObject(condition);
            JSONObject jschedule = jcondition.optJSONObject("schedule");

            int end = (jschedule == null ? 0 : jschedule.optInt("end", 0));
            Calendar cal = getRelativeCalendar(end, message.received);
            wakeup = cal.getTimeInMillis() + duration * 3600 * 1000L;
        } else
            wakeup = message.received + duration * 3600 * 1000L;

        if (wakeup < new Date().getTime())
            return false;

        DB db = DB.getInstance(context);
        db.message().setMessageSnoozed(message.id, wakeup);
        db.message().setMessageUiIgnored(message.id, true);
        EntityMessage.snooze(context, message.id, wakeup);

        message.ui_snoozed = wakeup;

        if (seen)
            onActionSeen(context, message, true);

        return true;
    }

    private boolean onActionFlag(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        Integer color = (jargs.has("color") && !jargs.isNull("color")
                ? jargs.getInt("color") : null);

        EntityOperation.queue(context, message, EntityOperation.FLAG, true, color);

        message.ui_flagged = true;
        message.color = color;

        return true;
    }

    private boolean onActionImportance(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        Integer importance = jargs.getInt("value");
        if (EntityMessage.PRIORITIY_NORMAL.equals(importance))
            importance = null;

        DB db = DB.getInstance(context);
        db.message().setMessageImportance(message.id, importance);

        message.importance = importance;

        return true;
    }

    private boolean onActionKeyword(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        String keyword = jargs.getString("keyword");
        if (TextUtils.isEmpty(keyword)) {
            Log.w("Keyword empty");
            return false;
        }

        EntityOperation.queue(context, message, EntityOperation.KEYWORD, keyword, true);

        return true;
    }

    private static Calendar getRelativeCalendar(int minutes, long reference) {
        int d = minutes / (24 * 60);
        int h = minutes / 60 % 24;
        int m = minutes % 60;

        Calendar cal = Calendar.getInstance();
        if (reference > cal.getTimeInMillis() - 7 * 24 * 3600 * 1000L)
            cal.setTimeInMillis(reference);
        long time = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY + d);
        if (cal.getTimeInMillis() < time)
            cal.add(Calendar.HOUR_OF_DAY, 7 * 24);

        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);

        return cal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityRule) {
            EntityRule other = (EntityRule) obj;
            return this.folder.equals(other.folder) &&
                    this.name.equals(other.name) &&
                    this.order == other.order &&
                    this.enabled == other.enabled &&
                    this.stop == other.stop &&
                    this.condition.equals(other.condition) &&
                    this.action.equals(other.action) &&
                    this.applied.equals(other.applied);
        } else
            return false;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("order", order);
        json.put("enabled", enabled);
        json.put("stop", stop);
        json.put("condition", condition);
        json.put("action", action);
        json.put("applied", applied);
        return json;
    }

    public static EntityRule fromJSON(JSONObject json) throws JSONException {
        EntityRule rule = new EntityRule();
        // id
        rule.name = json.getString("name");
        rule.order = json.getInt("order");
        rule.enabled = json.getBoolean("enabled");
        rule.stop = json.getBoolean("stop");
        rule.condition = json.getString("condition");
        rule.action = json.getString("action");
        rule.applied = json.optInt("applied", 0);
        return rule;
    }
}
