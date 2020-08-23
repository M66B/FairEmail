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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

public class FragmentPro extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView tvPending;
    private TextView tvActivated;
    private TextView tvInfo;
    private CheckBox cbHide;
    private TextView tvList;
    private Button btnPurchase;
    private TextView tvPrice;
    private ImageView ivExternal;
    private TextView tvPriceHint;
    private TextView tvRestoreHint;
    private Button btnCheck;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_pro);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean debug = prefs.getBoolean("debug", false);

        View view = inflater.inflate(R.layout.fragment_pro, container, false);

        tvPending = view.findViewById(R.id.tvPending);
        tvActivated = view.findViewById(R.id.tvActivated);
        tvInfo = view.findViewById(R.id.tvInfo);
        cbHide = view.findViewById(R.id.cbHide);
        tvList = view.findViewById(R.id.tvList);
        btnPurchase = view.findViewById(R.id.btnPurchase);
        tvPrice = view.findViewById(R.id.tvPrice);
        ivExternal = view.findViewById(R.id.ivExternal);
        tvPriceHint = view.findViewById(R.id.tvPriceHint);
        tvRestoreHint = view.findViewById(R.id.tvRestoreHint);

        btnCheck = view.findViewById(R.id.btnCheck);

        tvInfo.setText(getString(R.string.title_pro_info)
                .replaceAll("^\\s+", "").replaceAll("\\s+", " "));

        long banner_hidden = prefs.getLong("banner_hidden", 0);
        cbHide.setChecked(banner_hidden > 0);
        cbHide.setText(getString(R.string.title_pro_hide, ServiceUI.HIDE_BANNER));

        cbHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ServiceUI.scheduleBanner(getContext(), isChecked);
            }
        });

        tvList.setPaintFlags(tvList.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(getContext(), Uri.parse(BuildConfig.PRO_FEATURES_URI), false);
            }
        });

        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivityBilling.ACTION_PURCHASE));
            }
        });

        ivExternal.setVisibility(Helper.isPlayStoreInstall() ? View.GONE : View.VISIBLE);

        tvPriceHint.setPaintFlags(tvPriceHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPriceHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 19);
            }
        });

        tvRestoreHint.setPaintFlags(tvRestoreHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvRestoreHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 117);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivityBilling.ACTION_PURCHASE_CHECK));
            }
        });

        tvPending.setVisibility(View.GONE);
        tvActivated.setVisibility(View.GONE);
        cbHide.setVisibility(View.GONE);
        btnPurchase.setEnabled(!Helper.isPlayStoreInstall());
        tvPrice.setText(null);
        tvRestoreHint.setVisibility(Helper.isPlayStoreInstall() || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        btnCheck.setEnabled(false);
        btnCheck.setVisibility(Helper.isPlayStoreInstall() && debug ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addBillingListener(new ActivityBilling.IBillingListener() {
            @Override
            public void onConnected() {
                btnCheck.setEnabled(true);
            }

            @Override
            public void onDisconnected() {
                btnCheck.setEnabled(false);
            }

            @Override
            public void onSkuDetails(String sku, String price) {
                if (ActivityBilling.getSkuPro().equals(sku)) {
                    tvPrice.setText(price);
                    btnPurchase.setEnabled(true);
                }
            }

            @Override
            public void onPurchasePending(String sku) {
                if (ActivityBilling.getSkuPro().equals(sku)) {
                    btnPurchase.setEnabled(false);
                    tvPending.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPurchased(String sku) {
                if (ActivityBilling.getSkuPro().equals(sku)) {
                    btnPurchase.setEnabled(false);
                    tvPending.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(final String message) {
                final View view = getView();
                if (view == null)
                    return;

                Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                        .setGestureInsetBottomIgnored(true);
                snackbar.setAction(R.string.title_setup_help, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(view.getContext());
                        lbm.sendBroadcast(
                                new Intent(ActivityBilling.ACTION_PURCHASE_ERROR)
                                        .putExtra("message", message));
                    }
                });
                snackbar.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "pro");
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("pro".equals(key)) {
            boolean pro = ActivityBilling.isPro(getContext());
            tvActivated.setVisibility(pro ? View.VISIBLE : View.GONE);
            cbHide.setVisibility(pro ? View.GONE : View.VISIBLE);
        } else if ("banner_hidden".equals(key)) {
            long banner_hidden = prefs.getLong("banner_hidden", 0);
            cbHide.setChecked(banner_hidden > 0);
        }
    }
}
