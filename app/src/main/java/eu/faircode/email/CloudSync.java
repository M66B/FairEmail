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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(password))
            return;
        if (!ActivityBilling.isPro(context))
            return;

        JSONObject jrequest = new JSONObject();

        EntityLog.log(context, EntityLog.Type.Cloud, "Cloud command=" + command);
        if ("sync".equals(command)) {
            long lrevision = prefs.getLong("cloud_lrevision", 0);
            EntityLog.log(context, EntityLog.Type.Cloud,
                    "Cloud local revision=" + lrevision + " (" + new Date(lrevision) + ")");

            Long lastUpdate = updateSyncdata(context);
            EntityLog.log(context, EntityLog.Type.Cloud,
                    "Cloud last update=" + (lastUpdate == null ? null : new Date(lastUpdate)));
            if (lastUpdate != null && lrevision > lastUpdate) {
                String msg = "Cloud invalid local revision" +
                        " lrevision=" + lrevision + " last=" + lastUpdate;
                Log.w(msg);
                EntityLog.log(context, EntityLog.Type.Cloud, msg);
                lrevision = lastUpdate;
                prefs.edit().putLong("cloud_lrevision", lrevision).apply();
            }

            JSONObject jsyncstatus = new JSONObject();
            jsyncstatus.put("key", "sync.status");
            jsyncstatus.put("rev", lrevision);

            JSONArray jitems = new JSONArray();
            jitems.put(jsyncstatus);

            jrequest.put("items", jitems);

            JSONObject jresponse = call(context, user, password, "read", jrequest);
            jitems = jresponse.getJSONArray("items");

            if (jitems.length() == 0) {
                EntityLog.log(context, EntityLog.Type.Cloud, "Cloud server is empty");
                sendLocalData(context, user, password, lrevision == 0
                        ? (lastUpdate == null ? new Date().getTime() : lastUpdate)
                        : lrevision);
            } else if (jitems.length() == 1) {
                EntityLog.log(context, EntityLog.Type.Cloud, "Cloud sync check");
                jsyncstatus = jitems.getJSONObject(0);
                long rrevision = jsyncstatus.getLong("rev");
                JSONObject jstatus = new JSONObject(jsyncstatus.getString("val"));
                int sync_version = jstatus.optInt("sync.version", 0);
                int app_version = jstatus.optInt("app.version", 0);
                EntityLog.log(context, EntityLog.Type.Cloud,
                        "Cloud version sync=" + sync_version + " app=" + app_version +
                                " local=" + lrevision + " last=" + lastUpdate + " remote=" + rrevision);

                // last > local (local mods) && remote > local (remote mods) = CONFLICT
                // local > last = ignorable ERROR
                // remote > local = fetch remote
                // last > remote = send local

                if (lastUpdate != null && lastUpdate > rrevision) // local newer than remote
                    sendLocalData(context, user, password, lastUpdate);
                else if (rrevision > lrevision) // remote changes
                    if (lastUpdate != null && lastUpdate > lrevision) { // local changes
                        EntityLog.log(context, EntityLog.Type.Cloud,
                                "Cloud conflict" +
                                        " lrevision=" + lrevision + " last=" + lastUpdate + " rrevision=" + rrevision);
                        if (manual)
                            if (lastUpdate >= rrevision)
                                sendLocalData(context, user, password, lastUpdate);
                            else
                                receiveRemoteData(context, user, password, lrevision, rrevision, jstatus);
                    } else
                        receiveRemoteData(context, user, password, lrevision, rrevision, jstatus);
                else if (BuildConfig.DEBUG && false)
                    receiveRemoteData(context, user, password, lrevision - 1, rrevision, jstatus);
            } else
                throw new IllegalArgumentException("Expected one status item");
        } else {
            JSONArray jitems = new JSONArray();
            jrequest.put("items", jitems);
            call(context, user, password, command, jrequest);
        }

        prefs.edit().putLong("cloud_last_sync", new Date().getTime()).apply();
    }

    private static Long updateSyncdata(Context context) throws IOException, JSONException {
        DB db = DB.getInstance(context);
        File dir = Helper.ensureExists(new File(context.getFilesDir(), "syncdata"));

        Long last = null;

        List<EntityAccount> accounts = db.account().getAccounts();
        if (accounts != null)
            for (EntityAccount account : accounts)
                if (account.synchronize && !TextUtils.isEmpty(account.uuid)) {
                    EntityAccount aexisting = null;
                    File afile = new File(dir, "account." + account.uuid + ".json");
                    if (afile.exists())
                        try (InputStream is = new FileInputStream(afile)) {
                            aexisting = EntityAccount.fromJSON(new JSONObject(Helper.readStream(is)));
                        }

                    boolean apassword = (account.auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD);
                    if (aexisting == null ||
                            !EntityAccount.areEqual(account, aexisting, apassword, false)) {
                        Helper.writeText(afile, account.toJSON().toString());
                        if (account.last_modified != null)
                            afile.setLastModified(account.last_modified);
                    }

                    long atime = afile.lastModified();
                    if (last == null || atime > last)
                        last = atime;

                    List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                    if (identities != null)
                        for (EntityIdentity identity : identities)
                            if (identity.synchronize && !TextUtils.isEmpty(identity.uuid)) {
                                EntityIdentity iexisting = null;
                                File ifile = new File(dir, "identity." + identity.uuid + ".json");
                                if (ifile.exists())
                                    try (InputStream is = new FileInputStream(ifile)) {
                                        iexisting = EntityIdentity.fromJSON(new JSONObject(Helper.readStream(is)));
                                    }

                                boolean ipassword = (account.auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD);
                                if (iexisting == null ||
                                        !EntityIdentity.areEqual(identity, iexisting, ipassword, false)) {
                                    Helper.writeText(ifile, identity.toJSON().toString());
                                    if (identity.last_modified != null)
                                        ifile.setLastModified(identity.last_modified);
                                }

                                long itime = ifile.lastModified();
                                if (itime > last)
                                    last = itime;
                            }
                }

        return last;
    }

    private static void sendLocalData(Context context, String user, String password, long lrevision)
            throws JSONException, GeneralSecurityException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean cloud_send = prefs.getBoolean("cloud_send", true);

        if (!cloud_send) {
            EntityLog.log(context, EntityLog.Type.Cloud, "Cloud skip send");
            return;
        }

        List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
        EntityLog.log(context, EntityLog.Type.Cloud,
                "Cloud accounts=" + (accounts == null ? null : accounts.size()));
        if (accounts == null || accounts.size() == 0)
            return;

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

                            JSONObject jidentity = identity.toJSON();
                            jidentity.put("account_uuid", account.uuid);

                            JSONObject jidentitykv = new JSONObject();
                            jidentitykv.put("key", "identity." + identity.uuid);
                            jidentitykv.put("val", jidentity.toString());
                            jidentitykv.put("rev", lrevision);
                            jupload.put(jidentitykv);
                        }

                JSONObject jaccount = account.toJSON();
                if (account.swipe_left != null && account.swipe_left > 0) {
                    EntityFolder f = db.folder().getFolder(account.swipe_left);
                    if (f != null) {
                        jaccount.put("swipe_left_name", f.name);
                        jaccount.put("swipe_left_type", f.type);
                    }
                }
                if (account.swipe_right != null && account.swipe_right > 0) {
                    EntityFolder f = db.folder().getFolder(account.swipe_right);
                    if (f != null) {
                        jaccount.put("swipe_right_name", f.name);
                        jaccount.put("swipe_right_type", f.name);
                    }
                }

                JSONObject jaccountdata = new JSONObject();
                jaccountdata.put("account", jaccount);
                jaccountdata.put("identities", jidentitieuuids);

                JSONObject jaccountkv = new JSONObject();
                jaccountkv.put("key", "account." + account.uuid);
                jaccountkv.put("val", jaccountdata.toString());
                jaccountkv.put("rev", lrevision);
                jupload.put(jaccountkv);
            }

        JSONObject jaccountuuids = new JSONObject();
        jaccountuuids.put("uuids", jaccountuuidlist);

        JSONObject jstatus = new JSONObject();
        jstatus.put("sync.version", 1);
        jstatus.put("app.version", BuildConfig.VERSION_CODE);
        jstatus.put("accounts", jaccountuuids);

        JSONObject jstatuskv = new JSONObject();
        jstatuskv.put("key", "sync.status");
        jstatuskv.put("val", jstatus.toString());
        jstatuskv.put("rev", lrevision);
        jupload.put(jstatuskv);

        JSONObject jrequest = new JSONObject();
        jrequest.put("items", jupload);
        call(context, user, password, "write", jrequest);

        prefs.edit().putLong("cloud_lrevision", lrevision).apply();
    }

    private static void receiveRemoteData(Context context, String user, String password, long lrevision, long rrevision, JSONObject jstatus)
            throws JSONException, GeneralSecurityException, IOException {
        DB db = DB.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean cloud_receive = prefs.getBoolean("cloud_receive", true);

        if (!cloud_receive) {
            EntityLog.log(context, EntityLog.Type.Cloud, "Cloud skip receive");
            return;
        }

        // New revision
        boolean updates = false;
        JSONArray jdownload = new JSONArray();

        // Get accounts
        JSONObject jaccountstatus = jstatus.getJSONObject("accounts");
        JSONArray jaccountuuidlist = jaccountstatus.getJSONArray("uuids");
        for (int i = 0; i < jaccountuuidlist.length(); i++) {
            String uuid = jaccountuuidlist.getString(i);
            JSONObject jaccountkv = new JSONObject();
            jaccountkv.put("key", "account." + uuid);
            jaccountkv.put("rev", lrevision);
            jdownload.put(jaccountkv);
            EntityLog.log(context, EntityLog.Type.Cloud, "Cloud account uuid=" + uuid);
        }

        if (jdownload.length() > 0) {
            EntityLog.log(context, EntityLog.Type.Cloud, "Cloud getting accounts");
            JSONObject jrequest = new JSONObject();
            jrequest.put("items", jdownload);
            JSONObject jresponse = call(context, user, password, "sync", jrequest);

            // Process accounts
            EntityLog.log(context, EntityLog.Type.Cloud, "Cloud processing accounts");
            JSONArray jitems = jresponse.getJSONArray("items");
            jdownload = new JSONArray();
            for (int i = 0; i < jitems.length(); i++) {
                JSONObject jaccountkv = jitems.getJSONObject(i);
                String value = jaccountkv.getString("val");
                long revision = jaccountkv.getLong("rev");

                JSONObject jaccountdata = new JSONObject(value);
                JSONObject jaccount = jaccountdata.getJSONObject("account");
                JSONArray jidentities = jaccountdata.getJSONArray("identities");
                EntityAccount raccount = EntityAccount.fromJSON(jaccount);
                EntityAccount laccount = db.account().getAccountByUUID(raccount.uuid);

                EntityFolder left = null;
                if (jaccount.has("swipe_left_name") && !jaccount.isNull("swipe_left_name")) {
                    left = new EntityFolder();
                    left.name = jaccount.getString("swipe_left_name");
                    left.type = jaccount.getString("swipe_left_type");
                }

                EntityFolder right = null;
                if (jaccount.has("swipe_right_name") && !jaccount.isNull("swipe_right_name")) {
                    right = new EntityFolder();
                    right.name = jaccount.getString("swipe_right_name");
                    right.type = jaccount.getString("swipe_right_type");
                }

                EntityLog.log(context, EntityLog.Type.Cloud,
                        "Cloud account " + raccount.uuid + "=" +
                                (laccount == null ? "insert" :
                                        (EntityAccount.areEqual(raccount, laccount,
                                                laccount.auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD, true)
                                                ? "equal" : "update")) +
                                " rev=" + revision +
                                " left=" + (left == null ? null : left.name + ":" + left.type) +
                                " right=" + (right == null ? null : right.name + ":" + right.type) +
                                " identities=" + jidentities +
                                " size=" + value.length());

                raccount.id = null;

                try {
                    db.beginTransaction();

                    if (laccount == null) {
                        raccount.notify = false; // TODO
                        if (raccount.swipe_left != null && raccount.swipe_left > 0)
                            raccount.swipe_left = null;
                        if (raccount.swipe_right != null && raccount.swipe_right > 0)
                            raccount.swipe_right = null;
                        raccount.move_to = null; // TODO
                        raccount.id = db.account().insertAccount(raccount);

                        if (raccount.protocol == EntityAccount.TYPE_POP) {
                            for (EntityFolder folder : EntityFolder.getPopFolders(context)) {
                                EntityFolder existing = db.folder().getFolderByType(raccount.id, folder.type);
                                if (existing == null) {
                                    folder.account = raccount.id;
                                    folder.id = db.folder().insertFolder(folder);
                                }
                            }
                        } else {
                            if (left != null) {
                                left.account = raccount.id;
                                left.setProperties();
                                left.setSpecials(raccount);
                                left.id = db.folder().insertFolder(left);
                            }

                            if (right != null) {
                                right.account = raccount.id;
                                right.setProperties();
                                right.setSpecials(raccount);
                                right.id = db.folder().insertFolder(right);
                            }

                            db.account().setAccountSwipes(raccount.id,
                                    left == null ? null : left.id,
                                    right == null ? null : right.id);
                        }
                    } else {
                        raccount.id = laccount.id;
                        raccount.notify = laccount.notify; // TODO
                        raccount.swipe_left = laccount.swipe_left; // TODO
                        raccount.swipe_right = laccount.swipe_right; // TODO
                        raccount.move_to = laccount.move_to; // TODO
                        db.account().updateAccount(raccount);
                    }

                    if (raccount.id != null) {
                        if (raccount.primary) {
                            db.account().resetPrimary();
                            db.account().setAccountPrimary(raccount.id, true);
                        }
                        db.account().setAccountLastModified(raccount.id, rrevision);
                    }

                    db.setTransactionSuccessful();

                    updates = true;
                } finally {
                    db.endTransaction();
                }

                for (int j = 0; j < jidentities.length(); j++) {
                    JSONObject jidentitykv = new JSONObject();
                    jidentitykv.put("key", "identity." + jidentities.getString(j));
                    jidentitykv.put("rev", lrevision);
                    jdownload.put(jidentitykv);
                }
            }

            if (jdownload.length() > 0) {
                // Get identities
                EntityLog.log(context, EntityLog.Type.Cloud, "Cloud getting identities");
                jrequest.put("items", jdownload);
                jresponse = call(context, user, password, "sync", jrequest);

                // Process identities
                EntityLog.log(context, EntityLog.Type.Cloud, "Cloud processing identities");
                jitems = jresponse.getJSONArray("items");
                for (int i = 0; i < jitems.length(); i++) {
                    JSONObject jidentitykv = jitems.getJSONObject(i);
                    long revision = jidentitykv.getLong("rev");
                    String value = jidentitykv.getString("val");
                    JSONObject jidentity = new JSONObject(value);
                    EntityIdentity ridentity = EntityIdentity.fromJSON(jidentity);
                    EntityIdentity lidentity = db.identity().getIdentityByUUID(ridentity.uuid);

                    EntityLog.log(context, EntityLog.Type.Cloud,
                            "Cloud identity " + ridentity.uuid + "=" +
                                    (lidentity == null ? "insert" :
                                            (EntityIdentity.areEqual(ridentity, lidentity,
                                                    lidentity.auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD, true)
                                                    ? "equal" : "update")) +
                                    " rev=" + revision +
                                    " size=" + value.length());

                    ridentity.id = null;
                    ridentity.primary = false;
                    ridentity.last_modified = rrevision;

                    try {
                        db.beginTransaction();

                        if (lidentity == null) {
                            EntityAccount account = db.account().getAccountByUUID(jidentity.getString("account_uuid"));
                            if (account != null) {
                                ridentity.account = account.id;
                                ridentity.id = db.identity().insertIdentity(ridentity);
                            }
                        } else {
                            ridentity.id = lidentity.id;
                            db.identity().updateIdentity(ridentity);
                        }

                        if (ridentity.id != null) {
                            if (ridentity.primary) {
                                db.identity().resetPrimary(ridentity.account);
                                db.identity().setIdentityPrimary(ridentity.id, true);
                            }
                            db.identity().setIdentityLastModified(ridentity.id, rrevision);
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                }
            }
        }

        EntityLog.log(context, EntityLog.Type.Cloud, "Cloud set lrevision=" + rrevision);
        prefs.edit().putLong("cloud_lrevision", rrevision).apply();

        if (updates)
            ServiceSynchronize.reload(context, null, true, "sync");
    }

    // Lower level

    public static JSONObject call(Context context, String user, String password, String command, JSONObject jrequest)
            throws GeneralSecurityException, JSONException, IOException {
        EntityLog.log(context, EntityLog.Type.Cloud, "Cloud command=" + command);

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
            EntityLog.log(context, EntityLog.Type.Cloud, "Cloud generating key");
            key = getKeyPair(salt, password);
            synchronized (keyCache) {
                keyCache.put(lookup, key);
            }
        } else {
            EntityLog.log(context, EntityLog.Type.Cloud, "Cloud using cached key");
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
        EntityLog.log(context, EntityLog.Type.Cloud,
                "Cloud request length=" + request.length());

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
                String msg = "Cloud error=" + error + " detail=" + detail;
                Log.e(msg);
                EntityLog.log(context, EntityLog.Type.Cloud, msg);
                JSONObject jerror = new JSONObject(detail);
                if (status == HttpsURLConnection.HTTP_FORBIDDEN)
                    throw new SecurityException(jerror.optString("error"));
                else
                    throw new IOException(error + " " + jerror);
            }

            String response = Helper.readStream(connection.getInputStream());
            EntityLog.log(context, EntityLog.Type.Cloud,
                    "Cloud response length=" + response.length());
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
