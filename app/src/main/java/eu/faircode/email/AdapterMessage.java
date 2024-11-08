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

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.RemoteAction;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
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
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.textclassifier.ConversationAction;
import android.view.textclassifier.ConversationActions;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.text.method.LinkMovementMethodCompat;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import androidx.webkit.WebViewFeature;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
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
import java.util.SortedMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.WriteContext;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.CalendarScale;
import biweekly.property.Created;
import biweekly.property.LastModified;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceRule;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Transparency;
import biweekly.util.ICalDate;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.ViewHolder> {
    private Fragment parentFragment;
    private String type;
    private boolean found;
    private String searched;
    private boolean searchedPartial;
    private ViewType viewType;
    private boolean compact;
    private int zoom;
    private String sort;
    private boolean ascending;
    private boolean filter_duplicates;
    private boolean filter_trash;
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

    private int colorCardBackground;
    private int colorStripeWidth;
    private int colorAccent;
    private int textColorPrimary;
    private int textColorSecondary;
    private int textColorTertiary;
    private int textColorLink;
    private int textColorHighlightInverse;
    private int colorUnreadHighlight;
    private int colorUnread;
    private int colorRead;
    private int colorSubject;
    private int colorVerified;
    private int colorEncrypt;
    private int colorSeparator;
    private int colorBookmark;
    private int colorWarning;
    private int colorError;
    private int colorControlNormal;

    private boolean hasWebView;
    private boolean pin;
    private boolean contacts;
    private float textSize;

    private boolean date;
    private boolean date_week;
    private boolean date_fixed;
    private boolean date_time;
    private boolean cards;
    private boolean shadow_unread;
    private boolean shadow_border;
    private boolean shadow_highlight;
    private boolean tabular_unread_bg;
    private boolean threading;
    private boolean threading_unread;
    private boolean indentation;
    private boolean avatars;
    private int account_color;
    private boolean check_authentication;
    private boolean native_dkim;
    private boolean check_tls;
    private boolean check_reply_domain;
    private boolean check_mx;
    private boolean check_blocklist;
    private boolean show_addresses_default;
    private boolean hide_attachments_default;

    private MessageHelper.AddressFormat email_format;
    private boolean prefer_contact;
    private boolean only_contact;
    private boolean distinguish_contacts;
    private boolean show_recipients;
    private boolean reverse_addresses;
    private Float font_size_sender;
    private Float font_size_subject;
    private boolean subject_top;
    private boolean subject_italic;
    private String sender_ellipsize;
    private String subject_ellipsize;

    private boolean show_filtered;
    private boolean keywords_header;
    private boolean labels_header;
    private boolean flags;
    private boolean flags_background;
    private boolean preview;
    private boolean preview_italic;
    private int preview_lines;
    private boolean align_header;
    private boolean large_buttons;
    private int message_zoom;
    private boolean attachments_alt;
    private boolean thumbnails;
    private boolean pdf_preview;
    private boolean video_preview;
    private boolean audio_preview;
    private boolean contrast;
    private boolean hyphenation;
    private String display_font;
    private boolean inline;
    private boolean collapse_quotes;
    private boolean authentication;
    private boolean authentication_indicator;
    private boolean infra;
    private boolean tld_flags;

    private boolean autoclose_unseen;
    private boolean collapse_marked;

    private boolean language_detection;
    private List<String> languages;
    private static boolean debug;
    private boolean canDarken;
    private boolean fake_dark;
    private boolean show_recent;

    private boolean gotoTop = false;
    private Integer gotoPos = null;
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
    private DateFormat DTFS;

    private static final ExecutorService executorDiffer =
            Helper.getBackgroundExecutor(0, "differ");
    private static final ExecutorService executorAvatar =
            Helper.getBackgroundExecutor(0, "avatar");

    private static final int MAX_RECIPIENTS_COMPACT = 3;
    private static final int MAX_RECIPIENTS_NORMAL = 7;

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener,
            CompoundButton.OnCheckedChangeListener,
            View.OnLayoutChangeListener {
        private ViewCardOptional card;
        private View view;
        private View header;

        private ImageView ivBadge;
        private View vwColor;
        private ImageButton ibExpander;
        private ImageView ibFlagged;
        private ImageButton ibAvatar;
        private ImageButton ibVerified;
        private ImageButton ibAuth;
        private ImageButton ibPriority;
        private ImageButton ibSensitivity;
        private ImageView ivImportance;
        private ImageButton ibSigned;
        private ImageButton ibEncrypted;
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
        private ImageButton ibError;

        private View vsBody;

        private ImageButton ibExpanderAddress;

        private ImageView ivPlain;
        private ImageButton ibReceipt;
        private ImageView ivAutoSubmitted;
        private ImageView ivList;
        private ImageView ivBrowsed;
        private ImageView ivRaw;

        private ImageButton ibSearchContact;
        private ImageButton ibNotifyContact;
        private ImageButton ibPinContact;
        private ImageButton ibAddContact;

        private TextView tvSignedByTitle;
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

        private TextView tvSignedBy;
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
        private View vSeparatorAttachments;
        private ImageButton ibExpanderAttachments;
        private TextView tvAttachments;
        private CheckBox cbInline;
        private ImageButton ibSaveAttachments;
        private ImageButton ibDownloadAttachments;
        private TextView tvNoInternetAttachments;

        private View vSeparator;
        private ImageButton ibFull;
        private ImageButton ibImages;
        private ImageButton ibAmp;
        private ImageButton ibDecrypt;
        private ImageButton ibVerify;
        private ImageButton ibUndo;
        private ImageButton ibAnswer;
        private ImageButton ibRule;
        private ImageButton ibUnsubscribe;
        private ImageButton ibRaw;
        private ImageButton ibHtml;
        private ImageButton ibHeaders;
        private ImageButton ibPrint;
        private ImageButton ibPin;
        private ImageButton ibShare;
        private ImageButton ibEvent;
        private ImageButton ibSearchText;
        private ImageButton ibSearch;
        private ImageButton ibTranslate;
        private ImageButton ibSummarize;
        private ImageButton ibFullScreen;
        private ImageButton ibForceLight;
        private ImageButton ibImportance;
        private ImageButton ibHide;
        private ImageButton ibSeen;
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
        private View vwEmpty;
        private Flow ibFlow;
        private ImageButton ibTools;
        private Flow buttons;
        private TextView tvReformatted;
        private TextView tvDecrypt;
        private TextView tvSignedData;

        private TextView tvBody;
        private View wvBody;
        private ContentLoadingProgressBar pbBody;
        private View vwRipple;
        private TextView tvNoInternetBody;
        private ImageButton ibDownloading;
        private Group grpDownloading;
        private ImageButton ibDownload;
        private ImageButton ibInfrastructure;
        private ImageButton ibTrashBottom;
        private ImageButton ibArchiveBottom;
        private ImageButton ibMoveBottom;
        private ImageButton ibSeenBottom;
        private Flow flow;

        private ImageButton ibCalendar;
        private ImageButton ibOnline;
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

        private ImageButton ibStoreMedia;
        private ImageButton ibShareImages;
        private RecyclerView rvMedia;

        private Group grpAddresses;
        private Group grpHeaders;
        private Group grpAction;
        private Group grpCalendar;
        private Group grpCalendarResponse;
        private Group grpMedia;

        private AdapterAttachment adapterAttachment;
        private AdapterMedia adapterMedia;

        private TwoStateOwner eowner = new TwoStateOwner(owner, "MessageExpanded");
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
                        boolean show_images = properties.getValue("images", message.id);

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean inline = prefs.getBoolean("inline_images", false);

                        if (scale > 10)
                            return true;

                        // Text size
                        properties.setSize(message.id, size);
                        tvBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

                        // Image size
                        Spanned spanned = (Spanned) tvBody.getText();
                        for (ImageSpan img : spanned.getSpans(0, spanned.length(), ImageSpan.class)) {
                            String source = img.getSource();
                            boolean embedded = (source != null && source.startsWith("cid:"));
                            Drawable d = img.getDrawable();
                            int w = 0;
                            int h = 0;
                            if (img instanceof ImageSpanEx) {
                                if (show_images || (embedded && inline)) {
                                    w = ((ImageSpanEx) img).getWidth();
                                    h = ((ImageSpanEx) img).getHeight();
                                } else {
                                    w = (zoom + 1) * 24;
                                    h = w;
                                }
                            }
                            ImageHelper.fitDrawable(d, w, h, scale, tvBody);
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
                    if (view.getId() == R.id.tvBody)
                        view.getParent().requestDisallowInterceptTouchEvent(false);
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
                            return onClick(event, false);
                        }

                        @Override
                        public void onLongPress(@NonNull MotionEvent event) {
                            boolean confirm_links = prefs.getBoolean("confirm_links", true);
                            if (!confirm_links)
                                onClick(event, true);
                        }

                        private boolean onClick(MotionEvent event, boolean longClick) {
                            Spannable buffer = (Spannable) tvBody.getText();
                            int off = Helper.getOffset(tvBody, buffer, event);

                            TupleMessageEx message = getMessage();
                            if (message == null)
                                return false;

                            boolean show_images = properties.getValue("images", message.id);
                            if (!show_images) {
                                ImageSpan[] image = buffer.getSpans(off, off, ImageSpan.class);
                                if (image.length > 0 && image[0].getSource() != null) {
                                    Uri uri = Uri.parse(image[0].getSource());

                                    if (UriHelper.isHyperLink(uri)) {
                                        ripple(event);
                                        if (onOpenLink(uri, null,
                                                longClick || EntityFolder.JUNK.equals(message.folderType)))
                                            return true;
                                    }
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

                                ripple(event);
                                if (onOpenLink(uri, title,
                                        longClick || EntityFolder.JUNK.equals(message.folderType)))
                                    return true;
                            }

                            ImageSpan[] image = buffer.getSpans(off, off, ImageSpan.class);
                            if (image.length > 0 && image[0] instanceof ImageSpanEx) {
                                String tracking = ((ImageSpanEx) image[0]).getTracking();
                                if (!TextUtils.isEmpty(tracking)) {
                                    onOpenLink(Uri.parse(tracking),
                                            context.getString(R.string.title_legend_tracking_pixel), true);
                                    return true;
                                }

                                ripple(event);
                                onOpenImage(message.id, image[0].getSource(),
                                        longClick || EntityFolder.JUNK.equals(message.folderType));
                                return true;
                            }

                            if (!longClick) {
                                DynamicDrawableSpan[] ddss = buffer.getSpans(off, off, DynamicDrawableSpan.class);
                                if (ddss.length > 0) {
                                    int f = buffer.getSpanFlags(ddss[0]);
                                    properties.setValue("quotes", message.id, (f & Spanned.SPAN_USER) == 0);
                                    properties.setHeight(message.id, null);
                                    bindBody(message, false);
                                    return true;
                                }
                            }

                            return false;
                        }

                        private void ripple(MotionEvent event) {
                            int r = context.getResources().getDimensionPixelSize(R.dimen.ripple_radius);
                            vwRipple.setLeft(tvBody.getLeft() + Math.round(event.getX()) - r);
                            vwRipple.setTop(tvBody.getTop() + Math.round(event.getY()) - r);
                            vwRipple.setRight(tvBody.getLeft() + Math.round(event.getX()) + r);
                            vwRipple.setBottom(tvBody.getTop() + Math.round(event.getY()) + r);
                            vwRipple.setClickable(true);
                            vwRipple.setPressed(true);
                            vwRipple.setPressed(false);
                            vwRipple.setClickable(false);
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

            ivBadge = itemView.findViewById(R.id.ivBadge);
            vwColor = itemView.findViewById(R.id.vwColor);
            ibExpander = itemView.findViewById(R.id.ibExpander);
            ibFlagged = itemView.findViewById(R.id.ibFlagged);
            ibAvatar = itemView.findViewById(R.id.ibAvatar);
            ibVerified = itemView.findViewById(R.id.ibVerified);
            ibAuth = itemView.findViewById(R.id.ibAuth);
            ibPriority = itemView.findViewById(R.id.ibPriority);
            ibSensitivity = itemView.findViewById(R.id.ibSensitivity);
            ivImportance = itemView.findViewById(R.id.ivImportance);
            ibSigned = itemView.findViewById(R.id.ibSigned);
            ibEncrypted = itemView.findViewById(R.id.ibEncrypted);
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
            if (tvError != null)
                tvError.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
            ibError = itemView.findViewById(R.id.ibError);

            if (account_color == 1 && vwColor != null)
                vwColor.getLayoutParams().width = colorStripeWidth;

            if (tvFrom != null) {
                if (compact) {
                    boolean full = "full".equals(sender_ellipsize);
                    tvFrom.setSingleLine(!full);

                    if ("start".equals(sender_ellipsize))
                        tvFrom.setEllipsize(TextUtils.TruncateAt.START);
                    else if ("end".equals(sender_ellipsize))
                        tvFrom.setEllipsize(TextUtils.TruncateAt.END);
                    else if ("middle".equals(sender_ellipsize))
                        tvFrom.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                    else
                        tvFrom.setEllipsize(null);
                }
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

            if (tvKeywords != null) {
                if (compact)
                    tvKeywords.setSingleLine(true);
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
            ivList = vsBody.findViewById(R.id.ivList);
            ivBrowsed = vsBody.findViewById(R.id.ivBrowsed);
            ivRaw = vsBody.findViewById(R.id.ivRaw);

            ibSearchContact = vsBody.findViewById(R.id.ibSearchContact);
            ibNotifyContact = vsBody.findViewById(R.id.ibNotifyContact);
            ibPinContact = vsBody.findViewById(R.id.ibPinContact);
            ibAddContact = vsBody.findViewById(R.id.ibAddContact);

            tvSignedByTitle = vsBody.findViewById(R.id.tvSignedByTitle);
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

            tvSignedBy = vsBody.findViewById(R.id.tvSignedBy);
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
            tvHeaders.setMovementMethod(LinkMovementMethodCompat.getInstance());
            ibCopyHeaders = vsBody.findViewById(R.id.ibCopyHeaders);
            ibCloseHeaders = vsBody.findViewById(R.id.ibCloseHeaders);
            pbHeaders = vsBody.findViewById(R.id.pbHeaders);
            tvNoInternetHeaders = vsBody.findViewById(R.id.tvNoInternetHeaders);

            ibCalendar = vsBody.findViewById(R.id.ibCalendar);
            ibOnline = vsBody.findViewById(R.id.ibOnline);
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

            adapterAttachment = new AdapterAttachment(parentFragment, true, properties);
            rvAttachment.setAdapter(adapterAttachment);

            vSeparatorAttachments = attachments.findViewById(R.id.vSeparatorAttachments);
            ibExpanderAttachments = attachments.findViewById(R.id.ibExpanderAttachments);
            tvAttachments = attachments.findViewById(R.id.tvAttachments);
            cbInline = attachments.findViewById(R.id.cbInline);
            ibSaveAttachments = attachments.findViewById(R.id.ibSaveAttachments);
            ibDownloadAttachments = attachments.findViewById(R.id.ibDownloadAttachments);
            tvNoInternetAttachments = attachments.findViewById(R.id.tvNoInternetAttachments);

            vSeparator = vsBody.findViewById(R.id.vSeparator);
            ibFull = vsBody.findViewById(R.id.ibFull);
            ibImages = vsBody.findViewById(R.id.ibImages);
            ibAmp = vsBody.findViewById(R.id.ibAmp);
            ibDecrypt = vsBody.findViewById(R.id.ibDecrypt);
            ibVerify = vsBody.findViewById(R.id.ibVerify);
            ibUndo = vsBody.findViewById(R.id.ibUndo);
            ibAnswer = vsBody.findViewById(R.id.ibAnswer);
            ibRule = vsBody.findViewById(R.id.ibRule);
            ibUnsubscribe = vsBody.findViewById(R.id.ibUnsubscribe);
            ibRaw = vsBody.findViewById(R.id.ibRaw);
            ibHtml = vsBody.findViewById(R.id.ibHtml);
            ibHeaders = vsBody.findViewById(R.id.ibHeaders);
            ibPrint = vsBody.findViewById(R.id.ibPrint);
            ibPin = vsBody.findViewById(R.id.ibPin);
            ibShare = vsBody.findViewById(R.id.ibShare);
            ibEvent = vsBody.findViewById(R.id.ibEvent);
            ibSearchText = vsBody.findViewById(R.id.ibSearchText);
            ibSearch = vsBody.findViewById(R.id.ibSearch);
            ibTranslate = vsBody.findViewById(R.id.ibTranslate);
            ibSummarize = vsBody.findViewById(R.id.ibSummarize);
            ibFullScreen = vsBody.findViewById(R.id.ibFullScreen);
            ibForceLight = vsBody.findViewById(R.id.ibForceLight);
            ibImportance = vsBody.findViewById(R.id.ibImportance);
            ibHide = vsBody.findViewById(R.id.ibHide);
            ibSeen = vsBody.findViewById(R.id.ibSeen);
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
            vwEmpty = vsBody.findViewById(R.id.vwEmpty);
            ibFlow = vsBody.findViewById(R.id.ibFlow);
            ibTools = vsBody.findViewById(R.id.ibTools);

            buttons = vsBody.findViewById(R.id.buttons);

            tvReformatted = vsBody.findViewById(R.id.tvReformatted);
            tvDecrypt = vsBody.findViewById(R.id.tvDecrypt);
            tvSignedData = vsBody.findViewById(R.id.tvSignedData);

            tvBody = vsBody.findViewById(R.id.tvBody);
            if (hyphenation &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                    tvBody.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL); // Default before Q
                else
                    tvBody.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    tvBody.setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY);
            }
            wvBody = vsBody.findViewById(R.id.wvBody);
            pbBody = vsBody.findViewById(R.id.pbBody);
            vwRipple = vsBody.findViewById(R.id.vwRipple);
            tvNoInternetBody = vsBody.findViewById(R.id.tvNoInternetBody);
            ibDownloading = vsBody.findViewById(R.id.ibDownloading);
            grpDownloading = vsBody.findViewById(R.id.grpDownloading);
            ibDownload = vsBody.findViewById(R.id.ibDownload);
            ibInfrastructure = vsBody.findViewById(R.id.ibInfrastructure);
            ibTrashBottom = vsBody.findViewById(R.id.ibTrashBottom);
            ibArchiveBottom = vsBody.findViewById(R.id.ibArchiveBottom);
            ibMoveBottom = vsBody.findViewById(R.id.ibMoveBottom);
            ibSeenBottom = vsBody.findViewById(R.id.ibSeenBottom);
            flow = vsBody.findViewById(R.id.flow);

            ibStoreMedia = vsBody.findViewById(R.id.ibStoreMedia);
            ibShareImages = vsBody.findViewById(R.id.ibShareImages);
            rvMedia = vsBody.findViewById(R.id.rvMedia);
            rvMedia.setHasFixedSize(false);
            StaggeredGridLayoutManager sglm =
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            rvMedia.setLayoutManager(sglm);
            adapterMedia = new AdapterMedia(parentFragment);
            rvMedia.setAdapter(adapterMedia);

            grpAddresses = vsBody.findViewById(R.id.grpAddresses);
            grpHeaders = vsBody.findViewById(R.id.grpHeaders);
            grpAction = vsBody.findViewById(R.id.grpAction);
            grpCalendar = vsBody.findViewById(R.id.grpCalendar);
            grpCalendarResponse = vsBody.findViewById(R.id.grpCalendarResponse);
            grpMedia = vsBody.findViewById(R.id.grpMedia);

            if (large_buttons) {
                int dp36 = Helper.dp2pixels(context, 42);
                List<Integer> ids = new ArrayList<>();
                ids.addAll(Helper.fromIntArray(ibFlow.getReferencedIds()));
                ids.addAll(Arrays.asList(R.id.ibTools, R.id.vwEmpty,
                        R.id.ibFull, R.id.ibImages, R.id.ibAmp,
                        R.id.ibDecrypt, R.id.ibVerify,
                        R.id.ibInfrastructure,
                        R.id.ibTrashBottom, R.id.ibArchiveBottom, R.id.ibMoveBottom,
                        R.id.ibSeenBottom));
                for (int id : ids) {
                    View v = view.findViewById(id);
                    ViewGroup.LayoutParams lparam = v.getLayoutParams();
                    lparam.width = dp36;
                    lparam.height = dp36;
                }
            }

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

            itemView.addOnLayoutChangeListener(this);
            ibAvatar.setOnClickListener(this);
            ibVerified.setOnClickListener(this);
            ibAuth.setOnClickListener(this);
            ibPriority.setOnClickListener(this);
            ibSensitivity.setOnClickListener(this);
            ibSigned.setOnClickListener(this);
            ibEncrypted.setOnClickListener(this);
            ibSnoozed.setOnClickListener(this);
            ibFlagged.setOnClickListener(this);
            if (viewType == ViewType.THREAD) {
                ibFlagged.setOnLongClickListener(this);
                tvFolder.setOnLongClickListener(this);
            }
            tvError.setOnClickListener(this);
            ibError.setOnClickListener(this);

            if (vsBody != null) {
                ibExpanderAddress.setOnClickListener(this);
                ibReceipt.setOnClickListener(this);
                ibSearchContact.setOnClickListener(this);
                ibNotifyContact.setOnClickListener(this);
                ibPinContact.setOnClickListener(this);
                ibAddContact.setOnClickListener(this);

                if (BuildConfig.DEBUG) {
                    ibPinContact.setOnLongClickListener(this);
                    ibAddContact.setOnLongClickListener(this);
                }

                ibCopyHeaders.setOnClickListener(this);
                ibCloseHeaders.setOnClickListener(this);

                ibExpanderAttachments.setOnClickListener(this);
                cbInline.setOnCheckedChangeListener(this);

                ibSaveAttachments.setOnClickListener(this);
                ibDownloadAttachments.setOnClickListener(this);

                ibFull.setOnClickListener(this);
                ibFull.setOnLongClickListener(this);
                ibImages.setOnClickListener(this);
                ibAmp.setOnClickListener(this);
                ibDecrypt.setOnClickListener(this);
                ibVerify.setOnClickListener(this);
                ibUndo.setOnClickListener(this);
                ibAnswer.setOnClickListener(this);
                ibRule.setOnClickListener(this);
                ibUnsubscribe.setOnClickListener(this);
                ibRaw.setOnClickListener(this);
                ibHtml.setOnClickListener(this);
                ibHeaders.setOnClickListener(this);
                ibHeaders.setOnLongClickListener(this);
                ibPrint.setOnClickListener(this);
                ibPin.setOnClickListener(this);
                ibShare.setOnClickListener(this);
                ibEvent.setOnClickListener(this);
                ibSearchText.setOnClickListener(this);
                ibSearch.setOnClickListener(this);
                ibTranslate.setOnClickListener(this);
                ibTranslate.setOnLongClickListener(this);
                ibSummarize.setOnClickListener(this);
                ibSummarize.setOnLongClickListener(this);
                ibFullScreen.setOnClickListener(this);
                ibForceLight.setOnClickListener(this);
                ibImportance.setOnClickListener(this);
                ibImportance.setOnLongClickListener(this);
                ibHide.setOnClickListener(this);
                ibSeen.setOnClickListener(this);
                ibNotes.setOnClickListener(this);
                ibNotes.setOnLongClickListener(this);
                ibLabels.setOnClickListener(this);
                ibKeywords.setOnClickListener(this);
                ibCopy.setOnClickListener(this);
                ibMove.setOnClickListener(this);
                ibMove.setOnLongClickListener(this);
                ibArchive.setOnClickListener(this);
                ibTrash.setOnClickListener(this);
                ibTrash.setOnLongClickListener(this);
                ibJunk.setOnClickListener(this);
                ibInbox.setOnClickListener(this);
                ibInbox.setOnLongClickListener(this);
                ibMore.setOnClickListener(this);
                ibTools.setOnClickListener(this);

                ibDownloading.setOnClickListener(this);
                ibDownload.setOnClickListener(this);
                ibInfrastructure.setOnClickListener(this);
                ibTrashBottom.setOnClickListener(this);
                ibTrashBottom.setOnLongClickListener(this);
                ibArchiveBottom.setOnClickListener(this);
                ibMoveBottom.setOnClickListener(this);
                ibMoveBottom.setOnLongClickListener(this);
                ibSeenBottom.setOnClickListener(this);

                tvBody.setOnTouchListener(touchListener);
                tvBody.setTextIsSelectable(false);
                tvBody.setTextIsSelectable(true);
                tvBody.setMovementMethod(movementMethod);
                tvBody.addOnLayoutChangeListener(this);

                ibCalendar.setOnClickListener(this);
                ibOnline.setOnClickListener(this);
                btnCalendarAccept.setOnClickListener(this);
                btnCalendarDecline.setOnClickListener(this);
                btnCalendarMaybe.setOnClickListener(this);

                btnCalendarAccept.setOnLongClickListener(this);
                btnCalendarDecline.setOnLongClickListener(this);
                btnCalendarMaybe.setOnLongClickListener(this);

                ibStoreMedia.setOnClickListener(this);
                ibShareImages.setOnClickListener(this);
            }

            if (accessibility) {
                view.setAccessibilityDelegate(accessibilityDelegateHeader);
                header.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
        }

        private void unwire() {
            final View touch = (viewType == ViewType.THREAD ? ibExpander : header);
            touch.setOnClickListener(null);

            itemView.removeOnLayoutChangeListener(this);
            ibAvatar.setOnClickListener(null);
            ibVerified.setOnClickListener(null);
            ibAuth.setOnClickListener(null);
            ibPriority.setOnClickListener(null);
            ibSensitivity.setOnClickListener(null);
            ibSigned.setOnClickListener(null);
            ibEncrypted.setOnClickListener(null);
            ibSnoozed.setOnClickListener(null);
            ibFlagged.setOnClickListener(null);
            if (viewType == ViewType.THREAD) {
                ibFlagged.setOnLongClickListener(null);
                tvFolder.setOnLongClickListener(null);
            }
            tvError.setOnClickListener(null);
            ibError.setOnClickListener(null);

            if (vsBody != null) {
                ibExpanderAddress.setOnClickListener(null);
                ibReceipt.setOnLongClickListener(null);
                ibSearchContact.setOnClickListener(null);
                ibNotifyContact.setOnClickListener(null);
                ibPinContact.setOnClickListener(null);
                ibAddContact.setOnClickListener(null);

                if (BuildConfig.DEBUG) {
                    ibPinContact.setOnLongClickListener(null);
                    ibAddContact.setOnLongClickListener(null);
                }

                ibCopyHeaders.setOnClickListener(null);
                ibCloseHeaders.setOnClickListener(null);

                ibExpanderAttachments.setOnClickListener(null);
                cbInline.setOnCheckedChangeListener(null);

                ibSaveAttachments.setOnClickListener(null);
                ibDownloadAttachments.setOnClickListener(null);

                ibFull.setOnClickListener(null);
                ibFull.setOnLongClickListener(null);
                ibImages.setOnClickListener(null);
                ibAmp.setOnClickListener(null);
                ibDecrypt.setOnClickListener(null);
                ibVerify.setOnClickListener(null);
                ibUndo.setOnClickListener(null);
                ibAnswer.setOnClickListener(null);
                ibRule.setOnClickListener(null);
                ibUnsubscribe.setOnClickListener(null);
                ibRaw.setOnClickListener(null);
                ibHtml.setOnClickListener(null);
                ibHeaders.setOnClickListener(null);
                ibHeaders.setOnLongClickListener(null);
                ibPrint.setOnClickListener(null);
                ibPin.setOnClickListener(null);
                ibShare.setOnClickListener(null);
                ibEvent.setOnClickListener(null);
                ibSearchText.setOnClickListener(null);
                ibSearch.setOnClickListener(null);
                ibTranslate.setOnClickListener(null);
                ibTranslate.setOnLongClickListener(null);
                ibSummarize.setOnClickListener(null);
                ibSummarize.setOnLongClickListener(null);
                ibFullScreen.setOnClickListener(null);
                ibForceLight.setOnClickListener(null);
                ibImportance.setOnClickListener(null);
                ibImportance.setOnLongClickListener(null);
                ibHide.setOnClickListener(null);
                ibSeen.setOnClickListener(null);
                ibNotes.setOnClickListener(null);
                ibNotes.setOnLongClickListener(null);
                ibLabels.setOnClickListener(null);
                ibKeywords.setOnClickListener(null);
                ibCopy.setOnClickListener(null);
                ibMove.setOnClickListener(null);
                ibMove.setOnClickListener(null);
                ibArchive.setOnClickListener(null);
                ibTrash.setOnClickListener(null);
                ibTrash.setOnLongClickListener(null);
                ibJunk.setOnClickListener(null);
                ibInbox.setOnClickListener(null);
                ibInbox.setOnLongClickListener(null);
                ibMore.setOnClickListener(null);
                ibTools.setOnClickListener(null);

                ibDownloading.setOnClickListener(null);
                ibDownload.setOnClickListener(null);
                ibInfrastructure.setOnClickListener(null);
                ibTrashBottom.setOnClickListener(null);
                ibTrashBottom.setOnLongClickListener(null);
                ibArchiveBottom.setOnClickListener(null);
                ibMoveBottom.setOnClickListener(null);
                ibMoveBottom.setOnLongClickListener(null);
                ibSeenBottom.setOnClickListener(null);

                tvBody.setOnTouchListener(null);
                tvBody.setMovementMethod(null);
                tvBody.removeOnLayoutChangeListener(this);

                btnCalendarAccept.setOnClickListener(null);
                btnCalendarDecline.setOnClickListener(null);
                btnCalendarMaybe.setOnClickListener(null);
                ibCalendar.setOnClickListener(null);
                ibOnline.setOnClickListener(null);

                btnCalendarAccept.setOnLongClickListener(null);
                btnCalendarDecline.setOnLongClickListener(null);
                btnCalendarMaybe.setOnLongClickListener(null);

                ibStoreMedia.setOnClickListener(null);
                ibShareImages.setOnClickListener(null);
            }

            if (accessibility)
                view.setAccessibilityDelegate(null);
        }

        @SuppressLint("WrongConstant")
        private void bindTo(final TupleMessageEx message, boolean scroll) {
            boolean inbox = EntityFolder.INBOX.equals(message.folderType);
            boolean outbox = EntityFolder.OUTBOX.equals(message.folderType);
            boolean outgoing = isOutgoing(message);
            boolean reverse = reverse_addresses &&
                    ((outgoing && viewType != ViewType.THREAD &&
                            (EntityFolder.isOutgoing(type) || viewType == ViewType.SEARCH)) ||
                            (viewType == ViewType.UNIFIED && type == null &&
                                    message.folderUnified && outgoing) ||
                            (viewType == ViewType.FOLDER &&
                                    message.folderUnified && outgoing) ||
                            EntityFolder.isOutgoing(message.folderInheritedType));
            String selector = (reverse ? null : message.bimi_selector);
            Address[] addresses = (reverse ? message.to : (message.isForwarder() ? message.submitter : message.from));
            Address[] senders = ContactInfo.fillIn(
                    reverse && !show_recipients ? message.to : message.senders, prefer_contact, only_contact);
            Address[] recipients = ContactInfo.fillIn(
                    reverse && !show_recipients ? message.from : message.recipients, prefer_contact, only_contact);
            boolean authenticated =
                    !((Boolean.FALSE.equals(message.dkim) && check_authentication) ||
                            (Boolean.FALSE.equals(message.spf) && check_authentication) ||
                            (Boolean.FALSE.equals(message.dmarc) && check_authentication) ||
                            (Boolean.FALSE.equals(message.auth) && check_authentication) ||
                            (Boolean.FALSE.equals(message.reply_domain) && check_reply_domain) ||
                            (Boolean.FALSE.equals(message.mx) && check_mx) ||
                            (Boolean.TRUE.equals(message.blocklist) && check_blocklist));
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
                ibSensitivity.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ivImportance.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibSigned.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
                ibEncrypted.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
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
                ibError.setAlpha(dim ? Helper.LOW_LIGHT : 1.0f);
            }

            bindSeen(message);

            // Account color
            int colorBackground =
                    (message.accountColor == null || !ActivityBilling.isPro(context)
                            ? colorSeparator : message.accountColor);

            if (account_color == 2 &&
                    !Objects.equals(ivBadge.getTag(), colorBackground)) {
                ivBadge.setTag(colorBackground);
                Helper.setColor(ivBadge.getDrawable(), colorBackground);
            }
            ivBadge.setVisibility(account_color == 2 ? View.VISIBLE : View.GONE);

            if (account_color == 1 &&
                    !Objects.equals(vwColor.getTag(), colorBackground)) {
                vwColor.setTag(colorBackground);
                vwColor.setBackgroundColor(colorBackground);
            }
            vwColor.setVisibility(account_color == 1 ? View.VISIBLE : View.GONE);

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
            } else if (authentication && authentication_indicator) {
                int auths =
                        (Boolean.TRUE.equals(message.dkim) ? 1 : 0) +
                                (Boolean.TRUE.equals(message.spf) ? 1 : 0) +
                                (Boolean.TRUE.equals(message.dmarc) ? 1 : 0);

                // https://en.wikipedia.org/wiki/DMARC#Alignment
                if (Boolean.TRUE.equals(message.dmarc) &&
                        (Boolean.TRUE.equals(message.dkim) ||
                                Boolean.TRUE.equals(message.spf)))
                    auths = 3;

                if (Boolean.TRUE.equals(message.auth))
                    auths = 3;
                if (check_tls && Boolean.TRUE.equals(message.tls))
                    auths++;

                if (!Boolean.TRUE.equals(message.auth) &&
                        message.dkim == null && message.spf == null && message.dmarc == null)
                    ibAuth.setImageLevel(1);
                else
                    ibAuth.setImageLevel(auths + 2);
                ibAuth.setImageTintList(ColorStateList.valueOf(
                        auths >= 3 ? colorVerified : colorControlNormal));
                ibAuth.setVisibility(auths > 0 || (check_tls && !outgoing) ? View.VISIBLE : View.GONE);
            } else
                ibAuth.setVisibility(View.GONE);

            if (EntityMessage.PRIORITIY_HIGH.equals(message.ui_priority)) {
                ibPriority.setImageLevel(message.ui_priority);
                ibPriority.setVisibility(View.VISIBLE);
            } else if (EntityMessage.PRIORITIY_LOW.equals(message.ui_priority)) {
                ibPriority.setImageLevel(message.ui_priority);
                ibPriority.setVisibility(View.VISIBLE);
            } else
                ibPriority.setVisibility(View.GONE);

            ibSensitivity.setImageLevel(message.sensitivity == null ? 0 : message.sensitivity);
            ibSensitivity.setVisibility(message.sensitivity == null ? View.GONE : View.VISIBLE);

            if (EntityMessage.PRIORITIY_HIGH.equals(message.ui_importance)) {
                ivImportance.setImageLevel(message.ui_importance);
                ivImportance.setImageTintList(ColorStateList.valueOf(colorWarning));
                ivImportance.setVisibility(View.VISIBLE);
            } else if (EntityMessage.PRIORITIY_LOW.equals(message.ui_importance)) {
                ivImportance.setImageLevel(message.ui_importance);
                ivImportance.setImageTintList(ColorStateList.valueOf(colorControlNormal));
                ivImportance.setVisibility(View.VISIBLE);
            } else
                ivImportance.setVisibility(View.GONE);

            if (!Objects.equals(ibSigned.getTag(), message.verified)) {
                ibSigned.setTag(message.verified);
                if (message.verified)
                    ibSigned.setColorFilter(colorEncrypt);
                else
                    ibSigned.clearColorFilter();
            }
            ibSigned.setVisibility(message.isSigned() ? View.VISIBLE : View.GONE);
            ibEncrypted.setVisibility(message.isEncrypted() ? View.VISIBLE : View.GONE);

            MessageHelper.AddressFormat format = email_format;

            if (show_recipients && recipients != null && recipients.length > 0) {
                int maxRecipients = (viewType == ViewType.THREAD
                        ? Integer.MAX_VALUE
                        : (compact ? MAX_RECIPIENTS_COMPACT : MAX_RECIPIENTS_NORMAL));
                tvFrom.setText(context.getString(outgoing && viewType != ViewType.THREAD && compact
                                ? R.string.title_to_from
                                : R.string.title_from_to,
                        formatAddresses(senders, format, maxRecipients),
                        formatAddresses(recipients, format, maxRecipients)));
            } else {
                if (tld_flags && viewType == ViewType.THREAD) {
                    Address[] a = (message.reply == null || message.reply.length == 0 ? message.from : message.reply);
                    if (outgoing || a == null || a.length == 0)
                        tvFrom.setCompoundDrawablesRelative(null, null, null, null);
                    else {
                        String email = ((InternetAddress) a[0]).getAddress();
                        String domain = UriHelper.getEmailDomain(email);
                        String tld = (domain == null ? "" : UriHelper.getTld(context, domain));
                        int resid = context.getResources().getIdentifier(
                                "flag_" + tld, "drawable", context.getPackageName());
                        Drawable d = (resid > 0 ? context.getDrawable(resid) : null);
                        tvFrom.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
                    }
                }

                tvFrom.setText(MessageHelper.formatAddresses(senders, format, false));
            }

            tvFrom.setPaintFlags(tvFrom.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
            tvSize.setText(message.totalSize == null ? null : Helper.humanReadableByteCount(message.totalSize));
            tvSize.setVisibility(
                    message.totalSize != null && ("size".equals(sort) || "attachments".equals(sort))
                            ? View.VISIBLE : View.GONE);
            SpannableStringBuilder time;
            if (EntityMessage.PRIORITIY_HIGH.equals(message.importance) ||
                    EntityMessage.PRIORITIY_LOW.equals(message.importance))
                time = new SpannableStringBuilderEx(Helper.getRelativeDateTimeSpanString(context, message.received, true));
            else if (date_time)
                time = new SpannableStringBuilderEx(Helper.getRelativeDateTimeSpanString(context, message.received, false));
            else if (date_week)
                time = new SpannableStringBuilderEx(Helper.getRelativeDateSpanString(context, message.received));
            else
                time = new SpannableStringBuilderEx(
                        (date && !date_fixed && FragmentMessages.SORT_DATE_HEADER.contains(sort)) ||
                                (date_fixed && "time".equals(sort))
                                ? TF.format(message.received)
                                : Helper.getRelativeTimeSpanString(context, message.received));
            if (show_recent && message.recent)
                time.setSpan(new UnderlineSpan(), 0, time.length(), 0);
            tvTime.setText(time);

            // Line 2
            tvSubject.setText(message.subject);

            // Workaround layout bug
            //tvSubject.requestLayout();
            //tvSubject.invalidate();

            if (keywords_header) {
                Spanned keywords = getKeywords(message);
                tvKeywords.setVisibility(keywords == null ? View.GONE : View.VISIBLE);
                tvKeywords.setText(keywords);
            } else
                tvKeywords.setVisibility(View.GONE);

            // Line 3
            int icon = (message.drafts > 0
                    ? R.drawable.twotone_edit_24
                    : (outgoing ? R.drawable.twotone_send_24_80 : EntityFolder.getIcon(message.folderType)));
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
            ivClassified.setImageResource(message.isFiltered()
                    ? R.drawable.twotone_filter_alt_24 : R.drawable.twotone_push_pin_24);
            ivClassified.setVisibility(message.auto_classified || (show_filtered && message.isFiltered())
                    ? View.VISIBLE : View.GONE);

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
            ivAttachments.setVisibility(message.totalAttachments > 0 ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.FOLDER)
                tvFolder.setText(outbox ? message.identityEmail : message.accountName);
            else if (viewType == ViewType.THREAD || viewType == ViewType.SEARCH)
                tvFolder.setText(message.getFolderName(context));
            else {
                SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                ssb.append(message.accountName);
                if (message.accountColor != null)
                    ssb.setSpan(new ForegroundColorSpan(message.accountColor), 0, ssb.length(), 0);
                ssb.append('/');
                int s = ssb.length();
                ssb.append(message.getFolderName(context));
                if (message.folderColor != null)
                    ssb.setSpan(new ForegroundColorSpan(message.folderColor), s, ssb.length(), 0);
                tvFolder.setText(ssb);
            }

            tvFolder.setVisibility(compact && viewType != ViewType.THREAD ? View.GONE : View.VISIBLE);

            Spanned labels = getLabels(message);
            tvLabels.setText(labels);
            tvLabels.setVisibility(labels == null ? View.GONE : View.VISIBLE);

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
                    ivThread.setColorFilter(colorBookmark);
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
                    StyleHelper.getTypeface(display_font, context),
                    preview_italic ? Typeface.ITALIC : Typeface.NORMAL);
            tvPreview.setText(message.preview);
            tvPreview.setVisibility(preview && !TextUtils.isEmpty(message.preview) ? View.VISIBLE : View.GONE);

            tvNotes.setText(message.notes);
            tvNotes.setTextColor(message.notes_color == null ? textColorSecondary : message.notes_color);
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
                ibError.setVisibility(View.VISIBLE);
            } else {
                tvError.setText(error);
                tvError.setVisibility(error == null ? View.GONE : View.VISIBLE);
                ibError.setVisibility(error == null ? View.GONE : View.VISIBLE);
            }

            // Contact info
            ContactInfo[] info = ContactInfo.getCached(context,
                    message.account, message.folderType, selector, Boolean.TRUE.equals(message.dmarc), addresses);
            if (info == null) {
                if (taskContactInfo != null) {
                    taskContactInfo.cancel(context);
                    taskContactInfo = null;
                }

                Bundle aargs = new Bundle();
                aargs.putLong("id", message.id);
                aargs.putLong("account", message.account);
                aargs.putString("folderType", message.folderType);
                aargs.putString("selector", selector);
                aargs.putBoolean("dmarc", Boolean.TRUE.equals(message.dmarc));
                aargs.putSerializable("addresses", addresses);

                taskContactInfo = new SimpleTask<ContactInfo[]>() {
                    @Override
                    protected ContactInfo[] onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");
                        String folderType = args.getString("folderType");
                        String selector = args.getString("selector");
                        boolean dmarc = args.getBoolean("dmarc");
                        Address[] addresses = (Address[]) args.getSerializable("addresses");
                        return ContactInfo.get(context, account, folderType, selector, dmarc, addresses);
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
                    protected void onDestroyed(Bundle args) {
                        taskContactInfo = null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.setExecutor(executorAvatar).setLog(false);
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
        }

        private void clearExpanded(TupleMessageEx message) {
            if (compact) {
                if ("full".equals(sender_ellipsize)
                        ? tvFrom.getMaxLines() == 1
                        : tvFrom.getMaxLines() > 1)
                    tvFrom.setSingleLine(!"full".equals(sender_ellipsize));

                if ("full".equals(subject_ellipsize)
                        ? tvSubject.getMaxLines() == 1
                        : tvSubject.getMaxLines() > 1)
                    tvSubject.setSingleLine(!"full".equals(subject_ellipsize));

                tvKeywords.setSingleLine(true);
            }

            tvPreview.setVisibility(
                    preview && message != null && !TextUtils.isEmpty(message.preview)
                            ? View.VISIBLE : View.GONE);

            if (vsBody == null)
                return;

            eowner.stop();
            cowner.stop();

            grpAddresses.setVisibility(View.GONE);
            grpHeaders.setVisibility(View.GONE);
            grpAction.setVisibility(View.GONE);
            grpCalendar.setVisibility(View.GONE);
            grpCalendarResponse.setVisibility(View.GONE);
            grpMedia.setVisibility(View.GONE);

            ivPlain.setVisibility(View.GONE);
            ibReceipt.setVisibility(View.GONE);
            ivAutoSubmitted.setVisibility(View.GONE);
            ivList.setVisibility(View.GONE);
            ivBrowsed.setVisibility(View.GONE);
            ivRaw.setVisibility(View.GONE);

            ibSearchContact.setVisibility(View.GONE);
            ibNotifyContact.setVisibility(View.GONE);
            ibPinContact.setVisibility(View.GONE);
            ibAddContact.setVisibility(View.GONE);

            tvSignedByTitle.setVisibility(View.GONE);
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

            tvSignedBy.setVisibility(View.GONE);
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

            rvAttachment.setVisibility(View.GONE);
            vSeparatorAttachments.setVisibility(View.GONE);
            ibExpanderAttachments.setVisibility(View.GONE);
            tvAttachments.setVisibility(View.GONE);
            cbInline.setVisibility(View.GONE);
            ibSaveAttachments.setVisibility(View.GONE);
            ibDownloadAttachments.setVisibility(View.GONE);
            tvNoInternetAttachments.setVisibility(View.GONE);

            vSeparator.setVisibility(View.GONE);
            ibFull.setVisibility(View.GONE);
            ibImages.setVisibility(View.GONE);
            ibAmp.setVisibility(View.GONE);
            ibDecrypt.setVisibility(View.GONE);
            ibVerify.setVisibility(View.GONE);
            ibUndo.setVisibility(View.GONE);
            ibAnswer.setVisibility(View.GONE);
            ibRule.setVisibility(View.GONE);
            ibUnsubscribe.setVisibility(View.GONE);
            ibRaw.setVisibility(View.GONE);
            ibHtml.setVisibility(View.GONE);
            ibHeaders.setVisibility(View.GONE);
            ibPrint.setVisibility(View.GONE);
            ibPin.setVisibility(View.GONE);
            ibShare.setVisibility(View.GONE);
            ibEvent.setVisibility(View.GONE);
            ibSearchText.setVisibility(View.GONE);
            ibSearch.setVisibility(View.GONE);
            ibTranslate.setVisibility(View.GONE);
            ibSummarize.setVisibility(View.GONE);
            ibFullScreen.setVisibility(View.GONE);
            ibForceLight.setVisibility(View.GONE);
            ibImportance.setVisibility(View.GONE);
            ibHide.setVisibility(View.GONE);
            ibSeen.setVisibility(View.GONE);
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
            vwEmpty.setVisibility(View.GONE);
            ibTools.setVisibility(View.GONE);
            clearButtons();
            tvReformatted.setVisibility(View.GONE);
            tvDecrypt.setVisibility(View.GONE);
            tvSignedData.setVisibility(View.GONE);

            tvNoInternetBody.setVisibility(View.GONE);
            grpDownloading.setVisibility(View.GONE);
            ibDownload.setVisibility(View.GONE);
            tvBody.setText(null);
            tvBody.setVisibility(View.GONE);
            vwRipple.setVisibility(View.GONE);
            // TO DO: clear web view?
            wvBody.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            clearActions();
            ibInfrastructure.setVisibility(View.GONE);
            ibTrashBottom.setVisibility(View.GONE);
            ibArchiveBottom.setVisibility(View.GONE);
            ibMoveBottom.setVisibility(View.GONE);
            ibSeenBottom.setVisibility(View.GONE);

            ibStoreMedia.setVisibility(View.GONE);
            ibShareImages.setVisibility(View.GONE);
        }

        private void clearButtons() {
            ConstraintLayout cl = (ConstraintLayout) buttons.getParent();
            for (int id : buttons.getReferencedIds()) {
                View v = cl.findViewById(id);
                cl.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            buttons.removeView(v);
                            cl.removeView(v);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
                // https://github.com/androidx/constraintlayout/issues/430
                // v.setVisibility(View.GONE);
            }
        }

        private void clearActions() {
            ConstraintLayout cl = (ConstraintLayout) flow.getParent();
            for (int id : flow.getReferencedIds()) {
                View v = cl.findViewById(id);
                cl.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            flow.removeView(v);
                            cl.removeView(v);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
                // https://github.com/androidx/constraintlayout/issues/430
                // v.setVisibility(View.GONE);
            }
        }

        private void clearCalendar() {
            ibCalendar.setVisibility(View.GONE);
            ibOnline.setVisibility(View.GONE);
            tvCalendarSummary.setVisibility(View.GONE);
            tvCalendarDescription.setVisibility(View.GONE);
            tvCalendarLocation.setVisibility(View.GONE);
            tvCalendarStart.setVisibility(View.GONE);
            tvCalendarEnd.setVisibility(View.GONE);
            tvAttendees.setVisibility(View.GONE);
            pbCalendarWait.setVisibility(View.GONE);
        }

        private void bindSeen(TupleMessageEx message) {
            if (cards && shadow_unread && shadow_border) {
                boolean shadow = (message.unseen > 0);
                int color = (shadow
                        ? ColorUtils.setAlphaComponent(shadow_highlight ? colorUnreadHighlight : colorAccent, 127)
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
                        ibAvatar.setLayoutParams(lparams);
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
            int mcolor = (message.color == null || !pro ? colorAccent : message.color);
            int tint = (flagged ? mcolor : textColorSecondary);

            if (!Objects.equals(ibFlagged.getTag(), flagged)) {
                ibFlagged.setTag(flagged);
                ibFlagged.setImageResource(flagged ? R.drawable.baseline_star_24 : R.drawable.twotone_star_border_24);
            }

            ColorStateList csl = ibFlagged.getImageTintList();
            if (csl == null || csl.getColorForState(new int[0], 0) != tint)
                ibFlagged.setImageTintList(ColorStateList.valueOf(tint));

            ibFlagged.setEnabled(message.uid != null || message.accountProtocol != EntityAccount.TYPE_IMAP);

            boolean split = (viewType != ViewType.THREAD && properties.getValue("split", message.id));

            int color = Color.TRANSPARENT;
            if (cards && shadow_unread && message.unseen > 0) {
                if (shadow_border)
                    color = ColorUtils.setAlphaComponent(colorCardBackground, 192);
                else {
                    int fg = (shadow_highlight ? colorUnreadHighlight : colorAccent);
                    color = ColorUtils.blendARGB(colorCardBackground, fg, 0.125f);
                }
            } else if (!cards && tabular_unread_bg && message.unseen > 0)
                color = ColorUtils.setAlphaComponent(colorSeparator, 127);
            else if (split)
                color = ColorUtils.setAlphaComponent(textColorHighlightInverse, 127);
            else if (flags_background && flagged && !expanded)
                color = ColorUtils.setAlphaComponent(mcolor, 127);

            card.setCardBackgroundColor(color);

            ibFlagged.setVisibility(flags || message.ui_flagged ? View.VISIBLE : View.GONE);
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
                        (Boolean.TRUE.equals(message.auth) ||
                                (Boolean.TRUE.equals(message.dkim) &&
                                        Boolean.TRUE.equals(message.spf) &&
                                        Boolean.TRUE.equals(message.dmarc)))) {
                    ibVerified.setImageLevel(main.isVerified() ? 1 : 0);
                    ibVerified.setImageTintList(ColorStateList.valueOf(main.isVerified()
                            ? colorVerified : colorControlNormal));
                    ibVerified.setContentDescription(context.getString(main.isVerified()
                            ? R.string.title_advanced_bimi_verified
                            : R.string.title_advanced_bimi_unverified));
                    if (main.isVerified() || BuildConfig.DEBUG) {
                        ibVerified.setVisibility(View.VISIBLE);

                        if (authentication && authentication_indicator)
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

            eowner.recreate();
            cowner.recreate();

            if (compact) {
                tvFrom.setSingleLine(false);
                tvSubject.setSingleLine(false);
                tvKeywords.setSingleLine(false);
            }

            tvPreview.setVisibility(View.GONE);

            ensureExpanded();

            bindAddresses(message);
            bindHeaders(message, false);

            List<EntityAttachment> attachments = (attachments_alt
                    ? new ArrayList<>() : properties.getAttachments(message.id));
            bindAttachments(message, attachments, false);

            // Actions
            vSeparator.setVisibility(View.VISIBLE);
            ibFull.setEnabled(false);
            ibFull.setVisibility(View.VISIBLE);
            ibImages.setVisibility(View.INVISIBLE);
            ibAmp.setVisibility(View.GONE);
            ibDecrypt.setVisibility(View.GONE);
            ibVerify.setVisibility(View.GONE);
            ibUndo.setVisibility(View.GONE);
            ibAnswer.setVisibility(View.GONE);
            ibRule.setVisibility(View.GONE);
            ibUnsubscribe.setVisibility(View.GONE);
            ibRaw.setVisibility(View.GONE);
            ibHtml.setVisibility(View.GONE);
            ibHeaders.setVisibility(View.GONE);
            ibPrint.setVisibility(View.GONE);
            ibPin.setVisibility(View.GONE);
            ibShare.setVisibility(View.GONE);
            ibEvent.setVisibility(View.GONE);
            ibSearchText.setVisibility(View.GONE);
            ibSearch.setVisibility(View.GONE);
            ibTranslate.setVisibility(View.GONE);
            ibSummarize.setVisibility(View.GONE);
            ibFullScreen.setVisibility(View.GONE);
            ibForceLight.setVisibility(View.GONE);
            ibImportance.setVisibility(View.GONE);
            ibHide.setVisibility(View.GONE);
            ibSeen.setVisibility(View.GONE);
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
            vwEmpty.setVisibility(View.GONE);
            ibTools.setVisibility(View.GONE);
            clearButtons();
            tvReformatted.setVisibility(View.GONE);
            tvDecrypt.setVisibility(View.GONE);
            tvSignedData.setVisibility(View.GONE);

            // Message text
            boolean content = (message.content || message.error != null);
            tvNoInternetBody.setVisibility(suitable || content ? View.GONE : View.VISIBLE);

            grpDownloading.setVisibility(View.GONE);
            ibDownload.setVisibility(View.GONE);

            eowner.start();
            db.operation().liveOperations(message.id, EntityOperation.BODY).observe(eowner, new Observer<TupleMessageOperation>() {
                @Override
                public void onChanged(TupleMessageOperation operation) {
                    grpDownloading.setVisibility(operation != null && !operation.content &&
                            (operation.id != null || operation.uid == null) ? View.VISIBLE : View.GONE);
                    ibDownload.setVisibility(operation != null &&
                            operation.id == null && operation.uid != null && !operation.content ? View.VISIBLE : View.GONE);
                }
            });

            boolean show_full = properties.getValue("full", message.id);
            if (show_full)
                wvBody.setVisibility(View.GONE);
            else {
                int height = properties.getHeight(message.id, 0);
                if (height == 0)
                    tvBody.setVisibility(View.GONE);
                else {
                    tvBody.setVisibility(View.INVISIBLE);
                    tvBody.setMinHeight(height);
                }
            }

            vwRipple.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);

            clearCalendar();
            grpCalendar.setVisibility(View.GONE);
            grpCalendarResponse.setVisibility(View.GONE);

            grpAction.setVisibility(View.GONE);
            clearActions();
            ibInfrastructure.setVisibility(View.GONE);
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
                                else {
                                    String mimeType = attachment.getMimeType();
                                    if ("text/x-amp-html".equals(mimeType) ||
                                            "message/rfc822".equals(mimeType))
                                        embeddedMessages++;
                                }

                    int lastInlineImages = 0;
                    int lastEmbeddedMessages = 0;
                    List<EntityAttachment> lastAttachments = properties.getAttachments(message.id);
                    if (lastAttachments != null)
                        for (EntityAttachment attachment : lastAttachments)
                            if (attachment.available)
                                if (attachment.isInline() && attachment.isImage())
                                    lastInlineImages++;
                                else {
                                    String mimeType = attachment.getMimeType();
                                    if ("text/x-amp-html".equals(mimeType) ||
                                            "message/rfc822".equals(mimeType))
                                        lastEmbeddedMessages++;
                                }

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
                    if (data.folders != null)
                        for (EntityFolder folder : data.folders)
                            if (folder.selectable)
                                if (EntityFolder.INBOX.equals(folder.type))
                                    hasInbox = true;
                                else if (EntityFolder.ARCHIVE.equals(folder.type))
                                    hasArchive = true;
                                else if (EntityFolder.TRASH.equals(folder.type))
                                    hasTrash = true;

                    boolean pop = (message.accountProtocol == EntityAccount.TYPE_POP);
                    boolean imap = (message.accountProtocol == EntityAccount.TYPE_IMAP);

                    boolean inInbox = EntityFolder.INBOX.equals(message.folderType);
                    boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                    boolean inSent = EntityFolder.SENT.equals(message.folderType);
                    boolean inTrash = EntityFolder.TRASH.equals(message.folderType);
                    boolean inJunk = EntityFolder.JUNK.equals(message.folderType);
                    boolean outbox = EntityFolder.OUTBOX.equals(message.folderType);

                    boolean move = !(message.folderReadOnly || message.uid == null);
                    boolean archive = (move && (hasArchive && !inArchive && !inSent && !inTrash && !inJunk));
                    boolean trash = (move || outbox || debug || pop);
                    boolean inbox = (move && hasInbox && (inArchive || inTrash || inJunk) && imap) ||
                            (pop && message.accountLeaveDeleted && inTrash);
                    boolean keywords = (message.uid != null && imap);
                    boolean labels = (data.isGmail && move && !inTrash && !inJunk && !outbox);
                    boolean seen = (message.uid != null || pop);

                    int froms = (message.from == null ||
                            message.from.length == 0 ||
                            TextUtils.isEmpty(((InternetAddress) message.from[0]).getAddress())
                            ? 0 : message.from.length);
                    int tos = (message.to == null ||
                            message.to.length == 0 ||
                            TextUtils.isEmpty(((InternetAddress) message.to[0]).getAddress())
                            ? 0 : message.to.length);

                    boolean delete = (inTrash || !hasTrash || inJunk || outbox || message.uid == null || pop);
                    boolean forever = (delete && (!pop || !message.accountLeaveDeleted));
                    boolean report = (pop ? inInbox : !inJunk && move && !inSent);
                    boolean headers = (message.uid != null || (pop && message.headers != null));
                    boolean raw = (message.uid != null ||
                            (EntityFolder.INBOX.equals(message.folderType) &&
                                    message.accountProtocol == EntityAccount.TYPE_POP));

                    evalProperties(message); // TODO: done again in bindBody

                    boolean full = properties.getValue("full", message.id);
                    boolean dark = Helper.isDarkTheme(context);
                    boolean force_light = properties.getValue("force_light", message.id);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean expand_all = prefs.getBoolean("expand_all", false);
                    boolean expand_one = prefs.getBoolean("expand_one", true);
                    boolean swipe_reply = prefs.getBoolean("swipe_reply", false);
                    boolean tools = prefs.getBoolean("message_tools", true);
                    boolean button_junk = prefs.getBoolean("button_junk", true);
                    boolean button_trash = prefs.getBoolean("button_trash", true);
                    boolean button_archive = prefs.getBoolean("button_archive", true);
                    boolean button_move = prefs.getBoolean("button_move", true);
                    boolean button_copy = prefs.getBoolean("button_copy", false);
                    boolean button_keywords = prefs.getBoolean("button_keywords", false);
                    boolean button_labels = prefs.getBoolean("button_labels", true);
                    boolean button_notes = prefs.getBoolean("button_notes", false);
                    boolean button_seen = prefs.getBoolean("button_seen", false);
                    boolean button_hide = prefs.getBoolean("button_hide", false);
                    boolean button_importance = prefs.getBoolean("button_importance", false);
                    boolean button_translate = prefs.getBoolean("button_translate", true);
                    boolean button_summarize = prefs.getBoolean("button_summarize", false);
                    boolean button_full_screen = prefs.getBoolean("button_full_screen", false);
                    boolean button_force_light = prefs.getBoolean("button_force_light", true);
                    boolean button_search = prefs.getBoolean("button_search", false);
                    boolean button_search_text = prefs.getBoolean("button_search_text", false);
                    boolean button_event = prefs.getBoolean("button_event", false);
                    boolean button_share = prefs.getBoolean("button_share", false);
                    boolean button_pin = prefs.getBoolean("button_pin", false);
                    boolean button_print = prefs.getBoolean("button_print", false);
                    boolean button_headers = prefs.getBoolean("button_headers", false);
                    boolean button_html = prefs.getBoolean("button_html", false);
                    boolean button_raw = prefs.getBoolean("button_raw", false);
                    boolean button_unsubscribe = prefs.getBoolean("button_unsubscribe", true);
                    boolean button_rule = prefs.getBoolean("button_rule", false);
                    boolean button_answer = prefs.getBoolean("button_answer", false);

                    int importance = (((message.ui_importance == null ? 1 : message.ui_importance) + 1) % 3);

                    ibImportance.setImageLevel(importance);
                    ibHide.setImageResource(message.ui_snoozed == null ? R.drawable.twotone_visibility_off_24 : R.drawable.twotone_visibility_24);
                    ibSeen.setImageResource(message.ui_seen ? R.drawable.twotone_mail_24 : R.drawable.twotone_drafts_24);
                    ibTrash.setTag(delete);
                    ibTrash.setImageResource(forever ? R.drawable.twotone_delete_forever_24 : R.drawable.twotone_delete_24);
                    ibTrash.setImageTintList(ColorStateList.valueOf(outbox ? colorWarning : colorControlNormal));
                    ibTrashBottom.setImageResource(forever ? R.drawable.twotone_delete_forever_24 : R.drawable.twotone_delete_24);
                    ibInbox.setImageResource(inJunk ? R.drawable.twotone_report_off_24 : R.drawable.twotone_inbox_24);

                    ibUndo.setVisibility(outbox ? View.VISIBLE : View.GONE);
                    ibAnswer.setVisibility(!tools || outbox || (!expand_all && expand_one && !button_answer) || (!threading && !button_answer) || (swipe_reply && !button_answer) ? View.GONE : View.VISIBLE);
                    ibRule.setVisibility(tools && button_rule && !outbox && !message.folderReadOnly ? View.VISIBLE : View.GONE);
                    ibUnsubscribe.setVisibility(tools && button_unsubscribe && message.unsubscribe != null ? View.VISIBLE : View.GONE);
                    ibRaw.setVisibility(tools && button_raw && raw ? View.VISIBLE : View.GONE);
                    ibHtml.setVisibility(tools && hasWebView && button_html && message.content ? View.VISIBLE : View.GONE);
                    ibHeaders.setVisibility(tools && button_headers && headers ? View.VISIBLE : View.GONE);
                    ibPrint.setVisibility(tools && !outbox && button_print && hasWebView && message.content && Helper.canPrint(context) ? View.VISIBLE : View.GONE);
                    ibPin.setVisibility(tools && !outbox && button_pin && pin ? View.VISIBLE : View.GONE);
                    ibShare.setVisibility(tools && !outbox && button_share && message.content ? View.VISIBLE : View.GONE);
                    ibEvent.setVisibility(tools && !outbox && button_event && message.content ? View.VISIBLE : View.GONE);
                    ibSearchText.setVisibility(tools && !outbox && button_search_text && message.content ? View.VISIBLE : View.GONE);
                    ibSearch.setVisibility(tools && !outbox && button_search && (froms > 0 || tos > 0) ? View.VISIBLE : View.GONE);
                    ibTranslate.setVisibility(tools && !outbox && button_translate && DeepL.isAvailable(context) && message.content ? View.VISIBLE : View.GONE);
                    ibSummarize.setVisibility(tools && !outbox && button_summarize && AI.isAvailable(context) && message.content ? View.VISIBLE : View.GONE);
                    ibFullScreen.setVisibility(tools && full && button_full_screen && message.content ? View.VISIBLE : View.GONE);
                    ibForceLight.setVisibility(tools && full && dark && button_force_light && message.content ? View.VISIBLE : View.GONE);
                    ibForceLight.setImageLevel(!(canDarken || fake_dark) || force_light ? 1 : 0);
                    ibImportance.setVisibility(tools && button_importance && !outbox && seen ? View.VISIBLE : View.GONE);
                    ibHide.setVisibility(tools && button_hide && !outbox ? View.VISIBLE : View.GONE);
                    ibSeen.setVisibility(tools && button_seen && !outbox && seen ? View.VISIBLE : View.GONE);
                    ibNotes.setVisibility(tools && button_notes && !outbox ? View.VISIBLE : View.GONE);
                    ibLabels.setVisibility(tools && labels_header && labels && button_labels ? View.VISIBLE : View.GONE);
                    ibKeywords.setVisibility(tools && button_keywords && keywords ? View.VISIBLE : View.GONE);
                    ibCopy.setVisibility(tools && button_copy && move ? View.VISIBLE : View.GONE);
                    ibMove.setVisibility(tools && button_move && move ? View.VISIBLE : View.GONE);
                    ibArchive.setVisibility(tools && button_archive && archive ? View.VISIBLE : View.GONE);
                    ibTrash.setVisibility(outbox || (tools && button_trash && trash) ? View.VISIBLE : View.GONE);
                    ibJunk.setVisibility(tools && button_junk && report ? View.VISIBLE : View.GONE);
                    ibInbox.setVisibility(tools && inbox ? View.VISIBLE : View.GONE);
                    ibMore.setVisibility(tools && !outbox ? View.VISIBLE : View.GONE);
                    vwEmpty.setVisibility(outbox ? View.GONE : View.VISIBLE);
                    ibTools.setImageLevel(tools ? 0 : 1);
                    ibTools.setContentDescription(context.getString(tools ? R.string.title_less : R.string.title_more));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        ibTools.setTooltipText(ibTools.getContentDescription());
                    ibTools.setVisibility(outbox ? View.GONE : View.VISIBLE);

                    if (tools)
                        bindButtons(message);
                    else
                        clearButtons();

                    if (bind)
                        bindBody(message, scroll);
                    else
                        bindExtras(message);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.setLog(false).execute(context, owner, sargs, "message:tools");
        }

        private void bindButtons(TupleMessageEx message) {
            String keywords = prefs.getString("global_keywords", null);
            if (keywords == null)
                return;

            if (buttons.getReferencedIds().length > 0)
                return;

            int dp3 = Helper.dp2pixels(context, 3);
            Drawable on = ContextCompat.getDrawable(context, R.drawable.twotone_check_12);
            Drawable off = ContextCompat.getDrawable(context, R.drawable.twotone_close_12);
            on.setBounds(0, 0, on.getIntrinsicWidth(), on.getIntrinsicHeight());
            off.setBounds(0, 0, off.getIntrinsicWidth(), off.getIntrinsicHeight());

            List<String> selected = Arrays.asList(message.keywords);
            for (String keyword : keywords.split(" ")) {
                boolean set = selected.contains(keyword);
                String title = prefs.getString("kwtitle." + keyword, keyword);
                String c = "kwcolor." + keyword;
                Integer color = (prefs.contains(c) ? prefs.getInt(c, Color.GRAY) : null);

                Button button = new Button(context, null, android.R.attr.buttonStyleSmall);
                button.setId(View.generateViewId());
                button.setText(title);
                button.setCompoundDrawablePadding(dp3);
                button.setCompoundDrawablesRelative(null, null, set ? off : on, null);
                if (color != null)
                    button.setBackgroundTintList(ColorStateList.valueOf(color));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle args = new Bundle();
                        args.putLong("id", message.id);
                        args.putString("keyword", keyword);
                        args.putBoolean("set", !set);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) throws Throwable {
                                long id = args.getLong("id");
                                String keyword = args.getString("keyword");
                                boolean set = args.getBoolean("set");

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(id);
                                    if (message == null)
                                        return null;

                                    EntityOperation.queue(context, message, EntityOperation.KEYWORD, keyword, set);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {

                            }
                        }.execute(context, owner, args, "toggle:keyword");
                    }
                });

                ((ConstraintLayout) buttons.getParent()).addView(button);
                buttons.addView(button);
            }
        }

        private String formatAddresses(Address[] addresses, MessageHelper.AddressFormat format, int max) {
            List<Address> list = new ArrayList<>();
            if (addresses != null)
                for (Address address : addresses)
                    if (!list.contains(address))
                        list.add(address);

            Address[] sub = list.subList(0, Math.min(list.size(), max)).toArray(new Address[0]);
            String result = MessageHelper.formatAddresses(sub, format, false);
            if (list.size() > sub.length)
                result = context.getString(R.string.title_name_plus, result, list.size() - sub.length);

            return result;
        }

        private Spanned formatAddresses(Address[] addresses, boolean full) {
            return formatAddresses(addresses, full, Integer.MAX_VALUE);
        }

        private Spanned formatAddresses(Address[] addresses, boolean full, int max) {
            SpannableStringBuilder ssb = new SpannableStringBuilderEx();

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
                        boolean homoPersonal = TextHelper.isSingleScript(personal);
                        if (debug && !homoPersonal)
                            personal = TextHelper.getNonLatinCodepoints(personal);
                        ssb.append(personal);
                        if (!homoPersonal) {
                            int start = ssb.length() - personal.length();
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                            ssb.setSpan(new ForegroundColorSpan(colorError), start, ssb.length(), 0);
                        }

                        Matcher m = Helper.EMAIL_ADDRESS.matcher(personal);
                        while (m.find())
                            try {
                                String e = personal.substring(m.start(), m.end());
                                if (!e.equalsIgnoreCase(email)) {
                                    int start = ssb.length() - personal.length();
                                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start + m.start(), start + m.end(), 0);
                                    ssb.setSpan(new ForegroundColorSpan(colorError), start + m.start(), start + m.end(), 0);
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                                break;
                            }

                        if (full) {
                            ssb.append(" <");
                            if (!TextUtils.isEmpty(email)) {
                                int start = ssb.length();
                                ssb.append(email);
                                ssb.setSpan(new ForegroundColorSpan(textColorLink), start, ssb.length(), 0);
                            }
                            ssb.append(">");
                        }
                    }
                } else
                    ssb.append(addresses[i].toString());
            }

            if (addresses.length > max)
                ssb.append(context.getString(R.string.title_name_plus, "", addresses.length - max));

            return ssb;
        }

        private void bindAddresses(TupleMessageEx message) {
            boolean show_addresses = properties.getValue("addresses", message.id, getShowAddressesDefault(message));
            boolean full = (show_addresses || email_format == MessageHelper.AddressFormat.NAME_EMAIL);

            int froms = (message.from == null ||
                    message.from.length == 0 ||
                    TextUtils.isEmpty(((InternetAddress) message.from[0]).getAddress())
                    ? 0 : message.from.length);
            int tos = (message.to == null ||
                    message.to.length == 0 ||
                    TextUtils.isEmpty(((InternetAddress) message.to[0]).getAddress())
                    ? 0 : message.to.length);
            boolean hasChannel = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
            int maxRecipients = (compact ? MAX_RECIPIENTS_COMPACT : MAX_RECIPIENTS_NORMAL);
            Spanned submitter = formatAddresses(message.submitter, true);
            Spanned from = formatAddresses(message.senders, true);

            grpAddresses.setVisibility(View.VISIBLE);

            ibExpanderAddress.setImageLevel(show_addresses ? 0 /* less */ : 1 /* more */);
            ibExpanderAddress.setContentDescription(context.getString(show_addresses ? R.string.title_accessibility_hide_addresses : R.string.title_accessibility_show_addresses));

            ivPlain.setVisibility(show_addresses && message.isPlainOnly() ? View.VISIBLE : View.GONE);
            ibReceipt.setVisibility(message.receipt_request != null && message.receipt_request ? View.VISIBLE : View.GONE);
            ibReceipt.setImageTintList(ColorStateList.valueOf(message.ui_answered ? colorControlNormal : colorError));
            ivAutoSubmitted.setVisibility(show_addresses && message.auto_submitted != null && message.auto_submitted ? View.VISIBLE : View.GONE);
            ivList.setVisibility(message.list_post != null && message.list_post.length > 0 ? View.VISIBLE : View.GONE);
            ivBrowsed.setVisibility(show_addresses && message.ui_browsed ? View.VISIBLE : View.GONE);
            ivRaw.setVisibility(BuildConfig.DEBUG && Boolean.TRUE.equals(message.raw) ? View.VISIBLE : View.GONE);

            boolean button_search = prefs.getBoolean("button_search", false);
            ibSearchContact.setVisibility(show_addresses && (froms > 0 || tos > 0) && !button_search ? View.VISIBLE : View.GONE);
            ibNotifyContact.setVisibility(show_addresses && hasChannel && froms > 0 ? View.VISIBLE : View.GONE);
            ibPinContact.setVisibility(show_addresses && pin && contacts && froms > 0 ? View.VISIBLE : View.GONE);
            ibAddContact.setVisibility(show_addresses && contacts && froms > 0 ? View.VISIBLE : View.GONE);

            boolean known_signer = false;
            SpannableStringBuilder signers = new SpannableStringBuilderEx();
            if (native_dkim && message.signedby != null) {
                // https://dmarcly.com/blog/what-is-dmarc-identifier-alignment-domain-alignment
                List<Address> envelop = new ArrayList<>();
                if (message.return_path != null)
                    envelop.addAll(Arrays.asList(message.return_path));
                if (message.from != null)
                    envelop.addAll(Arrays.asList(message.from));
                for (String signer : message.signedby.split(",")) {
                    if (signers.length() > 0)
                        signers.append(", ");
                    int start = signers.length();
                    signers.append(signer);
                    for (Address a : envelop) {
                        String domain = UriHelper.getEmailDomain(((InternetAddress) a).getAddress());
                        if (signer != null && domain != null && Objects.equals(
                                UriHelper.getRootDomain(context, signer.toLowerCase(Locale.ROOT)),
                                UriHelper.getRootDomain(context, domain.toLowerCase(Locale.ROOT)))) {
                            signers.setSpan(new ForegroundColorSpan(textColorLink), start, signers.length(), 0);
                            known_signer = true;
                            break;
                        }
                    }
                }
            }
            boolean show_signers = (native_dkim &&
                    message.signedby != null &&
                    (show_addresses || !known_signer));

            tvSignedByTitle.setVisibility(show_signers ? View.VISIBLE : View.GONE);
            tvSignedByTitle.setTextColor(known_signer ? textColorTertiary : colorWarning);
            tvSignedBy.setVisibility(show_signers ? View.VISIBLE : View.GONE);
            tvSignedBy.setText(signers);

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

            boolean show_reply = (Boolean.FALSE.equals(message.reply_domain) || show_addresses) &&
                    (message.reply != null && message.reply.length > 0);
            tvReplyToTitle.setVisibility(show_reply ? View.VISIBLE : View.GONE);
            tvReplyTo.setVisibility(show_reply ? View.VISIBLE : View.GONE);
            tvReplyTo.setText(formatAddresses(message.reply, show_addresses));

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

            tvIdentityTitle.setTextColor(message.identityColor == null ? textColorTertiary : message.identityColor);
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
            boolean homoSubject = TextHelper.isSingleScript(message.subject);
            tvSubjectEx.setTextColor(homoSubject ? textColorTertiary : colorError);
            tvSubjectEx.setTypeface(Typeface.DEFAULT,
                    (subject_italic ? Typeface.ITALIC : Typeface.NORMAL) |
                            (homoSubject ? Typeface.NORMAL : Typeface.BOLD));
            // Flags
            tvFlags.setVisibility(show_addresses && debug ? View.VISIBLE : View.GONE);
            tvFlags.setText(message.flags);

            // Keywords
            if (keywords_header) {
                tvKeywordsEx.setVisibility(show_addresses && message.keywords.length > 0 ? View.VISIBLE : View.GONE);
                tvKeywordsEx.setText(TextUtils.join(" ", message.keywords));
            } else {
                Spanned keywords = getKeywords(message);
                tvKeywordsEx.setVisibility(!show_addresses || keywords == null ? View.GONE : View.VISIBLE);
                tvKeywordsEx.setText(keywords);
            }

            view.post(new RunnableEx("setTextIsSelectable") {
                @Override
                public void delegate() {
                    if (!owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        return;

                    tvSignedBy.setTextIsSelectable(false);
                    tvSubmitter.setTextIsSelectable(false);
                    tvDeliveredTo.setTextIsSelectable(false);
                    tvFromEx.setTextIsSelectable(false);
                    tvTo.setTextIsSelectable(false);
                    tvReplyTo.setTextIsSelectable(false);
                    tvCc.setTextIsSelectable(false);
                    tvBcc.setTextIsSelectable(false);
                    tvIdentity.setTextIsSelectable(false);
                    tvSent.setTextIsSelectable(false);
                    tvReceived.setTextIsSelectable(false);
                    tvStored.setTextIsSelectable(false);
                    tvSizeEx.setTextIsSelectable(false);
                    tvLanguage.setTextIsSelectable(false);
                    tvSubject.setTextIsSelectable(false);
                    tvFlags.setTextIsSelectable(false);
                    tvKeywordsEx.setTextIsSelectable(false);

                    tvSignedBy.setTextIsSelectable(tvSignedBy.getVisibility() == View.VISIBLE);
                    tvSubmitter.setTextIsSelectable(tvSubmitter.getVisibility() == View.VISIBLE);
                    tvDeliveredTo.setTextIsSelectable(tvDeliveredTo.getVisibility() == View.VISIBLE);
                    tvFromEx.setTextIsSelectable(tvFromEx.getVisibility() == View.VISIBLE);
                    tvTo.setTextIsSelectable(tvTo.getVisibility() == View.VISIBLE);
                    tvReplyTo.setTextIsSelectable(tvReplyTo.getVisibility() == View.VISIBLE);
                    tvCc.setTextIsSelectable(tvCc.getVisibility() == View.VISIBLE);
                    tvBcc.setTextIsSelectable(tvBcc.getVisibility() == View.VISIBLE);
                    tvIdentity.setTextIsSelectable(tvIdentity.getVisibility() == View.VISIBLE);
                    tvSent.setTextIsSelectable(tvSent.getVisibility() == View.VISIBLE);
                    tvReceived.setTextIsSelectable(tvReceived.getVisibility() == View.VISIBLE);
                    tvStored.setTextIsSelectable(tvStored.getVisibility() == View.VISIBLE);
                    tvSizeEx.setTextIsSelectable(tvSizeEx.getVisibility() == View.VISIBLE);
                    tvLanguage.setTextIsSelectable(tvLanguage.getVisibility() == View.VISIBLE);
                    tvSubjectEx.setTextIsSelectable(tvSubjectEx.getVisibility() == View.VISIBLE);
                    tvFlags.setTextIsSelectable(tvFlags.getVisibility() == View.VISIBLE);
                    tvKeywordsEx.setTextIsSelectable(tvKeywordsEx.getVisibility() == View.VISIBLE && keywords_header);
                }
            });
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
                SpannableStringBuilder ssb = HtmlHelper.highlightHeaders(context,
                        message.from, message.to, message.received, message.headers,
                        message.blocklist != null && message.blocklist, true);
                if (BuildConfig.DEBUG) {
                    float stroke = context.getResources().getDisplayMetrics().density;

                    ssb.append("\n\uFFFC"); // Object replacement character
                    ssb.setSpan(new LineSpan(colorSeparator, stroke, 0), ssb.length() - 1, ssb.length(), 0);
                    ssb.append('\n');

                    ssb.append('\n');
                    ssb.append("TLS=").append(message.tls == null ? "-" : (message.tls ? "✓" : "✗"));
                    ssb.append(" DKIM=").append(message.dkim == null ? "-" : (message.dkim ? "✓" : "✗"));
                    ssb.append(" SPF=").append(message.spf == null ? "-" : (message.spf ? "✓" : "✗"));
                    ssb.append(" DMARC=").append(message.dmarc == null ? "-" : (message.dmarc ? "✓" : "✗"));
                    if (message.auth != null)
                        ssb.append(" SMTP=").append(message.auth ? "✓" : "✗");
                    ssb.append(" BL=").append(message.blocklist == null ? "-" : (message.blocklist ? "✓" : "✗"));
                    ssb.append('\n');
                }

                tvHeaders.setText(ssb);
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

        private void evalProperties(TupleMessageEx message) {
            if (message.show_full) {
                properties.setValue("full", message.id, hasWebView);
                properties.setValue("full_asked", message.id, hasWebView);
            }
            if (message.show_images) {
                properties.setValue("images", message.id, true);
                properties.setValue("images_asked", message.id, true);
            }

            Address[] senders = (message.isForwarder() ? message.submitter : message.from);

            if (senders != null)
                for (Address sender : senders) {
                    String from = ((InternetAddress) sender).getAddress();
                    if (TextUtils.isEmpty(from))
                        continue;
                    int at = from.indexOf('@');
                    String domain = (at < 0 ? from : from.substring(at));
                    if (prefs.getBoolean(from + ".show_full", false) ||
                            prefs.getBoolean(domain + ".show_full", false)) {
                        properties.setValue("full", message.id, hasWebView);
                        properties.setValue("full_asked", message.id, hasWebView);
                    }
                    if (prefs.getBoolean(from + ".show_images", false) ||
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
                properties.setValue("full", message.id, hasWebView);
                properties.setValue("full_asked", message.id, hasWebView);
            }

            if (!properties.getValue("force_light_default", message.id)) {
                boolean default_light = prefs.getBoolean("default_light", false);
                properties.setValue("force_light", message.id, default_light);
                properties.setValue("force_light_default", message.id, true);
            }
        }

        private void bindBody(TupleMessageEx message, final boolean scroll) {
            if (!Objects.equals(tvBody.getTag(), message.id)) {
                tvBody.setTag(message.id);
                tvBody.setText(null);
            }
            properties.endSearch();
            clearActions();

            if (!message.content) {
                bindExtras(message);
                if (scroll)
                    properties.scrollTo(getAdapterPosition(), 0);
                return;
            }

            grpDownloading.setVisibility(View.GONE);
            ibDownload.setVisibility(View.GONE);

            evalProperties(message);

            boolean show_full = properties.getValue("full", message.id);
            boolean show_images = properties.getValue("images", message.id);
            boolean show_quotes = properties.getValue("quotes", message.id);

            boolean dark = Helper.isDarkTheme(context);
            boolean force_light = properties.getValue("force_light", message.id);
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
            ibFull.setImageTintList(ColorStateList.valueOf(hasWebView ? colorAccent : colorSeparator));
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

            if (message.isEncrypted() && !message.isUnlocked()) {
                tvBody.setVisibility(View.GONE);
                wvBody.setVisibility(View.GONE);
            } else {
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

                    webView.setMinimumHeight(dp60);

                    int maxHeight = (rv == null ? 0 : rv.getHeight() - rv.getPaddingTop());
                    webView.init(height, maxHeight, size, position, force_light,
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
                                public void onScrollChange(int dx, int dy, int scrollX, int scrollY) {
                                    properties.setPosition(message.id, new Pair<>(dx, dy), new Pair<>(scrollX, scrollY));
                                }

                                @Override
                                public boolean onOpenLink(String url, boolean always) {
                                    if (parentFragment == null)
                                        return false;

                                    Uri uri = Uri.parse(url);
                                    return ViewHolder.this.onOpenLink(uri, null,
                                            always || EntityFolder.JUNK.equals(message.folderType));
                                }

                                @Override
                                public void onUserInterAction() {
                                    try {
                                        Log.i("WebView user interaction");
                                        parentFragment.getActivity().onUserInteraction();
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
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
                    tvBody.setTypeface(StyleHelper.getTypeface(display_font, context));

                    tvBody.setVisibility(View.VISIBLE);
                    wvBody.setVisibility(View.GONE);
                }
            }

            final Bundle args = new Bundle();
            args.putSerializable("message", message);
            args.putBoolean("show_full", show_full);
            args.putBoolean("show_images", show_images);
            args.putBoolean("show_quotes", show_quotes);
            args.putBoolean("collapse_quotes", collapse_quotes);
            args.putInt("zoom", zoom);

            float scale = (size == 0 || textSize == 0 ? 1.0f : size / (textSize * message_zoom / 100f));
            args.putFloat("scale", scale);

            args.putBoolean("fake_dark", !canDarken && fake_dark && dark && !force_light);

            Log.breadcrumb("message:body", args);

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
                    final boolean collapse_quotes = args.getBoolean("collapse_quotes");
                    final int zoom = args.getInt("zoom");
                    final float scale = args.getFloat("scale");
                    final boolean download_plain = prefs.getBoolean("download_plain", false);
                    final boolean json_ld = prefs.getBoolean("json_ld", false);

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
                    if (!file.exists()) {
                        try {
                            db.beginTransaction();

                            db.message().resetMessageContent(message.id);
                            EntityOperation.queue(context, message, EntityOperation.BODY);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return null;
                    }

                    if (file.length() > 0)
                        signed_data = false;
                    args.putBoolean("signed_data", signed_data);

                    Document document = JsoupEx.parse(file);
                    HtmlHelper.cleanup(document);

                    // Add embedded messages
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available && "message/rfc822".equals(attachment.getMimeType()))
                            try (FileInputStream fis = new FileInputStream(attachment.getFile(context))) {
                                Properties props = MessageHelper.getSessionProperties(true);
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

                                String html = parts.getHtml(context, download_plain);
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

                    HtmlHelper.removeRelativeLinks(document);

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

                    // Check for AMP
                    boolean has_amp = false;
                    for (EntityAttachment attachment : attachments)
                        if ("text/x-amp-html".equals(attachment.type)) {
                            has_amp = attachment.available;
                            break;
                        }
                    args.putBoolean("has_amp", has_amp);

                    // Check for structured email
                    if (json_ld)
                        for (Element struct : document.select("script[type=application/ld+json]"))
                            document.body().append(new JsonLd(struct.html()).getHtml(context));

                    // Format message
                    if (show_full) {
                        if (HtmlHelper.truncate(document, HtmlHelper.MAX_FULL_TEXT_SIZE)) {
                            document.body()
                                    .appendElement("p")
                                    .appendElement("em")
                                    .text(context.getString(R.string.title_truncated));
                            document.body()
                                    .appendElement("p")
                                    .appendElement("big")
                                    .appendElement("a")
                                    .attr("href", "external:")
                                    .text(context.getString(R.string.title_show_full));
                        }

                        if (message.isPlainOnly()) {
                            document.select("body")
                                    .attr("style", "margin:0; padding:0;");
                            boolean monospaced_pre = prefs.getBoolean("monospaced_pre", false);
                            if (monospaced_pre)
                                HtmlHelper.restorePre(document);
                        }

                        boolean fake_dark = args.getBoolean("fake_dark");
                        if (fake_dark)
                            HtmlHelper.fakeDark(document);

                        boolean browser_zoom = prefs.getBoolean("browser_zoom", false);
                        int message_zoom = prefs.getInt("message_zoom", 100);
                        if (browser_zoom && message_zoom != 100) {
                            String z = String.format("%.2f", message_zoom / 100f);
                            document.select("body").attr("style",
                                    "zoom: " + z + ";" +
                                            "-moz-transform: scale(" + z + ");" + // Firefox
                                            "-moz-transform-origin: 0 0;" +
                                            "-o-transform: scale(" + z + ");" + // Opera
                                            "-o-transform-origin: 0 0;" +
                                            "-webkit-transform: scale(" + z + ");" + // Safari
                                            "-webkit-transform-origin: 0 0;" +
                                            "transform: scale(" + z + ");" + // Standard
                                            "transform-origin: 0 0;");
                        }

                        HtmlHelper.guessSchemes(document);
                        HtmlHelper.autoLink(document);

                        if (message.ui_found && found && !TextUtils.isEmpty(searched))
                            HtmlHelper.highlightSearched(context, document, searched, searchedPartial);

                        boolean overview_mode = prefs.getBoolean("overview_mode", false);
                        HtmlHelper.setViewport(document, overview_mode);
                        if (inline || show_images)
                            HtmlHelper.embedInlineImages(context, message.id, document, show_images);

                        HtmlHelper.markText(document);

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
                        HtmlHelper.autoLink(document);

                        if (message.ui_found && found && !TextUtils.isEmpty(searched))
                            HtmlHelper.highlightSearched(context, document, searched, searchedPartial);

                        // Cleanup message
                        document = HtmlHelper.sanitizeView(context, document, show_images);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            args.putParcelable("actions", getConversationActions(message, document, context));

                        // Draw images
                        SpannableStringBuilder ssb = HtmlHelper.fromDocument(context, document, new HtmlHelper.ImageGetterEx() {
                            @Override
                            public Drawable getDrawable(Element element) {
                                return ImageHelper.decodeImage(context,
                                        message.id, element, show_images, zoom, scale, tvBody);
                            }
                        }, null);

                        if (!collapse_quotes)
                            return ssb;

                        // Replace quote spans
                        QuoteSpan[] quoteSpans = ssb.getSpans(0, ssb.length(), QuoteSpan.class);
                        if (quoteSpans == null || quoteSpans.length == 0)
                            return ssb;

                        List<QuoteSpan> lqs = new ArrayList<>();
                        for (QuoteSpan quoteSpan : quoteSpans) {
                            int s = ssb.getSpanStart(quoteSpan);
                            int e = ssb.getSpanEnd(quoteSpan);

                            boolean enclosed = false;
                            for (QuoteSpan eqs : quoteSpans) {
                                if (eqs == quoteSpan)
                                    continue;

                                int es = ssb.getSpanStart(eqs);
                                int ee = ssb.getSpanEnd(eqs);
                                if (es <= s && ee >= e) {
                                    enclosed = true;
                                    break;
                                }
                            }

                            if (!enclosed)
                                lqs.add(quoteSpan);
                        }

                        final int px = Helper.dp2pixels(context, 24 + (zoom) * 8);
                        final Drawable d = ContextCompat.getDrawable(context, show_quotes
                                ? R.drawable.outline_unfold_less_24
                                : R.drawable.twotone_format_quote_24);
                        d.setTint(colorAccent);
                        d.setBounds(0, 0, px, px);

                        for (QuoteSpan quoteSpan : lqs) {
                            int s = ssb.getSpanStart(quoteSpan);
                            int e = ssb.getSpanEnd(quoteSpan);
                            if (s == 0 || s == e)
                                continue;

                            if (show_quotes) {
                                ssb.insert(s - 1, "\n ");
                                ssb.setSpan(
                                        new DynamicDrawableSpan() {
                                            @Override
                                            public Drawable getDrawable() {
                                                return d;
                                            }
                                        },
                                        s, s + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE | Spanned.SPAN_USER);
                            } else {
                                for (Object span : ssb.getSpans(s, e, Object.class)) {
                                    int s0 = ssb.getSpanStart(span);
                                    int e0 = ssb.getSpanEnd(span);
                                    if (s0 >= s && e0 <= e)
                                        ssb.removeSpan(span);
                                }
                                ssb.delete(s, e);
                                ssb.insert(s - 1, "\n ");
                                ssb.setSpan(
                                        new DynamicDrawableSpan() {
                                            @Override
                                            public Drawable getDrawable() {
                                                return d;
                                            }
                                        },
                                        s, s + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            }
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
                    ibImages.setVisibility(has_images && !(show_full && always_images) ? View.VISIBLE : View.INVISIBLE);

                    boolean inline = args.getBoolean("inline_encrypted");
                    boolean verifiable = message.isVerifiable() && !inline;
                    boolean encrypted = message.isEncrypted() || inline;
                    boolean unlocked = message.isUnlocked() && !inline;
                    properties.setValue("inline_encrypted", message.id, inline);

                    // Show AMP
                    boolean has_amp = args.getBoolean("has_amp");
                    ibAmp.setVisibility(has_amp && Helper.hasWebView(context)
                            ? View.VISIBLE : View.GONE);

                    // Show encrypt actions
                    ibVerify.setVisibility(verifiable ? View.VISIBLE : View.GONE);
                    ibDecrypt.setImageResource(unlocked
                            ? R.drawable.twotone_lock_24 : R.drawable.twotone_lock_open_24);
                    ibDecrypt.setImageTintList(ColorStateList.valueOf(unlocked
                            ? colorControlNormal : colorAccent));
                    ibDecrypt.setVisibility(encrypted &&
                            !EntityFolder.OUTBOX.equals(message.folderType)
                            ? View.VISIBLE : View.GONE);

                    boolean reformatted_hint = prefs.getBoolean("reformatted_hint", true);
                    tvReformatted.setVisibility(reformatted_hint && hasWebView ? View.VISIBLE : View.GONE);

                    boolean signed_data = args.getBoolean("signed_data");
                    tvDecrypt.setVisibility(encrypted && !unlocked ? View.VISIBLE : View.GONE);
                    tvSignedData.setVisibility(signed_data ? View.VISIBLE : View.GONE);

                    if (!encrypted || inline || unlocked) {
                        if (show_full) {
                            ((WebViewEx) wvBody).setOnPageLoaded(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                            bindConversationActions(message, args.getParcelable("actions"));
                                        bindExtras(message);
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
                                        vwRipple.setVisibility(View.VISIBLE);

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                            bindConversationActions(message, args.getParcelable("actions"));
                                        bindExtras(message);
                                        cowner.start(); // Show attachments
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }
                            });
                        }
                    } else {
                        bindExtras(message);
                        cowner.start(); // Show attachments
                    }

                    if (scroll)
                        properties.scrollTo(getAdapterPosition(), 0);

                    boolean auto_decrypt = prefs.getBoolean("auto_decrypt", false);
                    boolean auto_decrypted = properties.getValue("auto_decrypted", message.id);
                    boolean auto_verify = prefs.getBoolean("auto_verify", false);
                    boolean auto_verified = properties.getValue("auto_verified", message.id);
                    if (auto_decrypt && !auto_decrypted &&
                            (EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) ||
                                    EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt))) {
                        properties.setValue("auto_decrypted", message.id, true);
                        onActionVerifyDecrypt(message, true);
                    } else if (auto_verify && !auto_verified && !message.verified &&
                            (EntityMessage.PGP_SIGNONLY.equals(message.encrypt) ||
                                    EntityMessage.SMIME_SIGNONLY.equals(message.encrypt))) {
                        properties.setValue("auto_verified", message.id, true);
                        onActionVerifyDecrypt(message, true);
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    if (ex instanceof OutOfMemoryError)
                        Helper.setSnackbarOptions(
                                        Snackbar.make(parentFragment.getView(), new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                                .show();
                    else
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                private ConversationActions getConversationActions(TupleMessageEx message, Document document, Context context) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean conversation_actions = prefs.getBoolean("conversation_actions", Helper.isGoogle());
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

        public void searchWebView(String text) {
            if (wvBody instanceof WebView)
                ((WebView) wvBody).findAllAsync(text);
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
                                        raction.getActionIntent().send(); // PendingIntentCompat.send()
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }

                        private void onReply() {
                            Intent reply = new Intent(context, ActivityCompose.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("action", "reply")
                                    .putExtra("reference", message.id)
                                    .putExtra("text", action.getTextReply());
                            context.startActivity(reply);
                        }

                        private void onCopy() {
                            ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
                            if (clipboard == null)
                                return;

                            ClipData clip = ClipData.newPlainText(title, text);
                            clipboard.setPrimaryClip(clip);

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
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

        private void bindExtras(TupleMessageEx message) {
            int resid = 0;
            if (infra && message.infrastructure != null) {
                String resname = "infra_" + message.infrastructure;
                resid = context.getResources()
                        .getIdentifier(resname, "drawable", context.getPackageName());
            }
            if (resid != 0)
                ibInfrastructure.setImageResource(resid);
            ibInfrastructure.setVisibility(resid != 0 ? View.VISIBLE : View.GONE);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean button_extra = prefs.getBoolean("button_extra", false);
            boolean outbox = EntityFolder.OUTBOX.equals(message.folderType);

            ibTrashBottom.setVisibility(outbox || !button_extra ? View.GONE : ibTrash.getVisibility());
            ibArchiveBottom.setVisibility(button_extra ? ibArchive.getVisibility() : View.GONE);
            ibMoveBottom.setVisibility(button_extra ? ibMove.getVisibility() : View.GONE);

            ibSeenBottom.setImageResource(message.ui_seen
                    ? R.drawable.twotone_mail_24 : R.drawable.twotone_drafts_24);
            ibSeenBottom.setVisibility(message.uid != null ||
                    message.accountProtocol == EntityAccount.TYPE_POP
                    ? View.VISIBLE : View.INVISIBLE);
        }

        private void bindAttachments(final TupleMessageEx message, @Nullable List<EntityAttachment> attachments, boolean bind_extras) {
            if (attachments == null)
                attachments = new ArrayList<>();
            properties.setAttachments(message.id, attachments);

            boolean hide_attachments = properties.getValue("hide_attachments", message.id, hide_attachments_default);
            boolean show_inline = properties.getValue("inline", message.id);
            Log.i("Hide attachments=" + hide_attachments + " Show inline=" + show_inline);

            int available = 0;
            int unavailable = 0;
            int downloadable = 0;
            boolean downloading = false;
            boolean has_inline = false;
            EntityAttachment calendar = null;

            List<EntityAttachment> show = new ArrayList<>();
            for (EntityAttachment attachment : attachments) {
                if (attachment.subsequence == null) {
                    if (attachment.available)
                        available++;
                    else
                        unavailable++;

                    if (attachment.progress == null) {
                        if (!attachment.available)
                            downloadable++;
                    } else
                        downloading = true;
                }

                boolean inline = (attachment.isEncryption() ||
                        "text/x-amp-html".equals(attachment.type) ||
                        (attachment.isInline() && attachment.isImage()));
                if (inline && attachment.available)
                    has_inline = true;

                if (show_inline || !inline || !attachment.available)
                    show.add(attachment);

                if (attachment.available &&
                        "text/calendar".equals(attachment.getMimeType()))
                    calendar = attachment;
            }

            rvAttachment.setVisibility(show.size() > 0 && !hide_attachments ? View.VISIBLE : View.GONE);

            vSeparatorAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

            tvAttachments.setText(context.getResources()
                    .getQuantityString(R.plurals.title_attachments, attachments.size(), attachments.size()));
            tvAttachments.setVisibility(attachments.size() > 0 && hide_attachments ? View.VISIBLE : View.GONE);

            ibExpanderAttachments.setImageLevel(hide_attachments ? 1 /* more */ : 0 /* less */);
            ibExpanderAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

            cbInline.setChecked(show_inline);
            cbInline.setVisibility(has_inline && !hide_attachments ? View.VISIBLE : View.GONE);

            ibSaveAttachments.setVisibility(available > 1 && unavailable == 0 && !hide_attachments ? View.VISIBLE : View.GONE);
            ibDownloadAttachments.setVisibility(downloadable > 1 && suitable && !hide_attachments ? View.VISIBLE : View.GONE);
            tvNoInternetAttachments.setVisibility(downloading && !suitable && !hide_attachments ? View.VISIBLE : View.GONE);

            rvAttachment.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        adapterAttachment.set(show);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });

            if (calendar != null && bind_extras)
                bindCalendar(message, calendar);

            int iavailable = 0;
            List<EntityAttachment> media = new ArrayList<>();
            if (thumbnails && !EntityFolder.JUNK.equals(message.folderType) && bind_extras) {
                for (EntityAttachment attachment : attachments)
                    if ((pdf_preview && attachment.isPDF()) ||
                            (video_preview && attachment.isVideo()) ||
                            (audio_preview && attachment.isAudio()) ||
                            (attachment.isAttachment() && attachment.isImage())) {
                        media.add(attachment);
                        if (attachment.available &&
                                attachment.isAttachment() && attachment.isImage())
                            iavailable++;
                    }
            }
            adapterMedia.set(media);
            grpMedia.setVisibility(media.size() > 0 ? View.VISIBLE : View.GONE);

            ibStoreMedia.setVisibility(
                    iavailable > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                            ? View.VISIBLE : View.GONE);
            ibShareImages.setVisibility(iavailable > 0 ? View.VISIBLE : View.GONE);
        }

        private void bindCalendar(final TupleMessageEx message, EntityAttachment attachment) {
            // https://tools.ietf.org/html/rfc5546

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putLong("attachment", attachment.id);
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
                    try {
                        return CalendarHelper.parse(context, file);
                    } catch (Throwable ex) {
                        long attachment = args.getLong("attachment");
                        String message = ex.getMessage();
                        if (TextUtils.isEmpty(message))
                            message = Log.formatThrowable(ex);

                        DB db = DB.getInstance(context);
                        db.attachment().setWarning(attachment, message);

                        return null;
                    }
                }

                @Override
                protected void onExecuted(Bundle args, ICalendar icalendar) {
                    if (icalendar == null)
                        return;

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

                    Uri uri = CalendarHelper.getOnlineMeetingUrl(context, event);
                    ibCalendar.setVisibility(View.VISIBLE);
                    ibOnline.setVisibility(uri == null ? View.GONE : View.VISIBLE);

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
                    // Dummy
                }
            }.setLog(false).execute(context, owner, args, "message:calendar");
        }

        private void onActionCalendar(TupleMessageEx message, int action, boolean share) {
            if (action != R.id.ibCalendar && !ActivityBilling.isPro(context)) {
                context.startActivity(new Intent(context, ActivityBilling.class));
                return;
            }

            if (action == R.id.ibCalendar &&
                    Helper.hasPermission(context, Manifest.permission.WRITE_CALENDAR)) {
                Bundle args = new Bundle();
                args.putLong("message", message.id);
                args.putLong("account", message.account);

                new SimpleTask<String>() {
                    @Override
                    protected String onExecute(Context context, Bundle args) throws Throwable {
                        long aid = args.getLong("account");

                        DB db = DB.getInstance(context);
                        EntityAccount account = db.account().getAccount(aid);
                        return (account == null ? null : account.calendar);
                    }

                    @Override
                    protected void onExecuted(Bundle args, String calendar) {
                        args.putString("calendar", calendar);
                        args.putBoolean("forevent", true);

                        FragmentDialogCalendar fragment = new FragmentDialogCalendar();
                        fragment.setArguments(args);
                        fragment.setTargetFragment(parentFragment, FragmentMessages.REQUEST_CALENDAR);
                        fragment.show(parentFragment.getParentFragmentManager(), "insert:calendar");
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.execute(context, owner, args, "insert:calendar");

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

                    EntityAccount account = db.account().getAccount(message.account);
                    if (account == null)
                        return null;

                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available && "text/calendar".equals(attachment.getMimeType())) {
                            File file = attachment.getFile(context);
                            ICalendar icalendar = CalendarHelper.parse(context, file);
                            CalendarScale scale = (icalendar.getCalendarScale() == null
                                    ? CalendarScale.gregorian()
                                    : icalendar.getCalendarScale());
                            VEvent event = icalendar.getEvents().get(0);

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean ical_tentative = prefs.getBoolean("ical_tentative", true);

                            boolean permission = Helper.hasPermission(context, Manifest.permission.WRITE_CALENDAR);

                            if (permission && !ical_tentative && account.calendar != null) {
                                if (action == R.id.btnCalendarAccept)
                                    CalendarHelper.insert(context, icalendar, event,
                                            CalendarContract.Events.STATUS_CONFIRMED, account, message);
                                else if (action == R.id.btnCalendarDecline)
                                    CalendarHelper.delete(context, event, message);
                                else if (action == R.id.btnCalendarMaybe)
                                    CalendarHelper.insert(context, icalendar, event,
                                            CalendarContract.Events.STATUS_TENTATIVE, account, message);
                            }

                            if (action == R.id.ibCalendar) {
                                String summary = (event.getSummary() == null ? null : event.getSummary().getValue());
                                String description = (event.getDescription() == null ? null : event.getDescription().getValue());
                                String location = (event.getLocation() == null ? null : event.getLocation().getValue());

                                ICalDate start = (event.getDateStart() == null ? null : event.getDateStart().getValue());
                                ICalDate end = (event.getDateEnd() == null ? null : event.getDateEnd().getValue());

                                List<String> attendee = new ArrayList<>();
                                for (Attendee a : event.getAttendees()) {
                                    String email = a.getEmail();
                                    if (!TextUtils.isEmpty(email)) {
                                        email = email.replaceAll("\\s+", "");
                                        attendee.add(email);
                                    }
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

                                RecurrenceRule recurrence = event.getRecurrenceRule();
                                if (recurrence != null) {
                                    RecurrenceRuleScribe scribe = new RecurrenceRuleScribe();
                                    WriteContext wcontext = new WriteContext(ICalVersion.V2_0, icalendar.getTimezoneInfo(), null);
                                    String rrule = scribe.writeText(recurrence, wcontext);
                                    intent.putExtra(CalendarContract.Events.RRULE, rrule);
                                }

                                // This will result in sending unwanted invites
                                //if (attendee.size() > 0)
                                //    intent.putExtra(Intent.EXTRA_EMAIL, TextUtils.join(",", attendee));

                                return intent;
                            }

                            if (action == R.id.ibOnline)
                                return CalendarHelper.getOnlineMeetingUrl(context, event);

                            Created created = event.getCreated();
                            LastModified modified = event.getLastModified();
                            Transparency transparancy = event.getTransparency();

                            // https://tools.ietf.org/html/rfc5546#section-4.2.2
                            VEvent ev = new VEvent();

                            if (created != null && false)
                                ev.setCreated(created);
                            if (modified != null && false)
                                ev.setLastModified(modified);
                            if (transparancy != null && false)
                                ev.setTransparency(transparancy);

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
                            if (!TextUtils.isEmpty(email))
                                email = email
                                        .replace("\n", " ")
                                        .replace("\r", " ")
                                        .replace("\"", "'");
                            if (!TextUtils.isEmpty(name))
                                name = name
                                        .replace("\n", " ")
                                        .replace("\r", " ")
                                        .replace("\"", "'");

                            Attendee attendee = new Attendee(name, email);
                            //attendee.setCalendarUserType(CalendarUserType.INDIVIDUAL);
                            attendee.setRole(Role.ATTENDEE);
                            //attendee.setRsvp(true);

                            String status = null;
                            if (action == R.id.btnCalendarAccept) {
                                //ev.setStatus(Status.accepted());
                                attendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
                                status = context.getString(R.string.title_icalendar_accept);
                            } else if (action == R.id.btnCalendarDecline) {
                                //ev.setStatus(Status.declined());
                                attendee.setParticipationStatus(ParticipationStatus.DECLINED);
                                status = context.getString(R.string.title_icalendar_decline);
                            } else if (action == R.id.btnCalendarMaybe) {
                                //ev.setStatus(Status.tentative());
                                attendee.setParticipationStatus(ParticipationStatus.TENTATIVE);
                                status = context.getString(R.string.title_icalendar_maybe);
                            }

                            ev.addAttendee(attendee);

                            if (status != null) {
                                args.putString("status", status);
                                ev.setStatus(Status.confirmed());
                                Summary summary = ev.getSummary();
                                ev.setSummary(status + ": " + (summary == null ? "" : summary.getValue()));
                            }

                            // Microsoft specific properties:
                            // X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE
                            // X-MICROSOFT-CDO-IMPORTANCE:1
                            // X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY
                            // X-MICROSOFT-DISALLOW-COUNTER:FALSE
                            // X-MS-OLK-AUTOSTARTCHECK:FALSE
                            // X-MS-OLK-CONFTYPE:0

                            // https://icalendar.org/validator.html
                            ICalendar response = new ICalendar();
                            response.setCalendarScale(scale);
                            response.setMethod(Method.REPLY);
                            response.addEvent(ev);

                            File dir = Helper.ensureExists(context, "calendar");
                            File ics = new File(dir, message.id + ".ics");
                            response.write(ics);

                            return ics;
                        }

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Object result) {
                    if (result instanceof File) {
                        String status = args.getString("status");

                        if (args.getBoolean("share"))
                            Helper.share(context, (File) result, "text/calendar", status + ".ics");
                        else {
                            Intent reply = new Intent(context, ActivityCompose.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("action", "participation")
                                    .putExtra("reference", args.getLong("id"))
                                    .putExtra("ics", (File) result)
                                    .putExtra("status", status);
                            context.startActivity(reply);
                        }
                    } else if (result instanceof Intent) {
                        try {
                            context.startActivity((Intent) result);
                        } catch (Throwable ex) {
                            Helper.reportNoViewer(context, (Intent) result, ex);
                        }
                    } else if (result instanceof Uri)
                        Helper.view(context, (Uri) result, true);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    if (!(ex instanceof AssertionError))
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:participation");
        }

        private void onStoreMedia(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Uri>() {
                @Override
                protected Uri onExecute(Context context, Bundle args) throws IOException {
                    long id = args.getLong("id");

                    ContentResolver resolver = context.getContentResolver();

                    String folder = context.getString(R.string.app_name);
                    Uri collection = (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                            ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                    File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    // Android < 10 requires:
                    // <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
                    // https://developer.android.com/training/data-storage/shared/media#request-permissions

                    DB db = DB.getInstance(context);
                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    if (attachments == null || attachments.size() == 0)
                        return null;

                    for (EntityAttachment attachment : attachments)
                        if (attachment.available &&
                                attachment.isAttachment() && attachment.isImage()) {

                            // Check if exists
                            if (attachment.media_uri != null) {
                                Uri uri = Uri.parse(attachment.media_uri);
                                try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                                    if (cursor.moveToFirst())
                                        continue;
                                }
                            }

                            File file = attachment.getFile(context);
                            String type = attachment.getMimeType();
                            String title = (attachment.name == null ? file.getName() : attachment.name);

                            ContentValues values = new ContentValues();
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                File image = new File(pictures, file.getName());
                                values.put(MediaStore.Images.Media.DATA, image.getPath());
                            } else {
                                File target = new File(Environment.DIRECTORY_PICTURES, folder);
                                values.put(MediaStore.Images.Media.RELATIVE_PATH, target.getPath());
                            }
                            values.put(MediaStore.Images.Media.TITLE, title);
                            values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
                            values.put(MediaStore.Images.Media.MIME_TYPE, type);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                            Uri uri = resolver.insert(collection, values);
                            if (uri == null)
                                return null;

                            InputStream is = null;
                            OutputStream os = null;
                            try {
                                is = new FileInputStream(file);
                                os = resolver.openOutputStream(uri);
                                if (os == null)
                                    throw new FileNotFoundException(uri.toString());
                                Helper.copy(is, os);
                            } finally {
                                try {
                                    if (is != null)
                                        is.close();
                                } finally {
                                    if (os != null)
                                        os.close();
                                }
                            }

                            values.clear();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                            resolver.update(uri, values, null, null);

                            attachment.media_uri = uri.toString();
                            db.attachment().setMediaUri(attachment.id, attachment.media_uri);
                        }

                    // Viewing the containing folder is not possible
                    for (EntityAttachment attachment : attachments)
                        if (attachment.media_uri != null)
                            return Uri.parse(attachment.media_uri);

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Uri uri) {
                    if (uri == null)
                        return;
                    Intent view = new Intent(Intent.ACTION_VIEW)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setDataAndType(uri, "image/*");
                    context.startActivity(
                            Intent.createChooser(view, context.getString(R.string.title_select)));
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "images:store");
        }

        private void onShareImages(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<ArrayList<Uri>>() {
                @Override
                protected ArrayList<Uri> onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    if (attachments == null)
                        return null;

                    ArrayList<Uri> result = new ArrayList<>();
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available &&
                                attachment.isAttachment() && attachment.isImage())
                            result.add(attachment.getUri(context));

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, ArrayList<Uri> uris) {
                    if (uris == null)
                        return;

                    ShareCompat.IntentBuilder shareIntentBuilder = new ShareCompat.IntentBuilder(context);
                    shareIntentBuilder.setType("image/*");
                    for (Uri uri : uris)
                        shareIntentBuilder.addStream(uri);
                    shareIntentBuilder
                            .setChooserTitle(R.string.title_select_app)
                            .startChooser();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "images:share");
        }

        private boolean isOutgoing(TupleMessageEx message) {
            if (EntityFolder.isOutgoing(message.folderType) || EntityFolder.isOutgoing(message.folderInheritedType))
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

                if (message.submitter != null)
                    for (Address submitter : message.submitter)
                        if (message.identityEmail.equalsIgnoreCase(((InternetAddress) submitter).getAddress()))
                            return true;

                return false;
            }
        }

        private TupleMessageEx getMessage() {
            try {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return null;

                return differ.getItem(pos);
            } catch (Throwable ex) {
                /*
                    Exception java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
                      at androidx.paging.PagedList.loadAround (PagedList.java:424)
                      at androidx.paging.AsyncPagedListDiffer.getItem (AsyncPagedListDiffer.java:216)
                      at eu.faircode.email.AdapterMessage$ViewHolder.getMessage (AdapterMessage.java:4334)
                      at eu.faircode.email.AdapterMessage$ViewHolder.onKeyPressed (AdapterMessage.java:4746)
                      at eu.faircode.email.FragmentMessages$131.onKeyPressed (FragmentMessages.java:8253)
                 */
                Log.e(ex);
                return null;
            }
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (itemView.getId() == v.getId()) {
                properties.layoutChanged();
                return;
            }

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

            int id = view.getId();
            if (id == R.id.ibAvatar)
                onViewContact(message);
            else if (id == R.id.ibVerified)
                onShowVerified(message);
            else if (id == R.id.ibAuth)
                onShowAuth(message, null);
            else if (id == R.id.ibPriority)
                onShowPriority(message);
            else if (id == R.id.ibSensitivity)
                onShowSensitivity(message);
            else if (id == R.id.ibSigned)
                onShowSigned(message);
            else if (id == R.id.ibEncrypted)
                onShowEncrypted(message);
            else if (id == R.id.ibSnoozed)
                onShowSnoozed(message);
            else if (id == R.id.ibFlagged)
                onToggleFlag(message);
            else if (id == R.id.ibError)
                onHelp(message);
            else if (id == R.id.ibReceipt)
                onReceipt(message);
            else if (id == R.id.ibSearchContact)
                onSearchContact(message, false);
            else if (id == R.id.ibNotifyContact)
                onNotifyContact(message);
            else if (id == R.id.ibPinContact)
                onPinContact(message);
            else if (id == R.id.ibAddContact)
                onAddContact(message);
            else if (viewType == ViewType.THREAD) {
                if (id == R.id.ibExpanderAddress) {
                    onToggleAddresses(message);
                } else if (id == R.id.ibCopyHeaders) {
                    onCopyHeaders(message);
                } else if (id == R.id.ibCloseHeaders) {
                    onMenuShowHeaders(message);
                } else if (id == R.id.ibExpanderAttachments) {
                    onExpandAttachments(message);
                } else if (id == R.id.ibSaveAttachments) {
                    onSaveAttachments(message);
                } else if (id == R.id.ibDownloadAttachments) {
                    onDownloadAttachments(message);
                } else if (id == R.id.ibFull) {
                    onShow(message, true);
                } else if (id == R.id.ibImages) {
                    onShow(message, false);
                } else if (id == R.id.ibAmp) {
                    onShowAmp(message);
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
                        onActionVerifyDecrypt(message, false);
                } else if (id == R.id.ibVerify) {
                    onActionVerifyDecrypt(message, false);
                } else if (id == R.id.ibUndo) {
                    ActivityCompose.undoSend(message.id, context, owner, parentFragment.getParentFragmentManager());
                } else if (id == R.id.ibAnswer) {
                    onActionAnswer(message, ibAnswer);
                } else if (id == R.id.ibRule) {
                    onMenuCreateRule(message);
                } else if (id == R.id.ibUnsubscribe) {
                    onActionUnsubscribe(message);
                } else if (id == R.id.ibRaw) {
                    onMenuRawSave(message);
                } else if (id == R.id.ibHtml) {
                    onMenuShareHtml(message);
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
                    onSearchContact(message, false);
                } else if (id == R.id.ibTranslate) {
                    onActionTranslate(message);
                } else if (id == R.id.ibSummarize) {
                    onActionSummarize(message, ibSummarize);
                } else if (id == R.id.ibFullScreen)
                    onActionOpenFull(message);
                else if (id == R.id.ibForceLight) {
                    onActionForceLight(message);
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
                } else if (id == R.id.ibInfrastructure) {
                    onActionShowInfra(message);
                } else if (id == R.id.ibMore) {
                    onActionMore(message);
                } else if (id == R.id.ibTools) {
                    onActionTools(message);
                } else if (id == R.id.ibDownloading) {
                    Helper.viewFAQ(context, 15);
                } else if (id == R.id.ibDownload)
                    onActionDownload(message);
                else if (id == R.id.ibSeen || id == R.id.ibSeenBottom) {
                    onToggleSeen(message);
                } else if (id == R.id.ibHide) {
                    onMenuHide(message);
                } else if (id == R.id.ibImportance) {
                    int importance = (((message.ui_importance == null ? 1 : message.ui_importance) + 1) % 3);
                    onMenuSetImportance(message, importance);
                } else if (id == R.id.btnCalendarAccept || id == R.id.btnCalendarDecline || id == R.id.btnCalendarMaybe ||
                        id == R.id.ibCalendar || id == R.id.ibOnline) {
                    onActionCalendar(message, view.getId(), false);
                } else if (id == R.id.ibStoreMedia) {
                    onStoreMedia(message);
                } else if (id == R.id.ibShareImages) {
                    onShareImages(message);
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
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("action", "edit")
                                    .putExtra("id", message.id));
                    properties.setValue("selected", message.id, true);
                } else {
                    boolean filter_archive = !(viewType == ViewType.SEARCH ||
                            EntityFolder.ARCHIVE.equals(message.folderType));
                    final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    final Intent viewThread = new Intent(ActivityView.ACTION_VIEW_THREAD)
                            .putExtra("account", message.account)
                            .putExtra("folder", message.folder)
                            .putExtra("thread", message.thread)
                            .putExtra("id", message.id)
                            .putExtra("lpos", getAdapterPosition())
                            .putExtra("filter_archive", filter_archive)
                            .putExtra("found", viewType == ViewType.SEARCH)
                            .putExtra("searched", searched)
                            .putExtra("searchedPartial", searchedPartial);

                    boolean doubletap = prefs.getBoolean("doubletap", false);

                    if (!doubletap ||
                            (message.uid == null && message.accountProtocol == EntityAccount.TYPE_IMAP) ||
                            EntityFolder.OUTBOX.equals(message.folderType)) {
                        lbm.sendBroadcast(viewThread);
                        properties.setValue("selected", message.id, true);
                        return;
                    }

                    boolean firstClick = !properties.getValue("firstClick", message.id);
                    properties.setValue("firstClick", message.id, firstClick);
                    if (firstClick) {
                        ApplicationEx.getMainHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                boolean firstClick = properties.getValue("firstClick", message.id);
                                if (firstClick) {
                                    properties.setValue("firstClick", message.id, false);
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
            if (message == null)
                return false;

            int id = view.getId();
            if (id == R.id.ibFlagged) {
                onMenuColoredStar(message);
                return true;
            } else if (id == R.id.ibAddContact) {
                onInfo(message, true);
                return true;
            } else if (id == R.id.ibPinContact) {
                onInfo(message, false);
                return true;
            } else if (id == R.id.tvFolder) {
                onGotoFolder(message);
                return true;
            } else if (id == R.id.ibInbox) {
                boolean inJunk = EntityFolder.JUNK.equals(message.folderType);
                if (inJunk)
                    onActionJunk(message);
                return inJunk;
            } else if (id == R.id.ibImportance) {
                int importance = (((message.ui_importance == null ? 1 : message.ui_importance) + 2) % 3);
                onMenuSetImportance(message, importance);
                return true;
            } else if (id == R.id.ibNotes) {
                onActionCopyNote(message);
                return true;
            } else if (id == R.id.ibTranslate) {
                DeepL.FragmentDialogDeepL fragment = new DeepL.FragmentDialogDeepL();
                fragment.show(parentFragment.getParentFragmentManager(), "deepl:configure");
                return true;
            } else if (id == R.id.ibHeaders) {
                onMenuShareHtml(message);
                return true;
            } else if (id == R.id.ibSummarize) {
                context.startActivity(new Intent(context, ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "integrations"));
                return true;
            } else if (id == R.id.ibFull) {
                boolean full = properties.getValue("full", message.id);
                if (!full) {
                    onActionOpenFull(message);
                    return true;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibFull);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_fullscreen, 1, R.string.title_fullscreen);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_fit_width, 2, R.string.title_fit_width)
                        .setCheckable(true)
                        .setChecked(prefs.getBoolean("overview_mode", false));
                popupMenu.getMenu().add(Menu.NONE, R.string.title_monospaced_pre, 4, R.string.title_monospaced_pre)
                        .setCheckable(true)
                        .setChecked(prefs.getBoolean("monospaced_pre", false));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.string.title_fullscreen) {
                            onActionOpenFull(message);
                            return true;
                        } else if (itemId == R.string.title_fit_width ||
                                itemId == R.string.title_monospaced_pre) {
                            boolean enabled = !item.isChecked();
                            item.setChecked(enabled);

                            if (itemId == R.string.title_fit_width)
                                prefs.edit().putBoolean("overview_mode", enabled).apply();
                            else if (itemId == R.string.title_monospaced_pre)
                                prefs.edit().putBoolean("monospaced_pre", enabled).apply();

                            properties.setSize(message.id, null);
                            properties.setHeight(message.id, null);
                            properties.setPosition(message.id, null, null);

                            if (wvBody instanceof WebViewEx)
                                ((WebViewEx) wvBody).clearCache();

                            if (itemId == R.string.title_fit_width && wvBody instanceof WebView)
                                ((WebView) wvBody).getSettings().setLoadWithOverviewMode(enabled);

                            bindBody(message, false);
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
                return true;
            } else if (id == R.id.ibMove) {
                if (message.folderReadOnly)
                    return false;
                onActionMoveAccount(message, ibMove);
                return true;
            } else if (id == R.id.ibMoveBottom) {
                if (message.folderReadOnly)
                    return false;
                onActionMoveAccount(message, ibMoveBottom);
                return true;
            } else if (id == R.id.ibTrash || id == R.id.ibTrashBottom) {
                if (message.folderReadOnly)
                    return false;
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

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            final TupleMessageEx message = getMessage();
            if (message == null)
                return;

            int id = compoundButton.getId();
            if (id == R.id.cbInline) {
                onShowInlineAttachments(message, isChecked);
            }
        }

        public boolean onKeyPressed(KeyEvent event) {
            TupleMessageEx message = getMessage();
            if (message == null)
                return false;

            if (event.isCtrlPressed() || event.isAltPressed())
                return false;

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
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
                } catch (Throwable ex) {
                    Helper.reportNoViewer(context, intent, ex);
                }
            }
        }

        private void onShowVerified(TupleMessageEx message) {
            onShowAuth(message, ibVerified.getContentDescription().toString());
        }

        private void onShowAuth(TupleMessageEx message, String title) {
            SpannableStringBuilder ssb = new SpannableStringBuilderEx();

            if (title != null)
                ssb.append(title).append('\n');

            List<String> result = new ArrayList<>();
            if (Boolean.FALSE.equals(message.dkim))
                result.add("DKIM");
            if (Boolean.FALSE.equals(message.spf))
                result.add("SPF");
            if (Boolean.FALSE.equals(message.dmarc))
                result.add("DMARC");
            if (Boolean.FALSE.equals(message.auth))
                result.add("SMTP");
            if (Boolean.FALSE.equals(message.mx))
                result.add("MX");

            if (result.size() > 0)
                ssb.append(context.getString(R.string.title_authentication_failed, TextUtils.join(", ", result)))
                        .append('\n');

            if (authentication_indicator) {
                if (check_tls)
                    ssb.append("TLS: ")
                            .append(message.tls == null ? "-" : (message.tls ? "✓" : "✗")).append('\n');
                ssb.append("DKIM: ")
                        .append(message.dkim == null ? "-" : (message.dkim ? "✓" : "✗")).append('\n');
                ssb.append("SPF: ")
                        .append(message.spf == null ? "-" : (message.spf ? "✓" : "✗")).append('\n');
                ssb.append("DMARC: ")
                        .append(message.dmarc == null ? "-" : (message.dmarc ? "✓" : "✗")).append('\n');
                if (message.auth != null)
                    ssb.append("SMTP: ")
                            .append(message.auth ? "✓" : "✗").append('\n');
                if (check_mx)
                    ssb.append("MX: ")
                            .append(message.mx == null ? "-" : (message.mx ? "✓" : "✗")).append('\n');
            }

            if (native_dkim && !TextUtils.isEmpty(message.signedby)) {
                ssb.append(context.getString(R.string.title_signed_by)).append(' ');
                for (String signer : message.signedby.split(",")) {
                    int start = ssb.length();
                    ssb.append(signer);
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                    ssb.append('\n');
                }
            }

            if (Boolean.TRUE.equals(message.blocklist))
                ssb.append(context.getString(R.string.title_on_blocklist)).append('\n');

            if (Boolean.FALSE.equals(message.from_domain) && message.smtp_from != null)
                for (Address smtp_from : message.smtp_from) {
                    String domain = UriHelper.getEmailDomain(((InternetAddress) smtp_from).getAddress());
                    String root = UriHelper.getRootDomain(context, domain);
                    if (root != null)
                        ssb.append(context.getString(R.string.title_via, root)).append('\n');
                }

            if (Boolean.FALSE.equals(message.reply_domain)) {
                String[] warning = message.checkReplyDomain(context);
                if (warning != null)
                    ssb.append(context.getString(R.string.title_reply_domain, warning[0], warning[1])).append('\n');
            }

            if (message.from != null && message.from.length > 0) {
                String email = ((InternetAddress) message.from[0]).getAddress();
                String domain = UriHelper.getEmailDomain(email);
                if (!TextUtils.isEmpty(domain)) {
                    ssb.insert(0, "\n").insert(0, domain);
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, domain.length(), 0);
                }
            }

            if (ssb.length() > 0 && ssb.charAt(ssb.length() - 1) == '\n')
                ssb.delete(ssb.length() - 1, ssb.length());

            ToastEx.makeText(context, ssb, Toast.LENGTH_LONG).show();
        }

        private int getPriority(TupleMessageEx message) {
            int resid = -1;
            if (EntityMessage.PRIORITIY_HIGH.equals(message.ui_priority))
                resid = R.string.title_legend_priority;
            else
                resid = R.string.title_legend_priority_low;
            return resid;
        }

        private void onShowPriority(TupleMessageEx message) {
            int resid = getPriority(message);
            if (resid > 0)
                ToastEx.makeText(context, resid, Toast.LENGTH_LONG).show();
        }

        private int getSensitivity(TupleMessageEx message) {
            int resid = -1;
            if (EntityMessage.SENSITIVITY_PERSONAL.equals(message.sensitivity))
                resid = R.string.title_legend_sensitivity_personal;
            else if (EntityMessage.SENSITIVITY_PRIVATE.equals(message.sensitivity))
                resid = R.string.title_legend_sensitivity_private;
            else if (EntityMessage.SENSITIVITY_CONFIDENTIAL.equals(message.sensitivity))
                resid = R.string.title_legend_sensitivity_confidential;
            return resid;
        }

        private void onShowSensitivity(TupleMessageEx message) {
            int resid = getSensitivity(message);
            if (resid > 0)
                ToastEx.makeText(context, resid, Toast.LENGTH_LONG).show();
        }

        private int getSigned(TupleMessageEx message) {
            int resid = -1;
            if (EntityMessage.PGP_SIGNONLY.equals(message.ui_encrypt))
                resid = R.string.title_advanced_caption_pgp;
            else if (EntityMessage.SMIME_SIGNONLY.equals(message.ui_encrypt))
                resid = R.string.title_advanced_caption_smime;
            return resid;
        }

        private void onShowSigned(TupleMessageEx message) {
            int resid = getSigned(message);
            if (resid > 0)
                ToastEx.makeText(context, resid, Toast.LENGTH_LONG).show();
        }

        private int getEncrypted(TupleMessageEx message) {
            int resid = -1;
            if (EntityMessage.PGP_SIGNENCRYPT.equals(message.ui_encrypt))
                resid = R.string.title_advanced_caption_pgp;
            else if (EntityMessage.SMIME_SIGNENCRYPT.equals(message.ui_encrypt))
                resid = R.string.title_advanced_caption_smime;
            return resid;
        }

        private void onShowEncrypted(TupleMessageEx message) {
            int resid = getEncrypted(message);
            if (resid > 0)
                ToastEx.makeText(context, resid, Toast.LENGTH_LONG).show();
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

        private void onInfo(TupleMessageEx message, boolean gpa) {
            if (!BuildConfig.DEBUG)
                return;

            Address[] from;
            if (message.reply == null || message.reply.length == 0)
                from = (isOutgoing(message) ? message.to : message.from);
            else
                from = message.reply;
            if (from == null || from.length == 0)
                return;

            String email = ((InternetAddress) from[0]).getAddress();
            if (TextUtils.isEmpty(email))
                return;

            Uri uri;
            if (gpa)
                uri = Uri.parse(BuildConfig.GPA_URI).buildUpon()
                        .appendQueryParameter("search", email)
                        .build();
            else
                uri = Uri.parse(BuildConfig.INFO_URI).buildUpon()
                        .appendQueryParameter("email", email)
                        .build();
            Helper.view(context, uri, true);
        }

        private void onGotoFolder(TupleMessageEx message) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            if (EntityFolder.OUTBOX.equals(message.folderType))
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_VIEW_OUTBOX));
            else
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("account", message.account)
                                .putExtra("folder", message.folder)
                                .putExtra("type", message.folderType));
        }

        private void onHelp(TupleMessageEx message) {
            if (message.error != null &&
                    message.error.contains("535 5.7.3 Authentication unsuccessful")) {
                Intent intent = new Intent(context, ActivityError.class);
                intent.setAction("535:" + message.identity);
                intent.putExtra("title", "535 5.7.3 Authentication unsuccessful");
                intent.putExtra("message", message.error);
                intent.putExtra("provider", "outlookgraph");
                intent.putExtra("account", message.account);
                intent.putExtra("protocol", message.accountProtocol);
                intent.putExtra("auth_type", ServiceAuthenticator.AUTH_TYPE_GRAPH);
                intent.putExtra("host", "");
                intent.putExtra("identity", message.identity);
                intent.putExtra("personal", message.identityName);
                intent.putExtra("address", message.identityEmail);
                intent.putExtra("faq", 14);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibError);

            int order = 0;
            popupMenu.getMenu().add(Menu.NONE, R.string.title_setup_help, order++, R.string.title_setup_help).
                    setIcon(R.drawable.twotone_text_snippet_24);
            if (Helper.hasValidFingerprint(context) || BuildConfig.DEBUG)
                popupMenu.getMenu().add(Menu.NONE, R.string.menu_faq, order++, R.string.menu_faq)
                        .setIcon(R.drawable.twotone_question_answer_24);
            popupMenu.getMenu().add(Menu.NONE, R.string.menu_setup, order++, R.string.menu_setup)
                    .setIcon(R.drawable.twotone_settings_24);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_clipboard_copy, order++, R.string.title_clipboard_copy)
                    .setIcon(R.drawable.twotone_file_copy_24);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.string.title_setup_help) {
                        onHelp();
                        return true;
                    } else if (itemId == R.string.menu_faq) {
                        Helper.view(context, Helper.getSupportUri(context, "Message:error"), false);
                        return true;
                    } else if (itemId == R.string.menu_setup) {
                        onSettings();
                        return true;
                    } else if (itemId == R.string.title_clipboard_copy) {
                        onCopy();
                        return true;
                    }
                    return false;
                }

                private void onHelp() {
                    Helper.viewFAQ(context, 130);
                }

                private void onSettings() {
                    if (EntityFolder.OUTBOX.equals(message.folderType))
                        context.startActivity(new Intent(context, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("target", "identities")
                                .putExtra("id", message.identity));
                    else
                        context.startActivity(new Intent(context, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("target", "accounts")
                                .putExtra("id", message.account)
                                .putExtra("protocol", message.accountProtocol));
                }

                private void onCopy() {
                    ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
                    if (clipboard == null)
                        return;

                    ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), tvError.getText());
                    clipboard.setPrimaryClip(clip);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                        ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
                }
            });

            popupMenu.insertIcons(context);

            popupMenu.show();
        }

        private void onReceipt(TupleMessageEx message) {
            if (EntityFolder.isOutgoing(message.folderType))
                ToastEx.makeText(context, R.string.title_legend_receipt, Toast.LENGTH_LONG).show();
            else {
                Intent reply = new Intent(context, ActivityCompose.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("action", "dsn")
                        .putExtra("dsn", EntityMessage.DSN_RECEIPT)
                        .putExtra("reference", message.id);
                context.startActivity(reply);
            }
        }

        private void onSearchContact(TupleMessageEx message, boolean sender_only) {
            FragmentMessages.searchContact(context, owner, parentFragment.getParentFragmentManager(), message.id, sender_only);
        }

        @TargetApi(Build.VERSION_CODES.O)
        private void onNotifyContact(final TupleMessageEx message) {
            final NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
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
                    channel.setBypassDnd(true);
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
                    } catch (Throwable ex) {
                        Helper.reportNoViewer(context, intent, ex);
                    }
                }

                private void onActionDeleteChannel() {
                    NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
                    nm.deleteNotificationChannel(channelId);
                }
            });

            popupMenu.show();
        }

        private void onPinContact(TupleMessageEx message) {
            try {
                ShortcutInfoCompat.Builder builder =
                        Shortcuts.getShortcut(context, (InternetAddress) message.from[0]);
                Shortcuts.requestPinShortcut(context, builder.build());
            } catch (Throwable ex) {
                Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
            }
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
            pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            properties.setValue("name", name);
            properties.setValue("email", email);
            try {
                parentFragment.startActivityForResult(
                        Helper.getChooser(context, pick), FragmentMessages.REQUEST_PICK_CONTACT);
            } catch (Throwable ex) {
                Helper.reportNoViewer(context, pick, ex);
            }
        }

        private void onInsertContact(String name, String email) {
            if (TextUtils.isEmpty(name))
                name = UriHelper.getEmailUser(email);

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
            if (EntityFolder.DRAFTS.equals(message.folderType) && message.visible == 1 &&
                    !EntityMessage.PGP_SIGNENCRYPT.equals(message.encrypt) &&
                    !EntityMessage.SMIME_SIGNENCRYPT.equals(message.encrypt))
                context.startActivity(
                        new Intent(context, ActivityCompose.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("action", "edit")
                                .putExtra("id", message.id));
            else {
                boolean expanded = !properties.getValue("expanded", message.id);
                properties.setExpanded(message, expanded, expanded);
            }
        }

        private void onToggleAddresses(TupleMessageEx message) {
            boolean addresses = !properties.getValue("addresses", message.id, getShowAddressesDefault(message));
            properties.setValue("addresses", message.id, addresses);
            bindAddresses(message);
        }

        private void onActionDownload(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        db.message().setMessageError(message.id, null);

                        EntityOperation.queue(context, message, EntityOperation.BODY);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragment(), ex);
                }
            }.execute(context, owner, args, "message:downlaod");
        }

        private boolean getShowAddressesDefault(TupleMessageEx message) {
            if (show_addresses_default)
                return true;

            if (message.from != null)
                for (Address from : message.from) {
                    if (!(from instanceof InternetAddress))
                        continue;
                    String personal = ((InternetAddress) from).getPersonal();
                    if (TextUtils.isEmpty(personal))
                        continue;
                    if (!TextHelper.isSingleScript(personal))
                        return true;
                    if (false && !BuildConfig.PLAY_STORE_RELEASE && Helper.EMAIL_ADDRESS.matcher(personal).find())
                        return true;
                }

            return false;
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
                        if (message == null)
                            return null;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            return null;

                        if (account.protocol == EntityAccount.TYPE_IMAP && message.uid == null)
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

        private void onExpandAttachments(TupleMessageEx message) {
            boolean hide_attachments = properties.getValue("hide_attachments", message.id, hide_attachments_default);
            properties.setValue("hide_attachments", message.id, !hide_attachments);
            cowner.restart();
            bindAttachments(message, properties.getAttachments(message.id), false);
        }

        private void onShowInlineAttachments(TupleMessageEx message, boolean isChecked) {
            properties.setValue("inline", message.id, isChecked);
            cowner.restart();
            bindAttachments(message, properties.getAttachments(message.id), false);
        }

        private void onSaveAttachments(TupleMessageEx message) {
            ((FragmentBase) parentFragment).onStoreAttachments(message.id);
        }

        private void onActionCopyNote(TupleMessageEx message) {
            if (TextUtils.isEmpty(message.notes))
                return;

            ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
            if (clipboard == null)
                return;

            ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), message.notes);
            clipboard.setPrimaryClip(clip);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
        }

        private void onShow(final TupleMessageEx message, boolean full) {
            if (full && tvReformatted.getVisibility() == View.VISIBLE) {
                prefs.edit().putBoolean("reformatted_hint", false).apply();
                tvReformatted.setVisibility(View.GONE);
            }

            boolean junk = EntityFolder.JUNK.equals(message.folderType);
            Address[] senders = (message.isForwarder() ? message.submitter : message.from);

            boolean current = properties.getValue(full ? "full" : "images", message.id);
            boolean asked = properties.getValue(full ? "full_asked" : "images_asked", message.id);
            boolean confirm = prefs.getBoolean(full ? "confirm_html" : "confirm_images", true) || junk;
            boolean ask = prefs.getBoolean(full ? "ask_html" : "ask_images", true) || junk;
            if (current || asked || !confirm || !ask) {
                if (current && senders != null) {
                    SharedPreferences.Editor editor = prefs.edit();
                    for (Address sender : senders) {
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
            TextView tvMessage = dview.findViewById(R.id.tvMessage);
            CheckBox cbAlwaysImages = dview.findViewById(R.id.cbAlwaysImages); // Full only
            CheckBox cbNotAgainSender = dview.findViewById(R.id.cbNotAgainSender);
            CheckBox cbNotAgainDomain = dview.findViewById(R.id.cbNotAgainDomain);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            CheckBox cbNeverAgain = dview.findViewById(R.id.cbNeverAgain);
            TextView tvNeverAgainHint = dview.findViewById(R.id.tvNeverAgainHint);

            tvMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(v.getContext(), 35);
                }
            });

            if (junk) {
                if (full)
                    cbAlwaysImages.setVisibility(View.GONE);
                cbNotAgainSender.setVisibility(View.GONE);
                cbNotAgainDomain.setVisibility(View.GONE);
                cbNotAgain.setVisibility(View.GONE);
                cbNeverAgain.setVisibility(View.GONE);
                tvNeverAgainHint.setVisibility(View.GONE);
            } else if (senders == null || senders.length == 0) {
                cbNotAgainSender.setVisibility(View.GONE);
                cbNotAgainDomain.setVisibility(View.GONE);
            } else {
                List<String> froms = new ArrayList<>();
                List<String> domains = new ArrayList<>();
                for (Address address : senders) {
                    String from = ((InternetAddress) address).getAddress();
                    froms.add(from);
                    int at = from.indexOf('@');
                    String domain = (at < 0 ? from : from.substring(at));
                    if (!Objects.equals(from, domain))
                        domains.add(domain);
                }
                cbNotAgainSender.setText(context.getString(R.string.title_no_ask_for_again,
                        TextUtils.join(", ", froms)));
                cbNotAgainDomain.setText(context.getString(R.string.title_no_ask_for_again,
                        TextUtils.join(", ", domains)));
                cbNotAgainDomain.setVisibility(domains.size() > 0 ? View.VISIBLE : View.GONE);
            }

            cbNotAgainDomain.setEnabled(false);
            cbNotAgainSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cbNotAgainDomain.setEnabled(isChecked);
                }
            });

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    cbNotAgainSender.setEnabled(!isChecked);
                    cbNotAgainDomain.setEnabled(!isChecked && cbNotAgainSender.isChecked());
                    cbNeverAgain.setEnabled(isChecked);
                }
            });

            cbNeverAgain.setEnabled(false);

            if (full) {
                TextView tvDark = dview.findViewById(R.id.tvDark);

                cbAlwaysImages.setChecked(prefs.getBoolean("html_always_images", false));

                cbAlwaysImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        prefs.edit().putBoolean("html_always_images", isChecked).apply();
                    }
                });

                boolean isDark = Helper.isDarkTheme(context);
                tvDark.setVisibility(isDark && !(canDarken || fake_dark) ? View.VISIBLE : View.GONE);
            }

            boolean disable_tracking = prefs.getBoolean("disable_tracking", true);

            TextView tvTracking = dview.findViewById(R.id.tvTracking);
            Group grpTracking = dview.findViewById(R.id.grpTracking);

            grpTracking.setVisibility(disable_tracking ? View.VISIBLE : View.GONE);

            tvTracking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(v.getContext(), 82);
                }
            });

            // TODO: dialog fragment
            final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            properties.setValue(full ? "full" : "images", message.id, true);
                            properties.setValue(full ? "full_asked" : "images_asked", message.id, true);

                            if (!junk) {
                                SharedPreferences.Editor editor = prefs.edit();

                                if (cbNotAgain.isChecked() && cbNeverAgain.isChecked())
                                    editor.putBoolean(full ? "confirm_html" : "confirm_images", false);
                                else if (senders != null && !cbNotAgain.isChecked())
                                    for (Address sender : senders) {
                                        String from = ((InternetAddress) sender).getAddress();
                                        if (TextUtils.isEmpty(from))
                                            continue;
                                        int at = from.indexOf('@');
                                        String domain = (at < 0 ? from : from.substring(at));
                                        editor.putBoolean(from + (full ? ".show_full" : ".show_images"),
                                                cbNotAgainSender.isChecked());
                                        editor.putBoolean(domain + (full ? ".show_full" : ".show_images"),
                                                cbNotAgainSender.isChecked() && cbNotAgainDomain.isChecked());
                                    }

                                editor.putBoolean(full ? "ask_html" : "ask_images", !cbNotAgain.isChecked());

                                editor.apply();
                            }

                            onShowConfirmed(message, full, true);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            if (!EntityFolder.JUNK.equals(message.folderType))
                builder.setNeutralButton(R.string.title_setup, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(context, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("tab", "privacy"));
                    }
                });

            Dialog dialog = builder.create();

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
                        onViewRecycled(ViewHolder.this);
                        dialog.dismiss();
                        owner.getLifecycle().removeObserver(this);
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

            properties.setSize(message.id, null);
            properties.setHeight(message.id, null);
            properties.setPosition(message.id, null, null);

            if (full)
                setupTools(message, false, false);

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

        private void onShowAmp(final TupleMessageEx message) {
            boolean open_amp_confirmed = prefs.getBoolean("open_amp_confirmed", false);
            if (open_amp_confirmed)
                onShowAmpConfirmed(message);
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                View dview = inflater.inflate(R.layout.dialog_ask_amp, null, false);
                final TextView tvRemark = dview.findViewById(R.id.tvRemark);
                final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

                tvRemark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.view(v.getContext(), Uri.parse("https://amp.dev/about/email/"), true);
                    }
                });

                new AlertDialog.Builder(context)
                        .setView(dview)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (cbNotAgain.isChecked())
                                    prefs.edit().putBoolean("open_amp_confirmed", true).apply();
                                onShowAmpConfirmed(message);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        }

        private void onShowAmpConfirmed(final TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<EntityAttachment>() {
                @Override
                protected EntityAttachment onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    if (attachments == null)
                        return null;

                    for (EntityAttachment attachment : attachments)
                        if ("text/x-amp-html".equals(attachment.type))
                            return attachment;

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, EntityAttachment attachment) {
                    if (attachment == null)
                        return;

                    context.startActivity(new Intent(context, ActivityAMP.class)
                            .setData(attachment.getUri(context))
                            .putExtra("id", attachment.message));
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:amp");
        }

        private void onActionOpenFull(final TupleMessageEx message) {
            boolean junk = EntityFolder.JUNK.equals(message.folderType);
            boolean open_full_confirmed = prefs.getBoolean("open_full_confirmed", false);
            if (open_full_confirmed && !junk)
                onActionOpenFullConfirmed(message);
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                View dview = inflater.inflate(R.layout.dialog_ask_full, null, false);
                final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

                cbNotAgain.setVisibility(junk ? View.GONE : View.VISIBLE);

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
            boolean force_light = properties.getValue("force_light", message.id);

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("force_light", force_light);

            new SimpleTask<String>() {
                @Override
                protected String onExecute(Context context, Bundle args) throws Throwable {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean disable_tracking = prefs.getBoolean("disable_tracking", true);
                    boolean monospaced_pre = prefs.getBoolean("monospaced_pre", false);

                    long id = args.getLong("id");
                    File file = EntityMessage.getFile(context, id);
                    Document document = JsoupEx.parse(file);
                    HtmlHelper.cleanup(document);

                    if (message.isPlainOnly() && monospaced_pre)
                        HtmlHelper.restorePre(document);
                    HtmlHelper.guessSchemes(document);
                    HtmlHelper.autoLink(document);
                    HtmlHelper.embedInlineImages(context, message.id, document, true);
                    HtmlHelper.markText(document);
                    if (disable_tracking)
                        HtmlHelper.removeTrackingPixels(context, document);

                    return document.html();
                }

                @Override
                protected void onExecuted(Bundle args, String html) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean overview_mode = prefs.getBoolean("overview_mode", false);
                    boolean safe_browsing = prefs.getBoolean("safe_browsing", false);

                    Bundle fargs = new Bundle();
                    fargs.putString("html", html);
                    fargs.putBoolean("overview_mode", overview_mode);
                    fargs.putBoolean("safe_browsing", safe_browsing);
                    fargs.putBoolean("force_light", args.getBoolean("force_light"));

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
            if (message.unsubscribe.startsWith(MessageHelper.ONE_CLICK_UNSUBSCRIBE)) {
                Bundle args = new Bundle();
                args.putString("uri", message.unsubscribe.substring(MessageHelper.ONE_CLICK_UNSUBSCRIBE.length()));
                args.putString("from", MessageHelper.formatAddresses(message.from));

                FragmentDialogUnsubscribe fragment = new FragmentDialogUnsubscribe();
                fragment.setArguments(args);
                fragment.show(parentFragment.getParentFragmentManager(), "unsubscribe");
            } else {
                Uri uri = Uri.parse(message.unsubscribe);
                onOpenLink(uri,
                        context.getString(R.string.title_legend_show_unsubscribe),
                        EntityFolder.JUNK.equals(message.folderType));
            }
        }

        private void onActionVerifyDecrypt(TupleMessageEx message, boolean auto) {
            boolean inline = properties.getValue("inline_encrypted", message.id);
            int encrypt = (message.encrypt == null || inline ? EntityMessage.PGP_SIGNENCRYPT /* Inline */ : message.encrypt);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(FragmentMessages.ACTION_VERIFYDECRYPT)
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

        private void onActionEditSubject(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putString("subject", message.subject);

            FragmentDialogEditSubject fragment = new FragmentDialogEditSubject();
            fragment.setArguments(args);
            fragment.setTargetFragment(parentFragment, FragmentMessages.REQUEST_EDIT_SUBJECT);
            fragment.show(parentFragment.getParentFragmentManager(), "message:subject");
        }

        private void onActionMove(TupleMessageEx message, final boolean copy) {
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
                        onActionMove(message, copy, message.account,
                                new long[]{message.folder, inbox == null ? -1L : inbox.id});
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }.execute(context, owner, args, "move:pop");
            } else
                onActionMove(message, copy, message.account, new long[]{message.folder});
        }

        private void onActionMove(TupleMessageEx message, final boolean copy, long account, long[] disabled) {
            if (parentFragment == null)
                return;

            Bundle args = new Bundle();
            args.putInt("icon", copy ? R.drawable.twotone_file_copy_24 : R.drawable.twotone_drive_file_move_24);
            args.putString("title", context.getString(copy ? R.string.title_copy_to : R.string.title_move_to_folder));
            args.putLong("account", account);
            args.putLongArray("disabled", disabled);
            args.putLong("message", message.id);
            args.putBoolean("copy", copy);
            args.putBoolean("cancopy", true);
            args.putBoolean("similar", false);

            FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
            fragment.setArguments(args);
            fragment.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_MOVE);
            fragment.show(parentFragment.getParentFragmentManager(), "message:move");
        }

        private void onActionMoveAccount(TupleMessageEx message, View anchor) {
            Bundle args = new Bundle();

            new SimpleTask<List<EntityAccount>>() {
                @Override
                protected List<EntityAccount> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);
                    return db.account().getSynchronizingAccounts(EntityAccount.TYPE_IMAP);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                    if (accounts == null)
                        return;

                    PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, owner, anchor);

                    int order = 0;
                    for (EntityAccount account : accounts) {
                        String title = context.getString(R.string.title_move_to_account, account.name);
                        SpannableString ss = new SpannableString(title);
                        if (account.name != null && account.color != null) {
                            int i = title.indexOf(account.name);
                            int first = title.codePointAt(i);
                            int count = Character.charCount(first);
                            ss.setSpan(new ForegroundColorSpan(account.color), i, i + count, 0);
                        }
                        popupMenu.getMenu().add(Menu.NONE, R.string.title_move_to_account, order++, ss)
                                .setIntent(new Intent().putExtra("account", account.id));
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            long account = item.getIntent().getLongExtra("account", -1);
                            long[] disabled = (message.account.equals(account) ? new long[]{account} : new long[]{});
                            onActionMove(message, false, account, disabled);
                            return true;
                        }
                    });

                    popupMenu.show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:amove");
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

        private void onActionDeleteAttachments(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            FragmentDialogDeleteAttachments fragment = new FragmentDialogDeleteAttachments();
            fragment.setArguments(args);
            fragment.show(parentFragment.getParentFragmentManager(), "attachments:delete");
        }

        private void onActionDelete(TupleMessageEx message) {
            boolean leaveDeleted =
                    (message.accountProtocol == EntityAccount.TYPE_POP &&
                            message.accountLeaveDeleted);

            Bundle aargs = new Bundle();
            if (leaveDeleted)
                aargs.putString("question", context.getResources()
                        .getQuantityString(R.plurals.title_moving_messages, 1, 1));
            else
                aargs.putString("question", context.getString(R.string.title_ask_delete));
            aargs.putString("remark", message.getRemark());
            aargs.putLong("id", message.id);
            aargs.putInt("faq", 160);
            aargs.putString("notagain", "delete_asked");
            if (!leaveDeleted)
                aargs.putString("accept", context.getString(R.string.title_ask_delete_accept));
            aargs.putBoolean("warning", true);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean delete_asked = prefs.getBoolean("delete_asked", false);
            if (delete_asked) {
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
            if (message.accountProtocol == EntityAccount.TYPE_POP) {
                Bundle aargs = new Bundle();
                aargs.putLongArray("ids", new long[]{message.id});

                FragmentDialogBlockSender ask = new FragmentDialogBlockSender();
                ask.setArguments(aargs);
                ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_BLOCK_SENDERS);
                ask.show(parentFragment.getParentFragmentManager(), "message:block");
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
                ask.setTargetFragment(parentFragment, FragmentMessages.REQUEST_MESSAGE_JUNK);
                ask.show(parentFragment.getParentFragmentManager(), "message:junk");
            }
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
            boolean dark = Helper.isDarkTheme(context);
            boolean force_light = properties.getValue("force_light", message.id);

            boolean experiments = prefs.getBoolean("experiments", false);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibMore == null ? view : ibMore);
            popupMenu.inflate(R.menu.popup_message_more);

            popupMenu.getMenu().findItem(R.id.menu_unseen)
                    .setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen)
                    .setIcon(message.ui_seen ? R.drawable.twotone_mail_24 : R.drawable.twotone_drafts_24)
                    .setEnabled(message.uid != null ||
                            message.accountProtocol != EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_hide)
                    .setTitle(message.ui_snoozed == null ? R.string.title_hide : R.string.title_unhide)
                    .setIcon(message.ui_snoozed == null ? R.drawable.twotone_visibility_off_24 : R.drawable.twotone_visibility_24);

            popupMenu.getMenu().findItem(R.id.menu_flag_color)
                    .setVisible(flags || message.ui_flagged)
                    .setEnabled(message.uid != null ||
                            message.accountProtocol != EntityAccount.TYPE_IMAP);

            int i = (message.importance == null ? EntityMessage.PRIORITIY_NORMAL : message.importance);
            boolean can = (message.uid != null || message.accountProtocol == EntityAccount.TYPE_POP);
            popupMenu.getMenu().findItem(R.id.menu_set_importance_low).setEnabled(can && !EntityMessage.PRIORITIY_LOW.equals(i));
            popupMenu.getMenu().findItem(R.id.menu_set_importance_normal).setEnabled(can && !EntityMessage.PRIORITIY_NORMAL.equals(i));
            popupMenu.getMenu().findItem(R.id.menu_set_importance_high).setEnabled(can && !EntityMessage.PRIORITIY_HIGH.equals(i));

            popupMenu.getMenu().findItem(R.id.menu_edit_subject)
                    .setEnabled(message.uid != null && !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP &&
                            experiments);

            popupMenu.getMenu().findItem(R.id.menu_move_to)
                    .setEnabled(message.uid != null && !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_copy_to)
                    .setEnabled(message.uid != null && !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_delete_attachments)
                    .setEnabled(message.uid != null && !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP && !Helper.isPlayStoreInstall());

            popupMenu.getMenu().findItem(R.id.menu_delete)
                    .setEnabled(message.uid == null || !message.folderReadOnly)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_create_rule).setVisible(!message.folderReadOnly);

            popupMenu.getMenu().findItem(R.id.menu_manage_keywords)
                    .setEnabled(message.uid != null)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP);

            popupMenu.getMenu().findItem(R.id.menu_search_in_text).setEnabled(message.content && !full);
            popupMenu.getMenu().findItem(R.id.menu_translate).setVisible(
                    DeepL.isAvailable(context) && message.content);
            popupMenu.getMenu().findItem(R.id.menu_summarize).setVisible(
                    AI.isAvailable(context) && message.content);

            popupMenu.getMenu().findItem(R.id.menu_force_light).setVisible(full && dark);
            popupMenu.getMenu().findItem(R.id.menu_force_light).setChecked(force_light);

            popupMenu.getMenu().findItem(R.id.menu_share).setEnabled(message.content);
            popupMenu.getMenu().findItem(R.id.menu_share_link).setVisible(BuildConfig.DEBUG);
            popupMenu.getMenu().findItem(R.id.menu_pin).setVisible(pin);
            popupMenu.getMenu().findItem(R.id.menu_event).setEnabled(message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setEnabled(hasWebView && message.content);
            popupMenu.getMenu().findItem(R.id.menu_print).setVisible(Helper.canPrint(context));

            popupMenu.getMenu().findItem(R.id.menu_show_headers).setChecked(show_headers);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setEnabled(message.uid != null ||
                    (message.accountProtocol == EntityAccount.TYPE_POP && message.headers != null));

            popupMenu.getMenu().findItem(R.id.menu_show_html).setVisible(hasWebView && message.content);

            boolean popReload = (message.accountProtocol == EntityAccount.TYPE_POP &&
                    message.accountLeaveOnServer &&
                    EntityFolder.INBOX.equals(message.folderType));

            popupMenu.getMenu().findItem(R.id.menu_raw_save).setEnabled(message.uid != null || popReload);
            popupMenu.getMenu().findItem(R.id.menu_raw_send_message).setEnabled(message.uid != null || popReload);
            popupMenu.getMenu().findItem(R.id.menu_raw_send_thread).setEnabled(message.uid != null || popReload);

            popupMenu.getMenu().findItem(R.id.menu_thread_info)
                    .setVisible(BuildConfig.DEBUG || debug);
            popupMenu.getMenu().findItem(R.id.menu_jsoup)
                    .setVisible(BuildConfig.DEBUG || debug)
                    .setEnabled(message.content);

            popupMenu.getMenu().findItem(R.id.menu_resync)
                    .setEnabled(message.uid != null ||
                            message.accountProtocol == EntityAccount.TYPE_POP)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP || popReload);
            popupMenu.getMenu().findItem(R.id.menu_charset)
                    .setEnabled(message.uid != null ||
                            message.accountProtocol == EntityAccount.TYPE_POP)
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP || popReload);
            popupMenu.getMenu().findItem(R.id.menu_alternative)
                    .setTitle(message.isPlainOnly()
                            ? R.string.title_alternative_html : R.string.title_alternative_text)
                    .setEnabled(message.hasAlt() && !message.isEncrypted() &&
                            (message.uid != null || message.accountProtocol == EntityAccount.TYPE_POP))
                    .setVisible(message.accountProtocol == EntityAccount.TYPE_IMAP || popReload);

            popupMenu.insertIcons(context);

            MenuCompat.setGroupDividerEnabled(popupMenu.getMenu(), true);

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
                        onToggleSeen(message);
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
                    } else if (itemId == R.id.menu_edit_subject) {
                        onActionEditSubject(message);
                        return true;
                    } else if (itemId == R.id.menu_move_to) {
                        onActionMove(message, false);
                        return true;
                    } else if (itemId == R.id.menu_copy_to) {
                        onActionMove(message, true);
                        return true;
                    } else if (itemId == R.id.menu_delete_attachments) {
                        onActionDeleteAttachments(message);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        onActionDelete(message);
                        return true;
                    } else if (itemId == R.id.menu_edit_notes) {
                        onMenuNotes(message);
                        return true;
                    } else if (itemId == R.id.menu_create_rule) {
                        onMenuCreateRule(message);
                        return true;
                    } else if (itemId == R.id.menu_manage_keywords) {
                        onMenuManageKeywords(message);
                        return true;
                    } else if (itemId == R.id.menu_search_in_text) {
                        onSearchText(message);
                        return true;
                    } else if (itemId == R.id.menu_translate) {
                        onActionTranslate(message);
                        return true;
                    } else if (itemId == R.id.menu_summarize) {
                        onActionSummarize(message, null);
                        return true;
                    } else if (itemId == R.id.menu_force_light) {
                        onActionForceLight(message);
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
                    } else if (itemId == R.id.menu_share_link) {
                        onMenuShareLink(message);
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
                    } else if (itemId == R.id.menu_show_html) {
                        onMenuShareHtml(message);
                        return true;
                    } else if (itemId == R.id.menu_raw_save) {
                        onMenuRawSave(message);
                        return true;
                    } else if (itemId == R.id.menu_raw_send_message) {
                        onMenuRawSend(message, false);
                        return true;
                    } else if (itemId == R.id.menu_raw_send_thread) {
                        onMenuRawSend(message, true);
                        return true;
                    } else if (itemId == R.id.menu_thread_info) {
                        onMenuThreadInfo(message);
                        return true;
                    } else if (itemId == R.id.menu_jsoup) {
                        onMenuJsoup(message);
                        return true;
                    } else if (itemId == R.id.menu_reset_questions) {
                        onMenuResetQuestions(message);
                        return true;
                    } else if (itemId == R.id.menu_resync) {
                        onMenuResync(message);
                        return true;
                    } else if (itemId == R.id.menu_charset) {
                        onMenuCharset(message);
                        return true;
                    } else if (itemId == R.id.menu_alternative) {
                        onMenuAlt(message);
                        return true;
                    } else if (itemId == R.id.menu_log) {
                        onMenuLog(message);
                        return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }

        private void onActionShowInfra(TupleMessageEx message) {
            String resname = "infra_" + message.infrastructure;
            int resid = context.getResources()
                    .getIdentifier(resname, "string", context.getPackageName());
            String infra = (resid < 0 ? message.infrastructure : context.getString(resid));
            ToastEx.makeText(context, infra, Toast.LENGTH_LONG).show();
        }

        private boolean onOpenLink(Uri uri, String title, boolean always_confirm) {
            Log.i("Opening uri=" + uri + " title=" + title + " always confirm=" + always_confirm);

            try {
                uri = Uri.parse(uri.toString().trim().replaceAll("[\r\n]", ""));
                if (UriHelper.isHyperLink(uri))
                    uri = Uri.parse(uri.toString().replaceAll("\\s+", "+"));

                if (ProtectedContent.isProtectedContent(uri)) {
                    Bundle args = new Bundle();
                    args.putParcelable("uri", uri);

                    FragmentDialogBase dialog = new ProtectedContent.FragmentDialogDecrypt();
                    dialog.setArguments(args);
                    dialog.show(parentFragment.getParentFragmentManager(), "decrypt");
                    return true;
                }

                try {
                    String url = uri.getQueryParameter("url");
                    if (!TextUtils.isEmpty(url)) {
                        Uri alt = Uri.parse(url);
                        if (isActivate(alt))
                            uri = alt;
                    }

                    Uri sanitized = UriHelper.sanitize(context, uri);
                    if (sanitized != null && isActivate(sanitized))
                        uri = sanitized;
                    else if (title != null) {
                        Uri alt = Uri.parse(title);
                        if (isActivate(alt))
                            uri = alt;
                    }
                } catch (Throwable ignored) {
                }

                if (isActivate(uri)) {
                    try {
                        if (ActivityBilling.activatePro(context, uri))
                            ToastEx.makeText(context, R.string.title_pro_valid, Toast.LENGTH_LONG).show();
                        else {
                            Uri invalid = Uri.parse(BuildConfig.PRO_FEATURES_URI + "invalid.html" +
                                    "?challenge=" + ActivityBilling.getChallenge(context) +
                                    "&version=" + BuildConfig.VERSION_CODE);
                            Helper.view(context, invalid, true);
                        }
                    } catch (NoSuchAlgorithmException ex) {
                        Log.e(ex);
                        ToastEx.makeText(context, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String scheme = uri.getScheme();
                    if ("full".equals(scheme)) {
                        TupleMessageEx message = getMessage();
                        if (message != null)
                            onShow(message, true);
                        return (message != null);
                    } else if ("external".equals(scheme)) {
                        TupleMessageEx message = getMessage();
                        if (message == null)
                            return false;

                        Bundle args = new Bundle();
                        args.putLong("id", message.id);

                        new SimpleTask<Uri>() {
                            @Override
                            protected Uri onExecute(Context context, Bundle args) throws Throwable {
                                long id = args.getLong("id");

                                File source = EntityMessage.getFile(context, id);

                                File dir = Helper.ensureExists(context, "shared");
                                File target = new File(dir, id + ".html");

                                Helper.copy(source, target);

                                return FileProviderEx.getUri(context, BuildConfig.APPLICATION_ID, target);
                            }

                            @Override
                            protected void onExecuted(Bundle args, Uri uri) {
                                Helper.share(context, uri, "text/html", null);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                            }
                        }.execute(context, owner, args, "message:external");

                        return true;
                    }

                    if ("cid".equals(uri.getScheme()) || "data".equals(uri.getScheme()))
                        return false;

                    boolean confirm_links = prefs.getBoolean("confirm_links", true);

                    String chost = FragmentDialogOpenLink.getConfirmHost(uri);
                    boolean confirm_link = (chost == null || prefs.getBoolean(chost + ".confirm_link", true));
                    if (always_confirm || (confirm_links && confirm_link)) {
                        if (parentFragment == null)
                            return false;

                        Bundle args = new Bundle();
                        args.putParcelable("uri", uri);
                        args.putString("title", title);
                        args.putBoolean("always_confirm", always_confirm);

                        FragmentDialogOpenLink fragment = new FragmentDialogOpenLink();
                        fragment.setArguments(args);
                        fragment.show(parentFragment.getParentFragmentManager(), "open:link");
                    } else {
                        boolean link_view = prefs.getBoolean(chost + ".link_view", false);
                        boolean link_sanitize = prefs.getBoolean(chost + ".link_sanitize", false);

                        if (link_sanitize && UriHelper.isHyperLink(uri)) {
                            Uri sanitized = UriHelper.sanitize(context, uri);
                            if (sanitized != null)
                                uri = sanitized;
                            Log.i("Open sanitized=" + uri);
                        }

                        if (link_view) {
                            Log.i("Open view=" + uri);
                            Intent view = new Intent(Intent.ACTION_VIEW, UriHelper.fix(uri));
                            Intent chooser = Intent.createChooser(view, context.getString(R.string.title_select_app));
                            try {
                                context.startActivity(chooser);
                            } catch (ActivityNotFoundException ex) {
                                Log.w(ex);
                                Helper.view(context, uri, true, true);
                            }
                        } else {
                            boolean tabs = prefs.getBoolean("open_with_tabs", true);
                            Helper.view(context, UriHelper.guessScheme(uri), !tabs, !tabs);
                        }
                    }
                }

                return true;
            } catch (Throwable ex) {
                /*
                    Exception java.lang.NullPointerException:
                      at eu.faircode.email.AdapterMessage$ViewHolder.onOpenLink (AdapterMessage.java:6506)
                      at eu.faircode.email.AdapterMessage$ViewHolder.access$900 (AdapterMessage.java:354)
                      at eu.faircode.email.AdapterMessage$ViewHolder$2$1.onClick (AdapterMessage.java:670)
                      at eu.faircode.email.AdapterMessage$ViewHolder$2$1.onLongPress (AdapterMessage.java:632)
                      at android.view.GestureDetector.dispatchLongPress (GestureDetector.java:1014)
                      at android.view.GestureDetector.-$$Nest$mdispatchLongPress
                      at android.view.GestureDetector$GestureHandler.handleMessage (GestureDetector.java:358)
                      at android.os.Handler.dispatchMessage (Handler.java:106)
                 */
                Log.e(ex);
                return false;
            }
        }

        private boolean isActivate(Uri uri) {
            if ("eu.faircode.email".equals(uri.getHost()))
                return true;
            return ("email.faircode.eu".equals(uri.getHost()) &&
                    "/activate/".equals(uri.getPath()));
        }

        private void onOpenImage(long id, @NonNull String source, boolean always_confirm) {
            Log.i("Viewing image source=" + source);

            Uri uri = Uri.parse(source);
            String scheme = uri.getScheme();

            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putString("source", source);
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

            else if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                onOpenLink(uri, null, always_confirm);

            } else if ("data".equals(scheme))
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
                Helper.reportNoViewer(context, uri, null);
        }

        private void onToggleSeen(final TupleMessageEx message) {
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

                    if (!message.ui_seen && autoclose_unseen)
                        properties.finish();
                    else if (collapse_marked)
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
            if (message.ui_snoozed != null)
                args.putLong("time", message.ui_snoozed);
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
            args.putInt("faq", 187);

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
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        db.message().setMessageImportance(message.id, importance);

                        EntityOperation.queue(context, message, EntityOperation.KEYWORD,
                                MessageHelper.FLAG_LOW_IMPORTANCE, EntityMessage.PRIORITIY_LOW.equals(importance));
                        EntityOperation.queue(context, message, EntityOperation.KEYWORD,
                                MessageHelper.FLAG_HIGH_IMPORTANCE, EntityMessage.PRIORITIY_HIGH.equals(importance));

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
            }.execute(context, owner, args, "importance:set");
        }

        private void onMenuThreadInfo(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<List<EntityMessage>>() {
                @Override
                protected List<EntityMessage> onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    Map<String, EntityMessage> map = new HashMap<>();

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    if (!TextUtils.isEmpty(message.inreplyto))
                        for (EntityMessage m : db.message().getMessagesByMsgId(message.account, message.inreplyto))
                            map.put(m.msgid, m);

                    if (!TextUtils.isEmpty(message.references))
                        for (String ref : message.references.split(" "))
                            for (EntityMessage m : db.message().getMessagesByMsgId(message.account, ref))
                                map.put(m.msgid, m);

                    return new ArrayList<>(map.values());
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityMessage> referenced) {
                    DateFormat DTF = Helper.getDateTimeInstance(context);

                    SpannableStringBuilder ssb = new SpannableStringBuilderEx();

                    int start = 0;
                    ssb.append("Message-ID: ");
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                    ssb.append(message.msgid).append("\n");

                    if (!TextUtils.isEmpty(message.inreplyto)) {
                        start = ssb.length();
                        ssb.append("In-reply-to: ");
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                        ssb.append(message.inreplyto).append("\n");
                    }

                    if (!TextUtils.isEmpty(message.references)) {
                        start = ssb.length();
                        ssb.append("References: ");
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                        ssb.append("\n");
                        for (String ref : message.references.split(" "))
                            ssb.append(ref).append("\n");
                    }

                    start = ssb.length();
                    ssb.append("Thread: ");
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                    ssb.append(message.thread).append("\n");

                    ssb.append("\n");

                    if (referenced != null)
                        for (EntityMessage ref : referenced) {
                            start = ssb.length();
                            ssb.append(ref.msgid).append(": ");
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                            ssb.append(DTF.format(ref.received)).append(' ')
                                    .append(ref.subject == null ? "" : ref.subject)
                                    .append("\n\n");
                        }

                    ssb.setSpan(new RelativeSizeSpan(HtmlHelper.FONT_SMALL), 0, ssb.length(), 0);

                    new AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.title_thread_info))
                            .setMessage(ssb)
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:resync");
        }

        private void onMenuJsoup(TupleMessageEx message) {
            if (!tvBody.hasSelection())
                return;

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putString("selected",
                    tvBody.getText().subSequence(tvBody.getSelectionStart(), tvBody.getSelectionEnd()).toString());

            new SimpleTask<List<String>>() {
                @Override
                protected List<String> onExecute(Context context, Bundle args) throws IOException {
                    long id = args.getLong("id");
                    String selected = args.getString("selected");

                    Map<String, EntityMessage> map = new HashMap<>();

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null || !message.content)
                        return null;

                    Document d = JsoupEx.parse(message.getFile(context));

                    List<String> result = new ArrayList<>();
                    for (Element element : d.select(String.format("*:containsOwn(%s)", selected))) {
                        List<String> path = new ArrayList<>();
                        Element e = element;
                        while (e != null && e.parent() != null /* skip #root */) {
                            int index = 0;
                            for (Element p : e.previousElementSiblings())
                                if (Objects.equals(e.tagName(), p.tagName()))
                                    index++;
                            path.add(String.format("%s:eq(%d)", e.tagName(), index));
                            e = e.parent();
                        }
                        Collections.reverse(path);
                        result.add(TextUtils.join(" > ", path));
                    }

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, List<String> jsoup) {
                    if (jsoup != null && jsoup.size() > 0) {
                        ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
                        if (clipboard == null)
                            return;

                        ClipData clip = ClipData.newPlainText(
                                context.getString(R.string.app_name),
                                jsoup.get(0));
                        clipboard.setPrimaryClip(clip);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                            ToastEx.makeText(context, jsoup.get(0), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:jsoup");
        }

        private void onMenuResetQuestions(TupleMessageEx message) {
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

                    SharedPreferences.Editor editor = prefs.edit();

                    List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                    if (attachments != null)
                        for (EntityAttachment attachment : attachments) {
                            String ext = Helper.getExtension(attachment.name);
                            if (ext == null)
                                continue;
                            editor.remove(ext.toLowerCase(Locale.ROOT) + ".confirm_files");
                        }

                    if (message.content) {
                        Document d = JsoupEx.parse(message.getFile(context));
                        for (Element a : d.select("a")) {
                            String href = a.attr("href");
                            if (TextUtils.isEmpty(href))
                                continue;

                            Uri uri = Uri.parse(href);
                            if (uri == null || !UriHelper.isHyperLink(uri))
                                continue;

                            String host = uri.getHost();
                            if (TextUtils.isEmpty(host))
                                continue;

                            editor.remove(host + ".confirm_link");
                        }
                    }

                    editor.apply();

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    ToastEx.makeText(context, R.string.title_completed, Toast.LENGTH_LONG).show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "reset");
        }

        private void onMenuResync(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    EntityAccount account;
                    EntityFolder folder;

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        account = db.account().getAccount(message.account);
                        if (account == null)
                            return null;

                        folder = db.folder().getFolder(message.folder);
                        if (folder == null)
                            return null;

                        if (message.uid == null && account.protocol == EntityAccount.TYPE_IMAP)
                            return null;
                        if (account.protocol == EntityAccount.TYPE_POP && !EntityFolder.INBOX.equals(folder.type))
                            return null;

                        db.message().deleteMessage(id);

                        if (account.protocol == EntityAccount.TYPE_IMAP)
                            EntityOperation.queue(context, folder, EntityOperation.FETCH, message.uid);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    if (account.protocol == EntityAccount.TYPE_IMAP)
                        ServiceSynchronize.eval(context, "resync");
                    else
                        EntityOperation.sync(context, folder.id, true);

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

        private void onMenuCharset(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<SortedMap<String, Charset>>() {
                @Override
                protected SortedMap<String, Charset> onExecute(Context context, Bundle args) {
                    return Charset.availableCharsets();
                }

                @Override
                protected void onExecuted(Bundle args, SortedMap<String, Charset> charsets) {
                    PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, ibMore);

                    popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.title_charset_auto)
                            .setIntent(new Intent().putExtra("charset", (String) null));

                    int order = 0;
                    for (String name : charsets.keySet()) {
                        order++;
                        popupMenu.getMenu().add(Menu.NONE, order, order, name)
                                .setIntent(new Intent().putExtra("charset", name));
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            properties.setSize(message.id, null);
                            properties.setHeight(message.id, null);
                            properties.setPosition(message.id, null, null);

                            args.putString("charset", item.getIntent().getStringExtra("charset"));

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    long id = args.getLong("id");
                                    String charset = args.getString("charset");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        EntityMessage message = db.message().getMessage(id);
                                        if (message == null)
                                            return null;

                                        db.message().resetMessageContent(message.id);
                                        EntityOperation.queue(context, message, EntityOperation.BODY, null, charset);

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
                            }.execute(context, owner, args, "body:charset");

                            return true;
                        }
                    });

                    popupMenu.show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:charset");

        }

        private void onMenuAlt(TupleMessageEx message) {
            properties.setSize(message.id, null);
            properties.setHeight(message.id, null);
            properties.setPosition(message.id, null, null);

            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("plain", message.isPlainOnly());

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean plain = args.getBoolean("plain");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        EntityOperation.queue(context, message, EntityOperation.BODY, !plain);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

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
            }.execute(context, owner, args, "message:alt");
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
            if (DeepL.canTranslate(context)) {
                Bundle args = new Bundle();
                args.putLong("id", message.id);

                FragmentDialogTranslate fragment = new FragmentDialogTranslate();
                fragment.setArguments(args);
                fragment.show(parentFragment.getParentFragmentManager(), "message:translate");
            } else {
                DeepL.FragmentDialogDeepL fragment = new DeepL.FragmentDialogDeepL();
                fragment.show(parentFragment.getParentFragmentManager(), "deepl:configure");
            }
        }

        private void onActionSummarize(TupleMessageEx message, View anchor) {
            FragmentDialogSummarize.summarize(message, parentFragment.getParentFragmentManager(), anchor, owner);
        }

        private void onActionForceLight(TupleMessageEx message) {
            if (canDarken || fake_dark) {
                boolean force_light = !properties.getValue("force_light", message.id);
                properties.setValue("force_light", message.id, force_light);
                ibForceLight.setImageLevel(force_light ? 1 : 0);
                bindBody(message, false);
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && false) {
                    View view = LayoutInflater.from(context).inflate(R.layout.dialog_dark, null);
                    final Button btnIssue = view.findViewById(R.id.btnIssue);

                    btnIssue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Helper.viewFAQ(v.getContext(), 81);
                        }
                    });

                    new AlertDialog.Builder(context)
                            .setView(view)
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                } else {
                    Intent update = new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(Helper.PACKAGE_WEBVIEW))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(update);
                }
            }
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
            args.putLongArray("ids", new long[]{message.id});

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

                    String link = message.getLink();

                    Document document = JsoupEx.parse(file);
                    HtmlHelper.truncate(document, HtmlHelper.MAX_SHARE_TEXT_SIZE);

                    Element a = document.createElement("a");
                    a.text(link);
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
                                Uri uri = FileProviderEx.getUri(context, BuildConfig.APPLICATION_ID, file, attachment.name);
                                uris.add(uri);
                            }

                        if (uris.size() > 0) {
                            if (uris.size() == 1)
                                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
                            else {
                                // Too many apps don't accept ACTION_SEND_MULTIPLE
                                ClipData clip = null;
                                ContentResolver resolver = context.getContentResolver();
                                for (Uri uri : uris) {
                                    if (clip == null)
                                        clip = ClipData.newUri(resolver, context.getString(R.string.app_name), uri);
                                    else
                                        clip.addItem(new ClipData.Item(uri));
                                }
                                intent.setClipData(clip);
                            }
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }

                    EntityLog.log(context, "Sharing " + intent +
                            " extras=" + TextUtils.join(", ", Log.getExtras(intent.getExtras())));

                    context.startActivity(intent);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "message:share");
        }

        private void onMenuShareLink(TupleMessageEx message) {
            String link = message.getLink();
            String title = context.getString(R.string.title_share_link_open);
            String html = "<a href=\"" + link + "\">" + Html.escapeHtml(title) + "<a/>";

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link);
            intent.putExtra(Intent.EXTRA_HTML_TEXT, html);
            context.startActivity(intent);
        }

        private void onMenuPin(TupleMessageEx message) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_shortcut_label, null);
            EditText etLabel = view.findViewById(R.id.etLabel);

            etLabel.setText(message.subject);

            new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String label = etLabel.getText().toString().trim();
                            if (!TextUtils.isEmpty(label))
                                _onMenuPin(message, label);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void _onMenuPin(TupleMessageEx message, String label) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putLong("account", message.account);
            args.putString("folderType", message.folderType);
            args.putString("selector", message.bimi_selector);
            args.putBoolean("dmarc", Boolean.TRUE.equals(message.dmarc));
            args.putSerializable("addresses", message.from);

            new SimpleTask<ContactInfo[]>() {
                @Override
                protected ContactInfo[] onExecute(Context context, Bundle args) {
                    long account = args.getLong("account");
                    String folderType = args.getString("folderType");
                    String selector = args.getString("selector");
                    boolean dmarc = args.getBoolean("dmarc");
                    Address[] addresses = (Address[]) args.getSerializable("addresses");
                    return ContactInfo.get(context, account, folderType, selector, dmarc, addresses);
                }

                @Override
                protected void onExecuted(Bundle args, ContactInfo[] contactInfo) {
                    ShortcutInfoCompat.Builder builder =
                            Shortcuts.getShortcut(context, message, label, contactInfo);
                    Shortcuts.requestPinShortcut(context, builder.build());
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
            args.putCharSequence("selected", getSelectedText());

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
            ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
            if (clipboard == null)
                return;

            ClipData clip = ClipData.newPlainText(context.getString(R.string.title_show_headers), message.headers);
            clipboard.setPrimaryClip(clip);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
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
                        long id = args.getLong("id");

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
            context.startActivity(new Intent(context, ActivityCode.class)
                    .putExtra("id", message.id)
                    .putExtra("selected", getSelectedText()));
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

        private void onMenuRawSend(TupleMessageEx message, boolean threads) {
            Bundle args = new Bundle();
            args.putLongArray("ids", new long[]{message.id});
            args.putBoolean("threads", threads);

            FragmentDialogForwardRaw ask = new FragmentDialogForwardRaw();
            ask.setArguments(args);
            ask.show(parentFragment.getParentFragmentManager(), "message:raw");
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

        private void onMenuLog(TupleMessageEx message) {
            if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                parentFragment.getParentFragmentManager().popBackStack("logs", FragmentManager.POP_BACK_STACK_INCLUSIVE);

            Bundle args = new Bundle();
            args.putLong("message", message.id);

            Fragment fragment = new FragmentLogs();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = parentFragment.getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("logs");
            fragmentTransaction.commit();
        }

        private Spanned getLabels(TupleMessageEx message) {
            if (!labels_header)
                return null;
            if (message.labels == null || message.labels.length == 0)
                return null;

            SpannableStringBuilder ssb = new SpannableStringBuilderEx();
            for (int i = 0; i < message.labels.length; i++) {
                if (ssb.length() > 0)
                    ssb.append(' ');

                String label = message.labels[i];
                ssb.append(label);

                if (message.label_colors == null)
                    continue;
                if (i >= message.label_colors.length)
                    continue;
                if (message.label_colors[i] == null)
                    continue;

                int len = ssb.length();
                ssb.setSpan(new ForegroundColorSpan(message.label_colors[i]),
                        len - label.length(), len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return ssb;
        }

        private Spanned getKeywords(TupleMessageEx message) {
            if (message.keywords == null || message.keywords.length == 0)
                return null;

            SpannableStringBuilder ssb = new SpannableStringBuilderEx();
            for (int i = 0; i < message.keywords.length; i++) {
                String keyword = message.keywords[i];
                if (debug || MessageHelper.showKeyword(keyword)) {
                    if (ssb.length() > 0)
                        ssb.append(' ');

                    if (message.keyword_titles != null &&
                            i < message.keyword_titles.length &&
                            message.keyword_titles[i] != null)
                        keyword = message.keyword_titles[i];

                    ssb.append(keyword);

                    if (message.keyword_colors == null)
                        continue;
                    if (i >= message.keyword_colors.length)
                        continue;
                    if (message.keyword_colors[i] == null)
                        continue;

                    int len = ssb.length();
                    ssb.setSpan(
                            new ForegroundColorSpan(message.keyword_colors[i]),
                            len - keyword.length(), len,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            return (ssb.length() == 0 ? null : ssb);
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
            return new ItemDetailsMessage(this);
        }

        Long getKey() {
            return getKeyAtPosition(getAdapterPosition());
        }

        CharSequence getSelectedText() {
            if (tvBody == null)
                return null;

            int start = tvBody.getSelectionStart();
            int end = tvBody.getSelectionEnd();

            if (start < 0)
                start = 0;
            if (end < 0)
                end = 0;

            if (start == end)
                return null;

            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            }

            return tvBody.getText().subSequence(start, end);
        }

        private final View.AccessibilityDelegate accessibilityDelegateHeader = new View.AccessibilityDelegate() {
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

                try {
                    TupleMessageEx message = getMessage();
                    if (message == null)
                        return;

                    boolean expanded = properties.getValue("expanded", message.id);

                    ivBadge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    vwColor.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (ibExpander.getVisibility() == View.VISIBLE)
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibExpander,
                                context.getString(expanded ? R.string.title_accessibility_collapse : R.string.title_accessibility_expand)));
                    ibExpander.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibSeen,
                            context.getString(message.ui_seen ? R.string.title_unseen : R.string.title_seen)));

                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibAnswer,
                            context.getString(R.string.title_reply)));

                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibArchive,
                            context.getString(R.string.title_archive)));

                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibTrash,
                            context.getString(R.string.title_trash)));

                    if (properties.getSelectionCount() > 0)
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibDelete,
                                context.getString(R.string.title_trash_selection)));

                    if (ibAvatar != null) {
                        if (ibAvatar.getVisibility() == View.VISIBLE && ibAvatar.isEnabled())
                            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibAvatar,
                                    context.getString(R.string.title_accessibility_view_contact)));
                        ibAvatar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    }

                    if (ibFlagged != null) {
                        if (ibFlagged.getVisibility() == View.VISIBLE && ibFlagged.isEnabled()) {
                            int flagged = (message.count - message.unflagged);
                            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibFlagged,
                                    context.getString(flagged > 0 ? R.string.title_unflag : R.string.title_flag)));
                        }
                        ibFlagged.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    }

                    if (ibAuth != null) {
                        if (ibAuth.getVisibility() == View.VISIBLE)
                            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibAuth,
                                    context.getString(R.string.title_accessibility_show_authentication_result)));
                        ibAuth.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    }

                    if (ibSnoozed != null) {
                        if (ibSnoozed.getVisibility() == View.VISIBLE)
                            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibSnoozed,
                                    context.getString(R.string.title_accessibility_show_snooze_time)));
                        ibSnoozed.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    }

                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibMore,
                            context.getString(R.string.title_advanced_more)));
                    if (ibMore != null)
                        ibMore.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    if (ibError != null) {
                        if (ibError.getVisibility() == View.VISIBLE)
                            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.ibError,
                                    context.getString(R.string.title_accessibility_view_help)));
                        ibError.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    }

                    info.setContentDescription(populateContentDescription(message));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }

            @Override
            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                TupleMessageEx message = getMessage();
                if (message == null)
                    return false;

                if (action == R.id.ibExpander) {
                    onToggleMessage(message);
                    return true;
                } else if (action == R.id.ibSeen) {
                    onToggleSeen(message);
                    return true;
                } else if (action == R.id.ibAnswer) {
                    onActionAnswer(message, view);
                    return true;
                } else if (action == R.id.ibArchive) {
                    onActionArchive(message);
                    return true;
                } else if (action == R.id.ibTrash) {
                    onActionTrash(message, false);
                    return true;
                } else if (action == R.id.ibDelete) {
                    properties.moveSelection(EntityFolder.TRASH, false);
                    return true;
                } else if (action == R.id.ibAvatar) {
                    onViewContact(message);
                    return true;
                } else if (action == R.id.ibFlagged) {
                    onToggleFlag(message);
                    return true;
                } else if (action == R.id.ibAuth) {
                    onShowAuth(message, null);
                    return true;
                } else if (action == R.id.ibSnoozed) {
                    onShowSnoozed(message);
                    return true;
                } else if (action == R.id.ibMore) {
                    onActionMore(message);
                    return true;
                } else if (action == R.id.ibError) {
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

                if (tvCount.getVisibility() == View.VISIBLE) {
                    result.add(context.getResources().getQuantityString(
                            R.plurals.title_accessibility_messages, message.visible, message.visible));
                    tvCount.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ibExpander.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(
                            expanded ? R.string.title_accessibility_expanded : R.string.title_accessibility_collapsed));
                    ibExpander.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (message.drafts > 0)
                    result.add(context.getString(R.string.title_legend_draft));

                if (ivAnswered.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_accessibility_answered));
                    ivAnswered.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ivForwarded.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_accessibility_forwarded));
                    ivForwarded.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ibFlagged.getVisibility() == View.VISIBLE && ibFlagged.isEnabled()) {
                    int flagged = (message.count - message.unflagged);
                    if (flagged > 0)
                        result.add(context.getString(R.string.title_accessibility_flagged));
                }

                if (ibPriority.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(getPriority(message)));
                    ibPriority.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ibSensitivity.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(getSensitivity(message)));
                    ibSensitivity.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ivAttachments.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_accessibility_attachment));
                    ivAttachments.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (tvNotes.getVisibility() == View.VISIBLE) {
                    result.add(tvNotes.getText().toString());
                    tvNotes.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                boolean outgoing = isOutgoing(message);
                Address[] addresses = (EntityFolder.isOutgoing(message.folderType) &&
                        (viewType != ViewType.THREAD || !threading) ? message.to : message.senders);
                MessageHelper.AddressFormat format = email_format;
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
                tvFrom.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                tvSubject.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                tvTime.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                if (ibSigned.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_legend_signed) + " " + context.getString(getSigned(message)));
                    ibSigned.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ibEncrypted.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_legend_encrypted) + " " + context.getString(getEncrypted(message)));
                    ibEncrypted.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ibAuth.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_legend_auth));
                    ibAuth.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ivFound.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_legend_found));
                    ivFound.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (ibSnoozed.getVisibility() == View.VISIBLE) {
                    result.add(context.getString(R.string.title_legend_snoozed));
                    ibSnoozed.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (expanded) {
                    if (message.receipt_request != null && message.receipt_request)
                        result.add(context.getString(R.string.title_legend_receipt));
                    if (message.isPlainOnly())
                        result.add(context.getString(R.string.title_legend_plain_only));
                    if (message.ui_browsed)
                        result.add(context.getString(R.string.title_legend_browsed));
                }

                if (tvFolder.getVisibility() == View.VISIBLE) {
                    result.add(tvFolder.getText().toString());
                    tvFolder.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (tvLabels.getVisibility() == View.VISIBLE) {
                    result.add(tvLabels.getText().toString());
                    tvLabels.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (tvSize.getVisibility() == View.VISIBLE) {
                    result.add(tvSize.getText().toString());
                    tvSize.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (tvError.getVisibility() == View.VISIBLE) {
                    result.add(tvError.getText().toString());
                    tvError.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (tvExpand.getVisibility() == View.VISIBLE) {
                    result.add(tvExpand.getText().toString());
                    tvExpand.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (tvPreview.getVisibility() == View.VISIBLE) {
                    result.add(tvPreview.getText().toString());
                    tvPreview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

                if (tvKeywords.getVisibility() == View.VISIBLE) {
                    result.add(tvKeywords.getText().toString());
                    tvKeywords.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

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
                   String type, boolean found, String searched, boolean searchedPartial, ViewType viewType,
                   boolean compact, int zoom, boolean large_buttons, String sort, boolean ascending,
                   boolean filter_duplicates, boolean filter_trash,
                   final IProperties properties) {
        this.parentFragment = parentFragment;
        this.type = type;
        this.found = found;
        this.searched = searched;
        this.searchedPartial = searchedPartial;
        this.viewType = viewType;
        this.compact = compact;
        this.zoom = zoom;
        this.large_buttons = large_buttons;
        this.sort = sort;
        this.ascending = ascending;
        this.filter_duplicates = filter_duplicates;
        this.filter_trash = filter_trash;
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
        this.DTFS = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

        ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
        this.suitable = state.isSuitable();
        this.unmetered = state.isUnmetered();

        this.colorCardBackground = Helper.resolveColor(context, R.attr.colorCardBackground);
        boolean color_stripe_wide = prefs.getBoolean("color_stripe_wide", false);
        this.colorStripeWidth = Helper.dp2pixels(context, color_stripe_wide ? 12 : 6);
        this.colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
        this.textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        this.textColorTertiary = Helper.resolveColor(context, android.R.attr.textColorTertiary);
        this.textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);
        this.textColorHighlightInverse = Helper.resolveColor(context, android.R.attr.textColorHighlightInverse);

        boolean highlight_unread = prefs.getBoolean("highlight_unread", true);
        boolean highlight_subject = prefs.getBoolean("highlight_subject", false);
        this.colorUnreadHighlight = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorUnreadHighlight));

        this.colorUnread = (highlight_unread ? colorUnreadHighlight : Helper.resolveColor(context, R.attr.colorUnread));
        this.colorRead = Helper.resolveColor(context, R.attr.colorRead);
        this.colorSubject = Helper.resolveColor(context, highlight_subject ? R.attr.colorUnreadHighlight : R.attr.colorRead);
        this.colorVerified = Helper.resolveColor(context, R.attr.colorVerified);
        this.colorEncrypt = Helper.resolveColor(context, R.attr.colorEncrypt);
        this.colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
        this.colorBookmark = Helper.resolveColor(context, R.attr.colorBookmark);
        this.colorError = Helper.resolveColor(context, androidx.appcompat.R.attr.colorError);
        this.colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        this.colorControlNormal = Helper.resolveColor(context, androidx.appcompat.R.attr.colorControlNormal);

        this.hasWebView = Helper.hasWebView(context);
        this.pin = Shortcuts.can(context);
        this.contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        this.textSize = Helper.getTextSize(context, zoom);

        boolean contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        boolean avatars = prefs.getBoolean("avatars", true);
        boolean bimi = prefs.getBoolean("bimi", false);
        boolean gravatars = (prefs.getBoolean("gravatars", false) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean libravatars = (prefs.getBoolean("libravatars", false) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean favicons = prefs.getBoolean("favicons", false);
        boolean generated = prefs.getBoolean("generated_icons", true);

        this.date = prefs.getBoolean("date", true);
        this.date_week = prefs.getBoolean("date_week", false);
        this.date_fixed = (!date && prefs.getBoolean("date_fixed", false));
        this.date_time = prefs.getBoolean("date_time", false);
        this.cards = prefs.getBoolean("cards", true);
        this.shadow_unread = prefs.getBoolean("shadow_unread", false);
        this.shadow_border = prefs.getBoolean("shadow_border", true);
        this.shadow_highlight = prefs.getBoolean("shadow_highlight", false);
        this.tabular_unread_bg = prefs.getBoolean("tabular_unread_bg", true);
        this.threading = prefs.getBoolean("threading", true);
        this.threading_unread = threading && prefs.getBoolean("threading_unread", false);
        this.indentation = prefs.getBoolean("indentation", false);

        this.avatars = (contacts && avatars) || (bimi || gravatars || libravatars || favicons || generated);
        this.account_color = prefs.getInt("account_color", 1);
        this.check_authentication = prefs.getBoolean("check_authentication", true);
        this.native_dkim = prefs.getBoolean("native_dkim", false);
        this.check_tls = prefs.getBoolean("check_tls", true);
        this.check_reply_domain = prefs.getBoolean("check_reply_domain", true);
        this.check_mx = prefs.getBoolean("check_mx", false);
        this.check_blocklist = prefs.getBoolean("check_blocklist", false);
        this.show_addresses_default = prefs.getBoolean("addresses", false);
        this.hide_attachments_default = prefs.getBoolean("hide_attachments", false);

        this.email_format = MessageHelper.getAddressFormat(context);
        this.prefer_contact = prefs.getBoolean("prefer_contact", false);
        this.only_contact = prefs.getBoolean("only_contact", false);
        this.distinguish_contacts = prefs.getBoolean("distinguish_contacts", false);
        this.show_recipients = prefs.getBoolean("show_recipients", false);
        this.reverse_addresses = prefs.getBoolean("reverse_addresses", true);

        this.subject_top = prefs.getBoolean("subject_top", false);

        int fz_sender = prefs.getInt("font_size_sender", -1);
        if (fz_sender >= 0)
            font_size_sender = Helper.getTextSize(context, fz_sender);

        int fz_subject = prefs.getInt("font_size_subject", -1);
        if (fz_subject >= 0)
            font_size_subject = Helper.getTextSize(context, fz_subject);

        this.subject_italic = prefs.getBoolean("subject_italic", true);
        this.sender_ellipsize = prefs.getString("sender_ellipsize", "end");
        this.subject_ellipsize = prefs.getString("subject_ellipsize", "full");
        this.show_filtered = prefs.getBoolean("show_filtered", false);
        this.keywords_header = prefs.getBoolean("keywords_header", false);
        this.labels_header = prefs.getBoolean("labels_header", true);
        this.flags = prefs.getBoolean("flags", true);
        this.flags_background = prefs.getBoolean("flags_background", false);
        this.preview = prefs.getBoolean("preview", false);
        this.preview_italic = prefs.getBoolean("preview_italic", true);
        this.preview_lines = prefs.getInt("preview_lines", 1);
        this.align_header = prefs.getBoolean("align_header", false);
        this.message_zoom = prefs.getInt("message_zoom", 100);
        this.attachments_alt = prefs.getBoolean("attachments_alt", false);
        this.thumbnails = prefs.getBoolean("thumbnails", true);
        this.pdf_preview = prefs.getBoolean("pdf_preview", true);
        this.video_preview = prefs.getBoolean("video_preview", true);
        this.audio_preview = prefs.getBoolean("audio_preview", true);
        this.contrast = prefs.getBoolean("contrast", false);
        this.hyphenation = prefs.getBoolean("hyphenation", false);
        this.display_font = prefs.getString("display_font", "");
        this.inline = prefs.getBoolean("inline_images", false);
        this.collapse_quotes = prefs.getBoolean("collapse_quotes", false);
        this.authentication = prefs.getBoolean("authentication", true);
        this.authentication_indicator = prefs.getBoolean("authentication_indicator", false);
        this.infra = prefs.getBoolean("infra", false);
        this.tld_flags = prefs.getBoolean("tld_flags", false);
        this.language_detection = prefs.getBoolean("language_detection", false);
        this.autoclose_unseen = prefs.getBoolean("autoclose_unseen", false);
        this.collapse_marked = prefs.getBoolean("collapse_marked", true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            languages = new ArrayList<>();
            LocaleList ll = context.getResources().getConfiguration().getLocales();
            for (int i = 0; i < ll.size(); i++)
                languages.add(ll.get(i).getLanguage());
        } else
            languages = null;

        debug = prefs.getBoolean("debug", false);

        this.canDarken = WebViewEx.isFeatureSupported(context, WebViewFeature.ALGORITHMIC_DARKENING);
        this.fake_dark = prefs.getBoolean("fake_dark", false);
        this.show_recent = prefs.getBoolean("show_recent", false);

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

                    if (prev.uid == null && next.uid != null && // once only
                            properties.getValue("expanded", next.id)) {
                        // Mark seen when needed
                        if (!Boolean.TRUE.equals(next.ui_seen) && next.accountAutoSeen)
                            EntityOperation.queue(context, next, EntityOperation.SEEN, true);

                        // Download body when needed
                        if (!next.content)
                            EntityOperation.queue(context, next, EntityOperation.BODY);
                    }
                }
                if (!Objects.equals(prev.msgid, next.msgid)) {
                    // debug info
                    same = false;
                    log("msgid changed", next.id);
                }
                // references
                if (!Objects.equals(prev.deliveredto, next.deliveredto)) {
                    same = false;
                    log("deliveredto changed", next.id);
                }
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
                if (!Objects.equals(prev.sensitivity, next.sensitivity)) {
                    same = false;
                    log("sensitivity changed", next.id);
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
                if (!Objects.equals(prev.signedby, next.signedby)) {
                    same = false;
                    log("signedby changed", next.id);
                }
                if (!Objects.equals(prev.tls, next.tls)) {
                    same = false;
                    log("tls changed", next.id);
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
                if (!Objects.equals(prev.auth, next.auth)) {
                    same = false;
                    log("auth changed", next.id);
                }
                if (!Objects.equals(prev.mx, next.mx)) {
                    same = false;
                    log("mx changed", next.id);
                }
                if (!Objects.equals(prev.blocklist, next.blocklist)) {
                    same = false;
                    log("blocklist changed", next.id);
                }
                // from_domain
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
                // return_path
                // smtp_from
                if (!MessageHelper.equal(prev.submitter, next.submitter)) {
                    same = false;
                    log("submitter changed", next.id);
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
                if (!Objects.equals(prev.infrastructure, next.infrastructure)) {
                    same = false;
                    log("infrastructure changed", next.id);
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
                if (!prev.recent.equals(next.recent)) {
                    // updated after decryption
                    same = false;
                    log("recent changed", next.id);
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
                // fts
                if (!prev.auto_classified.equals(next.auto_classified)) {
                    same = false;
                    log("auto_classified changed " + prev.auto_classified + "/" + next.auto_classified, next.id);
                }
                // notifying
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
                // ui_local_only
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
                // last_touched

                // accountPop
                if (!Objects.equals(prev.accountName, next.accountName)) {
                    same = false;
                    log("accountName changed", next.id);
                }
                if (!Objects.equals(prev.accountCategory, next.accountCategory)) {
                    same = false;
                    log("accountCategory changed", next.id);
                }
                if (!Objects.equals(prev.accountColor, next.accountColor)) {
                    same = false;
                    log("accountColor changed", next.id);
                }
                // accountNotify
                // accountSummary
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
                if (prev.visible != next.visible) {
                    same = false;
                    log("visible changed " + prev.visible + "/" + next.visible, next.id);
                }
                if (prev.visible_unseen != next.visible_unseen) {
                    same = false;
                    log("visible_unseen changed " + prev.visible_unseen + "/" + next.visible_unseen, next.id);
                }
                if (prev.totalAttachments != next.totalAttachments) {
                    same = false;
                    log("totalAttachments changed " + prev.totalAttachments + "/" + next.totalAttachments, next.id);
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
                .setBackgroundThreadExecutor(executorDiffer)
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
                            boolean filter_seen = prefs.getBoolean(FragmentMessages.getFilter(context, "seen", viewType, type), false);
                            boolean filter_unflagged = prefs.getBoolean(FragmentMessages.getFilter(context, "unflagged", viewType, type), false);
                            boolean filter_snoozed = prefs.getBoolean(FragmentMessages.getFilter(context, "snoozed", viewType, type), true);

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
                AdapterMessage.this.rv = null;
                owner.getLifecycle().removeObserver(this);
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

                addExtra(message.from, message.extra);

                if (threading) {
                    message.senders = merge(message.from, message.senders);
                    message.recipients = merge(message.to, message.recipients);
                    addExtra(message.senders, message.extra);
                } else {
                    message.senders = message.from;
                    message.recipients = message.to;
                }

                message.resolveLabelColors(context);
                message.resolveKeywordColors(context);
            }
        }

        if (rv != null)
            savedState = rv.getLayoutManager().onSaveInstanceState();

        differ.submitList(list, new Runnable() {
            @Override
            public void run() {
                try {
                    if (rv != null)
                        rv.invalidateItemDecorations();
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    static Address[] merge(Address[] base, Address[] addresses) {
        if (base == null || base.length == 0)
            return (addresses == null ? new Address[0] : addresses);
        if (addresses == null)
            return base;

        List<Address> result = new ArrayList<>();
        result.addAll(Arrays.asList(base));
        for (Address a : addresses)
            for (Address b : base) {
                if (a.equals(b)) {
                    if (a instanceof InternetAddress && b instanceof InternetAddress) {
                        if (Objects.equals(((InternetAddress) a).getPersonal(), ((InternetAddress) b).getPersonal()))
                            continue;
                    } else
                        continue;
                }
                result.add(a);
            }
        return result.toArray(new Address[0]);
    }

    static void addExtra(Address[] addresses, String extra) {
        if (addresses == null || addresses.length == 0)
            return;
        if (extra == null)
            return;

        String email = ((InternetAddress) addresses[0]).getAddress();
        if (email == null)
            return;

        Pair<String, String> p = MessageHelper.getExtra(email, extra);

        if (p.first != null)
            try {
                ((InternetAddress) addresses[0]).setPersonal(p.first);
            } catch (Throwable ex) {
                Log.e(ex);
            }

        if (p.second != null)
            ((InternetAddress) addresses[0]).setAddress(p.second);
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

    void setPadding(int padding) {
        if (rv != null) {
            rv.getRecycledViewPool().clear();
            rv.getLayoutManager().removeAllViews();
        }
        properties.refresh();
    }

    void setLargeButtons(boolean large_buttons) {
        this.large_buttons = large_buttons;
        if (rv != null) {
            rv.getRecycledViewPool().clear();
            rv.getLayoutManager().removeAllViews();
        }
        properties.refresh();
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

    void setFilterTrash(boolean filter_trash) {
        if (this.filter_trash != filter_trash) {
            this.filter_trash = filter_trash;
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

        if (filter_trash && EntityFolder.TRASH.equals(message.folderType) && !allTrashed())
            return R.layout.item_message_duplicate;

        return (compact ? R.layout.item_message_compact : R.layout.item_message_normal);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(viewType, parent, false);
        if (align_header &&
                (viewType == R.layout.item_message_compact ||
                        viewType == R.layout.item_message_normal)) {
            View v = view.findViewById(R.id.ibAvatar);
            ConstraintLayout.LayoutParams lparam = (ConstraintLayout.LayoutParams) v.getLayoutParams();
            lparam.bottomToBottom = R.id.tvNotes;   // tvFolder
            for (int id : new int[]{R.id.tvLabels, R.id.tvExpand, R.id.tvPreview, R.id.tvNotes}) {
                v = view.findViewById(id);
                lparam = (ConstraintLayout.LayoutParams) v.getLayoutParams();
                lparam.startToEnd = R.id.ibAvatar;
            }
        }
        return new ViewHolder(view, viewType);
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

        message.resolveLabelColors(context);
        message.resolveKeywordColors(context);

        if (viewType == ViewType.THREAD && cards && threading && indentation) {
            boolean outgoing = holder.isOutgoing(message);
            ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            lparam.setMarginStart(outgoing ? dp12 : 0);
            lparam.setMarginEnd(outgoing ? 0 : dp12);
            holder.itemView.setLayoutParams(lparam);
        }

        if ((filter_duplicates && message.duplicate) ||
                (filter_trash && EntityFolder.TRASH.equals(message.folderType) && !allTrashed())) {
            holder.card.setCardBackgroundColor(message.folderColor == null
                    ? Color.TRANSPARENT
                    : ColorUtils.setAlphaComponent(message.folderColor, 128));
            if (filter_duplicates && message.duplicate)
                holder.tvFolder.setText(context.getString(R.string.title_duplicate_in,
                        message.getFolderName(context)));
            else
                holder.tvFolder.setText(context.getString(R.string.title_trashed_from,
                        MessageHelper.formatAddresses(message.from, false, false)));
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

    private boolean allTrashed() {
        if (differ.getItemCount() == 1)
            return true;

        for (int i = 0; i < differ.getItemCount(); i++) {
            TupleMessageEx m = differ.getItem(i);
            if (m == null || !EntityFolder.TRASH.equals(m.folderType))
                return false;
        }

        return true;
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
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        // View will become visible (possibly without rebinding)
        //holder.cowner.start();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        // View is invisible, but can be reused (without rebinding)
        //holder.cowner.stop();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        // Called before moving view into RecycledViewPool
        holder.eowner.recreate();
        holder.cowner.recreate();

        if (holder.ibAvatar != null)
            holder.ibAvatar.setImageDrawable(null);
        if (holder.tvBody != null)
            holder.tvBody.setText(null);
        if (holder.wvBody instanceof WebView)
            ((WebView) holder.wvBody).loadDataWithBaseURL(null, "", "text/html", StandardCharsets.UTF_8.name(), null);

        if (holder.taskContactInfo != null) {
            holder.taskContactInfo.cancel(context);
            holder.taskContactInfo = null;
        }
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

        String getValue(String key);

        boolean getValue(String name, long id);

        boolean getValue(String name, long id, boolean def);

        void setExpanded(TupleMessageEx message, boolean expanded, boolean scroll);

        void setSize(long id, Float size);

        float getSize(long id, float defaultSize);

        void setHeight(long id, Integer height);

        int getHeight(long id, int defaultHeight);

        void setPosition(long id, Pair<Integer, Integer> delta, Pair<Integer, Integer> position);

        Pair<Integer, Integer> getPosition(long id);

        void setAttachments(long id, List<EntityAttachment> attachments);

        List<EntityAttachment> getAttachments(long id);

        void scrollTo(int pos, int y);

        void scrollBy(int x, int y);

        void ready(long id);

        void move(long id, String type);

        int getSelectionCount();

        void moveSelection(String type, boolean block);

        void reply(TupleMessageEx message, CharSequence selected, View anchor);

        void startSearch(TextView view);

        void endSearch();

        void lock(long id);

        void layoutChanged();

        void refresh();

        void finish();
    }
}
