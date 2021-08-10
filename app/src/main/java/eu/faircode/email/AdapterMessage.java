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

import static android.app.Activity.RESULT_OK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF;
import static androidx.webkit.WebSettingsCompat.FORCE_DARK_ON;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.RemoteAction;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.os.LocaleList;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
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
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.textclassifier.ConversationAction;
import android.view.textclassifier.ConversationActions;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.DialogFragment;
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
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
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
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Attendee;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.RawProperty;
import biweekly.util.ICalDate;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.ViewHolder> {
    private Fragment parentFragment;
    private String type;
    private boolean found;
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
    private SharedPreferences prefs;
    private boolean accessibility;

    private int dp1;
    private int dp12;
    private int dp60;

    private boolean suitable;
    private boolean unmetered;

    private int colorAccent;
    private int textColorPrimary;
    private int textColorSecondary;
    private int textColorTertiary;
    private int textColorLink;
    private int colorUnread;
    private int colorRead;
    private int colorSubject;
    private int colorVerified;
    private int colorEncrypt;
    private int colorSeparator;
    private int colorWarning;
    private int colorError;
    private int colorControlNormal;

    private boolean hasWebView;
    private boolean pin;
    private boolean contacts;
    private float textSize;

    private boolean date;
    private boolean cards;
    private boolean shadow_unread;
    private boolean threading;
    private boolean threading_unread;
    private boolean indentation;
    private boolean avatars;
    private boolean color_stripe;
    private boolean check_authentication;
    private boolean check_reply_domain;
    private boolean check_mx;
    private boolean check_blocklist;

    private MessageHelper.AddressFormat email_format;
    private boolean prefer_contact;
    private boolean only_contact;
    private boolean distinguish_contacts;
    private boolean show_recipients;
    private Float font_size_sender;
    private Float font_size_subject;
    private boolean subject_top;
    private boolean subject_italic;
    private String subject_ellipsize;

    private boolean keywords_header;
    private boolean labels_header;
    private boolean flags;
    private boolean flags_background;
    private boolean preview;
    private boolean preview_italic;
    private int preview_lines;
    private int message_zoom;
    private boolean attachments_alt;
    private boolean thumbnails;
    private boolean contrast;
    private boolean monospaced;
    private boolean inline;
    private boolean collapse_quotes;
    private boolean authentication;
    private boolean authentication_indicator;

    private boolean autoclose_unseen;

    private boolean language_detection;
    private List<String> languages;
    private static boolean debug;

    private boolean gotoTop = false;
    private Integer gotoPos = null;
    private boolean firstClick = false;
    private AsyncPagedListDiffer<TupleMessageEx> differ;
    private Map<Long, Integer> keyPosition = new HashMap<>();
    private Map<Integer, Long> positionKey = new HashMap<>();
    private SelectionTracker<Long> selectionTracker = null;

    private RecyclerView rv = null;
    private Parcelable savedState = null;

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    private NumberFormat NF = NumberFormat.getNumberInstance();
    private DateFormat TF;
    private DateFormat DTF;

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(2, "differ");

    private static final int MAX_RECIPIENTS_COMPACT = 3;
    private static final int MAX_RECIPIENTS_NORMAL = 7;
    private static final int MAX_QUOTE_LEVEL = 3;
    private static final int MAX_TRANSLATABLE_TEXT_SIZE = 50 * 1024;

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener,
            View.OnLayoutChangeListener {
        private ViewCardOptional card;
        private View view;
        private View header;

        private View vwColor;
        private ImageButton ibExpander;
        private ImageView ibFlagged;
        private ImageButton ibAvatar;
        private ImageButton ibVerified;
        private ImageButton ibAuth;
        private ImageButton ibPriority;
        private ImageView ivImportance;
        private ImageView ivSigned;
        private ImageView ivEncrypted;
        private TextView tvFrom;
        private TextView tvSize;
        private TextView tvTime;
        private ImageView ivType;
        private ImageView ivFound;
        private ImageView ivClassified;
        private ImageButton ibSnoozed;
        private ImageView ivAnswered;
        private ImageView ivForwarded;
        private ImageView ivAttachments;
        private TextView tvSubject;
        private TextView tvKeywords;
        private TextView tvFolder;
        private TextView tvLabels;
        private TextView tvCount;
        private ImageView ivThread;
        private TextView tvExpand;
        private TextView tvPreview;
        private TextView tvNotes;
        private TextView tvError;
        private ImageButton ibHelp;

        private View vsBody;

        private ImageButton ibExpanderAddress;

        private ImageView ivPlain;
        private ImageButton ibReceipt;
        private ImageView ivAutoSubmitted;
        private ImageView ivBrowsed;

        private ImageButton ibSearchContact;
        private ImageButton ibNotifyContact;
        private ImageButton ibPinContact;
        private ImageButton ibAddContact;

        private TextView tvSubmitterTitle;
        private TextView tvDeliveredToTitle;
        private TextView tvFromExTitle;
        private TextView tvToTitle;
        private TextView tvReplyToTitle;
        private TextView tvCcTitle;
        private TextView tvBccTitle;
        private TextView tvIdentityTitle;
        private TextView tvSentTitle;
        private TextView tvReceivedTitle;
        private TextView tvStoredTitle;
        private TextView tvSizeExTitle;
        private TextView tvLanguageTitle;

        private TextView tvSubmitter;
        private TextView tvDeliveredTo;
        private TextView tvFromEx;
        private TextView tvTo;
        private TextView tvReplyTo;
        private TextView tvCc;
        private TextView tvBcc;
        private TextView tvIdentity;
        private TextView tvSent;
        private TextView tvReceived;
        private TextView tvStored;
        private TextView tvSizeEx;
        private TextView tvLanguage;

        private TextView tvSubjectEx;
        private TextView tvFlags;
        private TextView tvKeywordsEx;

        private TextView tvHeaders;
        private ImageButton ibCopyHeaders;
        private ImageButton ibCloseHeaders;
        private ContentLoadingProgressBar pbHeaders;
        private TextView tvNoInternetHeaders;

        private RecyclerView rvAttachment;
        private CheckBox cbInline;
        private ImageButton ibSaveAttachments;
        private ImageButton ibDownloadAttachments;
        private TextView tvNoInternetAttachments;

        private View vSeparator;
        private ImageButton ibFull;
        private ImageButton ibImages;
        private ImageButton ibDecrypt;
        private ImageButton ibVerify;
        private ImageButton ibUndo;
        private ImageButton ibRule;
        private ImageButton ibUnsubscribe;
        private ImageButton ibHeaders;
        private ImageButton ibPrint;
        private ImageButton ibPin;
        private ImageButton ibShare;
        private ImageButton ibEvent;
        private ImageButton ibSearchText;
        private ImageButton ibSearch;
        private ImageButton ibTranslate;
        private ImageButton ibHide;
        private ImageButton ibSeen;
        private ImageButton ibAnswer;
        private ImageButton ibNotes;
        private ImageButton ibLabels;
        private ImageButton ibKeywords;
        private ImageButton ibCopy;
        private ImageButton ibMove;
        private ImageButton ibArchive;
        private ImageButton ibTrash;
        private ImageButton ibJunk;
        private ImageButton ibInbox;
        private ImageButton ibMore;
        private ImageButton ibTools;
        private TextView tvReformatted;
        private TextView tvSignedData;

        private TextView tvBody;
        private View wvBody;
        private ContentLoadingProgressBar pbBody;
        private TextView tvNoInternetBody;
        private ImageButton ibDownloading;
        private Group grpDownloading;
        private ImageButton ibTrashBottom;
        private ImageButton ibArchiveBottom;
        private ImageButton ibMoveBottom;
        private ImageButton ibSeenBottom;
        private Flow flow;

        private ImageButton ibCalendar;
        private TextView tvCalendarSummary;
        private TextView tvCalendarDescription;
        private TextView tvCalendarLocation;
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
        private Group grpAction;
        private Group grpCalendar;
        private Group grpCalendarResponse;
        private Group grpAttachments;
        private Group grpImages;

        private AdapterAttachment adapterAttachment;
        private AdapterImage adapterImage;

        private TwoStateOwner cowner = new TwoStateOwner(owner, "MessageAttachments");
        private TwoStateOwner powner = new TwoStateOwner(owner, "MessagePopup");

        private View.OnTouchListener touchListener = new View.OnTouchListener() {
            private ScaleGestureDetector gestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                private Toast toast = null;

                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    TupleMessageEx message = getMessage();
                    if (message != null) {
                        // Scale factor
                        float factor = detector.getScaleFactor();
                        float size = tvBody.getTextSize() * factor;
                        float scale = (textSize == 0 ? 1.0f : size / (textSize * message_zoom / 100f));
                        if (scale > 10)
                            return true;

                        // Text size
                        properties.setSize(message.id, size);
                        tvBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

                        // Image size
                        Spanned spanned = (Spanned) tvBody.getText();
                        for (ImageSpan img : spanned.getSpans(0, spanned.length(), ImageSpan.class)) {
                            Drawable d = img.getDrawable();
                            ImageHelper.AnnotatedSource a = new ImageHelper.AnnotatedSource(img.getSource());
                            ImageHelper.fitDrawable(d, a, scale, tvBody);
                        }

                        // Feedback
                        String perc = Math.round(scale * 100) + " %";
                        if (toast != null)
                            toast.cancel();
                        toast = ToastEx.makeText(context, perc, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    return true;
                }
            });

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
                    //view.getParent().requestDisallowInterceptTouchEvent(false);
                    //return (view.getId() == R.id.wvBody && ev.getAction() == MotionEvent.ACTION_MOVE);
                    boolean intercept = (view.getId() == R.id.wvBody && ((WebViewEx) wvBody).isZoomedY());
                    view.getParent().requestDisallowInterceptTouchEvent(intercept);
                    return false;
                }
            }
        };

        private MovementMethod movementMethod = new ArrowKeyMovementMethod() {
            private GestureDetector gestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent event) {
                            return onClick(event);
                        }

                        private boolean onClick(MotionEvent event) {
                            TextView widget = tvBody;
                            Spannable buffer = (Spannable) tvBody.getText();
                            int off = Helper.getOffset(widget, buffer, event);

                            TupleMessageEx message = getMessage();
                            if (message == null)
                                return false;

                            boolean show_images = properties.getValue("images", message.id);
                            if (!show_images) {
                                ImageSpan[] image = buffer.getSpans(off, off, ImageSpan.class);
                                if (image.length > 0 && image[0].getSource() != null) {
                                    ImageHelper.AnnotatedSource a = new ImageHelper.AnnotatedSource(image[0].getSource());
                                    Uri uri = Uri.parse(a.getSource());
                                    if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                                        if (onOpenLink(uri, null, false))
                                            return true;
                                }
                            }

                            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
                            if (link.length > 0) {
                                String url = link[0].getURL();
                                Uri uri = Uri.parse(url);

                                int start = buffer.getSpanStart(link[0]);
                                int end = buffer.getSpanEnd(link[0]);
                                String title = (start < 0 || end < 0 || end <= start
                                        ? null : buffer.subSequence(start, end).toString());
                                if (url.equals(title))
                                    title = null;

                                if (onOpenLink(uri, title, false))
                                    return true;
                            }

                            ImageSpan[] image = buffer.getSpans(off, off, ImageSpan.class);
                            if (image.length > 0) {
                                ImageHelper.AnnotatedSource a = new ImageHelper.AnnotatedSource(image[0].getSource());
                                String source = a.getSource();
                                if (!TextUtils.isEmpty(source)) {
                                    if (!a.isTracking())
                                        onOpenImage(message.id, source);
                                    return true;
                                }
                            }

                            DynamicDrawableSpan[] ddss = buffer.getSpans(off, off, DynamicDrawableSpan.class);
                            if (ddss.length > 0) {
                                properties.setValue("quotes", message.id, true);
                                bindBody(message, false);
                                return true;
                            }

                            return false;
                        }
                    });

            @Override
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        private SimpleTask taskContactInfo;

        ViewHolder(final View itemView, long viewType) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            view = itemView.findViewById(R.id.clItem);
            header = itemView.findViewById(R.id.inHeader);

            vwColor = itemView.findViewById(R.id.vwColor);
            ibExpander = itemView.findViewById(R.id.ibExpander);
            ibFlagged = itemView.findViewById(R.id.ibFlagged);
            ibAvatar = itemView.findViewById(R.id.ibAvatar);
            ibVerified = itemView.findViewById(R.id.ibVerified);
            ibAuth = itemView.findViewById(R.id.ibAuth);
            ibPriority = itemView.findViewById(R.id.ibPriority);
            ivImportance = itemView.findViewById(R.id.ivImportance);
            ivSigned = itemView.findViewById(R.id.ivSigned);
            ivEncrypted = itemView.findViewById(R.id.ivEncrypted);
            tvFrom = itemView.findViewById(subject_top ? R.id.tvSubject : R.id.tvFrom);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivType = itemView.findViewById(R.id.ivType);
            ivFound = itemView.findViewById(R.id.ivFound);
            ivClassified = itemView.findViewById(R.id.ivClassified);
            ibSnoozed = itemView.findViewById(R.id.ibSnoozed);
            ivAnswered = itemView.findViewById(R.id.ivAnswered);
            ivForwarded = itemView.findViewById(R.id.ivForwarded);
            ivAttachments = itemView.findViewById(R.id.ivAttachments);
            tvSubject = itemView.findViewById(subject_top ? R.id.tvFrom : R.id.tvSubject);
            tvKeywords = itemView.findViewById(R.id.tvKeywords);
            tvExpand = itemView.findViewById(R.id.tvExpand);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvLabels = itemView.findViewById(R.id.tvLabels);
            tvCount = itemView.findViewById(R.id.tvCount);
            ivThread = itemView.findViewById(R.id.ivThread);
            tvError = itemView.findViewById(R.id.tvError);
            ibHelp = itemView.findViewById(R.id.ibHelp);

            if (tvFrom != null) {
                if (compact)
                    tvFrom.setSingleLine(true);
            }

            if (tvSubject != null) {
                tvSubject.setTextColor(colorSubject);

                if (compact) {
                    boolean full = "full".equals(subject_ellipsize);
                    tvSubject.setSingleLine(!full);

                    if ("start".equals(subject_ellipsize))
                        tvSubject.setEllipsize(TextUtils.TruncateAt.START);
                    else if ("end".equals(subject_ellipsize))
                        tvSubject.setEllipsize(TextUtils.TruncateAt.END);
                    else if ("middle".equals(subject_ellipsize))
                        tvSubject.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                    else
                        tvSubject.setEllipsize(null);
                }
            }

            if (viewType != R.layout.item_message_compact && viewType != R.layout.item_message_normal)
                return;

            if (!BuildConfig.DEBUG && !accessibility)
                return;
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
            ibReceipt = vsBody.findViewById(R.id.ibReceipt);
            ivAutoSubmitted = vsBody.findViewById(R.id.ivAutoSubmitted);
            ivBrowsed = vsBody.findViewById(R.id.ivBrowsed);

            ibSearchContact = vsBody.findViewById(R.id.ibSearchContact);
            ibNotifyContact = vsBody.findViewById(R.id.ibNotifyContact);
            ibPinContact = vsBody.findViewById(R.id.ibPinContact);
            ibAddContact = vsBody.findViewById(R.id.ibAddContact);

            tvSubmitterTitle = vsBody.findViewById(R.id.tvSubmitterTitle);
            tvDeliveredToTitle = vsBody.findViewById(R.id.tvDeliveredToTitle);
            tvFromExTitle = vsBody.findViewById(R.id.tvFromExTitle);
            tvToTitle = vsBody.findViewById(R.id.tvToTitle);
            tvReplyToTitle = vsBody.findViewById(R.id.tvReplyToTitle);
            tvCcTitle = vsBody.findViewById(R.id.tvCcTitle);
            tvBccTitle = vsBody.findViewById(R.id.tvBccTitle);
            tvIdentityTitle = vsBody.findViewById(R.id.tvIdentityTitle);
            tvSentTitle = vsBody.findViewById(R.id.tvSentTitle);
            tvReceivedTitle = vsBody.findViewById(R.id.tvReceivedTitle);
            tvStoredTitle = vsBody.findViewById(R.id.tvStoredTitle);
            tvSizeExTitle = vsBody.findViewById(R.id.tvSizeExTitle);
            tvLanguageTitle = vsBody.findViewById(R.id.tvLanguageTitle);

            tvSubmitter = vsBody.findViewById(R.id.tvSubmitter);
            tvDeliveredTo = vsBody.findViewById(R.id.tvDeliveredTo);
            tvFromEx = vsBody.findViewById(R.id.tvFromEx);
            tvTo = vsBody.findViewById(R.id.tvTo);
            tvReplyTo = vsBody.findViewById(R.id.tvReplyTo);
            tvCc = vsBody.findViewById(R.id.tvCc);
            tvBcc = vsBody.findViewById(R.id.tvBcc);
            tvIdentity = vsBody.findViewById(R.id.tvIdentity);
            tvSent = vsBody.findViewById(R.id.tvSent);
            tvReceived = vsBody.findViewById(R.id.tvReceived);
            tvStored = vsBody.findViewById(R.id.tvStored);
            tvSizeEx = vsBody.findViewById(R.id.tvSizeEx);
            tvLanguage = vsBody.findViewById(R.id.tvLanguage);

            tvSubjectEx = vsBody.findViewById(R.id.tvSubjectEx);
            tvFlags = vsBody.findViewById(R.id.tvFlags);
            tvKeywordsEx = vsBody.findViewById(R.id.tvKeywordsEx);

            tvHeaders = vsBody.findViewById(R.id.tvHeaders);
            tvHeaders.setMovementMethod(LinkMovementMethod.getInstance());
            ibCopyHeaders = vsBody.findViewById(R.id.ibCopyHeaders);
            ibCloseHeaders = vsBody.findViewById(R.id.ibCloseHeaders);
            pbHeaders = vsBody.findViewById(R.id.pbHeaders);
            tvNoInternetHeaders = vsBody.findViewById(R.id.tvNoInternetHeaders);

            ibCalendar = vsBody.findViewById(R.id.ibCalendar);
            tvCalendarSummary = vsBody.findViewById(R.id.tvCalendarSummary);
            tvCalendarDescription = vsBody.findViewById(R.id.tvCalendarDescription);
            tvCalendarLocation = vsBody.findViewById(R.id.tvCalendarLocation);
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

            adapterAttachment = new AdapterAttachment(parentFragment, true);
            rvAttachment.setAdapter(adapterAttachment);

            cbInline = attachments.findViewById(R.id.cbInline);
            ibSaveAttachments = attachments.findViewById(R.id.ibSaveAttachments);
            ibDownloadAttachments = attachments.findViewById(R.id.ibDownloadAttachments);
            tvNoInternetAttachments = attachments.findViewById(R.id.tvNoInternetAttachments);

            vSeparator = vsBody.findViewById(R.id.vSeparator);
            ibFull = vsBody.findViewById(R.id.ibFull);
            ibImages = vsBody.findViewById(R.id.ibImages);
            ibDecrypt = vsBody.findViewById(R.id.ibDecrypt);
            ibVerify = vsBody.findViewById(R.id.ibVerify);
            ibUndo = vsBody.findViewById(R.id.ibUndo);
            ibRule = vsBody.findViewById(R.id.ibRule);
            ibUnsubscribe = vsBody.findViewById(R.id.ibUnsubscribe);
            ibHeaders = vsBody.findViewById(R.id.ibHeaders);
            ibPrint = vsBody.findViewById(R.id.ibPrint);
            ibPin = vsBody.findViewById(R.id.ibPin);
            ibShare = vsBody.findViewById(R.id.ibShare);
            ibEvent = vsBody.findViewById(R.id.ibEvent);
            ibSearchText = vsBody.findViewById(R.id.ibSearchText);
            ibSearch = vsBody.findViewById(R.id.ibSearch);
            ibTranslate = vsBody.findViewById(R.id.ibTranslate);
            ibHide = vsBody.findViewById(R.id.ibHide);
            ibSeen = vsBody.findViewById(R.id.ibSeen);
            ibAnswer = vsBody.findViewById(R.id.ibAnswer);
            ibNotes = vsBody.findViewById(R.id.ibNotes);
            ibLabels = vsBody.findViewById(R.id.ibLabels);
            ibKeywords = vsBody.findViewById(R.id.ibKeywords);
            ibCopy = vsBody.findViewById(R.id.ibCopy);
            ibMove = vsBody.findViewById(R.id.ibMove);
            ibArchive = vsBody.findViewById(R.id.ibArchive);
            ibTrash = vsBody.findViewById(R.id.ibTrash);
            ibJunk = vsBody.findViewById(R.id.ibJunk);
            ibInbox = vsBody.findViewById(R.id.ibInbox);
            ibMore = vsBody.findViewById(R.id.ibMore);
            ibTools = vsBody.findViewById(R.id.ibTools);
            tvReformatted = vsBody.findViewById(R.id.tvReformatted);
            tvSignedData = vsBody.findViewById(R.id.tvSignedData);

            tvBody = vsBody.findViewById(R.id.tvBody);
            wvBody = vsBody.findViewById(R.id.wvBody);
            pbBody = vsBody.findViewById(R.id.pbBody);
            tvNoInternetBody = vsBody.findViewById(R.id.tvNoInternetBody);
            ibDownloading = vsBody.findViewById(R.id.ibDownloading);
            grpDownloading = vsBody.findViewById(R.id.grpDownloading);
            ibTrashBottom = vsBody.findViewById(R.id.ibTrashBottom);
            ibArchiveBottom = vsBody.findViewById(R.id.ibArchiveBottom);
            ibMoveBottom = vsBody.findViewById(R.id.ibMoveBottom);
            ibSeenBottom = vsBody.findViewById(R.id.ibSeenBottom);
            flow = vsBody.findViewById(R.id.flow);

            rvImage = vsBody.findViewById(R.id.rvImage);
            rvImage.setHasFixedSize(false);
            StaggeredGridLayoutManager sglm =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            rvImage.setLayoutManager(sglm);
            adapterImage = new AdapterImage(parentFragment);
            rvImage.setAdapter(adapterImage);

            grpAddresses = vsBody.findViewById(R.id.grpAddresses);
            grpHeaders = vsBody.findViewById(R.id.grpHeaders);
            grpAction = vsBody.findViewById(R.id.grpAction);
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
            final View touch = (viewType == ViewType.THREAD ? ibExpander : header);
            touch.setOnClickListener(this);
            if (touch == ibExpander)
                header.post(new Runnable() {
                    @Override
                    public void run() {
                        Rect rect = new Rect(
                                header.getLeft(),
                                header.getTop(),
                                header.getRight(),
                                header.getBottom());
                        header.setTouchDelegate(new TouchDelegate(rect, touch));
                    }
                });

            ibAvatar.setOnClickListener(this);
            ibVerified.setOnClickListener(this);
            ibAuth.setOnClickListener(this);
            ibPriority.setOnClickListener(this);
            ibSnoozed.setOnClickListener(this);
            ibFlagged.setOnClickListener(this);
            if (viewType == ViewType.THREAD)
                ibFlagged.setOnLongClickListener(this);
            ibHelp.setOnClickListener(this);

            if (vsBody != null) {
                ibExpanderAddress.setOnClickListener(this);
                ibReceipt.setOnClickListener(this);
                ibSearchContact.setOnClickListener(this);
                ibNotifyContact.setOnClickListener(this);
                ibPinContact.setOnClickListener(this);
                ibAddContact.setOnClickListener(this);

                ibCopyHeaders.setOnClickListener(this);
                ibCloseHeaders.setOnClickListener(this);

                ibSaveAttachments.setOnClickListener(this);
                ibDownloadAttachments.setOnClickListener(this);

                ibFull.setOnClickListener(this);
                ibFull.setOnLongClickListener(this);
                ibImages.setOnClickListener(this);
                ibDecrypt.setOnClickListener(this);
                ibVerify.setOnClickListener(this);
                ibUndo.setOnClickListener(this);
                ibRule.setOnClickListener(this);
                ibUnsubscribe.setOnClickListener(this);
                ibHeaders.setOnClickListener(this);
                ibPrint.setOnClickListener(this);
                ibPin.setOnClickListener(this);
                ibShare.setOnClickListener(this);
                ibEvent.setOnClickListener(this);
                ibSearchText.setOnClickListener(this);
                ibSearch.setOnClickListener(this);
                ibTranslate.setOnClickListener(this);
                ibTranslate.setOnLongClickListener(this);
                ibHide.setOnClickListener(this);
                ibSeen.setOnClickListener(this);
                ibAnswer.setOnClickListener(this);
                ibNotes.setOnClickListener(this);
                ibNotes.setOnLongClickListener(this);
                ibLabels.setOnClickListener(this);
                ibKeywords.setOnClickListener(this);
                ibCopy.setOnClickListener(this);
                ibMove.setOnClickListener(this);
                ibArchive.setOnClickListener(this);
                ibTrash.setOnClickListener(this);
                ibTrash.setOnLongClickListener(this);
                ibJunk.setOnClickListener(this);
                ibInbox.setOnClickListener(this);
                ibMore.setOnClickListener(this);
                ibTools.setOnClickListener(this);

                ibDownloading.setOnClickListener(this);
                ibTrashBottom.setOnClickListener(this);
                ibTrashBottom.setOnLongClickListener(this);
                ibArchiveBottom.setOnClickListener(this);
                ibMoveBottom.setOnClickListener(this);
                ibSeenBottom.setOnClickListener(this);

                tvBody.setOnTouchListener(touchListener);
                tvBody.setMovementMethod(movementMethod);
                tvBody.addOnLayoutChangeListener(this);

                ibCalendar.setOnClickListener(this);
                btnCalendarAccept.setOnClickListener(this);
                btnCalendarDecline.setOnClickListener(this);
                btnCalendarMaybe.setOnClickListener(this);

                btnCalendarAccept.setOnLongClickListener(this);
                btnCalendarDecline.setOnLongClickListener(this);
                btnCalendarMaybe.setOnLongClickListener(this);
            }

            if (accessibility) {
                view.setAccessibilityDelegate(accessibilityDelegateHeader);
                header.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
        }

        private void unwire() {
            final View touch = (viewType == ViewType.THREAD ? ibExpander : header);
            touch.setOnClickListener(null);

            ibAvatar.setOnClickListener(null);
            ibVerified.setOnClickListener(null);
            ibAuth.setOnClickListener(null);
            ibPriority.setOnClickListener(null);
            ibSnoozed.setOnClickListener(null);
            ibFlagged.setOnClickListener(null);
            if (viewType == ViewType.THREAD)
                ibFlagged.setOnLongClickListener(null);
            ibHelp.setOnClickListener(null);

            if (vsBody != null) {
                ibExpanderAddress.setOnClickListener(null);
                ibReceipt.setOnLongClickListener(null);
                ibSearchContact.setOnClickListener(null);
                ibNotifyContact.setOnClickListener(null);
                ibPinContact.setOnClickListener(null);
                ibAddContact.setOnClickListener(null);

                ibCopyHeaders.setOnClickListener(null);
                ibCloseHeaders.setOnClickListener(null);

                ibSaveAttachments.setOnClickListener(null);
                ibDownloadAttachments.setOnClickListener(null);

                ibFull.setOnClickListener(null);
                ibFull.setOnLongClickListener(null);
                ibImages.setOnClickListener(null);
                ibDecrypt.setOnClickListener(null);
                ibVerify.setOnClickListener(null);
                ibUndo.setOnClickListener(null);
                ibRule.setOnClickListener(null);
                ibUnsubscribe.setOnClickListener(null);
                ibHeaders.setOnClickListener(null);
                ibPrint.setOnClickListener(null);
                ibPin.setOnClickListener(null);
                ibShare.setOnClickListener(null);
                ibEvent.setOnClickListener(null);
                ibSearchText.setOnClickListener(null);
                ibSearch.setOnClickListener(null);
                ibTranslate.setOnClickListener(null);
                ibTranslate.setOnLongClickListener(null);
                ibHide.setOnClickListener(null);
                ibSeen.setOnClickListener(null);
                ibAnswer.setOnClickListener(null);
                ibNotes.setOnClickListener(null);
                ibNotes.setOnLongClickListener(null);
                ibLabels.setOnClickListener(null);
                ibKeywords.setOnClickListener(null);
                ibCopy.setOnClickListener(null);
                ibMove.setOnClickListener(null);
                ibArchive.setOnClickListener(null);
                ibTrash.setOnClickListener(null);
                ibTrash.setOnLongClickListener(null);
                ibJunk.setOnClickListener(null);
                ibInbox.setOnClickListener(null);
                ibMore.setOnClickListener(null);
                ibTools.setOnClickListener(null);

                ibDownloading.setOnClickListener(null);
                ibTrashBottom.setOnClickListener(null);
                ibTrashBottom.setOnLongClickListener(null);
                ibArchiveBottom.setOnClickListener(null);
                ibMoveBottom.setOnClickListener(null);
                ibSeenBottom.setOnClickListener(null);

                tvBody.setOnTouchListener(null);
                tvBody.setMovementMethod(null);
                tvBody.removeOnLayoutChangeListener(this);

                btnCalendarAccept.setOnClickListener(null);
                btnCalendarDecline.setOnClickListener(null);
                btnCalendarMaybe.setOnClickListener(null);
                ibCalendar.setOnClickListener(null);

                btnCalendarAccept.setOnLongClickListener(null);
                btnCalendarDecline.setOnLongClickListener(null);
                btnCalendarMaybe.setOnLongClickListener(null);
            }

            if (accessibility)
                view.setAccessibilityDelegate(null);
        }

        @SuppressLint("WrongConstant")
        private void bindTo(final TupleMessageEx message, boolean scroll) {
            boolean inbox = EntityFolder.INBOX.equals(message.folderType);
            boolean junk = EntityFolder.JUNK.equals(message.folderType);
            boolean outbox = EntityFolder.OUTBOX.equals(message.folderType);
            boolean outgoing = isOutgoing(message);
            boolean reverse = (outgoing && viewType != ViewType.THREAD &&
                    (EntityFolder.isOutgoing(type) || viewType == ViewType.SEARCH));
            String selector = (reverse ? null : message.bimi_selector);
            Address[] addresses = (reverse ? message.to : message.from);
            Address[] senders = ContactInfo.fillIn(
                    reverse && !show_recipients ? message.to : message.senders, prefer_contact, only_contact);
            Address[] recipients = ContactInfo.fillIn(
                    reverse && !show_recipients ? message.from : message.recipients, prefer_contact, only_contact);
            boolean authenticated =
                    !((Boolean.FALSE.equals(message.dkim) && check_authentication) ||
                            (Boolean.FALSE.equals(message.spf) && check_authentication) ||
                            (Boolean.FALSE.equals(message.dmarc) && check_authentication) ||
                            (Boolean.FALSE.equals(message.reply_domain) && check_reply_domain) ||
                            (Boolean.FALSE.equals(message.mx) && check_mx) ||
                            (Boolean.TRUE.equals(message.blocklist) && check_blocklist));
            int auths = (check_authentication &&
                    Boolean.TRUE.equals(message.dkim) ? 1 : 0) +
                    (Boolean.TRUE.equals(message.spf) ? 1 : 0) +
                    (Boolean.TRUE.equals(message.dmarc) ? 1 : 0);
            boolean expanded = (viewType == ViewType.THREAD && properties.getValue("expanded", message.id));

            // Text size
            if (textSize != 0) {
                // 14, 18, 22 sp
                //tvKeywords.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);
                //tvFolder.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);
                //tvLabels.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);
                tvPreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);
                tvNotes.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.9f);
            }

            // Selected / disabled
            view.setActivated(selectionTracker != null && selectionTracker.isSelected(message.id));
            view.setAlpha(
                    (EntityFolder.OUTBOX.equals(message.folderType)
                            ? message.identitySynchronize == null || !message.identitySynchronize
                            : message.accountProtocol == EntityAccount.TYPE_IMAP && (message.uid == null || message.ui_deleted))
                            ? Helper.LOW_LIGHT : 1.0f);

            // Duplicate
            if (viewType == ViewType.THREAD) {
                boolean dim = (message.duplicate || EntityFolder.TRASH.equals(message.folderType));
                ibFlagged.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibAvatar.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibVerified.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibAuth.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibPriority.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivImportance.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivSigned.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivEncrypted.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvFrom.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvSize.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvTime.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivType.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivFound.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivClassified.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibSnoozed.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivAnswered.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivForwarded.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivAttachments.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvSubject.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvKeywords.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvFolder.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvLabels.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvCount.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivThread.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvPreview.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvNotes.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                tvError.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
            }

            bindSeen(message);

            // Account color
            int colorBackground =
                    (message.accountColor == null || !ActivityBilling.isPro(context)
                            ? colorSeparator : message.accountColor);
            if (!Objects.equals(vwColor.getTag() == null, colorBackground)) {
                vwColor.setTag(colorBackground);
                vwColor.setBackgroundColor(colorBackground);
            }
            vwColor.setVisibility(color_stripe ? View.VISIBLE : View.GONE);

            // Expander
            if (!Objects.equals(ibExpander.getTag(), expanded)) {
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
            ibVerified.setVisibility(View.GONE);

            if (authentication && !authenticated) {
                ibAuth.setImageLevel(0);
                ibAuth.setImageTintList(ColorStateList.valueOf(colorWarning));
                ibAuth.setVisibility(View.VISIBLE);
            } else if (authentication_indicator) {
                ibAuth.setImageLevel(auths + 1);
                ibAuth.setImageTintList(ColorStateList.valueOf(
                        auths < 3 ? colorControlNormal : colorVerified));
                ibAuth.setVisibility(auths > 0 ? View.VISIBLE : View.GONE);
            } else
                ibAuth.setVisibility(View.GONE);

            if (EntityMessage.PRIORITIY_HIGH.equals(message.ui_priority)) {
                ibPriority.setImageLevel(0);
                ibPriority.setVisibility(View.VISIBLE);
            } else if (EntityMessage.PRIORITIY_LOW.equals(message.ui_priority)) {
                ibPriority.setImageLevel(1);
                ibPriority.setVisibility(View.VISIBLE);
            } else
                ibPriority.setVisibility(View.GONE);

            if (EntityMessage.PRIORITIY_HIGH.equals(message.ui_importance)) {
                ivImportance.setImageLevel(0);
                ivImportance.setVisibility(View.VISIBLE);
            } else if (EntityMessage.PRIORITIY_LOW.equals(message.ui_importance)) {
                ivImportance.setImageLevel(1);
                ivImportance.setVisibility(View.VISIBLE);
            } else
                ivImportance.setVisibility(View.GONE);

            ivSigned.setVisibility(message.signed > 0 ? View.VISIBLE : View.GONE);
            if (message.verified)
                ivSigned.setColorFilter(colorEncrypt);
            else
                ivSigned.clearColorFilter();
            ivEncrypted.setVisibility(message.encrypted > 0 ? View.VISIBLE : View.GONE);

            MessageHelper.AddressFormat format = email_format;
            if (junk)
                format = MessageHelper.AddressFormat.NAME_EMAIL;

            if (show_recipients && recipients != null && recipients.length > 0) {
                tvFrom.setText(context.getString(outgoing && viewType != ViewType.THREAD && compact
                                ? R.string.title_to_from
                                : R.string.title_from_to,
                        MessageHelper.formatAddresses(senders, format, false),
                        MessageHelper.formatAddresses(recipients, format, false)));
            } else
                tvFrom.setText(MessageHelper.formatAddresses(senders, format, false));

            tvFrom.setPaintFlags(tvFrom.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            tvSize.setText(message.totalSize == null ? null : Helper.humanReadableByteCount(message.totalSize));
            tvSize.setVisibility(
                    message.totalSize != null && ("size".equals(sort) || "attachments".equals(sort))
                            ? View.VISIBLE : View.GONE);
            tvTime.setText(date && FragmentMessages.SORT_DATE_HEADER.contains(sort)
                    ? TF.format(message.received)
                    : Helper.getRelativeTimeSpanString(context, message.received));

            // Line 2
            tvSubject.setText(message.subject);

            // Workaround layout bug
            tvSubject.requestLayout();
            tvSubject.invalidate();

            if (keywords_header) {
                SpannableStringBuilder keywords = getKeywords(message);
                tvKeywords.setVisibility(keywords.length() > 0 ? View.VISIBLE : View.GONE);
                tvKeywords.setText(keywords);
            } else
                tvKeywords.setVisibility(View.GONE);

            // Line 3
            int icon = (message.drafts > 0
                    ? R.drawable.twotone_edit_24
                    : EntityFolder.getIcon(outgoing ? EntityFolder.SENT : message.folderType));
            ivType.setVisibility(message.drafts > 0 ||
                    (viewType == ViewType.UNIFIED && type == null && (!inbox || outgoing)) ||
                    (viewType == ViewType.FOLDER && outgoing && !EntityFolder.SENT.equals(message.folderType)) ||
                    (viewType == ViewType.THREAD && (outgoing || EntityFolder.SENT.equals(message.folderType))) ||
                    viewType == ViewType.SEARCH
                    ? View.VISIBLE : View.GONE);
            if (!Objects.equals(ivType.getTag(), icon)) {
                ivType.setTag(icon);
                ivType.setImageResource(icon);
            }

            ivFound.setVisibility(message.ui_found && found ? View.VISIBLE : View.GONE);
            ivClassified.setVisibility(message.auto_classified ? View.VISIBLE : View.GONE);

            int snoozy = (message.ui_snoozed != null && message.ui_snoozed == Long.MAX_VALUE
                    ? R.drawable.twotone_visibility_off_24
                    : R.drawable.twotone_timelapse_24);
            if (!Objects.equals(ibSnoozed.getTag(), snoozy)) {
                ibSnoozed.setTag(snoozy);
                ibSnoozed.setImageResource(snoozy);
            }
            if (message.ui_unsnoozed)
                ibSnoozed.setColorFilter(colorAccent);
            else
                ibSnoozed.clearColorFilter();

            ibSnoozed.setVisibility(message.ui_snoozed == null && !message.ui_unsnoozed ? View.GONE : View.VISIBLE);
            ivAnswered.setVisibility(message.ui_answered ? View.VISIBLE : View.GONE);
            ivForwarded.setVisibility(message.isForwarded() ? View.VISIBLE : View.GONE);
            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.FOLDER)
                tvFolder.setText(outbox ? message.identityEmail : message.accountName);
            else if (viewType == ViewType.THREAD || viewType == ViewType.SEARCH)
                tvFolder.setText(message.getFolderName(context));
            else
                tvFolder.setText(message.accountName + "/" + message.getFolderName(context));

            tvFolder.setVisibility(compact && viewType != ViewType.THREAD ? View.GONE : View.VISIBLE);

            tvLabels.setText(message.labels == null ? null : TextUtils.join(", ", message.labels));
            tvLabels.setVisibility(
                    labels_header && message.labels != null && message.labels.length > 0
                            ? View.VISIBLE : View.GONE);

            boolean selected = properties.getValue("selected", message.id);
            if (viewType == ViewType.THREAD || (!threading && !selected)) {
                tvCount.setVisibility(View.GONE);
                ivThread.setVisibility(View.GONE);
            } else {
                tvCount.setVisibility(threading && message.visible > 1 ? View.VISIBLE : View.GONE);
                ivThread.setVisibility(selected || message.visible > 1 ? View.VISIBLE : View.GONE);

                if (threading_unread)
                    tvCount.setText(context.getString(R.string.title_of,
                            NF.format(message.visible_unseen),
                            NF.format(message.visible)));
                else
                    tvCount.setText(NF.format(message.visible));

                if (selected)
                    ivThread.setColorFilter(colorAccent);
                else
                    ivThread.clearColorFilter();
            }

            // Starred
            bindFlagged(message, expanded);

            // Expand warning
            bindExpandWarning(message, expanded);

            // Message text preview
            int textColor = (contrast ? textColorPrimary : textColorSecondary);
            if (!Objects.equals(tvPreview.getTag(), textColor)) {
                tvPreview.setTag(textColor);
                tvPreview.setTextColor(textColor);
                if (preview_lines == 1)
                    tvPreview.setSingleLine(true);
                else
                    tvPreview.setMaxLines(preview_lines);
            }
            tvPreview.setTypeface(
                    monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT,
                    preview_italic ? Typeface.ITALIC : Typeface.NORMAL);
            tvPreview.setText(message.preview);
            tvPreview.setVisibility(preview && !TextUtils.isEmpty(message.preview) ? View.VISIBLE : View.GONE);

            tvNotes.setText(message.notes);
            tvNotes.setTextColor(message.notes_color == null ? textColorTertiary : message.notes_color);
            tvNotes.setVisibility(TextUtils.isEmpty(message.notes) ? View.GONE : View.VISIBLE);

            // Error / warning
            String error = message.error;
            if (message.warning != null)
                if (error == null)
                    error = message.warning;
                else
                    error += " " + message.warning;

            if (debug) {
                String text = context.getString(R.string.menu_setup) + "/" +
                        context.getString(R.string.title_advanced_debug) + "/" +
                        context.getString(R.string.title_advanced_section_misc) + " !!!" +
                        "\nerror=" + error +
                        "\nuid=" + message.uid + " id=" + message.id +
                        " fid=" + message.folder + " aid=" + message.account +
                        " " + DTF.format(new Date(message.received)) +
                        "\n" + (message.ui_hide ? "HIDDEN " : "") +
                        "seen=" + message.seen + "/" + message.ui_seen +
                        " unseen=" + message.unseen +
                        " ignored=" + message.ui_ignored +
                        " found=" + message.ui_found +
                        " busy=" + (message.ui_busy == null ? null : new Date(message.ui_busy)) +
                        "\nhash=" + message.hash +
                        "\nmsgid=" + message.msgid + "/" + message.uidl +
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
            ContactInfo[] info = ContactInfo.getCached(context,
                    message.account, message.folderType, selector, addresses);
            if (info == null) {
                if (taskContactInfo != null)
                    taskContactInfo.cancel(context);

                Bundle aargs = new Bundle();
                aargs.putLong("id", message.id);
                aargs.putLong("account", message.account);
                aargs.putString("folderType", message.folderType);
                aargs.putString("selector", selector);
                aargs.putSerializable("addresses", addresses);

                taskContactInfo = new SimpleTask<ContactInfo[]>() {
                    @Override
                    protected ContactInfo[] onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");
                        String folderType = args.getString("folderType");
                        String selector = args.getString("selector");
                        Address[] addresses = (Address[]) args.getSerializable("addresses");
                        return ContactInfo.get(context, account, folderType, selector, addresses);
                    }

                    @Override
                    protected void onExecuted(Bundle args, ContactInfo[] info) {
                        taskContactInfo = null;

                        long id = args.getLong("id");
                        TupleMessageEx amessage = getMessage();
                        if (amessage == null || !amessage.id.equals(id))
                            return;

                        bindContactInfo(amessage, info, addresses);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.setLog(false);
                taskContactInfo.execute(context, owner, aargs, "message:avatar");
            } else
                bindContactInfo(message, info, addresses);

            if (viewType == ViewType.THREAD)
                if (expanded)
                    bindExpanded(message, scroll);
                else {
                    clearExpanded(message);
                    if (scroll)
                        properties.scrollTo(getAdapterPosition(), 0);
                }

            if (properties.getValue("raw_save", message.id)) {
                properties.setValue("raw_save", message.id, false);
                onMenuRawSave(message);
            }

            if (properties.getValue("raw_send", message.id)) {
                properties.setValue("raw_send", message.id, false);
                onMenuRawSend(message);
            }
        }

        private void clearExpanded(TupleMessageEx message) {
            if (compact) {
                if (tvFrom.getMaxLines() > 1)
                    tvFrom.setSingleLine(true);
                boolean full = "full".equals(subject_ellipsize);
                if (full ? tvSubject.getMaxLines() == 1 : tvSubject.getMaxLines() > 1)
                    tvSubject.setSingleLine(!full);
            }

            tvPreview.setVisibility(
                    preview && message != null && !TextUtils.isEmpty(message.preview)
                            ? View.VISIBLE : View.GONE);

            if (vsBody == null)
                return;

            cowner.stop();

            grpAddresses.setVisibility(View.GONE);
            grpHeaders.setVisibility(View.GONE);
            grpAction.setVisibility(View.GONE);
            grpCalendar.setVisibility(View.GONE);
            grpCalendarResponse.setVisibility(View.GONE);
            grpAttachments.setVisibility(View.GONE);
            grpImages.setVisibility(View.GONE);

            ivPlain.setVisibility(View.GONE);
            ibReceipt.setVisibility(View.GONE);
            ivAutoSubmitted.setVisibility(View.GONE);
            ivBrowsed.setVisibility(View.GONE);

            ibSearchContact.setVisibility(View.GONE);
            ibNotifyContact.setVisibility(View.GONE);
            ibPinContact.setVisibility(View.GONE);
            ibAddContact.setVisibility(View.GONE);

            tvSubmitterTitle.setVisibility(View.GONE);
            tvDeliveredToTitle.setVisibility(View.GONE);
            tvFromExTitle.setVisibility(View.GONE);
            tvToTitle.setVisibility(View.GONE);
            tvReplyToTitle.setVisibility(View.GONE);
            tvCcTitle.setVisibility(View.GONE);
            tvBccTitle.setVisibility(View.GONE);
            tvIdentityTitle.setVisibility(View.GONE);
            tvSentTitle.setVisibility(View.GONE);
            tvReceivedTitle.setVisibility(View.GONE);
            tvStoredTitle.setVisibility(View.GONE);
            tvSizeExTitle.setVisibility(View.GONE);
            tvLanguageTitle.setVisibility(View.GONE);

            tvSubmitter.setVisibility(View.GONE);
            tvDeliveredTo.setVisibility(View.GONE);
            tvFromEx.setVisibility(View.GONE);
            tvTo.setVisibility(View.GONE);
            tvReplyTo.setVisibility(View.GONE);
            tvCc.setVisibility(View.GONE);
            tvBcc.setVisibility(View.GONE);
            tvIdentity.setVisibility(View.GONE);
            tvSent.setVisibility(View.GONE);
            tvReceived.setVisibility(View.GONE);
            tvStored.setVisibility(View.GONE);
            tvSizeEx.setVisibility(View.GONE);
            tvLanguage.setVisibility(View.GONE);

            tvSubjectEx.setVisibility(View.GONE);
            tvFlags.setVisibility(View.GONE);
            tvKeywordsEx.setVisibility(View.GONE);

            ibCopyHeaders.setVisibility(View.GONE);

            pbHeaders.setVisibility(View.GONE);
            tvNoInternetHeaders.setVisibility(View.GONE);

            clearCalendar();

            cbInline.setVisibility(View.GONE);
            ibSaveAttachments.setVisibility(View.GONE);
            ibDownloadAttachments.setVisibility(View.GONE);
            tvNoInternetAttachments.setVisibility(View.GONE);

            vSeparator.setVisibility(View.GONE);
            ibFull.setVisibility(View.GONE);
            ibImages.setVisibility(View.GONE);
            ibDecrypt.setVisibility(View.GONE);
            ibVerify.setVisibility(View.GONE);
            ibUndo.setVisibility(View.GONE);
            ibRule.setVisibility(View.GONE);
            ibUnsubscribe.setVisibility(View.GONE);
            ibHeaders.setVisibility(View.GONE);
            ibPrint.setVisibility(View.GONE);
            ibPin.setVisibility(View.GONE);
            ibShare.setVisibility(View.GONE);
            ibEvent.setVisibility(View.GONE);
            ibSearchText.setVisibility(View.GONE);
            ibSearch.setVisibility(View.GONE);
            ibTranslate.setVisibility(View.GONE);
            ibHide.setVisibility(View.GONE);
            ibSeen.setVisibility(View.GONE);
            ibAnswer.setVisibility(View.GONE);
            ibNotes.setVisibility(View.GONE);
            ibLabels.setVisibility(View.GONE);
            ibKeywords.setVisibility(View.GONE);
            ibCopy.setVisibility(View.GONE);
            ibMove.setVisibility(View.GONE);
            ibArchive.setVisibility(View.GONE);
            ibTrash.setVisibility(View.GONE);
            ibJunk.setVisibility(View.GONE);
            ibInbox.setVisibility(View.GONE);
            ibMore.setVisibility(View.GONE);
            ibTools.setVisibility(View.GONE);
            tvReformatted.setVisibility(View.GONE);
            tvSignedData.setVisibility(View.GONE);

            tvNoInternetBody.setVisibility(View.GONE);
            grpDownloading.setVisibility(View.GONE);
            tvBody.setText(null);
            tvBody.setVisibility(View.GONE);
            // TO DO: clear web view?
            wvBody.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            grpAction.setVisibility(View.GONE);
            clearActions();
            ibTrashBottom.setVisibility(View.GONE);
            ibArchiveBottom.setVisibility(View.GONE);
            ibMoveBottom.setVisibility(View.GONE);
            ibSeenBottom.setVisibility(View.GONE);
        }

        private void clearActions() {
            ConstraintLayout cl = (ConstraintLayout) flow.getParent();
            for (int id : flow.getReferencedIds()) {
                View v = cl.findViewById(id);
                flow.removeView(v);
                cl.removeView(v);
            }
        }

        private void clearCalendar() {
            ibCalendar.setVisibility(View.GONE);
            tvCalendarSummary.setVisibility(View.GONE);
            tvCalendarDescription.setVisibility(View.GONE);
            tvCalendarLocation.setVisibility(View.GONE);
            tvCalendarStart.setVisibility(View.GONE);
            tvCalendarEnd.setVisibility(View.GONE);
            tvAttendees.setVisibility(View.GONE);
            pbCalendarWait.setVisibility(View.GONE);
        }

        private void bindSeen(TupleMessageEx message) {
            if (cards && shadow_unread) {
                boolean shadow = (message.unseen > 0);
                int color = (shadow
                        ? ColorUtils.setAlphaComponent(colorAccent, 127)
                        : Color.TRANSPARENT);
                if (!Objects.equals(itemView.getTag(), shadow)) {
                    itemView.setTag(shadow);

                    itemView.setBackgroundColor(color);

                    ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
                    lparam.topMargin = (shadow ? dp1 : 0);
                    lparam.bottomMargin = (shadow ? dp1 : 0);
                    itemView.setLayoutParams(lparam);
                }
            }

            if (textSize != 0) {
                float fz_sender = (font_size_sender == null ? textSize : font_size_sender) * (message.unseen > 0 ? 1.1f : 1f);
                float fz_subject = (font_size_subject == null ? textSize : font_size_subject) * 0.9f;
                tvFrom.setTextSize(TypedValue.COMPLEX_UNIT_PX, fz_sender);
                tvSubject.setTextSize(TypedValue.COMPLEX_UNIT_PX, fz_subject);

                if (avatars) {
                    int px = Math.round(fz_sender + fz_subject + (compact ? 0 : textSize * 0.9f));
                    ViewGroup.LayoutParams lparams = ibAvatar.getLayoutParams();
                    if (lparams.width != px || lparams.height != px) {
                        lparams.width = px;
                        lparams.height = px;
                        ibAvatar.requestLayout();
                    }
                }
            }

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
            if (!Objects.equals(tvFrom.getTag(), colorUnseen)) {
                tvFrom.setTag(colorUnseen);
                tvFrom.setTextColor(colorUnseen);
                tvSize.setTextColor(colorUnseen);
                tvTime.setTextColor(colorUnseen);
            }
        }

        private void bindFlagged(TupleMessageEx message, boolean expanded) {
            boolean pro = ActivityBilling.isPro(context);
            boolean flagged = (message.count - message.unflagged) > 0;
            int color = (message.color == null || !pro ? colorAccent : message.color);
            int tint = (flagged ? color : textColorSecondary);

            if (!Objects.equals(ibFlagged.getTag(), flagged)) {
                ibFlagged.setTag(flagged);
                ibFlagged.setImageResource(flagged ? R.drawable.baseline_star_24 : R.drawable.twotone_star_border_24);
            }

            ColorStateList csl = ibFlagged.getImageTintList();
            if (csl == null || csl.getColorForState(new int[0], 0) != tint)
                ibFlagged.setImageTintList(ColorStateList.valueOf(tint));

            ibFlagged.setEnabled(message.uid != null || message.accountProtocol != EntityAccount.TYPE_IMAP);

            card.setCardBackgroundColor(
                    flags_background && flagged && !expanded
                            ? ColorUtils.setAlphaComponent(color, 127) : Color.TRANSPARENT);

            if (flags)
                ibFlagged.setVisibility(message.folderReadOnly ? View.INVISIBLE : View.VISIBLE);
            else
                ibFlagged.setVisibility(View.GONE);
        }

        private void bindContactInfo(TupleMessageEx message, ContactInfo[] info, Address[] addresses) {
            if (avatars) {
                ContactInfo main = (info.length > 0 ? info[0] : null);
                if (main == null || !main.hasPhoto()) {
                    ibAvatar.setImageDrawable(null);
                    ibAvatar.setTag(null);
                    ibAvatar.setEnabled(false);
                } else {
                    ibAvatar.setImageBitmap(main.getPhotoBitmap());

                    Uri lookupUri = main.getLookupUri();
                    ibAvatar.setTag(lookupUri);

                    if (BuildConfig.DEBUG)
                        ibAvatar.setContentDescription(main.getEmailAddress() + "=" + main.getType());

                    ibAvatar.setEnabled(lookupUri != null || BuildConfig.DEBUG);
                }
                ibAvatar.setVisibility(main == null || !main.hasPhoto() ? View.GONE : View.VISIBLE);

                if (main != null && "vmc".equals(main.getType()) &&
                        Boolean.TRUE.equals(message.dkim) &&
                        Boolean.TRUE.equals(message.spf) &&
                        Boolean.TRUE.equals(message.dmarc)) {
                    ibVerified.setImageLevel(main.isVerified() ? 1 : 0);
                    ibVerified.setImageTintList(ColorStateList.valueOf(main.isVerified()
                            ? colorVerified : colorControlNormal));
                    ibVerified.setContentDescription(context.getString(main.isVerified()
                            ? R.string.title_advanced_bimi_verified
                            : R.string.title_advanced_bimi_unverified));
                    if (main.isVerified() || BuildConfig.DEBUG) {
                        ibVerified.setVisibility(View.VISIBLE);

                        if (authentication_indicator)
                            ibAuth.setVisibility(View.GONE);
                    }
                }
            }

            if (distinguish_contacts) {
                boolean known = false;
                if (addresses != null)
                    for (int i = 0; i < addresses.length; i++)
                        if (info[i].isKnown()) {
                            known = true;
                            break;
                        }
                if (known)
                    tvFrom.setPaintFlags(tvFrom.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }
        }

        private void bindExpandWarning(TupleMessageEx message, boolean expanded) {
            if (viewType != ViewType.THREAD || expanded || message.content || message.uid == null || unmetered)
                tvExpand.setVisibility(View.GONE);
            else {
                tvExpand.setText(context.getString(R.string.title_expand_warning,
                        message.size == null ? "? kB" : Helper.humanReadableByteCount(message.size)));
                tvExpand.setVisibility(View.VISIBLE);
            }
        }

        private void bindExpanded(final TupleMessageEx message, final boolean scroll) {
            DB db = DB.getInstance(context);

            cowner.recreate();

            if (compact) {
                tvFrom.setSingleLine(false);
                tvSubject.setSingleLine(false);
            }

            tvPreview.setVisibility(View.GONE);

            ensureExpanded();

            bindAddresses(message);
            bindHeaders(message, false);
            bindAttachments(message, properties.getAttachments(message.id), false);

            // Actions
            vSeparator.setVisibility(View.VISIBLE);
            ibFull.setEnabled(false);
            ibFull.setVisibility(View.VISIBLE);
            ibImages.setVisibility(View.GONE);
            ibDecrypt.setVisibility(View.GONE);
            ibVerify.setVisibility(View.GONE);
            ibUndo.setVisibility(View.GONE);
            ibRule.setVisibility(View.GONE);
            ibUnsubscribe.setVisibility(View.GONE);
            ibHeaders.setVisibility(View.GONE);
            ibPrint.setVisibility(View.GONE);
            ibPin.setVisibility(View.GONE);
            ibShare.setVisibility(View.GONE);
            ibEvent.setVisibility(View.GONE);
            ibSearchText.setVisibility(View.GONE);
            ibSearch.setVisibility(View.GONE);
            ibTranslate.setVisibility(View.GONE);
            ibHide.setVisibility(View.GONE);
            ibSeen.setVisibility(View.GONE);
            ibAnswer.setVisibility(View.GONE);
            ibNotes.setVisibility(View.GONE);
            ibLabels.setVisibility(View.GONE);
            ibKeywords.setVisibility(View.GONE);
            ibCopy.setVisibility(View.GONE);
            ibMove.setVisibility(View.GONE);
            ibArchive.setVisibility(View.GONE);
            ibTrash.setVisibility(View.GONE);
            ibJunk.setVisibility(View.GONE);
            ibInbox.setVisibility(View.GONE);
            ibMore.setVisibility(View.GONE);
            ibTools.setVisibility(View.GONE);
            tvReformatted.setVisibility(View.GONE);
            tvSignedData.setVisibility(View.GONE);

            // Message text
            tvNoInternetBody.setVisibility(suitable || message.content ? View.GONE : View.VISIBLE);
            grpDownloading.setVisibility(message.content ? View.GONE : View.VISIBLE);

            int height = properties.getHeight(message.id, 0);
            if (height == 0) {
                tvBody.setVisibility(View.GONE);
                wvBody.setVisibility(View.GONE);
            } else {
                boolean show_full = properties.getValue("full", message.id);
                if (show_full) {
                    wvBody.setVisibility(View.INVISIBLE);
                    wvBody.setMinimumHeight(height);
                } else {
                    tvBody.setVisibility(View.INVISIBLE);
                    tvBody.setMinHeight(height);
                }
            }
            pbBody.setVisibility(View.GONE);

            clearCalendar();
            grpCalendar.setVisibility(View.GONE);
            grpCalendarResponse.setVisibility(View.GONE);

            grpAction.setVisibility(View.GONE);
            clearActions();
            ibTrashBottom.setVisibility(View.GONE);
            ibArchiveBottom.setVisibility(View.GONE);
            ibMoveBottom.setVisibility(View.GONE);
            ibSeenBottom.setVisibility(View.GONE);

            db.attachment().liveAttachments(message.id).observe(cowner, new Observer<List<EntityAttachment>>() {
                @Override
                public void onChanged(@Nullable List<EntityAttachment> attachments) {
                    int inlineImages = 0;
                    int embeddedMessages = 0;
                    if (attachments != null)
                        for (EntityAttachment attachment : attachments)
                            if (attachment.available)
                                if (attachment.isInline() && attachment.isImage())
                                    inlineImages++;
                                else if ("message/rfc822".equals(attachment.getMimeType()))
                                    embeddedMessages++;

                    int lastInlineImages = 0;
                    int lastEmbeddedMessages = 0;
                    List<EntityAttachment> lastAttachments = properties.getAttachments(message.id);
                    if (lastAttachments != null)
                        for (EntityAttachment attachment : lastAttachments)
                            if (attachment.available)
                                if (attachment.isInline() && attachment.isImage())
                                    lastInlineImages++;
                                else if ("message/rfc822".equals(attachment.getMimeType()))
                                    lastEmbeddedMessages++;

                    boolean show_images = properties.getValue("images", message.id);
                    boolean inline = prefs.getBoolean("inline_images", false);
                    if (embeddedMessages > lastEmbeddedMessages ||
                            (inlineImages > lastInlineImages && (show_images || inline)))
                        bindBody(message, false);

                    bindAttachments(message, attachments, true);

                    if (!scroll)
                        properties.ready(message.id);
                }
            });

            // Setup actions
            setupTools(message, scroll, true);
        }

        private void setupTools(final TupleMessageEx message, final boolean scroll, final boolean bind) {
            Bundle sargs = new Bundle();
            sargs.putLong("id", message.id);
            sargs.putLong("account", message.account);

            new SimpleTask<ToolData>() {
                @Override
                protected ToolData onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    long aid = args.getLong("account");

                    ToolData data = new ToolData();

                    DB db = DB.getInstance(context);
                    EntityAccount account = db.account().getAccount(aid);
                    data.isGmail = (account != null && account.isGmail());
                    data.folders = db.folder().getSystemFolders(aid);
                    data.attachments = db.attachment().getAttachments(id);

                    return data;
                }

                @Override
                protected void onExecuted(Bundle args, ToolData data) {
                    long id = args.getLong("id");
                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(id))
                        return;

                    boolean show_expanded = properties.getValue("expanded", message.id);
                    if (!show_expanded)
                        return;

                    if (!attachments_alt && bind)
                        bindAttachments(message, data.attachments, false);

                    boolean hasInbox = false;
                    boolean hasArchive = false;
                    boolean hasTrash = false;
                    boolean hasJunk = false;
                    if (data.folders != null)
                        for (EntityFolder folder : data.folders)
                            if (folder.selectable)
                                if (EntityFolder.INBOX.equals(folder.type))
                                    hasInbox = true;
                                else if (EntityFolder.ARCHIVE.equals(folder.type))
                                    hasArchive = true;
                                else if (EntityFolder.TRASH.equals(folder.type))
                                    hasTrash = true;
                                else if (EntityFolder.JUNK.equals(folder.type))
                                    hasJunk = true;

                    boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                    boolean inSent = EntityFolder.SENT.equals(message.folderType);
                    boolean inTrash = EntityFolder.TRASH.equals(message.folderType);
                    boolean inJunk = EntityFolder.JUNK.equals(message.folderType);
                    boolean outbox = EntityFolder.OUTBOX.equals(message.folderType);

                    boolean move = !(message.folderReadOnly || message.uid == null) ||
                            (message.accountProtocol == EntityAccount.TYPE_POP &&
                                    EntityFolder.TRASH.equals(message.folderType));
                    boolean archive = (move && (hasArchive && !inArchive && !inSent && !inTrash && !inJunk));
                    boolean trash = (move || outbox || debug ||
                            message.accountProtocol == EntityAccount.TYPE_POP);
                    boolean inbox = (move && hasInbox && (inArchive || inTrash || inJunk));
                    boolean keywords = (!message.folderReadOnly && message.uid != null &&
                            message.accountProtocol == EntityAccount.TYPE_IMAP);
                    boolean labels = (data.isGmail && move && !inTrash && !inJunk && !outbox);
                    boolean seen = (!(message.folderReadOnly || message.uid == null) ||
                            message.accountProtocol == EntityAccount.TYPE_POP);

                    int froms = (message.from == null ? 0 : message.from.length);
                    int tos = (message.to == null ? 0 : message.to.length);

                    boolean delete = (inTrash || !hasTrash || inJunk || outbox ||
                            message.uid == null || message.accountProtocol == EntityAccount.TYPE_POP);

                    boolean headers = (message.uid != null ||
                            (message.accountProtocol == EntityAccount.TYPE_POP && message.headers != null));

                    boolean full = properties.getValue("full", message.id);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean expand_all = prefs.getBoolean("expand_all", false);
                    boolean expand_one = prefs.getBoolean("expand_one", true);
                    boolean tools = prefs.getBoolean("message_tools", true);
                    boolean button_junk = prefs.getBoolean("button_junk", true);
                    boolean button_trash = prefs.getBoolean("button_trash", true);
                    boolean button_archive = prefs.getBoolean("button_archive", true);
                    boolean button_move = prefs.getBoolean("button_move", true);
                    boolean button_copy = prefs.getBoolean("button_copy", false);
                    boolean button_keywords = prefs.getBoolean("button_keywords", false);
                    boolean button_notes = prefs.getBoolean("button_notes", false);
                    boolean button_seen = prefs.getBoolean("button_seen", false);
                    boolean button_hide = prefs.getBoolean("button_hide", false);
                    boolean button_translate = prefs.getBoolean("button_translate", false);
                    boolean button_search = prefs.getBoolean("button_search", false);
                    boolean button_search_text = prefs.getBoolean("button_search_text", false);
                    boolean button_event = prefs.getBoolean("button_event", false);
                    boolean button_share = prefs.getBoolean("button_share", false);
                    boolean button_pin = prefs.getBoolean("button_pin", false);
                    boolean button_print = prefs.getBoolean("button_print", false);
                    boolean button_headers = prefs.getBoolean("button_headers", false);
                    boolean button_unsubscribe = prefs.getBoolean("button_unsubscribe", true);
                    boolean button_rule = prefs.getBoolean("button_rule", false);
                    boolean button_extra = prefs.getBoolean("button_extra", false);

                    ibHide.setImageResource(message.ui_snoozed == null ? R.drawable.twotone_visibility_off_24 : R.drawable.twotone_visibility_24);
                    ibSeen.setImageResource(message.ui_seen ? R.drawable.twotone_mail_24 : R.drawable.twotone_drafts_24);
                    ibTrash.setTag(delete);
                    ibTrash.setImageResource(delete ? R.drawable.twotone_delete_forever_24 : R.drawable.twotone_delete_24);
                    ibTrashBottom.setImageResource(delete ? R.drawable.twotone_delete_forever_24 : R.drawable.twotone_delete_24);

                    ibUndo.setVisibility(outbox ? View.VISIBLE : View.GONE);
                    ibRule.setVisibility(tools && button_rule && !outbox && !message.folderReadOnly ? View.VISIBLE : View.GONE);
                    ibUnsubscribe.setVisibility(tools && button_unsubscribe && message.unsubscribe != null ? View.VISIBLE : View.GONE);
                    ibHeaders.setVisibility(tools && button_headers && headers ? View.VISIBLE : View.GONE);
                    ibPrint.setVisibility(tools && !outbox && button_print && hasWebView && message.content && Helper.canPrint(context) ? View.VISIBLE : View.GONE);
                    ibPin.setVisibility(tools && !outbox && button_pin && pin ? View.VISIBLE : View.GONE);
                    ibShare.setVisibility(tools && !outbox && button_share && message.content ? View.VISIBLE : View.GONE);
                    ibEvent.setVisibility(tools && !outbox && button_event && message.content ? View.VISIBLE : View.GONE);
                    ibSearchText.setVisibility(tools && !outbox && button_search_text && message.content && !full ? View.VISIBLE : View.GONE);
                    ibSearch.setVisibility(tools && !outbox && button_search && (froms > 0 || tos > 0) ? View.VISIBLE : View.GONE);
                    ibTranslate.setVisibility(tools && !outbox && button_translate && DeepL.isAvailable(context) && message.content ? View.VISIBLE : View.GONE);
                    ibHide.setVisibility(tools && button_hide && !outbox ? View.VISIBLE : View.GONE);
                    ibSeen.setVisibility(tools && button_seen && !outbox && seen ? View.VISIBLE : View.GONE);
                    ibAnswer.setVisibility(!tools || outbox || (!expand_all && expand_one) ? View.GONE : View.VISIBLE);
                    ibNotes.setVisibility(tools && button_notes && !outbox ? View.VISIBLE : View.GONE);
                    ibLabels.setVisibility(tools && labels_header && labels ? View.VISIBLE : View.GONE);
                    ibKeywords.setVisibility(tools && button_keywords && keywords ? View.VISIBLE : View.GONE);
                    ibCopy.setVisibility(tools && button_copy && move ? View.VISIBLE : View.GONE);
                    ibMove.setVisibility(tools && button_move && move ? View.VISIBLE : View.GONE);
                    ibArchive.setVisibility(tools && button_archive && archive ? View.VISIBLE : View.GONE);
                    ibTrash.setVisibility(outbox || (tools && button_trash && trash) ? View.VISIBLE : View.GONE);
                    ibJunk.setVisibility(tools && button_junk && move ? View.VISIBLE : View.GONE);
                    ibInbox.setVisibility(tools && inbox ? View.VISIBLE : View.GONE);
                    ibMore.setVisibility(tools && !outbox ? View.VISIBLE : View.GONE);
                    ibTools.setImageLevel(tools ? 0 : 1);
                    ibTools.setContentDescription(context.getString(tools ? R.string.title_less : R.string.title_more));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        ibTools.setTooltipText(ibTools.getContentDescription());
                    ibTools.setVisibility(outbox ? View.GONE : View.VISIBLE);

                    ibTrashBottom.setVisibility(button_extra && button_trash && trash ? View.VISIBLE : View.GONE);
                    ibArchiveBottom.setVisibility(button_extra && button_archive && archive ? View.VISIBLE : View.GONE);
                    ibMoveBottom.setVisibility(button_extra && button_move && move ? View.VISIBLE : View.GONE);

                    if (bind)
                        bindBody(message, scroll);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.setLog(false).execute(context, owner, sargs, "message:tools");
        }

        private Spanned formatAddresses(Address[] addresses, boolean full) {
            return formatAddresses(addresses, full, Integer.MAX_VALUE);
        }

        private Spanned formatAddresses(Address[] addresses, boolean full, int max) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();

            if (addresses == null || addresses.length == 0)
                return ssb;

            for (int i = 0; i < addresses.length && i < max; i++) {
                if (i > 0)
                    ssb.append("; ");

                if (addresses[i] instanceof InternetAddress) {
                    InternetAddress address = (InternetAddress) addresses[i];
                    String email = address.getAddress();
                    String personal = address.getPersonal();

                    if (TextUtils.isEmpty(personal)) {
                        if (!TextUtils.isEmpty(email)) {
                            int start = ssb.length();
                            ssb.append(email);
                            ssb.setSpan(new ForegroundColorSpan(textColorLink), start, ssb.length(), 0);
                        }
                    } else {
                        if (full) {
                            ssb.append(personal).append(" <");
                            if (!TextUtils.isEmpty(email)) {
                                int start = ssb.length();
                                ssb.append(email);
                                ssb.setSpan(new ForegroundColorSpan(textColorLink), start, ssb.length(), 0);
                            }
                            ssb.append(">");
                        } else
                            ssb.append(personal);
                    }
                } else
                    ssb.append(addresses[i].toString());
            }

            if (addresses.length > max)
                ssb.append(context.getString(R.string.title_name_plus, "", addresses.length - max));

            return ssb;
        }

        private void bindAddresses(TupleMessageEx message) {
            boolean show_addresses = properties.getValue("addresses", message.id);
            boolean full = (show_addresses ||
                    email_format == MessageHelper.AddressFormat.NAME_EMAIL ||
                    EntityFolder.JUNK.equals(message.folderType));

            int froms = (message.from == null ? 0 : message.from.length);
            int tos = (message.to == null ? 0 : message.to.length);
            boolean hasChannel = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
            int maxRecipients = (compact ? MAX_RECIPIENTS_COMPACT : MAX_RECIPIENTS_NORMAL);
            Spanned submitter = formatAddresses(message.submitter, true);
            Spanned from = formatAddresses(message.senders, true);
            Spanned replyto = formatAddresses(message.reply, true);

            grpAddresses.setVisibility(View.VISIBLE);

            ibExpanderAddress.setImageLevel(show_addresses ? 0 /* less */ : 1 /* more */);
            ibExpanderAddress.setContentDescription(context.getString(show_addresses ? R.string.title_accessibility_hide_addresses : R.string.title_accessibility_show_addresses));

            ivPlain.setVisibility(show_addresses && message.plain_only != null && message.plain_only ? View.VISIBLE : View.GONE);
            ibReceipt.setVisibility(message.receipt_request != null && message.receipt_request ? View.VISIBLE : View.GONE);
            ibReceipt.setImageTintList(ColorStateList.valueOf(message.ui_answered ? colorControlNormal : colorError));
            ivAutoSubmitted.setVisibility(show_addresses && message.auto_submitted != null && message.auto_submitted ? View.VISIBLE : View.GONE);
            ivBrowsed.setVisibility(show_addresses && message.ui_browsed ? View.VISIBLE : View.GONE);

            boolean button_search = prefs.getBoolean("button_search", false);
            ibSearchContact.setVisibility(show_addresses && (froms > 0 || tos > 0) && !button_search ? View.VISIBLE : View.GONE);
            ibNotifyContact.setVisibility(show_addresses && hasChannel && froms > 0 ? View.VISIBLE : View.GONE);
            ibPinContact.setVisibility(show_addresses && pin && froms > 0 ? View.VISIBLE : View.GONE);
            ibAddContact.setVisibility(show_addresses && contacts && froms > 0 ? View.VISIBLE : View.GONE);

            tvSubmitterTitle.setVisibility(!TextUtils.isEmpty(submitter) ? View.VISIBLE : View.GONE);
            tvSubmitter.setVisibility(!TextUtils.isEmpty(submitter) ? View.VISIBLE : View.GONE);
            tvSubmitter.setText(submitter);

            InternetAddress deliveredto = new InternetAddress();
            deliveredto.setAddress(message.deliveredto);
            tvDeliveredToTitle.setVisibility(show_addresses && !TextUtils.isEmpty(message.deliveredto) ? View.VISIBLE : View.GONE);
            tvDeliveredTo.setVisibility(show_addresses && !TextUtils.isEmpty(message.deliveredto) ? View.VISIBLE : View.GONE);
            tvDeliveredTo.setText(formatAddresses(new Address[]{deliveredto}, true));

            tvFromExTitle.setVisibility((froms > 1 || show_addresses) && !TextUtils.isEmpty(from) ? View.VISIBLE : View.GONE);
            tvFromEx.setVisibility((froms > 1 || show_addresses) && !TextUtils.isEmpty(from) ? View.VISIBLE : View.GONE);
            tvFromEx.setText(from);

            tvToTitle.setVisibility((!show_recipients || show_addresses) && (message.to != null && message.to.length > 0) ? View.VISIBLE : View.GONE);
            tvTo.setVisibility((!show_recipients || show_addresses) && (message.to != null && message.to.length > 0) ? View.VISIBLE : View.GONE);
            tvTo.setText(formatAddresses(message.to, full, show_addresses ? Integer.MAX_VALUE : maxRecipients));

            tvReplyToTitle.setVisibility(show_addresses && !TextUtils.isEmpty(replyto) ? View.VISIBLE : View.GONE);
            tvReplyTo.setVisibility(show_addresses && !TextUtils.isEmpty(replyto) ? View.VISIBLE : View.GONE);
            tvReplyTo.setText(replyto);

            tvCcTitle.setVisibility(message.cc == null || message.cc.length == 0 ? View.GONE : View.VISIBLE);
            tvCc.setVisibility(message.cc == null || message.cc.length == 0 ? View.GONE : View.VISIBLE);
            tvCc.setText(formatAddresses(message.cc, full, show_addresses ? Integer.MAX_VALUE : maxRecipients));

            tvBccTitle.setVisibility(message.bcc == null || message.bcc.length == 0 ? View.GONE : View.VISIBLE);
            tvBcc.setVisibility(message.bcc == null || message.bcc.length == 0 ? View.GONE : View.VISIBLE);
            tvBcc.setText(formatAddresses(message.bcc, full, show_addresses ? Integer.MAX_VALUE : maxRecipients));

            InternetAddress via = null;
            if (message.identityEmail != null)
                try {
                    via = new InternetAddress(message.identityEmail, message.identityName, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException ignored) {
                }

            tvIdentityTitle.setVisibility(show_addresses && via != null ? View.VISIBLE : View.GONE);
            tvIdentity.setVisibility(show_addresses && via != null ? View.VISIBLE : View.GONE);
            tvIdentity.setText(via == null ? null : formatAddresses(new Address[]{via}, true));

            tvSentTitle.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvSent.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvSent.setText(message.sent == null ? null : DTF.format(message.sent));

            tvReceivedTitle.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvReceived.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvReceived.setText(DTF.format(message.received));

            tvStoredTitle.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvStored.setVisibility(show_addresses ? View.VISIBLE : View.GONE);
            tvStored.setText(DTF.format(message.stored));

            if (!message.duplicate)
                tvSizeEx.setAlpha(message.content ? 1.0f : Helper.LOW_LIGHT);
            tvSizeExTitle.setVisibility(!show_addresses || message.size == null ? View.GONE : View.VISIBLE);
            tvSizeEx.setVisibility(!show_addresses || (message.size == null && message.total == null) ? View.GONE : View.VISIBLE);
            StringBuilder size = new StringBuilder();
            size
                    .append(message.size == null ? "-" : Helper.humanReadableByteCount(message.size))
                    .append("/")
                    .append(message.total == null ? "-" : Helper.humanReadableByteCount(message.total));
            tvSizeEx.setText(size.toString());

            boolean showLanguage = (language_detection && message.language != null &&
                    (show_addresses ||
                            (languages != null && !languages.contains(message.language))));
            tvLanguageTitle.setVisibility(showLanguage ? View.VISIBLE : View.GONE);
            tvLanguage.setVisibility(showLanguage ? View.VISIBLE : View.GONE);
            tvLanguage.setText(message.language == null ? null : new Locale(message.language).getDisplayLanguage());

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
            if (keywords_header) {
                tvKeywordsEx.setVisibility(show_addresses && message.keywords.length > 0 ? View.VISIBLE : View.GONE);
                tvKeywordsEx.setText(TextUtils.join(" ", message.keywords));
            } else {
                SpannableStringBuilder keywords = getKeywords(message);
                tvKeywordsEx.setVisibility(show_addresses && keywords.length() > 0 ? View.VISIBLE : View.GONE);
                tvKeywordsEx.setText(keywords);
            }
        }

        private void bindHeaders(TupleMessageEx message, boolean scroll) {
            boolean show_headers = properties.getValue("headers", message.id);

            grpHeaders.setVisibility(show_headers ? View.VISIBLE : View.GONE);
            if (show_headers && message.headers == null) {
                pbHeaders.setVisibility(suitable ? View.VISIBLE : View.GONE);
                tvNoInternetHeaders.setVisibility(suitable ? View.GONE : View.VISIBLE);
            } else {
                pbHeaders.setVisibility(View.GONE);
                tvNoInternetHeaders.setVisibility(View.GONE);
            }

            if (show_headers && message.headers != null) {
                tvHeaders.setText(HtmlHelper.highlightHeaders(context,
                        message.headers, message.blocklist != null && message.blocklist));
                ibCopyHeaders.setVisibility(View.VISIBLE);
            } else {
                tvHeaders.setText(null);
                ibCopyHeaders.setVisibility(View.GONE);
            }

            if (scroll)
                ApplicationEx.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        View inHeaders = itemView.findViewById(R.id.inHeaders);

                        Rect rect = new Rect();
                        inHeaders.getDrawingRect(rect);
                        ((ViewGroup) itemView).offsetDescendantRectToMyCoords(inHeaders, rect);

                        properties.scrollTo(getAdapterPosition(), rect.top);
                    }
                });
        }

        private void bindBody(TupleMessageEx message, final boolean scroll) {
            if (!Objects.equals(tvBody.getTag(), message.id)) {
                tvBody.setTag(message.id);
                tvBody.setText(null);
            }
            properties.endSearch();
            clearActions();

            ibSeenBottom.setImageResource(message.ui_seen
                    ? R.drawable.twotone_mail_24 : R.drawable.twotone_drafts_24);
            ibSeenBottom.setVisibility(!(message.folderReadOnly || message.uid == null) ||
                    message.accountProtocol == EntityAccount.TYPE_POP
                    ? View.VISIBLE : View.GONE);

            if (!message.content) {
                if (scroll)
                    properties.scrollTo(getAdapterPosition(), 0);
                return;
            }

            if (message.from != null)
                for (Address sender : message.from) {
                    String from = ((InternetAddress) sender).getAddress();
                    if (TextUtils.isEmpty(from))
                        continue;
                    int at = from.indexOf('@');
                    String domain = (at < 0 ? from : from.substring(at));
                    if (message.show_full ||
                            prefs.getBoolean(from + ".show_full", false) ||
                            prefs.getBoolean(domain + ".show_full", false)) {
                        properties.setValue("full", message.id, true);
                        properties.setValue("full_asked", message.id, true);
                    }
                    if (message.show_images ||
                            prefs.getBoolean(from + ".show_images", false) ||
                            prefs.getBoolean(domain + ".show_images", false)) {
                        properties.setValue("images", message.id, true);
                        properties.setValue("images_asked", message.id, true);
                    }
                }

            boolean confirm_images = prefs.getBoolean("confirm_images", true);
            if (!confirm_images &&
                    !EntityFolder.JUNK.equals(message.folderType) &&
                    !properties.getValue("images_asked", message.id)) {
                properties.setValue("images", message.id, true);
                properties.setValue("images_asked", message.id, true);
            }

            boolean confirm_html = prefs.getBoolean("confirm_html", true);
            if (!confirm_html &&
                    !EntityFolder.JUNK.equals(message.folderType) &&
                    !properties.getValue("full_asked", message.id)) {
                properties.setValue("full", message.id, true);
                properties.setValue("full_asked", message.id, true);
            }

            boolean show_full = properties.getValue("full", message.id);
            boolean show_images = properties.getValue("images", message.id);
            boolean show_quotes = (properties.getValue("quotes", message.id) || !collapse_quotes);

            boolean always_images = prefs.getBoolean("html_always_images", false);
            if (always_images && show_full) {
                show_images = true;
                properties.setValue("images", message.id, true);
            }

            float size = properties.getSize(message.id, show_full ? 0 : textSize * message_zoom / 100f);
            int height = properties.getHeight(message.id, dp60);
            Pair<Integer, Integer> position = properties.getPosition(message.id);
            Log.i("Bind size=" + size + " height=" + height);

            ibFull.setEnabled(hasWebView);
            ibFull.setImageResource(show_full ? R.drawable.twotone_fullscreen_exit_24 : R.drawable.twotone_fullscreen_24);
            ibFull.setContentDescription(context.getString(show_full
                    ? R.string.title_legend_show_reformatted
                    : R.string.title_legend_show_full));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ibFull.setTooltipText(ibFull.getContentDescription());

            ibImages.setImageResource(show_images ? R.drawable.twotone_article_24 : R.drawable.twotone_image_24);
            ibImages.setContentDescription(context.getString(show_images
                    ? R.string.title_legend_hide_images
                    : R.string.title_legend_show_images));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ibImages.setTooltipText(ibImages.getContentDescription());

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
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex, false);
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

                webView.setMinimumHeight(height);

                webView.init(height, size, position,
                        new WebViewEx.IWebView() {
                            @Override
                            public void onSizeChanged(int w, int h, int ow, int oh) {
                                if (h > dp60)
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
                            public void onOverScrolled(int scrollX, int scrollY, int dx, int dy, boolean clampedX, boolean clampedY) {
                                if (clampedY && ((WebViewEx) wvBody).isZoomedY())
                                    properties.scrollBy(0, dy);
                            }

                            @Override
                            public boolean onOpenLink(String url) {
                                if (parentFragment == null)
                                    return false;

                                Uri uri = Uri.parse(url);
                                return ViewHolder.this.onOpenLink(uri, null, false);
                            }
                        });
                webView.setImages(show_images, inline);
                webView.setOnTouchListener(touchListener);

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

            float scale = (size == 0 || textSize == 0 ? 1.0f : size / (textSize * message_zoom / 100f));
            args.putFloat("scale", scale);

            new SimpleTask<Object>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    pbBody.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(Bundle args) {
                    pbBody.setVisibility(View.GONE);
                }

                @Override
                protected Object onExecute(final Context context, final Bundle args) throws IOException {
                    TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                    final boolean show_full = args.getBoolean("show_full");
                    final boolean show_images = args.getBoolean("show_images");
                    final boolean show_quotes = args.getBoolean("show_quotes");
                    final int zoom = args.getInt("zoom");
                    final float scale = args.getFloat("scale");

                    if (message == null || !message.content)
                        return null;

                    DB db = DB.getInstance(context);
                    List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                    properties.setAttachments(message.id, attachments);

                    boolean signed_data = false;
                    for (EntityAttachment attachment : attachments)
                        if (EntityAttachment.SMIME_SIGNED_DATA.equals(attachment.encryption)) {
                            signed_data = true;
                            break;
                        }

                    File file = message.getFile(context);
                    if (!file.exists())
                        return null;

                    if (file.length() > 0)
                        signed_data = false;
                    args.putBoolean("signed_data", signed_data);

                    Document document = JsoupEx.parse(file);

                    // Add embedded messages
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available && "message/rfc822".equals(attachment.getMimeType()))
                            try (FileInputStream fis = new FileInputStream(attachment.getFile(context))) {
                                Properties props = MessageHelper.getSessionProperties();
                                Session isession = Session.getInstance(props, null);
                                MimeMessage imessage = new MimeMessage(isession, fis);
                                MessageHelper helper = new MessageHelper(imessage, context);
                                MessageHelper.MessageParts parts = helper.getMessageParts();

                                EntityMessage embedded = new EntityMessage();
                                embedded.from = helper.getFrom();
                                embedded.to = helper.getTo();
                                embedded.cc = helper.getCc();
                                embedded.received = helper.getReceivedHeader();
                                if (embedded.received == null)
                                    embedded.received = helper.getSent();
                                embedded.subject = helper.getSubject();

                                String html = parts.getHtml(context);
                                Document d = (html == null ? Document.createShell("") : JsoupEx.parse(html));

                                Element div = document.createElement("div");
                                div.appendElement("hr");

                                Element h = document.createElement("p");
                                h.attr("style", "text-align: center;");

                                Element em = document.createElement("em");
                                em.text(TextUtils.isEmpty(attachment.name)
                                        ? context.getString(R.string.title_attachment_eml) : attachment.name);
                                h.appendChild(em);

                                div.appendChild(h);

                                Element p = embedded.getReplyHeader(context, document, false, true);
                                div.appendChild(p);

                                div.appendChild(d.body().tagName("p"));

                                document.body().appendChild(div);
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }

                    HtmlHelper.cleanup(document);

                    // Check for inline encryption
                    boolean iencrypted = HtmlHelper.contains(document, new String[]{
                            Helper.PGP_BEGIN_MESSAGE,
                            Helper.PGP_END_MESSAGE
                    });
                    args.putBoolean("inline_encrypted", iencrypted);

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
                    if (show_images)
                        try {
                            db.beginTransaction();

                            for (EntityAttachment attachment : attachments)
                                if (attachment.isInline() && attachment.isImage() &&
                                        attachment.progress == null && !attachment.available)
                                    EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.id);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                    // Format message
                    if (show_full) {
                        if (HtmlHelper.truncate(document, HtmlHelper.MAX_FULL_TEXT_SIZE))
                            document.body()
                                    .appendElement("br")
                                    .appendElement("p")
                                    .appendElement("em")
                                    .text(context.getString(R.string.title_truncated));

                        boolean overview_mode = prefs.getBoolean("overview_mode", false);
                        HtmlHelper.setViewport(document, overview_mode);
                        if (inline || show_images)
                            HtmlHelper.embedInlineImages(context, message.id, document, show_images);

                        boolean disable_tracking = prefs.getBoolean("disable_tracking", true);
                        if (disable_tracking)
                            HtmlHelper.removeTrackingPixels(context, document);

                        if (debug) {
                            Document format = JsoupEx.parse(file);
                            format.outputSettings().prettyPrint(true).outline(true).indentAmount(1);
                            Element pre = document.createElement("pre");
                            pre.text(format.html());
                            document.body().appendChild(pre);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            args.putParcelable("actions", getConversationActions(message, document, context));

                        return document.html();
                    } else {
                        // Cleanup message
                        document = HtmlHelper.sanitizeView(context, document, show_images);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            args.putParcelable("actions", getConversationActions(message, document, context));

                        // Collapse quotes
                        if (!show_quotes)
                            HtmlHelper.collapseQuotes(document);

                        // Draw images
                        SpannableStringBuilder ssb = HtmlHelper.fromDocument(context, document, new Html.ImageGetter() {
                            @Override
                            public Drawable getDrawable(String source) {
                                Drawable drawable = ImageHelper.decodeImage(context, message.id, source, show_images, zoom, scale, tvBody);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    if (drawable instanceof AnimatedImageDrawable)
                                        ((AnimatedImageDrawable) drawable).start();
                                }

                                return drawable;
                            }
                        }, null);

                        if (show_quotes)
                            return ssb;

                        // Replace quote spans
                        final int px = Helper.dp2pixels(context, 24 + (zoom) * 8);
                        QuoteSpan[] quoteSpans = ssb.getSpans(0, ssb.length(), QuoteSpan.class);
                        for (QuoteSpan quoteSpan : quoteSpans) {
                            int s = ssb.getSpanStart(quoteSpan);
                            int e = ssb.getSpanEnd(quoteSpan);
                            ssb.setSpan(
                                    new DynamicDrawableSpan() {
                                        @Override
                                        public Drawable getDrawable() {
                                            Drawable d = context.getDrawable(R.drawable.twotone_format_quote_24);
                                            d.setTint(colorAccent);
                                            d.setBounds(0, 0, px, px);
                                            return d;
                                        }
                                    },
                                    s, e, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        return ssb;
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

                    boolean show_full = args.getBoolean("show_full");
                    boolean has_images = args.getBoolean("has_images");
                    boolean always_images = prefs.getBoolean("html_always_images", false);

                    // Show images
                    ibImages.setVisibility(has_images && !(show_full && always_images) ? View.VISIBLE : View.GONE);

                    // Show encrypt actions
                    ibVerify.setVisibility(false ||
                            EntityMessage.PGP_SIGNONLY.equals(message.encrypt) ||
                            EntityMessage.SMIME_SIGNONLY.equals(message.encrypt)
                            ? View.VISIBLE : View.GONE);
                    ibDecrypt.setImageResource(false ||
                            (EntityMessage.PGP_SIGNENCRYPT.equals(message.ui_encrypt) &&
                                    !EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt)) ||
                            (EntityMessage.SMIME_SIGNENCRYPT.equals(message.ui_encrypt) &&
                                    !EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt))
                            ? R.drawable.twotone_lock_24 : R.drawable.twotone_lock_open_24
                    );
                    ibDecrypt.setVisibility(!EntityFolder.OUTBOX.equals(message.folderType) &&
                            (args.getBoolean("inline_encrypted") ||
                                    EntityMessage.PGP_SIGNENCRYPT.equals(message.ui_encrypt) ||
                                    EntityMessage.SMIME_SIGNENCRYPT.equals(message.ui_encrypt))
                            ? View.VISIBLE : View.GONE);

                    boolean reformatted_hint = prefs.getBoolean("reformatted_hint", true);
                    tvReformatted.setVisibility(reformatted_hint ? View.VISIBLE : View.GONE);

                    boolean signed_data = args.getBoolean("signed_data");
                    tvSignedData.setVisibility(signed_data ? View.VISIBLE : View.GONE);

                    if (show_full) {
                        ((WebViewEx) wvBody).setOnPageLoaded(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                        bindConversationActions(message, args.getParcelable("actions"));

                                    cowner.start(); // Show attachments
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }
                            }
                        });

                        if (result == null)
                            ((WebView) wvBody).loadDataWithBaseURL(null, "", "text/html", StandardCharsets.UTF_8.name(), null);
                        else
                            ((WebView) wvBody).loadDataWithBaseURL(null, (String) result, "text/html", StandardCharsets.UTF_8.name(), null);
                    } else {
                        tvBody.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    tvBody.setText((Spanned) result);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                        bindConversationActions(message, args.getParcelable("actions"));

                                    cowner.start(); // Show attachments
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }
                            }
                        });
                    }

                    if (scroll)
                        properties.scrollTo(getAdapterPosition(), 0);

                    boolean auto_decrypt = prefs.getBoolean("auto_decrypt", false);
                    if (auto_decrypt &&
                            (EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) ||
                                    EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt)))
                        onActionDecrypt(message, true);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    if (ex instanceof OutOfMemoryError)
                        Snackbar.make(parentFragment.getView(), ex.getMessage(), Snackbar.LENGTH_LONG)
                                .setGestureInsetBottomIgnored(true).show();
                    else
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                private ConversationActions getConversationActions(TupleMessageEx message, Document document, Context context) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean conversation_actions = prefs.getBoolean("conversation_actions", true);
                    boolean conversation_actions_replies = prefs.getBoolean("conversation_actions_replies", true);
                    if (!conversation_actions)
                        return null;

                    List<String> texts = new ArrayList<>();
                    if (!TextUtils.isEmpty(message.subject))
                        texts.add(message.subject);
                    texts.add(document.text());

                    return TextHelper.getConversationActions(
                            context,
                            texts.toArray(new String[0]),
                            conversation_actions_replies,
                            isOutgoing(message),
                            message.received);
                }
            }.setCount(false).execute(context, owner, args, "message:body");
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        private void bindConversationActions(TupleMessageEx message, ConversationActions cactions) {
            boolean has = false;
            if (cactions != null) {
                List<ConversationAction> actions = cactions.getConversationActions();
                for (final ConversationAction action : actions) {
                    final CharSequence text;
                    final CharSequence title;
                    final String type = action.getType();
                    final RemoteAction raction = action.getAction();

                    switch (type) {
                        case ConversationAction.TYPE_TEXT_REPLY:
                            text = action.getTextReply();
                            title = context.getString(R.string.title_conversation_action_reply, text);
                            break;
                        case "copy":
                            Bundle extras = action.getExtras().getParcelable("entities-extras");
                            if (extras == null)
                                continue;
                            text = extras.getString("text");
                            title = context.getString(R.string.title_conversation_action_copy, text);
                            break;
                        default:
                            if (raction == null) {
                                Log.w("Unknown action type=" + type);
                                continue;
                            }
                            text = null;
                            title = raction.getTitle();
                            if (TextUtils.isEmpty(title)) {
                                Log.e("Empty action type=" + type);
                                continue;
                            }
                    }

                    Button button = new Button(context, null, android.R.attr.buttonStyleSmall);
                    button.setId(View.generateViewId());
                    button.setText(title);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                switch (type) {
                                    case ConversationAction.TYPE_TEXT_REPLY:
                                        onReply();
                                        break;
                                    case "copy":
                                        onCopy();
                                        break;
                                    default:
                                        raction.getActionIntent().send();
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }

                        private void onReply() {
                            Intent reply = new Intent(context, ActivityCompose.class)
                                    .putExtra("action", "reply")
                                    .putExtra("reference", message.id)
                                    .putExtra("text", action.getTextReply());
                            context.startActivity(reply);
                        }

                        private void onCopy() {
                            ClipboardManager clipboard =
                                    (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboard == null)
                                return;

                            ClipData clip = ClipData.newPlainText(title, text);
                            clipboard.setPrimaryClip(clip);
                            ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
                        }
                    });

                    ((ConstraintLayout) flow.getParent()).addView(button);
                    flow.addView(button);
                    has = true;
                }
                grpAction.setVisibility(has ? View.VISIBLE : View.GONE);
            }
        }

        private void bindAttachments(final TupleMessageEx message, @Nullable List<EntityAttachment> attachments, boolean bind_extras) {
            if (attachments == null)
                attachments = new ArrayList<>();
            properties.setAttachments(message.id, attachments);

            grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

            boolean show_inline = properties.getValue("inline", message.id);
            Log.i("Show inline=" + show_inline);

            boolean has_inline = false;
            int download = 0;
            boolean save = (attachments.size() > 1);
            boolean downloading = false;
            EntityAttachment calendar = null;

            List<EntityAttachment> a = new ArrayList<>();
            for (EntityAttachment attachment : attachments) {
                boolean inline = (attachment.isEncryption() ||
                        (attachment.isInline() && attachment.isImage()));
                if (inline && attachment.available)
                    has_inline = true;
                if (attachment.progress == null && !attachment.available)
                    download++;
                if (!attachment.available)
                    save = false;
                if (attachment.progress != null)
                    downloading = true;

                if (show_inline || !inline || !attachment.available)
                    a.add(attachment);

                if (attachment.available &&
                        "text/calendar".equals(attachment.getMimeType()))
                    calendar = attachment;
            }

            rvAttachment.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        adapterAttachment.set(a);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });

            if (calendar != null && bind_extras)
                bindCalendar(message, calendar);

            cbInline.setOnCheckedChangeListener(null);
            cbInline.setChecked(show_inline);
            cbInline.setVisibility(has_inline ? View.VISIBLE : View.GONE);

            ibSaveAttachments.setVisibility(save ? View.VISIBLE : View.GONE);
            ibDownloadAttachments.setVisibility(download > 1 && suitable ? View.VISIBLE : View.GONE);
            tvNoInternetAttachments.setVisibility(downloading && !suitable ? View.VISIBLE : View.GONE);

            cbInline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    properties.setValue("inline", message.id, isChecked);
                    cowner.restart();
                    bindAttachments(message, properties.getAttachments(message.id), true);
                }
            });

            List<EntityAttachment> images = new ArrayList<>();
            if (thumbnails && bind_extras)
                for (EntityAttachment attachment : attachments)
                    if (attachment.isAttachment() && attachment.isImage())
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

                    boolean show_expanded = properties.getValue("expanded", message.id);
                    if (!show_expanded)
                        return;

                    if (icalendar == null ||
                            icalendar.getEvents().size() == 0) {
                        clearCalendar();
                        grpCalendar.setVisibility(View.GONE);
                        grpCalendarResponse.setVisibility(View.GONE);
                        return;
                    }

                    DateFormat DTF = Helper.getDateTimeInstance(context);

                    VEvent event = icalendar.getEvents().get(0);

                    String summary = (event.getSummary() == null ? null : event.getSummary().getValue());
                    String description = (event.getDescription() == null ? null : event.getDescription().getValue());
                    String location = (event.getLocation() == null ? null : event.getLocation().getValue());

                    if (false) {
                        RawProperty xAltDesc = event.getExperimentalProperty("X-ALT-DESC");
                        if (xAltDesc != null &&
                                xAltDesc.getValue() != null &&
                                "text/html".equals(xAltDesc.getParameter("FMTTYPE")))
                            description = xAltDesc.getValue();
                    }

                    ICalDate start = (event.getDateStart() == null ? null : event.getDateStart().getValue());
                    ICalDate end = (event.getDateEnd() == null ? null : event.getDateEnd().getValue());

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

                    if (summary != null)
                        summary = summary.trim();
                    if (description != null)
                        description = description.trim();
                    if (location != null)
                        location = location.trim();

                    Organizer organizer = event.getOrganizer();

                    ibCalendar.setVisibility(View.VISIBLE);

                    tvCalendarSummary.setText(summary);
                    tvCalendarSummary.setVisibility(TextUtils.isEmpty(summary) ? View.GONE : View.VISIBLE);

                    if (description == null)
                        tvCalendarDescription.setText(description);
                    else
                        try {
                            tvCalendarDescription.setText(HtmlHelper.fromHtml(description, context));
                        } catch (Throwable ex) {
                            Log.w(ex);
                            tvCalendarDescription.setText(description);
                        }
                    tvCalendarDescription.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);

                    tvCalendarLocation.setText(location);
                    tvCalendarLocation.setVisibility(TextUtils.isEmpty(location) ? View.GONE : View.VISIBLE);

                    tvCalendarStart.setText(start == null ? null : DTF.format(start.getTime()));
                    tvCalendarStart.setVisibility(start == null ? View.GONE : View.VISIBLE);

                    tvCalendarEnd.setText(end == null ? null : DTF.format(end.getTime()));
                    tvCalendarEnd.setVisibility(end == null ? View.GONE : View.VISIBLE);

                    tvAttendees.setText(TextUtils.join(", ", attendee));
                    tvAttendees.setVisibility(attendee.size() == 0 ? View.GONE : View.VISIBLE);

                    boolean canRespond =
                            (icalendar.getMethod() != null &&
                                    icalendar.getMethod().isRequest() &&
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

        private void onActionCalendar(TupleMessageEx message, int action, boolean share) {
            if (action != R.id.ibCalendar && !ActivityBilling.isPro(context)) {
                context.startActivity(new Intent(context, ActivityBilling.class));
                return;
            }

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putInt("action", action);
            args.putBoolean("share", share);

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
                        if (attachment.available && "text/calendar".equals(attachment.getMimeType())) {
                            File file = attachment.getFile(context);
                            ICalendar icalendar = Biweekly.parse(file).first();
                            VEvent event = icalendar.getEvents().get(0);

                            if (action == R.id.ibCalendar) {
                                String summary = (event.getSummary() == null ? null : event.getSummary().getValue());
                                String description = (event.getDescription() == null ? null : event.getDescription().getValue());
                                String location = (event.getLocation() == null ? null : event.getLocation().getValue());

                                ICalDate start = (event.getDateStart() == null ? null : event.getDateStart().getValue());
                                ICalDate end = (event.getDateEnd() == null ? null : event.getDateEnd().getValue());

                                List<String> attendee = new ArrayList<>();
                                for (Attendee a : event.getAttendees()) {
                                    String email = a.getEmail();
                                    if (!TextUtils.isEmpty(email))
                                        attendee.add(email);
                                }

                                int status;
                                if (icalendar.getMethod() == null)
                                    status = CalendarContract.Events.STATUS_TENTATIVE;
                                else if (icalendar.getMethod().isRequest())
                                    status = CalendarContract.Events.STATUS_TENTATIVE;
                                else if (icalendar.getMethod().isCancel())
                                    status = CalendarContract.Events.STATUS_CANCELED;
                                else
                                    status = CalendarContract.Events.STATUS_CONFIRMED;

                                // https://developer.android.com/guide/topics/providers/calendar-provider.html#intent-insert
                                Intent intent = new Intent(Intent.ACTION_INSERT)
                                        .setData(CalendarContract.Events.CONTENT_URI)
                                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                                        .putExtra(CalendarContract.Events.STATUS, status);

                                if (summary != null)
                                    intent.putExtra(CalendarContract.Events.TITLE, summary);

                                if (description != null)
                                    intent.putExtra(CalendarContract.Events.DESCRIPTION, description);

                                if (location != null)
                                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);

                                if (start != null)
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.getTime());

                                if (end != null)
                                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTime());

                                // This will result in sending unwanted invites
                                //if (attendee.size() > 0)
                                //    intent.putExtra(Intent.EXTRA_EMAIL, TextUtils.join(",", attendee));

                                return intent;
                            }

                            // https://tools.ietf.org/html/rfc5546#section-4.2.2
                            VEvent ev = new VEvent();

                            ev.setOrganizer(event.getOrganizer());

                            ev.setUid(event.getUid());
                            if (event.getSequence() != null)
                                ev.setSequence(event.getSequence());

                            ev.setSummary(event.getSummary());
                            ev.setDescription(event.getDescription());
                            ev.setLocation(event.getLocation());

                            if (event.getDateStart() != null)
                                ev.setDateStart(event.getDateStart());
                            if (event.getDateEnd() != null)
                                ev.setDateEnd(event.getDateEnd());

                            InternetAddress to = null;

                            if (message.identity != null) {
                                EntityIdentity identity = db.identity().getIdentity(message.identity);
                                if (identity != null) {
                                    InternetAddress same = null;
                                    InternetAddress similar = null;
                                    for (Address recipient : message.to)
                                        if (identity.sameAddress(recipient))
                                            same = (InternetAddress) recipient;
                                        else if (identity.similarAddress(recipient))
                                            similar = (InternetAddress) recipient;

                                    if (same != null)
                                        to = same;
                                    else if (similar != null)
                                        to = similar;
                                }
                            }

                            if (to == null)
                                to = (InternetAddress) message.to[0];

                            String email = to.getAddress();
                            String name = to.getPersonal();

                            /*
                                java.lang.IllegalArgumentException:
                                Property "ATTENDEE" has a parameter named "CN" whose value contains one or more invalid characters.
                                The following characters are not permitted: [ \n \r " ]
                                  at b.b.a.a.f.h.k(SourceFile:17)
                                  at b.b.a.a.f.h.o(SourceFile:1)
                                  at biweekly.io.text.ICalWriter.writeProperty(SourceFile:9)
                                  at biweekly.io.text.ICalWriter.writeComponent(SourceFile:13)
                                  at biweekly.io.text.ICalWriter.writeComponent(SourceFile:21)
                                  at biweekly.io.text.ICalWriter._write(SourceFile:1)
                                  at biweekly.io.StreamWriter.write(SourceFile:9)
                                  at biweekly.io.chain.ChainingTextWriter.go(SourceFile:18)
                                  at biweekly.io.chain.ChainingTextWriter.go(SourceFile:3)
                                  at biweekly.io.chain.ChainingTextWriter.go(SourceFile:1)
                                  at biweekly.ICalendar.write(SourceFile:2)
                             */
                            if (!TextUtils.isEmpty(name))
                                name = name.replaceAll("\\r?\\n", " ");

                            Attendee attendee = new Attendee(name, email);

                            if (action == R.id.btnCalendarAccept) {
                                attendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
                            } else if (action == R.id.btnCalendarDecline) {
                                attendee.setParticipationStatus(ParticipationStatus.DECLINED);
                            } else if (action == R.id.btnCalendarMaybe) {
                                attendee.setParticipationStatus(ParticipationStatus.TENTATIVE);
                            }

                            ev.addAttendee(attendee);

                            // https://icalendar.org/validator.html
                            ICalendar response = new ICalendar();
                            response.setMethod(Method.REPLY);
                            response.addEvent(ev);

                            File dir = new File(context.getCacheDir(), "calendar");
                            if (!dir.exists())
                                dir.mkdir();
                            File ics = new File(dir, message.id + ".ics");
                            response.write(ics);

                            return ics;
                        }

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Object result) {
                    if (result instanceof File) {
                        String status = null;
                        if (action == R.id.btnCalendarAccept) {
                            status = context.getString(R.string.title_icalendar_accept);
                        } else if (action == R.id.btnCalendarDecline) {
                            status = context.getString(R.string.title_icalendar_decline);
                        } else if (action == R.id.btnCalendarMaybe) {
                            status = context.getString(R.string.title_icalendar_maybe);
                        }

                        if (args.getBoolean("share"))
                            Helper.share(context, (File) result, "text/calendar", status + ".ics");
                        else {
                            Intent reply = new Intent(context, ActivityCompose.class)
                                    .putExtra("action", "participation")
                                    .putExtra("reference", args.getLong("id"))
                                    .putExtra("ics", (File) result)
                                    .putExtra("status", status);
                            context.startActivity(reply);
                        }
                    } else if (result instanceof Intent) {
                        try {
                            context.startActivity((Intent) result);
                        } catch (ActivityNotFoundException ex) {
                            Log.w(ex);
                            Helper.reportNoViewer(context, (Intent) result);
                        }
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
            else {
                if (message.identityEmail == null)
                    return false;

                if (message.from != null && message.from.length == 1 &&
                        message.to != null && message.to.length == 1 &&
                        message.identityEmail.equalsIgnoreCase(((InternetAddress) message.from[0]).getAddress()) &&
                        message.identityEmail.equalsIgnoreCase(((InternetAddress) message.to[0]).getAddress()))
                    return false;

                if (message.from != null)
                    for (Address from : message.from)
                        if (message.identityEmail.equalsIgnoreCase(((InternetAddress) from).getAddress()))
                            return true;

                return false;
            }
        }

        private TupleMessageEx getMessage() {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return null;

            return differ.getItem(pos);
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            TupleMessageEx message = getMessage();
            if (message != null) {
                int h = bottom - top;
                if (h > dp60)
                    properties.setHeight(message.id, bottom - top);
            }
        }

        @Override
        public void onClick(View view) {
            if (selectionTracker != null && selectionTracker.hasSelection())
                return;

            final TupleMessageEx message = getMessage();
            if (message == null)
                return;

            if (view.getId() == R.id.ibAvatar)
                onViewContact(message);
            else if (view.getId() == R.id.ibVerified)
                onShowVerified(message);
            else if (view.getId() == R.id.ibAuth)
                onShowAuth(message);
            else if (view.getId() == R.id.ibPriority)
                onShowPriority(message);
            else if (view.getId() == R.id.ibSnoozed)
                onShowSnoozed(message);
            else if (view.getId() == R.id.ibFlagged)
                onToggleFlag(message);
            else if (view.getId() == R.id.ibHelp)
                onHelp(message);
            else if (view.getId() == R.id.ibReceipt)
                onReceipt(message);
            else if (view.getId() == R.id.ibSearchContact)
                onSearchContact(message);
            else if (view.getId() == R.id.ibNotifyContact)
                onNotifyContact(message);
            else if (view.getId() == R.id.ibPinContact)
                onPinContact(message);
            else if (view.getId() == R.id.ibAddContact)
                onAddContact(message);
            else if (viewType == ViewType.THREAD) {
                int id = view.getId();
                if (id == R.id.ibExpanderAddress) {
                    onToggleAddresses(message);
                } else if (id == R.id.ibCopyHeaders) {
                    onCopyHeaders(message);
                } else if (id == R.id.ibCloseHeaders) {
                    onMenuShowHeaders(message);
                } else if (id == R.id.ibSaveAttachments) {
                    onSaveAttachments(message);
                } else if (id == R.id.ibDownloadAttachments) {
                    onDownloadAttachments(message);
                } else if (id == R.id.ibFull) {
                    onShow(message, true);
                } else if (id == R.id.ibImages) {
                    onShow(message, false);
                } else if (id == R.id.ibDecrypt) {
                    boolean lock =
                            (EntityMessage.PGP_SIGNENCRYPT.equals(message.ui_encrypt) &&
                                    !EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt)) ||
                                    (EntityMessage.SMIME_SIGNENCRYPT.equals(message.ui_encrypt) &&
                                            !EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt));
                    if (lock) {
                        properties.lock(message.id);
                        properties.setExpanded(message, false, false);
                        properties.setHeight(message.id, null);
                    } else
                        onActionDecrypt(message, false);
                } else if (id == R.id.ibVerify) {
                    onActionDecrypt(message, false);
                } else if (id == R.id.ibUndo) {
                    ActivityCompose.undoSend(message.id, context, owner, parentFragment.getParentFragmentManager());
                } else if (id == R.id.ibRule) {
                    onMenuCreateRule(message);
                } else if (id == R.id.ibUnsubscribe) {
                    onActionUnsubscribe(message);
                } else if (id == R.id.ibHeaders) {
                    onMenuShowHeaders(message);
                } else if (id == R.id.ibPrint) {
                    onMenuPrint(message);
                } else if (id == R.id.ibPin) {
                    onMenuPin(message);
                } else if (id == R.id.ibShare) {
                    onMenuShare(message, false);
                } else if (id == R.id.ibEvent) {
                    if (ActivityBilling.isPro(context))
                        onMenuShare(message, true);
                    else
                        context.startActivity(new Intent(context, ActivityBilling.class));
                } else if (id == R.id.ibSearchText) {
                    onSearchText(message);
                } else if (id == R.id.ibSearch) {
                    onSearchContact(message);
                } else if (id == R.id.ibTranslate) {
                    if (DeepL.canTranslate(context))
                        onActionTranslate(message);
                    else {
                        DeepL.FragmentDialogDeepL fragment = new DeepL.FragmentDialogDeepL();
                        fragment.show(parentFragment.getParentFragmentManager(), "deepl:configure");
                    }
                } else if (id == R.id.ibAnswer) {
                    onActionAnswer(message, ibAnswer);
                } else if (id == R.id.ibNotes) {
                    onMenuNotes(message);
                } else if (id == R.id.ibLabels) {
                    onActionLabels(message);
                } else if (id == R.id.ibKeywords) {
                    onMenuManageKeywords(message);
                } else if (id == R.id.ibCopy) {
                    onActionMove(message, true);
                } else if (id == R.id.ibMove || id == R.id.ibMoveBottom) {
                    onActionMove(message, false);
                } else if (id == R.id.ibArchive || id == R.id.ibArchiveBottom) {
                    onActionArchive(message);
                } else if (id == R.id.ibTrash || id == R.id.ibTrashBottom) {
                    onActionTrash(message, (Boolean) ibTrash.getTag());
                } else if (id == R.id.ibJunk) {
                    onActionJunk(message);
                } else if (id == R.id.ibInbox) {
                    onActionInbox(message);
                } else if (id == R.id.ibMore) {
                    onActionMore(message);
                } else if (id == R.id.ibTools) {
                    onActionTools(message);
                } else if (id == R.id.ibDownloading) {
                    Helper.viewFAQ(context, 15);
                } else if (id == R.id.ibSeen || id == R.id.ibSeenBottom) {
                    onMenuUnseen(message);
                } else if (id == R.id.ibHide) {
                    onMenuHide(message);
                } else if (id == R.id.btnCalendarAccept || id == R.id.btnCalendarDecline || id == R.id.btnCalendarMaybe || id == R.id.ibCalendar) {
                    onActionCalendar(message, view.getId(), false);
                } else {
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

                if (EntityFolder.DRAFTS.equals(message.folderType) && message.visible == 1 &&
                        !EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) &&
                        !EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt)) {
                    context.startActivity(
                            new Intent(context, ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", message.id));
                    properties.setValue("selected", message.id, true);
                } else {
                    final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    final Intent viewThread = new Intent(ActivityView.ACTION_VIEW_THREAD)
                            .putExtra("account", message.account)
                            .putExtra("folder", message.folder)
                            .putExtra("thread", message.thread)
                            .putExtra("id", message.id)
                            .putExtra("lpos", getAdapterPosition())
                            .putExtra("filter_archive", !EntityFolder.ARCHIVE.equals(message.folderType))
                            .putExtra("found", viewType == ViewType.SEARCH);

                    boolean doubletap = prefs.getBoolean("doubletap", true);

                    if (!doubletap ||
                            message.folderReadOnly ||
                            (message.uid == null && message.accountProtocol == EntityAccount.TYPE_IMAP) ||
                            EntityFolder.OUTBOX.equals(message.folderType)) {
                        lbm.sendBroadcast(viewThread);
                        properties.setValue("selected", message.id, true);
                        return;
                    }

                    firstClick = !firstClick;
                    if (firstClick) {
                        ApplicationEx.getMainHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (firstClick) {
                                    firstClick = false;
                                    lbm.sendBroadcast(viewThread);
                                    properties.setValue("selected", message.id, true);
                                }
                            }
                        }, ViewConfiguration.getDoubleTapTimeout());
                    } else {
                        message.unseen = (message.unseen == 0 ? message.count : 0);
                        message.ui_seen = (message.unseen == 0);
                        bindSeen(message);

                        Bundle args = new Bundle();
                        args.putLong("id", message.id);
                        args.putBoolean("seen", message.ui_seen);

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

                                    // When marking read: in all folders
                                    List<EntityMessage> messages = db.message().getMessagesByThread(
                                            message.account, message.thread, threading ? null : id, seen ? null : message.folder);
                                    for (EntityMessage threaded : messages)
                                        if (threaded.ui_seen != seen)
                                            EntityOperation.queue(context, threaded, EntityOperation.SEEN, seen);

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
        public boolean onLongClick(View view) {
            final TupleMessageEx message = getMessage();
            if (message == null || message.folderReadOnly)
                return false;

            int id = view.getId();
            if (id == R.id.ibFlagged) {
                onMenuColoredStar(message);
                return true;
            } else if (id == R.id.ibNotes) {
                onActionCopyNote(message);
                return true;
            } else if (id == R.id.ibTranslate) {
                DeepL.FragmentDialogDeepL fragment = new DeepL.FragmentDialogDeepL();
                fragment.show(parentFragment.getParentFragmentManager(), "deepl:configure");
                return true;
            } else if (id == R.id.ibFull) {
                onActionOpenFull(message);
                return true;
            } else if (id == R.id.ibTrash || id == R.id.ibTrashBottom) {
                if (EntityFolder.OUTBOX.equals(message.folderType))
                    return false;
                onActionTrash(message, true);
                return true;
            } else if (id == R.id.btnCalendarAccept || id == R.id.btnCalendarDecline || id == R.id.btnCalendarMaybe) {
                onActionCalendar(message, view.getId(), true);
                return true;
            }
            return false;
        }

        public boolean onKeyPressed(KeyEvent event) {
            TupleMessageEx message = getMessage();
            if (message == null)
                return false;

            if (event.isCtrlPressed() || event.isAltPressed())
                return false;

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_BUTTON_A:
                    boolean expanded = properties.getValue("expanded", message.id);
                    if (expanded)
                        return false;
                    onClick(view);
                    return true;
                case KeyEvent.KEYCODE_A:
                    onActionArchive(message);
                    return false;
                case KeyEvent.KEYCODE_D:
                    onActionTrash(message, false);
                    return false;
                case KeyEvent.KEYCODE_S:
                    if (selectionTracker == null)
                        return false;
                    if (selectionTracker.isSelected(message.id))
                        selectionTracker.deselect(message.id);
                    else
                        selectionTracker.select(message.id);
                    return true;
                case KeyEvent.KEYCODE_T:
                    if (tvBody != null && tvBody.getVisibility() == View.VISIBLE) {
                        tvBody.requestFocus();
                        return true;
                    }
                    if (wvBody != null && wvBody.getVisibility() == View.VISIBLE) {
                        wvBody.requestFocus();
                        return true;
                    }
                    return false;
                case KeyEvent.KEYCODE_8:
                    if (event.isShiftPressed()) {
                        onToggleFlag(message);
                        return true;
                    } else
                        return false;
                case KeyEvent.KEYCODE_NUMPAD_MULTIPLY:
                    onToggleFlag(message);
                    return true;
                default:
                    return false;
            }
        }

        private void onViewContact(TupleMessageEx message) {
            Uri lookupUri = (Uri) ibAvatar.getTag();
            if (lookupUri == null) {
                if (BuildConfig.DEBUG) {
                    CharSequence type = ibAvatar.getContentDescription();
                    ToastEx.makeText(context, type, Toast.LENGTH_LONG).show();
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, lookupUri);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.w(ex);
                    Helper.reportNoViewer(context, intent);
                }
            }
        }

        private void onShowVerified(TupleMessageEx message) {
            ToastEx.makeText(context, ibVerified.getContentDescription(), Toast.LENGTH_LONG).show();
        }

        private void onShowAuth(TupleMessageEx message) {
            StringBuilder sb = new StringBuilder();

            List<String> result = new ArrayList<>();
            if (Boolean.FALSE.equals(message.dkim))
                result.add("DKIM");
            if (Boolean.FALSE.equals(message.spf))
                result.add("SPF");
            if (Boolean.FALSE.equals(message.dmarc))
                result.add("DMARC");
            if (Boolean.FALSE.equals(message.mx))
                result.add("MX");

            if (result.size() > 0)
                sb.append(context.getString(R.string.title_authentication_failed, TextUtils.join(", ", result)));
            else {
                sb.append("DKIM: ")
                        .append(message.dkim == null ? "-" : (message.dkim ? "✓" : "✗"))
                        .append('\n');
                sb.append("SPF: ")
                        .append(message.spf == null ? "-" : (message.spf ? "✓" : "✗"))
                        .append('\n');
                sb.append("DMARC: ")
                        .append(message.dmarc == null ? "-" : (message.dmarc ? "✓" : "✗"));
            }

            if (Boolean.TRUE.equals(message.blocklist)) {
                if (sb.length() > 0)
                    sb.append('\n');
                sb.append(context.getString(R.string.title_on_blocklist));
            }

            if (Boolean.FALSE.equals(message.reply_domain)) {
                if (sb.length() > 0)
                    sb.append('\n');
                sb.append(message.checkReplyDomain(context));
            }

            if (message.from != null && message.from.length > 0) {
                String email = ((InternetAddress) message.from[0]).getAddress();
                String domain = UriHelper.getEmailDomain(email);
                if (!TextUtils.isEmpty(domain))
                    sb.insert(0, '\n').insert(0, domain);
            }

            ToastEx.makeText(context, sb.toString(), Toast.LENGTH_LONG).show();
        }

        private void onShowPriority(TupleMessageEx message) {
            if (EntityMessage.PRIORITIY_HIGH.equals(message.ui_priority))
                ToastEx.makeText(context, R.string.title_legend_priority, Toast.LENGTH_LONG).show();
            else
                ToastEx.makeText(context, R.string.title_legend_priority_low, Toast.LENGTH_LONG).show();
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
                                        DateUtils.SECOND_IN_MILLIS,
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
                                    message.account, message.thread, threading && thread ? null : id, flagged ? message.folder : null);
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

        private void onReceipt(TupleMessageEx message) {
            Intent reply = new Intent(context, ActivityCompose.class)
                    .putExtra("action", "dsn")
                    .putExtra("dsn", EntityMessage.DSN_RECEIPT)
                    .putExtra("reference", message.id);
            context.startActivity(reply);
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
                            new Intent(ActivityView.ACTION_SEARCH_ADDRESS)
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

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibNotifyContact);
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
                    int itemId = item.getItemId();
                    if (itemId == R.string.title_create_channel) {
                        onActionCreateChannel();
                        return true;
                    } else if (itemId == R.string.title_edit_channel) {
                        onActionEditChannel();
                        return true;
                    } else if (itemId == R.string.title_delete_channel) {
                        onActionDeleteChannel();
                        return true;
                    }
                    return false;
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
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        Log.w(ex);
                        Helper.reportNoViewer(context, intent);
                    }
                }

                private void onActionDeleteChannel() {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.deleteNotificationChannel(channelId);
                }
            });

            popupMenu.show();
        }

        private void onPinContact(TupleMessageEx message) {
            ShortcutInfoCompat.Builder builder =
                    Shortcuts.getShortcut(context, (InternetAddress) message.from[0]);
            ShortcutManagerCompat.requestPinShortcut(context, builder.build(), null);
        }

        private void onAddContact(TupleMessageEx message) {
            InternetAddress ia = (InternetAddress) message.from[0];
            String name = ia.getPersonal();
            String email = ia.getAddress();

            Uri lookupUri = null;
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

                    lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }

            if (lookupUri == null) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibAddContact);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_insert_contact, 1, R.string.title_insert_contact);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_contact, 2, R.string.title_edit_contact);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.string.title_insert_contact) {
                            onInsertContact(name, email);
                            return true;
                        } else if (itemId == R.string.title_edit_contact) {
                            onPickContact(name, email);
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();

            } else
                onEditContact(name, email, lookupUri);
        }

        private void onPickContact(String name, String email) {
            Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            properties.setValue("name", name);
            properties.setValue("email", email);
            try {
                parentFragment.startActivityForResult(
                        Helper.getChooser(context, pick), FragmentMessages.REQUEST_PICK_CONTACT);
            } catch (ActivityNotFoundException ex) {
                Log.w(ex);
                Helper.reportNoViewer(context, pick);
            } catch (Throwable ex) {
                Log.unexpectedError(parentFragment.getParentFragmentManager(), ex, false);
            }
        }

        private void onInsertContact(String name, String email) {
            if (TextUtils.isEmpty(name)) {
                int at = email.indexOf('@');
                if (at > 0)
                    name = email.substring(0, at);
            }

            // https://developer.android.com/training/contacts-provider/modify-data
            Intent insert = new Intent();
            insert.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
            if (!TextUtils.isEmpty(name))
                insert.putExtra(ContactsContract.Intents.Insert.NAME, name);
            insert.setAction(Intent.ACTION_INSERT);
            insert.setType(ContactsContract.Contacts.CONTENT_TYPE);
            context.startActivity(insert);
        }

        private void onEditContact(String name, String email, Uri lookupUri) {
            // https://developer.android.com/training/contacts-provider/modify-data
            Intent edit = new Intent();
            edit.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
            edit.setAction(Intent.ACTION_EDIT);
            edit.setDataAndTypeAndNormalize(lookupUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            context.startActivity(edit);
        }

        private void onToggleMessage(TupleMessageEx message) {
            if (EntityFolder.DRAFTS.equals(message.folderType) &&
                    !EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) &&
                    !EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt))
                context.startActivity(
                        new Intent(context, ActivityCompose.class)
                                .putExtra("action", "edit")
                                .putExtra("id", message.id));
            else {
                boolean expanded = !properties.getValue("expanded", message.id);
                properties.setExpanded(message, expanded, expanded);
            }
        }

        private void onToggleAddresses(TupleMessageEx message) {
            boolean addresses = !properties.getValue("addresses", message.id);
            properties.setValue("addresses", message.id, addresses);
            bindAddresses(message);
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
                    new Intent(FragmentBase.ACTION_STORE_ATTACHMENTS)
                            .putExtra("id", message.id));
        }

        private void onActionCopyNote(TupleMessageEx message) {
            if (TextUtils.isEmpty(message.notes))
                return;

            ClipboardManager clipboard =
                    (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null)
                return;

            ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), message.notes);
            clipboard.setPrimaryClip(clip);
            ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
        }

        private void onShow(final TupleMessageEx message, boolean full) {
            if (full && tvReformatted.getVisibility() == View.VISIBLE) {
                prefs.edit().putBoolean("reformatted_hint", false).apply();
                tvReformatted.setVisibility(View.GONE);
            }

            boolean current = properties.getValue(full ? "full" : "images", message.id);
            boolean asked = properties.getValue(full ? "full_asked" : "images_asked", message.id);
            if (current || asked) {
                if (current && message.from != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    for (Address sender : message.from) {
                        String from = ((InternetAddress) sender).getAddress();
                        if (TextUtils.isEmpty(from))
                            continue;
                        int at = from.indexOf('@');
                        String domain = (at < 0 ? from : from.substring(at));
                        editor.remove(from + (full ? ".show_full" : ".show_images"));
                        editor.remove(domain + (full ? ".show_full" : ".show_images"));
                    }
                    editor.apply();
                }

                properties.setValue(full ? "full" : "images", message.id, !current);
                onShowConfirmed(message, full, !current);

                return;
            }

            View dview = LayoutInflater.from(context).inflate(
                    full ? R.layout.dialog_show_full : R.layout.dialog_show_images, null);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            CheckBox cbNotAgainDomain = dview.findViewById(R.id.cbNotAgainDomain);

            if (message.from == null || message.from.length == 0) {
                cbNotAgain.setVisibility(View.GONE);
                cbNotAgainDomain.setVisibility(View.GONE);
            } else {
                List<String> froms = new ArrayList<>();
                List<String> domains = new ArrayList<>();
                for (Address address : message.from) {
                    String from = ((InternetAddress) address).getAddress();
                    froms.add(from);
                    int at = from.indexOf('@');
                    String domain = (at < 0 ? from : from.substring(at));
                    domains.add(domain);
                }
                cbNotAgain.setText(context.getString(R.string.title_no_ask_for_again,
                        TextUtils.join(", ", froms)));
                cbNotAgainDomain.setText(context.getString(R.string.title_no_ask_for_again,
                        TextUtils.join(", ", domains)));
            }

            cbNotAgainDomain.setEnabled(false);
            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cbNotAgainDomain.setEnabled(isChecked);
                }
            });

            if (full) {
                TextView tvDark = dview.findViewById(R.id.tvDark);
                CheckBox cbDark = dview.findViewById(R.id.cbDark);
                CheckBox cbAlwaysImages = dview.findViewById(R.id.cbAlwaysImages);

                boolean confirm_html = prefs.getBoolean("confirm_html", true);
                cbDark.setChecked(prefs.getBoolean("html_dark", confirm_html));
                cbAlwaysImages.setChecked(prefs.getBoolean("html_always_images", false));

                cbDark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        prefs.edit().putBoolean("html_dark", isChecked).apply();
                    }
                });

                cbAlwaysImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        prefs.edit().putBoolean("html_always_images", isChecked).apply();
                    }
                });

                boolean isDark = Helper.isDarkTheme(context);
                boolean canDark = WebViewEx.isFeatureSupported(WebViewFeature.FORCE_DARK);

                tvDark.setVisibility(isDark && !canDark ? View.VISIBLE : View.GONE);
                cbDark.setVisibility(isDark && canDark ? View.VISIBLE : View.GONE);

            } else {
                boolean disable_tracking = prefs.getBoolean("disable_tracking", true);

                ImageView ivInfo = dview.findViewById(R.id.ivInfo);
                Group grpTracking = dview.findViewById(R.id.grpTracking);

                grpTracking.setVisibility(disable_tracking ? View.VISIBLE : View.GONE);

                ivInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.viewFAQ(v.getContext(), 82);
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

                            SharedPreferences.Editor editor = prefs.edit();
                            if (message.from != null)
                                for (Address sender : message.from) {
                                    String from = ((InternetAddress) sender).getAddress();
                                    if (TextUtils.isEmpty(from))
                                        continue;
                                    int at = from.indexOf('@');
                                    String domain = (at < 0 ? from : from.substring(at));
                                    editor.putBoolean(from + (full ? ".show_full" : ".show_images"),
                                            cbNotAgain.isChecked());
                                    editor.putBoolean(domain + (full ? ".show_full" : ".show_images"),
                                            cbNotAgain.isChecked() && cbNotAgainDomain.isChecked());
                                }
                            editor.apply();

                            onShowConfirmed(message, full, true);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.title_setup, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent privacy = new Intent(context, ActivitySetup.class)
                                    .setAction("privacy")
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra("tab", "privacy");
                            context.startActivity(privacy);
                        }
                    })
                    .create();

            owner.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                public void onCreate() {
                    try {
                        dialog.show();
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroyed() {
                    try {
                        dialog.dismiss();
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });
        }

        private void onShowConfirmed(final TupleMessageEx message, boolean full, boolean value) {
            if (full)
                message.show_full = value;
            else
                message.show_images = value;

            if (full) {
                properties.setSize(message.id, null);
                properties.setHeight(message.id, null);
                properties.setPosition(message.id, null);

                setupTools(message, false, false);
            }

            bindBody(message, false);

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("full", full);
            args.putBoolean("value", value);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean full = args.getBoolean("full");
                    boolean value = args.getBoolean("value");

                    DB db = DB.getInstance(context);
                    if (full)
                        db.message().setMessageShowFull(id, value);
                    else
                        db.message().setMessageShowImages(id, value);

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:full");
        }

        private void onActionOpenFull(final TupleMessageEx message) {
            boolean open_full_confirmed = prefs.getBoolean("open_full_confirmed", false);
            if (open_full_confirmed)
                onActionOpenFullConfirmed(message);
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                View dview = inflater.inflate(R.layout.dialog_ask_again, null, false);
                final TextView tvMessage = dview.findViewById(R.id.tvMessage);
                final TextView tvRemark = dview.findViewById(R.id.tvRemark);
                final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

                tvMessage.setText(R.string.title_ask_show_html);
                tvRemark.setText(R.string.title_ask_show_image_hint);

                new AlertDialog.Builder(context)
                        .setView(dview)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (cbNotAgain.isChecked())
                                    prefs.edit().putBoolean("open_full_confirmed", true).apply();
                                onActionOpenFullConfirmed(message);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        }

        private void onActionOpenFullConfirmed(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<String>() {
                @Override
                protected String onExecute(Context context, Bundle args) throws Throwable {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean overview_mode = prefs.getBoolean("overview_mode", false);
                    boolean disable_tracking = prefs.getBoolean("disable_tracking", true);

                    long id = args.getLong("id");
                    File file = EntityMessage.getFile(context, id);
                    Document document = JsoupEx.parse(file);

                    HtmlHelper.cleanup(document);
                    HtmlHelper.setViewport(document, overview_mode);
                    HtmlHelper.embedInlineImages(context, message.id, document, true);
                    if (disable_tracking)
                        HtmlHelper.removeTrackingPixels(context, document);

                    return document.html();
                }

                @Override
                protected void onExecuted(Bundle args, String html) {
                    Bundle fargs = new Bundle();
                    fargs.putString("html", html);

                    FragmentDialogOpenFull dialog = new FragmentDialogOpenFull();
                    dialog.setArguments(fargs);
                    dialog.show(parentFragment.getParentFragmentManager(), "open");
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "open");
        }

        private void onActionUnsubscribe(TupleMessageEx message) {
            Uri uri = Uri.parse(message.unsubscribe);
            onOpenLink(uri, context.getString(R.string.title_legend_show_unsubscribe), true);
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

        private void onActionAnswer(TupleMessageEx message, View anchor) {
            properties.reply(message, getSelectedText(), anchor);
        }

        private void onActionLabels(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putLong("account", message.account);
            args.putString("self", message.folderName);

            new SimpleTask<String[]>() {
                @Override
                protected String[] onExecute(Context context, Bundle args) {
                    long account = args.getLong("account");

                    DB db = DB.getInstance(context);
                    List<EntityFolder> folders = db.folder().getFolders(account, true, true);

                    List<String> result = new ArrayList<>();
                    if (folders != null)
                        for (EntityFolder folder : folders)
                            if (EntityFolder.USER.equals(folder.type))
                                result.add(folder.name);

                    Collator collator = Collator.getInstance(Locale.getDefault());
                    collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                    Collections.sort(result, collator);

                    return result.toArray(new String[0]);
                }

                @Override
                protected void onExecuted(Bundle args, String[] folders) {
                    args.putStringArray("labels", message.labels);
                    args.putStringArray("folders", folders);

                    FragmentDialogLabelsManage fragment = new FragmentDialogLabelsManage();
                    fragment.setArguments(args);
                    fragment.show(parentFragment.getParentFragmentManager(), "labels:manage");
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "labels:fetch");
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

        private void onActionArchive(TupleMessageEx message) {
            properties.move(message.id, EntityFolder.ARCHIVE);
        }

        private void onActionTrash(TupleMessageEx message, boolean delete) {
            if (delete)
                onActionDelete(message);
            else
                properties.move(message.id, EntityFolder.TRASH);
        }

        private void onActionDelete(TupleMessageEx message) {
            Bundle aargs = new Bundle();
            aargs.putString("question", context.getString(R.string.title_ask_delete));
            aargs.putString("remark", message.getRemark());
            aargs.putLong("id", message.id);
            aargs.putInt("faq", 160);
            aargs.putString("notagain", "delete_asked");
            aargs.putString("accept", context.getString(R.string.title_ask_delete_accept));
            aargs.putBoolean("warning", true);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean delete_asked = prefs.getBoolean("delete_asked", false);
            if (delete_asked ||
                    (message.accountProtocol == EntityAccount.TYPE_POP &&
                            message.accountLeaveDeleted)) {
                Intent data = new Intent();
                data.putExtra("args", aargs);
                parentFragment.onActivityResult(FragmentMessages.REQUEST_MESSAGE_DELETE, RESULT_OK, data);
                return;
            }

            FragmentDialogAsk ask = new FragmentDialogAsk();
            ask.setArguments(aargs);
            ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_DELETE);
            ask.show(parentFragment.getParentFragmentManager(), "message:delete");
        }

        private void onActionJunk(TupleMessageEx message) {
            boolean canBlock = false;
            if (message.from != null && message.from.length > 0) {
                String email = ((InternetAddress) message.from[0]).getAddress();
                canBlock = !TextUtils.isEmpty(email) && Helper.EMAIL_ADDRESS.matcher(email).matches();
            }

            Bundle aargs = new Bundle();
            aargs.putLong("id", message.id);
            aargs.putLong("account", message.account);
            aargs.putInt("protocol", message.accountProtocol);
            aargs.putLong("folder", message.folder);
            aargs.putString("type", message.folderType);
            aargs.putString("from", MessageHelper.formatAddresses(message.from));
            aargs.putBoolean("inJunk", EntityFolder.JUNK.equals(message.folderType));
            aargs.putBoolean("canBlock", canBlock);

            FragmentDialogJunk ask = new FragmentDialogJunk();
            ask.setArguments(aargs);
            ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_JUNK);
            ask.show(parentFragment.getParentFragmentManager(), "message:junk");
        }

        private void onActionInbox(TupleMessageEx message) {
            properties.move(message.id, EntityFolder.INBOX);
        }

        private void onActionTools(TupleMessageEx message) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean message_tools = !prefs.getBoolean("message_tools", true);
            prefs.edit().putBoolean("message_tools", message_tools).apply();
            setupTools(message, false, false);
        }

        private void onActionMore(TupleMessageEx message) {
            boolean show_headers = properties.getValue("headers", message.id);
            boolean full = properties.getValue("full", message.id);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibMore);
            popupMenu.inflate(R.menu.popup_message_more);

            popupMenu.getMenu().findItem(R.id.menu_unseen)
                    .setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen)
                    .setIcon(message.ui_seen ? R.drawable.twotone_drafts_24 : R.drawable.twotone_mail_24)
                    .setEnabled((message.uid != null && !message.folderReadOnly) ||
                            message.accountProtocol != EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_hide)
                    .setTitle(message.ui_snoozed == null ? R.string.title_hide : R.string.title_unhide)
                    .setIcon(message.ui_snoozed == null ? R.drawable.twotone_visibility_off_24 : R.drawable.twotone_visibility_24);

            popupMenu.getMenu().findItem(R.id.menu_flag_color)
                    .setEnabled((message.uid != null && !message.folderReadOnly) ||
                            message.accountProtocol != EntityAccount.TYPE_IMAP);

            int i = (message.importance == null ? EntityMessage.PRIORITIY_NORMAL : message.importance);
            popupMenu.getMenu().findItem(R.id.menu_set_importance_low).setEnabled(!EntityMessage.PRIORITIY_LOW.equals(i));
            popupMenu.getMenu().findItem(R.id.menu_set_importance_normal).setEnabled(!EntityMessage.PRIORITIY_NORMAL.equals(i));
            popupMenu.getMenu().findItem(R.id.menu_set_importance_high).setEnabled(!EntityMessage.PRIORITIY_HIGH.equals(i));

            popupMenu.getMenu().findItem(R.id.menu_move_to)
                    .setEnabled(message.uid != null && !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_copy_to)
                    .setEnabled(message.uid != null && !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_delete)
                    .setEnabled(message.uid == null || !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_resync)
                    .setEnabled(message.uid != null)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_search_in_text).setEnabled(message.content && !full);

            popupMenu.getMenu().findItem(R.id.menu_create_rule).setVisible(!message.folderReadOnly);

            popupMenu.getMenu().findItem(R.id.menu_manage_keywords)
                    .setEnabled(message.uid != null && !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_share).setEnabled(message.content);
            popupMenu.getMenu().findItem(R.id.menu_pin).setVisible(pin);
            popupMenu.getMenu().findItem(R.id.menu_event).setEnabled(message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setEnabled(hasWebView && message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setVisible(Helper.canPrint(context));

            popupMenu.getMenu().findItem(R.id.menu_show_headers).setChecked(show_headers);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setEnabled(message.uid != null ||
                    (message.accountProtocol == EntityAccount.TYPE_POP && message.headers != null));

            popupMenu.getMenu().findItem(R.id.menu_share_as_html).setVisible(message.content && BuildConfig.DEBUG);

            popupMenu.getMenu().findItem(R.id.menu_raw_save).setEnabled(message.uid != null);
            popupMenu.getMenu().findItem(R.id.menu_raw_send).setEnabled(message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_raw_save).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);
            popupMenu.getMenu().findItem(R.id.menu_raw_send).setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.insertIcons(context);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    int itemId = target.getItemId();
                    if (itemId == R.id.menu_button) {
                        FragmentDialogButtons buttons = new FragmentDialogButtons();
                        buttons.setTargetFragment(parentFragment, FragmentMessages.REQUEST_BUTTONS);
                        buttons.show(parentFragment.getParentFragmentManager(), "dialog:buttons");
                        return true;
                    } else if (itemId == R.id.menu_unseen) {
                        onMenuUnseen(message);
                        return true;
                    } else if (itemId == R.id.menu_snooze) {
                        onMenuSnooze(message);
                        return true;
                    } else if (itemId == R.id.menu_hide) {
                        onMenuHide(message);
                        return true;
                    } else if (itemId == R.id.menu_flag_color) {
                        onMenuColoredStar(message);
                        return true;
                    } else if (itemId == R.id.menu_set_importance_low) {
                        onMenuSetImportance(message, EntityMessage.PRIORITIY_LOW);
                        return true;
                    } else if (itemId == R.id.menu_set_importance_normal) {
                        onMenuSetImportance(message, EntityMessage.PRIORITIY_NORMAL);
                        return true;
                    } else if (itemId == R.id.menu_set_importance_high) {
                        onMenuSetImportance(message, EntityMessage.PRIORITIY_HIGH);
                        return true;
                    } else if (itemId == R.id.menu_move_to) {
                        onActionMove(message, false);
                        return true;
                    } else if (itemId == R.id.menu_copy_to) {
                        onActionMove(message, true);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        onActionDelete(message);
                        return true;
                    } else if (itemId == R.id.menu_resync) {
                        onMenuResync(message);
                        return true;
                    } else if (itemId == R.id.menu_edit_notes) {
                        onMenuNotes(message);
                        return true;
                    } else if (itemId == R.id.menu_search_in_text) {
                        onSearchText(message);
                        return true;
                    } else if (itemId == R.id.menu_create_rule) {
                        onMenuCreateRule(message);
                        return true;
                    } else if (itemId == R.id.menu_manage_keywords) {
                        onMenuManageKeywords(message);
                        return true;
                    } else if (itemId == R.id.menu_event) {
                        if (ActivityBilling.isPro(context))
                            onMenuShare(message, true);
                        else
                            context.startActivity(new Intent(context, ActivityBilling.class));
                        return true;
                    } else if (itemId == R.id.menu_share) {
                        onMenuShare(message, false);
                        return true;
                    } else if (itemId == R.id.menu_pin) {
                        onMenuPin(message);
                        return true;
                    } else if (itemId == R.id.menu_print) {
                        onMenuPrint(message);
                        return true;
                    } else if (itemId == R.id.menu_show_headers) {
                        onMenuShowHeaders(message);
                        return true;
                    } else if (itemId == R.id.menu_share_as_html) {
                        onMenuShareHtml(message);
                        return true;
                    } else if (itemId == R.id.menu_raw_save) {
                        onMenuRawSave(message);
                        return true;
                    } else if (itemId == R.id.menu_raw_send) {
                        onMenuRawSend(message);
                        return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }

        private boolean onOpenLink(Uri uri, String title, boolean always_confirm) {
            Log.i("Opening uri=" + uri + " title=" + title);
            uri = Uri.parse(uri.toString().replaceAll("\\s+", ""));

            try {
                String url = uri.getQueryParameter("url");
                if (!TextUtils.isEmpty(url)) {
                    Uri alt = Uri.parse(url);
                    if (isActivate(alt))
                        uri = alt;
                }
            } catch (Throwable ignored) {
            }

            if (isActivate(uri)) {
                try {
                    if (ActivityBilling.activatePro(context, uri))
                        ToastEx.makeText(context, R.string.title_pro_valid, Toast.LENGTH_LONG).show();
                    else
                        ToastEx.makeText(context, R.string.title_pro_invalid, Toast.LENGTH_LONG).show();
                } catch (NoSuchAlgorithmException ex) {
                    Log.e(ex);
                    ToastEx.makeText(context, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
                }
            } else {
                if ("full".equals(uri.getScheme())) {
                    TupleMessageEx message = getMessage();
                    if (message != null)
                        onShow(message, true);
                    return (message != null);
                }

                if ("cid".equals(uri.getScheme()) || "data".equals(uri.getScheme()))
                    return false;

                boolean confirm_links = prefs.getBoolean("confirm_links", true);
                boolean confirm_link =
                        !"https".equals(uri.getScheme()) || TextUtils.isEmpty(uri.getHost()) ||
                                prefs.getBoolean(uri.getHost() + ".confirm_link", true);
                if (always_confirm || (confirm_links && confirm_link)) {
                    Bundle args = new Bundle();
                    args.putParcelable("uri", uri);
                    args.putString("title", title);

                    FragmentDialogOpenLink fragment = new FragmentDialogOpenLink();
                    fragment.setArguments(args);
                    fragment.show(parentFragment.getParentFragmentManager(), "open:link");
                } else {
                    boolean browse_links = prefs.getBoolean("browse_links", false);
                    Helper.view(context, uri, browse_links, browse_links);
                }
            }

            return true;
        }

        private boolean isActivate(Uri uri) {
            return ("email.faircode.eu".equals(uri.getHost()) &&
                    "/activate/".equals(uri.getPath()));
        }

        private void onOpenImage(long id, @NonNull String source) {
            Log.i("Viewing image source=" + source);

            ImageHelper.AnnotatedSource annotated = new ImageHelper.AnnotatedSource(source);
            Uri uri = Uri.parse(annotated.getSource());
            String scheme = uri.getScheme();

            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putString("source", annotated.getSource());
            args.putInt("zoom", zoom);

            if ("cid".equals(scheme))
                new SimpleTask<EntityAttachment>() {
                    @Override
                    protected EntityAttachment onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        String source = args.getString("source");

                        DB db = DB.getInstance(context);
                        String cid = "<" + source.substring(4) + ">";
                        return db.attachment().getAttachment(id, cid);
                    }

                    @Override
                    protected void onExecuted(Bundle args, EntityAttachment attachment) {
                        if (attachment != null)
                            Helper.share(context, attachment.getFile(context), attachment.getMimeType(), attachment.name);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.execute(context, owner, args, "view:cid");

            else if ("http".equals(scheme) || "https".equals(scheme))
                onOpenLink(uri, null, false);

            else if ("data".equals(scheme))
                new SimpleTask<File>() {
                    @Override
                    protected File onExecute(Context context, Bundle args) throws IOException {
                        long id = args.getLong("id");
                        String source = args.getString("source");

                        String type = ImageHelper.getDataUriType(source);
                        args.putString("type", type == null ? "application/octet-stream" : type);

                        String extention = Helper.guessExtension(type);
                        extention = "." + (extention == null ? "" : extention);

                        ByteArrayInputStream bis = ImageHelper.getDataUriStream(source);
                        File file = ImageHelper.getCacheFile(context, id, source, extention);
                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                            Helper.copy(bis, os);
                        }

                        return file;
                    }

                    @Override
                    protected void onExecuted(Bundle args, File file) {
                        if (file == null)
                            return;
                        String type = args.getString("type");
                        Helper.share(context, file, type, file.getName());
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex, false);
                    }
                }.execute(context, owner, args, "view:cid");

            else
                Helper.reportNoViewer(context, uri);
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
                    long id = args.getLong("id");
                    TupleMessageEx amessage = getMessage();
                    if (amessage == null || !amessage.id.equals(id))
                        return;

                    message.ui_seen = args.getBoolean("seen");
                    message.unseen = (message.ui_seen ? 0 : message.count);
                    if (!message.ui_seen &&
                            (autoclose_unseen || getItemCount() == 1))
                        properties.finish();
                    else
                        properties.setExpanded(message, false, true);
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
            args.putLong("folder", message.folder);
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
            args.putBoolean("reset", true);

            FragmentDialogColor fragment = new FragmentDialogColor();
            fragment.setArguments(args);
            fragment.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_COLOR);
            fragment.show(parentFragment.getParentFragmentManager(), "message:color");
        }

        private void onMenuSetImportance(TupleMessageEx message, int importance) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putInt("importance", importance);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");
                    Integer importance = args.getInt("importance");
                    if (EntityMessage.PRIORITIY_NORMAL.equals(importance))
                        importance = null;

                    DB db = DB.getInstance(context);
                    db.message().setMessageImportance(id, importance);

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "importance:set");
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
                protected void onExecuted(Bundle args, Void data) {
                    ToastEx.makeText(context, R.string.title_fetching_again, Toast.LENGTH_LONG).show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:resync");
        }

        private void onMenuNotes(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putString("notes", message.notes);
            args.putInt("color", message.notes_color == null ? Color.TRANSPARENT : message.notes_color);

            FragmentDialogNotes fragment = new FragmentDialogNotes();
            fragment.setArguments(args);
            fragment.show(parentFragment.getParentFragmentManager(), "edit:notes");
        }

        private void onActionTranslate(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            FragmentDialogTranslate fragment = new FragmentDialogTranslate();
            fragment.setArguments(args);
            fragment.show(parentFragment.getParentFragmentManager(), "message:translate");
        }

        private void onSearchText(TupleMessageEx message) {
            properties.startSearch(tvBody);
        }

        private void onMenuCreateRule(TupleMessageEx message) {
            Intent rule = new Intent(ActivityView.ACTION_EDIT_RULE);
            rule.putExtra("account", message.account);
            rule.putExtra("folder", message.folder);
            rule.putExtra("protocol", message.accountProtocol);
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

            FragmentDialogKeywordManage fragment = new FragmentDialogKeywordManage();
            fragment.setArguments(args);
            fragment.show(parentFragment.getParentFragmentManager(), "keyword:manage");
        }

        private void onMenuShare(TupleMessageEx message, final boolean event) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Map<String, Object>>() {
                @Override
                protected Map<String, Object> onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    Map<String, Object> result = new HashMap<>();

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null || !message.content)
                        return null;

                    File file = message.getFile(context);
                    if (!file.exists())
                        return null;

                    if (message.identity != null) {
                        EntityIdentity identity = db.identity().getIdentity(message.identity);
                        if (identity != null)
                            result.put("me", identity.email);
                    }

                    if (message.from != null && message.from.length > 0)
                        result.put("from", ((InternetAddress) message.from[0]).getAddress());

                    if (!TextUtils.isEmpty(message.subject))
                        result.put("subject", message.subject);

                    String link = "message://" + BuildConfig.APPLICATION_ID + "/" + message.id;

                    Document document = JsoupEx.parse(file);
                    HtmlHelper.quoteLimit(document, MAX_QUOTE_LEVEL);

                    Element a = document.createElement("a");
                    a.text(context.getString(R.string.app_name));
                    a.attr("href", link);

                    document.body().appendElement("p").appendChild(a);

                    String html = document.body().html();
                    String text = HtmlHelper.getText(context, html);

                    result.put("html", html);
                    result.put("text", text);
                    result.put("attachments", db.attachment().getAttachments(message.id));

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, Map<String, Object> data) {
                    if (data == null)
                        return;

                    Intent intent = new Intent();
                    if (event) {
                        intent.setAction(Intent.ACTION_INSERT);
                        intent.setData(CalendarContract.Events.CONTENT_URI);
                        if (data.containsKey("me"))
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{(String) data.get("me")});
                        if (data.containsKey("subject"))
                            intent.putExtra(CalendarContract.Events.TITLE, (String) data.get("subject"));
                        if (data.containsKey("text"))
                            intent.putExtra(CalendarContract.Events.DESCRIPTION, (String) data.get("text"));
                    } else {
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        if (data.containsKey("from"))
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{(String) data.get("from")});
                        if (data.containsKey("subject"))
                            intent.putExtra(Intent.EXTRA_SUBJECT, (String) data.get("subject"));
                        if (data.containsKey("text"))
                            intent.putExtra(Intent.EXTRA_TEXT, (String) data.get("text"));
                        if (data.containsKey("html"))
                            intent.putExtra(Intent.EXTRA_HTML_TEXT, (String) data.get("html"));

                        ArrayList<Uri> uris = new ArrayList<>();

                        List<EntityAttachment> attachments = (List<EntityAttachment>) data.get("attachments");
                        if (attachments != null)
                            for (EntityAttachment attachment : attachments) {
                                File file = attachment.getFile(context);
                                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
                                uris.add(uri);
                            }

                        if (uris.size() > 0) {
                            if (uris.size() == 1)
                                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
                            else {
                                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            }
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }

                    PackageManager pm = context.getPackageManager();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                            intent.resolveActivity(pm) == null) // system whitelisted
                        Helper.reportNoViewer(context, intent);
                    else
                        context.startActivity(intent);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:share");
        }

        private void onMenuPin(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putLong("account", message.account);
            args.putString("folderType", message.folderType);
            args.putString("selector", message.bimi_selector);
            args.putSerializable("addresses", message.from);

            new SimpleTask<ContactInfo[]>() {
                @Override
                protected ContactInfo[] onExecute(Context context, Bundle args) {
                    long account = args.getLong("account");
                    String folderType = args.getString("folderType");
                    String selector = args.getString("selector");
                    Address[] addresses = (Address[]) args.getSerializable("addresses");
                    return ContactInfo.get(context, account, folderType, selector, addresses);
                }

                @Override
                protected void onExecuted(Bundle args, ContactInfo[] contactInfo) {
                    ShortcutInfoCompat.Builder builder =
                            Shortcuts.getShortcut(context, message, contactInfo);
                    ShortcutManagerCompat.requestPinShortcut(context, builder.build(), null);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:pin");
        }

        private void onMenuPrint(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("headers", properties.getValue("headers", message.id));

            if (prefs.getBoolean("print_html_confirmed", false)) {
                Intent data = new Intent();
                data.putExtra("args", args);
                parentFragment.onActivityResult(FragmentMessages.REQUEST_PRINT, RESULT_OK, data);
                return;
            }

            FragmentDialogPrint ask = new FragmentDialogPrint();
            ask.setArguments(args);
            ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_PRINT);
            ask.show(parentFragment.getParentFragmentManager(), "message:print");
        }

        private void onCopyHeaders(TupleMessageEx message) {
            ClipboardManager clipboard =
                    (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null)
                return;

            ClipData clip = ClipData.newPlainText(context.getString(R.string.title_show_headers), message.headers);
            clipboard.setPrimaryClip(clip);

            ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
        }

        private void onMenuShowHeaders(TupleMessageEx message) {
            boolean show_headers = !properties.getValue("headers", message.id);
            properties.setValue("headers", message.id, show_headers);

            bindHeaders(message, true);

            if (show_headers && message.headers == null) {
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
            }
        }

        private void onMenuShareHtml(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<String>() {
                @Override
                protected String onExecute(Context context, Bundle args) throws IOException {
                    Long id = args.getLong("id");

                    File file = EntityMessage.getFile(context, id);
                    Document d = JsoupEx.parse(file);

                    return d.html();
                }

                @Override
                protected void onExecuted(Bundle args, String html) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, html);
                    context.startActivity(intent);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:headers");
        }

        private void onMenuRawSave(TupleMessageEx message) {
            if (message.raw == null || !message.raw) {
                properties.setValue("raw_save", message.id, true);
                rawDownload(message);
            } else {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(FragmentMessages.ACTION_STORE_RAW)
                                .putExtra("id", message.id)
                                .putExtra("subject", message.subject));
            }
        }

        private void onMenuRawSend(TupleMessageEx message) {
            if (message.raw == null || !message.raw) {
                properties.setValue("raw_send", message.id, true);
                rawDownload(message);
            } else
                try {
                    File file = message.getRawFile(context);
                    Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);

                    Intent send = new Intent(Intent.ACTION_SEND);
                    //send.setPackage(BuildConfig.APPLICATION_ID);
                    send.putExtra(Intent.EXTRA_STREAM, uri);
                    send.setType("message/rfc822");
                    send.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    context.startActivity(send);
                } catch (Throwable ex) {
                    // java.lang.IllegalArgumentException: Failed to resolve canonical path for ...
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
        }

        private void rawDownload(TupleMessageEx message) {
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
                protected void onExecuted(Bundle args, Void data) {
                    ToastEx.makeText(context, R.string.title_download_message, Toast.LENGTH_LONG).show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:raw");
        }

        private SpannableStringBuilder getKeywords(TupleMessageEx message) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();

            if (message.keyword_titles == null || message.keyword_colors == null) {
                ssb.append("Keywords missing!");
                return ssb;
            }

            for (int i = 0; i < message.keywords.length; i++) {
                if (MessageHelper.showKeyword(message.keywords[i])) {
                    if (ssb.length() > 0)
                        ssb.append(' ');

                    String keyword = message.keyword_titles[i];
                    ssb.append(keyword);

                    if (message.keyword_colors[i] != null) {
                        int len = ssb.length();
                        ssb.setSpan(
                                new ForegroundColorSpan(message.keyword_colors[i]),
                                len - keyword.length(), len,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            return ssb;
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
            return new ItemDetailsMessage(this);
        }

        Long getKey() {
            return getKeyAtPosition(getAdapterPosition());
        }

        String getSelectedText() {
            if (tvBody == null)
                return null;

            int start = tvBody.getSelectionStart();
            int end = tvBody.getSelectionEnd();
            if (start == end)
                return null;

            if (start < 0)
                start = 0;
            if (end < 0)
                end = 0;

            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            }

            return tvBody.getText().subSequence(start, end).toString();
        }

        private View.AccessibilityDelegate accessibilityDelegateHeader = new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onInitializeAccessibilityEvent(host, event);

                TupleMessageEx message = getMessage();
                if (message == null)
                    return;

                event.setContentDescription(populateContentDescription(message));
            }

            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                try {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                } catch (Throwable ex) {
                    /*
                        java.lang.IllegalArgumentException: Comparison method violates its general contract!
                                at java.util.TimSort.mergeHi(TimSort.java:864)
                                at java.util.TimSort.mergeAt(TimSort.java:481)
                                at java.util.TimSort.mergeForceCollapse(TimSort.java:422)
                                at java.util.TimSort.sort(TimSort.java:219)
                                at java.util.TimSort.sort(TimSort.java:169)
                                at java.util.Arrays.sort(Arrays.java:2010)
                                at java.util.Collections.sort(Collections.java:1883)
                                at android.view.ViewGroup$ChildListForAccessibility.init(ViewGroup.java:7181)
                                at android.view.ViewGroup$ChildListForAccessibility.obtain(ViewGroup.java:7138)
                                at android.view.ViewGroup.addChildrenForAccessibility(ViewGroup.java:1792)
                                at android.view.ViewGroup.addChildrenForAccessibility(ViewGroup.java:1801)
                                at android.view.ViewGroup.addChildrenForAccessibility(ViewGroup.java:1801)
                                at android.view.ViewGroup.onInitializeAccessibilityNodeInfoInternal(ViewGroup.java:2761)
                                at android.view.View$AccessibilityDelegate.onInitializeAccessibilityNodeInfo(View.java:21332)
                     */
                    Log.e(ex);
                }

                TupleMessageEx message = getMessage();
                if (message == null)
                    return;

                boolean expanded = properties.getValue("expanded", message.id);

                vwColor.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                if (ibExpander.getVisibility() == View.VISIBLE)
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibExpander,
                            context.getString(expanded ? R.string.title_accessibility_collapse : R.string.title_accessibility_expand)));
                ibExpander.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                if (ibAvatar.getVisibility() == View.VISIBLE && ibAvatar.isEnabled())
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibAvatar,
                            context.getString(R.string.title_accessibility_view_contact)));
                ibAvatar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                if (ibFlagged.getVisibility() == View.VISIBLE && ibFlagged.isEnabled()) {
                    int flagged = (message.count - message.unflagged);
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibFlagged,
                            context.getString(flagged > 0 ? R.string.title_unflag : R.string.title_flag)));
                }
                ibFlagged.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                if (ibAuth.getVisibility() == View.VISIBLE)
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibAuth,
                            context.getString(R.string.title_accessibility_show_authentication_result)));
                ibAuth.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                if (ibSnoozed.getVisibility() == View.VISIBLE)
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibSnoozed,
                            context.getString(R.string.title_accessibility_show_snooze_time)));
                ibSnoozed.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                if (ibHelp.getVisibility() == View.VISIBLE)
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibHelp,
                            context.getString(R.string.title_accessibility_view_help)));
                ibHelp.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                info.setContentDescription(populateContentDescription(message));
            }

            @Override
            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                TupleMessageEx message = getMessage();
                if (message == null)
                    return false;

                if (action == R.id.ibExpander) {
                    onToggleMessage(message);
                    return true;
                } else if (action == R.id.ibAvatar) {
                    onViewContact(message);
                    return true;
                } else if (action == R.id.ibFlagged) {
                    onToggleFlag(message);
                    return true;
                } else if (action == R.id.ibAuth) {
                    onShowAuth(message);
                    return true;
                } else if (action == R.id.ibSnoozed) {
                    onShowSnoozed(message);
                    return true;
                } else if (action == R.id.ibHelp) {
                    onHelp(message);
                    return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }

            private String populateContentDescription(TupleMessageEx message) {
                boolean expanded = properties.getValue("expanded", message.id);

                List<String> result = new ArrayList<>();

                if (selectionTracker != null && selectionTracker.isSelected(message.id))
                    result.add(context.getString(R.string.title_accessibility_selected));

                result.add(context.getString(
                        message.unseen > 0 ? R.string.title_accessibility_unseen : R.string.title_accessibility_seen));

                if (tvCount.getVisibility() == View.VISIBLE)
                    result.add(context.getResources().getQuantityString(
                            R.plurals.title_accessibility_messages, message.visible, message.visible));

                if (ibExpander.getVisibility() == View.VISIBLE)
                    result.add(context.getString(
                            expanded ? R.string.title_accessibility_expanded : R.string.title_accessibility_collapsed));

                if (message.drafts > 0)
                    result.add(context.getString(R.string.title_legend_draft));

                if (message.ui_answered)
                    result.add(context.getString(R.string.title_accessibility_answered));

                if (ibFlagged.getVisibility() == View.VISIBLE && ibFlagged.isEnabled()) {
                    int flagged = (message.count - message.unflagged);
                    if (flagged > 0)
                        result.add(context.getString(R.string.title_accessibility_flagged));
                }

                if (EntityMessage.PRIORITIY_HIGH.equals(message.ui_priority))
                    result.add(context.getString(R.string.title_legend_priority));
                else if (EntityMessage.PRIORITIY_LOW.equals(message.ui_priority))
                    result.add(context.getString(R.string.title_legend_priority_low));

                if (message.attachments > 0)
                    result.add(context.getString(R.string.title_accessibility_attachment));

                boolean outgoing = isOutgoing(message);
                boolean junk = EntityFolder.JUNK.equals(message.folderType);
                Address[] addresses = (EntityFolder.isOutgoing(message.folderType) &&
                        (viewType != ViewType.THREAD || !threading) ? message.to : message.senders);
                MessageHelper.AddressFormat format = email_format;
                if (junk)
                    format = MessageHelper.AddressFormat.NAME_EMAIL;
                String from = MessageHelper.formatAddresses(addresses, format, false);

                // For a11y purpose subject is reported first when: user wishes so or this is a single outgoing message
                if (subject_top || (outgoing && message.visible == 1)) {
                    result.add(message.subject); // Don't want to ellipsize for a11y
                    result.add(tvTime.getText().toString());
                    result.add(from);
                } else {
                    result.add(from);
                    result.add(tvTime.getText().toString());
                    result.add(message.subject);
                }

                if (message.encrypted > 0)
                    result.add(context.getString(R.string.title_legend_encrypted));
                else if (message.signed > 0)
                    result.add(context.getString(R.string.title_legend_signed));

                if (ibAuth.getVisibility() == View.VISIBLE)
                    result.add(context.getString(R.string.title_legend_auth));

                if (ivFound.getVisibility() == View.VISIBLE)
                    result.add(context.getString(R.string.title_legend_found));

                if (ibSnoozed.getVisibility() == View.VISIBLE)
                    result.add(context.getString(R.string.title_legend_snoozed));

                if (expanded) {
                    if (message.receipt_request != null && message.receipt_request)
                        result.add(context.getString(R.string.title_legend_receipt));
                    if (message.plain_only != null && message.plain_only)
                        result.add(context.getString(R.string.title_legend_plain_only));
                    if (message.ui_browsed)
                        result.add(context.getString(R.string.title_legend_browsed));
                }

                if (tvFolder.getVisibility() == View.VISIBLE)
                    result.add(tvFolder.getText().toString());

                if (tvSize.getVisibility() == View.VISIBLE)
                    result.add(tvSize.getText().toString());

                if (tvError.getVisibility() == View.VISIBLE)
                    result.add(tvError.getText().toString());

                if (tvPreview.getVisibility() == View.VISIBLE)
                    result.add(tvPreview.getText().toString());

                return TextUtils.join(", ", result);
            }
        };

        private class ToolData {
            private boolean isGmail;
            private List<EntityFolder> folders;
            private List<EntityAttachment> attachments;
        }
    }

    AdapterMessage(Fragment parentFragment,
                   String type, boolean found, ViewType viewType,
                   boolean compact, int zoom, String sort, boolean ascending, boolean filter_duplicates,
                   final IProperties properties) {
        this.parentFragment = parentFragment;
        this.type = type;
        this.found = found;
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
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.dp1 = Helper.dp2pixels(context, 1);
        this.dp12 = Helper.dp2pixels(context, 12);
        this.dp60 = Helper.dp2pixels(context, 60);

        this.accessibility = Helper.isAccessibilityEnabled(context);

        this.TF = Helper.getTimeInstance(context, SimpleDateFormat.SHORT);
        this.DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.LONG, SimpleDateFormat.LONG);

        ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
        this.suitable = state.isSuitable();
        this.unmetered = state.isUnmetered();

        this.colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        this.textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        this.textColorTertiary = Helper.resolveColor(context, android.R.attr.textColorTertiary);
        this.textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);

        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        boolean highlight_subject = prefs.getBoolean("highlight_subject", false);
        int colorHighlight = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorUnreadHighlight));

        this.colorUnread = (highlight_unread ? colorHighlight : Helper.resolveColor(context, R.attr.colorUnread));
        this.colorRead = Helper.resolveColor(context, R.attr.colorRead);
        this.colorSubject = Helper.resolveColor(context, highlight_subject ? R.attr.colorUnreadHighlight : R.attr.colorRead);
        this.colorVerified = Helper.resolveColor(context, R.attr.colorVerified);
        this.colorEncrypt = Helper.resolveColor(context, R.attr.colorEncrypt);
        this.colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
        this.colorError = Helper.resolveColor(context, R.attr.colorError);
        this.colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        this.colorControlNormal = Helper.resolveColor(context, R.attr.colorControlNormal);

        this.hasWebView = Helper.hasWebView(context);
        this.pin = ShortcutManagerCompat.isRequestPinShortcutSupported(context);
        this.contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        this.textSize = Helper.getTextSize(context, zoom);

        boolean contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        boolean avatars = prefs.getBoolean("avatars", true);
        boolean gravatars = prefs.getBoolean("gravatars", false);
        boolean favicons = prefs.getBoolean("favicons", false);
        boolean generated = prefs.getBoolean("generated_icons", true);

        this.date = prefs.getBoolean("date", true);
        this.cards = prefs.getBoolean("cards", true);
        this.shadow_unread = prefs.getBoolean("shadow_unread", false);
        this.threading = prefs.getBoolean("threading", true);
        this.threading_unread = threading && prefs.getBoolean("threading_unread", false);
        this.indentation = prefs.getBoolean("indentation", false);

        this.avatars = (contacts && avatars) || (gravatars || favicons || generated);
        this.color_stripe = prefs.getBoolean("color_stripe", true);
        this.check_authentication = prefs.getBoolean("check_authentication", true);
        this.check_reply_domain = prefs.getBoolean("check_reply_domain", true);
        this.check_mx = prefs.getBoolean("check_mx", false);
        this.check_blocklist = prefs.getBoolean("check_blocklist", false);

        this.email_format = MessageHelper.getAddressFormat(context);
        this.prefer_contact = prefs.getBoolean("prefer_contact", false);
        this.only_contact = prefs.getBoolean("only_contact", false);
        this.distinguish_contacts = prefs.getBoolean("distinguish_contacts", false);
        this.show_recipients = prefs.getBoolean("show_recipients", false);

        this.subject_top = prefs.getBoolean("subject_top", false);

        int fz_sender = prefs.getInt("font_size_sender", -1);
        if (fz_sender >= 0)
            font_size_sender = Helper.getTextSize(context, fz_sender);

        int fz_subject = prefs.getInt("font_size_subject", -1);
        if (fz_subject >= 0)
            font_size_subject = Helper.getTextSize(context, fz_subject);

        this.subject_italic = prefs.getBoolean("subject_italic", true);
        this.subject_ellipsize = prefs.getString("subject_ellipsize", "full");
        this.keywords_header = prefs.getBoolean("keywords_header", false);
        this.labels_header = prefs.getBoolean("labels_header", true);
        this.flags = prefs.getBoolean("flags", true);
        this.flags_background = prefs.getBoolean("flags_background", false);
        this.preview = prefs.getBoolean("preview", false);
        this.preview_italic = prefs.getBoolean("preview_italic", true);
        this.preview_lines = prefs.getInt("preview_lines", 2);
        this.message_zoom = prefs.getInt("message_zoom", 100);
        this.attachments_alt = prefs.getBoolean("attachments_alt", false);
        this.thumbnails = prefs.getBoolean("thumbnails", true);
        this.contrast = prefs.getBoolean("contrast", false);
        this.monospaced = prefs.getBoolean("monospaced", false);
        this.inline = prefs.getBoolean("inline_images", false);
        this.collapse_quotes = prefs.getBoolean("collapse_quotes", false);
        this.authentication = prefs.getBoolean("authentication", true);
        this.authentication_indicator = (this.authentication &&
                prefs.getBoolean("authentication_indicator", false));
        this.language_detection = prefs.getBoolean("language_detection", false);
        this.autoclose_unseen = prefs.getBoolean("autoclose_unseen", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            languages = new ArrayList<>();
            LocaleList ll = context.getResources().getConfiguration().getLocales();
            for (int i = 0; i < ll.size(); i++)
                languages.add(ll.get(i).getLanguage());
        } else
            languages = null;

        debug = prefs.getBoolean("debug", false);

        DiffUtil.ItemCallback<TupleMessageEx> callback = new DiffUtil.ItemCallback<TupleMessageEx>() {
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
                    log("Entity changed", next.id);
                }
                // extra
                if (!Objects.equals(prev.uid, next.uid)) {
                    same = false;
                    log("uid changed", next.id);

                    // Download body when needed
                    if (!next.content &&
                            prev.uid == null && next.uid != null && // once only
                            properties.getValue("expanded", next.id))
                        EntityOperation.queue(context, next, EntityOperation.BODY);
                }
                if (!Objects.equals(prev.msgid, next.msgid)) {
                    // debug info
                    same = false;
                    log("msgid changed", next.id);
                }
                // references
                // deliveredto
                // inreplyto
                if (!Objects.equals(prev.thread, next.thread)) {
                    same = false;
                    log("thread changed", next.id);
                }
                if (!Objects.equals(prev.ui_priority, next.ui_priority)) {
                    same = false;
                    log("ui_priority changed", next.id);
                }
                if (!Objects.equals(prev.ui_importance, next.ui_importance)) {
                    same = false;
                    log("ui_importance changed", next.id);
                }
                if (!Objects.equals(prev.receipt_request, next.receipt_request)) {
                    same = false;
                    log("receipt_request changed", next.id);
                }
                if (!MessageHelper.equal(prev.receipt_to, next.receipt_to)) {
                    same = false;
                    log("receipt_to changed", next.id);
                }
                if (!Objects.equals(prev.bimi_selector, next.bimi_selector)) {
                    same = false;
                    log("bimi_selector changed", next.id);
                }
                if (!Objects.equals(prev.dkim, next.dkim)) {
                    same = false;
                    log("dkim changed", next.id);
                }
                if (!Objects.equals(prev.spf, next.spf)) {
                    same = false;
                    log("spf changed", next.id);
                }
                if (!Objects.equals(prev.dmarc, next.dmarc)) {
                    same = false;
                    log("dmarc changed", next.id);
                }
                if (!Objects.equals(prev.mx, next.mx)) {
                    same = false;
                    log("mx changed", next.id);
                }
                if (!Objects.equals(prev.blocklist, next.blocklist)) {
                    same = false;
                    log("blocklist changed", next.id);
                }
                if (!Objects.equals(prev.reply_domain, next.reply_domain)) {
                    same = false;
                    log("reply_domain changed", next.id);
                }
                if (!Objects.equals(prev.avatar, next.avatar)) {
                    same = false;
                    log("avatar changed", next.id);
                }
                if (!Objects.equals(prev.sender, next.sender)) {
                    same = false;
                    log("sender changed", next.id);
                }
                if (!MessageHelper.equal(prev.from, next.from)) {
                    same = false;
                    log("from changed", next.id);
                }
                if (!MessageHelper.equal(prev.to, next.to)) {
                    same = false;
                    log("to changed", next.id);
                }
                if (!MessageHelper.equal(prev.cc, next.cc)) {
                    same = false;
                    log("cc changed", next.id);
                }
                if (!MessageHelper.equal(prev.bcc, next.bcc)) {
                    same = false;
                    log("bcc changed", next.id);
                }
                if (!MessageHelper.equal(prev.reply, next.reply)) {
                    same = false;
                    log("reply changed", next.id);
                }
                if (!MessageHelper.equal(prev.list_post, next.list_post)) {
                    same = false;
                    log("list_post changed", next.id);
                }
                if (!Objects.equals(prev.headers, next.headers)) {
                    same = false;
                    log("headers changed", next.id);
                }
                if (!Objects.equals(prev.raw, next.raw)) {
                    same = false;
                    log("raw changed", next.id);
                }
                if (!Objects.equals(prev.subject, next.subject)) {
                    same = false;
                    log("subject changed", next.id);
                }
                if (!Objects.equals(prev.size, next.size)) {
                    same = false;
                    log("size changed", next.id);
                }
                if (!Objects.equals(prev.total, next.total)) {
                    same = false;
                    log("total changed", next.id);
                }
                if (!Objects.equals(prev.attachments, next.attachments)) {
                    same = false;
                    log("attachments changed", next.id);
                }
                if (!prev.content.equals(next.content)) {
                    same = false;
                    log("content changed", next.id);
                }
                if (!Objects.equals(prev.language, next.language)) {
                    same = false;
                    log("language changed", next.id);
                }
                if (!Objects.equals(prev.plain_only, next.plain_only)) {
                    same = false;
                    log("plain_only changed", next.id);
                }
                if (!Objects.equals(prev.encrypt, next.encrypt)) {
                    same = false;
                    log("encrypt changed", next.id);
                }
                if (!Objects.equals(prev.ui_encrypt, next.ui_encrypt)) {
                    same = false;
                    log("ui_encrypt changed", next.id);
                }
                if (!Objects.equals(prev.verified, next.verified)) {
                    same = false;
                    log("verified changed", next.id);
                }
                if (!Objects.equals(prev.preview, next.preview)) {
                    same = false;
                    log("preview changed", next.id);
                }
                if (!Objects.equals(prev.notes, next.notes) ||
                        !Objects.equals(prev.notes_color, next.notes_color)) {
                    same = false;
                    log("notes/color changed", next.id);
                }
                if (!Objects.equals(prev.sent, next.sent)) {
                    same = false;
                    log("sent changed", next.id);
                }
                if (!prev.received.equals(next.received)) {
                    same = false;
                    log("received changed", next.id);
                }
                if (!prev.stored.equals(next.stored)) {
                    // updated after decryption
                    same = false;
                    log("stored changed", next.id);
                }
                // seen
                // answered
                // flagged
                // deleted
                if (debug && !Objects.equals(prev.flags, next.flags)) {
                    same = false;
                    log("flags changed", next.id);
                }
                if (!Helper.equal(prev.keywords, next.keywords)) {
                    same = false;
                    log("keywords changed", next.id);
                }
                if (!Helper.equal(prev.labels, next.labels)) {
                    same = false;
                    log("labels changed", next.id);
                }
                // notifying
                // fts
                if (!prev.ui_seen.equals(next.ui_seen)) {
                    same = false;
                    log("ui_seen changed " + prev.ui_seen + "/" + next.ui_seen, next.id);
                }
                if (!prev.ui_answered.equals(next.ui_answered)) {
                    same = false;
                    log("ui_answer changed", next.id);
                }
                if (!prev.ui_flagged.equals(next.ui_flagged)) {
                    same = false;
                    log("ui_flagged changed", next.id);
                }
                if (!prev.ui_deleted.equals(next.ui_deleted)) {
                    same = false;
                    log("ui_deleted changed", next.id);
                }
                if (!prev.ui_hide.equals(next.ui_hide)) {
                    same = false;
                    log("ui_hide changed", next.id);
                }
                if (!prev.ui_found.equals(next.ui_found)) {
                    same = false;
                    log("ui_found changed", next.id);
                }
                if (debug &&
                        !prev.ui_ignored.equals(next.ui_ignored)) {
                    same = false;
                    log("ui_ignored changed", next.id);
                }
                // ui_silent
                if (!prev.ui_browsed.equals(next.ui_browsed)) {
                    same = false;
                    log("ui_browsed changed", next.id);
                }
                if (!Objects.equals(prev.ui_busy, next.ui_busy)) {
                    same = false;
                    log("ui_busy changed " + prev.ui_busy + "/" + next.ui_busy, next.id);
                }
                if (!Objects.equals(prev.ui_snoozed, next.ui_snoozed)) {
                    same = false;
                    log("ui_snoozed changed", next.id);
                }
                if (!Objects.equals(prev.ui_unsnoozed, next.ui_unsnoozed)) {
                    same = false;
                    log("ui_unsnoozed changed", next.id);
                }
                // show_images
                // show_full
                if (!Objects.equals(prev.color, next.color)) {
                    same = false;
                    log("color changed", next.id);
                }
                // revision
                // revisions
                if (!Objects.equals(prev.warning, next.warning)) {
                    same = false;
                    log("warning changed", next.id);
                }
                if (!Objects.equals(prev.error, next.error)) {
                    same = false;
                    log("error changed", next.id);
                }
                if (!Objects.equals(prev.last_attempt, next.last_attempt)) {
                    same = false;
                    log("last_attempt changed " + prev.last_attempt + "/" + next.last_attempt, next.id);
                }

                // accountPop
                if (!Objects.equals(prev.accountName, next.accountName)) {
                    same = false;
                    log("accountName changed", next.id);
                }
                if (!Objects.equals(prev.accountColor, next.accountColor)) {
                    same = false;
                    log("accountColor changed", next.id);
                }
                // accountNotify
                // accountAutoSeen
                if (!prev.folderName.equals(next.folderName)) {
                    same = false;
                    log("folderName changed", next.id);
                }
                if (!Objects.equals(prev.folderDisplay, next.folderDisplay)) {
                    same = false;
                    log("folderDisplay changed", next.id);
                }
                if (!prev.folderType.equals(next.folderType)) {
                    same = false;
                    log("folderType changed", next.id);
                }
                if (prev.folderReadOnly != next.folderReadOnly) {
                    same = false;
                    log("folderReadOnly changed", next.id);
                }
                if (!Objects.equals(prev.identityName, next.identityName)) {
                    same = false;
                    log("identityName changed", next.id);
                }
                if (!Objects.equals(prev.identityEmail, next.identityEmail)) {
                    same = false;
                    log("identityEmail changed", next.id);
                }
                if (!Objects.equals(prev.identityColor, next.identityColor)) {
                    same = false;
                    log("identityColor changed", next.id);
                }
                if (!Objects.equals(prev.identitySynchronize, next.identitySynchronize)) {
                    same = false;
                    log("identitySynchronize changed", next.id);
                }
                // senders
                if (prev.count != next.count) {
                    same = false;
                    log("count changed " + prev.count + "/" + next.count, next.id);
                }
                if (prev.unseen != next.unseen) {
                    same = false;
                    log("unseen changed " + prev.unseen + "/" + next.unseen, next.id);
                }
                if (prev.unflagged != next.unflagged) {
                    same = false;
                    log("unflagged changed", next.id);
                }
                if (prev.drafts != next.drafts) {
                    same = false;
                    log("drafts changed", next.id);
                }
                if (prev.signed != next.signed) {
                    same = false;
                    log("signed changed", next.id);
                }
                if (prev.encrypted != next.encrypted) {
                    same = false;
                    log("encrypted changed", next.id);
                }
                if (prev.visible != next.visible) {
                    same = false;
                    log("visible changed " + prev.visible + "/" + next.visible, next.id);
                }
                if (prev.visible_unseen != next.visible_unseen) {
                    same = false;
                    log("visible_unseen changed " + prev.visible_unseen + "/" + next.visible_unseen, next.id);
                }
                if (!Objects.equals(prev.totalSize, next.totalSize)) {
                    same = false;
                    log("totalSize changed", next.id);
                }
                if (prev.duplicate != next.duplicate) {
                    same = false;
                    log("duplicate changed", next.id);
                }

                return same;
            }

            private void log(String msg, long id) {
                Log.i(msg + " id=" + id);
                if (BuildConfig.DEBUG || debug)
                    parentFragment.getView().post(new Runnable() {
                        @Override
                        public void run() {
                            if (properties.getValue("expanded", id)) {
                                Context context = parentFragment.getContext();
                                if (context != null)
                                    ToastEx.makeText(context, msg + " id=" + id, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        };

        AsyncDifferConfig<TupleMessageEx> config = new AsyncDifferConfig.Builder<>(callback)
                .setBackgroundThreadExecutor(executor)
                .build();
        this.differ = new AsyncPagedListDiffer<>(new AdapterListUpdateCallback(this), config);
        this.differ.addPagedListListener(new AsyncPagedListDiffer.PagedListListener<TupleMessageEx>() {
            @Override
            public void onCurrentListChanged(@Nullable PagedList<TupleMessageEx> previousList, @Nullable PagedList<TupleMessageEx> currentList) {
                // https://issuetracker.google.com/issues/70149059
                if (rv != null && savedState != null)
                    rv.getLayoutManager().onRestoreInstanceState(savedState);
                savedState = null;

                if (gotoTop && previousList != null) {
                    if (ascending) {
                        if (currentList != null && currentList.size() > 0) {
                            properties.scrollTo(currentList.size() - 1, 0);
                            gotoTop = false;
                        }
                    } else {
                        gotoTop = false;
                        properties.scrollTo(0, 0);
                    }
                }

                if (gotoPos != null && currentList != null && currentList.size() > 0) {
                    properties.scrollTo(gotoPos, 0);
                    gotoPos = null;
                }

                if (selectionTracker != null && selectionTracker.hasSelection()) {
                    Selection<Long> selection = selectionTracker.getSelection();

                    long[] ids = new long[selection.size()];
                    int i = 0;
                    for (Long id : selection)
                        ids[i++] = id;

                    Bundle args = new Bundle();
                    args.putLongArray("ids", ids);
                    args.putString("type", type);

                    new SimpleTask<List<Long>>() {
                        @Override
                        protected List<Long> onExecute(Context context, Bundle args) throws Throwable {
                            long[] ids = args.getLongArray("ids");
                            String type = args.getString("type");

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean filter_seen = prefs.getBoolean(FragmentMessages.getFilter("seen", type), false);
                            boolean filter_unflagged = prefs.getBoolean(FragmentMessages.getFilter("unflagged", type), false);
                            boolean filter_snoozed = prefs.getBoolean(FragmentMessages.getFilter("snoozed", type), true);

                            List<Long> removed = new ArrayList<>();

                            DB db = DB.getInstance(context);
                            for (long id : ids)
                                if (db.message().countVisible(id, filter_seen, filter_unflagged, filter_snoozed) == 0)
                                    removed.add(id);

                            return removed;
                        }

                        @Override
                        protected void onExecuted(Bundle args, List<Long> removed) {
                            Log.i("Selection removed=" + removed.size());
                            for (long id : removed)
                                selectionTracker.deselect(id);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "selection:update");
                }
            }
        });

        try {
            // https://issuetracker.google.com/issues/135628748
            Field mMainThreadExecutor = this.differ.getClass().getDeclaredField("mMainThreadExecutor");
            mMainThreadExecutor.setAccessible(true);
            mMainThreadExecutor.set(this.differ, new Executor() {
                @Override
                public void execute(final Runnable command) {
                    ApplicationEx.getMainHandler().post(new Runnable() {
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
        if (ascending) {
            PagedList<TupleMessageEx> list = getCurrentList();
            if (list != null && list.size() > 0)
                properties.scrollTo(list.size() - 1, 0);
        } else
            properties.scrollTo(0, 0);
        this.gotoTop = true;
    }

    void gotoPos(int pos) {
        if (pos != RecyclerView.NO_POSITION)
            gotoPos = pos;
    }

    void submitList(PagedList<TupleMessageEx> list) {
        for (int i = 0; i < list.size(); i++) {
            TupleMessageEx message = list.get(i);
            if (message != null) {
                keyPosition.put(message.id, i);
                positionKey.put(i, message.id);
                message.resolveKeywordColors(context);
            }
        }

        if (rv != null)
            savedState = rv.getLayoutManager().onSaveInstanceState();

        differ.submitList(list);
    }

    PagedList<TupleMessageEx> getCurrentList() {
        return differ.getCurrentList();
    }

    void setCompact(boolean compact) {
        if (this.compact != compact) {
            this.compact = compact;
            properties.refresh();
        }
    }

    void setZoom(int zoom) {
        if (this.zoom != zoom) {
            this.zoom = zoom;
            textSize = Helper.getTextSize(context, zoom);
            properties.refresh();
        }
    }

    int getZoom() {
        return this.zoom;
    }

    void setSort(String sort) {
        if (!sort.equals(this.sort)) {
            this.sort = sort;
            properties.refresh();
            // Needed to redraw item decorators / add/remove size
        }
    }

    String getSort() {
        return this.sort;
    }

    void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    boolean getAscending() {
        return ascending;
    }

    void setFilterDuplicates(boolean filter_duplicates) {
        if (this.filter_duplicates != filter_duplicates) {
            this.filter_duplicates = filter_duplicates;
            properties.refresh();
        }
    }

    void checkInternet() {
        ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
        if (this.suitable != state.isSuitable() || this.unmetered != state.isUnmetered()) {
            this.suitable = state.isSuitable();
            this.unmetered = state.isUnmetered();
            PagedList<TupleMessageEx> list = differ.getCurrentList();
            if (list != null)
                for (int i = 0; i < list.size(); i++) {
                    TupleMessageEx message = list.get(i);
                    if (message != null &&
                            (!message.content || message.attachments > 0) &&
                            properties.getValue("expanded", message.id))
                        notifyItemChanged(i);
                }
        }
    }

    @Override
    public int getItemCount() {
        return differ.getItemCount();
    }

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
        return new ViewHolder(inflater.inflate(viewType, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            _onBindViewHolder(holder, position);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void _onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TupleMessageEx message = differ.getItem(position);
        holder.powner.recreate(message == null ? null : message.id);

        if (message == null || context == null)
            return;

        Integer p = keyPosition.get(message.id);
        Long i = positionKey.get(position);
        if (p != null)
            positionKey.remove(p);
        if (i != null)
            keyPosition.remove(i);

        keyPosition.put(message.id, position);
        positionKey.put(position, message.id);

        message.resolveKeywordColors(context);

        if (viewType == ViewType.THREAD && cards && threading && indentation) {
            boolean outgoing = holder.isOutgoing(message);
            ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            lparam.setMarginStart(outgoing ? dp12 : 0);
            lparam.setMarginEnd(outgoing ? 0 : dp12);
            holder.itemView.setLayoutParams(lparam);
        }

        if (filter_duplicates && message.duplicate) {
            holder.card.setCardBackgroundColor(message.folderColor == null
                    ? Color.TRANSPARENT
                    : ColorUtils.setAlphaComponent(message.folderColor, 128));
            holder.tvFolder.setText(context.getString(R.string.title_duplicate_in, message.getFolderName(context)));
            holder.tvFolder.setTypeface(message.unseen > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            holder.tvFolder.setTextColor(message.unseen > 0 ? colorUnread : colorRead);
            holder.tvFolder.setAlpha(Helper.LOW_LIGHT);
            return;
        }

        boolean scroll = false;
        if (viewType == ViewType.THREAD) {
            scroll = properties.getValue("scroll", message.id);
            properties.setValue("scroll", message.id, false);
        }

        holder.unwire();
        holder.bindTo(message, scroll);
        holder.wire();
    }

    public void onItemSelected(@NonNull ViewHolder holder, boolean selected) {
        if (accessibility && holder.view != null)
            try {
                AccessibilityEvent event = AccessibilityEvent.obtain();
                holder.view.onInitializeAccessibilityEvent(event);
                event.setEventType(AccessibilityEvent.TYPE_VIEW_SELECTED);
                holder.view.getParent().requestSendAccessibilityEvent(holder.view, event);
            } catch (Throwable ex) {
                Log.w(ex);
            }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.cowner.stop();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.cowner.recreate();

        if (holder.ibAvatar != null)
            holder.ibAvatar.setImageDrawable(null);
        if (holder.tvBody != null)
            holder.tvBody.setText(null);
        if (holder.wvBody instanceof WebView)
            ((WebView) holder.wvBody).loadDataWithBaseURL(null, "", "text/html", StandardCharsets.UTF_8.name(), null);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        rv = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        rv = null;
    }

    void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    int getPositionForKey(long key) {
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
        Long key = positionKey.get(pos);
        Log.d("Key=" + key + " @Position=" + pos);
        return key;
    }

    interface IProperties {
        void setValue(String key, String value);

        void setValue(String name, long id, boolean enabled);

        boolean getValue(String name, long id);

        void setExpanded(TupleMessageEx message, boolean expanded, boolean scroll);

        void setSize(long id, Float size);

        float getSize(long id, float defaultSize);

        void setHeight(long id, Integer height);

        int getHeight(long id, int defaultHeight);

        void setPosition(long id, Pair<Integer, Integer> position);

        Pair<Integer, Integer> getPosition(long id);

        void setAttachments(long id, List<EntityAttachment> attachments);

        List<EntityAttachment> getAttachments(long id);

        void scrollTo(int pos, int y);

        void scrollBy(int x, int y);

        void ready(long id);

        void move(long id, String type);

        void reply(TupleMessageEx message, String selected, View anchor);

        void startSearch(TextView view);

        void endSearch();

        void lock(long id);

        void refresh();

        void finish();
    }

    public static class FragmentDialogNotes extends FragmentDialogBase {
        private ViewButtonColor btnColor;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Bundle args = getArguments();
            final long id = args.getLong("id");
            final String notes = args.getString("notes");

            final Context context = getContext();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            final Integer color = (TextUtils.isEmpty(notes)
                    ? prefs.getInt("note_color", Color.TRANSPARENT)
                    : args.getInt("color"));

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_notes, null);
            final EditText etNotes = view.findViewById(R.id.etNotes);
            btnColor = view.findViewById(R.id.btnColor);

            etNotes.setText(notes);
            btnColor.setColor(color);

            etNotes.selectAll();

            btnColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.hideKeyboard(etNotes);

                    Bundle args = new Bundle();
                    args.putInt("color", btnColor.getColor());
                    args.putString("title", getString(R.string.title_color));
                    args.putBoolean("reset", true);

                    FragmentDialogColor fragment = new FragmentDialogColor();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentDialogNotes.this, 1);
                    fragment.show(getParentFragmentManager(), "identity:color");
                }
            });

            final SimpleTask<Void> task = new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    String notes = args.getString("notes");
                    Integer color = args.getInt("color");

                    if ("".equals(notes.trim()))
                        notes = null;

                    if (color == Color.TRANSPARENT)
                        color = null;

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        db.message().setMessageNotes(message.id, notes, color);

                        if (TextUtils.isEmpty(message.msgid))
                            return null;

                        List<EntityMessage> messages = db.message().getMessagesByMsgId(message.account, message.msgid);
                        if (messages == null)
                            return null;

                        for (EntityMessage m : messages)
                            db.message().setMessageNotes(m.id, notes, color);

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
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putLong("id", id);
                            args.putString("notes", etNotes.getText().toString());
                            args.putInt("color", btnColor.getColor());

                            task.execute(getContext(), getActivity(), args, "message:note");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            if (!TextUtils.isEmpty(notes))
                builder.setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLong("id", id);
                        args.putString("notes", "");
                        args.putInt("color", Color.TRANSPARENT);

                        task.execute(getContext(), getActivity(), args, "message:note");
                    }
                });

            return builder.create();
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            try {
                if (resultCode == RESULT_OK && data != null) {
                    Bundle args = data.getBundleExtra("args");
                    int color = args.getInt("color");
                    btnColor.setColor(color);

                    Context context = getContext();
                    if (context != null) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        prefs.edit().putInt("note_color", color).apply();
                    }
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    }

    public static class FragmentDialogTranslate extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Context context = getContext();
            final View view = LayoutInflater.from(context).inflate(R.layout.dialog_translate, null);
            final Spinner spLanguage = view.findViewById(R.id.spLanguage);
            final TextView tvText = view.findViewById(R.id.tvText);
            final ContentLoadingProgressBar pbWait = view.findViewById(R.id.pbWait);

            List<DeepL.Language> languages = DeepL.getTargetLanguages(context);
            ArrayAdapter<DeepL.Language> adapter = new ArrayAdapter<DeepL.Language>(context, android.R.layout.simple_spinner_item, android.R.id.text1, languages) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    return _getView(position, super.getView(position, convertView, parent));
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    return _getView(position, super.getDropDownView(position, convertView, parent));
                }

                private View _getView(int position, View view) {
                    DeepL.Language language = getItem(position);
                    if (language != null) {
                        TextView tv = view.findViewById(android.R.id.text1);

                        Drawable icon = context.getDrawable(language.icon);
                        int iconSize = context.getResources()
                                .getDimensionPixelSize(R.dimen.menu_item_icon_size);
                        icon.setBounds(0, 0, iconSize, iconSize);
                        ImageSpan imageSpan = new CenteredImageSpan(icon);

                        SpannableStringBuilder ssb = new SpannableStringBuilder(language.name);
                        ssb.insert(0, "\uFFFC\u2002"); // object replacement character, en space
                        ssb.setSpan(imageSpan, 0, 1, 0);

                        tv.setText(ssb);
                    }

                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spLanguage.setAdapter(adapter);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String current = prefs.getString("deepl_target", null);

            for (int pos = 0; pos < languages.size(); pos++)
                if (languages.get(pos).target.equals(current)) {
                    spLanguage.setSelection(pos);
                    break;
                }

            spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    prefs.edit().putString("deepl_target", languages.get(position).target).apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    prefs.edit().remove("deepl_target").apply();
                }
            });

            tvText.setText(null);

            new SimpleTask<String>() {
                @Override
                protected void onPreExecute(Bundle args) {
                    pbWait.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(Bundle args) {
                    pbWait.setVisibility(View.GONE);
                }

                @Override
                protected String onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    File file = EntityMessage.getFile(context, id);
                    String html = Helper.readText(file);
                    Document d = HtmlHelper.sanitizeCompose(context, html, false);
                    d.select("blockquote").remove();
                    HtmlHelper.truncate(d, MAX_TRANSLATABLE_TEXT_SIZE);
                    SpannableStringBuilder ssb = HtmlHelper.fromDocument(context, d, null, null);
                    return ssb.toString()
                            .replace("\uFFFC", "") // Object replacement character
                            .replaceAll("\n\\s+\n", "\n")
                            .replaceAll("\n+", "\n\n");
                }

                @Override
                protected void onExecuted(Bundle args, String text) {
                    tvText.setText(text);

                    tvText.setMovementMethod(new ArrowKeyMovementMethod() {
                        @Override
                        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP)
                                translate(widget, buffer, event);
                            return super.onTouchEvent(widget, buffer, event);
                        }

                        private void translate(TextView widget, Spannable buffer, MotionEvent event) {
                            int off = Helper.getOffset(widget, buffer, event);

                            int start = off;
                            while (start > 0 && buffer.charAt(start - 1) != '\n')
                                start--;

                            int end = off;
                            while (end < buffer.length() && buffer.charAt(end - 1) != '\n')
                                end++;

                            if (end <= start)
                                return;

                            StyleSpan[] spans = buffer.getSpans(start, end, StyleSpan.class);
                            if (spans != null && spans.length > 0)
                                return;

                            final StyleSpan mark = new StyleSpan(Typeface.ITALIC);
                            buffer.setSpan(mark, start, end, 0);

                            DeepL.Language language = (DeepL.Language) spLanguage.getSelectedItem();
                            if (language == null)
                                return;

                            Bundle args = new Bundle();
                            args.putString("target", language.target);
                            args.putString("text", buffer.subSequence(start, end).toString());

                            new SimpleTask<DeepL.Translation>() {
                                @Override
                                protected void onPreExecute(Bundle args) {
                                    pbWait.setVisibility(View.VISIBLE);
                                }

                                @Override
                                protected void onPostExecute(Bundle args) {
                                    pbWait.setVisibility(View.GONE);
                                }

                                @Override
                                protected DeepL.Translation onExecute(Context context, Bundle args) throws Throwable {
                                    String text = args.getString("text");
                                    String target = args.getString("target");
                                    return DeepL.translate(text, target, context);
                                }

                                @Override
                                protected void onExecuted(Bundle args, DeepL.Translation translation) {
                                    SpannableStringBuilder ssb = new SpannableStringBuilder(tvText.getText());
                                    int start = ssb.getSpanStart(mark);
                                    int end = ssb.getSpanEnd(mark);
                                    int textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);

                                    ssb.removeSpan(mark);

                                    ssb = ssb.replace(start, end, translation.translated_text);
                                    end = start + translation.translated_text.length();

                                    ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    ssb.setSpan(new ForegroundColorSpan(textColorPrimary), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    Locale source = Locale.forLanguageTag(translation.detected_language);
                                    Locale target = Locale.forLanguageTag(args.getString("target"));

                                    String lang = "[" + source.getDisplayLanguage(target) + "] ";
                                    ssb.insert(start, lang);

                                    ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, start + lang.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), start, start + lang.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    tvText.setText(ssb);
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Throwable exex = new Throwable("DeepL", ex);
                                    ToastEx.makeText(context, Log.formatThrowable(exex), Toast.LENGTH_LONG).show();
                                }
                            }.execute(FragmentDialogTranslate.this, args, "paragraph:translate");
                        }
                    });
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    tvText.setText(ex.toString());
                }
            }.execute(this, getArguments(), "message:translate");

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.cancel, null);

            return builder.create();
        }
    }

    public static class FragmentDialogKeywordManage extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long id = getArguments().getLong("id");

            final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_keyword_manage, null);
            final RecyclerView rvKeyword = dview.findViewById(R.id.rvKeyword);
            final TextView tvPro = dview.findViewById(R.id.tvPro);
            final FloatingActionButton fabAdd = dview.findViewById(R.id.fabAdd);
            final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

            rvKeyword.setHasFixedSize(false);
            final LinearLayoutManager llm = new LinearLayoutManager(getContext());
            rvKeyword.setLayoutManager(llm);

            final AdapterKeyword adapter = new AdapterKeyword(getContext(), getViewLifecycleOwner());
            rvKeyword.setAdapter(adapter);

            Helper.linkPro(tvPro);

            fabAdd.setEnabled(ActivityBilling.isPro(getContext()));
            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putLong("id", id);

                    FragmentDialogKeywordAdd fragment = new FragmentDialogKeywordAdd();
                    fragment.setArguments(args);
                    fragment.show(getParentFragmentManager(), "keyword:add");
                }
            });

            pbWait.setVisibility(View.VISIBLE);

            DB db = DB.getInstance(getContext());
            db.message().liveMessageKeywords(id).observe(getViewLifecycleOwner(), new Observer<TupleKeyword.Persisted>() {
                @Override
                public void onChanged(TupleKeyword.Persisted data) {
                    pbWait.setVisibility(View.GONE);
                    adapter.set(id, TupleKeyword.from(getContext(), data));
                }
            });

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_manage_keywords)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        @Override
        public void onResume() {
            super.onResume();
            Dialog dialog = getDialog();
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public static class FragmentDialogKeywordAdd extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long id = getArguments().getLong("id");

            final Context context = getContext();

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_keyword_add, null);
            final EditText etKeyword = view.findViewById(R.id.etKeyword);
            etKeyword.setText(null);

            return new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                                }.execute(getContext(), getActivity(), args, "keyword:add");
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public static class FragmentDialogLabelsManage extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long id = getArguments().getLong("id");
            String self = getArguments().getString("self");
            String[] labels = getArguments().getStringArray("labels");
            final String[] folders = getArguments().getStringArray("folders");

            List<String> l = new ArrayList<>();
            if (self != null)
                l.add(self);
            if (labels != null)
                l.addAll(Arrays.asList(labels));

            boolean[] checked = new boolean[folders.length];
            for (int i = 0; i < folders.length; i++)
                if (l.contains(folders[i]))
                    checked[i] = true;

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_manage_labels)
                    .setMultiChoiceItems(folders, checked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            Bundle args = new Bundle();
                            args.putLong("id", id);
                            args.putString("label", folders[which]);
                            args.putBoolean("set", isChecked);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    long id = args.getLong("id");
                                    String label = args.getString("label");
                                    boolean set = args.getBoolean("set");

                                    DB db = DB.getInstance(context);
                                    EntityMessage message = db.message().getMessage(id);
                                    if (message == null)
                                        return null;

                                    EntityOperation.queue(context, message, EntityOperation.LABEL, label, set);

                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }
                            }.execute(FragmentDialogLabelsManage.this, args, "label:set");
                        }
                    })
                    .create();
        }
    }

    public static class FragmentDialogOpenFull extends FragmentDialogBase {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.fullScreenDialog);
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null)
                dialog.getWindow().setLayout(MATCH_PARENT, MATCH_PARENT);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            String html = getArguments().getString("html");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean overview_mode = prefs.getBoolean("overview_mode", false);
            boolean safe_browsing = prefs.getBoolean("safe_browsing", false);
            boolean confirm_html = prefs.getBoolean("confirm_html", true);
            boolean html_dark = prefs.getBoolean("html_dark", confirm_html);

            View view = inflater.inflate(R.layout.fragment_open_full, container, false);
            WebView wv = view.findViewById(R.id.wv);

            WebSettings settings = wv.getSettings();
            settings.setUserAgentString(WebViewEx.getUserAgent(getContext(), wv));
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(overview_mode);

            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);

            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

            settings.setAllowFileAccess(false);
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                settings.setSafeBrowsingEnabled(safe_browsing);

            if (html_dark &&
                    WebViewEx.isFeatureSupported(WebViewFeature.FORCE_DARK))
                WebSettingsCompat.setForceDark(settings,
                        Helper.isDarkTheme(getContext()) ? FORCE_DARK_ON : FORCE_DARK_OFF);

            settings.setLoadsImagesAutomatically(true);
            settings.setBlockNetworkLoads(false);
            settings.setBlockNetworkImage(false);

            wv.loadDataWithBaseURL(null, html, "text/html", StandardCharsets.UTF_8.name(), null);

            return view;
        }
    }

    public static class FragmentDialogPrint extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Context context = getContext();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            View dview = LayoutInflater.from(context).inflate(R.layout.dialog_print, null);
            CheckBox cbHeader = dview.findViewById(R.id.cbHeader);
            CheckBox cbImages = dview.findViewById(R.id.cbImages);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            cbHeader.setChecked(prefs.getBoolean("print_html_header", true));
            cbHeader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("print_html_header", isChecked).apply();
                }
            });

            cbImages.setChecked(prefs.getBoolean("print_html_images", true));
            cbImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("print_html_images", isChecked).apply();
                }
            });

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("print_html_confirmed", isChecked).apply();
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

    public static class FragmentDialogButtons extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Context context = getContext();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_buttons, null);
            final CheckBox cbJunk = dview.findViewById(R.id.cbJunk);
            final CheckBox cbTrash = dview.findViewById(R.id.cbTrash);
            final CheckBox cbArchive = dview.findViewById(R.id.cbArchive);
            final CheckBox cbMove = dview.findViewById(R.id.cbMove);
            final CheckBox cbCopy = dview.findViewById(R.id.cbCopy);
            final CheckBox cbKeywords = dview.findViewById(R.id.cbKeywords);
            final CheckBox cbNotes = dview.findViewById(R.id.cbNotes);
            final CheckBox cbSeen = dview.findViewById(R.id.cbSeen);
            final CheckBox cbHide = dview.findViewById(R.id.cbHide);
            final CheckBox cbSearch = dview.findViewById(R.id.cbSearch);
            final CheckBox cbSearchText = dview.findViewById(R.id.cbSearchText);
            final CheckBox cbTranslate = dview.findViewById(R.id.cbTranslate);
            final CheckBox cbEvent = dview.findViewById(R.id.cbEvent);
            final CheckBox cbShare = dview.findViewById(R.id.cbShare);
            final CheckBox cbPin = dview.findViewById(R.id.cbPin);
            final CheckBox cbPrint = dview.findViewById(R.id.cbPrint);
            final CheckBox cbHeaders = dview.findViewById(R.id.cbHeaders);
            final CheckBox cbUnsubscribe = dview.findViewById(R.id.cbUnsubscribe);
            final CheckBox cbRule = dview.findViewById(R.id.cbRule);

            cbTranslate.setVisibility(DeepL.isAvailable(context) ? View.VISIBLE : View.GONE);
            cbPin.setVisibility(Shortcuts.can(context) ? View.VISIBLE : View.GONE);

            cbJunk.setChecked(prefs.getBoolean("button_junk", true));
            cbTrash.setChecked(prefs.getBoolean("button_trash", true));
            cbArchive.setChecked(prefs.getBoolean("button_archive", true));
            cbMove.setChecked(prefs.getBoolean("button_move", true));
            cbCopy.setChecked(prefs.getBoolean("button_copy", false));
            cbKeywords.setChecked(prefs.getBoolean("button_keywords", false));
            cbNotes.setChecked(prefs.getBoolean("button_notes", false));
            cbSeen.setChecked(prefs.getBoolean("button_seen", false));
            cbHide.setChecked(prefs.getBoolean("button_hide", false));
            cbSearch.setChecked(prefs.getBoolean("button_search", false));
            cbSearchText.setChecked(prefs.getBoolean("button_search_text", false));
            cbTranslate.setChecked(prefs.getBoolean("button_translate", false));
            cbEvent.setChecked(prefs.getBoolean("button_event", false));
            cbShare.setChecked(prefs.getBoolean("button_share", false));
            cbPin.setChecked(prefs.getBoolean("button_pin", false));
            cbPrint.setChecked(prefs.getBoolean("button_print", false));
            cbHeaders.setChecked(prefs.getBoolean("button_headers", false));
            cbUnsubscribe.setChecked(prefs.getBoolean("button_unsubscribe", true));
            cbRule.setChecked(prefs.getBoolean("button_rule", false));

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("button_junk", cbJunk.isChecked());
                            editor.putBoolean("button_trash", cbTrash.isChecked());
                            editor.putBoolean("button_archive", cbArchive.isChecked());
                            editor.putBoolean("button_move", cbMove.isChecked());
                            editor.putBoolean("button_copy", cbCopy.isChecked());
                            editor.putBoolean("button_keywords", cbKeywords.isChecked());
                            editor.putBoolean("button_notes", cbNotes.isChecked());
                            editor.putBoolean("button_seen", cbSeen.isChecked());
                            editor.putBoolean("button_hide", cbHide.isChecked());
                            editor.putBoolean("button_search", cbSearch.isChecked());
                            editor.putBoolean("button_search_text", cbSearchText.isChecked());
                            editor.putBoolean("button_translate", cbTranslate.isChecked());
                            editor.putBoolean("button_event", cbEvent.isChecked());
                            editor.putBoolean("button_share", cbShare.isChecked());
                            editor.putBoolean("button_pin", cbPin.isChecked());
                            editor.putBoolean("button_print", cbPrint.isChecked());
                            editor.putBoolean("button_headers", cbHeaders.isChecked());
                            editor.putBoolean("button_unsubscribe", cbUnsubscribe.isChecked());
                            editor.putBoolean("button_rule", cbRule.isChecked());
                            editor.apply();
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
