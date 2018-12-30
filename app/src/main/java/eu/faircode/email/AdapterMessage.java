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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;
    private FragmentManager fragmentManager;
    private ViewType viewType;
    private boolean outgoing;
    private boolean internet;
    private IProperties properties;

    private boolean threading;
    private boolean compact;
    private boolean contacts;
    private boolean avatars;
    private boolean identicons;
    private boolean preview;
    private boolean confirm;
    private boolean debug;

    private int dp24;
    private float textSize;
    private int colorPrimary;
    private int colorAccent;
    private int textColorSecondary;
    private int colorUnread;
    private String theme;
    private boolean hasWebView;

    private SelectionTracker<Long> selectionTracker = null;
    private AsyncPagedListDiffer<TupleMessageEx> differ = new AsyncPagedListDiffer<>(this, DIFF_CALLBACK);

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    private static final float LOW_LIGHT = 0.6f;

    private static DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG);

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
        private View itemView;
        private View vwColor;
        private ImageView ivExpander;
        private ImageView ivFlagged;
        private ImageView ivAvatar;
        private TextView tvFrom;
        private TextView tvSize;
        private TextView tvTime;
        private ImageView ivAnswered;
        private ImageView ivAttachments;
        private TextView tvSubject;
        private TextView tvFolder;
        private TextView tvCount;
        private ImageView ivThread;
        private TextView tvPreview;
        private TextView tvError;
        private ContentLoadingProgressBar pbLoading;

        private ImageView ivExpanderAddress;
        private TextView tvFromEx;
        private ImageView ivAddContact;
        private TextView tvTo;
        private TextView tvReplyTo;
        private TextView tvCc;
        private TextView tvBcc;
        private TextView tvTimeEx;
        private TextView tvSizeEx;
        private TextView tvSubjectEx;
        private TextView tvKeywords;

        private TextView tvHeaders;
        private ContentLoadingProgressBar pbHeaders;
        private TextView tvNoInternetHeaders;

        private RecyclerView rvAttachment;
        private TextView tvNoInternetAttachments;

        private BottomNavigationView bnvActions;

        private Button btnHtml;
        private ImageButton ibQuotes;
        private ImageButton ibImages;
        private TextView tvBody;
        private ContentLoadingProgressBar pbBody;
        private TextView tvNoInternetBody;

        private Group grpAddress;
        private Group grpHeaders;
        private Group grpAttachments;
        private Group grpExpanded;

        private AdapterAttachment adapter;
        private LiveData<List<EntityAttachment>> liveAttachments = null;
        private Observer<List<EntityAttachment>> observerAttachments = null;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivExpander = itemView.findViewById(R.id.ivExpander);
            ivFlagged = itemView.findViewById(R.id.ivFlagged);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivAnswered = itemView.findViewById(R.id.ivAnswered);
            ivAttachments = itemView.findViewById(R.id.ivAttachments);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvCount = itemView.findViewById(R.id.tvCount);
            ivThread = itemView.findViewById(R.id.ivThread);
            tvError = itemView.findViewById(R.id.tvError);
            pbLoading = itemView.findViewById(R.id.pbLoading);

            ivExpanderAddress = itemView.findViewById(R.id.ivExpanderAddress);
            tvFromEx = itemView.findViewById(R.id.tvFromEx);
            ivAddContact = itemView.findViewById(R.id.ivAddContact);
            tvTo = itemView.findViewById(R.id.tvTo);
            tvReplyTo = itemView.findViewById(R.id.tvReplyTo);
            tvCc = itemView.findViewById(R.id.tvCc);
            tvBcc = itemView.findViewById(R.id.tvBcc);
            tvTimeEx = itemView.findViewById(R.id.tvTimeEx);
            tvSizeEx = itemView.findViewById(R.id.tvSizeEx);
            tvSubjectEx = itemView.findViewById(R.id.tvSubjectEx);
            tvKeywords = itemView.findViewById(R.id.tvKeywords);

            tvHeaders = itemView.findViewById(R.id.tvHeaders);
            pbHeaders = itemView.findViewById(R.id.pbHeaders);
            tvNoInternetHeaders = itemView.findViewById(R.id.tvNoInternetHeaders);

            rvAttachment = itemView.findViewById(R.id.rvAttachment);
            rvAttachment.setHasFixedSize(false);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            rvAttachment.setLayoutManager(llm);
            rvAttachment.setItemAnimator(null);

            adapter = new AdapterAttachment(context, owner, true);
            rvAttachment.setAdapter(adapter);

            tvNoInternetAttachments = itemView.findViewById(R.id.tvNoInternetAttachments);

            bnvActions = itemView.findViewById(R.id.bnvActions);

            btnHtml = itemView.findViewById(R.id.btnHtml);
            ibQuotes = itemView.findViewById(R.id.ibQuotes);
            ibImages = itemView.findViewById(R.id.ibImages);
            tvBody = itemView.findViewById(R.id.tvBody);
            pbBody = itemView.findViewById(R.id.pbBody);
            tvNoInternetBody = itemView.findViewById(R.id.tvNoInternetBody);

            grpAddress = itemView.findViewById(R.id.grpAddress);
            grpHeaders = itemView.findViewById(R.id.grpHeaders);
            grpAttachments = itemView.findViewById(R.id.grpAttachments);
            grpExpanded = itemView.findViewById(R.id.grpExpanded);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            ivFlagged.setOnClickListener(this);
            ivExpanderAddress.setOnClickListener(this);
            ivAddContact.setOnClickListener(this);
            btnHtml.setOnClickListener(this);
            ibQuotes.setOnClickListener(this);
            ibImages.setOnClickListener(this);

            bnvActions.setOnNavigationItemSelectedListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            ivFlagged.setOnClickListener(null);
            ivExpanderAddress.setOnClickListener(null);
            ivAddContact.setOnClickListener(null);
            btnHtml.setOnClickListener(null);
            ibQuotes.setOnClickListener(null);
            ibImages.setOnClickListener(null);

            bnvActions.setOnNavigationItemSelectedListener(null);
        }

        private void clear() {
            vwColor.setVisibility(View.GONE);
            ivExpander.setVisibility(View.GONE);
            ivFlagged.setVisibility(View.GONE);
            ivAvatar.setVisibility(View.GONE);
            tvFrom.setText(null);
            tvSize.setText(null);
            tvTime.setText(null);
            ivAnswered.setVisibility(View.GONE);
            ivAttachments.setVisibility(View.GONE);
            tvSubject.setText(null);
            tvFolder.setText(null);
            tvCount.setText(null);
            ivThread.setVisibility(View.GONE);
            tvPreview.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            pbLoading.setVisibility(View.VISIBLE);

            tvKeywords.setVisibility(View.GONE);

            pbHeaders.setVisibility(View.GONE);
            tvNoInternetHeaders.setVisibility(View.GONE);

            tvNoInternetAttachments.setVisibility(View.GONE);

            bnvActions.setVisibility(View.GONE);
            btnHtml.setVisibility(View.GONE);
            ibQuotes.setVisibility(View.GONE);
            ibImages.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            tvNoInternetBody.setVisibility(View.GONE);

            grpAddress.setVisibility(View.GONE);
            ivAddContact.setVisibility(View.GONE);
            grpHeaders.setVisibility(View.GONE);
            grpAttachments.setVisibility(View.GONE);
            grpExpanded.setVisibility(View.GONE);
        }

        private void bindTo(int position, final TupleMessageEx message) {
            final DB db = DB.getInstance(context);
            final boolean show_expanded = properties.getValue("expanded", message.id);
            boolean show_addresses = !properties.getValue("addresses", message.id);
            boolean show_headers = properties.getValue("headers", message.id);

            pbLoading.setVisibility(View.GONE);

            if (viewType == ViewType.THREAD) {
                ivFlagged.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                ivAvatar.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvFrom.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvSize.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvTime.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                ivAnswered.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                ivAttachments.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvSubject.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvFolder.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvCount.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                ivThread.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvPreview.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
                tvError.setAlpha(message.duplicate ? LOW_LIGHT : 1.0f);
            }

            if (avatars || identicons) {
                ivAvatar.setVisibility(compact ? View.GONE : View.INVISIBLE);

                Bundle aargs = new Bundle();
                aargs.putLong("id", message.id);
                aargs.putString("uri", message.avatar);
                if (message.from != null && message.from.length > 0)
                    aargs.putString("from", message.from[0].toString());

                ivAvatar.setTag(message.id);

                new SimpleTask<Drawable>() {
                    @Override
                    protected Drawable onLoad(Context context, Bundle args) {
                        String uri = args.getString("uri");
                        if (avatars && !outgoing && uri != null)
                            try {
                                ContentResolver resolver = context.getContentResolver();
                                InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(resolver, Uri.parse(uri));
                                if (is != null)
                                    return Drawable.createFromStream(is, "avatar");
                            } catch (SecurityException ex) {
                                Log.e(ex);
                            }

                        String from = args.getString("from");
                        if (identicons && !outgoing && from != null) {
                            return new BitmapDrawable(
                                    context.getResources(),
                                    Identicon.generate(from, dp24, 5, "light".equals(theme)));
                        }

                        return null;
                    }

                    @Override
                    protected void onLoaded(Bundle args, Drawable avatar) {
                        if (avatar != null) {
                            if ((long) ivAvatar.getTag() == args.getLong("id")) {
                                ivAvatar.setImageDrawable(avatar);
                                ivAvatar.setVisibility(View.VISIBLE);
                            } else
                                Log.i("Skipping avatar");
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.load(context, owner, aargs);
            } else
                ivAvatar.setVisibility(View.GONE);

            vwColor.setBackgroundColor(message.accountColor == null ? Color.TRANSPARENT : message.accountColor);
            vwColor.setVisibility(View.VISIBLE);

            ivExpander.setImageResource(show_expanded ? R.drawable.baseline_expand_less_24 : R.drawable.baseline_expand_more_24);
            if (viewType == ViewType.THREAD && threading)
                ivExpander.setVisibility(EntityFolder.DRAFTS.equals(message.folderType) ? View.INVISIBLE : View.VISIBLE);
            else
                ivExpander.setVisibility(View.GONE);

            int flagged = (message.count - message.unflagged);
            ivFlagged.setImageResource(flagged > 0 ? R.drawable.baseline_star_24 : R.drawable.baseline_star_border_24);
            ivFlagged.setImageTintList(ColorStateList.valueOf(flagged > 0 ? colorAccent : textColorSecondary));
            ivFlagged.setVisibility(message.uid == null ? View.INVISIBLE : View.VISIBLE);

            tvFrom.setText(MessageHelper.getFormattedAddresses(outgoing ? message.to : message.from, false));
            tvSize.setText(message.size == null ? null : Helper.humanReadableByteCount(message.size, true));
            tvSize.setVisibility(message.size == null || message.content ? View.GONE : View.VISIBLE);
            tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.received));

            ivAnswered.setVisibility(message.ui_answered ? View.VISIBLE : View.GONE);
            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);
            tvNoInternetAttachments.setVisibility(View.GONE);
            tvSubject.setText(message.subject);

            if (viewType == ViewType.THREAD || viewType == ViewType.SEARCH)
                tvFolder.setText(message.folderDisplay == null
                        ? Helper.localizeFolderName(context, message.folderName)
                        : message.folderDisplay);
            else
                tvFolder.setText(message.accountName);
            tvFolder.setVisibility(viewType == ViewType.FOLDER && compact ? View.GONE : View.VISIBLE);

            tvPreview.setText(message.preview);
            tvPreview.setVisibility(preview && !TextUtils.isEmpty(message.preview) ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.THREAD || !threading) {
                tvCount.setVisibility(View.GONE);
                ivThread.setVisibility(View.GONE);
            } else {
                tvCount.setText(Integer.toString(message.visible));
                ivThread.setVisibility(View.VISIBLE);
            }

            if (debug) {
                String text = "error=" + message.error +
                        "\n uid=" + message.uid + " id=" + message.id + " " + df.format(new Date(message.received)) +
                        "\n" + (message.ui_hide ? "HIDDEN " : "") +
                        "seen=" + message.seen + "/" + message.ui_seen + " unseen=" + message.unseen +
                        " found=" + message.ui_found +
                        "\n msgid=" + message.msgid +
                        "\n thread=" + message.thread +
                        "\n sender=" + message.sender;

                tvError.setText(text);
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setText(message.error);
                tvError.setVisibility(message.error == null ? View.GONE : View.VISIBLE);
            }

            // Unseen
            int typeface = (message.unseen > 0 ? Typeface.BOLD : Typeface.NORMAL);
            tvFrom.setTypeface(null, typeface);
            tvSize.setTypeface(null, typeface);
            tvTime.setTypeface(null, typeface);
            tvSubject.setTypeface(null, typeface);
            tvCount.setTypeface(null, typeface);

            if (textSize != 0) {
                tvFrom.setTextSize(textSize);
                tvSubject.setTextSize(textSize);
            }

            int colorUnseen = (message.unseen > 0 ? colorUnread : textColorSecondary);
            tvFrom.setTextColor(colorUnseen);
            tvSize.setTextColor(colorUnseen);
            tvTime.setTextColor(colorUnseen);

            grpExpanded.setVisibility(viewType == ViewType.THREAD && show_expanded ? View.VISIBLE : View.GONE);
            grpAddress.setVisibility(viewType == ViewType.THREAD && show_expanded && show_addresses ? View.VISIBLE : View.GONE);
            tvKeywords.setVisibility(View.GONE);
            ivAddContact.setVisibility(viewType == ViewType.THREAD && show_expanded && show_addresses && contacts && message.from != null ? View.VISIBLE : View.GONE);

            if (show_headers && show_expanded && message.headers == null) {
                pbHeaders.setVisibility(internet ? View.VISIBLE : View.GONE);
                tvNoInternetHeaders.setVisibility(internet ? View.GONE : View.VISIBLE);
            } else {
                pbHeaders.setVisibility(View.GONE);
                tvNoInternetHeaders.setVisibility(View.GONE);
            }

            grpHeaders.setVisibility(show_headers && show_expanded ? View.VISIBLE : View.GONE);
            grpAttachments.setVisibility(message.attachments > 0 && show_expanded ? View.VISIBLE : View.GONE);
            bnvActions.setVisibility(viewType == ViewType.THREAD && show_expanded ? View.INVISIBLE : View.GONE);
            btnHtml.setVisibility(viewType == ViewType.THREAD && show_expanded ? View.INVISIBLE : View.GONE);
            ibQuotes.setVisibility(viewType == ViewType.THREAD && show_expanded ? View.INVISIBLE : View.GONE);
            ibImages.setVisibility(viewType == ViewType.THREAD && show_expanded ? View.INVISIBLE : View.GONE);
            pbBody.setVisibility(View.GONE);
            tvNoInternetBody.setVisibility(View.GONE);

            bnvActions.setTag(null);

            if (show_expanded) {
                ivExpanderAddress.setImageResource(show_addresses ? R.drawable.baseline_expand_less_24 : R.drawable.baseline_expand_more_24);

                tvFromEx.setText(MessageHelper.getFormattedAddresses(message.from, true));
                tvTo.setText(MessageHelper.getFormattedAddresses(message.to, true));
                tvReplyTo.setText(MessageHelper.getFormattedAddresses(message.reply, true));
                tvCc.setText(MessageHelper.getFormattedAddresses(message.cc, true));
                tvBcc.setText(MessageHelper.getFormattedAddresses(message.bcc, true));

                tvTimeEx.setText(df.format(new Date(message.received)));

                tvSizeEx.setText(message.size == null ? null : Helper.humanReadableByteCount(message.size, true));
                if (!message.duplicate)
                    tvSizeEx.setAlpha(message.content ? 1.0f : LOW_LIGHT);
                tvSizeEx.setVisibility(message.size == null ? View.GONE : View.VISIBLE);

                tvSubjectEx.setText(message.subject);
                tvKeywords.setText(TextUtils.join(" ", message.keywords));
                tvKeywords.setVisibility(message.keywords.length > 0 ? View.VISIBLE : View.GONE);

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

                for (int i = 0; i < bnvActions.getMenu().size(); i++)
                    bnvActions.getMenu().getItem(i).setVisible(false);
                bnvActions.setVisibility(View.VISIBLE);

                if (textSize != 0)
                    tvBody.setTextSize(textSize);

                Spanned body = properties.getBody(message.id);
                tvBody.setText(body);
                tvBody.setMovementMethod(new UrlHandler());
                if (internet || message.content)
                    pbBody.setVisibility(View.VISIBLE);
                else
                    tvNoInternetBody.setVisibility(View.VISIBLE);

                if (body == null && message.content) {
                    Bundle args = new Bundle();
                    args.putSerializable("message", message);
                    bodyTask.load(context, owner, args);
                }

                // Observe attachments
                observerAttachments = new Observer<List<EntityAttachment>>() {
                    @Override
                    public void onChanged(@Nullable List<EntityAttachment> attachments) {
                        if (attachments == null)
                            attachments = new ArrayList<>();

                        adapter.set(attachments);

                        boolean downloading = false;
                        for (EntityAttachment attachment : attachments)
                            if (attachment.progress != null) {
                                downloading = true;
                                break;
                            }

                        tvNoInternetAttachments.setVisibility(downloading && !internet ? View.VISIBLE : View.GONE);

                        if (message.content) {
                            Bundle args = new Bundle();
                            args.putSerializable("message", message);
                            bodyTask.load(context, owner, args);
                        }
                    }
                };
                liveAttachments = db.attachment().liveAttachments(message.id);
                liveAttachments.observe(owner, observerAttachments);

                // Setup action
                Bundle sargs = new Bundle();
                sargs.putLong("account", message.account);

                new SimpleTask<List<EntityFolder>>() {
                    @Override
                    protected void onInit(Bundle args) {
                        bnvActions.setHasTransientState(true);
                    }

                    @Override
                    protected void onCleanup(Bundle args) {
                        bnvActions.setHasTransientState(false);
                    }

                    @Override
                    protected List<EntityFolder> onLoad(Context context, Bundle args) {
                        long account = args.getLong("account");
                        return DB.getInstance(context).folder().getSystemFolders(account);
                    }

                    @Override
                    protected void onLoaded(Bundle args, List<EntityFolder> folders) {
                        boolean hasJunk = false;
                        boolean hasTrash = false;
                        boolean hasArchive = false;

                        if (folders != null)
                            for (EntityFolder folder : folders) {
                                if (EntityFolder.JUNK.equals(folder.type))
                                    hasJunk = true;
                                else if (EntityFolder.TRASH.equals(folder.type))
                                    hasTrash = true;
                                else if (EntityFolder.ARCHIVE.equals(folder.type))
                                    hasArchive = true;
                            }

                        boolean inOutbox = EntityFolder.OUTBOX.equals(message.folderType);
                        boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                        boolean inTrash = EntityFolder.TRASH.equals(message.folderType);

                        ActionData data = new ActionData();
                        data.hasJunk = hasJunk;
                        data.delete = (inTrash || !hasTrash || inOutbox);
                        data.message = message;
                        bnvActions.setTag(data);

                        bnvActions.getMenu().findItem(R.id.action_more).setVisible(!inOutbox);
                        bnvActions.getMenu().findItem(R.id.action_delete).setVisible(
                                (inTrash && message.msgid != null) ||
                                        (!inTrash && hasTrash && message.uid != null) ||
                                        (inOutbox && !TextUtils.isEmpty(message.error)));
                        bnvActions.getMenu().findItem(R.id.action_delete).setTitle(inTrash ? R.string.title_delete : R.string.title_trash);
                        bnvActions.getMenu().findItem(R.id.action_move).setVisible(message.uid != null);
                        bnvActions.getMenu().findItem(R.id.action_archive).setVisible(message.uid != null && !inArchive && hasArchive);
                        bnvActions.getMenu().findItem(R.id.action_reply).setEnabled(message.content);
                        bnvActions.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.load(context, owner, sargs);
            } else
                properties.setBody(message.id, null);

            itemView.setActivated(selectionTracker != null && selectionTracker.isSelected(message.id));
        }

        void unbind() {
            if (liveAttachments != null) {
                liveAttachments.removeObserver(observerAttachments);
                liveAttachments = null;
            }
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleMessageEx message = differ.getItem(pos);

            if (view.getId() == R.id.ivFlagged)
                onToggleFlag(message);
            else if (view.getId() == R.id.ivAddContact)
                onAddContact(message);
            else if (viewType == ViewType.THREAD) {
                if (view.getId() == R.id.ivExpanderAddress)
                    onToggleAddresses(pos, message);
                else if (view.getId() == R.id.btnHtml)
                    onShowHtml(message);
                else if (view.getId() == R.id.ibQuotes)
                    onShowQuotes(message);
                else if (view.getId() == R.id.ibImages)
                    onShowImages(message);
                else
                    onToggleMessage(pos, message);
            } else {
                if (EntityFolder.DRAFTS.equals(message.folderType))
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
                                    .putExtra("id", message.id));
                }
            }
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

                Cursor cursor = null;
                try {
                    ContentResolver resolver = context.getContentResolver();
                    cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                                    ContactsContract.Contacts.LOOKUP_KEY
                            },
                            ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                            new String[]{email}, null);
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
                } finally {
                    if (cursor != null)
                        cursor.close();
                }

                context.startActivity(edit);
            }
        }

        private void onToggleMessage(int pos, TupleMessageEx message) {
            if (EntityFolder.DRAFTS.equals(message.folderType))
                context.startActivity(
                        new Intent(context, ActivityCompose.class)
                                .putExtra("action", "edit")
                                .putExtra("id", message.id));
            else {
                boolean expanded = !properties.getValue("expanded", message.id);
                properties.setValue("expanded", message.id, expanded);
                if (!expanded) {
                    properties.setValue("quotes", message.id, false);
                    properties.setValue("images", message.id, false);
                }

                notifyItemChanged(pos);

                if (expanded)
                    properties.scrollTo(pos, Math.round(tvBody.getY()));
            }
        }

        private void onToggleAddresses(int pos, TupleMessageEx message) {
            boolean addresses = !properties.getValue("addresses", message.id);
            properties.setValue("addresses", message.id, addresses);
            notifyItemChanged(pos);
        }

        private void onShowHtml(final TupleMessageEx message) {
            if (confirm)
                new DialogBuilderLifecycle(context, owner)
                        .setMessage(R.string.title_ask_show_html)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onShowHtmlConfirmed(message);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            else
                onShowHtmlConfirmed(message);
        }

        private void onShowHtmlConfirmed(final TupleMessageEx message) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_VIEW_FULL)
                            .putExtra("id", message.id)
                            .putExtra("from", MessageHelper.getFormattedAddresses(message.from, true)));
        }

        private void onShowQuotes(final TupleMessageEx message) {
            properties.setValue("quotes", message.id, true);
            ibQuotes.setEnabled(false);

            Bundle args = new Bundle();
            args.putSerializable("message", message);
            bodyTask.load(context, owner, args);
        }

        private void onShowImages(final TupleMessageEx message) {
            if (confirm)
                new DialogBuilderLifecycle(context, owner)
                        .setMessage(R.string.title_ask_show_image)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onShowImagesConfirmed(message);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            else
                onShowImagesConfirmed(message);
        }

        private void onShowImagesConfirmed(final TupleMessageEx message) {
            properties.setValue("images", message.id, true);
            ibImages.setEnabled(false);

            Bundle args = new Bundle();
            args.putSerializable("message", message);
            bodyTask.load(context, owner, args);
        }

        private SimpleTask<SpannableStringBuilder> bodyTask = new SimpleTask<SpannableStringBuilder>() {
            private String body = null;

            @Override
            protected void onInit(Bundle args) {
                btnHtml.setHasTransientState(true);
                ibQuotes.setHasTransientState(true);
                ibImages.setHasTransientState(true);
                tvBody.setHasTransientState(true);
                pbBody.setHasTransientState(true);
            }

            @Override
            protected void onCleanup(Bundle args) {
                btnHtml.setHasTransientState(false);
                ibQuotes.setHasTransientState(false);
                ibImages.setHasTransientState(false);
                tvBody.setHasTransientState(false);
                pbBody.setHasTransientState(false);
            }

            @Override
            protected SpannableStringBuilder onLoad(Context context, final Bundle args) {
                DB db = DB.getInstance(context);
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                if (body == null)
                    try {
                        body = message.read(context);
                    } catch (IOException ex) {
                        Log.e(ex);
                        body = "";
                        db.message().setMessageContent(message.id, false, null);
                    }

                Spanned html = decodeHtml(message, body);

                SpannableStringBuilder builder = new SpannableStringBuilder(html);
                QuoteSpan[] quotes = builder.getSpans(0, builder.length(), QuoteSpan.class);
                for (QuoteSpan quote : quotes) {
                    builder.setSpan(
                            new StyledQuoteSpan(),
                            builder.getSpanStart(quote),
                            builder.getSpanEnd(quote),
                            builder.getSpanFlags(quote));
                    builder.removeSpan(quote);
                }

                return builder;
            }

            class StyledQuoteSpan extends QuoteSpan {
                StyledQuoteSpan() {
                    super(colorPrimary);
                }

                @Override
                public int getLeadingMargin(boolean first) {
                    return 6 /* stripeWidth */ + 12 /* gapWidth */;
                }

                @Override
                public void drawLeadingMargin(@NonNull Canvas c, @NonNull Paint p, int x, int dir, int top, int baseline, int bottom, @NonNull CharSequence text, int start, int end, boolean first, @NonNull Layout layout) {
                    Paint.Style style = p.getStyle();
                    int color = p.getColor();

                    p.setStyle(Paint.Style.FILL);
                    p.setColor(getColor());

                    c.drawRect(x, top, x + dir * 6 /* stripeWidth */, bottom, p);

                    p.setStyle(style);
                    p.setColor(color);
                }
            }

            @Override
            protected void onLoaded(Bundle args, SpannableStringBuilder body) {
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                boolean has_quotes = (body.getSpans(0, body.length(), StyledQuoteSpan.class).length > 0);
                boolean has_images = (body.getSpans(0, body.length(), ImageSpan.class).length > 0);
                boolean show_expanded = properties.getValue("expanded", message.id);
                boolean show_quotes = properties.getValue("quotes", message.id);
                boolean show_images = properties.getValue("images", message.id);

                btnHtml.setVisibility(hasWebView && show_expanded ? View.VISIBLE : View.GONE);
                ibQuotes.setVisibility(has_quotes && show_expanded && !show_quotes ? View.VISIBLE : View.GONE);
                ibImages.setVisibility(has_images && show_expanded && !show_images ? View.VISIBLE : View.GONE);
                tvBody.setText(body);
                tvBody.setMovementMethod(new UrlHandler());
                pbBody.setVisibility(View.GONE);
                properties.setBody(message.id, body);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(context, owner, ex);
            }
        };

        private Spanned decodeHtml(final EntityMessage message, String body) {
            final boolean show_quotes = properties.getValue("quotes", message.id);
            final boolean show_images = properties.getValue("images", message.id);

            return Html.fromHtml(HtmlHelper.sanitize(body, show_quotes), new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    return HtmlHelper.decodeImage(source, context, message.id, show_images);
                }
            }, new Html.TagHandler() {
                @Override
                public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                    if (BuildConfig.DEBUG)
                        Log.i("HTML tag=" + tag + " opening=" + opening);
                }
            });
        }

        private class UrlHandler extends LinkMovementMethod {
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;

                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
                if (link.length != 0) {
                    String url = link[0].getURL();
                    Uri uri = Uri.parse(url);

                    if (BuildConfig.APPLICATION_ID.equals(uri.getHost()) && "/activate/".equals(uri.getPath())) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_ACTIVATE_PRO)
                                        .putExtra("uri", uri));

                    } else {
                        View view = LayoutInflater.from(context).inflate(R.layout.dialog_link, null);
                        final EditText etLink = view.findViewById(R.id.etLink);
                        etLink.setText(url);
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

                return true;
            }
        }

        private class ActionData {
            boolean hasJunk;
            boolean delete;
            TupleMessageEx message;
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            ActionData data = (ActionData) bnvActions.getTag();
            if (data == null)
                return false;

            switch (item.getItemId()) {
                case R.id.action_more:
                    onMore(data);
                    return true;
                case R.id.action_delete:
                    onDelete(data);
                    return true;
                case R.id.action_move:
                    onMove(data);
                    return true;
                case R.id.action_archive:
                    onArchive(data);
                    return true;
                case R.id.action_reply:
                    onReply(data, false);
                    return true;
                default:
                    return false;
            }
        }

        private void onJunk(final ActionData data) {
            new DialogBuilderLifecycle(context, owner)
                    .setMessage(R.string.title_ask_spam)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putLong("id", data.message.id);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onLoad(Context context, Bundle args) {
                                    long id = args.getLong("id");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        EntityMessage message = db.message().getMessage(id);
                                        EntityFolder junk = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                                        EntityOperation.queue(db, message, EntityOperation.MOVE, junk.id);

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
                            }.load(context, owner, args);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void onForward(final ActionData data, final boolean raw) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<Boolean>() {
                @Override
                protected Boolean onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    List<EntityAttachment> attachments = DB.getInstance(context).attachment().getAttachments(id);
                    for (EntityAttachment attachment : attachments)
                        if (!attachment.available)
                            return false;
                    return true;
                }

                @Override
                protected void onLoaded(Bundle args, Boolean available) {
                    final Intent forward = new Intent(context, ActivityCompose.class)
                            .putExtra("action", "forward")
                            .putExtra("reference", data.message.id)
                            .putExtra("raw", raw);
                    if (available)
                        context.startActivity(forward);
                    else
                        new DialogBuilderLifecycle(context, owner)
                                .setMessage(R.string.title_attachment_unavailable)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        context.startActivity(forward);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.load(context, owner, args);
        }

        private void onAnswer(final ActionData data) {
            new SimpleTask<List<EntityAnswer>>() {
                @Override
                protected List<EntityAnswer> onLoad(Context context, Bundle args) {
                    return DB.getInstance(context).answer().getAnswers();
                }

                @Override
                protected void onLoaded(Bundle args, List<EntityAnswer> answers) {
                    if (answers == null || answers.size() == 0) {
                        Snackbar snackbar = Snackbar.make(
                                itemView,
                                context.getString(R.string.title_no_answers),
                                Snackbar.LENGTH_LONG);
                        snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.content_frame, new FragmentAnswers()).addToBackStack("answers");
                                fragmentTransaction.commit();
                            }
                        });
                        snackbar.show();
                    } else {
                        final Collator collator = Collator.getInstance(Locale.getDefault());
                        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                        Collections.sort(answers, new Comparator<EntityAnswer>() {
                            @Override
                            public int compare(EntityAnswer a1, EntityAnswer a2) {
                                return collator.compare(a1.name, a2.name);
                            }
                        });

                        View anchor = bnvActions.findViewById(R.id.action_more);
                        PopupMenu popupMenu = new PopupMenu(context, anchor);

                        int order = 0;
                        for (EntityAnswer answer : answers)
                            popupMenu.getMenu().add(Menu.NONE, answer.id.intValue(), order++, answer.name);

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem target) {
                                if (Helper.isPro(context))
                                    context.startActivity(new Intent(context, ActivityCompose.class)
                                            .putExtra("action", "reply")
                                            .putExtra("reference", data.message.id)
                                            .putExtra("answer", (long) target.getItemId()));
                                else {
                                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                                    lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                                }
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
            }.load(context, owner, new Bundle());
        }

        private void onUnseen(final ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        EntityOperation.queue(db, message, EntityOperation.SEEN, false);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onLoaded(Bundle args, Void ignored) {
                    properties.setValue("expanded", data.message.id, false);
                    notifyDataSetChanged();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.load(context, owner, args);
        }

        private void onToggleFlag(TupleMessageEx message) {
            Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("flagged", !message.ui_flagged);
            args.putBoolean("thread", viewType != ViewType.THREAD);
            Log.i("Set message id=" + message.id + " flagged=" + !message.ui_flagged);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean flagged = args.getBoolean("flagged");
                    boolean thread = args.getBoolean("thread");

                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    List<EntityMessage> messages = db.message().getMessageByThread(
                            message.account, message.thread, threading && thread ? null : id, message.folder);
                    for (EntityMessage threaded : messages)
                        EntityOperation.queue(db, threaded, EntityOperation.FLAG, flagged);

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.load(context, owner, args);
        }

        private void onShowHeaders(ActionData data) {
            boolean show_headers = !properties.getValue("headers", data.message.id);
            properties.setValue("headers", data.message.id, show_headers);
            if (show_headers && data.message.headers == null) {
                grpHeaders.setVisibility(View.VISIBLE);
                if (internet)
                    pbHeaders.setVisibility(View.VISIBLE);
                else
                    tvNoInternetHeaders.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", data.message.id);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        Long id = args.getLong("id");
                        DB db = DB.getInstance(context);
                        EntityMessage message = db.message().getMessage(id);
                        EntityOperation.queue(db, message, EntityOperation.HEADERS);
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.load(context, owner, args);
            } else
                notifyDataSetChanged();
        }

        private void onManageKeywords(ActionData data) {
            if (!Helper.isPro(context)) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                return;
            }

            Bundle args = new Bundle();
            args.putSerializable("message", data.message);

            new SimpleTask<EntityFolder>() {
                @Override
                protected EntityFolder onLoad(Context context, Bundle args) {
                    EntityMessage message = (EntityMessage) args.getSerializable("message");
                    return DB.getInstance(context).folder().getFolder(message.folder);
                }

                @Override
                protected void onLoaded(final Bundle args, EntityFolder folder) {
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
                                    args.putStringArray("keywords", items.toArray(new String[0]));
                                    args.putBooleanArray("selected", selected);
                                    args.putBooleanArray("dirty", dirty);

                                    new SimpleTask<Void>() {
                                        @Override
                                        protected Void onLoad(Context context, Bundle args) {
                                            EntityMessage message = (EntityMessage) args.getSerializable("message");
                                            String[] keywords = args.getStringArray("keywords");
                                            boolean[] selected = args.getBooleanArray("selected");
                                            boolean[] dirty = args.getBooleanArray("dirty");

                                            DB db = DB.getInstance(context);

                                            try {
                                                db.beginTransaction();

                                                for (int i = 0; i < selected.length; i++)
                                                    if (dirty[i])
                                                        EntityOperation.queue(db, message, EntityOperation.KEYWORD, keywords[i], selected[i]);

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
                                    }.load(context, owner, args);
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
                                                    String keyword = Helper.sanitizeKeyword(etKeyword.getText().toString());
                                                    if (!TextUtils.isEmpty(keyword)) {
                                                        args.putString("keyword", keyword);

                                                        new SimpleTask<Void>() {
                                                            @Override
                                                            protected Void onLoad(Context context, Bundle args) {
                                                                EntityMessage message = (EntityMessage) args.getSerializable("message");
                                                                String keyword = args.getString("keyword");

                                                                DB db = DB.getInstance(context);
                                                                EntityOperation.queue(db, message, EntityOperation.KEYWORD, keyword, true);

                                                                return null;
                                                            }

                                                            @Override
                                                            protected void onException(Bundle args, Throwable ex) {
                                                                Helper.unexpectedError(context, owner, ex);
                                                            }
                                                        }.load(context, owner, args);
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
            }.load(context, owner, args);
        }

        private void onDecrypt(ActionData data) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_DECRYPT)
                            .putExtra("id", data.message.id));
        }

        private void onMore(final ActionData data) {
            boolean show_headers = properties.getValue("headers", data.message.id);

            View anchor = bnvActions.findViewById(R.id.action_more);
            PopupMenu popupMenu = new PopupMenu(context, anchor);
            popupMenu.inflate(R.menu.menu_message);
            popupMenu.getMenu().findItem(R.id.menu_junk).setVisible(data.message.uid != null && data.hasJunk);

            popupMenu.getMenu().findItem(R.id.menu_forward).setEnabled(data.message.content);
            popupMenu.getMenu().findItem(R.id.menu_forward_raw).setVisible(data.message.content && data.message.headers != null);

            popupMenu.getMenu().findItem(R.id.menu_reply_all).setEnabled(data.message.content);

            popupMenu.getMenu().findItem(R.id.menu_answer).setEnabled(data.message.content);

            popupMenu.getMenu().findItem(R.id.menu_unseen).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_show_headers).setChecked(show_headers);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_manage_keywords).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_decrypt).setEnabled(
                    data.message.content && data.message.to != null && data.message.to.length > 0);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    switch (target.getItemId()) {
                        case R.id.menu_junk:
                            onJunk(data);
                            return true;
                        case R.id.menu_forward:
                            onForward(data, false);
                            return true;
                        case R.id.menu_forward_raw:
                            onForward(data, true);
                            return true;
                        case R.id.menu_reply_all:
                            onReply(data, true);
                            return true;
                        case R.id.menu_answer:
                            onAnswer(data);
                            return true;
                        case R.id.menu_unseen:
                            onUnseen(data);
                            return true;
                        case R.id.menu_show_headers:
                            onShowHeaders(data);
                            return true;
                        case R.id.menu_manage_keywords:
                            onManageKeywords(data);
                            return true;
                        case R.id.menu_decrypt:
                            onDecrypt(data);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }

        private void onDelete(final ActionData data) {
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
                                    protected Void onLoad(Context context, Bundle args) {
                                        long id = args.getLong("id");

                                        DB db = DB.getInstance(context);
                                        try {
                                            db.beginTransaction();

                                            EntityMessage message = db.message().getMessage(id);
                                            if (message.uid == null && !TextUtils.isEmpty(message.error)) {
                                                // outbox
                                                db.message().deleteMessage(id);

                                                db.identity().setIdentityError(message.identity, null);

                                                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                nm.cancel("send", message.identity.intValue());
                                            } else
                                                EntityOperation.queue(db, message, EntityOperation.DELETE);

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
                                }.load(context, owner, args);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            } else
                properties.move(data.message.id, EntityFolder.TRASH, true);
        }

        private void onMove(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<List<EntityFolder>>() {
                @Override
                protected List<EntityFolder> onLoad(Context context, Bundle args) {
                    DB db = DB.getInstance(context);

                    EntityMessage message = db.message().getMessage(args.getLong("id"));

                    List<EntityFolder> folders = db.folder().getFolders(message.account);
                    List<EntityFolder> targets = new ArrayList<>();
                    for (EntityFolder folder : folders)
                        if (!folder.hide &&
                                !folder.id.equals(message.folder) &&
                                !EntityFolder.ARCHIVE.equals(folder.type) &&
                                !EntityFolder.TRASH.equals(folder.type) &&
                                !EntityFolder.JUNK.equals(folder.type))
                            targets.add(folder);

                    EntityFolder.sort(context, targets);

                    return targets;
                }

                @Override
                protected void onLoaded(final Bundle args, List<EntityFolder> folders) {
                    View anchor = bnvActions.findViewById(R.id.action_move);
                    PopupMenu popupMenu = new PopupMenu(context, anchor);

                    int order = 0;
                    for (EntityFolder folder : folders)
                        popupMenu.getMenu().add(Menu.NONE, folder.id.intValue(), order++, folder.getDisplayName(context));

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(final MenuItem target) {
                            args.putLong("target", target.getItemId());

                            new SimpleTask<String>() {
                                @Override
                                protected String onLoad(Context context, Bundle args) {
                                    long target = args.getLong("target");
                                    return DB.getInstance(context).folder().getFolder(target).name;
                                }

                                @Override
                                protected void onLoaded(Bundle args, String folderName) {
                                    long id = args.getLong("id");
                                    properties.move(id, folderName, false);
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Helper.unexpectedError(context, owner, ex);
                                }
                            }.load(context, owner, args);

                            return true;
                        }
                    });

                    popupMenu.show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.load(context, owner, args);
        }

        private void onArchive(ActionData data) {
            properties.move(data.message.id, EntityFolder.ARCHIVE, true);
        }

        private void onReply(final ActionData data, final boolean all) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<Boolean>() {
                @Override
                protected Boolean onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    List<EntityAttachment> attachments = DB.getInstance(context).attachment().getAttachments(id);
                    for (EntityAttachment attachment : attachments)
                        if (attachment.cid != null && !attachment.available)
                            return false;
                    return true;
                }

                @Override
                protected void onLoaded(Bundle args, Boolean available) {
                    final Intent reply = new Intent(context, ActivityCompose.class)
                            .putExtra("action", all ? "reply_all" : "reply")
                            .putExtra("reference", data.message.id);
                    if (available)
                        context.startActivity(reply);
                    else
                        new DialogBuilderLifecycle(context, owner)
                                .setMessage(R.string.title_image_unavailable)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        context.startActivity(reply);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.load(context, owner, args);
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
            return new ItemDetailsMessage(this);
        }

        Long getKey() {
            return getKeyAtPosition(getAdapterPosition());
        }
    }

    AdapterMessage(Context context, LifecycleOwner owner, FragmentManager fragmentManager,
                   ViewType viewType, boolean outgoing, int zoom, IProperties properties) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        this.fragmentManager = fragmentManager;
        this.viewType = viewType;
        this.outgoing = outgoing;
        this.internet = (Helper.isMetered(context, false) != null);
        this.properties = properties;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.threading = prefs.getBoolean("threading", true);
        this.compact = prefs.getBoolean("compact", false);
        this.contacts = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED);
        this.avatars = (prefs.getBoolean("avatars", true) && this.contacts);
        this.identicons = prefs.getBoolean("identicons", false);
        this.preview = prefs.getBoolean("preview", false);
        this.confirm = prefs.getBoolean("confirm", false);
        this.debug = prefs.getBoolean("debug", false);

        this.dp24 = Helper.dp2pixels(context, 24);
        this.textSize = Helper.getTextSize(context, zoom);
        this.colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
        this.colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        this.colorUnread = Helper.resolveColor(context, R.attr.colorUnread);
        this.theme = prefs.getString("theme", "light");

        PackageManager pm = context.getPackageManager();
        this.hasWebView = pm.hasSystemFeature("android.software.webview");
    }

    void submitList(PagedList<TupleMessageEx> pagedList) {
        differ.submitList(pagedList);
    }

    PagedList<TupleMessageEx> getCurrentList() {
        return differ.getCurrentList();
    }

    void setZoom(int zoom) {
        textSize = Helper.getTextSize(context, zoom);
        notifyDataSetChanged();
    }

    void checkInternet() {
        boolean internet = (Helper.isMetered(context, false) != null);
        if (this.internet != internet) {
            this.internet = internet;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return differ.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
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
        return new ViewHolder(inflater.inflate(
                compact ? R.layout.item_message_compact : R.layout.item_message_normal,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unbind();
        holder.unwire();

        TupleMessageEx message = differ.getItem(position);
        if (message == null)
            holder.clear();
        else {
            holder.bindTo(position, message);
            holder.wire();
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.unbind();
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
        if (list != null && pos < list.size()) {
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
        Log.i("Item=" + null + " @Key" + key);
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

        void scrollTo(int pos, int dy);

        void move(long id, String target, boolean type);
    }
}
