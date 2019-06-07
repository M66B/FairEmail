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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.bugsnag.android.Bugsnag;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;

public class FragmentMessages extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvSupport;
    private ImageButton ibHintSupport;
    private ImageButton ibHintSwipe;
    private ImageButton ibHintSelect;
    private TextView tvNoEmail;
    private FixedRecyclerView rvMessage;
    private SeekBar seekBar;
    private ImageButton ibDown;
    private ImageButton ibUp;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpSupport;
    private Group grpHintSupport;
    private Group grpHintSwipe;
    private Group grpHintSelect;
    private Group grpReady;
    private FloatingActionButton fab;
    private FloatingActionButton fabMore;
    private FloatingActionButton fabSearch;
    private FloatingActionButton fabError;

    private long account;
    private long folder;
    private boolean server;
    private String thread;
    private long id;
    private boolean found;
    private String query;
    private boolean pane;

    private boolean date;
    private boolean threading;
    private boolean pull;
    private boolean swipenav;
    private boolean autoscroll;
    private boolean actionbar;
    private boolean autoexpand;
    private boolean autoclose;
    private boolean autonext;
    private boolean addresses;

    private int colorPrimary;
    private int colorAccent;

    private long primary;
    private boolean outbox = false;
    private boolean connected;
    private boolean reset = false;
    private String searching = null;
    private boolean loading = false;
    private boolean manual = false;
    private Integer lastUnseen = null;
    private boolean swiping = false;

    private AdapterMessage adapter;

    private AdapterMessage.ViewType viewType;
    private SelectionPredicateMessage selectionPredicate = null;
    private SelectionTracker<Long> selectionTracker = null;

    private Long previous = null;
    private Long next = null;
    private Long closeNext = null;
    private int autoCloseCount = 0;
    private boolean autoExpanded = true;
    private Map<String, List<Long>> values = new HashMap<>();
    private LongSparseArray<Spanned> bodies = new LongSparseArray<>();
    private LongSparseArray<List<EntityAttachment>> attachments = new LongSparseArray<>();
    private LongSparseArray<TupleAccountSwipes> accountSwipes = new LongSparseArray<>();

    private NumberFormat nf = NumberFormat.getNumberInstance();

    private static final int UNDO_TIMEOUT = 5000; // milliseconds
    private static final int SWIPE_DISABLE_SELECT_DURATION = 1500; // milliseconds

    private static final List<String> DUPLICATE_ORDER = Collections.unmodifiableList(Arrays.asList(
            EntityFolder.INBOX,
            EntityFolder.OUTBOX,
            EntityFolder.DRAFTS,
            EntityFolder.SENT,
            EntityFolder.SYSTEM,
            EntityFolder.USER,
            EntityFolder.ARCHIVE,
            EntityFolder.TRASH,
            EntityFolder.JUNK
    ));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        account = args.getLong("account", -1);
        folder = args.getLong("folder", -1);
        server = args.getBoolean("server", false);
        thread = args.getString("thread");
        id = args.getLong("id", -1);
        found = args.getBoolean("found", false);
        query = args.getString("query");
        pane = args.getBoolean("pane", false);
        primary = args.getLong("primary", -1);
        connected = args.getBoolean("connected", false);

        if (TextUtils.isEmpty(query))
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

        swipenav = prefs.getBoolean("swipenav", true);
        autoscroll = (prefs.getBoolean("autoscroll", false) || viewType == AdapterMessage.ViewType.THREAD);
        date = prefs.getBoolean("date", true);
        threading = prefs.getBoolean("threading", true);
        actionbar = prefs.getBoolean("actionbar", true);
        autoexpand = prefs.getBoolean("autoexpand", true);
        autoclose = prefs.getBoolean("autoclose", true);
        autonext = (!autoclose && prefs.getBoolean("autonext", false));
        addresses = prefs.getBoolean("addresses", false);

        colorPrimary = Helper.resolveColor(getContext(), R.attr.colorPrimary);
        colorAccent = Helper.resolveColor(getContext(), R.attr.colorAccent);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_messages, container, false);

        // Get controls
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvSupport = view.findViewById(R.id.tvSupport);
        ibHintSupport = view.findViewById(R.id.ibHintSupport);
        ibHintSwipe = view.findViewById(R.id.ibHintSwipe);
        ibHintSelect = view.findViewById(R.id.ibHintSelect);
        tvNoEmail = view.findViewById(R.id.tvNoEmail);
        rvMessage = view.findViewById(R.id.rvMessage);
        seekBar = view.findViewById(R.id.seekBar);
        ibDown = view.findViewById(R.id.ibDown);
        ibUp = view.findViewById(R.id.ibUp);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpSupport = view.findViewById(R.id.grpSupport);
        grpHintSupport = view.findViewById(R.id.grpHintSupport);
        grpHintSwipe = view.findViewById(R.id.grpHintSwipe);
        grpHintSelect = view.findViewById(R.id.grpHintSelect);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);
        fabSearch = view.findViewById(R.id.fabSearch);
        fabMore = view.findViewById(R.id.fabMore);
        fabError = view.findViewById(R.id.fabError);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Wire controls

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
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
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
        final LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvMessage.setLayoutManager(llm);

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
        rvMessage.addItemDecoration(itemDecorator);

        DividerItemDecoration dateDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View view = parent.getChildAt(i);
                    int pos = parent.getChildAdapterPosition(view);
                    View header = getView(parent, pos);
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
                View header = getView(parent, pos);
                if (header == null)
                    outRect.setEmpty();
                else
                    outRect.top = header.getMeasuredHeight();
            }

            private View getView(RecyclerView parent, int pos) {
                if (!date || !"time".equals(adapter.getSort()))
                    return null;

                if (pos == RecyclerView.NO_POSITION)
                    return null;

                TupleMessageEx prev = (pos > 0 ? adapter.getCurrentList().get(pos - 1) : null);
                TupleMessageEx message = adapter.getCurrentList().get(pos);
                if (pos > 0 && prev == null)
                    return null;
                if (message == null)
                    return null;

                if (pos > 0) {
                    Calendar cal0 = Calendar.getInstance();
                    Calendar cal1 = Calendar.getInstance();
                    cal0.setTimeInMillis(prev.received);
                    cal1.setTimeInMillis(message.received);
                    int year0 = cal0.get(Calendar.YEAR);
                    int year1 = cal1.get(Calendar.YEAR);
                    int day0 = cal0.get(Calendar.DAY_OF_YEAR);
                    int day1 = cal1.get(Calendar.DAY_OF_YEAR);
                    if (year0 == year1 && day0 == day1)
                        return null;
                }

                View header = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_date, parent, false);
                TextView tvDate = header.findViewById(R.id.tvDate);
                tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, Helper.getTextSize(parent.getContext(), adapter.getZoom()));

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.DAY_OF_MONTH, -2);
                if (message.received <= cal.getTimeInMillis())
                    tvDate.setText(
                            DateUtils.formatDateRange(
                                    parent.getContext(),
                                    message.received,
                                    message.received,
                                    FORMAT_SHOW_WEEKDAY | FORMAT_SHOW_DATE));
                else
                    tvDate.setText(
                            DateUtils.getRelativeTimeSpanString(
                                    message.received,
                                    new Date().getTime(),
                                    DAY_IN_MILLIS, 0));

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }
        };
        rvMessage.addItemDecoration(dateDecorator);

        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        String sort = prefs.getString("sort", "time");
        boolean filter_duplicates = prefs.getBoolean("filter_duplicates", false);

        adapter = new AdapterMessage(
                getContext(), getViewLifecycleOwner(),
                viewType, compact, zoom, sort, filter_duplicates, iProperties);
        rvMessage.setAdapter(adapter);

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        ibDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToVisibleItem(llm, true);
            }
        });

        ibUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToVisibleItem(llm, false);
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        onActionMove(EntityFolder.TRASH);
                        return true;

                    case R.id.action_snooze:
                        onActionSnooze();
                        return true;

                    case R.id.action_archive:
                        onActionMove(EntityFolder.ARCHIVE);
                        return true;

                    case R.id.action_prev:
                        navigate(previous, true);
                        return true;

                    case R.id.action_next:
                        navigate(next, false);
                        return true;

                    default:
                        return false;
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ActivityCompose.class)
                        .putExtra("action", "new")
                        .putExtra("account", account)
                );
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
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
                }.execute(FragmentMessages.this, args, "messages:drafts");

                return true;
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folder < 0) {
                    Bundle args = new Bundle();

                    new SimpleTask<Map<EntityAccount, List<EntityFolder>>>() {
                        @Override
                        protected Map<EntityAccount, List<EntityFolder>> onExecute(Context context, Bundle args) {
                            Map<EntityAccount, List<EntityFolder>> result = new LinkedHashMap<>();

                            DB db = DB.getInstance(context);
                            List<EntityAccount> accounts = db.account().getSynchronizingAccounts();

                            for (EntityAccount account : accounts) {
                                List<EntityFolder> folders = db.folder().getFolders(account.id);
                                if (folders.size() > 0)
                                    Collections.sort(folders, folders.get(0).getComparator(context));
                                result.put(account, folders);
                            }

                            return result;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Map<EntityAccount, List<EntityFolder>> result) {
                            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), fabSearch);

                            popupMenu.getMenu().add(R.string.title_search_in).setEnabled(false);

                            int order = 1;
                            for (EntityAccount account : result.keySet()) {
                                SubMenu smenu = popupMenu.getMenu()
                                        .addSubMenu(Menu.NONE, 0, order++, account.name);
                                int sorder = 1;
                                for (EntityFolder folder : result.get(account)) {
                                    MenuItem item = smenu.add(Menu.NONE, 1, sorder++, folder.getDisplayName(getContext()));
                                    item.setIntent(new Intent().putExtra("target", folder.id));
                                }
                            }

                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem target) {
                                    Intent intent = target.getIntent();
                                    if (intent == null)
                                        return false;

                                    long folder = intent.getLongExtra("target", -1);
                                    search(getContext(), getViewLifecycleOwner(), getFragmentManager(), folder, true, query);

                                    return true;
                                }
                            });

                            popupMenu.show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                        }
                    }.execute(FragmentMessages.this, args, "messages:search");
                } else
                    search(getContext(), getViewLifecycleOwner(), getFragmentManager(), folder, true, query);
            }
        });

        fabMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMore();
            }
        });

        fabError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuFolders(account);
            }
        });

        addBackPressedListener(onBackPressedListener);

        // Initialize
        tvNoEmail.setVisibility(View.GONE);
        seekBar.setVisibility(View.GONE);
        ibDown.setVisibility(View.GONE);
        ibUp.setVisibility(View.GONE);
        bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(false);
        bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(false);
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        fab.hide();
        if (viewType == AdapterMessage.ViewType.SEARCH && !server)
            fabSearch.show();
        else
            fabSearch.hide();
        fabMore.hide();
        fabError.hide();

        if (viewType == AdapterMessage.ViewType.THREAD) {
            ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getViewLifecycleOwner(), id, new ViewModelMessages.IPrevNext() {
                @Override
                public void onPrevious(boolean exists, Long id) {
                    previous = id;
                    bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(id != null);
                }

                @Override
                public void onNext(boolean exists, Long id) {
                    next = id;
                    bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(id != null);
                }

                @Override
                public void onFound(int position, int size) {
                    if (actionbar) {
                        seekBar.setMax(size - 1);
                        seekBar.setProgress(size - 1 - position);
                        seekBar.getProgressDrawable().setAlpha(0);
                        seekBar.getThumb().setColorFilter(
                                position == 0 || position == size - 1 ? colorAccent : colorPrimary,
                                PorterDuff.Mode.SRC_IN);
                        seekBar.setVisibility(size > 1 ? View.VISIBLE : View.GONE);
                    }
                }
            });

            if (swipenav) {
                Log.i("Swipe navigation");

                final SwipeListener swipeListener = new SwipeListener(getContext(), new SwipeListener.ISwipeListener() {
                    @Override
                    public boolean onSwipeRight() {
                        if (previous == null) {
                            Animation bounce = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_right);
                            view.startAnimation(bounce);
                        } else
                            navigate(previous, true);

                        return (previous != null);
                    }

                    @Override
                    public boolean onSwipeLeft() {
                        if (next == null) {
                            Animation bounce = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_left);
                            view.startAnimation(bounce);
                        } else
                            navigate(next, false);

                        return (next != null);
                    }
                });

                rvMessage.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent ev) {
                        swipeListener.onTouch(rv, ev);
                        return false;
                    }

                    @Override
                    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent ev) {
                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                    }
                });
            } else
                new ItemTouchHelper(touchHelper).attachToRecyclerView(rvMessage);
        } else {
            new ItemTouchHelper(touchHelper).attachToRecyclerView(rvMessage);

            selectionPredicate = new SelectionPredicateMessage(rvMessage);

            selectionTracker = new SelectionTracker.Builder<>(
                    "messages-selection",
                    rvMessage,
                    new ItemKeyProviderMessage(rvMessage),
                    new ItemDetailsLookupMessage(rvMessage),
                    StorageStrategy.createLongStorage())
                    .withSelectionPredicate(selectionPredicate)
                    .build();
            adapter.setSelectionTracker(selectionTracker);

            selectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
                @Override
                public void onSelectionChanged() {
                    FragmentActivity activity = getActivity();
                    if (activity != null)
                        activity.invalidateOptionsMenu();

                    if (selectionTracker != null && selectionTracker.hasSelection())
                        fabMore.show();
                    else
                        fabMore.hide();
                    updateSwipeRefresh();
                }
            });
        }

        updateSwipeRefresh();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateSwipeRefresh() {
        swipeRefresh.setEnabled(pull && !swiping && (selectionTracker == null || !selectionTracker.hasSelection()));
    }

    private void scrollToVisibleItem(LinearLayoutManager llm, boolean bottom) {
        int pos = llm.findLastVisibleItemPosition();
        if (pos == RecyclerView.NO_POSITION)
            return;

        do {
            Long key = adapter.getKeyAtPosition(pos);
            if (key != null && isExpanded(key)) {
                int first = llm.findFirstVisibleItemPosition();
                View child = rvMessage.getChildAt(pos - (first < 0 ? 0 : first));

                if (child != null) {
                    TranslateAnimation bounce = new TranslateAnimation(
                            0, 0, Helper.dp2pixels(getContext(), bottom ? -12 : 12), 0);
                    bounce.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
                    child.startAnimation(bounce);
                }

                if (bottom && child != null)
                    llm.scrollToPositionWithOffset(pos, rvMessage.getHeight() - llm.getDecoratedMeasuredHeight(child));
                else
                    rvMessage.scrollToPosition(pos);

                break;
            }
            pos--;
        } while (pos >= 0);
    }

    private void onSwipeRefresh() {
        Bundle args = new Bundle();
        args.putLong("folder", folder);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                manual = true;
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");

                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalArgumentException(context.getString(R.string.title_no_internet));

                boolean now = true;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<EntityFolder> folders = new ArrayList<>();
                    if (fid < 0)
                        folders.addAll(db.folder().getFoldersSynchronizingUnified());
                    else {
                        EntityFolder folder = db.folder().getFolder(fid);
                        if (folder != null)
                            folders.add(folder);
                    }

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
                manual = false;
                swipeRefresh.setRefreshing(false);
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
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
                if (!values.get(name).contains(id))
                    values.get(name).add(id);
            } else
                values.get(name).remove(id);

            if ("expanded".equals(name)) {
                updateExpanded();
                if (enabled)
                    handleExpand(id);
            }
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
        public void setBody(long id, Spanned value) {
            if (value == null)
                bodies.remove(id);
            else
                bodies.put(id, value);
        }

        @Override
        public Spanned getBody(long id) {
            return bodies.get(id);
        }

        @Override
        public void setAttchments(long id, List<EntityAttachment> list) {
            attachments.put(id, list);
        }

        @Override
        public List<EntityAttachment> getAttachments(long id) {
            return attachments.get(id);
        }

        @Override
        public void scrollTo(final int pos) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    rvMessage.scrollToPosition(pos);
                }
            });
        }

        @Override
        public void scrollBy(final int dx, final int dy) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    rvMessage.scrollBy(dx, dy);
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

        @Override
        public void finish() {
            FragmentMessages.this.finish();
        }
    };

    private ItemTouchHelper.Callback touchHelper = new ItemTouchHelper.Callback() {
        private Handler handler = new Handler();

        private Runnable enableSelection = new Runnable() {
            @Override
            public void run() {
                if (selectionPredicate != null)
                    selectionPredicate.setEnabled(true);
            }
        };

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
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            if (selectionPredicate != null) {
                handler.removeCallbacks(enableSelection);
                if (isCurrentlyActive)
                    selectionPredicate.setEnabled(false);
                else
                    handler.postDelayed(enableSelection, SWIPE_DISABLE_SELECT_DURATION);
            }

            TupleMessageEx message = getMessage(viewHolder);
            if (message == null)
                return;

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null)
                return;

            AdapterMessage.ViewHolder holder = ((AdapterMessage.ViewHolder) viewHolder);
            Rect rect = holder.getItemRect();
            int margin = Helper.dp2pixels(getContext(), 12);
            int size = Helper.dp2pixels(getContext(), 24);

            if (dX > 0) {
                // Right swipe
                Drawable d = getResources().getDrawable(EntityFolder.getIcon(swipes.right_type), getContext().getTheme());
                d.setAlpha(Math.round(255 * Math.min(dX / (2 * margin + size), 1.0f)));
                int padding = (rect.height() - size);
                d.setBounds(
                        rect.left + margin,
                        rect.top + padding / 2,
                        rect.left + margin + size,
                        rect.top + padding / 2 + size);
                d.draw(canvas);
            } else if (dX < 0) {
                // Left swipe
                Drawable d = getResources().getDrawable(EntityFolder.getIcon(swipes.left_type), getContext().getTheme());
                d.setAlpha(Math.round(255 * Math.min(-dX / (2 * margin + size), 1.0f)));
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
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            swiping = (actionState == ItemTouchHelper.ACTION_STATE_SWIPE);
            updateSwipeRefresh();
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
                        if (target == null)
                            throw new IllegalArgumentException(context.getString(R.string.title_no_folder));

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

            PagedList<TupleMessageEx> list = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList();
            if (pos >= list.size())
                return null;

            TupleMessageEx message = list.get(pos);
            if (message == null || message.uid == null)
                return null;

            if (isExpanded(message.id))
                return null;

            if (EntityFolder.OUTBOX.equals(message.folderType))
                return null;

            return message;
        }
    };

    private boolean isExpanded(long id) {
        return (values.containsKey("expanded") && values.get("expanded").contains(id));
    }

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

    private void onActionSnooze() {
        DialogDuration.show(getContext(), getViewLifecycleOwner(), R.string.title_snooze,
                new DialogDuration.IDialogDuration() {
                    @Override
                    public void onDurationSelected(long duration, long time) {
                        if (!Helper.isPro(getContext())) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                            lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                            return;
                        }

                        Bundle args = new Bundle();
                        args.putLong("account", account);
                        args.putString("thread", thread);
                        args.putLong("id", id);
                        args.putLong("wakeup", duration == 0 ? -1 : time);

                        new SimpleTask<Long>() {
                            @Override
                            protected Long onExecute(Context context, Bundle args) {
                                long account = args.getLong("account");
                                String thread = args.getString("thread");
                                long id = args.getLong("id");
                                Long wakeup = args.getLong("wakeup");
                                if (wakeup < 0)
                                    wakeup = null;

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    List<EntityMessage> messages = db.message().getMessageByThread(
                                            account, thread, threading ? null : id, null);
                                    for (EntityMessage threaded : messages) {
                                        db.message().setMessageSnoozed(threaded.id, wakeup);
                                        EntityMessage.snooze(context, threaded.id, wakeup);
                                        EntityOperation.queue(context, threaded, EntityOperation.SEEN, true);
                                    }

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                return wakeup;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Long wakeup) {
                                if (wakeup != null)
                                    finish();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(getContext(), getViewLifecycleOwner(), args, "message:snooze");
                    }

                    @Override
                    public void onDismiss() {
                    }
                });
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

                result.folders = new ArrayList<>();
                for (long id : ids) {
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        continue;

                    if (!result.folders.contains(message.folder))
                        result.folders.add(message.folder);

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

                result.accounts = db.account().getSynchronizingAccounts();

                for (EntityAccount account : result.accounts)
                    result.targets.put(account.id, db.folder().getFoldersEx(account.id));

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, final MoreResult result) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), fabMore);

                if (result.unseen) // Unseen, not draft
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_seen, 1, R.string.title_seen);
                if (result.seen) // Seen, not draft
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unseen, 2, R.string.title_unseen);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_snooze, 3, R.string.title_snooze);

                if (result.unflagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag, 4, R.string.title_flag);
                if (result.flagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unflag, 5, R.string.title_unflag);
                if (result.unflagged || result.flagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag_color, 6, R.string.title_flag_color);

                if (result.hasArchive && !result.isArchive) // has archive and not is archive/drafts
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_archive, 7, R.string.title_archive);

                int order = 8;
                for (EntityAccount account : result.accounts) {
                    MenuItem item = popupMenu.getMenu()
                            .add(Menu.NONE, R.string.title_move_to_account, order++,
                                    getString(R.string.title_move_to_account, account.name));
                    item.setIntent(new Intent().putExtra("account", account.id));
                }

                if (result.isTrash) // is trash
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, order++, R.string.title_delete);

                if (!result.isTrash && result.hasTrash) // not trash and has trash
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_trash, order++, R.string.title_trash);

                if (result.hasJunk && !result.isJunk && !result.isDrafts) // has junk and not junk/drafts
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_spam, order++, R.string.title_spam);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        switch (target.getItemId()) {
                            case R.string.title_seen:
                                onActionSeenSelection(true);
                                return true;
                            case R.string.title_unseen:
                                onActionSeenSelection(false);
                                return true;
                            case R.string.title_snooze:
                                onActionSnoozeSelection();
                                return true;
                            case R.string.title_flag:
                                onActionFlagSelection(true, null);
                                return true;
                            case R.string.title_unflag:
                                onActionFlagSelection(false, null);
                                return true;
                            case R.string.title_flag_color:
                                onActionFlagColorSelection();
                                return true;
                            case R.string.title_archive:
                                onActionMoveSelection(EntityFolder.ARCHIVE);
                                return true;
                            case R.string.title_delete:
                                onActionDeleteSelection();
                                return true;
                            case R.string.title_trash:
                                onActionMoveSelection(EntityFolder.TRASH);
                                return true;
                            case R.string.title_spam:
                                onActionJunkSelection();
                                return true;
                            case R.string.title_move_to_account:
                                long account = target.getIntent().getLongExtra("account", -1);
                                onActionMoveSelectionAccount(account, result.targets.get(account), result.folders);
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
                                EntityOperation.queue(context, threaded, EntityOperation.SEEN, seen);
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
                        if (!Helper.isPro(getContext())) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                            lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                            return;
                        }

                        Bundle args = new Bundle();
                        args.putLongArray("ids", getSelection());
                        args.putLong("wakeup", duration == 0 ? -1 : time);

                        selectionTracker.clearSelection();

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long[] ids = args.getLongArray("ids");
                                Long wakeup = args.getLong("wakeup");
                                if (wakeup < 0)
                                    wakeup = null;

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    for (long id : ids) {
                                        EntityMessage message = db.message().getMessage(id);
                                        if (message != null) {
                                            List<EntityMessage> messages = db.message().getMessageByThread(
                                                    message.account, message.thread, threading ? null : id, message.folder);
                                            for (EntityMessage threaded : messages) {
                                                db.message().setMessageSnoozed(threaded.id, wakeup);
                                                EntityMessage.snooze(context, threaded.id, wakeup);
                                                EntityOperation.queue(context, threaded, EntityOperation.SEEN, true);
                                            }
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
                        }.execute(FragmentMessages.this, args, "messages:snooze");
                    }

                    @Override
                    public void onDismiss() {
                    }
                });
    }

    private void onActionFlagSelection(boolean flagged, Integer color) {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());
        args.putBoolean("flagged", flagged);
        if (color != null)
            args.putInt("color", color);

        selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean flagged = args.getBoolean("flagged");
                Integer color = (args.containsKey("color") ? args.getInt("color") : null);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null) {
                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    message.account, message.thread, threading ? null : id, message.folder);
                            for (EntityMessage threaded : messages)
                                EntityOperation.queue(context, threaded, EntityOperation.FLAG, flagged, color);
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

    private void onActionFlagColorSelection() {
        int[] colors = getResources().getIntArray(R.array.colorPicker);
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
        colorPickerDialog.initialize(R.string.title_flag_color, colors, Color.TRANSPARENT, 4, colors.length);
        colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                if (!Helper.isPro(getContext())) {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                    lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                    return;
                }

                onActionFlagSelection(true, color);
            }
        });
        colorPickerDialog.show(getFragmentManager(), "colorpicker");
    }

    private void onActionDeleteSelection() {
        Bundle args = new Bundle();
        args.putLongArray("selected", getSelection());

        new SimpleTask<List<Long>>() {
            @Override
            protected List<Long> onExecute(Context context, Bundle args) {
                long[] selected = args.getLongArray("selected");
                List<Long> ids = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : selected) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null) {
                            List<EntityMessage> messages = db.message().getMessageByThread(
                                    message.account, message.thread, threading ? null : id, message.folder);
                            for (EntityMessage threaded : messages)
                                ids.add(threaded.id);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return ids;
            }

            @Override
            protected void onExecuted(Bundle args, final List<Long> ids) {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setMessage(getResources().getQuantityString(R.plurals.title_deleting_messages, ids.size(), ids.size()))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle args = new Bundle();
                                args.putLongArray("ids", Helper.toLongArray(ids));

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
                                                if (message != null)
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
                                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                                    }
                                }.execute(FragmentMessages.this, args, "messages:delete:execute");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:delete:ask");
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

    private void onActionMoveSelectionAccount(long account, List<TupleFolderEx> folders, List<Long> disabled) {
        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_folder_select, null);
        final RecyclerView rvFolder = dview.findViewById(R.id.rvFolder);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        final Dialog dialog = new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setTitle(R.string.title_move_to_folder)
                .setView(dview)
                .create();

        rvFolder.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvFolder.setLayoutManager(llm);

        final AdapterFolder adapter = new AdapterFolder(getContext(), getViewLifecycleOwner(), account,
                new AdapterFolder.IFolderSelectedListener() {
                    @Override
                    public void onFolderSelected(TupleFolderEx folder) {
                        dialog.dismiss();
                        onActionMoveSelection(folder.id);
                    }
                });

        adapter.setDisabled(disabled);
        adapter.set(folders);

        rvFolder.setAdapter(adapter);

        rvFolder.setVisibility(View.VISIBLE);
        pbWait.setVisibility(View.GONE);
        dialog.show();
    }

    private void onActionMoveSelection(long target) {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());
        args.putLong("target", target);

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
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:reset", reset);
        outState.putString("fair:searching", searching);

        outState.putBoolean("fair:autoExpanded", autoExpanded);
        outState.putInt("fair:autoCloseCount", autoCloseCount);

        outState.putStringArray("fair:values", values.keySet().toArray(new String[0]));
        for (String name : values.keySet())
            outState.putLongArray("fair:name:" + name, Helper.toLongArray(values.get(name)));

        if (rvMessage != null) {
            Parcelable rv = rvMessage.getLayoutManager().onSaveInstanceState();
            outState.putParcelable("fair:rv", rv);
        }

        if (selectionTracker != null)
            selectionTracker.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            reset = savedInstanceState.getBoolean("fair:reset");
            searching = savedInstanceState.getString("fair:searching");

            autoExpanded = savedInstanceState.getBoolean("fair:autoExpanded");
            autoCloseCount = savedInstanceState.getInt("fair:autoCloseCount");

            for (String name : savedInstanceState.getStringArray("fair:values")) {
                values.put(name, new ArrayList<Long>());
                for (Long value : savedInstanceState.getLongArray("fair:name:" + name))
                    values.get(name).add(value);
            }

            if (rvMessage != null) {
                Parcelable rv = savedInstanceState.getBundle("fair:rv");
                rvMessage.getLayoutManager().onRestoreInstanceState(rv);
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

        db.answer().liveAnswerCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                adapter.setAnswerCount(count == null ? -1 : count);
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

                        updateState(folders);
                    }
                });
                db.message().liveHidden(null).observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
                    @Override
                    public void onChanged(List<Long> ids) {
                        if (ids != null && selectionTracker != null)
                            for (long id : ids)
                                selectionTracker.deselect(id);
                    }
                });
                break;

            case FOLDER:
                db.folder().liveFolderEx(folder).observe(getViewLifecycleOwner(), new Observer<TupleFolderEx>() {
                    @Override
                    public void onChanged(@Nullable TupleFolderEx folder) {
                        List<TupleFolderEx> folders = new ArrayList<>();
                        if (folder != null)
                            folders.add(folder);

                        updateState(folders);

                        boolean outbox = EntityFolder.OUTBOX.equals(folder.type);
                        if (FragmentMessages.this.outbox != outbox) {
                            FragmentMessages.this.outbox = outbox;
                            getActivity().invalidateOptionsMenu();
                        }
                    }
                });
                db.message().liveHidden(folder).observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
                    @Override
                    public void onChanged(List<Long> ids) {
                        if (ids != null && selectionTracker != null)
                            for (long id : ids)
                                selectionTracker.deselect(id);
                    }
                });
                break;

            case THREAD:
                db.message().liveThreadStats(account, thread, null).observe(getViewLifecycleOwner(), new Observer<TupleThreadStats>() {
                    Integer lastUnseen = null;

                    @Override
                    public void onChanged(TupleThreadStats stats) {
                        setSubtitle(getString(R.string.title_folder_thread,
                                stats == null || stats.accountName == null ? "" : stats.accountName));

                        if (stats != null && stats.count != null && stats.seen != null) {
                            int unseen = stats.count - stats.seen;
                            if (lastUnseen == null || lastUnseen != unseen) {
                                if (autoscroll && lastUnseen != null && lastUnseen < unseen)
                                    loadMessages(true);
                                lastUnseen = unseen;
                            }
                        }
                    }
                });
                db.message().liveHidden(account, thread).observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
                    @Override
                    public void onChanged(List<Long> ids) {
                        if (ids != null) {
                            for (long id : ids) {
                                Log.i("Hidden id=" + id);
                                for (String key : values.keySet())
                                    values.get(key).remove(id);
                                bodies.remove(id);
                                attachments.remove(id);
                            }
                            updateExpanded();
                        }
                    }
                });
                break;

            case SEARCH:
                setSubtitle(getString(R.string.title_searching, query));
                break;
        }

        loadMessages(false);

        updateExpanded();

        if (selectionTracker != null && selectionTracker.hasSelection())
            fabMore.show();
        else
            fabMore.hide();

        if (viewType != AdapterMessage.ViewType.THREAD && viewType != AdapterMessage.ViewType.SEARCH) {
            db.identity().liveComposableIdentities(account < 0 ? null : account).observe(getViewLifecycleOwner(),
                    new Observer<List<TupleIdentityEx>>() {
                        @Override
                        public void onChanged(List<TupleIdentityEx> identities) {
                            if (identities == null || identities.size() == 0)
                                fab.hide();
                            else
                                fab.show();
                        }
                    });
        }

        checkReporting();
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        adapter.setCompact(compact);
        adapter.setZoom(zoom);

        // Restart spinner
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
            swipeRefresh.setRefreshing(true);
        }

        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "pro");
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("pro".equals(key)) {
            boolean pro = prefs.getBoolean(key, false);
            grpSupport.setVisibility(
                    viewType == AdapterMessage.ViewType.THREAD || pro
                            ? View.GONE : View.VISIBLE);
        }
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

    private void checkReporting() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("crash_reports", false) ||
                prefs.getBoolean("crash_reports_asked", false))
            return;

        final Snackbar snackbar = Snackbar.make(view, R.string.title_ask_help, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                askReporting();
            }
        });

        snackbar.show();
    }

    private void askReporting() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_error_reporting, null);
        final Button btnInfo = dview.findViewById(R.id.btnInfo);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

        final Intent info = new Intent(Intent.ACTION_VIEW);
        info.setData(Uri.parse(Helper.FAQ_URI + "#user-content-faq104"));
        info.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        btnInfo.setVisibility(
                info.resolveActivity(getContext().getPackageManager()) == null ? View.GONE : View.VISIBLE);

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(info);
            }
        });

        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setView(dview)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().putBoolean("crash_reports", true).apply();
                        if (cbNotAgain.isChecked())
                            prefs.edit().putBoolean("crash_reports_asked", true).apply();
                        Bugsnag.startSession();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cbNotAgain.isChecked())
                            prefs.edit().putBoolean("crash_reports_asked", true).apply();
                    }
                })
                .show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_messages, menu);

        final MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setQueryHint(getString(R.string.title_search));

        if (!TextUtils.isEmpty(searching)) {
            menuSearch.expandActionView();
            searchView.setQuery(searching, false);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                searching = newText;
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = null;
                menuSearch.collapseActionView();
                search(
                        getContext(), getViewLifecycleOwner(), getFragmentManager(),
                        folder, false, query);
                return true;
            }
        });

        menu.findItem(R.id.menu_folders).setActionView(R.layout.action_button);
        ImageButton ib = (ImageButton) menu.findItem(R.id.menu_folders).getActionView();
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuFolders(primary);
            }
        });
        ib.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bundle args = new Bundle();

                new SimpleTask<List<EntityAccount>>() {
                    @Override
                    protected List<EntityAccount> onExecute(Context context, Bundle args) {
                        DB db = DB.getInstance(context);
                        return db.account().getSynchronizingAccounts();
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                        final ArrayAdapter<EntityAccount> adapter =
                                new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, accounts);

                        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        EntityAccount account = adapter.getItem(which);
                                        onMenuFolders(account.id);
                                    }
                                })
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(getContext(), getViewLifecycleOwner(), args, "messages:accounts");
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        menu.findItem(R.id.menu_search).setVisible(
                viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER);

        menu.findItem(R.id.menu_folders).setVisible(viewType == AdapterMessage.ViewType.UNIFIED && primary >= 0);
        ImageButton ib = (ImageButton) menu.findItem(R.id.menu_folders).getActionView();
        ib.setImageResource(connected
                ? R.drawable.baseline_folder_special_24 : R.drawable.baseline_folder_open_24);

        menu.findItem(R.id.menu_sort_on).setVisible(
                viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER);

        String sort = prefs.getString("sort", "time");
        if ("time".equals(sort))
            menu.findItem(R.id.menu_sort_on_time).setChecked(true);
        else if ("unread".equals(sort))
            menu.findItem(R.id.menu_sort_on_unread).setChecked(true);
        else if ("starred".equals(sort))
            menu.findItem(R.id.menu_sort_on_starred).setChecked(true);
        else if ("sender".equals(sort))
            menu.findItem(R.id.menu_sort_on_sender).setChecked(true);
        else if ("subject".equals(sort))
            menu.findItem(R.id.menu_sort_on_subject).setChecked(true);
        else if ("size".equals(sort))
            menu.findItem(R.id.menu_sort_on_size).setChecked(true);
        else if ("snoozed".equals(sort))
            menu.findItem(R.id.menu_sort_on_snoozed).setChecked(true);

        menu.findItem(R.id.menu_filter).setVisible(viewType != AdapterMessage.ViewType.SEARCH && !outbox);
        menu.findItem(R.id.menu_filter_seen).setVisible(viewType != AdapterMessage.ViewType.THREAD);
        menu.findItem(R.id.menu_filter_unflagged).setVisible(viewType != AdapterMessage.ViewType.THREAD);
        menu.findItem(R.id.menu_filter_snoozed).setVisible(viewType != AdapterMessage.ViewType.THREAD);
        menu.findItem(R.id.menu_filter_duplicates).setVisible(viewType == AdapterMessage.ViewType.THREAD);
        menu.findItem(R.id.menu_filter_seen).setChecked(prefs.getBoolean("filter_seen", false));
        menu.findItem(R.id.menu_filter_unflagged).setChecked(prefs.getBoolean("filter_unflagged", false));
        menu.findItem(R.id.menu_filter_snoozed).setChecked(prefs.getBoolean("filter_snoozed", true));
        menu.findItem(R.id.menu_filter_duplicates).setChecked(prefs.getBoolean("filter_duplicates", false));

        menu.findItem(R.id.menu_compact).setChecked(prefs.getBoolean("compact", false));

        menu.findItem(R.id.menu_select_all).setVisible(!outbox &&
                (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER));

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_folders:
                // Obsolete
                onMenuFolders(primary);
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

            case R.id.menu_sort_on_subject:
                item.setChecked(true);
                onMenuSort("subject");
                return true;

            case R.id.menu_sort_on_size:
                item.setChecked(true);
                onMenuSort("size");
                return true;

            case R.id.menu_sort_on_snoozed:
                item.setChecked(true);
                onMenuSort("snoozed");
                return true;

            case R.id.menu_filter_seen:
                onMenuFilterRead(!item.isChecked());
                return true;

            case R.id.menu_filter_unflagged:
                onMenuFilterUnflagged(!item.isChecked());
                return true;

            case R.id.menu_filter_snoozed:
                onMenuFilterSnoozed(!item.isChecked());
                return true;

            case R.id.menu_filter_duplicates:
                onMenuFilterDuplicates(!item.isChecked());
                return true;

            case R.id.menu_zoom:
                onMenuZoom();
                return true;

            case R.id.menu_compact:
                onMenuCompact();
                return true;

            case R.id.menu_select_all:
                onMenuSelectAll();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuFolders(long account) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getFragmentManager().popBackStack("unified", 0);

        Bundle args = new Bundle();
        args.putLong("account", account);

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
        loadMessages(true);
    }

    private void onMenuFilterRead(boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("filter_seen", filter).apply();
        getActivity().invalidateOptionsMenu();
        if (selectionTracker != null)
            selectionTracker.clearSelection();
        loadMessages(true);
    }

    private void onMenuFilterUnflagged(boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("filter_unflagged", filter).apply();
        getActivity().invalidateOptionsMenu();
        if (selectionTracker != null)
            selectionTracker.clearSelection();
        loadMessages(true);
    }

    private void onMenuFilterSnoozed(boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("filter_snoozed", filter).apply();
        getActivity().invalidateOptionsMenu();
        if (selectionTracker != null)
            selectionTracker.clearSelection();
        loadMessages(true);
    }

    private void onMenuFilterDuplicates(boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("filter_duplicates", filter).apply();
        getActivity().invalidateOptionsMenu();
        adapter.setFilterDuplicates(filter);
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

    private void onMenuSelectAll() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean snoozed = prefs.getBoolean("snoozed", false);

        Bundle args = new Bundle();
        args.putLong("id", folder);
        args.putBoolean("snoozed", snoozed);

        new SimpleTask<List<Long>>() {
            @Override
            protected List<Long> onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                boolean snoozed = args.getBoolean("snoozed");

                DB db = DB.getInstance(context);
                return db.message().getMessageAll(id < 0 ? null : id, snoozed);
            }

            @Override
            protected void onExecuted(Bundle args, List<Long> ids) {
                for (long id : ids)
                    selectionTracker.select(id);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "messages:all");
    }

    private void updateState(List<TupleFolderEx> folders) {
        Log.i("Folder state updated count=" + folders.size());

        // Get state
        int unseen = 0;
        boolean errors = false;
        boolean refreshing = false;
        for (TupleFolderEx folder : folders) {
            unseen += folder.unseen;
            if (folder.error != null && folder.account != null /* outbox */)
                errors = true;
            if (folder.sync_state != null &&
                    (folder.account == null || "connected".equals(folder.accountState))) {
                refreshing = true;
                break;
            }
        }

        // Get name
        String name;
        if (viewType == AdapterMessage.ViewType.UNIFIED)
            name = getString(R.string.title_folder_unified);
        else
            name = (folders.size() > 0 ? folders.get(0).getDisplayName(getContext()) : "");

        // Show name/unread
        if (unseen == 0)
            setSubtitle(name);
        else
            setSubtitle(getString(R.string.title_name_count, name, nf.format(unseen)));

        if (errors)
            fabError.show();
        else
            fabError.hide();

        // Auto scroll
        if (lastUnseen == null || lastUnseen != unseen) {
            if ((!refreshing && manual) ||
                    (autoscroll && lastUnseen != null && lastUnseen < unseen))
                loadMessages(true);
            manual = false;
            lastUnseen = unseen;
        }

        swipeRefresh.setRefreshing(refreshing);
    }

    private void loadMessages(final boolean top) {
        if (viewType == AdapterMessage.ViewType.THREAD && autonext) {
            ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getViewLifecycleOwner(), id, new ViewModelMessages.IPrevNext() {
                boolean once = false;

                @Override
                public void onPrevious(boolean exists, Long id) {
                    // Do nothing
                }

                @Override
                public void onNext(boolean exists, Long id) {
                    if (!exists || id != null) {
                        closeNext = id;
                        if (!once) {
                            once = true;
                            loadMessagesNext(top);
                        }
                    }
                }

                @Override
                public void onFound(int position, int size) {
                    // Do nothing
                }
            });
        } else if (viewType == AdapterMessage.ViewType.SEARCH && !reset) {
            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    DB.getInstance(context).message().resetSearch();
                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    reset = true;
                    loadMessagesNext(top);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(getContext(), getViewLifecycleOwner(), new Bundle(), "search:reset");
        } else
            loadMessagesNext(top);
    }

    private void loadMessagesNext(final boolean top) {
        if (top)
            adapter.gotoTop();

        ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);

        ViewModelMessages.Model vmodel = model.getModel(
                getContext(), getViewLifecycleOwner(),
                viewType, account, folder, thread, id, query, server);

        vmodel.setCallback(callback);
        vmodel.setObserver(getViewLifecycleOwner(), observer);
    }

    private BoundaryCallbackMessages.IBoundaryCallbackMessages callback = new BoundaryCallbackMessages.IBoundaryCallbackMessages() {
        @Override
        public void onLoading() {
            loading = true;
            pbWait.setVisibility(View.VISIBLE);
            tvNoEmail.setVisibility(View.GONE);
        }

        @Override
        public void onLoaded(int fetched) {
            loading = false;

            Integer submitted = (Integer) rvMessage.getTag();
            if (submitted == null)
                return;

            pbWait.setVisibility(View.GONE);
            if (submitted + fetched == 0)
                tvNoEmail.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(Throwable ex) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                            .setMessage(Helper.formatThrowable(ex))
                            .setPositiveButton(android.R.string.cancel, null)
                            .create()
                            .show();
        }
    };

    private Observer<PagedList<TupleMessageEx>> observer = new Observer<PagedList<TupleMessageEx>>() {
        @Override
        public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
            if (messages == null)
                return;

            if (viewType == AdapterMessage.ViewType.THREAD)
                if (handleThreadActions(messages))
                    return;

            Log.i("Submit messages=" + messages.size());
            adapter.submitList(messages);

            // This is to workaround not drawing when the search is expanded
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    rvMessage.requestLayout();
                }
            });

            rvMessage.setTag(messages.size());

            if (!loading) {
                pbWait.setVisibility(View.GONE);
                if (messages.size() == 0)
                    tvNoEmail.setVisibility(View.VISIBLE);
            }
            if (messages.size() > 0) {
                tvNoEmail.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        }
    };

    private boolean handleThreadActions(@NonNull PagedList<TupleMessageEx> messages) {
        // Auto close / next
        if (messages.size() == 0 && (autoclose || autonext)) {
            handleAutoClose();
            return true;
        }

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
                    @Override
                    public int compare(TupleMessageEx d1, TupleMessageEx d2) {
                        int o1 = DUPLICATE_ORDER.indexOf(d1.folderType);
                        int o2 = DUPLICATE_ORDER.indexOf(d2.folderType);
                        return ((Integer) o1).compareTo(o2);
                    }
                });
                for (int i = 1; i < dups.size(); i++)
                    dups.get(i).duplicate = true;
            }
        }

        if (autoExpanded) {
            autoExpanded = false;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            long download = prefs.getInt("download", MessageHelper.DEFAULT_ATTACHMENT_DOWNLOAD_SIZE);
            if (download == 0)
                download = Long.MAX_VALUE;

            boolean unmetered = ConnectionHelper.getNetworkState(getContext()).isUnmetered();

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

                if (!(EntityFolder.OUTBOX.equals(message.folderType) && message.ui_snoozed != null) &&
                        !EntityFolder.ARCHIVE.equals(message.folderType) &&
                        !EntityFolder.SENT.equals(message.folderType) &&
                        !EntityFolder.TRASH.equals(message.folderType) &&
                        !EntityFolder.JUNK.equals(message.folderType))
                    autoCloseCount++;
            }

            // Auto expand when:
            // - single, non archived/trashed/sent message
            // - one unread, non archived/trashed/sent message in conversation
            // - sole message
            if (autoexpand) {
                TupleMessageEx expand = null;
                if (count == 1)
                    expand = single;
                else if (unseen == 1)
                    expand = see;
                else if (messages.size() == 1)
                    expand = messages.get(0);

                if (expand != null &&
                        (expand.content || unmetered || (expand.size != null && expand.size < download)))
                    iProperties.setValue("expanded", expand.id, true);
            }
        } else {
            if (autoCloseCount > 0 && (autoclose || autonext)) {
                int count = 0;
                for (int i = 0; i < messages.size(); i++) {
                    TupleMessageEx message = messages.get(i);
                    if (message == null)
                        continue;
                    if (!(EntityFolder.OUTBOX.equals(message.folderType) && message.ui_snoozed != null) &&
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
                    return true;
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
                    boolean snoozable = false;
                    boolean archivable = false;
                    for (EntityMessage message : messages) {
                        EntityFolder folder = db.folder().getFolder(message.folder);
                        if (!EntityFolder.DRAFTS.equals(folder.type) &&
                                !EntityFolder.OUTBOX.equals(folder.type) &&
                                // allow sent
                                !EntityFolder.TRASH.equals(folder.type) &&
                                !EntityFolder.JUNK.equals(folder.type))
                            trashable = true;

                        if (!EntityFolder.OUTBOX.equals(folder.type))
                            snoozable = true;

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

                    return new Boolean[]{trashable, snoozable, archivable};
                }

                @Override
                protected void onExecuted(Bundle args, Boolean[] data) {
                    bottom_navigation.getMenu().findItem(R.id.action_delete).setVisible(data[0]);
                    bottom_navigation.getMenu().findItem(R.id.action_snooze).setVisible(data[1]);
                    bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(data[2]);
                    bottom_navigation.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(FragmentMessages.this, args, "messages:navigation");
        }
        return false;
    }

    private void updateExpanded() {
        boolean expanded = (values.containsKey("expanded") && values.get("expanded").size() > 0);
        ibDown.setVisibility(expanded ? View.VISIBLE : View.GONE);
        ibUp.setVisibility(expanded ? View.VISIBLE : View.GONE);
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
                    if (message == null)
                        return null;

                    if (message.uid != null) {
                        if (!message.content)
                            EntityOperation.queue(context, message, EntityOperation.BODY);
                        if (!message.ui_seen)
                            EntityOperation.queue(context, message, EntityOperation.SEEN, true);
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
            if (closeNext == null)
                finish();
            else {
                Log.i("Navigating to last next=" + closeNext);
                navigate(closeNext, false);
            }
        }
    }

    private void navigate(long id, final boolean left) {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            return;

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
                if (message == null) {
                    finish();
                    return;
                }

                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    getFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getArguments().putBoolean("fade", true);
                getArguments().putBoolean("left", left);

                Bundle nargs = new Bundle();
                nargs.putLong("account", message.account);
                nargs.putString("thread", message.thread);
                nargs.putLong("id", message.id);
                nargs.putBoolean("found", found);
                nargs.putBoolean("pane", pane);
                nargs.putLong("primary", primary);
                nargs.putBoolean("connected", connected);
                nargs.putBoolean("left", left);

                FragmentMessages fragment = new FragmentMessages();
                fragment.setArguments(nargs);

                int res = (pane ? R.id.content_pane : R.id.content_frame);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(res, fragment).addToBackStack("thread");
                fragmentTransaction.commit();
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
        if (selectionTracker != null)
            selectionTracker.clearSelection();

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
                            EntityOperation.queue(context, message, EntityOperation.MOVE, target.folder.id);
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

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            for (MessageTarget target : result) {
                                EntityMessage message = db.message().getMessage(target.id);
                                if (message != null && message.ui_hide) {
                                    Log.i("Move id=" + id + " target=" + target.folder.name);
                                    EntityOperation.queue(context, message, EntityOperation.MOVE, target.folder.id);
                                }
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                    }
                }, "messages:timeout");
                thread.setPriority(THREAD_PRIORITY_BACKGROUND);
                thread.start();
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

            int count = (values.containsKey("expanded") ? values.get("expanded").size() : 0);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean collapse = prefs.getBoolean("collapse", false);
            if ((count == 1 && collapse) || count > 1) {
                values.get("expanded").clear();
                updateExpanded();
                adapter.notifyDataSetChanged();
                return true;
            }

            return false;
        }
    };

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Bundle args = getArguments();
        Boolean left = (Boolean) args.get("left");
        if (viewType == AdapterMessage.ViewType.THREAD && args != null) {
            if (enter) {
                if (left != null)
                    return AnimationUtils.loadAnimation(getContext(), left ? R.anim.enter_from_left : R.anim.enter_from_right);
            } else {
                if (args.getBoolean("fade")) {
                    args.remove("fade");
                    return AnimationUtils.loadAnimation(getContext(), left ? R.anim.leave_to_right : R.anim.leave_to_left);
                }
            }
        }

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    static void search(
            final Context context, final LifecycleOwner owner, final FragmentManager manager,
            long folder, boolean server, String query) {
        if (!Helper.isPro(context)) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
            return;
        }

        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            manager.popBackStack("search", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putBoolean("server", server);
        args.putString("query", query);

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("search");
        fragmentTransaction.commit();
    }

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
        List<Long> folders;
        List<EntityAccount> accounts;
        Map<Long, List<TupleFolderEx>> targets = new HashMap<>();
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
