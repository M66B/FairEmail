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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public abstract class TaskComposeAction extends SimpleTask<EntityMessage> {
    private static final int MAX_REASONABLE_SIZE = 5 * 1024 * 1024;

    @Override
    protected EntityMessage onExecute(final Context context, Bundle args) throws Throwable {
        // Get data
        long id = args.getLong("id");
        int action = args.getInt("action");
        long aid = args.getLong("account");
        long iid = args.getLong("identity");
        String extra = args.getString("extra");
        String to = args.getString("to");
        String cc = args.getString("cc");
        String bcc = args.getString("bcc");
        String subject = args.getString("subject");
        Spanned loaded = (Spanned) args.getCharSequence("loaded");
        Spanned spanned = (Spanned) args.getCharSequence("spanned");
        boolean signature = args.getBoolean("signature");
        boolean empty = args.getBoolean("empty");
        boolean notext = args.getBoolean("notext");
        Bundle extras = args.getBundle("extras");

        boolean silent = extras.getBoolean("silent");

        boolean dirty = false;
        String body = HtmlHelper.toHtml(spanned, context);
        EntityMessage draft;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean discard_delete = prefs.getBoolean("discard_delete", true);
        boolean write_below = prefs.getBoolean("write_below", false);
        boolean save_drafts = prefs.getBoolean("save_drafts", true);
        int send_delayed = prefs.getInt("send_delayed", 0);

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            // Get draft & selected identity
            draft = db.message().getMessage(id);
            EntityIdentity identity = db.identity().getIdentity(iid);

            // Draft deleted by server
            if (draft == null || draft.ui_hide)
                throw new MessageRemovedException("Draft for action was deleted hide=" + (draft != null));

            Log.i("Load action id=" + draft.id + " action=" + getActionName(action));

            if (action == R.id.action_delete) {
                dirty = true;
                EntityFolder trash = db.folder().getFolderByType(draft.account, EntityFolder.TRASH);
                EntityFolder drafts = db.folder().getFolderByType(draft.account, EntityFolder.DRAFTS);
                if (empty || trash == null || discard_delete || !save_drafts || (drafts != null && drafts.local))
                    EntityOperation.queue(context, draft, EntityOperation.DELETE);
                else {
                    Map<String, String> c = new HashMap<>();
                    c.put("id", draft.id == null ? null : Long.toString(draft.id));
                    c.put("encrypt", draft.encrypt + "/" + draft.ui_encrypt);
                    Log.breadcrumb("Discard draft", c);

                    EntityOperation.queue(context, draft, EntityOperation.ADD);
                    EntityOperation.queue(context, draft, EntityOperation.MOVE, trash.id);
                }

                ApplicationEx.getMainHandler().post(new Runnable() {
                    public void run() {
                        ToastEx.makeText(context, R.string.title_draft_deleted, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // Move draft to new account
                if (draft.account != aid && aid >= 0) {
                    Log.i("Account changed");

                    Long uid = draft.uid;
                    String msgid = draft.msgid;
                    boolean content = draft.content;
                    Boolean ui_hide = draft.ui_hide;

                    // To prevent violating constraints
                    draft.uid = null;
                    draft.msgid = null;
                    db.message().updateMessage(draft);

                    // Create copy to delete
                    draft.id = null;
                    draft.uid = uid;
                    draft.msgid = msgid;
                    draft.content = false;
                    draft.ui_hide = true;
                    draft.id = db.message().insertMessage(draft);
                    EntityOperation.queue(context, draft, EntityOperation.DELETE);

                    // Restore original with new account, no uid and new msgid
                    draft.id = id;
                    draft.account = aid;
                    draft.folder = db.folder().getFolderByType(aid, EntityFolder.DRAFTS).id;
                    draft.uid = null;
                    draft.msgid = EntityMessage.generateMessageId();
                    draft.content = content;
                    draft.ui_hide = ui_hide;
                    db.message().updateMessage(draft);

                    if (draft.content)
                        dirty = true;
                }

                Map<String, String> crumb = new HashMap<>();
                crumb.put("draft", draft.folder + ":" + draft.id);
                crumb.put("content", Boolean.toString(draft.content));
                crumb.put("revision", Integer.toString(draft.revision == null ? -1 : draft.revision));
                crumb.put("revisions", Integer.toString(draft.revisions == null ? -1 : draft.revisions));
                crumb.put("file", Boolean.toString(draft.getFile(context).exists()));
                crumb.put("action", getActionName(action));
                Log.breadcrumb("compose", crumb);

                List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);

                // Get data
                InternetAddress[] afrom = (identity == null ? null : new InternetAddress[]{new InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name())});
                InternetAddress[] ato = MessageHelper.dedup(MessageHelper.parseAddresses(context, to));
                InternetAddress[] acc = MessageHelper.dedup(MessageHelper.parseAddresses(context, cc));
                InternetAddress[] abcc = MessageHelper.dedup(MessageHelper.parseAddresses(context, bcc));

                // Safe guard
                if (action == R.id.action_send) {
                    checkAddress(ato, context);
                    checkAddress(acc, context);
                    checkAddress(abcc, context);
                }

                if (TextUtils.isEmpty(extra))
                    extra = null;

                List<Integer> eparts = new ArrayList<>();
                for (EntityAttachment attachment : attachments)
                    if (attachment.available)
                        if (attachment.isEncryption())
                            eparts.add(attachment.encryption);

                if (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt)) {
                    if (!eparts.contains(EntityAttachment.PGP_KEY) ||
                            !eparts.contains(EntityAttachment.PGP_SIGNATURE) ||
                            !eparts.contains(EntityAttachment.PGP_CONTENT))
                        dirty = true;
                } else if (EntityMessage.PGP_ENCRYPTONLY.equals(draft.ui_encrypt)) {
                    if (!eparts.contains(EntityAttachment.PGP_MESSAGE))
                        dirty = true;
                } else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                    if (!eparts.contains(EntityAttachment.PGP_KEY) ||
                            !eparts.contains(EntityAttachment.PGP_MESSAGE))
                        dirty = true;
                } else if (EntityMessage.SMIME_SIGNONLY.equals(draft.ui_encrypt)) {
                    if (!eparts.contains(EntityAttachment.SMIME_SIGNATURE) ||
                            !eparts.contains(EntityAttachment.SMIME_CONTENT))
                        dirty = true;
                } else if (EntityMessage.SMIME_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                    if (!eparts.contains(EntityAttachment.SMIME_MESSAGE))
                        dirty = true;
                }

                Long ident = (identity == null ? null : identity.id);
                Pair<Integer, List<EntityAttachment>> last = get();
                if (!Objects.equals(draft.identity, ident) ||
                        !Objects.equals(draft.extra, extra) ||
                        !MessageHelper.equal(draft.from, afrom) ||
                        !MessageHelper.equal(draft.to, ato) ||
                        !MessageHelper.equal(draft.cc, acc) ||
                        !MessageHelper.equal(draft.bcc, abcc) ||
                        !Objects.equals(draft.subject, subject) ||
                        !draft.signature.equals(signature) ||
                        !Objects.equals(last.first, draft.plain_only) ||
                        !EntityAttachment.equals(last.second, attachments))
                    dirty = true;

                set(draft.plain_only, attachments);

                if (dirty) {
                    // Update draft
                    draft.identity = ident;
                    draft.extra = extra;
                    draft.from = afrom;
                    draft.to = ato;
                    draft.cc = acc;
                    draft.bcc = abcc;
                    draft.subject = subject;
                    draft.signature = signature;
                    draft.sender = MessageHelper.getSortKey(draft.from);
                    Uri lookupUri = ContactInfo.getLookupUri(draft.from);
                    draft.avatar = (lookupUri == null ? null : lookupUri.toString());
                    db.message().updateMessage(draft);
                }

                Document doc = JsoupEx.parse(draft.getFile(context));
                Element first = (doc.body().childrenSize() == 0 ? null : doc.body().child(0));
                boolean below = (first != null && first.attr("fairemail").equals("reference"));
                doc.select("div[fairemail=signature]").remove();
                Elements ref = doc.select("div[fairemail=reference]");
                ref.remove();

                if (extras.containsKey("html"))
                    dirty = true;

                boolean wb = (draft == null || draft.write_below == null ? write_below : draft.write_below);
                if (below != wb &&
                        doc.body().childrenSize() > 0 &&
                        draft.wasforwardedfrom == null)
                    dirty = true;

                if (!dirty)
                    if (loaded == null) {
                        Document b = JsoupEx.parse(body); // Is-dirty
                        if (!Objects.equals(b.body().html(), doc.body().html()))
                            dirty = true;
                    } else {
                        // Was not dirty before
                        String hloaded = HtmlHelper.toHtml(loaded, context);
                        String hspanned = HtmlHelper.toHtml(spanned, context);
                        if (!Objects.equals(hloaded, hspanned))
                            dirty = true;
                    }

                if (draft.revision == null) {
                    draft.revision = 1;
                    draft.revisions = 1;
                }

                int revision = draft.revision; // Save for undo/redo
                if (dirty) {
                    dirty = true;

                    // Get saved body
                    Document d;
                    if (extras.containsKey("html")) {
                        // Save current revision
                        Document c = JsoupEx.parse(body);

                        for (Element e : ref)
                            if (wb && draft.wasforwardedfrom == null)
                                c.body().prependChild(e);
                            else
                                c.body().appendChild(e);

                        ComposeHelper.addSignature(context, c, draft, identity);

                        Helper.writeText(draft.getFile(context, draft.revision), c.html());

                        d = JsoupEx.parse(extras.getString("html"));
                    } else {
                        d = JsoupEx.parse(body); // Save

                        for (Element e : ref)
                            if (wb && draft.wasforwardedfrom == null)
                                d.body().prependChild(e);
                            else
                                d.body().appendChild(e);

                        ComposeHelper.addSignature(context, d, draft, identity);
                    }

                    body = d.html();

                    // Create new revision
                    draft.revisions++;
                    draft.revision = draft.revisions;

                    Helper.writeText(draft.getFile(context, draft.revision), body);
                } else
                    body = Helper.readText(draft.getFile(context));

                if (action == R.id.action_undo || action == R.id.action_redo) {
                    if (action == R.id.action_undo) {
                        if (revision > 1)
                            draft.revision = revision - 1;
                        else
                            draft.revision = revision;
                    } else {
                        if (revision < draft.revisions)
                            draft.revision = revision + 1;
                        else
                            draft.revision = revision;
                    }

                    // Restore revision
                    Log.i("Restoring revision=" + draft.revision);
                    File file = draft.getFile(context, draft.revision);
                    if (file.exists())
                        body = Helper.readText(file);
                    else
                        Log.e("Missing" +
                                " revision=" + draft.revision + "/" + draft.revisions +
                                " action=" + getActionName(action));

                    dirty = true;
                } else if (action == R.id.action_send) {
                    if (!draft.isPlainOnly()) {
                        // Remove unused inline images
                        List<String> cids = new ArrayList<>();
                        Document d = JsoupEx.parse(body);
                        for (Element element : d.select("img")) {
                            String src = element.attr("src");
                            if (src.startsWith("cid:"))
                                cids.add("<" + src.substring(4) + ">");
                        }

                        for (EntityAttachment attachment : new ArrayList<>(attachments))
                            if (attachment.isInline() && attachment.isImage() &&
                                    attachment.cid != null && !cids.contains(attachment.cid)) {
                                Log.i("Removing unused inline attachment cid=" + attachment.cid);
                                attachments.remove(attachment);
                                db.attachment().deleteAttachment(attachment.id);
                                dirty = true;
                            }
                    } else {
                        // Convert inline images to attachments
                        for (EntityAttachment attachment : new ArrayList<>(attachments))
                            if (attachment.isInline() && attachment.isImage()) {
                                Log.i("Converting to attachment cid=" + attachment.cid);
                                attachment.disposition = Part.ATTACHMENT;
                                attachment.cid = null;
                                db.attachment().setDisposition(attachment.id, attachment.disposition, attachment.cid);
                                dirty = true;
                            }
                    }
                }

                File f = draft.getFile(context);
                Helper.writeText(f, body);
                if (f.length() > MAX_REASONABLE_SIZE)
                    args.putBoolean("large", true);

                String full = HtmlHelper.getFullText(body);
                draft.preview = HtmlHelper.getPreview(full);
                draft.language = HtmlHelper.getLanguage(context, draft.subject, full);
                db.message().setMessageContent(draft.id,
                        true,
                        draft.language,
                        draft.plain_only, // unchanged
                        draft.preview,
                        null);

                db.message().setMessageRevision(draft.id, draft.revision);
                db.message().setMessageRevisions(draft.id, draft.revisions);

                if (dirty) {
                    draft.received = new Date().getTime();
                    draft.sent = draft.received;
                    db.message().setMessageReceived(draft.id, draft.received);
                    db.message().setMessageSent(draft.id, draft.sent);
                }

                if (silent) {
                    // Skip storing on the server, etc
                    db.setTransactionSuccessful();
                    return draft;
                }

                // Execute action
                boolean encrypted = extras.getBoolean("encrypted");
                boolean shouldEncrypt = EntityMessage.PGP_ENCRYPTONLY.equals(draft.ui_encrypt) ||
                        EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt) ||
                        (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt) && action == R.id.action_send) ||
                        EntityMessage.SMIME_SIGNENCRYPT.equals(draft.ui_encrypt) ||
                        (EntityMessage.SMIME_SIGNONLY.equals(draft.ui_encrypt) && action == R.id.action_send);
                boolean needsEncryption = (dirty && !encrypted && shouldEncrypt);
                boolean autosave = extras.getBoolean("autosave");
                if (needsEncryption && !autosave) {
                    args.putBoolean("needsEncryption", true);
                    db.setTransactionSuccessful();
                    return draft;
                }

                if (!shouldEncrypt && !autosave)
                    for (EntityAttachment attachment : attachments)
                        if (attachment.isEncryption())
                            db.attachment().deleteAttachment(attachment.id);

                if (action == R.id.action_save ||
                        action == R.id.action_undo ||
                        action == R.id.action_redo ||
                        action == R.id.action_check) {
                    boolean unencrypted =
                            (!EntityMessage.PGP_ENCRYPTONLY.equals(draft.ui_encrypt) &&
                                    !EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt) &&
                                    !EntityMessage.SMIME_SIGNENCRYPT.equals(draft.ui_encrypt));
                    if ((dirty && unencrypted) || encrypted) {
                        if (save_drafts) {
                            Map<String, String> c = new HashMap<>();
                            c.put("id", draft.id == null ? null : Long.toString(draft.id));
                            c.put("dirty", Boolean.toString(dirty));
                            c.put("encrypt", draft.encrypt + "/" + draft.ui_encrypt);
                            c.put("encrypted", Boolean.toString(encrypted));
                            c.put("needsEncryption", Boolean.toString(needsEncryption));
                            c.put("autosave", Boolean.toString(autosave));
                            Log.breadcrumb("Save draft", c);

                            EntityOperation.queue(context, draft, EntityOperation.ADD);
                        }
                    }

                    if (action == R.id.action_check) {
                        // Check data
                        if (draft.identity == null)
                            throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                        if (false) {
                            EntityAccount account = db.account().getAccount(draft.account);
                            EntityFolder sent = db.folder().getFolderByType(draft.account, EntityFolder.SENT);
                            if (account != null && account.protocol == EntityAccount.TYPE_IMAP && sent == null)
                                args.putBoolean("sent_missing", true);
                        }

                        try {
                            checkAddress(ato, context);
                            checkAddress(acc, context);
                            checkAddress(abcc, context);

                            List<InternetAddress> check = new ArrayList<>();
                            List<String> checked = new ArrayList<>();
                            List<String> dup = new ArrayList<>();
                            if (ato != null)
                                check.addAll(Arrays.asList(ato));
                            if (acc != null)
                                check.addAll(Arrays.asList(acc));
                            if (abcc != null)
                                check.addAll(Arrays.asList(abcc));

                            for (InternetAddress a : check) {
                                String email = a.getAddress();
                                if (TextUtils.isEmpty(email))
                                    continue;
                                if (checked.contains(a.getAddress()))
                                    dup.add(email);
                                else
                                    checked.add(email);
                            }

                            if (dup.size() > 0)
                                throw new AddressException(context.getString(
                                        R.string.title_address_duplicate,
                                        TextUtils.join(", ", dup)));
                        } catch (AddressException ex) {
                            args.putString("address_error", ex.getMessage());
                        }

                        if (draft.to == null && draft.cc == null && draft.bcc == null &&
                                (identity == null || (identity.cc == null && identity.bcc == null)))
                            args.putBoolean("remind_to", true);

                        //if (TextUtils.isEmpty(draft.extra) &&
                        //        identity != null && identity.sender_extra)
                        //    args.putBoolean("remind_extra", true);

                        List<Address> recipients = new ArrayList<>();
                        if (draft.to != null)
                            recipients.addAll(Arrays.asList(draft.to));
                        if (draft.cc != null)
                            recipients.addAll(Arrays.asList(draft.cc));
                        if (draft.bcc != null)
                            recipients.addAll(Arrays.asList(draft.bcc));

                        boolean noreply = false;
                        for (Address recipient : recipients)
                            if (MessageHelper.isNoReply(recipient)) {
                                noreply = true;
                                break;
                            }
                        args.putBoolean("remind_noreply", noreply);

                        if (identity != null && !TextUtils.isEmpty(identity.internal)) {
                            boolean external = false;
                            String[] internals = identity.internal.split(",");
                            for (Address recipient : recipients) {
                                String email = ((InternetAddress) recipient).getAddress();
                                String domain = UriHelper.getEmailDomain(email);
                                if (domain == null)
                                    continue;

                                boolean found = false;
                                for (String internal : internals)
                                    if (internal.equalsIgnoreCase(domain)) {
                                        found = true;
                                        break;
                                    }
                                if (!found) {
                                    external = true;
                                    break;
                                }
                            }
                            args.putBoolean("remind_external", external);
                        }

                        if ((draft.dsn == null ||
                                EntityMessage.DSN_NONE.equals(draft.dsn)) &&
                                (draft.ui_encrypt == null ||
                                        EntityMessage.ENCRYPT_NONE.equals(draft.ui_encrypt))) {
                            args.putBoolean("remind_pgp", PgpHelper.hasPgpKey(context, recipients, false));
                            args.putBoolean("remind_smime", SmimeHelper.hasSmimeKey(context, recipients, false));
                        }

                        if (TextUtils.isEmpty(draft.subject))
                            args.putBoolean("remind_subject", true);

                        Document d = JsoupEx.parse(body);

                        if (notext &&
                                d.select("div[fairemail=reference]").isEmpty())
                            args.putBoolean("remind_text", true);

                        boolean styled = HtmlHelper.isStyled(d);
                        args.putBoolean("styled", styled);

                        int attached = 0;
                        List<String> dangerous = new ArrayList<>();
                        for (EntityAttachment attachment : attachments) {
                            if (!attachment.available)
                                throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                            else if (attachment.isAttachment())
                                attached++;
                            String ext = Helper.getExtension(attachment.name);
                            if (Helper.DANGEROUS_EXTENSIONS.contains(ext))
                                dangerous.add(attachment.name);
                        }
                        if (dangerous.size() > 0)
                            args.putString("remind_extension", String.join(", ", dangerous));

                        // Check for missing attachments
                        if (attached == 0) {
                            List<String> keywords = new ArrayList<>();
                            for (String text : Helper.getStrings(context, R.string.title_attachment_keywords))
                                keywords.addAll(Arrays.asList(text.split(",")));

                            d.select("div[fairemail=signature]").remove();
                            d.select("div[fairemail=reference]").remove();

                            String text = d.text();
                            for (String keyword : keywords)
                                if (text.matches("(?si).*\\b" + Pattern.quote(keyword.trim()) + "\\b.*")) {
                                    args.putBoolean("remind_attachment", true);
                                    break;
                                }
                        }

                        if (EntityMessage.DSN_HARD_BOUNCE.equals(draft.dsn))
                            args.putBoolean("remind_dsn", true);

                        // Check size
                        if (identity != null && identity.max_size != null)
                            try {
                                Properties props = MessageHelper.getSessionProperties(true);
                                if (identity.unicode)
                                    props.put("mail.mime.allowutf8", "true");
                                Session isession = Session.getInstance(props, null);
                                Message imessage = MessageHelper.from(context, draft, identity, isession, false);

                                File file = draft.getRawFile(context);
                                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                                    imessage.writeTo(os);
                                }

                                long size = file.length();
                                if (size > identity.max_size) {
                                    args.putBoolean("remind_size", true);
                                    args.putLong("size", size);
                                    args.putLong("max_size", identity.max_size);
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }

                        args.putBoolean("remind_internet", !ConnectionHelper.getNetworkState(context).isConnected());
                    } else {
                        int mid;
                        if (action == R.id.action_undo)
                            mid = R.string.title_undo;
                        else if (action == R.id.action_redo)
                            mid = R.string.title_redo;
                        else
                            mid = R.string.title_draft_saved;
                        final String msg = context.getString(mid) +
                                (BuildConfig.DEBUG
                                        ? " " + draft.revision + (dirty ? "*" : "")
                                        : "");

                        ApplicationEx.getMainHandler().post(new Runnable() {
                            public void run() {
                                ToastEx.makeText(context, msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } else if (action == R.id.action_send) {
                    EntityFolder outbox = EntityFolder.getOutbox(context);

                    // Delay sending message
                    if (draft.ui_snoozed == null && send_delayed != 0) {
                        if (extras.getBoolean("now"))
                            draft.ui_snoozed = null;
                        else
                            draft.ui_snoozed = new Date().getTime() + send_delayed * 1000L;
                    }

                    if (draft.ui_snoozed != null)
                        draft.received = draft.ui_snoozed;

                    // Copy message to outbox
                    long did = draft.id;

                    draft.id = null;
                    draft.folder = outbox.id;
                    draft.uid = null;
                    draft.fts = false;
                    draft.ui_hide = false;
                    draft.id = db.message().insertMessage(draft);
                    Helper.writeText(draft.getFile(context), body);

                    // Move attachments
                    for (EntityAttachment attachment : attachments)
                        db.attachment().setMessage(attachment.id, draft.id);

                    // Send message
                    if (draft.ui_snoozed == null)
                        EntityOperation.queue(context, draft, EntityOperation.SEND);

                    // Delete draft (cannot move to outbox)
                    EntityMessage tbd = db.message().getMessage(did);
                    if (tbd != null)
                        EntityOperation.queue(context, tbd, EntityOperation.DELETE);

                    final String feedback;
                    if (draft.ui_snoozed == null) {
                        boolean suitable = ConnectionHelper.getNetworkState(context).isSuitable();
                        if (suitable)
                            feedback = context.getString(R.string.title_queued);
                        else
                            feedback = context.getString(R.string.title_notification_waiting);
                    } else {
                        DateFormat DTF = Helper.getDateTimeInstance(context);
                        feedback = context.getString(R.string.title_queued_at, DTF.format(draft.ui_snoozed));
                    }

                    toast(feedback);

                    if (extras.getBoolean("archive")) {
                        EntityFolder archive = db.folder().getFolderByType(draft.account, EntityFolder.ARCHIVE);
                        if (archive != null) {
                            List<EntityMessage> messages = db.message().getMessagesByMsgId(draft.account, draft.inreplyto);
                            if (messages != null)
                                for (EntityMessage message : messages)
                                    EntityOperation.queue(context, message, EntityOperation.MOVE, archive.id);
                        }
                    }
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (action == R.id.action_check)
            try {
                InternetAddress[] ato = MessageHelper.dedup(MessageHelper.parseAddresses(context, to));
                InternetAddress[] acc = MessageHelper.dedup(MessageHelper.parseAddresses(context, cc));
                InternetAddress[] abcc = MessageHelper.dedup(MessageHelper.parseAddresses(context, bcc));

                try {
                    checkMx(ato, context);
                    checkMx(acc, context);
                    checkMx(abcc, context);
                } catch (UnknownHostException ex) {
                    args.putString("mx_error", ex.getMessage());
                }
            } catch (Throwable ignored) {
            }

        args.putBoolean("dirty", dirty);
        if (dirty)
            ServiceSynchronize.eval(context, "compose/action");

        if (action == R.id.action_send)
            if (draft.ui_snoozed == null)
                ServiceSend.start(context);
            else {
                Log.i("Delayed send id=" + draft.id + " at " + new Date(draft.ui_snoozed));
                EntityMessage.snooze(context, draft.id, draft.ui_snoozed);
            }

        return draft;
    }

    protected Pair<Integer, List<EntityAttachment>> get() {
        throw new NotImplementedException(this.getClass().getName());
    }

    protected void set(Integer plain_only, List<EntityAttachment> attachments) {
        throw new NotImplementedException(this.getClass().getName());
    }

    protected void toast(String feedback) {
        throw new NotImplementedException(this.getClass().getName());
    }

    private void checkAddress(InternetAddress[] addresses, Context context) throws AddressException {
        if (addresses == null)
            return;

        for (InternetAddress address : addresses)
            try {
                address.validate();
            } catch (AddressException ex) {
                throw new AddressException(context.getString(R.string.title_address_parse_error,
                        MessageHelper.formatAddressesCompose(new Address[]{address}), ex.getMessage()));
            }
    }

    private void checkMx(InternetAddress[] addresses, Context context) throws UnknownHostException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean lookup_mx = prefs.getBoolean("lookup_mx", false);
        if (!lookup_mx)
            return;

        if (addresses == null)
            return;

        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
        NetworkInfo ani = (cm == null ? null : cm.getActiveNetworkInfo());
        if (ani != null && ani.isConnected())
            DnsHelper.checkMx(context, addresses);
    }

    static String getActionName(int id) {
        if (id == R.id.action_delete) {
            return "delete";
        } else if (id == R.id.action_undo) {
            return "undo";
        } else if (id == R.id.action_redo) {
            return "redo";
        } else if (id == R.id.action_save) {
            return "save";
        } else if (id == R.id.action_check) {
            return "check";
        } else if (id == R.id.action_send) {
            return "send";
        }
        return Integer.toString(id);
    }
}
