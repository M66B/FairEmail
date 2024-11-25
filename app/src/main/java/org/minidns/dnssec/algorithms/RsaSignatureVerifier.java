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

import org.minidns.dnssec.DnssecValidationFailedException.DnssecInvalidKeySpecException;
import org.minidns.record.DNSKEY;
import org.minidns.record.RRSIG;
import org.minidns.dnssec.DnssecValidationFailedException.DataMalformedException;

import java.io.DataInput;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

class RsaSignatureVerifier extends JavaSecSignatureVerifier {
    RsaSignatureVerifier(String algorithm) throws NoSuchAlgorithmException {
        super("RSA", algorithm);
    }

    @Override
    protected PublicKey getPublicKey(DNSKEY key) throws DataMalformedException, DnssecInvalidKeySpecException {
        DataInput dis = key.getKeyAsDataInputStream();
        BigInteger exponent, modulus;

        try {
            int exponentLength = dis.readUnsignedByte();
            int bytesRead = 1;
            if (exponentLength == 0) {
                bytesRead += 2;
                exponentLength = dis.readUnsignedShort();
            }

            byte[] exponentBytes = new byte[exponentLength];
            dis.readFully(exponentBytes);
            bytesRead += exponentLength;
            exponent = new BigInteger(1, exponentBytes);

            byte[] modulusBytes = new byte[key.getKeyLength() - bytesRead];
            dis.readFully(modulusBytes);
            modulus = new BigInteger(1, modulusBytes);
        } catch (IOException e) {
            throw new DataMalformedException(e, key.getKey());
        }

        try {
            return getKeyFactory().generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (InvalidKeySpecException e) {
            throw new DnssecInvalidKeySpecException(e);
        }
    }

    @Override
    protected byte[] getSignature(RRSIG rrsig) {
        return rrsig.getSignature();
    }
}
