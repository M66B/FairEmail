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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ActivityView extends ActivityBilling implements FragmentManager.OnBackStackChangedListener {
    private View view;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private boolean newIntent = false;
    private long attachment = -1;

    private static final int ATTACHMENT_BUFFER_SIZE = 8192; // bytes

    static final int REQUEST_VIEW = 1;
    static final int REQUEST_UNSEEN = 2;

    static final int REQUEST_ATTACHMENT = 1;
    static final int REQUEST_OPENPGP = 2;

    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_VIEW_MESSAGE = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGE";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";
    static final String ACTION_EDIT_ANSWER = BuildConfig.APPLICATION_ID + ".EDIT_ANSWER";
    static final String ACTION_STORE_ATTACHMENT = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = LayoutInflater.from(this).inflate(R.layout.activity_view, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Helper.resolveColor(this, R.attr.colorDrawerScrim));

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        drawerList = findViewById(R.id.drawer_list);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerItem item = (DrawerItem) parent.getAdapter().getItem(position);
                switch (item.getId()) {
                    case -1:
                        onMenuFolders((long) item.getData());
                        break;
                    case R.string.menu_setup:
                        onMenuSetup();
                        break;
                    case R.string.menu_answers:
                        onMenuAnswers();
                        break;
                    case R.string.menu_operations:
                        onMenuOperations();
                        break;
                    case R.string.menu_legend:
                        onMenuLegend();
                        break;
                    case R.string.menu_faq:
                        onMenuFAQ();
                        break;
                    case R.string.menu_pro:
                        onMenuPro();
                        break;
                    case R.string.menu_privacy:
                        onMenuPrivacy();
                        break;
                    case R.string.menu_about:
                        onMenuAbout();
                        break;
                    case R.string.menu_other:
                        onMenuOtherApps();
                        break;
                }

                drawerLayout.closeDrawer(drawerList);
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        DB.getInstance(this).account().liveAccounts(true).observe(this, new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();

                ArrayAdapterDrawer drawerArray = new ArrayAdapterDrawer(ActivityView.this);

                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                Collections.sort(accounts, new Comparator<EntityAccount>() {
                    @Override
                    public int compare(EntityAccount a1, EntityAccount a2) {
                        return collator.compare(a1.name, a2.name);
                    }
                });

                for (EntityAccount account : accounts)
                    drawerArray.add(new DrawerItem(R.layout.item_drawer, -1, R.drawable.baseline_folder_24, account.name, account.id));

                drawerArray.add(new DrawerItem(R.layout.item_drawer_separator));

                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_settings_applications_24, R.string.menu_setup));
                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_reply_24, R.string.menu_answers));

                if (PreferenceManager.getDefaultSharedPreferences(ActivityView.this).getBoolean("debug", false))
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_list_24, R.string.menu_operations));

                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_help_24, R.string.menu_legend));

                if (getIntentFAQ().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_question_answer_24, R.string.menu_faq));

                Intent pro = getIntentPro();
                if (pro == null || pro.resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_monetization_on_24, R.string.menu_pro));

                if (getIntentPrivacy().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_account_box_24, R.string.menu_privacy));

                drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_info_24, R.string.menu_about));

                if (getIntentOtherApps().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.layout.item_drawer, R.drawable.baseline_get_app_24, R.string.menu_other));

                drawerList.setAdapter(drawerArray);
            }
        });

        if (getSupportFragmentManager().getFragments().size() == 0) {
            Bundle args = new Bundle();
            args.putLong("folder", -1);

            FragmentMessages fragment = new FragmentMessages();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("unified");
            fragmentTransaction.commit();
        }

        if (savedInstanceState != null)
            drawerToggle.setDrawerIndicatorEnabled(savedInstanceState.getBoolean("toggle"));

        new SimpleTask<Long>() {
            @Override
            protected Long onLoad(Context context, Bundle args) throws Throwable {
                File file = new File(context.getCacheDir(), "crash.log");
                if (file.exists()) {
                    // Get version info
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%s: %s\r\n", context.getString(R.string.app_name), BuildConfig.VERSION_NAME));
                    sb.append(String.format("Android: %s (SDK %d)\r\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
                    sb.append("\r\n");

                    // Get device info
                    sb.append(String.format("Brand: %s\r\n", Build.BRAND));
                    sb.append(String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
                    sb.append(String.format("Model: %s\r\n", Build.MODEL));
                    sb.append(String.format("Product: %s\r\n", Build.PRODUCT));
                    sb.append(String.format("Device: %s\r\n", Build.DEVICE));
                    sb.append(String.format("Host: %s\r\n", Build.HOST));
                    sb.append(String.format("Display: %s\r\n", Build.DISPLAY));
                    sb.append(String.format("Id: %s\r\n", Build.ID));
                    sb.append("\r\n");

                    BufferedReader in = null;
                    try {
                        String line;
                        in = new BufferedReader(new FileReader(file));
                        while ((line = in.readLine()) != null)
                            sb.append(line).append("\r\n");
                    } finally {
                        if (in != null)
                            in.close();
                    }

                    file.delete();

                    String body = "<pre>" + sb.toString().replaceAll("\\r?\\n", "<br />") + "</pre>";

                    EntityMessage draft = null;

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityFolder drafts = db.folder().getPrimaryDrafts();
                        if (drafts != null) {
                            draft = new EntityMessage();
                            draft.account = drafts.account;
                            draft.folder = drafts.id;
                            draft.msgid = EntityMessage.generateMessageId();
                            draft.to = new Address[]{Helper.myAddress()};
                            draft.subject = context.getString(R.string.app_name) + " crash log";
                            draft.received = new Date().getTime();
                            draft.seen = false;
                            draft.ui_seen = false;
                            draft.ui_hide = false;
                            draft.ui_found = false;
                            draft.id = db.message().insertMessage(draft);
                            draft.write(context, body);
                        }

                        EntityOperation.queue(db, draft, EntityOperation.ADD);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    EntityOperation.process(context);

                    return (draft == null ? null : draft.id);
                }

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Long id) {
                if (id != null)
                    startActivity(
                            new Intent(ActivityView.this, ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", id));

            }
        }.load(this, new Bundle());

        checkIntent(getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("toggle", drawerToggle.isDrawerIndicatorEnabled());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("setup", false))
            intent.getExtras().remove("setup");
        else
            newIntent = true;
        checkIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_VIEW_MESSAGES);
        iff.addAction(ACTION_VIEW_MESSAGE);
        iff.addAction(ACTION_EDIT_FOLDER);
        iff.addAction(ACTION_EDIT_ANSWER);
        iff.addAction(ACTION_STORE_ATTACHMENT);
        lbm.registerReceiver(receiver, iff);

        if (newIntent) {
            newIntent = false;
            getSupportFragmentManager().popBackStack("unified", 0);
        }
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerList))
            drawerLayout.closeDrawer(drawerList);
        else
            super.onBackPressed();
    }

    @Override
    public void onBackStackChanged() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0)
            finish();
        else {
            if (drawerLayout.isDrawerOpen(drawerList))
                drawerLayout.closeDrawer(drawerList);
            drawerToggle.setDrawerIndicatorEnabled(count == 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case android.R.id.home:
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    getSupportFragmentManager().popBackStack();
                return true;
            default:
                return false;
        }
    }

    private void checkIntent(Intent intent) {
        Log.i(Helper.TAG, "View intent=" + intent + " action=" + intent.getAction());
        String action = intent.getAction();
        if ("unseen".equals(action)) {
            intent.setAction(null);
            setIntent(intent);

            Bundle args = new Bundle();
            args.putLong("time", new Date().getTime());

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long time = args.getLong("time");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        for (EntityAccount account : db.account().getAccounts(true))
                            db.account().setAccountSeenUntil(account.id, time);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Toast.makeText(ActivityView.this, ex.toString(), Toast.LENGTH_LONG).show();
                }
            }.load(this, args);
        }
    }

    private Intent getIntentPrivacy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://email.faircode.eu/privacy/"));
        return intent;
    }

    private Intent getIntentFAQ() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/FAQ.md"));
        return intent;
    }

    private Intent getIntentOtherApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/dev?id=8420080860664580239"));
        return intent;
    }

    private void onMenuFolders(long account) {
        getSupportFragmentManager().popBackStack("unified", 0);

        Bundle args = new Bundle();
        args.putLong("account", account);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folders");
        fragmentTransaction.commit();
    }

    private void onMenuSetup() {
        startActivity(new Intent(ActivityView.this, ActivitySetup.class));
    }

    private void onMenuAnswers() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAnswers()).addToBackStack("answers");
        fragmentTransaction.commit();
    }

    private void onMenuOperations() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentOperations()).addToBackStack("operations");
        fragmentTransaction.commit();
    }

    private void onMenuLegend() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLegend()).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuFAQ() {
        startActivity(getIntentFAQ());
    }

    private void onMenuPro() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
        fragmentTransaction.commit();
    }

    private void onMenuPrivacy() {
        startActivity(getIntentPrivacy());
    }

    private void onMenuAbout() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private void onMenuOtherApps() {
        startActivity(getIntentOtherApps());
    }

    private class DrawerItem {
        private int layout;
        private int id;
        private int icon;
        private String title;
        private Object data;

        DrawerItem(int layout) {
            this.layout = layout;
        }

        DrawerItem(Context context, int layout, int icon, int title) {
            this.layout = layout;
            this.id = title;
            this.icon = icon;
            this.title = context.getString(title);
        }

        DrawerItem(int layout, int id, int icon, String title, Object data) {
            this.layout = layout;
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.data = data;
        }

        public int getId() {
            return this.id;
        }

        public Object getData() {
            return this.data;
        }
    }

    private static class ArrayAdapterDrawer extends ArrayAdapter<DrawerItem> {
        ArrayAdapterDrawer(@NonNull Context context) {
            super(context, -1);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            DrawerItem item = getItem(position);
            View row = LayoutInflater.from(getContext()).inflate(item.layout, null);

            ImageView iv = row.findViewById(R.id.ivItem);
            TextView tv = row.findViewById(R.id.tvItem);

            if (iv != null)
                iv.setImageResource(item.icon);
            if (tv != null)
                tv.setText(item.title);

            return row;
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_VIEW_MESSAGES.equals(intent.getAction()))
                onViewMessages(intent);
            else if (ACTION_VIEW_MESSAGE.equals(intent.getAction()))
                onViewMessage(intent);
            else if (ACTION_EDIT_FOLDER.equals(intent.getAction()))
                onEditFolder(intent);
            else if (ACTION_EDIT_ANSWER.equals(intent.getAction()))
                onEditAnswer(intent);
            else if (ACTION_STORE_ATTACHMENT.equals(intent.getAction()))
                onStoreAttachment(intent);
        }
    };

    private void onViewMessages(Intent intent) {
        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("messages");
        fragmentTransaction.commit();
    }

    private void onViewMessage(Intent intent) {
        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) {
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (EntityMessage tmessage : db.message().getMessageByThread(message.account, message.thread)) {
                        db.message().setMessageUiSeen(tmessage.id, true);
                        EntityOperation.queue(db, tmessage, EntityOperation.SEEN, true);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                EntityOperation.process(context);

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Void result) {
                FragmentMessage fragment = new FragmentMessage();
                fragment.setArguments(args);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("message");
                fragmentTransaction.commit();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Toast.makeText(ActivityView.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(ActivityView.this, intent.getExtras());
    }

    private void onEditFolder(Intent intent) {
        FragmentFolder fragment = new FragmentFolder();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folder");
        fragmentTransaction.commit();
    }

    private void onEditAnswer(Intent intent) {
        FragmentAnswer fragment = new FragmentAnswer();
        fragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("answer");
        fragmentTransaction.commit();
    }

    private void onStoreAttachment(Intent intent) {
        attachment = intent.getLongExtra("id", -1);
        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.setType(intent.getStringExtra("type"));
        create.putExtra(Intent.EXTRA_TITLE, intent.getStringExtra("name"));
        startActivityForResult(create, REQUEST_ATTACHMENT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(Helper.TAG, "View onActivityResult request=" + requestCode + " result=" + resultCode + " data=" + data);
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == REQUEST_ATTACHMENT) {
                Bundle args = new Bundle();
                args.putLong("id", attachment);
                args.putParcelable("uri", data.getData());
                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");
                        Uri uri = args.getParcelable("uri");

                        File file = EntityAttachment.getFile(context, id);

                        ParcelFileDescriptor pfd = null;
                        FileOutputStream fos = null;
                        FileInputStream fis = null;
                        try {
                            pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                            fos = new FileOutputStream(pfd.getFileDescriptor());
                            fis = new FileInputStream(file);

                            byte[] buffer = new byte[ATTACHMENT_BUFFER_SIZE];
                            int read;
                            while ((read = fis.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                        } finally {
                            try {
                                if (pfd != null)
                                    pfd.close();
                            } catch (Throwable ex) {
                                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            }
                            try {
                                if (fos != null)
                                    fos.close();
                            } catch (Throwable ex) {
                                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            }
                            try {
                                if (fis != null)
                                    fis.close();
                            } catch (Throwable ex) {
                                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            }
                        }

                        return null;
                    }

                    @Override
                    protected void onLoaded(Bundle args, Void data) {
                        Toast.makeText(ActivityView.this, R.string.title_attachment_saved, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        Toast.makeText(ActivityView.this, ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }.load(this, args);
            }
    }
}
