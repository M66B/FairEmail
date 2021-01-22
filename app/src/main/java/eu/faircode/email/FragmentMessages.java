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

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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
import android.os.Parcel;
import android.os.Parcelable;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.LongSparseArray;
import android.util.Pair;
import android.util.TypedValue;
import android.view.KeyEvent;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuItemCompat;
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
import org.bouncycastle.util.Store;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openintents.openpgp.AutocryptPeerUpdate;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import me.everything.android.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;

import static android.app.Activity.RESULT_OK;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_DECELERATE_FACTOR;
import static me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK;
import static me.everything.android.ui.overscroll.OverScrollBounceEffectDecoratorBase.DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_KEY_MISSING;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_NO_SIGNATURE;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_VALID_KEY_CONFIRMED;
import static org.openintents.openpgp.OpenPgpSignatureResult.RESULT_VALID_KEY_UNCONFIRMED;

public class FragmentMessages extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private ViewGroup view;
    private SwipeRefreshLayoutEx swipeRefresh;
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
    private TextView tvSelectedCount;
    private FloatingActionButton fabSearch;
    private FloatingActionButton fabError;

    private String type;
    private long account;
    private long folder;
    private boolean server;
    private String thread;
    private long id;
    private boolean filter_archive;
    private boolean found;
    private BoundaryCallbackMessages.SearchCriteria criteria = null;
    private boolean pane;

    private WebView printWebView = null;

    private long message = -1;
    private OpenPgpServiceConnection pgpService;

    private boolean cards;
    private boolean beige;
    private boolean date;
    private boolean threading;
    private boolean swipenav;
    private boolean seekbar;
    private boolean actionbar;
    private boolean actionbar_color;
    private boolean autoexpand;
    private boolean autoclose;
    private String onclose;
    private boolean quick_scroll;
    private boolean addresses;

    private int colorPrimary;
    private int colorSecondary;

    private long primary;
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
    private boolean autoExpanded = true;

    final private Map<String, String> kv = new HashMap<>();
    final private Map<String, List<Long>> values = new HashMap<>();
    final private LongSparseArray<Float> sizes = new LongSparseArray<>();
    final private LongSparseArray<Integer> heights = new LongSparseArray<>();
    final private LongSparseArray<Pair<Integer, Integer>> positions = new LongSparseArray<>();
    final private LongSparseArray<List<EntityAttachment>> attachments = new LongSparseArray<>();
    final private LongSparseArray<TupleAccountSwipes> accountSwipes = new LongSparseArray<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private static final int MAX_MORE = 100; // messages
    private static final int SWIPE_DISABLE_SELECT_DURATION = 1500; // milliseconds
    private static final float LUMINANCE_THRESHOLD = 0.7f;

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

    static final String ACTION_STORE_RAW = BuildConfig.APPLICATION_ID + ".STORE_RAW";
    static final String ACTION_DECRYPT = BuildConfig.APPLICATION_ID + ".DECRYPT";
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
        account = args.getLong("account", -1);
        folder = args.getLong("folder", -1);
        server = args.getBoolean("server", false);
        thread = args.getString("thread");
        id = args.getLong("id", -1);
        filter_archive = args.getBoolean("filter_archive", true);
        found = args.getBoolean("found", false);
        criteria = (BoundaryCallbackMessages.SearchCriteria) args.getSerializable("criteria");
        pane = args.getBoolean("pane", false);
        primary = args.getLong("primary", -1);
        connected = args.getBoolean("connected", false);

        if (folder > 0 && thread == null && type == null && criteria == null)
            Log.e("Messages for folder without type");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swipenav = prefs.getBoolean("swipenav", true);
        cards = prefs.getBoolean("cards", true);
        beige = prefs.getBoolean("beige", true);
        date = prefs.getBoolean("date", true);
        threading = prefs.getBoolean("threading", true);
        seekbar = prefs.getBoolean("seekbar", false);
        actionbar = prefs.getBoolean("actionbar", true);
        actionbar_color = prefs.getBoolean("actionbar_color", false);
        autoexpand = prefs.getBoolean("autoexpand", true);
        autoclose = prefs.getBoolean("autoclose", true);
        onclose = (autoclose ? null : prefs.getString("onclose", null));
        quick_scroll = prefs.getBoolean("quick_scroll", true);
        addresses = prefs.getBoolean("addresses", false);

        colorPrimary = Helper.resolveColor(getContext(), R.attr.colorPrimary);
        colorSecondary = Helper.resolveColor(getContext(), R.attr.colorSecondary);

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

        if (viewType != AdapterMessage.ViewType.THREAD)
            getParentFragmentManager().setFragmentResultListener("message.selected", this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                    long id = result.getLong("id", -1);
                    iProperties.setValue("selected", id, true);
                }
            });
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
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
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

        ibHintSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("message_select", true).apply();
                grpHintSelect.setVisibility(View.GONE);
            }
        });

        rvMessage.setHasFixedSize(false);

        int threads = prefs.getInt("query_threads", 4);
        if (threads >= 4)
            rvMessage.setItemViewCacheSize(10); // Default: 2
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
        };
        rvMessage.setLayoutManager(llm);

        if (!cards) {
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

        DividerItemDecoration dateDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                for (int i = 0; i < parent.getChildCount(); i++) {
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
                if (!date || !SORT_DATE_HEADER.contains(adapter.getSort()))
                    return null;

                if (pos == NO_POSITION)
                    return null;

                TupleMessageEx prev = adapter.getItemAtPosition(pos - 1);
                TupleMessageEx message = adapter.getItemAtPosition(pos);
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

                view.setContentDescription(tvDate.getText().toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    view.setAccessibilityHeading(true);

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }
        };
        rvMessage.addItemDecoration(dateDecorator);

        rvMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy != 0) {
                    boolean down = (dy > 0);
                    if (scrolling != down) {
                        scrolling = down;
                        if (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER)
                            if (dy > 0)
                                fabCompose.hide();
                            else
                                fabCompose.show();
                        updateExpanded();
                    }
                }
            }
        });

        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        String sort = prefs.getString("sort", "time");
        boolean ascending = prefs.getBoolean(
                viewType == AdapterMessage.ViewType.THREAD ? "ascending_thread" : "ascending_list", false);
        boolean filter_duplicates = prefs.getBoolean("filter_duplicates", true);

        adapter = new AdapterMessage(
                this, type, found, viewType,
                compact, zoom, sort, ascending, filter_duplicates,
                iProperties);
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
                onMenuFilter("filter_seen", !filter);
            }
        });

        ibUnflagged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean filter = prefs.getBoolean("filter_unflagged", true);
                onMenuFilter("filter_unflagged", !filter);
            }
        });

        ibSnoozed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean filter = prefs.getBoolean("filter_snoozed", true);
                onMenuFilter("filter_snoozed", !filter);
            }
        });

        bottom_navigation.findViewById(R.id.action_delete).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onActionDelete();
                return true;
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                ActionData data = (ActionData) bottom_navigation.getTag();
                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        if (data.delete)
                            onActionDelete();
                        else
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
                args.putBoolean("filter_archive", filter_archive);

                new SimpleTask<ArrayList<MessageTarget>>() {
                    @Override
                    protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                        long aid = args.getLong("account");
                        String thread = args.getString("thread");
                        long id = args.getLong("id");
                        String type = args.getString("type");
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
                                        (!EntityFolder.SENT.equals(sourceFolder.type) || EntityFolder.TRASH.equals(targetFolder.type)) &&
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
                        moveAsk(result, false, !autoclose && onclose == null);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
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
                fragment.show(getParentFragmentManager(), "message:snooze");
            }
        });

        fabReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReply();
            }
        });

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCompose();
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

        tvSelectedCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMore();
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folder > 0) {
                    search(getContext(), getViewLifecycleOwner(), getParentFragmentManager(),
                            account, folder, true, criteria);
                    return;
                }

                Bundle args = new Bundle();
                args.putLong("account", account);
                args.putLong("folder", folder);

                new SimpleTask<List<EntityAccount>>() {
                    @Override
                    protected List<EntityAccount> onExecute(Context context, Bundle args) {
                        long aid = args.getLong("account");
                        long fid = args.getLong("folder");

                        List<EntityAccount> result = new ArrayList<>();
                        DB db = DB.getInstance(context);
                        if (aid < 0) {
                            List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                            for (EntityAccount account : accounts)
                                if (account.protocol == EntityAccount.TYPE_IMAP)
                                    result.add(account);
                        } else {
                            EntityAccount account = db.account().getAccount(aid);
                            if (account != null && account.protocol == EntityAccount.TYPE_IMAP)
                                result.add(account);
                        }

                        if (folder > 0) {
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
                            if (account.isGmail())
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
                                    if (gmail)
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
                        aargs.putString("title", getString(R.string.title_search_in));
                        aargs.putLong("account", account);
                        aargs.putLongArray("disabled", new long[]{});
                        aargs.putSerializable("criteria", criteria);

                        FragmentDialogFolder fragment = new FragmentDialogFolder();
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
                        if (ex instanceof IllegalArgumentException)
                            ToastEx.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        else
                            Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentMessages.this, args, "messages:search");
            }
        });

        fabError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuFolders(account);
            }
        });

        addKeyPressedListener(onBackPressedListener);

        // Initialize

        if (cards && !Helper.isDarkTheme(getContext()))
            view.setBackgroundColor(ContextCompat.getColor(getContext(), beige
                    ? R.color.lightColorBackground_cards_beige
                    : R.color.lightColorBackground_cards));

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

        if (viewType == AdapterMessage.ViewType.SEARCH && criteria != null && !server) {
            if (criteria.with_hidden ||
                    criteria.with_encrypted ||
                    criteria.with_attachments ||
                    criteria.with_types != null)
                fabSearch.hide();
            else
                fabSearch.show();
        } else
            fabSearch.hide();

        fabMore.hide();
        tvSelectedCount.setVisibility(View.GONE);
        fabError.hide();

        if (viewType == AdapterMessage.ViewType.THREAD) {
            ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getContext(), getViewLifecycleOwner(), id, new ViewModelMessages.IPrevNext() {
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
                                position == 0 || position == size - 1 ? colorSecondary : colorPrimary,
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
                    // https://issuetracker.google.com/issues/154178289
                    .withGestureTooltypes(MotionEvent.TOOL_TYPE_FINGER, MotionEvent.TOOL_TYPE_STYLUS)
                    .build();
            adapter.setSelectionTracker(selectionTracker);

            selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
                @Override
                public void onSelectionChanged() {
                    FragmentActivity activity = getActivity();
                    if (activity != null)
                        activity.invalidateOptionsMenu();

                    updateMore();
                }

                @Override
                public void onItemStateChanged(@NonNull Long key, boolean selected) {
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
                decor.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
                    private boolean triggered = false;

                    @Override
                    public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                        float height = decor.getView().getHeight();
                        if (height == 0)
                            return;

                        if (offset == 0)
                            triggered = false;
                        else if (!triggered) {
                            float dx = Math.abs(offset * DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD);
                            if (offset > 0 && dx > height / 4) {
                                triggered = true;
                                handleAutoClose();
                            }

                            if (offset < 0 && dx > height / 8) {
                                triggered = true;

                                Bundle args = new Bundle();
                                args.putString("title", getString(R.string.title_move_to_folder));
                                args.putLong("account", account);
                                args.putString("thread", thread);
                                args.putLong("id", id);
                                args.putBoolean("filter_archive", filter_archive);
                                args.putLongArray("disabled", new long[]{folder});

                                FragmentDialogFolder fragment = new FragmentDialogFolder();
                                fragment.setArguments(args);
                                fragment.setTargetFragment(FragmentMessages.this, REQUEST_THREAD_MOVE);
                                fragment.show(getParentFragmentManager(), "overscroll:move");
                            }
                        }
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

        final String pkg = Helper.getOpenKeychainPackage(getContext());
        Log.i("PGP binding to " + pkg);
        pgpService = new OpenPgpServiceConnection(getContext(), pkg);
        pgpService.bindToService();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        IntentFilter iff = new IntentFilter(SimpleTask.ACTION_TASK_COUNT);
        lbm.registerReceiver(treceiver, iff);

        return view;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(treceiver);

        if (pgpService != null && pgpService.isBound()) {
            Log.i("PGP unbinding");
            pgpService.unbindFromService();
        }
        pgpService = null;

        //kv.clear();
        //values.clear();
        //sizes.clear();
        //heights.clear();
        //positions.clear();
        //attachments.clear();
        //accountSwipes.clear();

        //values.remove("selected");

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putString("type", type);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long fid = args.getLong("folder");
                String type = args.getString("type");

                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                boolean now = true;
                boolean force = false;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean enabled = prefs.getBoolean("enabled", true);
                int pollInterval = prefs.getInt("poll_interval", ServiceSynchronize.DEFAULT_POLL_INTERVAL);

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
                        EntityOperation.sync(context, folder.id, true);

                        if (folder.account != null) {
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
        }.execute(this, args, "messages:refresh");
    }

    private AdapterMessage.IProperties iProperties = new AdapterMessage.IProperties() {
        @Override
        public void setValue(String key, String value) {
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

            if ("selected".equals(name) && enabled) {
                final List<Integer> changed = new ArrayList<>();

                int pos = adapter.getPositionForKey(id);
                if (pos != NO_POSITION)
                    changed.add(pos);

                for (Long other : new ArrayList<>(values.get("selected")))
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
        public boolean getValue(String name, long id) {
            if (values.containsKey(name))
                return values.get(name).contains(id);
            else {
                if ("addresses".equals(name))
                    return addresses;
            }
            return false;
        }

        @Override
        public void setExpanded(TupleMessageEx message, boolean value, boolean scroll) {
            // Prevent flicker
            if (value && message.accountAutoSeen && !message.folderReadOnly) {
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

            if (value)
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
        public void setPosition(long id, Pair<Integer, Integer> position) {
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
                    rvMessage.scrollBy(x, y);
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

                        result.add(new MessageTarget(context, message, account, sourceFolder, account, targetFolder));

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, ArrayList<MessageTarget> result) {
                    moveAsk(result, false, !autoclose && onclose == null);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(FragmentMessages.this, args, "messages:move");
        }

        @Override
        public void reply(TupleMessageEx message, String selected, View anchor) {
            onReply(message, selected, anchor);
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
        public void refresh() {
            rvMessage.post(new Runnable() {
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
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int pos = viewHolder.getAdapterPosition();
            if (pos == NO_POSITION)
                return 0;

            TupleMessageEx message = getMessage(pos);
            if (message == null)
                return 0;

            if (EntityFolder.OUTBOX.equals(message.folderType))
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);

            if (message.folderReadOnly)
                return 0;

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null)
                return 0;

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
                    getMainHandler().postDelayed(enableSelection, SWIPE_DISABLE_SELECT_DURATION);
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

            TupleAccountSwipes swipes;
            if (EntityFolder.OUTBOX.equals(message.folderType)) {
                swipes = new TupleAccountSwipes();
                swipes.swipe_right = 0L;
                swipes.right_type = EntityFolder.DRAFTS;
                swipes.swipe_left = 0L;
                swipes.left_type = EntityFolder.DRAFTS;
            } else {
                swipes = accountSwipes.get(message.account);
                if (swipes == null)
                    return;
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

            int icon;
            if (EntityMessage.SWIPE_ACTION_ASK.equals(action))
                icon = R.drawable.twotone_help_24;
            else if (EntityMessage.SWIPE_ACTION_SEEN.equals(action))
                icon = (message.ui_seen ? R.drawable.twotone_visibility_off_24 : R.drawable.twotone_visibility_24);
            else if (EntityMessage.SWIPE_ACTION_FLAG.equals(action))
                icon = (message.ui_flagged ? R.drawable.twotone_star_border_24 : R.drawable.baseline_star_24);
            else if (EntityMessage.SWIPE_ACTION_SNOOZE.equals(action))
                icon = (message.ui_snoozed == null ? R.drawable.twotone_timelapse_24 : R.drawable.twotone_timer_off_24);
            else if (EntityMessage.SWIPE_ACTION_HIDE.equals(action))
                icon = (message.ui_snoozed == null ? R.drawable.twotone_visibility_off_24 :
                        (message.ui_snoozed == Long.MAX_VALUE
                                ? R.drawable.twotone_visibility_24 : R.drawable.twotone_timer_off_24));
            else if (EntityMessage.SWIPE_ACTION_MOVE.equals(action))
                icon = R.drawable.twotone_folder_24;
            else if (EntityMessage.SWIPE_ACTION_JUNK.equals(action))
                icon = R.drawable.twotone_report_problem_24;
            else if (EntityMessage.SWIPE_ACTION_DELETE.equals(action) ||
                    (action.equals(message.folder) && EntityFolder.TRASH.equals(message.folderType)) ||
                    (EntityFolder.TRASH.equals(actionType) && EntityFolder.JUNK.equals(message.folderType)))
                icon = R.drawable.twotone_delete_forever_24;
            else
                icon = EntityFolder.getIcon(dX > 0 ? swipes.right_type : swipes.left_type);

            Drawable d = getResources().getDrawable(icon, context.getTheme()).mutate();
            d.setTint(Helper.resolveColor(context, android.R.attr.textColorSecondary));

            if (dX > 0) {
                // Right swipe
                d.setAlpha(Math.round(255 * Math.min(dX / (2 * margin + size), 1.0f)));
                if (swipes.right_color == null) {
                    Integer color = EntityFolder.getDefaultColor(swipes.right_type);
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
                d.setAlpha(Math.round(255 * Math.min(-dX / (2 * margin + size), 1.0f)));
                if (swipes.left_color == null) {
                    Integer color = EntityFolder.getDefaultColor(swipes.left_type);
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
            int pos = viewHolder.getAdapterPosition();
            if (pos == NO_POSITION) {
                adapter.notifyDataSetChanged();
                return;
            }

            TupleMessageEx message = getMessage(pos);
            if (message == null) {
                adapter.notifyDataSetChanged();
                return;
            }

            if (EntityFolder.OUTBOX.equals(message.folderType)) {
                FragmentMessages.onActionUndo(message, getContext(), getViewLifecycleOwner(), getParentFragmentManager());
                return;
            }

            TupleAccountSwipes swipes = accountSwipes.get(message.account);
            if (swipes == null) {
                adapter.notifyDataSetChanged();
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
                adapter.notifyDataSetChanged();
                return;
            }

            Log.i("Swiped dir=" + direction + " message=" + message.id);

            if (EntityMessage.SWIPE_ACTION_ASK.equals(action)) {
                adapter.notifyItemChanged(pos);
                onSwipeAsk(message, viewHolder);
            } else if (EntityMessage.SWIPE_ACTION_SEEN.equals(action))
                onActionSeenSelection(!message.ui_seen, message.id);
            else if (EntityMessage.SWIPE_ACTION_FLAG.equals(action))
                onActionFlagSelection(!message.ui_flagged, null, message.id);
            else if (EntityMessage.SWIPE_ACTION_SNOOZE.equals(action))
                onActionSnooze(message);
            else if (EntityMessage.SWIPE_ACTION_HIDE.equals(action))
                onActionHide(message);
            else if (EntityMessage.SWIPE_ACTION_MOVE.equals(action)) {
                adapter.notifyItemChanged(pos);
                onSwipeMove(message);
            } else if (EntityMessage.SWIPE_ACTION_JUNK.equals(action)) {
                adapter.notifyItemChanged(pos);
                onSwipeJunk(message);
            } else if (EntityMessage.SWIPE_ACTION_DELETE.equals(action) ||
                    (action.equals(message.folder) && EntityFolder.TRASH.equals(message.folderType)) ||
                    (EntityFolder.TRASH.equals(actionType) && EntityFolder.JUNK.equals(message.folderType))) {
                adapter.notifyItemChanged(pos);
                onSwipeDelete(message);
            } else
                swipeFolder(message, action);
        }

        private TupleMessageEx getMessage(int pos) {
            if (selectionTracker != null && selectionTracker.hasSelection())
                return null;

            PagedList<TupleMessageEx> list = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList();
            if (pos >= list.size())
                return null;

            TupleMessageEx message = list.get(pos);
            if (message == null)
                return null;

            if (message.uid == null &&
                    !EntityFolder.OUTBOX.equals(message.folderType) &&
                    message.accountProtocol == EntityAccount.TYPE_IMAP)
                return null;

            if (iProperties.getValue("expanded", message.id))
                return null;

            return message;
        }

        private void onSwipeAsk(final @NonNull TupleMessageEx message, @NonNull RecyclerView.ViewHolder viewHolder) {
            // Make sure animations are done
            rvMessage.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        int order = 1;
                        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), viewHolder.itemView);

                        if (message.ui_seen)
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_unseen, order++, R.string.title_unseen);
                        else
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_seen, order++, R.string.title_seen);

                        if (message.ui_flagged)
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_unflag, order++, R.string.title_unflag);
                        else
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_flag, order++, R.string.title_flag);

                        popupMenu.getMenu().add(Menu.NONE, R.string.title_snooze, order++, R.string.title_snooze);

                        if (message.ui_snoozed == null)
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_hide, order++, R.string.title_hide);
                        else if (message.ui_snoozed == Long.MAX_VALUE)
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_unhide, order++, R.string.title_unhide);

                        popupMenu.getMenu().add(Menu.NONE, R.string.title_flag_color, order++, R.string.title_flag_color);
                        if (message.accountProtocol == EntityAccount.TYPE_IMAP) {
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_move, order++, R.string.title_move);
                            popupMenu.getMenu().add(Menu.NONE, R.string.title_report_spam, order++, R.string.title_report_spam);
                        }
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_permanently, order++, R.string.title_delete_permanently);

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
                                    case R.string.title_flag:
                                        onActionFlagSelection(true, null, message.id);
                                        return true;
                                    case R.string.title_unflag:
                                        onActionFlagSelection(false, null, message.id);
                                        return true;
                                    case R.string.title_snooze:
                                        onMenuSnooze();
                                        return true;
                                    case R.string.title_hide:
                                    case R.string.title_unhide:
                                        onActionHide(message);
                                        return true;
                                    case R.string.title_flag_color:
                                        onMenuColor();
                                        return true;
                                    case R.string.title_move:
                                        onSwipeMove(message);
                                        return true;
                                    case R.string.title_report_spam:
                                        onSwipeJunk(message);
                                        return true;
                                    case R.string.title_delete_permanently:
                                        onSwipeDelete(message);
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
                                fragment.show(getParentFragmentManager(), "message:snooze");
                            }

                            private void onMenuColor() {
                                Bundle args = new Bundle();
                                args.putLong("id", message.id);
                                args.putInt("color", message.color == null ? Color.TRANSPARENT : message.color);
                                args.putString("title", getString(R.string.title_flag_color));

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

        private void onSwipeMove(final @NonNull TupleMessageEx message) {
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
            fragment.show(getParentFragmentManager(), "swipe:move");
        }

        private void onSwipeJunk(final @NonNull TupleMessageEx message) {
            Bundle aargs = new Bundle();
            aargs.putLong("id", message.id);
            aargs.putLong("account", message.account);
            aargs.putInt("protocol", message.accountProtocol);
            aargs.putLong("folder", message.folder);
            aargs.putString("type", message.folderType);
            aargs.putString("from", MessageHelper.formatAddresses(message.from));
            aargs.putBoolean("inJunk", EntityFolder.JUNK.equals(message.folderType));

            AdapterMessage.FragmentDialogJunk ask = new AdapterMessage.FragmentDialogJunk();
            ask.setArguments(aargs);
            ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGE_JUNK);
            ask.show(getParentFragmentManager(), "swipe:junk");
        }

        private void onSwipeDelete(@NonNull TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putString("question", getString(R.string.title_ask_delete));
            args.putLong("id", message.id);
            args.putBoolean("warning", true);

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
                                message.account, message.thread, threading && thread ? null : id, message.folder);
                        for (EntityMessage threaded : messages) {
                            EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                            if (sourceFolder == null || sourceFolder.read_only)
                                continue;

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
                    moveUndo(result);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                                .setGestureInsetBottomIgnored(true).show();
                    else
                        Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(FragmentMessages.this, args, "swipe:folder");
        }
    };

    private void onReply() {
        if (values.containsKey("expanded") && values.get("expanded").size() > 0) {
            long id = values.get("expanded").get(0);
            int pos = adapter.getPositionForKey(id);
            TupleMessageEx message = adapter.getItemAtPosition(pos);
            AdapterMessage.ViewHolder holder =
                    (AdapterMessage.ViewHolder) rvMessage.findViewHolderForAdapterPosition(pos);
            String selected = (holder == null ? null : holder.getSelectedText());
            if (message == null)
                return;
            onReply(message, selected, fabReply);
        }
    }

    private void onReply(final TupleMessageEx message, final String selected, final View anchor) {
        Bundle args = new Bundle();
        args.putLong("id", message.id);

        new SimpleTask<ReplyData>() {
            @Override
            protected ReplyData onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                ReplyData result = new ReplyData();

                DB db = DB.getInstance(context);

                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return result;

                args.putInt("answers", db.answer().getAnswerCount());

                result.identities = db.identity().getComposableIdentities(message.account);
                result.answers = db.answer().getAnswersByFavorite(true);

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, ReplyData data) {
                if (data.identities == null)
                    data.identities = new ArrayList<>();

                final Address[] to =
                        message.replySelf(data.identities, message.account)
                                ? message.to
                                : (message.reply == null || message.reply.length == 0 ? message.from : message.reply);

                Address[] recipients = message.getAllRecipients(data.identities, message.account);

                int answers = args.getInt("answers");

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), anchor);
                popupMenu.inflate(R.menu.popup_reply);
                popupMenu.getMenu().findItem(R.id.menu_reply_to_all).setVisible(recipients.length > 0);
                popupMenu.getMenu().findItem(R.id.menu_reply_list).setVisible(message.list_post != null);
                popupMenu.getMenu().findItem(R.id.menu_reply_receipt).setVisible(message.receipt_to != null);
                popupMenu.getMenu().findItem(R.id.menu_new_message).setVisible(to != null && to.length > 0);
                popupMenu.getMenu().findItem(R.id.menu_reply_answer).setVisible(answers != 0 || !ActivityBilling.isPro(getContext()));

                popupMenu.getMenu().findItem(R.id.menu_reply_to_sender).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_reply_to_all).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_forward).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_editasnew).setEnabled(message.content);
                popupMenu.getMenu().findItem(R.id.menu_reply_answer).setEnabled(message.content);

                if (data.answers != null) {
                    int order = 100;
                    for (EntityAnswer answer : data.answers) {
                        order++;
                        popupMenu.getMenu().add(1, order, order, answer.toString())
                                .setIntent(new Intent().putExtra("id", answer.id));
                    }
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        if (target.getGroupId() == 1) {
                            startActivity(new Intent(getContext(), ActivityCompose.class)
                                    .putExtra("action", "reply")
                                    .putExtra("reference", message.id)
                                    .putExtra("answer", target.getIntent().getLongExtra("id", -1)));

                            return true;
                        }

                        switch (target.getItemId()) {
                            case R.id.menu_reply_to_sender:
                                onMenuReply(message, "reply", selected);
                                return true;
                            case R.id.menu_reply_to_all:
                                onMenuReply(message, "reply_all", selected);
                                return true;
                            case R.id.menu_reply_list:
                                onMenuReply(message, "list", selected);
                                return true;
                            case R.id.menu_reply_receipt:
                                onMenuReply(message, "receipt");
                                return true;
                            case R.id.menu_forward:
                                onMenuReply(message, "forward");
                                return true;
                            case R.id.menu_editasnew:
                                onMenuReply(message, "editasnew");
                                return true;
                            case R.id.menu_new_message:
                                onMenuNew(message, to);
                                return true;
                            case R.id.menu_reply_answer:
                                onMenuAnswer(message);
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(FragmentMessages.this, args, "messages:reply");
    }

    private void onMenuReply(TupleMessageEx message, String action) {
        onMenuReply(message, action, null);
    }

    private void onMenuReply(TupleMessageEx message, String action, String selected) {
        Intent reply = new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", action)
                .putExtra("reference", message.id)
                .putExtra("selected", selected);
        startActivity(reply);
    }

    private void onMenuNew(TupleMessageEx message, Address[] to) {
        Intent reply = new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "new")
                .putExtra("to", MessageHelper.formatAddresses(to, true, true));
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
                if (answers == null || answers.size() == 0) {
                    Snackbar snackbar = Snackbar.make(view, R.string.title_no_answers, Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true);
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                            lbm.sendBroadcast(new Intent(ActivityView.ACTION_EDIT_ANSWERS));
                        }
                    });
                    snackbar.show();
                } else {
                    PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), fabReply);
                    Menu main = popupMenu.getMenu();

                    Map<String, SubMenu> map = new HashMap<>();

                    int order = 0;
                    for (EntityAnswer answer : answers) {
                        order++;
                        if (answer.group == null)
                            main.add(Menu.NONE, order, order++, answer.toString())
                                    .setIntent(new Intent().putExtra("id", answer.id));
                        else {
                            if (!map.containsKey(answer.group))
                                map.put(answer.group, main.addSubMenu(Menu.NONE, order, order++, answer.group));
                            SubMenu smenu = map.get(answer.group);
                            smenu.add(Menu.NONE, smenu.size(), smenu.size() + 1, answer.toString())
                                    .setIntent(new Intent().putExtra("id", answer.id));
                        }
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem target) {
                            Intent intent = target.getIntent();
                            if (intent == null)
                                return false;

                            if (!ActivityBilling.isPro(getContext())) {
                                startActivity(new Intent(getContext(), ActivityBilling.class));
                                return true;
                            }

                            startActivity(new Intent(getContext(), ActivityCompose.class)
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

    private void onCompose() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean identities_asked = prefs.getBoolean("identities_asked", false);
        if (identities_asked)
            startActivity(new Intent(getContext(), ActivityCompose.class)
                    .putExtra("action", "new")
                    .putExtra("account", account)
            );
        else {
            Bundle args = new Bundle();
            args.putLong("account", account);

            FragmentDialogIdentity fragment = new FragmentDialogIdentity();
            fragment.setArguments(args);
            fragment.show(getParentFragmentManager(), "messages:identities");
        }
    }

    private void onMore() {
        Bundle args = new Bundle();
        args.putLongArray("ids", getSelection());

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

                Map<Long, EntityAccount> accounts = new HashMap<>();
                Map<Long, EntityFolder> folders = new HashMap<>();

                DB db = DB.getInstance(context);

                boolean pop = false;
                MoreResult result = new MoreResult();
                result.folders = new ArrayList<>();

                if (ids.length > MAX_MORE) {
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
                        folders.put(folder.id, folder);
                    }

                    if (account.protocol != EntityAccount.TYPE_IMAP)
                        pop = true;

                    if (!result.folders.contains(message.folder))
                        result.folders.add(message.folder);

                    boolean isInbox = EntityFolder.INBOX.equals(folder.type);
                    boolean isArchive = EntityFolder.ARCHIVE.equals(folder.type);
                    boolean isTrash = (EntityFolder.TRASH.equals(folder.type) || account.protocol != EntityAccount.TYPE_IMAP);
                    boolean isJunk = EntityFolder.JUNK.equals(folder.type);
                    boolean isDrafts = EntityFolder.DRAFTS.equals(folder.type);
                    boolean isSent = EntityFolder.SENT.equals(folder.type);

                    if (pop && isSent)
                        isInbox = true;

                    result.isInbox = (result.isInbox == null ? isInbox : result.isInbox && isInbox);
                    result.isArchive = (result.isArchive == null ? isArchive : result.isArchive && isArchive);
                    result.isTrash = (result.isTrash == null ? isTrash : result.isTrash && isTrash);
                    result.isJunk = (result.isJunk == null ? isJunk : result.isJunk && isJunk);
                    result.isDrafts = (result.isDrafts == null ? isDrafts : result.isDrafts && isDrafts);

                    if (result.seen && result.unseen &&
                            result.flagged && result.unflagged &&
                            result.importance == -1 &&
                            result.visible && result.hidden)
                        continue;

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            message.account, message.thread, threading ? null : id, null);
                    for (EntityMessage threaded : messages) {
                        if (threaded.folder.equals(message.folder))
                            if (threaded.ui_seen)
                                result.seen = true;
                            else
                                result.unseen = true;

                        if (threaded.ui_flagged)
                            result.flagged = true;
                        else
                            result.unflagged = true;

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
                }

                for (EntityAccount account : accounts.values()) {
                    boolean hasArchive = (account.protocol == EntityAccount.TYPE_IMAP &&
                            db.folder().getFolderByType(account.id, EntityFolder.ARCHIVE) != null);
                    boolean hasTrash = (account.protocol == EntityAccount.TYPE_IMAP &&
                            db.folder().getFolderByType(account.id, EntityFolder.TRASH) != null);
                    boolean hasJunk = (account.protocol == EntityAccount.TYPE_IMAP &&
                            db.folder().getFolderByType(account.id, EntityFolder.JUNK) != null);

                    result.hasArchive = (result.hasArchive == null ? hasArchive : result.hasArchive && hasArchive);
                    result.hasTrash = (result.hasTrash == null ? hasTrash : result.hasTrash && hasTrash);
                    result.hasJunk = (result.hasJunk == null ? hasJunk : result.hasJunk && hasJunk);

                    if (accounts.size() == 1 && account.protocol == EntityAccount.TYPE_IMAP)
                        result.copyto = account;
                }

                if (result.isInbox == null) result.isInbox = false;
                if (result.isArchive == null) result.isArchive = false;
                if (result.isTrash == null) result.isTrash = false;
                if (result.isJunk == null) result.isJunk = false;
                if (result.isDrafts == null) result.isDrafts = false;

                if (result.hasArchive == null) result.hasArchive = false;
                if (result.hasTrash == null) result.hasTrash = false;
                if (result.hasJunk == null) result.hasJunk = false;

                result.accounts = new ArrayList<>();
                if (!pop)
                    for (EntityAccount account : db.account().getSynchronizingAccounts())
                        if (account.protocol == EntityAccount.TYPE_IMAP)
                            result.accounts.add(account);

                if (result.folders.size() > 1)
                    result.folders = new ArrayList<>();

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

                if (result.visible)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_hide, order++, R.string.title_hide);
                if (result.hidden)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unhide, order++, R.string.title_unhide);

                if (result.unflagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag, order++, R.string.title_flag);
                if (result.flagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_unflag, order++, R.string.title_unflag);
                if (result.unflagged || result.flagged)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_flag_color, order++, R.string.title_flag_color);

                SubMenu importance = popupMenu.getMenu()
                        .addSubMenu(Menu.NONE, Menu.NONE, order++, R.string.title_set_importance);
                importance.add(Menu.NONE, R.string.title_importance_high, 1, R.string.title_importance_high)
                        .setEnabled(!EntityMessage.PRIORITIY_HIGH.equals(result.importance));
                importance.add(Menu.NONE, R.string.title_importance_normal, 2, R.string.title_importance_normal)
                        .setEnabled(!EntityMessage.PRIORITIY_NORMAL.equals(result.importance));
                importance.add(Menu.NONE, R.string.title_importance_low, 3, R.string.title_importance_low)
                        .setEnabled(!EntityMessage.PRIORITIY_LOW.equals(result.importance));

                if (!result.isInbox) // not is inbox
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_folder_inbox, order++, R.string.title_folder_inbox);

                if (result.hasArchive && !result.isArchive) // has archive and not is archive
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_archive, order++, R.string.title_archive);

                if (result.isTrash || !result.hasTrash || result.isJunk) // is trash or no trash or is junk
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, order++, R.string.title_delete);

                if (!result.isTrash && result.hasTrash && !result.isJunk) // not trash and has trash and not is junk
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_trash, order++, R.string.title_trash);

                if (result.hasJunk && !result.isJunk && !result.isDrafts) // has junk and not junk/drafts
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_spam, order++, R.string.title_spam);

                for (EntityAccount account : result.accounts) {
                    String title = getString(R.string.title_move_to_account, account.name);
                    SpannableString ss = new SpannableString(title);
                    if (account.name != null && account.color != null) {
                        int i = title.indexOf(account.name);
                        ss.setSpan(new ForegroundColorSpan(account.color), i, i + 1, 0);
                    }
                    MenuItem item = popupMenu.getMenu().add(Menu.NONE, R.string.title_move_to_account, order++, ss);
                    item.setIntent(new Intent().putExtra("account", account.id));
                }

                if (result.copyto != null)
                    popupMenu.getMenu().add(Menu.NONE, R.string.title_copy_to, order++, R.string.title_copy_to);

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
                            case R.string.title_hide:
                                onHideSelection(true);
                                return true;
                            case R.string.title_unhide:
                                onHideSelection(false);
                                return true;
                            case R.string.title_flag:
                                onActionFlagSelection(true, null, null);
                                return true;
                            case R.string.title_unflag:
                                onActionFlagSelection(false, null, null);
                                return true;
                            case R.string.title_flag_color:
                                onActionFlagColorSelection();
                                return true;
                            case R.string.title_importance_low:
                                onActionSetImportanceSelection(EntityMessage.PRIORITIY_LOW);
                                return true;
                            case R.string.title_importance_normal:
                                onActionSetImportanceSelection(EntityMessage.PRIORITIY_NORMAL);
                                return true;
                            case R.string.title_importance_high:
                                onActionSetImportanceSelection(EntityMessage.PRIORITIY_HIGH);
                                return true;
                            case R.string.title_folder_inbox:
                                onActionMoveSelection(EntityFolder.INBOX);
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
                                onActionMoveSelectionAccount(account, false, result.folders);
                                return true;
                            case R.string.title_copy_to:
                                onActionMoveSelectionAccount(result.copyto.id, true, result.folders);
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
                Log.unexpectedError(getParentFragmentManager(), ex);
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

        //if (selectionTracker != null)
        //    selectionTracker.clearSelection();

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

                ServiceSynchronize.eval(context, "seen");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:seen");
    }

    private void onActionSnooze(TupleMessageEx message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        long duration = prefs.getInt("default_snooze", 1) * 3600 * 1000L;

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

        onSnooze(args);
    }

    private void onActionHide(TupleMessageEx message) {
        Bundle args = new Bundle();
        args.putLong("account", message.account);
        args.putString("thread", message.thread);
        args.putLong("id", message.id);
        args.putLong("duration", message.ui_snoozed == null ? Long.MAX_VALUE : 0);
        args.putLong("time", message.ui_snoozed == null ? Long.MAX_VALUE : 0);
        args.putBoolean("hide", true);

        onSnooze(args);
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

    private void onActionFlagSelection(boolean flagged, Integer color, Long id) {
        Bundle args = new Bundle();
        args.putLongArray("ids", id == null ? getSelection() : new long[]{id});
        args.putBoolean("flagged", flagged);
        if (color != null)
            args.putInt("color", color);

        //selectionTracker.clearSelection();

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

    private void onActionFlagColorSelection() {
        Bundle args = new Bundle();
        args.putInt("color", Color.TRANSPARENT);
        args.putString("title", getString(R.string.title_flag_color));

        FragmentDialogColor fragment = new FragmentDialogColor();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_COLOR);
        fragment.show(getParentFragmentManager(), "messages:color");
    }

    private void onActionSetImportanceSelection(int importance) {
        Bundle args = new Bundle();
        args.putLongArray("selected", getSelection());
        args.putInt("importance", importance);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long[] selected = args.getLongArray("selected");
                Integer importance = args.getInt("importance");
                if (EntityMessage.PRIORITIY_NORMAL.equals(importance))
                    importance = null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (long id : selected) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages)
                            db.message().setMessageImportance(threaded.id, importance);
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

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages)
                            if (message.uid != null || account.protocol != EntityAccount.TYPE_IMAP)
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
                aargs.putBoolean("warning", true);

                FragmentDialogAsk ask = new FragmentDialogAsk();
                ask.setArguments(aargs);
                ask.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_DELETE);
                ask.show(getParentFragmentManager(), "messages:delete");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
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
        ask.show(getParentFragmentManager(), "messages:junk");
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

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            continue;

                        EntityFolder targetFolder = db.folder().getFolderByType(message.account, type);
                        if (targetFolder == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages) {
                            EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                            if (sourceFolder == null || sourceFolder.read_only)
                                continue;

                            result.add(new MessageTarget(context, threaded, account, sourceFolder, account, targetFolder));
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
                    moveAsk(result, true, true);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:move");
    }

    private void onActionMoveSelectionAccount(long account, boolean copy, List<Long> disabled) {
        Bundle args = new Bundle();
        args.putString("title", getString(copy ? R.string.title_copy_to : R.string.title_move_to_folder));
        args.putLong("account", account);
        args.putBoolean("copy", copy);
        args.putLongArray("disabled", Helper.toLongArray(disabled));

        FragmentDialogFolder fragment = new FragmentDialogFolder();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentMessages.this, REQUEST_MESSAGES_MOVE);
        fragment.show(getParentFragmentManager(), "messages:move");
    }

    private void onActionMoveSelection(Bundle args) {
        args.putLongArray("ids", getSelection());

        new SimpleTask<ArrayList<MessageTarget>>() {
            @Override
            protected ArrayList<MessageTarget> onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                long tid = args.getLong("folder");
                boolean copy = args.getBoolean("copy");

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
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages) {
                            EntityFolder sourceFolder = db.folder().getFolder(threaded.folder);
                            if (sourceFolder == null || sourceFolder.read_only)
                                continue;

                            result.add(new MessageTarget(context, threaded, sourceAccount, sourceFolder, targetAccount, targetFolder, copy));
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
                if (copy)
                    moveAskConfirmed(result);
                else
                    moveAsk(result, true, true);
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
                                !EntityFolder.isOutgoing(sourceFolder.type) &&
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

                    List<EntityMessage> messages = db.message().getMessagesByThread(
                            aid, thread, threading ? null : id, null);
                    for (EntityMessage threaded : messages) {
                        EntityFolder folder = db.folder().getFolder(threaded.folder);
                        if (!folder.read_only &&
                                (!filter_archive || !EntityFolder.ARCHIVE.equals(folder.type)) &&
                                !EntityFolder.DRAFTS.equals(folder.type) &&
                                !EntityFolder.OUTBOX.equals(folder.type) &&
                                // sent
                                // trash
                                !EntityFolder.JUNK.equals(folder.type))
                            result.add(threaded.id);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return result;
            }

            @Override
            protected void onExecuted(Bundle args, List<Long> ids) {
                Bundle aargs = new Bundle();
                aargs.putString("question", getResources()
                        .getQuantityString(R.plurals.title_deleting_messages, ids.size(), ids.size()));
                aargs.putLongArray("ids", Helper.toLongArray(ids));
                aargs.putBoolean("warning", true);

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

    static void onActionUndo(TupleMessageEx message, final Context context, final LifecycleOwner owner, final FragmentManager manager) {
        Bundle args = new Bundle();
        args.putLong("id", message.id);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                EntityMessage message;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    message = db.message().getMessage(id);
                    if (message == null)
                        return null;
                    if (message.account == null)
                        throw new IllegalStateException("Account missing");

                    db.folder().setFolderError(message.folder, null);
                    if (message.identity != null)
                        db.identity().setIdentityError(message.identity, null);

                    File source = message.getFile(context);

                    // Insert into drafts
                    EntityFolder drafts = db.folder().getFolderByType(message.account, EntityFolder.DRAFTS);
                    if (drafts == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_drafts));

                    message.id = null;
                    message.folder = drafts.id;
                    message.fts = false;
                    message.ui_snoozed = null;
                    message.error = null;
                    message.id = db.message().insertMessage(message);

                    File target = message.getFile(context);
                    source.renameTo(target);

                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    for (EntityAttachment attachment : attachments)
                        db.attachment().setMessage(attachment.id, message.id);

                    EntityOperation.queue(context, message, EntityOperation.ADD);

                    // Delete from outbox
                    db.message().deleteMessage(id); // will delete operation too

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "outbox/drafts");

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel("send:" + id, 1);

                return message;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null)
                    context.startActivity(
                            new Intent(context, ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", draft.id));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(manager, ex, !(ex instanceof IllegalArgumentException));
            }
        }.execute(context, owner, args, "message:move:draft");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:reset", reset);
        outState.putBoolean("fair:autoExpanded", autoExpanded);
        outState.putInt("fair:autoCloseCount", autoCloseCount);

        outState.putStringArray("fair:values", values.keySet().toArray(new String[0]));
        for (String name : values.keySet())
            outState.putLongArray("fair:name:" + name, Helper.toLongArray(values.get(name)));

        if (rvMessage != null) {
            Parcelable rv = rvMessage.getLayoutManager().onSaveInstanceState();
            outState.putParcelable("fair:rv", rv);

            LinearLayoutManager llm = (LinearLayoutManager) rvMessage.getLayoutManager();
            outState.putInt("fair:scroll", llm.findFirstVisibleItemPosition());
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
            autoExpanded = savedInstanceState.getBoolean("fair:autoExpanded");
            autoCloseCount = savedInstanceState.getInt("fair:autoCloseCount");

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

            adapter.gotoPos(savedInstanceState.getInt("fair:scroll"));

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

        loadMessages(false);

        updateExpanded();
        updateMore();

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
                            updateExpanded();
                        }
                    }
                });
                break;

            case SEARCH:
                setSubtitle(criteria.getTitle(getContext()));
                break;
        }

        if (!checkReporting())
            if (!checkReview())
                checkFingerprint();
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
        iff.addAction(ACTION_KEYWORDS);
        lbm.registerReceiver(receiver, iff);

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        adapter.setCompact(compact);
        adapter.setZoom(zoom);

        // Restart spinner
        swipeRefresh.resetRefreshing();

        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "pro");

        if (viewType == AdapterMessage.ViewType.UNIFIED || viewType == AdapterMessage.ViewType.FOLDER) {
            boolean notify_clear = prefs.getBoolean("notify_clear", false);
            if (notify_clear) {
                Bundle args = new Bundle();
                args.putLong("folder", folder);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        Long folder = args.getLong("folder");
                        if (folder < 0)
                            folder = null;

                        DB db = DB.getInstance(context);
                        db.message().ignoreAll(null, folder);
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(this, args, "messages:ignore");
            }
        }
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
        if ("pro".equals(key) || "banner_hidden".equals(key)) {
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
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        if (!rvMessage.isComputingLayout())
                            adapter.checkInternet();
                }
            });
        }
    };

    private boolean checkReporting() {
        if (viewType != AdapterMessage.ViewType.UNIFIED)
            return false;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("crash_reports", false) ||
                prefs.getBoolean("crash_reports_asked", false))
            return false;

        final Snackbar snackbar = Snackbar.make(view, R.string.title_ask_help, Snackbar.LENGTH_INDEFINITE)
                .setGestureInsetBottomIgnored(true);
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

        if (!Helper.isPlayStoreInstall() && !BuildConfig.DEBUG)
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

        final Snackbar snackbar = Snackbar.make(view, R.string.title_ask_review, Snackbar.LENGTH_INDEFINITE)
                .setGestureInsetBottomIgnored(true);
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
                prefs.edit().putLong("review_later", new Date().getTime()).apply();
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

        final Snackbar snackbar = Snackbar.make(view, R.string.title_third_party, Snackbar.LENGTH_INDEFINITE)
                .setGestureInsetBottomIgnored(true);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_messages, menu);

        menu.findItem(R.id.menu_folders).setActionView(R.layout.action_button);
        ImageButton ib = (ImageButton) menu.findItem(R.id.menu_folders).getActionView();
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
                FragmentDialogAccount fragment = new FragmentDialogAccount();
                fragment.setArguments(new Bundle());
                fragment.setTargetFragment(FragmentMessages.this, REQUEST_ACCOUNT);
                fragment.show(getParentFragmentManager(), "messages:accounts");
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
        boolean filter_unknown = prefs.getBoolean("filter_unknown", false);
        boolean filter_snoozed = prefs.getBoolean("filter_snoozed", true);
        boolean filter_duplicates = prefs.getBoolean("filter_duplicates", true);
        boolean language_detection = prefs.getBoolean("language_detection", false);
        boolean compact = prefs.getBoolean("compact", false);
        boolean quick_filter = prefs.getBoolean("quick_filter", false);

        boolean drafts = EntityFolder.DRAFTS.equals(type);
        boolean outbox = EntityFolder.OUTBOX.equals(type);
        boolean sent = EntityFolder.SENT.equals(type);

        boolean folder =
                (viewType == AdapterMessage.ViewType.UNIFIED ||
                        (viewType == AdapterMessage.ViewType.FOLDER && !outbox));

        boolean filter_active = (filter_seen || filter_unflagged || filter_unknown);
        MenuItem menuFilter = menu.findItem(R.id.menu_filter);
        menuFilter.setShowAsAction(folder && filter_active
                ? MenuItem.SHOW_AS_ACTION_ALWAYS
                : MenuItem.SHOW_AS_ACTION_NEVER);
        MenuItemCompat.setIconTintList(menuFilter,
                folder && filter_active ?
                        ColorStateList.valueOf(Helper.resolveColor(getContext(), R.attr.colorSecondary)) : null);
        menuFilter.setIcon(folder && filter_active ? R.drawable.twotone_filter_alt_24 : R.drawable.twotone_filter_list_24);

        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        menuSearch.setVisible(folder);

        menu.findItem(R.id.menu_folders).setVisible(viewType == AdapterMessage.ViewType.UNIFIED && primary >= 0);
        ImageButton ib = (ImageButton) menu.findItem(R.id.menu_folders).getActionView();
        ib.setImageResource(connected
                ? R.drawable.twotone_folder_special_24 : R.drawable.twotone_folder_24);

        menu.findItem(R.id.menu_sort_on).setVisible(viewType != AdapterMessage.ViewType.SEARCH);

        if (viewType == AdapterMessage.ViewType.THREAD) {
            menu.findItem(R.id.menu_sort_on_time).setVisible(false);
            menu.findItem(R.id.menu_sort_on_unread).setVisible(false);
            menu.findItem(R.id.menu_sort_on_priority).setVisible(false);
            menu.findItem(R.id.menu_sort_on_starred).setVisible(false);
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
        menu.findItem(R.id.menu_filter_duplicates).setVisible(viewType == AdapterMessage.ViewType.THREAD);

        menu.findItem(R.id.menu_filter_seen).setChecked(filter_seen);
        menu.findItem(R.id.menu_filter_unflagged).setChecked(filter_unflagged);
        menu.findItem(R.id.menu_filter_unknown).setChecked(filter_unknown);
        menu.findItem(R.id.menu_filter_snoozed).setChecked(filter_snoozed);
        menu.findItem(R.id.menu_filter_duplicates).setChecked(filter_duplicates);

        menu.findItem(R.id.menu_compact).setChecked(compact);

        menu.findItem(R.id.menu_select_language).setVisible(language_detection && folder);
        menu.findItem(R.id.menu_select_all).setVisible(folder);
        menu.findItem(R.id.menu_select_found).setVisible(viewType == AdapterMessage.ViewType.SEARCH);
        menu.findItem(R.id.menu_mark_all_read).setVisible(folder);

        menu.findItem(R.id.menu_force_sync).setVisible(viewType == AdapterMessage.ViewType.UNIFIED);
        menu.findItem(R.id.menu_force_send).setVisible(outbox);

        ibSeen.setImageResource(filter_seen ? R.drawable.twotone_drafts_24 : R.drawable.twotone_mail_24);
        ibUnflagged.setImageResource(filter_unflagged ? R.drawable.twotone_star_border_24 : R.drawable.baseline_star_24);
        ibSnoozed.setImageResource(filter_snoozed ? R.drawable.twotone_visibility_off_24 : R.drawable.twotone_visibility_24);

        ibSeen.setVisibility(quick_filter && folder ? View.VISIBLE : View.GONE);
        ibUnflagged.setVisibility(quick_filter && folder ? View.VISIBLE : View.GONE);
        ibSnoozed.setVisibility(quick_filter && folder && !drafts ? View.VISIBLE : View.GONE);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onMenuSearch();
                return true;

            case R.id.menu_folders:
                // Obsolete
                onMenuFolders(primary);
                return true;

            case R.id.menu_empty_trash:
                onMenuEmpty(EntityFolder.TRASH);
                return true;

            case R.id.menu_empty_spam:
                onMenuEmpty(EntityFolder.JUNK);
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

            case R.id.menu_sort_on_priority:
                item.setChecked(true);
                onMenuSort("priority");
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

            case R.id.menu_sort_on_attachments:
                item.setChecked(true);
                onMenuSort("attachments");
                return true;

            case R.id.menu_sort_on_snoozed:
                item.setChecked(true);
                onMenuSort("snoozed");
                return true;

            case R.id.menu_ascending:
                onMenuAscending(!item.isChecked());
                return true;

            case R.id.menu_filter_seen:
                onMenuFilter("filter_seen", !item.isChecked());
                return true;

            case R.id.menu_filter_unflagged:
                onMenuFilter("filter_unflagged", !item.isChecked());
                return true;

            case R.id.menu_filter_unknown:
                onMenuFilter("filter_unknown", !item.isChecked());
                return true;

            case R.id.menu_filter_snoozed:
                onMenuFilter("filter_snoozed", !item.isChecked());
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

            case R.id.menu_theme:
                onMenuTheme();
                return true;

            case R.id.menu_select_language:
                onMenuSelectLanguage();
                return true;

            case R.id.menu_select_all:
            case R.id.menu_select_found:
                onMenuSelectAll();
                return true;

            case R.id.menu_mark_all_read:
                onMenuMarkAllRead();
                return true;

            case R.id.menu_force_sync:
                onMenuForceSync();
                return true;

            case R.id.menu_force_send:
                onSwipeRefresh();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuSearch() {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);

        FragmentDialogSearch fragment = new FragmentDialogSearch();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "search");
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
        Bundle aargs = new Bundle();
        if (EntityFolder.TRASH.equals(type))
            aargs.putString("question", getString(
                    account < 0 ? R.string.title_empty_trash_all_ask : R.string.title_empty_trash_ask));
        else if (EntityFolder.JUNK.equals(type))
            aargs.putString("question", getString(
                    account < 0 ? R.string.title_empty_spam_all_ask : R.string.title_empty_spam_ask));
        else
            throw new IllegalArgumentException("Invalid folder type=" + type);
        aargs.putBoolean("warning", true);
        aargs.putString("remark", getString(R.string.title_empty_all));
        aargs.putLong("account", account);
        aargs.putString("type", type);

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(this, REQUEST_EMPTY_FOLDER);
        ask.show(getParentFragmentManager(), "messages:empty");
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

    private void onMenuFilter(String name, boolean filter) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean(name, filter).apply();
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
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        zoom = ++zoom % 3;
        prefs.edit().putInt("view_zoom", zoom).apply();
        clearMeasurements();
        adapter.setZoom(zoom);
    }

    private void onMenuCompact() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = !prefs.getBoolean("compact", false);
        prefs.edit().putBoolean("compact", compact).apply();

        int zoom = (compact ? 0 : 1);
        prefs.edit().putInt("view_zoom", zoom).apply();

        adapter.setCompact(compact);
        adapter.setZoom(zoom);
        clearMeasurements();
        getActivity().invalidateOptionsMenu();
    }

    private void onMenuTheme() {
        new FragmentDialogTheme().show(getParentFragmentManager(), "messages:theme");
    }

    private void clearMeasurements() {
        sizes.clear();
        heights.clear();
        positions.clear();
    }

    private void onMenuSelectLanguage() {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);

        new SimpleTask<List<String>>() {
            @Override
            protected List<String> onExecute(Context context, Bundle args) {
                long account = args.getLong("account");
                long folder = args.getLong("folder");

                DB db = DB.getInstance(context);
                return db.message().getLanguages(account < 0 ? null : account, folder < 0 ? null : folder);
            }

            @Override
            protected void onExecuted(Bundle args, List<String> languages) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                String current = prefs.getString("filter_language", null);

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), vwAnchor);

                String all = getString(R.string.title_language_all) + (current == null ? " ★" : "");
                popupMenu.getMenu().add(Menu.NONE, 0, 0, all);

                for (int i = 0; i < languages.size(); i++) {
                    String language = languages.get(i);
                    Locale locale = new Locale(language);
                    String title = locale.getDisplayLanguage() + (language.equals(current) ? " ★" : "");
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

    private void onMenuSelectAll() {
        ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);
        model.getIds(getContext(), getViewLifecycleOwner(), new Observer<List<Long>>() {
            @Override
            public void onChanged(List<Long> ids) {
                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        selectionTracker.clearSelection();
                        for (long id : ids)
                            selectionTracker.select(id);

                        Context context = getContext();
                        if (context == null)
                            return;

                        ToastEx.makeText(context,
                                context.getResources().getQuantityString(
                                        R.plurals.title_selected_conversations, ids.size(), ids.size()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void onMenuMarkAllRead() {
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putLong("folder", folder);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                String type = args.getString("type");
                long folder = args.getLong("folder");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
                boolean filter_unknown = prefs.getBoolean("filter_unknown", false);
                boolean filter_snoozed = prefs.getBoolean("filter_snoozed", true);
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
        }.execute(FragmentMessages.this, args, "message:read");
    }

    private void onMenuForceSync() {
        ServiceSynchronize.reload(getContext(), null, true, "force sync");
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
                    (folder.account == null || "connected".equals(folder.accountState)))
                refreshing = true;
        }

        // Get name
        String name;
        if (viewType == AdapterMessage.ViewType.UNIFIED)
            if (type == null)
                name = getString(R.string.title_folder_unified);
            else
                name = EntityFolder.localizeType(getContext(), type);
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

        swipeRefresh.setRefreshing(refreshing);
    }

    private void updateMore() {
        if (selectionTracker != null && selectionTracker.hasSelection()) {
            fabMore.show();

            Context context = tvSelectedCount.getContext();
            int count = selectionTracker.getSelection().size();
            tvSelectedCount.setText(NF.format(count));
            if (count > (BuildConfig.DEBUG ? 10 : MAX_MORE)) {
                int ts = Math.round(tvSelectedCount.getTextSize());
                Drawable w = context.getResources().getDrawable(R.drawable.twotone_warning_24, context.getTheme());
                w.setBounds(0, 0, ts, ts);
                w.setTint(tvSelectedCount.getCurrentTextColor());
                tvSelectedCount.setCompoundDrawablesRelative(null, null, w, null);
                tvSelectedCount.setCompoundDrawablePadding(ts / 2);
            } else
                tvSelectedCount.setCompoundDrawablesRelative(null, null, null, null);
            tvSelectedCount.setVisibility(View.VISIBLE);
        } else {
            fabMore.hide();
            tvSelectedCount.setVisibility(View.GONE);
        }
    }

    private void loadMessages(final boolean top) {
        if (viewType == AdapterMessage.ViewType.THREAD && onclose != null) {
            ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);
            model.observePrevNext(getContext(), getViewLifecycleOwner(), id, new ViewModelMessages.IPrevNext() {
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
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "search:reset");
        } else
            loadMessagesNext(top);
    }

    private void loadMessagesNext(final boolean top) {
        if (top)
            adapter.gotoTop();

        ViewModelMessages model = new ViewModelProvider(getActivity()).get(ViewModelMessages.class);

        ViewModelMessages.Model vmodel = model.getModel(
                getContext(), getViewLifecycleOwner(),
                viewType, type, account, folder, thread, id, filter_archive, criteria, server);

        vmodel.setCallback(getViewLifecycleOwner(), callback);
        vmodel.setObserver(getViewLifecycleOwner(), observer);
    }

    private BoundaryCallbackMessages.IBoundaryCallbackMessages callback = new BoundaryCallbackMessages.IBoundaryCallbackMessages() {
        @Override
        public void onLoading() {
            loading = true;
            updateListState("Loading", SimpleTask.getCount(), adapter.getItemCount());
        }

        @Override
        public void onLoaded(int found) {
            loading = false;
            updateListState("Loaded found=" + found, SimpleTask.getCount(), adapter.getItemCount() + found);
        }

        @Override
        public void onException(@NonNull Throwable ex) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                if (ex instanceof IllegalStateException) {
                    // No internet connection
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
                } else {
                    Bundle args = new Bundle();
                    args.putString("error", Log.formatThrowable(ex, false));

                    FragmentDialogBoundaryError fragment = new FragmentDialogBoundaryError();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentMessages.this, REQUEST_BOUNDARY_RETRY);
                    fragment.show(getParentFragmentManager(), "boundary:error");
                }
        }
    };

    private Observer<PagedList<TupleMessageEx>> observer = new Observer<PagedList<TupleMessageEx>>() {
        @Override
        public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
            if (messages == null)
                return;

            if (viewType == AdapterMessage.ViewType.THREAD) {
                if (handleThreadActions(messages, null, null))
                    return;
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean autoscroll = prefs.getBoolean("autoscroll", false);
                if (autoscroll) {
                    ActivityView activity = (ActivityView) getActivity();
                    if (activity != null &&
                            activity.isFolderUpdated(viewType == AdapterMessage.ViewType.UNIFIED ? -1L : folder))
                        adapter.gotoTop();
                }
            }

            Log.i("Submit messages=" + messages.size());
            adapter.submitList(messages);

            updateExpanded();

            initialized = true;
            updateListState("Observed", SimpleTask.getCount(), messages.size());

            grpReady.setVisibility(View.VISIBLE);
        }
    };

    private void updateListState(String reason, int tasks, int items) {
        Context context = getContext();
        if (context == null)
            return;
        if (getView() == null)
            return;
        if (!getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean filter_seen = prefs.getBoolean("filter_seen", false);
        boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
        boolean filter_unknown = prefs.getBoolean("filter_unknown", false);
        boolean language_detection = prefs.getBoolean("language_detection", false);
        String filter_language = prefs.getString("filter_language", null);
        boolean filter_active = (filter_seen || filter_unflagged || filter_unknown ||
                (language_detection && !TextUtils.isEmpty(filter_language)));

        boolean none = (items == 0 && !loading && tasks == 0 && initialized);
        boolean filtered = (filter_active && viewType != AdapterMessage.ViewType.SEARCH);

        pbWait.setVisibility(loading || tasks > 0 ? View.VISIBLE : View.GONE);
        tvNoEmail.setVisibility(none ? View.VISIBLE : View.GONE);
        tvNoEmailHint.setVisibility(none && filtered ? View.VISIBLE : View.GONE);

        Log.i("List state reason=" + reason +
                " tasks=" + tasks + " loading=" + loading +
                " items=" + items + " initialized=" + initialized +
                " wait=" + (pbWait.getVisibility() == View.VISIBLE) +
                " no=" + (tvNoEmail.getVisibility() == View.VISIBLE));
    }

    private boolean handleThreadActions(
            @NonNull PagedList<TupleMessageEx> messages,
            ArrayList<MessageTarget> targets, List<Long> removed) {
        // Auto close / next
        if (messages.size() == 0 && (autoclose || onclose != null)) {
            handleAutoClose();
            return true;
        }

        // Mark duplicates
        Map<String, List<TupleMessageEx>> duplicates = new HashMap<>();
        for (TupleMessageEx message : messages)
            if (message != null &&
                    !TextUtils.isEmpty(message.hash)) {
                if (!duplicates.containsKey(message.hash))
                    duplicates.put(message.hash, new ArrayList<>());
                duplicates.get(message.hash).add(message);
            }
        for (String hash : duplicates.keySet()) {
            List<TupleMessageEx> dups = duplicates.get(hash);
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

        if (autoExpanded) {
            autoExpanded = false;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            long download = prefs.getInt("download", MessageHelper.DEFAULT_DOWNLOAD_SIZE);
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

                if (message.folder == folder &&
                        !EntityFolder.OUTBOX.equals(message.folderType))
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
                else if (messages.size() > 0) {
                    TupleMessageEx first = messages.get(adapter.getAscending() ? messages.size() - 1 : 0);
                    if (first != null && EntityFolder.OUTBOX.equals(first.folderType))
                        expand = first;
                }

                if (expand != null &&
                        (expand.content || unmetered || (expand.size != null && expand.size < download)))
                    iProperties.setExpanded(expand, true, false);
            }

            // Auto expand all seen messages
            boolean expand_all = prefs.getBoolean("expand_all", false);
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
                                if (!target.isAccross()) {
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
                    if (!target.isAccross() && target.targetFolder.id == folder &&
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

            new SimpleTask<ActionData>() {
                @Override
                protected ActionData onExecute(Context context, Bundle args) {
                    long aid = args.getLong("account");
                    String thread = args.getString("thread");
                    long id = args.getLong("id");

                    EntityAccount account;
                    EntityFolder trash;
                    EntityFolder archive;

                    boolean trashable = false;
                    boolean snoozable = false;
                    boolean archivable = false;

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

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    ActionData data = new ActionData();
                    data.delete = (trash == null ||
                            (account != null && account.protocol == EntityAccount.TYPE_POP));
                    data.trashable = trashable;
                    data.snoozable = snoozable;
                    data.archivable = (archivable && archive != null);
                    return data;
                }

                @Override
                protected void onExecuted(Bundle args, ActionData data) {
                    if (actionbar_color && args.containsKey("color")) {
                        int color = args.getInt("color");
                        bottom_navigation.setBackgroundColor(color);

                        float lum = (float) ColorUtils.calculateLuminance(color);
                        if (lum > LUMINANCE_THRESHOLD)
                            bottom_navigation.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                        else if ((1.0f - lum) > LUMINANCE_THRESHOLD)
                            bottom_navigation.setItemIconTintList(ColorStateList.valueOf(Color.WHITE));
                    }

                    bottom_navigation.setTag(data);

                    bottom_navigation.getMenu().findItem(R.id.action_delete).setVisible(data.trashable);
                    bottom_navigation.getMenu().findItem(R.id.action_snooze).setVisible(data.snoozable);
                    bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(data.archivable);
                    bottom_navigation.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.setLog(false).execute(this, args, "messages:navigation");
        }

        return false;
    }

    private void updateExpanded() {
        int expanded = (values.containsKey("expanded") ? values.get("expanded").size() : 0);
        if (scrolling)
            fabReply.hide();
        else {
            if (expanded == 1) {
                long id = values.get("expanded").get(0);
                int pos = adapter.getPositionForKey(id);
                TupleMessageEx message = adapter.getItemAtPosition(pos);
                if (message != null && !EntityFolder.OUTBOX.equals(message.folderType))
                    fabReply.show();
                else
                    fabReply.hide();
            } else
                fabReply.hide();
        }

        ibDown.setVisibility(quick_scroll && expanded > 0 ? View.VISIBLE : View.GONE);
        ibUp.setVisibility(quick_scroll && expanded > 0 ? View.VISIBLE : View.GONE);
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

                    EntityFolder folder = db.folder().getFolder(message.folder);
                    if (folder == null || folder.account == null)
                        return null;

                    EntityAccount account = db.account().getAccount(folder.account);
                    if (account == null)
                        return null;

                    if (message.ui_unsnoozed)
                        db.message().setMessageUnsnoozed(message.id, false);

                    if (!account.auto_seen && !message.ui_ignored) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean notify_remove = prefs.getBoolean("notify_remove", true);
                        if (notify_remove)
                            db.message().setMessageUiIgnored(message.id, true);
                    }

                    if (account.protocol != EntityAccount.TYPE_IMAP) {
                        if (!message.ui_seen && account.auto_seen)
                            EntityOperation.queue(context, message, EntityOperation.SEEN, true);
                    } else {
                        if (!folder.read_only && account.auto_seen) {
                            int ops = db.operation().getOperationCount(message.folder, message.id, EntityOperation.SEEN);
                            if (!message.seen || ops > 0)
                                EntityOperation.queue(context, message, EntityOperation.SEEN, true);
                        }

                        if (!message.content)
                            EntityOperation.queue(context, message, EntityOperation.BODY);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "expand");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:expand");
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
                if (context == null)
                    finish();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean reversed = prefs.getBoolean("reversed", false);
                navigate(closeId, "previous".equals(onclose) ^ reversed);
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

    private void navigate(long id, final boolean left) {
        if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            return;
        if (navigating)
            return;
        navigating = true;

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
                nargs.putBoolean("found", found);
                nargs.putBoolean("pane", pane);
                nargs.putLong("primary", primary);
                nargs.putBoolean("connected", connected);
                nargs.putBoolean("left", left);

                FragmentMessages fragment = new FragmentMessages();
                fragment.setArguments(nargs);

                int res = (pane ? R.id.content_pane : R.id.content_frame);
                if (getActivity() != null && getActivity().findViewById(res) != null) {
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(res, fragment).addToBackStack("thread");
                    fragmentTransaction.commit();
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "messages:navigate");
    }

    private void moveAsk(final ArrayList<MessageTarget> result, boolean undo, boolean canUndo) {
        if (result.size() == 0)
            return;

        canUndo = true;

        if (undo) {
            moveUndo(result);
            return;
        }

        String key = (result.size() == 1 ? "move_1_confirmed" : "move_n_confirmed");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean(key, false)) {
            if (canUndo)
                moveUndo(result);
            else
                moveAskConfirmed(result);
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
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
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
                        // Prevent new message notification on undo
                        db.message().setMessageUiIgnored(target.id, true);
                        db.message().setMessageLastAttempt(target.id, now);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                if (viewType == AdapterMessage.ViewType.THREAD) {
                    PagedList<TupleMessageEx> messages = adapter.getCurrentList();
                    if (messages != null && result.size() > 0) {
                        Log.i("Eval undo messages=" + messages.size() + " targets=" + result.size());
                        handleThreadActions(messages, result, null);
                    }
                }

                SimpleTask<Void> move = new SimpleTask<Void>() {
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
                                db.message().setMessageLastAttempt(target.id, new Date().getTime());
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

                SimpleTask<Void> show = new SimpleTask<Void>() {
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
                                db.message().setMessageLastAttempt(target.id, new Date().getTime());
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

                FragmentActivity activity = getActivity();
                if (!(activity instanceof ActivityView)) {
                    Log.e("Undo: activity missing");
                    return;
                }

                String title = getString(R.string.title_move_undo, getDisplay(result, true), result.size());
                ((ActivityView) activity).undo(title, args, move, show);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "undo:hide");
    }

    private static String getDisplay(ArrayList<MessageTarget> result, boolean dest) {
        boolean across = false;
        for (MessageTarget target : result)
            if (target.isAccross())
                across = true;

        List<String> displays = new ArrayList<>();
        for (MessageTarget target : result) {
            String display = "";
            if (across)
                display += (dest ? target.targetAccount.name : target.sourceAccount.name) + "/";
            display += (dest ? target.targetFolder.display : target.sourceFolder.display);
            if (!displays.contains(display))
                displays.add(display);
        }

        Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
        Collections.sort(displays, collator);

        return TextUtils.join(", ", displays);
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
            for (EntityAttachment attachment : attachments) {
                if (attachment.encryption != null) {
                    inline = false;
                    break;
                }
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

            File file = message.getFile(context);
            Helper.writeText(file, null);
            db.message().setMessageContent(message.id, true, null, null, null, null);
            //db.message().setMessageSubject(id, subject);
            db.attachment().deleteAttachments(message.id);
            db.message().setMessageEncrypt(message.id, message.ui_encrypt);
            db.message().setMessageStored(message.id, new Date().getTime());

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private ActivityBase.IKeyPressedListener onBackPressedListener = new ActivityBase.IKeyPressedListener() {
        @Override
        public boolean onKeyPressed(KeyEvent event) {
            Context context = getContext();
            if (context == null)
                return false;
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
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
                        return (down && onScroll(context, true));
                    break;
                case KeyEvent.KEYCODE_PAGE_DOWN:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (viewType == AdapterMessage.ViewType.THREAD)
                        return (down && onScroll(context, false));
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

        @Override
        public boolean onBackPressed() {
            if (selectionTracker != null && selectionTracker.hasSelection()) {
                selectionTracker.clearSelection();
                return true;
            }

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
                rvMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            adapter.notifyDataSetChanged();
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
                return true;
            }

            handleExit();

            return false;
        }

        private boolean onNext(Context context) {
            if (next == null) {
                Animation bounce = AnimationUtils.loadAnimation(context, R.anim.bounce_left);
                view.startAnimation(bounce);
            } else
                navigate(next, false);
            return true;
        }

        private boolean onPrevious(Context context) {
            if (prev == null) {
                Animation bounce = AnimationUtils.loadAnimation(context, R.anim.bounce_right);
                view.startAnimation(bounce);
            } else
                navigate(prev, true);
            return true;
        }

        private boolean onArchive(Context context) {
            if (bottom_navigation == null ||
                    !bottom_navigation.isEnabled() ||
                    bottom_navigation.getVisibility() != View.VISIBLE)
                return false;
            MenuItem archive = bottom_navigation.getMenu().findItem(R.id.action_archive);
            if (archive == null || !archive.isVisible() || !archive.isEnabled())
                return false;
            bottom_navigation.getMenu().performIdentifierAction(R.id.action_archive, 0);
            return true;
        }

        private boolean onDelete(Context context) {
            if (bottom_navigation == null ||
                    !bottom_navigation.isEnabled() ||
                    bottom_navigation.getVisibility() != View.VISIBLE)
                return false;
            MenuItem delete = bottom_navigation.getMenu().findItem(R.id.action_delete);
            if (delete == null || !delete.isVisible() || !delete.isEnabled())
                return false;
            bottom_navigation.getMenu().performIdentifierAction(R.id.action_delete, 0);
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

        private boolean onScroll(Context context, boolean up) {
            rvMessage.scrollBy(0, (up ? -1 : 1) *
                    context.getResources().getDisplayMetrics().heightPixels / 2);
            return true;
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

    private BroadcastReceiver treceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onTaskCount(intent);
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                String action = intent.getAction();
                if (ACTION_STORE_RAW.equals(action))
                    onStoreRaw(intent);
                else if (ACTION_DECRYPT.equals(action))
                    onDecrypt(intent);
                else if (ACTION_KEYWORDS.equals(action))
                    onKeywords(intent);
            }
        }
    };

    private void onTaskCount(Intent intent) {
        updateListState("Tasks", intent.getIntExtra("count", 0), adapter.getItemCount());
    }

    private void onStoreRaw(Intent intent) {
        message = intent.getLongExtra("id", -1);
        String subject = intent.getStringExtra("subject");
        String name = (TextUtils.isEmpty(subject) ? "email" : Helper.sanitizeFilename(subject)) + ".eml";

        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.setType("*/*");
        create.putExtra(Intent.EXTRA_TITLE, name);
        Helper.openAdvanced(create);
        PackageManager pm = getContext().getPackageManager();
        if (create.resolveActivity(pm) == null) // system whitelisted
            Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG)
                    .setGestureInsetBottomIgnored(true).show();
        else
            startActivityForResult(Helper.getChooser(getContext(), create), REQUEST_RAW);
    }

    private void onDecrypt(Intent intent) {
        long id = intent.getLongExtra("id", -1);
        boolean auto = intent.getBooleanExtra("auto", false);
        int type = intent.getIntExtra("type", EntityMessage.ENCRYPT_NONE);

        final Bundle args = new Bundle();
        args.putLong("id", id);
        args.putInt("type", type);

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
                    Boolean auto = args.getBoolean("auto");
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
                            Snackbar snackbar = Snackbar.make(view, R.string.title_no_key, Snackbar.LENGTH_LONG)
                                    .setGestureInsetBottomIgnored(true);
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
            if (pgpService.isBound()) {
                Intent data = new Intent();
                data.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);
                data.putExtra(BuildConfig.APPLICATION_ID, id);
                onPgp(data, auto);
            } else {
                Snackbar snackbar = Snackbar.make(view, R.string.title_no_openpgp, Snackbar.LENGTH_LONG)
                        .setGestureInsetBottomIgnored(true);
                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.viewFAQ(v.getContext(), 12);
                    }
                });
                snackbar.show();
            }
        }
    }

    private void onKeywords(Intent intent) {
        rvMessage.post(new Runnable() {
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
                        onPgp(data, false);
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
                            startActivity(new Intent(getContext(), ActivityBilling.class));
                            return;
                        }

                        Bundle args = data.getBundleExtra("args");
                        onActionFlagSelection(true, args.getInt("color"), null);
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

                OutputStream os = null;
                InputStream is = null;
                try {
                    os = context.getContentResolver().openOutputStream(uri);
                    is = new FileInputStream(file);

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
                Snackbar.make(view, R.string.title_raw_saved, Snackbar.LENGTH_LONG)
                        .setGestureInsetBottomIgnored(true).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof FileNotFoundException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else if (!(ex instanceof MessageRemovedException))
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "raw:save");
    }

    private void onPgp(Intent data, boolean auto) {
        Bundle args = new Bundle();
        args.putParcelable("data", data);
        args.putBoolean("auto", auto);

        new SimpleTask<PendingIntent>() {
            @Override
            protected PendingIntent onExecute(Context context, Bundle args) throws Throwable {
                // Get arguments
                boolean auto = args.getBoolean("auto");
                Intent data = args.getParcelable("data");
                long id = data.getLongExtra(BuildConfig.APPLICATION_ID, -1);

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    return null;
                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                if (attachments == null)
                    return null;

                InputStream in = null;
                OutputStream out = null;
                boolean inline = false;
                File plain = File.createTempFile("plain", "." + message.id, context.getCacheDir());

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
                            String body = Helper.readText(file);
                            int begin = body.indexOf(Helper.PGP_BEGIN_MESSAGE);
                            int end = body.indexOf(Helper.PGP_END_MESSAGE);
                            if (begin >= 0 && begin < end) {
                                String[] lines = body
                                        .substring(begin, end + Helper.PGP_END_MESSAGE.length())
                                        .replace("<br>", "\r\n")
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
                        String[] param = message.autocrypt.split(";");
                        for (int i = 0; i < param.length; i++) {
                            int e = param[i].indexOf("=");
                            if (e > 0) {
                                String key = param[i].substring(0, e).trim().toLowerCase(Locale.ROOT);
                                String value = param[i].substring(e + 1);
                                Log.i("Autocrypt " + key + "=" + value);
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
                    Log.i("Executing " + data.getAction());
                    Log.logExtras(data);
                    OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
                    result = api.executeApi(data, in, out);

                    int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
                    Log.i("Result action=" + data.getAction() + " code=" + resultCode);
                    Log.logExtras(data);
                    switch (resultCode) {
                        case OpenPgpApi.RESULT_CODE_SUCCESS:
                            if (out != null)
                                if (inline) {
                                    try {
                                        db.beginTransaction();

                                        // Write decrypted body
                                        String text = Helper.readText(plain);
                                        String html = "<div x-plain=\"true\">" + HtmlHelper.formatPre(text) + "</div>";
                                        Helper.writeText(message.getFile(context), html);
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
                                    Properties props = MessageHelper.getSessionProperties();
                                    Session isession = Session.getInstance(props, null);
                                    MimeMessage imessage;
                                    try (InputStream fis = new FileInputStream(plain)) {
                                        imessage = new MimeMessage(isession, fis);
                                    }

                                    MessageHelper helper = new MessageHelper(imessage, context);
                                    parts = helper.getMessageParts();
                                    String subject = parts.getProtectedSubject();
                                    if (subject != null)
                                        db.message().setMessageSubject(message.id, subject);

                                    try {
                                        db.beginTransaction();

                                        // Write decrypted body
                                        String html = parts.getHtml(context);
                                        Helper.writeText(message.getFile(context), html);

                                        // Remove existing attachments
                                        db.attachment().deleteAttachments(message.id);

                                        // Add decrypted attachments
                                        List<EntityAttachment> remotes = parts.getAttachments();
                                        for (int index = 0; index < remotes.size(); index++) {
                                            EntityAttachment remote = remotes.get(index);
                                            remote.message = message.id;
                                            remote.sequence = index + 1;
                                            remote.id = db.attachment().insertAttachment(remote);
                                            try {
                                                parts.downloadAttachment(context, index, remote);
                                            } catch (Throwable ex) {
                                                Log.e(ex);
                                            }
                                        }

                                        db.message().setMessageEncrypt(message.id, parts.getEncryption());
                                        db.message().setMessageStored(message.id, new Date().getTime());
                                        db.message().setMessageFts(message.id, false);

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

                            if (sresult == RESULT_NO_SIGNATURE)
                                args.putString("sigresult", context.getString(R.string.title_signature_none));
                            else if (sresult == RESULT_VALID_KEY_CONFIRMED || sresult == RESULT_VALID_KEY_UNCONFIRMED) {
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
                                String text = context.getString(R.string.title_signature_invalid_reason, Integer.toString(sresult));
                                args.putString("sigresult", text);
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
                    plain.delete();
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, PendingIntent pi) {
                if (args.containsKey("sigresult")) {
                    String text = args.getString("sigresult");
                    Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                }

                if (pi != null)
                    try {
                        Log.i("Executing pi=" + pi);
                        startIntentSenderForResult(
                                pi.getIntentSender(),
                                REQUEST_OPENPGP,
                                null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        // Likely cancelled
                        Log.w(ex);
                    }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException) {
                    Log.i(ex);
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "decrypt:pgp");
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
                                    String fingerprint = EntityCertificate.getFingerprint(cert);
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

                                            for (X509Certificate c : certs) {
                                                boolean[] usage = c.getKeyUsage();
                                                boolean root = (usage != null && usage[5]);
                                                boolean selfSigned = c.getIssuerX500Principal().equals(c.getSubjectX500Principal());
                                                if (root && !selfSigned && ks.getCertificateAlias(c) == null) {
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

                    if (result == null)
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

                    // Get encrypted message
                    File input = null;
                    List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                    for (EntityAttachment attachment : attachments)
                        if (EntityAttachment.SMIME_MESSAGE.equals(attachment.encryption)) {
                            if (!attachment.available)
                                throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                            input = attachment.getFile(context);
                            break;
                        }

                    if (input == null)
                        throw new IllegalArgumentException("Encrypted message missing");

                    int count = -1;
                    boolean decoded = false;
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
                                        } catch (CMSException ex) {
                                            Log.w(ex);
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
                                    }
                                } else
                                    break; // out of recipients
                            }

                            count++;
                        }

                    if (!decoded) {
                        if (message.identity != null)
                            db.identity().setIdentitySignKeyAlias(message.identity, null);
                        throw new IllegalArgumentException(context.getString(R.string.title_unknown_key));
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
                        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                                .setGestureInsetBottomIgnored(true).show();
                    } else
                        try {
                            String sender = args.getString("sender");
                            Date time = (Date) args.getSerializable("time");
                            boolean known = args.getBoolean("known");
                            boolean valid = args.getBoolean("valid");
                            String reason = args.getString("reason");
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
                                Snackbar.make(view, R.string.title_signature_valid, Snackbar.LENGTH_LONG)
                                        .setGestureInsetBottomIgnored(true).show();
                            else {
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

                                ibInfo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        StringBuilder sb = new StringBuilder();
                                        for (int i = 0; i < trace.size(); i++) {
                                            if (i > 0)
                                                sb.append("\n\n");
                                            sb.append(i + 1).append(") ").append(trace.get(i));
                                        }

                                        new AlertDialog.Builder(getContext())
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

                                                        String fingerprint = EntityCertificate.getFingerprint(cert);
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
                                                Log.unexpectedError(getParentFragmentManager(), ex);
                                            }
                                        }
                                    });

                                builder.show();
                            }
                        } catch (Throwable ex) {
                            Snackbar.make(view, Log.formatThrowable(ex), Snackbar.LENGTH_LONG)
                                    .setGestureInsetBottomIgnored(true).show();
                        }
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException ||
                        ex instanceof CMSException || ex instanceof KeyChainException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }

            private void decodeMessage(Context context, InputStream is, EntityMessage message, Bundle args) throws MessagingException, IOException {
                String alias = args.getString("alias");
                boolean duplicate = args.getBoolean("duplicate");

                // Decode message
                Properties props = MessageHelper.getSessionProperties();
                Session isession = Session.getInstance(props, null);
                MimeMessage imessage = new MimeMessage(isession, is);
                MessageHelper helper = new MessageHelper(imessage, context);
                MessageHelper.MessageParts parts = helper.getMessageParts();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    // Write decrypted body
                    String html = parts.getHtml(context);
                    Helper.writeText(message.getFile(context), html);
                    Log.i("s/mime html=" + (html == null ? null : html.length()));

                    // Remove existing attachments
                    db.attachment().deleteAttachments(message.id);

                    // Add decrypted attachments
                    List<EntityAttachment> remotes = parts.getAttachments();
                    for (int index = 0; index < remotes.size(); index++) {
                        EntityAttachment remote = remotes.get(index);
                        remote.message = message.id;
                        remote.sequence = index + 1;
                        remote.id = db.attachment().insertAttachment(remote);
                        try {
                            parts.downloadAttachment(context, index, remote);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                        Log.i("s/mime attachment=" + remote);
                    }

                    db.message().setMessageEncrypt(message.id, parts.getEncryption());
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
                        boolean keyCertSign = (usage != null && usage[5]);
                        boolean selfSigned = cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal());
                        EntityCertificate record = EntityCertificate.from(cert, null);
                        trace.add(record.subject +
                                " (" + (selfSigned ? "selfSigned" : cert.getIssuerX500Principal()) + ")" +
                                (keyCertSign ? " (keyCertSign)" : "") +
                                (ks != null && ks.getCertificateAlias(cert) != null ? " (Android)" : ""));
                    } catch (Throwable ex) {
                        Log.e(ex);
                        trace.add(ex.toString());
                    }
                return trace;
            }
        }.execute(this, args, "decrypt:s/mime");
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

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel("send:" + id, 1);

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
                List<String> whitelist = EmailProvider.getDomainNames(context);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                    if (junk == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_junk_folder));

                    if (!message.folder.equals(junk.id))
                        EntityOperation.queue(context, message, EntityOperation.MOVE, junk.id);

                    if (block_sender || block_domain) {
                        EntityRule rule = EntityRule.blockSender(context, message, junk, block_domain, whitelist);
                        if (rule != null) {
                            if (message.folder.equals(junk.id)) {
                                EntityFolder inbox = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                                if (inbox == null)
                                    rule = null;
                                else
                                    rule.folder = inbox.id;
                            }
                        }
                        if (rule != null)
                            rule.id = db.rule().insertRule(rule);
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
                    Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(getContext(), ActivitySetup.class));
                        }
                    });
                    snackbar.show();
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:junk");
    }

    private void onMoveAskAcross(final ArrayList<MessageTarget> result) {
        boolean across = false;
        for (MessageTarget target : result)
            if (target.isAccross()) {
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

                ServiceSynchronize.eval(context, "flag");

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
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
                    for (EntityMessage threaded : messages)
                        if (threaded.ui_unsnoozed && wakeup == null)
                            db.message().setMessageUnsnoozed(threaded.id, false);
                        else {
                            db.message().setMessageSnoozed(threaded.id, wakeup);
                            db.message().setMessageUiIgnored(threaded.id, true);
                            if (!hide && flag_snoozed && threaded.folder.equals(message.folder))
                                EntityOperation.queue(context, threaded, EntityOperation.FLAG, wakeup != null);
                            EntityMessage.snooze(context, threaded.id, wakeup);
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
                                message.account, message.thread, threading ? null : id, message.folder);
                        for (EntityMessage threaded : messages)
                            if (threaded.ui_unsnoozed && wakeup == null)
                                db.message().setMessageUnsnoozed(threaded.id, false);
                            else {
                                db.message().setMessageSnoozed(threaded.id, wakeup);
                                db.message().setMessageUiIgnored(message.id, true);
                                if (flag_snoozed && threaded.folder.equals(message.folder))
                                    EntityOperation.queue(context, threaded, EntityOperation.FLAG, wakeup != null);
                                EntityMessage.snooze(context, threaded.id, wakeup);
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
                            if (sourceFolder == null || sourceFolder.read_only)
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
                    moveAsk(result, false, !autoclose && onclose == null);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:move");
    }

    private void onPrint(Bundle args) {
        new SimpleTask<String[]>() {
            @Override
            protected String[] onExecute(Context context, Bundle args) throws IOException {
                long id = args.getLong("id");
                boolean headers = args.getBoolean("headers");

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null || !message.content)
                    return null;

                File file = message.getFile(context);
                if (!file.exists())
                    return null;

                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                if (attachments == null)
                    return null;

                Document document = JsoupEx.parse(file);
                HtmlHelper.embedInlineImages(context, id, document, true);

                // Prevent multiple pages for Microsoft Office
                Element section = document.select(".WordSection1").first();
                if (section == null)
                    section = document.body();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean print_html_header = prefs.getBoolean("print_html_header", true);
                if (print_html_header) {
                    Element header = document.createElement("p");

                    if (message.from != null && message.from.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_from));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.from));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (message.to != null && message.to.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_to));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.to));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (message.cc != null && message.cc.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_cc));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.cc));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (message.received != null) {
                        DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.LONG, SimpleDateFormat.LONG);

                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_received));
                        span.appendChild(strong);
                        span.appendText(" " + DTF.format(message.received));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (!TextUtils.isEmpty(message.subject)) {
                        Element span = document.createElement("span");
                        span.appendText(message.subject);
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (headers && message.headers != null) {
                        header.appendElement("hr");
                        Element pre = document.createElement("pre");
                        pre.text(message.headers);
                        header.appendChild(pre);
                    }

                    header.appendElement("hr").appendElement("br");

                    section.prependChild(header);

                    boolean hasAttachments = false;
                    Element footer = document.createElement("p");
                    footer.appendElement("br").appendElement("hr");
                    for (EntityAttachment attachment : attachments)
                        if (attachment.isAttachment()) {
                            hasAttachments = true;
                            Element strong = document.createElement("strong");
                            strong.text(context.getString(R.string.title_attachment));
                            footer.appendChild(strong);
                            if (!TextUtils.isEmpty(attachment.name))
                                footer.appendText(" " + attachment.name);
                            if (attachment.size != null)
                                footer.appendText(" " + Helper.humanReadableByteCount(attachment.size));
                            footer.appendElement("br");
                        }

                    if (hasAttachments)
                        section.appendChild(footer);
                }

                return new String[]{message.subject, section.html()};
            }

            @Override
            protected void onExecuted(Bundle args, final String[] data) {
                if (data == null) {
                    Log.w("Print no data");
                    return;
                }

                ActivityBase activity = (ActivityBase) getActivity();
                if (activity == null) {
                    Log.w("Print no activity");
                    return;
                }

                final Context context = activity.getOriginalContext();

                // https://developer.android.com/training/printing/html-docs.html
                printWebView = new WebView(context);

                WebSettings settings = printWebView.getSettings();
                settings.setLoadsImagesAutomatically(true);
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                settings.setAllowFileAccess(false);

                printWebView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        Log.i("Print page finished");

                        try {
                            if (printWebView == null) {
                                Log.w("Print no view");
                                return;
                            }

                            PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                            String jobName = getString(R.string.app_name);
                            if (!TextUtils.isEmpty(data[0]))
                                jobName += " - " + data[0];

                            Log.i("Print queue job=" + jobName);
                            PrintDocumentAdapter adapter = printWebView.createPrintDocumentAdapter(jobName);
                            PrintJob job = printManager.print(jobName, adapter, new PrintAttributes.Builder().build());
                            EntityLog.log(context, "Print queued job=" + job.getInfo());
                        } catch (Throwable ex) {
                            try {
                                Log.unexpectedError(getParentFragmentManager(), ex, !(ex instanceof ActivityNotFoundException));
                            } catch (IllegalStateException exex) {
                                ToastEx.makeText(context, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
                            }
                        } finally {
                            printWebView = null;
                        }
                    }
                });

                Log.i("Print load data");
                printWebView.loadDataWithBaseURL("about:blank", data[1], "text/html", StandardCharsets.UTF_8.name(), null);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "message:print");
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
                        accounts = db.account().getSynchronizingAccounts();
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

                            if (message.uid != null || account.protocol == EntityAccount.TYPE_POP)
                                db.message().setMessageUiHide(message.id, true);
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
            long account, long folder, boolean server, String query) {
        search(context, owner, manager,
                account, folder,
                server, new BoundaryCallbackMessages.SearchCriteria(query));
    }

    static void search(
            final Context context, final LifecycleOwner owner, final FragmentManager manager,
            long account, long folder, boolean server, BoundaryCallbackMessages.SearchCriteria criteria) {
        if (server && !ActivityBilling.isPro(context)) {
            context.startActivity(new Intent(context, ActivityBilling.class));
            return;
        }

        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            manager.popBackStack("search", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);
        args.putBoolean("server", server);
        args.putSerializable("criteria", criteria);

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("search");
        fragmentTransaction.commit();
    }

    private static class ActionData {
        private boolean delete;
        private boolean trashable;
        private boolean snoozable;
        private boolean archivable;
    }

    private class ReplyData {
        List<TupleIdentityEx> identities;
        List<EntityAnswer> answers;
    }

    private class MoreResult {
        boolean seen;
        boolean unseen;
        boolean visible;
        boolean hidden;
        boolean flagged;
        boolean unflagged;
        Integer importance;
        Boolean hasArchive;
        Boolean hasTrash;
        Boolean hasJunk;
        Boolean isInbox;
        Boolean isArchive;
        Boolean isTrash;
        Boolean isJunk;
        Boolean isDrafts;
        List<Long> folders;
        List<EntityAccount> accounts;
        EntityAccount copyto;
    }

    public static class MessageTarget implements Parcelable {
        long id;
        Account sourceAccount;
        Folder sourceFolder;
        Account targetAccount;
        Folder targetFolder;
        boolean copy;

        MessageTarget(Context context, EntityMessage message,
                      EntityAccount sourceAccount, EntityFolder sourceFolder,
                      EntityAccount targetAccount, EntityFolder targetFolder) {
            this(context, message, sourceAccount, sourceFolder, targetAccount, targetFolder, false);
        }

        MessageTarget(Context context, EntityMessage message,
                      EntityAccount sourceAccount, EntityFolder sourceFolder,
                      EntityAccount targetAccount, EntityFolder targetFolder,
                      boolean copy) {
            this.id = message.id;
            this.sourceAccount = new Account(sourceAccount);
            this.sourceFolder = new Folder(context, sourceFolder);
            this.targetAccount = new Account(targetAccount);
            this.targetFolder = new Folder(context, targetFolder);
            this.copy = copy;
        }

        protected MessageTarget(Parcel in) {
            id = in.readLong();
            sourceAccount = (Account) in.readSerializable();
            sourceFolder = (Folder) in.readSerializable();
            targetAccount = (Account) in.readSerializable();
            targetFolder = (Folder) in.readSerializable();
            copy = (in.readInt() != 0);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeSerializable(sourceAccount);
            dest.writeSerializable(sourceFolder);
            dest.writeSerializable(targetAccount);
            dest.writeSerializable(targetFolder);
            dest.writeInt(copy ? 1 : 0);
        }

        boolean isAccross() {
            return (sourceAccount.id != targetAccount.id);
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

    public static class FragmentDialogReporting extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_error_reporting, null);
            Button btnInfo = dview.findViewById(R.id.btnInfo);

            btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(v.getContext(), 104);
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
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putBoolean("crash_reports_asked", true).apply();
                        }
                    })
                    .create();
        }
    }

    public static class FragmentDialogReview extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review, null);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putBoolean("review_asked", true).apply();
                            startActivity(Helper.getIntentRate(getContext()));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putBoolean("review_asked", true).apply();
                        }
                    })
                    .setNeutralButton(R.string.title_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            prefs.edit().putLong("review_later", new Date().getTime()).apply();
                        }
                    })
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
                    Log.unexpectedError(getParentFragmentManager(), ex);
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
            final Spinner spIdentity = dview.findViewById(R.id.spIdentity);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            final Button btnFix = dview.findViewById(R.id.btnFix);
            final Group grpIdentities = dview.findViewById(R.id.grpIdentities);
            final Group grpNoIdentities = dview.findViewById(R.id.grpNoIdentities);
            final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

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
                    DB db = DB.getInstance(context);
                    return db.identity().getComposableIdentities(null);
                }

                @Override
                protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                    AdapterIdentitySelect iadapter = new AdapterIdentitySelect(getContext(), identities);
                    spIdentity.setAdapter(iadapter);

                    Integer fallback = null;
                    long account = getArguments().getLong("account");
                    for (int pos = 0; pos < identities.size(); pos++) {
                        EntityIdentity identity = identities.get(pos);
                        if (identity.account.equals(account)) {
                            if (identity.primary) {
                                fallback = null;
                                spIdentity.setSelection(pos);
                                break;
                            }
                            if (fallback == null)
                                fallback = pos;
                        }
                    }
                    if (fallback != null)
                        spIdentity.setSelection(fallback);

                    grpIdentities.setVisibility(identities.size() > 0 ? View.VISIBLE : View.GONE);
                    grpNoIdentities.setVisibility(identities.size() > 0 ? View.GONE : View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "identity:select");

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TupleIdentityEx identity = (TupleIdentityEx) spIdentity.getSelectedItem();
                            if (identity != null)
                                startActivity(new Intent(getContext(), ActivityCompose.class)
                                        .putExtra("action", "new")
                                        .putExtra("account", identity.account)
                                        .putExtra("identity", identity.id)
                                );
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogBoundaryError extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            String error = getArguments().getString("error");

            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_boundary_error, null);
            TextView tvError = dview.findViewById(R.id.tvError);

            tvError.setText(error);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(R.string.title_boundary_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendResult(Activity.RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendResult(Activity.RESULT_CANCELED);
                        }
                    })
                    .create();
        }
    }

    public static class FragmentMoveAsk extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            String notagain = getArguments().getString("notagain");
            ArrayList<MessageTarget> result = getArguments().getParcelableArrayList("result");

            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ask_move, null);
            TextView tvMessages = dview.findViewById(R.id.tvMessages);
            TextView tvSourceFolders = dview.findViewById(R.id.tvSourceFolders);
            TextView tvTargetFolders = dview.findViewById(R.id.tvTargetFolders);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            String question = getResources()
                    .getQuantityString(R.plurals.title_moving_messages,
                            result.size(), result.size());

            tvMessages.setText(question);
            tvSourceFolders.setText(getDisplay(result, false));
            tvTargetFolders.setText(getDisplay(result, true));

            List<String> sources = new ArrayList<>();
            List<String> targets = new ArrayList<>();
            Integer sourceColor = null;
            Integer targetColor = null;
            for (MessageTarget t : result) {
                if (!sources.contains(t.sourceFolder.type))
                    sources.add(t.sourceFolder.type);
                if (!targets.contains(t.targetFolder.type))
                    targets.add(t.targetFolder.type);
                if (sourceColor == null)
                    sourceColor = t.sourceFolder.color;
                if (targetColor == null)
                    targetColor = t.targetFolder.color;
            }

            Drawable source = null;
            if (sources.size() == 1) {
                source = getResources().getDrawable(EntityFolder.getIcon(sources.get(0)), null);
                if (source != null)
                    source.setBounds(0, 0, source.getIntrinsicWidth(), source.getIntrinsicHeight());
                if (sourceColor == null)
                    sourceColor = EntityFolder.getDefaultColor(sources.get(0));
            } else
                sourceColor = null;

            Drawable target = null;
            if (targets.size() == 1) {
                target = getResources().getDrawable(EntityFolder.getIcon(targets.get(0)), null);
                if (target != null)
                    target.setBounds(0, 0, target.getIntrinsicWidth(), target.getIntrinsicHeight());
                if (targetColor == null)
                    targetColor = EntityFolder.getDefaultColor(targets.get(0));
            } else
                targetColor = null;

            tvSourceFolders.setCompoundDrawablesRelative(source, null, null, null);
            tvTargetFolders.setCompoundDrawablesRelative(target, null, null, null);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (sourceColor != null)
                    tvSourceFolders.setCompoundDrawableTintList(ColorStateList.valueOf(sourceColor));
                if (targetColor != null)
                    tvTargetFolders.setCompoundDrawableTintList(ColorStateList.valueOf(targetColor));
            }

            if (notagain != null)
                cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        prefs.edit().putBoolean(notagain, isChecked).apply();
                    }
                });

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendResult(Activity.RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendResult(Activity.RESULT_CANCELED);
                        }
                    })
                    .create();
        }
    }
}
