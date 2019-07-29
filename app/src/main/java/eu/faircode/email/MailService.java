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

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Service;
import javax.mail.Session;

public class MailService implements AutoCloseable {
    private Context context;
    private String protocol;
    private boolean debug;
    private Properties properties;
    private Session isession;
    private Service iservice;

    private static final String any4 = "0.0.0.0";

    private MailService() {
    }

    MailService(Context context, String protocol, String realm, boolean insecure, boolean debug) {
        this.context = context;
        this.protocol = protocol;
        this.debug = debug;
        this.properties = MessageHelper.getSessionProperties(realm, insecure);
    }

    void setPartialFetch(boolean enabled) {
        if (!enabled)
            this.properties.put("mail." + this.protocol + ".partialfetch", "false");
    }

    void setUseIp(boolean enabled, String host) throws UnknownHostException {
        String haddr;
        if (enabled) {
            InetAddress addr = InetAddress.getByName(host);
            if (addr instanceof Inet4Address)
                haddr = "[" + Inet4Address.getLocalHost().getHostAddress() + "]";
            else
                haddr = "[IPv6:" + Inet6Address.getLocalHost().getHostAddress() + "]";
        } else
            haddr = host;

        Log.i("Send localhost=" + haddr);
        this.properties.put("mail." + this.protocol + ".localhost", haddr);
    }

    void setSeparateStoreConnection() {
        this.properties.put("mail." + this.protocol + ".separatestoreconnection", "true");
    }

    public void connect(EntityAccount account) throws MessagingException {
        connect(account.host, account.port, account.user, account.password);
    }

    public void connect(EntityIdentity identity) throws MessagingException {
        connect(identity.host, identity.port, identity.user, identity.password);
    }

    public void connect(String host, int port, String user, String password) throws MessagingException {
        try {
            _connect(context, host, port, user, password);
        } catch (MailConnectException ex) {
            if (!hasIPv6(host))
                throw ex;

            try {
                Log.i("Binding to " + any4);
                properties.put("mail.imap.localaddress", any4);
                properties.put("mail.imaps.localaddress", any4);
                properties.put("mail.smtp.localaddress", any4);
                properties.put("mail.smtps.localaddress", any4);
                _connect(context, host, port, user, password);
            } catch (Throwable ex1) {
                Log.w(ex1);
                throw ex;
            }
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
            iservice = isession.getTransport(protocol);
            iservice.connect(host, port, user, password);
        } else
            throw new NoSuchProviderException(protocol);
    }

    private static boolean hasIPv6(String host) {
        boolean has = false;
        try {
            for (InetAddress iaddr : InetAddress.getAllByName(host)) {
                Log.i(host + " resolves to " + iaddr);
                if (iaddr instanceof Inet6Address)
                    has = true;
            }
        } catch (UnknownHostException ignored) {
        }
        return has;
    }

    IMAPStore getStore() {
        return (IMAPStore) this.iservice;
    }

    SMTPTransport getTransport() {
        return (SMTPTransport) this.iservice;
    }

    public void close() throws MessagingException {
        try {
            if (iservice != null)
                iservice.close();
        } finally {
            this.context = null;
            this.properties = null;
            this.isession = null;
            this.iservice = null;
        }
    }
}