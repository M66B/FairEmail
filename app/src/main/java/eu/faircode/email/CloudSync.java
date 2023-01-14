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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.util.Base64;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class CloudSync {
    private static final int CLOUD_TIMEOUT = 10 * 1000; // timeout

    public static JSONObject perform(Context context, String user, String password, JSONObject jrequest)
            throws GeneralSecurityException, JSONException, IOException {
        byte[] salt = MessageDigest.getInstance("SHA256").digest(user.getBytes());
        byte[] huser = MessageDigest.getInstance("SHA256").digest(salt);
        byte[] userid = Arrays.copyOfRange(huser, 0, 8);
        String cloudUser = Base64.encodeToString(userid, Base64.NO_PADDING | Base64.NO_WRAP);

        Pair<byte[], byte[]> key = getKeyPair(salt, password);
        String cloudPassword = Base64.encodeToString(key.first, Base64.NO_PADDING | Base64.NO_WRAP);

        jrequest.put("version", 1);
        jrequest.put("username", cloudUser);
        jrequest.put("password", cloudPassword);
        jrequest.put("debug", BuildConfig.DEBUG);

        if (jrequest.has("items")) {
            JSONArray jitems = jrequest.getJSONArray("items");
            for (int i = 0; i < jitems.length(); i++) {
                JSONObject jitem = jitems.getJSONObject(i);
                int revision = jitem.getInt("revision");

                String k = jitem.getString("key");
                jitem.put("key", transform(k, key.second, null, true));

                if (jitem.has("value") && !jitem.isNull("value")) {
                    String v = jitem.getString("value");
                    jitem.put("value", transform(v, key.second, revision, true));
                }
            }
        }

        String request = jrequest.toString();
        Log.i("Cloud request=" + request);

        URL url = new URL(BuildConfig.CLOUD_URI);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setReadTimeout(CLOUD_TIMEOUT);
        connection.setConnectTimeout(CLOUD_TIMEOUT);
        ConnectionHelper.setUserAgent(context, connection);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(request.length()));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();

        try {
            connection.getOutputStream().write(request.getBytes());

            int status = connection.getResponseCode();
            if (status != HttpsURLConnection.HTTP_OK) {
                String error = "Error " + status + ": " + connection.getResponseMessage();
                String detail = Helper.readStream(connection.getErrorStream());
                Log.w("Cloud error=" + error + " detail=" + detail);
                JSONObject jerror = new JSONObject(detail);
                if (status == HttpsURLConnection.HTTP_FORBIDDEN)
                    throw new SecurityException(jerror.optString("error"));
                else
                    throw new IOException(error + " " + jerror);
            }

            String response = Helper.readStream(connection.getInputStream());
            Log.i("Cloud response=" + response);
            JSONObject jresponse = new JSONObject(response);

            if (jresponse.has("items")) {
                JSONArray jitems = jresponse.getJSONArray("items");
                for (int i = 0; i < jitems.length(); i++) {
                    JSONObject jitem = jitems.getJSONObject(i);
                    int revision = jitem.getInt("revision");

                    String ekey = jitem.getString("key");
                    jitem.put("key", transform(ekey, key.second, null, false));

                    if (jitem.has("value") && !jitem.isNull("value")) {
                        String evalue = jitem.getString("value");
                        jitem.put("value", transform(evalue, key.second, revision, false));
                    }
                }
            }

            return jresponse;
        } finally {
            connection.disconnect();
        }
    }

    private static Pair<byte[], byte[]> getKeyPair(byte[] salt, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 310000, 2 * 256);
        SecretKey secret = keyFactory.generateSecret(keySpec);

        byte[] encoded = secret.getEncoded();
        int half = encoded.length / 2;
        return new Pair<>(
                Arrays.copyOfRange(encoded, 0, half),
                Arrays.copyOfRange(encoded, half, half + half));
    }

    private static String transform(String value, byte[] key, Integer revision, boolean encrypt) throws GeneralSecurityException {
        SecretKeySpec secret = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM-SIV/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[12]);
        cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secret, ivSpec);
        if (revision != null)
            cipher.updateAAD(ByteBuffer.allocate(4).putInt(revision).array());
        if (encrypt) {
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encrypted, Base64.NO_PADDING | Base64.NO_WRAP);
        } else {
            byte[] encrypted = Base64.decode(value, Base64.NO_PADDING | Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        }
    }
}
