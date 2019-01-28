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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FragmentMessages extends FragmentBase {
    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvSupport;
    private ImageButton ibHintSupport;
    private ImageButton ibHintSwipe;
    private ImageButton ibHintSelect;
    private TextView tvNoEmail;
    private FixedRecyclerView rvMessage;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpSupport;
    private Group grpHintSupport;
    private Group grpHintSwipe;
    private Group grpHintSelect;
    private Group grpReady;
    private FloatingActionButton fab;
    private FloatingActionButton fabMore;

    private long account;
    private long folder;
    private String thread;
    private long id;
    private String search;

    private boolean threading;
    private boolean pull;
    private boolean actionbar;
    private boolean autoclose;
    private boolean autonext;
    private boolean addresses;

    private long primary = -1;
    private boolean outbox = false;
    private boolean connected = false;
    private boolean searching = false;
    private AdapterMessage adapter;

    private AdapterMessage.ViewType viewType;
    private SelectionTracker<Long> selectionTracker = null;

    private int autoCloseCount = 0;
    private boolean autoExpand = true;
    private Map<String, List<Long>> values = new HashMap<>();
    private LongSparseArray<Spanned> bodies = new LongSparseArray<>();
    private LongSparseArray<TupleAccountSwipes> accountSwipes = new LongSparseArray<>();

    private BoundaryCallbackMessages boundaryCallback = null;

    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private final int action_seen = 1;
    private final int action_unseen = 2;
    private final int action_snooze = 3;
    private final int action_flag = 4;
    private final int action_unflag = 5;
    private final int action_archive = 6;
    private final int action_trash = 7;
    private final int action_delete = 8;
    private final int action_junk = 9;
    private final int action_move = 10;

    private static final int LOCAL_PAGE_SIZE = 100;
    private static final int REMOTE_PAGE_SIZE = 10;
    private static final int UNDO_TIMEOUT = 5000; // milliseconds

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        account = args.getLong("account", -1);
        folder = args.getLong("folder", -1);
        thread = args.getString("thread");
        id = args.getLong("id", -1);
        search = args.getString("search");

        if (TextUtils.isEmpty(search))
            if (thread == null)
                if (folder < 0)
                    viewType = AdapterMessage.ViewType.UNIFIED;
                else
                    viewType = AdapterMessage.ViewType.FOLDER;
            else
                viewType = AdapterMessage.ViewType.THREAD;
        else
            viewType = AdapterMessage.ViewType.SEARCH;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER)
            pull = prefs.getBoolean("pull", true);
        else
            pull = false;

        threading = prefs.getBoolean("threading", true);
        actionbar = prefs.getBoolean("actionbar", true);
        autoclose = prefs.getBoolean("autoclose", true);
        autonext = prefs.getBoolean("autonext", false);
        addresses = prefs.getBoolean("addresses", true);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_messages, container, false);

        setHasOptionsMenu(true);

        // Get controls
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvSupport = view.findViewById(R.id.tvSupport);
        ibHintSupport = view.findViewById(R.id.ibHintSupport);
        ibHintSwipe = view.findViewById(R.id.ibHintSwipe);
        ibHintSelect = view.findViewById(R.id.ibHintSelect);
        tvNoEmail = view.findViewById(R.id.tvNoEmail);
        rvMessage = view.findViewById(R.id.rvMessage);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpSupport = view.findViewById(R.id.grpSupport);
        grpHintSupport = view.findViewById(R.id.grpHintSupport);
        grpHintSwipe = view.findViewById(R.id.grpHintSwipe);
        grpHintSelect = view.findViewById(R.id.grpHintSelect);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);
        fabMore = view.findViewById(R.id.fabMore);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

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

        tvSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                fragmentTransaction.commit();
            }
        });

        ibHintSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("app_support", true).apply();
                grpHintSupport.setVisibility(View.GONE);
            }
        });

        ibHintSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("message_swipe", true).apply();
                grpHintSwipe.setVisibility(View.GONE);
            }
        });

        ibHintSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("message_select", true).apply();
                grpHintSelect.setVisibility(View.GONE);
            }
        });

        rvMessage.setHasFixedSize(false);
        //rvMessage.setItemViewCacheSize(10);
        //rvMessage.getRecycledViewPool().setMaxRecycledViews(0, 10);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvMessage.setLayoutManager(llm);

        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        String sort = prefs.getString("sort", "time");

        adapter = new AdapterMessage(
                getContext(), getViewLifecycleOwner(),
                viewType, compact, zoom, sort, iProperties);

        rvMessage.setAdapter(adapter);

        if (viewType == AdapterMessage.ViewType.THREAD) {
            ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getViewLifecycleOwner(), thread, new ViewModelMessages.IPrevNext() {
                @Override
                public void onPrevious(Long id) {
                    bottom_navigation.getMenu().findItem(R.id.action_prev).setIntent(new Intent().putExtra("id", id));
                    bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(id != null);
                }

                @Override
                public void onNext(Long id) {
                    bottom_navigation.getMenu().findItem(R.id.action_next).setIntent(new Intent().putExtra("id", id));
                    bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(id != null);
                }
            });
        } else {
            final SelectionPredicateMessage predicate = new SelectionPredicateMessage(rvMessage);

            selectionTracker = new SelectionTracker.Builder<>(
                    "messages-selection",
                    rvMessage,
                    new ItemKeyProviderMessage(rvMessage),
                    new ItemDetailsLookupMessage(rvMessage),
                    StorageStrategy.createLongStorage())
                    .withSelectionPredicate(predicate)
                    .build();
            adapter.setSelectionTracker(selectionTracker);

            selectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
                @Override
                public void onSelectionChanged() {
                    SelectionTracker tracker = selectionTracker;
                    if (tracker == null)
                        return;

                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        try {
                            ViewModelMessages modelMessages = ViewModelProviders.of(activity).get(ViewModelMessages.class);
                            if (tracker.hasSelection())
                                modelMessages.removeObservers(viewType, getViewLifecycleOwner());
                            else
                                modelMessages.observe(viewType, getViewLifecycleOwner(), observer);
                        } catch (IllegalStateException ex) {
                            // getViewLifecycleOwner
                            Log.w(ex);
                        }

                        activity.invalidateOptionsMenu();
                    }

                    if (tracker.hasSelection()) {
                        swipeRefresh.setEnabled(false);
                        fabMore.show();
                    } else {
                        fabMore.hide();
                        swipeRefresh.setEnabled(pull);
                    }
                }
            });
        }

        new ItemTouchHelper(touchHelper).attachToRecyclerView(rvMessage);

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        onActionMove(EntityFolder.TRASH);
                        return true;

                    case R.id.action_archive:
                        onActionMove(EntityFolder.ARCHIVE);
                        return true;

                    case R.id.action_prev:
                        navigate(menuItem.getIntent().getLongExtra("id", -1));
                        return true;

                    case R.id.action_next:
                        navigate(menuItem.getIntent().getLongExtra("id", -1));
                        return true;

                    default:
                        return false;
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EntityFolder drafts = (EntityFolder) fab.getTag();

                startActivity(new Intent(getContext(), ActivityCompose.class)
                        .putExtra("action", "new")
                        .putExtra("account", drafts.account)
                );
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EntityFolder drafts = (EntityFolder) fab.getTag();

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("account", drafts.account)
                                .putExtra("folder", drafts.id));

                return true;
            }
        });

        fabMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMore();
            }
        });

        ((ActivityBase) getActivity()).addBackPressedListener(onBackPressedListener);

        // Initialize
        swipeRefresh.setEnabled(pull);
        tvNoEmail.setVisibility(View.GONE);
        bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(false);
        bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(false);
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        fab.hide();
        fabMore.hide();

        return view;
    }

    @Override
    public void onDestroy() {
        selectionTracker = null;
        super.onDestroy();
    }

    private void onSwipeRefresh() {
        Bundle args = new Bundle();
        args.putLong("folder", folder);

        new SimpleTask<Boolean>() {
            @Override
            protected Boolean onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");

                boolean connected = false;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<EntityFolder> folders = new ArrayList<>();
                    if (fid < 0) {
                        List<EntityFolder> unified = db.folder().getFoldersSynchronizingUnified();
                        if (unified != null)
                            folders.addAll(unified);
                    } else {
                        EntityFolder folder = db.folder().getFolder(fid);
                        if (folder != null)
                            folders.add(folder);
                    }

                    for (EntityFolder folder : folders) {
                        EntityOperation.sync(db, folder.id);

                        if (folder.account == null) { // outbox
                            if ("connected".equals(folder.state))
                                connected = true;
                        } else {
                            EntityAccount account = db.account().getAccount(folder.account);
                            if ("connected".equals(account.state))
                                connected = true;
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return connected;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean connected) {
                if (!connected) {
                    swipeRefresh.setRefreshing(false);
                    Snackbar.make(view, R.string.title_sync_queued, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:refresh");
    }

    private AdapterMessage.IProperties iProperties = new AdapterMessage.IProperties() {
        @Override
        public void setValue(String name, long id, boolean enabled) {
            if (!values.containsKey(name))
                values.put(name, new ArrayList<Long>());
            if (enabled) {
                values.get(name).add(id);
                if ("expanded".equals(name))
                    handleExpand(id);
            } else
                values.get(name).remove(id);
        }

        @Override
        public boolean getValue(String name, long id) {
            if (values.containsKey(name))
                return values.get(name).contains(id);
            else if ("addresses".equals(name))
                return !addresses;
            return false;
        }

        @Override
        public void setBody(long id, Spanned body) {
            if (body == null)
                bodies.remove(id);
            else
                bodies.put(id, body);
        }

        @Override
        public Spanned getBody(long id) {
            return bodies.get(id);
        }

        @Override
        public void scrollTo(final int pos, final int dy) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    rvMessage.scrollToPosition(pos);
                    rvMessage.scrollBy(0, dy);
                }
            });
        }

        @Override
        public void move(long id, String name, boolean type) {
            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putString("name", name);
            args.putBoolean("type", type);

            new SimpleTask<ArrayList<MessageTarget>>() {
                @Override
                protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    String name = args.getString("name");
                    boolean type = args.getBoolean("type");

                    ArrayList<MessageTarget> result = new ArrayList<>();

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);

                        EntityFolder target = null;
                        if (message != null)
                            if (type)
                                target = db.folder().getFolderByType(message.account, name);
                            else
                                target = db.folder().getFolderByName(message.account, name);

                        if (target != null) {
                            EntityAccount account = db.account().getAccount(target.account);
                            result.add(new MessageTarget(message, account, target));
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                    moveAsk(result);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(FragmentMessages.this, args, "messages:move");
        }
    };

    private ItemTouchHelper.Callback touchHelper = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            TupleMessageEx message = getMessage(viewHolder);
            if (message == null)
                return 0;

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null)
                return 0;

            int flags = 0;
            if (swipes.swipe_left != null && !swipes.swipe_left.equals(message.folder))
                flags |= ItemTouchHelper.LEFT;
            if (swipes.swipe_right != null && !swipes.swipe_right.equals(message.folder))
                flags |= ItemTouchHelper.RIGHT;

            return makeMovementFlags(0, flags);
        }

        @Override
        public boolean onMove(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onChildDraw(
                @NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            AdapterMessage.ViewHolder holder = ((AdapterMessage.ViewHolder) viewHolder);
            holder.setDisplacement(dX);

            TupleMessageEx message = getMessage(viewHolder);
            if (message == null)
                return;

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null)
                return;

            Rect rect = holder.getItemRect();
            int margin = Helper.dp2pixels(getContext(), 12);
            int size = Helper.dp2pixels(getContext(), 24);

            if (dX > margin) {
                // Right swipe
                Drawable d = getResources().getDrawable(EntityFolder.getIcon(swipes.right_type), getContext().getTheme());
                int padding = (rect.height() - size);
                d.setBounds(
                        rect.left + margin,
                        rect.top + padding / 2,
                        rect.left + margin + size,
                        rect.top + padding / 2 + size);
                d.draw(canvas);
            } else if (dX < -margin) {
                // Left swipe
                Drawable d = getResources().getDrawable(EntityFolder.getIcon(swipes.left_type), getContext().getTheme());
                int padding = (rect.height() - size);
                d.setBounds(
                        rect.left + rect.width() - size - margin,
                        rect.top + padding / 2,
                        rect.left + rect.width() - margin,
                        rect.top + padding / 2 + size);
                d.draw(canvas);
            }
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            TupleMessageEx message = getMessage(viewHolder);
            if (message == null)
                return;

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null)
                return;

            Log.i("Swiped dir=" + direction + " message=" + message.id);

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("thread", viewType != AdapterMessage.ViewType.THREAD);
            args.putLong("target", direction == ItemTouchHelper.LEFT ? swipes.swipe_left : swipes.swipe_right);

            new SimpleTask<ArrayList<MessageTarget>>() {
                @Override
                protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean thread = args.getBoolean("thread");
                    long tid = args.getLong("target");

                    ArrayList<MessageTarget> result = new ArrayList<>();

                    // Get target folder and hide message
                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityFolder target = db.folder().getFolder(tid);
                        if (target != null) {
                            EntityAccount account = db.account().getAccount(target.account);
                            EntityMessage message = db.message().getMessage(id);
                            if (message != null) {
                                List<EntityMessage> messages = db.message().getMessageByThread(
                                        message.account, message.thread, threading && thread ? null : id, message.folder);
                                for (EntityMessage threaded : messages) {
                                    result.add(new MessageTarget(threaded, account, target));
                                    db.message().setMessageUiHide(threaded.id, true);
                                    // Prevent new message notification on undo
                                    db.message().setMessageUiIgnored(threaded.id, true);
                                }
                            }
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                    moveUndo(result);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(FragmentMessages.this, args, "messages:swipe");
        }

        private TupleMessageEx getMessage(RecyclerView.ViewHolder viewHolder) {
            if (selectionTracker != null && selectionTracker.hasSelection())
                return null;

            int pos = viewHolder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return null;

            TupleMessageEx message = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList().get(pos);
            if (message == null || message.uid == null)
                return null;

            if (values.containsKey("expanded") && values.get("expanded").contains(message.id))
                return null;

            if (EntityFolder.DRAFTS.equals(message.folderType) || EntityFolder.OUTBOX.equals(message.folderType))
                return null;

            return message;
        }
    };

    private void onActionMove(String folderType) {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putString("thread", thread);
        args.putLong("id", id);
        args.putString("type", folderType);

        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");
                String thread = args.getString("thread");
                long id = args.getLong("id");
                String type = args.getString("type");

                ArrayList<MessageTarget> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder target = db.folder().getFolderByType(aid, type);
                    if (target != null) {
                        EntityAccount account = db.account().getAccount(target.account);
                        List<EntityMessage> messages = db.message().getMessageByThread(
                                aid, thread, threading ? null : id, null);
                        for (EntityMessage threaded : messages) {
                            EntityFolder folder = db.folder().getFolder(threaded.folder);
                            if (!target.id.equals(threaded.folder) &&
                                    !EntityFolder.DRAFTS.equals(folder.type) &&
                                    !EntityFolder.OUTBOX.equals(folder.type) &&
                                    (!EntityFolder.SENT.equals(folder.type) || EntityFolder.TRASH.equals(target.type)) &&
                                    !EntityFolder.TRASH.equals(folder.type) &&
                                    !EntityFolder.JUNK.equals(folder.type))
                                result.add(new MessageTarget(threaded, account, target));
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                moveAsk(result);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:move");
    }

    private void onMore() {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());

        new SimpleTask<MoreResult>() {
            @Override
            protected MoreResult onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");

                MoreResult result = new MoreResult();

                DB db = DB.getInstance(context);

                List<Long> fids = new ArrayList<>();
                for (long id : ids) {
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        continue;

                    if (!fids.contains(message.folder))
                        fids.add(message.folder);

                    if (message.ui_seen)
                        result.seen = true;
                    else
                        result.unseen = true;

                    if (message.ui_flagged)
                        result.flagged = true;
                    else
                        result.unflagged = true;

                    EntityFolder folder = db.folder().getFolder(message.folder);
                    boolean isArchive = EntityFolder.ARCHIVE.equals(folder.type);
                    boolean isTrash = EntityFolder.TRASH.equals(folder.type);
                    boolean isJunk = EntityFolder.JUNK.equals(folder.type);
                    boolean isDrafts = EntityFolder.DRAFTS.equals(folder.type);

                    result.isArchive = (result.isArchive == null ? isArchive : result.isArchive && isArchive);
                    result.isTrash = (result.isTrash == null ? isTrash : result.isTrash && isTrash);
                    result.isJunk = (result.isJunk == null ? isJunk : result.isJunk && isJunk);
                    result.isDrafts = (result.isDrafts == null ? isDrafts : result.isDrafts && isDrafts);

                    boolean hasArchive = (db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE) != null);
                    boolean hasTrash = (db.folder().getFolderByType(message.account, EntityFolder.TRASH) != null);
                    boolean hasJunk = (db.folder().getFolderByType(message.account, EntityFolder.JUNK) != null);

                    result.hasArchive = (result.hasArchive == null ? hasArchive : result.hasArchive && hasArchive);
                    result.hasTrash = (result.hasTrash == null ? hasTrash : result.hasTrash && hasTrash);
                    result.hasJunk = (result.hasJunk == null ? hasJunk : result.hasJunk && hasJunk);
                }

                if (result.isArchive == null) result.isArchive = false;
                if (result.isTrash == null) result.isTrash = false;
                if (result.isJunk == null) result.isJunk = false;
                if (result.isDrafts == null) result.isDrafts = false;

                if (result.hasArchive == null) result.hasArchive = false;
                if (result.hasTrash == null) result.hasTrash = false;
                if (result.hasJunk == null) result.hasJunk = false;

                result.accounts = db.account().getAccounts(true);

                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
                Collections.sort(result.accounts, new Comparator<EntityAccount>() {
                    @Override
                    public int compare(EntityAccount a1, EntityAccount a2) {
                        int p = -a1.primary.compareTo(a2.primary);
                        if (p != 0)
                            return p;
                        return collator.compare(a1.name, a2.name);
                    }
                });

                for (EntityAccount account : result.accounts) {
                    List<EntityFolder> targets = new ArrayList<>();
                    List<EntityFolder> folders = db.folder().getFolders(account.id);
                    for (EntityFolder target : folders)
                        if (!target.hide &&
                                !EntityFolder.ARCHIVE.equals(target.type) &&
                                !EntityFolder.TRASH.equals(target.type) &&
                                !EntityFolder.JUNK.equals(target.type) &&
                                (fids.size() != 1 || !fids.contains(target.id)))
                            targets.add(target);
                    EntityFolder.sort(context, targets);
                    result.targets.put(account, targets);
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, final MoreResult result) {
                PopupMenu popupMenu = new PopupMenu(getContext(), fabMore);

                if (result.unseen) // Unseen, not draft
                    popupMenu.getMenu().add(Menu.NONE, action_seen, 1, R.string.title_seen);
                if (result.seen) // Seen, not draft
                    popupMenu.getMenu().add(Menu.NONE, action_unseen, 2, R.string.title_unseen);

                popupMenu.getMenu().add(Menu.NONE, action_snooze, 3, R.string.title_snooze);

                if (result.unflagged)
                    popupMenu.getMenu().add(Menu.NONE, action_flag, 4, R.string.title_flag);
                if (result.flagged)
                    popupMenu.getMenu().add(Menu.NONE, action_unflag, 5, R.string.title_unflag);

                if (result.hasArchive && !result.isArchive && !result.isDrafts) // has archive and not is archive/drafts
                    popupMenu.getMenu().add(Menu.NONE, action_archive, 6, R.string.title_archive);

                if (result.isTrash) // is trash
                    popupMenu.getMenu().add(Menu.NONE, action_delete, 7, R.string.title_delete);

                if (!result.isTrash && result.hasTrash) // not trash and has trash
                    popupMenu.getMenu().add(Menu.NONE, action_trash, 8, R.string.title_trash);

                if (result.hasJunk && !result.isJunk && !result.isDrafts) // has junk and not junk/drafts
                    popupMenu.getMenu().add(Menu.NONE, action_junk, 9, R.string.title_spam);

                if (!result.isDrafts) { // not drafts
                    int order = 11;
                    for (EntityAccount account : result.accounts) {
                        SubMenu smenu = popupMenu.getMenu()
                                .addSubMenu(Menu.NONE, 0, order++, getString(R.string.title_move_to, account.name));
                        int sorder = 1;
                        for (EntityFolder target : result.targets.get(account)) {
                            MenuItem item = smenu.add(Menu.NONE, action_move, sorder++, target.getDisplayName(getContext()));
                            item.setIntent(new Intent().putExtra("target", target.id));
                        }
                    }
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        switch (target.getItemId()) {
                            case action_seen:
                                onActionSeenSelection(true);
                                return true;
                            case action_unseen:
                                onActionSeenSelection(false);
                                return true;
                            case action_snooze:
                                onActionSnoozeSelection();
                                return true;
                            case action_flag:
                                onActionFlagSelection(true);
                                return true;
                            case action_unflag:
                                onActionFlagSelection(false);
                                return true;
                            case action_archive:
                                onActionMoveSelection(EntityFolder.ARCHIVE);
                                return true;
                            case action_trash:
                                onActionMoveSelection(EntityFolder.TRASH);
                                return true;
                            case action_delete:
                                onActionDeleteSelection();
                                return true;
                            case action_junk:
                                onActionJunkSelection();
                                return true;
                            case action_move:
                                onActionMoveSelection(target.getIntent().getLongExtra("target", -1));
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:more");
    }

    private long[] getSelection() {
        Selection<Long> selection = selectionTracker.getSelection();

        long[] ids = new long[selection.size()];
        int i = 0;
        for (Long id : selection)
            ids[i++] = id;

        return ids;
    }

    private void onActionSeenSelection(boolean seen) {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());
        args.putBoolean("seen", seen);

        selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean seen = args.getBoolean("seen");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null && message.ui_seen != seen) {
                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    message.account, message.thread, threading ? null : id, message.folder);
                            for (EntityMessage threaded : messages)
                                EntityOperation.queue(context, db, threaded, EntityOperation.SEEN, seen);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:seen");
    }

    private void onActionSnoozeSelection() {
        DialogDuration.show(getContext(), getViewLifecycleOwner(), R.string.title_snooze,
                new DialogDuration.IDialogDuration() {
                    @Override
                    public void onDurationSelected(long duration, long time) {
                        if (Helper.isPro(getContext())) {
                            Bundle args = new Bundle();
                            args.putLongArray("ids", getSelection());
                            args.putLong("wakeup", duration == 0 ? -1 : time);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    long[] ids = args.getLongArray("ids");
                                    Long wakeup = args.getLong("wakeup");
                                    if (wakeup < 0)
                                        wakeup = null;

                                    DB db = DB.getInstance(context);
                                    for (long id : ids) {
                                        EntityMessage message = db.message().getMessage(id);
                                        if (message != null) {
                                            List<EntityMessage> messages = db.message().getMessageByThread(
                                                    message.account, message.thread, threading ? null : id, message.folder);
                                            for (EntityMessage threaded : messages) {
                                                db.message().setMessageSnoozed(threaded.id, wakeup);
                                                EntityMessage.snooze(context, threaded.id, wakeup);
                                            }
                                        }
                                    }

                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                                }
                            }.execute(FragmentMessages.this, args, "messages:snooze");
                        } else {
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                            fragmentTransaction.commit();
                        }
                    }

                    @Override
                    public void onDismiss() {
                        selectionTracker.clearSelection();
                    }
                });
    }

    private void onActionFlagSelection(boolean flagged) {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());
        args.putBoolean("flagged", flagged);

        selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean flagged = args.getBoolean("flagged");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null && message.ui_flagged != flagged) {
                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    message.account, message.thread, threading ? null : id, message.folder);
                            for (EntityMessage threaded : messages)
                                EntityOperation.queue(context, db, threaded, EntityOperation.FLAG, flagged);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:flag");
    }

    private void onActionDeleteSelection() {
        int count = selectionTracker.getSelection().size();
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(getResources().getQuantityString(R.plurals.title_deleting_messages, count, count))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLongArray("ids", getSelection());

                        selectionTracker.clearSelection();

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long[] ids = args.getLongArray("ids");

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    for (long id : ids) {
                                        EntityMessage message = db.message().getMessage(id);
                                        if (message != null) {
                                            List<EntityMessage> messages = db.message().getMessageByThread(
                                                    message.account, message.thread, threading ? null : id, message.folder);
                                            for (EntityMessage threaded : messages)
                                                if (threaded.uid != null)
                                                    EntityOperation.queue(context, db, threaded, EntityOperation.DELETE);
                                        }
                                    }

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentMessages.this, args, "messages:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onActionJunkSelection() {
        int count = selectionTracker.getSelection().size();
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(getResources().getQuantityString(R.plurals.title_ask_spam, count, count))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onActionMoveSelection(EntityFolder.JUNK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onActionMoveSelection(final String type) {
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putLongArray("ids", getSelection());

        selectionTracker.clearSelection();

        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                String type = args.getString("type");
                long[] ids = args.getLongArray("ids");

                ArrayList<MessageTarget> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null) {
                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    message.account, message.thread, threading ? null : id, message.folder);
                            for (EntityMessage threaded : messages) {
                                EntityFolder target = db.folder().getFolderByType(message.account, type);
                                EntityAccount account = db.account().getAccount(target.account);
                                result.add(new MessageTarget(threaded, account, target));
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                if (EntityFolder.JUNK.equals(type))
                    moveAskConfirmed(result);
                else
                    moveAsk(result);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:move");
    }

    private void onActionMoveSelection(long target) {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());
        args.putLong("target", target);

        selectionTracker.clearSelection();

        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                long tid = args.getLong("target");

                ArrayList<MessageTarget> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder target = db.folder().getFolder(tid);
                    if (target != null) {
                        EntityAccount account = db.account().getAccount(target.account);
                        for (long id : ids) {
                            EntityMessage message = db.message().getMessage(id);
                            if (message != null) {
                                List<EntityMessage> messages = db.message().getMessageByThread(
                                        message.account, message.thread, threading ? null : id, message.folder);
                                for (EntityMessage threaded : messages)
                                    result.add(new MessageTarget(threaded, account, target));
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                moveAsk(result);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:move");
    }

    @Override
    public void onDestroyView() {
        ((ActivityBase) getActivity()).removeBackPressedListener(onBackPressedListener);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("autoExpand", autoExpand);
        outState.putInt("autoCloseCount", autoCloseCount);

        outState.putStringArray("values", values.keySet().toArray(new String[0]));
        for (String name : values.keySet())
            outState.putLongArray(name, Helper.toLongArray(values.get(name)));

        if (selectionTracker != null)
            selectionTracker.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            autoExpand = savedInstanceState.getBoolean("autoExpand");
            autoCloseCount = savedInstanceState.getInt("autoCloseCount");

            String[] names = savedInstanceState.getStringArray("values");
            for (String name : names) {
                values.put(name, new ArrayList<Long>());
                for (Long value : savedInstanceState.getLongArray(name))
                    values.get(name).add(value);
            }

            if (selectionTracker != null)
                selectionTracker.onRestoreInstanceState(savedInstanceState);
        }

        boolean hints = (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        grpHintSupport.setVisibility(prefs.getBoolean("app_support", false) || !hints ? View.GONE : View.VISIBLE);
        grpHintSwipe.setVisibility(prefs.getBoolean("message_swipe", false) || !hints ? View.GONE : View.VISIBLE);
        grpHintSelect.setVisibility(prefs.getBoolean("message_select", false) || !hints ? View.GONE : View.VISIBLE);

        final DB db = DB.getInstance(getContext());

        // Primary account
        db.account().livePrimaryAccount().observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
            @Override
            public void onChanged(EntityAccount account) {
                long primary = (account == null ? -1 : account.id);
                boolean connected = (account != null && "connected".equals(account.state));
                if (FragmentMessages.this.primary != primary || FragmentMessages.this.connected != connected) {
                    FragmentMessages.this.primary = primary;
                    FragmentMessages.this.connected = connected;
                    getActivity().invalidateOptionsMenu();
                }
            }
        });

        db.account().liveAccountSwipes().observe(getViewLifecycleOwner(), new Observer<List<TupleAccountSwipes>>() {
            @Override
            public void onChanged(List<TupleAccountSwipes> swipes) {
                if (swipes == null)
                    swipes = new ArrayList<>();

                Log.i("Swipes=" + swipes.size());

                accountSwipes.clear();
                for (TupleAccountSwipes swipe : swipes)
                    accountSwipes.put(swipe.id, swipe);
            }
        });

        // Folder
        switch (viewType) {
            case UNIFIED:
                db.folder().liveUnified().observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                    @Override
                    public void onChanged(List<TupleFolderEx> folders) {
                        if (folders == null)
                            folders = new ArrayList<>();

                        int unseen = 0;
                        for (TupleFolderEx folder : folders)
                            unseen += folder.unseen;
                        String name = getString(R.string.title_folder_unified);
                        if (unseen > 0)
                            setSubtitle(getString(R.string.title_unseen_count, name, unseen));
                        else
                            setSubtitle(name);

                        boolean refreshing = false;
                        for (TupleFolderEx folder : folders)
                            if (folder.sync_state != null && "connected".equals(folder.accountState)) {
                                refreshing = true;
                                break;
                            }

                        swipeRefresh.setRefreshing(refreshing);
                    }
                });
                break;

            case FOLDER:
                db.folder().liveFolderEx(folder).observe(getViewLifecycleOwner(), new Observer<TupleFolderEx>() {
                    @Override
                    public void onChanged(@Nullable TupleFolderEx folder) {
                        if (folder == null)
                            setSubtitle(null);
                        else {
                            String name = folder.getDisplayName(getContext());
                            if (folder.unseen > 0)
                                setSubtitle(getString(R.string.title_unseen_count, name, folder.unseen));
                            else
                                setSubtitle(name);

                            boolean outbox = EntityFolder.OUTBOX.equals(folder.type);
                            if (FragmentMessages.this.outbox != outbox) {
                                FragmentMessages.this.outbox = outbox;
                                getActivity().invalidateOptionsMenu();
                            }
                        }

                        swipeRefresh.setRefreshing(
                                folder != null && folder.sync_state != null &&
                                        "connected".equals(EntityFolder.OUTBOX.equals(folder.type)
                                                ? folder.state : folder.accountState));
                    }
                });
                break;

            case THREAD:
                db.account().liveAccount(account).observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
                    @Override
                    public void onChanged(EntityAccount account) {
                        setSubtitle(getString(R.string.title_folder_thread, account == null ? "" : account.name));
                    }
                });
                break;

            case SEARCH:
                setSubtitle(getString(R.string.title_searching, search));
                break;
        }

        loadMessages();

        if (selectionTracker != null && selectionTracker.hasSelection())
            fabMore.show();
        else
            fabMore.hide();

        if (viewType != AdapterMessage.ViewType.THREAD) {
            db.folder().liveDrafts(account < 0 ? null : account).observe(getViewLifecycleOwner(), new Observer<EntityFolder>() {
                @Override
                public void onChanged(EntityFolder drafts) {
                    if (drafts == null)
                        fab.hide();
                    else {
                        fab.setTag(drafts);
                        fab.show();
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        grpSupport.setVisibility(viewType == AdapterMessage.ViewType.THREAD ||
                Helper.isPro(getContext()) ? View.GONE : View.VISIBLE);

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        adapter.setCompact(compact);
        adapter.setZoom(zoom);
    }

    @Override
    public void onPause() {
        super.onPause();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            check();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            check();
        }

        @Override
        public void onLost(Network network) {
            check();
        }

        private void check() {
            Activity activity = getActivity();
            if (activity != null)
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                            adapter.checkInternet();
                    }
                });
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_messages, menu);

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
        searchView.setQueryHint(getString(folder < 0 ? R.string.title_search_device : R.string.title_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = false;
                menuSearch.collapseActionView();

                if (Helper.isPro(getContext())) {
                    Bundle args = new Bundle();
                    args.putLong("folder", folder);
                    args.putString("search", query);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            DB.getInstance(context).message().resetSearch();
                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            FragmentMessages fragment = new FragmentMessages();
                            fragment.setArguments(args);

                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("search");
                            fragmentTransaction.commit();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                        }
                    }.execute(FragmentMessages.this, args, "messages:resetsearch");
                } else {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                    fragmentTransaction.commit();
                }

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean selection = (selectionTracker != null && selectionTracker.hasSelection());

        menu.findItem(R.id.menu_search).setVisible(viewType != AdapterMessage.ViewType.SEARCH);

        menu.findItem(R.id.menu_folders).setVisible(primary >= 0);
        menu.findItem(R.id.menu_folders).setIcon(connected ? R.drawable.baseline_folder_24 : R.drawable.baseline_folder_open_24);

        menu.findItem(R.id.menu_sort_on).setVisible(!selection &&
                (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER));

        String sort = prefs.getString("sort", "time");
        if ("time".equals(sort))
            menu.findItem(R.id.menu_sort_on_time).setChecked(true);
        else if ("unread".equals(sort))
            menu.findItem(R.id.menu_sort_on_unread).setChecked(true);
        else if ("starred".equals(sort))
            menu.findItem(R.id.menu_sort_on_starred).setChecked(true);
        else if ("sender".equals(sort))
            menu.findItem(R.id.menu_sort_on_sender).setChecked(true);

        menu.findItem(R.id.menu_zoom).setVisible(!selection);

        menu.findItem(R.id.menu_compact).setVisible(!selection);
        menu.findItem(R.id.menu_compact).setChecked(prefs.getBoolean("compact", false));

        menu.findItem(R.id.menu_snoozed).setVisible(!selection && !outbox &&
                (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER));
        menu.findItem(R.id.menu_snoozed).setChecked(prefs.getBoolean("snoozed", false));

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_folders:
                onMenuFolders();
                loadMessages();
                return true;

            case R.id.menu_sort_on_time:
                item.setChecked(true);
                onMenuSort("time");
                return true;

            case R.id.menu_sort_on_unread:
                item.setChecked(true);
                onMenuSort("unread");
                return true;

            case R.id.menu_sort_on_starred:
                item.setChecked(true);
                onMenuSort("starred");
                return true;

            case R.id.menu_sort_on_sender:
                item.setChecked(true);
                onMenuSort("sender");
                return true;

            case R.id.menu_zoom:
                onMenuZoom();
                return true;

            case R.id.menu_compact:
                onMenuCompact();
                return true;

            case R.id.menu_snoozed:
                onMenuSnoozed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuFolders() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getFragmentManager().popBackStack("unified", 0);

        Bundle args = new Bundle();
        args.putLong("account", primary);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folders");
        fragmentTransaction.commit();
    }

    private void onMenuSort(String sort) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("sort", sort).apply();
        adapter.setSort(sort);
        loadMessages();
    }

    private void onMenuZoom() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        zoom = ++zoom % 3;
        prefs.edit().putInt("zoom", zoom).apply();
        adapter.setZoom(zoom);
    }

    private void onMenuCompact() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = !prefs.getBoolean("compact", false);
        prefs.edit().putBoolean("compact", compact).apply();

        int zoom = (compact ? 0 : 1);
        prefs.edit().putInt("zoom", zoom).apply();

        adapter.setCompact(compact);
        adapter.setZoom(zoom);
        getActivity().invalidateOptionsMenu();
    }

    private void onMenuSnoozed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean snoozed = prefs.getBoolean("snoozed", false);
        prefs.edit().putBoolean("snoozed", !snoozed).apply();
        loadMessages();
    }

    private void loadMessages() {
        ViewModelBrowse modelBrowse = ViewModelProviders.of(getActivity()).get(ViewModelBrowse.class);
        modelBrowse.set(getContext(), folder, search, REMOTE_PAGE_SIZE);

        if (viewType == AdapterMessage.ViewType.FOLDER || viewType == AdapterMessage.ViewType.SEARCH)
            if (boundaryCallback == null)
                boundaryCallback = new BoundaryCallbackMessages(this, modelBrowse,
                        new BoundaryCallbackMessages.IBoundaryCallbackMessages() {
                            @Override
                            public void onLoading() {
                                pbWait.setTag(true);
                                tvNoEmail.setVisibility(View.GONE);
                                pbWait.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoaded(int fetched) {
                                RecyclerView.Adapter adapter = rvMessage.getAdapter();
                                int items = (adapter == null ? 0 : adapter.getItemCount());
                                tvNoEmail.setVisibility(items + fetched == 0 ? View.VISIBLE : View.GONE);
                                pbWait.setVisibility(View.GONE);
                                pbWait.setTag(null);
                            }

                            @Override
                            public void onError(Throwable ex) {
                                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                                    new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                                            .setMessage(Helper.formatThrowable(ex))
                                            .setPositiveButton(android.R.string.cancel, null)
                                            .create()
                                            .show();
                            }
                        });

        // Observe folder/messages/search
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = prefs.getString("sort", "time");
        boolean snoozed = prefs.getBoolean("snoozed", false);
        boolean debug = prefs.getBoolean("debug", false);
        Log.i("Load messages type=" + viewType + " sort=" + sort + " debug=" + debug);

        // Sort changed
        final ViewModelMessages modelMessages = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
        modelMessages.removeObservers(viewType, getViewLifecycleOwner());

        DB db = DB.getInstance(getContext());
        LivePagedListBuilder<Integer, TupleMessageEx> builder = null;
        switch (viewType) {
            case UNIFIED:
                builder = new LivePagedListBuilder<>(
                        db.message().pagedUnifiedInbox(threading, sort, snoozed, false, debug), LOCAL_PAGE_SIZE);
                break;

            case FOLDER:
                PagedList.Config configFolder = new PagedList.Config.Builder()
                        .setPageSize(LOCAL_PAGE_SIZE)
                        .setPrefetchDistance(REMOTE_PAGE_SIZE)
                        .build();
                builder = new LivePagedListBuilder<>(
                        db.message().pagedFolder(folder, threading, sort, snoozed, false, debug), configFolder);
                builder.setBoundaryCallback(boundaryCallback);
                break;

            case THREAD:
                builder = new LivePagedListBuilder<>(
                        db.message().pagedThread(account, thread, threading ? null : id, debug), LOCAL_PAGE_SIZE);
                break;

            case SEARCH:
                PagedList.Config configSearch = new PagedList.Config.Builder()
                        .setPageSize(LOCAL_PAGE_SIZE)
                        .setPrefetchDistance(REMOTE_PAGE_SIZE)
                        .build();
                if (folder < 0)
                    builder = new LivePagedListBuilder<>(
                            db.message().pagedUnifiedInbox(threading, "time", snoozed, true, debug), configSearch);
                else
                    builder = new LivePagedListBuilder<>(
                            db.message().pagedFolder(folder, threading, "time", snoozed, true, debug), configSearch);
                builder.setBoundaryCallback(boundaryCallback);
                break;
        }

        builder.setFetchExecutor(executor);

        modelMessages.setMessages(viewType, getActivity(), builder.build());
        modelMessages.observe(viewType, getViewLifecycleOwner(), observer);
    }

    private Observer<PagedList<TupleMessageEx>> observer = new Observer<PagedList<TupleMessageEx>>() {
        @Override
        public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
            if (messages == null ||
                    (viewType == AdapterMessage.ViewType.THREAD && messages.size() == 0 &&
                            (autoclose || autonext))) {
                handleAutoClose();
                return;
            }

            if (viewType == AdapterMessage.ViewType.THREAD) {
                // Mark duplicates
                Map<String, List<TupleMessageEx>> duplicates = new HashMap<>();
                for (TupleMessageEx message : messages)
                    if (message != null && message.msgid != null) {
                        if (!duplicates.containsKey(message.msgid))
                            duplicates.put(message.msgid, new ArrayList<TupleMessageEx>());
                        duplicates.get(message.msgid).add(message);
                    }
                for (String msgid : duplicates.keySet()) {
                    List<TupleMessageEx> dups = duplicates.get(msgid);
                    if (dups.size() > 1) {
                        Collections.sort(dups, new Comparator<TupleMessageEx>() {
                            final List<String> ORDER = Arrays.asList(
                                    EntityFolder.INBOX,
                                    EntityFolder.OUTBOX,
                                    EntityFolder.DRAFTS,
                                    EntityFolder.SENT,
                                    EntityFolder.TRASH,
                                    EntityFolder.JUNK,
                                    EntityFolder.SYSTEM,
                                    EntityFolder.USER,
                                    EntityFolder.ARCHIVE
                            );

                            @Override
                            public int compare(TupleMessageEx d1, TupleMessageEx d2) {
                                int o1 = ORDER.indexOf(d1.folderType);
                                int o2 = ORDER.indexOf(d2.folderType);
                                return ((Integer) o1).compareTo(o2);
                            }
                        });
                        for (int i = 1; i < dups.size(); i++)
                            dups.get(i).duplicate = true;
                    }
                }

                if (autoExpand) {
                    autoExpand = false;

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    long download = prefs.getInt("download", 32768);
                    if (download == 0)
                        download = Long.MAX_VALUE;

                    Boolean isMetered = Helper.isMetered(getContext(), false);
                    boolean metered = (isMetered == null || isMetered);

                    int count = 0;
                    int unseen = 0;
                    TupleMessageEx single = null;
                    TupleMessageEx see = null;
                    for (TupleMessageEx message : messages) {
                        if (message == null)
                            continue;

                        if (!message.duplicate &&
                                !EntityFolder.DRAFTS.equals(message.folderType) &&
                                !EntityFolder.TRASH.equals(message.folderType)) {
                            count++;
                            single = message;
                            if (!message.ui_seen) {
                                unseen++;
                                see = message;
                            }
                        }

                        if (!EntityFolder.ARCHIVE.equals(message.folderType) &&
                                !EntityFolder.SENT.equals(message.folderType) &&
                                !EntityFolder.TRASH.equals(message.folderType) &&
                                !EntityFolder.JUNK.equals(message.folderType))
                            autoCloseCount++;
                    }

                    // Auto expand when:
                    // - single, non archived/trashed/sent message
                    // - one unread, non archived/trashed/sent message in conversation
                    // - sole message

                    TupleMessageEx expand = null;
                    if (count == 1)
                        expand = single;
                    else if (unseen == 1)
                        expand = see;
                    else if (messages.size() == 1)
                        expand = messages.get(0);

                    if (expand != null &&
                            (expand.content || !metered || (expand.size != null && expand.size < download))) {
                        if (!values.containsKey("expanded"))
                            values.put("expanded", new ArrayList<Long>());
                        values.get("expanded").add(expand.id);
                        handleExpand(expand.id);
                    }
                } else {
                    if (autoCloseCount > 0 && (autoclose || autonext)) {
                        int count = 0;
                        for (int i = 0; i < messages.size(); i++) {
                            TupleMessageEx message = messages.get(i);
                            if (message != null &&
                                    !EntityFolder.ARCHIVE.equals(message.folderType) &&
                                    !EntityFolder.SENT.equals(message.folderType) &&
                                    !EntityFolder.TRASH.equals(message.folderType) &&
                                    !EntityFolder.JUNK.equals(message.folderType))
                                count++;
                        }
                        Log.i("Auto close=" + count);

                        // Auto close/next when:
                        // - no more non archived/trashed/sent messages

                        if (count == 0) {
                            handleAutoClose();
                            return;
                        }
                    }
                }

                if (actionbar) {
                    Bundle args = new Bundle();
                    args.putLong("account", account);
                    args.putString("thread", thread);
                    args.putLong("id", id);

                    new SimpleTask<Boolean[]>() {
                        @Override
                        protected Boolean[] onExecute(Context context, Bundle args) {
                            long account = args.getLong("account");
                            String thread = args.getString("thread");
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);

                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    account, thread, threading ? null : id, null);

                            boolean trashable = false;
                            boolean archivable = false;
                            for (EntityMessage message : messages)
                                if (message.uid != null) {
                                    EntityFolder folder = db.folder().getFolder(message.folder);
                                    if (!EntityFolder.DRAFTS.equals(folder.type) &&
                                            !EntityFolder.OUTBOX.equals(folder.type) &&
                                            // allow sent
                                            !EntityFolder.TRASH.equals(folder.type) &&
                                            !EntityFolder.JUNK.equals(folder.type))
                                        trashable = true;
                                    if (!EntityFolder.isOutgoing(folder.type) &&
                                            !EntityFolder.TRASH.equals(folder.type) &&
                                            !EntityFolder.JUNK.equals(folder.type) &&
                                            !EntityFolder.ARCHIVE.equals(folder.type))
                                        archivable = true;
                                }

                            EntityFolder trash = db.folder().getFolderByType(account, EntityFolder.TRASH);
                            EntityFolder archive = db.folder().getFolderByType(account, EntityFolder.ARCHIVE);

                            trashable = (trashable && trash != null);
                            archivable = (archivable && archive != null);

                            return new Boolean[]{trashable, archivable};
                        }

                        @Override
                        protected void onExecuted(Bundle args, Boolean[] data) {
                            bottom_navigation.getMenu().findItem(R.id.action_delete).setVisible(data[0]);
                            bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(data[1]);
                            bottom_navigation.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                        }
                    }.execute(FragmentMessages.this, args, "messages:navigation");
                }
            }

            Log.i("Submit messages=" + messages.size());
            adapter.submitList(messages);

            if (pbWait.getTag() == null) {
                pbWait.setVisibility(View.GONE);
                if (!(viewType == AdapterMessage.ViewType.FOLDER || viewType == AdapterMessage.ViewType.SEARCH))
                    tvNoEmail.setVisibility(messages.size() == 0 ? View.VISIBLE : View.GONE);
                grpReady.setVisibility(messages.size() > 0 ? View.VISIBLE : View.GONE);
            }
        }
    };

    private void handleExpand(long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message.uid != null) {
                        if (!message.content)
                            EntityOperation.queue(context, db, message, EntityOperation.BODY);
                        if (!message.ui_seen)
                            EntityOperation.queue(context, db, message, EntityOperation.SEEN, true);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "messages:expand");
    }

    private void handleAutoClose() {
        if (autoclose)
            finish();
        else if (autonext) {
            Intent intent = bottom_navigation.getMenu().findItem(R.id.action_next).getIntent();
            Long id = (intent == null ? null : intent.getLongExtra("id", -1));
            if (id == null || id < 0)
                finish();
            else
                navigate(id);
        }
    }

    private void navigate(long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);
        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).message().getMessage(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage message) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_THREAD)
                                .putExtra("account", message.account)
                                .putExtra("thread", message.thread)
                                .putExtra("id", message.id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "messages:navigate");
    }

    private void moveAsk(final ArrayList<MessageTarget> result) {
        if (result.size() == 0)
            return;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("automove", false)) {
            moveAskAcross(result);
            return;
        }

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ask_again, null);
        final TextView tvMessage = dview.findViewById(R.id.tvMessage);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

        tvMessage.setText(getResources().getQuantityString(R.plurals.title_moving_messages,
                result.size(), result.size(), getDisplay(result)));

        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cbNotAgain.isChecked())
                            prefs.edit().putBoolean("automove", true).apply();
                        moveAskAcross(result);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void moveAskAcross(final ArrayList<MessageTarget> result) {
        boolean across = false;
        for (MessageTarget target : result)
            if (target.across) {
                across = true;
                break;
            }

        if (across)
            new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                    .setMessage(R.string.title_accross_remark)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveAskConfirmed(result);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        else
            moveAskConfirmed(result);
    }

    private void moveAskConfirmed(ArrayList<MessageTarget> result) {
        Bundle args = new Bundle();
        args.putParcelableArrayList("result", result);

        // Move messages
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                try {
                    List<MessageTarget> result = args.getParcelableArrayList("result");

                    db.beginTransaction();

                    for (MessageTarget target : result) {
                        EntityMessage message = db.message().getMessage(target.id);
                        if (message != null) {
                            Log.i("Move id=" + target.id + " target=" + target.folder.name);
                            EntityOperation.queue(context, db, message, EntityOperation.MOVE, target.folder.id);
                        }
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
        }.execute(FragmentMessages.this, args, "messages:move");
    }

    private void moveUndo(final ArrayList<MessageTarget> result) {
        // Show undo snackbar
        final Snackbar snackbar = Snackbar.make(
                view,
                getString(R.string.title_moving, getDisplay(result)),
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.title_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();

                Bundle args = new Bundle();
                args.putParcelableArrayList("result", result);

                // Show message again
                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        DB db = DB.getInstance(context);
                        ArrayList<MessageTarget> result = args.getParcelableArrayList("result");
                        for (MessageTarget target : result) {
                            Log.i("Move undo id=" + target.id);
                            db.message().setMessageUiHide(target.id, false);
                        }
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentMessages.this, args, "messages:undo");
            }
        });
        snackbar.show();

        final Context context = getContext().getApplicationContext();

        // Wait
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Move timeout");

                // Remove snackbar
                if (snackbar.isShown())
                    snackbar.dismiss();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            for (MessageTarget target : result) {
                                EntityMessage message = db.message().getMessage(target.id);
                                if (message != null && message.ui_hide) {
                                    Log.i("Move id=" + id + " target=" + target.folder.name);
                                    EntityOperation.queue(context, db, message, EntityOperation.MOVE, target.folder.id);
                                }
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                    }
                }).start();
            }
        }, UNDO_TIMEOUT);
    }

    private String getDisplay(ArrayList<MessageTarget> result) {
        boolean across = false;
        for (MessageTarget target : result)
            if (target.across)
                across = true;

        List<String> displays = new ArrayList<>();
        for (MessageTarget target : result) {
            String display = (across ? target.account.name + "/" : "") +
                    target.folder.getDisplayName(getContext());
            if (!displays.contains(display))
                displays.add(display);
        }

        Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
        Collections.sort(displays, collator);

        return TextUtils.join(", ", displays);
    }

    private ActivityBase.IBackPressedListener onBackPressedListener = new ActivityBase.IBackPressedListener() {
        @Override
        public boolean onBackPressed() {
            if (selectionTracker != null && selectionTracker.hasSelection()) {
                selectionTracker.clearSelection();
                return true;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (prefs.getBoolean("collapse", false))
                if (values.containsKey("expanded") && values.get("expanded").size() > 0) {
                    values.get("expanded").clear();
                    adapter.notifyDataSetChanged();
                    return true;
                }

            return false;
        }
    };

    private class MoreResult {
        boolean seen;
        boolean unseen;
        boolean flagged;
        boolean unflagged;
        Boolean hasArchive;
        Boolean hasTrash;
        Boolean hasJunk;
        Boolean isArchive;
        Boolean isTrash;
        Boolean isJunk;
        Boolean isDrafts;
        List<EntityAccount> accounts;
        Map<EntityAccount, List<EntityFolder>> targets = new HashMap<>();
    }

    private static class MessageTarget implements Parcelable {
        long id;
        boolean across;
        EntityAccount account;
        EntityFolder folder;

        MessageTarget(EntityMessage message, EntityAccount account, EntityFolder folder) {
            this.id = message.id;
            this.across = !folder.account.equals(message.account);
            this.account = account;
            this.folder = folder;
        }

        protected MessageTarget(Parcel in) {
            id = in.readLong();
            across = (in.readInt() != 0);
            account = (EntityAccount) in.readSerializable();
            folder = (EntityFolder) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeInt(across ? 1 : 0);
            dest.writeSerializable(account);
            dest.writeSerializable(folder);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<MessageTarget> CREATOR = new Creator<MessageTarget>() {
            @Override
            public MessageTarget createFromParcel(Parcel in) {
                return new MessageTarget(in);
            }

            @Override
            public MessageTarget[] newArray(int size) {
                return new MessageTarget[size];
            }
        };
    }
}
