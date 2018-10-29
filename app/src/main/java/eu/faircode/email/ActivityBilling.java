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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.android.material.snackbar.Snackbar;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

abstract class ActivityBilling extends ActivityBase implements PurchasesUpdatedListener {
    private BillingClient billingClient = null;

    static final String ACTION_PURCHASE = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE";
    static final String ACTION_ACTIVATE_PRO = BuildConfig.APPLICATION_ID + ".ACTIVATE_PRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Helper.isPlayStoreInstall(this)) {
            billingClient = BillingClient.newBuilder(this).setListener(this).build();
            billingClient.startConnection(billingClientStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_PURCHASE);
        iff.addAction(ACTION_ACTIVATE_PRO);
        lbm.registerReceiver(receiver, iff);

        if (billingClient != null && billingClient.isReady())
            queryPurchases();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        if (billingClient != null)
            billingClient.endConnection();
        super.onDestroy();
    }

    protected Intent getIntentPro() {
        if (Helper.isPlayStoreInstall(this))
            return null;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://email.faircode.eu/pro/?challenge=" + getChallenge()));
            return intent;
        } catch (NoSuchAlgorithmException ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            return null;
        }
    }

    private String getChallenge() throws NoSuchAlgorithmException {
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return Helper.sha256(android_id);
    }

    private String getResponse() throws NoSuchAlgorithmException {
        return Helper.sha256(BuildConfig.APPLICATION_ID + getChallenge());
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_PURCHASE.equals(intent.getAction()))
                onPurchase(intent);
            else if (ACTION_ACTIVATE_PRO.equals(intent.getAction()))
                onActivatePro(intent);
        }
    };

    private View getView() {
        return findViewById(android.R.id.content);
    }

    private void onPurchase(Intent intent) {
        if (Helper.isPlayStoreInstall(this)) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSku(BuildConfig.APPLICATION_ID + ".pro")
                    .setType(BillingClient.SkuType.INAPP)
                    .build();
            int responseCode = billingClient.launchBillingFlow(this, flowParams);
            String text = Helper.getBillingResponseText(responseCode);
            Log.i(Helper.TAG, "IAB launch billing flow response=" + text);
            if (responseCode != BillingClient.BillingResponse.OK)
                Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
        } else
            Helper.view(this, getIntentPro());
    }

    private void onActivatePro(Intent intent) {
        try {
            Uri data = intent.getParcelableExtra("uri");
            String challenge = getChallenge();
            String response = data.getQueryParameter("response");
            Log.i(Helper.TAG, "Challenge=" + challenge);
            Log.i(Helper.TAG, "Response=" + response);
            String expected = getResponse();
            if (expected.equals(response)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putBoolean("pro", true).apply();
                Log.i(Helper.TAG, "Response valid");
                Snackbar.make(getView(), R.string.title_pro_valid, Snackbar.LENGTH_LONG).show();
            } else {
                Log.i(Helper.TAG, "Response invalid");
                Snackbar.make(getView(), R.string.title_pro_invalid, Snackbar.LENGTH_LONG).show();
            }
        } catch (NoSuchAlgorithmException ex) {
            Log.e(Helper.TAG, Log.getStackTraceString(ex));
            Helper.unexpectedError(this, ex);
        }
    }

    private BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
        private int backoff = 4; // seconds

        @Override
        public void onBillingSetupFinished(@BillingClient.BillingResponse int responseCode) {
            String text = Helper.getBillingResponseText(responseCode);
            Log.i(Helper.TAG, "IAB connected response=" + text);
            if (responseCode == BillingClient.BillingResponse.OK) {
                backoff = 4;
                queryPurchases();
            } else
                Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onBillingServiceDisconnected() {
            backoff *= 2;
            Log.i(Helper.TAG, "IAB disconnected retry in " + backoff + " s");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!billingClient.isReady())
                        billingClient.startConnection(billingClientStateListener);
                }
            }, backoff * 1000L);
        }
    };

    @Override
    public void onPurchasesUpdated(int responseCode, @android.support.annotation.Nullable List<Purchase> purchases) {
        String text = Helper.getBillingResponseText(responseCode);
        Log.i(Helper.TAG, "IAB purchases updated response=" + text);
        if (responseCode == BillingClient.BillingResponse.OK)
            checkPurchases(purchases);
        else
            Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
    }

    private void queryPurchases() {
        Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        String text = Helper.getBillingResponseText(result.getResponseCode());
        Log.i(Helper.TAG, "IAB query purchases response=" + text);
        if (result.getResponseCode() == BillingClient.BillingResponse.OK)
            checkPurchases(result.getPurchasesList());
        else
            Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
    }

    private void checkPurchases(List<Purchase> purchases) {
        if (purchases != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            if (prefs.getBoolean("play_store", true))
                editor.remove("pro");
            for (Purchase purchase : purchases) {
                Log.i(Helper.TAG, "IAB SKU=" + purchase.getSku());
                if ((BuildConfig.APPLICATION_ID + ".pro").equals(purchase.getSku())) {
                    editor.putBoolean("pro", true);
                    Log.i(Helper.TAG, "IAB pro features activated");
                }
            }
            editor.apply();
        }
    }
}
