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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static androidx.room.ForeignKey.CASCADE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;

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
    public String uuid = UUID.randomUUID().toString();
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
    public Long last_applied;

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
    static final int TYPE_TTS = 14;
    static final int TYPE_DELETE = 15;
    static final int TYPE_SOUND = 16;

    static final String ACTION_AUTOMATION = BuildConfig.APPLICATION_ID + ".AUTOMATION";
    static final String EXTRA_RULE = "rule";
    static final String EXTRA_SENDER = "sender";
    static final String EXTRA_SUBJECT = "subject";
    static final String EXTRA_RECEIVED = "received";

    private static final long SEND_DELAY = 5000L; // milliseconds

    private static final ExecutorService executor = Helper.getBackgroundExecutor(1, "rule");

    static boolean needsHeaders(EntityMessage message, List<EntityRule> rules) {
        return needs(rules, "header");
    }

    static boolean needsBody(EntityMessage message, List<EntityRule> rules) {
        if (message.encrypt != null && !EntityMessage.ENCRYPT_NONE.equals(message.encrypt))
            return false;
        return needs(rules, "body");
    }

    private static boolean needs(List<EntityRule> rules, String what) {
        for (EntityRule rule : rules)
            try {
                JSONObject jcondition = new JSONObject(rule.condition);
                if (jcondition.has(what))
                    return true;
            } catch (Throwable ex) {
                Log.e(ex);
            }

        return false;
    }

    boolean matches(Context context, EntityMessage message, List<Header> headers, String html) throws MessagingException {
        try {
            JSONObject jcondition = new JSONObject(condition);

            // Sender
            JSONObject jsender = jcondition.optJSONObject("sender");
            if (jsender != null) {
                String value = jsender.getString("value");
                boolean regex = jsender.getBoolean("regex");
                boolean known = jsender.optBoolean("known");

                boolean matches = false;
                List<Address> senders = new ArrayList<>();
                if (message.from != null)
                    senders.addAll(Arrays.asList(message.from));
                if (message.reply != null)
                    senders.addAll(Arrays.asList(message.reply));
                for (Address sender : senders) {
                    InternetAddress ia = (InternetAddress) sender;
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
                        if (matches(context, message, value, formatted, regex)) {
                            matches = true;
                            break;
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
                    if (matches(context, message, value, formatted, regex)) {
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

                if (!matches(context, message, value, message.subject, regex))
                    return false;
            }

            // Attachments
            if (jcondition.optBoolean("attachments")) {
                DB db = DB.getInstance(context);
                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                if (attachments.size() == 0)
                    return false;

                if (jcondition.has("mimetype")) {
                    String mimeType = jcondition.getString("mimetype");
                    if (!TextUtils.isEmpty(mimeType)) {
                        boolean found = false;
                        for (EntityAttachment attachment : attachments)
                            if (mimeType.equalsIgnoreCase(attachment.getMimeType())) {
                                found = true;
                                break;
                            }

                        if (!found)
                            return false;
                    }
                }
            }

            // Header
            JSONObject jheader = jcondition.optJSONObject("header");
            if (jheader != null) {
                String value = jheader.getString("value");
                boolean regex = jheader.getBoolean("regex");

                if (!regex &&
                        value != null &&
                        value.startsWith("$") &&
                        value.endsWith("$")) {
                    String keyword = value.substring(1, value.length() - 1);

                    if ("$tls".equals(keyword)) {
                        if (!Boolean.TRUE.equals(message.tls))
                            return false;
                    } else if ("$dkim".equals(keyword)) {
                        if (!Boolean.TRUE.equals(message.dkim))
                            return false;
                    } else if ("$spf".equals(keyword)) {
                        if (!Boolean.TRUE.equals(message.spf))
                            return false;
                    } else if ("$dmarc".equals(keyword)) {
                        if (!Boolean.TRUE.equals(message.dmarc))
                            return false;
                    } else if ("$mx".equals(keyword)) {
                        if (!Boolean.TRUE.equals(message.mx))
                            return false;
                    } else if ("$blocklist".equals(keyword)) {
                        if (!Boolean.FALSE.equals(message.blocklist))
                            return false;
                    } else if ("$replydomain".equals(keyword)) {
                        if (!Boolean.TRUE.equals(message.reply_domain))
                            return false;
                    } else if ("$nofrom".equals(keyword)) {
                        if (message.from != null && message.from.length > 0)
                            return false;
                    } else if ("$multifrom".equals(keyword)) {
                        if (message.from == null || message.from.length < 2)
                            return false;
                    } else if ("$automatic".equals(keyword)) {
                        if (!Boolean.TRUE.equals(message.auto_submitted))
                            return false;
                    } else if ("$lowpriority".equals(keyword)) {
                        if (!EntityMessage.PRIORITIY_LOW.equals(message.priority))
                            return false;
                    } else if ("$highpriority".equals(keyword)) {
                        if (!EntityMessage.PRIORITIY_HIGH.equals(message.priority))
                            return false;
                    } else if ("$signed".equals(keyword)) {
                        if (!message.isSigned())
                            return false;
                    } else if ("$encrypted".equals(keyword)) {
                        if (!message.isEncrypted())
                            return false;
                    } else {
                        List<String> keywords = new ArrayList<>();
                        keywords.addAll(Arrays.asList(message.keywords));

                        if (message.ui_seen)
                            keywords.add("$seen");
                        if (message.ui_answered)
                            keywords.add("$answered");
                        if (message.ui_flagged)
                            keywords.add("$flagged");
                        if (message.ui_deleted)
                            keywords.add("$deleted");
                        if (message.infrastructure != null)
                            keywords.add('$' + message.infrastructure);

                        if (!keywords.contains(keyword))
                            return false;
                    }
                } else {
                    if (headers == null) {
                        if (message.headers == null)
                            throw new IllegalArgumentException(context.getString(R.string.title_rule_no_headers));

                        ByteArrayInputStream bis = new ByteArrayInputStream(message.headers.getBytes());
                        headers = Collections.list(new InternetHeaders(bis, true).getAllHeaders());
                    }

                    boolean matches = false;
                    for (Header header : headers) {
                        String formatted = header.getName() + ": " + header.getValue();
                        if (matches(context, message, value, formatted, regex)) {
                            matches = true;
                            break;
                        }
                    }
                    if (!matches)
                        return false;
                }
            }

            // Body
            JSONObject jbody = jcondition.optJSONObject("body");
            if (jbody != null) {
                String value = jbody.getString("value");
                boolean regex = jbody.getBoolean("regex");
                boolean skip_quotes = jbody.optBoolean("skip_quotes");

                if (!regex)
                    value = value.replaceAll("\\s+", " ");

                if (html == null && message.content) {
                    File file = message.getFile(context);
                    try {
                        html = Helper.readText(file);
                    } catch (IOException ex) {
                        Log.e(ex);
                    }
                }

                if (html == null)
                    if (message.encrypt == null || EntityMessage.ENCRYPT_NONE.equals(message.encrypt))
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_no_body));
                    else
                        return false;

                Document d = JsoupEx.parse(html);
                if (skip_quotes)
                    d.select("blockquote").remove();
                String text = d.body().text();
                if (!matches(context, message, value, text, regex))
                    return false;
            }

            // Date
            JSONObject jdate = jcondition.optJSONObject("date");
            if (jdate != null) {
                long after = jdate.optLong("after", 0);
                long before = jdate.optLong("before", 0);
                if ((after != 0 && message.received < after) || (before != 0 && message.received > before))
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
                    jbody == null &&
                    jdate == null &&
                    jschedule == null)
                return false;
        } catch (JSONException ex) {
            Log.e(ex);
            return false;
        }

        return true;
    }

    private boolean matches(Context context, EntityMessage message, String needle, String haystack, boolean regex) {
        boolean matched = false;
        if (needle != null && haystack != null)
            if (regex) {
                Pattern pattern = Pattern.compile(needle, Pattern.DOTALL);
                matched = pattern.matcher(haystack).matches();
            } else
                matched = haystack.toLowerCase().contains(needle.trim().toLowerCase());

        if (matched)
            EntityLog.log(context, EntityLog.Type.Rules, message,
                    "Rule=" + name + ":" + order + " matched " +
                            " needle=" + needle + " haystack=" + haystack + " regex=" + regex);
        else
            Log.i("Rule=" + name + ":" + order + " matched=" + matched +
                    " needle=" + needle + " haystack=" + haystack + " regex=" + regex);
        return matched;
    }

    boolean execute(Context context, EntityMessage message) throws JSONException {
        boolean executed = _execute(context, message);
        if (this.id != null && executed) {
            DB db = DB.getInstance(context);
            db.rule().applyRule(id, new Date().getTime());
        }
        return executed;
    }

    private boolean _execute(Context context, EntityMessage message) throws JSONException, IllegalArgumentException {
        JSONObject jaction = new JSONObject(action);
        int type = jaction.getInt("type");
        EntityLog.log(context, EntityLog.Type.Rules, message, "Executing rule=" + type + ":" + name);

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
            case TYPE_TTS:
                return onActionTts(context, message, jaction);
            case TYPE_AUTOMATION:
                return onActionAutomation(context, message, jaction);
            case TYPE_DELETE:
                return onActionDelete(context, message, jaction);
            case TYPE_SOUND:
                return onActionSound(context, message, jaction);
            default:
                throw new IllegalArgumentException("Unknown rule type=" + type + " name=" + name);
        }
    }

    void validate(Context context) throws JSONException, IllegalArgumentException {
        JSONObject jargs = new JSONObject(action);
        int type = jargs.getInt("type");

        DB db = DB.getInstance(context);
        switch (type) {
            case TYPE_NOOP:
                return;
            case TYPE_SEEN:
                return;
            case TYPE_UNSEEN:
                return;
            case TYPE_HIDE:
                return;
            case TYPE_IGNORE:
                return;
            case TYPE_SNOOZE:
                return;
            case TYPE_FLAG:
                return;
            case TYPE_IMPORTANCE:
                return;
            case TYPE_KEYWORD:
                String keyword = jargs.optString("keyword");
                if (TextUtils.isEmpty(keyword))
                    throw new IllegalArgumentException(context.getString(R.string.title_rule_keyword_missing));
                return;
            case TYPE_MOVE:
            case TYPE_COPY:
                long target = jargs.optLong("target", -1);
                if (target < 0)
                    throw new IllegalArgumentException(context.getString(R.string.title_rule_folder_missing));
                EntityFolder folder = db.folder().getFolder(target);
                if (folder == null)
                    throw new IllegalArgumentException("Folder not found");
                return;
            case TYPE_ANSWER:
                long iid = jargs.optLong("identity", -1);
                if (iid < 0)
                    throw new IllegalArgumentException(context.getString(R.string.title_rule_identity_missing));
                EntityIdentity identity = db.identity().getIdentity(iid);
                if (identity == null)
                    throw new IllegalArgumentException("Identity not found");

                long aid = jargs.optLong("answer", -1);
                if (aid < 0) {
                    String to = jargs.optString("to");
                    if (TextUtils.isEmpty(to))
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_answer_missing));
                    else
                        try {
                            InternetAddress[] addresses = MessageHelper.parseAddresses(context, to);
                            if (addresses == null || addresses.length == 0)
                                throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                        } catch (AddressException ex) {
                            throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, to));
                        }
                } else {
                    EntityAnswer answer = db.answer().getAnswer(aid);
                    if (answer == null)
                        throw new IllegalArgumentException("Template not found");
                }
                return;
            case TYPE_TTS:
                return;
            case TYPE_AUTOMATION:
                return;
            case TYPE_DELETE:
                return;
            case TYPE_SOUND:
                String uri = jargs.optString("uri");
                if (TextUtils.isEmpty(uri))
                    throw new IllegalArgumentException(context.getString(R.string.title_rule_select_sound));
                return;
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
        String create = jargs.optString("create");
        boolean seen = jargs.optBoolean("seen");
        boolean thread = jargs.optBoolean("thread");

        DB db = DB.getInstance(context);

        EntityFolder folder = db.folder().getFolder(target);
        if (folder == null)
            throw new IllegalArgumentException("Rule move to folder not found name=" + name);

        if (!TextUtils.isEmpty(create)) {
            Calendar calendar = Calendar.getInstance();
            String year = String.format(Locale.ROOT, "%04d", calendar.get(Calendar.YEAR));
            String month = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.MONTH) + 1);
            String week = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.WEEK_OF_YEAR));

            create = create.replace("$year$", year);
            create = create.replace("$month$", month);
            create = create.replace("$week$", week);

            String domain = null;
            if (message.from != null &&
                    message.from.length > 0 &&
                    message.from[0] instanceof InternetAddress) {
                InternetAddress from = (InternetAddress) message.from[0];
                domain = UriHelper.getEmailDomain(from.getAddress());
            }
            create = create.replace("$domain$", domain == null ? "" : domain);

            String name = folder.name + (folder.separator == null ? "" : folder.separator) + create;
            EntityFolder created = db.folder().getFolderByName(folder.account, name);
            if (created == null) {
                created = new EntityFolder();
                created.tbc = true;
                created.account = folder.account;
                created.namespace = folder.namespace;
                created.separator = folder.separator;
                created.name = name;
                created.type = EntityFolder.USER;
                created.subscribed = true;
                created.setProperties();

                EntityAccount account = db.account().getAccount(folder.account);
                created.setSpecials(account);

                created.synchronize = folder.synchronize;
                created.poll = folder.poll;
                created.poll_factor = folder.poll_factor;
                created.download = folder.download;
                created.auto_classify_source = folder.auto_classify_source;
                created.auto_classify_target = folder.auto_classify_target;
                created.sync_days = folder.sync_days;
                created.keep_days = folder.keep_days;
                created.unified = folder.unified;
                created.navigation = folder.navigation;
                created.notify = folder.notify;

                created.id = db.folder().insertFolder(created);
            }
            target = created.id;
        }

        List<EntityMessage> messages = db.message().getMessagesByThread(
                message.account, message.thread, thread ? null : message.id, message.folder);
        for (EntityMessage threaded : messages)
            EntityOperation.queue(context, threaded, EntityOperation.MOVE, target,
                    seen, null, true, false, !TextUtils.isEmpty(create));

        message.ui_hide = true;

        if (seen) {
            message.ui_seen = true;
            message.ui_ignored = true;
        }

        return true;
    }

    private boolean onActionCopy(Context context, EntityMessage message, JSONObject jargs) {
        long target = jargs.optLong("target", -1);

        DB db = DB.getInstance(context);
        EntityFolder folder = db.folder().getFolder(target);
        if (folder == null)
            throw new IllegalArgumentException("Rule copy to folder not found name=" + name);

        EntityOperation.queue(context, message, EntityOperation.COPY, target, false);

        return true;
    }

    private boolean onActionAnswer(Context context, EntityMessage message, JSONObject jargs) {
        DB db = DB.getInstance(context);
        String to = jargs.optString("to");
        boolean resend = jargs.optBoolean("resend");
        boolean attached = jargs.optBoolean("attached");
        boolean attachments = jargs.optBoolean("attachments");

        if (TextUtils.isEmpty(to) &&
                message.auto_submitted != null && message.auto_submitted) {
            EntityLog.log(context, EntityLog.Type.Rules, message,
                    "Auto submitted rule=" + name);
            return false;
        }

        boolean complete = true;

        if (!message.content) {
            complete = false;
            EntityOperation.queue(context, message, EntityOperation.BODY);
        }

        if (attachments)
            for (EntityAttachment attachment : db.attachment().getAttachments(message.id))
                if (!attachment.available) {
                    complete = false;
                    EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.id);
                }

        if (resend && message.headers == null) {
            complete = false;
            EntityOperation.queue(context, message, EntityOperation.HEADERS);
        }

        if (!resend && attached && !Boolean.TRUE.equals(message.raw)) {
            complete = false;
            EntityOperation.queue(context, message, EntityOperation.RAW);
        }

        if (!complete && this.id != null) {
            EntityOperation.queue(context, message, EntityOperation.RULE, this.id);
            return true;
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    answer(context, EntityRule.this, message, jargs);
                } catch (Throwable ex) {
                    db.message().setMessageError(message.id, Log.formatThrowable(ex));
                    Log.w(ex);
                }
            }
        });

        return true;
    }

    private static void answer(Context context, EntityRule rule, EntityMessage message, JSONObject jargs) throws JSONException, AddressException, IOException {
        Log.i("Answering name=" + rule.name);

        DB db = DB.getInstance(context);

        long iid = jargs.getLong("identity");
        long aid = jargs.getLong("answer");
        boolean answer_subject = jargs.optBoolean("answer_subject", false);
        boolean original_text = jargs.optBoolean("original_text", true);
        boolean attachments = jargs.optBoolean("attachments");
        String to = jargs.optString("to");
        boolean resend = jargs.optBoolean("resend");
        boolean attached = jargs.optBoolean("attached");
        boolean cc = jargs.optBoolean("cc");

        boolean isReply = TextUtils.isEmpty(to);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean separate_reply = prefs.getBoolean("separate_reply", false);
        boolean extended_reply = prefs.getBoolean("extended_reply", false);
        boolean quote_reply = prefs.getBoolean("quote_reply", true);
        boolean quote = (quote_reply && isReply);

        EntityIdentity identity = db.identity().getIdentity(iid);
        if (identity == null)
            throw new IllegalArgumentException("Rule identity not found name=" + rule.name);

        EntityAnswer answer;
        if (aid < 0 || resend) {
            if (isReply)
                throw new IllegalArgumentException("Rule template missing name=" + rule.name);

            answer = new EntityAnswer();
            answer.name = message.subject;
            answer.text = "";
        } else {
            answer = db.answer().getAnswer(aid);
            if (answer == null)
                throw new IllegalArgumentException("Rule template not found name=" + rule.name);
        }

        EntityFolder outbox = db.folder().getOutbox();
        if (outbox == null) {
            outbox = EntityFolder.getOutbox();
            outbox.id = db.folder().insertFolder(outbox);
        }

        Address[] from = new InternetAddress[]{new InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name())};

        // Prevent loop
        List<EntityMessage> messages = db.message().getMessagesByThread(
                message.account, message.thread, null, null);
        for (EntityMessage threaded : messages)
            if (!threaded.id.equals(message.id) &&
                    MessageHelper.equal(threaded.from, from)) {
                EntityLog.log(context, EntityLog.Type.Rules, message,
                        "Answer loop" +
                                " name=" + answer.name +
                                " from=" + MessageHelper.formatAddresses(from));
                return;
            }

        EntityMessage reply = new EntityMessage();
        reply.account = message.account;
        reply.folder = outbox.id;
        reply.identity = identity.id;
        reply.msgid = EntityMessage.generateMessageId();

        if (isReply) {
            reply.references = (message.references == null ? "" : message.references + " ") + message.msgid;
            reply.inreplyto = message.msgid;
            reply.thread = message.thread;
            reply.to = (message.reply == null || message.reply.length == 0 ? message.from : message.reply);
        } else {
            if (resend) {
                reply.resend = true;
                reply.headers = message.headers;
            } else
                reply.wasforwardedfrom = message.msgid;
            reply.thread = reply.msgid; // new thread
            reply.to = MessageHelper.parseAddresses(context, to);
        }

        reply.from = from;
        if (cc)
            reply.cc = message.cc;
        if (isReply)
            reply.unsubscribe = "mailto:" + identity.email;
        reply.auto_submitted = true;
        if (resend)
            reply.subject = message.subject;
        else
            reply.subject = EntityMessage.getSubject(context,
                    message.language,
                    answer_subject ? answer.name : message.subject,
                    !isReply);
        reply.received = new Date().getTime();
        reply.sender = MessageHelper.getSortKey(reply.from);

        Uri lookupUri = ContactInfo.getLookupUri(reply.from);
        reply.avatar = (lookupUri == null ? null : lookupUri.toString());

        reply.id = db.message().insertMessage(reply);

        String body;
        if (resend)
            body = Helper.readText(message.getFile(context));
        else {
            body = answer.getHtml(context, message.from);

            if (original_text) {
                Document msg = JsoupEx.parse(body);

                Element div = msg.createElement("div");

                Element p = message.getReplyHeader(context, msg, separate_reply, extended_reply);
                div.appendChild(p);

                Document answering = JsoupEx.parse(message.getFile(context));
                Element e = answering.body();
                if (quote) {
                    String style = e.attr("style");
                    style = HtmlHelper.mergeStyles(style, HtmlHelper.getQuoteStyle(e));
                    e.tagName("blockquote").attr("style", style);
                } else
                    e.tagName("p");
                div.appendChild(e);

                msg.body().appendChild(div);

                body = msg.outerHtml();
            }
        }

        File file = reply.getFile(context);
        Helper.writeText(file, body);
        String text = HtmlHelper.getFullText(body);
        reply.preview = HtmlHelper.getPreview(text);
        reply.language = HtmlHelper.getLanguage(context, reply.subject, text);
        db.message().setMessageContent(reply.id,
                true,
                reply.language,
                0,
                reply.preview,
                null);

        if (attachments || resend)
            EntityAttachment.copy(context, message.id, reply.id);

        if (!resend && attached) {
            EntityAttachment attachment = new EntityAttachment();
            attachment.message = reply.id;
            attachment.sequence = db.attachment().getAttachmentSequence(reply.id) + 1;
            attachment.name = "email.eml";
            attachment.type = "message/rfc822";
            attachment.disposition = Part.ATTACHMENT;
            attachment.progress = 0;
            attachment.id = db.attachment().insertAttachment(attachment);

            File source = message.getRawFile(context);
            File target = attachment.getFile(context);
            Helper.copy(source, target);
            db.attachment().setDownloaded(attachment.id, target.length());
        }

        EntityOperation.queue(context, reply, EntityOperation.SEND);

        // Batch send operations, wait until after commit
        ServiceSend.schedule(context, SEND_DELAY);
    }

    private boolean onActionAutomation(Context context, EntityMessage message, JSONObject jargs) {
        String sender = (message.from == null || message.from.length == 0
                ? null : ((InternetAddress) message.from[0]).getAddress());

        // ISO 8601
        DateFormat DTF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DTF.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

        Intent automation = new Intent(ACTION_AUTOMATION);
        automation.putExtra(EXTRA_RULE, name);
        automation.putExtra(EXTRA_SENDER, sender);
        automation.putExtra(EXTRA_SUBJECT, message.subject);
        automation.putExtra(EXTRA_RECEIVED, DTF.format(message.received));

        List<String> extras = Log.getExtras(automation.getExtras());
        EntityLog.log(context, EntityLog.Type.Rules, message,
                "Sending " + automation + " " + TextUtils.join(" ", extras));
        context.sendBroadcast(automation);

        return true;
    }

    private boolean onActionTts(Context context, EntityMessage message, JSONObject jargs) {
        DB db = DB.getInstance(context);

        if (message.ui_seen)
            return false;

        if (!message.content && this.id != null) {
            EntityOperation.queue(context, message, EntityOperation.BODY);
            EntityOperation.queue(context, message, EntityOperation.RULE, this.id);
            return true;
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (MediaPlayerHelper.isInCall(context) || MediaPlayerHelper.isDnd(context))
                        return;
                    speak(context, EntityRule.this, message);
                } catch (Throwable ex) {
                    db.message().setMessageError(message.id, Log.formatThrowable(ex));
                    Log.w(ex);
                }
            }
        });

        return true;
    }

    private static void speak(Context context, EntityRule rule, EntityMessage message) throws IOException {
        Log.i("Speaking name=" + rule.name);

        if (message.ui_seen)
            return;

        Locale locale = (message.language == null ? Locale.getDefault() : new Locale(message.language));

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        Resources res = context.createConfigurationContext(configuration).getResources();

        StringBuilder sb = new StringBuilder();
        sb.append(res.getString(R.string.title_rule_tts_prefix)).append(". ");

        if (message.from != null && message.from.length > 0)
            sb.append(res.getString(R.string.title_rule_tts_from))
                    .append(' ').append(MessageHelper.formatAddressesShort(message.from)).append(". ");

        if (!TextUtils.isEmpty(message.subject))
            sb.append(res.getString(R.string.title_rule_tts_subject))
                    .append(' ').append(message.subject).append(". ");

        String body = Helper.readText(message.getFile(context));
        String text = HtmlHelper.getFullText(body);
        String preview = HtmlHelper.getPreview(text);

        if (!TextUtils.isEmpty(preview))
            sb.append(res.getString(R.string.title_rule_tts_content))
                    .append(' ').append(preview);

        TTSHelper.speak(context, "rule:" + message.id, sb.toString(), locale);
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

        EntityOperation.queue(context, message, EntityOperation.FLAG, true, color, false);

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

        EntityOperation.queue(context, message, EntityOperation.KEYWORD,
                MessageHelper.FLAG_LOW_IMPORTANCE, EntityMessage.PRIORITIY_LOW.equals(importance));
        EntityOperation.queue(context, message, EntityOperation.KEYWORD,
                MessageHelper.FLAG_HIGH_IMPORTANCE, EntityMessage.PRIORITIY_HIGH.equals(importance));

        message.importance = importance;

        return true;
    }

    private boolean onActionKeyword(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        String keyword = jargs.getString("keyword");
        if (TextUtils.isEmpty(keyword))
            throw new IllegalArgumentException("Keyword missing rule=" + name);

        EntityOperation.queue(context, message, EntityOperation.KEYWORD, keyword, true);

        return true;
    }

    private boolean onActionDelete(Context context, EntityMessage message, JSONObject jargs) {
        EntityOperation.queue(context, message, EntityOperation.DELETE);

        return true;
    }

    private boolean onActionSound(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        Log.i("Speaking name=" + name);

        if (message.ui_seen)
            return false;

        Uri uri = Uri.parse(jargs.getString("uri"));
        boolean alarm = jargs.getBoolean("alarm");
        int duration = jargs.optInt("duration", MediaPlayerHelper.DEFAULT_ALARM_DURATION);

        DB db = DB.getInstance(context);

        message.ui_silent = true;
        db.message().setMessageUiSilent(message.id, message.ui_silent);

        MediaPlayerHelper.queue(context, uri, alarm, duration);

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

    @NonNull
    static List<EntityRule> blockSender(Context context, EntityMessage message, EntityFolder junk, boolean block_domain) throws JSONException {
        List<EntityRule> rules = new ArrayList<>();

        if (message.from == null)
            return rules;

        List<String> domains = new ArrayList<>();
        for (Address from : message.from) {
            String sender = ((InternetAddress) from).getAddress();
            String name = MessageHelper.formatAddresses(new Address[]{from});

            if (TextUtils.isEmpty(sender) ||
                    !Helper.EMAIL_ADDRESS.matcher(sender).matches())
                continue;

            boolean regex = false;
            if (block_domain) {
                String domain = UriHelper.getEmailDomain(sender);
                if (!TextUtils.isEmpty(domain) && !domains.contains(domain)) {
                    domains.add(domain);
                    regex = true;
                    sender = ".*@.*" + Pattern.quote(domain) + ".*";
                }
            }

            JSONObject jsender = new JSONObject();
            jsender.put("value", sender);
            jsender.put("regex", regex);

            JSONObject jcondition = new JSONObject();
            jcondition.put("sender", jsender);

            JSONObject jaction = new JSONObject();
            jaction.put("type", TYPE_MOVE);
            jaction.put("target", junk.id);
            jaction.put("seen", true);

            EntityRule rule = new EntityRule();
            rule.folder = message.folder;
            rule.name = context.getString(R.string.title_block, name);
            rule.order = 1000;
            rule.enabled = true;
            rule.stop = true;
            rule.condition = jcondition.toString();
            rule.action = jaction.toString();

            rules.add(rule);
        }

        return rules;
    }

    boolean isBlockingSender(EntityMessage message, EntityFolder junk) throws JSONException {
        if (message.from == null || message.from.length == 0)
            return false;

        String sender = ((InternetAddress) message.from[0]).getAddress();
        if (sender == null)
            return false;

        JSONObject jcondition = new JSONObject(condition);
        if (!jcondition.has("sender"))
            return false;
        JSONObject jsender = jcondition.getJSONObject("sender");
        String value = jsender.optString("value");
        boolean regex = jsender.optBoolean("regex");

        if (regex) {
            Pattern pattern = Pattern.compile(value, Pattern.DOTALL);
            if (!pattern.matcher(sender).matches())
                return false;
        } else {
            int at = sender.indexOf('@');
            String domain = (at < 0 ? sender : sender.substring(at));
            if (!sender.equals(value) && !domain.equals(value))
                return false;
        }

        JSONObject jaction = new JSONObject(action);
        if (jaction.optInt("type", -1) != TYPE_MOVE)
            return false;
        if (jaction.optLong("target", -1) != junk.id)
            return false;

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityRule) {
            EntityRule other = (EntityRule) obj;
            return Objects.equals(this.uuid, other.uuid) &&
                    this.folder.equals(other.folder) &&
                    this.name.equals(other.name) &&
                    this.order == other.order &&
                    this.enabled == other.enabled &&
                    this.stop == other.stop &&
                    this.condition.equals(other.condition) &&
                    this.action.equals(other.action) &&
                    this.applied.equals(other.applied) &&
                    Objects.equals(this.last_applied, other.last_applied);
        } else
            return false;
    }

    boolean matches(String query) {
        if (this.name.toLowerCase().contains(query))
            return true;

        try {
            JSONObject jcondition = new JSONObject(this.condition);
            JSONObject jaction = new JSONObject(this.action);
            JSONObject jmerged = new JSONObject();
            jmerged.put("condition", jcondition);
            jmerged.put("action", jaction);
            return contains(jmerged, query);
        } catch (JSONException ex) {
            Log.e(ex);
        }

        return false;
    }

    private boolean contains(JSONObject jobject, String query) throws JSONException {
        Iterator<String> keys = jobject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jobject.get(key);
            if (value instanceof JSONObject) {
                if (contains((JSONObject) value, query))
                    return true;
            } else {
                if (value.toString().toLowerCase().contains(query))
                    return true;
            }
        }

        return false;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("uuid", uuid);
        json.put("name", name);
        json.put("order", order);
        json.put("enabled", enabled);
        json.put("stop", stop);
        json.put("condition", condition);
        json.put("action", action);
        json.put("applied", applied);
        json.put("last_applied", last_applied);
        return json;
    }

    public static EntityRule fromJSON(JSONObject json) throws JSONException {
        EntityRule rule = new EntityRule();
        // id
        if (json.has("uuid"))
            rule.uuid = json.getString("uuid");
        rule.name = json.getString("name");
        rule.order = json.getInt("order");
        rule.enabled = json.getBoolean("enabled");
        rule.stop = json.getBoolean("stop");
        rule.condition = json.getString("condition");
        rule.action = json.getString("action");
        rule.applied = json.optInt("applied", 0);
        if (json.has("last_applied") && !json.isNull("last_applied"))
            rule.last_applied = json.getLong("last_applied");
        return rule;
    }
}
