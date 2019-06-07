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
import android.net.MailTo;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MessageRemovedIOException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import biweekly.Biweekly;
import biweekly.ICalendar;

public class MessageHelper {
    private MimeMessage imessage;

    private final static int CONNECT_TIMEOUT = 20 * 1000; // milliseconds
    private final static int WRITE_TIMEOUT = 40 * 1000; // milliseconds
    private final static int READ_TIMEOUT = 40 * 1000; // milliseconds
    private final static int FETCH_SIZE = 256 * 1024; // bytes, default 16K
    private final static int POOL_TIMEOUT = 45 * 1000; // milliseconds, default 45 sec

    static final int ATTACHMENT_BUFFER_SIZE = 8192; // bytes
    static final int DEFAULT_ATTACHMENT_DOWNLOAD_SIZE = 65536; // bytes

    static void setSystemProperties() {
        System.setProperty("mail.mime.decodetext.strict", "false");

        System.setProperty("mail.mime.ignoreunknownencoding", "true"); // Content-Transfer-Encoding
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
        System.setProperty("mail.mime.decodefilename", "true");
        System.setProperty("mail.mime.encodefilename", "true");
        System.setProperty("mail.mime.allowutf8", "false"); // InternetAddress, MimeBodyPart, MimeUtility

        // https://docs.oracle.com/javaee/6/api/javax/mail/internet/MimeMultipart.html
        System.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true"); // javax.mail.internet.ParseException: In parameter list
        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
    }

    static Properties getSessionProperties(int auth_type, String realm, boolean insecure) {
        Properties props = new Properties();

        props.put("mail.event.scope", "folder");

        String checkserveridentity = Boolean.toString(!insecure).toLowerCase();

        // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#properties
        props.put("mail.imaps.ssl.checkserveridentity", checkserveridentity);
        props.put("mail.imaps.ssl.trust", "*");
        props.put("mail.imaps.starttls.enable", "false");

        if (realm != null)
            props.put("mail.imaps.auth.ntlm.domain", realm);

        // TODO: make timeouts configurable?
        props.put("mail.imaps.connectiontimeout", Integer.toString(CONNECT_TIMEOUT));
        props.put("mail.imaps.writetimeout", Integer.toString(WRITE_TIMEOUT)); // one thread overhead
        props.put("mail.imaps.timeout", Integer.toString(READ_TIMEOUT));

        props.put("mail.imaps.connectionpool.debug", "true");
        props.put("mail.imaps.connectionpoolsize", "2");
        props.put("mail.imaps.connectionpooltimeout", Integer.toString(POOL_TIMEOUT));

        props.put("mail.imaps.finalizecleanclose", "false");

        // https://tools.ietf.org/html/rfc4978
        // https://docs.oracle.com/javase/8/docs/api/java/util/zip/Deflater.html
        props.put("mail.imaps.compress.enable", "true");
        //props.put("mail.imaps.compress.level", "-1");
        //props.put("mail.imaps.compress.strategy", "0");

        props.put("mail.imaps.throwsearchexception", "true");
        props.put("mail.imaps.fetchsize", Integer.toString(FETCH_SIZE));
        props.put("mail.imaps.peek", "true");

        props.put("mail.imap.ssl.checkserveridentity", checkserveridentity);
        props.put("mail.imap.ssl.trust", "*");
        props.put("mail.imap.starttls.enable", "true");
        props.put("mail.imap.starttls.required", "true");

        if (realm != null)
            props.put("mail.imap.auth.ntlm.domain", realm);

        props.put("mail.imap.connectiontimeout", Integer.toString(CONNECT_TIMEOUT));
        props.put("mail.imap.writetimeout", Integer.toString(WRITE_TIMEOUT)); // one thread overhead
        props.put("mail.imap.timeout", Integer.toString(READ_TIMEOUT));

        props.put("mail.imap.connectionpool.debug", "true");
        props.put("mail.imap.connectionpoolsize", "2");
        props.put("mail.imap.connectionpooltimeout", Integer.toString(POOL_TIMEOUT));

        props.put("mail.imap.finalizecleanclose", "false");

        props.put("mail.imap.compress.enable", "true");

        props.put("mail.imap.throwsearchexception", "true");
        props.put("mail.imap.fetchsize", Integer.toString(FETCH_SIZE));
        props.put("mail.imap.peek", "true");

        // https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html#properties
        props.put("mail.smtps.ssl.checkserveridentity", checkserveridentity);
        props.put("mail.smtps.ssl.trust", "*");
        props.put("mail.smtps.starttls.enable", "false");
        props.put("mail.smtps.starttls.required", "false");

        props.put("mail.smtps.auth", "true");
        if (realm != null)
            props.put("mail.smtps.auth.ntlm.domain", realm);

        props.put("mail.smtps.connectiontimeout", Integer.toString(CONNECT_TIMEOUT));
        props.put("mail.smtps.writetimeout", Integer.toString(WRITE_TIMEOUT)); // one thread overhead
        props.put("mail.smtps.timeout", Integer.toString(READ_TIMEOUT));

        props.put("mail.smtp.ssl.checkserveridentity", checkserveridentity);
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        props.put("mail.smtp.auth", "true");
        if (realm != null)
            props.put("mail.smtp.auth.ntlm.domain", realm);

        props.put("mail.smtp.connectiontimeout", Integer.toString(CONNECT_TIMEOUT));
        props.put("mail.smtp.writetimeout", Integer.toString(WRITE_TIMEOUT)); // one thread overhead
        props.put("mail.smtp.timeout", Integer.toString(READ_TIMEOUT));

        // MIME
        props.put("mail.mime.allowutf8", "false"); // SMTPTransport, MimeMessage
        props.put("mail.mime.address.strict", "false");

        if (false) {
            Log.i("Prefering IPv4");
            System.setProperty("java.net.preferIPv4Stack", "true");
        }

        // https://javaee.github.io/javamail/OAuth2
        Log.i("Auth type=" + auth_type);
        if (auth_type == ConnectionHelper.AUTH_TYPE_GMAIL) {
            props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
            props.put("mail.imap.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtps.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        }

        return props;
    }

    static MimeMessageEx from(Context context, EntityMessage message, EntityIdentity identity, Session isession)
            throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        MimeMessageEx imessage = new MimeMessageEx(isession, message.msgid);

        if (message.references != null)
            imessage.addHeader("References", message.references);
        if (message.inreplyto != null)
            imessage.addHeader("In-Reply-To", message.inreplyto);

        imessage.addHeader("X-Mailer", context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        imessage.addHeader("X-FairEmail-ID", message.msgid);

        imessage.setFlag(Flags.Flag.SEEN, message.seen);
        imessage.setFlag(Flags.Flag.FLAGGED, message.flagged);
        imessage.setFlag(Flags.Flag.ANSWERED, message.answered);

        if (message.from != null && message.from.length > 0) {
            String email = ((InternetAddress) message.from[0]).getAddress();
            String name = ((InternetAddress) message.from[0]).getPersonal();
            if (email != null && identity != null && identity.sender_extra && !TextUtils.isEmpty(message.extra)) {
                int at = email.indexOf('@');
                email = message.extra + email.substring(at);
                Log.i("extra=" + email);
            }
            imessage.setFrom(new InternetAddress(email, name));
        }

        if (message.to != null && message.to.length > 0)
            imessage.setRecipients(Message.RecipientType.TO, message.to);

        if (message.cc != null && message.cc.length > 0)
            imessage.setRecipients(Message.RecipientType.CC, message.cc);

        if (message.bcc != null && message.bcc.length > 0)
            imessage.setRecipients(Message.RecipientType.BCC, message.bcc);

        if (message.subject != null)
            imessage.setSubject(message.subject);

        imessage.setSentDate(new Date());

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);

        if (message.from != null && message.from.length > 0)
            for (EntityAttachment attachment : attachments)
                if (attachment.available && EntityAttachment.PGP_SIGNATURE.equals(attachment.encryption)) {
                    InternetAddress from = (InternetAddress) message.from[0];
                    File file = attachment.getFile(context);
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = br.readLine()) != null)
                            if (!line.startsWith("-----") && !line.endsWith("-----"))
                                sb.append(line);
                    }

                    imessage.addHeader("Autocrypt", "addr=" + from.getAddress() + "; keydata=" + sb.toString());
                }

        for (final EntityAttachment attachment : attachments)
            if (attachment.available && EntityAttachment.PGP_MESSAGE.equals(attachment.encryption)) {
                Multipart multipart = new MimeMultipart("encrypted; protocol=\"application/pgp-encrypted\"");

                BodyPart pgp = new MimeBodyPart();
                pgp.setContent("", "application/pgp-encrypted");
                multipart.addBodyPart(pgp);

                BodyPart bpAttachment = new MimeBodyPart();
                bpAttachment.setFileName(attachment.name);

                File file = attachment.getFile(context);
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
                bpAttachment.setDisposition(Part.INLINE);

                multipart.addBodyPart(bpAttachment);

                imessage.setContent(multipart);

                return imessage;
            }

        build(context, message, attachments, identity, imessage);

        return imessage;
    }

    static void build(Context context, EntityMessage message, List<EntityAttachment> attachments, EntityIdentity identity, MimeMessage imessage) throws IOException, MessagingException {
        if (message.receipt_request != null && message.receipt_request) {
            // https://www.ietf.org/rfc/rfc3798.txt
            Multipart report = new MimeMultipart("report; report-type=disposition-notification");

            String plainContent = HtmlHelper.getText(Helper.readText(message.getFile(context)));

            BodyPart plainPart = new MimeBodyPart();
            plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            report.addBodyPart(plainPart);

            BodyPart dnsPart = new MimeBodyPart();
            dnsPart.setContent("", "message/disposition-notification; name=\"MDNPart2.txt\"");
            dnsPart.setDisposition(Part.INLINE);
            report.addBodyPart(dnsPart);

            //BodyPart headersPart = new MimeBodyPart();
            //headersPart.setContent("", "text/rfc822-headers; name=\"MDNPart3.txt\"");
            //headersPart.setDisposition(Part.INLINE);
            //report.addBodyPart(headersPart);

            imessage.setContent(report);
            return;
        }

        StringBuilder body = new StringBuilder();
        body.append(Helper.readText(message.getFile(context)));

        if (identity != null && !TextUtils.isEmpty(identity.signature))
            body.append(identity.signature);

        File refFile = message.getRefFile(context);
        if (refFile.exists())
            body.append(Helper.readText(refFile));

        String plainContent = HtmlHelper.getText(body.toString());

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append(body.toString()).append("\n");

        BodyPart plainPart = new MimeBodyPart();
        plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());

        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent.toString(), "text/html; charset=" + Charset.defaultCharset().name());

        Multipart alternativePart = new MimeMultipart("alternative");
        alternativePart.addBodyPart(plainPart);
        alternativePart.addBodyPart(htmlPart);

        int available = 0;
        for (EntityAttachment attachment : attachments)
            if (attachment.available)
                available++;
        Log.i("Attachments available=" + available);

        if (available == 0)
            if (message.plain_only != null && message.plain_only)
                imessage.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            else
                imessage.setContent(alternativePart);
        else {
            Multipart mixedPart = new MimeMultipart("mixed");

            BodyPart attachmentPart = new MimeBodyPart();
            if (message.plain_only != null && message.plain_only)
                attachmentPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            else
                attachmentPart.setContent(alternativePart);
            mixedPart.addBodyPart(attachmentPart);

            for (final EntityAttachment attachment : attachments)
                if (attachment.available) {
                    BodyPart bpAttachment = new MimeBodyPart();

                    File file = attachment.getFile(context);

                    FileDataSource dataSource = new FileDataSource(file);
                    dataSource.setFileTypeMap(new FileTypeMap() {
                        @Override
                        public String getContentType(File file) {
                            // https://tools.ietf.org/html/rfc6047
                            if ("text/calendar".equals(attachment.type))
                                try {
                                    ICalendar icalendar = Biweekly.parse(file).first();
                                    if (icalendar != null &&
                                            icalendar.getMethod() != null &&
                                            icalendar.getMethod().isReply())
                                        return "text/calendar" +
                                                "; method=REPLY" +
                                                "; charset=" + Charset.defaultCharset().name();
                                } catch (IOException ex) {
                                    Log.e(ex);
                                }

                            return attachment.type;
                        }

                        @Override
                        public String getContentType(String filename) {
                            return getContentType(new File(filename));
                        }
                    });
                    bpAttachment.setDataHandler(new DataHandler(dataSource));

                    bpAttachment.setFileName(attachment.name);
                    if (attachment.disposition != null)
                        bpAttachment.setDisposition(attachment.disposition);
                    if (attachment.cid != null)
                        bpAttachment.setHeader("Content-ID", attachment.cid);

                    mixedPart.addBodyPart(bpAttachment);
                }

            imessage.setContent(mixedPart);
        }
    }

    MessageHelper(MimeMessage message) {
        this.imessage = message;
    }

    boolean getSeen() throws MessagingException {
        return imessage.isSet(Flags.Flag.SEEN);
    }

    boolean getAnsered() throws MessagingException {
        return imessage.isSet(Flags.Flag.ANSWERED);
    }

    boolean getFlagged() throws MessagingException {
        return imessage.isSet(Flags.Flag.FLAGGED);
    }

    String getFlags() throws MessagingException {
        if (!BuildConfig.DEBUG)
            return null;

        Flags flags = imessage.getFlags();
        flags.clearUserFlags();
        return flags.toString();
    }

    String[] getKeywords() throws MessagingException {
        return imessage.getFlags().getUserFlags();
    }

    String getMessageID() throws MessagingException {
        // Outlook outbox -> sent
        String header = imessage.getHeader("X-FairEmail-ID", null);
        if (header == null)
            header = imessage.getHeader("Message-ID", null);
        return (header == null ? null : MimeUtility.unfold(header));
    }

    String[] getReferences() throws MessagingException {
        String refs = imessage.getHeader("References", null);
        return (refs == null ? new String[0] : MimeUtility.unfold(refs).split("\\s+"));
    }

    String getDeliveredTo() throws MessagingException {
        String header = imessage.getHeader("Delivered-To", null);
        if (header == null)
            header = imessage.getHeader("X-Delivered-To", null);
        return (header == null ? null : MimeUtility.unfold(header));
    }

    String getInReplyTo() throws MessagingException {
        String header = imessage.getHeader("In-Reply-To", null);
        return (header == null ? null : MimeUtility.unfold(header));
    }

    String getThreadId(Context context, long account, long uid) throws MessagingException {
        List<String> refs = new ArrayList<>();

        for (String ref : getReferences())
            if (!TextUtils.isEmpty(ref))
                refs.add(ref);

        String inreplyto = getInReplyTo();
        if (!TextUtils.isEmpty(inreplyto) && !refs.contains(inreplyto))
            refs.add(inreplyto);

        DB db = DB.getInstance(context);
        for (String ref : refs) {
            List<EntityMessage> messages = db.message().getMessageByMsgId(account, ref);
            if (messages.size() > 0)
                return messages.get(0).thread;
        }

        if (refs.size() > 0)
            return refs.get(0);

        String msgid = getMessageID();
        return (TextUtils.isEmpty(msgid) ? Long.toString(uid) : msgid);
    }

    boolean getReceiptRequested() throws MessagingException {
        return (imessage.getHeader("Return-Receipt-To") != null ||
                imessage.getHeader("Disposition-Notification-To") != null);
    }

    Address[] getReceiptTo() throws MessagingException {
        try {
            String to = imessage.getHeader("Disposition-Notification-To", null);
            if (to == null)
                return null;

            to = MimeUtility.unfold(to);

            InternetAddress[] address = null;
            try {
                address = InternetAddress.parse(to);
            } catch (AddressException ex) {
                Log.w(ex);
            }

            if (address == null || address.length == 0)
                return null;

            fix(address[0]);

            return new Address[]{address[0]};
        } catch (AddressException ex) {
            Log.w(ex);
            return null;
        }
    }

    String getAuthentication() throws MessagingException {
        String header = imessage.getHeader("Authentication-Results", null);
        return (header == null ? null : MimeUtility.unfold(header));
    }

    static Boolean getAuthentication(String type, String header) {
        if (header == null)
            return null;

        // https://tools.ietf.org/html/rfc7601
        Boolean result = null;
        String[] part = header.split(";");
        for (int i = 1; i < part.length; i++) {
            String[] kv = part[i].split("=");
            if (kv.length > 1) {
                String key = kv[0].trim();
                String[] val = kv[1].trim().split(" ");
                if (val.length > 0 && type.equals(key)) {
                    if ("fail".equals(val[0]))
                        result = false;
                    else if ("pass".equals(val[0]))
                        if (result == null)
                            result = true;
                }
            }
        }

        return result;
    }

    Address[] getFrom() throws MessagingException {
        return fix(imessage.getFrom());
    }

    Address[] getTo() throws MessagingException {
        return fix(imessage.getRecipients(Message.RecipientType.TO));
    }

    Address[] getCc() throws MessagingException {
        return fix(imessage.getRecipients(Message.RecipientType.CC));
    }

    Address[] getBcc() throws MessagingException {
        return fix(imessage.getRecipients(Message.RecipientType.BCC));
    }

    Address[] getReply() throws MessagingException {
        // Prevent getting To header
        String[] headers = imessage.getHeader("Reply-To");
        if (headers != null && headers.length > 0)
            return fix(imessage.getReplyTo());
        else
            return null;
    }

    Address[] getListPost() throws MessagingException {
        try {
            // https://www.ietf.org/rfc/rfc2369.txt
            String list = imessage.getHeader("List-Post", null);
            if (list == null)
                return null;

            list = MimeUtility.unfold(list);
            if ("NO".equals(list))
                return null;

            String[] to = list.split(",");
            if (to.length < 1 || !to[0].startsWith("<") || !to[0].endsWith(">"))
                return null;

            // https://www.ietf.org/rfc/rfc2368.txt
            MailTo mailto = MailTo.parse(to[0].substring(1, to[0].length() - 1));
            if (mailto.getTo() == null)
                return null;

            return new Address[]{new InternetAddress(mailto.getTo().split(",")[0])};
        } catch (android.net.ParseException ex) {
            Log.w(ex);
            return null;
        } catch (AddressException ex) {
            Log.w(ex);
            return null;
        }
    }

    private static Address[] fix(Address[] addresses) {
        if (addresses != null)
            for (int i = 0; i < addresses.length; i++)
                fix((InternetAddress) addresses[i]);
        return addresses;
    }

    private static void fix(InternetAddress address) {
        try {
            String email = decodeMime(address.getAddress());
            String personal = decodeMime(address.getPersonal());
            try {
                InternetAddress[] a = InternetAddress.parse(email);
                if (a.length < 1)
                    throw new AddressException("empty");
                String p = a[0].getPersonal();
                address.setAddress(a[0].getAddress());
                address.setPersonal(TextUtils.isEmpty(personal) ? p : personal + (p == null ? "" : " " + p));
            } catch (AddressException ex) {
                Log.w(ex);
                address.setAddress(email);
                address.setPersonal(personal);
            }
        } catch (UnsupportedEncodingException ex) {
            Log.w(ex);
        }
    }

    String getSubject() throws MessagingException {
        String subject = imessage.getHeader("Subject", null);
        if (subject == null)
            return null;

        subject = MimeUtility.unfold(subject);

        if (subject.contains("=?")) {
            // Decode header
            try {
                subject = MimeUtility.decodeText(subject);
            } catch (UnsupportedEncodingException ex) {
                Log.w(ex);
            }
        } else
            subject = fixUTF8(subject);

        return decodeMime(subject);
    }

    Long getSize() throws MessagingException {
        long size = imessage.getSize();
        if (size == 0)
            throw new MessagingException("Message empty");
        return (size < 0 ? null : size);
    }

    long getReceived() throws MessagingException {
        return imessage.getReceivedDate().getTime();
    }

    Long getSent() throws MessagingException {
        Date date = imessage.getSentDate();
        return (date == null ? null : date.getTime());
    }

    String getHeaders() throws MessagingException {
        StringBuilder sb = new StringBuilder();
        Enumeration<Header> headers = imessage.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header header = headers.nextElement();
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        return sb.toString();
    }

    static String formatAddresses(Address[] addresses) {
        return formatAddresses(addresses, true, false);
    }

    static String formatAddressesShort(Address[] addresses) {
        return formatAddresses(addresses, false, false);
    }

    static String formatAddressesCompose(Address[] addresses) {
        String result = formatAddresses(addresses, true, true);
        if (!TextUtils.isEmpty(result))
            result += ", ";
        return result;
    }

    static String formatAddresses(Address[] addresses, boolean full, boolean compose) {
        if (addresses == null || addresses.length == 0)
            return "";

        List<String> formatted = new ArrayList<>();
        for (Address address : addresses)
            if (address instanceof InternetAddress) {
                InternetAddress a = (InternetAddress) address;
                String personal = a.getPersonal();
                if (TextUtils.isEmpty(personal))
                    formatted.add(a.getAddress());
                else {
                    if (compose) {
                        boolean quote = false;
                        for (int i = 0; i < personal.length(); i++)
                            if ("()<>,;:\\\"[]@".indexOf(personal.charAt(i)) >= 0) {
                                quote = true;
                                break;
                            }
                        if (quote)
                            personal = "\"" + personal + "\"";
                    }

                    if (full)
                        formatted.add(personal + " <" + a.getAddress() + ">");
                    else
                        formatted.add(personal);
                }
            } else
                formatted.add(address.toString());
        return TextUtils.join(", ", formatted);
    }

    static String canonicalAddress(String address) {
        String[] a = address.split("@");
        if (a.length > 0) {
            String[] extra = a[0].split("\\+");
            if (extra.length > 0)
                a[0] = extra[0];
        }
        return TextUtils.join("@", a).toLowerCase();
    }

    static String decodeMime(String text) {
        if (text == null)
            return null;

        int i = 0;
        while (i < text.length()) {
            int s = text.indexOf("=?", i);
            if (s < 0)
                break;

            int e = text.indexOf("?=", s + 2);
            if (e < 0)
                break;

            String decode = text.substring(s, e + 2);
            try {
                String decoded = MimeUtility.decodeWord(decode);
                text = text.substring(0, s) + decoded + text.substring(e + 2);
                i += decoded.length();
            } catch (ParseException ex) {
                Log.w(ex);
                i += decode.length();
            } catch (UnsupportedEncodingException ex) {
                Log.w(ex);
                i += decode.length();
            }
        }

        return text;
    }

    static String fixUTF8(String text) {
        try {
            char[] kars = text.toCharArray();
            byte[] bytes = new byte[kars.length];
            for (int i = 0; i < kars.length; i++)
                bytes[i] = (byte) kars[i];

            CharsetDecoder cs = StandardCharsets.UTF_8.newDecoder();
            CharBuffer out = cs.decode(ByteBuffer.wrap(bytes));
            if (out.length() > 0)
                return new String(bytes, StandardCharsets.UTF_8);
        } catch (CharacterCodingException ex) {
            Log.w(ex);
        }

        return text;
    }

    static String getSortKey(Address[] addresses) {
        if (addresses == null || addresses.length == 0)
            return null;
        InternetAddress address = (InternetAddress) addresses[0];
        // Sort on name will result in inconsistent results
        // because the sender name and sender contact name can differ
        return address.getAddress();
    }

    class MessageParts {
        private Part plain = null;
        private Part html = null;
        private List<AttachmentPart> attachments = new ArrayList<>();
        private ArrayList<String> warnings = new ArrayList<>();

        Boolean isPlainOnly() {
            if (plain == null && html == null)
                return null;
            return (html == null);
        }

        String getHtml(Context context) throws MessagingException {
            if (plain == null && html == null) {
                warnings.add(context.getString(R.string.title_no_body));
                return null;
            }

            String result;
            boolean text = false;
            Part part = (html == null ? plain : html);

            try {
                Object content = part.getContent();
                if (content instanceof String)
                    result = (String) content;
                else if (content instanceof InputStream)
                    // Typically com.sun.mail.util.QPDecoderStream
                    result = readStream((InputStream) content, "UTF-8");
                else
                    result = content.toString();
            } catch (FolderClosedException ex) {
                throw ex;
            } catch (FolderClosedIOException ex) {
                throw new FolderClosedException(ex.getFolder(), "getHtml", ex);
            } catch (Throwable ex) {
                Log.w(ex);
                warnings.add(Helper.formatThrowable(ex));
                return null;
            }

            try {
                ContentType ct = new ContentType(part.getContentType());
                String charset = ct.getParameter("charset");
                if (TextUtils.isEmpty(charset)) {
                    if (BuildConfig.DEBUG)
                        warnings.add(context.getString(R.string.title_no_charset, ct.toString()));
                    if (part.isMimeType("text/plain")) {
                        // The first 127 characters are the same as in US-ASCII
                        result = new String(result.getBytes(StandardCharsets.ISO_8859_1));
                    }
                } else {
                    if ("US-ASCII".equals(Charset.forName(charset).name()) &&
                            !"US-ASCII".equals(charset.toUpperCase()))
                        warnings.add(context.getString(R.string.title_no_charset, charset));
                    if (part.isMimeType("text/plain") && "US-ASCII".equals(charset.toUpperCase()))
                        result = fixUTF8(result);
                }
            } catch (ParseException ex) {
                Log.w(ex);
                warnings.add(Helper.formatThrowable(ex));
            }

            if (part.isMimeType("text/plain") || text) {
                result = TextUtils.htmlEncode(result);
                result = result.replaceAll("\\r?\\n", "<br />");
                result = "<span>" + result + "</span>";
            }

            return result;
        }

        List<AttachmentPart> getAttachmentParts() {
            return attachments;
        }

        List<EntityAttachment> getAttachments() {
            List<EntityAttachment> result = new ArrayList<>();
            for (AttachmentPart apart : attachments)
                result.add(apart.attachment);
            return result;
        }

        void downloadAttachment(Context context, int index, long id, String name) throws MessagingException, IOException {
            Log.i("downloading attachment id=" + id);

            DB db = DB.getInstance(context);

            // Get data
            AttachmentPart apart = attachments.get(index);

            // Download attachment
            File file = EntityAttachment.getFile(context, id, name);
            db.attachment().setProgress(id, null);
            try (InputStream is = apart.part.getInputStream()) {
                long size = 0;
                long total = apart.part.getSize();
                int lastprogress = 0;

                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                    byte[] buffer = new byte[ATTACHMENT_BUFFER_SIZE];
                    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                        size += len;
                        os.write(buffer, 0, len);

                        // Update progress
                        if (total > 0) {
                            int progress = (int) (size * 100 / total / 20 * 20);
                            if (progress != lastprogress) {
                                lastprogress = progress;
                                db.attachment().setProgress(id, progress);
                            }
                        }
                    }
                }

                // Store attachment data
                db.attachment().setDownloaded(id, size);

                Log.i("Downloaded attachment size=" + size);
            } catch (FolderClosedIOException ex) {
                db.attachment().setError(id, Helper.formatThrowable(ex));
                throw new FolderClosedException(ex.getFolder(), "downloadAttachment", ex);
            } catch (MessageRemovedIOException ex) {
                db.attachment().setError(id, Helper.formatThrowable(ex));
                throw new MessagingException("downloadAttachment", ex);
            } catch (Throwable ex) {
                // Reset progress on failure
                db.attachment().setError(id, Helper.formatThrowable(ex));
                throw ex;
            }
        }

        String getWarnings(String existing) {
            if (existing != null)
                warnings.add(0, existing);
            if (warnings.size() == 0)
                return null;
            else
                return TextUtils.join(", ", warnings);
        }
    }

    class AttachmentPart {
        String disposition;
        String filename;
        boolean pgp;
        Part part;
        EntityAttachment attachment;
    }

    MessageParts getMessageParts() throws IOException, FolderClosedException {
        MessageParts parts = new MessageParts();

        MimeMessage cmessage = imessage;
        try {
            // Load body structure
            cmessage.getContentID();
        } catch (MessagingException ex) {
            // https://javaee.github.io/javamail/FAQ#imapserverbug
            if ("Unable to load BODYSTRUCTURE".equals(ex.getMessage())) {
                Log.w(ex);
                parts.warnings.add(Helper.formatThrowable(ex));
                try {
                    cmessage = new MimeMessage(imessage);
                } catch (MessagingException ignored) {
                }
            }
        }

        getMessageParts(cmessage, parts, false);

        return parts;
    }

    private void getMessageParts(Part part, MessageParts parts, boolean pgp) throws IOException, FolderClosedException {
        try {
            if (part.isMimeType("multipart/*")) {
                Multipart multipart;
                Object content = part.getContent();
                if (content instanceof Multipart)
                    multipart = (Multipart) part.getContent();
                else if (content instanceof String) {
                    String text = (String) content;
                    String sample = text.substring(0, Math.min(80, text.length()));
                    throw new ParseException(content.getClass().getName() + ": " + sample);
                } else
                    throw new ParseException(content.getClass().getName());

                for (int i = 0; i < multipart.getCount(); i++)
                    try {
                        Part cpart = multipart.getBodyPart(i);
                        getMessageParts(cpart, parts, pgp);
                        ContentType ct = new ContentType(cpart.getContentType());
                        if ("application/pgp-encrypted".equals(ct.getBaseType().toLowerCase()))
                            pgp = true;
                    } catch (ParseException ex) {
                        // Nested body: try to continue
                        // ParseException: In parameter list boundary="...">, expected parameter name, got ";"
                        Log.w(ex);
                        parts.warnings.add(Helper.formatThrowable(ex));
                    }
            } else {
                // https://www.iana.org/assignments/cont-disp/cont-disp.xhtml
                String disposition;
                try {
                    disposition = part.getDisposition();
                    if (disposition != null)
                        disposition = disposition.toLowerCase();
                } catch (MessagingException ex) {
                    Log.w(ex);
                    parts.warnings.add(Helper.formatThrowable(ex));
                    disposition = null;
                }

                String filename;
                try {
                    filename = part.getFileName();
                } catch (MessagingException ex) {
                    Log.w(ex);
                    parts.warnings.add(Helper.formatThrowable(ex));
                    filename = null;
                }

                //Log.i("Part" +
                //        " disposition=" + disposition +
                //        " filename=" + filename +
                //        " content type=" + part.getContentType());

                if (!Part.ATTACHMENT.equalsIgnoreCase(disposition) &&
                        ((parts.plain == null && part.isMimeType("text/plain")) ||
                                (parts.html == null && part.isMimeType("text/html")))) {
                    if (part.isMimeType("text/plain"))
                        parts.plain = part;
                    else
                        parts.html = part;
                } else {
                    AttachmentPart apart = new AttachmentPart();
                    apart.disposition = disposition;
                    apart.filename = filename;
                    apart.pgp = pgp;
                    apart.part = part;

                    ContentType ct;
                    try {
                        ct = new ContentType(apart.part.getContentType());
                    } catch (ParseException ex) {
                        Log.w(ex);
                        parts.warnings.add(Helper.formatThrowable(ex));
                        ct = new ContentType("application/octet-stream");
                    }

                    String[] cid = null;
                    try {
                        cid = apart.part.getHeader("Content-ID");
                    } catch (MessagingException ex) {
                        Log.w(ex);
                        parts.warnings.add(Helper.formatThrowable(ex));
                    }

                    apart.attachment = new EntityAttachment();
                    apart.attachment.name = apart.filename;
                    apart.attachment.type = ct.getBaseType().toLowerCase();
                    apart.attachment.disposition = apart.disposition;
                    apart.attachment.size = (long) apart.part.getSize();
                    apart.attachment.cid = (cid == null || cid.length == 0 ? null : MimeUtility.unfold(cid[0]));
                    apart.attachment.encryption = (apart.pgp ? EntityAttachment.PGP_MESSAGE : null);

                    if ("text/calendar".equalsIgnoreCase(apart.attachment.type) && TextUtils.isEmpty(apart.attachment.name))
                        apart.attachment.name = "invite.ics";

                    // Try to guess a better content type
                    // Sometimes PDF files are sent using the wrong type
                    if ("application/octet-stream".equalsIgnoreCase(apart.attachment.type)) {
                        String extension = Helper.getExtension(apart.attachment.name);
                        if (extension != null) {
                            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                            if (type != null) {
                                Log.w("Guessing file=" + apart.attachment.name + " type=" + type);
                                apart.attachment.type = type;
                            }
                        }
                    }

                    if (apart.attachment.size <= 0)
                        apart.attachment.size = null;

                    // https://tools.ietf.org/html/rfc2392
                    if (apart.attachment.cid != null) {
                        if (!apart.attachment.cid.startsWith("<"))
                            apart.attachment.cid = "<" + apart.attachment.cid;
                        if (!apart.attachment.cid.endsWith(">"))
                            apart.attachment.cid += ">";
                    }

                    parts.attachments.add(apart);
                }
            }
        } catch (FolderClosedException ex) {
            throw ex;
        } catch (MessagingException ex) {
            Log.w(ex);
            parts.warnings.add(Helper.formatThrowable(ex));
        }
    }

    private static String readStream(InputStream is, String charset) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer))
            os.write(buffer, 0, len);
        return new String(os.toByteArray(), charset);
    }

    static boolean equal(Address[] a1, Address[] a2) {
        if (a1 == null && a2 == null)
            return true;

        if (a1 == null || a2 == null)
            return false;

        if (a1.length != a2.length)
            return false;

        for (int i = 0; i < a1.length; i++)
            if (!a1[i].toString().equals(a2[i].toString()))
                return false;

        return true;
    }
}
