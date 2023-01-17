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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    static void execute(Context context, String command, boolean manual)
            throws JSONException, GeneralSecurityException, IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("cloud_user", null);
        String password = prefs.getString("cloud_password", null);

        JSONObject jrequest = new JSONObject();

        if ("sync".equals(command)) {
            DB db = DB.getInstance(context);

            long lrevision = prefs.getLong("sync_status", new Date().getTime());
            Log.i("Cloud local revision=" + lrevision + " (" + new Date(lrevision) + ")");

            Long lastUpdate = getLastUpdate(context);
            Log.i("Cloud last update=" + (lastUpdate == null ? null : new Date(lastUpdate)));
            if (lastUpdate != null && lrevision > lastUpdate)
                Log.w("Cloud invalid local revision" +
                        " lrevision=" + lrevision + " last=" + lastUpdate);

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
                long rrevision = jsyncstatus.getLong("rev");
                JSONObject jstatus = new JSONObject(jsyncstatus.getString("val"));
                int sync_version = jstatus.optInt("sync.version", 0);
                int app_version = jstatus.optInt("app.version", 0);
                Log.i("Cloud version sync=" + sync_version + " app=" + app_version +
                        " local=" + lrevision + " last=" + lastUpdate + " remote=" + rrevision);

                // last > local (local mods) && remote > local (remote mods) = CONFLICT
                // local > last = ignorable ERROR
                // remote > local = fetch remote
                // last > remote = send local

                if (lastUpdate != null && lastUpdate > rrevision) // local newer than remote
                    sendLocalData(context, user, password, lastUpdate);
                else if (rrevision > lrevision) // remote changes
                    if (lastUpdate != null && lastUpdate > lrevision) { // local changes
                        Log.w("Cloud conflict" +
                                " lrevision=" + lrevision + " last=" + lastUpdate + " rrevision=" + rrevision);
                        if (manual)
                            if (lastUpdate >= rrevision)
                                sendLocalData(context, user, password, lastUpdate);
                            else
                                receiveRemoteData(context, user, password, lrevision, jstatus);
                    } else
                        receiveRemoteData(context, user, password, lrevision, jstatus);
                else if (BuildConfig.DEBUG)
                    receiveRemoteData(context, user, password, lrevision - 1, jstatus);
            } else
                throw new IllegalArgumentException("Expected one status item");

            if (lastUpdate != null)
                db.sync().deleteSyncByTime(lastUpdate);
        } else {
            JSONArray jitems = new JSONArray();
            jrequest.put("items", jitems);
            call(context, user, password, command, jrequest);
        }

        prefs.edit().putLong("cloud_last_sync", new Date().getTime()).apply();
    }

    private static Long getLastUpdate(Context context) {
        DB db = DB.getInstance(context);

        Long lastUpdate = null;

        for (EntitySync sync : db.sync().getSync(null, null, Long.MAX_VALUE)) {
            Log.i("Cloud sync " + sync.entity + ":" + sync.reference + " " + sync.action + " " + new Date(sync.time));
            if (sync.reference == null) {
                Log.w("Cloud reference missing");
                db.sync().deleteSync(sync.id);
                continue;
            }

            if ("account".equals(sync.entity) && "auth".equals(sync.action)) {
                EntityAccount account = db.account().getAccountByUUID(sync.reference);
                if (account == null || account.auth_type != AUTH_TYPE_PASSWORD) {
                    if (account == null)
                        Log.w("Cloud account missing uuid=" + sync.reference);
                    else
                        Log.i("Cloud account oauth uuid=" + sync.reference);
                    db.sync().deleteSync(sync.id);
                    continue;
                }
            }

            if ("identity".equals(sync.entity) && "auth".equals(sync.action)) {
                EntityIdentity identity = db.identity().getIdentityByUUID(sync.reference);
                if (identity == null || identity.auth_type != AUTH_TYPE_PASSWORD) {
                    if (identity == null)
                        Log.w("Cloud identity missing uuid=" + sync.reference);
                    else
                        Log.i("Cloud identity oauth uuid=" + sync.reference);
                    db.sync().deleteSync(sync.id);
                    continue;
                }
            }

            if (lastUpdate == null || sync.time > lastUpdate)
                lastUpdate = sync.time;
        }

        return lastUpdate;
    }

    private static void sendLocalData(Context context, String user, String password, long lrevision)
            throws JSONException, GeneralSecurityException, IOException {
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
                            jidentity.put("val", toJSON(identity).toString());
                            jidentity.put("rev", lrevision);
                            jupload.put(jidentity);
                        }

                JSONObject jaccountdata = new JSONObject();
                jaccountdata.put("account", toJSON(account));
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
        jstatus.put("sync.version", 1);
        jstatus.put("app.version", BuildConfig.VERSION_CODE);
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

    private static void receiveRemoteData(Context context, String user, String password, long lrevision, JSONObject jstatus)
            throws JSONException, GeneralSecurityException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sync_accounts = prefs.getBoolean("cloud_sync_accounts", true);
        boolean sync_accounts_delete = prefs.getBoolean("cloud_sync_accounts_delete", false);
        boolean sync_blocked_senders = prefs.getBoolean("cloud_sync_blocked_senders", true);
        boolean sync_filter_rules = prefs.getBoolean("cloud_sync_filter_rules", true);

        // New revision
        boolean updates = false;
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
                EntityAccount raccount = accountFromJSON(jaccountdata.getJSONObject("account"));
                EntityAccount laccount = db.account().getAccountByUUID(raccount.uuid);

                JSONArray jidentities = jaccountdata.getJSONArray("identities");
                Log.i("Cloud account " + raccount.uuid + "=" +
                        (laccount == null ? "insert" : (areEqual(toJSON(raccount), toJSON(laccount)) ? "equal" : "update")) +
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
                    EntityIdentity ridentity = identityFromJSON(new JSONObject(value));
                    EntityIdentity lidentity = db.identity().getIdentityByUUID(ridentity.uuid);

                    Log.i("Cloud identity " + ridentity.uuid + "=" +
                            (lidentity == null ? "insert" : (areEqual(toJSON(ridentity), toJSON(lidentity)) ? "equal" : "update")) +
                            " rev=" + revision +
                            " size=" + value.length());
                }
            }
        }

        prefs.edit().putLong("sync_status", lrevision).apply();

        if (updates)
            ServiceSynchronize.reload(context, null, true, "sync");
    }

    private static boolean areEqual(JSONObject o1, JSONObject o2) throws JSONException {
        if (o1 == null && o2 == null)
            return true;
        if (o1 == null || o2 == null)
            return false;

        Iterator<String> i1 = o1.keys();
        while (i1.hasNext()) {
            String k1 = i1.next();
            if (!o2.has(k1))
                return false;
        }

        Iterator<String> i2 = o2.keys();
        while (i2.hasNext()) {
            String k2 = i2.next();
            if (!o2.has(k2))
                return false;
            if (!Objects.equals(o1.get(k2), o2.get(k2)))
                return false;
        }

        return true;
    }

    private static JSONObject toJSON(EntityAccount account) throws JSONException {
        JSONObject json = new JSONObject();
        if (account == null)
            return json;
        //json.put("id", id);
        json.put("uuid", account.uuid);
        //json.put("order", order);
        json.put("protocol", account.protocol);
        json.put("host", account.host);
        json.put("encryption", account.encryption);
        json.put("insecure", account.insecure);
        json.put("port", account.port);
        json.put("auth_type", account.auth_type);
        json.put("provider", account.provider);
        json.put("user", account.user);
        json.put("password", account.password);
        //json.put("certificate_alias", certificate_alias);
        json.put("realm", account.realm);
        json.put("fingerprint", account.fingerprint);

        //json.put("name", name);
        //json.put("category", category);
        //json.put("color", color);
        //json.put("calendar", calendar);

        //json.put("synchronize", synchronize);
        //json.put("ondemand", ondemand);
        //json.put("poll_exempted", poll_exempted);
        //json.put("primary", primary);
        //json.put("notify", notify);
        //json.put("browse", browse);
        //json.put("leave_on_server", leave_on_server);
        //json.put("leave_deleted", leave_deleted);
        //json.put("leave_on_device", leave_on_device);
        //json.put("max_messages", max_messages);
        //json.put("auto_seen", auto_seen);
        // not separator

        //json.put("swipe_left", swipe_left);
        //json.put("swipe_right", swipe_right);

        //json.put("move_to", move_to);

        json.put("poll_interval", account.poll_interval);
        json.put("keep_alive_noop", account.keep_alive_noop);
        json.put("partial_fetch", account.partial_fetch);
        json.put("ignore_size", account.ignore_size);
        json.put("use_date", account.use_date);
        json.put("use_received", account.use_received);
        json.put("unicode", account.unicode);
        //json.put("conditions", conditions);
        // not prefix
        // not created
        // not tbd
        // not state
        // not warning
        // not error
        // not last connected
        return json;
    }

    public static EntityAccount accountFromJSON(JSONObject json) throws JSONException {
        EntityAccount account = new EntityAccount();
        account.uuid = json.getString("uuid");
        if (json.has("protocol"))
            account.protocol = json.getInt("protocol");

        account.host = json.getString("host");
        account.encryption = json.getInt("encryption");
        account.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        account.port = json.getInt("port");
        account.auth_type = json.getInt("auth_type");
        if (json.has("provider") && !json.isNull("provider"))
            account.provider = json.getString("provider");
        account.user = json.getString("user");
        account.password = json.getString("password");
        if (json.has("realm") && !json.isNull("realm"))
            account.realm = json.getString("realm");
        if (json.has("fingerprint") && !json.isNull("fingerprint"))
            account.fingerprint = json.getString("fingerprint");

        account.poll_interval = json.getInt("poll_interval");
        account.keep_alive_noop = json.optBoolean("keep_alive_noop");

        account.partial_fetch = json.optBoolean("partial_fetch", true);
        account.ignore_size = json.optBoolean("ignore_size", false);
        account.use_date = json.optBoolean("use_date", false);
        account.use_received = json.optBoolean("use_received", false);
        account.unicode = json.optBoolean("unicode", false);

        return account;
    }

    private static JSONObject toJSON(EntityIdentity identity) throws JSONException {
        JSONObject json = new JSONObject();
        if (identity == null)
            return json;
        //json.put("id", id);
        json.put("uuid", identity.uuid);
        //json.put("name", name);
        json.put("email", identity.email);
        // not account
        //json.put("display", display);
        //if (color != null)
        //    json.put("color", color);
        //TODO json.put("signature", signature);

        json.put("host", identity.host);
        json.put("encryption", identity.encryption);
        json.put("insecure", identity.insecure);
        json.put("port", identity.port);
        json.put("auth_type", identity.auth_type);
        json.put("provider", identity.provider);
        json.put("user", identity.user);
        json.put("password", identity.password);
        //json.put("certificate_alias", certificate_alias);
        json.put("realm", identity.realm);
        json.put("fingerprint", identity.fingerprint);
        json.put("use_ip", identity.use_ip);
        json.put("ehlo", identity.ehlo);

        //json.put("synchronize", synchronize);
        //json.put("primary", primary);
        //TODO json.put("self", self);
        //TODO json.put("sender_extra", sender_extra);
        //TODO json.put("sender_extra_name", sender_extra_name);
        //TODO json.put("sender_extra_regex", sender_extra_regex);

        //json.put("replyto", replyto);
        //json.put("cc", cc);
        //json.put("bcc", bcc);
        //json.put("internal", internal);

        json.put("unicode", identity.unicode);
        json.put("octetmime", identity.octetmime);
        // not plain_only
        //json.put("sign_default", sign_default);
        //json.put("encrypt_default", encrypt_default);
        // not encrypt
        // delivery_receipt
        // read_receipt
        // not store_sent
        // not sent_folder
        // not sign_key
        // sign_key_alias
        // not tbd
        // not state
        // not error
        // not last_connected
        // not max_size
        return json;
    }

    public static EntityIdentity identityFromJSON(JSONObject json) throws JSONException {
        EntityIdentity identity = new EntityIdentity();
        identity.uuid = json.getString("uuid");
        identity.email = json.getString("email");

        identity.host = json.getString("host");
        identity.encryption = json.getInt("encryption");
        identity.insecure = json.optBoolean("insecure");
        identity.port = json.getInt("port");
        identity.auth_type = json.getInt("auth_type");
        if (json.has("provider") && !json.isNull("provider"))
            identity.provider = json.getString("provider");
        identity.user = json.getString("user");
        identity.password = json.getString("password");
        if (json.has("realm") && !json.isNull("realm"))
            identity.realm = json.getString("realm");
        if (json.has("fingerprint") && !json.isNull("fingerprint"))
            identity.fingerprint = json.getString("fingerprint");
        if (json.has("use_ip"))
            identity.use_ip = json.getBoolean("use_ip");
        if (json.has("ehlo") && !json.isNull("ehlo"))
            identity.ehlo = json.getString("ehlo");

        identity.unicode = json.optBoolean("unicode");
        identity.octetmime = json.optBoolean("octetmime");

        return identity;
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

    private static String transform(String value, byte[] key, byte[] ad, boolean encrypt)
            throws GeneralSecurityException, IOException {
        SecretKeySpec secret = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM-SIV/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[12]);
        cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secret, ivSpec);
        if (ad != null)
            cipher.updateAAD(ad);
        if (encrypt) {
            byte[] encrypted = cipher.doFinal(compress(value.getBytes()));
            return Base64.encodeToString(encrypted, Base64.NO_PADDING | Base64.NO_WRAP);
        } else {
            byte[] encrypted = Base64.decode(value, Base64.NO_PADDING | Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decompress(decrypted));
        }
    }

    public static byte[] compress(byte[] data) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length)) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(data);
            }
            return bos.toByteArray();
        }
    }

    public static byte[] decompress(byte[] compressed) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(compressed)) {
            try (GZIPInputStream gis = new GZIPInputStream(is)) {
                return Helper.readBytes(gis);
            }
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
