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

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.net.ssl.HttpsURLConnection;

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
    public String group;
    @NonNull
    public int order;
    @NonNull
    public boolean enabled;
    @NonNull
    public boolean daily;
    @NonNull
    public boolean stop;
    @NonNull
    public String condition;
    @NonNull
    public String action;
    @NonNull
    public Integer applied = 0;
    public Long last_applied;

    @Ignore
    public boolean async;

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
    static final int TYPE_LOCAL_ONLY = 17;
    static final int TYPE_NOTES = 18;
    static final int TYPE_URL = 19;
    static final int TYPE_SILENT = 20;
    static final int TYPE_SUMMARIZE = 21;

    static final String ACTION_AUTOMATION = BuildConfig.APPLICATION_ID + ".AUTOMATION";
    static final String EXTRA_RULE = "rule";
    static final String EXTRA_RECEIVED = "received";
    static final String EXTRA_SENDER = "sender";
    static final String EXTRA_NAME = "name";
    static final String EXTRA_SUBJECT = "subject";
    static final String EXTRA_PREVIEW = "preview";

    static final String[] EXTRA_ALL = new String[]{
            EXTRA_RULE, EXTRA_SENDER, EXTRA_NAME, EXTRA_SUBJECT, EXTRA_RECEIVED
    };

    static final String JSOUP_PREFIX = "jsoup:";
    private static final long SEND_DELAY = 5000L; // milliseconds
    private static final int MAX_NOTES_LENGTH = 512; // characters
    private static final int URL_TIMEOUT = 15 * 1000; // milliseconds

    static boolean needsHeaders(EntityMessage message, List<EntityRule> rules) {
        return needsHeaders(rules);
    }

    static boolean needsHeaders(List<EntityRule> rules) {
        return needs(rules, "header");
    }

    static boolean needsBody(EntityMessage message, List<EntityRule> rules) {
        if (message.encrypt != null && !EntityMessage.ENCRYPT_NONE.equals(message.encrypt))
            return false;
        return needsBody(rules);
    }

    static boolean needsBody(List<EntityRule> rules) {
        return needs(rules, "body") || needs(rules, "notes_jsoup");
    }

    private static boolean needs(List<EntityRule> rules, String what) {
        for (EntityRule rule : rules)
            try {
                JSONObject jaction = new JSONObject(rule.action);
                int type = jaction.getInt("type");
                if (type == TYPE_SUMMARIZE)
                    return true;

                JSONObject jcondition = new JSONObject(rule.condition);

                if (jcondition.has(what)) {
                    if ("header".equals(what)) {
                        JSONObject jheader = jcondition.getJSONObject("header");
                        String value = jheader.getString("value");
                        boolean regex = jheader.getBoolean("regex");
                        if (!regex && value.startsWith("$$") && value.endsWith("$"))
                            continue;
                    }
                    return true;
                }

                if (jcondition.has("expression")) {
                    Expression expression = ExpressionHelper.getExpression(rule, null, null, null, null);
                    if (expression != null) {
                        if ("header".equals(what) && ExpressionHelper.needsHeaders(expression))
                            return true;
                        if ("body".equals(what) && ExpressionHelper.needsBody(expression))
                            return true;
                    }
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }

        return false;
    }

    static int run(Context context, List<EntityRule> rules,
                   EntityMessage message, boolean browsed, List<Header> headers, String html)
            throws JSONException, MessagingException, IOException {
        int applied = 0;

        List<String> stopped = new ArrayList<>();
        for (EntityRule rule : rules) {
            if (rule.group != null && stopped.contains(rule.group))
                continue;
            if (rule.matches(context, message, headers, html)) {
                if (rule.execute(context, message, browsed, html))
                    applied++;
                if (rule.stop)
                    if (rule.group == null)
                        break;
                    else {
                        if (!stopped.contains(rule.group))
                            stopped.add(rule.group);
                    }
            }
        }

        return applied;
    }

    boolean matches(Context context, EntityMessage message, List<Header> headers, String html) throws MessagingException {
        try {
            JSONObject jcondition = new JSONObject(condition);

            // general
            int age = 0;
            if (this.daily) {
                JSONObject jgeneral = jcondition.optJSONObject("general");
                if (jgeneral != null) {
                    age = jgeneral.optInt("age");
                    if (age > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(message.received);
                        cal.add(Calendar.DAY_OF_MONTH, age);
                        if (cal.getTimeInMillis() > new Date().getTime())
                            return false;
                    }
                }
            }

            // Sender
            JSONObject jsender = jcondition.optJSONObject("sender");
            if (jsender != null) {
                boolean not = jsender.optBoolean("not");
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
                if (matches == not)
                    return false;
            }

            // Recipient
            JSONObject jrecipient = jcondition.optJSONObject("recipient");
            if (jrecipient != null) {
                boolean not = jrecipient.optBoolean("not");
                String value = jrecipient.getString("value");
                boolean regex = jrecipient.getBoolean("regex");

                boolean matches = false;
                List<Address> recipients = new ArrayList<>();
                if (message.to != null)
                    recipients.addAll(Arrays.asList(message.to));
                if (message.cc != null)
                    recipients.addAll(Arrays.asList(message.cc));
                if (message.bcc != null)
                    recipients.addAll(Arrays.asList(message.bcc));
                for (Address recipient : recipients) {
                    InternetAddress ia = (InternetAddress) recipient;
                    String personal = ia.getPersonal();
                    String formatted = ((personal == null ? "" : personal + " ") + "<" + ia.getAddress() + ">");
                    if (matches(context, message, value, formatted, regex)) {
                        matches = true;
                        break;
                    }
                }
                if (matches == not)
                    return false;
            }

            // Subject
            JSONObject jsubject = jcondition.optJSONObject("subject");
            if (jsubject != null) {
                boolean not = jsubject.optBoolean("not");
                String value = jsubject.getString("value");
                boolean regex = jsubject.getBoolean("regex");

                if (matches(context, message, value, message.subject, regex) == not)
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
                boolean not = jheader.optBoolean("not");
                String value = jheader.getString("value");
                boolean regex = jheader.getBoolean("regex");

                if (!regex &&
                        value.startsWith("$") &&
                        value.endsWith("$")) {
                    if (matchKeywords(context, message, value) != not)
                        return false;
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
                    if (matches == not)
                        return false;
                }
            }

            // Body
            JSONObject jbody = jcondition.optJSONObject("body");
            if (jbody != null) {
                boolean not = jbody.optBoolean("not");
                String value = jbody.getString("value");
                boolean regex = jbody.getBoolean("regex");
                boolean skip_quotes = jbody.optBoolean("skip_quotes");

                boolean jsoup = value.startsWith(JSOUP_PREFIX);

                if (!regex && !jsoup)
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
                    if (false && (message.encrypt == null || EntityMessage.ENCRYPT_NONE.equals(message.encrypt)))
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_no_body));
                    else
                        return false;

                Document d = JsoupEx.parse(html);
                if (skip_quotes)
                    HtmlHelper.removeQuotes(d, true);
                if (jsoup) {
                    String selector = value.substring(JSOUP_PREFIX.length());
                    if (d.select(selector).isEmpty() != not)
                        return false;
                } else {
                    String text = d.body().text();
                    if (matches(context, message, value, text, regex) == not)
                        return false;
                }
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
                boolean all = jschedule.optBoolean("all", false);
                int start = jschedule.optInt("start", 0);
                int end = jschedule.optInt("end", 0);

                Calendar cal_start = getRelativeCalendar(all, start, message.received);
                Calendar cal_end = getRelativeCalendar(all, end, message.received);

                if (cal_start.getTimeInMillis() > cal_end.getTimeInMillis())
                    if (all)
                        if (cal_end.getTimeInMillis() < message.received)
                            cal_end.add(Calendar.DATE, 1);
                        else
                            cal_start.add(Calendar.DATE, -1);
                    else
                        cal_start.add(Calendar.HOUR_OF_DAY, -7 * 24);

                if (message.received < cal_start.getTimeInMillis() ||
                        message.received > cal_end.getTimeInMillis())
                    return false;
            }

            // Younger
            if (jcondition.has("younger")) {
                int younger = jcondition.getInt("younger");
                Calendar y = Calendar.getInstance();
                y.add(Calendar.HOUR_OF_DAY, -younger);
                if (message.received < y.getTimeInMillis())
                    return false;
            }

            // Expression
            Expression expression = ExpressionHelper.getExpression(this, message, headers, html, context);
            if (expression != null) {
                if (ExpressionHelper.needsHeaders(expression) && headers == null && message.headers == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_rule_no_headers));

                Log.i("EXPR evaluating='" + jcondition.getString("expression") + "'");
                EvaluationValue val = expression.evaluate();
                Boolean result = val.getBooleanValue();
                Log.i("EXPR evaluated=" + result + " value=" + val);
                if (!Boolean.TRUE.equals(result))
                    return false;
            }

            // Safeguard
            if (age == 0 &&
                    jsender == null &&
                    jrecipient == null &&
                    jsubject == null &&
                    !jcondition.optBoolean("attachments") &&
                    jheader == null &&
                    jbody == null &&
                    jdate == null &&
                    jschedule == null &&
                    !jcondition.has("younger") &&
                    !jcondition.has("expression"))
                return false;
        } catch (JSONException | ParseException | EvaluationException ex) {
            Log.e(ex);
            return false;
        }

        return true;
    }

    private static boolean matchKeywords(Context context, EntityMessage message, String value) {
        String keyword = value.substring(1, value.length() - 1);

        if ("$tls".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.tls))
                return true;
        } else if ("$aligned".equals(keyword)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean native_dkim = prefs.getBoolean("native_dkim", false);
            if (!native_dkim)
                return true;
            if (message.signedby == null)
                return true;
            if (message.from == null || message.from.length != 1)
                return true;
            String domain = UriHelper.getEmailDomain(((InternetAddress) message.from[0]).getAddress());
            if (domain == null)
                return true;
            boolean valid = false;
            for (String signer : message.signedby.split(","))
                if (Objects.equals(
                        UriHelper.getRootDomain(context, signer),
                        UriHelper.getRootDomain(context, domain))) {
                    valid = true;
                    break;
                }
            if (!valid)
                return true;
        } else if ("$dkim".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.dkim))
                return true;
        } else if ("$spf".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.spf))
                return true;
        } else if ("$dmarc".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.dmarc))
                return true;
        } else if ("$auth".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.auth))
                return true;
        } else if ("$mx".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.mx))
                return true;
        } else if ("$blocklist".equals(keyword)) {
            if (!Boolean.FALSE.equals(message.blocklist))
                return true;
        } else if ("$replydomain".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.reply_domain))
                return true;
        } else if ("$nofrom".equals(keyword)) {
            if (message.from != null && message.from.length > 0)
                return true;
        } else if ("$multifrom".equals(keyword)) {
            if (message.from == null || message.from.length < 2)
                return true;
        } else if ("$automatic".equals(keyword)) {
            if (!Boolean.TRUE.equals(message.auto_submitted))
                return true;
        } else if ("$lowpriority".equals(keyword)) {
            if (!EntityMessage.PRIORITIY_LOW.equals(message.priority))
                return true;
        } else if ("$highpriority".equals(keyword)) {
            if (!EntityMessage.PRIORITIY_HIGH.equals(message.priority))
                return true;
        } else if ("$signed".equals(keyword)) {
            if (!message.isSigned())
                return true;
        } else if ("$encrypted".equals(keyword)) {
            if (!message.isEncrypted())
                return true;
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
                return true;
        }

        return false;
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
                    "Rule=" + name + "@" + order + " matched " +
                            " needle=" + needle + " haystack=" + haystack + " regex=" + regex);
        else
            Log.i("Rule=" + name + "@" + order + " matched=" + matched +
                    " needle=" + needle + " haystack=" + haystack + " regex=" + regex);
        return matched;
    }

    boolean execute(Context context, EntityMessage message, boolean browsed, String html) throws JSONException, IOException {
        boolean executed = _execute(context, message, browsed, html);
        if (this.id != null && executed) {
            DB db = DB.getInstance(context);
            db.rule().applyRule(id, new Date().getTime());
        }
        return executed;
    }

    private boolean _execute(Context context, EntityMessage message, boolean browsed, String html) throws JSONException, IllegalArgumentException, IOException {
        JSONObject jaction = new JSONObject(action);
        int type = jaction.getInt("type");
        EntityLog.log(context, EntityLog.Type.Rules, message,
                "Executing rule=" + type + ":" + this.name + "@" + this.order);

        switch (type) {
            case TYPE_NOOP:
                return true;
            case TYPE_SEEN:
                return onActionSeen(context, message, true);
            case TYPE_UNSEEN:
                return onActionSeen(context, message, false);
            case TYPE_HIDE:
                return onActionHide(context, message, jaction);
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
                return onActionAnswer(context, message, browsed, jaction);
            case TYPE_TTS:
                return onActionTts(context, message, browsed, jaction);
            case TYPE_AUTOMATION:
                return onActionAutomation(context, message, jaction);
            case TYPE_DELETE:
                return onActionDelete(context, message, jaction);
            case TYPE_SOUND:
                return onActionSound(context, message, browsed, jaction);
            case TYPE_LOCAL_ONLY:
                return onActionLocalOnly(context, message, jaction);
            case TYPE_NOTES:
                return onActionNotes(context, message, jaction, html);
            case TYPE_URL:
                return onActionUrl(context, message, jaction, html);
            case TYPE_SILENT:
                return onActionSilent(context, message, jaction);
            case TYPE_SUMMARIZE:
                return onActionSummarize(context, message, browsed, jaction);
            default:
                throw new IllegalArgumentException("Unknown rule type=" + type + " name=" + name);
        }
    }

    void validate(Context context) throws JSONException, IllegalArgumentException {
        try {
            Expression expression = ExpressionHelper.getExpression(this, null, null, null, context);
            if (expression != null)
                ExpressionHelper.check(expression);
        } catch (ParseException | MessagingException ex) {
            Log.w("EXPR", ex);
            String message = ex.getMessage();
            if (TextUtils.isEmpty(message))
                message = "Invalid expression";
            throw new IllegalArgumentException(message, ex);
        }

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
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_answer_missing));
                }
                return;
            case TYPE_TTS:
                return;
            case TYPE_AUTOMATION:
                return;
            case TYPE_DELETE:
                return;
            case TYPE_SOUND:
                return;
            case TYPE_LOCAL_ONLY:
                return;
            case TYPE_NOTES:
                String notes = jargs.optString("notes");
                if (TextUtils.isEmpty(notes))
                    throw new IllegalArgumentException(context.getString(R.string.title_rule_notes_missing));
                return;
            case TYPE_URL:
                String url = jargs.optString("url");
                if (TextUtils.isEmpty(url) || !Patterns.WEB_URL.matcher(url).matches())
                    throw new IllegalArgumentException(context.getString(R.string.title_rule_url_missing));
                return;
            case TYPE_SILENT:
                return;
            case TYPE_SUMMARIZE:
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

    private boolean onActionHide(Context context, EntityMessage message, JSONObject jargs) {
        boolean seen = jargs.optBoolean("seen");

        DB db = DB.getInstance(context);

        EntityFolder folder = db.folder().getFolder(message.folder);
        if (EntityFolder.DRAFTS.equals(folder.type))
            return false;

        db.message().setMessageSnoozed(message.id, Long.MAX_VALUE);
        db.message().setMessageUiIgnored(message.id, true);
        EntityMessage.snooze(context, message.id, Long.MAX_VALUE);

        message.ui_snoozed = Long.MAX_VALUE;
        message.ui_ignored = true;

        return (!seen || onActionSeen(context, message, true));
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
            calendar.setTimeInMillis(message.received);
            String year = String.format(Locale.ROOT, "%04d", calendar.get(Calendar.YEAR));
            String month = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.MONTH) + 1);
            String week = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.WEEK_OF_YEAR));
            String day = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.DAY_OF_MONTH));

            create = create.replace("$year$", year);
            create = create.replace("$month$", month);
            create = create.replace("$week$", week);
            create = create.replace("$day$", day);

            String user = null;
            String extra = null;
            String domain = null;
            if (message.from != null &&
                    message.from.length > 0 &&
                    message.from[0] instanceof InternetAddress) {
                InternetAddress from = (InternetAddress) message.from[0];
                user = UriHelper.getEmailUser(from.getAddress());
                domain = UriHelper.getEmailDomain(from.getAddress());
                if (user != null) {
                    int plus = user.indexOf('+');
                    if (plus > 0)
                        extra = user.substring(plus + 1);
                }
            }
            create = create.replace("$user$", user == null ? "" : user);
            create = create.replace("$extra$", extra == null ? "" : extra);
            create = create.replace("$domain$", domain == null ? "" : domain);

            if (create.contains("$group$")) {
                EntityContact local = null;
                if (message.from != null && message.from.length == 1) {
                    String email = ((InternetAddress) message.from[0]).getAddress();
                    if (!TextUtils.isEmpty(email))
                        local = db.contact().getContact(message.account, EntityContact.TYPE_FROM, email);
                }

                if (local != null && !TextUtils.isEmpty(local.group)) {
                    Log.i(this.name + " local group=" + local.group);
                    create = create.replace("$group$", local.group);
                } else {
                    if (!Helper.hasPermission(context, Manifest.permission.READ_CONTACTS))
                        return false;
                    Log.i(this.name + " lookup=" + message.avatar);
                    if (message.avatar == null)
                        return false;

                    ContentResolver resolver = context.getContentResolver();
                    try (Cursor contact = resolver.query(Uri.parse(message.avatar),
                            new String[]{ContactsContract.Contacts._ID}, null, null, null)) {
                        Log.i(this.name + " contacts=" + contact.getCount());
                        if (!contact.moveToNext())
                            return false;

                        long contactId = contact.getLong(0);
                        Log.i(this.name + " contactId=" + contactId);

                        try (Cursor membership = resolver.query(ContactsContract.Data.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID},
                                ContactsContract.Data.MIMETYPE + "= ? AND " +
                                        ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID + "= ?",
                                new String[]{
                                        ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                                        Long.toString((contactId))
                                },
                                null)) {
                            Log.i(this.name + " membership=" + membership.getCount());

                            int count = 0;
                            String groupName = null;
                            while (membership.moveToNext()) {
                                long groupId = membership.getLong(0);
                                try (Cursor groups = resolver.query(ContactsContract.Groups.CONTENT_URI,
                                        new String[]{
                                                ContactsContract.Groups.TITLE,
                                                ContactsContract.Groups.AUTO_ADD,
                                                ContactsContract.Groups.GROUP_VISIBLE,
                                        },
                                        ContactsContract.Groups._ID + " = ?",
                                        new String[]{Long.toString(groupId)},
                                        ContactsContract.Groups.TITLE)) {
                                    while (groups.moveToNext()) {
                                        groupName = groups.getString(0);
                                        int auto_add = groups.getInt(1);
                                        int visible = groups.getInt(2);
                                        if (auto_add == 0)
                                            count++;
                                        Log.i(this.name + " membership groupId=" + groupId +
                                                " name=" + groupName + " auto_add=" + auto_add + " visible=" + visible);
                                    }
                                }
                            }
                            Log.i(this.name + " name=" + groupName + " count=" + count);
                            if (count == 1)
                                create = create.replace("$group$", groupName);
                            else
                                return false;
                        }
                    }
                }
            }

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

    private boolean onActionAnswer(Context context, EntityMessage message, boolean browsed, JSONObject jargs) {
        DB db = DB.getInstance(context);
        String to = jargs.optString("to");
        boolean resend = jargs.optBoolean("resend");
        boolean attached = jargs.optBoolean("attached");
        boolean attachments = jargs.optBoolean("attachments");
        boolean checks = jargs.optBoolean("checks", true);

        if (TextUtils.isEmpty(to)) {
            if (checks && Boolean.TRUE.equals(message.auto_submitted)) {
                EntityLog.log(context, EntityLog.Type.Rules, message, "Auto submitted rule=" + name);
                return false;
            }

            Address[] recipients = (message.reply == null || message.reply.length == 0 ? message.from : message.reply);
            if (recipients.length == 0) {
                EntityLog.log(context, EntityLog.Type.Rules, message, "No recipients rule=" + name);
                return false;
            }

            if (checks && MessageHelper.isNoReply(recipients)) {
                EntityLog.log(context, EntityLog.Type.Rules, message, "No-reply rule=" + name);
                return false;
            }
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
            EntityOperation.queue(context, message, EntityOperation.RULE, this.id, browsed);
            return true;
        }

        Helper.getSerialExecutor().submit(new Runnable() {
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

        EntityFolder outbox = EntityFolder.getOutbox(context);

        Address[] from = new InternetAddress[]{new InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name())};

        // Prevent loop
        if (isReply) {
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
        EntityAnswer.Data answerData = null;
        if (resend)
            body = Helper.readText(message.getFile(context));
        else {
            answerData = answer.getData(context, message.from);
            body = answerData.getHtml();

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
        String text = HtmlHelper.getFullText(context, body);
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

        if (answerData != null)
            answerData.insertAttachments(context, reply.id);

        EntityOperation.queue(context, reply, EntityOperation.SEND);

        // Batch send operations, wait until after commit
        ServiceSend.schedule(context, SEND_DELAY);
    }

    private boolean onActionAutomation(Context context, EntityMessage message, JSONObject jargs) {
        InternetAddress iaddr =
                (message.from == null || message.from.length == 0
                        ? null : ((InternetAddress) message.from[0]));

        // ISO 8601
        DateFormat DTF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DTF.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

        Intent automation = new Intent(ACTION_AUTOMATION);
        automation.putExtra(EXTRA_RULE, name);
        automation.putExtra(EXTRA_RECEIVED, DTF.format(message.received));
        automation.putExtra(EXTRA_SENDER, iaddr == null ? null : iaddr.getAddress());
        automation.putExtra(EXTRA_NAME, iaddr == null ? null : iaddr.getPersonal());
        automation.putExtra(EXTRA_SUBJECT, message.subject);
        automation.putExtra(EXTRA_PREVIEW, message.preview);

        List<String> extras = Log.getExtras(automation.getExtras());
        EntityLog.log(context, EntityLog.Type.Rules, message,
                "Sending " + automation + " " + TextUtils.join(" ", extras));
        context.sendBroadcast(automation);

        return true;
    }

    private boolean onActionTts(Context context, EntityMessage message, boolean browsed, JSONObject jargs) {
        DB db = DB.getInstance(context);

        if (message.ui_seen || browsed)
            return false;

        if (!message.content && this.id != null) {
            EntityOperation.queue(context, message, EntityOperation.BODY);
            EntityOperation.queue(context, message, EntityOperation.RULE, this.id, browsed);
            return true;
        }

        Helper.getMediaTaskExecutor().submit(new Runnable() {
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
        if (Helper.isPlayStoreInstall())
            throw new IllegalArgumentException("TTS is available in the GitHub version only because Google doesn't allow it in the Play Store :-(");

        Log.i("Speaking name=" + rule.name);

        if (message.ui_seen)
            return;

        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.title_rule_tts_prefix)).append(". ");

        if (message.from != null && message.from.length > 0)
            sb.append(context.getString(R.string.title_rule_tts_from))
                    .append(' ').append(MessageHelper.formatAddressesShort(message.from)).append(". ");

        if (!TextUtils.isEmpty(message.subject))
            sb.append(context.getString(R.string.title_rule_tts_subject))
                    .append(' ').append(message.subject).append(". ");

        String body = Helper.readText(message.getFile(context));
        String text = HtmlHelper.getFullText(context, body);
        String preview = HtmlHelper.getPreview(text);

        if (!TextUtils.isEmpty(preview))
            sb.append(context.getString(R.string.title_rule_tts_content))
                    .append(' ').append(preview);

        Intent intent = new Intent(context, ServiceTTS.class)
                .setAction("tts:" + message.id)
                .putExtra(ServiceTTS.EXTRA_FLUSH, false)
                .putExtra(ServiceTTS.EXTRA_TEXT, sb.toString())
                .putExtra(ServiceTTS.EXTRA_LANGUAGE, message.language)
                .putExtra(ServiceTTS.EXTRA_UTTERANCE_ID, "rule:" + message.id);
        context.startService(intent);
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
            Calendar cal = getRelativeCalendar(false, end, message.received);
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
        boolean set = jargs.optBoolean("set", true);
        if (TextUtils.isEmpty(keyword))
            throw new IllegalArgumentException("Keyword missing rule=" + name);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(message.received);
        String year = String.format(Locale.ROOT, "%04d", calendar.get(Calendar.YEAR));
        String month = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.MONTH) + 1);
        String week = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.WEEK_OF_YEAR));
        String day = String.format(Locale.ROOT, "%02d", calendar.get(Calendar.DAY_OF_MONTH));

        keyword = keyword.replace("$year$", year);
        keyword = keyword.replace("$month$", month);
        keyword = keyword.replace("$week$", week);
        keyword = keyword.replace("$day$", day);

        EntityOperation.queue(context, message, EntityOperation.KEYWORD, keyword, set);

        return true;
    }

    private boolean onActionDelete(Context context, EntityMessage message, JSONObject jargs) {
        EntityOperation.queue(context, message, EntityOperation.DELETE);

        return true;
    }

    private boolean onActionSound(Context context, EntityMessage message, boolean browsed, JSONObject jargs) throws JSONException {
        Uri uri = (jargs.has("uri") ? Uri.parse(jargs.getString("uri")) : null);
        boolean loop = jargs.optBoolean("loop");
        boolean alarm = jargs.optBoolean("alarm");
        int duration = jargs.optInt("duration", MediaPlayerHelper.DEFAULT_ALARM_DURATION);
        Log.i("Sound uri=" + uri + " loop=" + loop + " alarm=" + alarm + " duration=" + duration);

        if (browsed)
            return false;

        DB db = DB.getInstance(context);

        message.ui_silent = true;
        db.message().setMessageUiSilent(message.id, message.ui_silent);

        if (uri != null)
            MediaPlayerHelper.queue(context, uri, loop, alarm, duration);

        return true;
    }

    private boolean onActionLocalOnly(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        if (message.ui_seen)
            return false;

        DB db = DB.getInstance(context);

        message.ui_local_only = true;
        db.message().setMessageUiLocalOnly(message.id, message.ui_local_only);

        return true;
    }

    private boolean onActionNotes(Context context, EntityMessage message, JSONObject jargs, String html) throws JSONException {
        String notes = jargs.getString("notes");
        Integer color = (jargs.has("color") ? jargs.getInt("color") : null);

        if (notes.startsWith(JSOUP_PREFIX)) {
            if (html == null && message.content) {
                File file = message.getFile(context);
                try {
                    html = Helper.readText(file);
                } catch (IOException ex) {
                    Log.e(ex);
                }
            }

            if (html != null) {
                Document d = JsoupEx.parse(html);
                String selector = notes.substring(JSOUP_PREFIX.length());
                String regex = null;
                if (selector.endsWith(("}"))) {
                    int b = selector.lastIndexOf('{');
                    if (b > 0) {
                        regex = selector.substring(b + 1, selector.length() - 1);
                        selector = selector.substring(0, b);
                    }
                }

                Element e = d.select(selector).first();
                if (e == null) {
                    notes = null;
                    Log.w("Nothing selected Jsoup=" + selector);
                } else {
                    notes = e.ownText();
                    if (!TextUtils.isEmpty(regex)) {
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(notes);
                        if (m.matches() && m.groupCount() > 0)
                            notes = m.group(1);
                        else
                            Log.w("Nothing selected regex=" + regex + " value=" + notes);
                    }
                }
            }
        }

        if (TextUtils.isEmpty(notes))
            notes = null;
        else if (notes.length() > MAX_NOTES_LENGTH)
            notes = notes.substring(0, MAX_NOTES_LENGTH);

        DB db = DB.getInstance(context);
        db.message().setMessageNotes(message.id, notes, color);

        return true;
    }

    private boolean onActionUrl(Context context, EntityMessage message, JSONObject jargs, String html) throws JSONException, IOException {
        String url = jargs.getString("url");
        String method = jargs.optString("method");
        String body = (jargs.isNull("body") ? null : jargs.optString("body"));

        if (TextUtils.isEmpty(method))
            method = "GET";

        InternetAddress iaddr =
                (message.from == null || message.from.length == 0
                        ? null : ((InternetAddress) message.from[0]));
        String address = (iaddr == null ? null : iaddr.getAddress());
        String personal = (iaddr == null ? null : iaddr.getPersonal());

        // ISO 8601
        DateFormat DTF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DTF.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

        url = url.replace("$" + EXTRA_RULE + "$", Uri.encode(name == null ? "" : name));
        url = url.replace("$" + EXTRA_SENDER + "$", Uri.encode(address == null ? "" : address));
        url = url.replace("$" + EXTRA_NAME + "$", Uri.encode(personal == null ? "" : personal));
        url = url.replace("$" + EXTRA_SUBJECT + "$", Uri.encode(message.subject == null ? "" : message.subject));
        url = url.replace("$" + EXTRA_RECEIVED + "$", Uri.encode(DTF.format(message.received)));

        if (!TextUtils.isEmpty(body)) {
            body = body.replace("$" + EXTRA_RULE + "$", name == null ? "" : name);
            body = body.replace("$" + EXTRA_SENDER + "$", address == null ? "" : address);
            body = body.replace("$" + EXTRA_NAME + "$", personal == null ? "" : personal);
            body = body.replace("$" + EXTRA_SUBJECT + "$", message.subject == null ? "" : message.subject);
            body = body.replace("$" + EXTRA_RECEIVED + "$", DTF.format(message.received));
        }

        Log.i("GET " + url);

        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(body != null);
            connection.setReadTimeout(URL_TIMEOUT);
            connection.setConnectTimeout(URL_TIMEOUT);
            connection.setInstanceFollowRedirects(true);
            ConnectionHelper.setUserAgent(context, connection);
            connection.connect();

            if (body != null)
                connection.getOutputStream().write(body.getBytes());

            int status = connection.getResponseCode();
            if (status < 200 || status > 299) {
                String error = "Error " + status + ": " + connection.getResponseMessage();
                try {
                    InputStream is = connection.getErrorStream();
                    if (is != null)
                        error += "\n" + Helper.readStream(is);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                throw new IOException(error);
            }
        } finally {
            if (connection != null)
                connection.disconnect();
        }

        return true;
    }

    private boolean onActionSilent(Context context, EntityMessage message, JSONObject jargs) {
        DB db = DB.getInstance(context);
        db.message().setMessageUiSilent(message.id, true);

        message.ui_silent = true;
        return true;
    }

    private boolean onActionSummarize(Context context, EntityMessage message, boolean browsed, JSONObject jargs) throws JSONException, IOException {
        DB db = DB.getInstance(context);

        if (message.ui_hide)
            return false;

        if (!this.async && this.id != null) {
            EntityOperation.queue(context, message, EntityOperation.RULE, this.id, browsed);
            return true;
        }

        try {
            Spanned summary = AI.getSummaryText(context, message, -1L);
            if (summary != null)
                message.preview = summary.toString().trim();
        } catch (Throwable ex) {
            message.error = Log.formatThrowable(ex);
            db.message().setMessageError(message.id, message.error);
            return false;
        }

        db.message().setMessageContent(message.id, message.content, message.language, message.plain_only, message.preview, message.warning);
        db.message().setMessageNotifying(message.id, 0);

        return true;
    }

    private static Calendar getRelativeCalendar(boolean all, int minutes, long reference) {
        int d = minutes / (24 * 60);
        int h = minutes / 60 % 24;
        int m = minutes % 60;

        Calendar cal = Calendar.getInstance();

        if (all)
            cal.setTimeInMillis(reference);
        else {
            if (reference > cal.getTimeInMillis() - 7 * 24 * 3600 * 1000L)
                cal.setTimeInMillis(reference);
            long time = cal.getTimeInMillis();

            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY + d);
            if (cal.getTimeInMillis() < time)
                cal.add(Calendar.HOUR_OF_DAY, 7 * 24);
        }

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

            if (TextUtils.isEmpty(sender))
                continue;

            boolean regex = false;
            if (block_domain) {
                String domain = UriHelper.getEmailDomain(sender);
                if (domain != null)
                    domain = domain.trim();
                if (!TextUtils.isEmpty(domain) && !domains.contains(domain)) {
                    String parent = UriHelper.getParentDomain(context, domain);
                    if (parent != null)
                        domain = parent;
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
                    Objects.equals(this.group, other.group) &&
                    this.order == other.order &&
                    this.enabled == other.enabled &&
                    this.daily == other.daily &&
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
        json.put("group", group);
        json.put("order", order);
        json.put("enabled", enabled);
        json.put("daily", daily);
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
        if (json.has("group") && !json.isNull("group"))
            rule.group = json.getString("group");
        rule.order = json.getInt("order");
        rule.enabled = json.getBoolean("enabled");
        rule.daily = json.optBoolean("daily");
        rule.stop = json.getBoolean("stop");
        rule.condition = json.getString("condition");
        rule.action = json.getString("action");
        rule.applied = json.optInt("applied", 0);
        if (json.has("last_applied") && !json.isNull("last_applied"))
            rule.last_applied = json.getLong("last_applied");
        return rule;
    }
}
