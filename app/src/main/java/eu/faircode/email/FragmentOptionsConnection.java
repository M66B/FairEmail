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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsConnection extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swMetered;
    private Spinner spDownload;
    private SwitchCompat swRoaming;
    private SwitchCompat swRlah;
    private EditText etTimeout;
    private SwitchCompat swPreferIp4;
    private SwitchCompat swTcpKeepAlive;
    private SwitchCompat swSslHarden;
    private Button btnManage;
    private TextView tvNetworkMetered;
    private TextView tvNetworkRoaming;
    private TextView tvNetworkInfo;

    private final static String[] RESET_OPTIONS = new String[]{
            "metered", "download", "roaming", "rlah", "timeout", "prefer_ip4", "tcp_keep_alive", "ssl_harden"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_connection, container, false);

        // Get controls

        swMetered = view.findViewById(R.id.swMetered);
        spDownload = view.findViewById(R.id.spDownload);
        swRoaming = view.findViewById(R.id.swRoaming);
        swRlah = view.findViewById(R.id.swRlah);
        etTimeout = view.findViewById(R.id.etTimeout);
        swPreferIp4 = view.findViewById(R.id.swPreferIp4);
        swTcpKeepAlive = view.findViewById(R.id.swTcpKeepAlive);
        swSslHarden = view.findViewById(R.id.swSslHarden);
        btnManage = view.findViewById(R.id.btnManage);

        tvNetworkMetered = view.findViewById(R.id.tvNetworkMetered);
        tvNetworkRoaming = view.findViewById(R.id.tvNetworkRoaming);
        tvNetworkInfo = view.findViewById(R.id.tvNetworkInfo);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swMetered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("metered", checked).apply();
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
            }
        });

        swRlah.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("rlah", checked).apply();
            }
        });

        etTimeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int timeout = (s.length() > 0 ? Integer.parseInt(s.toString()) : 0);
                    if (timeout == 0)
                        prefs.edit().remove("timeout").apply();
                    else
                        prefs.edit().putInt("timeout", timeout).apply();
                } catch (NumberFormatException ex) {
                    Log.e(ex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        swPreferIp4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("prefer_ip4", checked).apply();
            }
        });

        swTcpKeepAlive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                try {
                    System.setProperty("fairemail.tcp_keep_alive", Boolean.toString(checked));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
                prefs.edit().putBoolean("tcp_keep_alive", checked).apply();
            }
        });

        swSslHarden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("ssl_harden", checked).apply();
            }
        });

        final Intent manage = getIntentConnectivity();
        PackageManager pm = getContext().getPackageManager();
        btnManage.setVisibility(
                manage.resolveActivity(pm) == null // system whitelisted
                        ? View.GONE : View.VISIBLE);

        btnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(manage);
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        tvNetworkMetered.setVisibility(View.GONE);
        tvNetworkRoaming.setVisibility(View.GONE);
        tvNetworkInfo.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("timeout".equals(key))
            return;

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            setOptions();
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return;

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onPause() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return;

        cm.unregisterNetworkCallback(networkCallback);

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swMetered.setChecked(prefs.getBoolean("metered", true));

        int download = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
        int[] downloadValues = getResources().getIntArray(R.array.downloadValues);
        for (int pos = 0; pos < downloadValues.length; pos++)
            if (downloadValues[pos] == download) {
                spDownload.setSelection(pos);
                break;
            }

        swRoaming.setChecked(prefs.getBoolean("roaming", true));
        swRlah.setChecked(prefs.getBoolean("rlah", true));

        int timeout = prefs.getInt("timeout", 0);
        etTimeout.setText(timeout == 0 ? null : Integer.toString(timeout));
        etTimeout.setHint(Integer.toString(EmailService.DEFAULT_CONNECT_TIMEOUT));

        swPreferIp4.setChecked(prefs.getBoolean("prefer_ip4", true));
        swTcpKeepAlive.setChecked(prefs.getBoolean("tcp_keep_alive", false));
        swSslHarden.setChecked(prefs.getBoolean("ssl_harden", false));
    }

    private static Intent getIntentConnectivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        else
            return new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            showConnectionType();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            showConnectionType();
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            showConnectionType();
        }

        @Override
        public void onLost(@NonNull Network network) {
            showConnectionType();
        }
    };

    private void showConnectionType() {
        final ConnectionHelper.NetworkState networkState = ConnectionHelper.getNetworkState(getContext());

        final StringBuilder sb = new StringBuilder();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean debug = prefs.getBoolean("debug", false);
        if ((debug || BuildConfig.DEBUG) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            try {
                ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                Network active = (cm == null ? null : cm.getActiveNetwork());
                if (active != null) {
                    NetworkInfo ni = cm.getNetworkInfo(active);
                    if (ni != null)
                        sb.append(ni).append("\r\n\r\n");

                    NetworkCapabilities nc = cm.getNetworkCapabilities(active);
                    if (nc != null)
                        sb.append(nc).append("\r\n\r\n");

                    LinkProperties lp = cm.getLinkProperties(active);
                    if (lp != null)
                        sb.append(lp).append("\r\n\r\n");
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    tvNetworkMetered.setText(networkState.isUnmetered() ? R.string.title_legend_unmetered : R.string.title_legend_metered);
                    tvNetworkInfo.setText(sb.toString());
                    tvNetworkMetered.setVisibility(networkState.isConnected() ? View.VISIBLE : View.GONE);
                    tvNetworkRoaming.setVisibility(networkState.isRoaming() ? View.VISIBLE : View.GONE);
                    tvNetworkInfo.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);
                }
            }
        });
    }
}
