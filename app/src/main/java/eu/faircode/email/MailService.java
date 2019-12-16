package eu.faircode.email;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.MailSSLSocketFactory;

import org.bouncycastle.asn1.x509.GeneralName;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.StoreListener;
import javax.net.ssl.SSLSocket;

public class MailService implements AutoCloseable {
    private Context context;
    private String protocol;
    private boolean useip;
    private boolean debug;
    private String trustedFingerprint;
    private Properties properties;
    private Session isession;
    private Service iservice;
    private X509Certificate certificate;
    private StoreListener listener;

    private ExecutorService executor = Helper.getBackgroundExecutor(0, "mail");

    static final int AUTH_TYPE_PASSWORD = 1;
    static final int AUTH_TYPE_GMAIL = 2;
    static final int AUTH_TYPE_OUTLOOK = 3;

    private final static int CHECK_TIMEOUT = 15 * 1000; // milliseconds
    private final static int CONNECT_TIMEOUT = 20 * 1000; // milliseconds
    private final static int WRITE_TIMEOUT = 60 * 1000; // milliseconds
    private final static int READ_TIMEOUT = 60 * 1000; // milliseconds
    private final static int FETCH_SIZE = 256 * 1024; // bytes, default 16K
    private final static int POOL_TIMEOUT = 45 * 1000; // milliseconds, default 45 sec

    private static final int APPEND_BUFFER_SIZE = 4 * 1024 * 1024; // bytes

    private MailService() {
    }

    MailService(Context context, String protocol, String realm, boolean insecure, boolean check, boolean debug) throws NoSuchProviderException {
        this.context = context.getApplicationContext();
        this.protocol = protocol;
        this.debug = debug;

        properties = MessageHelper.getSessionProperties();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean socks_enabled = prefs.getBoolean("socks_enabled", false);
        String socks_proxy = prefs.getString("socks_proxy", "localhost:9050");

        if (BuildConfig.DEBUG)
            try {
                // openssl s_client -connect host:port < /dev/null 2>/dev/null | openssl x509 -fingerprint -noout -in /dev/stdin
                MailSSLSocketFactory sf = new MailSSLSocketFactory() {
                    @Override
                    public synchronized boolean isServerTrusted(String server, SSLSocket sslSocket) {
                        try {
                            Certificate[] certificates = sslSocket.getSession().getPeerCertificates();
                            if (certificates == null || certificates.length == 0 ||
                                    !(certificates[0] instanceof X509Certificate))
                                return false;

                            certificate = (X509Certificate) certificates[0];

                            boolean trusted = false;

                            String name = getDnsName(certificate);
                            if (name != null && matches(server, name))
                                trusted = true;

                            if (getFingerPrint(certificate).equals(trustedFingerprint))
                                trusted = true;

                            Log.i("Is trusted? server=" + server + " trusted=" + trusted);
                            return trusted;
                        } catch (Throwable ex) {
                            Log.e(ex);
                            return false;
                        }
                    }
                };

                properties.put("mail." + protocol + ".ssl.socketFactory", sf);
                properties.put("mail." + protocol + ".socketFactory.fallback", "false");
            } catch (GeneralSecurityException ex) {
                Log.e(ex);
            }

        // SOCKS proxy
        if (socks_enabled) {
            String[] address = socks_proxy.split(":");
            String host = (address.length > 0 ? address[0] : null);
            String port = (address.length > 1 ? address[1] : null);
            if (TextUtils.isEmpty(host))
                host = "localhost";
            if (TextUtils.isEmpty(port))
                port = "9050";
            properties.put("mail." + protocol + ".socks.host", host);
            properties.put("mail." + protocol + ".socks.port", port);
            Log.i("Using SOCKS proxy=" + host + ":" + port);
        }

        properties.put("mail.event.scope", "folder");
        properties.put("mail.event.executor", executor);

        properties.put("mail." + protocol + ".sasl.realm", realm == null ? "" : realm);
        properties.put("mail." + protocol + ".auth.ntlm.domain", realm == null ? "" : realm);

        // TODO: make timeouts configurable?
        properties.put("mail." + protocol + ".connectiontimeout", Integer.toString(check ? CHECK_TIMEOUT : CONNECT_TIMEOUT));
        properties.put("mail." + protocol + ".writetimeout", Integer.toString(check ? CHECK_TIMEOUT : WRITE_TIMEOUT)); // one thread overhead
        properties.put("mail." + protocol + ".timeout", Integer.toString(check ? CHECK_TIMEOUT : READ_TIMEOUT));

        if (debug && BuildConfig.DEBUG)
            properties.put("mail.debug.auth", "true");

        if ("pop3".equals(protocol) || "pop3s".equals(protocol)) {
            this.debug = true;

            // https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html#properties
            properties.put("mail." + protocol + ".ssl.checkserveridentity", Boolean.toString(!insecure));
            properties.put("mail." + protocol + ".ssl.trust", "*");

            properties.put("mail.pop3s.starttls.enable", "false");

            properties.put("mail.pop3.starttls.enable", "true");
            properties.put("mail.pop3.starttls.required", Boolean.toString(!insecure));

        } else if ("imap".equals(protocol) || "imaps".equals(protocol)) {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#properties
            properties.put("mail.imaps.starttls.enable", "false");

            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.starttls.required", Boolean.toString(!insecure));

            properties.put("mail." + protocol + ".separatestoreconnection", "true");
            properties.put("mail." + protocol + ".connectionpool.debug", "true");
            properties.put("mail." + protocol + ".connectionpoolsize", "1");
            properties.put("mail." + protocol + ".connectionpooltimeout", Integer.toString(POOL_TIMEOUT));

            properties.put("mail." + protocol + ".finalizecleanclose", "false");
            //properties.put("mail." + protocol + ".closefoldersonstorefailure", "false");

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
            properties.put("mail.smtps.starttls.enable", "false");

            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", Boolean.toString(!insecure));

            properties.put("mail." + protocol + ".auth", "true");

        } else
            throw new NoSuchProviderException(protocol);
    }

    void setPartialFetch(boolean enabled) {
        properties.put("mail." + protocol + ".partialfetch", Boolean.toString(enabled));
    }

    void setIgnoreBodyStructureSize(boolean enabled) {
        properties.put("mail." + protocol + ".ignorebodystructuresize", Boolean.toString(enabled));
    }

    void setUseIp(boolean enabled) {
        useip = enabled;
    }

    void setLeaveOnServer(boolean keep) {
        properties.put("mail." + protocol + ".rsetbeforequit", Boolean.toString(keep));
    }

    void setListener(StoreListener listener) {
        this.listener = listener;
    }

    public void connect(EntityAccount account) throws MessagingException {
        String password = connect(account.host, account.port, account.auth_type, account.user, account.password, account.fingerprint);
        if (password != null) {
            DB db = DB.getInstance(context);
            int count = db.account().setAccountPassword(account.id, account.password);
            Log.i(account.name + " token refreshed=" + count);
        }
    }

    public void connect(EntityIdentity identity) throws MessagingException {
        String password = connect(identity.host, identity.port, identity.auth_type, identity.user, identity.password, identity.fingerprint);
        if (password != null) {
            DB db = DB.getInstance(context);
            int count = db.identity().setIdentityPassword(identity.id, identity.password);
            Log.i(identity.email + " token refreshed=" + count);
        }
    }

    public String connect(String host, int port, int auth, String user, String password, String fingerprint) throws MessagingException {
        this.trustedFingerprint = fingerprint;
        try {
            if (auth == AUTH_TYPE_GMAIL || auth == AUTH_TYPE_OUTLOOK)
                properties.put("mail." + protocol + ".auth.mechanisms", "XOAUTH2");

            //if (BuildConfig.DEBUG)
            //    throw new MailConnectException(new SocketConnectException("Debug", new Exception(), host, port, 0));

            _connect(context, host, port, user, password);
            return null;
        } catch (AuthenticationFailedException ex) {
            // Refresh token
            if (auth == AUTH_TYPE_GMAIL)
                try {
                    String type = "com.google";
                    AccountManager am = AccountManager.get(context);
                    Account[] accounts = am.getAccountsByType(type);
                    for (Account account : accounts)
                        if (user.equals(account.name)) {
                            Log.i("Refreshing token user=" + user);
                            am.invalidateAuthToken(type, password);
                            String token = am.blockingGetAuthToken(account, getAuthTokenType(type), true);
                            if (token == null)
                                throw new IllegalArgumentException("No token on refresh");

                            _connect(context, host, port, user, token);
                            return token;
                        }

                    throw new IllegalArgumentException("Account not found");
                } catch (Exception ex1) {
                    Log.e(ex1);
                    throw new AuthenticationFailedException(ex.getMessage(), ex1);
                }
            else
                throw ex;
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
                            return null;
                        } catch (MessagingException ex1) {
                            Log.w(ex1);
                        }
            } catch (Throwable ex1) {
                Log.w(ex1);
            }

            throw ex;
        } catch (MessagingException ex) {
            if (BuildConfig.DEBUG &&
                    ex.getCause() instanceof IOException &&
                    ex.getCause().getMessage() != null &&
                    ex.getCause().getMessage().startsWith("Server is not trusted:")) {
                String sfingerprint;
                try {
                    sfingerprint = getFingerPrint(certificate);
                } catch (Throwable ex1) {
                    Log.e(ex1);
                    throw ex;
                }
                throw new UntrustedException(sfingerprint, ex);
            } else
                throw ex;
        }
    }

    private void _connect(Context context, String host, int port, String user, String password) throws MessagingException {
        isession = Session.getInstance(properties, null);
        isession.setDebug(debug);
        //System.setProperty("mail.socket.debug", Boolean.toString(debug));

        if ("pop3".equals(protocol) || "pop3s".equals(protocol)) {
            isession.setDebug(true);
            iservice = isession.getStore(protocol);
            iservice.connect(host, port, user, password);

        } else if ("imap".equals(protocol) || "imaps".equals(protocol)) {
            iservice = isession.getStore(protocol);
            if (listener != null)
                ((IMAPStore) iservice).addStoreListener(listener);
            iservice.connect(host, port, user, password);

            // https://www.ietf.org/rfc/rfc2971.txt
            IMAPStore istore = (IMAPStore) getStore();
            if (istore.hasCapability("ID"))
                try {
                    Map<String, String> id = new LinkedHashMap<>();
                    id.put("name", context.getString(R.string.app_name));
                    id.put("version", BuildConfig.VERSION_NAME);
                    Map<String, String> sid = istore.id(id);
                    if (sid != null) {
                        Map<String, String> crumb = new HashMap<>();
                        for (String key : sid.keySet()) {
                            crumb.put(key, sid.get(key));
                            EntityLog.log(context, "Server " + key + "=" + sid.get(key));
                        }
                        Log.breadcrumb("server", crumb);
                    }
                } catch (MessagingException ex) {
                    Log.w(ex);
                }

        } else if ("smtp".equals(protocol) || "smtps".equals(protocol)) {
            String[] c = BuildConfig.APPLICATION_ID.split("\\.");
            Collections.reverse(Arrays.asList(c));
            String haddr = TextUtils.join(".", c);

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

    static String getAuthTokenType(String type) {
        // https://developers.google.com/gmail/imap/xoauth2-protocol
        if ("com.google".equals(type))
            return "oauth2:https://mail.google.com/";
        return null;
    }

    List<EntityFolder> getFolders() throws MessagingException {
        List<EntityFolder> folders = new ArrayList<>();
        List<EntityFolder> guesses = new ArrayList<>();

        for (Folder ifolder : getStore().getDefaultFolder().list("*")) {
            String fullName = ifolder.getFullName();
            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
            String type = EntityFolder.getType(attrs, fullName, true);
            Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs) + " type=" + type);

            if (type != null) {
                EntityFolder folder = new EntityFolder(fullName, type);
                folders.add(folder);

                if (EntityFolder.USER.equals(type)) {
                    String guess = EntityFolder.guessType(fullName);
                    if (guess != null)
                        guesses.add(folder);
                }
            }
        }

        for (EntityFolder guess : guesses) {
            boolean has = false;
            String gtype = EntityFolder.guessType(guess.name);
            for (EntityFolder folder : folders)
                if (folder.type.equals(gtype)) {
                    has = true;
                    break;
                }
            if (!has) {
                guess.type = gtype;
                Log.i(guess.name + " guessed type=" + gtype);
            }
        }

        boolean inbox = false;
        boolean drafts = false;
        for (EntityFolder folder : folders)
            if (EntityFolder.INBOX.equals(folder.type))
                inbox = true;
            else if (EntityFolder.DRAFTS.equals(folder.type))
                drafts = true;

        if (!inbox || !drafts)
            return null;

        return folders;
    }

    Store getStore() {
        return (Store) iservice;
    }

    SMTPTransport getTransport() {
        return (SMTPTransport) iservice;
    }

    boolean hasCapability(String capability) throws MessagingException {
        Store store = getStore();
        if (store instanceof IMAPStore)
            return ((IMAPStore) getStore()).hasCapability(capability);
        else
            return false;
    }

    public void close() throws MessagingException {
        try {
            if (iservice != null && iservice.isConnected())
                iservice.close();
        } finally {
            context = null;
        }
    }

    private static String getDnsName(X509Certificate certificate) throws CertificateParsingException {
        Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
        if (altNames == null)
            return null;

        for (List altName : altNames)
            if (altName.get(0).equals(GeneralName.dNSName))
                return (String) altName.get(1);

        return null;
    }

    private static String getFingerPrint(X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException {
        return Helper.sha1(certificate.getEncoded());
    }

    private static boolean matches(String server, String name) {
        if (name.startsWith("*.")) {
            // Wildcard certificate
            String domain = name.substring(2);
            if (TextUtils.isEmpty(domain))
                return false;

            int dot = server.indexOf(".");
            if (dot < 0)
                return false;

            String cdomain = server.substring(dot + 1);
            if (TextUtils.isEmpty(cdomain))
                return false;

            Log.i("Trust " + domain + " =? " + cdomain);
            return domain.equalsIgnoreCase(cdomain);
        } else {
            Log.i("Trust " + server + " =? " + name);
            return server.equalsIgnoreCase(name);
        }
    }

    class UntrustedException extends MessagingException {
        private String fingerprint;

        UntrustedException(@NonNull String fingerprint, @NonNull Exception cause) {
            super(cause.getMessage(), cause);
            this.fingerprint = fingerprint;
        }

        String getFingerprint() {
            return fingerprint;
        }

        @Override
        public synchronized String toString() {
            return getCause().toString();
        }
    }
}