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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_KEY_MISSING;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_NO_SIGNATURE;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_VALID_KEY_CONFIRMED;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_VALID_KEY_UNCONFIRMED;
import static me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_DECELERATE_FACTOR;
import static me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK;
import static me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.LongSparseArray;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScrollCaptureCallback;
import android.view.ScrollCaptureSession;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.KeyTransRecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
import org.bouncycastle.util.Store;
import org.json.JSONException;
import org.openintents.openpgp.AutocryptPeerUpdate;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.text.Collator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import javax.mail.Address;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Status;
import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollState;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;

public class FragmentMessages extends FragmentBase
        implements SharedPreferences.OnSharedPreferenceChangeListener, FragmentManager.OnBackStackChangedListener {
    private ViewGroup view;
    private SwipeRefreshLayoutEx swipeRefresh;
    private TextView tvAirplane;
    private TextView tvNotifications;
    private TextView tvBatteryOptimizations;
    private TextView tvDataSaver;
    private TextView tvVpnActive;
    private TextView tvSupport;
    private ImageButton ibHintSupport;
    private ImageButton ibHintSwipe;
    private ImageButton ibHintOutbox;
    private ImageButton ibHintSelect;
    private ImageButton ibHintJunk;
    private TextView tvMod;
    private ImageButton ibMotd;
    private TextView tvNoEmail;
    private TextView tvNoEmailHint;
    private FixedRecyclerView rvMessage;
    private View vwAnchor;
    private SeekBar sbThread;
    private ImageButton ibDown;
    private ImageButton ibUp;
    private ImageButton ibOutbox;
    private TextView tvOutboxCount;
    private ImageButton ibSeen;
    private ImageButton ibUnflagged;
    private ImageButton ibSnoozed;
    private TextView tvDebug;
    private TextViewAutoCompleteAction etSearch;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpAirplane;
    private Group grpNotifications;
    private Group grpBatteryOptimizations;
    private Group grpDataSaver;
    private Group grpVpnActive;
    private Group grpSupport;
    private Group grpHintSupport;
    private Group grpHintSwipe;
    private Group grpHintOutbox;
    private Group grpHintSelect;
    private Group grpHintJunk;
    private Group grpMotd;
    private Group grpReady;
    private Group grpOutbox;
    private FloatingActionButton fabReply;
    private FloatingActionButton fabCompose;
    private FloatingActionButton fabMore;
    private TextView tvSelectedCount;
    private CardView cardMore;
    private ImageButton ibAnswer;
    private ImageButton ibSummarize;
    private ImageButton ibBatchSeen;
    private ImageButton ibBatchUnseen;
    private ImageButton ibBatchSnooze;
    private ImageButton ibBatchHide;
    private ImageButton ibBatchFlag;
    private ImageButton ibBatchFlagColor;
    private ImageButton ibLowImportance;
    private ImageButton ibNormalImportance;
    private ImageButton ibHighImportance;
    private ImageButton ibMove;
    private ImageButton ibArchive;
    private ImageButton ibTrash;
    private ImageButton ibDelete;
    private ImageButton ibJunk;
    private ImageButton ibInbox;
    private ImageButton ibKeywords;
    private ImageButton ibMoreSettings;
    private FloatingActionButton fabSearch;
    private FloatingActionButton fabError;
    private ObjectAnimator animator;

    private String type;
    private String category;
    private long account;
    private long folder;
    private boolean server;
    private String thread;
    private long id;
    private int lpos;
    private boolean filter_archive;
    private boolean found;
    private String searched;
    private boolean searchedPartial;
    private boolean pinned;
    private String msgid;
    private BoundaryCallbackMessages.SearchCriteria criteria = null;
    private boolean pane;

    private int searchIndex = 0;
    private TextView searchView = null;

    private boolean cards;
    private boolean dividers;
    private boolean group_category;
    private boolean date;
    private boolean date_week;
    private boolean date_fixed;
    private boolean date_bold;
    private boolean threading;
    private boolean swipenav;
    private boolean seekbar;
    private boolean move_thread_all;
    private boolean move_thread_sent;
    private boolean swipe_trash_all;
    private boolean actionbar;
    private int actionbar_delete_id;
    private int actionbar_archive_id;
    private boolean actionbar_color;
    private int seen_delay = 0;
    private boolean autoexpand;
    private boolean autoclose;
    private String onclose;
    private boolean quick_scroll;
    private boolean auto_hide_answer;
    private boolean swipe_reply;
    private boolean quick_actions;

    private int colorPrimary;
    private int colorAccent;
    private int colorControlNormal;
    private int colorSeparator;
    private int colorWarning;

    private boolean accessibility;

    private long primary = -1;
    private boolean connected;
    private boolean reset = false;
    private boolean initialized = false;
    private boolean loading = false;
    private boolean swiping = false;
    private boolean scrolling = false;
    private boolean navigating = false;

    private AdapterMessage adapter;

    private AdapterMessage.ViewType viewType;
    private SelectionPredicateMessage selectionPredicate = null;
    private SelectionTracker<Long> selectionTracker = null;

    private Long prev = null;
    private Long next = null;
    private Long closeId = null;
    private int autoCloseCount = 0;
    private int lastSentCount = 0;
    private boolean autoExpanded = true;
    private Long lastSync = null;

    private Integer lastUnseen;
    private Boolean lastRefreshing;
    private Boolean lastFolderErrors;
    private Boolean lastAccountErrors;

    final private Map<String, String> kv = new HashMap<>();
    final private Map<String, List<Long>> values = new HashMap<>();
    final private LongSparseArray<Float> sizes = new LongSparseArray<>();
    final private LongSparseArray<Integer> heights = new LongSparseArray<>();
    final private LongSparseArray<Pair<Integer, Integer>> positions = new LongSparseArray<>();
    final private LongSparseArray<List<EntityAttachment>> attachments = new LongSparseArray<>();
    final private LongSparseArray<TupleAccountSwipes> accountSwipes = new LongSparseArray<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "more");

    private static final int MAX_MORE = 100; // messages
    private static final int MAX_SEND_RAW = 50; // messages
    private static final int ITEM_CACHE_SIZE = 10; // Default: 2 items
    private static final long MAX_FORWARD_ADDRESS_AGE = 7 * 24 * 3600 * 1000L; // milliseconds

    private static final int REQUEST_RAW = 1;
    private static final int REQUEST_OPENPGP = 4;
    static final int REQUEST_MESSAGE_DELETE = 5;
    private static final int REQUEST_MESSAGES_DELETE = 6;
    static final int REQUEST_MESSAGE_JUNK = 7;
    private static final int REQUEST_MESSAGES_JUNK = 8;
    private static final int REQUEST_ASKED_MOVE = 9;
    private static final int REQUEST_ASKED_MOVE_ACROSS = 10;
    static final int REQUEST_MESSAGE_COLOR = 11;
    private static final int REQUEST_MESSAGES_COLOR = 12;
    static final int REQUEST_MESSAGE_SNOOZE = 13;
    private static final int REQUEST_MESSAGES_SNOOZE = 14;
    static final int REQUEST_MESSAGE_MOVE = 15;
    private static final int REQUEST_MESSAGES_MOVE = 16;
    private static final int REQUEST_THREAD_MOVE = 17;
    static final int REQUEST_PRINT = 18;
    private static final int REQUEST_SEARCH = 19;
    private static final int REQUEST_ACCOUNT = 20;
    private static final int REQUEST_EMPTY_FOLDER = 21;
    private static final int REQUEST_BOUNDARY_RETRY = 22;
    static final int REQUEST_PICK_CONTACT = 23;
    static final int REQUEST_BUTTONS = 24;
    private static final int REQUEST_ALL_READ = 25;
    private static final int REQUEST_SAVE_SEARCH = 26;
    private static final int REQUEST_QUICK_ACTIONS = 27;
    static final int REQUEST_BLOCK_SENDERS = 28;
    static final int REQUEST_CALENDAR = 29;
    static final int REQUEST_EDIT_SUBJECT = 30;
    private static final int REQUEST_ANSWER_SETTINGS = 31;
    private static final int REQUEST_DESELECT = 32;

    static final String ACTION_STORE_RAW = BuildConfig.APPLICATION_ID + ".STORE_RAW";
    static final String ACTION_VERIFYDECRYPT = BuildConfig.APPLICATION_ID + ".VERIFYDECRYPT";
    static final String ACTION_KEYWORDS = BuildConfig.APPLICATION_ID + ".KEYWORDS";

    private static final long REVIEW_ASK_DELAY = 14 * 24 * 3600 * 1000L; // milliseconds
    private static final long REVIEW_LATER_DELAY = 3 * 24 * 3600 * 1000L; // milliseconds

    static final List<String> SORT_DATE_HEADER = Collections.unmodifiableList(Arrays.asList(
            "time", "unread", "starred", "priority"
    ));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        type = args.getString("type");
        category = args.getString("category");
        account = args.getLong("account", -1);
        folder = args.getLong("folder", -1);
        server = args.getBoolean("server", false);
        thread = args.getString("thread");
        id = args.getLong("id", -1);
        lpos = args.getInt("lpos", RecyclerView.NO_POSITION);
        filter_archive = args.getBoolean("filter_archive", true);
        found = args.getBoolean("found", false);
        searched = args.getString("searched");
        searchedPartial = args.getBoolean("searchedPartial");
        pinned = args.getBoolean("pinned", false);
        msgid = args.getString("msgid");
        criteria = (BoundaryCallbackMessages.SearchCriteria) args.getSerializable("criteria");
        if (criteria != null) {
            searched = criteria.query;
            searchedPartial = criteria.isPartial();
        }
        pane = args.getBoolean("pane", false);
        primary = args.getLong("primary", -1);
        connected = args.getBoolean("connected", false);

        if (folder > 0 && thread == null && type == null && criteria == null)
            Log.e("Messages for folder without type");

        accessibility = Helper.isAccessibilityEnabled(getContext());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        cards = prefs.getBoolean("cards", true);
        dividers = prefs.getBoolean("dividers", true);
        group_category = (category == null && prefs.getBoolean("group_category", false));
        date = prefs.getBoolean("date", true);
        date_week = prefs.getBoolean("date_week", false);
        date_fixed = (!date && prefs.getBoolean("date_fixed", false));
        date_bold = prefs.getBoolean("date_bold", false);
        threading = (prefs.getBoolean("threading", true) ||
                args.getBoolean("force_threading"));
        swipenav = prefs.getBoolean("swipenav", true);
        seekbar = prefs.getBoolean("seekbar", false);
        move_thread_all = prefs.getBoolean("move_thread_all", false);
        move_thread_sent = (move_thread_all || prefs.getBoolean("move_thread_sent", false));
        swipe_trash_all = prefs.getBoolean("swipe_trash_all", true);
        actionbar = prefs.getBoolean("actionbar", true);
        boolean actionbar_swap = prefs.getBoolean("actionbar_swap", false);
        actionbar_delete_id = (actionbar_swap ? R.id.action_archive : R.id.action_delete);
        actionbar_archive_id = (actionbar_swap ? R.id.action_delete : R.id.action_archive);
        actionbar_color = prefs.getBoolean("actionbar_color", false);
        seen_delay = prefs.getInt("seen_delay", 0);
        autoexpand = prefs.getBoolean("autoexpand", true);
        autoclose = prefs.getBoolean("autoclose", true);
        onclose = (autoclose ? null : prefs.getString("onclose", null));
        quick_scroll = prefs.getBoolean("quick_scroll", true);
        auto_hide_answer = prefs.getBoolean("auto_hide_answer", false);
        swipe_reply = prefs.getBoolean("swipe_reply", false);
        quick_actions = prefs.getBoolean("quick_actions", true);

        colorPrimary = Helper.resolveColor(getContext(), androidx.appcompat.R.attr.colorPrimary);
        colorAccent = Helper.resolveColor(getContext(), androidx.appcompat.R.attr.colorAccent);
        colorControlNormal = Helper.resolveColor(getContext(), android.R.attr.colorControlNormal);
        colorSeparator = Helper.resolveColor(getContext(), R.attr.colorSeparator);
        colorWarning = Helper.resolveColor(getContext(), R.attr.colorWarning);

        if (criteria == null)
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
            setTitle(server ? R.string.title_search_server : R.string.title_search_device);
        }

        if (viewType != AdapterMessage.ViewType.THREAD &&
                (EntityFolder.ARCHIVE.equals(type) || viewType == AdapterMessage.ViewType.SEARCH))
            filter_archive = false;

        try {
            FragmentManager fm = getParentFragmentManager();
            if (viewType != AdapterMessage.ViewType.THREAD)
                fm.setFragmentResultListener("message.selected", this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        long id = result.getLong("id", -1);
                        iProperties.setValue("selected", id, true);
                    }
                });
            fm.addOnBackStackChangedListener(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        setActionBarListener(getViewLifecycleOwner(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentDialogSelectUnifiedFolder fragment = new FragmentDialogSelectUnifiedFolder();
                    fragment.show(getParentFragmentManager(), "unified:select");
                } catch (Throwable ex) {
                    /*
                        Exception java.lang.IllegalStateException:
                          at androidx.fragment.app.Fragment.getParentFragmentManager (Fragment.java:1112)
                          at eu.faircode.email.FragmentMessages$2.onClick (FragmentMessages.java:569)
                          at android.view.View.performClick (View.java:8047)
                          at android.view.View.performClickInternal (View.java:8024)
                          at android.view.View.-$$Nest$mperformClickInternal
                          at android.view.View$PerformClick.run (View.java:31890)
                          at android.os.Handler.handleCallback (Handler.java:958)
                     */
                    Log.e(ex);
                }
            }
        });

        view = (ViewGroup) inflater.inflate(R.layout.fragment_messages, container, false);

        // Get controls
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvAirplane = view.findViewById(R.id.tvAirplane);
        tvNotifications = view.findViewById(R.id.tvNotifications);
        tvBatteryOptimizations = view.findViewById(R.id.tvBatteryOptimizations);
        tvDataSaver = view.findViewById(R.id.tvDataSaver);
        tvVpnActive = view.findViewById(R.id.tvVpnActive);
        tvSupport = view.findViewById(R.id.tvSupport);
        ibHintSupport = view.findViewById(R.id.ibHintSupport);
        ibHintSwipe = view.findViewById(R.id.ibHintSwipe);
        ibHintOutbox = view.findViewById(R.id.ibHintOutbox);
        ibHintSelect = view.findViewById(R.id.ibHintSelect);
        ibHintJunk = view.findViewById(R.id.ibHintJunk);
        tvMod = view.findViewById(R.id.tvMotd);
        ibMotd = view.findViewById(R.id.ibMotd);
        tvNoEmail = view.findViewById(R.id.tvNoEmail);
        tvNoEmailHint = view.findViewById(R.id.tvNoEmailHint);
        rvMessage = view.findViewById(R.id.rvMessage);
        vwAnchor = view.findViewById(R.id.vwAnchor);
        sbThread = view.findViewById(R.id.sbThread);
        ibDown = view.findViewById(R.id.ibDown);
        ibUp = view.findViewById(R.id.ibUp);
        ibOutbox = view.findViewById(R.id.ibOutbox);
        tvOutboxCount = view.findViewById(R.id.tvOutboxCount);
        tvDebug = view.findViewById(R.id.tvDebug);
        ibSeen = view.findViewById(R.id.ibSeen);
        ibUnflagged = view.findViewById(R.id.ibUnflagged);
        ibSnoozed = view.findViewById(R.id.ibSnoozed);
        etSearch = view.findViewById(R.id.etSearch);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);
        grpAirplane = view.findViewById(R.id.grpAirplane);
        grpNotifications = view.findViewById(R.id.grpNotifications);
        grpBatteryOptimizations = view.findViewById(R.id.grpBatteryOptimizations);
        grpDataSaver = view.findViewById(R.id.grpDataSaver);
        grpVpnActive = view.findViewById(R.id.grpVpnActive);
        grpSupport = view.findViewById(R.id.grpSupport);
        grpHintSupport = view.findViewById(R.id.grpHintSupport);
        grpHintSwipe = view.findViewById(R.id.grpHintSwipe);
        grpHintOutbox = view.findViewById(R.id.grpHintOutbox);
        grpHintSelect = view.findViewById(R.id.grpHintSelect);
        grpHintJunk = view.findViewById(R.id.grpHintJunk);
        grpMotd = view.findViewById(R.id.grpMotd);
        grpReady = view.findViewById(R.id.grpReady);
        grpOutbox = view.findViewById(R.id.grpOutbox);

        fabReply = view.findViewById(R.id.fabReply);
        fabCompose = view.findViewById(R.id.fabCompose);
        fabMore = view.findViewById(R.id.fabMore);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        cardMore = view.findViewById(R.id.cardMore);
        ibAnswer = view.findViewById(R.id.ibAnswer);
        ibSummarize = view.findViewById(R.id.ibSummarize);
        ibBatchSeen = view.findViewById(R.id.ibBatchSeen);
        ibBatchUnseen = view.findViewById(R.id.ibBatchUnseen);
        ibBatchSnooze = view.findViewById(R.id.ibBatchSnooze);
        ibBatchHide = view.findViewById(R.id.ibBatchHide);
        ibBatchFlag = view.findViewById(R.id.ibBatchFlag);
        ibBatchFlagColor = view.findViewById(R.id.ibBatchFlagColor);
        ibLowImportance = view.findViewById(R.id.ibLowImportance);
        ibNormalImportance = view.findViewById(R.id.ibNormalImportance);
        ibHighImportance = view.findViewById(R.id.ibHighImportance);
        ibMove = view.findViewById(R.id.ibMove);
        ibArchive = view.findViewById(R.id.ibArchive);
        ibTrash = view.findViewById(R.id.ibTrash);
        ibDelete = view.findViewById(R.id.ibDelete);
        ibJunk = view.findViewById(R.id.ibJunk);
        ibInbox = view.findViewById(R.id.ibInbox);
        ibKeywords = view.findViewById(R.id.ibKeywords);
        ibMoreSettings = view.findViewById(R.id.ibMoreSettings);
        fabSearch = view.findViewById(R.id.fabSearch);
        fabError = view.findViewById(R.id.fabError);

        animator = Helper.getFabAnimator(fabSearch, getViewLifecycleOwner());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Wire controls

        int c = Helper.resolveColor(getContext(), R.attr.colorInfoForeground);
        swipeRefresh.setColorSchemeColors(c, c, c);
        swipeRefresh.setProgressBackgroundColorSchemeColor(colorPrimary);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Helper.performHapticFeedback(swipeRefresh, HapticFeedbackConstants.CONFIRM);
                onSwipeRefresh();
            }
        });

        tvAirplane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });

        tvNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentDialogNotifications().show(getParentFragmentManager(), "notifications");
            }
        });

        tvBatteryOptimizations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                v.getContext().startActivity(intent);
            }
        });

        tvDataSaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentDialogDataSaver().show(getParentFragmentManager(), "datasaver");
            }
        });

        tvVpnActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentDialogVPN().show(getParentFragmentManager(), "vpn");
            }
        });

        grpSupport.setVisibility(View.GONE);
        tvSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivityBilling.class));
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

        ibHintOutbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("message_outbox", true).apply();
                grpHintOutbox.setVisibility(View.GONE);
            }
        });

        ibHintSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("message_select", true).apply();
                grpHintSelect.setVisibility(View.GONE);
            }
        });

        ibHintJunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("message_junk", true).apply();
                grpHintJunk.setVisibility(View.GONE);
            }
        });

        if (Helper.isPixelBeta())
            tvMod.setText(getString(R.string.app_motd));

        ibMotd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("motd", false).apply();
                grpMotd.setVisibility(View.GONE);
            }
        });

        rvMessage.setHasFixedSize(false);

        rvMessage.setItemViewCacheSize(ITEM_CACHE_SIZE);
        //rvMessage.getRecycledViewPool().setMaxRecycledViews(0, 10); // Default 5

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

            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException ex) {
                    /*
                        java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder{c6d0306 position=571 id=-1, oldPos=534, pLpos:534 scrap [attachedScrap] tmpDetached not recyclable(1) no parent} eu.faircode.email.FixedRecyclerView{8cb71ae VFED..... .F....ID 0,0-959,1068 #7f0902e9 app:id/rvMessage}, adapter:eu.faircode.email.AdapterMessage@5d69b4f, layout:eu.faircode.email.FragmentMessages$6@dcc62dc, context:eu.faircode.email.ActivityView@31147e2
                          at androidx.recyclerview.widget.RecyclerView$Recycler.validateViewHolderForOffsetPosition(SourceFile:5974)
                          at androidx.recyclerview.widget.RecyclerView$Recycler.tryGetViewHolderForPositionByDeadline(SourceFile:6158)
                          at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(SourceFile:6118)
                          at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(SourceFile:6114)
                          at androidx.recyclerview.widget.LinearLayoutManager$LayoutState.next(SourceFile:2303)
                          at androidx.recyclerview.widget.LinearLayoutManager.layoutChunk(SourceFile:1627)
                          at androidx.recyclerview.widget.LinearLayoutManager.fill(SourceFile:1587)
                          at androidx.recyclerview.widget.LinearLayoutManager.onLayoutChildren(SourceFile:665)
                          at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep1(SourceFile:4085)
                          at androidx.recyclerview.widget.RecyclerView.dispatchLayout(SourceFile:3849)
                          at androidx.recyclerview.widget.RecyclerView.onLayout(SourceFile:4404)

                        possibly related to the workaround for:
                          https://issuetracker.google.com/issues/135628748
                     */
                    Log.w(ex);
                }
            }

            @Override
            public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
                iProperties.layoutChanged();
            }

            @Override
            public void onItemsRemoved(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
                iProperties.layoutChanged();
            }
        };
        rvMessage.setLayoutManager(llm);

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
            rvMessage.addItemDecoration(itemDecorator);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // https://developer.android.com/reference/android/view/ScrollCaptureCallback
            rvMessage.setScrollCaptureHint(android.view.View.SCROLL_CAPTURE_HINT_EXCLUDE_DESCENDANTS);
            rvMessage.setScrollCaptureCallback(new ScrollCaptureCallback() {
                private View child;
                private Rect rect;

                @Override
                public void onScrollCaptureSearch(@NonNull CancellationSignal signal, @NonNull Consumer<Rect> onReady) {
                    rect = new Rect();
                    try {
                        List<Long> expanded = values.get("expanded");
                        Log.i("Capture expanded=" + (expanded == null ? null : expanded.size()));
                        if (expanded != null && expanded.size() == 1) {
                            long id = expanded.get(0);
                            int pos = adapter.getPositionForKey(id);
                            Log.i("Capture pos=" + pos);
                            child = llm.findViewByPosition(pos);
                            Log.i("Capture child=" + child);
                            if (child != null) {
                                int w = child.getWidth();
                                int h = child.getHeight();
                                Log.i("Capture " + w + "x" + h);
                                rect.set(0, 0, w, h);
                            }
                        }
                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        Log.i("Capture search=" + rect);
                        onReady.accept(rect);
                    }
                }

                @Override
                public void onScrollCaptureStart(@NonNull ScrollCaptureSession session, @NonNull CancellationSignal signal, @NonNull Runnable onReady) {
                    Log.i("Capture selected scroll=" + session.getScrollBounds());
                    onReady.run();
                }

                @Override
                public void onScrollCaptureImageRequest(@NonNull ScrollCaptureSession session, @NonNull CancellationSignal signal, @NonNull Rect captureArea, @NonNull Consumer<Rect> onComplete) {
                    Log.i("Capture draw=" + captureArea + " scroll=" + session.getScrollBounds());
                    Canvas canvas = session.getSurface().lockHardwareCanvas();
                    canvas.save();
                    try {
                        canvas.translate(-captureArea.left, -captureArea.top - session.getScrollBounds().bottom);
                        child.draw(canvas);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        canvas.restore();
                        session.getSurface().unlockCanvasAndPost(canvas);
                    }

                    Log.i("Capture drawn");
                    onComplete.accept(captureArea);
                }

                @Override
                public void onScrollCaptureEnd(@NonNull Runnable onReady) {
                    Log.i("Capture end");
                    child = null;
                    rect = null;
                    onReady.run();
                }
            });
        }

        View inGroup = view.findViewById(R.id.inGroup);
        TextView tvFixedCategory = inGroup.findViewById(R.id.tvCategory);
        TextView tvFixedDate = inGroup.findViewById(R.id.tvDate);
        View vFixedSeparator = inGroup.findViewById(R.id.vSeparator);

        String sort = prefs.getString(getSort(getContext(), viewType, type), "time");
        inGroup.setVisibility(date_fixed && "time".equals(sort) ? View.INVISIBLE : View.GONE);
        tvFixedCategory.setVisibility(View.GONE);
        if (cards)
            vFixedSeparator.setVisibility(View.GONE);
        if (date_bold)
            tvFixedDate.setTypeface(Typeface.DEFAULT_BOLD);

        DividerItemDecoration dateDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int count = parent.getChildCount();
                String sort = (adapter == null ? null : adapter.getSort());

                if (date_fixed)
                    if ("time".equals(sort))
                        inGroup.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
                    else
                        inGroup.setVisibility(View.GONE);

                for (int i = 0; i < count; i++) {
                    View view = parent.getChildAt(i);
                    int pos = parent.getChildAdapterPosition(view);

                    if (i == 0 && date_fixed && "time".equals(sort)) {
                        TupleMessageEx top = adapter.getItemAtPosition(pos);
                        tvFixedDate.setVisibility(top == null ? View.INVISIBLE : View.VISIBLE);
                        if (!cards && dividers)
                            vFixedSeparator.setVisibility(top == null ? View.INVISIBLE : View.VISIBLE);
                        tvFixedDate.setText(top == null ? null : getRelativeDate(top.received, parent.getContext()));
                    } else {
                        View header = getView(view, parent, pos);
                        if (header != null) {
                            canvas.save();
                            canvas.translate(0, parent.getChildAt(i).getTop() - header.getMeasuredHeight());
                            header.draw(canvas);
                            canvas.restore();
                        }
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

                TupleMessageEx prev = adapter.getItemAtPosition(pos - 1);
                TupleMessageEx message = adapter.getItemAtPosition(pos);
                if (pos > 0 && prev == null)
                    return null;
                if (message == null)
                    return null;

                boolean ch = (group_category &&
                        viewType == AdapterMessage.ViewType.UNIFIED &&
                        (pos == 0
                                ? message.accountCategory != null
                                : !Objects.equals(prev.accountCategory, message.accountCategory)));
                boolean dh = (date && !date_fixed && SORT_DATE_HEADER.contains(adapter.getSort()));

                if (!ch && !dh)
                    return null;

                Integer importance =
                        (viewType == AdapterMessage.ViewType.UNIFIED ||
                                viewType == AdapterMessage.ViewType.FOLDER
                                ? message.importance : null);

                if (EntityMessage.PRIORITIY_HIGH.equals(importance)) {
                    if (pos > 0)
                        return null;
                } else if (EntityMessage.PRIORITIY_LOW.equals(importance)) {
                    if (prev != null && EntityMessage.PRIORITIY_LOW.equals(prev.importance))
                        return null;
                } else if (pos > 0) {
                    Calendar cal0 = Calendar.getInstance();
                    Calendar cal1 = Calendar.getInstance();
                    cal0.setMinimalDaysInFirstWeek(4); // ISO 8601
                    cal1.setMinimalDaysInFirstWeek(4); // ISO 8601
                    cal0.setFirstDayOfWeek(Calendar.MONDAY);
                    cal1.setFirstDayOfWeek(Calendar.MONDAY);
                    cal0.setTimeInMillis(prev.received);
                    cal1.setTimeInMillis(message.received);
                    int year0 = cal0.get(Calendar.YEAR);
                    int year1 = cal1.get(Calendar.YEAR);
                    if (date_week && year0 - 1 == year1)
                        year0--;
                    int day0 = cal0.get(date_week ? Calendar.WEEK_OF_YEAR : Calendar.DAY_OF_YEAR);
                    int day1 = cal1.get(date_week ? Calendar.WEEK_OF_YEAR : Calendar.DAY_OF_YEAR);
                    if (year0 == year1 && day0 == day1 &&
                            !EntityMessage.PRIORITIY_HIGH.equals(prev.importance))
                        dh = false;
                }

                if (!ch && !dh)
                    return null;

                View header = inflater.inflate(R.layout.item_group, parent, false);
                TextView tvCategory = header.findViewById(R.id.tvCategory);
                TextView tvDate = header.findViewById(R.id.tvDate);
                tvCategory.setVisibility(ch ? View.VISIBLE : View.GONE);
                tvDate.setVisibility(dh ? View.VISIBLE : View.GONE);

                if (ch) {
                    tvCategory.setText(message.accountCategory);
                    if (date_bold)
                        tvCategory.setTypeface(Typeface.DEFAULT_BOLD);
                }

                if (dh) {
                    int zoom = adapter.getZoom();
                    if (zoom > 0)
                        zoom--;
                    tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, Helper.getTextSize(parent.getContext(), zoom));
                    if (date_bold)
                        tvDate.setTypeface(Typeface.DEFAULT_BOLD);

                    if (cards || !dividers) {
                        View vSeparator = header.findViewById(R.id.vSeparator);
                        vSeparator.setVisibility(View.GONE);
                    }

                    if (EntityMessage.PRIORITIY_HIGH.equals(importance))
                        tvDate.setText(R.string.title_important);
                    else if (EntityMessage.PRIORITIY_LOW.equals(importance))
                        tvDate.setText(R.string.title_unimportant);
                    else
                        tvDate.setText(date_week
                                ? getWeek(message.received, parent.getContext())
                                : getRelativeDate(message.received, parent.getContext()));

                    view.setContentDescription(tvDate.getText().toString());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        view.setAccessibilityHeading(true);
                }

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }

            @NonNull
            String getRelativeDate(long time, Context context) {
                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.add(Calendar.DAY_OF_MONTH, -1);

                try {
                    CharSequence rtime;
                    if (time <= cal.getTimeInMillis())
                        rtime = DateUtils.formatDateRange(context,
                                time, time,
                                FORMAT_SHOW_WEEKDAY | FORMAT_SHOW_DATE);
                    else
                        rtime = DateUtils.getRelativeTimeSpanString(
                                time, now.getTime(),
                                DAY_IN_MILLIS, 0);
                    return (rtime == null ? "" : rtime.toString());
                } catch (Throwable ex) {
                    Log.e(ex);
                    /*
                        java.util.MissingResourceException: Can't find resource for bundle android/icu/impl/data/icudt60b/supplementalData.res, key calendarPreferenceData
                            at android.icu.util.UResourceBundle.get(UResourceBundle.java:491)
                            at android.icu.util.Calendar.getKeywordValuesForLocale(Calendar.java:1873)
                            at android.icu.text.DateTimePatternGenerator.getCalendarTypeToUse(DateTimePatternGenerator.java:165)
                            at android.icu.text.DateTimePatternGenerator.addCLDRData(DateTimePatternGenerator.java:265)
                            at android.icu.text.DateTimePatternGenerator.initData(DateTimePatternGenerator.java:139)
                            at android.icu.text.DateTimePatternGenerator.getFrozenInstance(DateTimePatternGenerator.java:123)
                            at android.icu.text.DateTimePatternGenerator.getInstance(DateTimePatternGenerator.java:92)
                            at android.icu.text.DateIntervalFormat.getInstance(DateIntervalFormat.java:470)
                            at libcore.icu.DateIntervalFormat.getFormatter(DateIntervalFormat.java:100)
                            at libcore.icu.DateIntervalFormat.formatDateRange(DateIntervalFormat.java:87)
                            at libcore.icu.DateIntervalFormat.formatDateRange(DateIntervalFormat.java:49)
                            at android.text.format.DateUtils.formatDateRange(DateUtils.java:735)
                            at android.text.format.DateUtils.formatDateRange(DateUtils.java:560)
                            at android.text.format.DateUtils.formatDateRange(DateUtils.java:537)
                            at eu.faircode.email.FragmentMessages$13.getRelativeDate(SourceFile:52)
                     */
                    return Helper.getDateInstance(context).format(time);
                }
            }

            @NonNull
            String getWeek(long time, Context context) {
                StringBuilder sb = new StringBuilder();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time);
                cal.setMinimalDaysInFirstWeek(4);
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                sb.append(cal.get(Calendar.YEAR)).append("-W").append(cal.get(Calendar.WEEK_OF_YEAR));
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                sb.append(' ').append(Helper.getDateInstance(context).format(cal.getTimeInMillis()));
                return sb.toString();
            }
        };
        rvMessage.addItemDecoration(dateDecorator);

        rvMessage.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private final GestureDetector gestureDetector =
                    new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public void onLongPress(@NonNull MotionEvent e) {
                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                return;

                            int x = Math.round(e.getX());
                            int y = Math.round(e.getY());

                            Rect rect = new Rect();
                            for (int i = 0; i < rvMessage.getChildCount(); i++) {
                                View child = rvMessage.getChildAt(i);
                                if (child == null)
                                    continue;

                                dateDecorator.getItemOffsets(rect, child, rvMessage, null);
                                if (rect.height() == 0)
                                    continue;

                                rect.set(child.getLeft(), child.getTop() - rect.top, child.getRight(), child.getTop());
                                if (!rect.contains(x, y))
                                    continue;

                                int pos = rvMessage.getChildAdapterPosition(child);
                                if (pos == NO_POSITION)
                                    continue;

                                TupleMessageEx message = adapter.getItemAtPosition(pos);
                                if (message == null)
                                    continue;

                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(message.received);
                                cal.set(Calendar.HOUR_OF_DAY, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);

                                if (date_week) {
                                    cal.setMinimalDaysInFirstWeek(4); // ISO 8601
                                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                                    cal.add(Calendar.DATE, 7);
                                } else
                                    cal.add(Calendar.DATE, 1);
                                long to = cal.getTimeInMillis();

                                cal.add(Calendar.DATE, date_week ? -7 : -1);
                                long from = cal.getTimeInMillis();

                                onMenuSelect(from, to, true);
                                Helper.performHapticFeedback(view, HapticFeedbackConstants.CONFIRM);
                                return;
                            }
                        }
                    });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        rvMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy != 0) {
                    boolean down = (dy > 0 && rv.canScrollVertically(1));
                    if (scrolling != down) {
                        scrolling = down;
                        updateCompose();
                        updateExpanded();
                    }
                }
            }
        });

        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        boolean large_buttons = prefs.getBoolean("large_buttons", false);
        boolean outbox = EntityFolder.OUTBOX.equals(type);
        boolean ascending = prefs.getBoolean(getSortOrder(getContext(), viewType, type), outbox);
        boolean filter_duplicates = prefs.getBoolean("filter_duplicates", true);
        boolean filter_trash = prefs.getBoolean("filter_trash", false);

        if (viewType != AdapterMessage.ViewType.THREAD)
            filter_trash = false;

        adapter = new AdapterMessage(
                this, type, found, searched, searchedPartial, viewType,
                compact, zoom, large_buttons, sort, ascending,
                filter_duplicates, filter_trash,
                iProperties);
        if (viewType == AdapterMessage.ViewType.THREAD)
            adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                if (accessibility || BuildConfig.DEBUG) {
                    RecyclerView.ViewHolder vh = rvMessage.findViewHolderForAdapterPosition(positionStart);
                    if (vh == null && positionStart > 0)
                        vh = rvMessage.findViewHolderForAdapterPosition(positionStart - 1);
                    if (vh == null)
                        return;

                    View v = vh.itemView;
                    rvMessage.post(new RunnableEx("focus") {
                        @Override
                        protected void delegate() {
                            v.requestFocus();
                        }
                    });
                }
            }
        });
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

        ibOutbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_VIEW_OUTBOX));
            }
        });

        tvDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Manual GC");
                Runtime rt = Runtime.getRuntime();
                rt.runFinalization();
                rt.gc();
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateDebugInfo();
                    }
                }, 1000L);
            }
        });

        tvDebug.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                DB.shrinkMemory(view.getContext());
                new ViewModelProvider(getActivity()).get(ViewModelMessages.class).cleanup();
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateDebugInfo();
                    }
                }, 1000L);

                return true;
            }
        });

        ibSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getFilter(v.getContext(), "seen", viewType, type);
                boolean filter = prefs.getBoolean(name, true);
                onMenuFilter(name, !filter);
            }
        });

        ibUnflagged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getFilter(v.getContext(), "unflagged", viewType, type);
                boolean filter = prefs.getBoolean(name, true);
                onMenuFilter(name, !filter);
            }
        });

        ibSnoozed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getFilter(v.getContext(), "snoozed", viewType, type);
                boolean filter = prefs.getBoolean(name, true);
                onMenuFilter(name, !filter);
            }
        });

        etSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    endSearch();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    endSearch();
                    return true;
                } else
                    return false;
            }
        });

        etSearch.setActionRunnable(new Runnable() {
            @Override
            public void run() {
                performSearch(true);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                ActionData data = (ActionData) bottom_navigation.getTag();
                int itemId = menuItem.getItemId();
                if (itemId == actionbar_delete_id) {
                    if (data.delete)
                        onActionDelete();
                    else
                        onActionMove(EntityFolder.TRASH);
                    return true;
                } else if (itemId == R.id.action_snooze) {
                    onActionSnooze();
                    return true;
                } else if (itemId == actionbar_archive_id) {
                    onActionMove(EntityFolder.ARCHIVE);
                    return true;
                } else if (itemId == R.id.action_prev) {
                    navigate(prev, true, false);
                    return true;
                } else if (itemId == R.id.action_next) {
                    navigate(next, false, true);
                    return true;
                }
                return false;
            }

            private void onActionMove(String folderType) {
                Bundle args = new Bundle();
                args.putLong("account", account);
                args.putString("thread", thread);
                args.putLong("id", id);
                args.putString("type", folderType);
                args.putBoolean("move_thread_sent", move_thread_sent);
                args.putBoolean("filter_archive", filter_archive);

                new SimpleTask<ArrayList<MessageTarget>>() {
                    @Override
                    protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                        long aid = args.getLong("account");
                        String thread = args.getString("thread");
                        long id = args.getLong("id");
                        String type = args.getString("type");
                        boolean move_thread_sent = args.getBoolean("move_thread_sent");
                        boolean filter_archive = args.getBoolean("filter_archive");

                        ArrayList<MessageTarget> result = new ArrayList<>();

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityAccount account = db.account().getAccount(aid);
                            if (account == null)
                                return result;

                            EntityFolder targetFolder = db.folder().getFolderByType(aid, type);
                            if (targetFolder == null)
                                return result;

                            List<EntityMessage> messages = db.message().getMessagesByThread(
                                    aid, thread, threading ? null : id, null);
                            for (EntityMessage threaded : messages) {
                                EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                                if (sourceFolder != null && !sourceFolder.read_only &&
                                        !targetFolder.id.equals(threaded.folder) &&
                                        (!filter_archive || !EntityFolder.ARCHIVE.equals(sourceFolder.type)) &&
                                        !EntityFolder.DRAFTS.equals(sourceFolder.type) && !EntityFolder.OUTBOX.equals(sourceFolder.type) &&
                                        !(EntityFolder.SENT.equals(sourceFolder.type) && EntityFolder.ARCHIVE.equals(targetFolder.type)) &&
                                        !(EntityFolder.SENT.equals(sourceFolder.type) && EntityFolder.JUNK.equals(targetFolder.type)) &&
                                        (!EntityFolder.SENT.equals(sourceFolder.type) || !EntityFolder.TRASH.equals(targetFolder.type) || move_thread_sent) &&
                                        !EntityFolder.TRASH.equals(sourceFolder.type) && !EntityFolder.JUNK.equals(sourceFolder.type))
                                    result.add(new MessageTarget(context, threaded, account, sourceFolder, account, targetFolder));
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return result;
                    }

                    @Override
                    protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                        moveAsk(result, false);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentMessages.this, args, "messages:move");
            }

            private void onActionSnooze() {
                Long time = null;
                List<TupleMessageEx> list = adapter.getCurrentList();
                if (list != null)
                    for (TupleMessageEx message : list)
                        if (message != null && message.ui_snoozed != null) {
                            if (time == null || message.ui_snoozed < time || message.id.equals(id))
                                time = message.ui_snoozed;
                            if (message.id.equals(id))
                                break;
                        }

                Bundle args = new Bundle();
                args.putString("title", getString(R.string.title_snooze));
                args.putLong("account", account);
                args.putString("thread", thread);
                args.putLong("id", id);
                if (time != null)
                    args.putLong("time", time);
                args.putBoolean("finish", true);

                FragmentDialogDuration fragment = new FragmentDialogDuration();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_SNOOZE);
                fragment.show(getParentFragmentManager(), "message:snooze");
            }
        });

        fabReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReply(false);
            }
        });

        fabReply.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onReply(true);
                return true;
            }
        });

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentDialogIdentity.onCompose(
                        getContext(),
                        getViewLifecycleOwner(),
                        getParentFragmentManager(),
                        fabCompose, account, folder);
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

        fabMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMore();
            }
        });

        tvSelectedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMore();
            }
        });

        // Workaround for RTL layout bug
        boolean rtl = getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        if (rtl) {
            int dp71 = Helper.dp2pixels(getContext(), 56 /* FAB width */ + 15 /* FAB padding */);
            ((ViewGroup.MarginLayoutParams) cardMore.getLayoutParams()).setMarginEnd(dp71);
        }

        ibAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreResult result = (MoreResult) cardMore.getTag();
                if (result == null || result.single == null || !result.single.content)
                    return;
                onReply(result.single, null, v);
            }
        });

        ibSummarize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreResult result = (MoreResult) cardMore.getTag();
                if (result == null || result.single == null || !result.single.content)
                    return;

                FragmentDialogSummarize.summarize(result.single, getParentFragmentManager(), ibSummarize, getViewLifecycleOwner());
            }
        });

        ibBatchSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionSeenSelection(true, null, more_clear);
            }
        });

        ibBatchUnseen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionSeenSelection(false, null, more_clear);
            }
        });

        ibBatchSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionSnoozeSelection();
            }
        });

        ibBatchHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHideSelection(true);
            }
        });

        ibBatchFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreResult result = (MoreResult) cardMore.getTag();
                if (result == null)
                    return;
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionFlagSelection(result.unflagged, Color.TRANSPARENT, null, more_clear);
            }
        });

        ibBatchFlagColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionFlagColorSelection(more_clear);
            }
        });

        ibLowImportance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionSetImportanceSelection(EntityMessage.PRIORITIY_LOW, null, more_clear);
            }
        });

        ibNormalImportance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionSetImportanceSelection(EntityMessage.PRIORITIY_NORMAL, null, more_clear);
            }
        });

        ibHighImportance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionSetImportanceSelection(EntityMessage.PRIORITIY_HIGH, null, more_clear);
            }
        });

        Runnable runMoveTo = new RunnableEx("moveto") {
            @Override
            protected void delegate() {
                MoreResult result = (MoreResult) cardMore.getTag();
                if (result == null)
                    return;

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), ibMove);

                int order = 0;
                for (EntityAccount account : result.imapAccounts) {
                    order++;
                    popupMenu.getMenu().add(Menu.NONE, order, order, account.name)
                            .setIntent(new Intent().putExtra("account", account.id));
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        Intent intent = target.getIntent();
                        if (intent == null)
                            return false;

                        long account = intent.getLongExtra("account", -1);
                        onActionMoveSelectionAccount(account, false, result.folders);

                        return true;
                    }
                });

                popupMenu.show();
            }
        };

        ibMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreResult result = (MoreResult) cardMore.getTag();
                if (result == null)
                    return;

                if (result.copyto == null)
                    runMoveTo.run();
                else
                    onActionMoveSelectionAccount(result.copyto.id, false, result.folders);
            }
        });

        ibMove.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                runMoveTo.run();
                return true;
            }
        });

        ibArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionMoveSelection(EntityFolder.ARCHIVE, false);
            }
        });

        ibTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionMoveSelection(EntityFolder.TRASH, false);
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionDeleteSelection();
            }
        });

        ibJunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreResult result = (MoreResult) cardMore.getTag();
                if (result == null)
                    return;

                if (result.hasPop && !result.hasImap)
                    onActionBlockSender();
                else if (!result.hasPop && result.hasImap)
                    onActionJunkSelection();
            }
        });

        ibInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionMoveSelection(EntityFolder.INBOX, false);
            }
        });

        ibInbox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MoreResult result = (MoreResult) cardMore.getTag();
                if (result == null || !result.isJunk)
                    return false;

                if (result.hasPop && !result.hasImap)
                    onActionBlockSender();
                else if (!result.hasPop && result.hasImap)
                    onActionJunkSelection();

                return true;
            }
        });

        ibKeywords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean more_clear = prefs.getBoolean("more_clear", true);
                onActionManageKeywords(more_clear);
            }
        });

        ibMoreSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogQuickActions buttons = new FragmentDialogQuickActions();
                buttons.setTargetFragment(FragmentMessages.this, REQUEST_QUICK_ACTIONS);
                buttons.show(getParentFragmentManager(), "dialog:quickactions");
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folder > 0 && !server) {
                    search(getContext(), getViewLifecycleOwner(), getParentFragmentManager(),
                            account, folder, true, criteria);
                    return;
                }

                Bundle args = new Bundle();
                args.putLong("account", server ? -1L : account);
                args.putLong("folder", server ? -1L : folder);

                new SimpleTask<List<EntityAccount>>() {
                    @Override
                    protected List<EntityAccount> onExecute(Context context, Bundle args) {
                        long aid = args.getLong("account");
                        long fid = args.getLong("folder");

                        List<EntityAccount> result = new ArrayList<>();
                        DB db = DB.getInstance(context);
                        if (aid < 0)
                            result.addAll(db.account().getSynchronizingAccounts(EntityAccount.TYPE_IMAP));
                        else {
                            EntityAccount account = db.account().getAccount(aid);
                            if (account != null && account.protocol == EntityAccount.TYPE_IMAP)
                                result.add(account);
                        }

                        if (fid > 0) {
                            EntityFolder folder = db.folder().getFolder(fid);
                            if (folder != null)
                                args.putString("folderName", folder.getDisplayName(context));
                        }

                        if (result.size() == 0)
                            throw new IllegalArgumentException(context.getString(R.string.title_no_search));
                        else
                            return result;
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                        if (accounts.size() == 1) {
                            EntityAccount account = accounts.get(0);
                            if (account.isGmail() && !server)
                                searchArchive(account.id);
                            else
                                searchAccount(account.id);
                        } else {
                            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), fabSearch);

                            int order = 0;

                            SpannableString ss = new SpannableString(getString(R.string.title_search_server));
                            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
                            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
                            popupMenu.getMenu().add(Menu.NONE, 0, order++, ss)
                                    .setEnabled(false);

                            for (EntityAccount account : accounts)
                                popupMenu.getMenu().add(Menu.NONE, 1, order++, account.name)
                                        .setIntent(new Intent()
                                                .putExtra("account", account.id)
                                                .putExtra("gmail", account.isGmail()));

                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem target) {
                                    Intent intent = target.getIntent();
                                    if (intent == null)
                                        return false;

                                    long account = intent.getLongExtra("account", -1);
                                    boolean gmail = intent.getBooleanExtra("gmail", false);
                                    if (gmail && !server)
                                        searchArchive(account);
                                    else
                                        searchAccount(account);

                                    return true;
                                }
                            });

                            popupMenu.show();
                        }
                    }

                    private void searchAccount(long account) {
                        Bundle aargs = new Bundle();
                        aargs.putInt("icon", R.drawable.twotone_search_24);
                        aargs.putString("title", getString(R.string.title_search_in));
                        aargs.putLong("account", account);
                        aargs.putLongArray("disabled", new long[]{});
                        aargs.putSerializable("criteria", criteria);

                        FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
                        fragment.setArguments(aargs);
                        fragment.setTargetFragment(FragmentMessages.this, REQUEST_SEARCH);
                        fragment.show(getParentFragmentManager(), "messages:search");
                    }

                    private void searchArchive(long account) {
                        Bundle args = new Bundle();
                        args.putLong("account", account);

                        new SimpleTask<EntityFolder>() {
                            @Override
                            protected EntityFolder onExecute(Context context, Bundle args) {
                                long account = args.getLong("account");

                                DB db = DB.getInstance(context);
                                return db.folder().getFolderByType(account, EntityFolder.ARCHIVE);
                            }

                            @Override
                            protected void onExecuted(Bundle args, EntityFolder archive) {
                                if (archive == null)
                                    searchAccount(args.getLong("account"));
                                else
                                    search(getContext(), getViewLifecycleOwner(), getParentFragmentManager(),
                                            archive.account, archive.id, true, criteria);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentMessages.this, args, "search:folder");
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        boolean report = !(ex instanceof IllegalArgumentException);
                        Log.unexpectedError(getParentFragmentManager(), ex, report);
                    }
                }.execute(FragmentMessages.this, args, "messages:search");
            }
        });

        fabError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Boolean.TRUE.equals(v.getTag())) {
                    Bundle args = new Bundle();
                    args.putBoolean("settings", false);

                    FragmentAccounts fragment = new FragmentAccounts();
                    fragment.setArguments(args);

                    FragmentManager fm = getParentFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("unified");
                    fragmentTransaction.commit();
                } else
                    onMenuFolders(account);
            }
        });

        addKeyPressedListener(keyPressedListener);
        setBackPressedCallback(backPressedCallback);

        // Initialize
        grpAirplane.setVisibility(View.GONE);
        grpNotifications.setVisibility(View.GONE);
        grpBatteryOptimizations.setVisibility(View.GONE);
        grpDataSaver.setVisibility(View.GONE);
        grpVpnActive.setVisibility(View.GONE);
        tvNoEmail.setVisibility(View.GONE);
        tvNoEmailHint.setVisibility(View.GONE);
        etSearch.setVisibility(View.GONE);
        sbThread.setVisibility(View.GONE);
        ibDown.setVisibility(View.GONE);
        ibUp.setVisibility(View.GONE);
        tvDebug.setText(null);
        tvDebug.setVisibility(
                BuildConfig.DEBUG && viewType != AdapterMessage.ViewType.THREAD
                        ? View.VISIBLE : View.GONE);
        ibSeen.setVisibility(View.GONE);
        ibUnflagged.setVisibility(View.GONE);
        ibSnoozed.setVisibility(View.GONE);
        bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(false);
        bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(false);
        updateNavPrevNext();
        bottom_navigation.setVisibility(actionbar && viewType == AdapterMessage.ViewType.THREAD ? View.INVISIBLE : View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        grpOutbox.setVisibility(View.GONE);

        fabReply.hide();

        if (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER)
            fabCompose.show();
        else
            fabCompose.hide();

        if (viewType != AdapterMessage.ViewType.SEARCH ||
                criteria == null ||
                criteria.with_hidden ||
                criteria.with_encrypted ||
                criteria.with_attachments ||
                criteria.with_notes ||
                criteria.with_types != null) {
            fabSearch.hide();
            if (animator != null && animator.isStarted())
                animator.end();
        } else {
            fabSearch.setBackgroundTintList(ColorStateList.valueOf(
                    Helper.resolveColor(getContext(), server
                            ? R.attr.colorSeparator
                            : R.attr.colorFabBackground)));
            fabSearch.show();
            if (server) {
                if (animator != null && animator.isStarted())
                    animator.end();
            } else {
                if (animator != null && !animator.isStarted())
                    animator.start();
            }
        }

        fabMore.hide();
        tvSelectedCount.setVisibility(View.GONE);
        cardMore.setVisibility(View.GONE);
        fabError.hide();

        if (viewType == AdapterMessage.ViewType.THREAD) {
            ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getContext(), getViewLifecycleOwner(), id, lpos, new ViewModelMessages.IPrevNext() {
                @Override
                public void onPrevious(boolean exists, Long id) {
                    boolean reversed = prefs.getBoolean("reversed", false);
                    if (reversed)
                        next = id;
                    else
                        prev = id;
                    bottom_navigation.getMenu().findItem(R.id.action_prev).setEnabled(prev != null);
                    bottom_navigation.getMenu().findItem(R.id.action_next).setEnabled(next != null);
                    updateNavPrevNext();
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
                    updateNavPrevNext();
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
                            navigate(prev, true, false);

                        return (prev != null);
                    }

                    @Override
                    public boolean onSwipeLeft() {
                        if (next == null) {
                            Animation bounce = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_left);
                            view.startAnimation(bounce);
                        } else
                            navigate(next, false, true);

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
                    // https://issuetracker.google.com/issues/154178289
                    .withGestureTooltypes(MotionEvent.TOOL_TYPE_FINGER, MotionEvent.TOOL_TYPE_STYLUS)
                    .build();
            adapter.setSelectionTracker(selectionTracker);

            selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
                @Override
                public void onSelectionChanged() {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;

                    FragmentActivity activity = getActivity();
                    if (activity != null)
                        activity.invalidateOptionsMenu();

                    updateMore();
                }

                @Override
                public void onItemStateChanged(@NonNull Long key, boolean selected) {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;

                    int pos = adapter.getPositionForKey(key);
                    if (pos == NO_POSITION)
                        return;

                    RecyclerView.ViewHolder viewHolder = rvMessage.findViewHolderForAdapterPosition(pos);
                    if (viewHolder == null)
                        return;

                    adapter.onItemSelected((AdapterMessage.ViewHolder) viewHolder, selected);
                }
            });
        }

        swipeRefresh.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                if (viewType != AdapterMessage.ViewType.UNIFIED && viewType != AdapterMessage.ViewType.FOLDER)
                    return true;
                if (!prefs.getBoolean("pull", true) && !EntityFolder.OUTBOX.equals(type))
                    return true;
                if (swiping)
                    return true;
                if (selectionTracker != null && selectionTracker.hasSelection())
                    return true;
                return rvMessage.canScrollVertically(-1);
            }
        });

        if (viewType == AdapterMessage.ViewType.THREAD)
            try {
                boolean swipe_close = prefs.getBoolean("swipe_close", false);
                boolean swipe_move = prefs.getBoolean("swipe_move", false);
                IOverScrollDecor decor = new VerticalOverScrollBounceEffectDecorator(
                        new RecyclerViewOverScrollDecorAdapter(rvMessage, touchHelper) {
                            @Override
                            public boolean isInAbsoluteStart() {
                                if (!swipe_close)
                                    return false;
                                return super.isInAbsoluteStart();
                            }

                            @Override
                            public boolean isInAbsoluteEnd() {
                                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                    return false;

                                PagedList<TupleMessageEx> list = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList();
                                if (list == null)
                                    return false;

                                boolean moveable = false;
                                for (TupleMessageEx message : list) {
                                    if (message == null)
                                        return false;

                                    if (!EntityFolder.isOutgoing(message.folderType) &&
                                            (!filter_archive || !EntityFolder.ARCHIVE.equals(message.folderType))) {
                                        moveable = true;
                                        break;
                                    }
                                }

                                if (!moveable)
                                    return false;

                                if (!swipe_move)
                                    return false;
                                return super.isInAbsoluteEnd();
                            }
                        },
                        DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD,
                        DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
                        DEFAULT_DECELERATE_FACTOR
                );

                ObjectHolder<Boolean> otriggered = new ObjectHolder<>(false);

                decor.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
                    @Override
                    public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                        float height = decor.getView().getHeight();
                        if (height == 0)
                            return;

                        if (!otriggered.value) {
                            float dx = Math.abs(offset * DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD);
                            if (offset > 0 && dx > height / 4) {
                                otriggered.value = true;
                                handleAutoClose();
                            }

                            if (offset < 0 && dx > height / 8) {
                                otriggered.value = true;

                                moveThread();
                            }
                        }
                    }
                });

                decor.setOverScrollStateListener(new IOverScrollStateListener() {
                    @Override
                    public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) {
                        // offset is unreliable
                        if (newState == IOverScrollState.STATE_IDLE)
                            otriggered.value = false;
                    }
                });
            } catch (Throwable ex) {
            /*
                java.lang.NoClassDefFoundError: Failed resolution of: Lme/a/a/a/a/a/b$1;
                  at me.a.a.a.a.a.b.setUpTouchHelperCallback(SourceFile:78)
                  at me.a.a.a.a.a.b.<init>(SourceFile:69)
                  at eu.faircode.email.FragmentMessages$29.<init>(SourceFile:1315)
             */
            }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        IntentFilter iff = new IntentFilter();
        iff.addAction(SimpleTask.ACTION_TASK_COUNT);
        iff.addAction(ServiceTTS.ACTION_TTS_COMPLETED);
        lbm.registerReceiver(treceiver, iff);

        return view;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(treceiver);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        try {
            FragmentManager fm = getParentFragmentManager();
            fm.removeOnBackStackChangedListener(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
        super.onDestroy();
    }

    @Override
    public void onBackStackChanged() {
        if (viewType == AdapterMessage.ViewType.THREAD)
            return;

        FragmentActivity activity = getActivity();
        FragmentManager fm = getParentFragmentManager();
        int count = fm.getBackStackEntryCount();
        boolean split = (activity instanceof ActivityView &&
                ((ActivityView) activity).isSplit() &&
                count > 0 && "thread".equals(fm.getBackStackEntryAt(count - 1).getName()));
        List<Long> ids = values.get("selected");
        if (ids != null)
            for (long id : ids)
                iProperties.setValue("split", id, split);
    }

    private void scrollToVisibleItem(LinearLayoutManager llm, boolean bottom) {
        int first = llm.findFirstVisibleItemPosition();
        int last = llm.findLastVisibleItemPosition();
        if (first == NO_POSITION || last == NO_POSITION)
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

                    View wvBody = child.findViewById(R.id.wvBody);
                    if (wvBody instanceof WebView) {
                        if (bottom) {
                            int ch = ((WebView) wvBody).getContentHeight();
                            wvBody.scrollTo(0, Helper.dp2pixels(wvBody.getContext(), ch));
                        } else
                            wvBody.scrollTo(0, 0);
                    }

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
        swipeRefresh.onRefresh();
        refresh(false);
    }

    private void refresh(boolean force) {
        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putString("type", type);
        args.putBoolean("force", force);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");
                String type = args.getString("type");

                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;
                boolean reload = false;
                boolean outbox = false;
                boolean force = args.getBoolean("force");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<EntityFolder> folders = new ArrayList<>();
                    if (fid < 0) {
                        folders.addAll(db.folder().getFoldersUnified(type, type == null));
                        if (folders.size() > 0)
                            Collections.sort(folders, folders.get(0).getComparator(context));
                    } else {
                        EntityFolder folder = db.folder().getFolder(fid);
                        if (folder != null)
                            folders.add(folder);
                    }

                    for (EntityFolder folder : folders) {
                        EntityOperation.sync(context, folder.id, true, force, true);

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
        }.execute(this, args, "messages:refresh");
    }

    private void onExpunge() {
        new AlertDialog.Builder(view.getContext())
                .setIcon(R.drawable.twotone_warning_24)
                .setTitle(R.string.title_expunge)
                .setMessage(R.string.title_expunge_remark)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expunge();
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

    private void expunge() {
        Bundle args = new Bundle();
        args.putLong("id", folder);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(id);
                if (folder == null)
                    return null;

                EntityOperation.queue(context, folder, EntityOperation.EXPUNGE);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:expunge");

    }

    private AdapterMessage.IProperties iProperties = new AdapterMessage.IProperties() {
        @Override
        public void setValue(String key, String value) {
            if (value == null)
                kv.remove(key);
            else
                kv.put(key, value);
        }

        @Override
        public void setValue(String name, long id, boolean enabled) {
            if (!values.containsKey(name))
                values.put(name, new ArrayList<Long>());

            if (enabled) {
                if (!values.get(name).contains(id))
                    values.get(name).add(id);
            } else
                values.get(name).remove(id);

            if ("split".equals(name) || ("selected".equals(name) && enabled)) {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                final List<Integer> changed = new ArrayList<>();

                int pos = adapter.getPositionForKey(id);
                if (pos != NO_POSITION)
                    changed.add(pos);

                for (Long other : new ArrayList<>(values.get(name)))
                    if (!other.equals(id)) {
                        values.get(name).remove(other);

                        pos = adapter.getPositionForKey(other);
                        if (pos != NO_POSITION)
                            changed.add(pos);
                    }

                rvMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                return;
                            if (rvMessage.isComputingLayout())
                                Log.e("isComputingLayout");
                            for (Integer pos : changed)
                                adapter.notifyItemChanged(pos);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
            }
        }

        @Override
        public String getValue(String key) {
            return kv.get(key);
        }

        @Override
        public boolean getValue(String name, long id) {
            return getValue(name, id, false);
        }

        @Override
        public boolean getValue(String name, long id, boolean def) {
            return ((values.containsKey(name)) ? values.get(name).contains(id) : def);
        }

        @Override
        public void setExpanded(TupleMessageEx message, boolean value, boolean scroll) {
            // Prevent flicker
            if (value && message.accountAutoSeen && seen_delay == 0 &&
                    (message.uid != null || message.accountProtocol == EntityAccount.TYPE_POP)) {
                message.unseen = 0;
                message.ui_seen = true;
                message.visible_unseen = 0;
                message.ui_unsnoozed = false;
            }

            setValue("expanded", message.id, value);
            if (scroll)
                setValue("scroll", message.id, true);

            final int p = adapter.getPositionForKey(message.id);
            if (p != NO_POSITION)
                rvMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                return;
                            if (rvMessage.isComputingLayout())
                                Log.e("isComputingLayout");
                            adapter.notifyItemChanged(p);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });

            // Collapse other messages
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean expand_all = prefs.getBoolean("expand_all", false);
            boolean expand_one = prefs.getBoolean("expand_one", true);
            if (!expand_all && expand_one) {
                for (Long other : new ArrayList<>(values.get("expanded")))
                    if (!other.equals(message.id)) {
                        values.get("expanded").remove(other);

                        int pos = adapter.getPositionForKey(other);
                        if (pos == NO_POSITION)
                            continue;

                        rvMessage.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                        return;
                                    if (rvMessage.isComputingLayout())
                                        Log.e("isComputingLayout");
                                    adapter.notifyItemChanged(pos);
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                    /*
                                        java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling eu.faircode.email.FixedRecyclerView{8162ff9 VFED..... .......D 0,0-1080,1939 #7f0a03b3 app:id/rvMessage}, adapter:eu.faircode.email.AdapterMessage@7a67b3e, layout:eu.faircode.email.FragmentMessages$6@b638d9f, context:eu.faircode.email.ActivityView@6627433
                                          at androidx.recyclerview.widget.RecyclerView.assertNotInLayoutOrScroll(SourceFile:3153)
                                          at androidx.recyclerview.widget.RecyclerView$RecyclerViewDataObserver.onItemRangeChanged(SourceFile:5693)
                                          at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged(SourceFile:12645)
                                          at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged(SourceFile:12635)
                                          at androidx.recyclerview.widget.RecyclerView$Adapter.notifyItemChanged(SourceFile:7570)
                                     */
                                }
                            }
                        });
                    }
            }

            scrolling = false;
            updateExpanded();
            if (value)
                handleExpand(message.id);
        }

        @Override
        public void setSize(long id, Float size) {
            if (size == null)
                sizes.remove(id);
            else
                sizes.put(id, size);
        }

        @Override
        public float getSize(long id, float defaultSize) {
            return sizes.get(id, defaultSize);
        }

        @Override
        public void setHeight(long id, Integer height) {
            if (height == null)
                heights.remove(id);
            else
                heights.put(id, height);
        }

        @Override
        public int getHeight(long id, int defaultHeight) {
            return heights.get(id, defaultHeight);
        }

        @Override
        public void setPosition(long id, Pair<Integer, Integer> delta, Pair<Integer, Integer> position) {
            if (delta != null && delta.second != 0) {
                boolean down = (delta.second > 0);
                if (scrolling != down) {
                    scrolling = down;
                    updateCompose();
                    updateExpanded();
                }
            }

            if (position == null)
                positions.remove(id);
            else
                positions.put(id, position);
        }

        @Override
        public Pair<Integer, Integer> getPosition(long id) {
            return positions.get(id);
        }

        @Override
        public void setAttachments(long id, List<EntityAttachment> list) {
            synchronized (attachments) {
                attachments.put(id, list);
            }
        }

        @Override
        public List<EntityAttachment> getAttachments(long id) {
            synchronized (attachments) {
                return attachments.get(id);
            }
        }

        @Override
        public void scrollTo(final int pos, final int y) {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;
                    LinearLayoutManager llm = (LinearLayoutManager) rvMessage.getLayoutManager();
                    View child = llm.getChildAt(pos);
                    int dy = (child == null ? 0 : llm.getTopDecorationHeight(child));
                    llm.scrollToPositionWithOffset(pos, -y - dy);
                }
            });
        }

        @Override
        public void scrollBy(int x, int y) {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;
                    rvMessage.scrollBy(x, y);
                }
            });
        }

        @Override
        public void ready(long id) {
            iProperties.setValue("ready", id, true);

            if (!values.containsKey("expanded"))
                return;

            for (long expanded : values.get("expanded"))
                if (!iProperties.getValue("ready", expanded))
                    return;

            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                            return;
                        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.ALLOW);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
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

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            return result;

                        EntityFolder sourceFolder = db.folder().getFolder(message.folder);
                        if (sourceFolder == null)
                            return result;

                        EntityFolder targetFolder = db.folder().getFolderByType(message.account, type);
                        if (targetFolder == null)
                            return result;

                        if (sourceFolder.id.equals(targetFolder.id))
                            return result;

                        result.add(new MessageTarget(context, message, account, sourceFolder, account, targetFolder));

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                    moveAsk(result, false);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(FragmentMessages.this, args, "messages:move");
        }

        @Override
        public int getSelectionCount() {
            return getSelection().length;
        }

        @Override
        public void moveSelection(String type, boolean block) {
            onActionMoveSelection(type, block);
        }

        @Override
        public void reply(TupleMessageEx message, CharSequence selected, View anchor) {
            onReply(message, selected, anchor);
        }

        public void startSearch(TextView view) {
            FragmentMessages.this.startSearch(view);
        }

        public void endSearch() {
            FragmentMessages.this.endSearch();
        }

        @Override
        public void lock(long id) {
            Bundle args = new Bundle();
            args.putLong("id", id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");
                    lockMessage(id);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(FragmentMessages.this, args, "message:lock");
        }

        @Override
        public void layoutChanged() {
            if (rvMessage == null)
                return;
            rvMessage.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (scrolling &&
                                rvMessage != null &&
                                !rvMessage.canScrollVertically(-1)) {
                            scrolling = false;
                            updateCompose();
                            updateExpanded();
                        }
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });
        }

        @Override
        public void refresh() {
            rvMessage.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                            return;
                        if (rvMessage.isComputingLayout())
                            Log.e("isComputingLayout");
                        rvMessage.setItemViewCacheSize(0);
                        rvMessage.getRecycledViewPool().clear();
                        rvMessage.setItemViewCacheSize(ITEM_CACHE_SIZE);
                        adapter.notifyDataSetChanged();
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });
        }

        @Override
        public void finish() {
            FragmentMessages.this.finish();
        }
    };

    private ItemTouchHelper.Callback touchHelper = new ItemTouchHelper.Callback() {
        private Runnable enableSelection = new Runnable() {
            @Override
            public void run() {
                if (selectionPredicate != null)
                    selectionPredicate.setEnabled(true);
            }
        };

        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return super.getSwipeEscapeVelocity(defaultValue) * getSwipeSensitivityFactor();
        }

        @Override
        public float getSwipeVelocityThreshold(float defaultValue) {
            return super.getSwipeVelocityThreshold(defaultValue) * getSwipeSensitivityFactor();
        }

        private int getSwipeSensitivityFactor() {
            int swipe_sensitivity = FragmentOptionsBehavior.DEFAULT_SWIPE_SENSITIVITY;
            Context context = getContext();
            if (context != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                swipe_sensitivity = prefs.getInt("swipe_sensitivity", swipe_sensitivity);
            }
            return (FragmentOptionsBehavior.MAX_SWIPE_SENSITIVITY - swipe_sensitivity + 1);
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int pos = viewHolder.getAdapterPosition();
            if (pos == NO_POSITION)
                return 0;

            TupleMessageEx message = getMessage(pos);
            if (message == null)
                return 0;

            boolean expanded = iProperties.getValue("expanded", message.id);

            if (expanded && swipe_reply)
                return makeMovementFlags(0, ItemTouchHelper.RIGHT);

            if (EntityFolder.OUTBOX.equals(message.folderType))
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null)
                return 0;

            if (message.uid == null &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP &&
                    EntityFolder.DRAFTS.equals(message.folderType))
                return makeMovementFlags(0,
                        (EntityFolder.TRASH.equals(swipes.left_type) ? ItemTouchHelper.LEFT : 0) |
                                (EntityFolder.TRASH.equals(swipes.right_type) ? ItemTouchHelper.RIGHT : 0));

            if (message.uid == null && message.accountProtocol == EntityAccount.TYPE_IMAP)
                return 0;

            if (!message.content) {
                if (EntityMessage.SWIPE_ACTION_SUMMARIZE.equals(swipes.swipe_left))
                    swipes.swipe_left = null;
                if (EntityMessage.SWIPE_ACTION_SUMMARIZE.equals(swipes.swipe_right))
                    swipes.swipe_right = null;
            }

            if (message.folderReadOnly) {
                if (!EntityMessage.SWIPE_ACTION_SEEN.equals(swipes.swipe_left) &&
                        !EntityMessage.SWIPE_ACTION_FLAG.equals(swipes.swipe_left))
                    swipes.swipe_left = null;
                if (!EntityMessage.SWIPE_ACTION_SEEN.equals(swipes.swipe_right) &&
                        !EntityMessage.SWIPE_ACTION_FLAG.equals(swipes.swipe_right))
                    swipes.swipe_right = null;
            }

            if (message.accountProtocol != EntityAccount.TYPE_IMAP)
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

            int flags = 0;
            if (swipes.swipe_left != null &&
                    (swipes.swipe_left < 0 ||
                            (swipes.swipe_left.equals(message.folder)
                                    ? EntityFolder.TRASH.equals(swipes.left_type) : swipes.left_type != null)))
                flags |= ItemTouchHelper.LEFT;
            if (swipes.swipe_right != null &&
                    (swipes.swipe_right < 0 ||
                            (swipes.swipe_right.equals(message.folder)
                                    ? EntityFolder.TRASH.equals(swipes.right_type) : swipes.right_type != null)))
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
                getMainHandler().removeCallbacks(enableSelection);
                if (isCurrentlyActive)
                    selectionPredicate.setEnabled(false);
                else
                    getMainHandler().postDelayed(enableSelection, ViewConfiguration.getLongPressTimeout() + 100);
            }

            Context context = getContext();
            if (context == null)
                return;

            int pos = viewHolder.getAdapterPosition();
            if (pos == NO_POSITION)
                return;

            TupleMessageEx message = getMessage(pos);
            if (message == null)
                return;

            boolean expanded = iProperties.getValue("expanded", message.id);

            TupleAccountSwipes swipes;
            if (expanded && swipe_reply) {
                swipes = new TupleAccountSwipes();
                swipes.swipe_right = EntityMessage.SWIPE_ACTION_REPLY;
                swipes.right_type = null;
                swipes.swipe_left = null;
                swipes.left_type = null;
            } else if (EntityFolder.OUTBOX.equals(message.folderType)) {
                swipes = new TupleAccountSwipes();
                if (message.warning == null) {
                    swipes.swipe_right = 0L;
                    swipes.right_type = EntityFolder.DRAFTS;
                    swipes.swipe_left = 0L;
                    swipes.left_type = EntityFolder.DRAFTS;
                } else {
                    swipes.swipe_right = EntityMessage.SWIPE_ACTION_DELETE;
                    swipes.right_type = null;
                    swipes.swipe_left = EntityMessage.SWIPE_ACTION_DELETE;
                    swipes.left_type = null;
                }
            } else {
                swipes = accountSwipes.get(message.account);
                if (swipes == null)
                    return;
            }

            if (message.uid == null &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP &&
                    EntityFolder.DRAFTS.equals(message.folderType)) {
                boolean right = EntityFolder.TRASH.equals(swipes.right_type);
                boolean left = EntityFolder.TRASH.equals(swipes.left_type);
                swipes = new TupleAccountSwipes();
                swipes.swipe_right = (right ? EntityMessage.SWIPE_ACTION_DELETE : null);
                swipes.right_type = null;
                swipes.swipe_left = (left ? EntityMessage.SWIPE_ACTION_DELETE : null);
                swipes.left_type = null;
            }

            if (message.accountProtocol != EntityAccount.TYPE_IMAP) {
                if (swipes.swipe_right == null)
                    swipes.swipe_right = EntityMessage.SWIPE_ACTION_SEEN;
                if (swipes.swipe_left == null)
                    swipes.swipe_left = EntityMessage.SWIPE_ACTION_DELETE;
            }

            Long action = (dX > 0 ? swipes.swipe_right : swipes.swipe_left);
            String actionType = (dX > 0 ? swipes.right_type : swipes.left_type);
            if (action == null)
                return;

            AdapterMessage.ViewHolder holder = ((AdapterMessage.ViewHolder) viewHolder);
            Rect rect = holder.getItemRect();
            int margin = Helper.dp2pixels(context, 12);
            int size = Helper.dp2pixels(context, 24);

            if (expanded && swipe_reply) {
                Rect r1 = new Rect();
                holder.itemView.getGlobalVisibleRect(r1);
                Rect r2 = new Rect();
                recyclerView.getGlobalVisibleRect(r2);
                rect.top = Math.max(rect.top, r1.top - r2.top);
                rect.bottom = Math.min(rect.bottom, r1.bottom - r2.top);
            }

            int icon;
            if (EntityMessage.SWIPE_ACTION_ASK.equals(action))
                icon = R.drawable.twotone_help_24;
            else if (EntityMessage.SWIPE_ACTION_SEEN.equals(action))
                icon = (message.unseen > 0 ? R.drawable.twotone_drafts_24 : R.drawable.twotone_mail_24);
            else if (EntityMessage.SWIPE_ACTION_FLAG.equals(action))
                icon = (message.ui_flagged ? R.drawable.twotone_star_border_24 : R.drawable.baseline_star_24);
            else if (EntityMessage.SWIPE_ACTION_IMPORTANCE.equals(action)) {
                int importance = (message.importance == null ? EntityMessage.PRIORITIY_NORMAL : message.importance);
                importance = (importance + 1) % 3;
                if (EntityMessage.PRIORITIY_HIGH.equals(importance))
                    icon = R.drawable.twotone_north_24;
                else if (EntityMessage.PRIORITIY_LOW.equals(importance))
                    icon = R.drawable.twotone_south_24;
                else
                    icon = R.drawable.twotone_horizontal_rule_24;
            } else if (EntityMessage.SWIPE_ACTION_SNOOZE.equals(action))
                icon = (message.ui_snoozed == null ? R.drawable.twotone_timelapse_24 : R.drawable.twotone_timer_off_24);
            else if (EntityMessage.SWIPE_ACTION_HIDE.equals(action))
                icon = (message.ui_snoozed == null ? R.drawable.twotone_visibility_off_24 :
                        (message.ui_snoozed == Long.MAX_VALUE
                                ? R.drawable.twotone_visibility_24 : R.drawable.twotone_timer_off_24));
            else if (EntityMessage.SWIPE_ACTION_MOVE.equals(action))
                icon = R.drawable.twotone_folder_24;
            else if (EntityMessage.SWIPE_ACTION_TTS.equals(action))
                icon = R.drawable.twotone_play_arrow_24;
            else if (EntityMessage.SWIPE_ACTION_SUMMARIZE.equals(action))
                icon = R.drawable.twotone_smart_toy_24;
            else if (EntityMessage.SWIPE_ACTION_JUNK.equals(action))
                icon = R.drawable.twotone_report_24;
            else if (EntityMessage.SWIPE_ACTION_DELETE.equals(action) ||
                    (action.equals(message.folder) && EntityFolder.TRASH.equals(message.folderType)) ||
                    (EntityFolder.TRASH.equals(actionType) && EntityFolder.JUNK.equals(message.folderType)))
                icon = (message.accountLeaveDeleted && EntityFolder.INBOX.equals(message.folderType)
                        ? R.drawable.twotone_delete_24
                        : R.drawable.twotone_delete_forever_24);
            else if (EntityMessage.SWIPE_ACTION_REPLY.equals(action))
                icon = R.drawable.twotone_reply_24;
            else
                icon = EntityFolder.getIcon(dX > 0 ? swipes.right_type : swipes.left_type);

            Drawable d = ContextCompat.getDrawable(context, icon).mutate();
            d.setTint(Helper.resolveColor(context, android.R.attr.textColorSecondary));

            int half = rect.width() / 2;
            if (dX > 0) {
                // Right swipe
                if (dX < half)
                    d.setAlpha(Math.round(255 * Math.min(dX / (2 * margin + size), 1.0f)));
                else
                    d.setAlpha(Math.round(255 * (1.0f - (dX - half) / half)));
                if (swipes.right_color == null) {
                    Integer color = EntityFolder.getDefaultColor(swipes.swipe_right, swipes.right_type, context);
                    if (color != null)
                        d.setTint(color);
                } else
                    d.setTint(swipes.right_color);
                int padding = (rect.height() - size);
                d.setBounds(
                        rect.left + margin,
                        rect.top + padding / 2,
                        rect.left + margin + size,
                        rect.top + padding / 2 + size);
                d.draw(canvas);
            } else if (dX < 0) {
                // Left swipe
                if (-dX < half)
                    d.setAlpha(Math.round(255 * Math.min(-dX / (2 * margin + size), 1.0f)));
                else
                    d.setAlpha(Math.round(255 * (1.0f - (-dX - half) / half)));
                if (swipes.left_color == null) {
                    Integer color = EntityFolder.getDefaultColor(swipes.swipe_left, swipes.left_type, context);
                    if (color != null)
                        d.setTint(color);
                } else
                    d.setTint(swipes.left_color);
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
            try {
                int pos = viewHolder.getAdapterPosition();
                if (pos == NO_POSITION) {
                    redraw(null);
                    return;
                }

                TupleMessageEx message = getMessage(pos);
                if (message == null) {
                    redraw(null);
                    return;
                }

                boolean expanded = iProperties.getValue("expanded", message.id);

                if (expanded && swipe_reply) {
                    redraw(viewHolder);
                    onMenuReply(message, "reply", null, null);
                    return;
                }

                if (EntityFolder.OUTBOX.equals(message.folderType)) {
                    if (message.warning == null)
                        ActivityCompose.undoSend(message.id, getContext(), getViewLifecycleOwner(), getParentFragmentManager());
                    else
                        onDelete(message.id);
                    return;
                }

                TupleAccountSwipes swipes = accountSwipes.get(message.account);
                if (swipes == null) {
                    redraw(null);
                    return;
                }

                if (message.accountProtocol != EntityAccount.TYPE_IMAP) {
                    if (swipes.swipe_right == null)
                        swipes.swipe_right = EntityMessage.SWIPE_ACTION_SEEN;
                    if (swipes.swipe_left == null)
                        swipes.swipe_left = EntityMessage.SWIPE_ACTION_DELETE;
                }

                Long action = (direction == ItemTouchHelper.LEFT ? swipes.swipe_left : swipes.swipe_right);
                String actionType = (direction == ItemTouchHelper.LEFT ? swipes.left_type : swipes.right_type);
                if (action == null) {
                    redraw(null);
                    return;
                }

                if (message.uid == null &&
                        message.accountProtocol == EntityAccount.TYPE_IMAP &&
                        EntityFolder.DRAFTS.equals(message.folderType) &&
                        EntityFolder.TRASH.equals(actionType)) {
                    action = EntityMessage.SWIPE_ACTION_DELETE;
                    actionType = null;
                }

                Log.i("Swiped dir=" + direction +
                        " action=" + action +
                        " type=" + actionType +
                        " message=" + message.id +
                        " folder=" + message.folderType);

                if (EntityMessage.SWIPE_ACTION_ASK.equals(action)) {
                    rvMessage.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                        @Override
                        public void onChildViewAttachedToWindow(@NonNull View view) {
                            rvMessage.removeOnChildAttachStateChangeListener(this);
                            onSwipeAsk(message, view);
                        }

                        @Override
                        public void onChildViewDetachedFromWindow(@NonNull View view) {
                        }
                    });
                    redraw(viewHolder);
                } else if (EntityMessage.SWIPE_ACTION_SEEN.equals(action)) {
                    redraw(viewHolder);
                    onActionSeenSelection(!message.ui_seen, message.id, false);
                } else if (EntityMessage.SWIPE_ACTION_FLAG.equals(action))
                    onActionFlagSelection(!message.ui_flagged, Color.TRANSPARENT, message.id, false);
                else if (EntityMessage.SWIPE_ACTION_IMPORTANCE.equals(action)) {
                    int importance = (message.importance == null ? EntityMessage.PRIORITIY_NORMAL : message.importance);
                    onActionSetImportanceSelection((importance + 1) % 3, message.id, false);
                } else if (EntityMessage.SWIPE_ACTION_SNOOZE.equals(action))
                    onSwipeSnooze(message, viewHolder);
                else if (EntityMessage.SWIPE_ACTION_HIDE.equals(action))
                    onActionHide(message);
                else if (EntityMessage.SWIPE_ACTION_MOVE.equals(action)) {
                    redraw(viewHolder);
                    onSwipeMove(message);
                } else if (EntityMessage.SWIPE_ACTION_TTS.equals(action)) {
                    redraw(viewHolder);
                    onSwipeTTS(message);
                } else if (EntityMessage.SWIPE_ACTION_SUMMARIZE.equals(action)) {
                    redraw(viewHolder);
                    onSwipeSummarize(message);
                } else if (EntityMessage.SWIPE_ACTION_JUNK.equals(action)) {
                    redraw(viewHolder);
                    onSwipeJunk(message);
                } else if (EntityMessage.SWIPE_ACTION_DELETE.equals(action) ||
                        (action.equals(message.folder) && EntityFolder.TRASH.equals(message.folderType)) ||
                        (EntityFolder.TRASH.equals(actionType) && EntityFolder.JUNK.equals(message.folderType)))
                    onSwipeDelete(message, viewHolder);
                else
                    swipeFolder(message, action);
            } catch (Throwable ex) {
                Log.e(ex);
                /*
                    java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling ...
                            at androidx.recyclerview.widget.RecyclerView.assertNotInLayoutOrScroll(RecyclerView:3185)
                            at androidx.recyclerview.widget.RecyclerView$RecyclerViewDataObserver.onItemRangeChanged(RecyclerView:5712)
                            at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged(RecyclerView:12674)
                            at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged(RecyclerView:12664)
                            at androidx.recyclerview.widget.RecyclerView$Adapter.notifyItemChanged(RecyclerView:7599)
                            at eu.faircode.email.FragmentMessages$60.onSwiped(FragmentMessages:2818)
                 */
            }
        }

        private TupleMessageEx getMessage(int pos) {
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                return null;

            if (selectionTracker != null && selectionTracker.hasSelection())
                return null;

            PagedList<TupleMessageEx> list = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList();
            if (pos >= list.size())
                return null;

            TupleMessageEx message = list.get(pos);
            if (message == null)
                return null;

            boolean expanded = iProperties.getValue("expanded", message.id);

            if (expanded && !swipe_reply)
                return null;

            return message;
        }

        private void redraw(RecyclerView.ViewHolder vh) {
            if (vh != null)
                try {
                    RecyclerView.LayoutManager lm = rvMessage.getLayoutManager();
                    if (lm != null) {
                        lm.detachView(vh.itemView);
                        lm.removeDetachedView(vh.itemView);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }

            rvMessage.post(new RunnableEx("redraw") {
                @Override
                public void delegate() {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;

                    if (rvMessage.isComputingLayout())
                        Log.e("isComputingLayout");

                    if (vh == null)
                        adapter.notifyDataSetChanged();
                    else {
                        int pos = vh.getAbsoluteAdapterPosition();
                        if (pos == NO_POSITION)
                            adapter.notifyDataSetChanged();
                        else
                            adapter.notifyItemChanged(pos);
                    }
                }
            });
        }

        private void onSwipeAsk(final @NonNull TupleMessageEx message, @NonNull View anchor) {
            Bundle args = new Bundle();
            args.putLong("account", message.account);

            new SimpleTask<Pair<EntityFolder, EntityFolder>>() {
                @Override
                protected Pair<EntityFolder, EntityFolder> onExecute(Context context, Bundle args) throws Throwable {
                    long account = args.getLong("account");

                    DB db = DB.getInstance(context);
                    return new Pair(
                            db.folder().getFolderByType(account, EntityFolder.ARCHIVE),
                            db.folder().getFolderByType(account, EntityFolder.TRASH));
                }

                @Override
                protected void onExecuted(Bundle args, Pair<EntityFolder, EntityFolder> data) {
                    // Make sure animations are done
                    rvMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Context context = getContext();

                                int order = 1;
                                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, getViewLifecycleOwner(), anchor);

                                if (message.ui_seen)
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unseen, order++, R.string.title_unseen)
                                            .setIcon(R.drawable.twotone_mail_24);
                                else
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_seen, order++, R.string.title_seen)
                                            .setIcon(R.drawable.twotone_drafts_24);

                                popupMenu.getMenu().add(Menu.NONE, R.string.title_snooze, order++, R.string.title_snooze)
                                        .setIcon(R.drawable.twotone_timelapse_24);

                                if (message.ui_snoozed == null)
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_hide, order++, R.string.title_hide)
                                            .setIcon(R.drawable.twotone_visibility_off_24);
                                else if (message.ui_snoozed == Long.MAX_VALUE)
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unhide, order++, R.string.title_unhide)
                                            .setIcon(R.drawable.twotone_visibility_24);

                                if (message.ui_flagged)
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unflag, order++, R.string.title_unflag)
                                            .setIcon(R.drawable.twotone_star_border_24);
                                else
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag, order++, R.string.title_flag)
                                            .setIcon(R.drawable.twotone_star_24);

                                popupMenu.getMenu().add(Menu.NONE, R.string.title_flag_color, order++, R.string.title_flag_color)
                                        .setIcon(R.drawable.twotone_auto_awesome_24);

                                SubMenu importance = popupMenu.getMenu()
                                        .addSubMenu(Menu.NONE, Menu.NONE, order++, R.string.title_set_importance)
                                        .setIcon(R.drawable.twotone_north_24);
                                importance.add(Menu.NONE, R.string.title_importance_high, 1, R.string.title_importance_high)
                                        .setIcon(R.drawable.twotone_north_24)
                                        .setEnabled(!EntityMessage.PRIORITIY_HIGH.equals(message.importance));
                                importance.add(Menu.NONE, R.string.title_importance_normal, 2, R.string.title_importance_normal)
                                        .setIcon(R.drawable.twotone_horizontal_rule_24)
                                        .setEnabled(!EntityMessage.PRIORITIY_NORMAL.equals(message.importance));
                                importance.add(Menu.NONE, R.string.title_importance_low, 3, R.string.title_importance_low)
                                        .setIcon(R.drawable.twotone_south_24)
                                        .setEnabled(!EntityMessage.PRIORITIY_LOW.equals(message.importance));

                                if (AI.isAvailable(context))
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_summarize, order++, R.string.title_summarize)
                                            .setIcon(R.drawable.twotone_smart_toy_24);

                                if (data.first != null && data.first.id != null)
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_archive, order++, R.string.title_archive)
                                            .setIcon(R.drawable.twotone_archive_24)
                                            .setIntent(new Intent().putExtra("folder", data.first.id));
                                if (data.second != null && data.second.id != null)
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_trash, order++, R.string.title_trash)
                                            .setIcon(R.drawable.twotone_delete_24)
                                            .setIntent(new Intent().putExtra("folder", data.second.id));

                                if (message.accountProtocol == EntityAccount.TYPE_IMAP) {
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_move, order++, R.string.title_move)
                                            .setIcon(R.drawable.twotone_drive_file_move_24);
                                    popupMenu.getMenu().add(Menu.NONE, R.string.title_report_spam, order++, R.string.title_report_spam)
                                            .setIcon(R.drawable.twotone_report_24);
                                }
                                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_permanently, order++, R.string.title_delete_permanently)
                                        .setIcon(R.drawable.twotone_delete_forever_24);

                                popupMenu.insertIcons(context);

                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem target) {
                                        int itemId = target.getItemId();
                                        if (itemId == R.string.title_seen) {
                                            onActionSeenSelection(true, message.id, false);
                                            return true;
                                        } else if (itemId == R.string.title_unseen) {
                                            onActionSeenSelection(false, message.id, false);
                                            return true;
                                        } else if (itemId == R.string.title_snooze) {
                                            onMenuSnooze();
                                            return true;
                                        } else if (itemId == R.string.title_hide || itemId == R.string.title_unhide) {
                                            onActionHide(message);
                                            return true;
                                        } else if (itemId == R.string.title_flag) {
                                            onActionFlagSelection(true, Color.TRANSPARENT, message.id, false);
                                            return true;
                                        } else if (itemId == R.string.title_unflag) {
                                            onActionFlagSelection(false, Color.TRANSPARENT, message.id, false);
                                            return true;
                                        } else if (itemId == R.string.title_flag_color) {
                                            onMenuColor();
                                            return true;
                                        } else if (itemId == R.string.title_importance_low) {
                                            onActionSetImportanceSelection(EntityMessage.PRIORITIY_LOW, message.id, false);
                                            return true;
                                        } else if (itemId == R.string.title_importance_normal) {
                                            onActionSetImportanceSelection(EntityMessage.PRIORITIY_NORMAL, message.id, false);
                                            return true;
                                        } else if (itemId == R.string.title_importance_high) {
                                            onActionSetImportanceSelection(EntityMessage.PRIORITIY_HIGH, message.id, false);
                                            return true;
                                        } else if (itemId == R.string.title_summarize) {
                                            onSwipeSummarize(message);
                                            return true;
                                        } else if (itemId == R.string.title_archive || itemId == R.string.title_trash) {
                                            Intent intent = target.getIntent();
                                            long folder = (intent == null ? -1L : intent.getLongExtra("folder", -1L));
                                            if (folder < 0)
                                                return false;
                                            swipeFolder(message, folder);
                                            return true;
                                        } else if (itemId == R.string.title_move) {
                                            onSwipeMove(message);
                                            return true;
                                        } else if (itemId == R.string.title_report_spam) {
                                            onSwipeJunk(message);
                                            return true;
                                        } else if (itemId == R.string.title_delete_permanently) {
                                            onSwipeDelete(message, null);
                                            return true;
                                        }
                                        return false;
                                    }

                                    private void onMenuSnooze() {
                                        Bundle args = new Bundle();
                                        args.putString("title", getString(R.string.title_snooze));
                                        args.putLong("account", message.account);
                                        args.putString("thread", message.thread);
                                        args.putLong("id", message.id);
                                        if (message.ui_snoozed != null)
                                            args.putLong("time", message.ui_snoozed);
                                        args.putBoolean("finish", false);

                                        FragmentDialogDuration fragment = new FragmentDialogDuration();
                                        fragment.setArguments(args);
                                        fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_SNOOZE);
                                        fragment.show(getParentFragmentManager(), "message:snooze");
                                    }

                                    private void onMenuColor() {
                                        Bundle args = new Bundle();
                                        args.putLong("id", message.id);
                                        args.putInt("color", message.color == null ? Color.TRANSPARENT : message.color);
                                        args.putString("title", getString(R.string.title_flag_color));
                                        args.putBoolean("reset", true);
                                        args.putInt("faq", 187);

                                        FragmentDialogColor fragment = new FragmentDialogColor();
                                        fragment.setArguments(args);
                                        fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_COLOR);
                                        fragment.show(getParentFragmentManager(), "message:color");
                                    }
                                });

                                popupMenu.show();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }
                    });
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragment(), ex);
                }
            }.execute(FragmentMessages.this, args, "swipe:ask");
        }

        private void onSwipeSnooze(TupleMessageEx message, RecyclerView.ViewHolder viewHolder) {
            if (!ActivityBilling.isPro(getContext())) {
                redraw(viewHolder);
                startActivity(new Intent(getContext(), ActivityBilling.class));
                return;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            long duration = prefs.getInt("default_snooze", 1) * 3600 * 1000L;

            if (duration == 0) {
                redraw(viewHolder);
                Bundle args = new Bundle();
                args.putString("title", getString(R.string.title_snooze));
                args.putLong("account", message.account);
                args.putLong("folder", message.folder);
                args.putString("thread", message.thread);
                args.putLong("id", message.id);
                if (message.ui_snoozed != null)
                    args.putLong("time", message.ui_snoozed);

                FragmentDialogDuration fragment = new FragmentDialogDuration();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_SNOOZE);
                fragment.show(getParentFragmentManager(), "message:snooze");
            } else {
                Bundle args = new Bundle();
                args.putLong("account", message.account);
                args.putString("thread", message.thread);
                args.putLong("id", message.id);
                if (message.ui_snoozed == null) {
                    args.putLong("duration", duration);
                    args.putLong("time", new Date().getTime() + duration);
                } else {
                    args.putLong("duration", 0);
                    args.putLong("time", 0);
                }

                onSnoozeOrHide(args);
            }
        }

        private void onSwipeMove(final @NonNull TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putInt("icon", R.drawable.twotone_drive_file_move_24);
            args.putString("title", getString(R.string.title_move_to_folder));
            args.putLong("account", message.account);
            args.putLongArray("disabled", new long[]{message.folder});
            args.putLong("message", message.id);
            args.putBoolean("copy", false);
            args.putBoolean("similar", true);

            FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
            fragment.setArguments(args);
            fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_MOVE);
            fragment.show(getParentFragmentManager(), "swipe:move");
        }

        private void onSwipeTTS(final @NonNull TupleMessageEx message) {
            boolean tts = iProperties.getValue("tts", message.id, false);
            iProperties.setValue("tts", message.id, !tts);

            if (tts) {
                Intent intent = new Intent(getContext(), ServiceTTS.class);
                intent.putExtra(ServiceTTS.EXTRA_FLUSH, true);
                intent.putExtra(ServiceTTS.EXTRA_TEXT, "");
                intent.putExtra(ServiceTTS.EXTRA_LANGUAGE, message.language);
                intent.putExtra(ServiceTTS.EXTRA_UTTERANCE_ID, "tts:" + message.id);
                getContext().startService(intent);
                return;
            }

            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<String>() {
                @Override
                protected String onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    StringBuilder sb = new StringBuilder();

                    if (message.received != null) {
                        DateFormat DF = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                        sb.append(DF.format(message.received)).append(". ");
                    }

                    if (message.from != null && message.from.length > 0)
                        sb.append(context.getString(R.string.title_rule_tts_from))
                                .append(' ').append(MessageHelper.formatAddressesShort(message.from)).append(". ");

                    if (!TextUtils.isEmpty(message.subject))
                        sb.append(context.getString(R.string.title_rule_tts_subject))
                                .append(' ').append(message.subject).append(". ");

                    String body = Helper.readText(message.getFile(context));
                    String text = HtmlHelper.getFullText(context, body);

                    // Avoid: Not enough namespace quota ... for ...
                    text = HtmlHelper.truncate(text, ServiceTTS.getMaxTextSize() / 3);

                    if (!TextUtils.isEmpty(text))
                        sb.append(context.getString(R.string.title_rule_tts_content))
                                .append(' ').append(text);

                    return sb.toString();
                }

                @Override
                protected void onExecuted(Bundle args, String text) {
                    if (text == null)
                        return;

                    Intent intent = new Intent(getContext(), ServiceTTS.class);
                    intent.putExtra(ServiceTTS.EXTRA_FLUSH, true);
                    intent.putExtra(ServiceTTS.EXTRA_TEXT, text);
                    intent.putExtra(ServiceTTS.EXTRA_LANGUAGE, message.language);
                    intent.putExtra(ServiceTTS.EXTRA_UTTERANCE_ID, "tts:" + message.id);
                    getContext().startService(intent);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(FragmentMessages.this, args, "tts");
        }

        private void onSwipeSummarize(final @NonNull TupleMessageEx message) {
            final Context context = getContext();
            if (AI.isAvailable(context))
                FragmentDialogSummarize.summarize(message, getParentFragmentManager(), null, getViewLifecycleOwner());
            else
                context.startActivity(new Intent(context, ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "integrations"));
        }

        private void onSwipeJunk(final @NonNull TupleMessageEx message) {
            if (message.accountProtocol == EntityAccount.TYPE_POP) {
                Bundle aargs = new Bundle();
                aargs.putLongArray("ids", new long[]{message.id});

                FragmentDialogBlockSender ask = new FragmentDialogBlockSender();
                ask.setArguments(aargs);
                ask.setTargetFragment(FragmentMessages.this, REQUEST_BLOCK_SENDERS);
                ask.show(getParentFragmentManager(), "message:block");
            } else {
                Bundle aargs = new Bundle();
                aargs.putLong("id", message.id);
                aargs.putLong("account", message.account);
                aargs.putInt("protocol", message.accountProtocol);
                aargs.putLong("folder", message.folder);
                aargs.putString("type", message.folderType);
                aargs.putString("from", DB.Converters.encodeAddresses(message.from));

                FragmentDialogJunk ask = new FragmentDialogJunk();
                ask.setArguments(aargs);
                ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_JUNK);
                ask.show(getParentFragmentManager(), "swipe:junk");
            }
        }

        private void onSwipeDelete(@NonNull TupleMessageEx message, RecyclerView.ViewHolder vh) {
            boolean leave_deleted =
                    (message.accountProtocol == EntityAccount.TYPE_POP &&
                            message.accountLeaveDeleted);

            Bundle args = new Bundle();
            if (leave_deleted)
                args.putString("question", getResources()
                        .getQuantityString(R.plurals.title_moving_messages, 1, 1));
            else
                args.putString("question", getString(R.string.title_ask_delete));
            args.putString("remark", message.getRemark());
            args.putLong("id", message.id);
            args.putInt("faq", 160);
            args.putString("notagain", "delete_asked");
            if (!leave_deleted)
                args.putString("accept", getString(R.string.title_ask_delete_accept));
            args.putBoolean("warning", true);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean delete_asked = prefs.getBoolean("delete_asked", false);
            final int undo_timeout = prefs.getInt("undo_timeout", 5000);
            if (delete_asked) {
                if (leave_deleted) {
                    new SimpleTask<Void>() {
                        @Override
                        protected void onPreExecute(Bundle args) {
                            message.ui_hide = true;
                        }

                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            long now = new Date().getTime();
                            long busy = now + undo_timeout * 2;

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                db.message().setMessageUiBusy(id, busy);
                                db.message().setMessageUiHide(id, true);
                                db.message().setMessageFound(id, false);
                                // Prevent new message notification on undo
                                db.message().setMessageUiIgnored(id, true);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }
                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void v) {
                            FragmentActivity activity = getActivity();
                            if (!(activity instanceof ActivityView)) {
                                Intent data = new Intent();
                                data.putExtra("args", args);
                                onActivityResult(REQUEST_MESSAGE_DELETE, RESULT_OK, data);
                                return;
                            }

                            String title = getString(R.string.title_move_undo, getString(R.string.title_trash), 1);
                            ((ActivityView) activity).undo(title, args, taskDeleteLeaveDo, taskDeleteLeaveUndo);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentMessages.this, args, "delete:leave");
                } else {
                    Intent data = new Intent();
                    data.putExtra("args", args);
                    onActivityResult(REQUEST_MESSAGE_DELETE, RESULT_OK, data);
                }
                return;
            }

            if (vh != null)
                redraw(vh);

            FragmentDialogAsk ask = new FragmentDialogAsk();
            ask.setArguments(args);
            ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_DELETE);
            ask.show(getParentFragmentManager(), "swipe:delete");
        }

        private void swipeFolder(@NonNull TupleMessageEx message, @NonNull Long target) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("thread", viewType != AdapterMessage.ViewType.THREAD);
            args.putLong("target", target);
            args.putBoolean("filter_archive", filter_archive);
            args.putBoolean("swipe_trash_all", swipe_trash_all);

            new SimpleTask<ArrayList<MessageTarget>>() {
                @Override
                protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean thread = args.getBoolean("thread");
                    long tid = args.getLong("target");
                    boolean filter_archive = args.getBoolean("filter_archive");
                    boolean swipe_trash_all = args.getBoolean("swipe_trash_all");

                    ArrayList<MessageTarget> result = new ArrayList<>();

                    // Get target folder and hide message
                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return result;

                        EntityAccount sourceAccount = db.account().getAccount(message.account);
                        if (sourceAccount == null)
                            return result;
                        EntityFolder baseFolder = db.folder().getFolder(message.folder);
                        if (baseFolder == null)
                            return result;

                        EntityFolder targetFolder = db.folder().getFolder(tid);
                        if (targetFolder == null)
                            throw new IllegalArgumentException(context.getString(R.string.title_no_folder));

                        EntityAccount targetAccount = db.account().getAccount(targetFolder.account);
                        if (targetAccount == null)
                            return result;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread,
                                threading && thread ? null : id,
                                swipe_trash_all &&
                                        !EntityFolder.DRAFTS.equals(baseFolder.type) &&
                                        EntityFolder.TRASH.equals(targetFolder.type) ? null : message.folder);
                        for (EntityMessage threaded : messages) {
                            EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                            if (sourceFolder == null ||
                                    sourceFolder.read_only ||
                                    sourceFolder.id.equals(targetFolder.id))
                                continue;
                            if (EntityFolder.TRASH.equals(targetFolder.type)) {
                                if (EntityFolder.ARCHIVE.equals(sourceFolder.type) && thread && filter_archive)
                                    continue;
                                if (EntityFolder.JUNK.equals(sourceFolder.type) && !threaded.folder.equals(message.folder))
                                    continue;
                            }

                            result.add(new MessageTarget(context, threaded, sourceAccount, sourceFolder, targetAccount, targetFolder));
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                    if (result == null || result.size() == 0)
                        redraw(null);
                    else
                        moveUndo(result);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        Helper.setSnackbarOptions(
                                        Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                                .show();
                    else
                        Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(FragmentMessages.this, args, "swipe:folder");
        }
    };

    private static final SimpleTask<Void> taskDeleteLeaveDo = new SimpleTask<Void>() {
        @Override
        protected Void onExecute(Context context, Bundle args) {
            long id = args.getLong("id");
            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                message.ui_busy = null;
                db.message().setMessageUiBusy(message.id, message.ui_busy);
                EntityOperation.queue(context, message, EntityOperation.DELETE);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return null;
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
        }
    };

    private static final SimpleTask<Void> taskDeleteLeaveUndo = new SimpleTask<Void>() {
        @Override
        protected Void onExecute(Context context, Bundle args) {
            long id = args.getLong("id");

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                db.message().setMessageUiHide(id, false);
                db.message().setMessageUiBusy(id, null);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            return null;
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
        }
    };

    private void onReply(boolean long_press) {
        if (values.containsKey("expanded") && values.get("expanded").size() > 0) {
            long id = values.get("expanded").get(0);
            int pos = adapter.getPositionForKey(id);
            TupleMessageEx message = adapter.getItemAtPosition(pos);
            AdapterMessage.ViewHolder holder =
                    (AdapterMessage.ViewHolder) rvMessage.findViewHolderForAdapterPosition(pos);
            CharSequence selected = (holder == null ? null : holder.getSelectedText());
            if (message == null)
                return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String action = prefs.getString(
                    long_press ? "answer_action" : "answer_single",
                    long_press ? "reply" : "menu");
            if ("move".equals(action)) {
                if (canMove(message))
                    onMenuMove(message);
            } else if ("menu".equals(action) || !message.content)
                onReply(message, selected, fabReply);
            else
                onMenuReply(message, action);
        }
    }

    private void onReply(final TupleMessageEx message, final CharSequence selected, final View anchor) {
        Bundle args = new Bundle();
        args.putLong("id", message.id);

        new SimpleTask<ReplyData>() {
            @Override
            protected void onPreExecute(Bundle args) {
                fabReply.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                fabReply.setEnabled(true);
            }

            @Override
            protected ReplyData onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean experiments = prefs.getBoolean("experiments", false);

                ReplyData result = new ReplyData();

                DB db = DB.getInstance(context);

                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return result;

                args.putInt("answers", db.answer().getAnswerCount(false));

                result.identities = db.identity().getComposableIdentities(null);
                result.answers = db.answer().getAnswersByFavorite(true);

                result.forwarded = new ArrayList<>();
                if (experiments) {
                    long last = new Date().getTime() - MAX_FORWARD_ADDRESS_AGE;
                    List<String> fwds = db.message().getForwardAddresses(message.account, last);
                    if (fwds != null)
                        for (String fwd : fwds)
                            for (Address address : DB.Converters.decodeAddresses(fwd))
                                if (address instanceof InternetAddress)
                                    result.forwarded.add((InternetAddress) address);
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, ReplyData data) {
                if (data.identities == null)
                    data.identities = new ArrayList<>();

                final Context context = getContext();
                if (context == null)
                    return;

                boolean replySelf = message.replySelf(data.identities, message.account);
                final Address[] to = replySelf
                        ? message.to
                        : (message.reply == null || message.reply.length == 0 ? message.from : message.reply);

                Address[] recipients = message.getAllRecipients(data.identities, message.account);

                int answers = args.getInt("answers"); // Non favorite

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean experiments = prefs.getBoolean("experiments", false);

                boolean canBounce = false;
                if (message.return_path != null && message.return_path.length > 0) {
                    canBounce = true;
                    for (Address return_path : message.return_path)
                        for (EntityIdentity identity : data.identities)
                            if (identity.similarAddress(return_path)) {
                                canBounce = false;
                                break;
                            }
                }

                boolean canResend = message.content;
                for (Address r : recipients) {
                    String email = ((InternetAddress) r).getAddress();
                    if ("undisclosed-recipients:".equals(email)) {
                        canResend = false;
                        break;
                    }
                }

                boolean canRaw = (message.uid != null ||
                        (EntityFolder.INBOX.equals(message.folderType) &&
                                message.accountProtocol == EntityAccount.TYPE_POP));

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, getViewLifecycleOwner(), anchor);
                popupMenu.inflate(R.menu.popup_reply);
                popupMenu.getMenu().findItem(R.id.menu_reply_to_all).setVisible(recipients.length > 0);
                popupMenu.getMenu().findItem(R.id.menu_reply_list).setVisible(message.list_post != null);
                popupMenu.getMenu().findItem(R.id.menu_reply_receipt).setVisible(message.receipt_to != null);
                popupMenu.getMenu().findItem(R.id.menu_reply_hard_bounce).setVisible(experiments);
                popupMenu.getMenu().findItem(R.id.menu_reply_hard_bounce).setEnabled(canBounce);
                popupMenu.getMenu().findItem(R.id.menu_new_message).setVisible(to != null && to.length > 0);
                popupMenu.getMenu().findItem(R.id.menu_resend).setVisible(experiments);
                popupMenu.getMenu().findItem(R.id.menu_resend).setEnabled(canResend);
                popupMenu.getMenu().findItem(R.id.menu_reply_answer).setVisible(answers != 0 || !ActivityBilling.isPro(context));

                popupMenu.getMenu().findItem(R.id.menu_reply_to_sender)
                        .setTitle(getString(replySelf ? R.string.title_reply_to_recipient : R.string.title_reply_to_sender))
                        .setEnabled(message.content);

                popupMenu.getMenu().findItem(R.id.menu_reply_to_all).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_forward).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_forward_raw).setEnabled(canRaw);
                popupMenu.getMenu().findItem(R.id.menu_editasnew).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_reply_answer).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_move_to).setEnabled(canMove(message));

                if (data.answers != null) {
                    int order = 100;
                    for (EntityAnswer answer : data.answers) {
                        SpannableStringBuilder ssb = new SpannableStringBuilderEx(answer.name);

                        if (answer.color != null) {
                            int first = answer.name.codePointAt(0);
                            int count = Character.charCount(first);
                            ssb.setSpan(new ForegroundColorSpan(answer.color), 0, count, 0);
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, count, 0);
                            ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_LARGE), 0, count, 0);
                        }

                        order++;
                        popupMenu.getMenu().add(Menu.FIRST, order, order, ssb)
                                .setIcon(R.drawable.twotone_star_24)
                                .setIntent(new Intent().putExtra("id", answer.id));
                    }
                }

                if (data.forwarded.isEmpty())
                    popupMenu.getMenu().findItem(R.id.menu_forward_to).setVisible(false);
                else {
                    int order = 200;
                    for (InternetAddress fwd : data.forwarded) {
                        order++;
                        popupMenu.getMenu().findItem(R.id.menu_forward_to).getSubMenu()
                                .add(2, order, order,
                                        MessageHelper.formatAddressesShort(new InternetAddress[]{fwd}))
                                .setIntent(new Intent()
                                        .putExtra("email", fwd.getAddress())
                                        .putExtra("name", fwd.getPersonal()));
                    }
                }

                popupMenu.insertIcons(context);

                MenuCompat.setGroupDividerEnabled(popupMenu.getMenu(), true);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        if (target.getGroupId() == Menu.FIRST) {
                            startActivity(new Intent(context, ActivityCompose.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("action", "reply")
                                    .putExtra("reference", message.id)
                                    .putExtra("answer", target.getIntent().getLongExtra("id", -1)));

                            return true;
                        }

                        if (target.getGroupId() == 2) {
                            try {
                                InternetAddress fwd = new InternetAddress(
                                        target.getIntent().getStringExtra("email"),
                                        target.getIntent().getStringExtra("name"));
                                onMenuReply(message, "forward", fwd, null);
                            } catch (UnsupportedEncodingException ex) {
                                Log.e(ex);
                                onMenuReply(message, "forward");
                            }
                            return true;
                        }

                        int itemId = target.getItemId();
                        if (itemId == R.id.menu_reply_to_sender) {
                            onMenuReply(message, "reply", null, selected);
                            return true;
                        } else if (itemId == R.id.menu_reply_to_all) {
                            onMenuReply(message, "reply_all", null, selected);
                            return true;
                        } else if (itemId == R.id.menu_reply_list) {
                            onMenuReply(message, "list", null, selected);
                            return true;
                        } else if (itemId == R.id.menu_reply_receipt) {
                            onMenuDsn(message, EntityMessage.DSN_RECEIPT);
                            return true;
                        } else if (itemId == R.id.menu_reply_hard_bounce) {
                            onMenuDsn(message, EntityMessage.DSN_HARD_BOUNCE);
                            return true;
                        } else if (itemId == R.id.menu_forward) {
                            onMenuReply(message, "forward");
                            return true;
                        } else if (itemId == R.id.menu_forward_raw) {
                            onActionRaw(message.id);
                            return true;
                        } else if (itemId == R.id.menu_resend) {
                            onMenuResend(message);
                            return true;
                        } else if (itemId == R.id.menu_editasnew) {
                            onMenuReply(message, "editasnew");
                            return true;
                        } else if (itemId == R.id.menu_new_message) {
                            onMenuNew(message, to);
                            return true;
                        } else if (itemId == R.id.menu_reply_answer) {
                            onMenuAnswer(message);
                            return true;
                        } else if (itemId == R.id.menu_move_to) {
                            onMenuMove(message);
                            return true;
                        } else if (itemId == R.id.menu_settings) {
                            onMenuAnswerSettings();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:reply");
    }

    private void onMenuReply(TupleMessageEx message, String action) {
        onMenuReply(message, action, null, null);
    }

    private void onMenuReply(TupleMessageEx message, String action, InternetAddress to, CharSequence selected) {
        final Context context = getContext();
        if (context == null)
            return;

        if (!"reply".equals(action) &&
                !"reply_all".equals(action) &&
                !"list".equals(action))
            selected = null;

        Intent reply = new Intent(context, ActivityCompose.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("action", action)
                .putExtra("reference", message.id)
                .putExtra("selected", selected);

        if (to != null)
            reply.putExtra("to", MessageHelper.formatAddressesCompose(new InternetAddress[]{to}));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean attachments_asked = prefs.getBoolean("attachments_asked", false);
        if (attachments_asked) {
            startActivity(reply);
            return;
        }

        if ("reply".equals(action) || "reply_all".equals(action) ||
                "forward".equals(action) ||
                "resend".equals(action) ||
                "editasnew".equals(action)) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<List<Long>>() {
                @Override
                protected List<Long> onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    List<Long> download = new ArrayList<>();

                    long size = 0;

                    DB db = DB.getInstance(context);
                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    if (attachments != null)
                        for (EntityAttachment attachment : attachments)
                            if (!attachment.available &&
                                    attachment.subsequence == null &&
                                    !attachment.isEncryption()) {
                                if (attachment.size != null)
                                    size += attachment.size;
                                download.add(attachment.id);
                            }

                    args.putLong("size", size);

                    return download;
                }

                @Override
                protected void onExecuted(Bundle args, List<Long> download) {
                    if (download.isEmpty()) {
                        startActivity(reply);
                        return;
                    }

                    args.putLongArray("download", Helper.toLongArray(download));
                    args.putParcelable("intent", reply);

                    FragmentDialogDownloadAttachments dialog = new FragmentDialogDownloadAttachments();
                    dialog.setArguments(args);
                    dialog.show(getParentFragmentManager(), "message:attachments");
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragment(), ex);
                }
            }.execute(this, args, "message:attachments");
        } else
            startActivity(reply);
    }

    private void onMenuResend(TupleMessageEx message) {
        if (message.headers == null) {
            iProperties.setValue("resend", message.id, true);

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    EntityOperation.queue(context, message, EntityOperation.HEADERS);

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    ToastEx.makeText(getContext(), R.string.title_fetching_headers, Toast.LENGTH_LONG).show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "resend:headers");
        } else
            onMenuReply(message, "resend");
    }

    private void onMenuDsn(TupleMessageEx message, int type) {
        Intent reply = new Intent(getContext(), ActivityCompose.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("action", "dsn")
                .putExtra("reference", message.id)
                .putExtra("dsn", type);
        startActivity(reply);
    }

    private void onMenuNew(TupleMessageEx message, Address[] to) {
        Intent reply = new Intent(getContext(), ActivityCompose.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("action", "new")
                .putExtra("identity", message.identity == null ? -1 : message.identity)
                .putExtra("to", MessageHelper.formatAddressesCompose(to));
        startActivity(reply);
    }

    private void onMenuAnswer(TupleMessageEx message) {
        new SimpleTask<List<EntityAnswer>>() {
            @Override
            protected List<EntityAnswer> onExecute(Context context, Bundle args) {
                return DB.getInstance(context).answer().getAnswersByFavorite(false);
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAnswer> answers) {
                final Context context = getContext();
                if (answers == null || answers.size() == 0) {
                    Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_no_answers, Snackbar.LENGTH_LONG));
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                            lbm.sendBroadcast(new Intent(ActivityView.ACTION_EDIT_ANSWERS));
                        }
                    });
                    snackbar.show();
                } else {
                    PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, getViewLifecycleOwner(), fabReply);
                    EntityAnswer.fillMenu(popupMenu.getMenu(), false, answers, context);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem target) {
                            Intent intent = target.getIntent();
                            if (intent == null)
                                return false;

                            if (!ActivityBilling.isPro(context)) {
                                startActivity(new Intent(context, ActivityBilling.class));
                                return true;
                            }

                            startActivity(new Intent(context, ActivityCompose.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("action", "reply")
                                    .putExtra("reference", message.id)
                                    .putExtra("answer", intent.getLongExtra("id", -1)));
                            return true;
                        }
                    });

                    popupMenu.show();
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(getContext(), getViewLifecycleOwner(), new Bundle(), "message:answer");
    }

    private boolean canMove(TupleMessageEx message) {
        boolean pop = (message.accountProtocol == EntityAccount.TYPE_POP);
        boolean move = !(message.folderReadOnly || message.uid == null) ||
                (pop && EntityFolder.TRASH.equals(message.folderType));
        return move;
    }

    private void onMenuMove(TupleMessageEx message) {
        if (message.accountProtocol == EntityAccount.TYPE_POP &&
                EntityFolder.TRASH.equals(message.folderType) && !message.accountLeaveDeleted) {
            Bundle args = new Bundle();
            args.putLong("id", message.account);

            new SimpleTask<EntityFolder>() {
                @Override
                protected EntityFolder onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    return db.folder().getFolderByType(id, EntityFolder.INBOX);
                }

                @Override
                protected void onExecuted(Bundle args, EntityFolder inbox) {
                    _onMenuMove(message, new long[]{message.folder, inbox == null ? -1L : inbox.id});
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "move:pop");
        } else
            _onMenuMove(message, new long[]{message.folder});
    }

    private void _onMenuMove(TupleMessageEx message, long[] disabled) {
        Bundle args = new Bundle();
        args.putInt("icon", R.drawable.twotone_drive_file_move_24);
        args.putString("title", getString(R.string.title_move_to_folder));
        args.putLong("account", message.account);
        args.putLongArray("disabled", disabled);
        args.putLong("message", message.id);
        args.putBoolean("copy", false);
        args.putBoolean("cancopy", true);
        args.putBoolean("similar", false);

        FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_MESSAGE_MOVE);
        fragment.show(getParentFragmentManager(), "message:move");
    }

    private void onMenuAnswerSettings() {
        FragmentDialogAnswerButton fragment = new FragmentDialogAnswerButton();
        fragment.setTargetFragment(this, REQUEST_ANSWER_SETTINGS);
        fragment.show(getParentFragmentManager(), "dialog:answer");
    }

    private void onMore() {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());
        args.putBoolean("threading", threading);

        new SimpleTask<MoreResult>() {
            @Override
            protected void onPreExecute(Bundle args) {
                fabMore.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                fabMore.setEnabled(true);
            }

            @Override
            protected MoreResult onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean threading = args.getBoolean("threading");
                return MoreResult.get(context, ids, threading, false);
            }

            @Override
            protected void onExecuted(Bundle args, final MoreResult result) {
                long[] ids = args.getLongArray("ids");

                final Context context = getContext();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean flags = prefs.getBoolean("flags", true);

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, getViewLifecycleOwner(), fabMore);

                int order = 0;

                if (result.unseen) // Unseen, not draft
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_seen, order++, R.string.title_seen)
                            .setIcon(R.drawable.twotone_drafts_24);
                if (result.seen) // Seen, not draft
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unseen, order++, R.string.title_unseen)
                            .setIcon(R.drawable.twotone_mail_24);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_snooze, order++, R.string.title_snooze)
                        .setIcon(R.drawable.twotone_timelapse_24);

                if (result.visible && !result.isDrafts)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_hide, order++, R.string.title_hide)
                            .setIcon(R.drawable.twotone_visibility_off_24);
                if (result.hidden)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unhide, order++, R.string.title_unhide)
                            .setIcon(R.drawable.twotone_visibility_24);

                if (result.unflagged && flags)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag, order++, R.string.title_flag)
                            .setIcon(R.drawable.twotone_star_24);
                if (result.flagged && flags)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unflag, order++, R.string.title_unflag)
                            .setIcon(R.drawable.twotone_star_border_24);
                if ((result.unflagged || result.flagged) && flags)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag_color, order++, R.string.title_flag_color)
                            .setIcon(R.drawable.twotone_auto_awesome_24);

                SubMenu importance = popupMenu.getMenu()
                        .addSubMenu(Menu.NONE, Menu.NONE, order++, R.string.title_set_importance)
                        .setIcon(R.drawable.twotone_north_24);
                importance.add(Menu.NONE, R.string.title_importance_high, 1, R.string.title_importance_high)
                        .setIcon(R.drawable.twotone_north_24)
                        .setEnabled(!EntityMessage.PRIORITIY_HIGH.equals(result.importance));
                importance.add(Menu.NONE, R.string.title_importance_normal, 2, R.string.title_importance_normal)
                        .setIcon(R.drawable.twotone_horizontal_rule_24)
                        .setEnabled(!EntityMessage.PRIORITIY_NORMAL.equals(result.importance));
                importance.add(Menu.NONE, R.string.title_importance_low, 3, R.string.title_importance_low)
                        .setIcon(R.drawable.twotone_south_24)
                        .setEnabled(!EntityMessage.PRIORITIY_LOW.equals(result.importance));

                if (ids.length < MAX_SEND_RAW)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_raw_send, order++, R.string.title_raw_send)
                            .setIcon(R.drawable.twotone_attach_email_24);

                if (result.canInbox()) // not is inbox
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_folder_inbox, order++, R.string.title_folder_inbox)
                            .setIcon(R.drawable.twotone_move_to_inbox_24);

                if (result.canArchive()) // has archive and not is archive
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_archive, order++, R.string.title_archive)
                            .setIcon(R.drawable.twotone_archive_24);

                if (result.canJunk()) // has junk and not junk/drafts
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_spam, order++, R.string.title_spam)
                            .setIcon(R.drawable.twotone_report_24);

                if (result.canTrash()) // not trash and has trash and not is junk
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_trash, order++, R.string.title_trash)
                            .setIcon(R.drawable.twotone_delete_24);

                if (result.canDelete())
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_delete_permanently, order++, R.string.title_delete_permanently)
                            .setIcon(R.drawable.twotone_delete_forever_24);

                if (!result.read_only) {
                    for (EntityAccount account : result.imapAccounts) {
                        String title = getString(R.string.title_move_to_account, account.name);
                        SpannableString ssb = new SpannableString(title);
                        if (account.name != null && account.color != null) {
                            int i = title.indexOf(account.name);
                            int first = title.codePointAt(i);
                            int count = Character.charCount(first);
                            ssb.setSpan(new ForegroundColorSpan(account.color), i, i + count, 0);
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), i, i + count, 0);
                            ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_LARGE), i, i + count, 0);
                        }
                        //if (!result.accounts.get(account))
                        //    ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, ssb.length(), 0);
                        MenuItem item = popupMenu.getMenu().add(Menu.FIRST, R.string.title_move_to_account, order++, ssb)
                                .setIcon(R.drawable.twotone_drive_file_move_24);
                        item.setIntent(new Intent().putExtra("account", account.id));
                    }
                }

                if (result.copyto != null)
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_copy_to, order++, R.string.title_copy_to)
                            .setIcon(R.drawable.twotone_file_copy_24);

                if (!result.hasPop && result.hasImap)
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_manage_keywords, order++, R.string.title_manage_keywords)
                            .setIcon(R.drawable.twotone_label_important_24);

                if (ids.length == 1)
                    popupMenu.getMenu().add(Menu.FIRST, R.string.title_search_sender, order++, R.string.title_search_sender)
                            .setIcon(R.drawable.twotone_search_24);

                popupMenu.insertIcons(context);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        int itemId = target.getItemId();
                        if (itemId == R.string.title_seen) {
                            onActionSeenSelection(true, null, false);
                            return true;
                        } else if (itemId == R.string.title_unseen) {
                            onActionSeenSelection(false, null, false);
                            return true;
                        } else if (itemId == R.string.title_snooze) {
                            onActionSnoozeSelection();
                            return true;
                        } else if (itemId == R.string.title_hide) {
                            onHideSelection(true);
                            return true;
                        } else if (itemId == R.string.title_unhide) {
                            onHideSelection(false);
                            return true;
                        } else if (itemId == R.string.title_flag) {
                            onActionFlagSelection(true, Color.TRANSPARENT, null, false);
                            return true;
                        } else if (itemId == R.string.title_unflag) {
                            onActionFlagSelection(false, Color.TRANSPARENT, null, false);
                            return true;
                        } else if (itemId == R.string.title_flag_color) {
                            onActionFlagColorSelection(false);
                            return true;
                        } else if (itemId == R.string.title_importance_low) {
                            onActionSetImportanceSelection(EntityMessage.PRIORITIY_LOW, null, false);
                            return true;
                        } else if (itemId == R.string.title_importance_normal) {
                            onActionSetImportanceSelection(EntityMessage.PRIORITIY_NORMAL, null, false);
                            return true;
                        } else if (itemId == R.string.title_importance_high) {
                            onActionSetImportanceSelection(EntityMessage.PRIORITIY_HIGH, null, false);
                            return true;
                        } else if (itemId == R.string.title_raw_send) {
                            onActionRaw(null);
                            return true;
                        } else if (itemId == R.string.title_folder_inbox) {
                            onActionMoveSelection(EntityFolder.INBOX, false);
                            return true;
                        } else if (itemId == R.string.title_archive) {
                            onActionMoveSelection(EntityFolder.ARCHIVE, false);
                            return true;
                        } else if (itemId == R.string.title_spam) {
                            if (result.hasPop && !result.hasImap)
                                onActionBlockSender();
                            else if (!result.hasPop && result.hasImap)
                                onActionJunkSelection();
                            return true;
                        } else if (itemId == R.string.title_trash) {
                            onActionMoveSelection(EntityFolder.TRASH, false);
                            return true;
                        } else if (itemId == R.string.title_delete_permanently) {
                            onActionDeleteSelection();
                            return true;
                        } else if (itemId == R.string.title_move_to_account) {
                            long account = target.getIntent().getLongExtra("account", -1);
                            onActionMoveSelectionAccount(account, false, result.folders);
                            return true;
                        } else if (itemId == R.string.title_copy_to) {
                            onActionMoveSelectionAccount(result.copyto.id, true, result.folders);
                            return true;
                        } else if (itemId == R.string.title_manage_keywords) {
                            onActionManageKeywords(false);
                            return true;
                        } else if (itemId == R.string.title_search_sender) {
                            long[] ids = getSelection();
                            if (ids.length != 1)
                                return false;
                            searchContact(getContext(), getViewLifecycleOwner(), getParentFragmentManager(), ids[0], true);
                            return true;
                        }
                        return false;
                    }
                });

                MenuCompat.setGroupDividerEnabled(popupMenu.getMenu(), true);

                popupMenu.show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:more");
    }

    private long[] getSelection() {
        if (selectionTracker == null)
            return new long[0];

        Selection<Long> selection = selectionTracker.getSelection();

        long[] ids = new long[selection.size()];
        int i = 0;
        for (Long id : selection)
            ids[i++] = id;

        return ids;
    }

    private void onActionSeenSelection(boolean seen, Long id, boolean clear) {
        Bundle args = new Bundle();
        args.putLongArray("ids", id == null ? getSelection() : new long[]{id});
        args.putBoolean("seen", seen);
        args.putBoolean("threading", threading &&
                (id == null || viewType != AdapterMessage.ViewType.THREAD));

        if (clear && selectionTracker != null)
            selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean seen = args.getBoolean("seen");
                boolean threading = args.getBoolean("threading");

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

                ServiceSynchronize.eval(context, "seen");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:seen");
    }

    private void onActionHide(TupleMessageEx message) {
        Bundle args = new Bundle();
        args.putLong("account", message.account);
        args.putString("thread", message.thread);
        args.putLong("id", message.id);
        args.putLong("duration", message.ui_snoozed == null ? Long.MAX_VALUE : 0);
        args.putLong("time", message.ui_snoozed == null ? Long.MAX_VALUE : 0);
        args.putBoolean("hide", true);

        onSnoozeOrHide(args);
    }

    private void onActionSnoozeSelection() {
        Bundle args = new Bundle();
        args.putString("title", getString(R.string.title_snooze));

        FragmentDialogDuration fragment = new FragmentDialogDuration();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_MESSAGES_SNOOZE);
        fragment.show(getParentFragmentManager(), "messages:snooze");
    }

    private void onHideSelection(boolean hide) {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());
        args.putBoolean("hide", hide);

        if (selectionTracker != null)
            selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean hide = args.getBoolean("hide");

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
                            db.message().setMessageSnoozed(threaded.id, hide ? Long.MAX_VALUE : null);
                            db.message().setMessageUiIgnored(message.id, true);
                            EntityMessage.snooze(context, threaded.id, hide ? Long.MAX_VALUE : null);
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:flag");
    }

    private void onActionFlagSelection(boolean flagged, int color, Long id, boolean clear) {
        Bundle args = new Bundle();
        args.putLongArray("ids", id == null ? getSelection() : new long[]{id});
        args.putBoolean("flagged", flagged);
        args.putInt("color", color);
        args.putBoolean("threading", threading &&
                (id == null || viewType != AdapterMessage.ViewType.THREAD));

        if (clear && selectionTracker != null)
            selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean flagged = args.getBoolean("flagged");
                Integer color = args.getInt("color");
                boolean threading = args.getBoolean("threading");

                if (color == Color.TRANSPARENT)
                    color = null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, flagged ? message.folder : null);
                        for (EntityMessage threaded : messages)
                            if (threaded.ui_flagged != flagged || !Objects.equals(threaded.color, color))
                                EntityOperation.queue(context, threaded, EntityOperation.FLAG, flagged, color);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "flag");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:flag");
    }

    private void onActionFlagColorSelection(boolean clear) {
        Bundle args = new Bundle();
        args.putInt("color", Color.TRANSPARENT);
        args.putString("title", getString(R.string.title_flag_color));
        args.putBoolean("reset", true);
        args.putBoolean("clear", clear);
        args.putInt("faq", 187);

        FragmentDialogColor fragment = new FragmentDialogColor();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_COLOR);
        fragment.show(getParentFragmentManager(), "messages:color");
    }

    private void onActionSetImportanceSelection(int importance, Long id, boolean clear) {
        Bundle args = new Bundle();
        args.putLongArray("ids", id == null ? getSelection() : new long[]{id});
        args.putInt("importance", importance);

        if (clear && selectionTracker != null)
            selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                Integer importance = args.getInt("importance");
                if (EntityMessage.PRIORITIY_NORMAL.equals(importance))
                    importance = null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages) {
                            db.message().setMessageImportance(threaded.id, importance);

                            EntityOperation.queue(context, threaded, EntityOperation.KEYWORD,
                                    MessageHelper.FLAG_LOW_IMPORTANCE, EntityMessage.PRIORITIY_LOW.equals(importance));
                            EntityOperation.queue(context, threaded, EntityOperation.KEYWORD,
                                    MessageHelper.FLAG_HIGH_IMPORTANCE, EntityMessage.PRIORITIY_HIGH.equals(importance));
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:set:importance");
    }

    private void onActionRaw(Long id) {
        Bundle args = new Bundle();
        args.putLongArray("ids", id == null ? getSelection() : new long[]{id});
        args.putBoolean("threads", false);

        if (id == null && selectionTracker != null)
            selectionTracker.clearSelection();

        FragmentDialogForwardRaw ask = new FragmentDialogForwardRaw();
        ask.setArguments(args);
        ask.show(getParentFragmentManager(), "messages:raw");
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
                        if (message == null || message.ui_hide)
                            continue;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages)
                            if (!threaded.ui_hide &&
                                    (threaded.uid != null || account.protocol != EntityAccount.TYPE_IMAP))
                                ids.add(threaded.id);
                    }

                    if (ids.size() == 1) {
                        EntityMessage message = db.message().getMessage(ids.get(0));
                        args.putString("remark", message == null ? null : message.getRemark());
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
                aargs.putString("remark", args.getString("remark"));
                aargs.putString("accept", getString(R.string.title_ask_delete_accept));
                aargs.putInt("faq", 160);
                aargs.putLongArray("ids", Helper.toLongArray(ids));
                aargs.putBoolean("warning", true);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean delete_confirmation = prefs.getBoolean("delete_confirmation", true);

                if (delete_confirmation) {
                    FragmentDialogAsk ask = new FragmentDialogAsk();
                    ask.setArguments(aargs);
                    ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_DELETE);
                    ask.show(getParentFragmentManager(), "messages:delete");
                } else {
                    Intent data = new Intent();
                    data.putExtra("args", aargs);
                    onActivityResult(REQUEST_MESSAGES_DELETE, RESULT_OK, data);
                    return;
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:delete:ask");
    }

    private void onActionJunkSelection() {
        Bundle aargs = new Bundle();
        aargs.putInt("count", getSelection().length);

        FragmentDialogAskSpam ask = new FragmentDialogAskSpam();
        ask.setArguments(aargs);
        ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_JUNK);
        ask.show(getParentFragmentManager(), "messages:junk");
    }

    private void onActionBlockSender() {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());

        FragmentDialogBlockSender ask = new FragmentDialogBlockSender();
        ask.setArguments(args);
        ask.setTargetFragment(FragmentMessages.this, REQUEST_BLOCK_SENDERS);
        ask.show(getParentFragmentManager(), "messages:block");
    }

    private void onActionMoveSelection(final String type, boolean block) {
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putBoolean("block", block);
        args.putLongArray("ids", getSelection());
        args.putBoolean("move_thread_all", move_thread_all);
        args.putBoolean("move_thread_sent", move_thread_sent);
        args.putBoolean("filter_archive", filter_archive);

        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                String type = args.getString("type");
                boolean block = args.getBoolean("block");
                long[] ids = args.getLongArray("ids");
                boolean move_thread_all = args.getBoolean("move_thread_all");
                boolean move_thread_sent = args.getBoolean("move_thread_sent");
                boolean filter_archive = args.getBoolean("filter_archive");

                ArrayList<MessageTarget> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            continue;

                        EntityFolder targetFolder = db.folder().getFolderByType(message.account, type);
                        if (targetFolder == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread,
                                threading ? null : id,
                                move_thread_all || move_thread_sent ? null : message.folder);
                        for (EntityMessage threaded : messages) {
                            EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                            if (sourceFolder == null ||
                                    sourceFolder.read_only ||
                                    sourceFolder.id.equals(targetFolder.id))
                                continue;

                            if (!threaded.folder.equals(message.folder) &&
                                    !(move_thread_all ||
                                            (move_thread_sent && EntityFolder.SENT.equals(sourceFolder.type))))
                                continue;

                            if (EntityFolder.TRASH.equals(targetFolder.type)) {
                                if (EntityFolder.ARCHIVE.equals(sourceFolder.type) && filter_archive)
                                    continue;
                                if (EntityFolder.JUNK.equals(sourceFolder.type) && !threaded.folder.equals(message.folder))
                                    continue;
                            }

                            result.add(new MessageTarget(context, threaded, account, sourceFolder, account, targetFolder)
                                    .setBlock(block));
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
                    moveAsk(result, true);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:move");
    }

    private void onActionMoveSelectionAccount(long account, boolean copy, List<Long> disabled) {
        Bundle args = new Bundle();
        args.putInt("icon", copy ? R.drawable.twotone_file_copy_24 : R.drawable.twotone_drive_file_move_24);
        args.putString("title", getString(copy ? R.string.title_copy_to : R.string.title_move_to_folder));
        args.putLong("account", account);
        args.putBoolean("copy", copy);
        args.putBoolean("cancopy", true);
        args.putLongArray("disabled", Helper.toLongArray(disabled));
        args.putLongArray("messages", getSelection());
        args.putBoolean("move_thread_all", move_thread_all);
        args.putBoolean("move_thread_sent", move_thread_sent);
        args.putBoolean("filter_archive", filter_archive);

        FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_MOVE);
        fragment.show(getParentFragmentManager(), "messages:move");
    }

    private void onActionManageKeywords(boolean clear) {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());

        FragmentDialogKeywordManage fragment = new FragmentDialogKeywordManage();
        fragment.setArguments(args);
        if (clear)
            fragment.setTargetFragment(FragmentMessages.this, REQUEST_DESELECT);
        fragment.show(getParentFragmentManager(), "keyword:manage");
    }

    private void onActionMoveSelection(Bundle args) {
        args.putLongArray("ids", getSelection());

        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                long tid = args.getLong("folder");
                boolean copy = args.getBoolean("copy");
                boolean move_thread_all = args.getBoolean("move_thread_all");
                boolean move_thread_sent = args.getBoolean("move_thread_sent");
                boolean filter_archive = args.getBoolean("filter_archive");

                ArrayList<MessageTarget> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder targetFolder = db.folder().getFolder(tid);
                    if (targetFolder == null)
                        return result;

                    EntityAccount targetAccount = db.account().getAccount(targetFolder.account);
                    if (targetAccount == null)
                        return result;

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        EntityAccount sourceAccount = db.account().getAccount(message.account);
                        if (sourceAccount == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id,
                                move_thread_all || move_thread_sent ? null : message.folder);
                        for (EntityMessage threaded : messages) {
                            EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                            if (sourceFolder == null ||
                                    sourceFolder.read_only ||
                                    sourceFolder.id.equals(targetFolder.id))
                                continue;

                            if (!threaded.folder.equals(message.folder) &&
                                    !(move_thread_all ||
                                            (move_thread_sent && EntityFolder.SENT.equals(sourceFolder.type))))
                                continue;

                            if (EntityFolder.TRASH.equals(targetFolder.type)) {
                                if (EntityFolder.ARCHIVE.equals(sourceFolder.type) && filter_archive)
                                    continue;
                                if (EntityFolder.JUNK.equals(sourceFolder.type) && !threaded.folder.equals(message.folder))
                                    continue;
                            }

                            result.add(new MessageTarget(context, threaded, sourceAccount, sourceFolder, targetAccount, targetFolder).setCopy(copy));
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
                boolean copy = args.getBoolean("copy");
                if (copy) {
                    moveAskConfirmed(result);
                    ToastEx.makeText(getContext(), R.string.title_copy, Toast.LENGTH_LONG).show();
                } else
                    moveAsk(result, true);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:move");
    }

    private void onActionMoveThread(Bundle args) {
        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");
                String thread = args.getString("thread");
                long id = args.getLong("id");
                boolean move_thread_sent = args.getBoolean("move_thread_sent");
                boolean filter_archive = args.getBoolean("filter_archive");
                long tid = args.getLong("folder");

                ArrayList<MessageTarget> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityAccount account = db.account().getAccount(aid);
                    if (account == null)
                        return result;

                    EntityFolder targetFolder = db.folder().getFolder(tid);
                    if (targetFolder == null)
                        return result;

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            aid, thread, threading ? null : id, null);
                    for (EntityMessage threaded : messages) {
                        EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                        if (sourceFolder != null && !sourceFolder.read_only &&
                                !targetFolder.id.equals(threaded.folder) &&
                                (!EntityFolder.isOutgoing(sourceFolder.type) ||
                                        (EntityFolder.SENT.equals(sourceFolder.type) && move_thread_sent)) &&
                                (!filter_archive || !EntityFolder.ARCHIVE.equals(sourceFolder.type)))
                            result.add(new MessageTarget(context, threaded, account, sourceFolder, account, targetFolder));
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                moveAskConfirmed(result);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:move");
    }

    private void onActionDelete() {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putString("thread", thread);
        args.putLong("id", id);
        args.putBoolean("filter_archive", filter_archive);

        new SimpleTask<List<Long>>() {
            @Override
            protected List<Long> onExecute(Context context, Bundle args) throws Throwable {
                long aid = args.getLong("account");
                String thread = args.getString("thread");
                long id = args.getLong("id");
                boolean filter_archive = args.getBoolean("filter_archive");

                ArrayList<Long> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityAccount account = db.account().getAccount(aid);
                    if (account != null &&
                            account.protocol == EntityAccount.TYPE_POP &&
                            account.leave_deleted)
                        args.putBoolean("leave_deleted", true);

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            aid, thread, threading ? null : id, null);
                    for (EntityMessage threaded : messages) {
                        EntityFolder folder = db.folder().getFolder(threaded.folder);
                        if (!folder.read_only &&
                                (!filter_archive || !EntityFolder.ARCHIVE.equals(folder.type)) &&
                                !EntityFolder.DRAFTS.equals(folder.type) &&
                                !EntityFolder.OUTBOX.equals(folder.type)
                            /* sent, trash, junk */)
                            result.add(threaded.id);
                    }

                    if (result.size() == 1) {
                        EntityMessage message = db.message().getMessage(result.get(0));
                        args.putString("remark", message == null ? null : message.getRemark());
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, List<Long> ids) {
                boolean leave_deleted = args.getBoolean("leave_deleted");

                Bundle aargs = new Bundle();
                if (leave_deleted) {
                    aargs.putString("question", getResources()
                            .getQuantityString(R.plurals.title_moving_messages, ids.size(), ids.size()));
                    aargs.putString("notagain", "delete_asked");
                } else {
                    aargs.putString("question", getResources()
                            .getQuantityString(R.plurals.title_deleting_messages, ids.size(), ids.size()));
                    aargs.putString("remark", args.getString("remark"));
                    aargs.putString("accept", getString(R.string.title_ask_delete_accept));
                }
                aargs.putInt("faq", 160);
                aargs.putLongArray("ids", Helper.toLongArray(ids));
                aargs.putBoolean("warning", true);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean delete_asked = prefs.getBoolean("delete_asked", false);
                boolean delete_confirmation = prefs.getBoolean("delete_confirmation", true);

                if (leave_deleted ? delete_asked : !delete_confirmation) {
                    Intent data = new Intent();
                    data.putExtra("args", aargs);
                    onActivityResult(REQUEST_MESSAGES_DELETE, RESULT_OK, data);
                    return;
                }

                FragmentDialogAsk ask = new FragmentDialogAsk();
                ask.setArguments(aargs);
                ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_DELETE);
                ask.show(getParentFragmentManager(), "messages:delete");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:delete");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:reset", reset);
        outState.putBoolean("fair:autoExpanded", autoExpanded);
        outState.putInt("fair:autoCloseCount", autoCloseCount);
        outState.putInt("fair:lastSentCount", lastSentCount);

        outState.putStringArray("fair:values", values.keySet().toArray(new String[0]));
        for (String name : values.keySet())
            outState.putLongArray("fair:name:" + name, Helper.toLongArray(values.get(name)));

        if (rvMessage != null) {
            Parcelable rv = rvMessage.getLayoutManager().onSaveInstanceState();
            outState.putParcelable("fair:rv", rv);
        }

        values.remove("ready");

        if (selectionTracker != null)
            selectionTracker.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            reset = savedInstanceState.getBoolean("fair:reset");
            autoExpanded = savedInstanceState.getBoolean("fair:autoExpanded");
            autoCloseCount = savedInstanceState.getInt("fair:autoCloseCount");
            lastSentCount = savedInstanceState.getInt("fair:lastSentCount");

            for (String name : savedInstanceState.getStringArray("fair:values"))
                if (!"selected".equals(name)) {
                    values.put(name, new ArrayList<>());
                    for (Long value : savedInstanceState.getLongArray("fair:name:" + name))
                        values.get(name).add(value);
                }

            if (rvMessage != null) {
                Parcelable rv = savedInstanceState.getParcelable("fair:rv");
                rvMessage.getLayoutManager().onRestoreInstanceState(rv);
            }

            if (selectionTracker != null)
                selectionTracker.onRestoreInstanceState(savedInstanceState);
        }

        boolean hints = (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER);
        boolean outbox = EntityFolder.OUTBOX.equals(type);
        boolean junk = EntityFolder.JUNK.equals(type);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean app_support = prefs.getBoolean("app_support", false);
        boolean message_swipe = prefs.getBoolean("message_swipe", false);
        boolean message_outbox = prefs.getBoolean("message_outbox", false);
        boolean message_select = prefs.getBoolean("message_select", false);
        boolean message_junk = prefs.getBoolean("message_junk", false);
        boolean motd = prefs.getBoolean("motd", false);
        boolean send_pending = prefs.getBoolean("send_pending", true);

        grpHintSupport.setVisibility(app_support || !hints || junk ? View.GONE : View.VISIBLE);
        grpHintSwipe.setVisibility(message_swipe || !hints || junk ? View.GONE : View.VISIBLE);
        grpHintOutbox.setVisibility(message_outbox || !hints || !outbox ? View.GONE : View.VISIBLE);
        grpHintSelect.setVisibility(message_select || !hints || junk ? View.GONE : View.VISIBLE);
        grpHintJunk.setVisibility(message_junk || !junk ? View.GONE : View.VISIBLE);
        grpMotd.setVisibility(motd ? View.VISIBLE : View.GONE);

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
                    invalidateOptionsMenu();
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

        loadMessages(false);

        updateExpanded();
        updateMore();

        // Folder
        switch (viewType) {
            case UNIFIED:
                db.folder().liveUnified(type, category).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                    @Override
                    public void onChanged(List<TupleFolderEx> folders) {
                        updateState(folders);
                    }
                });
                break;

            case FOLDER:
                db.folder().liveFolderEx(folder).observe(getViewLifecycleOwner(), new Observer<TupleFolderEx>() {
                    @Override
                    public void onChanged(@Nullable TupleFolderEx folder) {
                        List<TupleFolderEx> folders = new ArrayList<>();
                        if (folder != null) {
                            lastSync = folder.last_sync;
                            folders.add(folder);
                        }

                        updateState(folders);
                    }
                });
                break;

            case THREAD:
                db.message().liveThreadStats(account, thread, null, filter_archive).observe(getViewLifecycleOwner(), new Observer<TupleThreadStats>() {
                    @Override
                    public void onChanged(TupleThreadStats stats) {
                        if (stats == null)
                            return;

                        int unseen = stats.count - stats.seen;
                        if (unseen == 0)
                            setSubtitle(stats.accountName);
                        else
                            setSubtitle(getString(R.string.title_name_count, stats.accountName, NF.format(unseen)));
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
                                sizes.remove(id);
                                heights.remove(id);
                                positions.remove(id);
                                attachments.remove(id);
                            }
                        }
                    }
                });
                break;

            case SEARCH:
                setSubtitle(criteria.getTitle(getContext()));
                if (server) {
                    tvNoEmailHint.setText(null);
                    tvNoEmailHint.setCompoundDrawables(null, null, null, null);
                    db.folder().liveFolderEx(folder).observe(getViewLifecycleOwner(), new Observer<TupleFolderEx>() {
                        @Override
                        public void onChanged(TupleFolderEx folder) {
                            if (folder != null) {
                                tvNoEmailHint.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.twotone_search_24, 0, 0, 0);
                                tvNoEmailHint.setText(folder.accountName + "/" + folder.name);
                            }
                        }
                    });
                }
                break;
        }

        if (!EntityFolder.OUTBOX.equals(type) &&
                (viewType == AdapterMessage.ViewType.UNIFIED ||
                        viewType == AdapterMessage.ViewType.FOLDER))
            db.message().liveOutboxPending().observe(getViewLifecycleOwner(), new Observer<TupleOutboxStats>() {
                @Override
                public void onChanged(TupleOutboxStats stats) {
                    int pending = (stats == null || stats.pending == null ? 0 : stats.pending);
                    int errors = (stats == null || stats.errors == null ? 0 : stats.errors);

                    int count = (send_pending ? pending : errors);
                    if (count > 10)
                        tvOutboxCount.setText("+");
                    else
                        tvOutboxCount.setText(count == 0 ? null : NF.format(count));

                    int color = (errors == 0 ? colorAccent : colorWarning);
                    ibOutbox.setImageTintList(ColorStateList.valueOf(color));
                    tvOutboxCount.setTextColor(color);
                    ibOutbox.setAlpha(errors == 0 ? 0.4f : 1.0f);
                    tvOutboxCount.setAlpha(errors == 0 ? 0.4f : 1.0f);

                    grpOutbox.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                }
            });
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_STORE_RAW);
        iff.addAction(ACTION_VERIFYDECRYPT);
        iff.addAction(ACTION_KEYWORDS);
        lbm.registerReceiver(receiver, iff);

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);
        /*
            android.net.ConnectivityManager$TooManyRequestsException
                at android.net.ConnectivityManager.convertServiceException(ConnectivityManager.java:3771)
                at android.net.ConnectivityManager.sendRequestForNetwork(ConnectivityManager.java:3960)
                at android.net.ConnectivityManager.sendRequestForNetwork(ConnectivityManager.java:3967)
                at android.net.ConnectivityManager.registerNetworkCallback(ConnectivityManager.java:4349)
                at android.net.ConnectivityManager.registerNetworkCallback(ConnectivityManager.java:4319)
                at eu.faircode.email.FragmentMessages.onResume(SourceFile:69)
         */

        updateAirplaneMode(ConnectionHelper.airplaneMode(context));
        ContextCompat.registerReceiver(context,
                airplanemode,
                new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED);

        boolean isIgnoring = !Boolean.FALSE.equals(Helper.isIgnoringOptimizations(context));
        //boolean canSchedule = AlarmManagerCompatEx.canScheduleExactAlarms(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        boolean reminder = prefs.getBoolean("setup_reminder", true);
        boolean was_ignoring = prefs.getBoolean("was_ignoring", false);
        boolean targeting = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);
        grpBatteryOptimizations.setVisibility(
                !isIgnoring && enabled && reminder && !was_ignoring && targeting ? View.VISIBLE : View.GONE);

        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        adapter.setCompact(compact);
        adapter.setZoom(zoom);

        updateListState("Resume", SimpleTask.getCount(), adapter.getItemCount());

        if (true || !checkRedmiNote())
            if (true || !checkDoze())
                if (!checkReporting())
                    if (!checkReview())
                        if (!checkFingerprint())
                            if (!checkGmail())
                                if (!checkOutlook())
                                    ;

        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "notifications_reminder");
        onSharedPreferenceChanged(prefs, "datasaver_reminder");
        onSharedPreferenceChanged(prefs, "pro");

        if (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER) {
            boolean notify_clear = prefs.getBoolean("notify_clear", false);

            Bundle args = new Bundle();
            args.putLong("folder", folder);
            args.putString("type", type);
            args.putBoolean("notify_clear", notify_clear);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long folder = args.getLong("folder");
                    String type = args.getString("type");
                    boolean notify_clear = args.getBoolean("notify_clear");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        if (folder < 0) {
                            List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
                            if (accounts != null)
                                for (EntityAccount account : accounts) {
                                    if (notify_clear)
                                        db.message().ignoreAll(account.id, null, type);
                                    db.folder().setFolderLastView(account.id, null, type, new Date().getTime());
                                }
                        } else {
                            if (notify_clear)
                                db.message().ignoreAll(null, folder, type);
                            db.folder().setFolderLastView(null, folder, type, new Date().getTime());
                        }

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
            }.execute(this, args, "messages:ignore");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        context.unregisterReceiver(airplanemode);

        ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
        cm.unregisterNetworkCallback(networkCallback);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (grpNotifications != null && "notifications_reminder".equals(key)) {
            boolean canNotify =
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            hasPermission(Manifest.permission.POST_NOTIFICATIONS));
            if (canNotify)
                prefs.edit().remove("notifications_reminder").apply();
            boolean notifications_reminder = prefs.getBoolean("notifications_reminder", true);
            grpNotifications.setVisibility(
                    !canNotify && notifications_reminder
                            ? View.VISIBLE : View.GONE);
        }

        if (grpDataSaver != null &&
                ("enabled".equals(key) || "datasaver_reminder".equals(key))) {
            boolean isDataSaving = ConnectionHelper.isDataSaving(getContext());
            if (!isDataSaving)
                prefs.edit().remove("datasaver_reminder").apply();

            boolean enabled = prefs.getBoolean("enabled", true);
            boolean datasaver_reminder = prefs.getBoolean("datasaver_reminder", true);
            grpDataSaver.setVisibility(
                    isDataSaving && enabled && datasaver_reminder
                            ? View.VISIBLE : View.GONE);
        }

        if (grpVpnActive != null && "vpn_reminder".equals(key))
            updateVPN();

        if (grpSupport != null &&
                ("pro".equals(key) || "banner_hidden".equals(key))) {
            boolean pro = ActivityBilling.isPro(getContext());
            long banner_hidden = prefs.getLong("banner_hidden", 0);
            grpSupport.setVisibility(
                    !pro && banner_hidden == 0 && viewType == AdapterMessage.ViewType.UNIFIED
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
            getMainHandler().post(new RunnableEx("messages:network") {
                @Override
                public void delegate() {
                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;
                    if (!rvMessage.isComputingLayout())
                        adapter.checkInternet();
                    updateAirplaneMode(ConnectionHelper.airplaneMode(getContext()));

                    updateVPN();
                }
            });
        }
    };

    private BroadcastReceiver airplanemode = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean on = intent.getBooleanExtra("state", false);
            updateAirplaneMode(on);
        }
    };

    private void updateAirplaneMode(boolean on) {
        on = on && !ConnectionHelper.getNetworkState(getContext()).isConnected();
        grpAirplane.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void updateVPN() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean vpn_reminder = prefs.getBoolean("vpn_reminder", true);
        grpVpnActive.setVisibility(vpn_reminder && ConnectionHelper.vpnActive(getContext())
                ? View.VISIBLE : View.GONE);
    }

    private boolean checkRedmiNote() {
        if (!Helper.isRedmiNote())
            return false;

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean redmi_note = prefs.getBoolean("redmi_note", false);
        if (!redmi_note)
            return false;

        final Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, R.string.app_data_loss, Snackbar.LENGTH_INDEFINITE));
        snackbar.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("redmi_note", false).apply();
                Helper.view(v.getContext(), Uri.parse("https://github.com/M66B/FairEmail/blob/master/FAQ.md#redmi"), false);
            }
        });
        snackbar.show();

        return true;
    }

    private boolean checkDoze() {
        if (viewType != AdapterMessage.ViewType.UNIFIED)
            return false;

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean setup_reminder = prefs.getBoolean("setup_reminder", true);
        if (!setup_reminder)
            return false;

        if (!Helper.isAndroid12())
            return false;
        if (!Boolean.FALSE.equals(Helper.isIgnoringOptimizations(context)))
            return false;
        if (AlarmManagerCompatEx.canScheduleExactAlarms(context))
            return false;

        final Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view,
                R.string.title_setup_alarm_12,
                Snackbar.LENGTH_INDEFINITE));
        snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
        snackbar.show();

        return true;
    }

    private boolean checkReporting() {
        if (viewType != AdapterMessage.ViewType.UNIFIED)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("crash_reports", false) ||
                prefs.getBoolean("crash_reports_asked", false))
            return false;

        final Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_ask_help, Snackbar.LENGTH_INDEFINITE));
        snackbar.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                new FragmentDialogReporting().show(getParentFragmentManager(), "reporting");
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                prefs.edit().putBoolean("crash_reports_asked", true).apply();
            }
        });
        snackbar.show();

        return true;
    }

    private boolean checkReview() {
        if (viewType != AdapterMessage.ViewType.UNIFIED)
            return false;

        if (!Helper.isPlayStoreInstall() &&
                !(Helper.hasPlayStore(getContext()) &&
                        (Helper.hasValidFingerprint(getContext()) || BuildConfig.DEBUG)))
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("review_asked", false))
            return false;

        long now = new Date().getTime();
        long later = prefs.getLong("review_later", -1);
        if (later < 0) {
            long installed = Helper.getInstallTime(getContext());
            Log.i("Review installed=" + new Date(installed));

            if (installed + REVIEW_ASK_DELAY > now)
                return false;
        } else {
            Log.i("Review later=" + new Date(later));

            if (later + REVIEW_LATER_DELAY > now)
                return false;
        }

        final Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_ask_review, Snackbar.LENGTH_INDEFINITE));
        snackbar.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                new FragmentDialogReview().show(getParentFragmentManager(), "review");
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                prefs.edit().putBoolean("review_asked", true).apply();
            }
        });
        snackbar.show();

        return true;
    }

    private boolean checkFingerprint() {
        if (Helper.hasValidFingerprint(getContext()))
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("third_party_notified", false))
            return false;

        final Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_third_party, Snackbar.LENGTH_INDEFINITE));
        snackbar.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                Helper.viewFAQ(v.getContext(), 147);
                prefs.edit().putBoolean("third_party_notified", true).apply();
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                prefs.edit().putBoolean("third_party_notified", true).apply();
            }
        });
        snackbar.show();

        return true;
    }

    private boolean checkGmail() {
        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("gmail_checked", false))
            return false;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_MONTH, 30);
        cal.set(Calendar.MONTH, Calendar.MAY);
        cal.set(Calendar.YEAR, 2022);

        long now = new Date().getTime();
        if (now < cal.getTimeInMillis() - 30 * 24 * 3600 * 1000L)
            return false; // Not yet

        if (Helper.getInstallTime(context) > cal.getTimeInMillis()) {
            prefs.edit().putBoolean("gmail_checked", true).apply();
            return false;
        }

        cal.add(Calendar.MONTH, 2);

        if (now > cal.getTimeInMillis()) {
            prefs.edit().putBoolean("gmail_checked", true).apply();
            return false;
        }

        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) throws Throwable {
                DB db = DB.getInstance(context);
                return db.account().getAccounts();
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                int oauth = 0;
                int passwd = 0;
                if (accounts != null)
                    for (EntityAccount account : accounts)
                        if (account.isGmail())
                            if (account.auth_type == ServiceAuthenticator.AUTH_TYPE_GMAIL)
                                oauth++;
                            else if (account.auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD)
                                passwd++;

                if (oauth + passwd == 0) {
                    prefs.edit().putBoolean("gmail_checked", true).apply();
                    return;
                }

                final int resid = (passwd > 0
                        ? R.string.title_check_gmail_password
                        : R.string.title_check_gmail_oauth);
                final Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, resid, Snackbar.LENGTH_INDEFINITE));
                snackbar.setAction(R.string.title_info, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        Helper.viewFAQ(v.getContext(), 6);
                        prefs.edit().putBoolean("gmail_checked", true).apply();
                    }
                });
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        prefs.edit().putBoolean("gmail_checked", true).apply();
                    }
                });
                snackbar.show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.e(ex);
            }
        }.execute(this, new Bundle(), "gmail:check");

        return true;
    }

    private boolean checkOutlook() {
        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long outlook_last_checked = prefs.getLong("outlook_last_checked", 0);
        boolean outlook_checked = prefs.getBoolean("outlook_checked", false);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.DAY_OF_MONTH, 16);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.YEAR, 2024);
        long at = cal.getTimeInMillis();

        long now = new Date().getTime();

        int days;
        if (now > at - 3 * 24 * 3600 * 1000L)
            days = 1;
        else if (now > at - 15 * 24 * 3600 * 1000L)
            days = 3;
        else if (now > at - 30 * 24 * 3600 * 1000L)
            days = 7;
        else
            days = 14;

        if (outlook_last_checked + days * 24 * 3600 * 1000L > now)
            return false;

        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) throws Throwable {
                DB db = DB.getInstance(context);
                if (BuildConfig.DEBUG)
                    return db.account().getAccounts();
                return db.account().getSynchronizingAccounts(null);
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                int oauth = 0;
                int passwd = 0;
                if (accounts != null)
                    for (EntityAccount account : accounts)
                        if (account.isOutlook())
                            if (account.auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH)
                                oauth++;
                            else if (account.auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD)
                                passwd++;

                if (oauth + passwd == 0)
                    return;

                if (oauth > 0 && passwd == 0 && outlook_checked)
                    return;

                boolean checked = (passwd == 0);
                int resid = (checked ? R.string.title_check_outlook_oauth : R.string.title_check_outlook_password);
                final Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, resid, Snackbar.LENGTH_INDEFINITE));
                Helper.setSnackbarLines(snackbar, 5);
                snackbar.setAction(checked ? android.R.string.ok : R.string.title_info, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        prefs.edit().putBoolean("outlook_checked", true).apply();
                        if (!checked) {
                            prefs.edit().putLong("outlook_last_checked", now).apply();
                            Helper.viewFAQ(v.getContext(), 14);
                        }
                    }
                });
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        prefs.edit().putBoolean("outlook_checked", true).apply();
                        if (!checked)
                            prefs.edit().putLong("outlook_last_checked", now).apply();
                    }
                });
                snackbar.show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.e(ex);
            }
        }.execute(this, new Bundle(), "outlook:check");

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            inflater.inflate(R.menu.menu_messages, menu);

            final Context context = getContext();
            PopupMenuLifecycle.insertIcons(context, menu, false);

            ActionBar actionBar = getSupportActionBar();
            Context actionBarContext = (actionBar == null ? context : actionBar.getThemedContext());
            LayoutInflater infl = LayoutInflater.from(actionBarContext);

            ImageButton ib = (ImageButton) infl.inflate(R.layout.action_button, null);
            ib.setId(View.generateViewId());
            ib.setImageResource(R.drawable.twotone_folder_24);
            ib.setContentDescription(getString(R.string.title_legend_section_folders));
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMenuFolders(primary);
                }
            });
            ib.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
                    fragment.setArguments(new Bundle());
                    fragment.setTargetFragment(FragmentMessages.this, REQUEST_ACCOUNT);
                    fragment.show(getParentFragmentManager(), "messages:accounts");
                    return true;
                }
            });
            menu.findItem(R.id.menu_folders).setActionView(ib);

            MenuCompat.setGroupDividerEnabled(menu, true);

            super.onCreateOptionsMenu(menu, inflater);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        try {
            boolean drafts = EntityFolder.DRAFTS.equals(type);
            boolean outbox = EntityFolder.OUTBOX.equals(type);
            boolean sent = EntityFolder.SENT.equals(type);

            final Context context = getContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean primary_inbox = "inbox".equals(prefs.getString("startup", "unified"));
            String sort = prefs.getString(getSort(context, viewType, type), "time");
            boolean ascending = prefs.getBoolean(getSortOrder(context, viewType, type), outbox);
            boolean filter_seen = prefs.getBoolean(getFilter(context, "seen", viewType, type), false);
            boolean filter_unflagged = prefs.getBoolean(getFilter(context, "unflagged", viewType, type), false);
            boolean filter_unknown = prefs.getBoolean(getFilter(context, "unknown", viewType, type), false);
            boolean filter_snoozed = prefs.getBoolean(getFilter(context, "snoozed", viewType, type), true);
            boolean filter_deleted = prefs.getBoolean(getFilter(context, "deleted", viewType, type), false);
            boolean filter_duplicates = prefs.getBoolean("filter_duplicates", true);
            boolean filter_trash = prefs.getBoolean("filter_trash", false);
            boolean language_detection = prefs.getBoolean("language_detection", false);
            String filter_language = prefs.getString("filter_language", null);
            boolean perform_expunge = prefs.getBoolean("perform_expunge", true);
            boolean large_buttons = prefs.getBoolean("large_buttons", false);
            boolean compact = prefs.getBoolean("compact", false);
            boolean confirm_links = prefs.getBoolean("confirm_links", true);
            int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
            int padding = prefs.getInt("view_padding", compact || !cards ? 0 : 1);
            boolean quick_filter = prefs.getBoolean("quick_filter", false);

            boolean folder =
                    (viewType == AdapterMessage.ViewType.UNIFIED ||
                            (viewType == AdapterMessage.ViewType.FOLDER && !outbox));

            boolean filter_active = (filter_seen || filter_unflagged || filter_unknown ||
                    (language_detection && !TextUtils.isEmpty(filter_language)));
            int filterColor = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
            float filterLighten = 0.7f - (float) ColorUtils.calculateLuminance(filterColor);
            if (filterLighten > 0)
                filterColor = ColorUtils.blendARGB(filterColor, Color.WHITE, filterLighten);
            MenuItem menuFilter = menu.findItem(R.id.menu_filter);
            menuFilter.setShowAsAction(folder && filter_active
                    ? MenuItem.SHOW_AS_ACTION_ALWAYS
                    : MenuItem.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setIconTintList(menuFilter, folder && filter_active
                    ? ColorStateList.valueOf(filterColor) : null);
            menuFilter.setIcon(folder && filter_active ? R.drawable.twotone_filter_alt_24 : R.drawable.twotone_filter_list_24);

            MenuItem menuSearch = menu.findItem(R.id.menu_search);
            menuSearch.setVisible(folder);

            menu.findItem(R.id.menu_save_search).setVisible(
                    viewType == AdapterMessage.ViewType.SEARCH &&
                            criteria != null && criteria.id == null);
            menu.findItem(R.id.menu_edit_search).setVisible(
                    viewType == AdapterMessage.ViewType.SEARCH &&
                            criteria != null && criteria.id != null);

            menu.findItem(R.id.menu_folders).setVisible(
                    viewType == AdapterMessage.ViewType.UNIFIED &&
                            type == null && primary >= 0);
            ImageButton ib = (ImageButton) menu.findItem(R.id.menu_folders).getActionView();
            ib.setImageResource(connected
                    ? R.drawable.twotone_folder_special_24 : R.drawable.twotone_folder_24);

            menu.findItem(R.id.menu_sort_on).setVisible(viewType != AdapterMessage.ViewType.SEARCH);

            if (viewType == AdapterMessage.ViewType.THREAD) {
                menu.findItem(R.id.menu_sort_on_time).setVisible(false);
                menu.findItem(R.id.menu_sort_on_unread).setVisible(false);
                menu.findItem(R.id.menu_sort_on_priority).setVisible(false);
                menu.findItem(R.id.menu_sort_on_starred).setVisible(false);
                menu.findItem(R.id.menu_sort_on_unread_starred).setVisible(false);
                menu.findItem(R.id.menu_sort_on_starred_unread).setVisible(false);
                menu.findItem(R.id.menu_sort_on_sender).setVisible(false);
                menu.findItem(R.id.menu_sort_on_subject).setVisible(false);
                menu.findItem(R.id.menu_sort_on_size).setVisible(false);
                menu.findItem(R.id.menu_sort_on_attachments).setVisible(false);
                menu.findItem(R.id.menu_sort_on_snoozed).setVisible(false);
            }

            boolean unselected = (selectionTracker == null || !selectionTracker.hasSelection());
            menu.findItem(R.id.menu_empty_trash).setVisible(EntityFolder.TRASH.equals(type) && folder && unselected);
            menu.findItem(R.id.menu_empty_spam).setVisible(EntityFolder.JUNK.equals(type) && folder && unselected);

            if ("time".equals(sort))
                menu.findItem(R.id.menu_sort_on_time).setChecked(true);
            else if ("unread".equals(sort))
                menu.findItem(R.id.menu_sort_on_unread).setChecked(true);
            else if ("starred".equals(sort))
                menu.findItem(R.id.menu_sort_on_starred).setChecked(true);
            else if ("unread+starred".equals(sort))
                menu.findItem(R.id.menu_sort_on_unread_starred).setChecked(true);
            else if ("starred+unread".equals(sort))
                menu.findItem(R.id.menu_sort_on_starred_unread).setChecked(true);
            else if ("priority".equals(sort))
                menu.findItem(R.id.menu_sort_on_priority).setChecked(true);
            else if ("sender".equals(sort))
                menu.findItem(R.id.menu_sort_on_sender).setChecked(true);
            else if ("subject".equals(sort))
                menu.findItem(R.id.menu_sort_on_subject).setChecked(true);
            else if ("size".equals(sort))
                menu.findItem(R.id.menu_sort_on_size).setChecked(true);
            else if ("attachments".equals(sort))
                menu.findItem(R.id.menu_sort_on_attachments).setChecked(true);
            else if ("snoozed".equals(sort))
                menu.findItem(R.id.menu_sort_on_snoozed).setChecked(true);
            menu.findItem(R.id.menu_ascending).setChecked(ascending);

            menu.findItem(R.id.menu_filter).setVisible(viewType != AdapterMessage.ViewType.SEARCH && !outbox);
            menu.findItem(R.id.menu_filter_seen).setVisible(folder);
            menu.findItem(R.id.menu_filter_unflagged).setVisible(folder);
            menu.findItem(R.id.menu_filter_unknown).setVisible(folder && !drafts && !sent);
            menu.findItem(R.id.menu_filter_snoozed).setVisible(folder && !drafts);
            menu.findItem(R.id.menu_filter_deleted).setVisible(folder && !perform_expunge);
            menu.findItem(R.id.menu_filter_duplicates).setVisible(viewType == AdapterMessage.ViewType.THREAD);
            menu.findItem(R.id.menu_filter_trash).setVisible(viewType == AdapterMessage.ViewType.THREAD);

            menu.findItem(R.id.menu_filter_seen).setChecked(filter_seen);
            menu.findItem(R.id.menu_filter_unflagged).setChecked(filter_unflagged);
            menu.findItem(R.id.menu_filter_unknown).setChecked(filter_unknown);
            menu.findItem(R.id.menu_filter_snoozed).setChecked(filter_snoozed);
            menu.findItem(R.id.menu_filter_deleted).setChecked(filter_deleted);
            menu.findItem(R.id.menu_filter_language).setVisible(language_detection && folder);
            menu.findItem(R.id.menu_filter_duplicates).setChecked(filter_duplicates);
            menu.findItem(R.id.menu_filter_trash).setChecked(filter_trash);

            SpannableStringBuilder ssbZoom = new SpannableStringBuilder(getString(R.string.title_zoom));
            ssbZoom.append(' ');
            for (int i = 0; i <= zoom; i++)
                ssbZoom.append('+');

            SpannableStringBuilder ssbPadding = new SpannableStringBuilder(getString(R.string.title_padding));
            ssbPadding.append(' ');
            for (int i = 0; i <= padding; i++)
                ssbPadding.append('+');

            menu.findItem(R.id.menu_zoom).setTitle(ssbZoom);
            PopupMenuLifecycle.insertIcon(context, menu.findItem(R.id.menu_zoom), false);

            menu.findItem(R.id.menu_padding).setTitle(ssbPadding);
            PopupMenuLifecycle.insertIcon(context, menu.findItem(R.id.menu_padding), false);

            menu.findItem(R.id.menu_large_buttons)
                    .setChecked(large_buttons)
                    .setVisible(viewType == AdapterMessage.ViewType.THREAD);

            menu.findItem(R.id.menu_compact).setChecked(compact);
            menu.findItem(R.id.menu_theme).setVisible(viewType == AdapterMessage.ViewType.UNIFIED || primary_inbox);

            menu.findItem(R.id.menu_confirm_links)
                    .setChecked(confirm_links)
                    .setVisible(viewType == AdapterMessage.ViewType.THREAD);

            menu.findItem(R.id.menu_select_all).setVisible(folder);
            menu.findItem(R.id.menu_select_found).setVisible(viewType == AdapterMessage.ViewType.SEARCH);
            menu.findItem(R.id.menu_mark_all_read).setVisible(folder);

            menu.findItem(R.id.menu_view_thread).setVisible(viewType == AdapterMessage.ViewType.THREAD && !threading);

            MenuItem menuSync = menu.findItem(R.id.menu_sync);
            if (lastSync == null)
                menuSync.setVisible(false);
            else {
                CharSequence title = DateUtils.getRelativeTimeSpanString(
                        lastSync,
                        new Date().getTime(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
                menuSync.setTitle(title)
                        .setVisible(true);
                PopupMenuLifecycle.insertIcon(context, menuSync, false);
            }

            menu.findItem(R.id.menu_sync_more).setVisible(folder);
            menu.findItem(R.id.menu_force_sync).setVisible(viewType == AdapterMessage.ViewType.UNIFIED || primary_inbox);
            menu.findItem(R.id.menu_force_send).setVisible(outbox);

            menu.findItem(R.id.menu_expunge).setVisible(viewType == AdapterMessage.ViewType.FOLDER &&
                    (!perform_expunge || BuildConfig.DEBUG));

            menu.findItem(R.id.menu_edit_properties).setVisible(viewType == AdapterMessage.ViewType.FOLDER && !outbox);

            // In some cases onPrepareOptionsMenu can be called before onCreateView
            if (ibSeen == null)
                ibSeen = view.findViewById(R.id.ibSeen);
            if (ibUnflagged == null)
                ibUnflagged = view.findViewById(R.id.ibUnflagged);
            if (ibSnoozed == null)
                ibSnoozed = view.findViewById(R.id.ibSnoozed);

            ibSeen.setImageResource(filter_seen ? R.drawable.twotone_drafts_24 : R.drawable.twotone_mail_24);
            ibUnflagged.setImageResource(filter_unflagged ? R.drawable.twotone_star_border_24 : R.drawable.baseline_star_24);
            ibSnoozed.setImageResource(filter_snoozed ? R.drawable.twotone_visibility_off_24 : R.drawable.twotone_visibility_24);

            ibSeen.setImageTintList(ColorStateList.valueOf(filter_seen ? colorAccent : colorControlNormal));
            ibUnflagged.setImageTintList(ColorStateList.valueOf(filter_unflagged ? colorAccent : colorControlNormal));
            ibSnoozed.setImageTintList(ColorStateList.valueOf(filter_snoozed ? colorControlNormal : colorAccent));

            ibSeen.setVisibility(quick_filter && folder ? View.VISIBLE : View.GONE);
            ibUnflagged.setVisibility(quick_filter && folder ? View.VISIBLE : View.GONE);
            ibSnoozed.setVisibility(quick_filter && folder && !drafts ? View.VISIBLE : View.GONE);

            super.onPrepareOptionsMenu(menu);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_search) {
            onMenuSearch();
            return true;
        } else if (itemId == R.id.menu_save_search || itemId == R.id.menu_edit_search) {
            onMenuSaveSearch();
            return true;
        } else if (itemId == R.id.menu_folders) { // Obsolete
            onMenuFolders(primary);
            return true;
        } else if (itemId == R.id.menu_empty_trash) {
            onMenuEmpty(EntityFolder.TRASH);
            return true;
        } else if (itemId == R.id.menu_empty_spam) {
            onMenuEmpty(EntityFolder.JUNK);
            return true;
        } else if (itemId == R.id.menu_sort_on_time) {
            item.setChecked(true);
            onMenuSort("time");
            return true;
        } else if (itemId == R.id.menu_sort_on_unread) {
            item.setChecked(true);
            onMenuSort("unread");
            return true;
        } else if (itemId == R.id.menu_sort_on_starred) {
            item.setChecked(true);
            onMenuSort("starred");
            return true;
        } else if (itemId == R.id.menu_sort_on_unread_starred) {
            item.setChecked(true);
            onMenuSort("unread+starred");
            return true;
        } else if (itemId == R.id.menu_sort_on_starred_unread) {
            item.setChecked(true);
            onMenuSort("starred+unread");
            return true;
        } else if (itemId == R.id.menu_sort_on_priority) {
            item.setChecked(true);
            onMenuSort("priority");
            return true;
        } else if (itemId == R.id.menu_sort_on_sender) {
            item.setChecked(true);
            onMenuSort("sender");
            return true;
        } else if (itemId == R.id.menu_sort_on_subject) {
            item.setChecked(true);
            onMenuSort("subject");
            return true;
        } else if (itemId == R.id.menu_sort_on_size) {
            item.setChecked(true);
            onMenuSort("size");
            return true;
        } else if (itemId == R.id.menu_sort_on_attachments) {
            item.setChecked(true);
            onMenuSort("attachments");
            return true;
        } else if (itemId == R.id.menu_sort_on_snoozed) {
            item.setChecked(true);
            onMenuSort("snoozed");
            return true;
        } else if (itemId == R.id.menu_ascending) {
            onMenuAscending(!item.isChecked());
            return true;
        } else if (itemId == R.id.menu_filter_seen) {
            onMenuFilter(getFilter(getContext(), "seen", viewType, type), !item.isChecked());
            return true;
        } else if (itemId == R.id.menu_filter_unflagged) {
            onMenuFilter(getFilter(getContext(), "unflagged", viewType, type), !item.isChecked());
            return true;
        } else if (itemId == R.id.menu_filter_unknown) {
            onMenuFilter(getFilter(getContext(), "unknown", viewType, type), !item.isChecked());
            return true;
        } else if (itemId == R.id.menu_filter_snoozed) {
            onMenuFilter(getFilter(getContext(), "snoozed", viewType, type), !item.isChecked());
            return true;
        } else if (itemId == R.id.menu_filter_deleted) {
            onMenuFilter(getFilter(getContext(), "deleted", viewType, type), !item.isChecked());
            return true;
        } else if (itemId == R.id.menu_filter_language) {
            onMenuFilterLanguage();
            return true;
        } else if (itemId == R.id.menu_filter_duplicates) {
            onMenuFilterDuplicates(!item.isChecked());
            return true;
        } else if (itemId == R.id.menu_filter_trash) {
            onMenuFilterTrash(!item.isChecked());
            return true;
        } else if (itemId == R.id.menu_zoom) {
            onMenuZoom();
            return true;
        } else if (itemId == R.id.menu_padding) {
            onMenuPadding();
            return true;
        } else if (itemId == R.id.menu_large_buttons) {
            onMenuLargeButtons();
            return true;
        } else if (itemId == R.id.menu_compact) {
            onMenuCompact();
            return true;
        } else if (itemId == R.id.menu_theme) {
            onMenuTheme();
            return true;
        } else if (itemId == R.id.menu_confirm_links) {
            onMenuConfirmLinks();
            return true;
        } else if (itemId == R.id.menu_select_all || itemId == R.id.menu_select_found) {
            onMenuSelect(0, Long.MAX_VALUE, false);
            return true;
        } else if (itemId == R.id.menu_mark_all_read) {
            onMenuMarkAllRead();
            return true;
        } else if (itemId == R.id.menu_view_thread) {
            onMenuViewThread();
            return true;
        } else if (itemId == R.id.menu_sync) {
            refresh(false);
            return true;
        } else if (itemId == R.id.menu_sync_more) {
            onMenuSyncMore();
            return true;
        } else if (itemId == R.id.menu_force_sync) {
            onMenuForceSync();
            return true;
        } else if (itemId == R.id.menu_force_send) {
            onSwipeRefresh();
            return true;
        } else if (itemId == R.id.menu_expunge) {
            onExpunge();
            return true;
        } else if (itemId == R.id.menu_edit_properties) {
            onMenuEditProperties();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuSearch() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);

        FragmentDialogSearch fragment = new FragmentDialogSearch();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "search");
    }

    private void onMenuSaveSearch() {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);
        args.putSerializable("criteria", criteria);

        FragmentDialogSaveSearch fragment = new FragmentDialogSaveSearch();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_SAVE_SEARCH);
        fragment.show(getParentFragmentManager(), "search:save");
    }

    private void onMenuFolders(long account) {
        if (!isAdded())
            return;

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getParentFragmentManager().popBackStack("unified", 0);

        Bundle args = new Bundle();
        args.putLong("account", account);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folders");
        fragmentTransaction.commit();
    }

    private void onMenuEmpty(String type) {
        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");

                List<EntityAccount> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                if (aid < 0) {
                    List<EntityAccount> accounts = db.account().getSynchronizingAccounts(null);
                    if (accounts != null)
                        result.addAll(accounts);
                } else {
                    EntityAccount account = db.account().getAccount(aid);
                    if (account != null)
                        result.add(account);
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                boolean permanent = false;
                for (EntityAccount account : accounts)
                    if (account.protocol == EntityAccount.TYPE_IMAP || !account.leave_deleted) {
                        permanent = true;
                        break;
                    }

                Bundle aargs = new Bundle();

                if (EntityFolder.TRASH.equals(type))
                    aargs.putString("question", getString(
                            accounts.size() > 1 ? R.string.title_empty_trash_all_ask : R.string.title_empty_trash_ask));
                else if (EntityFolder.JUNK.equals(type))
                    aargs.putString("question", getString(
                            accounts.size() > 1 ? R.string.title_empty_spam_all_ask : R.string.title_empty_spam_ask));
                else
                    throw new IllegalArgumentException("Invalid folder type=" + type);

                aargs.putBoolean("warning", true);

                if (permanent)
                    aargs.putString("remark", getString(R.string.title_empty_all));

                aargs.putLong("account", args.getLong("account"));
                aargs.putString("type", type);

                FragmentDialogAsk ask = new FragmentDialogAsk();
                ask.setArguments(aargs);
                ask.setTargetFragment(FragmentMessages.this, REQUEST_EMPTY_FOLDER);
                ask.show(getParentFragmentManager(), "messages:empty");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:empty");
    }

    private void onMenuSort(String sort) {
        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(getSort(context, viewType, type), sort).apply();
        adapter.setSort(sort);
        loadMessages(true);
    }

    private void onMenuAscending(boolean ascending) {
        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(getSortOrder(context, viewType, type), ascending).apply();
        adapter.setAscending(ascending);
        invalidateOptionsMenu();
        loadMessages(true);
    }

    private void onMenuFilter(String name, boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean(name, filter).apply();
        invalidateOptionsMenu();
        if (selectionTracker != null)
            selectionTracker.clearSelection();
        loadMessages(true);
    }

    private void onMenuFilterLanguage() {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);

        new SimpleTask<List<Locale>>() {
            @Override
            protected List<Locale> onExecute(Context context, Bundle args) {
                long account = args.getLong("account");
                long folder = args.getLong("folder");

                DB db = DB.getInstance(context);
                List<String> languages = db.message().getLanguages(
                        account < 0 ? null : account,
                        folder < 0 ? null : folder);

                List<Locale> locales = new ArrayList<>();
                for (String language : languages)
                    locales.add(new Locale(language));

                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                Collections.sort(locales, new Comparator<Locale>() {
                    @Override
                    public int compare(Locale l1, Locale l2) {
                        return collator.compare(l1.getDisplayLanguage(), l2.getDisplayLanguage());
                    }
                });

                return locales;
            }

            @Override
            protected void onExecuted(Bundle args, List<Locale> locales) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                String current = prefs.getString("filter_language", null);

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), vwAnchor);

                SpannableStringBuilder all = new SpannableStringBuilderEx(getString(R.string.title_language_all));
                if (current == null) {
                    all.setSpan(new StyleSpan(Typeface.BOLD), 0, all.length(), 0);
                    all.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_LARGE), 0, all.length(), 0);
                }

                popupMenu.getMenu().add(Menu.NONE, 0, 0, all);

                for (int i = 0; i < locales.size(); i++) {
                    Locale locale = locales.get(i);
                    String language = locale.getLanguage();
                    SpannableStringBuilder title = new SpannableStringBuilderEx(locale.getDisplayLanguage());
                    if (language.equals(current)) {
                        title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
                        title.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_LARGE), 0, title.length(), 0);
                    }
                    popupMenu.getMenu()
                            .add(Menu.NONE, i + 1, i + 1, title)
                            .setIntent(new Intent().putExtra("locale", locale));
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == 0) // all
                            prefs.edit().remove("filter_language").apply();
                        else {
                            Locale locale = (Locale) item.getIntent().getSerializableExtra("locale");
                            prefs.edit().putString("filter_language", locale.getLanguage()).apply();
                        }

                        FragmentActivity activity = getActivity();
                        if (activity != null)
                            activity.invalidateOptionsMenu();

                        if (selectionTracker != null)
                            selectionTracker.clearSelection();

                        loadMessages(true);

                        return true;
                    }
                });
                popupMenu.show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "menu:language");
    }

    private void onMenuFilterDuplicates(boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("filter_duplicates", filter).apply();
        invalidateOptionsMenu();
        adapter.setFilterDuplicates(filter);
    }

    private void onMenuFilterTrash(boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("filter_trash", filter).apply();
        invalidateOptionsMenu();
        adapter.setFilterTrash(filter);
    }

    private void onMenuZoom() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        zoom = ++zoom % 3;
        prefs.edit().putInt("view_zoom", zoom).apply();
        clearMeasurements();
        adapter.setZoom(zoom);
        invalidateOptionsMenu();
    }

    private void onMenuPadding() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int padding = prefs.getInt("view_padding", compact || !cards ? 0 : 1);
        padding = ++padding % 3;
        prefs.edit().putInt("view_padding", padding).apply();
        clearMeasurements();
        adapter.setPadding(padding);
        invalidateOptionsMenu();
    }

    private void onMenuLargeButtons() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean large_buttons = !prefs.getBoolean("large_buttons", false);
        prefs.edit().putBoolean("large_buttons", large_buttons).apply();
        adapter.setLargeButtons(large_buttons);
    }

    private void onMenuCompact() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = !prefs.getBoolean("compact", false);
        prefs.edit().putBoolean("compact", compact).apply();

        int zoom = (compact ? 0 : 1);
        int padding = (compact || !cards ? 0 : 1);
        prefs.edit()
                .putInt("view_zoom", zoom)
                .putInt("view_padding", padding)
                .apply();

        adapter.setCompact(compact);
        adapter.setZoom(zoom);
        adapter.setPadding(padding);
        clearMeasurements();
        invalidateOptionsMenu();
    }

    private void onMenuTheme() {
        new FragmentDialogTheme().show(getParentFragmentManager(), "messages:theme");
    }

    private void onMenuConfirmLinks() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean confirm_links = !prefs.getBoolean("confirm_links", true);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("confirm_links", confirm_links);
        if (confirm_links)
            for (String key : prefs.getAll().keySet())
                if (key.endsWith(".confirm_link"))
                    editor.remove(key);
        editor.apply();
        invalidateOptionsMenu();
    }

    private void clearMeasurements() {
        sizes.clear();
        heights.clear();
        positions.clear();
    }

    private void onMenuSelect(long from, long to, boolean extend) {
        ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);
        model.getIds(getContext(), getViewLifecycleOwner(), from, to, new Observer<List<Long>>() {
            @Override
            public void onChanged(List<Long> ids) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (selectionTracker == null)
                                return;
                            if (!extend)
                                selectionTracker.clearSelection();
                            for (long id : ids)
                                if (extend) {
                                    if (selectionTracker.isSelected(id))
                                        selectionTracker.deselect(id);
                                    else
                                        selectionTracker.select(id);
                                } else
                                    selectionTracker.select(id);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
            }
        });
    }

    private void onMenuMarkAllRead() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean all_read_asked = prefs.getBoolean("all_read_asked", false);
        if (all_read_asked) {
            markAllRead();
            return;
        }

        Bundle args = new Bundle();
        args.putString("question", getString(R.string.title_mark_all_read));
        args.putString("notagain", "all_read_asked");

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(args);
        ask.setTargetFragment(FragmentMessages.this, REQUEST_ALL_READ);
        ask.show(getParentFragmentManager(), "messages:allread");
    }

    private void onMenuViewThread() {
        Bundle args = new Bundle(getArguments());
        args.putBoolean("force_threading", true);

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "thread").addToBackStack("thread");
        fragmentTransaction.commit();
    }

    private void markAllRead() {
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putLong("folder", folder);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                String type = args.getString("type");
                long folder = args.getLong("folder");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean filter_unflagged = prefs.getBoolean(getFilter(context, "unflagged", viewType, type), false);
                boolean filter_unknown = prefs.getBoolean(getFilter(context, "unknown", viewType, type), false);
                boolean filter_snoozed = prefs.getBoolean(getFilter(context, "snoozed", viewType, type), true);
                boolean language_detection = prefs.getBoolean("language_detection", false);
                String filter_language = prefs.getString("filter_language", null);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<Long> ids = db.message().getMessageUnseen(
                            folder < 0 ? null : folder,
                            folder < 0 ? type : null,
                            filter_unflagged, filter_unknown, filter_snoozed,
                            language_detection ? filter_language : null);
                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null)
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:allread");
    }

    private void onSaveSearch(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                BoundaryCallbackMessages.SearchCriteria criteria =
                        (BoundaryCallbackMessages.SearchCriteria) args.getSerializable("criteria");

                DB db = DB.getInstance(context);

                EntityAccount account = db.account().getAccount(args.getLong("account"));
                EntityFolder folder = db.folder().getFolder(args.getLong("folder"));

                EntitySearch search = null;
                if (criteria.id != null)
                    search = db.search().getSearch(criteria.id);
                if (search == null)
                    search = new EntitySearch();

                int order = args.getInt("order");

                search.account_uuid = (account == null ? null : account.uuid);
                search.folder_name = (folder == null ? null : folder.name);
                search.name = args.getString("name");
                search.order = (order < 0 ? null : order);
                search.color = args.getInt("color", Color.TRANSPARENT);
                search.data = criteria.toJsonData().toString();

                if (search.color == Color.TRANSPARENT)
                    search.color = null;

                if (search.id == null)
                    search.id = db.search().insertSearch(search);
                else
                    db.search().updateSearch(search);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "search:save");
    }

    private void onDeleteSearch(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                BoundaryCallbackMessages.SearchCriteria criteria =
                        (BoundaryCallbackMessages.SearchCriteria) args.getSerializable("criteria");

                DB db = DB.getInstance(context);
                db.search().deleteSearch(criteria.id);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "search:delete");
    }

    private void onMenuSyncMore() {
        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putString("type", type);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");
                if (fid < 0)
                    return null;

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(fid);
                if (folder != null)
                    args.putString("name", folder.getDisplayName(context));

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                FragmentDialogSync sync = new FragmentDialogSync();
                sync.setArguments(args);
                sync.show(getParentFragmentManager(), "folder:months");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:months");
    }

    private void onMenuForceSync() {
        refresh(true);
        ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
    }

    private void onMenuEditProperties() {
        Bundle args = new Bundle();
        args.putLong("folder", folder);

        new SimpleTask<EntityFolder>() {
            @Override
            protected EntityFolder onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("folder");

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(fid);
                if (folder == null)
                    return null;

                EntityAccount account = db.account().getAccount(folder.account);
                if (account == null)
                    return null;

                args.putBoolean("imap", account.protocol == EntityAccount.TYPE_IMAP);

                return folder;
            }

            @Override
            protected void onExecuted(Bundle args, EntityFolder folder) {
                if (folder == null)
                    return;

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_EDIT_FOLDER)
                                .putExtra("id", folder.id)
                                .putExtra("account", folder.account)
                                .putExtra("imap", args.getBoolean("imap")));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:properties");
    }

    private void updateState(List<TupleFolderEx> folders) {
        if (folders == null)
            folders = new ArrayList<>();
        Log.i("Folder state updated count=" + folders.size());

        // Get state
        int unseen = 0;
        boolean refreshing = false;
        boolean folderErrors = false;
        boolean accountErrors = false;
        for (TupleFolderEx folder : folders) {
            unseen += folder.unseen;

            if (folder.sync_state != null &&
                    !"downloading".equals(folder.sync_state) &&
                    (folder.account == null ||
                            "connecting".equals(folder.accountState) ||
                            "connected".equals(folder.accountState)))
                refreshing = true;

            if (folder.account != null) { // Outbox
                if (folder.error != null)
                    folderErrors = true;
                if (!BuildConfig.PLAY_STORE_RELEASE && folder.accountError != null)
                    accountErrors = true;
            }
        }

        if (refreshing == swipeRefresh.isRefreshing() &&
                Objects.equals(lastUnseen, unseen) &&
                Objects.equals(lastRefreshing, refreshing) &&
                Objects.equals(lastFolderErrors, folderErrors) &&
                Objects.equals(lastAccountErrors, accountErrors)) {
            Log.i("Folder state unchanged");
            return;
        }

        lastUnseen = unseen;
        lastRefreshing = refreshing;
        lastFolderErrors = folderErrors;
        lastAccountErrors = accountErrors;

        // Get name
        String name;
        Context context = getContext();
        if (viewType == AdapterMessage.ViewType.UNIFIED)
            if (type == null) {
                name = (folders.size() == 1 ? folders.get(0).accountName : null);
                if (name == null)
                    name = getString(R.string.title_folder_unified);
            } else
                name = "»" + EntityFolder.localizeType(context, type) + (category == null ? "" : "/" + category);
        else {
            name = (folders.size() > 0 ? folders.get(0).getDisplayName(context) : "");
            if (folders.size() == 1) {
                String accountName = folders.get(0).accountName;
                if (accountName != null)
                    name = accountName + "/" + name;
            }
        }

        // Show name/unread
        if (unseen > 0)
            name = getString(R.string.title_name_count, name, NF.format(unseen));
        setSubtitle(name);

        fabError.setTag(accountErrors);
        if (folderErrors || accountErrors)
            fabError.show();
        else
            fabError.hide();

        swipeRefresh.setRefreshing(refreshing);
    }

    private void updateMore() {
        if (selectionTracker != null && selectionTracker.hasSelection()) {
            fabMore.show();

            long[] selection = getSelection();

            Context context = tvSelectedCount.getContext();
            int count = selection.length;
            tvSelectedCount.setText(NF.format(count));
            if (count > (BuildConfig.DEBUG ? 10 : MAX_MORE)) {
                int ts = Math.round(tvSelectedCount.getTextSize());
                Drawable w = ContextCompat.getDrawable(context, R.drawable.twotone_warning_24);
                w.setBounds(0, 0, ts, ts);
                w.setTint(tvSelectedCount.getCurrentTextColor());
                tvSelectedCount.setCompoundDrawablesRelative(null, null, w, null);
                tvSelectedCount.setCompoundDrawablePadding(ts / 2);
            } else
                tvSelectedCount.setCompoundDrawablesRelative(null, null, null, null);
            tvSelectedCount.setVisibility(View.VISIBLE);

            if (quick_actions) {
                Bundle args = new Bundle();
                args.putLongArray("ids", selection);
                args.putBoolean("threading", threading);

                new SimpleTask<MoreResult>() {
                    @Override
                    protected MoreResult onExecute(Context context, Bundle args) {
                        long[] ids = args.getLongArray("ids");
                        boolean threading = args.getBoolean("threading");
                        return MoreResult.get(context, ids, threading, true);
                    }

                    @Override
                    protected void onExecuted(Bundle args, MoreResult result) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean more_answer = prefs.getBoolean("more_answer", false);
                        boolean more_seen = prefs.getBoolean("more_seen", true);
                        boolean more_unseen = prefs.getBoolean("more_unseen", false);
                        boolean more_snooze = prefs.getBoolean("more_snooze", false);
                        boolean more_hide = prefs.getBoolean("more_hide", false);
                        boolean more_flag = prefs.getBoolean("more_flag", false);
                        boolean more_flag_color = prefs.getBoolean("more_flag_color", false);
                        boolean more_importance_high = prefs.getBoolean("more_importance_high", false);
                        boolean more_importance_normal = prefs.getBoolean("more_importance_normal", false);
                        boolean more_importance_low = prefs.getBoolean("more_importance_low", false);
                        boolean more_summarize = prefs.getBoolean("more_summarize", false);
                        boolean more_inbox = prefs.getBoolean("more_inbox", true);
                        boolean more_archive = prefs.getBoolean("more_archive", true);
                        boolean more_junk = prefs.getBoolean("more_junk", true);
                        boolean more_trash = prefs.getBoolean("more_trash", true);
                        boolean more_delete = prefs.getBoolean("more_delete", false);
                        boolean more_move = prefs.getBoolean("more_move", true);
                        boolean more_keywords = prefs.getBoolean("more_keywords", false);

                        boolean inTrash = EntityFolder.TRASH.equals(type);
                        boolean inJunk = EntityFolder.JUNK.equals(type);

                        int count = 0;

                        boolean move = (more_move && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.canMove());
                        if (move)
                            count++;

                        boolean archive = (more_archive && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.canArchive());
                        if (archive)
                            count++;

                        boolean delete = (more_delete && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.canDelete());
                        if (delete)
                            count++;

                        boolean trash = (more_trash && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.canTrash());
                        if (trash)
                            count++;

                        if (!delete && !trash && (inTrash || inJunk) &&
                                more_trash && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.canDelete()) {
                            delete = true;
                            count++;
                        }

                        boolean junk = (more_junk && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.canJunk());
                        if (junk)
                            count++;

                        boolean inbox = ((more_inbox || (more_junk && inJunk)) && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.canInbox());
                        if (inbox)
                            count++;

                        boolean keywords = (more_keywords && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && !result.hasPop && result.hasImap);
                        if (keywords)
                            count++;

                        boolean importance_high = (more_importance_high && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS &&
                                !EntityMessage.PRIORITIY_HIGH.equals(result.importance));
                        if (importance_high)
                            count++;

                        boolean importance_normal = (more_importance_normal && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS &&
                                !EntityMessage.PRIORITIY_NORMAL.equals(result.importance));
                        if (importance_normal)
                            count++;

                        boolean importance_low = (more_importance_low && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS &&
                                !EntityMessage.PRIORITIY_LOW.equals(result.importance));
                        if (importance_low)
                            count++;

                        boolean flag = (more_flag && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.unflagged);
                        if (flag)
                            count++;

                        boolean unflag = (more_flag && !flag && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.flagged);
                        if (unflag)
                            count++;

                        boolean flag_color = (more_flag_color && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && (result.unflagged || result.flagged));
                        if (flag_color)
                            count++;

                        boolean hide = (more_hide && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.visible);
                        if (hide)
                            count++;

                        boolean snooze = (more_snooze && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS);
                        if (snooze)
                            count++;

                        boolean unseen = (more_unseen && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.seen);
                        if (unseen)
                            count++;

                        boolean seen = (more_seen && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS && result.unseen);
                        if (seen)
                            count++;

                        boolean summarize = (more_summarize && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS &&
                                result.single != null && result.single.content);
                        if (summarize)
                            count++;

                        boolean answer = (more_answer && count < FragmentDialogQuickActions.MAX_QUICK_ACTIONS &&
                                result.single != null && result.single.content);

                        ibBatchFlag.setImageResource(unflag ? R.drawable.twotone_star_border_24 : R.drawable.twotone_star_24);
                        ibInbox.setImageResource(inJunk ? R.drawable.twotone_report_off_24 : R.drawable.twotone_inbox_24);

                        ibAnswer.setVisibility(answer ? View.VISIBLE : View.GONE);
                        ibSummarize.setVisibility(summarize ? VISIBLE : GONE);
                        ibBatchSeen.setVisibility(seen ? View.VISIBLE : View.GONE);
                        ibBatchUnseen.setVisibility(unseen ? View.VISIBLE : View.GONE);
                        ibBatchSnooze.setVisibility(snooze ? View.VISIBLE : View.GONE);
                        ibBatchHide.setVisibility(hide ? View.VISIBLE : View.GONE);
                        ibBatchFlag.setVisibility(flag || unflag ? View.VISIBLE : View.GONE);
                        ibBatchFlagColor.setVisibility(flag_color ? View.VISIBLE : View.GONE);
                        ibLowImportance.setVisibility(importance_low ? View.VISIBLE : View.GONE);
                        ibNormalImportance.setVisibility(importance_normal ? View.VISIBLE : View.GONE);
                        ibHighImportance.setVisibility(importance_high ? View.VISIBLE : View.GONE);
                        ibMove.setVisibility(move ? View.VISIBLE : View.GONE);
                        ibArchive.setVisibility(archive ? View.VISIBLE : View.GONE);
                        ibTrash.setVisibility(trash ? View.VISIBLE : View.GONE);
                        ibDelete.setVisibility(delete ? View.VISIBLE : View.GONE);
                        ibJunk.setVisibility(junk ? View.VISIBLE : View.GONE);
                        ibInbox.setVisibility(inbox ? View.VISIBLE : View.GONE);
                        ibKeywords.setVisibility(keywords ? View.VISIBLE : View.GONE);
                        cardMore.setTag(fabMore.isOrWillBeShown() ? result : null);
                        cardMore.setVisibility(fabMore.isOrWillBeShown() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.setExecutor(executor).setId("messages:" + FragmentMessages.this.hashCode()).execute(this, args, "quickactions");
            }
        } else {
            fabMore.hide();
            tvSelectedCount.setVisibility(View.GONE);
            cardMore.setVisibility(View.GONE);
            cardMore.setTag(null);
        }
    }

    private void loadMessages(final boolean top) {
        if (viewType == AdapterMessage.ViewType.THREAD && onclose != null) {
            ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getContext(), getViewLifecycleOwner(), id, lpos, new ViewModelMessages.IPrevNext() {
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
        } else
            loadMessagesNext(top);
    }

    private void loadMessagesNext(final boolean top) {
        if (top)
            adapter.gotoTop();

        ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);

        ViewModelMessages.Model vmodel = model.getModel(
                getContext(), getViewLifecycleOwner(),
                viewType, type, category, account, folder, thread, id, threading, filter_archive, criteria, server);

        initialized = false;
        loading = false;
        vmodel.setCallback(getViewLifecycleOwner(), callback);
        vmodel.setObserver(getViewLifecycleOwner(), observer);
    }

    private BoundaryCallbackMessages.IBoundaryCallbackMessages callback = new BoundaryCallbackMessages.IBoundaryCallbackMessages() {
        @Override
        public void onLoading() {
            loading = true;
            updateListState("Loading", SimpleTask.getCount(), adapter == null ? 0 : adapter.getItemCount());
        }

        @Override
        public void onLoaded(int found) {
            loading = false;
            if (viewType == AdapterMessage.ViewType.SEARCH)
                initialized = true;
            updateListState("Loaded found=" + found, SimpleTask.getCount(), adapter == null ? 0 : adapter.getItemCount() + found);
        }

        @Override
        public void onWarning(String message) {
            ToastEx.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onException(@NonNull Throwable ex) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                if (ex instanceof IllegalStateException) {
                    // No internet connection
                    Snackbar snackbar = Helper.setSnackbarOptions(
                            Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG));
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("tab", "connection"));
                        }
                    });
                    snackbar.show();
                } else {
                    if (viewType == AdapterMessage.ViewType.SEARCH && !server)
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    else {
                        Bundle args = new Bundle();
                        args.putString("error", Log.formatThrowable(ex, false));

                        FragmentDialogBoundaryError fragment = new FragmentDialogBoundaryError();
                        fragment.setArguments(args);
                        fragment.setTargetFragment(FragmentMessages.this, REQUEST_BOUNDARY_RETRY);
                        fragment.show(getParentFragmentManager(), "boundary:error");
                    }
                }
        }
    };

    private Observer<PagedList<TupleMessageEx>> observer = new Observer<PagedList<TupleMessageEx>>() {
        @Override
        public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
            if (messages == null)
                return;

            if (viewType != AdapterMessage.ViewType.SEARCH)
                setCount(messages.size() <= 1 ? null : NF.format(messages.size()));

            if (viewType == AdapterMessage.ViewType.THREAD) {
                if (handleThreadActions(messages, null, null))
                    return;

                List<Long> ids = values.get("expanded");
                if (ids != null)
                    for (long id : new ArrayList<>(ids)) {
                        boolean found = false;
                        for (TupleMessageEx message : messages)
                            if (message != null && Objects.equals(message.id, id)) {
                                found = true;
                                break;
                            }
                        if (!found)
                            ids.remove(id);
                    }
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean autoscroll = prefs.getBoolean("autoscroll", false);
                if (autoscroll) {
                    ActivityView activity = (ActivityView) getActivity();
                    if (activity != null &&
                            activity.isFolderUpdated(viewType == AdapterMessage.ViewType.UNIFIED ? null : folder, type))
                        adapter.gotoTop();
                }
            }

            Log.i("Submit messages=" + messages.size());
            adapter.submitList(messages);

            updateExpanded();

            if (viewType != AdapterMessage.ViewType.SEARCH)
                initialized = true;
            updateListState("Observed", SimpleTask.getCount(), messages.size());

            grpReady.setVisibility(View.VISIBLE);
        }
    };

    private void updateListState(String reason, int tasks, int items) {
        Context context = getContext();
        if (context == null)
            return;
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        boolean outbox = EntityFolder.OUTBOX.equals(type);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean filter_seen = prefs.getBoolean(getFilter(context, "seen", viewType, type), false);
        boolean filter_unflagged = prefs.getBoolean(getFilter(context, "unflagged", viewType, type), false);
        boolean filter_unknown = prefs.getBoolean(getFilter(context, "unknown", viewType, type), false);
        boolean language_detection = prefs.getBoolean("language_detection", false);
        String filter_language = prefs.getString("filter_language", null);
        boolean filter_active = ((filter_seen && !outbox) ||
                (filter_unflagged && !outbox) ||
                (filter_unknown && !EntityFolder.isOutgoing(type)) ||
                (language_detection && !TextUtils.isEmpty(filter_language) && !outbox));

        boolean none = (items == 0 && initialized);
        boolean search = (viewType == AdapterMessage.ViewType.SEARCH && server);
        boolean searching = (search && (!initialized || loading) && items == 0);
        boolean filtered = (filter_active && viewType != AdapterMessage.ViewType.SEARCH);

        pbWait.setVisibility(loading || tasks > 0 ? View.VISIBLE : View.GONE);
        tvNoEmail.setText(searching ? R.string.title_search_server_wait : R.string.title_no_messages);
        tvNoEmail.setVisibility(none || searching ? View.VISIBLE : View.GONE);

        tvNoEmailHint.setVisibility(none && (filtered || search) ? View.VISIBLE : View.GONE);

        if (BuildConfig.DEBUG)
            updateDebugInfo();

        Log.i("List state who=" + Helper.getWho(this) + "" +
                " reason=" + reason +
                " tasks=" + tasks + " loading=" + loading +
                " items=" + items + " initialized=" + initialized +
                " wait=" + (pbWait.getVisibility() == View.VISIBLE) +
                " no=" + (tvNoEmail.getVisibility() == View.VISIBLE));
    }

    private Long lastCpu = null;
    private Long lastTime = null;

    private void updateDebugInfo() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        long[] stats = Log.jni_safe_runtime_stats();
        if (stats == null) {
            tvDebug.setText("OOM");
            return;
        }

        long hused = stats[0] - stats[1];
        long hmax = stats[2];
        int processors = (int) stats[3];
        long nheap = stats[4];
        int perc = Math.round(hused * 100f / hmax);

        int utilization = 0;
        long cpu = android.os.Process.getElapsedCpuTime();
        long time = SystemClock.elapsedRealtime();
        if (lastCpu != null) {
            int cpuDelta = (int) (cpu - lastCpu);
            int timeDelta = (int) (time - lastTime);
            if (timeDelta != 0)
                utilization = 100 * cpuDelta / timeDelta / processors;
        }
        lastCpu = cpu;
        lastTime = time;

        tvDebug.setText(utilization + "%\n" + perc + "% " + (nheap / (1024 * 1024)) + "M");
    }

    private boolean handleThreadActions(
            @NonNull PagedList<TupleMessageEx> messages,
            ArrayList<MessageTarget> targets, List<Long> removed) {
        if (messages.size() == 0 && pinned)
            return false;

        // Auto close / next
        if (messages.size() == 0 && (autoclose || onclose != null)) {
            handleAutoClose();
            return true;
        }

        final Context context = getContext();
        if (context == null)
            return true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean expand_first = prefs.getBoolean("expand_first", true);
        boolean expand_all = prefs.getBoolean("expand_all", false);
        boolean autoclose_send = prefs.getBoolean("autoclose_send", false);
        long download = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
        boolean download_limited = prefs.getBoolean("download_limited", false);
        boolean dup_msgids = prefs.getBoolean("dup_msgids", false);

        if (autoclose_send) {
            int sent = 0;
            for (TupleMessageEx message : messages)
                if (message != null &&
                        (EntityFolder.OUTBOX.equals(message.folderType) ||
                                EntityFolder.SENT.equals(message.folderType)))
                    sent++;

            if (lastSentCount > 0 && sent > lastSentCount) {
                finish();
                return true;
            }

            lastSentCount = sent;
        }

        // Mark duplicates
        Map<String, List<TupleMessageEx>> duplicates = new HashMap<>();
        for (TupleMessageEx message : messages) {
            if (message == null)
                continue;

            String key = (dup_msgids ? message.msgid : message.hash);
            if (TextUtils.isEmpty(key))
                continue;

            if (!duplicates.containsKey(key))
                duplicates.put(key, new ArrayList<>());
            duplicates.get(key).add(message);
        }

        for (String key : duplicates.keySet()) {
            List<TupleMessageEx> dups = duplicates.get(key);
            int base = 0;
            for (int i = 0; i < dups.size(); i++)
                if (dups.get(i).folder == folder) {
                    base = i;
                    break;
                }
            for (int i = 0; i < dups.size(); i++)
                if (i != base)
                    dups.get(i).duplicate = true;
        }

        // Check headers for resend
        for (TupleMessageEx message : messages) {
            if (message == null || message.headers == null)
                continue;
            boolean resend = iProperties.getValue("resend", message.id);
            if (!resend)
                continue;
            iProperties.setValue("resend", message.id, false);
            onMenuReply(message, "resend");
        }

        // Auto expand
        if (autoExpanded) {
            autoExpanded = false;
            if (download == 0)
                download = Long.MAX_VALUE;

            boolean unmetered = ConnectionHelper.getNetworkState(context).isUnmetered();

            int count = 0;
            int unseen = 0;
            int flagged = 0;
            int finds = 0;
            TupleMessageEx singleMessage = null;
            TupleMessageEx unseenMessage = null;
            TupleMessageEx flaggedMessage = null;
            TupleMessageEx lastMessage = null;
            TupleMessageEx pinnedMessage = null;
            TupleMessageEx foundMessage = null;
            for (TupleMessageEx message : messages) {
                if (message == null)
                    continue;

                if (!message.duplicate &&
                        !EntityFolder.DRAFTS.equals(message.folderType)) {
                    count++;

                    if (singleMessage == null)
                        singleMessage = message;

                    if (!message.ui_seen) {
                        unseen++;
                        unseenMessage = message;
                    }

                    if (message.ui_flagged) {
                        flagged++;
                        flaggedMessage = message;
                    }

                    lastMessage = message;
                }

                if (pinned &&
                        (message.id.equals(id) || Objects.equals(message.msgid, msgid)))
                    pinnedMessage = message;

                if (found && !message.duplicate && message.ui_found) {
                    finds++;
                    if (foundMessage == null)
                        foundMessage = message;
                }

                if (message.folder == folder &&
                        !EntityFolder.OUTBOX.equals(message.folderType))
                    autoCloseCount++;
            }

            // Auto expand when:
            // - single, non archived/trashed/sent message
            // - one unread, non archived/trashed/sent message in conversation
            // - sole message
            if (autoexpand || (pinned && pinnedMessage != null)) {
                TupleMessageEx expand = null;
                if (finds > 0) {
                    if (finds == 1)
                        expand = foundMessage;
                } else if (pinnedMessage != null)
                    expand = pinnedMessage;
                else if (count == 1)
                    expand = singleMessage;
                else if (unseen == 1)
                    expand = unseenMessage;
                else if (unseen == 0 && flagged == 1)
                    expand = flaggedMessage;
                else if (count > 0) {
                    TupleMessageEx firstMessage = (adapter.getAscending() ? lastMessage : singleMessage);
                    if (firstMessage != null &&
                            (EntityFolder.OUTBOX.equals(firstMessage.folderType) ||
                                    (expand_first && unseen == 0)))
                        expand = firstMessage;
                }

                if (expand != null && (expand.content ||
                        (!download_limited && unmetered) ||
                        (expand.size != null && expand.size < download))) {
                    iProperties.setExpanded(expand, true, false);
                    for (int pos = 0; pos < messages.size(); pos++) {
                        TupleMessageEx message = messages.get(pos);
                        if (message == expand) {
                            adapter.gotoPos(pos);
                            break;
                        }
                    }
                }
            }

            // Auto expand all seen messages
            if (expand_all)
                for (TupleMessageEx message : messages)
                    if (message != null &&
                            message.ui_seen &&
                            !message.duplicate &&
                            !EntityFolder.DRAFTS.equals(message.folderType) &&
                            !EntityFolder.TRASH.equals(message.folderType))
                        iProperties.setExpanded(message, true, false);
        } else {
            if (autoCloseCount > 0 && (autoclose || onclose != null)) {
                List<MessageTarget> mt = new ArrayList<>();
                if (targets != null)
                    mt.addAll(targets);

                int count = 0;
                for (int i = 0; i < messages.size(); i++) {
                    TupleMessageEx message = messages.get(i);
                    if (message == null ||
                            (removed != null && removed.contains(message.id)))
                        continue;

                    boolean found = false;
                    if (targets != null)
                        for (MessageTarget target : targets)
                            if (message.id.equals(target.id)) {
                                Log.i("Eval thread target id=" + target.id);
                                if (!target.isAcross()) {
                                    found = true;
                                    if (target.targetFolder.id == folder)
                                        count++;
                                }
                                mt.remove(target);
                                break;
                            }

                    if (!found && message.folder == folder)
                        count++;
                }

                for (MessageTarget target : mt)
                    if (!target.isAcross() && target.targetFolder.id == folder &&
                            (removed == null || !removed.contains(target.id)))
                        count++;

                Log.i("Auto close=" + count +
                        " targets=" + (targets != null) +
                        " removed=" + (removed != null));

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
            args.putBoolean("move_thread_sent", move_thread_sent);
            args.putBoolean("filter_archive", filter_archive);

            new SimpleTask<ActionData>() {
                @Override
                protected ActionData onExecute(Context context, Bundle args) {
                    long aid = args.getLong("account");
                    String thread = args.getString("thread");
                    long id = args.getLong("id");
                    boolean move_thread_sent = args.getBoolean("move_thread_sent");
                    boolean filter_archive = args.getBoolean("filter_archive");

                    EntityAccount account;
                    EntityFolder trash;
                    EntityFolder archive;

                    boolean trashable = false;
                    boolean snoozable = false;
                    boolean archivable = false;
                    Boolean junkOnly = null;

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        account = db.account().getAccount(aid);
                        if (account != null && account.color != null)
                            args.putInt("color", account.color);

                        trash = db.folder().getFolderByType(aid, EntityFolder.TRASH);
                        archive = db.folder().getFolderByType(aid, EntityFolder.ARCHIVE);

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                aid, thread, threading ? null : id, null);

                        for (EntityMessage message : messages) {
                            EntityFolder folder = db.folder().getFolder(message.folder);
                            if (filter_archive && EntityFolder.ARCHIVE.equals(folder.type))
                                continue;

                            junkOnly = (junkOnly == null || junkOnly) &&
                                    EntityFolder.JUNK.equals(folder.type);

                            if (!folder.read_only &&
                                    !EntityFolder.DRAFTS.equals(folder.type) &&
                                    !EntityFolder.OUTBOX.equals(folder.type) &&
                                    (!EntityFolder.SENT.equals(folder.type) || move_thread_sent) &&
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

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    if (junkOnly == null)
                        junkOnly = false;

                    boolean pop = (account != null && account.protocol == EntityAccount.TYPE_POP);

                    ActionData data = new ActionData();
                    data.delete = (trash == null || junkOnly || pop);
                    data.forever = (data.delete && (!pop || !account.leave_deleted));
                    data.trashable = trashable || junkOnly;
                    data.snoozable = snoozable;
                    data.archivable = (archivable && archive != null);
                    return data;
                }

                @Override
                protected void onExecuted(Bundle args, ActionData data) {
                    if (actionbar_color && args.containsKey("color")) {
                        int color = args.getInt("color");
                        bottom_navigation.setBackgroundColor(color);

                        Integer itemColor = null;
                        float lum = (float) ColorUtils.calculateLuminance(color);
                        if (lum > Helper.BNV_LUMINANCE_THRESHOLD)
                            itemColor = Color.BLACK;
                        else if ((1.0f - lum) > Helper.BNV_LUMINANCE_THRESHOLD)
                            itemColor = Color.WHITE;

                        if (itemColor != null)
                            bottom_navigation.setItemIconTintList(new ColorStateList(
                                    new int[][]{
                                            new int[]{android.R.attr.state_enabled},
                                            new int[]{}
                                    },
                                    new int[]{
                                            itemColor,
                                            Color.GRAY
                                    }
                            ));
                    }

                    bottom_navigation.setTag(data);

                    bottom_navigation.getMenu().findItem(actionbar_delete_id)
                            .setIcon(data.forever ? R.drawable.twotone_delete_forever_24 : R.drawable.twotone_delete_24)
                            .setTitle(data.forever ? R.string.title_delete_permanently : R.string.title_trash)
                            .setVisible(data.trashable);
                    bottom_navigation.getMenu().findItem(R.id.action_snooze)
                            .setVisible(data.snoozable);
                    bottom_navigation.getMenu().findItem(actionbar_archive_id)
                            .setIcon(R.drawable.twotone_archive_24)
                            .setTitle(R.string.title_archive)
                            .setVisible(data.archivable);
                    bottom_navigation.setVisibility(View.VISIBLE);

                    bottom_navigation.findViewById(actionbar_archive_id)
                            .setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    moveThread();
                                    return true;
                                }
                            });

                    bottom_navigation.findViewById(actionbar_delete_id)
                            .setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    onActionDelete();
                                    return true;
                                }
                            });
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.setLog(false).execute(this, args, "messages:navigation");
        }

        return false;
    }

    private void updateNavPrevNext() {
        MenuItem prev = bottom_navigation.getMenu().findItem(R.id.action_prev);
        MenuItem next = bottom_navigation.getMenu().findItem(R.id.action_next);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("updown", true)) {
            boolean reversed = prefs.getBoolean("reversed", false);
            prev.setIcon(prev.isEnabled()
                    ? (reversed ? R.drawable.twotone_north_24 : R.drawable.twotone_south_24)
                    : R.drawable.twotone_horizontal_rule_24);
            next.setIcon(next.isEnabled()
                    ? (reversed ? R.drawable.twotone_south_24 : R.drawable.twotone_north_24)
                    : R.drawable.twotone_horizontal_rule_24);
        } else {
            prev.setIcon(prev.isEnabled()
                    ? R.drawable.twotone_play_arrow_back_24
                    : R.drawable.twotone_horizontal_rule_24);
            next.setIcon(next.isEnabled()
                    ? R.drawable.twotone_play_arrow_24
                    : R.drawable.twotone_horizontal_rule_24);
        }
    }

    private void updateCompose() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        if (viewType == AdapterMessage.ViewType.UNIFIED ||
                viewType == AdapterMessage.ViewType.FOLDER)
            if (auto_hide_answer && scrolling)
                fabCompose.hide();
            else
                fabCompose.show();
    }

    private void updateExpanded() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        int expanded = (values.containsKey("expanded") ? values.get("expanded").size() : 0);
        if (auto_hide_answer && scrolling)
            fabReply.hide();
        else {
            if (expanded == 1) {
                long id = values.get("expanded").get(0);
                int pos = adapter.getPositionForKey(id);
                TupleMessageEx message = adapter.getItemAtPosition(pos);
                if (message != null && !EntityFolder.OUTBOX.equals(message.folderType)) {
                    updateAnswerIcon();
                    fabReply.show();
                } else
                    fabReply.hide();
            } else
                fabReply.hide();
        }

        ibDown.setVisibility(quick_scroll && expanded > 0 ? View.VISIBLE : View.GONE);
        ibUp.setVisibility(quick_scroll && expanded > 0 ? View.VISIBLE : View.GONE);
    }

    private void updateAnswerIcon() {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String action = prefs.getString("answer_single", "menu");
        switch (action) {
            case "reply":
                fabReply.setImageResource(R.drawable.twotone_reply_24);
                break;
            case "reply_all":
                fabReply.setImageResource(R.drawable.twotone_reply_all_24);
                break;
            case "list":
                fabReply.setImageResource(R.drawable.twotone_reorder_24);
                break;
            case "forward":
                fabReply.setImageResource(R.drawable.twotone_forward_24);
                break;
            case "resend":
                fabReply.setImageResource(R.drawable.twotone_redo_24);
                break;
            case "editasnew":
                fabReply.setImageResource(R.drawable.twotone_add_24);
                break;
            case "move":
                fabReply.setImageResource(R.drawable.twotone_drive_file_move_24);
                break;
            default:
                fabReply.setImageResource(R.drawable.twotone_reply_24_options);
        }
    }

    private void handleExpand(long id) {
        SimpleTask<Void> taskExpand = new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                boolean seen = args.getBoolean("seen");

                Long reload = null;

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

                    if (!"connected".equals(account.state) && !account.isTransient(context))
                        reload = account.id;

                    if (seen) {
                        if (message.ui_unsnoozed)
                            db.message().setMessageUnsnoozed(message.id, false);

                        if (!account.auto_seen && !message.ui_ignored && message.ui_snoozed == null) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean notify_remove = prefs.getBoolean("notify_remove", true);
                            if (notify_remove)
                                db.message().setMessageUiIgnored(message.id, true);
                        }

                        if (account.auto_seen)
                            if (account.protocol != EntityAccount.TYPE_IMAP || message.uid != null)
                                EntityOperation.queue(context, message, EntityOperation.SEEN, true);
                            else if (false)
                                for (EntityMessage similar : db.message().getMessagesBySimilarity(message.account, message.id, message.msgid, message.hash)) {
                                    db.message().setMessageSeen(similar.id, true);
                                    db.message().setMessageUiSeen(similar.id, true);
                                }
                    }

                    if (account.protocol != EntityAccount.TYPE_IMAP || message.uid != null) {
                        if (!message.content)
                            EntityOperation.queue(context, message, EntityOperation.BODY);
                    }

                    if (!EntityFolder.OUTBOX.equals(folder.type))
                        db.message().setMessageLastTouched(message.id, new Date().getTime());

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload == null)
                    ServiceSynchronize.eval(context, "expand");
                else
                    ServiceSynchronize.reload(context, reload, false, "expand");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.setLog(false);

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putBoolean("seen", seen_delay == 0);
        taskExpand.execute(this, args, "messages:expand");

        if (seen_delay == 0)
            return;

        Bundle dargs = new Bundle();
        dargs.putLong("id", id);
        dargs.putBoolean("seen", true);

        view.postDelayed(new RunnableEx("seen_delay") {
            @Override
            public void delegate() {
                if (values.containsKey("expanded") && values.get("expanded").contains(id))
                    taskExpand.execute(FragmentMessages.this, dargs, "messages:seen_delay");
            }
        }, seen_delay);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                Long reload = null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessageEx(id);
                    if (message == null)
                        return null;

                    EntityFolder folder = db.folder().getFolder(message.folder);
                    if (folder == null || folder.account == null)
                        return null;

                    EntityAccount account = db.account().getAccount(folder.account);
                    if (account == null)
                        return null;

                    if (!"connected".equals(account.state) && !account.isTransient(context))
                        reload = account.id;

                    if (account.protocol != EntityAccount.TYPE_IMAP || message.uid != null) {
                        if (!message.content)
                            EntityOperation.queue(context, message, EntityOperation.BODY);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload == null)
                    ServiceSynchronize.eval(context, "expand");
                else
                    ServiceSynchronize.reload(context, reload, false, "expand");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.setLog(false).execute(this, dargs, "messages:expand");
    }

    private void handleAutoClose() {
        if (autoclose)
            finish();
        else if (onclose != null) {
            if (closeId == null)
                finish();
            else {
                Log.i("Navigating to id=" + closeId);

                Context context = getContext();
                if (context == null) {
                    finish();
                    return;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean reversed = prefs.getBoolean("reversed", false);
                navigate(closeId, "previous".equals(onclose) ^ reversed, null);
            }
        }
    }

    private void handleExit() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean auto_undecrypt = prefs.getBoolean("auto_undecrypt", false);

        if (auto_undecrypt &&
                viewType == AdapterMessage.ViewType.THREAD) {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < adapter.getItemCount(); i++) {
                TupleMessageEx message = adapter.getItemAtPosition(i);
                if (message == null)
                    continue;
                if ((EntityMessage.PGP_SIGNENCRYPT.equals(message.ui_encrypt) &&
                        !EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt)) ||
                        (EntityMessage.SMIME_SIGNENCRYPT.equals(message.ui_encrypt) &&
                                !EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt)))
                    ids.add(message.id);
            }

            Bundle args = new Bundle();
            args.putLongArray("ids", Helper.toLongArray(ids));

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws Throwable {
                    long[] ids = args.getLongArray("ids");

                    for (long id : ids)
                        lockMessage(id);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "messages:lock");
        }
    }

    private void navigate(long id, final boolean left, final Boolean forward) {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;
        if (navigating)
            return;
        navigating = true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean reversed = prefs.getBoolean("reversed", false);

        Bundle result = new Bundle();
        result.putLong("id", id);
        getParentFragmentManager().setFragmentResult("message.selected", result);

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
                    getParentFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getArguments().putBoolean("fade", true);
                getArguments().putBoolean("left", left);

                Bundle nargs = new Bundle();
                nargs.putLong("account", message.account);
                nargs.putLong("folder", message.folder);
                nargs.putString("thread", message.thread);
                nargs.putLong("id", message.id);
                if (lpos != NO_POSITION)
                    if (forward == null)
                        nargs.putInt("lpos", lpos);
                    else
                        nargs.putInt("lpos", forward ^ reversed ? lpos + 1 : lpos - 1);
                nargs.putBoolean("found", found);
                nargs.putString("searched", searched);
                nargs.putBoolean("searchedPartial", searchedPartial);
                nargs.putBoolean("pane", pane);
                nargs.putLong("primary", primary);
                nargs.putBoolean("connected", connected);
                nargs.putBoolean("left", left);

                FragmentMessages fragment = new FragmentMessages();
                fragment.setArguments(nargs);

                int res = (pane ? R.id.content_pane : R.id.content_frame);
                if (getActivity() != null && getActivity().findViewById(res) != null) {
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(res, fragment, "thread").addToBackStack("thread");
                    fragmentTransaction.commit();
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:navigate");
    }

    private void moveThread() {
        Bundle args = new Bundle();
        args.putInt("icon", R.drawable.twotone_drive_file_move_24);
        args.putString("title", getString(R.string.title_move_to_folder));
        args.putLong("account", account);
        args.putString("thread", thread);
        args.putLong("id", id);
        args.putBoolean("move_thread_sent", move_thread_sent);
        args.putBoolean("filter_archive", filter_archive);
        args.putLongArray("disabled", new long[]{folder});

        FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_THREAD_MOVE);
        fragment.show(getParentFragmentManager(), "messages:move:thread");
    }

    private void moveAsk(final ArrayList<MessageTarget> result, boolean undo) {
        if (result.size() == 0)
            return;

        if (undo) {
            moveUndo(result);
            return;
        }

        String key = (result.size() == 1 ? "move_1_confirmed" : "move_n_confirmed");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(key, false)) {
            moveUndo(result);
            return;
        }

        Bundle aargs = new Bundle();
        aargs.putString("notagain", key);
        aargs.putParcelableArrayList("result", result);

        FragmentMoveAsk ask = new FragmentMoveAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(FragmentMessages.this, REQUEST_ASKED_MOVE);
        ask.show(getParentFragmentManager(), "messages:move");
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

                        Log.i("Move id=" + target.id + " target=" + target.targetFolder.name + " copy=" + target.copy);
                        if (target.copy)
                            EntityOperation.queue(context, message, EntityOperation.COPY, target.targetFolder.id);
                        else
                            EntityOperation.queue(context, message, EntityOperation.MOVE, target.targetFolder.id, null, null, !target.block);

                        if (target.block &&
                                EntityFolder.JUNK.equals(target.targetFolder.type))
                            EntityContact.update(context,
                                    message.account, message.identity, message.from,
                                    EntityContact.TYPE_JUNK, message.received);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "move");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (viewType == AdapterMessage.ViewType.THREAD) {
                    PagedList<TupleMessageEx> messages = adapter.getCurrentList();
                    if (messages != null && result.size() > 0) {
                        Log.i("Eval confirmed messages=" + messages.size() + " targets=" + result.size());
                        handleThreadActions(messages, result, null);
                    }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG))
                            .show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:move");
    }

    private void moveUndo(final ArrayList<MessageTarget> result) {
        final Bundle args = new Bundle();
        args.putParcelableArrayList("result", result);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                AdapterMessage adapter = (rvMessage == null ? null : (AdapterMessage) rvMessage.getAdapter());
                if (adapter == null)
                    return;

                ArrayList<MessageTarget> result = args.getParcelableArrayList("result");
                for (MessageTarget target : result) {
                    TupleMessageEx message = adapter.getItemForKey(target.id);
                    if (message != null)
                        message.ui_hide = true;
                }
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                ArrayList<MessageTarget> result = args.getParcelableArrayList("result");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                final int undo_timeout = prefs.getInt("undo_timeout", 5000);

                DB db = DB.getInstance(context);

                long now = new Date().getTime();
                long busy = now + undo_timeout * 2;
                try {
                    db.beginTransaction();

                    for (MessageTarget target : result) {
                        db.message().setMessageUiBusy(target.id, busy);
                        db.message().setMessageUiHide(target.id, true);
                        db.message().setMessageFound(target.id, false);
                        // Prevent new message notification on undo
                        db.message().setMessageUiIgnored(target.id, true);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                FragmentActivity activity = getActivity();
                if (!(activity instanceof ActivityView)) {
                    Log.e("Undo: activity missing");
                    return;
                }

                String title = getString(R.string.title_move_undo, FragmentMoveAsk.getNames(result, true), result.size());
                ((ActivityView) activity).undo(title, args, taskUndoMove, taskUndoShow);

                if (viewType == AdapterMessage.ViewType.THREAD) {
                    PagedList<TupleMessageEx> messages = adapter.getCurrentList();
                    if (messages != null && result.size() > 0) {
                        Log.i("Eval undo messages=" + messages.size() + " targets=" + result.size());
                        handleThreadActions(messages, result, null);
                    }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "undo:hide");
    }

    private static final SimpleTask<Void> taskUndoMove = new SimpleTask<Void>() {
        @Override
        protected Void onExecute(Context context, Bundle args) {
            ArrayList<MessageTarget> result = args.getParcelableArrayList("result");

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                for (MessageTarget target : result) {
                    EntityMessage message = db.message().getMessage(target.id);
                    if (message == null || !message.ui_hide)
                        continue;

                    Log.i("Move id=" + target.id + " target=" + target.targetFolder.name);
                    db.message().setMessageUiBusy(target.id, null);
                    EntityOperation.queue(context, message, EntityOperation.MOVE, target.targetFolder.id);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            ServiceSynchronize.eval(context, "move");

            return null;
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            Log.e(ex);
        }
    };

    private static final SimpleTask<Void> taskUndoShow = new SimpleTask<Void>() {
        @Override
        protected Void onExecute(Context context, Bundle args) {
            ArrayList<MessageTarget> result = args.getParcelableArrayList("result");

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                for (MessageTarget target : result) {
                    Log.i("Move undo id=" + target.id);
                    db.message().setMessageUiBusy(target.id, null);
                    db.message().setMessageUiHide(target.id, false);
                    db.message().setMessageFound(target.id, target.found);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return null;
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            Log.e(ex);
        }
    };

    static String getSort(Context context, AdapterMessage.ViewType viewType, String type) {
        if (viewType == AdapterMessage.ViewType.UNIFIED)
            return "sort_unified";
        else
            return "sort";
    }

    static String getSortOrder(Context context, AdapterMessage.ViewType viewType, String type) {
        if (EntityFolder.OUTBOX.equals(type))
            return "ascending_outbox";
        else if (viewType == AdapterMessage.ViewType.THREAD)
            return "ascending_thread";
        else if (viewType == AdapterMessage.ViewType.UNIFIED)
            return "ascending_unified";
        else
            return "ascending_list";
    }

    static String getFilter(Context context, String name, AdapterMessage.ViewType viewType, String type) {
        String filter;
        if (EntityFolder.isOutgoing(type))
            filter = "out_";
        else if (EntityFolder.ARCHIVE.equals(type) ||
                EntityFolder.TRASH.equals(type) ||
                EntityFolder.JUNK.equals(type))
            filter = type.toLowerCase(Locale.ROOT) + "_";
        else
            filter = "";
        return "filter_" + filter + name;
    }

    private void lockMessage(long id) throws IOException {
        Context context = getContext();
        if (context == null)
            return;

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            EntityMessage message = db.message().getMessage(id);
            if (message == null)
                return;

            boolean inline = true;
            List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
            for (EntityAttachment attachment : attachments)
                if (attachment.encryption != null) {
                    inline = false;
                    if (EntityMessage.SMIME_SIGNENCRYPT.equals(message.ui_encrypt) &&
                            !EntityAttachment.SMIME_MESSAGE.equals(attachment.encryption))
                        db.attachment().deleteAttachment(attachment.id);
                }

            if (inline) {
                if (message.uid == null)
                    return;

                EntityFolder folder = db.folder().getFolder(message.folder);
                if (folder == null)
                    return;

                db.message().deleteMessage(id);
                EntityOperation.queue(context, folder, EntityOperation.FETCH, message.uid);

                return;
            }

            if (message.revision != null && message.revision < 0)
                db.message().setMessageSubject(message.id, "...");

            File file = message.getFile(context);
            Helper.writeText(file, null);
            db.message().setMessageContent(message.id, true, null, null, null, null);
            //db.message().setMessageSubject(id, subject);
            db.attachment().deleteAttachments(message.id, new int[]{
                    EntityAttachment.PGP_MESSAGE,
                    EntityAttachment.SMIME_MESSAGE,
                    EntityAttachment.SMIME_SIGNED_DATA
            });
            db.message().setMessageEncrypt(message.id, message.ui_encrypt);
            db.message().setMessageRaw(message.id, false);
            db.message().setMessageRevision(message.id, null);
            db.message().setMessageStored(message.id, new Date().getTime());

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void startSearch(TextView view) {
        searchView = view;

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                // Do nothing
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                endSearch();
            }
        });

        etSearch.setText(null);
        etSearch.setVisibility(View.VISIBLE);
        etSearch.requestFocus();
        Helper.showKeyboard(etSearch);
    }

    private void endSearch() {
        if (etSearch == null)
            return;

        Helper.hideKeyboard(etSearch);
        etSearch.setVisibility(View.GONE);
        clearSearch();
        searchView = null;
    }

    private void performSearch(boolean next) {
        clearSearch();

        if (searchView == null)
            return;

        View itemView = rvMessage.findContainingItemView(searchView);
        if (itemView == null) {
            Log.w("Search: itemView not found");
            return;
        }

        int p = rvMessage.getChildAdapterPosition(itemView);
        if (p == NO_POSITION) {
            Log.w("Search: position not found");
            return;
        }

        long id = adapter.getKeyAtPosition(p);
        if (id == NO_POSITION) {
            Log.w("Search: id not found");
            return;
        }

        boolean show_full = iProperties.getValue("full", id);
        if (show_full) {
            AdapterMessage.ViewHolder holder = (AdapterMessage.ViewHolder) rvMessage.getChildViewHolder(itemView);
            String query = etSearch.getText().toString().toLowerCase();
            holder.searchWebView(query);
            return;
        }

        searchIndex = (next ? searchIndex + 1 : 1);
        String query = etSearch.getText().toString().toLowerCase();
        String text = searchView.getText().toString().toLowerCase();

        int pos = -1;
        for (int i = 0; i < searchIndex; i++)
            pos = (pos < 0 ? text.indexOf(query) : text.indexOf(query, pos + 1));

        // Wrap around
        if (pos < 0 && searchIndex > 1) {
            searchIndex = 1;
            pos = text.indexOf(query);
        }

        // Scroll to found text
        if (pos >= 0) {
            Context context = searchView.getContext();
            int color = Helper.resolveColor(context, R.attr.colorHighlight);
            SpannableString ss = new SpannableString(searchView.getText());
            ss.setSpan(new HighlightSpan(color),
                    pos, pos + query.length(), Spannable.SPAN_COMPOSING);
            ss.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_LARGE),
                    pos, pos + query.length(), Spannable.SPAN_COMPOSING);
            searchView.setText(ss);

            Layout layout = searchView.getLayout();
            if (layout != null) {
                int line = layout.getLineForOffset(pos);
                int y = layout.getLineTop(line);
                int dy = context.getResources().getDimensionPixelSize(R.dimen.search_in_text_margin);

                Rect rect = new Rect();
                searchView.getDrawingRect(rect);

                RecyclerView.ViewHolder holder = rvMessage.getChildViewHolder(itemView);
                ((ViewGroup) itemView).offsetDescendantRectToMyCoords(searchView, rect);

                iProperties.scrollTo(holder.getAdapterPosition(), rect.top + y - dy);
            }
        }

        boolean hasNext = (pos >= 0 &&
                (text.indexOf(query) != pos ||
                        text.indexOf(query, pos + 1) >= 0));
        etSearch.setActionEnabled(hasNext);
    }

    private boolean isSearching() {
        return (searchView != null);
    }

    private void clearSearch() {
        if (searchView == null)
            return;
        HtmlHelper.clearComposingText(searchView);

        View itemView = rvMessage.findContainingItemView(searchView);
        if (itemView == null)
            return;

        int p = rvMessage.getChildAdapterPosition(itemView);
        if (p == NO_POSITION)
            return;

        long id = adapter.getKeyAtPosition(p);
        if (id == NO_POSITION)
            return;

        boolean show_full = iProperties.getValue("full", id);
        if (show_full) {
            AdapterMessage.ViewHolder holder = (AdapterMessage.ViewHolder) rvMessage.getChildViewHolder(itemView);
            holder.searchWebView("");
        }
    }

    private ActivityBase.IKeyPressedListener keyPressedListener = new ActivityBase.IKeyPressedListener() {
        @Override
        public boolean onKeyPressed(KeyEvent event) {
            Context context = getContext();
            if (context == null)
                return false;
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                return false;

            List<Fragment> fragments = getParentFragmentManager().getFragments();
            if (fragments != null && fragments.size() > 0 &&
                    fragments.get(fragments.size() - 1) != FragmentMessages.this)
                return false;

            if (event.isCtrlPressed() || event.isAltPressed())
                return false;

            boolean up = (event.getAction() == ACTION_UP);
            boolean down = (event.getAction() == ACTION_DOWN);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean volumenav = prefs.getBoolean("volumenav", false);

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return (down && volumenav && onNext(context));
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return (down && volumenav && onPrevious(context));
                case KeyEvent.KEYCODE_A:
                    if (viewType == AdapterMessage.ViewType.THREAD)
                        return (up && onArchive(context));
                    break;
                case KeyEvent.KEYCODE_C:
                    return (up && onCompose(context));
                case KeyEvent.KEYCODE_D:
                    if (viewType == AdapterMessage.ViewType.THREAD)
                        return (up && onDelete(context));
                    break;
                case KeyEvent.KEYCODE_M:
                    return (up && onMore(context));
                case KeyEvent.KEYCODE_N:
                    if (viewType == AdapterMessage.ViewType.THREAD)
                        return (up && onNext(context));
                    break;
                case KeyEvent.KEYCODE_P:
                    if (viewType == AdapterMessage.ViewType.THREAD)
                        return (up && onPrevious(context));
                    break;
                case KeyEvent.KEYCODE_R:
                    return (up && onReply(context));
                case KeyEvent.KEYCODE_PAGE_UP:
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (viewType == AdapterMessage.ViewType.THREAD)
                        return (down && onScroll(context, true, 0.125f));
                    break;
                case KeyEvent.KEYCODE_PAGE_DOWN:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (viewType == AdapterMessage.ViewType.THREAD)
                        return (down && onScroll(context, false, 0.125f));
                    break;
            }

            if (!up)
                return false;

            View focused = rvMessage.getFocusedChild();
            if (focused == null)
                return false;
            int pos = rvMessage.getChildAdapterPosition(focused);
            if (pos == NO_POSITION)
                return false;
            AdapterMessage.ViewHolder holder =
                    (AdapterMessage.ViewHolder) rvMessage.getChildViewHolder(focused);
            if (holder == null)
                return false;
            return holder.onKeyPressed(event);
        }

        private boolean onNext(Context context) {
            if (next == null) {
                Animation bounce = AnimationUtils.loadAnimation(context, R.anim.bounce_left);
                view.startAnimation(bounce);
            } else
                navigate(next, false, true);
            return true;
        }

        private boolean onPrevious(Context context) {
            if (prev == null) {
                Animation bounce = AnimationUtils.loadAnimation(context, R.anim.bounce_right);
                view.startAnimation(bounce);
            } else
                navigate(prev, true, false);
            return true;
        }

        private boolean onArchive(Context context) {
            if (bottom_navigation == null ||
                    !bottom_navigation.isEnabled() ||
                    bottom_navigation.getVisibility() != View.VISIBLE)
                return false;
            MenuItem archive = bottom_navigation.getMenu().findItem(actionbar_archive_id);
            if (archive == null || !archive.isVisible() || !archive.isEnabled())
                return false;
            bottom_navigation.getMenu().performIdentifierAction(actionbar_archive_id, 0);
            return true;
        }

        private boolean onDelete(Context context) {
            if (bottom_navigation == null ||
                    !bottom_navigation.isEnabled() ||
                    bottom_navigation.getVisibility() != View.VISIBLE)
                return false;
            MenuItem delete = bottom_navigation.getMenu().findItem(actionbar_delete_id);
            if (delete == null || !delete.isVisible() || !delete.isEnabled())
                return false;
            bottom_navigation.getMenu().performIdentifierAction(actionbar_delete_id, 0);
            return true;
        }

        private boolean onReply(Context context) {
            if (!fabReply.isOrWillBeShown())
                return false;
            fabReply.performClick();
            return true;
        }

        private boolean onCompose(Context context) {
            if (!fabCompose.isOrWillBeShown())
                return false;
            fabCompose.performClick();
            return true;
        }

        private boolean onMore(Context context) {
            if (!fabMore.isOrWillBeShown())
                return false;
            fabMore.performClick();
            return true;
        }

        private boolean onScroll(Context context, boolean up, float percent) {
            int h = context.getResources().getDisplayMetrics().heightPixels;
            rvMessage.scrollBy(0, Math.round((up ? -1 : 1) * h * percent));
            return true;
        }
    };

    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            try {
                if (Helper.isKeyboardVisible(view)) {
                    Helper.hideKeyboard(view);
                    return;
                }

                if (isSearching()) {
                    endSearch();
                    return;
                }

                if (selectionTracker != null && selectionTracker.hasSelection()) {
                    selectionTracker.clearSelection();
                    return;
                }

                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean collapse_multiple = prefs.getBoolean("collapse_multiple", true);

                int count = 0;
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    TupleMessageEx message = adapter.getItemAtPosition(i);
                    if (message != null && !message.duplicate)
                        count++;
                }

                int expanded = (values.containsKey("expanded") ? values.get("expanded").size() : 0);
                if (collapse_multiple && expanded > 0 && count > 1) {
                    values.get("expanded").clear();
                    updateExpanded();
                    iProperties.refresh();
                    return;
                }

                if (expanded > 0)
                    values.get("expanded").clear();

                handleExit();

                FragmentActivity activity = getActivity();
                if (activity instanceof ActivityBase)
                    ((ActivityBase) activity).onBackPressedFragment();
                else
                    finish();
            } catch (Throwable ex) {
                Log.w(ex);
                /*
                    java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling eu.faircode.email.FixedRecyclerView{9095e62 VFED..... ........ 0,0-1440,2704 #7f0a0600 app:id/rvMessage}, adapter:eu.faircode.email.AdapterMessage@c71b6dc, layout:eu.faircode.email.FragmentMessages$11@58b3bf3, context:eu.faircode.email.ActivityView@9b77aad
                            at androidx.recyclerview.widget.RecyclerView.assertNotInLayoutOrScroll(RecyclerView:3482)
                            at androidx.recyclerview.widget.RecyclerView$RecyclerViewDataObserver.onItemRangeChanged(RecyclerView:6071)
                            at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged(RecyclerView:13219)
                            at androidx.recyclerview.widget.RecyclerView$Adapter.notifyItemChanged(RecyclerView:8136)
                            at androidx.recyclerview.selection.EventBridge$TrackerToAdapterBridge.onItemStateChanged(EventBridge:99)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.notifyItemStateChanged(DefaultSelectionTracker:439)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.notifySelectionCleared(DefaultSelectionTracker:451)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.clearPrimarySelection(DefaultSelectionTracker:182)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.clearSelection(DefaultSelectionTracker:170)
                            at eu.faircode.email.FragmentMessages$130.handleOnBackPressed(FragmentMessages:8288)
                 */
                /*
                    Exception java.lang.IllegalStateException:
                            at androidx.recyclerview.widget.RecyclerView.assertNotInLayoutOrScroll (RecyclerView.java:3482)
                            at androidx.recyclerview.widget.RecyclerView$RecyclerViewDataObserver.onItemRangeChanged (RecyclerView.java:6071)
                            at androidx.recyclerview.widget.RecyclerView$AdapterDataObservable.notifyItemRangeChanged (RecyclerView.java:13219)
                            at androidx.recyclerview.widget.RecyclerView$Adapter.notifyItemChanged (RecyclerView.java:8136)
                            at androidx.recyclerview.selection.EventBridge$TrackerToAdapterBridge.onItemStateChanged (EventBridge.java:99)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.notifyItemStateChanged (DefaultSelectionTracker.java:439)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.notifySelectionCleared (DefaultSelectionTracker.java:451)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.clearPrimarySelection (DefaultSelectionTracker.java:182)
                            at androidx.recyclerview.selection.DefaultSelectionTracker.clearSelection (DefaultSelectionTracker.java:170)
                            at eu.faircode.email.FragmentMessages$134.handleOnBackPressed (FragmentMessages.java:8358)
                 */
            }
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
        } else if (false)
            return AnimationUtils.loadAnimation(getContext(), enter ? android.R.anim.fade_in : android.R.anim.fade_out);

        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    private BroadcastReceiver treceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SimpleTask.ACTION_TASK_COUNT.equals(action))
                onTaskCount(intent);
            else if (ServiceTTS.ACTION_TTS_COMPLETED.equals(action))
                onTTSCompleted(intent);
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                String action = intent.getAction();
                if (ACTION_STORE_RAW.equals(action))
                    onStoreRaw(intent);
                else if (ACTION_VERIFYDECRYPT.equals(action))
                    onVerifyDecrypt(intent);
                else if (ACTION_KEYWORDS.equals(action))
                    onKeywords(intent);
            }
        }
    };

    private void onTaskCount(Intent intent) {
        updateListState("Tasks", intent.getIntExtra("count", 0), adapter.getItemCount());
    }

    private void onTTSCompleted(Intent intent) {
        String utteranceId = intent.getStringExtra(ServiceTTS.EXTRA_UTTERANCE_ID);
        if (utteranceId != null && utteranceId.startsWith("tts:"))
            try {
                long id = Long.parseLong(utteranceId.substring("tts:".length()));
                Log.i("TTS completed id=" + id);
                iProperties.setValue("tts", id, false);
            } catch (Throwable ex) {
                Log.e(ex);
            }
    }

    private void onStoreRaw(Intent intent) {
        getArguments().putLong("selected_message", intent.getLongExtra("id", -1));
        String subject = intent.getStringExtra("subject");
        String name = (TextUtils.isEmpty(subject) ? "email" : Helper.sanitizeFilename(subject)) + ".eml";

        final Context context = getContext();

        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        create.setType("*/*");
        create.putExtra(Intent.EXTRA_TITLE, name);
        Helper.openAdvanced(context, create);
        PackageManager pm = context.getPackageManager();
        if (create.resolveActivity(pm) == null) // system whitelisted
            Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG))
                    .show();
        else
            startActivityForResult(Helper.getChooser(context, create), REQUEST_RAW);
    }

    private void onVerifyDecrypt(Intent intent) {
        long id = intent.getLongExtra("id", -1);
        boolean auto = intent.getBooleanExtra("auto", false);
        int type = intent.getIntExtra("type", EntityMessage.ENCRYPT_NONE);

        final Bundle args = new Bundle();
        args.putLong("id", id);
        args.putInt("type", type);
        args.putBoolean("auto", auto);

        if (EntityMessage.SMIME_SIGNONLY.equals(type))
            onSmime(args);
        else if (EntityMessage.SMIME_SIGNENCRYPT.equals(type)) {
            new SimpleTask<EntityIdentity>() {
                @Override
                protected EntityIdentity onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null || message.identity == null)
                        return null;

                    EntityIdentity identity = db.identity().getIdentity(message.identity);
                    if (identity == null)
                        return null;

                    List<EntityIdentity> duplicates = db.identity().getIdentities(identity.account, identity.email);
                    if (duplicates != null && duplicates.size() > 1) {
                        args.putBoolean("duplicate", true);
                        return null;
                    }

                    return identity;
                }

                @Override
                protected void onExecuted(Bundle args, EntityIdentity identity) {
                    boolean auto = args.getBoolean("auto");
                    if (auto && identity == null)
                        return;

                    String alias = (identity == null ? null : identity.sign_key_alias);
                    Helper.selectKeyAlias(getActivity(), getViewLifecycleOwner(), alias, new Helper.IKeyAlias() {
                        @Override
                        public void onSelected(String alias) {
                            args.putString("alias", alias);
                            onSmime(args);
                        }

                        @Override
                        public void onNothingSelected() {
                            Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_no_key, Snackbar.LENGTH_LONG));
                            final Intent intent = (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                                    ? KeyChain.createInstallIntent()
                                    : new Intent(Settings.ACTION_SECURITY_SETTINGS));
                            PackageManager pm = getContext().getPackageManager();
                            if (intent.resolveActivity(pm) != null) // system whitelisted
                                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(intent);
                                    }
                                });
                            snackbar.show();
                        }
                    });
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "messages:alias");
        } else {
            Intent data = new Intent();
            data.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);
            data.putExtra(BuildConfig.APPLICATION_ID, id);
            onPgp(data, auto, false);
        }
    }

    private void onKeywords(Intent intent) {
        iProperties.refresh();
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
                case REQUEST_OPENPGP:
                    if (resultCode == RESULT_OK && data != null)
                        onPgp(data, false, false);
                    break;
                case REQUEST_MESSAGE_DELETE:
                    if (resultCode == RESULT_OK && data != null)
                        onDelete(data.getBundleExtra("args").getLong("id"));
                    break;
                case REQUEST_MESSAGES_DELETE:
                    if (resultCode == RESULT_OK && data != null)
                        onDelete(data.getBundleExtra("args"));
                    break;
                case REQUEST_MESSAGE_JUNK:
                    if (resultCode == RESULT_OK && data != null)
                        onJunk(data.getBundleExtra("args"));
                    break;
                case REQUEST_MESSAGES_JUNK:
                    if (resultCode == RESULT_OK)
                        onActionMoveSelection(EntityFolder.JUNK,
                                data.getBundleExtra("args").getBoolean("block"));
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
                            startActivity(new Intent(getContext(), ActivityBilling.class));
                            return;
                        }

                        Bundle args = data.getBundleExtra("args");
                        onActionFlagSelection(true, args.getInt("color"), null, args.getBoolean("clear"));
                    }
                    break;
                case REQUEST_MESSAGE_SNOOZE:
                    if (resultCode == RESULT_OK && data != null)
                        onSnoozeOrHide(data.getBundleExtra("args"));
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
                    if (resultCode == RESULT_OK && data != null)
                        onActionMoveSelection(data.getBundleExtra("args"));
                    break;
                case REQUEST_THREAD_MOVE:
                    if (resultCode == RESULT_OK && data != null)
                        onActionMoveThread(data.getBundleExtra("args"));
                    break;
                case REQUEST_PRINT:
                    if (resultCode == RESULT_OK && data != null)
                        onPrint(data.getBundleExtra("args"));
                    break;
                case REQUEST_SEARCH:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        BoundaryCallbackMessages.SearchCriteria criteria =
                                (BoundaryCallbackMessages.SearchCriteria) args.getSerializable("criteria");
                        search(getContext(), getViewLifecycleOwner(), getParentFragmentManager(),
                                args.getLong("account"),
                                args.getLong("folder"),
                                true, criteria);
                    }
                    break;
                case REQUEST_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onMenuFolders(args.getLong("account"));
                    }
                    break;
                case REQUEST_EMPTY_FOLDER:
                    if (resultCode == RESULT_OK)
                        onEmptyFolder(data.getBundleExtra("args"));
                    break;
                case REQUEST_BOUNDARY_RETRY:
                    if (resultCode == RESULT_OK)
                        onBoundaryRetry();
                    break;
                case REQUEST_PICK_CONTACT:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(data.getData());
                    break;
                case REQUEST_BUTTONS:
                    iProperties.refresh();
                    break;
                case REQUEST_ALL_READ:
                    if (resultCode == RESULT_OK)
                        markAllRead();
                    break;
                case REQUEST_SAVE_SEARCH:
                    if (resultCode == RESULT_OK && data != null)
                        onSaveSearch(data.getBundleExtra("args"));
                    else if (resultCode == RESULT_FIRST_USER && data != null)
                        onDeleteSearch(data.getBundleExtra("args"));
                    break;
                case REQUEST_QUICK_ACTIONS:
                    if (resultCode == RESULT_OK)
                        updateMore();
                    break;
                case REQUEST_BLOCK_SENDERS:
                    if (resultCode == RESULT_OK)
                        onBlockSenders(data.getBundleExtra("args"));
                    break;
                case REQUEST_CALENDAR:
                    if (resultCode == RESULT_OK)
                        onInsertCalendar(data.getBundleExtra("args"));
                    break;
                case REQUEST_EDIT_SUBJECT:
                    if (resultCode == RESULT_OK)
                        onEditSubject(data.getBundleExtra("args"));
                    break;
                case REQUEST_ANSWER_SETTINGS:
                    if (resultCode == RESULT_OK)
                        updateAnswerIcon();
                    break;
                case REQUEST_DESELECT:
                    if (selectionTracker != null)
                        selectionTracker.clearSelection();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSaveRaw(Intent data) {
        Bundle args = new Bundle();
        args.putLong("id", getArguments().getLong("selected_message", -1L));
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Save raw uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    throw new MessageRemovedException();

                File file = message.getRawFile(context);
                Log.i("Raw file=" + file);

                if (!file.exists())
                    db.message().setMessageRaw(message.id, false);

                OutputStream os = null;
                InputStream is = null;
                try {
                    os = context.getContentResolver().openOutputStream(uri);
                    is = new FileInputStream(file);

                    if (os == null)
                        throw new FileNotFoundException(uri.toString());

                    byte[] buffer = new byte[Helper.BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                } finally {
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
                Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_raw_saved, Snackbar.LENGTH_LONG))
                        .show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof FileNotFoundException)
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG))
                            .show();
                else if (!(ex instanceof MessageRemovedException))
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "raw:save");
    }

    private void onPgp(Intent data, boolean auto, boolean stripped) {
        Bundle args = new Bundle();
        args.putParcelable("data", data);
        args.putBoolean("auto", auto);
        args.putBoolean("stripped", stripped);

        new SimpleTask<PendingIntent>() {
            @Override
            protected PendingIntent onExecute(Context context, Bundle args) throws Throwable {
                // Get arguments
                boolean auto = args.getBoolean("auto");
                boolean stripped = args.getBoolean("stripped");
                Intent data = args.getParcelable("data");
                long id = data.getLongExtra(BuildConfig.APPLICATION_ID, -1);

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;
                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                if (attachments == null)
                    return null;

                if (auto && message.revision != null)
                    return null;

                InputStream in = null;
                OutputStream out = null;
                boolean inline = false;

                File tmp = Helper.ensureExists(context, "encryption");
                File plain = new File(tmp, message.id + ".pgp_out");

                // Find encrypted data
                for (EntityAttachment attachment : attachments)
                    if (EntityAttachment.PGP_CONTENT.equals(attachment.encryption) ||
                            EntityAttachment.PGP_MESSAGE.equals(attachment.encryption)) {
                        if (!attachment.available)
                            if (auto)
                                return null;
                            else
                                throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));

                        File file = attachment.getFile(context);
                        Log.i("PGP in=" + file.getAbsolutePath() + " exist=" + file.exists() + "/" + file.length());
                        in = new FileInputStream(file);

                        if (EntityAttachment.PGP_MESSAGE.equals(attachment.encryption)) {
                            Log.i("PGP out=" + plain.getAbsolutePath());
                            out = new FileOutputStream(plain);
                        }

                    } else if (EntityAttachment.PGP_SIGNATURE.equals(attachment.encryption)) {
                        if (!attachment.available)
                            throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));

                        File file = attachment.getFile(context);
                        byte[] signature = new byte[(int) file.length()];
                        try (FileInputStream fis = new FileInputStream(file)) {
                            fis.read(signature);
                        }
                        data.putExtra(OpenPgpApi.EXTRA_DETACHED_SIGNATURE, signature);
                    }

                if (in == null) {
                    if (message.content) {
                        File file = message.getFile(context);
                        if (file.exists()) {
                            // https://tools.ietf.org/html/rfc4880#section-6.2
                            String html = Helper.readText(file);
                            String body = HtmlHelper.fromHtml(html, context).toString();
                            int begin = body.indexOf(Helper.PGP_BEGIN_MESSAGE);
                            int end = body.indexOf(Helper.PGP_END_MESSAGE);
                            if (begin >= 0 && begin < end) {
                                String[] lines = body
                                        .substring(begin, end + Helper.PGP_END_MESSAGE.length())
                                        .split("\\r?\\n");

                                List<String> disarmored = new ArrayList<>();
                                for (String line : lines)
                                    if (!TextUtils.isEmpty(line) && !line.contains(": "))
                                        disarmored.add(line);

                                String pgpMessage = TextUtils.join("\n\r", disarmored);

                                inline = true;
                                Log.i("PGP inline");
                                in = new ByteArrayInputStream(pgpMessage.getBytes());
                                out = new FileOutputStream(plain);
                            }
                        }
                    }
                }

                if (in == null)
                    if (auto)
                        return null;
                    else
                        throw new IllegalArgumentException(context.getString(R.string.title_not_encrypted));

                if (stripped)
                    in = new MessageHelper.StripStream(new BufferedInputStream(in));

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean autocrypt = prefs.getBoolean("autocrypt", true);
                if (autocrypt &&
                        message.from != null && message.from.length > 0 &&
                        message.autocrypt != null &&
                        OpenPgpApi.ACTION_DECRYPT_VERIFY.equals(data.getAction()))
                    try {
                        String peer = ((InternetAddress) message.from[0]).getAddress();
                        String addr = null;
                        boolean mutual = false;
                        byte[] keydata = null;

                        // https://autocrypt.org/level1.html#the-autocrypt-header
                        Map<String, String> kv = MessageHelper.getKeyValues(message.autocrypt);
                        for (String key : kv.keySet()) {
                            String value = kv.get(key);
                            Log.i("Autocrypt " + key + "=" + value);
                            if (value == null)
                                continue;
                            switch (key) {
                                case "addr":
                                    addr = value;
                                    break;
                                case "prefer-encrypt":
                                    mutual = value.trim().toLowerCase(Locale.ROOT).equals("mutual");
                                    break;
                                case "keydata":
                                    keydata = Base64.decode(value, Base64.DEFAULT);
                                    break;
                            }
                        }

                        if (addr == null)
                            throw new IllegalArgumentException("Autocrypt: addr not found");

                        if (!addr.equalsIgnoreCase(peer))
                            throw new IllegalArgumentException("Autocrypt: addr different from peer");

                        if (keydata == null)
                            throw new IllegalArgumentException("Autocrypt: keydata not found");

                        AutocryptPeerUpdate update = AutocryptPeerUpdate.create(
                                keydata, new Date(message.received), mutual);

                        data.putExtra(OpenPgpApi.EXTRA_AUTOCRYPT_PEER_ID, addr);
                        data.putExtra(OpenPgpApi.EXTRA_AUTOCRYPT_PEER_UPDATE, update);
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }

                Intent result;
                try {
                    // Decrypt message
                    result = PgpHelper.execute(context, data, in, out);
                    int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
                    switch (resultCode) {
                        case OpenPgpApi.RESULT_CODE_SUCCESS:
                            Integer encrypt = null;
                            if (out != null)
                                if (inline) {
                                    try {
                                        db.beginTransaction();

                                        // Write decrypted body
                                        String text = Helper.readText(plain);
                                        String html = "<div x-plain=\"true\">" + HtmlHelper.formatPlainText(text) + "</div>";
                                        Helper.writeText(message.getFile(context), html);
                                        db.message().setMessageRevision(message.id, 1);
                                        db.message().setMessageStored(message.id, new Date().getTime());
                                        db.message().setMessageFts(message.id, false);

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    WorkerFts.init(context, false);
                                } else {
                                    // Decode message
                                    MessageHelper.MessageParts parts;
                                    Properties props = MessageHelper.getSessionProperties(true);
                                    Session isession = Session.getInstance(props, null);
                                    MimeMessage imessage;
                                    try (InputStream fis = new FileInputStream(plain)) {
                                        imessage = new MimeMessage(isession, fis);
                                    }

                                    MessageHelper helper = new MessageHelper(imessage, context);
                                    parts = helper.getMessageParts();
                                    String protect_subject = parts.getProtectedSubject();

                                    // Write decrypted body
                                    boolean debug = prefs.getBoolean("debug", false);
                                    boolean download_plain = prefs.getBoolean("download_plain", false);
                                    String html = parts.getHtml(context, download_plain);

                                    if (html == null && debug) {
                                        int textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);
                                        SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                                        MessageHelper.getStructure(imessage, ssb, 0, textColorLink);
                                        html = HtmlHelper.toHtml(ssb, context);
                                    }

                                    Helper.writeText(message.getFile(context), html);
                                    Log.i("pgp html=" + (html == null ? null : html.length()));

                                    String text = HtmlHelper.getFullText(context, html);
                                    message.preview = HtmlHelper.getPreview(text);
                                    message.language = HtmlHelper.getLanguage(context, message.subject, text);

                                    try {
                                        db.beginTransaction();

                                        if (protect_subject != null)
                                            db.message().setMessageSubject(message.id, protect_subject);

                                        db.message().setMessageContent(message.id,
                                                true,
                                                message.language,
                                                parts.isPlainOnly(download_plain),
                                                message.preview,
                                                message.warning);

                                        // Remove existing attachments
                                        db.attachment().deleteAttachments(message.id, new int[]{EntityAttachment.PGP_MESSAGE});

                                        // Add decrypted attachments
                                        List<EntityAttachment> remotes = parts.getAttachments();
                                        for (int index = 0; index < remotes.size(); index++) {
                                            EntityAttachment remote = remotes.get(index);
                                            remote.message = message.id;
                                            remote.sequence = index + 1;
                                            remote.id = db.attachment().insertAttachment(remote);
                                            try {
                                                parts.downloadAttachment(context, index, remote, null);
                                            } catch (Throwable ex) {
                                                Log.e(ex);
                                            }
                                        }

                                        boolean pep = checkPep(message, remotes, context);

                                        encrypt = parts.getEncryption();
                                        db.message().setMessageEncrypt(message.id, encrypt);
                                        db.message().setMessageRevision(message.id, pep || protect_subject == null ? 1 : -1);
                                        db.message().setMessageStored(message.id, new Date().getTime());
                                        db.message().setMessageFts(message.id, false);

                                        if (BuildConfig.DEBUG || debug) {
                                            File raw = message.getRawFile(context);
                                            Helper.copy(plain, raw);
                                            db.message().setMessageRaw(message.id, true);
                                        }

                                        db.setTransactionSuccessful();
                                    } catch (SQLiteConstraintException ex) {
                                        // Message removed
                                        Log.w(ex);
                                    } finally {
                                        db.endTransaction();
                                    }

                                    WorkerFts.init(context, false);
                                }

                            // Check signature status
                            OpenPgpSignatureResult sigResult = result.getParcelableExtra(OpenPgpApi.RESULT_SIGNATURE);
                            int sresult = (sigResult == null ? RESULT_NO_SIGNATURE : sigResult.getResult());
                            if (sigResult == null)
                                Log.w("PGP signature result missing");
                            else
                                Log.i("PGP signature result=" + sresult);

                            if (sresult == RESULT_NO_SIGNATURE) {
                                if (!EntityAttachment.PGP_SIGNATURE.equals(encrypt))
                                    args.putString("sigresult", context.getString(R.string.title_signature_none));
                            } else if (sresult == RESULT_VALID_KEY_CONFIRMED || sresult == RESULT_VALID_KEY_UNCONFIRMED) {
                                List<String> users = sigResult.getConfirmedUserIds();
                                String text;
                                if (users.size() > 0)
                                    text = context.getString(sresult == RESULT_VALID_KEY_UNCONFIRMED
                                                    ? R.string.title_signature_unconfirmed_from
                                                    : R.string.title_signature_valid_from,
                                            TextUtils.join(", ", users));
                                else
                                    text = context.getString(sresult == RESULT_VALID_KEY_UNCONFIRMED
                                            ? R.string.title_signature_unconfirmed
                                            : R.string.title_signature_valid);
                                args.putString("sigresult", text);
                                if (sresult == RESULT_VALID_KEY_CONFIRMED)
                                    db.message().setMessageVerified(message.id, true);
                            } else if (sresult == RESULT_KEY_MISSING)
                                args.putString("sigresult", context.getString(R.string.title_signature_key_missing));
                            else {
                                if (stripped) {
                                    String text = context.getString(R.string.title_signature_invalid_reason, Integer.toString(sresult));
                                    args.putString("sigresult", text);
                                } else {
                                    View v = view;
                                    if (v == null)
                                        return null;
                                    v.post(new RunnableEx("stripped") {
                                        @Override
                                        protected void delegate() {
                                            onPgp(data, auto, true);
                                        }
                                    });
                                    return null;
                                }
                            }

                            break;

                        case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                            if (auto)
                                return null;
                            return result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

                        case OpenPgpApi.RESULT_CODE_ERROR:
                            OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                            throw new IllegalArgumentException(
                                    "OpenPgp" +
                                            " error " + (error == null ? "?" : error.getErrorId()) +
                                            ": " + (error == null ? "?" : error.getMessage()));

                        default:
                            throw new IllegalStateException("OpenPgp unknown result code=" + resultCode);
                    }
                } finally {
                    Helper.secureDelete(plain);
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, PendingIntent pi) {
                if (args.containsKey("sigresult")) {
                    String text = args.getString("sigresult");
                    Snackbar sb = Helper.setSnackbarOptions(
                            Snackbar.make(view, text, Snackbar.LENGTH_LONG));
                    Helper.setSnackbarLines(sb, 7);
                    sb.show();
                }

                if (pi != null)
                    try {
                        Log.i("Executing pi=" + pi);
                        startIntentSenderForResult(
                                pi.getIntentSender(),
                                REQUEST_OPENPGP,
                                null, 0, 0, 0,
                                Helper.getBackgroundActivityOptions());
                    } catch (IntentSender.SendIntentException ex) {
                        // Likely cancelled
                        Log.w(ex);
                    }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean auto = args.getBoolean("auto");
                if (auto)
                    return;

                if (ex instanceof IllegalArgumentException) {
                    Log.i(ex);
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG))
                            .show();
                } else if (ex instanceof OperationCanceledException) {
                    Snackbar snackbar = Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_no_openpgp, Snackbar.LENGTH_INDEFINITE));
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            Helper.viewFAQ(v.getContext(), 12);
                        }
                    });
                    snackbar.show();
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.serial().execute(this, args, "decrypt:pgp");
    }

    private void onSmime(Bundle args) {
        new SimpleTask<X509Certificate>() {
            @Override
            protected X509Certificate onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                int type = args.getInt("type");

                DB db = DB.getInstance(context);

                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                X509Certificate result = null;
                if (EntityMessage.SMIME_SIGNONLY.equals(type)) {
                    // Get content/signature
                    boolean sdata = false;
                    File content = null;
                    File signature = null;
                    List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                    for (EntityAttachment attachment : attachments)
                        if (EntityAttachment.SMIME_SIGNATURE.equals(attachment.encryption)) {
                            if (!attachment.available)
                                throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                            signature = attachment.getFile(context);
                        } else if (EntityAttachment.SMIME_SIGNED_DATA.equals(attachment.encryption)) {
                            if (!attachment.available)
                                throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                            sdata = true;
                            signature = attachment.getFile(context);
                        } else if (EntityAttachment.SMIME_CONTENT.equals(attachment.encryption)) {
                            if (!attachment.available)
                                throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                            content = attachment.getFile(context);
                        }

                    if (content == null && !sdata)
                        throw new IllegalArgumentException("Signed content missing");
                    if (signature == null)
                        throw new IllegalArgumentException("Signature missing");

                    // Build signed data
                    InputStream is = null;
                    FileInputStream fis = new FileInputStream(signature);
                    CMSSignedData signedData;
                    // TODO: CMSSignedDataParser
                    if (sdata) {
                        signedData = new CMSSignedData(fis);

                        CMSTypedData sc = signedData.getSignedContent();
                        if (sc == null)
                            throw new IllegalArgumentException("Signed content missing");

                        is = new ByteArrayInputStream((byte[]) sc.getContent());
                    } else {
                        CMSProcessable signedContent = new CMSProcessableFile(content);
                        signedData = new CMSSignedData(signedContent, fis);
                    }

                    // Check signature
                    boolean matching = false;
                    Store store = signedData.getCertificates();
                    SignerInformationStore signerInfos = signedData.getSignerInfos();
                    Collection<SignerInformation> signers = signerInfos.getSigners();
                    Log.i("Signers count=" + signers.size());
                    for (SignerInformation signer : signers) {
                        SignerId sid = signer.getSID();
                        Log.i("Checking signer=" + (sid == null ? null : sid.getIssuer()));
                        Collection<Object> matches = store.getMatches(sid);
                        Log.i("Matching certificates count=" + matches.size());
                        for (Object match : matches) {
                            matching = true;
                            X509CertificateHolder certHolder = (X509CertificateHolder) match;
                            X509Certificate cert = new JcaX509CertificateConverter()
                                    .getCertificate(certHolder);
                            Log.i("Checking certificate subject=" + cert.getSubjectDN());
                            try {
                                Date signingTime;
                                AttributeTable at = signer.getSignedAttributes();
                                Attribute attr = (at == null ? null : at.get(CMSAttributes.signingTime));
                                if (attr != null && attr.getAttrValues().size() == 1)
                                    signingTime = Time.getInstance(attr.getAttrValues()
                                            .getObjectAt(0).toASN1Primitive()).getDate();
                                else
                                    signingTime = new Date(message.received);
                                args.putSerializable("time", signingTime);

                                SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                                        .build(cert);
                                SignerInformation s = new SignerInformation(signer) {
                                    @Override
                                    public AttributeTable getSignedAttributes() {
                                        // The certificate validity will be check below
                                        AttributeTable at = super.getSignedAttributes();
                                        return (at == null ? null : at.remove(CMSAttributes.signingTime));
                                    }

                                    @Override
                                    public byte[] getEncodedSignedAttributes() throws IOException {
                                        // http://www.bouncycastle.org/jira/browse/BJA-587
                                        // http://luca.ntop.org/Teaching/Appunti/asn1.html
                                        return signedAttributeSet.getEncoded(ASN1Encoding.DL);
                                    }
                                };

                                if (s.verify(verifier)) {
                                    boolean known = true;
                                    String fingerprint = EntityCertificate.getFingerprintSha256(cert);
                                    List<String> emails = EntityCertificate.getEmailAddresses(cert);
                                    for (String email : emails) {
                                        EntityCertificate record = db.certificate().getCertificate(fingerprint, email);
                                        if (record == null)
                                            known = false;
                                    }

                                    String sender = null;
                                    if (message.from != null && message.from.length == 1)
                                        sender = ((InternetAddress) message.from[0]).getAddress();

                                    args.putString("sender", sender);
                                    args.putBoolean("known", known);

                                    String algo;
                                    try {
                                        DefaultAlgorithmNameFinder af = new DefaultAlgorithmNameFinder();
                                        algo = af.getAlgorithmName(new ASN1ObjectIdentifier(s.getEncryptionAlgOID()));
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                        algo = s.getEncryptionAlgOID();
                                    }
                                    args.putString("algo", algo);

                                    List<X509Certificate> certs = new ArrayList<>();
                                    try {
                                        for (Object m : store.getMatches(null)) {
                                            X509CertificateHolder h = (X509CertificateHolder) m;
                                            certs.add(new JcaX509CertificateConverter().getCertificate(h));
                                        }
                                    } catch (Throwable ex) {
                                        Log.w(ex);
                                    }

                                    KeyStore ks = null;
                                    try {
                                        // https://tools.ietf.org/html/rfc3852#section-10.2.3
                                        ks = KeyStore.getInstance("AndroidCAStore");
                                        ks.load(null, null);

                                        // https://docs.oracle.com/javase/7/docs/technotes/guides/security/certpath/CertPathProgGuide.html
                                        X509CertSelector target = new X509CertSelector();
                                        target.setCertificate(cert);

                                        // Load/store intermediate certificates
                                        List<X509Certificate> local = new ArrayList<>(certs);
                                        try {
                                            List<EntityCertificate> ecs = db.certificate().getIntermediateCertificate();
                                            for (EntityCertificate ec : ecs)
                                                local.add(ec.getCertificate());

                                            // TODO: check digitalSignature/nonRepudiation key usage
                                            // https://datatracker.ietf.org/doc/html/rfc3850#section-4.4.2

                                            for (X509Certificate c : certs) {
                                                boolean[] usage = c.getKeyUsage();
                                                boolean keyCertSign = (usage != null && usage.length > 5 && usage[5]);
                                                boolean selfSigned = c.getIssuerX500Principal().equals(c.getSubjectX500Principal());
                                                if (keyCertSign && !selfSigned && ks.getCertificateAlias(c) == null) {
                                                    boolean found = false;
                                                    String issuer = (c.getIssuerDN() == null ? "" : c.getIssuerDN().getName());
                                                    EntityCertificate record = EntityCertificate.from(c, true, issuer);
                                                    for (EntityCertificate ec : ecs)
                                                        if (ec.fingerprint.equals(record.fingerprint)) {
                                                            found = true;
                                                            break;
                                                        }

                                                    if (!found) {
                                                        Log.i("Storing certificate subject=" + record.subject);
                                                        local.add(record.getCertificate());
                                                        db.certificate().insertCertificate(record);
                                                    }
                                                }
                                            }
                                        } catch (Throwable ex) {
                                            Log.e(ex);
                                        }

                                        // Intermediate certificates
                                        Log.i("Intermediate certificates=" + local.size());
                                        PKIXBuilderParameters params = new PKIXBuilderParameters(ks, target);
                                        CertStoreParameters intermediates = new CollectionCertStoreParameters(local);
                                        params.addCertStore(CertStore.getInstance("Collection", intermediates));
                                        params.setRevocationEnabled(false);
                                        params.setDate(signingTime);

                                        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
                                        CertPathBuilderResult path = builder.build(params);

                                        CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
                                        cpv.validate(path.getCertPath(), params);

                                        List<X509Certificate> pcerts = new ArrayList<>();
                                        for (Certificate c : path.getCertPath().getCertificates())
                                            if (c instanceof X509Certificate)
                                                pcerts.add((X509Certificate) c);
                                        if (path instanceof PKIXCertPathValidatorResult) {
                                            X509Certificate root = ((PKIXCertPathValidatorResult) path).getTrustAnchor().getTrustedCert();
                                            if (root != null)
                                                pcerts.add(root);
                                        }

                                        args.putStringArrayList("trace", getTrace(pcerts, ks));

                                        boolean valid = true;
                                        for (Certificate pcert : pcerts)
                                            try {
                                                ((X509Certificate) pcert).checkValidity(signingTime);
                                            } catch (CertificateException ex) {
                                                Log.w(ex);
                                                valid = false;
                                                break;
                                            }

                                        args.putBoolean("valid", valid);
                                        if (known)
                                            db.message().setMessageVerified(message.id, true);
                                    } catch (Throwable ex) {
                                        Log.w(ex);
                                        args.putString("reason", ex.getMessage());
                                        args.putStringArrayList("trace", getTrace(certs, ks));
                                    }

                                    result = cert;
                                    break;
                                } else
                                    Log.w("Signature invalid");
                            } catch (CMSException ex) {
                                Log.w(ex);
                                args.putString("reason", ex.getMessage());
                            }
                        }
                        if (result != null)
                            break;
                    }

                    if (result == null && !args.containsKey("reason"))
                        args.putString("reason", matching
                                ? "Signature could not be verified"
                                : "Certificates and signatures do not match");

                    if (is != null)
                        decodeMessage(context, is, message, args);
                } else {
                    // Check alias
                    String alias = args.getString("alias");
                    if (alias == null)
                        throw new IllegalArgumentException("Key alias missing");

                    // Get private key
                    PrivateKey privkey = KeyChain.getPrivateKey(context, alias);
                    if (privkey == null)
                        throw new IllegalArgumentException("Private key missing");

                    // Get public key
                    X509Certificate[] chain = KeyChain.getCertificateChain(context, alias);
                    if (chain == null || chain.length == 0)
                        throw new IllegalArgumentException("Public key missing");

                    // Get last encrypted message
                    File input = null;
                    List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                    for (EntityAttachment attachment : attachments)
                        if (EntityAttachment.SMIME_MESSAGE.equals(attachment.encryption)) {
                            if (!attachment.available)
                                throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                            input = attachment.getFile(context);
                        }

                    if (input == null)
                        throw new IllegalArgumentException("Encrypted message missing");

                    int count = -1;
                    boolean decoded = false;
                    Throwable last = null;
                    while (!decoded)
                        try (FileInputStream fis = new FileInputStream(input)) {
                            // Create parser
                            CMSEnvelopedDataParser envelopedData = new CMSEnvelopedDataParser(fis);

                            // Get recipient info
                            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(privkey);
                            Collection<RecipientInformation> recipients = envelopedData.getRecipientInfos().getRecipients(); // KeyTransRecipientInformation

                            // Find recipient
                            if (count < 0) {
                                BigInteger serialno = chain[0].getSerialNumber();
                                for (RecipientInformation recipientInfo : recipients) {
                                    KeyTransRecipientId recipientId = (KeyTransRecipientId) recipientInfo.getRID();
                                    if (serialno != null && serialno.equals(recipientId.getSerialNumber())) {
                                        try {
                                            InputStream is = recipientInfo.getContentStream(recipient).getContentStream();
                                            decodeMessage(context, is, message, args);
                                            decoded = true;

                                            String algo;
                                            try {
                                                DefaultAlgorithmNameFinder af = new DefaultAlgorithmNameFinder();
                                                algo = af.getAlgorithmName(envelopedData.getContentEncryptionAlgorithm());
                                            } catch (Throwable ex) {
                                                Log.e(ex);
                                                algo = envelopedData.getEncryptionAlgOID();
                                            }
                                            Log.i("Encryption algo=" + algo);
                                            args.putString("algo", algo);
                                        } catch (CMSException ex) {
                                            Log.w(ex);
                                            last = ex;
                                        }
                                        break; // only one try
                                    }
                                }
                            } else {
                                List<RecipientInformation> list = new ArrayList<>(recipients);
                                if (count < list.size()) {
                                    RecipientInformation recipientInfo = list.get(count);
                                    try {
                                        InputStream is = recipientInfo.getContentStream(recipient).getContentStream();
                                        decodeMessage(context, is, message, args);
                                        decoded = true;
                                        break;
                                    } catch (CMSException ex) {
                                        Log.w(ex);
                                        last = ex;
                                    }
                                } else
                                    break; // out of recipients
                            }

                            count++;
                        }

                    if (!decoded) {
                        if (message.identity != null)
                            db.identity().setIdentitySignKeyAlias(message.identity, null);
                        String msg = (last == null ? null : last.getMessage());
                        throw new IllegalArgumentException(context.getString(R.string.title_unknown_key) +
                                (TextUtils.isEmpty(msg) ? "" : " (" + msg + ")"));
                    }
                }

                return result;
            }

            @Override
            protected void onExecuted(final Bundle args, X509Certificate cert) {
                int type = args.getInt("type");
                if (EntityMessage.SMIME_SIGNONLY.equals(type)) {
                    if (cert == null) {
                        String message;
                        String reason = args.getString("reason");
                        if (TextUtils.isEmpty(reason))
                            message = getString(R.string.title_signature_invalid);
                        else
                            message = getString(R.string.title_signature_invalid_reason, reason);
                        Helper.setSnackbarOptions(
                                        Snackbar.make(view, message, Snackbar.LENGTH_LONG))
                                .show();
                    } else
                        try {
                            boolean auto = args.getBoolean("auto");
                            String sender = args.getString("sender");
                            Date time = (Date) args.getSerializable("time");
                            boolean known = args.getBoolean("known");
                            boolean valid = args.getBoolean("valid");
                            String reason = args.getString("reason");
                            String algo = args.getString("algo");
                            final ArrayList<String> trace = args.getStringArrayList("trace");
                            EntityCertificate record = EntityCertificate.from(cert, null);

                            if (time == null)
                                time = new Date();

                            boolean match = false;
                            List<String> emails = EntityCertificate.getEmailAddresses(cert);
                            for (String email : emails)
                                if (email.equalsIgnoreCase(sender)) {
                                    match = true;
                                    break;
                                }

                            if (known && !record.isExpired(time) && match && valid)
                                Helper.setSnackbarOptions(Snackbar.make(view, R.string.title_signature_valid, Snackbar.LENGTH_LONG))
                                        .show();
                            else if (!auto) {
                                LayoutInflater inflator = LayoutInflater.from(getContext());
                                View dview = inflator.inflate(R.layout.dialog_certificate, null);
                                TextView tvCertificateInvalid = dview.findViewById(R.id.tvCertificateInvalid);
                                TextView tvCertificateReason = dview.findViewById(R.id.tvCertificateReason);
                                TextView tvSender = dview.findViewById(R.id.tvSender);
                                TextView tvEmail = dview.findViewById(R.id.tvEmail);
                                TextView tvEmailInvalid = dview.findViewById(R.id.tvEmailInvalid);
                                TextView tvSubject = dview.findViewById(R.id.tvSubject);
                                ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
                                TextView tvAfter = dview.findViewById(R.id.tvAfter);
                                TextView tvBefore = dview.findViewById(R.id.tvBefore);
                                TextView tvExpired = dview.findViewById(R.id.tvExpired);
                                TextView tvAlgorithm = dview.findViewById(R.id.tvAlgorithm);

                                tvCertificateInvalid.setVisibility(valid ? View.GONE : View.VISIBLE);
                                tvCertificateReason.setText(reason);
                                tvCertificateReason.setVisibility(reason == null ? View.GONE : View.VISIBLE);
                                tvSender.setText(sender);
                                tvEmail.setText(TextUtils.join(",", emails));
                                tvEmailInvalid.setVisibility(match ? View.GONE : View.VISIBLE);
                                tvSubject.setText(record.subject);

                                DateFormat TF = Helper.getDateTimeInstance(getContext(), SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                                tvAfter.setText(record.after == null ? null : TF.format(record.after));
                                tvBefore.setText(record.before == null ? null : TF.format(record.before));
                                tvExpired.setVisibility(record.isExpired(time) ? View.VISIBLE : View.GONE);

                                if (!TextUtils.isEmpty(algo))
                                    algo = algo.replace("WITH", "/");
                                tvAlgorithm.setText(algo);

                                ibInfo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        StringBuilder sb = new StringBuilder();
                                        for (int i = 0; i < trace.size(); i++) {
                                            if (i > 0)
                                                sb.append("\n\n");
                                            sb.append(i + 1).append(") ").append(trace.get(i));
                                        }

                                        new AlertDialog.Builder(v.getContext())
                                                .setMessage(sb.toString())
                                                .show();
                                    }
                                });
                                ibInfo.setVisibility(trace != null && trace.size() > 0 ? View.VISIBLE : View.GONE);

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                        .setView(dview)
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .setNeutralButton(R.string.title_info, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Helper.viewFAQ(getContext(), 12);
                                            }
                                        });

                                if (!TextUtils.isEmpty(sender) && !known && emails.size() > 0)
                                    builder.setPositiveButton(R.string.title_signature_store, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                args.putByteArray("encoded", cert.getEncoded());

                                                new SimpleTask<Void>() {
                                                    @Override
                                                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                                                        long id = args.getLong("id");
                                                        byte[] encoded = args.getByteArray("encoded");

                                                        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                                                                .generateCertificate(new ByteArrayInputStream(encoded));

                                                        DB db = DB.getInstance(context);
                                                        EntityMessage message = db.message().getMessage(id);
                                                        if (message == null)
                                                            return null;

                                                        String fingerprint = EntityCertificate.getFingerprintSha256(cert);
                                                        List<String> emails = EntityCertificate.getEmailAddresses(cert);
                                                        for (String email : emails) {
                                                            EntityCertificate record = db.certificate().getCertificate(fingerprint, email);
                                                            if (record == null) {
                                                                record = EntityCertificate.from(cert, email);
                                                                record.id = db.certificate().insertCertificate(record);
                                                            }
                                                        }

                                                        db.message().setMessageVerified(message.id, true);

                                                        return null;
                                                    }

                                                    @Override
                                                    protected void onException(Bundle args, Throwable ex) {
                                                        Log.unexpectedError(getParentFragmentManager(), ex);
                                                    }
                                                }.execute(FragmentMessages.this, args, "certificate:store");
                                            } catch (Throwable ex) {
                                                Log.unexpectedError(FragmentMessages.this, ex);
                                            }
                                        }
                                    });

                                builder.show();
                            }
                        } catch (Throwable ex) {
                            Helper.setSnackbarOptions(
                                            Snackbar.make(view, Log.formatThrowable(ex), Snackbar.LENGTH_LONG))
                                    .show();
                        }
                } else if (EntityMessage.SMIME_SIGNENCRYPT.equals(type)) {
                    String algo = args.getString("algo");
                    if (!TextUtils.isEmpty(algo))
                        Helper.setSnackbarOptions(
                                        Snackbar.make(view, algo, Snackbar.LENGTH_LONG))
                                .show();
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                boolean auto = args.getBoolean("auto");
                if (auto)
                    return;

                if (ex instanceof IllegalArgumentException ||
                        ex instanceof CMSException || ex instanceof KeyChainException)
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG))
                            .show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }

            private void decodeMessage(Context context, InputStream is, EntityMessage message, Bundle args) throws MessagingException, IOException {
                String alias = args.getString("alias");
                boolean duplicate = args.getBoolean("duplicate");

                // Decode message
                Properties props = MessageHelper.getSessionProperties(true);
                Session isession = Session.getInstance(props, null);
                MimeMessage imessage = new MimeMessage(isession, is);
                MessageHelper helper = new MessageHelper(imessage, context);
                MessageHelper.MessageParts parts = helper.getMessageParts();
                String protect_subject = parts.getProtectedSubject();

                // Write decrypted body
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean debug = prefs.getBoolean("debug", false);
                boolean download_plain = prefs.getBoolean("download_plain", false);
                String html = parts.getHtml(context, download_plain);

                if (html == null && debug) {
                    int textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);
                    SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                    MessageHelper.getStructure(imessage, ssb, 0, textColorLink);
                    html = HtmlHelper.toHtml(ssb, context);
                }

                Helper.writeText(message.getFile(context), html);
                Log.i("s/mime html=" + (html == null ? null : html.length()));

                String text = HtmlHelper.getFullText(context, html);
                message.preview = HtmlHelper.getPreview(text);
                message.language = HtmlHelper.getLanguage(context, message.subject, text);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (protect_subject != null)
                        db.message().setMessageSubject(message.id, protect_subject);

                    db.message().setMessageContent(message.id,
                            true,
                            message.language,
                            parts.isPlainOnly(download_plain),
                            message.preview,
                            message.warning);

                    // Remove existing attachments
                    db.attachment().deleteAttachments(message.id, new int[]{
                            EntityAttachment.SMIME_MESSAGE,
                            EntityAttachment.SMIME_SIGNED_DATA
                    });

                    // Add decrypted attachments
                    boolean signedData = false;
                    List<EntityAttachment> remotes = parts.getAttachments();
                    for (int index = 0; index < remotes.size(); index++) {
                        EntityAttachment remote = remotes.get(index);
                        remote.message = message.id;
                        remote.sequence = index + 1;
                        remote.id = db.attachment().insertAttachment(remote);
                        Log.i("s/mime attachment=" + remote);

                        try {
                            parts.downloadAttachment(context, index, remote, null);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }

                        if (!parts.hasBody() && remotes.size() == 1 &&
                                ("application/pkcs7-mime".equals(remote.type) ||
                                        "application/x-pkcs7-mime".equals(remote.type)))
                            try (FileInputStream fos = new FileInputStream(remote.getFile(context))) {
                                new CMSSignedData(fos).getSignedContent().getContent();
                                signedData = true;
                                remote.encryption = EntityAttachment.SMIME_SIGNED_DATA;
                                db.attachment().setEncryption(remote.id, remote.encryption);
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                    }

                    boolean pep = checkPep(message, remotes, context);

                    db.message().setMessageEncrypt(message.id,
                            signedData ? EntityMessage.SMIME_SIGNONLY : parts.getEncryption());
                    db.message().setMessageRevision(message.id, pep || protect_subject == null ? 1 : -1);
                    db.message().setMessageStored(message.id, new Date().getTime());
                    db.message().setMessageFts(message.id, false);

                    if (alias != null && !duplicate && message.identity != null)
                        db.identity().setIdentitySignKeyAlias(message.identity, alias);

                    db.setTransactionSuccessful();
                } catch (SQLiteConstraintException ex) {
                    // Message removed
                    Log.w(ex);
                } finally {
                    db.endTransaction();
                }

                WorkerFts.init(context, false);
            }

            private ArrayList<String> getTrace(List<X509Certificate> certs, KeyStore ks) {
                // https://tools.ietf.org/html/rfc5280#section-4.2.1.3
                ArrayList<String> trace = new ArrayList<>();
                for (Certificate c : certs)
                    try {
                        X509Certificate cert = (X509Certificate) c;
                        boolean[] usage = cert.getKeyUsage();
                        boolean digitalSignature = (usage != null && usage.length > 0 && usage[0]);
                        boolean nonRepudiation = (usage != null && usage.length > 1 && usage[1]);
                        boolean keyEncipherment = (usage != null && usage.length > 2 && usage[2]);
                        boolean dataEncipherment = (usage != null && usage.length > 3 && usage[4]);
                        boolean keyAgreement = (usage != null && usage.length > 4 && usage[4]);
                        boolean keyCertSign = (usage != null && usage.length > 5 && usage[5]);
                        boolean cRLSign = (usage != null && usage.length > 6 && usage[6]);
                        boolean encipherOnly = (usage != null && usage.length > 7 && usage[7]);
                        boolean decipherOnly = (usage != null && usage.length > 8 && usage[8]);
                        boolean selfSigned = cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal());
                        EntityCertificate record = EntityCertificate.from(cert, null);
                        trace.add(record.subject +
                                " (" + (selfSigned ? "selfSigned" : cert.getIssuerX500Principal()) + ")" +
                                (digitalSignature ? " (digitalSignature)" : "") +
                                (nonRepudiation ? " (nonRepudiation)" : "") +
                                (keyEncipherment ? " (keyEncipherment)" : "") +
                                (dataEncipherment ? " (dataEncipherment)" : "") +
                                (keyAgreement ? " (keyAgreement)" : "") +
                                (keyCertSign ? " (keyCertSign)" : "") +
                                (cRLSign ? " (cRLSign)" : "") +
                                (encipherOnly ? " (encipherOnly)" : "") +
                                (decipherOnly ? " (decipherOnly)" : "") +
                                (ks != null && ks.getCertificateAlias(cert) != null ? " (Android)" : ""));
                    } catch (Throwable ex) {
                        Log.e(ex);
                        trace.add(new ThrowableWrapper(ex).toSafeString());
                    }
                return trace;
            }
        }.serial().execute(this, args, "decrypt:s/mime");
    }

    private static boolean checkPep(EntityMessage message, List<EntityAttachment> remotes, Context context) {
        DB db = DB.getInstance(context);
        for (EntityAttachment remote : remotes)
            if ("message/rfc822".equals(remote.getMimeType()))
                try {
                    Properties props = MessageHelper.getSessionProperties(true);
                    Session isession = Session.getInstance(props, null);

                    MimeMessage imessage;
                    try (InputStream fis = new FileInputStream(remote.getFile(context))) {
                        imessage = new MimeMessage(isession, fis);
                    }

                    String[] xpep = imessage.getHeader("X-pEp-Wrapped-Message-Info");
                    if (xpep == null || xpep.length == 0 || !"INNER".equalsIgnoreCase(xpep[0]))
                        continue;

                    MessageHelper helper = new MessageHelper(imessage, context);
                    String subject = helper.getSubject();
                    String html = helper.getMessageParts().getHtml(context);

                    if (!TextUtils.isEmpty(html))
                        Helper.writeText(message.getFile(context), html);

                    try {
                        db.beginTransaction();

                        if (!TextUtils.isEmpty(subject))
                            db.message().setMessageSubject(message.id, subject);

                        // Prevent showing the embedded message
                        db.attachment().setType(remote.id, "application/octet-stream");

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return true;
                } catch (Throwable ex) {
                    Log.e(ex);
                }
        return false;
    }

    private void onDelete(long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);

                // Cancel send operation
                EntityOperation operation = db.operation().getOperation(id, EntityOperation.SEND);
                if (operation != null)
                    if ("executing".equals(operation.state))
                        return null;
                    else
                        db.operation().deleteOperation(operation.id);

                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    EntityAccount account = db.account().getAccount(message.account);
                    if (account == null)
                        return null;

                    EntityFolder folder = db.folder().getFolder(message.folder);
                    if (folder == null)
                        return null;

                    if (EntityFolder.OUTBOX.equals(folder.type)) {
                        db.message().deleteMessage(id);

                        db.folder().setFolderError(message.folder, null);
                        if (message.identity != null)
                            db.identity().setIdentityError(message.identity, null);
                    } else if (message.uid == null && account.protocol == EntityAccount.TYPE_IMAP) {
                        db.message().deleteMessage(id);
                        db.folder().setFolderError(message.folder, null);
                    } else
                        EntityOperation.queue(context, message, EntityOperation.DELETE);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "delete");

                NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                nm.cancel("send:" + id, NotificationHelper.NOTIFICATION_TAGGED);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (viewType == AdapterMessage.ViewType.THREAD) {
                    PagedList<TupleMessageEx> messages = adapter.getCurrentList();
                    if (messages != null) {
                        Log.i("Eval delete messages=" + messages.size() + " id=" + id);
                        handleThreadActions(adapter.getCurrentList(), null, Arrays.asList(id));
                    }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:delete");
    }

    private void onDelete(Bundle args) {
        if (selectionTracker != null)
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

                ServiceSynchronize.eval(context, "delete");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (viewType == AdapterMessage.ViewType.THREAD) {
                    long[] ids = args.getLongArray("ids");
                    PagedList<TupleMessageEx> messages = adapter.getCurrentList();
                    if (messages != null && ids.length > 0) {
                        Log.i("Eval thread messages=" + messages.size() + " ids=" + ids.length);
                        handleThreadActions(adapter.getCurrentList(), null, Helper.fromLongArray(ids));
                    }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:delete:execute");
    }

    private void onJunk(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws JSONException {
                long id = args.getLong("id");
                boolean block_sender = args.getBoolean("block_sender");
                boolean block_domain = args.getBoolean("block_domain");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    List<TupleIdentityEx> identities = db.identity().getComposableIdentities(null);
                    if (message.fromSelf(identities))
                        return null;

                    EntityAccount account = db.account().getAccount(message.account);
                    if (account == null || account.protocol != EntityAccount.TYPE_IMAP)
                        return null;

                    if (block_sender)
                        EntityContact.update(context,
                                message.account, message.identity, message.from,
                                EntityContact.TYPE_JUNK, message.received);

                    EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                    if (junk == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_junk_folder));

                    if (!message.folder.equals(junk.id))
                        EntityOperation.queue(context, message, EntityOperation.MOVE, junk.id, null, null, true);

                    if (block_domain) {
                        List<EntityRule> rules = EntityRule.blockSender(context, message, junk, block_domain);
                        for (EntityRule rule : rules) {
                            if (message.folder.equals(junk.id)) {
                                EntityFolder inbox = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                                if (inbox == null)
                                    continue;
                                rule.folder = inbox.id;
                            }
                            rule.id = db.rule().insertRule(rule);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "junk");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException) {
                    Snackbar snackbar = Helper.setSnackbarOptions(
                            Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_INDEFINITE));
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("target", "accounts"));
                        }
                    });
                    snackbar.show();
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:junk");
    }

    private void onBlockSenders(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long[] ids = args.getLongArray("ids");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<TupleIdentityEx> identities = db.identity().getComposableIdentities(null);

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null || message.fromSelf(identities))
                            continue;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null || account.protocol != EntityAccount.TYPE_POP)
                            continue;

                        EntityContact.update(context,
                                message.account, message.identity, message.from,
                                EntityContact.TYPE_JUNK, message.received);

                        db.message().deleteMessage(message.id);
                    }

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
        }.execute(this, args, "messages:block");
    }

    private void onInsertCalendar(Bundle args) {
        new SimpleTask<Long>() {
            @Override
            protected Long onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("message");
                String selectedAccount = args.getString("account");
                String selectedName = args.getString("name");

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                if (attachments == null)
                    return null;

                EntityAttachment calendar = null;
                for (EntityAttachment attachment : attachments)
                    if (attachment.available &&
                            "text/calendar".equals(attachment.getMimeType()))
                        calendar = attachment;

                if (calendar == null)
                    return null;

                ICalendar icalendar = CalendarHelper.parse(context, calendar.getFile(context));
                List<VEvent> events = icalendar.getEvents();
                if (events == null || events.size() == 0)
                    return null;

                VEvent event = events.get(0);
                int status = CalendarContract.Events.STATUS_TENTATIVE;
                if (event.getStatus() != null &&
                        Status.CONFIRMED.equals(event.getStatus().getValue()))
                    status = CalendarContract.Events.STATUS_CONFIRMED;

                return CalendarHelper.insert(context, icalendar, event, status,
                        selectedAccount, selectedName, message);
            }

            @Override
            protected void onExecuted(Bundle args, Long eventId) {
                if (eventId == null)
                    return;

                // https://developer.android.com/guide/topics/providers/calendar-provider.html#intent-view
                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
                startActivity(intent);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "insert:calendar");
    }

    private void onEditSubject(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                String subject = args.getString("subject");

                DB db = DB.getInstance(context);

                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                if (TextUtils.isEmpty(subject))
                    subject = null;

                EntityOperation.queue(context, message, EntityOperation.SUBJECT, subject);

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "edit:subject");
    }

    private void onMoveAskAcross(final ArrayList<MessageTarget> result) {
        boolean across = false;
        for (MessageTarget target : result)
            if (target.isAcross()) {
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
            ask.show(getParentFragmentManager(), "messages:move:across");
        } else
            moveAskConfirmed(result);
    }

    private void onColor(long id, int color) {
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putInt("color", color);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(final Context context, Bundle args) {
                long id = args.getLong("id");
                Integer color = args.getInt("color");

                if (color == Color.TRANSPARENT)
                    color = null;

                final DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    EntityOperation.queue(context, message, EntityOperation.FLAG, true, color);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "flag");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:color");
    }

    private void onSnoozeOrHide(Bundle args) {
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
                boolean hide = args.getBoolean("hide");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean flag_snoozed = prefs.getBoolean("flag_snoozed", false);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return wakeup;

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            account, thread, threading ? null : id, null);
                    for (EntityMessage threaded : messages) {
                        db.message().setMessageUnsnoozed(threaded.id, false);
                        db.message().setMessageUiIgnored(threaded.id, true);
                        if (hide) {
                            db.message().setMessageSnoozed(threaded.id, wakeup);
                            EntityMessage.snooze(context, threaded.id, wakeup);
                        } else {
                            if (threaded.id.equals(id)) {
                                db.message().setMessageSnoozed(threaded.id, wakeup);
                                EntityMessage.snooze(context, threaded.id, wakeup);
                                if (wakeup != null)
                                    EntityOperation.queue(context, threaded, EntityOperation.SEEN, true);
                            } else {
                                db.message().setMessageSnoozed(threaded.id, wakeup == null ? null : Long.MAX_VALUE); // show/hide
                                EntityMessage.snooze(context, threaded.id, null);
                            }
                            if (flag_snoozed && threaded.folder.equals(message.folder))
                                EntityOperation.queue(context, threaded, EntityOperation.FLAG, wakeup != null);
                        }
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:snooze");
    }

    private void onSnoozeSelection(Bundle args) {
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        long duration = args.getLong("duration");
        long time = args.getLong("time");
        args.putLong("wakeup", duration == 0 ? -1 : time);
        args.putLongArray("ids", getSelection());

        if (selectionTracker != null)
            selectionTracker.clearSelection();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                Long wakeup = args.getLong("wakeup");
                if (wakeup < 0)
                    wakeup = null;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean flag_snoozed = prefs.getBoolean("flag_snoozed", false);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, null);
                        for (EntityMessage threaded : messages) {
                            db.message().setMessageUnsnoozed(threaded.id, false);
                            db.message().setMessageUiIgnored(threaded.id, true);
                            if (threaded.id.equals(id)) {
                                db.message().setMessageSnoozed(threaded.id, wakeup);
                                EntityMessage.snooze(context, threaded.id, wakeup);
                                if (wakeup != null)
                                    EntityOperation.queue(context, threaded, EntityOperation.SEEN, true);
                            } else {
                                db.message().setMessageSnoozed(threaded.id, wakeup == null ? null : Long.MAX_VALUE); // show/hide
                                EntityMessage.snooze(context, threaded.id, null);
                            }
                            if (flag_snoozed && threaded.folder.equals(message.folder))
                                EntityOperation.queue(context, threaded, EntityOperation.FLAG, wakeup != null);
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
                Log.unexpectedError(getParentFragmentManager(), ex);
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

                    EntityAccount sourceAccount = db.account().getAccount(message.account);
                    if (sourceAccount == null)
                        return result;

                    EntityFolder targetFolder = db.folder().getFolder(tid);
                    if (targetFolder == null)
                        return result;

                    EntityAccount targetAccount = db.account().getAccount(targetFolder.account);
                    if (targetAccount == null)
                        return result;

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            message.account, message.thread, threading && similar ? null : id, message.folder);
                    for (EntityMessage threaded : messages)
                        if (copy)
                            EntityOperation.queue(context, message, EntityOperation.COPY, tid);
                        else {
                            EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                            if (sourceFolder == null ||
                                    sourceFolder.read_only ||
                                    sourceFolder.id.equals(targetFolder.id))
                                continue;
                            result.add(new MessageTarget(context, threaded, sourceAccount, sourceFolder, targetAccount, targetFolder));
                        }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (copy)
                    ServiceSynchronize.eval(context, "copy");

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                boolean copy = args.getBoolean("copy");
                if (copy)
                    ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
                else
                    moveAsk(result, true);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:move");
    }

    private void onPrint(Bundle args) {
        FragmentDialogPrint.print((ActivityBase) getActivity(), getParentFragmentManager(), args);
    }

    private void onEmptyFolder(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");
                String type = args.getString("type");
                EntityLog.log(context, "Empty account=" + account + " type=" + type);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<EntityAccount> accounts;
                    if (account < 0)
                        accounts = db.account().getSynchronizingAccounts(null);
                    else {
                        EntityAccount account = db.account().getAccount(aid);
                        if (account == null)
                            return null;
                        accounts = Arrays.asList(account);
                    }

                    for (EntityAccount account : accounts) {
                        EntityFolder folder = db.folder().getFolderByType(account.id, type);
                        if (folder == null)
                            continue;

                        EntityLog.log(context,
                                "Empty account=" + account.name + " folder=" + folder.name + " count=" + folder.total);

                        List<Long> ids = db.message().getMessageByFolder(folder.id);
                        for (Long id : ids) {
                            EntityMessage message = db.message().getMessage(id);
                            if (message == null)
                                continue;

                            if (account.protocol == EntityAccount.TYPE_POP)
                                db.message().setMessageUiHide(message.id, true);
                            else {
                                if (message.uid == null)
                                    db.message().deleteMessage(id);
                                else
                                    db.message().setMessageUiHide(message.id, true);
                            }
                        }

                        EntityOperation.queue(context, folder, EntityOperation.PURGE);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "purge");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                Helper.performHapticFeedback(view, HapticFeedbackConstants.CONFIRM);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:delete");
    }

    private void onBoundaryRetry() {
        ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);
        model.retry(viewType);
    }

    private void onPickContact(Uri contactUri) {
        String email = kv.get("email");

        // This requires contacts permission
        ContentResolver resolver = getContext().getContentResolver();
        Uri lookupUri = ContactsContract.Contacts.getLookupUri(resolver, contactUri);

        Intent edit = new Intent();
        edit.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
        edit.setAction(Intent.ACTION_EDIT);
        edit.setDataAndTypeAndNormalize(lookupUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        startActivity(edit);
    }

    static void search(
            final Context context, final LifecycleOwner owner, final FragmentManager manager,
            long account, long folder, boolean server, BoundaryCallbackMessages.SearchCriteria criteria) {
        if (criteria.onServer()) {
            if (account > 0 && folder > 0)
                server = true;
            else {
                ToastEx.makeText(context, R.string.title_complex_search, Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            manager.popBackStack("search", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (manager.isDestroyed())
            return;

        DB db = DB.getInstance(context);
        Helper.getUIExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    db.message().resetSearch();
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);
        args.putBoolean("server", server);
        args.putSerializable("criteria", criteria);

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("search");
        fragmentTransaction.commitAllowingStateLoss();
    }

    static void searchContact(Context context, LifecycleOwner owner, FragmentManager fm, long message, boolean sender_only) {
        Bundle args = new Bundle();
        args.putLong("id", message);

        new SimpleTask<Address[]>() {
            @Override
            protected Address[] onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;

                EntityFolder folder = db.folder().getFolder(message.folder);
                if (folder == null)
                    return null;

                boolean ingoing = false;
                boolean outgoing = EntityFolder.isOutgoing(folder.type);

                if (message.identity != null) {
                    EntityIdentity identity = db.identity().getIdentity(message.identity);
                    if (identity == null)
                        return null;

                    if (message.to != null)
                        for (Address recipient : message.to)
                            if (identity.similarAddress(recipient)) {
                                ingoing = true;
                                break;
                            }

                    if (message.from != null)
                        for (Address sender : message.from)
                            if (identity.similarAddress(sender)) {
                                outgoing = true;
                                break;
                            }
                }

                if (outgoing && ingoing && message.reply != null)
                    return message.reply;

                return (outgoing ? message.to : message.from);
            }

            @Override
            protected void onExecuted(Bundle args, Address[] addresses) {
                if (addresses == null || addresses.length == 0)
                    return;

                String query = ((InternetAddress) addresses[0]).getAddress();
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_SEARCH_ADDRESS)
                                .putExtra("account", -1L)
                                .putExtra("folder", -1L)
                                .putExtra("query", query)
                                .putExtra("sender_only", sender_only));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(fm, ex);
            }
        }.execute(context, owner, args, "message:search");
    }

    private static class ActionData {
        private boolean delete; // Selects action
        private boolean forever; // Selects icon
        private boolean trashable;
        private boolean snoozable;
        private boolean archivable;
    }

    private class ReplyData {
        List<TupleIdentityEx> identities;
        List<EntityAnswer> answers;
        List<InternetAddress> forwarded;
    }

    private static class MoreResult {
        boolean seen;
        boolean unseen;
        boolean visible;
        boolean hidden;
        boolean flagged;
        boolean unflagged;
        Integer importance;
        Boolean hasInbox;
        Boolean hasArchive;
        Boolean hasTrash;
        Boolean hasJunk;
        Boolean isInbox;
        Boolean isSent;
        Boolean isArchive;
        Boolean isTrash;
        Boolean isJunk;
        Boolean isDrafts;
        boolean hasImap;
        boolean hasPop;
        Boolean leave_on_server;
        Boolean leave_deleted;
        boolean read_only;
        List<Long> folders;
        List<EntityAccount> imapAccounts;
        EntityAccount copyto;
        TupleMessageEx single;

        boolean canInbox() {
            if (read_only)
                return false;
            return ((hasInbox && !isInbox) ||
                    (leave_deleted != null && leave_deleted && isTrash));
        }

        boolean canArchive() {
            if (read_only)
                return false;
            return (hasArchive && !isArchive);
        }

        boolean canJunk() {
            if (read_only)
                return false;
            return (hasJunk && !isJunk && !isDrafts) ||
                    (hasPop && isInbox && !isSent && !hasImap);
        }

        boolean canTrash() {
            if (read_only)
                return false;
            return (!isTrash && hasTrash && !isJunk) ||
                    (hasPop && Boolean.TRUE.equals(leave_deleted) && isInbox);
        }

        boolean canDelete() {
            if (read_only)
                return false;
            return (!hasPop || !Boolean.TRUE.equals(leave_deleted) || (isTrash || isDrafts || isSent));
        }

        boolean canMove() {
            if (read_only)
                return false;
            return (!hasPop || Boolean.TRUE.equals(leave_on_server)) && (imapAccounts.size() > 0);
        }

        static MoreResult get(Context context, long[] ids, boolean threading, boolean all) {
            Map<Long, EntityAccount> accounts = new HashMap<>();
            Map<Long, EntityFolder> folders = new HashMap<>();

            DB db = DB.getInstance(context);

            MoreResult result = new MoreResult();
            result.folders = new ArrayList<>();

            if (!all && ids.length > MAX_MORE) {
                result.seen = true;
                result.unseen = true;
                result.flagged = true;
                result.unflagged = true;
                result.importance = -1;
                result.visible = true;
                result.hidden = true;
            }

            for (long id : ids) {
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    continue;

                EntityAccount account = accounts.get(message.account);
                if (account == null) {
                    account = db.account().getAccount(message.account);
                    if (account == null)
                        continue;
                    accounts.put(account.id, account);
                }

                EntityFolder folder = folders.get(message.folder);
                if (folder == null) {
                    folder = db.folder().getFolder(message.folder);
                    if (folder == null)
                        continue;
                    if (folder.read_only)
                        result.read_only = true;
                    folders.put(folder.id, folder);
                }

                if (!result.folders.contains(message.folder))
                    result.folders.add(message.folder);

                boolean isInbox = EntityFolder.INBOX.equals(folder.type);
                boolean isArchive = EntityFolder.ARCHIVE.equals(folder.type);
                boolean isTrash = (EntityFolder.TRASH.equals(folder.type));
                boolean isJunk = EntityFolder.JUNK.equals(folder.type);
                boolean isDrafts = EntityFolder.DRAFTS.equals(folder.type);
                boolean isSent = EntityFolder.SENT.equals(folder.type);

                if (account.protocol == EntityAccount.TYPE_POP && isSent)
                    isInbox = true;

                result.isInbox = (result.isInbox == null ? isInbox : result.isInbox && isInbox);
                result.isSent = (result.isSent == null ? isSent : result.isSent && isSent);
                result.isArchive = (result.isArchive == null ? isArchive : result.isArchive && isArchive);
                result.isTrash = (result.isTrash == null ? isTrash : result.isTrash && isTrash);
                result.isJunk = (result.isJunk == null ? isJunk : result.isJunk && isJunk);
                result.isDrafts = (result.isDrafts == null ? isDrafts : result.isDrafts && isDrafts);

                if (result.seen && result.unseen &&
                        result.flagged && result.unflagged &&
                        result.importance == -1 &&
                        result.visible && result.hidden)
                    continue;

                if (message.ui_seen)
                    result.seen = true;
                if (!message.ui_flagged)
                    result.unflagged = true;

                List<EntityMessage> messages = db.message().getMessagesByThread(
                        message.account, message.thread, threading ? null : id, null);
                for (EntityMessage threaded : messages) {
                    if (threaded.folder.equals(message.folder))
                        if (!threaded.ui_seen)
                            result.unseen = true;

                    if (threaded.ui_flagged)
                        result.flagged = true;

                    int i = (message.importance == null ? EntityMessage.PRIORITIY_NORMAL : message.importance);
                    if (result.importance == null)
                        result.importance = i;
                    else if (!result.importance.equals(i))
                        result.importance = -1; // mixed

                    if (threaded.folder.equals(message.folder))
                        if (message.ui_snoozed == null)
                            result.visible = true;
                        else
                            result.hidden = true;
                }

                if (ids.length == 1)
                    result.single = db.message().getMessageEx(id);
            }

            for (EntityAccount account : accounts.values()) {
                boolean hasInbox = false;
                boolean hasArchive = false;
                boolean hasTrash = false;
                boolean hasJunk = false;

                if (account.protocol == EntityAccount.TYPE_IMAP) {
                    result.hasImap = true;

                    EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                    EntityFolder archive = db.folder().getFolderByType(account.id, EntityFolder.ARCHIVE);
                    EntityFolder trash = db.folder().getFolderByType(account.id, EntityFolder.TRASH);
                    EntityFolder junk = db.folder().getFolderByType(account.id, EntityFolder.JUNK);

                    hasInbox = (inbox != null && inbox.selectable);
                    hasArchive = (archive != null && archive.selectable);
                    hasTrash = (trash != null && trash.selectable);
                    hasJunk = (junk != null && junk.selectable);
                } else {
                    result.hasPop = true;

                    if (result.leave_on_server == null)
                        result.leave_on_server = account.leave_on_server;
                    else
                        result.leave_on_server = (result.leave_on_server && account.leave_on_server);

                    if (result.leave_deleted == null)
                        result.leave_deleted = account.leave_deleted;
                    else
                        result.leave_deleted = (result.leave_deleted && account.leave_deleted);
                }

                result.hasInbox = (result.hasInbox == null ? hasInbox : result.hasInbox && hasInbox);
                result.hasArchive = (result.hasArchive == null ? hasArchive : result.hasArchive && hasArchive);
                result.hasTrash = (result.hasTrash == null ? hasTrash : result.hasTrash && hasTrash);
                result.hasJunk = (result.hasJunk == null ? hasJunk : result.hasJunk && hasJunk);

                if (accounts.size() == 1 && account.protocol == EntityAccount.TYPE_IMAP)
                    result.copyto = account;
            }

            if (result.isInbox == null) result.isInbox = false;
            if (result.isSent == null) result.isSent = false;
            if (result.isArchive == null) result.isArchive = false;
            if (result.isTrash == null) result.isTrash = false;
            if (result.isJunk == null) result.isJunk = false;
            if (result.isDrafts == null) result.isDrafts = false;

            if (result.hasInbox == null) result.hasInbox = false;
            if (result.hasArchive == null) result.hasArchive = false;
            if (result.hasTrash == null) result.hasTrash = false;
            if (result.hasJunk == null) result.hasJunk = false;

            result.imapAccounts = new ArrayList<>();
            if (!result.hasPop ||
                    (accounts.size() == 1 && result.isInbox && !result.isSent)) {
                List<EntityAccount> syncing = db.account().getSynchronizingAccounts(EntityAccount.TYPE_IMAP);
                if (syncing != null)
                    result.imapAccounts.addAll(syncing);
            }

            if (result.folders.size() > 1)
                result.folders = new ArrayList<>();

            return result;
        }
    }

    public static class MessageTarget implements Parcelable {
        long id;
        boolean found;
        Account sourceAccount;
        Folder sourceFolder;
        Account targetAccount;
        Folder targetFolder;
        boolean copy;
        boolean block;

        MessageTarget(Context context, EntityMessage message,
                      EntityAccount sourceAccount, EntityFolder sourceFolder,
                      EntityAccount targetAccount, EntityFolder targetFolder) {
            this.id = message.id;
            this.found = message.ui_found;
            this.sourceAccount = new Account(sourceAccount);
            this.sourceFolder = new Folder(context, sourceFolder);
            this.targetAccount = new Account(targetAccount);
            this.targetFolder = new Folder(context, targetFolder);
        }

        MessageTarget setCopy(boolean copy) {
            this.copy = copy;
            return this;
        }

        MessageTarget setBlock(boolean block) {
            this.block = block;
            return this;
        }

        boolean isAcross() {
            return (sourceAccount.id != targetAccount.id);
        }

        protected MessageTarget(Parcel in) {
            id = in.readLong();
            found = (in.readInt() != 0);
            sourceAccount = (Account) in.readSerializable();
            sourceFolder = (Folder) in.readSerializable();
            targetAccount = (Account) in.readSerializable();
            targetFolder = (Folder) in.readSerializable();
            copy = (in.readInt() != 0);
            block = (in.readInt() != 0);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeInt(found ? 1 : 0);
            dest.writeSerializable(sourceAccount);
            dest.writeSerializable(sourceFolder);
            dest.writeSerializable(targetAccount);
            dest.writeSerializable(targetFolder);
            dest.writeInt(copy ? 1 : 0);
            dest.writeInt(block ? 1 : 0);
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

        static class Account implements Serializable {
            long id;
            String name;

            Account(EntityAccount account) {
                this.id = account.id;
                this.name = account.name;
            }
        }

        static class Folder implements Serializable {
            long id;
            String type;
            String name;
            String display;
            Integer color;

            Folder(Context context, EntityFolder folder) {
                this.id = folder.id;
                this.type = folder.type;
                this.name = folder.name;
                this.display = folder.getDisplayName(context);
                this.color = folder.color;
            }
        }
    }
}
