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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import java.text.NumberFormat;
import java.util.Collections;
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
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabCompose;
    private FloatingActionButton fabError;

    private boolean cards;
    private boolean compact;

    private long account;
    private boolean primary;
    private boolean show_hidden = false;
    private boolean show_flagged;
    private AdapterFolder adapter;

    private NumberFormat NF = NumberFormat.getNumberInstance();

    static final int REQUEST_SYNC = 1;
    static final int REQUEST_DELETE_LOCAL = 2;
    static final int REQUEST_EMPTY_FOLDER = 3;
    static final int REQUEST_DELETE_FOLDER = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        account = args.getLong("account", -1);
        primary = args.getBoolean("primary");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);
        compact = prefs.getBoolean("compact_folders", false);
        show_flagged = prefs.getBoolean("flagged_folders", false);

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

        rvFolder.setHasFixedSize(true);
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

        adapter = new AdapterFolder(this, account, primary, compact, show_hidden, show_flagged, null);
        rvFolder.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                startActivity(new Intent(getContext(), ActivityCompose.class)
                        .putExtra("action", "new")
                        .putExtra("account", account)
                );
            }
        });

        fabCompose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bundle args = new Bundle();
                args.putLong("account", account);

                new SimpleTask<EntityFolder>() {
                    @Override
                    protected EntityFolder onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");

                        DB db = DB.getInstance(context);
                        if (account < 0)
                            return db.folder().getPrimaryDrafts();
                        else
                            return db.folder().getFolderByType(account, EntityFolder.DRAFTS);
                    }

                    @Override
                    protected void onExecuted(Bundle args, EntityFolder drafts) {
                        if (drafts == null)
                            return;

                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                        .putExtra("account", drafts.account)
                                        .putExtra("folder", drafts.id)
                                        .putExtra("type", drafts.type));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentFolders.this, args, "folders:drafts");

                return true;
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

        swipeRefresh.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (!prefs.getBoolean("pull", true))
                    return true;
                return rvFolder.canScrollVertically(-1);
            }
        });

        // Initialize

        if (cards && !Helper.isDarkTheme(getContext()))
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightColorBackground_cards));

        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        fabAdd.hide();
        fabCompose.hide();
        fabError.hide();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        grpHintActions.setVisibility(prefs.getBoolean("folder_actions", false) ? View.GONE : View.VISIBLE);
        grpHintSync.setVisibility(prefs.getBoolean("folder_sync", false) ? View.GONE : View.VISIBLE);

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
                    if (account != null && account.quota_usage != null && account.quota_limit != null) {
                        int percent = Math.round((float) account.quota_usage * 100 / account.quota_limit);
                        setSubtitle(getString(R.string.title_name_count,
                                account.name, NF.format(percent) + "%"));
                    } else
                        setSubtitle(account == null ? null : account.name);

                    if (account != null && account.error != null)
                        fabError.show();
                    else
                        fabError.hide();

                    if (!primary) {
                        if (account == null || account.protocol != EntityAccount.TYPE_IMAP)
                            fabAdd.hide();
                        else
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
                boolean force = false;
                boolean outbox = false;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean enabled = prefs.getBoolean("enabled", true);
                int pollInterval = prefs.getInt("poll_interval", ServiceSynchronize.DEFAULT_POLL_INTERVAL);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<EntityFolder> folders;
                    if (aid < 0)
                        folders = db.folder().getFoldersUnified(null, true);
                    else
                        folders = db.folder().getSynchronizingFolders(aid);

                    if (folders.size() > 0)
                        Collections.sort(folders, folders.get(0).getComparator(context));

                    for (EntityFolder folder : folders) {
                        EntityOperation.sync(context, folder.id, true);

                        if (folder.account == null)
                            outbox = true;
                        else {
                            EntityAccount account = db.account().getAccount(folder.account);
                            if (account != null && !"connected".equals(account.state)) {
                                now = false;
                                if (enabled && !account.ondemand &&
                                        (pollInterval == 0 || account.poll_exempted))
                                    force = true;
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (force)
                    ServiceSynchronize.reload(context, null, true, "refresh");
                else
                    ServiceSynchronize.eval(context, "refresh");

                if (outbox)
                    ServiceSend.start(context);

                if (!now)
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
                        public void onClick(View view) {
                            startActivity(
                                    new Intent(getContext(), ActivitySetup.class)
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean subscriptions = prefs.getBoolean("subscriptions", false);
        boolean subscribed_only = prefs.getBoolean("subscribed_only", false);

        menu.findItem(R.id.menu_compact).setChecked(compact);
        menu.findItem(R.id.menu_show_hidden).setChecked(show_hidden);
        menu.findItem(R.id.menu_show_flagged).setChecked(show_flagged);
        menu.findItem(R.id.menu_subscribed_only).setChecked(subscribed_only);
        menu.findItem(R.id.menu_subscribed_only).setVisible(subscriptions);
        menu.findItem(R.id.menu_apply_all).setVisible(account >= 0);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onMenuSearch();
                return true;
            case R.id.menu_compact:
                onMenuCompact();
                return true;
            case R.id.menu_show_hidden:
                onMenuShowHidden();
                return true;
            case R.id.menu_show_flagged:
                onMenuShowFlagged();
                return true;
            case R.id.menu_subscribed_only:
                onMenuSubscribedOnly();
                return true;
            case R.id.menu_apply_all:
                onMenuApplyToAll();
                return true;
            case R.id.menu_force_sync:
                onMenuForceSync();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuSearch() {
        Bundle args = new Bundle();
        args.putLong("account", account);

        FragmentDialogSearch fragment = new FragmentDialogSearch();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "search");
    }

    private void onMenuCompact() {
        compact = !compact;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("compact_folders", compact).apply();

        getActivity().invalidateOptionsMenu();
        adapter.setCompact(compact);
    }

    private void onMenuShowHidden() {
        show_hidden = !show_hidden;
        getActivity().invalidateOptionsMenu();
        adapter.setShowHidden(show_hidden);
    }

    private void onMenuShowFlagged() {
        show_flagged = !show_flagged;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("flagged_folders", show_flagged).apply();

        getActivity().invalidateOptionsMenu();
        adapter.setShowFlagged(show_flagged);
    }

    private void onMenuSubscribedOnly() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean subscribed_only = !prefs.getBoolean("subscribed_only", false);
        prefs.edit().putBoolean("subscribed_only", subscribed_only).apply();
        getActivity().invalidateOptionsMenu();
        adapter.setSubscribedOnly(subscribed_only);
    }

    private void onMenuApplyToAll() {
        Bundle args = new Bundle();
        args.putLong("account", account);

        FragmentDialogApply fragment = new FragmentDialogApply();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "folders:apply");
    }

    private void onMenuForceSync() {
        ServiceSynchronize.reload(getContext(), null, true, "force sync");
        ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_SYNC:
                    if (resultCode == RESULT_OK && data != null)
                        onSync(data.getBundleExtra("args"));
                    break;
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
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSync(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                int months = args.getInt("months", -1);
                long fid = args.getLong("folder");

                if (months < 0 && !ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder folder = db.folder().getFolder(fid);
                    if (folder == null)
                        return null;

                    if (months == 0) {
                        db.folder().setFolderInitialize(folder.id, Integer.MAX_VALUE);
                        db.folder().setFolderKeep(folder.id, Integer.MAX_VALUE);
                    } else if (months > 0) {
                        db.folder().setFolderInitialize(folder.id, months * 30);
                        db.folder().setFolderKeep(folder.id, months * 30);
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

                ServiceSynchronize.eval(context, "refresh/folder");

                if (!now)
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
                        public void onClick(View view) {
                            startActivity(
                                    new Intent(getContext(), ActivitySetup.class)
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
        }.execute(this, args, "folder:sync");
    }

    private void onDeleteLocal(Bundle args) {
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
                long fid = args.getLong("folder");
                boolean browsed = args.getBoolean("browsed");
                Log.i("Delete local messages browsed=" + browsed);

                DB db = DB.getInstance(context);

                try {
                    db.beginTransaction();

                    if (browsed)
                        db.message().deleteBrowsedMessages(fid);
                    else {
                        db.message().deleteLocalMessages(fid);
                        db.folder().setFolderKeywords(fid, DB.Converters.fromStringArray(null));
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
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
                    EntityOperation.sync(context, folder.id, false);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "delete");

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

                ServiceSynchronize.reload(context, folder.account, false, "delete folder");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete");
    }

    public static class FragmentDialogApply extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_folder_all, null);
            final EditText etSyncDays = view.findViewById(R.id.etSyncDays);
            final EditText etKeepDays = view.findViewById(R.id.etKeepDays);
            final CheckBox cbKeepAll = view.findViewById(R.id.cbKeepAll);

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
                            args.putString("sync", etSyncDays.getText().toString());
                            args.putString("keep", cbKeepAll.isChecked()
                                    ? Integer.toString(Integer.MAX_VALUE)
                                    : etKeepDays.getText().toString());

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) throws Throwable {
                                    long account = args.getLong("account");
                                    String sync = args.getString("sync");
                                    String keep = args.getString("keep");

                                    if (TextUtils.isEmpty(sync))
                                        sync = "7";
                                    if (TextUtils.isEmpty(keep))
                                        keep = "30";

                                    DB db = DB.getInstance(context);
                                    db.folder().setFolderProperties(
                                            account,
                                            Integer.parseInt(sync),
                                            Integer.parseInt(keep));

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