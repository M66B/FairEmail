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
import com.android.billingclient.api.PurchasesResponseListener;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ActivityBilling extends ActivityBase implements
        /* BillingClientStateListener, SkuDetailsResponseListener, PurchasesResponseListener, PurchasesUpdatedListener, */
        FragmentManager.OnBackStackChangedListener {
    private boolean standalone = false;
    private int backoff = 4; // seconds
    //private BillingClient billingClient = null;
    private List<IBillingListener> listeners = new ArrayList<>();

    static final String ACTION_PURCHASE = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE";
    static final String ACTION_PURCHASE_CONSUME = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE_CONSUME";
    static final String ACTION_PURCHASE_ERROR = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE_ERROR";

    private static final String SKU_TEST = "android.test.purchased";
    private static final long MAX_SKU_CACHE_DURATION = 24 * 3600 * 1000L; // milliseconds
    private static final long MAX_SKU_NOACK_DURATION = 24 * 3600 * 1000L; // milliseconds

    @Override
    @SuppressLint("MissingSuperCall")
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, true);
    }

    protected void onCreate(Bundle savedInstanceState, boolean standalone) {
        super.onCreate(savedInstanceState);

        this.standalone = standalone;

        if (standalone) {
            setContentView(R.layout.activity_billing);

            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                fragmentTransaction.commit();
            }

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportFragmentManager().addOnBackStackChangedListener(this);
        }

        if (Helper.isPlayStoreInstall() || isTesting(this))
            try {
                Log.i("IAB start");
/*
                billingClient = BillingClient.newBuilder(getApplicationContext())
                        .enablePendingPurchases()
                        .setListener(this)
                        .build();
                billingClient.startConnection(this);
                getLifecycle().addObserver(new LifecycleObserver() {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    public void onDestroyed() {
                        getLifecycle().removeObserver(this);
                        if (billingClient != null)
                            try {
                                Log.i("IAB end");
                                billingClient.endConnection();
                                billingClient = null;
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                    }
                });
*/
            } catch (Throwable ex) {
                Log.e(ex);
                /*
                    Exception java.lang.RuntimeException:
                      at android.app.ActivityThread.performLaunchActivity (ActivityThread.java:4171)
                      at android.app.ActivityThread.handleLaunchActivity (ActivityThread.java:4317)
                      at android.app.servertransaction.LaunchActivityItem.execute (LaunchActivityItem.java:101)
                      at android.app.servertransaction.TransactionExecutor.executeCallbacks (TransactionExecutor.java:135)
                      at android.app.servertransaction.TransactionExecutor.execute (TransactionExecutor.java:95)
                      at android.app.ActivityThread$H.handleMessage (ActivityThread.java:2576)
                      at android.os.Handler.dispatchMessage (Handler.java:106)
                      at android.os.Looper.loopOnce (Looper.java:226)
                      at android.os.Looper.loop (Looper.java:313)
                      at android.app.ActivityThread.main (ActivityThread.java:8772)
                      at java.lang.reflect.Method.invoke (Method.java)
                      at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:571)
                      at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1067)
                    Caused by java.lang.IllegalStateException: Too many bind requests(999+) for service Intent { act=com.android.vending.billing.InAppBillingService.BIND pkg=com.android.vending cmp=com.android.vending/com.google.android.finsky.billing.iab.InAppBillingService (has extras) }
                      at android.app.ContextImpl.bindServiceCommon (ContextImpl.java:2115)
                      at android.app.ContextImpl.bindService (ContextImpl.java:2024)
                      at android.content.ContextWrapper.bindService (ContextWrapper.java:870)
                      at com.android.billingclient.api.BillingClientImpl.startConnection (com.android.billingclient:billing@@4.1.0:52)
                      at eu.faircode.email.ActivityBilling.onCreate (ActivityBilling.java:116)
                      at eu.faircode.email.ActivityView.onCreate (ActivityView.java:192)
                      at android.app.Activity.performCreate (Activity.java:8565)
                      at android.app.Activity.performCreate (Activity.java:8544)
                      at android.app.Instrumentation.callActivityOnCreate (Instrumentation.java:1384)
                      at android.app.ActivityThread.performLaunchActivity (ActivityThread.java:4152)
                 */
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

        if (standalone) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            IntentFilter iff = new IntentFilter();
            iff.addAction(ACTION_PURCHASE);
            iff.addAction(ACTION_PURCHASE_CONSUME);
            iff.addAction(ACTION_PURCHASE_ERROR);
            lbm.registerReceiver(receiver, iff);
        }

        //if (billingClient != null && billingClient.isReady())
        //    queryPurchases();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (standalone) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            lbm.unregisterReceiver(receiver);
        }
    }

    @NonNull
    static String getSkuPro(Context context) {
        if (isTesting(context))
            return SKU_TEST;
        else
            return BuildConfig.APPLICATION_ID + ".pro";
    }

    static boolean isTesting(Context context) {
        if (context == null)
            return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return (BuildConfig.DEBUG && BuildConfig.TEST_RELEASE &&
                prefs.getBoolean("test_iab", false));
    }

    static String getChallenge(Context context) throws NoSuchAlgorithmException {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (android_id == null) {
            Log.e("Android ID empty");
            android_id = Long.toHexString(System.currentTimeMillis() / (24 * 3600 * 1000L));
        }
        return Helper.sha256(android_id);
    }

    private static String getResponse(Context context) throws NoSuchAlgorithmException {
        return Helper.sha256(BuildConfig.APPLICATION_ID.replace(".debug", "") + getChallenge(context));
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
        if (context == null)
            return false;
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("pro", false);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                if (ACTION_PURCHASE.equals(intent.getAction()))
                    onPurchase(intent);
                else if (ACTION_PURCHASE_CONSUME.equals(intent.getAction()))
                    ;//onPurchaseConsume(intent);
                else if (ACTION_PURCHASE_ERROR.equals(intent.getAction()))
                    ;//onPurchaseError(intent);
            }
        }
    };

    private void onPurchase(Intent intent) {
        if (Helper.isPlayStoreInstall() || isTesting(this)) {
            String skuPro = getSkuPro(this);
            Log.i("IAB purchase SKU=" + skuPro);
/*
            SkuDetailsParams.Builder builder = SkuDetailsParams.newBuilder();
            builder.setSkusList(Arrays.asList(skuPro));
            builder.setType(BillingClient.SkuType.INAPP);
            billingClient.querySkuDetailsAsync(builder.build(),
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(@NonNull BillingResult r, List<SkuDetails> skuDetailsList) {
                            if (r.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                if (skuDetailsList.size() == 0)
                                    reportError(null, "Unknown SKU=" + skuPro);
                                else {
                                    SkuDetails skuDetails = skuDetailsList.get(0);
                                    Log.i("IAB purchase details=" + skuDetails);

                                    BillingFlowParams.Builder flowParams = BillingFlowParams.newBuilder();
                                    flowParams.setSkuDetails(skuDetails);

                                    BillingResult result = billingClient.launchBillingFlow(ActivityBilling.this, flowParams.build());
                                    if (result.getResponseCode() != BillingClient.BillingResponseCode.OK)
                                        reportError(result, "IAB launch billing flow");
                                }
                            } else
                                reportError(r, "IAB query SKUs");
                        }
                    });
*/
        } else
            try {
                Uri uri = Uri.parse(BuildConfig.PRO_FEATURES_URI +
                        "?challenge=" + getChallenge(this) +
                        "&version=" + BuildConfig.VERSION_CODE);
                Helper.view(this, uri, true);
            } catch (NoSuchAlgorithmException ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
    }
/*
    private void onPurchaseConsume(Intent intent) {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult result, @NonNull List<Purchase> list) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (Purchase purchase : list)
                        consumePurchase(purchase);
                } else
                    reportError(result, "IAB onPurchaseConsume");
            }
        });
    }

    private void onPurchaseError(Intent intent) {
        String message = intent.getStringExtra("message");
        boolean play = Helper.hasPlayStore(this);
        Uri uri = Helper.getSupportUri(this, "Purchase:error");
        if (!TextUtils.isEmpty(message))
            uri = uri
                    .buildUpon()
                    .appendQueryParameter("message", "IAB: " + message + " Play: " + play)
                    .build();
        Helper.view(this, uri, true);
    }

    @Override
    public void onBillingSetupFinished(BillingResult result) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            EntityLog.log(this, "IAB connected");
            for (IBillingListener listener : listeners)
                listener.onConnected();

            backoff = 4;
            queryPurchases();
        } else
            reportError(result, "IAB connected");
    }

    @Override
    public void onBillingServiceDisconnected() {
        EntityLog.log(this, "IAB disconnected");
        for (IBillingListener listener : listeners)
            listener.onDisconnected();

        backoff *= 2;
        retry(backoff);
    }

    private void retry(int backoff) {
        Log.i("IAB connect retry in " + backoff + " s");

        getMainHandler().postDelayed(new RunnableEx("IAB retry") {
            @Override
            public void delegate() {
                try {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;
                    boolean ready = billingClient.isReady();
                    Log.i("IAB ready=" + ready);
                    if (!ready)
                        billingClient.startConnection(ActivityBilling.this);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }, backoff * 1000L);
    }

    @Override
    public void onPurchasesUpdated(BillingResult result, @Nullable List<Purchase> purchases) {
        Log.i("IAB purchases updated");
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK)
            checkPurchases(purchases);
        else
            reportError(result, "IAB purchases updated");
    }

    private void queryPurchases() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, this);
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult result, @NonNull List<Purchase> list) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK)
            checkPurchases(list);
        else
            reportError(result, "IAB query purchases");
    }
*/
    interface IBillingListener {
        void onConnected();

        void onDisconnected();

        void onSkuDetails(String sku, String price);

        void onPurchasePending(String sku);

        void onPurchased(String sku, boolean purchased);

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
        query.add(getSkuPro(this));

        if (purchases != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            if (prefs.getBoolean("play_store", true)) {
                long cached = prefs.getLong(getSkuPro(this) + ".cached", 0);
                if (cached + MAX_SKU_CACHE_DURATION < new Date().getTime()) {
                    Log.i("IAB cache expired=" + new Date(cached));
                    editor.remove("pro");
                } else
                    Log.i("IAB caching until=" + new Date(cached + MAX_SKU_CACHE_DURATION));
            }

            for (Purchase purchase : purchases)
                for (String sku : purchase.getSkus())
                    try {
                        query.remove(sku);

                        long time = purchase.getPurchaseTime();
                        Log.i("IAB SKU=" + sku +
                                " purchased=" + isPurchased(purchase) +
                                " valid=" + isPurchaseValid(purchase) +
                                " time=" + new Date(time));
                        Log.i("IAB json=" + purchase.getOriginalJson());

                        for (IBillingListener listener : listeners)
                            if (isPurchaseValid(purchase))
                                listener.onPurchased(sku, true);
                            else
                                listener.onPurchasePending(sku);

                        if (isPurchased(purchase)) {
                            byte[] decodedKey = Base64.decode(getString(R.string.public_key), Base64.DEFAULT);
                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
                            Signature sig = Signature.getInstance("SHA1withRSA");
                            sig.initVerify(publicKey);
                            sig.update(purchase.getOriginalJson().getBytes());
                            if (SKU_TEST.equals(sku) ||
                                    sig.verify(Base64.decode(purchase.getSignature(), Base64.DEFAULT))) {
                                Log.i("IAB valid signature");
                                if (getSkuPro(this).equals(sku)) {
                                    if (isPurchaseValid(purchase)) {
                                        editor.putBoolean("pro", true);
                                        editor.putLong(sku + ".cached", new Date().getTime());
                                        editor.putString("iab_json", purchase.getOriginalJson());
                                        editor.putString("iab_signature", purchase.getSignature());
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
        billingClient.querySkuDetailsAsync(builder.build(), this);
    }

    @Override
    public void onSkuDetailsResponse(@NonNull BillingResult result, List<SkuDetails> skuDetailsList) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            for (SkuDetails skuDetail : skuDetailsList) {
                Log.i("IAB SKU detail=" + skuDetail);
                for (IBillingListener listener : listeners)
                    listener.onSkuDetails(skuDetail.getSku(), skuDetail.getPrice());
            }
        } else
            reportError(result, "IAB query SKUs");
    }

    private void consumePurchase(final Purchase purchase) {
        for (String sku : purchase.getSkus()) {
            Log.i("IAB consuming SKU=" + sku);
            ConsumeParams params = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.consumeAsync(params, new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(@NonNull BillingResult result, @NonNull String purchaseToken) {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (IBillingListener listener : listeners)
                            listener.onPurchased(sku, false);
                    } else
                        reportError(result, "IAB consuming SKU=" + sku);
                }
            });
        }
    }

    private void acknowledgePurchase(final Purchase purchase, int retry) {
        for (String sku : purchase.getSkus()) {
            Log.i("IAB acknowledging purchase SKU=" + sku);
            AcknowledgePurchaseParams params =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult result) {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityBilling.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("pro", true);
                        editor.putLong(sku + ".cached", new Date().getTime());
                        editor.apply();

                        for (IBillingListener listener : listeners)
                            listener.onPurchased(sku, true);

                        WidgetUnified.updateData(ActivityBilling.this);
                    } else {
                        if (retry < 3) {
                            new Handler().postDelayed(new RunnableEx("IAB ack retry") {
                                @Override
                                public void delegate() {
                                    acknowledgePurchase(purchase, retry + 1);
                                }
                            }, (retry + 1) * 10 * 1000L);
                        } else
                            reportError(result, "IAB acknowledged SKU=" + sku);
                    }
                }
            });
        }
    }

    private boolean isPurchased(Purchase purchase) {
        return (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED);
    }

    private boolean isPurchaseValid(Purchase purchase) {
        return (isPurchased(purchase) &&
                (purchase.isAcknowledged() ||
                        purchase.getSkus().contains(SKU_TEST) ||
                        purchase.getPurchaseTime() + MAX_SKU_NOACK_DURATION > new Date().getTime()));
    }

    private void reportError(BillingResult result, String stage) {
        String message;
        if (result == null)
            message = stage;
        else {
            message = getBillingResponseText(result);

            if (result.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE)
                message += " Is the Play Store app logged into the account used to install the app?";

            String debug = result.getDebugMessage();
            if (!TextUtils.isEmpty(debug))
                message += " " + debug;

            message += " " + stage;
        }

        EntityLog.log(this, message);

        if (result != null) {
            // https://developer.android.com/reference/com/android/billingclient/api/BillingClient.BillingResponse#service_disconnected
            if (result.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                retry(60);

            if (result.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED)
                return;
        }

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

            case BillingClient.BillingResponseCode.NETWORK_ERROR:
                // A network error occurred during the operation
                return "NETWORK_ERROR";

            default:
                return Integer.toString(result.getResponseCode());
        }
    }
 */
}
