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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsConnection extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swMetered;
    private Spinner spDownload;
    private SwitchCompat swRlah;
    private SwitchCompat swRoaming;
    private TextView tvConnectionType;
    private TextView tvConnectionRoaming;

    private final static String[] RESET_OPTIONS = new String[]{
            "metered", "download", "rlah", "roaming"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_connection, container, false);

        // Get controls

        swMetered = view.findViewById(R.id.swMetered);
        spDownload = view.findViewById(R.id.spDownload);
        swRlah = view.findViewById(R.id.swRlah);
        swRoaming = view.findViewById(R.id.swRoaming);

        tvConnectionType = view.findViewById(R.id.tvConnectionType);
        tvConnectionRoaming = view.findViewById(R.id.tvConnectionRoaming);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swMetered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("metered", checked).apply();
                ServiceSynchronize.reload(getContext(), "metered=" + checked);
            }
        });

        spDownload.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.downloadValues);
                prefs.edit().putInt("download", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("download").apply();
            }
        });

        swRlah.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("rlah", checked).apply();
                ServiceSynchronize.reload(getContext(), "rlah=" + checked);
            }
        });

        swRoaming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("roaming", checked).apply();
                ServiceSynchronize.reload(getContext(), "roaming=" + checked);
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        tvConnectionType.setVisibility(View.GONE);
        tvConnectionRoaming.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        setOptions();
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onPause() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default:
                onMenuDefault();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_OPTIONS)
            editor.remove(option);
        editor.apply();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swMetered.setChecked(prefs.getBoolean("metered", true));

        int download = prefs.getInt("download", MessageHelper.DEFAULT_ATTACHMENT_DOWNLOAD_SIZE);
        int[] downloadValues = getResources().getIntArray(R.array.downloadValues);
        for (int pos = 0; pos < downloadValues.length; pos++)
            if (downloadValues[pos] == download) {
                spDownload.setSelection(pos);
                break;
            }

        swRlah.setChecked(prefs.getBoolean("rlah", true));
        swRoaming.setChecked(prefs.getBoolean("roaming", true));
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            showConnectionType();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            showConnectionType();
        }

        @Override
        public void onLost(Network network) {
            showConnectionType();
        }
    };

    private void showConnectionType() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    ConnectionHelper.NetworkState networkState = ConnectionHelper.getNetworkState(getContext());

                    tvConnectionType.setText(networkState.isUnmetered() ? R.string.title_legend_unmetered : R.string.title_legend_metered);
                    tvConnectionType.setVisibility(networkState.isConnected() ? View.VISIBLE : View.GONE);
                    tvConnectionRoaming.setVisibility(networkState.isRoaming() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }
}
