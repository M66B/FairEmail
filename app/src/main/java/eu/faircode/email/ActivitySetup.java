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

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ActivitySetup extends ActivityBase implements FragmentManager.OnBackStackChangedListener {
    private View view;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ConstraintLayout drawerContainer;
    private RecyclerView rvMenu;

    private boolean hasAccount;

    static final int REQUEST_SOUND_INBOUND = 1;
    static final int REQUEST_RINGTONE_OUTBOUND = 2;
    static final int REQUEST_AUDIO_OUTBOUND = 3;
    static final int REQUEST_CHOOSE_ACCOUNT = 4;
    static final int REQUEST_DONE = 5;
    static final int REQUEST_IMPORT_CERTIFICATE = 6;
    static final int REQUEST_OAUTH = 7;
    static final int REQUEST_STILL = 8;
    static final int REQUEST_DEFAULT_IDENTITY = 9;
    static final int REQUEST_SELECT_IDENTITY = 10;
    static final int REQUEST_EDIT_SIGNATURE = 11;
    static final int REQUEST_CHANGE_PASSWORD = 12;
    static final int REQUEST_EDIT_ACCOUNT_COLOR = 13;
    static final int REQUEST_DELETE_ACCOUNT = 14;
    static final int REQUEST_EDIT_IDENITY_COLOR = 15;
    static final int REQUEST_IMPORT_PROVIDERS = 16;
    static final int REQUEST_GRAPH_CONTACTS = 17;
    static final int REQUEST_GRAPH_CONTACTS_OAUTH = 18;
    static final int REQUEST_DEBUG_INFO = 7000;

    static final int PI_CONNECTION = 1;
    static final int PI_MISC = 2;

    static final String ACTION_QUICK_GMAIL = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_GMAIL";
    static final String ACTION_QUICK_OAUTH = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_OAUTH";
    static final String ACTION_QUICK_SETUP = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_SETUP";
    static final String ACTION_QUICK_POP3 = BuildConfig.APPLICATION_ID + ".ACTION_QUICK_POP3";
    static final String ACTION_VIEW_ACCOUNTS = BuildConfig.APPLICATION_ID + ".ACTION_VIEW_ACCOUNTS";
    static final String ACTION_VIEW_IDENTITIES = BuildConfig.APPLICATION_ID + ".ACTION_VIEW_IDENTITIES";
    static final String ACTION_EDIT_ACCOUNT = BuildConfig.APPLICATION_ID + ".EDIT_ACCOUNT";
    static final String ACTION_EDIT_IDENTITY = BuildConfig.APPLICATION_ID + ".EDIT_IDENTITY";
    static final String ACTION_MANAGE_LOCAL_CONTACTS = BuildConfig.APPLICATION_ID + ".MANAGE_LOCAL_CONTACTS";
    static final String ACTION_MANAGE_CERTIFICATES = BuildConfig.APPLICATION_ID + ".MANAGE_CERTIFICATES";
    static final String ACTION_IMPORT_CERTIFICATE = BuildConfig.APPLICATION_ID + ".IMPORT_CERTIFICATE";
    static final String ACTION_SETUP_REORDER = BuildConfig.APPLICATION_ID + ".SETUP_REORDER";
    static final String ACTION_SETUP_MORE = BuildConfig.APPLICATION_ID + ".SETUP_MORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(this).inflate(R.layout.activity_setup, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Helper.resolveColor(this, R.attr.colorDrawerScrim));

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        drawerContainer = findViewById(R.id.drawer_container);
        rvMenu = drawerContainer.findViewById(R.id.rvMenu);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvMenu.setLayoutManager(llm);
        final AdapterNavMenu adapter = new AdapterNavMenu(this, this);
        rvMenu.setAdapter(adapter);

        final Drawable d = getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, llm.getOrientation()) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                NavMenuItem menu = (adapter == null ? null : adapter.get(pos));
                outRect.set(0, 0, 0, menu != null && menu.isSeparated() ? d.getIntrinsicHeight() : 0);
            }
        };
        itemDecorator.setDrawable(d);
        rvMenu.addItemDecoration(itemDecorator);

        final List<NavMenuItem> menus = new ArrayList<>();

        int colorWarning = Helper.resolveColor(this, R.attr.colorWarning);
        menus.add(new NavMenuItem(R.drawable.twotone_close_24, R.string.title_setup_close, new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivitySetup.this);
                    prefs.edit().remove("eula").apply();
                }

                onMenuClose();
            }
        }).setColor(colorWarning).setSeparated());

        menus.add(new NavMenuItem(R.drawable.twotone_reorder_24, R.string.title_setup_reorder_accounts, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOrder(R.string.title_setup_reorder_accounts, EntityAccount.class.getName());
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_reorder_24, R.string.title_setup_reorder_folders, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuOrder(R.string.title_setup_reorder_folders, TupleFolderSort.class.getName());
            }
        }).setSeparated());

        menus.add(new NavMenuItem(R.drawable.twotone_list_alt_24, R.string.title_log, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuLog();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_help_24, R.string.menu_legend, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuLegend();
            }
        }));

        menus.add(new NavMenuItem(R.drawable.twotone_support_24, R.string.menu_faq, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuFAQ();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (DebugHelper.isAvailable()) {
                    drawerLayout.closeDrawer(drawerContainer);
                    onDebugInfo();
                    return true;
                } else
                    return false;
            }
        }).setExternal(true));

        menus.add(new NavMenuItem(R.drawable.twotone_feedback_24, R.string.menu_issue, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuIssue();
            }
        }).setExternal(true));

        menus.add(new NavMenuItem(R.drawable.twotone_account_circle_24, R.string.menu_privacy, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuPrivacy();
            }
        }).setExternal(true));

        menus.add(new NavMenuItem(R.drawable.twotone_info_24, R.string.menu_about, new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(drawerContainer);
                onMenuAbout();
            }
        }, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                startActivity(new Intent(ActivitySetup.this, ActivityBilling.class));
                return true;
            }
        }).setSubtitle(BuildConfig.VERSION_NAME));

        adapter.set(menus, true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Helper.isKeyboardVisible(view))
                    Helper.hideKeyboard(view);
                else
                    onExit();
            }
        });

        if (getSupportFragmentManager().getFragments().size() == 0) {
            Intent intent = getIntent();
            String target = intent.getStringExtra("target");
            long id = intent.getLongExtra("id", -1L);

            if ("accounts".equals(target) && id > 0)
                onEditAccount(intent);
            else if ("identities".equals(target) && id > 0)
                onEditIdentity(intent);
            else if ("gmail".equals(target))
                onGmail(intent);
            else if ("oauth".equals(target))
                onOAuth(intent);
            else {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                if ("accounts".equals(target))
                    fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
                else
                    fragmentTransaction.replace(R.id.content_frame, new FragmentOptions()).addToBackStack("options");
                fragmentTransaction.commit();
            }

            if (intent.hasExtra("target")) {
                intent.removeExtra("target");
                setIntent(intent);
            }
        }

        if (savedInstanceState != null) {
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("fair:toggle"));
        }

        DB db = DB.getInstance(this);

        db.account().liveSynchronizingAccounts().observe(this, new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(List<EntityAccount> accounts) {
                hasAccount = (accounts != null && accounts.size() > 0);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:toggle", drawerToggle == null || drawerToggle.isDrawerIndicatorEnabled());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_QUICK_GMAIL);
        iff.addAction(ACTION_QUICK_OAUTH);
        iff.addAction(ACTION_QUICK_SETUP);
        iff.addAction(ACTION_QUICK_POP3);
        iff.addAction(ACTION_VIEW_ACCOUNTS);
        iff.addAction(ACTION_VIEW_IDENTITIES);
        iff.addAction(ACTION_EDIT_ACCOUNT);
        iff.addAction(ACTION_EDIT_IDENTITY);
        iff.addAction(ACTION_MANAGE_LOCAL_CONTACTS);
        iff.addAction(ACTION_MANAGE_CERTIFICATES);
        iff.addAction(ACTION_IMPORT_CERTIFICATE);
        iff.addAction(ACTION_SETUP_REORDER);
        iff.addAction(ACTION_SETUP_MORE);
        lbm.registerReceiver(receiver, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void onExit() {
        if (drawerLayout.isDrawerOpen(drawerContainer))
            drawerLayout.closeDrawer(drawerContainer);
        else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                performBack();
                return;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean setup_reminder = prefs.getBoolean("setup_reminder", true);

            boolean hasContactPermissions =
                    hasPermission(android.Manifest.permission.READ_CONTACTS);
            boolean hasNotificationPermissions =
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            hasPermission(Manifest.permission.POST_NOTIFICATIONS));
            boolean isIgnoring = !Boolean.FALSE.equals(Helper.isIgnoringOptimizations(this));

            if (!setup_reminder ||
                    (hasContactPermissions && hasNotificationPermissions && isIgnoring))
                performBack();
            else {
                FragmentDialogPermissions fragment = new FragmentDialogPermissions();
                fragment.setTargetActivity(this, REQUEST_STILL);
                fragment.show(getSupportFragmentManager(), "setup:still");
            }
        }
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (hasAccount) {
                startActivity(new Intent(this, ActivityView.class));
                finishAndRemoveTask();
            } else
                finish();
        } else {
            if (drawerLayout.isDrawerOpen(drawerContainer))
                drawerLayout.closeDrawer(drawerContainer);
            drawerToggle.setDrawerIndicatorEnabled(count == 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        int itemId = item.getItemId();
        if (itemId == R.id.menu_close) {
            onMenuClose();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_IMPORT_CERTIFICATE:
                    if (resultCode == RESULT_OK && data != null)
                        handleImportCertificate(data);
                    break;
                case REQUEST_IMPORT_PROVIDERS:
                    if (resultCode == RESULT_OK && data != null)
                        handleImportProviders(data);
                    break;
                case ActivitySetup.REQUEST_STILL:
                    if (resultCode == Activity.RESULT_OK) {
                        Bundle result = new Bundle();
                        result.putInt("page", 0);
                        getSupportFragmentManager().setFragmentResult("options:tab", result);
                    } else
                        performBack();
                    break;
                case REQUEST_DEBUG_INFO:
                    if (resultCode == RESULT_OK && data != null)
                        onDebugInfo(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onMenuClose() {
        drawerLayout.closeDrawer(drawerContainer, false);
        onExit();
    }

    private void onMenuOrder(int title, String className) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("order", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putString("class", className);

        FragmentOrder fragment = new FragmentOrder();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("order");
        fragmentTransaction.commit();
    }

    private void onMenuLog() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("logs", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLogs()).addToBackStack("logs");
        fragmentTransaction.commit();
    }

    private void onMenuLegend() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("legend", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLegend()).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuFAQ() {
        Helper.viewFAQ(this, 0);
    }

    private void onDebugInfo() {
        FragmentDialogDebug fragment = new FragmentDialogDebug();
        fragment.setArguments(new Bundle());
        fragment.setTargetActivity(this, REQUEST_DEBUG_INFO);
        fragment.show(getSupportFragmentManager(), "debug");
    }

    private void onDebugInfo(Bundle args) {
        new SimpleTask<Long>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ToastEx.makeText(ActivitySetup.this, R.string.title_debug_info, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Long onExecute(Context context, Bundle args) throws IOException, JSONException {
                EntityMessage m = DebugHelper.getDebugInfo(context,
                        "setup", R.string.title_debug_info_remark, null, null, args);
                return (m == null ? null : m.id);
            }

            @Override
            protected void onExecuted(Bundle args, Long id) {
                if (id == null)
                    return;
                startActivity(new Intent(ActivitySetup.this, ActivityCompose.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("action", "edit")
                        .putExtra("id", id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean report = !(ex instanceof IllegalArgumentException);
                Log.unexpectedError(getSupportFragmentManager(), ex, report);
            }

        }.execute(this, args, "debug:info");
    }

    private void onMenuIssue() {
        startActivity(Helper.getIntentIssue(this, "Setup:issue"));
    }

    private void onMenuPrivacy() {
        Helper.view(this, Helper.getPrivacyUri(this), false);
    }

    private void onMenuAbout() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getSupportFragmentManager().popBackStack("about", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private void handleImportCertificate(Intent data) {
        Uri uri = data.getData();
        if (uri != null) {
            Bundle args = new Bundle();
            args.putParcelable("uri", uri);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws Throwable {
                    Uri uri = args.getParcelable("uri");
                    Log.i("Import certificate uri=" + uri);

                    if (uri == null)
                        throw new FileNotFoundException();

                    boolean der = false;
                    String extension = Helper.getExtension(uri.getLastPathSegment());
                    DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
                    String type = (dfile == null ? null : dfile.getType());
                    // https://pki-tutorial.readthedocs.io/en/latest/mime.html
                    if (!"pem".equalsIgnoreCase(extension) &&
                            !"application/x-pem-file".equals(type))
                        try {
                            if (type != null && type.startsWith("application/"))
                                der = true;
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                    Log.i("Extension=" + extension + "type=" + type + " DER=" + der);

                    X509Certificate cert;
                    CertificateFactory fact = CertificateFactory.getInstance("X.509");
                    try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                        if (is == null)
                            throw new FileNotFoundException(uri.toString());

                        if (der)
                            cert = (X509Certificate) fact.generateCertificate(is);
                        else {
                            // throws DecoderException extends IllegalStateException
                            PemObject pem = new PemReader(new InputStreamReader(is)).readPemObject();
                            if (pem == null)
                                throw new IllegalArgumentException("Invalid key file");
                            ByteArrayInputStream bis = new ByteArrayInputStream(pem.getContent());
                            cert = (X509Certificate) fact.generateCertificate(bis);
                        }
                    }

                    String fingerprint = EntityCertificate.getFingerprintSha256(cert);
                    List<String> emails = EntityCertificate.getEmailAddresses(cert);

                    if (emails.size() == 0)
                        throw new IllegalArgumentException("No email address found in key");

                    DB db = DB.getInstance(context);
                    for (String email : emails) {
                        EntityCertificate record = db.certificate().getCertificate(fingerprint, email);
                        if (record == null) {
                            record = EntityCertificate.from(cert, email);
                            record.id = db.certificate().insertCertificate(record);
                        }
                    }

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    ToastEx.makeText(ActivitySetup.this, R.string.title_completed, Toast.LENGTH_LONG).show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    // DecoderException: unable to decode base64 string: invalid characters encountered in base64 data
                    if (ex instanceof DecoderException)
                        ex = new Throwable("Are you trying to import a PGP key as an S/MIME key?", ex);
                    boolean expected =
                            (ex instanceof IllegalArgumentException ||
                                    ex instanceof FileNotFoundException ||
                                    ex instanceof CertificateException ||
                                    ex instanceof DecoderException ||
                                    ex instanceof SecurityException);
                    Log.unexpectedError(getSupportFragmentManager(), ex, !expected);
                }
            }.execute(this, args, "setup:cert");
        }
    }

    private void handleImportProviders(Intent data) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                Log.i("Reading URI=" + uri);
                ContentResolver resolver = context.getContentResolver();
                InputStream is = resolver.openInputStream(uri);
                if (is == null)
                    throw new FileNotFoundException(uri.toString());
                EmailProvider.importProfiles(is, context);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(ActivitySetup.this, R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "import:providers");
    }

    private void onGmail(Intent intent) {
        FragmentGmail fragment = new FragmentGmail();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("quick");
        fragmentTransaction.commit();
    }

    private void onOAuth(Intent intent) {
        FragmentOAuth fragment = new FragmentOAuth();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("quick");
        fragmentTransaction.commit();
    }

    private void onQuickSetup(Intent intent) {
        Bundle args = new Bundle();
        args.putInt("title", intent.getIntExtra("title", R.string.title_setup_other));

        FragmentQuickSetup fragment = new FragmentQuickSetup();
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("quick");
        fragmentTransaction.commit();
    }

    private void onQuickPop3(Intent intent) {
        FragmentBase fragment = new FragmentPop();
        fragment.setArguments(new Bundle());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("account");
        fragmentTransaction.commit();
    }

    private void onViewAccounts(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
        fragmentTransaction.commit();
    }

    private void onViewIdentities(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentIdentities()).addToBackStack("identities");
        fragmentTransaction.commit();
    }

    private void onEditAccount(Intent intent) {
        int protocol = intent.getIntExtra("protocol", EntityAccount.TYPE_IMAP);
        FragmentBase fragment;
        switch (protocol) {
            case EntityAccount.TYPE_IMAP:
                fragment = new FragmentAccount();
                break;
            case EntityAccount.TYPE_POP:
                fragment = new FragmentPop();
                break;
            default:
                throw new IllegalArgumentException("Unknown protocol=" + protocol);
        }
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("account");
        fragmentTransaction.commit();
    }

    private void onEditIdentity(Intent intent) {
        FragmentIdentity fragment = new FragmentIdentity();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("identity");
        fragmentTransaction.commit();
    }

    private void onManageLocalContacts(Intent intent) {
        Bundle args = new Bundle();
        // All accounts
        args.putBoolean("junk", intent.getBooleanExtra("junk", false));

        FragmentContacts fragment = new FragmentContacts();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("contacts");
        fragmentTransaction.commit();
    }

    private void onManageCertificates(Intent intent) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentCertificates()).addToBackStack("certificates");
        fragmentTransaction.commit();
    }

    private void onImportCertificate(Intent intent) {
        Intent open = new Intent(Intent.ACTION_GET_CONTENT);
        open.addCategory(Intent.CATEGORY_OPENABLE);
        open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        open.setType("*/*");
        if (open.resolveActivity(getPackageManager()) == null)  // system whitelisted
            Log.unexpectedError(getSupportFragmentManager(),
                    new IllegalArgumentException(getString(R.string.title_no_saf)), 25);
        else
            startActivityForResult(Helper.getChooser(this, open), REQUEST_IMPORT_CERTIFICATE);
    }

    private void onSetupMore(Intent intent) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                String action = intent.getAction();
                if (ACTION_QUICK_GMAIL.equals(action))
                    onGmail(intent);
                else if (ACTION_QUICK_OAUTH.equals(action))
                    onOAuth(intent);
                else if (ACTION_QUICK_SETUP.equals(action))
                    onQuickSetup(intent);
                else if (ACTION_QUICK_POP3.equals(action))
                    onQuickPop3(intent);
                else if (ACTION_VIEW_ACCOUNTS.equals(action))
                    onViewAccounts(intent);
                else if (ACTION_VIEW_IDENTITIES.equals(action))
                    onViewIdentities(intent);
                else if (ACTION_EDIT_ACCOUNT.equals(action))
                    onEditAccount(intent);
                else if (ACTION_EDIT_IDENTITY.equals(action))
                    onEditIdentity(intent);
                else if (ACTION_MANAGE_LOCAL_CONTACTS.equals(action))
                    onManageLocalContacts(intent);
                else if (ACTION_MANAGE_CERTIFICATES.equals(action))
                    onManageCertificates(intent);
                else if (ACTION_IMPORT_CERTIFICATE.equals(action))
                    onImportCertificate(intent);
                else if (ACTION_SETUP_REORDER.equals(action))
                    onMenuOrder(
                            intent.getIntExtra("title", -1),
                            intent.getStringExtra("className"));
                else if (ACTION_SETUP_MORE.equals(action))
                    onSetupMore(intent);
            }
        }
    };
}
