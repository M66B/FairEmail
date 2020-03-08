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
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsConnection extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swMetered;
    private Spinner spDownload;
    private SwitchCompat swRoaming;
    private SwitchCompat swRlah;
    private EditText etTimeout;
    private SwitchCompat swSslHarden;
    private SwitchCompat swSocks;
    private EditText etSocks;
    private Button btnSocks;
    private Button btnManage;
    private TextView tvConnectionType;
    private TextView tvConnectionRoaming;

    private final static String[] RESET_OPTIONS = new String[]{
            "metered", "download", "roaming", "rlah", "timeout", "ssl_harden", "socks_enabled", "socks_proxy"
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
        swSslHarden = view.findViewById(R.id.swSslHarden);
        swSocks = view.findViewById(R.id.swSocks);
        etSocks = view.findViewById(R.id.etSocks);
        btnSocks = view.findViewById(R.id.btnSocks);
        btnManage = view.findViewById(R.id.btnManage);

        tvConnectionType = view.findViewById(R.id.tvConnectionType);
        tvConnectionRoaming = view.findViewById(R.id.tvConnectionRoaming);

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

        swSslHarden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("ssl_harden", checked).apply();
            }
        });

        swSocks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("socks_enabled", checked).apply();
                etSocks.setEnabled(checked);
                btnSocks.setEnabled(checked);
            }
        });

        btnSocks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String proxy = etSocks.getText().toString();
                if (TextUtils.isEmpty(proxy))
                    prefs.edit().remove("socks_proxy").apply();
                else
                    prefs.edit().putString("socks_proxy", proxy).apply();
            }
        });

        final Intent manage = getIntentConnectivity();
        btnManage.setVisibility(
                manage.resolveActivity(getContext().getPackageManager()) == null
                        ? View.GONE : View.VISIBLE);

        btnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(manage);
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

        int download = prefs.getInt("download", MessageHelper.DEFAULT_ATTACHMENT_DOWNLOAD_SIZE);
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

        swSslHarden.setChecked(prefs.getBoolean("ssl_harden", false));
        swSocks.setChecked(prefs.getBoolean("socks_enabled", false));
        etSocks.setText(prefs.getString("socks_proxy", null));
        etSocks.setEnabled(swSocks.isChecked());
        btnSocks.setEnabled(swSocks.isChecked());
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
        public void onLost(@NonNull Network network) {
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
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    ConnectionHelper.NetworkState networkState = ConnectionHelper.getNetworkState(getContext());

                    tvConnectionType.setText(networkState.isUnmetered() ? R.string.title_legend_unmetered : R.string.title_legend_metered);
                    tvConnectionType.setVisibility(networkState.isConnected() ? View.VISIBLE : View.GONE);
                    tvConnectionRoaming.setVisibility(networkState.isRoaming() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }
}
