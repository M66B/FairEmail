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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
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

public class FragmentMessages extends FragmentEx {
    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private View popupAnchor;
    private TextView tvSupport;
    private ImageButton ibHintSupport;
    private ImageButton ibHintSwipe;
    private ImageButton ibHintSelect;
    private ImageButton ibHintCompact;
    private TextView tvNoEmail;
    private FixedRecyclerView rvMessage;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpSupport;
    private Group grpHintSupport;
    private Group grpHintSwipe;
    private Group grpHintSelect;
    private Group grpHintCompact;
    private Group grpReady;
    private FloatingActionButton fab;
    private FloatingActionButton fabMore;

    private long account;
    private long folder;
    private boolean outgoing;
    private String thread;
    private long id;
    private String search;

    private boolean compact;
    private boolean threading;
    private boolean pull;
    private boolean actionbar;
    private boolean autoclose;

    private long primary = -1;
    private boolean outbox = false;
    private boolean connected = false;
    private boolean searching = false;
    private AdapterMessage adapter;
    private List<Long> archives = new ArrayList<>();
    private List<Long> trashes = new ArrayList<>();

    private AdapterMessage.ViewType viewType;
    private SelectionTracker<Long> selectionTracker = null;
    private LiveData<PagedList<TupleMessageEx>> messages = null;

    private int autoCloseCount = 0;
    private boolean autoExpand = true;
    private Map<String, List<Long>> values = new HashMap<>();
    private Map<Long, Spanned> bodies = new HashMap<>();

    private BoundaryCallbackMessages searchCallback = null;

    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

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
        outgoing = args.getBoolean("outgoing", false);
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
        compact = prefs.getBoolean("compact", false);

        if (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER)
            pull = prefs.getBoolean("pull", true);
        else
            pull = false;

        threading = prefs.getBoolean("threading", true);
        actionbar = prefs.getBoolean("actionbar", true);
        autoclose = prefs.getBoolean("autoclose", true);

    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_messages, container, false);

        setHasOptionsMenu(true);

        // Get controls
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        popupAnchor = view.findViewById(R.id.popupAnchor);
        tvSupport = view.findViewById(R.id.tvSupport);
        ibHintSupport = view.findViewById(R.id.ibHintSupport);
        ibHintSwipe = view.findViewById(R.id.ibHintSwipe);
        ibHintSelect = view.findViewById(R.id.ibHintSelect);
        ibHintCompact = view.findViewById(R.id.ibHintCompact);
        tvNoEmail = view.findViewById(R.id.tvNoEmail);
        rvMessage = view.findViewById(R.id.rvMessage);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpSupport = view.findViewById(R.id.grpSupport);
        grpHintSupport = view.findViewById(R.id.grpHintSupport);
        grpHintSwipe = view.findViewById(R.id.grpHintSwipe);
        grpHintSelect = view.findViewById(R.id.grpHintSelect);
        grpHintCompact = view.findViewById(R.id.grpHintCompact);
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
                }.execute(FragmentMessages.this, args);
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

        ibHintCompact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("message_compact", true).apply();
                grpHintCompact.setVisibility(View.GONE);
            }
        });

        rvMessage.setHasFixedSize(false);
        //rvMessage.setItemViewCacheSize(10);
        //rvMessage.getRecycledViewPool().setMaxRecycledViews(0, 10);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvMessage.setLayoutManager(llm);

        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        adapter = new AdapterMessage(
                getContext(), getViewLifecycleOwner(), getFragmentManager(),
                viewType, outgoing,
                zoom,
                new AdapterMessage.IProperties() {
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

                        new SimpleTask<MessageTarget>() {
                            @Override
                            protected MessageTarget onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                String name = args.getString("name");
                                boolean type = args.getBoolean("type");

                                MessageTarget result = new MessageTarget();

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(id);
                                    if (type)
                                        result.target = db.folder().getFolderByType(message.account, name);
                                    else
                                        result.target = db.folder().getFolderByName(message.account, name);
                                    result.ids.add(message.id);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                return result;
                            }

                            @Override
                            protected void onExecuted(Bundle args, MessageTarget result) {
                                moveAsk(result);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentMessages.this, args);
                    }
                });

        rvMessage.setAdapter(adapter);

        if (viewType != AdapterMessage.ViewType.THREAD) {
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
                    try {
                        if (selectionTracker.hasSelection()) {
                            swipeRefresh.setEnabled(false);
                            if (getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                                if (messages != null)
                                    messages.removeObservers(getViewLifecycleOwner());
                            fabMore.show();
                        } else {
                            predicate.clearAccount();
                            fabMore.hide();
                            if (getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                                loadMessages();
                            swipeRefresh.setEnabled(pull);
                        }
                    } catch (IllegalStateException ex) {
                        Log.w(ex);
                    }
                }
            });
        }

        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (!prefs.getBoolean("swipe", true))
                    return 0;

                if (selectionTracker != null && selectionTracker.hasSelection())
                    return 0;

                int pos = viewHolder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return 0;

                TupleMessageEx message = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList().get(pos);
                if (message == null || message.uid == null ||
                        (values.containsKey("expanded") && values.get("expanded").contains(message.id)) ||
                        EntityFolder.DRAFTS.equals(message.folderType) ||
                        EntityFolder.OUTBOX.equals(message.folderType))
                    return 0;

                int flags = 0;
                if (archives.contains(message.account))
                    flags |= ItemTouchHelper.RIGHT;
                if (trashes.contains(message.account))
                    flags |= ItemTouchHelper.LEFT;

                return makeMovementFlags(0, flags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                int pos = viewHolder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                TupleMessageEx message = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList().get(pos);
                if (message == null)
                    return;

                boolean inbox = (EntityFolder.ARCHIVE.equals(message.folderType) || EntityFolder.TRASH.equals(message.folderType));

                View itemView = viewHolder.itemView;
                int margin = Helper.dp2pixels(getContext(), 12);

                if (dX > margin) {
                    // Right swipe
                    Drawable d = getResources().getDrawable(
                            inbox ? R.drawable.baseline_move_to_inbox_24 : R.drawable.baseline_archive_24,
                            getContext().getTheme());
                    int padding = (itemView.getHeight() - d.getIntrinsicHeight());
                    d.setBounds(
                            itemView.getLeft() + margin,
                            itemView.getTop() + padding / 2,
                            itemView.getLeft() + margin + d.getIntrinsicWidth(),
                            itemView.getTop() + padding / 2 + d.getIntrinsicHeight());
                    d.draw(canvas);
                } else if (dX < -margin) {
                    // Left swipe
                    Drawable d = getResources().getDrawable(inbox ? R.drawable.baseline_move_to_inbox_24 : R.drawable.baseline_delete_24, getContext().getTheme());
                    int padding = (itemView.getHeight() - d.getIntrinsicHeight());
                    d.setBounds(
                            itemView.getLeft() + itemView.getWidth() - d.getIntrinsicWidth() - margin,
                            itemView.getTop() + padding / 2,
                            itemView.getLeft() + itemView.getWidth() - margin,
                            itemView.getTop() + padding / 2 + d.getIntrinsicHeight());
                    d.draw(canvas);
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                TupleMessageEx message = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList().get(pos);
                if (message == null)
                    return;
                Log.i("Swiped dir=" + direction + " message=" + message.id);

                Bundle args = new Bundle();
                args.putLong("id", message.id);
                args.putBoolean("thread", viewType != AdapterMessage.ViewType.THREAD);
                args.putInt("direction", direction);

                new SimpleTask<MessageTarget>() {
                    @Override
                    protected MessageTarget onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        boolean thread = args.getBoolean("thread");
                        int direction = args.getInt("direction");

                        MessageTarget result = new MessageTarget();
                        EntityFolder target = null;

                        // Get target folder and hide message
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityMessage message = db.message().getMessage(id);

                            EntityFolder folder = db.folder().getFolder(message.folder);
                            if (EntityFolder.ARCHIVE.equals(folder.type) || EntityFolder.TRASH.equals(folder.type))
                                target = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                            else {
                                if (direction == ItemTouchHelper.RIGHT)
                                    target = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                                if (direction == ItemTouchHelper.LEFT || target == null)
                                    target = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                if (target == null)
                                    target = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                            }

                            result.target = target;

                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    message.account, message.thread, threading && thread ? null : id, message.folder);
                            for (EntityMessage threaded : messages) {
                                result.ids.add(threaded.id);
                                db.message().setMessageUiHide(threaded.id, true);
                                // Prevent new message notification on undo
                                db.message().setMessageUiIgnored(threaded.id, true);
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return result;
                    }

                    @Override
                    protected void onExecuted(final Bundle args, final MessageTarget result) {
                        moveUndo(result);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentMessages.this, args);
            }
        }).attachToRecyclerView(rvMessage);

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                ViewModelMessages.Target[] pn = (ViewModelMessages.Target[]) bottom_navigation.getTag();

                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        onActionMove(EntityFolder.TRASH);
                        return true;

                    case R.id.action_archive:
                        onActionMove(EntityFolder.ARCHIVE);
                        return true;

                    case R.id.action_prev:
                        onActionNavigate(pn[0]);
                        return true;

                    case R.id.action_next:
                        onActionNavigate(pn[1]);
                        return true;

                    default:
                        return false;
                }
            }

            private void onActionMove(String folderType) {
                Bundle args = new Bundle();
                args.putLong("account", account);
                args.putString("thread", thread);
                args.putLong("id", id);
                args.putString("folderType", folderType);

                new SimpleTask<MessageTarget>() {
                    @Override
                    protected MessageTarget onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");
                        String thread = args.getString("thread");
                        long id = args.getLong("id");
                        String folderType = args.getString("folderType");

                        MessageTarget result = new MessageTarget();

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            result.target = db.folder().getFolderByType(account, folderType);

                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    account, thread, threading ? null : id, null);
                            for (EntityMessage threaded : messages) {
                                EntityFolder folder = db.folder().getFolder(threaded.folder);
                                if (!result.target.id.equals(threaded.folder) &&
                                        !EntityFolder.DRAFTS.equals(folder.type) &&
                                        !EntityFolder.OUTBOX.equals(folder.type) &&
                                        (!EntityFolder.SENT.equals(folder.type) || EntityFolder.TRASH.equals(result.target.type)) &&
                                        !EntityFolder.TRASH.equals(folder.type) &&
                                        !EntityFolder.JUNK.equals(folder.type))
                                    result.ids.add(threaded.id);
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return result;
                    }

                    @Override
                    protected void onExecuted(Bundle args, MessageTarget result) {
                        moveAsk(result);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentMessages.this, args);
            }

            private void onActionNavigate(ViewModelMessages.Target target) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_THREAD)
                                .putExtra("account", target.account)
                                .putExtra("thread", target.thread)
                                .putExtra("id", target.id));
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
                                .putExtra("folder", drafts.id)
                                .putExtra("outgoing", drafts.isOutgoing()));

                return true;
            }
        });

        fabMore.setOnClickListener(new View.OnClickListener() {
            private final int action_seen = 1;
            private final int action_unseen = 2;
            private final int action_flag = 3;
            private final int action_unflag = 4;
            private final int action_archive = 5;
            private final int action_trash = 6;
            private final int action_delete = 7;
            private final int action_junk = 8;
            private final int action_move = 9;

            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putLong("folder", folder);
                args.putLongArray("ids", getSelection());

                new SimpleTask<Boolean[]>() {
                    @Override
                    protected Boolean[] onExecute(Context context, Bundle args) {
                        long fid = args.getLong("folder");
                        long[] ids = args.getLongArray("ids");

                        Boolean[] result = new Boolean[10];
                        for (int i = 0; i < result.length; i++)
                            result[i] = false;

                        DB db = DB.getInstance(context);

                        long account = -1;
                        for (long id : ids) {
                            EntityMessage message = db.message().getMessage(id);
                            if (message != null) {
                                account = message.account;
                                result[message.ui_seen ? 1 : 0] = true;
                                result[message.flagged ? 3 : 2] = true;
                            }
                        }

                        EntityFolder archive = db.folder().getFolderByType(account, EntityFolder.ARCHIVE);
                        EntityFolder trash = db.folder().getFolderByType(account, EntityFolder.TRASH);

                        result[4] = (archive != null);
                        result[5] = (trash != null);

                        EntityFolder folder = db.folder().getFolder(fid);
                        if (folder != null) {
                            result[6] = EntityFolder.ARCHIVE.equals(folder.type);
                            result[7] = EntityFolder.TRASH.equals(folder.type);
                            result[8] = EntityFolder.JUNK.equals(folder.type);
                            result[9] = EntityFolder.DRAFTS.equals(folder.type);
                        }

                        return result;
                    }

                    @Override
                    protected void onExecuted(Bundle args, final Boolean[] result) {
                        PopupMenu popupMenu = new PopupMenu(getContext(), fabMore);

                        if (result[0] && !result[9])
                            popupMenu.getMenu().add(Menu.NONE, action_seen, 1, R.string.title_seen);
                        if (result[1] && !result[9])
                            popupMenu.getMenu().add(Menu.NONE, action_unseen, 2, R.string.title_unseen);

                        if (result[2])
                            popupMenu.getMenu().add(Menu.NONE, action_flag, 3, R.string.title_flag);
                        if (result[3])
                            popupMenu.getMenu().add(Menu.NONE, action_unflag, 4, R.string.title_unflag);

                        if (result[4] && !result[6] && !result[9]) // has archive and not is archive
                            popupMenu.getMenu().add(Menu.NONE, action_archive, 5, R.string.title_archive);

                        if (result[5]) // has trash
                            if (result[7] || result[9]) // is trash or drafts
                                popupMenu.getMenu().add(Menu.NONE, action_delete, 6, R.string.title_trash);
                            else
                                popupMenu.getMenu().add(Menu.NONE, action_trash, 6, R.string.title_trash);

                        if (!result[8] && !result[9])
                            popupMenu.getMenu().add(Menu.NONE, action_junk, 6, R.string.title_spam);

                        if (!result[9])
                            popupMenu.getMenu().add(Menu.NONE, action_move, 7, R.string.title_move);

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem target) {
                                switch (target.getItemId()) {
                                    case action_seen:
                                        onActionSeen(true);
                                        return true;
                                    case action_unseen:
                                        onActionSeen(false);
                                        return true;
                                    case action_flag:
                                        onActionFlag(true);
                                        return true;
                                    case action_unflag:
                                        onActionFlag(false);
                                        return true;
                                    case action_archive:
                                        onActionMove(EntityFolder.ARCHIVE);
                                        return true;
                                    case action_trash:
                                        onActionMove(EntityFolder.TRASH);
                                        return true;
                                    case action_delete:
                                        onActionDelete();
                                        return true;
                                    case action_junk:
                                        onActionJunk();
                                        return true;
                                    case action_move:
                                        onActionMove();
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
                }.execute(FragmentMessages.this, args);
            }

            private long[] getSelection() {
                Selection<Long> selection = selectionTracker.getSelection();

                long[] ids = new long[selection.size()];
                int i = 0;
                for (Long id : selection)
                    ids[i++] = id;

                return ids;
            }

            private void onActionSeen(boolean seen) {
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
                }.execute(FragmentMessages.this, args);
            }

            private void onActionFlag(boolean flagged) {
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
                }.execute(FragmentMessages.this, args);
            }

            private void onActionJunk() {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setMessage(R.string.title_ask_spam)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onActionMove(EntityFolder.JUNK);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }

            private void onActionDelete() {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setMessage(R.string.title_ask_delete_selected)
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
                                                        if (threaded.uid == null && !TextUtils.isEmpty(threaded.error)) // outbox
                                                            db.message().deleteMessage(threaded.id);
                                                        else
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
                                }.execute(FragmentMessages.this, args);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }

            private void onActionMove(String type) {
                Bundle args = new Bundle();
                args.putString("type", type);
                args.putLongArray("ids", getSelection());

                selectionTracker.clearSelection();

                new SimpleTask<MessageTarget>() {
                    @Override
                    protected MessageTarget onExecute(Context context, Bundle args) {
                        String type = args.getString("type");
                        long[] ids = args.getLongArray("ids");

                        MessageTarget result = new MessageTarget();

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            long account = -1;
                            for (long id : ids) {
                                EntityMessage message = db.message().getMessage(id);
                                if (message != null) {
                                    account = message.account;
                                    List<EntityMessage> messages = db.message().getMessageByThread(
                                            message.account, message.thread, threading ? null : id, message.folder);
                                    for (EntityMessage threaded : messages)
                                        result.ids.add(threaded.id);
                                }
                            }

                            result.target = db.folder().getFolderByType(account, type);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return result;
                    }

                    @Override
                    protected void onExecuted(Bundle args, MessageTarget result) {
                        if (EntityFolder.JUNK.equals(result.target.type))
                            moveAskConfirmed(result);
                        else
                            moveAsk(result);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentMessages.this, args);
            }

            private void onActionMove() {
                Bundle args = new Bundle();
                args.putLong("folder", folder);
                args.putLongArray("ids", getSelection());

                new SimpleTask<List<EntityFolder>>() {
                    @Override
                    protected List<EntityFolder> onExecute(Context context, Bundle args) {
                        long fid = args.getLong("folder");
                        long[] ids = args.getLongArray("ids");

                        DB db = DB.getInstance(context);

                        long account = -1;
                        for (long id : ids) {
                            EntityMessage message = db.message().getMessage(id);
                            if (message != null) {
                                account = message.account;
                                break;
                            }
                        }

                        List<EntityFolder> folders = db.folder().getFolders(account);

                        List<EntityFolder> targets = new ArrayList<>();
                        for (EntityFolder folder : folders)
                            if (!folder.hide &&
                                    !EntityFolder.ARCHIVE.equals(folder.type) &&
                                    !EntityFolder.TRASH.equals(folder.type) &&
                                    !EntityFolder.JUNK.equals(folder.type) &&
                                    (fid < 0 ? !folder.unified : !folder.id.equals(fid)))
                                targets.add(folder);

                        EntityFolder.sort(context, targets);

                        return targets;
                    }

                    @Override
                    protected void onExecuted(final Bundle args, List<EntityFolder> folders) {
                        PopupMenu popupMenu = new PopupMenu(getContext(), popupAnchor);

                        int order = 0;
                        for (EntityFolder folder : folders)
                            popupMenu.getMenu().add(Menu.NONE, folder.id.intValue(), order++, folder.getDisplayName(getContext()));

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(final MenuItem target) {
                                args.putLong("target", target.getItemId());

                                selectionTracker.clearSelection();

                                new SimpleTask<MessageTarget>() {
                                    @Override
                                    protected MessageTarget onExecute(Context context, Bundle args) {
                                        long[] ids = args.getLongArray("ids");
                                        long target = args.getLong("target");

                                        MessageTarget result = new MessageTarget();

                                        DB db = DB.getInstance(context);
                                        try {
                                            db.beginTransaction();

                                            result.target = db.folder().getFolder(target);

                                            for (long id : ids) {
                                                EntityMessage message = db.message().getMessage(id);
                                                if (message != null) {
                                                    List<EntityMessage> messages = db.message().getMessageByThread(
                                                            message.account, message.thread, threading ? null : id, message.folder);
                                                    for (EntityMessage threaded : messages)
                                                        result.ids.add(threaded.id);
                                                }
                                            }

                                            db.setTransactionSuccessful();
                                        } finally {
                                            db.endTransaction();
                                        }

                                        return result;
                                    }

                                    @Override
                                    protected void onExecuted(Bundle args, MessageTarget result) {
                                        moveAsk(result);
                                    }

                                    @Override
                                    protected void onException(Bundle args, Throwable ex) {
                                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                                    }
                                }.execute(FragmentMessages.this, args);

                                return true;
                            }
                        });

                        popupMenu.show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentMessages.this, args);
            }
        });

        ((ActivityBase) getActivity()).addBackPressedListener(onBackPressedListener);

        // Initialize
        swipeRefresh.setEnabled(pull);
        tvNoEmail.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        fab.hide();
        fabMore.hide();

        return view;
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
        grpHintCompact.setVisibility(prefs.getBoolean("message_compact", false) || !hints ? View.GONE : View.VISIBLE);

        final DB db = DB.getInstance(getContext());

        // Primary account
        db.account().livePrimaryAccount().observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
            @Override
            public void onChanged(EntityAccount account) {
                primary = (account == null ? -1 : account.id);
                connected = (account != null && "connected".equals(account.state));
                getActivity().invalidateOptionsMenu();
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

                            outbox = EntityFolder.OUTBOX.equals(folder.type);
                            getActivity().invalidateOptionsMenu();
                        }

                        swipeRefresh.setRefreshing(
                                folder != null && folder.sync_state != null &&
                                        "connected".equals(EntityFolder.OUTBOX.equals(folder.type)
                                                ? folder.state : folder.accountState));
                    }
                });
                break;

            case THREAD:
                setSubtitle(R.string.title_folder_thread);
                break;

            case SEARCH:
                setSubtitle(getString(R.string.title_searching, search));
                break;
        }

        // Folders and messages
        db.folder().liveSystemFolders(account).observe(getViewLifecycleOwner(), new Observer<List<EntityFolder>>() {
            @Override
            public void onChanged(List<EntityFolder> folders) {
                if (folders == null)
                    folders = new ArrayList<>();

                archives.clear();
                trashes.clear();

                for (EntityFolder folder : folders)
                    if (EntityFolder.ARCHIVE.equals(folder.type))
                        archives.add(folder.account);
                    else if (EntityFolder.TRASH.equals(folder.type))
                        trashes.add(folder.account);

                loadMessages();
            }
        });

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
        grpSupport.setVisibility(Helper.isPro(getContext()) ? View.GONE : View.VISIBLE);

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        adapter.setZoom(zoom);
    }

    @Override
    public void onPause() {
        super.onPause();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
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
        searchView.setQueryHint(getString(R.string.title_search_hint));
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
                    }.execute(FragmentMessages.this, args);
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
        menu.findItem(R.id.menu_search).setVisible(
                folder >= 0 && viewType != AdapterMessage.ViewType.SEARCH);
        menu.findItem(R.id.menu_sort_on).setVisible(
                viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER);
        menu.findItem(R.id.menu_folders).setVisible(primary >= 0);
        menu.findItem(R.id.menu_folders).setIcon(connected ? R.drawable.baseline_folder_24 : R.drawable.baseline_folder_open_24);
        menu.findItem(R.id.menu_move_sent).setVisible(outbox);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = prefs.getString("sort", "time");
        if ("time".equals(sort))
            menu.findItem(R.id.menu_sort_on_time).setChecked(true);
        else if ("unread".equals(sort))
            menu.findItem(R.id.menu_sort_on_unread).setChecked(true);
        else if ("starred".equals(sort))
            menu.findItem(R.id.menu_sort_on_starred).setChecked(true);
        else if ("sender".equals(sort))
            menu.findItem(R.id.menu_sort_on_sender).setChecked(true);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

            case R.id.menu_folders:
                onMenuFolders();
                loadMessages();
                return true;

            case R.id.menu_move_sent:
                onMenuMoveSent();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuSort(String sort) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString("sort", sort).apply();
        loadMessages();
    }

    private void onMenuZoom() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        zoom = ++zoom % 3;
        prefs.edit().putInt("zoom", zoom).apply();
        adapter.setZoom(zoom);
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

    private void onMenuMoveSent() {
        Bundle args = new Bundle();
        args.putLong("folder", folder);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long outbox = args.getLong("folder");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (EntityMessage message : db.message().getMessageSeen(outbox))
                        if (message.identity != null) {
                            EntityIdentity identity = db.identity().getIdentity(message.identity);
                            EntityFolder sent = db.folder().getFolderByType(identity.account, EntityFolder.SENT);
                            if (sent != null) {
                                message.folder = sent.id;
                                message.uid = null;
                                db.message().updateMessage(message);
                                Log.i("Appending sent msgid=" + message.msgid);
                                EntityOperation.queue(context, db, message, EntityOperation.ADD); // Could already exist
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
        }.execute(this, args);
    }

    private void loadMessages() {
        final DB db = DB.getInstance(getContext());

        ViewModelBrowse model = ViewModelProviders.of(getActivity()).get(ViewModelBrowse.class);
        model.set(getContext(), folder, search, REMOTE_PAGE_SIZE);

        // Observe folder/messages/search
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = prefs.getString("sort", "time");
        boolean debug = prefs.getBoolean("debug", false);
        Log.i("Load messages type=" + viewType + " sort=" + sort + " debug=" + debug);

        // Sort changed
        if (messages != null)
            messages.removeObservers(getViewLifecycleOwner());

        LivePagedListBuilder<Integer, TupleMessageEx> builder = null;
        switch (viewType) {
            case UNIFIED:
                builder = new LivePagedListBuilder<>(
                        db.message().pagedUnifiedInbox(threading, sort, debug), LOCAL_PAGE_SIZE);
                break;

            case FOLDER:
                if (searchCallback == null)
                    searchCallback = new BoundaryCallbackMessages(this, model,
                            new BoundaryCallbackMessages.IBoundaryCallbackMessages() {
                                @Override
                                public void onLoading() {
                                    pbWait.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoaded() {
                                    pbWait.setVisibility(View.GONE);
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

                PagedList.Config configFolder = new PagedList.Config.Builder()
                        .setPageSize(LOCAL_PAGE_SIZE)
                        .setPrefetchDistance(REMOTE_PAGE_SIZE)
                        .build();
                builder = new LivePagedListBuilder<>(
                        db.message().pagedFolder(folder, threading, sort, false, debug), configFolder);
                builder.setBoundaryCallback(searchCallback);
                break;

            case THREAD:
                builder = new LivePagedListBuilder<>(
                        db.message().pagedThread(account, thread, threading ? null : id, debug), LOCAL_PAGE_SIZE);
                break;

            case SEARCH:
                if (searchCallback == null)
                    searchCallback = new BoundaryCallbackMessages(this, model,
                            new BoundaryCallbackMessages.IBoundaryCallbackMessages() {
                                @Override
                                public void onLoading() {
                                    tvNoEmail.setVisibility(View.GONE);
                                    pbWait.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoaded() {
                                    pbWait.setVisibility(View.GONE);
                                    if (messages.getValue() == null || messages.getValue().size() == 0)
                                        tvNoEmail.setVisibility(View.VISIBLE);
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

                PagedList.Config configSearch = new PagedList.Config.Builder()
                        .setPageSize(LOCAL_PAGE_SIZE)
                        .setPrefetchDistance(REMOTE_PAGE_SIZE)
                        .build();
                builder = new LivePagedListBuilder<>(
                        db.message().pagedFolder(folder, threading, "time", true, false), configSearch);
                builder.setBoundaryCallback(searchCallback);
                break;
        }

        builder.setFetchExecutor(executor);

        messages = builder.build();
        messages.observe(getViewLifecycleOwner(), new Observer<PagedList<TupleMessageEx>>() {
            @Override
            public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
                if (messages == null ||
                        (viewType == AdapterMessage.ViewType.THREAD && messages.size() == 0 && autoclose)) {
                    finish();
                    return;
                }

                if (viewType == AdapterMessage.ViewType.THREAD) {
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
                        if (autoCloseCount > 0 && autoclose) {
                            int count = 0;
                            for (int i = 0; i < messages.size(); i++) {
                                TupleMessageEx message = messages.get(i);
                                if (!EntityFolder.ARCHIVE.equals(message.folderType) &&
                                        !EntityFolder.SENT.equals(message.folderType) &&
                                        !EntityFolder.TRASH.equals(message.folderType) &&
                                        !EntityFolder.JUNK.equals(message.folderType))
                                    count++;
                            }
                            Log.i("Auto close=" + count);

                            // Auto close when:
                            // - no more non archived/trashed/sent messages

                            if (count == 0) {
                                finish();
                                return;
                            }
                        }
                    }

                    if (actionbar) {
                        ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
                        ViewModelMessages.Target[] pn = model.getPrevNext(thread);
                        bottom_navigation.setTag(pn);
                        bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(pn[0] != null);
                        bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(pn[1] != null);

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

                                return new Boolean[]{trashable, archivable};
                            }

                            @Override
                            protected void onExecuted(Bundle args, Boolean[] data) {
                                bottom_navigation.getMenu().findItem(R.id.action_delete).setVisible(trashes.size() > 0 && data[0]);
                                bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(archives.size() > 0 && data[1]);
                                bottom_navigation.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentMessages.this, args);
                    }

                } else {
                    ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
                    model.setMessages(messages);
                }

                Log.i("Submit messages=" + messages.size());
                adapter.submitList(messages);

                boolean searching = (searchCallback != null && searchCallback.isSearching());

                if (!searching)
                    pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);

                if (messages.size() == 0) {
                    tvNoEmail.setVisibility(searching ? View.GONE : View.VISIBLE);
                    rvMessage.setVisibility(View.GONE);
                } else {
                    tvNoEmail.setVisibility(View.GONE);
                    rvMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

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
                    EntityFolder folder = db.folder().getFolder(message.folder);

                    if (!message.content)
                        EntityOperation.queue(context, db, message, EntityOperation.BODY);

                    if (!message.ui_seen && !EntityFolder.OUTBOX.equals(folder.type))
                        EntityOperation.queue(context, db, message, EntityOperation.SEEN, true);

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
        }.execute(this, args);
    }

    private void moveAsk(final MessageTarget result) {
        if (result.target == null)
            return;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("automove", false)) {
            moveAskConfirmed(result);
            return;
        }

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ask_again, null);
        final TextView tvMessage = dview.findViewById(R.id.tvMessage);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

        tvMessage.setText(getResources().getQuantityString(
                R.plurals.title_moving_messages,
                result.ids.size(), result.ids.size(),
                result.target.getDisplayName(getContext())));

        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cbNotAgain.isChecked())
                            prefs.edit().putBoolean("automove", true).apply();
                        moveAskConfirmed(result);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void moveAskConfirmed(MessageTarget result) {
        Bundle args = new Bundle();
        args.putSerializable("result", result);

        // Move messages
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                try {
                    MessageTarget result = (MessageTarget) args.getSerializable("result");

                    db.beginTransaction();

                    for (long id : result.ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null) {
                            Log.i("Move id=" + id + " target=" + result.target.name);
                            EntityFolder folder = db.folder().getFolderByName(message.account, result.target.name);
                            EntityOperation.queue(context, db, message, EntityOperation.MOVE, folder.id);
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
        }.execute(FragmentMessages.this, args);
    }

    private void moveUndo(final MessageTarget result) {
        if (result.target == null)
            return;

        // Show undo snackbar
        final Snackbar snackbar = Snackbar.make(
                view,
                getString(R.string.title_moving, result.target.getDisplayName(getContext())),
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.title_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();

                Bundle args = new Bundle();
                args.putSerializable("result", result);

                // Show message again
                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        DB db = DB.getInstance(context);
                        MessageTarget result = (MessageTarget) args.getSerializable("result");
                        for (long id : result.ids) {
                            Log.i("Move undo id=" + id);
                            db.message().setMessageUiHide(id, false);
                        }
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(FragmentMessages.this, args);
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

                            for (long id : result.ids) {
                                EntityMessage message = db.message().getMessage(id);
                                if (message != null && message.ui_hide) {
                                    Log.i("Move id=" + id + " target=" + result.target.name);
                                    EntityFolder folder = db.folder().getFolderByName(message.account, result.target.name);
                                    EntityOperation.queue(context, db, message, EntityOperation.MOVE, folder.id);
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

    class MessageTarget implements Serializable {
        List<Long> ids = new ArrayList<>();
        EntityFolder target;
    }
}
