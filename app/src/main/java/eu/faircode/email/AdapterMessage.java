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

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.Pair;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Attendee;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.util.ICalDate;

import static android.app.Activity.RESULT_OK;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.ViewHolder> {
    private Fragment parentFragment;
    private String type;
    private ViewType viewType;
    private boolean compact;
    private int zoom;
    private String sort;
    private boolean ascending;
    private boolean filter_duplicates;
    private IProperties properties;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private boolean accessibility;

    private boolean suitable;
    private boolean unmetered;

    private int dp36;
    private int colorPrimary;
    private int colorAccent;
    private int textColorPrimary;
    private int textColorSecondary;
    private int colorUnread;
    private int colorRead;
    private int colorSeparator;

    private boolean hasWebView;
    private boolean contacts;
    private float textSize;

    private boolean date;
    private boolean threading;
    private boolean avatars;
    private boolean name_email;
    private boolean distinguish_contacts;
    private Float font_size_sender;
    private Float font_size_subject;
    private boolean subject_top;
    private boolean subject_italic;
    private String subject_ellipsize;

    private boolean flags;
    private boolean flags_background;
    private boolean preview;
    private boolean preview_italic;
    private int preview_lines;
    private boolean attachments_alt;
    private boolean contrast;
    private boolean monospaced;
    private boolean inline;
    private boolean collapse_quotes;
    private boolean authentication;
    private static boolean debug;

    private int answers = -1;
    private boolean gotoTop = false;
    private boolean firstClick = false;
    private AsyncPagedListDiffer<TupleMessageEx> differ;
    private Map<Long, Integer> keyPosition = new HashMap<>();
    private SelectionTracker<Long> selectionTracker = null;

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    private NumberFormat NF = NumberFormat.getNumberInstance();
    private DateFormat TF;
    private DateFormat DTF;

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(2, "differ");

    // https://github.com/newhouse/url-tracking-stripper
    private static final List<String> PARANOID_QUERY = Collections.unmodifiableList(Arrays.asList(
            "utm_source",
            "utm_medium",
            "utm_campaign",
            "utm_term",
            "utm_content",

            "utm_name",
            "utm_cid",
            "utm_reader",
            "utm_viz_id",
            "utm_pubreferrer",
            "utm_swu",

            "gclid",
            "fbclid"
    ));

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnKeyListener,
            View.OnClickListener,
            View.OnLongClickListener,
            View.OnTouchListener,
            View.OnLayoutChangeListener,
            BottomNavigationView.OnNavigationItemSelectedListener {
        private ViewCardOptional card;
        private View view;

        private View vwColor;
        private ImageButton ibExpander;
        private ImageView ibFlagged;
        private ImageButton ibAvatar;
        private ImageButton ibAuth;
        private ImageView ivPriorityHigh;
        private ImageView ivPriorityLow;
        private ImageView ivSigned;
        private ImageView ivEncrypted;
        private TextView tvFrom;
        private TextView tvSize;
        private TextView tvTime;
        private ImageView ivType;
        private ImageButton ibSnoozed;
        private ImageView ivAnswered;
        private ImageView ivAttachments;
        private TextView tvSubject;
        private TextView tvFolder;
        private TextView tvCount;
        private ImageView ivThread;
        private TextView tvExpand;
        private TextView tvPreview;
        private TextView tvError;
        private ImageButton ibHelp;

        private View vsBody;

        private ImageButton ibExpanderAddress;

        private ImageView ivPlain;
        private ImageView ivReceipt;
        private ImageView ivBrowsed;

        private ImageButton ibSearchContact;
        private ImageButton ibNotifyContact;
        private ImageButton ibAddContact;

        private TextView tvFromExTitle;
        private TextView tvToTitle;
        private TextView tvReplyToTitle;
        private TextView tvCcTitle;
        private TextView tvBccTitle;
        private TextView tvIdentityTitle;
        private TextView tvSentTitle;
        private TextView tvReceivedTitle;
        private TextView tvSizeExTitle;

        private TextView tvFromEx;
        private TextView tvTo;
        private TextView tvReplyTo;
        private TextView tvCc;
        private TextView tvBcc;
        private TextView tvIdentity;
        private TextView tvSent;
        private TextView tvReceived;
        private TextView tvSizeEx;

        private TextView tvSubjectEx;
        private TextView tvFlags;
        private TextView tvKeywords;

        private TextView tvHeaders;
        private ContentLoadingProgressBar pbHeaders;
        private TextView tvNoInternetHeaders;

        private RecyclerView rvAttachment;
        private CheckBox cbInline;
        private Button btnSaveAttachments;
        private Button btnDownloadAttachments;
        private TextView tvNoInternetAttachments;

        private BottomNavigationView bnvActions;
        private Group grpActions;

        private ImageButton ibFull;
        private ImageButton ibImages;
        private ImageButton ibUnsubscribe;
        private ImageButton ibVerify;
        private ImageButton ibDecrypt;

        private TextView tvBody;
        private View wvBody;
        private ContentLoadingProgressBar pbBody;
        private TextView tvNoInternetBody;
        private ImageButton ibDownloading;
        private Group grpDownloading;

        private TextView tvCalendarSummary;
        private TextView tvCalendarStart;
        private TextView tvCalendarEnd;
        private TextView tvAttendees;
        private Button btnCalendarAccept;
        private Button btnCalendarDecline;
        private Button btnCalendarMaybe;
        private ImageButton ibCalendar;
        private ContentLoadingProgressBar pbCalendarWait;

        private RecyclerView rvImage;

        private Group grpAddresses;
        private Group grpHeaders;
        private Group grpCalendar;
        private Group grpCalendarResponse;
        private Group grpAttachments;
        private Group grpImages;

        private AdapterAttachment adapterAttachment;
        private AdapterImage adapterImage;

        private TwoStateOwner cowner = new TwoStateOwner(owner, "MessageAttachments");
        private TwoStateOwner powner = new TwoStateOwner(owner, "MessagePopup");

        private boolean hasJunk;
        private boolean delete;

        private ScaleGestureDetector gestureDetector;

        ViewHolder(final View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            view = itemView.findViewById(R.id.clItem);

            vwColor = itemView.findViewById(R.id.vwColor);
            ibExpander = itemView.findViewById(R.id.ibExpander);
            ibFlagged = itemView.findViewById(R.id.ibFlagged);
            ibAvatar = itemView.findViewById(R.id.ibAvatar);
            ibAuth = itemView.findViewById(R.id.ibAuth);
            ivPriorityHigh = itemView.findViewById(R.id.ivPriorityHigh);
            ivPriorityLow = itemView.findViewById(R.id.ivPriorityLow);
            ivSigned = itemView.findViewById(R.id.ivSigned);
            ivEncrypted = itemView.findViewById(R.id.ivEncrypted);
            tvFrom = itemView.findViewById(subject_top ? R.id.tvSubject : R.id.tvFrom);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivType = itemView.findViewById(R.id.ivType);
            ibSnoozed = itemView.findViewById(R.id.ibSnoozed);
            ivAnswered = itemView.findViewById(R.id.ivAnswered);
            ivAttachments = itemView.findViewById(R.id.ivAttachments);
            tvSubject = itemView.findViewById(subject_top ? R.id.tvFrom : R.id.tvSubject);
            tvExpand = itemView.findViewById(R.id.tvExpand);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvCount = itemView.findViewById(R.id.tvCount);
            ivThread = itemView.findViewById(R.id.ivThread);
            tvError = itemView.findViewById(R.id.tvError);
            ibHelp = itemView.findViewById(R.id.ibHelp);

            if (tvSubject != null) {
                tvSubject.setTextColor(colorRead);

                if (compact)
                    if ("start".equals(subject_ellipsize))
                        tvSubject.setEllipsize(TextUtils.TruncateAt.START);
                    else if ("end".equals(subject_ellipsize))
                        tvSubject.setEllipsize(TextUtils.TruncateAt.END);
                    else
                        tvSubject.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            }

            // Accessibility

            if (!BuildConfig.DEBUG && !accessibility)
                return;

            view.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);

                    TupleMessageEx message = getMessage();
                    if (message == null || (filter_duplicates && message.duplicate))
                        return;

                    boolean expanded = properties.getValue("expanded", message.id);

                    List<String> result = new ArrayList<>();

                    vwColor.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (selectionTracker != null && selectionTracker.isSelected(message.id))
                        result.add(context.getString(R.string.title_accessibility_selected));

                    if (message.count == 1)
                        result.add(context.getString(message.unseen > 0 ? R.string.title_accessibility_unseen : R.string.title_accessibility_seen));
                    else if (message.unseen == message.count)
                        result.add(context.getResources().getQuantityString(
                                R.plurals.title_accessibility_all_of_unseen, message.count, message.count));
                    else if (message.unseen == 0)
                        result.add(context.getResources().getQuantityString(
                                R.plurals.title_accessibility_all_of_seen, message.count, message.count));
                    else
                        result.add(context.getResources().getQuantityString(
                                R.plurals.title_accessibility_count_of_seen, message.unseen, message.unseen, message.count));

                    if (ibExpander.getVisibility() == View.VISIBLE) {
                        result.add(context.getString(expanded ? R.string.title_accessibility_expanded : R.string.title_accessibility_collapsed));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibExpander,
                                context.getString(expanded ? R.string.title_accessibility_collapse : R.string.title_accessibility_expand)));
                    }
                    ibExpander.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (ibAvatar.getVisibility() == View.VISIBLE && ibAvatar.isEnabled())
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibAvatar,
                                context.getString(R.string.title_accessibility_view_contact)));
                    ibAvatar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (message.drafts > 0)
                        result.add(context.getString(R.string.title_legend_draft));
                    if (message.ui_answered)
                        result.add(context.getString(R.string.title_accessibility_answered));

                    if (ibFlagged.getVisibility() == View.VISIBLE && ibFlagged.isEnabled()) {
                        int flagged = (message.count - message.unflagged);
                        if (flagged > 0)
                            result.add(context.getString(R.string.title_accessibility_flagged));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibFlagged,
                                context.getString(flagged > 0 ? R.string.title_unflag : R.string.title_flag)));
                    }
                    ibFlagged.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (EntityMessage.PRIORITIY_HIGH.equals(message.priority))
                        result.add(context.getString(R.string.title_legend_priority));
                    if (EntityMessage.PRIORITIY_LOW.equals(message.priority))
                        result.add(context.getString(R.string.title_legend_priority_low));
                    if (message.attachments > 0)
                        result.add(context.getString(R.string.title_legend_attachment));

                    // For a11y purpose report addresses first in case of incoming message
                    boolean outgoing = isOutgoing(message);
                    if (!outgoing || message.count > 1)
                        result.add(tvFrom.getText().toString());
                    else
                        result.add(message.subject); // Don't want to ellipsize for a11y
                    result.add(tvTime.getText().toString());
                    if (outgoing && message.count == 1)
                        result.add(tvFrom.getText().toString());
                    else
                        result.add(message.subject);

                    if (message.encrypted > 0)
                        result.add(context.getString(R.string.title_legend_encrypted));
                    if (message.signed > 0)
                        result.add(context.getString(R.string.title_legend_signed));

                    if (ibAuth.getVisibility() == View.VISIBLE) {
                        result.add(context.getString(R.string.title_legend_auth));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibAuth,
                                context.getString(R.string.title_accessibility_show_authentication_result)));
                    }
                    ibAuth.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (ibSnoozed.getVisibility() == View.VISIBLE) {
                        result.add(context.getString(R.string.title_legend_snoozed));
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibSnoozed,
                                context.getString(R.string.title_accessibility_show_snooze_time)));
                    }
                    ibSnoozed.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (tvFolder.getVisibility() == View.VISIBLE)
                        result.add(tvFolder.getText().toString());
                    if (tvSize.getVisibility() == View.VISIBLE)
                        result.add(tvSize.getText().toString());
                    if (tvError.getVisibility() == View.VISIBLE)
                        result.add(tvError.getText().toString());
                    if (tvPreview.getVisibility() == View.VISIBLE)
                        result.add(tvPreview.getText().toString());

                    if (ibHelp.getVisibility() == View.VISIBLE)
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibHelp,
                                context.getString(R.string.title_accessibility_view_help)));
                    ibHelp.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    info.setContentDescription(TextUtils.join(", ", result));
                }

                @Override
                public boolean performAccessibilityAction(View host, int action, Bundle args) {
                    TupleMessageEx message = getMessage();
                    if (message == null)
                        return false;

                    switch (action) {
                        case R.id.ibExpander:
                            onToggleMessage(message);
                            return true;
                        case R.id.ibAvatar:
                            onViewContact(message);
                            return true;
                        case R.id.ibFlagged:
                            onToggleFlag(message);
                            return true;
                        case R.id.ibAuth:
                            onShowAuth(message);
                            return true;
                        case R.id.ibSnoozed:
                            onShowSnoozed(message);
                            return true;
                        case R.id.ibHelp:
                            onHelp(message);
                            return true;
                        default:
                            return super.performAccessibilityAction(host, action, args);
                    }
                }
            });
        }

        private void ensureExpanded() {
            if (vsBody != null)
                return;

            vsBody = ((ViewStub) itemView.findViewById(R.id.vsBody)).inflate();

            ConstraintLayout inAttachments = vsBody.findViewById(R.id.inAttachments);
            ConstraintLayout inAttachmentsAlt = vsBody.findViewById(R.id.inAttachmentsAlt);
            inAttachments.setVisibility(attachments_alt ? View.GONE : View.VISIBLE);
            inAttachmentsAlt.setVisibility(attachments_alt ? View.VISIBLE : View.GONE);
            ConstraintLayout attachments = (attachments_alt ? inAttachmentsAlt : inAttachments);

            ibExpanderAddress = vsBody.findViewById(R.id.ibExpanderAddress);

            ivPlain = vsBody.findViewById(R.id.ivPlain);
            ivReceipt = vsBody.findViewById(R.id.ivReceipt);
            ivBrowsed = vsBody.findViewById(R.id.ivBrowsed);

            ibSearchContact = vsBody.findViewById(R.id.ibSearchContact);
            ibNotifyContact = vsBody.findViewById(R.id.ibNotifyContact);
            ibAddContact = vsBody.findViewById(R.id.ibAddContact);

            tvFromExTitle = vsBody.findViewById(R.id.tvFromExTitle);
            tvToTitle = vsBody.findViewById(R.id.tvToTitle);
            tvReplyToTitle = vsBody.findViewById(R.id.tvReplyToTitle);
            tvCcTitle = vsBody.findViewById(R.id.tvCcTitle);
            tvBccTitle = vsBody.findViewById(R.id.tvBccTitle);
            tvIdentityTitle = vsBody.findViewById(R.id.tvIdentityTitle);
            tvSentTitle = vsBody.findViewById(R.id.tvSentTitle);
            tvReceivedTitle = vsBody.findViewById(R.id.tvReceivedTitle);
            tvSizeExTitle = vsBody.findViewById(R.id.tvSizeExTitle);

            tvFromEx = vsBody.findViewById(R.id.tvFromEx);
            tvTo = vsBody.findViewById(R.id.tvTo);
            tvReplyTo = vsBody.findViewById(R.id.tvReplyTo);
            tvCc = vsBody.findViewById(R.id.tvCc);
            tvBcc = vsBody.findViewById(R.id.tvBcc);
            tvIdentity = vsBody.findViewById(R.id.tvIdentity);
            tvSent = vsBody.findViewById(R.id.tvSent);
            tvReceived = vsBody.findViewById(R.id.tvReceived);
            tvSizeEx = vsBody.findViewById(R.id.tvSizeEx);

            tvSubjectEx = vsBody.findViewById(R.id.tvSubjectEx);
            tvFlags = vsBody.findViewById(R.id.tvFlags);
            tvKeywords = vsBody.findViewById(R.id.tvKeywords);

            tvHeaders = vsBody.findViewById(R.id.tvHeaders);
            pbHeaders = vsBody.findViewById(R.id.pbHeaders);
            tvNoInternetHeaders = vsBody.findViewById(R.id.tvNoInternetHeaders);

            tvCalendarSummary = vsBody.findViewById(R.id.tvCalendarSummary);
            tvCalendarStart = vsBody.findViewById(R.id.tvCalendarStart);
            tvCalendarEnd = vsBody.findViewById(R.id.tvCalendarEnd);
            tvAttendees = vsBody.findViewById(R.id.tvAttendees);
            btnCalendarAccept = vsBody.findViewById(R.id.btnCalendarAccept);
            btnCalendarDecline = vsBody.findViewById(R.id.btnCalendarDecline);
            btnCalendarMaybe = vsBody.findViewById(R.id.btnCalendarMaybe);
            ibCalendar = vsBody.findViewById(R.id.ibCalendar);
            pbCalendarWait = vsBody.findViewById(R.id.pbCalendarWait);

            rvAttachment = attachments.findViewById(R.id.rvAttachment);
            rvAttachment.setHasFixedSize(false);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            rvAttachment.setLayoutManager(llm);
            rvAttachment.setItemAnimator(null);

            adapterAttachment = new AdapterAttachment(parentFragment, true);
            rvAttachment.setAdapter(adapterAttachment);

            cbInline = attachments.findViewById(R.id.cbInline);
            btnSaveAttachments = attachments.findViewById(R.id.btnSaveAttachments);
            btnDownloadAttachments = attachments.findViewById(R.id.btnDownloadAttachments);
            tvNoInternetAttachments = attachments.findViewById(R.id.tvNoInternetAttachments);

            bnvActions = vsBody.findViewById(R.id.bnvActions);
            if (compact) {
                bnvActions.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);
                ViewGroup.LayoutParams lparam = bnvActions.getLayoutParams();
                lparam.height = dp36;
                bnvActions.setLayoutParams(lparam);
            }
            grpActions = vsBody.findViewById(R.id.grpActions);

            ibFull = vsBody.findViewById(R.id.ibFull);
            ibImages = vsBody.findViewById(R.id.ibImages);
            ibUnsubscribe = vsBody.findViewById(R.id.ibUnsubscribe);
            ibVerify = vsBody.findViewById(R.id.ibVerify);
            ibDecrypt = vsBody.findViewById(R.id.ibDecrypt);

            tvBody = vsBody.findViewById(R.id.tvBody);
            wvBody = vsBody.findViewById(R.id.wvBody);
            pbBody = vsBody.findViewById(R.id.pbBody);
            tvNoInternetBody = vsBody.findViewById(R.id.tvNoInternetBody);
            ibDownloading = vsBody.findViewById(R.id.ibDownloading);
            grpDownloading = vsBody.findViewById(R.id.grpDownloading);

            rvImage = vsBody.findViewById(R.id.rvImage);
            rvImage.setHasFixedSize(false);
            StaggeredGridLayoutManager sglm =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            rvImage.setLayoutManager(sglm);
            adapterImage = new AdapterImage(parentFragment);
            rvImage.setAdapter(adapterImage);

            grpAddresses = vsBody.findViewById(R.id.grpAddresses);
            grpHeaders = vsBody.findViewById(R.id.grpHeaders);
            grpCalendar = vsBody.findViewById(R.id.grpCalendar);
            grpCalendarResponse = vsBody.findViewById(R.id.grpCalendarResponse);
            grpAttachments = attachments.findViewById(R.id.grpAttachments);
            grpImages = vsBody.findViewById(R.id.grpImages);

            unwire();
            wire();
        }

        Rect getItemRect() {
            return new Rect(
                    super.itemView.getLeft(),
                    super.itemView.getTop(),
                    super.itemView.getRight(),
                    super.itemView.getBottom());
        }

        private void wire() {
            final View touch = (viewType == ViewType.THREAD ? ibExpander : vwColor);
            touch.setOnClickListener(this);
            view.post(new Runnable() {
                @Override
                public void run() {
                    Rect rect = new Rect(
                            view.getLeft(),
                            vwColor.getTop(),
                            view.getRight(),
                            vwColor.getBottom());
                    view.setTouchDelegate(new TouchDelegate(rect, touch));
                }
            });
            view.setOnKeyListener(this);

            ibAvatar.setOnClickListener(this);
            ibAuth.setOnClickListener(this);
            ibSnoozed.setOnClickListener(this);
            ibFlagged.setOnClickListener(this);
            if (viewType == ViewType.THREAD)
                ibFlagged.setOnLongClickListener(this);
            ibHelp.setOnClickListener(this);

            if (vsBody != null) {
                ibExpanderAddress.setOnClickListener(this);
                ibSearchContact.setOnClickListener(this);
                ibNotifyContact.setOnClickListener(this);
                ibAddContact.setOnClickListener(this);

                btnSaveAttachments.setOnClickListener(this);
                btnDownloadAttachments.setOnClickListener(this);

                bnvActions.setOnNavigationItemSelectedListener(this);

                ibFull.setOnClickListener(this);
                ibImages.setOnClickListener(this);
                ibUnsubscribe.setOnClickListener(this);
                ibVerify.setOnClickListener(this);
                ibDecrypt.setOnClickListener(this);

                ibDownloading.setOnClickListener(this);

                tvBody.setOnTouchListener(this);
                tvBody.addOnLayoutChangeListener(this);

                btnCalendarAccept.setOnClickListener(this);
                btnCalendarDecline.setOnClickListener(this);
                btnCalendarMaybe.setOnClickListener(this);
                ibCalendar.setOnClickListener(this);

                gestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        TupleMessageEx message = getMessage();
                        if (message != null) {
                            float factor = detector.getScaleFactor();
                            float size = tvBody.getTextSize() * factor;
                            //Log.i("Gesture factor=" + factor + " size=" + size);
                            properties.setSize(message.id, size);
                            tvBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                        }
                        return true;
                    }
                });
            }
        }

        private void unwire() {
            final View touch = (viewType == ViewType.THREAD ? ibExpander : vwColor);
            touch.setOnClickListener(null);
            view.setOnKeyListener(null);

            ibAvatar.setOnClickListener(null);
            ibAuth.setOnClickListener(null);
            ibSnoozed.setOnClickListener(null);
            ibFlagged.setOnClickListener(null);
            if (viewType == ViewType.THREAD)
                ibFlagged.setOnLongClickListener(null);
            ibHelp.setOnClickListener(null);

            if (vsBody != null) {
                ibExpanderAddress.setOnClickListener(null);
                ibSearchContact.setOnClickListener(null);
                ibNotifyContact.setOnClickListener(null);
                ibAddContact.setOnClickListener(null);

                btnSaveAttachments.setOnClickListener(null);
                btnDownloadAttachments.setOnClickListener(null);

                bnvActions.setOnNavigationItemSelectedListener(null);

                ibFull.setOnClickListener(null);
                ibImages.setOnClickListener(null);
                ibUnsubscribe.setOnClickListener(null);
                ibVerify.setOnClickListener(null);
                ibDecrypt.setOnClickListener(null);

                ibDownloading.setOnClickListener(null);

                tvBody.setOnTouchListener(null);
                tvBody.removeOnLayoutChangeListener(this);

                btnCalendarAccept.setOnClickListener(null);
                btnCalendarDecline.setOnClickListener(null);
                btnCalendarMaybe.setOnClickListener(null);
                ibCalendar.setOnClickListener(null);
            }
        }

        private void clear() {
            vwColor.setVisibility(View.GONE);
            ibExpander.setVisibility(View.GONE);
            ibFlagged.setVisibility(View.GONE);
            ibAvatar.setVisibility(View.GONE);
            ibAuth.setVisibility(View.GONE);
            ivPriorityHigh.setVisibility(View.GONE);
            ivPriorityLow.setVisibility(View.GONE);
            ivSigned.setVisibility(View.GONE);
            ivEncrypted.setVisibility(View.GONE);
            tvFrom.setText(null);
            tvSize.setText(null);
            tvTime.setText(null);
            ivType.setVisibility(View.GONE);
            ibSnoozed.setVisibility(View.GONE);
            ivAnswered.setVisibility(View.GONE);
            ivAttachments.setVisibility(View.GONE);
            tvSubject.setText(null);
            tvFolder.setText(null);
            tvCount.setText(null);
            ivThread.setVisibility(View.GONE);
            tvExpand.setVisibility(View.GONE);
            tvPreview.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            ibHelp.setVisibility(View.GONE);

            clearExpanded(null);
        }

        @SuppressLint("WrongConstant")
        private void bindTo(final TupleMessageEx message, int position) {
            boolean inbox = EntityFolder.INBOX.equals(message.folderType);
            boolean outbox = EntityFolder.OUTBOX.equals(message.folderType);
            boolean outgoing = isOutgoing(message);
            Address[] addresses = (outgoing && (viewType != ViewType.THREAD || !threading) ? message.to : message.senders);
            boolean authenticated =
                    !(Boolean.FALSE.equals(message.dkim) ||
                            Boolean.FALSE.equals(message.spf) ||
                            Boolean.FALSE.equals(message.dmarc) ||
                            Boolean.FALSE.equals(message.mx));
            boolean expanded = (viewType == ViewType.THREAD && properties.getValue("expanded", message.id));

            // Text size
            if (textSize != 0) {
                float fz_sender = (font_size_sender == null ? textSize : font_size_sender) * (message.unseen > 0 ? 1.1f : 1f);
                float fz_subject = (font_size_subject == null ? textSize : font_size_subject) * 0.9f;
                tvFrom.setTextSize(TypedValue.COMPLEX_UNIT_PX, fz_sender);
                tvSubject.setTextSize(TypedValue.COMPLEX_UNIT_PX, fz_subject);
                tvFolder.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);
                tvPreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);

                int px = Math.round(fz_sender + fz_subject + (compact ? 0 : textSize * 0.9f));
                ViewGroup.LayoutParams lparams = ibAvatar.getLayoutParams();
                if (lparams.height != px) {
                    lparams.width = px;
                    lparams.height = px;
                    ibAvatar.requestLayout();
                }
            }

            // Selected / disabled
            view.setActivated(selectionTracker != null && selectionTracker.isSelected(message.id));
            view.setAlpha(
                    (EntityFolder.OUTBOX.equals(message.folderType)
                            ? message.identitySynchronize == null || !message.identitySynchronize
                            : message.uid == null && message.accountProtocol == EntityAccount.TYPE_IMAP)
                            ? Helper.LOW_LIGHT : 1.0f);

            // Duplicate
            if (viewType == ViewType.THREAD) {
                boolean dim = (message.duplicate || EntityFolder.TRASH.equals(message.folderType));
                ibFlagged.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibAvatar.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibAuth.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivPriorityHigh.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivPriorityLow.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivSigned.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivEncrypted.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvFrom.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvSize.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvTime.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivType.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibSnoozed.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivAnswered.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivAttachments.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvSubject.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvFolder.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvCount.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivThread.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvPreview.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvError.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
            }

            // Unseen
            Typeface typeface = (message.unseen > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tvFrom.setTypeface(typeface);
            tvSize.setTypeface(typeface);
            tvTime.setTypeface(typeface);
            if (subject_italic)
                if (message.unseen > 0)
                    tvSubject.setTypeface(null, Typeface.BOLD_ITALIC);
                else
                    tvSubject.setTypeface(null, Typeface.ITALIC);
            else
                tvSubject.setTypeface(typeface);
            tvCount.setTypeface(typeface);

            int colorUnseen = (message.unseen > 0 ? colorUnread : colorRead);
            if (tvFrom.getTag() == null || (int) tvFrom.getTag() != colorUnseen) {
                tvFrom.setTag(colorUnseen);
                tvFrom.setTextColor(colorUnseen);
                tvSize.setTextColor(colorUnseen);
                tvTime.setTextColor(colorUnseen);
            }

            // Account color
            int colorBackground =
                    (message.accountColor == null || !ActivityBilling.isPro(context)
                            ? colorSeparator : message.accountColor);
            if (vwColor.getTag() == null || (int) vwColor.getTag() != colorBackground) {
                vwColor.setTag(colorBackground);
                vwColor.setBackgroundColor(colorBackground);
            }

            // Expander
            if (ibExpander.getTag() == null || (boolean) ibExpander.getTag() != expanded) {
                ibExpander.setTag(expanded);
                ibExpander.setImageLevel(expanded ? 0 /* less */ : 1 /* more */);
            }
            if (viewType == ViewType.THREAD)
                ibExpander.setVisibility(EntityFolder.DRAFTS.equals(message.folderType) ? View.INVISIBLE : View.VISIBLE);
            else
                ibExpander.setVisibility(View.GONE);

            // Photo
            ibAvatar.setVisibility(avatars ? View.INVISIBLE : View.GONE);

            // Line 1
            ibAuth.setVisibility(authentication && !authenticated ? View.VISIBLE : View.GONE);
            ivPriorityHigh.setVisibility(EntityMessage.PRIORITIY_HIGH.equals(message.priority) ? View.VISIBLE : View.GONE);
            ivPriorityLow.setVisibility(EntityMessage.PRIORITIY_LOW.equals(message.priority) ? View.VISIBLE : View.GONE);
            ivSigned.setVisibility(message.signed > 0 ? View.VISIBLE : View.GONE);
            ivEncrypted.setVisibility(message.encrypted > 0 ? View.VISIBLE : View.GONE);
            tvFrom.setText(MessageHelper.formatAddresses(addresses, name_email, false));
            tvFrom.setPaintFlags(tvFrom.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            tvSize.setText(message.totalSize == null ? null : Helper.humanReadableByteCount(message.totalSize, true));
            tvSize.setVisibility(message.totalSize != null && "size".equals(sort) ? View.VISIBLE : View.GONE);
            tvTime.setText(date && "time".equals(sort)
                    ? TF.format(message.received)
                    : Helper.getRelativeTimeSpanString(context, message.received));

            // Line 2
            tvSubject.setText(message.subject);

            // Line 3
            int icon = (message.drafts > 0
                    ? R.drawable.baseline_edit_24
                    : EntityFolder.getIcon(outgoing ? EntityFolder.SENT : message.folderType));
            ivType.setVisibility(message.drafts > 0 ||
                    (viewType == ViewType.UNIFIED && type == null && (!inbox || outgoing)) ||
                    (viewType == ViewType.FOLDER && outgoing && !EntityFolder.SENT.equals(message.folderType)) ||
                    (viewType == ViewType.THREAD && (outgoing || EntityFolder.SENT.equals(message.folderType))) ||
                    viewType == ViewType.SEARCH
                    ? View.VISIBLE : View.GONE);
            if (ivType.getTag() == null || (int) ivType.getTag() != icon) {
                ivType.setTag(icon);
                ivType.setImageResource(icon);
            }

            ibSnoozed.setImageResource(
                    message.ui_snoozed != null && message.ui_snoozed == Long.MAX_VALUE
                            ? R.drawable.baseline_visibility_off_24 : R.drawable.baseline_timelapse_24);

            ibSnoozed.setVisibility(message.ui_snoozed == null ? View.GONE : View.VISIBLE);
            ivAnswered.setVisibility(message.ui_answered ? View.VISIBLE : View.GONE);
            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.FOLDER)
                tvFolder.setText(outbox ? message.identityEmail : message.accountName);
            else if (viewType == ViewType.THREAD || viewType == ViewType.SEARCH)
                tvFolder.setText(message.getFolderName(context));
            else
                tvFolder.setText(message.accountName + "/" + message.getFolderName(context));

            tvFolder.setVisibility(compact && viewType != ViewType.THREAD ? View.GONE : View.VISIBLE);

            if (viewType == ViewType.THREAD || !threading) {
                tvCount.setVisibility(View.GONE);
                ivThread.setVisibility(View.GONE);
            } else {
                tvCount.setText(NF.format(message.visible));
                ivThread.setVisibility(View.VISIBLE);
            }

            // Starred
            bindFlagged(message, expanded);

            // Expand warning
            bindExpandWarning(message, expanded);

            // Message text preview
            int textColor = (contrast ? textColorPrimary : textColorSecondary);
            if (tvPreview.getTag() == null || (int) tvPreview.getTag() != textColor) {
                tvPreview.setTag(textColor);
                tvPreview.setTextColor(textColor);
                tvPreview.setMaxLines(preview_lines);
            }
            tvPreview.setTypeface(
                    monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT,
                    preview_italic ? Typeface.ITALIC : Typeface.NORMAL);
            tvPreview.setText(message.preview);
            tvPreview.setVisibility(preview && !TextUtils.isEmpty(message.preview) ? View.VISIBLE : View.GONE);

            // Error / warning
            String error = message.error;
            if (message.warning != null)
                if (error == null)
                    error = message.warning;
                else
                    error += " " + message.warning;

            if (debug) {
                String text = "error=" + error +
                        "\nuid=" + message.uid + " id=" + message.id + " " + DTF.format(new Date(message.received)) +
                        "\n" + (message.ui_hide ? "HIDDEN " : "") +
                        "seen=" + message.seen + "/" + message.ui_seen +
                        " unseen=" + message.unseen +
                        " ignored=" + message.ui_ignored +
                        " found=" + message.ui_found +
                        "\nmsgid=" + message.msgid +
                        "\nthread=" + message.thread +
                        "\nsender=" + message.sender;

                tvError.setText(text);
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setText(error);
                tvError.setVisibility(error == null ? View.GONE : View.VISIBLE);
                ibHelp.setVisibility(error == null ? View.GONE : View.VISIBLE);
            }

            // Contact info
            ContactInfo info = ContactInfo.get(context, message.account, addresses, true);
            if (info == null) {
                Bundle aargs = new Bundle();
                aargs.putLong("id", message.id);
                aargs.putLong("account", message.account);
                aargs.putSerializable("addresses", addresses);

                new SimpleTask<ContactInfo>() {
                    @Override
                    protected ContactInfo onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");
                        Address[] addresses = (Address[]) args.getSerializable("addresses");

                        return ContactInfo.get(context, account, addresses, false);
                    }

                    @Override
                    protected void onExecuted(Bundle args, ContactInfo info) {
                        long id = args.getLong("id");
                        TupleMessageEx amessage = getMessage();
                        if (amessage == null || !amessage.id.equals(id))
                            return;

                        bindContactInfo(info, addresses, name_email);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.setLog(false).execute(context, owner, aargs, "message:avatar");
            } else
                bindContactInfo(info, addresses, name_email);

            if (viewType == ViewType.THREAD)
                if (expanded)
                    bindExpanded(message, false);
                else
                    clearExpanded(message);
        }

        private void clearExpanded(TupleMessageEx message) {
            if (compact) {
                tvFrom.setSingleLine(true);
                tvSubject.setSingleLine(true);
            }

            tvPreview.setVisibility(
                    preview && message != null && !TextUtils.isEmpty(message.preview)
                            ? View.VISIBLE : View.GONE);

            if (vsBody == null)
                return;

            cowner.stop();

            grpAddresses.setVisibility(View.GONE);
            grpHeaders.setVisibility(View.GONE);
            grpCalendar.setVisibility(View.GONE);
            grpCalendarResponse.setVisibility(View.GONE);
            grpAttachments.setVisibility(View.GONE);
            grpImages.setVisibility(View.GONE);

            ivPlain.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.GONE);
            ivBrowsed.setVisibility(View.GONE);

            ibSearchContact.setVisibility(View.GONE);
            ibNotifyContact.setVisibility(View.GONE);
            ibAddContact.setVisibility(View.GONE);

            tvFromExTitle.setVisibility(View.GONE);
            tvToTitle.setVisibility(View.GONE);
            tvReplyToTitle.setVisibility(View.GONE);
            tvCcTitle.setVisibility(View.GONE);
            tvBccTitle.setVisibility(View.GONE);
            tvIdentityTitle.setVisibility(View.GONE);
            tvSentTitle.setVisibility(View.GONE);
            tvReceivedTitle.setVisibility(View.GONE);
            tvSizeExTitle.setVisibility(View.GONE);

            tvFromEx.setVisibility(View.GONE);
            tvTo.setVisibility(View.GONE);
            tvReplyTo.setVisibility(View.GONE);
            tvCc.setVisibility(View.GONE);
            tvBcc.setVisibility(View.GONE);
            tvIdentity.setVisibility(View.GONE);
            tvSent.setVisibility(View.GONE);
            tvReceived.setVisibility(View.GONE);
            tvSizeEx.setVisibility(View.GONE);
            tvSubjectEx.setVisibility(View.GONE);
            tvFlags.setVisibility(View.GONE);
            tvKeywords.setVisibility(View.GONE);

            pbHeaders.setVisibility(View.GONE);
            tvNoInternetHeaders.setVisibility(View.GONE);

            tvCalendarSummary.setVisibility(View.GONE);
            tvCalendarStart.setVisibility(View.GONE);
            tvCalendarEnd.setVisibility(View.GONE);
            tvAttendees.setVisibility(View.GONE);
            pbCalendarWait.setVisibility(View.GONE);

            cbInline.setVisibility(View.GONE);
            btnSaveAttachments.setVisibility(View.GONE);
            btnDownloadAttachments.setVisibility(View.GONE);
            tvNoInternetAttachments.setVisibility(View.GONE);

            bnvActions.setVisibility(View.GONE);
            grpActions.setVisibility(View.GONE);

            ibFull.setVisibility(View.GONE);
            ibImages.setVisibility(View.GONE);
            ibUnsubscribe.setVisibility(View.GONE);
            ibVerify.setVisibility(View.GONE);
            ibDecrypt.setVisibility(View.GONE);

            tvBody.setVisibility(View.GONE);
            wvBody.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            tvNoInternetBody.setVisibility(View.GONE);
            grpDownloading.setVisibility(View.GONE);
        }

        private void bindFlagged(TupleMessageEx message, boolean expanded) {
            boolean pro = ActivityBilling.isPro(context);
            int flagged = (message.count - message.unflagged);
            int color = (message.color == null || !pro ? colorAccent : message.color);

            ibFlagged.setImageResource(flagged > 0 ? R.drawable.baseline_star_24 : R.drawable.baseline_star_border_24);
            ibFlagged.setImageTintList(ColorStateList.valueOf(flagged > 0 ? color : textColorSecondary));
            ibFlagged.setEnabled(message.uid != null || message.accountProtocol != EntityAccount.TYPE_IMAP);

            card.setCardBackgroundColor(
                    flags_background && flagged > 0 && !expanded
                            ? ColorUtils.setAlphaComponent(color, 127) : Color.TRANSPARENT);

            if (flags)
                ibFlagged.setVisibility(message.folderReadOnly ? View.INVISIBLE : View.VISIBLE);
            else
                ibFlagged.setVisibility(View.GONE);
        }

        private void bindContactInfo(ContactInfo info, Address[] addresses, boolean name_email) {
            if (info.hasPhoto()) {
                ibAvatar.setImageBitmap(info.getPhotoBitmap());
                ibAvatar.setVisibility(View.VISIBLE);
            } else
                ibAvatar.setVisibility(View.GONE);

            Uri lookupUri = info.getLookupUri();
            ibAvatar.setTag(lookupUri);
            ibAvatar.setEnabled(lookupUri != null);

            String displayName = info.getDisplayName();
            if (!TextUtils.isEmpty(displayName) &&
                    addresses != null && addresses.length == 1) {
                String email = ((InternetAddress) addresses[0]).getAddress();
                String personal = ((InternetAddress) addresses[0]).getPersonal();
                if (TextUtils.isEmpty(personal))
                    try {
                        InternetAddress a = new InternetAddress(email, displayName, StandardCharsets.UTF_8.name());
                        tvFrom.setText(MessageHelper.formatAddresses(new Address[]{a}, name_email, false));
                    } catch (UnsupportedEncodingException ex) {
                        Log.w(ex);
                    }
            }

            if (distinguish_contacts && info.isKnown())
                tvFrom.setPaintFlags(tvFrom.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        private void bindExpandWarning(TupleMessageEx message, boolean expanded) {
            if (viewType != ViewType.THREAD || expanded || message.content || message.uid == null || unmetered)
                tvExpand.setVisibility(View.GONE);
            else {
                tvExpand.setText(context.getString(R.string.title_expand_warning,
                        message.size == null ? "?" : Helper.humanReadableByteCount(message.size, true)));
                tvExpand.setVisibility(View.VISIBLE);
            }
        }

        private void bindExpanded(final TupleMessageEx message, final boolean scroll) {
            DB db = DB.getInstance(context);

            boolean show_addresses = !properties.getValue("addresses", message.id);
            boolean show_headers = properties.getValue("headers", message.id);

            if (compact) {
                tvFrom.setSingleLine(false);
                tvSubject.setSingleLine(false);
            }

            tvPreview.setVisibility(View.GONE);

            ensureExpanded();

            grpAddresses.setVisibility(View.VISIBLE);

            boolean hasFrom = (message.from != null && message.from.length > 0);
            boolean hasTo = (message.to != null && message.to.length > 0);
            boolean hasChannel = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);

            ivPlain.setVisibility(show_addresses && message.plain_only != null && message.plain_only ? View.VISIBLE : View.GONE);
            ivReceipt.setVisibility(show_addresses && message.receipt_request != null && message.receipt_request ? View.VISIBLE : View.GONE);
            ivBrowsed.setVisibility(show_addresses && message.ui_browsed ? View.VISIBLE : View.GONE);

            ibSearchContact.setVisibility(show_addresses && (hasFrom || hasTo) ? View.VISIBLE : View.GONE);
            ibNotifyContact.setVisibility(show_addresses && hasChannel && hasFrom ? View.VISIBLE : View.GONE);
            ibAddContact.setVisibility(show_addresses && contacts && hasFrom ? View.VISIBLE : View.GONE);

            grpHeaders.setVisibility(show_headers ? View.VISIBLE : View.GONE);
            if (show_headers && message.headers == null) {
                pbHeaders.setVisibility(suitable ? View.VISIBLE : View.GONE);
                tvNoInternetHeaders.setVisibility(suitable ? View.GONE : View.VISIBLE);
            } else {
                pbHeaders.setVisibility(View.GONE);
                tvNoInternetHeaders.setVisibility(View.GONE);
            }

            grpActions.setVisibility(View.VISIBLE);
            for (int i = 0; i < bnvActions.getMenu().size(); i++)
                bnvActions.getMenu().getItem(i).setVisible(false);

            ibFull.setEnabled(false);
            ibFull.setVisibility(View.VISIBLE);
            ibImages.setVisibility(View.GONE);
            ibUnsubscribe.setVisibility(message.unsubscribe == null ? View.GONE : View.VISIBLE);
            ibDecrypt.setVisibility(View.GONE);
            ibVerify.setVisibility(View.GONE);

            // Addresses
            ibExpanderAddress.setImageLevel(show_addresses ? 0 /* less */ : 1 /* more */);

            String from = MessageHelper.formatAddresses(message.senders);
            String to = MessageHelper.formatAddresses(message.to);
            String replyto = MessageHelper.formatAddresses(message.reply);
            String cc = MessageHelper.formatAddresses(message.cc);
            String bcc = MessageHelper.formatAddresses(message.bcc);

            tvFromExTitle.setVisibility(show_addresses && !TextUtils.isEmpty(from) ? View.VISIBLE : View.GONE);
            tvFromEx.setVisibility(show_addresses && !TextUtils.isEmpty(from) ? View.VISIBLE : View.GONE);
            tvFromEx.setText(from);

            tvToTitle.setVisibility(show_addresses && !TextUtils.isEmpty(to) ? View.VISIBLE : View.GONE);
            tvTo.setVisibility(show_addresses && !TextUtils.isEmpty(to) ? View.VISIBLE : View.GONE);
            tvTo.setText(to);

            tvReplyToTitle.setVisibility(show_addresses && !TextUtils.isEmpty(replyto) ? View.VISIBLE : View.GONE);
            tvReplyTo.setVisibility(show_addresses && !TextUtils.isEmpty(replyto) ? View.VISIBLE : View.GONE);
            tvReplyTo.setText(replyto);

            tvCcTitle.setVisibility(show_addresses && !TextUtils.isEmpty(cc) ? View.VISIBLE : View.GONE);
            tvCc.setVisibility(show_addresses && !TextUtils.isEmpty(cc) ? View.VISIBLE : View.GONE);
            tvCc.setText(cc);

            tvBccTitle.setVisibility(show_addresses && !TextUtils.isEmpty(bcc) ? View.VISIBLE : View.GONE);
            tvBcc.setVisibility(show_addresses && !TextUtils.isEmpty(bcc) ? View.VISIBLE : View.GONE);
            tvBcc.setText(bcc);

            InternetAddress via = null;
            if (message.identityEmail != null)
                try {
                    via = new InternetAddress(message.identityEmail, message.identityName);
                } catch (UnsupportedEncodingException ignored) {
                }

            tvIdentityTitle.setVisibility(show_addresses && via != null ? View.VISIBLE : View.GONE);
            tvIdentity.setVisibility(show_addresses && via != null ? View.VISIBLE : View.GONE);
            tvIdentity.setText(via == null ? null : MessageHelper.formatAddresses(new Address[]{via}));

            tvSentTitle.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvSent.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvSent.setText(message.sent == null ? null : DTF.format(message.sent));

            tvReceivedTitle.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvReceived.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvReceived.setText(DTF.format(message.received));

            if (!message.duplicate)
                tvSizeEx.setAlpha(message.content ? 1.0f : Helper.LOW_LIGHT);
            tvSizeExTitle.setVisibility(!show_addresses || message.size == null ? View.GONE : View.VISIBLE);
            tvSizeEx.setVisibility(!show_addresses || (message.size == null && message.total == null) ? View.GONE : View.VISIBLE);
            StringBuilder size = new StringBuilder();
            size
                    .append(message.size == null ? "-" : Helper.humanReadableByteCount(message.size, true))
                    .append("/")
                    .append(message.total == null ? "-" : Helper.humanReadableByteCount(message.total, true));
            tvSizeEx.setText(size.toString());

            tvSubjectEx.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvSubjectEx.setText(message.subject);
            if (subject_italic)
                tvSubjectEx.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            else
                tvSubjectEx.setTypeface(Typeface.DEFAULT);

            // Flags
            tvFlags.setVisibility(show_addresses && debug ? View.VISIBLE : View.GONE);
            tvFlags.setText(message.flags);

            // Keywords
            tvKeywords.setVisibility(show_addresses && message.keywords.length > 0 ? View.VISIBLE : View.GONE);
            tvKeywords.setText(TextUtils.join(" ", message.keywords));

            // Headers
            if (show_headers && message.headers != null)
                tvHeaders.setText(HtmlHelper.highlightHeaders(context, message.headers));
            else
                tvHeaders.setText(null);

            // Attachments
            bindAttachments(message, properties.getAttachments(message.id));

            // Setup actions
            Bundle sargs = new Bundle();
            sargs.putLong("id", message.id);
            sargs.putLong("account", message.account);

            new SimpleTask<List<EntityFolder>>() {
                @Override
                protected List<EntityFolder> onExecute(Context context, Bundle args) {
                    long account = args.getLong("account");
                    return DB.getInstance(context).folder().getSystemFolders(account);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityFolder> folders) {
                    long id = args.getLong("id");
                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(id))
                        return;

                    boolean hasArchive = false;
                    boolean hasTrash = false;
                    hasJunk = false;
                    if (folders != null)
                        for (EntityFolder folder : folders) {
                            if (EntityFolder.ARCHIVE.equals(folder.type))
                                hasArchive = true;
                            else if (EntityFolder.TRASH.equals(folder.type))
                                hasTrash = true;
                            else if (EntityFolder.JUNK.equals(folder.type))
                                hasJunk = true;
                        }

                    boolean inOutbox = EntityFolder.OUTBOX.equals(message.folderType);
                    boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                    boolean inTrash = EntityFolder.TRASH.equals(message.folderType);
                    boolean inJunk = EntityFolder.JUNK.equals(message.folderType);

                    delete = (inTrash || !hasTrash || inOutbox);

                    bnvActions.getMenu().findItem(R.id.action_more).setVisible(!inOutbox);

                    if (!message.folderReadOnly) {
                        bnvActions.getMenu().findItem(R.id.action_delete).setVisible(
                                (delete ? message.uid != null || !TextUtils.isEmpty(message.msgid) : message.uid != null));
                        bnvActions.getMenu().findItem(R.id.action_delete).setTitle(
                                delete ? R.string.title_delete : R.string.title_trash);

                        bnvActions.getMenu().findItem(R.id.action_move).setVisible(
                                message.uid != null || inOutbox);
                        bnvActions.getMenu().findItem(R.id.action_move).setTitle(
                                inOutbox ? R.string.title_folder_drafts : R.string.title_move);
                        bnvActions.getMenu().findItem(R.id.action_move).setIcon(
                                inOutbox ? R.drawable.baseline_drafts_24 : R.drawable.baseline_folder_24);

                        bnvActions.getMenu().findItem(R.id.action_archive).setVisible(
                                message.uid != null && (inJunk || (!inArchive && hasArchive)));
                        bnvActions.getMenu().findItem(R.id.action_archive).setTitle(
                                inJunk ? R.string.title_folder_inbox : R.string.title_archive);
                        bnvActions.getMenu().findItem(R.id.action_archive).setIcon(
                                inJunk ? R.drawable.baseline_inbox_24 : R.drawable.baseline_archive_24);
                    }

                    bnvActions.getMenu().findItem(R.id.action_reply).setEnabled(message.content);
                    bnvActions.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.setLog(false).execute(context, owner, sargs, "message:actions");

            // Message text
            pbBody.setVisibility(suitable || message.content ? View.VISIBLE : View.GONE);
            tvNoInternetBody.setVisibility(suitable || message.content ? View.GONE : View.VISIBLE);

            cowner.recreate();
            bindBody(message);

            db.attachment().liveAttachments(message.id).observe(cowner, new Observer<List<EntityAttachment>>() {
                private int lastInlineImages = 0;

                @Override
                public void onChanged(@Nullable List<EntityAttachment> attachments) {
                    bindAttachments(message, attachments);

                    int inlineImages = 0;
                    if (attachments != null)
                        for (EntityAttachment attachment : attachments)
                            if (attachment.available && attachment.isInline() && attachment.isImage())
                                inlineImages++;

                    if (inlineImages != lastInlineImages) {
                        lastInlineImages = inlineImages;
                        bindBody(message);
                    }

                    if (scroll)
                        properties.scrollTo(getAdapterPosition());
                }
            });
        }

        private void bindBody(TupleMessageEx message) {
            tvBody.setText(null);
            grpDownloading.setVisibility(message.content ? View.GONE : View.VISIBLE);

            if (!message.content)
                return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (message.from != null)
                for (Address sender : message.from) {
                    String from = ((InternetAddress) sender).getAddress();
                    if (prefs.getBoolean(from + ".show_full", false)) {
                        properties.setValue("full", message.id, true);
                        properties.setValue("full_asked", message.id, true);
                    }
                    if (prefs.getBoolean(from + ".show_images", false)) {
                        properties.setValue("images", message.id, true);
                        properties.setValue("images_asked", message.id, true);
                    }
                }

            boolean show_full = properties.getValue("full", message.id);
            boolean show_images = properties.getValue("images", message.id);
            boolean show_quotes = (properties.getValue("quotes", message.id) || !collapse_quotes);

            boolean always_images = prefs.getBoolean("html_always_images", false);
            if (always_images && show_full) {
                show_images = true;
                properties.setValue("images", message.id, true);
            }

            float size = properties.getSize(message.id, show_full ? 0 : textSize);
            int height = properties.getHeight(message.id, 0);
            Pair<Integer, Integer> position = properties.getPosition(message.id);
            Log.i("Bind size=" + size + " height=" + height);

            ibFull.setEnabled(hasWebView);
            ibFull.setImageResource(show_full ? R.drawable.baseline_fullscreen_exit_24 : R.drawable.baseline_fullscreen_24);
            ibImages.setImageResource(show_images ? R.drawable.baseline_format_align_justify_24 : R.drawable.baseline_image_24);

            if (show_full) {
                // Create web view
                WebViewEx webView;
                if (wvBody instanceof WebView)
                    webView = (WebViewEx) wvBody;
                else {
                    try {
                        webView = new WebViewEx(context);
                    } catch (Throwable ex) {
                        /*
                            android.util.AndroidRuntimeException: java.lang.reflect.InvocationTargetException
                                    at android.webkit.WebViewFactory.getProvider(WebViewFactory.java:270)
                                    at android.webkit.WebView.getFactory(WebView.java:2681)
                                    at android.webkit.WebView.ensureProviderCreated(WebView.java:2676)
                                    at android.webkit.WebView.setOverScrollMode(WebView.java:2741)
                                    at android.view.View.<init>(View.java:4815)
                                    at android.view.View.<init>(View.java:4956)
                                    at android.view.ViewGroup.<init>(ViewGroup.java:659)
                                    at android.widget.AbsoluteLayout.<init>(AbsoluteLayout.java:55)
                                    at android.webkit.WebView.<init>(WebView.java:659)
                                    at android.webkit.WebView.<init>(WebView.java:604)
                                    at android.webkit.WebView.<init>(WebView.java:587)
                                    at android.webkit.WebView.<init>(WebView.java:574)
                                    at android.webkit.WebView.<init>(WebView.java:564)
                         */
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        return;
                    }

                    webView.setId(wvBody.getId());

                    ConstraintLayout cl = (ConstraintLayout) vsBody;
                    cl.removeView(wvBody);
                    cl.addView(webView, wvBody.getLayoutParams());
                    cl.setPadding(
                            wvBody.getPaddingLeft(), wvBody.getPaddingTop(),
                            wvBody.getPaddingRight(), wvBody.getPaddingBottom());

                    wvBody = webView;
                }

                int dp60 = Helper.dp2pixels(context, 60);
                webView.setMinimumHeight(height == 0 ? dp60 : height);

                webView.init(
                        height, size, position,
                        textSize, monospaced,
                        show_images, inline,
                        new WebViewEx.IWebView() {
                            @Override
                            public void onSizeChanged(int w, int h, int ow, int oh) {
                                properties.setHeight(message.id, h);
                            }

                            @Override
                            public void onScaleChanged(float newScale) {
                                properties.setSize(message.id, newScale);
                            }

                            @Override
                            public void onScrollChange(int scrollX, int scrollY) {
                                properties.setPosition(message.id, new Pair<Integer, Integer>(scrollX, scrollY));
                            }

                            @Override
                            public boolean onOpenLink(String url) {
                                Uri uri = Uri.parse(url);
                                if ("cid".equals(uri.getScheme()) || "data".equals(uri.getScheme()))
                                    return false;

                                if (parentFragment == null)
                                    return false;

                                Bundle args = new Bundle();
                                args.putParcelable("uri", uri);
                                args.putString("title", null);

                                FragmentDialogLink fragment = new FragmentDialogLink();
                                fragment.setArguments(args);
                                fragment.show(parentFragment.getParentFragmentManager(), "open:link");

                                return true;
                            }
                        });
                webView.setOnTouchListener(ViewHolder.this);

                tvBody.setVisibility(View.GONE);
                wvBody.setVisibility(View.VISIBLE);
            } else {
                tvBody.setMinHeight(height);

                if (size != 0)
                    tvBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

                tvBody.setTextColor(contrast ? textColorPrimary : colorRead);
                tvBody.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);

                tvBody.setVisibility(View.VISIBLE);
                wvBody.setVisibility(View.GONE);
            }

            final Bundle args = new Bundle();
            args.putSerializable("message", message);
            args.putBoolean("show_full", show_full);
            args.putBoolean("show_images", show_images);
            args.putBoolean("show_quotes", show_quotes);
            args.putInt("zoom", zoom);

            new SimpleTask<Object>() {
                @Override
                protected Object onExecute(final Context context, final Bundle args) throws IOException {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                    final boolean show_full = args.getBoolean("show_full");
                    final boolean show_images = args.getBoolean("show_images");
                    final boolean show_quotes = args.getBoolean("show_quotes");
                    final int zoom = args.getInt("zoom");

                    if (message == null || !message.content)
                        return null;

                    File file = message.getFile(context);
                    if (!file.exists())
                        return null;

                    String body = Helper.readText(file);
                    Document document = JsoupEx.parse(body);

                    // Check for inline encryption
                    int begin = body.indexOf(Helper.PGP_BEGIN_MESSAGE);
                    int end = body.indexOf(Helper.PGP_END_MESSAGE);
                    args.putBoolean("inline_encrypted", begin >= 0 && begin < end);

                    // Check for images
                    boolean has_images = false;
                    for (Element img : document.select("img")) {
                        if (inline) {
                            String src = img.attr("src");
                            if (!src.startsWith("cid:")) {
                                has_images = true;
                                break;
                            }
                        } else {
                            has_images = true;
                            break;
                        }
                    }
                    args.putBoolean("has_images", has_images);

                    // Download inline images
                    if (show_images) {
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                            for (EntityAttachment attachment : attachments)
                                if (attachment.isInline() && attachment.isImage() &&
                                        attachment.progress == null && !attachment.available)
                                    EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.id);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                    }

                    // Format message
                    if (show_full) {
                        HtmlHelper.setViewport(document);
                        if (inline || show_images)
                            HtmlHelper.embedInlineImages(context, message.id, document);

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean disable_tracking = prefs.getBoolean("disable_tracking", true);
                        if (disable_tracking)
                            HtmlHelper.removeTrackingPixels(context, document);

                        if (debug) {
                            Document format = JsoupEx.parse(document.html());
                            format.outputSettings().prettyPrint(true).outline(true).indentAmount(1);
                            Element pre = document.createElement("pre");
                            pre.text(format.html());
                            document.body().appendChild(pre);
                        }

                        return document.html();
                    } else {
                        // Cleanup message
                        document = HtmlHelper.sanitize(context, body, show_images, true);

                        // Collapse quotes
                        if (!show_quotes) {
                            for (Element quote : document.select("blockquote"))
                                quote.html("&#8230;");
                        }

                        // Add debug info
                        if (debug) {
                            document.outputSettings().prettyPrint(true).outline(true).indentAmount(1);
                            String[] lines = document.html().split("\\r?\\n");
                            for (int i = 0; i < lines.length; i++)
                                lines[i] = Html.escapeHtml(lines[i]);
                            Element pre = document.createElement("pre");
                            pre.html(TextUtils.join("<br>", lines));
                            document.appendChild(pre);
                        }

                        // Draw images
                        Spanned spanned = HtmlHelper.fromHtml(document.html(), new Html.ImageGetter() {
                            @Override
                            public Drawable getDrawable(String source) {
                                Drawable drawable = ImageHelper.decodeImage(context, message.id, source, show_images, zoom, tvBody);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    if (drawable instanceof AnimatedImageDrawable)
                                        ((AnimatedImageDrawable) drawable).start();
                                }

                                return drawable;
                            }
                        }, null);

                        // Replace quote spans
                        final int px = Helper.dp2pixels(context, 24 + (zoom) * 8);
                        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
                        QuoteSpan[] quoteSpans = builder.getSpans(0, builder.length(), QuoteSpan.class);
                        for (QuoteSpan quoteSpan : quoteSpans) {
                            int s = builder.getSpanStart(quoteSpan);
                            int e = builder.getSpanEnd(quoteSpan);

                            builder.removeSpan(quoteSpan);

                            StyledQuoteSpan squote = new StyledQuoteSpan(context, colorPrimary);
                            builder.setSpan(squote, s, e, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                            if (!show_quotes)
                                builder.setSpan(
                                        new DynamicDrawableSpan() {
                                            @Override
                                            public Drawable getDrawable() {
                                                Drawable d = context.getDrawable(R.drawable.baseline_format_quote_24);
                                                d.setTint(colorAccent);
                                                d.setBounds(0, 0, px, px);
                                                return d;
                                            }
                                        },
                                        s, e, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        return builder;
                    }
                }

                @Override
                protected void onExecuted(Bundle args, Object result) {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(message.id))
                        return;

                    boolean show_expanded = properties.getValue("expanded", message.id);
                    if (!show_expanded)
                        return;

                    boolean has_images = args.getBoolean("has_images");

                    if (result instanceof Spanned) {
                        tvBody.setText((Spanned) result);
                        tvBody.setTextIsSelectable(false);
                        tvBody.setTextIsSelectable(true);
                        tvBody.setMovementMethod(new TouchHandler(message));
                    } else if (result instanceof String)
                        ((WebView) wvBody).loadDataWithBaseURL(null, (String) result, "text/html", StandardCharsets.UTF_8.name(), null);
                    else if (result == null) {
                        boolean show_full = args.getBoolean("show_full");
                        if (show_full)
                            ((WebView) wvBody).loadDataWithBaseURL(null, "", "text/html", StandardCharsets.UTF_8.name(), null);
                        else
                            tvBody.setText(null);
                    } else
                        throw new IllegalStateException("Result=" + result);

                    pbBody.setVisibility(View.GONE);

                    // Show attachments
                    cowner.start();

                    // Show encrypt actions
                    ibVerify.setVisibility(false ||
                            EntityMessage.PGP_SIGNONLY.equals(message.encrypt) ||
                            EntityMessage.SMIME_SIGNONLY.equals(message.encrypt)
                            ? View.VISIBLE : View.GONE);
                    ibDecrypt.setVisibility(args.getBoolean("inline_encrypted") ||
                            EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) ||
                            EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt)
                            ? View.VISIBLE : View.GONE);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean auto_decrypt = prefs.getBoolean("auto_decrypt", false);
                    if (auto_decrypt &&
                            (EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) ||
                                    EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt)))
                        onActionDecrypt(message, true);

                    boolean show_full = properties.getValue("full", message.id);
                    boolean always_images = prefs.getBoolean("html_always_images", false);

                    // Show images
                    ibImages.setVisibility(has_images && !(show_full && always_images) ? View.VISIBLE : View.GONE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:body");
        }

        private void bindAttachments(final TupleMessageEx message, @Nullable List<EntityAttachment> attachments) {
            if (attachments == null)
                attachments = new ArrayList<>();
            properties.setAttachments(message.id, attachments);

            grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

            boolean show_inline = properties.getValue("inline", message.id);
            Log.i("Show inline=" + show_inline);

            boolean has_inline = false;
            boolean download = false;
            boolean save = (attachments.size() > 1);
            boolean downloading = false;
            boolean calendar = false;

            List<EntityAttachment> a = new ArrayList<>();
            for (EntityAttachment attachment : attachments) {
                boolean inline = ((attachment.isInline() && attachment.isImage()) || attachment.encryption != null);
                if (inline)
                    has_inline = true;
                if (attachment.progress == null && !attachment.available)
                    download = true;
                if (!attachment.available)
                    save = false;
                if (attachment.progress != null)
                    downloading = true;

                if (show_inline || !inline || !attachment.available)
                    a.add(attachment);

                if (attachment.available && "text/calendar".equals(attachment.type)) {
                    calendar = true;
                    bindCalendar(message, attachment);
                }
            }
            adapterAttachment.set(a);

            if (!calendar) {
                tvCalendarSummary.setVisibility(View.GONE);
                tvCalendarStart.setVisibility(View.GONE);
                tvCalendarEnd.setVisibility(View.GONE);
                tvAttendees.setVisibility(View.GONE);
                pbCalendarWait.setVisibility(View.GONE);
                grpCalendar.setVisibility(View.GONE);
                grpCalendarResponse.setVisibility(View.GONE);
            }

            cbInline.setOnCheckedChangeListener(null);
            cbInline.setChecked(show_inline);
            cbInline.setVisibility(has_inline ? View.VISIBLE : View.GONE);

            btnSaveAttachments.setVisibility(save ? View.VISIBLE : View.GONE);
            btnDownloadAttachments.setVisibility(download && suitable ? View.VISIBLE : View.GONE);
            tvNoInternetAttachments.setVisibility(downloading && !suitable ? View.VISIBLE : View.GONE);

            cbInline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    properties.setValue("inline", message.id, isChecked);
                    cowner.restart();
                    DB.getInstance(context).attachment().liveAttachments(message.id).observe(cowner, new Observer<List<EntityAttachment>>() {
                        @Override
                        public void onChanged(@Nullable List<EntityAttachment> attachments) {
                            bindAttachments(message, attachments);
                        }
                    });
                }
            });

            List<EntityAttachment> images = new ArrayList<>();
            for (EntityAttachment attachment : attachments)
                if (!attachment.isInline() && attachment.isImage())
                    images.add(attachment);
            adapterImage.set(images);
            grpImages.setVisibility(images.size() > 0 ? View.VISIBLE : View.GONE);
        }

        private void bindCalendar(final TupleMessageEx message, EntityAttachment attachment) {
            // https://tools.ietf.org/html/rfc5546

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putSerializable("file", attachment.getFile(context));

            new SimpleTask<ICalendar>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    grpCalendar.setVisibility(View.VISIBLE);
                    pbCalendarWait.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(Bundle args) {
                    pbCalendarWait.setVisibility(View.GONE);
                }

                @Override
                protected ICalendar onExecute(Context context, Bundle args) throws IOException {
                    File file = (File) args.getSerializable("file");
                    return Biweekly.parse(file).first();
                }

                @Override
                protected void onExecuted(Bundle args, ICalendar icalendar) {
                    long id = args.getLong("id");
                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(id))
                        return;

                    if (icalendar == null ||
                            icalendar.getMethod() == null ||
                            icalendar.getEvents().size() == 0) {
                        tvCalendarSummary.setVisibility(View.GONE);
                        tvCalendarStart.setVisibility(View.GONE);
                        tvCalendarEnd.setVisibility(View.GONE);
                        tvAttendees.setVisibility(View.GONE);
                        pbCalendarWait.setVisibility(View.GONE);
                        grpCalendar.setVisibility(View.GONE);
                        grpCalendarResponse.setVisibility(View.GONE);
                        return;
                    }

                    DateFormat DTF = Helper.getDateTimeInstance(context);

                    VEvent event = icalendar.getEvents().get(0);

                    String summary = event.getSummary() == null ? null : event.getSummary().getValue();

                    ICalDate start = event.getDateStart() == null ? null : event.getDateStart().getValue();
                    ICalDate end = event.getDateEnd() == null ? null : event.getDateEnd().getValue();

                    List<String> attendee = new ArrayList<>();
                    for (Attendee a : event.getAttendees()) {
                        String email = a.getEmail();
                        String name = a.getCommonName();
                        if (TextUtils.isEmpty(name)) {
                            if (!TextUtils.isEmpty(email))
                                attendee.add(email);
                        } else {
                            if (TextUtils.isEmpty(email) || name.equals(email))
                                attendee.add(name);
                            else
                                attendee.add(name + " (" + email + ")");
                        }
                    }

                    Organizer organizer = event.getOrganizer();

                    tvCalendarSummary.setText(summary);
                    tvCalendarSummary.setVisibility(summary == null ? View.GONE : View.VISIBLE);

                    tvCalendarStart.setText(start == null ? null : DTF.format(start.getTime()));
                    tvCalendarStart.setVisibility(start == null ? View.GONE : View.VISIBLE);

                    tvCalendarEnd.setText(end == null ? null : DTF.format(end.getTime()));
                    tvCalendarEnd.setVisibility(end == null ? View.GONE : View.VISIBLE);

                    tvAttendees.setText(TextUtils.join(", ", attendee));
                    tvAttendees.setVisibility(attendee.size() == 0 ? View.GONE : View.VISIBLE);

                    boolean canRespond =
                            (icalendar.getMethod().isRequest() &&
                                    organizer != null && organizer.getEmail() != null &&
                                    message.to != null && message.to.length > 0);
                    grpCalendarResponse.setVisibility(canRespond ? View.VISIBLE : View.GONE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.setLog(false).execute(context, owner, args, "message:calendar");
        }

        private void onActionCalendar(TupleMessageEx message, int action) {
            if (!ActivityBilling.isPro(context)) {
                context.startActivity(new Intent(context, ActivityBilling.class));
                return;
            }

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putInt("action", action);

            new SimpleTask<Object>() {
                @Override
                protected Object onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");
                    int action = args.getInt("action");

                    DB db = DB.getInstance(context);

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available && "text/calendar".equals(attachment.type)) {
                            File file = attachment.getFile(context);
                            ICalendar icalendar = Biweekly.parse(file).first();
                            VEvent event = icalendar.getEvents().get(0);

                            if (action == R.id.ibCalendar) {
                                String summary = event.getSummary() == null ? null : event.getSummary().getValue();

                                ICalDate start = event.getDateStart() == null ? null : event.getDateStart().getValue();
                                ICalDate end = event.getDateEnd() == null ? null : event.getDateEnd().getValue();

                                String location = event.getLocation() == null ? null : event.getLocation().getValue();

                                List<String> attendee = new ArrayList<>();
                                for (Attendee a : event.getAttendees()) {
                                    String email = a.getEmail();
                                    if (!TextUtils.isEmpty(email))
                                        attendee.add(email);
                                }

                                // https://developer.android.com/guide/topics/providers/calendar-provider.html#intent-insert
                                Intent intent = new Intent(Intent.ACTION_INSERT)
                                        .setData(CalendarContract.Events.CONTENT_URI)
                                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

                                if (summary != null)
                                    intent.putExtra(CalendarContract.Events.TITLE, summary);

                                if (start != null)
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.getTime());

                                if (end != null)
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTime());

                                if (location != null)
                                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);

                                if (attendee.size() > 0)
                                    intent.putExtra(Intent.EXTRA_EMAIL, TextUtils.join(",", attendee));

                                return intent;
                            }

                            // https://tools.ietf.org/html/rfc5546#section-4.2.2
                            VEvent ev = new VEvent();
                            ev.setOrganizer(event.getOrganizer());
                            ev.setUid(event.getUid());
                            if (event.getSequence() != null)
                                ev.setSequence(event.getSequence());
                            if (event.getDateStart() != null)
                                ev.setDateStart(event.getDateStart());
                            if (event.getDateEnd() != null)
                                ev.setDateEnd(event.getDateEnd());

                            InternetAddress to = (InternetAddress) message.to[0];
                            Attendee attendee = new Attendee(to.getPersonal(), to.getAddress());

                            switch (action) {
                                case R.id.btnCalendarAccept:
                                    attendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
                                    break;
                                case R.id.btnCalendarDecline:
                                    attendee.setParticipationStatus(ParticipationStatus.DECLINED);
                                    break;
                                case R.id.btnCalendarMaybe:
                                    attendee.setParticipationStatus(ParticipationStatus.TENTATIVE);
                                    break;
                            }

                            ev.addAttendee(attendee);

                            // https://icalendar.org/validator.html
                            ICalendar response = new ICalendar();
                            response.setMethod(Method.REPLY);
                            response.addEvent(ev);

                            File ics = File.createTempFile("calendar", ".ics", context.getCacheDir());
                            response.write(ics);

                            return ics;
                        }

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Object result) {
                    if (result instanceof File) {
                        String status = null;
                        switch (action) {
                            case R.id.btnCalendarAccept:
                                status = context.getString(R.string.title_icalendar_accept);
                                break;
                            case R.id.btnCalendarDecline:
                                status = context.getString(R.string.title_icalendar_decline);
                                break;
                            case R.id.btnCalendarMaybe:
                                status = context.getString(R.string.title_icalendar_maybe);
                                break;
                        }

                        Intent reply = new Intent(context, ActivityCompose.class)
                                .putExtra("action", "participation")
                                .putExtra("reference", args.getLong("id"))
                                .putExtra("ics", (File) result)
                                .putExtra("status", status);
                        context.startActivity(reply);
                    } else if (result instanceof Intent) {
                        context.startActivity((Intent) result);
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:participation");
        }

        private boolean isOutgoing(TupleMessageEx message) {
            if (EntityFolder.isOutgoing(message.folderType))
                return true;
            else
                return (message.identityEmail != null &&
                        message.from != null && message.from.length == 1 &&
                        message.identityEmail.equals(((InternetAddress) message.from[0]).getAddress()));
        }

        private TupleMessageEx getMessage() {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return null;

            return differ.getItem(pos);
        }

        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            if (ev.getPointerCount() > 1) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if (view.getId() == R.id.tvBody) {
                    gestureDetector.onTouchEvent(ev);
                    return true;
                } else
                    return false;
            } else {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            TupleMessageEx message = getMessage();
            if (message != null)
                properties.setHeight(message.id, bottom - top);
        }

        @Override
        public void onClick(View view) {
            final TupleMessageEx message = getMessage();
            if (message == null)
                return;

            if (view.getId() == R.id.ibAvatar)
                onViewContact(message);
            else if (view.getId() == R.id.ibAuth)
                onShowAuth(message);
            else if (view.getId() == R.id.ibSnoozed)
                onShowSnoozed(message);
            else if (view.getId() == R.id.ibFlagged)
                onToggleFlag(message);
            else if (view.getId() == R.id.ibHelp)
                onHelp(message);
            else if (view.getId() == R.id.ibSearchContact)
                onSearchContact(message);
            else if (view.getId() == R.id.ibNotifyContact)
                onNotifyContact(message);
            else if (view.getId() == R.id.ibAddContact)
                onAddContact(message);
            else if (viewType == ViewType.THREAD) {
                switch (view.getId()) {
                    case R.id.ibExpanderAddress:
                        onToggleAddresses(message);
                        break;

                    case R.id.btnSaveAttachments:
                        onSaveAttachments(message);
                        break;
                    case R.id.btnDownloadAttachments:
                        onDownloadAttachments(message);
                        break;

                    case R.id.ibFull:
                        onShow(message, true);
                        break;
                    case R.id.ibImages:
                        onShow(message, false);
                        break;
                    case R.id.ibUnsubscribe:
                        onActionUnsubscribe(message);
                        break;
                    case R.id.ibVerify:
                    case R.id.ibDecrypt:
                        onActionDecrypt(message, false);
                        break;

                    case R.id.ibDownloading:
                        Helper.viewFAQ(context, 15);
                        break;

                    case R.id.btnCalendarAccept:
                    case R.id.btnCalendarDecline:
                    case R.id.btnCalendarMaybe:
                    case R.id.ibCalendar:
                        onActionCalendar(message, view.getId());
                        break;
                    default:
                        onToggleMessage(message);
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // Unreveal
                    int cx = card.getWidth() / 2;
                    int cy = card.getHeight() / 2;
                    int r = Math.max(card.getWidth(), card.getHeight());
                    Animator anim = ViewAnimationUtils.createCircularReveal(card, cx, cy, r, 0);
                    anim.setInterpolator(new AccelerateDecelerateInterpolator());
                    anim.setDuration(context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
                    anim.start();
                } else {
                    // selectableItemBackground
                    card.setClickable(true);
                    card.setPressed(true);
                    card.setPressed(false);
                    card.setClickable(false);
                }

                if (EntityFolder.DRAFTS.equals(message.folderType) && message.visible == 1)
                    context.startActivity(
                            new Intent(context, ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", message.id));
                else {
                    final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    final Intent viewThread = new Intent(ActivityView.ACTION_VIEW_THREAD)
                            .putExtra("account", message.account)
                            .putExtra("thread", message.thread)
                            .putExtra("id", message.id)
                            .putExtra("found", viewType == ViewType.SEARCH);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean doubletap = prefs.getBoolean("doubletap", false);

                    if (!doubletap || message.folderReadOnly || EntityFolder.OUTBOX.equals(message.folderType)) {
                        lbm.sendBroadcast(viewThread);
                        return;
                    }

                    firstClick = !firstClick;
                    if (firstClick) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (firstClick) {
                                    firstClick = false;
                                    lbm.sendBroadcast(viewThread);
                                }
                            }
                        }, ViewConfiguration.getDoubleTapTimeout());
                    } else {
                        message.ui_seen = !message.ui_seen;
                        message.unseen = (message.ui_seen ? 0 : message.count);
                        bindTo(message, getAdapterPosition());

                        Bundle args = new Bundle();
                        args.putLong("id", message.id);
                        args.putInt("protocol", message.accountProtocol);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                int protocol = args.getInt("protocol");

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(id);
                                    if (message == null)
                                        return null;

                                    if (protocol != EntityAccount.TYPE_IMAP)
                                        EntityOperation.queue(context, message, EntityOperation.SEEN, !message.ui_seen);
                                    else {
                                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                                message.account, message.thread, threading ? null : id, message.ui_seen ? message.folder : null);
                                        for (EntityMessage threaded : messages)
                                            if (threaded.ui_seen == message.ui_seen)
                                                EntityOperation.queue(context, threaded, EntityOperation.SEEN, !message.ui_seen);
                                    }

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                ServiceSynchronize.eval(context, "doubletap");

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                            }
                        }.execute(context, owner, args, "message:seen");
                    }
                }
            }
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final TupleMessageEx message = getMessage();
            if (message == null)
                return false;

            switch (item.getItemId()) {
                case R.id.action_more:
                    onActionMore(message);
                    return true;
                case R.id.action_delete:
                    onActionDelete(message);
                    return true;
                case R.id.action_move:
                    if (EntityFolder.OUTBOX.equals(message.folderType))
                        onActionMoveOutbox(message);
                    else
                        onActionMove(message, false);
                    return true;
                case R.id.action_archive:
                    if (EntityFolder.JUNK.equals(message.folderType))
                        onActionMoveJunk(message);
                    else
                        onActionArchive(message);
                    return true;
                case R.id.action_reply:
                    onActionReplyMenu(message);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            final TupleMessageEx message = getMessage();
            if (message == null || message.folderReadOnly)
                return false;

            if (view.getId() == R.id.ibFlagged) {
                onMenuColoredStar(message);
                return true;
            }

            return false;
        }

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_ENTER ||
                            keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyCode == KeyEvent.KEYCODE_BUTTON_A)) {
                onClick(view);
                return true;
            } else
                return false;
        }

        private void onViewContact(TupleMessageEx message) {
            Uri lookupUri = (Uri) ibAvatar.getTag();
            if (lookupUri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, lookupUri);
                if (intent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(intent);
            }
        }

        private void onShowAuth(TupleMessageEx message) {
            List<String> result = new ArrayList<>();
            if (Boolean.FALSE.equals(message.dkim))
                result.add("DKIM");
            if (Boolean.FALSE.equals(message.spf))
                result.add("SPF");
            if (Boolean.FALSE.equals(message.dmarc))
                result.add("DMARC");
            if (Boolean.FALSE.equals(message.mx))
                result.add("MX");

            ToastEx.makeText(context,
                    context.getString(R.string.title_authentication_failed, TextUtils.join(", ", result)),
                    Toast.LENGTH_LONG)
                    .show();
        }

        private void onShowSnoozed(TupleMessageEx message) {
            if (message.ui_snoozed != null && message.ui_snoozed != Long.MAX_VALUE) {
                DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
                DateFormat D = new SimpleDateFormat("E");
                ToastEx.makeText(
                        context,
                        D.format(message.ui_snoozed) + " " + DTF.format(message.ui_snoozed) + " - " +
                                DateUtils.getRelativeTimeSpanString(
                                        message.ui_snoozed,
                                        System.currentTimeMillis(),
                                        DateUtils.MINUTE_IN_MILLIS,
                                        DateUtils.FORMAT_ABBREV_RELATIVE),
                        Toast.LENGTH_LONG).show();
            }
        }

        private void onToggleFlag(TupleMessageEx message) {
            int flagged = (message.count - message.unflagged);
            Log.i("Set message id=" + message.id + " flagged=" + flagged);

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("flagged", flagged == 0);
            args.putBoolean("thread", viewType != ViewType.THREAD);

            message.unflagged = message.ui_flagged ? message.count : 0;
            message.ui_flagged = !message.ui_flagged;

            boolean expanded = properties.getValue("expanded", message.id);
            bindFlagged(message, expanded);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean flagged = args.getBoolean("flagged");
                    boolean thread = args.getBoolean("thread");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            return null;

                        if (account.protocol != EntityAccount.TYPE_IMAP)
                            EntityOperation.queue(context, message, EntityOperation.FLAG, flagged);
                        else {
                            List<EntityMessage> messages = db.message().getMessagesByThread(
                                    message.account, message.thread, threading && thread ? null : id, null);
                            for (EntityMessage threaded : messages)
                                EntityOperation.queue(context, threaded, EntityOperation.FLAG, flagged);
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
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:flag");
        }

        private void onHelp(TupleMessageEx message) {
            Helper.viewFAQ(context, 130);
        }

        private void onSearchContact(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

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

                    boolean outgoing = EntityFolder.isOutgoing(folder.type);

                    if (message.identity != null) {
                        if (message.from != null && message.from.length > 0) {
                            EntityIdentity identity = db.identity().getIdentity(message.identity);
                            if (identity == null)
                                return null;

                            for (Address sender : message.from)
                                if (identity.similarAddress(sender)) {
                                    outgoing = true;
                                    break;
                                }
                        }
                    }

                    if (outgoing && message.reply != null &&
                            MessageHelper.equal(message.from, message.to))
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
                            new Intent(ActivityView.ACTION_SEARCH)
                                    .putExtra("account", -1L)
                                    .putExtra("folder", -1L)
                                    .putExtra("query", query));
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:search");
        }

        @TargetApi(Build.VERSION_CODES.O)
        private void onNotifyContact(final TupleMessageEx message) {
            final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final String channelId = message.getNotificationChannelId();

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibAddContact);
            NotificationChannel channel = nm.getNotificationChannel(channelId);
            if (channel == null)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_create_channel, 1, R.string.title_create_channel);
            else {
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_channel, 2, R.string.title_edit_channel);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_delete_channel, 3, R.string.title_delete_channel);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_create_channel:
                            onActionCreateChannel();
                            return true;

                        case R.string.title_edit_channel:
                            onActionEditChannel();
                            return true;

                        case R.string.title_delete_channel:
                            onActionDeleteChannel();
                            return true;

                        default:
                            return false;
                    }
                }

                @TargetApi(Build.VERSION_CODES.O)
                private void onActionCreateChannel() {
                    if (!ActivityBilling.isPro(context)) {
                        context.startActivity(new Intent(context, ActivityBilling.class));
                        return;
                    }

                    InternetAddress from = (InternetAddress) message.from[0];
                    NotificationChannel channel = new NotificationChannel(
                            channelId, from.getAddress(),
                            NotificationManager.IMPORTANCE_HIGH);
                    channel.setGroup("contacts");
                    channel.setDescription(from.getPersonal());
                    channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    channel.enableLights(true);
                    nm.createNotificationChannel(channel);
                    onActionEditChannel();
                }

                private void onActionEditChannel() {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
                            .putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
                    context.startActivity(intent);
                }

                private void onActionDeleteChannel() {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.deleteNotificationChannel(channelId);
                }
            });

            popupMenu.show();
        }

        private void onAddContact(TupleMessageEx message) {
            for (Address address : message.from) {
                InternetAddress ia = (InternetAddress) address;
                String name = ia.getPersonal();
                String email = ia.getAddress();

                // https://developer.android.com/training/contacts-provider/modify-data
                Intent edit = new Intent();
                if (!TextUtils.isEmpty(name))
                    edit.putExtra(ContactsContract.Intents.Insert.NAME, name);
                if (!TextUtils.isEmpty(email))
                    edit.putExtra(ContactsContract.Intents.Insert.EMAIL, email);

                ContentResolver resolver = context.getContentResolver();
                try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                                ContactsContract.Contacts.LOOKUP_KEY
                        },
                        ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                        new String[]{email}, null)) {
                    if (cursor != null && cursor.moveToNext()) {
                        int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                        int colLookupKey = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);

                        long contactId = cursor.getLong(colContactId);
                        String lookupKey = cursor.getString(colLookupKey);

                        Uri lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);

                        edit.setAction(Intent.ACTION_EDIT);
                        edit.setDataAndTypeAndNormalize(lookupUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    } else {
                        edit.setAction(Intent.ACTION_INSERT);
                        edit.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    }
                }

                PackageManager pm = context.getPackageManager();
                if (edit.resolveActivity(pm) == null)
                    Snackbar.make(parentFragment.getView(),
                            R.string.title_no_contacts, Snackbar.LENGTH_LONG).show();
                else
                    context.startActivity(edit);
            }
        }

        private void onToggleMessage(TupleMessageEx message) {
            if (EntityFolder.DRAFTS.equals(message.folderType))
                context.startActivity(
                        new Intent(context, ActivityCompose.class)
                                .putExtra("action", "edit")
                                .putExtra("id", message.id));
            else {
                boolean expanded = !properties.getValue("expanded", message.id);
                properties.setValue("expanded", message.id, expanded);

                ibExpander.setTag(expanded);
                ibExpander.setImageLevel(expanded ? 0 /* less*/ : 1 /* more */);

                if (expanded)
                    bindExpanded(message, true);
                else
                    clearExpanded(message);

                bindFlagged(message, expanded);
                bindExpandWarning(message, expanded);

                // Needed for expand one
                properties.scrollTo(getAdapterPosition());
            }
        }

        private void onToggleAddresses(TupleMessageEx message) {
            boolean addresses = !properties.getValue("addresses", message.id);
            properties.setValue("addresses", message.id, addresses);
            bindExpanded(message, false);
        }

        private void onDownloadAttachments(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long mid = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(mid);
                        if (message == null || message.uid == null)
                            return null;

                        for (EntityAttachment attachment : db.attachment().getAttachments(message.id))
                            if (attachment.progress == null && !attachment.available)
                                EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.id);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    ServiceSynchronize.eval(context, "attachment");

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:attachment:download");
        }

        private void onSaveAttachments(TupleMessageEx message) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(FragmentMessages.ACTION_STORE_ATTACHMENTS)
                            .putExtra("id", message.id));
        }

        private void onShow(final TupleMessageEx message, boolean full) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            boolean current = properties.getValue(full ? "full" : "images", message.id);
            boolean asked = properties.getValue(full ? "full_asked" : "images_asked", message.id);
            if (current || asked) {
                if (current) {
                    SharedPreferences.Editor editor = prefs.edit();
                    for (Address sender : message.from) {
                        String from = ((InternetAddress) sender).getAddress();
                        editor.remove(from + (full ? ".show_full" : ".show_images"));
                    }
                    editor.apply();
                }

                properties.setValue(full ? "full" : "images", message.id, !current);
                if (full)
                    onShowFullConfirmed(message);
                else
                    onShowImagesConfirmed(message);
                return;
            }

            View dview = LayoutInflater.from(context).inflate(
                    full ? R.layout.dialog_show_full : R.layout.dialog_show_images, null);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            CheckBox cbAlwaysImages = dview.findViewById(R.id.cbAlwaysImages);

            if (full) {
                cbAlwaysImages.setChecked(prefs.getBoolean("html_always_images", false));

                cbAlwaysImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        prefs.edit().putBoolean("html_always_images", isChecked).apply();
                    }
                });
            }

            if (message.from == null || message.from.length == 0)
                cbNotAgain.setVisibility(View.GONE);
            else {
                List<String> froms = new ArrayList<>();
                for (Address address : message.from)
                    froms.add(((InternetAddress) address).getAddress());
                cbNotAgain.setText(context.getString(R.string.title_no_ask_for_again,
                        TextUtils.join(", ", froms)));
            }

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = prefs.edit();
                    for (Address sender : message.from) {
                        String from = ((InternetAddress) sender).getAddress();
                        editor.putBoolean(from + (full ? ".show_full" : ".show_images"), isChecked);
                    }
                    editor.apply();
                }
            });

            if (full) {
                TextView tvDark = dview.findViewById(R.id.tvDark);
                tvDark.setVisibility(Helper.isDarkTheme(context) ? View.VISIBLE : View.GONE);
            } else {
                boolean disable_tracking = prefs.getBoolean("disable_tracking", true);

                ImageView ivInfo = dview.findViewById(R.id.ivInfo);
                Group grpTracking = dview.findViewById(R.id.grpTracking);

                grpTracking.setVisibility(disable_tracking ? View.VISIBLE : View.GONE);

                ivInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.viewFAQ(context, 82);
                    }
                });
            }

            // TODO: dialog fragment
            final Dialog dialog = new AlertDialog.Builder(context)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            properties.setValue(full ? "full" : "images", message.id, true);
                            properties.setValue(full ? "full_asked" : "images_asked", message.id, true);
                            if (full)
                                onShowFullConfirmed(message);
                            else
                                onShowImagesConfirmed(message);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            owner.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                public void onCreate() {
                    dialog.show();
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroyed() {
                    dialog.dismiss();
                }
            });
        }

        private void onShowFullConfirmed(final TupleMessageEx message) {
            properties.setSize(message.id, null);
            properties.setHeight(message.id, null);
            properties.setPosition(message.id, null);

            bindBody(message);
        }

        private void onShowImagesConfirmed(TupleMessageEx message) {
            bindBody(message);
        }

        private void onActionUnsubscribe(TupleMessageEx message) {
            Uri uri = Uri.parse(message.unsubscribe);
            onOpenLink(uri, context.getString(R.string.title_legend_show_unsubscribe));
        }

        private void onActionDecrypt(TupleMessageEx message, boolean auto) {
            int encrypt = (message.encrypt == null ? EntityMessage.PGP_SIGNENCRYPT /* Inline */ : message.encrypt);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(FragmentMessages.ACTION_DECRYPT)
                            .putExtra("id", message.id)
                            .putExtra("auto", auto)
                            .putExtra("type", encrypt));
        }

        private void onActionReplyMenu(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putSerializable("message", message);

            new SimpleTask<List<TupleIdentityEx>>() {
                @Override
                protected List<TupleIdentityEx> onExecute(Context context, Bundle args) {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                    if (message == null)
                        return null;

                    DB db = DB.getInstance(context);
                    return db.identity().getComposableIdentities(message.account);
                }

                @Override
                protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(message.id))
                        return;

                    final Address[] to =
                            message.replySelf(identities, message.account)
                                    ? message.to
                                    : (message.reply == null || message.reply.length == 0 ? message.from : message.reply);

                    Address[] recipients = message.getAllRecipients(identities, message.account);

                    View anchor = bnvActions.findViewById(R.id.action_reply);
                    PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, anchor);
                    popupMenu.inflate(R.menu.popup_reply);
                    popupMenu.getMenu().findItem(R.id.menu_reply_to_all).setVisible(recipients.length > 0);
                    popupMenu.getMenu().findItem(R.id.menu_reply_list).setVisible(message.list_post != null);
                    popupMenu.getMenu().findItem(R.id.menu_reply_receipt).setVisible(message.receipt_to != null);
                    popupMenu.getMenu().findItem(R.id.menu_reply_answer).setVisible(answers != 0 || !ActivityBilling.isPro(context));
                    popupMenu.getMenu().findItem(R.id.menu_new_message).setVisible(to != null && to.length > 0);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem target) {
                            switch (target.getItemId()) {
                                case R.id.menu_reply_to_sender:
                                    onMenuReply(message, "reply");
                                    return true;
                                case R.id.menu_reply_to_all:
                                    onMenuReply(message, "reply_all");
                                    return true;
                                case R.id.menu_reply_list:
                                    onMenuReply(message, "list");
                                    return true;
                                case R.id.menu_reply_receipt:
                                    onMenuReply(message, "receipt");
                                    return true;
                                case R.id.menu_reply_answer:
                                    onMenuAnswer(message);
                                    return true;
                                case R.id.menu_forward:
                                    onMenuReply(message, "forward");
                                    return true;
                                case R.id.menu_new_message:
                                    onMenuNew(message, to);
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
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:reply");
        }

        private void onMenuReply(TupleMessageEx message, String action) {
            Intent reply = new Intent(context, ActivityCompose.class)
                    .putExtra("action", action)
                    .putExtra("reference", message.id);
            context.startActivity(reply);
        }

        private void onMenuNew(TupleMessageEx message, Address[] to) {
            Intent reply = new Intent(context, ActivityCompose.class)
                    .putExtra("action", "new")
                    .putExtra("to", MessageHelper.formatAddresses(to, true, true));
            context.startActivity(reply);
        }

        private void onMenuAnswer(TupleMessageEx message) {
            new SimpleTask<List<EntityAnswer>>() {
                @Override
                protected List<EntityAnswer> onExecute(Context context, Bundle args) {
                    return DB.getInstance(context).answer().getAnswers(false);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAnswer> answers) {
                    if (answers == null || answers.size() == 0) {
                        Snackbar snackbar = Snackbar.make(
                                parentFragment.getView(),
                                context.getString(R.string.title_no_answers),
                                Snackbar.LENGTH_LONG);
                        snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                                lbm.sendBroadcast(new Intent(ActivityView.ACTION_EDIT_ANSWERS));
                            }
                        });
                        snackbar.show();
                    } else {
                        View anchor = bnvActions.findViewById(R.id.action_reply);
                        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, anchor);

                        int order = 0;
                        for (EntityAnswer answer : answers)
                            popupMenu.getMenu().add(Menu.NONE, answer.id.intValue(), order++, answer.name);

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem target) {
                                if (!ActivityBilling.isPro(context)) {
                                    context.startActivity(new Intent(context, ActivityBilling.class));
                                    return true;
                                }

                                context.startActivity(new Intent(context, ActivityCompose.class)
                                        .putExtra("action", "reply")
                                        .putExtra("reference", message.id)
                                        .putExtra("answer", (long) target.getItemId()));
                                return true;
                            }
                        });

                        popupMenu.show();
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, new Bundle(), "message:answer");
        }

        private void onActionArchive(TupleMessageEx message) {
            properties.move(message.id, EntityFolder.ARCHIVE);
        }

        private void onActionMove(TupleMessageEx message, final boolean copy) {
            Bundle args = new Bundle();
            args.putString("title", context.getString(copy ? R.string.title_copy_to : R.string.title_move_to_folder));
            args.putLong("account", message.account);
            args.putLongArray("disabled", new long[]{message.folder});
            args.putLong("message", message.id);
            args.putBoolean("copy", copy);
            args.putBoolean("similar", false);

            FragmentDialogFolder fragment = new FragmentDialogFolder();
            fragment.setArguments(args);
            fragment.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_MOVE);
            fragment.show(parentFragment.getParentFragmentManager(), "message:move");
        }

        private void onActionMoveOutbox(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    EntityMessage message;

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        db.folder().setFolderError(message.folder, null);

                        File source = message.getFile(context);

                        // Insert into drafts
                        EntityFolder drafts = db.folder().getFolderByType(message.account, EntityFolder.DRAFTS);
                        message.id = null;
                        message.folder = drafts.id;
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

                    if (message.identity != null) {
                        // Identity can be deleted
                        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.cancel("send:" + message.identity, 1);
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:move:draft");
        }

        private void onActionMoveJunk(TupleMessageEx message) {
            properties.move(message.id, EntityFolder.INBOX);
        }

        private void onActionDelete(TupleMessageEx message) {
            if (delete) {
                Bundle aargs = new Bundle();
                aargs.putString("question", context.getString(R.string.title_ask_delete));
                aargs.putLong("id", message.id);

                FragmentDialogAsk ask = new FragmentDialogAsk();
                ask.setArguments(aargs);
                ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_DELETE);
                ask.show(parentFragment.getParentFragmentManager(), "message:delete");
            } else
                properties.move(message.id, EntityFolder.TRASH);
        }

        private void onActionMore(TupleMessageEx message) {
            boolean show_headers = properties.getValue("headers", message.id);

            View anchor = bnvActions.findViewById(R.id.action_more);
            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, anchor);
            popupMenu.inflate(R.menu.popup_message_more);

            popupMenu.getMenu().findItem(R.id.menu_editasnew).setEnabled(message.content);

            popupMenu.getMenu().findItem(R.id.menu_unseen).setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen);
            popupMenu.getMenu().findItem(R.id.menu_unseen).setEnabled(
                    (message.uid != null && !message.folderReadOnly) || message.accountProtocol != EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_hide).setTitle(message.ui_snoozed == null ? R.string.title_hide : R.string.title_unhide);

            popupMenu.getMenu().findItem(R.id.menu_flag_color).setEnabled(
                    (message.uid != null && !message.folderReadOnly) || message.accountProtocol != EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_copy).setEnabled(message.uid != null && !message.folderReadOnly);
            popupMenu.getMenu().findItem(R.id.menu_copy).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_delete).setEnabled(message.uid == null || !message.folderReadOnly);
            popupMenu.getMenu().findItem(R.id.menu_delete).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_resync).setEnabled(message.uid != null);
            popupMenu.getMenu().findItem(R.id.menu_resync).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_create_rule).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_manage_keywords).setEnabled(message.uid != null && !message.folderReadOnly);
            popupMenu.getMenu().findItem(R.id.menu_manage_keywords).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_junk).setEnabled(message.uid != null && !message.folderReadOnly);
            popupMenu.getMenu().findItem(R.id.menu_junk).setVisible(hasJunk && !EntityFolder.JUNK.equals(message.folderType));

            popupMenu.getMenu().findItem(R.id.menu_share).setEnabled(message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setEnabled(hasWebView && message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setVisible(Helper.canPrint(context));

            popupMenu.getMenu().findItem(R.id.menu_show_headers).setChecked(show_headers);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setEnabled(message.uid != null);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_raw_download).setEnabled(
                    message.uid != null && (message.raw == null || !message.raw));
            popupMenu.getMenu().findItem(R.id.menu_raw_save).setEnabled(
                    message.uid != null && (message.raw != null && message.raw));
            popupMenu.getMenu().findItem(R.id.menu_raw_send).setEnabled(
                    message.uid != null && (message.raw != null && message.raw));

            popupMenu.getMenu().findItem(R.id.menu_raw_download).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);
            popupMenu.getMenu().findItem(R.id.menu_raw_save).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);
            popupMenu.getMenu().findItem(R.id.menu_raw_send).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    switch (target.getItemId()) {
                        case R.id.menu_editasnew:
                            onMenuEditAsNew(message);
                            return true;
                        case R.id.menu_unseen:
                            onMenuUnseen(message);
                            return true;
                        case R.id.menu_snooze:
                            onMenuSnooze(message);
                            return true;
                        case R.id.menu_hide:
                            onMenuHide(message);
                            return true;
                        case R.id.menu_flag_color:
                            onMenuColoredStar(message);
                            return true;
                        case R.id.menu_copy:
                            onActionMove(message, true);
                            return true;
                        case R.id.menu_delete:
                            onMenuDelete(message);
                            return true;
                        case R.id.menu_junk:
                            onMenuJunk(message);
                            return true;
                        case R.id.menu_resync:
                            onMenuResync(message);
                            return true;
                        case R.id.menu_create_rule:
                            onMenuCreateRule(message);
                            return true;
                        case R.id.menu_manage_keywords:
                            onMenuManageKeywords(message);
                            return true;
                        case R.id.menu_share:
                            onMenuShare(message);
                            return true;
                        case R.id.menu_print:
                            onMenuPrint(message);
                            return true;
                        case R.id.menu_show_headers:
                            onMenuShowHeaders(message);
                            return true;
                        case R.id.menu_raw_download:
                            onMenuRawDownload(message);
                            return true;
                        case R.id.menu_raw_save:
                            onMenuRawSave(message);
                            return true;
                        case R.id.menu_raw_send:
                            onMenuRawSend(message);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }

        private class TouchHandler extends ArrowKeyMovementMethod {
            private TupleMessageEx message;

            TouchHandler(TupleMessageEx message) {
                this.message = message;
            }

            @Override
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();

                    x += widget.getScrollX();
                    y += widget.getScrollY();

                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int off = layout.getOffsetForHorizontal(line, x);

                    boolean show_images = properties.getValue("images", message.id);
                    if (!show_images) {
                        ImageSpan[] image = buffer.getSpans(off, off, ImageSpan.class);
                        if (image.length > 0 && image[0].getSource() != null) {
                            ImageHelper.AnnotatedSource a = new ImageHelper.AnnotatedSource(image[0].getSource());
                            Uri uri = Uri.parse(a.getSource());
                            if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
                                onOpenLink(uri, null);
                                return true;
                            }
                        }
                    }

                    URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
                    if (link.length > 0) {
                        String url = link[0].getURL();
                        Uri uri = Uri.parse(url);
                        if (uri.getScheme() == null)
                            uri = Uri.parse("https://" + url);

                        int start = buffer.getSpanStart(link[0]);
                        int end = buffer.getSpanEnd(link[0]);
                        String title = (start < 0 || end < 0 || end <= start
                                ? null : buffer.subSequence(start, end).toString());
                        if (url.equals(title))
                            title = null;

                        onOpenLink(uri, title);
                        return true;
                    }

                    ImageSpan[] image = buffer.getSpans(off, off, ImageSpan.class);
                    if (image.length > 0) {
                        String source = image[0].getSource();
                        if (source != null) {
                            onOpenImage(message.id, source);
                            return true;
                        }
                    }

                    DynamicDrawableSpan[] ddss = buffer.getSpans(off, off, DynamicDrawableSpan.class);
                    if (ddss.length > 0) {
                        properties.setValue("quotes", message.id, true);
                        bindBody(message);
                    }
                }

                return super.onTouchEvent(widget, buffer, event);
            }
        }

        private void onOpenLink(final Uri uri, String title) {
            Log.i("Opening uri=" + uri);

            if (BuildConfig.APPLICATION_ID.equals(uri.getHost()) && "/activate/".equals(uri.getPath())) {
                try {
                    if (ActivityBilling.activatePro(context, uri))
                        ToastEx.makeText(context, R.string.title_pro_valid, Toast.LENGTH_LONG).show();
                    else
                        ToastEx.makeText(context, R.string.title_pro_invalid, Toast.LENGTH_LONG).show();
                } catch (NoSuchAlgorithmException ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            } else {
                if ("cid".equals(uri.getScheme()))
                    return;

                Bundle args = new Bundle();
                args.putParcelable("uri", uri);
                args.putString("title", title);

                FragmentDialogLink fragment = new FragmentDialogLink();
                fragment.setArguments(args);
                fragment.show(parentFragment.getParentFragmentManager(), "open:link");
            }
        }

        private void onOpenImage(long id, String source) {
            Log.i("Viewing image source=" + source);

            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putString("source", source);
            args.putInt("zoom", zoom);

            FragmentDialogImage fragment = new FragmentDialogImage();
            fragment.setArguments(args);
            fragment.show(parentFragment.getParentFragmentManager(), "view:image");
        }

        private void onMenuEditAsNew(final TupleMessageEx message) {
            Intent asnew = new Intent(context, ActivityCompose.class)
                    .putExtra("action", "editasnew")
                    .putExtra("reference", message.id);
            context.startActivity(asnew);
        }

        private void onMenuUnseen(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("seen", !message.ui_seen);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean seen = args.getBoolean("seen");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        EntityOperation.queue(context, message, EntityOperation.SEEN, seen);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    ServiceSynchronize.eval(context, "seen");

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void ignored) {
                    boolean seen = args.getBoolean("seen");
                    if (!seen)
                        properties.setValue("expanded", message.id, false);
                    notifyDataSetChanged();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:unseen");
        }

        private void onMenuSnooze(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putString("title", context.getString(R.string.title_snooze));
            args.putLong("account", message.account);
            args.putString("thread", message.thread);
            args.putLong("id", message.id);
            args.putBoolean("finish", true);

            FragmentDialogDuration fragment = new FragmentDialogDuration();
            fragment.setArguments(args);
            fragment.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_SNOOZE);
            fragment.show(parentFragment.getParentFragmentManager(), "message:snooze");
        }

        private void onMenuHide(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("hide", message.ui_snoozed == null);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean hide = args.getBoolean("hide");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        db.message().setMessageSnoozed(message.id, hide ? Long.MAX_VALUE : null);
                        db.message().setMessageUiIgnored(message.id, true);
                        EntityMessage.snooze(context, message.id, hide ? Long.MAX_VALUE : null);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:hide");
        }

        private void onMenuColoredStar(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putInt("color", message.color == null ? Color.TRANSPARENT : message.color);
            args.putString("title", context.getString(R.string.title_flag_color));

            FragmentDialogColor fragment = new FragmentDialogColor();
            fragment.setArguments(args);
            fragment.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_COLOR);
            fragment.show(parentFragment.getParentFragmentManager(), "message:color");
        }

        private void onMenuDelete(final TupleMessageEx message) {
            Bundle aargs = new Bundle();
            aargs.putString("question", context.getString(R.string.title_ask_delete));
            aargs.putLong("id", message.id);

            FragmentDialogAsk ask = new FragmentDialogAsk();
            ask.setArguments(aargs);
            ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_DELETE);
            ask.show(parentFragment.getParentFragmentManager(), "message:delete");
        }

        private void onMenuJunk(final TupleMessageEx message) {
            String who = MessageHelper.formatAddresses(message.from);

            Bundle aargs = new Bundle();
            aargs.putString("question", context.getString(R.string.title_ask_spam_who, who));
            aargs.putLong("id", message.id);

            FragmentDialogAsk ask = new FragmentDialogAsk();
            ask.setArguments(aargs);
            ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_JUNK);
            ask.show(parentFragment.getParentFragmentManager(), "message:junk");
        }

        private void onMenuResync(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null || message.uid == null)
                            return null;

                        EntityFolder folder = db.folder().getFolder(message.folder);
                        if (folder == null)
                            return null;

                        db.message().deleteMessage(id);

                        EntityOperation.queue(context, folder, EntityOperation.FETCH, message.uid);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    ServiceSynchronize.eval(context, "resync");

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:share");
        }

        private void onMenuCreateRule(TupleMessageEx message) {
            Intent rule = new Intent(ActivityView.ACTION_EDIT_RULE);
            rule.putExtra("account", message.account);
            rule.putExtra("folder", message.folder);
            if (message.from != null && message.from.length > 0)
                rule.putExtra("sender", ((InternetAddress) message.from[0]).getAddress());
            if (message.to != null && message.to.length > 0)
                rule.putExtra("recipient", ((InternetAddress) message.to[0]).getAddress());
            if (!TextUtils.isEmpty(message.subject))
                rule.putExtra("subject", message.subject);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(rule);
        }

        private void onMenuManageKeywords(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putStringArray("keywords", message.keywords);

            new SimpleTask<EntityFolder>() {
                @Override
                protected EntityFolder onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    return db.folder().getFolder(message.folder);
                }

                @Override
                protected void onExecuted(final Bundle args, EntityFolder folder) {
                    if (folder == null)
                        return;

                    args.putStringArray("fkeywords", folder.keywords);

                    FragmentKeywordManage fragment = new FragmentKeywordManage();
                    fragment.setArguments(args);
                    fragment.show(parentFragment.getParentFragmentManager(), "keyword:manage");
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:keywords");
        }

        private void onMenuShare(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<String[]>() {
                @Override
                protected String[] onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null || !message.content)
                        return null;

                    File file = message.getFile(context);
                    if (!file.exists())
                        return null;

                    String from = null;
                    if (message.from != null && message.from.length > 0)
                        from = ((InternetAddress) message.from[0]).getAddress();

                    String text = HtmlHelper.getText(Helper.readText(file));

                    return new String[]{from, message.subject, text};
                }

                @Override
                protected void onExecuted(Bundle args, String[] text) {
                    if (text == null)
                        return;

                    Intent share = new Intent();
                    share.setAction(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    if (!TextUtils.isEmpty(text[0]))
                        share.putExtra(Intent.EXTRA_EMAIL, new String[]{text[0]});
                    if (!TextUtils.isEmpty(text[1]))
                        share.putExtra(Intent.EXTRA_SUBJECT, text[1]);
                    if (!TextUtils.isEmpty(text[2]))
                        share.putExtra(Intent.EXTRA_TEXT, text[2]);

                    PackageManager pm = context.getPackageManager();
                    if (share.resolveActivity(pm) == null)
                        Snackbar.make(parentFragment.getView(),
                                context.getString(R.string.title_no_viewer, share.getAction()),
                                Snackbar.LENGTH_LONG).
                                show();
                    else
                        context.startActivity(Helper.getChooser(context, share));
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:share");
        }

        private void onMenuPrint(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("headers", properties.getValue("headers", message.id));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("print_html_confirmed", false)) {
                Intent data = new Intent();
                data.putExtra("args", args);
                parentFragment.onActivityResult(FragmentMessages.REQUEST_PRINT, RESULT_OK, data);
                return;
            }

            args.putString("question", context.getString(R.string.title_ask_show_html));
            args.putString("notagain", "print_html_confirmed");

            FragmentDialogAsk ask = new FragmentDialogAsk();
            ask.setArguments(args);
            ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_PRINT);
            ask.show(parentFragment.getParentFragmentManager(), "message:print");
        }

        private void onMenuShowHeaders(TupleMessageEx message) {
            boolean show_headers = !properties.getValue("headers", message.id);
            properties.setValue("headers", message.id, show_headers);
            if (show_headers && message.headers == null) {
                grpHeaders.setVisibility(View.VISIBLE);
                if (suitable)
                    pbHeaders.setVisibility(View.VISIBLE);
                else
                    tvNoInternetHeaders.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", message.id);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        Long id = args.getLong("id");

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityMessage message = db.message().getMessage(id);
                            if (message == null)
                                return null;

                            EntityOperation.queue(context, message, EntityOperation.HEADERS);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        ServiceSynchronize.eval(context, "headers");

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.execute(context, owner, args, "message:headers");
            } else
                notifyDataSetChanged();
        }

        private void onMenuRawDownload(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    Long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        EntityOperation.queue(context, message, EntityOperation.RAW);

                        db.message().setMessageRaw(message.id, false);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    ServiceSynchronize.eval(context, "raw");

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:raw");
        }

        private void onMenuRawSave(TupleMessageEx message) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(FragmentMessages.ACTION_STORE_RAW)
                            .putExtra("id", message.id));
        }

        private void onMenuRawSend(TupleMessageEx message) {
            File file = message.getRawFile(context);
            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);

            Intent send = new Intent(Intent.ACTION_SEND);
            send.putExtra(Intent.EXTRA_STREAM, uri);
            send.setType("message/rfc822");
            send.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(send);
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
            return new ItemDetailsMessage(this);
        }

        Long getKey() {
            return getKeyAtPosition(getAdapterPosition());
        }
    }

    AdapterMessage(Fragment parentFragment,
                   String type, ViewType viewType,
                   boolean compact, int zoom, String sort, boolean ascending, boolean filter_duplicates,
                   final IProperties properties) {
        this.parentFragment = parentFragment;
        this.type = type;
        this.viewType = viewType;
        this.compact = compact;
        this.zoom = zoom;
        this.sort = sort;
        this.ascending = ascending;
        this.filter_duplicates = filter_duplicates;
        this.properties = properties;

        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        this.accessibility = (am != null && am.isEnabled());

        this.TF = Helper.getTimeInstance(context, SimpleDateFormat.SHORT);
        this.DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.LONG, SimpleDateFormat.LONG);

        ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
        this.suitable = state.isSuitable();
        this.unmetered = state.isUnmetered();

        this.dp36 = Helper.dp2pixels(context, 36);
        this.colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
        this.colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        this.textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean highlight_unread = prefs.getBoolean("highlight_unread", false);

        this.colorUnread = Helper.resolveColor(context, highlight_unread ? R.attr.colorUnreadHighlight : R.attr.colorUnread);
        this.colorRead = Helper.resolveColor(context, R.attr.colorRead);

        this.colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);

        this.hasWebView = Helper.hasWebView(context);
        this.contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        this.textSize = Helper.getTextSize(context, zoom);

        boolean contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        boolean avatars = prefs.getBoolean("avatars", true);
        boolean generated = prefs.getBoolean("generated_icons", true);

        this.date = prefs.getBoolean("date", true);
        this.threading = prefs.getBoolean("threading", true);
        this.avatars = (contacts && avatars) || generated;
        this.name_email = prefs.getBoolean("name_email", false);
        this.distinguish_contacts = prefs.getBoolean("distinguish_contacts", false);

        this.subject_top = prefs.getBoolean("subject_top", false);

        int fz_sender = prefs.getInt("font_size_sender", -1);
        if (fz_sender >= 0)
            font_size_sender = Helper.getTextSize(context, fz_sender);

        int fz_subject = prefs.getInt("font_size_subject", -1);
        if (fz_subject >= 0)
            font_size_subject = Helper.getTextSize(context, fz_subject);

        this.subject_italic = prefs.getBoolean("subject_italic", true);
        this.subject_ellipsize = prefs.getString("subject_ellipsize", "middle");
        this.flags = prefs.getBoolean("flags", true);
        this.flags_background = prefs.getBoolean("flags_background", false);
        this.preview = prefs.getBoolean("preview", false);
        this.preview_italic = prefs.getBoolean("preview_italic", true);
        this.preview_lines = prefs.getInt("preview_lines", 2);
        this.attachments_alt = prefs.getBoolean("attachments_alt", false);
        this.contrast = prefs.getBoolean("contrast", false);
        this.monospaced = prefs.getBoolean("monospaced", false);
        this.inline = prefs.getBoolean("inline_images", false);
        this.collapse_quotes = prefs.getBoolean("collapse_quotes", false);
        this.authentication = prefs.getBoolean("authentication", true);

        debug = prefs.getBoolean("debug", false);

        AsyncDifferConfig<TupleMessageEx> config = new AsyncDifferConfig.Builder<>(DIFF_CALLBACK)
                .setBackgroundThreadExecutor(executor)
                .build();
        this.differ = new AsyncPagedListDiffer<>(new AdapterListUpdateCallback(this), config);
        this.differ.addPagedListListener(new AsyncPagedListDiffer.PagedListListener<TupleMessageEx>() {
            @Override
            public void onCurrentListChanged(@Nullable PagedList<TupleMessageEx> previousList, @Nullable PagedList<TupleMessageEx> currentList) {
                if (gotoTop) {
                    gotoTop = false;
                    properties.scrollTo(0);
                }
            }
        });

        try {
            // https://issuetracker.google.com/issues/135628748
            Handler handler = new Handler(Looper.getMainLooper());
            Field mMainThreadExecutor = this.differ.getClass().getDeclaredField("mMainThreadExecutor");
            mMainThreadExecutor.setAccessible(true);
            mMainThreadExecutor.set(this.differ, new Executor() {
                @Override
                public void execute(final Runnable command) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                command.run();
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }
                    });
                }
            });
        } catch (Throwable ex) {
            Log.e(ex);
        }

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterMessage.this + " parent destroyed");
                AdapterMessage.this.parentFragment = null;
            }
        });
    }

    void gotoTop() {
        properties.scrollTo(0);
        this.gotoTop = true;
    }

    void submitList(PagedList<TupleMessageEx> list) {
        keyPosition.clear();
        differ.submitList(list);
    }

    PagedList<TupleMessageEx> getCurrentList() {
        return differ.getCurrentList();
    }

    void setCompact(boolean compact) {
        if (this.compact != compact) {
            this.compact = compact;
            notifyDataSetChanged();
        }
    }

    void setZoom(int zoom) {
        if (this.zoom != zoom) {
            this.zoom = zoom;
            textSize = Helper.getTextSize(context, zoom);
            notifyDataSetChanged();
        }
    }

    int getZoom() {
        return this.zoom;
    }

    void setSort(String sort) {
        if (!sort.equals(this.sort)) {
            this.sort = sort;
            notifyDataSetChanged();
            // Needed to redraw item decorators / add/remove size
        }
    }

    String getSort() {
        return this.sort;
    }

    void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    void setFilterDuplicates(boolean filter_duplicates) {
        if (this.filter_duplicates != filter_duplicates) {
            this.filter_duplicates = filter_duplicates;
            notifyDataSetChanged();
        }
    }

    void checkInternet() {
        ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
        if (this.suitable != state.isSuitable() || this.unmetered != state.isUnmetered()) {
            this.suitable = state.isSuitable();
            this.unmetered = state.isUnmetered();
            notifyDataSetChanged();
        }
    }

    void setAnswerCount(int answers) {
        this.answers = answers;
        Log.i("Answer count=" + answers);
    }

    @Override
    public int getItemCount() {
        return differ.getItemCount();
    }

    private static final DiffUtil.ItemCallback<TupleMessageEx> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TupleMessageEx>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull TupleMessageEx prev, @NonNull TupleMessageEx next) {
                    return prev.id.equals(next.id);
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull TupleMessageEx prev, @NonNull TupleMessageEx next) {
                    boolean same = true;

                    // id
                    // account
                    // folder
                    if (!Objects.equals(prev.identity, next.identity)) {
                        // via
                        same = false;
                        Log.i("Entity changed id=" + next.id);
                    }
                    // extra
                    if (!Objects.equals(prev.uid, next.uid)) {
                        same = false;
                        Log.i("uid changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.msgid, next.msgid)) {
                        // debug info
                        same = false;
                        Log.i("msgid changed id=" + next.id);
                    }
                    // references
                    // deliveredto
                    // inreplyto
                    if (!Objects.equals(prev.thread, next.thread)) {
                        same = false;
                        Log.i("thread changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.priority, next.priority)) {
                        same = false;
                        Log.i("priority changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.receipt_request, next.receipt_request)) {
                        same = false;
                        Log.i("receipt_request changed id=" + next.id);
                    }
                    if (!MessageHelper.equal(prev.receipt_to, next.receipt_to)) {
                        same = false;
                        Log.i("receipt_to changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.dkim, next.dkim)) {
                        same = false;
                        Log.i("dkim changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.spf, next.spf)) {
                        same = false;
                        Log.i("spf changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.dmarc, next.dmarc)) {
                        same = false;
                        Log.i("dmarc changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.mx, next.mx)) {
                        same = false;
                        Log.i("mx changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.avatar, next.avatar)) {
                        same = false;
                        Log.i("avatar changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.sender, next.sender)) {
                        same = false;
                        Log.i("sender changed id=" + next.id);
                    }
                    if (!MessageHelper.equal(prev.from, next.from)) {
                        same = false;
                        Log.i("from changed id=" + next.id);
                    }
                    if (!MessageHelper.equal(prev.to, next.to)) {
                        same = false;
                        Log.i("to changed id=" + next.id);
                    }
                    if (!MessageHelper.equal(prev.cc, next.cc)) {
                        same = false;
                        Log.i("cc changed id=" + next.id);
                    }
                    if (!MessageHelper.equal(prev.bcc, next.bcc)) {
                        same = false;
                        Log.i("bcc changed id=" + next.id);
                    }
                    if (!MessageHelper.equal(prev.reply, next.reply)) {
                        same = false;
                        Log.i("reply changed id=" + next.id);
                    }
                    if (!MessageHelper.equal(prev.list_post, next.list_post)) {
                        same = false;
                        Log.i("list_post changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.headers, next.headers)) {
                        same = false;
                        Log.i("headers changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.raw, next.raw)) {
                        same = false;
                        Log.i("raw changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.subject, next.subject)) {
                        same = false;
                        Log.i("subject changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.size, next.size)) {
                        same = false;
                        Log.i("size changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.total, next.total)) {
                        same = false;
                        Log.i("total changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.attachments, next.attachments)) {
                        same = false;
                        Log.i("attachments changed id=" + next.id);
                    }
                    if (!prev.content.equals(next.content)) {
                        same = false;
                        Log.i("content changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.plain_only, next.plain_only)) {
                        same = false;
                        Log.i("plain_only changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.preview, next.preview)) {
                        same = false;
                        Log.i("preview changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.sent, next.sent)) {
                        same = false;
                        Log.i("sent changed id=" + next.id);
                    }
                    if (!prev.received.equals(next.received)) {
                        same = false;
                        Log.i("received changed id=" + next.id);
                    }
                    if (!prev.stored.equals(next.stored)) {
                        // updated after decryption
                        same = false;
                        Log.i("stored changed id=" + next.id);
                    }
                    // seen
                    // answered
                    // flagged
                    if (debug && !Objects.equals(prev.flags, next.flags)) {
                        same = false;
                        Log.i("flags changed id=" + next.id);
                    }
                    if (!Helper.equal(prev.keywords, next.keywords)) {
                        same = false;
                        Log.i("keywords changed id=" + next.id);
                    }
                    // notifying
                    if (!prev.ui_seen.equals(next.ui_seen)) {
                        same = false;
                        Log.i("ui_seen changed id=" + next.id);
                    }
                    if (!prev.ui_answered.equals(next.ui_answered)) {
                        same = false;
                        Log.i("ui_answer changed id=" + next.id);
                    }
                    if (!prev.ui_flagged.equals(next.ui_flagged)) {
                        same = false;
                        Log.i("ui_flagged changed id=" + next.id);
                    }
                    if (!prev.ui_hide.equals(next.ui_hide)) {
                        same = false;
                        Log.i("ui_hide changed id=" + next.id);
                    }
                    if (!prev.ui_found.equals(next.ui_found)) {
                        same = false;
                        Log.i("ui_found changed id=" + next.id);
                    }
                    // ui_ignored
                    if (!prev.ui_browsed.equals(next.ui_browsed)) {
                        same = false;
                        Log.i("ui_browsed changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.ui_snoozed, next.ui_snoozed)) {
                        same = false;
                        Log.i("ui_snoozed changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.color, next.color)) {
                        same = false;
                        Log.i("color changed id=" + next.id);
                    }
                    // revision
                    // revisions
                    if (!Objects.equals(prev.warning, next.warning)) {
                        same = false;
                        Log.i("warning changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.error, next.error)) {
                        same = false;
                        Log.i("error changed id=" + next.id);
                    }
                    // last_attempt

                    // accountPop
                    if (!Objects.equals(prev.accountName, next.accountName)) {
                        same = false;
                        Log.i("accountName changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.accountColor, next.accountColor)) {
                        same = false;
                        Log.i("accountColor changed id=" + next.id);
                    }
                    // accountNotify
                    // accountAutoSeen
                    if (!prev.folderName.equals(next.folderName)) {
                        same = false;
                        Log.i("folderName changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.folderDisplay, next.folderDisplay)) {
                        same = false;
                        Log.i("folderDisplay changed id=" + next.id);
                    }
                    if (!prev.folderType.equals(next.folderType)) {
                        same = false;
                        Log.i("folderType changed id=" + next.id);
                    }
                    if (prev.folderReadOnly != next.folderReadOnly) {
                        same = false;
                        Log.i("folderReadOnly changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.identityName, next.identityName)) {
                        same = false;
                        Log.i("identityName changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.identityEmail, next.identityEmail)) {
                        same = false;
                        Log.i("identityEmail changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.identitySynchronize, next.identitySynchronize)) {
                        same = false;
                        Log.i("identitySynchronize changed id=" + next.id);
                    }
                    // senders
                    if (prev.count != next.count) {
                        same = false;
                        Log.i("count changed id=" + next.id);
                    }
                    if (prev.unseen != next.unseen) {
                        same = false;
                        Log.i("unseen changed id=" + next.id);
                    }
                    if (prev.unflagged != next.unflagged) {
                        same = false;
                        Log.i("unflagged changed id=" + next.id);
                    }
                    if (prev.drafts != next.drafts) {
                        same = false;
                        Log.i("drafts changed id=" + next.id);
                    }
                    if (prev.signed != next.signed) {
                        same = false;
                        Log.i("signed changed id=" + next.id);
                    }
                    if (prev.encrypted != next.encrypted) {
                        same = false;
                        Log.i("encrypted changed id=" + next.id);
                    }
                    if (prev.visible != next.visible) {
                        same = false;
                        Log.i("visible changed id=" + next.id);
                    }
                    if (!Objects.equals(prev.totalSize, next.totalSize)) {
                        same = false;
                        Log.i("totalSize changed id=" + next.id);
                    }
                    if (prev.duplicate != next.duplicate) {
                        same = false;
                        Log.i("duplicate changed id=" + next.id);
                    }

                    return same;
                }
            };

    @Override
    public int getItemViewType(int position) {
        TupleMessageEx message = differ.getItem(position);

        if (message == null || context == null)
            return R.layout.item_message_placeholder;

        if (filter_duplicates && message.duplicate)
            return R.layout.item_message_duplicate;

        return (compact ? R.layout.item_message_compact : R.layout.item_message_normal);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TupleMessageEx message = differ.getItem(position);

        if (message == null || context == null)
            return;

        if (viewType == ViewType.THREAD) {
            boolean outgoing = holder.isOutgoing(message);
            holder.card.setOutgoing(outgoing);
        }

        if (filter_duplicates && message.duplicate) {
            holder.tvFolder.setText(context.getString(R.string.title_duplicate_in, message.getFolderName(context)));
            holder.tvFolder.setTypeface(message.unseen > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            holder.tvFolder.setTextColor(message.unseen > 0 ? colorUnread : colorRead);
            holder.tvFolder.setAlpha(Helper.LOW_LIGHT);
            return;
        }

        holder.unwire();
        holder.bindTo(message, position);
        holder.wire();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.cowner.stop();
        holder.powner.recreate();
    }

    void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    int getPositionForKey(long key) {
        if (keyPosition.isEmpty()) {
            PagedList<TupleMessageEx> messages = getCurrentList();
            if (messages != null) {
                for (int i = 0; i < messages.size(); i++) {
                    TupleMessageEx message = messages.get(i);
                    if (message != null)
                        keyPosition.put(message.id, i);
                }
                Log.i("Mapped keys=" + keyPosition.size());
            }
        }

        if (keyPosition.containsKey(key)) {
            int pos = keyPosition.get(key);
            Log.d("Position=" + pos + " @Key=" + key);
            return pos;
        }

        Log.i("Position=" + RecyclerView.NO_POSITION + " @Key=" + key);
        return RecyclerView.NO_POSITION;
    }

    TupleMessageEx getItemAtPosition(int pos) {
        PagedList<TupleMessageEx> messages = getCurrentList();
        if (messages != null && pos >= 0 && pos < messages.size()) {
            TupleMessageEx message = messages.get(pos);
            Long key = (message == null ? null : message.id);
            Log.d("Item=" + key + " @Position=" + pos);
            return message;
        } else {
            Log.d("Item=" + null + " @Position=" + pos);
            return null;
        }
    }

    TupleMessageEx getItemForKey(long key) {
        int pos = getPositionForKey(key);
        if (pos == RecyclerView.NO_POSITION) {
            Log.d("Item=" + null + " @Key=" + key);
            return null;
        } else
            return getItemAtPosition(pos);
    }

    Long getKeyAtPosition(int pos) {
        TupleMessageEx message = getItemAtPosition(pos);
        Long key = (message == null ? null : message.id);
        Log.d("Key=" + key + " @Position=" + pos);
        return key;
    }

    interface IProperties {
        void setValue(String name, long id, boolean enabled);

        boolean getValue(String name, long id);

        void setSize(long id, Float size);

        float getSize(long id, float defaultSize);

        void setHeight(long id, Integer height);

        int getHeight(long id, int defaultHeight);

        void setPosition(long id, Pair<Integer, Integer> position);

        Pair<Integer, Integer> getPosition(long id);

        void setAttachments(long id, List<EntityAttachment> attachments);

        List<EntityAttachment> getAttachments(long id);

        void scrollTo(int pos);

        void move(long id, String type);

        void finish();
    }

    public static class FragmentDialogLink extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Uri uri = getArguments().getParcelable("uri");
            String title = getArguments().getString("title");

            final Uri sanitized;
            if (uri.isOpaque())
                sanitized = uri;
            else {
                // https://en.wikipedia.org/wiki/UTM_parameters
                Uri.Builder builder = uri.buildUpon();

                boolean changed = false;
                builder.clearQuery();
                for (String key : uri.getQueryParameterNames())
                    if (PARANOID_QUERY.contains(key.toLowerCase(Locale.ROOT)))
                        changed = true;
                    else if (!TextUtils.isEmpty(key))
                        for (String value : uri.getQueryParameters(key)) {
                            Log.i("Query " + key + "=" + value);
                            builder.appendQueryParameter(key, value);
                        }

                sanitized = (changed ? builder.build() : uri);
            }

            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_open_link, null);
            TextView tvTitle = dview.findViewById(R.id.tvTitle);
            final EditText etLink = dview.findViewById(R.id.etLink);
            TextView tvDifferent = dview.findViewById(R.id.tvDifferent);
            final CheckBox cbSecure = dview.findViewById(R.id.cbSecure);
            CheckBox cbSanitize = dview.findViewById(R.id.cbSanitize);
            final Button btnOwner = dview.findViewById(R.id.btnOwner);
            TextView tvOwnerRemark = dview.findViewById(R.id.tvOwnerRemark);
            final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);
            final TextView tvHost = dview.findViewById(R.id.tvHost);
            final TextView tvOwner = dview.findViewById(R.id.tvOwner);
            final Group grpOwner = dview.findViewById(R.id.grpOwner);

            etLink.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Uri uri = Uri.parse(editable.toString());

                    boolean secure = (!uri.isOpaque() &&
                            "https".equals(uri.getScheme()));
                    boolean hyperlink = (!uri.isOpaque() &&
                            ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())));

                    cbSecure.setTag(secure);
                    cbSecure.setChecked(secure);

                    cbSecure.setText(
                            secure ? R.string.title_link_https : R.string.title_link_http);
                    cbSecure.setTextColor(Helper.resolveColor(getContext(),
                            secure ? android.R.attr.textColorSecondary : R.attr.colorWarning));
                    cbSecure.setTypeface(
                            secure ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);

                    cbSecure.setVisibility(hyperlink ? View.VISIBLE : View.GONE);
                }
            });

            cbSecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    boolean tag = (Boolean) compoundButton.getTag();
                    if (tag == checked)
                        return;

                    Uri uri = Uri.parse(etLink.getText().toString());
                    Uri.Builder builder = uri.buildUpon();

                    builder.scheme(checked ? "https" : "http");

                    String authority = uri.getEncodedAuthority();
                    if (authority != null) {
                        authority = authority.replace(checked ? ":80" : ":443", checked ? ":443" : ":80");
                        builder.encodedAuthority(authority);
                    }

                    etLink.setText(builder.build().toString());
                }
            });

            cbSanitize.setVisibility(uri.equals(sanitized) ? View.GONE : View.VISIBLE);

            cbSanitize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if (checked)
                        etLink.setText(sanitized.toString());
                    else
                        etLink.setText(uri.toString());
                }
            });

            tvOwnerRemark.setMovementMethod(LinkMovementMethod.getInstance());
            pbWait.setVisibility(View.GONE);
            grpOwner.setVisibility(View.GONE);

            btnOwner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    args.putParcelable("uri", uri);

                    new SimpleTask<String[]>() {
                        @Override
                        protected void onPreExecute(Bundle args) {
                            btnOwner.setEnabled(false);
                            pbWait.setVisibility(View.VISIBLE);
                            grpOwner.setVisibility(View.GONE);
                        }

                        @Override
                        protected void onPostExecute(Bundle args) {
                            btnOwner.setEnabled(true);
                            pbWait.setVisibility(View.GONE);
                            grpOwner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected String[] onExecute(Context context, Bundle args) throws Throwable {
                            Uri uri = args.getParcelable("uri");
                            return IPInfo.getOrganization(uri, context);
                        }

                        @Override
                        protected void onExecuted(Bundle args, String[] data) {
                            String host = data[0];
                            String organization = data[1];
                            tvHost.setText(host);
                            tvOwner.setText(organization == null ? "?" : organization);
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    dview.scrollTo(0, tvOwner.getBottom());
                                }
                            });
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            tvOwner.setText(ex.getMessage());
                        }
                    }.execute(FragmentDialogLink.this, args, "link:owner");
                }
            });

            tvTitle.setText(title);
            tvTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
            etLink.setText(uri.toString());

            Uri uriTitle = Uri.parse(title == null ? "" : title);
            tvDifferent.setVisibility(uriTitle.getHost() == null || uri.getHost() == null ||
                    uriTitle.getHost().equalsIgnoreCase(uri.getHost())
                    ? View.GONE : View.VISIBLE);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse(etLink.getText().toString());
                            Helper.view(getContext(), uri, false);
                        }
                    })
                    .setNeutralButton(R.string.title_browse, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse(etLink.getText().toString());
                            Helper.view(getContext(), uri, true);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogImage extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final PhotoView pv = new PhotoView(getContext());

            new SimpleTask<Drawable>() {
                @Override
                protected Drawable onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");
                    String source = args.getString("source");
                    int zoom = args.getInt("zoom");
                    return ImageHelper.decodeImage(context, id, source, true, zoom, null);
                }

                @Override
                protected void onExecuted(Bundle args, Drawable drawable) {
                    pv.setImageDrawable(drawable);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (drawable instanceof AnimatedImageDrawable)
                            ((AnimatedImageDrawable) drawable).start();
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, getArguments(), "view:image");

            final Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(pv);

            return dialog;
        }
    }

    public static class FragmentKeywordManage extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long id = getArguments().getLong("id");
            List<String> keywords = Arrays.asList(getArguments().getStringArray("keywords"));
            List<String> fkeywords = Arrays.asList(getArguments().getStringArray("fkeywords"));

            final List<String> items = new ArrayList<>(keywords);
            for (String keyword : fkeywords)
                if (!items.contains(keyword))
                    items.add(keyword);

            Collections.sort(items);

            final boolean[] selected = new boolean[items.size()];
            final boolean[] dirty = new boolean[items.size()];
            for (int i = 0; i < selected.length; i++) {
                selected[i] = keywords.contains(items.get(i));
                dirty[i] = false;
            }

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_manage_keywords)
                    .setMultiChoiceItems(items.toArray(new String[0]), selected, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            dirty[which] = true;
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!ActivityBilling.isPro(getContext())) {
                                startActivity(new Intent(getContext(), ActivityBilling.class));
                                return;
                            }

                            Bundle args = new Bundle();
                            args.putLong("id", id);
                            args.putStringArray("keywords", items.toArray(new String[0]));
                            args.putBooleanArray("selected", selected);
                            args.putBooleanArray("dirty", dirty);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    long id = args.getLong("id");
                                    String[] keywords = args.getStringArray("keywords");
                                    boolean[] selected = args.getBooleanArray("selected");
                                    boolean[] dirty = args.getBooleanArray("dirty");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        EntityMessage message = db.message().getMessage(id);
                                        if (message == null)
                                            return null;

                                        for (int i = 0; i < selected.length; i++)
                                            if (dirty[i])
                                                EntityOperation.queue(context, message, EntityOperation.KEYWORD, keywords[i], selected[i]);

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    ServiceSynchronize.eval(context, "keywords");

                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }
                            }.execute(getContext(), getActivity(), args, "message:keywords:manage");
                        }
                    })
                    .setNeutralButton(R.string.title_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putLong("id", id);

                            FragmentKeywordAdd fragment = new FragmentKeywordAdd();
                            fragment.setArguments(args);
                            fragment.show(getParentFragmentManager(), "keyword:add");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentKeywordAdd extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long id = getArguments().getLong("id");

            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_keyword, null);
            final EditText etKeyword = view.findViewById(R.id.etKeyword);
            etKeyword.setText(null);

            return new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!ActivityBilling.isPro(getContext())) {
                                startActivity(new Intent(getContext(), ActivityBilling.class));
                                return;
                            }

                            String keyword = MessageHelper.sanitizeKeyword(etKeyword.getText().toString());
                            if (!TextUtils.isEmpty(keyword)) {
                                Bundle args = new Bundle();
                                args.putLong("id", id);
                                args.putString("keyword", keyword);

                                new SimpleTask<Void>() {
                                    @Override
                                    protected Void onExecute(Context context, Bundle args) {
                                        long id = args.getLong("id");
                                        String keyword = args.getString("keyword");

                                        DB db = DB.getInstance(context);
                                        try {
                                            db.beginTransaction();

                                            EntityMessage message = db.message().getMessage(id);
                                            if (message == null)
                                                return null;

                                            EntityOperation.queue(context, message, EntityOperation.KEYWORD, keyword, true);

                                            db.setTransactionSuccessful();
                                        } finally {
                                            db.endTransaction();
                                        }

                                        ServiceSynchronize.eval(context, "keyword=" + keyword);

                                        return null;
                                    }

                                    @Override
                                    protected void onException(Bundle args, Throwable ex) {
                                        Log.unexpectedError(getParentFragmentManager(), ex);
                                    }
                                }.execute(getContext(), getActivity(), args, "message:keyword:add");
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
