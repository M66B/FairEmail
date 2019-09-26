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
import android.content.SharedPreferences;
import android.net.MailTo;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.preference.PreferenceManager;

import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MessageRemovedIOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import biweekly.Biweekly;
import biweekly.ICalendar;

public class MessageHelper {
    private MimeMessage imessage;

    static final int SMALL_MESSAGE_SIZE = 32 * 1024; // bytes
    static final int DEFAULT_ATTACHMENT_DOWNLOAD_SIZE = 256 * 1024; // bytes

    static void setSystemProperties(Context context) {
        System.setProperty("mail.mime.decodetext.strict", "false");

        System.setProperty("mail.mime.ignoreunknownencoding", "true"); // Content-Transfer-Encoding
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
        System.setProperty("mail.mime.decodefilename", "true");
        System.setProperty("mail.mime.encodefilename", "true");
        System.setProperty("mail.mime.allowutf8", "false"); // InternetAddress, MimeBodyPart, MimeUtility
        System.setProperty("mail.mime.cachemultipart", "false");

        // https://docs.oracle.com/javaee/6/api/javax/mail/internet/MimeMultipart.html
        System.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true"); // javax.mail.internet.ParseException: In parameter list
        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true");
    }

    static Properties getSessionProperties() {
        Properties props = new Properties();

        // MIME
        props.put("mail.mime.allowutf8", "false"); // SMTPTransport, MimeMessage
        props.put("mail.mime.address.strict", "false");

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

        imessage.addHeader("X-Correlation-ID", message.msgid);

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

        MailDateFormat mdf = new MailDateFormat();
        mdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        imessage.setHeader("Date", mdf.format(new Date()));
        //imessage.setSentDate(new Date());

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);

        if (message.from != null && message.from.length > 0)
            for (EntityAttachment attachment : attachments)
                if (attachment.available && EntityAttachment.PGP_KEY.equals(attachment.encryption)) {
                    InternetAddress from = (InternetAddress) message.from[0];
                    File file = attachment.getFile(context);
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = br.readLine()) != null)
                            if (!line.startsWith("-----") && !line.endsWith("-----"))
                                sb.append(line);
                    }

                    imessage.addHeader("Autocrypt",
                            "addr=" + from.getAddress() + ";" +
                                    " prefer-encrypt=mutual;" +
                                    " keydata=" + sb.toString());
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean usenet = prefs.getBoolean("usenet_signature", false);

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

        // Build html body
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");

        Document mdoc = Jsoup.parse(Helper.readText(message.getFile(context)));
        if (mdoc.body() != null)
            body.append(mdoc.body().html());

        // When sending message
        if (identity != null) {
            if (!TextUtils.isEmpty(identity.signature) && message.signature) {
                Document sdoc = Jsoup.parse(identity.signature);
                if (sdoc.body() != null) {
                    if (usenet) // https://www.ietf.org/rfc/rfc3676.txt
                        body.append("<span>-- <br></span>");
                    body.append(sdoc.body().html());
                }
            }

            File refFile = message.getRefFile(context);
            if (refFile.exists())
                body.append(Helper.readText(refFile));
        }

        body.append("</body></html>");

        // multipart/mixed
        //   multipart/related
        //     multipart/alternative
        //       text/plain
        //       text/html
        //     inlines
        //  attachments

        String htmlContent = body.toString();
        String plainContent = HtmlHelper.getText(htmlContent);

        BodyPart plainPart = new MimeBodyPart();
        plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());

        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=" + Charset.defaultCharset().name());

        Multipart altMultiPart = new MimeMultipart("alternative");
        altMultiPart.addBodyPart(plainPart);
        altMultiPart.addBodyPart(htmlPart);

        boolean plain_only = (message.plain_only != null && message.plain_only);

        int availableAttachments = 0;
        boolean hasInline = false;
        for (EntityAttachment attachment : attachments)
            if (attachment.available) {
                availableAttachments++;
                if (attachment.isInline())
                    hasInline = true;
            }

        if (availableAttachments == 0)
            if (plain_only)
                imessage.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            else
                imessage.setContent(altMultiPart);
        else {
            Multipart mixedMultiPart = new MimeMultipart("mixed");
            Multipart relatedMultiPart = new MimeMultipart("related");

            BodyPart bodyPart = new MimeBodyPart();
            if (plain_only)
                bodyPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            else
                bodyPart.setContent(altMultiPart);

            if (hasInline && !plain_only) {
                relatedMultiPart.addBodyPart(bodyPart);
                MimeBodyPart relatedPart = new MimeBodyPart();
                relatedPart.setContent(relatedMultiPart);
                mixedMultiPart.addBodyPart(relatedPart);
            } else
                mixedMultiPart.addBodyPart(bodyPart);

            for (final EntityAttachment attachment : attachments)
                if (attachment.available) {
                    BodyPart attachmentPart = new MimeBodyPart();

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
                    attachmentPart.setDataHandler(new DataHandler(dataSource));

                    attachmentPart.setFileName(attachment.name);
                    if (attachment.disposition != null)
                        attachmentPart.setDisposition(attachment.disposition);
                    if (attachment.cid != null)
                        attachmentPart.setHeader("Content-ID", attachment.cid);

                    if (attachment.isInline() && !plain_only)
                        relatedMultiPart.addBodyPart(attachmentPart);
                    else
                        mixedMultiPart.addBodyPart(attachmentPart);
                }

            imessage.setContent(mixedMultiPart);
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
        String header = imessage.getHeader("X-Correlation-ID", null);
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
        return getAddressHeader("Disposition-Notification-To");
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

    private Address[] getAddressHeader(String name) throws MessagingException {
        String header = imessage.getHeader(name, ",");
        if (header == null)
            return null;

        header = new String(header.getBytes(StandardCharsets.ISO_8859_1));
        Address[] addresses = InternetAddress.parseHeader(header, false);

        for (Address address : addresses) {
            InternetAddress iaddress = (InternetAddress) address;
            iaddress.setAddress(decodeMime(iaddress.getAddress()));
            String personal = iaddress.getPersonal();
            if (personal != null) {
                try {
                    iaddress.setPersonal(decodeMime(personal));
                } catch (UnsupportedEncodingException ex) {
                    Log.w(ex);
                }
            }
        }

        return addresses;
    }

    Address[] getFrom() throws MessagingException {
        Address[] address = getAddressHeader("From");
        if (address == null)
            address = getAddressHeader("Sender");
        return address;
    }

    Address[] getTo() throws MessagingException {
        return getAddressHeader("To");
    }

    Address[] getCc() throws MessagingException {
        return getAddressHeader("Cc");
    }

    Address[] getBcc() throws MessagingException {
        return getAddressHeader("Bcc");
    }

    Address[] getReply() throws MessagingException {
        return getAddressHeader("Reply-To");
    }

    Address[] getListPost() throws MessagingException {
        String list;
        try {
            // https://www.ietf.org/rfc/rfc2369.txt
            list = imessage.getHeader("List-Post", null);
            if (list == null)
                return null;

            list = MimeUtility.unfold(list);
            list = decodeMime(list);

            // List-Post: NO (posting not allowed on this list)
            if (list != null && list.startsWith("NO"))
                return null;

            // https://www.ietf.org/rfc/rfc2368.txt
            for (String entry : list.split(",")) {
                entry = entry.trim();
                int lt = entry.indexOf("<");
                int gt = entry.lastIndexOf(">");
                if (lt >= 0 && gt > lt)
                    try {
                        MailTo mailto = MailTo.parse(entry.substring(lt + 1, gt));
                        if (mailto.getTo() != null)
                            return new Address[]{new InternetAddress(mailto.getTo().split(",")[0], null)};
                    } catch (Throwable ex) {
                        Log.i(ex);
                    }
            }

            Log.w(new IllegalArgumentException("List-Post: " + list));
            return null;
        } catch (AddressException ex) {
            Log.w(ex);
            return null;
        }
    }

    String getListUnsubscribe() throws MessagingException {
        String list;
        try {
            // https://www.ietf.org/rfc/rfc2369.txt
            list = imessage.getHeader("List-Unsubscribe", null);
            if (list == null)
                return null;

            list = MimeUtility.unfold(list);
            list = decodeMime(list);

            if (list != null && list.startsWith("NO"))
                return null;

            String link = null;
            String mailto = null;
            for (String entry : list.split(",")) {
                entry = entry.trim();
                int lt = entry.indexOf("<");
                int gt = entry.lastIndexOf(">");
                if (lt >= 0 && gt > lt) {
                    String unsubscribe = entry.substring(lt + 1, gt);
                    Uri uri = Uri.parse(unsubscribe);
                    String scheme = uri.getScheme();
                    if (mailto == null && "mailto".equals(scheme))
                        mailto = unsubscribe;
                    if (link == null && ("http".equals(scheme) || "https".equals(scheme)))
                        link = unsubscribe;
                }
            }

            if (link != null)
                return link;
            if (mailto != null)
                return mailto;

            Log.w(new IllegalArgumentException("List-Unsubscribe: " + list));
            return null;
        } catch (AddressException ex) {
            Log.w(ex);
            return null;
        }
    }

    String getSubject() throws MessagingException {
        String subject = imessage.getHeader("Subject", null);
        if (subject == null)
            return null;

        subject = MimeUtility.unfold(subject);
        subject = new String(subject.getBytes(StandardCharsets.ISO_8859_1));
        subject = decodeMime(subject);

        return subject;
    }

    Long getSize() throws MessagingException {
        long size = imessage.getSize();
        if (size == 0)
            throw new MessagingException("Message empty");
        return (size < 0 ? null : size);
    }

    long getReceived() throws MessagingException {
        Date received = imessage.getReceivedDate();
        if (received == null)
            received = imessage.getSentDate();
        return (received == null ? new Date() : received).getTime();
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
        for (int i = 0; i < addresses.length; i++) {
            boolean duplicate = false;
            for (int j = 0; j < i; j++)
                if (addresses[i].equals(addresses[j])) {
                    duplicate = true;
                    break;
                }
            if (duplicate)
                continue;

            if (addresses[i] instanceof InternetAddress) {
                InternetAddress address = (InternetAddress) addresses[i];
                String personal = address.getPersonal();
                if (TextUtils.isEmpty(personal))
                    formatted.add(address.getAddress());
                else {
                    if (compose) {
                        boolean quote = false;
                        for (int c = 0; c < personal.length(); c++)
                            if ("()<>,;:\\\"[]@".indexOf(personal.charAt(c)) >= 0) {
                                quote = true;
                                break;
                            }
                        if (quote)
                            personal = "\"" + personal + "\"";
                    }

                    if (full)
                        formatted.add(personal + " <" + address.getAddress() + ">");
                    else
                        formatted.add(personal);
                }
            } else
                formatted.add(addresses[i].toString());
        }
        return TextUtils.join(", ", formatted);
    }

    static String decodeMime(String text) {
        if (text == null)
            return null;

        // https://tools.ietf.org/html/rfc2047
        // encoded-word = "=?" charset "?" encoding "?" encoded-text "?="

        int i = 0;
        boolean first = true;
        List<MimeTextPart> parts = new ArrayList<>();

        while (i < text.length()) {
            int s = text.indexOf("=?", i);
            if (s < 0)
                break;

            int q1 = text.indexOf("?", s + 2);
            if (q1 < 0)
                break;

            int q2 = text.indexOf("?", q1 + 1);
            if (q2 < 0)
                break;

            int e = text.indexOf("?=", q2 + 1);
            if (e < 0)
                break;

            String plain = text.substring(i, s);
            if (!first)
                plain = plain.replaceAll("[ \t\n\r]$", "");
            if (!TextUtils.isEmpty(plain))
                parts.add(new MimeTextPart(plain));

            parts.add(new MimeTextPart(
                    text.substring(s + 2, q1),
                    text.substring(q1 + 1, q2),
                    text.substring(q2 + 1, e)));

            i = e + 2;
            first = false;
        }

        if (i < text.length())
            parts.add(new MimeTextPart(text.substring(i)));

        // Fold words to not break encoding
        /*
        int p = 0;
        while (p + 1 < parts.size()) {
            MimeTextPart p1 = parts.get(p);
            MimeTextPart p2 = parts.get(p + 1);
            if (p1.charset != null && p1.charset.equalsIgnoreCase(p2.charset) &&
                    p1.encoding != null && p1.encoding.equalsIgnoreCase(p2.encoding)) {
                p1.text += p2.text;
                parts.remove(p + 1);
            } else
                p++;
        }
        */

        StringBuilder sb = new StringBuilder();
        for (MimeTextPart part : parts)
            sb.append(part);
        return sb.toString();
    }

    private static class MimeTextPart {
        String charset;
        String encoding;
        String text;

        MimeTextPart(String text) {
            this.text = text;
        }

        MimeTextPart(String charset, String encoding, String text) {
            this.charset = charset;
            this.encoding = encoding;
            this.text = text;
        }

        @Override
        public String toString() {
            if (charset == null)
                return text;

            String word = "=?" + charset + "?" + encoding + "?" + text + "?=";
            try {
                return decodeMime(MimeUtility.decodeWord(word));
            } catch (Throwable ex) {
                Log.w(new IllegalArgumentException(word, ex));
                return word;
            }
        }
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

        String getHtml(Context context) throws MessagingException, IOException {
            if (plain == null && html == null) {
                Log.i("No body part");
                return null;
            }

            String result;
            Part part = (html == null ? plain : html);

            try {
                Object content = part.getContent();
                Log.i("Content class=" + (content == null ? null : content.getClass().getName()));

                if (content == null) {
                    warnings.add(context.getString(R.string.title_no_body));
                    return null;
                }

                if (content instanceof String)
                    result = (String) content;
                else if (content instanceof InputStream)
                    // Typically com.sun.mail.util.QPDecoderStream
                    result = Helper.readStream((InputStream) content, StandardCharsets.UTF_8.name());
                else
                    result = content.toString();
            } catch (IOException | FolderClosedException | MessageRemovedException ex) {
                throw ex;
            } catch (Throwable ex) {
                Log.w(ex);
                warnings.add(Helper.formatThrowable(ex, false));
                return null;
            }

            try {
                ContentType ct = new ContentType(part.getContentType());
                String charset = ct.getParameter("charset");

                // Fix common mistakes
                if (charset != null) {
                    charset = charset.replace("\"", "");
                    if ("ASCII".equals(charset.toUpperCase()))
                        charset = "US-ASCII";
                }

                if (TextUtils.isEmpty(charset) || "US-ASCII".equals(charset.toUpperCase())) {
                    // The first 127 characters are the same as in US-ASCII
                    result = new String(result.getBytes(StandardCharsets.ISO_8859_1));
                } else {
                    // See UnknownCharsetProvider class
                    if ("US-ASCII".equals(Charset.forName(charset).name())) {
                        Log.w("Unsupported encoding charset=" + charset);
                        warnings.add(context.getString(R.string.title_no_charset, charset));
                        result = new String(result.getBytes(StandardCharsets.ISO_8859_1));
                    }
                }
            } catch (ParseException ex) {
                Log.w(ex);
                warnings.add(Helper.formatThrowable(ex, false));
            }

            // Prevent Jsoup throwing an exception
            result = result.replace("\0", "");

            if (part.isMimeType("text/plain")) {
                StringBuilder sb = new StringBuilder();
                sb.append("<span>");

                int level = 0;
                String[] lines = result.split("\\r?\\n");
                for (String line : lines) {
                    // Opening quotes
                    int tlevel = 0;
                    while (line.startsWith(">")) {
                        tlevel++;
                        if (tlevel > level)
                            sb.append("<blockquote>");

                        line = line.substring(1); // >

                        if (line.startsWith(" "))
                            line = line.substring(1);
                    }

                    // Closing quotes
                    for (int i = 0; i < level - tlevel; i++)
                        sb.append("</blockquote>");
                    level = tlevel;

                    // Show as-is
                    line = Html.escapeHtml(line);

                    // Non breaking spaces
                    boolean start = true;
                    int len = line.length();
                    for (int j = 0; j < len; j++) {
                        char kar = line.charAt(j);
                        if (kar == ' ' &&
                                (start || j + 1 < len && line.charAt(j + 1) == ' '))
                            sb.append("&nbsp;");
                        else {
                            start = false;
                            sb.append(kar);
                        }
                    }

                    sb.append("<br>");
                }

                // Closing quotes
                for (int i = 0; i < level; i++)
                    sb.append("</blockquote>");

                sb.append("</span>");

                result = sb.toString();
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

        void downloadAttachment(Context context, EntityAttachment local) throws IOException, MessagingException {
            List<EntityAttachment> remotes = getAttachments();

            // Some servers order attachments randomly

            int index = -1;
            boolean warning = false;

            // Get attachment by position
            if (local.sequence <= remotes.size()) {
                EntityAttachment remote = remotes.get(local.sequence - 1);
                if (Objects.equals(remote.name, local.name) &&
                        Objects.equals(remote.type, local.type) &&
                        Objects.equals(remote.disposition, local.disposition) &&
                        Objects.equals(remote.cid, local.cid) &&
                        Objects.equals(remote.size, local.size))
                    index = local.sequence - 1;
            }

            // Match attachment by name/cid
            if (index < 0 && !(local.name == null && local.cid == null)) {
                warning = true;
                Log.w("Matching attachment by name/cid");
                for (int i = 0; i < remotes.size(); i++) {
                    EntityAttachment remote = remotes.get(i);
                    if (Objects.equals(remote.name, local.name) &&
                            Objects.equals(remote.cid, local.cid)) {
                        index = i;
                        break;
                    }
                }
            }

            // Match attachment by type/size
            if (index < 0) {
                warning = true;
                Log.w("Matching attachment by type/size");
                for (int i = 0; i < remotes.size(); i++) {
                    EntityAttachment remote = remotes.get(i);
                    if (Objects.equals(remote.type, local.type) &&
                            Objects.equals(remote.size, local.size)) {
                        index = i;
                        break;
                    }
                }
            }

            if (index < 0 || warning) {
                Map<String, String> crumb = new HashMap<>();
                crumb.put("local", local.toString());
                Log.w("Attachment not found local=" + local);
                for (int i = 0; i < remotes.size(); i++) {
                    EntityAttachment remote = remotes.get(i);
                    crumb.put("remote:" + i, remote.toString());
                    Log.w("Attachment remote=" + remote);
                }
                Log.breadcrumb("attachments", crumb);
            }

            if (index < 0)
                throw new IllegalArgumentException("Attachment not found");

            downloadAttachment(context, index, local);
        }

        void downloadAttachment(Context context, int index, EntityAttachment local) throws MessagingException, IOException {
            Log.i("downloading attachment id=" + local.id + " index=" + index + " " + local);

            DB db = DB.getInstance(context);

            // Get data
            AttachmentPart apart = attachments.get(index);

            // Download attachment
            File file = EntityAttachment.getFile(context, local.id, local.name);
            db.attachment().setProgress(local.id, null);
            try (InputStream is = apart.part.getInputStream()) {
                long size = 0;
                long total = apart.part.getSize();
                int lastprogress = 0;

                try (OutputStream os = new FileOutputStream(file)) {
                    byte[] buffer = new byte[Helper.BUFFER_SIZE];
                    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                        size += len;
                        os.write(buffer, 0, len);

                        // Update progress
                        if (total > 0) {
                            int progress = (int) (size * 100 / total / 20 * 20);
                            if (progress != lastprogress) {
                                lastprogress = progress;
                                db.attachment().setProgress(local.id, progress);
                            }
                        }
                    }
                }

                // Store attachment data
                db.attachment().setDownloaded(local.id, size);

                Log.i("Downloaded attachment size=" + size);
            } catch (FolderClosedIOException ex) {
                db.attachment().setError(local.id, Helper.formatThrowable(ex));
                throw new FolderClosedException(ex.getFolder(), "downloadAttachment", ex);
            } catch (MessageRemovedIOException ex) {
                db.attachment().setError(local.id, Helper.formatThrowable(ex));
                throw new MessagingException("downloadAttachment", ex);
            } catch (Throwable ex) {
                // Reset progress on failure
                Log.e(ex);
                db.attachment().setError(local.id, Helper.formatThrowable(ex));
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

    MessageParts getMessageParts() throws IOException, MessagingException {
        MessageParts parts = new MessageParts();
        getMessageParts(imessage, parts, false);
        return parts;
    }

    private void getMessageParts(Part part, MessageParts parts, boolean pgp) throws IOException, MessagingException {
        try {
            if (BuildConfig.DEBUG)
                Log.i("Part class=" + part.getClass() + " type=" + part.getContentType());
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

                        try {
                            ContentType ct = new ContentType(cpart.getContentType());
                            if ("application/pgp-encrypted".equals(ct.getBaseType().toLowerCase(Locale.ROOT))) {
                                pgp = true;
                                continue;
                            }
                        } catch (ParseException ex) {
                            Log.w(ex);
                        }

                        getMessageParts(cpart, parts, pgp);
                    } catch (ParseException ex) {
                        // Nested body: try to continue
                        // ParseException: In parameter list boundary="...">, expected parameter name, got ";"
                        Log.w(ex);
                        parts.warnings.add(Helper.formatThrowable(ex, false));
                    }
            } else {
                // https://www.iana.org/assignments/cont-disp/cont-disp.xhtml
                String disposition;
                try {
                    disposition = part.getDisposition();
                    if (disposition != null)
                        disposition = disposition.toLowerCase(Locale.ROOT);
                } catch (MessagingException ex) {
                    Log.w(ex);
                    parts.warnings.add(Helper.formatThrowable(ex, false));
                    disposition = null;
                }

                String filename;
                try {
                    filename = part.getFileName();
                    if (filename != null)
                        filename = decodeMime(filename);
                } catch (MessagingException ex) {
                    Log.w(ex);
                    parts.warnings.add(Helper.formatThrowable(ex, false));
                    filename = null;
                }

                if (!Part.ATTACHMENT.equalsIgnoreCase(disposition) &&
                        TextUtils.isEmpty(filename) &&
                        ((parts.plain == null && part.isMimeType("text/plain")) ||
                                (parts.html == null && part.isMimeType("text/html")))) {
                    if (part.isMimeType("text/plain")) {
                        if (parts.plain == null)
                            parts.plain = part;
                    } else {
                        if (parts.html == null)
                            parts.html = part;
                    }
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
                        parts.warnings.add(Helper.formatThrowable(ex, false));
                        ct = new ContentType("application/octet-stream");
                    }

                    String[] cid = null;
                    try {
                        cid = apart.part.getHeader("Content-ID");
                    } catch (MessagingException ex) {
                        Log.w(ex);
                        if (!"Failed to fetch headers".equals(ex.getMessage()))
                            parts.warnings.add(Helper.formatThrowable(ex, false));
                    }

                    apart.attachment = new EntityAttachment();
                    apart.attachment.name = apart.filename;
                    apart.attachment.type = ct.getBaseType().toLowerCase(Locale.ROOT);
                    apart.attachment.disposition = apart.disposition;
                    apart.attachment.size = (long) apart.part.getSize();
                    apart.attachment.cid = (cid == null || cid.length == 0 ? null : MimeUtility.unfold(cid[0]));
                    apart.attachment.encryption = (apart.pgp ? EntityAttachment.PGP_MESSAGE : null);

                    if ("text/calendar".equalsIgnoreCase(apart.attachment.type) && TextUtils.isEmpty(apart.attachment.name))
                        apart.attachment.name = "invite.ics";

                    // Try to guess a better content type
                    // For example, sometimes PDF files are sent as application/octet-stream
                    if (!apart.pgp) {
                        String extension = Helper.getExtension(apart.attachment.name);
                        if (extension != null &&
                                ("pdf".equals(extension.toLowerCase(Locale.ROOT)) ||
                                        "application/octet-stream".equals(apart.attachment.type))) {
                            String type = MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
                            if (type != null) {
                                if (!type.equals(apart.attachment.type))
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
            if (retryRaw(ex))
                throw ex;
            Log.w(ex);
            parts.warnings.add(Helper.formatThrowable(ex, false));
        }
    }

    static boolean retryRaw(MessagingException ex) {
        return ("Failed to load IMAP envelope".equals(ex.getMessage()) ||
                "Unable to load BODYSTRUCTURE".equals(ex.getMessage()));
    }

    static String sanitizeKeyword(String keyword) {
        // https://tools.ietf.org/html/rfc3501
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyword.length(); i++) {
            // flag-keyword    = atom
            // atom            = 1*ATOM-CHAR
            // ATOM-CHAR       = <any CHAR except atom-specials>
            char kar = keyword.charAt(i);
            // atom-specials   = "(" / ")" / "{" / SP / CTL / list-wildcards / quoted-specials / resp-specials
            if (kar == '(' || kar == ')' || kar == '{' || kar == ' ' || Character.isISOControl(kar))
                continue;
            // list-wildcards  = "%" / "*"
            if (kar == '%' || kar == '*')
                continue;
            // quoted-specials = DQUOTE / "\"
            if (kar == '"' || kar == '\\')
                continue;
            // resp-specials   = "]"
            if (kar == ']')
                continue;
            sb.append(kar);
        }
        return sb.toString();
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
