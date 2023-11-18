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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessageRemovedException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Organizer;

public abstract class TaskComposeDraft extends SimpleTask<ComposeHelper.DraftData> {
    private static final int MAX_QUOTE_LEVEL = 5;

    @Override
    protected ComposeHelper.DraftData onExecute(Context context, Bundle args) throws Throwable {
        String action = args.getString("action");
        long id = args.getLong("id", -1);
        long aid = args.getLong("account", -1);
        long iid = args.getLong("identity", -1);
        long reference = args.getLong("reference", -1);
        int dsn = args.getInt("dsn", EntityMessage.DSN_RECEIPT);
        File ics = (File) args.getSerializable("ics");
        String status = args.getString("status");
        // raw
        long answer = args.getLong("answer", -1);
        String to = args.getString("to");
        String cc = args.getString("cc");
        String bcc = args.getString("bcc");
        // inreplyto
        String external_subject = args.getString("subject", "");
        String external_body = args.getString("body", "");
        String external_text = args.getString("text");
        CharSequence selected_text = args.getCharSequence("selected");
        ArrayList<Uri> uris = args.getParcelableArrayList("attachments");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean plain_only = prefs.getBoolean("plain_only", false);
        boolean plain_only_reply = prefs.getBoolean("plain_only_reply", false);
        boolean resize_reply = prefs.getBoolean("resize_reply", true);
        boolean sign_default = prefs.getBoolean("sign_default", false);
        boolean encrypt_default = prefs.getBoolean("encrypt_default", false);
        boolean receipt_default = prefs.getBoolean("receipt_default", false);
        boolean write_below = prefs.getBoolean("write_below", false);
        boolean save_drafts = prefs.getBoolean("save_drafts", true);
        boolean auto_identity = prefs.getBoolean("auto_identity", false);
        boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
        boolean suggest_received = prefs.getBoolean("suggest_received", false);
        boolean forward_new = prefs.getBoolean("forward_new", true);

        Log.i("Load draft action=" + action + " id=" + id + " reference=" + reference);

        Map<String, String> crumb = new HashMap<>();
        crumb.put("draft", Long.toString(id));
        crumb.put("reference", Long.toString(reference));
        crumb.put("action", action);
        Log.breadcrumb("compose", crumb);

        ComposeHelper.DraftData data = new ComposeHelper.DraftData();

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            data.identities = db.identity().getComposableIdentities(null);
            if (data.identities == null || data.identities.size() == 0)
                throw new OperationCanceledException(context.getString(R.string.title_no_composable));

            data.draft = db.message().getMessage(id);
            boolean wb = (data.draft == null || data.draft.write_below == null ? write_below : data.draft.write_below);
            if (data.draft == null || data.draft.ui_hide) {
                // New draft
                if ("edit".equals(action))
                    throw new MessageRemovedException("Draft for edit was deleted hide=" + (data.draft != null));

                EntityMessage ref = db.message().getMessage(reference);

                data.draft = new EntityMessage();
                data.draft.msgid = EntityMessage.generateMessageId();

                // Select identity matching from address
                EntityIdentity selected = null;

                if (aid < 0)
                    if (ref == null) {
                        EntityAccount primary = db.account().getPrimaryAccount();
                        if (primary != null)
                            aid = primary.id;
                    } else
                        aid = ref.account;
                if (iid < 0 && ref != null && ref.identity != null)
                    iid = ref.identity;

                if (iid >= 0)
                    for (EntityIdentity identity : data.identities)
                        if (identity.id.equals(iid)) {
                            selected = identity;
                            EntityLog.log(context, "Selected requested identity=" + iid);
                            break;
                        }

                if (ref != null) {
                    Address[] refto;
                    boolean self = ref.replySelf(data.identities, ref.account);
                    if (ref.to == null || ref.to.length == 0 || self)
                        refto = ref.from;
                    else
                        refto = ref.to;
                    Log.i("Ref self=" + self +
                            " to=" + MessageHelper.formatAddresses(refto));
                    if (refto != null && refto.length > 0) {
                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.account.equals(aid) &&
                                            identity.sameAddress(sender)) {
                                        selected = identity;
                                        EntityLog.log(context, "Selected same account/identity");
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.account.equals(aid) &&
                                            identity.similarAddress(sender)) {
                                        selected = identity;
                                        EntityLog.log(context, "Selected similar account/identity");
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.sameAddress(sender)) {
                                        selected = identity;
                                        EntityLog.log(context, "Selected same */identity");
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.similarAddress(sender)) {
                                        selected = identity;
                                        EntityLog.log(context, "Selected similer */identity");
                                        break;
                                    }
                    }
                }

                if (selected == null && auto_identity)
                    try {
                        Address[] tos = MessageHelper.parseAddresses(context, to);
                        if (tos != null && tos.length > 0) {
                            String email = ((InternetAddress) tos[0]).getAddress();
                            List<Long> identities = null;
                            if (suggest_sent)
                                identities = db.contact().getIdentities(email, EntityContact.TYPE_TO);
                            if (suggest_received && (identities == null || identities.size() == 0))
                                identities = db.contact().getIdentities(email, EntityContact.TYPE_FROM);
                            if (identities != null && identities.size() == 1) {
                                EntityIdentity identity = db.identity().getIdentity(identities.get(0));
                                if (identity != null)
                                    selected = identity;
                            }
                        }
                    } catch (AddressException ex) {
                        Log.i(ex);
                    }

                if (selected == null)
                    for (EntityIdentity identity : data.identities)
                        if (identity.account.equals(aid) && identity.primary) {
                            selected = identity;
                            EntityLog.log(context, "Selected primary account/identity");
                            break;
                        }

                if (selected == null)
                    for (EntityIdentity identity : data.identities)
                        if (identity.account.equals(aid)) {
                            selected = identity;
                            EntityLog.log(context, "Selected account/identity");
                            break;
                        }

                if (selected == null)
                    for (EntityIdentity identity : data.identities)
                        if (identity.primary) {
                            selected = identity;
                            EntityLog.log(context, "Selected primary */identity");
                            break;
                        }

                if (selected == null)
                    for (EntityIdentity identity : data.identities) {
                        selected = identity;
                        EntityLog.log(context, "Selected */identity");
                        break;
                    }

                if (selected == null)
                    throw new OperationCanceledException(context.getString(R.string.title_no_composable));

                EntityLog.log(context, "Selected=" + selected.email);

                if (!"dsn".equals(action)) {
                    if (plain_only &&
                            !"resend".equals(action) &&
                            !"editasnew".equals(action))
                        data.draft.plain_only = 1;

                    if (encrypt_default || selected.encrypt_default)
                        if (selected.encrypt == 0)
                            data.draft.ui_encrypt = EntityMessage.PGP_SIGNENCRYPT;
                        else
                            data.draft.ui_encrypt = EntityMessage.SMIME_SIGNENCRYPT;
                    else if (sign_default || selected.sign_default)
                        if (selected.encrypt == 0)
                            data.draft.ui_encrypt = EntityMessage.PGP_SIGNONLY;
                        else
                            data.draft.ui_encrypt = EntityMessage.SMIME_SIGNONLY;
                }

                if (receipt_default)
                    data.draft.receipt_request = true;

                data.draft.sensitivity = (selected.sensitivity < 1 ? null : selected.sensitivity);

                Document document = Document.createShell("");

                if (ref == null) {
                    data.draft.thread = data.draft.msgid;

                    try {
                        data.draft.to = MessageHelper.parseAddresses(context, to);
                    } catch (AddressException ex) {
                        Log.w(ex);
                    }

                    try {
                        data.draft.cc = MessageHelper.parseAddresses(context, cc);
                    } catch (AddressException ex) {
                        Log.w(ex);
                    }

                    try {
                        data.draft.bcc = MessageHelper.parseAddresses(context, bcc);
                    } catch (AddressException ex) {
                        Log.w(ex);
                    }

                    data.draft.inreplyto = args.getString("inreplyto", null);

                    data.draft.subject = external_subject;

                    if (!TextUtils.isEmpty(external_body)) {
                        Document d = JsoupEx.parse(external_body); // Passed html
                        Element e = document
                                .createElement("div")
                                .html(d.body().html());
                        document.body().appendChild(e);
                    }

                    EntityAnswer a = (answer < 0
                            ? db.answer().getStandardAnswer()
                            : db.answer().getAnswer(answer));
                    if (a != null) {
                        db.answer().applyAnswer(a.id, new Date().getTime());
                        if (answer > 0)
                            data.draft.subject = a.name;
                        if (TextUtils.isEmpty(external_body)) {
                            Document d = JsoupEx.parse(a.getHtml(context, null));
                            document.body().append(d.body().html());
                        }
                    }

                    data.draft.signature = prefs.getBoolean("signature_new", true);
                    ComposeHelper.addSignature(context, document, data.draft, selected);
                } else {
                    // Actions:
                    // - reply
                    // - reply_all
                    // - forward
                    // - resend
                    // - editasnew
                    // - list
                    // - dsn
                    // - receipt
                    // - participation

                    // References
                    if ("reply".equals(action) || "reply_all".equals(action) ||
                            "list".equals(action) ||
                            "dsn".equals(action) ||
                            "participation".equals(action)) {
                        // https://tools.ietf.org/html/rfc5322#section-3.6.4
                        // The "References:" field will contain the contents of the parent's "References:" field (if any)
                        // followed by the contents of the parent's "Message-ID:" field (if any).
                        String refs = (ref.references == null ? "" : ref.references);
                        if (!TextUtils.isEmpty(ref.msgid))
                            refs = (TextUtils.isEmpty(refs) ? ref.msgid : refs + " " + ref.msgid);
                        data.draft.references = refs;
                        data.draft.inreplyto = ref.msgid;
                        data.draft.thread = ref.thread;

                        if ("list".equals(action) && ref.list_post != null)
                            data.draft.to = ref.list_post;
                        else if ("dsn".equals(action)) {
                            if (EntityMessage.DSN_RECEIPT.equals(dsn)) {
                                if (ref.receipt_to != null)
                                    data.draft.to = ref.receipt_to;
                            } else if (EntityMessage.DSN_HARD_BOUNCE.equals(dsn)) {
                                if (ref.return_path != null)
                                    data.draft.to = ref.return_path;
                            }
                        } else {
                            // Prevent replying to self
                            if (ref.replySelf(data.identities, ref.account)) {
                                EntityLog.log(context, "Reply self ref" +
                                        " from=" + MessageHelper.formatAddresses(ref.from) +
                                        " to=" + MessageHelper.formatAddresses(ref.to));
                                data.draft.from = ref.from;
                                data.draft.to = ref.to;
                            } else {
                                data.draft.from = ref.to;
                                data.draft.to = (ref.reply == null || ref.reply.length == 0 ? ref.from : ref.reply);
                            }

                            if (ref.identity != null) {
                                EntityIdentity recognized = db.identity().getIdentity(ref.identity);
                                EntityLog.log(context, "Recognized=" + (recognized == null ? null : recognized.email));

                                Address preferred = null;
                                if (recognized != null) {
                                    Address same = null;
                                    Address similar = null;

                                    List<Address> addresses = new ArrayList<>();
                                    if (data.draft.from != null)
                                        addresses.addAll(Arrays.asList(data.draft.from));
                                    if (data.draft.to != null)
                                        addresses.addAll(Arrays.asList(data.draft.to));
                                    if (ref.cc != null)
                                        addresses.addAll(Arrays.asList(ref.cc));
                                    if (ref.bcc != null)
                                        addresses.addAll(Arrays.asList(ref.bcc));

                                    for (Address from : addresses) {
                                        if (same == null && recognized.sameAddress(from))
                                            same = from;
                                        if (similar == null && recognized.similarAddress(from))
                                            similar = from;
                                    }

                                    //if (ref.deliveredto != null)
                                    //    try {
                                    //        Address deliveredto = new InternetAddress(ref.deliveredto);
                                    //        if (same == null && recognized.sameAddress(deliveredto))
                                    //            same = deliveredto;
                                    //        if (similar == null && recognized.similarAddress(deliveredto))
                                    //            similar = deliveredto;
                                    //    } catch (AddressException ex) {
                                    //        Log.w(ex);
                                    //    }

                                    EntityLog.log(context, "From=" + MessageHelper.formatAddresses(data.draft.from) +
                                            " delivered-to=" + ref.deliveredto +
                                            " same=" + (same == null ? null : ((InternetAddress) same).getAddress()) +
                                            " similar=" + (similar == null ? null : ((InternetAddress) similar).getAddress()));

                                    preferred = (same == null ? similar : same);
                                }

                                if (preferred != null) {
                                    String from = ((InternetAddress) preferred).getAddress();
                                    String name = ((InternetAddress) preferred).getPersonal();
                                    EntityLog.log(context, "Preferred=" + name + " <" + from + ">");
                                    if (TextUtils.isEmpty(from) || from.equalsIgnoreCase(recognized.email))
                                        from = null;
                                    if (!recognized.reply_extra_name ||
                                            TextUtils.isEmpty(name) || name.equals(recognized.name))
                                        name = null;
                                    String username = UriHelper.getEmailUser(from);
                                    String extra = (name == null ? "" : name + ", ") +
                                            (username == null ? "" : username);
                                    data.draft.extra = (TextUtils.isEmpty(extra) ? null : extra);
                                } else
                                    EntityLog.log(context, "Preferred=null");
                            } else
                                EntityLog.log(context, "Recognized=null");
                        }

                        if ("reply_all".equals(action)) {
                            List<Address> all = new ArrayList<>();
                            for (Address recipient : ref.getAllRecipients(data.identities, ref.account)) {
                                boolean found = false;
                                if (data.draft.to != null)
                                    for (Address t : data.draft.to)
                                        if (MessageHelper.equalEmail(recipient, t)) {
                                            found = true;
                                            break;
                                        }
                                if (!found)
                                    all.add(recipient);
                            }
                            data.draft.cc = all.toArray(new Address[0]);
                        } else if ("dsn".equals(action)) {
                            data.draft.dsn = dsn;
                            data.draft.receipt_request = false;
                        }

                    } else if ("forward".equals(action)) {
                        if (forward_new)
                            data.draft.thread = data.draft.msgid; // new thread
                        else {
                            data.draft.thread = ref.thread;
                            data.draft.inreplyto = ref.msgid;
                            data.draft.references = (ref.references == null ? "" : ref.references + " ") + ref.msgid;
                        }
                        data.draft.wasforwardedfrom = ref.msgid;
                    } else if ("resend".equals(action)) {
                        data.draft.resend = true;
                        data.draft.thread = data.draft.msgid;
                        data.draft.headers = ref.headers;
                    } else if ("editasnew".equals(action))
                        data.draft.thread = data.draft.msgid;

                    // Subject
                    String subject = (ref.subject == null ? "" : ref.subject);
                    if ("reply".equals(action) || "reply_all".equals(action)) {
                        data.draft.subject =
                                EntityMessage.getSubject(context, ref.language, subject, false);

                        if (external_text != null) {
                            Element div = document.createElement("div");
                            for (String line : external_text.split("\\r?\\n")) {
                                Element span = document.createElement("span");
                                span.text(line);
                                div.appendChild(span);
                                div.appendElement("br");
                            }
                            document.body().appendChild(div);
                        }
                    } else if ("forward".equals(action)) {
                        data.draft.subject =
                                EntityMessage.getSubject(context, ref.language, subject, true);
                    } else if ("resend".equals(action)) {
                        data.draft.subject = ref.subject;
                    } else if ("editasnew".equals(action)) {
                        if (ref.from != null && ref.from.length == 1) {
                            String from = ((InternetAddress) ref.from[0]).getAddress();
                            for (EntityIdentity identity : data.identities)
                                if (identity.email.equals(from)) {
                                    selected = identity;
                                    break;
                                }
                        }

                        data.draft.to = ref.to;
                        data.draft.cc = ref.cc;
                        data.draft.bcc = ref.bcc;
                        data.draft.subject = ref.subject;

                        if (ref.content)
                            document = JsoupEx.parse(ref.getFile(context));
                    } else if ("list".equals(action)) {
                        data.draft.subject = ref.subject;
                    } else if ("dsn".equals(action)) {
                        if (EntityMessage.DSN_HARD_BOUNCE.equals(dsn))
                            data.draft.subject = context.getString(R.string.title_hard_bounce_subject);
                        else
                            data.draft.subject = context.getString(R.string.title_receipt_subject, subject);

                        String[] texts;
                        if (EntityMessage.DSN_HARD_BOUNCE.equals(dsn))
                            texts = new String[]{context.getString(R.string.title_hard_bounce_text)};
                        else {
                            EntityAnswer receipt = db.answer().getReceiptAnswer();
                            if (receipt == null)
                                texts = Helper.getStrings(context, ref.language, R.string.title_receipt_text);
                            else {
                                db.answer().applyAnswer(receipt.id, new Date().getTime());
                                texts = new String[0];
                                Document d = JsoupEx.parse(receipt.getHtml(context, null));
                                document.body().append(d.body().html());
                            }
                        }

                        for (int i = 0; i < texts.length; i++) {
                            if (i > 0)
                                document.body()
                                        .appendElement("br");

                            Element div = document.createElement("div");
                            div.text(texts[i]);
                            document.body()
                                    .appendChild(div)
                                    .appendElement("br");
                        }
                    } else if ("participation".equals(action))
                        data.draft.subject = status + ": " + ref.subject;

                    if (!"dsn".equals(action)) {
                        // Sensitivity
                        data.draft.sensitivity = ref.sensitivity;

                        // Plain-only
                        if (plain_only_reply && ref.isPlainOnly())
                            data.draft.plain_only = 1;

                        // Encryption
                        List<Address> recipients = new ArrayList<>();
                        if (data.draft.to != null)
                            recipients.addAll(Arrays.asList(data.draft.to));
                        if (data.draft.cc != null)
                            recipients.addAll(Arrays.asList(data.draft.cc));
                        if (data.draft.bcc != null)
                            recipients.addAll(Arrays.asList(data.draft.bcc));

                        if (!BuildConfig.DEBUG)
                            if (EntityMessage.PGP_SIGNONLY.equals(ref.ui_encrypt) ||
                                    EntityMessage.PGP_SIGNENCRYPT.equals(ref.ui_encrypt)) {
                                if (PgpHelper.isOpenKeychainInstalled(context) &&
                                        selected.sign_key != null &&
                                        PgpHelper.hasPgpKey(context, recipients, true))
                                    data.draft.ui_encrypt = ref.ui_encrypt;
                            } else if (EntityMessage.SMIME_SIGNONLY.equals(ref.ui_encrypt) ||
                                    EntityMessage.SMIME_SIGNENCRYPT.equals(ref.ui_encrypt)) {
                                if (ActivityBilling.isPro(context) &&
                                        selected.sign_key_alias != null &&
                                        SmimeHelper.hasSmimeKey(context, recipients, true))
                                    data.draft.ui_encrypt = ref.ui_encrypt;
                            }
                    }

                    // Reply template
                    EntityAnswer a = null;
                    if (answer < 0) {
                        if ("reply".equals(action) || "reply_all".equals(action) ||
                                "forward".equals(action) || "list".equals(action))
                            a = db.answer().getStandardAnswer();
                    } else
                        a = db.answer().getAnswer(answer);

                    if (a != null) {
                        db.answer().applyAnswer(a.id, new Date().getTime());
                        if (a.label != null && ref != null)
                            EntityOperation.queue(context, ref, EntityOperation.LABEL, a.label, true);
                        Document d = JsoupEx.parse(a.getHtml(context, data.draft.to));
                        document.body().append(d.body().html());
                    }

                    // Signature
                    if ("reply".equals(action) || "reply_all".equals(action))
                        data.draft.signature = prefs.getBoolean("signature_reply", true);
                    else if ("forward".equals(action))
                        data.draft.signature = prefs.getBoolean("signature_forward", true);
                    else
                        data.draft.signature = false;

                    if (ref.content && "resend".equals(action)) {
                        document = JsoupEx.parse(ref.getFile(context));
                        HtmlHelper.clearAnnotations(document);
                        // Save original body
                        Element div = document.body()
                                .tagName("div")
                                .attr("fairemail", "reference");
                        Element body = document.createElement("body")
                                .appendChild(div);
                        document.body().replaceWith(body);
                    }

                    // Reply header
                    if (ref.content &&
                            !"resend".equals(action) &&
                            !"editasnew".equals(action) &&
                            !("list".equals(action) && TextUtils.isEmpty(selected_text)) &&
                            !"dsn".equals(action)) {
                        // Reply/forward
                        Element reply = document.createElement("div");
                        reply.attr("fairemail", "reference");

                        // Build reply header
                        boolean separate_reply = prefs.getBoolean("separate_reply", false);
                        boolean extended_reply = prefs.getBoolean("extended_reply", false);
                        Element p = ref.getReplyHeader(context, document, separate_reply, extended_reply);
                        reply.appendChild(p);

                        Document d;
                        if (TextUtils.isEmpty(selected_text)) {
                            // Get referenced message body
                            d = JsoupEx.parse(ref.getFile(context));
                            HtmlHelper.normalizeNamespaces(d, false);
                            HtmlHelper.clearAnnotations(d); // Legacy left-overs

                            if (BuildConfig.DEBUG)
                                d.select(".faircode_remove").remove();

                            if ("reply".equals(action) || "reply_all".equals(action)) {
                                // Remove signature separators
                                boolean remove_signatures = prefs.getBoolean("remove_signatures", false);
                                if (remove_signatures)
                                    HtmlHelper.removeSignatures(d);

                                // Limit number of nested block quotes
                                boolean quote_limit = prefs.getBoolean("quote_limit", true);
                                if (quote_limit)
                                    HtmlHelper.quoteLimit(d, MAX_QUOTE_LEVEL);
                            }
                        } else {
                            // Selected text
                            d = Document.createShell("");

                            Element div = d.createElement("div");
                            if (selected_text instanceof Spanned)
                                div.html(HtmlHelper.toHtml((Spanned) selected_text, context));
                            else
                                for (String line : selected_text.toString().split("\\r?\\n")) {
                                    Element span = document.createElement("span");
                                    span.text(line);
                                    div.appendChild(span);
                                    div.appendElement("br");
                                }

                            d.body().appendChild(div);
                        }

                        Element e = d.body();

                        // Apply styles
                        List<CSSStyleSheet> sheets = HtmlHelper.parseStyles(d.head().select("style"));
                        for (Element element : e.select("*")) {
                            String tag = element.tagName();
                            String clazz = element.attr("class");
                            String style = HtmlHelper.processStyles(context, tag, clazz, null, sheets);
                            style = HtmlHelper.mergeStyles(style, element.attr("style"));
                            if (!TextUtils.isEmpty(style))
                                element.attr("style", style);
                        }

                        // Quote referenced message body
                        boolean quote_reply = prefs.getBoolean("quote_reply", true);
                        boolean quote = (quote_reply &&
                                ("reply".equals(action) || "reply_all".equals(action) || "list".equals(action)));

                        if (quote) {
                            String style = e.attr("style");
                            style = HtmlHelper.mergeStyles(style, HtmlHelper.getQuoteStyle(e));
                            e.tagName("blockquote").attr("style", style);
                        } else
                            e.tagName("p");
                        reply.appendChild(e);

                        if (wb && data.draft.wasforwardedfrom == null)
                            document.body().prependChild(reply);
                        else
                            document.body().appendChild(reply);

                        ComposeHelper.addSignature(context, document, data.draft, selected);
                    }
                }

                EntityFolder drafts = db.folder().getFolderByType(selected.account, EntityFolder.DRAFTS);
                if (drafts == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_drafts));

                boolean signature_once = prefs.getBoolean("signature_reply_once", false);
                if (signature_once && data.draft.signature &&
                        ref != null && ref.thread != null &&
                        ("reply".equals(action) || "reply_all".equals(action))) {
                    List<EntityMessage> outbound = new ArrayList<>();

                    EntityFolder sent = db.folder().getFolderByType(drafts.account, EntityFolder.SENT);
                    if (sent != null)
                        outbound.addAll(db.message().getMessagesByThread(drafts.account, ref.thread, null, sent.id));

                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox != null)
                        outbound.addAll(db.message().getMessagesByThread(drafts.account, ref.thread, null, outbox.id));

                    if (outbound.size() > 0) {
                        Log.i("Signature suppressed");
                        data.draft.signature = false;
                    }
                }

                data.draft.account = drafts.account;
                data.draft.folder = drafts.id;
                data.draft.identity = selected.id;
                data.draft.from = new InternetAddress[]{new InternetAddress(selected.email, selected.name, StandardCharsets.UTF_8.name())};

                data.draft.sender = MessageHelper.getSortKey(data.draft.from);
                Uri lookupUri = ContactInfo.getLookupUri(data.draft.from);
                data.draft.avatar = (lookupUri == null ? null : lookupUri.toString());

                data.draft.received = new Date().getTime();
                data.draft.seen = true;
                data.draft.ui_seen = true;

                data.draft.revision = 1;
                data.draft.revisions = 1;

                data.draft.id = db.message().insertMessage(data.draft);

                String html = document.html();
                Helper.writeText(data.draft.getFile(context), html);
                Helper.writeText(data.draft.getFile(context, data.draft.revision), html);

                String text = HtmlHelper.getFullText(html);
                data.draft.preview = HtmlHelper.getPreview(text);
                data.draft.language = HtmlHelper.getLanguage(context, data.draft.subject, text);
                db.message().setMessageContent(data.draft.id,
                        true,
                        data.draft.language,
                        data.draft.plain_only,
                        data.draft.preview,
                        null);

                if ("participation".equals(action)) {
                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = data.draft.id;
                    attachment.sequence = 1;
                    attachment.name = "meeting.ics";
                    attachment.type = "text/calendar";
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.size = ics.length();
                    attachment.progress = null;
                    attachment.available = true;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    File file = attachment.getFile(context);
                    Helper.copy(ics, file);
                    Helper.secureDelete(ics);

                    ICalendar icalendar = CalendarHelper.parse(context, file);
                    VEvent event = icalendar.getEvents().get(0);
                    Organizer organizer = event.getOrganizer();
                    if (organizer != null) {
                        String email = organizer.getEmail();
                        String name = organizer.getCommonName();
                        if (!TextUtils.isEmpty(email)) {
                            InternetAddress o = new InternetAddress(email, name, StandardCharsets.UTF_8.name());
                            Log.i("Setting organizer=" + o);
                            data.draft.to = new Address[]{o};
                        }
                    }
                }

                if ("new".equals(action) && uris != null) {
                    ArrayList<Uri> images = new ArrayList<>();
                    for (Uri uri : uris)
                        try {
                            ComposeHelper.UriInfo info = ComposeHelper.getUriInfo(uri, context);
                            if (info.isImage())
                                images.add(uri);
                            else
                                ComposeHelper.addAttachment(context, data.draft.id, uri, null, false, 0, false);
                        } catch (IOException ex) {
                            Log.e(ex);
                        }

                    if (images.size() > 0)
                        args.putParcelableArrayList("images", images);
                }

                if (ref != null &&
                        ("reply".equals(action) || "reply_all".equals(action) ||
                                "forward".equals(action) ||
                                "resend".equals(action) ||
                                "editasnew".equals(action))) {
                    List<String> cid = new ArrayList<>();
                    for (Element img : document.select("img")) {
                        String src = img.attr("src");
                        if (src.startsWith("cid:"))
                            cid.add("<" + src.substring(4) + ">");
                    }

                    int sequence = 0;
                    List<EntityAttachment> attachments = db.attachment().getAttachments(ref.id);
                    for (EntityAttachment attachment : attachments)
                        if (attachment.subsequence == null &&
                                !attachment.isEncryption() &&
                                (cid.contains(attachment.cid) ||
                                        !("reply".equals(action) || "reply_all".equals(action)))) {
                            if (attachment.available) {
                                File source = attachment.getFile(context);

                                if (cid.contains(attachment.cid))
                                    attachment.disposition = Part.INLINE;
                                else {
                                    attachment.cid = null;
                                    attachment.related = false;
                                    attachment.disposition = Part.ATTACHMENT;
                                }

                                attachment.id = null;
                                attachment.message = data.draft.id;
                                attachment.sequence = ++sequence;
                                attachment.id = db.attachment().insertAttachment(attachment);

                                File target = attachment.getFile(context);
                                Helper.copy(source, target);

                                if (resize_reply &&
                                        ("reply".equals(action) || "reply_all".equals(action)))
                                    ComposeHelper.resizeAttachment(context, attachment, ComposeHelper.REDUCED_IMAGE_SIZE);
                            } else
                                args.putBoolean("incomplete", true);
                        }
                }

                if (save_drafts &&
                        (data.draft.ui_encrypt == null ||
                                EntityMessage.ENCRYPT_NONE.equals(data.draft.ui_encrypt)) &&
                        (!"new".equals(action) ||
                                answer > 0 ||
                                !TextUtils.isEmpty(to) ||
                                !TextUtils.isEmpty(cc) ||
                                !TextUtils.isEmpty(bcc) ||
                                !TextUtils.isEmpty(external_subject) ||
                                !TextUtils.isEmpty(external_body) ||
                                !TextUtils.isEmpty(external_text) ||
                                !TextUtils.isEmpty(selected_text) ||
                                (uris != null && uris.size() > 0))) {
                    Map<String, String> c = new HashMap<>();
                    c.put("id", data.draft.id == null ? null : Long.toString(data.draft.id));
                    c.put("encrypt", data.draft.encrypt + "/" + data.draft.ui_encrypt);
                    c.put("action", action);
                    Log.breadcrumb("Load draft", c);

                    EntityOperation.queue(context, data.draft, EntityOperation.ADD);
                }
            } else {
                args.putBoolean("saved", true);

                if (!data.draft.ui_seen)
                    EntityOperation.queue(context, data.draft, EntityOperation.SEEN, true);

                // External draft
                if (data.draft.identity == null) {
                    for (EntityIdentity identity : data.identities)
                        if (identity.account.equals(data.draft.account))
                            if (identity.primary) {
                                data.draft.identity = identity.id;
                                break;
                            } else if (data.draft.identity == null)
                                data.draft.identity = identity.id;

                    if (data.draft.identity != null)
                        db.message().setMessageIdentity(data.draft.id, data.draft.identity);
                    Log.i("Selected external identity=" + data.draft.identity);
                }

                if (data.draft.revision == null || data.draft.revisions == null) {
                    data.draft.revision = 1;
                    data.draft.revisions = 1;
                    db.message().setMessageRevision(data.draft.id, data.draft.revision);
                    db.message().setMessageRevisions(data.draft.id, data.draft.revisions);
                }

                if (data.draft.content || data.draft.uid == null) {
                    if (data.draft.uid == null && !data.draft.content)
                        Log.e("Draft without uid");

                    File file = data.draft.getFile(context);

                    Document doc = (data.draft.content ? JsoupEx.parse(file) : Document.createShell(""));
                    doc.select("div[fairemail=signature]").remove();
                    Elements ref = doc.select("div[fairemail=reference]");
                    ref.remove();

                    File refFile = data.draft.getRefFile(context);
                    if (refFile.exists()) {
                        ref.html(Helper.readText(refFile));
                        Helper.secureDelete(refFile);
                    }

                    // Possibly external draft

                    for (Element e : ref)
                        if (wb && data.draft.wasforwardedfrom == null)
                            doc.body().prependChild(e);
                        else
                            doc.body().appendChild(e);

                    EntityIdentity identity = null;
                    if (data.draft.identity != null)
                        identity = db.identity().getIdentity(data.draft.identity);

                    ComposeHelper.addSignature(context, doc, data.draft, identity);

                    String html = doc.html();
                    Helper.writeText(file, html);
                    Helper.writeText(data.draft.getFile(context, data.draft.revision), html);

                    String text = HtmlHelper.getFullText(html);
                    data.draft.preview = HtmlHelper.getPreview(text);
                    data.draft.language = HtmlHelper.getLanguage(context, data.draft.subject, text);
                    db.message().setMessageContent(data.draft.id,
                            true,
                            data.draft.language,
                            data.draft.plain_only,
                            data.draft.preview,
                            null);
                } else
                    EntityOperation.queue(context, data.draft, EntityOperation.BODY);
            }

            List<EntityAttachment> attachments = db.attachment().getAttachments(data.draft.id);
            if (attachments != null)
                for (EntityAttachment attachment : attachments)
                    if (!attachment.available && attachment.progress == null && attachment.error == null)
                        EntityOperation.queue(context, data.draft, EntityOperation.ATTACHMENT, attachment.id);

            set(data.draft.plain_only, attachments);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        ServiceSynchronize.eval(context, "compose/draft");

        return data;
    }

    @Override
    protected void onException(Bundle args, Throwable ex) {
        throw new NotImplementedException(this.getClass().getName());
    }

    protected void set(Integer plain_only, List<EntityAttachment> attachments) {
        throw new NotImplementedException(this.getClass().getName());
    }
}
