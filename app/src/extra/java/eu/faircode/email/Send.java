package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import androidx.documentfile.provider.DocumentFile;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Send {
    // https://datatracker.ietf.org/doc/html/rfc8188
    // https://github.com/nneonneo/ffsend/blob/master/ffsend.py

    /*
        curl --request POST \
            --url https://send.zcyph.cc/api/info/... \
            --header 'Content-Type: application/json' \
            --data '{"owner_token": "..."}'
    */

    static final int DEFAULT_DLIMIT = 10;
    static final int DEFAULT_TLIMIT = 3 * 24; // hours
    static final String DEFAULT_SERVER = "https://send.vis.ee/";

    private static final int TIMEOUT = 20 * 1000; // milliseconds

    public static String upload(InputStream is, DocumentFile dfile, int dLimit, int timeLimit, String host, IProgress intf) throws Throwable {
        String result;
        SecureRandom rnd = new SecureRandom();

        byte[] secret = new byte[16];
        rnd.nextBytes(secret);

        JSONObject jupload = getMetadata(dfile, dLimit, timeLimit, secret);

        Uri uri = Uri.parse("wss://" + Uri.parse(host).getHost() + "/api/ws");

        WebSocket ws = new WebSocketFactory().createSocket(uri.toString(), TIMEOUT);
        ws.setFrameQueueSize(32); // 32 x 64KB = 2 MB

        Semaphore sem = new Semaphore(0);
        List<String> queue = Collections.synchronizedList(new ArrayList<>());

        ws.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket ws, String text) {
                Log.i("Send text message=" + text);
                queue.add(text);
                sem.release();
            }
        });

        Log.i("Send connect");
        ws.connect();

        try {
            Log.i("Send upload=" + jupload);
            ws.sendText(jupload.toString());

            Log.i("Send wait reply");
            if (!sem.tryAcquire(TIMEOUT, TimeUnit.MILLISECONDS))
                throw new TimeoutException("reply");

            JSONObject jreply = new JSONObject(queue.remove(0));
            Log.i("Send reply=" + jreply);

            if (jreply.has("error")) {
                String error = jreply.getString("error");
                if ("400".equals(error))
                    error += " - try lower limits";
                throw new IOException("Error: " + error);
            }

            result = jreply.getString("url") +
                    "#" + Base64.encodeToString(secret, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            Log.i("Send url=" + result);

            // The record sequence number (SEQ) is a 96-bit unsigned integer in network byte order that starts at zero.
            // network byte order = transmitting the most significant byte first
            // Java = big endian = network byte order
            // sizeof(int) = 4 bytes
            // sizeof(long) = 8 bytes
            long seq = 0;
            byte[] buffer = new byte[65536];

            byte[] salt = new byte[16];
            rnd.nextBytes(salt);

            // https://datatracker.ietf.org/doc/html/rfc8188#section-2.1
            byte[] header = new byte[16 /* salt */ + 4 /* rs */ + 1 /* idlen */ + 0 /* keyid */];
            ByteBuffer.wrap(header)
                    .put(salt)
                    .putInt(salt.length, buffer.length);
            // idlen = 0
            // keyid = ""

            Log.i("Send header=" + Helper.hex(header));
            ws.sendBinary(header);

            // https://datatracker.ietf.org/doc/html/rfc8188#section-2.2

            // cek_info = "Content-Encoding: aes128gcm" || 0x00
            // CEK = HMAC-SHA-256(PRK, cek_info || 0x01)
            // AEAD_AES_128_GCM requires a 16-octet (128-bit) content-encryption key (CEK),
            //   so the length (L) parameter to HKDF is 16
            byte[] cek = new byte[16];
            HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
            hkdf.init(new HKDFParameters(secret /* ikm */, salt, "Content-Encoding: aes128gcm\0".getBytes()));
            hkdf.generateBytes(cek /* okm */, 0, cek.length);

            // nonce_info = "Content-Encoding: nonce" || 0x00
            // NONCE = HMAC-SHA-256(PRK, nonce_info || 0x01) XOR SEQ
            // The length (L) parameter is 12 octets.
            byte[] nonce_base = new byte[12];
            hkdf = new HKDFBytesGenerator(new SHA256Digest());
            hkdf.init(new HKDFParameters(secret /* ikm */, salt, "Content-Encoding: nonce\0".getBytes()));
            hkdf.generateBytes(nonce_base /* okm */, 0, nonce_base.length);
            Log.i("Send nonce base=" + Helper.hex(nonce_base));

            // TODO zero length files
            int len;
            long size = 0;
            long fileSize = dfile.length();
            // content any length up to rs-17 octets
            while ((len = is.read(buffer, 0, buffer.length - 17)) > 0) {
                Log.i("Send read=" + len);

                size += len;
                intf.onProgress((int) (100 * size / fileSize));

                // add a delimiter octet (0x01 or 0x02)
                //   then 0x00-valued octets to rs-16 (or less on the last record)
                // The last record uses a padding delimiter octet set to the value 2,
                //   all other records have a padding delimiter octet value of 1.
                if (size == fileSize)
                    buffer[len++] = 0x02;
                else {
                    buffer[len++] = 0x01;
                    while (len < buffer.length - 17)
                        buffer[len++] = 0x00;
                }
                Log.i("Send record len=" + len + " size=" + size + "/" + fileSize);

                byte[] nonce = Arrays.copyOf(nonce_base, nonce_base.length);
                ByteBuffer xor = ByteBuffer.wrap(nonce);
                xor.putLong(nonce.length - 8, xor.getLong(nonce.length - 8) ^ seq);
                Log.i("Send seq=" + seq + " nonce=" + Helper.hex(nonce));

                // encrypt with AEAD_AES_128_GCM; final size is rs; the last record can be smaller
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(cek, "AES"),
                        new GCMParameterSpec(16 * 8, nonce));
                byte[] message = cipher.doFinal(buffer, 0, len);
                Log.i("Send message len=" + message.length);
                ws.sendBinary(message);

                seq++;

                if (!intf.isRunning())
                    throw new InterruptedException();
            }

            Log.i("Send EOF size=" + size);
            ws.sendBinary(new byte[]{0}, true);

            Log.i("Send wait confirm");
            if (!sem.tryAcquire(TIMEOUT, TimeUnit.MILLISECONDS))
                throw new TimeoutException("confirm");

            JSONObject jconfirm = new JSONObject(queue.remove(0));
            Log.i("Send confirm=" + jconfirm);
            if (!jconfirm.optBoolean("ok"))
                throw new FileNotFoundException(jconfirm.toString());
        } finally {
            ws.disconnect();
        }

        return result;
    }

    private static JSONObject getMetadata(DocumentFile dfile, int dLimit, int timeLimit, byte[] secret)
            throws JSONException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String fileName = dfile.getName();
        long fileSize = dfile.length();
        String mimeType = dfile.getType();

        if (TextUtils.isEmpty(mimeType))
            mimeType = Helper.guessMimeType(fileName);

        JSONObject jfile = new JSONObject();
        jfile.put("name", fileName);
        jfile.put("size", fileSize);
        jfile.put("type", mimeType);

        JSONArray jfiles = new JSONArray();
        jfiles.put(jfile);

        JSONObject jmanifest = new JSONObject();
        jmanifest.put("files", jfiles);

        JSONObject jmeta = new JSONObject();
        jmeta.put("name", fileName); // Shown on website
        jmeta.put("size", fileSize);
        jmeta.put("type", mimeType);
        jmeta.put("manifest", jmanifest);

        Log.i("Send meta=" + jmeta);

        byte[] auth_key = new byte[64];
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(secret /* ikm */, new byte[0] /* salt */, "authentication".getBytes()));
        hkdf.generateBytes(auth_key /* okm */, 0, auth_key.length);

        byte[] meta_key = new byte[16];
        hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(secret /* ikm */, new byte[0] /* salt */, "metadata".getBytes()));
        hkdf.generateBytes(meta_key /* okm */, 0, meta_key.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(meta_key, "AES"),
                new GCMParameterSpec(16 * 8, new byte[12]));

        byte[] metadata = cipher.doFinal(jmeta.toString().getBytes());

        JSONObject jupload = new JSONObject();
        jupload.put("fileMetadata", Base64.encodeToString(metadata, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
        jupload.put("authorization", "send-v1 " + Base64.encodeToString(auth_key, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
        jupload.put("dlimit", dLimit);
        jupload.put("timeLimit", timeLimit); // seconds

        return jupload;
    }

    public interface IProgress {
        void onProgress(int percentage);

        boolean isRunning();
    }
}
