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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
/*
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
*/
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

public class ActivityBilling extends ActivityBase implements /*PurchasesUpdatedListener,*/ FragmentManager.OnBackStackChangedListener {
    //private BillingClient billingClient = null;
    //private Map<String, SkuDetails> skuDetails = new HashMap<>();
    private List<IBillingListener> listeners = new ArrayList<>();

    static final String ACTION_PURCHASE = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE";
    static final String ACTION_PURCHASE_CHECK = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE_CHECK";
    static final String ACTION_PURCHASE_ERROR = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE_ERROR";

    private final static long MAX_SKU_CACHE_DURATION = 24 * 3600 * 1000L; // milliseconds
    private final static long MAX_SKU_NOACK_DURATION = 24 * 3600 * 1000L; // milliseconds

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, true);
    }

    protected void onCreate(Bundle savedInstanceState, boolean standalone) {
        super.onCreate(savedInstanceState);

        if (standalone) {
            setContentView(R.layout.activity_billing);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
            fragmentTransaction.commit();

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportFragmentManager().addOnBackStackChangedListener(this);
        }

        if (Helper.isPlayStoreInstall()) {
            Log.i("IAB start");
            //billingClient = BillingClient.newBuilder(this)
            //        .enablePendingPurchases()
            //        .setListener(this)
            //        .build();
            //billingClient.startConnection(billingClientStateListener);
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_PURCHASE);
        iff.addAction(ACTION_PURCHASE_CHECK);
        iff.addAction(ACTION_PURCHASE_ERROR);
        lbm.registerReceiver(receiver, iff);

        //if (billingClient != null && billingClient.isReady())
        //    queryPurchases();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        //if (billingClient != null)
        //    billingClient.endConnection();

        super.onDestroy();
    }

    @NonNull
    static String getSkuPro() {
        if (BuildConfig.DEBUG)
            return "android.test.purchased";
        else
            return BuildConfig.APPLICATION_ID + ".pro";
    }

    private static String getChallenge(Context context) throws NoSuchAlgorithmException {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return Helper.sha256(android_id);
    }

    private static String getResponse(Context context) throws NoSuchAlgorithmException {
        return Helper.sha256(BuildConfig.APPLICATION_ID + getChallenge(context));
    }

    static boolean activatePro(Context context, Uri data) throws NoSuchAlgorithmException {
        String response = data.getQueryParameter("response");
        return activatePro(context, response);
    }

    static boolean activatePro(Context context, String response) throws NoSuchAlgorithmException {
        String challenge = getChallenge(context);
        Log.i("IAB challenge=" + challenge);
        Log.i("IAB response=" + response);
        String expected = getResponse(context);
        if (expected.equals(response)) {
            Log.i("IAB response valid");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit()
                    .putBoolean("pro", true)
                    .putBoolean("play_store", false)
                    .apply();

            WidgetUnified.updateData(context);
            return true;
        } else {
            Log.i("IAB response invalid");
            return false;
        }
    }

    static boolean isPro(Context context) {
        if (BuildConfig.DEBUG && false)
            return true;
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("pro", false);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                if (ACTION_PURCHASE.equals(intent.getAction()))
                    onPurchase(intent);
                else if (ACTION_PURCHASE_CHECK.equals(intent.getAction()))
                    ;//onPurchaseCheck(intent);
                else if (ACTION_PURCHASE_ERROR.equals(intent.getAction()))
                    ;//onPurchaseError(intent);
            }
        }
    };

    private void onPurchase(Intent intent) {
        if (Helper.isPlayStoreInstall()) {
            //BillingFlowParams.Builder flowParams = BillingFlowParams.newBuilder();
            //if (skuDetails.containsKey(getSkuPro())) {
            //    Log.i("IAB purchase SKU=" + skuDetails.get(getSkuPro()));
            //    flowParams.setSkuDetails(skuDetails.get(getSkuPro()));
            //}

            //BillingResult result = billingClient.launchBillingFlow(this, flowParams.build());
            //if (result.getResponseCode() != BillingClient.BillingResponseCode.OK)
            //    reportError(result, "IAB launch billing flow");
        } else
            try {
                Uri uri = Uri.parse(BuildConfig.PRO_FEATURES_URI + "?challenge=" + getChallenge(this));
                Helper.view(this, uri, true);
            } catch (NoSuchAlgorithmException ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
    }
/*
    private void onPurchaseCheck(Intent intent) {
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, new PurchaseHistoryResponseListener() {
            @Override
            public void onPurchaseHistoryResponse(BillingResult result, List<PurchaseHistoryRecord> records) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (PurchaseHistoryRecord record : records)
                        Log.i("IAB history=" + record.toString());

                    queryPurchases();

                    ToastEx.makeText(ActivityBilling.this, R.string.title_setup_done, Toast.LENGTH_LONG).show();
                } else
                    reportError(result, "IAB history");
            }
        });
    }

    private void onPurchaseError(Intent intent) {
        String message = intent.getStringExtra("message");
        Uri uri = Uri.parse(Helper.SUPPORT_URI);
        if (!TextUtils.isEmpty(message))
            uri = uri.buildUpon().appendQueryParameter("message", "IAB: " + message).build();
        Helper.view(this, uri, true);
    }

    private BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
        private int backoff = 4; // seconds

        @Override
        public void onBillingSetupFinished(BillingResult result) {
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (IBillingListener listener : listeners)
                    listener.onConnected();

                backoff = 4;
                queryPurchases();
            } else
                reportError(result, "IAB connected");
        }

        @Override
        public void onBillingServiceDisconnected() {
            for (IBillingListener listener : listeners)
                listener.onDisconnected();

            backoff *= 2;
            retry(backoff);
        }
    };

    private void retry(int backoff) {
        Log.i("IAB connect retry in " + backoff + " s");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!billingClient.isReady())
                    billingClient.startConnection(billingClientStateListener);
            }
        }, backoff * 1000L);
    }

    @Override
    public void onPurchasesUpdated(BillingResult result, @Nullable List<Purchase> purchases) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK)
            checkPurchases(purchases);
        else
            reportError(result, "IAB purchases updated");
    }

    private void queryPurchases() {
        Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK)
            checkPurchases(result.getPurchasesList());
        else
            reportError(result.getBillingResult(), "IAB query purchases");
    }
*/
    interface IBillingListener {
        void onConnected();

        void onDisconnected();

        void onSkuDetails(String sku, String price);

        void onPurchasePending(String sku);

        void onPurchased(String sku);

        void onError(String message);
    }

    void addBillingListener(final IBillingListener listener, LifecycleOwner owner) {
        Log.i("IAB adding billing listener=" + listener);
        listeners.add(listener);

        //if (billingClient != null)
        //    if (billingClient.isReady()) {
        //        listener.onConnected();
        //        queryPurchases();
        //    } else
        //        listener.onDisconnected();

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.i("IAB removing billing listener=" + listener);
                listeners.remove(listener);
            }
        });
    }
/*
    private void checkPurchases(List<Purchase> purchases) {
        Log.i("IAB purchases=" + (purchases == null ? null : purchases.size()));

        List<String> query = new ArrayList<>();
        query.add(getSkuPro());

        if (purchases != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            if (prefs.getBoolean("play_store", true)) {
                long cached = prefs.getLong(getSkuPro() + ".cached", 0);
                if (cached + MAX_SKU_CACHE_DURATION < new Date().getTime()) {
                    Log.i("IAB cache expired=" + new Date(cached));
                    editor.remove("pro");
                } else
                    Log.i("IAB caching until=" + new Date(cached + MAX_SKU_CACHE_DURATION));
            }

            for (Purchase purchase : purchases)
                try {
                    query.remove(purchase.getSku());

                    long time = purchase.getPurchaseTime();
                    Log.i("IAB SKU=" + purchase.getSku() +
                            " purchased=" + isPurchased(purchase) +
                            " valid=" + isPurchaseValid(purchase) +
                            " time=" + new Date(time));

                    //if (new Date().getTime() - purchase.getPurchaseTime() > 3 * 60 * 1000L) {
                    //    consumePurchase(purchase);
                    //    continue;
                    //}

                    for (IBillingListener listener : listeners)
                        if (isPurchaseValid(purchase))
                            listener.onPurchased(purchase.getSku());
                        else
                            listener.onPurchasePending(purchase.getSku());

                    if (isPurchased(purchase)) {
                        byte[] decodedKey = Base64.decode(getString(R.string.public_key), Base64.DEFAULT);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
                        Signature sig = Signature.getInstance("SHA1withRSA");
                        sig.initVerify(publicKey);
                        sig.update(purchase.getOriginalJson().getBytes());
                        if (sig.verify(Base64.decode(purchase.getSignature(), Base64.DEFAULT))) {
                            Log.i("IAB valid signature");
                            if (getSkuPro().equals(purchase.getSku())) {
                                if (isPurchaseValid(purchase)) {
                                    editor.putBoolean("pro", true);
                                    editor.putLong(purchase.getSku() + ".cached", new Date().getTime());
                                }

                                if (!purchase.isAcknowledged())
                                    acknowledgePurchase(purchase, 0);
                            }
                        } else {
                            Log.w("IAB invalid signature");
                            editor.putBoolean("pro", false);
                            reportError(null, "Invalid purchase");
                        }
                    }
                } catch (Throwable ex) {
                    reportError(null, Log.formatThrowable(ex, false));
                }

            editor.apply();

            WidgetUnified.updateData(this);
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
                        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (SkuDetails skuDetail : skuDetailsList) {
                                Log.i("IAB SKU detail=" + skuDetail);
                                skuDetails.put(skuDetail.getSku(), skuDetail);
                                for (IBillingListener listener : listeners)
                                    listener.onSkuDetails(skuDetail.getSku(), skuDetail.getPrice());
                            }
                        } else
                            reportError(result, "IAB query SKUs");
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
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK)
                    reportError(result, "IAB consumed SKU=" + purchase.getSku());
            }
        });
    }

    private void acknowledgePurchase(final Purchase purchase, int retry) {
        Log.i("IAB acknowledging purchase SKU=" + purchase.getSku());
        AcknowledgePurchaseParams params =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
        billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityBilling.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("pro", true);
                    editor.putLong(purchase.getSku() + ".cached", new Date().getTime());
                    editor.apply();

                    for (IBillingListener listener : listeners)
                        listener.onPurchased(purchase.getSku());

                    WidgetUnified.updateData(ActivityBilling.this);
                } else {
                    if (retry < 3) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                acknowledgePurchase(purchase, retry + 1);
                            }
                        }, (retry + 1) * 10 * 1000L);
                    } else
                        reportError(result, "IAB acknowledged SKU=" + purchase.getSku());
                }
            }
        });
    }

    private boolean isPurchased(Purchase purchase) {
        return (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED);
    }

    private boolean isPurchaseValid(Purchase purchase) {
        return (isPurchased(purchase) &&
                (purchase.isAcknowledged() ||
                        purchase.getPurchaseTime() + MAX_SKU_NOACK_DURATION > new Date().getTime()));
    }

    private void reportError(BillingResult result, String stage) {
        String message;
        if (result == null)
            message = stage;
        else {
            String debug = result.getDebugMessage();
            message = getBillingResponseText(result) + (debug == null ? "" : " " + debug) + " " + stage;

            // https://developer.android.com/reference/com/android/billingclient/api/BillingClient.BillingResponse#service_disconnected
            if (result.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                retry(60);
        }

        EntityLog.log(this, message);

        if (result.getResponseCode() != BillingClient.BillingResponseCode.USER_CANCELED)
            for (IBillingListener listener : listeners)
                listener.onError(message);
    }

    private static String getBillingResponseText(BillingResult result) {
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

            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                // The request has reached the maximum timeout before Google Play responds.
                return "SERVICE_TIMEOUT";

            case BillingClient.BillingResponseCode.USER_CANCELED:
                // User pressed back or canceled a dialog
                return "USER_CANCELED";

            default:
                return Integer.toString(result.getResponseCode());
        }
    }
 */
}
