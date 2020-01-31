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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.security.KeyChain;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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

import org.openintents.openpgp.util.OpenPgpApi;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FragmentOptionsPrivacy extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swConfirmLinks;
    private SwitchCompat swConfirmImages;
    private SwitchCompat swConfirmHtml;
    private SwitchCompat swDisableTracking;
    private SwitchCompat swDisplayHidden;
    private Spinner spEncryptMethod;
    private Spinner spOpenPgp;
    private SwitchCompat swAutocrypt;
    private SwitchCompat swAutocryptMutual;
    private SwitchCompat swSign;
    private SwitchCompat swEncrypt;
    private SwitchCompat swAutoDecrypt;
    private SwitchCompat swSecure;
    private Button btnBiometrics;
    private Button btnPin;
    private Spinner spBiometricsTimeout;
    private Button btnManageCertificates;
    private Button btnImportKey;
    private Button btnManageKeys;
    private TextView tvKeySize;

    private List<String> openPgpProvider = new ArrayList<>();

    private final static String[] RESET_OPTIONS = new String[]{
            "confirm_links", "confirm_images", "confirm_html",
            "disable_tracking", "display_hidden",
            "default_encrypt_method", "openpgp_provider", "autocrypt", "autocrypt_mutual",
            "sign_default", "encrypt_default", "auto_decrypt",
            "secure",
            "biometrics", "pin", "biometrics_timeout"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        PackageManager pm = getContext().getPackageManager();
        View view = inflater.inflate(R.layout.fragment_options_privacy, container, false);

        // Get controls

        swConfirmLinks = view.findViewById(R.id.swConfirmLinks);
        swConfirmImages = view.findViewById(R.id.swConfirmImages);
        swConfirmHtml = view.findViewById(R.id.swConfirmHtml);
        swDisableTracking = view.findViewById(R.id.swDisableTracking);
        swDisplayHidden = view.findViewById(R.id.swDisplayHidden);
        spEncryptMethod = view.findViewById(R.id.spEncryptMethod);
        spOpenPgp = view.findViewById(R.id.spOpenPgp);
        swAutocrypt = view.findViewById(R.id.swAutocrypt);
        swAutocryptMutual = view.findViewById(R.id.swAutocryptMutual);
        swSign = view.findViewById(R.id.swSign);
        swEncrypt = view.findViewById(R.id.swEncrypt);
        swAutoDecrypt = view.findViewById(R.id.swAutoDecrypt);
        swSecure = view.findViewById(R.id.swSecure);
        btnBiometrics = view.findViewById(R.id.btnBiometrics);
        btnPin = view.findViewById(R.id.btnPin);
        spBiometricsTimeout = view.findViewById(R.id.spBiometricsTimeout);
        btnManageCertificates = view.findViewById(R.id.btnManageCertificates);
        btnImportKey = view.findViewById(R.id.btnImportKey);
        btnManageKeys = view.findViewById(R.id.btnManageKeys);
        tvKeySize = view.findViewById(R.id.tvKeySize);

        Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
        List<ResolveInfo> ris = pm.queryIntentServices(intent, 0);
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

        swConfirmLinks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("confirm_links", checked).apply();
            }
        });

        swConfirmImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("confirm_images", checked).apply();
            }
        });

        swConfirmHtml.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("confirm_html", checked).apply();
            }
        });

        swDisableTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("disable_tracking", checked).apply();
            }
        });

        swDisplayHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("display_hidden", checked).apply();
            }
        });

        spEncryptMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1)
                    prefs.edit().putString("default_encrypt_method", "s/mime").apply();
                else
                    onNothingSelected(parent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("default_encrypt_method").apply();
            }
        });

        spOpenPgp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prefs.edit().putString("openpgp_provider", openPgpProvider.get(position)).apply();
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

        swSecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("secure", checked).commit(); // apply won't work here
                restart();
            }
        });

        btnBiometrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean biometrics = prefs.getBoolean("biometrics", false);

                Helper.authenticate(getActivity(), biometrics, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean pro = ActivityBilling.isPro(getContext());
                            if (pro) {
                                prefs.edit().putBoolean("biometrics", !biometrics).apply();
                                btnBiometrics.setText(biometrics
                                        ? R.string.title_setup_biometrics_disable
                                        : R.string.title_setup_biometrics_enable);
                            } else
                                startActivity(new Intent(getContext(), ActivityBilling.class));
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        // Do nothing
                    }
                });
            }
        });

        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogPin fragment = new FragmentDialogPin();
                fragment.show(getParentFragmentManager(), "pin");
            }
        });

        spBiometricsTimeout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.biometricsTimeoutValues);
                prefs.edit().putInt("biometrics_timeout", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("biometrics_timeout").apply();
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
        btnImportKey.setEnabled(importKey.resolveActivity(pm) != null);
        btnImportKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(importKey);
            }
        });

        final Intent security = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        btnImportKey.setEnabled(security.resolveActivity(pm) != null);
        btnManageKeys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(security);
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

        swConfirmLinks.setChecked(prefs.getBoolean("confirm_links", true));
        swConfirmImages.setChecked(prefs.getBoolean("confirm_images", true));
        swConfirmHtml.setChecked(prefs.getBoolean("confirm_html", true));
        swDisableTracking.setChecked(prefs.getBoolean("disable_tracking", true));
        swDisplayHidden.setChecked(prefs.getBoolean("display_hidden", false));

        String encrypt_method = prefs.getString("default_encrypt_method", "pgp");
        if ("s/mime".equals(encrypt_method))
            spEncryptMethod.setSelection(1);

        String provider = prefs.getString("openpgp_provider", "org.sufficientlysecure.keychain");
        for (int pos = 0; pos < openPgpProvider.size(); pos++)
            if (provider.equals(openPgpProvider.get(pos))) {
                spOpenPgp.setSelection(pos);
                break;
            }

        swAutocrypt.setChecked(prefs.getBoolean("autocrypt", true));
        swAutocryptMutual.setChecked(prefs.getBoolean("autocrypt_mutual", true));
        swAutocryptMutual.setEnabled(swAutocrypt.isChecked());
        swSign.setChecked(prefs.getBoolean("sign_default", false));
        swEncrypt.setChecked(prefs.getBoolean("encrypt_default", false));
        swSign.setEnabled(!swEncrypt.isChecked());
        swAutoDecrypt.setChecked(prefs.getBoolean("auto_decrypt", false));
        swSecure.setChecked(prefs.getBoolean("secure", false));

        boolean biometrics = prefs.getBoolean("biometrics", false);
        btnBiometrics.setText(biometrics
                ? R.string.title_setup_biometrics_disable
                : R.string.title_setup_biometrics_enable);
        btnBiometrics.setEnabled(Helper.canAuthenticate(getContext()));

        int biometrics_timeout = prefs.getInt("biometrics_timeout", 2);
        int[] biometricTimeoutValues = getResources().getIntArray(R.array.biometricsTimeoutValues);
        for (int pos = 0; pos < biometricTimeoutValues.length; pos++)
            if (biometricTimeoutValues[pos] == biometrics_timeout) {
                spBiometricsTimeout.setSelection(pos);
                break;
            }
    }

    public static class FragmentDialogPin extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pin_set, null);
            final EditText etPin = dview.findViewById(R.id.etPin);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pin = etPin.getText().toString();
                            if (TextUtils.isEmpty(pin))
                                prefs.edit().remove("pin").apply();
                            else {
                                boolean pro = ActivityBilling.isPro(getContext());
                                if (pro) {
                                    Helper.setAuthenticated(getContext());
                                    prefs.edit().putString("pin", pin).apply();
                                } else
                                    startActivity(new Intent(getContext(), ActivityBilling.class));
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            String pin = prefs.getString("pin", null);
            if (!TextUtils.isEmpty(pin))
                builder.setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().remove("pin").apply();
                    }
                });

            final Dialog dialog = builder.create();

            etPin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                        return true;
                    } else
                        return false;
                }
            });

            etPin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    etPin.requestFocus();
                }
            });

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    etPin.requestFocus();
                }
            });

            return dialog;
        }
    }
}
