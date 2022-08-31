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
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
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
    static final int REQUEST_EDIT_ACCOUNT_NAME = 6;
    static final int REQUEST_EDIT_ACCOUNT_COLOR = 7;

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
                    Helper.viewFAQ(v.getContext(), 170, true);
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
                        fabCompose, account);
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
        FragmentDialogTheme.setBackground(getContext(), view, false);
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
                    else
                        folders = db.folder().getSynchronizingFolders(aid);

                    if (folders.size() > 0)
                        Collections.sort(folders, folders.get(0).getComparator(context));

                    for (EntityFolder folder : folders) {
                        EntityOperation.sync(context, folder.id, true, force);

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
                    ServiceSynchronize.reload(context, null, true, "refresh");
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
                    Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true);
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
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
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

        LayoutInflater infl = LayoutInflater.from(getContext());
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
        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(new Bundle());

        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("messages");
        fragmentTransaction.commit();
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

        FragmentDialogApply fragment = new FragmentDialogApply();
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
                case REQUEST_EDIT_ACCOUNT_NAME:
                    if (resultCode == RESULT_OK && data != null)
                        onEditAccountName(data.getBundleExtra("args"));
                    break;
                case REQUEST_EDIT_ACCOUNT_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onEditAccountColor(data.getBundleExtra("args"));
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

                List<EntityRule> rules = db.rule().getEnabledRules(fid);
                if (rules == null)
                    return 0;
                EntityLog.log(context, "Executing rules count=" + rules.size());

                for (EntityRule rule : rules) {
                    JSONObject jcondition = new JSONObject(rule.condition);
                    JSONObject jheader = jcondition.optJSONObject("header");
                    if (jheader != null)
                        throw new IllegalArgumentException(context.getString(R.string.title_rule_no_headers));
                }

                List<Long> ids = db.message().getMessageIdsByFolder(fid);
                if (ids == null)
                    return 0;
                EntityLog.log(context, "Executing rules messages=" + ids.size());

                int applied = 0;
                for (long mid : ids)
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(mid);
                        if (message == null)
                            continue;

                        EntityLog.log(context, "Executing rules message=" + message.id);

                        for (EntityRule rule : rules) {
                            EntityLog.log(context, "Executing rules evaluating=" + rule.name);
                            if (rule.matches(context, message, null, null)) {
                                EntityLog.log(context, "Executing rules matches=" + rule.name);
                                if (rule.execute(context, message)) {
                                    EntityLog.log(context, "Executing rules applied=" + rule.name);
                                    applied++;
                                }
                                if (rule.stop)
                                    break;
                            }
                        }

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
                Log.unexpectedError(getParentFragmentManager(), ex, false);
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

                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, "progress")
                                .setSmallIcon(R.drawable.baseline_get_app_white_24)
                                .setContentTitle(getString(R.string.title_export_messages))
                                .setAutoCancel(false)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                                .setLocalOnly(true)
                                .setOngoing(true);

                DB db = DB.getInstance(context);
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.setKeepAwake(true).execute(this, args, "folder:export");
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

    public static class FragmentDialogApply extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_folder_all, null);
            final RadioGroup rgSynchronize = view.findViewById(R.id.rgSynchronize);
            final EditText etSyncDays = view.findViewById(R.id.etSyncDays);
            final EditText etKeepDays = view.findViewById(R.id.etKeepDays);
            final CheckBox cbKeepAll = view.findViewById(R.id.cbKeepAll);
            final CheckBox cbPollSystem = view.findViewById(R.id.cbPollSystem);
            final CheckBox cbPollUser = view.findViewById(R.id.cbPollUser);

            cbKeepAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    etKeepDays.setEnabled(!isChecked);
                }
            });

            return new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = getArguments();
                            int optionId = rgSynchronize.getCheckedRadioButtonId();
                            if (optionId == R.id.rbEnable)
                                args.putBoolean("enable", true);
                            else if (optionId == R.id.rbDisable)
                                args.putBoolean("enable", false);
                            args.putString("sync", etSyncDays.getText().toString());
                            args.putString("keep", cbKeepAll.isChecked()
                                    ? Integer.toString(Integer.MAX_VALUE)
                                    : etKeepDays.getText().toString());
                            args.putBoolean("system", cbPollSystem.isChecked());
                            args.putBoolean("user", cbPollUser.isChecked());

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) throws Throwable {
                                    long aid = args.getLong("account");
                                    Boolean enable = null;
                                    if (args.containsKey("enable"))
                                        enable = args.getBoolean("enable");
                                    String sync = args.getString("sync");
                                    String keep = args.getString("keep");
                                    boolean system = args.getBoolean("system");
                                    boolean user = args.getBoolean("user");

                                    if (TextUtils.isEmpty(sync))
                                        sync = "7";
                                    if (TextUtils.isEmpty(keep))
                                        keep = "30";

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        EntityAccount account = db.account().getAccount(aid);
                                        if (account == null)
                                            return null;

                                        if (system && account.poll_interval > 15)
                                            db.account().setAccountKeepAliveInterval(account.id, 15);

                                        List<EntityFolder> folders = db.folder().getFolders(aid, false, true);
                                        if (folders == null)
                                            return null;

                                        for (EntityFolder folder : folders) {
                                            if (EntityFolder.USER.equals(folder.type)) {
                                                if (enable != null) {
                                                    folder.synchronize = enable;
                                                    db.folder().setFolderSynchronize(folder.id, folder.synchronize);
                                                }

                                                db.folder().setFolderProperties(
                                                        folder.id,
                                                        Integer.parseInt(sync),
                                                        Integer.parseInt(keep));
                                            }

                                            if (folder.synchronize && !folder.poll)
                                                if (EntityFolder.USER.equals(folder.type)
                                                        ? user
                                                        : system && !EntityFolder.INBOX.equals(folder.type))
                                                    db.folder().setFolderPoll(folder.id, true);
                                        }

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    ServiceSynchronize.reload(context, aid, false, "Apply");

                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }
                            }.execute(FragmentDialogApply.this, args, "folders:all");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
