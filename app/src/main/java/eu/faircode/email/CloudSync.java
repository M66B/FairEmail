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

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;

import androidx.preference.PreferenceManager;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class CloudSync {
    private static final int CLOUD_TIMEOUT = 10 * 1000; // timeout
    private static final int BATCH_SIZE = 25;

    private static final Map<String, Pair<byte[], byte[]>> keyCache = new HashMap<>();

    // Upper level

    static void execute(Context context, String command)
            throws JSONException, GeneralSecurityException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("cloud_user", null);
        String password = prefs.getString("cloud_password", null);

        JSONObject jrequest = new JSONObject();

        if ("sync".equals(command)) {
            DB db = DB.getInstance(context);

            long lrevision = prefs.getLong("sync_status", new Date().getTime());
            Log.i("Cloud local revision=" + lrevision + " (" + new Date(lrevision) + ")");

            Long lastUpdate = null;
            for (EntitySync s : db.sync().getSync(null, null, Long.MAX_VALUE)) {
                Log.i("Cloud sync " + s.entity + ":" + s.reference + " " + s.action + " " + new Date(s.time));
                if (s.reference == null) {
                    Log.w("Cloud reference missing");
                    db.sync().deleteSync(s.id);
                    continue;
                }

                if ("account".equals(s.entity) && "auth".equals(s.reference)) {
                    EntityAccount account = db.account().getAccountByUUID(s.reference);
                    if (account == null || account.auth_type != AUTH_TYPE_PASSWORD) {
                        if (account == null)
                            Log.w("Cloud account missing uuid=" + s.reference);
                        else
                            Log.w("Cloud account auth uuid=" + s.reference);
                        db.sync().deleteSync(s.id);
                        continue;
                    }
                }

                if ("identity".equals(s.entity) && "auth".equals(s.reference)) {
                    EntityIdentity identity = db.identity().getIdentityByUUID(s.reference);
                    if (identity == null || identity.auth_type != AUTH_TYPE_PASSWORD) {
                        if (identity == null)
                            Log.w("Cloud identity missing uuid=" + s.reference);
                        else
                            Log.w("Cloud identity auth uuid=" + s.reference);
                        db.sync().deleteSync(s.id);
                        continue;
                    }
                }

                if (lastUpdate == null || s.time > lastUpdate)
                    lastUpdate = s.time;
            }

            Log.i("Cloud last update=" + (lastUpdate == null ? null : new Date(lastUpdate)));

            if (lastUpdate != null)
                db.sync().deleteSyncByTime(lastUpdate);

            JSONObject jsyncstatus = new JSONObject();
            jsyncstatus.put("key", "sync.status");
            jsyncstatus.put("rev", lrevision);

            JSONArray jitems = new JSONArray();
            jitems.put(jsyncstatus);

            jrequest.put("items", jitems);

            JSONObject jresponse = call(context, user, password, "read", jrequest);
            jitems = jresponse.getJSONArray("items");

            if (jitems.length() == 0) {
                Log.i("Cloud server is empty");
                sendLocalData(context, user, password, lrevision);
            } else if (jitems.length() == 1) {
                Log.i("Cloud sync check");
                jsyncstatus = jitems.getJSONObject(0);
                receiveRemoteData(context, user, password, lrevision, jsyncstatus);
            } else
                throw new IllegalArgumentException("Expected one status item");
        } else {
            JSONArray jitems = new JSONArray();
            jrequest.put("items", jitems);
            call(context, user, password, command, jrequest);
        }

        prefs.edit().putLong("cloud_last_sync", new Date().getTime()).apply();
    }

    private static void sendLocalData(Context context, String user, String password, long lrevision) throws JSONException, GeneralSecurityException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
        Log.i("Cloud accounts=" + (accounts == null ? null : accounts.size()));
        if (accounts == null || accounts.size() == 0) {
            Log.i("Cloud no accounts");
            return;
        }

        JSONArray jupload = new JSONArray();

        JSONArray jaccountuuidlist = new JSONArray();
        for (EntityAccount account : accounts)
            if (!TextUtils.isEmpty(account.uuid)) {
                jaccountuuidlist.put(account.uuid);

                JSONArray jidentitieuuids = new JSONArray();
                List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                if (identities != null)
                    for (EntityIdentity identity : identities)
                        if (!TextUtils.isEmpty(identity.uuid)) {
                            jidentitieuuids.put(identity.uuid);

                            JSONObject jidentity = new JSONObject();
                            jidentity.put("key", "identity." + identity.uuid);
                            jidentity.put("val", identity.toJSON().toString());
                            jidentity.put("rev", lrevision);
                            jupload.put(jidentity);
                        }

                JSONObject jaccountdata = new JSONObject();
                jaccountdata.put("account", account.toJSON());
                jaccountdata.put("identities", jidentitieuuids);

                JSONObject jaccount = new JSONObject();
                jaccount.put("key", "account." + account.uuid);
                jaccount.put("val", jaccountdata.toString());
                jaccount.put("rev", lrevision);
                jupload.put(jaccount);
            }

        JSONObject jaccountuuids = new JSONObject();
        jaccountuuids.put("uuids", jaccountuuidlist);

        JSONObject jstatus = new JSONObject();
        jstatus.put("version", 1);
        jstatus.put("accounts", jaccountuuids);

        JSONObject jsyncstatus = new JSONObject();
        jsyncstatus.put("key", "sync.status");
        jsyncstatus.put("val", jstatus.toString());
        jsyncstatus.put("rev", lrevision);
        jupload.put(jsyncstatus);

        JSONObject jrequest = new JSONObject();
        jrequest.put("items", jupload);
        call(context, user, password, "write", jrequest);

        prefs.edit().putLong("sync_status", lrevision).apply();
    }

    private static void receiveRemoteData(Context context, String user, String password, long lrevision, JSONObject jsyncstatus) throws JSONException, GeneralSecurityException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        long rrevision = jsyncstatus.getLong("rev");
        JSONObject jstatus = new JSONObject(jsyncstatus.getString("val"));
        int version = jstatus.optInt("version", 0);
        Log.i("Cloud version=" + version + " revision=" + lrevision + "/" + rrevision);

        if (BuildConfig.DEBUG && false)
            lrevision--;

        if (rrevision <= lrevision) {
            Log.i("Cloud no changes");
            return;
        }

        // New revision
        JSONArray jdownload = new JSONArray();

        // Get accounts
        JSONObject jaccountstatus = jstatus.getJSONObject("accounts");
        JSONArray jaccountuuidlist = jaccountstatus.getJSONArray("uuids");
        for (int i = 0; i < jaccountuuidlist.length(); i++) {
            String uuid = jaccountuuidlist.getString(i);
            JSONObject jaccount = new JSONObject();
            jaccount.put("key", "account." + uuid);
            jaccount.put("rev", lrevision);
            jdownload.put(jaccount);
            Log.i("Cloud account uuid=" + uuid);
        }

        if (jdownload.length() > 0) {
            Log.i("Cloud getting accounts");
            JSONObject jrequest = new JSONObject();
            jrequest.put("items", jdownload);
            JSONObject jresponse = call(context, user, password, "sync", jrequest);

            // Process accounts
            Log.i("Cloud processing accounts");
            JSONArray jitems = jresponse.getJSONArray("items");
            jdownload = new JSONArray();
            for (int i = 0; i < jitems.length(); i++) {
                JSONObject jaccount = jitems.getJSONObject(i);
                String value = jaccount.getString("val");
                long revision = jaccount.getLong("rev");

                JSONObject jaccountdata = new JSONObject(value);
                EntityAccount raccount = EntityAccount.fromJSON(jaccountdata.getJSONObject("account"));
                EntityAccount laccount = db.account().getAccountByUUID(raccount.uuid);

                JSONArray jidentities = jaccountdata.getJSONArray("identities");
                Log.i("Cloud account " + raccount.uuid + "=" + (laccount == null ? "insert" : "update") +
                        " rev=" + revision +
                        " identities=" + jidentities +
                        " size=" + value.length());

                for (int j = 0; j < jidentities.length(); j++) {
                    JSONObject jidentity = new JSONObject();
                    jidentity.put("key", "identity." + jidentities.getString(j));
                    jidentity.put("rev", lrevision);
                    jdownload.put(jidentity);
                }
            }

            if (jdownload.length() > 0) {
                // Get identities
                Log.i("Cloud getting identities");
                jrequest.put("items", jdownload);
                jresponse = call(context, user, password, "sync", jrequest);

                // Process identities
                Log.i("Cloud processing identities");
                jitems = jresponse.getJSONArray("items");
                for (int i = 0; i < jitems.length(); i++) {
                    JSONObject jidentity = jitems.getJSONObject(i);
                    String value = jidentity.getString("val");
                    long revision = jidentity.getLong("rev");
                    EntityIdentity ridentity = EntityIdentity.fromJSON(new JSONObject(value));
                    EntityIdentity lidentity = db.identity().getIdentityByUUID(ridentity.uuid);
                    Log.i("Cloud identity " + ridentity.uuid + "=" + (lidentity == null ? "insert" : "update") +
                            " rev=" + revision +
                            " size=" + value.length());
                }
            }
        }

        prefs.edit().putLong("sync_status", rrevision).apply();
    }

    // Lower level

    public static JSONObject call(Context context, String user, String password, String command, JSONObject jrequest)
            throws GeneralSecurityException, JSONException, IOException {
        Log.i("Cloud command=" + command);
        jrequest.put("command", command);
        List<JSONObject> responses = new ArrayList<>();
        for (JSONArray batch : partition(jrequest.getJSONArray("items"))) {
            jrequest.put("items", batch);
            responses.add(_call(context, user, password, jrequest));
        }
        if (responses.size() == 1)
            return responses.get(0);
        else {
            JSONArray jall = new JSONArray();
            for (JSONObject response : responses) {
                JSONArray jitems = response.getJSONArray("items");
                for (int i = 0; i < jitems.length(); i++)
                    jall.put(jitems.getJSONObject(i));
            }
            JSONObject jresponse = responses.get(0);
            jresponse.put("items", jall);
            return jresponse;
        }
    }

    private static JSONObject _call(Context context, String user, String password, JSONObject jrequest)
            throws GeneralSecurityException, JSONException, IOException {
        byte[] salt = MessageDigest.getInstance("SHA256").digest(user.getBytes());
        byte[] huser = MessageDigest.getInstance("SHA256").digest(salt);
        byte[] userid = Arrays.copyOfRange(huser, 0, 8);
        String cloudUser = Base64.encodeToString(userid, Base64.NO_PADDING | Base64.NO_WRAP);

        Pair<byte[], byte[]> key;
        String lookup = Helper.hex(salt) + ":" + password;
        synchronized (keyCache) {
            key = keyCache.get(lookup);
        }
        if (key == null) {
            Log.i("Cloud generating key");
            key = getKeyPair(salt, password);
            synchronized (keyCache) {
                keyCache.put(lookup, key);
            }
        } else {
            Log.i("Cloud using cached key");
        }

        String cloudPassword = Base64.encodeToString(key.first, Base64.NO_PADDING | Base64.NO_WRAP);

        jrequest.put("version", 1);
        jrequest.put("username", cloudUser);
        jrequest.put("password", cloudPassword);
        jrequest.put("debug", BuildConfig.DEBUG);

        if (jrequest.has("items")) {
            JSONArray jitems = jrequest.getJSONArray("items");
            for (int i = 0; i < jitems.length(); i++) {
                JSONObject jitem = jitems.getJSONObject(i);
                long revision = jitem.getLong("rev");

                String k = jitem.getString("key");
                jitem.put("key", transform(k, key.second, null, true));

                String v = null;
                if (jitem.has("val") && !jitem.isNull("val")) {
                    v = jitem.getString("val");
                    jitem.put("val", transform(v, key.second, getAd(k, revision), true));
                }
                v = (v == null ? null : "#" + v.length());

                Log.i("Cloud > " + k + "=" + v + " @" + revision);
            }
        }

        String request = jrequest.toString();
        Log.i("Cloud request length=" + request.length());

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
            Log.i("Cloud response length=" + response.length());
            JSONObject jresponse = new JSONObject(response);

            if (jresponse.has("account")) {
                JSONObject jaccount = jresponse.getJSONObject("account");
                if (jaccount.has("consumed"))
                    Log.i("Cloud $$$ account consumed=" + jaccount.get("consumed"));
                if (jaccount.has("metrics"))
                    Log.i("Cloud $$$ account metrics=" + jaccount.get("metrics"));
            }
            if (jresponse.has("consumed"))
                Log.i("Cloud $$$ consumed=" + jresponse.get("consumed"));
            if (jresponse.has("metrics"))
                Log.i("Cloud $$$ metrics=" + jresponse.get("metrics"));

            if (jresponse.has("items")) {
                JSONArray jitems = jresponse.getJSONArray("items");
                for (int i = 0; i < jitems.length(); i++) {
                    JSONObject jitem = jitems.getJSONObject(i);
                    long revision = jitem.getLong("rev");

                    String ekey = jitem.getString("key");
                    String k = transform(ekey, key.second, null, false);
                    jitem.put("key", k);

                    String v = null;
                    if (jitem.has("val") && !jitem.isNull("val")) {
                        String evalue = jitem.getString("val");
                        v = transform(evalue, key.second, getAd(k, revision), false);
                        jitem.put("val", v);
                    }
                    v = (v == null ? null : "#" + v.length());

                    Log.i("Cloud < " + k + "=" + v + " @" + revision);
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

    private static byte[] getAd(String key, long revision) throws NoSuchAlgorithmException {
        byte[] k = MessageDigest.getInstance("SHA256").digest(key.getBytes());
        byte[] ad = ByteBuffer.allocate(8 + 8)
                .putLong(revision)
                .put(Arrays.copyOfRange(k, 0, 8))
                .array();
        return ad;
    }

    private static String transform(String value, byte[] key, byte[] ad, boolean encrypt) throws GeneralSecurityException {
        SecretKeySpec secret = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM-SIV/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[12]);
        cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secret, ivSpec);
        if (ad != null)
            cipher.updateAAD(ad);
        if (encrypt) {
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encrypted, Base64.NO_PADDING | Base64.NO_WRAP);
        } else {
            byte[] encrypted = Base64.decode(value, Base64.NO_PADDING | Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        }
    }

    private static List<JSONArray> partition(JSONArray jarray) throws JSONException {
        if (jarray.length() <= BATCH_SIZE)
            return Arrays.asList(jarray);

        int count = 0;
        List<JSONArray> jpartitions = new ArrayList<>();
        for (int i = 0; i < jarray.length(); i += BATCH_SIZE) {
            JSONArray jpartition = new JSONArray();
            for (int j = 0; j < BATCH_SIZE && i + j < jarray.length(); j++) {
                count++;
                jpartition.put(jarray.get(i + j));
            }
            jpartitions.add(jpartition);
        }

        if (count != jarray.length())
            throw new IllegalArgumentException("Partition error size=" + count + "/" + jarray.length());

        return jpartitions;
    }
}
