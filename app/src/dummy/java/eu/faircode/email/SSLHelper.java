package eu.faircode.email;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLHelper {
    // https://support.google.com/faqs/answer/6346016

    static TrustManager[] getTrustManagers(String server, boolean secure, boolean cert_strict, String trustedFingerprint, ITrust intf) {
        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            return tmf.getTrustManagers();
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static boolean customTrustManager() {
        return false;
    }

    interface ITrust {
        void checkServerTrusted(X509Certificate[] chain);
    }
}
