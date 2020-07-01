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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.security.KeyChain;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class FragmentOptionsEncryption extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swSign;
    private SwitchCompat swEncrypt;
    private SwitchCompat swAutoDecrypt;

    private Spinner spOpenPgp;
    private TextView tvOpenPgpStatus;
    private SwitchCompat swAutocrypt;
    private SwitchCompat swAutocryptMutual;

    private SwitchCompat swCheckCertificate;
    private Button btnManageCertificates;
    private Button btnImportKey;
    private Button btnManageKeys;
    private Button btnCa;
    private TextView tvKeySize;

    private OpenPgpServiceConnection pgpService;
    private List<String> openPgpProvider = new ArrayList<>();

    private final static String[] RESET_OPTIONS = new String[]{
            "sign_default", "encrypt_default", "auto_decrypt",
            "openpgp_provider", "autocrypt", "autocrypt_mutual",
            "check_certificate"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        PackageManager pm = getContext().getPackageManager();
        View view = inflater.inflate(R.layout.fragment_options_encryption, container, false);

        // Get controls

        swSign = view.findViewById(R.id.swSign);
        swEncrypt = view.findViewById(R.id.swEncrypt);
        swAutoDecrypt = view.findViewById(R.id.swAutoDecrypt);

        spOpenPgp = view.findViewById(R.id.spOpenPgp);
        tvOpenPgpStatus = view.findViewById(R.id.tvOpenPgpStatus);
        swAutocrypt = view.findViewById(R.id.swAutocrypt);
        swAutocryptMutual = view.findViewById(R.id.swAutocryptMutual);

        swCheckCertificate = view.findViewById(R.id.swCheckCertificate);
        btnManageCertificates = view.findViewById(R.id.btnManageCertificates);
        btnImportKey = view.findViewById(R.id.btnImportKey);
        btnManageKeys = view.findViewById(R.id.btnManageKeys);
        btnCa = view.findViewById(R.id.btnCa);
        tvKeySize = view.findViewById(R.id.tvKeySize);

        Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
        List<ResolveInfo> ris = pm.queryIntentServices(intent, 0); // package whitelisted
        for (ResolveInfo ri : ris)
            if (ri.serviceInfo != null)
                openPgpProvider.add(ri.serviceInfo.packageName);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.addAll(openPgpProvider);
        spOpenPgp.setAdapter(adapter);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swSign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sign_default", checked).apply();
            }
        });

        swEncrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("encrypt_default", checked).apply();
                swSign.setEnabled(!checked);
            }
        });

        swAutoDecrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_decrypt", checked).apply();
            }
        });

        // PGP

        spOpenPgp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String pkg = openPgpProvider.get(position);
                prefs.edit().putString("openpgp_provider", pkg).apply();

                String tag = (String) spOpenPgp.getTag();
                if (tag != null && !tag.equals(pkg)) {
                    spOpenPgp.setTag(pkg);
                    testOpenPgp(pkg);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("openpgp_provider").apply();
            }
        });

        swAutocrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autocrypt", checked).apply();
                swAutocryptMutual.setEnabled(checked);
            }
        });

        swAutocryptMutual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autocrypt_mutual", checked).apply();
            }
        });

        // S/MIME

        swCheckCertificate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("check_certificate", checked).apply();
            }
        });

        btnManageCertificates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_MANAGE_CERTIFICATES));
            }
        });

        final Intent importKey = KeyChain.createInstallIntent();
        btnImportKey.setEnabled(importKey.resolveActivity(pm) != null); // system whitelisted
        btnImportKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(importKey);
            }
        });

        final Intent security = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        btnImportKey.setEnabled(security.resolveActivity(pm) != null); // system whitelisted
        btnManageKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(security);
            }
        });

        btnCa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<List<String>>() {
                    @Override
                    protected List<String> onExecute(Context context, Bundle args) throws Throwable {
                        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
                        ks.load(null, null);

                        List<String> issuers = new ArrayList<>();
                        Enumeration<String> aliases = ks.aliases();
                        while (aliases.hasMoreElements()) {
                            String alias = aliases.nextElement();
                            Certificate kcert = ks.getCertificate(alias);
                            if (kcert instanceof X509Certificate) {
                                Principal issuer = ((X509Certificate) kcert).getIssuerDN();
                                if (issuer != null) {
                                    String name = issuer.getName();
                                    if (name != null)
                                        issuers.add(name);
                                }
                            }
                        }

                        Collections.sort(issuers);
                        return issuers;
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<String> issuers) {
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.title_advanced_ca)
                                .setMessage(TextUtils.join("\r\n", issuers))
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
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsEncryption.this, new Bundle(), "ca");
            }
        });

        try {
            int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
            tvKeySize.setText(getString(R.string.title_advanced_aes_key_size, maxKeySize));
        } catch (NoSuchAlgorithmException ex) {
            tvKeySize.setText(Log.formatThrowable(ex));
        }

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);

        if (pgpService != null && pgpService.isBound()) {
            Log.i("PGP unbinding");
            pgpService.unbindFromService();
        }
        pgpService = null;

        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            setOptions();
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
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swSign.setChecked(prefs.getBoolean("sign_default", false));
        swEncrypt.setChecked(prefs.getBoolean("encrypt_default", false));
        swSign.setEnabled(!swEncrypt.isChecked());
        swAutoDecrypt.setChecked(prefs.getBoolean("auto_decrypt", false));

        String provider = prefs.getString("openpgp_provider", "org.sufficientlysecure.keychain");
        spOpenPgp.setTag(provider);
        for (int pos = 0; pos < openPgpProvider.size(); pos++)
            if (provider.equals(openPgpProvider.get(pos))) {
                spOpenPgp.setSelection(pos);
                break;
            }
        testOpenPgp(provider);

        swAutocrypt.setChecked(prefs.getBoolean("autocrypt", true));
        swAutocryptMutual.setChecked(prefs.getBoolean("autocrypt_mutual", true));
        swAutocryptMutual.setEnabled(swAutocrypt.isChecked());

        swCheckCertificate.setChecked(prefs.getBoolean("check_certificate", true));
    }

    private void testOpenPgp(String pkg) {
        if (pgpService != null && pgpService.isBound())
            pgpService.unbindFromService();

        tvOpenPgpStatus.setText("PGP binding to " + pkg);
        pgpService = new OpenPgpServiceConnection(getContext(), pkg, new OpenPgpServiceConnection.OnBound() {
            @Override
            public void onBound(IOpenPgpService2 service) {
                tvOpenPgpStatus.setText("PGP bound to " + pkg);
            }

            @Override
            public void onError(Exception ex) {
                if ("bindService() returned false!".equals(ex.getMessage()))
                    tvOpenPgpStatus.setText(ex.getMessage());
                else
                    tvOpenPgpStatus.setText(ex.toString());
            }
        });
        pgpService.bindToService();
    }
}
