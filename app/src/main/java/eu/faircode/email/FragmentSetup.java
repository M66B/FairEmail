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

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.util.List;

public class FragmentSetup extends FragmentBase {
    private ViewGroup view;

    private TextView tvWelcome;
    private ImageButton ibWelcome;

    private Button btnHelp;
    private Button btnQuick;
    private TextView tvQuickRemark;

    private TextView tvAccountDone;
    private Button btnAccount;
    private TextView tvNoPrimaryDrafts;

    private TextView tvIdentityDone;
    private Button btnIdentity;
    private TextView tvNoIdentities;

    private TextView tvPermissionsDone;
    private Button btnPermissions;

    private TextView tvDozeDone;
    private Button btnDoze;
    private TextView tvBatteryUsage;
    private TextView tvSyncStopped;

    private Button btnDataSaver;

    private Button btnInbox;

    private Group grpWelcome;
    private Group grpDataSaver;

    private int textColorPrimary;
    private int colorWarning;
    private Drawable check;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        textColorPrimary = Helper.resolveColor(getContext(), android.R.attr.textColorPrimary);
        colorWarning = Helper.resolveColor(getContext(), R.attr.colorWarning);
        check = getResources().getDrawable(R.drawable.baseline_check_24, getContext().getTheme());

        view = (ViewGroup) inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls
        tvWelcome = view.findViewById(R.id.tvWelcome);
        ibWelcome = view.findViewById(R.id.ibWelcome);

        btnHelp = view.findViewById(R.id.btnHelp);
        btnQuick = view.findViewById(R.id.btnQuick);
        tvQuickRemark = view.findViewById(R.id.tvQuickRemark);

        tvAccountDone = view.findViewById(R.id.tvAccountDone);
        btnAccount = view.findViewById(R.id.btnAccount);
        tvNoPrimaryDrafts = view.findViewById(R.id.tvNoPrimaryDrafts);

        tvIdentityDone = view.findViewById(R.id.tvIdentityDone);
        btnIdentity = view.findViewById(R.id.btnIdentity);
        tvNoIdentities = view.findViewById(R.id.tvNoIdentities);

        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);
        btnPermissions = view.findViewById(R.id.btnPermissions);

        tvDozeDone = view.findViewById(R.id.tvDozeDone);
        btnDoze = view.findViewById(R.id.btnDoze);
        tvBatteryUsage = view.findViewById(R.id.tvBatteryUsage);
        tvSyncStopped = view.findViewById(R.id.tvSyncStopped);

        btnDataSaver = view.findViewById(R.id.btnDataSaver);

        btnInbox = view.findViewById(R.id.btnInbox);

        grpWelcome = view.findViewById(R.id.grpWelcome);
        grpDataSaver = view.findViewById(R.id.grpDataSaver);

        PackageManager pm = getContext().getPackageManager();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Wire controls

        tvWelcome.setText(getString(R.string.title_setup_welcome)
                .replaceAll("^\\s+", "").replaceAll("\\s+", " "));

        ibWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit().putBoolean("welcome", false).apply();
                grpWelcome.setVisibility(View.GONE);
            }
        });

        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("name", "SETUP.md");
                FragmentDialogMarkdown fragment = new FragmentDialogMarkdown();
                fragment.setArguments(args);
                fragment.show(getChildFragmentManager(), "help");
            }
        });

        btnQuick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), btnQuick);

                int order = 1;
                popupMenu.getMenu().add(Menu.NONE, R.string.title_setup_gmail, order++, R.string.title_setup_gmail);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_setup_outlook, order++, R.string.title_setup_outlook);

                if (Helper.hasValidFingerprint(getContext()) || BuildConfig.DEBUG)
                    for (EmailProvider provider : EmailProvider.loadProfiles(getContext()))
                        if (provider.oauth != null && provider.oauth.enabled)
                            popupMenu.getMenu()
                                    .add(Menu.NONE, -1, order++, getString(R.string.title_setup_oauth, provider.name))
                                    .setIntent(new Intent(ActivitySetup.ACTION_QUICK_OAUTH)
                                            .putExtra("id", provider.id)
                                            .putExtra("name", provider.name)
                                            .putExtra("askAccount", provider.oauth.askAccount));

                //popupMenu.getMenu().add(Menu.NONE, R.string.title_setup_activesync, order++, R.string.title_setup_activesync);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_setup_other, order++, R.string.title_setup_other);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                        switch (item.getItemId()) {
                            case R.string.title_setup_gmail:
                                if (Helper.hasValidFingerprint(getContext()) || BuildConfig.DEBUG)
                                    lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_QUICK_GMAIL));
                                else
                                    ToastEx.makeText(getContext(), R.string.title_setup_gmail_support, Toast.LENGTH_LONG).show();
                                return true;
                            case R.string.title_setup_activesync:
                                Helper.viewFAQ(getContext(), 133);
                                return true;
                            case R.string.title_setup_outlook:
                            case R.string.title_setup_other:
                                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_QUICK_SETUP));
                                return true;
                            default:
                                if (item.getIntent() == null)
                                    return false;
                                else {
                                    lbm.sendBroadcast(item.getIntent());
                                    return true;
                                }
                        }
                    }
                });

                popupMenu.show();
            }
        });

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_VIEW_ACCOUNTS));
            }
        });

        btnIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_VIEW_IDENTITIES));
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPermissions.setEnabled(false);
                String permission = Manifest.permission.READ_CONTACTS;
                requestPermissions(new String[]{permission}, ActivitySetup.REQUEST_PERMISSION);
            }
        });

        btnDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentDialogDoze().show(getParentFragmentManager(), "setup:doze");
            }
        });

        tvBatteryUsage.setPaintFlags(tvBatteryUsage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvBatteryUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 39);
            }
        });

        tvSyncStopped.setPaintFlags(tvSyncStopped.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvSyncStopped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(getContext(), 16);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Intent settings = new Intent(
                    Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID));

            btnDataSaver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(settings);
                }
            });
            btnDataSaver.setEnabled(settings.resolveActivity(pm) != null);
        }

        btnInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentBase) getParentFragment()).finish();
            }
        });

        // Initialize
        tvQuickRemark.setVisibility(View.GONE);

        tvAccountDone.setText(null);
        tvAccountDone.setCompoundDrawables(null, null, null, null);
        tvNoPrimaryDrafts.setVisibility(View.GONE);

        tvIdentityDone.setText(null);
        tvIdentityDone.setCompoundDrawables(null, null, null, null);
        btnIdentity.setEnabled(false);
        tvNoIdentities.setVisibility(View.GONE);

        tvPermissionsDone.setText(null);
        tvPermissionsDone.setCompoundDrawables(null, null, null, null);

        tvDozeDone.setText(null);
        tvDozeDone.setCompoundDrawables(null, null, null, null);
        btnDoze.setEnabled(false);

        btnInbox.setEnabled(false);

        boolean welcome = prefs.getBoolean("welcome", true);
        grpWelcome.setVisibility(welcome ? View.VISIBLE : View.GONE);
        grpDataSaver.setVisibility(View.GONE);

        setContactsPermission(hasPermission(Manifest.permission.READ_CONTACTS));

        // Create outbox
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox == null) {
                        outbox = EntityFolder.getOutbox();
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "outbox:create");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        db.account().liveSynchronizingAccounts().observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            private boolean done = false;

            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                done = (accounts != null && accounts.size() > 0);

                getActivity().invalidateOptionsMenu();

                tvQuickRemark.setVisibility(done ? View.VISIBLE : View.GONE);

                tvAccountDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvAccountDone.setTextColor(done ? textColorPrimary : colorWarning);
                tvAccountDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);

                btnIdentity.setEnabled(done);
                btnInbox.setEnabled(done);

                prefs.edit().putBoolean("has_accounts", done).apply();

                if (done)
                    new SimpleTask<EntityFolder>() {
                        @Override
                        protected EntityFolder onExecute(Context context, Bundle args) {
                            DB db = DB.getInstance(context);
                            return db.folder().getPrimaryDrafts();
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityFolder drafts) {
                            tvNoPrimaryDrafts.setVisibility(drafts == null ? View.VISIBLE : View.GONE);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentSetup.this, new Bundle(), "setup:drafts");
            }
        });

        db.identity().liveComposableIdentities().observe(getViewLifecycleOwner(), new Observer<List<TupleIdentityEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleIdentityEx> identities) {
                boolean done = (identities != null && identities.size() > 0);
                tvIdentityDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvIdentityDone.setTextColor(done ? textColorPrimary : colorWarning);
                tvIdentityDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
                tvNoIdentities.setVisibility(done ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Doze
        Boolean ignoring = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                ignoring = Helper.isIgnoringOptimizations(getContext());
                if (ignoring == null)
                    ignoring = true;
            }
        }

        btnDoze.setEnabled(!ignoring);

        // https://issuetracker.google.com/issues/37070074
        //ignoring = (ignoring || Build.VERSION.SDK_INT != Build.VERSION_CODES.M);

        tvDozeDone.setText(ignoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setTextColor(ignoring ? textColorPrimary : colorWarning);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(ignoring ? check : null, null, null, null);

        // https://developer.android.com/training/basics/network-ops/data-saver.html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                int status = cm.getRestrictBackgroundStatus();
                grpDataSaver.setVisibility(
                        status == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
                                ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++)
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (Manifest.permission.READ_CONTACTS.equals(permissions[i]))
                    setContactsPermission(true);
            }
    }

    private void setContactsPermission(boolean granted) {
        if (granted)
            ContactInfo.init(getContext());

        tvPermissionsDone.setText(granted ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setTextColor(granted ? textColorPrimary : colorWarning);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(granted ? check : null, null, null, null);
        btnPermissions.setEnabled(!granted);
    }

    public static class FragmentDialogDoze extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
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
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
