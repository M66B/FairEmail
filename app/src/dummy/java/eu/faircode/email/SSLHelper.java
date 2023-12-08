package eu.faircode.email;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.X509TrustManager;

public class SSLHelper {
    static X509TrustManager getTrustManager(X509TrustManager rtm,
                                            String server,
                                            boolean secure, boolean cert_strict,
                                            String trustedFingerprint,
                                            ITrust intf) {
        return new X509TrustManager() {
            // openssl s_client -connect <host>

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (secure)
                    rtm.checkClientTrusted(chain, authType);
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (intf != null)
                    intf.checkServerTrusted(chain);

                if (secure) {
                    // Check if selected fingerprint
                    if (trustedFingerprint != null && matches(chain[0], trustedFingerprint)) {
                        Log.i("Trusted selected fingerprint");
                        return;
                    }

                    // Check certificates
                    try {
                        Log.i("Auth type=" + authType);
                        rtm.checkServerTrusted(chain, authType);
                    } catch (CertificateException ex) {
                        Principal principal = chain[0].getSubjectDN();
                        if (principal == null)
                            throw ex;
                        else if (cert_strict)
                            throw new CertificateException(principal.getName(), ex);
                        else if (noAnchor(ex) || isExpired(ex)) {
                            if (BuildConfig.PLAY_STORE_RELEASE)
                                Log.i(ex);
                            else
                                Log.w(ex);
                        } else
                            throw new CertificateException(principal.getName(), ex);
                    }

                    // Check host name
                    List<String> names = EntityCertificate.getDnsNames(chain[0]);
                    if (EntityCertificate.matches(server, names))
                        return;

                    // Fallback: check server/certificate IP address
                    if (!cert_strict)
                        try {
                            InetAddress ip = InetAddress.getByName(server);
                            Log.i("Checking server ip=" + ip);
                            for (String name : names) {
                                if (name.startsWith("*."))
                                    name = name.substring(2);
                                Log.i("Checking cert name=" + name);

                                try {
                                    for (InetAddress addr : InetAddress.getAllByName(name))
                                        if (Arrays.equals(ip.getAddress(), addr.getAddress())) {
                                            Log.i("Accepted " + name + " for " + server);
                                            return;
                                        }
                                } catch (UnknownHostException ex) {
                                    Log.w(ex);
                                }
                            }
                        } catch (UnknownHostException ex) {
                            Log.w(ex);
                        } catch (Throwable ex) {
                            Log.e(ex);
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

            private boolean noAnchor(Throwable ex) {
                while (ex != null) {
                    if (ex instanceof CertPathValidatorException &&
                            "Trust anchor for certification path not found."
                                    .equals(ex.getMessage()))
                        return true;
                    ex = ex.getCause();
                }
                return false;
            }

            private boolean isExpired(Throwable ex) {
                while (ex != null) {
                    if (ex instanceof CertPathValidatorException &&
                            "timestamp check failed"
                                    .equals(ex.getMessage()))
                        return true;

                    ex = ex.getCause();
                }
                return false;
            }
        };
    }

    private static boolean matches(X509Certificate certificate, @NonNull String trustedFingerprint) {
        // Get certificate fingerprint
        try {
            String fingerprint = EntityCertificate.getFingerprintSha1(certificate);
            int slash = trustedFingerprint.indexOf('/');
            if (slash < 0)
                return trustedFingerprint.equals(fingerprint);
            else {
                String keyId = EntityCertificate.getKeyId(certificate);
                if (trustedFingerprint.substring(slash + 1).equals(keyId))
                    return true;
                return trustedFingerprint.substring(0, slash).equals(fingerprint);
            }
        } catch (Throwable ex) {
            Log.w(ex);
            return false;
        }
    }

    interface ITrust {
        void checkServerTrusted(X509Certificate[] chain);
    }
}
