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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Check {
    static final String URI_VT_PRIVACY = "https://support.virustotal.com/hc/en-us/articles/115002168385-Privacy-Policy";
    private static final int VT_TIMEOUT = 20; // seconds
    private static final String URI_VT_ENDPOINT = "https://www.virustotal.com/";

    static void virus(Context context, LifecycleOwner owner, FragmentManager fm, long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<String>() {
            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityAttachment attachment = db.attachment().getAttachment(id);
                if (attachment == null)
                    return null;

                String hash;
                try (InputStream is = new FileInputStream(attachment.getFile(context))) {
                    hash = Helper.getHash(is, "SHA-256");
                }

                if (!TextUtils.isEmpty(BuildConfig.VT_APIKEY)) {
                    //hash = "51e31f76c8d70eaeda1aba0e21fc50f44d261b81416c4338ac3f71694a6648b3";
                    URL url = new URL(URI_VT_ENDPOINT + "api/v3/files/" + hash);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(VT_TIMEOUT * 1000);
                    connection.setConnectTimeout(VT_TIMEOUT * 1000);
                    ConnectionHelper.setUserAgent(context, connection);
                    connection.setRequestProperty("x-apikey", BuildConfig.VT_APIKEY);
                    connection.connect();

                    try {
                        int status = connection.getResponseCode();
                        if (status != HttpsURLConnection.HTTP_OK && status != HttpsURLConnection.HTTP_NOT_FOUND) {
                            String error = "Error " + status + ": " + connection.getResponseMessage();
                            try {
                                InputStream is = connection.getErrorStream();
                                if (is != null)
                                    error += "\n" + Helper.readStream(is);
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                            throw new FileNotFoundException(error);
                        }

                        if (status == HttpsURLConnection.HTTP_OK) {
                            String response = Helper.readStream(connection.getInputStream());
                            Log.i("VT response=" + response);

                            JSONObject jroot = new JSONObject(response);
                            JSONObject jdata = jroot.getJSONObject("data");
                            JSONObject jattributes = jdata.getJSONObject("attributes");
                            JSONObject jclassification = jattributes.getJSONObject("popular_threat_classification");
                            String label = jclassification.getString("suggested_threat_label");

                            args.putString("label", label);
                        }
                    } finally {
                        connection.disconnect();
                    }
                }

                return hash;
            }

            @Override
            protected void onExecuted(Bundle args, String hash) {
                if (hash == null)
                    return;

                Uri uri = Uri.parse(URI_VT_ENDPOINT + "gui/file/" + hash);
                Helper.view(context, uri, false);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(fm, ex);
            }
        }.execute(context, owner, args, "attachment:scan");
    }
}
