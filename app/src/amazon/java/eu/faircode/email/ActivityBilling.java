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
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserDataResponse;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityBilling extends ActivityBase implements PurchasingListener, FragmentManager.OnBackStackChangedListener {
    private boolean standalone = false;
    private List<IBillingListener> listeners = new ArrayList<>();

    static final String ACTION_PURCHASE = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE";
    static final String ACTION_PURCHASE_CONSUME = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE_CONSUME";
    static final String ACTION_PURCHASE_ERROR = BuildConfig.APPLICATION_ID + ".ACTION_PURCHASE_ERROR";

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

        if (Helper.isAmazonInstall() || isTesting(this)) {
            Log.i("IAB start sandbox=" + PurchasingService.IS_SANDBOX_MODE);
            PurchasingService.registerListener(this.getApplicationContext(), this);
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

        update();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (standalone) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            lbm.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @NonNull
    static String getSkuPro(Context context) {
        return BuildConfig.APPLICATION_ID.replace(".debug", "") + ".pro";
    }

    static boolean isTesting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return (BuildConfig.TEST_RELEASE && prefs.getBoolean("test_iab", false));
    }

    private static String getChallenge(Context context) throws NoSuchAlgorithmException {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
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
                    onPurchaseConsume(intent);
                else if (ACTION_PURCHASE_ERROR.equals(intent.getAction()))
                    onPurchaseError(intent);
            }
        }
    };

    private void onPurchase(Intent intent) {
        if (Helper.isAmazonInstall() || isTesting(this))
            try {
                String skuPro = getSkuPro(this);
                Log.i("IAB purchase SKU=" + skuPro);
                RequestId requestId = PurchasingService.purchase(skuPro);
                Log.i("IAB request=" + requestId);
            } catch (Throwable ex) {
                reportError(ex.toString(), "onPurchase");
            }
        else
            try {
                Uri uri = Uri.parse(BuildConfig.PRO_FEATURES_URI +
                        "?challenge=" + getChallenge(this) +
                        "&version=" + BuildConfig.VERSION_CODE);
                Helper.view(this, uri, true);
            } catch (NoSuchAlgorithmException ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
    }

    private void onPurchaseConsume(Intent intent) {
        PurchasingService.getPurchaseUpdates(true);
    }

    private void onPurchaseError(Intent intent) {
        String message = intent.getStringExtra("message");
        Uri uri = Helper.getSupportUri(this, "Purchase:error");
        if (!TextUtils.isEmpty(message))
            uri = uri.buildUpon().appendQueryParameter("message", "IAB: " + message).build();
        Helper.view(this, uri, true);
    }

    private void update() {
        if (Helper.isAmazonInstall() || isTesting(this)) {
            Log.i("IAB update");
            PurchasingService.getUserData();
            PurchasingService.getPurchaseUpdates(true); // TODO: reset?
            Set<String> skus = new HashSet<>();
            skus.add(getSkuPro(this));
            PurchasingService.getProductData(skus);
        }
    }

    @Override
    public void onUserDataResponse(UserDataResponse response) {
        Log.i("IAB user data status=" + response.getRequestStatus());

        switch (response.getRequestStatus()) {
            case SUCCESSFUL:
                Log.i("IAB user=" + response.getUserData()
                        .toString().replace('\n', '|'));
                for (IBillingListener listener : listeners)
                    listener.onConnected();
                break;

            default:
                reportError(response.getRequestStatus().toString(), "onUserDataResponse");
                break;
        }
    }

    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
        Log.i("IAB purchase updates status=" + response.getRequestStatus());

        switch (response.getRequestStatus()) {
            case SUCCESSFUL:
                for (Receipt receipt : response.getReceipts())
                    handleReceipt(receipt);
                if (response.hasMore())
                    PurchasingService.getPurchaseUpdates(false);
                break;

            default:
                reportError(response.getRequestStatus().toString(), "onPurchaseUpdatesResponse");
                break;
        }
    }

    @Override
    public void onProductDataResponse(ProductDataResponse response) {
        Log.i("IAB product data status=" + response.getRequestStatus());

        switch (response.getRequestStatus()) {
            case SUCCESSFUL:
                for (String sku : response.getUnavailableSkus())
                    Log.i("IAB unavailable sku=" + sku);
                Map<String, Product> products = response.getProductData();
                for (String key : products.keySet()) {
                    Product product = products.get(key);
                    Log.i("IAB product=" + product.toString().replace('\n', '|'));
                    if (getSkuPro(this).equals(product.getSku()))
                        for (IBillingListener listener : listeners)
                            listener.onSkuDetails(product.getSku(), product.getPrice());
                }
                break;

            default:
                reportError(response.getRequestStatus().toString(), "onProductDataResponse");
                break;
        }
    }

    @Override
    public void onPurchaseResponse(PurchaseResponse response) {
        Log.i("IAB purchase response status=" + response.getRequestStatus());

        switch (response.getRequestStatus()) {
            case SUCCESSFUL:
                handleReceipt(response.getReceipt());
                break;

            default:
                reportError(response.getRequestStatus().toString(), "onPurchaseResponse");
                break;
        }
    }

    private void handleReceipt(Receipt receipt) {
        Log.i("IAB receipt=" + receipt.toString().replace('\n', '|') +
                " canceled=" + receipt.isCanceled() + "/" + receipt.getCancelDate());

        if (getSkuPro(this).equals(receipt.getSku())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (receipt.isCanceled()) {
                prefs.edit().remove("pro").apply();
                PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.UNAVAILABLE);
            } else {
                prefs.edit().putBoolean("pro", true).apply();
                PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
            }

            WidgetUnified.updateData(this);

            for (IBillingListener listener : listeners)
                listener.onPurchased(receipt.getSku(), !receipt.isCanceled());
        }
    }

    private void reportError(String status, String stage) {
        String message = status + " " + stage;
        Log.e(message);
        EntityLog.log(this, message);

        for (IBillingListener listener : listeners)
            listener.onError(message);
    }

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

        update();

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.i("IAB removing billing listener=" + listener);
                listeners.remove(listener);
            }
        });
    }
}
