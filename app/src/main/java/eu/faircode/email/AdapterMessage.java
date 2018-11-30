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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterMessage extends PagedListAdapter<TupleMessageEx, AdapterMessage.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private FragmentManager fragmentManager;
    private ViewType viewType;
    private boolean outgoing;
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
    private String theme;
    private boolean hasWebView;

    private SelectionTracker<Long> selectionTracker = null;

    private DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG);

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    private static final float LOW_LIGHT = 0.6f;
    private static final long CACHE_IMAGE_DURATION = 3 * 24 * 3600 * 1000L;

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
        private ProgressBar pbLoading;

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
        private ProgressBar pbHeaders;

        private BottomNavigationView bnvActions;

        private View vSeparatorBody;
        private Button btnHtml;
        private Button btnImages;
        private TextView tvBody;
        private ProgressBar pbBody;

        private RecyclerView rvAttachment;
        private AdapterAttachment adapter;

        private Group grpAddress;
        private Group grpHeaders;
        private Group grpAttachments;
        private Group grpExpanded;

        private ItemDetailsMessage itemDetails = null;

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

            bnvActions = itemView.findViewById(R.id.bnvActions);

            vSeparatorBody = itemView.findViewById(R.id.vSeparatorBody);
            btnHtml = itemView.findViewById(R.id.btnHtml);
            btnImages = itemView.findViewById(R.id.btnImages);
            tvBody = itemView.findViewById(R.id.tvBody);
            pbBody = itemView.findViewById(R.id.pbBody);

            rvAttachment = itemView.findViewById(R.id.rvAttachment);
            rvAttachment.setHasFixedSize(false);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            rvAttachment.setLayoutManager(llm);
            rvAttachment.setItemAnimator(null);

            adapter = new AdapterAttachment(context, owner, true);
            rvAttachment.setAdapter(adapter);

            grpAddress = itemView.findViewById(R.id.grpAddress);
            grpHeaders = itemView.findViewById(R.id.grpHeaders);
            grpAttachments = itemView.findViewById(R.id.grpAttachments);
            grpExpanded = itemView.findViewById(R.id.grpExpanded);

            tvBody.setMovementMethod(new UrlHandler());

            if (viewType == ViewType.THREAD)
                itemView.setHasTransientState(true);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            ivExpanderAddress.setOnClickListener(this);
            ivAddContact.setOnClickListener(this);
            btnHtml.setOnClickListener(this);
            btnImages.setOnClickListener(this);

            bnvActions.setOnNavigationItemSelectedListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            ivExpanderAddress.setOnClickListener(null);
            ivAddContact.setOnClickListener(null);
            btnHtml.setOnClickListener(null);
            btnImages.setOnClickListener(null);

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
            bnvActions.setVisibility(View.GONE);
            vSeparatorBody.setVisibility(View.GONE);
            btnHtml.setVisibility(View.GONE);
            btnImages.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);

            grpAddress.setVisibility(View.GONE);
            grpHeaders.setVisibility(View.GONE);
            grpAttachments.setVisibility(View.GONE);
            grpExpanded.setVisibility(View.GONE);
        }

        private void bindTo(int position, final TupleMessageEx message) {
            final DB db = DB.getInstance(context);
            final boolean show_expanded = properties.isExpanded(message.id);
            boolean show_addresses = properties.showAddresses(message.id);
            boolean show_headers = properties.showHeaders(message.id);

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

            boolean photo = false;
            if (avatars && !outgoing) {
                if (message.avatar != null)
                    try {
                        ContentResolver resolver = context.getContentResolver();
                        InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(resolver, Uri.parse(message.avatar));
                        if (is != null) {
                            photo = true;
                            ivAvatar.setImageDrawable(Drawable.createFromStream(is, "avatar"));
                        }
                    } catch (SecurityException ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }
            }
            if (!photo && identicons) {
                if (message.from != null && message.from.length > 0) {
                    ivAvatar.setImageBitmap(Identicon.generate(message.from[0].toString(), dp24, 5, "light".equals(theme)));
                    photo = true;
                } else
                    ivAvatar.setImageDrawable(null);
            }
            ivAvatar.setVisibility(photo ? View.VISIBLE : View.GONE);

            vwColor.setBackgroundColor(message.accountColor == null ? Color.TRANSPARENT : message.accountColor);

            ivExpander.setImageResource(show_expanded ? R.drawable.baseline_expand_less_24 : R.drawable.baseline_expand_more_24);
            if (viewType == ViewType.THREAD && threading)
                ivExpander.setVisibility(EntityFolder.DRAFTS.equals(message.folderType) ? View.INVISIBLE : View.VISIBLE);
            else
                ivExpander.setVisibility(View.GONE);

            if (viewType == ViewType.THREAD)
                ivFlagged.setVisibility(message.unflagged == 1 ? View.GONE : View.VISIBLE);
            else
                ivFlagged.setVisibility(message.count - message.unflagged > 0 ? View.VISIBLE : View.GONE);

            tvFrom.setText(MessageHelper.getFormattedAddresses(outgoing ? message.to : message.from, !compact));
            tvSize.setText(message.size == null ? null : Helper.humanReadableByteCount(message.size, true));
            tvSize.setVisibility(message.size == null || message.content ? View.GONE : View.VISIBLE);
            tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.received));

            ivAnswered.setVisibility(message.ui_answered ? View.VISIBLE : View.GONE);
            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);
            tvSubject.setText(message.subject);

            if (viewType == ViewType.UNIFIED)
                tvFolder.setText(message.accountName);
            else
                tvFolder.setText(message.folderDisplay == null
                        ? Helper.localizeFolderName(context, message.folderName)
                        : message.folderDisplay);
            tvFolder.setVisibility(viewType == ViewType.FOLDER ? View.GONE : View.VISIBLE);

            tvPreview.setText(message.preview);
            tvPreview.setVisibility(preview && !TextUtils.isEmpty(message.preview) ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.THREAD || !threading) {
                tvCount.setVisibility(View.GONE);
                ivThread.setVisibility(View.GONE);
            } else {
                tvCount.setText(Integer.toString(message.count));
                ivThread.setVisibility(View.VISIBLE);
            }

            if (debug) {
                db.operation().getOperationsByMessage(message.id).removeObservers(owner);
                db.operation().getOperationsByMessage(message.id).observe(owner, new Observer<List<EntityOperation>>() {
                    @Override
                    public void onChanged(List<EntityOperation> operations) {
                        String text = message.error +
                                "\n" + message.uid + "/" + message.id + " " + df.format(new Date(message.received)) +
                                "\n" + (message.ui_hide ? "HIDDEN " : "") +
                                "seen=" + message.seen + "/" + message.ui_seen + "/" + message.unseen +
                                " found=" + message.ui_found +
                                "\n" + message.msgid +
                                "\n" + message.thread;
                        if (operations != null)
                            for (EntityOperation op : operations)
                                text += "\n" + op.id + ":" + op.name + " " + df.format(new Date(op.created));

                        tvError.setText(text);
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
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

            int colorUnseen = Helper.resolveColor(context, message.unseen > 0
                    ? R.attr.colorUnread : android.R.attr.textColorSecondary);
            tvFrom.setTextColor(colorUnseen);
            tvSize.setTextColor(colorUnseen);
            tvTime.setTextColor(colorUnseen);

            grpAddress.setVisibility(viewType == ViewType.THREAD && show_expanded && show_addresses ? View.VISIBLE : View.GONE);
            tvKeywords.setVisibility(View.GONE);
            ivAddContact.setVisibility(viewType == ViewType.THREAD && show_expanded && contacts && message.from != null ? View.VISIBLE : View.GONE);
            pbHeaders.setVisibility(View.GONE);
            grpHeaders.setVisibility(show_headers && show_expanded ? View.VISIBLE : View.GONE);
            bnvActions.setVisibility(View.GONE);
            vSeparatorBody.setVisibility(View.GONE);
            btnHtml.setVisibility(View.GONE);
            btnImages.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            grpAttachments.setVisibility(message.attachments > 0 && show_expanded ? View.VISIBLE : View.GONE);
            grpExpanded.setVisibility(viewType == ViewType.THREAD && show_expanded ? View.VISIBLE : View.GONE);

            db.folder().liveSystemFolders(message.account).removeObservers(owner);
            db.attachment().liveAttachments(message.id).removeObservers(owner);

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

                tvHeaders.setText(show_headers ? message.headers : null);

                vSeparatorBody.setVisibility(View.VISIBLE);
                tvBody.setText(null);
                pbBody.setVisibility(View.VISIBLE);

                if (message.content) {
                    Bundle args = new Bundle();
                    args.putSerializable("message", message);
                    bodyTask.load(context, owner, args);
                }

                db.folder().liveSystemFolders(message.account).observe(owner, new Observer<List<EntityFolder>>() {
                    @Override
                    public void onChanged(@Nullable List<EntityFolder> folders) {
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

                        bnvActions.getMenu().findItem(R.id.action_delete).setVisible((message.uid != null && hasTrash) || (inOutbox && !TextUtils.isEmpty(message.error)));
                        bnvActions.getMenu().findItem(R.id.action_move).setVisible(message.uid != null);
                        bnvActions.getMenu().findItem(R.id.action_archive).setVisible(message.uid != null && !inArchive && hasArchive);
                        bnvActions.getMenu().findItem(R.id.action_reply).setEnabled(message.content);
                        bnvActions.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);

                        bnvActions.setVisibility(View.VISIBLE);
                        vSeparatorBody.setVisibility(View.GONE);
                    }
                });

                // Observe attachments
                db.attachment().liveAttachments(message.id).observe(owner,
                        new Observer<List<EntityAttachment>>() {
                            @Override
                            public void onChanged(@Nullable List<EntityAttachment> attachments) {
                                if (attachments == null)
                                    attachments = new ArrayList<>();

                                adapter.set(attachments);

                                if (message.content) {
                                    Bundle args = new Bundle();
                                    args.putSerializable("message", message);
                                    bodyTask.load(context, owner, args);
                                }
                            }
                        });
            }

            itemDetails = new ItemDetailsMessage(position, message.id);
            itemView.setActivated(selectionTracker != null && selectionTracker.isSelected(message.id));
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleMessageEx message = getItem(pos);

            if (view.getId() == R.id.ivAddContact)
                onAddContact(message);
            else if (viewType == ViewType.THREAD) {
                if (view.getId() == R.id.ivExpanderAddress)
                    onToggleAddresses(pos, message);
                else if (view.getId() == R.id.btnHtml)
                    onShowHtml(message);
                else if (view.getId() == R.id.btnImages)
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
                                    .putExtra("id", message.id)
                                    .putExtra("found", message.ui_found));
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
                    if (cursor.moveToNext()) {
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
                boolean expanded = !properties.isExpanded(message.id);
                properties.setExpanded(message.id, expanded);
                notifyItemChanged(pos);
            }
        }

        private void onToggleAddresses(int pos, TupleMessageEx message) {
            boolean addresses = !properties.showAddresses(message.id);
            properties.setAddresses(message.id, addresses);
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
            properties.setImages(message.id, true);
            btnImages.setEnabled(false);

            Bundle args = new Bundle();
            args.putSerializable("message", message);
            bodyTask.load(context, owner, args);
        }

        private SimpleTask<Spanned> bodyTask = new SimpleTask<Spanned>() {
            private String body = null;

            @Override
            protected Spanned onLoad(final Context context, final Bundle args) throws Throwable {
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                if (body == null)
                    body = message.read(context);
                return decodeHtml(message, body);
            }

            @Override
            protected void onLoaded(Bundle args, Spanned body) {
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                SpannedString ss = new SpannedString(body);
                boolean has_images = (ss.getSpans(0, ss.length(), ImageSpan.class).length > 0);
                boolean show_expanded = properties.isExpanded(message.id);
                boolean show_images = properties.showImages(message.id);

                btnHtml.setVisibility(hasWebView && show_expanded ? View.VISIBLE : View.GONE);
                btnImages.setVisibility(has_images && show_expanded && !show_images ? View.VISIBLE : View.GONE);
                tvBody.setText(body);
                pbBody.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(context, ex);
            }
        };

        private Spanned decodeHtml(final EntityMessage message, String body) {
            return Html.fromHtml(HtmlHelper.sanitize(body), new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    float scale = context.getResources().getDisplayMetrics().density;
                    int px = Math.round(48 * scale);

                    if (TextUtils.isEmpty(source)) {
                        Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
                        d.setBounds(0, 0, px / 2, px / 2);
                        return d;
                    }

                    boolean embedded = (source.startsWith("cid") && source.split(":").length > 1);

                    if (properties.showImages(message.id)) {
                        // Embedded images
                        if (embedded) {
                            String cid = "<" + source.split(":")[1] + ">";
                            EntityAttachment attachment = DB.getInstance(context).attachment().getAttachment(message.id, cid);
                            if (attachment == null || !attachment.available) {
                                Drawable d = context.getResources().getDrawable(R.drawable.baseline_photo_library_24, context.getTheme());
                                d.setBounds(0, 0, px, px);
                                return d;
                            } else {
                                File file = EntityAttachment.getFile(context, attachment.id);
                                Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                                if (d == null) {
                                    d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
                                    d.setBounds(0, 0, px / 2, px / 2);
                                } else
                                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                                return d;
                            }
                        }

                        // Get cache folder
                        File dir = new File(context.getCacheDir(), "images");
                        dir.mkdir();

                        // Cleanup cache
                        long now = new Date().getTime();
                        File[] images = dir.listFiles();
                        if (images != null)
                            for (File image : images)
                                if (image.isFile() && image.lastModified() + CACHE_IMAGE_DURATION < now) {
                                    Log.i(Helper.TAG, "Deleting from image cache " + image.getName());
                                    image.delete();
                                }

                        InputStream is = null;
                        FileOutputStream os = null;
                        try {
                            // Create unique file name
                            File file = new File(dir, message.id + "_" + source.hashCode());

                            // Get input stream
                            if (file.exists()) {
                                Log.i(Helper.TAG, "Using cached " + file);
                                is = new FileInputStream(file);
                            } else {
                                Log.i(Helper.TAG, "Downloading " + source);
                                is = new URL(source).openStream();
                            }

                            // Decode image from stream
                            Bitmap bm = BitmapFactory.decodeStream(is);
                            if (bm == null)
                                throw new IllegalArgumentException();

                            // Cache bitmap
                            if (!file.exists()) {
                                os = new FileOutputStream(file);
                                bm.compress(Bitmap.CompressFormat.PNG, 100, os);
                            }

                            // Create drawable from bitmap
                            Drawable d = new BitmapDrawable(context.getResources(), bm);
                            d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                            return d;
                        } catch (Throwable ex) {
                            // Show warning icon
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
                            d.setBounds(0, 0, px / 2, px / 2);
                            return d;
                        } finally {
                            // Close streams
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    Log.w(Helper.TAG, e + "\n" + Log.getStackTraceString(e));
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    Log.w(Helper.TAG, e + "\n" + Log.getStackTraceString(e));
                                }
                            }
                        }
                    } else {
                        // Show placeholder icon
                        int resid = (embedded ? R.drawable.baseline_photo_library_24 : R.drawable.baseline_image_24);
                        Drawable d = context.getResources().getDrawable(resid, context.getTheme());
                        d.setBounds(0, 0, px, px);
                        return d;
                    }
                }
            }, new Html.TagHandler() {
                @Override
                public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                    if (BuildConfig.DEBUG)
                        Log.i(Helper.TAG, "HTML tag=" + tag + " opening=" + opening);
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

                    if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                        Toast.makeText(context, context.getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
                        return true;
                    }

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

                                        if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                                            Toast.makeText(context, context.getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        Helper.view(context, uri);
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
                    onReply(data);
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
                                        db.message().setMessageUiHide(id, true);

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    EntityOperation.process(context);

                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Helper.unexpectedError(context, ex);
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
                    Helper.unexpectedError(context, ex);
                }
            }.load(context, owner, args);
        }

        private void onReplyAll(ActionData data) {
            context.startActivity(new Intent(context, ActivityCompose.class)
                    .putExtra("action", "reply_all")
                    .putExtra("reference", data.message.id));
        }

        private void onAnswer(final ActionData data) {
            final DB db = DB.getInstance(context);
            db.answer().liveAnswers().observe(owner, new Observer<List<EntityAnswer>>() {
                @Override
                public void onChanged(List<EntityAnswer> answers) {
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

                    db.answer().liveAnswers().removeObservers(owner);
                }
            });
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
                        db.message().setMessageUiSeen(message.id, false);
                        db.message().setMessageUiIgnored(message.id, true);
                        EntityOperation.queue(db, message, EntityOperation.SEEN, false);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    EntityOperation.process(context);

                    return null;
                }

                @Override
                protected void onLoaded(Bundle args, Void ignored) {
                    properties.setExpanded(data.message.id, false);
                    notifyDataSetChanged();
                }
            }.load(context, owner, args);
        }

        private void onFlag(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);
            args.putBoolean("flagged", !data.message.ui_flagged);
            Log.i(Helper.TAG, "Set message id=" + data.message.id + " flagged=" + !data.message.ui_flagged);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    boolean flagged = args.getBoolean("flagged");
                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    db.message().setMessageUiFlagged(message.id, flagged);
                    EntityOperation.queue(db, message, EntityOperation.FLAG, flagged);
                    EntityOperation.process(context);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, ex);
                }
            }.load(context, owner, args);
        }

        private void onShowHeaders(ActionData data) {
            boolean show_headers = !properties.showHeaders(data.message.id);
            properties.setHeaders(data.message.id, show_headers);
            if (show_headers && data.message.headers == null) {
                grpHeaders.setVisibility(View.VISIBLE);
                pbHeaders.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", data.message.id);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        Long id = args.getLong("id");
                        DB db = DB.getInstance(context);
                        EntityMessage message = db.message().getMessage(id);
                        EntityOperation.queue(db, message, EntityOperation.HEADERS);
                        EntityOperation.process(context);
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, ex);
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
                protected EntityFolder onLoad(Context context, Bundle args) throws Throwable {
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
                                        protected Void onLoad(Context context, Bundle args) throws Throwable {
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

                                            EntityOperation.process(context);

                                            return null;
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
                                                            protected Void onLoad(Context context, Bundle args) throws Throwable {
                                                                EntityMessage message = (EntityMessage) args.getSerializable("message");
                                                                String keyword = args.getString("keyword");

                                                                DB db = DB.getInstance(context);
                                                                EntityOperation.queue(db, message, EntityOperation.KEYWORD, keyword, true);
                                                                EntityOperation.process(context);

                                                                return null;
                                                            }
                                                        }.load(context, owner, args);
                                                    }
                                                }
                                            }).show();
                                }
                            })
                            .show();
                }
            }.load(context, owner, args);
        }

        private void onDecrypt(ActionData data) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_DECRYPT)
                            .putExtra("id", data.message.id)
                            .putExtra("to", ((InternetAddress) data.message.to[0]).getAddress()));
        }

        private void onMore(final ActionData data) {
            boolean show_headers = properties.showHeaders(data.message.id);

            View anchor = bnvActions.findViewById(R.id.action_more);
            PopupMenu popupMenu = new PopupMenu(context, anchor);
            popupMenu.inflate(R.menu.menu_message);
            popupMenu.getMenu().findItem(R.id.menu_junk).setVisible(data.message.uid != null && data.hasJunk);

            popupMenu.getMenu().findItem(R.id.menu_forward).setEnabled(data.message.content);
            popupMenu.getMenu().findItem(R.id.menu_forward_raw).setVisible(data.message.content && data.message.headers != null);

            popupMenu.getMenu().findItem(R.id.menu_reply_all).setEnabled(data.message.content);

            popupMenu.getMenu().findItem(R.id.menu_answer).setEnabled(data.message.content);

            popupMenu.getMenu().findItem(R.id.menu_unseen).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_flag).setChecked(data.message.unflagged != 1);
            popupMenu.getMenu().findItem(R.id.menu_flag).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_show_headers).setChecked(show_headers);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_manage_keywords).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_decrypt).setEnabled(data.message.to != null && data.message.to.length > 0);

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
                            onReplyAll(data);
                            return true;
                        case R.id.menu_answer:
                            onAnswer(data);
                            return true;
                        case R.id.menu_unseen:
                            onUnseen(data);
                            return true;
                        case R.id.menu_flag:
                            onFlag(data);
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
                                                NotificationManager nm = context.getSystemService(NotificationManager.class);
                                                nm.cancel("send", message.account.intValue());
                                            } else {
                                                db.message().setMessageUiHide(message.id, true);
                                                EntityOperation.queue(db, message, EntityOperation.DELETE);
                                            }

                                            db.setTransactionSuccessful();
                                        } finally {
                                            db.endTransaction();
                                        }

                                        EntityOperation.process(context);

                                        return null;
                                    }

                                    @Override
                                    protected void onException(Bundle args, Throwable ex) {
                                        Helper.unexpectedError(context, ex);
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
                        if (!folder.hide && !folder.id.equals(message.folder))
                            targets.add(folder);

                    EntityFolder.sort(targets);

                    return targets;
                }

                @Override
                protected void onLoaded(final Bundle args, List<EntityFolder> folders) {
                    View anchor = bnvActions.findViewById(R.id.action_move);
                    PopupMenu popupMenu = new PopupMenu(context, anchor);

                    int order = 0;
                    for (EntityFolder folder : folders) {
                        String name = (folder.display == null
                                ? Helper.localizeFolderName(context, folder.name)
                                : folder.display);
                        popupMenu.getMenu().add(Menu.NONE, folder.id.intValue(), order++, name);
                    }

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
                                    Helper.unexpectedError(context, ex);
                                }
                            }.load(context, owner, args);

                            return true;
                        }
                    });

                    popupMenu.show();
                }
            }.load(context, owner, args);
        }

        private void onArchive(ActionData data) {
            properties.move(data.message.id, EntityFolder.ARCHIVE, true);
        }

        private void onReply(ActionData data) {
            context.startActivity(new Intent(context, ActivityCompose.class)
                    .putExtra("action", "reply")
                    .putExtra("reference", data.message.id));
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
            return itemDetails;
        }
    }

    AdapterMessage(Context context, LifecycleOwner owner, FragmentManager fragmentManager,
                   ViewType viewType, boolean outgoing, IProperties properties) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.owner = owner;
        this.fragmentManager = fragmentManager;
        this.viewType = viewType;
        this.outgoing = outgoing;
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

        this.dp24 = Math.round(24 * context.getResources().getDisplayMetrics().density);
        this.theme = prefs.getString("theme", "light");

        PackageManager pm = context.getPackageManager();
        this.hasWebView = pm.hasSystemFeature("android.software.webview");
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
                    return prev.equals(next);
                }
            };

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(
                compact ? R.layout.item_message_compact : R.layout.item_message_normal,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleMessageEx message = getItem(position);
        if (message == null)
            holder.clear();
        else {
            holder.bindTo(position, message);
            holder.wire();
        }
    }

    void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    interface IProperties {
        void setExpanded(long id, boolean expand);

        void setAddresses(long id, boolean show);

        void setHeaders(long id, boolean show);

        void setImages(long id, boolean show);

        boolean isExpanded(long id);

        boolean showAddresses(long id);

        boolean showHeaders(long id);

        boolean showImages(long id);

        void move(long id, String target, boolean type);
    }
}
