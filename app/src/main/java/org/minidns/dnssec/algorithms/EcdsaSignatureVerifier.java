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

import org.minidns.dnssec.DnssecValidationFailedException.DataMalformedException;
import org.minidns.dnssec.DnssecValidationFailedException.DnssecInvalidKeySpecException;
import org.minidns.record.DNSKEY;
import org.minidns.record.RRSIG;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
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

abstract class EcdsaSignatureVerifier extends JavaSecSignatureVerifier {
    private final ECParameterSpec spec;
    private final int length;

    EcdsaSignatureVerifier(BigInteger[] spec, int length, String algorithm) throws NoSuchAlgorithmException {
        this(new ECParameterSpec(new EllipticCurve(new ECFieldFp(spec[0]), spec[1], spec[2]), new ECPoint(spec[3], spec[4]), spec[5], 1), length, algorithm);
    }

    EcdsaSignatureVerifier(ECParameterSpec spec, int length, String algorithm) throws NoSuchAlgorithmException {
        super("EC", algorithm);
        this.length = length;
        this.spec = spec;
    }

    @Override
    protected byte[] getSignature(RRSIG rrsig) throws DataMalformedException {
        DataInput dis = rrsig.getSignatureAsDataInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            byte[] r = new byte[length];
            dis.readFully(r);
            int rlen = (r[0] < 0) ? length + 1 : length;

            byte[] s = new byte[length];
            dis.readFully(s);
            int slen = (s[0] < 0) ? length + 1 : length;

            dos.writeByte(0x30);
            dos.writeByte(rlen + slen + 4);

            dos.writeByte(0x2);
            dos.writeByte(rlen);
            if (rlen > length) dos.writeByte(0);
            dos.write(r);

            dos.writeByte(0x2);
            dos.writeByte(slen);
            if (slen > length) dos.writeByte(0);
            dos.write(s);
        } catch (IOException e) {
            throw new DataMalformedException(e, rrsig.getSignature());
        }

        return bos.toByteArray();
    }

    @Override
    protected PublicKey getPublicKey(DNSKEY key) throws DataMalformedException, DnssecInvalidKeySpecException {
        DataInput dis = key.getKeyAsDataInputStream();
        BigInteger x, y;

        try {
            byte[] xBytes = new byte[length];
            dis.readFully(xBytes);
            x = new BigInteger(1, xBytes);

            byte[] yBytes = new byte[length];
            dis.readFully(yBytes);
            y = new BigInteger(1, yBytes);
        } catch (IOException e) {
            throw new DataMalformedException(e, key.getKey());
        }

        try {
            return getKeyFactory().generatePublic(new ECPublicKeySpec(new ECPoint(x, y), spec));
        } catch (InvalidKeySpecException e) {
            throw new DnssecInvalidKeySpecException(e);
        }
    }

    public static class P256SHA256 extends EcdsaSignatureVerifier {
        private static BigInteger[] SPEC = {
                new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16),
                new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16),
                new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16),
                new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16),
                new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16),
                new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16)
        };

        P256SHA256() throws NoSuchAlgorithmException {
            super(SPEC, 32, "SHA256withECDSA");
        }
    }

    public static class P384SHA284 extends EcdsaSignatureVerifier {
        private static BigInteger[] SPEC = {
                new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFF", 16),
                new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFC", 16),
                new BigInteger("B3312FA7E23EE7E4988E056BE3F82D19181D9C6EFE8141120314088F5013875AC656398D8A2ED19D2A85C8EDD3EC2AEF", 16),
                new BigInteger("AA87CA22BE8B05378EB1C71EF320AD746E1D3B628BA79B9859F741E082542A385502F25DBF55296C3A545E3872760AB7", 16),
                new BigInteger("3617DE4A96262C6F5D9E98BF9292DC29F8F41DBD289A147CE9DA3113B5F0B8C00A60B1CE1D7E819D7A431D7C90EA0E5F", 16),
                new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC7634D81F4372DDF581A0DB248B0A77AECEC196ACCC52973", 16)
        };

        P384SHA284() throws NoSuchAlgorithmException {
            super(SPEC, 48, "SHA384withECDSA");
        }
    }
}
