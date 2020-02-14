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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.net.MailTo;
import android.net.Uri;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MessageRemovedIOException;

import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Pattern;

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
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;

import biweekly.Biweekly;
import biweekly.ICalendar;

public class MessageHelper {
    private MimeMessage imessage;

    static final int SMALL_MESSAGE_SIZE = 64 * 1024; // bytes
    static final int DEFAULT_ATTACHMENT_DOWNLOAD_SIZE = 256 * 1024; // bytes
    static final long ATTACHMENT_PROGRESS_UPDATE = 1500L; // milliseconds

    // https://tools.ietf.org/html/rfc4021

    static void setSystemProperties(Context context) {
        System.setProperty("mail.mime.decodetext.strict", "false");

        System.setProperty("mail.mime.ignoreunknownencoding", "true"); // Content-Transfer-Encoding
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
        System.setProperty("mail.mime.decodefilename", "true");
        System.setProperty("mail.mime.encodefilename", "true");
        System.setProperty("mail.mime.allowutf8", "false"); // InternetAddress, MimeBodyPart, MimeUtility
        System.setProperty("mail.mime.cachemultipart", "false");

        // https://docs.oracle.com/javaee/6/api/javax/mail/internet/MimeMultipart.html
        System.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true"); // default true, javax.mail.internet.ParseException: In parameter list
        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "true"); // default false
        System.setProperty("mail.mime.multipart.ignoremissingendboundary", "true"); // default true
        System.setProperty("mail.mime.multipart.allowempty", "true"); // default false
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

        // Flags
        imessage.setFlag(Flags.Flag.SEEN, message.seen);
        imessage.setFlag(Flags.Flag.FLAGGED, message.flagged);
        imessage.setFlag(Flags.Flag.ANSWERED, message.answered);

        // Priority
        if (EntityMessage.PRIORITIY_LOW.equals(message.priority)) {
            // Low
            imessage.addHeader("Importance", "Low");
            imessage.addHeader("Priority", "Non-Urgent");
            imessage.addHeader("X-Priority", "5"); // Lowest
            imessage.addHeader("X-MSMail-Priority", "Low");
        } else if (EntityMessage.PRIORITIY_HIGH.equals(message.priority)) {
            // High
            imessage.addHeader("Importance", "High");
            imessage.addHeader("Priority", "Urgent");
            imessage.addHeader("X-Priority", "1"); // Highest
            imessage.addHeader("X-MSMail-Priority", "High");
        }

        // References
        if (message.references != null)
            imessage.addHeader("References", message.references);
        if (message.inreplyto != null)
            imessage.addHeader("In-Reply-To", message.inreplyto);
        imessage.addHeader("X-Correlation-ID", message.msgid);

        // Addresses
        if (message.from != null && message.from.length > 0) {
            String email = ((InternetAddress) message.from[0]).getAddress();
            String name = ((InternetAddress) message.from[0]).getPersonal();
            if (identity != null && identity.sender_extra &&
                    identity.email.contains("@") &&
                    email != null &&
                    email.contains("@") &&
                    message.extra != null &&
                    !message.extra.equals(identity.email.split("@")[0])) {
                int at = email.indexOf('@');
                email = message.extra + email.substring(at);
                name = null;
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

        // Send message
        if (identity != null) {
            // Add reply to
            if (identity.replyto != null)
                imessage.setReplyTo(InternetAddress.parse(identity.replyto));

            // Add extra bcc
            if (identity.bcc != null) {
                List<Address> bcc = new ArrayList<>();

                Address[] existing = imessage.getRecipients(Message.RecipientType.BCC);
                if (existing != null)
                    bcc.addAll(Arrays.asList(existing));

                Address[] all = imessage.getAllRecipients();
                Address[] abccs = InternetAddress.parse(identity.bcc);
                for (Address abcc : abccs) {
                    boolean found = false;
                    if (all != null)
                        for (Address a : all)
                            if (equalEmail(a, abcc)) {
                                found = true;
                                break;
                            }
                    if (!found)
                        bcc.add(abcc);
                }

                imessage.setRecipients(Message.RecipientType.BCC, bcc.toArray(new Address[0]));
            }

            // Delivery/read request
            if (message.receipt_request != null && message.receipt_request) {
                String to = (identity.replyto == null ? identity.email : identity.replyto);

                // defacto standard
                imessage.addHeader("Return-Receipt-To", to);

                // https://tools.ietf.org/html/rfc3798
                imessage.addHeader("Disposition-Notification-To", to);
            }
        }

        // Auto answer
        if (message.unsubscribe != null)
            imessage.addHeader("List-Unsubscribe", "<" + message.unsubscribe + ">");

        MailDateFormat mdf = new MailDateFormat();
        mdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        imessage.setHeader("Date", mdf.format(new Date()));
        //imessage.setSentDate(new Date());

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);

        if (message.from != null && message.from.length > 0)
            for (EntityAttachment attachment : attachments)
                if (EntityAttachment.PGP_KEY.equals(attachment.encryption)) {
                    InternetAddress from = (InternetAddress) message.from[0];

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean autocrypt = prefs.getBoolean("autocrypt", true);
                    boolean mutual = prefs.getBoolean("autocrypt_mutual", true);

                    if (autocrypt) {
                        String mode = (mutual ? "mutual" : "nopreference");

                        StringBuilder sb = new StringBuilder();
                        File file = attachment.getFile(context);
                        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                            String line = br.readLine();
                            while (line != null) {
                                String data = null;
                                if (line.length() > 0 &&
                                        !line.startsWith("-----BEGIN ") &&
                                        !line.startsWith("-----END "))
                                    data = line;

                                line = br.readLine();

                                // https://www.w3.org/Protocols/rfc822/3_Lexical.html#z0
                                if (data != null &&
                                        line != null && !line.startsWith("-----END "))
                                    sb.append("\r\n ").append(data);
                            }
                        }

                        // https://autocrypt.org/level1.html#the-autocrypt-header
                        imessage.addHeader("Autocrypt",
                                "addr=" + from.getAddress() + ";" +
                                        " prefer-encrypt=" + mode + ";" +
                                        " keydata=" + sb.toString());
                    }
                }

        // PGP: https://tools.ietf.org/html/rfc3156
        // S/MIME: https://tools.ietf.org/html/rfc8551
        for (final EntityAttachment attachment : attachments)
            if (EntityAttachment.PGP_SIGNATURE.equals(attachment.encryption)) {
                Log.i("Sending PGP signed message");

                for (final EntityAttachment content : attachments)
                    if (EntityAttachment.PGP_CONTENT.equals(content.encryption)) {
                        BodyPart bpContent = new MimeBodyPart(new FileInputStream(content.getFile(context)));

                        final ContentType cts = new ContentType(attachment.type);
                        String micalg = cts.getParameter("micalg");
                        ParameterList params = cts.getParameterList();
                        params.remove("micalg");
                        cts.setParameterList(params);

                        // Build signature
                        BodyPart bpSignature = new MimeBodyPart();
                        bpSignature.setFileName(attachment.name);
                        FileDataSource dsSignature = new FileDataSource(attachment.getFile(context));
                        dsSignature.setFileTypeMap(new FileTypeMap() {
                            @Override
                            public String getContentType(File file) {
                                return cts.toString();
                            }

                            @Override
                            public String getContentType(String filename) {
                                return cts.toString();
                            }
                        });
                        bpSignature.setDataHandler(new DataHandler(dsSignature));
                        bpSignature.setDisposition(Part.INLINE);

                        // Build message
                        ContentType ct = new ContentType("multipart/signed");
                        ct.setParameter("micalg", micalg);
                        ct.setParameter("protocol", "application/pgp-signature");
                        String ctx = ct.toString();
                        int slash = ctx.indexOf("/");
                        Multipart multipart = new MimeMultipart(ctx.substring(slash + 1));
                        multipart.addBodyPart(bpContent);
                        multipart.addBodyPart(bpSignature);
                        imessage.setContent(multipart);

                        return imessage;
                    }
                throw new IllegalStateException("PGP content not found");
            } else if (EntityAttachment.PGP_MESSAGE.equals(attachment.encryption)) {
                Log.i("Sending PGP encrypted message");

                // Build header
                BodyPart bpHeader = new MimeBodyPart();
                bpHeader.setContent("", "application/pgp-encrypted");

                // Build content
                BodyPart bpContent = new MimeBodyPart();
                bpContent.setFileName(attachment.name);
                FileDataSource dsContent = new FileDataSource(attachment.getFile(context));
                dsContent.setFileTypeMap(new FileTypeMap() {
                    @Override
                    public String getContentType(File file) {
                        return attachment.type;
                    }

                    @Override
                    public String getContentType(String filename) {
                        return attachment.type;
                    }
                });
                bpContent.setDataHandler(new DataHandler(dsContent));
                bpContent.setDisposition(Part.INLINE);

                // Build message
                ContentType ct = new ContentType("multipart/encrypted");
                ct.setParameter("protocol", "application/pgp-encrypted");
                String ctx = ct.toString();
                int slash = ctx.indexOf("/");
                Multipart multipart = new MimeMultipart(ctx.substring(slash + 1));
                multipart.addBodyPart(bpHeader);
                multipart.addBodyPart(bpContent);
                imessage.setContent(multipart);

                return imessage;
            } else if (EntityAttachment.SMIME_SIGNATURE.equals(attachment.encryption)) {
                Log.i("Sending S/MIME signed message");

                for (final EntityAttachment content : attachments)
                    if (EntityAttachment.SMIME_CONTENT.equals(content.encryption)) {
                        BodyPart bpContent = new MimeBodyPart(new FileInputStream(content.getFile(context)));

                        final ContentType cts = new ContentType(attachment.type);
                        String micalg = cts.getParameter("micalg");
                        ParameterList params = cts.getParameterList();
                        params.remove("micalg");
                        cts.setParameterList(params);

                        // Build signature
                        BodyPart bpSignature = new MimeBodyPart();
                        bpSignature.setFileName(attachment.name);
                        FileDataSource dsSignature = new FileDataSource(attachment.getFile(context));
                        dsSignature.setFileTypeMap(new FileTypeMap() {
                            @Override
                            public String getContentType(File file) {
                                return cts.toString();
                            }

                            @Override
                            public String getContentType(String filename) {
                                return cts.toString();
                            }
                        });
                        bpSignature.setDataHandler(new DataHandler(dsSignature));
                        bpSignature.setDisposition(Part.INLINE);

                        // Build message
                        ContentType ct = new ContentType("multipart/signed");
                        ct.setParameter("micalg", micalg);
                        ct.setParameter("protocol", "application/pkcs7-signature");
                        ct.setParameter("smime-type", "signed-data");
                        String ctx = ct.toString();
                        int slash = ctx.indexOf("/");
                        Multipart multipart = new MimeMultipart(ctx.substring(slash + 1));
                        multipart.addBodyPart(bpContent);
                        multipart.addBodyPart(bpSignature);
                        imessage.setContent(multipart);

                        return imessage;
                    }
                throw new IllegalStateException("S/MIME content not found");
            } else if (EntityAttachment.SMIME_MESSAGE.equals(attachment.encryption)) {
                Log.i("Sending S/MIME encrypted message");

                // Build message
                imessage.setDisposition(Part.ATTACHMENT);
                imessage.setFileName(attachment.name);
                imessage.setDescription("S/MIME Encrypted Message");

                ContentType ct = new ContentType("application/pkcs7-mime");
                ct.setParameter("name", attachment.name);
                ct.setParameter("smime-type", "enveloped-data");

                File file = attachment.getFile(context);
                FileDataSource dataSource = new FileDataSource(file);
                dataSource.setFileTypeMap(new FileTypeMap() {
                    @Override
                    public String getContentType(File file) {
                        return ct.toString();
                    }

                    @Override
                    public String getContentType(String filename) {
                        return ct.toString();
                    }
                });

                imessage.setDataHandler(new DataHandler(dataSource));

                return imessage;
            }

        build(context, message, attachments, identity, imessage);

        return imessage;
    }

    static void build(Context context, EntityMessage message, List<EntityAttachment> attachments, EntityIdentity identity, MimeMessage imessage) throws IOException, MessagingException {
        if (message.receipt != null && message.receipt) {
            // https://www.ietf.org/rfc/rfc3798.txt
            Multipart report = new MimeMultipart("report; report-type=disposition-notification");

            String html = Helper.readText(message.getFile(context));
            String plainContent = HtmlHelper.getText(html);

            BodyPart plainPart = new MimeBodyPart();
            plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            report.addBodyPart(plainPart);

            String from = null;
            if (message.from != null && message.from.length > 0)
                from = ((InternetAddress) message.from[0]).getAddress();

            StringBuilder sb = new StringBuilder();
            sb.append("Reporting-UA: ").append(BuildConfig.APPLICATION_ID).append("; ").append(BuildConfig.VERSION_NAME).append("\r\n");
            if (from != null)
                sb.append("Original-Recipient: rfc822;").append(from).append("\r\n");
            sb.append("Disposition: manual-action/MDN-sent-manually; displayed").append("\r\n");

            BodyPart dnsPart = new MimeBodyPart();
            dnsPart.setContent(sb.toString(), "message/disposition-notification; name=\"MDNPart2.txt\"");
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
        String html = Helper.readText(message.getFile(context));
        Document document = JsoupEx.parse(html);

        // When sending message
        if (identity != null)
            document.select("div[fairemail=signature],div[fairemail=reference]").removeAttr("fairemail");

        // multipart/mixed
        //   multipart/related
        //     multipart/alternative
        //       text/plain
        //       text/html
        //     inlines
        //  attachments

        String htmlContent = document.html();
        String plainContent = HtmlHelper.getText(htmlContent);

        BodyPart plainPart = new MimeBodyPart();
        plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());

        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=" + Charset.defaultCharset().name());

        Multipart altMultiPart = new MimeMultipart("alternative");
        altMultiPart.addBodyPart(plainPart);
        altMultiPart.addBodyPart(htmlPart);

        int availableAttachments = 0;
        boolean hasInline = false;
        for (EntityAttachment attachment : attachments)
            if (attachment.available) {
                availableAttachments++;
                if (attachment.isInline())
                    hasInline = true;
            }

        if (availableAttachments == 0)
            if (message.plain_only != null && message.plain_only)
                imessage.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            else
                imessage.setContent(altMultiPart);
        else {
            Multipart mixedMultiPart = new MimeMultipart("mixed");
            Multipart relatedMultiPart = new MimeMultipart("related");

            BodyPart bodyPart;
            if (message.plain_only != null && message.plain_only)
                bodyPart = plainPart;
            else {
                bodyPart = new MimeBodyPart();
                bodyPart.setContent(altMultiPart);
            }

            if (hasInline) {
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

                    if (attachment.isInline())
                        relatedMultiPart.addBodyPart(attachmentPart);
                    else
                        mixedMultiPart.addBodyPart(attachmentPart);
                }

            imessage.setContent(mixedMultiPart);
        }
    }

    static void overrideContentTransferEncoding(Multipart mp) throws MessagingException, IOException {
        for (int i = 0; i < mp.getCount(); i++) {
            Part part = mp.getBodyPart(i);
            Object content = part.getContent();
            if (content instanceof Multipart) {
                part.setHeader("Content-Transfer-Encoding", "7bit");
                overrideContentTransferEncoding((Multipart) content);
            } else
                part.setHeader("Content-Transfer-Encoding", "base64");
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
        List<String> keywords = Arrays.asList(imessage.getFlags().getUserFlags());
        Collections.sort(keywords);
        return keywords.toArray(new String[0]);
    }

    String getMessageID() throws MessagingException {
        // Outlook outbox -> sent
        String header = imessage.getHeader("X-Correlation-ID", null);
        if (header == null)
            header = imessage.getHeader("Message-ID", null);
        return (header == null ? null : MimeUtility.unfold(header));
    }

    String[] getReferences() throws MessagingException {
        List<String> result = new ArrayList<>();
        String refs = imessage.getHeader("References", null);
        if (refs != null)
            result.addAll(Arrays.asList(MimeUtility.unfold(refs).split("\\s+")));

        try {
            // Merge references of original message for threading
            if (imessage.isMimeType("multipart/report")) {
                ContentType ct = new ContentType(imessage.getContentType());
                String reportType = ct.getParameter("report-type");
                if ("delivery-status".equalsIgnoreCase(reportType) ||
                        "disposition-notification".equalsIgnoreCase(reportType)) {
                    String arefs = null;
                    String amsgid = null;

                    MessageParts parts = new MessageParts();
                    getMessageParts(imessage, parts, null);
                    for (AttachmentPart apart : parts.attachments)
                        if ("text/rfc822-headers".equalsIgnoreCase(apart.attachment.type)) {
                            InternetHeaders iheaders = new InternetHeaders(apart.part.getInputStream());
                            arefs = iheaders.getHeader("References", null);
                            amsgid = iheaders.getHeader("Message-Id", null);
                            break;
                        } else if ("message/rfc822".equalsIgnoreCase(apart.attachment.type)) {
                            Properties props = MessageHelper.getSessionProperties();
                            Session isession = Session.getInstance(props, null);
                            MimeMessage amessage = new MimeMessage(isession, apart.part.getInputStream());
                            arefs = amessage.getHeader("References", null);
                            amsgid = amessage.getHeader("Message-Id", null);
                            break;
                        }

                    if (arefs != null)
                        for (String ref : MimeUtility.unfold(arefs).split("\\s+"))
                            if (!result.contains(ref)) {
                                Log.i("rfc822 ref=" + ref);
                                result.add(ref);
                            }

                    if (amsgid != null) {
                        String msgid = MimeUtility.unfold(amsgid);
                        if (!result.contains(msgid)) {
                            Log.i("rfc822 id=" + msgid);
                            result.add(msgid);
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return result.toArray(new String[0]);
    }

    String getDeliveredTo() throws MessagingException {
        String header = imessage.getHeader("Delivered-To", null);
        if (header == null)
            header = imessage.getHeader("X-Delivered-To", null);
        if (header == null)
            header = imessage.getHeader("Envelope-To", null);
        if (header == null)
            header = imessage.getHeader("X-Envelope-To", null);
        if (header == null)
            header = imessage.getHeader("X-Original-To", null);

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
            List<EntityMessage> messages = db.message().getMessagesByMsgId(account, ref);
            if (messages.size() > 0)
                return messages.get(0).thread;
        }

        if (refs.size() > 0)
            return refs.get(0);

        String msgid = getMessageID();
        return (TextUtils.isEmpty(msgid) ? Long.toString(uid) : msgid);
    }

    Integer getPriority() throws MessagingException {
        Integer priority = null;

        // https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxcmail/2bb19f1b-b35e-4966-b1cb-1afd044e83ab
        String header = imessage.getHeader("Importance", null);
        if (header == null)
            header = imessage.getHeader("Priority", null);
        if (header == null)
            header = imessage.getHeader("X-Priority", null);
        if (header == null)
            header = imessage.getHeader("X-MSMail-Priority", null);

        if (header != null) {
            header = decodeMime(header);

            int sp = header.indexOf(" ");
            if (sp >= 0)
                header = header.substring(0, sp); // "2 (High)"

            header = header.replaceAll("[^A-Za-z0-9\\-]", "");
        }

        if ("high".equalsIgnoreCase(header) ||
                "highest".equalsIgnoreCase(header) ||
                "u".equalsIgnoreCase(header) || // Urgent?
                "urgent".equalsIgnoreCase(header) ||
                "critical".equalsIgnoreCase(header) ||
                "yes".equalsIgnoreCase(header))
            priority = EntityMessage.PRIORITIY_HIGH;
        else if ("normal".equalsIgnoreCase(header) ||
                "medium".equalsIgnoreCase(header) ||
                "med".equalsIgnoreCase(header))
            priority = EntityMessage.PRIORITIY_NORMAL;
        else if ("low".equalsIgnoreCase(header) ||
                "non-urgent".equalsIgnoreCase(header) ||
                "marketing".equalsIgnoreCase(header) ||
                "bulk".equalsIgnoreCase(header) ||
                "batch".equalsIgnoreCase(header) ||
                "b".equalsIgnoreCase(header) ||
                "mass".equalsIgnoreCase(header))
            priority = EntityMessage.PRIORITIY_LOW;
        else if (!TextUtils.isEmpty(header))
            try {
                priority = Integer.parseInt(header);
                if (priority < 3)
                    priority = EntityMessage.PRIORITIY_HIGH;
                else if (priority > 3)
                    priority = EntityMessage.PRIORITIY_LOW;
                else
                    priority = EntityMessage.PRIORITIY_NORMAL;
            } catch (NumberFormatException ex) {
                Log.e("priority=" + header);
            }

        if (EntityMessage.PRIORITIY_NORMAL.equals(priority))
            priority = null;

        return priority;
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

    String getReceivedFromHost() throws MessagingException {
        String[] received = imessage.getHeader("Received");
        if (received == null || received.length == 0)
            return null;

        String origin = MimeUtility.unfold(received[received.length - 1]);

        String[] h = origin.split("\\s+");
        if (h.length > 1 && h[0].equalsIgnoreCase("from")) {
            String host = h[1];
            if (host.startsWith("["))
                host = host.substring(1);
            if (host.endsWith("]"))
                host = host.substring(0, host.length() - 1);
            if (!TextUtils.isEmpty(host))
                return host;
        }

        return null;
    }

    private Address[] getAddressHeader(String name) throws MessagingException {
        String header = imessage.getHeader(name, ",");
        if (header == null)
            return null;

        if (!Helper.isUTF8(header)) {
            Log.w("Converting header '" + name + "' to ISO8859-1");
            header = new String(header.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
        }
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

    Address[] getSender() throws MessagingException {
        return getAddressHeader("Sender");
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

            Log.i(new IllegalArgumentException("List-Post: " + list));
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

            Log.i(new IllegalArgumentException("List-Unsubscribe: " + list));
            return null;
        } catch (AddressException ex) {
            Log.w(ex);
            return null;
        }
    }

    String getAutocrypt() throws MessagingException {
        String autocrypt = imessage.getHeader("Autocrypt", null);
        if (autocrypt == null)
            return null;

        return MimeUtility.unfold(autocrypt);
    }

    String getSubject() throws MessagingException {
        String subject = imessage.getHeader("Subject", null);
        if (subject == null)
            return null;

        if (!Helper.isUTF8(subject)) {
            Log.w("Converting subject to ISO8859-1");
            subject = new String(subject.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
        }
        subject = subject.replaceAll("\\?=\\r?\\n\\s+=\\?", "\\?==\\?");
        subject = MimeUtility.unfold(subject);
        subject = decodeMime(subject);

        return subject
                .trim()
                .replace("\n", "")
                .replace("\r", "");
    }

    Long getSize() throws MessagingException {
        long size = imessage.getSize();
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
                        personal = personal.replace("\"", "");
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

        Long getBodySize() throws MessagingException {
            Part part = (html == null ? plain : html);
            if (part == null)
                return null;
            int size = part.getSize();
            if (size < 0)
                return null;
            else
                return (long) size;
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
                warnings.add(Log.formatThrowable(ex, false));
                return null;
            }

            try {
                ContentType ct = new ContentType(part.getContentType());
                String charset = ct.getParameter("charset");
                if (UnknownCharsetProvider.charsetForMime(charset) == null)
                    warnings.add(context.getString(R.string.title_no_charset, charset));
            } catch (ParseException ex) {
                Log.e(ex);
            }

            if (part == plain)
                result = "<div>" + HtmlHelper.formatPre(result) + "</div>";

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

        Integer getEncryption() {
            for (AttachmentPart apart : attachments)
                if (EntityAttachment.PGP_SIGNATURE.equals(apart.attachment.encryption))
                    return EntityMessage.PGP_SIGNONLY;
                else if (EntityAttachment.PGP_MESSAGE.equals(apart.attachment.encryption))
                    return EntityMessage.PGP_SIGNENCRYPT;
                else if (EntityAttachment.SMIME_SIGNATURE.equals(apart.attachment.encryption) ||
                        EntityAttachment.SMIME_SIGNED_DATA.equals(apart.attachment.encryption))
                    return EntityMessage.SMIME_SIGNONLY;
                else if (EntityAttachment.SMIME_MESSAGE.equals(apart.attachment.encryption))
                    return EntityMessage.SMIME_SIGNENCRYPT;
            return null;
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
            db.attachment().setProgress(local.id, 0);

            if (EntityAttachment.PGP_CONTENT.equals(apart.encrypt) ||
                    EntityAttachment.SMIME_CONTENT.equals(apart.encrypt)) {
                ContentType ct = new ContentType(apart.part.getContentType());
                String boundary = ct.getParameter("boundary");
                if (TextUtils.isEmpty(boundary))
                    throw new ParseException("Signed boundary missing");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                apart.part.writeTo(bos);
                String raw = new String(bos.toByteArray());
                String[] parts = raw.split("\\r?\\n" + Pattern.quote("--" + boundary) + "\\r?\\n");
                if (parts.length < 2)
                    throw new ParseException("Signed part missing");

                String c = parts[1]
                        .replaceAll(" +$", "") // trim trailing spaces
                        .replace("\\r?\\n", "\\r\\n"); // normalize new lines
                try (OutputStream os = new FileOutputStream(file)) {
                    os.write(c.getBytes());
                }

                db.attachment().setDownloaded(local.id, file.length());
            } else
                try (InputStream is = apart.part.getInputStream()) {
                    long size = 0;
                    long total = apart.part.getSize();
                    long lastprogress = System.currentTimeMillis();

                    try (OutputStream os = new FileOutputStream(file)) {
                        byte[] buffer = new byte[Helper.BUFFER_SIZE];
                        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                            size += len;
                            os.write(buffer, 0, len);

                            // Update progress
                            if (total > 0) {
                                long now = System.currentTimeMillis();
                                if (now - lastprogress > ATTACHMENT_PROGRESS_UPDATE) {
                                    lastprogress = now;
                                    db.attachment().setProgress(local.id, (int) (size * 100 / total));
                                }
                            }
                        }
                    }

                    // Store attachment data
                    db.attachment().setDownloaded(local.id, size);

                    Log.i("Downloaded attachment size=" + size);
                } catch (FolderClosedIOException ex) {
                    db.attachment().setError(local.id, Log.formatThrowable(ex));
                    throw new FolderClosedException(ex.getFolder(), "downloadAttachment", ex);
                } catch (MessageRemovedIOException ex) {
                    db.attachment().setError(local.id, Log.formatThrowable(ex));
                    throw new MessagingException("downloadAttachment", ex);
                } catch (Throwable ex) {
                    // Reset progress on failure
                    if (ex instanceof IOException)
                        Log.i(ex);
                    else
                        Log.e(ex);
                    db.attachment().setError(local.id, Log.formatThrowable(ex));
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
        Integer encrypt;
        Part part;
        EntityAttachment attachment;
    }

    MessageParts getMessageParts(Context context) throws IOException, MessagingException {
        MessageParts parts = new MessageParts();

        try {
            imessage.getContentType();
        } catch (MessagingException ex) {
            // https://javaee.github.io/javamail/FAQ#imapserverbug
            if ("Failed to load IMAP envelope".equals(ex.getMessage()) ||
                    "Unable to load BODYSTRUCTURE".equals(ex.getMessage())) {
                Log.w("Fetching raw message");
                File file = File.createTempFile("serverbug", null, context.getCacheDir());
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                    imessage.writeTo(os);
                }

                Properties props = MessageHelper.getSessionProperties();
                Session isession = Session.getInstance(props, null);

                Log.w("Decoding raw message");
                try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                    imessage = new MimeMessageEx(isession, is, imessage);
                }

                file.delete();
            } else
                throw ex;
        }

        try {
            if (imessage.isMimeType("multipart/signed")) {
                ContentType ct = new ContentType(imessage.getContentType());
                String protocol = ct.getParameter("protocol");
                if ("application/pgp-signature".equals(protocol) ||
                        "application/pkcs7-signature".equals(protocol) ||
                        "application/x-pkcs7-signature".equals(protocol)) {
                    Multipart multipart = (Multipart) imessage.getContent();
                    if (multipart.getCount() == 2) {
                        getMessageParts(multipart.getBodyPart(0), parts, null);
                        getMessageParts(multipart.getBodyPart(1), parts,
                                "application/pgp-signature".equals(protocol)
                                        ? EntityAttachment.PGP_SIGNATURE
                                        : EntityAttachment.SMIME_SIGNATURE);

                        AttachmentPart apart = new AttachmentPart();
                        apart.disposition = Part.INLINE;
                        apart.filename = "content.asc";
                        apart.encrypt = "application/pgp-signature".equals(protocol)
                                ? EntityAttachment.PGP_CONTENT
                                : EntityAttachment.SMIME_CONTENT;
                        apart.part = imessage;

                        apart.attachment = new EntityAttachment();
                        apart.attachment.disposition = apart.disposition;
                        apart.attachment.name = apart.filename;
                        apart.attachment.type = "text/plain";
                        apart.attachment.size = getSize();
                        apart.attachment.encryption = apart.encrypt;

                        parts.attachments.add(apart);

                        return parts;
                    }
                }
            } else if (imessage.isMimeType("multipart/encrypted")) {
                ContentType ct = new ContentType(imessage.getContentType());
                String protocol = ct.getParameter("protocol");
                if ("application/pgp-encrypted".equals(protocol)) {
                    Multipart multipart = (Multipart) imessage.getContent();
                    if (multipart.getCount() == 2) {
                        // Ignore header
                        getMessageParts(multipart.getBodyPart(1), parts, EntityAttachment.PGP_MESSAGE);
                        return parts;
                    }
                }
            } else if (imessage.isMimeType("application/pkcs7-mime") ||
                    imessage.isMimeType("application/x-pkcs7-mime")) {
                ContentType ct = new ContentType(imessage.getContentType());
                String smimeType = ct.getParameter("smime-type");
                if ("enveloped-data".equals(smimeType)) {
                    getMessageParts(imessage, parts, EntityAttachment.SMIME_MESSAGE);
                    return parts;
                } else if ("signed-data".equals(smimeType)) {
                    getMessageParts(imessage, parts, EntityAttachment.SMIME_SIGNED_DATA);
                    return parts;
                }
            }
        } catch (ParseException ex) {
            Log.w(ex);
        }

        getMessageParts(imessage, parts, null);
        return parts;
    }

    private void getMessageParts(Part part, MessageParts parts, Integer encrypt) throws IOException, MessagingException {
        try {
            Log.d("Part class=" + part.getClass() + " type=" + part.getContentType());
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
                        getMessageParts(multipart.getBodyPart(i), parts, encrypt);
                    } catch (ParseException ex) {
                        // Nested body: try to continue
                        // ParseException: In parameter list boundary="...">, expected parameter name, got ";"
                        Log.w(ex);
                        parts.warnings.add(Log.formatThrowable(ex, false));
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
                    parts.warnings.add(Log.formatThrowable(ex, false));
                    disposition = null;
                }

                String filename;
                try {
                    filename = part.getFileName();
                    if (filename != null)
                        filename = decodeMime(filename);
                } catch (MessagingException ex) {
                    Log.w(ex);
                    parts.warnings.add(Log.formatThrowable(ex, false));
                    filename = null;
                }

                ContentType contentType;
                try {
                    String c = part.getContentType();
                    contentType = new ContentType(c == null ? "" : c);
                } catch (ParseException ex) {
                    Log.w(ex);
                    parts.warnings.add(Log.formatThrowable(ex, false));

                    if (part instanceof MimeMessage)
                        contentType = new ContentType("text/html");
                    else
                        contentType = new ContentType(Helper.guessMimeType(filename));
                }

                if (!Part.ATTACHMENT.equalsIgnoreCase(disposition) &&
                        TextUtils.isEmpty(filename) &&
                        ((parts.plain == null && "text/plain".equalsIgnoreCase(contentType.getBaseType())) ||
                                (parts.html == null && "text/html".equalsIgnoreCase(contentType.getBaseType())))) {
                    if ("text/html".equalsIgnoreCase(contentType.getBaseType()))
                        parts.html = part;
                    else
                        parts.plain = part;
                } else {
                    AttachmentPart apart = new AttachmentPart();
                    apart.disposition = disposition;
                    apart.filename = filename;
                    apart.encrypt = encrypt;
                    apart.part = part;

                    String[] cid = null;
                    try {
                        cid = apart.part.getHeader("Content-ID");
                    } catch (MessagingException ex) {
                        Log.w(ex);
                        if (!"Failed to fetch headers".equals(ex.getMessage()))
                            parts.warnings.add(Log.formatThrowable(ex, false));
                    }

                    apart.attachment = new EntityAttachment();
                    apart.attachment.disposition = apart.disposition;
                    apart.attachment.name = apart.filename;
                    apart.attachment.type = contentType.getBaseType().toLowerCase(Locale.ROOT);
                    apart.attachment.size = (long) apart.part.getSize();
                    apart.attachment.cid = (cid == null || cid.length == 0 ? null : MimeUtility.unfold(cid[0]));
                    apart.attachment.encryption = apart.encrypt;

                    if ("text/calendar".equalsIgnoreCase(apart.attachment.type) &&
                            TextUtils.isEmpty(apart.attachment.name))
                        apart.attachment.name = "invite.ics";

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
            parts.warnings.add(Log.formatThrowable(ex, false));
        }
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

    static String sanitizeEmail(String email) {
        if (email.contains("<") && email.contains(">"))
            try {
                InternetAddress address = new InternetAddress(email);
                return address.getAddress();
            } catch (AddressException ignored) {
            }

        return email;
    }

    static boolean equalEmail(Address a1, Address a2) {
        String email1 = ((InternetAddress) a1).getAddress();
        String email2 = ((InternetAddress) a2).getAddress();
        if (email1 != null)
            email1 = email1.toLowerCase();
        if (email2 != null)
            email2 = email2.toLowerCase();
        return Objects.equals(email1, email2);
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
