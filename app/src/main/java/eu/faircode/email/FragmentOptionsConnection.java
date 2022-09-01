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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsConnection extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View view;
    private ImageButton ibHelp;
    private SwitchCompat swMetered;
    private Spinner spDownload;
    private SwitchCompat swRoaming;
    private SwitchCompat swRlah;
    private SwitchCompat swDownloadHeaders;
    private SwitchCompat swDownloadEml;
    private SwitchCompat swDownloadPlain;
    private SwitchCompat swValidated;
    private SwitchCompat swValidatedCaptive;
    private SwitchCompat swVpnOnly;
    private EditText etTimeout;
    private SwitchCompat swPreferIp4;
    private SwitchCompat swBindSocket;
    private SwitchCompat swStandaloneVpn;
    private SwitchCompat swTcpKeepAlive;
    private TextView tvTcpKeepAliveHint;
    private SwitchCompat swSslHarden;
    private SwitchCompat swSslHardenStrict;
    private SwitchCompat swCertStrict;
    private SwitchCompat swOpenSafe;
    private Button btnManage;
    private TextView tvNetworkMetered;
    private TextView tvNetworkRoaming;
    private CardView cardDebug;
    private Button btnCiphers;
    private TextView tvNetworkInfo;

    private Group grpValidated;

    private final static String[] RESET_OPTIONS = new String[]{
            "metered", "download", "roaming", "rlah",
            "download_headers", "download_eml", "download_plain",
            "require_validated", "require_validated_captive", "vpn_only",
            "timeout", "prefer_ip4", "bind_socket", "standalone_vpn", "tcp_keep_alive",
            "ssl_harden", "ssl_harden_strict", "cert_strict", "open_safe"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_options_connection, container, false);

        // Get controls

        ibHelp = view.findViewById(R.id.ibHelp);
        swMetered = view.findViewById(R.id.swMetered);
        spDownload = view.findViewById(R.id.spDownload);
        swRoaming = view.findViewById(R.id.swRoaming);
        swRlah = view.findViewById(R.id.swRlah);
        swDownloadHeaders = view.findViewById(R.id.swDownloadHeaders);
        swDownloadEml = view.findViewById(R.id.swDownloadEml);
        swDownloadPlain = view.findViewById(R.id.swDownloadPlain);
        swValidated = view.findViewById(R.id.swValidated);
        swValidatedCaptive = view.findViewById(R.id.swValidatedCaptive);
        swVpnOnly = view.findViewById(R.id.swVpnOnly);
        etTimeout = view.findViewById(R.id.etTimeout);
        swPreferIp4 = view.findViewById(R.id.swPreferIp4);
        swBindSocket = view.findViewById(R.id.swBindSocket);
        swStandaloneVpn = view.findViewById(R.id.swStandaloneVpn);
        swTcpKeepAlive = view.findViewById(R.id.swTcpKeepAlive);
        tvTcpKeepAliveHint = view.findViewById(R.id.tvTcpKeepAliveHint);
        swSslHarden = view.findViewById(R.id.swSslHarden);
        swSslHardenStrict = view.findViewById(R.id.swSslHardenStrict);
        swCertStrict = view.findViewById(R.id.swCertStrict);
        swOpenSafe = view.findViewById(R.id.swOpenSafe);
        btnManage = view.findViewById(R.id.btnManage);

        tvNetworkMetered = view.findViewById(R.id.tvNetworkMetered);
        tvNetworkRoaming = view.findViewById(R.id.tvNetworkRoaming);

        cardDebug = view.findViewById(R.id.cardDebug);
        btnCiphers = view.findViewById(R.id.btnCiphers);
        tvNetworkInfo = view.findViewById(R.id.tvNetworkInfo);

        grpValidated = view.findViewById(R.id.grpValidated);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean debug = prefs.getBoolean("debug", false);

        ibHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Options:connection"), false);
            }
        });

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

        swDownloadHeaders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("download_headers", checked).apply();
            }
        });

        swDownloadEml.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("download_eml", checked).apply();
            }
        });

        swDownloadPlain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("download_plain", checked).apply();
            }
        });

        grpValidated.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? View.GONE : View.VISIBLE);
        swValidated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("require_validated", checked).apply();
                swValidatedCaptive.setEnabled(!checked);
            }
        });

        swValidatedCaptive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("require_validated_captive", checked).apply();
            }
        });

        swVpnOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("vpn_only", checked).apply();
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

        swBindSocket.setVisibility(debug || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        swBindSocket.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("bind_socket", checked).apply();
            }
        });

        swStandaloneVpn.setVisibility(debug || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        swStandaloneVpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("standalone_vpn", checked).apply();
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
                swSslHardenStrict.setEnabled(checked);
            }
        });

        swSslHardenStrict.setVisibility(BuildConfig.PLAY_STORE_RELEASE ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                ? View.GONE : View.VISIBLE);
        swSslHardenStrict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("ssl_harden_strict", checked).apply();
            }
        });

        swCertStrict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("cert_strict", checked).apply();
            }
        });

        swOpenSafe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("open_safe", checked).apply();
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

        btnCiphers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setIcon(R.drawable.twotone_info_24)
                        .setTitle(R.string.title_advanced_ciphers)
                        .setMessage(Log.getCiphers())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        // Initialize
        FragmentDialogTheme.setBackground(getContext(), view, false);
        tvNetworkMetered.setVisibility(View.GONE);
        tvNetworkRoaming.setVisibility(View.GONE);
        cardDebug.setVisibility(View.GONE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

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

        setOptions();
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = Helper.getSystemService(getContext(), ConnectivityManager.class);
        if (cm == null)
            return;

        showConnectionType();

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onPause() {
        ConnectivityManager cm = Helper.getSystemService(getContext(), ConnectivityManager.class);
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
        if (item.getItemId() == R.id.menu_default) {
            FragmentOptions.reset(getContext(), RESET_OPTIONS, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOptions() {
        if (view == null || getContext() == null)
            return;

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

        swDownloadHeaders.setChecked(prefs.getBoolean("download_headers", false));
        swDownloadEml.setChecked(prefs.getBoolean("download_eml", false));
        swDownloadPlain.setChecked(prefs.getBoolean("download_plain", false));

        swValidated.setChecked(prefs.getBoolean("require_validated", false));
        swValidatedCaptive.setChecked(prefs.getBoolean("require_validated_captive", true));
        swValidatedCaptive.setEnabled(!swValidated.isChecked());
        swVpnOnly.setChecked(prefs.getBoolean("vpn_only", false));

        int timeout = prefs.getInt("timeout", 0);
        etTimeout.setText(timeout == 0 ? null : Integer.toString(timeout));
        etTimeout.setHint(Integer.toString(EmailService.DEFAULT_CONNECT_TIMEOUT));

        swPreferIp4.setChecked(prefs.getBoolean("prefer_ip4", true));
        swBindSocket.setChecked(prefs.getBoolean("bind_socket", false));
        swStandaloneVpn.setChecked(prefs.getBoolean("standalone_vpn", false));
        swTcpKeepAlive.setChecked(prefs.getBoolean("tcp_keep_alive", false));
        swSslHarden.setChecked(prefs.getBoolean("ssl_harden", false));
        swSslHardenStrict.setChecked(prefs.getBoolean("ssl_harden_strict", false));
        swSslHardenStrict.setEnabled(swSslHarden.isChecked());
        swCertStrict.setChecked(prefs.getBoolean("cert_strict", !BuildConfig.PLAY_STORE_RELEASE));
        swOpenSafe.setChecked(prefs.getBoolean("open_safe", false));
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
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        final Context context = getContext();
        final ConnectionHelper.NetworkState networkState = ConnectionHelper.getNetworkState(context);

        final StringBuilder sb = new StringBuilder();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false);
        if ((debug || BuildConfig.DEBUG) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            try {
                ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
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

                sb.append("VPN=")
                        .append(ConnectionHelper.vpnActive(context)).append("\r\n");
                sb.append("Airplane mode=")
                        .append(ConnectionHelper.airplaneMode(context)).append("\r\n");
            } catch (Throwable ex) {
                Log.e(ex);
            }

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;
                tvNetworkMetered.setText(networkState.isUnmetered() ? R.string.title_legend_unmetered : R.string.title_legend_metered);
                tvNetworkInfo.setText(sb.toString());
                tvNetworkMetered.setVisibility(networkState.isConnected() ? View.VISIBLE : View.GONE);
                tvNetworkRoaming.setVisibility(networkState.isRoaming() ? View.VISIBLE : View.GONE);
                cardDebug.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);
            }
        });
    }
}
