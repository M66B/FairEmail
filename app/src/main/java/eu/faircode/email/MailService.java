package eu.faircode.email;

import android.content.Context;

import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailConnectException;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Service;
import javax.mail.Session;

public class MailService implements AutoCloseable {
    private Context context;
    private String protocol;
    private boolean useip;
    private boolean debug;
    private Properties properties;
    private Session isession;
    private Service iservice;

    private ExecutorService executor = Executors.newCachedThreadPool(Helper.backgroundThreadFactory);

    private final static int CONNECT_TIMEOUT = 20 * 1000; // milliseconds
    private final static int WRITE_TIMEOUT = 60 * 1000; // milliseconds
    private final static int READ_TIMEOUT = 60 * 1000; // milliseconds
    private final static int FETCH_SIZE = 256 * 1024; // bytes, default 16K
    private final static int POOL_TIMEOUT = 45 * 1000; // milliseconds, default 45 sec

    private static final int APPEND_BUFFER_SIZE = 4 * 1024 * 1024; // bytes

    private MailService() {
    }

    MailService(Context context, String protocol, String realm, boolean insecure, boolean debug) throws NoSuchProviderException {
        this.context = context.getApplicationContext();
        this.protocol = protocol;
        this.debug = debug;
        properties = MessageHelper.getSessionProperties();

        properties.put("mail.event.scope", "folder");
        properties.put("mail.event.executor", executor);

        String checkserveridentity = Boolean.toString(!insecure).toLowerCase();

        if ("imap".equals(protocol) || "imaps".equals(protocol)) {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#properties
            properties.put("mail." + protocol + ".ssl.checkserveridentity", checkserveridentity);
            properties.put("mail." + protocol + ".ssl.trust", "*");

            properties.put("mail.imaps.starttls.enable", "false");

            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.starttls.required", "true");

            if (realm != null)
                properties.put("mail." + protocol + ".auth.ntlm.domain", realm);

            // TODO: make timeouts configurable?
            properties.put("mail." + protocol + ".connectiontimeout", Integer.toString(CONNECT_TIMEOUT));
            properties.put("mail." + protocol + ".writetimeout", Integer.toString(WRITE_TIMEOUT)); // one thread overhead
            properties.put("mail." + protocol + ".timeout", Integer.toString(READ_TIMEOUT));

            properties.put("mail." + protocol + ".connectionpool.debug", "true");
            properties.put("mail." + protocol + ".connectionpoolsize", "2");
            properties.put("mail." + protocol + ".connectionpooltimeout", Integer.toString(POOL_TIMEOUT));

            properties.put("mail." + protocol + ".finalizecleanclose", "false");

            // https://tools.ietf.org/html/rfc4978
            // https://docs.oracle.com/javase/8/docs/api/java/util/zip/Deflater.html
            properties.put("mail." + protocol + ".compress.enable", "true");
            //properties.put("mail.imaps.compress.level", "-1");
            //properties.put("mail.imaps.compress.strategy", "0");

            properties.put("mail." + protocol + ".throwsearchexception", "true");
            properties.put("mail." + protocol + ".fetchsize", Integer.toString(FETCH_SIZE));
            properties.put("mail." + protocol + ".peek", "true");
            properties.put("mail." + protocol + ".appendbuffersize", Integer.toString(APPEND_BUFFER_SIZE));

        } else if ("smtp".equals(protocol) || "smtps".equals(protocol)) {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html#properties
            properties.put("mail." + protocol + ".ssl.checkserveridentity", checkserveridentity);
            properties.put("mail." + protocol + ".ssl.trust", "*");

            properties.put("mail.smtps.starttls.enable", "false");

            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");

            properties.put("mail." + protocol + ".auth", "true");

            if (realm != null)
                properties.put("mail." + protocol + ".auth.ntlm.domain", realm);

            properties.put("mail." + protocol + ".connectiontimeout", Integer.toString(CONNECT_TIMEOUT));
            properties.put("mail." + protocol + ".writetimeout", Integer.toString(WRITE_TIMEOUT)); // one thread overhead
            properties.put("mail." + protocol + ".timeout", Integer.toString(READ_TIMEOUT));

        } else
            throw new NoSuchProviderException(protocol);
    }

    void setPartialFetch(boolean enabled) {
        if (!enabled)
            properties.put("mail." + protocol + ".partialfetch", "false");
    }

    void setUseIp(boolean enabled) {
        useip = enabled;
    }

    void setSeparateStoreConnection() {
        properties.put("mail." + protocol + ".separatestoreconnection", "true");
    }

    public void connect(EntityAccount account) throws MessagingException {
        connect(account.host, account.port, account.user, account.password);
    }

    public void connect(EntityIdentity identity) throws MessagingException {
        connect(identity.host, identity.port, identity.user, identity.password);
    }

    public void connect(String host, int port, String user, String password) throws MessagingException {
        try {
            //if (BuildConfig.DEBUG)
            //    throw new MailConnectException(new SocketConnectException("Debug", new Exception(), host, port, 0));
            _connect(context, host, port, user, password);
        } catch (MailConnectException ex) {
            try {
                // Some devices resolve IPv6 addresses while not having IPv6 connectivity
                properties.put("mail." + protocol + ".ssl.checkserveridentity", "false");
                InetAddress[] iaddrs = InetAddress.getAllByName(host);
                if (iaddrs.length > 1)
                    for (InetAddress iaddr : iaddrs)
                        try {
                            Log.i("Falling back to " + iaddr.getHostAddress());
                            _connect(context, iaddr.getHostAddress(), port, user, password);
                            return;
                        } catch (MessagingException ex1) {
                            Log.w(ex1);
                        }
            } catch (Throwable ex1) {
                Log.w(ex1);
            }

            throw ex;
        }
    }

    private void _connect(Context context, String host, int port, String user, String password) throws MessagingException {
        isession = Session.getInstance(properties, null);
        isession.setDebug(debug);
        //System.setProperty("mail.socket.debug", Boolean.toString(debug));

        if ("imap".equals(protocol) || "imaps".equals(protocol)) {
            iservice = isession.getStore(protocol);
            iservice.connect(host, port, user, password);

            // https://www.ietf.org/rfc/rfc2971.txt
            if (getStore().hasCapability("ID"))
                try {
                    Map<String, String> id = new LinkedHashMap<>();
                    id.put("name", context.getString(R.string.app_name));
                    id.put("version", BuildConfig.VERSION_NAME);
                    Map<String, String> sid = getStore().id(id);
                    if (sid != null) {
                        Map<String, String> crumb = new HashMap<>();
                        for (String key : sid.keySet()) {
                            crumb.put(key, sid.get(key));
                            EntityLog.log(context, "Server " + key + "=" + sid.get(key));
                        }
                        Bugsnag.leaveBreadcrumb("server", BreadcrumbType.LOG, crumb);
                    }
                } catch (MessagingException ex) {
                    Log.w(ex);
                }

        } else if ("smtp".equals(protocol) || "smtps".equals(protocol)) {
            String haddr = host;

            if (useip)
                try {
                    // This assumes getByName always returns the same address (type)
                    InetAddress addr = InetAddress.getByName(host);
                    if (addr instanceof Inet4Address)
                        haddr = "[" + Inet4Address.getLocalHost().getHostAddress() + "]";
                    else
                        haddr = "[IPv6:" + Inet6Address.getLocalHost().getHostAddress() + "]";
                } catch (UnknownHostException ex) {
                    Log.w(ex);
                }

            Log.i("Using localhost=" + haddr);
            properties.put("mail." + protocol + ".localhost", haddr);

            iservice = isession.getTransport(protocol);
            iservice.connect(host, port, user, password);
        } else
            throw new NoSuchProviderException(protocol);
    }

    IMAPStore getStore() {
        return (IMAPStore) iservice;
    }

    SMTPTransport getTransport() {
        return (SMTPTransport) iservice;
    }

    public void close() throws MessagingException {
        try {
            if (iservice != null)
                iservice.close();
        } finally {
            context = null;
        }
    }
}