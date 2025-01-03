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

import static org.openintents.openpgp.util.OpenPgpApi.RESULT_CODE_ERROR;
import static org.openintents.openpgp.util.OpenPgpApi.RESULT_CODE_SUCCESS;
import static org.openintents.openpgp.util.OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.OperationCanceledException;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class PgpHelper {
    private static final long CONNECT_TIMEOUT = 5000L;
    private static final long KEY_TIMEOUT = 250L;

    static Intent execute(Context context, Intent data, InputStream is, OutputStream os) {
        return execute(context, data, is, os, CONNECT_TIMEOUT);
    }

    static Intent execute(Context context, Intent data, InputStream is, OutputStream os, long timeout) {
        OpenPgpServiceConnection pgpService = null;
        try {
            pgpService = getConnection(context, timeout);

            Log.i("Executing " + data.getAction() +
                    " " + TextUtils.join(", ", Log.getExtras(data.getExtras())));
            OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
            Intent result = api.executeApi(data, is, os);
            int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, RESULT_CODE_ERROR);
            Log.i("Result action=" + data.getAction() + " code=" + getResultName(resultCode) +
                    " " + TextUtils.join(", ", Log.getExtras(result.getExtras())));
            return result;
        } finally {
            if (pgpService != null && pgpService.isBound())
                pgpService.unbindFromService();
        }
    }

    static boolean hasPgpKey(Context context, List<Address> recipients, boolean all) {
        return hasPgpKey(context, recipients, all, KEY_TIMEOUT);  // milliseconds
    }

    private static boolean hasPgpKey(Context context, List<Address> recipients, boolean all, long timeout) {
        if (recipients == null || recipients.size() == 0)
            return false;

        String[] userIds = new String[recipients.size()];
        for (int i = 0; i < recipients.size(); i++) {
            InternetAddress recipient = (InternetAddress) recipients.get(i);
            userIds[i] = recipient.getAddress();
        }

        Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY_IDS);
        intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, userIds);

        try {
            Intent result = execute(context, intent, null, null, timeout);
            int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
            if (resultCode == OpenPgpApi.RESULT_CODE_SUCCESS) {
                long[] keyIds = result.getLongArrayExtra(OpenPgpApi.EXTRA_KEY_IDS);
                return (all ? keyIds.length == recipients.size() : keyIds.length > 0);
            }
        } catch (OperationCanceledException ignored) {
            // Do nothing
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return false;
    }

    private static String getResultName(int code) {
        switch (code) {
            case RESULT_CODE_ERROR:
                return "error";
            case RESULT_CODE_SUCCESS:
                return "success";
            case RESULT_CODE_USER_INTERACTION_REQUIRED:
                return "interaction";
            default:
                return Integer.toString(code);
        }
    }

    private static OpenPgpServiceConnection getConnection(Context context, long timeout) {
        final String pkg = PgpHelper.getPackageName(context);
        Log.i("PGP binding to " + pkg + " timeout=" + timeout);

        CountDownLatch latch = new CountDownLatch(1);
        OpenPgpServiceConnection pgpService = new OpenPgpServiceConnection(context, pkg,
                new OpenPgpServiceConnection.OnBound() {
                    @Override
                    public void onBound(IOpenPgpService2 service) {
                        Log.i("Bound to " + pkg);
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception ex) {
                        Log.i(ex.getMessage());
                        latch.countDown();
                    }
                });
        pgpService.bindToService();

        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Log.w(ex);
        }

        Log.i("PGP bound=" + pgpService.isBound());
        if (!pgpService.isBound()) {
            try {
                pgpService.unbindFromService();
            } catch (Throwable ex) {
                Log.e(ex);
            }
            throw new OperationCanceledException();
        }

        return pgpService;
    }

    static String getPackageName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("openpgp_provider", Helper.PGP_OPENKEYCHAIN_PACKAGE);
    }

    static boolean isOpenKeychainInstalled(Context context) {
        String provider = getPackageName(context);

        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
            intent.setPackage(provider);
            List<ResolveInfo> ris = pm.queryIntentServices(intent, 0);

            return (ris != null && ris.size() > 0);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }
}
