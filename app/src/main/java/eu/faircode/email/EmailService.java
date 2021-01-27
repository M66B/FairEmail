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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;
import android.security.KeyChain;
import android.system.ErrnoException;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.sun.mail.gimap.GmailSSLProvider;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.SocketConnectException;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.StoreListener;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_GMAIL;
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_OAUTH;

// IMAP standards: https://imapwiki.org/Specs

public class EmailService implements AutoCloseable {
    private Context context;
    private String protocol;
    private boolean insecure;
    private int purpose;
    private boolean harden;
    private boolean useip;
    private String ehlo;
    private boolean log;
    private boolean debug;
    private Properties properties;
    private Session isession;
    private Service iservice;
    private StoreListener listener;

    private ExecutorService executor = Helper.getBackgroundExecutor(0, "mail");

    static final int PURPOSE_CHECK = 1;
    static final int PURPOSE_USE = 2;
    static final int PURPOSE_SEARCH = 3;

    static final int ENCRYPTION_SSL = 0;
    static final int ENCRYPTION_STARTTLS = 1;
    static final int ENCRYPTION_NONE = 2;

    final static int DEFAULT_CONNECT_TIMEOUT = 20; // seconds
    final static boolean SEPARATE_STORE_CONNECTION = false;

    private final static int SEARCH_TIMEOUT = 90 * 1000; // milliseconds
    private final static int FETCH_SIZE = 1024 * 1024; // bytes, default 16K
    private final static int POOL_SIZE = 1; // connections
    private final static int POOL_TIMEOUT = 45 * 1000; // milliseconds, default 45 sec

    private final static int TCP_KEEP_ALIVE_INTERVAL = 9 * 60; // seconds

    private static final int APPEND_BUFFER_SIZE = 4 * 1024 * 1024; // bytes

    // https://developer.android.com/reference/javax/net/ssl/SSLSocket.html#protocols
    private static final List<String> SSL_PROTOCOL_BLACKLIST = Collections.unmodifiableList(Arrays.asList(
            "SSLv2", "SSLv3", "TLSv1", "TLSv1.1"
    ));

    // https://developer.android.com/reference/javax/net/ssl/SSLSocket.html#cipher-suites
    private static final Pattern SSL_CIPHER_BLACKLIST =
            Pattern.compile(".*(_DES|DH_|DSS|EXPORT|MD5|NULL|RC4|TLS_FALLBACK_SCSV).*");

    // TLS_FALLBACK_SCSV https://tools.ietf.org/html/rfc7507
    // TLS_EMPTY_RENEGOTIATION_INFO_SCSV https://tools.ietf.org/html/rfc5746

    static {
        System.loadLibrary("fairemail");
    }

    private static native int jni_socket_keep_alive(int fd, int seconds);

    private EmailService() {
        // Prevent instantiation
    }

    EmailService(Context context, String protocol, String realm, int encryption, boolean insecure, boolean debug) throws NoSuchProviderException {
        this(context, protocol, realm, encryption, insecure, PURPOSE_USE, debug);
    }

    EmailService(Context context, String protocol, String realm, int encryption, boolean insecure, int purpose, boolean debug) throws NoSuchProviderException {
        this.context = context.getApplicationContext();
        this.protocol = protocol;
        this.insecure = insecure;
        this.purpose = purpose;
        this.debug = debug;

        properties = MessageHelper.getSessionProperties();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.log = prefs.getBoolean("protocol", false);
        this.harden = prefs.getBoolean("ssl_harden", false);

        boolean auth_plain = prefs.getBoolean("auth_plain", true);
        boolean auth_login = prefs.getBoolean("auth_login", true);
        boolean auth_ntlm = prefs.getBoolean("auth_ntlm", true);
        boolean auth_sasl = prefs.getBoolean("auth_sasl", true);
        Log.i("Authenticate" +
                " plain=" + auth_plain +
                " login=" + auth_login +
                " ntlm=" + auth_ntlm +
                " sasl=" + auth_sasl);

        properties.put("mail.event.scope", "folder");
        properties.put("mail.event.executor", executor);

        if (!auth_plain)
            properties.put("mail." + protocol + ".auth.plain.disable", "true");
        if (!auth_login)
            properties.put("mail." + protocol + ".auth.login.disable", "true");
        if (!auth_ntlm)
            properties.put("mail." + protocol + ".auth.ntlm.disable", "true");

        // SASL is attempted before other authentication methods
        properties.put("mail." + protocol + ".sasl.enable", Boolean.toString(auth_sasl));
        properties.put("mail." + protocol + ".sasl.mechanisms", "CRAM-MD5");
        properties.put("mail." + protocol + ".sasl.realm", realm == null ? "" : realm);

        properties.put("mail." + protocol + ".auth.ntlm.domain", realm == null ? "" : realm);

        // writetimeout: one thread overhead
        int timeout = prefs.getInt("timeout", DEFAULT_CONNECT_TIMEOUT) * 1000;
        Log.i("Timeout=" + timeout);
        if (purpose == PURPOSE_SEARCH) {
            properties.put("mail." + protocol + ".connectiontimeout", Integer.toString(timeout));
            properties.put("mail." + protocol + ".writetimeout", Integer.toString(SEARCH_TIMEOUT));
            properties.put("mail." + protocol + ".timeout", Integer.toString(SEARCH_TIMEOUT));
        } else {
            int factor = 2;
            if ("smtp".equals(protocol) || "smtps".equals(protocol))
                factor *= 2;
            properties.put("mail." + protocol + ".connectiontimeout", Integer.toString(timeout));
            properties.put("mail." + protocol + ".writetimeout", Integer.toString(timeout * factor));
            properties.put("mail." + protocol + ".timeout", Integer.toString(timeout * factor));
        }

        if (debug && BuildConfig.DEBUG)
            properties.put("mail.debug.auth", "true");

        boolean starttls = (encryption == ENCRYPTION_STARTTLS);

        if (encryption == ENCRYPTION_NONE) {
            properties.put("mail." + protocol + ".ssl.enable", "false");
            properties.put("mail." + protocol + ".socketFactory", new SocketFactoryService());
        }

        if ("pop3".equals(protocol) || "pop3s".equals(protocol)) {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html#properties
            properties.put("mail.pop3s.starttls.enable", "false");

            properties.put("mail.pop3.starttls.enable", Boolean.toString(starttls));
            properties.put("mail.pop3.starttls.required", Boolean.toString(starttls && !insecure));

        } else if ("imap".equals(protocol) || "imaps".equals(protocol) || "gimaps".equals(protocol)) {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#properties
            properties.put("mail.imaps.starttls.enable", "false");

            properties.put("mail.imap.starttls.enable", Boolean.toString(starttls));
            properties.put("mail.imap.starttls.required", Boolean.toString(starttls && !insecure));

            properties.put("mail." + protocol + ".separatestoreconnection", Boolean.toString(SEPARATE_STORE_CONNECTION));
            properties.put("mail." + protocol + ".connectionpool.debug", "true");
            properties.put("mail." + protocol + ".connectionpoolsize", Integer.toString(POOL_SIZE));
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

            properties.put("mail.smtp.starttls.enable", Boolean.toString(starttls));
            properties.put("mail.smtp.starttls.required", Boolean.toString(starttls && !insecure));

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

    void setUseIp(boolean enabled, String host) {
        this.useip = enabled;
        this.ehlo = host;
    }

    void setLeaveOnServer(boolean keep) {
        properties.put("mail." + protocol + ".rsetbeforequit", Boolean.toString(keep));
    }

    void setUnicode(boolean value) {
        properties.put("mail.mime.allowutf8", Boolean.toString(value));
    }

    // https://tools.ietf.org/html/rfc3461
    void setDsnNotify(String what) {
        properties.put("mail." + protocol + ".dsn.notify", what);
    }

    void setListener(StoreListener listener) {
        this.listener = listener;
    }

    public void connect(EntityAccount account) throws MessagingException {
        connect(
                account.host, account.port,
                account.auth_type, account.provider,
                account.user, account.password,
                new ServiceAuthenticator.IAuthenticated() {
                    @Override
                    public void onPasswordChanged(String newPassword) {
                        DB db = DB.getInstance(context);
                        int count = db.account().setAccountPassword(account.id, newPassword);
                        EntityLog.log(context, account.name + " token refreshed=" + count);
                    }
                },
                account.certificate_alias, account.fingerprint);
    }

    public void connect(EntityIdentity identity) throws MessagingException {
        connect(
                identity.host, identity.port,
                identity.auth_type, identity.provider,
                identity.user, identity.password,
                new ServiceAuthenticator.IAuthenticated() {
                    @Override
                    public void onPasswordChanged(String newPassword) {
                        DB db = DB.getInstance(context);
                        int count = db.identity().setIdentityPassword(identity.id, newPassword);
                        EntityLog.log(context, identity.email + " token refreshed=" + count);

                    }
                },
                identity.certificate_alias, identity.fingerprint);
    }

    public void connect(
            String host, int port,
            int auth, String provider, String user, String password,
            String certificate, String fingerprint) throws MessagingException {
        connect(host, port, auth, provider, user, password, null, certificate, fingerprint);
    }

    private void connect(
            String host, int port,
            int auth, String provider, String user, String password,
            ServiceAuthenticator.IAuthenticated intf,
            String certificate, String fingerprint) throws MessagingException {
        SSLSocketFactoryService factory = null;
        try {
            PrivateKey key = null;
            X509Certificate[] chain = null;
            if (certificate != null) {
                Log.i("Get client certificate alias=" + certificate);
                try {
                    key = KeyChain.getPrivateKey(context, certificate);
                    chain = KeyChain.getCertificateChain(context, certificate);
                } catch (Throwable ex) {
                    Log.w(ex);
                }
            }

            factory = new SSLSocketFactoryService(host, insecure, harden, key, chain, fingerprint);
            properties.put("mail." + protocol + ".ssl.socketFactory", factory);
            properties.put("mail." + protocol + ".socketFactory.fallback", "false");
            properties.put("mail." + protocol + ".ssl.checkserveridentity", "false");
        } catch (GeneralSecurityException ex) {
            properties.put("mail." + protocol + ".ssl.checkserveridentity", Boolean.toString(!insecure));
            if (insecure)
                properties.put("mail." + protocol + ".ssl.trust", "*");
            Log.e("Trust issues", ex);
        }

        properties.put("mail." + protocol + ".forcepasswordrefresh", "true");
        ServiceAuthenticator authenticator = new ServiceAuthenticator(context, auth, provider, user, password, intf);

        try {
            if (auth == AUTH_TYPE_GMAIL || auth == AUTH_TYPE_OAUTH) {
                properties.put("mail." + protocol + ".auth.mechanisms", "XOAUTH2");
                properties.put("mail." + protocol + ".auth.xoauth2.disable", "false");
            } else
                properties.put("mail." + protocol + ".auth.xoauth2.disable", "true");

            if (auth == AUTH_TYPE_OAUTH && "imap.mail.yahoo.com".equals(host))
                properties.put("mail." + protocol + ".yahoo.guid", "FAIRMAIL_V1");

            connect(host, port, auth, user, authenticator, factory);
        } catch (AuthenticationFailedException ex) {
            if (auth == AUTH_TYPE_GMAIL || auth == AUTH_TYPE_OAUTH) {
                try {
                    authenticator.refreshToken(true);
                    connect(host, port, auth, user, authenticator, factory);
                } catch (Exception ex1) {
                    Log.e(ex1);
                    throw new AuthenticationFailedException(ex.getMessage(), ex1);
                }
            } else if (purpose == PURPOSE_CHECK) {
                String msg = ex.getMessage();
                if (msg != null)
                    msg = msg.trim();
                if (TextUtils.isEmpty(msg))
                    throw ex;
                throw new AuthenticationFailedException(
                        context.getString(R.string.title_service_auth, msg),
                        ex.getNextException());
            } else
                throw ex;
        } catch (MailConnectException ex) {
            if (ConnectionHelper.vpnActive(context)) {
                MailConnectException mex = new MailConnectException(
                        new SocketConnectException(
                                context.getString(R.string.title_service_vpn),
                                new Exception(),
                                ex.getHost(),
                                ex.getPort(),
                                ex.getConnectionTimeout()));
                mex.setNextException(ex.getNextException());
                throw mex;
            } else
                throw ex;
        } catch (MessagingException ex) {
            if (purpose == PURPOSE_CHECK) {
                if (port == 995 && !("pop3".equals(protocol) || "pop3s".equals(protocol)))
                    throw new MessagingException(context.getString(R.string.title_service_port), ex);
                else if (ex.getMessage() != null &&
                        ex.getMessage().contains("Got bad greeting"))
                    throw new MessagingException(context.getString(R.string.title_service_protocol), ex);
                else if (ex.getCause() instanceof SSLException &&
                        ex.getCause().getMessage() != null &&
                        ex.getCause().getMessage().contains("Unable to parse TLS packet header"))
                    throw new MessagingException(context.getString(R.string.title_service_protocol), ex);
                else if (ex.getCause() instanceof SSLHandshakeException)
                    throw new MessagingException(context.getString(R.string.title_service_protocol), ex);
                else
                    throw ex;
            } else
                throw ex;
        }
    }

    private void connect(
            String host, int port, int auth,
            String user, Authenticator authenticator,
            SSLSocketFactoryService factory) throws MessagingException {
        InetAddress main = null;
        boolean require_id = (purpose == PURPOSE_CHECK &&
                auth == AUTH_TYPE_OAUTH &&
                "outlook.office365.com".equals(host));
        Log.i("Require ID=" + require_id);
        try {
            //if (BuildConfig.DEBUG)
            //    throw new MailConnectException(
            //            new SocketConnectException("Debug", new IOException("Test"), host, port, 0));

            main = InetAddress.getByName(host);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean prefer_ip4 = prefs.getBoolean("prefer_ip4", true);
            if (prefer_ip4 && main instanceof Inet6Address) {
                for (InetAddress iaddr : InetAddress.getAllByName(host))
                    if (iaddr instanceof Inet4Address) {
                        main = iaddr;
                        EntityLog.log(context, "Preferring=" + main);
                        break;
                    }
            }

            EntityLog.log(context, "Connecting to " + main);
            _connect(main, port, require_id, user, authenticator, factory);
        } catch (UnknownHostException ex) {
            throw new MessagingException(ex.getMessage(), ex);
        } catch (MessagingException ex) {
            boolean ioError = false;
            Throwable ce = ex;
            while (ce != null) {
                if (factory != null && ce instanceof CertificateException)
                    throw new UntrustedException(factory.getFingerPrintSelect(), ex);
                if (ce instanceof IOException)
                    ioError = true;
                ce = ce.getCause();
            }

            if (ioError) {
                EntityLog.log(context, "Connect ex=" +
                        ex.getClass().getName() + ":" + ex.getMessage());
                try {
                    // Some devices resolve IPv6 addresses while not having IPv6 connectivity
                    InetAddress[] iaddrs = InetAddress.getAllByName(host);
                    boolean ip4 = (main instanceof Inet4Address);
                    boolean ip6 = (main instanceof Inet6Address);

                    boolean has4 = false;
                    boolean has6 = false;
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces != null && interfaces.hasMoreElements()) {
                        NetworkInterface ni = interfaces.nextElement();
                        for (InterfaceAddress iaddr : ni.getInterfaceAddresses()) {
                            InetAddress addr = iaddr.getAddress();
                            boolean local = (addr.isLoopbackAddress() || addr.isLinkLocalAddress());
                            EntityLog.log(context, "Interface=" + ni + " addr=" + addr + " local=" + local);
                            if (!local)
                                if (addr instanceof Inet4Address)
                                    has4 = true;
                                else if (addr instanceof Inet6Address)
                                    has6 = true;
                        }
                    }

                    EntityLog.log(context, "Address main=" + main +
                            " count=" + iaddrs.length +
                            " ip4=" + ip4 + "/" + has4 +
                            " ip6=" + ip6 + "/" + has6);

                    for (InetAddress iaddr : iaddrs) {
                        EntityLog.log(context, "Address resolved=" + iaddr);

                        if (iaddr.equals(main))
                            continue;

                        if (iaddr instanceof Inet4Address) {
                            if (!has4 || ip4)
                                continue;
                            ip4 = true;
                        }

                        if (iaddr instanceof Inet6Address) {
                            if (!has6 || ip6)
                                continue;
                            ip6 = true;
                        }

                        try {
                            EntityLog.log(context, "Falling back to " + iaddr);
                            _connect(iaddr, port, require_id, user, authenticator, factory);
                            return;
                        } catch (MessagingException ex1) {
                            ex = ex1;
                            EntityLog.log(context, "Fallback ex=" +
                                    ex1.getClass().getName() + ":" + ex1.getMessage());
                        }
                    }
                } catch (IOException ex1) {
                    throw new MessagingException(ex1.getMessage(), ex1);
                }
            }

            throw ex;
        }
    }

    private void _connect(
            InetAddress address, int port, boolean require_id,
            String user, Authenticator authenticator,
            SSLSocketFactoryService factory) throws MessagingException {
        isession = Session.getInstance(properties, authenticator);

        isession.setDebug(debug || log);
        if (debug || log)
            isession.setDebugOut(new PrintStream(new OutputStream() {
                private ByteArrayOutputStream bos = new ByteArrayOutputStream();

                @Override
                public void write(int b) {
                    try {
                        if (((char) b) == '\n') {
                            String line = bos.toString();
                            if (!line.endsWith("ignoring socket timeout"))
                                if (log)
                                    EntityLog.log(context, user + " " + line);
                                else
                                    android.util.Log.i("javamail", user + " " + line);
                            bos.reset();
                        } else
                            bos.write(b);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            }, true));

        //System.setProperty("mail.socket.debug", Boolean.toString(debug));
        isession.addProvider(new GmailSSLProvider());

        if ("pop3".equals(protocol) || "pop3s".equals(protocol)) {
            iservice = isession.getStore(protocol);
            iservice.connect(address.getHostAddress(), port, user, null);

        } else if ("imap".equals(protocol) || "imaps".equals(protocol) || "gimaps".equals(protocol)) {
            iservice = isession.getStore(protocol);
            if (listener != null)
                ((IMAPStore) iservice).addStoreListener(listener);
            iservice.connect(address.getHostAddress(), port, user, null);

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
                        for (Map.Entry<String, String> entry : sid.entrySet()) {
                            String key = entry.getKey();
                            crumb.put(key, entry.getValue());
                            EntityLog.log(context, "Server " + key + "=" + entry.getValue());
                        }
                        Log.breadcrumb("server", crumb);
                    }
                } catch (MessagingException ex) {
                    Log.w(ex);
                    // Check for 'User is authenticated but not connected'
                    if (require_id)
                        throw ex;
                }

        } else if ("smtp".equals(protocol) || "smtps".equals(protocol)) {
            // https://tools.ietf.org/html/rfc5321#section-4.1.3
            String hdomain = getDefaultEhlo();
            String haddr = (address instanceof Inet4Address ? "[127.0.0.1]" : "[IPv6:::1]");

            properties.put("mail." + protocol + ".localhost",
                    ehlo == null ? (useip ? haddr : hdomain) : ehlo);
            Log.i("Using localhost=" + properties.getProperty("mail." + protocol + ".localhost"));

            iservice = isession.getTransport(protocol);
            try {
                iservice.connect(address.getHostAddress(), port, user, null);
            } catch (MessagingException ex) {
                if (ehlo == null && ConnectionHelper.isSyntacticallyInvalid(ex)) {
                    properties.put("mail." + protocol + ".localhost", useip ? hdomain : haddr);
                    Log.i("Fallback localhost=" + properties.getProperty("mail." + protocol + ".localhost"));
                    try {
                        iservice.connect(address.getHostAddress(), port, user, null);
                    } catch (MessagingException ex1) {
                        if (ConnectionHelper.isSyntacticallyInvalid(ex1))
                            Log.e("Used localhost=" + haddr + "/" + hdomain);
                        throw ex1;
                    }
                } else
                    throw ex;
            }
        } else
            throw new NoSuchProviderException(protocol);
    }

    static String getDefaultEhlo() {
        if (BuildConfig.APPLICATION_ID.startsWith("eu.faircode.email"))
            return "dummy.faircode.eu";
        String[] c = BuildConfig.APPLICATION_ID.split("\\.");
        Collections.reverse(Arrays.asList(c));
        return TextUtils.join(".", c);
    }

    List<EntityFolder> getFolders() throws MessagingException {
        List<EntityFolder> folders = new ArrayList<>();

        for (Folder ifolder : getStore().getDefaultFolder().list("*")) {
            String fullName = ifolder.getFullName();
            String[] attrs = ((IMAPFolder) ifolder).getAttributes();
            String type = EntityFolder.getType(attrs, fullName, true);
            Log.i(fullName + " attrs=" + TextUtils.join(" ", attrs) + " type=" + type);

            if (type != null)
                folders.add(new EntityFolder(fullName, type));
        }

        EntityFolder.guessTypes(folders, getStore().getDefaultFolder().getSeparator());

        boolean inbox = false;
        for (EntityFolder folder : folders)
            if (EntityFolder.INBOX.equals(folder.type)) {
                inbox = true;
                break;
            }

        if (!inbox)
            throw new IllegalArgumentException(context.getString(R.string.title_setup_no_inbox));

        return folders;
    }

    Store getStore() {
        return (Store) iservice;
    }

    SMTPTransport getTransport() {
        return (SMTPTransport) iservice;
    }

    Long getMaxSize() throws MessagingException {
        // https://support.google.com/mail/answer/6584#limit

        String size;
        if (iservice instanceof SMTPTransport) {
            // https://tools.ietf.org/html/rfc1870
            size = getTransport().getExtensionParameter("SIZE");
        } else if (iservice instanceof IMAPStore) {
            // https://tools.ietf.org/html/rfc7889
            size = ((IMAPStore) iservice).getCapability("APPENDLIMIT");
        } else
            return null;

        if (!TextUtils.isEmpty(size) && TextUtils.isDigitsOnly(size)) {
            long s = Long.parseLong(size);
            if (s != 0) // Not infinite
                return s;
        }

        return null;
    }

    boolean hasCapability(String capability) throws MessagingException {
        Store store = getStore();
        if (store instanceof IMAPStore)
            return ((IMAPStore) getStore()).hasCapability(capability);
        else
            return false;
    }

    public boolean isOpen() {
        return (iservice != null && iservice.isConnected());
    }

    public void close() throws MessagingException {
        try {
            if (iservice != null && iservice.isConnected())
                iservice.close();
        } finally {
            context = null;
        }
    }

    private static class SocketFactoryService extends SocketFactory {
        private SocketFactory factory = SocketFactory.getDefault();

        @Override
        public Socket createSocket() throws IOException {
            return configure(factory.createSocket());
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return configure(factory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return configure(factory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return configure(factory.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return configure(factory.createSocket(address, port, localAddress, localPort));
        }

        private Socket configure(Socket socket) throws SocketException {
            configureSocketOptions(socket);
            return socket;
        }
    }

    private static class SSLSocketFactoryService extends SSLSocketFactory {
        // openssl s_client -connect host:port < /dev/null 2>/dev/null | openssl x509 -fingerprint -noout -in /dev/stdin
        private String server;
        private boolean secure;
        private boolean harden;
        private String trustedFingerprint;
        private SSLSocketFactory factory;
        private X509Certificate certificate;

        SSLSocketFactoryService(String host, boolean insecure, boolean harden, PrivateKey key, X509Certificate[] chain, String fingerprint) throws GeneralSecurityException {
            this.server = host;
            this.secure = !insecure;
            this.harden = harden;
            this.trustedFingerprint = fingerprint;

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);

            TrustManager[] tms = tmf.getTrustManagers();
            if (tms == null || tms.length == 0 || !(tms[0] instanceof X509TrustManager)) {
                Log.e("Missing root trust manager");
                sslContext.init(null, tms, null);
            } else {
                final X509TrustManager rtm = (X509TrustManager) tms[0];

                X509TrustManager tm = new X509TrustManager() {
                    // openssl s_client -connect <host>

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        if (secure)
                            rtm.checkClientTrusted(chain, authType);
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        certificate = chain[0];

                        if (secure) {
                            // Check if selected fingerprint
                            if (trustedFingerprint != null && matches(certificate, trustedFingerprint)) {
                                Log.i("Trusted selected fingerprint");
                                return;
                            }

                            // Check certificates
                            try {
                                rtm.checkServerTrusted(chain, authType);
                            } catch (CertificateException ex) {
                                Principal principal = certificate.getSubjectDN();
                                if (principal == null)
                                    throw ex;
                                else
                                    throw new CertificateException(principal.getName(), ex);
                            }

                            // Check host name
                            List<String> names = getDnsNames(certificate);
                            for (String name : names)
                                if (matches(server, name)) {
                                    Log.i("Trusted server=" + server + " name=" + name);
                                    return;
                                }

                            String error = server + " not in certificate: " + TextUtils.join(",", names);
                            Log.i(error);
                            throw new CertificateException(error);
                        }
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return rtm.getAcceptedIssuers();
                    }
                };

                KeyManager[] km = null;
                if (key != null && chain != null)
                    try {
                        Log.i("Client certificate init");

                        KeyStore ks = KeyStore.getInstance("PKCS12");
                        ks.load(null, new char[0]);
                        ks.setKeyEntry(server, key, new char[0], chain);

                        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                        kmf.init(ks, new char[0]);
                        km = kmf.getKeyManagers();

                        Log.i("Client certificate initialized");
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                sslContext.init(km, new TrustManager[]{tm}, null);
            }

            factory = sslContext.getSocketFactory();
        }

        @Override
        public Socket createSocket() throws IOException {
            Log.e("createSocket");
            throw new IOException("createSocket");
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return configure(factory.createSocket(server, port));
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return configure(factory.createSocket(s, server, port, autoClose));
        }

        @Override
        public Socket createSocket(InetAddress address, int port) throws IOException {
            Log.e("createSocket(address, port)");
            throw new IOException("createSocket");
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException {
            return configure(factory.createSocket(server, port, clientAddress, clientPort));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
            Log.e("createSocket(address, port, clientAddress, clientPort)");
            throw new IOException("createSocket");
        }

        private Socket configure(Socket socket) throws SocketException {
            configureSocketOptions(socket);

            if (socket instanceof SSLSocket) {
                SSLSocket sslSocket = (SSLSocket) socket;

                if (!secure) {
                    sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());

                    List<String> ciphers = new ArrayList<>();
                    for (String cipher : sslSocket.getSupportedCipherSuites())
                        if (!cipher.endsWith("_SCSV"))
                            ciphers.add(cipher);
                    sslSocket.setEnabledCipherSuites(ciphers.toArray(new String[0]));
                } else if (harden) {
                    List<String> protocols = new ArrayList<>();
                    for (String protocol : sslSocket.getEnabledProtocols())
                        if (SSL_PROTOCOL_BLACKLIST.contains(protocol))
                            Log.i("SSL disabling protocol=" + protocol);
                        else
                            protocols.add(protocol);
                    sslSocket.setEnabledProtocols(protocols.toArray(new String[0]));

                    List<String> ciphers = new ArrayList<>();
                    for (String cipher : sslSocket.getEnabledCipherSuites()) {
                        if (SSL_CIPHER_BLACKLIST.matcher(cipher).matches())
                            Log.i("SSL disabling cipher=" + cipher);
                        else
                            ciphers.add(cipher);
                    }
                    sslSocket.setEnabledCipherSuites(ciphers.toArray(new String[0]));
                } else {
                    List<String> ciphers = new ArrayList<>(Arrays.asList(sslSocket.getEnabledCipherSuites()));
                    for (String cipher : sslSocket.getSupportedCipherSuites())
                        if (cipher.contains("3DES")) {
                            // Some servers support 3DES and RC4 only
                            Log.i("SSL enabling cipher=" + cipher);
                            ciphers.add(cipher);
                        }
                    sslSocket.setEnabledCipherSuites(ciphers.toArray(new String[0]));
                }

                Log.i("SSL protocols=" + TextUtils.join(",", sslSocket.getEnabledProtocols()));
                Log.i("SSL ciphers=" + TextUtils.join(",", sslSocket.getEnabledCipherSuites()));
            }

            return socket;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return factory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return factory.getSupportedCipherSuites();
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

                return domain.equalsIgnoreCase(cdomain);
            } else
                return server.equalsIgnoreCase(name);
        }

        private static List<String> getDnsNames(X509Certificate certificate) throws CertificateParsingException {
            List<String> result = new ArrayList<>();

            Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
            if (altNames == null)
                return result;

            for (List altName : altNames)
                if (altName.get(0).equals(GeneralName.dNSName))
                    result.add((String) altName.get(1));

            return result;
        }

        private static boolean matches(X509Certificate certificate, @NonNull String trustedFingerprint) {
            // Get certificate fingerprint
            try {
                String fingerprint = getFingerPrint(certificate);
                int slash = trustedFingerprint.indexOf('/');
                if (slash < 0)
                    return trustedFingerprint.equals(fingerprint);
                else {
                    String keyId = getKeyId(certificate);
                    if (trustedFingerprint.substring(slash + 1).equals(keyId))
                        return true;
                    return trustedFingerprint.substring(0, slash).equals(fingerprint);
                }
            } catch (Throwable ex) {
                Log.w(ex);
                return false;
            }
        }

        private static String getKeyId(X509Certificate certificate) {
            try {
                byte[] extension = certificate.getExtensionValue(Extension.subjectKeyIdentifier.getId());
                if (extension == null)
                    return null;
                byte[] bytes = DEROctetString.getInstance(extension).getOctets();
                SubjectKeyIdentifier keyId = SubjectKeyIdentifier.getInstance(bytes);
                return Helper.hex(keyId.getKeyIdentifier());
            } catch (Throwable ex) {
                Log.e(ex);
                return null;
            }
        }

        private static String getFingerPrint(X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException {
            return Helper.sha1(certificate.getEncoded());
        }

        String getFingerPrintSelect() {
            try {
                if (certificate == null)
                    return null;
                String keyId = getKeyId(certificate);
                String fingerPrint = getFingerPrint(certificate);
                return fingerPrint + (keyId == null ? "" : "/" + keyId);
            } catch (Throwable ex) {
                Log.e(ex);
                return null;
            }
        }
    }

    private static void configureSocketOptions(Socket socket) throws SocketException {
        int timeout = socket.getSoTimeout();
        boolean keepAlive = socket.getKeepAlive();
        int linger = socket.getSoLinger();

        Log.i("Socket type=" + socket.getClass().getName() +
                " timeout=" + timeout +
                " keep-alive=" + keepAlive +
                " linger=" + linger);

        if (keepAlive) {
            Log.e("Socket keep-alive=" + keepAlive);
            socket.setKeepAlive(false); // sets SOL_SOCKET/SO_KEEPALIVE
        }

        if (linger >= 0) {
            Log.e("Socket linger=" + linger);
            socket.setSoLinger(false, -1);
        }

        try {
            boolean tcp_keep_alive = Boolean.parseBoolean(System.getProperty("fairemail.tcp_keep_alive"));
            if (tcp_keep_alive) {
                Log.i("Enabling TCP keep alive");

                int fd = ParcelFileDescriptor.fromSocket(socket).getFd();
                int errno = jni_socket_keep_alive(fd, TCP_KEEP_ALIVE_INTERVAL);
                if (errno == 0)
                    Log.i("Enabled TCP keep alive");
                else
                    throw new ErrnoException("jni_socket_keep_alive", errno);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    class UntrustedException extends MessagingException {
        private String fingerprint;

        UntrustedException(@NonNull String fingerprint, @NonNull Exception cause) {
            super("Untrusted", cause);
            this.fingerprint = fingerprint;
        }

        String getFingerprint() {
            return fingerprint;
        }

        @NonNull
        @Override
        public synchronized String toString() {
            return getCause().toString();
        }
    }
}