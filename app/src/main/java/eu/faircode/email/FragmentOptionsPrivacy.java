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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.preference.PreferenceManager;

import org.openintents.openpgp.util.OpenPgpApi;

import java.util.ArrayList;
import java.util.List;

public class FragmentOptionsPrivacy extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swDisableTracking;
    private SwitchCompat swDisplayHidden;
    private Spinner spOpenPgp;
    private SwitchCompat swEncrypt;
    private SwitchCompat swAutoDecrypt;
    private SwitchCompat swNoHistory;
    private Button btnBiometrics;
    private Spinner spBiometricsTimeout;
    private Button btnPin;

    private List<String> openPgpProvider = new ArrayList<>();

    private final static String[] RESET_OPTIONS = new String[]{
            "disable_tracking", "display_hidden",
            "openpgp_provider", "encrypt_default", "auto_decrypt",
            "no_history",
            "biometrics", "pin", "biometrics_timeout"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_privacy, container, false);

        // Get controls

        swDisableTracking = view.findViewById(R.id.swDisableTracking);
        swDisplayHidden = view.findViewById(R.id.swDisplayHidden);
        spOpenPgp = view.findViewById(R.id.spOpenPgp);
        swEncrypt = view.findViewById(R.id.swEncrypt);
        swAutoDecrypt = view.findViewById(R.id.swAutoDecrypt);
        swNoHistory = view.findViewById(R.id.swNoHistory);
        btnBiometrics = view.findViewById(R.id.btnBiometrics);
        spBiometricsTimeout = view.findViewById(R.id.spBiometricsTimeout);
        btnPin = view.findViewById(R.id.btnPin);

        Intent intent = new Intent(OpenPgpApi.SERVICE_INTENT_2);
        List<ResolveInfo> ris = getContext().getPackageManager().queryIntentServices(intent, 0);
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

        swEncrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("encrypt_default", checked).apply();
            }
        });

        swAutoDecrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_decrypt", checked).apply();
            }
        });

        swNoHistory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("no_history", checked).commit(); // apply won't work here
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
                        boolean pro = ActivityBilling.isPro(getContext());
                        if (pro) {
                            prefs.edit().putBoolean("biometrics", !biometrics).apply();
                            btnBiometrics.setText(biometrics
                                    ? R.string.title_setup_biometrics_disable
                                    : R.string.title_setup_biometrics_enable);
                        } else
                            startActivity(new Intent(getContext(), ActivityBilling.class));
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

        swDisableTracking.setChecked(prefs.getBoolean("disable_tracking", true));
        swDisplayHidden.setChecked(prefs.getBoolean("display_hidden", false));

        String provider = prefs.getString("openpgp_provider", "org.sufficientlysecure.keychain");
        for (int pos = 0; pos < openPgpProvider.size(); pos++)
            if (provider.equals(openPgpProvider.get(pos))) {
                spOpenPgp.setSelection(pos);
                break;
            }

        swEncrypt.setChecked(prefs.getBoolean("encrypt_default", false));
        swAutoDecrypt.setChecked(prefs.getBoolean("auto_decrypt", false));
        swNoHistory.setChecked(prefs.getBoolean("no_history", false));

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
