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
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabCompose;
    private FloatingActionButton fabError;

    private boolean cards;
    private boolean dividers;
    private boolean compact;

    private long account;
    private boolean unified = false;
    private boolean imap = false;
    private boolean primary;
    private boolean show_hidden = false;
    private boolean show_flagged = false;
    private String searching = null;
    private AdapterFolder adapter;

    private NumberFormat NF = NumberFormat.getNumberInstance();

    static final int REQUEST_DELETE_LOCAL = 1;
    static final int REQUEST_EMPTY_FOLDER = 2;
    static final int REQUEST_DELETE_FOLDER = 3;
    static final int REQUEST_EXECUTE_RULES = 4;
    static final int REQUEST_EXPORT_MESSAGES = 5;
    static final int REQUEST_IMPORT_MESSAGES = 6;
    static final int REQUEST_EDIT_FOLDER_COLOR = 7;
    static final int REQUEST_EDIT_ACCOUNT_NAME = 8;
    static final int REQUEST_EDIT_ACCOUNT_COLOR = 9;
    static final int REQUEST_ALL_READ = 10;

    private static final long EXPORT_PROGRESS_INTERVAL = 5000L; // milliseconds

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        account = args.getLong("account", -1);
        unified = args.getBoolean("unified");
        primary = args.getBoolean("primary");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);
        dividers = prefs.getBoolean("dividers", true);
        compact = prefs.getBoolean("compact_folders", true);
        show_hidden = false; // prefs.getBoolean("hidden_folders", false);
        show_flagged = prefs.getBoolean("flagged_folders", false);

        if (BuildConfig.DEBUG) {
            ViewModelSelected selectedModel =
                    new ViewModelProvider(getActivity()).get(ViewModelSelected.class);
            if (savedInstanceState == null)
                selectedModel.select(null);
        }

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
        fabAdd = view.findViewById(R.id.fabAdd);
        fabCompose = view.findViewById(R.id.fabCompose);
        fabError = view.findViewById(R.id.fabError);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        int c = Helper.resolveColor(getContext(), R.attr.colorInfoForeground);
        swipeRefresh.setColorSchemeColors(c, c, c);
        int colorPrimary = Helper.resolveColor(getContext(), androidx.appcompat.R.attr.colorPrimary);
        swipeRefresh.setProgressBackgroundColorSchemeColor(colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Helper.performHapticFeedback(swipeRefresh, HapticFeedbackConstants.CONFIRM);
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

        if (!cards && dividers) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    View clItem = view.findViewById(R.id.clItem);
                    if (clItem == null || clItem.getVisibility() == View.GONE)
                        outRect.setEmpty();
                    else
                        super.getItemOffsets(outRect, view, parent, state);
                }
            };
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvFolder.addItemDecoration(itemDecorator);
        }

        if (unified) {
            DividerItemDecoration categoryDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
                @Override
                public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    int count = parent.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View view = parent.getChildAt(i);
                        int pos = parent.getChildAdapterPosition(view);

                        View header = getView(view, parent, pos);
                        if (header != null) {
                            canvas.save();
                            canvas.translate(0, parent.getChildAt(i).getTop() - header.getMeasuredHeight());
                            header.draw(canvas);
                            canvas.restore();
                        }
                    }
                }

                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    int pos = parent.getChildAdapterPosition(view);
                    View header = getView(view, parent, pos);
                    if (header == null)
                        outRect.setEmpty();
                    else
                        outRect.top = header.getMeasuredHeight();
                }

                private View getView(View view, RecyclerView parent, int pos) {
                    if (pos == NO_POSITION)
                        return null;

                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return null;

                    TupleFolderEx prev = adapter.getItemAtPosition(pos - 1);
                    TupleFolderEx account = adapter.getItemAtPosition(pos);
                    if (pos > 0 && prev == null)
                        return null;
                    if (account == null)
                        return null;

                    if (pos > 0) {
                        if (Objects.equals(prev.accountCategory, account.accountCategory))
                            return null;
                    } else {
                        if (account.accountCategory == null)
                            return null;
                    }

                    View header = inflater.inflate(R.layout.item_group, parent, false);
                    TextView tvCategory = header.findViewById(R.id.tvCategory);
                    TextView tvDate = header.findViewById(R.id.tvDate);

                    if (cards || !dividers) {
                        View vSeparator = header.findViewById(R.id.vSeparator);
                        vSeparator.setVisibility(View.GONE);
                    }

                    tvCategory.setText(account.accountCategory);
                    tvDate.setVisibility(View.GONE);

                    header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                    return header;
                }
            };
            rvFolder.addItemDecoration(categoryDecorator);
        }

        adapter = new AdapterFolder(this, account, unified, primary, compact, show_hidden, show_flagged, null);
        rvFolder.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean pop = (Boolean) v.getTag();
                if (pop != null && pop) {
                    Helper.viewFAQ(v.getContext(), 170);
                    //ToastEx.makeText(v.getContext(), R.string.title_pop_folders, Toast.LENGTH_LONG).show();
                    return;
                }

                Bundle args = new Bundle();
                args.putLong("account", account);
                FragmentFolder fragment = new FragmentFolder();
                fragment.setArguments(args);
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folder");
                fragmentTransaction.commit();
            }
        });

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentDialogIdentity.onCompose(
                        getContext(),
                        getViewLifecycleOwner(),
                        getParentFragmentManager(),
                        fabCompose, account, -1L);
            }
        });

        fabCompose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                FragmentDialogIdentity.onDrafts(
                        getContext(),
                        getViewLifecycleOwner(),
                        getParentFragmentManager(),
                        fabCompose, account);
                return true;
            }
        });

        fabError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putLong("id", account);

                new SimpleTask<EntityAccount>() {
                    @Override
                    protected EntityAccount onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");

                        DB db = DB.getInstance(context);
                        return db.account().getAccount(id);
                    }

                    @Override
                    protected void onExecuted(Bundle args, EntityAccount account) {
                        if (account == null)
                            return;

                        String title = getString(R.string.title_notification_failed, account.name);

                        Intent intent = new Intent(getContext(), ActivityError.class);
                        intent.putExtra("title", title);
                        intent.putExtra("message", account.error);
                        intent.putExtra("provider", account.provider);
                        intent.putExtra("account", account.id);
                        intent.putExtra("protocol", account.protocol);
                        intent.putExtra("auth_type", account.auth_type);
                        intent.putExtra("host", account.host);
                        intent.putExtra("address", account.user);
                        intent.putExtra("faq", 22);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentFolders.this, args, "folders:error");
            }
        });

        swipeRefresh.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (!prefs.getBoolean("pull", true))
                    return true;
                return rvFolder.canScrollVertically(-1);
            }
        });

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        fabAdd.hide();
        fabCompose.hide();
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
        adapter.search(searching);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean folder_actions = prefs.getBoolean("folder_actions", false);
        boolean folder_sync = prefs.getBoolean("folder_sync", false);

        grpHintActions.setVisibility(folder_actions ? View.GONE : View.VISIBLE);
        grpHintSync.setVisibility(folder_sync ? View.GONE : View.VISIBLE);

        DB db = DB.getInstance(getContext());

        if (account < 0 || primary)
            fabCompose.show();

        // Observe account
        if (account < 0)
            setSubtitle(primary ? R.string.title_folder_primary : R.string.title_folders_unified);
        else
            db.account().liveAccount(account).observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
                @Override
                public void onChanged(@Nullable EntityAccount account) {
                    imap = (account != null && account.protocol == EntityAccount.TYPE_IMAP);

                    if (account == null)
                        setSubtitle(null);
                    else {
                        Integer percent = account.getQuotaPercentage();
                        if (percent == null)
                            setSubtitle(account.name);
                        else
                            setSubtitle(getString(R.string.title_name_count,
                                    account.name, NF.format(percent) + "%"));
                    }

                    if (account != null && account.error != null)
                        fabError.show();
                    else
                        fabError.hide();

                    if (account == null || primary)
                        fabAdd.hide();
                    else {
                        fabAdd.setTag(!imap);
                        fabAdd.show();
                    }
                }
            });

        // Observe folders
        db.folder().liveFolders(account < 0 ? null : account, primary).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
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
        refresh(false);
    }

    private void refresh(boolean force) {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putBoolean("primary", primary);
        args.putBoolean("force", force);

        new SimpleTask<Void>() {
            @Override
            protected void onPostExecute(Bundle args) {
                swipeRefresh.setRefreshing(false);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");
                boolean primary = args.getBoolean("primary");

                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;
                boolean reload = false;
                boolean outbox = false;
                boolean force = args.getBoolean("force");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean pull_all = prefs.getBoolean("pull_all", false);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (primary) {
                        EntityAccount account = db.account().getPrimaryAccount();
                        if (account != null)
                            aid = account.id;
                    }

                    List<EntityFolder> folders;
                    if (aid < 0)
                        folders = db.folder().getFoldersUnified(null, true);
                    else {
                        if (pull_all)
                            folders = db.folder().getFolders(aid, false, true);
                        else
                            folders = db.folder().getSynchronizingFolders(aid);
                    }

                    if (folders.size() > 0)
                        Collections.sort(folders, folders.get(0).getComparator(context));

                    for (EntityFolder folder : folders) {
                        if (EntityOperation.sync(context, folder.id, true, force))
                            reload = true;

                        if (folder.account == null)
                            outbox = true;
                        else {
                            EntityAccount account = db.account().getAccount(folder.account);
                            if (account != null && !"connected".equals(account.state)) {
                                now = false;
                                if (!account.isTransient(context))
                                    reload = true;
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (force || reload)
                    ServiceSynchronize.reload(context, aid < 0 ? null : aid, force, "refresh");
                else
                    ServiceSynchronize.eval(context, "refresh");

                if (outbox)
                    ServiceSend.start(context);

                if (!now && !force)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_connection));

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalStateException) {
                    Snackbar snackbar = Helper.setSnackbarOptions(
                            Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG));
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("tab", "connection"));
                        }
                    });
                    snackbar.show();
                } else if (ex instanceof IllegalArgumentException)
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                            .show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folders:refresh");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_folders, menu);

        MenuItem menuSearch = menu.findItem(R.id.menu_search_folder);
        SearchView searchView = (SearchView) menuSearch.getActionView();

        if (searchView != null)
            searchView.setQueryHint(getString(R.string.title_search));

        final String search = searching;
        view.post(new RunnableEx("folders:search") {
            @Override
            public void delegate() {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                if (TextUtils.isEmpty(search))
                    menuSearch.collapseActionView();
                else {
                    menuSearch.expandActionView();
                    searchView.setQuery(search, true);
                }
            }
        });

        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                menuSearch.collapseActionView();
                getViewLifecycleOwner().getLifecycle().removeObserver(this);
            }
        });

        if (searchView != null)
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        searching = newText;
                        adapter.search(newText);
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        searching = query;
                        adapter.search(query);
                    }
                    return true;
                }
            });

        ActionBar actionBar = getSupportActionBar();
        Context actionBarContext = (actionBar == null ? getContext() : actionBar.getThemedContext());
        LayoutInflater infl = LayoutInflater.from(actionBarContext);

        ImageButton ibSearch = (ImageButton) infl.inflate(R.layout.action_button, null);
        ibSearch.setId(View.generateViewId());
        ibSearch.setImageResource(R.drawable.twotone_search_24);
        ibSearch.setContentDescription(getString(R.string.title_legend_search));
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMenuSearch();
            }
        });
        ibSearch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onMenuSearchFolder(menu.findItem(R.id.menu_search_folder));
                return true;
            }
        });
        menu.findItem(R.id.menu_search).setActionView(ibSearch);

        MenuCompat.setGroupDividerEnabled(menu, true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean subscriptions = prefs.getBoolean("subscriptions", false);
        boolean subscribed_only = prefs.getBoolean("subscribed_only", false);
        boolean sort_unread_atop = prefs.getBoolean("sort_unread_atop", false);

        menu.findItem(R.id.menu_unified).setVisible(account < 0 || primary);
        menu.findItem(R.id.menu_outbox).setVisible(account < 0 || primary);
        menu.findItem(R.id.menu_compact).setChecked(compact);
        menu.findItem(R.id.menu_theme).setVisible(account < 0 || primary);
        menu.findItem(R.id.menu_show_hidden).setChecked(show_hidden);
        menu.findItem(R.id.menu_show_flagged).setChecked(show_flagged);
        menu.findItem(R.id.menu_subscribed_only)
                .setChecked(subscribed_only)
                .setVisible(subscriptions);
        menu.findItem(R.id.menu_sort_unread_atop).setChecked(sort_unread_atop);
        menu.findItem(R.id.menu_apply_all).setVisible(account >= 0 && imap);
        menu.findItem(R.id.menu_edit_account_name).setVisible(account >= 0);
        menu.findItem(R.id.menu_edit_account_color).setVisible(account >= 0);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_search) {
            onMenuSearch();
            return true;
        } else if (itemId == R.id.menu_unified) {
            onMenuUnified();
            return true;
        } else if (itemId == R.id.menu_outbox) {
            onMenuOutbox();
            return true;
        } else if (itemId == R.id.menu_compact) {
            onMenuCompact();
            return true;
        } else if (itemId == R.id.menu_theme) {
            onMenuTheme();
            return true;
        } else if (itemId == R.id.menu_show_hidden) {
            onMenuShowHidden();
            return true;
        } else if (itemId == R.id.menu_show_flagged) {
            onMenuShowFlagged();
            return true;
        } else if (itemId == R.id.menu_subscribed_only) {
            onMenuSubscribedOnly();
            return true;
        } else if (itemId == R.id.menu_sort_unread_atop) {
            onMenuSortUnreadAtop();
            return true;
        } else if (itemId == R.id.menu_search_folder) {
            onMenuSearchFolder(item);
            return true;
        } else if (itemId == R.id.menu_apply_all) {
            onMenuApplyToAll();
            return true;
        } else if (itemId == R.id.menu_edit_account_name) {
            onMenuEditAccount();
            return true;
        } else if (itemId == R.id.menu_edit_account_color) {
            onMenuEditColor();
            return true;
        } else if (itemId == R.id.menu_force_sync) {
            onMenuForceSync();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuSearch() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        Bundle args = new Bundle();
        args.putLong("account", account);

        FragmentDialogSearch fragment = new FragmentDialogSearch();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "search");
    }

    private void onMenuUnified() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.sendBroadcast(
                new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                        .putExtra("unified", true));
    }

    private void onMenuOutbox() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.sendBroadcast(new Intent(ActivityView.ACTION_VIEW_OUTBOX));
    }

    private void onMenuCompact() {
        compact = !compact;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("compact_folders", compact).apply();

        invalidateOptionsMenu();
        adapter.setCompact(compact);
        rvFolder.post(new Runnable() {
            @Override
            public void run() {
                try {
                    adapter.notifyDataSetChanged();
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private void onMenuTheme() {
        new FragmentDialogTheme().show(getParentFragmentManager(), "messages:theme");
    }

    private void onMenuShowHidden() {
        show_hidden = !show_hidden;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("hidden_folders", show_hidden).apply();

        invalidateOptionsMenu();
        adapter.setShowHidden(show_hidden);
    }

    private void onMenuShowFlagged() {
        show_flagged = !show_flagged;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("flagged_folders", show_flagged).apply();

        invalidateOptionsMenu();
        adapter.setShowFlagged(show_flagged);
        rvFolder.post(new Runnable() {
            @Override
            public void run() {
                try {
                    adapter.notifyDataSetChanged();
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private void onMenuSubscribedOnly() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean subscribed_only = !prefs.getBoolean("subscribed_only", false);
        prefs.edit().putBoolean("subscribed_only", subscribed_only).apply();
        invalidateOptionsMenu();
        adapter.setSubscribedOnly(subscribed_only);
    }

    private void onMenuSortUnreadAtop() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean sort_unread_atop = !prefs.getBoolean("sort_unread_atop", false);
        prefs.edit().putBoolean("sort_unread_atop", sort_unread_atop).apply();
        invalidateOptionsMenu();
        adapter.setSortUnreadAtop(sort_unread_atop);
    }

    private void onMenuSearchFolder(MenuItem item) {
        if (item.isActionViewExpanded())
            item.collapseActionView();
        else
            item.expandActionView();
    }

    private void onMenuApplyToAll() {
        Bundle args = new Bundle();
        args.putLong("account", account);

        FragmentDialogFoldersApply fragment = new FragmentDialogFoldersApply();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "folders:apply");
    }

    private void onMenuEditAccount() {
        Bundle args = new Bundle();
        args.putLong("id", account);

        new SimpleTask<EntityAccount>() {
            @Override
            protected EntityAccount onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                DB db = DB.getInstance(context);
                return db.account().getAccount(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityAccount account) {
                if (account == null)
                    return;

                args.putString("name", account.name);
                args.putBoolean("primary", account.primary);

                FragmentDialogEditName fragment = new FragmentDialogEditName();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentFolders.this, REQUEST_EDIT_ACCOUNT_NAME);
                fragment.show(getParentFragmentManager(), "account:name");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "account:name");
    }

    private void onMenuEditColor() {
        Bundle args = new Bundle();
        args.putLong("id", account);

        new SimpleTask<EntityAccount>() {
            @Override
            protected EntityAccount onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                return db.account().getAccount(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityAccount account) {
                if (account == null)
                    return;

                args.putInt("color", account.color == null ? Color.TRANSPARENT : account.color);
                args.putString("title", getString(R.string.title_color));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentFolders.this, REQUEST_EDIT_ACCOUNT_COLOR);
                fragment.show(getParentFragmentManager(), "edit:color");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "edit:color");
    }

    private void onMenuForceSync() {
        refresh(true);
        ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_DELETE_LOCAL:
                    if (resultCode == RESULT_OK && data != null)
                        onDeleteLocal(data.getBundleExtra("args"));
                    break;
                case REQUEST_EMPTY_FOLDER:
                    if (resultCode == RESULT_OK && data != null)
                        onEmptyFolder(data.getBundleExtra("args"));
                    break;
                case REQUEST_DELETE_FOLDER:
                    if (resultCode == RESULT_OK && data != null)
                        onDeleteFolder(data.getBundleExtra("args"));
                    break;
                case REQUEST_EXECUTE_RULES:
                    if (resultCode == RESULT_OK && data != null)
                        onExecuteRules(data.getBundleExtra("args"));
                    break;
                case REQUEST_EXPORT_MESSAGES:
                    if (resultCode == RESULT_OK && data != null)
                        onExportMessages(data.getData());
                    break;
                case REQUEST_IMPORT_MESSAGES:
                    if (resultCode == RESULT_OK && data != null)
                        onImportMessages(data.getData());
                    break;
                case REQUEST_EDIT_FOLDER_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onEditFolderColor(data.getBundleExtra("args"));
                    break;
                case REQUEST_EDIT_ACCOUNT_NAME:
                    if (resultCode == RESULT_OK && data != null)
                        onEditAccountName(data.getBundleExtra("args"));
                    break;
                case REQUEST_EDIT_ACCOUNT_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onEditAccountColor(data.getBundleExtra("args"));
                    break;
                case REQUEST_ALL_READ:
                    if (resultCode == RESULT_OK && data != null)
                        onMarkAllRead(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onDeleteLocal(Bundle args) {
        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");
                boolean browsed = args.getBoolean("browsed");
                Log.i("Delete local messages browsed=" + browsed);

                DB db = DB.getInstance(context);

                try {
                    db.beginTransaction();

                    if (browsed) {
                        EntityFolder folder = db.folder().getFolder(fid);
                        if (folder == null)
                            return null;

                        int keep_days = folder.keep_days;
                        if (keep_days == folder.sync_days &&
                                keep_days != Integer.MAX_VALUE)
                            keep_days++;

                        Calendar cal_keep = Calendar.getInstance();
                        cal_keep.add(Calendar.DAY_OF_MONTH, -keep_days);
                        cal_keep.set(Calendar.HOUR_OF_DAY, 0);
                        cal_keep.set(Calendar.MINUTE, 0);
                        cal_keep.set(Calendar.SECOND, 0);
                        cal_keep.set(Calendar.MILLISECOND, 0);

                        long keep_time = cal_keep.getTimeInMillis();
                        if (keep_time < 0)
                            keep_time = 0;

                        db.message().deleteBrowsedMessages(fid, keep_time);
                    } else {
                        db.message().deleteLocalMessages(fid);
                        db.folder().setFolderKeywords(fid, DB.Converters.fromStringArray(null));
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                WorkerCleanup.cleanup(context, false);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete:local");
    }

    private void onEmptyFolder(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");
                String type = args.getString("type");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder folder = db.folder().getFolder(fid);
                    if (folder == null)
                        return null;

                    if (!folder.type.equals(type))
                        throw new IllegalStateException("Invalid folder type=" + type);

                    EntityAccount account = db.account().getAccount(folder.account);
                    if (account == null)
                        return null;

                    EntityLog.log(context,
                            "Empty account=" + account.name + " folder=" + folder.name + " count=" + folder.total);

                    List<Long> ids = db.message().getMessageByFolder(folder.id);
                    for (Long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        if (message.uid != null || account.protocol == EntityAccount.TYPE_POP)
                            db.message().setMessageUiHide(message.id, true);
                    }

                    EntityOperation.queue(context, folder, EntityOperation.PURGE);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "purge");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete");
    }

    private void onDeleteFolder(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                EntityFolder folder;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    folder = db.folder().getFolder(id);
                    if (folder == null)
                        return null;

                    db.folder().setFolderTbd(folder.id);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.reload(context, folder.account, true, "delete folder");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete");
    }

    private void onExecuteRules(Bundle args) {
        new SimpleTask<Integer>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Integer onExecute(Context context, Bundle args) throws JSONException, MessagingException, IOException {
                long fid = args.getLong("id");

                DB db = DB.getInstance(context);

                List<EntityRule> rules = db.rule().getEnabledRules(fid, null);
                if (rules == null)
                    return 0;
                EntityLog.log(context, "Executing rules count=" + rules.size());

                List<Long> ids = db.message().getMessageIdsByFolder(fid);
                if (ids == null)
                    return 0;

                EntityLog.log(context, "Executing rules messages=" + ids.size());

                // Check header conditions
                for (long mid : ids) {
                    EntityMessage message = db.message().getMessage(mid);
                    if (message == null || message.ui_hide)
                        continue;
                    for (EntityRule rule : rules)
                        rule.matches(context, message, null, null);
                }

                int applied = 0;
                for (long mid : ids)
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(mid);
                        if (message == null || message.ui_hide)
                            continue;

                        EntityLog.log(context, "Executing rules message=" + message.id);
                        applied = EntityRule.run(context, rules, message, false, null, null);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                EntityLog.log(context, "Executing rules applied=" + applied);

                if (applied > 0)
                    ServiceSynchronize.eval(context, "rules/manual");

                return applied;
            }

            @Override
            protected void onExecuted(Bundle args, Integer applied) {
                ToastEx.makeText(getContext(),
                        getString(R.string.title_rule_applied, applied),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean report = !(ex instanceof IllegalArgumentException);
                Log.unexpectedError(getParentFragmentManager(), ex, report, 71);
            }
        }.execute(this, args, "folder:rules");
    }

    private void onExportMessages(Uri uri) {
        long id = getArguments().getLong("selected_folder", -1L);

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putParcelable("uri", uri);

        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Export uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(fid);
                if (folder == null)
                    return null;
                EntityAccount account = db.account().getAccount(folder.account);
                if (account == null)
                    return null;

                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, "progress")
                                .setSmallIcon(R.drawable.twotone_archive_24)
                                .setContentTitle(getString(R.string.title_export_messages))
                                .setContentText(account.name + ":" + folder.name)
                                .setAutoCancel(false)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                                .setLocalOnly(true)
                                .setOngoing(true);

                List<Long> ids = db.message().getMessageIdsByFolder(fid);
                if (ids == null)
                    return null;

                String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
                SimpleDateFormat df = new SimpleDateFormat(PATTERN_ASCTIME, Locale.US);

                Properties props = MessageHelper.getSessionProperties(true);
                Session isession = Session.getInstance(props, null);

                // https://www.ietf.org/rfc/rfc4155.txt (Appendix A)
                // http://qmail.org./man/man5/mbox.html
                long last = new Date().getTime();
                ContentResolver resolver = context.getContentResolver();
                OutputStream os = resolver.openOutputStream(uri);
                if (os == null)
                    throw new FileNotFoundException(uri.toString());
                try (OutputStream out = new BufferedOutputStream(os)) {
                    for (int i = 0; i < ids.size(); i++)
                        try {
                            long now = new Date().getTime();
                            if (now - last > EXPORT_PROGRESS_INTERVAL) {
                                last = now;
                                builder.setProgress(ids.size(), i, false);
                                Notification notification = builder.build();
                                notification.flags |= Notification.FLAG_NO_CLEAR;
                                if (NotificationHelper.areNotificationsEnabled(nm))
                                    nm.notify("export", NotificationHelper.NOTIFICATION_TAGGED, notification);
                            }

                            long id = ids.get(i);
                            EntityMessage message = db.message().getMessage(id);
                            if (message == null)
                                continue;

                            String email = null;
                            if (message.from != null && message.from.length > 0)
                                email = ((InternetAddress) message.from[0]).getAddress();
                            if (TextUtils.isEmpty(email))
                                email = "MAILER-DAEMON";

                            out.write(("From " + email + " " + df.format(message.received) + "\n").getBytes());

                            Message imessage = null;

                            if (Boolean.TRUE.equals(message.raw))
                                try (InputStream is = new FileInputStream(message.getRawFile(context))) {
                                    imessage = new MimeMessage(isession, is);
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }

                            if (imessage == null)
                                imessage = MessageHelper.from(context, message, null, isession, false);

                            imessage.writeTo(new FilterOutputStream(out) {
                                private boolean cr = false;
                                private ByteArrayOutputStream buffer = new ByteArrayOutputStream(998);

                                @Override
                                public void write(int b) throws IOException {
                                    if (b == 13 /* CR */) {
                                        if (cr) // another
                                            line();
                                        cr = true;
                                    } else if (b == 10 /* LF */) {
                                        line();
                                    } else {
                                        if (cr) // dangling
                                            line();
                                        buffer.write(b);
                                    }
                                }

                                @Override
                                public void flush() throws IOException {
                                    if (buffer.size() > 0 || cr /* dangling */)
                                        line();
                                    out.write(10);
                                    super.flush();
                                }

                                private void line() throws IOException {
                                    byte[] b = buffer.toByteArray();

                                    int i = 0;
                                    for (; i < b.length; i++)
                                        if (b[i] != '>')
                                            break;

                                    if (i + 4 < b.length &&
                                            b[i + 0] == 'F' &&
                                            b[i + 1] == 'r' &&
                                            b[i + 2] == 'o' &&
                                            b[i + 3] == 'm' &&
                                            b[i + 4] == ' ')
                                        out.write('>');

                                    for (i = 0; i < b.length; i++)
                                        out.write(b[i]);

                                    out.write(10);

                                    buffer.reset();
                                    cr = false;
                                }
                            });
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                } finally {
                    nm.cancel("export", NotificationHelper.NOTIFICATION_TAGGED);
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean report = !(ex instanceof IllegalArgumentException);
                Log.unexpectedError(getParentFragmentManager(), ex, report);
            }
        }.setKeepAwake(true).execute(this, args, "folder:export");
    }

    private void onImportMessages(Uri uri) {
        long id = getArguments().getLong("selected_folder", -1L);

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putParcelable("uri", uri);

        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Import uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(fid);
                if (folder == null)
                    return null;
                EntityAccount account = db.account().getAccount(folder.account);
                if (account == null)
                    return null;

                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, "progress")
                                .setSmallIcon(R.drawable.twotone_unarchive_24)
                                .setContentTitle(getString(R.string.title_import_messages))
                                .setContentText(account.name + ":" + folder.name)
                                .setAutoCancel(false)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                                .setLocalOnly(true)
                                .setOngoing(true)
                                .setProgress(0, 0, true);
                nm.notify("import", NotificationHelper.NOTIFICATION_TAGGED, builder.build());

                Properties props = MessageHelper.getSessionProperties(true);
                Session isession = Session.getInstance(props, null);

                // https://www.ietf.org/rfc/rfc4155.txt (Appendix A)
                // http://qmail.org./man/man5/mbox.html
                ContentResolver resolver = context.getContentResolver();
                InputStream is = resolver.openInputStream(uri);
                if (is == null)
                    throw new FileNotFoundException(uri.toString());

                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    final ObjectHolder<String> line = new ObjectHolder<>(br.readLine());
                    if (line.value == null || !line.value.startsWith("From "))
                        throw new IllegalArgumentException("Invalid mbox file");

                    while ((line.value = br.readLine()) != null) {
                        line.value += "\n";

                        try {
                            MimeMessage imessage = new MimeMessage(isession, new InputStream() {
                                private int i = 0;

                                @Override
                                public int read() throws IOException {
                                    if (line.value == null)
                                        return -1;

                                    if (i >= line.value.length()) {
                                        line.value = br.readLine();
                                        if (line.value == null)
                                            return -1;
                                        if (line.value.startsWith(">From "))
                                            line.value = line.value.substring(1);
                                        line.value += "\n";
                                        i = 0;
                                    }

                                    if (line.value.startsWith("From "))
                                        return -1;

                                    return line.value.charAt(i++);
                                }
                            });

                            MessageHelper helper = new MessageHelper(imessage, context);

                            String msgid = helper.getPOP3MessageID();

                            int count = db.message().countMessageByMsgId(folder.id, msgid, true);
                            if (count == 1) {
                                EntityLog.log(context, "Import: message exists msgid=" + msgid);
                                continue;
                            }

                            Long sent = helper.getSent();
                            long received = helper.getPOP3Received();

                            String[] authentication = helper.getAuthentication();
                            MessageHelper.MessageParts parts = helper.getMessageParts();

                            EntityMessage message = new EntityMessage();
                            message.account = folder.account;
                            message.folder = folder.id;
                            message.uid = null;
                            message.msgid = msgid;
                            message.hash = helper.getHash();
                            message.references = TextUtils.join(" ", helper.getReferences());
                            message.inreplyto = helper.getInReplyTo();
                            message.deliveredto = helper.getDeliveredTo();
                            message.thread = helper.getThreadId(context, account.id, folder.id, 0, received);
                            message.priority = helper.getPriority();
                            message.sensitivity = helper.getSensitivity();
                            message.auto_submitted = helper.getAutoSubmitted();
                            message.receipt_request = helper.getReceiptRequested();
                            message.receipt_to = helper.getReceiptTo();
                            message.bimi_selector = helper.getBimiSelector();
                            message.tls = helper.getTLS();
                            message.dkim = MessageHelper.getAuthentication("dkim", authentication);
                            message.spf = MessageHelper.getAuthentication("spf", authentication);
                            if (message.spf == null)
                                message.spf = helper.getSPF();
                            message.dmarc = MessageHelper.getAuthentication("dmarc", authentication);
                            message.auth = MessageHelper.getAuthentication("auth", authentication);
                            message.smtp_from = helper.getMailFrom(authentication);
                            message.return_path = helper.getReturnPath();
                            message.submitter = helper.getSubmitter();
                            message.from = helper.getFrom();
                            message.to = helper.getTo();
                            message.cc = helper.getCc();
                            message.bcc = helper.getBcc();
                            message.reply = helper.getReply();
                            message.list_post = helper.getListPost();
                            message.unsubscribe = helper.getListUnsubscribe();
                            message.headers = helper.getHeaders();
                            message.infrastructure = helper.getInfrastructure();
                            message.subject = helper.getSubject();
                            message.size = parts.getBodySize();
                            message.total = helper.getSize();
                            message.content = false;
                            message.encrypt = parts.getEncryption();
                            message.ui_encrypt = message.encrypt;
                            message.received = received;
                            message.sent = sent;
                            message.seen = true;
                            message.answered = false;
                            message.flagged = false;
                            message.flags = null;
                            message.keywords = new String[0];
                            message.ui_seen = true;
                            message.ui_answered = false;
                            message.ui_flagged = false;
                            message.ui_hide = false;
                            message.ui_found = false;
                            message.ui_ignored = true;
                            message.ui_browsed = false;
                            message.ui_busy = Long.MAX_VALUE;

                            if (message.deliveredto != null)
                                try {
                                    Address deliveredto = new InternetAddress(message.deliveredto);
                                    if (MessageHelper.equalEmail(new Address[]{deliveredto}, message.to))
                                        message.deliveredto = null;
                                } catch (AddressException ex) {
                                    Log.w(ex);
                                }

                            if (MessageHelper.equalEmail(message.submitter, message.from))
                                message.submitter = null;

                            if (message.size == null && message.total != null)
                                message.size = message.total;

                            EntityIdentity identity = Core.matchIdentity(context, folder, message);
                            message.identity = (identity == null ? null : identity.id);

                            message.sender = MessageHelper.getSortKey(message.from);
                            Uri lookupUri = ContactInfo.getLookupUri(message.from);
                            message.avatar = (lookupUri == null ? null : lookupUri.toString());

                            message.from_domain = (message.checkFromDomain(context) == null);

                            try {
                                db.beginTransaction();

                                message.id = db.message().insertMessage(message);
                                EntityLog.log(context, account.name + " Import added id=" + message.id +
                                        " msgid=" + message.msgid);

                                int sequence = 1;
                                for (EntityAttachment attachment : parts.getAttachments()) {
                                    Log.i(account.name + " Import attachment seq=" + sequence +
                                            " name=" + attachment.name + " type=" + attachment.type +
                                            " cid=" + attachment.cid + " pgp=" + attachment.encryption +
                                            " size=" + attachment.size);
                                    attachment.message = message.id;
                                    attachment.sequence = sequence++;
                                    attachment.id = db.attachment().insertAttachment(attachment);
                                }

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            String body = parts.getHtml(context, false);

                            File file = message.getFile(context);
                            Helper.writeText(file, body);
                            String text = HtmlHelper.getFullText(context, body);
                            message.preview = HtmlHelper.getPreview(text);
                            message.language = HtmlHelper.getLanguage(context, message.subject, text);
                            db.message().setMessageContent(message.id,
                                    true,
                                    message.language,
                                    parts.isPlainOnly(false),
                                    message.preview,
                                    parts.getWarnings(message.warning));

                            try {
                                for (EntityAttachment attachment : parts.getAttachments())
                                    if (attachment.subsequence == null)
                                        parts.downloadAttachment(context, attachment, folder);
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);

                            EntityLog.log(context, "Import error=" + Log.formatThrowable(ex, false));

                            // Resync
                            while (line.value != null && !line.value.startsWith("From "))
                                line.value = br.readLine();
                        }

                        if (line.value == null)
                            break;
                    }
                } finally {
                    nm.cancel("import", NotificationHelper.NOTIFICATION_TAGGED);
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean report = !(ex instanceof IllegalArgumentException);
                Log.unexpectedError(getParentFragmentManager(), ex, report);
            }
        }.setKeepAwake(true).execute(this, args, "folder:export");
    }

    private void onEditFolderColor(Bundle args) {
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                boolean children = args.getBoolean("children");
                Integer color = args.getInt("color");

                if (color == Color.TRANSPARENT)
                    color = null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (children)
                        for (EntityFolder folder : EntityFolder.getChildFolders(context, id))
                            db.folder().setFolderColor(folder.id, color);
                    else
                        db.folder().setFolderColor(id, color);

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
        }.execute(this, args, "edit:color");
    }

    private void onEditAccountName(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                String name = args.getString("name");
                boolean primary = args.getBoolean("primary");

                if (TextUtils.isEmpty(name))
                    return null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityAccount account = db.account().getAccount(id);
                    if (account == null)
                        return null;

                    db.account().setAccountName(account.id, name);
                    if (primary)
                        db.account().resetPrimary();
                    db.account().setAccountPrimary(account.id, primary);

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
        }.execute(this, args, "edit:name");
    }

    private void onEditAccountColor(Bundle args) {
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                Integer color = args.getInt("color");

                if (color == Color.TRANSPARENT)
                    color = null;

                DB db = DB.getInstance(context);
                db.account().setAccountColor(id, color);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "edit:color");
    }

    private void onMarkAllRead(Bundle args) {
        FragmentMessages.markAllRead(this,
                args.getString("type"), args.getLong("folder"), AdapterMessage.ViewType.FOLDER);
    }
}
