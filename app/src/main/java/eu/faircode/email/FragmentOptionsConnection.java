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

public class FragmentOptionsConnection extends FragmentBase {
    private TextView tvConnectionType;
    private TextView tvConnectionRoaming;
    private SwitchCompat swMetered;
    private Spinner spDownload;
    private SwitchCompat swRoaming;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);

        View view = inflater.inflate(R.layout.fragment_options_connection, container, false);

        // Get controls

        tvConnectionType = view.findViewById(R.id.tvConnectionType);
        tvConnectionRoaming = view.findViewById(R.id.tvConnectionRoaming);
        swMetered = view.findViewById(R.id.swMetered);
        spDownload = view.findViewById(R.id.spDownload);
        swRoaming = view.findViewById(R.id.swRoaming);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        setOptions();

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

        swRoaming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("roaming", checked).apply();
                ServiceSynchronize.reload(getContext(), "roaming=" + checked);
            }
        });

        return view;
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
                    Helper.NetworkState networkState = Helper.getNetworkState(getContext());

                    tvConnectionType.setText(networkState.isUnmetered() ? R.string.title_legend_unmetered : R.string.title_legend_metered);
                    tvConnectionType.setVisibility(networkState.isConnected() ? View.VISIBLE : View.GONE);
                    tvConnectionRoaming.setVisibility(networkState.isRoaming() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }
}
