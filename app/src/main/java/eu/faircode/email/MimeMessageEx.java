package eu.faircode.email;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class MimeMessageEx extends MimeMessage {
    private long id = -1;

    public MimeMessageEx(Session session, long id) {
        super(session);
        this.id = id;
    }

    @Override
    protected void updateMessageID() throws MessagingException {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append('<')
                    .append(id).append('.')
                    .append(BuildConfig.APPLICATION_ID).append('.')
                    .append(System.currentTimeMillis()).append('.')
                    .append("anonymous@localhost")
                    .append('>');

            setHeader("Message-ID", sb.toString());
            Log.i(Helper.TAG, "Override Message-ID=" + sb.toString());
        } catch (Throwable ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            super.updateMessageID();
        }
    }

    public static long getId(MimeMessage message) {
        try {
            String msgid = message.getMessageID();
            if (msgid == null)
                return -1;

            List<String> parts = new ArrayList<>(Arrays.asList(msgid.split("\\.")));
            if (parts.size() < 1)
                return -1;

            String part = parts.get(0);
            parts.remove(0);
            if (!TextUtils.join(".", parts).startsWith(BuildConfig.APPLICATION_ID))
                return -1;

            long id = Long.parseLong(part.substring(1));
            Log.i(Helper.TAG, "Parsed Message-ID=" + msgid + " id=" + id);
            return id;
        } catch (Throwable ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            return -1;
        }
    }
}
