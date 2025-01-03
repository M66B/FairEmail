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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.TextViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.Date;

public class FragmentPro extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView tvPending;
    private TextView tvActivated;
    private Button btnBackup;
    private TextView tvInfo;
    private CheckBox cbHide;
    private TextView tvList;
    private Button btnPurchase;
    private TextView tvPrice;
    private TextView tvGoogle;
    private TextView tvNoPlay;
    private TextView tvDownloaded;
    private TextView tvPriceHint;
    private TextView tvFamilyHint;
    private TextView tvRestoreHint;
    private Button btnSupport;
    private Button btnConsume;
    private ImageView ivConnected;

    private static final int HIDE_BANNER = 8; // weeks

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_pro);
        setHasOptionsMenu(true);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        View view = inflater.inflate(R.layout.fragment_pro, container, false);

        tvPending = view.findViewById(R.id.tvPending);
        tvActivated = view.findViewById(R.id.tvActivated);
        btnBackup = view.findViewById(R.id.btnBackup);
        tvInfo = view.findViewById(R.id.tvInfo);
        cbHide = view.findViewById(R.id.cbHide);
        tvList = view.findViewById(R.id.tvList);
        btnPurchase = view.findViewById(R.id.btnPurchase);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvGoogle = view.findViewById(R.id.tvGoogle);
        tvNoPlay = view.findViewById(R.id.tvNoPlay);
        tvDownloaded = view.findViewById(R.id.tvDownloaded);
        tvPriceHint = view.findViewById(R.id.tvPriceHint);
        tvFamilyHint = view.findViewById(R.id.tvFamilyHint);
        tvRestoreHint = view.findViewById(R.id.tvRestoreHint);
        btnSupport = view.findViewById(R.id.btnSupport);
        btnConsume = view.findViewById(R.id.btnConsume);
        ivConnected = view.findViewById(R.id.ivConnected);

        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "backup"));
            }
        });

        tvInfo.setText(getString(R.string.title_pro_info)
                .replaceAll("^\\s+", "").replaceAll("\\s+", " "));

        cbHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    long banner_hidden = new Date().getTime() + HIDE_BANNER * 7 * 24 * 3600 * 1000L;
                    prefs.edit().putLong("banner_hidden", banner_hidden).apply();
                } else
                    prefs.edit().remove("banner_hidden").apply();
            }
        });

        tvList.setPaintFlags(tvList.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(view.getContext(), Uri.parse(BuildConfig.PRO_FEATURES_URI), false);
            }
        });

        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivityBilling.ACTION_PURCHASE));
            }
        });

        String type = Log.getReleaseType(getContext());
        String installer = Helper.getInstallerName(getContext());
        tvDownloaded.setText(getString(R.string.app_download, type));
        if (BuildConfig.PLAY_STORE_RELEASE)
            tvDownloaded.setVisibility(
                    installer != null && !Helper.PLAY_PACKAGE_NAME.equals(installer)
                            ? View.VISIBLE : View.GONE);
        else
            tvDownloaded.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        tvPriceHint.setPaintFlags(tvPriceHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPriceHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 19);
            }
        });

        tvFamilyHint.setPaintFlags(tvFamilyHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvFamilyHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 66);
            }
        });

        tvRestoreHint.setPaintFlags(tvRestoreHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvRestoreHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 117);
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Pro:support"), false);
            }
        });

        btnConsume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivityBilling.ACTION_PURCHASE_CONSUME));
            }
        });

        boolean play = (Helper.isPlayStoreInstall() || ActivityBilling.isTesting(getContext()));

        long now = new Date().getTime();
        long banner_hidden = prefs.getLong("banner_hidden", 0);
        cbHide.setChecked(banner_hidden > 0 && now < banner_hidden);
        cbHide.setText(getString(R.string.title_pro_hide, HIDE_BANNER));

        tvPending.setVisibility(View.GONE);
        tvActivated.setVisibility(View.GONE);
        btnBackup.setVisibility(View.GONE);
        cbHide.setVisibility(View.GONE);
        btnPurchase.setEnabled(!play);
        btnPurchase.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                play ? R.drawable.twotone_shop_24 : R.drawable.twotone_open_in_new_24, 0);
        tvPrice.setVisibility(View.GONE);
        tvGoogle.setVisibility(play ? View.VISIBLE : View.GONE);
        tvNoPlay.setVisibility(
                BuildConfig.PLAY_STORE_RELEASE && !Helper.hasPlayStore(getContext())
                        ? View.VISIBLE : View.GONE);
        tvFamilyHint.setVisibility(play ? View.VISIBLE : View.GONE);
        tvRestoreHint.setVisibility(play ? View.VISIBLE : View.GONE);
        btnSupport.setVisibility(play ? View.VISIBLE : View.GONE);
        btnConsume.setEnabled(false);
        btnConsume.setVisibility(ActivityBilling.isTesting(getContext()) ? View.VISIBLE : View.GONE);
        ivConnected.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addBillingListener(new ActivityBilling.IBillingListener() {
            @Override
            public void onConnected() {
                post(new RunnableEx("pro:connected") {
                    @Override
                    public void delegate() {
                        ivConnected.setImageResource(R.drawable.twotone_cloud_done_24);
                        ivConnected.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onDisconnected() {
                post(new RunnableEx("pro:disconnected") {
                    @Override
                    public void delegate() {
                        if (ivConnected != null) {
                            ivConnected.setImageResource(R.drawable.twotone_cloud_off_24);
                            ivConnected.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onSkuDetails(String sku, String price) {
                if (!ActivityBilling.getSkuPro(getContext()).equals(sku))
                    return;

                post(new RunnableEx("pro:sku") {
                    @Override
                    public void delegate() {
                        tvPrice.setText(getString(R.string.title_pro_one_time, price));
                        tvPrice.setVisibility(View.VISIBLE);
                        btnPurchase.setEnabled(true);
                    }
                });
            }

            @Override
            public void onPurchasePending(String sku) {
                if (!ActivityBilling.getSkuPro(getContext()).equals(sku))
                    return;

                post(new RunnableEx("pro:pending") {
                    @Override
                    public void delegate() {
                        btnPurchase.setEnabled(false);
                        tvPending.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onPurchased(String sku, boolean purchased) {
                if (!ActivityBilling.getSkuPro(getContext()).equals(sku))
                    return;

                post(new RunnableEx("pro:purchased") {
                    @Override
                    public void delegate() {
                        int color = Helper.resolveColor(btnPurchase.getContext(), R.attr.colorInfoForeground);
                        if (purchased)
                            color = ColorUtils.setAlphaComponent(color, (int) Math.round(0.6 * 255));
                        btnPurchase.setEnabled(!purchased);
                        btnPurchase.setTextColor(color);
                        TextViewCompat.setCompoundDrawableTintList(btnPurchase, ColorStateList.valueOf(color));
                        tvPending.setVisibility(View.GONE);
                        btnConsume.setEnabled(purchased);
                    }
                });
            }

            @Override
            public void onError(final String message) {
                final View view = getView();
                if (view == null)
                    return;

                Snackbar snackbar = Helper.setSnackbarOptions(
                        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE));
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

            private void post(Runnable runnable) {
                final View view = getView();
                if (view == null)
                    return;

                view.post(new RunnableEx("pro:post") {
                    @Override
                    public void delegate() {
                        try {
                            runnable.run();
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pro, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.menu_response).setVisible(!Helper.isPlayStoreInstall());
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_response) {
            onMenuResponse();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuResponse() {
        final Context context = getContext();
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_response, null);
        final EditText etResponse = dview.findViewById(R.id.etResponse);

        new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String response = etResponse.getText().toString().trim();
                            int q = response.indexOf("?response=");
                            if (q > 0)
                                response = response.substring(q + 10);
                            if (ActivityBilling.activatePro(context, response))
                                ToastEx.makeText(context, R.string.title_pro_valid, Toast.LENGTH_LONG).show();
                            else
                                ToastEx.makeText(context, R.string.title_pro_invalid, Toast.LENGTH_LONG).show();
                        } catch (Throwable ex) {
                            Log.e(ex);
                            ToastEx.makeText(context, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("pro".equals(key)) {
            boolean pro = ActivityBilling.isPro(getContext());
            tvActivated.setVisibility(pro ? View.VISIBLE : View.GONE);
            btnBackup.setVisibility(pro ? View.VISIBLE : View.GONE);
            cbHide.setVisibility(pro ? View.GONE : View.VISIBLE);
        } else if ("banner_hidden".equals(key)) {
            long now = new Date().getTime();
            long banner_hidden = prefs.getLong("banner_hidden", 0);
            cbHide.setChecked(banner_hidden > 0 && now < banner_hidden);
        }
    }
}
