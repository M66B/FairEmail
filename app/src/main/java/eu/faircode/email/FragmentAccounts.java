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
import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FragmentAccounts extends FragmentBase {
    private boolean settings;

    private boolean cards;
    private boolean dividers;
    private boolean compact;
    private boolean show_folders;

    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvHintActions;
    private Button btnGrant;
    private RecyclerView rvAccount;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;
    private FloatingActionButton fabCompose;
    private ObjectAnimator animator;

    private AdapterAccount adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        settings = (args == null || args.getBoolean("settings", true));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);
        dividers = prefs.getBoolean("dividers", true);
        compact = prefs.getBoolean("compact_accounts", false) && !settings;
        show_folders = prefs.getBoolean("folders_accounts", false) && !settings;
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_list_accounts);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_accounts, container, false);

        // Get controls
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvHintActions = view.findViewById(R.id.tvHintActions);
        btnGrant = view.findViewById(R.id.btnGrant);
        rvAccount = view.findViewById(R.id.rvAccount);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);
        fabCompose = view.findViewById(R.id.fabCompose);

        // Wire controls

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
        swipeRefresh.setEnabled(!settings);

        btnGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    requestPermissions(Helper.getOAuthPermissions(), REQUEST_PERMISSIONS);
                } catch (Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }
        });

        rvAccount.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAccount.setLayoutManager(llm);

        if (!cards && dividers) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvAccount.addItemDecoration(itemDecorator);
        }

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

                TupleAccountFolder prev = adapter.getItemAtPosition(pos - 1);
                TupleAccountFolder account = adapter.getItemAtPosition(pos);
                if (pos > 0 && prev == null)
                    return null;
                if (account == null)
                    return null;

                if (pos > 0) {
                    if (Objects.equals(prev.category, account.category))
                        return null;
                } else {
                    if (account.category == null)
                        return null;
                }

                View header = inflater.inflate(R.layout.item_group, parent, false);
                TextView tvCategory = header.findViewById(R.id.tvCategory);
                TextView tvDate = header.findViewById(R.id.tvDate);

                if (cards || !dividers) {
                    View vSeparator = header.findViewById(R.id.vSeparator);
                    vSeparator.setVisibility(View.GONE);
                }

                tvCategory.setText(account.category);
                tvDate.setVisibility(View.GONE);

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }
        };
        rvAccount.addItemDecoration(categoryDecorator);

        adapter = new AdapterAccount(this, settings, compact, show_folders);
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
                        int itemId = item.getItemId();
                        if (itemId == R.string.title_imap) {
                            onCreate(true);
                            return true;
                        } else if (itemId == R.string.title_pop3) {
                            onCreate(false);
                            return true;
                        }
                        return false;
                    }

                    private void onCreate(boolean imap) {
                        FragmentBase fragment = imap ? new FragmentAccount() : new FragmentPop();
                        fragment.setArguments(new Bundle());
                        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
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
                FragmentDialogIdentity.onCompose(
                        getContext(),
                        getViewLifecycleOwner(),
                        getParentFragmentManager(),
                        fabCompose, -1L, -1L);
            }
        });

        fabCompose.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                FragmentDialogIdentity.onDrafts(
                        getContext(),
                        getViewLifecycleOwner(),
                        getParentFragmentManager(),
                        fabCompose, -1L);
                return true;
            }
        });

        animator = Helper.getFabAnimator(fab, getViewLifecycleOwner());

        // Initialize
        if (settings) {
            fab.show();
            fabCompose.hide();
        } else {
            fab.hide();
            fabCompose.show();
        }

        tvHintActions.setVisibility(settings ? View.VISIBLE : View.GONE);
        btnGrant.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context context = getContext();
        final DB db = DB.getInstance(context);

        // Observe accounts
        db.account().liveAccountFolder(settings)
                .observe(getViewLifecycleOwner(), new Observer<List<TupleAccountFolder>>() {
                    @Override
                    public void onChanged(@Nullable List<TupleAccountFolder> accounts) {
                        if (accounts == null)
                            accounts = new ArrayList<>();

                        boolean authorized = true;
                        for (TupleAccountFolder account : accounts)
                            if (account.auth_type != AUTH_TYPE_PASSWORD &&
                                    !Helper.hasPermissions(getContext(), Helper.getOAuthPermissions())) {
                                authorized = false;
                            }
                        btnGrant.setVisibility(authorized ? View.GONE : View.VISIBLE);

                        adapter.set(accounts);

                        pbWait.setVisibility(View.GONE);
                        grpReady.setVisibility(View.VISIBLE);

                        if (accounts.size() == 0) {
                            fab.setCustomSize(Helper.dp2pixels(context, 3 * 56 / 2));
                            if (animator != null && !animator.isStarted())
                                animator.start();
                        } else {
                            fab.clearCustomSize();
                            if (animator != null && animator.isStarted())
                                animator.end();
                        }
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_accounts, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_delete).setVisible(settings);
        menu.findItem(R.id.menu_search).setVisible(!settings);
        menu.findItem(R.id.menu_unified).setVisible(!settings);
        menu.findItem(R.id.menu_outbox).setVisible(!settings);
        menu.findItem(R.id.menu_compact).setChecked(compact);
        menu.findItem(R.id.menu_compact).setVisible(!settings);
        menu.findItem(R.id.menu_show_folders).setChecked(show_folders);
        menu.findItem(R.id.menu_show_folders).setVisible(!settings);
        menu.findItem(R.id.menu_theme).setVisible(!settings);
        menu.findItem(R.id.menu_force_sync).setVisible(!settings);
        menu.findItem(R.id.menu_pwned).setVisible(settings && !TextUtils.isEmpty(BuildConfig.PWNED_ENDPOINT));

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_delete) {
            onMenuDelete();
            return true;
        } else if (itemId == R.id.menu_search) {
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
        } else if (itemId == R.id.menu_show_folders) {
            onMenuShowFolders();
            return true;
        } else if (itemId == R.id.menu_theme) {
            onMenuTheme();
            return true;
        } else if (itemId == R.id.menu_force_sync) {
            onMenuForceSync();
            return true;
        } else if (itemId == R.id.menu_pwned) {
            onMenuPwned();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuDelete() {
        Bundle args = new Bundle();
        args.putBoolean("all", true);

        FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, ActivitySetup.REQUEST_DELETE_ACCOUNT);
        fragment.show(getParentFragmentManager(), "accounts:delete");
    }

    private void onMenuSearch() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        Bundle args = new Bundle();

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
        prefs.edit().putBoolean("compact_accounts", compact).apply();

        invalidateOptionsMenu();
        adapter.setCompact(compact);
        rvAccount.post(new Runnable() {
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

    private void onMenuShowFolders() {
        show_folders = !show_folders;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("folders_accounts", show_folders).apply();

        invalidateOptionsMenu();
        adapter.setShowFolders(show_folders);
    }

    private void onMenuTheme() {
        new FragmentDialogTheme().show(getParentFragmentManager(), "messages:theme");
    }

    private void onMenuForceSync() {
        refresh(true);
        ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
    }

    private void onMenuPwned() {
        new FragmentDialogPwned().show(getParentFragmentManager(), "pawned");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_EDIT_ACCOUNT_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onEditAccountColor(data.getBundleExtra("args"));
                    break;
                case ActivitySetup.REQUEST_DELETE_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onDeleteAccount(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Helper.hasPermissions(getContext(), permissions)) {
            btnGrant.setVisibility(View.GONE);
            ServiceSynchronize.reload(getContext(), null, false, "Permissions regranted");
        }
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

    private void onDeleteAccount(Bundle args) {
        long account = args.getLong("account");
        String name = args.getString("name");

        final Context context = getContext();

        Drawable d = ContextCompat.getDrawable(context, R.drawable.twotone_warning_24);
        d.mutate();
        d.setTint(Helper.resolveColor(context, R.attr.colorWarning));

        new AlertDialog.Builder(context)
                .setIcon(d)
                .setTitle(name)
                .setMessage(R.string.title_account_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLong("id", account);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) throws Throwable {
                                long id = args.getLong("id");

                                DB db = DB.getInstance(context);
                                db.account().deleteAccount(id);

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentAccounts.this, args, "setup:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .show();
    }

    private void onSwipeRefresh() {
        refresh(false);
    }

    private void refresh(boolean force) {
        Bundle args = new Bundle();
        args.putBoolean("force", force);

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
                boolean reload = false;
                boolean outbox = false;
                boolean force = args.getBoolean("force");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean pull_all = prefs.getBoolean("pull_all", false);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    // Unified inbox
                    List<EntityFolder> folders;
                    if (pull_all) {
                        folders = new ArrayList<>();
                        List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
                        if (accounts != null)
                            for (EntityAccount account : accounts) {
                                List<EntityFolder> f = db.folder().getFolders(account.id, false, true);
                                if (f != null)
                                    folders.addAll(f);
                            }
                    } else
                        folders = db.folder().getFoldersUnified(null, true);

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
                    ServiceSynchronize.reload(context, null, force, "refresh");
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
}
