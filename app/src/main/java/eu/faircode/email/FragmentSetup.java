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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class FragmentSetup extends FragmentEx {
    private ViewGroup view;

    private Button btnQuick;

    private Button btnAccount;
    private TextView tvAccountDone;
    private TextView tvNoPrimaryDrafts;

    private Button btnIdentity;
    private TextView tvIdentityDone;

    private Button btnPermissions;
    private TextView tvPermissionsDone;

    private Button btnDoze;
    private TextView tvDozeDone;

    private Button btnData;

    private int textColorPrimary;
    private int colorWarning;
    private Drawable check;

    private static final String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        textColorPrimary = Helper.resolveColor(getContext(), android.R.attr.textColorPrimary);
        colorWarning = Helper.resolveColor(getContext(), R.attr.colorWarning);
        check = getResources().getDrawable(R.drawable.baseline_check_24, getContext().getTheme());

        view = (ViewGroup) inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls
        btnQuick = view.findViewById(R.id.btnQuick);

        btnAccount = view.findViewById(R.id.btnAccount);
        tvAccountDone = view.findViewById(R.id.tvAccountDone);
        tvNoPrimaryDrafts = view.findViewById(R.id.tvNoPrimaryDrafts);

        btnIdentity = view.findViewById(R.id.btnIdentity);
        tvIdentityDone = view.findViewById(R.id.tvIdentityDone);

        btnPermissions = view.findViewById(R.id.btnPermissions);
        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);

        btnDoze = view.findViewById(R.id.btnDoze);
        tvDozeDone = view.findViewById(R.id.tvDozeDone);

        btnData = view.findViewById(R.id.btnData);

        // Wire controls

        btnQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentQuickSetup()).addToBackStack("quick");
                fragmentTransaction.commit();
            }
        });

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
                requestPermissions(permissions, ActivitySetup.REQUEST_PERMISSION);
            }
        });

        btnDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setMessage(R.string.title_setup_doze_instructions)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                                } catch (Throwable ex) {
                                    Log.e(ex);
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
                    Log.e(ex);
                }
            }
        });

        // Initialize
        tvAccountDone.setText(null);
        tvAccountDone.setCompoundDrawables(null, null, null, null);
        tvNoPrimaryDrafts.setVisibility(View.GONE);

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

        checkPermissions(permissions, grantResults, true);

        // Create outbox
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox == null) {
                        outbox = new EntityFolder();
                        outbox.name = "OUTBOX";
                        outbox.type = EntityFolder.OUTBOX;
                        outbox.level = 0;
                        outbox.synchronize = false;
                        outbox.sync_days = 0;
                        outbox.keep_days = 0;
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
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, new Bundle(), "outbox:create");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());

        db.account().liveAccounts(true).observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            private boolean done = false;
            private LiveData<EntityFolder> livePrimaryDrafts = null;
            private LiveData<EntityFolder> livePrimaryArchive = null;

            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                done = (accounts != null && accounts.size() > 0);

                btnIdentity.setEnabled(done);
                tvAccountDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvAccountDone.setTextColor(done ? textColorPrimary : colorWarning);
                tvAccountDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);

                if (livePrimaryDrafts == null)
                    livePrimaryDrafts = db.folder().livePrimaryDrafts();
                else
                    livePrimaryDrafts.removeObservers(getViewLifecycleOwner());

                if (livePrimaryArchive == null)
                    livePrimaryArchive = db.folder().livePrimaryArchive();
                else
                    livePrimaryArchive.removeObservers(getViewLifecycleOwner());

                livePrimaryDrafts.observe(getViewLifecycleOwner(), new Observer<EntityFolder>() {
                    @Override
                    public void onChanged(EntityFolder drafts) {
                        tvNoPrimaryDrafts.setVisibility(done && drafts == null ? View.VISIBLE : View.GONE);
                    }
                });

                livePrimaryArchive.observe(getViewLifecycleOwner(), new Observer<EntityFolder>() {
                    @Override
                    public void onChanged(EntityFolder archive) {
                        PackageManager pm = getContext().getPackageManager();
                        pm.setComponentEnabledSetting(
                                new ComponentName(getContext(), ActivitySearch.class),
                                archive == null
                                        ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                                        : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                });
            }
        });

        db.identity().liveIdentities(null, true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
            @Override
            public void onChanged(@Nullable List<EntityIdentity> identities) {
                boolean done = (identities != null && identities.size() > 0);
                tvIdentityDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvIdentityDone.setTextColor(done ? textColorPrimary : colorWarning);
                tvIdentityDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean ignoring = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                ignoring = pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
            }
        }
        btnDoze.setEnabled(!ignoring);
        tvDozeDone.setText(ignoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setTextColor(ignoring ? textColorPrimary : colorWarning);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(ignoring ? check : null, null, null, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean saving = (cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED);
            btnData.setVisibility(saving ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ActivitySetup.REQUEST_PERMISSION)
            checkPermissions(permissions, grantResults, false);
    }

    private void checkPermissions(String[] permissions, @NonNull int[] grantResults, boolean init) {
        boolean has = (grantResults.length > 0);
        for (int result : grantResults)
            if (result != PackageManager.PERMISSION_GRANTED) {
                has = false;
                break;
            }

        btnPermissions.setEnabled(!has);
        tvPermissionsDone.setText(has ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setTextColor(has ? textColorPrimary : colorWarning);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(has ? check : null, null, null, null);

        if (has && !init)
            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);
                    for (EntityFolder folder : db.folder().getFoldersSynchronizing())
                        EntityOperation.sync(db, folder.id);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(FragmentSetup.this, new Bundle(), "setup:sync");
    }
}
