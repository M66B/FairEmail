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

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FairEmailBackupAgent extends BackupAgent {
    // https://developer.android.com/identity/data/keyvaluebackup#BackupAgent
    // https://developer.android.com/identity/data/testingbackup#Preparing

    // bmgr backupnow "eu.faircode.email.debug"

    private static final String KEY_JSON = "eu.faircode.email.json";

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        try {
            DB db = DB.getInstance(this);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean enabled = prefs.getBoolean("google_backup", BuildConfig.PLAY_STORE_RELEASE);

            EntityLog.log(this, "Backup start enabled=" + enabled);

            JSONObject jroot = new JSONObject();
            jroot.put("version", 1);

            if (enabled) {
                JSONObject jsettings = new JSONObject();
                jsettings.put("enabled", prefs.getBoolean("enabled", true));
                jsettings.put("poll_interval", prefs.getInt("poll_interval", 0));
                jsettings.put("startup", prefs.getString("startup", "unified"));
                String theme = prefs.getString("theme", null);
                if (!TextUtils.isEmpty(theme))
                    jsettings.put("theme", theme);
                jsettings.put("beige", prefs.getBoolean("beige", true));
                jsettings.put("cards", prefs.getBoolean("cards", true));
                jsettings.put("threading", prefs.getBoolean("threading", true));
                jroot.put("settings", jsettings);

                JSONArray jaccounts = new JSONArray();
                List<EntityAccount> accounts = db.account().getAccounts();
                EntityLog.log(this, "Backup accounts=" + accounts.size());
                for (EntityAccount account : accounts)
                    try {
                        JSONObject jaccount = account.toJSON();

                        JSONArray jfolders = new JSONArray();
                        List<EntityFolder> folders = db.folder().getFolders(account.id, false, true);
                        for (EntityFolder folder : folders)
                            if (!EntityFolder.USER.equals(folder.type))
                                jfolders.put(folder.toJSON());
                        jaccount.put("folders", jfolders);

                        JSONArray jidentities = new JSONArray();
                        List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                        for (EntityIdentity identity : identities)
                            jidentities.put(identity.toJSON());
                        jaccount.put("identities", jidentities);

                        jaccounts.put(jaccount);
                    } catch (JSONException ex) {
                        Log.e(ex);
                    }
                jroot.put("accounts", jaccounts);
            }

            byte[] dataBuf = jroot.toString().getBytes(StandardCharsets.UTF_8);
            String dataHash = Helper.sha256(dataBuf);

            String lastHash = null;
            try {
                FileInputStream instream = new FileInputStream(oldState.getFileDescriptor());
                DataInputStream in = new DataInputStream(instream);
                lastHash = in.readUTF();
            } catch (IOException ex) {
                Log.i(ex);
            }

            boolean write = !Objects.equals(dataHash, lastHash);
            EntityLog.log(this, "Backup write=" + write + " size=" + dataBuf.length);
            if (write) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (GZIPOutputStream gos = new GZIPOutputStream(bos)) {
                    gos.write(dataBuf, 0, dataBuf.length);
                }
                dataBuf = bos.toByteArray();
                EntityLog.log(this, "Backup compressed=" + dataBuf.length);

                data.writeEntityHeader(KEY_JSON, dataBuf.length);
                data.writeEntityData(dataBuf, dataBuf.length);
            }

            FileOutputStream outstream = new FileOutputStream(newState.getFileDescriptor());
            DataOutputStream out = new DataOutputStream(outstream);
            out.writeUTF(dataHash);
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            EntityLog.log(this, "Backup end");
        }
    }

    @Override
    public void onQuotaExceeded(long backupDataBytes, long quotaBytes) {
        Log.e("Backup quota exceeded " + backupDataBytes + "/" + quotaBytes);
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) {
        try {
            EntityLog.log(this, "Restore start version=" + appVersionCode);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean enabled = prefs.getBoolean("google_backup", BuildConfig.PLAY_STORE_RELEASE);

            while (data.readNextHeader()) {
                String dataKey = data.getKey();
                int dataSize = data.getDataSize();
                EntityLog.log(this, "Restore key=" + dataKey + " size=" + dataSize);

                if (KEY_JSON.equals(dataKey))
                    try {
                        byte[] dataBuf = new byte[dataSize];
                        data.readEntityData(dataBuf, 0, dataSize);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(dataBuf))) {
                            Helper.copy(gis, bos);
                        }
                        dataBuf = bos.toByteArray();
                        EntityLog.log(this, "Restore decompressed=" + dataBuf.length);

                        JSONObject jroot = new JSONObject(new String(dataBuf, StandardCharsets.UTF_8));
                        EntityLog.log(this, "Restore version=" + jroot.optInt("version", 0));

                        if (!enabled || !jroot.has("accounts")) {
                            EntityLog.log(this, "Restore empty or disabled");
                            continue;
                        }

                        SharedPreferences.Editor editor = prefs.edit();
                        JSONObject jsettings = jroot.getJSONObject("settings");
                        editor.putBoolean("enabled", jsettings.optBoolean("enabled"));
                        editor.putInt("poll_interval", jsettings.optInt("poll_interval", 0));
                        editor.putString("startup", jsettings.optString("startup", "unified"));
                        String theme = jsettings.optString("theme", null);
                        if (!TextUtils.isEmpty(theme))
                            editor.putString("theme", theme);
                        editor.putBoolean("beige", jsettings.optBoolean("beige", true));
                        editor.putBoolean("cards", jsettings.optBoolean("cards", true));
                        editor.putBoolean("threading", jsettings.optBoolean("threading", true));
                        editor.putBoolean("google_backup", true);
                        editor.apply();

                        JSONArray jaccounts = jroot.getJSONArray("accounts");
                        EntityLog.log(this, "Restore accounts=" + jaccounts.length());

                        DB db = DB.getInstance(this);
                        for (int i = 0; i < jaccounts.length(); i++)
                            try {
                                db.beginTransaction();

                                EntityLog.log(this, "Restoring account=" + i);

                                JSONObject jaccount = jaccounts.getJSONObject(i);
                                JSONArray jfolders = jaccount.getJSONArray("folders");
                                JSONArray jidentities = jaccount.getJSONArray("identities");
                                EntityAccount account = EntityAccount.fromJSON(jaccount);
                                if (TextUtils.isEmpty(account.uuid) ||
                                        db.account().getAccountByUUID(account.uuid) != null)
                                    continue;

                                if (jaccounts.length() == 1)
                                    account.primary = true;

                                EntityAccount primary = db.account().getPrimaryAccount();
                                if (primary != null)
                                    account.primary = false;

                                if (account.auth_type == ServiceAuthenticator.AUTH_TYPE_GMAIL)
                                    account.synchronize = false;

                                account.id = db.account().insertAccount(account);

                                for (int j = 0; j < jfolders.length(); j++) {
                                    EntityFolder folder = EntityFolder.fromJSON(jfolders.getJSONObject(j));
                                    folder.account = account.id;
                                    db.folder().insertFolder(folder);
                                }

                                for (int j = 0; j < jidentities.length(); j++) {
                                    EntityIdentity identity = EntityIdentity.fromJSON(jidentities.getJSONObject(j));
                                    identity.account = account.id;
                                    db.identity().insertIdentity(identity);
                                }

                                EntityLog.log(this, "Restored account=" + account.name);

                                db.setTransactionSuccessful();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            } finally {
                                db.endTransaction();
                            }
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                else {
                    data.skipEntityData();
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            EntityLog.log(this, "Restore end");
        }
    }

    @Override
    public void onRestoreFinished() {
        EntityLog.log(this, "Restore finished");
    }

    static void dataChanged(Context context) {
        try {
            new BackupManager(context).dataChanged();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
