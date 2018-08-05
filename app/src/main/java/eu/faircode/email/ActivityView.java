package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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

import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class ActivityView extends ActivityBase implements FragmentManager.OnBackStackChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private ExecutorService executor = Executors.newCachedThreadPool();

    static final int LOADER_ACCOUNT_PUT = 1;
    static final int LOADER_IDENTITY_PUT = 2;
    static final int LOADER_FOLDER_PUT = 3;
    static final int LOADER_MESSAGES_INIT = 4;
    static final int LOADER_MESSAGE_INIT = 5;
    static final int LOADER_MESSAGE_MOVE = 6;

    static final int REQUEST_VIEW = 1;

    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_VIEW_MESSAGE = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGE";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view);

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
                    case R.string.menu_folders:
                        onMenuFolders();
                        break;
                    case R.string.menu_setup:
                        onMenuSetup();
                        break;
                    case R.string.menu_debug:
                        onMenuDebug();
                        break;
                }

                if (!item.isCheckable())
                    drawerLayout.closeDrawer(drawerList);
            }
        });

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        updateDrawer();

        if (getSupportFragmentManager().getFragments().size() == 0)
            init();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_VIEW_MESSAGES);
        iff.addAction(ACTION_VIEW_MESSAGE);
        iff.addAction(ACTION_EDIT_FOLDER);
        lbm.registerReceiver(receiver, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view, menu);
        return true;
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
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        super.onSharedPreferenceChanged(prefs, key);
        if ("eula".equals(key))
            init();
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finish();
    }

    private void syncState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean eula = prefs.getBoolean("eula", false);
        drawerToggle.setDrawerIndicatorEnabled(eula);
        getSupportActionBar().setDisplayHomeAsUpEnabled(eula);
        getSupportActionBar().setHomeButtonEnabled(eula);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.menu_folders:
                onMenuFolders();
                return true;
            default:
                return false;
        }
    }

    private void init() {
        syncState();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("eula", false)) {
            Bundle args = new Bundle();
            args.putLong("folder", -1);

            FragmentMessages fragment = new FragmentMessages();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("unified");
            fragmentTransaction.commit();

            Fragment eula = getSupportFragmentManager().findFragmentByTag("eula");
            if (eula != null)
                getSupportFragmentManager().beginTransaction().remove(eula).commit();

            DB.getInstance(this).account().liveAccounts(true).observe(this, new Observer<List<EntityAccount>>() {
                @Override
                public void onChanged(@Nullable List<EntityAccount> accounts) {
                    if (accounts.size() == 0)
                        startActivity(new Intent(ActivityView.this, ActivitySetup.class));
                    else
                        ServiceSynchronize.start(ActivityView.this);
                }
            });
        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentEula(), "eula");
            fragmentTransaction.commit();
        }
    }

    public void updateDrawer() {
        ArrayAdapterDrawer drawerArray = new ArrayAdapterDrawer(this, R.layout.item_drawer);
        drawerArray.add(new DrawerItem(ActivityView.this, R.string.menu_setup));
        drawerArray.add(new DrawerItem(ActivityView.this, R.string.menu_debug));
        drawerList.setAdapter(drawerArray);
    }

    private void onMenuFolders() {
        getSupportFragmentManager().popBackStack("unified", 0);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentFolders()).addToBackStack("folders");
        fragmentTransaction.commit();
    }

    private void onMenuSetup() {
        startActivity(new Intent(ActivityView.this, ActivitySetup.class));
    }

    private void onMenuDebug() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(ActivityView.this);
                    EntityFolder drafts = db.folder().getPrimaryDraftFolder();
                    if (drafts != null) {
                        StringBuilder info = Helper.getDebugInfo();
                        info.insert(0, getString(R.string.title_debug_info) + "\n\n\n\n");

                        Address to = new InternetAddress("marcel+email@faircode.eu", "FairCode");

                        EntityMessage draft = new EntityMessage();
                        draft.account = drafts.account;
                        draft.folder = drafts.id;
                        draft.to = MessageHelper.encodeAddresses(new Address[]{to});
                        draft.subject = BuildConfig.APPLICATION_ID + " debug info";
                        draft.body = "<pre>" + info.toString().replaceAll("\\r?\\n", "<br />") + "</pre>";
                        draft.received = new Date().getTime();
                        draft.seen = false;
                        draft.ui_seen = false;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);

                        EntityOperation.queue(ActivityView.this, draft, EntityOperation.ADD);

                        startActivity(new Intent(ActivityView.this, ActivityCompose.class)
                                .putExtra("id", draft.id));
                    }
                } catch (Throwable ex) {
                    Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        });
    }

    private class DrawerItem {
        private int id;
        private String title;
        private boolean checkable;
        private boolean checked;

        DrawerItem(Context context, int title) {
            this.id = title;
            this.title = context.getString(title);
            this.checkable = false;
            this.checked = false;
        }

        DrawerItem(Context context, int title, boolean checked) {
            this.id = title;
            this.title = context.getString(title);
            this.checkable = true;
            this.checked = checked;
        }

        public int getId() {
            return this.id;
        }

        public String getTitle() {
            return this.title;
        }

        public boolean isCheckable() {
            return this.checkable;
        }

        public boolean isChecked() {
            return this.checked;
        }
    }

    private static class ArrayAdapterDrawer extends ArrayAdapter<DrawerItem> {
        private int resource;

        ArrayAdapterDrawer(@NonNull Context context, int resource) {
            super(context, resource);
            this.resource = resource;
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row;
            if (null == convertView)
                row = LayoutInflater.from(getContext()).inflate(this.resource, null);
            else
                row = convertView;

            DrawerItem item = getItem(position);

            TextView tv = row.findViewById(R.id.tvItem);
            CheckBox cb = row.findViewById(R.id.cbItem);
            tv.setText(item.getTitle());
            cb.setVisibility(item.isCheckable() ? View.VISIBLE : View.GONE);
            cb.setChecked(item.isChecked());

            return row;
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_VIEW_MESSAGES.equals(intent.getAction())) {
                getSupportFragmentManager().popBackStack("unified", 0);

                FragmentMessages fragment = new FragmentMessages();
                fragment.setArguments(intent.getExtras());
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("messages");
                fragmentTransaction.commit();
            } else if (ACTION_VIEW_MESSAGE.equals(intent.getAction())) {
                FragmentMessage fragment = new FragmentMessage();
                fragment.setArguments(intent.getExtras());
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("message");
                fragmentTransaction.commit();
            } else if (ACTION_EDIT_FOLDER.equals(intent.getAction())) {
                FragmentFolder fragment = new FragmentFolder();
                fragment.setArguments(intent.getExtras());
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folder");
                fragmentTransaction.commit();
            }
        }
    };
}
