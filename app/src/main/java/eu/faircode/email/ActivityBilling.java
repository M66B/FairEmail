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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.snackbar.Snackbar;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ActivityBilling extends ActivityBase implements PurchasesUpdatedListener {
    private BillingClient billingClient = null;
    private Map<String, SkuDetails> skuDetails = new HashMap<>();
    private List<IBillingListener> listeners = new ArrayList<>();

    static final String ACTION_PURCHASE = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE";
    static final String ACTION_ACTIVATE_PRO = BuildConfig.APPLICATION_ID + ".ACTIVATE_PRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Helper.isPlayStoreInstall(this)) {
            Log.i("IAB start");
            billingClient = BillingClient.newBuilder(this)
                    .enablePendingPurchases()
                    .setListener(this)
                    .build();
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

    static String getSkuPro() {
        if (BuildConfig.DEBUG)
            return "android.test.purchased";
        else
            return BuildConfig.APPLICATION_ID + ".pro";
    }

    protected Intent getIntentPro() {
        if (Helper.isPlayStoreInstall(this))
            return null;

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(BuildConfig.PRO_FEATURES_URI + "?challenge=" + getChallenge()));
            return intent;
        } catch (NoSuchAlgorithmException ex) {
            Log.e(ex);
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_PURCHASE.equals(intent.getAction()))
                onPurchase(intent);
            else if (ACTION_ACTIVATE_PRO.equals(intent.getAction()))
                onActivatePro(intent);
        }
    };

    private void onPurchase(Intent intent) {
        if (Helper.isPlayStoreInstall(this)) {
            BillingFlowParams.Builder flowParams = BillingFlowParams.newBuilder();
            if (skuDetails.containsKey(getSkuPro())) {
                Log.i("IAB purchase SKU=" + skuDetails.get(getSkuPro()));
                flowParams.setSkuDetails(skuDetails.get(getSkuPro()));
            }

            BillingResult result = billingClient.launchBillingFlow(this, flowParams.build());
            String text = getBillingResponseText(result);
            Log.i("IAB launch billing flow response=" + text);
            if (result.getResponseCode() != BillingClient.BillingResponseCode.OK)
                Snackbar.make(getVisibleView(), text, Snackbar.LENGTH_LONG).show();
        } else
            Helper.view(this, this, getIntentPro());
    }

    private void onActivatePro(Intent intent) {
        try {
            Uri data = intent.getParcelableExtra("uri");
            String challenge = getChallenge();
            String response = data.getQueryParameter("response");
            Log.i("Challenge=" + challenge);
            Log.i("Response=" + response);
            String expected = getResponse();
            if (expected.equals(response)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit()
                        .putBoolean("pro", true)
                        .putBoolean("play_store", false)
                        .apply();
                Log.i("Response valid");
                Snackbar snackbar = Snackbar.make(getVisibleView(), R.string.title_pro_valid, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.title_check, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                        fragmentTransaction.commit();
                    }
                });
                snackbar.show();
            } else {
                Log.i("Response invalid");
                Snackbar.make(getVisibleView(), R.string.title_pro_invalid, Snackbar.LENGTH_LONG).show();
            }
        } catch (NoSuchAlgorithmException ex) {
            Log.e(ex);
            Helper.unexpectedError(this, this, ex);
        }
    }

    private BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
        private int backoff = 4; // seconds

        @Override
        public void onBillingSetupFinished(BillingResult result) {
            String text = getBillingResponseText(result);
            Log.i("IAB connected response=" + text);

            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                return;

            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                backoff = 4;
                queryPurchases();
            } else
                Snackbar.make(getVisibleView(), text, Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onBillingServiceDisconnected() {
            backoff *= 2;
            Log.i("IAB disconnected retry in " + backoff + " s");
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
    public void onPurchasesUpdated(BillingResult result, @Nullable List<Purchase> purchases) {
        String text = getBillingResponseText(result);
        Log.i("IAB purchases updated response=" + text);

        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            return;

        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK)
            checkPurchases(purchases);
        else
            Snackbar.make(getVisibleView(), text, Snackbar.LENGTH_LONG).show();
    }

    private void queryPurchases() {
        Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        String text = getBillingResponseText(result.getBillingResult());
        Log.i("IAB query purchases response=" + text);

        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK)
            checkPurchases(result.getPurchasesList());
        else
            Snackbar.make(getVisibleView(), text, Snackbar.LENGTH_LONG).show();
    }

    interface IBillingListener {
        void onSkuDetails(String sku, String price);

        void onPurchasePending(String sku);

        void onPurchased(String sku);
    }

    void addBillingListener(final IBillingListener listener, LifecycleOwner owner) {
        Log.i("Adding billing listener=" + listener);
        listeners.add(listener);

        if (billingClient != null && billingClient.isReady())
            queryPurchases();

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.i("Removing billing listener=" + listener);
                listeners.remove(listener);
            }
        });
    }

    private void checkPurchases(List<Purchase> purchases) {
        List<String> query = new ArrayList<>();
        query.add(getSkuPro());

        if (purchases != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            if (prefs.getBoolean("play_store", true))
                editor.remove("pro");

            for (Purchase purchase : purchases)
                try {
                    query.remove(purchase.getSku());
                    boolean purchased = (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED);
                    long time = purchase.getPurchaseTime();
                    Log.i("IAB SKU=" + purchase.getSku() + " purchased=" + purchased + " time=" + new Date(time));

                    //if (new Date().getTime() - purchase.getPurchaseTime() > 3 * 60 * 1000L) {
                    //    consumePurchase(purchase);
                    //    continue;
                    //}

                    for (IBillingListener listener : listeners)
                        if (purchased && purchase.isAcknowledged())
                            listener.onPurchased(purchase.getSku());
                        else
                            listener.onPurchasePending(purchase.getSku());

                    if (!purchased)
                        continue;

                    byte[] decodedKey = Base64.decode(getString(R.string.public_key), Base64.DEFAULT);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
                    Signature sig = Signature.getInstance("SHA1withRSA");
                    sig.initVerify(publicKey);
                    sig.update(purchase.getOriginalJson().getBytes());
                    if (sig.verify(Base64.decode(purchase.getSignature(), Base64.DEFAULT))) {
                        if (getSkuPro().equals(purchase.getSku())) {
                            if (purchase.isAcknowledged()) {
                                editor.putBoolean("pro", true);
                                Log.i("IAB pro features activated");
                            } else
                                acknowledgePurchase(purchase);
                        }

                    } else {
                        Log.w("Invalid signature");
                        Snackbar.make(getVisibleView(), R.string.title_pro_invalid, Snackbar.LENGTH_LONG).show();
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                    Snackbar.make(getVisibleView(), ex.getMessage(), Snackbar.LENGTH_LONG).show();
                }

            editor.apply();
        }

        if (query.size() > 0)
            querySkus(query);
    }

    private void querySkus(List<String> query) {
        Log.i("IAB query SKUs");
        SkuDetailsParams.Builder builder = SkuDetailsParams.newBuilder();
        builder.setSkusList(query);
        builder.setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(builder.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult result, List<SkuDetails> skuDetailsList) {
                        String text = getBillingResponseText(result);
                        Log.i("IAB query SKUs response=" + text);
                        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (SkuDetails skuDetail : skuDetailsList) {
                                Log.i("IAB SKU detail=" + skuDetail);
                                skuDetails.put(skuDetail.getSku(), skuDetail);
                                for (IBillingListener listener : listeners)
                                    listener.onSkuDetails(skuDetail.getSku(), skuDetail.getPrice());
                            }
                        }
                    }
                });
    }

    private void consumePurchase(final Purchase purchase) {
        Log.i("IAB SKU=" + purchase.getSku() + " consuming");
        ConsumeParams params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.consumeAsync(params, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult result, String purchaseToken) {
                String text = getBillingResponseText(result);
                Log.i("IAB SKU=" + purchase.getSku() + " consumed response=" + text);
            }
        });
    }

    private void acknowledgePurchase(final Purchase purchase) {
        Log.i("IAB acknowledging purchase SKU=" + purchase.getSku());
        AcknowledgePurchaseParams params =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
        billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult result) {
                String text = getBillingResponseText(result);
                Log.i("IAB acknowledged SKU=" + purchase.getSku() + " response=" + text);
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityBilling.this);
                    prefs.edit().putBoolean("pro", true).apply();

                    for (IBillingListener listener : listeners)
                        listener.onPurchased(purchase.getSku());
                }
            }
        });
    }

    private static String getBillingResponseText(BillingResult result) {
        String debug = result.getDebugMessage();
        return _getBillingResponseText(result) + (debug == null ? "" : " " + debug);
    }

    private static String _getBillingResponseText(BillingResult result) {
        switch (result.getResponseCode()) {
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                // Billing API version is not supported for the type requested
                return "BILLING_UNAVAILABLE";

            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                // Invalid arguments provided to the API.
                return "DEVELOPER_ERROR";

            case BillingClient.BillingResponseCode.ERROR:
                // Fatal error during the API action
                return "ERROR";

            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                // Requested feature is not supported by Play Store on the current device.
                return "FEATURE_NOT_SUPPORTED";

            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                // Failure to purchase since item is already owned
                return "ITEM_ALREADY_OWNED";

            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                // Failure to consume since item is not owned
                return "ITEM_NOT_OWNED";

            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                // Requested product is not available for purchase
                return "ITEM_UNAVAILABLE";

            case BillingClient.BillingResponseCode.OK:
                // Success
                return "OK";

            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                // Play Store service is not connected now - potentially transient state.
                return "SERVICE_DISCONNECTED";

            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                // Network connection is down
                return "SERVICE_UNAVAILABLE";

            case BillingClient.BillingResponseCode.USER_CANCELED:
                // User pressed back or canceled a dialog
                return "USER_CANCELED";

            default:
                return Integer.toString(result.getResponseCode());
        }
    }
}
