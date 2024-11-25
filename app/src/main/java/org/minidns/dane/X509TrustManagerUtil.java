/*
 * Copyright 2015-2020 the original author or authors
 *
 * This software is licensed under the Apache License, Version 2.0,
 * the GNU Lesser General Public License version 2 or later ("LGPL")
 * and the WTFPL.
 * You may choose either license to govern your use of this software only
 * upon the condition that you accept all of the terms of either
 * the Apache License 2.0, the LGPL 2.1+ or the WTFPL.
 */
package org.minidns.dane;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class X509TrustManagerUtil {

    public static X509TrustManager getDefault() {
        return getDefault(null);
    }

    public static X509TrustManager getDefault(KeyStore keyStore) {
        String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(defaultAlgorithm);
            trustManagerFactory.init(keyStore);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new AssertionError(e);
        }

        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        throw new AssertionError("No trust manager for the default algorithm " + defaultAlgorithm + " found");
    }
}
