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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FragmentAccounts extends FragmentBase {
    private boolean settings;

    private boolean cards;

    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvAccount;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;
    private FloatingActionButton fabCompose;
    private ObjectAnimator animator;

    private String searching = null;
    private AdapterAccount adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        settings = (args == null || args.getBoolean("settings", true));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_list_accounts);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_accounts, container, false);

        // Get controls
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvAccount = view.findViewById(R.id.rvAccount);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);
        fabCompose = view.findViewById(R.id.fabCompose);

        // Wire controls

        int colorPrimary = Helper.resolveColor(getContext(), R.attr.colorPrimary);
        swipeRefresh.setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE);
        swipeRefresh.setProgressBackgroundColorSchemeColor(colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onSwipeRefresh();
            }
        });
        swipeRefresh.setEnabled(!settings);

        rvAccount.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAccount.setLayoutManager(llm);

        if (!cards) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvAccount.addItemDecoration(itemDecorator);
        }

        adapter = new AdapterAccount(this, settings);
        rvAccount.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), fab);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_imap, 1, R.string.title_imap);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_pop3, 2, R.string.title_pop3);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.string.title_imap:
                                onCreate(true);
                                return true;
                            case R.string.title_pop3:
                                onCreate(false);
                                return true;
                            default:
                                return false;
                        }
                    }

                    private void onCreate(boolean imap) {
                        FragmentBase fragment = imap ? new FragmentAccount() : new FragmentPop();
                        fragment.setArguments(new Bundle());
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("account");
                        fragmentTransaction.commit();
                    }
                });

                popupMenu.show();
            }
        });

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivityCompose.class)
                        .putExtra("action", "new")
                        .putExtra("account", -1L)
                );
            }
        });

        fabCompose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bundle args = new Bundle();

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
                                        .putExtra("folder", drafts.id)
                                        .putExtra("type", drafts.type));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
                }.execute(FragmentAccounts.this, args, "account:drafts");

                return true;
            }
        });

        animator = ObjectAnimator.ofFloat(fab, "alpha", 0.5f, 1.0f);
        animator.setDuration(500L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fab.setAlpha((float) animation.getAnimatedValue());
            }
        });

        // Initialize

        if (cards && !Helper.isDarkTheme(getContext()))
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightColorBackground_cards));

        if (settings) {
            fab.show();
            fabCompose.hide();
        } else {
            fab.hide();
            fabCompose.show();
        }

        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("fair:searching", searching);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
            searching = savedInstanceState.getString("fair:searching");

        DB db = DB.getInstance(getContext());

        // Observe accounts
        db.account().liveAccountsEx(settings)
                .observe(getViewLifecycleOwner(), new Observer<List<TupleAccountEx>>() {
                    @Override
                    public void onChanged(@Nullable List<TupleAccountEx> accounts) {
                        if (accounts == null)
                            accounts = new ArrayList<>();

                        adapter.set(accounts);

                        pbWait.setVisibility(View.GONE);
                        grpReady.setVisibility(View.VISIBLE);

                        if (accounts.size() == 0)
                            animator.start();
                        else
                            animator.end();
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_accounts, menu);

        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchViewEx searchView = (SearchViewEx) menuSearch.getActionView();
        searchView.setup(getViewLifecycleOwner(), menuSearch, searching, new SearchViewEx.ISearch() {
            @Override
            public void onSave(String query) {
                searching = query;
            }

            @Override
            public void onSearch(String query) {
                FragmentMessages.search(
                        getContext(), getViewLifecycleOwner(), getFragmentManager(),
                        -1, false, query);
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_search).setVisible(!settings);

        super.onPrepareOptionsMenu(menu);
    }

    private void onSwipeRefresh() {
        Bundle args = new Bundle();

        new SimpleTask<Void>() {
            @Override
            protected void onPostExecute(Bundle args) {
                swipeRefresh.setRefreshing(false);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    // Unified inbox
                    List<EntityFolder> folders = db.folder().getFoldersSynchronizingUnified(null);
                    for (EntityFolder folder : folders) {
                        EntityOperation.sync(context, folder.id, true);

                        if (folder.account != null) {
                            EntityAccount account = db.account().getAccount(folder.account);
                            if (account != null && !"connected".equals(account.state))
                                now = false;
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (!now)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_connection));

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalStateException) {
                    Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getContext().startActivity(
                                    new Intent(getContext(), ActivitySetup.class)
                                            .putExtra("tab", "connection"));
                        }
                    });
                    snackbar.show();
                } else if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "folders:refresh");
    }
}
