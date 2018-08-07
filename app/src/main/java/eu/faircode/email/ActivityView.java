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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ActivityView extends ActivityBase implements FragmentManager.OnBackStackChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean newIntent = false;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    static final int LOADER_ACCOUNT_PUT = 1;
    static final int LOADER_IDENTITY_PUT = 2;
    static final int LOADER_FOLDER_PUT = 3;
    static final int LOADER_MESSAGES_INIT = 4;
    static final int LOADER_MESSAGE_MOVE = 5;

    static final int REQUEST_VIEW = 1;

    static final String ACTION_VIEW_MESSAGES = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGES";
    static final String ACTION_VIEW_MESSAGE = BuildConfig.APPLICATION_ID + ".VIEW_MESSAGE";
    static final String ACTION_EDIT_FOLDER = BuildConfig.APPLICATION_ID + ".EDIT_FOLDER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Helper.TAG, "View create");
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
                    case -1:
                        onMenuFolders((long) item.getData());
                        break;
                    case R.string.menu_setup:
                        onMenuSetup();
                        break;
                    case R.string.menu_faq:
                        onMenuFAQ();
                        break;
                    case R.string.menu_about:
                        onMenuAbout();
                        break;
                }

                drawerLayout.closeDrawer(drawerList);
            }
        });

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        DB.getInstance(this).account().liveAccounts().observe(this, new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                ArrayAdapterDrawer drawerArray = new ArrayAdapterDrawer(ActivityView.this, R.layout.item_drawer);

                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                Collections.sort(accounts, new Comparator<EntityAccount>() {
                    @Override
                    public int compare(EntityAccount a1, EntityAccount a2) {
                        return collator.compare(a1.name, a2.name);
                    }
                });

                for (EntityAccount account : accounts)
                    drawerArray.add(new DrawerItem(-1, R.drawable.baseline_folder_24, account.name, account.id));

                drawerArray.add(new DrawerItem(ActivityView.this, R.drawable.baseline_settings_applications_24, R.string.menu_setup));
                if (getIntentFAQ().resolveActivity(getPackageManager()) != null)
                    drawerArray.add(new DrawerItem(ActivityView.this, R.drawable.baseline_question_answer_24, R.string.menu_faq));
                drawerArray.add(new DrawerItem(ActivityView.this, R.drawable.baseline_help_24, R.string.menu_about));

                drawerList.setAdapter(drawerArray);
            }
        });

        if (getSupportFragmentManager().getFragments().size() == 0)
            init();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.i(Helper.TAG, "View post create");
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
        syncState();
    }

    private void syncState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean eula = prefs.getBoolean("eula" , false);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        drawerToggle.setDrawerIndicatorEnabled(count == 1 && eula);
    }

    @Override
    protected void onResume() {
        Log.i(Helper.TAG, "View resume");
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_VIEW_MESSAGES);
        iff.addAction(ACTION_VIEW_MESSAGE);
        iff.addAction(ACTION_EDIT_FOLDER);
        lbm.registerReceiver(receiver, iff);

        if (newIntent) {
            newIntent = false;
            getSupportFragmentManager().popBackStack("unified" , 0);
        }
    }

    @Override
    protected void onPause() {
        Log.i(Helper.TAG, "View pause");
        super.onPause();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(Helper.TAG, "View new intent=" + intent);
        newIntent = true;
        super.onNewIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(Helper.TAG, "View configuration changed");
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
        syncState();
    }

    @Override
    protected void onDestroy() {
        Log.i(Helper.TAG, "View destroyed");
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
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
        syncState();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        super.onSharedPreferenceChanged(prefs, key);
        if ("eula".equals(key))
            init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                return true;
            default:
                return false;
        }
    }

    private void init() {
        invalidateOptionsMenu();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("eula" , false)) {
            getSupportFragmentManager().popBackStack(); // eula

            Bundle args = new Bundle();
            args.putLong("folder" , -1);

            FragmentMessages fragment = new FragmentMessages();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("unified");
            fragmentTransaction.commit();

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
            fragmentTransaction.replace(R.id.content_frame, new FragmentEula()).addToBackStack("eula");
            fragmentTransaction.commit();
        }
    }

    private Intent getIntentFAQ() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/FAQ.md"));
        return intent;
    }

    private void onMenuFolders(long account) {
        getSupportFragmentManager().popBackStack("unified" , 0);

        Bundle args = new Bundle();
        args.putLong("account" , account);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folders");
        fragmentTransaction.commit();
    }

    private void onMenuSetup() {
        startActivity(new Intent(ActivityView.this, ActivitySetup.class));
    }

    private void onMenuFAQ() {
        startActivity(getIntentFAQ());
    }

    private void onMenuAbout() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private class DrawerItem {
        private int id;
        private int icon;
        private String title;
        private Object data;

        DrawerItem(Context context, int icon, int title) {
            this.id = title;
            this.icon = icon;
            this.title = context.getString(title);
        }

        DrawerItem(int id, int icon, String title, Object data) {
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

            ImageView iv = row.findViewById(R.id.ivItem);
            TextView tv = row.findViewById(R.id.tvItem);

            iv.setImageResource(item.icon);
            tv.setText(item.title);

            return row;
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_VIEW_MESSAGES.equals(intent.getAction())) {
                getSupportFragmentManager().popBackStack("unified" , 0);

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
