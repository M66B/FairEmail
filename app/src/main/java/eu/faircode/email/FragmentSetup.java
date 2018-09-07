package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

public class FragmentSetup extends FragmentEx {
    private Button btnAccount;
    private TextView tvAccountDone;

    private Button btnIdentity;
    private TextView tvIdentityDone;

    private Button btnPermissions;
    private TextView tvPermissionsDone;

    private Button btnDoze;
    private TextView tvDozeDone;

    private Button btnData;

    private ToggleButton tbDarkTheme;

    private Button btnOptions;

    private Drawable check;

    private static final String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        check = getResources().getDrawable(R.drawable.baseline_check_24, getContext().getTheme());

        View view = inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls
        btnAccount = view.findViewById(R.id.btnAccount);
        tvAccountDone = view.findViewById(R.id.tvAccountDone);

        btnIdentity = view.findViewById(R.id.btnIdentity);
        tvIdentityDone = view.findViewById(R.id.tvIdentityDone);

        btnPermissions = view.findViewById(R.id.btnPermissions);
        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);

        btnDoze = view.findViewById(R.id.btnDoze);
        tvDozeDone = view.findViewById(R.id.tvDozeDone);

        btnData = view.findViewById(R.id.btnData);

        tbDarkTheme = view.findViewById(R.id.tbDarkTheme);
        btnOptions = view.findViewById(R.id.btnOptions);

        // Wire controls

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
                fragmentTransaction.commit();
            }
        });

        btnIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentIdentities()).addToBackStack("identities");
                fragmentTransaction.commit();
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPermissions.setEnabled(false);
                requestPermissions(permissions, 1);
            }
        });

        btnDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.title_setup_doze_instructions)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });

        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.N)
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String theme = prefs.getString("theme", "light");
        boolean dark = "dark".equals(theme);
        tbDarkTheme.setTag(dark);
        tbDarkTheme.setChecked(dark);
        tbDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                if (checked != (Boolean) button.getTag()) {
                    button.setTag(checked);
                    tbDarkTheme.setChecked(checked);
                    prefs.edit().putString("theme", checked ? "dark" : "light").apply();
                }
            }
        });

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentOptions()).addToBackStack("options");
                fragmentTransaction.commit();
            }
        });

        // Initialize

        tvAccountDone.setText(null);
        tvAccountDone.setCompoundDrawables(null, null, null, null);

        btnIdentity.setEnabled(false);
        tvIdentityDone.setText(null);
        tvIdentityDone.setCompoundDrawables(null, null, null, null);

        tvPermissionsDone.setText(null);
        tvPermissionsDone.setCompoundDrawables(null, null, null, null);

        btnDoze.setEnabled(false);
        tvDozeDone.setText(null);
        tvDozeDone.setCompoundDrawables(null, null, null, null);

        btnData.setVisibility(View.GONE);

        int[] grantResults = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++)
            grantResults[i] = ContextCompat.checkSelfPermission(getActivity(), permissions[i]);

        onRequestPermissionsResult(0, permissions, grantResults);

        // Create outbox
        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox == null) {
                        outbox = new EntityFolder();
                        outbox.name = "OUTBOX";
                        outbox.type = EntityFolder.OUTBOX;
                        outbox.synchronize = false;
                        outbox.after = 0;
                        outbox.id = db.folder().insertFolder(outbox);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, new Bundle());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DB db = DB.getInstance(getContext());

        db.account().liveAccounts(true).observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                boolean done = (accounts != null && accounts.size() > 0);
                btnIdentity.setEnabled(done);
                tvAccountDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvAccountDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
            }
        });

        db.identity().liveIdentities(true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
            @Override
            public void onChanged(@Nullable List<EntityIdentity> identities) {
                boolean done = (identities != null && identities.size() > 0);
                tvIdentityDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvIdentityDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        PowerManager pm = getContext().getSystemService(PowerManager.class);
        boolean ignoring = pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
        btnDoze.setEnabled(!ignoring);
        tvDozeDone.setText(ignoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(ignoring ? check : null, null, null, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = getContext().getSystemService(ConnectivityManager.class);
            boolean saving = (cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED);
            btnData.setVisibility(saving ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean has = (grantResults.length > 0);
        for (int result : grantResults)
            if (result != PackageManager.PERMISSION_GRANTED) {
                has = false;
                break;
            }

        btnPermissions.setEnabled(!has);
        tvPermissionsDone.setText(has ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(has ? check : null, null, null, null);
    }
}
