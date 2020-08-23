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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FragmentOptionsPrivacy extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swConfirmLinks;
    private SwitchCompat swBrowseLinks;
    private SwitchCompat swConfirmImages;
    private SwitchCompat swConfirmHtml;
    private SwitchCompat swDisableTracking;
    private SwitchCompat swHideTimeZone;
    private Button btnPin;
    private Button btnBiometrics;
    private Spinner spBiometricsTimeout;
    private SwitchCompat swDisplayHidden;
    private SwitchCompat swSecure;
    private SwitchCompat swSafeBrowsing;
    private ImageButton ibSafeBrowsing;
    private ImageButton ibDisconnectBlacklist;
    private Button btnDisconnectBlacklist;
    private TextView tvDisconnectBlacklistTime;
    private SwitchCompat swDisconnectLinks;
    private SwitchCompat swDisconnectImages;

    private Group grpSafeBrowsing;

    private final static String[] RESET_OPTIONS = new String[]{
            "confirm_links", "browse_links", "confirm_images", "confirm_html",
            "disable_tracking", "hide_timezone",
            "pin", "biometrics", "biometrics_timeout",
            "display_hidden", "secure", "safe_browsing",
            "disconnect_links", "disconnect_images"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_privacy, container, false);

        // Get controls

        swConfirmLinks = view.findViewById(R.id.swConfirmLinks);
        swBrowseLinks = view.findViewById(R.id.swBrowseLinks);
        swConfirmImages = view.findViewById(R.id.swConfirmImages);
        swConfirmHtml = view.findViewById(R.id.swConfirmHtml);
        swDisableTracking = view.findViewById(R.id.swDisableTracking);
        swHideTimeZone = view.findViewById(R.id.swHideTimeZone);
        btnPin = view.findViewById(R.id.btnPin);
        btnBiometrics = view.findViewById(R.id.btnBiometrics);
        spBiometricsTimeout = view.findViewById(R.id.spBiometricsTimeout);
        swDisplayHidden = view.findViewById(R.id.swDisplayHidden);
        swSecure = view.findViewById(R.id.swSecure);
        swSafeBrowsing = view.findViewById(R.id.swSafeBrowsing);
        ibSafeBrowsing = view.findViewById(R.id.ibSafeBrowsing);
        ibDisconnectBlacklist = view.findViewById(R.id.ibDisconnectBlacklist);
        btnDisconnectBlacklist = view.findViewById(R.id.btnDisconnectBlacklist);
        tvDisconnectBlacklistTime = view.findViewById(R.id.tvDisconnectBlacklistTime);
        swDisconnectLinks = view.findViewById(R.id.swDisconnectLinks);
        swDisconnectImages = view.findViewById(R.id.swDisconnectImages);

        grpSafeBrowsing = view.findViewById(R.id.grpSafeBrowsing);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swConfirmLinks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("confirm_links", checked).apply();
                swBrowseLinks.setEnabled(!checked);
            }
        });

        swBrowseLinks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("browse_links", checked).apply();
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

        swHideTimeZone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("hide_timezone", checked).apply();
            }
        });

        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogPin fragment = new FragmentDialogPin();
                fragment.show(getParentFragmentManager(), "pin");
            }
        });

        btnBiometrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean biometrics = prefs.getBoolean("biometrics", false);

                Helper.authenticate(getActivity(), getViewLifecycleOwner(), biometrics, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean pro = ActivityBilling.isPro(getContext());
                            if (pro) {
                                prefs.edit().putBoolean("biometrics", !biometrics).apply();
                                btnBiometrics.setText(!biometrics
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

        swDisplayHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("display_hidden", checked).apply();
            }
        });

        swSecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("secure", checked).commit(); // apply won't work here
                restart();
            }
        });

        swSafeBrowsing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("safe_browsing", checked).apply();
            }
        });

        ibSafeBrowsing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(getContext(), Uri.parse("https://developers.google.com/safe-browsing"), true);
            }
        });

        grpSafeBrowsing.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? View.GONE : View.VISIBLE);

        ibDisconnectBlacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 159);
            }
        });

        btnDisconnectBlacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SimpleTask<Void>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnDisconnectBlacklist.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnDisconnectBlacklist.setEnabled(true);
                    }

                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        DisconnectBlacklist.download(context);
                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        setOptions();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex, !(ex instanceof IOException));
                    }
                }.execute(FragmentOptionsPrivacy.this, new Bundle(), "disconnect");
            }
        });

        swDisconnectLinks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("disconnect_links", checked).apply();
            }
        });

        swDisconnectImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("disconnect_images", checked).apply();
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

        swConfirmLinks.setChecked(prefs.getBoolean("confirm_links", true));
        swBrowseLinks.setChecked(prefs.getBoolean("browse_links", false));
        swBrowseLinks.setEnabled(!swConfirmLinks.isChecked());
        swConfirmImages.setChecked(prefs.getBoolean("confirm_images", true));
        swConfirmHtml.setChecked(prefs.getBoolean("confirm_html", true));
        swDisableTracking.setChecked(prefs.getBoolean("disable_tracking", true));
        swHideTimeZone.setChecked(prefs.getBoolean("hide_timezone", true));

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

        swDisplayHidden.setChecked(prefs.getBoolean("display_hidden", false));
        swSecure.setChecked(prefs.getBoolean("secure", false));
        swSafeBrowsing.setChecked(prefs.getBoolean("safe_browsing", false));

        long time = prefs.getLong("disconnect_last", -1);
        DateFormat DF = SimpleDateFormat.getDateTimeInstance();
        tvDisconnectBlacklistTime.setText(time < 0 ? null : DF.format(time));
        tvDisconnectBlacklistTime.setVisibility(time < 0 ? View.GONE : View.VISIBLE);

        swDisconnectLinks.setChecked(prefs.getBoolean("disconnect_links", true));
        swDisconnectImages.setChecked(prefs.getBoolean("disconnect_images", false));
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

            ApplicationEx.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    etPin.requestFocus();
                }
            });

            return dialog;
        }
    }
}
