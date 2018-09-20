package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
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
import javax.mail.internet.ParseException;

public class MessageHelper {
    private MimeMessage imessage;
    private String raw = null;

    final static int NETWORK_TIMEOUT = 60 * 1000; // milliseconds

    static Properties getSessionProperties(Context context, int auth_type) {
        Properties props = new Properties();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#properties
        props.put("mail.imaps.ssl.checkserveridentity", "true");
        props.put("mail.imaps.ssl.trust", "*");
        props.put("mail.imaps.starttls.enable", "false");

        // TODO: make timeouts configurable?
        props.put("mail.imaps.connectiontimeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.imaps.timeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.imaps.writetimeout", Integer.toString(NETWORK_TIMEOUT)); // one thread overhead

        props.put("mail.imaps.connectionpool.debug", "true");
        props.put("mail.imaps.connectionpooltimeout", Integer.toString(3 * 60 * 1000)); // default: 45 sec

        // "mail.imaps.finalizecleanclose"

        // https://tools.ietf.org/html/rfc4978
        // https://docs.oracle.com/javase/8/docs/api/java/util/zip/Deflater.html
        if (prefs.getBoolean("compress", true)) {
            Log.i(Helper.TAG, "IMAP compress enabled");
            props.put("mail.imaps.compress.enable", "true");
            //props.put("mail.imaps.compress.level", "-1");
            //props.put("mail.imaps.compress.strategy", "0");
        }

        props.put("mail.imaps.fetchsize", Integer.toString(48 * 1024)); // default 16K

        // https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html#properties
        props.put("mail.smtps.ssl.checkserveridentity", "true");
        props.put("mail.smtps.ssl.trust", "*");
        props.put("mail.smtps.starttls.enable", "false");
        props.put("mail.smtps.starttls.required", "false");
        props.put("mail.smtps.auth", "true");

        props.put("mail.smtps.connectiontimeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.smtps.writetimeout", Integer.toString(NETWORK_TIMEOUT)); // one thread overhead
        props.put("mail.smtps.timeout", Integer.toString(NETWORK_TIMEOUT));

        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.auth", "true");

        props.put("mail.smtp.connectiontimeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.smtp.writetimeout", Integer.toString(NETWORK_TIMEOUT)); // one thread overhead
        props.put("mail.smtp.timeout", Integer.toString(NETWORK_TIMEOUT));

        props.put("mail.imaps.peek", "true");
        //props.put("mail.imaps.minidletime", "5000");

        props.put("mail.mime.address.strict", "false");
        props.put("mail.mime.decodetext.strict", "false");

        props.put("mail.mime.ignoreunknownencoding", "true"); // Content-Transfer-Encoding
        props.put("mail.mime.decodefilename", "true");
        props.put("mail.mime.encodefilename", "true");

        // https://docs.oracle.com/javaee/6/api/javax/mail/internet/MimeMultipart.html
        props.put("mail.mime.multipart.ignoremissingboundaryparameter", "true"); // javax.mail.internet.ParseException: In parameter list
        props.put("mail.mime.multipart.ignoreexistingboundaryparameter", "true");

        // The documentation is unclear/inconsistent whether this are system or session properties:
        System.setProperty("mail.mime.address.strict", "false");
        System.setProperty("mail.mime.decodetext.strict", "false");

        System.setProperty("mail.mime.ignoreunknownencoding", "true"); // Content-Transfer-Encoding
        System.setProperty("mail.mime.decodefilename", "true");
        System.setProperty("mail.mime.encodefilename", "true");

        System.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true"); // javax.mail.internet.ParseException: In parameter list
        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");

        if (prefs.getBoolean("ipv4", false)) {
            Log.i(Helper.TAG, "Prefering IPv4");
            System.setProperty("java.net.preferIPv4Stack", "true");
        }

        // https://javaee.github.io/javamail/OAuth2
        Log.i(Helper.TAG, "Auth type=" + auth_type);
        if (auth_type == Helper.AUTH_TYPE_GMAIL) {
            props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtps.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        }

        return props;
    }

    static MimeMessageEx from(Context context, EntityMessage message, EntityMessage reply, List<EntityAttachment> attachments, Session isession) throws MessagingException, IOException {
        MimeMessageEx imessage = new MimeMessageEx(isession, message.msgid);

        if (reply == null)
            imessage.addHeader("References", message.msgid);
        else {
            imessage.addHeader("In-Reply-To", reply.msgid);
            imessage.addHeader("References", (reply.references == null ? "" : reply.references + " ") + reply.msgid);
        }

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

        if (attachments.size() == 0)
            imessage.setText(message.read(context), Charset.defaultCharset().name(), "html");
        else {
            Multipart multipart = new MimeMultipart();

            BodyPart bpMessage = new MimeBodyPart();
            bpMessage.setContent(message.read(context), "text/html; charset=" + Charset.defaultCharset().name());
            multipart.addBodyPart(bpMessage);

            for (final EntityAttachment attachment : attachments)
                if (attachment.available) {
                    BodyPart bpAttachment = new MimeBodyPart();
                    bpAttachment.setFileName(attachment.name);

                    File file = EntityAttachment.getFile(context, attachment.id);
                    FileDataSource dataSource = new FileDataSource(file);
                    dataSource.setFileTypeMap(new FileTypeMap() {
                        @Override
                        public String getContentType(File file) {
                            return attachment.type;
                        }

                        @Override
                        public String getContentType(String filename) {
                            return attachment.type;
                        }
                    });
                    bpAttachment.setDataHandler(new DataHandler(dataSource));
                    if (attachment.cid != null)
                        bpAttachment.setHeader("Content-ID", attachment.cid);

                    multipart.addBodyPart(bpAttachment);
                }

            imessage.setContent(multipart);
        }

        imessage.setSentDate(new Date());

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

    boolean getFlagged() throws MessagingException {
        return imessage.isSet(Flags.Flag.FLAGGED);
    }

    String getMessageID() throws MessagingException {
        return imessage.getHeader("Message-ID", null);
    }

    String[] getReferences() throws MessagingException {
        String refs = imessage.getHeader("References", null);
        return (refs == null ? new String[0] : refs.split("\\s+"));
    }

    String getDeliveredTo() throws MessagingException {
        return imessage.getHeader("Delivered-To", imessage.getHeader("X-Delivered-To", null));
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
        String[] headers = imessage.getHeader("Reply-To");
        if (headers != null && headers.length > 0)
            return imessage.getReplyTo();
        else
            return null;
    }

    Integer getSize() throws MessagingException {
        int size = imessage.getSize();
        return (size < 0 ? null : size);
    }

    static String getFormattedAddresses(Address[] addresses, boolean full) {
        if (addresses == null || addresses.length == 0)
            return "";

        List<String> formatted = new ArrayList<>();
        for (Address address : addresses)
            if (address instanceof InternetAddress) {
                InternetAddress a = (InternetAddress) address;
                String personal = a.getPersonal();
                if (TextUtils.isEmpty(personal))
                    formatted.add(address.toString());
                else {
                    personal = personal.replaceAll("[\\,\\<\\>]", "");
                    if (full)
                        formatted.add(personal + " <" + a.getAddress() + ">");
                    else
                        formatted.add(personal);
                }
            } else
                formatted.add(address.toString());
        return TextUtils.join(", ", formatted);
    }

    String getHtml() throws MessagingException, IOException {
        return getHtml(imessage);
    }

    private static String getHtml(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/*")) {
            String s;
            try {
                s = part.getContent().toString();
            } catch (UnsupportedEncodingException ex) {
                // x-binaryenc
                Log.w(Helper.TAG, "Unsupported encoding: " + part.getContentType());
                // https://javaee.github.io/javamail/FAQ#unsupen
                InputStream is = part.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                for (int len = is.read(buffer); len != -1; len = is.read(buffer))
                    os.write(buffer, 0, len);
                s = new String(os.toByteArray(), "US-ASCII");
            } catch (IOException ex) {
                // IOException; Unknown encoding: none
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                s = ex.toString();
            }

            if (part.isMimeType("text/plain"))
                s = "<pre>" + s.replaceAll("\\r?\\n", "<br />") + "</pre>";
            return s;
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
            } catch (ParseException ex) {
                // ParseException: In parameter list boundary="...">, expected parameter name, got ";"
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                text = ex.toString();
            }
            return text;
        }

        if (part.isMimeType("multipart/*"))
            try {
                Multipart mp = (Multipart) part.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    String s = getHtml(mp.getBodyPart(i));
                    if (s != null)
                        return s;
                }
            } catch (ParseException ex) {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                return ex.toString();
            }

        return null;
    }

    public List<EntityAttachment> getAttachments() throws IOException, MessagingException {
        List<EntityAttachment> result = new ArrayList<>();

        try {
            Object content = imessage.getContent();
            if (content instanceof String)
                return result;

            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int i = 0; i < multipart.getCount(); i++)
                    result.addAll(getAttachments(multipart.getBodyPart(i)));
            }
        } catch (ParseException ex) {
            Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
        }

        return result;
    }

    private static List<EntityAttachment> getAttachments(BodyPart part) throws
            IOException, MessagingException {
        List<EntityAttachment> result = new ArrayList<>();

        Object content;
        try {
            content = part.getContent();
        } catch (UnsupportedEncodingException ex) {
            Log.w(Helper.TAG, "attachment content type=" + part.getContentType());
            content = part.getInputStream();
        } catch (ParseException ex) {
            Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            content = null;
        }

        if (content instanceof InputStream || content instanceof String) {
            String disposition;
            try {
                disposition = part.getDisposition();
            } catch (MessagingException ex) {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                disposition = null;
            }

            String filename;
            try {
                filename = part.getFileName();
            } catch (MessagingException ex) {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                filename = null;
            }

            if (Part.ATTACHMENT.equalsIgnoreCase(disposition) ||
                    part.isMimeType("image/*") ||
                    !TextUtils.isEmpty(filename)) {
                ContentType ct = new ContentType(part.getContentType());
                String[] cid = part.getHeader("Content-ID");

                EntityAttachment attachment = new EntityAttachment();
                attachment.name = filename;
                attachment.type = ct.getBaseType().toLowerCase();
                attachment.size = part.getSize();
                attachment.cid = (cid == null || cid.length == 0 ? null : cid[0]);
                attachment.part = part;

                // Try to guess a better content type
                // Sometimes PDF files are sent using the wrong type
                if ("application/octet-stream".equals(attachment.type)) {
                    String extension = Helper.getExtension(attachment.name);
                    if (extension != null) {
                        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                        if (type != null) {
                            Log.w(Helper.TAG, "Guessing file=" + attachment.name + " type=" + type);
                            attachment.type = type;
                        }
                    }
                }

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
