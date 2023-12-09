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
        return null;
    }

    static boolean customTrustManager() {
        return false;
    }

    interface ITrust {
        void checkServerTrusted(X509Certificate[] chain);
    }
}
