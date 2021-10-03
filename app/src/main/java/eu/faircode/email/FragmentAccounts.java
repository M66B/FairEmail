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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
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

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

public class FragmentAccounts extends FragmentBase {
    private boolean settings;

    private boolean cards;

    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private Button btnGrant;
    private RecyclerView rvAccount;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;
    private FloatingActionButton fabCompose;
    private ObjectAnimator animator;

    private AdapterAccount adapter;

    private static final int REQUEST_IMPORT_OAUTH = 1;

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
        btnGrant = view.findViewById(R.id.btnGrant);
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

        btnGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    requestPermissions(Helper.getOAuthPermissions(), REQUEST_IMPORT_OAUTH);
                } catch (Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }
        });

        rvAccount.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAccount.setLayoutManager(llm);

        if (!cards) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvAccount.addItemDecoration(itemDecorator);
        }

        DividerItemDecoration dateDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
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

                TupleAccountEx prev = adapter.getItemAtPosition(pos - 1);
                TupleAccountEx account = adapter.getItemAtPosition(pos);
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

                View header = inflater.inflate(R.layout.item_message_date, parent, false);
                TextView tvDate = header.findViewById(R.id.tvDate);

                if (cards) {
                    View vSeparatorDate = header.findViewById(R.id.vSeparatorDate);
                    vSeparatorDate.setVisibility(View.GONE);
                }

                tvDate.setText(account.category);

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }
        };
        rvAccount.addItemDecoration(dateDecorator);

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
                        fabCompose, -1L);
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

        animator = ObjectAnimator.ofFloat(fab, "alpha", 0.5f, 1.0f);
        animator.setDuration(750L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;
                fab.setAlpha((float) animation.getAnimatedValue());
            }
        });

        // Initialize
        FragmentDialogTheme.setBackground(getContext(), view, false);

        if (settings) {
            fab.show();
            fabCompose.hide();
        } else {
            fab.hide();
            fabCompose.show();
        }

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
        db.account().liveAccountsEx(settings)
                .observe(getViewLifecycleOwner(), new Observer<List<TupleAccountEx>>() {
                    @Override
                    public void onChanged(@Nullable List<TupleAccountEx> accounts) {
                        if (accounts == null)
                            accounts = new ArrayList<>();

                        boolean authorized = true;
                        for (TupleAccountEx account : accounts)
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
                            if (!animator.isStarted())
                                animator.start();
                        } else {
                            fab.clearCustomSize();
                            if (animator.isStarted())
                                animator.end();
                        }
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_accounts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_search).setVisible(!settings);
        menu.findItem(R.id.menu_unified).setVisible(!settings);
        menu.findItem(R.id.menu_theme).setVisible(!settings);
        menu.findItem(R.id.menu_force_sync).setVisible(!settings);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_search) {
            onMenuSearch();
            return true;
        } else if (itemId == R.id.menu_unified) {
            onMenuUnified();
            return true;
        } else if (itemId == R.id.menu_theme) {
            onMenuTheme();
            return true;
        } else if (itemId == R.id.menu_force_sync) {
            onMenuForceSync();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuSearch() {
        Bundle args = new Bundle();

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

    private void onMenuTheme() {
        new FragmentDialogTheme().show(getParentFragmentManager(), "messages:theme");
    }

    private void onMenuForceSync() {
        refresh(true);
        ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_IMPORT_OAUTH)
            if (Helper.hasPermissions(getContext(), permissions)) {
                btnGrant.setVisibility(View.GONE);
                ServiceSynchronize.reload(getContext(), null, false, "Permissions regranted");
            }
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
                boolean force = args.getBoolean("force");
                boolean outbox = false;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    // Unified inbox
                    List<EntityFolder> folders = db.folder().getFoldersUnified(null, true);

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

                if (!now && !args.getBoolean("force"))
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
}
