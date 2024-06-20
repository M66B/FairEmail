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
package org.minidns.dnssec.algorithms;

import org.minidns.constants.DnssecConstants.DigestAlgorithm;
import org.minidns.constants.DnssecConstants.SignatureAlgorithm;
import org.minidns.dnssec.DnssecValidatorInitializationException;
import org.minidns.dnssec.DigestCalculator;
import org.minidns.dnssec.SignatureVerifier;
import org.minidns.record.NSEC3.HashAlgorithm;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AlgorithmMap {
    private Logger LOGGER = Logger.getLogger(AlgorithmMap.class.getName());

    public static final AlgorithmMap INSTANCE = new AlgorithmMap();

    private final Map<DigestAlgorithm, DigestCalculator> dsDigestMap = new HashMap<>();
    private final Map<SignatureAlgorithm, SignatureVerifier> signatureMap = new HashMap<>();
    private final Map<HashAlgorithm, DigestCalculator> nsecDigestMap = new HashMap<>();

    @SuppressWarnings("deprecation")
    private AlgorithmMap() {
        try {
            dsDigestMap.put(DigestAlgorithm.SHA1, new JavaSecDigestCalculator("SHA-1"));
            nsecDigestMap.put(HashAlgorithm.SHA1, new JavaSecDigestCalculator("SHA-1"));
        } catch (NoSuchAlgorithmException e) {
            // SHA-1 is MANDATORY
            throw new DnssecValidatorInitializationException("SHA-1 is mandatory", e);
        }
        try {
            dsDigestMap.put(DigestAlgorithm.SHA256, new JavaSecDigestCalculator("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is MANDATORY
            throw new DnssecValidatorInitializationException("SHA-256 is mandatory", e);
        }
        try {
            dsDigestMap.put(DigestAlgorithm.SHA384, new JavaSecDigestCalculator("SHA-384"));
        } catch (NoSuchAlgorithmException e) {
            // SHA-384 is OPTIONAL
            LOGGER.log(Level.FINE, "Platform does not support SHA-384", e);
        }

        try {
            signatureMap.put(SignatureAlgorithm.RSAMD5, new RsaSignatureVerifier("MD5withRSA"));
        } catch (NoSuchAlgorithmException e) {
            // RSA/MD5 is DEPRECATED
            LOGGER.log(Level.FINER, "Platform does not support RSA/MD5", e);
        }
        try {
            DsaSignatureVerifier sha1withDSA = new DsaSignatureVerifier("SHA1withDSA");
            signatureMap.put(SignatureAlgorithm.DSA, sha1withDSA);
            signatureMap.put(SignatureAlgorithm.DSA_NSEC3_SHA1, sha1withDSA);
        } catch (NoSuchAlgorithmException e) {
            // DSA/SHA-1 is OPTIONAL
            LOGGER.log(Level.FINE, "Platform does not support DSA/SHA-1", e);
        }
        try {
            RsaSignatureVerifier sha1withRSA = new RsaSignatureVerifier("SHA1withRSA");
            signatureMap.put(SignatureAlgorithm.RSASHA1, sha1withRSA);
            signatureMap.put(SignatureAlgorithm.RSASHA1_NSEC3_SHA1, sha1withRSA);
        } catch (NoSuchAlgorithmException e) {
            throw new DnssecValidatorInitializationException("Platform does not support RSA/SHA-1", e);
        }
        try {
            signatureMap.put(SignatureAlgorithm.RSASHA256, new RsaSignatureVerifier("SHA256withRSA"));
        } catch (NoSuchAlgorithmException e) {
            // RSA/SHA-256 is RECOMMENDED
            LOGGER.log(Level.INFO, "Platform does not support RSA/SHA-256", e);
        }
        try {
            signatureMap.put(SignatureAlgorithm.RSASHA512, new RsaSignatureVerifier("SHA512withRSA"));
        } catch (NoSuchAlgorithmException e) {
            // RSA/SHA-512 is RECOMMENDED
            LOGGER.log(Level.INFO, "Platform does not support RSA/SHA-512", e);
        }
        try {
            signatureMap.put(SignatureAlgorithm.ECC_GOST, new EcgostSignatureVerifier());
        } catch (NoSuchAlgorithmException e) {
            // GOST R 34.10-2001 is OPTIONAL
            LOGGER.log(Level.FINE, "Platform does not support GOST R 34.10-2001", e);
        }
        try {
            signatureMap.put(SignatureAlgorithm.ECDSAP256SHA256, new EcdsaSignatureVerifier.P256SHA256());
        } catch (NoSuchAlgorithmException e) {
            // ECDSA/SHA-256 is RECOMMENDED
            LOGGER.log(Level.INFO, "Platform does not support ECDSA/SHA-256", e);
        }
        try {
            signatureMap.put(SignatureAlgorithm.ECDSAP384SHA384, new EcdsaSignatureVerifier.P384SHA284());
        } catch (NoSuchAlgorithmException e) {
            // ECDSA/SHA-384 is RECOMMENDED
            LOGGER.log(Level.INFO, "Platform does not support ECDSA/SHA-384", e);
        }
    }

    public DigestCalculator getDsDigestCalculator(DigestAlgorithm algorithm) {
        return dsDigestMap.get(algorithm);
    }

    public SignatureVerifier getSignatureVerifier(SignatureAlgorithm algorithm) {
        return signatureMap.get(algorithm);
    }

    public DigestCalculator getNsecDigestCalculator(HashAlgorithm algorithm) {
        return nsecDigestMap.get(algorithm);
    }
}
