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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FragmentFolders extends FragmentBase {
    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private ImageButton ibHintActions;
    private ImageButton ibHintSync;
    private RecyclerView rvFolder;
    private ContentLoadingProgressBar pbWait;
    private Group grpHintActions;
    private Group grpHintSync;
    private Group grpReady;
    private FloatingActionButton fab;

    private long account;
    private boolean searching = false;
    private AdapterFolder adapter;

    private Boolean show_hidden = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        account = args.getLong("account", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_folders, container, false);

        // Get controls
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        ibHintActions = view.findViewById(R.id.ibHintActions);
        ibHintSync = view.findViewById(R.id.ibHintSync);
        rvFolder = view.findViewById(R.id.rvFolder);
        pbWait = view.findViewById(R.id.pbWait);
        grpHintActions = view.findViewById(R.id.grpHintActions);
        grpHintSync = view.findViewById(R.id.grpHintSync);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        int colorPrimary = Helper.resolveColor(getContext(), R.attr.colorPrimary);
        swipeRefresh.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE);
        swipeRefresh.setProgressBackgroundColorSchemeColor(colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onSwipeRefresh();
            }
        });

        ibHintActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("folder_actions", true).apply();
                grpHintActions.setVisibility(View.GONE);
            }
        });

        ibHintSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("folder_sync", true).apply();
                grpHintSync.setVisibility(View.GONE);
            }
        });

        rvFolder.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvFolder.setLayoutManager(llm);

        adapter = new AdapterFolder(getContext(), getViewLifecycleOwner());
        rvFolder.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (account < 0) {
                    startActivity(new Intent(getContext(), ActivityCompose.class)
                            .putExtra("action", "new")
                            .putExtra("account", account)
                    );
                } else {
                    Bundle args = new Bundle();
                    args.putLong("account", account);
                    FragmentFolder fragment = new FragmentFolder();
                    fragment.setArguments(args);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folder");
                    fragmentTransaction.commit();
                }
            }
        });

        if (account < 0)
            fab.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new SimpleTask<EntityFolder>() {
                        @Override
                        protected EntityFolder onExecute(Context context, Bundle args) {
                            return DB.getInstance(context).folder().getPrimaryDrafts();
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityFolder drafts) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("account", drafts.account)
                                            .putExtra("folder", drafts.id));
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                        }
                    }.execute(FragmentFolders.this, new Bundle(), "folders:drafts");

                    return true;
                }
            });

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        fab.hide();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (show_hidden != null)
            outState.putBoolean("show_hidden", show_hidden);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            show_hidden = (Boolean) savedInstanceState.get("show_hidden");
            getActivity().invalidateOptionsMenu();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        grpHintActions.setVisibility(prefs.getBoolean("folder_actions", false) ? View.GONE : View.VISIBLE);
        grpHintSync.setVisibility(prefs.getBoolean("folder_sync", false) ? View.GONE : View.VISIBLE);

        DB db = DB.getInstance(getContext());

        // Observe account
        if (account < 0) {
            setSubtitle(R.string.title_folders_unified);

            fab.setImageResource(R.drawable.baseline_edit_24);

            db.identity().liveComposableIdentities(null).observe(getViewLifecycleOwner(),
                    new Observer<List<TupleIdentityEx>>() {
                        @Override
                        public void onChanged(List<TupleIdentityEx> identities) {
                            if (identities == null || identities.size() == 0)
                                fab.hide();
                            else
                                fab.show();
                        }
                    });
        } else
            db.account().liveAccount(account).observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
                @Override
                public void onChanged(@Nullable EntityAccount account) {
                    setSubtitle(account == null ? null : account.name);
                    if (account == null)
                        fab.hide();
                    else
                        fab.show();
                }
            });

        // Observe folders
        db.folder().liveFolders(account < 0 ? null : account).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleFolderEx> folders) {
                if (folders == null) {
                    finish();
                    return;
                }

                boolean has_hidden = false;
                for (TupleFolderEx folder : folders)
                    if (folder.hide) {
                        has_hidden = true;
                        break;
                    }

                if (has_hidden) {
                    if (show_hidden == null) {
                        show_hidden = false;
                        getActivity().invalidateOptionsMenu();
                    }
                } else {
                    if (show_hidden != null) {
                        show_hidden = null;
                        getActivity().invalidateOptionsMenu();
                    }
                }

                adapter.set(account, show_hidden == null || show_hidden, folders);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onSwipeRefresh() {
        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<Void>() {
            @Override
            protected void onPostExecute(Bundle args) {
                swipeRefresh.setRefreshing(false);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");

                if (!Helper.isConnected(context))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_internet));

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (aid < 0) {
                        // Unified inbox
                        List<EntityFolder> folders = db.folder().getFoldersSynchronizingUnified();
                        for (EntityFolder folder : folders)
                            EntityOperation.sync(context, folder.id);
                    } else {
                        // Folder list
                        EntityAccount account = db.account().getAccount(aid);
                        if (account != null && !"connected".equals(account.state))
                            ServiceUI.fsync(context, aid);
                        else
                            ServiceSynchronize.reload(getContext(), "refresh folders");
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentFolders.this, args, "folders:refresh");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_folders, menu);

        final MenuItem menuSearch = menu.findItem(R.id.menu_search);
        menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searching = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searching = false;
                return true;
            }
        });
        if (searching)
            menuSearch.expandActionView();

        final SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setQueryHint(getString(R.string.title_search_device));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = false;
                menuSearch.collapseActionView();
                FragmentMessages.search(getContext(), getViewLifecycleOwner(), getFragmentManager(), -1, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_search).setVisible(account < 0);

        MenuItem item = menu.findItem(R.id.menu_show_hidden);
        if (show_hidden != null) {
            item.setTitle(show_hidden ? R.string.title_hide_folders : R.string.title_show_folders);
            item.setIcon(show_hidden ? R.drawable.baseline_visibility_off_24 : R.drawable.baseline_visibility_24);
        }
        item.setVisible(show_hidden != null);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_show_hidden:
                onMenuShowHidden();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuShowHidden() {
        if (show_hidden == null)
            show_hidden = false;
        else
            show_hidden = !show_hidden;
        getActivity().invalidateOptionsMenu();
        adapter.showHidden(show_hidden);
    }
}
