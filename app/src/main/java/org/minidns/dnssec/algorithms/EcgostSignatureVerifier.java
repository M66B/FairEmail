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
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;

class EcgostSignatureVerifier extends JavaSecSignatureVerifier {
    private static final int LENGTH = 32;
    private static final ECParameterSpec SPEC = new ECParameterSpec(
            new EllipticCurve(
                    new ECFieldFp(new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFD97", 16)),
                    new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFD94", 16),
                    new BigInteger("A6", 16)
            ),
            new ECPoint(BigInteger.ONE, new BigInteger("8D91E471E0989CDA27DF505A453F2B7635294F2DDF23E3B122ACC99C9E9F1E14", 16)),
            new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF6C611070995AD10045841B09B761B893", 16),
            1
    );

    EcgostSignatureVerifier() throws NoSuchAlgorithmException {
        super("ECGOST3410", "GOST3411withECGOST3410");
    }

    @Override
    protected byte[] getSignature(RRSIG rrsig) {
        return rrsig.getSignature();
    }

    @Override
    protected PublicKey getPublicKey(DNSKEY key) throws DataMalformedException, DnssecInvalidKeySpecException {
        DataInput dis = key.getKeyAsDataInputStream();
        BigInteger x, y;

        try {
            byte[] xBytes = new byte[LENGTH];
            dis.readFully(xBytes);
            reverse(xBytes);
            x = new BigInteger(1, xBytes);

            byte[] yBytes = new byte[LENGTH];
            dis.readFully(yBytes);
            reverse(yBytes);
            y = new BigInteger(1, yBytes);
        } catch (IOException e) {
            throw new DataMalformedException(e, key.getKey());
        }

        try {
            return getKeyFactory().generatePublic(new ECPublicKeySpec(new ECPoint(x, y), SPEC));
        } catch (InvalidKeySpecException e) {
            throw new DnssecInvalidKeySpecException(e);
        }
    }

    private static void reverse(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int j = array.length - i - 1;
            byte tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }
}
