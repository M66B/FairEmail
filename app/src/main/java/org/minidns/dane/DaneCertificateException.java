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

import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;

import org.minidns.record.TLSA;

public abstract class DaneCertificateException extends CertificateException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected DaneCertificateException() {
    }

    protected DaneCertificateException(String message) {
        super(message);
    }

    public static class CertificateMismatch extends DaneCertificateException {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public final TLSA tlsa;
        public final byte[] computed;

        public CertificateMismatch(TLSA tlsa, byte[] computed) {
            super("The TLSA RR does not match the certificate");
            this.tlsa = tlsa;
            this.computed = computed;
        }
    }

    public static class MultipleCertificateMismatchExceptions extends DaneCertificateException {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public final List<CertificateMismatch> certificateMismatchExceptions;

        public MultipleCertificateMismatchExceptions(List<CertificateMismatch> certificateMismatchExceptions) {
            super("There where multiple CertificateMismatch exceptions because none of the TLSA RR does match the certificate");
            assert !certificateMismatchExceptions.isEmpty();
            this.certificateMismatchExceptions = Collections.unmodifiableList(certificateMismatchExceptions);
        }
    }
}
