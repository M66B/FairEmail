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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

    static MimeMessage from(EntityMessage message, Session isession) throws MessagingException {
        MimeMessage imessage = new MimeMessage(isession);

        if (message.from != null)
            imessage.setFrom(MessageHelper.decodeAddresses(message.from)[0]);

        if (message.to != null)
            imessage.setRecipients(Message.RecipientType.TO, MessageHelper.decodeAddresses(message.to));

        if (message.cc != null)
            imessage.setRecipients(Message.RecipientType.CC, MessageHelper.decodeAddresses(message.to));

        if (message.subject != null)
            imessage.setSubject(message.subject);

        if (message.body != null)
            imessage.setText(message.body, null, "html");

        imessage.setSentDate(new Date());

        return imessage;
    }

    static MimeMessage from(EntityMessage message, EntityMessage reply, Session isession) throws MessagingException {
        MimeMessage imessage = from(message, isession);
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

    String getFrom() throws MessagingException, JSONException {
        return encodeAddresses(imessage.getFrom());
    }

    String getTo() throws MessagingException, JSONException {
        return encodeAddresses(imessage.getRecipients(Message.RecipientType.TO));
    }

    String getCc() throws MessagingException, JSONException {
        return encodeAddresses(imessage.getRecipients(Message.RecipientType.CC));
    }

    String getReply() throws MessagingException, JSONException {
        return encodeAddresses(imessage.getReplyTo());
    }

    static String encodeAddresses(Address[] addresses) throws JSONException {
        JSONArray jaddresses = new JSONArray();
        if (addresses != null)
            for (Address address : addresses)
                if (address instanceof InternetAddress) {
                    String a = ((InternetAddress) address).getAddress();
                    String p = ((InternetAddress) address).getPersonal();
                    JSONObject jaddress = new JSONObject();
                    if (a != null)
                        jaddress.put("address", a);
                    if (p != null)
                        jaddress.put("personal", p);
                    jaddresses.put(jaddress);
                }
        return jaddresses.toString();
    }

    static Address[] decodeAddresses(String json) {
        List<Address> result = new ArrayList<>();
        if (json != null)
            try {
                JSONArray jaddresses = new JSONArray(json);
                for (int i = 0; i < jaddresses.length(); i++) {
                    JSONObject jaddress = (JSONObject) jaddresses.get(i);
                    if (jaddress.has("personal"))
                        result.add(new InternetAddress(
                                jaddress.getString("address"),
                                jaddress.getString("personal")));
                    else
                        result.add(new InternetAddress(
                                jaddress.getString("address")));
                }
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
        return result.toArray(new Address[0]);
    }

    String getHtml() throws MessagingException {
        return getHtml(imessage);
    }

    static String getFormattedAddresses(String json) {
        try {
            List<String> addresses = new ArrayList<>();
            for (Address address : decodeAddresses(json))
                if (address instanceof InternetAddress) {
                    InternetAddress a = (InternetAddress) address;
                    String personal = a.getPersonal();
                    if (TextUtils.isEmpty(personal))
                        addresses.add(address.toString());
                    else
                        addresses.add(personal);
                } else
                    addresses.add(address.toString());
            return TextUtils.join(", ", addresses);
        } catch (Throwable ex) {
            return ex.getMessage();
        }
    }

    private String getHtml(Part part) throws MessagingException {
        if (part.isMimeType("text/*"))
            try {
                String s = (String) part.getContent();
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

    boolean getSeen() throws MessagingException {
        return imessage.isSet(Flags.Flag.SEEN);
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
