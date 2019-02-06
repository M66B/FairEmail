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
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

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
import java.nio.charset.Charset;
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

public class MessageHelper {
    private MimeMessage imessage;

    private final static int NETWORK_TIMEOUT = 20 * 1000; // milliseconds
    private final static int FETCH_SIZE = 1024 * 1024; // bytes, default 16K
    private final static int POOL_TIMEOUT = 45 * 1000; // milliseconds, default 45 sec

    static final int ATTACHMENT_BUFFER_SIZE = 8192; // bytes

    static void setSystemProperties() {
        System.setProperty("mail.mime.decodetext.strict", "false");

        System.setProperty("mail.mime.ignoreunknownencoding", "true"); // Content-Transfer-Encoding
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
        System.setProperty("mail.mime.decodefilename", "true");
        System.setProperty("mail.mime.encodefilename", "true");
        System.setProperty("mail.mime.allowutf8", "true"); // InternetAddress, MimeBodyPart, MimeUtility

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
        props.put("mail.imaps.connectiontimeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.imaps.timeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.imaps.writetimeout", Integer.toString(NETWORK_TIMEOUT)); // one thread overhead

        props.put("mail.imaps.connectionpool.debug", "true");
        props.put("mail.imaps.connectionpooltimeout", Integer.toString(POOL_TIMEOUT));

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

        props.put("mail.imap.connectiontimeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.imap.timeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.imap.writetimeout", Integer.toString(NETWORK_TIMEOUT)); // one thread overhead

        props.put("mail.imap.connectionpool.debug", "true");
        props.put("mail.imap.connectionpooltimeout", Integer.toString(POOL_TIMEOUT));

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

        props.put("mail.smtps.connectiontimeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.smtps.writetimeout", Integer.toString(NETWORK_TIMEOUT)); // one thread overhead
        props.put("mail.smtps.timeout", Integer.toString(NETWORK_TIMEOUT));

        props.put("mail.smtp.ssl.checkserveridentity", checkserveridentity);
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        props.put("mail.smtp.auth", "true");
        if (realm != null)
            props.put("mail.smtp.auth.ntlm.domain", realm);

        props.put("mail.smtp.connectiontimeout", Integer.toString(NETWORK_TIMEOUT));
        props.put("mail.smtp.writetimeout", Integer.toString(NETWORK_TIMEOUT)); // one thread overhead
        props.put("mail.smtp.timeout", Integer.toString(NETWORK_TIMEOUT));

        props.put("mail.mime.allowutf8", "true"); // SMTPTransport, MimeMessage
        props.put("mail.mime.address.strict", "false");

        if (false) {
            Log.i("Prefering IPv4");
            System.setProperty("java.net.preferIPv4Stack", "true");
        }

        // https://javaee.github.io/javamail/OAuth2
        Log.i("Auth type=" + auth_type);
        if (auth_type == Helper.AUTH_TYPE_GMAIL) {
            props.put("mail.imaps.auth.mechanisms", "XOAUTH2");
            props.put("mail.imap.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtps.auth.mechanisms", "XOAUTH2");
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
        }

        return props;
    }

    static MimeMessageEx from(Context context, EntityMessage message, Session isession) throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        MimeMessageEx imessage = new MimeMessageEx(isession, message.msgid);

        if (message.references != null)
            imessage.addHeader("References", message.references);
        if (message.inreplyto != null)
            imessage.addHeader("In-Reply-To", message.inreplyto);

        imessage.addHeader("X-FairEmail-ID", message.msgid);

        imessage.setFlag(Flags.Flag.SEEN, message.seen);
        imessage.setFlag(Flags.Flag.FLAGGED, message.flagged);
        imessage.setFlag(Flags.Flag.ANSWERED, message.answered);

        if (message.from != null && message.from.length > 0) {
            String email = ((InternetAddress) message.from[0]).getAddress();
            String name = ((InternetAddress) message.from[0]).getPersonal();
            if (email != null && !TextUtils.isEmpty(message.extra)) {
                int at = email.indexOf('@');
                email = email.substring(0, at) + message.extra + email.substring(at);
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
                    File file = EntityAttachment.getFile(context, attachment.id);
                    BufferedReader br = null;
                    StringBuilder sb = new StringBuilder();
                    try {
                        br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null)
                            if (!line.startsWith("-----") && !line.endsWith("-----"))
                                sb.append(line);
                    } finally {
                        if (br != null)
                            br.close();
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
                bpAttachment.setDisposition(Part.INLINE);

                multipart.addBodyPart(bpAttachment);

                imessage.setContent(multipart);

                return imessage;
            }

        build(context, message, imessage);

        return imessage;
    }

    static void build(Context context, EntityMessage message, MimeMessage imessage) throws IOException, MessagingException {
        DB db = DB.getInstance(context);

        StringBuilder body = new StringBuilder();
        body.append(Helper.readText(EntityMessage.getFile(context, message.id)));

        if (message.identity != null) {
            EntityIdentity identity = db.identity().getIdentity(message.identity);
            if (!TextUtils.isEmpty(identity.signature))
                body.append(identity.signature);
        }

        File refFile = EntityMessage.getRefFile(context, message.id);
        if (refFile.exists())
            body.append(Helper.readText(refFile));

        String plainContent = HtmlHelper.getText(body.toString());

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>").append("\n");
        htmlContent.append("<html>").append("\n");
        htmlContent.append("<head>").append("\n");
        htmlContent.append("<meta charset=\"utf-8\" /> ").append("\n");
        htmlContent.append("</head>").append("\n");
        htmlContent.append("<body>").append("\n");
        htmlContent.append(body.toString()).append("\n");
        htmlContent.append("</body>").append("\n");
        htmlContent.append("</html>").append("\n");

        BodyPart plainPart = new MimeBodyPart();
        plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());

        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent.toString(), "text/html; charset=" + Charset.defaultCharset().name());

        Multipart alternativePart = new MimeMultipart("alternative");
        alternativePart.addBodyPart(plainPart);
        alternativePart.addBodyPart(htmlPart);

        int available = 0;
        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
        for (EntityAttachment attachment : attachments)
            if (attachment.available)
                available++;
        Log.i("Attachments available=" + available);

        if (available == 0)
            imessage.setContent(alternativePart);
        else {
            Multipart mixedPart = new MimeMultipart("mixed");

            BodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setContent(alternativePart);
            mixedPart.addBodyPart(attachmentPart);

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
        String[] xID = imessage.getHeader("X-FairEmail-ID");
        if (xID != null && xID.length > 0)
            return xID[0];

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

        String inreplyto = getInReplyTo();
        if (inreplyto != null)
            return inreplyto;

        String msgid = getMessageID();
        return (TextUtils.isEmpty(msgid) ? Long.toString(uid) : msgid);
    }

    Address getSender() throws MessagingException {
        String sender = imessage.getHeader("Sender", null);
        if (sender == null)
            return null;

        InternetAddress[] address = null;
        try {
            address = InternetAddress.parse(sender);
        } catch (AddressException ex) {
            Log.w(ex);
        }

        if (address == null || address.length == 0)
            return null;

        return address[0];
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

    String getSubject() throws MessagingException, UnsupportedEncodingException {
        String subject = imessage.getSubject();
        if (subject == null)
            return subject;

        int i = 0;
        int s = subject.indexOf("=?", i);
        int e = subject.indexOf("?=", i);
        while (s >= 0 && e >= 0 && i < subject.length()) {
            String decode = subject.substring(s, e + 2);
            String decoded = MimeUtility.decodeText(decode);
            subject = subject.substring(0, i) + decoded + subject.substring(e + 2);
            i += decoded.length();
            s = subject.indexOf("=?", i);
            e = subject.indexOf("?=", i);
        }

        return subject;
    }

    Integer getSize() throws MessagingException {
        int size = imessage.getSize();
        return (size < 0 ? null : size);
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
                    formatted.add(address.toString());
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
            } catch (Throwable ex) {
                Log.w(ex);
                text = true;
                result = ex + "\n" + android.util.Log.getStackTraceString(ex);
            }

            ContentType ct = new ContentType(part.getContentType());
            String charset = ct.getParameter("charset");
            if (TextUtils.isEmpty(charset)) {
                if (BuildConfig.DEBUG)
                    warnings.add(context.getString(R.string.title_no_charset, ct.toString()));
                if (part.isMimeType("text/plain"))
                    try {
                        // The first 127 characters are the same as in US-ASCII
                        result = new String(result.getBytes("ISO-8859-1"));
                    } catch (UnsupportedEncodingException ex) {
                        warnings.add(Helper.formatThrowable(ex));
                    }
            } else {
                if ("US-ASCII".equals(Charset.forName(charset).name()) &&
                        !"US-ASCII".equals(charset.toUpperCase()))
                    warnings.add(context.getString(R.string.title_no_charset, charset));
            }

            if (part.isMimeType("text/plain") || text)
                result = "<pre>" + result.replaceAll("\\r?\\n", "<br />") + "</pre>";

            return result;
        }

        List<AttachmentPart> getAttachmentParts() {
            return attachments;
        }

        List<EntityAttachment> getAttachments() throws MessagingException {
            List<EntityAttachment> result = new ArrayList<>();

            for (AttachmentPart apart : attachments) {
                ContentType ct = new ContentType(apart.part.getContentType());
                String[] cid = apart.part.getHeader("Content-ID");

                EntityAttachment attachment = new EntityAttachment();
                attachment.name = apart.filename;
                attachment.type = ct.getBaseType().toLowerCase();
                attachment.disposition = apart.disposition;
                attachment.size = (long) apart.part.getSize();
                attachment.cid = (cid == null || cid.length == 0 ? null : cid[0]);
                attachment.encryption = (apart.pgp ? EntityAttachment.PGP_MESSAGE : null);

                if ("text/calendar".equalsIgnoreCase(attachment.type) && TextUtils.isEmpty(attachment.name))
                    attachment.name = "invite.ics";

                // Try to guess a better content type
                // Sometimes PDF files are sent using the wrong type
                if ("application/octet-stream".equalsIgnoreCase(attachment.type)) {
                    String extension = Helper.getExtension(attachment.name);
                    if (extension != null) {
                        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                        if (type != null) {
                            Log.w("Guessing file=" + attachment.name + " type=" + type);
                            attachment.type = type;
                        }
                    }
                }

                if (attachment.size < 0)
                    attachment.size = null;

                result.add(attachment);
            }

            // Fix duplicate CIDs
            for (int i = 0; i < result.size(); i++) {
                String cid = result.get(i).cid;
                if (cid != null)
                    for (int j = i + 1; j < result.size(); j++) {
                        EntityAttachment a = result.get(j);
                        if (cid.equals(a.cid))
                            a.cid = null;
                    }
            }

            return result;
        }

        boolean downloadAttachment(Context context, DB db, long id, int sequence) throws IOException {
            // Attachments of drafts might not have been uploaded yet
            if (sequence > attachments.size()) {
                Log.w("Attachment unavailable sequence=" + sequence + " size=" + attachments.size());
                return false;
            }

            // Get data
            AttachmentPart apart = attachments.get(sequence - 1);
            File file = EntityAttachment.getFile(context, id);

            // Download attachment
            OutputStream os = null;
            try {
                db.attachment().setProgress(id, null);

                InputStream is = apart.part.getInputStream();
                os = new BufferedOutputStream(new FileOutputStream(file));

                long size = 0;
                long total = apart.part.getSize();
                byte[] buffer = new byte[ATTACHMENT_BUFFER_SIZE];
                for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                    size += len;
                    os.write(buffer, 0, len);

                    // Update progress
                    if (total > 0)
                        db.attachment().setProgress(id, (int) (size * 100 / total));
                }

                // Store attachment data
                db.attachment().setDownloaded(id, size);

                Log.i("Downloaded attachment size=" + size);
                return true;
            } catch (Throwable ex) {
                Log.w(ex);
                // Reset progress on failure
                db.attachment().setError(id, Helper.formatThrowable(ex));
                return false;
            } finally {
                if (os != null)
                    os.close();
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
    }

    MessageParts getMessageParts() throws IOException {
        MessageParts parts = new MessageParts();
        getMessageParts(imessage, parts, false); // Can throw ParseException
        return parts;
    }

    private void getMessageParts(Part part, MessageParts parts, boolean pgp) throws IOException {
        try {
            if (part.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) part.getContent();
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
                    }
            } else {
                // https://www.iana.org/assignments/cont-disp/cont-disp.xhtml
                String disposition;
                try {
                    disposition = part.getDisposition();
                } catch (MessagingException ex) {
                    Log.w(ex);
                    disposition = null;
                }

                String filename;
                try {
                    filename = part.getFileName();
                } catch (MessagingException ex) {
                    Log.w(ex);
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
                    parts.attachments.add(apart);
                }
            }
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
