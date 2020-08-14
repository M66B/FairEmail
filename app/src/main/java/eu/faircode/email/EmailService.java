package eu.faircode.email;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.KeyChain;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.sun.mail.gimap.GmailSSLProvider;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.SocketConnectException;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.NoClientAuthentication;

import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
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
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.StoreListener;
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

// IMAP standards: https://imapwiki.org/Specs

public class EmailService implements AutoCloseable {
    private Context context;
    private String protocol;
    private boolean insecure;
    private int purpose;
    private boolean harden;
    private boolean useip;
    private String ehlo;
    private boolean debug;
    private Properties properties;
    private Session isession;
    private Service iservice;
    private StoreListener listener;

    private ExecutorService executor = Helper.getBackgroundExecutor(0, "mail");

    static final int AUTH_TYPE_PASSWORD = 1;
    static final int AUTH_TYPE_GMAIL = 2;
    static final int AUTH_TYPE_OAUTH = 3;

    static final int PURPOSE_CHECK = 1;
    static final int PURPOSE_USE = 2;
    static final int PURPOSE_SEARCH = 3;

    final static int DEFAULT_CONNECT_TIMEOUT = 20; // seconds

    private final static int SEARCH_TIMEOUT = 90 * 1000; // milliseconds
    private final static int FETCH_SIZE = 1024 * 1024; // bytes, default 16K
    private final static int POOL_TIMEOUT = 90 * 1000; // milliseconds, default 45 sec

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

    private EmailService() {
        // Prevent instantiation
    }

    EmailService(Context context, String protocol, String realm, boolean insecure, boolean debug) throws NoSuchProviderException {
        this(context, protocol, realm, insecure, PURPOSE_USE, debug);
    }

    EmailService(Context context, String protocol, String realm, boolean insecure, int purpose, boolean debug) throws NoSuchProviderException {
        this.context = context.getApplicationContext();
        this.protocol = protocol;
        this.insecure = insecure;
        this.purpose = purpose;
        this.debug = debug;

        properties = MessageHelper.getSessionProperties();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.harden = prefs.getBoolean("ssl_harden", false);

        boolean auth_plain = prefs.getBoolean("auth_plain", true);
        boolean auth_login = prefs.getBoolean("auth_login", true);
        boolean auth_sasl = prefs.getBoolean("auth_sasl", true);
        Log.i("Authenticate plain=" + auth_plain + " login=" + auth_login + " sasl=" + auth_sasl);

        properties.put("mail.event.scope", "folder");
        properties.put("mail.event.executor", executor);

        if (!auth_plain)
            properties.put("mail." + protocol + ".auth.plain.disable", "true");
        if (!auth_login)
            properties.put("mail." + protocol + ".auth.login.disable", "true");

        properties.put("mail." + protocol + ".sasl.enable", "true");
        if (auth_sasl) {
            properties.put("mail." + protocol + ".sasl.mechanisms", "CRAM-MD5");
            properties.put("mail." + protocol + ".sasl.realm", realm == null ? "" : realm);
        }
        properties.put("mail." + protocol + ".auth.ntlm.domain", realm == null ? "" : realm);

        // writetimeout: one thread overhead
        int timeout = prefs.getInt("timeout", DEFAULT_CONNECT_TIMEOUT) * 1000;
        Log.i("Timeout=" + timeout);
        if (purpose == PURPOSE_SEARCH) {
            properties.put("mail." + protocol + ".connectiontimeout", Integer.toString(timeout));
            properties.put("mail." + protocol + ".writetimeout", Integer.toString(SEARCH_TIMEOUT));
            properties.put("mail." + protocol + ".timeout", Integer.toString(SEARCH_TIMEOUT));
        } else {
            properties.put("mail." + protocol + ".connectiontimeout", Integer.toString(timeout));
            properties.put("mail." + protocol + ".writetimeout", Integer.toString(timeout * 2));
            properties.put("mail." + protocol + ".timeout", Integer.toString(timeout * 2));
        }

        if (debug && BuildConfig.DEBUG)
            properties.put("mail.debug.auth", "true");

        if ("pop3".equals(protocol) || "pop3s".equals(protocol)) {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html#properties
            properties.put("mail.pop3s.starttls.enable", "false");

            properties.put("mail.pop3.starttls.enable", "true");
            properties.put("mail.pop3.starttls.required", Boolean.toString(!insecure));

        } else if ("imap".equals(protocol) || "imaps".equals(protocol) || "gimaps".equals(protocol)) {
            // https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#properties
            properties.put("mail.imaps.starttls.enable", "false");

            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.starttls.required", Boolean.toString(!insecure));

            properties.put("mail." + protocol + ".separatestoreconnection", "true");
            properties.put("mail." + protocol + ".connectionpool.debug", "true");
            properties.put("mail." + protocol + ".connectionpoolsize", Integer.toString(2));
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

    void setListener(StoreListener listener) {
        this.listener = listener;
    }

    public void connect(EntityAccount account) throws MessagingException {
        String password = connect(
                account.host, account.port,
                account.auth_type, account.provider,
                account.user, account.password,
                account.certificate_alias, account.fingerprint);
        if (password != null) {
            DB db = DB.getInstance(context);
            int count = db.account().setAccountPassword(account.id, account.password);
            Log.i(account.name + " token refreshed=" + count);
        }
    }

    public void connect(EntityIdentity identity) throws MessagingException {
        String password = connect(
                identity.host, identity.port,
                identity.auth_type, identity.provider,
                identity.user, identity.password,
                identity.certificate_alias, identity.fingerprint);
        if (password != null) {
            DB db = DB.getInstance(context);
            int count = db.identity().setIdentityPassword(identity.id, identity.password);
            Log.i(identity.email + " token refreshed=" + count);
        }
    }

    public String connect(
            String host, int port,
            int auth, String provider, String user, String password,
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

        try {
            if (auth == AUTH_TYPE_GMAIL || auth == AUTH_TYPE_OAUTH)
                properties.put("mail." + protocol + ".auth.mechanisms", "XOAUTH2");

            if (auth == AUTH_TYPE_OAUTH) {
                if ("imap.mail.yahoo.com".equals(host))
                    properties.put("mail." + protocol + ".yahoo.guid", "FAIRMAIL_V1");
                AuthState authState = OAuthRefresh(context, provider, password);
                connect(host, port, auth, user, authState.getAccessToken(), factory);
                return authState.jsonSerializeString();
            } else {
                connect(host, port, auth, user, password, factory);
                return null;
            }
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
                                throw new AuthenticatorException("No token on refresh for " + user);

                            connect(host, port, auth, user, token, factory);
                            return token;
                        }

                    throw new AuthenticatorException("Account not found for " + user);
                } catch (Exception ex1) {
                    Log.e(ex1);
                    throw new AuthenticationFailedException(ex.getMessage(), ex1);
                }
            else if (auth == AUTH_TYPE_OAUTH) {
                AuthState authState = OAuthRefresh(context, provider, password);
                connect(host, port, auth, user, authState.getAccessToken(), factory);
                return authState.jsonSerializeString();
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
            String user, String password,
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
            EntityLog.log(context, "Connecting to " + main);
            _connect(main, port, require_id, user, password, factory);
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
                            _connect(iaddr, port, require_id, user, password, factory);
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
            String user, String password,
            SSLSocketFactoryService factory) throws MessagingException {
        isession = Session.getInstance(properties, null);
        isession.setDebug(debug);
        //System.setProperty("mail.socket.debug", Boolean.toString(debug));
        isession.addProvider(new GmailSSLProvider());

        if ("pop3".equals(protocol) || "pop3s".equals(protocol)) {
            iservice = isession.getStore(protocol);
            iservice.connect(address.getHostAddress(), port, user, password);

        } else if ("imap".equals(protocol) || "imaps".equals(protocol) || "gimaps".equals(protocol)) {
            iservice = isession.getStore(protocol);
            if (listener != null)
                ((IMAPStore) iservice).addStoreListener(listener);
            iservice.connect(address.getHostAddress(), port, user, password);

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
                    // Check for 'User is authenticated but not connected'
                    if (require_id)
                        throw ex;
                }

        } else if ("smtp".equals(protocol) || "smtps".equals(protocol)) {
            String[] c = BuildConfig.APPLICATION_ID.split("\\.");
            Collections.reverse(Arrays.asList(c));
            String hdomain = TextUtils.join(".", c);

            // https://tools.ietf.org/html/rfc5321#section-4.1.3
            String haddr = (address instanceof Inet4Address ? "[127.0.0.1]" : "[IPv6:::1]");

            properties.put("mail." + protocol + ".localhost",
                    ehlo == null ? (useip ? haddr : hdomain) : ehlo);
            Log.i("Using localhost=" + properties.getProperty("mail." + protocol + ".localhost"));

            iservice = isession.getTransport(protocol);
            try {
                iservice.connect(address.getHostAddress(), port, user, password);
            } catch (MessagingException ex) {
                if (ehlo == null && ConnectionHelper.isSyntacticallyInvalid(ex)) {
                    properties.put("mail." + protocol + ".localhost", useip ? hdomain : haddr);
                    Log.i("Fallback localhost=" + properties.getProperty("mail." + protocol + ".localhost"));
                    try {
                        iservice.connect(address.getHostAddress(), port, user, password);
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

    private static class ErrorHolder {
        AuthorizationException error;
    }

    private static AuthState OAuthRefresh(Context context, String id, String json) throws MessagingException {
        try {
            AuthState authState = AuthState.jsonDeserialize(json);

            ClientAuthentication clientAuth;
            EmailProvider provider = EmailProvider.getProvider(context, id);
            if (provider.oauth.clientSecret == null)
                clientAuth = NoClientAuthentication.INSTANCE;
            else
                clientAuth = new ClientSecretPost(provider.oauth.clientSecret);

            ErrorHolder holder = new ErrorHolder();
            Semaphore semaphore = new Semaphore(0);

            Log.i("OAuth refresh");
            AuthorizationService authService = new AuthorizationService(context);
            authState.performActionWithFreshTokens(
                    authService,
                    clientAuth,
                    new AuthState.AuthStateAction() {
                        @Override
                        public void execute(String accessToken, String idToken, AuthorizationException error) {
                            if (error != null)
                                holder.error = error;
                            semaphore.release();
                        }
                    });

            semaphore.acquire();
            Log.i("OAuth refreshed");

            if (holder.error != null)
                throw holder.error;

            return authState;
        } catch (Exception ex) {
            throw new MessagingException("OAuth refresh", ex);
        }
    }

    static String getAuthTokenType(String type) {
        // https://developers.google.com/gmail/imap/xoauth2-protocol
        if ("com.google".equals(type))
            return "oauth2:https://mail.google.com/";
        return null;
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

        private Socket configure(Socket socket) {
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
                    List<String> ciphers = new ArrayList<>();
                    ciphers.addAll(Arrays.asList(sslSocket.getEnabledCipherSuites()));
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

    class UntrustedException extends MessagingException {
        private String fingerprint;

        UntrustedException(@NonNull String fingerprint, @NonNull Exception cause) {
            super("Untrusted", cause);
            this.fingerprint = fingerprint;
        }

        String getFingerprint() {
            return fingerprint;
        }

        @NotNull
        @Override
        public synchronized String toString() {
            return getCause().toString();
        }
    }
}