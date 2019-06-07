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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Attendee;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.Summary;
import biweekly.util.ICalDate;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;
    private ViewType viewType;
    private boolean compact;
    private int zoom;
    private String sort;
    private boolean filter_duplicates;
    private boolean suitable;
    private int answers = -1;
    private IProperties properties;

    private int dp36;
    private int colorPrimary;
    private int colorAccent;
    private int colorWarning;
    private int textColorSecondary;
    private int colorUnread;

    private boolean hasWebView;
    private boolean contacts;
    private float textSize;

    private boolean date;
    private boolean threading;
    private boolean avatars;
    private boolean name_email;
    private boolean subject_italic;
    private boolean flags;
    private boolean preview;
    private boolean attachments_alt;
    private boolean monospaced;
    private boolean autoimages;
    private boolean authentication;
    private boolean debug;

    private boolean gotoTop = false;
    private AsyncPagedListDiffer<TupleMessageEx> differ;
    private SelectionTracker<Long> selectionTracker = null;

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    private NumberFormat NF = NumberFormat.getNumberInstance();
    private DateFormat TF;
    private DateFormat DTF = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG);

    private static final List<String> PARANOID_QUERY = Collections.unmodifiableList(Arrays.asList(
            "utm_source",
            "utm_medium",
            "utm_campaign",
            "utm_term",
            "utm_content"
    ));

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            BottomNavigationView.OnNavigationItemSelectedListener {
        private View view;
        private View vwColor;
        private View vwStatus;
        private ImageView ivExpander;
        private ImageView ivFlagged;
        private ImageView ivAvatar;
        private TextView tvFrom;
        private TextView tvSize;
        private TextView tvTime;
        private ImageView ivType;
        private ImageView ivSnoozed;
        private ImageView ivBrowsed;
        private ImageView ivAnswered;
        private ImageView ivPlain;
        private ImageView ivReceipt;
        private ImageView ivAttachments;
        private TextView tvSubject;
        private TextView tvFolder;
        private TextView tvCount;
        private ImageView ivThread;
        private TextView tvPreview;
        private TextView tvError;
        private ContentLoadingProgressBar pbLoading;
        private View vwRipple;

        private View vsBody;

        private ImageView ivExpanderAddress;

        private ImageButton ibSearchContact;
        private ImageButton ibNotifyContact;
        private ImageButton ibAddContact;

        private TextView tvFromExTitle;
        private TextView tvToTitle;
        private TextView tvReplyToTitle;
        private TextView tvCcTitle;
        private TextView tvBccTitle;
        private TextView tvIdentityTitle;
        private TextView tvTimeExTitle;
        private TextView tvSizeExTitle;

        private TextView tvFromEx;
        private TextView tvTo;
        private TextView tvReplyTo;
        private TextView tvCc;
        private TextView tvBcc;
        private TextView tvIdentity;
        private TextView tvTimeEx;
        private TextView tvSizeEx;

        private TextView tvSubjectEx;
        private TextView tvFlags;
        private TextView tvKeywords;

        private TextView tvHeaders;
        private ContentLoadingProgressBar pbHeaders;
        private TextView tvNoInternetHeaders;

        private RecyclerView rvAttachment;
        private CheckBox cbInline;
        private Button btnDownloadAttachments;
        private Button btnSaveAttachments;
        private TextView tvNoInternetAttachments;

        private BottomNavigationView bnvActions;

        private ImageButton ibImages;
        private ImageButton ibFull;
        private TextView tvBody;
        private ContentLoadingProgressBar pbBody;
        private TextView tvNoInternetBody;

        private TextView tvCalendarSummary;
        private TextView tvCalendarStart;
        private TextView tvCalendarEnd;
        private TextView tvAttendees;
        private Button btnCalendarAccept;
        private Button btnCalendarDecline;
        private Button btnCalendarMaybe;
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

        ViewHolder(final View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);

            vwColor = itemView.findViewById(R.id.vwColor);
            vwStatus = itemView.findViewById(R.id.vwStatus);
            ivExpander = itemView.findViewById(R.id.ivExpander);
            ivFlagged = itemView.findViewById(R.id.ivFlagged);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivType = itemView.findViewById(R.id.ivType);
            ivSnoozed = itemView.findViewById(R.id.ivSnoozed);
            ivBrowsed = itemView.findViewById(R.id.ivBrowsed);
            ivAnswered = itemView.findViewById(R.id.ivAnswered);
            ivPlain = itemView.findViewById(R.id.ivPlain);
            ivReceipt = itemView.findViewById(R.id.ivReceipt);
            ivAttachments = itemView.findViewById(R.id.ivAttachments);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvCount = itemView.findViewById(R.id.tvCount);
            ivThread = itemView.findViewById(R.id.ivThread);
            tvError = itemView.findViewById(R.id.tvError);
            pbLoading = itemView.findViewById(R.id.pbLoading);
            vwRipple = itemView.findViewById(R.id.vwRipple);
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

            ivExpanderAddress = vsBody.findViewById(R.id.ivExpanderAddress);

            ibSearchContact = vsBody.findViewById(R.id.ibSearchContact);
            ibNotifyContact = vsBody.findViewById(R.id.ibNotifyContact);
            ibAddContact = vsBody.findViewById(R.id.ibAddContact);

            tvFromExTitle = vsBody.findViewById(R.id.tvFromExTitle);
            tvToTitle = vsBody.findViewById(R.id.tvToTitle);
            tvReplyToTitle = vsBody.findViewById(R.id.tvReplyToTitle);
            tvCcTitle = vsBody.findViewById(R.id.tvCcTitle);
            tvBccTitle = vsBody.findViewById(R.id.tvBccTitle);
            tvIdentityTitle = vsBody.findViewById(R.id.tvIdentityTitle);
            tvTimeExTitle = vsBody.findViewById(R.id.tvTimeExTitle);
            tvSizeExTitle = vsBody.findViewById(R.id.tvSizeExTitle);

            tvFromEx = vsBody.findViewById(R.id.tvFromEx);
            tvTo = vsBody.findViewById(R.id.tvTo);
            tvReplyTo = vsBody.findViewById(R.id.tvReplyTo);
            tvCc = vsBody.findViewById(R.id.tvCc);
            tvBcc = vsBody.findViewById(R.id.tvBcc);
            tvIdentity = vsBody.findViewById(R.id.tvIdentity);
            tvTimeEx = vsBody.findViewById(R.id.tvTimeEx);
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
            pbCalendarWait = vsBody.findViewById(R.id.pbCalendarWait);

            rvAttachment = attachments.findViewById(R.id.rvAttachment);
            rvAttachment.setHasFixedSize(false);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            rvAttachment.setLayoutManager(llm);
            rvAttachment.setItemAnimator(null);

            adapterAttachment = new AdapterAttachment(context, owner, true);
            rvAttachment.setAdapter(adapterAttachment);

            cbInline = attachments.findViewById(R.id.cbInline);
            btnDownloadAttachments = attachments.findViewById(R.id.btnDownloadAttachments);
            btnSaveAttachments = attachments.findViewById(R.id.btnSaveAttachments);
            tvNoInternetAttachments = attachments.findViewById(R.id.tvNoInternetAttachments);

            bnvActions = vsBody.findViewById(R.id.bnvActions);
            if (compact) {
                bnvActions.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);
                ViewGroup.LayoutParams lparam = bnvActions.getLayoutParams();
                lparam.height = dp36;
                bnvActions.setLayoutParams(lparam);
            }

            ibImages = vsBody.findViewById(R.id.ibImages);
            ibFull = vsBody.findViewById(R.id.ibFull);
            tvBody = vsBody.findViewById(R.id.tvBody);
            pbBody = vsBody.findViewById(R.id.pbBody);
            tvNoInternetBody = vsBody.findViewById(R.id.tvNoInternetBody);

            rvImage = vsBody.findViewById(R.id.rvImage);
            rvImage.setHasFixedSize(false);
            StaggeredGridLayoutManager sglm =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            rvImage.setLayoutManager(sglm);
            adapterImage = new AdapterImage(context, owner);
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
                    super.itemView.getBottom() - vwColor.getHeight(),
                    super.itemView.getRight(),
                    super.itemView.getBottom());
        }

        private void wire() {
            final View touch = (viewType == ViewType.THREAD && threading ? ivExpander : vwColor);
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

            ivSnoozed.setOnClickListener(this);
            ivFlagged.setOnClickListener(this);

            if (vsBody != null) {
                ivExpanderAddress.setOnClickListener(this);
                ibSearchContact.setOnClickListener(this);
                ibNotifyContact.setOnClickListener(this);
                ibAddContact.setOnClickListener(this);

                btnDownloadAttachments.setOnClickListener(this);
                btnSaveAttachments.setOnClickListener(this);

                ibImages.setOnClickListener(this);
                ibFull.setOnClickListener(this);

                btnCalendarAccept.setOnClickListener(this);
                btnCalendarDecline.setOnClickListener(this);
                btnCalendarMaybe.setOnClickListener(this);

                bnvActions.setOnNavigationItemSelectedListener(this);
            }
        }

        private void unwire() {
            if (viewType == ViewType.THREAD) {
                vwColor.setOnClickListener(null);
                ivExpander.setOnClickListener(null);
            } else
                view.setOnClickListener(null);

            ivSnoozed.setOnClickListener(null);
            ivFlagged.setOnClickListener(null);

            if (vsBody != null) {
                ivExpanderAddress.setOnClickListener(null);
                ibSearchContact.setOnClickListener(null);
                ibNotifyContact.setOnClickListener(null);
                ibAddContact.setOnClickListener(null);

                btnDownloadAttachments.setOnClickListener(null);
                btnSaveAttachments.setOnClickListener(null);

                ibImages.setOnClickListener(null);
                ibFull.setOnClickListener(null);

                btnCalendarAccept.setOnClickListener(null);
                btnCalendarDecline.setOnClickListener(null);
                btnCalendarMaybe.setOnClickListener(null);

                bnvActions.setOnNavigationItemSelectedListener(null);
            }
        }

        private void clear() {
            vwColor.setVisibility(View.GONE);
            vwStatus.setVisibility(View.GONE);
            ivExpander.setVisibility(View.GONE);
            ivFlagged.setVisibility(View.GONE);
            ivAvatar.setVisibility(View.GONE);
            tvFrom.setText(null);
            tvSize.setText(null);
            tvTime.setText(null);
            ivType.setVisibility(View.GONE);
            ivSnoozed.setVisibility(View.GONE);
            ivBrowsed.setVisibility(View.GONE);
            ivAnswered.setVisibility(View.GONE);
            ivPlain.setVisibility(View.GONE);
            ivReceipt.setVisibility(View.GONE);
            ivAttachments.setVisibility(View.GONE);
            tvSubject.setText(null);
            tvFolder.setText(null);
            tvCount.setText(null);
            ivThread.setVisibility(View.GONE);
            tvPreview.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            pbLoading.setVisibility(View.VISIBLE);

            clearExpanded(null);
        }

        @SuppressLint("WrongConstant")
        private void bindTo(final TupleMessageEx message) {
            pbLoading.setVisibility(View.GONE);

            if (viewType == ViewType.THREAD)
                view.setVisibility(!filter_duplicates || !message.duplicate ? View.VISIBLE : View.GONE);

            // Text size
            if (textSize != 0) {
                tvFrom.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * (message.unseen > 0 ? 1.1f : 1f));
                tvSubject.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);
                tvPreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);

                int px = Math.round(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_PX, textSize * (compact ? 1.5f : 3.0f),
                        context.getResources().getDisplayMetrics()));
                if (compact && tvFrom.getMinHeight() != px)
                    tvFrom.setMinimumHeight(px);

                ViewGroup.LayoutParams lparams = ivAvatar.getLayoutParams();
                if (lparams.height != px) {
                    lparams.width = px;
                    lparams.height = px;
                    ivAvatar.requestLayout();
                }
            }

            // Selected / disabled
            view.setActivated(selectionTracker != null && selectionTracker.isSelected(message.id));
            view.setAlpha(message.uid == null && !EntityFolder.OUTBOX.equals(message.folderType)
                    ? Helper.LOW_LIGHT : 1.0f);

            // Duplicate
            if (viewType == ViewType.THREAD) {
                boolean dim = (message.duplicate || EntityFolder.TRASH.equals(message.folderType));
                ivFlagged.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivAvatar.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvFrom.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvSize.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvTime.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivType.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivSnoozed.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivBrowsed.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivAnswered.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivPlain.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivReceipt.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
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

            int colorUnseen = (message.unseen > 0 ? colorUnread : textColorSecondary);
            tvFrom.setTextColor(colorUnseen);
            tvSize.setTextColor(colorUnseen);
            tvTime.setTextColor(colorUnseen);

            // Account color
            vwColor.setBackgroundColor(message.accountColor == null ? Color.TRANSPARENT : message.accountColor);
            vwColor.setVisibility(Helper.isPro(context) ? View.VISIBLE : View.INVISIBLE);

            vwStatus.setBackgroundColor(
                    Boolean.FALSE.equals(message.dkim) ||
                            Boolean.FALSE.equals(message.spf) ||
                            Boolean.FALSE.equals(message.dmarc)
                            ? colorAccent : Color.TRANSPARENT);
            vwStatus.setVisibility(authentication ? View.VISIBLE : View.GONE);

            // Expander
            boolean expanded = (viewType == ViewType.THREAD && properties.getValue("expanded", message.id));
            ivExpander.setImageLevel(expanded ? 0 /* less */ : 1 /* more */);
            if (viewType == ViewType.THREAD && threading)
                ivExpander.setVisibility(EntityFolder.DRAFTS.equals(message.folderType) ? View.INVISIBLE : View.VISIBLE);
            else
                ivExpander.setVisibility(View.GONE);

            // Line 1
            Long size = ("size".equals(sort) ? message.totalSize : message.size);
            tvSize.setText(size == null ? null : Helper.humanReadableByteCount(size, true));
            tvSize.setVisibility(size == null || (message.content && !"size".equals(sort)) ? View.GONE : View.VISIBLE);
            tvTime.setText(date && "time".equals(sort)
                    ? TF.format(message.received)
                    : Helper.getRelativeTimeSpanString(context, message.received));

            // Line 2
            tvSubject.setText(message.subject);

            // Line 3
            ivType.setImageResource(message.drafts > 0
                    ? R.drawable.baseline_edit_24 : EntityFolder.getIcon(message.folderType));
            ivType.setVisibility(message.drafts > 0 ||
                    (viewType == ViewType.UNIFIED && !EntityFolder.INBOX.equals(message.folderType)) ||
                    (viewType == ViewType.THREAD && EntityFolder.SENT.equals(message.folderType))
                    ? View.VISIBLE : View.GONE);
            ivSnoozed.setVisibility(message.ui_snoozed == null ? View.GONE : View.VISIBLE);
            ivBrowsed.setVisibility(message.ui_browsed ? View.VISIBLE : View.GONE);
            ivAnswered.setVisibility(message.ui_answered ? View.VISIBLE : View.GONE);
            ivPlain.setVisibility(message.plain_only != null && message.plain_only ? View.VISIBLE : View.GONE);
            ivReceipt.setVisibility(message.receipt_request != null && message.receipt_request ? View.VISIBLE : View.GONE);
            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.FOLDER)
                tvFolder.setText(message.accountName);
            else {
                String folderName = (message.folderDisplay == null
                        ? Helper.localizeFolderName(context, message.folderName)
                        : message.folderDisplay);
                tvFolder.setText((compact ? "" : message.accountName + "/") + folderName);
            }
            tvFolder.setVisibility(compact &&
                    (viewType == ViewType.FOLDER ||
                            (viewType == ViewType.UNIFIED && EntityFolder.INBOX.equals(message.folderType)))
                    ? View.GONE : View.VISIBLE);

            if (viewType == ViewType.THREAD || !threading) {
                tvCount.setVisibility(View.GONE);
                ivThread.setVisibility(View.GONE);
            } else {
                tvCount.setText(NF.format(message.visible));
                ivThread.setVisibility(View.VISIBLE);
            }

            // Starred
            bindFlagged(message);

            // Message text preview
            tvPreview.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT, Typeface.ITALIC);
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
            }

            // Contact info
            boolean outgoing = (viewType != ViewType.THREAD && EntityFolder.isOutgoing(message.folderType));
            Address[] addresses = (outgoing ? message.to : message.from);
            ContactInfo info = ContactInfo.get(context, addresses, true);
            if (info == null) {
                Bundle aargs = new Bundle();
                aargs.putLong("id", message.id);
                aargs.putSerializable("addresses", addresses);

                new SimpleTask<ContactInfo>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        Address[] addresses = (Address[]) args.getSerializable("addresses");
                        ivAvatar.setVisibility(avatars ? View.INVISIBLE : View.GONE);
                        tvFrom.setText(MessageHelper.formatAddresses(addresses, !compact, false));
                    }

                    @Override
                    protected ContactInfo onExecute(Context context, Bundle args) {
                        Address[] addresses = (Address[]) args.getSerializable("addresses");
                        return ContactInfo.get(context, addresses, false);
                    }

                    @Override
                    protected void onExecuted(Bundle args, ContactInfo info) {
                        long id = args.getLong("id");
                        TupleMessageEx amessage = getMessage();
                        if (amessage == null || !amessage.id.equals(id))
                            return;

                        bindContactInfo(info, message);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.execute(context, owner, aargs, "message:avatar");
            } else
                bindContactInfo(info, message);

            if (message.avatar != null) {
                if (autoimages)
                    properties.setValue("images", message.id, true);
            }

            if (viewType == ViewType.THREAD) {
                boolean show_expanded = properties.getValue("expanded", message.id);
                if (show_expanded)
                    bindExpanded(message);
                else {
                    clearExpanded(message);
                    properties.setBody(message.id, null);
                }
            }
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

            ibSearchContact.setVisibility(View.GONE);
            ibNotifyContact.setVisibility(View.GONE);
            ibAddContact.setVisibility(View.GONE);

            tvFromExTitle.setVisibility(View.GONE);
            tvToTitle.setVisibility(View.GONE);
            tvReplyToTitle.setVisibility(View.GONE);
            tvCcTitle.setVisibility(View.GONE);
            tvBccTitle.setVisibility(View.GONE);
            tvIdentityTitle.setVisibility(View.GONE);
            tvTimeExTitle.setVisibility(View.GONE);
            tvSizeExTitle.setVisibility(View.GONE);

            tvFromEx.setVisibility(View.GONE);
            tvTo.setVisibility(View.GONE);
            tvReplyTo.setVisibility(View.GONE);
            tvCc.setVisibility(View.GONE);
            tvBcc.setVisibility(View.GONE);
            tvIdentity.setVisibility(View.GONE);
            tvTimeEx.setVisibility(View.GONE);
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
            btnDownloadAttachments.setVisibility(View.GONE);
            btnSaveAttachments.setVisibility(View.GONE);
            tvNoInternetAttachments.setVisibility(View.GONE);

            bnvActions.setVisibility(View.GONE);

            ibImages.setVisibility(View.GONE);
            ibFull.setVisibility(View.GONE);
            tvBody.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            tvNoInternetBody.setVisibility(View.GONE);
        }

        private void bindFlagged(TupleMessageEx message) {
            int flagged = (message.count - message.unflagged);
            ivFlagged.setImageResource(flagged > 0 ? R.drawable.baseline_star_24 : R.drawable.baseline_star_border_24);
            ivFlagged.setImageTintList(ColorStateList.valueOf(flagged > 0
                    ? message.color == null ? colorAccent : message.color
                    : textColorSecondary));
            ivFlagged.setVisibility(flags ? (message.uid == null ? View.INVISIBLE : View.VISIBLE) : View.GONE);
        }

        private void bindContactInfo(ContactInfo info, TupleMessageEx message) {
            if (info.hasPhoto())
                ivAvatar.setImageBitmap(info.getPhotoBitmap());
            else
                ivAvatar.setImageResource(R.drawable.baseline_person_24);
            ivAvatar.setVisibility(avatars ? View.VISIBLE : View.GONE);
            tvFrom.setText(info.getDisplayName(name_email));
        }

        private void bindExpanded(final TupleMessageEx message) {
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

            grpAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);

            bnvActions.setVisibility(View.VISIBLE);
            bnvActions.setTag(null);
            for (int i = 0; i < bnvActions.getMenu().size(); i++)
                bnvActions.getMenu().getItem(i).setVisible(false);

            ibImages.setVisibility(View.INVISIBLE);
            ibFull.setVisibility(hasWebView ? View.INVISIBLE : View.GONE);

            if (textSize != 0)
                tvBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

            tvBody.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);
            tvBody.setVisibility(View.INVISIBLE);

            // Addresses
            ivExpanderAddress.setImageLevel(show_addresses ? 0 /* less */ : 1 /* more */);

            String from = MessageHelper.formatAddresses(message.from);
            String to = MessageHelper.formatAddresses(message.to);
            String replyto = MessageHelper.formatAddresses(message.reply);
            String cc = MessageHelper.formatAddresses(message.cc);
            String bcc = MessageHelper.formatAddresses(message.bcc);

            boolean self = false;
            InternetAddress via = null;
            if (message.identityEmail != null)
                try {
                    via = new InternetAddress(message.identityEmail, message.identityName);
                    if (message.to != null) {
                        String v = MessageHelper.canonicalAddress(via.getAddress());
                        for (Address t : message.to) {
                            if (v.equals(MessageHelper.canonicalAddress(((InternetAddress) t).getAddress()))) {
                                self = true;
                                break;
                            }
                        }
                    }
                } catch (UnsupportedEncodingException ignored) {
                }

            tvFromExTitle.setVisibility(show_addresses && !TextUtils.isEmpty(from) ? View.VISIBLE : View.GONE);
            tvFromEx.setVisibility(show_addresses && !TextUtils.isEmpty(from) ? View.VISIBLE : View.GONE);
            tvFromEx.setText(from);

            tvToTitle.setVisibility(show_addresses && !TextUtils.isEmpty(to) ? View.VISIBLE : View.GONE);
            tvTo.setVisibility(show_addresses && !TextUtils.isEmpty(to) ? View.VISIBLE : View.GONE);
            tvTo.setText(to);
            tvToTitle.setTextColor(self ? textColorSecondary : colorWarning);
            tvTo.setTextColor(self ? textColorSecondary : colorWarning);

            tvReplyToTitle.setVisibility(show_addresses && !TextUtils.isEmpty(replyto) ? View.VISIBLE : View.GONE);
            tvReplyTo.setVisibility(show_addresses && !TextUtils.isEmpty(replyto) ? View.VISIBLE : View.GONE);
            tvReplyTo.setText(replyto);

            tvCcTitle.setVisibility(show_addresses && !TextUtils.isEmpty(cc) ? View.VISIBLE : View.GONE);
            tvCc.setVisibility(show_addresses && !TextUtils.isEmpty(cc) ? View.VISIBLE : View.GONE);
            tvCc.setText(cc);

            tvBccTitle.setVisibility(show_addresses && !TextUtils.isEmpty(bcc) ? View.VISIBLE : View.GONE);
            tvBcc.setVisibility(show_addresses && !TextUtils.isEmpty(bcc) ? View.VISIBLE : View.GONE);
            tvBcc.setText(bcc);

            tvIdentityTitle.setVisibility(show_addresses && via != null ? View.VISIBLE : View.GONE);
            tvIdentity.setVisibility(show_addresses && via != null ? View.VISIBLE : View.GONE);
            tvIdentity.setText(via == null ? null : MessageHelper.formatAddresses(new Address[]{via}));

            tvTimeExTitle.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvTimeEx.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvTimeEx.setText(DTF.format(message.received));

            if (!message.duplicate)
                tvSizeEx.setAlpha(message.content ? 1.0f : Helper.LOW_LIGHT);
            tvSizeExTitle.setVisibility(!show_addresses || message.size == null ? View.GONE : View.VISIBLE);
            tvSizeEx.setVisibility(!show_addresses || message.size == null ? View.GONE : View.VISIBLE);
            tvSizeEx.setText(message.size == null ? null : Helper.humanReadableByteCount(message.size, true));

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
            if (show_headers && message.headers != null) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(message.headers);
                int index = 0;
                for (String line : message.headers.split("\n")) {
                    if (line.length() > 0 && !Character.isWhitespace(line.charAt(0))) {
                        int colon = line.indexOf(':');
                        if (colon > 0)
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), index, index + colon, 0);
                    }
                    index += line.length() + 1;
                }

                tvHeaders.setText(ssb);
            } else
                tvHeaders.setText(null);

            // Attachments
            bindAttachments(message, properties.getAttachments(message.id));
            cowner.recreate();
            cowner.start();
            db.attachment().liveAttachments(message.id).observe(cowner, new Observer<List<EntityAttachment>>() {
                @Override
                public void onChanged(@Nullable List<EntityAttachment> attachments) {
                    bindAttachments(message, attachments);
                    loadText(message);
                }
            });

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
                    boolean hasJunk = false;
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

                    ActionData data = new ActionData();
                    data.hasJunk = hasJunk;
                    data.delete = (inTrash || !hasTrash);
                    data.message = message;
                    bnvActions.setTag(data);

                    bnvActions.getMenu().findItem(R.id.action_more).setVisible(!inOutbox);

                    bnvActions.getMenu().findItem(R.id.action_delete).setVisible(debug ||
                            (inTrash && (message.uid != null || message.msgid != null)) ||
                            (!inTrash && hasTrash && message.uid != null));
                    bnvActions.getMenu().findItem(R.id.action_delete).setTitle(data.delete ? R.string.title_delete : R.string.title_trash);

                    bnvActions.getMenu().findItem(R.id.action_move).setVisible(message.uid != null || inOutbox);
                    bnvActions.getMenu().findItem(R.id.action_move).setTitle(inOutbox ? R.string.title_folder_drafts : R.string.title_move);

                    bnvActions.getMenu().findItem(R.id.action_archive).setVisible(message.uid != null && (inJunk || (!inArchive && hasArchive)));
                    bnvActions.getMenu().findItem(R.id.action_archive).setTitle(inJunk ? R.string.title_folder_inbox : R.string.title_archive);

                    bnvActions.getMenu().findItem(R.id.action_reply).setEnabled(message.content);
                    bnvActions.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, sargs, "message:actions");

            // Message text
            pbBody.setVisibility(suitable || message.content ? View.VISIBLE : View.GONE);
            tvNoInternetBody.setVisibility(suitable || message.content ? View.GONE : View.VISIBLE);

            Spanned body = properties.getBody(message.id);
            tvBody.setText(body);
            tvBody.setMovementMethod(null);
            tvBody.setVisibility(View.VISIBLE);

            loadText(message);
        }

        private void bindAttachments(final TupleMessageEx message, @Nullable List<EntityAttachment> attachments) {
            if (attachments == null)
                attachments = new ArrayList<>();
            properties.setAttchments(message.id, attachments);

            boolean show_inline = properties.getValue("inline", message.id);
            Log.i("Show inline=" + show_inline);

            boolean has_inline = false;
            boolean download = false;
            boolean save = (attachments.size() > 1);
            boolean downloading = false;
            boolean calendar = false;
            List<EntityAttachment> a = new ArrayList<>();
            for (EntityAttachment attachment : attachments) {
                boolean inline = (TextUtils.isEmpty(attachment.name) ||
                        (attachment.isInline() && attachment.isImage()));
                if (inline)
                    has_inline = true;
                if (attachment.progress == null && !attachment.available)
                    download = true;
                if (!attachment.available)
                    save = false;
                if (attachment.progress != null)
                    downloading = true;
                if (show_inline || !inline)
                    a.add(attachment);

                if (attachment.available && "text/calendar".equals(attachment.type)) {
                    // https://tools.ietf.org/html/rfc5546
                    calendar = true;

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

                            DateFormat df = SimpleDateFormat.getDateTimeInstance();

                            VEvent event = icalendar.getEvents().get(0);

                            Summary summary = event.getSummary();

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

                            tvCalendarSummary.setText(summary == null ? null : summary.getValue());
                            tvCalendarSummary.setVisibility(summary == null ? View.GONE : View.VISIBLE);

                            tvCalendarStart.setText(start == null ? null : df.format(start.getTime()));
                            tvCalendarStart.setVisibility(start == null ? View.GONE : View.VISIBLE);

                            tvCalendarEnd.setText(end == null ? null : df.format(end.getTime()));
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
                            Helper.unexpectedError(context, owner, ex);
                        }
                    }.execute(context, owner, args, "message:calendar");
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
            btnDownloadAttachments.setVisibility(download && suitable ? View.VISIBLE : View.GONE);
            btnSaveAttachments.setVisibility(save ? View.VISIBLE : View.GONE);
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

        private void onActionCalendar(TupleMessageEx message, int action) {
            if (!Helper.isPro(context)) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                return;
            }

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putInt("action", action);

            new SimpleTask<File>() {
                @Override
                protected File onExecute(Context context, Bundle args) throws Throwable {
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

                            // https://tools.ietf.org/html/rfc5546#section-4.2.2
                            VEvent ev = new VEvent();
                            ev.setOrganizer(event.getOrganizer());
                            ev.setUid(event.getUid());
                            if (event.getSequence() != null)
                                ev.setSequence(event.getSequence());

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

                            ICalendar response = new ICalendar();
                            response.setMethod(Method.REPLY);
                            response.addEvent(ev);

                            File dir = new File(context.getFilesDir(), "temporary");
                            if (!dir.exists())
                                dir.mkdir();
                            File ics = new File(dir, "meeting.ics");
                            response.write(ics);

                            return ics;
                        }

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, File ics) {
                    Intent reply = new Intent(context, ActivityCompose.class)
                            .putExtra("action", "participation")
                            .putExtra("reference", args.getLong("id"))
                            .putExtra("ics", ics);
                    context.startActivity(reply);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:participation");
        }

        private TupleMessageEx getMessage() {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return null;

            return differ.getItem(pos);
        }

        @Override
        public void onClick(View view) {
            TupleMessageEx message = getMessage();
            if (message == null)
                return;

            if (view.getId() == R.id.ivSnoozed)
                onShowSnoozed(message);
            else if (view.getId() == R.id.ivFlagged)
                onToggleFlag(message);
            else if (view.getId() == R.id.ibSearchContact)
                onSearchContact(message);
            else if (view.getId() == R.id.ibNotifyContact)
                onNotifyContact(message);
            else if (view.getId() == R.id.ibAddContact)
                onAddContact(message);
            else if (viewType == ViewType.THREAD) {
                switch (view.getId()) {
                    case R.id.ivExpanderAddress:
                        onToggleAddresses(message);
                        break;
                    case R.id.btnDownloadAttachments:
                        onDownloadAttachments(message);
                        break;
                    case R.id.btnSaveAttachments:
                        onSaveAttachments(message);
                        break;
                    case R.id.ibImages:
                        onShowImages(message);
                        break;
                    case R.id.ibFull:
                        onShowFull(message);
                        break;
                    case R.id.btnCalendarAccept:
                    case R.id.btnCalendarDecline:
                    case R.id.btnCalendarMaybe:
                        onActionCalendar(message, view.getId());
                        break;
                    default:
                        onToggleMessage(message);
                }
            } else {
                vwRipple.setPressed(true);
                vwRipple.setPressed(false);

                if (EntityFolder.DRAFTS.equals(message.folderType) && message.visible == 1)
                    context.startActivity(
                            new Intent(context, ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", message.id));
                else {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_VIEW_THREAD)
                                    .putExtra("account", message.account)
                                    .putExtra("thread", message.thread)
                                    .putExtra("id", message.id)
                                    .putExtra("found", viewType == ViewType.SEARCH));
                }
            }
        }

        private void onShowSnoozed(TupleMessageEx message) {
            if (message.ui_snoozed != null) {
                DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
                DateFormat day = new SimpleDateFormat("E");
                Toast.makeText(context,
                        day.format(message.ui_snoozed) + " " + df.format(message.ui_snoozed),
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
            bindFlagged(message);

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

                        List<EntityMessage> messages = db.message().getMessageByThread(
                                message.account, message.thread, threading && thread ? null : id, null);
                        for (EntityMessage threaded : messages)
                            EntityOperation.queue(context, threaded, EntityOperation.FLAG, flagged);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:flag");
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

                    boolean outgoing;
                    if (message.identity == null || message.from == null || message.from.length == 0)
                        outgoing = EntityFolder.isOutgoing(folder.type);
                    else {
                        String from = ((InternetAddress) message.from[0]).getAddress();
                        EntityIdentity identity = db.identity().getIdentity(message.identity);
                        outgoing = MessageHelper.canonicalAddress(identity.email).equals(MessageHelper.canonicalAddress(from));
                    }

                    return (outgoing ? message.to : message.from);
                }

                @Override
                protected void onExecuted(Bundle args, Address[] addresses) {
                    String query = ((InternetAddress) addresses[0]).getAddress();
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_SEARCH)
                                    .putExtra("folder", -1L)
                                    .putExtra("query", query));
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:search");
        }

        @TargetApi(Build.VERSION_CODES.O)
        private void onNotifyContact(final TupleMessageEx message) {
            final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final InternetAddress from = (InternetAddress) message.from[0];
            final String channelId = "notification." + from.getAddress().toLowerCase();

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
                    if (!Helper.isPro(context)) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                        return;
                    }

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
                        edit.setDataAndType(lookupUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    } else {
                        edit.setAction(Intent.ACTION_INSERT);
                        edit.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    }
                }

                PackageManager pm = context.getPackageManager();
                if (edit.resolveActivity(pm) == null)
                    Toast.makeText(context, R.string.title_no_contacts, Toast.LENGTH_LONG).show();
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

                ivExpander.setImageLevel(expanded ? 0 /* less*/ : 1 /* more */);

                if (expanded) {
                    bindExpanded(message);
                    properties.scrollTo(getAdapterPosition());
                } else
                    clearExpanded(message);
            }
        }

        private void onToggleAddresses(TupleMessageEx message) {
            boolean addresses = !properties.getValue("addresses", message.id);
            properties.setValue("addresses", message.id, addresses);
            bindExpanded(message);
        }

        private void onDownloadAttachments(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage msg = db.message().getMessage(id);
                        if (msg != null)
                            for (EntityAttachment attachment : db.attachment().getAttachments(message.id))
                                if (attachment.progress == null && !attachment.available) {
                                    db.attachment().setProgress(attachment.id, 0);
                                    EntityOperation.queue(context, msg, EntityOperation.ATTACHMENT, attachment.id);
                                }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:attachment:download");
        }

        private void onSaveAttachments(TupleMessageEx message) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_STORE_ATTACHMENTS)
                            .putExtra("id", message.id));
        }

        private void onShowFull(final TupleMessageEx message) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (properties.getValue("confirmed", message.id) ||
                    prefs.getBoolean("show_html_confirmed", false)) {
                onShowFullConfirmed(message);
                return;
            }

            final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_ask_again, null);
            final TextView tvMessage = dview.findViewById(R.id.tvMessage);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            tvMessage.setText(context.getText(R.string.title_ask_show_html));

            new DialogBuilderLifecycle(context, owner)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            properties.setValue("confirmed", message.id, true);
                            if (cbNotAgain.isChecked())
                                prefs.edit().putBoolean("show_html_confirmed", true).apply();
                            onShowFullConfirmed(message);
                        }
                    })
                    .show();
        }

        private void onShowFullConfirmed(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<String>() {
                @Override
                protected String onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null || !message.content)
                        return null;

                    File file = message.getFile(context);
                    if (!file.exists())
                        return null;

                    String html = HtmlHelper.getHtmlEmbedded(context, id, Helper.readText(file));

                    // Remove viewport limitations
                    Document doc = Jsoup.parse(html);
                    for (Element meta : doc.select("meta").select("[name=viewport]")) {
                        String content = meta.attr("content");
                        String[] params = content.split(";");
                        if (params.length > 0) {
                            List<String> viewport = new ArrayList<>();
                            for (String param : params)
                                if (!param.toLowerCase().contains("maximum-scale") &&
                                        !param.toLowerCase().contains("user-scalable"))
                                    viewport.add(param.trim());

                            if (viewport.size() == 0)
                                meta.attr("content", "");
                            else
                                meta.attr("content", TextUtils.join(" ;", viewport) + ";");
                        }
                    }

                    return doc.html();
                }

                @Override
                protected void onExecuted(Bundle args, String html) {
                    long id = args.getLong("id");

                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(id))
                        return;

                    WebView webView = new WebView(context);
                    setupWebView(webView);

                    boolean show_images = properties.getValue("images", id);

                    WebSettings settings = webView.getSettings();
                    settings.setDefaultFontSize(Math.round(textSize));
                    settings.setDefaultFixedFontSize(Math.round(textSize));
                    settings.setLoadsImagesAutomatically(show_images);
                    settings.setBuiltInZoomControls(true);
                    settings.setDisplayZoomControls(false);

                    webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", null);

                    final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    dialog.setContentView(webView);

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

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:full");
        }

        private void setupWebView(WebView webView) {
            webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i("Open url=" + url);

                    Uri uri = Uri.parse(url);
                    if ("cid".equals(uri.getScheme()) || "data".equals(uri.getScheme()))
                        return false;

                    onOpenLink(uri, null);
                    return true;
                }
            });

            webView.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(
                        String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    Log.i("Download url=" + url + " mime type=" + mimetype);
                    Uri uri = Uri.parse(url);
                    Helper.view(context, owner, uri, true);
                }
            });

            webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    WebView.HitTestResult result = ((WebView) view).getHitTestResult();
                    if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                            result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        Log.i("Long press url=" + result.getExtra());

                        Uri uri = Uri.parse(result.getExtra());
                        Helper.view(context, owner, uri, true);

                        return true;
                    }
                    return false;
                }
            });

            WebSettings settings = webView.getSettings();
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setAllowFileAccess(false);

            if (monospaced)
                settings.setStandardFontFamily("monospace");
        }

        private void onShowImages(final TupleMessageEx message) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("show_images_confirmed", false)) {
                onShowImagesConfirmed(message);
                return;
            }

            final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_ask_again, null);
            final TextView tvMessage = dview.findViewById(R.id.tvMessage);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            tvMessage.setText(context.getText(R.string.title_ask_show_image));

            new DialogBuilderLifecycle(context, owner)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (cbNotAgain.isChecked())
                                prefs.edit().putBoolean("show_images_confirmed", true).apply();
                            onShowImagesConfirmed(message);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void onShowImagesConfirmed(final TupleMessageEx message) {
            properties.setValue("images", message.id, true);

            ibImages.setVisibility(View.GONE);

            loadText(message);

            // Download inline images
            Bundle args = new Bundle();
            args.putSerializable("message", message);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                        for (EntityAttachment attachment : attachments)
                            if (!attachment.available && !TextUtils.isEmpty(attachment.cid))
                                EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.id);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "show:images");
        }

        private void loadText(TupleMessageEx message) {
            if (message.content) {
                boolean show_images = properties.getValue("images", message.id);
                boolean show_quotes = properties.getValue("quotes", message.id);

                Bundle args = new Bundle();
                args.putSerializable("message", message);
                args.putBoolean("show_images", show_images);
                args.putBoolean("show_quotes", show_quotes);
                args.putInt("zoom", zoom);
                bodyTask.execute(context, owner, args, "message:body");
            }
        }

        private SimpleTask<SpannableStringBuilder> bodyTask = new SimpleTask<SpannableStringBuilder>() {
            @Override
            protected SpannableStringBuilder onExecute(final Context context, final Bundle args) throws IOException {
                final TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                final boolean show_images = args.getBoolean("show_images");
                boolean show_quotes = args.getBoolean("show_quotes");
                int zoom = args.getInt("zoom");

                if (message == null || !message.content)
                    return null;

                File file = message.getFile(context);
                if (!file.exists())
                    return null;

                String body = Helper.readText(file);

                if (!show_quotes) {
                    Document document = Jsoup.parse(body);
                    for (Element quote : document.select("blockquote"))
                        quote.html("&#8230;");
                    body = document.html();
                }

                String html = HtmlHelper.sanitize(context, body);
                if (debug)
                    html += "<pre>" + Html.escapeHtml(html) + "</pre>";

                Spanned spanned = HtmlHelper.fromHtml(html, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        Drawable image = HtmlHelper.decodeImage(source, message.id, show_images, tvBody);

                        ConstraintLayout.LayoutParams params =
                                (ConstraintLayout.LayoutParams) tvBody.getLayoutParams();
                        float width = context.getResources().getDisplayMetrics().widthPixels
                                - params.leftMargin - params.rightMargin;
                        if (image.getIntrinsicWidth() > width) {
                            float scale = width / image.getIntrinsicWidth();
                            image.setBounds(0, 0,
                                    Math.round(image.getIntrinsicWidth() * scale),
                                    Math.round(image.getIntrinsicHeight() * scale));
                        }

                        return image;
                    }
                }, null);

                SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
                QuoteSpan[] quoteSpans = builder.getSpans(0, builder.length(), QuoteSpan.class);
                for (QuoteSpan quoteSpan : quoteSpans) {
                    builder.setSpan(
                            new StyledQuoteSpan(colorPrimary),
                            builder.getSpanStart(quoteSpan),
                            builder.getSpanEnd(quoteSpan),
                            builder.getSpanFlags(quoteSpan));
                    builder.removeSpan(quoteSpan);
                }

                if (!show_quotes) {
                    final int px = Helper.dp2pixels(context, 24 + (zoom) * 8);

                    StyledQuoteSpan[] squotes = builder.getSpans(0, builder.length(), StyledQuoteSpan.class);
                    for (StyledQuoteSpan squote : squotes)
                        builder.setSpan(new DynamicDrawableSpan() {
                                            @Override
                                            public Drawable getDrawable() {
                                                Drawable d = context.getDrawable(R.drawable.baseline_format_quote_24);
                                                d.setTint(colorAccent);
                                                d.setBounds(0, 0, px, px);
                                                return d;
                                            }
                                        },
                                builder.getSpanStart(squote),
                                builder.getSpanEnd(squote),
                                builder.getSpanFlags(squote));
                }

                args.putBoolean("has_images", builder.getSpans(0, body.length(), ImageSpan.class).length > 0);

                return builder;
            }

            @Override
            protected void onExecuted(Bundle args, SpannableStringBuilder body) {
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                properties.setBody(message.id, body);

                TupleMessageEx amessage = getMessage();
                if (amessage == null || !amessage.id.equals(message.id))
                    return;

                boolean show_expanded = properties.getValue("expanded", message.id);
                if (!show_expanded)
                    return;

                boolean has_images = args.getBoolean("has_images");
                boolean show_images = properties.getValue("images", message.id);

                ibFull.setVisibility(hasWebView ? View.VISIBLE : View.GONE);
                ibImages.setVisibility(has_images && !show_images ? View.VISIBLE : View.GONE);

                tvBody.setText(body);
                tvBody.setTextIsSelectable(false);
                tvBody.setTextIsSelectable(true);
                tvBody.setMovementMethod(new TouchHandler(message));

                pbBody.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(context, owner, ex);
            }
        };

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
                            Uri uri = Uri.parse(image[0].getSource());
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
                    if (image.length > 0 && image[0].getSource() != null) {
                        onOpenImage(image[0].getDrawable());
                        return true;
                    }

                    DynamicDrawableSpan[] ddss = buffer.getSpans(off, off, DynamicDrawableSpan.class);
                    if (ddss.length > 0) {
                        properties.setValue("quotes", message.id, true);
                        loadText(message);
                    }
                }

                return super.onTouchEvent(widget, buffer, event);
            }
        }

        private void onOpenLink(final Uri uri, String title) {
            Log.i("Opening uri=" + uri);

            if (BuildConfig.APPLICATION_ID.equals(uri.getHost()) && "/activate/".equals(uri.getPath())) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_ACTIVATE_PRO)
                                .putExtra("uri", uri));
            } else {
                if ("cid".equals(uri.getScheme()))
                    return;

                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean paranoid = prefs.getBoolean("paranoid", true);

                final Uri _uri;
                if (paranoid && !uri.isOpaque()) {
                    // https://en.wikipedia.org/wiki/UTM_parameters
                    Uri.Builder builder = new Uri.Builder();

                    String scheme = uri.getScheme();
                    if (!TextUtils.isEmpty(scheme))
                        builder.scheme(scheme);

                    String authority = uri.getEncodedAuthority();
                    if (!TextUtils.isEmpty(authority))
                        builder.encodedAuthority(authority);

                    String path = uri.getEncodedPath();
                    if (!TextUtils.isEmpty(path))
                        builder.encodedPath(path);

                    for (String key : uri.getQueryParameterNames())
                        if (!PARANOID_QUERY.contains(key.toLowerCase()))
                            for (String value : uri.getQueryParameters(key))
                                builder.appendQueryParameter(key, value);

                    String fragment = uri.getEncodedFragment();
                    if (!TextUtils.isEmpty(fragment))
                        builder.encodedFragment(fragment);

                    _uri = builder.build();

                    Log.i("Source uri=" + uri);
                    Log.i("Target uri=" + _uri);
                } else
                    _uri = uri;

                View view = LayoutInflater.from(context).inflate(R.layout.dialog_open_link, null);
                TextView tvTitle = view.findViewById(R.id.tvTitle);
                final EditText etLink = view.findViewById(R.id.etLink);
                TextView tvInsecure = view.findViewById(R.id.tvInsecure);
                final TextView tvOwner = view.findViewById(R.id.tvOwner);
                final Group grpOwner = view.findViewById(R.id.grpOwner);

                tvTitle.setText(title);
                tvTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);

                etLink.setText(_uri.toString());
                tvInsecure.setVisibility("http".equals(_uri.getScheme()) ? View.VISIBLE : View.GONE);
                grpOwner.setVisibility(View.GONE);

                if (paranoid) {
                    Bundle args = new Bundle();
                    args.putParcelable("uri", _uri);

                    new SimpleTask<String>() {
                        @Override
                        protected void onPreExecute(Bundle args) {
                            tvOwner.setText("…");
                            grpOwner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected String onExecute(Context context, Bundle args) throws Throwable {
                            Uri uri = args.getParcelable("uri");
                            return IPInfo.getOrganization(uri);
                        }

                        @Override
                        protected void onExecuted(Bundle args, String organization) {
                            tvOwner.setText(organization == null ? "?" : organization);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            if (ex instanceof UnknownHostException)
                                grpOwner.setVisibility(View.GONE);
                            else
                                tvOwner.setText(ex.getMessage());
                        }
                    }.execute(context, owner, args, "link:domain");
                }

                new DialogBuilderLifecycle(context, owner)
                        .setView(view)
                        .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse(etLink.getText().toString());
                                Helper.view(context, owner, uri, false);
                            }
                        })
                        .setNeutralButton(R.string.title_browse, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse(etLink.getText().toString());
                                Helper.view(context, owner, uri, true);
                            }
                        })
                        .setNegativeButton(R.string.title_no, null)
                        .show();
            }
        }

        private void onOpenImage(Drawable drawable) {
            PhotoView pv = new PhotoView(context);
            pv.setImageDrawable(drawable);

            final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(pv);

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

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            ActionData data = (ActionData) bnvActions.getTag();
            if (data == null)
                return false;

            switch (item.getItemId()) {
                case R.id.action_more:
                    onActionMore(data);
                    return true;
                case R.id.action_delete:
                    onActionDelete(data);
                    return true;
                case R.id.action_move:
                    if (EntityFolder.OUTBOX.equals(data.message.folderType))
                        onnActionMoveOutbox(data);
                    else
                        onActionMove(data, false);
                    return true;
                case R.id.action_archive:
                    if (EntityFolder.JUNK.equals(data.message.folderType))
                        onActionMoveJunk(data);
                    else
                        onActionArchive(data);
                    return true;
                case R.id.action_reply:
                    onActionReplyMenu(data);
                    return true;
                default:
                    return false;
            }
        }

        private void onMenuForward(final ActionData data) {
            Intent forward = new Intent(context, ActivityCompose.class)
                    .putExtra("action", "forward")
                    .putExtra("reference", data.message.id);
            context.startActivity(forward);
        }

        private void onMenuUnseen(final ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

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

                        EntityOperation.queue(context, message, EntityOperation.SEEN, false);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void ignored) {
                    properties.setValue("expanded", data.message.id, false);
                    notifyDataSetChanged();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:unseen");
        }

        private void onMenuColoredStar(final ActionData data) {
            Intent color = new Intent(ActivityView.ACTION_COLOR);
            color.putExtra("id", data.message.id);
            color.putExtra("color", data.message.color == null ? Color.TRANSPARENT : data.message.color);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(color);
        }

        private void onMenuDelete(final ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    db.message().deleteMessage(id);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:delete");
        }

        private void onMenuJunk(final ActionData data) {
            String who = MessageHelper.formatAddresses(data.message.from);
            new DialogBuilderLifecycle(context, owner)
                    .setMessage(context.getString(R.string.title_ask_spam_who, who))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putLong("id", data.message.id);

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
                                    Helper.unexpectedError(context, owner, ex);
                                }
                            }.execute(context, owner, args, "message:spam");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void onMenuDecrypt(ActionData data) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_DECRYPT)
                            .putExtra("id", data.message.id));
        }

        private void onMenuResync(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

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
                        db.message().deleteMessage(id);

                        EntityOperation.sync(context, message.folder, false);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:share");
        }

        private void onMenuCreateRule(ActionData data) {
            Intent rule = new Intent(ActivityView.ACTION_EDIT_RULE);
            rule.putExtra("account", data.message.account);
            rule.putExtra("folder", data.message.folder);
            if (data.message.from != null && data.message.from.length > 0)
                rule.putExtra("sender", ((InternetAddress) data.message.from[0]).getAddress());
            if (data.message.to != null && data.message.to.length > 0)
                rule.putExtra("recipient", ((InternetAddress) data.message.to[0]).getAddress());
            if (!TextUtils.isEmpty(data.message.subject))
                rule.putExtra("subject", data.message.subject);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(rule);
        }

        private void onMenuManageKeywords(ActionData data) {
            Bundle args = new Bundle();
            args.putSerializable("message", data.message);

            new SimpleTask<EntityFolder>() {
                @Override
                protected EntityFolder onExecute(Context context, Bundle args) {
                    EntityMessage message = (EntityMessage) args.getSerializable("message");
                    return DB.getInstance(context).folder().getFolder(message.folder);
                }

                @Override
                protected void onExecuted(final Bundle args, EntityFolder folder) {
                    EntityMessage message = (EntityMessage) args.getSerializable("message");

                    List<String> keywords = Arrays.asList(message.keywords);

                    final List<String> items = new ArrayList<>(keywords);
                    for (String keyword : folder.keywords)
                        if (!items.contains(keyword))
                            items.add(keyword);

                    Collections.sort(items);

                    final boolean selected[] = new boolean[items.size()];
                    final boolean dirty[] = new boolean[items.size()];
                    for (int i = 0; i < selected.length; i++) {
                        selected[i] = keywords.contains(items.get(i));
                        dirty[i] = false;
                    }

                    new DialogBuilderLifecycle(context, owner)
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
                                    if (!Helper.isPro(context)) {
                                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                                        lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                                        return;
                                    }

                                    args.putStringArray("keywords", items.toArray(new String[0]));
                                    args.putBooleanArray("selected", selected);
                                    args.putBooleanArray("dirty", dirty);

                                    new SimpleTask<Void>() {
                                        @Override
                                        protected Void onExecute(Context context, Bundle args) {
                                            EntityMessage message = (EntityMessage) args.getSerializable("message");
                                            String[] keywords = args.getStringArray("keywords");
                                            boolean[] selected = args.getBooleanArray("selected");
                                            boolean[] dirty = args.getBooleanArray("dirty");

                                            DB db = DB.getInstance(context);
                                            try {
                                                db.beginTransaction();

                                                for (int i = 0; i < selected.length; i++)
                                                    if (dirty[i])
                                                        EntityOperation.queue(context, message, EntityOperation.KEYWORD, keywords[i], selected[i]);

                                                db.setTransactionSuccessful();
                                            } finally {
                                                db.endTransaction();
                                            }

                                            return null;
                                        }

                                        @Override
                                        protected void onException(Bundle args, Throwable ex) {
                                            Helper.unexpectedError(context, owner, ex);
                                        }
                                    }.execute(context, owner, args, "message:keywords:managa");
                                }
                            })
                            .setNeutralButton(R.string.title_add, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    View view = LayoutInflater.from(context).inflate(R.layout.dialog_keyword, null);
                                    final EditText etKeyword = view.findViewById(R.id.etKeyword);
                                    etKeyword.setText(null);
                                    new DialogBuilderLifecycle(context, owner)
                                            .setView(view)
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (!Helper.isPro(context)) {
                                                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                                                        lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                                                        return;
                                                    }

                                                    String keyword = Helper.sanitizeKeyword(etKeyword.getText().toString());
                                                    if (!TextUtils.isEmpty(keyword)) {
                                                        args.putString("keyword", keyword);

                                                        new SimpleTask<Void>() {
                                                            @Override
                                                            protected Void onExecute(Context context, Bundle args) {
                                                                EntityMessage message = (EntityMessage) args.getSerializable("message");
                                                                String keyword = args.getString("keyword");

                                                                EntityOperation.queue(context, message, EntityOperation.KEYWORD, keyword, true);
                                                                return null;
                                                            }

                                                            @Override
                                                            protected void onException(Bundle args, Throwable ex) {
                                                                Helper.unexpectedError(context, owner, ex);
                                                            }
                                                        }.execute(context, owner, args, "message:keyword:add");
                                                    }
                                                }
                                            }).show();
                                }
                            })
                            .show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:keywords");
        }

        private void onMenuShare(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

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

                    String html = HtmlHelper.getText(Helper.readText(file));

                    return new String[]{from, message.subject, html};
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
                        Toast.makeText(context, R.string.title_no_viewer, Toast.LENGTH_LONG).show();
                    else
                        context.startActivity(share);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:share");
        }

        private void onMenuPrint(final ActionData data) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("print_html_confirmed", false)) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_PRINT)
                                .putExtra("id", data.message.id));
                return;
            }

            final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_ask_again, null);
            final TextView tvMessage = dview.findViewById(R.id.tvMessage);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            tvMessage.setText(context.getText(R.string.title_ask_show_html));

            new DialogBuilderLifecycle(context, owner)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (cbNotAgain.isChecked())
                                prefs.edit().putBoolean("print_html_confirmed", true).apply();
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_PRINT)
                                            .putExtra("id", data.message.id));
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void onMenuShowHeaders(ActionData data) {
            boolean show_headers = !properties.getValue("headers", data.message.id);
            properties.setValue("headers", data.message.id, show_headers);
            if (show_headers && data.message.headers == null) {
                grpHeaders.setVisibility(View.VISIBLE);
                if (suitable)
                    pbHeaders.setVisibility(View.VISIBLE);
                else
                    tvNoInternetHeaders.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", data.message.id);

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
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.execute(context, owner, args, "message:headers");
            } else
                notifyDataSetChanged();
        }

        private void onMenuRaw(ActionData data) {
            if (data.message.raw == null) {
                Bundle args = new Bundle();
                args.putLong("id", data.message.id);

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
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.execute(context, owner, args, "message:raw");
            } else {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_STORE_RAW)
                                .putExtra("id", data.message.id));
            }
        }

        private void onActionMore(final ActionData data) {
            boolean show_headers = properties.getValue("headers", data.message.id);

            View anchor = bnvActions.findViewById(R.id.action_more);
            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, anchor);
            popupMenu.inflate(R.menu.menu_message);

            popupMenu.getMenu().findItem(R.id.menu_forward).setEnabled(data.message.content);

            popupMenu.getMenu().findItem(R.id.menu_unseen).setEnabled(data.message.uid != null);
            popupMenu.getMenu().findItem(R.id.menu_flag_color).setEnabled(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_copy).setEnabled(data.message.uid != null);
            popupMenu.getMenu().findItem(R.id.menu_delete).setVisible(debug);

            popupMenu.getMenu().findItem(R.id.menu_junk).setEnabled(data.message.uid != null);
            popupMenu.getMenu().findItem(R.id.menu_junk).setVisible(
                    data.hasJunk && !EntityFolder.JUNK.equals(data.message.folderType));

            popupMenu.getMenu().findItem(R.id.menu_share).setEnabled(data.message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setEnabled(hasWebView && data.message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setVisible(Helper.canPrint(context));

            popupMenu.getMenu().findItem(R.id.menu_show_headers).setChecked(show_headers);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setEnabled(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_raw).setEnabled(
                    data.message.uid != null && (data.message.raw == null || data.message.raw));
            popupMenu.getMenu().findItem(R.id.menu_raw).setTitle(
                    data.message.raw == null || !data.message.raw ? R.string.title_raw_download : R.string.title_raw_save);

            popupMenu.getMenu().findItem(R.id.menu_manage_keywords).setEnabled(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_decrypt).setEnabled(
                    data.message.content && data.message.to != null && data.message.to.length > 0);

            popupMenu.getMenu().findItem(R.id.menu_resync).setEnabled(data.message.uid != null);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    switch (target.getItemId()) {
                        case R.id.menu_forward:
                            onMenuForward(data);
                            return true;
                        case R.id.menu_unseen:
                            onMenuUnseen(data);
                            return true;
                        case R.id.menu_flag_color:
                            onMenuColoredStar(data);
                            return true;
                        case R.id.menu_copy:
                            onActionMove(data, true);
                            return true;
                        case R.id.menu_delete:
                            // For emergencies
                            onMenuDelete(data);
                            return true;
                        case R.id.menu_junk:
                            onMenuJunk(data);
                            return true;
                        case R.id.menu_decrypt:
                            onMenuDecrypt(data);
                            return true;
                        case R.id.menu_resync:
                            onMenuResync(data);
                            return true;
                        case R.id.menu_create_rule:
                            onMenuCreateRule(data);
                            return true;
                        case R.id.menu_manage_keywords:
                            onMenuManageKeywords(data);
                            return true;
                        case R.id.menu_share:
                            onMenuShare(data);
                            return true;
                        case R.id.menu_print:
                            onMenuPrint(data);
                            return true;
                        case R.id.menu_show_headers:
                            onMenuShowHeaders(data);
                            return true;
                        case R.id.menu_raw:
                            onMenuRaw(data);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }

        private void onActionDelete(final ActionData data) {
            if (data.delete) {
                // No trash or is trash
                new DialogBuilderLifecycle(context, owner)
                        .setMessage(R.string.title_ask_delete)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle args = new Bundle();
                                args.putLong("id", data.message.id);

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

                                            if (EntityFolder.OUTBOX.equals(folder.type)) {
                                                db.message().deleteMessage(id);

                                                db.folder().setFolderError(message.folder, null);
                                                db.identity().setIdentityError(message.identity, null);

                                                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                nm.cancel("send", message.identity.intValue());
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
                                        Helper.unexpectedError(context, owner, ex);
                                    }
                                }.execute(context, owner, args, "message:delete");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            } else
                properties.move(data.message.id, EntityFolder.TRASH, true);
        }

        private void onActionMove(final ActionData data, final boolean copy) {
            final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_folder_select, null);
            final RecyclerView rvFolder = dview.findViewById(R.id.rvFolder);
            final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

            final Dialog dialog = new DialogBuilderLifecycle(context, owner)
                    .setTitle(copy ? R.string.title_copy_to : R.string.title_move_to_folder)
                    .setView(dview)
                    .create();

            rvFolder.setHasFixedSize(false);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            rvFolder.setLayoutManager(llm);

            final AdapterFolder adapter = new AdapterFolder(context, owner, data.message.account,
                    new AdapterFolder.IFolderSelectedListener() {
                        @Override
                        public void onFolderSelected(TupleFolderEx folder) {
                            dialog.dismiss();

                            if (copy) {
                                Bundle args = new Bundle();
                                args.putLong("id", data.message.id);
                                args.putLong("target", folder.id);

                                new SimpleTask<Void>() {
                                    @Override
                                    protected Void onExecute(Context context, Bundle args) {
                                        long id = args.getLong("id");
                                        long target = args.getLong("target");

                                        DB db = DB.getInstance(context);
                                        try {
                                            db.beginTransaction();

                                            EntityMessage message = db.message().getMessage(id);
                                            if (message == null)
                                                return null;

                                            EntityOperation.queue(context, message, EntityOperation.COPY, target);

                                            db.setTransactionSuccessful();
                                        } finally {
                                            db.endTransaction();
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onException(Bundle args, Throwable ex) {
                                        Helper.unexpectedError(context, owner, ex);
                                    }
                                }.execute(context, owner, args, "message:copy");
                            } else
                                properties.move(data.message.id, folder.name, false);
                        }
                    });

            rvFolder.setAdapter(adapter);

            rvFolder.setVisibility(View.GONE);
            pbWait.setVisibility(View.VISIBLE);
            dialog.show();

            Bundle args = new Bundle();
            args.putLong("account", data.message.account);

            new SimpleTask<List<TupleFolderEx>>() {
                @Override
                protected List<TupleFolderEx> onExecute(Context context, Bundle args) {
                    long account = args.getLong("account");

                    DB db = DB.getInstance(context);
                    return db.folder().getFoldersEx(account);
                }

                @Override
                protected void onExecuted(final Bundle args, List<TupleFolderEx> folders) {
                    if (folders == null)
                        folders = new ArrayList<>();

                    adapter.setDisabled(Arrays.asList(data.message.folder));
                    adapter.set(folders);
                    pbWait.setVisibility(View.GONE);
                    rvFolder.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:move:list");
        }

        private void onnActionMoveOutbox(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

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

                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel("send", message.identity.intValue());

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:move:draft");
        }

        private void onActionArchive(ActionData data) {
            properties.move(data.message.id, EntityFolder.ARCHIVE, true);
        }

        private void onActionMoveJunk(ActionData data) {
            properties.move(data.message.id, EntityFolder.INBOX, true);
        }

        private void onActionReplyMenu(final ActionData data) {
            Bundle args = new Bundle();
            args.putSerializable("message", data.message);

            new SimpleTask<EntityIdentity>() {
                @Override
                protected EntityIdentity onExecute(Context context, Bundle args) {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                    if (message.identity == null)
                        return null;

                    DB db = DB.getInstance(context);
                    return db.identity().getIdentity(message.identity);
                }

                @Override
                protected void onExecuted(Bundle args, EntityIdentity identity) {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(message.id))
                        return;

                    String via = (identity == null ? null : MessageHelper.canonicalAddress(identity.email));
                    Address[] recipients = data.message.getAllRecipients(via);

                    if (recipients.length == 0 &&
                            data.message.list_post == null &&
                            data.message.receipt_to == null &&
                            (answers == 0 && Helper.isPro(context))) {
                        onMenuReply(data, "reply");
                        return;
                    }

                    View anchor = bnvActions.findViewById(R.id.action_reply);
                    PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, anchor);
                    popupMenu.inflate(R.menu.menu_reply);
                    popupMenu.getMenu().findItem(R.id.menu_reply_to_all).setVisible(recipients.length > 0);
                    popupMenu.getMenu().findItem(R.id.menu_reply_list).setVisible(data.message.list_post != null);
                    popupMenu.getMenu().findItem(R.id.menu_reply_receipt).setVisible(data.message.receipt_to != null);
                    popupMenu.getMenu().findItem(R.id.menu_reply_answer).setVisible(answers != 0 || !Helper.isPro(context));

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem target) {
                            switch (target.getItemId()) {
                                case R.id.menu_reply_to_sender:
                                    onMenuReply(data, "reply");
                                    return true;
                                case R.id.menu_reply_to_all:
                                    onMenuReply(data, "reply_all");
                                    return true;
                                case R.id.menu_reply_list:
                                    onMenuReply(data, "list");
                                    return true;
                                case R.id.menu_reply_receipt:
                                    onMenuReply(data, "receipt");
                                    return true;
                                case R.id.menu_reply_answer:
                                    onMenuAnswer(data);
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
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "message:reply");
        }

        private void onMenuReply(final ActionData data, String action) {
            Intent reply = new Intent(context, ActivityCompose.class)
                    .putExtra("action", action)
                    .putExtra("reference", data.message.id);
            context.startActivity(reply);
        }

        private void onMenuAnswer(final ActionData data) {
            new SimpleTask<List<EntityAnswer>>() {
                @Override
                protected List<EntityAnswer> onExecute(Context context, Bundle args) {
                    return DB.getInstance(context).answer().getAnswers(false);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAnswer> answers) {
                    if (answers == null || answers.size() == 0) {
                        Snackbar snackbar = Snackbar.make(
                                view,
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
                                if (!Helper.isPro(context)) {
                                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                                    lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                                    return true;
                                }

                                context.startActivity(new Intent(context, ActivityCompose.class)
                                        .putExtra("action", "reply")
                                        .putExtra("reference", data.message.id)
                                        .putExtra("answer", (long) target.getItemId()));
                                return true;
                            }
                        });

                        popupMenu.show();
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, new Bundle(), "message:answer");
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
            return new ItemDetailsMessage(this);
        }

        Long getKey() {
            return getKeyAtPosition(getAdapterPosition());
        }

        private class OriginalMessage {
            String html;
            boolean has_images;
        }

        private class ActionData {
            boolean hasJunk;
            boolean delete;
            TupleMessageEx message;
        }
    }

    AdapterMessage(Context context, LifecycleOwner owner,
                   ViewType viewType, boolean compact, int zoom, String sort, boolean filter_duplicates, final IProperties properties) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.TF = Helper.getTimeInstance(context, SimpleDateFormat.SHORT);

        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        this.viewType = viewType;
        this.compact = compact;
        this.zoom = zoom;
        this.sort = sort;
        this.filter_duplicates = filter_duplicates;
        this.suitable = ConnectionHelper.getNetworkState(context).isSuitable();
        this.properties = properties;

        this.dp36 = Helper.dp2pixels(context, 36);
        this.colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
        this.colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        this.colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        this.colorUnread = Helper.resolveColor(context, R.attr.colorUnread);

        this.hasWebView = Helper.hasWebView(context);
        this.contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        this.textSize = Helper.getTextSize(context, zoom);

        this.date = prefs.getBoolean("date", true);
        this.threading = prefs.getBoolean("threading", true);
        this.avatars = (prefs.getBoolean("avatars", true) ||
                prefs.getBoolean("identicons", false));
        this.name_email = prefs.getBoolean("name_email", !compact);
        this.subject_italic = prefs.getBoolean("subject_italic", true);
        this.flags = prefs.getBoolean("flags", true);
        this.preview = prefs.getBoolean("preview", false);
        this.attachments_alt = prefs.getBoolean("attachments_alt", false);
        this.monospaced = prefs.getBoolean("monospaced", false);
        this.autoimages = (this.contacts && prefs.getBoolean("autoimages", false));
        this.authentication = prefs.getBoolean("authentication", false);
        this.debug = prefs.getBoolean("debug", false);

        AsyncDifferConfig<TupleMessageEx> config = new AsyncDifferConfig.Builder<>(DIFF_CALLBACK)
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
    }

    void gotoTop() {
        properties.scrollTo(0);
        this.gotoTop = true;
    }

    void submitList(PagedList<TupleMessageEx> list) {
        differ.submitList(list);
    }

    PagedList<TupleMessageEx> getCurrentList() {
        return differ.getCurrentList();
    }

    void setCompact(boolean compact) {
        if (this.compact != compact) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            this.compact = compact;
            this.name_email = prefs.getBoolean("name_email", !compact);
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

    void setFilterDuplicates(boolean filter_duplicates) {
        if (this.filter_duplicates != filter_duplicates) {
            this.filter_duplicates = filter_duplicates;
            notifyDataSetChanged();
        }
    }

    void checkInternet() {
        boolean suitable = ConnectionHelper.getNetworkState(context).isSuitable();
        if (this.suitable != suitable) {
            this.suitable = suitable;
            notifyDataSetChanged();
        }
    }

    void setAnswerCount(int answers) {
        this.answers = answers;
        Log.i("Answer count=" + answers);
    }

    @Override
    public int getItemViewType(int position) {
        return (compact ? R.layout.item_message_compact : R.layout.item_message_normal);
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
                    return prev.uiEquals(next);
                }
            };

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleMessageEx message = differ.getItem(position);
        if (message == null)
            holder.clear();
        else {
            holder.bindTo(message);
            holder.wire();
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.cowner.stop();
        holder.powner.recreate();
    }

    void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    int getPositionForKey(long key) {
        PagedList<TupleMessageEx> messages = getCurrentList();
        if (messages != null)
            for (int i = 0; i < messages.size(); i++) {
                TupleMessageEx message = messages.get(i);
                if (message != null && message.id.equals(key)) {
                    Log.i("Position=" + i + " @Key=" + key);
                    return i;
                }
            }
        Log.i("Position=" + RecyclerView.NO_POSITION + " @Key=" + key);
        return RecyclerView.NO_POSITION;
    }

    TupleMessageEx getItemAtPosition(int pos) {
        PagedList<TupleMessageEx> list = getCurrentList();
        if (list != null && pos >= 0 && pos < list.size()) {
            TupleMessageEx message = list.get(pos);
            Long key = (message == null ? null : message.id);
            Log.i("Item=" + key + " @Position=" + pos);
            return message;
        } else {
            Log.i("Item=" + null + " @Position=" + pos);
            return null;
        }
    }

    TupleMessageEx getItemForKey(long key) {
        PagedList<TupleMessageEx> messages = getCurrentList();
        if (messages != null)
            for (int i = 0; i < messages.size(); i++) {
                TupleMessageEx message = messages.get(i);
                if (message != null && message.id.equals(key)) {
                    Log.i("Item=" + message.id + " @Key=" + key);
                    return message;
                }
            }
        Log.i("Item=" + null + " @Key=" + key);
        return null;
    }

    Long getKeyAtPosition(int pos) {
        TupleMessageEx message = getItemAtPosition(pos);
        Long key = (message == null ? null : message.id);
        Log.i("Key=" + key + " @Position=" + pos);
        return key;
    }

    interface IProperties {
        void setValue(String name, long id, boolean enabled);

        boolean getValue(String name, long id);

        void setBody(long id, Spanned body);

        Spanned getBody(long id);

        void setAttchments(long id, List<EntityAttachment> attachments);

        List<EntityAttachment> getAttachments(long id);

        void scrollTo(int pos);

        void scrollBy(int dx, int dy);

        void move(long id, String target, boolean type);

        void finish();
    }
}
