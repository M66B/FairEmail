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
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
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
import android.widget.RadioGroup;
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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class FragmentOptionsConnection extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View view;
    private ImageButton ibHelp;
    private SwitchCompat swMetered;
    private Spinner spDownload;
    private SwitchCompat swDownloadLimited;
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
    private SwitchCompat swPreferIp6;
    private SwitchCompat swBindSocket;
    private SwitchCompat swStandaloneVpn;
    private SwitchCompat swDnsCustom;
    private TextView tvDnsExtra;
    private EditText etDnsExtra;
    private SwitchCompat swDnsClear;
    private SwitchCompat swTcpKeepAlive;
    private SwitchCompat swSslUpdate;
    private SwitchCompat swSslHarden;
    private SwitchCompat swSslHardenStrict;
    private SwitchCompat swCertStrict;
    private SwitchCompat swCertTransparency;
    private ImageButton ibCertTransparency;
    private SwitchCompat swCheckNames;
    private SwitchCompat swOpenSafe;
    private SwitchCompat swHttpRedirect;
    private SwitchCompat swBouncyCastle;
    private SwitchCompat swFipsMode;
    private ImageButton ibBouncyCastle;
    private Button btnManage;
    private TextView tvNetworkMetered;
    private TextView tvNetworkRoaming;
    private CardView cardDebug;
    private Button btnCiphers;
    private EditText etHost;
    private RadioGroup rgEncryption;
    private EditText etPort;
    private Button btnCheck;
    private TextView tvNetworkInfo;

    private Group grpValidated;
    private Group grpCustomDns;
    private Group grpBC;
    private Group grpCustomSsl;

    final static List<String> RESET_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            "metered", "download", "download_limited", "roaming", "rlah",
            "download_headers", "download_eml", "download_plain",
            "require_validated", "require_validated_captive", "vpn_only",
            "timeout", "prefer_ip4", "prefer_ip6", "bind_socket", "standalone_vpn",
            "dns_extra", "dns_custom", "dns_clear",
            "tcp_keep_alive",
            "ssl_update", "ssl_harden", "ssl_harden_strict", "cert_strict", "cert_transparency", "check_names",
            "open_safe", "http_redirect",
            "bouncy_castle", "bc_fips"
    ));

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
        swDownloadLimited = view.findViewById(R.id.swDownloadLimited);
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
        swPreferIp6 = view.findViewById(R.id.swPreferIp6);
        swBindSocket = view.findViewById(R.id.swBindSocket);
        swStandaloneVpn = view.findViewById(R.id.swStandaloneVpn);
        swDnsCustom = view.findViewById(R.id.swDnsCustom);
        tvDnsExtra = view.findViewById(R.id.tvDnsExtra);
        etDnsExtra = view.findViewById(R.id.etDnsExtra);
        swDnsClear = view.findViewById(R.id.swDnsClear);
        swTcpKeepAlive = view.findViewById(R.id.swTcpKeepAlive);
        swSslUpdate = view.findViewById(R.id.swSslUpdate);
        swSslHarden = view.findViewById(R.id.swSslHarden);
        swSslHardenStrict = view.findViewById(R.id.swSslHardenStrict);
        swCertStrict = view.findViewById(R.id.swCertStrict);
        swCertTransparency = view.findViewById(R.id.swCertTransparency);
        ibCertTransparency = view.findViewById(R.id.ibCertTransparency);
        swCheckNames = view.findViewById(R.id.swCheckNames);
        swOpenSafe = view.findViewById(R.id.swOpenSafe);
        swHttpRedirect = view.findViewById(R.id.swHttpRedirect);
        swBouncyCastle = view.findViewById(R.id.swBouncyCastle);
        swFipsMode = view.findViewById(R.id.swFipsMode);
        ibBouncyCastle = view.findViewById(R.id.ibBouncyCastle);
        btnManage = view.findViewById(R.id.btnManage);

        tvNetworkMetered = view.findViewById(R.id.tvNetworkMetered);
        tvNetworkRoaming = view.findViewById(R.id.tvNetworkRoaming);

        cardDebug = view.findViewById(R.id.cardDebug);
        btnCiphers = view.findViewById(R.id.btnCiphers);
        etHost = view.findViewById(R.id.etHost);
        rgEncryption = view.findViewById(R.id.rgEncryption);
        etPort = view.findViewById(R.id.etPort);
        btnCheck = view.findViewById(R.id.btnCheck);
        tvNetworkInfo = view.findViewById(R.id.tvNetworkInfo);

        grpValidated = view.findViewById(R.id.grpValidated);
        grpCustomDns = view.findViewById(R.id.grpCustomDns);
        grpBC = view.findViewById(R.id.grpBC);
        grpCustomSsl = view.findViewById(R.id.grpCustomSsl);

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

        swDownloadLimited.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("download_limited", checked).apply();
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
                swPreferIp6.setEnabled(!checked);
            }
        });

        swPreferIp6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("prefer_ip6", checked).apply();
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

        swDnsCustom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                DnsHelper.clear(buttonView.getContext());
                prefs.edit().putBoolean("dns_custom", checked).apply();
                tvDnsExtra.setEnabled(checked || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q);
                etDnsExtra.setEnabled(checked || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q);
            }
        });

        etDnsExtra.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                prefs.edit().putString("dns_extra", s.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        swDnsClear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                DnsHelper.clear(buttonView.getContext());
                prefs.edit().putBoolean("dns_clear", checked).apply();
            }
        });

        swTcpKeepAlive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                try {
                    prefs.edit().putBoolean("tcp_keep_alive", checked).apply();
                    if (checked)
                        System.setProperty("fairemail.tcp_keep_alive", Boolean.toString(checked));
                    else
                        System.clearProperty("fairemail.tcp_keep_alive");
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        swSslUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit().putBoolean("ssl_update", checked).commit();
                ApplicationEx.restart(v.getContext(), "ssl_update");
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

        swCertTransparency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("cert_transparency", checked).apply();
            }
        });

        ibCertTransparency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 201);
            }
        });

        swCheckNames.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("check_names", checked).apply();
            }
        });

        swOpenSafe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("open_safe", checked).apply();
            }
        });

        swHttpRedirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("http_redirect", checked).apply();
            }
        });

        swBouncyCastle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("bouncy_castle", checked).apply();
                swFipsMode.setEnabled(checked);
            }
        });

        swFipsMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("bc_fips", checked).apply();
            }
        });

        ibBouncyCastle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse("https://www.bouncycastle.org/"), true);
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

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = etHost.getText().toString().trim();
                Integer port = Helper.parseInt(etPort.getText().toString().trim());

                String encryption;
                if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_starttls)
                    encryption = "starttls";
                else if (rgEncryption.getCheckedRadioButtonId() == R.id.radio_ssl)
                    encryption = "ssl";
                else
                    encryption = "none";

                int timeout = prefs.getInt("timeout", EmailService.DEFAULT_CONNECT_TIMEOUT) * 1000;

                Bundle args = new Bundle();
                args.putString("host", host);
                args.putInt("port", port == null ? 0 : port);
                args.putString("encryption", encryption);
                args.putInt("timeout", timeout);

                new SimpleTask<StringBuilder>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnCheck.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnCheck.setEnabled(true);
                    }

                    @Override
                    protected StringBuilder onExecute(Context context, Bundle args) throws Throwable {
                        String host = args.getString("host");
                        int port = args.getInt("port");
                        String encryption = args.getString("encryption");
                        int timeout = args.getInt("timeout");

                        StringBuilder sb = new StringBuilder();
                        sb.append("Host: ").append(host).append('\n');
                        sb.append("Port: ").append(port).append('\n');
                        sb.append("Encryption: ").append(encryption).append('\n');

                        InetSocketAddress address = new InetSocketAddress(host, port);
                        SocketFactory factory = (!"ssl".equals(encryption)
                                ? SocketFactory.getDefault()
                                : SSLSocketFactory.getDefault());
                        try (Socket socket = factory.createSocket()) {
                            socket.connect(address, timeout);
                            socket.setSoTimeout(timeout);

                            if (!"none".equals(encryption)) {
                                SSLSocket sslSocket = null;
                                try {
                                    if ("starttls".equals(encryption))
                                        sslSocket = ConnectionHelper.starttls(socket, host, port, context);
                                    else
                                        sslSocket = (SSLSocket) socket;

                                    sslSocket.startHandshake();

                                    SSLSession session = sslSocket.getSession();
                                    sb.append("Protocol: ").append(session.getProtocol()).append('\n');
                                    sb.append("Cipher: ").append(session.getCipherSuite()).append('\n');
                                    Certificate[] certificates = session.getPeerCertificates();
                                    List<X509Certificate> x509certs = new ArrayList<>();
                                    if (certificates != null)
                                        for (Certificate certificate : certificates) {
                                            if (certificate instanceof X509Certificate) {
                                                X509Certificate x509 = (X509Certificate) certificate;
                                                x509certs.add(x509);
                                                sb.append("Subject: ").append(x509.getSubjectDN()).append('\n');
                                                for (String dns : EntityCertificate.getDnsNames(x509))
                                                    sb.append("DNS name: ").append(dns).append('\n');
                                            }
                                        }

                                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                                    tmf.init((KeyStore) null);

                                    TrustManager[] tms = tmf.getTrustManagers();
                                    if (tms != null && tms.length > 0 && tms[0] instanceof X509TrustManager) {
                                        X509TrustManager tm = (X509TrustManager) tms[0];
                                        try {
                                            tm.checkServerTrusted(x509certs.toArray(new X509Certificate[0]), "UNKNOWN");
                                            sb.append("Peer certificate trusted\n");
                                        } catch (Throwable ex) {
                                            sb.append(new ThrowableWrapper(ex).toSafeString()).append('\n');
                                        }
                                    }
                                } finally {
                                    try {
                                        if (sslSocket != null) {
                                            ConnectionHelper.signOff(sslSocket, port, context);
                                            sslSocket.close();
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }
                            }
                        }

                        return sb;
                    }

                    @Override
                    protected void onExecuted(Bundle args, StringBuilder sb) {
                        new AlertDialog.Builder(getContext())
                                .setIcon(R.drawable.twotone_info_24)
                                .setTitle(R.string.title_advanced_section_connection)
                                .setMessage(sb)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragment(), ex);
                    }
                }.execute(FragmentOptionsConnection.this, args, "connection:check");
            }
        });

        // Initialize
        tvNetworkMetered.setVisibility(View.GONE);
        tvNetworkRoaming.setVisibility(View.GONE);
        grpCustomDns.setVisibility(debug || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        grpBC.setVisibility(debug || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        grpCustomSsl.setVisibility(SSLHelper.customTrustManager() ? View.VISIBLE : View.GONE);
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
        if (!RESET_OPTIONS.contains(key))
            return;

        if ("timeout".equals(key))
            return;
        if ("dns_extra".equals(key))
            return;

        getMainHandler().removeCallbacks(update);
        getMainHandler().postDelayed(update, FragmentOptions.DELAY_SETOPTIONS);
    }

    private Runnable update = new RunnableEx("connection") {
        @Override
        protected void delegate() {
            setOptions();
        }
    };

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
        try {
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

            swDownloadLimited.setChecked(prefs.getBoolean("download_limited", false));

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
            swPreferIp6.setChecked(prefs.getBoolean("prefer_ip6", false));
            swPreferIp6.setEnabled(!swPreferIp4.isChecked());
            swBindSocket.setChecked(prefs.getBoolean("bind_socket", false));
            swStandaloneVpn.setChecked(prefs.getBoolean("standalone_vpn", false));
            swDnsCustom.setChecked(prefs.getBoolean("dns_custom", false));
            etDnsExtra.setText(prefs.getString("dns_extra", null));
            tvDnsExtra.setEnabled(swDnsCustom.isChecked() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q);
            etDnsExtra.setEnabled(swDnsCustom.isChecked() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q);
            swDnsClear.setChecked(prefs.getBoolean("dns_clear", false));
            swTcpKeepAlive.setChecked(prefs.getBoolean("tcp_keep_alive", false));
            swSslUpdate.setChecked(prefs.getBoolean("ssl_update", Helper.isPlayStoreInstall()));
            swSslHarden.setChecked(prefs.getBoolean("ssl_harden", false));
            swSslHardenStrict.setChecked(prefs.getBoolean("ssl_harden_strict", false));
            swSslHardenStrict.setEnabled(swSslHarden.isChecked());
            swCertStrict.setChecked(prefs.getBoolean("cert_strict", true));
            swCertTransparency.setChecked(prefs.getBoolean("cert_transparency", false));
            swCheckNames.setChecked(prefs.getBoolean("check_names", !BuildConfig.PLAY_STORE_RELEASE));
            swOpenSafe.setChecked(prefs.getBoolean("open_safe", false));
            swHttpRedirect.setChecked(prefs.getBoolean("http_redirect", true));
            swBouncyCastle.setChecked(prefs.getBoolean("bouncy_castle", false));
            swFipsMode.setChecked(prefs.getBoolean("bc_fips", false));
            swFipsMode.setEnabled(swBouncyCastle.isChecked());
        } catch (Throwable ex) {
            Log.e(ex);
        }
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
