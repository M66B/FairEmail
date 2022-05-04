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

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.util.List;

public class FragmentSetup extends FragmentBase {
    private ViewGroup view;

    private TextView tvWelcome;
    private TextView tvPrivacy;
    private TextView tvSupport;
    private ImageButton ibWelcome;
    private Group grpWelcome;

    private TextView tvNoInternet;
    private ImageButton ibHelp;
    private Button btnQuick;
    private TextView tvQuickNew;

    private CardView cardManual;
    private ImageButton ibManual;
    private TextView tvManual;

    private Button btnAccount;
    private Button btnIdentity;

    private TextView tvExchangeSupport;
    private TextView tvIdentityWhat;
    private Button btnInbox;
    private TextView tvFree;
    private TextView tvNoComposable;

    private TextView tvPermissionsDone;
    private Button btnPermissions;
    private ImageButton ibPermissions;
    private TextView tvImportContacts;

    private TextView tvDozeDone;
    private Button btnDoze;
    private TextView tvDoze12;
    private ImageButton ibDoze;

    private Button btnBackgroundRestricted;
    private Button btnDataSaver;
    private TextView tvStamina;

    private TextView tvBatteryUsage;
    private TextView tvSyncStopped;

    private CardView cardExtra;
    private TextView tvExtra;
    private Button btnNotification;
    private Button btnDelete;
    private Button btnApp;
    private Button btnMore;
    private Button btnSupport;
    private ImageButton ibExtra;

    private Group grpBackgroundRestricted;
    private Group grpDataSaver;
    private Group grpSupport;
    private Group grpExtra;

    private int textColorPrimary;
    private int colorWarning;
    private Drawable check;

    private boolean manual = false;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        if (savedInstanceState != null)
            manual = savedInstanceState.getBoolean("fair:manual");

        textColorPrimary = Helper.resolveColor(getContext(), android.R.attr.textColorPrimary);
        colorWarning = Helper.resolveColor(getContext(), R.attr.colorWarning);
        check = getContext().getDrawable(R.drawable.twotone_check_24);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvPrivacy = view.findViewById(R.id.tvPrivacy);
        tvSupport = view.findViewById(R.id.tvSupport);
        ibWelcome = view.findViewById(R.id.ibWelcome);
        grpWelcome = view.findViewById(R.id.grpWelcome);

        tvNoInternet = view.findViewById(R.id.tvNoInternet);
        ibHelp = view.findViewById(R.id.ibHelp);
        btnQuick = view.findViewById(R.id.btnQuick);
        tvQuickNew = view.findViewById(R.id.tvQuickNew);

        cardManual = view.findViewById(R.id.cardManual);
        ibManual = view.findViewById(R.id.ibManual);
        tvManual = view.findViewById(R.id.tvManual);

        btnAccount = view.findViewById(R.id.btnAccount);
        btnIdentity = view.findViewById(R.id.btnIdentity);

        tvExchangeSupport = view.findViewById(R.id.tvExchangeSupport);
        tvIdentityWhat = view.findViewById(R.id.tvIdentityWhat);
        btnInbox = view.findViewById(R.id.btnInbox);
        tvFree = view.findViewById(R.id.tvFree);
        tvNoComposable = view.findViewById(R.id.tvNoComposable);

        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);
        btnPermissions = view.findViewById(R.id.btnPermissions);
        ibPermissions = view.findViewById(R.id.ibPermissions);
        tvImportContacts = view.findViewById(R.id.tvImportContacts);

        tvDozeDone = view.findViewById(R.id.tvDozeDone);
        btnDoze = view.findViewById(R.id.btnDoze);
        tvDoze12 = view.findViewById(R.id.tvDoze12);
        ibDoze = view.findViewById(R.id.ibDoze);

        btnBackgroundRestricted = view.findViewById(R.id.btnBackgroundRestricted);
        btnDataSaver = view.findViewById(R.id.btnDataSaver);
        tvStamina = view.findViewById(R.id.tvStamina);

        tvBatteryUsage = view.findViewById(R.id.tvBatteryUsage);
        tvSyncStopped = view.findViewById(R.id.tvSyncStopped);

        cardExtra = view.findViewById(R.id.cardExtra);
        tvExtra = view.findViewById(R.id.tvExtra);
        btnNotification = view.findViewById(R.id.btnNotification);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnApp = view.findViewById(R.id.btnApp);
        btnMore = view.findViewById(R.id.btnMore);
        btnSupport = view.findViewById(R.id.btnSupport);
        ibExtra = view.findViewById(R.id.ibExtra);

        grpBackgroundRestricted = view.findViewById(R.id.grpBackgroundRestricted);
        grpDataSaver = view.findViewById(R.id.grpDataSaver);
        grpSupport = view.findViewById(R.id.grpSupport);
        grpExtra = view.findViewById(R.id.grpExtra);

        // Wire controls

        tvWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ibWelcome.setPressed(true);
                ibWelcome.setPressed(false);
                ibWelcome.performClick();
            }
        });

        tvPrivacy.setPaintFlags(tvPrivacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getPrivacyUri(v.getContext()), false);
            }
        });

        tvSupport.setPaintFlags(tvPrivacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view = new Intent(Intent.ACTION_VIEW)
                        .setData(Helper.getSupportUri(v.getContext()));
                v.getContext().startActivity(view);
            }
        });

        updateWelcome();

        ibWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                boolean setup_welcome = !prefs.getBoolean("setup_welcome", true);
                prefs.edit().putBoolean("setup_welcome", setup_welcome).apply();
                updateWelcome();
            }
        });

        ibHelp.setOnClickListener(new View.OnClickListener() {
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
                final Context context = getContext();
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, getViewLifecycleOwner(), btnQuick);
                Menu menu = popupMenu.getMenu();

                Resources res = context.getResources();
                String pkg = context.getPackageName();

                int order = 1;
                String gmail = getString(R.string.title_setup_oauth, getString(R.string.title_setup_gmail));
                MenuItem item = menu.add(Menu.FIRST, R.string.title_setup_gmail, order++, gmail);
                int resid = res.getIdentifier("provider_gmail", "drawable", pkg);
                if (resid != 0)
                    item.setIcon(resid);

                for (EmailProvider provider : EmailProvider.loadProfiles(context))
                    if (provider.oauth != null &&
                            (provider.oauth.enabled || BuildConfig.DEBUG) &&
                            !TextUtils.isEmpty(provider.oauth.clientId)) {
                        item = menu
                                .add(Menu.FIRST, -1, order++, getString(R.string.title_setup_oauth, provider.description))
                                .setIntent(new Intent(ActivitySetup.ACTION_QUICK_OAUTH)
                                        .putExtra("id", provider.id)
                                        .putExtra("name", provider.description)
                                        .putExtra("privacy", provider.oauth.privacy)
                                        .putExtra("askAccount", provider.oauth.askAccount)
                                        .putExtra("askTenant", provider.oauth.askTenant())
                                        .putExtra("pop", provider.pop != null));
                        resid = res.getIdentifier("provider_" + provider.id, "drawable", pkg);
                        if (resid != 0)
                            item.setIcon(resid);
                    }

                menu.add(Menu.NONE, R.string.title_setup_other, order++, R.string.title_setup_other)
                        .setIcon(R.drawable.twotone_auto_fix_high_24);

                menu.add(Menu.NONE, R.string.title_setup_classic, order++, R.string.title_setup_classic)
                        .setIcon(R.drawable.twotone_settings_24)
                        .setVisible(false);

                SpannableString ss = new SpannableString(getString(R.string.title_setup_pop3));
                ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
                menu.add(Menu.NONE, R.string.title_setup_pop3, order++, ss);

                menu.add(Menu.NONE, R.string.menu_faq, order++, R.string.menu_faq)
                        .setIcon(R.drawable.twotone_support_24)
                        .setVisible(false);

                popupMenu.insertIcons(context);

                MenuCompat.setGroupDividerEnabled(menu, true);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());

                        int itemId = item.getItemId();
                        if (itemId == R.string.title_setup_gmail) {
                            if (Helper.hasValidFingerprint(getContext()) || BuildConfig.DEBUG)
                                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_QUICK_GMAIL));
                            else
                                new AlertDialog.Builder(getContext())
                                        .setIcon(R.drawable.twotone_info_24)
                                        .setTitle(item.getTitle())
                                        .setMessage(R.string.title_setup_gmail_support)
                                        .setNeutralButton(R.string.title_info, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Helper.viewFAQ(getContext(), 6);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                            return true;
                        } else if (itemId == R.string.title_setup_other) {
                            lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_QUICK_SETUP));
                            return true;
                        } else if (itemId == R.string.title_setup_classic) {
                            ibManual.setPressed(true);
                            ibManual.setPressed(false);
                            manual = true;
                            updateManual();
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        scrollTo(R.id.ibManual, 0);
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }
                            });
                            return true;
                        } else if (itemId == R.string.title_setup_pop3) {
                            lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_QUICK_POP3));
                            return true;
                        } else if (itemId == R.string.menu_faq) {
                            Helper.view(getContext(), Helper.getSupportUri(getContext()), false);
                            return true;
                        }

                        if (item.getIntent() == null)
                            return false;
                        else {
                            if (Helper.hasValidFingerprint(getContext()) || BuildConfig.DEBUG)
                                lbm.sendBroadcast(item.getIntent());
                            else
                                new AlertDialog.Builder(getContext())
                                        .setIcon(R.drawable.twotone_info_24)
                                        .setTitle(item.getTitle())
                                        .setMessage(R.string.title_setup_oauth_permission)
                                        .setNeutralButton(R.string.title_info, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Helper.viewFAQ(getContext(), 147);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                            return true;
                        }
                    }
                });

                popupMenu.show();
            }
        });

        tvQuickNew.setPaintFlags(tvQuickNew.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvQuickNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 112);
            }
        });

        ibManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manual = !manual;
                updateManual();
                if (manual)
                    ensureVisible(cardManual);
            }
        });

        tvManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibManual.setPressed(true);
                ibManual.setPressed(false);
                ibManual.performClick();
            }
        });

        updateManual();

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

        tvExchangeSupport.setPaintFlags(tvExchangeSupport.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvExchangeSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 8);
            }
        });

        tvIdentityWhat.setPaintFlags(tvIdentityWhat.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvIdentityWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 9);
            }
        });

        btnInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentBase) getParentFragment()).finish();
            }
        });

        tvFree.setPaintFlags(tvFree.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 19);
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    btnPermissions.setEnabled(false);
                    String permission = Manifest.permission.READ_CONTACTS;
                    requestPermissions(new String[]{permission}, REQUEST_PERMISSIONS);
                } catch (Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                    /*
                        android.content.ActivityNotFoundException: No Activity found to handle Intent { act=android.content.pm.action.REQUEST_PERMISSIONS pkg=com.google.android.packageinstaller (has extras) }
                          at android.app.Instrumentation.checkStartActivityResult(Instrumentation.java:1805)
                          at android.app.Instrumentation.execStartActivity(Instrumentation.java:1634)
                          at android.app.Activity.startActivityForResult(Activity.java:4583)
                          at android.app.Activity.requestPermissions(Activity.java:3850)
                          at androidx.core.app.ActivityCompat.requestPermissions(SourceFile:11)
                          at androidx.activity.ComponentActivity$2.onLaunch(SourceFile:13)
                          at androidx.activity.result.ActivityResultRegistry$3.launch(SourceFile:2)
                          at androidx.activity.result.ActivityResultLauncher.launch(SourceFile:1)
                          at androidx.fragment.app.FragmentManager.launchRequestPermissions(SourceFile:4)
                          at androidx.fragment.app.Fragment.requestPermissions(SourceFile:2)
                          at eu.faircode.email.FragmentSetup$11.onClick(SourceFile:2)
                     */
                }
            }
        });

        ibPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 1);
            }
        });

        tvImportContacts.setPaintFlags(tvImportContacts.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvImportContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 172, true);
            }
        });

        btnDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("BatteryLife")
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onClick(View v) {
                if (hasPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
                    if (Boolean.FALSE.equals(Helper.isIgnoringOptimizations(v.getContext()))) {
                        Intent intent = new Intent()
                                .setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                .setData(Uri.parse("package:" + v.getContext().getPackageName()));

                        PackageManager pm = v.getContext().getPackageManager();
                        if (intent.resolveActivity(pm) == null)
                            new FragmentDialogDoze().show(getParentFragmentManager(), "setup:doze");
                        else
                            v.getContext().startActivity(intent);
                    } else {
                        Intent intent = new Intent()
                                .setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        v.getContext().startActivity(intent);
                    }
                } else
                    new FragmentDialogDoze().show(getParentFragmentManager(), "setup:doze");
            }
        });

        ibDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 175, true);
            }
        });

        tvStamina.setPaintFlags(tvStamina.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvStamina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.DONTKILL_URI + "sony"), true);
            }
        });

        tvBatteryUsage.setPaintFlags(tvBatteryUsage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvBatteryUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 39);
            }
        });

        tvSyncStopped.setPaintFlags(tvSyncStopped.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvSyncStopped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 16);
            }
        });

        updateExtra();

        ibExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                boolean setup_extra = !prefs.getBoolean("setup_extra", false);
                prefs.edit().putBoolean("setup_extra", setup_extra).apply();
                updateExtra();
                if (setup_extra)
                    ensureVisible(cardExtra);
            }
        });

        PackageManager pm = getContext().getPackageManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final Intent settings = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID));

            btnBackgroundRestricted.setEnabled(settings.resolveActivity(pm) != null); // system whitelisted
            btnBackgroundRestricted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(settings);
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Intent settings = new Intent(
                    Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID));

            btnDataSaver.setEnabled(settings.resolveActivity(pm) != null); // system whitelisted
            btnDataSaver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(settings);
                }
            });
        }

        tvExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ibExtra.setPressed(true);
                ibExtra.setPressed(false);
                ibExtra.performClick();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("all", true);

                FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentSetup.this, ActivitySetup.REQUEST_DELETE_ACCOUNT);
                fragment.show(getParentFragmentManager(), "setup:delete");
            }
        });

        final Intent channelService = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, "service");

        btnNotification.setEnabled(channelService.resolveActivity(pm) != null); // system whitelisted
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(channelService);
            }
        });

        final Intent app = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        app.setData(Uri.parse("package:" + getContext().getPackageName()));
        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getContext().startActivity(app);
                } catch (Throwable ex) {
                    Helper.reportNoViewer(getContext(), app, ex);
                }
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_SETUP_MORE));
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view = new Intent(Intent.ACTION_VIEW)
                        .setData(Helper.getSupportUri(v.getContext()));
                v.getContext().startActivity(view);
            }
        });

        // Initialize
        FragmentDialogTheme.setBackground(getContext(), view, false);

        tvNoInternet.setVisibility(View.GONE);
        btnIdentity.setEnabled(false);
        tvNoComposable.setVisibility(View.GONE);

        tvPermissionsDone.setText(null);
        tvPermissionsDone.setCompoundDrawables(null, null, null, null);

        tvDozeDone.setText(null);
        tvDozeDone.setCompoundDrawables(null, null, null, null);
        btnDoze.setEnabled(false);
        tvDoze12.setVisibility(View.GONE);

        btnInbox.setEnabled(false);

        grpBackgroundRestricted.setVisibility(View.GONE);
        grpDataSaver.setVisibility(View.GONE);
        tvStamina.setVisibility(View.GONE);

        setContactsPermission(hasPermission(Manifest.permission.READ_CONTACTS));

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:manual", manual);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

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

        db.account().liveSynchronizingAccounts().observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            private boolean done = false;

            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                done = (accounts != null && accounts.size() > 0);

                btnIdentity.setEnabled(done);
                btnInbox.setEnabled(done);
                btnInbox.setTypeface(done ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

                prefs.edit().putBoolean("has_accounts", done).apply();
            }
        });

        db.identity().liveComposableIdentities().observe(getViewLifecycleOwner(), new Observer<List<TupleIdentityEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleIdentityEx> identities) {
                Bundle args = new Bundle();

                new SimpleTask<List<EntityAccount>>() {
                    @Override
                    protected List<EntityAccount> onExecute(Context context, Bundle args) throws Throwable {
                        DB db = DB.getInstance(context);
                        return db.account().getSynchronizingAccounts(null);
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                        boolean done = ((accounts == null || accounts.size() == 0) ||
                                (identities != null && identities.size() > 0));
                        tvNoComposable.setVisibility(done ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        // Ignored
                    }
                }.execute(FragmentSetup.this, args, "setup:accounts");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        updateManual();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = Helper.getSystemService(getContext(), ConnectivityManager.class);
            cm.registerDefaultNetworkCallback(networkCallback);
        }

        // Doze
        boolean isIgnoring = !Boolean.FALSE.equals(Helper.isIgnoringOptimizations(getContext()));
        boolean canScheduleExact = AlarmManagerCompatEx.canScheduleExactAlarms(getContext());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            btnDoze.setEnabled(false);
        else {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            PackageManager pm = getContext().getPackageManager();
            if (intent.resolveActivity(pm) == null)
                btnDoze.setEnabled(false);
            else
                btnDoze.setEnabled(!isIgnoring || BuildConfig.DEBUG);
        }

        tvDozeDone.setText(isIgnoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setTextColor(isIgnoring ? textColorPrimary : colorWarning);
        tvDozeDone.setTypeface(null, isIgnoring ? Typeface.NORMAL : Typeface.BOLD);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(isIgnoring ? check : null, null, null, null);

        tvDoze12.setVisibility(!canScheduleExact && !isIgnoring ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ActivityManager am = Helper.getSystemService(getContext(), ActivityManager.class);
            grpBackgroundRestricted.setVisibility(am.isBackgroundRestricted()
                    ? View.VISIBLE : View.GONE);
        }

        grpDataSaver.setVisibility(ConnectionHelper.isDataSaving(getContext())
                ? View.VISIBLE : View.GONE);

        tvStamina.setVisibility(Helper.isStaminaEnabled(getContext())
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = Helper.getSystemService(getContext(), ConnectivityManager.class);
            cm.unregisterNetworkCallback(networkCallback);
        }
    }

    private void updateWelcome() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean setup_welcome = prefs.getBoolean("setup_welcome", true);
        ibWelcome.setImageLevel(setup_welcome ? 0 /* less */ : 1 /* more */);
        grpWelcome.setVisibility(setup_welcome ? View.VISIBLE : View.GONE);

        ViewGroup vwWelcome = (ViewGroup) ibWelcome.getParent();
        if (vwWelcome == null)
            return;

        vwWelcome.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;
                    Rect rect = new Rect(
                            vwWelcome.getLeft(),
                            ibWelcome.getTop(),
                            vwWelcome.getRight(),
                            ibWelcome.getBottom());
                    vwWelcome.setTouchDelegate(new TouchDelegate(rect, ibWelcome));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private void updateManual() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            Intent intent = activity.getIntent();
            if (intent.hasExtra("manual")) {
                manual = intent.getBooleanExtra("manual", false);
                intent.removeExtra("manual");
                activity.setIntent(intent);
            }
        }

        ibManual.setImageLevel(manual ? 0 /* less */ : 1 /* more */);
        cardManual.setVisibility(manual ? View.VISIBLE : View.GONE);
    }

    private void updateExtra() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean setup_extra = prefs.getBoolean("setup_extra", false);
        ibExtra.setImageLevel(setup_extra ? 0 /* less */ : 1 /* more */);

        grpSupport.setVisibility(setup_extra &&
                (Helper.hasValidFingerprint(getContext()) || BuildConfig.DEBUG)
                ? View.VISIBLE : View.GONE);

        grpExtra.setVisibility(setup_extra ? View.VISIBLE : View.GONE);

        ViewGroup vwExtra = (ViewGroup) ibExtra.getParent();
        if (vwExtra == null)
            return;

        vwExtra.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;
                    Rect rect = new Rect(
                            vwExtra.getLeft(),
                            ibExtra.getTop(),
                            vwExtra.getRight(),
                            ibExtra.getBottom());
                    vwExtra.setTouchDelegate(new TouchDelegate(rect, ibExtra));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private void ensureVisible(View child) {
        view.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Rect rect = new Rect();
                    child.getDrawingRect(rect);
                    view.offsetDescendantRectToMyCoords(child, rect);

                    int vh = view.getHeight();
                    int ch = rect.height();
                    if (vh > 0 && ch > 0) {
                        int y = rect.top - (vh - ch);
                        if (y > 0 && view instanceof ScrollView)
                            view.scrollTo(0, y);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_DELETE_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onDeleteAccount(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++)
            if (Manifest.permission.READ_CONTACTS.equals(permissions[i]))
                setContactsPermission(grantResults[i] == PackageManager.PERMISSION_GRANTED);
    }

    private void setContactsPermission(boolean granted) {
        if (granted)
            ContactInfo.init(getContext().getApplicationContext());

        tvPermissionsDone.setText(granted ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setTextColor(granted ? textColorPrimary : colorWarning);
        tvPermissionsDone.setTypeface(null, granted ? Typeface.NORMAL : Typeface.BOLD);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(granted ? check : null, null, null, null);
        btnPermissions.setEnabled(!granted);
    }

    private void onDeleteAccount(Bundle args) {
        long account = args.getLong("account");
        String name = args.getString("name");

        final Context context = getContext();

        Drawable d = context.getDrawable(R.drawable.twotone_warning_24);
        d.mutate();
        d.setTint(Helper.resolveColor(context, R.attr.colorWarning));

        new AlertDialog.Builder(context)
                .setIcon(d)
                .setTitle(name)
                .setMessage(R.string.title_account_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLong("id", account);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) throws Throwable {
                                long id = args.getLong("id");

                                DB db = DB.getInstance(context);
                                db.account().deleteAccount(id);

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentSetup.this, args, "setup:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .show();
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    updateInternet(true);
                }
            });
        }

        @Override
        public void onLost(@NonNull Network network) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    updateInternet(false);
                }
            });
        }

        private void updateInternet(boolean available) {
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                return;
            tvNoInternet.setVisibility(available ? View.GONE : View.VISIBLE);
        }
    };

    public static class FragmentDialogDoze extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setIcon(R.drawable.twotone_info_24)
                    .setTitle(R.string.title_setup_doze)
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
