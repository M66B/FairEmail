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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class FragmentPro extends FragmentEx implements SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView tvActivated;
    private TextView tvList;
    private Button btnPurchase;
    private TextView tvPrice;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_pro);

        View view = inflater.inflate(R.layout.fragment_pro, container, false);

        tvActivated = view.findViewById(R.id.tvActivated);
        tvList = view.findViewById(R.id.tvList);
        btnPurchase = view.findViewById(R.id.btnPurchase);
        tvPrice = view.findViewById(R.id.tvPrice);

        tvList.setText(Html.fromHtml("<a href=\"" + BuildConfig.PRO_FEATURES + "\">" + Html.escapeHtml(getString(R.string.title_pro_list)) + "</a>"));
        tvList.setMovementMethod(LinkMovementMethod.getInstance());

        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_PURCHASE));
            }
        });

        tvPrice.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        onSharedPreferenceChanged(prefs, "pro");
        prefs.registerOnSharedPreferenceChangeListener(this);
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
            boolean pro = prefs.getBoolean(key, false);
            tvActivated.setVisibility(pro ? View.VISIBLE : View.GONE);
            btnPurchase.setEnabled(BuildConfig.DEBUG || !pro);
        }
    }
}
