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

import static android.system.OsConstants.ENOSPC;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.system.ErrnoException;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.net.MailTo;
import androidx.core.util.PatternsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPBodyPart;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.Utility;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.DecodingException;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.MessageRemovedIOException;
import com.sun.mail.util.ReadableMime;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.interfaces.EdDSAPublicKey;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.simplejavamail.outlookmessageparser.OutlookMessageParser;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.IDN;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.Normalizer;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
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
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Method;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardWriter;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Email;

public class MessageHelper {
    private boolean ensuredEnvelope = false;
    private boolean ensuredHeaders = false;
    private boolean ensuredStructure = false;
    private MimeMessage imessage;
    private String hash = null;
    private String threadId = null;
    private InternetHeaders reportHeaders = null;

    static final int SMALL_MESSAGE_SIZE = 192 * 1024; // bytes
    static final int DEFAULT_DOWNLOAD_SIZE = 4 * 1024 * 1024; // bytes
    static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    static final String HEADER_MICROSOFT_ORIGINAL_MESSAGE_ID = "X-Microsoft-Original-Message-ID";
    static final String HEADER_GOOGLE_ORIGINAL_MESSAGE_ID = "X-Google-Original-Message-ID";
    static final String HEADER_MODIFIED_TIME = "X-Modified-Time";
    static final int MAX_SUBJECT_AGE = 48; // hours
    static final int DEFAULT_THREAD_RANGE = 7; // 2^7 = 128 days
    static final int MAX_UNZIP_COUNT = 20;
    static final long MAX_UNZIP_SIZE = 10 * 1024 * 1024L;
    static final String ONE_CLICK_UNSUBSCRIBE = "oneclick:";

    static final List<String> UNZIP_FORMATS = Collections.unmodifiableList(Arrays.asList(
            "zip", "gz", "tar.gz"
    ));

    static final List<String> RECEIVED_WORDS = Collections.unmodifiableList(Arrays.asList(
            "from", "by", "via", "with", "id", "for"
    ));

    private static final int MAX_HEADER_LENGTH = 998;
    private static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024; // bytes
    private static final int MAX_KEYWORDS = 32;
    private static final int MAX_LABELS = 32;
    private static final long ATTACHMENT_PROGRESS_UPDATE = 1500L; // milliseconds
    private static final int MAX_META_EXCERPT = 1024; // characters
    private static final int FORMAT_FLOWED_LINE_LENGTH = 72; // characters
    private static final int MAX_DIAGNOSTIC = 250; // characters
    private static final int DKIM_MIN_KEY_LENGTH = 1024; //  bits

    private static final String DKIM_SIGNATURE = "DKIM-Signature";
    private static final String GOOGLE_DKIM_SIGNATURE = "X-Google-DKIM-Signature";
    private static final String ARC_SEAL = "ARC-Seal";
    private static final String AUTHENTICATION_RESULTS = "Authentication-Results";
    private static final String ARC_AUTHENTICATION_RESULTS = "ARC-Authentication-Results";
    private static final String ARC_MESSAGE_SIGNATURE = "ARC-Message-Signature";

    static final List<String> ARC_WHITELIST_DEFAULT = Collections.unmodifiableList(Arrays.asList(
            "google.com", "microsoft.com", "amazonses.com"
    ));

    private static final String DOCTYPE = "<!DOCTYPE";
    private static final String HTML_START = "<html>";
    private static final String HTML_END = "</html>";
    private static final String SMTP_MAILFORM = "smtp.mailfrom";

    private static final List<Charset> CHARSET16 = Collections.unmodifiableList(Arrays.asList(
            StandardCharsets.UTF_16,
            StandardCharsets.UTF_16BE,
            StandardCharsets.UTF_16LE
    ));

    private static final List<String> DO_NOT_REPLY = Collections.unmodifiableList(Arrays.asList(
            "noreply",
            "no.reply",
            "no-reply",
            "donotreply",
            "do.not.reply",
            "do-not-reply",
            "nicht.antworten",
            "nepasrepondre",
            "ne-pas-repondre"
    ));

    static final String FLAG_FORWARDED = "$Forwarded";
    static final String FLAG_JUNK = "$Junk";
    static final String FLAG_NOT_JUNK = "$NotJunk";
    static final String FLAG_CLASSIFIED = "$Classified";
    static final String FLAG_FILTERED = "$Filtered";
    static final String FLAG_DELIVERED = "$Delivered";
    static final String FLAG_NOT_DELIVERED = "$NotDelivered";
    static final String FLAG_DISPLAYED = "$Displayed";
    static final String FLAG_NOT_DISPLAYED = "$NotDisplayed";
    static final String FLAG_COMPLAINT = "Complaint";
    static final String FLAG_LOW_IMPORTANCE = "$LowImportance";
    static final String FLAG_HIGH_IMPORTANCE = "$HighImportance";
    static final String FLAG_PHISHING = "$Phishing"; // Gmail
    static final String CATEGORY_PREFIX = "$category:";

    // https://www.iana.org/assignments/imap-jmap-keywords/imap-jmap-keywords.xhtml
    // Not black listed: Gmail $Phishing
    private static final List<String> FLAG_BLACKLIST = Collections.unmodifiableList(Arrays.asList(
            FLAG_FORWARDED,
            FLAG_JUNK,
            FLAG_NOT_JUNK,
            FLAG_CLASSIFIED, // FairEmail
            FLAG_FILTERED, // FairEmail
            FLAG_LOW_IMPORTANCE, // FairEmail
            FLAG_HIGH_IMPORTANCE, // FairEmail
            "Sent",
            "$MDNSent", // https://tools.ietf.org/html/rfc3503
            "$SubmitPending",
            "$Submitted",
            "Junk",
            "NonJunk",
            "$recent",
            "DTAG_document",
            "DTAG_image",
            "$X-Me-Annot-1",
            "$X-Me-Annot-2",
            "\\Unseen", // Mail.ru
            "$sent", // Kmail
            "$attachment", // Kmail
            "$signed", // Kmail
            "$encrypted", // Kmail
            "$HasAttachment", // Dovecot
            "$HasNoAttachment", // Dovecot
            "$IsTrusted", // Fastmail
            "$X-ME-Annot-2", // Fastmail
            "$purchases", // mailbox.org
            "$social" // mailbox.org
    ));

    // https://tools.ietf.org/html/rfc4021

    static void setSystemProperties(Context context) {
        System.setProperty("mail.mime.decodetext.strict", "false");

        // https://docs.oracle.com/javaee/6/api/javax/mail/internet/package-summary.html
        System.setProperty("mail.mime.ignoreunknownencoding", "true"); // Content-Transfer-Encoding
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
        System.setProperty("mail.mime.decodefilename", "true");
        System.setProperty("mail.mime.encodefilename", "false");
        System.setProperty("mail.mime.decodeparameters", "true");
        System.setProperty("mail.mime.encodeparameters", "true");
        //System.setProperty("mail.mime.parameters.strict", "false");
        System.setProperty("mail.mime.allowutf8", "false"); // InternetAddress, (MimeBodyPart: session), MimeUtility
        System.setProperty("mail.mime.cachemultipart", "true");

        // https://docs.oracle.com/javaee/6/api/javax/mail/internet/MimeMultipart.html
        System.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true"); // default true, javax.mail.internet.ParseException: In parameter list
        System.setProperty("mail.mime.multipart.ignoreexistingboundaryparameter", "false"); // default false
        System.setProperty("mail.mime.multipart.ignoremissingendboundary", "true"); // default true
        System.setProperty("mail.mime.multipart.allowempty", "true"); // default false
        System.setProperty("mail.mime.contentdisposition.strict", "false"); // default true
        //System.setProperty("mail.mime.contenttypehandler", "eu.faircode.email.ContentTypeHandler");

        //System.setProperty("mail.mime.uudecode.ignoreerrors", "true");
        System.setProperty("mail.mime.uudecode.ignoremissingbeginend", "true");

        //System.setProperty("mail.imap.parse.debug", "true");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean preamble = prefs.getBoolean("preamble", false);
        boolean uid_command = prefs.getBoolean("uid_command", false);
        System.setProperty("fairemail.preamble", Boolean.toString(preamble));
        System.setProperty("fairemail.uid_command", Boolean.toString(uid_command));
    }

    static Properties getSessionProperties(boolean unicode) {
        Properties props = new Properties();

        // MIME
        // https://javaee.github.io/javamail/docs/api/javax/mail/internet/package-summary.html
        props.put("mail.mime.allowutf8", Boolean.toString(unicode)); // SMTPTransport, MimeMessage
        props.put("mail.mime.address.strict", "false");

        return props;
    }

    static MimeMessageEx from(Context context, EntityMessage message, EntityIdentity identity, Session isession, boolean send)
            throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int receipt_type = prefs.getInt("receipt_type", 2);
        boolean receipt_legacy = prefs.getBoolean("receipt_legacy", false);
        boolean hide_timezone = prefs.getBoolean("hide_timezone", false);
        boolean autocrypt = prefs.getBoolean("autocrypt", true);
        boolean mutual = prefs.getBoolean("autocrypt_mutual", true);
        boolean encrypt_subject = prefs.getBoolean("encrypt_subject", false);
        boolean forward_new = prefs.getBoolean("forward_new", false);

        if (identity != null && identity.receipt_type != null)
            receipt_type = identity.receipt_type;

        Map<String, String> c = new HashMap<>();
        c.put("id", message.id == null ? null : Long.toString(message.id));
        c.put("encrypt", message.encrypt + "/" + message.ui_encrypt);
        Log.breadcrumb("Build message", c);

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
            //imessage.addHeader("X-MSMail-Priority", "Low");
            // SpamAssassin Rule: MISSING_MIMEOLE
            // Standard description: Message has X-MSMail-Priority, but no X-MimeOLE
            // Explanation: The message is pretending to be generated by a Microsoft email program
            // which uses the extension header X-MSMail-Priority,
            // but is missing the extension header X-MimeOLE which is characteristic of Microsoft email.
            // This suggests that the sender is using badly-written mailout software,
            // rather than a genuine Microsoft email program.
        } else if (EntityMessage.PRIORITIY_HIGH.equals(message.priority)) {
            // High
            imessage.addHeader("Importance", "High");
            imessage.addHeader("Priority", "Urgent");
            imessage.addHeader("X-Priority", "1"); // Highest
            //imessage.addHeader("X-MSMail-Priority", "High");
        }

        // Sensitivity
        // https://datatracker.ietf.org/doc/html/rfc4021#section-2.1.55
        if (EntityMessage.SENSITIVITY_PERSONAL.equals(message.sensitivity))
            imessage.addHeader("Sensitivity", "Personal");
        else if (EntityMessage.SENSITIVITY_PRIVATE.equals(message.sensitivity))
            imessage.addHeader("Sensitivity", "Private");
        else if (EntityMessage.SENSITIVITY_CONFIDENTIAL.equals(message.sensitivity))
            imessage.addHeader("Sensitivity", "Company-Confidential");

        // References
        if (message.references != null)
            imessage.addHeader("References", limitReferences(message.references));

        if (message.inreplyto != null)
            imessage.addHeader("In-Reply-To", message.inreplyto);

        if (message.wasforwardedfrom != null && !forward_new)
            imessage.addHeader("X-Forwarded-Message-Id", message.wasforwardedfrom);

        imessage.addHeader(HEADER_CORRELATION_ID, message.msgid);

        MailDateFormat mdf = new MailDateFormat();
        mdf.setTimeZone(hide_timezone ? TimeZone.getTimeZone("UTC") : TimeZone.getDefault());
        String ourDate = mdf.format(new Date(message.sent == null ? message.received : message.sent));

        Address ourFrom = null;
        if (message.from != null && message.from.length > 0)
            ourFrom = getFrom(message, identity);

        if (message.headers == null || !Boolean.TRUE.equals(message.resend)) {
            imessage.setHeader("Date", ourDate);

            // Addresses
            if (ourFrom != null)
                imessage.setFrom(ourFrom);

            if (message.to != null && message.to.length > 0)
                imessage.setRecipients(Message.RecipientType.TO, convertAddress(message.to, identity));

            if (message.cc != null && message.cc.length > 0)
                imessage.setRecipients(Message.RecipientType.CC, convertAddress(message.cc, identity));

            if (message.bcc != null && message.bcc.length > 0) {
                if (false && (message.to == null || message.to.length == 0))
                    imessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse("Undisclosed-Recipients:"));
                imessage.setRecipients(Message.RecipientType.BCC, convertAddress(message.bcc, identity));
            }
        } else {
            // https://datatracker.ietf.org/doc/html/rfc2822#section-3.6.6
            ByteArrayInputStream bis = new ByteArrayInputStream(message.headers.getBytes());
            List<Header> headers = Collections.list(new InternetHeaders(bis, identity != null && identity.unicode).getAllHeaders());

            for (Header header : headers)
                try {
                    String name = header.getName();
                    String value = header.getValue();
                    if (name == null || TextUtils.isEmpty(value))
                        continue;

                    switch (name.toLowerCase(Locale.ROOT)) {
                        case "date":
                            imessage.setHeader("Date", value);
                            break;
                        case "from":
                            imessage.setFrom(value);
                            break;
                        case "to":
                            imessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(value));
                            break;
                        case "cc":
                            imessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(value));
                            break;
                        case "bcc":
                            imessage.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(value));
                            break;
                        case "reply-to":
                            imessage.setReplyTo(InternetAddress.parse(value));
                            break;
                        case "message-id":
                            if (send) {
                                imessage.setHeader("Resent-Message-ID", message.msgid);
                                imessage.updateMessageID(value);
                            }
                            break;
                        case "references":
                            imessage.setHeader("References", limitReferences(value));
                            break;
                        case "in-reply-to":
                            imessage.setHeader("In-Reply-To", value);
                            break;
                        // Resent-Sender (=on behalf of)
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }

            // The "Resent-Date:" indicates the date and time at which the resent
            //   message is dispatched by the resender of the message.
            imessage.addHeader("Resent-Date", ourDate);

            // a simple "Resent-From:" form which
            //   contains the mailbox of the individual doing the resending
            if (ourFrom != null)
                imessage.addHeader("Resent-From", ourFrom.toString());

            // The "Resent-To:", "Resent-Cc:", and "Resent-Bcc:" fields function
            //   identically to the "To:", "Cc:", and "Bcc:" fields respectively,
            //   except that they indicate the recipients of the resent message, not
            //   the recipients of the original message.
            // https://www.rfc-editor.org/rfc/rfc5322#appendix-A.3
            if (message.to != null && message.to.length > 0)
                imessage.addHeader("Resent-To", InternetAddress.toString(message.to));

            if (message.cc != null && message.cc.length > 0)
                imessage.addHeader("Resent-Cc", InternetAddress.toString(message.cc));

            if (message.bcc != null && message.bcc.length > 0)
                imessage.addHeader("Resent-Bcc", InternetAddress.toString(message.bcc));

            // Each new set of resent fields is prepended to the message;
            //   that is, the most recent set of resent fields appear earlier in the message.
            for (Header header : headers) {
                String name = header.getName();
                String value = header.getValue();
                if (name == null || TextUtils.isEmpty(value))
                    continue;
                if (name.toLowerCase(Locale.ROOT).startsWith("resent-"))
                    imessage.addHeader(name, value);
            }
        }

        if (message.subject != null) {
            int maxlen = MAX_HEADER_LENGTH - "Subject: ".length();
            if (message.subject.length() > maxlen)
                message.subject = message.subject.substring(0, maxlen - 4) + " ...";
            imessage.setSubject(message.subject);
        }

        // Send message
        if (identity != null) {
            if ((message.headers == null || !Boolean.TRUE.equals(message.resend)) &&
                    (message.dsn == null || EntityMessage.DSN_NONE.equals(message.dsn))) {
                // Add reply to
                if (identity.replyto != null)
                    imessage.setReplyTo(convertAddress(InternetAddress.parse(identity.replyto), identity));

                // Add extra cc
                if (identity.cc != null)
                    addAddress(identity.cc, Message.RecipientType.CC, imessage, identity);

                // Add extra bcc
                if (identity.bcc != null)
                    addAddress(identity.bcc, Message.RecipientType.BCC, imessage, identity);
            }

            // Delivery/read request
            if (message.receipt_request != null && message.receipt_request) {
                String to = (identity.replyto == null ? identity.email : identity.replyto);

                // 0=Read receipt
                // 1=Delivery receipt
                // 2=Read+delivery receipt

                // defacto standard
                if (receipt_type == 1 || receipt_type == 2) {
                    // Delivery receipt
                    if (receipt_legacy)
                        imessage.addHeader("Return-Receipt-To", to);
                }

                // https://tools.ietf.org/html/rfc3798
                // https://en.wikipedia.org/wiki/Return_receipt
                if (receipt_type == 0 || receipt_type == 2) {
                    // Read receipt
                    imessage.addHeader("Disposition-Notification-To", to);
                    imessage.addHeader("Read-Receipt-To", to);
                    if (receipt_legacy)
                        imessage.addHeader("Return-Receipt-To", to);
                    imessage.addHeader("X-Confirm-Reading-To", to);
                }
            }
        }

        // Auto answer
        if (message.unsubscribe != null)
            imessage.addHeader("List-Unsubscribe", "<" + message.unsubscribe + ">");

        if (message.auto_submitted != null && message.auto_submitted)
            imessage.addHeader("Auto-Submitted", "auto-replied");

        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);

        if (message.dsn == null || EntityMessage.DSN_NONE.equals(message.dsn)) {
            if (message.from != null && message.from.length > 0)
                for (EntityAttachment attachment : attachments)
                    if (EntityAttachment.PGP_KEY.equals(attachment.encryption)) {
                        InternetAddress from = (InternetAddress) message.from[0];

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
                            if (TextUtils.isEmpty(micalg)) {
                                // Some providers strip parameters
                                // https://tools.ietf.org/html/rfc3156#section-5
                                Log.w("PGP micalg missing type=" + attachment.type);
                            }
                            ParameterList params = cts.getParameterList();
                            if (params != null)
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
                            if (micalg != null)
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
                    // https://tools.ietf.org/html/rfc3156
                    BodyPart bpHeader = new MimeBodyPart();
                    bpHeader.setContent("Version: 1\n", "application/pgp-encrypted");

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

                    if (encrypt_subject)
                        imessage.setSubject("...");

                    return imessage;
                } else if (EntityAttachment.SMIME_SIGNATURE.equals(attachment.encryption)) {
                    Log.i("Sending S/MIME signed message");

                    for (final EntityAttachment content : attachments)
                        if (EntityAttachment.SMIME_CONTENT.equals(content.encryption)) {
                            BodyPart bpContent = new MimeBodyPart(new FileInputStream(content.getFile(context)));

                            final ContentType cts = new ContentType(attachment.type);
                            String micalg = cts.getParameter("micalg");
                            if (TextUtils.isEmpty(micalg)) {
                                // Some providers strip parameters
                                Log.w("S/MIME micalg missing type=" + attachment.type);
                                micalg = "sha-256";
                            }
                            ParameterList params = cts.getParameterList();
                            if (params != null)
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

            if (EntityMessage.PGP_SIGNENCRYPT.equals(message.ui_encrypt) ||
                    EntityMessage.SMIME_SIGNENCRYPT.equals(message.ui_encrypt)) {
                String msg = "Storing unencrypted message" +
                        " encrypt=" + message.encrypt + "/" + message.ui_encrypt;
                Log.w(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        build(context, message, attachments, identity, send, imessage);

        return imessage;
    }

    static Address getFrom(EntityMessage message, EntityIdentity identity) throws UnsupportedEncodingException {
        InternetAddress from = ((InternetAddress) message.from[0]);
        String email = from.getAddress();
        String name = from.getPersonal();

        if (identity != null && identity.sender_extra &&
                email != null && message.extra != null) {
            String username = UriHelper.getEmailUser(identity.email);
            if (!message.extra.equals(username)) {
                Pair<String, String> extra = getExtra(email, message.extra);

                if (extra.first != null)
                    name = extra.first;
                else if (!identity.sender_extra_name)
                    name = null;

                if (extra.second != null)
                    email = extra.second;

                Log.i("extra=\"" + name + "\" <" + email + ">");
            }
        }

        if (EntityMessage.DSN_HARD_BOUNCE.equals(message.dsn))
            name = null;

        return new InternetAddress(email, name, StandardCharsets.UTF_8.name());
    }

    static String limitReferences(String ref) {
        final int maxlen = MAX_HEADER_LENGTH - "References: ".length();

        String references = ref.trim();
        int sp = references.indexOf(' ');
        while (references.length() > maxlen && sp > 0) {
            Log.i("Dropping reference=" + references.substring(0, sp));
            references = references.substring(sp).trim();
            sp = references.indexOf(' ');
        }

        if (references.length() > maxlen) {
            Log.e("Too long References=" + Helper.getPrintableString(references, true));
            references = "";
        }

        return references;
    }

    static Pair<String, String> getExtra(String email, String extra) {
        String name = null;
        int comma = extra.lastIndexOf(',');
        if (comma >= 0) {
            name = extra.substring(0, comma).trim();
            extra = extra.substring(comma + 1).trim();
            if (TextUtils.isEmpty(extra))
                return new Pair<>(name, null);
        }

        int at = email.indexOf('@');
        if (at < 0)
            return new Pair<>(name, email);

        if (extra.length() > 1 && extra.startsWith("+"))
            email = email.substring(0, at) + extra + email.substring(at);
        else if (extra.length() > 1 && extra.startsWith("@"))
            email = email.substring(0, at) + extra + '.' + email.substring(at + 1);
        else
            email = extra + email.substring(at);

        return new Pair<>(name, email);
    }

    private static void addAddress(String email, Message.RecipientType type, MimeMessage imessage, EntityIdentity identity) throws MessagingException {
        List<Address> result = new ArrayList<>();

        Address[] existing = imessage.getRecipients(type);
        if (existing != null)
            result.addAll(Arrays.asList(existing));

        Address[] all = imessage.getAllRecipients(); // to, cc, bcc
        Address[] addresses = convertAddress(InternetAddress.parse(email), identity);
        for (Address address : addresses) {
            boolean found = false;
            if (all != null)
                for (Address a : all)
                    if (equalEmail(a, address)) {
                        found = true;
                        break;
                    }
            if (!found)
                result.add(address);
        }

        imessage.setRecipients(type, result.toArray(new Address[0]));
    }

    private static Address[] convertAddress(Address[] addresses, EntityIdentity identity) {
        if (identity != null && identity.unicode)
            return addresses;

        // https://en.wikipedia.org/wiki/International_email
        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            email = toPunyCode(email, false);
            ((InternetAddress) address).setAddress(email);
        }
        return addresses;
    }

    static void build(Context context, EntityMessage message, List<EntityAttachment> attachments, EntityIdentity identity, boolean send, MimeMessage imessage) throws IOException, MessagingException {
        if (EntityMessage.DSN_RECEIPT.equals(message.dsn)) {
            // https://www.ietf.org/rfc/rfc3798.txt
            Multipart report = new MimeMultipart("report; report-type=disposition-notification");

            String html = Helper.readText(message.getFile(context));
            String plainContent = HtmlHelper.getText(context, html);

            BodyPart plainPart = new MimeBodyPart();
            plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            report.addBodyPart(plainPart);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean client_id = prefs.getBoolean("client_id", true);

            String from = null;
            if (message.from != null && message.from.length > 0)
                from = ((InternetAddress) message.from[0]).getAddress();

            StringBuilder sb = new StringBuilder();

            sb.append("Reporting-UA: ");
            if (client_id)
                sb.append(BuildConfig.APPLICATION_ID).append("; ")
                        .append(context.getString(R.string.app_name)).append(' ')
                        .append(BuildConfig.VERSION_NAME).append("\r\n");
            else
                sb.append("example.com").append("\r\n");

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
        } else if (EntityMessage.DSN_HARD_BOUNCE.equals(message.dsn)) {
            // https://tools.ietf.org/html/rfc3464
            Multipart report = new MimeMultipart("report; report-type=delivery-status");

            String html = Helper.readText(message.getFile(context));
            String plainContent = HtmlHelper.getText(context, html);

            BodyPart plainPart = new MimeBodyPart();
            plainPart.setContent(plainContent, "text/plain; charset=" + Charset.defaultCharset().name());
            report.addBodyPart(plainPart);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean client_id = prefs.getBoolean("client_id", true);

            String from = null;
            if (message.from != null && message.from.length > 0)
                from = ((InternetAddress) message.from[0]).getAddress();

            StringBuilder sb = new StringBuilder();
            sb.append("Reporting-MTA: dns;");
            if (client_id)
                sb.append(EmailService.getDefaultEhlo()).append("\r\n");
            else
                sb.append("example.com").append("\r\n");
            sb.append("\r\n");

            if (from != null)
                sb.append("Final-Recipient: rfc822;").append(from).append("\r\n");

            sb.append("Action: failed").append("\r\n");
            sb.append("Status: 5.1.1").append("\r\n"); // https://tools.ietf.org/html/rfc3463
            sb.append("Diagnostic-Code: smtp; 550 user unknown").append("\r\n");

            MailDateFormat mdf = new MailDateFormat();
            mdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            sb.append("Last-Attempt-Date: ").append(mdf.format(message.received)).append("\r\n");

            BodyPart dnsPart = new MimeBodyPart();
            dnsPart.setContent(sb.toString(), "message/delivery-status");
            dnsPart.setDisposition(Part.INLINE);
            report.addBodyPart(dnsPart);

            imessage.setContent(report);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean format_flowed = prefs.getBoolean("format_flowed", false);
        int compose_color = prefs.getInt("compose_color", Color.TRANSPARENT);
        String compose_font = prefs.getString("compose_font", "");
        String compose_text_size = prefs.getString("compose_text_size", "");
        boolean auto_link = prefs.getBoolean("auto_link", false);

        // Build html body
        Document document = JsoupEx.parse(message.getFile(context));

        boolean resend = false;
        if (message.headers != null && Boolean.TRUE.equals(message.resend)) {
            Element body = document.body();
            if (body.children().size() == 1) {
                // Restore original body
                Element ref = body.children().get(0);
                if ("reference".equals(ref.attr("fairemail"))) {
                    body.replaceWith(ref.tagName("body").removeAttr("fairemail"));
                    resend = true;
                }
            }
        }

        if (!resend) {
            // https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/lang
            if (message.language != null)
                document.body().attr("lang", message.language);

            String defaultStyles = document.body().attr("style");
            //defaultStyles = HtmlHelper.mergeStyles(defaultStyles, "font-size: medium;");
            if (!TextUtils.isEmpty(defaultStyles))
                document.body().attr("style", defaultStyles);

            // When sending message
            if (identity != null && send) {
                if (auto_link) {
                    HtmlHelper.guessSchemes(document);
                    HtmlHelper.autoLink(document, true);
                }

                if (compose_color != Color.TRANSPARENT ||
                        !TextUtils.isEmpty(compose_font) ||
                        !TextUtils.isEmpty(compose_text_size)) {
                    List<Node> childs = new ArrayList<>();
                    for (Node child : document.body().childNodes())
                        if (TextUtils.isEmpty(child.attr("fairemail"))) {
                            childs.add(child);
                            child.remove();
                        } else
                            break;

                    StringBuilder style = new StringBuilder();
                    if (compose_color != Color.TRANSPARENT)
                        style.append("color: ").append(HtmlHelper.encodeWebColor(compose_color)).append(';');
                    if (!TextUtils.isEmpty(compose_font))
                        style.append("font-family: ").append(StyleHelper.getFamily(compose_font)).append(';');
                    if (!TextUtils.isEmpty(compose_text_size))
                        style.append("font-size: ").append(compose_text_size).append(';');

                    Element div = document.createElement("div").attr("style", style.toString());

                    for (Node child : childs)
                        div.appendChild(child);
                    document.body().prependChild(div);
                }

                document.select("div[fairemail=signature]")
                        .removeAttr("fairemail")
                        .addClass("fairemail_signature");
                document.select("div[fairemail=reference]")
                        .removeAttr("fairemail");

                Elements reply = document.select("div[fairemail=reply]");
                if (message.isPlainOnly())
                    reply.select("strong").tagName("span");
                reply.removeAttr("fairemail");

                DB db = DB.getInstance(context);

                try {
                    db.beginTransaction();

                    for (Element img : document.select("img")) {
                        String source = img.attr("src");
                        if (!source.startsWith("content:"))
                            continue;

                        Uri uri = Uri.parse(source);
                        DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
                        if (dfile == null)
                            continue;

                        String name = dfile.getName();
                        String type = dfile.getType();

                        if (TextUtils.isEmpty(name))
                            name = uri.getLastPathSegment();
                        if (TextUtils.isEmpty(type))
                            type = "image/*";

                        String cid = BuildConfig.APPLICATION_ID + ".content." + Math.abs(source.hashCode());
                        String acid = "<" + cid + ">";

                        if (db.attachment().getAttachment(message.id, acid) == null) {
                            EntityAttachment attachment = new EntityAttachment();
                            attachment.message = message.id;
                            attachment.sequence = db.attachment().getAttachmentSequence(message.id) + 1;
                            attachment.name = name;
                            attachment.type = type;
                            attachment.disposition = Part.INLINE;
                            attachment.cid = acid;
                            attachment.related = true;
                            attachment.size = null;
                            attachment.progress = 0;
                            attachment.id = db.attachment().insertAttachment(attachment);

                            attachment.size = Helper.copy(context, uri, attachment.getFile(context));
                            attachment.progress = null;
                            attachment.available = true;
                            db.attachment().setDownloaded(attachment.id, attachment.size);

                            attachments.add(attachment);
                        }

                        img.attr("src", "cid:" + cid);
                    }

                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.w(ex);
                } finally {
                    db.endTransaction();
                }

                if (ActivityBilling.isPro(context)) {
                    VCard vcard = null;

                    if (identity.uri != null &&
                            Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
                        vcard = new VCard();

                        ContentResolver resolver = context.getContentResolver();
                        try (Cursor cursor = resolver.query(Uri.parse(identity.uri),
                                new String[]{
                                        ContactsContract.Contacts._ID,
                                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                                }, null, null, null)) {
                            if (cursor != null && cursor.moveToFirst()) {
                                String contactId = cursor.getString(0);
                                String display = cursor.getString(1);

                                vcard.setFormattedName(display);

                                try (Cursor email = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                        new String[]{
                                                ContactsContract.CommonDataKinds.Email.TYPE,
                                                ContactsContract.CommonDataKinds.Email.ADDRESS
                                        },
                                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                        new String[]{contactId}, null)) {
                                    while (email.moveToNext()) {
                                        int type = email.getInt(0);
                                        String address = email.getString(1);

                                        switch (type) {
                                            case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                                                vcard.addEmail(address, EmailType.HOME);
                                                break;
                                            case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                                                vcard.addEmail(address, EmailType.WORK);
                                                break;
                                            case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                                                vcard.addEmail(new Email(address));
                                                break;
                                        }
                                    }
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }

                                try (Cursor address = resolver.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                        new String[]{
                                                ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                                                ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                                                ContactsContract.CommonDataKinds.StructuredPostal.POBOX,
                                                ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                                                ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                                                ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                                                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                                        },
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[]{contactId}, null)) {
                                    while (address.moveToNext()) {
                                        int type = address.getInt(0);
                                        if (type != ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME &&
                                                type != ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
                                            continue;

                                        ezvcard.property.Address a = new ezvcard.property.Address();
                                        if (!address.isNull(1))
                                            a.setStreetAddress(address.getString(1));
                                        if (!address.isNull(2))
                                            a.setPoBox(address.getString(2));
                                        if (!address.isNull(3))
                                            a.setLocality(address.getString(3));
                                        if (!address.isNull(4))
                                            a.setPostalCode(address.getString(4));
                                        if (!address.isNull(5))
                                            a.setCountry(address.getString(5));
                                        if (!address.isNull(6))
                                            a.setLabel(address.getString(6));

                                        switch (type) {
                                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
                                                a.setParameter("TYPE", AddressType.HOME.getValue());
                                                break;
                                            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
                                                a.setParameter("TYPE", AddressType.WORK.getValue());
                                                break;
                                        }

                                        vcard.addAddress(a);
                                    }
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }

                                try (Cursor web = resolver.query(ContactsContract.Data.CONTENT_URI,
                                        new String[]{
                                                ContactsContract.CommonDataKinds.Website.TYPE,
                                                ContactsContract.CommonDataKinds.Website.URL
                                        },
                                        ContactsContract.Data.CONTACT_ID + " = ?" +
                                                " AND " + ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                                        new String[]{
                                                contactId,
                                                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                                        }, null)) {
                                    while (web.moveToNext()) {
                                        int type = web.getInt(0);
                                        String url = web.getString(1);
                                        vcard.addUrl(url);
                                    }
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }

                                try (Cursor phones = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        new String[]{
                                                ContactsContract.CommonDataKinds.Phone.TYPE,
                                                ContactsContract.CommonDataKinds.Phone.NUMBER
                                        },
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[]{
                                                contactId
                                        }, null)) {
                                    while (phones.moveToNext()) {
                                        int type = phones.getInt(0);
                                        String number = phones.getString(1);
                                        switch (type) {
                                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                                vcard.addTelephoneNumber(number, TelephoneType.HOME);
                                                break;
                                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                                vcard.addTelephoneNumber(number, TelephoneType.WORK);
                                                break;
                                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                                vcard.addTelephoneNumber(number, TelephoneType.CELL);
                                                break;
                                        }
                                    }
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }
                            }
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                    }

                    try {
                        db.beginTransaction();

                        for (EntityAttachment attachment : new ArrayList<>(attachments))
                            if (attachment.cid != null && attachment.cid.startsWith(EntityAttachment.VCARD_PREFIX)) {
                                db.attachment().deleteAttachment(attachment.id);
                                attachments.remove(attachment);
                            }

                        if (vcard != null) {
                            EntityAttachment attachment = new EntityAttachment();
                            attachment.message = message.id;
                            attachment.sequence = db.attachment().getAttachmentSequence(message.id) + 1;
                            attachment.name = "contact.vcf";
                            attachment.type = "text/vcard";
                            attachment.disposition = Part.ATTACHMENT;
                            attachment.cid = EntityAttachment.VCARD_PREFIX + Math.abs(identity.uri.hashCode());
                            attachment.size = null;
                            attachment.progress = 0;
                            attachment.id = db.attachment().insertAttachment(attachment);

                            File file = attachment.getFile(context);
                            try (OutputStream os = new FileOutputStream(file)) {
                                try (VCardWriter writer = new VCardWriter(os, VCardVersion.V3_0)) {
                                    writer.write(vcard);
                                }
                            }

                            attachment.size = file.length();
                            attachment.progress = null;
                            attachment.available = true;
                            db.attachment().setDownloaded(attachment.id, attachment.size);

                            attachments.add(attachment);
                        }

                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    } finally {
                        db.endTransaction();
                    }
                }
            }
        }

        // multipart/mixed
        //   multipart/related
        //     multipart/alternative
        //       text/plain
        //       text/html
        //     inlines
        //  attachments

        String htmlContent = document.html();
        String htmlContentType = "text/html; charset=" + Charset.defaultCharset().name();

        String plainContent = HtmlHelper.getText(context, document.html());
        String plainContentType = "text/plain; charset=" + Charset.defaultCharset().name();

        if (format_flowed) {
            List<String> flowed = new ArrayList<>();
            for (String line : plainContent.split("\\r?\\n")) {
                if (line.contains(" ") && !"-- ".equals(line)) {
                    StringBuilder sb = new StringBuilder();
                    for (String word : line.split(" ")) {
                        if (sb.length() + word.length() > FORMAT_FLOWED_LINE_LENGTH) {
                            sb.append(' ');
                            flowed.add(sb.toString());

                            // https://tools.ietf.org/html/rfc3676#section-4.5
                            int i = 0;
                            if (sb.length() > 0 && sb.charAt(0) == '>') {
                                i++;
                                while (i < sb.length() &&
                                        (sb.charAt(i) == '>' || sb.charAt(i) == ' '))
                                    i++;
                            }
                            String prefix = sb.substring(0, i).trim();

                            sb = new StringBuilder(prefix);
                        }
                        if (sb.length() > 0)
                            sb.append(' ');
                        sb.append(word);
                    }
                    if (sb.length() > 0)
                        flowed.add(sb.toString());
                } else
                    flowed.add(line);
            }
            plainContent = TextUtils.join("\r\n", flowed);
            plainContentType += "; format=flowed";
        }

        BodyPart plainPart = new MimeBodyPart();
        plainPart.setContent(plainContent, plainContentType);

        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, htmlContentType);

        Multipart altMultiPart = new MimeMultipart("alternative");
        altMultiPart.addBodyPart(plainPart);
        altMultiPart.addBodyPart(htmlPart);

        for (EntityAttachment attachment : attachments)
            if (attachment.available &&
                    "text/calendar".equals(attachment.type))
                try {
                    File file = attachment.getFile(context);
                    ICalendar icalendar = CalendarHelper.parse(context, file);
                    Method method = (icalendar == null ? null : icalendar.getMethod());
                    if (method != null && method.isReply()) {
                        // https://www.rfc-editor.org/rfc/rfc6047#section-2.4
                        BodyPart calPart = new MimeBodyPart();
                        calPart.setContent(icalendar.write(), attachment.type + ";" +
                                " method=" + method.getValue() + ";" +
                                " charset=UTF-8;");
                        altMultiPart.addBodyPart(calPart);
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
                }

        int availableAttachments = 0;
        boolean hasInline = false;
        for (EntityAttachment attachment : attachments)
            if (attachment.available) {
                availableAttachments++;
                if (attachment.isInline())
                    hasInline = true;
            }

        if (availableAttachments == 0)
            if (message.isPlainOnly())
                imessage.setContent(plainContent, plainContentType);
            else
                imessage.setContent(altMultiPart);
        else {
            Multipart mixedMultiPart = new MimeMultipart("mixed");
            Multipart relatedMultiPart = new MimeMultipart("related");

            BodyPart bodyPart;
            if (message.isPlainOnly())
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
                                return attachment.type + "; charset=UTF-8;";

                            return attachment.type;
                        }

                        @Override
                        public String getContentType(String filename) {
                            return getContentType(new File(filename));
                        }
                    });
                    attachmentPart.setDataHandler(new DataHandler(dataSource));

                    if (attachment.name != null)
                        attachmentPart.setFileName(attachment.name);
                    if (attachment.disposition != null)
                        attachmentPart.setDisposition(attachment.disposition);
                    if (attachment.cid != null)
                        attachmentPart.setHeader("Content-ID", attachment.cid);
                    if ("message/rfc822".equals(attachment.type))
                        attachmentPart.setHeader("Content-Transfer-Encoding", "base64");

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

    MessageHelper(MimeMessage message, Context context) throws IOException {
        long cake = Helper.getAvailableStorageSpace();
        if (cake < Helper.MIN_REQUIRED_SPACE)
            throw new IOException(context.getString(R.string.app_cake),
                    new ErrnoException(context.getPackageName(), ENOSPC));
        this.imessage = message;
    }

    boolean isReport() {
        try {
            return isMimeType(imessage, "multipart/report");
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    boolean getRecent() throws MessagingException {
        return imessage.isSet(Flags.Flag.RECENT);
    }

    boolean getSeen() throws MessagingException {
        return imessage.isSet(Flags.Flag.SEEN);
    }

    boolean getAnswered() throws MessagingException {
        return imessage.isSet(Flags.Flag.ANSWERED);
    }

    boolean getFlagged() throws MessagingException {
        return imessage.isSet(Flags.Flag.FLAGGED);
    }

    boolean getDeleted() throws MessagingException {
        return imessage.isSet(Flags.Flag.DELETED);
    }

    String getFlags() throws MessagingException {
        if (!BuildConfig.DEBUG)
            return null;

        Flags flags = imessage.getFlags();
        flags.clearUserFlags();
        return flags.toString();
    }

    @NonNull
    String[] getKeywords(boolean outlook) throws MessagingException {
        List<String> keywords = new ArrayList<>(Arrays.asList(imessage.getFlags().getUserFlags()));

        if (outlook) {
            String categories = imessage.getHeader("Keywords", null);
            if (!TextUtils.isEmpty(categories))
                for (String category : categories.split(","))
                    keywords.add(CATEGORY_PREFIX + category);
        }

        while (keywords.size() > MAX_KEYWORDS)
            keywords.remove(keywords.size() - 1);

        Collections.sort(keywords);
        return keywords.toArray(new String[0]);
    }

    static boolean showKeyword(String keyword) {
        int len = FLAG_BLACKLIST.size();
        for (int i = 0; i < len; i++)
            if (FLAG_BLACKLIST.get(i).equalsIgnoreCase(keyword))
                return false;

        return true;
    }

    String getMessageID() throws MessagingException {
        ensureEnvelope();

        // Outlook outbox -> sent
        //   x-microsoft-original-message-id
        String header = imessage.getHeader(HEADER_CORRELATION_ID, null);
        if (header == null)
            header = imessage.getHeader("Message-ID", null);
        return (header == null ? null : MimeUtility.unfold(header));
    }

    @NonNull
    String getPOP3MessageID() throws MessagingException {
        String msgid = getMessageID();
        if (TextUtils.isEmpty(msgid)) {
            Long time = getSent();
            if (time == null)
                msgid = getHash();
            else
                msgid = Long.toString(time);
        }
        return msgid;
    }

    List<Header> getAllHeaders() throws MessagingException {
        ensureHeaders();
        return Collections.list(imessage.getAllHeaders());
    }

    String[] getReferences() throws MessagingException {
        ensureHeaders();

        List<String> result = new ArrayList<>();
        String refs = imessage.getHeader("References", null);
        result.addAll(getReferences(refs));

        // Merge references of reported message for threading
        InternetHeaders iheaders = getReportHeaders();
        if (iheaders != null) {
            String arefs = iheaders.getHeader("References", null);
            for (String ref : getReferences(arefs))
                if (!result.contains(ref)) {
                    Log.i("rfc822 ref=" + ref);
                    result.add(ref);
                }

            String amsgid = iheaders.getHeader("Message-Id", null);
            if (!TextUtils.isEmpty(amsgid)) {
                String msgid = MimeUtility.unfold(amsgid);
                if (!result.contains(msgid)) {
                    Log.i("rfc822 id=" + msgid);
                    result.add(msgid);
                }
            }
        }

        return result.toArray(new String[0]);
    }

    private List<String> getReferences(String header) {
        List<String> result = new ArrayList<>();
        if (header == null)
            return result;
        header = MimeUtility.unfold(header);
        if (TextUtils.isEmpty(header))
            return result;
        header = header
                .replaceAll("<\\s*<", "<")
                .replaceAll(">\\s*>", ">");
        for (String ref : header.split("[,\\s]+"))
            if (!result.contains(ref))
                result.add(ref);
        return result;
    }

    String getDeliveredTo() throws MessagingException {
        ensureHeaders();

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
        String[] a = getInReplyTos();
        return (a.length < 1 ? null : a[0]);
    }

    String[] getInReplyTos() throws MessagingException {
        ensureHeaders();

        List<String> result = new ArrayList<>();

        String header = imessage.getHeader("In-Reply-To", null);
        result.addAll(getReferences(header));

        if (result.size() == 0) {
            // Use reported message ID as synthetic in-reply-to
            InternetHeaders iheaders = getReportHeaders();
            if (iheaders != null) {
                header = iheaders.getHeader("Message-Id", null);
                if (!TextUtils.isEmpty(header)) {
                    result.add(header);
                    Log.i("rfc822 id=" + header);
                }
            }
        }

        return result.toArray(new String[0]);
    }

    private InternetHeaders getReportHeaders() {
        try {
            ensureStructure();

            if (isMimeType(imessage, "multipart/report")) {
                ContentType ct = new ContentType(imessage.getContentType());
                String reportType = ct.getParameter("report-type");
                if ("delivery-status".equalsIgnoreCase(reportType) ||
                        "disposition-notification".equalsIgnoreCase(reportType) ||
                        "feedback-report".equalsIgnoreCase(reportType)) {
                    MessageParts parts = new MessageParts();
                    getMessageParts(null, imessage, parts, null);
                    for (AttachmentPart apart : parts.attachments)
                        if ("text/rfc822-headers".equalsIgnoreCase(apart.attachment.type)) {
                            reportHeaders = new InternetHeaders(apart.part.getInputStream(), true);
                            break;
                        } else if ("message/rfc822".equalsIgnoreCase(apart.attachment.type)) {
                            Properties props = MessageHelper.getSessionProperties(true);
                            Session isession = Session.getInstance(props, null);
                            MimeMessage amessage = new MimeMessage(isession, apart.part.getInputStream());
                            reportHeaders = amessage.getHeaders();
                            break;
                        }
                }
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return reportHeaders;
    }

    String getThreadId(Context context, long account, long folder, long uid, long received) throws MessagingException {
        if (threadId == null)
            if (true)
                threadId = _getThreadIdAlt(context, account, folder, uid, received);
            else
                threadId = _getThreadId(context, account, folder, uid);
        return threadId;
    }

    private String _getThreadId(Context context, long account, long folder, long uid) throws MessagingException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (imessage instanceof GmailMessage) {
            // https://developers.google.com/gmail/imap/imap-extensions#access_to_the_gmail_thread_id_x-gm-thrid
            boolean gmail_thread_id = prefs.getBoolean("gmail_thread_id", false);
            if (gmail_thread_id) {
                long thrid = ((GmailMessage) imessage).getThrId();
                if (thrid > 0)
                    return "gmail:" + thrid;
            }
        }

        String thread = null;
        String msgid = getMessageID();

        List<String> refs = new ArrayList<>();

        for (String ref : getReferences())
            if (!TextUtils.isEmpty(ref) && !refs.contains(ref))
                refs.add(ref);

        for (String inreplyto : getInReplyTos())
            if (!TextUtils.isEmpty(inreplyto) && !refs.contains(inreplyto))
                refs.add(inreplyto);

        Collections.sort(refs);

        DB db = DB.getInstance(context);
        List<EntityMessage> before = new ArrayList<>();
        for (String ref : refs)
            before.addAll(db.message().getMessagesByMsgId(account, ref));

        String origin = null;

        for (EntityMessage message : before)
            if (!TextUtils.isEmpty(message.thread)) {
                origin = "before";
                thread = message.thread;
                break;
            }

        if (thread == null) {
            List<EntityMessage> similar = db.message().getMessagesByMsgId(account, msgid);
            for (EntityMessage message : similar)
                if (!TextUtils.isEmpty(message.thread) && Objects.equals(message.hash, getHash())) {
                    origin = "similar";
                    thread = message.thread;
                    break;
                }
        }

        // Common reference
        if (thread == null && refs.size() > 0) {
            String ref = refs.get(0);
            if (!Objects.equals(ref, msgid)) {
                origin = "common";
                thread = ref;
            }
        }

        if (thread == null) {
            origin = "hash";
            thread = getHash() + ":" + uid;
        }

        for (EntityMessage message : before)
            if (!thread.equals(message.thread)) {
                Log.w("Updating before thread from " + message.thread + " to " + thread + " origin=" + origin);
                db.message().updateMessageThread(message.account, message.thread, thread, null);
            }

        List<EntityMessage> after = db.message().getMessagesByInReplyTo(account, msgid);
        for (EntityMessage message : after)
            if (!thread.equals(message.thread)) {
                Log.w("Updating after thread from " + message.thread + " to " + thread + " origin=" + origin);
                db.message().updateMessageThread(message.account, message.thread, thread, null);
            }

        boolean subject_threading = prefs.getBoolean("subject_threading", false);
        if (subject_threading && !isReport()) {
            String sender = getSortKey(getFrom());
            String subject = getSubject();
            long since = new Date().getTime() - MAX_SUBJECT_AGE * 3600 * 1000L;
            if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(subject)) {
                List<EntityMessage> subjects = db.message().getMessagesBySubject(account, sender, subject, since);
                for (EntityMessage message : subjects)
                    if (!thread.equals(message.thread)) {
                        Log.w("Updating subject thread from " + message.thread + " to " + thread);
                        db.message().updateMessageThread(message.account, message.thread, thread, since);
                    }
            }
        }

        return thread;
    }

    private String _getThreadIdAlt(Context context, long account, long folder, long uid, long received) throws MessagingException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (imessage instanceof GmailMessage) {
            // https://developers.google.com/gmail/imap/imap-extensions#access_to_the_gmail_thread_id_x-gm-thrid
            boolean gmail_thread_id = prefs.getBoolean("gmail_thread_id", false);
            if (gmail_thread_id) {
                long thrid = ((GmailMessage) imessage).getThrId();
                Log.i("Gmail thread=" + thrid);
                if (thrid > 0)
                    return "gmail:" + thrid;
            }
        }

        // https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxomsg/9e994fbb-b839-495f-84e3-2c8c02c7dd9b
        if (BuildConfig.DEBUG && false)
            try {
                String tindex = imessage.getHeader("Thread-Index", null);
                if (tindex != null) {
                    boolean outlook_thread_id = prefs.getBoolean("outlook_thread_id", false);
                    if (outlook_thread_id) {
                        byte[] data = Base64.decode(tindex, Base64.DEFAULT);
                        if (data.length >= 22) {
                            long msb = 0, lsb = 0;
                            for (int i = 0 + 6; i < 8 + 6; i++)
                                msb = (msb << 8) | (data[i] & 0xff);
                            for (int i = 8 + 6; i < 16 + 6; i++)
                                lsb = (lsb << 8) | (data[i] & 0xff);
                            UUID guid = new UUID(msb, lsb);
                            Log.i("Outlook thread=" + guid);
                            return "outlook:" + guid;
                        }
                    }
                }
            } catch (Throwable ex) {
                Log.w(ex);
            }

        String thread = null;
        String msgid = getMessageID();

        List<String> refs = new ArrayList<>();

        String inreplyto = getInReplyTo();
        if (!TextUtils.isEmpty(inreplyto) && !refs.contains(inreplyto))
            refs.add(inreplyto);

        // The "References:" field will contain the contents of the parent's "References:" field (if any)
        // followed by the contents of the parent's "Message-ID:" field (if any).
        String[] r = getReferences();
        for (int i = r.length - 1; i >= 0; i--) {
            String ref = r[i];
            if (!TextUtils.isEmpty(ref) && !refs.contains(ref))
                refs.add(ref);
        }

        boolean forward_new = prefs.getBoolean("forward_new", false);
        if (!forward_new)
            try {
                String fwd = imessage.getHeader("X-Forwarded-Message-Id", null);
                if (!TextUtils.isEmpty(fwd) && !refs.contains(fwd))
                    refs.add(fwd);
            } catch (Throwable ex) {
                Log.w(ex);
            }

        DB db = DB.getInstance(context);

        List<String> all = new ArrayList<>();
        if (!TextUtils.isEmpty(msgid))
            all.add(msgid);
        all.addAll(refs);

        // https://www.sqlite.org/limits.html
        if (all.size() > 450)
            all = all.subList(0, 450);

        int thread_range = prefs.getInt("thread_range", MessageHelper.DEFAULT_THREAD_RANGE);
        int range = (int) Math.pow(2, thread_range);
        Long start = (received == 0 ? null : received - range * 24 * 3600 * 1000L);
        Long end = (received == 0 ? null : received + range * 24 * 3600 * 1000L);

        List<TupleThreadInfo> infos = (all.size() == 0
                ? new ArrayList<>()
                : db.message().getThreadInfo(account, all, start, end));

        // References, In-Reply-To (sent before)
        for (TupleThreadInfo info : infos)
            if (info.isReferencing(msgid) && !TextUtils.isEmpty(info.thread)) {
                thread = info.thread;
                break;
            }

        // Similar
        if (thread == null) {
            for (TupleThreadInfo info : infos)
                if (info.isSelf(msgid) && !TextUtils.isEmpty(info.thread) &&
                        Objects.equals(info.hash, getHash())) {
                    thread = info.thread;
                    break;
                }
        }

        if (thread == null && !TextUtils.isEmpty(BuildConfig.DEV_DOMAIN) && false) {
            String awsses = imessage.getHeader("X-SES-Outgoing", null);
            if (!TextUtils.isEmpty(awsses)) {
                Address[] froms = getFrom();
                if (froms != null && froms.length > 0) {
                    String from = ((InternetAddress) froms[0]).getAddress();
                    if (!TextUtils.isEmpty(from) && from.endsWith("@" + BuildConfig.DEV_DOMAIN)) {
                        Address[] rr = getReply();
                        Address[] tos = (rr != null && rr.length > 0 ? rr : getTo());
                        if (tos != null && tos.length > 0) {
                            String email = ((InternetAddress) tos[0]).getAddress();
                            if (!TextUtils.isEmpty(email) && !email.endsWith("@" + BuildConfig.DEV_DOMAIN))
                                thread = "ses:" + email;
                        }
                    }
                }
            }
        }

        // Common reference
        boolean thread_byref = prefs.getBoolean("thread_byref", !Helper.isPlayStoreInstall());
        if (thread == null && refs.size() > 0 && thread_byref) {
            // For example
            //   Message-ID: <organization/project/pull/nnn/issue_event/xxx@github.com>
            //   In-Reply-To: <organization/project/pull/nnn@github.com>
            //   References: <organization/project/pull/nnn@github.com>
            String ref = refs.get(0);
            if (!Objects.equals(ref, msgid))
                thread = account + ":" + ref;
        }

        if (thread == null)
            thread = account + ":" + getHash() + ":" + uid;

        // Sent before
        for (TupleThreadInfo info : infos)
            if (info.isReferencing(msgid) && !thread.equals(info.thread)) {
                Log.w("Updating before thread from " + info.thread + " to " + thread);
                db.message().updateMessageThread(account, info.thread, thread, null);
            }

        // Sent after
        for (TupleThreadInfo info : infos)
            if (info.isReferenced(msgid) && !thread.equals(info.thread)) {
                Log.w("Updating after thread from " + info.thread + " to " + thread);
                db.message().updateMessageThread(account, info.thread, thread, null);
            }

        boolean subject_threading = prefs.getBoolean("subject_threading", false);
        if (subject_threading && !isReport()) {
            String sender = getSortKey(getFrom());
            String subject = getSubject();
            long since = new Date().getTime() - MAX_SUBJECT_AGE * 3600 * 1000L;
            if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(subject)) {
                List<EntityMessage> subjects = db.message().getMessagesBySubject(account, sender, subject, since);
                for (EntityMessage message : subjects)
                    if (!thread.equals(message.thread)) {
                        Log.w("Updating subject thread from " + message.thread + " to " + thread);
                        db.message().updateMessageThread(message.account, message.thread, thread, since);
                    }
            }
        }

        return thread;
    }

    String[] getLabels() throws MessagingException {
        //ensureMessage(false);

        List<String> labels = new ArrayList<>();
        if (imessage instanceof GmailMessage)
            for (String label : ((GmailMessage) imessage).getLabels())
                if (!label.startsWith("\\"))
                    labels.add(label);

        while (labels.size() > MAX_LABELS)
            labels.remove(labels.size() - 1);

        Collections.sort(labels);

        return labels.toArray(new String[0]);
    }

    Integer getPriority() throws MessagingException {
        Integer priority = null;

        ensureHeaders();

        // https://tools.ietf.org/html/rfc2156
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
                "lowest".equalsIgnoreCase(header) ||
                "non-urgent".equalsIgnoreCase(header) ||
                "marketing".equalsIgnoreCase(header) ||
                "bulk".equalsIgnoreCase(header) ||
                "batch".equalsIgnoreCase(header) ||
                "mass".equalsIgnoreCase(header) ||
                "none".equalsIgnoreCase(header))
            priority = EntityMessage.PRIORITIY_LOW;
        else if ("a".equalsIgnoreCase(header) ||
                "b".equalsIgnoreCase(header) ||
                "c".equalsIgnoreCase(header) ||
                "aplus".equalsIgnoreCase(header))
            ; // Ignore unknown
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
                Log.w("priority=" + header);
            }

        if (EntityMessage.PRIORITIY_NORMAL.equals(priority))
            priority = null;

        return priority;
    }

    Integer getSensitivity() throws MessagingException {
        ensureHeaders();

        // https://www.rfc-editor.org/rfc/rfc4021.html#section-2.1.55
        String header = imessage.getHeader("Sensitivity", null);

        if (TextUtils.isEmpty(header))
            return null;

        header = header.toLowerCase(Locale.ROOT);

        if (header.contains("personal"))
            return EntityMessage.SENSITIVITY_PERSONAL;
        if (header.contains("private"))
            return EntityMessage.SENSITIVITY_PRIVATE;
        if (header.contains("company")) // company-confidential
            return EntityMessage.SENSITIVITY_CONFIDENTIAL;

        return null;
    }

    Boolean getAutoSubmitted() throws MessagingException {
        // https://tools.ietf.org/html/rfc3834
        // auto-generated, auto-replied
        String header = imessage.getHeader("Auto-Submitted", null);
        if (header == null) {
            // https://www.arp242.net/autoreply.html
            // https://github.com/jpmckinney/multi_mail/wiki/Detecting-autoresponders

            // Microsoft
            header = imessage.getHeader("X-Auto-Response-Suppress", null);
            if ("DR".equalsIgnoreCase(header)) // Suppress delivery reports
                return true;
            if ("AutoReply".equalsIgnoreCase(header)) // Suppress autoreply messages other than OOF notifications
                return true;
            if ("All".equalsIgnoreCase(header))
                return true;

            // Google
            // Feedback-ID: nnnnnnn:user:proton
            header = imessage.getHeader("Feedback-ID", null);
            if (header != null &&
                    !header.endsWith(":user:proton")) // How privacy-friendly is this anyway?
                return true;

            header = imessage.getHeader("Precedence", null);
            if ("bulk".equalsIgnoreCase(header)) // Used by Amazon
                return true;
            if ("auto_reply".equalsIgnoreCase(header))
                return true;
            if ("list".equalsIgnoreCase(header))
                return true;

            // Lists
            header = imessage.getHeader("List-Id", null);
            if (header != null)
                return true;
            header = imessage.getHeader("List-Unsubscribe", null);
            if (header != null)
                return true;

            return null;
        }

        return !"no".equalsIgnoreCase(header);
    }

    boolean getReceiptRequested() throws MessagingException {
        Address[] headers = getReceiptTo();
        return (headers != null && headers.length > 0);
    }

    Address[] getReceiptTo() throws MessagingException {
        // Return-Receipt-To = delivery receipt
        // Disposition-Notification-To = read receipt
        Address[] receipt = getAddressHeader("Disposition-Notification-To");
        if (receipt == null || receipt.length == 0)
            receipt = getAddressHeader("Read-Receipt-To");
        if (receipt == null || receipt.length == 0)
            receipt = getAddressHeader("Return-Receipt-To");
        if (receipt == null || receipt.length == 0)
            receipt = getAddressHeader("X-Confirm-Reading-To");
        return receipt;
    }

    String getBimiSelector() throws MessagingException {
        ensureHeaders();

        // BIMI-Selector: v=BIMI1; s=selector;
        String header = imessage.getHeader("BIMI-Selector", null);
        if (header == null)
            return null;

        header = MimeUtility.unfold(header);
        header = header.toLowerCase(Locale.ROOT);

        int s = header.indexOf("s=");
        if (s < 0)
            return null;

        int e = header.indexOf(';', s + 2);
        if (e < 0)
            e = header.length();

        String selector = header.substring(s + 2, e).trim();
        if (TextUtils.isEmpty(selector))
            return null;

        Log.i("BIMI selector=" + selector);
        return selector;
    }

    String[] getAuthentication() throws MessagingException {
        ensureHeaders();

        List<String> all = new ArrayList<>();

        // https://datatracker.ietf.org/doc/html/rfc8601
        String[] results = imessage.getHeader(AUTHENTICATION_RESULTS);
        if (results != null)
            all.addAll(Arrays.asList(results));

        String[] aresults = imessage.getHeader(ARC_AUTHENTICATION_RESULTS);
        if (aresults != null)
            all.addAll(Arrays.asList(aresults));

        if (all.size() == 0)
            return null;

        String[] headers = new String[all.size()];
        for (int i = 0; i < all.size(); i++)
            headers[i] = MimeUtility.unfold(all.get(i));

        return headers;
    }

    static Boolean getAuthentication(String type, String[] headers) {
        // https://tools.ietf.org/html/rfc7601

        if (headers == null || headers.length == 0)
            return null;

        String signer = null;
        for (String header : headers) {
            String v = getKeyValues(header).get(type);
            if (v == null)
                continue;

            if (signer == null)
                signer = getSigner(header);
            else {
                String signer2 = getSigner(header);
                if (!signer.equals(signer2)) {
                    Log.i("Different signer=" + signer + "/" + signer2);
                    break;
                }
            }

            String[] val = v.split("[^A-za-z]+");
            if (val.length == 0)
                continue;

            String value = val[0].toLowerCase(Locale.ROOT);
            switch (value) {
                case "none":
                    return null;
                case "pass":
                    return true;
                case "fail":
                case "policy":
                    return false;
                case "neutral":
                    return null;
                case "temperror":
                    return null;
                case "permerror":
                    return false;
                default: // Yahoo: unknown
                    return null;
            }
        }

        return null;
    }

    Address[] getMailFrom(String[] headers) {
        if (headers == null || headers.length == 0)
            return null;

        Address[] mailfrom = null;
        String spf = getKeyValues(headers[0]).get("spf");
        if (spf == null)
            return null;

        int i = spf.indexOf(SMTP_MAILFORM + "=");
        if (i < 0)
            return null;

        String v = spf.substring(i + SMTP_MAILFORM.length() + 1);
        int s = v.indexOf(' ');
        if (s > 0)
            v = v.substring(0, s);

        if (v.startsWith("\"") && v.endsWith("\""))
            v = v.substring(1, v.length() - 1);

        try {
            mailfrom = InternetAddress.parseHeader(v, false);
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return mailfrom;
    }

    static String getSigner(String[] headers) {
        if (headers == null || headers.length == 0)
            return null;
        return getSigner(headers[0]);
    }

    static String getSigner(String header) {
        if (TextUtils.isEmpty(header))
            return null;

        int semi = header.indexOf(';');
        if (semi < 0)
            return null;
        String signer = header.substring(0, semi).trim();

        if (signer.toLowerCase(Locale.ROOT).startsWith("i=")) {
            int semi2 = header.indexOf(';', semi + 1);
            if (semi2 < 0)
                return null;
            signer = header.substring(semi + 1, semi2).trim();
        }

        if (TextUtils.isEmpty(signer))
            signer = null;

        return signer;
    }

    Boolean getSPF() throws MessagingException {
        ensureHeaders();

        // http://www.open-spf.org/RFC_4408/#header-field
        String[] headers = imessage.getHeader("Received-SPF");
        if (headers == null || headers.length < 1)
            return null;

        // header-field = "Received-SPF:" [CFWS] result FWS [comment FWS] [ key-value-list ] CRLF
        // result = "Pass" / "Fail" / "SoftFail" / "Neutral" / "None" / "TempError" / "PermError"

        String spf = MimeUtility.unfold(headers[0]);
        String[] values = spf.trim().split("[^A-Za-z]+");
        if (values.length == 0)
            return null;

        String value = values[0].toLowerCase(Locale.ROOT);
        switch (value) {
            case "pass":
                return true;
            case "fail":
                return false;
            case "softfail":
            case "neutral":
            case "none":
            case "temperror":
                return null;
            case "permerror":
                return false;
            default:
                return null;
        }
    }

    @NonNull
    List<String> verifyDKIM(Context context) {
        List<String> signers = new ArrayList<>();

        try {
            // Workaround reformatted headers (Content-Type)
            // This will do a BODY.PEEK[] to fetch the headers and message body
            MimeMessage amessage = imessage;
            if (imessage instanceof ReadableMime) {
                Properties props = MessageHelper.getSessionProperties(true);
                Session isession = Session.getInstance(props, null);
                amessage = new MimeMessage(isession, ((ReadableMime) imessage).getMimeStream());
            }

            // https://datatracker.ietf.org/doc/html/rfc6376/
            List<Pair<String, String[]>> list = new ArrayList<>();
            list.add(new Pair<>(DKIM_SIGNATURE, amessage.getHeader(DKIM_SIGNATURE)));
            list.add(new Pair<>(ARC_MESSAGE_SIGNATURE, amessage.getHeader(ARC_MESSAGE_SIGNATURE)));
            list.add(new Pair<>(DKIM_SIGNATURE, amessage.getHeader(GOOGLE_DKIM_SIGNATURE)));

            boolean found = false;
            for (Pair<String, String[]> entry : list)
                if (entry.second != null)
                    for (String header : entry.second) {
                        found = true;
                        String signer = verifySignatureHeader(context, header, entry.first, amessage);
                        if (signer != null && !signers.contains(signer))
                            signers.add(signer);
                    }

            if (!found)
                return signers;

            Log.i("DKIM signers=" + TextUtils.join(",", signers));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean native_arc = prefs.getBoolean("native_arc", true);
            String native_arc_whitelist = prefs.getString("native_arc_whitelist", null);
            List<String> whitelist = (TextUtils.isEmpty(native_arc_whitelist)
                    ? ARC_WHITELIST_DEFAULT
                    : Arrays.asList(native_arc_whitelist.split(",")));
            if (signers.size() == 0 && native_arc && whitelist.size() > 0) {
                // https://datatracker.ietf.org/doc/html/rfc8617#section-5.2
                boolean ok = true; // Until it is not
                Map<Integer, String> as = new HashMap<>();
                Map<Integer, String> aar = new HashMap<>();
                Map<Integer, String> ams = new HashMap<>();

                // 1. Collect all ARC Sets currently attached to the message
                for (String n : new String[]{ARC_SEAL, ARC_AUTHENTICATION_RESULTS, ARC_MESSAGE_SIGNATURE}) {
                    Map<Integer, String> map;
                    if (ARC_SEAL.equals(n))
                        map = as;
                    else if (ARC_AUTHENTICATION_RESULTS.equals(n))
                        map = aar;
                    else if (ARC_MESSAGE_SIGNATURE.equals(n))
                        map = ams;
                    else
                        throw new IllegalArgumentException(n);

                    String[] aheaders = amessage.getHeader(n);
                    if (aheaders != null) {
                        for (String header : aheaders) {
                            Map<String, String> kv = getKeyValues(MimeUtility.unfold(header));
                            Integer i = Helper.parseInt(kv.get("i"));
                            if (i == null || map.containsKey(i)) {
                                // 3.A. Each ARC Set MUST contain exactly one each of the three ARC header fields
                                Log.i("ARC duplicate " + n + "@" + i);
                                ok = false;
                                break;
                            }
                            map.put(i, header);
                        }
                    }

                    if (!ok)
                        break;
                }

                if (ok)
                    ok = (as.size() > 0 && as.size() <= 50 &&
                            as.size() == aar.size() && as.size() == ams.size());

                if (ok)
                    for (int i = 1; i <= as.size(); i++) {
                        // 3.B. The instance values of the ARC Sets MUST form a continuous sequence from 1..N with no gaps or repetition
                        if (!as.containsKey(i) || !aar.containsKey(i) || !ams.containsKey(i)) {
                            ok = false;
                            break;
                        }
                        // 2. If the Chain Validation Status of the highest instance value ARC Set is "fail",
                        //    then the Chain Validation Status is "fail"
                        // 3.C. The "cv" value for all ARC-Seal header fields MUST NOT be "fail".
                        //      For ARC Sets with instance values > 1, the values MUST be "pass".
                        //      For the ARC Set with instance value = 1, the value MUST be "none".
                        Map<String, String> kv = getKeyValues(MimeUtility.unfold(as.get(i)));
                        String cv = kv.get("cv");
                        if (!(i == 1 ? "none" : "pass").equalsIgnoreCase(cv)) {
                            Log.i("ARC cv#" + i + "=" + cv);
                            ok = false;
                            break;
                        }
                    }
                Log.i("ARC as=" + as.size() + " aar=" + aar.size() + " ams=" + ams.size() + " ok=" + ok);

                // 4. Validate the AMS with the greatest instance value (most recent).
                //    If validation fails, then the Chain Validation Status is "fail", and the algorithm stops here.
                if (ok) {
                    String arc = ams.get(ams.size());
                    String signer = verifySignatureHeader(context, arc, ARC_MESSAGE_SIGNATURE, amessage);
                    if (signer != null && !signers.contains(signer)) {
                        boolean whitelisted = (whitelist.contains(signer) ||
                                "*".equals(native_arc_whitelist));
                        Log.i("ARC signer=" + signer + " whitelisted=" + whitelisted);
                        if (whitelisted)
                            signers.add(signer);
                    }
                }
            }
        } catch (Throwable ex) {
            Log.e("DKIM", ex);
            EntityLog.log(context, EntityLog.Type.Debug3, "DKIM failed" +
                    " ex=" + Log.formatThrowable(ex));
        }

        return signers;
    }

    private String verifySignatureHeader(Context context, String header, String name, MimeMessage amessage) {
        Map<String, String> kv = getKeyValues(MimeUtility.unfold(header));

        String a = kv.get("a");
        String halgo;
        String salgo;
        if ("rsa-sha1".equals(a)) {
            halgo = "SHA-1";
            salgo = "SHA1withRSA";
        } else if ("rsa-sha256".equals(a)) {
            halgo = "SHA-256";
            salgo = "SHA256withRSA";
        } else if ("ed25519-sha256".equals(a)) {
            halgo = "SHA-256";
            salgo = "Ed25519";
        } else {
            Log.i("DKIM a=" + a);
            return null;
        }

        try {
            // https://serverfault.com/questions/591655/what-domain-name-should-appear-in-a-dkim-signature
            String signer = kv.get("d");

            String dns = kv.get("s") + "._domainkey." + signer;
            Log.i("DKIM lookup " + dns);
            DnsHelper.DnsRecord[] records = DnsHelper.lookup(context, dns, "txt");
            if (records.length == 0)
                return null;

            Log.i("DKIM got " + records[0].response);
            Map<String, String> dk = getKeyValues(records[0].response);
            // DKIM version and key type are not always present
            //  v=DKIM1; k=rsa; p=...
            //  v=DKIM1; k=ed25519; p=...

            String note = dk.get("n");
            if (!TextUtils.isEmpty(note))
                Log.i("DKIM note=" + note);

            // https://datatracker.ietf.org/doc/html/rfc6376#section-3.5
            Integer t = Helper.parseInt(kv.get("t")); // Works until 2038
            if (t != null)
                Log.i("DKIM timestamp=" + new Date(t * 1000L));
            Integer x = Helper.parseInt(kv.get("x"));
            if (x != null)
                Log.i("DKIM expiry=" + new Date(x * 1000L));

            String canonic = kv.get("c");
            Log.i("DKIM canonicalization=" + canonic);
            if (canonic == null)
                canonic = "simple/simple";
            String[] c = canonic.split("/");

            StringBuilder head = new StringBuilder();

            String hs = kv.get("h");
            Log.i("DKIM headers=" + hs);

            boolean from = false;
            List<String> keys = new ArrayList<>();
            if (hs != null)
                for (String key : hs.split(":")) {
                    keys.add(key.trim());
                    from = (from || "from".equalsIgnoreCase(key.trim()));
                }
            if (!from)
                throw new IllegalArgumentException("from missing: " + hs);

            keys.add(name);

            Map<String, Integer> index = new Hashtable<>();
            for (String key : keys) {
                // https://datatracker.ietf.org/doc/html/rfc6376/#section-5.4.2
                String _key = key.toLowerCase(Locale.ROOT);
                Integer idx = index.get(_key);
                idx = (idx == null ? 1 : idx + 1);
                index.put(_key, idx);

                String[] values = (name.equals(key)
                        ? new String[]{header}
                        : amessage.getHeader(key));

                if (!"google.com".equalsIgnoreCase(signer) &&
                        "Message-ID".equalsIgnoreCase(key) &&
                        amessage.getHeader(HEADER_GOOGLE_ORIGINAL_MESSAGE_ID, null) != null)
                    values = amessage.getHeader(HEADER_GOOGLE_ORIGINAL_MESSAGE_ID);

                if (values == null || idx > values.length) {
                    // https://datatracker.ietf.org/doc/html/rfc6376/#section-5.4
                    Log.i("DKIM missing header=" +
                            key + "[" + idx + "/" + (values == null ? null : values.length) + "]");
                    continue;
                }

                String value = values[values.length - idx];
                if (name.equals(key)) {
                    int b = value.lastIndexOf("b=");
                    int s = value.indexOf(";", b + 2);
                    value = value.substring(0, b + 2) + (s < 0 ? "" : value.substring(s));
                } else
                    Log.i("DKIM " + key + "=" + value.replaceAll("\\r?\\n", "|"));

                if ("simple".equals(c[0])) {
                    if (name.equals(key))
                        head.append(key).append(": ").append(value);
                    else {
                        // Find original header/name (case sensitive)
                        int _idx = values.length - idx;
                        Enumeration<Header> oheaders = amessage.getAllHeaders();
                        while (oheaders.hasMoreElements()) {
                            Header oheader = oheaders.nextElement();
                            if (key.equalsIgnoreCase(oheader.getName())) {
                                if (_idx-- == 0) {
                                    head.append(oheader.getName()).append(": ")
                                            .append(oheader.getValue());
                                    break;
                                }
                            }
                        }
                    }
                } else if ("relaxed".equals(c[0])) {
                    value = MimeUtility.unfold(value);
                    head.append(_key).append(':')
                            .append(value.replaceAll("\\s+", " ").trim());
                } else
                    throw new IllegalArgumentException(c[0]);

                if (!name.equals(key))
                    head.append("\r\n");
            }
            Log.i("DKIM head=" + head.toString().replace("\r\n", "|"));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Helper.copy(amessage.getRawInputStream(), bos);
            String body = bos.toString(); // TODO: charset?
            if ("simple".equals(c[c.length > 1 ? 1 : 0])) {
                if (TextUtils.isEmpty(body))
                    body = "\r\n";
                else if (!body.endsWith("\r\n"))
                    body += "\r\n";
                else {
                    while (body.endsWith("\r\n\r\n"))
                        body = body.substring(0, body.length() - 2);
                }
            } else if ("relaxed".equals(c[c.length > 1 ? 1 : 0])) {
                if (TextUtils.isEmpty(body))
                    body = "";
                else {
                    body = body.replaceAll("[ \\t]+\r\n", "\r\n");
                    body = body.replaceAll("[ \\t]+", " ");
                    while (body.endsWith("\r\n\r\n"))
                        body = body.substring(0, body.length() - 2);
                    if ("\r\n".equals(body))
                        body = "";
                }
            } else
                throw new IllegalArgumentException(c[1]);

            String length = kv.get("l");
            if (!TextUtils.isEmpty(length))
                throw new IllegalArgumentException("Length l=" + length + " body=" + body.length());

            Log.i("DKIM body=" + body.replace("\r\n", "|"));

            byte[] bh = MessageDigest.getInstance(halgo).digest(body.getBytes());  // TODO: charset?
            Log.i("DKIM bh=" + Base64.encodeToString(bh, Base64.NO_WRAP) + "/" + kv.get("bh"));

            String pubkey = dk.get("p");
            if (pubkey == null)
                return null;
            if ("".equals(pubkey)) {
                Log.i("DKIM key revoked");
                return null;
            }

            String p = pubkey.replaceAll("\\s+", "");
            Log.i("DKIM pubkey=" + p);

            String hash = kv.get("b");
            if (hash == null)
                return null;
            String s = hash.replaceAll("\\s+", "");
            Log.i("DKIM signature=" + s);

            byte[] data = head.toString().getBytes();
            byte[] key = Base64.decode(p, Base64.DEFAULT);
            byte[] signature = Base64.decode(s, Base64.DEFAULT);

            // https://datatracker.ietf.org/doc/html/rfc8463
            if ("Ed25519".equals(salgo)) {
                if (false) {
                    // https://www.rfc-editor.org/rfc/rfc8037#page-9
                    data = "eyJhbGciOiJFZERTQSJ9.RXhhbXBsZSBvZiBFZDI1NTE5IHNpZ25pbmc".getBytes(StandardCharsets.UTF_8);
                    key = Base64.decode("11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo", Base64.URL_SAFE);
                    signature = Base64.decode("hgyY0il_MGCjP0JzlnLWG1PPOt7-09PGcvMg3AIbQR6dWbhijcNR4ki4iylGjg5BhVsPt9g7sVvpAr_MuM0KAg", Base64.URL_SAFE);
                }
                data = MessageDigest.getInstance("SHA-256").digest(head.toString().getBytes());
                key = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), key).getEncoded();
            }

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519".equals(salgo) ? "Ed25519" : "RSA");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
            Signature sig = Signature.getInstance("Ed25519".equals(salgo) ? "EdDSA" : salgo); // a=

            // https://stackoverflow.com/a/43984402/1794097
            if (pubKey instanceof RSAPublicKey)
                try {
                    BigInteger modulus = ((RSAPublicKey) pubKey).getModulus();
                    int keylen = modulus.bitLength();
                    Log.i("DKIM RSA pubkey length=" + keylen);
                    if (keylen < DKIM_MIN_KEY_LENGTH) {
                        EntityLog.log(context, EntityLog.Type.Debug5, "DKIM RSA pubkey length=" + keylen);
                        throw new IllegalArgumentException("DKIM RSA pubkey length " + keylen + " < " + DKIM_MIN_KEY_LENGTH);
                    }

                    // https://github.com/badkeys/badkeys
                    if (BuildConfig.DEBUG)
                        for (int prime = 3; prime <= 65537; prime += 2)
                            if (isPrime(prime) &&
                                    modulus.remainder(BigInteger.valueOf(prime)).compareTo(BigInteger.ZERO) == 0) {
                                EntityLog.log(context, EntityLog.Type.Debug5, "DKIM RSA pubkey with small prime=" + prime);
                                throw new IllegalArgumentException("DKIM RSA pubkey with small prime=" + prime);
                            }
                    Log.i("DKIM RSA okay");
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            else if (pubKey instanceof EdDSAPublicKey) {
                Log.i("DKIM EdDSA pubkey");
            } else
                Log.i("DKIM key class=" + pubKey.getClass());

            sig.initVerify(pubKey);
            sig.update(data);

            boolean verified = sig.verify(signature);
            String msg = "DKIM valid=" + verified +
                    " header=" + name +
                    " algo=" + salgo +
                    " dns=" + dns +
                    " from=" + formatAddresses(getFrom());
            Log.i(msg);
            EntityLog.log(context, verified ? EntityLog.Type.Debug2 : EntityLog.Type.Debug3, msg);

            if (verified)
                return signer;
        } catch (Throwable ex) {
            Log.e("DKIM", ex);
            Address[] from;
            try {
                from = getFrom();
            } catch (Throwable ignored) {
                from = null;
            }
            EntityLog.log(context, EntityLog.Type.Debug3, "DKIM failed" +
                    " from=" + formatAddresses(from) +
                    " ex=" + Log.formatThrowable(ex));
        }

        return null;
    }

    static boolean isPrime(int num) {
        for (int i = 2; i <= num / 2; i++)
            if ((num % i) == 0)
                return false;
        return true;
    }

    boolean isAligned(Context context, List<String> signers,
                      Address[] return_path, Address[] smtp_from, Address[] from,
                      Boolean spf) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean strict_alignment = prefs.getBoolean("strict_alignment", false);

        List<Address> envelop = new ArrayList<>();
        if (return_path != null)
            envelop.addAll(Arrays.asList(return_path));
        if (from != null)
            envelop.addAll(Arrays.asList(from));
        if (smtp_from != null && !strict_alignment)
            envelop.addAll(Arrays.asList(smtp_from));
        for (String signer : signers) {
            String sdomain = UriHelper.getRootDomain(context, signer);
            if (sdomain == null)
                continue;
            for (Address a : envelop) {
                String edomain = UriHelper.getEmailDomain(((InternetAddress) a).getAddress());
                if (sdomain.equalsIgnoreCase(UriHelper.getRootDomain(context, edomain)))
                    return true;
            }
        }

        if (Boolean.TRUE.equals(spf) && !strict_alignment)
            return true;

        return false;
    }

    private String fixEncoding(String name, String header) {
        if (header.trim().startsWith("=?"))
            return header;

        Charset detected = CharsetHelper.detect(header, StandardCharsets.ISO_8859_1);
        if (detected == null && CharsetHelper.isUTF8(header))
            detected = StandardCharsets.UTF_8;
        if (detected == null ||
                CHARSET16.contains(detected) ||
                StandardCharsets.US_ASCII.equals(detected) ||
                StandardCharsets.ISO_8859_1.equals(detected))
            return header;

        Log.i("Converting " + name + " to " + detected);
        return new String(header.getBytes(StandardCharsets.ISO_8859_1), detected);
    }

    private Address[] getAddressHeader(String name) throws MessagingException {
        ensureHeaders();

        String header = imessage.getHeader(name, ",");
        if (header == null)
            return null;

        header = fixEncoding(name, header);
        header = header.replaceAll("\\?=[\\r\\n\\t ]+=\\?", "\\?==\\?");
        Address[] addresses = InternetAddress.parseHeader(header, false);

        List<Address> result = new ArrayList<>();
        for (Address address : addresses) {
            InternetAddress iaddress = (InternetAddress) address;
            String email = iaddress.getAddress();
            String personal = iaddress.getPersonal();
            if (!TextUtils.isEmpty(personal))
                personal = personal.replace("\u00ad", BuildConfig.DEBUG ? "-" : ""); // soft hyphen

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(personal))
                continue;

            if (personal != null && personal.equals(email))
                try {
                    iaddress.setPersonal(null);
                    personal = null;
                } catch (UnsupportedEncodingException ex) {
                    Log.w(ex);
                }

            if (email != null) {
                email = decodeMime(email);
                email = fromPunyCode(email);
                email = toPunyCode(email, true);

                iaddress.setAddress(email);
            }

            if (personal != null) {
                try {
                    iaddress.setPersonal(decodeMime(personal));
                } catch (UnsupportedEncodingException ex) {
                    Log.w(ex);
                }
            }

            result.add(address);
        }

        return (result.size() == 0 ? null : result.toArray(new Address[0]));
    }

    Address[] getReturnPath() throws MessagingException {
        Address[] addresses = getAddressHeader("Return-Path");
        if (addresses == null)
            return null;

        List<Address> result = new ArrayList<>();
        for (int i = 0; i < addresses.length; i++) {
            boolean duplicate = false;
            for (int j = 0; j < i; j++)
                if (addresses[i].equals(addresses[j])) {
                    duplicate = true;
                    break;
                }
            if (!duplicate)
                result.add(addresses[i]);
        }

        return result.toArray(new Address[0]);
    }

    Address[] getSubmitter() throws MessagingException {
        Address[] sender = getAddressHeader("X-Google-Original-From");
        if (sender == null)
            sender = getAddressHeader("Duck-Original-From");
        if (sender == null)
            sender = getAddressHeader("X-SimpleLogin-Original-From");
        if (sender == null)
            sender = getAddressHeader("X-AnonAddy-Original-From-Header");
        if (sender == null)
            sender = getAddressHeader("Sender");
        if (sender == null) {
            Address[] from = getAddressHeader("From");
            if (from != null && from.length == 1) {
                String email = ((InternetAddress) from[0]).getAddress();
                if (email != null && email.endsWith(".mozmail.com"))
                    sender = getAddressHeader("Resent-From");
            }
        }
        return sender;
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
        ensureHeaders();

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
        ensureHeaders();

        try {
            // https://www.ietf.org/rfc/rfc2369.txt
            String list = imessage.getHeader("List-Unsubscribe", null);
            if (list == null)
                return null;

            list = MimeUtility.unfold(list);
            list = decodeMime(list);

            if (list == null || list.startsWith("NO"))
                return null;

            // https://datatracker.ietf.org/doc/html/rfc8058
            boolean oneclick = false;
            String post = imessage.getHeader("List-Unsubscribe-Post", null);
            if (post != null) {
                post = MimeUtility.unfold(post);
                post = decodeMime(post);
                oneclick = "List-Unsubscribe=One-Click".equalsIgnoreCase(post.trim());
            }

            String link = null;
            String mailto = null;
            int s = list.indexOf('<');
            int e = list.indexOf('>', s + 1);
            while (s >= 0 && e > s) {
                String unsubscribe = list.substring(s + 1, e).trim();
                if (TextUtils.isEmpty(unsubscribe))
                    ; // Empty address
                else if (unsubscribe.toLowerCase(Locale.ROOT).startsWith("mailto:")) {
                    if (mailto == null) {
                        try {
                            unsubscribe = "mailto:" + unsubscribe.substring("mailto:".length());
                            MailTo.parse(unsubscribe);
                            mailto = unsubscribe;
                        } catch (Throwable ex) {
                            Log.i(new Throwable(unsubscribe, ex));
                        }
                    }
                } else if (Helper.EMAIL_ADDRESS.matcher(unsubscribe).matches())
                    mailto = "mailto:" + unsubscribe;
                else {
                    if (link == null) {
                        Uri uri = Uri.parse(unsubscribe);
                        if (UriHelper.isHyperLink(uri))
                            link = unsubscribe;
                        else {
                            Pattern p =
                                    Pattern.compile(PatternsCompat.AUTOLINK_WEB_URL.pattern() + "|" +
                                            PatternsCompat.AUTOLINK_EMAIL_ADDRESS.pattern());
                            Matcher m = p.matcher(unsubscribe);
                            if (m.find())
                                link = unsubscribe.substring(m.start(), m.end());
                            else
                                Log.i(new Throwable(unsubscribe));
                        }
                    }
                }

                s = list.indexOf('<', e + 1);
                e = list.indexOf('>', s + 1);
            }

            if (true || link != null && !link.startsWith("https://"))
                oneclick = false;

            if (link != null)
                return (oneclick ? ONE_CLICK_UNSUBSCRIBE : "") + link;
            if (mailto != null)
                return mailto;

            if (!BuildConfig.PLAY_STORE_RELEASE)
                Log.i(new IllegalArgumentException("List-Unsubscribe: " + list));
            return null;
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    String getAutocrypt() throws MessagingException {
        ensureHeaders();

        String autocrypt = imessage.getHeader("Autocrypt", null);
        if (autocrypt == null)
            return null;

        return MimeUtility.unfold(autocrypt);
    }

    String getSubject() throws MessagingException {
        ensureHeaders();

        String subject = imessage.getHeader("Subject", null);
        if (subject == null)
            return null;

        subject = fixEncoding("subject", subject);
        subject = subject.replaceAll("\\?=[\\r\\n\\t ]+=\\?", "\\?==\\?");
        subject = MimeUtility.unfold(subject);
        subject = decodeMime(subject);

        return subject
                .trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace("\u00ad", BuildConfig.DEBUG ? "-" : "");  // soft hyphen
    }

    Long getSize() throws MessagingException {
        ensureEnvelope();

        long size = imessage.getSize();
        return (size < 0 ? null : size);
    }

    boolean isModified() throws MessagingException {
        ensureHeaders();
        return (imessage.getHeader(HEADER_MODIFIED_TIME) != null);
    }

    Long getReceived() throws MessagingException {
        ensureEnvelope();

        Date received = imessage.getReceivedDate();
        if (received == null)
            return null;

        return received.getTime();
    }

    Long getReceivedHeader() throws MessagingException {
        return getReceivedHeader(null);
    }

    long getPOP3Received() throws MessagingException {
        Long received = getReceivedHeader(getResent());
        if (received == null)
            received = getSent();
        if (received == null)
            received = 0L;
        return received;
    }

    private Long getReceivedHeader(Long before) throws MessagingException {
        ensureHeaders();

        // https://tools.ietf.org/html/rfc5321#section-4.4
        // https://tools.ietf.org/html/rfc5322#section-3.6.7
        String[] received = imessage.getHeader("Received");
        if (received == null || received.length == 0)
            return null;

        // First header is last added header
        for (int i = 0; i < received.length; i++) {
            String header = MimeUtility.unfold(received[i]);
            int semi = header.lastIndexOf(';');
            if (semi < 0)
                return null;

            MailDateFormat mdf = new MailDateFormat();
            Date date = mdf.parse(header, new ParsePosition(semi + 1));
            if (date == null)
                return null;

            long time = date.getTime();
            if (before == null || time < before)
                return time;
        }

        return null;
    }

    Boolean getTLS() throws MessagingException {
        try {
            Boolean tls = _getTLS();
            Log.i("--- TLS=" + tls);
            return tls;
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    private Boolean _getTLS() throws MessagingException {
        // https://datatracker.ietf.org/doc/html/rfc2821#section-4.4

        // Time-stamp-line = "Received:" FWS Stamp <CRLF>
        // Stamp = From-domain By-domain Opt-info ";"  FWS date-time
        // From-domain = "FROM" FWS Extended-Domain CFWS
        // By-domain = "BY" FWS Extended-Domain CFWS
        // Opt-info = [Via] [With] [ID] [For]
        // Via = "VIA" FWS Link CFWS
        // With = "WITH" FWS Protocol CFWS
        // ID = "ID" FWS String / msg-id CFWS
        // For = "FOR" FWS 1*( Path / Mailbox ) CFWS

        // Extended-Domain = Domain / ( Domain FWS "(" TCP-info ")" ) / ( Address-literal FWS "(" TCP-info ")" )
        // TCP-info = Address-literal / ( Domain FWS Address-literal )
        // Link = "TCP" / Addtl-Link
        // Addtl-Link = Atom
        // Protocol = "ESMTP" / "SMTP" / Attdl-Protocol
        // Attdl-Protocol = Atom

        ensureHeaders();

        String[] received = imessage.getHeader("Received");
        if (received == null || received.length == 0)
            return null;

        // First header is last added header
        Log.i("=======");
        for (int i = 0; i < received.length; i++) {
            String header = MimeUtility.unfold(received[i]);
            Log.i("--- header=" + header);
            Boolean tls = isTLS(header, i == received.length - 1);
            if (!Boolean.TRUE.equals(tls))
                return tls;
        }

        return true;
    }

    static Boolean isTLS(String header, boolean first) {
        // Strip date
        int semi = header.lastIndexOf(';');
        if (semi > 0)
            header = header.substring(0, semi);

        String h = header.toLowerCase(Locale.ROOT);
        if (h.contains("using tls") ||
                h.contains("via http") ||
                h.contains("version=tls")) {
            Log.i("--- found TLS");
            return true;
        }

        // (qmail nnn invoked by uid nnn); 1 Jan 2022 00:00:00 -0000
        // Postfix: by <host name> (<name>, from userid nnn)
        if (header.matches(".*\\(qmail \\d+ invoked by uid \\d+\\).*") ||
                header.matches(".*\\(nullmailer pid \\d+ invoked by uid \\d+\\).*") ||
                header.matches(".*\\(.*, from userid \\d+\\).*")) {
            Log.i("--- phrase");
            return true;
        }

        // Get key/values
        String[] parts = header.split("\\s+");
        Map<String, StringBuilder> kv = new HashMap<>();
        String key = null;
        for (int p = 0; p < parts.length; p++) {
            String k = parts[p].toLowerCase(Locale.ROOT);
            if (RECEIVED_WORDS.contains(k)) {
                key = k;
                if (!kv.containsKey(key))
                    kv.put(key, new StringBuilder());
            } else if (key != null) {
                StringBuilder sb = kv.get(key);
                if (sb.length() > 0)
                    sb.append(' ');
                sb.append(parts[p]);
            }
        }

        // Dump
        for (String k : kv.keySet())
            Log.i("--- " + k + "=" + kv.get(k));

        // Check if 'by' local address
        if (kv.containsKey("by")) {
            String by = kv.get("by").toString();
            if (by.matches(".*\\.google\\.com")) {
                Log.i("--- local by Google");
                return true;
            }
            if (by.toLowerCase(Locale.ROOT).contains("sendmail")) {
                Log.i("--- local by sendmail");
                return true;
            }
            if (by.startsWith("filterdrecv-")) {
                Log.i("--- local by filterdrecv");
                return true;
            }
            if (isLocal(by)) {
                Log.i("--- local by=" + by);
                return true;
            }
        }

        // Check if 'from' local address
        if (kv.containsKey("from")) {
            String from = kv.get("from").toString();
            if (isLocal(from)) {
                Log.i("--- local from=" + from);
                return true;
            }
        }

        // Check Microsoft front end transport (proxy)
        // https://social.technet.microsoft.com/wiki/contents/articles/50370.exchange-2016-what-is-the-front-end-transport-service-on-the-mailbox-role.aspx
        if (kv.containsKey("via") && false) {
            String via = kv.get("via").toString();
            if ("Frontend Transport".equals(via)) {
                Log.i("--- frontend via=" + via);
                return true;
            }
        }

        // Check protocol
        if (!kv.containsKey("with")) {
            Log.i("--- with missing");
            return null;
        }

        // https://datatracker.ietf.org/doc/html/rfc3848
        // https://www.iana.org/assignments/mail-parameters/mail-parameters.txt
        String with = kv.get("with").toString();
        int w = with.indexOf(' ');
        String protocol = (w < 0 ? with : with.substring(0, w)).toLowerCase(Locale.ROOT);

        if (with.contains("TLS")) {
            Log.i("--- with TLS");
            return true;
        }

        if ("local".equals(protocol)) {
            // Exim
            Log.i("--- local with=" + with);
            return true;
        }

        if (protocol.startsWith("lmtp")) {
            // https://en.wikipedia.org/wiki/Local_Mail_Transfer_Protocol
            Log.i("--- lmtp with=" + with);
            return true;
        }

        if ("mapi".equals(protocol)) {
            // https://en.wikipedia.org/wiki/MAPI
            Log.i("--- mapi with=" + with);
            return true;
        }

        if ("http".equals(protocol) ||
                "https".equals(protocol) ||
                "httprest".equals(protocol)) {
            // https: Outlook
            // httprest: by gmailapi.google.com
            Log.i("--- http with=" + with);
            return true;
        }

        if (!protocol.contains("mtp")) {
            Log.i("--- unknown with=" + with);
            return null;
        }

        if (protocol.contains("mtps")) {
            Log.i("--- insecure with=" + with);
            return true;
        }

        return false;
    }

    private static boolean isLocal(String value) {
        if (value.contains("exim") || // with sa-scanned / with dspam-scanned
                value.contains("localhost") ||
                value.contains("127.0.0.1") ||
                value.contains("[::1]"))
            return true;

        int s = value.indexOf('[');
        int e = value.indexOf(']', s + 1);
        if (s >= 0 && e > 0) {
            String ip = value.substring(s + 1, e);
            if (ip.toLowerCase(Locale.ROOT).startsWith("ipv6:"))
                ip = ip.substring(5);
            if (ConnectionHelper.isNumericAddress(ip) &&
                    ConnectionHelper.isLocalAddress(ip))
                return true;
        }

        int f = value.indexOf(' ');
        String host = (f < 0 ? value : value.substring(0, f));
        if (ConnectionHelper.isNumericAddress(host)) {
            if (ConnectionHelper.isLocalAddress(host))
                return true;
        }

        return false;
    }

    Long getSent() throws MessagingException {
        ensureEnvelope();

        Date sent = imessage.getSentDate();
        if (sent == null)
            return null;

        return sent.getTime();
    }

    Long getResent() throws MessagingException {
        ensureHeaders();

        String resent = imessage.getHeader("Resent-Date", null);
        if (resent == null)
            return null;

        MailDateFormat mdf = new MailDateFormat();
        Date date = mdf.parse(resent, new ParsePosition(0));
        if (date == null)
            return null;

        return date.getTime();
    }

    String getHeaders() throws MessagingException {
        ensureHeaders();

        StringBuilder sb = new StringBuilder();
        Enumeration<Header> headers = imessage.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header header = headers.nextElement();
            sb.append(header.getName()).append(": ").append(header.getValue()).append('\n');
        }
        return sb.toString();
    }

    String getInfrastructure() throws MessagingException {
        ensureHeaders();

        String awsses = imessage.getHeader("X-SES-Outgoing", null);
        if (!TextUtils.isEmpty(awsses))
            return "awsses";

        String sendgrid = imessage.getHeader("X-SG-EID", null);
        if (!TextUtils.isEmpty(sendgrid))
            return "sendgrid";

        String mailgun = imessage.getHeader("X-Mailgun-Sid", null);
        if (!TextUtils.isEmpty(mailgun))
            return "mailgun";

        String mandrill = imessage.getHeader("X-Mandrill-User", null);
        if (!TextUtils.isEmpty(mandrill))
            return "mandrill";

        String mailchimp = imessage.getHeader("X-MC-User", null);
        if (!TextUtils.isEmpty(mailchimp))
            return "mailchimp";

        String postmark = imessage.getHeader("X-PM-Message-Id", null);
        if (!TextUtils.isEmpty(postmark))
            return "postmark";

        String salesforce = imessage.getHeader("X-SFDC-User", null);
        if (!TextUtils.isEmpty(salesforce))
            return "salesforce";

        String mailjet = imessage.getHeader("X-MJ-Mid", null);
        if (!TextUtils.isEmpty(mailjet))
            return "mailjet";

        String sendinblue = imessage.getHeader("X-sib-id", null);
        if (!TextUtils.isEmpty(sendinblue))
            return "sendinblue";

        String sparkpost = imessage.getHeader("X-MSFBL", null);
        if (!TextUtils.isEmpty(sparkpost))
            return "sparkpost";

        String netcore = imessage.getHeader("X-FNCID", null);
        if (!TextUtils.isEmpty(netcore))
            return "netcore";

        String elastic = imessage.getHeader("X-Msg-EID", null);
        if (!TextUtils.isEmpty(elastic))
            return "elastic";

        String zeptomail = imessage.getHeader("X-JID", null); // TM-MAIL-JID
        if (!TextUtils.isEmpty(zeptomail))
            return "zeptomail";

        String gmail = imessage.getHeader("X-Gm-Message-State", null);
        if (!TextUtils.isEmpty(gmail))
            return "gmail";

        String outlook = imessage.getHeader("x-ms-publictraffictype", null);
        if (!TextUtils.isEmpty(outlook))
            return "outlook";

        String yahoo = imessage.getHeader("X-Sonic-MF", null);
        if (!TextUtils.isEmpty(yahoo))
            return "yahoo";

        String icloud = imessage.getHeader("X-Proofpoint-Spam-Details", null);
        if (!TextUtils.isEmpty(icloud))
            return "icloud";

        //String zoho = imessage.getHeader("X-ZohoMailClient", null);
        //if (!TextUtils.isEmpty(zoho))
        //    return "zoho";

        String icontact = imessage.getHeader("X-SFMC-Stack", null);
        if (!TextUtils.isEmpty(icontact))
            return "icontact";

        String paypal = imessage.getHeader("X-Email-Type-Id", null);
        if (!TextUtils.isEmpty(paypal))
            return "paypal";

        String xmailer = imessage.getHeader("X-Mailer", null);
        if (!TextUtils.isEmpty(xmailer)) {
            //if (xmailer.contains("iPhone Mail"))
            //    return "icloud";
            if (xmailer.contains("PHPMailer"))
                return "phpmailer";
            //if (xmailer.contains("Zoho Mail"))
            //    return "zoho";
        }

        String return_path = imessage.getHeader("Return-Path", null);
        if (!TextUtils.isEmpty(return_path)) {
            if (return_path.contains("pdmailservice.com"))
                return "icontact";
            if (return_path.contains("flowmailer.com"))
                return "flowmailer";
        }

        return null;
    }

    String getHash() throws MessagingException {
        try {
            if (hash == null)
                hash = Helper.sha1(getHeaders().getBytes());
            return hash;
        } catch (NoSuchAlgorithmException ex) {
            Log.e(ex);
            return null;
        }
    }

    enum AddressFormat {NAME_ONLY, EMAIL_ONLY, NAME_EMAIL, EMAIL_NAME}

    static AddressFormat getAddressFormat(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean name_email = prefs.getBoolean("name_email", false);
        int email_format = prefs.getInt("email_format", name_email
                ? MessageHelper.AddressFormat.NAME_EMAIL.ordinal()
                : MessageHelper.AddressFormat.NAME_ONLY.ordinal());
        if (email_format < MessageHelper.AddressFormat.values().length)
            return MessageHelper.AddressFormat.values()[email_format];
        else
            return MessageHelper.AddressFormat.NAME_ONLY;
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
        return formatAddresses(addresses, full ? AddressFormat.NAME_EMAIL : AddressFormat.NAME_ONLY, compose);
    }

    static String formatAddresses(Address[] addresses, AddressFormat format, boolean compose) {
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
                String email = address.getAddress();
                String personal = address.getPersonal();

                if (TextUtils.isEmpty(personal) || format == AddressFormat.EMAIL_ONLY)
                    formatted.add(TextUtils.isEmpty(email) ? "<>" : email);
                else {
                    if (compose) {
                        boolean quote = false;
                        personal = personal.replace("\"", "");
                        for (int c = 0; c < personal.length(); c++)
                            // https://tools.ietf.org/html/rfc822
                            if ("()<>@,;:\\\".[]".indexOf(personal.charAt(c)) >= 0) {
                                quote = true;
                                break;
                            }
                        if (quote)
                            personal = "\"" + personal + "\"";
                    }

                    if (format == AddressFormat.NAME_EMAIL && !TextUtils.isEmpty(email))
                        formatted.add(personal + " <" + email + ">");
                    else if (format == AddressFormat.EMAIL_NAME && !TextUtils.isEmpty(email))
                        formatted.add("<" + email + "> " + personal);
                    else
                        formatted.add(personal);
                }
            } else
                formatted.add(addresses[i].toString());
        }
        return TextUtils.join(compose ? ", " : "; ", formatted);
    }

    static String fromPunyCode(String email) {
        try {
            int at = email.indexOf('@');
            if (at > 0) {
                String user = email.substring(0, at);
                String domain = email.substring(at + 1);

                try {
                    user = IDN.toUnicode(user, IDN.ALLOW_UNASSIGNED);
                } catch (Throwable ex) {
                    Log.i(ex);
                }

                String[] parts = domain.split("\\.");
                for (int p = 0; p < parts.length; p++)
                    try {
                        parts[p] = IDN.toUnicode(parts[p], IDN.ALLOW_UNASSIGNED);
                    } catch (Throwable ex) {
                        Log.i(ex);
                    }

                email = user + '@' + TextUtils.join(".", parts);
            }
        } catch (Throwable ex) {
            Log.i(ex);
        }

        return email;
    }

    static String toPunyCode(String email, boolean single) {
        int at = email.indexOf('@');
        if (at > 0) {
            String user = email.substring(0, at);
            String domain = email.substring(at + 1);

            if (single &&
                    TextHelper.isSingleScript(user) &&
                    TextHelper.isSingleScript(domain))
                return email;

            try {
                user = IDN.toASCII(user, IDN.ALLOW_UNASSIGNED);
            } catch (Throwable ex) {
                Log.i(ex);
            }

            String[] parts = domain.split("\\.");
            for (int p = 0; p < parts.length; p++)
                try {
                    parts[p] = IDN.toASCII(parts[p], IDN.ALLOW_UNASSIGNED);
                } catch (Throwable ex) {
                    Log.i(ex);
                }

            email = user + '@' + TextUtils.join(".", parts);
        }

        return email;
    }

    public static String decodeMime(String text) {
        if (text == null)
            return null;

        // https://tools.ietf.org/html/rfc2045
        // https://tools.ietf.org/html/rfc2047
        // encoded-word = "=?" charset "?" encoding "?" encoded-text "?="

        int s, q1, q2, e, i = 0;
        List<MimeTextPart> parts = new ArrayList<>();
        while (i < text.length()) {
            s = text.indexOf("=?", i);
            if (s < 0)
                break;

            q1 = text.indexOf("?", s + 2);
            if (q1 < 0)
                break;

            q2 = text.indexOf("?", q1 + 1);
            if (q2 < 0)
                break;

            e = text.indexOf("?=", q2 + 1);
            if (e < 0)
                break;

            String plain = text.substring(i, s);
            if (!TextUtils.isEmpty(plain))
                parts.add(new MimeTextPart(plain));

            parts.add(new MimeTextPart(
                    text.substring(s + 2, q1),
                    text.substring(q1 + 1, q2),
                    text.substring(q2 + 1, e)));

            i = e + 2;
        }

        if (i < text.length())
            parts.add(new MimeTextPart(text.substring(i)));

        // Fold words to not break encoding
        int p = 0;
        while (p + 1 < parts.size()) {
            MimeTextPart p1 = parts.get(p);
            MimeTextPart p2 = parts.get(p + 1);
            // https://bugzilla.mozilla.org/show_bug.cgi?id=1374149
            if (!("ISO-2022-JP".equalsIgnoreCase(p1.charset) && "B".equalsIgnoreCase(p1.encoding)) &&
                    p1.charset != null && p1.charset.equalsIgnoreCase(p2.charset) &&
                    p1.encoding != null && p1.encoding.equalsIgnoreCase(p2.encoding) &&
                    p1.text != null && !p1.text.endsWith("=")) {
                /*
                try {
                    byte[] b1 = decodeWord(p1.text, p1.encoding, p1.charset);
                    byte[] b2 = decodeWord(p2.text, p2.encoding, p2.charset);
                    if (CharsetHelper.isValid(b1, p1.charset) && CharsetHelper.isValid(b2, p2.charset)) {
                        p++;
                        continue;
                    }

                    byte[] b = new byte[b1.length + b2.length];
                    System.arraycopy(b1, 0, b, 0, b1.length);
                    System.arraycopy(b2, 0, b, b1.length, b2.length);
                    p1.text = new String(b, p1.charset);
                    p1.charset = null;
                    p2.encoding = null;
                    parts.remove(p + 1);
                    continue;
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                */
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

    static byte[] decodeWord(String word, String encoding, String charset) throws IOException {
        String e = encoding.trim();
        if (e.equalsIgnoreCase("B"))
            while (word.startsWith("="))
                word = word.substring(1);
        ByteArrayInputStream bis = new ByteArrayInputStream(ASCIIUtility.getBytes(word));

        InputStream is;
        if (e.equalsIgnoreCase("B"))
            is = new BASE64DecoderStream(bis);
        else if (e.equalsIgnoreCase("Q"))
            is = new QDecoderStreamEx(bis);
        else {
            Log.e(new UnsupportedEncodingException("Encoding=" + encoding));
            return word.getBytes(charset);
        }

        int count = bis.available();
        byte[] bytes = new byte[count];
        count = is.read(bytes, 0, count);

        return Arrays.copyOf(bytes, count);
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

            try {
                return decodeMime(new String(decodeWord(text, encoding, charset), charset));
            } catch (Throwable ex) {
                String word = "=?" + charset + "?" + encoding + "?" + text + "?=";
                Log.e(new IllegalArgumentException(word, ex));
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

    class PartHolder {
        Part part;
        ContentType contentType;
        String filename;

        PartHolder(Part part, ContentType contentType) {
            this.part = part;
            this.contentType = contentType;
        }

        PartHolder(Part part, ContentType contentType, String filename) {
            this.part = part;
            this.contentType = contentType;
            this.filename = filename;
        }

        boolean isPlainText() {
            return "text/plain".equalsIgnoreCase(contentType.getBaseType());
        }

        boolean isHtml() {
            return "text/html".equalsIgnoreCase(contentType.getBaseType());
        }

        boolean isMarkdown() {
            return "text/markdown".equalsIgnoreCase(contentType.getBaseType());
        }

        boolean isPatch() {
            String ext = Helper.getExtension(filename);
            return "diff".equalsIgnoreCase(ext) ||
                    "patch".equalsIgnoreCase(ext) ||
                    "text/x-diff".equalsIgnoreCase(contentType.getBaseType()) ||
                    "text/x-patch".equalsIgnoreCase(contentType.getBaseType());
        }

        boolean isReport() {
            String ct = contentType.getBaseType();
            return (Report.isDeliveryStatus(ct) ||
                    Report.isDispositionNotification(ct) ||
                    Report.isFeedbackReport(ct));
        }
    }

    class MessageParts {
        private String protected_subject;
        private List<PartHolder> text = new ArrayList<>();
        private List<PartHolder> extra = new ArrayList<>();
        private List<AttachmentPart> attachments = new ArrayList<>();
        private ArrayList<String> warnings = new ArrayList<>();

        String getProtectedSubject() {
            return protected_subject;
        }

        Integer isPlainOnly(boolean download_plain) {
            Integer plain = isPlainOnly();
            if (plain == null)
                return null;
            if (download_plain && plain == 0x80)
                plain |= 1;
            return plain;
        }

        Integer isPlainOnly() {
            int html = 0;
            int plain = 0;
            for (PartHolder h : text) {
                if (h.isHtml())
                    html++;
                if (h.isPlainText())
                    plain++;
            }

            if (html + plain == 0)
                return null;
            if (html == 0)
                return 1;
            return (plain > 0 ? 0x80 : 0);
        }

        boolean hasBody() throws MessagingException {
            List<PartHolder> all = new ArrayList<>();
            all.addAll(text);
            all.addAll(extra);

            for (PartHolder h : all)
                if (h.part.getSize() > 0)
                    return true;

            return false;
        }

        void normalize() {
            Integer plain = isPlainOnly();
            if (plain != null && (plain & 1) != 0)
                for (AttachmentPart apart : attachments)
                    if (!TextUtils.isEmpty(apart.attachment.cid) ||
                            !Part.ATTACHMENT.equals(apart.attachment.disposition)) {
                        Log.i("Normalizing " + apart.attachment);
                        apart.attachment.cid = null;
                        apart.attachment.related = false;
                        apart.attachment.disposition = Part.ATTACHMENT;
                    }
        }

        Long getBodySize() throws MessagingException {
            Long size = null;

            List<PartHolder> all = new ArrayList<>();
            all.addAll(text);
            for (PartHolder h : all) {
                int s = h.part.getSize();
                if (s >= 0)
                    if (size == null)
                        size = (long) s;
                    else
                        size += (long) s;
            }

            for (EntityAttachment attachment : getAttachments())
                if (attachment.size != null &&
                        (EntityAttachment.PGP_MESSAGE.equals(attachment.encryption) ||
                                EntityAttachment.SMIME_MESSAGE.equals(attachment.encryption) ||
                                EntityAttachment.SMIME_SIGNED_DATA.equals(attachment.encryption)))
                    if (size == null)
                        size = attachment.size;
                    else
                        size += attachment.size;

            return size;
        }

        String getHtml(Context context) throws MessagingException, IOException {
            return getHtml(context, false);
        }

        String getHtml(Context context, boolean plain_text) throws MessagingException, IOException {
            return getHtml(context, plain_text, null);
        }

        String getHtml(Context context, boolean plain_text, String override) throws MessagingException, IOException {
            if (text.size() + extra.size() == 0) {
                Log.i("No body part");
                return null;
            }

            StringBuilder sb = new StringBuilder();

            List<PartHolder> parts = new ArrayList<>();

            Integer plain = isPlainOnly();
            if (plain != null && (plain & 1) != 0)
                // Plain only
                parts.addAll(text);
            else {
                // Either plain and HTML or HTML only
                boolean hasPlain = (plain != null && (plain & 0x80) != 0);
                for (PartHolder h : text)
                    if (plain_text && hasPlain) {
                        if (h.isPlainText())
                            parts.add(h);
                    } else {
                        if (h.isHtml())
                            parts.add(h);
                    }
            }

            parts.addAll(extra);

            boolean first = true;
            for (PartHolder h : parts) {
/*
                int size = h.part.getSize();
                if (size > 100 * 1024 * 1024)
                    Log.e("Unreasonable message size=" + size);
                if (size > MAX_MESSAGE_SIZE && size != Integer.MAX_VALUE) {
                    warnings.add(context.getString(R.string.title_insufficient_memory, size));
                    return null;
                }
*/

                if (Boolean.parseBoolean(System.getProperty("fairemail.preamble"))) {
                    String preamble = h.contentType.getParameter("preamble");
                    if (Boolean.parseBoolean(preamble)) {
                        String text = ((MimeMultipart) h.part.getContent()).getPreamble();
                        String html = "<div class=\"faircode_remove\">" +
                                "<h1>Preamble</h1>" +
                                "<div x-plain=\"true\">" +
                                HtmlHelper.formatPlainText(text) +
                                "</div>" +
                                "</div>";
                        sb.append(html);
                        continue;
                    }
                }

                // Check character set
                String charset = h.contentType.getParameter("charset");
                if (UnknownCharsetProvider.charsetForMime(charset) == null)
                    warnings.add(context.getString(R.string.title_no_charset, charset));

                if (TextUtils.isEmpty(charset) ||
                        charset.equalsIgnoreCase(StandardCharsets.US_ASCII.name()))
                    charset = null;

                Charset cs = null;
                if (charset != null)
                    try {
                        cs = Charset.forName(charset);
                    } catch (UnsupportedCharsetException ignored) {
                        cs = null;
                    }

                String result;
                try {
                    Object content;

                    // Check for UTF-16 LE without BOM
                    if (StandardCharsets.UTF_16.equals(cs) && override == null) {
                        BufferedInputStream bis = new BufferedInputStream(h.part.getDataHandler().getInputStream());
                        if (Boolean.TRUE.equals(CharsetHelper.isUTF16LE(bis))) {
                            Log.e("Charset " + cs + " -> UTF16LE");
                            cs = StandardCharsets.UTF_16LE;
                        }
                        content = Helper.readStream(bis, cs);
                    } else
                        content = h.part.getContent();

                    Log.i("Content class=" + (content == null ? null : content.getClass().getName()));

                    if (content == null) {
                        warnings.add(context.getString(R.string.title_no_body));
                        return null;
                    }

                    if (content instanceof String)
                        result = (String) content;
                    else if (content instanceof InputStream) {
                        // java.io.ByteArrayInputStream
                        // Typically com.sun.mail.util.QPDecoderStream
                        if (BuildConfig.DEBUG && false)
                            warnings.add(content.getClass().getName());
                        result = Helper.readStream((InputStream) content,
                                cs == null ? StandardCharsets.ISO_8859_1 : cs);
                    } else {
                        result = null;

                        StringBuilder m = new StringBuilder();
                        if (content instanceof Multipart) {
                            m.append("multipart");
                            Multipart mp = (Multipart) content;
                            for (int i = 0; i < mp.getCount(); i++) {
                                BodyPart bp = mp.getBodyPart(i);
                                try {
                                    ContentType ct = new ContentType(bp.getContentType());
                                    if (h.contentType.match(ct)) {
                                        String _charset = ct.getParameter("charset");
                                        Charset _cs = (TextUtils.isEmpty(_charset)
                                                ? StandardCharsets.ISO_8859_1
                                                : Charset.forName(_charset));
                                        result = Helper.readStream(bp.getInputStream(), _cs);
                                    }
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }
                                m.append(" [").append(bp.getContentType()).append("]");
                            }
                        } else
                            m.append(content.getClass().getName());

                        String msg = "Expected " + h.contentType + " got " + m + " result=" + (result != null);

                        if (result == null) {
                            Log.e(msg);
                            warnings.add(msg);
                            result = Helper.readStream(h.part.getInputStream(),
                                    cs == null ? StandardCharsets.ISO_8859_1 : cs);
                        } else
                            Log.w(msg);
                    }
                } catch (DecodingException | UnsupportedEncodingException ex) {
                    Log.e(ex);
                    warnings.add(Log.formatThrowable(ex, false));
                    return null;
                } catch (IOException | FolderClosedException | MessageRemovedException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    Log.e(ex);
                    if (Log.isTestRelease())
                        warnings.add(ex + "\n" + android.util.Log.getStackTraceString(ex));
                    else
                        warnings.add(Log.formatThrowable(ex, false));
                    return null;
                }

                if (h.isPlainText()) {
                    if (override == null) {
                        if (cs == null || StandardCharsets.ISO_8859_1.equals(cs)) {
                            if (StandardCharsets.ISO_8859_1.equals(cs) && CharsetHelper.isUTF8(result)) {
                                Log.i("Charset upgrade=UTF8");
                                result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                            } else {
                                Charset detected = CharsetHelper.detect(result, StandardCharsets.ISO_8859_1);
                                if (detected == null) {
                                    if (CharsetHelper.isUTF8(result)) {
                                        Log.i("Charset plain=UTF8");
                                        result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                    }
                                } else {
                                    Log.i("Charset plain=" + detected.name());
                                    result = new String(result.getBytes(StandardCharsets.ISO_8859_1), detected);
                                }
                            }
                        } else if (StandardCharsets.UTF_8.equals(cs))
                            result = CharsetHelper.utf8toW1252(result);
                    } else {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Helper.copy(h.part.getDataHandler().getInputStream(), bos);
                        result = bos.toString(override);
                    }

                    // https://datatracker.ietf.org/doc/html/rfc3676
                    if ("flowed".equalsIgnoreCase(h.contentType.getParameter("format")))
                        result = HtmlHelper.flow(result,
                                "yes".equalsIgnoreCase(h.contentType.getParameter("delsp")));

                    // https://www.w3.org/QA/2002/04/valid-dtd-list.html
                    if (result.length() > DOCTYPE.length()) {
                        String doctype = result.substring(0, DOCTYPE.length()).toUpperCase(Locale.ROOT);
                        if (doctype.startsWith(DOCTYPE)) {
                            String[] words = result.split("\\s+");
                            if (words.length > 1 &&
                                    "HTML".equals(words[1].toUpperCase(Locale.ROOT)))
                                return result;
                        }
                    }

                    int s = 0;
                    while (s < result.length() && Character.isWhitespace(result.charAt(s)))
                        s++;
                    int e = result.length();
                    while (e > 0 && Character.isWhitespace(result.charAt(e - 1)))
                        e--;
                    if (s + HTML_START.length() < result.length() && e - HTML_END.length() >= 0 &&
                            result.substring(s, s + HTML_START.length()).equalsIgnoreCase(HTML_START) &&
                            result.substring(e - HTML_END.length(), e).equalsIgnoreCase(HTML_END))
                        return result;

                    result = "<div x-plain=\"true\">" + HtmlHelper.formatPlainText(result) + "</div>";
                } else if (h.isHtml()) {
                    if (override == null) {
                        // Conditionally upgrade to UTF8
                        if ((cs == null ||
                                StandardCharsets.US_ASCII.equals(cs) ||
                                StandardCharsets.ISO_8859_1.equals(cs)) &&
                                CharsetHelper.isUTF8(result))
                            result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                        //if (StandardCharsets.UTF_8.equals(cs))
                        //    result = CharsetHelper.utf8w1252(result);

                        // Fix incorrect UTF16
                        try {
                            if (CHARSET16.contains(cs)) {
                                Charset detected = CharsetHelper.detect(result, cs);
                                if (!CHARSET16.contains(detected))
                                    Log.w(new Throwable("Charset=" + cs + " detected=" + detected));
                                // UTF-16 can be detected as US-ASCII
                                if (StandardCharsets.UTF_8.equals(detected)) {
                                    cs = null;
                                    result = new String(result.getBytes(cs), detected);
                                }
                            }
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }

                        if (cs == null || StandardCharsets.ISO_8859_1.equals(cs)) {
                            // <meta charset="utf-8" />
                            // <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
                            String excerpt = result.substring(0, Math.min(MAX_META_EXCERPT, result.length()));
                            Document d = JsoupEx.parse(excerpt);
                            for (Element meta : d.select("meta")) {
                                String mcharset = null;
                                if ("Content-Type".equalsIgnoreCase(meta.attr("http-equiv"))) {
                                    try {
                                        ContentType ct = new ContentType(meta.attr("content"));
                                        mcharset = ct.getParameter("charset");
                                    } catch (ParseException ex) {
                                        Log.w(ex);
                                    }
                                } else
                                    mcharset = meta.attr("charset");

                                if (!TextUtils.isEmpty(mcharset))
                                    try {
                                        Log.i("Charset meta=" + meta);
                                        Charset c = Charset.forName(mcharset);

                                        // US-ASCII is a subset of ISO8859-1
                                        if (StandardCharsets.US_ASCII.equals(c))
                                            break;

                                        // 16 bits charsets cannot be converted to 8 bits
                                        if (CHARSET16.contains(c)) {
                                            Log.w("Charset meta=" + meta);
                                            break;
                                        }

                                        // Check if really UTF-8
                                        if (StandardCharsets.UTF_8.equals(c) && !CharsetHelper.isUTF8(result)) {
                                            Log.w("Charset meta=" + meta + " !isUTF8");
                                            break;
                                        }

                                        // Check if same as detected charset
                                        Charset detected = CharsetHelper.detect(result, c);
                                        if (c.equals(detected) && !StandardCharsets.ISO_8859_1.equals(cs))
                                            break;

                                        // Common detected/meta
                                        // - windows-1250, windows-1257 / ISO-8859-1
                                        // - ISO-8859-1 / windows-1252
                                        // - US-ASCII / windows-1250, windows-1252, ISO-8859-1, ISO-8859-15, UTF-8

                                        if (StandardCharsets.US_ASCII.equals(detected) &&
                                                ("ISO-8859-15".equals(c.name()) ||
                                                        "windows-1250".equals(c.name()) ||
                                                        "windows-1252".equals(c.name()) ||
                                                        StandardCharsets.UTF_8.equals(c) ||
                                                        StandardCharsets.ISO_8859_1.equals(c)))
                                            break;

                                        // Convert
                                        Log.w("Converting detected=" + detected + " meta=" + c);
                                        result = new String(result.getBytes(StandardCharsets.ISO_8859_1), c);
                                        break;
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                            }
                        }
                    } else {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Helper.copy(h.part.getDataHandler().getInputStream(), bos);
                        result = bos.toString(override);
                    }
                } else if (h.isMarkdown()) {
                    try {
                        if (cs == null ||
                                StandardCharsets.US_ASCII.equals(cs) ||
                                StandardCharsets.ISO_8859_1.equals(cs))
                            result = new String(result.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        result = (first ? "" : "<br><hr>") + Markdown.toHtml(result);
                    } catch (Throwable ex) {
                        Log.e(ex);
                        result = HtmlHelper.formatPlainText(result);
                    }
                } else if (h.isPatch()) {
                    String filename = h.part.getFileName();
                    result = (first ? "" : "<br><hr>") +
                            (TextUtils.isEmpty(filename) ? "" :
                                    "<div style =\"text-align: center;\">" + Html.escapeHtml(filename) + "</div><br>") +
                            "<pre style=\"font-family: monospace; font-size:small;\">" +
                            HtmlHelper.formatPlainText(result) +
                            "</pre>";
                } else if (h.isReport()) {
                    Report report = new Report(h.contentType.getBaseType(), result, context);
                    result = report.html;

                    StringBuilder w = new StringBuilder();

                    if (report.isDeliveryStatus() && !report.isDelivered()) {
                        if (report.diagnostic != null) {
                            String diag = report.diagnostic;
                            if (diag.length() > MAX_DIAGNOSTIC)
                                diag = diag.substring(0, MAX_DIAGNOSTIC) + "…";
                            w.append(diag);
                        }
                        if (report.action != null) {
                            if (w.length() == 0)
                                w.append(report.action);
                            else
                                w.append(" (").append(report.action).append(')');
                        }
                    }

                    if (report.isDispositionNotification() && !report.isMdnDisplayed()) {
                        if (report.disposition != null)
                            w.append(report.disposition);
                    }

                    if (report.isFeedbackReport()) {
                        if (!TextUtils.isEmpty(report.feedback))
                            w.append(report.feedback);
                    }

                    if (w.length() > 0)
                        warnings.add(w.toString());
                } else
                    Log.w("Unexpected content type=" + h.contentType);

                sb.append(result);
                first = false;
            }

            return sb.toString();
        }

        Report getReport(Context context) throws MessagingException, IOException {
            for (PartHolder h : extra)
                if (h.isReport()) {
                    String result;
                    Object content = h.part.getContent();
                    if (content instanceof String)
                        result = (String) content;
                    else if (content instanceof InputStream)
                        result = Helper.readStream((InputStream) content);
                    else
                        result = content.toString();
                    return new Report(h.contentType.getBaseType(), result, context);
                }
            return null;
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

        void downloadAttachment(Context context, EntityAttachment local, EntityFolder folder) throws IOException, MessagingException {
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

            downloadAttachment(context, index, local, folder);
        }

        void downloadAttachment(Context context, int index, EntityAttachment local, EntityFolder folder) throws MessagingException, IOException {
            Log.i("downloading attachment id=" + local.id + " index=" + index + " " + local);

            // Get data
            AttachmentPart apart = attachments.get(index);

            // Download attachment
            File file = local.getFile(context);

            DB db = DB.getInstance(context);
            db.attachment().setProgress(local.id, 0);

            if (EntityAttachment.PGP_CONTENT.equals(apart.encrypt) ||
                    EntityAttachment.SMIME_CONTENT.equals(apart.encrypt)) {
                decodeEncrypted(context, local, apart);
            } else {
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

                if (folder == null || !EntityFolder.isOutgoing(folder.type)) {
                    if ("message/rfc822".equals(local.type))
                        decodeRfc822(context, local, 1);

                    else if ("text/calendar".equals(local.type) && ActivityBilling.isPro(context))
                        decodeICalendar(context, local);

                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && local.isCompressed()) {
                        decodeCompressed(context, local, 1);

                    } else if (Helper.isTnef(local.type, local.name))
                        decodeTNEF(context, local, 1);

                    else if ("msg".equalsIgnoreCase(Helper.getExtension(local.name)))
                        decodeOutlook(context, local, 1);
                }
            }
        }

        private void decodeEncrypted(Context context, EntityAttachment local, AttachmentPart apart) throws MessagingException, IOException {
            ContentType ct = new ContentType(apart.part.getContentType());
            String boundary = ct.getParameter("boundary");
            if (TextUtils.isEmpty(boundary))
                throw new ParseException("Signed boundary missing");

            File file = local.getFile(context);
            try (OutputStream os = new BufferedOutputStream(new CanonicalizingStream(new FileOutputStream(file), apart.encrypt, boundary))) {
                apart.part.writeTo(os);
            }

            DB db = DB.getInstance(context);
            db.attachment().setDownloaded(local.id, file.length());
        }

        private int decodeRfc822(Context context, EntityAttachment local, int subsequence) {
            DB db = DB.getInstance(context);
            try (FileInputStream fis = new FileInputStream(local.getFile(context))) {
                Properties props = MessageHelper.getSessionProperties(true);
                Session isession = Session.getInstance(props, null);
                MimeMessage imessage = new MimeMessage(isession, fis);
                MessageHelper helper = new MessageHelper(imessage, context);
                MessageParts parts = helper.getMessageParts();

                for (AttachmentPart epart : parts.getAttachmentParts())
                    try {
                        Log.i("Embedded attachment seq=" + local.sequence + ":" + subsequence);
                        epart.attachment.message = local.message;
                        epart.attachment.sequence = local.sequence;
                        epart.attachment.subsequence = subsequence++;
                        epart.attachment.id = db.attachment().insertAttachment(epart.attachment);

                        File efile = epart.attachment.getFile(context);
                        Log.i("Writing to " + efile);

                        try (InputStream is = epart.part.getInputStream()) {
                            try (OutputStream os = new FileOutputStream(efile)) {
                                byte[] buffer = new byte[Helper.BUFFER_SIZE];
                                for (int len = is.read(buffer); len != -1; len = is.read(buffer))
                                    os.write(buffer, 0, len);
                            }
                        }

                        db.attachment().setDownloaded(epart.attachment.id, efile.length());

                        if (Helper.isTnef(epart.attachment.type, epart.attachment.name))
                            subsequence = decodeTNEF(context, epart.attachment, subsequence);

                    } catch (Throwable ex) {
                        Log.w(ex);

                        if (epart.attachment.id == null)
                            continue;

                        db.attachment().setError(epart.attachment.id, Log.formatThrowable(ex));
                        db.attachment().setAvailable(epart.attachment.id, true); // unrecoverable
                    }
            } catch (Throwable ex) {
                Log.e(ex);
                if (ex instanceof ArchiveException)
                    db.attachment().setWarning(local.id, ex.getMessage());
                else
                    db.attachment().setWarning(local.id, Log.formatThrowable(ex));
            }

            return subsequence;
        }

        private int decodeCompressed(Context context, EntityAttachment local, int subsequence) {
            // https://commons.apache.org/proper/commons-compress/examples.html
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean unzip = prefs.getBoolean("unzip", !BuildConfig.PLAY_STORE_RELEASE);
            if (!unzip)
                return subsequence;

            DB db = DB.getInstance(context);

            if (local.isGzip() && !local.isTarGzip())
                try (GzipCompressorInputStream gzip = new GzipCompressorInputStream(
                        new BufferedInputStream(new FileInputStream(local.getFile(context))))) {
                    String name = gzip.getMetaData().getFilename();
                    long total = gzip.getUncompressedCount();

                    Log.i("Gzipped attachment seq=" + local.sequence + " " + name + ":" + total);

                    if (total <= MAX_UNZIP_SIZE) {
                        if (name == null &&
                                local.name != null && local.name.endsWith(".gz"))
                            name = local.name.substring(0, local.name.length() - 3);

                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = local.message;
                        attachment.sequence = local.sequence;
                        attachment.subsequence = subsequence++;
                        attachment.name = name;
                        attachment.type = Helper.guessMimeType(name);
                        if (total >= 0)
                            attachment.size = total;
                        attachment.id = db.attachment().insertAttachment(attachment);

                        File efile = attachment.getFile(context);
                        Log.i("Gunzipping to " + efile);

                        int last = 0;
                        long size = 0;
                        try (OutputStream os = new FileOutputStream(efile)) {
                            byte[] buffer = new byte[Helper.BUFFER_SIZE];
                            for (int len = gzip.read(buffer); len != -1; len = gzip.read(buffer)) {
                                size += len;
                                if (size > MAX_UNZIP_SIZE)
                                    throw new IOException("File too large");
                                os.write(buffer, 0, len);

                                if (total > 0) {
                                    int progress = (int) (size * 100 / total);
                                    if (progress / 20 > last / 20) {
                                        last = progress;
                                        db.attachment().setProgress(attachment.id, progress);
                                    }
                                }
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                            db.attachment().setError(attachment.id, Log.formatThrowable(ex));
                            db.attachment().setAvailable(attachment.id, true); // unrecoverable
                        }

                        db.attachment().setDownloaded(attachment.id, efile.length());
                    }
                } catch (Throwable ex) {
                    Log.e(new Throwable(local.name, ex));
                    db.attachment().setWarning(local.id, Log.formatThrowable(ex));
                }
            else
                try (FileInputStream fis = new FileInputStream(local.getFile(context))) {
                    ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(
                            new BufferedInputStream(local.isTarGzip() ? new GzipCompressorInputStream(fis) : fis));

                    int count = 0;
                    ArchiveEntry entry;
                    while ((entry = ais.getNextEntry()) != null)
                        if (ais.canReadEntryData(entry) && !entry.isDirectory()) {
                            if (entry.getSize() > MAX_UNZIP_SIZE)
                                count = MAX_UNZIP_COUNT;
                            if (++count > MAX_UNZIP_COUNT)
                                break;
                        }

                    Log.i("Zip entries=" + count);
                    if (count <= MAX_UNZIP_COUNT) {
                        fis.getChannel().position(0);

                        ais = new ArchiveStreamFactory().createArchiveInputStream(
                                new BufferedInputStream(local.isTarGzip() ? new GzipCompressorInputStream(fis) : fis));

                        while ((entry = ais.getNextEntry()) != null) {
                            if (!ais.canReadEntryData(entry)) {
                                Log.w("Zip invalid=" + entry);
                                continue;
                            }

                            String name = entry.getName();
                            long total = entry.getSize();

                            if (entry.isDirectory() ||
                                    (name != null && name.endsWith("\\"))) {
                                Log.i("Zipped folder=" + name);
                                continue;
                            }

                            Log.i("Zipped attachment seq=" + local.sequence + ":" + subsequence +
                                    " " + name + ":" + total);

                            EntityAttachment attachment = new EntityAttachment();
                            attachment.message = local.message;
                            attachment.sequence = local.sequence;
                            attachment.subsequence = subsequence++;
                            attachment.name = name;
                            attachment.type = Helper.guessMimeType(name);
                            if (total >= 0)
                                attachment.size = total;
                            attachment.id = db.attachment().insertAttachment(attachment);

                            File efile = attachment.getFile(context);
                            Log.i("Unzipping to " + efile);

                            int last = 0;
                            long size = 0;
                            try (OutputStream os = new FileOutputStream(efile)) {
                                byte[] buffer = new byte[Helper.BUFFER_SIZE];
                                for (int len = ais.read(buffer); len != -1; len = ais.read(buffer)) {
                                    size += len;
                                    if (size > MAX_UNZIP_SIZE)
                                        throw new IOException("File too large");
                                    os.write(buffer, 0, len);

                                    if (total > 0) {
                                        int progress = (int) (size * 100 / total);
                                        if (progress / 20 > last / 20) {
                                            last = progress;
                                            db.attachment().setProgress(attachment.id, progress);
                                        }
                                    }
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                                db.attachment().setError(attachment.id, Log.formatThrowable(ex));
                                db.attachment().setAvailable(attachment.id, true); // unrecoverable
                            }

                            db.attachment().setDownloaded(attachment.id, efile.length());
                        }
                    }
                } catch (Throwable ex) {
                    Log.e(new Throwable(local.name, ex));
                    // ArchiveException: Unsupported feature encryption used in entry ...
                    // UnsupportedZipFeatureException: No Archiver found for the stream signature
                    if (ex instanceof ArchiveException ||
                            ex instanceof UnsupportedZipFeatureException)
                        db.attachment().setWarning(local.id, ex.getMessage());
                    else
                        db.attachment().setWarning(local.id, Log.formatThrowable(ex));
                }

            return subsequence;
        }

        private void decodeICalendar(Context context, EntityAttachment local) {
            DB db = DB.getInstance(context);
            try {
                boolean permission = Helper.hasPermission(context, Manifest.permission.WRITE_CALENDAR);

                EntityMessage message = db.message().getMessage(local.message);
                EntityFolder folder = (message == null ? null : db.folder().getFolder(message.folder));
                EntityAccount account = (folder == null ? null : db.account().getAccount(folder.account));

                boolean received = (folder != null &&
                        (EntityFolder.INBOX.equals(folder.type) ||
                                EntityFolder.SYSTEM.equals(folder.type) ||
                                EntityFolder.USER.equals(folder.type)));

                if (!permission || !received || account == null || account.calendar == null) {
                    EntityLog.log(context, "Event not processed" +
                            " permission=" + permission +
                            " account=" + (account != null) +
                            " folder=" + (folder != null) + ":" + (folder == null ? null : folder.type) +
                            " received=" + received +
                            " calendar=" + (account == null ? null : account.calendar));
                    return;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean ical_tentative = prefs.getBoolean("ical_tentative", true);

                File file = local.getFile(context);
                ICalendar icalendar = CalendarHelper.parse(context, file);

                List<VEvent> events = icalendar.getEvents();
                if (events == null || events.size() == 0)
                    EntityLog.log(context, "No events");
                else {
                    VEvent event = events.get(0);

                    // https://www.rfc-editor.org/rfc/rfc5546#section-3.2
                    Method method = icalendar.getMethod();
                    if (method != null && method.isCancel())
                        CalendarHelper.delete(context, icalendar, event, account, message);
                    else if (method == null || method.isRequest()) {
                        if (ical_tentative)
                            CalendarHelper.insert(context, icalendar, event,
                                    CalendarContract.Events.STATUS_TENTATIVE, account, message);
                        else
                            EntityLog.log(context, "Tentative event not stored");
                    } else
                        EntityLog.log(context, "Unknown event method=" + method.getValue());
                }
            } catch (Throwable ex) {
                Log.w(ex);
                db.attachment().setWarning(local.id, Log.formatThrowable(ex));
            }
        }

        private int decodeTNEF(Context context, EntityAttachment local, int subsequence) {
            try {
                DB db = DB.getInstance(context);

                // https://poi.apache.org/components/hmef/index.html
                File file = local.getFile(context);
                org.apache.poi.hmef.HMEFMessage msg = new org.apache.poi.hmef.HMEFMessage(new FileInputStream(file));

                String subject = msg.getSubject();
                if (!TextUtils.isEmpty(subject)) {
                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = local.message;
                    attachment.sequence = local.sequence;
                    attachment.subsequence = subsequence++;
                    attachment.name = "subject.txt";
                    attachment.type = "text/plain";
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    try {
                        Helper.writeText(attachment.getFile(context), subject);
                        db.attachment().setDownloaded(attachment.id, (long) subject.length());
                    } catch (Throwable ex) {
                        Log.e(ex);
                        db.attachment().setError(attachment.id, Log.formatThrowable(ex));
                    }
                }

                String body = msg.getBody();
                if (TextUtils.isEmpty(body)) {
                    org.apache.poi.hmef.attribute.MAPIAttribute attr =
                            msg.getMessageMAPIAttribute(org.apache.poi.hsmf.datatypes.MAPIProperty.BODY_HTML);
                    if (attr == null)
                        attr = msg.getMessageMAPIAttribute(org.apache.poi.hsmf.datatypes.MAPIProperty.BODY);
                    if (attr != null) {
                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = local.message;
                        attachment.sequence = local.sequence;
                        attachment.subsequence = subsequence++;
                        if (attr.getProperty().equals(org.apache.poi.hsmf.datatypes.MAPIProperty.BODY_HTML)) {
                            attachment.name = "body.html";
                            attachment.type = "text/html";
                        } else {
                            attachment.name = "body.txt";
                            attachment.type = "text/plain";
                        }
                        attachment.disposition = Part.ATTACHMENT;
                        attachment.id = db.attachment().insertAttachment(attachment);

                        try {
                            byte[] data = attr.getData();
                            Helper.writeText(attachment.getFile(context), new String(data));
                            db.attachment().setDownloaded(attachment.id, (long) data.length);
                        } catch (Throwable ex) {
                            Log.e(ex);
                            db.attachment().setError(attachment.id, Log.formatThrowable(ex));
                        }
                    }
                } else {
                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = local.message;
                    attachment.sequence = local.sequence;
                    attachment.subsequence = subsequence++;
                    attachment.name = "body.rtf";
                    attachment.type = "application/rtf";
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    try {
                        Helper.writeText(attachment.getFile(context), body);
                        db.attachment().setDownloaded(attachment.id, (long) body.length());
                    } catch (Throwable ex) {
                        Log.e(ex);
                        db.attachment().setError(attachment.id, Log.formatThrowable(ex));
                    }
                }

                for (org.apache.poi.hmef.Attachment at : msg.getAttachments()) {
                    String filename = at.getLongFilename();
                    if (filename == null)
                        filename = at.getFilename();
                    if (filename == null) {
                        String ext = at.getExtension();
                        if (ext != null)
                            filename = "document." + ext;
                    }

                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = local.message;
                    attachment.sequence = local.sequence;
                    attachment.subsequence = subsequence++;
                    attachment.name = filename;
                    attachment.type = Helper.guessMimeType(attachment.name);
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    try {
                        byte[] data = at.getContents();
                        try (OutputStream os = new FileOutputStream(attachment.getFile(context))) {
                            os.write(data);
                        }

                        db.attachment().setDownloaded(attachment.id, (long) data.length);
                    } catch (Throwable ex) {
                        // java.lang.IllegalArgumentException: Attachment corrupt - no Data section
                        Log.e(ex);
                        db.attachment().setError(attachment.id, Log.formatThrowable(ex));
                    }
                }

                StringBuilder sb = new StringBuilder();
                for (org.apache.poi.hmef.attribute.TNEFAttribute attr : msg.getMessageAttributes())
                    sb.append(attr.toString()).append("\r\n");
                for (org.apache.poi.hmef.attribute.MAPIAttribute attr : msg.getMessageMAPIAttributes())
                    if (!org.apache.poi.hsmf.datatypes.MAPIProperty.RTF_COMPRESSED.equals(attr.getProperty()) &&
                            !org.apache.poi.hsmf.datatypes.MAPIProperty.BODY_HTML.equals(attr.getProperty()))
                        sb.append(attr.toString()).append("\r\n");
                if (sb.length() > 0) {
                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = local.message;
                    attachment.sequence = local.sequence;
                    attachment.subsequence = subsequence++;
                    attachment.name = "attributes.txt";
                    attachment.type = "text/plain";
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    try {
                        Helper.writeText(attachment.getFile(context), sb.toString());
                        db.attachment().setDownloaded(attachment.id, (long) sb.length());
                    } catch (Throwable ex) {
                        Log.e(ex);
                        db.attachment().setError(attachment.id, Log.formatThrowable(ex));
                    }
                }
            } catch (Throwable ex) {
                Log.w(ex);
            }

            return subsequence;
        }

        private int decodeOutlook(Context context, EntityAttachment local, int subsequence) {
            try {
                DB db = DB.getInstance(context);

                // https://poi.apache.org/components/hmef/index.html
                File file = local.getFile(context);
                OutlookMessage msg = new OutlookMessageParser().parseMsg(file);

                String headers = msg.getHeaders();
                if (!TextUtils.isEmpty(headers)) {
                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = local.message;
                    attachment.sequence = local.sequence;
                    attachment.subsequence = subsequence++;
                    attachment.name = "headers.txt";
                    attachment.type = "text/rfc822-headers";
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    Helper.writeText(attachment.getFile(context), headers);
                    db.attachment().setDownloaded(attachment.id, (long) headers.length());
                }

                String html = msg.getBodyHTML();
                if (!TextUtils.isEmpty(html)) {
                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = local.message;
                    attachment.sequence = local.sequence;
                    attachment.subsequence = subsequence++;
                    attachment.name = "body.html";
                    attachment.type = "text/html";
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    File a = attachment.getFile(context);
                    Helper.writeText(a, html);
                    db.attachment().setDownloaded(attachment.id, a.length());
                }

                if (TextUtils.isEmpty(html)) {
                    String text = msg.getBodyText();
                    if (!TextUtils.isEmpty(text)) {
                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = local.message;
                        attachment.sequence = local.sequence;
                        attachment.subsequence = subsequence++;
                        attachment.name = "body.txt";
                        attachment.type = "text/plain";
                        attachment.disposition = Part.ATTACHMENT;
                        attachment.id = db.attachment().insertAttachment(attachment);

                        File a = attachment.getFile(context);
                        Helper.writeText(a, text);
                        db.attachment().setDownloaded(attachment.id, a.length());
                    }
                }

                String rtf = msg.getBodyRTF();
                if (!TextUtils.isEmpty(rtf)) {
                    EntityAttachment attachment = new EntityAttachment();
                    attachment.message = local.message;
                    attachment.sequence = local.sequence;
                    attachment.subsequence = subsequence++;
                    attachment.name = "body.rtf";
                    attachment.type = "application/rtf";
                    attachment.disposition = Part.ATTACHMENT;
                    attachment.id = db.attachment().insertAttachment(attachment);

                    File a = attachment.getFile(context);
                    Helper.writeText(a, rtf);
                    db.attachment().setDownloaded(attachment.id, a.length());
                }

                List<OutlookAttachment> attachments = msg.getOutlookAttachments();
                for (OutlookAttachment oa : attachments)
                    if (oa instanceof OutlookFileAttachment) {
                        OutlookFileAttachment ofa = (OutlookFileAttachment) oa;

                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = local.message;
                        attachment.sequence = local.sequence;
                        attachment.subsequence = subsequence++;
                        attachment.name = ofa.getFilename();
                        attachment.type = ofa.getMimeTag();
                        attachment.disposition = Part.ATTACHMENT;
                        attachment.id = db.attachment().insertAttachment(attachment);

                        if (TextUtils.isEmpty(attachment.type))
                            attachment.type = Helper.guessMimeType(attachment.name);

                        byte[] data = ofa.getData();
                        try (OutputStream os = new FileOutputStream(attachment.getFile(context))) {
                            os.write(data);
                        }

                        db.attachment().setDownloaded(attachment.id, (long) data.length);
                    }

            } catch (Throwable ex) {
                Log.w(ex);
            }

            return subsequence;
        }

        String getWarnings(String existing) {
            if (existing != null) {
                boolean exists = false;
                for (String warning : warnings)
                    if (existing.equals(warning)) {
                        exists = true;
                        break;
                    }
                if (!exists)
                    warnings.add(0, existing);
            }

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

    MessageParts getMessageParts() throws IOException, MessagingException {
        return getMessageParts(true);
    }

    MessageParts getMessageParts(boolean normalize) throws IOException, MessagingException {
        MessageParts parts = new MessageParts();

        try {
            ensureStructure();

            try {
                MimePart part = imessage;

                if (isMimeType(part, "multipart/mixed")) {
                    Object content = part.getContent();

                    if (content instanceof String)
                        content = tryParseMultipart((String) content, part.getContentType());
                    else if (content instanceof com.sun.mail.imap.IMAPInputStream)
                        content = tryParseMultipart(Helper.readStream((com.sun.mail.imap.IMAPInputStream) content), part.getContentType());

                    if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        for (int i = 0; i < mp.getCount(); i++) {
                            BodyPart bp = mp.getBodyPart(i);
                            if (isMimeType(bp, "multipart/encrypted")) {
                                for (int j = 0; j < mp.getCount(); j++)
                                    if (j != i)
                                        getMessageParts(part, mp.getBodyPart(j), parts, null);
                                part = (MimePart) bp;
                                break;
                            } else if (isMimeType(bp, "application/pgp-encrypted") && i + 1 < mp.getCount()) {
                                for (int j = 0; j < i; j++)
                                    getMessageParts(part, mp.getBodyPart(j), parts, null);
                                // Workaround Outlook problem
                                //  --_xxxoutlookfr_
                                // Content-Type: text/plain; charset="us-ascii"
                                //
                                // --_xxxoutlookfr_
                                // Content-Type: application/pgp-encrypted; name="ATT00001"
                                // Content-Disposition: attachment; filename="ATT00001";
                                //
                                // --_xxxoutlookfr_
                                // Content-Type: application/octet-stream; name="encrypted.asc"
                                // Content-Disposition: attachment; filename="encrypted.asc";
                                getMessageParts(part, mp.getBodyPart(i + 1), parts, EntityAttachment.PGP_MESSAGE);
                                return parts;
                            }
                        }
                    } else {
                        String msg = "Expected multipart/mixed got " + content.getClass().getName();
                        Log.e(msg);
                        parts.warnings.add(msg);
                    }
                }

                if (isMimeType(part, "multipart/signed")) {
                    ContentType ct = new ContentType(part.getContentType());
                    String protocol = ct.getParameter("protocol");
                    if ("application/pgp-signature".equals(protocol) ||
                            "application/pkcs7-signature".equals(protocol) ||
                            "application/x-pkcs7-signature".equals(protocol)) {
                        Object content = part.getContent();

                        if (content instanceof String)
                            content = tryParseMultipart((String) content, part.getContentType());
                        else if (content instanceof com.sun.mail.imap.IMAPInputStream)
                            content = tryParseMultipart(Helper.readStream((com.sun.mail.imap.IMAPInputStream) content), part.getContentType());

                        if (content instanceof Multipart) {
                            Multipart multipart = (Multipart) content;
                            if (multipart.getCount() == 2) {
                                getMessageParts(part, multipart.getBodyPart(0), parts, null);
                                getMessageParts(part, multipart.getBodyPart(1), parts,
                                        "application/pgp-signature".equals(protocol)
                                                ? EntityAttachment.PGP_SIGNATURE
                                                : EntityAttachment.SMIME_SIGNATURE);

                                AttachmentPart apart = new AttachmentPart();
                                apart.disposition = Part.INLINE;
                                apart.filename = "content.asc";
                                apart.encrypt = "application/pgp-signature".equals(protocol)
                                        ? EntityAttachment.PGP_CONTENT
                                        : EntityAttachment.SMIME_CONTENT;
                                apart.part = part;

                                apart.attachment = new EntityAttachment();
                                apart.attachment.disposition = apart.disposition;
                                apart.attachment.name = apart.filename;
                                apart.attachment.type = "text/plain";
                                apart.attachment.size = getSize();
                                apart.attachment.encryption = apart.encrypt;

                                parts.attachments.add(apart);

                                return parts;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append(ct).append(" parts=").append(multipart.getCount()).append("/2");
                                for (int i = 0; i < multipart.getCount(); i++)
                                    sb.append(' ').append(i).append('=').append(multipart.getBodyPart(i).getContentType());
                                Log.e(sb.toString());
                            }
                        } else {
                            String msg = "Expected multipart/signed got " + content.getClass().getName();
                            Log.e(msg);
                            parts.warnings.add(msg);
                        }
                    } else
                        Log.e(ct.toString());
                } else if (isMimeType(part, "multipart/encrypted")) {
                    ContentType ct = new ContentType(part.getContentType());
                    String protocol = ct.getParameter("protocol");
                    if ("application/pgp-encrypted".equals(protocol) || protocol == null) {
                        Object content = part.getContent();

                        if (content instanceof String)
                            content = tryParseMultipart((String) content, part.getContentType());
                        else if (content instanceof com.sun.mail.imap.IMAPInputStream)
                            content = tryParseMultipart(Helper.readStream((com.sun.mail.imap.IMAPInputStream) content), part.getContentType());

                        if (content instanceof Multipart) {
                            Multipart multipart = (Multipart) content;
                            if (multipart.getCount() == 2) {
                                // Ignore header
                                getMessageParts(part, multipart.getBodyPart(1), parts, EntityAttachment.PGP_MESSAGE);
                                return parts;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append(ct).append(" parts=").append(multipart.getCount()).append("/2");
                                for (int i = 0; i < multipart.getCount(); i++)
                                    sb.append(' ').append(i).append('=').append(multipart.getBodyPart(i).getContentType());
                                Log.e(sb.toString());
                            }
                        } else {
                            String msg = "Expected multipart/encrypted got " + content.getClass().getName();
                            Log.e(msg);
                            parts.warnings.add(msg);

                        }
                    } else
                        Log.e(ct.toString());
                } else if (isMimeType(part, "application/pkcs7-mime") ||
                        isMimeType(part, "application/x-pkcs7-mime")) {
                    ContentType ct = new ContentType(part.getContentType());
                    String smimeType = ct.getParameter("smime-type");
                    if ("enveloped-data".equalsIgnoreCase(smimeType)) {
                        getMessageParts(null, part, parts, EntityAttachment.SMIME_MESSAGE);
                        return parts;
                    } else if ("signed-data".equalsIgnoreCase(smimeType)) {
                        getMessageParts(null, part, parts, EntityAttachment.SMIME_SIGNED_DATA);
                        return parts;
                    } else if ("signed-receipt".equalsIgnoreCase(smimeType)) {
                        // https://datatracker.ietf.org/doc/html/rfc2634#section-2
                    } else {
                        if (TextUtils.isEmpty(smimeType)) {
                            String name = ct.getParameter("name");
                            if ("smime.p7m".equalsIgnoreCase(name)) {
                                getMessageParts(null, part, parts, EntityAttachment.SMIME_MESSAGE);
                                return parts;
                            } else if ("smime.p7s".equalsIgnoreCase(name)) {
                                getMessageParts(null, part, parts, EntityAttachment.SMIME_SIGNED_DATA);
                                return parts;
                            }
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("Unexpected smime-type=").append(ct);
                        Log.e(sb.toString());
                    }
                }
            } catch (ParseException ex) {
                Log.w(ex);
            }

            getMessageParts(null, imessage, parts, null);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            parts.warnings.add(Log.formatThrowable(ex, false));
            /*
                java.lang.OutOfMemoryError: Failed to allocate a xxx byte allocation with yyy free bytes and zzMB until OOM
                        at java.io.ByteArrayOutputStream.expand(ByteArrayOutputStream.java:91)
                        at java.io.ByteArrayOutputStream.write(ByteArrayOutputStream.java:201)
                        at com.sun.mail.util.ASCIIUtility.getBytes(ASCIIUtility:279)
                        at javax.mail.internet.MimeMessage.parse(MimeMessage:336)
                        at javax.mail.internet.MimeMessage.<init>(MimeMessage:199)
                        at eu.faircode.email.MimeMessageEx.<init>(MimeMessageEx:44)
                        at eu.faircode.email.MessageHelper._ensureMessage(MessageHelper:2732)
                        at eu.faircode.email.MessageHelper.ensureStructure(MessageHelper:2685)
                        at eu.faircode.email.MessageHelper.getMessageParts(MessageHelper:2368)
             */
        }

        if (normalize)
            parts.normalize();

        return parts;
    }

    private void getMessageParts(Part parent, Part part, MessageParts parts, Integer encrypt) throws IOException, MessagingException {
        try {
            Log.d("Part class=" + part.getClass() + " type=" + part.getContentType());

            try {
                ContentType ct = new ContentType(part.getContentType());

                // https://github.com/autocrypt/protected-headers
                if ("v1".equals(ct.getParameter("protected-headers"))) {
                    String[] subject = part.getHeader("subject");
                    if (subject != null && subject.length != 0) {
                        subject[0] = subject[0].replaceAll("\\?=[\\r\\n\\t ]+=\\?", "\\?==\\?");
                        parts.protected_subject = decodeMime(subject[0]);
                    }
                }

                // https://en.wikipedia.org/wiki/MIME#Multipart_subtypes
                if ("multipart".equals(ct.getPrimaryType()) &&
                        !("mixed".equalsIgnoreCase(ct.getSubType()) ||
                                "none".equalsIgnoreCase(ct.getSubType()) ||
                                "signed".equalsIgnoreCase(ct.getSubType()) ||
                                "alternate".equalsIgnoreCase(ct.getSubType()) ||
                                "alternative".equalsIgnoreCase(ct.getSubType()) ||
                                "related".equalsIgnoreCase(ct.getSubType()) ||
                                "relative".equalsIgnoreCase(ct.getSubType()) || // typo?
                                "report".equalsIgnoreCase(ct.getSubType()) ||
                                "parallel".equalsIgnoreCase(ct.getSubType()) ||
                                "digest".equalsIgnoreCase(ct.getSubType()) ||
                                "appledouble".equalsIgnoreCase(ct.getSubType()) ||
                                "voice-message".equalsIgnoreCase(ct.getSubType())))
                    // voice-message: https://www.rfc-editor.org/rfc/rfc3458.txt
                    Log.e(part.getContentType());
            } catch (Throwable ex) {
                Log.e(ex);
            }

            if (isMimeType(part, "multipart/*")) {
                Multipart multipart;
                Object content = part.getContent(); // Should always be Multipart

                if (content instanceof String)
                    content = tryParseMultipart((String) content, part.getContentType());
                else if (content instanceof com.sun.mail.imap.IMAPInputStream)
                    content = tryParseMultipart(Helper.readStream((com.sun.mail.imap.IMAPInputStream) content), part.getContentType());

                if (content instanceof Multipart) {
                    multipart = (Multipart) content;
                    int count = multipart.getCount();
                    for (int i = 0; i < count; i++)
                        try {
                            BodyPart child = multipart.getBodyPart(i);
                            getMessageParts(part, child, parts, encrypt);
                        } catch (ParseException ex) {
                            // Nested body: try to continue
                            // ParseException: In parameter list boundary="...">, expected parameter name, got ";"
                            Log.w(ex);
                            parts.warnings.add(Log.formatThrowable(ex, false));
                        }

                    if (multipart instanceof MimeMultipart &&
                            Boolean.parseBoolean(System.getProperty("fairemail.preamble"))) {
                        String preamble = ((MimeMultipart) multipart).getPreamble();
                        if (!TextUtils.isEmpty(preamble)) {
                            ContentType plain = new ContentType("text/plain; preamble=\"true\"");
                            parts.extra.add(new PartHolder(part, plain));
                        }
                    }

                    return;
                } else {
                    String msg = "Expected multipart/* got " + content.getClass().getName();
                    Log.e(msg);
                    parts.warnings.add(msg);
                }
            }

            // https://www.iana.org/assignments/cont-disp/cont-disp.xhtml
            String disposition;
            try {
                // From the body structure
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
                // From the body structure:
                // 1. disposition filename
                // 2. content type name
                filename = part.getFileName(); // IMAPBodyPart/BODYSTRUCTURE
                if (filename != null) {
                    // https://tools.ietf.org/html/rfc2231
                    // http://kb.mozillazine.org/Attachments_renamed
                    // https://blog.nodemailer.com/2017/01/27/the-mess-that-is-attachment-filenames/
                    int q1 = filename.indexOf('\'');
                    int q2 = filename.indexOf('\'', q1 + 1);
                    if (q1 >= 0 && q2 > 0) {
                        try {
                            String charset = filename.substring(0, q1);
                            String language = filename.substring(q1 + 1, q2);
                            String name = filename.substring(q2 + 1)
                                    .replace("+", "%2B");

                            if (!TextUtils.isEmpty(charset))
                                filename = URLDecoder.decode(name, charset);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }

                    filename = decodeMime(filename);
                }
            } catch (MessagingException ex) {
                Log.w(ex);
                parts.warnings.add(Log.formatThrowable(ex, false));
                filename = null;
            }

            int size = Integer.MAX_VALUE;
            try {
                size = part.getSize();
            } catch (MessagingException ex) {
                Log.w(ex);
            }

            ContentType contentType;
            try {
                // From the body structure
                contentType = new ContentType(part.getContentType());
            } catch (ParseException ex) {
                if (part instanceof MimeMessage)
                    Log.w("MimeMessage content type=" + ex.getMessage());
                else
                    Log.w(ex);
                contentType = new ContentType(Helper.guessMimeType(filename));
            }

            String ct = contentType.getBaseType();
            if (("text/plain".equalsIgnoreCase(ct) || "text/html".equalsIgnoreCase(ct)) &&
                    TextUtils.isEmpty(filename) &&
                    !Part.ATTACHMENT.equalsIgnoreCase(disposition) &&
                    (size <= MAX_MESSAGE_SIZE || size == Integer.MAX_VALUE)) {
                parts.text.add(new PartHolder(part, contentType));
            } else {
                // Workaround for NIL message content type
                if ("application/octet-stream".equals(ct) && part instanceof MimeMessage) {
                    ContentType plain = new ContentType("text/plain");
                    plain.setParameterList(contentType.getParameterList());
                    Log.w("Converting from " + contentType + " to " + plain);
                    parts.text.add(new PartHolder(part, plain));
                }

                if (("text/plain".equalsIgnoreCase(ct) || "text/html".equalsIgnoreCase(ct)) &&
                        TextUtils.isEmpty(Helper.getExtension(filename))) {
                    if (TextUtils.isEmpty(filename))
                        filename = "body";
                    if ("text/plain".equalsIgnoreCase(ct))
                        filename += ".txt";
                    if ("text/html".equalsIgnoreCase(ct))
                        filename += ".html";
                }

                String ext = Helper.getExtension(filename);
                if ("text/markdown".equalsIgnoreCase(ct) ||
                        "text/x-diff".equalsIgnoreCase(ct) ||
                        "text/x-patch".equalsIgnoreCase(ct) ||
                        "diff".equalsIgnoreCase(ext) ||
                        "patch".equalsIgnoreCase(ext))
                    parts.extra.add(new PartHolder(part, contentType, filename));

                if (Report.isDeliveryStatus(ct) ||
                        Report.isDispositionNotification(ct) ||
                        Report.isFeedbackReport(ct))
                    parts.extra.add(new PartHolder(part, contentType));

                AttachmentPart apart = new AttachmentPart();
                apart.disposition = disposition;
                apart.filename = filename;
                apart.encrypt = encrypt;
                apart.part = part;

                String cid = null;
                try {
                    if (apart.part instanceof IMAPBodyPart)
                        cid = ((IMAPBodyPart) apart.part).getContentID();
                    if (TextUtils.isEmpty(cid)) {
                        String[] cids = apart.part.getHeader("Content-ID");
                        if (cids != null && cids.length > 0)
                            cid = MimeUtility.unfold(cids[0]);
                    }
                } catch (MessagingException ex) {
                    Log.w(ex);
                    if (!"Failed to fetch headers".equals(ex.getMessage()))
                        parts.warnings.add(Log.formatThrowable(ex, false));
                }

                Boolean related = null;
                if (parent != null)
                    try {
                        related = isMimeType(parent, "multipart/related");
                    } catch (MessagingException ex) {
                        Log.w(ex);
                    }

                apart.attachment = new EntityAttachment();
                if (part instanceof IMAPBodyPart)
                    apart.attachment.section = ((IMAPBodyPart) part).getSectionId();
                apart.attachment.disposition = apart.disposition;
                apart.attachment.name = apart.filename;
                apart.attachment.type = contentType.getBaseType().toLowerCase(Locale.ROOT);
                apart.attachment.size = (long) apart.part.getSize();
                apart.attachment.cid = cid;
                apart.attachment.related = related;
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
        } catch (FolderClosedException ex) {
            throw ex;
        } catch (MessagingException ex) {
            if (ex instanceof ParseException)
                Log.e(ex);
            else
                Log.w(ex);
            parts.warnings.add(Log.formatThrowable(ex, false));
        }
    }

    private static boolean isMimeType(Part part, String mimeType) throws MessagingException {
        if (mimeType.endsWith("/*"))
            return part.isMimeType(mimeType);

        if (part.isMimeType(mimeType)) {
            ContentType ct = new ContentType(part.getContentType());
            if (!"*".equals(ct.getSubType()))
                return true;
        }

        return false;
    }

    private Object tryParseMultipart(String text, String contentType) {
        try {
            return new MimeMultipart(new DataSource() {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1));
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return null;
                }

                @Override
                public String getContentType() {
                    return contentType;
                }

                @Override
                public String getName() {
                    return "String";
                }
            });
        } catch (MessagingException ex) {
            Log.e(ex);
            return text;
        }
    }

    private void ensureEnvelope() throws MessagingException {
        _ensureMessage(false, false);
    }

    private void ensureHeaders() throws MessagingException {
        _ensureMessage(false, true);
    }

    private void ensureStructure() throws MessagingException {
        _ensureMessage(true, true);
    }

    private void _ensureMessage(boolean structure, boolean headers) throws MessagingException {
        if (structure) {
            if (ensuredStructure)
                return;
            ensuredStructure = true;
        } else if (headers) {
            if (ensuredHeaders)
                return;
            ensuredHeaders = true;
        } else {
            if (ensuredEnvelope)
                return;
            ensuredEnvelope = true;
        }

        Log.i("Ensure structure=" + structure + " headers=" + headers);

        try {
            if (imessage instanceof IMAPMessage) {
                if (Boolean.parseBoolean(imessage.getSession().getProperty("fairemail.rawfetch"))) {
                    Properties props = MessageHelper.getSessionProperties(true);
                    Session isession = Session.getInstance(props, null);
                    imessage = new MimeMessage(isession, ((ReadableMime) imessage).getMimeStream());
                }

                if (structure)
                    imessage.getContentType(); // force loadBODYSTRUCTURE
                else {
                    if (headers)
                        imessage.getAllHeaders(); // force loadHeaders
                    else
                        imessage.getMessageID(); // force loadEnvelope
                }
            }
        } catch (MessagingException ex) {
            // https://javaee.github.io/javamail/FAQ#imapserverbug
            if ("Failed to load IMAP envelope".equals(ex.getMessage()) ||
                    "Unable to load BODYSTRUCTURE".equals(ex.getMessage()))
                try {
                    if (false)
                        ((IMAPFolder) imessage.getFolder()).doCommand(new IMAPFolder.ProtocolCommand() {
                            @Override
                            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                                MessageSet[] set = Utility.toMessageSet(new Message[]{imessage}, null);
                                Response[] r = p.fetch(set, p.isREV1() ? "BODY.PEEK[]" : "RFC822");
                                p.notifyResponseHandlers(r);
                                p.handleResult(r[r.length - 1]);
                                return null;
                            }
                        });

                    Log.w("Fetching raw message");
                    Helper.ByteArrayInOutStream bos = new Helper.ByteArrayInOutStream();
                    imessage.writeTo(bos);

                    ByteArrayInputStream bis = bos.getInputStream();
                    if (bis.available() == 0)
                        throw new IOException("NIL");

                    Properties props = MessageHelper.getSessionProperties(true);
                    Session isession = Session.getInstance(props, null);

                    Log.w("Decoding raw message");
                    imessage = new MimeMessageEx(isession, bis, imessage);
                } catch (IOException ex1) {
                    Log.e(ex1);
                    throw ex;
                }
            else
                throw ex;
        }
    }

    static int getMessageCount(Folder folder) {
        try {
            // Prevent pool lock
            if (folder instanceof IMAPFolder) {
                int count = ((IMAPFolder) folder).getCachedCount();
                Log.i(folder.getFullName() + " total count=" + count);
                return count;
            }

            int count = 0;
            for (Message message : folder.getMessages())
                if (!message.isExpunged())
                    count++;

            return count;
        } catch (Throwable ex) {
            if (BuildConfig.PLAY_STORE_RELEASE)
                Log.i(ex);
            else
                Log.e(ex);
            return -1;
        }
    }

    static boolean hasCapability(IMAPFolder ifolder, final String capability) throws MessagingException {
        // Folder can have different capabilities than the store
        return (boolean) ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                return protocol.hasCapability(capability);
            }
        });
    }

    static String sanitizeKeyword(String keyword) {
        // https://tools.ietf.org/html/rfc3501
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyword.length(); i++) {
            // flag-keyword    = atom
            // atom            = 1*ATOM-CHAR
            // ATOM-CHAR       = <any CHAR except atom-specials>
            // CHAR8           = %x01-ff ; any OCTET except NUL, %x00
            // So, basically ISO 8859-1
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

        return Normalizer.normalize(sb.toString(), Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    static InternetAddress buildAddress(String email, String name, boolean suggest) {
        try {
            InternetAddress address = (email == null ? new InternetAddress() : new InternetAddress(email));

            if (suggest && !TextUtils.isEmpty(name) &&
                    TextUtils.isEmpty(address.getPersonal())) {
                try {
                    address.setPersonal(name, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException ex) {
                    Log.i(ex);
                }
            }

            if (TextUtils.isEmpty(address.getAddress()) && TextUtils.isEmpty(address.getAddress()))
                return null;

            return address;
        } catch (AddressException ex) {
            Log.e(ex);
            return null;
        }
    }

    static String sanitizeName(String name) {
        if (!BuildConfig.DEBUG)
            return name;

        // https://www.w3.org/TR/xml-entity-names/1D4.html
        // https://www.utf8-chartable.de/unicode-utf8-table.pl?start=119808

        StringBuilder sb = new StringBuilder();

        int k = 0;
        while (k < name.length()) {
            int cp = name.codePointAt(k);

            if (cp >= 0x1D400 && cp <= 0x1D419) // MATHEMATICAL BOLD CAPITAL A-Z
                sb.append((char) (cp - 0x1D400 + 65));
            else if (cp >= 0x1D41A && cp <= 0x1D433) // MATHEMATICAL BOLD SMALL A-Z
                sb.append((char) (cp - 0x1D41A + 97));
            else if (cp >= 0x1D434 && cp <= 0x1D44D) // MATHEMATICAL ITALIC CAPITAL A-Z
                sb.append((char) (cp - 0x1D434 + 65));
            else if (cp >= 0x1D44E && cp <= 0x1D467) // MATHEMATICAL ITALIC SMALL A-Z
                sb.append((char) (cp - 0x1D44E + 97));
            else if (cp >= 0x1D468 && cp <= 0x1D481) // MATHEMATICAL BOLD ITALIC CAPITAL A-Z
                sb.append((char) (cp - 0x1D468 + 65));
            else if (cp >= 0x1D482 && cp <= 0x1D49B) // MATHEMATICAL BOLD ITALIC SMALL A-Z
                sb.append((char) (cp - 0x1D482 + 97));
            else if (cp >= 0x1D49C && cp <= 0x1D4B5) // MATHEMATICAL SCRIPT CAPITAL A-Z
                sb.append((char) (cp - 0x1D49C + 65));
            else if (cp >= 0x1D4B6 && cp <= 0x1D4CF) // MATHEMATICAL SCRIPT SMALL A-Z
                sb.append((char) (cp - 0x1D4B6 + 97));
            else if (cp >= 0x1D4D0 && cp <= 0x1D4E9) // MATHEMATICAL BOLD SCRIPT CAPITAL A-Z
                sb.append((char) (cp - 0x1D4D0 + 65));
            else if (cp >= 0x1D4EA && cp <= 0x1D4FF) // MATHEMATICAL BOLD SCRIPT SMALL A-Z
                sb.append((char) (cp - 0x1D4EA + 97));
            else
                sb.appendCodePoint(cp);

            k += Character.charCount(cp);
        }
        return sb.toString();
    }

    static InternetAddress[] parseAddresses(Context context, String text) throws AddressException {
        if (TextUtils.isEmpty(text))
            return null;

        int skip = 0;
        boolean quoted = false;
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char kar = text.charAt(i);

            if (kar == '"' && (quoted || text.indexOf('"', i + 1) > 0))
                quoted = !quoted;

            if (!quoted && kar == '(' && text.indexOf(')', i) > 0)
                skip++;
            else if (!quoted && kar == ')' && skip > 0)
                skip--;
            else if (skip == 0)
                sb.append(kar);
        }
        text = sb.toString();

        InternetAddress[] addresses = InternetAddress.parseHeader(text, false);
        if (addresses.length == 0)
            return null;

        for (InternetAddress address : addresses) {
            String email = address.getAddress();
            if (email != null)
                address.setAddress(email
                        .replace(" ", "")
                        .replace("\u00a0", ""));
        }

        return addresses;
    }

    static InternetAddress[] dedup(InternetAddress[] addresses) {
        if (addresses == null)
            return null;

        List<String> emails = new ArrayList<>();
        List<InternetAddress> result = new ArrayList<>();
        for (InternetAddress address : addresses) {
            String email = address.getAddress();
            if (!emails.contains(email)) {
                emails.add(email);
                result.add(address);
            }
        }

        return result.toArray(new InternetAddress[0]);
    }

    static Address[] removeGroups(Address[] addresses) {
        if (addresses == null)
            return null;

        List<Address> result = new ArrayList<>();

        for (Address address : addresses) {
            if (address instanceof InternetAddress && ((InternetAddress) address).isGroup())
                continue;
            result.add(address);
        }

        return result.toArray(new Address[0]);
    }

    static void getStructure(Part part, SpannableStringBuilder ssb, int level, int textColorLink) {
        try {
            Enumeration<Header> headers;
            if (level == 0) {
                List<Header> h = new ArrayList<>();

                String[] cte = part.getHeader("Content-Transfer-Encoding");
                if (cte != null)
                    for (String header : cte)
                        h.add(new Header("Content-Transfer-Encoding", header));

                String[] ct = part.getHeader("Content-Type");
                if (ct == null)
                    h.add(new Header("Content-Type", "text/plain"));
                else
                    for (String header : ct)
                        h.add(new Header("Content-Type", header));

                headers = new Enumeration<Header>() {
                    private int index = -1;

                    @Override
                    public boolean hasMoreElements() {
                        return (index + 1 < h.size());
                    }

                    @Override
                    public Header nextElement() {
                        return h.get(++index);
                    }
                };
            } else
                headers = part.getAllHeaders();

            while (headers.hasMoreElements()) {
                Header header = headers.nextElement();
                for (int i = 0; i < level; i++)
                    ssb.append("  ");
                int start = ssb.length();
                ssb.append(header.getName());
                ssb.setSpan(new ForegroundColorSpan(textColorLink), start, ssb.length(), 0);
                ssb.append(": ").append(header.getValue()).append('\n');
            }

            for (int i = 0; i < level; i++)
                ssb.append("  ");
            int size = part.getSize();
            ssb.append("Size: ")
                    .append(size > 0 ? Helper.humanReadableByteCount(size) : "?")
                    .append('\n');

            if (BuildConfig.DEBUG &&
                    !isMimeType(part, "multipart/*")) {
                Object content = part.getContent();
                if (content instanceof String) {
                    String text = (String) content;

                    String charset;
                    try {
                        ContentType ct = new ContentType(part.getContentType());
                        charset = ct.getParameter("charset");
                    } catch (Throwable ignored) {
                        charset = null;
                    }
                    if (charset == null)
                        charset = StandardCharsets.ISO_8859_1.name();

                    Charset cs = Charset.forName(charset);
                    Charset detected = CharsetHelper.detect(text, cs);
                    boolean isUtf8 = CharsetHelper.isUTF8(text.getBytes(cs));
                    boolean isUtf16 = CharsetHelper.isUTF16(text.getBytes(cs));
                    boolean isW1252 = !Objects.equals(text, CharsetHelper.utf8toW1252(text));

                    for (int i = 0; i < level; i++)
                        ssb.append("  ");

                    ssb.append("Detected: ")
                            .append(detected == null ? "?" : detected.toString())
                            .append(" isUTF8=").append(Boolean.toString(isUtf8))
                            .append(" isUTF16=").append(Boolean.toString(isUtf16))
                            .append(" isW1252=").append(Boolean.toString(isW1252))
                            .append('\n');
                }
            }

            ssb.append('\n');

            if (isMimeType(part, "multipart/*")) {
                Multipart multipart = (Multipart) part.getContent();
                for (int i = 0; i < multipart.getCount(); i++)
                    try {
                        getStructure(multipart.getBodyPart(i), ssb, level + 1, textColorLink);
                    } catch (Throwable ex) {
                        Log.w(ex);
                        ssb.append(new ThrowableWrapper(ex).toSafeString()).append('\n');
                    }
            }
        } catch (Throwable ex) {
            Log.w(ex);
            ssb.append(new ThrowableWrapper(ex).toSafeString()).append('\n');
        }
    }


    static boolean isRemoved(Throwable ex) {
        while (ex != null) {
            if (ex instanceof MessageRemovedException ||
                    ex instanceof MessageRemovedIOException)
                return true;
            ex = ex.getCause();
        }
        return false;
    }

    static boolean isNoReply(Address[] addresses) {
        return (addresses != null && isNoReply(Arrays.asList(addresses)));
    }

    static boolean isNoReply(@NonNull List<Address> addresses) {
        for (Address address : addresses)
            if (isNoReply(address))
                return true;
        return false;
    }

    static boolean isNoReply(Address address) {
        if (address instanceof InternetAddress) {
            String email = ((InternetAddress) address).getAddress();
            if (isNoReply(email))
                return true;
        }
        return false;
    }

    static boolean isNoReply(String email) {
        String username = UriHelper.getEmailUser(email);
        if (!TextUtils.isEmpty(username)) {
            username = username.toLowerCase(Locale.ROOT);
            for (String value : DO_NOT_REPLY)
                if (username.contains(value))
                    return true;
        }

        String domain = UriHelper.getEmailDomain(email);
        if (!TextUtils.isEmpty(domain)) {
            domain = domain.toLowerCase(Locale.ROOT);
            for (String value : DO_NOT_REPLY)
                if (domain.startsWith(value))
                    return true;
        }

        return false;
    }

    static Address[] removeAddresses(Address[] addresses, List<Address> removes) {
        if (addresses == null || addresses.length == 0)
            return new Address[0];

        List<Address> result = new ArrayList<>();
        for (Address address : addresses) {
            boolean found = false;
            for (Address remove : removes)
                if (equalEmail(address, remove)) {
                    found = true;
                    break;
                }
            if (!found)
                result.add(address);
        }

        return result.toArray(new Address[0]);
    }

    static boolean equalEmail(Address a1, Address a2) {
        String email1 = ((InternetAddress) a1).getAddress();
        String email2 = ((InternetAddress) a2).getAddress();
        if (email1 != null)
            email1 = email1.toLowerCase(Locale.ROOT);
        if (email2 != null)
            email2 = email2.toLowerCase(Locale.ROOT);
        return Objects.equals(email1, email2);
    }

    static boolean equalEmail(Address[] a1, Address[] a2) {
        if (a1 == null && a2 == null)
            return true;

        if (a1 == null || a2 == null)
            return false;

        if (a1.length != a2.length)
            return false;

        for (int i = 0; i < a1.length; i++)
            if (!equalEmail(a1[i], a2[i]))
                return false;

        return true;
    }

    static String[] equalRootDomain(Context context, Address[] a1, Address[] a2) {
        if (a1 == null || a1.length == 0)
            return null;
        if (a2 == null || a2.length == 0)
            return null;

        for (Address _a1 : a1) {
            String r = UriHelper.getEmailDomain(((InternetAddress) _a1).getAddress());
            if (r == null)
                continue;
            String d1 = UriHelper.getRootDomain(context, r);
            if (d1 == null)
                continue;

            for (Address _a2 : a2) {
                String f = UriHelper.getEmailDomain(((InternetAddress) _a2).getAddress());
                if (f == null)
                    continue;
                String d2 = UriHelper.getRootDomain(context, f);
                if (d2 == null)
                    continue;

                if (!d1.equalsIgnoreCase(d2))
                    return new String[]{d2, d1};
            }
        }

        return null;
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

    static Map<String, String> getKeyValues(String value) {
        Map<String, String> values = new HashMap<>();
        if (TextUtils.isEmpty(value))
            return values;

        String[] params = value.split(";");
        for (String param : params) {
            String k, v;
            int eq = param.indexOf('=');
            if (eq < 0) {
                k = param.trim().toLowerCase(Locale.ROOT);
                v = "";
            } else {
                k = param.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                v = param.substring(eq + 1).trim();
            }
            values.put(k, v);
        }

        return values;
    }

    static class Report {
        String type;
        String reporter;
        String action;
        String recipient;
        String status;
        String diagnostic;
        String disposition;
        String refid;
        String feedback;
        String html;

        Report(String type, String content, Context context) {
            this.type = type;
            StringBuilder report = new StringBuilder();
            report.append("<hr><div style=\"font-family: monospace; font-size: small;\">");
            content = content.replaceAll("(\\r?\\n)+", "\n");
            ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
            try {
                Enumeration<Header> headers = new InternetHeaders(bis, true).getAllHeaders();
                while (headers.hasMoreElements()) {
                    Header header = headers.nextElement();
                    String name = header.getName();
                    String value = header.getValue();
                    value = decodeMime(value);
                    report
                            .append("<strong>")
                            .append(TextUtils.htmlEncode(name))
                            .append("</strong>")
                            .append(": ")
                            .append(TextUtils.htmlEncode(value))
                            .append("<br>");

                    if (isDeliveryStatus(type)) {
                        // https://datatracker.ietf.org/doc/html/rfc3464#section-2.3
                        switch (name) {
                            case "Reporting-MTA":
                                this.reporter = value;
                                break;
                            case "Action":
                                this.action = value;
                                break;
                            case "Final-Recipient":
                                this.recipient = value;
                                break;
                            case "Status":
                                // https://www.iana.org/assignments/smtp-enhanced-status-codes/smtp-enhanced-status-codes.xhtml
                                this.status = value;
                                break;
                            case "Diagnostic-Code":
                                this.diagnostic = value;
                                break;
                            case "X-Original-Message-ID":
                                // GMail
                                this.refid = value;
                                break;
                        }
                    } else if (isDispositionNotification(type)) {
                        // https://datatracker.ietf.org/doc/html/rfc3798#section-3.2.6
                        switch (name) {
                            case "Reporting-UA":
                                this.reporter = value;
                                break;
                            case "Original-Recipient":
                                this.recipient = value;
                                break;
                            case "Disposition":
                                this.disposition = value;
                                break;
                            case "Original-Message-ID":
                                this.refid = value;
                                break;
                        }
                    } else if (isFeedbackReport(type)) {
                        // https://datatracker.ietf.org/doc/html/rfc5965
                        feedback = "complaint";
                        switch (name) {
                            case "Feedback-Type":
                                // abuse, fraud, other, virus
                                feedback = value;
                                break;
                        }
                    }
                }
            } catch (Throwable ex) {
                Log.e(ex);
                report.append(TextUtils.htmlEncode(new ThrowableWrapper(ex).toSafeString()));
            }

            report.append("</div>");

            if (isDeliveryStatus() && !isDelivered())
                report.append("<br><div style=\"font-size: small; font-style: italic;\">")
                        .append(TextUtils.htmlEncode(context.getString(R.string.title_report_remark)))
                        .append("</div>");

            this.html = report.toString();
        }

        boolean isDeliveryStatus() {
            return isDeliveryStatus(type);
        }

        boolean isDispositionNotification() {
            return isDispositionNotification(type);
        }

        boolean isFeedbackReport() {
            return isFeedbackReport(type);
        }

        boolean isDelivered() {
            return ("delivered".equals(action) || "relayed".equals(action) || "expanded".equals(action));
        }

        boolean isDelayed() {
            return "delayed".equals(action);
        }

        boolean isMdnManual() {
            return "manual-action".equals(getAction(0));
        }

        boolean isMdnAutomatic() {
            return "automatic-action".equals(getAction(0));
        }

        boolean isMdnManualSent() {
            return "MDN-sent-manually".equals(getAction(1));
        }

        boolean isMdnAutomaticSent() {
            return "MDN-sent-automatically".equals(getAction(1));
        }

        boolean isMdnDisplayed() {
            return "displayed".equalsIgnoreCase(getType());
        }

        boolean isMdnDeleted() {
            return "deleted".equalsIgnoreCase(getType());
        }

        private String getAction(int index) {
            if (disposition == null)
                return null;
            int semi = disposition.lastIndexOf(';');
            if (semi < 0)
                return null;
            String[] action = disposition.substring(0, semi).trim().split("/");
            return (index < action.length ? action[index] : null);
        }

        private String getType() {
            // manual-action/MDN-sent-manually; displayed
            // automatic-action/MDN-sent-automatically; deleted
            if (disposition == null)
                return null;
            int semi = disposition.lastIndexOf(';');
            if (semi < 0)
                return null;
            return disposition.substring(semi + 1).trim();
        }

        static boolean isDeliveryStatus(String type) {
            return "message/delivery-status".equalsIgnoreCase(type);
        }

        static boolean isDispositionNotification(String type) {
            return "message/disposition-notification".equalsIgnoreCase(type);
        }

        static boolean isFeedbackReport(String type) {
            return "message/feedback-report".equalsIgnoreCase(type);
        }
    }

    static class StripStream extends FilterInputStream {
        protected StripStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b == ' ') {
                super.mark(1000);
                while (true) {
                    b = super.read();
                    if (b != ' ') {
                        if (b == '\r' || b == '\n')
                            return b;
                        else {
                            super.reset();
                            return ' ';
                        }
                    }
                }
            } else
                return b;
        }

        @Override
        public int read(byte[] b) throws IOException {
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) read();
                if (b[i] < 0)
                    return i;
            }
            return b.length;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    static class CanonicalizingStream extends FilterOutputStream {
        private OutputStream os;
        private int content;
        private String boundary;

        private int boundaries = 0;
        private boolean carriage = false;
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.ISO_8859_1);

        // PGP: https://datatracker.ietf.org/doc/html/rfc3156#section-5
        // S/MIME: https://datatracker.ietf.org/doc/html/rfc8551#section-3.1.1

        public CanonicalizingStream(OutputStream out, int content, String boundary) {
            super(out);
            this.os = out;
            this.content = content;
            this.boundary = (boundary == null ? null : "--" + boundary);
        }

        @Override
        public void write(int b) throws IOException {
            this.write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                byte k = b[i];
                if (k == '\r')
                    carriage = true;
                else {
                    if (k == '\n') {
                        if (writeBuffer())
                            buffer.write(CRLF);
                    } else {
                        if (carriage) {
                            if (writeBuffer())
                                buffer.write(CRLF);
                        }
                        buffer.write(k);
                    }
                    carriage = false;
                }
            }
        }

        @Override
        public void flush() throws IOException {
            flushBuffer();
            super.flush();
        }

        @Override
        public void close() throws IOException {
            flushBuffer();
            super.close();
        }

        private boolean writeBuffer() throws IOException {
            try {
                String line = new String(buffer.toByteArray(), StandardCharsets.ISO_8859_1);

                if (boundary != null) {
                    if (boundary.equals(line.trim())) {
                        boundaries++;
                        return false;
                    }
                    if (boundaries != 1)
                        return false;
                }

                if (/*EntityAttachment.PGP_CONTENT.equals(content) ||*/ boundary == null)
                    line = line.replaceAll(" +$", "");

                os.write(line.getBytes(StandardCharsets.ISO_8859_1));

                return true;
            } finally {
                buffer.reset();
            }
        }

        private void flushBuffer() throws IOException {
            if (boundary != null && boundaries < 1)
                throw new IOException("Signed part missing");
            if (buffer.size() > 0)
                writeBuffer();
        }
    }
}
