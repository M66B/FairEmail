package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class MessageHelper {
    private MimeMessage imessage;
    private String raw = null;

    static Properties getSessionProperties() {
        Properties props = new Properties();

        // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#properties
        props.put("mail.imaps.ssl.checkserveridentity", "true");
        props.put("mail.imaps.ssl.trust", "*");
        props.put("mail.imaps.starttls.enable", "false");
        props.put("mail.imaps.timeout", "20000");
        props.put("mail.imaps.connectiontimeout", "20000");

        // https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html#properties
        props.put("mail.smtps.ssl.checkserveridentity", "true");
        props.put("mail.smtps.ssl.trust", "*");
        props.put("mail.smtps.starttls.enable", "false");
        props.put("mail.smtps.starttls.required", "false");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.timeout", "20000");
        props.put("mail.smtps.connectiontimeout", "20000");

        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "20000");
        props.put("mail.smtp.connectiontimeout", "20000");

        return props;
    }

    static MimeMessageEx from(EntityMessage message, List<EntityAttachment> attachments, Session isession) throws MessagingException {
        MimeMessageEx imessage = new MimeMessageEx(isession, message.msgid);

        imessage.setFlag(Flags.Flag.SEEN, message.seen);

        if (message.from != null && message.from.length > 0)
            imessage.setFrom(message.from[0]);

        if (message.to != null && message.to.length > 0)
            imessage.setRecipients(Message.RecipientType.TO, message.to);

        if (message.cc != null && message.cc.length > 0)
            imessage.setRecipients(Message.RecipientType.CC, message.cc);

        if (message.bcc != null && message.bcc.length > 0)
            imessage.setRecipients(Message.RecipientType.BCC, message.bcc);

        if (message.subject != null)
            imessage.setSubject(message.subject);

        // TODO: plain message?

        if (message.body == null)
            throw new IllegalArgumentException("null message");

        if (attachments.size() == 0)
            imessage.setText(message.body, Charset.defaultCharset().name(), "html");
        else {
            Multipart multipart = new MimeMultipart();

            BodyPart bpMessage = new MimeBodyPart();
            bpMessage.setContent(message.body, "text/html; charset=" + Charset.defaultCharset().name());
            multipart.addBodyPart(bpMessage);

            for (EntityAttachment attachment : attachments) {
                BodyPart bpAttachment = new MimeBodyPart();
                bpAttachment.setFileName(attachment.name);

                DataSource dataSource = new ByteArrayDataSource(attachment.content, attachment.type);
                bpAttachment.setDataHandler(new DataHandler(dataSource));

                multipart.addBodyPart(bpAttachment);
            }

            imessage.setContent(multipart);
        }

        imessage.setSentDate(new Date());

        return imessage;
    }

    static MimeMessageEx from(EntityMessage message, EntityMessage reply, List<EntityAttachment> attachments, Session isession) throws MessagingException {
        MimeMessageEx imessage = from(message, attachments, isession);
        imessage.addHeader("In-Reply-To", reply.msgid);
        imessage.addHeader("References", (reply.references == null ? "" : reply.references + " ") + reply.msgid);
        return imessage;
    }

    MessageHelper(MimeMessage message) {
        this.imessage = message;
    }

    MessageHelper(String raw, Session isession) throws MessagingException {
        byte[] bytes = Base64.decode(raw, Base64.URL_SAFE);
        InputStream is = new ByteArrayInputStream(bytes);
        this.imessage = new MimeMessage(isession, is);
    }

    boolean getSeen() throws MessagingException {
        return imessage.isSet(Flags.Flag.SEEN);
    }

    String getMessageID() throws MessagingException {
        return imessage.getHeader("Message-ID", null);
    }

    String[] getReferences() throws MessagingException {
        String refs = imessage.getHeader("References", null);
        return (refs == null ? new String[0] : refs.split("\\s+"));
    }

    String getInReplyTo() throws MessagingException {
        return imessage.getHeader("In-Reply-To", null);
    }

    String getThreadId(long uid) throws MessagingException {
        for (String ref : getReferences())
            if (!TextUtils.isEmpty(ref))
                return ref;
        String msgid = getMessageID();
        return (TextUtils.isEmpty(msgid) ? Long.toString(uid) : msgid);
    }

    Address[] getFrom() throws MessagingException {
        return imessage.getFrom();
    }

    Address[] getTo() throws MessagingException {
        return imessage.getRecipients(Message.RecipientType.TO);
    }

    Address[] getCc() throws MessagingException {
        return imessage.getRecipients(Message.RecipientType.CC);
    }

    Address[] getBcc() throws MessagingException {
        return imessage.getRecipients(Message.RecipientType.BCC);
    }

    Address[] getReply() throws MessagingException {
        return imessage.getReplyTo();
    }

    static String getFormattedAddresses(Address[] addresses, boolean full) {
        if (addresses == null)
            return null;

        List<String> formatted = new ArrayList<>();
        for (Address address : addresses)
            if (address instanceof InternetAddress) {
                InternetAddress a = (InternetAddress) address;
                String personal = a.getPersonal();
                if (TextUtils.isEmpty(personal))
                    formatted.add(address.toString());
                else if (full)
                    formatted.add(personal + " <" + a.getAddress() + ">");
                else
                    formatted.add(personal);
            } else
                formatted.add(address.toString());
        return TextUtils.join(", ", formatted);
    }

    String getHtml() throws MessagingException {
        return getHtml(imessage);
    }

    private String getHtml(Part part) throws MessagingException {
        if (part.isMimeType("text/*"))
            try {
                String s = part.getContent().toString();
                if (part.isMimeType("text/plain"))
                    s = "<pre>" + s.replaceAll("\\r?\\n", "<br />") + "</pre>";
                return s;
            } catch (IOException ex) {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                return null;
            }

        if (part.isMimeType("multipart/alternative")) {
            String text = null;
            try {
                Multipart mp = (Multipart) part.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain")) {
                        if (text == null)
                            text = getHtml(bp);
                    } else if (bp.isMimeType("text/html")) {
                        String s = getHtml(bp);
                        if (s != null)
                            return s;
                    } else
                        return getHtml(bp);
                }
            } catch (IOException ex) {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
            return text;
        }

        if (part.isMimeType("multipart/*")) {
            try {
                Multipart mp = (Multipart) part.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    String s = getHtml(mp.getBodyPart(i));
                    if (s != null)
                        return s;
                }
            } catch (IOException ex) {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
        }

        return null;
    }

    public List<EntityAttachment> getAttachments() throws IOException, MessagingException {
        List<EntityAttachment> result = new ArrayList<>();

        Object content = imessage.getContent();
        if (content instanceof String)
            return result;

        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++)
                result.addAll(getAttachments(multipart.getBodyPart(i)));
        }

        return result;
    }

    private List<EntityAttachment> getAttachments(BodyPart part) throws IOException, MessagingException {
        List<EntityAttachment> result = new ArrayList<>();

        Object content = part.getContent();
        if (content instanceof InputStream || content instanceof String) {
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || !TextUtils.isEmpty(part.getFileName())) {
                ContentType ct = new ContentType(part.getContentType());
                EntityAttachment attachment = new EntityAttachment();
                attachment.name = part.getFileName();
                attachment.type = ct.getBaseType();
                attachment.size = part.getSize();
                attachment.part = part;
                if (attachment.size < 0)
                    attachment.size = null;
                result.add(attachment);
            }
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++)
                result.addAll(getAttachments(multipart.getBodyPart(i)));
        }

        return result;
    }

    String getRaw() throws IOException, MessagingException {
        if (raw == null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            imessage.writeTo(os);
            raw = Base64.encodeToString(os.toByteArray(), Base64.URL_SAFE);
        }
        return raw;
    }
}
