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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
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
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sun.mail.util.FolderClosedIOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import java.util.Objects;
import java.util.Properties;

import javax.mail.FolderClosedException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static android.app.Activity.RESULT_OK;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_NO_SIGNATURE;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_VALID_KEY_CONFIRMED;

public class FragmentMessages extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ViewGroup view;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvSupport;
    private ImageButton ibHintSupport;
    private ImageButton ibHintSwipe;
    private ImageButton ibHintSelect;
    private TextView tvNoEmail;
    private TextView tvNoEmailHint;
    private FixedRecyclerView rvMessage;
    private View vwAnchor;
    private SeekBar sbThread;
    private ImageButton ibDown;
    private ImageButton ibUp;
    private ImageButton ibSeen;
    private ImageButton ibUnflagged;
    private ImageButton ibSnoozed;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpSupport;
    private Group grpHintSupport;
    private Group grpHintSwipe;
    private Group grpHintSelect;
    private Group grpReady;
    private FloatingActionButton fabReply;
    private FloatingActionButton fabCompose;
    private FloatingActionButton fabMore;
    private FloatingActionButton fabSearch;
    private FloatingActionButton fabError;

    private String type;
    private long account;
    private long folder;
    private boolean server;
    private String thread;
    private long id;
    private boolean found;
    private String query;
    private boolean pane;

    private long message = -1;
    private OpenPgpServiceConnection pgpService;

    private boolean cards;
    private boolean date;
    private boolean threading;
    private boolean swipenav;
    private boolean autoscroll;
    private boolean seekbar;
    private boolean actionbar;
    private boolean autoexpand;
    private boolean autoclose;
    private String onclose;
    private boolean addresses;

    private int colorPrimary;
    private int colorAccent;

    private long primary;
    private boolean connected;
    private boolean reset = false;
    private String searching = null;
    private boolean initialized = false;
    private boolean loading = false;
    private boolean manual = false;
    private Integer lastUnseen = null;
    private boolean swiping = false;

    private AdapterMessage adapter;

    private AdapterMessage.ViewType viewType;
    private SelectionPredicateMessage selectionPredicate = null;
    private SelectionTracker<Long> selectionTracker = null;

    private Long prev = null;
    private Long next = null;
    private Long closeId = null;
    private int autoCloseCount = 0;
    private boolean autoExpanded = true;
    private Map<String, List<Long>> values = new HashMap<>();
    private LongSparseArray<Float> sizes = new LongSparseArray<>();
    private LongSparseArray<Spanned> bodies = new LongSparseArray<>();
    private LongSparseArray<List<EntityAttachment>> attachments = new LongSparseArray<>();
    private LongSparseArray<TupleAccountSwipes> accountSwipes = new LongSparseArray<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private static final int UNDO_TIMEOUT = 5000; // milliseconds
    private static final int SWIPE_DISABLE_SELECT_DURATION = 1500; // milliseconds

    private static final int REQUEST_RAW = 1;
    private static final int REQUEST_DECRYPT = 4;
    static final int REQUEST_MESSAGE_DELETE = 5;
    private static final int REQUEST_MESSAGES_DELETE = 6;
    static final int REQUEST_MESSAGE_JUNK = 7;
    private static final int REQUEST_MESSAGES_JUNK = 8;
    private static final int REQUEST_ASKED_MOVE = 9;
    private static final int REQUEST_ASKED_MOVE_ACROSS = 10;
    static final int REQUEST_MESSAGE_COLOR = 11;
    private static final int REQUEST_MESSAGES_COLOR = 12;
    private static final int REQUEST_MESSAGE_SNOOZE = 13;
    private static final int REQUEST_MESSAGES_SNOOZE = 14;
    static final int REQUEST_MESSAGE_MOVE = 15;
    private static final int REQUEST_MESSAGES_MOVE = 16;
    static final int REQUEST_PRINT = 17;
    private static final int REQUEST_SEARCH = 18;
    private static final int REQUEST_ACCOUNT = 19;
    static final int REQUEST_MESSAGE_PROPERTY = 20;
    private static final int REQUEST_EMPTY_TRASH = 21;

    static final String ACTION_STORE_RAW = BuildConfig.APPLICATION_ID + ".STORE_RAW";
    static final String ACTION_DECRYPT = BuildConfig.APPLICATION_ID + ".DECRYPT";

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
        type = args.getString("type");
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

        if (folder > 0 && type == null && TextUtils.isEmpty(query))
            Log.e("Messages for folder without type");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swipenav = prefs.getBoolean("swipenav", true);
        autoscroll = (prefs.getBoolean("autoscroll", false) || viewType == AdapterMessage.ViewType.THREAD);
        cards = prefs.getBoolean("cards", true);
        date = prefs.getBoolean("date", true);
        threading = prefs.getBoolean("threading", true);
        seekbar = prefs.getBoolean("seekbar", false);
        actionbar = prefs.getBoolean("actionbar", true);
        autoexpand = prefs.getBoolean("autoexpand", true);
        autoclose = prefs.getBoolean("autoclose", true);
        onclose = (autoclose ? null : prefs.getString("onclose", null));
        addresses = prefs.getBoolean("addresses", false);

        colorPrimary = Helper.resolveColor(getContext(), R.attr.colorPrimary);
        colorAccent = Helper.resolveColor(getContext(), R.attr.colorAccent);

        if (TextUtils.isEmpty(query))
            if (thread == null) {
                if (folder < 0)
                    viewType = AdapterMessage.ViewType.UNIFIED;
                else
                    viewType = AdapterMessage.ViewType.FOLDER;
                setTitle(getResources().getQuantityString(
                        threading ? R.plurals.page_conversation : R.plurals.page_message, 10));
            } else {
                viewType = AdapterMessage.ViewType.THREAD;
                setTitle(getResources().getQuantityString(
                        threading ? R.plurals.page_conversation : R.plurals.page_message, 1));
            }
        else {
            viewType = AdapterMessage.ViewType.SEARCH;
            setTitle(R.string.title_search);
        }
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
        tvNoEmailHint = view.findViewById(R.id.tvNoEmailHint);
        rvMessage = view.findViewById(R.id.rvMessage);
        vwAnchor = view.findViewById(R.id.vwAnchor);
        sbThread = view.findViewById(R.id.sbThread);
        ibDown = view.findViewById(R.id.ibDown);
        ibUp = view.findViewById(R.id.ibUp);
        ibSeen = view.findViewById(R.id.ibSeen);
        ibUnflagged = view.findViewById(R.id.ibUnflagged);
        ibSnoozed = view.findViewById(R.id.ibSnoozed);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);
        grpSupport = view.findViewById(R.id.grpSupport);
        grpHintSupport = view.findViewById(R.id.grpHintSupport);
        grpHintSwipe = view.findViewById(R.id.grpHintSwipe);
        grpHintSelect = view.findViewById(R.id.grpHintSelect);
        grpReady = view.findViewById(R.id.grpReady);

        fabReply = view.findViewById(R.id.fabReply);
        fabCompose = view.findViewById(R.id.fabCompose);
        fabMore = view.findViewById(R.id.fabMore);
        fabSearch = view.findViewById(R.id.fabSearch);
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
                getContext().startActivity(new Intent(getContext(), ActivityBilling.class));
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
        final LinearLayoutManager llm = new LinearLayoutManager(getContext()) {
            private Rect parentRect = new Rect();
            private Rect childRect = new Rect();

            @Override
            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child, @NonNull Rect rect, boolean immediate) {
                return requestChildRectangleOnScreen(parent, child, rect, immediate, false);
            }

            @Override
            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child, @NonNull Rect rect, boolean immediate, boolean focusedChildVisible) {
                parent.getHitRect(parentRect);
                child.getHitRect(childRect);
                if (Rect.intersects(parentRect, childRect))
                    return false;

                try {
                    return super.requestChildRectangleOnScreen(parent, child, rect, immediate, focusedChildVisible);
                } catch (Throwable ex) {
                    Log.e(ex);
                    /*
                        java.lang.NullPointerException: Attempt to read from field 'int android.view.ViewGroup$LayoutParams.width' on a null object reference
                        java.lang.NullPointerException: Attempt to read from field 'int android.view.ViewGroup$LayoutParams.width' on a null object reference
                        at android.widget.PopupWindow.alignToAnchor(PopupWindow.java:2353)
                        at android.widget.PopupWindow.access$000(PopupWindow.java:106)
                        at android.widget.PopupWindow$1.onViewAttachedToWindow(PopupWindow.java:220)
                        at android.view.View.dispatchAttachedToWindow(View.java:18358)
                        at android.view.ViewGroup.dispatchAttachedToWindow(ViewGroup.java:3397)
                        at android.view.ViewGroup.addViewInner(ViewGroup.java:5077)
                        at android.view.ViewGroup.addView(ViewGroup.java:4865)
                        at android.view.ViewGroup.addView(ViewGroup.java:4805)
                        at androidx.recyclerview.widget.RecyclerView$5.addView(SourceFile:870)
                        at androidx.recyclerview.widget.ChildHelper.addView(SourceFile:107)
                        at androidx.recyclerview.widget.RecyclerView$LayoutManager.addViewInt(SourceFile:8569)
                        at androidx.recyclerview.widget.RecyclerView$LayoutManager.addView(SourceFile:8527)
                        at androidx.recyclerview.widget.RecyclerView$LayoutManager.addView(SourceFile:8515)
                        at androidx.recyclerview.widget.LinearLayoutManager.layoutChunk(SourceFile:1641)
                        at androidx.recyclerview.widget.LinearLayoutManager.fill(SourceFile:1587)
                        at androidx.recyclerview.widget.LinearLayoutManager.onLayoutChildren(SourceFile:665)
                        at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep2(SourceFile:4115)
                        at androidx.recyclerview.widget.RecyclerView.dispatchLayout(SourceFile:3832)
                        at androidx.recyclerview.widget.RecyclerView.consumePendingUpdateOperations(SourceFile:1881)
                        at androidx.recyclerview.widget.RecyclerView.scrollByInternal(SourceFile:1950)
                        at androidx.recyclerview.widget.RecyclerView.scrollBy(SourceFile:1826)
                        at androidx.recyclerview.widget.RecyclerView$LayoutManager.requestChildRectangleOnScreen(SourceFile:9881)
                     */
                    return false;
                }
            }
        };
        rvMessage.setLayoutManager(llm);

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
            rvMessage.addItemDecoration(itemDecorator);
        }

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

                if (cards) {
                    View vSeparatorDate = header.findViewById(R.id.vSeparatorDate);
                    vSeparatorDate.setVisibility(View.GONE);
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.DAY_OF_MONTH, -1);
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
        boolean ascending = prefs.getBoolean(
                viewType == AdapterMessage.ViewType.THREAD ? "ascending_thread" : "ascending_list", false);
        boolean filter_duplicates = prefs.getBoolean("filter_duplicates", true);

        adapter = new AdapterMessage(this, type, viewType, compact, zoom, sort, ascending, filter_duplicates, iProperties);
        rvMessage.setAdapter(adapter);

        sbThread.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
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

        ibSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean filter = prefs.getBoolean("filter_seen", true);
                onMenuFilterSeen(!filter);
            }
        });

        ibUnflagged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean filter = prefs.getBoolean("filter_unflagged", true);
                onMenuFilterUnflagged(!filter);
            }
        });

        ibSnoozed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean filter = prefs.getBoolean("filter_snoozed", true);
                onMenuFilterSnoozed(!filter);
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
                        navigate(prev, true);
                        return true;

                    case R.id.action_next:
                        navigate(next, false);
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
                                List<EntityMessage> messages = db.message().getMessagesByThread(
                                        aid, thread, threading ? null : id, null);
                                for (EntityMessage threaded : messages) {
                                    EntityFolder folder = db.folder().getFolder(threaded.folder);
                                    if (!target.id.equals(threaded.folder) &&
                                            !EntityFolder.DRAFTS.equals(folder.type) && !EntityFolder.OUTBOX.equals(folder.type) &&
                                            (!EntityFolder.SENT.equals(folder.type) || EntityFolder.TRASH.equals(target.type)) &&
                                            !EntityFolder.TRASH.equals(folder.type) && !EntityFolder.JUNK.equals(folder.type))
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
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
                }.execute(FragmentMessages.this, args, "messages:move");
            }

            private void onActionSnooze() {
                Bundle args = new Bundle();
                args.putString("title", getString(R.string.title_snooze));
                args.putLong("account", account);
                args.putString("thread", thread);
                args.putLong("id", id);
                args.putBoolean("finish", true);

                FragmentDialogDuration fragment = new FragmentDialogDuration();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_SNOOZE);
                fragment.show(getFragmentManager(), "message:snooze");
            }
        });

        fabReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReply("reply");
            }
        });

        fabReply.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return onReply("reply_all");
            }
        });

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean identities_asked = prefs.getBoolean("identities_asked", false);
                if (identities_asked)
                    startActivity(new Intent(getContext(), ActivityCompose.class)
                            .putExtra("action", "new")
                            .putExtra("account", account)
                    );
                else {
                    FragmentDialogIdentity fragment = new FragmentDialogIdentity();
                    fragment.show(getFragmentManager(), "messages:identities");
                }
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
                            Snackbar.make(view, R.string.title_no_primary_drafts, Snackbar.LENGTH_LONG).show();
                        else {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("account", drafts.account)
                                            .putExtra("folder", drafts.id)
                                            .putExtra("type", drafts.type));
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
                }.execute(FragmentMessages.this, args, "messages:drafts");

                return true;
            }
        });

        fabMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMore();
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folder < 0) {
                    Bundle args = new Bundle();

                    new SimpleTask<List<EntityAccount>>() {
                        @Override
                        protected List<EntityAccount> onExecute(Context context, Bundle args) {
                            Map<EntityAccount, List<EntityFolder>> result = new LinkedHashMap<>();

                            DB db = DB.getInstance(context);
                            return db.account().getSynchronizingAccounts();
                        }

                        @Override
                        protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), fabSearch);

                            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.title_search_server).setEnabled(false);

                            int order = 1;
                            for (EntityAccount account : accounts)
                                if (!account.pop)
                                    popupMenu.getMenu().add(Menu.NONE, 0, order++, account.name)
                                            .setIntent(new Intent().putExtra("account", account.id));

                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem target) {
                                    Intent intent = target.getIntent();
                                    if (intent == null)
                                        return false;

                                    Bundle args = new Bundle();
                                    args.putString("title", getString(R.string.title_search_in));
                                    args.putLong("account", intent.getLongExtra("account", -1));
                                    args.putLongArray("disabled", new long[]{});
                                    args.putString("query", query);

                                    FragmentDialogFolder fragment = new FragmentDialogFolder();
                                    fragment.setArguments(args);
                                    fragment.setTargetFragment(FragmentMessages.this, FragmentMessages.REQUEST_SEARCH);
                                    fragment.show(getFragmentManager(), "messages:search");

                                    return true;
                                }
                            });

                            popupMenu.show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentMessages.this, args, "messages:search");
                } else
                    search(getContext(), getViewLifecycleOwner(), getFragmentManager(), folder, true, query);
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
        if (cards && !Helper.isDarkTheme(getContext()))
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightColorBackground_cards));

        tvNoEmail.setVisibility(View.GONE);
        tvNoEmailHint.setVisibility(View.GONE);
        sbThread.setVisibility(View.GONE);
        ibDown.setVisibility(View.GONE);
        ibUp.setVisibility(View.GONE);
        ibSeen.setVisibility(View.GONE);
        ibUnflagged.setVisibility(View.GONE);
        ibSnoozed.setVisibility(View.GONE);
        bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(false);
        bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(false);
        bottom_navigation.setVisibility(actionbar && viewType == AdapterMessage.ViewType.THREAD ? View.INVISIBLE : View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        fabReply.hide();

        if (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER)
            fabCompose.show();
        else
            fabCompose.hide();

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
                    boolean reversed = prefs.getBoolean("reversed", false);
                    if (reversed)
                        next = id;
                    else
                        prev = id;
                    bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(prev != null);
                    bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(next != null);
                }

                @Override
                public void onNext(boolean exists, Long id) {
                    boolean reversed = prefs.getBoolean("reversed", false);
                    if (reversed)
                        prev = id;
                    else
                        next = id;
                    bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(prev != null);
                    bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(next != null);
                }

                @Override
                public void onFound(int position, int size) {
                    if (seekbar) {
                        sbThread.setMax(size - 1);
                        sbThread.setProgress(size - 1 - position);
                        sbThread.getProgressDrawable().setAlpha(0);
                        sbThread.getThumb().setColorFilter(
                                position == 0 || position == size - 1 ? colorAccent : colorPrimary,
                                PorterDuff.Mode.SRC_IN);
                        sbThread.setVisibility(size > 1 ? View.VISIBLE : View.GONE);
                    }
                }
            });

            if (swipenav) {
                Log.i("Swipe navigation");

                final SwipeListener swipeListener = new SwipeListener(getContext(), new SwipeListener.ISwipeListener() {
                    @Override
                    public boolean onSwipeRight() {
                        if (prev == null) {
                            Animation bounce = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_right);
                            view.startAnimation(bounce);
                        } else
                            navigate(prev, true);

                        return (prev != null);
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
                }
            });
        }

        swipeRefresh.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (viewType != AdapterMessage.ViewType.UNIFIED && viewType != AdapterMessage.ViewType.FOLDER)
                    return true;
                if (!prefs.getBoolean("pull", true))
                    return true;
                if (swiping)
                    return true;
                if (selectionTracker != null && selectionTracker.hasSelection())
                    return true;
                return rvMessage.canScrollVertically(-1);
            }
        });

        pgpService = new OpenPgpServiceConnection(getContext(), "org.sufficientlysecure.keychain");
        pgpService.bindToService();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        IntentFilter iff = new IntentFilter();
        iff.addAction(SimpleTask.ACTION_TASK_COUNT);
        lbm.registerReceiver(creceiver, iff);

        return view;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(creceiver);

        if (pgpService != null && pgpService.isBound())
            pgpService.unbindFromService();

        super.onDestroyView();
    }

    private BroadcastReceiver creceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Received " + intent);
            Log.logExtras(intent);

            int count = intent.getIntExtra("count", 0);
            if (count == 0) {
                if (initialized && !loading)
                    pbWait.setVisibility(View.GONE);
            } else
                pbWait.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void scrollToVisibleItem(LinearLayoutManager llm, boolean bottom) {
        int first = llm.findFirstVisibleItemPosition();
        int last = llm.findLastVisibleItemPosition();
        if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION)
            return;

        int pos = (bottom ? last : first);
        do {
            Long key = adapter.getKeyAtPosition(pos);
            if (key != null && iProperties.getValue("expanded", key)) {
                View child = llm.findViewByPosition(pos);
                if (child != null) {
                    TranslateAnimation bounce = new TranslateAnimation(
                            0, 0, Helper.dp2pixels(getContext(), bottom ? -6 : 6), 0);
                    bounce.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
                    child.startAnimation(bounce);

                    if (bottom)
                        llm.scrollToPositionWithOffset(pos,
                                rvMessage.getHeight() - llm.getDecoratedMeasuredHeight(child) + child.getPaddingBottom());
                    else
                        llm.scrollToPositionWithOffset(pos, -child.getPaddingTop());

                    break;
                }
            }

            if (bottom)
                pos--;
            else
                pos++;
        } while (pos >= first && pos <= last);
    }

    private void onSwipeRefresh() {
        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putString("type", type);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                manual = true;
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");
                String type = args.getString("type");

                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<EntityFolder> folders = new ArrayList<>();
                    if (fid < 0)
                        folders.addAll(db.folder().getFoldersSynchronizingUnified(type));
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
        }.execute(this, args, "messages:refresh");
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
                // Collapse other messages
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean expand_one = prefs.getBoolean("expand_one", true);
                if (expand_one) {
                    for (Long other : new ArrayList<>(values.get(name)))
                        if (!other.equals(id)) {
                            values.get(name).remove(other);
                            int pos = adapter.getPositionForKey(other);
                            if (pos != RecyclerView.NO_POSITION)
                                adapter.notifyItemChanged(pos);
                        }
                }

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
        public void setSize(long id, float size) {
            sizes.put(id, size);
        }

        @Override
        public float getSize(long id, float defaultSize) {
            return sizes.get(id, defaultSize);
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
        public void setAttachments(long id, List<EntityAttachment> list) {
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
        public void move(long id, String type) {
            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putString("type", type);

            new SimpleTask<ArrayList<MessageTarget>>() {
                @Override
                protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    String type = args.getString("type");

                    ArrayList<MessageTarget> result = new ArrayList<>();

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return result;

                        EntityFolder target = db.folder().getFolderByType(message.account, type);

                        if (target == null)
                            return result;

                        EntityAccount account = db.account().getAccount(target.account);
                        result.add(new MessageTarget(message, account, target));

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
                    Helper.unexpectedError(getFragmentManager(), ex);
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

            if (message.folderReadOnly)
                return 0;

            if (EntityFolder.OUTBOX.equals(message.folderType))
                return 0;

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null)
                return 0;

            int flags = 0;
            if (swipes.swipe_left != null &&
                    (swipes.swipe_left < 0 ||
                            (swipes.left_type != null && !swipes.swipe_left.equals(message.folder))))
                flags |= ItemTouchHelper.LEFT;
            if (swipes.swipe_right != null &&
                    (swipes.swipe_right < 0 ||
                            (swipes.right_type != null && !swipes.swipe_right.equals(message.folder))))
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

            Long action = (dX > 0 ? swipes.swipe_right : swipes.swipe_left);
            if (action == null)
                return;

            AdapterMessage.ViewHolder holder = ((AdapterMessage.ViewHolder) viewHolder);
            Rect rect = holder.getItemRect();
            int margin = Helper.dp2pixels(getContext(), 12);
            int size = Helper.dp2pixels(getContext(), 24);

            int icon;
            if (FragmentAccount.SWIPE_ACTION_ASK.equals(action))
                icon = R.drawable.baseline_list_24;
            else if (FragmentAccount.SWIPE_ACTION_SEEN.equals(action))
                if (message.ui_seen)
                    icon = R.drawable.baseline_visibility_off_24;
                else
                    icon = R.drawable.baseline_visibility_24;
            else
                icon = EntityFolder.getIcon(dX > 0 ? swipes.right_type : swipes.left_type);
            Drawable d = getResources().getDrawable(icon, getContext().getTheme()).mutate();
            d.setTint(Helper.resolveColor(getContext(), android.R.attr.textColorSecondary));

            if (dX > 0) {
                // Right swipe
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
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final TupleMessageEx message = getMessage(viewHolder);
            if (message == null) {
                adapter.notifyDataSetChanged();
                return;
            }

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null) {
                adapter.notifyDataSetChanged();
                return;
            }

            Long action = (direction == ItemTouchHelper.LEFT ? swipes.swipe_left : swipes.swipe_right);
            if (action == null) {
                adapter.notifyDataSetChanged();
                return;
            }

            Log.i("Swiped dir=" + direction + " message=" + message.id);

            if (FragmentAccount.SWIPE_ACTION_SEEN.equals(action))
                onActionSeenSelection(!message.ui_seen, message.id);
            else if (FragmentAccount.SWIPE_ACTION_ASK.equals(action))
                swipeAsk(message, viewHolder);
            else
                swipeFolder(message, action);
        }

        private TupleMessageEx getMessage(@NonNull RecyclerView.ViewHolder viewHolder) {
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

            if (iProperties.getValue("expanded", message.id))
                return null;

            if (EntityFolder.OUTBOX.equals(message.folderType))
                return null;

            return message;
        }

        private void swipeAsk(final @NonNull TupleMessageEx message, @NonNull RecyclerView.ViewHolder viewHolder) {
            // Use fixed anchor
            ConstraintLayout.LayoutParams lparam = (ConstraintLayout.LayoutParams) vwAnchor.getLayoutParams();
            lparam.topMargin = viewHolder.itemView.getTop() + viewHolder.itemView.getHeight();
            vwAnchor.setLayoutParams(lparam);

            adapter.notifyDataSetChanged();

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), vwAnchor);

            if (message.ui_seen)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_unseen, 1, R.string.title_unseen);
            else
                popupMenu.getMenu().add(Menu.NONE, R.string.title_seen, 1, R.string.title_seen);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_snooze, 2, R.string.title_snooze);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_flag_color, 3, R.string.title_flag_color);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_move, 4, R.string.title_move);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    switch (target.getItemId()) {
                        case R.string.title_seen:
                            onActionSeenSelection(true, message.id);
                            return true;
                        case R.string.title_unseen:
                            onActionSeenSelection(false, message.id);
                            return true;
                        case R.string.title_snooze:
                            onMenuSnooze();
                            return true;
                        case R.string.title_flag_color:
                            onMenuColor();
                            return true;
                        case R.string.title_move:
                            onMenuMove();
                            return true;
                        default:
                            return false;
                    }
                }

                private void onMenuSnooze() {
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.title_snooze));
                    args.putLong("account", message.account);
                    args.putString("thread", message.thread);
                    args.putLong("id", message.id);
                    args.putBoolean("finish", false);

                    FragmentDialogDuration fragment = new FragmentDialogDuration();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_SNOOZE);
                    fragment.show(getFragmentManager(), "message:snooze");
                }

                private void onMenuColor() {
                    int color = (message.color == null ? Color.TRANSPARENT : message.color);

                    Bundle args = new Bundle();
                    args.putLong("id", message.id);

                    FragmentDialogColor fragment = new FragmentDialogColor();
                    fragment.initialize(R.string.title_flag_color, color, args, getContext());
                    fragment.setTargetFragment(FragmentMessages.this, FragmentMessages.REQUEST_MESSAGE_COLOR);
                    fragment.show(getFragmentManager(), "message:color");
                }

                private void onMenuMove() {
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.title_move_to_folder));
                    args.putLong("account", message.account);
                    args.putLongArray("disabled", new long[]{message.folder});
                    args.putLong("message", message.id);
                    args.putBoolean("copy", false);
                    args.putBoolean("similar", true);

                    FragmentDialogFolder fragment = new FragmentDialogFolder();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_MOVE);
                    fragment.show(getFragmentManager(), "message:move");
                }
            });

            popupMenu.show();
        }

        private void swipeFolder(@NonNull TupleMessageEx message, @NonNull Long target) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("thread", viewType != AdapterMessage.ViewType.THREAD);
            args.putLong("target", target);

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

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return result;

                        EntityFolder target = db.folder().getFolder(tid);
                        if (target == null)
                            return result;

                        EntityAccount account = db.account().getAccount(target.account);
                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading && thread ? null : id, message.folder);
                        for (EntityMessage threaded : messages) {
                            result.add(new MessageTarget(threaded, account, target));
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
                protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                    moveUndo(result);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                    else
                        Helper.unexpectedError(getFragmentManager(), ex);
                }
            }.execute(FragmentMessages.this, args, "messages:swipe");
        }
    };

    private boolean onReply(String action) {
        if (values.containsKey("expanded") && values.get("expanded").size() > 0) {
            long id = values.get("expanded").get(0);
            Intent reply = new Intent(getContext(), ActivityCompose.class)
                    .putExtra("action", action)
                    .putExtra("reference", id);
            startActivity(reply);
            return true;
        } else
            return false;
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

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            message.account, message.thread, threading ? null : id, null);
                    for (EntityMessage threaded : messages) {
                        if (threaded.ui_seen)
                            result.seen = true;
                        else
                            result.unseen = true;

                        if (threaded.ui_flagged)
                            result.flagged = true;
                        else
                            result.unflagged = true;
                    }

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

                result.accounts = new ArrayList<>();
                for (EntityAccount account : db.account().getSynchronizingAccounts())
                    if (!account.pop)
                        result.accounts.add(account);

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, final MoreResult result) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), fabMore);

                int order = 0;

                if (result.unseen) // Unseen, not draft
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_seen, order++, R.string.title_seen);
                if (result.seen) // Seen, not draft
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unseen, order++, R.string.title_unseen);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_snooze, order++, R.string.title_snooze);

                if (result.unflagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag, order++, R.string.title_flag);
                if (result.flagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unflag, order++, R.string.title_unflag);
                if (result.unflagged || result.flagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag_color, order++, R.string.title_flag_color);

                if (result.hasArchive && !result.isArchive) // has archive and not is archive/drafts
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_archive, order++, R.string.title_archive);

                if (result.isTrash) // is trash
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, order++, R.string.title_delete);

                if (!result.isTrash && result.hasTrash) // not trash and has trash
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_trash, order++, R.string.title_trash);

                if (result.hasJunk && !result.isJunk && !result.isDrafts) // has junk and not junk/drafts
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_spam, order++, R.string.title_spam);

                for (EntityAccount account : result.accounts) {
                    MenuItem item = popupMenu.getMenu()
                            .add(Menu.NONE, R.string.title_move_to_account, order++,
                                    getString(R.string.title_move_to_account, account.name));
                    item.setIntent(new Intent().putExtra("account", account.id));
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        switch (target.getItemId()) {
                            case R.string.title_seen:
                                onActionSeenSelection(true, null);
                                return true;
                            case R.string.title_unseen:
                                onActionSeenSelection(false, null);
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
                                onActionMoveSelectionAccount(account, result.folders);
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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "messages:more");
    }

    private long[] getSelection() {
        Selection<Long> selection = selectionTracker.getSelection();

        long[] ids = new long[selection.size()];
        int i = 0;
        for (Long id : selection)
            ids[i++] = id;

        return ids;
    }

    private void onActionSeenSelection(boolean seen, Long id) {
        Bundle args = new Bundle();
        args.putLongArray("ids", id == null ? getSelection() : new long[]{id});
        args.putBoolean("seen", seen);

        if (selectionTracker != null)
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
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, seen ? null : message.folder);
                        for (EntityMessage threaded : messages)
                            if (threaded.ui_seen != seen)
                                EntityOperation.queue(context, threaded, EntityOperation.SEEN, seen);
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
        }.execute(this, args, "messages:seen");
    }

    private void onActionSnoozeSelection() {
        Bundle args = new Bundle();
        args.putString("title", getString(R.string.title_snooze));

        FragmentDialogDuration fragment = new FragmentDialogDuration();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_MESSAGES_SNOOZE);
        fragment.show(getFragmentManager(), "messages:snooze");
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
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, null);
                        for (EntityMessage threaded : messages)
                            if (threaded.ui_flagged != flagged || !Objects.equals(threaded.color, color))
                                EntityOperation.queue(context, threaded, EntityOperation.FLAG, flagged, color);
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
        }.execute(this, args, "messages:flag");
    }

    private void onActionFlagColorSelection() {
        FragmentDialogColor fragment = new FragmentDialogColor();
        fragment.initialize(R.string.title_flag_color, Color.TRANSPARENT, new Bundle(), getContext());
        fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_COLOR);
        fragment.show(getFragmentManager(), "messages:color");
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
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages)
                            if (message.uid != null)
                                ids.add(threaded.id);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return ids;
            }

            @Override
            protected void onExecuted(Bundle args, final List<Long> ids) {
                Bundle aargs = new Bundle();
                aargs.putString("question", getResources()
                        .getQuantityString(R.plurals.title_deleting_messages, ids.size(), ids.size()));
                aargs.putLongArray("ids", Helper.toLongArray(ids));

                FragmentDialogAsk ask = new FragmentDialogAsk();
                ask.setArguments(aargs);
                ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_DELETE);
                ask.show(getFragmentManager(), "messages:delete");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "messages:delete:ask");
    }

    private void onActionJunkSelection() {
        int count = selectionTracker.getSelection().size();

        Bundle aargs = new Bundle();
        aargs.putString("question", getResources()
                .getQuantityString(R.plurals.title_ask_spam, count, count));

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_JUNK);
        ask.show(getFragmentManager(), "messages:junk");
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
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages) {
                            EntityFolder target = db.folder().getFolderByType(message.account, type);
                            if (target != null) {
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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "messages:move");
    }

    private void onActionMoveSelectionAccount(long account, List<Long> disabled) {
        Bundle args = new Bundle();
        args.putString("title", getString(R.string.title_move_to_folder));
        args.putLong("account", account);
        args.putLongArray("disabled", Helper.toLongArray(disabled));

        FragmentDialogFolder fragment = new FragmentDialogFolder();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentMessages.this, FragmentMessages.REQUEST_MESSAGES_MOVE);
        fragment.show(getFragmentManager(), "messages:move");
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
                            if (message == null)
                                continue;

                            List<EntityMessage> messages = db.message().getMessagesByThread(
                                    message.account, message.thread, threading ? null : id, message.folder);
                            for (EntityMessage threaded : messages)
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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "messages:move");
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

        long[] skeys = new long[sizes.size()];
        float[] svalues = new float[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            skeys[i] = sizes.keyAt(i);
            svalues[i] = sizes.valueAt(i);
        }
        outState.putLongArray("fair:sizes:keys", skeys);
        outState.putFloatArray("fair:sizes:values", svalues);

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

            long[] skeys = savedInstanceState.getLongArray("fair:sizes:keys");
            float[] svalues = savedInstanceState.getFloatArray("fair:sizes:values");

            for (int i = 0; i < skeys.length; i++)
                sizes.put(skeys[i], svalues[i]);

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

        db.account().liveAccountSwipes(null).observe(getViewLifecycleOwner(), new Observer<List<TupleAccountSwipes>>() {
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
                db.folder().liveUnified(type).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                    @Override
                    public void onChanged(List<TupleFolderEx> folders) {
                        if (folders == null)
                            folders = new ArrayList<>();

                        updateState(folders);
                    }
                });
                db.message().liveHiddenFolder(null, type).observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
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
                    }
                });
                db.message().liveHiddenFolder(folder, null).observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
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
                        setSubtitle(stats == null || stats.accountName == null ? "" : stats.accountName);

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
                db.message().liveHiddenThread(account, thread).observe(getViewLifecycleOwner(), new Observer<List<Long>>() {
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
                setSubtitle(query);
                break;
        }

        loadMessages(false);

        updateExpanded();

        if (selectionTracker != null && selectionTracker.hasSelection())
            fabMore.show();
        else
            fabMore.hide();

        checkReporting();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!pgpService.isBound())
            pgpService.bindToService();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_STORE_RAW);
        iff.addAction(ACTION_DECRYPT);
        lbm.registerReceiver(receiver, iff);

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

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("pro".equals(key)) {
            boolean pro = prefs.getBoolean(key, false);
            grpSupport.setVisibility(
                    !pro && viewType == AdapterMessage.ViewType.UNIFIED
                            ? View.VISIBLE : View.GONE);
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
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                            adapter.checkInternet();
                    }
                });
        }
    };

    private void checkReporting() {
        if (viewType == AdapterMessage.ViewType.UNIFIED) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (prefs.getBoolean("crash_reports", false) ||
                    prefs.getBoolean("crash_reports_asked", false))
                return;

            final Snackbar snackbar = Snackbar.make(view, R.string.title_ask_help, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.title_info, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    new FragmentDialogReporting().show(getFragmentManager(), "reporting");
                }
            });

            snackbar.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_messages, menu);

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
                        folder, false, query);
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
                FragmentDialogAccount fragment = new FragmentDialogAccount();
                fragment.setArguments(new Bundle());
                fragment.setTargetFragment(FragmentMessages.this, REQUEST_ACCOUNT);
                fragment.show(getFragmentManager(), "messages:accounts");
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = prefs.getString("sort", "time");
        boolean ascending = prefs.getBoolean(
                viewType == AdapterMessage.ViewType.THREAD ? "ascending_thread" : "ascending_list", false);
        boolean filter_seen = prefs.getBoolean("filter_seen", false);
        boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
        boolean filter_snoozed = prefs.getBoolean("filter_snoozed", true);
        boolean filter_duplicates = prefs.getBoolean("filter_duplicates", true);
        boolean compact = prefs.getBoolean("compact", false);
        boolean experiments = prefs.getBoolean("experiments", false);

        boolean outbox = EntityFolder.OUTBOX.equals(type);
        boolean folder =
                (viewType == AdapterMessage.ViewType.UNIFIED ||
                        (viewType == AdapterMessage.ViewType.FOLDER && !outbox));

        boolean canSnooze =
                (viewType == AdapterMessage.ViewType.UNIFIED && !EntityFolder.DRAFTS.equals(type)) ||
                        (viewType == AdapterMessage.ViewType.FOLDER && !EntityFolder.DRAFTS.equals(type));

        menu.findItem(R.id.menu_search).setVisible(
                viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER);

        menu.findItem(R.id.menu_folders).setVisible(viewType == AdapterMessage.ViewType.UNIFIED && primary >= 0);
        ImageButton ib = (ImageButton) menu.findItem(R.id.menu_folders).getActionView();
        ib.setImageResource(connected
                ? R.drawable.baseline_folder_special_24 : R.drawable.baseline_folder_open_24);

        menu.findItem(R.id.menu_sort_on).setVisible(viewType != AdapterMessage.ViewType.SEARCH);

        if (viewType == AdapterMessage.ViewType.THREAD) {
            menu.findItem(R.id.menu_sort_on_time).setVisible(false);
            menu.findItem(R.id.menu_sort_on_unread).setVisible(false);
            menu.findItem(R.id.menu_sort_on_starred).setVisible(false);
            menu.findItem(R.id.menu_sort_on_sender).setVisible(false);
            menu.findItem(R.id.menu_sort_on_subject).setVisible(false);
            menu.findItem(R.id.menu_sort_on_size).setVisible(false);
            menu.findItem(R.id.menu_sort_on_snoozed).setVisible(false);
        }

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
        menu.findItem(R.id.menu_ascending).setChecked(ascending);

        menu.findItem(R.id.menu_filter).setVisible(viewType != AdapterMessage.ViewType.SEARCH && !outbox);
        menu.findItem(R.id.menu_filter_seen).setVisible(viewType != AdapterMessage.ViewType.THREAD);
        menu.findItem(R.id.menu_filter_unflagged).setVisible(viewType != AdapterMessage.ViewType.THREAD);
        menu.findItem(R.id.menu_filter_snoozed).setVisible(viewType != AdapterMessage.ViewType.THREAD && canSnooze);
        menu.findItem(R.id.menu_filter_duplicates).setVisible(viewType == AdapterMessage.ViewType.THREAD);
        menu.findItem(R.id.menu_filter_seen).setChecked(filter_seen);
        menu.findItem(R.id.menu_filter_unflagged).setChecked(filter_unflagged);
        menu.findItem(R.id.menu_filter_snoozed).setChecked(filter_snoozed);
        menu.findItem(R.id.menu_filter_duplicates).setChecked(filter_duplicates);

        menu.findItem(R.id.menu_compact).setChecked(compact);

        menu.findItem(R.id.menu_select_all).setVisible(!outbox &&
                (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER));
        menu.findItem(R.id.menu_empty_trash).setVisible(
                viewType == AdapterMessage.ViewType.FOLDER && EntityFolder.TRASH.equals(type));

        menu.findItem(R.id.menu_force_sync).setVisible(viewType == AdapterMessage.ViewType.UNIFIED);

        ibSeen.setImageResource(filter_seen ? R.drawable.baseline_drafts_24 : R.drawable.baseline_mail_24);
        ibUnflagged.setImageResource(filter_unflagged ? R.drawable.baseline_star_border_24 : R.drawable.baseline_star_24);
        ibSnoozed.setImageResource(filter_snoozed ? R.drawable.baseline_timelapse_24 : R.drawable.baseline_timer_off_24);

        ibSeen.setVisibility(experiments && folder ? View.VISIBLE : View.GONE);
        ibUnflagged.setVisibility(experiments && folder ? View.VISIBLE : View.GONE);
        ibSnoozed.setVisibility(experiments && folder && canSnooze ? View.VISIBLE : View.GONE);

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

            case R.id.menu_ascending:
                onMenuAscending(!item.isChecked());
                return true;

            case R.id.menu_filter_seen:
                onMenuFilterSeen(!item.isChecked());
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

            case R.id.menu_empty_trash:
                onMenuEmptyTrash();
                return true;

            case R.id.menu_force_sync:
                onMenuForceSync();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuFolders(long account) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
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

    private void onMenuAscending(boolean ascending) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean(
                viewType == AdapterMessage.ViewType.THREAD ? "ascending_thread" : "ascending_list", ascending).apply();
        adapter.setAscending(ascending);
        getActivity().invalidateOptionsMenu();
        loadMessages(true);
    }

    private void onMenuFilterSeen(boolean filter) {
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
        ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
        model.getIds(getContext(), getViewLifecycleOwner(), new Observer<List<Long>>() {
            @Override
            public void onChanged(List<Long> ids) {
                selectionTracker.clearSelection();
                for (long id : ids)
                    selectionTracker.select(id);

                ToastEx.makeText(getContext(),
                        getContext().getResources().getQuantityString(
                                R.plurals.title_selected_messages, ids.size(), ids.size()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onMenuEmptyTrash() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_empty_trash_ask));

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(this, FragmentMessages.REQUEST_EMPTY_TRASH);
        ask.show(getFragmentManager(), "messages:empty_trash");
    }

    private void onMenuForceSync() {
        ServiceSynchronize.reset(getContext());
        ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
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
            if (type == null)
                name = getString(R.string.title_folder_unified);
            else
                name = Helper.localizeFolderType(getContext(), type);
        else {
            name = (folders.size() > 0 ? folders.get(0).getDisplayName(getContext()) : "");
            if (folders.size() == 1) {
                String accountName = folders.get(0).accountName;
                if (accountName != null)
                    name += "/" + accountName;
            }
        }

        // Show name/unread
        if (unseen == 0)
            setSubtitle(name);
        else
            setSubtitle(getString(R.string.title_name_count, name, NF.format(unseen)));

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
        if (viewType == AdapterMessage.ViewType.THREAD && onclose != null) {
            ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getViewLifecycleOwner(), id, new ViewModelMessages.IPrevNext() {
                boolean once = false;

                @Override
                public void onPrevious(boolean exists, Long id) {
                    onData(false, exists, id);
                }

                @Override
                public void onNext(boolean exists, Long id) {
                    onData(true, exists, id);
                }

                @Override
                public void onFound(int position, int size) {
                    // Do nothing
                }

                private void onData(boolean next, boolean exists, Long id) {
                    if ((next ? "next" : "previous").equals(onclose))
                        if (!exists || id != null) {
                            closeId = id;
                            if (!once) {
                                once = true;
                                loadMessagesNext(top);
                            }
                        }
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
                    Helper.unexpectedError(getFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "search:reset");
        } else
            loadMessagesNext(top);
    }

    private void loadMessagesNext(final boolean top) {
        if (top)
            adapter.gotoTop();

        ViewModelMessages model = ViewModelProviders.of(getActivity()).get(ViewModelMessages.class);

        ViewModelMessages.Model vmodel = model.getModel(
                getContext(), getViewLifecycleOwner(),
                viewType, type, account, folder, thread, id, query, server);

        vmodel.setCallback(getViewLifecycleOwner(), callback);
        vmodel.setObserver(getViewLifecycleOwner(), observer);
    }

    private BoundaryCallbackMessages.IBoundaryCallbackMessages callback = new BoundaryCallbackMessages.IBoundaryCallbackMessages() {
        @Override
        public void onLoading() {
            loading = true;
            pbWait.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoaded(int fetched) {
            loading = false;
            if (initialized && SimpleTask.getCount() == 0)
                pbWait.setVisibility(View.GONE);
        }

        @Override
        public void onException(@NonNull Throwable ex) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
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
                } else if (ex instanceof IllegalArgumentException ||
                        ex instanceof FolderClosedException || ex instanceof FolderClosedIOException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else {
                    Bundle args = new Bundle();
                    args.putString("error", Helper.formatThrowable(ex, false));

                    FragmentDialogError fragment = new FragmentDialogError();
                    fragment.setArguments(args);
                    fragment.show(getFragmentManager(), "boundary:error");
                }
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

            updateExpanded();

            // This is to workaround not drawing when the search is expanded
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    rvMessage.requestLayout();
                }
            });

            initialized = true;
            if (!loading && SimpleTask.getCount() == 0)
                pbWait.setVisibility(View.GONE);

            boolean none = (messages.size() == 0 && !loading);
            tvNoEmail.setVisibility(none ? View.VISIBLE : View.GONE);
            tvNoEmailHint.setVisibility(
                    none && filterActive() && viewType != AdapterMessage.ViewType.SEARCH
                            ? View.VISIBLE : View.GONE);

            grpReady.setVisibility(View.VISIBLE);
        }
    };

    private boolean handleThreadActions(@NonNull PagedList<TupleMessageEx> messages) {
        // Auto close / next
        if (messages.size() == 0 && (autoclose || onclose != null)) {
            handleAutoClose();
            return true;
        }

        // Mark duplicates
        Map<String, List<TupleMessageEx>> duplicates = new HashMap<>();
        for (TupleMessageEx message : messages)
            if (message != null && !TextUtils.isEmpty(message.msgid)) {
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
            if (autoCloseCount > 0 && (autoclose || onclose != null)) {
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

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            account, thread, threading ? null : id, null);

                    boolean trashable = false;
                    boolean snoozable = false;
                    boolean archivable = false;
                    for (EntityMessage message : messages) {
                        EntityFolder folder = db.folder().getFolder(message.folder);

                        if (!folder.read_only &&
                                !EntityFolder.DRAFTS.equals(folder.type) &&
                                !EntityFolder.OUTBOX.equals(folder.type) &&
                                // allow sent
                                !EntityFolder.TRASH.equals(folder.type) &&
                                !EntityFolder.JUNK.equals(folder.type))
                            trashable = true;

                        if (!EntityFolder.OUTBOX.equals(folder.type))
                            snoozable = true;

                        if (!folder.read_only &&
                                !EntityFolder.isOutgoing(folder.type) &&
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
                    Helper.unexpectedError(getFragmentManager(), ex);
                }
            }.setLog(false).execute(this, args, "messages:navigation");
        }

        return false;
    }

    private void updateExpanded() {
        int expanded = (values.containsKey("expanded") ? values.get("expanded").size() : 0);

        if (expanded == 1) {
            long id = values.get("expanded").get(0);
            int pos = adapter.getPositionForKey(id);
            TupleMessageEx message = adapter.getItemAtPosition(pos);
            if (message != null && message.content && !EntityFolder.OUTBOX.equals(message.folderType))
                fabReply.show();
            else
                fabReply.hide();
        } else
            fabReply.hide();

        ibDown.setVisibility(expanded > 0 ? View.VISIBLE : View.GONE);
        ibUp.setVisibility(expanded > 0 ? View.VISIBLE : View.GONE);
    }

    private void handleExpand(long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean inline_images = prefs.getBoolean("inline_images", false);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    EntityFolder folder = db.folder().getFolder(message.folder);
                    if (folder == null || folder.account == null)
                        return null;

                    EntityAccount account = db.account().getAccount(folder.account);
                    if (account == null)
                        return null;

                    if (account.pop) {
                        if (!message.ui_seen)
                            EntityOperation.queue(context, message, EntityOperation.SEEN, true);
                    } else {
                        if (!message.content)
                            EntityOperation.queue(context, message, EntityOperation.BODY);

                        if (inline_images) {
                            List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                            for (EntityAttachment attachment : attachments)
                                if (!attachment.available && attachment.isInline() && attachment.isImage())
                                    EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.id);
                        }

                        if (account.auto_seen && !message.ui_seen && !folder.read_only)
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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.setLog(false).execute(this, args, "messages:expand");
    }

    private void handleAutoClose() {
        if (autoclose)
            finish();
        else if (onclose != null) {
            if (closeId == null)
                finish();
            else {
                Log.i("Navigating to id=" + closeId);
                navigate(closeId, "previous".equals(onclose));
            }
        }
    }

    private void navigate(long id, final boolean left) {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
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

                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
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
                if (getActivity() != null && getActivity().findViewById(res) != null) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(res, fragment).addToBackStack("thread");
                    fragmentTransaction.commit();
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "messages:navigate");
    }

    private void moveAsk(final ArrayList<MessageTarget> result) {
        if (result.size() == 0)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("automove", false)) {
            moveAskConfirmed(result);
            return;
        }

        Bundle aargs = new Bundle();
        aargs.putString("question", getResources()
                .getQuantityString(R.plurals.title_moving_messages,
                        result.size(), result.size(), getDisplay(result)));
        aargs.putString("notagain", "automove");
        aargs.putParcelableArrayList("result", result);

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(FragmentMessages.this, REQUEST_ASKED_MOVE);
        ask.show(getFragmentManager(), "messages:move");
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
                        if (message == null)
                            continue;

                        Log.i("Move id=" + target.id + " target=" + target.folder.name);
                        EntityOperation.queue(context, message, EntityOperation.MOVE, target.folder.id);
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
                    Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "messages:move");
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
                snackbar.getView().setTag(true);

                Bundle args = new Bundle();
                args.putParcelableArrayList("result", result);

                // Show message again
                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        DB db = DB.getInstance(context);

                        try {
                            db.beginTransaction();

                            ArrayList<MessageTarget> result = args.getParcelableArrayList("result");
                            for (MessageTarget target : result) {
                                Log.i("Move undo id=" + target.id);
                                db.message().setMessageUiHide(target.id, false);
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

                if (snackbar.getView().getTag() != null)
                    return;

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
                                if (message == null || !message.ui_hide)
                                    continue;

                                Log.i("Move id=" + id + " target=" + target.folder.name);
                                EntityOperation.queue(context, message, EntityOperation.MOVE, target.folder.id);
                            }

                            db.setTransactionSuccessful();
                        } catch (Throwable ex) {
                            Log.e(ex);
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

    private boolean filterActive() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean filter_seen = prefs.getBoolean("filter_seen", false);
        boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
        return (filter_seen || filter_unflagged);
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                String action = intent.getAction();

                if (ACTION_STORE_RAW.equals(action))
                    onStoreRaw(intent);
                else if (ACTION_DECRYPT.equals(action))
                    onDecrypt(intent);
            }
        }
    };

    private void onStoreRaw(Intent intent) {
        message = intent.getLongExtra("id", -1);
        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.setType("*/*");
        create.putExtra(Intent.EXTRA_TITLE, "email.eml");
        if (create.resolveActivity(getContext().getPackageManager()) == null)
            Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(getContext(), create), REQUEST_RAW);
    }

    private void onDecrypt(Intent intent) {
        if (pgpService.isBound()) {
            Intent data = new Intent();
            data.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);

            onDecrypt(data, intent.getLongExtra("id", -1));
        } else
            Snackbar.make(view, R.string.title_no_openpgp, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_RAW:
                    if (resultCode == RESULT_OK && data != null)
                        onSaveRaw(data);
                    break;
                case REQUEST_DECRYPT:
                    if (resultCode == RESULT_OK && data != null)
                        onDecrypt(data, message);
                    break;
                case REQUEST_MESSAGE_DELETE:
                    if (resultCode == RESULT_OK && data != null)
                        onDelete(data.getBundleExtra("args").getLong("id"));
                    break;
                case REQUEST_MESSAGES_DELETE:
                    if (resultCode == RESULT_OK && data != null)
                        onDelete(data.getBundleExtra("args").getLongArray("ids"));
                    break;
                case REQUEST_MESSAGE_JUNK:
                    if (resultCode == RESULT_OK && data != null)
                        onJunk(data.getBundleExtra("args").getLong("id"));
                    break;
                case REQUEST_MESSAGES_JUNK:
                    if (resultCode == RESULT_OK)
                        onActionMoveSelection(EntityFolder.JUNK);
                    break;
                case REQUEST_ASKED_MOVE:
                    if (resultCode == RESULT_OK && data != null)
                        onMoveAskAcross(data.getBundleExtra("args").<MessageTarget>getParcelableArrayList("result"));
                    break;
                case REQUEST_ASKED_MOVE_ACROSS:
                    if (resultCode == RESULT_OK && data != null)
                        moveAskConfirmed(data.getBundleExtra("args").<MessageTarget>getParcelableArrayList("result"));
                    break;
                case REQUEST_MESSAGE_COLOR:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onColor(args.getLong("id"), args.getInt("color"));
                    }
                    break;
                case REQUEST_MESSAGES_COLOR:
                    if (resultCode == RESULT_OK && data != null) {
                        if (!ActivityBilling.isPro(getContext())) {
                            getContext().startActivity(new Intent(getContext(), ActivityBilling.class));
                            return;
                        }

                        Bundle args = data.getBundleExtra("args");
                        onActionFlagSelection(true, args.getInt("color"));
                    }
                    break;
                case REQUEST_MESSAGE_SNOOZE:
                    if (resultCode == RESULT_OK && data != null)
                        onSnooze(data.getBundleExtra("args"));
                    break;
                case REQUEST_MESSAGES_SNOOZE:
                    if (resultCode == RESULT_OK && data != null)
                        onSnoozeSelection(data.getBundleExtra("args"));
                    break;
                case REQUEST_MESSAGE_MOVE:
                    if (resultCode == RESULT_OK && data != null)
                        onMove(data.getBundleExtra("args"));
                    break;
                case REQUEST_MESSAGES_MOVE:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onActionMoveSelection(args.getLong("folder"));
                    }
                    break;
                case REQUEST_PRINT:
                    if (resultCode == RESULT_OK && data != null)
                        onPrint(data.getBundleExtra("args"));
                    break;
                case REQUEST_SEARCH:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        search(
                                getContext(), getViewLifecycleOwner(), getFragmentManager(),
                                args.getLong("folder"), true, args.getString("query"));
                    }
                    break;
                case REQUEST_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onMenuFolders(args.getLong("account"));
                    }
                    break;
                case REQUEST_MESSAGE_PROPERTY:
                    if (resultCode == RESULT_OK)
                        onPropertySet(data.getBundleExtra("args"));
                    break;
                case REQUEST_EMPTY_TRASH:
                    if (resultCode == RESULT_OK)
                        onEmptyTrash();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSaveRaw(Intent data) {
        Bundle args = new Bundle();
        args.putLong("id", message);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if ("file".equals(uri.getScheme())) {
                    Log.w("Save raw uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    throw new FileNotFoundException();
                File file = message.getRawFile(context);
                Log.i("Raw file=" + file);

                ParcelFileDescriptor pfd = null;
                OutputStream os = null;
                InputStream is = null;
                try {
                    pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                    os = new FileOutputStream(pfd.getFileDescriptor());
                    is = new FileInputStream(file);

                    byte[] buffer = new byte[Helper.BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                } finally {
                    try {
                        if (pfd != null)
                            pfd.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (os != null)
                            os.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (is != null)
                            is.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                Snackbar.make(view, R.string.title_raw_saved, Snackbar.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "raw:save");
    }

    private void onDecrypt(Intent data, long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putParcelable("data", data);

        new SimpleTask<PendingIntent>() {
            @Override
            protected PendingIntent onExecute(Context context, Bundle args) throws Throwable {
                // Get arguments
                long id = args.getLong("id");
                Intent data = args.getParcelable("data");

                DB db = DB.getInstance(context);

                boolean inline = false;
                InputStream encrypted = null;

                // Find encrypted data
                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : attachments)
                    if (EntityAttachment.PGP_MESSAGE.equals(attachment.encryption)) {
                        if (!attachment.available)
                            throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));

                        File file = attachment.getFile(context);
                        encrypted = new FileInputStream(file);
                        break;
                    }

                if (encrypted == null) {
                    EntityMessage message = db.message().getMessage(id);
                    if (message != null && message.content) {
                        File file = message.getFile(context);
                        if (file.exists()) {
                            // https://tools.ietf.org/html/rfc4880#section-6.2
                            String body = Helper.readText(file);
                            int begin = body.indexOf(Helper.PGP_BEGIN_MESSAGE);
                            int end = body.indexOf(Helper.PGP_END_MESSAGE);
                            if (begin >= 0 && begin < end) {
                                String section = body.substring(begin, end + Helper.PGP_END_MESSAGE.length());
                                String[] lines = section.split("<br>");
                                List<String> disarmored = new ArrayList<>();
                                for (String line : lines)
                                    if (!TextUtils.isEmpty(line) && !line.contains(": "))
                                        disarmored.add(line);
                                section = TextUtils.join("\n\r", disarmored);

                                inline = true;
                                encrypted = new ByteArrayInputStream(section.getBytes());
                            }
                        }
                    }
                }

                if (encrypted == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_not_encrypted));

                ByteArrayOutputStream decrypted = new ByteArrayOutputStream();

                // Decrypt message
                OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
                Intent result = api.executeApi(data, encrypted, decrypted);

                Log.i("PGP result=" + result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR));
                switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
                    case OpenPgpApi.RESULT_CODE_SUCCESS:
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        if (inline) {
                            try {
                                db.beginTransaction();

                                // Write decrypted body
                                Helper.writeText(message.getFile(context),
                                        decrypted.toString().replace("\0", ""));

                                db.message().setMessageStored(id, new Date().getTime());

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                        } else {
                            // Decode message
                            Properties props = MessageHelper.getSessionProperties();
                            Session isession = Session.getInstance(props, null);
                            ByteArrayInputStream is = new ByteArrayInputStream(decrypted.toByteArray());
                            MimeMessage imessage = new MimeMessage(isession, is);
                            MessageHelper helper = new MessageHelper(imessage);
                            MessageHelper.MessageParts parts = helper.getMessageParts();

                            try {
                                db.beginTransaction();

                                // Write decrypted body
                                String html = parts.getHtml(context);
                                if (html != null)
                                    html = html.replace("\0", "");
                                Helper.writeText(message.getFile(context), html);

                                // Remove previously decrypted attachments
                                for (EntityAttachment local : attachments)
                                    if (local.encryption == null)
                                        db.attachment().deleteAttachment(local.id);

                                int sequence = db.attachment().getAttachmentSequence(id);

                                // Add decrypted attachments
                                List<EntityAttachment> remotes = parts.getAttachments();
                                for (int index = 0; index < remotes.size(); index++) {
                                    EntityAttachment remote = remotes.get(index);
                                    remote.message = id;
                                    remote.sequence = ++sequence;
                                    remote.id = db.attachment().insertAttachment(remote);
                                    try {
                                        parts.downloadAttachment(context, index, remote);
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }

                                db.message().setMessageStored(id, new Date().getTime());

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }
                        }

                        // Check signature status
                        OpenPgpSignatureResult sigResult = result.getParcelableExtra(OpenPgpApi.RESULT_SIGNATURE);
                        int sresult = (sigResult == null ? RESULT_NO_SIGNATURE : sigResult.getResult());
                        if (sresult == RESULT_NO_SIGNATURE)
                            Snackbar.make(view, R.string.title_signature_none, Snackbar.LENGTH_LONG).show();
                        else if (sresult == RESULT_VALID_KEY_CONFIRMED)
                            Snackbar.make(view, R.string.title_signature_valid, Snackbar.LENGTH_LONG).show();
                        else
                            db.message().setMessageError(id, getString(R.string.title_signature_invalid));

                        break;

                    case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                        FragmentMessages.this.message = id;
                        return result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

                    case OpenPgpApi.RESULT_CODE_ERROR:
                        OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                        throw new IllegalArgumentException(error.getMessage());
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, PendingIntent pi) {
                if (pi != null)
                    try {
                        Log.i("PGP executing pi=" + pi);
                        startIntentSenderForResult(
                                pi.getIntentSender(),
                                REQUEST_DECRYPT,
                                null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "decrypt");
    }

    private void onDelete(long id) {
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

                    EntityFolder folder = db.folder().getFolder(message.folder);
                    if (folder == null)
                        return null;

                    if (EntityFolder.OUTBOX.equals(folder.type)) {
                        db.message().deleteMessage(id);

                        db.folder().setFolderError(message.folder, null);
                        if (message.identity != null) {
                            db.identity().setIdentityError(message.identity, null);

                            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.cancel("send:" + message.identity, 1);
                        }
                    } else if (message.uid == null) {
                        db.message().deleteMessage(id);
                        db.folder().setFolderError(message.folder, null);
                    } else
                        EntityOperation.queue(context, message, EntityOperation.DELETE);

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
        }.execute(this, args, "message:delete");
    }

    private void onDelete(long[] ids) {
        Bundle args = new Bundle();
        args.putLongArray("ids", ids);

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
                        if (message == null)
                            continue;

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
        }.execute(this, args, "messages:delete:execute");
    }

    private void onJunk(long id) {
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

                    EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                    EntityOperation.queue(context, message, EntityOperation.MOVE, junk.id);

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
        }.execute(this, args, "message:junk");
    }

    private void onMoveAskAcross(final ArrayList<MessageTarget> result) {
        boolean across = false;
        for (MessageTarget target : result)
            if (target.across) {
                across = true;
                break;
            }

        if (across) {
            Bundle aargs = new Bundle();
            aargs.putString("question", getString(R.string.title_accross_remark));
            aargs.putParcelableArrayList("result", result);

            FragmentDialogAsk ask = new FragmentDialogAsk();
            ask.setArguments(aargs);
            ask.setTargetFragment(FragmentMessages.this, REQUEST_ASKED_MOVE_ACROSS);
            ask.show(getFragmentManager(), "messages:move:across");
        } else
            moveAskConfirmed(result);
    }

    private void onColor(long id, int color) {
        if (!ActivityBilling.isPro(getContext())) {
            getContext().startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putInt("color", color);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(final Context context, Bundle args) {
                final long id = args.getLong("id");
                final int color = args.getInt("color");

                final DB db = DB.getInstance(context);
                db.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return;

                        EntityOperation.queue(context, message, EntityOperation.FLAG, true, color);
                    }
                });
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "message:color");
    }

    private void onSnooze(Bundle args) {
        long duration = args.getLong("duration");
        long time = args.getLong("time");
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

                    List<EntityMessage> messages = db.message().getMessagesByThread(
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
                if (wakeup != null && args.getBoolean("finish"))
                    finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "message:snooze");
    }

    private void onSnoozeSelection(Bundle args) {
        if (!ActivityBilling.isPro(getContext())) {
            getContext().startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        long duration = args.getLong("duration");
        long time = args.getLong("time");
        args.putLong("wakeup", duration == 0 ? -1 : time);
        args.putLongArray("ids", getSelection());

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
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages) {
                            db.message().setMessageSnoozed(threaded.id, wakeup);
                            EntityMessage.snooze(context, threaded.id, wakeup);
                            EntityOperation.queue(context, threaded, EntityOperation.SEEN, true);
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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "messages:snooze");
    }

    private void onMove(Bundle args) {
        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                long id = args.getLong("message");
                long tid = args.getLong("folder");
                boolean copy = args.getBoolean("copy");
                boolean similar = args.getBoolean("similar");

                ArrayList<MessageTarget> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return result;

                    EntityFolder target = db.folder().getFolder(tid);
                    if (target == null)
                        return result;

                    EntityAccount account = db.account().getAccount(target.account);
                    if (account != null) {
                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading && similar ? null : id, message.folder);
                        for (EntityMessage threaded : messages)
                            if (copy)
                                EntityOperation.queue(context, message, EntityOperation.COPY, tid);
                            else
                                result.add(new MessageTarget(threaded, account, target));
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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "message:move");
    }

    private WebView printWebView = null;

    private void onPrint(Bundle args) {
        Bundle pargs = new Bundle();
        pargs.putLong("id", args.getLong("id"));

        new SimpleTask<String[]>() {
            @Override
            protected String[] onExecute(Context context, Bundle args) throws IOException {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null || !message.content)
                    return null;

                File file = message.getFile(context);
                if (!file.exists())
                    return null;

                String html = Helper.readText(file);
                Document document = Jsoup.parse(html);
                HtmlHelper.embedImages(context, id, document);

                Element body = document.body();
                if (body != null) {
                    Element p = document.createElement("p");

                    if (message.from != null && message.from.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(getString(R.string.title_from));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.from));
                        span.appendElement("br");
                        p.appendChild(span);
                    }

                    if (message.to != null && message.to.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(getString(R.string.title_to));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.to));
                        span.appendElement("br");
                        p.appendChild(span);
                    }

                    if (message.cc != null && message.cc.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(getString(R.string.title_cc));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.cc));
                        span.appendElement("br");
                        p.appendChild(span);
                    }

                    {
                        DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.LONG, SimpleDateFormat.LONG);

                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(getString(R.string.title_received));
                        span.appendChild(strong);
                        span.appendText(" " + DTF.format(message.received));
                        span.appendElement("br");
                        p.appendChild(span);
                    }

                    if (!TextUtils.isEmpty(message.subject)) {
                        Element span = document.createElement("span");
                        span.appendText(message.subject);
                        span.appendElement("br");
                        p.appendChild(span);
                    }

                    p.appendElement("hr").appendElement("br");

                    body.prependChild(p);
                }

                return new String[]{message.subject, document.html()};
            }

            @Override
            protected void onExecuted(Bundle args, final String[] data) {
                if (data == null)
                    return;

                // https://developer.android.com/training/printing/html-docs.html
                printWebView = new WebView(getContext());
                WebSettings settings = printWebView.getSettings();
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                settings.setAllowFileAccess(false);

                printWebView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        try {
                            ActivityBase activity = (ActivityBase) getActivity();
                            PrintManager printManager = (PrintManager) activity.getOriginalContext().getSystemService(Context.PRINT_SERVICE);
                            String jobName = getString(R.string.app_name);
                            if (!TextUtils.isEmpty(data[0]))
                                jobName += " - " + data[0];
                            PrintDocumentAdapter adapter = printWebView.createPrintDocumentAdapter(jobName);
                            printManager.print(jobName, adapter, new PrintAttributes.Builder().build());
                        } catch (Throwable ex) {
                            Log.e(ex);
                        } finally {
                            printWebView = null;
                        }
                    }
                });

                printWebView.loadDataWithBaseURL("about:blank", data[1], "text/html", "UTF-8", null);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, pargs, "message:print");
    }

    private void onPropertySet(Bundle args) {
        long id = args.getLong("id");
        String name = args.getString("name");
        boolean value = args.getBoolean("value");
        Log.i("Set property " + name + "=" + value + " id=" + id);
        iProperties.setValue(name, id, value);
    }

    private void onEmptyTrash() {
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

    static void search(
            final Context context, final LifecycleOwner owner, final FragmentManager manager,
            long folder, boolean server, String query) {
        if (server && !ActivityBilling.isPro(context)) {
            context.startActivity(new Intent(context, ActivityBilling.class));
            return;
        }

        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
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

    public static class FragmentDialogReporting extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_error_reporting, null);
            Button btnInfo = dview.findViewById(R.id.btnInfo);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(getContext(), 104);
                }
            });

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putBoolean("crash_reports_asked", isChecked).apply();
                }
            });

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putBoolean("crash_reports", true).apply();
                            Log.setCrashReporting(true);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .create();
        }
    }

    public static class FragmentDialogAccount extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final ArrayAdapter<EntityAccount> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1);

            // TODO: spinner
            new SimpleTask<List<EntityAccount>>() {
                @Override
                protected List<EntityAccount> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);
                    return db.account().getSynchronizingAccounts();
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                    adapter.addAll(accounts);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "messages:accounts");

            return new AlertDialog.Builder(getContext())
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EntityAccount account = adapter.getItem(which);
                            getArguments().putLong("account", account.id);
                            sendResult(RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogIdentity extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_identity, null);
            ListView lvIdentity = dview.findViewById(R.id.lvIdentity);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            Button btnFix = dview.findViewById(R.id.btnFix);
            Group grpIdentities = dview.findViewById(R.id.grpIdentities);
            Group grpNoIdentities = dview.findViewById(R.id.grpNoIdentities);
            ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

            lvIdentity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TupleIdentityEx identity = (TupleIdentityEx) lvIdentity.getAdapter().getItem(position);

                    startActivity(new Intent(getContext(), ActivityCompose.class)
                            .putExtra("action", "new")
                            .putExtra("account", identity.account)
                            .putExtra("identity", identity.id)
                    );

                    dismiss();
                }
            });

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putBoolean("identities_asked", isChecked).apply();
                }
            });

            btnFix.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), ActivitySetup.class));
                    getActivity().finish();

                    dismiss();
                }
            });

            grpIdentities.setVisibility(View.GONE);
            grpNoIdentities.setVisibility(View.GONE);

            new SimpleTask<List<TupleIdentityEx>>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    pbWait.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(Bundle args) {
                    pbWait.setVisibility(View.GONE);
                }

                @Override
                protected List<TupleIdentityEx> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(getContext());
                    return db.identity().getComposableIdentities(null);
                }

                @Override
                protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                    AdapterIdentitySelect iadapter = new AdapterIdentitySelect(getContext(), identities);
                    lvIdentity.setAdapter(iadapter);

                    grpIdentities.setVisibility(identities.size() > 0 ? View.VISIBLE : View.GONE);
                    grpNoIdentities.setVisibility(identities.size() > 0 ? View.GONE : View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "identity:select");

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogError extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            String error = getArguments().getString("error");

            return new AlertDialog.Builder(getContext())
                    .setMessage(error)
                    .setPositiveButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
