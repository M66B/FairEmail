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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.SystemFonts;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

public class FragmentOptionsMisc extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean resumed = false;
    private List<Pair<String, String>> languages = new ArrayList<>();

    private View view;
    private ImageButton ibHelp;

    private SwitchCompat swPowerMenu;
    private SwitchCompat swSendSelf;
    private SwitchCompat swExternalSearch;
    private SwitchCompat swSortAnswers;
    private SwitchCompat swExternalAnswer;
    private SwitchCompat swShortcuts;
    private SwitchCompat swICalTentative;
    private ImageButton ibICalTentative;
    private SwitchCompat swFts;
    private ImageButton ibFts;
    private TextView tvFtsIndexed;
    private TextView tvFtsPro;
    private SwitchCompat swClassification;
    private TextView tvClassMinProbability;
    private SeekBar sbClassMinProbability;
    private TextView tvClassMinDifference;
    private SeekBar sbClassMinDifference;
    private SwitchCompat swShowFiltered;
    private SwitchCompat swHapticFeedback;
    private SwitchCompat swHapticFeedbackSwipe;
    private ImageButton ibClassification;
    private Spinner spLanguage;
    private SwitchCompat swUpdates;
    private TextView tvGithubPrivacy;
    private ImageButton ibChannelUpdated;
    private SwitchCompat swCheckWeekly;
    private SwitchCompat swBeta;
    private TextView tvBitBucketPrivacy;
    private SwitchCompat swChangelog;
    private SwitchCompat swAnnouncements;
    private TextView tvAnnouncementsPrivacy;
    private SwitchCompat swCrashReports;
    private TextView tvUuid;
    private ImageButton ibCrashReports;
    private Button btnReset;
    private SwitchCompat swCleanupAttachments;
    private Button btnCleanup;
    private TextView tvLastCleanup;
    private TextView tvSdcard;
    private SwitchCompat swGoogleBackup;
    private TextView tvGoogleBackupPrivacy;

    private CardView cardAdvanced;
    private SwitchCompat swWatchdog;
    private SwitchCompat swExperiments;
    private TextView tvExperimentsHint;
    private SwitchCompat swMainLog;
    private SwitchCompat swMainLogMem;
    private SwitchCompat swProtocol;
    private SwitchCompat swLogInfo;
    private SwitchCompat swDebug;
    private SwitchCompat swCanary;
    private SwitchCompat swTest1;
    private SwitchCompat swTest2;
    private SwitchCompat swTest3;
    private SwitchCompat swTest4;
    private SwitchCompat swTest5;

    private Button btnRepair;
    private Button btnDaily;
    private TextView tvLastDaily;
    private SwitchCompat swAutostart;
    private SwitchCompat swEmergency;
    private SwitchCompat swWorkManager;
    private SwitchCompat swTaskDescription;
    private SwitchCompat swExternalStorage;
    private TextView tvExternalStorageFolder;
    private SwitchCompat swIntegrity;
    private SwitchCompat swWal;
    private SwitchCompat swCheckpoints;
    private SwitchCompat swAnalyze;
    private SwitchCompat swAutoVacuum;
    private SwitchCompat swSyncExtra;
    private TextView tvSqliteCache;
    private SeekBar sbSqliteCache;
    private ImageButton ibSqliteCache;
    private SwitchCompat swCacheLists;
    private SwitchCompat swOauthTabs;
    private TextView tvStartDelay;
    private SeekBar sbStartDelay;
    private TextView tvRangeSize;
    private SeekBar sbRangeSize;
    private TextView tvChunkSize;
    private SeekBar sbChunkSize;
    private TextView tvThreadRange;
    private SeekBar sbThreadRange;
    private TextView tvRestartInterval;
    private SeekBar sbRestartInterval;
    private SwitchCompat swAutoScroll;
    private SwitchCompat swUndoManager;
    private SwitchCompat swBrowserZoom;
    private EditText etViewportHeight;
    private SwitchCompat swIgnoreFormattedSize;
    private SwitchCompat swShowRecent;
    private SwitchCompat swModSeq;
    private SwitchCompat swPreamble;
    private SwitchCompat swUid;
    private SwitchCompat swExpunge;
    private SwitchCompat swUidExpunge;
    private SwitchCompat swAuthPlain;
    private SwitchCompat swAuthLogin;
    private SwitchCompat swAuthNtlm;
    private SwitchCompat swAuthSasl;
    private SwitchCompat swAuthApop;
    private SwitchCompat swUseTop;
    private SwitchCompat swForgetTop;
    private SwitchCompat swKeepAlivePoll;
    private SwitchCompat swEmptyPool;
    private SwitchCompat swIdleDone;
    private SwitchCompat swFastFetch;
    private TextView tvMaxBackoff;
    private SeekBar sbMaxBackOff;
    private SwitchCompat swLogarithmicBackoff;
    private SwitchCompat swExactAlarms;
    private SwitchCompat swNativeDkim;
    private SwitchCompat swNativeArc;
    private EditText etNativeArcWhitelist;
    private SwitchCompat swStrictAlignment;
    private SwitchCompat swSvg;
    private SwitchCompat swWebp;
    private SwitchCompat swAnimate;
    private SwitchCompat swPreviewHidden;
    private SwitchCompat swPreviewQuotes;
    private SwitchCompat swEasyCorrect;
    private SwitchCompat swPastePlain;
    private SwitchCompat swPasteQuote;
    private EditText etFaviconUri;
    private SwitchCompat swEmailJunk;
    private SwitchCompat swInfra;
    private SwitchCompat swTldFlags;
    private SwitchCompat swJsonLd;
    private SwitchCompat swDupMsgId;
    private SwitchCompat swThreadByRef;
    private SwitchCompat swSaveUserFlags;
    private SwitchCompat swMdn;
    private SwitchCompat swAppChooser;
    private SwitchCompat swAppChooserShare;
    private SwitchCompat swShareTask;
    private SwitchCompat swAdjacentLinks;
    private SwitchCompat swAdjacentDocuments;
    private SwitchCompat swAdjacentPortrait;
    private SwitchCompat swAdjacentLandscape;
    private SwitchCompat swDeleteConfirmation;
    private SwitchCompat swDeleteNotification;
    private SwitchCompat swDmarcViewer;
    private EditText etKeywords;
    private SwitchCompat swTestIab;
    private Button btnImportProviders;
    private Button btnExportClassifier;
    private TextView tvProcessors;
    private TextView tvMemoryClass;
    private TextView tvMemoryUsage;
    private TextView tvStorageUsage;
    private TextView tvCacheUsage;
    private TextView tvContactInfo;
    private TextView tvSuffixes;
    private TextView tvAndroidId;
    private TextView tvFingerprint;
    private TextView tvCursorWindow;
    private Button btnGC;
    private Button btnCharsets;
    private Button btnFontMap;
    private Button btnFiles;
    private Button btnUris;
    private Button btnAllPermissions;
    private TextView tvPermissions;

    private Group grpUpdates;
    private Group grpBitbucket;
    private Group grpAnnouncements;
    private Group grpTest;

    private CardView cardDebug;

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private static final int REQUEST_CLASSIFIER = 1;
    private static final long MIN_FILE_SIZE = 1024 * 1024L;

    final static List<String> RESET_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            "sort_answers", "shortcuts", "ical_tentative", "fts",
            "classification", "class_min_probability", "class_min_difference",
            "show_filtered", "haptic_feedback", "haptic_feedback_swipe",
            "language",
            "updates", "weekly", "beta", "show_changelog", "announcements",
            "crash_reports", "cleanup_attachments",
            "google_backup",
            "watchdog", "experiments", "main_log", "main_log_memory", "protocol", "log_level", "debug", "leak_canary",
            "test1", "test2", "test3", "test4", "test5",
            "emergency_file", "work_manager", "task_description", // "external_storage",
            "sqlite_integrity_check", "wal", "sqlite_checkpoints", "sqlite_analyze", "sqlite_auto_vacuum", "sqlite_sync_extra", "sqlite_cache",
            "cache_lists", "oauth_tabs",
            "start_delay", "range_size", "chunk_size", "thread_range", "restart_interval",
            "autoscroll_editor", "undo_manager",
            "browser_zoom",
            "ignore_formatted_size",
            "show_recent",
            "use_modseq", "preamble", "uid_command", "perform_expunge", "uid_expunge",
            "auth_plain", "auth_login", "auth_ntlm", "auth_sasl", "auth_apop", "use_top", "forget_top",
            "keep_alive_poll", "empty_pool", "idle_done", "fast_fetch",
            "max_backoff_power", "logarithmic_backoff",
            "exact_alarms",
            "native_dkim", "native_arc", "native_arc_whitelist", "strict_alignment",
            "svg", "webp", "animate_images",
            "preview_hidden", "preview_quotes",
            "easy_correct", "paste_plain", "paste_quote", "favicon_uri", "email_junk", "infra", "tld_flags", "json_ld", "dup_msgids", "thread_byref", "save_user_flags", "mdn",
            "app_chooser", "app_chooser_share", "share_task",
            "adjacent_links", "adjacent_documents", "adjacent_portrait", "adjacent_landscape",
            "delete_confirmation", "delete_notification", "global_keywords", "test_iab"
    ));

    private final static String[] RESET_QUESTIONS = new String[]{
            "first", "app_support", "notify_archive",
            "message_swipe", "message_outbox", "message_select", "message_junk",
            "folder_actions", "folder_sync",
            "crash_reports_asked", "review_asked", "review_later", "why",
            "reply_hint", "html_always_images", "open_full_confirmed", "open_amp_confirmed",
            "ask_images", "ask_html",
            "print_html_confirmed", "print_html_header", "print_html_images", "print_html_block_quotes",
            "reformatted_hint",
            "selected_folders", "move_1_confirmed", "move_n_confirmed",
            "last_search_senders", "last_search_recipients", "last_search_subject", "last_search_keywords", "last_search_message",
            "identities_asked", "identities_primary_hint",
            "attachments_asked",
            "raw_asked", "all_read_asked", "delete_asked",
            "cc_bcc", "inline_image_hint", "compose_reference", "send_dialog",
            "setup_reminder", "was_ignoring", "setup_advanced",
            "notifications_reminder", "datasaver_reminder", "vpn_reminder",
            "signature_images_hint",
            "gmail_checked",
            "eml_auto_confirm",
            "open_with_pkg", "open_with_tabs",
            "gmail_checked", "outlook_checked",
            "redmi_note",
            "accept_space", "accept_unsupported",
            "junk_hint",
            "last_update_check", "last_announcement_check",
            "motd",
            "outlook_last_checked", "outlook_checked",
            "send_archive"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Locale slocale = Resources.getSystem().getConfiguration().locale;
        for (String tag : getResources().getAssets().getLocales())
            languages.add(new Pair<>(tag, Locale.forLanguageTag(tag).getDisplayName(slocale)));

        Collections.sort(languages, new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> l1, Pair<String, String> l2) {
                return l1.second.compareTo(l2.second);
            }
        });
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_options_misc, container, false);

        // Get controls

        ibHelp = view.findViewById(R.id.ibHelp);
        swPowerMenu = view.findViewById(R.id.swPowerMenu);
        swSendSelf = view.findViewById(R.id.swSendSelf);
        swExternalSearch = view.findViewById(R.id.swExternalSearch);
        swSortAnswers = view.findViewById(R.id.swSortAnswers);
        swExternalAnswer = view.findViewById(R.id.swExternalAnswer);
        swShortcuts = view.findViewById(R.id.swShortcuts);
        swICalTentative = view.findViewById(R.id.swICalTentative);
        ibICalTentative = view.findViewById(R.id.ibICalTentative);
        swFts = view.findViewById(R.id.swFts);
        ibFts = view.findViewById(R.id.ibFts);
        tvFtsIndexed = view.findViewById(R.id.tvFtsIndexed);
        tvFtsPro = view.findViewById(R.id.tvFtsPro);
        swClassification = view.findViewById(R.id.swClassification);
        ibClassification = view.findViewById(R.id.ibClassification);
        tvClassMinProbability = view.findViewById(R.id.tvClassMinProbability);
        sbClassMinProbability = view.findViewById(R.id.sbClassMinProbability);
        tvClassMinDifference = view.findViewById(R.id.tvClassMinDifference);
        sbClassMinDifference = view.findViewById(R.id.sbClassMinDifference);
        swShowFiltered = view.findViewById(R.id.swShowFiltered);
        swHapticFeedback = view.findViewById(R.id.swHapticFeedback);
        swHapticFeedbackSwipe = view.findViewById(R.id.swHapticFeedbackSwipe);
        spLanguage = view.findViewById(R.id.spLanguage);
        swUpdates = view.findViewById(R.id.swUpdates);
        tvGithubPrivacy = view.findViewById(R.id.tvGithubPrivacy);
        ibChannelUpdated = view.findViewById(R.id.ibChannelUpdated);
        swCheckWeekly = view.findViewById(R.id.swWeekly);
        swBeta = view.findViewById(R.id.swBeta);
        tvBitBucketPrivacy = view.findViewById(R.id.tvBitBucketPrivacy);
        swChangelog = view.findViewById(R.id.swChangelog);
        swAnnouncements = view.findViewById(R.id.swAnnouncements);
        tvAnnouncementsPrivacy = view.findViewById(R.id.tvAnnouncementsPrivacy);
        swCrashReports = view.findViewById(R.id.swCrashReports);
        tvUuid = view.findViewById(R.id.tvUuid);
        ibCrashReports = view.findViewById(R.id.ibCrashReports);
        btnReset = view.findViewById(R.id.btnReset);
        swCleanupAttachments = view.findViewById(R.id.swCleanupAttachments);
        btnCleanup = view.findViewById(R.id.btnCleanup);
        tvLastCleanup = view.findViewById(R.id.tvLastCleanup);
        tvSdcard = view.findViewById(R.id.tvSdcard);
        swGoogleBackup = view.findViewById(R.id.swGoogleBackup);
        tvGoogleBackupPrivacy = view.findViewById(R.id.tvGoogleBackupPrivacy);

        cardAdvanced = view.findViewById(R.id.cardAdvanced);
        swWatchdog = view.findViewById(R.id.swWatchdog);
        swExperiments = view.findViewById(R.id.swExperiments);
        tvExperimentsHint = view.findViewById(R.id.tvExperimentsHint);
        swMainLog = view.findViewById(R.id.swMainLog);
        swMainLogMem = view.findViewById(R.id.swMainLogMem);
        swProtocol = view.findViewById(R.id.swProtocol);
        swLogInfo = view.findViewById(R.id.swLogInfo);
        swDebug = view.findViewById(R.id.swDebug);
        swCanary = view.findViewById(R.id.swCanary);
        swTest1 = view.findViewById(R.id.swTest1);
        swTest2 = view.findViewById(R.id.swTest2);
        swTest3 = view.findViewById(R.id.swTest3);
        swTest4 = view.findViewById(R.id.swTest4);
        swTest5 = view.findViewById(R.id.swTest5);

        btnRepair = view.findViewById(R.id.btnRepair);
        btnDaily = view.findViewById(R.id.btnDaily);
        tvLastDaily = view.findViewById(R.id.tvLastDaily);
        swAutostart = view.findViewById(R.id.swAutostart);
        swEmergency = view.findViewById(R.id.swEmergency);
        swWorkManager = view.findViewById(R.id.swWorkManager);
        swTaskDescription = view.findViewById(R.id.swTaskDescription);
        swExternalStorage = view.findViewById(R.id.swExternalStorage);
        tvExternalStorageFolder = view.findViewById(R.id.tvExternalStorageFolder);
        swIntegrity = view.findViewById(R.id.swIntegrity);
        swWal = view.findViewById(R.id.swWal);
        swCheckpoints = view.findViewById(R.id.swCheckpoints);
        swAnalyze = view.findViewById(R.id.swAnalyze);
        swAutoVacuum = view.findViewById(R.id.swAutoVacuum);
        swSyncExtra = view.findViewById(R.id.swSyncExtra);
        tvSqliteCache = view.findViewById(R.id.tvSqliteCache);
        sbSqliteCache = view.findViewById(R.id.sbSqliteCache);
        ibSqliteCache = view.findViewById(R.id.ibSqliteCache);
        swCacheLists = view.findViewById(R.id.swCacheLists);
        swOauthTabs = view.findViewById(R.id.swOauthTabs);
        tvStartDelay = view.findViewById(R.id.tvStartDelay);
        sbStartDelay = view.findViewById(R.id.sbStartDelay);
        tvRangeSize = view.findViewById(R.id.tvRangeSize);
        sbRangeSize = view.findViewById(R.id.sbRangeSize);
        tvChunkSize = view.findViewById(R.id.tvChunkSize);
        sbChunkSize = view.findViewById(R.id.sbChunkSize);
        tvThreadRange = view.findViewById(R.id.tvThreadRange);
        sbThreadRange = view.findViewById(R.id.sbThreadRange);
        tvRestartInterval = view.findViewById(R.id.tvRestartInterval);
        sbRestartInterval = view.findViewById(R.id.sbRestartInterval);
        swAutoScroll = view.findViewById(R.id.swAutoScroll);
        swUndoManager = view.findViewById(R.id.swUndoManager);
        swBrowserZoom = view.findViewById(R.id.swBrowserZoom);
        etViewportHeight = view.findViewById(R.id.etViewportHeight);
        swIgnoreFormattedSize = view.findViewById(R.id.swIgnoreFormattedSize);
        swShowRecent = view.findViewById(R.id.swShowRecent);
        swModSeq = view.findViewById(R.id.swModSeq);
        swPreamble = view.findViewById(R.id.swPreamble);
        swUid = view.findViewById(R.id.swUid);
        swExpunge = view.findViewById(R.id.swExpunge);
        swUidExpunge = view.findViewById(R.id.swUidExpunge);
        swAuthPlain = view.findViewById(R.id.swAuthPlain);
        swAuthLogin = view.findViewById(R.id.swAuthLogin);
        swAuthNtlm = view.findViewById(R.id.swAuthNtlm);
        swAuthSasl = view.findViewById(R.id.swAuthSasl);
        swAuthApop = view.findViewById(R.id.swAuthApop);
        swUseTop = view.findViewById(R.id.swUseTop);
        swForgetTop = view.findViewById(R.id.swForgetTop);
        swKeepAlivePoll = view.findViewById(R.id.swKeepAlivePoll);
        swEmptyPool = view.findViewById(R.id.swEmptyPool);
        swIdleDone = view.findViewById(R.id.swIdleDone);
        swFastFetch = view.findViewById(R.id.swFastFetch);
        tvMaxBackoff = view.findViewById(R.id.tvMaxBackoff);
        sbMaxBackOff = view.findViewById(R.id.sbMaxBackOff);
        swLogarithmicBackoff = view.findViewById(R.id.swLogarithmicBackoff);
        swExactAlarms = view.findViewById(R.id.swExactAlarms);
        swNativeDkim = view.findViewById(R.id.swNativeDkim);
        swNativeArc = view.findViewById(R.id.swNativeArc);
        etNativeArcWhitelist = view.findViewById(R.id.etNativeArcWhitelist);
        swStrictAlignment = view.findViewById(R.id.swStrictAlignment);
        swSvg = view.findViewById(R.id.swSvg);
        swWebp = view.findViewById(R.id.swWebp);
        swAnimate = view.findViewById(R.id.swAnimate);
        swPreviewHidden = view.findViewById(R.id.swPreviewHidden);
        swPreviewQuotes = view.findViewById(R.id.swPreviewQuotes);
        swEasyCorrect = view.findViewById(R.id.swEasyCorrect);
        swPastePlain = view.findViewById(R.id.swPastePlain);
        swPasteQuote = view.findViewById(R.id.swPasteQuote);
        etFaviconUri = view.findViewById(R.id.etFaviconUri);
        swEmailJunk = view.findViewById(R.id.swEmailJunk);
        swInfra = view.findViewById(R.id.swInfra);
        swTldFlags = view.findViewById(R.id.swTldFlags);
        swJsonLd = view.findViewById(R.id.swJsonLd);
        swDupMsgId = view.findViewById(R.id.swDupMsgId);
        swThreadByRef = view.findViewById(R.id.swThreadByRef);
        swSaveUserFlags = view.findViewById(R.id.swSaveUserFlags);
        swMdn = view.findViewById(R.id.swMdn);
        swAppChooser = view.findViewById(R.id.swAppChooser);
        swAppChooserShare = view.findViewById(R.id.swAppChooserShare);
        swShareTask = view.findViewById(R.id.swShareTask);
        swAdjacentLinks = view.findViewById(R.id.swAdjacentLinks);
        swAdjacentDocuments = view.findViewById(R.id.swAdjacentDocuments);
        swAdjacentPortrait = view.findViewById(R.id.swAdjacentPortrait);
        swAdjacentLandscape = view.findViewById(R.id.swAdjacentLandscape);
        swDeleteConfirmation = view.findViewById(R.id.swDeleteConfirmation);
        swDeleteNotification = view.findViewById(R.id.swDeleteNotification);
        swDmarcViewer = view.findViewById(R.id.swDmarcViewer);
        etKeywords = view.findViewById(R.id.etKeywords);
        swTestIab = view.findViewById(R.id.swTestIab);
        btnImportProviders = view.findViewById(R.id.btnImportProviders);
        btnExportClassifier = view.findViewById(R.id.btnExportClassifier);
        tvProcessors = view.findViewById(R.id.tvProcessors);
        tvMemoryClass = view.findViewById(R.id.tvMemoryClass);
        tvMemoryUsage = view.findViewById(R.id.tvMemoryUsage);
        tvStorageUsage = view.findViewById(R.id.tvStorageUsage);
        tvCacheUsage = view.findViewById(R.id.tvCacheUsage);
        tvContactInfo = view.findViewById(R.id.tvContactInfo);
        tvSuffixes = view.findViewById(R.id.tvSuffixes);
        tvAndroidId = view.findViewById(R.id.tvAndroidId);
        tvFingerprint = view.findViewById(R.id.tvFingerprint);
        tvCursorWindow = view.findViewById(R.id.tvCursorWindow);
        btnGC = view.findViewById(R.id.btnGC);
        btnCharsets = view.findViewById(R.id.btnCharsets);
        btnFontMap = view.findViewById(R.id.btnFontMap);
        btnFiles = view.findViewById(R.id.btnFiles);
        btnUris = view.findViewById(R.id.btnUris);
        btnAllPermissions = view.findViewById(R.id.btnAllPermissions);
        tvPermissions = view.findViewById(R.id.tvPermissions);

        grpUpdates = view.findViewById(R.id.grpUpdates);
        grpBitbucket = view.findViewById(R.id.grpBitbucket);
        grpAnnouncements = view.findViewById(R.id.grpAnnouncements);
        grpTest = view.findViewById(R.id.grpTest);

        cardDebug = view.findViewById(R.id.cardDebug);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        ibHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Options:misc"), false);
            }
        });

        swPowerMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    Helper.enableComponent(getContext(), ServicePowerControl.class, checked);
            }
        });

        swSendSelf.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Helper.enableComponent(getContext(), ActivitySendSelf.class, checked);
            }
        });

        swExternalSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Helper.enableComponent(getContext(), ActivitySearch.class, checked);
            }
        });

        swSortAnswers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sort_answers", checked).apply();
            }
        });

        swExternalAnswer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Helper.enableComponent(getContext(), ActivityAnswer.class, checked);
            }
        });

        swShortcuts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("shortcuts", checked).commit(); // apply won't work here
            }
        });

        swICalTentative.setVisibility(BuildConfig.PLAY_STORE_RELEASE ? View.GONE : View.VISIBLE);
        swICalTentative.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("ical_tentative", checked).apply();
            }
        });

        ibICalTentative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 186);
            }
        });

        swFts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("fts", checked).apply();

                WorkerFts.init(getContext(), true);

                if (!checked) {
                    Bundle args = new Bundle();

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            try {
                                SQLiteDatabase sdb = Fts4DbHelper.getInstance(context);
                                Fts4DbHelper.delete(sdb);
                                Fts4DbHelper.optimize(sdb);
                            } catch (SQLiteDatabaseCorruptException ex) {
                                Log.e(ex);
                                Fts4DbHelper.delete(context);
                            }

                            DB db = DB.getInstance(context);
                            db.message().resetFts();

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentOptionsMisc.this, args, "fts:reset");
                }
            }
        });

        ibFts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 13);
            }
        });

        Helper.linkPro(tvFtsPro);

        swClassification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private int count = 0;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("classification", checked).apply();
                if (!checked) {
                    count++;
                    if (count >= 3) {
                        count = 0;
                        MessageClassifier.clear(buttonView.getContext());
                        ToastEx.makeText(buttonView.getContext(), R.string.title_reset, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        ibClassification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 163);
            }
        });

        sbClassMinProbability.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("class_min_probability", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbClassMinDifference.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("class_min_difference", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        swShowFiltered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("show_filtered", isChecked).apply();
            }
        });

        swHapticFeedback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("haptic_feedback", isChecked).apply();
            }
        });

        swHapticFeedbackSwipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("haptic_feedback_swipe", isChecked).apply();
            }
        });

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String current = prefs.getString("language", null);
                String selected = (position == 0 ? null : languages.get(position - 1).first);
                if (Objects.equals(current, selected))
                    return;

                String title = (position == 0
                        ? getString(R.string.title_advanced_language_system)
                        : languages.get(position - 1).second);
                new AlertDialog.Builder(adapterView.getContext())
                        .setIcon(R.drawable.twotone_help_24)
                        .setTitle(title)
                        .setMessage(R.string.title_advanced_english_hint)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // apply won't work here
                                if (selected == null)
                                    prefs.edit().remove("language").commit();
                                else
                                    prefs.edit().putString("language", selected).commit();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                setOptions();
                            }
                        })
                        .show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("language").commit();
            }
        });

        swUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("updates", checked).apply();
                swCheckWeekly.setEnabled(checked);
                swBeta.setEnabled(checked);
                if (!checked) {
                    NotificationManager nm =
                            Helper.getSystemService(getContext(), NotificationManager.class);
                    nm.cancel(NotificationHelper.NOTIFICATION_UPDATE);
                }
            }
        });

        tvGithubPrivacy.getPaint().setUnderlineText(true);
        tvGithubPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.GITHUB_PRIVACY_URI), true);
            }
        });

        final Intent channelUpdate = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, "update");

        ibChannelUpdated.setVisibility(View.GONE);
        ibChannelUpdated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(channelUpdate);
            }
        });

        swCheckWeekly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("weekly", checked).apply();
            }
        });

        swBeta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("beta", checked).apply();
            }
        });

        tvBitBucketPrivacy.getPaint().setUnderlineText(true);
        tvBitBucketPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.BITBUCKET_PRIVACY_URI), true);
            }
        });

        swChangelog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("show_changelog", checked).apply();
            }
        });

        swAnnouncements.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("announcements", checked).apply();
            }
        });

        tvAnnouncementsPrivacy.getPaint().setUnderlineText(true);
        tvAnnouncementsPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.GITHUB_PRIVACY_URI), true);
            }
        });

        swCrashReports.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit()
                        .remove("crash_report_count")
                        .putBoolean("crash_reports", checked)
                        .apply();
                Log.setCrashReporting(checked);
            }
        });

        ibCrashReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 104);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetQuestions();
            }
        });

        swCleanupAttachments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("cleanup_attachments", checked).apply();
            }
        });

        btnCleanup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCleanup();
            }
        });

        tvSdcard.setPaintFlags(tvSdcard.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvSdcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 93);
            }
        });

        swGoogleBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("google_backup", checked).apply();
                FairEmailBackupAgent.dataChanged(compoundButton.getContext());
            }
        });

        tvGoogleBackupPrivacy.getPaint().setUnderlineText(true);
        tvGoogleBackupPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.GOOGLE_PRIVACY_URI), true);
            }
        });

        swWatchdog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("watchdog", checked).apply();
            }
        });

        tvExperimentsHint.setPaintFlags(tvExperimentsHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvExperimentsHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 125);
            }
        });

        swExperiments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("experiments", checked).apply();
            }
        });

        swMainLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("main_log", checked).apply();
                swMainLogMem.setEnabled(checked);
            }
        });

        swMainLogMem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("main_log_memory", checked).apply();
            }
        });

        swProtocol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("protocol", checked).apply();
                if (checked)
                    prefs.edit()
                            .putLong("protocol_since", new Date().getTime())
                            .apply();
                else
                    EntityLog.clear(compoundButton.getContext());
            }
        });

        swLogInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit().putInt("log_level", checked ? android.util.Log.INFO : android.util.Log.WARN).apply();
            }
        });

        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                cardDebug.setVisibility(checked || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
                if (checked)
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                view.scrollTo(0, cardAdvanced.getTop() + swDebug.getTop());
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }
                    });
            }
        });

        swCanary.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        swCanary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("leak_canary", checked).apply();
                CoalMine.setup(checked);
            }
        });

        swTest1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("test1", checked).apply();
            }
        });

        swTest2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("test2", checked).apply();
            }
        });

        swTest3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("test3", checked).apply();
            }
        });

        swTest4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("test4", checked).apply();
            }
        });

        swTest5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("test5", checked).apply();
            }
        });

        btnRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG)
                    new AlertDialog.Builder(view.getContext())
                            .setIcon(R.drawable.twotone_bug_report_24)
                            .setTitle(R.string.title_advanced_repair)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new SimpleTask<Void>() {
                                        @Override
                                        protected void onPostExecute(Bundle args) {
                                            prefs.edit().remove("debug").apply();
                                        }

                                        @Override
                                        protected Void onExecute(Context context, Bundle args) throws Throwable {
                                            DB db = DB.getInstance(context);

                                            List<EntityAccount> accounts = db.account().getAccounts();
                                            if (accounts == null)
                                                return null;

                                            for (EntityAccount account : accounts) {
                                                if (account.protocol != EntityAccount.TYPE_IMAP)
                                                    continue;

                                                List<EntityFolder> folders = db.folder().getFolders(account.id, false, false);
                                                if (folders == null)
                                                    continue;

                                                EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                                                for (EntityFolder folder : folders) {
                                                    if (inbox == null && "inbox".equalsIgnoreCase(folder.name))
                                                        folder.type = EntityFolder.INBOX;

                                                    if (!EntityFolder.USER.equals(folder.type) &&
                                                            !EntityFolder.SYSTEM.equals(folder.type)) {
                                                        EntityLog.log(context, "Repairing " + account.name + ":" + folder.type);
                                                        folder.setProperties();
                                                        folder.setSpecials(account);
                                                        db.folder().updateFolder(folder);
                                                    }
                                                }
                                            }

                                            return null;
                                        }

                                        @Override
                                        protected void onExecuted(Bundle args, Void data) {
                                            ToastEx.makeText(v.getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
                                            ServiceSynchronize.reload(v.getContext(), null, true, "repair");
                                        }

                                        @Override
                                        protected void onException(Bundle args, Throwable ex) {
                                            Log.unexpectedError(getParentFragmentManager(), ex);
                                        }
                                    }.execute(FragmentOptionsMisc.this, new Bundle(), "repair");
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
        });

        btnDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        WorkerDailyRules.daily(context);
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsMisc.this, new Bundle(), "daily");
            }
        });

        swAutostart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Helper.enableComponent(v.getContext(), ReceiverAutoStart.class, checked);
            }
        });

        swEmergency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit().putBoolean("emergency_file", checked).apply();
            }
        });

        swWorkManager.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("work_manager", isChecked).apply();
            }
        });

        swTaskDescription.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                prefs.edit().putBoolean("task_description", isChecked).apply();
            }
        });

        swExternalStorage.setEnabled(Helper.getExternalFilesDir(getContext()) != null);
        swExternalStorage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("external_storage", isChecked);
                editor.apply();

                Bundle args = new Bundle();
                args.putBoolean("external_storage", isChecked);

                new SimpleTask<Integer>() {
                    @Override
                    protected Integer onExecute(Context context, Bundle args) throws IOException {
                        boolean external_storage = args.getBoolean("external_storage");

                        File sourceRoot = (!external_storage
                                ? Helper.getExternalFilesDir(context)
                                : context.getFilesDir());

                        File targetRoot = (external_storage
                                ? Helper.getExternalFilesDir(context)
                                : context.getFilesDir());

                        File source = new File(sourceRoot, "attachments");
                        File target = new File(targetRoot, "attachments");
                        source.mkdirs();
                        target.mkdirs();

                        File[] attachments = source.listFiles();
                        if (attachments != null)
                            for (File attachment : attachments) {
                                File dest = new File(target, attachment.getName());
                                Log.w("Move " + attachment + " to " + dest);
                                Helper.copy(attachment, dest);
                                Helper.secureDelete(attachment);
                            }

                        return (attachments == null ? -1 : attachments.length);
                    }

                    @Override
                    protected void onExecuted(Bundle args, Integer count) {
                        String msg = String.format("Moved %d attachments", count);
                        ToastEx.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsMisc.this, args, "external");
            }
        });

        swIntegrity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit()
                        .putBoolean("sqlite_integrity_check", checked)
                        .remove("debug")
                        .commit();
                ApplicationEx.restart(v.getContext(), "sqlite_integrity_check");
            }
        });

        swWal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("wal", checked).commit(); // apply won't work here
            }
        });

        swCheckpoints.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sqlite_checkpoints", checked).apply();
            }
        });

        swAnalyze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sqlite_analyze", checked).apply();
            }
        });

        swAutoVacuum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit()
                        .putBoolean("sqlite_auto_vacuum", checked)
                        .remove("debug")
                        .commit();
                ApplicationEx.restart(v.getContext(), "sqlite_auto_vacuum");
            }
        });

        swSyncExtra.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit()
                        .putBoolean("sqlite_sync_extra", checked)
                        .remove("debug")
                        .commit();
                ApplicationEx.restart(v.getContext(), "sqlite_sync_extra");
            }
        });

        sbSqliteCache.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("sqlite_cache", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        ibSqliteCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().remove("debug").commit();
                ApplicationEx.restart(v.getContext(), "sqlite_cache");
            }
        });

        swCacheLists.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit().putBoolean("cache_lists", checked).apply();
            }
        });

        swOauthTabs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                prefs.edit().putBoolean("oauth_tabs", checked).apply();
            }
        });

        sbStartDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("start_delay", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbRangeSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / 100;
                if (progress < 1)
                    progress = 1;
                progress = progress * 100;
                prefs.edit().putInt("range_size", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbChunkSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / 10;
                if (progress < 1)
                    progress = 1;
                progress = progress * 10;
                prefs.edit().putInt("chunk_size", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbThreadRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("thread_range", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbRestartInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("restart_interval", progress * 10).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        swAutoScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoscroll_editor", checked).apply();
            }
        });

        swUndoManager.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("undo_manager", checked).apply();
            }
        });

        swBrowserZoom.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        swBrowserZoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("browser_zoom", checked).apply();
            }
        });

        etViewportHeight.setHint(Integer.toString(WebViewEx.getDefaultViewportHeight(getContext())));
        etViewportHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable edit) {
                try {
                    String vh = edit.toString();
                    if (TextUtils.isEmpty(vh))
                        prefs.edit().remove("viewport_height").apply();
                    else
                        prefs.edit().putInt("viewport_height", Integer.parseInt(vh)).apply();
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        swIgnoreFormattedSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("ignore_formatted_size", checked).apply();
            }
        });

        swShowRecent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("show_recent", checked).apply();
            }
        });

        swModSeq.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("use_modseq", checked).apply();
                ServiceSynchronize.reload(compoundButton.getContext(), null, true, "use_modseq");
            }
        });

        swPreamble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preamble", checked).apply();
                System.setProperty("fairemail.preamble", Boolean.toString(checked));
            }
        });

        swUid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("uid_command", checked).apply();
                System.setProperty("fairemail.uid_command", Boolean.toString(checked));
                ServiceSynchronize.reload(compoundButton.getContext(), null, true, "uid_command");
            }
        });

        swExpunge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("perform_expunge", checked).apply();
                ServiceSynchronize.reload(compoundButton.getContext(), null, true, "perform_expunge");
            }
        });

        swUidExpunge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("uid_expunge", checked).apply();
                ServiceSynchronize.reload(compoundButton.getContext(), null, true, "uid_expunge");
            }
        });

        swAuthPlain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_plain", checked).apply();
            }
        });

        swAuthLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_login", checked).apply();
            }
        });

        swAuthNtlm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_ntlm", checked).apply();
            }
        });

        swAuthSasl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_sasl", checked).apply();
            }
        });

        swAuthApop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_apop", checked).apply();
            }
        });

        swUseTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("use_top", checked).apply();
            }
        });

        swForgetTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("forget_top", checked).apply();
            }
        });

        swKeepAlivePoll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("keep_alive_poll", checked).apply();
            }
        });

        swEmptyPool.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("empty_pool", checked).apply();
            }
        });

        swIdleDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("idle_done", checked).apply();
            }
        });

        swFastFetch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("fast_fetch", checked).apply();
            }
        });

        sbMaxBackOff.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("max_backoff_power", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        swLogarithmicBackoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("logarithmic_backoff", checked).apply();
            }
        });

        swExactAlarms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("exact_alarms", checked).apply();
            }
        });

        swNativeDkim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("native_dkim", checked).apply();
                swNativeArc.setEnabled(checked && swNativeDkim.isEnabled());
                etNativeArcWhitelist.setEnabled(checked && swNativeDkim.isEnabled());
                swStrictAlignment.setEnabled(checked && swNativeDkim.isEnabled());
            }
        });

        swNativeArc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("native_arc", checked).apply();
            }
        });

        etNativeArcWhitelist.setHint(TextUtils.join(",", MessageHelper.ARC_WHITELIST_DEFAULT));
        etNativeArcWhitelist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                prefs.edit().putString("native_arc_whitelist", s.toString().trim()).apply();
            }
        });

        swStrictAlignment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("strict_alignment", checked).apply();
            }
        });

        swSvg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("svg", checked).apply();
            }
        });

        swWebp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("webp", checked).apply();
            }
        });

        swAnimate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("animate_images", checked).apply();
            }
        });

        swPreviewHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview_hidden", checked).apply();
            }
        });

        swPreviewQuotes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview_quotes", checked).apply();
            }
        });

        swEasyCorrect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("easy_correct", checked).apply();
            }
        });

        swPastePlain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("paste_plain", checked).apply();
            }
        });

        swPasteQuote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("paste_quote", checked).apply();
            }
        });

        etFaviconUri.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable edit) {
                String uri = edit.toString().trim();
                String prev = prefs.getString("favicon_uri", null);
                prefs.edit().putString("favicon_uri", uri).apply();
                if (uri.equals(prev))
                    ContactInfo.clearCache(getContext());
            }
        });

        swEmailJunk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("email_junk", checked).apply();
            }
        });

        swInfra.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("infra", checked).apply();
            }
        });

        swTldFlags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("tld_flags", checked).apply();
            }
        });

        swJsonLd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("json_ld", checked).apply();
            }
        });

        swDupMsgId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("dup_msgids", checked).apply();
            }
        });

        swThreadByRef.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("thread_byref", checked).apply();
            }
        });

        swSaveUserFlags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("save_user_flags", checked).apply();
            }
        });

        swMdn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("mdn", checked).apply();
            }
        });

        swAppChooser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("app_chooser", checked).apply();
            }
        });

        swAppChooserShare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("app_chooser_share", checked).apply();
            }
        });

        swShareTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("share_task", checked).apply();
            }
        });

        swAdjacentLinks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("adjacent_links", checked).apply();
            }
        });

        swAdjacentDocuments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("adjacent_documents", checked).apply();
            }
        });

        swAdjacentPortrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("adjacent_portrait", checked).apply();
            }
        });

        swAdjacentLandscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("adjacent_landscape", checked).apply();
            }
        });

        swDeleteConfirmation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("delete_confirmation", checked).apply();
            }
        });

        swDeleteNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("delete_notification", checked).apply();
            }
        });

        swDmarcViewer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Helper.enableComponent(compoundButton.getContext(), ActivityDMARC.class, checked);
            }
        });

        etKeywords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String keywords = s.toString().trim();
                String[] keyword = keywords.replaceAll("\\s+", " ").split(" ");
                for (int i = 0; i < keyword.length; i++)
                    keyword[i] = MessageHelper.sanitizeKeyword(keyword[i]);
                keywords = String.join(" ", keyword);

                if (TextUtils.isEmpty(keywords))
                    prefs.edit().remove("global_keywords").apply();
                else
                    prefs.edit().putString("global_keywords", keywords).apply();
            }
        });

        swTestIab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("test_iab", checked).apply();
            }
        });

        btnImportProviders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("*/*");
                Intent choose = Helper.getChooser(v.getContext(), intent);
                getActivity().startActivityForResult(choose, ActivitySetup.REQUEST_IMPORT_PROVIDERS);
            }
        });

        btnExportClassifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExportClassifier(v.getContext());
            }
        });

        btnGC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.gc("Miscellaneous");
                DB.shrinkMemory(v.getContext());
            }
        });

        btnCharsets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<SortedMap<String, Charset>>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnCharsets.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnCharsets.setEnabled(true);
                    }

                    @Override
                    protected SortedMap<String, Charset> onExecute(Context context, Bundle args) {
                        return Charset.availableCharsets();
                    }

                    @Override
                    protected void onExecuted(Bundle args, SortedMap<String, Charset> charsets) {
                        StringBuilder sb = new StringBuilder();
                        for (String key : charsets.keySet())
                            sb.append(charsets.get(key).displayName()).append("\r\n");
                        new AlertDialog.Builder(getContext())
                                .setIcon(R.drawable.twotone_info_24)
                                .setTitle(R.string.title_advanced_charsets)
                                .setMessage(sb.toString())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsMisc.this, new Bundle(), "setup:charsets");
            }
        });

        btnFontMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableStringBuilder ssb = new SpannableStringBuilderEx();

                try {
                    Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
                    Field f = Typeface.class.getDeclaredField("sSystemFontMap");
                    f.setAccessible(true);
                    Map<String, Typeface> sSystemFontMap = (Map<String, Typeface>) f.get(typeface);
                    for (String key : sSystemFontMap.keySet())
                        ssb.append(key).append("\n");
                } catch (Throwable ex) {
                    Log.e(ex);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ssb.append("\n");
                    for (Font font : SystemFonts.getAvailableFonts())
                        ssb.append(font.getFile().getName()).append("\n");
                }

                new AlertDialog.Builder(getContext())
                        .setIcon(R.drawable.twotone_info_24)
                        .setTitle(R.string.title_advanced_font_map)
                        .setMessage(ssb)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        final String title = getString(R.string.title_advanced_files, Helper.humanReadableByteCount(MIN_FILE_SIZE));
        btnFiles.setText(title);

        btnFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<List<File>>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnFiles.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnFiles.setEnabled(true);
                    }

                    @Override
                    protected List<File> onExecute(Context context, Bundle args) {
                        List<File> files = new ArrayList<>();
                        files.addAll(Helper.listFiles(context.getFilesDir(), MIN_FILE_SIZE));
                        files.addAll(Helper.listFiles(context.getCacheDir(), MIN_FILE_SIZE));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            files.addAll(Helper.listFiles(context.getDataDir(), MIN_FILE_SIZE));

                        Collections.sort(files, new Comparator<File>() {
                            @Override
                            public int compare(File f1, File f2) {
                                return -Long.compare(f1.length(), f2.length());
                            }
                        });

                        return files;
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<File> files) {
                        SpannableStringBuilder ssb = new SpannableStringBuilderEx();

                        final Context context = getContext();
                        File dataDir = (Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                                ? null : context.getDataDir());
                        File filesDir = context.getFilesDir();
                        File cacheDir = context.getCacheDir();

                        if (dataDir != null)
                            ssb.append("Data: ").append(dataDir.getAbsolutePath()).append("\r\n");
                        if (filesDir != null)
                            ssb.append("Files: ").append(filesDir.getAbsolutePath()).append("\r\n");
                        if (cacheDir != null)
                            ssb.append("Cache: ").append(cacheDir.getAbsolutePath()).append("\r\n");
                        ssb.append("\r\n");

                        for (File file : files) {
                            int start = ssb.length();
                            ssb.append(Helper.humanReadableByteCount(file.length()));
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                            ssb.append(' ')
                                    .append(file.getAbsolutePath())
                                    .append("\r\n");
                        }

                        ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, ssb.length(), 0);

                        new AlertDialog.Builder(context)
                                .setIcon(R.drawable.twotone_info_24)
                                .setTitle(title)
                                .setMessage(ssb)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsMisc.this, new Bundle(), "setup:files");
            }
        });

        btnUris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                List<UriPermission> permissions = v.getContext().getContentResolver().getPersistedUriPermissions();
                for (UriPermission permission : permissions) {
                    ssb.append(permission.getUri().toString());
                    ssb.append('\u00a0');
                    if (permission.isReadPermission())
                        ssb.append("r");
                    if (permission.isWritePermission())
                        ssb.append("w");
                    ssb.append('\n');
                }

                new AlertDialog.Builder(v.getContext())
                        .setIcon(R.drawable.twotone_info_24)
                        .setTitle(R.string.title_advanced_all_permissions)
                        .setMessage(ssb)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        btnAllPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<Spanned>() {
                    @Override
                    protected Spanned onExecute(Context context, Bundle args) throws Throwable {
                        SpannableStringBuilder ssb = new SpannableStringBuilderEx();

                        PackageManager pm = context.getPackageManager();
                        List<PermissionGroupInfo> groups = pm.getAllPermissionGroups(0);
                        groups.add(0, null); // Ungrouped

                        for (PermissionGroupInfo group : groups) {
                            String name = (group == null ? null : group.name);
                            int start = ssb.length();
                            ssb.append(name == null ? "Ungrouped" : name);
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                            ssb.append("\n\n");

                            try {
                                for (PermissionInfo permission : pm.queryPermissionsByGroup(name, 0)) {
                                    start = ssb.length();
                                    ssb.append(permission.name);
                                    ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), start, ssb.length(), 0);
                                    ssb.append("\n");
                                }
                            } catch (PackageManager.NameNotFoundException ex) {
                                Log.e(ex);
                            }

                            ssb.append("\n");
                        }

                        return ssb;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Spanned ssb) {
                        new AlertDialog.Builder(v.getContext())
                                .setIcon(R.drawable.twotone_info_24)
                                .setTitle(R.string.title_advanced_all_permissions)
                                .setMessage(ssb)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsMisc.this, new Bundle(), "misc:permissions");
            }
        });

        // Initialize
        swPowerMenu.setVisibility(!BuildConfig.PLAY_STORE_RELEASE &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                ? View.VISIBLE : View.GONE);

        tvFtsIndexed.setText(null);

        swExternalAnswer.setVisibility(
                ActivityAnswer.canAnswer(getContext()) ? View.VISIBLE : View.GONE);

        DB db = DB.getInstance(getContext());
        db.message().liveFts().observe(getViewLifecycleOwner(), new Observer<TupleFtsStats>() {
            private TupleFtsStats last = null;

            @Override
            public void onChanged(TupleFtsStats stats) {
                if (stats == null)
                    tvFtsIndexed.setText(null);
                else if (last == null || !last.equals(stats))
                    tvFtsIndexed.setText(getString(R.string.title_advanced_fts_indexed,
                            stats.fts,
                            stats.total,
                            Helper.humanReadableByteCount(Fts4DbHelper.size(tvFtsIndexed.getContext()))));
                last = stats;
            }
        });

        grpUpdates.setVisibility(!BuildConfig.DEBUG &&
                (Helper.isPlayStoreInstall() || !Helper.hasValidFingerprint(getContext()))
                ? View.GONE : View.VISIBLE);
        grpBitbucket.setVisibility(View.GONE);
        grpAnnouncements.setVisibility(TextUtils.isEmpty(BuildConfig.ANNOUNCEMENT_URI)
                ? View.GONE : View.VISIBLE);
        grpTest.setVisibility(Log.isTestRelease() ? View.VISIBLE : View.GONE);

        setLastCleanup(prefs.getLong("last_cleanup", -1));

        if (prefs.contains("last_daily"))
            tvLastDaily.setText(new Date(prefs.getLong("last_daily", 0)).toString());
        else
            tvLastDaily.setText(("-"));

        File external = Helper.getExternalFilesDir(getContext());
        boolean emulated = (external != null && Environment.isExternalStorageEmulated(external));
        tvExternalStorageFolder.setText(
                (external == null ? null : external.getAbsolutePath()) + (emulated ? " emulated" : ""));

        swExactAlarms.setEnabled(AlarmManagerCompatEx.canScheduleExactAlarms(getContext()));
        swTestIab.setVisibility(BuildConfig.DEBUG && Log.isTestRelease() ? View.VISIBLE : View.GONE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContactInfo();
        setSuffixes();
        setPermissionInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;

        if (!Helper.isPlayStoreInstall() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm =
                    Helper.getSystemService(getContext(), NotificationManager.class);

            NotificationChannel notification = nm.getNotificationChannel("update");
            if (notification != null) {
                boolean disabled = notification.getImportance() == NotificationManager.IMPORTANCE_NONE;
                ibChannelUpdated.setImageLevel(disabled ? 0 : 1);
                ibChannelUpdated.setVisibility(disabled ? View.VISIBLE : View.GONE);
            }
        }

        View view = getView();
        if (view != null)
            view.post(new Runnable() {
                @Override
                public void run() {
                    updateUsage();
                }
            });
    }

    @Override
    public void onPause() {
        super.onPause();
        resumed = false;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_CLASSIFIER:
                    if (resultCode == Activity.RESULT_OK && data != null)
                        onHandleExportClassifier(data);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (!RESET_OPTIONS.contains(key) &&
                !"last_cleanup".equals(key) &&
                !"last_daily".equals(key))
            return;

        if ("last_cleanup".equals(key))
            setLastCleanup(prefs.getLong(key, -1));

        if ("last_daily".equals(key))
            tvLastDaily.setText(new Date(prefs.getLong(key, 0)).toString());

        if ("viewport_height".equals(key))
            return;

        if ("favicon_uri".equals(key))
            return;

        if ("global_keywords".equals(key))
            return;

        if ("native_arc_whitelist".equals(key))
            return;

        getMainHandler().removeCallbacks(update);
        getMainHandler().postDelayed(update, FragmentOptions.DELAY_SETOPTIONS);
    }

    private Runnable update = new RunnableEx("misc") {
        @Override
        protected void delegate() {
            setOptions();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_default) {
            FragmentOptions.reset(getContext(), RESET_OPTIONS, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onResetQuestions() {
        final Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_reset_questions, null);
        final CheckBox cbGeneral = dview.findViewById(R.id.cbGeneral);
        final CheckBox cbLinks = dview.findViewById(R.id.cbLinks);
        final CheckBox cbFiles = dview.findViewById(R.id.cbFiles);
        final CheckBox cbImages = dview.findViewById(R.id.cbImages);
        final CheckBox cbFull = dview.findViewById(R.id.cbFull);

        new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();

                        if (cbGeneral.isChecked())
                            for (String key : RESET_QUESTIONS)
                                if (prefs.contains(key)) {
                                    Log.i("Removing option=" + key);
                                    editor.remove(key);
                                }

                        for (String key : prefs.getAll().keySet())
                            if ((!BuildConfig.DEBUG &&
                                    key.startsWith("translated_") && cbGeneral.isChecked()) ||
                                    key.startsWith("oauth.") ||
                                    (key.startsWith("announcement.") && cbGeneral.isChecked()) ||
                                    (key.endsWith(".confirm_link") && cbLinks.isChecked()) ||
                                    ("confirm_links".equals(key) && cbLinks.isChecked()) ||
                                    (key.endsWith(".link_view") && cbLinks.isChecked()) ||
                                    (key.endsWith(".link_sanitize") && cbLinks.isChecked()) ||
                                    (key.endsWith(".confirm_files") && cbFiles.isChecked()) ||
                                    ("confirm_files".equals(key) && cbFiles.isChecked()) ||
                                    (key.endsWith(".show_images") && cbImages.isChecked()) ||
                                    ("confirm_images".equals(key) && cbImages.isChecked()) ||
                                    (key.endsWith(".show_full") && cbFull.isChecked()) ||
                                    ("confirm_html".equals(key) && cbFull.isChecked())) {
                                Log.i("Removing option=" + key);
                                editor.remove(key);
                            }

                        editor.apply();

                        ToastEx.makeText(context, R.string.title_setup_done, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onCleanup() {
        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                btnCleanup.setEnabled(false);
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                btnCleanup.setEnabled(true);
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                WorkerCleanup.cleanup(context, true);
                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                final Context context = getContext();
                WorkManager.getInstance(context).pruneWork();
                WorkerAutoUpdate.init(context);
                WorkerCleanup.init(context);
                WorkerDailyRules.init(context);
                WorkerSync.init(context);
                ToastEx.makeText(context, R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "cleanup:run");
    }

    private void setOptions() {
        try {
            if (view == null || getContext() == null)
                return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            ActivityManager am = Helper.getSystemService(getContext(), ActivityManager.class);
            int class_mb = am.getMemoryClass();
            int class_large_mb = am.getLargeMemoryClass();
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);

            swSortAnswers.setChecked(prefs.getBoolean("sort_answers", false));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                swPowerMenu.setChecked(Helper.isComponentEnabled(getContext(), ServicePowerControl.class));
            swSendSelf.setChecked(Helper.isComponentEnabled(getContext(), ActivitySendSelf.class));
            swExternalSearch.setChecked(Helper.isComponentEnabled(getContext(), ActivitySearch.class));
            swExternalAnswer.setChecked(Helper.isComponentEnabled(getContext(), ActivityAnswer.class));
            swShortcuts.setChecked(prefs.getBoolean("shortcuts", true));
            swICalTentative.setChecked(prefs.getBoolean("ical_tentative", true));
            swFts.setChecked(prefs.getBoolean("fts", false));

            swClassification.setChecked(prefs.getBoolean("classification", false));

            int class_min_chance = prefs.getInt("class_min_probability", 5);
            tvClassMinProbability.setText(getString(R.string.title_advanced_class_min_chance, NF.format(class_min_chance)));
            sbClassMinProbability.setProgress(class_min_chance);

            int class_min_difference = prefs.getInt("class_min_difference", 40);
            tvClassMinDifference.setText(getString(R.string.title_advanced_class_min_difference, NF.format(class_min_difference)));
            sbClassMinDifference.setProgress(class_min_difference);

            swShowFiltered.setChecked(prefs.getBoolean("show_filtered", false));
            swHapticFeedback.setChecked(prefs.getBoolean("haptic_feedback", true));
            swHapticFeedbackSwipe.setChecked(prefs.getBoolean("haptic_feedback_swipe", false));

            int selected = -1;
            String language = prefs.getString("language", null);
            List<String> display = new ArrayList<>();
            display.add(getString(R.string.title_advanced_language_system));
            for (int pos = 0; pos < languages.size(); pos++) {
                Pair<String, String> lang = languages.get(pos);
                display.add(lang.second);
                if (lang.first.equals(language))
                    selected = pos + 1;
            }

            swUpdates.setChecked(prefs.getBoolean("updates", true));
            swCheckWeekly.setChecked(prefs.getBoolean("weekly", Helper.hasPlayStore(getContext())));
            swCheckWeekly.setEnabled(swUpdates.isChecked());
            swBeta.setChecked(prefs.getBoolean("beta", false));
            swBeta.setEnabled(swUpdates.isChecked());
            swChangelog.setChecked(prefs.getBoolean("show_changelog", true));
            swAnnouncements.setChecked(prefs.getBoolean("announcements", true));
            swExperiments.setChecked(prefs.getBoolean("experiments", false));
            swCrashReports.setChecked(prefs.getBoolean("crash_reports", false));
            tvUuid.setText(prefs.getString("uuid", null));
            swCleanupAttachments.setChecked(prefs.getBoolean("cleanup_attachments", false));
            swGoogleBackup.setChecked(prefs.getBoolean("google_backup", BuildConfig.PLAY_STORE_RELEASE));

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, android.R.id.text1, display);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spLanguage.setAdapter(adapter);
            if (selected >= 0)
                spLanguage.setSelection(selected);

            swWatchdog.setChecked(prefs.getBoolean("watchdog", true));
            swMainLog.setChecked(prefs.getBoolean("main_log", true));
            swMainLogMem.setChecked(prefs.getBoolean("main_log_memory", false));
            swMainLogMem.setEnabled(swMainLog.isChecked());
            swProtocol.setChecked(prefs.getBoolean("protocol", false));
            swLogInfo.setChecked(prefs.getInt("log_level", android.util.Log.WARN) <= android.util.Log.INFO);
            swDebug.setChecked(prefs.getBoolean("debug", false));
            swCanary.setChecked(prefs.getBoolean("leak_canary", BuildConfig.TEST_RELEASE));
            swTest1.setChecked(prefs.getBoolean("test1", false));
            swTest2.setChecked(prefs.getBoolean("test2", false));
            swTest3.setChecked(prefs.getBoolean("test3", false));
            swTest4.setChecked(prefs.getBoolean("test4", false));
            swTest5.setChecked(prefs.getBoolean("test5", false));

            swAutostart.setChecked(Helper.isComponentEnabled(getContext(), ReceiverAutoStart.class));
            swEmergency.setChecked(prefs.getBoolean("emergency_file", true));
            swWorkManager.setChecked(prefs.getBoolean("work_manager", true));
            swTaskDescription.setChecked(prefs.getBoolean("task_description", true));
            swExternalStorage.setChecked(prefs.getBoolean("external_storage", false));

            swIntegrity.setChecked(prefs.getBoolean("sqlite_integrity_check", false));
            swWal.setChecked(prefs.getBoolean("wal", true));
            swCheckpoints.setChecked(prefs.getBoolean("sqlite_checkpoints", true));
            swAnalyze.setChecked(prefs.getBoolean("sqlite_analyze", true));
            swAutoVacuum.setChecked(prefs.getBoolean("sqlite_auto_vacuum", false));
            swSyncExtra.setChecked(prefs.getBoolean("sqlite_sync_extra", true));

            int sqlite_cache = prefs.getInt("sqlite_cache", DB.DEFAULT_CACHE_SIZE);
            Integer cache_size = DB.getCacheSizeKb(getContext());
            if (cache_size == null)
                cache_size = 2000;
            tvSqliteCache.setText(getString(R.string.title_advanced_sqlite_cache,
                    NF.format(sqlite_cache),
                    Helper.humanReadableByteCount(cache_size * 1024L)));
            sbSqliteCache.setProgress(sqlite_cache);

            swCacheLists.setChecked(prefs.getBoolean("cache_lists", true));
            swOauthTabs.setChecked(prefs.getBoolean("oauth_tabs", true));

            int start_delay = prefs.getInt("start_delay", 0);
            tvStartDelay.setText(getString(R.string.title_advanced_start_delay, start_delay));
            sbStartDelay.setProgress(start_delay);

            int range_size = prefs.getInt("range_size", Core.DEFAULT_RANGE_SIZE);
            tvRangeSize.setText(getString(R.string.title_advanced_range_size, range_size));
            sbRangeSize.setProgress(range_size);

            int chunk_size = prefs.getInt("chunk_size", Core.DEFAULT_CHUNK_SIZE);
            tvChunkSize.setText(getString(R.string.title_advanced_chunk_size, chunk_size));
            sbChunkSize.setProgress(chunk_size);

            int thread_range = prefs.getInt("thread_range", MessageHelper.DEFAULT_THREAD_RANGE);
            int range = (int) Math.pow(2, thread_range);
            tvThreadRange.setText(getString(R.string.title_advanced_thread_range, range));
            sbThreadRange.setProgress(thread_range);

            int restart_interval = prefs.getInt("restart_interval", EmailService.DEFAULT_RESTART_INTERVAL);
            tvRestartInterval.setText(getString(R.string.title_advanced_restart_interval, restart_interval));
            sbRestartInterval.setProgress(restart_interval / 10);

            swAutoScroll.setChecked(prefs.getBoolean("autoscroll_editor", false));
            swUndoManager.setChecked(prefs.getBoolean("undo_manager", false));
            swBrowserZoom.setChecked(prefs.getBoolean("browser_zoom", false));

            int dvh = WebViewEx.getDefaultViewportHeight(getContext());
            int vh = prefs.getInt("viewport_height", dvh);
            etViewportHeight.setText(Integer.toString(vh));

            swIgnoreFormattedSize.setChecked(prefs.getBoolean("ignore_formatted_size", false));
            swShowRecent.setChecked(prefs.getBoolean("show_recent", false));
            swModSeq.setChecked(prefs.getBoolean("use_modseq", true));
            swPreamble.setChecked(prefs.getBoolean("preamble", false));
            swUid.setChecked(prefs.getBoolean("uid_command", false));
            swExpunge.setChecked(prefs.getBoolean("perform_expunge", true));
            swUidExpunge.setChecked(prefs.getBoolean("uid_expunge", false));
            swAuthPlain.setChecked(prefs.getBoolean("auth_plain", true));
            swAuthLogin.setChecked(prefs.getBoolean("auth_login", true));
            swAuthNtlm.setChecked(prefs.getBoolean("auth_ntlm", true));
            swAuthSasl.setChecked(prefs.getBoolean("auth_sasl", true));
            swAuthApop.setChecked(prefs.getBoolean("auth_apop", false));
            swUseTop.setChecked(prefs.getBoolean("use_top", true));
            swForgetTop.setChecked(prefs.getBoolean("forget_top", false));
            swKeepAlivePoll.setChecked(prefs.getBoolean("keep_alive_poll", false));
            swEmptyPool.setChecked(prefs.getBoolean("empty_pool", true));
            swIdleDone.setChecked(prefs.getBoolean("idle_done", true));
            swFastFetch.setChecked(prefs.getBoolean("fast_fetch", false));

            int max_backoff_power = prefs.getInt("max_backoff_power", ServiceSynchronize.DEFAULT_BACKOFF_POWER - 3);
            int max_backoff = (int) Math.pow(2, max_backoff_power + 3);
            tvMaxBackoff.setText(getString(R.string.title_advanced_max_backoff, max_backoff));
            sbMaxBackOff.setProgress(max_backoff_power);

            swLogarithmicBackoff.setChecked(prefs.getBoolean("logarithmic_backoff", true));
            swExactAlarms.setChecked(prefs.getBoolean("exact_alarms", true));
            swNativeDkim.setEnabled(!BuildConfig.PLAY_STORE_RELEASE || true);
            swNativeDkim.setChecked(prefs.getBoolean("native_dkim", false));
            swNativeArc.setEnabled(swNativeDkim.isEnabled() && swNativeDkim.isChecked());
            swNativeArc.setChecked(prefs.getBoolean("native_arc", true));
            etNativeArcWhitelist.setEnabled(swNativeDkim.isEnabled() && swNativeDkim.isChecked());
            etNativeArcWhitelist.setText(prefs.getString("native_arc_whitelist", null));
            swStrictAlignment.setEnabled(swNativeDkim.isEnabled() && swNativeDkim.isChecked());
            swStrictAlignment.setChecked(prefs.getBoolean("strict_alignment", false));
            swSvg.setChecked(prefs.getBoolean("svg", true));
            swWebp.setChecked(prefs.getBoolean("webp", true));
            swAnimate.setChecked(prefs.getBoolean("animate_images", true));
            swPreviewHidden.setChecked(prefs.getBoolean("preview_hidden", true));
            swPreviewQuotes.setChecked(prefs.getBoolean("preview_quotes", true));
            swEasyCorrect.setChecked(prefs.getBoolean("easy_correct", false));
            swPastePlain.setChecked(prefs.getBoolean("paste_plain", false));
            swPasteQuote.setChecked(prefs.getBoolean("paste_quote", false));
            etFaviconUri.setText(prefs.getString("favicon_uri", null));
            swInfra.setChecked(prefs.getBoolean("infra", false));
            swEmailJunk.setChecked(prefs.getBoolean("email_junk", false));
            swTldFlags.setChecked(prefs.getBoolean("tld_flags", false));
            swJsonLd.setChecked(prefs.getBoolean("json_ld", false));
            swDupMsgId.setChecked(prefs.getBoolean("dup_msgids", false));
            swThreadByRef.setChecked(prefs.getBoolean("thread_byref", !Helper.isPlayStoreInstall()));
            swSaveUserFlags.setChecked(prefs.getBoolean("save_user_flags", false));
            swMdn.setChecked(prefs.getBoolean("mdn", swExperiments.isChecked()));
            swAppChooser.setChecked(prefs.getBoolean("app_chooser", false));
            swAppChooserShare.setChecked(prefs.getBoolean("app_chooser_share", false));
            swShareTask.setChecked(prefs.getBoolean("share_task", false));
            swAdjacentLinks.setChecked(prefs.getBoolean("adjacent_links", false));
            swAdjacentDocuments.setChecked(prefs.getBoolean("adjacent_documents", true));
            swAdjacentPortrait.setChecked(prefs.getBoolean("adjacent_portrait", false));
            swAdjacentLandscape.setChecked(prefs.getBoolean("adjacent_landscape", false));
            swDeleteConfirmation.setChecked(prefs.getBoolean("delete_confirmation", true));
            swDeleteNotification.setChecked(prefs.getBoolean("delete_notification", false));
            swDmarcViewer.setChecked(Helper.isComponentEnabled(getContext(), ActivityDMARC.class));
            etKeywords.setText(prefs.getString("global_keywords", null));
            swTestIab.setChecked(prefs.getBoolean("test_iab", false));

            tvProcessors.setText(getString(R.string.title_advanced_processors, Runtime.getRuntime().availableProcessors()));
            tvMemoryClass.setText(getString(R.string.title_advanced_memory_class,
                    class_mb + " MB",
                    class_large_mb + " MB",
                    Helper.humanReadableByteCount(mi.totalMem)));

            String android_id;
            try {
                android_id = Settings.Secure.getString(
                        getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (android_id == null)
                    android_id = "<null>";
            } catch (Throwable ex) {
                Log.w(ex);
                android_id = "?";
            }
            tvAndroidId.setText(getString(R.string.title_advanced_android_id, android_id));

            tvFingerprint.setText(Helper.getFingerprint(getContext()));

            Integer cursorWindowSize = null;
            try {
                //Field fCursorWindowSize = android.database.CursorWindow.class.getDeclaredField("sDefaultCursorWindowSize");
                //fCursorWindowSize.setAccessible(true);
                //cursorWindowSize = fCursorWindowSize.getInt(null);
            } catch (Throwable ex) {
                Log.w(ex);
            }
            tvCursorWindow.setText(getString(R.string.title_advanced_cursor_window,
                    cursorWindowSize == null ? "?" : Helper.humanReadableByteCount(cursorWindowSize, false)));

            cardDebug.setVisibility(swDebug.isChecked() || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void updateUsage() {
        if (!resumed)
            return;

        try {
            Log.i("Update usage");

            Bundle args = new Bundle();

            new SimpleTask<StorageData>() {
                @Override
                protected StorageData onExecute(Context context, Bundle args) {
                    StorageData data = new StorageData();
                    Runtime rt = Runtime.getRuntime();
                    data.hused = rt.totalMemory() - rt.freeMemory();
                    data.hmax = rt.maxMemory();
                    data.nheap = Debug.getNativeHeapAllocatedSize();
                    data.available = Helper.getAvailableStorageSpace();
                    data.total = Helper.getTotalStorageSpace();
                    data.used = Helper.getSizeUsed(context.getFilesDir());
                    data.cache_used = Helper.getSizeUsed(context.getCacheDir());
                    data.cache_quota = Helper.getCacheQuota(context);
                    return data;
                }

                @Override
                protected void onExecuted(Bundle args, StorageData data) {
                    tvMemoryUsage.setText(getString(R.string.title_advanced_memory_usage,
                            Helper.humanReadableByteCount(data.hused),
                            Helper.humanReadableByteCount(data.hmax),
                            Helper.humanReadableByteCount(data.nheap)));

                    tvStorageUsage.setText(getString(R.string.title_advanced_storage_usage,
                            Helper.humanReadableByteCount(data.total - data.available),
                            Helper.humanReadableByteCount(data.total),
                            Helper.humanReadableByteCount(data.used)));
                    tvCacheUsage.setText(getString(R.string.title_advanced_cache_usage,
                            Helper.humanReadableByteCount(data.cache_used),
                            Helper.humanReadableByteCount(data.cache_quota)));

                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateUsage();
                        }
                    }, 2500);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.e(ex);
                }
            }.execute(this, args, "usage");
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void setLastCleanup(long time) {
        if (getContext() == null)
            return;

        java.text.DateFormat DTF = Helper.getDateTimeInstance(getContext());
        tvLastCleanup.setText(
                getString(R.string.title_advanced_last_cleanup,
                        time < 0 ? "-" : DTF.format(time)));
    }

    private void setContactInfo() {
        int[] stats = ContactInfo.getStats();
        tvContactInfo.setText(getString(R.string.title_advanced_contact_info, stats[0], stats[1]));
    }

    private void setSuffixes() {
        new SimpleTask<Integer>() {
            @Override
            protected void onPreExecute(Bundle args) {
                tvSuffixes.setText(getString(R.string.title_advanced_suffixes, -1));
            }

            @Override
            protected Integer onExecute(Context context, Bundle args) {
                return UriHelper.getSuffixCount(context);
            }

            @Override
            protected void onExecuted(Bundle args, Integer count) {
                tvSuffixes.setText(getString(R.string.title_advanced_suffixes, count));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.w(ex);
                tvSuffixes.setText(new ThrowableWrapper(ex).toSafeString());
            }
        }.execute(this, new Bundle(), "suffixes");
    }

    private void setPermissionInfo() {
        new SimpleTask<Spanned>() {
            @Override
            protected void onPreExecute(Bundle args) {
                tvPermissions.setText(null);
            }

            @Override
            protected Spanned onExecute(Context context, Bundle args) throws Throwable {
                return new SpannableStringBuilderEx();
/*
                int start = 0;
                int dp24 = Helper.dp2pixels(getContext(), 24);
                SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                PackageManager pm = getContext().getPackageManager();
                PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_PERMISSIONS);
                for (int i = 0; i < pi.requestedPermissions.length; i++) {
                    boolean granted = ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);

                    PermissionInfo info;
                    try {
                        info = pm.getPermissionInfo(pi.requestedPermissions[i], PackageManager.GET_META_DATA);
                    } catch (Throwable ex) {
                        info = new PermissionInfo();
                        info.name = pi.requestedPermissions[i];
                        if (!(ex instanceof PackageManager.NameNotFoundException))
                            info.group = ex.toString();
                    }

                    ssb.append(info.name).append('\n'); // TODO CASA permission info
                    if (granted)
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                    start = ssb.length();

                    if (info.group != null) {
                        ssb.append(info.group).append('\n'); // TODO CASA permission info
                        ssb.setSpan(new IndentSpan(dp24), start, ssb.length(), 0);
                        start = ssb.length();
                    }

                    CharSequence description = info.loadDescription(pm);
                    if (description != null) {
                        ssb.append(description).append('\n');
                        ssb.setSpan(new IndentSpan(dp24), start, ssb.length(), 0);
                        ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), start, ssb.length(), 0);
                        start = ssb.length();
                    }

                    if (info.protectionLevel != 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            switch (info.getProtection()) {
                                case PermissionInfo.PROTECTION_DANGEROUS:
                                    ssb.append("dangerous ");
                                    break;
                                case PermissionInfo.PROTECTION_NORMAL:
                                    ssb.append("normal ");
                                    break;
                                case PermissionInfo.PROTECTION_SIGNATURE:
                                    ssb.append("signature ");
                                    break;
                                case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
                                    ssb.append("signatureOrSystem ");
                                    break;
                            }

                        ssb.append(Integer.toHexString(info.protectionLevel)); // TODO CASA permission info

                        if (info.flags != 0)
                            ssb.append(' ').append(Integer.toHexString(info.flags));

                        ssb.append('\n');
                        ssb.setSpan(new IndentSpan(dp24), start, ssb.length(), 0);
                        start = ssb.length();
                    }

                    ssb.append('\n');
                }

                return ssb;
 */
            }

            @Override
            protected void onExecuted(Bundle args, Spanned permissions) {
                tvPermissions.setText(permissions);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.w(ex);
                tvPermissions.setText(new ThrowableWrapper(ex).toSafeString());
            }
        }.execute(this, new Bundle(), "permissions");
    }

    private void onExportClassifier(Context context) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "classifier.json");
        Helper.openAdvanced(context, intent);
        startActivityForResult(intent, REQUEST_CLASSIFIER);
    }

    private void onHandleExportClassifier(Intent intent) {
        Bundle args = new Bundle();
        args.putParcelable("uri", intent.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                ContentResolver resolver = context.getContentResolver();
                File file = MessageClassifier.getFile(context, false);
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    try (InputStream is = new FileInputStream(file)) {
                        Helper.copy(is, os);
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_setup_exported, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "classifier");
    }

    private static class StorageData {
        private long hused;
        private long hmax;
        private long nheap;
        private long available;
        private long total;
        private long used;
        private long cache_used;
        private long cache_quota;
    }
}
