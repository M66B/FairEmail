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
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.List;

import static android.app.Activity.RESULT_OK;

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
    private FloatingActionButton fabError;

    private boolean cards;

    private long account;
    private boolean show_hidden = false;
    private String searching = null;
    private AdapterFolder adapter;

    static final int REQUEST_SYNC = 1;
    static final int REQUEST_DELETE_LOCAL = 2;
    static final int REQUEST_EMPTY_TRASH = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        account = args.getLong("account", -1);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);

        setTitle(R.string.page_folders);
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
        fabError = view.findViewById(R.id.fabError);

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

        if (!cards) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    if (view.findViewById(R.id.clItem).getVisibility() == View.GONE)
                        outRect.setEmpty();
                    else
                        super.getItemOffsets(outRect, view, parent, state);
                }
            };
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvFolder.addItemDecoration(itemDecorator);
        }

        adapter = new AdapterFolder(this, account, show_hidden, null);
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

        fabError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivitySetup.class)
                        .putExtra("target", "accounts");
                startActivity(intent);
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
                                            .putExtra("folder", drafts.id)
                                            .putExtra("type", drafts.type));
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentFolders.this, new Bundle(), "folders:drafts");

                    return true;
                }
            });

        // Initialize

        if (cards && !Helper.isDarkTheme(getContext()))
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightColorBackground_cards));

        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        fab.hide();
        fabError.hide();

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        grpHintActions.setVisibility(prefs.getBoolean("folder_actions", false) ? View.GONE : View.VISIBLE);
        grpHintSync.setVisibility(prefs.getBoolean("folder_sync", false) ? View.GONE : View.VISIBLE);

        DB db = DB.getInstance(getContext());

        // Observe account
        if (account < 0) {
            setSubtitle(R.string.title_folders_unified);
            fab.setImageResource(R.drawable.baseline_edit_24);
            fab.show();
        } else
            db.account().liveAccount(account).observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
                @Override
                public void onChanged(@Nullable EntityAccount account) {
                    setSubtitle(account == null ? null : account.name);

                    if (account != null && account.error != null)
                        fabError.show();
                    else
                        fabError.hide();

                    if (account == null || account.pop)
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

                adapter.set(folders);

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

                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (aid < 0) {
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
                    } else {
                        // Folder list
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean enabled = prefs.getBoolean("enabled", true);
                        if (enabled)
                            ServiceSynchronize.reload(context, "refresh folders");
                        else
                            ServiceSynchronize.process(context, true);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_folders, menu);

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
        menu.findItem(R.id.menu_show_hidden).setChecked(show_hidden);
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
        show_hidden = !show_hidden;
        getActivity().invalidateOptionsMenu();
        adapter.setShowHidden(show_hidden);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_SYNC:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onSync(args.getLong("folder"), args.getBoolean("all"));
                    }
                    break;
                case REQUEST_DELETE_LOCAL:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onDeleteLocal(args.getLong("folder"), args.getBoolean("browsed"));
                    }
                    break;
                case REQUEST_EMPTY_TRASH:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onEmptyTrash(args.getLong("folder"));
                    }
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSync(long folder, boolean all) {
        Bundle args = new Bundle();
        args.putBoolean("all", all);
        args.putLong("folder", folder);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                boolean all = args.getBoolean("all");
                long fid = args.getLong("folder");

                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder folder = db.folder().getFolder(fid);
                    if (folder == null)
                        return null;

                    if (all) {
                        db.folder().setFolderInitialize(folder.id, Integer.MAX_VALUE);
                        db.folder().setFolderKeep(folder.id, Integer.MAX_VALUE);
                    }

                    EntityOperation.sync(context, folder.id, true);

                    if (folder.account != null) {
                        EntityAccount account = db.account().getAccount(folder.account);
                        if (account != null && !"connected".equals(account.state))
                            now = false;
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
                            startActivity(
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
        }.execute(this, args, "folder:sync");
    }

    private void onDeleteLocal(long folder, boolean browsed) {
        Bundle args = new Bundle();
        args.putLong("id", folder);
        args.putBoolean("browsed", browsed);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                boolean browsed = args.getBoolean("browsed");
                Log.i("Delete local messages browsed=" + browsed);
                if (browsed)
                    DB.getInstance(context).message().deleteBrowsedMessages(id);
                else
                    DB.getInstance(context).message().deleteLocalMessages(id);
                return null;
            }

            @Override
            public void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete:local");
    }

    private void onEmptyTrash(long folder) {
        Bundle args = new Bundle();
        args.putLong("folder", folder);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long folder = args.getLong("folder");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<Long> ids = db.message().getMessageByFolder(folder);
                    for (Long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message.uid != null || !TextUtils.isEmpty(message.msgid))
                            EntityOperation.queue(context, message, EntityOperation.DELETE);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete");
    }
}