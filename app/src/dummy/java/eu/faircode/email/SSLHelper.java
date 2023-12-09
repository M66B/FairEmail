package eu.faircode.email;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class SSLHelper {
    static X509TrustManager getTrustManager(X509TrustManager rtm,
                                            String server,
                                            boolean secure, boolean cert_strict,
                                            String trustedFingerprint,
                                            ITrust intf) {
        // https://support.google.com/faqs/answer/6346016
        return null;
    }

    static boolean customTrustManager() {
        return false;
    }

    interface ITrust {
        void checkServerTrusted(X509Certificate[] chain);
    }
}
