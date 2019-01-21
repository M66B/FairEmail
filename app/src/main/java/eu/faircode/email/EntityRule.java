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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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

    static final int TYPE_SEEN = 1;
    static final int TYPE_UNSEEN = 2;
    static final int TYPE_MOVE = 3;
    static final int TYPE_ANSWER = 4;

    boolean matches(Context context, EntityMessage message, Message imessage) throws MessagingException {
        try {
            JSONObject jcondition = new JSONObject(condition);

            JSONObject jsender = jcondition.optJSONObject("sender");
            if (jsender != null) {
                String value = jsender.getString("value");
                boolean regex = jsender.getBoolean("regex");

                boolean matches = false;
                if (message.from != null) {
                    for (Address from : message.from) {
                        InternetAddress ia = (InternetAddress) from;
                        String personal = ia.getPersonal();
                        String formatted = ((personal == null ? "" : personal + " ") + "<" + ia.getAddress() + ">");
                        if (matches(value, formatted, regex)) {
                            matches = true;
                            break;
                        }
                    }
                }
                if (!matches)
                    return false;
            }

            JSONObject jsubject = jcondition.optJSONObject("subject");
            if (jsubject != null) {
                String value = jsubject.getString("value");
                boolean regex = jsubject.getBoolean("regex");

                if (!matches(value, message.subject, regex))
                    return false;
            }

            JSONObject jheader = jcondition.optJSONObject("header");
            if (jheader != null) {
                String value = jheader.getString("value");
                boolean regex = jheader.getBoolean("regex");

                boolean matches = false;
                Enumeration<Header> headers = imessage.getAllHeaders();
                while (headers.hasMoreElements()) {
                    Header header = headers.nextElement();
                    String formatted = header.getName() + ": " + header.getValue();
                    if (matches(value, formatted, regex)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches)
                    return false;
            }

            // Safeguard
            if (jsender == null && jsubject == null && jheader == null)
                return false;
        } catch (JSONException ex) {
            Log.e(ex);
            return false;
        }

        return true;
    }

    private boolean matches(String needle, String haystack, boolean regex) {
        Log.i("Matches needle=" + needle + " haystack=" + haystack + " regex=" + regex);

        if (needle == null || haystack == null)
            return false;

        if (regex) {
            Pattern pattern = Pattern.compile(needle);
            return pattern.matcher(haystack).matches();
        } else
            return haystack.toLowerCase().contains(needle.toLowerCase());
    }

    void execute(Context context, DB db, EntityMessage message) throws IOException {
        try {
            JSONObject jargs = new JSONObject(action);
            int type = jargs.getInt("type");
            Log.i("Executing rule=" + type + ":" + name + " message=" + message.id);

            switch (type) {
                case TYPE_SEEN:
                    onActionSeen(context, db, message, true);
                    break;
                case TYPE_UNSEEN:
                    onActionSeen(context, db, message, false);
                    break;
                case TYPE_MOVE:
                    onActionMove(context, db, message, jargs);
                    break;
                case TYPE_ANSWER:
                    onActionAnswer(context, db, message, jargs);
                    break;
            }
        } catch (JSONException ex) {
            Log.e(ex);
        }
    }

    private void onActionSeen(Context context, DB db, EntityMessage message, boolean seen) {
        EntityOperation.queue(context, db, message, EntityOperation.SEEN, seen);
        message.seen = seen;
    }

    private void onActionMove(Context context, DB db, EntityMessage message, JSONObject jargs) throws JSONException {
        long target = jargs.getLong("target");
        EntityOperation.queue(context, db, message, EntityOperation.MOVE, target, false);
    }

    private void onActionAnswer(Context context, DB db, EntityMessage message, JSONObject jargs) throws JSONException, IOException {
        long iid = jargs.getLong("identity");
        long aid = jargs.getLong("answer");

        EntityIdentity identity = db.identity().getIdentity(iid);
        if (identity == null)
            throw new IllegalArgumentException("Rule identity not found");

        String body = EntityAnswer.getAnswerText(db, aid, message.from);
        if (body == null)
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
        reply.subject = context.getString(R.string.title_subject_reply, message.subject == null ? "" : message.subject);
        reply.sender = MessageHelper.getSortKey(reply.from);
        reply.received = new Date().getTime();
        reply.id = db.message().insertMessage(reply);
        Helper.writeText(EntityMessage.getFile(context, reply.id), body);
        db.message().setMessageContent(reply.id, true, HtmlHelper.getPreview(body));

        EntityOperation.queue(context, db, reply, EntityOperation.SEND);
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
                    this.action.equals(other.action);
        } else
            return false;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("order", order);
        json.put("enabled", enabled);
        json.put("stop", stop);
        json.put("condition", condition);
        json.put("action", action);
        return json;
    }

    public static EntityRule fromJSON(JSONObject json) throws JSONException {
        EntityRule rule = new EntityRule();
        rule.name = json.getString("name");
        rule.order = json.getInt("order");
        rule.enabled = json.getBoolean("enabled");
        rule.stop = json.getBoolean("stop");
        rule.condition = json.getString("condition");
        rule.action = json.getString("action");
        return rule;
    }
}
