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

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretPost;
import net.openid.appauth.GrantTypeValues;
import net.openid.appauth.NoClientAuthentication;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentSetup extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ViewGroup view;

    private TextView tvWelcome;
    private TextView tvFree;
    private TextView tvPrivacy;
    private TextView tvSupport;
    private ImageButton ibWelcome;
    private Group grpWelcome;

    private TextView tvNoInternet;
    private ImageButton ibHelp;
    private Button btnQuick;
    private TextView tvTutorials;
    private TextView tvQuickNew;

    private CardView cardManual;
    private ImageButton ibManual;
    private TextView tvManual;

    private Button btnAccount;
    private Button btnIdentity;

    private TextView tvExchangeSupport;
    private TextView tvIdentityWhat;
    private Button btnInbox;
    private TextView tvNoComposable;

    private TextView tvCalendarPermissions;
    private TextView tvNotificationPermissions;
    private TextView tvPermissionsDone;
    private Button btnPermissions;
    private TextView tvPermissionsWhy;
    private TextView tvImportContacts;
    private ImageButton ibGraphContacts;
    private Button btnGraphContacts;

    private TextView tvDozeDone;
    private Button btnDoze;
    private TextView tvDoze15;
    private TextView tvDoze12;
    private TextView tvDozeWhy;
    private TextView tvKilling;

    private Button btnBackgroundRestricted;
    private Button btnDataSaver;
    private TextView tvStamina;
    private CheckBox cbAlways;

    private TextView tvBatteryUsage;
    private TextView tvSyncStopped;

    private CardView cardExtra;
    private TextView tvExtra;
    private Button btnNotification;
    private Button btnDefaultIdentity;
    private Button btnSignature;
    private Button btnReorderAccounts;
    private Button btnReorderFolders;
    private Button btnPassword;
    private Button btnDelete;
    private Button btnApp;
    private Button btnMore;
    private Button btnSupport;
    private ImageButton ibExtra;

    private Group grpGraphContacts;
    private Group grpBackgroundRestricted;
    private Group grpDataSaver;
    private Group grpSupport;
    private Group grpExtra;

    private int textColorPrimary;
    private int colorWarning;
    private Drawable todo;
    private Drawable done;

    private boolean manual = false;

    private static final String GRAPH_SCOPE_READ_CONTACTS = "https://graph.microsoft.com/Contacts.Read";

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (savedInstanceState != null)
            manual = savedInstanceState.getBoolean("fair:manual");

        textColorPrimary = Helper.resolveColor(getContext(), android.R.attr.textColorPrimary);
        colorWarning = Helper.resolveColor(getContext(), R.attr.colorWarning);
        todo = getContext().getDrawable(R.drawable.twotone_priority_high_24);
        done = getContext().getDrawable(R.drawable.twotone_check_24);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvFree = view.findViewById(R.id.tvFree);
        tvPrivacy = view.findViewById(R.id.tvPrivacy);
        tvSupport = view.findViewById(R.id.tvSupport);
        ibWelcome = view.findViewById(R.id.ibWelcome);
        grpWelcome = view.findViewById(R.id.grpWelcome);

        tvNoInternet = view.findViewById(R.id.tvNoInternet);
        ibHelp = view.findViewById(R.id.ibHelp);
        btnQuick = view.findViewById(R.id.btnQuick);
        tvTutorials = view.findViewById(R.id.tvTutorials);
        tvQuickNew = view.findViewById(R.id.tvQuickNew);

        cardManual = view.findViewById(R.id.cardManual);
        ibManual = view.findViewById(R.id.ibManual);
        tvManual = view.findViewById(R.id.tvManual);

        btnAccount = view.findViewById(R.id.btnAccount);
        btnIdentity = view.findViewById(R.id.btnIdentity);

        tvExchangeSupport = view.findViewById(R.id.tvExchangeSupport);
        tvIdentityWhat = view.findViewById(R.id.tvIdentityWhat);
        btnInbox = view.findViewById(R.id.btnInbox);
        tvNoComposable = view.findViewById(R.id.tvNoComposable);

        tvCalendarPermissions = view.findViewById(R.id.tvCalendarPermissions);
        tvNotificationPermissions = view.findViewById(R.id.tvNotificationPermissions);
        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);
        btnPermissions = view.findViewById(R.id.btnPermissions);
        tvPermissionsWhy = view.findViewById(R.id.tvPermissionsWhy);
        tvImportContacts = view.findViewById(R.id.tvImportContacts);
        ibGraphContacts = view.findViewById(R.id.ibGraphContacts);
        btnGraphContacts = view.findViewById(R.id.btnGraphContacts);

        tvDozeDone = view.findViewById(R.id.tvDozeDone);
        btnDoze = view.findViewById(R.id.btnDoze);
        tvDoze15 = view.findViewById(R.id.tvDoze15);
        tvDoze12 = view.findViewById(R.id.tvDoze12);
        tvDozeWhy = view.findViewById(R.id.tvDozeWhy);
        tvKilling = view.findViewById(R.id.tvKilling);

        btnBackgroundRestricted = view.findViewById(R.id.btnBackgroundRestricted);
        btnDataSaver = view.findViewById(R.id.btnDataSaver);
        tvStamina = view.findViewById(R.id.tvStamina);
        cbAlways = view.findViewById(R.id.cbAlways);

        tvBatteryUsage = view.findViewById(R.id.tvBatteryUsage);
        tvSyncStopped = view.findViewById(R.id.tvSyncStopped);

        cardExtra = view.findViewById(R.id.cardExtra);
        tvExtra = view.findViewById(R.id.tvExtra);
        btnNotification = view.findViewById(R.id.btnNotification);
        btnDefaultIdentity = view.findViewById(R.id.btnDefaultIdentity);
        btnSignature = view.findViewById(R.id.btnSignature);
        btnReorderAccounts = view.findViewById(R.id.btnReorderAccounts);
        btnReorderFolders = view.findViewById(R.id.btnReorderFolders);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnPassword = view.findViewById(R.id.btnPassword);
        btnApp = view.findViewById(R.id.btnApp);
        btnMore = view.findViewById(R.id.btnMore);
        btnSupport = view.findViewById(R.id.btnSupport);
        ibExtra = view.findViewById(R.id.ibExtra);

        grpGraphContacts = view.findViewById(R.id.grpGraphContacts);
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

        tvFree.setPaintFlags(tvFree.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 19);
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
                        .setData(Helper.getSupportUri(v.getContext(), "Welcome:support"));
                v.getContext().startActivity(view);
            }
        });

        updateWelcome();

        ibWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                final Context context = v.getContext();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.DEBUG);

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, getViewLifecycleOwner(), btnQuick);
                Menu menu = popupMenu.getMenu();

                List<EmailProvider> providers = EmailProvider.getProviders(context, debug);

                int order = 1;

                // OAuth
                order = getMenuItems(menu, context, providers, order, false, debug);

                menu.add(Menu.NONE, R.string.title_setup_other, order++, R.string.title_setup_other)
                        .setIcon(R.drawable.twotone_auto_fix_high_24);

                // Gmail / account manager
                {
                    Resources res = context.getResources();
                    String pkg = context.getPackageName();

                    String gmail = getString(R.string.title_setup_android, getString(R.string.title_setup_gmail));
                    SpannableString ss = new SpannableString(gmail);
                    ss.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, ss.length(), 0);
                    MenuItem item = menu.add(Menu.FIRST, R.string.title_setup_gmail, order++, ss);
                    int resid = res.getIdentifier("provider_gmail", "drawable", pkg);
                    if (resid != 0)
                        item.setIcon(resid);
                }

                order = getMenuItems(menu, context, providers, order, true, debug);

                SpannableString imap = new SpannableString(getString(R.string.title_setup_imap));
                imap.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, imap.length(), 0);
                menu.add(Menu.FIRST, R.string.title_setup_imap, order++, imap);

                SpannableString pop3 = new SpannableString(getString(R.string.title_setup_pop3));
                pop3.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, pop3.length(), 0);
                menu.add(Menu.FIRST, R.string.title_setup_pop3, order++, pop3);

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
                            lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_QUICK_SETUP)
                                    .putExtra("title", itemId));
                            return true;
                        } else if (itemId == R.string.title_setup_imap) {
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
                            Helper.view(getContext(), Helper.getSupportUri(getContext(), "Providers:support"), false);
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

            private int getMenuItems(Menu menu, Context context, List<EmailProvider> providers, int order, boolean alt, boolean debug) {
                Resources res = context.getResources();
                String pkg = context.getPackageName();

                for (EmailProvider provider : providers)
                    if (provider.oauth != null &&
                            (provider.oauth.enabled || (provider.debug && debug)) &&
                            !TextUtils.isEmpty(provider.oauth.clientId) &&
                            provider.alt == alt) {
                        String title = getString(R.string.title_setup_oauth, provider.description);
                        SpannableString ss = new SpannableString(title);
                        if (provider.alt)
                            ss.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, ss.length(), 0);
                        MenuItem item = menu
                                .add(alt ? Menu.FIRST : Menu.NONE, -1, order++, ss)
                                .setIntent(new Intent(ActivitySetup.ACTION_QUICK_OAUTH)
                                        .putExtra("id", provider.id)
                                        .putExtra("name", provider.description)
                                        .putExtra("privacy", provider.oauth.privacy)
                                        .putExtra("askAccount", provider.oauth.askAccount)
                                        .putExtra("askTenant", (provider.graph == null && provider.oauth.askTenant()))
                                        .putExtra("pop", provider.pop != null));
                        // https://developers.google.com/identity/branding-guidelines
                        int resid = res.getIdentifier("provider_" + provider.id, "drawable", pkg);
                        if (resid != 0)
                            item.setIcon(resid);
                    }

                return order;
            }
        });

        tvTutorials.setPaintFlags(tvTutorials.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvTutorials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.TUTORIALS_URI), false);
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
                FragmentActivity activity = getActivity();
                if (activity instanceof ActivitySetup)
                    ((ActivitySetup) activity).onExit();
                else
                    ((FragmentBase) getParentFragment()).finish();
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String[] desired = Helper.getDesiredPermissions(v.getContext());
                    if (Helper.hasPermissions(v.getContext(), desired)) {
                        Intent intent = new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        v.getContext().startActivity(intent);
                    } else {
                        List<String> requesting = new ArrayList<>();
                        for (String permission : desired)
                            if (!hasPermission(permission))
                                requesting.add((permission));
                        Log.i("Requesting permissions " + TextUtils.join(",", requesting));
                        requestPermissions(requesting.toArray(new String[0]), REQUEST_PERMISSIONS);
                    }
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

        tvPermissionsWhy.setPaintFlags(tvPermissionsWhy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvPermissionsWhy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 1);
            }
        });

        tvImportContacts.setPaintFlags(tvImportContacts.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvImportContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 172);
            }
        });

        ibGraphContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 193);
            }
        });

        btnGraphContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putInt("type", EntityAccount.TYPE_IMAP);
                args.putString("filter", "outlook");

                FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentSetup.this, ActivitySetup.REQUEST_GRAPH_CONTACTS);
                fragment.show(getParentFragmentManager(), "account:contacts");
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

        tvDozeWhy.setPaintFlags(tvDozeWhy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvDozeWhy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 175);
            }
        });

        tvKilling.setPaintFlags(tvKilling.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvKilling.setVisibility(Helper.isAggressivelyKilling() ? View.VISIBLE : View.GONE);
        tvKilling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.DONTKILL_URI), true);
            }
        });

        tvStamina.setPaintFlags(tvStamina.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvStamina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.DONTKILL_URI + "sony"), true);
            }
        });

        cbAlways.setChecked(ServiceSynchronize.getPollInterval(getContext()) == 0);
        cbAlways.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                int poll_interval = prefs.getInt("poll_interval", EntityAccount.DEFAULT_POLL_INTERVAL);
                if (poll_interval == 0)
                    poll_interval = EntityAccount.DEFAULT_POLL_INTERVAL;
                int value = (isChecked ? 0 : poll_interval);
                prefs.edit().putInt("poll_interval", value).apply();
                if (value == 0)
                    prefs.edit().remove("auto_optimize").apply();
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

        btnDefaultIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogSelectIdentity fragment = new FragmentDialogSelectIdentity();
                fragment.setArguments(new Bundle());
                fragment.setTargetFragment(FragmentSetup.this, ActivitySetup.REQUEST_DEFAULT_IDENTITY);
                fragment.show(getParentFragmentManager(), "select:identity:default");
            }
        });

        btnSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogSelectIdentity fragment = new FragmentDialogSelectIdentity();
                fragment.setArguments(new Bundle());
                fragment.setTargetFragment(FragmentSetup.this, ActivitySetup.REQUEST_SELECT_IDENTITY);
                fragment.show(getParentFragmentManager(), "select:identity:signature");
            }
        });

        btnReorderAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_SETUP_REORDER)
                        .putExtra("title", R.string.title_setup_reorder_accounts)
                        .putExtra("className", EntityAccount.class.getName()));
            }
        });

        btnReorderFolders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_SETUP_REORDER)
                        .putExtra("title", R.string.title_setup_reorder_folders)
                        .putExtra("className", TupleFolderSort.class.getName()));
            }
        });


        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("all", true);

                FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentSetup.this, ActivitySetup.REQUEST_CHANGE_PASSWORD);
                fragment.show(getParentFragmentManager(), "setup:password");
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
                        .setData(Helper.getSupportUri(v.getContext(), "Extra:support"));
                v.getContext().startActivity(view);
            }
        });

        // Initialize
        tvNoInternet.setVisibility(View.GONE);
        btnIdentity.setEnabled(false);
        tvNoComposable.setVisibility(View.GONE);

        tvCalendarPermissions.setVisibility(BuildConfig.PLAY_STORE_RELEASE ? View.GONE : View.VISIBLE);

        tvNotificationPermissions.setVisibility(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                        ? View.GONE : View.VISIBLE);
        tvPermissionsDone.setText(null);
        tvPermissionsDone.setCompoundDrawables(null, null, null, null);
        btnPermissions.setText(null);
        btnPermissions.setCompoundDrawables(null, null, null, null);

        tvDozeDone.setText(null);
        tvDozeDone.setCompoundDrawables(null, null, null, null);
        btnDoze.setText(null);
        btnDoze.setCompoundDrawables(null, null, null, null);
        tvDoze15.setVisibility(View.GONE);
        tvDoze12.setVisibility(View.GONE);

        btnInbox.setEnabled(false);

        grpGraphContacts.setVisibility(View.GONE);
        grpBackgroundRestricted.setVisibility(View.GONE);
        grpDataSaver.setVisibility(View.GONE);
        tvStamina.setVisibility(View.GONE);

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
        if ("poll_interval".equals(key) && cbAlways != null)
            cbAlways.setChecked(ServiceSynchronize.getPollInterval(getContext()) == 0);
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

                    EntityFolder.getOutbox(context);

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

                boolean outlook = false;
                if (accounts != null)
                    for (EntityAccount account : accounts)
                        if (account.isOutlook()) {
                            outlook = true;
                            break;
                        }

                grpGraphContacts.setVisibility(outlook ? View.VISIBLE : View.GONE);

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

        // Permissions
        setGrantedPermissions();

        // Doze
        boolean isIgnoring = !Boolean.FALSE.equals(Helper.isIgnoringOptimizations(getContext()));
        boolean canScheduleExact = AlarmManagerCompatEx.canScheduleExactAlarms(getContext());

        if (isIgnoring && !BuildConfig.DEBUG) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.edit().putBoolean("was_ignoring", true).apply();
        }

        tvDozeDone.setText(isIgnoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setTextColor(isIgnoring ? textColorPrimary : colorWarning);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(
                (isIgnoring ? done : todo).mutate(), null, null, null);
        TextViewCompat.setCompoundDrawableTintList(tvDozeDone,
                ColorStateList.valueOf(isIgnoring ? textColorPrimary : colorWarning));

        btnDoze.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Helper.isArc() && (!isIgnoring || BuildConfig.DEBUG));
        btnDoze.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, isIgnoring ? R.drawable.twotone_settings_24 : R.drawable.twotone_check_24, 0);
        btnDoze.setText(isIgnoring && BuildConfig.DEBUG ? R.string.title_setup_manage : R.string.title_setup_grant);

        tvDoze15.setVisibility(Helper.isAndroid15() && !isIgnoring ? View.VISIBLE : View.GONE);
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
    }

    private void updateManual() {
        boolean scroll = false;
        FragmentActivity activity = getActivity();
        if (activity != null) {
            Intent intent = activity.getIntent();
            if (intent.hasExtra("manual")) {
                manual = intent.getBooleanExtra("manual", false);
                scroll = intent.getBooleanExtra("scroll", false);
                intent.removeExtra("manual");
                intent.removeExtra("scroll");
                activity.setIntent(intent);
            }
        }

        ibManual.setImageLevel(manual ? 0 /* less */ : 1 /* more */);
        cardManual.setVisibility(manual ? View.VISIBLE : View.GONE);
        if (scroll)
            ensureVisible(cardManual);
    }

    private void updateExtra() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean setup_extra = prefs.getBoolean("setup_extra", false);
        ibExtra.setImageLevel(setup_extra ? 0 /* less */ : 1 /* more */);

        grpSupport.setVisibility(setup_extra &&
                (Helper.hasValidFingerprint(getContext()) || BuildConfig.DEBUG)
                ? View.VISIBLE : View.GONE);

        grpExtra.setVisibility(setup_extra ? View.VISIBLE : View.GONE);
    }

    void prepareSearch() {
        try {
            manual = true;
            updateManual();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.edit().putBoolean("setup_extra", true).apply();
            updateExtra();
        } catch (Throwable ex) {
            Log.e(ex);
        }
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
                case ActivitySetup.REQUEST_DEFAULT_IDENTITY:
                    if (resultCode == RESULT_OK && data != null)
                        onSelectDefaultIdentity(data.getBundleExtra("args"));
                    break;
                case ActivitySetup.REQUEST_SELECT_IDENTITY:
                    if (resultCode == RESULT_OK && data != null)
                        onSelectIdentity(data.getBundleExtra("args"));
                    break;
                case ActivitySetup.REQUEST_EDIT_SIGNATURE:
                    if (resultCode == RESULT_OK && data != null)
                        onEditIdentity(data.getExtras());
                    break;
                case ActivitySetup.REQUEST_CHANGE_PASSWORD:
                    if (resultCode == RESULT_OK && data != null)
                        onChangePassword(data.getBundleExtra("args"));
                    break;
                case ActivitySetup.REQUEST_DELETE_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onDeleteAccount(data.getBundleExtra("args"));
                    break;
                case ActivitySetup.REQUEST_GRAPH_CONTACTS:
                    if (resultCode == RESULT_OK && data != null)
                        handleImportGraphContacts(data.getBundleExtra("args"));
                    break;
                case ActivitySetup.REQUEST_GRAPH_CONTACTS_OAUTH:
                    if (resultCode == RESULT_OK && data != null)
                        onHandleGraphContactsOAuth(data);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        setGrantedPermissions();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();

        int denied = 0;
        for (int i = 0; i < Math.min(permissions.length, grantResults.length); i++) {
            String key = "requested." + permissions[i];

            Log.i("Permission " + permissions[i] + "=" +
                    (grantResults[i] == PackageManager.PERMISSION_GRANTED));

            if (grantResults[i] == PackageManager.PERMISSION_DENIED &&
                    grantResults[i] == prefs.getInt(key, PackageManager.PERMISSION_GRANTED))
                denied++;

            if (grantResults[i] == PackageManager.PERMISSION_GRANTED &&
                    Manifest.permission.READ_CONTACTS.equals(permissions[i]))
                ContactInfo.init(getContext().getApplicationContext());

            editor.putInt(key, grantResults[i]);
        }

        editor.apply();

        if (denied > 0) {
            Intent settings = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID));
            startActivity(settings);
        }
    }

    private void setGrantedPermissions() {
        boolean all = true;
        for (String permission : Helper.getDesiredPermissions(getContext()))
            if (!hasPermission(permission)) {
                all = false;
                break;
            }

        tvPermissionsDone.setText(all ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setTextColor(all ? textColorPrimary : colorWarning);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(
                (all ? done : todo).mutate(), null, null, null);
        TextViewCompat.setCompoundDrawableTintList(tvPermissionsDone,
                ColorStateList.valueOf(all ? textColorPrimary : colorWarning));

        btnPermissions.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, all ? R.drawable.twotone_settings_24 : R.drawable.twotone_check_24, 0);
        btnPermissions.setText(all ? R.string.title_setup_manage : R.string.title_setup_grant);
    }

    private void onSelectDefaultIdentity(Bundle args) {
        FragmentCompose.onSelectIdentity(getContext(), getViewLifecycleOwner(), getParentFragmentManager(), args);
    }

    private void onSelectIdentity(Bundle args) {
        Intent intent = new Intent(getContext(), ActivitySignature.class);
        intent.putExtra("id", args.getLong("id"));
        intent.putExtra("html", args.getString("html"));
        startActivityForResult(intent, ActivitySetup.REQUEST_EDIT_SIGNATURE);
    }

    private void onEditIdentity(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                String html = args.getString("html");

                DB db = DB.getInstance(context);
                db.identity().setIdentitySignature(id, html);

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "set:signature");
    }

    private void onChangePassword(Bundle args) {
        long account = args.getLong("account");
        int protocol = args.getInt("protocol");
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.sendBroadcast(
                new Intent(ActivitySetup.ACTION_EDIT_ACCOUNT)
                        .putExtra("id", account)
                        .putExtra("protocol", protocol));
    }

    private void onDeleteAccount(Bundle args) {
        long account = args.getLong("account");
        String name = args.getString("name");

        final Context context = getContext();

        Drawable d = ContextCompat.getDrawable(context, R.drawable.twotone_warning_24);
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

    private void handleImportGraphContacts(Bundle args) {
        final Context context = getContext();
        try {
            long account = args.getLong("account");
            String user = args.getString("user");
            EmailProvider provider = EmailProvider.getProvider(context, "outlookgraph");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean("suggest_sent", true).apply();

            if (BuildConfig.DEBUG) {
                String json = prefs.getString("graph.contacts." + account, null);
                if (!TextUtils.isEmpty(json)) {
                    args.putString("authState", json);
                    taskGraph.execute(this, args, "graph:contacts");
                    return;
                }
            }

            AppAuthConfiguration appAuthConfig = new AppAuthConfiguration.Builder()
                    .build();
            AuthorizationService authService = new AuthorizationService(context, appAuthConfig);

            AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                    Uri.parse(provider.graph.authorizationEndpoint),
                    Uri.parse(provider.graph.tokenEndpoint));

            AuthorizationRequest.Builder authRequestBuilder =
                    new AuthorizationRequest.Builder(
                            serviceConfig,
                            provider.graph.clientId,
                            ResponseTypeValues.CODE,
                            Uri.parse(provider.graph.redirectUri))
                            .setScopes(GRAPH_SCOPE_READ_CONTACTS)
                            .setState(provider.id + ":" + account)
                            .setLoginHint(user);

            if (!TextUtils.isEmpty(provider.graph.prompt))
                authRequestBuilder.setPrompt(provider.graph.prompt);

            Intent authIntent = authService.getAuthorizationRequestIntent(authRequestBuilder.build());
            EntityLog.log(context, "Graph/contacts intent=" + authIntent);
            startActivityForResult(authIntent, ActivitySetup.REQUEST_GRAPH_CONTACTS_OAUTH);
        } catch (Throwable ex) {
            EntityLog.log(context, "Graph/contacts ex=" + Log.formatThrowable(ex, false));
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    }

    private void onHandleGraphContactsOAuth(@NonNull Intent data) {
        final Context context = getContext();
        try {
            EntityLog.log(context, "Graph/contacts authorized");

            AuthorizationResponse auth = AuthorizationResponse.fromIntent(data);
            if (auth == null) {
                AuthorizationException ex = AuthorizationException.fromIntent(data);
                if (ex == null)
                    throw new IllegalArgumentException("No response data");
                else
                    throw ex;
            }

            final AuthState authState = new AuthState(auth, null);
            final EmailProvider provider = EmailProvider.getProvider(context, "outlookgraph");

            AuthorizationService authService = new AuthorizationService(context);

            ClientAuthentication clientAuth;
            if (provider.graph.clientSecret == null)
                clientAuth = NoClientAuthentication.INSTANCE;
            else
                clientAuth = new ClientSecretPost(provider.graph.clientSecret);

            TokenRequest.Builder builder = new TokenRequest.Builder(
                    auth.request.configuration,
                    auth.request.clientId)
                    .setGrantType(GrantTypeValues.AUTHORIZATION_CODE)
                    .setRedirectUri(auth.request.redirectUri)
                    .setCodeVerifier(auth.request.codeVerifier)
                    .setAuthorizationCode(auth.authorizationCode)
                    .setNonce(auth.request.nonce);

            if (provider.graph.tokenScopes)
                builder.setScope(GRAPH_SCOPE_READ_CONTACTS);

            authService.performTokenRequest(
                    builder.build(),
                    clientAuth,
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(TokenResponse access, AuthorizationException error) {
                            try {
                                if (error != null)
                                    throw error;

                                if (access == null || access.accessToken == null)
                                    throw new IllegalStateException("No access token");

                                authState.update(access, null);
                                EntityLog.log(context, "Graph/contacts got token");

                                int semi = auth.request.state.lastIndexOf(':');
                                long account = Long.parseLong(auth.request.state.substring(semi + 1));

                                Bundle args = new Bundle();
                                args.putLong("account", account);
                                args.putString("authState", authState.jsonSerializeString());

                                taskGraph.execute(FragmentSetup.this, args, "graph:contacts");
                            } catch (Throwable ex) {
                                try {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                } catch (Throwable exex) {
                                    Log.w(exex);
                                }
                            }
                        }
                    });
        } catch (Throwable ex) {
            EntityLog.log(context, "Graph/contacts ex=" + Log.formatThrowable(ex, false));
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    }

    private final SimpleTask<Integer> taskGraph = new SimpleTask<Integer>() {
        @Override
        protected Integer onExecute(Context context, Bundle args) throws Throwable {
            long account = args.getLong("account");
            String json = args.getString("authState");
            AuthState authState = AuthState.jsonDeserialize(json);

            return MicrosoftGraph.downloadContacts(context, account, authState);
        }

        @Override
        protected void onExecuted(Bundle args, @NonNull Integer count) {
            final Context context = getContext();
            EntityLog.log(context, "Graph/contacts count=" + count);

            NumberFormat NF = NumberFormat.getInstance();
            String msg = getString(R.string.title_setup_import_graph_new, NF.format(count));

            final Snackbar snackbar = Helper.setSnackbarOptions(
                    Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE));
            snackbar.setAction(R.string.title_check, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                    lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_MANAGE_LOCAL_CONTACTS));
                }
            });
            snackbar.show();
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            EntityLog.log(getContext(), "Graph/contacts ex=" + Log.formatThrowable(ex, false));
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    };

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    updateInternet(true);
                }
            });
        }

        @Override
        public void onLost(@NonNull Network network) {
            getMainHandler().post(new Runnable() {
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
}
